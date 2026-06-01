package co.theportman.way_of_the_goat.data.repository

import co.theportman.way_of_the_goat.data.fake.FakeCategoryServingsDao
import co.theportman.way_of_the_goat.data.fake.FakeDailyServingsDao
import co.theportman.way_of_the_goat.data.fake.FakeDb
import co.theportman.way_of_the_goat.data.fake.FakeTransactionRunner
import co.theportman.way_of_the_goat.data.fake.FakeUserPreferencesDao
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ServingsRepositoryTest {

    private val db = FakeDb()
    private var clock = 1_000L
    private val repo = ServingsRepository(
        preferencesDao = FakeUserPreferencesDao(db),
        dailyServingsDao = FakeDailyServingsDao(db),
        categoryServingsDao = FakeCategoryServingsDao(db),
        transactionRunner = FakeTransactionRunner(),
        now = { clock },
    )

    private val date = LocalDate(2026, 1, 15)
    private val fruit = CategoryId("fruit")
    private val veg = CategoryId("veg")

    @Test
    fun `given no prefs when initialize then default suite persisted and active`() = runTest {
        val result = repo.initialize()

        assertTrue(result.isSuccess)
        assertEquals(SuiteDefinitions.defaultSuite.id, repo.activeSuite.value.id)
        assertEquals(SuiteDefinitions.defaultSuite.id.value, db.prefs?.active_suite_id)
    }

    @Test
    fun `given existing prefs when initialize then stored suite becomes active`() = runTest {
        repo.setActiveSuiteThroughInit(SuiteDefinitions.RACING_WEIGHT_ID)

        val fresh = ServingsRepository(
            FakeUserPreferencesDao(db),
            FakeDailyServingsDao(db),
            FakeCategoryServingsDao(db),
            FakeTransactionRunner(),
            now = { clock },
        )
        fresh.initialize()

        assertEquals(SuiteDefinitions.RACING_WEIGHT_ID, fresh.activeSuite.value.id)
    }

    @Test
    fun `given unknown suite when setActiveSuite then failure and active unchanged`() = runTest {
        repo.initialize()
        val before = repo.activeSuite.value.id

        val result = repo.setActiveSuite(SuiteId("does_not_exist"))

        assertTrue(result.isFailure)
        assertEquals(before, repo.activeSuite.value.id)
    }

    @Test
    fun `given servings saved when getServingsForDate then round-trips zero-count dropped`() = runTest {
        repo.initialize()
        repo.saveServings(
            DailyServings(
                date = date,
                suiteId = SuiteDefinitions.defaultSuite.id,
                servings = mapOf(fruit to 3, veg to 0),
            ),
        )

        val loaded = repo.getServingsForDate(date).getOrThrow()

        assertEquals(3, loaded?.getServings(fruit))
        assertEquals(0, loaded?.getServings(veg)) // 0 not persisted, defaults to 0 on read
        assertEquals(1, loaded?.servings?.size)
    }

    @Test
    fun `given existing record when saveServings again then replaced not duplicated`() = runTest {
        repo.initialize()
        val suite = SuiteDefinitions.defaultSuite.id
        repo.saveServings(DailyServings(date = date, suiteId = suite, servings = mapOf(fruit to 2)))
        repo.saveServings(DailyServings(date = date, suiteId = suite, servings = mapOf(veg to 5)))

        val loaded = repo.getServingsForDate(date).getOrThrow()

        assertEquals(1L, repo.getDaysTrackedCount().getOrThrow())
        assertEquals(0, loaded?.getServings(fruit)) // old category cleared
        assertEquals(5, loaded?.getServings(veg))
    }

    @Test
    fun `given no record when updateCategoryServings then creates day with active suite`() = runTest {
        repo.initialize()

        repo.updateCategoryServings(date, fruit, 4)

        val loaded = repo.getServingsForDate(date).getOrThrow()
        assertEquals(4, loaded?.getServings(fruit))
        assertEquals(SuiteDefinitions.defaultSuite.id, loaded?.suiteId)
    }

    @Test
    fun `given category set to zero when updateCategoryServings then row removed`() = runTest {
        repo.initialize()
        repo.updateCategoryServings(date, fruit, 4)

        repo.updateCategoryServings(date, fruit, 0)

        val loaded = repo.getServingsForDate(date).getOrThrow()
        assertEquals(0, loaded?.getServings(fruit))
        assertEquals(0, loaded?.servings?.size)
    }

    @Test
    fun `given record when deleteServingsForDate then gone and categories cascade`() = runTest {
        repo.initialize()
        repo.updateCategoryServings(date, fruit, 4)

        repo.deleteServingsForDate(date)

        assertNull(repo.getServingsForDate(date).getOrThrow())
        assertTrue(db.categories.isEmpty()) // cascade removed children
    }

    @Test
    fun `given multiple dates when getServingsForRange then filtered and newest first`() = runTest {
        repo.initialize()
        val suite = SuiteDefinitions.defaultSuite.id
        repo.saveServings(DailyServings(date = LocalDate(2026, 1, 10), suiteId = suite, servings = mapOf(fruit to 1)))
        repo.saveServings(DailyServings(date = LocalDate(2026, 1, 20), suiteId = suite, servings = mapOf(fruit to 2)))
        repo.saveServings(DailyServings(date = LocalDate(2026, 2, 5), suiteId = suite, servings = mapOf(fruit to 3)))

        val range = repo.getServingsForRange(LocalDate(2026, 1, 1), LocalDate(2026, 1, 31)).getOrThrow()

        assertEquals(listOf(LocalDate(2026, 1, 20), LocalDate(2026, 1, 10)), range.map { it.date })
    }

    @Test
    fun `given earlier records when getLastServingsBeforeDate then most recent prior returned`() = runTest {
        repo.initialize()
        val suite = SuiteDefinitions.RACING_WEIGHT_ID
        repo.saveServings(DailyServings(date = LocalDate(2026, 1, 5), suiteId = SuiteDefinitions.defaultSuite.id, servings = mapOf(fruit to 1)))
        repo.saveServings(DailyServings(date = LocalDate(2026, 1, 12), suiteId = suite, servings = mapOf(fruit to 1)))

        val last = repo.getLastServingsBeforeDate(LocalDate(2026, 1, 15)).getOrThrow()

        assertEquals(LocalDate(2026, 1, 12), last?.date)
        assertEquals(suite, last?.suiteId)
    }

    @Test
    fun `given saved day when calculateScoreForDate then matches ScoreCalculator via suite`() = runTest {
        repo.initialize()
        val suite = SuiteDefinitions.defaultSuite
        val servings = DailyServings(date = date, suiteId = suite.id, servings = mapOf(fruit to 2, veg to 2))
        repo.saveServings(servings)

        val result = repo.calculateScoreForDate(date).getOrThrow()

        assertEquals(servings.calculateScore(suite), result?.score)
        assertEquals(suite.id, result?.suiteId)
        assertEquals(suite.maxPossibleDailyScore, result?.maxPossibleScore)
    }

    @Test
    fun `given no record when calculateScoreForDate then null`() = runTest {
        repo.initialize()
        assertNull(repo.calculateScoreForDate(date).getOrThrow())
    }

    @Test
    fun `given dates when getAllDatesWithRecords then distinct newest first`() = runTest {
        repo.initialize()
        val suite = SuiteDefinitions.defaultSuite.id
        repo.saveServings(DailyServings(date = LocalDate(2026, 1, 10), suiteId = suite, servings = mapOf(fruit to 1)))
        repo.saveServings(DailyServings(date = LocalDate(2026, 1, 20), suiteId = suite, servings = mapOf(fruit to 1)))

        val dates = repo.getAllDatesWithRecords().getOrThrow()

        assertEquals(listOf(LocalDate(2026, 1, 20), LocalDate(2026, 1, 10)), dates)
    }

    // Helper: persist a preferences row with the given suite via a throwaway repo init + switch.
    private suspend fun ServingsRepository.setActiveSuiteThroughInit(suiteId: SuiteId) {
        initialize()
        setActiveSuite(suiteId)
    }
}
