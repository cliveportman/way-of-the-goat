package co.theportman.way_of_the_goat.data.cache

import android.content.Context
import co.theportman.way_of_the_goat.data.database.RoomTransactionRunner
import co.theportman.way_of_the_goat.data.database.WayOfTheGoatDatabase
import co.theportman.way_of_the_goat.data.repository.ServingsRepository
import co.theportman.way_of_the_goat.data.scoring.DailyScoreResult
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import co.theportman.way_of_the_goat.util.logError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Shared in-memory cache manager for food servings data.
 *
 * Ported from the KMP build. The only change for native Android is initialization:
 * it now builds a Room [WayOfTheGoatDatabase] from a [Context] and wires the DAOs
 * into [ServingsRepository], replacing the SQLDelight `DatabaseDriverFactory`.
 * Caching, reactive [StateFlow] updates, and range loading are unchanged.
 */
class ServingsDataManager private constructor() : ServingsDataSource {

    private var repository: ServingsRepository? = null
    private val mutex = Mutex()

    private val cachedServings = mutableMapOf<LocalDate, DailyServings>()

    private var oldestLoadedDate: LocalDate? = null
    private var newestLoadedDate: LocalDate? = null

    private val loadingRanges = mutableSetOf<Pair<LocalDate, LocalDate>>()

    private val _activeSuite = MutableStateFlow(SuiteDefinitions.defaultSuite)
    val activeSuite: StateFlow<ScoringSuite> = _activeSuite.asStateFlow()

    private val _servingsFlow = MutableStateFlow<Map<LocalDate, DailyServings>>(emptyMap())
    override val servingsFlow: StateFlow<Map<LocalDate, DailyServings>> = _servingsFlow.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    /**
     * Initialize with an Android [Context]. Builds the Room database, wires the
     * repository, and loads user preferences. Idempotent. Must be called before
     * other methods.
     */
    suspend fun initialize(context: Context): Result<Unit> {
        return mutex.withLock {
            try {
                if (repository != null) {
                    return@withLock Result.success(Unit)
                }

                val database = WayOfTheGoatDatabase.create(context)
                val repo = ServingsRepository(
                    preferencesDao = database.userPreferencesDao(),
                    dailyServingsDao = database.dailyServingsDao(),
                    categoryServingsDao = database.categoryServingsDao(),
                    transactionRunner = RoomTransactionRunner(database),
                )
                repository = repo

                repo.initialize().fold(
                    onSuccess = {
                        _activeSuite.value = repo.activeSuite.value
                        _isInitialized.value = true
                        Result.success(Unit)
                    },
                    onFailure = { Result.failure(it) },
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun loadInitialData(aroundDate: LocalDate, bufferDays: Int): Result<Unit> {
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

    suspend fun ensureDateLoaded(date: LocalDate, bufferDays: Int = 30): Boolean {
        return mutex.withLock {
            val requiredOldest = date.minus(bufferDays, DateTimeUnit.DAY)
            val requiredNewest = date.plus(bufferDays, DateTimeUnit.DAY)

            var didLoad = false

            val currentOldest = oldestLoadedDate
            if (currentOldest == null || requiredOldest < currentOldest) {
                val loadOldest = requiredOldest
                val loadNewest = currentOldest?.minus(1, DateTimeUnit.DAY) ?: date
                if (loadOldest <= loadNewest) {
                    loadDateRangeInternal(loadOldest, loadNewest).fold(
                        onSuccess = { didLoad = true },
                        onFailure = { e -> logError("ServingsDataManager", "Failed to load date range: ${e.message}") },
                    )
                }
            }

            val currentNewest = newestLoadedDate
            if (currentNewest == null || requiredNewest > currentNewest) {
                val loadOldest = currentNewest?.plus(1, DateTimeUnit.DAY) ?: date
                val loadNewest = requiredNewest
                if (loadOldest <= loadNewest) {
                    loadDateRangeInternal(loadOldest, loadNewest).fold(
                        onSuccess = { didLoad = true },
                        onFailure = { e -> logError("ServingsDataManager", "Failed to load date range: ${e.message}") },
                    )
                }
            }

            didLoad
        }
    }

    fun getServingsForDate(date: LocalDate): DailyServings? = _servingsFlow.value[date]

    suspend fun getServingsForDateAsync(date: LocalDate): Result<DailyServings?> {
        _servingsFlow.value[date]?.let { return Result.success(it) }
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

    suspend fun updateCategoryServings(
        date: LocalDate,
        categoryId: CategoryId,
        servingCount: Int,
    ): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))
        return repo.updateCategoryServings(date, categoryId, servingCount).also { result ->
            if (result.isSuccess) {
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

    suspend fun incrementServings(
        date: LocalDate,
        categoryId: CategoryId,
        maxServings: Int = 6,
    ): Result<Unit> {
        val current = getServingsForDate(date)
        val currentCount = current?.getServings(categoryId) ?: 0
        if (currentCount >= maxServings) {
            return Result.success(Unit)
        }
        return updateCategoryServings(date, categoryId, currentCount + 1)
    }

    suspend fun decrementServings(
        date: LocalDate,
        categoryId: CategoryId,
    ): Result<Unit> {
        val current = getServingsForDate(date)
        val currentCount = current?.getServings(categoryId) ?: 0
        if (currentCount <= 0) {
            return Result.success(Unit)
        }
        return updateCategoryServings(date, categoryId, currentCount - 1)
    }

    suspend fun calculateScoreForDate(date: LocalDate): DailyScoreResult? {
        val repo = repository ?: return null
        return repo.calculateScoreForDate(date).getOrNull()
    }

    suspend fun setActiveSuite(suiteId: SuiteId): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))
        return repo.setActiveSuite(suiteId).also { result ->
            if (result.isSuccess) {
                val suite = SuiteDefinitions.getSuiteById(suiteId) ?: SuiteDefinitions.defaultSuite
                _activeSuite.value = suite
            }
        }
    }

    suspend fun getLastUsedProfile(beforeDate: LocalDate): SuiteId? {
        val repo = repository ?: return null
        return repo.getLastServingsBeforeDate(beforeDate).fold(
            onSuccess = { servings -> servings?.suiteId },
            onFailure = { null },
        )
    }

    suspend fun createEmptyDayWithSuite(date: LocalDate, suiteId: SuiteId): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))

        val ts = Clock.System.now().toEpochMilliseconds()
        val emptyServings = DailyServings(
            id = 0,
            date = date,
            suiteId = suiteId,
            servings = emptyMap(),
            createdAt = ts,
            updatedAt = ts,
        )

        val saveResult = repo.saveServings(emptyServings)
        if (saveResult.isSuccess) {
            repo.getServingsForDate(date).onSuccess { servings ->
                mutex.withLock {
                    if (servings != null) {
                        cachedServings[date] = servings
                    }
                    _servingsFlow.value = cachedServings.toMap()
                }
            }
        }
        return saveResult
    }

