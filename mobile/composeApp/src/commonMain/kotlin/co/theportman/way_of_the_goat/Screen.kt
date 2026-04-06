package co.theportman.way_of_the_goat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object IntroFlow : Screen("intro_flow")
    data object SecondPage : Screen("second_page")
    data object Scores : Screen("scores")
    data object ScoresOverTime : Screen("scores_over_time")
    data object RunsOverTime : Screen("runs_over_time")
    data object Help : Screen("help")
    data object DesignTokens : Screen("design_tokens")
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Scores, Icons.Filled.GridView, "Scores"),
    BottomNavItem(Screen.ScoresOverTime, Icons.AutoMirrored.Filled.List, "History"),
    BottomNavItem(Screen.RunsOverTime, Icons.Filled.ShowChart, "Runs"),
    BottomNavItem(Screen.Help, Icons.Filled.Info, "User guide")
)
