package co.theportman.way_of_the_goat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.theportman.way_of_the_goat.data.cache.ServingsDataManager
import co.theportman.way_of_the_goat.data.scoring.WeeklyScoreBuilder
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.WeekScoreData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for the Scores Over Time screen.
 *
 * Delegates week grouping and score calculation to [WeeklyScoreBuilder].
 */
class ScoresOverTimeViewModel(
    private val servingsDataManager: ServingsDataManager = ServingsDataManager.instance,
    private val weeklyScoreBuilder: WeeklyScoreBuilder = WeeklyScoreBuilder()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScoresOverTimeUiState>(ScoresOverTimeUiState.Loading)
    val uiState: StateFlow<ScoresOverTimeUiState> = _uiState.asStateFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    init {
        loadData()
        observeServingsChanges()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = ScoresOverTimeUiState.Loading

            if (!servingsDataManager.isInitialized.value) {
                _uiState.value = ScoresOverTimeUiState.Empty
                return@launch
            }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            servingsDataManager.loadInitialData(aroundDate = today, bufferDays = 91).fold(
                onSuccess = {
                    rebuildFromServings(servingsDataManager.servingsFlow.value)
                },
                onFailure = { error ->
                    val message = error.message ?: "Failed to load scores"
                    _uiState.value = ScoresOverTimeUiState.Error(message)
                    _errors.emit(message)
                }
            )
        }
    }

    private fun observeServingsChanges() {
        viewModelScope.launch {
            servingsDataManager.servingsFlow.collect { servingsMap ->
                if (_uiState.value !is ScoresOverTimeUiState.Loading) {
                    rebuildFromServings(servingsMap)
                }
            }
        }
    }

    private fun rebuildFromServings(servingsMap: Map<LocalDate, DailyServings>) {
        val weeks = weeklyScoreBuilder.buildWeeks(servingsMap)
        _uiState.value = if (weeks != null) {
            ScoresOverTimeUiState.Success(weeks)
        } else {
            ScoresOverTimeUiState.Empty
        }
    }
}

sealed class ScoresOverTimeUiState {
    data object Loading : ScoresOverTimeUiState()
    data class Success(val weeks: List<WeekScoreData>) : ScoresOverTimeUiState()
    data object Empty : ScoresOverTimeUiState()
    data class Error(val message: String) : ScoresOverTimeUiState()
}
