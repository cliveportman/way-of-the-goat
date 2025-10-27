package co.theportman.way_of_the_goat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object SecondPage : Screen("second_page")
    data object Progress : Screen("progress")
    data object Today : Screen("today")
    data object Help : Screen("help")
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Progress, Icons.AutoMirrored.Filled.List, "Progress"),
    BottomNavItem(Screen.Today, Icons.Filled.Star, "Today"),
    BottomNavItem(Screen.Help, Icons.Filled.Info, "Help")
)
