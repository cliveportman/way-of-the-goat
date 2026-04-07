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
import co.theportman.way_of_the_goat.data.scoring.model.DayScore
import co.theportman.way_of_the_goat.data.scoring.model.WeekScoreData
import co.theportman.way_of_the_goat.ui.theme.GoatColorScheme
import co.theportman.way_of_the_goat.ui.theme.GoatSizing
import co.theportman.way_of_the_goat.ui.theme.GoatSpacing
import co.theportman.way_of_the_goat.ui.theme.WayOfTheGoatTheme
import co.theportman.way_of_the_goat.ui.theme.goatColors
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

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
                if (dayScore != null) {
                    ScoreTile(
                        dayScore = dayScore,
                        contentDescription = "${dayScore.dayName}: score ${dayScore.score}",
                        onClick = { onDateClick(dayScore.date) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BlankTile(
                        contentDescription = "${dayNameForIndex(index)}: no score",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * A scored tile that displays the day's score and handles tap.
 */
@Composable
private fun ScoreTile(
    dayScore: DayScore,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(GoatSizing.Touch.default)
            .semantics { this.contentDescription = contentDescription },
        shape = RectangleShape,
        color = scoreColor(dayScore.score, MaterialTheme.goatColors)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = dayScore.score.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.goatColors.surface
            )
        }
    }
}

/**
 * Returns the tile background colour for a given score value.
 *
 * Uses 4 of the 6 score tokens — scorePlus1 and scoreMinus2 are
 * intentionally omitted per the design spec's score tier thresholds.
 */
private fun scoreColor(score: Int, colors: GoatColorScheme): Color {
    return when {
        score <= 0 -> colors.scoreMinus3
        score in 1..10 -> colors.scoreMinus1
        score in 11..20 -> colors.score0
        else -> colors.scorePlus2
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
