package co.theportman.way_of_the_goat.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import co.theportman.way_of_the_goat.data.scoring.model.DayActivity
import co.theportman.way_of_the_goat.data.scoring.model.WeekActivityData
import co.theportman.way_of_the_goat.ui.theme.GoatSizing
import co.theportman.way_of_the_goat.ui.theme.GoatSpacing
import co.theportman.way_of_the_goat.ui.theme.WayOfTheGoatTheme
import co.theportman.way_of_the_goat.ui.theme.goatColors
import co.theportman.way_of_the_goat.util.formatDistance
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Displays a single week's activity distances as a row of tiles.
 *
 * Shows a header with date range and weekly total in km, followed by
 * 7 tiles (Mon-Sun) with distance text and activity count dots.
 */
@Composable
fun RunWeekRow(
    weekData: WeekActivityData,
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
                text = "${formatDistance(weekData.weeklyTotalKm)} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.goatColors.onSurface
            )
        }

        Spacer(modifier = Modifier.height(GoatSpacing.s4))

        // Tiles row: 7 distance tiles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(GoatSizing.Touch.default),
            horizontalArrangement = Arrangement.spacedBy(GoatSpacing.s4)
        ) {
            weekData.dailyActivities.forEachIndexed { index, dayActivity ->
                if (dayActivity != null) {
                    DistanceTile(
                        dayActivity = dayActivity,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BlankTile(
                        contentDescription = "${dayNameForIndex(index)}: no activity",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * A tile showing the day's total distance and activity count dots.
 */
@Composable
private fun DistanceTile(
    dayActivity: DayActivity,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(GoatSizing.Touch.default)
            .semantics {
                contentDescription = "${dayActivity.dayName}: ${formatDistance(dayActivity.distance)} km, ${dayActivity.activityCount} ${if (dayActivity.activityCount == 1) "activity" else "activities"}"
            },
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.onSurface
    ) {
        Box {
            // Distance text — centered
            Text(
                text = formatDistance(dayActivity.distance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.goatColors.surface,
                modifier = Modifier.align(Alignment.Center)
            )

            // Activity count dots — bottom-right
            if (dayActivity.activityCount > 0) {
                ActivityDots(
                    count = dayActivity.activityCount.coerceAtMost(3),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(GoatSpacing.s4)
                )
            }
        }
    }
}

/**
 * Row of 1-3 small circles indicating the number of activities.
 */
@Composable
private fun ActivityDots(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GoatSpacing.s2)
    ) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .size(GoatSpacing.s4)
                    .clip(CircleShape)
                    .background(MaterialTheme.goatColors.surface)
            )
        }
    }
}


// region Previews

@Preview(name = "Dark")
@Composable
private fun RunWeekRowPreview() {
    WayOfTheGoatTheme {
        RunWeekRow(weekData = previewWeekData())
    }
}

@Preview(name = "Light")
@Composable
private fun RunWeekRowPreviewLight() {
    WayOfTheGoatTheme(darkTheme = false) {
        RunWeekRow(weekData = previewWeekData())
    }
}

@Preview(name = "All Blank")
@Composable
private fun RunWeekRowPreviewAllBlank() {
    WayOfTheGoatTheme {
        RunWeekRow(
            weekData = WeekActivityData(
                dateRangeLabel = "Jan 5-11",
                dailyActivities = listOf(null, null, null, null, null, null, null),
                weeklyTotalKm = 0.0
            )
        )
    }
}

private fun previewWeekData() = WeekActivityData(
    dateRangeLabel = "Feb 16-22",
    dailyActivities = listOf(
        DayActivity(LocalDate(2026, 2, 16), "Monday", 14.2, 1),
        DayActivity(LocalDate(2026, 2, 17), "Tuesday", 8.0, 2),
        DayActivity(LocalDate(2026, 2, 18), "Wednesday", 12.5, 1),
        null,
        DayActivity(LocalDate(2026, 2, 20), "Friday", 21.3, 3),
        null,
        DayActivity(LocalDate(2026, 2, 22), "Sunday", 30.6, 1)
    ),
    weeklyTotalKm = 86.6
)

// endregion
