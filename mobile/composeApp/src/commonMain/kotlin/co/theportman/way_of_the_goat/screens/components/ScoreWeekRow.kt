package co.theportman.way_of_the_goat.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import co.theportman.way_of_the_goat.ui.theme.GoatSizing
import co.theportman.way_of_the_goat.ui.theme.GoatSpacing
import co.theportman.way_of_the_goat.ui.theme.WayOfTheGoatTheme
import co.theportman.way_of_the_goat.ui.theme.goatColors
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

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

/**
 * Displays a single week's scores as a row of coloured tiles.
 *
 * Shows a header with date range and weekly total, followed by
 * 7 tiles (Mon-Sun) coloured by score tier.
 */
@Composable
fun ScoreWeekRow(
    weekData: WeekScoreData,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Week of ${weekData.dateRangeLabel}" }
    ) {
        // Header row: date range (left) and weekly total (right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(GoatSpacing.s20),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = weekData.dateRangeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.goatColors.onSurface
            )
            Text(
                text = weekData.weeklyTotal.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.goatColors.onSurface
            )
        }

        Spacer(modifier = Modifier.height(GoatSpacing.s4))

        // Tiles row: 7 score tiles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(GoatSizing.Touch.default),
            horizontalArrangement = Arrangement.spacedBy(GoatSpacing.s4)
        ) {
            weekData.dailyScores.forEachIndexed { index, dayScore ->
                val dayName = dayScore?.dayName ?: dayNameForIndex(index)
                val description = if (dayScore != null) {
                    "$dayName: score ${dayScore.score}"
                } else {
                    "$dayName: no score"
                }

                ScoreTile(
                    dayScore = dayScore,
                    contentDescription = description,
                    onClick = if (dayScore != null) {
                        { onDateClick(dayScore.date) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * A single score tile within a week row.
 */
@Composable
private fun ScoreTile(
    dayScore: DayScore?,
    contentDescription: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (dayScore != null) {
        scoreColor(dayScore.score)
    } else {
        MaterialTheme.goatColors.surfaceContainerHigh
    }

    val tileModifier = modifier
        .height(GoatSizing.Touch.default)
        .semantics { this.contentDescription = contentDescription }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = tileModifier,
            shape = RectangleShape,
            color = backgroundColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = dayScore!!.score.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.goatColors.surface
                )
            }
        }
    } else {
        Surface(
            modifier = tileModifier,
            shape = RectangleShape,
            color = backgroundColor
        ) {}
    }
}

/**
 * Returns the tile background colour for a given score value.
 *
 * Uses 4 of the 6 score tokens — scorePlus1 and scoreMinus2 are
 * intentionally omitted per the design spec's score tier thresholds.
 */
@Composable
private fun scoreColor(score: Int): Color {
    return when {
        score <= 0 -> MaterialTheme.goatColors.scoreMinus3
        score in 1..10 -> MaterialTheme.goatColors.scoreMinus1
        score in 11..20 -> MaterialTheme.goatColors.score0
        else -> MaterialTheme.goatColors.scorePlus2
    }
}

/**
 * Fallback day name for tiles with no score data.
 */
private fun dayNameForIndex(index: Int): String {
    return when (index) {
        0 -> "Monday"
        1 -> "Tuesday"
        2 -> "Wednesday"
        3 -> "Thursday"
        4 -> "Friday"
        5 -> "Saturday"
        6 -> "Sunday"
        else -> "Day"
    }
}

@Preview(name = "Dark")
@Composable
private fun ScoreWeekRowPreview() {
    WayOfTheGoatTheme {
        ScoreWeekRow(
            weekData = previewWeekData(),
            onDateClick = {}
        )
    }
}

@Preview(name = "Light")
@Composable
private fun ScoreWeekRowPreviewLight() {
    WayOfTheGoatTheme(darkTheme = false) {
        ScoreWeekRow(
            weekData = previewWeekData(),
            onDateClick = {}
        )
    }
}

@Preview(name = "All Blank")
@Composable
private fun ScoreWeekRowPreviewAllBlank() {
    WayOfTheGoatTheme {
        ScoreWeekRow(
            weekData = WeekScoreData(
                dateRangeLabel = "Jan 5-11",
                dailyScores = listOf(null, null, null, null, null, null, null),
                weeklyTotal = 0
            ),
            onDateClick = {}
        )
    }
}

private fun previewWeekData() = WeekScoreData(
    dateRangeLabel = "Feb 16-22",
    dailyScores = listOf(
        DayScore(LocalDate(2026, 2, 16), "Monday", 22),
        DayScore(LocalDate(2026, 2, 17), "Tuesday", 15),
        DayScore(LocalDate(2026, 2, 18), "Wednesday", 8),
        DayScore(LocalDate(2026, 2, 19), "Thursday", -4),
        DayScore(LocalDate(2026, 2, 20), "Friday", 18),
        null,
        null
    ),
    weeklyTotal = 59
)
