package co.theportman.way_of_the_goat.data.scoring.model

import kotlinx.datetime.LocalDate

/**
 * A single day's aggregated activity data for display in a week row.
 *
 * @param date The calendar date.
 * @param dayName Full English day name (e.g. "Monday").
 * @param distance Total distance in km (converted from metres at the builder layer).
 * @param activityCount Number of individual activities recorded that day (drives dot display, capped at 3 visually).
 */
data class DayActivity(
    val date: LocalDate,
    val dayName: String,
    val distance: Double,
    val activityCount: Int
)
