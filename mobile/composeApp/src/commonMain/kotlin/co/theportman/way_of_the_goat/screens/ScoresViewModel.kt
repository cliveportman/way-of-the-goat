package co.theportman.way_of_the_goat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.theportman.way_of_the_goat.data.cache.ServingsDataManager
import co.theportman.way_of_the_goat.data.scoring.DailyScoreResult
import co.theportman.way_of_the_goat.data.scoring.DailyTotalsForDisplay
import co.theportman.way_of_the_goat.data.scoring.ScoreCalculator
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * State for the profile switcher bottom sheet and confirmation dialog.
 */
data class ProfileSwitcherState(
    val isSheetOpen: Boolean = false,
    val selectedSuiteId: SuiteId? = null,
    val useFutureChecked: Boolean = true,
    val showConfirmationDialog: Boolean = false,
    val targetDate: LocalDate? = null,
    val lastUsedSuiteId: SuiteId? = null,  // For "Last used: X" hint on empty days
    val isEmptyPastDay: Boolean = false    // True if target day has no profile selected
) {
    /**
     * Whether the selected profile differs from the current active profile.
     * For empty past days, any selection is considered "new" since there's no current profile.
     */
    fun isNewProfileSelected(currentSuiteId: SuiteId): Boolean {
        if (isEmptyPastDay) {
            return selectedSuiteId != null  // Any selection is valid for empty past days
        }
        return selectedSuiteId != null && selectedSuiteId != currentSuiteId
    }
}

/**
 * ViewModel for the Scores (Nutrition) screen
 * Manages food servings data for daily tracking
 * Supports bidirectional lazy loading
 */
class ScoresViewModel : ViewModel() {

    private val servingsDataManager = ServingsDataManager.instance

    private val _uiState = MutableStateFlow<ScoresUiState>(ScoresUiState.Loading)
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Profile switcher state
    private val _profileSwitcherState = MutableStateFlow(ProfileSwitcherState())
    val profileSwitcherState: StateFlow<ProfileSwitcherState> = _profileSwitcherState.asStateFlow()

    // One-shot error events for snackbar display
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    // Expose active suite for UI
    val activeSuite: StateFlow<ScoringSuite> = servingsDataManager.activeSuite

    // All available profiles for the switcher
    val allProfiles: List<ScoringSuite> = SuiteDefinitions.allSuites

    // Expose servings flow for reactive UI updates
    val servingsFlow: StateFlow<Map<LocalDate, DailyServings>> = servingsDataManager.servingsFlow

    // Track last viewed date for scroll direction detection
    private var lastViewedDate: LocalDate? = null

    init {
        loadInitialData()
    }

