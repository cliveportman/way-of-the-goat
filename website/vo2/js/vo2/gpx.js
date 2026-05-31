import { parseTimestamp } from './timestamp.js';

const MAX_TRACK_POINTS = 200_000;

function parseAttrFloat(tag, attr) {
  const pat = attr + '="';
  const idx = tag.indexOf(pat);
  if (idx < 0) return null;
  const rest = tag.slice(idx + pat.length);
  const end = rest.indexOf('"');
  if (end < 0) return null;
  const v = parseFloat(rest.slice(0, end));
  return Number.isFinite(v) ? v : null;
}

function localTagName(tag) {
  if (tag.length === 0) return '';
  const c = tag[0];
  if (c === '/' || c === '!' || c === '?') return '';
  let name = tag;
  const ws = name.search(/[\s]/);
  if (ws >= 0) name = name.slice(0, ws);
  while (name.endsWith('/')) name = name.slice(0, -1);
  const colon = name.lastIndexOf(':');
  if (colon >= 0) name = name.slice(colon + 1);
  return name;
}

// Byte-driven, namespace-tolerant GPX parser. Extracts trkpt elements
// and their ele/time/hr children. Mirrors the Rust/Go/C# ports.
export function parseGpx(content) {
  const points = [];
  let current = null;

  let i = 0;
  while (i < content.length) {
    if (content.charCodeAt(i) !== 60 /* '<' */) { i++; continue; }
    const tagStart = i + 1;

    // CDATA
    if (content.startsWith('![CDATA[', tagStart)) {
      const off = content.indexOf(']]>', i);
      if (off < 0) throw new Error('Unterminated CDATA section');
      i = off + 3;
      continue;
    }
    // Comment
    if (content.startsWith('!--', tagStart)) {
      const off = content.indexOf('-->', i);
      if (off < 0) throw new Error('Unterminated comment');
      i = off + 3;
      continue;
    }

    let j = tagStart;
    while (j < content.length && content.charCodeAt(j) !== 62 /* '>' */) j++;
    if (j >= content.length) break;
    const tag = content.slice(tagStart, j);
    i = j + 1;

    const local = localTagName(tag);

    if (local === 'trkpt') {
      const lat = parseAttrFloat(tag, 'lat');
      const lon = parseAttrFloat(tag, 'lon');
      if (lat !== null && lon !== null) {
        current = { lat, lon, ele: null, timeS: null, hr: null };
      }
    } else if (tag.startsWith('/trkpt')) {
      if (current !== null) {
        points.push(current);
        current = null;
        if (points.length > MAX_TRACK_POINTS) {
          throw new Error(
            `GPX file exceeds ${MAX_TRACK_POINTS} track points — file is too large to analyse`
          );
        }
      }
    } else if (current !== null && local !== '') {
      if (local === 'ele' || local === 'time' || local === 'hr') {
        const textStart = i;
        while (i < content.length && content.charCodeAt(i) !== 60) i++;
        const text = content.slice(textStart, i).trim();
        if (text === '') continue;
        if (local === 'ele') {
          const v = parseFloat(text);
          if (Number.isFinite(v)) current.ele = v;
        } else if (local === 'time') {
          const v = parseTimestamp(text);
          if (v !== null) current.timeS = v;
        } else { // 'hr'
          const v = parseInt(text, 10);
          if (Number.isFinite(v) && v >= 0) current.hr = v;
        }
      }
    }
  }

  return points;
}
