package co.theportman.way_of_the_goat.testutils

import co.theportman.way_of_the_goat.data.cache.ActivityDataSource
import co.theportman.way_of_the_goat.data.remote.models.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

/**
 * Fake [ActivityDataSource] for ViewModel tests.
 *
 * Provides controllable [activitiesFlow] and [loadInitialData] behaviour
 * without requiring a real API client.
 */
class FakeActivityDataSource : ActivityDataSource {

    private val _activitiesFlow = MutableStateFlow<List<Activity>>(emptyList())
    override val activitiesFlow: StateFlow<List<Activity>> = _activitiesFlow.asStateFlow()

    /** Set to non-null to make [loadInitialData] fail. */
    var loadError: Throwable? = null

    /** The activities list that [loadInitialData] will populate on success. */
    var activitiesToLoad: List<Activity> = emptyList()

    override suspend fun loadInitialData(aroundDate: LocalDate, bufferDays: Int): Result<Unit> {
        loadError?.let { return Result.failure(it) }
        _activitiesFlow.value = activitiesToLoad
        return Result.success(Unit)
    }

    /** Emit new activities to simulate external changes after initial load. */
    fun emitActivities(activities: List<Activity>) {
        _activitiesFlow.value = activities
    }
}
