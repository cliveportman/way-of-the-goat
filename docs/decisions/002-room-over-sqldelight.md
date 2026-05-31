# 002. Use Room for persistence in the native Android app

**Date:** 2026-05-31
**Status:** Proposed

> Follows [ADR 001](001-kmp-to-native-android-food-scoring.md), which leaves the persistence technology open.

## Context

The native Android rewrite ([ADR 001](001-kmp-to-native-android-food-scoring.md)) needs a local persistence layer. The app is now **Android-only, local, and offline**, and the data model is small — daily food servings, the derived scores, and the selected scoring suite/profile.

The current KMP app uses **SQLDelight** (the `DatabaseDriverFactory` expect/actual pattern with `.sq` files). That choice was driven by multiplatform: SQLDelight runs the same SQL on Android and iOS. With iOS dropped and the codebase deliberately simplifying to native Android conventions, that motivation no longer applies.

Two realistic options:

- **Keep SQLDelight** — reuse the existing schema and queries; least porting effort.
- **Switch to Room** — Google's first-party Android persistence library; the idiomatic native-Android choice.

## Decision

Adopt **Room** for the native Android app.

The `ServingsRepository` interface stays stable; only its implementation changes. The existing SQLDelight schema is small enough to re-express as Room entities + DAOs at low cost, and the scoring engine (pure Kotlin) is unaffected by the persistence choice either way.

This is a recommendation pending ratification — if minimising porting effort outweighs idiomatic fit for you, keeping SQLDelight is a reasonable alternative and this ADR can be flipped.

## Consequences

**Positive:**
- Idiomatic, first-party native Android — aligns with the `android-conventions`/`android-review-criteria` skills and the reviewer agents' (Nick Butcher / Jake Wharton) expertise.
- First-class Kotlin `Flow` and coroutine support integrates cleanly with the Compose + ViewModel/StateFlow UI.
- Mature migration support (`@Database` versioning), KSP codegen, and strong tooling/community.
- Sheds the multiplatform-era `DatabaseDriverFactory` indirection.

**Negative:**
- One-time porting cost: re-express the existing `.sq` schema and queries as Room `@Entity`/`@Dao`/`@Database`.
- Loses SQLDelight's SQL-first, compile-time-checked query ergonomics.

**Neutral:**
- The repository interface is unchanged, so the UI and scoring layers don't care which is used.
- On-device data migration from any currently-installed app is a separate concern, tied to the open "what's live on Play" question in the [implementation plan](../features/native-android-rewrite/plan.md) — not resolved by this ADR.
