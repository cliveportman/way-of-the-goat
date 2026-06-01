package co.theportman.way_of_the_goat.data.repository

import co.theportman.way_of_the_goat.data.auth.ApiKeyAuthProvider
import co.theportman.way_of_the_goat.data.remote.HttpClientFactory
import co.theportman.way_of_the_goat.data.remote.IntervalsApiClient
import co.theportman.way_of_the_goat.data.remote.models.Activity
import co.theportman.way_of_the_goat.data.remote.models.WellnessData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository for intervals.icu data
 * Provides a clean API for the UI layer and handles business logic
 */
class IntervalsRepository {

    // TODO: Replace with dependency injection
    private val authProvider = ApiKeyAuthProvider()
    private val mutex = Mutex()
    private var httpClient: io.ktor.client.HttpClient? = null
    private var apiClient: IntervalsApiClient? = null

    private suspend fun getApiClient(): IntervalsApiClient {
        return mutex.withLock {
            apiClient ?: run {
                val client = HttpClientFactory.create(authProvider)
                httpClient = client
                IntervalsApiClient(client).also { apiClient = it }
            }
        }
    }

    /**
     * Get wellness data for a date range
     */
    suspend fun getWellness(oldest: String, newest: String): Result<List<WellnessData>> {
        return try {
            val data = getApiClient().getWellness(oldest, newest)
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
            val data = getApiClient().getActivities(oldest, newest)
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
            val data = getApiClient().getTodaysWellness()
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
            val data = getApiClient().getRecentActivities()
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
        httpClient?.close()
    }
}
