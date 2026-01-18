package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SuiteDefinitionsTest {

    @Test
    fun allSuites_containsFourSuites() {
        assertEquals(4, SuiteDefinitions.allSuites.size)
    }

    @Test
    fun allSuites_containsExpectedSuites() {
        val suiteNames = SuiteDefinitions.allSuites.map { it.name }

        assertTrue(suiteNames.contains("Balanced"))
        assertTrue(suiteNames.contains("Racing Weight"))
        assertTrue(suiteNames.contains("Body Composition"))
        assertTrue(suiteNames.contains("High Mileage"))
    }

    @Test
    fun defaultSuite_isBalanced() {
        assertEquals(SuiteDefinitions.BALANCED_ID, SuiteDefinitions.defaultSuite.id)
        assertEquals("Balanced", SuiteDefinitions.defaultSuite.name)
    }

    @Test
    fun getSuiteById_existingSuite_returnsSuite() {
        val balanced = SuiteDefinitions.getSuiteById(SuiteDefinitions.BALANCED_ID)
        assertNotNull(balanced)
        assertEquals("Balanced", balanced.name)

        val racingWeight = SuiteDefinitions.getSuiteById(SuiteDefinitions.RACING_WEIGHT_ID)
        assertNotNull(racingWeight)
        assertEquals("Racing Weight", racingWeight.name)
    }

    @Test
    fun getSuiteById_unknownSuite_returnsNull() {
        val result = SuiteDefinitions.getSuiteById(SuiteId("nonexistent"))
        assertNull(result)
    }

    @Test
    fun balancedSuite_hasCorrectCategoryCount() {
        // Balanced: 6 healthy + 5 unhealthy = 11 categories
        assertEquals(11, SuiteDefinitions.BALANCED.categories.size)
    }

    @Test
    fun balancedSuite_hasCorrectHealthyCategories() {
        val healthyCategories = SuiteDefinitions.BALANCED.healthyCategories

        assertEquals(6, healthyCategories.size)

        val healthyNames = healthyCategories.map { it.name }
        assertTrue(healthyNames.contains("Fruit"))
        assertTrue(healthyNames.contains("Vegetables"))
        assertTrue(healthyNames.contains("Lean proteins"))
        assertTrue(healthyNames.contains("Dairy"))
        assertTrue(healthyNames.contains("Whole grains"))
        assertTrue(healthyNames.contains("Nuts + seeds"))
    }

    @Test
    fun balancedSuite_hasCorrectUnhealthyCategories() {
        val unhealthyCategories = SuiteDefinitions.BALANCED.unhealthyCategories

        assertEquals(5, unhealthyCategories.size)

        val unhealthyNames = unhealthyCategories.map { it.name }
        assertTrue(unhealthyNames.contains("Fatty meats"))
        assertTrue(unhealthyNames.contains("Refined grains"))
        assertTrue(unhealthyNames.contains("Sweets"))
        assertTrue(unhealthyNames.contains("Junk foods"))
        assertTrue(unhealthyNames.contains("Alcohol"))
    }

    @Test
    fun racingWeightSuite_hasCorrectCategoryCount() {
        // Racing Weight: 6 healthy + 4 unhealthy = 10 categories
        assertEquals(10, SuiteDefinitions.RACING_WEIGHT.categories.size)
    }

    @Test
    fun allSuites_haveUniqueIds() {
        val ids = SuiteDefinitions.allSuites.map { it.id }
        val uniqueIds = ids.toSet()

        assertEquals(ids.size, uniqueIds.size)
    }

    @Test
    fun allSuites_haveNonEmptyCategories() {
        for (suite in SuiteDefinitions.allSuites) {
            assertTrue(suite.categories.isNotEmpty(), "Suite ${suite.name} should have categories")
        }
    }

    @Test
    fun allSuites_categoriesHaveUniqueDisplayOrder() {
        for (suite in SuiteDefinitions.allSuites) {
            val displayOrders = suite.categories.map { it.displayOrder }
            val uniqueOrders = displayOrders.toSet()

            assertEquals(
                displayOrders.size,
                uniqueOrders.size,
                "Suite ${suite.name} should have unique display orders for categories"
            )
        }
    }

    @Test
    fun maxPossibleDailyScore_isPositive() {
        for (suite in SuiteDefinitions.allSuites) {
            assertTrue(
                suite.maxPossibleDailyScore > 0,
                "Suite ${suite.name} should have positive max score"
            )
        }
    }

    @Test
    fun minPossibleDailyScore_isNegativeOrZero() {
        for (suite in SuiteDefinitions.allSuites) {
            assertTrue(
                suite.minPossibleDailyScore <= 0,
                "Suite ${suite.name} should have non-positive min score"
            )
        }
    }

    @Test
    fun balancedSuite_fruitCategory_hasCorrectTargetServings() {
        val fruitCategory = SuiteDefinitions.BALANCED.categories.find { it.name == "Fruit" }
        assertNotNull(fruitCategory)
        assertEquals(4, fruitCategory.scoringRule.targetServings)
    }

    @Test
    fun balancedSuite_sweetsCategory_hasZeroTargetServings() {
        val sweetsCategory = SuiteDefinitions.BALANCED.categories.find { it.name == "Sweets" }
        assertNotNull(sweetsCategory)
        assertEquals(0, sweetsCategory.scoringRule.targetServings)
    }
}
