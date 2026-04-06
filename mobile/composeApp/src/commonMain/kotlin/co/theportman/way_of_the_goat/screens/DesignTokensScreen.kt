package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.theportman.way_of_the_goat.ui.theme.GoatPalette
import co.theportman.way_of_the_goat.ui.theme.GoatRadius
import co.theportman.way_of_the_goat.ui.theme.GoatSizing
import co.theportman.way_of_the_goat.ui.theme.GoatSpacing
import co.theportman.way_of_the_goat.ui.theme.GoatStroke
import co.theportman.way_of_the_goat.ui.theme.goatColors

@Composable
fun DesignTokensScreen(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.goatColors
    val typography = MaterialTheme.typography

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(16.dp)) }

        // -- Colours: Primitives --
        item { SectionHeading("Colours \u2014 Primitives") }

        item {
            PaletteRow("Slate", listOf(
                "50" to GoatPalette.Slate50,
                "100" to GoatPalette.Slate100,
                "200" to GoatPalette.Slate200,
                "300" to GoatPalette.Slate300,
                "400" to GoatPalette.Slate400,
                "500" to GoatPalette.Slate500,
                "600" to GoatPalette.Slate600,
                "700" to GoatPalette.Slate700,
                "800" to GoatPalette.Slate800,
                "900" to GoatPalette.Slate900,
                "950" to GoatPalette.Slate950,
            ))
        }

        item {
            PaletteRow("Lime", listOf(
                "50" to GoatPalette.Lime50,
                "100" to GoatPalette.Lime100,
                "200" to GoatPalette.Lime200,
                "300" to GoatPalette.Lime300,
                "400" to GoatPalette.Lime400,
                "500" to GoatPalette.Lime500,
                "600" to GoatPalette.Lime600,
                "700" to GoatPalette.Lime700,
                "800" to GoatPalette.Lime800,
                "900" to GoatPalette.Lime900,
                "950" to GoatPalette.Lime950,
            ))
        }

        item {
            PaletteRow("Green", listOf(
                "50" to GoatPalette.Green50,
                "100" to GoatPalette.Green100,
                "200" to GoatPalette.Green200,
                "300" to GoatPalette.Green300,
                "400" to GoatPalette.Green400,
                "500" to GoatPalette.Green500,
                "600" to GoatPalette.Green600,
                "700" to GoatPalette.Green700,
                "800" to GoatPalette.Green800,
                "900" to GoatPalette.Green900,
                "950" to GoatPalette.Green950,
            ))
        }

        item {
            PaletteRow("Orange", listOf(
                "50" to GoatPalette.Orange50,
                "100" to GoatPalette.Orange100,
                "200" to GoatPalette.Orange200,
                "300" to GoatPalette.Orange300,
                "400" to GoatPalette.Orange400,
                "500" to GoatPalette.Orange500,
                "600" to GoatPalette.Orange600,
                "700" to GoatPalette.Orange700,
                "800" to GoatPalette.Orange800,
                "900" to GoatPalette.Orange900,
                "950" to GoatPalette.Orange950,
            ))
        }

        item {
            PaletteRow("Amber", listOf(
                "50" to GoatPalette.Amber50,
                "100" to GoatPalette.Amber100,
                "200" to GoatPalette.Amber200,
                "300" to GoatPalette.Amber300,
                "400" to GoatPalette.Amber400,
                "500" to GoatPalette.Amber500,
                "600" to GoatPalette.Amber600,
                "700" to GoatPalette.Amber700,
                "800" to GoatPalette.Amber800,
                "900" to GoatPalette.Amber900,
                "950" to GoatPalette.Amber950,
            ))
        }

        item {
            PaletteRow("Red", listOf(
                "50" to GoatPalette.Red50,
                "100" to GoatPalette.Red100,
                "200" to GoatPalette.Red200,
                "300" to GoatPalette.Red300,
                "400" to GoatPalette.Red400,
                "500" to GoatPalette.Red500,
                "600" to GoatPalette.Red600,
                "700" to GoatPalette.Red700,
                "800" to GoatPalette.Red800,
                "900" to GoatPalette.Red900,
                "950" to GoatPalette.Red950,
            ))
        }

        // -- Colours: Semantic (Dark) --
        item { Spacer(Modifier.height(8.dp)) }
        item { SectionHeading("Colours \u2014 Semantic (Dark)") }

        val semanticTokens = listOf(
            "surface" to colors.surface,
            "surfaceContainer" to colors.surfaceContainer,
            "surfaceContainerHigh" to colors.surfaceContainerHigh,
            "onSurface" to colors.onSurface,
            "onSurfaceVariant" to colors.onSurfaceVariant,
            "outline" to colors.outline,
            "scorePlus2" to colors.scorePlus2,
            "scorePlus1" to colors.scorePlus1,
            "score0" to colors.score0,
            "scoreMinus1" to colors.scoreMinus1,
            "scoreMinus2" to colors.scoreMinus2,
            "scoreMinus3" to colors.scoreMinus3,
            "primary" to colors.primary,
            "onPrimary" to colors.onPrimary,
            "error" to colors.error,
            "elevation0" to colors.elevation0,
            "elevation1" to colors.elevation1,
            "elevation2" to colors.elevation2,
            "elevation3" to colors.elevation3,
            "elevation4" to colors.elevation4,
        )

        semanticTokens.forEach { (name, color) ->
            item(key = "semantic_$name") {
                SemanticColorRow(name = name, color = color)
            }
        }

        // -- Typography --
        item { Spacer(Modifier.height(8.dp)) }
        item { SectionHeading("Typography") }

        val typographySlots = listOf(
            "displayLarge" to typography.displayLarge,
            "displayMedium" to typography.displayMedium,
            "displaySmall" to typography.displaySmall,
            "headlineLarge" to typography.headlineLarge,
            "headlineMedium" to typography.headlineMedium,
            "headlineSmall" to typography.headlineSmall,
            "titleLarge" to typography.titleLarge,
            "titleMedium" to typography.titleMedium,
            "titleSmall" to typography.titleSmall,
            "bodyLarge" to typography.bodyLarge,
            "bodyMedium" to typography.bodyMedium,
            "bodySmall" to typography.bodySmall,
            "labelLarge" to typography.labelLarge,
            "labelMedium" to typography.labelMedium,
            "labelSmall" to typography.labelSmall,
        )

        typographySlots.forEach { (name, style) ->
            item(key = "type_$name") {
                TypographyRow(name = name, style = style)
            }
        }

        // -- Spacing --
        item { Spacer(Modifier.height(8.dp)) }
        item { SectionHeading("Spacing") }

        val spacingValues = listOf(
            "s1" to GoatSpacing.s1,
            "s2" to GoatSpacing.s2,
            "s4" to GoatSpacing.s4,
            "s8" to GoatSpacing.s8,
            "s12" to GoatSpacing.s12,
            "s16" to GoatSpacing.s16,
            "s20" to GoatSpacing.s20,
            "s24" to GoatSpacing.s24,
            "s32" to GoatSpacing.s32,
            "s40" to GoatSpacing.s40,
            "s48" to GoatSpacing.s48,
            "s64" to GoatSpacing.s64,
        )

        spacingValues.forEach { (name, dp) ->
            item(key = "spacing_$name") {
                SpacingRow(name = name, value = dp)
            }
        }

        // -- Sizing --
        item { Spacer(Modifier.height(8.dp)) }
        item { SectionHeading("Sizing") }

        val sizingValues = listOf(
            "xs" to GoatSizing.xs,
            "sm" to GoatSizing.sm,
            "md" to GoatSizing.md,
            "lg" to GoatSizing.lg,
            "xl" to GoatSizing.xl,
            "xl2" to GoatSizing.xl2,
            "xl3" to GoatSizing.xl3,
            "Icon.sm" to GoatSizing.Icon.sm,
            "Icon.md" to GoatSizing.Icon.md,
            "Icon.default" to GoatSizing.Icon.default,
            "Icon.lg" to GoatSizing.Icon.lg,
            "Touch.min" to GoatSizing.Touch.min,
            "Touch.default" to GoatSizing.Touch.default,
        )

        sizingValues.forEach { (name, dp) ->
            item(key = "sizing_$name") {
                SizingRow(name = name, value = dp)
            }
        }

        // -- Radius --
        item { Spacer(Modifier.height(8.dp)) }
        item { SectionHeading("Radius") }

        val radiusValues = listOf(
            "xs" to GoatRadius.xs,
            "sm" to GoatRadius.sm,
            "md" to GoatRadius.md,
            "lg" to GoatRadius.lg,
            "full" to GoatRadius.full,
        )

        radiusValues.forEach { (name, dp) ->
            item(key = "radius_$name") {
                RadiusRow(name = name, value = dp)
            }
        }

        // -- Stroke --
        item { Spacer(Modifier.height(8.dp)) }
        item { SectionHeading("Stroke") }

        val strokeValues = listOf(
            "default" to GoatStroke.default,
            "emphasis" to GoatStroke.emphasis,
            "strong" to GoatStroke.strong,
        )

        strokeValues.forEach { (name, dp) ->
            item(key = "stroke_$name") {
                StrokeRow(name = name, value = dp)
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun SectionHeading(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.goatColors.onSurface,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun PaletteRow(familyName: String, shades: List<Pair<String, Color>>) {
    val colors = MaterialTheme.goatColors

    Column {
        Text(
            text = familyName,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            shades.forEach { (shade, color) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                    Text(
                        text = shade,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SemanticColorRow(name: String, color: Color) {
    val colors = MaterialTheme.goatColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(
                    width = 1.dp,
                    color = colors.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = color.toHexString(),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun TypographyRow(name: String, style: TextStyle) {
    val colors = MaterialTheme.goatColors

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Text(
            text = "The quick brown fox",
            style = style,
            color = colors.onSurface
        )
    }
}

@Composable
private fun SpacingRow(name: String, value: Dp) {
    val colors = MaterialTheme.goatColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface,
            modifier = Modifier.width(40.dp)
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(value.coerceAtMost(280.dp))
                .height(12.dp)
                .background(
                    color = colors.primary,
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${value.value.toInt()}dp",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun SizingRow(name: String, value: Dp) {
    val colors = MaterialTheme.goatColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${value.value.toInt()}dp",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun RadiusRow(name: String, value: Dp) {
    val colors = MaterialTheme.goatColors
    // For "full" radius, cap the visual corner radius so the box is visible
    val visualRadius = if (value > 48.dp) 24.dp else value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(
                    width = 2.dp,
                    color = colors.primary,
                    shape = RoundedCornerShape(visualRadius)
                )
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (value > 1000.dp) "9999dp" else "${value.value.toInt()}dp",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun StrokeRow(name: String, value: Dp) {
    val colors = MaterialTheme.goatColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(
                    width = value,
                    color = colors.primary,
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${value.value.toInt()}dp",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

/**
 * Converts a Compose Color to a hex string like "#AARRGGBB" or "#RRGGBB".
 */
private fun Color.toHexString(): String {
    val alpha = (this.alpha * 255).toInt()
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return if (alpha == 255) {
        "#${red.toHexByte()}${green.toHexByte()}${blue.toHexByte()}"
    } else {
        "#${alpha.toHexByte()}${red.toHexByte()}${green.toHexByte()}${blue.toHexByte()}"
    }
}

private fun Int.toHexByte(): String =
    this.toString(16).padStart(2, '0').uppercase()
