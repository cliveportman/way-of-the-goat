package co.theportman.way_of_the_goat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.theportman.way_of_the_goat.data.remote.models.Activity
import co.theportman.way_of_the_goat.data.repository.IntervalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DayOfWeek

/**
 * ViewModel for the Today (Scores) screen
 * Manages view mode toggle and data for a specific date
 */
class ScoresViewModel : ViewModel() {

    private val repository = IntervalsRepository()

    private val _uiState = MutableStateFlow<ScoresUiState>(ScoresUiState.Loading)
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    private val _viewMode = MutableStateFlow(TodayViewMode.NUTRITION)
    val viewMode: StateFlow<TodayViewMode> = _viewMode.asStateFlow()

    init {
        loadRecentActivities()
    }

    fun toggleViewMode() {
        _viewMode.value = when (_viewMode.value) {
            TodayViewMode.NUTRITION -> TodayViewMode.ACTIVITIES
            TodayViewMode.ACTIVITIES -> TodayViewMode.NUTRITION
        }
    }

    fun loadRecentActivities() {
        viewModelScope.launch {
            _uiState.value = ScoresUiState.Loading

            repository.getRecentActivities().fold(
                onSuccess = { activities ->
                    _uiState.value = ScoresUiState.Success(activities)
                },
                onFailure = { error ->
                    _uiState.value = ScoresUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    /**
     * Get activities for a specific date
     */
    fun getActivitiesForDate(date: LocalDate): List<Activity> {
        val currentState = _uiState.value
        if (currentState !is ScoresUiState.Success) return emptyList()

        return currentState.activities.filter { activity ->
            activity.startDateLocal.isNotEmpty() &&
            activity.startDateLocal.substringBefore('T') == date.toString()
        }
    }

    /**
     * Get nutrition summary for a specific date (mock data for now)
     */
    fun getNutritionScore(date: LocalDate): Int {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> 16
            DayOfWeek.TUESDAY -> 4
            DayOfWeek.WEDNESDAY -> 12
            DayOfWeek.THURSDAY -> 8
            DayOfWeek.FRIDAY -> 20
            DayOfWeek.SATURDAY -> -2
            DayOfWeek.SUNDAY -> 15
            else -> 0 // Required: DayOfWeek is an expect enum in KMP, compiler requires else branch
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}

sealed class ScoresUiState {
    data object Loading : ScoresUiState()
    data class Success(val activities: List<Activity>) : ScoresUiState()
    data class Error(val message: String) : ScoresUiState()
}

enum class TodayViewMode {
    ACTIVITIES,
    NUTRITION
}
