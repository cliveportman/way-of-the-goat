package co.theportman.way_of_the_goat.screens

import app.cash.turbine.test
import co.theportman.way_of_the_goat.data.remote.models.Activity
import co.theportman.way_of_the_goat.data.scoring.WeeklyActivityBuilder
import co.theportman.way_of_the_goat.testutils.FakeActivityDataSource
import co.theportman.way_of_the_goat.testutils.TestDispatcherHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RunsOverTimeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedTimeZone = TimeZone.UTC
    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-02-18T12:00:00Z")
    }
    private val weeklyActivityBuilder = WeeklyActivityBuilder(clock = fixedClock, timeZone = fixedTimeZone)

    private lateinit var fakeDataSource: FakeActivityDataSource

    @BeforeTest
    fun setup() {
        TestDispatcherHelper.setMainDispatcher(testDispatcher)
        fakeDataSource = FakeActivityDataSource()
    }

    @AfterTest
    fun tearDown() {
        TestDispatcherHelper.resetMainDispatcher()
    }

    private fun createViewModel(): RunsOverTimeViewModel {
        return RunsOverTimeViewModel(
            activityDataSource = fakeDataSource,
            weeklyActivityBuilder = weeklyActivityBuilder,
            clock = fixedClock,
            timeZone = fixedTimeZone
        )
    }

    private fun activityOn(dateLocal: String, distanceMetres: Double? = 10000.0): Activity {
        return Activity(
            id = "act-$dateLocal-${distanceMetres?.toInt()}",
            startDateLocal = "${dateLocal}T08:00:00",
            distance = distanceMetres
        )
    }

    @Test
    fun `given successful load with data when created then transitions from Loading to Success`() = runTest {
        fakeDataSource.activitiesToLoad = listOf(
            activityOn("2026-02-16", 15000.0)
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RunsOverTimeUiState.Loading, awaitItem())

            val success = awaitItem()
            assertTrue(success is RunsOverTimeUiState.Success)
            assertTrue(success.weeks.isNotEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given successful load with no data when created then transitions from Loading to Empty`() = runTest {
        // activitiesToLoad defaults to emptyList, so buildWeeks returns null -> Empty
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RunsOverTimeUiState.Loading, awaitItem())
            assertEquals(RunsOverTimeUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given load failure when created then transitions from Loading to Error`() = runTest {
        fakeDataSource.loadError = RuntimeException("Network error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RunsOverTimeUiState.Loading, awaitItem())

            val error = awaitItem()
            assertTrue(error is RunsOverTimeUiState.Error)
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
    fun `given activities change after initial load when flow emits then rebuilds UI state`() = runTest {
        val initialActivities = listOf(
            activityOn("2026-02-16", 15000.0)
        )
        fakeDataSource.activitiesToLoad = initialActivities

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Loading -> Success
            assertEquals(RunsOverTimeUiState.Loading, awaitItem())
            val firstSuccess = awaitItem()
            assertTrue(firstSuccess is RunsOverTimeUiState.Success)

            // Simulate activities change (add another day)
            fakeDataSource.emitActivities(
                initialActivities + activityOn("2026-02-17", 20000.0)
            )

            val updated = awaitItem()
            assertTrue(updated is RunsOverTimeUiState.Success)
            // The week should now contain activities for both days
            val week = updated.weeks.first()
            val activeDays = week.dailyActivities.filterNotNull()
            assertEquals(2, activeDays.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
