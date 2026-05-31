import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal data class TrackPoint(
    val lat: Double,
    val lon: Double,
    var ele: Double? = null,
    var timeS: Double? = null,
    var hr: Int? = null,
)

@Serializable
data class ChartPoint(
    @SerialName("distance_km") val distanceKm: Double,
    @SerialName("pace_min_per_km") val paceMinPerKm: Double?,
    val hr: Int?,
    @SerialName("elevation_m") val elevationM: Double?,
)

@Serializable
data class Vo2Estimate(
    val method: String,
    val value: Double,
    @SerialName("confidence_pct") val confidencePct: Int,
    val notes: String,
)

@Serializable
data class PeakKmResult(
    @SerialName("vo2_expressed") val vo2Expressed: Double,
    @SerialName("vo2max_est") val vo2MaxEst: Double?,
    @SerialName("pace_min_per_km") val paceMinPerKm: Double,
    @SerialName("avg_grade_pct") val avgGradePct: Double,
    @SerialName("avg_hr") val avgHr: Int?,
    @SerialName("start_distance_km") val startDistanceKm: Double,
)

@Serializable
data class DriftPoint(
    @SerialName("distance_km") val distanceKm: Double,
    val efficiency: Double,
)

@Serializable
data class DescentPoint(
    @SerialName("grade_pct") val gradePct: Double,
    @SerialName("speed_kmh") val speedKmh: Double,
    val progress: Double,
)

@Serializable
data class AnalysisResult(
    @SerialName("total_distance_km") val totalDistanceKm: Double = 0.0,
    @SerialName("total_duration_min") val totalDurationMin: Double = 0.0,
    @SerialName("avg_pace_min_per_km") val avgPaceMinPerKm: Double = 0.0,
    @SerialName("elevation_gain_m") val elevationGainM: Double = 0.0,
    @SerialName("avg_hr") val avgHr: Double? = null,
    @SerialName("max_hr_recorded") val maxHrRecorded: Int? = null,
    @SerialName("has_hr_data") val hasHrData: Boolean = false,
    @SerialName("has_elevation_data") val hasElevationData: Boolean = false,
    @SerialName("has_time_data") val hasTimeData: Boolean = false,
    @SerialName("point_count") val pointCount: Int = 0,
    val estimates: List<Vo2Estimate> = emptyList(),
    @SerialName("fitness_category") val fitnessCategory: String? = null,
    @SerialName("fitness_description") val fitnessDescription: String? = null,
    @SerialName("peak_1km") val peak1Km: PeakKmResult? = null,
    @SerialName("chart_points") val chartPoints: List<ChartPoint> = emptyList(),
    @SerialName("cardiac_drift") val cardiacDrift: List<DriftPoint> = emptyList(),
    @SerialName("decoupling_pct") val decouplingPct: Double? = null,
    @SerialName("descent_points") val descentPoints: List<DescentPoint> = emptyList(),
    val error: String? = null,
)
