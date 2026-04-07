package co.theportman.way_of_the_goat.testutils

import co.theportman.way_of_the_goat.data.cache.ServingsDataSource
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

/**
 * Fake [ServingsDataSource] for ViewModel tests.
 *
 * Provides controllable [servingsFlow] and [loadInitialData] behaviour
 * without requiring a real database.
 */
class FakeServingsDataSource : ServingsDataSource {

    private val _isInitialized = MutableStateFlow(true)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _servingsFlow = MutableStateFlow<Map<LocalDate, DailyServings>>(emptyMap())
    override val servingsFlow: StateFlow<Map<LocalDate, DailyServings>> = _servingsFlow.asStateFlow()

    /** Set to non-null to make [loadInitialData] fail. */
    var loadError: Throwable? = null

    /** The servings map that [loadInitialData] will populate on success. */
    var servingsToLoad: Map<LocalDate, DailyServings> = emptyMap()

    override suspend fun loadInitialData(aroundDate: LocalDate, bufferDays: Int): Result<Unit> {
        loadError?.let { return Result.failure(it) }
        _servingsFlow.value = servingsToLoad
        return Result.success(Unit)
    }

    /** Emit new servings to simulate external changes after initial load. */
    fun emitServings(servings: Map<LocalDate, DailyServings>) {
        _servingsFlow.value = servings
    }

    fun setInitialized(value: Boolean) {
        _isInitialized.value = value
    }
}
