---
name: claude-review-criteria
description: Review criteria and knowledge base for .claude/ directory changes — agents, skills, commands, hooks, and settings. Covers Anthropic best practices, optimisation patterns, and cross-tool awareness from Codex, Opencode, Cursor, Gemini CLI, and others. Use when reviewing .claude/ additions or edits.
---

# Claude Directory Review Criteria

This skill guides thorough, opinionated reviews of changes to the `.claude/` directory. Apply it when reviewing new or updated agents, skills, commands, hooks, or settings.

## Review Philosophy

A good `.claude/` review catches three things in order of priority:

1. **Correctness** — Does the frontmatter parse correctly? Will it route as intended?
2. **Efficiency** — Does it minimise context cost without sacrificing quality?
3. **Clarity** — Will future contributors (or a fresh Claude session) understand what it does?

---

## 1. Agent Review Checklist

### Frontmatter Correctness
- [ ] `name` matches the filename stem exactly (kebab-case, no spaces)
- [ ] `description` is 1–2 sentences; no XML tags; includes trigger conditions
- [ ] `model` is appropriate for the task complexity (see guide below)
- [ ] `tools` follows least-privilege (reviewers use `Read, Glob, Grep` only)
- [ ] `skills` lists only skills needed on >80% of invocations — eager loading injects the full SKILL.md into every spawn
- [ ] No `color:` field (silently ignored but adds noise)
- [ ] No `Agent` tool listed — subagents cannot spawn subagents

### Model Selection Guide

| Agent type | Recommended model | Rationale |
|---|---|---|
| Fast search, file exploration | `haiku` | Speed matters; reasoning depth doesn't |
| Implementation, review, most tasks | `sonnet` | Best cost/capability balance |
| Complex architecture, multi-step reasoning | `opus` | Justified for orchestrators and deep analysis |
| Reviewer inheriting parent context | `inherits` | Avoids unnecessary override |

Rule of thumb: use the cheapest model that reliably produces correct output. Upgrade only on observed quality failures.

### Body Content
- [ ] Clear role statement within the first 2 lines
- [ ] Explicit scope AND out-of-scope (prevents mis-routing and scope creep)
- [ ] Lazy skill references (`Read .claude/skills/foo/SKILL.md`) for occasionally needed skills
- [ ] No verbatim duplication of content in CLAUDE.md or sub-project CLAUDE.md files
- [ ] Read-only agents (reviewers): confirm no `Write`, `Edit` in tools list

### Anti-patterns to Flag
- **Over-eager skills**: More than 2 skills in frontmatter `skills:` without strong justification. Each one adds full SKILL.md tokens to every spawn.
- **Under-scoped description**: "Helps with code" or "Backend expert" — too vague for auto-routing. Include domain, action verbs, and trigger conditions.
- **Context leakage**: Instructions that repeat verbatim from CLAUDE.md. Agents inherit CLAUDE.md; don't duplicate it.
- **Wrong model provisioning**: Opus on a search agent (wasteful); Haiku on a multi-file implementation (quality risk).
- **Nesting attempt**: Agent body instructs Claude to "delegate to @other-agent" — only the main session can spawn agents.

---

## 2. Skill Review Checklist

### Frontmatter
- [ ] `name` is descriptive and domain-specific (not generic like "patterns")
- [ ] `description` is specific enough for auto-routing — include domain, trigger verbs, and keywords
- [ ] `disable-model-invocation: true` only if the skill must never auto-trigger
- [ ] `user-invocable: false` only if the skill is exclusively for programmatic/agent use

### Content Quality
- [ ] Under 500 lines (longer skills should split detail into `references/` subdocs)
- [ ] Concrete code examples present — not just prose principles
- [ ] Clear headings for scanability within a large context window
- [ ] No duplication of knowledge in other skills in this project

### Loading Strategy Flags
Flag as a suggestion if:
- Skill is large (>300 lines) AND listed as eager (`skills:` frontmatter) in any agent
- Skill is generic enough to be useful across many agents but is only referenced by one
- Two skills cover overlapping ground — consolidation opportunity

---

## 3. Command Review Checklist

- [ ] First 2 lines clearly state the command's purpose
- [ ] `$ARGUMENTS` usage documented if the command accepts arguments
- [ ] Named agent used for execution rather than inline instructions where appropriate
- [ ] Worktree isolation (`EnterWorktree`/`ExitWorktree`) used for reviews needing clean state
- [ ] Output format specified (structured section headers, tables, rating scale, etc.)
- [ ] Interactive commands explicitly say "stop and wait for user input" after presenting findings
- [ ] `--recheck` or similar delta modes documented if the command may be run repeatedly on the same target

---

