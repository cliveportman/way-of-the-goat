package co.theportman.way_of_the_goat.data.scoring.model

/**
 * Data for a single week row in the Runs Over Time screen.
 *
 * @param dateRangeLabel Formatted label like "Feb 16-22" or "Dec 28-Jan 3".
 * @param dailyActivities 7 entries (Mon–Sun), null for days with no activity.
 * @param weeklyTotalKm Sum of daily distances in km.
 */
data class WeekActivityData(
    val dateRangeLabel: String,
    val dailyActivities: List<DayActivity?>,
    val weeklyTotalKm: Double
)
