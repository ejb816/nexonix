# DRACO review ledger

The findings that carry between reviews, each with a stable ID and a status. A review reports
*transitions* against this file and then updates it; the review itself stays a snapshot.

**Seeded 2026-09-16** from `review-2026-08-15.md` (at `db87dbd`) and `review-2026-08-28.md` (at
`0af56fd`). Status column is **as of `0af56fd`** unless a later commit is named. Nothing here has
been re-verified against a commit later than `0af56fd`; the next review does that.

Conventions: IDs are never reused. A withdrawn finding keeps its row with status `withdrawn` and a
reason. `Where` gives the location at last verification — line numbers move, so the next review
re-finds the code and updates the cell. Status vocabulary: `open`, `open (worse)`, `half`, `fixed
<commit>`, `closed` (design finding no longer applies), `withdrawn`, `deliberate` (open by Dev's
decision — not debt).

The IDs of the recommendations (R-nn) are what the next review scores for uptake.

---

## D — Concrete defects

| ID | First seen | Where (at `0af56fd`) | Finding | Status | Note |
|---|---|---|---|---|---|
| D-01 | 08-15 | `src/main/resources/draco/Draco.json:73-125` | `elementTypeNames` omits `Local`; rules interleaved alphabetically where `GeneratorCLI.elementCategory:183-186` sorts rules after types → `draco-gen verify Draco.json` exits non-zero | open | 51 listed, 52 siblings; first order divergence at index 10 |
| D-02 | 08-15 | `GeneratorCLI.scala:218-227` | `runDiscover` rebuilds `TypeDefinition` without `_codecAspect` | open | |
| D-03 | 08-15 | `ActorAspect.scala:51` vs `TypeDefinition.scala:21-22`; `RuleAspect.scala:18` vs `:41` | `isEmpty` ignores `messageType`; encoder elides the aspect on `isEmpty`. `RuleAspect` encoder gates `pattern` on `variables.nonEmpty` while `isEmpty` also counts `conditions` | open (worse) | Load-bearing since `05b14bd` made `messageType` the sole carrier — see D-19 |
| D-04 | 08-15 | `CLI.scala:17-19` vs `build.sbt:2` | `version` prints `2.0.0-alpha.1`; `CLI.load` degrades to `TypeDefinition.Null` → NPE at `Generator.scala:55` | open (worse) | Build is alpha.6; gap grew by a release |
| D-05 | 08-15 | `Main.scala:20` / `Main.json:49` | `sinkRoot` resolves via `classOf[Test]` | open | |
| D-06 | 08-15 | `SourceContract.scala:51` | `ExpressionRenderer` lacks `"\|\|"`, used by `SelfDeclaration.json:22` | open | `Drake.expression:100` and `Generator.expression:100` have it |
| D-07 | 08-15 | `AssemblyValidator.scala:34-42`; `AssemblySpawner.scala:62-67` | Validator only recognised an `Actor[M]` derivation; `spawnOrder` has no cycle guard | half | Validator fixed `05b14bd` (aspect first, derivation fallback). Spawner still marks `ordered` after recursing → infinite recursion on a cycle |
| D-08 | 08-15 | `Generator.scala:895,898,899`; `Generated.scala:34,35,39` | `override val` (not `lazy`) in emitted Null instances and a hand-built companion; `SourceContent.Null`/`ContentSink.Null` NPE when forced | open | `checks/` also finds six hand-written mods companions with `override val` (`domains/*/OriginateReport.scala`, `sentient/EgoActor.scala`, `terrestrial/Output.scala`) — not yet assessed |
| D-09 | 08-15 | `base/Unit.scala:5` | `trait Unit` shadows `scala.Unit` inside `draco.base` | open | |
| D-10 | 08-15 | `TypeLoader.scala:14` | `readDefinition` uses `.toOption`: malformed JSON indistinguishable from missing → stub → `Completeness` reports a present file as unauthored | open | `:17` rewritten by `324556c`; `:14` untouched |
| D-11 | 08-15 | `drake.dlt:2, 70-86, 191-193, 325, 355-357` | Five stale items: "until the parser exists"; `\|\|` `(,)` `=` undeclared; `_`-prefix paragraph contradicted by `:389`; "members keep their aspect suffix"; `codec` template with encoder/decoder selectors `CodecAspect` lacks | open | File edited twice since (+47 lines) without touching any of the five. Two docs now vouch for it as "current and authoritative" (X-05) |
| D-12 | 08-15 | `DrakeParseTest.scala:139-146`, `DrakeCLI.scala:123-126` | `surfaceCarried`/`check` stripped `namePackage` from all references, so gate 2 did not verify packages | **fixed `324556c`** | Gate 2 now forbids the loss it used to measure |
| D-13 | 08-15 | `PrimesRulesTest:47-74`, `TupleFactReteTest:20-22`, `AerialGenTest:29-39`, `PonCorpusTest:165-166` | Tests write into tracked source/resources; `DracoGenTest` pass can depend on run order | open | All four paths confirmed tracked via `git ls-files`. `GeneratorDefinitionToSourceTest` (→ gitignored `src/generated/`) and `GenerateAndCompileTest` (→ `/tmp`) are fine |
| D-14 | 08-15 | `TypeDefinitionTest:60-81`; `GeneratorDefinitionToSourceTest` (20 tests, 0 asserts); `PrimesRulesTest:10-37`; `ExampleDomainsGenTest:120` | Log-only or `total > 0`; Primes firing test declares the rule inline rather than firing the generated one | open (worse) | +2 report-only suites at HEAD (D-23). Overlaps GitHub #62 |
| D-15 | 08-15 | `src/main/scala/org/nexonix/**` (9), `src/test/scala/org/nexonix/**` (9), `src/mods/scala/draco/format/yaml/`, `viz/` (9), `PrimeOrdinal.txt` | Dead/orphaned | open | `PrimeOrdinal` is on DRACO.md's own Retired list, which makes the root file stale by the repo's rule |
| D-16 | 08-15 | `build.sbt:15,21,24,33,34` | Unused: `scala-swing`, `jline`, `pekko-actor-testkit-typed`; `circe-generic` only via orphaned `JsonMain`. Pekko on milestone `1.2.0-M1`; circe `0.14.1` | open | 08-28 correction: `circe-optics` *is* imported, by the orphaned `TestCirceJson.scala:4`. build.sbt comment "jline (used at runtime by the REPL)" is false |
| D-17 | 08-15 | `.github/workflows/release.yml:3-6` | Only workflow, tag-only, runs the checkout-mutating suite | open | `5b48f0c` bumped action majors and added tag/notes checks, but the only tag (`v2.0.0-alpha.6` → `70ab3f2`) predates it: the repaired workflow has never executed |
| D-18 | 08-15 | `.gitignore:12` | `CLAUDE.md` ignored but tracked | open | Now a tracked, ignored symlink to the file every session reads |
| D-19 | 08-28 | `ActorAspect.scala:51`; `TypeDefinition.scala:21`; `Generator.scala:1119-1120`; `DrakeGenTest.scala:125`; `Drake.scala:543-546` | The `messageType` migration is not backed by `isEmpty`: an aspect carrying only `messageType` never persists, is not an actor to dispatch, is filtered from the drake gate, gets no `actor` section | open | Safe by accident: all 11 carriers also have bodies. `checks/` §7 reports the latent count. Fixing D-03 is the fix |
| D-20 | 08-28 | `Generator.scala:1360, 1372, 1717-1769` | `Generator.isActor` matches exactly one definition in 132 (`Actor.json`); the `isLeaf \|\| isActor` branch and `instanceType = "actor"` are unreachable for every example actor | open | Delete or count as a role (F-04) |
| D-21 | 08-28 | `AssemblyValidator.scala:37` vs `Generator.scala:1139` | Validator compares via `namePath` (drops `typeParameters`); generator via `spelled` (keeps them) → spurious mismatch on a parameterized message type. Comment at `:32` claims they agree | open | No corpus case yet |
| D-22 | 08-28 | `Drake.scala:452-454`; `TypeLoader.scala:17`; `DrakeParseTest.scala:67-71` | Unwritten invariant "no JSON may spell `DracoType`": `rootRestored` elides it, `rooted` re-appends it, but the test's loader does not root, so a sole `[DracoType]` derivation fails gate 2 | open | `324556c` removed the last two such files; nothing prevents the next. `checks/` §8 counts explicit spellings |
| D-23 | 08-28 | `ScenarioDrakeTest.scala:41,57,100`; `SubtypeFactVisibilityTest.scala:83,86,110` | Two more report-only suites ending in `succeed`; the scenario test uses a CWD-relative path and `assume`, so a wrong CWD cancels rather than fails; the probe swallows `Throwable` into a string | open | Three suite headlines now cannot go red |
| D-24 | 08-28 | `domains/world/Consumer.json:12`, `Provider.json:12` | Keep an `Actor(World)` derivation and have no `actorAspect`; both notions of actor-ness false; plain-leaf emission; divergence visible only in report-only `ExampleDomainsGenTest` | deliberate | #63's acceptance corpus per `git-record-2026-08-26-1009`. `DRACO.md:189-191` Retired list denies they exist (X-06) |

