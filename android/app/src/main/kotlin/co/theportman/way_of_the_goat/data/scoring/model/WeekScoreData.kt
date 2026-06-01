package co.theportman.way_of_the_goat.data.scoring.model

import kotlinx.datetime.LocalDate

/**
 * A single day's score for display in a week row.
 */
data class DayScore(
    val date: LocalDate,
    val dayName: String,
    val score: Int
)

/**
 * Data for a single week row in the Scores Over Time screen.
 */
data class WeekScoreData(
    val dateRangeLabel: String,
    val dailyScores: List<DayScore?>,
    val weeklyTotal: Int
)
