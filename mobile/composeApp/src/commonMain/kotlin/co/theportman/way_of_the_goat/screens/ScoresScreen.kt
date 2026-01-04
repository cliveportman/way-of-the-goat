package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.screens.components.FoodCategoryRow
import co.theportman.way_of_the_goat.screens.components.ScoreSummary
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

// Figma colors
private val BackgroundColor = Color(0xFF020618)  // slate-950
private val TextColor = Color(0xFFF1F5F9)        // slate-100

@Composable
fun ScoresScreen(
    targetDateEpochDay: Long? = null,
    viewModel: ScoresViewModel = viewModel { ScoresViewModel() }
) {
    // Get today's date
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Collect active suite
    val activeSuite by viewModel.activeSuite.collectAsState()

    // Collect servings flow for reactive updates
    val servingsMap by viewModel.servingsFlow.collectAsState()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // Calculate date for this page
            val daysAgo = numberOfDays - 1 - page
            val pageDate = today.minus(daysAgo, DateTimeUnit.DAY)

            // Check if this date's data is loaded
            val isDateLoaded = viewModel.isDateLoaded(pageDate)

            // Get servings for this date from the flow
            val dailyServings = servingsMap[pageDate]

            ScoresPageContent(
                date = pageDate,
                viewModel = viewModel,
                uiState = uiState,
                isDateLoaded = isDateLoaded,
                activeSuite = activeSuite,
                dailyServings = dailyServings
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoresPageContent(
    date: LocalDate,
    viewModel: ScoresViewModel,
    uiState: ScoresUiState,
    isDateLoaded: Boolean,
    activeSuite: ScoringSuite,
    dailyServings: DailyServings?
) {
    // Collect refresh state
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Show loading indicator if date data isn't loaded yet
    if (!isDateLoaded && uiState is ScoresUiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(color = TextColor)
                Text(
                    text = "Loading day data...",
                    color = TextColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    // Get totals for display
    val totals = viewModel.getTotalsForDate(date)

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshCurrentDate(date) },
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            // Date heading
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = formatDate(date),
                color = TextColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Food categories list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = activeSuite.categories,
                    key = { it.id.value }
                ) { category ->
                    val servingCount = dailyServings?.getServings(category.id) ?: 0

                    FoodCategoryRow(
                        category = category,
                        servingCount = servingCount,
                        onIncrement = {
                            viewModel.incrementServings(date, category.id)
                        },
                        onDecrement = {
                            viewModel.decrementServings(date, category.id)
                        }
                    )
                }
            }

            // Score summary at bottom
            ScoreSummary(totals = totals)
        }
    }
}

private fun formatDate(date: LocalDate): String {
    val dayOfWeek = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
        else -> ""
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

    return "$dayOfWeek ${date.dayOfMonth} $month"
}