    /**
     * Load initial data (today ± 30 days or around a specific target date)
     */
    fun loadInitialData(targetDate: LocalDate? = null) {
        viewModelScope.launch {
            _uiState.value = ScoresUiState.Loading

            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val centerDate = targetDate ?: today

                // Check if ServingsDataManager is initialized
                if (!servingsDataManager.isInitialized.value) {
                    // Not initialized yet - set to success state, data will load when initialized
                    _uiState.value = ScoresUiState.Success
                    return@launch
                }

                // Load ±30 days around the center date
                servingsDataManager.loadInitialData(aroundDate = centerDate, bufferDays = 30).fold(
                    onSuccess = {
                        _uiState.value = ScoresUiState.Success
                    },
                    onFailure = { error ->
                        _uiState.value = ScoresUiState.Error(error.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ScoresUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Ensure data is loaded around the given date.
     * Detects scroll direction and preloads accordingly.
     */
    fun ensureDateLoaded(currentDate: LocalDate) {
        viewModelScope.launch {
            if (!servingsDataManager.isInitialized.value) return@launch

            val previousDate = lastViewedDate

            if (previousDate != null) {
                // Detect scroll direction and preload accordingly
                if (currentDate != previousDate) {
                    servingsDataManager.ensureDateLoaded(currentDate, bufferDays = 30)
                }
            }

            lastViewedDate = currentDate
        }
    }

    /**
     * Refresh the current visible date's data
     */
    fun refreshCurrentDate(date: LocalDate) {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                servingsDataManager.refreshDate(date).fold(
                    onSuccess = {
                        // Success - data is automatically updated via StateFlow
                    },
                    onFailure = { error ->
                        println("Error refreshing date: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                println("Exception refreshing date: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Check if a specific date is loaded
     */
    fun isDateLoaded(date: LocalDate): Boolean {
        if (!servingsDataManager.isInitialized.value) return true // Allow UI to render
        return servingsDataManager.isDateLoaded(date)
    }

    /**
     * Get servings for a specific date
     */
    fun getServingsForDate(date: LocalDate): DailyServings? {
        return servingsDataManager.getServingsForDate(date)
    }

    /**
     * Get the scoring suite for a specific date.
     * Returns the suite stored with the day's data if it exists,
     * otherwise returns the current active suite.
     */
    fun getSuiteForDate(date: LocalDate): ScoringSuite {
        val dailyServings = servingsDataManager.getServingsForDate(date)
        return if (dailyServings != null) {
            SuiteDefinitions.getSuiteById(dailyServings.suiteId) ?: activeSuite.value
        } else {
            activeSuite.value
        }
    }

    /**
     * Get calculated totals for display.
     * Uses the suite stored with the day's data.
     */
    fun getTotalsForDate(date: LocalDate): DailyTotalsForDisplay {
        val servings = servingsDataManager.getServingsForDate(date)
        val suite = getSuiteForDate(date)

        return if (servings != null) {
            ScoreCalculator.calculateTotalsForDisplay(servings, suite)
        } else {
            DailyTotalsForDisplay.empty()
        }
    }

    /**
     * Get calculated totals for display with explicit servings and suite.
     * Use this overload from the UI for reactive updates.
     */
    fun getTotalsForDisplay(servings: DailyServings?, suite: ScoringSuite): DailyTotalsForDisplay {
        return if (servings != null) {
            ScoreCalculator.calculateTotalsForDisplay(servings, suite)
        } else {
            DailyTotalsForDisplay.empty()
        }
    }

    /**
     * Increment servings for a category on a date
     */
    fun incrementServings(date: LocalDate, categoryId: CategoryId) {
        viewModelScope.launch {
            val category = activeSuite.value.getCategoryById(categoryId)
            val maxServings = category?.scoringRule?.maxTrackedServings ?: 6

            servingsDataManager.incrementServings(date, categoryId, maxServings).fold(
                onSuccess = { /* UI updates via StateFlow */ },
                onFailure = { error ->
                    println("Error incrementing servings: ${error.message}")
                }
            )
        }
    }

    /**
     * Decrement servings for a category on a date
     */
    fun decrementServings(date: LocalDate, categoryId: CategoryId) {
        viewModelScope.launch {
            servingsDataManager.decrementServings(date, categoryId).fold(
                onSuccess = { /* UI updates via StateFlow */ },
                onFailure = { error ->
                    println("Error decrementing servings: ${error.message}")
                }
            )
        }
    }

    // ==================== Profile Switcher Methods ====================

    /**
     * Open the profile switcher sheet for a specific date.
     * For empty past days, fetches the "last used" profile as a hint.
     */
    fun openProfileSwitcher(date: LocalDate) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dailyServings = servingsDataManager.getServingsForDate(date)
        val hasData = dailyServings != null
        val isEmptyPastDay = !hasData && date < today
        val initialSuiteId = dailyServings?.suiteId ?: activeSuite.value.id

        _profileSwitcherState.value = ProfileSwitcherState(
            isSheetOpen = true,
            selectedSuiteId = initialSuiteId,
            useFutureChecked = true,
            showConfirmationDialog = false,
            targetDate = date,
            lastUsedSuiteId = null,  // Will be populated async for empty past days
            isEmptyPastDay = isEmptyPastDay
        )

        // For empty past days, fetch the last used profile as a hint
        if (isEmptyPastDay) {
            viewModelScope.launch {
                val lastUsed = servingsDataManager.getLastUsedProfile(date)
                val currentState = _profileSwitcherState.value
                // Only update if sheet is still open for the same date
                if (currentState.isSheetOpen && currentState.targetDate == date) {
                    _profileSwitcherState.value = currentState.copy(
                        lastUsedSuiteId = lastUsed
                    )
                }
            }
        }
    }

    /**
     * Close the profile switcher sheet without making changes.
     */
    fun closeProfileSwitcher() {
        _profileSwitcherState.value = ProfileSwitcherState()
    }

    /**
     * Select a profile in the sheet (doesn't apply it yet).
     */
    fun selectProfileInSheet(suiteId: SuiteId) {
        _profileSwitcherState.value = _profileSwitcherState.value.copy(
            selectedSuiteId = suiteId
        )
    }

    /**
     * Toggle the "Continue using this profile in future" checkbox.
     */
    fun toggleFutureProfileCheckbox(checked: Boolean) {
        _profileSwitcherState.value = _profileSwitcherState.value.copy(
            useFutureChecked = checked
        )
    }

    /**
     * Check if the target date has existing servings data.
     */
    fun hasExistingData(date: LocalDate): Boolean {
        return servingsDataManager.hasServingsForDate(date)
    }

    /**
     * Initiate profile switch. If data exists, shows confirmation dialog.
     */
    fun initiateProfileSwitch() {
        val state = _profileSwitcherState.value
        val date = state.targetDate ?: return
        val selectedId = state.selectedSuiteId ?: return

        // Check if this is actually a different profile from what's stored for this date
        val currentSuiteId = servingsDataManager.getServingsForDate(date)?.suiteId
            ?: activeSuite.value.id
        if (!state.isNewProfileSelected(currentSuiteId)) {
            closeProfileSwitcher()
            return
        }

        // Check if there's existing data that would be lost
        if (hasExistingData(date)) {
            // Show confirmation dialog
            _profileSwitcherState.value = state.copy(showConfirmationDialog = true)
        } else {
            // No data to lose, switch immediately
            confirmProfileSwitch()
        }
    }

    /**
     * Confirm profile switch (after data loss warning).
     * Deletes existing data for the target date.
     *
     * For TODAY: If "Continue using this profile in future" is checked,
     *            the user's default preference is also updated.
     * For PAST DAYS: Creates an empty daily record with the selected profile.
     */
    fun confirmProfileSwitch() {
        val state = _profileSwitcherState.value
        val date = state.targetDate ?: return
        val selectedId = state.selectedSuiteId ?: return
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val isToday = date == today

        viewModelScope.launch {
            var hasError = false

            // Delete existing servings for this date if any
            if (hasExistingData(date)) {
                servingsDataManager.deleteServingsForDate(date).fold(
                    onSuccess = { /* Data deleted */ },
                    onFailure = { error ->
                        hasError = true
                        _errorEvent.emit("Failed to delete existing data. Please try again.")
                        println("Error deleting servings: ${error.message}")
                    }
                )
            }

            if (hasError) return@launch

            if (isToday) {
                // For TODAY: always create per-day record
                servingsDataManager.createEmptyDayWithSuite(date, selectedId).fold(
                    onSuccess = { /* Empty record created */ },
                    onFailure = { error ->
                        hasError = true
                        _errorEvent.emit("Failed to switch profile. Please try again.")
                        println("Error creating empty day record: ${error.message}")
                    }
                )

                // Optionally update default preference for future days
                if (!hasError && state.useFutureChecked) {
                    servingsDataManager.setActiveSuite(selectedId).fold(
                        onSuccess = { /* Default preference updated */ },
                        onFailure = { error ->
                            hasError = true
                            _errorEvent.emit("Failed to update default profile. Please try again.")
                            println("Error setting active suite: ${error.message}")
                        }
                    )
                }
            } else {
                // For PAST DAYS: create an empty daily record with the selected profile
                // This establishes which profile the day uses
                servingsDataManager.createEmptyDayWithSuite(date, selectedId).fold(
                    onSuccess = { /* Empty record created */ },
                    onFailure = { error ->
                        hasError = true
                        _errorEvent.emit("Failed to set profile for this day. Please try again.")
                        println("Error creating empty day record: ${error.message}")
                    }
                )
            }

            // Only close the sheet if there were no errors
            if (!hasError) {
                closeProfileSwitcher()
            }
        }
    }

    /**
     * Cancel the confirmation dialog (returns to sheet).
     */
    fun cancelConfirmation() {
        _profileSwitcherState.value = _profileSwitcherState.value.copy(
            showConfirmationDialog = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        // ServingsDataManager is a singleton, don't close it here
    }
}

sealed class ScoresUiState {
    data object Loading : ScoresUiState()
    data object Success : ScoresUiState()
    data class Error(val message: String) : ScoresUiState()
}
