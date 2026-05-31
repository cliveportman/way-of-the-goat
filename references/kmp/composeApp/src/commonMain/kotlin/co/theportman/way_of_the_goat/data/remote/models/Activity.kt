package co.theportman.way_of_the_goat.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Activity data from intervals.icu
 * Documentation: https://intervals.icu/api/v1/docs/swagger-ui/index.html
 */
@Serializable
data class Activity(
    @SerialName("id")
    val id: String? = null,

    @SerialName("athlete_id")
    val athleteId: String? = null,

    @SerialName("start_date_local")
    val startDateLocal: String, // ISO 8601 format

    @SerialName("type")
    val type: String? = null, // e.g., "Ride", "Run", "Swim"

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("distance")
    val distance: Double? = null, // meters

    @SerialName("moving_time")
    val movingTime: Int? = null, // seconds

    @SerialName("elapsed_time")
    val elapsedTime: Int? = null, // seconds

    @SerialName("total_elevation_gain")
    val totalElevationGain: Double? = null, // meters

    @SerialName("average_speed")
    val averageSpeed: Double? = null, // m/s

    @SerialName("max_speed")
    val maxSpeed: Double? = null, // m/s

    @SerialName("average_watts")
    val averageWatts: Double? = null,

    @SerialName("weighted_average_watts")
    val weightedAverageWatts: Double? = null,

    @SerialName("max_watts")
    val maxWatts: Double? = null,

    @SerialName("average_heartrate")
    val averageHeartrate: Double? = null,

    @SerialName("max_heartrate")
    val maxHeartrate: Double? = null,

    @SerialName("calories")
    val calories: Double? = null,

    @SerialName("icu_training_load")
    val icuTrainingLoad: Double? = null,

    @SerialName("icu_intensity")
    val icuIntensity: Double? = null,

    @SerialName("feel")
    val feel: Int? = null, // 1-10 scale

    @SerialName("perceived_exertion")
    val perceivedExertion: Int? = null // RPE
)
