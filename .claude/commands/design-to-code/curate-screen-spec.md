Curate Screen Spec for: $ARGUMENTS

# Task: Curate Screen Spec

## Purpose

Create an implementation-ready **screen spec** for a standalone screen (not part of a multi-screen flow). Screens are Compose screens composed of child composables. Optionally accepts Figma JSON exports for layout analysis.

For multi-screen flows, use `/curate-flow-spec` instead — it creates screen specs as part of the flow.

## Prerequisites

- `.claude/skills/design-specs/SKILL.md` exists (defines all spec formats)
- `design-specs/components/` contains curated component specs to reference

---

## Workflow Steps

### Step 1: Inventory Existing Specs

**Silently** read all specs:

1. Read all files in `design-specs/components/*.md` — build a list of available component specs
2. Read all files in `design-specs/screens/*.md` — build a list of existing screen specs
3. Read `design-specs/tokens.json` if it exists

**Present a summary:**

```
## Available Component Library

I found {n} component specs:

**Components:** {comma-separated list}
**Existing Screens:** {list or "none yet"}

These are available to reference in your screen. Components not in this list will be flagged as needing new specs.
```

---

### Step 2: Describe the Screen

Ask the user:

```
Let's define this screen. I need to understand:

1. **What is this screen for?** What does the user see when they land here?
2. **What can the user do?** What actions and interactions are available?
3. **What navigates the user here?** (e.g. tapping a tab, completing a previous screen, deep link)
4. **Do you have a Figma JSON export?** If so, provide the path to the raw JSON file.
```

---

### Step 3: Analyse Figma JSON (if provided)

If the user provided a Figma JSON export:

1. Read the JSON and analyse the top-level frame
2. Identify overall layout: dimensions, padding, spacing, `layoutMode`
3. Find child elements and classify:
   - `type: "INSTANCE"` — match instance name against existing component specs
   - `type: "FRAME"` — pattern-match properties against existing specs
   - Group related elements into logical sections
4. Extract typography and colour tokens — map against `tokens.json`
5. Present findings:

```
## Figma JSON Analysis

**Screen dimensions:** {width} × {height} dp
**Layout:** {vertical/horizontal}, padding {values}, gap {value}

### Sections Found

| # | Section | Matched Component | Confidence |
|---|---------|-------------------|------------|
| 1 | {description} | [{component}](../components/{name}.md) | High — INSTANCE name matches |
| 2 | {description} | [{component}](../components/{name}.md) | Medium — layout pattern |
| 3 | {description} | **No match — needs new spec** | — |

### New Colours Found

| Hex | Suggested Token | Usage |
|-----|----------------|-------|
| #XXXXXX | {suggestion} | {where it appears} |

Does this mapping look right?
```

---

### Step 4: Map Sections to Components

For each screen section, confirm the mapping:

```
## Component Mapping

| Order | Section | Component Spec | Visibility |
|-------|---------|---------------|------------|
| 1 | {section} | [{component}](../components/{name}.md) | Always |
| 2 | {section} | [{component}](../components/{name}.md) | `loading` state only |
| 3 | {section} | **NEW — needs spec** | Always |

**Components needing new specs:** {list, or "None"}
```

---

### Step 5: Spec New Components Inline

For each unmatched section:

1. Extract the component's JSON subtree (if JSON was provided)
2. Run through the component spec workflow:
   - Analyse dimensions, colours, typography, layout
   - Map to design tokens
   - Identify states and interactions
3. Ask clarifying questions (token names, interactions, component name)
4. Generate the component spec → save to `design-specs/components/<name>.md`
5. Update `design-specs/tokens.json` if new tokens found
6. Resume screen curation with the new component now in the library

---

### Step 6: Identify Screen States

```
Does this screen have different states or variants?

Common examples:
- **default** — normal view with data
- **loading** — data is being fetched (skeleton or spinner)
- **empty** — no data to display
- **error** — something went wrong
- **editing** — user is in edit mode

For each state:
- Which sections appear or disappear?
- What content changes?
- Any visual differences?
```

