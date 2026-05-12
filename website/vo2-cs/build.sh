#!/usr/bin/env bash
# Publish the VO2 calculator to WebAssembly with AOT and aggressive trim,
# then stage the deployable files into ../vo2/cs/ where they're served
# from `/vo2/cs` on the deployed site (and tracked in git).
set -euo pipefail

cd "$(dirname "$0")"

if ! command -v dotnet >/dev/null 2>&1; then
  echo "error: dotnet SDK is not installed. Install .NET 10+." >&2
  exit 1
fi

if ! dotnet workload list 2>/dev/null | grep -q wasm-tools; then
  echo "error: wasm-tools workload not installed. Run:" >&2
  echo "  sudo dotnet workload install wasm-tools" >&2
  exit 1
fi

rm -rf dist
dotnet publish -c Release -o ./dist

published="dist/wwwroot"
if [ ! -d "$published" ]; then
  echo "error: expected $published to exist after publish" >&2
  exit 1
fi

total_wasm=$(find "$published/_framework" -maxdepth 1 -name '*.wasm' \
  ! -name '*.br' ! -name '*.gz' \
  -exec stat -f "%z" {} \; 2>/dev/null \
  | awk '{s+=$1} END {print s+0}')
if [ -z "$total_wasm" ] || [ "$total_wasm" = "0" ]; then
  total_wasm=$(find "$published/_framework" -maxdepth 1 -name '*.wasm' \
    ! -name '*.br' ! -name '*.gz' \
    -exec stat -c "%s" {} \; \
    | awk '{s+=$1} END {print s+0}')
fi
echo "Total $published size: $(du -sh "$published" | cut -f1)"
echo "Total .wasm bytes in _framework: $total_wasm"
echo "If this changed, update WASM_SIZE_BYTES at the top of wwwroot/main.js."

# Stage to ../vo2/cs/ so the page is served at /vo2/cs alongside the
# other implementations.
staged="../vo2/cs"
rm -rf "$staged"
mkdir -p "$staged"
cp -R "$published"/. "$staged"/
echo "Staged deployable files into $staged"
