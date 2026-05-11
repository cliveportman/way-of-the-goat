// JS↔WASM bridge for the VO2 calculator.
//
// ABI:
//   alloc(len) -> [*]u8        — allocate a writable buffer in WASM memory.
//   free(ptr, len)             — release a buffer alloc'd via alloc.
//   analyze_gpx(ptr, len, weight, max_hr) -> usize
//                              — run the analyser; returns the JSON length.
//                                The previous result (if any) is freed first.
//   get_result_ptr() -> [*]const u8 — pointer to the most recent JSON output.
//
// All bytes are UTF-8. The result string is owned by WASM until the next
// analyze_gpx call.

const std = @import("std");
const analyzer = @import("analyzer.zig");

// std.heap.wasm_allocator grows linear memory on demand via @wasmMemoryGrow.
const allocator = std.heap.wasm_allocator;

var last_result: ?[]u8 = null;

export fn alloc(len: usize) ?[*]u8 {
    const slice = allocator.alloc(u8, len) catch return null;
    return slice.ptr;
}

export fn free(ptr: [*]u8, len: usize) void {
    allocator.free(ptr[0..len]);
}

export fn analyze_gpx(
    input_ptr: [*]const u8,
    input_len: usize,
    weight_kg: f64,
    max_hr: i32,
) usize {
    if (last_result) |r| {
        allocator.free(r);
        last_result = null;
    }
    const input = input_ptr[0..input_len];
    const out = analyzer.run(allocator, input, weight_kg, max_hr) catch |err| blk: {
        const msg = std.fmt.allocPrint(allocator, "{{\"error\":\"Zig error: {s}\",\"estimates\":[],\"chart_points\":[]}}", .{@errorName(err)}) catch {
            return 0;
        };
        break :blk msg;
    };
    last_result = out;
    return out.len;
}

export fn get_result_ptr() ?[*]const u8 {
    if (last_result) |r| return r.ptr;
    return null;
}
