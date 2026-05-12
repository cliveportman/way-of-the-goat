import kotlinx.serialization.json.Json

private val json = Json { encodeDefaults = true; explicitNulls = true }

@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
fun analyze_gpx(content: String, weightKg: Double, maxHr: Int): String {
    val parsed = parseGpx(content)
    val result = if (parsed.error != null) {
        AnalysisResult(error = "Failed to parse GPX: ${parsed.error}")
    } else {
        analyze(parsed.points, weightKg, maxHr)
    }
    return json.encodeToString(AnalysisResult.serializer(), result)
}

fun main() {}
