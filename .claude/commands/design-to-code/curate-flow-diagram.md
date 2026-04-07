Curate a Mermaid flow diagram for: $ARGUMENTS

# Task: Curate Flow Diagram

## Purpose

Generate a Mermaid flow diagram from screenshots, an existing ASCII diagram, or conversation. The output is a single `.md` file stored in `design-specs/flows/` with a `-mermaid` suffix.

Supports three input modes:
1. **Screenshots/images** — annotated Figma screenshots or wireframes
2. **Existing diagram** — ASCII diagram, Miro export, or other visual to convert
3. **Conversation only** — built through Q&A

All modes converge on the same iterative refinement loop: generate draft → preview → gather feedback → revise.

## Prerequisites

Read before generating any diagram:
- `.claude/skills/mermaid/SKILL.md` — conventions, node shapes, colour scheme
- `design-specs/flows/` — save output here

---

## Workflow Steps

### Step 0: Read Mermaid Skill

**Silently** read `.claude/skills/mermaid/SKILL.md` before generating any diagram. It defines node shapes, classDef colours, and quality rules.

---

### Step 1: Determine Input Mode

Ask the user:

```
How would you like to build this diagram?

1. **Screenshots** — I have Figma screenshots, wireframes, or photos
2. **Existing diagram** — I have an ASCII diagram or other visual to convert to Mermaid
3. **Conversation** — let's build it from scratch through discussion

Drop screenshots or the diagram now, or say "conversation" and we'll start talking.
```

---

### Path A: Screenshots / Images

#### A1: Analyse Screenshots

1. Study each image in order — identify screens, actions, decisions, branches, error states
2. Map UI elements to Mermaid node shapes per the skill file
3. Present extracted flow as a numbered summary:

```
## Screenshot Analysis

**Entry:** {trigger}

1. {Step} — {description}
2. {Step} — {decision: Yes → ..., No → ...}
...

**Branches detected:** {list}
**Error/validation states:** {list}

Does this capture the flow? Anything missed?
```

4. After confirmation, proceed to Step 3.

#### A2: Check for Additional Screenshots

```
Are there screenshots showing error states, alternative paths, or screens I haven't seen?
Drop them now, or say "that's everything".
```

---

### Path B: Existing Diagram Conversion

#### B1: Analyse the Source Diagram

1. Study the source (ASCII art, pasted text, photo)
2. Extract all nodes and their roles, all connections and labels, any annotations
3. Present the analysis:

```
## Source Diagram Analysis

**Nodes ({n}):**
- {node} — {role: start / action / decision / output / end}

**Connections ({n}):**
- {from} → {to} ({label})

I'll map these to Mermaid conventions (correct node shapes, camelCase IDs, classDef styling).
Anything I've misread?
```

4. Clarify any ambiguities (unlabelled decision branches, ambiguous direction) before generating.

---

### Path C: Conversation Only

#### C1: Gather Happy Path

```
1. **Where does the flow start?**
2. **Walk me through each step:** what happens, what does the user see/do, where next?
3. **Where does the flow end?**

Don't worry about error paths yet — we'll add those next.
```

Summarise and confirm.

#### C2: Add Branches and Error Paths

```
Now the branches:
- **Decision points:** Where can the flow go different ways?
- **Error paths:** What happens when things fail?
- **Escape routes:** Can the user cancel or go back?
```

---

### Step 3: Generate Diagram

Using the confirmed flow:

1. Build the Mermaid flowchart following all conventions from `.claude/skills/mermaid/SKILL.md`:
   - `flowchart LR` for user flows (default)
   - Descriptive camelCase node IDs
   - Correct shapes per role
   - Labelled decision edges
   - Annotation notes with dashed borders connected via `~~~`
   - `classDef` colour scheme at the end with `class` assignments

2. Write the file at `design-specs/flows/{flow-name}-mermaid.md`:

````markdown
# {Flow Title}

{2–3 sentence description of what this flow covers.}

```mermaid
flowchart LR
    ...
```

## Notes

- {Contextual notes that don't fit in the diagram}
````

3. Present the markdown and invite feedback:

```
Here's the diagram. Take a look and let me know what needs adjusting —
layout, missing paths, wording, or anything else.
```

---

### Step 4: Iterate

```
What would you like to change?
- Missing paths or error states
- Rewording node labels
- Layout restructure (grouping, flow direction)
- Add or revise annotations

Say **"looks good"** when you're happy with it.
```

For each round: apply changes, update the file, present what changed. Repeat until confirmed.

---

### Step 5: Finalise

Once approved:

```
## Flow Diagram Complete

**File:** `design-specs/flows/{flow-name}-mermaid.md`

**Stats:**
- {n} nodes ({n} actions, {n} decisions, {n} outputs)
- {n} connections
- {n} annotations

The diagram is ready to reference from flow specs.
GitHub renders Mermaid code blocks natively.
```

---

## Quality Checklist

Per `.claude/skills/mermaid/SKILL.md`:

- [ ] Every decision node has labelled edges for all outcomes
- [ ] Node IDs are descriptive camelCase — not single letters
- [ ] Entry/exit points use stadium shapes `([text])`
- [ ] UI outputs (modals, errors, toasts) use parallelogram shapes `[/text/]`
- [ ] No step numbers in node labels
- [ ] No emojis in annotation notes
- [ ] Annotations use dashed border and muted colour
- [ ] `classDef` styles defined and applied consistently
- [ ] Diagram renders without syntax errors
- [ ] File saved with `-mermaid` suffix in `design-specs/flows/`

---

## Edge Cases

### Source diagram uses different notation (BPMN, UML activity)
Map concepts to Mermaid flowchart equivalents. Note any that don't translate cleanly (swimlanes, parallel gateways) and ask how to handle them.

### Diagram exceeds ~30 nodes
Suggest splitting into sub-diagrams linked by subroutine nodes `[[sub-flow name]]`. Each sub-diagram gets its own `-mermaid.md` file.

### User wants a sequence or state diagram
Note this command defaults to flowcharts. Offer to generate a sequence or state diagram using Mermaid's relevant diagram type if it's a better fit.
