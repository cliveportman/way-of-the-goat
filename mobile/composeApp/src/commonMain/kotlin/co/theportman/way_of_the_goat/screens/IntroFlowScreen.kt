package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.theportman.way_of_the_goat.ui.theme.GoatColors
import kotlinx.coroutines.launch

/**
 * Data class representing content for a single intro page
 */
data class IntroContent(
    val heading: String,
    val bodyParagraphs: List<String>
)

/**
 * Intro flow with 3 screens explaining the app
 *
 * TODO: Add preferences storage to show this only on first launch
 * Currently always shows intro flow after splash screen
 */
@Composable
fun IntroFlowScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    // Define content for all three pages
    val pages = listOf(
        IntroContent(
            heading = "What?",
            bodyParagraphs = listOf(
                "This app is designed to help endurance athletes quickly and easily score what they eat.",
                "By working towards your optimal racing weight, you'll perform better in your races.",
                "And you achieve this by focusing on diet quality, not counting calories."
            )
        ),
        IntroContent(
            heading = "Why?",
            bodyParagraphs = listOf(
                "Weigh yourself regularly, and you're more likely to hit your weight goal. Monitor your diet regularly, you're more likely to nail your nutrition, too.",
                "The benefits of a good diet are well known. But for endurance athletes, a low body-fat percentage is unfortunately a prerequisite for success. What to do?",
                "This app exists to help you quickly and easily track your diet, and score it, helping you towards your racing weight so you can achieve those podium places or finish those bucket list races."
            )
        ),
        IntroContent(
            heading = "How?",
            bodyParagraphs = listOf(
                "Each day, record your dietary intake using the food categories provided. Nutritious foods will raise your score, unhealthy foods will lower it."
            )
        )
    )

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = true // Enable swipe gestures to navigate between pages
    ) { page ->
        IntroPage(
            content = pages[page],
            currentPage = page,
            totalPages = 3,
            onContinueClick = {
                if (page < 2) {
                    scope.launch { pagerState.animateScrollToPage(page + 1) }
                } else {
                    onComplete()
                }
            }
        )
    }
}

/**
 * Reusable component for a single intro page
 */
@Composable
private fun IntroPage(
    content: IntroContent,
    currentPage: Int,
    totalPages: Int,
    onContinueClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GoatColors.Navy950),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 64.dp)
        ) {
            // Scrollable content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Heading
                Text(
                    text = content.heading,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoatColors.Slate100
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Body paragraphs
                content.bodyParagraphs.forEachIndexed { index, paragraph ->
                    Text(
                        text = paragraph,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        ),
                        color = GoatColors.Slate50
                    )

                    if (index < content.bodyParagraphs.size - 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Continue button (matching HomeScreen.kt exact position)
            OutlinedButton(
                onClick = onContinueClick,
                modifier = Modifier
                    .size(110.dp)
                    .background(GoatColors.Navy900, CircleShape),
                shape = CircleShape,
                border = BorderStroke(1.dp, GoatColors.Navy800)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp
                    ),
                    color = GoatColors.Slate50
                )
            }

            // Bottom spacing (matches HomeScreen exactly)
            Spacer(modifier = Modifier.height(24.dp))

            // Page indicators (at very bottom)
            PageIndicators(
                currentPage = currentPage,
                totalPages = totalPages
            )
        }
    }
}

/**
 * Page indicators component showing current progress
 */
@Composable
private fun PageIndicators(
    currentPage: Int,
    totalPages: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index == currentPage) GoatColors.Slate50 else GoatColors.Navy800,
                        shape = CircleShape
                    )
            )
        }
    }
}
