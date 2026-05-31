package co.theportman.way_of_the_goat.data.scoring.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Unique identifier for a food category within a suite.
 */
@JvmInline
@Serializable
value class CategoryId(val value: String)

/**
 * Represents a food category within a scoring suite.
 *
 * Each category has its own scoring rules that define how servings
 * translate to points. Different suites can have different categories
 * or the same categories with different scoring rules.
 */
@Serializable
data class FoodCategory(
    val id: CategoryId,
    val name: String,
    val shortName: String,
    val icon: String? = null,
    val scoringRule: ScoringRule,
    val displayOrder: Int
)

/**
 * Defines how servings in a category translate to points.
 *
 * The [scorePerServing] list contains the points awarded for each serving:
 * - Index 0 = points for 1st serving
 * - Index 1 = points for 2nd serving
 * - etc.
 *
 * Example: `[2, 2, 2, 1, 0, 0]` means:
 * - 1st serving: +2 points
 * - 2nd serving: +2 points
 * - 3rd serving: +2 points
 * - 4th serving: +1 point
 * - 5th-6th servings: 0 points
 * - Total for 3 servings: 6 points
 */
@Serializable
data class ScoringRule(
    val targetServings: Int,
    val scorePerServing: List<Int>,
    val maxTrackedServings: Int = scorePerServing.size
) {
    /**
     * Calculate total score for a given number of servings.
     */
    fun calculateScore(servings: Int): Int {
        if (servings <= 0) return 0
        val effectiveServings = servings.coerceAtMost(scorePerServing.size)
        return scorePerServing.take(effectiveServings).sum()
    }

    /**
     * Maximum possible score (sum of all positive values in scorePerServing)
     */
    val maxPossibleScore: Int
        get() = scorePerServing.filter { it > 0 }.sum()

    /**
     * Minimum possible score (sum of all negative values in scorePerServing)
     */
    val minPossibleScore: Int
        get() = scorePerServing.filter { it < 0 }.sum()

    /**
     * Get the score for a specific serving number (1-indexed).
     * Returns 0 if the serving number is out of range.
     */
    fun getScoreForServing(servingNumber: Int): Int {
        if (servingNumber <= 0 || servingNumber > scorePerServing.size) return 0
        return scorePerServing[servingNumber - 1]
    }
}
