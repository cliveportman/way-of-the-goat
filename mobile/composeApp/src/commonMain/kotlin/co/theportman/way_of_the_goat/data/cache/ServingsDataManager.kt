package co.theportman.way_of_the_goat.data.cache

import co.theportman.way_of_the_goat.data.database.DatabaseDriverFactory
import co.theportman.way_of_the_goat.data.database.WayOfTheGoatDatabase
import co.theportman.way_of_the_goat.data.repository.ServingsRepository
import co.theportman.way_of_the_goat.data.scoring.DailyScoreResult
import co.theportman.way_of_the_goat.data.scoring.ScoreCalculator
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.DailyServingsBuilder
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Shared cache manager for food servings data.
 * Follows the pattern established by ActivityDataManager.
 *
 * Provides:
 * - Cached access to servings data
 * - Reactive StateFlow updates
 * - Date range loading with buffer
 * - Active suite management
 */
class ServingsDataManager private constructor() {

    private var repository: ServingsRepository? = null
    private val mutex = Mutex()

    // Cached servings data by date
    private val cachedServings = mutableMapOf<LocalDate, DailyServings>()

    // Track loaded date ranges
    private var oldestLoadedDate: LocalDate? = null
    private var newestLoadedDate: LocalDate? = null

    // Track currently loading ranges to prevent duplicates
    private val loadingRanges = mutableSetOf<Pair<LocalDate, LocalDate>>()

    // Expose active suite
    private val _activeSuite = MutableStateFlow(SuiteDefinitions.defaultSuite)
    val activeSuite: StateFlow<ScoringSuite> = _activeSuite.asStateFlow()

    // Expose servings data as flow for reactive updates
    private val _servingsFlow = MutableStateFlow<Map<LocalDate, DailyServings>>(emptyMap())
    val servingsFlow: StateFlow<Map<LocalDate, DailyServings>> = _servingsFlow.asStateFlow()

    // Initialization state
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    /**
     * Initialize with database driver factory.
     * Must be called before using other methods.
     */
    suspend fun initialize(driverFactory: DatabaseDriverFactory): Result<Unit> {
        return mutex.withLock {
            try {
                if (repository != null) {
                    return@withLock Result.success(Unit)
                }

                val driver = driverFactory.createDriver()
                val database = WayOfTheGoatDatabase(driver)
                repository = ServingsRepository(database)

                // Initialize repository (loads user preferences)
                repository!!.initialize().fold(
                    onSuccess = {
                        _activeSuite.value = repository!!.activeSuite.value
                        _isInitialized.value = true
                        Result.success(Unit)
                    },
                    onFailure = { Result.failure(it) }
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Load initial data around a specific date.
     */
    suspend fun loadInitialData(aroundDate: LocalDate, bufferDays: Int = 30): Result<Unit> {
        return mutex.withLock {
            try {
                val oldest = aroundDate.minus(bufferDays, DateTimeUnit.DAY)
                val newest = aroundDate.plus(bufferDays, DateTimeUnit.DAY)

                loadDateRangeInternal(oldest, newest)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Ensure a specific date + buffer is loaded.
     * Returns true if new data was loaded, false if already present.
     */
    suspend fun ensureDateLoaded(date: LocalDate, bufferDays: Int = 30): Boolean {
        return mutex.withLock {
            val requiredOldest = date.minus(bufferDays, DateTimeUnit.DAY)
            val requiredNewest = date.plus(bufferDays, DateTimeUnit.DAY)

            var didLoad = false

            // Check if we need to load older data
            if (oldestLoadedDate == null || requiredOldest < oldestLoadedDate!!) {
                val loadOldest = requiredOldest
                val loadNewest = oldestLoadedDate?.minus(1, DateTimeUnit.DAY) ?: date
                if (loadOldest <= loadNewest) {
                    loadDateRangeInternal(loadOldest, loadNewest).fold(
                        onSuccess = { didLoad = true },
                        onFailure = { /* Log error but don't fail */ }
                    )
                }
            }

            // Check if we need to load newer data
            if (newestLoadedDate == null || requiredNewest > newestLoadedDate!!) {
                val loadOldest = newestLoadedDate?.plus(1, DateTimeUnit.DAY) ?: date
                val loadNewest = requiredNewest
                if (loadOldest <= loadNewest) {
                    loadDateRangeInternal(loadOldest, loadNewest).fold(
                        onSuccess = { didLoad = true },
                        onFailure = { /* Log error but don't fail */ }
                    )
                }
            }

            didLoad
        }
    }

    /**
     * Get servings for a specific date (from cache).
     * Returns null if not loaded or no data exists for that date.
     */
    fun getServingsForDate(date: LocalDate): DailyServings? {
        return cachedServings[date]
    }

    /**
     * Get servings for a specific date, loading from DB if needed.
     */
    suspend fun getServingsForDateAsync(date: LocalDate): Result<DailyServings?> {
        // Check cache first
        cachedServings[date]?.let { return Result.success(it) }

        // Load from database
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))

        return repo.getServingsForDate(date).map { servings ->
            servings?.also {
                mutex.withLock {
                    cachedServings[date] = it
                    _servingsFlow.value = cachedServings.toMap()
                }
            }
        }
    }

    /**
     * Save servings for a date.
     */
    suspend fun saveServings(dailyServings: DailyServings): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))

        return repo.saveServings(dailyServings).also { result ->
            if (result.isSuccess) {
                mutex.withLock {
                    cachedServings[dailyServings.date] = dailyServings
                    _servingsFlow.value = cachedServings.toMap()
                }
            }
        }
    }

    /**
     * Update a single category's serving count.
     * Creates the daily record if it doesn't exist.
     */
    suspend fun updateCategoryServings(
        date: LocalDate,
        categoryId: CategoryId,
        servingCount: Int
    ): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))