## 4. Hook Review Checklist

### Script Quality
- [ ] `#!/bin/bash` shebang present
- [ ] `input=$(cat)` reads stdin (hooks receive JSON event data via stdin)
- [ ] `jq` used for JSON parsing with sensible fallbacks (`// "unknown"`, `// empty`)
- [ ] Carriage return stripping: `tr -d '\r'` on extracted values, `gsub("\r"; "")` in jq
- [ ] Absolute path used for log file (relative paths break when cwd changes)
- [ ] `cwd` extracted with fallback to repo root
- [ ] `branch` extracted with `git -C "$cwd" branch --show-current` with fallback to `?`
- [ ] Script is executable (`chmod +x`)

### Registration in settings.local.json
- [ ] Hook registered under the correct `PostToolUse` matcher
- [ ] Absolute path used in `"command"` field
- [ ] New hooks added alongside existing ones (not replacing them)

### Available Hook Matchers

| Matcher | Fires when |
|---|---|
| `Agent` | A subagent is spawned via the Agent tool |
| `Read` | A file is read with the Read tool |
| `Skill` | A skill is invoked |
| `Bash` | A bash command runs |
| `Write` | A file is written |
| `Edit` | A file is edited |

---

## 5. Settings Review Checklist (`settings.local.json`)

- [ ] No wildcard write permissions (e.g. `Write(**)` or `Edit(**)` — overly broad)
- [ ] Destructive git operations (`git reset --hard`, `git clean -f`) listed only if genuinely needed
- [ ] Bash allowlist entries are as specific as possible — prefer `Bash(git log:*)` over `Bash(*)`
- [ ] MCP permissions use the minimum set required for the workflow
- [ ] New hooks added to `PostToolUse` without removing existing hooks

---

## 6. Cross-cutting Consistency

### CLAUDE.md Sync
After any agent, command, or skill addition:
- [ ] Agent routing table in root CLAUDE.md updated with name, use-case, and model
- [ ] Commands table in root CLAUDE.md updated
- [ ] New skills referenced in the skills section of root CLAUDE.md

### Naming Conventions

| Artefact | Convention | Example |
|---|---|---|
| Agent filename | kebab-case | `backend-coder.md` |
| Skill directory | kebab-case | `api-handler-pattern/` |
| Command filename | kebab-case | `review.md` |
| Hook script | kebab-case | `skill-hook.sh` |

### Scope Overlap Check
When adding a new agent: confirm no existing agent already covers the same domain. Overlap leads to mis-routing and inconsistent behaviour.

---

## 7. Anthropic Documentation Alignment

Key principles from the current Claude Code documentation and observed platform behaviour:

### Context Window and Loading
- **Skill loading is lazy by default in the main session** — only skill descriptions (~2% of content) are loaded. The full SKILL.md is injected only when the skill is invoked.
- **Eager skills in agent frontmatter** (`skills:`) inject the full SKILL.md into every agent spawn, regardless of whether the skill is needed. Budget them carefully.
- **Agent descriptions drive routing** — the `description` field is the primary signal Claude uses to decide when to invoke an agent. Vague descriptions cause missed or incorrect invocations.

### Subagent Architecture
- **No nesting**: Spawned subagents cannot themselves spawn subagents. Only the main session can use the Agent tool. Design orchestrators accordingly.
- **Parallel dispatch**: The main session can spawn multiple agents simultaneously. Identify independent tasks and run them in parallel rather than sequentially.
- **Worktree isolation**: `isolation: worktree` creates a clean git worktree per agent run. Useful for review agents and any work that must not affect the current working state.

### Hooks System
- Hooks fire on `PostToolUse` (after) and `PreToolUse` (before) events
- Hooks receive the full tool call JSON via stdin
- Hook failures are logged but do not block the tool call — hooks are for observability, not control flow
- `bypassPermissions` permission mode should only be used for trusted, non-interactive CI-style flows

### Memory and Persistence
- `.auto-memory/` persists user/feedback/project/reference knowledge across conversations
- `plans/` directory pattern provides compaction-resilient multi-session coordination (agents explicitly re-read plan files after compaction)
- Agent session logs (`plans/rubber-duck/session-YYYYMMDD-HHMM.md`) provide audit trail

---

## 8. Competitor Tool Awareness

Understanding how other AI coding tools approach the same problems helps identify optimisation opportunities and Claude Code differentiators. This section is forward-looking — use it to spot gaps and strengths.

### Codex CLI (OpenAI)
- Uses `AGENTS.md` convention at repo root (analogous to `CLAUDE.md`) for project-level instructions
- Structured `--approval-policy` flags (`suggest`, `auto-edit`, `full-auto`) map roughly to Claude Code's permission modes
- Strong emphasis on showing full diffs before application — a useful UX pattern
- **Opportunity**: More explicit approval gates in commands where irreversible changes (destructive migrations, bulk file operations) are involved. Consider adding a "preview and confirm" step in affected commands.

