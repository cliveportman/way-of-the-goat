# Commit Changes

Stage and commit all changes with a semantic prefix and contributor emoji.

**Add** all modified and new files to git. If you think there are files that should not be in version control, ask the user. If you see changes that should be bundled into separate commits (e.g. a design spec change and a Kotlin implementation change), ask the user.

THEN commit with a clear and concise one-line commit message using semantic commit notation:
- `feat:` — new feature or screen
- `fix:` — bug fix
- `spec:` — design spec created or updated (in design-specs/)
- `refactor:` — code restructure with no behaviour change
- `style:` — formatting, naming, no logic change
- `docs:` — documentation only
- `test:` — adding or updating tests
- `chore:` — build config, dependencies, tooling

**Identify contributor and their preferred emoji**:
- Run `git config user.name` to get the current git username
- Look up their preferred emoji from this mapping:
  | Git Username | Preferred Emoji |
  |---|---|
  | Clive Portman | 🐏 |
- The message should begin with the emoji.

The user is explicitly giving you permission to:
- use `git add` commands to stage files
- use `git commit` to create commits
