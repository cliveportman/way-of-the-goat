Curate a Mermaid flow diagram for: $ARGUMENTS

# Task: Curate Flow Diagram

## Purpose

Generate a Mermaid flow diagram from screenshots, conversation, or an existing diagram (ASCII, whiteboard photo, Miro export, etc.). The output is a single `.md` file with a Mermaid flowchart that follows team conventions — stored in `design-specs/flows/` with a `-mermaid` suffix.

This command supports three input modes:
1. **Screenshots/images** — user provides annotated screenshots, wireframes, or whiteboard photos
2. **Existing diagram** — user provides an ASCII diagram, Miro export, or other visual to convert
3. **Conversation only** — no visual input; the flow is built through Q&A

All modes converge on the same iterative refinement loop: generate draft, preview, gather feedback, revise.

## Prerequisites

- Read the mermaid skill files before generating any diagram:
  - `.claude/skills/mermaid/SKILL.md` — conventions
  - `.claude/skills/mermaid/syntax-cheatsheet.md` — node shapes, edge syntax
  - `.claude/skills/mermaid/examples.md` — team-style reference diagrams

---

## Workflow Steps

### Step 0: Read Mermaid Skill Files

**Silently** read the three reference files listed in Prerequisites. These inform all diagram generation in subsequent steps.

### Step 1: Determine Input Mode

Ask the user how they'd like to provide the flow:

> How would you like to build this diagram?
>
> 1. **Screenshots** — I have screenshots, wireframes, or photos of the flow
> 2. **Existing diagram** — I have an ASCII diagram, Miro board, or other visual to convert to Mermaid
> 3. **Conversation** — Let's build it from scratch through discussion
>
> If you have screenshots or an existing diagram, drop them now. Otherwise we'll start talking through the flow.

Proceed to the matching path below based on the user's choice.

---

### Path A: Screenshots / Images

#### A1: Analyse Screenshots

When the user provides images:

1. Study each image in order — identify screens, actions, decisions, branches, error states, and flow direction
2. Match UI elements against the mermaid skill node shapes:
   - Entry/exit points → stadium `([text])`
   - User actions / process steps → rectangle `[text]`
   - Decision points → diamond `{text}`
   - UI outputs (dialogs, toasts, errors) → parallelogram `[/text/]`
   - API calls / system operations → hexagon `{{text}}`
3. Present the extracted flow as a numbered summary:

> ## Screenshot Analysis
>
> I've extracted the following flow:
>
> **Entry:** {trigger} from {origin}
>
> 1. {Step description} — {what user sees/does}
> 2. {Step description} — {decision point: Yes → ..., No → ...}
> ...
> n. {Final step} → {exit destination}
>
> **Branches detected:** {list}
> **Validation / error states detected:** {list}
>
> Does this capture the flow correctly? Anything I've missed or misread?

4. Wait for confirmation. Incorporate corrections.

#### A2: Ask for Additional Screenshots

After the initial analysis, check for gaps:

> Are there any additional screenshots showing:
> - Error states or validation messages?
> - Alternative paths or edge cases?
> - Screens I haven't seen yet?
>
> Drop them now, or say "that's everything" and I'll generate the diagram.

If the user provides more images, analyse them and merge into the flow. Repeat until complete.

Proceed to **Step 2: Generate Diagram**.

---

### Path B: Existing Diagram Conversion

#### B1: Receive and Analyse the Source Diagram

The user provides an existing diagram (ASCII art, pasted text, Miro export, whiteboard photo, etc.).

1. Study the source diagram and extract:
   - All nodes and their roles (start, action, decision, output, end)
   - All connections and their labels
   - Subgroups or phases
   - Annotations or notes
2. Present the extracted structure:

> ## Source Diagram Analysis
>
> I've identified the following structure:
>
> **Nodes ({count}):**
> - {node} — {role: start / action / decision / output / end}
> ...
>
> **Connections ({count}):**
> - {from} → {to} ({label if any})
> ...
>
> **Notes/annotations:**
> - {list}
>
> I'll map these to the team's Mermaid conventions (node shapes, styling, camelCase IDs).
> Anything I've misread from the source?

3. Wait for confirmation.

#### B2: Clarify Ambiguities

If the source diagram is unclear on any point (e.g., unlabelled decision branches, ambiguous flow direction, missing error paths):

> A few things aren't clear from the source:
> - {question about ambiguity}
> - {question about ambiguity}
>
> Can you clarify these before I generate the Mermaid version?

Proceed to **Step 2: Generate Diagram**.

---

### Path C: Conversation Only

#### C1: Gather the Happy Path

> Let's map the happy path first — the ideal journey from start to finish.
>
> 1. **Where does the flow start?** (e.g. user taps a button, lands on a screen, system event triggers)
> 2. **Walk me through each step in order:**
>    - What happens?
>    - What does the user see or do?
>    - Where do they go next?
> 3. **Where does the flow end?** (e.g. success screen, navigate back, data saved)
>
> Don't worry about error paths or branches yet — we'll add those next.

After the user responds, summarise the happy path and confirm:

> ## Happy Path
>
> **Entry:** {trigger}
>
> 1. {Step} — {description}
> 2. {Step} — {description}
> ...
> n. {Exit} — {description}
>
> Does this look right?

#### C2: Add Branches and Error Paths

