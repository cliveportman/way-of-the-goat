package co.theportman.way_of_the_goat.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wellness data from intervals.icu
 * Documentation: https://intervals.icu/api/v1/docs/swagger-ui/index.html
 */
@Serializable
data class WellnessData(
    @SerialName("id")
    val id: String? = null,

    @SerialName("athlete_id")
    val athleteId: String? = null,

    @SerialName("date")
    val date: String? = null, // Format: YYYY-MM-DD

    @SerialName("weight")
    val weight: Double? = null,

    @SerialName("restingHR")
    val restingHR: Int? = null,

    @SerialName("hrv")
    val hrv: Double? = null,

    @SerialName("menstrualPhase")
    val menstrualPhase: String? = null,

    @SerialName("ctl")
    val ctl: Double? = null,

    @SerialName("atl")
    val atl: Double? = null,

    @SerialName("rampRate")
    val rampRate: Double? = null,

    @SerialName("sleepSecs")
    val sleepSecs: Int? = null,

    @SerialName("sleepScore")
    val sleepScore: Int? = null,

    @SerialName("soreness")
    val soreness: Int? = null,

    @SerialName("fatigue")
    val fatigue: Int? = null,

    @SerialName("mood")
    val mood: Int? = null,

    @SerialName("motivation")
    val motivation: Int? = null,

    @SerialName("stress")
    val stress: Int? = null,

    @SerialName("readiness")
    val readiness: Int? = null
)