        return repo.updateCategoryServings(date, categoryId, servingCount).also { result ->
            if (result.isSuccess) {
                // Refresh cache for this date
                repo.getServingsForDate(date).onSuccess { servings ->
                    mutex.withLock {
                        if (servings != null) {
                            cachedServings[date] = servings
                        } else {
                            cachedServings.remove(date)
                        }
                        _servingsFlow.value = cachedServings.toMap()
                    }
                }
            }
        }
    }

    /**
     * Increment servings for a category on a date.
     */
    suspend fun incrementServings(
        date: LocalDate,
        categoryId: CategoryId,
        maxServings: Int = 6
    ): Result<Unit> {
        val current = getServingsForDate(date)
        val currentCount = current?.getServings(categoryId) ?: 0

        if (currentCount >= maxServings) {
            return Result.success(Unit) // Already at max
        }

        return updateCategoryServings(date, categoryId, currentCount + 1)
    }

    /**
     * Decrement servings for a category on a date.
     */
    suspend fun decrementServings(
        date: LocalDate,
        categoryId: CategoryId
    ): Result<Unit> {
        val current = getServingsForDate(date)
        val currentCount = current?.getServings(categoryId) ?: 0

        if (currentCount <= 0) {
            return Result.success(Unit) // Already at zero
        }

        return updateCategoryServings(date, categoryId, currentCount - 1)
    }

    /**
     * Calculate score for a date.
     */
    suspend fun calculateScoreForDate(date: LocalDate): DailyScoreResult? {
        val repo = repository ?: return null
        return repo.calculateScoreForDate(date).getOrNull()
    }

    /**
     * Switch active suite.
     */
    suspend fun setActiveSuite(suiteId: SuiteId): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))

        return repo.setActiveSuite(suiteId).also { result ->
            if (result.isSuccess) {
                val suite = SuiteDefinitions.getSuiteById(suiteId) ?: SuiteDefinitions.defaultSuite
                _activeSuite.value = suite
            }
        }
    }

    /**
     * Check if a specific date is loaded.
     */
    fun isDateLoaded(date: LocalDate): Boolean {
        return oldestLoadedDate != null &&
                newestLoadedDate != null &&
                date >= oldestLoadedDate!! &&
                date <= newestLoadedDate!!
    }

    /**
     * Get servings for a date range (from cache).
     */
    fun getServingsForRange(oldest: LocalDate, newest: LocalDate): List<DailyServings> {
        return cachedServings.filter { (date, _) ->
            date >= oldest && date <= newest
        }.values.sortedByDescending { it.date }
    }

    /**
     * Internal method to load a date range.
     * Must be called within mutex lock.
     */
    private suspend fun loadDateRangeInternal(oldest: LocalDate, newest: LocalDate): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))

        // Check if this range is already being loaded
        val range = Pair(oldest, newest)
        if (loadingRanges.contains(range)) {
            return Result.success(Unit)
        }

        loadingRanges.add(range)

        return try {
            repo.getServingsForRange(oldest, newest).fold(
                onSuccess = { servingsList ->
                    // Add to cache
                    servingsList.forEach { servings ->
                        cachedServings[servings.date] = servings
                    }

                    // Update loaded date range
                    if (oldestLoadedDate == null || oldest < oldestLoadedDate!!) {
                        oldestLoadedDate = oldest
                    }
                    if (newestLoadedDate == null || newest > newestLoadedDate!!) {
                        newestLoadedDate = newest
                    }

                    // Emit updated servings
                    _servingsFlow.value = cachedServings.toMap()

                    loadingRanges.remove(range)
                    Result.success(Unit)
                },
                onFailure = { error ->
                    loadingRanges.remove(range)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            loadingRanges.remove(range)
            Result.failure(e)
        }
    }

    /**
     * Clear all cached data.
     */
    fun clearCache() {
        cachedServings.clear()
        oldestLoadedDate = null
        newestLoadedDate = null
        loadingRanges.clear()
        _servingsFlow.value = emptyMap()
    }

    /**
     * Refresh data for a specific date from the database.
     */
    suspend fun refreshDate(date: LocalDate): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))

        return repo.getServingsForDate(date).map { servings ->
            mutex.withLock {
                if (servings != null) {
                    cachedServings[date] = servings
                } else {
                    cachedServings.remove(date)
                }
                _servingsFlow.value = cachedServings.toMap()
            }
        }
    }

    companion object {
        /**
         * Singleton instance.
         */
        val instance: ServingsDataManager by lazy { ServingsDataManager() }
    }
}
