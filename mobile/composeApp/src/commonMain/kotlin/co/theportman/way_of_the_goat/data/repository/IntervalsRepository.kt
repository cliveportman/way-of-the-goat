package co.theportman.way_of_the_goat.data.repository

import co.theportman.way_of_the_goat.data.auth.ApiKeyAuthProvider
import co.theportman.way_of_the_goat.data.remote.HttpClientFactory
import co.theportman.way_of_the_goat.data.remote.IntervalsApiClient
import co.theportman.way_of_the_goat.data.remote.models.Activity
import co.theportman.way_of_the_goat.data.remote.models.WellnessData
import kotlin.coroutines.cancellation.CancellationException

/**
 * Repository for intervals.icu data
 * Provides a clean API for the UI layer and handles business logic
 */
class IntervalsRepository {

    // TODO: Replace with dependency injection
    private val authProvider = ApiKeyAuthProvider()
    private val httpClient = HttpClientFactory.create(authProvider)
    private val apiClient = IntervalsApiClient(httpClient)

    /**
     * Get wellness data for a date range
     */
    suspend fun getWellness(oldest: String, newest: String): Result<List<WellnessData>> {
        return try {
            val data = apiClient.getWellness(oldest, newest)
            Result.success(data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get activities for a date range
     */
    suspend fun getActivities(oldest: String, newest: String): Result<List<Activity>> {
        return try {
            val data = apiClient.getActivities(oldest, newest)
            Result.success(data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get today's wellness data
     */
    suspend fun getTodaysWellness(): Result<WellnessData?> {
        return try {
            val data = apiClient.getTodaysWellness()
            Result.success(data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get recent activities (last 30 days)
     */
    suspend fun getRecentActivities(): Result<List<Activity>> {
        return try {
            val data = apiClient.getRecentActivities()
            Result.success(data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clean up resources when done
     */
    fun close() {
        httpClient.close()
    }
}
