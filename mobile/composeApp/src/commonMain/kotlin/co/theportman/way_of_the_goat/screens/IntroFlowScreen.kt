package co.theportman.way_of_the_goat.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.theportman.way_of_the_goat.ui.components.ContinueButton
import co.theportman.way_of_the_goat.ui.theme.goatColors
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
fun IntroFlowScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.goatColors.surface)
    ) {
        // Only the text content slides
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            IntroPageContent(content = pages[page])
        }

        // Fixed bottom section - doesn't slide with pager
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicators(
                currentPage = pagerState.currentPage,
                totalPages = 3
            )
            Spacer(modifier = Modifier.height(24.dp))
            ContinueButton(onClick = {
                if (pagerState.currentPage < 2) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    onComplete()
                }
            })
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Content for a single intro page (only the sliding content)
 */
@Composable
private fun IntroPageContent(content: IntroContent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 64.dp, bottom = 200.dp) // Bottom padding for fixed controls
            .verticalScroll(rememberScrollState())
    ) {
        // Heading
        Text(
            text = content.heading,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.goatColors.onSurface
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
                color = MaterialTheme.goatColors.onSurface
            )

            if (index < content.bodyParagraphs.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
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
                        color = if (index == currentPage) MaterialTheme.goatColors.onSurface else MaterialTheme.goatColors.surfaceContainerHigh,
                        shape = CircleShape
                    )
            )
        }
    }
}
