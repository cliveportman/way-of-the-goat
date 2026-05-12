const std = @import("std");

pub fn build(b: *std.Build) void {
    const optimize = b.standardOptimizeOption(.{ .preferred_optimize_mode = .ReleaseSmall });

    const exe = b.addExecutable(.{
        .name = "vo2",
        .root_module = b.createModule(.{
            .root_source_file = b.path("src/main.zig"),
            .target = b.resolveTargetQuery(.{
                .cpu_arch = .wasm32,
                .os_tag = .freestanding,
            }),
            .optimize = optimize,
        }),
    });
    // No _start entry point — we expose individual functions.
    exe.entry = .disabled;
    // Export every function marked `pub` / `export`.
    exe.rdynamic = true;

    b.installArtifact(exe);
}
