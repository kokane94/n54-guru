# AGENTS.md — N54 Guru

> Guidelines for AI coding agents working on this project.
> Owner: Kane (github.com/kokane94). AI partner: Hermes by Nous Research.

---

## RULE 0 — OWNER OVERRIDE

If Kane tells you to do something, do it. He is in charge.

---

## RULE 1 — NEVER DELETE FILES WITHOUT PERMISSION

You are not allowed to delete any file, folder, or commit history in this repo
without Kane's explicit written permission. This includes:

- `rm -rf` anywhere in the project
- `git reset --hard`
- `git clean -fd`
- `DROP TABLE`, `TRUNCATE`, or destructive DB operations
- overwriting existing files without approval
- deleting "temporary" or "test" files unless Kane explicitly says so

If something looks unused, ask before deleting.

---

## RULE 2 — NO DESTRUCTIVE COMMANDS

Never run these without explicit permission:

- `git reset --hard`
- `git clean -fd`
- `rm -rf ./src ./app ./build` or similar
- `gradle clean` (ok only if specifically requested)
- any command that rewrites git history (`rebase -i`, `filter-branch`, etc.)

If cleanup is needed, prefer:

- `git status`
- `git diff`
- `git stash`
- move/rename instead of delete

---

## RULE 3 — KEEP IT KISS / DRY

- Reuse existing code before adding new files
- New files only for genuinely new functionality
- Do not create `mainV2.kt`, `screen_improved.kt`, etc.
- Match existing code style

---

## RULE 4 — VERIFY BEFORE CLAIMING DONE

- Run the actual build command or CI before saying something works
- If the local environment can't build (e.g. no JDK), say that explicitly
- Do not fabricate passing results

---

## RULE 5 — PERSIST CONTEXT IN REPO FILES

This project survives across Hermes session restarts. Important decisions,
roadmaps, and feature specs must live in repo files, not just chat memory.

Use:

- `FEATURES.md` for planned/implemented features
- `DECISIONS.md` for architectural decisions
- `STATUS.md` for current state and blockers

---

## RULE 6 — ANDROID APP CONVENTIONS

- Kotlin source lives under `app/src/main/java/com/example/n54guru/`
- Compose screens go in `knowledge/` or `ui/`
- Shared UI components go in `ui/theme/`
- Colors and themes go in `app/src/main/res/values/`
- Do not bump Compose / AGP / Gradle versions unless asked
- The project targets Material3 `1.1.2`

---

## RULE 7 — COMMIT AND PUSH

When Kane says "commit", create a clear commit message and push to the
appropriate branch. When in doubt, push to `fix/build-deps` and open a PR,
or ask Kane if he wants direct merge to `main`.

---

## RULE 8 — TREAT KANE'S WORK AS VALUABLE

Kane spent months getting this build green. Do not throw away working code.
Refactor in place. Preserve features. If a change breaks something, fix it
before moving on.

---

## Contact

- Repo: https://github.com/kokane94/n54-guru
- Hermes Agent by Nous Research
