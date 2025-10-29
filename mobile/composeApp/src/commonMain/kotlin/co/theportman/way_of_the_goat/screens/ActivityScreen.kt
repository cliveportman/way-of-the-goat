package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.theportman.way_of_the_goat.data.remote.models.Activity
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

@Composable
fun ActivityScreen(
    targetDateEpochDay: Long? = null,
    viewModel: ActivityViewModel = viewModel()
) {
    // Get today's date
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Calculate number of days to show (e.g., last 365 days up to today)
    val numberOfDays = 365

    // Calculate initial page based on target date or default to today
    val initialPage = remember(targetDateEpochDay) {
        if (targetDateEpochDay != null) {
            val targetDate = LocalDate.fromEpochDays(targetDateEpochDay.toInt())
            val daysAgo = today.toEpochDays() - targetDate.toEpochDays()
            val page = numberOfDays - 1 - daysAgo
            // Ensure page is within valid range
            page.coerceIn(0, numberOfDays - 1)
        } else {
            numberOfDays - 1 // Default to today
        }
    }

    // Create pager state
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { numberOfDays }
    )

    // Navigate to the target page when targetDateEpochDay changes
    LaunchedEffect(targetDateEpochDay) {
        if (targetDateEpochDay != null) {
            val targetDate = LocalDate.fromEpochDays(targetDateEpochDay.toInt())
            val daysAgo = today.toEpochDays() - targetDate.toEpochDays()
            val page = numberOfDays - 1 - daysAgo
            val validPage = page.coerceIn(0, numberOfDays - 1)
            pagerState.scrollToPage(validPage)
        }
    }

    // Calculate the current date based on the current page
    val currentDate = remember {
        derivedStateOf {
            val daysAgo = numberOfDays - 1 - pagerState.currentPage
            today.minus(daysAgo, DateTimeUnit.DAY)
        }
    }

    // Monitor scroll position and preload data
    LaunchedEffect(currentDate.value) {
        viewModel.ensureDateLoaded(currentDate.value)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        // Calculate date for this page
        val daysAgo = numberOfDays - 1 - page
        val pageDate = today.minus(daysAgo, DateTimeUnit.DAY)

        // Check if this date's data is loaded
        val isDateLoaded = viewModel.isDateLoaded(pageDate)

        ActivityPageContent(
            date = pageDate,
            viewModel = viewModel,
            uiState = uiState,
            isDateLoaded = isDateLoaded
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityPageContent(
    date: LocalDate,
    viewModel: ActivityViewModel,
    uiState: ActivityUiState,
    isDateLoaded: Boolean
) {
    // Collect refresh state
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Show loading indicator if date data isn't loaded yet
    if (!isDateLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator()
                Text(
                    text = "Loading day data...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshCurrentDate(date) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Date heading
            Text(
                text = formatDate(date),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // Activities list
            when (uiState) {
                is ActivityUiState.Loading -> {
                    Text(
                        text = "Loading activities...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                is ActivityUiState.Success -> {
                    val activities = viewModel.getActivitiesForDate(date)
                    if (activities.isEmpty()) {
                        Text(
                            text = "No activities for this day",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ActivitiesList(activities = activities)
                    }
                }
                is ActivityUiState.Error -> {
                    Text(
                        text = "Error: ${uiState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivitiesList(activities: List<Activity>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(activities) { activity ->
            ActivityCard(activity = activity)
        }
    }
}

@Composable
private fun ActivityCard(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Activity name
            Text(
                text = activity.name ?: "Unnamed Activity",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Activity type
            activity.type?.let { type ->
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Distance and moving time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                activity.distance?.let { distance ->
                    val distanceKm = (distance / 1000.0 * 100).toInt() / 100.0
                    Text(
                        text = "Distance: $distanceKm km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                activity.movingTime?.let { movingTime ->
                    val hours = movingTime / 3600
                    val minutes = (movingTime % 3600) / 60
                    val timeStr = if (hours > 0) {
                        "${hours}h ${minutes}m"
                    } else {
                        "${minutes}m"
                    }
                    Text(
                        text = "Time: $timeStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDate(date: LocalDate): String {
    val dayOfWeek = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Monday"
        DayOfWeek.TUESDAY -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY -> "Friday"
        DayOfWeek.SATURDAY -> "Saturday"
        DayOfWeek.SUNDAY -> "Sunday"
        else -> "Unknown" // Required: DayOfWeek is an expect enum in KMP, compiler requires else branch
    }

    val month = when (date.monthNumber) {
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

    return "$dayOfWeek, $month ${date.dayOfMonth}, ${date.year}"
}
