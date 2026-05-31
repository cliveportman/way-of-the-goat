using System.Globalization;

namespace Vo2;

internal static class Gpx
{
    private const int MaxTrackPoints = 200_000;

    private static bool TryParseAttrDouble(string tag, string attr, out double value)
    {
        value = 0;
        var pat = attr + "=\"";
        var idx = tag.IndexOf(pat, StringComparison.Ordinal);
        if (idx < 0) return false;
        var rest = tag.AsSpan(idx + pat.Length);
        var end = rest.IndexOf('"');
        if (end < 0) return false;
        return double.TryParse(rest[..end], NumberStyles.Float, CultureInfo.InvariantCulture, out value);
    }

    private static ReadOnlySpan<char> LocalTagName(string tag)
    {
        if (tag.Length == 0) return ReadOnlySpan<char>.Empty;
        switch (tag[0])
        {
            case '/': case '!': case '?': return ReadOnlySpan<char>.Empty;
        }
        var span = tag.AsSpan();
        var ws = span.IndexOfAny([' ', '\t', '\r', '\n']);
        if (ws >= 0) span = span[..ws];
        span = span.TrimEnd('/');
        var colon = span.LastIndexOf(':');
        if (colon >= 0) span = span[(colon + 1)..];
        return span;
    }

    /// <summary>
    /// Byte-driven, namespace-tolerant GPX parser. Extracts trkpt elements
    /// and their ele/time/hr children; tolerates CDATA, comments, and
    /// arbitrary namespace prefixes. Mirrors the Rust implementation.
    /// </summary>
    public static List<TrackPoint> Parse(string content)
    {
        var points = new List<TrackPoint>(1024);
        TrackPoint? current = null;

        var i = 0;
        while (i < content.Length)
        {
            if (content[i] != '<') { i++; continue; }
            var tagStart = i + 1;

            // CDATA
            if (tagStart + 8 <= content.Length
                && content.AsSpan(tagStart, 8).SequenceEqual("![CDATA["))
            {
                var off = content.IndexOf("]]>", i, StringComparison.Ordinal);
                if (off < 0) throw new FormatException("Unterminated CDATA section");
                i = off + 3;
                continue;
            }
            // Comment
            if (tagStart + 3 <= content.Length
                && content.AsSpan(tagStart, 3).SequenceEqual("!--"))
            {
                var off = content.IndexOf("-->", i, StringComparison.Ordinal);
                if (off < 0) throw new FormatException("Unterminated comment");
                i = off + 3;
                continue;
            }

            var j = tagStart;
            while (j < content.Length && content[j] != '>') j++;
            if (j >= content.Length) break;
            var tag = content.Substring(tagStart, j - tagStart);
            i = j + 1;

            var local = LocalTagName(tag);

            if (local.SequenceEqual("trkpt"))
            {
                if (TryParseAttrDouble(tag, "lat", out var lat)
                    && TryParseAttrDouble(tag, "lon", out var lon))
                {
                    current = new TrackPoint { Lat = lat, Lon = lon };
                }
            }
            else if (tag.StartsWith("/trkpt", StringComparison.Ordinal))
            {
                if (current != null)
                {
                    points.Add(current);
                    current = null;
                    if (points.Count > MaxTrackPoints)
                    {
                        throw new FormatException(
                            $"GPX file exceeds {MaxTrackPoints} track points — file is too large to analyse");
                    }
                }
            }
            else if (current != null && local.Length > 0)
            {
                if (local.SequenceEqual("ele") || local.SequenceEqual("time") || local.SequenceEqual("hr"))
                {
                    var textStart = i;
                    while (i < content.Length && content[i] != '<') i++;
                    var text = content[textStart..i].Trim();
                    if (text.Length == 0) continue;

                    if (local.SequenceEqual("ele"))
                    {
                        if (double.TryParse(text, NumberStyles.Float, CultureInfo.InvariantCulture, out var ele))
                            current.Ele = ele;
                    }
                    else if (local.SequenceEqual("time"))
                    {
                        if (Timestamp.TryParse(text, out var t)) current.TimeS = t;
                    }
                    else // hr
                    {
                        if (uint.TryParse(text, NumberStyles.Integer, CultureInfo.InvariantCulture, out var hr))
                            current.Hr = hr;
                    }
                }
            }
        }

        return points;
    }
}
