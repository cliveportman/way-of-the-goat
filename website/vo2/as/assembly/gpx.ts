import { parseTimestamp } from './timestamp';

const MAX_TRACK_POINTS: i32 = 200_000;

// Track points use NaN for missing ele/time, and -1 for missing hr.
export class TrackPoint {
  lat: f64 = 0;
  lon: f64 = 0;
  ele: f64 = NaN;
  timeS: f64 = NaN;
  hr: i32 = -1;
}

function parseAttrFloat(tag: string, attr: string): f64 {
  const pat = attr + '="';
  const idx = tag.indexOf(pat);
  if (idx < 0) return NaN;
  const rest = tag.substring(idx + pat.length);
  const end = rest.indexOf('"');
  if (end < 0) return NaN;
  return parseFloat(rest.substring(0, end));
}

function localTagName(tag: string): string {
  if (tag.length == 0) return '';
  const c = tag.charCodeAt(0);
  if (c == 47 /* '/' */ || c == 33 /* '!' */ || c == 63 /* '?' */) return '';
  let name = tag;
  // Strip everything after first whitespace
  let ws = -1;
  for (let i = 0; i < name.length; i++) {
    const ch = name.charCodeAt(i);
    if (ch == 32 || ch == 9 || ch == 10 || ch == 13) { ws = i; break; }
  }
  if (ws >= 0) name = name.substring(0, ws);
  while (name.endsWith('/')) name = name.substring(0, name.length - 1);
  const colon = name.lastIndexOf(':');
  if (colon >= 0) name = name.substring(colon + 1);
  return name;
}

export class ParseResult {
  points: TrackPoint[];
  error: string | null;
  constructor(points: TrackPoint[], error: string | null) {
    this.points = points; this.error = error;
  }
}

export function parseGpx(content: string): ParseResult {
  const points: TrackPoint[] = [];
  let current: TrackPoint | null = null;

  let i: i32 = 0;
  const n: i32 = content.length;
  while (i < n) {
    if (content.charCodeAt(i) != 60 /* '<' */) { i++; continue; }
    const tagStart = i + 1;

    // CDATA: skip to ]]>
    if (content.startsWith('![CDATA[', tagStart)) {
      const off = content.indexOf(']]>', i);
      if (off < 0) return new ParseResult([], 'Unterminated CDATA section');
      i = off + 3;
      continue;
    }
    // Comment: skip to -->
    if (content.startsWith('!--', tagStart)) {
      const off = content.indexOf('-->', i);
      if (off < 0) return new ParseResult([], 'Unterminated comment');
      i = off + 3;
      continue;
    }

    let j = tagStart;
    while (j < n && content.charCodeAt(j) != 62 /* '>' */) j++;
    if (j >= n) break;
    const tag = content.substring(tagStart, j);
    i = j + 1;

    const local = localTagName(tag);

    if (local == 'trkpt') {
      const lat = parseAttrFloat(tag, 'lat');
      const lon = parseAttrFloat(tag, 'lon');
      if (!isNaN(lat) && !isNaN(lon)) {
        const pt = new TrackPoint();
        pt.lat = lat;
        pt.lon = lon;
        current = pt;
      }
    } else if (tag.startsWith('/trkpt')) {
      if (current != null) {
        points.push(current!);
        current = null;
        if (points.length > MAX_TRACK_POINTS) {
          return new ParseResult([],
            'GPX file exceeds ' + MAX_TRACK_POINTS.toString() + ' track points — file is too large to analyse');
        }
      }
    } else if (current != null && local.length > 0) {
      if (local == 'ele' || local == 'time' || local == 'hr') {
        const textStart = i;
        while (i < n && content.charCodeAt(i) != 60) i++;
        const text = content.substring(textStart, i).trim();
        if (text.length == 0) continue;
        if (local == 'ele') {
          const v = parseFloat(text);
          if (!isNaN(v)) current!.ele = v;
        } else if (local == 'time') {
          const v = parseTimestamp(text);
          if (!isNaN(v)) current!.timeS = v;
        } else {
          const v = <i32>parseInt(text, 10);
          if (v >= 0) current!.hr = v;
        }
      }
    }
  }

  return new ParseResult(points, null);
}
