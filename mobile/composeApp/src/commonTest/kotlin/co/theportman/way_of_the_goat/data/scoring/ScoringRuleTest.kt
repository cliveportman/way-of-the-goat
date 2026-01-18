package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.scoring.model.ScoringRule
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoringRuleTest {

    @Test
    fun calculateScore_withZeroServings_returnsZero() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        assertEquals(0, rule.calculateScore(0))
    }

    @Test
    fun calculateScore_withNegativeServings_returnsZero() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        assertEquals(0, rule.calculateScore(-1))
        assertEquals(0, rule.calculateScore(-100))
    }

    @Test
    fun calculateScore_withOneServing_returnsFirstScore() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        assertEquals(2, rule.calculateScore(1))
    }

    @Test
    fun calculateScore_withTargetServings_returnsCumulativeScore() {
        val rule = ScoringRule(
            targetServings = 4,
            scorePerServing = listOf(2, 2, 2, 1, 0, 0)
        )

        // 4 servings: 2 + 2 + 2 + 1 = 7
        assertEquals(7, rule.calculateScore(4))
    }

    @Test
    fun calculateScore_withExcessServings_capsAtMaxTracked() {
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
    fun calculateScore_withPartialServings_returnsCumulativeScore() {
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
    fun calculateScore_unhealthyCategory_returnsNegativeScore() {
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
    fun calculateScore_alcoholCategory_zeroForFirstServing() {
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
    fun maxPossibleScore_returnsOnlyPositiveSum() {
        val rule = ScoringRule(
            targetServings = 3,
            scorePerServing = listOf(2, 2, 1, 0, -1, -2)
        )

        // Only sum positive: 2 + 2 + 1 = 5
        assertEquals(5, rule.maxPossibleScore)
    }

    @Test
    fun maxPossibleScore_unhealthyCategory_returnsZero() {
        val rule = ScoringRule(
            targetServings = 0,
            scorePerServing = listOf(-2, -2, -2, -2, -2, -2)
        )

        assertEquals(0, rule.maxPossibleScore)
    }

    @Test
    fun minPossibleScore_returnsOnlyNegativeSum() {
        val rule = ScoringRule(
            targetServings = 3,
            scorePerServing = listOf(2, 2, 1, 0, -1, -2)
        )

        // Only sum negative: -1 + -2 = -3
        assertEquals(-3, rule.minPossibleScore)
    }

    @Test
    fun getScoreForServing_returnsCorrectValue() {
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
    fun getScoreForServing_outOfRange_returnsZero() {
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
