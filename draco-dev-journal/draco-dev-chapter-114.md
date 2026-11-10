# Chapter 114 - Output's Location Branch and the Report Factories

**Session date:** October 29–30, 2026
**Topic:** Terrestrial Output's conversion moves into its definition as a case branch; LocationReport and then the three sibling reports take a json factory and every report in the example chain constructs through its definition.

**Provenance:** Original Claude Code session `f12792d4-b3c1-4ac3-b8f9-c2853199c07f`, extract records 27–44; see the [bounded extract](sources/claude-2026-10-26-to-11-03.jsonl) and [retrieval audit](sources/claude-2026-10-26-to-11-03-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Accept the Output repair as recommended, or correct it?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 1379, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-10-29T20:31:12.371Z; source line 1389; extract record 27*

Accepted, build it

---

**[Claude]** *2026-10-29T20:38:13.188Z; source line 1646; extract record 28*

**Built and verified directly.** Output's trio moved as one, and every discovered suite passed on fresh class roots.

**What changed:**

- `src/mods/resources/domains/terrestrial/Output.drake` now carries the conversion as a `case location Location [ … ]` branch. The branch type is spelled bare because Location is in Output's own package. Inside it are two strict bindings, `now fix payload` and `now fix report`, and `mon consumer(report)`. The accepted wording said "three strict bindings"; that count included the consumer line, which is a call, not a binding.
- The report is built as `new LocationReport { … }` host text, the form the terrestrial OriginateReport rule has carried since October 24. LocationReport still has no factory.
- `Output.json` is re-canonicalized from the drake with the compiled parser, drift zero over terrestrial. `Output.scala` is the compiled generator's output. It gains the Terrestrial actor convention the handwritten file lacked: `elementTypeNames` and a `knowledge` registering the domain's two rules, exactly as Creator and the terrestrial Consumer have. The dispatch renders `case location: Location =>` with the generator's `case _ => ()`. No engine change.
- `OutputProjectionTest` (4 tests) is the 73rd suite: exact trio agreement with the World actor role, one LocationReport per Location with the four payload keys, Observable and Position ignored before and after, and lazy consumer capture once per actor.

**Verification.** The first direct run failed one test, the lazy-capture case, because my test helper took the consumer strictly and evaluated the counting factory at the call. The helper now takes it by-name, as actorType does; Output itself was never at fault. The second run passed every suite.

| | result |
|---|---|
| tests / suites (direct, incl. discovery) | 942 / 74, zero failed or aborted |
| expected under sbt | 942 / 73 |
| example map | 37 match, 11 differ, 0 error, 0 missing (was 36 / 12) |
| every other headline | unchanged; pending mods actors 0 |

The record `draco-git-record/git-record-2026-10-29-1640`, CHANGELOG entry, DRACO.md table and baseline, README lines, handoff and resume prompt are written, with your gate marked pending until you paste it. Link checker clean.

The gate, with the console headlines and the file-only pending count:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:" && grep "pending" target/test-output/DrakeGenTest.log
```

Paste both grep outputs. The expected line is 942 passed, zero failed, with the example map at 37 match / 11 differ and everything else as in the table. Once it is green I'll finalize the gate wording in the documents and hand you the path-scoped commit block.

---

*(Delegated action — Dev ran the full sbt gate: 942 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 37 match, 11 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 133 types; Drake.parse scope: 110 draco + 23 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; mods actors pending .drake authoring (0):. Source: 2026-10-30T01:01:17.083Z; source line 1652; extract record 29. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-30T01:02:08.680Z; source line 1698; extract record 30*

**Gate confirmed.** 942 passed, zero failed, canceled, ignored or pending, example map 37 match / 11 differ, every other headline matching the table, pending mods actors 0. Suites move to 73.

The documents now say verified rather than pending. The record carries your gate line, DRACO.md's baseline reads 942 / 73 with the increment paragraph closed out, README's two checkpoint lines and the example-map paragraph are updated, and the handoff ledger carries this increment as pending its commit hash.

The record and the test are untracked, so the block adds both and scopes the commit to the ten paths:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-10-29-1640 src/test/scala/domains/terrestrial/OutputProjectionTest.scala && git commit -F draco-git-record/git-record-2026-10-29-1640 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- src/mods/resources/domains/terrestrial/Output.drake src/mods/resources/domains/terrestrial/Output.json src/mods/scala/domains/terrestrial/Output.scala src/test/scala/domains/terrestrial/OutputProjectionTest.scala draco-git-record/git-record-2026-10-29-1640 CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md && git push && git log -1 --oneline
```

Once it lands I'll write the hash into the ledger and bring the next point from the inventory. The adjacent one is LocationReport, which has no drake surface and no factory, so both Output and OriginateReport construct it as host text.

---

*(Delegated action — Dev committed c5b8089, “Carry terrestrial Output's Location branch in its actor definition” (10 files changed, 323 insertions(+), 132 deletions(-)). Pushed to origin/main: 112f277..c5b8089. Source: 2026-10-30T02:50:55.270Z; source line 1702; extract record 31. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-30T02:55:50.421Z; source line 1918; extract record 32*

**c5b8089 landed and is recorded.** The handoff ledger row and Start-here bullet, and the resume prompt, now name the hash instead of "pending". Those two files are the only uncommitted changes and will ride with the next commit.

**Next related point: the report types construct through a factory, not host text.**

The Output repair left one thing in its own definition that is not drake: the report line. Five definition sites build a report the same way, as the host-opaque string `new X { override lazy val typeDefinition = X.typeDefinition; override lazy val json = payload }`: terrestrial Output and the four OriginateReports. The reason is that the four report types (terrestrial LocationReport, aerial PositionReport, marine FixReport, ethereal EphemerisReport) are JSON-only definitions with no drake surface, no elements and no factory. Each is `type X from <Medium> draco format dracon Dracon` and nothing more.

I probed with the compiled CLIs before recommending. A LocationReport surface carrying

```text
type LocationReport from Terrestrial draco format dracon Dracon
  factory
    parameters
      par json Json
domain domains terrestrial Terrestrial
```

parses, emits back clean, and generates a companion with `apply(_json: => Json)`, a Dracon-shaped encoder and decoder, and `Null = apply(Json.Null)`, importing `io.circe.{Decoder, Encoder, Json}` exactly as Dracon's own companion does. The bare spelling `Json` matters: the qualified `io.circe.Json` spelling yields a Null of `null.asInstanceOf[io.circe.Json]`, which is worse. Re-authoring the two terrestrial sites as `fix report LocationReport LocationReport(payload)` regenerates OriginateReport.scala and Output.scala with exactly that one line changed each. No engine change. The hand constructions in tests stay valid Scala.

**Recommendation:** give LocationReport its drake surface with the `json` factory, re-canonicalize its JSON, replace its hand-written Scala with the generated file, re-author both terrestrial sites as `LocationReport(payload)` and regenerate them, and add one `LocationReportTest` covering exact trio agreement, a lazily evaluated factory argument, and a codec round-trip of the json. The three sibling reports follow by the same pattern as the next increment. Expected headline moves: parse scope to 110 draco + 24 mods and the surface-loss count to 134 types, example map unchanged at 37 / 11, everything else unchanged.

One observation, not part of this point: the four Intent types share the same shape, but no definition constructs them, only tests do.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the LocationReport json factory with both terrestrial sites re-authored, or correct the scope?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 1907, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-10-30T14:36:49.869Z; source line 1922; extract record 33*

Accepted, build it

---

**[Claude]** *2026-10-30T14:42:28.360Z; source line 2146; extract record 34*

**Built, directly verified, and ready for your gate.**

What changed:
- `LocationReport.drake` is new, the first drake surface for a report type, carrying `factory parameters par json Json` with the bare spelling. Its JSON is the compiled parser's re-canonicalization and its Scala the generator's output, which keeps the trait, kind-vals and loader line and adds `apply`, the Dracon-shaped codec and `Null`.
- OriginateReport and Output are re-authored `fix report LocationReport LocationReport(payload)`. Each regenerates with exactly that one line changed. `DrakeCLI check` over terrestrial reports zero drift.
- `LocationReportTest` has three cases: exact trio agreement with the factory's one parameter and the two parents, the by-name argument evaluated once on first read and never when unused, and the codec round-trip. The existing OriginateReportTest and OutputProjectionTest pin the two regenerated sites unchanged.
- No engine change. Record, CHANGELOG, DRACO.md baseline and table, README, handoff and resume prompt are written, with Dev's gate marked pending.

Direct run of every discovered suite with current main, mods and test resources on the loader roots:

| figure | value |
|---|---|
| tests | 947 passed, 0 failed, canceled, ignored, pending |
| suites | 75 including discovery, 0 aborted |
| parse scope | 110 draco + 24 mods |
| surface losses | 0 across 134 types |
| example map | 37 match / 11 differ |
| pending mods actors | 0 |

The count is 942 plus the three new tests plus the two parser checks the new drake surface activates, so expect 947 tests / 74 suites from sbt. The first direct run stopped at test compilation on an accessor in the new test, corrected before the second; no production file changed between runs.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:" ; grep "pending" target/test-output/DrakeGenTest.log
```

Paste the grep output. Expected: every headline above unchanged except parse scope and surface-loss coverage, and `Tests: succeeded 947, failed 0`. After a green paste I'll finalize the documents and hand over the path-scoped commit block.

---

*(Delegated action — Dev ran the full sbt gate: 947 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 37 match, 11 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 134 types; Drake.parse scope: 110 draco + 24 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; mods actors pending .drake authoring (0):. Source: 2026-10-30T14:50:24.149Z; source line 2150; extract record 35. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-30T14:51:20.527Z; source line 2188; extract record 36*

**Gate confirmed.** 947 succeeded, zero failed, canceled, ignored or pending. Parse scope moved to 110 draco + 24 mods and surface-loss coverage to 134 types, as the new drake surface predicts. Every other headline is unchanged, example map 37 / 11, pending mods actors 0. The suite count is 74.

The documents now say verified: the record carries your gate line, DRACO.md's baseline reads 947 / 74 with the increment paragraph closed, the 942 baseline is marked committed as c5b8089, README's checkpoint lines are updated, and the handoff ledger carries this increment pending its commit hash.

Three files are untracked, so the block adds them and scopes the commit to the sixteen paths:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-10-30-1038 src/mods/resources/domains/terrestrial/LocationReport.drake src/test/scala/domains/terrestrial/LocationReportTest.scala && git commit -F draco-git-record/git-record-2026-10-30-1038 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- src/mods/resources/domains/terrestrial/LocationReport.drake src/mods/resources/domains/terrestrial/LocationReport.json src/mods/scala/domains/terrestrial/LocationReport.scala src/mods/resources/domains/terrestrial/OriginateReport.drake src/mods/resources/domains/terrestrial/OriginateReport.json src/mods/scala/domains/terrestrial/OriginateReport.scala src/mods/resources/domains/terrestrial/Output.drake src/mods/resources/domains/terrestrial/Output.json src/mods/scala/domains/terrestrial/Output.scala src/test/scala/domains/terrestrial/LocationReportTest.scala draco-git-record/git-record-2026-10-30-1038 CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md && git push && git log -1 --oneline
```

Once it lands I'll write the hash into the ledger and bring the next related point: the three sibling reports, PositionReport, FixReport and EphemerisReport, by the same pattern.

---

*(Delegated action — Dev committed e138f04, “Give terrestrial LocationReport a json factory and construct it through it” (16 files changed, 288 insertions(+), 33 deletions(-)). Pushed to origin/main: c5b8089..e138f04. Source: 2026-10-30T14:52:11.717Z; source line 2192; extract record 37. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-30T14:53:42.179Z; source line 2246; extract record 38*

**e138f04 landed and is recorded.** The handoff ledger, Start-here bullet, resume prompt and DRACO.md's increment paragraph now name the hash. Those three files are the only uncommitted changes and will ride with the next commit.

**Next related point: the three sibling reports take the same factory.**

PositionReport (aerial), FixReport (marine) and EphemerisReport (ethereal) have exactly LocationReport's former shape: JSON-only, no surface, no elements, no factory, each constructed as host text at one site, its medium's OriginateReport. I probed all three with the compiled CLIs, the same way as LocationReport:

- Each surface `type X from <Medium> draco format dracon Dracon` with `factory parameters par json Json` parses, emits back clean, and generates a companion that adds 27 lines to the hand-written one and removes none: the same `apply`, codec and `Null` LocationReport gained.
- Each OriginateReport re-authored `fix report X X(payload)` regenerates with exactly that one line changed.
- All four OriginateReport tests already pin their Scala by exact agreement, so the regenerated rules are gated without a test change.

**Recommendation:** build the three in one increment, since the pattern is proven and each piece is one new surface plus one re-authored line. Replace the three hand-written companions with the generated files, re-canonicalize the three OriginateReport JSONs, and add one `ReportFactoryTest` in the domains package with three cases, each iterating over the three reports: exact trio agreement, the by-name argument, the codec round-trip. Expected headline moves: parse scope to 110 + 27, surface-loss coverage to 137 types, example map unchanged at 37 / 11, and 947 plus three tests plus six activated parser checks, so 956 tests / 75 suites.

After this, every report in the example chain constructs through its definition, and the thread that began with Output's binder is closed.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the three sibling report factories in one increment, or correct the scope?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 2233, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-10-30T15:12:42.306Z; source line 2250; extract record 39*

Accepted, build it

---

**[Claude]** *2026-10-30T15:16:32.656Z; source line 2358; extract record 40*

**Built, directly verified on the first run, and ready for your gate.**

What changed:
- Three new drake surfaces, `PositionReport.drake`, `FixReport.drake` and `EphemerisReport.drake`, each `type X from <Medium> draco format dracon Dracon` with `factory parameters par json Json`. Their JSON files carry the factory record and nothing else moved; their companions are the generator's output, 27 lines added and none removed.
- The aerial, marine and ethereal OriginateReports are re-authored `fix report X X(payload)`, each regenerating with exactly that one line changed. `DrakeCLI check` over the three media reports zero drift.
- `ReportFactoryTest` is one suite with three cases, each iterating over the three reports: exact trio agreement, the by-name argument evaluated once on first read, and the codec round-trip. The four existing OriginateReportTests pin the regenerated rules unchanged.
- No engine change. Record, CHANGELOG, DRACO.md baseline and table, README, handoff and resume prompt are written with your gate marked pending.

Direct run of every discovered suite with current main, mods and test resources on the loader roots:

| figure | value |
|---|---|
| tests | 956 passed, 0 failed, canceled, ignored, pending |
| suites | 76 including discovery, 0 aborted |
| parse scope | 110 draco + 27 mods |
| surface losses | 0 across 137 types |
| example map | 37 match / 11 differ |
| pending mods actors | 0 |

That is 947 plus three new tests plus the six parser checks the three surfaces activate, so expect 956 tests / 75 suites from sbt.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:" ; grep "pending" target/test-output/DrakeGenTest.log
```

Paste the grep output. Expected: every headline unchanged except parse scope and surface-loss coverage, and `Tests: succeeded 956, failed 0`. After a green paste I'll finalize the documents and hand over the path-scoped commit block for the 25 paths.

---

*(Delegated action — Dev ran the full sbt gate: 956 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 37 match, 11 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 137 types; Drake.parse scope: 110 draco + 27 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; mods actors pending .drake authoring (0):. Source: 2026-10-30T16:36:19.118Z; source line 2362; extract record 41. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-30T16:38:36.063Z; source line 2501; extract record 42*

**Gate verified: 956 tests / 75 suites, green on its first run, every headline matching the direct run.** Parse scope moved to 110 draco + 27 mods and surface-loss coverage to 137 types, as the three new surfaces predicted. The example map stayed at 37 match / 11 differ, and pending mods actors is 0.

The documents are finalized from pending to verified: the 1113 record's verification paragraph, the DRACO.md baseline and increment heading, the README checkpoint lines, the handoff's start-here bullet, section and ledger row, and the resume prompt. The link checker is clean. The tree holds 25 paths, 20 modified and 5 untracked.

Commit block, path-scoped, the five untracked files added first:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-10-30-1113 src/mods/resources/domains/aerial/PositionReport.drake src/mods/resources/domains/marine/FixReport.drake src/mods/resources/domains/ethereal/EphemerisReport.drake src/test/scala/domains/ReportFactoryTest.scala && git commit -F draco-git-record/git-record-2026-10-30-1113 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- src/mods/resources/domains/aerial/PositionReport.drake src/mods/resources/domains/aerial/PositionReport.json src/mods/scala/domains/aerial/PositionReport.scala src/mods/resources/domains/aerial/OriginateReport.drake src/mods/resources/domains/aerial/OriginateReport.json src/mods/scala/domains/aerial/OriginateReport.scala src/mods/resources/domains/marine/FixReport.drake src/mods/resources/domains/marine/FixReport.json src/mods/scala/domains/marine/FixReport.scala src/mods/resources/domains/marine/OriginateReport.drake src/mods/resources/domains/marine/OriginateReport.json src/mods/scala/domains/marine/OriginateReport.scala src/mods/resources/domains/ethereal/EphemerisReport.drake src/mods/resources/domains/ethereal/EphemerisReport.json src/mods/scala/domains/ethereal/EphemerisReport.scala src/mods/resources/domains/ethereal/OriginateReport.drake src/mods/resources/domains/ethereal/OriginateReport.json src/mods/scala/domains/ethereal/OriginateReport.scala src/test/scala/domains/ReportFactoryTest.scala draco-git-record/git-record-2026-10-30-1113 CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md && git push && git log -1 --oneline
```

Expected: 25 files changed, and `git status --short` empty afterwards. Paste the commit and push output and I will fold the hash into the ledger, handoff, resume prompt and DRACO.md.

After that hash lands, the report thread is closed and nothing from it remains preserved. The next related point, per your standing rule, comes from this inventory, none of it accepted: the eleven remaining example differences, the four Intent types that only tests construct, deferred activation, and the .dcon migration. Journal chapter 113 for this session is also unwritten.

---

*(Delegated action — Dev committed 8540341, “Give the three sibling reports the json factory and construct them through it” (25 files changed, 359 insertions(+), 40 deletions(-)). Pushed to origin/main: e138f04..8540341. Source: 2026-10-30T18:18:24.297Z; source line 2510; extract record 43. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-30T18:21:14.772Z; source line 2650; extract record 44*

**Committed and pushed as 8540341, 25 files, tree clean but for the hash fold.** DRACO.md's baseline paragraph and increment heading, the handoff's start-here, section and ledger row, the resume prompt and the README checkpoint lines now name the commit. Those four document edits are uncommitted and ride with the next increment's commit, as the earlier folds did. Memory is updated. Every report in the example chain constructs through its definition, and nothing from the report thread remains preserved.

**Next related point.** The four Intent types are the one remaining shape of the kind the report thread repaired. FlightIntent, VoyageIntent, LaunchIntent and MarchIntent each derive their medium and Dracon, carry no elements, no factory and no drake surface, and are constructed as host text at eight test sites: the four OriginateReportTests, the three chain tests and AerialAssemblyTest, four of them with a strict `override val json`. They are each medium's entry message, the fact OriginateReport joins, so a Haskell target needs their constructor as much as the reports'.

I probed FlightIntent with the compiled parser and generator in the scratchpad. The surface parses and emits back clean. The JSON gains the factory record and nothing else moves. The companion gains 25 lines, the same `apply`, Dracon-shaped codec and `Null` the reports gained, with none removed.

**Recommendation:** give the four Intent types the json factory by the report pattern, in one increment. Four drake surfaces, JSON re-canonicalized, companions replaced by the generator's output, the eight test sites re-authored `X(payload)`, and one `IntentFactoryTest` in the domains package with the three ReportFactoryTest cases iterating over the four. No engine change. Expected headline moves:

| headline | expected |
|---|---|
| parse scope | 110 draco + 31 mods |
| surface losses | 0 across 141 types |
| example map | 37 / 11, unchanged |
| tests | 956 + 3 + 8 parser checks = 967 / 76 |

The handoff records this as raised and unaccepted. The rest of the inventory stays as it was: the eleven example differences, deferred activation and the .dcon migration, none accepted. Chapter 113 for this session is unwritten.

---

## Status

Three increments, each accepted with "Accepted, build it", gated and committed and pushed by Dev: c5b8089 (Output's Location branch, 942 tests, example map 37 / 11), e138f04 (LocationReport's json factory, 947 tests) and 8540341 (PositionReport, FixReport and EphemerisReport, 956 tests). No engine change in any of them. Open at the close and NOT accepted: the same factory for the four Intent types.
