// End-to-end smoke test for the Zig WASM module.
// Exercises the full alloc/call/read/free ABI from a Node host.
//
// Run from the project root: node test/smoke.mjs

import { readFile } from 'node:fs/promises';

const wasmBytes = await readFile(new URL('../zig-out/bin/vo2.wasm', import.meta.url));
const { instance } = await WebAssembly.instantiate(wasmBytes, {});
const { memory, alloc, free, analyze_gpx, get_result_ptr } = instance.exports;

const enc = new TextEncoder();
const dec = new TextDecoder('utf-8');

function call(gpx, weight, maxHr) {
  const bytes = enc.encode(gpx);
  const ptr = alloc(bytes.length);
  if (ptr === 0) throw new Error('alloc returned null');
  new Uint8Array(memory.buffer, ptr, bytes.length).set(bytes);
  const outLen = analyze_gpx(ptr, bytes.length, weight, maxHr);
  if (outLen === 0) throw new Error('analyze_gpx returned 0 length');
  const outPtr = get_result_ptr();
  const jsonBytes = new Uint8Array(memory.buffer, outPtr, outLen).slice();
  free(ptr, bytes.length);
  return dec.decode(jsonBytes);
}

function synth(n) {
  let s = `<?xml version="1.0"?><gpx version="1.1"><trk><trkseg>`;
  for (let i = 0; i < n; i++) {
    const lon = (i * 0.00003).toFixed(6);
    const ele = (100 + 50 * Math.sin(i / 30)).toFixed(2);
    const hr = Math.round(140 + 20 * Math.sin(i / 40));
    const m = String(Math.floor(i / 60)).padStart(2, '0');
    const sec = String(i % 60).padStart(2, '0');
    s += `<trkpt lat="0.0" lon="${lon}"><ele>${ele}</ele><time>2024-01-01T00:${m}:${sec}Z</time><extensions><gpxtpx:TrackPointExtension><gpxtpx:hr>${hr}</gpxtpx:hr></gpxtpx:TrackPointExtension></extensions></trkpt>`;
  }
  s += `</trkseg></trk></gpx>`;
  return s;
}

const gpx = synth(1500);
const json = call(gpx, 70, 185);
const data = JSON.parse(json);

if (data.error) { console.error('error:', data.error); process.exit(1); }
if (data.point_count !== 1500) { console.error(`point_count: got ${data.point_count}, want 1500`); process.exit(1); }
if (!data.has_hr_data || !data.has_time_data || !data.has_elevation_data) {
  console.error('missing data flags', data);
  process.exit(1);
}
if (data.total_distance_km < 4.5 || data.total_distance_km > 5.5) {
  console.error(`total_distance_km out of range: ${data.total_distance_km}`);
  process.exit(1);
}
console.log('OK — points:', data.point_count, 'distance:', data.total_distance_km, 'km, estimates:', data.estimates.length);
