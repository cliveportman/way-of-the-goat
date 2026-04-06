package co.theportman.way_of_the_goat.screens

import app.cash.turbine.test
import co.theportman.way_of_the_goat.testutils.TestDispatcherHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Example sealed class for testing state emissions.
 */
private sealed class TestUiState {
    data object Loading : TestUiState()
    data object Success : TestUiState()
    data class Error(val message: String) : TestUiState()
}

/**
 * Example tests demonstrating Turbine usage with StateFlow.
 *
 * This file serves as a template for ViewModel tests once dependency
 * injection is set up for ServingsDataManager.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateFlowTestExample {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        TestDispatcherHelper.setMainDispatcher(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        TestDispatcherHelper.resetMainDispatcher()
    }

    @Test
    fun turbine_collectsInitialValue() = runTest {
        val stateFlow = MutableStateFlow("initial")

        stateFlow.test {
            assertEquals("initial", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun turbine_collectsEmittedValues() = runTest {
        val stateFlow = MutableStateFlow(0)

        stateFlow.test {
            assertEquals(0, awaitItem())

            stateFlow.value = 1
            assertEquals(1, awaitItem())

            stateFlow.value = 2
            assertEquals(2, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun turbine_ignoresIntermediateValuesWithExpectMostRecentItem() = runTest {
        val stateFlow = MutableStateFlow(0)

        stateFlow.test {
            // Skip initial
            skipItems(1)

            // Emit multiple values rapidly
            stateFlow.value = 1
            stateFlow.value = 2
            stateFlow.value = 3

            // Get the most recent value
            assertEquals(3, expectMostRecentItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun turbine_withSealedClassState() = runTest {
        val stateFlow = MutableStateFlow<TestUiState>(TestUiState.Loading)

        stateFlow.test {
            assertEquals(TestUiState.Loading, awaitItem())

            stateFlow.value = TestUiState.Success
            assertEquals(TestUiState.Success, awaitItem())

            stateFlow.value = TestUiState.Error("Network error")
            val errorState = awaitItem()
            assertEquals(TestUiState.Error("Network error"), errorState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun profileSwitcherState_flowEmissions() = runTest {
        val stateFlow = MutableStateFlow(ProfileSwitcherState())

        stateFlow.test {
            // Initial state
            val initial = awaitItem()
            assertEquals(false, initial.isSheetOpen)

            // Open sheet
            stateFlow.value = ProfileSwitcherState(isSheetOpen = true)
            val opened = awaitItem()
            assertEquals(true, opened.isSheetOpen)

            // Close sheet
            stateFlow.value = ProfileSwitcherState()
            val closed = awaitItem()
            assertEquals(false, closed.isSheetOpen)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
