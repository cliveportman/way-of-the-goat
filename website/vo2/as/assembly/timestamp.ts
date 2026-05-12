// Returns NaN on parse failure (signals "no value").

function isLeap(year: i32): bool {
  return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
}

const MONTH_DAYS: StaticArray<i32> = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

export function parseTimestamp(input: string): f64 {
  let s = input.trim();
  if (s.endsWith('Z')) s = s.substring(0, s.length - 1);

  const t = s.indexOf('T');
  if (t < 0) return NaN;
  const dateStr = s.substring(0, t);
  let timeStr = s.substring(t + 1);

  const dp = dateStr.split('-');
  if (dp.length != 3) return NaN;
  const year = <i32>parseInt(dp[0], 10);
  const month = <i32>parseInt(dp[1], 10);
  const day = <i32>parseInt(dp[2], 10);
  if (year == 0 && dp[0] != '0') return NaN; // crude parse-failure detection
  // (parseInt returns 0 for unparseable in AS; the above guards against false zero)

  const dot = timeStr.indexOf('.');
  if (dot >= 0) timeStr = timeStr.substring(0, dot);
  const tp = timeStr.split(':');
  if (tp.length != 3) return NaN;
  const hour = parseFloat(tp[0]);
  const min = parseFloat(tp[1]);
  const sec = parseFloat(tp[2]);
  if (isNaN(hour) || isNaN(min) || isNaN(sec)) return NaN;

  if (year < 2000 || year > 2100) return NaN;
  if (month < 1 || month > 12) return NaN;
  let maxDay: i32;
  if (month == 2) maxDay = isLeap(year) ? 29 : 28;
  else if (month == 4 || month == 6 || month == 9 || month == 11) maxDay = 30;
  else maxDay = 31;
  if (day < 1 || day > maxDay) return NaN;
  if (hour < 0.0 || hour > 23.0 || min < 0.0 || min > 59.0 || sec < 0.0 || sec > 59.0) return NaN;

  let days: i32 = 0;
  for (let y: i32 = 2000; y < year; y++) days += isLeap(y) ? 366 : 365;
  for (let m: i32 = 0; m < month - 1; m++) {
    days += MONTH_DAYS[m];
    if (m == 1 && isLeap(year)) days += 1;
  }
  days += day - 1;

  return <f64>days * 86400.0 + hour * 3600.0 + min * 60.0 + sec;
}
