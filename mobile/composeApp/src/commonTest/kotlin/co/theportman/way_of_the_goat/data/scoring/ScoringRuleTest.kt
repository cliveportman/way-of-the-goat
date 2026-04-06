package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.scoring.model.ScoringRule
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoringRuleTest {

    @Test
    fun `given zero servings when calculateScore then returns zero`() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        assertEquals(0, rule.calculateScore(0))
    }

    @Test
    fun `given negative servings when calculateScore then returns zero`() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        assertEquals(0, rule.calculateScore(-1))
        assertEquals(0, rule.calculateScore(-100))
    }

    @Test
    fun `given one serving when calculateScore then returns first score`() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        assertEquals(2, rule.calculateScore(1))
    }

    @Test
    fun `given target number of servings when calculateScore then returns cumulative score`() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        // 4 servings: 2 + 2 + 2 + 1 = 7
        assertEquals(7, rule.calculateScore(4))
    }

    @Test
    fun `given excess servings when calculateScore then caps at max tracked`() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        // All 6 values: 2 + 2 + 2 + 1 + 0 + 0 = 7
        assertEquals(7, rule.calculateScore(6))
        // Beyond max tracked: still 7
        assertEquals(7, rule.calculateScore(10))
        assertEquals(7, rule.calculateScore(100))
    }

    @Test
    fun `given partial servings when calculateScore then returns cumulative score`() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        // 2 servings: 2 + 2 = 4
        assertEquals(4, rule.calculateScore(2))
        // 3 servings: 2 + 2 + 2 = 6
        assertEquals(6, rule.calculateScore(3))
    }

    @Test
    fun `given unhealthy category rule when calculateScore then returns negative score`() {
        // Sweets category scoring rule
        val rule = ScoringRule(
            targetServings = 0,
            scorePerServing = listOf(-2, -2, -2, -2, -2, -2)
        )

        assertEquals(0, rule.calculateScore(0))
        assertEquals(-2, rule.calculateScore(1))
        assertEquals(-4, rule.calculateScore(2))
        assertEquals(-12, rule.calculateScore(6))
    }

    @Test
    fun `given alcohol category rule when calculateScore then returns zero for first serving`() {
        // Alcohol category: first drink is "free"
        val rule = ScoringRule(
            targetServings = 0,
            scorePerServing = listOf(0, -1, -2, -2, -2, -2)
        )

        assertEquals(0, rule.calculateScore(1))
        assertEquals(-1, rule.calculateScore(2))
        assertEquals(-3, rule.calculateScore(3))
    }

    @Test
    fun `given mixed score per serving when maxPossibleScore then returns only positive sum`() {
        val rule = ScoringRule(
            targetServings = 3,
            scorePerServing = listOf(2, 2, 1, 0, -1, -2)
        )

        // Only sum positive: 2 + 2 + 1 = 5
        assertEquals(5, rule.maxPossibleScore)
    }

    @Test
    fun `given unhealthy category rule when maxPossibleScore then returns zero`() {
        val rule = ScoringRule(
            targetServings = 0,
            scorePerServing = listOf(-2, -2, -2, -2, -2, -2)
        )

        assertEquals(0, rule.maxPossibleScore)
    }

    @Test
    fun `given mixed score per serving when minPossibleScore then returns only negative sum`() {
        val rule = ScoringRule(
            targetServings = 3,
            scorePerServing = listOf(2, 2, 1, 0, -1, -2)
        )

        // Only sum negative: -1 + -2 = -3
        assertEquals(-3, rule.minPossibleScore)
    }

    @Test
    fun `given valid serving index when getScoreForServing then returns correct value`() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        assertEquals(2, rule.getScoreForServing(1))
        assertEquals(2, rule.getScoreForServing(2))
        assertEquals(2, rule.getScoreForServing(3))
        assertEquals(1, rule.getScoreForServing(4))
        assertEquals(0, rule.getScoreForServing(5))
        assertEquals(0, rule.getScoreForServing(6))
    }

    @Test
    fun `given out of range serving index when getScoreForServing then returns zero`() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        assertEquals(0, rule.getScoreForServing(0))
        assertEquals(0, rule.getScoreForServing(-1))
        assertEquals(0, rule.getScoreForServing(7))
        assertEquals(0, rule.getScoreForServing(100))
    }
}
