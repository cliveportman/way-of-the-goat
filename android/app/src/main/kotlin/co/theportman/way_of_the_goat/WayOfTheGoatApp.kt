package co.theportman.way_of_the_goat

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.theportman.way_of_the_goat.screens.HelpScreen
import co.theportman.way_of_the_goat.screens.HomeScreen
import co.theportman.way_of_the_goat.screens.IntroFlowScreen
import co.theportman.way_of_the_goat.screens.ScoresOverTimeScreen
import co.theportman.way_of_the_goat.screens.ScoresScreen

/**
 * Root navigation host for the scoring-only app.
 *
 * On a normal launch the start destination is [Screen.Scores]. On first launch it is
 * the branded welcome screen [Screen.Home], whose Continue button leads into
 * [Screen.IntroFlow]; finishing the intro persists the flag via [onOnboardingComplete]
 * and pops the Home + intro entries off the back stack. The bottom navigation bar is
 * hidden during the welcome/onboarding screens.
 *
 * @param showOnboarding Whether to start on the first-run welcome + intro flow.
 * @param onOnboardingComplete Called when the user finishes the intro flow.
 */
@Composable
fun WayOfTheGoatApp(
    showOnboarding: Boolean,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Target date for the Scores screen, set when a day is tapped on the history screen.
    var targetScoresDateEpochDay by remember { mutableStateOf<Long?>(null) }

    val showBottomNav = currentRoute in listOf(
        Screen.Scores.route,
        Screen.ScoresOverTime.route,
        Screen.Help.route
    )

    val startDestination = if (showOnboarding) Screen.Home.route else Screen.Scores.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    onScreenNavigate = { screen ->
                        if (screen == Screen.Scores) targetScoresDateEpochDay = null
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onContinueClick = { navController.navigate(Screen.IntroFlow.route) }
                )
            }
            composable(Screen.IntroFlow.route) {
                IntroFlowScreen(
                    onComplete = {
                        onOnboardingComplete()
                        navController.navigate(Screen.Scores.route) {
                            // Clear the welcome + intro entries so back doesn't return to them.
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Scores.route) {
                ScoresScreen(
                    targetDateEpochDay = targetScoresDateEpochDay
                )
            }
            composable(Screen.ScoresOverTime.route) {
                ScoresOverTimeScreen(
                    onDateClick = { date ->
                        targetScoresDateEpochDay = date.toEpochDays().toLong()
                        navController.navigate(Screen.Scores.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Help.route) {
                HelpScreen()
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?,
    onScreenNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    onScreenNavigate(item.screen)
                    navController.navigate(item.screen.route) {
                        // Pop up to the start destination to avoid building up a large stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
