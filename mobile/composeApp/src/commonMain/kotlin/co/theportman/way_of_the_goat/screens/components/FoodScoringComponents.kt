package co.theportman.way_of_the_goat.screens.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.theportman.way_of_the_goat.data.scoring.DailyTotalsForDisplay
import co.theportman.way_of_the_goat.data.scoring.model.FoodCategory
import co.theportman.way_of_the_goat.ui.theme.GoatColors

// Figma colors
private val BackgroundColor = Color(0xFF020618)        // slate-950
private val TextColor = Color(0xFFF1F5F9)              // slate-100
private val CategoryLabelBg = Color(0xFF1D293D)        // slate-800

/**
 * Get the background colour for a score cell based on its point value.
 */
private fun getScoreColor(score: Int): Color = when (score) {
    2 -> GoatColors.ScorePlus2
    1 -> GoatColors.ScorePlus1
    0 -> GoatColors.ScoreZero
    -1 -> GoatColors.ScoreMinus1
    -2 -> GoatColors.ScoreMinus2
    else -> GoatColors.ScoreZero // fallback
}

/**
 * A row displaying a food category with its serving cells.
 *
 * @param category The food category to display
 * @param servingCount Current number of servings logged
 * @param onIncrement Called when user taps to add a serving
 * @param onDecrement Called when user long-presses to remove a serving
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoodCategoryRow(
    category: FoodCategory,
    servingCount: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category name label
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .background(CategoryLabelBg),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = category.name,
                color = TextColor,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Gap between category label and score cells
        Spacer(modifier = Modifier.width(4.dp))

        // Serving cells
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val scoringRule = category.scoringRule
            val maxServings = scoringRule.maxTrackedServings

            repeat(maxServings) { index ->
                val servingNumber = index + 1
                val isFilled = index < servingCount
                val pointValue = scoringRule.getScoreForServing(servingNumber)

                ServingCell(
                    pointValue = pointValue,
                    isFilled = isFilled,
                    onClick = {
                        if (servingCount <= index) {
                            // Tap on empty cell or the next cell to fill
                            onIncrement()
                        }
                    },
                    onLongClick = {
                        if (servingCount > 0) {
                            onDecrement()
                        }
                    }
                )
            }
        }
    }
}

/**
 * A single serving cell showing the point value.
 * - Filled (selected): full opacity background, dark navy text
 * - Empty (unselected): 0.5 opacity background, 0.1 opacity text showing potential score
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServingCell(
    pointValue: Int,
    isFilled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scoreColor = getScoreColor(pointValue)
    val backgroundAlpha = if (isFilled) 1f else 0.5f
    val textColor = if (isFilled) GoatColors.Navy950 else GoatColors.Navy950.copy(alpha = 0.1f)

    Box(
        modifier = modifier
            .width(36.dp)
            .height(38.dp)
            .background(scoreColor.copy(alpha = backgroundAlpha))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatPointValue(pointValue),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Score summary displayed at the bottom of the screen.
 */
@Composable
fun ScoreSummary(
    totals: DailyTotalsForDisplay,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Center: Total score and portions
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = totals.total,
                color = TextColor,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${totals.portions} portions",
                color = TextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Right: Healthy and unhealthy breakdown
        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = totals.healthy,
                color = TextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = totals.unhealthy?.let { if (it >= 0) "+$it" else "$it" } ?: "---",
                color = TextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Format a point value for display (e.g., +2, +1, 0, -1, -2)
 */
private fun formatPointValue(points: Int): String {
    return when {
        points > 0 -> "+$points"
        else -> "$points"
    }
}
