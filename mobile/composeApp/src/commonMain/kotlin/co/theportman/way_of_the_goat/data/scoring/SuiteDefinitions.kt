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
    val BODY_FAT_REDUCTION_ID = SuiteId("body_fat_reduction")
    val HIGH_LOAD_ID = SuiteId("high_load")

    /**
     * Racing Weight suite - original scoring from the book.
     * Based on Matt Fitzgerald's Racing Weight methodology.
     */
    val RACING_WEIGHT = ScoringSuite(
        id = RACING_WEIGHT_ID,
        name = "Racing Weight",
        description = "Original scoring from the Racing Weight book",
        version = 1,
        categories = listOf(
            // === HEALTHY CATEGORIES (positive scoring) ===
            FoodCategory(
                id = CategoryId("fruit"),
                name = "Fruit",
                shortName = "Fruit",
                icon = null,
                displayOrder = 1,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 2, 2, 1, 0, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("veg"),
                name = "Vegetables",
                shortName = "Veg",
                icon = null,
                displayOrder = 2,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 2, 2, 1, 0, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("leanproteins"),
                name = "Lean Meats & Fish",
                shortName = "Protein",
                icon = null,
                displayOrder = 3,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 2, 1, 0, 0, -1)
                )
            ),
            FoodCategory(
                id = CategoryId("nuts"),
                name = "Nuts & Seeds",
                shortName = "Nuts",
                icon = null,
                displayOrder = 4,
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
                displayOrder = 5,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 2, 1, 0, 0, -1)
                )
            ),
            FoodCategory(
                id = CategoryId("dairy"),
                name = "Dairy",
                shortName = "Dairy",
                icon = null,
                displayOrder = 6,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(1, 1, 1, 0, -1, -2)
                )
            ),

            // === UNHEALTHY CATEGORIES (negative scoring) ===
            FoodCategory(
                id = CategoryId("refinedgrains"),
                name = "Refined Grains",
                shortName = "Refined",
                icon = null,
                displayOrder = 7,
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
                displayOrder = 8,
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
                    scorePerServing = listOf(-1, -1, -2, -2, -2, -2)
                )
            )
        )
    )

    /**
     * Body Fat Reduction suite.
     * Discourages higher fat foods and focuses on fewer portions.
     */
    val BODY_FAT_REDUCTION = ScoringSuite(
        id = BODY_FAT_REDUCTION_ID,
        name = "Body Fat Reduction",
        description = "Discourages higher fat foods, focuses on fewer portions",
        version = 1,
        categories = listOf(
            // === HEALTHY CATEGORIES (positive scoring) ===
            FoodCategory(
                id = CategoryId("fruit"),
                name = "Fruit",
                shortName = "Fruit",
                icon = null,
                displayOrder = 1,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 2, 2, 1, 0, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("veg"),
                name = "Vegetables",
                shortName = "Veg",
                icon = null,
                displayOrder = 2,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 2, 2, 1, 0, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("leanproteins"),
                name = "Lean Proteins",
                shortName = "Protein",
                icon = null,
                displayOrder = 3,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 2, 2, 0, -1, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("leandairy"),
                name = "Lean Dairy",
                shortName = "L.Dairy",
                icon = null,
                displayOrder = 4,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 2, 1, 0, -1, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("wholegrains"),
                name = "Whole Grains",
                shortName = "Grains",
                icon = null,
                displayOrder = 5,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 2, 1, 0, -1, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("nuts"),
                name = "Nuts & Seeds",
                shortName = "Nuts",
                icon = null,
                displayOrder = 6,
                scoringRule = ScoringRule(
                    targetServings = 1,
                    scorePerServing = listOf(2, 0, -2, -2, -2, -2)
                )
            ),

            // === UNHEALTHY CATEGORIES (negative scoring) ===
            FoodCategory(
                id = CategoryId("fattyproteins"),
                name = "Fatty Proteins",
                shortName = "Fatty",
                icon = null,
                displayOrder = 7,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(-1, -1, -2, -2, -2, -2)
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
                id = CategoryId("junkfoods"),
                name = "Junk Foods",
                shortName = "Junk",
                icon = null,
                displayOrder = 10,
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
                displayOrder = 11,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(0, -2, -2, -2, -2, -2)
                )
            )
        )
    )

    /**
     * High Mileage / Higher Intensity suite.
     * Encourages more portions of protein while covering fueling and micronutrient needs.
     */
    val HIGH_LOAD = ScoringSuite(
        id = HIGH_LOAD_ID,
        name = "High Mileage",
        description = "Encourages more protein for higher training loads",
        version = 1,
        categories = listOf(
            // === HEALTHY CATEGORIES (positive scoring) ===
            FoodCategory(
                id = CategoryId("fruit"),
                name = "Fruit",
                shortName = "Fruit",
                icon = null,
                displayOrder = 1,
                scoringRule = ScoringRule(
                    targetServings = 5,
                    scorePerServing = listOf(2, 1, 1, 1, 1, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("veg"),
                name = "Vegetables",
                shortName = "Veg",
                icon = null,
                displayOrder = 2,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 2, 2, 1, 0, 0)
                )
            ),
            FoodCategory(
                id = CategoryId("leanproteins"),
                name = "Lean Proteins",
                shortName = "Protein",
                icon = null,
                displayOrder = 3,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 2, 1, 1, 0, -1)
                )
            ),
            FoodCategory(
                id = CategoryId("leandairy"),
                name = "Lean Dairy",
                shortName = "L.Dairy",
                icon = null,
                displayOrder = 4,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 1, 1, 0, -1, -2)
                )
            ),
            FoodCategory(
                id = CategoryId("wholegrains"),
                name = "Whole Grains",
                shortName = "Grains",
                icon = null,
                displayOrder = 5,
                scoringRule = ScoringRule(
                    targetServings = 4,
                    scorePerServing = listOf(2, 1, 1, 1, 0, -1)
                )
            ),
            FoodCategory(
                id = CategoryId("nuts"),
                name = "Nuts & Seeds",
                shortName = "Nuts",
                icon = null,
                displayOrder = 6,
                scoringRule = ScoringRule(
                    targetServings = 3,
                    scorePerServing = listOf(2, 1, 1, 0, -1, -2)
                )
            ),

            // === UNHEALTHY CATEGORIES (negative scoring) ===
            FoodCategory(
                id = CategoryId("fattyproteins"),
                name = "Fatty Proteins",
                shortName = "Fatty",
                icon = null,
                displayOrder = 7,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(0, -1, -2, -2, -2, -2)
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
                    scorePerServing = listOf(0, -1, -2, -2, -2, -2)
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
                id = CategoryId("junkfoods"),
                name = "Junk Foods",
                shortName = "Junk",
                icon = null,
                displayOrder = 10,
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
                displayOrder = 11,
                scoringRule = ScoringRule(
                    targetServings = 0,
                    scorePerServing = listOf(0, -2, -2, -2, -2, -2)
                )
            )
        )
    )

    /**
     * All available suites in display order.
     */
    val allSuites: List<ScoringSuite> = listOf(
        RACING_WEIGHT,
        BODY_FAT_REDUCTION,
        HIGH_LOAD
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
