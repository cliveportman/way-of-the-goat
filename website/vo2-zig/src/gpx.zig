const std = @import("std");
const timestamp = @import("timestamp.zig");

const MAX_TRACK_POINTS: usize = 200_000;

pub const TrackPoint = struct {
    lat: f64,
    lon: f64,
    ele: f64 = std.math.nan(f64),   // NaN = missing
    time_s: f64 = std.math.nan(f64),
    hr: i32 = -1,                   // -1 = missing
};

pub const ParseError = error{
    UnterminatedCdata,
    UnterminatedComment,
    TooManyPoints,
    OutOfMemory,
};

fn parseAttrFloat(tag: []const u8, attr: []const u8) f64 {
    // Build "<attr>=\"" inline by scanning.
    var i: usize = 0;
    while (i + attr.len + 2 <= tag.len) : (i += 1) {
        if (std.mem.startsWith(u8, tag[i..], attr) and
            tag[i + attr.len] == '=' and
            tag[i + attr.len + 1] == '"')
        {
            const start = i + attr.len + 2;
            const end = std.mem.indexOfScalarPos(u8, tag, start, '"') orelse return std.math.nan(f64);
            return std.fmt.parseFloat(f64, tag[start..end]) catch std.math.nan(f64);
        }
    }
    return std.math.nan(f64);
}

fn localTagName(tag: []const u8) []const u8 {
    if (tag.len == 0) return "";
    switch (tag[0]) {
        '/', '!', '?' => return "",
        else => {},
    }
    var name = tag;
    var ws: usize = 0;
    while (ws < name.len) : (ws += 1) {
        const c = name[ws];
        if (c == ' ' or c == '\t' or c == '\r' or c == '\n') {
            name = name[0..ws];
            break;
        }
    }
    while (name.len > 0 and name[name.len - 1] == '/') name = name[0 .. name.len - 1];
    if (std.mem.lastIndexOfScalar(u8, name, ':')) |colon| name = name[colon + 1 ..];
    return name;
}

pub fn parseGpx(allocator: std.mem.Allocator, content: []const u8) ParseError!std.ArrayList(TrackPoint) {
    var points: std.ArrayList(TrackPoint) = .empty;
    try points.ensureTotalCapacity(allocator, 1024);
    errdefer points.deinit(allocator);

    var current: ?TrackPoint = null;
    var i: usize = 0;
    const n = content.len;

    while (i < n) {
        if (content[i] != '<') { i += 1; continue; }
        const tag_start = i + 1;

        // CDATA
        if (tag_start + 8 <= n and std.mem.eql(u8, content[tag_start .. tag_start + 8], "![CDATA[")) {
            const off = std.mem.indexOfPos(u8, content, i, "]]>") orelse return error.UnterminatedCdata;
            i = off + 3;
            continue;
        }
        // Comment
        if (tag_start + 3 <= n and std.mem.eql(u8, content[tag_start .. tag_start + 3], "!--")) {
            const off = std.mem.indexOfPos(u8, content, i, "-->") orelse return error.UnterminatedComment;
            i = off + 3;
            continue;
        }

        var j: usize = tag_start;
        while (j < n and content[j] != '>') : (j += 1) {}
        if (j >= n) break;
        const tag = content[tag_start..j];
        i = j + 1;

        const local = localTagName(tag);

        if (std.mem.eql(u8, local, "trkpt")) {
            const lat = parseAttrFloat(tag, "lat");
            const lon = parseAttrFloat(tag, "lon");
            if (!std.math.isNan(lat) and !std.math.isNan(lon)) {
                current = TrackPoint{ .lat = lat, .lon = lon };
            }
        } else if (std.mem.startsWith(u8, tag, "/trkpt")) {
            if (current) |pt| {
                try points.append(allocator, pt);
                current = null;
                if (points.items.len > MAX_TRACK_POINTS) return error.TooManyPoints;
            }
        } else if (current != null and local.len > 0) {
            if (std.mem.eql(u8, local, "ele") or
                std.mem.eql(u8, local, "time") or
                std.mem.eql(u8, local, "hr"))
            {
                const text_start = i;
                while (i < n and content[i] != '<') : (i += 1) {}
                const text = std.mem.trim(u8, content[text_start..i], " \t\r\n");
                if (text.len > 0) {
                    if (std.mem.eql(u8, local, "ele")) {
                        if (std.fmt.parseFloat(f64, text)) |v| current.?.ele = v else |_| {}
                    } else if (std.mem.eql(u8, local, "time")) {
                        const v = timestamp.parseTimestamp(text);
                        if (!std.math.isNan(v)) current.?.time_s = v;
                    } else { // "hr"
                        if (std.fmt.parseInt(i32, text, 10)) |v| {
                            if (v >= 0) current.?.hr = v;
                        } else |_| {}
                    }
                }
            }
        }
    }

    return points;
}
