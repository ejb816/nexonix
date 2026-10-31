# Development Handoff

Prepared October 26, 2026 by Codex for a new session in either Codex or Claude Code;
refreshed by Claude Code through October 30 after the named-construction repairs, the
terrestrial Output repair, the report and intent json factories and the coordinate surfaces.
This checkpoint supersedes the accumulated historical checkpoints in earlier versions of
this file. Git records, commits and journal chapters retain their historical evidence.
No destination session has been launched or messaged.

## Start here

Read DRACO.md (AGENTS.md and CLAUDE.md are symlinks), this file, and
`.draco/skills/update-draco-docs/SKILL.md`. Inspect status and log before editing.
Use DEVELOPMENT_RESUME_PROMPT.md as the opening prompt. Private agent memory is not required.

- Branch: main. Code commits this session: **e241378**, the named-construction head repair
  (record `git-record-2026-10-26-1449`); **9779e39**, the dyn-result increment (record
  `git-record-2026-10-26-1534`); **112f277**, the head-keyed factory lookup (record
  `git-record-2026-10-29-0755`), all three committed and pushed by Dev; and **c5b8089**, the
  terrestrial Output repair (record `git-record-2026-10-29-1640`), all four committed and pushed
  by Dev; and **e138f04**, the LocationReport json factory (record `git-record-2026-10-30-1038`), Dev-gated
  at 947 / 74 and committed and pushed by Dev on October 30; then the sibling report factories
  (record `git-record-2026-10-30-1113`), Dev-gated at 956 / 75 and committed and pushed by Dev as **8540341** on October 30; then the Intent json factories (record
  `git-record-2026-10-30-2014`), Dev-gated at 967 / 76 and committed and pushed by Dev as **3a3cbfc** on October 31; then the coordinate surfaces and companions (record
  `git-record-2026-10-31-0842`), Dev-gated at 974 / 77 and committed and pushed by Dev as **23333f3** on October 31; then the
  example map closed as a gate (record `git-record-2026-10-31-1105`), Dev-gated at 982 / 77 and committed and pushed by Dev as **7be1497** on October 31; then the
  example body sweep with the validator's identity comparison (record `git-record-2026-10-31-1436`), Dev-gated at 983 / 77 on October 31, commit PENDING —
  inspect `git log -1` and `git status --short` to learn where they stand. Before them a8b5447 (Consumer) and 7495539
  (the documentation handoff).
- Latest Dev-verified gate: **983 tests / 77 suites**, October 31, on the example-sweep tree (record
  `git-record-2026-10-31-1436`, commit pending): zero failed/canceled/ignored/pending tests, parse scope
  110 + 37, surface losses 0 across 147 types, example gate 48 match / 0 differ, every other headline
  unchanged, pending mods actors 0. Before it 982 / 77 on the example-gate tree (7be1497).
- In the tree, Dev-gated at 983 / 77 on October 31 but NOT yet committed: the example body sweep —
  twelve example trios in the compact form with `uses` records (surfaces re-emitted, JSON re-canonicalized,
  Scala regenerated), `AssemblyValidator` comparing message types by identity, WorldAssemblyTest +1, the
  record `git-record-2026-10-31-1436`, the CHANGELOG entry and this documentation. Direct run October 31:
  983 / 78 including discovery, zero failed/canceled/ignored/pending/aborted, every headline unchanged,
  pending mods actors 0. Dev's full sbt gate on October 31: 983 succeeded, zero failed/canceled/ignored/pending,
  every headline matching, pending mods actors 0 — 983 / 77, green on its first run.
- All accepted implementation work through 7be1497 is committed and pushed.
- Journal coverage reaches chapter 112. The October 26 Claude Code session (handoff receipt,
  verification of the reading, the accepted repair) is NOT yet journaled: chapter 113 is
  pending, to be sourced from the session's original records.

## Named-construction head repair (accepted and built, October 26)

