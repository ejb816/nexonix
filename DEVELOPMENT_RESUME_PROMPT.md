Continue Draco development in /Users/edwardburke/IdeaProjects/nexonix.

This is an October 26, 2026 handoff for either Codex or Claude Code, refreshed by Claude Code
after the named-construction head repair. First read AGENTS.md/CLAUDE.md (both link to
DRACO.md), DEVELOPMENT_HANDOFF.md and the shared documentation skill at
.draco/skills/update-draco-docs/SKILL.md. Read journal chapters 105–112 and
sources/codex-2026-10-22-to-26-audit.md for the Codex session's decisions and coverage.
Inspect git status/log; preserve uncommitted documentation. No private agent memory is required.

Last Dev-verified increment: the named-construction head repair (record
git-record-2026-10-26-1449), gated by me at 936 tests / 72 suites on October 26, zero failures
or aborts, every report-only headline unchanged (example map 36 match / 12 differ, zero surface
losses across 133 types, parse scope 110 draco + 23 mods, file-only pending mods actors 0).
Check `git log -1` for its commit; the code commit before it is a8b5447 (Consumer), then the
documentation handoff 7495539. All accepted implementation work is done.

Chapter 113, covering the October 26 Claude Code session, is NOT yet written; source it from
the session's original records, not from summaries.

PRESERVED BUT NOT ACCEPTED, two observations from the repair: a dyn-body result renders its
named arguments through expression with no `_name` mapping (world/Observable authors
`_geocentric:` by hand, a Scala spelling in a drake source), and constructionInitializer keys
its factory lookup on the slot type rather than the applied head. Bring ONE as a recommendation
for my acceptance or correction before building.

Keep to one design point per reply, one recommendation with accept-or-correct, and build only
what I accept. Do not run sbt, commit or push; hand over path-scoped commands and wait for my
results. Every command block you hand me must begin with
`cd /Users/edwardburke/IdeaProjects/nexonix &&`, since I often run it from a fresh terminal.
Use grep in my shell, where rg is unavailable. New issues require my explicit approval.
Before handing over a gate, run every discovered suite on compiled classes with current main,
mods and test resources on loader roots, and inspect every report-only headline. The handoff
records a reproducible direct-run procedure; temporary files are not prerequisites. After each
verified commit, bring the next related point. Activation and .dcon migration remain deferred.

Preserve historical Status sections and do not invent dialogue. Keep the shared handoff current.
