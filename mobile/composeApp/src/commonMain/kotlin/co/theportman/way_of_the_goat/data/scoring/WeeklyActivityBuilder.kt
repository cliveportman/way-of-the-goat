package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.remote.models.Activity
import co.theportman.way_of_the_goat.data.scoring.model.DayActivity
import co.theportman.way_of_the_goat.data.scoring.model.WeekActivityData
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * Builds weekly activity display data from raw [Activity] objects.
 *
 * Groups activities by week (Mon-Sun), aggregates daily distances and
 * activity counts, and sorts most recent first. Stateless — all inputs
 * are parameters.
 *
 * Reuses [WeeklyScoreBuilder] companion functions for date utilities.
 */
class WeeklyActivityBuilder(
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {

    /**
     * Groups activities by week (Mon-Sun) and builds display data.
     * Returns null if activities list is empty.
     */
    fun buildWeeks(activities: List<Activity>): List<WeekActivityData>? {
        if (activities.isEmpty()) return null

        val today = clock.todayIn(timeZone)

        // Find the earliest activity date
        val earliestDate = activities.mapNotNull { activityDate(it) }.minOrNull() ?: return null

        val currentWeekMonday = WeeklyScoreBuilder.getMonday(today)
        val earliestWeekMonday = WeeklyScoreBuilder.getMonday(earliestDate)

        val weeks = mutableListOf<WeekActivityData>()

        var weekMonday = currentWeekMonday
        while (weekMonday >= earliestWeekMonday) {
            weeks.add(buildWeekData(weekMonday, activities))
            weekMonday = weekMonday.minus(7, DateTimeUnit.DAY)
        }

        return weeks.ifEmpty { null }
    }

    /**
     * Builds a single week's display data from Monday to Sunday.
     */
    internal fun buildWeekData(
        weekMonday: LocalDate,
        activities: List<Activity>
    ): WeekActivityData {
        val dailyActivities = (0..6).map { dayOffset ->
            val date = weekMonday.plus(dayOffset, DateTimeUnit.DAY)
            val dateString = date.toString()

            val dayActivities = activities.filter { activity ->
                activity.startDateLocal.isNotEmpty() &&
                    activity.startDateLocal.substringBefore('T') == dateString
            }

            if (dayActivities.isNotEmpty()) {
                val totalDistanceKm = dayActivities.sumOf { (it.distance ?: 0.0) } / 1000.0
                DayActivity(
                    date = date,
                    dayName = WeeklyScoreBuilder.dayNameForDayOfWeek(date.dayOfWeek),
                    distance = totalDistanceKm,
                    activityCount = dayActivities.size
                )
            } else {
                null
            }
        }

        val weeklyTotalKm = dailyActivities.filterNotNull().sumOf { it.distance }
        val dateRangeLabel = WeeklyScoreBuilder.formatDateRange(weekMonday)

        return WeekActivityData(
            dateRangeLabel = dateRangeLabel,
            dailyActivities = dailyActivities,
            weeklyTotalKm = weeklyTotalKm
        )
    }

    /**
     * Extracts the [LocalDate] from an activity's startDateLocal string.
     * Returns null if the string is empty or cannot be parsed.
     */
    private fun activityDate(activity: Activity): LocalDate? {
        if (activity.startDateLocal.isEmpty()) return null
        return try {
            LocalDate.parse(activity.startDateLocal.substringBefore('T'))
        } catch (_: Exception) {
            null
        }
    }
}
