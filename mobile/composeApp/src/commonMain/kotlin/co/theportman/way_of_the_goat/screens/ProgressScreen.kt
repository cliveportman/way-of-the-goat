package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@Composable
fun ProgressScreen(
    onDateClick: (LocalDate) -> Unit = {},
    viewModel: ProgressViewModel = viewModel { ProgressViewModel() }
) {
    // Get today's date
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    // Observe UI state
    val uiState by viewModel.uiState.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()

    // Find the Monday of the current week
    val currentWeekMonday = remember {
        val daysFromMonday = when (today.dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
            else -> 0 // Required: DayOfWeek is an expect enum in KMP, compiler requires else branch
        }
        today.minus(daysFromMonday, DateTimeUnit.DAY)
    }

    // Number of weeks to show (e.g., last 52 weeks up to current week)
    val numberOfWeeks = 52

    // Create pager state starting at the last page (current week)
    val pagerState = rememberPagerState(
        initialPage = numberOfWeeks - 1,
        pageCount = { numberOfWeeks }
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is ProgressUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading activities...")
                    }
                }
            }
            is ProgressUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error loading data",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is ProgressUiState.Success -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    // Calculate the Monday of this week
                    val weeksAgo = numberOfWeeks - 1 - page
                    val weekMonday = currentWeekMonday.minus(weeksAgo * 7, DateTimeUnit.DAY)
                    val weekSunday = weekMonday.plus(6, DateTimeUnit.DAY)

                    ProgressWeekContent(
                        weekMonday = weekMonday,
                        weekSunday = weekSunday,
                        viewModel = viewModel,
                        viewMode = viewMode,
                        onViewModeClick = { viewModel.cycleViewMode() },
                        onDateClick = onDateClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressWeekContent(
    weekMonday: LocalDate,
    weekSunday: LocalDate,
    viewModel: ProgressViewModel,
    viewMode: ViewMode,
    onViewModeClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // View mode toggle button
        Button(
            onClick = onViewModeClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(
                text = when (viewMode) {
                    ViewMode.ACTIVITIES -> "Activities"
                    ViewMode.NUTRITION -> "Nutrition"
                    ViewMode.COMBINED -> "Combined"
                },
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Week date range heading
        Text(
            text = formatWeekRange(weekMonday, weekSunday),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Week summary
        val weekSummary = viewModel.getWeekSummary(weekMonday, weekSunday)
        val weekNutritionSummary = viewModel.getWeekNutritionSummary(weekMonday, weekSunday)
        WeekSummaryCard(
            activitySummary = weekSummary,
            nutritionSummary = weekNutritionSummary,
            viewMode = viewMode
        )

        // 7 bars for days of the week
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (dayOffset in 0..6) {
                val currentDay = weekMonday.plus(dayOffset, DateTimeUnit.DAY)
                val daySummary = viewModel.getDaySummary(currentDay)
                val nutritionSummary = viewModel.getNutritionSummary(currentDay)
                DayBar(
                    date = currentDay,
                    activitySummary = daySummary,
                    nutritionSummary = nutritionSummary,
                    viewMode = viewMode,
                    onClick = { onDateClick(currentDay) }
                )
            }
        }
    }
}

@Composable
private fun WeekSummaryCard(
    activitySummary: WeekSummary,
    nutritionSummary: WeekNutritionSummary,
    viewMode: ViewMode
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        when (viewMode) {
            ViewMode.ACTIVITIES -> {
                // Show activity stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${activitySummary.activityCount}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (activitySummary.activityCount == 1) "Activity" else "Activities",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatDistance(activitySummary.totalDistanceKm),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "km total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            ViewMode.NUTRITION -> {
                // Show nutrition stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (nutritionSummary.totalScore >= 0) "+${nutritionSummary.totalScore}" else "${nutritionSummary.totalScore}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Weekly score",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            ViewMode.COMBINED -> {
                // Show both side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Activities column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${activitySummary.activityCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (activitySummary.activityCount == 1) "Activity" else "Activities",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${formatDistance(activitySummary.totalDistanceKm)} km",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                    )

                    // Nutrition column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (nutritionSummary.totalScore >= 0) "+${nutritionSummary.totalScore}" else "${nutritionSummary.totalScore}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Nutrition score",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayBar(
    date: LocalDate,
    activitySummary: DaySummary,
    nutritionSummary: NutritionSummary,
    viewMode: ViewMode,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day label (e.g., "Mon 21")
        Text(
            text = formatDayLabel(date),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(70.dp)
        )

        when (viewMode) {
            ViewMode.ACTIVITIES -> {
                // Bar showing activity data
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (activitySummary.activityCount > 0) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (activitySummary.activityCount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activitySummary.activityCount}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${formatDistance(activitySummary.totalDistanceKm)} km",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            ViewMode.NUTRITION -> {
                // Bar showing nutrition data
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (nutritionSummary.score >= 0) "+${nutritionSummary.score}" else "${nutritionSummary.score}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
            ViewMode.COMBINED -> {
                // Split bar showing both
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Activity bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (activitySummary.activityCount > 0) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activitySummary.activityCount > 0) {
                            Text(
                                text = "${activitySummary.activityCount} • ${formatDistance(activitySummary.totalDistanceKm)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Nutrition bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (nutritionSummary.score >= 0) "+${nutritionSummary.score}" else "${nutritionSummary.score}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }
    }
}

private fun formatWeekRange(monday: LocalDate, sunday: LocalDate): String {
    val mondayMonth = getMonthAbbreviation(monday.monthNumber)
    val sundayMonth = getMonthAbbreviation(sunday.monthNumber)

    return if (monday.year == sunday.year) {
        if (monday.monthNumber == sunday.monthNumber) {
            // Same month and year: "Jan 15 - 21, 2025"
            "$mondayMonth ${monday.dayOfMonth} - ${sunday.dayOfMonth}, ${monday.year}"
        } else {
            // Different months, same year: "Jan 29 - Feb 4, 2025"
            "$mondayMonth ${monday.dayOfMonth} - $sundayMonth ${sunday.dayOfMonth}, ${monday.year}"
        }
    } else {
        // Different years: "Dec 30, 2024 - Jan 5, 2025"
        "$mondayMonth ${monday.dayOfMonth}, ${monday.year} - $sundayMonth ${sunday.dayOfMonth}, ${sunday.year}"
    }
}

private fun formatDayLabel(date: LocalDate): String {
    val dayOfWeek = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
        else -> "?" // Required: DayOfWeek is an expect enum in KMP, compiler requires else branch
    }

    return "$dayOfWeek ${date.dayOfMonth}"
}

private fun getMonthAbbreviation(monthNumber: Int): String {
    return when (monthNumber) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
}

private fun formatDistance(km: Double): String {
    // Round to 1 decimal place
    val rounded = (km * 10).toInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        // If it's a whole number, show without decimal
        rounded.toInt().toString()
    } else {
        // Otherwise show with one decimal place
        rounded.toString()
    }
}
