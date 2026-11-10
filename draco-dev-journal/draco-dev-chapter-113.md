# Chapter 113 - The Named-Construction Head, the Dyn Result and the Head-Keyed Lookup

**Session date:** October 26–29, 2026
**Topic:** Claude Code receives the Codex handoff at 7495539 and builds three accepted named-construction repairs in the generator; Dev corrects a command block that omitted the change of directory and asks whether pasting only the filtered gate output is enough.

**Provenance:** Original Claude Code session `f12792d4-b3c1-4ac3-b8f9-c2853199c07f`, extract records 1–26; see the [bounded extract](sources/claude-2026-10-26-to-11-03.jsonl) and [retrieval audit](sources/claude-2026-10-26-to-11-03-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

**[Dev]** *2026-10-26T18:17:50.241Z; source line 3; extract record 1*

```
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
```

---

**[Claude]** *2026-10-26T18:23:04.058Z; source line 197; extract record 2*

**Handoff received.** Branch main, HEAD 7495539 (the documentation commit after a8b5447), tree clean, both agent symlinks intact. Dev's last verified gate is 933 tests / 71 suites on the Consumer tree. Journal and sync markers reach chapter 112; the audit's extract covers both compactions. No discrepancy between the handoff and the tree.

**Codex's reading is verified.** `constructionInitializer` in [DracoGenerator.scala:270](../src/mods/scala/draco/DracoGenerator.scala) spells a named-argument construction as the slot type's last segment. I authored a Consumer variant with `domains.terrestrial.Location(latitude:…, longitude:…, elevationMetres:…)`, parsed and generated it with the compiled engine, and compiled the output:

```text
val location: domains.terrestrial.Location = Location(_latitude = …, _longitude = …, _elevationMetres = …)
error: not found: value Location
```

The positional path renders the head verbatim, which is why the accepted Consumer compiles.

**Corpus impact, measured.** Only three definitions carry a named argument, all examples. Input constructs its own package's Position, so stripping is harmless there. Output is not intercepted at all, since LocationReport has no factory, and falls to the unmapped `name = v` rendering it already differs by. Observable's construction sits in a dyn body, which renders through `expression` with no `_` mapping, so its author spelled `_geocentric` by hand. The main and staged corpus has zero named-argument nodes. Either repair below leaves every committed Scala file byte-identical.

**One recommendation.** Spell the constructed type from the application's head as authored and resolved, exactly as the positional path does, instead of from the slot type's last segment. Keep the `_name` parameter mapping and the factory check unchanged. Add one focused test that authors a cross-package named construction from a package with no masking import, generates it unscoped and under the example scope, compiles the result with the runtime compiler and constructs a Location through it. Consumer's positional call stays as accepted.

Two observations surfaced only for the record, not recommended now: the method keys its factory lookup on the slot type rather than the head, and a dyn-body result never maps named parameters to the factory's `_` convention.

---

**[Dev]** *2026-10-26T18:27:38.611Z; source line 201; extract record 3*

Accepted: spell the named-construction head as authored/resolved, same as the positional path, with one cross-package compile-and-run test checking scoped and unscoped generation. Build it.

---

**[Claude]** *2026-10-26T18:36:53.349Z; source line 468; extract record 4*

**Built as accepted.** Two files changed, nothing else touched:

- [DracoGenerator.scala:260](../src/mods/scala/draco/DracoGenerator.scala) — `constructionInitializer` now spells the applied head through `expression(operands.head)`, the same rule the positional path uses, instead of the slot type's last segment. The factory check and the `_name` parameter mapping are unchanged.
- [NamedConstructionTest.scala](../src/test/scala/domains/world/NamedConstructionTest.scala) — a new suite of three tests. A probe authored in domains.world constructs `domains.terrestrial.Location` with named arguments. Unscoped, the generated file keeps the qualified head and only imports `draco._` and `domains._`. Scoped through the example scope, the engine shortens it to `Location` and adds `import domains.terrestrial.Location`. Both generations are compiled with the runtime compiler and run, and the constructed Location carries the three authored values. The third test pins an own-package head authored bare staying bare, as aerial/Input spells its Position.

**Corpus impact, measured.** Old and new engine produce byte-identical Scala for Input, Output, Observable and Consumer, so no committed file regenerates. Consumer's positional call is unchanged.

**Direct verification.** All main, mods and test sources compiled into fresh class roots with main, mods and test resources overlaid and the hidden anchors removed. Every discovered suite ran:

| headline | result |
|---|---|
| tests / suites | 936 passed / 73 including discovery, zero failed, canceled, ignored, pending or aborted |
| example gen map | 36 match, 12 differ, 0 error, 0 missing |
| surface losses | 0 across 133 types |
| parse scope | 110 draco + 23 mods |
| pending mods actors (file-only) | 0 |

PON, canonical, scenario, forest, both aspect probes, GenDrake 108/108 and the subtype-visibility line all match the table. The tree after the run holds only the two intended changes.

Full gate, from the project root:

```bash
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:|^\[info\] All tests|Total number"
```

Then the file-only line:

```bash
grep "pending" target/test-output/DrakeGenTest.log
```

Expected: 936 tests / 72 suites in sbt's count. Once the gate is green I'll write the git record, CHANGELOG entry, DRACO.md baseline and handoff refresh, and hand over the path-scoped commit.

---

**[Dev]** *2026-10-26T18:46:13.822Z; source line 472; extract record 5*

You left out the change directory to project command. I frequently open a new terminal either within or between task-sessions:

*(Delegated action, pasted with the prompt above — Dev pasted the gate command as run from the home directory, where sbt refused it: “Neither build.sbt nor a 'project' directory in the current directory: /Users/edwardburke”. Source: 2026-10-26T18:46:13.822Z; source line 472; extract record 5. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-26T18:46:36.618Z; source line 494; extract record 6*

My mistake. The gate, self-contained:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:|^\[info\] All tests|Total number"
```

Then the file-only line:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && grep "pending" target/test-output/DrakeGenTest.log
```

Expected: 936 tests / 72 suites, zero failed, canceled, ignored, pending or aborted, pending mods actors 0. Paste both outputs and I'll write the record, CHANGELOG, DRACO.md and handoff, then hand over the commit.

---

*(Delegated action — Dev ran the full sbt gate: 936 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 36 match, 12 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 133 types; Drake.parse scope: 110 draco + 23 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; mods actors pending .drake authoring (0):. Source: 2026-10-26T18:48:43.783Z; source line 498; extract record 7. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-26T18:52:31.286Z; source line 618; extract record 8*

**Gate confirmed.** Your run reports 936 tests passed with zero failed, canceled, ignored or pending, and every report-only headline matches the table, including pending mods actors 0 in the file-only log. That is 72 suites in sbt's count, the 71 before plus NamedConstructionTest.

**Documentation written for this commit:**

- `draco-git-record/git-record-2026-10-26-1449` is the record and the commit message. Its subject is "Spell a named construction's head as authored, as the positional path does".
- `CHANGELOG.md` gains one entry under Unreleased.
- `DRACO.md` carries the new 936 / 72 baseline, the Consumer paragraph points at the repair, and a new increment paragraph records the accepted words, the probe, the measured corpus impact and the two adjacent observations as not accepted.
- `README.md` updates its two checkpoint passages. `DEVELOPMENT_HANDOFF.md` and `DEVELOPMENT_RESUME_PROMPT.md` are refreshed, with the two observations as the next design point and chapter 113 marked pending.

The test file is already staged by the IDE. The record is untracked, so the block adds it first and then scopes the commit itself to the eight paths:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-10-26-1449 src/test/scala/domains/world/NamedConstructionTest.scala && git commit -F draco-git-record/git-record-2026-10-26-1449 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- src/mods/scala/draco/DracoGenerator.scala src/test/scala/domains/world/NamedConstructionTest.scala draco-git-record/git-record-2026-10-26-1449 CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md && git push && git log -1 --oneline
```

Once it lands I'll bring the next related point: the dyn-body result rendering that forces Observable to author `_geocentric` by hand.

---

*(Delegated action — Dev committed e241378, “Spell a named construction's head as authored, as the positional path does” (8 files changed, 250 insertions(+), 71 deletions(-)). Pushed to origin/main: 7495539..e241378. Source: 2026-10-26T19:06:39.512Z; source line 622; extract record 9. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-26T19:07:46.061Z; source line 659; extract record 10*

**Landed.** e241378 is on main and origin/main with the eight files, and the working tree is clean. The handoff's ledger still reads "pending" for that row; I'll carry the hash into it with the next commit rather than spend a documentation-only commit on one cell.

**Next related point: a dyn's result bypasses the named-construction interception.** Every `fix` in a body renders through the initializer, which is where a named construction gets its `_name` mapping. The result of a dyn does not. A body's `fix value` and a leaf dyn's value both render through plain `expression`, so a named argument reaches Scala verbatim. The corpus shows the consequence in one place: Observable's `fromGeodetic` authors `_geocentric:` by hand, a Scala spelling inside a drake source, because that is the only way its named construction compiles today.

**Measured.** The corpus has 86 `fix value` results. Exactly one is a named construction, Observable's. Six are Text-typed with non-literal values, and four of those are plain paths such as `rendered.value`. That rules out routing the result through the full initializer, whose String-slot rule would quote those paths as text. The safe route is the narrow one: the named-construction interception alone, with today's `expression` as the fallback.

**One recommendation.** Render a dyn's result, body `fix value` and leaf alike, through `constructionInitializer` with the existing `expression` fallback. Re-author Observable's `_geocentric:` as `geocentric:` and re-canonicalize its JSON. Its Scala stays byte-identical, since the mapping now supplies the underscore, so no other committed file moves. Add one case to NamedConstructionTest whose probe constructs through a dyn result and is compiled and run like the others.

The second observation, that the interception keys its factory lookup on the slot type rather than the applied head, stays preserved and unaccepted until this one is settled.

---

**[Dev]** *2026-10-26T19:09:51.819Z; source line 663; extract record 11*

Accepted: route a dyn's result (body `fix value` and leaf) through constructionInitializer with the existing expression fallback, re-author Observable's `_geocentric:` as `geocentric:` with its JSON re-canonicalized and Scala byte-identical, and add one NamedConstructionTest case. Build it.

---

**[Claude]** *2026-10-26T19:17:27.479Z; source line 849; extract record 12*

**Built as accepted, with one enabling change I need to flag.** Four files changed:

- [DracoGenerator.scala](../src/mods/scala/draco/DracoGenerator.scala) — a new `dynResult` helper renders a dyn's result, body `fix value` and leaf alike, through `constructionInitializer` with the existing `expression` fallback, at all five result sites. The String-slot quoting rule of the full initializer is deliberately not applied, since four corpus results are Text-typed paths it would have quoted.
- **The enabling change.** The first probe left Observable unmapped, because `loadDracoType` looked up only a DOTTED slot type and Observable's result is `fix value Observable`. Input had worked only because it spells its slot type qualified. The generator now keeps a definition-in-effect, a lazy thread-local set while a definition is spelled, and a bare slot type resolves against that definition's package, exactly as the codec judge resolves a bare parameter type. Without it, Observable's Scala could not have stayed byte-identical with `geocentric:` authored; with it, unscoped and scoped generation agree, since scoped spelling already qualified such names.
- [Observable.drake](../src/mods/resources/domains/world/Observable.drake) authors `geocentric:`; its JSON was re-canonicalized by the compiled parser and differs in that one leaf only. Observable.scala regenerates byte-identical under the new engine, as do Input, Output and Consumer.
- [NamedConstructionTest.scala](../src/test/scala/domains/world/NamedConstructionTest.scala) gains the accepted case: a probe whose named construction is a body result and a leaf result, both generated, compiled and run. The own-package case now authors its slot type bare, pinning the resolution above.

**Direct verification.** Fresh class roots, resources overlaid, hidden anchors removed, every discovered suite:

| headline | result |
|---|---|
| tests / suites | 937 passed / 73 including discovery, zero failed, canceled, ignored, pending or aborted |
| example gen map | 36 match, 12 differ |
| surface losses | 0 across 133 types |
| parse scope | 110 draco + 23 mods |
| pending mods actors (file-only) | 0 |

All other headlines match the table. The full gate:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:|^\[info\] All tests|Total number"
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && grep "pending" target/test-output/DrakeGenTest.log
```

Expected: 937 tests, 72 suites, pending mods actors 0. On green I'll write the record, CHANGELOG, DRACO.md and handoff, carry e241378 into the ledger, and hand over the commit.

---

*(Delegated action — Dev ran the full sbt gate: 937 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 36 match, 12 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 133 types; Drake.parse scope: 110 draco + 23 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; mods actors pending .drake authoring (0):. Source: 2026-10-26T19:33:51.224Z; source line 853; extract record 13. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-26T19:35:47.000Z; source line 923; extract record 14*

**Gate confirmed.** 937 tests passed with zero failed, canceled, ignored or pending, every headline matching the table, and pending mods actors 0 in the file-only log. Suites stay at 72, since the new case joined the existing suite.

**Documentation for this commit:**

- `draco-git-record/git-record-2026-10-26-1534` is the record and the commit message. It states the accepted words, the measurement that ruled out the full initializer, and the enabling definition-in-effect change as a flagged addition to the accepted scope.
- `CHANGELOG.md` gains one entry. `DRACO.md` carries the new 937 / 72 baseline, marks the previous increment as committed at e241378, and adds the increment paragraph. `README.md` updates its two checkpoint lines.
- `DEVELOPMENT_HANDOFF.md` records both increments, carries e241378 into the ledger with this one as pending, and leaves the slot-type observation as the single unaccepted next point. `DEVELOPMENT_RESUME_PROMPT.md` matches.

The record is untracked, so the block adds it and scopes the commit to the ten paths:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-10-26-1534 && git commit -F draco-git-record/git-record-2026-10-26-1534 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- src/mods/scala/draco/DracoGenerator.scala src/test/scala/domains/world/NamedConstructionTest.scala src/mods/resources/domains/world/Observable.drake src/mods/resources/domains/world/Observable.json draco-git-record/git-record-2026-10-26-1534 CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md && git push && git log -1 --oneline
```

Once it lands, the next related point is the last observation from this work: the interception keys its factory lookup on the slot type rather than the applied head.

---

**[Dev]** *2026-10-26T19:42:04.566Z; source line 927; extract record 15*

Before this commit, I'm noticing your split between running sbt test and the command to grep the output from it. Are you indicating that not pasting the sbt test output and only pasting the grep out is enough, and will save on the model usage limits if the sbt test direct output is not copy/pasted into my response prompt?

---

**[Claude]** *2026-10-26T19:42:19.889Z; source line 933; extract record 16*

Yes, that's the intent. The first command already runs the full suite and filters its output through the grep; what you paste back is exactly what I need to verify the gate. The full output goes to `/tmp/sbt-test.log` through the `tee`, so nothing is lost. If a run ever fails, I'll ask for specific lines from that file rather than the whole thing.

The second command is separate because the pending-mods-actors headline is never printed to the console. It goes only to `target/test-output/DrakeGenTest.log`, so no console grep can see it. DRACO.md's §1 table marks that row file-only for this reason.

On usage: a full `sbt test` run prints thousands of lines, and the two grep outputs together are about twenty. Pasting only the grep output is both sufficient and the cheaper choice.

The commit block from my previous message still stands, unchanged.

---

*(Delegated action — Dev committed 9779e39, “Intercept a dyn's result as a named construction, and resolve a bare slot type” (10 files changed, 196 insertions(+), 59 deletions(-)). Pushed to origin/main: e241378..9779e39. Source: 2026-10-26T19:43:49.561Z; source line 937; extract record 17. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-26T19:44:46.456Z; source line 969; extract record 18*

**Landed.** 9779e39 is on main and origin/main with the ten files, and the tree is clean. The handoff ledger shows this increment as pending; I'll carry its hash in with the next commit, as before.

**Next related point: the interception keys its factory lookup on the slot type, not the applied head.** `constructionInitializer` decides whether a named construction is a factory call by loading the SLOT type and checking for a factory. The thing being constructed is the HEAD. The two coincide in every corpus site today, which is why nothing has shown it. A probe shows what happens when they differ:

```text
dyn make domains.terrestrial.Terrestrial domains.terrestrial.Location(latitude:1.5, longitude:-2.5, elevationMetres:7)
```

generates

```text
def make: domains.terrestrial.Terrestrial = domains.terrestrial.Location(latitude = 1.5, longitude = -2.5, elevationMetres = 7)
```

Terrestrial is a domain with no factory, so the lookup says "not a construction" and the arguments reach Scala unmapped. Location's factory takes `_latitude`, so this does not compile. Typing a slot by a parent and constructing a child is an ordinary thing to author, and the engine should not depend on the two names agreeing.

**One recommendation.** Key the lookup on the applied head: dotted as spelled, bare resolved against the definition in effect, exactly as the slot type is resolved now. The slot type then plays no part in the decision, which is what the positional path already does. Corpus impact is nil by construction, since in all three named sites the head names the same definition the slot does, so Input, Output and Observable regenerate byte-identical. One NamedConstructionTest case authors the parent-typed slot above, generated, compiled and run, with the result checked to be a Location.

---

**[Dev]** *2026-10-27T20:54:34.486Z; source line 977; extract record 19*

Accepted: key constructionInitializer's factory lookup on the applied head (dotted as spelled, bare against the definition in effect) instead of the slot type, and add one NamedConstructionTest case with a parent-typed slot, compiled and run. Build it.

---

**[Claude]** *2026-10-27T20:58:46.812Z; source line 1089; extract record 20*

**Built as accepted.** Two files changed:

- [DracoGenerator.scala](../src/mods/scala/draco/DracoGenerator.scala) — `constructionInitializer` now takes only the value and asks `loadDracoType` about the applied head, a name leaf that is dotted as spelled or bare against the definition in effect. The slot type no longer enters the decision, so `dynResult` and `initializer` pass only the value. Everything else in the interception is unchanged.
- [NamedConstructionTest.scala](../src/test/scala/domains/world/NamedConstructionTest.scala) — the accepted fifth case authors a slot typed by the parent domain Terrestrial constructing a Location with named arguments. The generation is asserted to map the arguments, compiled with the runtime compiler, run, and the result checked to be a Location carrying the authored values.

**Corpus impact.** Input, Output and Observable generate byte-identical to the previous engine, and Observable and Consumer to their committed Scala. The parent-slot probe that rendered unmapped `latitude = ` yesterday now renders `_latitude = `.

**Direct verification.** Fresh class roots, resources overlaid, hidden anchors removed, every discovered suite:

| headline | result |
|---|---|
| tests / suites | 938 passed / 73 including discovery, zero failed, canceled, ignored, pending or aborted |
| example gen map | 36 match, 12 differ |
| surface losses | 0 across 133 types |
| parse scope | 110 draco + 23 mods |
| pending mods actors (file-only) | 0 |

All other headlines match the table. The gate:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:|^\[info\] All tests|Total number"
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && grep "pending" target/test-output/DrakeGenTest.log
```

Expected: 938 tests, 72 suites, pending mods actors 0. On green I'll write the record and documentation, carry 9779e39 into the ledger, and hand over the commit. This closes the last observation from the named-construction work, so after that commit the next point will come from the handoff's remaining inventory rather than from this thread.

---

*(Delegated action — Reported lines: mods actors pending .drake authoring (0):. Source: 2026-10-29T11:52:51.663Z; source line 1095; extract record 21. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-29T11:53:06.647Z; source line 1116; extract record 22*

The file-only line is 0, and its 07:51 timestamp shows a run completed this morning. I don't have that run's gate output, though: only the second command's result was pasted. Before I write the record and documentation I need the first command's grep lines, the test count and the headlines, since the record states what your gate reported rather than what my direct run did.

If the output is no longer in your terminal, it is in the tee'd log:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:|^\[info\] All tests|Total number" /tmp/sbt-test.log
```

Expected: 938 tests, zero failed, every headline unchanged.

---

*(Delegated action — Dev ran the full sbt gate: 938 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 36 match, 12 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 133 types; Drake.parse scope: 110 draco + 23 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. Source: 2026-10-29T11:54:51.924Z; source line 1120; extract record 23. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-29T11:56:35.451Z; source line 1188; extract record 24*

**Gate confirmed.** 938 tests passed, zero failed, canceled, ignored or pending, every headline matching the table, pending mods actors 0. Suites stay at 72.

**Documentation for this commit:**

- `draco-git-record/git-record-2026-10-29-0755` is the record and the commit message. It names the probe, the accepted words, the nil corpus impact, and records that the three October 26 observations are now all closed.
- `CHANGELOG.md` gains one entry. `DRACO.md` carries the 938 / 72 baseline, marks the dyn-result increment as committed at 9779e39, and adds the increment paragraph. `README.md` updates its two checkpoint lines.
- `DEVELOPMENT_HANDOFF.md` carries 9779e39 into the ledger with this increment as pending. Its next-design-point section now says no recommendation is preserved and points at the remaining inventory: the twelve example differences, deferred activation, and .dcon migration. `DEVELOPMENT_RESUME_PROMPT.md` matches and notes chapter 113 still unwritten.

The record is untracked, so the block adds it and scopes the commit to the eight paths:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-10-29-0755 && git commit -F draco-git-record/git-record-2026-10-29-0755 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- src/mods/scala/draco/DracoGenerator.scala src/test/scala/domains/world/NamedConstructionTest.scala draco-git-record/git-record-2026-10-29-0755 CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md && git push && git log -1 --oneline
```

Once it lands, the named-construction thread is complete. The next point will come from the handoff's inventory, and the one difference known to be more than spelling is terrestrial/Output, whose authored projection still lacks the message binder and dispatch its handwritten Scala carries.

---

*(Delegated action — Dev committed 112f277, “Key a named construction's factory lookup on the applied head, not the slot type” (8 files changed, 168 insertions(+), 67 deletions(-)). Pushed to origin/main: 9779e39..112f277. Source: 2026-10-29T19:07:18.617Z; source line 1192; extract record 25. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-10-29T19:10:41.326Z; source line 1385; extract record 26*

**112f277 is on origin**, and the three named-construction observations of October 26 are all closed. `DEVELOPMENT_HANDOFF.md` and `DEVELOPMENT_RESUME_PROMPT.md` now name that hash in place of "pending". Those two edits stay in the tree to ride with the next commit.

**Next point: terrestrial Output.** Its authored projection is the one example difference the audit found to be more than spelling, and I verified the reading against the current engine.

Output's drake message body is three statements with no dispatch:

```text
message
  fix payload io.circe.Json io.circe.Json.obj(… location.latitude …)
  fix report domains.terrestrial.LocationReport domains.terrestrial.LocationReport(typeDefinition:LocationReport.typeDefinition, json:payload)
  mon consumer(report)
```

Two defects follow. The body reads `location` that nothing binds, because the hand-written Scala's `case location: Location =>` and its catch-all were never carried into the definition. And the report is a named construction of a type with no factory, so the engine passes it through unmapped and Scala has no `LocationReport.apply` to meet it. The generated file cannot compile either way. Terrestrial's own OriginateReport rule, repaired on October 24, constructs the same report as `new LocationReport { … }` host text, and world's Consumer, repaired on October 25, carries exactly this shape of dispatch as a `case` branch.

**Recommendation:** re-author Output's message as the Consumer-style branch, with the report constructed as OriginateReport does:

```text
message
  case location domains.terrestrial.Location [
    now fix payload io.circe.Json io.circe.Json.obj(…)
    now fix report LocationReport new LocationReport { override lazy val typeDefinition: TypeDefinition = LocationReport.typeDefinition; override lazy val json: io.circe.Json = payload }
    mon consumer(report)
  ]
```

Then regenerate `Output.scala` from the definition and add one `OutputProjectionTest` on the ConsumerProjectionTest pattern: exact drake, JSON and Scala agreement with the World actor role; one report per Location carrying type, latitude, longitude and elevationMetres; non-Location messages ignored before and after; the consumer captured lazily, once. The example map should move from 36 to 37 match. No engine change and no other definition touched.

**Adjacent observation, not recommended now:** LocationReport has no drake surface and no factory, so every construction of it is host text. A `json` factory on LocationReport would let Output and OriginateReport both write `LocationReport(json:payload)` through the named-construction path just repaired. That would change LocationReport's committed Scala and OriginateReport's, so it is a separate point.

---

## Status

Dev accepted three points separately and each was built, gated by Dev's full sbt run and committed and pushed by Dev: e241378 (the named-construction head spelled as authored, 936 tests), 9779e39 (a dyn's result intercepted as a named construction, 937 tests) and 112f277 (the factory lookup keyed on the applied head, 938 tests, gated on October 29). The named-construction thread is closed. Two practices date from this chapter: every handed-over command block begins with the change of directory, and Dev pastes the filtered gate output only. Open at the close and NOT accepted: carrying terrestrial Output's Location branch in its definition; observed beside it, a `json` factory on LocationReport. Activation and .dcon remain deferred.
