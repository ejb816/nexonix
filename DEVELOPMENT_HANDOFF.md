# Development Handoff

This is the shared resumption checkpoint for Claude Code and Codex, not an additional
architecture specification or a transcript. Follow DRACO.md section 7 and verify this
snapshot against the current tree before acting.

## Checkpoint

- Updated: 2026-09-29, by Codex, after reviewing Dev's full-run results and transport clarification.
- Destination: either Claude Code or Codex when Dev chooses to continue; no session dispatched.
- Observed branch/HEAD: `main`, `ad6466a` (Claude Code's checkpoint refresh), over
  `79b363f` (shared skill) and `aa7ece9` (chapter-83 reconciliation). Dev supplied
  successful push output for 79b363f; no fresh remote query performed here.
- Tree was clean before this increment. Current uncommitted work is Draco-aspect
  generation for Drake output: two definition trios, two domain membership trios,
  Drake delegation, tests/fixtures, documentation and git record 2026-09-28-1634.
- Dev authorized this implementation after explicitly clarifying Drake-only scope.
  Dev's September 28 full run verified this still-uncommitted implementation at 669/47.
- Permission: Dev explicitly authorized both agents to maintain the journal and shared
  documentation on September 23. The former Cowork-only restriction is superseded.
  Existing sbt/commit/push and new-issue restrictions remain in force.

## Completed Development

- Draco-owned Text is realized as native Scala String. Nominal, Surface, DomainLine,
  DomainLineOf and Emission name Text in their definitions without changing runtime representation.
- DomainAspectText and DomainAspectOf generate the complete domain section used by Drake.emit
  and thus the existing generator rule flow. Parameterized references and member order survive;
  superDomain remains in DracoAspect. Type-parameter spelling is still a callback to the
  handwritten Drake renderer.
- DracoAspectText and DracoAspectOf now provide the complete fundamental type-section
  layout/mapping on that same path, including reference/root/factory-result elision.
  Four callbacks retain type-parameter, type-form, value-type-slot and element-body
  rendering in Drake. SuperDomain stays owned by DracoAspect and output in the domain
  section. Rule/actor emission and codec rejection remain unchanged; Scala generation
  has not migrated. Seven focused tests and two frozen authored-ahead fixtures were added.
- DomainLine and DomainLineOf remain compatibility helpers. Follow-on transport/codec
  work is authorized as described under Next Increment, not inferred from a proposal.
- Supporting records: `draco-git-record/git-record-2026-09-22-1231`, `-1331`, `-1339`,
  and `-1453` (the latter contains the combined commit message).

## Verification

Dev ran the full suite on September 28, finishing at 17:04:22:
**669 tests / 47 suites passed**, zero failures or aborted suites. Pasted output and
the saved /tmp/sbt-test.log agree; Codex also checked the file-only baseline.

- Complete domain sections: 103 reproduced, 1 parameterized included, 0 skipped.
- Complete Draco sections: 103 reproduced, 2 frozen authored-ahead baselines, 0 skipped.
- GenDrake: 101/101 emitted, 0 missing or unexpected.
- Parse scope: 103 draco + 10 mods; surface losses: 1 across 113 types (known ActorAspect loss).
- Example generation: 28 match / 20 differ / 0 error / 0 missing of 48.
- PON: 80 numbers / 550 expressions / 42 discrepancies; canonical differences: 7.
- Scenario: 23 clean; forest scenario passed; mods actors pending Drake authoring: 0.
- Known Evrete subtype co-declaration limitation unchanged; it is not a newly fixed behavior.

These full-suite results apply to the current Draco-aspect implementation, before
any ZeroMQ/codec changes. On September 28 Codex also compiled the changed Scala and new test in an isolated overlay,
then passed 538 tests across seven suites: DracoAspectTest, DomainAspectTest,
DracoGenTest, DrakeGenTest, DrakeParseTest, GenDrakeTest and SourceContractTest.
The current scoped measurements are 103 Draco + 10 mods, one loss across 113,
GenDrake 101/101, and both aspect sections reproduced across 103 definitions with
none skipped. The Draco test uses two documented pre-migration fixtures for the
authored-ahead ActorAspect and BodyElement surfaces. Comparing old/new emitters with
the same current resources produced 103 byte-identical Drake and 103 byte-identical
Scala projections. Dev's subsequent full run confirmed 669 tests / 47 suites and
all report-only baselines. Codex did not run sbt.
Temporary probe log: /tmp/draco-aspect-probe.log; it may be overwritten.

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
- The September 28 implementation and its Drake-only authorization are recorded in
  this conversation and git record 2026-09-28-1634, not yet a new journal chapter.
  Chapter-sync markers remain 83 even though architecture docs include this increment.

## Resumption

The chapter-83 reconciliation and shared skill are committed; Claude's checkpoint
refresh is ad6466a. The current Draco-aspect implementation is full-suite verified
and awaits Dev's separate commit/push before the transport/codec implementation.
Dev still runs sbt, commit and push commands; no new GitHub issue is authorized. Missing
historical dialogue is not permission to invent it or restart completed implementation.

## Next Increment

Dev authorized the previously proposed order on September 29: close the verified
Draco-aspect increment independently, establish the messaging/codec contracts, build
an end-to-end early-access slice in src/mods, then use it to settle actor/codec
additions before resuming definition-backed aspect migrations. Rule-aspect migration
is deferred. No ZeroMQ dependency or new codec syntax has been implemented yet.

Accepted message flow, from Dev's clarification:

- ZeroMQ transports text containing JSON. Incoming text enters the ZeroMQ actor's
  working memory. Rules there parse it into the target language's JSON value.
- Those rules extract the destination-domain information, resolve it through the
  active domain dictionary and dispatch JSON to that domain's input actor.
- The input actor's message type is JSON, not a transport-selected Draco type.
  Its rules convert JSON to the domain's typed data.
- The output domain actor converts typed results to JSON. The ZeroMQ actor's rules
  wrap that payload with source-domain identification and send it to the ZeroMQ
  destination. A separate adapter must not take over rule-owned parsing or routing.

Next: inspect existing dictionary/assembly and codec contracts, choose a bounded
scenario, and propose concrete envelope fields, endpoint association, failure behavior
and default/custom codec semantics. Those details are not yet Dev's decisions.
Prototype implementation belongs in src/mods/{resources,scala}/draco, with tests in
src/test. Transport-specific fields are not automatically additions to ActorAspect.
This planning evidence is in the conversation/handoff, not yet a new journal chapter.
