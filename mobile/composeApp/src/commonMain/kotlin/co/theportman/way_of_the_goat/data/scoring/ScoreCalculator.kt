package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.FoodCategory
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlinx.datetime.LocalDate

/**
 * Utility object for score calculations.
 *
 * Provides pure functions for calculating scores based on suite configurations.
 * Migrated from references/core/helpers.ts scoring logic.
 */
object ScoreCalculator {

    /**
     * Calculate total score for a daily servings record using its associated suite.
     * Returns null if the suite is unknown.
     */
    fun calculateDailyScore(dailyServings: DailyServings): Int? {
        val suite = SuiteDefinitions.getSuiteById(dailyServings.suiteId) ?: return null
        return dailyServings.calculateScore(suite)
    }

    /**
     * Calculate score for daily servings using a specific suite.
     */
    fun calculateDailyScore(dailyServings: DailyServings, suite: ScoringSuite): Int {
        return suite.categories.sumOf { category ->
            val servingCount = dailyServings.getServings(category.id)
            category.scoringRule.calculateScore(servingCount)
        }
    }

    /**
     * Calculate healthy categories score only.
     */
    fun calculateHealthyScore(dailyServings: DailyServings, suite: ScoringSuite): Int {
        return suite.healthyCategories.sumOf { category ->
            val servingCount = dailyServings.getServings(category.id)
            category.scoringRule.calculateScore(servingCount)
        }
    }

    /**
     * Calculate unhealthy categories score only.
     */
    fun calculateUnhealthyScore(dailyServings: DailyServings, suite: ScoringSuite): Int {
        return suite.unhealthyCategories.sumOf { category ->
            val servingCount = dailyServings.getServings(category.id)
            category.scoringRule.calculateScore(servingCount)
        }
    }

    /**
     * Calculate detailed score breakdown by category.
     */
    fun calculateScoreBreakdown(
        dailyServings: DailyServings,
        suite: ScoringSuite
    ): List<CategoryScoreBreakdown> {
        return suite.categories.map { category ->
            val servingCount = dailyServings.getServings(category.id)
            val score = category.scoringRule.calculateScore(servingCount)

            CategoryScoreBreakdown(
                category = category,
                servingCount = servingCount,
                score = score,
                maxPossibleScore = category.scoringRule.maxPossibleScore,
                targetServings = category.scoringRule.targetServings
            )
        }
    }

    /**
     * Calculate aggregate totals for display.
     * Returns formatted strings suitable for UI display.
     */
    fun calculateTotalsForDisplay(
        dailyServings: DailyServings,
        suite: ScoringSuite
    ): DailyTotalsForDisplay {
        val healthyScore = calculateHealthyScore(dailyServings, suite)
        val unhealthyScore = calculateUnhealthyScore(dailyServings, suite)
        val totalScore = healthyScore + unhealthyScore
        val totalServings = dailyServings.totalServings

        return if (dailyServings.hasAnyServings) {
            DailyTotalsForDisplay(
                healthy = if (healthyScore >= 0) "+$healthyScore" else "$healthyScore",
                unhealthy = unhealthyScore,
                total = if (totalScore >= 0) "+$totalScore" else "$totalScore",
                portions = totalServings
            )
        } else {
            DailyTotalsForDisplay.empty()
        }
    }

    /**
     * Calculate aggregate totals for calculations/charts.
     */
    fun calculateTotalsForMaths(
        dailyServings: DailyServings,
        suite: ScoringSuite
    ): DailyTotalsForMaths {
        return DailyTotalsForMaths(
            healthy = calculateHealthyScore(dailyServings, suite),
            unhealthy = calculateUnhealthyScore(dailyServings, suite),
            total = calculateDailyScore(dailyServings, suite),
            portions = dailyServings.totalServings
        )
    }

    /**
     * Calculate weekly aggregate score from a list of daily scores.
     */
    fun calculateWeeklyScore(dailyScores: List<DailyScoreResult>): WeeklyScoreSummary {
        val totalScore = dailyScores.sumOf { it.score }
        val averageScore = if (dailyScores.isNotEmpty()) {
            totalScore.toDouble() / dailyScores.size
        } else {
            0.0
        }

        val suitesUsed = dailyScores.map { it.suiteId }.toSet()

        return WeeklyScoreSummary(
            totalScore = totalScore,
            averageScore = averageScore,
            daysTracked = dailyScores.size,
            suitesUsed = suitesUsed.toList()
        )
    }
}

/**
 * Breakdown of score for a single category.
 */
data class CategoryScoreBreakdown(
    val category: FoodCategory,
    val servingCount: Int,
    val score: Int,
    val maxPossibleScore: Int,
    val targetServings: Int
) {
    /**
     * Progress toward target as a percentage (0.0 to 1.0+)
     */
    val targetProgress: Float
        get() = if (targetServings > 0) {
            servingCount.toFloat() / targetServings
        } else {
            if (servingCount == 0) 1f else 0f
        }

    /**
     * Whether the target has been met.
     */
    val targetMet: Boolean
        get() = if (targetServings > 0) {
            servingCount >= targetServings
        } else {
            servingCount == 0
        }
}

/**
 * Daily totals formatted for display.
 */
data class DailyTotalsForDisplay(
    val healthy: String,
    val unhealthy: Int?,
    val total: String,
    val portions: Int
) {
    companion object {
        private const val NO_DATA = "---"

        fun empty() = DailyTotalsForDisplay(
            healthy = NO_DATA,
            unhealthy = null,
            total = NO_DATA,
            portions = 0
        )
    }

    val hasData: Boolean
        get() = healthy != "---"
}

/**
 * Daily totals for calculations.
 */
data class DailyTotalsForMaths(
    val healthy: Int,
    val unhealthy: Int,
    val total: Int,
    val portions: Int
)

/**
 * Result of calculating a daily score.
 */
data class DailyScoreResult(
    val date: LocalDate,
    val score: Int,
    val suiteId: SuiteId,
    val suiteName: String,
    val maxPossibleScore: Int
)

/**
 * Weekly score summary.
 */
data class WeeklyScoreSummary(
    val totalScore: Int,
    val averageScore: Double,
    val daysTracked: Int,
    val suitesUsed: List<SuiteId>
) {
    /**
     * Whether multiple suites were used in this week.
     */
    val hasMultipleSuites: Boolean
        get() = suitesUsed.size > 1
}
