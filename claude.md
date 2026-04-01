# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Way of the Goat is a nutrition tracking app for endurance athletes, implementing food quality scoring based on the Racing Weight methodology by Matt Fitzgerald.

**Repository Structure:**
```
way-of-the-goat/
├── .claude/          # Agents, commands, skills (see below)
├── mobile/           # Kotlin Multiplatform app (active development) - has its own CLAUDE.md
├── design-specs/     # Design specs and Figma JSON exports (shared across all sub-projects)
├── design-tokens/    # Figma-sourced token JSON (tokens.json)
├── references/       # Legacy React Native v1.4.3 (reference only)
├── api/              # Go backend (planned)
└── website/          # Marketing site (planned)
```

## Agents

All agents are in `.claude/agents/`. Use the appropriate agent for the task:

| Agent | Use For | Model |
|-------|---------|-------|
| `jake-wharton` | KMP/Compose implementation from design specs | opus |
| `nick-butcher` | Compose/KMP code review (read-only) | inherits |
| `rubber-duck` | Brainstorming, planning, architecture discussion (no code) | sonnet |

## Commands

| Command | Use For |
|---------|---------|
| `/commit` | Stage and commit changes with semantic prefix and contributor emoji |
| `/app-review <pr-number>` | Full KMP/Compose code review of a PR via Nick Butcher agent |
| `/app-review <pr-number> --recheck` | Re-review changed files only, with follow-up on previous issues |
| `/design-to-code` | Full pipeline: Figma design → spec curation → Compose implementation |
| `/design-to-code --edit <spec-path>` | Update specs and code when a design changes |

### Design-to-Code Sub-commands

The `/design-to-code` command orchestrates these sub-commands internally:

| Sub-command | Purpose |
|-------------|---------|
| `design-to-code/curate-component-spec` | Figma JSON → component spec |
| `design-to-code/curate-screen-spec` | Conversation/JSON → screen spec |
| `design-to-code/curate-flow-spec` | Screenshots/conversation → flow + screen specs |
| `design-to-code/curate-flow-diagram` | Flow spec/screenshots → Mermaid diagram |
| `design-to-code/implement-spec` | Spec → production Compose code |

## Skills

Skills are in `.claude/skills/`. Agents read these before working:

| Skill | Contents |
|-------|----------|
| `kmp-conventions` | Architecture, state management, file naming, error handling, testing |
| `design-specs` | Spec file formats, Figma token → Compose mapping |
| `kmp-review-criteria` | Review checklist for KMP/Compose code |
| `mermaid` | Mermaid flowchart conventions and colour scheme |

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
