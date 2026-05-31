// ISO 8601 timestamp parser. Returns seconds since 2000-01-01, or null.
// Handles "2024-01-15T10:30:00Z" and "2024-01-15T10:30:00.000Z".

function isLeap(year) {
  return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
}

const MONTH_DAYS = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

export function parseTimestamp(s) {
  s = s.trim();
  if (s.endsWith('Z')) s = s.slice(0, -1);

  const t = s.indexOf('T');
  if (t < 0) return null;
  const dateStr = s.slice(0, t);
  let timeStr = s.slice(t + 1);

  const dp = dateStr.split('-');
  if (dp.length !== 3) return null;
  const year = parseInt(dp[0], 10);
  const month = parseInt(dp[1], 10);
  const day = parseInt(dp[2], 10);
  if (Number.isNaN(year) || Number.isNaN(month) || Number.isNaN(day)) return null;

  const dot = timeStr.indexOf('.');
  if (dot >= 0) timeStr = timeStr.slice(0, dot);
  const tp = timeStr.split(':');
  if (tp.length !== 3) return null;
  const hour = parseFloat(tp[0]);
  const min = parseFloat(tp[1]);
  const sec = parseFloat(tp[2]);
  if (Number.isNaN(hour) || Number.isNaN(min) || Number.isNaN(sec)) return null;

  if (year < 2000 || year > 2100) return null;
  if (month < 1 || month > 12) return null;
  let maxDay;
  if (month === 2) maxDay = isLeap(year) ? 29 : 28;
  else if (month === 4 || month === 6 || month === 9 || month === 11) maxDay = 30;
  else maxDay = 31;
  if (day < 1 || day > maxDay) return null;
  if (hour < 0 || hour > 23 || min < 0 || min > 59 || sec < 0 || sec > 59) return null;

  let days = 0;
  for (let y = 2000; y < year; y++) days += isLeap(y) ? 366 : 365;
  for (let m = 0; m < month - 1; m++) {
    days += MONTH_DAYS[m];
    if (m === 1 && isLeap(year)) days += 1;
  }
  days += day - 1;

  return days * 86400 + hour * 3600 + min * 60 + sec;
}
