---
name: docs-conventions
description: Formats and naming conventions for project documentation in docs/ — features, issues, and Architecture Decision Records.
---

# Docs Conventions

Formats and naming conventions for project documentation stored in the `docs/` directory. Read before writing any documentation file.

---

## Directory Structure

```
docs/
├── features/
│   └── {feature-name}/        # kebab-case, matches branch name or Notion title
│       ├── research.md        # Research & design findings
│       ├── plan.md            # Implementation plan (the one agents read)
│       └── retro.md           # Post-merge retrospective
├── issues/
│   └── {issue-name}/
│       ├── investigation.md   # Root cause analysis, repro steps
│       ├── fix-plan.md        # Proposed fix and tasks
│       └── retro.md           # Post-fix retrospective
└── decisions/
    └── NNN-short-title.md     # Architecture Decision Records
```

---

## Naming

- **Feature folders:** kebab-case, descriptive of the feature, e.g. `food-logging`, `past-day-profile-handling`
- **Issue folders:** kebab-case, descriptive of the problem, e.g. `score-calculation-off-by-one`, `stale-cache-on-profile-switch`
- **Decision files:** three-digit sequential number prefix + kebab-case title, e.g. `001-sqldelight-over-room.md`, `002-shared-viewmodel-for-flows.md`

---

## Feature Documentation

### `research.md` — Research & Design

Written during the exploration phase (typically by the rubber-duck agent during a brainstorming session).

```markdown
# {Feature Name}

**Status:** Research | Design | Ready for Planning
**Date:** {YYYY-MM-DD}

## Problem Statement

{What problem does this feature solve? Who is it for? Why does it matter?}

## Current State

{What exists today — relevant files, patterns, constraints}

## Design Decisions

{Options considered, trade-offs, and the chosen approach. Each significant decision should also get its own ADR in `docs/decisions/`.}

## Open Questions

- {Unanswered questions that need resolution before or during implementation}

## References

- {Links to Figma designs, design specs, external resources}
```

### `plan.md` — Implementation Plan

Written at the end of a planning session (typically by the rubber-duck agent). **This is the primary document that implementation and review agents read.**

```markdown
# {Feature Name} — Implementation Plan

**Status:** Planning | In Progress | Complete
**Date:** {YYYY-MM-DD}
**Branch:** {branch-name, if known}

## Goal

{1–2 sentences: what this implementation achieves}

## Phases

### Phase 1: {Name}

- [ ] {Task description}
- [ ] {Task description}

**Files to create/modify:**
- `{path}` — {what changes}

### Phase 2: {Name}

{Same structure}

## Testing Strategy

- {How to verify each phase}
- {Specific test scenarios}

## Dependencies

- {Other features, design specs, or API endpoints this depends on}

## Risks

- {Known risks and mitigations}
```

### `retro.md` — Retrospective

Written after merge (typically by the `/retro` command).

```markdown
# {Feature Name} — Retrospective

**Date:** {YYYY-MM-DD}
**PR:** {link}
**Duration:** {rough time from first commit to merge}

## What Happened

{Brief narrative: what was built, any scope changes from the plan}

## What Worked Well

- {Things worth repeating}

## What Didn't Work Well

- {Pain points, surprises, things that took longer than expected}

## Lessons Learned

- {Concrete takeaways for future work}

## Follow-up Items

- {Bugs discovered, technical debt introduced, future improvements}
```

---

## Issue Documentation

### `investigation.md`

```markdown
# {Issue Name}

**Severity:** Critical | High | Medium | Low
**Date discovered:** {YYYY-MM-DD}
**Source:** Testing | Code Review | User Report | Technical Debt

## Symptoms

{What the user or developer sees}

## Reproduction Steps

1. {Step 1}
2. {Step 2}

## Root Cause

{What's actually wrong — file paths, line references, explanation}

## Impact

{What's affected — which screens, which users, how often}
```

### `fix-plan.md`

Same format as `plan.md` above, but scoped to the fix.

### `retro.md`

Same format as the feature retro, scoped to the fix.

---

## Architecture Decision Records (ADRs)

ADRs capture significant, hard-to-reverse technical decisions. They're worth writing when:

- Choosing between competing technologies or libraries
- Establishing a pattern that the whole codebase will follow
- Making a data model decision that's expensive to change later
- Deliberately *not* doing something (recording the reasoning for future developers)

They're NOT needed for:
- Routine implementation choices
- Decisions that are easily reversible
- Style preferences

### ADR Format

```markdown
# {NNN}. {Decision Title}

**Date:** {YYYY-MM-DD}
**Status:** Proposed | Accepted | Deprecated | Superseded by {NNN}

## Context

{What is the situation? What forces are at play? What problem needs solving?}

## Decision

{What we decided to do, stated clearly.}

## Consequences

**Positive:**
- {Good things that follow from this decision}

**Negative:**
- {Trade-offs we're accepting}

**Neutral:**
- {Things that change but aren't clearly good or bad}
```

### ADR Numbering

- Sequential three-digit prefix: `001`, `002`, `003`
- To find the next number, read the `docs/decisions/` directory and increment from the highest existing number
- If superseding an earlier ADR, note it in the old ADR's status and link to the new one

---

## Who Writes What

| Document | Primary author | When |
|---|---|---|
| `research.md` | `rubber-duck` agent | During exploration/brainstorming session |
| `plan.md` | `rubber-duck` agent | End of planning session, before implementation |
| `retro.md` | `/retro` command | After merge |
| `investigation.md` | `rubber-duck` agent or developer | When a bug is being investigated |
| `fix-plan.md` | `rubber-duck` agent | Before fixing a bug |
| `decisions/*.md` | `rubber-duck` agent (proposes) | When a significant decision is reached |

---

## How Implementation and Review Agents Use Docs

**`jake-wharton` and `nick-butcher` do NOT browse `docs/` proactively.** They only read a specific document when:

1. The user mentions a feature by name → look for `docs/features/{name}/plan.md`
2. The user provides a plan path explicitly → read that file
3. A design spec references a feature → follow the link to the plan

They read **only `plan.md`** (or `fix-plan.md` for issues) — never the research or retro unless specifically asked. The plan is the contract; the research is background.
