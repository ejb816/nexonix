Continue Draco development in /Users/edwardburke/IdeaProjects/nexonix.

This is an October 26, 2026 handoff from Codex for either Codex or Claude Code. First read
AGENTS.md/CLAUDE.md (both link to DRACO.md), DEVELOPMENT_HANDOFF.md and the shared documentation
skill at .draco/skills/update-draco-docs/SKILL.md. Read journal chapters 105–112 and
sources/codex-2026-10-22-to-26-audit.md for this session’s decisions and coverage. Inspect git status/log;
preserve uncommitted documentation. No private agent memory is required.

Last code commit: a8b5447, Consumer repair, committed and pushed by me. A documentation-only
handoff commit may follow it. Latest verified baseline: 933 tests / 71 suites, my October 25
full sbt run, zero failures or aborts. Example map 36 match / 12 differ, zero surface losses
across 133 types, parse scope 110 draco + 23 mods; all other report-only headlines match the
handoff, including file-only pending mods actors 0. All accepted implementation work is done.

PRESERVED BUT NOT ACCEPTED: Codex recommends fixing named-construction qualification in
DracoGenerator.constructionInitializer. It strips the constructed type to its last name, so
qualified domains.terrestrial.Location construction can emit unresolved Location. Verify this
reading and restate this one recommendation for my acceptance or correction before building.
Proposed scope: preserve resolved qualification and named-parameter mapping, add cross-package
generated compilation/runtime coverage without masking imports, check scoped and unscoped
generation, and measure corpus impact before necessary regeneration. Keep Consumer's accepted
positional call unchanged. No general constructor redesign or other example repair is accepted.

Keep to one design point per reply, one recommendation with accept-or-correct, and build only
what I accept. Do not run sbt, commit or push; hand over path-scoped commands and wait for my
results. Use grep in my shell, where rg is unavailable. New issues require my explicit approval.
Before handing over a gate, run every discovered suite on compiled classes with current main,
mods and test resources on loader roots, and inspect every report-only headline. The handoff
records a reproducible direct-run procedure; temporary files are not prerequisites. After each
verified commit, bring the next related point. Activation and .dcon migration remain deferred.

Journal coverage and sync markers reach chapter 112. Original Codex messages were recovered
across both observed compactions, and the prior Claude closing exchange is preserved. The
frozen extract ends at my October 26 acceptance of journal recovery; that repair’s final reply
is outside its boundary. Preserve historical Status sections and do not invent dialogue.
Keep the shared handoff current.
