package co.theportman.way_of_the_goat.data.cache

import co.theportman.way_of_the_goat.data.remote.models.Activity
import co.theportman.way_of_the_goat.data.repository.IntervalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Shared singleton cache for activity data
 * Used by both ProgressViewModel and ScoresViewModel to avoid duplicate API calls
 */
object ActivityDataManager {

    private val repository = IntervalsRepository()
    private val mutex = Mutex()

    // All loaded activities
    private val allActivities = mutableListOf<Activity>()

    // Track loaded date ranges
    private var oldestLoadedDate: LocalDate? = null
    private var newestLoadedDate: LocalDate? = null

    // Track currently loading dates to prevent duplicates
    private val loadingRanges = mutableSetOf<Pair<LocalDate, LocalDate>>()

    // Expose activities as StateFlow for reactive updates
    private val _activitiesFlow = MutableStateFlow<List<Activity>>(emptyList())
    val activitiesFlow: StateFlow<List<Activity>> = _activitiesFlow.asStateFlow()

    /**
     * Load initial data around a specific date
     * Loads targetDate ± bufferDays
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
     * Ensure a specific date + buffer is loaded
     * Returns true if new data was loaded, false if already present
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
     * Check if a specific date is loaded
     */
    fun isDateLoaded(date: LocalDate): Boolean {
        return oldestLoadedDate != null &&
                newestLoadedDate != null &&
                date >= oldestLoadedDate!! &&
                date <= newestLoadedDate!!
    }

    /**
     * Get activities for a specific date
     */
    fun getActivitiesForDate(date: LocalDate): List<Activity> {
        return allActivities.filter { activity ->
            activity.startDateLocal.isNotEmpty() &&
            activity.startDateLocal.substringBefore('T') == date.toString()
        }
    }

    /**
     * Get activities for a date range
     */
    fun getActivitiesForRange(oldest: LocalDate, newest: LocalDate): List<Activity> {
        return allActivities.filter { activity ->
            if (activity.startDateLocal.isEmpty()) return@filter false
            val activityDate = activity.startDateLocal.substringBefore('T')
            activityDate >= oldest.toString() && activityDate <= newest.toString()
        }
    }

    /**
     * Internal method to load a date range
     * Must be called within mutex lock
     */
    private suspend fun loadDateRangeInternal(oldest: LocalDate, newest: LocalDate): Result<Unit> {
        // Check if this range is already being loaded
        val range = Pair(oldest, newest)
        if (loadingRanges.contains(range)) {
            return Result.success(Unit)
        }

        loadingRanges.add(range)

        return try {
            val result = repository.getActivities(
                oldest = oldest.toString(),
                newest = newest.toString()
            )

            result.fold(
                onSuccess = { newActivities ->
                    // Add new activities and remove duplicates
                    val existingIds = allActivities.map { it.id }.toSet()
                    val uniqueNewActivities = newActivities.filter { it.id !in existingIds }
                    allActivities.addAll(uniqueNewActivities)

                    // Update date range
                    if (oldestLoadedDate == null || oldest < oldestLoadedDate!!) {
                        oldestLoadedDate = oldest
                    }
                    if (newestLoadedDate == null || newest > newestLoadedDate!!) {
                        newestLoadedDate = newest
                    }

                    // Emit updated activities
                    _activitiesFlow.value = allActivities.toList()

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
     * Clear all cached data
     */
    fun clear() {
        allActivities.clear()
        oldestLoadedDate = null
        newestLoadedDate = null
        loadingRanges.clear()
        _activitiesFlow.value = emptyList()
    }

    /**
     * Clean up resources
     */
    fun close() {
        repository.close()
    }
}
