package co.theportman.way_of_the_goat

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.theportman.way_of_the_goat.screens.DesignTokensScreen
import co.theportman.way_of_the_goat.screens.HelpScreen
import co.theportman.way_of_the_goat.screens.HomeScreen
import co.theportman.way_of_the_goat.screens.IntroFlowScreen
import co.theportman.way_of_the_goat.screens.RunsOverTimeScreen
import co.theportman.way_of_the_goat.screens.ScoresOverTimeScreen
import co.theportman.way_of_the_goat.screens.ScoresScreen
import co.theportman.way_of_the_goat.screens.SecondPage
import co.theportman.way_of_the_goat.ui.theme.WayOfTheGoatTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    WayOfTheGoatTheme(darkTheme = isSystemInDarkTheme()) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // State to hold the target date for the Scores screen
        var targetScoresDateEpochDay by remember { mutableStateOf<Long?>(null) }

        // Determine if bottom nav should be visible (hide on SecondPage and Home)
        val showBottomNav = currentRoute in listOf(
            Screen.Scores.route,
            Screen.ScoresOverTime.route,
            Screen.RunsOverTime.route,
            Screen.Help.route
        )

        Scaffold(
            modifier = modifier,
            bottomBar = {
                if (showBottomNav) {
                    BottomNavigationBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        onScreenNavigate = { screen ->
                            when (screen) {
                                Screen.Scores -> targetScoresDateEpochDay = null
                                else -> {}
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onContinueClick = {
                            navController.navigate(Screen.IntroFlow.route)
                        }
                    )
                }
                composable(Screen.IntroFlow.route) {
                    IntroFlowScreen(
                        onComplete = {
                            navController.navigate(Screen.Scores.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    )
                }
                composable(Screen.SecondPage.route) {
                    SecondPage()
                }
                composable(Screen.RunsOverTime.route) {
                    RunsOverTimeScreen()
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
                    HelpScreen(
                        onNavigateToDesignTokens = {
                            navController.navigate(Screen.DesignTokens.route)
                        }
                    )
                }
                composable(Screen.DesignTokens.route) {
                    DesignTokensScreen()
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
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
                    // Clear target date when navigating from bottom nav
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