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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

/**
 * ViewModel for the Progress screen
 * Fetches activity data from intervals.icu
 */
class ProgressViewModel : ViewModel() {

    private val repository = IntervalsRepository()

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.ACTIVITIES)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    init {
        loadRecentActivities()
    }

    fun cycleViewMode() {
        _viewMode.value = when (_viewMode.value) {
            ViewMode.ACTIVITIES -> ViewMode.NUTRITION
            ViewMode.NUTRITION -> ViewMode.COMBINED
            ViewMode.COMBINED -> ViewMode.ACTIVITIES
        }
    }

    fun loadRecentActivities() {
        viewModelScope.launch {
            _uiState.value = ProgressUiState.Loading

            repository.getRecentActivities().fold(
                onSuccess = { activities ->
                    _uiState.value = ProgressUiState.Success(activities)
                },
                onFailure = { error ->
                    _uiState.value = ProgressUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    fun getActivitiesForDate(date: LocalDate): List<Activity> {
        val currentState = _uiState.value
        if (currentState !is ProgressUiState.Success) return emptyList()

        return currentState.activities.filter { activity ->
            // Parse the ISO date from the activity and match it with the given date
            activity.startDateLocal.isNotEmpty() &&
            activity.startDateLocal.substringBefore('T') == date.toString()
        }
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
        val currentState = _uiState.value
        if (currentState !is ProgressUiState.Success) return WeekSummary(0, 0.0)

        val weekActivities = currentState.activities.filter { activity ->
            if (activity.startDateLocal.isEmpty()) return@filter false

            val activityDate = activity.startDateLocal.substringBefore('T')
            activityDate >= weekMonday.toString() && activityDate <= weekSunday.toString()
        }

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
        repository.close()
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
