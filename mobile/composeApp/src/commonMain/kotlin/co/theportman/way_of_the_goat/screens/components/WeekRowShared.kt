package co.theportman.way_of_the_goat.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import co.theportman.way_of_the_goat.ui.theme.GoatSizing
import co.theportman.way_of_the_goat.ui.theme.GoatSpacing
import co.theportman.way_of_the_goat.ui.theme.goatColors

/**
 * An empty tile for days with no data.
 *
 * Shared between [RunWeekRow] and [ScoreWeekRow].
 */
@Composable
internal fun BlankTile(
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(GoatSizing.Touch.default)
            .semantics { this.contentDescription = contentDescription },
        shape = RectangleShape,
        color = MaterialTheme.goatColors.surfaceContainerHigh
    ) {}
}

/**
 * Single-letter day-of-week labels for header rows (Monday-first).
 */
internal val dayOfWeekLabels = listOf("M", "T", "W", "T", "F", "S", "S")

/**
 * Row of day-of-week header labels (M T W T F S S) aligned with the tile columns.
 * Uses the same weight(1f) + s4 gap distribution as the score/distance tiles.
 */
@Composable
internal fun DayOfWeekHeaders(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GoatSpacing.s4)
    ) {
        dayOfWeekLabels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.goatColors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Fallback day name for tiles with no data, by Monday-first index.
 */
internal fun dayNameForIndex(index: Int): String {
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
