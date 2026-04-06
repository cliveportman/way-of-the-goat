package co.theportman.way_of_the_goat.screens

import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileSwitcherStateTest {

    @Test
    fun defaultState_sheetIsClosed() {
        val state = ProfileSwitcherState()
        assertFalse(state.isSheetOpen)
    }

    @Test
    fun defaultState_noSelectionOrDialog() {
        val state = ProfileSwitcherState()
        assertFalse(state.showConfirmationDialog)
        assertTrue(state.selectedSuiteId == null)
        assertTrue(state.targetDate == null)
    }

    @Test
    fun isNewProfileSelected_sameProfile_returnsFalse() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = currentId,
            isEmptyPastDay = false
        )

        assertFalse(state.isNewProfileSelected(currentId))
    }

    @Test
    fun isNewProfileSelected_differentProfile_returnsTrue() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = SuiteDefinitions.RACING_WEIGHT_ID,
            isEmptyPastDay = false
        )

        assertTrue(state.isNewProfileSelected(currentId))
    }

    @Test
    fun isNewProfileSelected_noSelection_returnsFalse() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = null,
            isEmptyPastDay = false
        )

        assertFalse(state.isNewProfileSelected(currentId))
    }

    @Test
    fun isNewProfileSelected_emptyPastDay_anySelectionIsNew() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = SuiteDefinitions.BALANCED_ID, // Same as current
            isEmptyPastDay = true
        )

        // For empty past days, any selection (even same as current) is valid
        assertTrue(state.isNewProfileSelected(currentId))
    }

    @Test
    fun isNewProfileSelected_emptyPastDayNoSelection_returnsFalse() {
        val currentId = SuiteDefinitions.BALANCED_ID
        val state = ProfileSwitcherState(
            selectedSuiteId = null,
            isEmptyPastDay = true
        )

        // No selection is not valid even for empty past days
        assertFalse(state.isNewProfileSelected(currentId))
    }

    @Test
    fun stateWithOpenSheet_containsTargetDate() {
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
    fun useFutureChecked_defaultsToTrue() {
        val state = ProfileSwitcherState()
        assertTrue(state.useFutureChecked)
    }
}
