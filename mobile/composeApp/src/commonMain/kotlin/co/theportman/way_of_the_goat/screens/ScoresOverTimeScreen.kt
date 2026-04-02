package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.theportman.way_of_the_goat.screens.components.DayScore
import co.theportman.way_of_the_goat.screens.components.ScoreWeekRow
import co.theportman.way_of_the_goat.screens.components.WeekScoreData
import co.theportman.way_of_the_goat.ui.theme.GoatSpacing
import co.theportman.way_of_the_goat.ui.theme.WayOfTheGoatTheme
import co.theportman.way_of_the_goat.ui.theme.goatColors
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

private val dayOfWeekLabels = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun ScoresOverTimeScreen(
    onDateClick: (LocalDate) -> Unit = {},
    viewModel: ScoresOverTimeViewModel = viewModel { ScoresOverTimeViewModel() },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScoresOverTimeContent(
        uiState = uiState,
        onDateClick = onDateClick,
        modifier = modifier
    )
}

@Composable
private fun ScoresOverTimeContent(
    uiState: ScoresOverTimeUiState,
    onDateClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.goatColors.surface)
            .padding(horizontal = GoatSpacing.s24)
    ) {
        Spacer(modifier = Modifier.height(GoatSpacing.s32))

        // Screen title
        Text(
            text = "Your scores",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.goatColors.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(GoatSpacing.s16))

        // Day-of-week headers (fixed, not scrolled)
        DayOfWeekHeaders()

        Spacer(modifier = Modifier.height(GoatSpacing.s16))

        // Week rows content
        when (uiState) {
            is ScoresOverTimeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.goatColors.onSurface
                    )
                }
            }

            is ScoresOverTimeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GoatSpacing.s4)
                ) {
                    items(
                        items = uiState.weeks,
                        key = { it.dateRangeLabel }
                    ) { weekData ->
                        ScoreWeekRow(
                            weekData = weekData,
                            onDateClick = onDateClick
                        )
                    }
                }
            }

            is ScoresOverTimeUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scores yet. Start logging food to see your scores here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.goatColors.onSurfaceVariant
                    )
                }
            }

            is ScoresOverTimeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.goatColors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Row of day-of-week header labels (M T W T F S S) aligned with the tile columns.
 * Uses the same weight(1f) + s4 gap distribution as the score tiles.
 */
@Composable
private fun DayOfWeekHeaders(
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// region Previews

@Preview
@Composable
private fun ScoresOverTimeScreenPreview() {
    WayOfTheGoatTheme {
        ScoresOverTimeContent(
            uiState = ScoresOverTimeUiState.Success(
                weeks = sampleWeeks()
            )
        )
    }
}

@Preview(name = "Light")
@Composable
private fun ScoresOverTimeScreenPreviewLight() {
    WayOfTheGoatTheme(darkTheme = false) {
        ScoresOverTimeContent(
            uiState = ScoresOverTimeUiState.Success(
                weeks = sampleWeeks()
            )
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun ScoresOverTimeScreenPreviewLoading() {
    WayOfTheGoatTheme {
        ScoresOverTimeContent(
            uiState = ScoresOverTimeUiState.Loading
        )
    }
}

@Preview(name = "Empty")
@Composable
private fun ScoresOverTimeScreenPreviewEmpty() {
    WayOfTheGoatTheme {
        ScoresOverTimeContent(
            uiState = ScoresOverTimeUiState.Empty
        )
    }
}

private fun sampleWeeks(): List<WeekScoreData> = listOf(
    WeekScoreData(
        dateRangeLabel = "Mar 24-30",
        dailyScores = listOf(
            DayScore(LocalDate(2026, 3, 24), "Monday", 22),
            DayScore(LocalDate(2026, 3, 25), "Tuesday", 15),
            DayScore(LocalDate(2026, 3, 26), "Wednesday", 8),
            DayScore(LocalDate(2026, 3, 27), "Thursday", 25),
            DayScore(LocalDate(2026, 3, 28), "Friday", 18),
            null,
            null
        ),
        weeklyTotal = 88
    ),
    WeekScoreData(
        dateRangeLabel = "Mar 17-23",
        dailyScores = listOf(
            DayScore(LocalDate(2026, 3, 17), "Monday", 12),
            DayScore(LocalDate(2026, 3, 18), "Tuesday", -4),
            DayScore(LocalDate(2026, 3, 19), "Wednesday", 19),
            DayScore(LocalDate(2026, 3, 20), "Thursday", 5),
            DayScore(LocalDate(2026, 3, 21), "Friday", 22),
            DayScore(LocalDate(2026, 3, 22), "Saturday", 16),
            DayScore(LocalDate(2026, 3, 23), "Sunday", 11),
        ),
        weeklyTotal = 81
    ),
    WeekScoreData(
        dateRangeLabel = "Mar 10-16",
        dailyScores = listOf(
            null,
            null,
            DayScore(LocalDate(2026, 3, 12), "Wednesday", 3),
            null,
            null,
            null,
            null
        ),
        weeklyTotal = 3
    )
)

// endregion
