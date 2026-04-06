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
    fun `given empty servings when calculateDailyScore then returns zero`() {
        val dailyServings = createDailyServings(emptyMap())
        val score = ScoreCalculator.calculateDailyScore(dailyServings, balancedSuite)

        assertEquals(0, score)
    }

    @Test
    fun `given single category when calculateDailyScore then calculates correctly`() {
        // 3 servings of fruit: 2 + 2 + 2 = 6
        val dailyServings = createDailyServings(mapOf("fruit" to 3))
        val score = ScoreCalculator.calculateDailyScore(dailyServings, balancedSuite)

        assertEquals(6, score)
    }

    @Test
    fun `given multiple categories when calculateDailyScore then sums correctly`() {
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
    fun `given unhealthy categories when calculateDailyScore then subtracts correctly`() {
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
    fun `given mismatched suite when calculateScore then returns null`() {
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
    fun `given mixed categories when calculateHealthyScore then only counts healthy`() {
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
    fun `given mixed categories when calculateUnhealthyScore then only counts unhealthy`() {
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
    fun `given servings when calculateScoreBreakdown then returns correct breakdown`() {
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
    fun `given partial servings when targetProgress then calculates correctly`() {
        val dailyServings = createDailyServings(mapOf("fruit" to 2))
        val breakdown = ScoreCalculator.calculateScoreBreakdown(dailyServings, balancedSuite)

        val fruitBreakdown = breakdown.find { it.category.id.value == "fruit" }
        assertNotNull(fruitBreakdown)

        // 2 servings / 4 target = 0.5
        assertEquals(0.5f, fruitBreakdown.targetProgress, 0.001f)
    }

    @Test
    fun `given target reached when targetMet then returns true`() {
        val dailyServings = createDailyServings(mapOf("fruit" to 4))
        val breakdown = ScoreCalculator.calculateScoreBreakdown(dailyServings, balancedSuite)

        val fruitBreakdown = breakdown.find { it.category.id.value == "fruit" }
        assertNotNull(fruitBreakdown)
        assertTrue(fruitBreakdown.targetMet)
    }

    @Test
    fun `given unhealthy category when zero servings then targetMet is true`() {
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
    fun `given servings when calculateTotalsForDisplay then formats correctly`() {
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
    fun `given empty servings when calculateTotalsForDisplay then returns empty state`() {
        val dailyServings = createDailyServings(emptyMap())
        val totals = ScoreCalculator.calculateTotalsForDisplay(dailyServings, balancedSuite)

        assertEquals("---", totals.healthy)
        assertNull(totals.unhealthy)
        assertEquals("---", totals.total)
        assertEquals(0, totals.portions)
        assertFalse(totals.hasData)
    }

    @Test
    fun `given servings when calculateTotalsForMaths then returns integer values`() {
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
    fun `given empty list when calculateWeeklyScore then returns zeros`() {
        val summary = ScoreCalculator.calculateWeeklyScore(emptyList())

        assertEquals(0, summary.totalScore)
        assertEquals(0.0, summary.averageScore, 0.001)
        assertEquals(0, summary.daysTracked)
        assertTrue(summary.suitesUsed.isEmpty())
    }

    @Test
    fun `given single day when calculateWeeklyScore then calculates correctly`() {
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
    fun `given multiple days when calculateWeeklyScore then calculates average`() {
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
    fun `given multiple suites when calculateWeeklyScore then detects correctly`() {
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