## F — Design findings

| ID | First seen | Review § | Finding | Status | Note |
|---|---|---|---|---|---|
| F-01 | 08-28 | 3.0 | **Co-declaration drops the fact.** Evrete honours inheritance one level at a time; an ancestor and a descendant declared in one knowledge → resolver picks neither, insert skipped, no throw, one JUL warning. Corpus safe by shape, not design | open | Ranked first. Belongs in `DomainBuilder.validate` as a rule (R-11). Motivates #63; reframes dispatch as hierarchy→siblings, not routing |
| F-02 | 08-15 | 3.2 | **Parsed drake is not generation-safe.** `Drake.leafValue:679-684` demotes every non-application value to a host-opaque string; `Generator.expression:86` passes it through; no lint, no warning | open (worse) | Now demonstrated by the drake-first `scenario/` corpus (22 files, no JSON). Loss report 15 fields: 12 expression-form (#61), 3 empty-collection (#55). Lint still unwritten (R-13) |
| F-03 | 08-15 | 3.3 | **Generator is not a pure function of its input.** `chainHits:51-64` (catches `Throwable` at `:61`), `loadDracoType:111-120`, `inheritedElementNames:615-628`, `actorKnowledge:1169-1184` probe the classpath during emission; nothing cached | open | Unchanged between reviews |
| F-04 | 08-15 | 3.4 | **Two competing notions of actor-ness.** `hasActorBehavior` drives dispatch; `isActor` (derivation) drives imports/`isLeaf`, not counted as a role | half | Resolved in the corpus by `05b14bd`: 11 aspect carriers, `isActor` matches only `Actor.json` (D-20). Residue: D-20, D-21, D-24 |
| F-05 | 08-15 | 3.5 | **Codec inferred in code, declared in principle; presence model deferred.** Zero `codecAspect` carriers; both drake sides reject `codec`; `authoredAhead` = `BodyElement.json`, `ActorAspect.json` | open | No movement across both reviews; 4½ months at 08-28. Decision Q-02 |
| F-06 | 08-15 | 3.6 | **Core vs mods split inverted.** Engine (9 files) hand-written in mods; `TypeDictionary.apply:15` builds hollow members; `DomainBuilder.define:52-69` is the workaround | open | 08-28 restatement: `Generated.scala`/`Codec.scala` violate the ch. 43 principle, not `validate` (which checks declared members only). Decision Q-01 |
| F-07 | 08-15 | 3.7 | **DRAKE robustness** — (a) `par =x` swallowed silently; (b) `[cursor` runs to next `]`/EOF; (c) `mut{T}`; (d) spaced `Iterator ( (K,V) )`; (e) no line/column; (f) `take` at EOF; (g) duplicate sections overwrite; (h) latent Parameter-default-is-application in `leafLines:352-357`; (i) three renderers drift | open | 08-28 corrections: (c) now *errors* without position, not mis-parses; (d) narrower — only where a single token is expected; (f) is `IndexOutOfBounds`, not `ArrayIndexOutOfBounds`; (g) *unknown* sections now error at `:1063`. (a),(b),(e),(h),(i) unchanged |
| F-08 | 08-15 | 6 | **Authority laundering / bus factor of one.** Model coinages cited back as Dev's intent (ch. 69-70); #51's design lived only in issue comments | half | `DRACO.md:70-73` (model prose ≠ intent) and `:22-26` (issue approval) now in the auto-loaded file; both fired in ch. 74. Residue: #63's design lives in issue comments + one journal chapter — same pattern |
| F-09 | 08-15 | 6 | **Gate-scope drift** — counts reported without stating `testOnly` vs full suite | closed | All 10 records dated ≥ 08-15 state scope; expected report-only headlines written before the run; `sbt test &&` chained into the commit block (08-27) |
| F-10 | 08-15 | 6 | **Journal a week behind the tree** | closed | Ch. 73 extended to 08-25; ch. 74 reaches 08-28, behind by one exchange |
| F-11 | 08-15 | 6 | **Verification asymmetry** — `DracoGenTest` compares text not compilation; `ExampleDomainsGenTest` report-only (28/20); no push/PR CI | open (worse) | `DRACO.md:88-93` argues text-compare is transitively a compile check. New unverified surface: `scenario/` never generated or compiled |
| F-12 | 08-15 | 6 | **Reversals with high sunk cost** (YAML, reference frames, rule/actor suffix) — corpus-wide conventions should be prototyped on one file and measured first | closed | Method adopted: both #51 slices simulated corpus-wide before compiling, zero differences |

## X — Documentation drift

Grouped by file. `fixed 8aef403` is the 2026-08-17 doc-rewrite commit (four git-records squashed).

| ID | First seen | File | Finding | Status | Note |
|---|---|---|---|---|---|
| X-01 | 08-15 | `DRACO.md` | Described the `*Instance` triad, flat `TypeDefinition`, `Primal[String]`, `Generator.loadType`, "no `elementTypeNames`", stale Key Files table, nothing on `.drake` | **fixed `8aef403`** | Every §3 claim verified at source on 08-28. Key Files table deleted rather than corrected — no file index now |
| X-02 | 08-15 | `DRACO.md` | "File an issue for deferred work" contradicted the ch. 72 restraint memory | **fixed `8aef403`** | `:22-26`: new issue needs Dev's approval every time |
| X-03 | 08-28 | `DRACO.md:229` | "`CHANGELOG.md` — stops at alpha.5; ~25 git-records since" — false in the commit that wrote it | open | |
| X-04 | 08-28 | `DRACO.md:98, 218, 221` | Three incompatible statements about README (to be corrected / stale / rewritten and verified, canonical) | open | |
| X-05 | 08-28 | `DRACO.md:170, 230`; `README.md:196` | Call `drake.dlt` "current and authoritative" | open | See D-11 |
| X-06 | 08-28 | `DRACO.md:189-191` | Retired list says the `Actor(T)` derivation on an actor is retired; `world/Consumer.json`, `world/Provider.json` carry it by design; `Actor.scala:5` defines it | open | See D-24 |
| X-07 | 08-28 | `DRACO.md:211` | Lists 5 `draco-gen` subcommands; `GeneratorCLI.scala:277-289` has 7 | open | `generate-multi`, `compile-multi` |
| X-08 | 08-15 | `AGENTS.md` | Regular file, not the symlink it claims to be; **verbatim the pre-rewrite DRACO.md** (last touched `91e4a7c`, 2026-06-23) | open | Deleting tracked content is Dev's call — Q-05 |
| X-09 | 08-15 | `README.md` | Four aspects, `.rule` files, `loadRuleType`, `draco.language`, `from-yaml`/`to-yaml`, 5-way dispatch, "no endogenous actor", stale tree, tags alpha.1-3 | **fixed `8aef403`** | Rewritten language-neutral; residues table accurate row for row |
| X-10 | 08-28 | `README.md:459` | "Current build version is `2.0.0-alpha.5`" | open | Build is alpha.6 |
| X-11 | 08-28 | `README.md:451-453`; `GETTING_STARTED_TARGET_SCALA.md:180,195` | `bin/draco-gen verify draco/base/Meters.json` — `GeneratorCLI.scala:70-74` resolves a filesystem path; from repo root this exits 2 | open | First command a new user types |
| X-12 | 08-28 | `README.md:285-313` | Rule example then "condition and action body are expression trees, not text" — `PrimesFromNaturalSequence.json:44-70`'s action body is 4 host-opaque strings + 1 Fixed | open | |
| X-13 | 08-28 | `README.md:518-541` | Project tree omits `org/nexonix/` (main+test), `format/yaml/`, `resources/draco/format/`, `test/resources/scenario/` | open | |
| X-14 | 08-15 | `GETTING_STARTED.md` | `from-yaml`/`to-yaml`; `inspect <TypeName>`; "REPL"; no `DrakeCLI` | **fixed `8aef403`** | Split into three per-target guides; nine identical sections verified; every command in the Scala table exists |
| X-15 | 08-28 | `GETTING_STARTED_TARGET_SCALA.md` | `discover` listed without the `--force` its usage text requires; §5 runs `draco.CLI` without noting it prints alpha.1 | open | minor |
| X-16 | 08-15 | `CHANGELOG.md` | Stopped at alpha.5 (2026-06-03) | **fixed `8aef403`** | Backfilled; per-commit trigger; held 3/3 after the `9f9bebd` self-repair |
| X-17 | 08-28 | `CHANGELOG.md:81` | `[2.0.0-alpha.6] - 2026-08-17`; `git log` puts the release commit and tag at 2026-08-25 | open | Prepared 08-17, cut 08-25; gap was 83 days, not 75 |
| X-18 | 08-15 | `src/mods/README.md:18, 40` | "`resources/` (empty initially)" (49 JSON); "currently `Generator`" (9 engine files); layout tree omits `domains/` | open | Untouched by any commit since 08-15 |
| X-19 | 08-15 | `HOLARCHY.md:20-81` | `Egocentric`/`Ego` (now `Sentient` under `World`); `Terrain` (now `Terrestrial`); fifth medium absent | open | Honest status header; but the name-overlap its media section rests on no longer exists |
| X-20 | 08-15 | `ORION.md` | Vocabulary "absent from code" per DRACO.md — six names exist as empty traits under `dreams/orion/`; nothing connects them | open | |
| X-21 | 08-15 | root | `PrimeOrdinal.txt` (0 refs in `src/`); two unattributed vendor notes, one with a leading-space filename | open | = D-15 |

## R — Recommendations and uptake

Uptake is scored at the *next* review. 08-15 recommendations were scored on 08-28; 08-28 recommendations are scored by the next review.

| ID | Review | Recommendation | Uptake (at `0af56fd`) |
|---|---|---|---|
| R-01 | 08-15 #1 | Rewrite `DRACO.md` against §2; reconcile issue-filing with memory; symlink `AGENTS.md` | **taken** (DRACO.md, reconciliation) / **not** (AGENTS.md) |
| R-02 | 08-15 #2 | Close the concrete bugs, rows 1-10, one session | **not started** — no record mentions it |
| R-03 | 08-15 #3 | Drake-form-token lint; operator trees via one fixity table; bracket Parameter default in `leafLines`; narrow `surfaceCarried` | **partial** — `surfaceCarried` strip removed outright (`324556c`); lint, trees, `leafLines` not |
| R-04 | 08-15 #4 | Stop tests writing source; assertions on log-only suites; fire generated primes rules; push/PR CI | **not started** — CI deliberately deferred to Dev (`git-record-2026-08-17-1830`) |
| R-05 | 08-15 #5 | #51 as scoped (derivation edge only) after lifting its design into a doc; TypeName shape change Scala-first on one file | **taken** — derivation edge `324556c`, actor edge `05b14bd`; shape change rejected on principle; design partly lifted into `drake.dlt`/CHANGELOG/DRACO.md |
| R-06 | 08-15 #6 | Generator pure function; unify headers/factory blocks; `generate(Seq)` through dispatch; collapse `isActor`; mods actors onto `messageType` | **partial** — mods actors migrated 10/12 (`05b14bd`); purity, unification, `isActor` not |
| R-07 | 08-15 #7 | Retire dead weight; Pekko off milestone | **not started** |
| R-08 | 08-15 #8 | Decide the deferred pair (presence model, declared codec) | **not started** |
| R-09 | 08-15 #9 | Docs pass per §5; DRAKE section; `bin/draco-drake`; alpha.6 CHANGELOG; tag/release | **mostly taken** — docs, CHANGELOG, release `v2.0.0-alpha.6`; `bin/draco-drake` not (now a published limitation) |
| R-10 | 08-15 #10 | Make the transform thesis a real test; move `world/Consumer.scala:17-28` into a rule | **not started** — and `world/Consumer` is now #63's worked example, so the second half is withdrawn |
| R-11 | 08-28 #1 | Encode the co-declaration constraint as a `DomainBuilder.validate` rule; write the hierarchy→siblings framing into #63 | pending |
| R-12 | 08-28 #2 | Close the §4 list, D-03 first; add D-21, D-22; delete `isActor` (D-20) | pending |
| R-13 | 08-28 #3 | Drake-form-token lint now; first #61 slice with one fixity table for all three renderers; bracket Parameter default | pending |
| R-14 | 08-28 #4 | Stop tests writing tracked source; take #62; fire generated primes rules; push/PR CI | pending |
| R-15 | 08-28 #5 | #51 remaining edges (`valueType` expressions, `TypeParameter` atom), renderer-first, one file, measured | pending |
| R-16 | 08-28 #6 | Generator pure function; loaded-family context; cache; drop `Throwable` catch | pending |
| R-17 | 08-28 #7 | Doc residue: X-03..X-07, D-11, X-11, X-12, X-13, X-18; decide `AGENTS.md` | pending |
| R-18 | 08-28 #8 | Retire dead weight; unused deps; Pekko off milestone | pending |
| R-19 | 08-28 #9 | Decide the deferred pair or re-file post-Generator[L]; fix the two `authoredAhead` JSONs | pending |
| R-20 | 08-28 #10 | Transform thesis as a real test; `world/Consumer` stays hand-written but gets a real assertion | pending |
