#!/usr/bin/env bash
# Build the VO2 calculator with Zig and stage the WASM in pkg/.
set -euo pipefail

cd "$(dirname "$0")"

if ! command -v zig >/dev/null 2>&1; then
  echo "error: zig is not installed. Install with: brew install zig" >&2
  exit 1
fi

zig build -Drelease=true

mkdir -p pkg
cp zig-out/bin/vo2.wasm pkg/vo2.wasm

size=$(stat -f "%z" pkg/vo2.wasm 2>/dev/null || stat -c "%s" pkg/vo2.wasm)
echo "Built pkg/vo2.wasm — $size bytes ($(du -h pkg/vo2.wasm | cut -f1))"
echo "If this changed, update WASM_SIZE_BYTES at the top of main.js."
