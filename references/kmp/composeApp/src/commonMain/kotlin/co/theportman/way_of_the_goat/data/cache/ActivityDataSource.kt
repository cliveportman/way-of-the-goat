package co.theportman.way_of_the_goat.data.cache

import co.theportman.way_of_the_goat.data.remote.models.Activity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

/**
 * Read-only data source for activities, used by ViewModels.
 *
 * Extracted from [ActivityDataManager] to allow fake implementations in tests.
 * Mirrors the [ServingsDataSource] pattern.
 */
interface ActivityDataSource {
    val activitiesFlow: StateFlow<List<Activity>>
    suspend fun loadInitialData(aroundDate: LocalDate, bufferDays: Int = 30): Result<Unit>
}