---

### Step 7: Define Params and Callbacks

```
What data flows in and out of this screen?

**Params / Inputs** — data the screen receives:
- Navigation route params (e.g. date string, category ID)
- ViewModel-provided state (usually via StateFlow)

**Callbacks / Outputs** — events the screen fires:
- Navigation triggers (onNavigateBack, onNavigateToDetail)
- User actions passed to ViewModel (onServingTapped, onScoreChanged)
```

---

### Step 8: Generate Screen Spec

Using the format from `.claude/skills/design-specs/SKILL.md`:

```markdown
# {Screen Name}

**Type:** Screen
**Figma:** [TODO: Add Figma link]
**Created:** {YYYY-MM-DD}

## Overview

{1–2 sentences: what this screen shows and its purpose}

## Used In Flows

None — standalone screen.

## Layout

- **Structure:** {e.g. Scaffold with TopAppBar, LazyColumn content}
- **Content padding:** {dp values}
- **Section spacing:** {dp gap}

## Section Layout

| Order | Section | Component Spec | Visibility Condition |
|-------|---------|---------------|---------------------|
| 1 | {name} | [{component}](../components/{name}.md) | Always |
| 2 | {name} | [{component}](../components/{name}.md) | `loading` state only |

## Screen States

### `default`

{Which sections are visible, what data is shown}

### `loading`

{What changes — e.g. content replaced by skeleton composable}

## Props / Inputs

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| {name} | {Kotlin type} | {yes/no} | {what it controls} |

## Callbacks / Outputs

| Callback | Signature | Description |
|----------|-----------|-------------|
| {name} | `() -> Unit` | {when it fires} |
| {name} | `({type}) -> Unit` | {when it fires} |

## Design Tokens Used

Only tokens for the screen's own layout — not child component internals.

```json
{}
```

## Implementation Notes

- **Composition:** {how child composables are arranged}
- **Data source:** {ViewModel + StateFlow; what the ViewModel fetches}
- **Navigation:** {how to navigate to/from this screen}
- **Accessibility:** {focus management on load, screen reader landmarks}
```

---

### Step 9: Review and Save

Present the generated spec for review:

```
## Generated Screen Spec — Review

I've generated:
- `design-specs/screens/{screen-name}.md`

Take a look and let me know if anything needs correcting before I save.
```

Show full spec content. Wait for approval. Save the file.

---

### Step 10: Summarise

```
## Screen Spec Complete

**Created:**
- `design-specs/screens/{screen-name}.md`
{if inline component specs created:}
- `design-specs/components/{name}.md`

**Updated:**
- `design-specs/tokens.json` ({n} new tokens)

**Stats:**
- {n} sections mapped to components
- {n} screen states documented

**Next steps:**
1. Review spec for accuracy against Figma
2. Implement with `/design-to-code:implement-spec`
```

---

## Quality Checklist

- [ ] All screen sections reference a component spec (or flagged as TODO)
- [ ] Screen states fully documented (sections that appear/disappear)
- [ ] Params and callbacks cover all data flowing in and out
- [ ] Layout uses dp values (not absolute coordinates)
- [ ] Design tokens are screen-level only (not duplicating child component tokens)
- [ ] Implementation notes include data source, navigation, and accessibility
- [ ] File saved in `design-specs/screens/`
- [ ] Inline component specs saved in `design-specs/components/`
- [ ] `tokens.json` updated with new values

## Handling Edge Cases

### No Figma JSON available
Work from conversation. Use `[TODO: Confirm from Figma]` where precise measurements are needed.

### Component spec doesn't exist and user doesn't want inline speccing
Flag as `[TODO: Spec needed]` in the Section Layout table. List in summary.

### Screen is very simple (1–2 sections)
Generate full spec format for consistency. Note in Implementation Notes if the screen is simple enough to be a single composable function rather than a composed screen.
