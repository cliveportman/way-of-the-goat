package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import co.theportman.way_of_the_goat.screens.components.DataLossConfirmationDialog
import co.theportman.way_of_the_goat.screens.components.FoodCategoryRow
import co.theportman.way_of_the_goat.screens.components.ProfileSwitcherSheet
import co.theportman.way_of_the_goat.screens.components.ScoreSummary
import co.theportman.way_of_the_goat.ui.theme.GoatColors
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // Collect profile history flow for reactive updates
    val profileHistoryMap by viewModel.profileHistoryFlow.collectAsState()

    // Collect profile switcher state
    val profileSwitcherState by viewModel.profileSwitcherState.collectAsState()

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

            // Check if this is today
            val isToday = pageDate == today

            // Derive suite reactively with the following priority:
            // 1. If day has servings data → use stored suite_id
            // 2. If day is TODAY → always use current activeSuite (protected from history changes)
            // 3. If past day with no data → look up profile history, then fall back to activeSuite
            val pageSuite = when {
                // Day has data - use the stored suite
                dailyServings != null -> {
                    SuiteDefinitions.getSuiteById(dailyServings.suiteId) ?: activeSuite
                }
                // Today is protected - always use current activeSuite
                isToday -> {
                    activeSuite
                }
                // Past day with no data - check profile history
                else -> {
                    val historicalSuiteId = profileHistoryMap[pageDate]
                    if (historicalSuiteId != null) {
                        SuiteDefinitions.getSuiteById(historicalSuiteId) ?: activeSuite
                    } else {
                        activeSuite
                    }
                }
            }

            ScoresPageContent(
                date = pageDate,
                today = today,
                viewModel = viewModel,
                uiState = uiState,
                isDateLoaded = isDateLoaded,
                displaySuite = pageSuite,
                dailyServings = dailyServings,
                onProfileClick = { viewModel.openProfileSwitcher(pageDate) }
            )
        }

        // Profile Switcher Bottom Sheet
        ProfileSwitcherSheet(
            isOpen = profileSwitcherState.isSheetOpen,
            onDismiss = { viewModel.closeProfileSwitcher() },
            profiles = viewModel.allProfiles,
            currentProfileId = activeSuite.id,
            selectedProfileId = profileSwitcherState.selectedSuiteId ?: activeSuite.id,
            onProfileSelected = { viewModel.selectProfileInSheet(it) },
            isToday = profileSwitcherState.targetDate == today,
            useFutureChecked = profileSwitcherState.useFutureChecked,
            onUseFutureChanged = { viewModel.toggleFutureProfileCheckbox(it) },
            hasExistingData = profileSwitcherState.targetDate?.let { viewModel.hasExistingData(it) } ?: false,
            onSwitchProfile = { viewModel.initiateProfileSwitch() },
            onCancel = { viewModel.closeProfileSwitcher() }
        )

        // Data Loss Confirmation Dialog
        DataLossConfirmationDialog(
            isOpen = profileSwitcherState.showConfirmationDialog,
            onDismiss = { viewModel.cancelConfirmation() },
            profileName = viewModel.allProfiles.find { it.id == profileSwitcherState.selectedSuiteId }?.name ?: "",
            onSwitchAnyway = { viewModel.confirmProfileSwitch() },
            onKeepCurrent = { viewModel.cancelConfirmation() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoresPageContent(
    date: LocalDate,
    today: LocalDate,
    viewModel: ScoresViewModel,
    uiState: ScoresUiState,
    isDateLoaded: Boolean,
    displaySuite: ScoringSuite,
    dailyServings: DailyServings?,
    onProfileClick: () -> Unit
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

    // Get totals for display using the reactive servings and suite
    val totals = viewModel.getTotalsForDisplay(dailyServings, displaySuite)

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
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = formatDate(date),
                color = TextColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Score profile name (clickable to open switcher)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onProfileClick() }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = displaySuite.name,
                    color = GoatColors.Slate400,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = if (displaySuite.id == SuiteDefinitions.RACING_WEIGHT_ID) FontStyle.Italic else FontStyle.Normal
                )
                Text(
                    text = " profile",
                    color = GoatColors.Slate400,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Change profile",
                    tint = GoatColors.Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Food categories list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = displaySuite.categories,
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

    val day = date.dayOfMonth.toString().padStart(2, '0')

    return "$dayOfWeek $day $month"
}
