package co.theportman.way_of_the_goat.data.cache

import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

/**
 * Read-only data source for servings, used by ViewModels.
 *
 * Extracted from [ServingsDataManager] to allow fake implementations in tests.
 */
interface ServingsDataSource {
    val isInitialized: StateFlow<Boolean>
    val servingsFlow: StateFlow<Map<LocalDate, DailyServings>>
    suspend fun loadInitialData(aroundDate: LocalDate, bufferDays: Int = 30): Result<Unit>
}
