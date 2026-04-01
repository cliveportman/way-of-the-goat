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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.screens.components.DataLossConfirmationDialog
import co.theportman.way_of_the_goat.screens.components.FoodCategoryRow
import co.theportman.way_of_the_goat.screens.components.ProfileSwitcherSheet
import co.theportman.way_of_the_goat.screens.components.ScoreSummary
import co.theportman.way_of_the_goat.ui.theme.goatColors
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

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

    // Collect profile switcher state
    val profileSwitcherState by viewModel.profileSwitcherState.collectAsState()

    // Snackbar for error messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect error events and show snackbar
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

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
            .background(MaterialTheme.goatColors.surface)
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
            // 2. If day is TODAY → always use current activeSuite
            // 3. If past day with no data → null (show "No profile selected")
            val pageSuite: ScoringSuite? = when {
                // Day has data - use the stored suite
                dailyServings != null -> {
                    SuiteDefinitions.getSuiteById(dailyServings.suiteId) ?: activeSuite
                }
                // Today - always use current activeSuite
                isToday -> {
                    activeSuite
                }
                // Past day with no data - no profile selected
                else -> {
                    null
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

        // Snackbar host for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Profile Switcher Bottom Sheet
        ProfileSwitcherSheet(
            isOpen = profileSwitcherState.isSheetOpen,
            onDismiss = { viewModel.closeProfileSwitcher() },
            profiles = viewModel.allProfiles,
            currentProfileId = profileSwitcherState.currentSuiteId ?: activeSuite.id,
            selectedProfileId = profileSwitcherState.selectedSuiteId ?: activeSuite.id,
            onProfileSelected = { viewModel.selectProfileInSheet(it) },
            isToday = profileSwitcherState.targetDate == today,
            useFutureChecked = profileSwitcherState.useFutureChecked,
            onUseFutureChanged = { viewModel.toggleFutureProfileCheckbox(it) },
            hasExistingData = profileSwitcherState.targetDate?.let { viewModel.hasExistingData(it) } ?: false,
            onSwitchProfile = { viewModel.initiateProfileSwitch() },
            onCancel = { viewModel.closeProfileSwitcher() },
            lastUsedSuiteId = profileSwitcherState.lastUsedSuiteId,
            isEmptyPastDay = profileSwitcherState.isEmptyPastDay
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
    displaySuite: ScoringSuite?,
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
                .background(MaterialTheme.goatColors.surface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.goatColors.onSurface)
                Text(
                    text = "Loading day data...",
                    color = MaterialTheme.goatColors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshCurrentDate(date) },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.goatColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Date heading
            Text(
                text = formatDate(date),
                color = MaterialTheme.goatColors.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Profile switch card
            ProfileSwitchCard(
                profileName = displaySuite?.name,
                onClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (displaySuite != null) {
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

                    // Score summary as last item in the list
                    item {
                        val totals = viewModel.getTotalsForDisplay(dailyServings, displaySuite)
                        ScoreSummary(totals = totals)
                    }
                }
            } else {
                // Empty state message
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select a profile above to start tracking",
                        color = MaterialTheme.goatColors.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSwitchCard(
    profileName: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "ACTIVE PROFILE",
                    color = MaterialTheme.goatColors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = profileName ?: "No profile selected",
                    color = if (profileName != null) MaterialTheme.goatColors.onSurface else MaterialTheme.goatColors.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Change profile",
                    color = MaterialTheme.goatColors.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Change profile",
                    tint = MaterialTheme.goatColors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
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
