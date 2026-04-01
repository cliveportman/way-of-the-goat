Curate a multi-screen user flow spec for: $ARGUMENTS

# Task: Curate Flow Spec

## Purpose

Capture a multi-screen user flow as an implementation-ready spec. Flows span multiple screens with possible branching logic, shared state, and screen reuse. The output is a **flow spec** (the journey narrative and navigation logic) plus **screen specs** (one per unique screen layout).

Screenshot-first — users typically drop Figma screenshots and the flow is extracted from them, then confirmed conversationally. Falls back to a fully conversational walkthrough when screenshots aren't available.

## Prerequisites

- `.claude/skills/design-specs/SKILL.md` exists (defines all spec formats)
- `design-specs/components/` contains curated component specs to reference

**Specs root:** `design-specs/`

---

## Workflow Steps

### Step 1: Inventory Existing Specs

**Silently** read all specs:

1. `design-specs/components/*.md` — available component specs
2. `design-specs/screens/*.md` — existing screen specs
3. `design-specs/flows/*.md` — existing flow specs
4. `design-specs/tokens.json`

**Present summary:**

```
## Available Component Library

I found {n} component specs:

**Components:** {comma-separated list}
**Existing Screens:** {list or "none yet"}
**Existing Flows:** {list or "none yet"}
```

---

### Step 2: Gather the Happy Path

**Screenshots are the primary input mode.** Prompt for them first:

```
Let's map the happy path. Drop your Figma screenshots of the flow — annotated screens
showing the journey from start to finish work best (labelled screens, arrows between
steps, notes on branching or validation).

I'll extract:
- Screen names and order
- UI components (matched against your component library)
- Navigation and branching
- Validation states and screen variants

If you don't have screenshots, walk me through the flow verbally instead.
```

#### Screenshot Path (primary)

When screenshots are provided:

1. Analyse each screenshot in order. Extract:
   - **Screen names** — from labels, frame titles, or headings
   - **UI components** — identify cards, buttons, fields, etc. and match against component library
   - **Navigation flow** — arrows, numbered sequences, visual connections
   - **Branching points** — decision paths, labelled arrows ("Yes"/"No", conditional annotations)
   - **Validation states** — error messages, disabled states, loading indicators
   - **Screen variants** — same layout appearing with different states

2. Present extracted flow for confirmation:

```
## Screenshot Analysis

I've extracted the following flow:

**Entry:** {trigger} from {origin}

1. **{Screen name}** — {what user sees}
   - Components spotted: {matched list}
   - User action: {action} → step 2
2. **{Screen name}** — {what user sees}
   ...
n. **{Screen name}** — flow complete → {destination}

**Branches detected:**
- At step {n}: {description}

**Validation states detected:**
- {screen}: `error` state — {description}

**Components not in library:**
- {description} on {screen} — [TODO: Spec needed]

Does this look right?
```

3. Confirm conversationally. Any gaps identified feed into subsequent steps.

#### Conversation Path (fallback)

```
Let's map the happy path first.

1. **Where does the user come from?**
2. **What triggers the flow?**
3. **Walk me through each screen in order:**
   - What does the user see?
   - What do they do?
   - Where do they go next?
4. **Where does the user end up when complete?**
```

Summarise as numbered steps and confirm before proceeding.

---

### Step 3: Identify Screen Reuse and Variants

**If screenshots provided:** Present detected groupings:

```
From the screenshots I've identified these screen groupings:

1. **{screen-name}** — appears in steps {list}
   - States: `default` (step X), `with-validation` (step Y)
2. **{screen-name}** — appears in step {n} only

This means I'll create {count} screen specs. Does this look right?
```

**If conversation-only:** Ask about screen reuse:

```
Do any steps share the same underlying screen layout?
For example, steps 2 and 5 might both be a form screen but step 5 has extra validation.
```

---

### Step 4: Map Branching Logic

**If screenshots provided:** Present detected branches and ask for gaps:

```
From the screenshots I detected:

- **Step {n}:** {branch description}

For each step, are there any branches I'm missing?
- **Error paths:** What if something fails?
- **Escape routes:** Can the user cancel or go back?
```

**If conversation-only:** Walk through branching from scratch.

Present the full branching as an ASCII flow diagram and confirm:

```
## Flow Diagram

  [Entry: {trigger}]
        │
        ▼
  ┌─────────────┐
  │ 1. {screen} │
  └──────┬──────┘
         │ (user action)
    ┌────┴────┐
    ▼         ▼
  [Path A]  [Path B]
    │         │
    ▼         ▼
  [Exit: {destination}]

Does this capture all paths?
```

---

### Step 5: Identify Shared State

**If screenshots provided:** Infer shared state from visible data:

```
Based on the screens, data that likely flows between steps:

| Key | Type | Description | Set By | Used By |
|-----|------|-------------|--------|---------|
| {key} | {Kotlin type} | {inferred} | Step {n} | Steps {list} |

Things I can't tell from screenshots:
- Does any data come from route params?
- Is anything loaded from an API at the start?

Does this cover all shared data?
```

**If conversation-only:** Ask about state from scratch with the same table structure.

