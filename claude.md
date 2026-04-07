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
└── website/          # Static HTML site (active — Vercel deployment)
```

## Agents

All agents are in `.claude/agents/`. Use the appropriate agent for the task:

| Agent | Use For | Model |
|-------|---------|-------|
| `jake-wharton` | KMP/Compose implementation from design specs | opus |
| `nick-butcher` | Compose/KMP code review (read-only) | inherits |
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
| `kmp-conventions` | Architecture, state management, file naming, error handling, testing |
| `design-specs` | Spec file formats, Figma token → Compose mapping |
| `kmp-review-criteria` | Review checklist for KMP/Compose code |
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
