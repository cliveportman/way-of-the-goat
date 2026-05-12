#!/usr/bin/env bash
# Build the Kotlin/Wasm bundle and stage it in dist/ for the page to load.
set -euo pipefail

cd "$(dirname "$0")"

./gradlew wasmJsBrowserProductionWebpack

src="build/kotlin-webpack/wasmJs/productionExecutable"
mkdir -p dist
rm -f dist/*.wasm dist/vo2.js dist/vo2.js.map

# Copy the JS shim under a stable name and the hashed .wasm. webpack writes
# the wasm filename into vo2.js, so we don't need to rename the wasm.
cp "$src/vo2.js" dist/
[ -f "$src/vo2.js.map" ] && cp "$src/vo2.js.map" dist/
cp "$src"/*.wasm dist/

wasm_file=$(find dist -maxdepth 1 -name '*.wasm' | head -1)
size=$(stat -f "%z" "$wasm_file" 2>/dev/null || stat -c "%s" "$wasm_file")
echo "Built $(basename "$wasm_file") — $size bytes"
echo "If this changed, update WASM_SIZE_BYTES at the top of main.js."
