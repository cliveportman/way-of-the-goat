// Tiny streaming JSON encoder. Output goes into a std.ArrayList(u8).
//
// We hand-roll instead of using std.json so the code is portable across
// Zig stdlib revisions and so the WASM stays minimal (no JSON reflection
// machinery pulled in).

const std = @import("std");

pub const Writer = struct {
    buf: *std.ArrayList(u8),
    allocator: std.mem.Allocator,

    pub fn push(self: *Writer, s: []const u8) !void {
        try self.buf.appendSlice(self.allocator, s);
    }

    pub fn pushChar(self: *Writer, c: u8) !void {
        try self.buf.append(self.allocator, c);
    }

    pub fn writeString(self: *Writer, s: []const u8) !void {
        try self.pushChar('"');
        for (s) |c| {
            switch (c) {
                '"' => try self.push("\\\""),
                '\\' => try self.push("\\\\"),
                '\n' => try self.push("\\n"),
                '\r' => try self.push("\\r"),
                '\t' => try self.push("\\t"),
                else => {
                    if (c < 0x20) {
                        try self.push("\\u00");
                        try self.pushChar(if (c < 16) '0' else '1');
                        const lo: u8 = c & 0xF;
                        try self.pushChar(if (lo < 10) '0' + lo else 'a' + lo - 10);
                    } else {
                        try self.pushChar(c);
                    }
                },
            }
        }
        try self.pushChar('"');
    }

    pub fn writeInt(self: *Writer, v: i64) !void {
        var tmp: [32]u8 = undefined;
        const s = try std.fmt.bufPrint(&tmp, "{d}", .{v});
        try self.push(s);
    }

    // Number — preserves integer formatting when possible so the output
    // matches Rust serde, which emits 70 not 70.0 for whole f64 values.
    pub fn writeNumber(self: *Writer, v: f64) !void {
        if (std.math.isNan(v) or !std.math.isFinite(v)) {
            try self.push("null");
            return;
        }
        var tmp: [64]u8 = undefined;
        if (v == @floor(v) and @abs(v) < 1e16) {
            const as_i64: i64 = @intFromFloat(v);
            const s = try std.fmt.bufPrint(&tmp, "{d}", .{as_i64});
            try self.push(s);
            return;
        }
        const s = try std.fmt.bufPrint(&tmp, "{d}", .{v});
        try self.push(s);
    }

    pub fn writeNumberOrNull(self: *Writer, v: f64) !void {
        if (std.math.isNan(v)) {
            try self.push("null");
        } else {
            try self.writeNumber(v);
        }
    }

    pub fn writeIntOrNull(self: *Writer, v: i32) !void {
        if (v < 0) {
            try self.push("null");
        } else {
            try self.writeInt(@as(i64, v));
        }
    }
};
