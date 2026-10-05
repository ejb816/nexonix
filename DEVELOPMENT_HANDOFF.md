# Development Handoff

This is the shared resumption checkpoint for Claude Code and Codex, not an additional
architecture specification or a transcript. Follow DRACO.md section 7 and verify this
snapshot against the current tree before acting.

## Checkpoint

- Updated: 2026-10-05, by Codex, after reviewing Dev's successful full-suite bridge run.
- Observed branch/HEAD: main at 604aff2, matching the local origin/main tracking ref.
  Dev supplied successful commit/push output for this checkpoint; no fresh remote query.
- The Service/contract/journal closeout is committed. Tree was clean before this
  increment. Current changes include the prior planning docs, chapters 88-89 and
  source audit, the timestamped record rename, JeroMQ dependency, TextOutput/Service
  trios, handwritten bridge engine and tests. Nothing from this increment is committed.
- Project-local continuity is carried by this handoff, DRACO.md and the Service plan.
  No private agent memory was overwritten or relied upon as shared authority.
- Permission: Dev explicitly authorized both agents to maintain the journal and shared
  documentation on September 23. The former Cowork-only restriction is superseded.
  Existing sbt/commit/push and new-issue restrictions remain in force.

## Completed Development

- The staged draco.service.Service domain now loads as a self-declaring dictionary
  peer, now containing TextOutput. DomainBuilder validates and generates it and
  composes it with Base. The bounded ZeroMQ bridge is staged; authoring operations,
  ServiceConfiguration, initialization client and persistence remain unimplemented.
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

September 30 Service increment: generated JSON/Scala, directly compiled the new
companion and ServiceTest in an isolated overlay, and passed 15 tests / 2 suites
(ServiceTest and DomainBuilderTest). Dev then ran the full suite: **674 tests / 48
suites passed**, zero failures or aborted suites, finishing September 30 at 10:38:48
America/New_York. Pasted results agree with /tmp/sbt-test.log; the file-only pending
Drake report is zero. The existing corpus measurements below remain unchanged: those scans do not
include staged draco definitions; five dedicated ServiceTest checks cover this trio.

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

The September 28 results apply to the Draco-aspect implementation before Service;
the September 30 results include Service. Neither run verifies a ZeroMQ/codec
implementation; the new bridge has only the later scoped verification below. On September 28 Codex also compiled the changed Scala and new test in an isolated overlay,
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

September 30 resumption review: Codex reviewed the complete pending diff and checked
the preserved transcript against the original log, including its frozen prefix hash
and all 53 message texts, hashes, timestamps and source order. Chapter 84-87 final
replies match the extract. Agent-link reporting and whitespace checks passed.
Runtime files remain unchanged from Dev's verified Service run; no new sbt run is claimed.

Documentation checks on September 23 passed: staged/unstaged whitespace, chapter links,
fence balance and required sections, chapter-83 markers, and preservation of all 33
non-responsive source turns in chapter 81. The local helper passed Bash syntax and
isolated checks for non-mutating reporting, creation of missing links, and refusal to
replace regular files or wrong-target links. Agent links remain tracked symlinks; no
runtime file or .gitignore changes were found. These checks are not a new sbt result.

## Documentation Coverage

- The previous journal update (chapters 81-83) covered the range after chapter 80's
  `5fd801d` through the September 23 documentation handoff. Current coverage reaches
  chapter 89 as detailed below.
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
- Chapter 84 now covers the September 27 sharing decision and commit `79b363f`.
  Historical local-only statements describe
  the September 23 state and are not rewritten retrospectively.
- Chapters 85-87 now cover Drake-only Draco-aspect generation, its September 29 push,
  rule-owned messaging, Service-first staging, the 674/48 full run and transcript integrity.
  At that checkpoint both documentation sync markers reached chapter 87. Chapter 83 gained a provenance
  addendum without changing its historical Status; chapters 81-82 remain unchanged.
- The bounded transcript/audit under draco-dev-journal/sources preserves 53 messages:
  27 user and 26 final replies, 26 complete exchanges plus the open audit request.
  Five exchanges audit chapter 83; 21 feed chapters 84-87. Original records survive
  both observed compactions. No loss is observed in that recovered sequence, but no
  independent inventory proves complete original capture. Earlier source gaps remain.
  The source boundary precedes this documentation work's closing response.

