# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Way of the Goat is a nutrition tracking app for endurance athletes, implementing food quality scoring based on the Racing Weight methodology by Matt Fitzgerald.

**Repository Structure:**
```
way-of-the-goat/
├── mobile/          # Kotlin Multiplatform app (active development) - has its own CLAUDE.md
├── references/      # Legacy React Native v1.4.3 (reference only)
├── api/             # Go backend (planned)
└── website/         # Marketing site (planned)
```

## Documentation

**Use Notion for all project documentation.** Before starting work, search Notion for context:

- `mcp__notion__notion-search` - Find docs, requirements, decisions
- `mcp__notion__notion-fetch` - Read full page content
- Search terms: "Way of the Goat", "WOTG", feature names

### Feature Documentation Structure

For each new feature, sub-feature, or bug fix, create a page in the "Way of the Goat - Features" database with three sub-pages:

1. **Research & Design** - Current state, problem statement, design decisions
2. **Implementation Plan** - Phased changes, file-by-file breakdown, testing checklist
3. **Retrospective** - What went well, issues encountered, future improvements

See "Past Day Profile Handling" in Notion as an example.

## References Directory

The `references/` directory contains the production React Native v1.4.3 codebase. Use it as reference when migrating business logic to KMP:

- `references/core/` - Business logic, scoring algorithms, database schema
- `references/app/` - Screen components (Expo Router)
- `references/features/` - Feature-specific UI components

**Pattern mappings:**
- TypeScript interfaces → Kotlin data classes
- React hooks → ViewModels with StateFlow
- `useEffect` → `LaunchedEffect`
- NativeWind classes → Compose Modifiers
