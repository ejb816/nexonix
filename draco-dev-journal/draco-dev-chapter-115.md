# Chapter 115 - The Intent Factories, the Coordinate Surfaces and the Example Map as a Gate

**Session date:** October 30–31, 2026
**Topic:** The four Intent types take the json factory; Position and Location take their drake surfaces and the generator's companions; the last nine example differences close and ExampleDomainsGenTest asserts 48 match / 0 differ.

**Provenance:** Original Claude Code session `f12792d4-b3c1-4ac3-b8f9-c2853199c07f`, extract records 45–62; see the [bounded extract](sources/claude-2026-10-26-to-11-03.jsonl) and [retrieval audit](sources/claude-2026-10-26-to-11-03-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Give the four Intent types the same json factory?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 2644, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-10-31T00:12:04.661Z; source line 2654; extract record 45*

Accepted, build it

---

**[Claude]** *2026-10-31T00:17:47.287Z; source line 2942; extract record 46*

**Built as accepted, green on the first direct run.** The four Intent types each carry the surface `type X from <Medium> draco format dracon Dracon` with the json factory. Their JSON gained the factory record and nothing else. Each companion is the generator's output, 25 lines and two imports added, none removed. `DrakeCLI check` reports zero drift over all four media. The eight test sites now construct `X(payload)`, and `IntentFactoryTest` in the domains package runs the three ReportFactoryTest cases over the four. No engine change, no definition site, since no definition constructs an intent.

Direct run of every discovered suite on compiled classes with current main, mods and test resources on the loader roots:

| headline | result |
|---|---|
| tests | 967 passed, 77 suites including discovery, 0 failed / canceled / ignored / pending / aborted |
| parse scope | 110 draco + 31 mods, 0 held back |
| surface losses | 0 across 141 types |
| example map | 37 match, 11 differ, 0 error, 0 missing |
| pending mods actors | 0, file-only log |

Every other headline unchanged: aspects 110, GenDrake 108 of 108, PON 80 / 550 / 42, canonical 7, scenario 23 clean, forest 1 of 2, CO-DECLARATION. sbt should report 967 tests / 76 suites. The record, CHANGELOG entry, DRACO.md baseline and table, README, handoff and resume prompt are written with pending wording and the previous increment's hash folded in.

Gate, paste the grep output as before:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:" ; grep -c "pending .drake authoring (0)" target/test-output/DrakeGenTest.log
```

The commit block follows the pasted gate, so the record and documents can first be set to verified wording.

---

*(Delegated action — Dev ran the full sbt gate: 967 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 37 match, 11 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 141 types; Drake.parse scope: 110 draco + 31 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. The trailing count of the zero-pending line printed 1. Source: 2026-10-31T12:12:47.837Z; source line 2946; extract record 47. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-31T12:14:04.888Z; source line 2991; extract record 48*

**Verified: 967 tests / 76 suites, every headline matching, pending mods actors 0, green on your first run.** The record, DRACO.md baseline and increment paragraph, CHANGELOG, README, handoff and resume prompt all read "verified by Dev on October 31, commit pending". The link checker is clean.

The tree holds 27 files: four new drake surfaces, four re-canonicalized JSON files, four regenerated companions, eight re-authored test files, IntentFactoryTest, the record, and the six documents, including the four that carried the previous hash fold.

Commit and push, path-scoped to that set:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-10-30-2014 src/mods/resources/domains/aerial/FlightIntent.drake src/mods/resources/domains/marine/VoyageIntent.drake src/mods/resources/domains/ethereal/LaunchIntent.drake src/mods/resources/domains/terrestrial/MarchIntent.drake src/test/scala/domains/IntentFactoryTest.scala && git commit -F draco-git-record/git-record-2026-10-30-2014 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- draco-git-record/git-record-2026-10-30-2014 CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md src/mods/resources/domains src/mods/scala/domains src/test/scala/domains && git push && git log -1 --oneline && git status --short
```

Paste the output. Then the hash is folded into the documents, and the next related point follows: with every example-chain message constructing through its definition, the remaining inventory is the eleven example differences, deferred activation and the .dcon migration, with nothing preserved as a recommendation.

---

*(Delegated action — Dev committed 3a3cbfc, “Give the four Intent types the json factory and construct them through it” (27 files changed, 413 insertions(+), 64 deletions(-)). Pushed to origin/main: 8540341..3a3cbfc. Source: 2026-10-31T12:25:25.670Z; source line 2995; extract record 49. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-31T12:28:27.403Z; source line 3112; extract record 50*

**Committed and pushed as 3a3cbfc, 27 files, tree clean but for the hash fold.** DRACO.md's baseline and increment paragraph, the handoff's start-here bullet, section and ledger row, the resume prompt and the README checkpoint lines now name the commit. Those four edits are uncommitted and ride with the next commit, as before. Every message in the example chain now constructs through its definition, and nothing from the report thread remains preserved.

**Next related point.** Of the eleven example differences, nine are companion spelling the generator would fix in five to seven lines each. Two are substantive in the same way the report thread was: aerial Position and terrestrial Location, the two coordinate types, have no drake surface, and their hand-written companions lack the codec the generator infers from their elements. Cartesian had exactly this gap, and its inferred codecs were accepted on October 25.

I probed both with the compiled CLIs. `DrakeCLI emit` renders each surface from its JSON, three fixed elements and a factory with three defaulted parameters. Parsing that surface back reproduces the committed JSON byte for byte. The generator's companion differs from the hand-written one only by the inferred codec, an encoder and a decoder that supplies the factory defaults for missing fields, plus the import lines. Callers, Input, the world Consumer and five tests, construct through the unchanged `apply`.

**Recommendation:** give Position and Location their drake surfaces as the emitter writes them, replace both companions with the generator's output, and add one `CoordinateProjectionTest` in the domains package iterating over the two: trio agreement, lazy defaults and Null, codec round-trip with missing-field defaults. No engine change, no definition site changes. Expected headline moves:

| headline | expected |
|---|---|
| example map | 39 match / 9 differ |
| parse scope | 110 draco + 33 mods |
| surface losses | 0 across 143 types |
| tests | 967 + 3 + 4 parser checks = 974 / 77 |

The handoff records this as raised and unaccepted, with the remaining inventory after it: the three ConsumeReport companions, the five domain objects, aerial Input, deferred activation and the .dcon migration. Chapter 113 for this session is unwritten.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Give Position and Location their drake surfaces and generated companions?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 3100, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-10-31T12:41:45.933Z; source line 3116; extract record 51*

Accepted, build it

---

**[Claude]** *2026-10-31T12:46:18.979Z; source line 3311; extract record 52*

**Built as accepted, green on the first direct run.** Position and Location carry the surfaces the emitter renders from their JSON, and parsing each back reproduces the committed JSON byte for byte, so no JSON moved. Both companions are the generator's output, differing from the hand-written ones only by the inferred codec, 21 lines each. `DrakeCLI check` reports zero drift over aerial and terrestrial. `CoordinateProjectionTest` in the domains package runs three cases over the two: the trio, the lazy defaulted factory and Null, the codec with missing-field defaults and an invalid value refused. No engine change, no caller changed.

Direct run of every discovered suite on compiled classes with current main, mods and test resources on the loader roots:

| headline | result |
|---|---|
| tests | 974 passed, 78 suites including discovery, 0 failed / canceled / ignored / pending / aborted |
| example map | 39 match, 9 differ, 0 error, 0 missing |
| parse scope | 110 draco + 33 mods, 0 held back |
| surface losses | 0 across 143 types |
| pending mods actors | 0, file-only log |

Every other headline unchanged: aspects 110, GenDrake 108 of 108, PON 80 / 550 / 42, canonical 7, scenario 23 clean, forest 1 of 2, CO-DECLARATION. sbt should report 974 tests / 77 suites. The record, CHANGELOG entry, DRACO.md baseline, table and §2 figure, README, handoff and resume prompt are written with pending wording, and the earlier hash fold for 3a3cbfc rides in the same set.

One procedural note for the record: the emitter's `--write` was run on a scratch copy of each directory, never on the real one, because it would also have created a ConsumeReport surface outside the accepted scope.

Gate, paste the grep output as before:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:" ; grep -c "pending .drake authoring (0)" target/test-output/DrakeGenTest.log
```

The commit block follows the pasted gate, so the record and documents can first be set to verified wording.

---

*(Delegated action — Dev ran the full sbt gate: 974 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 39 match, 9 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 143 types; Drake.parse scope: 110 draco + 33 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. The trailing count of the zero-pending line printed 1. Source: 2026-10-31T13:25:52.642Z; source line 3315; extract record 53. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-31T13:26:39.628Z; source line 3346; extract record 54*

**Verified: 974 tests / 77 suites, every headline matching, pending mods actors 0, green on your first run.** The record, DRACO.md baseline and increment paragraph, CHANGELOG, README, handoff and resume prompt all read "verified by Dev on October 31, commit pending". The link checker is clean.

The tree holds 11 files: two new drake surfaces, two regenerated companions, CoordinateProjectionTest, the record, and the six documents, four of which also carry the fold of 3a3cbfc.

Commit and push, path-scoped to that set:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-10-31-0842 src/mods/resources/domains/aerial/Position.drake src/mods/resources/domains/terrestrial/Location.drake src/test/scala/domains/CoordinateProjectionTest.scala && git commit -F draco-git-record/git-record-2026-10-31-0842 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- draco-git-record/git-record-2026-10-31-0842 CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md src/mods/resources/domains src/mods/scala/domains src/test/scala/domains && git push && git log -1 --oneline && git status --short
```

Paste the output. Then the hash is folded into the documents and the next related point follows. Nine example differences remain, all companion spelling: the three ConsumeReport companions, the five domain objects, and aerial Input.

---

*(Delegated action — Dev committed 23333f3, “Give Position and Location their drake surfaces and the generator's companions” (11 files changed, 288 insertions(+), 28 deletions(-)). Pushed to origin/main: 3a3cbfc..23333f3. Source: 2026-10-31T13:36:04.359Z; source line 3350; extract record 55. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-31T13:38:49.390Z; source line 3442; extract record 56*

**Committed and pushed as 23333f3, 11 files, tree clean but for the hash fold.** DRACO.md, the handoff, the resume prompt and the README name the commit. Those four edits are uncommitted and ride with the next commit, as before.

**Next related point.** Nine example differences remain and every one is companion spelling. The three ConsumeReport companions lack `with DracoType` and `override`. The five domain objects lack the same two words and spell their imports by hand. Aerial Input lacks the actor convention Creator already carries: `elementTypeNames`, a private unused knowledge, lazy bindings in receive. The four ConsumeReports also have no drake surface.

I probed all of it with the compiled CLIs. All nine generated companions compile against the current classes, with only the corpus's usual App warnings. The emitter renders each ConsumeReport surface: the type line, the domain line, a rule with one report variable and one monadic action. Parsing a surface back re-canonicalizes its JSON, treeing the action call and putting the aspect keys in canonical order, and the Scala generated from either JSON is identical. So those four move as trios, aerial's companion already matching.

**Recommendation:** close the example map and make it a gate, in one increment. Four ConsumeReport surfaces with their JSON re-canonicalized, nine companions replaced by the generator's output, and `ExampleDomainsGenTest` tightened from a report into a gate asserting zero differences. Its own comment names that step, and the example-map row leaves the DRACO.md table, a measurement become an assertion. No new suite, no engine change. Expected headline moves:

| headline | expected |
|---|---|
| example map | 48 match / 0 differ, then asserted |
| parse scope | 110 draco + 37 mods |
| surface losses | 0 across 147 types |
| tests | 974 + 8 parser checks = 982 / 77 |

After it the example inventory is empty. Deferred activation and the .dcon migration remain, none accepted. The handoff records this as raised and unaccepted. Chapter 113 is unwritten.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Close the example map, nine companions and four ConsumeReport trios, and make it a gate?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 3436, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-10-31T15:02:39.566Z; source line 3446; extract record 57*

Accepted, build it

---

**[Claude]** *2026-10-31T15:12:29.124Z; source line 3811; extract record 58*

**Built as accepted, direct run green, ready for your gate.**

**What is in the tree.** The four ConsumeReport surfaces are the emitter's rendering, placed under each medium's resources, and parsing them back re-canonicalized their JSON: the action strings became call trees, aerial's `ctx.getRuntime().get("consumed")` a nested one, and the aspect keys took the canonical order. The Scala generated from either JSON is identical, and aerial ConsumeReport's companion regenerates byte for byte. The nine companions are the generator's output, equal to the probe's. Drift check is clean over all five media. No engine change, no new suite.

**The test.** `ExampleDomainsGenTest` now asserts zero differ, zero error and zero missing over the 48 example definitions, keeping its per-type log and its console line. Its row leaves the DRACO.md table as the third measurement to become an assertion.

**One correction on the way.** The first direct run failed one test, the all-48 resolution in ExampleLoadingTest. With the actions treed, name resolution reads `domains.terrestrial.TerrestrialSink.record` and knew only AerialSink, the one sink the example caller bound on October 23. The caller helper now binds all four sinks by qualified name, the same decision extended, and the loading test checks each is a binding rather than a member. The second run was green. The record names this as the first run's failure.

| headline | second direct run |
|---|---|
| tests | 982 passed, 78 suites including discovery, 0 failed or aborted |
| example map | 48 match, 0 differ, 0 error, 0 missing, asserted |
| surface losses | 0 across 147 types |
| parse scope | 110 draco + 37 mods |
| pending mods actors | 0 (file-only) |

Every other headline is unchanged. The record is `git-record-2026-10-31-1105`; CHANGELOG, DRACO.md, README, handoff and resume prompt are worded with your gate pending, and the four earlier hash-fold edits ride along.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:" ; grep -c "pending .drake authoring (0)" target/test-output/DrakeGenTest.log
```

Expected: 982 succeeded, zero failed, canceled, ignored or pending, 77 suites, the headlines above, and a trailing `1`. Paste the output and I will switch the wording to verified and hand over the commit block.

---

*(Delegated action — Dev ran the full sbt gate: 982 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 147 types; Drake.parse scope: 110 draco + 37 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. The trailing count of the zero-pending line printed 1. Source: 2026-10-31T16:25:04.761Z; source line 3815; extract record 59. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-31T16:27:09.723Z; source line 3930; extract record 60*

**Gate verified, wording switched, commit block ready.**

Your run matches the direct run line for line: 982 succeeded, zero failed, canceled, ignored or pending, the example map 48 match / 0 differ asserted, surface losses 0 across 147 types, parse scope 110 + 37, every other headline unchanged, and the trailing 1 for pending mods actors. First full Dev gate green.

The record `git-record-2026-10-31-1105`, DRACO.md (baseline paragraph in the standard verified form, increment heading "Dev verified 982 / 77"), README, the handoff (Start-here, section heading, ledger row) and the resume prompt now say verified by Dev on 2026-10-31 with the commit pending. The link checker is clean. The IDE has already staged the five new files, so the `git add` in the block is a no-op there.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-10-31-1105 src/mods/resources/domains/aerial/ConsumeReport.drake src/mods/resources/domains/ethereal/ConsumeReport.drake src/mods/resources/domains/marine/ConsumeReport.drake src/mods/resources/domains/terrestrial/ConsumeReport.drake && git commit -F draco-git-record/git-record-2026-10-31-1105 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md draco-git-record/git-record-2026-10-31-1105 src/mods/resources/domains/aerial/ConsumeReport.drake src/mods/resources/domains/aerial/ConsumeReport.json src/mods/resources/domains/ethereal/ConsumeReport.drake src/mods/resources/domains/ethereal/ConsumeReport.json src/mods/resources/domains/marine/ConsumeReport.drake src/mods/resources/domains/marine/ConsumeReport.json src/mods/resources/domains/terrestrial/ConsumeReport.drake src/mods/resources/domains/terrestrial/ConsumeReport.json src/mods/scala/domains/aerial/Aerial.scala src/mods/scala/domains/aerial/Input.scala src/mods/scala/domains/ethereal/ConsumeReport.scala src/mods/scala/domains/ethereal/Ethereal.scala src/mods/scala/domains/marine/ConsumeReport.scala src/mods/scala/domains/marine/Marine.scala src/mods/scala/domains/terrestrial/ConsumeReport.scala src/mods/scala/domains/terrestrial/Terrestrial.scala src/mods/scala/domains/world/World.scala src/test/scala/domains/ExampleDomainsGenTest.scala src/test/scala/domains/ExampleLoadingTest.scala src/test/scala/domains/ExampleScope.scala && git push && git log -1 --oneline && git status --short
```

Paste the `git log -1 --oneline` line and the `git status --short` output (expected empty). I will fold the hash into DRACO.md, README, the handoff ledger, the resume prompt and memory, and then bring the next related point: with the example inventory empty, what remains is the deferred activation recommendation, the deferred .dcon migration, and the observation that the example bodies are unswept.

---

*(Delegated action — Dev pasted the command line as run, without its output (git add, git commit, git push, git log, git status). Source: 2026-10-31T16:28:17.767Z; source line 3934; extract record 61. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-31T16:35:44.014Z; source line 4172; extract record 62*

**7be1497 is committed and pushed, and the next related point is measured.**

Your commit landed as 7be1497 with the 26 files, the tree is clean and local main is level with origin. The hash is folded into DRACO.md, README, the handoff ledger and the resume prompt. Those four edits are uncommitted and ride with the next increment, as before.

**The next related point: sweep the example bodies.** The October 21 sweep compacted the canonical corpus and the staged dreams and service definitions with their records and left the example domains out. With every example definition now a trio or a generator-backed JSON-only type, and the map a gate, the examples are the one part of the corpus still spelled as authored, and the asymmetry the sweep removed elsewhere remains here: an unswept file gains its imports only under a scope.

I measured it with a probe over the compiled classes, compacting all 48 example definitions through the example caller scope.

| measurement | result |
|---|---|
| definitions that change and gain a record | 12 of 48 |
| which | the four Creators, the four OriginateReports, aerial Input, terrestrial Output, world Consumer and Provider |
| what shortens | `Dracon`, `World`, `Position`, `Location`, and the factory's `ActorType`, as the dreams Editor and Initialization records already carry it |
| drake surfaces re-emitted | 12, each with a `uses` section; every one parses back to its compact JSON |
| Scala regenerated | 12 files, 4 to 14 lines each: selective imports gained, qualified spellings shortened; unscoped and scoped generation identical for all |
| other 36 definitions | unchanged |

**One engine consequence, found by the probe.** The assembly validator compares a consumer parameter's message type with the actor's by text. Over the swept Creator, whose parameter reads `Dracon -> ()`, and the world actors, whose `World -> ()` is own-package bare and stays bare in the recorded scope, the Aerial and World assembly tests would fail with "expects a consumer of Dracon but receives draco.format.dracon.Dracon". This is the third consumer of the type vocabulary again, the same place the unit type caught on October 21. The validator should resolve the inner name through the from-definition's recorded scope, complete a bare result with the definition's own package, and compare by identity.

**Recommendation.** Sweep the twelve example definitions that compact changes: JSON compacted with their records, the twelve drake surfaces re-emitted, the twelve Scala files regenerated and pinned by the example gate, and the assembly validator comparing a consumer's message type by identity through the definition's record instead of by text, with one assembly test over a compact definition. No other engine change. Expected: no new suite, the two assembly suites green, every headline unchanged, since no surface is added. Accept or correct.

**Observed, not recommended.** Five sentient definitions, Direction, Ego, Lean, Percept and Waypoint, carry a tuple value type as the string `"(Double, Double)"`, the form before type-form trees of September 17, and they alone fail the drake round-trip. Nothing re-canonicalized them because the sentient domain has no drake surfaces at all. Their JSON does not change under the sweep, so this is separate.

The handoff and resume prompt now carry this recommendation as raised and unaccepted, with the probe figures and the sentient observation.

---

## Status

Committed and pushed by Dev after each gate: 3a3cbfc (967 tests), 23333f3 (974 tests, example map 39 / 9) and 7be1497 (982 tests, the example map asserted at 48 / 0 and off the DRACO.md table). The first direct run of the third increment failed ExampleLoadingTest until the example caller bound the four sinks by qualified name. For 7be1497 Dev pasted the command line without its output; the reply that follows read the commit from the tree. Open at the close and NOT accepted: the example body sweep with the assembly validator comparing by identity; observed, the five sentient tuple value types still in the pre-tree string form.