Chapters 88-89 now cover the prior task's closing reply, the 604aff2 commit/push,
and this task's Service decisions through the bridge authorization. A new bounded
extract preserves 23 messages (12 user, 11 final replies), 11 exchanges plus the open
request. One exchange overlaps chapter 87 to close its formerly open request. Both
sync markers reach 89; prior chapter statuses and the September 30 audit are unchanged.
The current bridge implementation is after that source boundary, so its implementation
results belong here and in the git record until a later sourced journal pass.

## Resumption

604aff2 is the committed checkpoint. The pending bridge is directly compiled and
verified: 548 tests / 7 suites passed (ServiceTest, ZeroMqBridgeTest, DomainBuilderTest,
DracoGenTest, DrakeGenTest, DrakeParseTest, GenDrakeTest). Scope measured 103 draco +
10 mods, one loss across 113 types, GenDrake 101/101, with zero pending mods actors in
DrakeGenTest's file report. Dev subsequently verified the full suite as recorded below.

The sandbox initially denied local socket binding; the socket probe then passed with
loopback access. A wider probe initially aborted because Main.roots derives paths
from compiled-class location; recreating target/scala-2.13/classes layout in /tmp
fixed the harness. Current source/resource overlay and dependency jars are isolated
under /tmp/draco-zmq-check; /tmp/draco-zmq-probe.log contains the passing run.

Dev's full sbt run passed 685 tests / 49 suites (11 additional tests, one suite),
zero failures/aborts, finishing October 5 at 13:01:12 America/New_York. Pasted output
matches /tmp/sbt-test.log; all report-only figures match DRACO.md, including the
zero-pending-Drake file report. This is the latest verified full-suite baseline.
Only verification documentation changed after that run. The increment is ready for
Dev's scoped commit/push; wait for those results before the next implementation.
Codex did not run sbt, commit or push.

The pending git-record-2026-10-05-service-plan was renamed to -1107 using the source
closeout time (15:07 UTC / 11:07 EDT). Its old path may still appear as an index addition
plus working-tree deletion until Dev stages the rename; include both paths in scope.
Committed descriptive filenames remain historical exceptions. Future names use HHMM.

## Next Increment

Accepted decisions from the current task, recorded October 5:

- ServiceConfiguration contains Assembly; other proposed member shapes remain open.
- The primordial external client creates/modifies domain and type definitions and
  creates/modifies a DomainDictionary. First implement authoring, validation and
  dictionary composition. Generation and activation are later explicit operations.
- An endogenous initialization client builds the initial Draco domain dictionary
  from supplied canonical JSON definitions using existing actors/messages. It needs
  no external transport and can be developed in parallel with ZeroMQ integration.
- Both client paths share service operations. Bootstrap ordering remains to be
  designed; a prebuilt dictionary is not the only intended initialization path.
- The consuming project owns persistence. Wire format/transport details belong to
  the ZeroMQ dependency integration, not the local initialization client's API.

The authorized first bridge now implements a provisional loopback PAIR, one-frame
UTF-8 protocol with payload-only input and source-tagged output to the same peer.
It uses a startup dictionary snapshot, exact identity and configured endpoints; the
JeroMQ peer test exercises Draco TypeName input/output rules. Production socket,
reply/correlation and failure-wire choices remain open, as do custom codec declarations.
Stage implementation in src/mods/{resources,scala}/draco and tests in src/test.
Rule-aspect migration remains deferred pending the service/transport work.

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

Inspection is complete: DomainDictionary does not contain live actor endpoints;
AssemblySpawner keys by namePath; host Codec requires both conversion directions;
codec-aspect selectors are not implemented. The contract proposes a bounded scenario,
envelope, explicit live-endpoint association, codec semantics and acceptance checks.
Those proposals are not yet Dev's decisions. A later transport question is whether
results go to a configured output destination or the originating peer, which determines
socket wiring and correlation requirements. Full-envelope versus payload-only forwarding
and custom-codec declaration representation also remain explicit design choices.
Prototype implementation belongs in src/mods/{resources,scala}/draco, with tests in
src/test. Transport-specific fields are not automatically additions to ActorAspect.
The earlier transport planning and Service establishment are journaled in chapters 86-87.
The old documentation closeout added no runtime behavior; this increment adds the
bridge and dependency, but no configuration types or codec selectors. The transcript rules are shared through DRACO.md and
the tracked documentation skill; no private memory store was overwritten.
