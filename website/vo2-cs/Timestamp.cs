using System.Globalization;

namespace Vo2;

internal static class Timestamp
{
    private static bool IsLeap(long year) => (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;

    /// <summary>
    /// Parses an ISO 8601 timestamp like "2024-01-15T10:30:00Z" or
    /// "2024-01-15T10:30:00.000Z" into seconds since 2000-01-01.
    /// </summary>
    public static bool TryParse(string s, out double seconds)
    {
        seconds = 0;
        s = s.Trim();
        if (s.EndsWith('Z')) s = s[..^1];

        var tIdx = s.IndexOf('T');
        if (tIdx < 0) return false;
        var dateStr = s[..tIdx];
        var timeStr = s[(tIdx + 1)..];

        var dp = dateStr.Split('-');
        if (dp.Length != 3) return false;
        if (!long.TryParse(dp[0], NumberStyles.Integer, CultureInfo.InvariantCulture, out var year)
            || !long.TryParse(dp[1], NumberStyles.Integer, CultureInfo.InvariantCulture, out var month)
            || !long.TryParse(dp[2], NumberStyles.Integer, CultureInfo.InvariantCulture, out var day))
        {
            return false;
        }

        var dotIdx = timeStr.IndexOf('.');
        if (dotIdx >= 0) timeStr = timeStr[..dotIdx];

        var tp = timeStr.Split(':');
        if (tp.Length != 3) return false;
        if (!double.TryParse(tp[0], NumberStyles.Float, CultureInfo.InvariantCulture, out var hour)
            || !double.TryParse(tp[1], NumberStyles.Float, CultureInfo.InvariantCulture, out var min)
            || !double.TryParse(tp[2], NumberStyles.Float, CultureInfo.InvariantCulture, out var sec))
        {
            return false;
        }

        if (year < 2000 || year > 2100) return false;
        if (month < 1 || month > 12) return false;
        long maxDay = month switch
        {
            2 => IsLeap(year) ? 29 : 28,
            4 or 6 or 9 or 11 => 30,
            _ => 31,
        };
        if (day < 1 || day > maxDay) return false;
        if (hour < 0 || hour > 23 || min < 0 || min > 59 || sec < 0 || sec > 59) return false;

        long days = 0;
        for (long y = 2000; y < year; y++) days += IsLeap(y) ? 366 : 365;
        ReadOnlySpan<int> monthDays = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
        for (int m = 0; m < month - 1; m++)
        {
            days += monthDays[m];
            if (m == 1 && IsLeap(year)) days++;
        }
        days += day - 1;

        seconds = days * 86400.0 + hour * 3600.0 + min * 60.0 + sec;
        return true;
    }
}