> Now let's layer in the branches:
>
> - **Decision points:** Where can the flow go different ways? (Yes/No, user choices, system conditions)
> - **Error paths:** What happens when things fail? (validation, API errors, timeouts)
> - **Escape routes:** Can the user cancel, go back, or skip at any step?
>
> Walk me through any alternate paths.

After the user responds, present the full flow (happy path + branches) and confirm.

#### C3: Ask for Screenshots

Before generating, check if the user has any visual reference:

> Before I generate the diagram — do you have any screenshots, wireframes, or sketches of this flow? They can help me get the details right.
>
> If not, no worries — I'll generate from our conversation.

If screenshots are provided, analyse them (follow Path A steps) and merge with the conversational flow.

Proceed to **Step 2: Generate Diagram**.

---

### Step 2: Generate Diagram

Using the confirmed flow structure from whichever path was taken:

1. **Build the Mermaid flowchart** following all conventions from the skill files:
   - Use `flowchart LR` for user flows (default), `flowchart TD` for hierarchies
   - Descriptive camelCase node IDs (never single letters)
   - Correct node shapes per role (stadium for start/end, diamond for decisions, etc.)
   - Labelled decision edges (`|Yes|`, `|No|`)
   - Annotation notes using styled parallelogram nodes with dashed borders, connected via `~~~`
   - `classDef` colour scheme at the end, with `class` assignments
   - No step numbers in node labels — describe what the node does

2. **Write the file** at `design-specs/flows/<flow-name>-mermaid.md` with this structure:

```markdown
# {Flow Title}

{2-3 sentence description of what the flow covers.}

```mermaid
flowchart LR
    ...
```

## Notes

- {Contextual notes that don't fit in the diagram}
```

3. **Preview with MCP tool** — if `mermaid_preview` is available, render the diagram for live feedback:

> Here's the initial diagram. I've opened a live preview so you can see the layout.
>
> Take a look and let me know what needs adjusting — layout, missing paths, wording, or anything else.

If `mermaid_preview` is not available, present the markdown content and suggest previewing in VS Code or mermaid.live.

### Step 3: Iterate

Enter a feedback loop:

> What would you like to change? Common adjustments:
> - Add missing paths or error states
> - Reword node labels
> - Restructure the layout (grouping, flow direction)
> - Add or revise annotations
> - Break a complex section into a subgraph
>
> If you have additional screenshots showing states I haven't captured, drop them and I'll incorporate them.
>
> Say **"looks good"** when you're happy with it.

For each round of feedback:
1. Apply the requested changes
2. Update the file
3. Re-render with `mermaid_preview` if available
4. Present a brief summary of what changed

Repeat until the user confirms the diagram is complete.

### Step 4: Finalise

Once the user approves:

1. **Save the final file** (should already be saved from Step 2/3 iterations)
2. **Optionally save a rendered image** — if the user wants a PNG/SVG, use `mermaid_save` to export. Note: rendered images should not be committed to git (GitHub renders mermaid natively)
3. Present a summary:

> ## Flow Diagram Complete
>
> **File:** `design-specs/flows/{flow-name}-mermaid.md`
>
> **Diagram stats:**
> - {n} nodes ({breakdown by type: actions, decisions, outputs})
> - {n} connections
> - {n} annotations
>
> **Next steps:**
> - The diagram is ready to reference from flow specs and PRs
> - GitHub will render the Mermaid code block natively
> - To create an implementation-ready flow spec, use `/curate-flow-spec`

---

## Quality Checklist

Before presenting a diagram as complete, verify against the mermaid skill checklist:

- [ ] Every decision node has labelled edges for each outcome
- [ ] Node IDs are descriptive camelCase, not single letters
- [ ] Start/end points use stadium shapes `([text])`
- [ ] UI outputs (dialogs, errors, toasts) use parallelogram shapes `[/text/]`
- [ ] No step numbers in node labels — labels describe what happens
- [ ] No emojis or icons in annotation notes
- [ ] Annotations are visually distinct (dashed border, muted colour)
- [ ] The diagram renders without syntax errors
- [ ] `classDef` styles are defined at the end and applied consistently
- [ ] File is saved with `-mermaid` suffix in `design-specs/flows/`

## Edge Cases

### Source diagram uses a different notation (e.g. BPMN, UML activity)

Map the notation's concepts to Mermaid flowchart equivalents. Note any concepts that don't translate cleanly (e.g. swimlanes, parallel gateways) and ask the user how to handle them.

### Flow is too large for a single diagram

If the diagram exceeds ~30 nodes and becomes hard to read, suggest splitting into sub-diagrams linked by subroutine nodes `[[sub-flow name]]`. Each sub-diagram gets its own `-mermaid.md` file.

### User wants a different diagram type (sequence, state, ER)

This command defaults to flowcharts. If the user's flow would be better served by a sequence diagram or state diagram, suggest the alternative and generate accordingly using the mermaid skill conventions.

### User iterates beyond the initial flow

If the user wants to add entirely new branches or phases during iteration, treat it as new input — analyse, confirm, then regenerate. Don't silently add large changes.

## Example Invocations

**From screenshots:**
> `/curate-flow-diagram context switch flow`
> *(user drops annotated Figma screenshots showing the flow)*

**Converting an existing ASCII diagram:**
> `/curate-flow-diagram convert the onboarding flow diagram`
> *(user pastes an ASCII flow chart from an existing spec)*

**Conversation only:**
> `/curate-flow-diagram food logging journey`
> *(no images — Claude walks through the flow via Q&A)*
