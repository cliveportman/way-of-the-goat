package co.theportman.way_of_the_goat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.theportman.way_of_the_goat.data.cache.ServingsDataManager
import co.theportman.way_of_the_goat.data.scoring.DailyScoreResult
import co.theportman.way_of_the_goat.data.scoring.DailyTotalsForDisplay
import co.theportman.way_of_the_goat.data.scoring.ScoreCalculator
import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

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

    // Expose active suite for UI
    val activeSuite: StateFlow<ScoringSuite> = servingsDataManager.activeSuite

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
     * Ensure data is loaded around the given date
     * Detects scroll direction and preloads accordingly
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
     * Get calculated totals for display
     */
    fun getTotalsForDate(date: LocalDate): DailyTotalsForDisplay {
        val servings = servingsDataManager.getServingsForDate(date)
        val suite = activeSuite.value

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
