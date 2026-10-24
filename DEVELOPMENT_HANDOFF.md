# Development Handoff

Updated October 23, 2026 by Codex after Dev verified the Marine repair.
This is the current checkpoint; the journal and git records preserve historical checkpoints.
Follow DRACO.md section 7 and verify the actual tree on arrival.

## Checkpoint and permissions

- Observed branch: `main`, HEAD **3608fab**, committed and pushed by Dev after the 880 / 64 gate.
  Status was clean and aligned with origin/main on receipt; Dev’s pasted output confirms push.
  The Aerial repair is committed. The accepted Marine OriginateReport repair is uncommitted.
- Latest verified baseline: **886 tests / 65 suites**, Dev's October 23 full sbt gate on
  the Marine OriginateReport repair, green on its first run. Zero failed/canceled/ignored/
  pending tests and zero aborted suites. Example map 30/18; zero losses across 127 types;
  parse scope 110 + 17. All other headlines unchanged, including pending mods actors 0.
  Await Dev's path-scoped commit and push; HEAD remains 3608fab.
- Do not run sbt, git commit or git push. Give Dev commands and wait for results. Direct
  compiler/projection probes have been used (overlay the current resources on a copy of the
  compiled classes under `target/scala-2.13/`, recompile the changed engine into it, put
  recompiled test suites on the scalatest Runner's runpath rather than the classpath); they do
  not replace Dev's full gate, and they missed suites more than once this session — run every
  discovered suite, with the test resources on the loader's roots, before handing over a gate.
- One design point per reply; one recommendation with accept-or-correct; build only what Dev
  accepts. After each verified commit, bring the next design point tied to the existing work.
- New GitHub issues require explicit approval every time. None created or modified here.
- Shared project documentation is the cross-agent memory. No private Claude/Codex memory is
  required or overwritten. The copyable prompt is in DEVELOPMENT_RESUME_PROMPT.md.

## Current receiving-session increment (Codex, October 22)

Dev accepted Presence-typed role slots and field-level encoding. Implemented and committed by Dev as b04d81d:
Absent omits a field; Present writes its aspect object, including empty objects. The generator
supports these two role-aspect field types, with no general standalone Presence codec.
Role readers, rebuilds, completeness and stub checks, dreams and inspection scripts are
migrated. BodyElement and ActorAspect are canonical plain types; frozen fixtures and
exclusions are removed. Four new tests plus four restored tests yield 857 / 61.
Final direct full-suite verification passed **857 tests**, 62 suites including Runner discovery,
zero failures/canceled/ignored/pending tests and zero aborted suites. Dev subsequently verified 857 tests / 61 suites.
Current main/mods/test resources were included in loader roots. Surface losses: 0 across 120;
DracoAspect: 110 sections, 0 frozen, 0 skipped. Every other headline matches the historical
baseline below, including pending mods actors 0. Dev’s first sbt gate passed with matching figures; Dev committed and pushed b04d81d.
The first probe location in /tmp broke repository-root discovery; corrected to target/scala-2.13.
An intermediate run caught missing compact records and a domainless test fixture; both corrected.
The final run had local socket access for existing ZeroMQ tests after sandbox binding failures.
Observed starting HEAD is 4dcef69, the documentation commit following 7b1bfea.
Record: git-record-2026-10-22-0800. Journal coverage stays at chapter 104; this session
is not yet journaled. No sbt, commit or push has been run by Codex.

## Completed in the previous session (Claude Code, October 21–22)

| Commit | Increment | Dev's full gate |
|---|---|---|
| b95419c | Two-stage loading: `DomainBuilder.identities` / `load` | 839 / 59 |
| 7d5de4f | The unit type is the empty Objective `()` | 840 / 59 (second run) |
| 44a5005 | `draco.scalatarget.HostBindings`; Local listed in Draco | 845 / 59 (second run) |
| 0b8fc03 | Seventeen Dracon referrers compact; canonical scoped generation | 845 / 59 (third run) |
| 773b6d9 | The resolution record, `uses` | 849 / 60 |
| d279709 | The canonical corpus swept | 849 / 60 (second run) |
| 7b1bfea | Formals in `DracoAspect.typeParameters`; TypeName actuals only | 849 / 60 |

All seven commits and pushes were performed by Dev, with results preserved in the transcript
extract. Chapters 100–104 cover them; chapter 99 ends with the Codex handoff this session received.

Durable contracts:

- `DomainBuilder.identities(declarations, path)` composes the identity-only ontology of declared
  domains from anchors alone; `DomainBuilder.load(declarations, bindings, path)` resolves every body
  through NameResolution over it. Nothing is discovered; the companions load unscoped.
- The unit type is the empty Objective `()`, spelled `Unit` by the Scala target; no host leaf names it.
- `HostBindings` declares the Scala target's host vocabulary as `[(Text, TypeName)]` pairs, supplied
  explicitly to `load` and to Generate; JDK-implicit names are bound bare; the hand-written engine
  objects (DracoGenerator, Drake, NameResolution, Generated) are engine bindings.
- Scoped generation CANONICALIZES: compact, then qualified only where the engine cannot read a bare
  name (`DracoGenerator.readable`), then shortened back by selective import; the output does not
  depend on the author's spelling. The compact form keeps host leaves as authored, shortens only
  names unique across ontology and vocabulary (draco.rete wraps stay bare), keeps nominal anchors in
  full and spells anchor parameters own-package bare, else full.
- A definition carries `uses`, its resolution record, written by compact, validated on every scoped
  read, dropped by resolution; `NameResolution.recorded(td)` is the lenient scope a file carries in
  itself; the engine reads it only during a generation. Unscoped, scoped and pinned generation
  agree on every definition of the canonical corpus (ResolutionRecordTest pins it).
- The canonical corpus is swept: every main definition but BodyElement and ActorAspect, and the
  staged dreams and service definitions, carry compact bodies; the example domains are not swept.
- A definition's header formals are `TypeParameter` records in `DracoAspect.typeParameters`; its own
  TypeName is bare; a TypeName's parameters are a reference's actuals. Rebuild sites that carry the
  slot: `TypeLoader.rooted`, the generator's `targetTypes` and family merge.
- The test suites that generate corpus Scala supply `CanonicalScope.scope` (a test helper) explicitly;
  PrimesRulesTest writes its generation into src/main and is scoped for that reason.

## Verification and lessons

| Headline | Verified value (7b1bfea) |
|---|---|
| DomainAspect | 110 complete, 1 parameterized included, 0 skipped |
| DracoAspect | 110 complete, 2 frozen authored-ahead baselines, 0 skipped |
| GenDrake | 108/108, 0 missing, 0 extra |
| Example-domain generation | 28 match, 20 differ, 0 error, 0 missing / 48 |
| Surface losses | 1 across 120 types, ActorAspect type form |
| Parse scope | 110 draco + 10 mods, 0 held back |
| PON / canonical | 80 numbers, 550 expressions, 42 discrepancies / 7 differ |
| Scenario | 23 files, all clean |
| Forest | 1 of 2 ash alarms crossed to birch |
| Subtype visibility | CO-DECLARATION DROPS THE FACT |
| Pending mods actors | 0, file-only DrakeGenTest.log |

Lessons the session paid for, each in its record: the §6 rebuild rule bit three times (`TypeLoader.rooted`
and the generator's rebuilds silently dropped `uses`, then `typeParameters`; compile the regenerated
loader BEFORE regenerating the corpus); the engine keys decisions on bare spellings, so scoped generation
must canonicalize rather than resolve; the engine's loader must apply a record only during generation or
DomainBuilder and the companions disagree; a `git checkout` of a directory holding uncommitted edits
reverts them — restore specific files; a probe that omits a suite or the test resources from the loader's
roots misses what Dev's gate finds.

## Deferred work and priority context

- The accepted presence increment above is verified, committed and pushed as b04d81d.
- The 48 example domains under `src/mods/resources/domains` are outside the canonical ontology,
  report-only (28 match / 20 differ) and unswept; whether they join under their own declared ontology
  is an open question, not put to Dev.
- Activation (an explicitly supplied Assembly of held actor definitions with factory-supplied
  executable constructors) and the `.dcon` extension remain deferred; no Haskell target exists.
- #43 broader codec-aspect emission, #12 Dreams, inferred derivation and membership reciprocity remain
  context, not authorizations. Persistence belongs to the consuming project; ZeroMQ choices are provisional.

## Documentary coverage and gaps

Chapters **100–104** cover the Claude Code session from the Codex handoff through Dev's close request,
with audit `draco-dev-journal/sources/claude-2026-10-21-to-22-audit.md`: 55 original records, 28 user
messages (11 assertive, 17 responsive), 27 final replies, 27 complete exchanges plus the open close
request. No compaction was observed in the frozen prefix; the reply completing this documentation pass is
outside it and can be recovered by the next pass. Chapters 95–99 (Codex) and 94 (Claude) keep their
historical Status sections. Both documentation sync markers advance to 104 after reconciliation.

## Executable empty-role coverage (accepted by Dev, implemented, Dev-verified, committed and pushed)

Four additional RolePresenceTest cases compile and load all four role combinations after
carrier round-trip, register and fire a session for declared rules, and exercise empty
actors with String and Int payloads through BehaviorTestKit. All cases passed without
production changes. The combined projection supports both checks. Empty action invocation
count is not measured. Full compiled-class run: 861 tests, 62 suites including discovery,
zero failures or aborts; all report-only headlines unchanged, pending mods actors 0.
Current resources, including test resources, were on loader roots. Dev’s first full sbt gate
verified 861 tests / 61 suites, no failures or aborts and all headlines matching.
Record: git-record-2026-10-22-0739. Dev committed and pushed this increment as 375c513; his pasted output confirms the push.

## Next proposal after 375c513 (Codex; unaccepted)

Measure the example domains against an explicitly declared resolution scope before proposing
a sweep. ExampleDomainsGenTest still generates 48 definitions unscoped and reports 28 match /
20 differ without asserting agreement. CanonicalScope lists no example domains. Aerial's
anchor lists four data/rule members but not its Creator, Consumer or Input actors, so a scope
built only from existing membership lists may not cover the files the report visits.
Recommend a read-only audit of declared domains/membership, resolution with HostBindings,
and scoped versus unscoped output over all 48 files. Report exact gaps and distinguish naming
changes from existing projection differences. No membership edits, corpus rewriting or new
issues are authorized by this proposal. Activation and .dcon remain deferred.

## Example-domain audit completed (accepted read-only scope)

See EXAMPLE_DOMAIN_AUDIT.md for scope, per-file evidence and limits. At 375c513:
32/48 identities held; 16 omitted members point at the same six owners. 45 resolve,
three first failures (AerialSink, HCursor, Cartesian). A diagnostic scope adding those
and Position resolves all 48, without changing membership files. Scoped output:
34 unchanged, 11 spelling-only changes, 3 errors; comparison with committed Scala:
23 matches / 22 differences / 3 errors. Unscoped remains 28 / 20 / 0 / 0.
The next unaccepted recommendation is reciprocal membership for the 16 already-owned
example definitions plus a consistency check, before host-binding or projection repairs.
No code or corpus changes; verified baseline stays 861 / 61. Audit/checkpoint docs are uncommitted.

## Example membership increment (October 23, accepted, implemented and Dev-verified; committed as 1d69df1)

Dev accepted the audit's membership recommendation. Five domain lists now include the 16
already-owned definitions; Sentient is unchanged. Added missing Drake surfaces for those
five domains, parsed their JSON back, and changed only membership lists in their existing
hand-written Scala companions. All 48 example identities are covered by declared domains.
Two ExampleMembershipTest checks plus ten newly active parser tests yield 873 tests / 62 suites.
Full compiled-class verification passed all 873 tests (63 suites including discovery), no failures or aborts. Body sweeping, host bindings, and projection
repairs remain separate, unaccepted work. Record: git-record-2026-10-23-1048.

Dev’s first full gate on October 23 verified 873 / 62, zero failures or aborts, every
expected headline matching, including the pasted file-only pending mods actors 0.
Dev committed and pushed 1d69df1.

## Design point after 1d69df1 (subsequently accepted)

A fresh read-only resolution probe using current compiled classes and the explicit canonical
plus six-example declaration set now holds 48/48 identities and resolves 46/48 definitions.
Only aerial.Consumer (domains.aerial.AerialSink) and aerial.Input (io.circe.HCursor) fail.
Cartesian and Position resolve through the repaired domain membership.

Recommendation: complete the explicit example resolution scope. Add io.circe.HCursor to
ScalaTarget HostBindings, supply domains.aerial.AerialSink as a caller-owned binding in the
example scope, and gate two-stage loading of all 48 examples. AerialSink is a hand-written
observation boundary with no JSON definition, so it belongs to the example caller's bindings,
not the shared target vocabulary or domain membership. No body sweep or projection repair.
Dev accepted this recommendation; the implemented result follows.

## Example resolution bindings (October 23, implemented, verified, committed and pushed)

Dev accepted HCursor in HostBindings, AerialSink in the example caller's bindings and a
loading gate. The HostBindings trio is updated; ExampleScope supplies explicit canonical
plus six-example declarations and the caller sink. ExampleLoadingTest adds two tests for
all 48 definitions, order independence and missing-binding failures. No body/projection edits.
Dev verified 875 / 63, every headline unchanged. Full compiled-class verification passed: 875 tests, 64 suites including Runner discovery, zero failures or aborts, every headline unchanged and pending mods actors 0. Current main/mods/test resources were on loader roots. Dev’s first full sbt gate passed 875 / 63 with zero failures/canceled/ignored/pending tests, zero aborted suites and all headlines matching. Dev committed and pushed ae0264e.
Record: git-record-2026-10-23-1106. No sbt, commit or push by Codex.

## Design point after ae0264e (accepted)

Start projection repair with domains.aerial.OriginateReport alone. Its JSON action calls
originate(intent), but the conversion exists only as a private method in hand-written Scala;
the definition has no corresponding body. Existing behavior reads callsign (default UNKNOWN)
and flightLevel (default 0), emits POSITION and altitudeFeet = flightLevel * 100.
Recommend authoring that existing conversion in the rule's Drake/JSON definition, regenerating
its Scala, and adding exact projection plus behavioral checks (normal input and missing fields).
Keep the rule's existing consumer contract. Do not sweep the other examples or introduce a
shared conversion abstraction in this step. Dev accepted this scope. The implementation and verification checkpoint follows.


## Aerial OriginateReport repair (October 23, accepted, implemented and Dev-verified; committed as 3608fab)

The new Drake surface owns the existing conversion. Its parsed JSON and generated Scala
agree exactly. OriginateReportTest checks all three projections and fires the rule for
normal input and absent fields, retaining the consumer callback and PositionReport payload.
Only this rule is repaired; the other example bodies remain outside the accepted scope.

All main/mods/domain sources and all test sources compiled directly. Every discovered suite
passed: 880 tests / 65 suites including Runner discovery, zero failed/canceled/ignored/pending
tests and zero aborted suites. Current main, mods and test resources were on loader roots.
Dev’s first full sbt gate passed: 880 tests / 64 suites (three new focused tests plus two newly active
parser checks). Example map is 29 match / 19 differ / 0 errors / 0 missing; parse scope
110 draco + 16 mods; surface losses 0 across 126 types. All other report-only headlines
unchanged, including file-only pending mods actors 0. No sbt, commit or push by Codex.
Dev’s pasted gate confirms zero failed/canceled/ignored/pending tests, zero aborted suites
and all expected headlines. Latest verified baseline is 880 / 64; Dev committed and pushed 3608fab.
Record: git-record-2026-10-23-1216. Journal coverage remains through chapter 104.


## Design point after 3608fab (subsequently accepted)

Repair domains.marine.OriginateReport alone. Its JSON still calls originate(intent), while
only handwritten Scala owns the conversion. Recommend carrying the existing VoyageIntent
conversion into Drake/JSON and regenerating Scala, preserving FixReport, kind FIX, vessel
default UNKNOWN, depthMetres default 0, and integer depthFathoms = (depthMetres * 10000) / 18288.
Pin exact projections and callback behavior for normal, non-integral conversion and missing
fields. Preserve current arithmetic; no shared abstraction or other example sweep. Dev accepted this scope; the implementation and verification checkpoint follows.


## Marine OriginateReport repair (October 23, implemented and Dev-verified; uncommitted)

The new Drake surface owns the existing VoyageIntent-to-FixReport conversion. Parsed JSON
and generated Scala agree exactly. Four focused tests check the trio and callback delivery
for exact conversion, fractional truncation and absent fields. Preserve kind FIX, vessel
UNKNOWN, depthMetres 0 and integer depthFathoms = (depthMetres * 10000) / 18288.
No engine or other example changes; the previous Aerial repair remains committed at 3608fab.

All main/mods/domain sources and all test sources compiled directly. Every discovered suite
passed: 886 tests / 66 suites including Runner discovery, zero failed/canceled/ignored/pending
tests and zero aborted suites. Current main, mods and test resources were on loader roots.
Dev’s first full sbt gate passed: 886 / 65 (four focused tests and two newly active parser checks).
Example map 30 match / 18 differ / 0 error / 0 missing; parse scope 110 + 17; surface losses
0 across 127 types. Every other headline unchanged, including file-only pending mods actors 0.
Dev’s pasted gate confirms 886 / 65 with zero failed/canceled/ignored/pending tests, zero
aborted suites and all expected headlines matching. Commit and push are pending. No sbt, commit or
push by Codex. Record: git-record-2026-10-23-1613. Journal coverage stays at chapter 104.
