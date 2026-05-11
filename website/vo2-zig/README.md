# VO2 Max Calculator — Zig build

Seventh implementation of the VO2 calculator. Same algorithm and JSON
shape as the Rust / TinyGo / .NET / pure-JS / AssemblyScript / Kotlin
ports. This one is written in Zig and compiled to `wasm32-freestanding`
with no runtime.

## Why Zig?

The interesting differences:

1. **Smallest WASM with no runtime**: **~88 KB** at `ReleaseSmall`.
   Smaller than Rust (130 KB), Kotlin (131 KB), TinyGo (448 KB), and .NET
   (8 MB). Only AssemblyScript (46 KB) is smaller — and that's because
   AS's GC adds a much heavier runtime cost.
2. **No auto-generated JS shim**: Zig produces a bare WASM module.
   Strings are marshalled by hand across linear memory — `alloc`, `free`,
   `analyze_gpx`, `get_result_ptr` are the exported ABI. ~30 lines of JS
   glue in `main.js`.
3. **Errors are values**: parser failures propagate as Zig error unions
   rather than exceptions or sentinels, matching the Rust port's idioms
   most closely.

## Prerequisites

- [Zig](https://ziglang.org/download/) 0.15+

```bash
brew install zig
```

## Building

```bash
./build.sh
```

This runs `zig build -Drelease=true` and copies the WASM to `pkg/`.
First build downloads nothing — Zig is a self-contained toolchain.

## Serving

```bash
python3 -m http.server 8086
```

Then open `http://localhost:8086`.

## Layout

```
vo2-zig/
├── src/
│   ├── main.zig           # Exports: alloc, free, analyze_gpx, get_result_ptr
│   ├── analyzer.zig       # Segments, VO2 methods, drift, descent + JSON serialiser
│   ├── gpx.zig            # Hand-rolled GPX parser
│   ├── timestamp.zig      # ISO 8601 timestamp parser
│   ├── geo.zig            # Haversine
│   ├── formulas.zig       # ACSM, Daniels, fitness, rounding helpers
│   └── json_writer.zig    # Minimal streaming JSON encoder
├── test/smoke.mjs         # End-to-end smoke test (node test/smoke.mjs)
├── build.zig
├── build.sh
├── pkg/vo2.wasm           # Committed build output
├── index.html
├── main.js
└── style.css
```

## ABI design

Strings cross the WASM boundary as `(pointer, length)` pairs. The JS side:

1. `alloc(input_len)` returns a pointer into WASM linear memory.
2. JS copies the UTF-8 GPX bytes into that buffer.
3. `analyze_gpx(ptr, len, weight, max_hr)` runs the analyser and returns
   the JSON byte length. The previous result is freed first.
4. `get_result_ptr()` returns the pointer to the JSON output.
5. JS reads `out_len` bytes from `memory.buffer` at that pointer and
   decodes them as UTF-8.
6. JS calls `free(input_ptr, input_len)` to release the input buffer.

The result string is owned by the WASM module until the next
`analyze_gpx` call. The `.slice()` on the Uint8Array in `main.js`
detaches a copy so the bytes survive subsequent allocations.

## Notes

- The smoke test (`node test/smoke.mjs`) instantiates the WASM directly
  via the Node `WebAssembly` API and exercises the full pipeline on a
  synthetic 1500-point GPX.
- Uses `std.heap.wasm_allocator`, which grows linear memory via
  `@wasmMemoryGrow` on demand. No fixed-size buffer limits.
