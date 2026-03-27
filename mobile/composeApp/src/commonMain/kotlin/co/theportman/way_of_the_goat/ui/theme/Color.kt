package co.theportman.way_of_the_goat.ui.theme

import androidx.compose.ui.graphics.Color

object GoatPalette {
    // Slate
    val Slate50 = Color(0xFFF8FAFC)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate300 = Color(0xFFCAD5E2)
    val Slate400 = Color(0xFF90A1B9)
    val Slate500 = Color(0xFF62748E)
    val Slate600 = Color(0xFF45556C)
    val Slate700 = Color(0xFF314158)
    val Slate800 = Color(0xFF1D293D)
    val Slate900 = Color(0xFF0F172B)
    val Slate950 = Color(0xFF020618)

    // Lime
    val Lime50 = Color(0xFFF7FEE7)
    val Lime100 = Color(0xFFECFCCA)
    val Lime200 = Color(0xFFD8F999)
    val Lime300 = Color(0xFFBBF451)
    val Lime400 = Color(0xFF9AE600)
    val Lime500 = Color(0xFF7CCF00)
    val Lime600 = Color(0xFF5EA500)
    val Lime700 = Color(0xFF497D00)
    val Lime800 = Color(0xFF3C6300)
    val Lime900 = Color(0xFF35530E)
    val Lime950 = Color(0xFF192E03)

    // Green
    val Green50 = Color(0xFFF0FDF4)
    val Green100 = Color(0xFFDCFCE7)
    val Green200 = Color(0xFFB9F8CF)
    val Green300 = Color(0xFF7BF1A8)
    val Green400 = Color(0xFF05DF72)
    val Green500 = Color(0xFF00C950)
    val Green600 = Color(0xFF00A63E)
    val Green700 = Color(0xFF008236)
    val Green800 = Color(0xFF016630)
    val Green900 = Color(0xFF0D542B)
    val Green950 = Color(0xFF032E15)

    // Orange
    val Orange50 = Color(0xFFFFF7ED)
    val Orange100 = Color(0xFFFFEDD4)
    val Orange200 = Color(0xFFFFD6A7)
    val Orange300 = Color(0xFFFFB86A)
    val Orange400 = Color(0xFFFF8904)
    val Orange500 = Color(0xFFFF6900)
    val Orange600 = Color(0xFFF54900)
    val Orange700 = Color(0xFFCA3500)
    val Orange800 = Color(0xFF9F2D00)
    val Orange900 = Color(0xFF7E2A0C)
    val Orange950 = Color(0xFF441306)

    // Amber
    val Amber50 = Color(0xFFFFFBEB)
    val Amber100 = Color(0xFFFEF3C6)
    val Amber200 = Color(0xFFFEE685)
    val Amber300 = Color(0xFFFFD230)
    val Amber400 = Color(0xFFFFB900)
    val Amber500 = Color(0xFFFE9A00)
    val Amber600 = Color(0xFFE17100)
    val Amber700 = Color(0xFFBB4D00)
    val Amber800 = Color(0xFF973C00)
    val Amber900 = Color(0xFF7B3306)
    val Amber950 = Color(0xFF461901)

    // Red
    val Red50 = Color(0xFFFEF2F2)
    val Red100 = Color(0xFFFFE2E2)
    val Red200 = Color(0xFFFFC9C9)
    val Red300 = Color(0xFFFFA2A2)
    val Red400 = Color(0xFFFF6467)
    val Red500 = Color(0xFFFB2C36)
    val Red600 = Color(0xFFE7000B)
    val Red700 = Color(0xFFC10007)
    val Red800 = Color(0xFF9F0712)
    val Red900 = Color(0xFF82181A)
    val Red950 = Color(0xFF460809)
}

data class GoatColorScheme(
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val scorePlus2: Color,
    val scorePlus1: Color,
    val score0: Color,
    val scoreMinus1: Color,
    val scoreMinus2: Color,
    val scoreMinus3: Color,
    val primary: Color,
    val onPrimary: Color,
    val error: Color,
    val elevation0: Color,
    val elevation1: Color,
    val elevation2: Color,
    val elevation3: Color,
    val elevation4: Color,
)

val GoatDarkColorScheme = GoatColorScheme(
    surface = GoatPalette.Slate950,
    surfaceContainer = GoatPalette.Slate900,
    surfaceContainerHigh = GoatPalette.Slate800,
    onSurface = GoatPalette.Slate50,
    onSurfaceVariant = GoatPalette.Slate400,
    outline = GoatPalette.Slate400,
    scorePlus2 = GoatPalette.Lime400,
    scorePlus1 = GoatPalette.Lime300,
    score0 = GoatPalette.Green300,
    scoreMinus1 = GoatPalette.Orange300,
    scoreMinus2 = GoatPalette.Orange400,
    scoreMinus3 = GoatPalette.Red500,
    primary = GoatPalette.Lime400,
    onPrimary = GoatPalette.Slate950,
    error = GoatPalette.Red500,
    elevation0 = GoatPalette.Slate950,
    elevation1 = GoatPalette.Slate900,
    elevation2 = GoatPalette.Slate800,
    elevation3 = GoatPalette.Slate700,
    elevation4 = GoatPalette.Slate600,
)

val GoatLightColorScheme = GoatColorScheme(
    surface = GoatPalette.Slate50,
    surfaceContainer = GoatPalette.Slate100,
    surfaceContainerHigh = GoatPalette.Slate200,
    onSurface = GoatPalette.Slate950,
    onSurfaceVariant = GoatPalette.Slate500,
    outline = GoatPalette.Slate500,
    scorePlus2 = GoatPalette.Lime400,
    scorePlus1 = GoatPalette.Lime300,
    score0 = GoatPalette.Green300,
    scoreMinus1 = GoatPalette.Orange300,
    scoreMinus2 = GoatPalette.Orange400,
    scoreMinus3 = GoatPalette.Red500,
    primary = GoatPalette.Lime400,
    onPrimary = GoatPalette.Slate950,
    error = GoatPalette.Red500,
    elevation0 = GoatPalette.Slate50,
    elevation1 = GoatPalette.Slate100,
    elevation2 = GoatPalette.Slate200,
    elevation3 = GoatPalette.Slate300,
    elevation4 = GoatPalette.Slate400,
)
