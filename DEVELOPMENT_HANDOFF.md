# Development Handoff

Prepared October 26, 2026 by Codex for a new session in either Codex or Claude Code;
refreshed by Claude Code through November 3 after the named-construction repairs, the
terrestrial Output repair, the report and intent json factories, the coordinate, ConsumeReport and
sentient surfaces, the example sweep, the anchor-parameter rule, the compact fixpoint assertion, the
mods surface gate, the widened parse scope, the removed Output hold-out, the asserted surface-loss count and the
asserted scenario round-trip.
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
  example body sweep with the validator's identity comparison (record `git-record-2026-10-31-1436`), Dev-gated at 983 / 77 and committed and pushed by Dev as **ded1b5b** on October 31; then the
  sentient surfaces (record `git-record-2026-10-31-1938`), built and verified directly on October 31 (1005 / 78 after
  the accepted Distance correction), Dev-gated at 1005 / 77 and committed and pushed by Dev as **5f0e023** on October 31 —
  then the anchor-parameter rule (record `git-record-2026-11-01-0831`), built and verified directly on November 1
  (1006 / 78), Dev-gated at 1006 / 77 and committed and pushed by Dev as **ed57aa9** on November 1 — then the compact fixpoint
  assertion (record `git-record-2026-11-01-1228`), built on November 1, verified directly on November 1 (1007 / 78), Dev-gated at 1007 / 77 and committed and pushed by Dev as **a31e49b** on November 1 — then the mods surface gate with
  Yaml's surface (record `git-record-2026-11-02-0907`), built and verified directly on November 2 (1068 / 78), Dev-gated at 1068 / 77 and committed and
  pushed by Dev as **8746ecd** on November 2 — then the widened DrakeParseTest mods scope (record
  `git-record-2026-11-02-1202`), built and verified directly on November 2 (1118 / 78), Dev-gated at 1118 / 77 and committed and pushed by Dev as **a4ff2c4** on November 2 — then the removed Output hold-out
  (record `git-record-2026-11-02-1433`), built and verified directly on November 2 (1119 / 78), Dev-gated at 1119 / 77 and committed and pushed by Dev as **6a15754** on November 2 — then the asserted surface-loss count
  (record `git-record-2026-11-02-1451`), built and verified directly on November 2 (1119 / 78), Dev-gated at 1119 / 77 and committed and pushed by Dev as **b321df6** on November 2 — then the asserted scenario round-trip
  (record `git-record-2026-11-03-0942`), built and verified directly on November 3 (1119 / 78), Dev-gated at 1119 / 77 on November 3, in the tree with Dev's commit pending —
  inspect `git log -1` and `git status --short` to learn where the tree stands. Before them a8b5447 (Consumer) and 7495539
  (the documentation handoff).
- Latest Dev-verified gate: **1119 tests / 77 suites**, November 3, on the asserted-scenario-round-trip tree (uncommitted, record
  `git-record-2026-11-03-0942`): zero failed/canceled/ignored/pending tests, parse scope
  110 + 73, surface losses 0 across 183 types (asserted), scenario 23 clean (asserted), example gate 48 match / 0 differ, every other headline
  unchanged, pending mods definitions 0. Before it the same 1119 / 77 on the asserted-surface-loss tree (b321df6) and on the removed-hold-out tree (6a15754), 1118 / 77 on the widened-parse-scope tree (a4ff2c4), 1068 / 77 on the mods-surface-gate tree (8746ecd), 1007 / 77 on the compact-fixpoint tree (a31e49b), 1006 / 77 on the
  anchor-parameter tree (ed57aa9) and 1005 / 77 on the sentient-surfaces tree (5f0e023).
- All accepted implementation work through b321df6 is committed and pushed; the tree was clean at that commit. In the
  tree since, uncommitted, gated by Dev at 1119 / 77 on November 3 (no count moved) with the commit pending: the asserted scenario round-trip —
  `src/test/scala/scenario/ScenarioDrakeTest.scala` (the round-trip test fails naming each rejected or drifted file, renamed,
  class doc reworded and corrected), its record `git-record-2026-11-03-0942`, the CHANGELOG entry, the DRACO.md table row
  removed, these documents and the fold of the b321df6 hash.
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

