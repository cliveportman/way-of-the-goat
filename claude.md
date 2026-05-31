# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Way of the Goat is a nutrition tracking app for endurance athletes, implementing food quality scoring based on the Racing Weight methodology by Matt Fitzgerald.

The mobile app is a **native Android** app (Kotlin + Jetpack Compose), scoped to food scoring only and fully local/offline. It is being migrated from the earlier Kotlin Multiplatform build — see [ADR 001](docs/decisions/001-kmp-to-native-android-food-scoring.md) and [the rewrite plan](docs/features/native-android-rewrite/plan.md).

**Repository Structure:**
```
way-of-the-goat/
├── .claude/          # Agents, commands, skills (see below)
├── references/       # Frozen porting references — not active builds
│   ├── kmp/          # Previous Kotlin Multiplatform app (Gradle build, has its own CLAUDE.md)
│   └── react-native/ # Original React Native v1.4.3 app
├── design-specs/     # Design specs and Figma JSON exports (shared across all sub-projects)
├── design-tokens/    # Figma-sourced token JSON (tokens.json)
├── api/              # Go backend (planned)
└── website/          # Static HTML site (active — Vercel deployment)
```

The new native Android module has not been scaffolded yet (Phase 3 of the [rewrite plan](docs/features/native-android-rewrite/plan.md)); until then the only mobile code in the tree lives under `references/` for porting.

## Agents

All agents are in `.claude/agents/`. Use the appropriate agent for the task:

| Agent | Use For | Model |
|-------|---------|-------|
| `jake-wharton` | Native Android (Kotlin + Compose) implementation from design specs | opus |
| `nick-butcher` | Native Android / Compose code review (read-only) | inherits |
| `rubber-duck` | Brainstorming, planning, architecture discussion (no production code) | sonnet |
| `claude-reviewer` | Review .claude/ changes — agents, skills, commands, hooks (read-only) | sonnet |
| `steve-klabnik` | Rust/WASM code review — correctness, safety, wasm-bindgen (read-only) | inherits |
| `addy-osmani` | Web HTML/CSS/JS review — semantics, a11y, performance, security (read-only) | inherits |

## Commands

| Command | Use For |
|---------|---------|
| `/commit` | Stage and commit changes with semantic prefix and contributor emoji |
| `/pr-create` | Create a draft PR and generate a test plan |
| `/review [pr-number]` | Auto-route review to nick-butcher, addy-osmani, steve-klabnik, or claude-reviewer based on changed files |
| `/review [pr-number] --recheck` | Re-review changed files only (mobile domain); full review for web/Rust/.claude |
| `/review --audit` | Full audit of entire `.claude/` directory via claude-reviewer |
| `/review --file <path>` | Review a single `.claude/` file via claude-reviewer |
| `/retro <feature-or-issue-name>` | Write a post-merge retrospective to `docs/` |
| `/design-to-code` | Full pipeline: Figma design → spec curation → Compose implementation |
| `/design-to-code --edit <spec-path>` | Update specs and code when a design changes |
| `/claude-update` | Fetch new Anthropic/competitor knowledge and propose updates to claude-review-criteria skill |

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
| `android-conventions` | Architecture, state management, file naming, Room, error handling, testing |
| `design-specs` | Spec file formats, Figma token → Compose mapping |
| `android-review-criteria` | Review checklist for native Android / Compose code |
| `web-review-criteria` | Review checklist for static HTML/CSS/JS and WASM loading code |
| `docs-conventions` | Documentation formats for `docs/` — features, issues, ADRs |
| `mermaid` | Mermaid flowchart conventions and colour scheme |
| `claude-review-criteria` | Review checklist for .claude/ changes — best practices, anti-patterns, competitor awareness |

## Documentation

Project documentation lives in the `docs/` directory:

```
docs/
├── features/{name}/     # research.md, plan.md, retro.md
├── issues/{name}/       # investigation.md, fix-plan.md, retro.md
└── decisions/           # NNN-short-title.md (Architecture Decision Records)
```

See `.claude/skills/docs-conventions/SKILL.md` for formats, naming, and which agents write what.

**Key rule:** `jake-wharton` and `nick-butcher` do **not** browse `docs/` proactively. They only read `plan.md` (or `fix-plan.md`) when a feature or issue is mentioned by name. The `rubber-duck` agent is the primary author of documentation.

## Reference Codebases

Two earlier codebases are kept for porting reference only — do not develop in them:

- **Kotlin Multiplatform app** — the previous Compose Multiplatform build at `references/kmp/` (self-contained Gradle project, frozen out of the active build). The native Android rewrite ports its scoring engine and Compose UI near-verbatim, so this is the primary porting source (see [the rewrite plan](docs/features/native-android-rewrite/plan.md)).
- **React Native v1.4.3** — the original codebase at `references/react-native/`. Use it for the original business logic and scoring algorithms:
  - `references/react-native/core/` — business logic, scoring algorithms, database schema
  - `references/react-native/app/` — screen components (Expo Router)
  - `references/react-native/features/` — feature-specific UI components

**RN → native Android pattern mappings:**
- TypeScript interfaces → Kotlin data classes
- React hooks → ViewModels with StateFlow
- `useEffect` → `LaunchedEffect`
- NativeWind classes → Compose Modifiers
