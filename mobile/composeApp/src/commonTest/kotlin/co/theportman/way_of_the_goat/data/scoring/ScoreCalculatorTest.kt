package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScoreCalculatorTest {

    private val balancedSuite = SuiteDefinitions.BALANCED
    private val testDate = LocalDate(2025, 1, 15)

    private fun createDailyServings(
        servings: Map<String, Int>,
        suiteId: SuiteId = SuiteDefinitions.BALANCED_ID
    ): DailyServings {
        return DailyServings(
            date = testDate,
            suiteId = suiteId,
            servings = servings.mapKeys { CategoryId(it.key) }
        )
    }

    @Test
    fun calculateDailyScore_emptyServings_returnsZero() {
        val dailyServings = createDailyServings(emptyMap())
        val score = ScoreCalculator.calculateDailyScore(dailyServings, balancedSuite)

        assertEquals(0, score)
    }

    @Test
    fun calculateDailyScore_singleCategory_calculatesCorrectly() {
        // 3 servings of fruit: 2 + 2 + 2 = 6
        val dailyServings = createDailyServings(mapOf("fruit" to 3))
        val score = ScoreCalculator.calculateDailyScore(dailyServings, balancedSuite)

        assertEquals(6, score)
    }

    @Test
    fun calculateDailyScore_multipleCategories_sumsCorrectly() {
        // 4 fruit: 2+2+2+1 = 7, 3 veg: 2+2+2 = 6, 2 protein: 2+2 = 4
        val dailyServings = createDailyServings(
            mapOf(
                "fruit" to 4,
                "veg" to 3,
                "leanproteins" to 2
            )
        )
        val score = ScoreCalculator.calculateDailyScore(dailyServings, balancedSuite)

        assertEquals(17, score)
    }

    @Test
    fun calculateDailyScore_withUnhealthyCategories_subtractsCorrectly() {
        // 2 fruit: 2+2 = 4, 2 sweets: -1 + -2 = -3
        val dailyServings = createDailyServings(
            mapOf(
                "fruit" to 2,
                "sweets" to 2
            )
        )
        val score = ScoreCalculator.calculateDailyScore(dailyServings, balancedSuite)

        assertEquals(1, score)
    }

    @Test
    fun calculateDailyScore_withWrongSuite_returnsNull() {
        val dailyServings = createDailyServings(
            mapOf("fruit" to 3),
            suiteId = SuiteDefinitions.RACING_WEIGHT_ID
        )

        // Using overload that returns nullable
        val score = ScoreCalculator.calculateDailyScore(dailyServings)
        assertNotNull(score) // Should work because Racing Weight suite exists

        // But calculateScore on DailyServings with mismatched suite returns null
        val resultWithWrongSuite = dailyServings.calculateScore(balancedSuite)
        assertNull(resultWithWrongSuite)
    }

    @Test
    fun calculateHealthyScore_onlyCountsHealthyCategories() {
        val dailyServings = createDailyServings(
            mapOf(
                "fruit" to 2,     // 2 + 2 = 4
                "sweets" to 3     // ignored for healthy score
            )
        )
        val healthyScore = ScoreCalculator.calculateHealthyScore(dailyServings, balancedSuite)

        assertEquals(4, healthyScore)
    }

    @Test
    fun calculateUnhealthyScore_onlyCountsUnhealthyCategories() {
        val dailyServings = createDailyServings(
            mapOf(
                "fruit" to 2,     // ignored for unhealthy score
                "sweets" to 2     // -1 + -2 = -3
            )
        )
        val unhealthyScore = ScoreCalculator.calculateUnhealthyScore(dailyServings, balancedSuite)

        assertEquals(-3, unhealthyScore)
    }

    @Test
    fun calculateScoreBreakdown_returnsCorrectBreakdown() {
        val dailyServings = createDailyServings(
            mapOf(
                "fruit" to 3,
                "veg" to 2
            )
        )
        val breakdown = ScoreCalculator.calculateScoreBreakdown(dailyServings, balancedSuite)

        // Should have all categories
        assertEquals(balancedSuite.categories.size, breakdown.size)

        // Check fruit breakdown
        val fruitBreakdown = breakdown.find { it.category.id.value == "fruit" }
        assertNotNull(fruitBreakdown)
        assertEquals(3, fruitBreakdown.servingCount)
        assertEquals(6, fruitBreakdown.score) // 2 + 2 + 2
        assertEquals(4, fruitBreakdown.targetServings)
        assertFalse(fruitBreakdown.targetMet) // 3 < 4

        // Check veg breakdown
        val vegBreakdown = breakdown.find { it.category.id.value == "veg" }
        assertNotNull(vegBreakdown)
        assertEquals(2, vegBreakdown.servingCount)
        assertEquals(4, vegBreakdown.score) // 2 + 2
    }

    @Test
    fun categoryScoreBreakdown_targetProgress_calculatesCorrectly() {
        val dailyServings = createDailyServings(mapOf("fruit" to 2))
        val breakdown = ScoreCalculator.calculateScoreBreakdown(dailyServings, balancedSuite)

        val fruitBreakdown = breakdown.find { it.category.id.value == "fruit" }
        assertNotNull(fruitBreakdown)

        // 2 servings / 4 target = 0.5
        assertEquals(0.5f, fruitBreakdown.targetProgress, 0.001f)
    }

    @Test
    fun categoryScoreBreakdown_targetMet_trueWhenTargetReached() {
        val dailyServings = createDailyServings(mapOf("fruit" to 4))
        val breakdown = ScoreCalculator.calculateScoreBreakdown(dailyServings, balancedSuite)

        val fruitBreakdown = breakdown.find { it.category.id.value == "fruit" }
        assertNotNull(fruitBreakdown)
        assertTrue(fruitBreakdown.targetMet)
    }

    @Test
    fun categoryScoreBreakdown_targetMet_forUnhealthyCategory() {
        // Sweets target is 0, so targetMet is true when servingCount is 0
        val dailyServingsNoSweets = createDailyServings(emptyMap())
        val breakdownNoSweets = ScoreCalculator.calculateScoreBreakdown(dailyServingsNoSweets, balancedSuite)
        val sweetsNoServings = breakdownNoSweets.find { it.category.id.value == "sweets" }
        assertNotNull(sweetsNoServings)
        assertTrue(sweetsNoServings.targetMet) // 0 servings = target met

        // When sweets are consumed, target is not met
        val dailyServingsWithSweets = createDailyServings(mapOf("sweets" to 1))
        val breakdownWithSweets = ScoreCalculator.calculateScoreBreakdown(dailyServingsWithSweets, balancedSuite)
        val sweetsWithServings = breakdownWithSweets.find { it.category.id.value == "sweets" }
        assertNotNull(sweetsWithServings)
        assertFalse(sweetsWithServings.targetMet) // 1 serving > 0 target
    }

    @Test
    fun calculateTotalsForDisplay_withServings_formatsCorrectly() {
        val dailyServings = createDailyServings(
            mapOf(
                "fruit" to 3,   // +6
                "veg" to 2,     // +4
                "sweets" to 1   // -1
            )
        )
        val totals = ScoreCalculator.calculateTotalsForDisplay(dailyServings, balancedSuite)

        assertEquals("+10", totals.healthy)
        assertEquals(-1, totals.unhealthy)
        assertEquals("+9", totals.total)
        assertEquals(6, totals.portions)
        assertTrue(totals.hasData)
    }

    @Test
    fun calculateTotalsForDisplay_emptyServings_returnsEmptyState() {
        val dailyServings = createDailyServings(emptyMap())
        val totals = ScoreCalculator.calculateTotalsForDisplay(dailyServings, balancedSuite)

        assertEquals("---", totals.healthy)
        assertNull(totals.unhealthy)
        assertEquals("---", totals.total)
        assertEquals(0, totals.portions)
        assertFalse(totals.hasData)
    }

    @Test
    fun calculateTotalsForMaths_returnsIntegerValues() {
        val dailyServings = createDailyServings(
            mapOf(
                "fruit" to 3,   // +6
                "sweets" to 2   // -3
            )
        )
        val totals = ScoreCalculator.calculateTotalsForMaths(dailyServings, balancedSuite)

        assertEquals(6, totals.healthy)
        assertEquals(-3, totals.unhealthy)
        assertEquals(3, totals.total)
        assertEquals(5, totals.portions)
    }

    @Test
    fun calculateWeeklyScore_emptyList_returnsZeros() {
        val summary = ScoreCalculator.calculateWeeklyScore(emptyList())

        assertEquals(0, summary.totalScore)
        assertEquals(0.0, summary.averageScore, 0.001)
        assertEquals(0, summary.daysTracked)
        assertTrue(summary.suitesUsed.isEmpty())
    }

    @Test
    fun calculateWeeklyScore_singleDay_calculatesCorrectly() {
        val dailyResults = listOf(
            DailyScoreResult(
                date = LocalDate(2025, 1, 15),
                score = 25,
                suiteId = SuiteDefinitions.BALANCED_ID,
                suiteName = "Balanced",
                maxPossibleScore = 32
            )
        )
        val summary = ScoreCalculator.calculateWeeklyScore(dailyResults)

        assertEquals(25, summary.totalScore)
        assertEquals(25.0, summary.averageScore, 0.001)
        assertEquals(1, summary.daysTracked)
        assertFalse(summary.hasMultipleSuites)
    }

    @Test
    fun calculateWeeklyScore_multipleDays_calculatesAverage() {
        val dailyResults = listOf(
            DailyScoreResult(
                date = LocalDate(2025, 1, 15),
                score = 20,
                suiteId = SuiteDefinitions.BALANCED_ID,
                suiteName = "Balanced",
                maxPossibleScore = 32
            ),
            DailyScoreResult(
                date = LocalDate(2025, 1, 16),
                score = 30,
                suiteId = SuiteDefinitions.BALANCED_ID,
                suiteName = "Balanced",
                maxPossibleScore = 32
            )
        )
        val summary = ScoreCalculator.calculateWeeklyScore(dailyResults)

        assertEquals(50, summary.totalScore)
        assertEquals(25.0, summary.averageScore, 0.001)
        assertEquals(2, summary.daysTracked)
    }

    @Test
    fun calculateWeeklyScore_multipleSuites_detectsCorrectly() {
        val dailyResults = listOf(
            DailyScoreResult(
                date = LocalDate(2025, 1, 15),
                score = 20,
                suiteId = SuiteDefinitions.BALANCED_ID,
                suiteName = "Balanced",
                maxPossibleScore = 32
            ),
            DailyScoreResult(
                date = LocalDate(2025, 1, 16),
                score = 25,
                suiteId = SuiteDefinitions.RACING_WEIGHT_ID,
                suiteName = "Racing Weight",
                maxPossibleScore = 28
            )
        )
        val summary = ScoreCalculator.calculateWeeklyScore(dailyResults)

        assertTrue(summary.hasMultipleSuites)
        assertEquals(2, summary.suitesUsed.size)
    }
}
