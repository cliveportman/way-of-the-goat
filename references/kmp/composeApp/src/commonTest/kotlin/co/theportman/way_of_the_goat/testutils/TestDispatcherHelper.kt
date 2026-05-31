package co.theportman.way_of_the_goat.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Helper for setting up test dispatchers in unit tests.
 *
 * Usage in tests:
 * ```
 * class MyViewModelTest {
 *     private val testDispatcher = StandardTestDispatcher()
 *
 *     @BeforeTest
 *     fun setup() {
 *         TestDispatcherHelper.setMainDispatcher(testDispatcher)
 *     }
 *
 *     @AfterTest
 *     fun tearDown() {
 *         TestDispatcherHelper.resetMainDispatcher()
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
object TestDispatcherHelper {
    /**
     * Sets the Main dispatcher to use the provided test dispatcher.
     * Call this in @BeforeTest.
     */
    fun setMainDispatcher(dispatcher: TestDispatcher = StandardTestDispatcher()) {
        Dispatchers.setMain(dispatcher)
    }

    /**
     * Resets the Main dispatcher to its original state.
     * Call this in @AfterTest.
     */
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }
}
