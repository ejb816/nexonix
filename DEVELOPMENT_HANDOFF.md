# Development Handoff

This is the shared resumption checkpoint for Claude Code and Codex, not an additional
architecture specification or a transcript. Follow DRACO.md section 7 and verify this
snapshot against the current tree before acting.

## Checkpoint

- Updated: 2026-09-27, by Claude Code, after Dev committed and pushed the shared-skill increment.
- Destination: either Claude Code or Codex when Dev chooses to continue; no session dispatched.
- Observed branch/HEAD: `main`, `79b363f` (shared documentation skill across agents),
  over `aa7ece9` (journal and shared handoff through chapter 83).
- Dev committed and pushed `79b363f` with Codex's commands; local `main` and `origin/main`
  agree at `79b363f`. No fresh remote query performed. The runtime checkpoint remains `7de07ba`.
- Tree after `79b363f`: clean. The only uncommitted work is this checkpoint refresh,
  its CHANGELOG line and git record 2026-09-27-1215. No runtime source changes.
- Permission: Dev explicitly authorized both agents to maintain the journal and shared
  documentation on September 23. The former Cowork-only restriction is superseded.
  Existing sbt/commit/push and new-issue restrictions remain in force.

## Completed Development

- Draco-owned Text is realized as native Scala String. Nominal, Surface, DomainLine,
  DomainLineOf and Emission name Text in their definitions without changing runtime representation.
- DomainAspectText and DomainAspectOf generate the complete domain section used by Drake.emit
  and thus the existing generator rule flow. Parameterized references and member order survive;
  superDomain remains in DracoAspect. Type-parameter spelling is still a callback to the
  handwritten Drake renderer. Other aspect emission remains handwritten.
- DomainLine and DomainLineOf remain compatibility helpers. No follow-on implementation
  is authorized merely by this handoff.
- Supporting records: `draco-git-record/git-record-2026-09-22-1231`, `-1331`, `-1339`,
  and `-1453` (the latter contains the combined commit message).

## Verification

Dev ran the full suite on September 22 at 14:57, before committing the implementation:
**652 tests / 46 suites passed**, zero failures or aborted suites. Codex inspected
the saved output and the file-only DrakeGenTest baseline in this conversation.

- Complete domain sections: 101 reproduced, 1 parameterized included, 0 skipped.
- GenDrake: 99/99 emitted, 0 missing or unexpected.
- Parse scope: 101 draco + 10 mods; surface losses: 1 across 111 types (known ActorAspect loss).
- Example generation: 28 match / 20 differ / 0 error / 0 missing of 48.
- PON: 80 numbers / 550 expressions / 42 discrepancies; canonical differences: 7.
- Scenario: 23 clean; forest scenario passed; mods actors pending Drake authoring: 0.
- Known Evrete subtype co-declaration limitation unchanged; it is not a newly fixed behavior.

These results apply to the implementation checkpoint, not to future edits. The current
handoff increment is documentation-only; it does not claim a new test run. Temporary test
logs may be overwritten; the committed records and DRACO.md preserve the verified result.

Documentation checks on September 23 passed: staged/unstaged whitespace, chapter links,
fence balance and required sections, chapter-83 markers, and preservation of all 33
non-responsive source turns in chapter 81. The local helper passed Bash syntax and
isolated checks for non-mutating reporting, creation of missing links, and refusal to
replace regular files or wrong-target links. Agent links remain tracked symlinks; no
runtime file or .gitignore changes were found. These checks are not a new sbt result.

## Documentation Coverage

- Journal: chapters 81-83 cover the range after chapter 80's `5fd801d` through this
  documentation handoff. DRACO.md and README.md were reconciled through chapter 83.
- Chapter 81 recovers parser/operators `665926e`, parentheses `309ea4d`, conditional
  cleanup `0dc4900` and bracket `8e59cbe` from Claude Code session
  `3df62d0b-dcac-4979-9085-52eac92d20dd`. Its final bracket full-run reply is absent;
  scoped probes and expected counts remain distinct from witnessed results.
- Chapter 82 uses selected Codex exchanges and Dev's run/push output. The initial Text
  task is a record-backed summary, not a recovered full transcript. Chapter 83 records
  explicit permission and the documentation work. These source limits are disclosed;
  coverage does not mean every dialogue has been preserved.
- Earlier moved sessions are already covered by the journal as Dev described; do not
  re-import them wholesale. Retrieve only a specific missing source if needed and available.
- No private Claude Code or Codex auto-memory has been synchronized or overwritten.
  Shared continuity now lives in tracked project files; tool-local memory may point here.
- On September 27 Dev authorized sharing the formerly local documentation skill.
  `.gitignore` now allowlists only `.draco/skills/update-draco-docs/SKILL.md` and
  `scripts/check_links.sh`; other .draco data remains ignored. DRACO.md directs both
  agents to read the same file, independent of automatic discovery or .claude setup.
  The skill uses the checkout root rather than a machine-specific path. Report mode
  remains non-mutating; authorized repairs only create missing links.
- Chapter 83 and its sync markers remain the journal endpoint. This September 27
  sharing decision is recorded in git record 2026-09-27-1050 (commit `79b363f`), but has
  not yet been added as a journal chapter. Historical local-only statements describe
  the September 23 state and are not rewritten retrospectively.

## Resumption

The chapter-83 reconciliation was committed and pushed as aa7ece9, and the shared-skill
increment as 79b363f. Only this checkpoint refresh awaits Dev's commit/push. Verify the
actual diff rather than assuming every staged file belongs to this work. No further Draco feature has been selected. Dev still runs any
required sbt, commit and push commands; no new GitHub issue is authorized. Missing
historical dialogue is not permission to invent it or restart completed implementation.