---

### Step 6: Map Components per Screen

For each unique screen, confirm the component mapping. Optionally request Figma JSON for precise layout:

```
I'd like more precise layout details for {screen-name}. Could you export the Figma JSON?
```

For unmatched sections, offer to spec inline (same process as `curate-component-spec` workflow). If the user declines, flag as `[TODO: Spec needed]`.

Present component mapping summary and confirm.

---

### Step 7: Generate Specs

Generate all files. **Screen specs first** (flow spec references them), then the flow spec.

#### Screen Spec Template

For each unique screen, `design-specs/screens/<screen-name>.md`:

````markdown
# {Screen Name}

**Type:** Screen
**Figma:** [TODO: Add Figma link]
**Created:** {YYYY-MM-DD}

## Overview

{1–2 sentences: role in the flow}

## Used In Flows

- [{flow-name}](../flows/{flow-name}.md) — Steps {list}

## Layout

- **Structure:** {e.g. Scaffold, Column}
- **Content padding:** {dp values}
- **Section spacing:** {dp gap}

## Section Layout

| Order | Section | Component Spec | Visibility Condition |
|-------|---------|---------------|---------------------|
| 1 | {name} | [{component}](../components/{name}.md) | Always |
| 2 | {name} | [TODO: Spec needed] | `error` state only |

## Screen States

### `default`
{Description}

### `{variant}`
{What changes from default}

## Props / Inputs

| Param | Type | Required | Description |
|-------|------|----------|-------------|

## Callbacks / Outputs

| Callback | Signature | Description |
|----------|-----------|-------------|

## Design Tokens Used

```json
{}
```

## Implementation Notes

- **Composition:** {child composable arrangement}
- **Data source:** {ViewModel + StateFlow}
- **Navigation:** {route to this screen}
- **Accessibility:** {focus management}
````

#### Flow Spec Template

`design-specs/flows/<flow-name>.md`:

````markdown
# {Flow Name}

**Type:** Flow
**Figma:** [TODO: Add Figma link]
**Created:** {YYYY-MM-DD}

## Overview

{2–4 sentences: what the user accomplishes, why this flow exists}

## Entry Points

| Origin | Trigger | Initial Step |
|--------|---------|-------------|
| {origin} | {trigger} | Step 1 |

## Shared State

| Key | Type | Description | Set By | Used By |
|-----|------|-------------|--------|---------|

## Flow Steps

### Step 1: {Step Name}

**Screen:** [{screen-name}](../screens/{screen-name}.md)
**Screen State:** `default`
**Purpose:** {what the user does}

| Action | Condition | Next Step | Side Effects |
|--------|-----------|-----------|-------------|
| {action} | {condition or "always"} | Step 2 | {changes} |
| Back | always | Exit: {destination} | — |

{...all steps}

## Exit Points

| Exit | From Step | Condition | Destination |
|------|-----------|-----------|-------------|

## Error Handling

| Error Condition | Occurs At | Recovery Path |
|----------------|-----------|---------------|

## Flow Diagram

```
{ASCII diagram from Step 4}
```

## Implementation Notes

- **Navigation:** {NavController route structure, e.g. `food/{date}/step1`}
- **State management:** {Shared ViewModel, route params, or DataManager}
- **Back behaviour:** {what happens when user presses system back at each step}
- **Deep linking:** {can users navigate directly to a step?}
````

---

### Step 8: Review and Save

Present all generated files for review. Show each file's content. Wait for approval or corrections. Then save all files.

---

### Step 9: Summarise

```
## Flow Spec Complete

**Created:**
- `design-specs/flows/{flow-name}.md`
- `design-specs/screens/{screen-1}.md`
- `design-specs/screens/{screen-2}.md`
...

**Stats:**
- {n} total steps
- {n} unique screens
- {n} exit points
- {n} shared state keys

**Component Gaps:**
- {list of [TODO: Spec needed] items, or "None"}

**Next steps:**
1. Review specs for accuracy
2. Create missing component specs with `/curate-component-spec`
3. Implement with `/design-to-code:implement-spec`
```

---

## Quality Checklist

- [ ] All steps in the happy path captured
- [ ] Screen reuse identified — no duplicate screen specs for same layout
- [ ] All branches and error paths mapped
- [ ] Shared state documented (set by / used by)
- [ ] Every screen section references a component spec (or flagged TODO)
- [ ] Flow spec has correct relative links to screen specs
- [ ] Screen specs have correct relative links to component specs
- [ ] ASCII flow diagram matches the step definitions
- [ ] Exit points cover all ways a user can leave the flow
- [ ] Implementation notes include navigation structure, state management, back behaviour
- [ ] Files saved: `flows/` for flow spec, `screens/` for screen specs

## Handling Edge Cases

### User doesn't know exact branching yet
Use `[TODO: Confirm branching logic]` placeholders and note in summary.

### Screen appears in multiple flows
Don't recreate it. Add the new flow to the existing screen spec's "Used In Flows" section.

### Flow is too complex (>15 steps)
Suggest splitting into sub-flows. Each gets its own spec referencing the others.