    fun isDateLoaded(date: LocalDate): Boolean {
        val oldest = oldestLoadedDate ?: return false
        val newest = newestLoadedDate ?: return false
        return date in oldest..newest
    }

    fun getServingsForRange(oldest: LocalDate, newest: LocalDate): List<DailyServings> {
        return _servingsFlow.value.filter { (date, _) ->
            date >= oldest && date <= newest
        }.values.sortedByDescending { it.date }
    }

    private suspend fun loadDateRangeInternal(oldest: LocalDate, newest: LocalDate): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))

        val range = Pair(oldest, newest)
        if (loadingRanges.contains(range)) {
            return Result.success(Unit)
        }
        loadingRanges.add(range)

        return try {
            repo.getServingsForRange(oldest, newest).fold(
                onSuccess = { servingsList ->
                    servingsList.forEach { servings -> cachedServings[servings.date] = servings }

                    val prevOldest = oldestLoadedDate
                    if (prevOldest == null || oldest < prevOldest) {
                        oldestLoadedDate = oldest
                    }
                    val prevNewest = newestLoadedDate
                    if (prevNewest == null || newest > prevNewest) {
                        newestLoadedDate = newest
                    }

                    _servingsFlow.value = cachedServings.toMap()
                    loadingRanges.remove(range)
                    Result.success(Unit)
                },
                onFailure = { error ->
                    loadingRanges.remove(range)
                    Result.failure(error)
                },
            )
        } catch (e: Exception) {
            loadingRanges.remove(range)
            Result.failure(e)
        }
    }

    suspend fun clearCache() {
        mutex.withLock {
            cachedServings.clear()
            oldestLoadedDate = null
            newestLoadedDate = null
            loadingRanges.clear()
            _servingsFlow.value = emptyMap()
        }
    }

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

    suspend fun deleteServingsForDate(date: LocalDate): Result<Unit> {
        val repo = repository ?: return Result.failure(IllegalStateException("Not initialized"))
        return repo.deleteServingsForDate(date).also { result ->
            if (result.isSuccess) {
                mutex.withLock {
                    cachedServings.remove(date)
                    _servingsFlow.value = cachedServings.toMap()
                }
            }
        }
    }

    fun hasServingsForDate(date: LocalDate): Boolean {
        val servings = _servingsFlow.value[date]
        return servings != null && servings.servings.values.any { it > 0 }
    }

    companion object {
        val instance: ServingsDataManager by lazy { ServingsDataManager() }
    }
}
