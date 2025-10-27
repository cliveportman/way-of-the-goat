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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.plus
import kotlinx.datetime.minus

/**
 * ViewModel for the Progress screen
 * Fetches activity data from intervals.icu
 */
class ProgressViewModel : ViewModel() {

    private val dataManager = ActivityDataManager

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.ACTIVITIES)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // Track loaded weeks (using Monday of the week as key)
    private val loadedWeeks = mutableSetOf<LocalDate>()
    private val loadingWeeks = mutableSetOf<LocalDate>()

    init {
        loadInitialWeeks()
    }

    fun cycleViewMode() {
        _viewMode.value = when (_viewMode.value) {
            ViewMode.ACTIVITIES -> ViewMode.NUTRITION
            ViewMode.NUTRITION -> ViewMode.COMBINED
            ViewMode.COMBINED -> ViewMode.ACTIVITIES
        }
    }

    /**
     * Load initial 5 weeks of data (current week + 4 weeks back)
     */
    private fun loadInitialWeeks() {
        viewModelScope.launch {
            _uiState.value = ProgressUiState.Loading

            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val currentWeekMonday = getMonday(today)

                // Load initial data through ActivityDataManager
                // Load ~35 days (5 weeks worth)
                val oldest = currentWeekMonday.minus(4 * 7, DateTimeUnit.DAY)
                val newest = currentWeekMonday.plus(6, DateTimeUnit.DAY) // Include current week's Sunday

                dataManager.loadInitialData(aroundDate = today, bufferDays = 28).fold(
                    onSuccess = {
                        // Mark weeks as loaded
                        for (weeksBack in 0..4) {
                            val weekMonday = currentWeekMonday.minus(weeksBack * 7, DateTimeUnit.DAY)
                            loadedWeeks.add(weekMonday)
                        }
                        _uiState.value = ProgressUiState.Success(emptyList()) // Success with empty, data comes from dataManager
                    },
                    onFailure = { error ->
                        _uiState.value = ProgressUiState.Error(error.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProgressUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Check if a week is already loaded
     */
    fun isWeekLoaded(weekMonday: LocalDate): Boolean {
        return loadedWeeks.contains(weekMonday)
    }

    /**
     * Ensure data is loaded for the specified week and 4 weeks before it
     */
    fun ensureWeekLoaded(currentWeekMonday: LocalDate) {
        viewModelScope.launch {
            // Load the week that's 4 weeks before the current visible week
            val targetWeekMonday = currentWeekMonday.minus(4 * 7, DateTimeUnit.DAY)

            if (!isWeekLoaded(targetWeekMonday) && !loadingWeeks.contains(targetWeekMonday)) {
                loadWeekData(targetWeekMonday)
            }
        }
    }

    /**
     * Load activities for a specific week (Monday to Sunday)
     */
    private suspend fun loadWeekData(weekMonday: LocalDate) {
        if (loadedWeeks.contains(weekMonday) || loadingWeeks.contains(weekMonday)) {
            return // Already loaded or loading
        }

        loadingWeeks.add(weekMonday)

        try {
            val weekSunday = weekMonday.plus(6, DateTimeUnit.DAY)

            // Use ActivityDataManager to check and load if needed
            val didLoad = dataManager.ensureDateLoaded(weekMonday, bufferDays = 3)

            loadedWeeks.add(weekMonday)
            loadingWeeks.remove(weekMonday)

            // UI state doesn't need updating as data comes from dataManager
        } catch (e: Exception) {
            loadingWeeks.remove(weekMonday)
            println("Exception loading week $weekMonday: ${e.message}")
        }
    }

    /**
     * Get the Monday of the week for a given date
     */
    private fun getMonday(date: LocalDate): LocalDate {
        val daysFromMonday = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
            else -> 0
        }
        return date.minus(daysFromMonday, DateTimeUnit.DAY)
    }

    fun getActivitiesForDate(date: LocalDate): List<Activity> {
        return dataManager.getActivitiesForDate(date)
    }

    /**
     * Get activity summary for a specific date (count and total distance in km)
     */
    fun getDaySummary(date: LocalDate): DaySummary {
        val activities = getActivitiesForDate(date)
        val totalDistanceMeters = activities.sumOf { it.distance ?: 0.0 }
        val totalDistanceKm = totalDistanceMeters / 1000.0

        return DaySummary(
            activityCount = activities.size,
            totalDistanceKm = totalDistanceKm
        )
    }

    /**
     * Get week summary for the week containing the given dates
     */
    fun getWeekSummary(weekMonday: LocalDate, weekSunday: LocalDate): WeekSummary {
        val weekActivities = dataManager.getActivitiesForRange(weekMonday, weekSunday)

        val totalDistanceMeters = weekActivities.sumOf { it.distance ?: 0.0 }
        val totalDistanceKm = totalDistanceMeters / 1000.0

        return WeekSummary(
            activityCount = weekActivities.size,
            totalDistanceKm = totalDistanceKm
        )
    }

    /**
     * Get nutrition summary for a specific date (mock data for now)
     */
    fun getNutritionSummary(date: LocalDate): NutritionSummary {
        // Mock data based on day of week
        val score = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> 16
            DayOfWeek.TUESDAY -> 4
            DayOfWeek.WEDNESDAY -> 12
            DayOfWeek.THURSDAY -> 8
            DayOfWeek.FRIDAY -> 20
            DayOfWeek.SATURDAY -> -2
            DayOfWeek.SUNDAY -> 15
            else -> 0 // Required: DayOfWeek is an expect enum in KMP, compiler requires else branch
        }
        return NutritionSummary(score = score)
    }

    /**
     * Get weekly nutrition summary (mock data for now)
     */
    fun getWeekNutritionSummary(weekMonday: LocalDate, weekSunday: LocalDate): WeekNutritionSummary {
        var totalScore = 0
        for (dayOffset in 0..6) {
            val currentDay = weekMonday.plus(dayOffset, DateTimeUnit.DAY)
            totalScore += getNutritionSummary(currentDay).score
        }
        return WeekNutritionSummary(totalScore = totalScore)
    }

    override fun onCleared() {
        super.onCleared()
        // ActivityDataManager is a singleton, don't close it here
        // It will be closed when the app terminates
    }
}

sealed class ProgressUiState {
    data object Loading : ProgressUiState()
    data class Success(val activities: List<Activity>) : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}

enum class ViewMode {
    ACTIVITIES,
    NUTRITION,
    COMBINED
}

data class DaySummary(
    val activityCount: Int,
    val totalDistanceKm: Double
)

data class WeekSummary(
    val activityCount: Int,
    val totalDistanceKm: Double
)

data class NutritionSummary(
    val score: Int
)

data class WeekNutritionSummary(
    val totalScore: Int
)
