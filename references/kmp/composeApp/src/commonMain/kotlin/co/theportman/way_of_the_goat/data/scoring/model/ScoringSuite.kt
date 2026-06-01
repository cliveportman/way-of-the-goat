package co.theportman.way_of_the_goat.data.scoring.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Unique identifier for a scoring suite.
 */
@JvmInline
@Serializable
value class SuiteId(val value: String)

/**
 * Represents a complete scoring suite configuration.
 * Suites are defined as constants and are immutable.
 *
 * Each suite can have different food categories with different scoring rules,
 * supporting use cases like Racing Weight (standard), Weight Loss, Vegan, etc.
 */
@Serializable
data class ScoringSuite(
    val id: SuiteId,
    val name: String,
    val description: String,
    val categories: List<FoodCategory>,
    val version: Int = 1
) {
    /**
     * Categories that have positive maximum scores (healthy foods)
     */
    val healthyCategories: List<FoodCategory>
        get() = categories.filter { it.scoringRule.maxPossibleScore > 0 }

    /**
     * Categories that have zero or negative maximum scores (unhealthy foods)
     */
    val unhealthyCategories: List<FoodCategory>
        get() = categories.filter { it.scoringRule.maxPossibleScore <= 0 }

    /**
     * Maximum possible daily score for this suite (sum of all positive category maxes)
     */
    val maxPossibleDailyScore: Int
        get() = categories.sumOf { it.scoringRule.maxPossibleScore.coerceAtLeast(0) }

    /**
     * Minimum possible daily score (worst case - sum of all negative category mins)
     */
    val minPossibleDailyScore: Int
        get() = categories.sumOf { it.scoringRule.minPossibleScore.coerceAtMost(0) }

    /**
     * Find a category by its ID
     */
    fun getCategoryById(categoryId: CategoryId): FoodCategory? =
        categories.find { it.id == categoryId }
}
