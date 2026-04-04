package co.theportman.way_of_the_goat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.theportman.way_of_the_goat.data.cache.ActivityDataManager
import co.theportman.way_of_the_goat.data.cache.ActivityDataSource
import co.theportman.way_of_the_goat.data.remote.models.Activity
import co.theportman.way_of_the_goat.data.scoring.WeeklyActivityBuilder
import co.theportman.way_of_the_goat.data.scoring.model.WeekActivityData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for the Runs Over Time screen.
 *
 * Delegates week grouping and distance aggregation to [WeeklyActivityBuilder].
 */
class RunsOverTimeViewModel(
    private val activityDataSource: ActivityDataSource = ActivityDataManager,
    private val weeklyActivityBuilder: WeeklyActivityBuilder = WeeklyActivityBuilder(),
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RunsOverTimeUiState>(RunsOverTimeUiState.Loading)
    val uiState: StateFlow<RunsOverTimeUiState> = _uiState.asStateFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    init {
        loadDataAndObserve()
    }

    private fun loadDataAndObserve() {
        viewModelScope.launch {
            _uiState.value = RunsOverTimeUiState.Loading

            val today = clock.todayIn(timeZone)

            activityDataSource.loadInitialData(aroundDate = today, bufferDays = 91).fold(
                onSuccess = {
                    // Collect activitiesFlow as the single source of truth.
                    // The first emission rebuilds from the data just loaded;
                    // subsequent emissions rebuild when activities change.
                    activityDataSource.activitiesFlow.collect { activities ->
                        rebuildFromActivities(activities)
                    }
                },
                onFailure = { error ->
                    val message = error.message ?: "Failed to load activities"
                    _uiState.value = RunsOverTimeUiState.Error(message)
                    _errors.emit(message)
                }
            )
        }
    }

    private fun rebuildFromActivities(activities: List<Activity>) {
        val weeks = weeklyActivityBuilder.buildWeeks(activities)
        _uiState.value = if (weeks != null) {
            RunsOverTimeUiState.Success(weeks)
        } else {
            RunsOverTimeUiState.Empty
        }
    }
}

sealed class RunsOverTimeUiState {
    data object Loading : RunsOverTimeUiState()
    data class Success(val weeks: List<WeekActivityData>) : RunsOverTimeUiState()
    data object Empty : RunsOverTimeUiState()
    data class Error(val message: String) : RunsOverTimeUiState()
}
