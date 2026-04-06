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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.theportman.way_of_the_goat.data.scoring.model.DayActivity
import co.theportman.way_of_the_goat.data.scoring.model.WeekActivityData
import co.theportman.way_of_the_goat.screens.components.RunWeekRow
import co.theportman.way_of_the_goat.ui.theme.GoatSpacing
import co.theportman.way_of_the_goat.screens.components.dayOfWeekLabels
import co.theportman.way_of_the_goat.ui.theme.WayOfTheGoatTheme
import co.theportman.way_of_the_goat.ui.theme.goatColors
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RunsOverTimeScreen(
    viewModel: RunsOverTimeViewModel = viewModel { RunsOverTimeViewModel() },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errors.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        RunsOverTimeContent(uiState = uiState)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun RunsOverTimeContent(
    uiState: RunsOverTimeUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.goatColors.surface)
            .padding(horizontal = GoatSpacing.s12)
    ) {
        Spacer(modifier = Modifier.height(GoatSpacing.s32))

        // Screen title
        Text(
            text = "Your endurance activities",
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
            is RunsOverTimeUiState.Loading -> {
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

            is RunsOverTimeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GoatSpacing.s24)
                ) {
                    items(
                        items = uiState.weeks,
                        key = { it.dateRangeLabel }
                    ) { weekData ->
                        RunWeekRow(weekData = weekData)
                    }
                }
            }

            is RunsOverTimeUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No runs yet. Start logging activities to see your distance here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.goatColors.onSurfaceVariant
                    )
                }
            }

            is RunsOverTimeUiState.Error -> {
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
 * Uses the same weight(1f) + s4 gap distribution as the distance tiles.
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
                textAlign = TextAlign.Center
            )
        }
    }
}

// region Previews

@Preview(name = "Dark")
@Composable
private fun RunsOverTimeScreenPreview() {
    WayOfTheGoatTheme {
        RunsOverTimeContent(
            uiState = RunsOverTimeUiState.Success(weeks = sampleActivityWeeks())
        )
    }
}

@Preview(name = "Light")
@Composable
private fun RunsOverTimeScreenPreviewLight() {
    WayOfTheGoatTheme(darkTheme = false) {
        RunsOverTimeContent(
            uiState = RunsOverTimeUiState.Success(weeks = sampleActivityWeeks())
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun RunsOverTimeScreenPreviewLoading() {
    WayOfTheGoatTheme {
        RunsOverTimeContent(uiState = RunsOverTimeUiState.Loading)
    }
}

@Preview(name = "Empty")
@Composable
private fun RunsOverTimeScreenPreviewEmpty() {
    WayOfTheGoatTheme {
        RunsOverTimeContent(uiState = RunsOverTimeUiState.Empty)
    }
}

private fun sampleActivityWeeks(): List<WeekActivityData> = listOf(
    WeekActivityData(
        dateRangeLabel = "Mar 24-30",
        dailyActivities = listOf(
            DayActivity(LocalDate(2026, 3, 24), "Monday", 14.2, 1),
            DayActivity(LocalDate(2026, 3, 25), "Tuesday", 8.0, 2),
            DayActivity(LocalDate(2026, 3, 26), "Wednesday", 12.5, 1),
            DayActivity(LocalDate(2026, 3, 27), "Thursday", 21.3, 3),
            DayActivity(LocalDate(2026, 3, 28), "Friday", 18.1, 1),
            null,
            null
        ),
        weeklyTotalKm = 74.1
    ),
    WeekActivityData(
        dateRangeLabel = "Mar 17-23",
        dailyActivities = listOf(
            DayActivity(LocalDate(2026, 3, 17), "Monday", 10.0, 1),
            null,
            DayActivity(LocalDate(2026, 3, 19), "Wednesday", 15.3, 1),
            null,
            DayActivity(LocalDate(2026, 3, 21), "Friday", 22.0, 2),
            DayActivity(LocalDate(2026, 3, 22), "Saturday", 16.8, 1),
            DayActivity(LocalDate(2026, 3, 23), "Sunday", 22.5, 1),
        ),
        weeklyTotalKm = 86.6
    ),
    WeekActivityData(
        dateRangeLabel = "Mar 10-16",
        dailyActivities = listOf(
            null,
            null,
            DayActivity(LocalDate(2026, 3, 12), "Wednesday", 8.0, 1),
            null,
            null,
            null,
            null
        ),
        weeklyTotalKm = 8.0
    )
)

// endregion
