package co.theportman.way_of_the_goat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object SecondPage : Screen("second_page")
    data object Progress : Screen("progress")
    data object Scores : Screen("scores")
    data object Activity : Screen("activity")
    data object Help : Screen("help")
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Scores, Icons.Filled.GridView, "Scores"),
    BottomNavItem(Screen.Activity, Icons.AutoMirrored.Filled.List, "Activity"),
    BottomNavItem(Screen.Progress, Icons.Filled.ShowChart, "Progress"),
    BottomNavItem(Screen.Help, Icons.Filled.Info, "Help")
)
