package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.DayScore
import co.theportman.way_of_the_goat.data.scoring.model.WeekScoreData
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * Builds weekly score display data from raw servings.
 *
 * Groups daily scores by week (Mon-Sun), calculates weekly totals,
 * and sorts most recent first. Stateless — all inputs are parameters.
 */
class WeeklyScoreBuilder(
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {

    /**
     * Groups servings by week (Mon-Sun) and builds display data.
     * Returns null if servingsMap is empty.
     */
    fun buildWeeks(servingsMap: Map<LocalDate, DailyServings>): List<WeekScoreData>? {
        if (servingsMap.isEmpty()) return null

        val today = clock.todayIn(timeZone)
        val earliestDate = servingsMap.keys.min()

        val currentWeekMonday = getMonday(today)
        val earliestWeekMonday = getMonday(earliestDate)

        val weeks = mutableListOf<WeekScoreData>()

        var weekMonday = currentWeekMonday
        while (weekMonday >= earliestWeekMonday) {
            weeks.add(buildWeekData(weekMonday, servingsMap))
            weekMonday = weekMonday.minus(7, DateTimeUnit.DAY)
        }

        return weeks.ifEmpty { null }
    }

    /**
     * Builds a single week's display data from Monday to Sunday.
     */
    internal fun buildWeekData(
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

    companion object {
        /**
         * Returns the Monday of the week containing the given date.
         */
        fun getMonday(date: LocalDate): LocalDate {
            val daysFromMonday = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> 0
                DayOfWeek.TUESDAY -> 1
                DayOfWeek.WEDNESDAY -> 2
                DayOfWeek.THURSDAY -> 3
                DayOfWeek.FRIDAY -> 4
                DayOfWeek.SATURDAY -> 5
                DayOfWeek.SUNDAY -> 6
                // Required: DayOfWeek is an expect enum in KMP; compiler requires else branch
                else -> 0
            }
            return date.minus(daysFromMonday, DateTimeUnit.DAY)
        }

        /**
         * Formats a date range label like "Feb 16-22" or "Dec 28-Jan 3".
         */
        fun formatDateRange(weekMonday: LocalDate): String {
            val weekSunday = weekMonday.plus(6, DateTimeUnit.DAY)
            val mondayMonth = shortMonthName(weekMonday.monthNumber)
            val sundayMonth = shortMonthName(weekSunday.monthNumber)

            return if (weekMonday.monthNumber == weekSunday.monthNumber) {
                "$mondayMonth ${weekMonday.dayOfMonth}-${weekSunday.dayOfMonth}"
            } else {
                "$mondayMonth ${weekMonday.dayOfMonth}-$sundayMonth ${weekSunday.dayOfMonth}"
            }
        }

        fun shortMonthName(monthNumber: Int): String {
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

        fun dayNameForDayOfWeek(dayOfWeek: DayOfWeek): String {
            return when (dayOfWeek) {
                DayOfWeek.MONDAY -> "Monday"
                DayOfWeek.TUESDAY -> "Tuesday"
                DayOfWeek.WEDNESDAY -> "Wednesday"
                DayOfWeek.THURSDAY -> "Thursday"
                DayOfWeek.FRIDAY -> "Friday"
                DayOfWeek.SATURDAY -> "Saturday"
                DayOfWeek.SUNDAY -> "Sunday"
                // Required: DayOfWeek is an expect enum in KMP; compiler requires else branch
                else -> "Day"
            }
        }
    }
}
