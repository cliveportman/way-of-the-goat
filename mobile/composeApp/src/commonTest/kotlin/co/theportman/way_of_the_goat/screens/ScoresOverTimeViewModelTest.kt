package co.theportman.way_of_the_goat.screens

import app.cash.turbine.test
import co.theportman.way_of_the_goat.data.scoring.WeeklyScoreBuilder
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.testutils.FakeServingsDataSource
import co.theportman.way_of_the_goat.testutils.TestDispatcherHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScoresOverTimeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedTimeZone = TimeZone.UTC
    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-02-18T12:00:00Z")
    }
    private val weeklyScoreBuilder = WeeklyScoreBuilder(clock = fixedClock, timeZone = fixedTimeZone)

    private lateinit var fakeDataSource: FakeServingsDataSource

    @BeforeTest
    fun setup() {
        TestDispatcherHelper.setMainDispatcher(testDispatcher)
        fakeDataSource = FakeServingsDataSource()
    }

    @AfterTest
    fun tearDown() {
        TestDispatcherHelper.resetMainDispatcher()
    }

    private fun createViewModel(): ScoresOverTimeViewModel {
        return ScoresOverTimeViewModel(
            servingsDataSource = fakeDataSource,
            weeklyScoreBuilder = weeklyScoreBuilder,
            clock = fixedClock,
            timeZone = fixedTimeZone
        )
    }

    @Test
    fun `given successful load with data when created then transitions from Loading to Success`() = runTest {
        val date = LocalDate(2026, 2, 16)
        fakeDataSource.servingsToLoad = mapOf(
            date to DailyServings(
                date = date,
                suiteId = SuiteDefinitions.BALANCED_ID,
                servings = mapOf(CategoryId("fruits") to 3)
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Initial Loading state
            assertEquals(ScoresOverTimeUiState.Loading, awaitItem())

            // After load completes, transitions to Success
            val success = awaitItem()
            assertTrue(success is ScoresOverTimeUiState.Success)
            assertTrue(success.weeks.isNotEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given successful load with no data when created then transitions from Loading to Empty`() = runTest {
        // servingsToLoad defaults to emptyMap, so buildWeeks returns null → Empty
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ScoresOverTimeUiState.Loading, awaitItem())
            assertEquals(ScoresOverTimeUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given load failure when created then transitions from Loading to Error`() = runTest {
        fakeDataSource.loadError = RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ScoresOverTimeUiState.Loading, awaitItem())

            val error = awaitItem()
            assertTrue(error is ScoresOverTimeUiState.Error)
            assertEquals("Network error", error.message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given load failure when created then emits error event`() = runTest {
        fakeDataSource.loadError = RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.errors.test {
            assertEquals("Network error", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given data source not initialized when created then transitions to Empty`() = runTest {
        fakeDataSource.setInitialized(false)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ScoresOverTimeUiState.Loading, awaitItem())
            assertEquals(ScoresOverTimeUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given servings change after initial load when flow emits then rebuilds UI state`() = runTest {
        val date = LocalDate(2026, 2, 16)
        val initialServings = mapOf(
            date to DailyServings(
                date = date,
                suiteId = SuiteDefinitions.BALANCED_ID,
                servings = mapOf(CategoryId("fruits") to 3)
            )
        )
        fakeDataSource.servingsToLoad = initialServings

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Loading → Success
            assertEquals(ScoresOverTimeUiState.Loading, awaitItem())
            val firstSuccess = awaitItem()
            assertTrue(firstSuccess is ScoresOverTimeUiState.Success)

            // Simulate servings change (add another day)
            val newDate = LocalDate(2026, 2, 17)
            fakeDataSource.emitServings(
                initialServings + (newDate to DailyServings(
                    date = newDate,
                    suiteId = SuiteDefinitions.BALANCED_ID,
                    servings = mapOf(CategoryId("fruits") to 5)
                ))
            )

            val updated = awaitItem()
            assertTrue(updated is ScoresOverTimeUiState.Success)
            // The week should now contain scores for both days
            val week = updated.weeks.first()
            val scoredDays = week.dailyScores.filterNotNull()
            assertEquals(2, scoredDays.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
