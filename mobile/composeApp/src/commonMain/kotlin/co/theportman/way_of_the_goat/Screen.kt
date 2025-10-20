package co.theportman.way_of_the_goat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object SecondPage : Screen("second_page")
    data object Progress : Screen("progress")
    data object Scores : Screen("scores")
    data object Help : Screen("help")
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Progress, Icons.Filled.List, "Progress"),
    BottomNavItem(Screen.Scores, Icons.Filled.Star, "Scores"),
    BottomNavItem(Screen.Help, Icons.Filled.Info, "Help")
)
