package co.theportman.way_of_the_goat

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import co.theportman.way_of_the_goat.screens.HelpScreen
import co.theportman.way_of_the_goat.screens.HomeScreen
import co.theportman.way_of_the_goat.screens.ProgressScreen
import co.theportman.way_of_the_goat.screens.ScoresScreen
import co.theportman.way_of_the_goat.screens.SecondPage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Determine if bottom nav should be visible (hide on SecondPage and Home)
        val showBottomNav = currentRoute in listOf(
            Screen.Progress.route,
            Screen.Scores.route,
            Screen.Help.route
        )

        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    BottomNavigationBar(
                        navController = navController,
                        currentRoute = currentRoute
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
                            navController.navigate(Screen.Progress.route)
                        }
                    )
                }
                composable(Screen.SecondPage.route) {
                    SecondPage()
                }
                composable(Screen.Progress.route) {
                    ProgressScreen()
                }
                composable(Screen.Scores.route) {
                    ScoresScreen()
                }
                composable(Screen.Help.route) {
                    HelpScreen()
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.screen.route,
                onClick = {
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