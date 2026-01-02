package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.FoodCategory
import co.theportman.way_of_the_goat.data.scoring.model.ScoringRule
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId

/**
 * Central repository of all scoring suite definitions.
 *
 * Suites are hardcoded for rapid iteration during development.
 * When modifying suites, increment the version number to support
 * future data migrations if category IDs change.
 */
object SuiteDefinitions {

    // Suite IDs as constants for type-safe references
    val RACING_WEIGHT_ID = SuiteId("racing_weight")
    val WEIGHT_LOSS_ID = SuiteId("weight_loss")

    /**
     * Standard Racing Weight suite based on Matt Fitzgerald methodology.
     * Scoring values migrated from references/core/constants.ts
     */
    val RACING_WEIGHT = ScoringSuite(
        id = RACING_WEIGHT_ID,
        name = "Racing Weight",
        description = "Standard diet quality scoring for endurance athletes",
        version = 1,
        categories = listOf(
            // === HEALTHY CATEGORIES (positive scoring) ===
            FoodCategory(
                id = CategoryId("veg"),
                name = "Vegetables",
                shortName = "Veg",
                icon = null,
                displayOrder = 1,
                scoringRule = ScoringRule(
                    targetServings = 6,
                    scorePerServing = listOf(2, 2, 2, 1, 0, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("fruit"),
                name = "Fruit",
                shortName = "Fruit",
                icon = null,
                displayOrder = 2,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 2, 2, 1, 0, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("nuts"),
                name = "Nuts & Seeds",
                shortName = "Nuts",
                icon = null,
                displayOrder = 3,
                scoringRule = ScoringRule(
                    targetServings = 2,
                    scorePerServing = listOf(2, 2, 1, 0, 0, -1)
                )
            ),
            FoodCategory(
                id = CategoryId("wholegrains"),
                name = "Whole Grains",
                shortName = "Grains",
                icon = null,
                displayOrder = 4,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 2, 1, 0, 0, -1)
                )
            ),
            FoodCategory(
                id = CategoryId("dairy"),
                name = "Low-Fat Dairy",
                shortName = "Dairy",
                icon = null,
                displayOrder = 5,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 1, 1, 0, -1, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("leanproteins"),
                name = "Lean Proteins",
                shortName = "Protein",
                icon = null,
                displayOrder = 6,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 1, 1, 0, -1, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("beverages"),
                name = "Beverages",
                shortName = "Drinks",
                icon = null,
                displayOrder = 7,
                scoringRule = ScoringRule(
                    targetServings = 2,
                    scorePerServing = listOf(1, 1, 0, 0, 0, 0)
                )
            ),

            // === UNHEALTHY CATEGORIES (negative scoring) ===
            FoodCategory(
                id = CategoryId("refinedgrains"),
                name = "Refined Grains",
                shortName = "Refined",
                icon = null,
                displayOrder = 8,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-1, -1, -2, -2, -2, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("sweets"),
                name = "Sweets",
                shortName = "Sweets",
                icon = null,
                displayOrder = 9,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-2, -2, -2, -2, -2, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("fattyproteins"),
                name = "Fatty Proteins",
                shortName = "Fatty",
                icon = null,
                displayOrder = 10,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-2, -2, -2, -2, -2, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("friedfoods"),
                name = "Fried Foods",
                shortName = "Fried",
                icon = null,
                displayOrder = 11,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-2, -2, -2, -2, -2, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("alcohol"),
                name = "Alcohol",
                shortName = "Alcohol",
                icon = null,
                displayOrder = 12,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(0, -2, -2, -2, -2, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("other"),
                name = "Other",
                shortName = "Other",
                icon = null,
                displayOrder = 13,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-1, -2, -2, -2, -2, -2)
                )
            )
        )
    )

    /**
     * Weight Loss suite with stricter scoring.
     *
     * PLACEHOLDER: Values to be tuned through experimentation.
     * Currently uses same categories as Racing Weight with modified scoring.
     */
    val WEIGHT_LOSS = ScoringSuite(
        id = WEIGHT_LOSS_ID,
        name = "Weight Loss",
        description = "Stricter scoring for active weight loss phases",
        version = 1,
        categories = listOf(
            // Placeholder - copies Racing Weight structure
            // TODO: Tune scoring values for weight loss goals
            FoodCategory(
                id = CategoryId("veg"),
                name = "Vegetables",
                shortName = "Veg",
                icon = null,
                displayOrder = 1,
                scoringRule = ScoringRule(
                    targetServings = 8,
                    scorePerServing = listOf(2, 2, 2, 2, 1, 1, 0, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("fruit"),
                name = "Fruit",
                shortName = "Fruit",
                icon = null,
                displayOrder = 2,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 2, 1, 0, -1, -1)
                )
            ),
            FoodCategory(
                id = CategoryId("nuts"),
                name = "Nuts & Seeds",
                shortName = "Nuts",
                icon = null,
                displayOrder = 3,
                scoringRule = ScoringRule(
                    targetServings = 1,
                    scorePerServing = listOf(2, 1, 0, -1, -1, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("wholegrains"),
                name = "Whole Grains",
                shortName = "Grains",
                icon = null,
                displayOrder = 4,
                scoringRule = ScoringRule(
                    targetServings = 2,
                    scorePerServing = listOf(2, 1, 0, -1, -1, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("dairy"),
                name = "Low-Fat Dairy",
                shortName = "Dairy",
                icon = null,
                displayOrder = 5,
                scoringRule = ScoringRule(
                    targetServings = 2,
                    scorePerServing = listOf(2, 1, 0, -1, -2, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("leanproteins"),
                name = "Lean Proteins",
                shortName = "Protein",
                icon = null,
                displayOrder = 6,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 2, 1, 1, 0, -1)
                )
            ),
            FoodCategory(
                id = CategoryId("beverages"),
                name = "Beverages",
                shortName = "Drinks",
                icon = null,
                displayOrder = 7,
                scoringRule = ScoringRule(
                    targetServings = 2,
                    scorePerServing = listOf(1, 1, 0, 0, 0, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("refinedgrains"),
                name = "Refined Grains",
                shortName = "Refined",
                icon = null,
                displayOrder = 8,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-2, -2, -2, -2, -2, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("sweets"),
                name = "Sweets",
                shortName = "Sweets",
                icon = null,
                displayOrder = 9,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-3, -3, -3, -3, -3, -3)
                )
            ),
            FoodCategory(
                id = CategoryId("fattyproteins"),
                name = "Fatty Proteins",
                shortName = "Fatty",
                icon = null,
                displayOrder = 10,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-2, -2, -2, -2, -2, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("friedfoods"),
                name = "Fried Foods",
                shortName = "Fried",
                icon = null,
                displayOrder = 11,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-3, -3, -3, -3, -3, -3)
                )
            ),
            FoodCategory(
                id = CategoryId("alcohol"),
                name = "Alcohol",
                shortName = "Alcohol",
                icon = null,
                displayOrder = 12,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-1, -2, -2, -2, -2, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("other"),
                name = "Other",
                shortName = "Other",
                icon = null,
                displayOrder = 13,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-2, -2, -2, -2, -2, -2)
                )
            )
        )
    )

    /**
     * All available suites in display order.
     */
    val allSuites: List<ScoringSuite> = listOf(
        RACING_WEIGHT,
        WEIGHT_LOSS
    )

    /**
     * Get a suite by its ID.
     */
    fun getSuiteById(id: SuiteId): ScoringSuite? = allSuites.find { it.id == id }

    /**
     * Default suite for new users.
     */
    val defaultSuite: ScoringSuite = RACING_WEIGHT
}
