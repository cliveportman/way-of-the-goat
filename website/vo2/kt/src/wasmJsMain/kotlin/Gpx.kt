private const val MAX_TRACK_POINTS = 200_000

internal class ParseResult(val points: List<TrackPoint>, val error: String?)

private fun parseAttrDouble(tag: String, attr: String): Double? {
    val pat = "$attr=\""
    val idx = tag.indexOf(pat)
    if (idx < 0) return null
    val rest = tag.substring(idx + pat.length)
    val end = rest.indexOf('"')
    if (end < 0) return null
    return rest.substring(0, end).toDoubleOrNull()
}

private fun localTagName(tag: String): String {
    if (tag.isEmpty()) return ""
    when (tag[0]) { '/', '!', '?' -> return "" }
    var name = tag
    val ws = name.indexOfAny(charArrayOf(' ', '\t', '\r', '\n'))
    if (ws >= 0) name = name.substring(0, ws)
    name = name.trimEnd('/')
    val colon = name.lastIndexOf(':')
    if (colon >= 0) name = name.substring(colon + 1)
    return name
}

// Byte-driven, namespace-tolerant GPX parser. Mirrors the Rust/Go/.NET/JS/AS ports.
internal fun parseGpx(content: String): ParseResult {
    val points = ArrayList<TrackPoint>(1024)
    var current: TrackPoint? = null
    val n = content.length
    var i = 0
    while (i < n) {
        if (content[i] != '<') { i++; continue }
        val tagStart = i + 1

        if (content.startsWith("![CDATA[", tagStart)) {
            val off = content.indexOf("]]>", i)
            if (off < 0) return ParseResult(emptyList(), "Unterminated CDATA section")
            i = off + 3
            continue
        }
        if (content.startsWith("!--", tagStart)) {
            val off = content.indexOf("-->", i)
            if (off < 0) return ParseResult(emptyList(), "Unterminated comment")
            i = off + 3
            continue
        }

        var j = tagStart
        while (j < n && content[j] != '>') j++
        if (j >= n) break
        val tag = content.substring(tagStart, j)
        i = j + 1

        val local = localTagName(tag)

        if (local == "trkpt") {
            val lat = parseAttrDouble(tag, "lat")
            val lon = parseAttrDouble(tag, "lon")
            if (lat != null && lon != null) {
                current = TrackPoint(lat = lat, lon = lon)
            }
        } else if (tag.startsWith("/trkpt")) {
            val pt = current
            if (pt != null) {
                points.add(pt)
                current = null
                if (points.size > MAX_TRACK_POINTS) {
                    return ParseResult(
                        emptyList(),
                        "GPX file exceeds $MAX_TRACK_POINTS track points — file is too large to analyse",
                    )
                }
            }
        } else {
            val cur = current
            if (cur != null && local.isNotEmpty() && (local == "ele" || local == "time" || local == "hr")) {
                val textStart = i
                while (i < n && content[i] != '<') i++
                val text = content.substring(textStart, i).trim()
                if (text.isNotEmpty()) {
                    when (local) {
                        "ele" -> text.toDoubleOrNull()?.let { cur.ele = it }
                        "time" -> parseTimestamp(text)?.let { cur.timeS = it }
                        "hr" -> text.toIntOrNull()?.takeIf { it >= 0 }?.let { cur.hr = it }
                    }
                }
            }
        }
    }
    return ParseResult(points, null)
}
