package co.theportman.way_of_the_goat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.theportman.way_of_the_goat.data.cache.ActivityDataManager
import co.theportman.way_of_the_goat.data.remote.models.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for the Activity screen
 * Manages activity data for a specific date
 * Supports bidirectional lazy loading
 */
class ActivityViewModel : ViewModel() {

    private val dataManager = ActivityDataManager

    private val _uiState = MutableStateFlow<ActivityUiState>(ActivityUiState.Loading)
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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
            _uiState.value = ActivityUiState.Loading

            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val centerDate = targetDate ?: today

                // Load ±30 days around the center date
                dataManager.loadInitialData(aroundDate = centerDate, bufferDays = 30).fold(
                    onSuccess = {
                        _uiState.value = ActivityUiState.Success(emptyList())
                    },
                    onFailure = { error ->
                        _uiState.value = ActivityUiState.Error(error.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ActivityUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Ensure data is loaded around the given date
     * Detects scroll direction and preloads accordingly
     */
    fun ensureDateLoaded(currentDate: LocalDate) {
        viewModelScope.launch {
            val previousDate = lastViewedDate

            if (previousDate != null) {
                // Detect scroll direction
                if (currentDate < previousDate) {
                    // Scrolling backwards (left) - into the past
                    // Preload 30 days before current date
                    dataManager.ensureDateLoaded(currentDate, bufferDays = 30)
                } else if (currentDate > previousDate) {
                    // Scrolling forwards (right) - towards today
                    // Preload 30 days after current date
                    dataManager.ensureDateLoaded(currentDate, bufferDays = 30)
                }
            }

            lastViewedDate = currentDate
        }
    }

    /**
     * Refresh the current visible date's data
     * Reloads just the specified date
     */
    fun refreshCurrentDate(date: LocalDate) {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                // Refresh just this date through ActivityDataManager
                dataManager.refreshDateRange(date, date).fold(
                    onSuccess = {
                        // Success - data is automatically updated via StateFlow
                    },
                    onFailure = { error ->
                        // Handle error - could show a toast/snackbar
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
        return dataManager.isDateLoaded(date)
    }

    /**
     * Get activities for a specific date
     */
    fun getActivitiesForDate(date: LocalDate): List<Activity> {
        return dataManager.getActivitiesForDate(date)
    }

    override fun onCleared() {
        super.onCleared()
        // ActivityDataManager is a singleton, don't close it here
    }
}

sealed class ActivityUiState {
    data object Loading : ActivityUiState()
    data class Success(val activities: List<Activity>) : ActivityUiState()
    data class Error(val message: String) : ActivityUiState()
}
