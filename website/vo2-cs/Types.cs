using System.Text.Json.Serialization;

namespace Vo2;

internal sealed class TrackPoint
{
    public double Lat { get; set; }
    public double Lon { get; set; }
    public double? Ele { get; set; }
    public double? TimeS { get; set; }
    public uint? Hr { get; set; }
}

public sealed class ChartPoint
{
    [JsonPropertyName("distance_km")] public double DistanceKm { get; set; }
    [JsonPropertyName("pace_min_per_km")] public double? PaceMinPerKm { get; set; }
    [JsonPropertyName("hr")] public uint? Hr { get; set; }
    [JsonPropertyName("elevation_m")] public double? ElevationM { get; set; }
}

public sealed class Vo2Estimate
{
    [JsonPropertyName("method")] public string Method { get; set; } = "";
    [JsonPropertyName("value")] public double Value { get; set; }
    [JsonPropertyName("confidence_pct")] public uint ConfidencePct { get; set; }
    [JsonPropertyName("notes")] public string Notes { get; set; } = "";
}

public sealed class PeakKmResult
{
    [JsonPropertyName("vo2_expressed")] public double Vo2Expressed { get; set; }
    [JsonPropertyName("vo2max_est")] public double? Vo2MaxEst { get; set; }
    [JsonPropertyName("pace_min_per_km")] public double PaceMinPerKm { get; set; }
    [JsonPropertyName("avg_grade_pct")] public double AvgGradePct { get; set; }
    [JsonPropertyName("avg_hr")] public uint? AvgHr { get; set; }
    [JsonPropertyName("start_distance_km")] public double StartDistanceKm { get; set; }
}

public sealed class DriftPoint
{
    [JsonPropertyName("distance_km")] public double DistanceKm { get; set; }
    [JsonPropertyName("efficiency")] public double Efficiency { get; set; }
}

public sealed class DescentPoint
{
    [JsonPropertyName("grade_pct")] public double GradePct { get; set; }
    [JsonPropertyName("speed_kmh")] public double SpeedKmh { get; set; }
    [JsonPropertyName("progress")] public double Progress { get; set; }
}

public sealed class AnalysisResult
{
    [JsonPropertyName("total_distance_km")] public double TotalDistanceKm { get; set; }
    [JsonPropertyName("total_duration_min")] public double TotalDurationMin { get; set; }
    [JsonPropertyName("avg_pace_min_per_km")] public double AvgPaceMinPerKm { get; set; }
    [JsonPropertyName("elevation_gain_m")] public double ElevationGainM { get; set; }
    [JsonPropertyName("avg_hr")] public double? AvgHr { get; set; }
    [JsonPropertyName("max_hr_recorded")] public uint? MaxHrRecorded { get; set; }
    [JsonPropertyName("has_hr_data")] public bool HasHrData { get; set; }
    [JsonPropertyName("has_elevation_data")] public bool HasElevationData { get; set; }
    [JsonPropertyName("has_time_data")] public bool HasTimeData { get; set; }
    [JsonPropertyName("point_count")] public int PointCount { get; set; }
    [JsonPropertyName("estimates")] public List<Vo2Estimate> Estimates { get; set; } = new();
    [JsonPropertyName("fitness_category")] public string? FitnessCategory { get; set; }
    [JsonPropertyName("fitness_description")] public string? FitnessDescription { get; set; }
    [JsonPropertyName("peak_1km")] public PeakKmResult? Peak1Km { get; set; }
    [JsonPropertyName("chart_points")] public List<ChartPoint> ChartPoints { get; set; } = new();
    [JsonPropertyName("cardiac_drift")] public List<DriftPoint> CardiacDrift { get; set; } = new();
    [JsonPropertyName("decoupling_pct")] public double? DecouplingPct { get; set; }
    [JsonPropertyName("descent_points")] public List<DescentPoint> DescentPoints { get; set; } = new();
    [JsonPropertyName("error")] public string? Error { get; set; }
}

// Source-generated JSON context so System.Text.Json works under trim/AOT
// without reflection.
[JsonSerializable(typeof(AnalysisResult))]
[JsonSerializable(typeof(List<Vo2Estimate>))]
[JsonSerializable(typeof(List<ChartPoint>))]
[JsonSerializable(typeof(List<DriftPoint>))]
[JsonSerializable(typeof(List<DescentPoint>))]
internal partial class Vo2JsonContext : JsonSerializerContext
{
}
