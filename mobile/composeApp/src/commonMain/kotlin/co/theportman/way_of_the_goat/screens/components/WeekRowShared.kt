package co.theportman.way_of_the_goat.screens.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import co.theportman.way_of_the_goat.ui.theme.GoatSizing
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