### Opencode
- Open-source, provider-agnostic (supports Claude, OpenAI, Gemini, local models via `.opencode/` config)
- YAML agent definitions closely mirror Claude Code's format — skills and agents are more portable
- Explicit provider-switch capability means workflows can be tested against other models
- **Opportunity**: Write skills to be model-agnostic where possible — avoid relying on Claude-specific capabilities (e.g. assuming tool use works in a specific way) so skills remain useful if the project's tool evolves.

### Cursor / Windsurf (IDE-first tools)
- Cursor uses `.cursor/rules/*.mdc` — scoped Markdown rules files, one per directory, composable
- Windsurf uses a flat `.windsurfrules` file — simpler but less composable
- Both tools benefit from IDE context (open files, cursor position, diagnostic errors) that terminal tools lack
- **Opportunity**: The sub-project CLAUDE.md pattern (already used in Mobilityways) mirrors Cursor's directory-scoped rules. Ensure every active sub-project has a maintained CLAUDE.md. Also consider using `.claude/agents/` scoped to sub-projects where conventions diverge significantly.

### Gemini CLI (Google)
- Uses `GEMINI.md` at repo root — same single-file convention as early Claude Code versions
- Strong native multi-modal reasoning (images, diagrams)
- **Opportunity**: Our design-to-code pipeline already leverages Figma screenshots. Ensure agents doing visual work have Figma MCP tools explicitly enabled, and skill content describes visual reasoning patterns.

### Amazon Q Developer
- Deep AWS/CI/CD integration with `/transform` command for large codebase migrations
- Migration pattern: automated bulk change → human review pass → targeted fixes
- **Opportunity**: For large refactors in this codebase (e.g. migrating a legacy module), adopt the batch-then-review pattern rather than file-by-file. The `rubber-duck` + `backend-architect` pipeline already supports this; make it explicit in the relevant commands.

### Aider
- Git-native: every change is a commit; clear undo path via git
- Architect + editor model separation (two-model approach: planner vs. applier)
- **Opportunity**: The architect/coder agent split already mirrors this. Ensure architect agents remain strictly read-only (no Write/Edit tools) so the separation holds.

### What Other Tools Do Well (Universal Patterns)
Things every major tool does well that we should maintain in our setup:

1. **Semantic commit conventions** — all tools can read git log for project context. Keep commit messages machine-readable.
2. **Structured output formats** — consistent response templates reduce hallucination and make downstream parsing predictable.
3. **Scoped context loading** — load only what's needed, when it's needed. Every tool converges on lazy loading.
4. **Explicit tool restrictions** — principle of least privilege is universal. Overly permissive agents are a liability across all platforms.

### Claude Code Differentiators to Maximise
Areas where Claude Code excels that are worth leaning into:

- **Main-session orchestration with parallel subagents** — the Agent tool dispatching multiple specialists simultaneously is a genuine capability differentiator. Use it for complex multi-domain tasks (e.g. database + backend + frontend changes in parallel).
- **Rich MCP ecosystem** — Figma, Notion, GitHub MCP integrations are deeper and more reliable than most competitors. Agents that use these well are hard to replicate elsewhere.
- **Granular hooks system** — `PreToolUse`, `PostToolUse`, `PostCompact` event hooks with full JSON context give fine-grained observability. Other tools have coarser event systems.
- **Named persona agents** — the `jake-wharton`, `nick-butcher` pattern (named expert agents with a distinct voice and review style) sets a tone that generic "code reviewer" agents don't. Maintain this pattern for review agents.
- **Worktree isolation** — automatic isolated git worktrees per agent run is uncommon in competing tools and significantly reduces risk in review and migration workflows.

---

## 9. Review Output Format

Structure all findings using this template:

```
## .claude/ Review

### Critical Issues
[Must fix — prevents correct operation, wastes significant context, or introduces a security/permission risk]

### Suggestions
[Worth doing — improves efficiency, clarity, or aligns with better practices. Not blocking.]

### Nit-picks
[Minor — naming, style, optional improvements. Author decides; not re-raised.]

---

**Agents:** [one-line assessment]
**Skills:** [one-line assessment]
**Commands:** [one-line assessment]
**Hooks:** [one-line assessment, or "n/a" if none changed]
**CLAUDE.md consistency:** [one-line assessment]
```

Number issues sequentially across all three tiers. Group related sub-points with decimals (e.g. 1.1, 1.2). Use plain numbers for single-point issues.
