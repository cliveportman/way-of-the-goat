Curate Screen Spec for: $ARGUMENTS

# Task: Curate Screen Spec

## Purpose

Create an implementation-ready **screen spec** for a standalone screen (not part of a multi-screen flow). Screens are compositions of child components arranged in a layout. This command is conversation-driven and optionally accepts Figma JSON exports for layout analysis.

For multi-screen flows, use `/curate-flow-spec` instead — it creates screen specs as part of the flow.

## Prerequisites

- `.claude/design-specs-guide.md` exists (defines all spec formats)
- `design-specs/` directory exists with a `components/` folder containing curated component specs

## Workflow Steps

### Step 1: Inventory Existing Specs

**Silently** read all specs:

1. Read all files in `design-specs/components/*.md` — build a list of available component specs
2. Read all files in `design-specs/screens/*.md` — build a list of existing screen specs
3. Read `design-specs/tokens.json` if it exists

**Present a summary to the user:**

```
## Available Component Library

I found {n} component specs:

**Components:** {comma-separated list of component names}

**Existing Screens:** {list or "none yet"}

These are available to reference in your screen. Components not in this list will be flagged as needing new specs.
```

### Step 2: Describe the Screen

Ask the user these questions:

```
Let's define this screen. I need to understand:

1. **What is this screen for?** What does the user see when they land here?
2. **What can the user do?** What actions/interactions are available?
3. **What navigation route does it live at?** (e.g. `Screen.Settings.Profile`, `Screen.FoodLog.Detail`)
4. **Do you have a Figma JSON export for this screen?** If so, provide the path to the raw JSON file.
```

Wait for the user's response before proceeding.

### Step 3: Analyze Figma JSON (if provided)

If the user provided a Figma JSON export:

1. **Read the JSON file** and analyze the top-level frame
2. **Identify the overall layout:**
   - Screen dimensions (width × height)
   - Content padding and spacing in dp
   - Alignment patterns (layoutMode, primaryAxisAlignItems, counterAxisAlignItems)
3. **Find child elements and classify them:**
   - Elements with `type: "INSTANCE"` — match the instance name against existing component specs
   - Elements with `type: "FRAME"` — pattern-match frame properties (dimensions, fills, strokes, children) against existing component specs
   - Group related elements into logical sections
4. **Extract typography and color tokens:**
   - Collect unique text styles (fontSize, fontName, color)
   - Collect unique fill/stroke colors
   - Map against existing `tokens.json`
5. **Present findings:**

```
## Figma JSON Analysis

**Screen dimensions:** {width} × {height}
**Layout:** {vertical/horizontal}, padding {values in dp}, gap {value in dp}

### Sections Found

| # | Section | Matched Component | Confidence |
|---|---------|-------------------|------------|
| 1 | {description} | [{component-name}](../components/{name}.md) | High — INSTANCE name matches |
| 2 | {description} | [{component-name}](../components/{name}.md) | Medium — layout pattern matches |
| 3 | {description} | **No match** — needs new spec | — |

### New Colors Found

| Hex | Suggested Token | Usage |
|-----|----------------|-------|
| #XXXXXX | {suggestion} | {where it appears} |

Does this mapping look right? Any corrections?
```

Wait for confirmation.

**If no JSON was provided**, skip to Step 4 and work from conversation only.

### Step 4: Map Sections to Components

For each screen section (from JSON analysis or conversation):

- **Match to existing component spec** — confirm with the user
- **Flag as new component** — needs a spec before the screen can be fully specced

Present the mapping:

```
## Component Mapping

| Order | Section | Component Spec | Visibility |
|-------|---------|---------------|------------|
| 1 | {section name} | [{component}](../components/{name}.md) | Always |
| 2 | {section name} | [{component}](../components/{name}.md) | `{state}` state only |
| 3 | {section name} | **NEW — needs spec** | Always |

**Components needing new specs:** {list, or "None — all sections match existing components"}

Does this mapping look right?
```

Wait for confirmation.

### Step 5: Spec New Components Inline

For each unmatched section that needs a new component spec:

1. **Extract the component's JSON subtree** from the screen export (if JSON was provided)
2. **Run through the component spec workflow** (same analysis as `/curate-component-spec`):
   - Analyze dimensions, colors, typography, layout patterns
   - Map to design tokens
   - Identify states and interactions
3. **Ask the user clarifying questions** for the component:
   - Semantic token names for new colors
   - Interaction behaviour and states
   - Component name (suggest based on frame name or content)
4. **Generate the component spec** and save to `design-specs/components/<name>.md`
5. **Update tokens.json** if new tokens were found
6. **Resume screen curation** with the new component now available in the library

Repeat for each unmatched component. If there are no unmatched components, skip this step.

### Step 6: Identify Screen States

Ask about screen variants:

```
Does this screen have different states or variants?

Common examples:
- **default** — normal view
- **loading** — data is being fetched (show skeleton or spinner)
- **empty** — no data to display
- **error** — something went wrong
- **editing** — user is in edit mode

For each state:
- Which sections appear or disappear?
- What content changes?
- Are there any visual differences (background, spacing)?
```

Wait for the user's response.

### Step 7: Define Parameters and Callbacks

Ask about data flow:

```
What data flows in and out of this screen?

**Parameters / Inputs** — data the composable receives:
- Navigation arguments (e.g. `userId: String` from the nav route)
- ViewModel state (if passed from a parent composable)
- Lambda callbacks from parent

**Callbacks / Outputs** — events the screen fires:
- `onNavigateBack: () -> Unit` — back navigation
- `onSubmit: (Data) -> Unit` — form submission
- `onNavigateTo: (Screen) -> Unit` — navigation requests
```

