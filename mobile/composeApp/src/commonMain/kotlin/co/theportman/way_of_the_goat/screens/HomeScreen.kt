package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.theportman.way_of_the_goat.ui.components.ContinueButton
import co.theportman.way_of_the_goat.ui.icons.GoatMoon
import co.theportman.way_of_the_goat.ui.theme.goatColors

@Composable
fun HomeScreen(onContinueClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.goatColors.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 64.dp)
        ) {
            // Top spacing
            Spacer(modifier = Modifier.height(40.dp))

            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Goat logo
                Image(
                    painter = rememberVectorPainter(GoatMoon),
                    contentDescription = "Way of the Goat Logo",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = "Way of the Goat",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.goatColors.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle
                Text(
                    text = "Diet scoring for endurance athletes",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.goatColors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Attribution text
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Normal
                            )
                        ) {
                            append("Based on the book\n")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Italic
                            )
                        ) {
                            append("Racing Weight\n")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Normal
                            )
                        ) {
                            append("by Matt Fitzgerald")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.goatColors.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Continue button
            ContinueButton(onClick = onContinueClick)

            // Bottom spacing
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