Dev accepted, in these words: "spell the named-construction head as authored/resolved, same as
the positional path, with one cross-package compile-and-run test checking scoped and unscoped
generation." `DracoGenerator.constructionInitializer` spells the applied head through
`expression(operands.head)`; the factory check and the `_name` parameter mapping are unchanged.
Unscoped, a cross-package construction keeps its qualification; scoped, the engine shortens it
and adds the selective import. `NamedConstructionTest` (src/test/scala/domains/world) generates,
compiles and runs a domains.world probe constructing domains.terrestrial.Location both ways and
pins an own-package bare head staying bare. Old and new engine generate byte-identical Scala for
the three named-argument sites (aerial/Input, terrestrial/Output, world/Observable) and Consumer;
no committed Scala regenerated.

## Dyn result as a named construction (accepted and built, October 26)

Dev accepted: "route a dyn's result (body `fix value` and leaf) through constructionInitializer
with the existing expression fallback, re-author Observable's `_geocentric:` as `geocentric:` with
its JSON re-canonicalized and Scala byte-identical, and add one NamedConstructionTest case."
`dynResult` renders every dyn result through the construction interception with `expression` as
fallback — never `defaultInitializer`, whose String-slot rule would quote the four Text-typed path
results in the corpus. One enabling change, flagged to Dev with the gate: `loadDracoType` looked up
only a dotted slot type, so the generator now keeps `definitionInEffect` (a lazy thread-local set
while a definition is spelled) and resolves a bare slot type against that definition's package, as
the codec judge does. Observable's JSON moved one leaf; Observable, Input, Output and Consumer
regenerate byte-identical. NamedConstructionTest has four cases.

## The head decides the construction (accepted and built, October 27)

Dev accepted: "key constructionInitializer's factory lookup on the applied head (dotted as
spelled, bare against the definition in effect) instead of the slot type, and add one
NamedConstructionTest case with a parent-typed slot, compiled and run." The lookup asks about
the head leaf; the slot type plays no part; `constructionInitializer` and `dynResult` take the
value alone. Nil corpus impact by construction. NamedConstructionTest has five cases. This closes
the three observations of October 26; nothing from the named-construction thread is preserved.

## Terrestrial Output carries its Location branch (accepted and built, October 29)