Suggest parameters/callbacks based on the section mapping. For example:
- If there's a form section → suggest an `onSubmit` callback
- If there's a list section → suggest an `onItemSelected` callback
- If the route has path arguments → suggest corresponding parameters

Wait for the user's response.

### Step 8: Generate Screen Spec

Produce the screen spec using the format from `.claude/design-specs-guide.md`:

```markdown
# {Screen Name}

**Type:** Screen
**Figma:** {link if provided, otherwise [TODO: Add Figma link]}
**Created:** {current date YYYY-MM-DD}

## Overview

{1–2 sentence description of what this screen shows and its purpose}

## Used In Flows

None — standalone screen.

## Layout

- **Structure:** {e.g. `Column(modifier = Modifier.fillMaxSize())`, single scrollable column}
- **Content padding:** `Modifier.padding(horizontal = {n}.dp, vertical = {n}.dp)`
- **Section gap:** `Arrangement.spacedBy({n}.dp)`

## Section Layout

| Order | Section | Component Spec | Visibility Condition |
|-------|---------|---------------|---------------------|
| 1 | {section name} | [{component-name}](../components/{component-name}.md) | Always |
| 2 | {section name} | [{component-name}](../components/{component-name}.md) | `{state}` state only |

## Screen States

### `default`

{Description of the default appearance — which sections are visible, what data is shown}

### `{variant-name}`

{Description of what changes from default — which sections appear/disappear, what content changes}

## Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| {paramName} | {type} | {yes/no} | {what it controls} |

## Callbacks

| Name | Signature | Description |
|------|-----------|-------------|
| {callbackName} | `() -> Unit` | {when it fires and what it means} |

## Design Tokens Used

Only tokens for the screen's own layout — not child component internals.

```json
{
  "tokenName": "#hexValue"
}
```

## Implementation Notes

- **Composition:** {how this screen composes its child composables}
- **Data fetching:** {ViewModel StateFlow observations or `LaunchedEffect` API calls}
- **Validation:** {form validation approach if applicable}
- **Accessibility:** {screen-level a11y — `Modifier.semantics`, focus management, `contentDescription`}
- **Route:** {Navigation Compose route, e.g. `Screen.Settings.Profile`}
```

### Step 9: Review and Save

Present the generated screen spec to the user for review:

```
## Generated Screen Spec — Review

I've generated the following:

- `design-specs/screens/{screen-name}.md`

I'll show you the full content now. Let me know if anything needs correcting before I save.
```

Show the full spec content and wait for the user to approve or request changes. Make corrections as needed. Then save the file.

### Step 10: Summarise

After saving, present a summary:

```
## Screen Spec Curation Complete

**Created:**
- `design-specs/screens/{screen-name}.md`
{if inline component specs were created:}
- `design-specs/components/{component-1}.md`
- `design-specs/components/{component-2}.md`

**Updated:**
- `design-specs/tokens.json` ({n} new tokens added)

**Screen Stats:**
- {n} sections mapped to components
- {n} screen states documented
- {n} parameters, {n} callbacks defined

**Component Gaps:**
- {list of any remaining gaps, or "None — all components have specs"}

**Next Steps:**
1. Review the generated spec for accuracy against Figma designs
2. Implement with reference to the spec
3. Commit to repository
```

---

## Quality Checklist

Before finalising, verify:

- [ ] All screen sections reference a component spec (or were specced inline)
- [ ] Screen states are fully documented (which sections appear/disappear)
- [ ] Parameters and callbacks cover all data flowing in and out
- [ ] Layout uses dp values and Compose modifier descriptions (not absolute coordinates)
- [ ] Design tokens are screen-level only (not duplicating child component tokens)
- [ ] Implementation notes include route, data fetching, and accessibility
- [ ] File is saved in `design-specs/screens/`
- [ ] Any inline component specs are saved in `design-specs/components/`
- [ ] `design-specs/tokens.json` is updated with any new values
- [ ] Summary clearly states all created/updated files

## Handling Edge Cases

### No Figma JSON available

Work entirely from conversation. Ask more detailed questions about layout, spacing, and visual appearance. Use placeholder values with `[TODO: Confirm from Figma]` where precise measurements are needed.

### Component spec doesn't exist and user doesn't want to spec it inline

Flag it as `[TODO: Spec needed]` in the Section Layout table. List in the summary so the user knows what to curate next with `/curate-component-spec`.

### Screen appears in a flow

If the user mentions the screen is part of a flow, suggest using `/curate-flow-spec` instead. If they want to spec the screen standalone first, proceed but note in the "Used In Flows" section: `[TODO: Link to flow spec when created]`.

### Screen is very simple (1–2 sections)

Still generate the full spec format for consistency, but the sections will be brief. Note in Implementation Notes if the screen is simple enough to be a single composable rather than a composed screen.

## Example Invocations

**User says:**

> `/curate-screen-spec settings profile page`

**Claude Code does:**

1. Inventory existing component specs
2. Ask about the screen's purpose, actions, navigation route, and if JSON is available
3. If JSON provided, analyze and map sections to components
4. Spec any new components inline
5. Document screen states, parameters, callbacks
6. Generate and save screen spec
7. Summarise

---

**User says:**

> `/curate-screen-spec design-specs/raw/food-log-screen.json`

**Claude Code does:**

1. Inventory specs
2. Read and analyze the JSON — identify sections, match to components
3. Ask about interactions and states
4. Spec unmatched components inline
5. Generate screen spec with JSON-derived layout details
6. Save and summarise
