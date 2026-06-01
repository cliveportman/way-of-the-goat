package co.theportman.way_of_the_goat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation destinations for the scoring-only app.
 *
 * The Intervals.icu activity stack (Runs Over Time) and the developer-only Design
 * Tokens screen were dropped in the native Android rewrite (ADR 001). [Home] is the
 * branded first-launch welcome/splash and is retained.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object IntroFlow : Screen("intro_flow")
    data object Scores : Screen("scores")
    data object ScoresOverTime : Screen("scores_over_time")
    data object Help : Screen("help")
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Scores, Icons.Filled.GridView, "Scores"),
    BottomNavItem(Screen.ScoresOverTime, Icons.AutoMirrored.Filled.List, "History"),
    BottomNavItem(Screen.Help, Icons.Filled.Info, "User guide")
)
