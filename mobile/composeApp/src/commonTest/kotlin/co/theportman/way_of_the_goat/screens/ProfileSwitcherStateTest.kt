package co.theportman.way_of_the_goat.screens

import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileSwitcherStateTest {

    @Test
    fun `given default state when isSheetOpen then is false`() {
        val state = ProfileSwitcherState()
        assertFalse(state.isSheetOpen)
    }

    @Test
    fun `given default state when checked then has no selection or dialog`() {
        val state = ProfileSwitcherState()
        assertFalse(state.showConfirmationDialog)
        assertTrue(state.selectedSuiteId == null)
        assertTrue(state.targetDate == null)
    }

    @Test
    fun `given same profile selected when isNewProfileSelected then returns false`() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = currentId,
            isEmptyPastDay = false
        )

        assertFalse(state.isNewProfileSelected(currentId))
    }

    @Test
    fun `given different profile selected when isNewProfileSelected then returns true`() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = SuiteDefinitions.RACING_WEIGHT_ID,
            isEmptyPastDay = false
        )

        assertTrue(state.isNewProfileSelected(currentId))
    }

    @Test
    fun `given no selection when isNewProfileSelected then returns false`() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = null,
            isEmptyPastDay = false
        )

        assertFalse(state.isNewProfileSelected(currentId))
    }

    @Test
    fun `given empty past day and any selection when isNewProfileSelected then returns true`() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = SuiteDefinitions.BALANCED_ID, // Same as current
            isEmptyPastDay = true
        )

        // For empty past days, any selection (even same as current) is valid
        assertTrue(state.isNewProfileSelected(currentId))
    }

    @Test
    fun `given empty past day with no selection when isNewProfileSelected then returns false`() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = null,
            isEmptyPastDay = true
        )

        // No selection is not valid even for empty past days
        assertFalse(state.isNewProfileSelected(currentId))
    }

    @Test
    fun `given open sheet state when targetDate then matches the given date`() {
        val targetDate = LocalDate(2025, 1, 15)
        val state = ProfileSwitcherState(
            isSheetOpen = true,
            targetDate = targetDate,
            selectedSuiteId = SuiteDefinitions.BALANCED_ID
        )

        assertTrue(state.isSheetOpen)
        assertTrue(state.targetDate == targetDate)
    }

    @Test
    fun `given default state when useFutureChecked then is true`() {
        val state = ProfileSwitcherState()
        assertTrue(state.useFutureChecked)
    }
}