## The example bodies are swept and the validator compares by identity (accepted and built, October 31; Dev verified 983 / 77, committed as ded1b5b)

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
its first run. Committed and pushed by Dev as ded1b5b.

## The sentient domain takes its drake surfaces (accepted and built, October 31; one correction accepted and built; direct run 1005 / 78, Dev verified 1005 / 77, committed as 5f0e023)

Dev accepted: "Accepted, build it" on the point raised after ded1b5b — give the eleven sentient definitions (Sentient,
Course, Direction, Distance, Effect, Ego, Gaze, Lean, Path, Percept, Waypoint, the one example domain with no drake
surface, JSON-only since 2026-06) the surfaces `DrakeCLI emit` renders, re-canonicalize the five whose derivation
argument is a tuple (Direction, Ego, Lean, Percept, Waypoint) by parsing the surface back, and replace the eleven
companions with the generator's output. The five alone in the corpus failed `parse(emit(td)) == td`: the tuple argument
to the parent, `Primal((Gaze, Distance))`, sat in the derivation's type parameter as the pre-tree STRING `"(Gaze,
Distance)"`, the form before type-form trees of 2026-09-17, and the domain had no surface for any sweep to parse.
MEASURED on a scratch copy with the compiled CLIs before recommending: eleven surfaces of one or two lines (`type
Waypoint from Sentient draco Primal((Gaze, Distance))` and the domain line; Sentient's lists its ten members); parsing
back reproduces six JSON byte for byte and moves exactly one leaf in each of the five, to the Objective `{"(,)": ["Gaze",
"Distance"]}`; the Scala from either JSON is whitespace-identical to the committed companions (a leading blank line, one
more in Distance and Sentient, a trailing space on Sentient's trait line — the example gate normalizes whitespace);
`DrakeCLI check` clean; the compact form changes none of the eleven. Built as probed: the eleven `.drake` files are the
emitter's output copied in unchanged, the JSON `DrakeCLI parse` of them (six identical, five moved on the one leaf),
the Scala `GeneratorCLI generate` over the current resources; `DrakeCLI check src/mods/resources/domains/sentient`:
surface drift 0, json drift 0, skipped 0. No engine change, no new test: the round trip is what the 22 newly active
DrakeParseTest checks assert. Headline moves: parse scope 110 + 48, surface losses 0 across 158 types; the example gate
stays 48 / 0. Every definition under `src/mods/resources/domains` is now a trio. THE DIRECT RUN FOUND A DEFECT THE PROBE MISSED:
1004 of 1005 passed, ExampleLoadingTest's all-48 resolution failing with `Ambiguous type 'Distance':
domains.sentient.Distance, draco.base.Distance` — the old string leaf was host-opaque text the resolver never read,
the tree makes `Distance` a name, and a bare name resolves by unique ontology lookup with no own-package precedence
(Dev, 2026-10-20). The probe ran the emitter, parser, check and generator but not the two-stage loader, the one
consumer that resolves type-form leaves. Measured after: the whole ontology (182 definitions as authored) holds exactly
two ambiguous bare anchor parameters, Percept's and Waypoint's; spelled `domains.sentient.Distance` in full (the
sweep's convention for a non-unique simple name, as Decoder and Encoder are spelled in the Dracon domain) the resolver
reads both, the surface round-trips byte for byte, `DrakeCLI check` is clean, and the two companions regenerate with one
trait line changed, `Primal[(Gaze, domains.sentient.Distance)]`. Separately, the COMPACT form of either spelling fails:
`NameResolution.rewrite`'s `anchorParameter` renders an own-package anchor parameter bare unconditionally where `short`
checks uniqueness, so `resolve` writes a bare `Distance` that a second read cannot resolve — an engine inconsistency on
a collision the corpus never presented before, not met by any gate, the next related point. Dev ACCEPTED the correction
("Accepted, build it"): Percept's and Waypoint's headers read `Primal((Gaze, domains.sentient.Distance))`, their JSON is
the parse of the new surface (byte-equal to the probe's), their companions regenerate on the one trait line, no engine
change; `DrakeCLI check` over the domain: surface drift 0, json drift 0. SECOND DIRECT RUN: 1005 tests / 78 suites
including discovery, zero failed, canceled, ignored, pending or aborted, every headline as stated, pending mods actors 0.
The record (git-record-2026-10-31-1938) carries the measurements and the correction. OBSERVED, not recommended: `domains/sentient/EgoActor.scala` is a hand-written actor over
Ego with no definition. Deferred, neither accepted: activation (Codex, 2026-10-15) and the .dcon migration.

## An anchor parameter's bare form takes the body leaf's uniqueness rule (accepted and built, November 1; direct run 1006 / 78, Dev verified 1006 / 77, committed as ed57aa9)

Dev accepted: "Accepted, build it" on the point raised after 5f0e023 — make `anchorParameter` in `NameResolution.rewrite`
apply the same judgment as `short`: a local binding keeps its authored spelling, an own-package name is written bare
only when `short` would shorten it (simple name unique across the ontology and the host vocabulary), everything else
stays in full. The resolver had written an own-package anchor parameter bare unconditionally, so `resolve` turned
Percept's and Waypoint's `domains.sentient.Distance` (the 5f0e023 correction) back into the bare name the loader had
refused, and the compact form of either (resolve, then re-read) failed; no gate compacts the example definitions and
the example gate generates unscoped. MEASURED over all 183 definitions before recommending: 33 bare anchor parameters,
thirteen own-package (DomainType, TypeDictionary, TypeName, TypeDefinition, XML, Dracon ×2, Gaze ×2, Percept, Effect,
World ×2) every one unique and no host binding's name, six already in full, twenty formals or host leaves — so no
committed JSON or Scala changes; the only observable change is that Percept's and Waypoint's compact form equals the
committed JSON and reads back. BUILT: one conjunct on `anchorParameter` (`short(full, local) != full`);
ResolutionRecordTest +1 (five cases) over the example caller scope — the two tuples
`{"(,)": ["Gaze", "domains.sentient.Distance"]}`, resolve to self, compact to the committed JSON, read back, unscoped
equals scoped generation equals the committed companion; Ego's `Holon(Percept, Effect)` and TypeDictionary's
`Dictionary(TypeName, TypeDefinition)` still bare. REJECTED: dropping the own-package restriction so an anchor
parameter could take the record's short form. DIRECT RUN: 1006 tests / 78 suites including discovery, zero failed,
canceled, ignored, pending or aborted, green on its first run, every headline unchanged, pending mods actors 0. The
record (git-record-2026-11-01-0831) carries the measurement. OBSERVED, not recommended: `domains/sentient/EgoActor.scala`
is a hand-written actor over Ego with no definition. Deferred, neither accepted: activation and the .dcon migration.

## The whole corpus is asserted a fixpoint of the compact form (accepted and built, November 1; Dev verified 1007 / 77, committed as a31e49b)

Dev accepted: "Accepted, build it" on the point raised after ed57aa9 — one new ResolutionRecordTest case asserting over
every definition under `src/main/resources` and `src/mods/resources` that its compact form is the definition itself and
reads back to the same resolution, the canonical scope (`CanonicalScope.scope`) for draco and the example caller's scope
(a `NameResolution` over `ExampleScope.declarations` and `ExampleScope.bindings`) for domains. WHY: the record of ed57aa9
named the gap its defect had passed through — nothing compacts the example definitions, the two-stage loader never
re-reads its result — and the suite kept that shape after it; the sweeps' claim that every definition is in the compact
form was measured in their records and asserted nowhere as a whole. MEASURED before recommending, on the compiled classes
of the ed57aa9 direct run: 183 definitions (the nameless domain's `.json` anchor excluded), 183 whose compact form encodes
to the definition, 183 whose compact form reads back; on the tree of 5f0e023 the same walk fails on Percept and Waypoint.
BUILT: the sixth ResolutionRecordTest case — the walk over the two resource roots, the scope chosen by package, the two
assertions per definition naming the failing stem, the count bounded below at 183 (as the suite's whole-corpus generation
case bounds the main corpus at 110: a silent loss fails, an added type does not). No engine line, no definition, no
committed Scala, no new suite; no headline moves. DIRECT RUN: 1007 tests / 78 suites including discovery,
zero failed, canceled, ignored, pending or aborted, green on its first run, every headline unchanged (parse scope 110 + 48,
surface losses 0 across 158 types, example gate 48 / 0, GenDrake 108 of 108, aspects 110, PON 80 / 550 / 42 and 7, scenario
23 clean, forest 1 of 2, CO-DECLARATION categorical), pending mods actors 0. The record (git-record-2026-11-01-1228) carries the
measurement; the commit also carries the ed57aa9 fold in these documents. OBSERVED, not recommended:
`domains/sentient/EgoActor.scala`, a hand-written actor over Ego with no definition — referenced nowhere but a test
comment and a review-ledger row, a June stub modeled on NaturalActor, no test, no assembly, the retired Actor suffix in
its name; a role-named definition or removal is Dev's decision. Deferred, neither accepted: activation and the .dcon
migration.

## Every mods drake surface is a gate, and Yaml takes its surface (accepted and built, November 2; direct run 1068 / 78, Dev's gate 1068 / 77, committed as 8746ecd)

Dev accepted: "Accepted, build it" on the point raised after a31e49b. WHY: the fixpoint walk's 183 definitions hold exactly
one with no drake surface, the staged `draco.format.yaml.Yaml` (kept when YAML authoring was retired, chapter 56; referenced
nowhere outside its trio), and it was invisible to every gate because DrakeGenTest's mods section — since the actor-emission
increment of 2026-09-10 — scanned only `domains` and pinned `Drake.emit` against the authored surface for the 12 definitions
there whose `actorAspect` is Present (the dreams Editor, under `draco/dreams`, sat outside the scan), its headline counting only actors; its comment called the plain and rule mods types "a later
pass". So the emit direction of 60 mods surfaces was pinned by nothing but the hand-run `DrakeCLI check` (DrakeParseTest pins
`emit(parse(source))` for the example tree; DreamsTest, InitializationTest and ServiceTest pin their own surfaces by name).
MEASURED before recommending, on the a31e49b classes, over all 73 mods definitions: 72 with a surface, 72 of 72 emitting it
byte for byte, 72 of 72 parsing back to the committed JSON, Yaml alone without; Yaml's emitted surface is two lines, `type
Yaml from draco format Format(String)` and `domain draco format yaml Yaml` (XML's form), parses back to the committed JSON
byte for byte, and `Yaml.scala` regenerates byte-identical. BUILT: the mods section walks the whole of `src/mods/resources`,
drops the actor filter, keeps the exact-match test — 73 per-type gates today — and the report-only test is "mods definitions
still lacking a .drake", logging `mods definitions pending .drake authoring (N)`, the phrase kept so the gate grep still reads
it; `Yaml.drake` is `DrakeCLI emit --write` over its directory, unedited; `DrakeCLI check src/mods/resources` surface drift 0,
json drift 0. The DRACO.md table row is relabeled "mods definitions pending `.drake`", value 0. No engine, JSON or Scala
change; the parse scope stays 110 + 48, DrakeParseTest's mods scope being the example tree — widening it is a point of its
own, not recommended here. Flagged, not recommended: removing Yaml instead, Dev's decision; the surface preserves the
definition either way. FOCUSED: DrakeGenTest 184 tests (110 main, 73 mods, the report; 123 before — the recommendation had counted 13 actors and expected 1066), green on its first run
— the probe harness's first attempt aborted before any test with its class directory first on the classpath, where
`Main.roots` reads the source root; recompiled into the test class directory it ran, nothing in the tree changed. DIRECT RUN:
1068 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run, every
headline unchanged, pending mods definitions 0. Dev's full gate 1068 / 77 on November 2, green on its first run, every
headline matching, pending mods definitions 0 (grep count 1); committed and pushed by Dev as 8746ecd.

## DrakeParseTest's mods scope widens to the whole mods tree (accepted and built, November 2; Dev verified 1118 / 77, committed as a4ff2c4)

Dev accepted: "Accepted, build it" on the point raised after 8746ecd. WHY: after that commit the emit direction of every
mods surface is a gate, but the parser's two round-trip gates — `emit(parse(source))` reproduces the surface,
`parse(emit(td))` reproduces the definition — and the surface-loss measurement still walked `domains` alone, as since the
actor-emission increment of 2026-09-10; the 25 staged definitions under `src/mods/resources/draco` (the dreams Editor,
Initialization, the User requests, the service types, Yaml) sat outside all three. DreamsTest, EditorTest, InitializationTest
and ServiceTest pin 24 of the 25 by hand-listed name, in the emit direction only; ServiceTest compares the parsed JSON with the
committed JSON for two Service members; Yaml was pinned by nothing in the parse direction, and a hand list admits a staged
definition silently, as it admitted Yaml. MEASURED before recommending, on the 8746ecd classes, with a scratch copy of the
suite walking the mods root: 380 tests, zero failed — 73 surface gates (48 before), 72 definition gates (terrestrial Output
still held out for its inline tuple argument), the reports and structural cases; `drake surface losses: 0 fields across 183
types` (158 before), the whole corpus and the fixpoint assertion's count; `Drake.parse scope: 110 draco + 73 mods types in`
(110 + 48 before). BUILT: `modsPaths` walks `modsResourceRoot` itself instead of its `domains` child, the two filters and the
hold-out unchanged; the class doc, the mods section comment and the loss test's comment say "mods definitions" where they
said "mods actors", the historical note on why the actors were once outside kept; the by-name pins stay. No engine,
definition or Scala change, no new suite. The DRACO.md table rows move in this commit: losses 158 → 183 types, scope 110 + 48 →
110 + 73. Flagged, not recommended: keeping the by-name pins as the coverage — Yaml would need hand-listing and 23 staged
definitions would still lack the parse direction. FOCUSED: DrakeParseTest 380 (330 before), green on its first run, both
headlines as measured. DIRECT RUN: 1118 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or
aborted, green on its first run, every other headline unchanged, pending mods definitions 0. DEV'S GATE: 1118 / 77 on
November 2, zero failed, canceled, ignored or pending, every headline matching, pending mods definitions 0, green on its first
run. Committed and pushed by Dev as a4ff2c4.

## The stale Output hold-out leaves DrakeParseTest's definition gate (accepted and built, November 2; Dev verified 1119 / 77, committed as 6a15754)

Dev accepted: "Accepted, build it" on the point raised after a4ff2c4. WHY: `inlineTupleArgument` held terrestrial Output out
of `parse(emit(td))` since 0acf2da (2026-08-12), the one definition of 73 outside the definition gate and the only entry the
set ever held. Its comment was true then — an inline tuple argument carrying an application was one token to the lexer, so
the application came back as host-opaque text — and false since two dated changes: tuple and collection literals treed on
2026-09-20 (glued tokens split at depth-zero parentheses and commas since 2026-09-16), and Output.json re-canonicalized from
its own surface at c5b8089 on 2026-10-29, so the committed JSON carries the payload as a full tree and is the parser's
reading by construction. MEASURED before recommending, on the a4ff2c4 classes: `DrakeCLI parse Output.drake` equals the
committed JSON byte for byte; `DrakeCLI check` over the terrestrial directory is clean; a scratch copy of the suite with the
set emptied, compiled into the overlay, passes 381 with Output's definition case green and both headlines unchanged. BUILT:
the val and its comment, the `filterNot` on the definition walk and the file-only "mods held out of the JSON gate" log line
are removed; the report test keeps its other lines and the console headline. No engine, definition or Scala change, no new
suite, no headline moves. Flagged, not recommended: keeping the set as an empty mechanism against a future hold-out — dead
code guarding a case the parser no longer has. FOCUSED: DrakeParseTest 381 (380 before), green on its first run. DIRECT RUN:
1119 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run, every
headline unchanged, pending mods definitions 0. DEV'S GATE: 1119 / 77 on November 2, zero failed, canceled, ignored or
pending, every headline matching, pending mods definitions 0, green on its first run. Committed and pushed by Dev as 6a15754.

## The surface-loss count is asserted at zero and leaves the table (accepted and built, November 2; direct run 1119 / 78, Dev's gate 1119 / 77)

Dev accepted: "Accepted, build it" on the point raised after 6a15754. WHY: DrakeParseTest's "measured tail" — the
unnormalized comparison of every definition's raw encoder leaves with those of `parse(emit(td))`, report-only since the
parser's first increments — had read zero since b04d81d (2026-10-22, the role-presence re-canonicalization of BodyElement and
ActorAspect; one field from 2026-09-22, fifteen or sixteen before), covered the whole corpus (183 types) since a4ff2c4, and is
stricter than gate 2, whose `surfaceCarried` normalization lets a string on one side and a tree on the other pass. Zero means
every definition's JSON is exactly the parser's reading of its own surface; a measurement with nothing left to measure, whose
regression a green suite would hide. BUILT: the test is renamed "drake surface losses, unnormalized: none" and fails after its
console line when the loss list is nonempty, listing each field; section comment and class doc reworded (tail closed
2026-10-22, asserted since 2026-11-02; a loss means re-canonicalize from the drake). DRACO.md: the row leaves the §1 table, the
assertions paragraph names the fourth, the "read the report-only numbers" rule drops surface losses. The console headline is
unchanged and stays in the gate grep. No test count moves, no engine, definition or Scala change. NEGATIVE PROBE after building,
on a scratch project tree with copied resources and overlays (the suite reads sources from the project root the first classpath
entry sits under, not from the classpath — a probe class directory alone aborts the Runner): one empty-collection `value` of
draco/Action.json set to the legacy "Seq()"; gate 2 still passes Action, the asserted test fails naming
`.dracoAspect.factory.parameters[0].value: "Seq()" -> {"[]":[]}`, 380 / 1, headline `1 fields across 183 types
[empty-collection spelling: 1]`; the real tree untouched. Flagged, not recommended: the same move for ScenarioDrakeTest's row.
FOCUSED: DrakeParseTest 381, green on its first run. DIRECT RUN: 1119 tests / 78 suites including discovery, zero failed,
canceled, ignored, pending or aborted, green on its first run, every suite's count unchanged, every headline unchanged, pending
mods definitions 0. DEV'S GATE: 1119 tests / 77 suites on November 2, zero failed, canceled, ignored or pending, every headline
matching, pending mods definitions 0, green on its first run. Committed and pushed by Dev as b321df6.

## The scenario round-trip is asserted and leaves the table (accepted and built, November 3; direct run 1119 / 78, Dev's gate 1119 / 77)

Dev accepted: "Accepted, build it" on the point raised after b321df6. WHY: ScenarioDrakeTest (since 0af56fd, 2026-08-27)
walked the 23 drake surfaces under src/test/resources/scenario, checked that emitting what the parser read reproduces the
source, whitespace-normalized, logged each file's parsed shape and ended in `succeed`; its class doc called it a worklist for
the grammar. The worklist was empty at its first reading (22 clean on 2026-08-27; 23 since the scenario became a corpus at
87a2bb9 on 2026-08-31) and never held a rejected or drifted file through every re-canonicalization that touched the scenario.
ScenarioGenTest's first gate asserts the JSON direction (each committed .json is the parse of its .drake) and its other four
the Scala, the accepted rules, the transform shape and the run; neither DrakeGenTest nor DrakeParseTest walks the scenario, so
the surface direction was the one unasserted leg of that corpus. The class doc was also stale: RootInterface's dispatch has
been two `case` branches since 12bed84, not a host-opaque Scala `match`. BUILT: the test is renamed "the scenario in today's
drake: every file parses and round-trips" and fails after its console line naming each rejected file with its error and each
drifted file; the class doc's report-only paragraph becomes "A gate since 2026-11-03; a worklist before", with the
RootInterface sentence corrected. DRACO.md: the row leaves the §1 table (which now holds the parse-scope row, the PON pair, the
file-only DrakeGenTest row and the categorical rete row), the assertions paragraph names the fifth, the "read the report-only numbers"
rule keeps PON as its one live example. The console headline is unchanged and stays in the gate grep. No test count moves,
no engine, definition or Scala change. REJECTED: folding the scenario into DrakeParseTest's walk. NEGATIVE PROBE after
building, on the scratch project tree with copied scenario resources and overlays, run with that tree as the working
directory (this suite reads `src/test/resources/scenario` relative to the working directory, not from the classpath root the
corpus suites derive): RootInterface given a redundant parenthesized pair the parser dissolves, birch Compound a stray section
line; the asserted test fails naming both, `REJECTED birch/Compound.drake: Drake.parse: unexpected section 'whatever' in
Compound` and `DRIFTED ash/RootInterface.drake`, headline 23 files, 1 rejected, 1 drifted, 21 clean; the real tree untouched.
FOCUSED: ScenarioDrakeTest 1, green on its first run. DIRECT RUN: 1119 tests / 78 suites including discovery, zero failed,
canceled, ignored, pending or aborted, green on its first run, every suite's count unchanged, every headline unchanged, pending
mods definitions 0. DEV'S GATE: 1119 tests / 77 suites on November 3, zero failed, canceled, ignored or pending, every headline
matching, pending mods definitions 0, green on its first run. Commit pending.

## Next design point — acceptance required

The asserted scenario round-trip is in the tree, gated by Dev at 1119 / 77 with the commit pending; the asserted surface-loss count
before it is committed and pushed as b321df6 at Dev's 1119 / 77 gate, the removed Output hold-out as 6a15754 at Dev's 1119 / 77 gate, the widened parse scope as a4ff2c4 at Dev's 1118 / 77 gate, the mods
surface gate as 8746ecd at Dev's 1068 / 77 gate. Nothing from the sentient, anchor-parameter, compact-fixpoint,
mods-surface or parse-scope threads remains preserved. In view,
measured and not recommended: `EgoActor.scala`, the one definition-less actor left in the example tree (unreferenced, a
June stub, the retired Actor suffix in its name) — define under a role name or remove, Dev's decision. Deferred activation and the .dcon migration remain deferred and unaccepted. Inventory
is context, not authorization.

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
| Surface losses (ASSERTED zero since 2026-11-02) | 0 fields across 183 types (158 before the widened mods scope, 147 before the sentient surfaces, 143 before the ConsumeReport surfaces, 141 before the coordinate surfaces, 137 before the Intent surfaces, 134 before the sibling surfaces, 133 before LocationReport's) |
| Parse scope | 110 draco + 73 mods, 0 held back (48 before the widened mods scope, 37 before the sentient surfaces, 33 before the ConsumeReport surfaces, 31 before the coordinate surfaces, 27 before the Intent surfaces, 24 before the sibling surfaces, 23 before LocationReport's) |
| PON | 80 numbers, 550 expressions, 42 discrepancies |
| Canonical PON | 80 numbers, 7 differ |
| Scenario (ASSERTED since 2026-11-03) | 23 files, 0 rejected, 0 drifted, 23 clean |
| Forest | 1 of 2 ash alarms crossed to birch |
| Subtype visibility | CO-DECLARATION DROPS THE FACT |
| Pending mods definitions (actors only before November 2) | 0; file-only target/test-output/DrakeGenTest.log |

The example sweep's direct run (October 31, record 2026-10-31-1436): 983 / 78 including discovery, every
row above unchanged; Dev's full gate 983 / 77 on October 31, every row matching. The sentient surfaces' direct run
(October 31, record 2026-10-31-1938): first run 1005 tests / 78 including discovery, 1004 passed, ONE FAILED —
ExampleLoadingTest's all-48 resolution, `Ambiguous type 'Distance': domains.sentient.Distance, draco.base.Distance` from
Percept's and Waypoint's now-treed derivation argument; second run, after the accepted correction spelling that name in
full, 1005 / 78, zero failed or aborted, every row above as stated, pending mods actors 0. Dev's full gate 1005 / 77 on
October 31, green on its first run, every row matching, pending mods actors 0; committed and pushed as 5f0e023. The
anchor-parameter rule's direct run (November 1, record 2026-11-01-0831): 1006 / 78 including discovery, zero failed or
aborted, every row above unchanged, pending mods actors 0. Dev's full gate 1006 / 77 on November 1, green on its first
run, every row matching, pending mods actors 0; committed and pushed as ed57aa9. The compact fixpoint assertion's direct
run (November 1, record 2026-11-01-1228): 1007 / 78 including discovery, zero failed or aborted, green on its first run, every row
above unchanged, pending mods actors 0. Dev's full gate 1007 / 77 on November 1, green on its first run, every row
matching, pending mods actors 0; committed and pushed as a31e49b. The mods surface gate's direct run (November 2, record
2026-11-02-0907): 1068 / 78 including discovery, zero failed or aborted, green on its first run, every row above unchanged
but the relabeled file-only row, pending mods definitions 0. Dev's full gate 1068 / 77 on November 2, green on its first
run, every row matching, pending mods definitions 0; committed and pushed as 8746ecd. The widened parse scope's direct run
(November 2, record 2026-11-02-1202): 1118 / 78 including discovery, zero failed or aborted, green on its first run, the
surface-loss and parse-scope rows moved as stated above, every other row unchanged, pending mods definitions 0. Dev's full
gate 1118 / 77 on November 2, green on its first run, every row matching, pending mods definitions 0; committed and pushed as a4ff2c4. The removed Output hold-out's direct run
(November 2, record 2026-11-02-1433): 1119 / 78 including discovery, zero failed or aborted, green on its first run, every row
unchanged, pending mods definitions 0. Dev's full gate 1119 / 77 on November 2, green on its first run, every row matching,
pending mods definitions 0; committed and pushed as 6a15754. The asserted surface-loss count's direct run (November 2, record
2026-11-02-1451): 1119 / 78 including discovery, zero failed or aborted, green on its first run, every row unchanged, the
surface-loss row now an assertion, pending mods definitions 0; Dev's full gate 1119 / 77 on November 2, every headline matching,
pending mods definitions 0; committed and pushed as b321df6. The asserted scenario round-trip's direct run (November 3, record
2026-11-03-0942): 1119 / 78 including discovery, zero failed or aborted, green on its first run, every row unchanged, the scenario
row now an assertion, pending mods definitions 0; Dev's full gate 1119 / 77 on November 3, every headline matching, pending mods
definitions 0, commit pending.

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
| ded1b5b | Twelve example trios in the compact form with `uses` records (surfaces re-emitted, JSON re-canonicalized, Scala regenerated); AssemblyValidator compares message types by identity; WorldAssemblyTest +1 (Claude Code) | 983 / 77 | 2026-10-31-1436 |
| 5f0e023 | Eleven sentient drake surfaces, five tuple derivations re-canonicalized, Percept and Waypoint spelling Distance in full, eleven companions the generator's output (Claude Code) | direct run 1005 / 78 (first run 1004, corrected); Dev's gate 1005 / 77 on October 31 | 2026-10-31-1938 |
| ed57aa9 | The anchor-parameter bare form takes `short`'s uniqueness rule; no definition or committed Scala change; ResolutionRecordTest +1 (Claude Code) | direct run 1006 / 78; Dev's gate 1006 / 77 on November 1 | 2026-11-01-0831 |
| a31e49b | The whole corpus asserted a fixpoint of the compact form over both resource roots; ResolutionRecordTest +1, no engine or corpus change (Claude Code) | direct run 1007 / 78; Dev's gate 1007 / 77 on November 1 | 2026-11-01-1228 |
| 8746ecd | DrakeGenTest's mods gate widened to every mods definition and its headline relabeled; `Yaml.drake` the emitter's two lines; no engine, JSON or Scala change (Claude Code) | direct run 1068 / 78; Dev's gate 1068 / 77 on November 2 | 2026-11-02-0907 |
| a4ff2c4 | DrakeParseTest's mods walk widened to the whole mods tree, three comments reworded; two table rows moved; no engine, definition or Scala change (Claude Code) | direct run 1118 / 78; Dev gate 1118 / 77 on November 2 | 2026-11-02-1202 |
| 6a15754 | DrakeParseTest's `inlineTupleArgument` hold-out removed, Output under the definition gate; no engine, definition or Scala change (Claude Code) | direct run 1119 / 78; Dev gate 1119 / 77 on November 2 | 2026-11-02-1433 |
| b321df6 | DrakeParseTest's surface-loss measurement asserts zero and its DRACO.md table row leaves; negative probe 380 / 1 on a scratch tree; no engine, definition or Scala change (Claude Code) | direct run 1119 / 78; Dev gate 1119 / 77 on November 2 | 2026-11-02-1451 |
| (in the tree) | ScenarioDrakeTest's round-trip asserts, naming each rejected or drifted scenario surface, its class doc corrected and its DRACO.md table row gone; negative probe 1 rejected / 1 drifted on a scratch tree; no engine, definition or Scala change (Claude Code) | direct run 1119 / 78; Dev gate 1119 / 77 on November 3, commit pending | 2026-11-03-0942 |

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
   ScenarioDrakeTest and ScenarioGenTest instead read `src/test/resources/scenario` relative to the
   working directory, so a probe of them against altered resources runs from a scratch project tree.
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
