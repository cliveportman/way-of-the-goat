package co.theportman.way_of_the_goat.data.scoring.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Represents a single day's food servings record.
 *
 * Each record is tied to the suite that was active when it was recorded,
 * enabling historical accuracy when viewing past data even if the user
 * has since switched to a different suite.
 */
@Serializable
data class DailyServings(
    val id: Long = 0,
    val date: LocalDate,
    val suiteId: SuiteId,
    val servings: Map<CategoryId, Int>,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    /**
     * Calculate total score for this day using the provided suite configuration.
     * Returns null if the suite doesn't match this record's suite.
     */
    fun calculateScore(suite: ScoringSuite): Int? {
        if (suite.id != suiteId) return null

        return suite.categories.sumOf { category ->
            val servingCount = servings[category.id] ?: 0
            category.scoringRule.calculateScore(servingCount)
        }
    }

    /**
     * Get servings count for a specific category, defaulting to 0.
     */
    fun getServings(categoryId: CategoryId): Int = servings[categoryId] ?: 0

    /**
     * Total number of servings across all categories.
     */
    val totalServings: Int
        get() = servings.values.sum()

    /**
     * Check if any servings have been recorded.
     */
    val hasAnyServings: Boolean
        get() = servings.values.any { it > 0 }

    companion object {
        /**
         * Create an empty record for a date with the given suite.
         */
        fun empty(date: LocalDate, suiteId: SuiteId): DailyServings {
            val now = Clock.System.now().toEpochMilliseconds()
            return DailyServings(
                date = date,
                suiteId = suiteId,
                servings = emptyMap(),
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Mutable builder for constructing/editing daily servings.
 * Used in ViewModels for tracking changes before saving.
 */
class DailyServingsBuilder(
    val date: LocalDate,
    val suiteId: SuiteId,
    private val servings: MutableMap<CategoryId, Int> = mutableMapOf(),
    private val existingId: Long = 0,
    private val existingCreatedAt: Long = 0
) {
    /**
     * Set the servings count for a category.
     */
    fun setServings(categoryId: CategoryId, count: Int) {
        if (count > 0) {
            servings[categoryId] = count
        } else {
            servings.remove(categoryId)
        }
    }

    /**
     * Get the current servings count for a category.
     */
    fun getServings(categoryId: CategoryId): Int = servings[categoryId] ?: 0

    /**
     * Increment servings for a category by 1.
     */
    fun incrementServings(categoryId: CategoryId, maxServings: Int = Int.MAX_VALUE) {
        val current = servings[categoryId] ?: 0
        if (current < maxServings) {
            servings[categoryId] = current + 1
        }
    }

    /**
     * Decrement servings for a category by 1 (minimum 0).
     */
    fun decrementServings(categoryId: CategoryId) {
        val current = servings[categoryId] ?: 0
        if (current > 1) {
            servings[categoryId] = current - 1
        } else {
            servings.remove(categoryId)
        }
    }

    /**
     * Build the immutable DailyServings record.
     */
    fun build(): DailyServings {
        val now = Clock.System.now().toEpochMilliseconds()
        return DailyServings(
            id = existingId,
            date = date,
            suiteId = suiteId,
            servings = servings.toMap(),
            createdAt = if (existingCreatedAt > 0) existingCreatedAt else now,
            updatedAt = now
        )
    }

    companion object {
        /**
         * Create a builder from an existing DailyServings record.
         */
        fun fromExisting(dailyServings: DailyServings): DailyServingsBuilder {
            return DailyServingsBuilder(
                date = dailyServings.date,
                suiteId = dailyServings.suiteId,
                servings = dailyServings.servings.toMutableMap(),
                existingId = dailyServings.id,
                existingCreatedAt = dailyServings.createdAt
            )
        }

        /**
         * Create a new builder for a date with the given suite.
         */
        fun newForDate(date: LocalDate, suiteId: SuiteId): DailyServingsBuilder {
            return DailyServingsBuilder(
                date = date,
                suiteId = suiteId
            )
        }
    }
}
