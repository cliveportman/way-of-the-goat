package co.theportman.way_of_the_goat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.theportman.way_of_the_goat.data.cache.ActivityDataManager
import co.theportman.way_of_the_goat.data.remote.models.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * ViewModel for the Today (Scores) screen
 * Manages view mode toggle and data for a specific date
 * Supports bidirectional lazy loading
 */
class ScoresViewModel : ViewModel() {

    private val dataManager = ActivityDataManager

    private val _uiState = MutableStateFlow<ScoresUiState>(ScoresUiState.Loading)
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    private val _viewMode = MutableStateFlow(TodayViewMode.NUTRITION)
    val viewMode: StateFlow<TodayViewMode> = _viewMode.asStateFlow()

    // Track last viewed date for scroll direction detection
    private var lastViewedDate: LocalDate? = null

    init {
        loadInitialData()
    }

    fun toggleViewMode() {
        _viewMode.value = when (_viewMode.value) {
            TodayViewMode.NUTRITION -> TodayViewMode.ACTIVITIES
            TodayViewMode.ACTIVITIES -> TodayViewMode.NUTRITION
        }
    }

    /**
     * Load initial data (today ± 30 days or around a specific target date)
     */
    fun loadInitialData(targetDate: LocalDate? = null) {
        viewModelScope.launch {
            _uiState.value = ScoresUiState.Loading

            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val centerDate = targetDate ?: today

                // Load ±30 days around the center date
                dataManager.loadInitialData(aroundDate = centerDate, bufferDays = 30).fold(
                    onSuccess = {
                        _uiState.value = ScoresUiState.Success(emptyList())
                    },
                    onFailure = { error ->
                        _uiState.value = ScoresUiState.Error(error.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ScoresUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Ensure data is loaded around the given date
     * Detects scroll direction and preloads accordingly
     */
    fun ensureDateLoaded(currentDate: LocalDate) {
        viewModelScope.launch {
            val previousDate = lastViewedDate

            if (previousDate != null) {
                // Detect scroll direction
                if (currentDate < previousDate) {
                    // Scrolling backwards (left) - into the past
                    // Preload 30 days before current date
                    dataManager.ensureDateLoaded(currentDate, bufferDays = 30)
                } else if (currentDate > previousDate) {
                    // Scrolling forwards (right) - towards today
                    // Preload 30 days after current date
                    dataManager.ensureDateLoaded(currentDate, bufferDays = 30)
                }
            }

            lastViewedDate = currentDate
        }
    }

    /**
     * Check if a specific date is loaded
     */
    fun isDateLoaded(date: LocalDate): Boolean {
        return dataManager.isDateLoaded(date)
    }

    /**
     * Get activities for a specific date
     */
    fun getActivitiesForDate(date: LocalDate): List<Activity> {
        return dataManager.getActivitiesForDate(date)
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
        // ActivityDataManager is a singleton, don't close it here
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
