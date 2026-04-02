package co.theportman.way_of_the_goat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.theportman.way_of_the_goat.data.cache.ServingsDataManager
import co.theportman.way_of_the_goat.data.scoring.ScoreCalculator
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.screens.components.DayScore
import co.theportman.way_of_the_goat.screens.components.WeekScoreData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * ViewModel for the Scores Over Time screen.
 *
 * Groups daily scores by week (Mon-Sun), calculates weekly totals,
 * and sorts most recent first.
 */
class ScoresOverTimeViewModel : ViewModel() {

    private val servingsDataManager = ServingsDataManager.instance

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
                // Not initialized yet — show success with empty data, will refresh when data arrives
                _uiState.value = ScoresOverTimeUiState.Empty
                return@launch
            }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            // Load a wide range of data (26 weeks / ~6 months)
            servingsDataManager.loadInitialData(aroundDate = today, bufferDays = 91).fold(
                onSuccess = {
                    buildWeeklyData(servingsDataManager.servingsFlow.value)
                },
                onFailure = { error ->
                    _uiState.value = ScoresOverTimeUiState.Error(error.message ?: "Failed to load scores")
                    _errors.emit(error.message ?: "Failed to load scores")
                }
            )
        }
    }

    /**
     * Observe servings changes to rebuild weekly data reactively.
     */
    private fun observeServingsChanges() {
        viewModelScope.launch {
            servingsDataManager.servingsFlow.collect { servingsMap ->
                // Only rebuild if we're past the initial loading state
                if (_uiState.value !is ScoresOverTimeUiState.Loading) {
                    buildWeeklyData(servingsMap)
                }
            }
        }
    }

    /**
     * Groups servings by week (Mon-Sun) and builds display data.
     */
    private fun buildWeeklyData(servingsMap: Map<LocalDate, DailyServings>) {
        if (servingsMap.isEmpty()) {
            _uiState.value = ScoresOverTimeUiState.Empty
            return
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Find the range of dates with data
        val dates = servingsMap.keys
        val earliestDate = dates.min()

        // Start from the Monday of the current week, go back to the Monday of the earliest date's week
        val currentWeekMonday = getMonday(today)
        val earliestWeekMonday = getMonday(earliestDate)

        val weeks = mutableListOf<WeekScoreData>()

        var weekMonday = currentWeekMonday
        while (weekMonday >= earliestWeekMonday) {
            val weekData = buildWeekData(weekMonday, servingsMap)
            weeks.add(weekData)
            weekMonday = weekMonday.minus(7, DateTimeUnit.DAY)
        }

        if (weeks.isEmpty()) {
            _uiState.value = ScoresOverTimeUiState.Empty
        } else {
            _uiState.value = ScoresOverTimeUiState.Success(weeks)
        }
    }

    /**
     * Builds a single week's display data from Monday to Sunday.
     */
    private fun buildWeekData(
        weekMonday: LocalDate,
        servingsMap: Map<LocalDate, DailyServings>
    ): WeekScoreData {
        val dailyScores = (0..6).map { dayOffset ->
            val date = weekMonday.plus(dayOffset, DateTimeUnit.DAY)
            val servings = servingsMap[date]

            if (servings != null && servings.hasAnyServings) {
                val suite = SuiteDefinitions.getSuiteById(servings.suiteId)
                val score = if (suite != null) {
                    ScoreCalculator.calculateDailyScore(servings, suite)
                } else {
                    ScoreCalculator.calculateDailyScore(servings) ?: 0
                }
                DayScore(
                    date = date,
                    dayName = dayNameForDayOfWeek(date.dayOfWeek),
                    score = score
                )
            } else {
                null
            }
        }

        val weeklyTotal = dailyScores.filterNotNull().sumOf { it.score }
        val dateRangeLabel = formatDateRange(weekMonday)

        return WeekScoreData(
            dateRangeLabel = dateRangeLabel,
            dailyScores = dailyScores,
            weeklyTotal = weeklyTotal
        )
    }

    /**
     * Returns the Monday of the week containing the given date.
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

    /**
     * Formats a date range label like "Feb 16-22" or "Dec 28-Jan 3".
     */
    private fun formatDateRange(weekMonday: LocalDate): String {
        val weekSunday = weekMonday.plus(6, DateTimeUnit.DAY)
        val mondayMonth = shortMonthName(weekMonday.monthNumber)
        val sundayMonth = shortMonthName(weekSunday.monthNumber)

        return if (weekMonday.monthNumber == weekSunday.monthNumber) {
            "$mondayMonth ${weekMonday.dayOfMonth}-${weekSunday.dayOfMonth}"
        } else {
            "$mondayMonth ${weekMonday.dayOfMonth}-$sundayMonth ${weekSunday.dayOfMonth}"
        }
    }

    private fun shortMonthName(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> ""
        }
    }

    private fun dayNameForDayOfWeek(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
            else -> "Day"
        }
    }
}

sealed class ScoresOverTimeUiState {
    data object Loading : ScoresOverTimeUiState()
    data class Success(val weeks: List<WeekScoreData>) : ScoresOverTimeUiState()
    data object Empty : ScoresOverTimeUiState()
    data class Error(val message: String) : ScoresOverTimeUiState()
}
