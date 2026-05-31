package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SuiteDefinitionsTest {

    @Test
    fun `given suite definitions when allSuites then contains four suites`() {
        assertEquals(4, SuiteDefinitions.allSuites.size)
    }

    @Test
    fun `given suite definitions when allSuites then contains all expected suite names`() {
        val suiteNames = SuiteDefinitions.allSuites.map { it.name }

        assertTrue(suiteNames.contains("Balanced"))
        assertTrue(suiteNames.contains("Racing Weight"))
        assertTrue(suiteNames.contains("Body Composition"))
        assertTrue(suiteNames.contains("High Mileage"))
    }

    @Test
    fun `given suite definitions when defaultSuite then is the balanced suite`() {
        assertEquals(SuiteDefinitions.BALANCED_ID, SuiteDefinitions.defaultSuite.id)
        assertEquals("Balanced", SuiteDefinitions.defaultSuite.name)
    }

    @Test
    fun `given existing suite id when getSuiteById then returns that suite`() {
        val balanced = SuiteDefinitions.getSuiteById(SuiteDefinitions.BALANCED_ID)
        assertNotNull(balanced)
        assertEquals("Balanced", balanced.name)

        val racingWeight = SuiteDefinitions.getSuiteById(SuiteDefinitions.RACING_WEIGHT_ID)
        assertNotNull(racingWeight)
        assertEquals("Racing Weight", racingWeight.name)
    }

    @Test
    fun `given unknown suite id when getSuiteById then returns null`() {
        val result = SuiteDefinitions.getSuiteById(SuiteId("nonexistent"))
        assertNull(result)
    }

    @Test
    fun `given balanced suite when categories then has correct category count`() {
        // Balanced: 6 healthy + 4 unhealthy = 10 categories
        assertEquals(10, SuiteDefinitions.BALANCED.categories.size)
    }

    @Test
    fun `given balanced suite when healthyCategories then contains all expected categories`() {
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
    fun `given balanced suite when unhealthyCategories then contains all expected categories`() {
        val unhealthyCategories = SuiteDefinitions.BALANCED.unhealthyCategories

        assertEquals(4, unhealthyCategories.size)

        val unhealthyNames = unhealthyCategories.map { it.name }
        assertTrue(unhealthyNames.contains("Fatty meats"))
        assertTrue(unhealthyNames.contains("Refined grains"))
        assertTrue(unhealthyNames.contains("Sweets"))
        assertTrue(unhealthyNames.contains("Junk foods"))
    }

    @Test
    fun `given racing weight suite when categories then has correct category count`() {
        // Racing Weight: 6 healthy + 4 unhealthy = 10 categories
        assertEquals(10, SuiteDefinitions.RACING_WEIGHT.categories.size)
    }

    @Test
    fun `given all suites when ids then are all unique`() {
        val ids = SuiteDefinitions.allSuites.map { it.id }
        val uniqueIds = ids.toSet()

        assertEquals(ids.size, uniqueIds.size)
    }

    @Test
    fun `given all suites when categories then are all non-empty`() {
        for (suite in SuiteDefinitions.allSuites) {
            assertTrue(suite.categories.isNotEmpty(), "Suite ${suite.name} should have categories")
        }
    }

    @Test
    fun `given all suites when displayOrder then is unique within each suite`() {
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
    fun `given all suites when maxPossibleDailyScore then is positive`() {
        for (suite in SuiteDefinitions.allSuites) {
            assertTrue(
                suite.maxPossibleDailyScore > 0,
                "Suite ${suite.name} should have positive max score"
            )
        }
    }

    @Test
    fun `given all suites when minPossibleDailyScore then is negative or zero`() {
        for (suite in SuiteDefinitions.allSuites) {
            assertTrue(
                suite.minPossibleDailyScore <= 0,
                "Suite ${suite.name} should have non-positive min score"
            )
        }
    }

    @Test
    fun `given balanced suite fruit category when targetServings then is correct`() {
        val fruitCategory = SuiteDefinitions.BALANCED.categories.find { it.name == "Fruit" }
        assertNotNull(fruitCategory)
        assertEquals(4, fruitCategory.scoringRule.targetServings)
    }

    @Test
    fun `given balanced suite sweets category when targetServings then is zero`() {
        val sweetsCategory = SuiteDefinitions.BALANCED.categories.find { it.name == "Sweets" }
        assertNotNull(sweetsCategory)
        assertEquals(0, sweetsCategory.scoringRule.targetServings)
    }
}
