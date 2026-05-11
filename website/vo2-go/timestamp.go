package vo2

import (
	"strconv"
	"strings"
)

func isLeap(year int64) bool {
	return (year%4 == 0 && year%100 != 0) || year%400 == 0
}

// parseTimestamp parses ISO 8601 timestamps like "2024-01-15T10:30:00Z"
// or "2024-01-15T10:30:00.000Z" into seconds since 2000-01-01.
// Returns ok=false on any parse or range error.
func parseTimestamp(s string) (float64, bool) {
	s = strings.TrimSpace(s)
	s = strings.TrimSuffix(s, "Z")
	dateStr, timeStr, ok := strings.Cut(s, "T")
	if !ok {
		return 0, false
	}

	dp := strings.Split(dateStr, "-")
	if len(dp) != 3 {
		return 0, false
	}
	year, err1 := strconv.ParseInt(dp[0], 10, 64)
	month, err2 := strconv.ParseInt(dp[1], 10, 64)
	day, err3 := strconv.ParseInt(dp[2], 10, 64)
	if err1 != nil || err2 != nil || err3 != nil {
		return 0, false
	}

	// Drop fractional seconds
	if dot := strings.IndexByte(timeStr, '.'); dot >= 0 {
		timeStr = timeStr[:dot]
	}
	tp := strings.Split(timeStr, ":")
	if len(tp) != 3 {
		return 0, false
	}
	hour, err4 := strconv.ParseFloat(tp[0], 64)
	min, err5 := strconv.ParseFloat(tp[1], 64)
	sec, err6 := strconv.ParseFloat(tp[2], 64)
	if err4 != nil || err5 != nil || err6 != nil {
		return 0, false
	}

	if year < 2000 || year > 2100 {
		return 0, false
	}
	if month < 1 || month > 12 {
		return 0, false
	}
	var maxDay int64
	switch month {
	case 2:
		if isLeap(year) {
			maxDay = 29
		} else {
			maxDay = 28
		}
	case 4, 6, 9, 11:
		maxDay = 30
	default:
		maxDay = 31
	}
	if day < 1 || day > maxDay {
		return 0, false
	}
	if hour < 0 || hour > 23 || min < 0 || min > 59 || sec < 0 || sec > 59 {
		return 0, false
	}

	var days int64
	for y := int64(2000); y < year; y++ {
		if isLeap(y) {
			days += 366
		} else {
			days += 365
		}
	}
	monthDays := [12]int64{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}
	for m := int64(0); m < month-1; m++ {
		days += monthDays[m]
		if m == 1 && isLeap(year) {
			days++
		}
	}
	days += day - 1

	return float64(days)*86400.0 + hour*3600.0 + min*60.0 + sec, true
}
