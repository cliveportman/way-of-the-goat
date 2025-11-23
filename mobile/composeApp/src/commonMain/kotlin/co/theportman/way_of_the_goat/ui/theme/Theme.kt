package co.theportman.way_of_the_goat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Design tokens from Figma
object GoatColors {
    val Navy950 = Color(0xFF020618)      // Background
    val Navy900 = Color(0xFF0f172b)      // Button background
    val Navy800 = Color(0xFF1d293d)      // Button border
    val Slate50 = Color(0xFFf8fafc)      // Primary text
    val Slate100 = Color(0xFFf1f5f9)     // Logo color
    val Slate400 = Color(0xFF90a1b9)     // Subtitle text
}

private val DarkColorScheme = darkColorScheme(
    primary = GoatColors.Slate50,
    onPrimary = GoatColors.Navy950,
    primaryContainer = GoatColors.Navy900,
    onPrimaryContainer = GoatColors.Slate50,
    secondary = GoatColors.Slate400,
    onSecondary = GoatColors.Navy950,
    background = GoatColors.Navy950,
    onBackground = GoatColors.Slate50,
    surface = GoatColors.Navy900,
    onSurface = GoatColors.Slate50,
    outline = GoatColors.Navy800
)

@Composable
fun WayOfTheGoatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = GoatTypography,
        content = content
    )
}