Dev accepted: "re-author Output's message as a case branch binding location to
domains.terrestrial.Location, with the three strict bindings and consumer(report), the report
constructed in the OriginateReport form, Output.scala regenerated from the definition, and one
OutputProjectionTest covering exact trio agreement, one report per Location, ignored other
messages and lazy consumer capture." The message body is `case location Location [ … ]` — the
branch type spelled bare, Location being in Output's own package — with two strict bindings
(`now fix payload`, `now fix report`; the accepted words' "three" counted the `mon consumer(report)`
line) and the report as `new LocationReport { … }` host text, the form OriginateReport already
carries. The JSON is re-canonicalized from the drake and Output.scala is the generator's output:
it gains the Terrestrial actor convention (`elementTypeNames`, a `knowledge` registering the
domain's two rules, as Creator and the terrestrial Consumer have) and renders the dispatch as
`case location: Location =>` with the generator's `case _ => ()`. OutputProjectionTest (4) is the
73rd suite. The example map moves to 37 match / 11 differ. LocationReport still has no factory, so
its construction stays host text; a `json` factory on it was named as adjacent, not recommended.

## LocationReport constructs through a json factory (accepted and built, October 30)

Dev accepted: "Build the LocationReport json factory with both terrestrial sites re-authored",
the recommendation being LocationReport's first drake surface carrying `factory parameters par
json Json` (the BARE spelling HostBindings carries and Dracon.drake authors; the qualified spelling
generates a `null.asInstanceOf` Null), its JSON re-canonicalized, its hand-written companion
replaced by the generator's output (which adds `apply(_json: => Json)`, the Dracon-shaped codec
and `Null`), and the two terrestrial sites — OriginateReport's action and Output's Location
branch — re-authored `fix report LocationReport LocationReport(payload)` and regenerated on that
one line each. No engine change. LocationReportTest (3) pins the trio, the by-name argument and
the codec round-trip; the existing OriginateReportTest and OutputProjectionTest pin the two
regenerated sites unchanged. Parse scope moves to 110 + 24, surface-loss coverage to 134 types;
the example map stays 37 / 11. Direct run of every discovered suite on October 30: 947 passed, 75
suites including discovery, zero failed/canceled/ignored/pending/aborted, every headline as stated,
pending mods actors 0 (the first run stopped at test compilation on an accessor in the new test,
corrected; no production file changed between runs). Dev's full sbt gate on October 30: 947 / 74, zero failed/canceled/ignored/pending, every headline matching, pending mods actors 0, green on its first run.

## The sibling reports take the json factory (accepted and built, October 30)

Dev accepted: "Build the three sibling report factories in one increment". Aerial PositionReport,
marine FixReport and ethereal EphemerisReport each gain the drake surface with `factory parameters
par json Json`, JSON re-canonicalized, companion replaced by the generator's output (27 lines
added, none removed), and each medium's OriginateReport re-authored `fix report X X(payload)` and
regenerated on that one line. No engine change. ReportFactoryTest (3), one suite iterating over the
three reports, pins the trios, the by-name argument and the codec round-trip; the four
OriginateReportTests pin the regenerated rules unchanged. Parse scope moves to 110 + 27,
surface-loss coverage to 137 types; the example map stays 37 / 11. Every report in the example
chain now constructs through its definition; the Output thread of October 29 is closed. Direct run of
every discovered suite on October 30: 956 passed, 76 suites including discovery, zero
failed/canceled/ignored/pending/aborted, green on its first run, every headline as stated, pending mods
actors 0. Dev's full sbt gate on October 30: 956 / 75, zero failed/canceled/ignored/pending, every
headline matching, pending mods actors 0, green on its first run. Committed and pushed by Dev as 8540341.

## The four Intent types take the json factory (accepted and built, October 30; Dev verified 967 / 76 on October 31)

Dev accepted: "Accepted, build it". Aerial FlightIntent, marine VoyageIntent, ethereal LaunchIntent and
terrestrial MarchIntent — each medium's entry message, the fact its OriginateReport joins — gain the drake
surface `type X from <Medium> draco format dracon Dracon` with `factory parameters par json Json`, JSON
re-canonicalized (the factory record only), companion replaced by the generator's output (25 lines and two
imports added, none removed). The eight test sites that built an intent as `new X { override lazy val
typeDefinition …; override (lazy) val json = … }` — the four OriginateReportTests, the marine, ethereal and
terrestrial chain tests, AerialAssemblyTest — now construct `X(payload)`. No engine change; no definition
constructs an intent (Creator's messageType is Dracon). IntentFactoryTest (3), one suite iterating over the
four, pins the trios, the by-name argument and the codec round-trip. Parse scope moves to 110 + 31,
surface-loss coverage to 141 types; the example map stays 37 / 11. Every message in the example chain now
constructs through its definition; the *Intent observation of the report records is closed. Direct run of
every discovered suite on October 30: 967 passed, 77 suites including discovery, zero
failed/canceled/ignored/pending/aborted, every headline as stated, pending mods actors 0. Dev's full sbt
gate on October 31: 967 / 76, zero failed/canceled/ignored/pending, every headline matching, pending mods
actors 0, green on its first run. Committed and pushed by Dev as 3a3cbfc.

## Position and Location take their surfaces and the generator's companions (accepted and built, October 31; Dev verified 974 / 77)

Dev accepted: "Accepted, build it". Aerial Position and terrestrial Location take the drake surfaces
`DrakeCLI emit` renders from their JSON (`type Position from Aerial`, three `fix` elements, a factory with
three defaulted parameters); parsing each back reproduces the committed JSON byte for byte, so no JSON
moves. Their hand-written companions are replaced by the generator's output, which differs only by the
codec inferred from the elements (21 lines: encoder by field name, decoder with the factory default for a
missing field, the imports), Cartesian's accepted inferred codecs of October 25 being the precedent; trait,
loader line, kind-vals, `apply` and `Null` are unchanged, so Input, world Consumer and five tests construct
as before. No engine change. CoordinateProjectionTest (3), one suite iterating over the two, pins the trios,
the lazy defaulted factory and Null, and the codec with missing-field defaults and an invalid value refused.
Parse scope moves to 110 + 33, surface-loss coverage to 143 types, the example map to 39 / 9. Direct run of
every discovered suite on October 31: 974 passed, 78 suites including discovery, zero
failed/canceled/ignored/pending/aborted, every headline as stated, pending mods actors 0. Dev's full sbt
gate on October 31: 974 / 77, zero failed/canceled/ignored/pending, every headline matching, pending mods
actors 0, green on its first run. Committed and pushed by Dev as 23333f3.

## The example map closes and becomes a gate (accepted and built, October 31; Dev verified 982 / 77, committed as 7be1497)

Dev accepted: "Accepted, build it". The nine remaining example differences were all companion spelling — the
three ConsumeReport companions (ethereal, marine, terrestrial: `with DracoType` and `override`), the five domain
objects (Aerial, Ethereal, Marine, Terrestrial, World: the same, `import domains._` and the wildcard world
import) and aerial Input (`elementTypeNames`, the unused private `knowledge` Creator carries, lazy bindings in
`receive`) — and the four ConsumeReports had no drake surface. Built as probed with the compiled CLIs: the four
ConsumeReport surfaces are the emitter's rendering (`type ConsumeReport`, the domain line, `rule` with one `var
report <Report>` and a `mon` action), their JSON re-canonicalized by parsing them back (the host-opaque action
string becomes a call tree, aerial's `ctx.getRuntime().get("consumed")` a nested one; the aspect keys take the
canonical order), the Scala generated from either JSON identical, aerial's companion regenerating byte-identical;
the nine companions are the generator's output; `DrakeCLI check` is clean over the five media. ExampleDomainsGenTest,
report-only since 2026-06-23, now asserts zero differ, zero error and zero missing over the 48 example definitions,
and the example-map row leaves the DRACO.md table, the third measurement to become an assertion. No engine change,
no new suite. Parse scope moves to 110 + 37, surface-loss coverage to 147 types, eight DrakeParseTest checks
activate. The first direct run failed ExampleLoadingTest's all-48 resolution: the treed ConsumeReport actions name the
three sibling sinks, and resolution knew only AerialSink (bound by ExampleScope on October 23); ExampleScope now
binds the four sinks by qualified name and the loading test checks each is a binding, not a member. Second direct
run of every discovered suite on October 31: 982 passed, 78 suites including discovery, zero
failed/canceled/ignored/pending/aborted, every headline as stated, pending mods actors 0. Dev's full sbt gate on
October 31: 982 succeeded, zero failed/canceled/ignored/pending, every headline matching, pending mods actors 0 —
982 / 77, green on its first run. Committed and pushed by Dev as 7be1497.

## The example bodies are swept and the validator compares by identity (accepted and built, October 31; Dev verified 983 / 77, commit pending)

Dev accepted: "Accepted, build it" on the point raised after 7be1497 — sweep the example bodies as the canonical
corpus was swept on October 21, the one part of the corpus that sweep left out. MEASURED first with a probe over the
compiled classes, compacting all 48 example definitions through the example caller scope (ExampleScope's identities
and bindings): twelve change and gain a record — the four Creators and four OriginateReports, aerial Input,
terrestrial Output, world Consumer and Provider — `draco.format.dracon.Dracon` to `Dracon` (the host text inside
`ctx.getRuntime().get[…]` keeps its spelling), `domains.world.World` to `World` (recorded in Input, own-package bare
in Consumer and Provider including their `Actor(World)` header), Position and Location in Consumer's branch and
Input's binding, and the factory's `ActorType` recorded as the dreams records carry it; anchors (Output's
`messageType domains world World`, every derivation) stay in full; the other 36 are unchanged. Built as probed: the
twelve surfaces are `Drake.emit` of the compact form with a `uses` section, the JSON `DrakeCLI parse` of each new
surface (equal to the probe's compact JSON), the Scala `GeneratorCLI generate` over the current resources — two to
eight lines per file (Creators +8/−6, Input +5/−3, OriginateReports +3/−1, Output +4/−2, Consumer +8/−5, Provider
+3/−3), selective imports gained and the qualified spellings shortened, unscoped and scoped generation byte-identical
for all 48 — `DrakeCLI check` clean over the five media. ONE ENGINE CHANGE: `AssemblyValidator` compared a consumer
parameter's message type with the actor's by TEXT (`m.text` against `messageType.namePath`); run over the swept tree
for the record, the old validator fails both assembly suites with five errors of the shape "expects a consumer of
World but receives domains.world.World". Now `identity(td, text)` resolves a name through `NameResolution.recorded(td)`
(the definition's own record as the scope, the generator's reading of a recorded definition), completes a bare result
with the definition's own package (the compact form's convention for a bare name), and both sides compare by
`namePath` — name and package, parameters free, as definitions are keyed; nothing is loaded or discovered. The actor's
message type is a TypeName anchor the carrier holds in full (the surface elides Provider's own-package `messageType
World`), so the completion is exercised today only on the consumer side. WorldAssemblyTest +1 pins the three compact
spellings with their records, the actors' identities, the clean service and a crossed binding reported with both
identities in full; its first focused run failed on the test's own assertion that Provider's message type reads bare,
corrected before the direct run. No headline moves: twelve surfaces re-emitted, none added. Direct run of every
discovered suite on October 31: 983 passed, 78 suites including discovery, zero failed/canceled/ignored/pending/aborted,
green on its first run, every headline as stated, pending mods actors 0. Dev's full sbt gate on October 31: 983
succeeded, zero failed/canceled/ignored/pending, every headline matching, pending mods actors 0 — 983 / 77, green on
its first run. Commit pending.

## Next design point — acceptance required

None raised yet (Claude Code, 2026-10-31, after the example sweep). After the sweep every definition in the corpus is
in the compact form but the five sentient JSON-only types. OBSERVED, not recommended: Direction, Ego, Lean, Percept and
Waypoint carry a tuple value type as a STRING (`"(Double, Double)"`), the pre-tree form of 2026-09-17, so they alone
fail `parse(emit(td)) == td`; nothing re-canonicalized them because the sentient domain has no drake surfaces.
Deferred, neither accepted: the activation recommendation (an explicitly supplied Assembly of held actor definitions
with factory-supplied constructor bindings, Codex, 2026-10-15) and the .dcon migration. Inventory is context, not
authorization.

Receiving agent: bring one point as a recommendation with accept-or-correct; build only what Dev
accepts.

## Operating boundaries

- Do not run sbt, git commit or git push. Dev runs those commands; supply path-scoped blocks
  and wait for his results. Scope the commit itself as well as git add; use a message file.
- Use grep in Dev's shell; rg is unavailable. Read-only git and direct compiler/test probes
  are allowed. Never reset or overwrite unrelated changes.
- One design point per reply, one recommendation with accept-or-correct; build only accepted
  scope. After a verified commit, bring the next related point rather than only acknowledging it.
- Before handing over a gate, run **every discovered suite**, with current test resources
  on the loader roots as well as main/mods resources. Read all report-only headlines.
- New GitHub issues require explicit approval. None created during this session.
- Definitions move as Drake/JSON/generated-Scala trios. Every val in an App object must be lazy.
  The canonical corpus remains free of comparison exclusions.
- Activation and .dcon migration remain deferred. No new transforms, geodesy redesign,
  Haskell target, or wholesale example sweep is authorized. Existing issue prose is prior
  model reasoning, not Dev's specification. Persistence belongs to the consuming project;
  transport choices remain provisional.

## Current verification evidence

Dev's full sbt outputs for the four increments were pasted into the Claude Code session
(October 26, 26, 29 and 29): 936, 937, 938 then 942 tests, zero failed/canceled/ignored/pending,
every headline below matching, file-only pending mods actors 0. Each was preceded by a direct
run of **every discovered suite** (936, 937, 938 / 73 and 942 / 74 including Runner discovery;
sbt reports one fewer): all
main/mods/domain and test sources compiled into fresh class roots under target/scala-2.13
(`nc-classes`, `nc-test-classes`) with current main/mods/test resources overlaid and the hidden
root anchors removed; `git status` afterwards showed only the intended changes. The Output
increment's first direct run failed its lazy-capture test through the test's own strict helper
parameter; the second run was green.

| Headline | Verified at the Output tree (and at 112f277, 9779e39, e241378, a8b5447) |
|---|---|
| DomainAspect | 110 complete, 1 parameterized included, 0 skipped |
| DracoAspect | 110 complete, 0 frozen baselines, 0 skipped |
| GenDrake | 108/108, 0 missing, 0 extra |
| Example generation | 48 match, 0 differ, 0 error, 0 missing / 48 — ASSERTED since October 31 (39 / 9 before the ConsumeReport trios and nine companions, 37 / 11 before the coordinate companions, 36 / 12 before the Output repair) |
| Surface losses | 0 fields across 147 types (143 before the ConsumeReport surfaces, 141 before the coordinate surfaces, 137 before the Intent surfaces, 134 before the sibling surfaces, 133 before LocationReport's) |
| Parse scope | 110 draco + 37 mods, 0 held back (33 before the ConsumeReport surfaces, 31 before the coordinate surfaces, 27 before the Intent surfaces, 24 before the sibling surfaces, 23 before LocationReport's) |
| PON | 80 numbers, 550 expressions, 42 discrepancies |
| Canonical PON | 80 numbers, 7 differ |
| Scenario | 23 files, 0 rejected, 0 drifted, 23 clean |
| Forest | 1 of 2 ash alarms crossed to birch |
| Subtype visibility | CO-DECLARATION DROPS THE FACT |
| Pending mods actors | 0; file-only target/test-output/DrakeGenTest.log |

The example sweep's direct run (October 31, record 2026-10-31-1436): 983 / 78 including discovery, every
row above unchanged; Dev's full gate 983 / 77 on October 31, every row matching.

Do not infer the last row from console output. The example map is a gate since October 31
(ExampleDomainsGenTest asserts zero differ, error and missing); individual repaired types also
have exact projection and behavior gates.

## Completed session ledger

These are factual commit/record summaries, not reconstructed dialogue. All implementation
commits and pushes were performed by Dev after his full gate. Records live in draco-git-record.

| Commit | Accepted increment | Dev gate | Record suffix |
|---|---|---|---|
| b04d81d | Presence-typed rule/actor slots and field encoding; retire authored-ahead exhibits | 857 / 61 | 2026-10-22-0800 |
| 375c513 | Compile and execute all four empty-role projections | 861 / 61 | 2026-10-22-0739 |
| 1d69df1 | Reciprocal membership for 16 existing definitions across five example domains | 873 / 62 | 2026-10-23-1048 |
| ae0264e | HCursor target binding, caller AerialSink binding, all-48 loading gate | 875 / 63 | 2026-10-23-1106 |
| 3608fab | Aerial OriginateReport conversion carried in definition | 880 / 64 | 2026-10-23-1216 |
| bd6b7b3 | Marine OriginateReport conversion and truncation | 886 / 65 | 2026-10-23-1613 |
| 97bf690 | Terrestrial OriginateReport conversion and signed truncation | 893 / 66 | 2026-10-24-1008 |
| e819663 | Ethereal OriginateReport; fourth conversion-helper gap closed | 900 / 67 | 2026-10-25-1455 |
| 804c8a4 | Cartesian tuple overload and inferred coordinate codecs | 908 / 68 | 2026-10-25-1505 |
| 81154e3 | Observable helpers, nested codecs and caller Geodesy binding | 918 / 69 | 2026-10-25-1517 |
| 3648806 | Provider actor role and forwarding | 925 / 70 | 2026-10-25-1543 |
| a8b5447 | Consumer actor role and Position-to-Location branch | 933 / 71 | 2026-10-25-1557 |
| 7495539 | Documentation handoff: chapters 105–112, audit, handoff files | — | 2026-10-26-1253 |
| e241378 | Named construction spells its head as authored; NamedConstructionTest (Claude Code) | 936 / 72 | 2026-10-26-1449 |
| 9779e39 | Dyn result intercepted as a named construction; bare slot type resolves; Observable `geocentric:` (Claude Code) | 937 / 72 | 2026-10-26-1534 |
| 112f277 | Construction factory lookup keyed on the applied head; parent-typed slot case (Claude Code) | 938 / 72 | 2026-10-29-0755 |
| c5b8089 | Terrestrial Output's Location branch in its definition; OutputProjectionTest (Claude Code) | 942 / 73 | 2026-10-29-1640 |
| e138f04 | LocationReport json factory; both terrestrial sites construct through it; LocationReportTest (Claude Code) | 947 / 74 | 2026-10-30-1038 |
| 8540341 | PositionReport, FixReport, EphemerisReport json factories; OriginateReports construct through them; ReportFactoryTest (Claude Code) | 956 / 75 | 2026-10-30-1113 |
| 3a3cbfc | FlightIntent, VoyageIntent, LaunchIntent, MarchIntent json factories; eight test sites construct through them; IntentFactoryTest (Claude Code) | 967 / 76 | 2026-10-30-2014 |
| 23333f3 | Position and Location drake surfaces and generated companions with inferred codecs; CoordinateProjectionTest (Claude Code) | 974 / 77 | 2026-10-31-0842 |
| 7be1497 | Four ConsumeReport drake surfaces with JSON re-canonicalized; nine companions the generator's output; ExampleDomainsGenTest a gate (Claude Code) | 982 / 77 | 2026-10-31-1105 |
| pending | Twelve example trios in the compact form with `uses` records (surfaces re-emitted, JSON re-canonicalized, Scala regenerated); AssemblyValidator compares message types by identity; WorldAssemblyTest +1 (Claude Code) | 983 / 77 | 2026-10-31-1436 |

Starting HEAD was 4dcef69, the documentation handoff after 7b1bfea (849 / 60).
EXAMPLE_DOMAIN_AUDIT.md is a historical read-only audit at 375c513, not a current snapshot.

## Durable implementation decisions and findings

- Aspects.ruleAspect and actorAspect carry Presence. Their containing record omits Absent
  and emits Present directly as the existing aspect JSON object, including `{}`. No standalone
  Presence codec was introduced. BodyElement and ActorAspect are canonical plain types;
  former role exhibits moved into RolePresenceTest. No frozen fixtures/exclusions remain.
- RolePresenceTest compiles/loads all four combinations after carrier round-trip, fires
  declared rules and exercises actors through BehaviorTestKit. Empty action invocation count
  is not measured.
- All 48 example identities belong to their explicitly declared six domains. ExampleScope
  composes those domains with canonical dependencies and explicit bindings. HCursor belongs
  to HostBindings; AerialSink and Geodesy are caller-owned helpers, not domain members or
  shared target vocabulary. Loading is tested independently of declaration order.
- Four OriginateReport conversions now originate from their definitions, preserving payload
  keys, defaults, integer arithmetic and callbacks. They are repaired individually, not abstracted.
- Cartesian keeps lazy scalar arguments, strict tuple argument and Null. Observable keeps
  strict helper parameters and lazy frames; Geodesy mathematics is untouched. Their inferred
  codecs were explicitly accepted and tested for round-trip and missing-value defaults.
- Observable's first direct run passed 916/918: two loading failures exposed Geodesy as a
  newly visible dependency. Adding its explicit caller binding and omission/status checks
  led to a fully green rerun and first green Dev gate. This failure is preserved in its record.
- Provider forwards unchanged World messages once; Consumer converts only Position, ignores
  other messages, uses feet * 0.3048 and math.round(h).toInt, and captures its callback lazily.
  Both preserve inert signals and existing World assembly behavior. Consumer uses strict local
  bindings in its case branch and a positional Location call. No generator change was made then;
  the three named-construction repairs of October 26–27 are the generator changes since.
- Terrestrial Output converts only Location, as a `case` branch in its definition, into a
  LocationReport whose payload keys are type, latitude, longitude and elevationMetres; other
  World messages are ignored, the consumer is captured lazily, signals are inert. The report is
  constructed as host text because LocationReport has no factory.
- A named-argument construction's head is spelled as authored or as the scope in effect spells
  it, exactly as a positional call is. Only NAMED-argument applications are intercepted, and only
  when the applied HEAD — a name leaf, dotted, or bare and resolved against the definition in
  effect's package — loads a definition with a factory; the slot type plays no part, and
  everything else renders as before. A dyn's result (body `fix value` and leaf) is intercepted
  the same way, with the plain expression as its fallback.
- Scoped canonical generation and TypeName formal/actual separation remain as documented in
  DRACO.md and chapters 100–104. Rebuilds must preserve uses and typeParameters. Companions
  load unscoped; recorded resolution is applied during generation. The examples were swept on
  October 31 only after a probe showed scoped and unscoped generation byte-identical for all 48.
- `AssemblyValidator` compares a consumer parameter's message type with the actor's by IDENTITY
  (name and package, parameters free): each name is resolved through its own definition's record
  (`NameResolution.recorded`), a bare result completed with that definition's package — the compact
  form's convention — and nothing is loaded or discovered. Any further consumer of the type
  vocabulary must read names the same way; text comparison broke at the unit type and again at
  the example sweep.

The 11 remaining example differences after the Output repair are:
aerial/Aerial, aerial/Input, aerial/Position; ethereal/ConsumeReport, ethereal/Ethereal;
marine/ConsumeReport, marine/Marine; terrestrial/ConsumeReport, terrestrial/Location,
terrestrial/Terrestrial; world/World. These have not been individually read for more than
spelling; the audit noted companion/import conventions and inferred codecs on Position and
Location. Inventory is context, not repair authorization.

## Reproducing the direct verification procedure

Temporary scripts/logs are optional evidence, not prerequisites. The session used
/tmp/membership-build.py, /tmp/draco-role-build/tests.log and target/scala-2.13/role-{classes,test-classes}.
Do not run that old script's `bootstrap` mode: it contains obsolete, source-mutating migration
steps. Reconstruct only the compile/test procedure below if those files are missing or stale.

1. Read the jar entries from target/streams/test/fullClasspath/_global/streams/export
   (fallback: target/streams/runtime/dependencyClasspath/_global/streams/export). Verify they
   exist. If dependencies are absent, request Dev's build rather than running sbt yourself.
2. Use dedicated clean class directories under target/scala-2.13/, not /tmp. Existing tests
   derive repository resource locations from class locations; /tmp output caused wrong roots.
   Exclude old test/classes from the compiler classpath to avoid silently reusing stale sources.
3. Overlay src/main/resources, src/mods/resources **and src/test/resources** into the dedicated
   main class root. Exclude its root .json/.drake anchors as sbt does; MinimalTypeTest relies on
   that behavior. Keep resources unique across loader roots to avoid DefinitionPath ambiguity.
4. Compile all Scala under src/main/scala, src/mods/scala/draco and src/mods/scala/domains via
   `java -Xmx3g -cp <jars> scala.tools.nsc.Main -classpath <classes>:<jars> -d <classes> @<sources>`.
   Then compile all src/test/scala into the dedicated test directory using the same classpath.
5. Run `java -Xmx3g -cp <classes>:<jars> org.scalatest.tools.Runner -o -R <test-classes>`.
   Put test classes on Runner's runpath, not the JVM classpath; this matters for isolated runtime
   compilation/class loading. Capture stdout. Existing ZeroMQ tests need local sockets and may
   require sandbox permission. Run every discovered suite, not just a remembered subset.
6. Check all headlines above, the file-only mods actor line, and git status/diff afterwards.
   Some tests generate files into source trees; PrimesRulesTest must remain scoped. Never
   revert such output blindly, especially when preserving another agent's edits.

Direct verification supplements, never replaces, Dev's full sbt gate. Hand over the established
console grep plus file-only grep only after the direct run passes and headlines are understood.

## Journal coverage and handoff integrity

DRACO.md and README.md markers now reach **chapter 112**. Read chapters 105–112 and
`sources/codex-2026-10-22-to-26-audit.md` for this receiving session’s decisions and verification.
The extract preserves 85 original messages: 43 user (19 assertive, 24 result receipts),
42 final replies, 42 completed exchanges and the open journal-recovery acceptance. Original
records survive both observed compactions; no missing original was detected in this bounded
sequence. The current maintenance pass’s final reply is outside the frozen prefix.

The previous Claude handoff close is recovered in `sources/claude-2026-10-22-handoff-close.jsonl`
and rendered in chapter 105: two replies and Dev’s documentation commit/push receipt. The
prior 55-record extract was verified against source; chapters 100–104’s historical statuses
remain unchanged. Chapter 104 correctly leaves Presence open at its own historical boundary.

The initial October 26 handoff omitted journal maintenance. Dev challenged that omission,
and accepted recovering the original exchanges here. Chapter 112 preserves that correction.
No compaction summary was used as quotation evidence. Git records supplement explicitly
labeled Status notes, including Observable’s failed direct run and subsequent binding fix.

Checks: source-line/timestamp/hash verification, exchange and compaction-boundary checks,
chapter text/index/link checks, documentation reconciliation, shared symlinks and whitespace.
Only documentation and bounded dialogue extracts change; no runtime/tests, sbt, commits,
pushes, new issues or private-memory modifications by Codex. No new implementation gate claimed.
