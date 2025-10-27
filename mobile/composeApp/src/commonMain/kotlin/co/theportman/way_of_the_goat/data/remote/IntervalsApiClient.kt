package co.theportman.way_of_the_goat.data.remote

import co.theportman.way_of_the_goat.data.remote.models.Activity
import co.theportman.way_of_the_goat.data.remote.models.WellnessData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * API client for intervals.icu
 * Documentation: https://intervals.icu/api/v1/docs/swagger-ui/index.html
 */
class IntervalsApiClient(
    private val httpClient: HttpClient
) {
    companion object {
        private const val BASE_URL = "https://intervals.icu/api/v1"
        private const val CURRENT_ATHLETE = "0" // Special ID for current authenticated user
    }

    /**
     * Get wellness data for a date range
     * @param oldest ISO date string (YYYY-MM-DD) - start of range
     * @param newest ISO date string (YYYY-MM-DD) - end of range
     */
    suspend fun getWellness(
        oldest: String,
        newest: String
    ): List<WellnessData> {
        return httpClient.get("$BASE_URL/athlete/$CURRENT_ATHLETE/wellness") {
            parameter("oldest", oldest)
            parameter("newest", newest)
        }.body()
    }

    /**
     * Get activities for a date range
     * @param oldest ISO date string (YYYY-MM-DD) - start of range
     * @param newest ISO date string (YYYY-MM-DD) - end of range
     */
    suspend fun getActivities(
        oldest: String,
        newest: String
    ): List<Activity> {
        return httpClient.get("$BASE_URL/athlete/$CURRENT_ATHLETE/activities") {
            parameter("oldest", oldest)
            parameter("newest", newest)
        }.body()
    }

    /**
     * Get a specific activity by ID
     * @param activityId The activity ID
     */
    suspend fun getActivity(activityId: String): Activity {
        return httpClient.get("$BASE_URL/activity/$activityId").body()
    }

    /**
     * Get today's wellness data
     */
    suspend fun getTodaysWellness(): WellnessData? {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

        val wellness = getWellness(oldest = today, newest = today)
        return wellness.firstOrNull()
    }

    /**
     * Get recent activities (last 30 days)
     */
    suspend fun getRecentActivities(): List<Activity> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val thirtyDaysAgo = today.minus(30, DateTimeUnit.DAY)

        val newest = today.toString()
        val oldest = thirtyDaysAgo.toString()

        return getActivities(oldest = oldest, newest = newest)
    }
}
