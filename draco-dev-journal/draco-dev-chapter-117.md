# Chapter 117 - The Anchor-Parameter Rule and the Compact Fixpoint

**Session date:** November 1, 2026
**Topic:** An own-package anchor parameter is written bare only when the body-leaf rule would shorten it; the whole corpus is asserted a fixpoint of the compact form.

**Provenance:** Original Claude Code session `f12792d4-b3c1-4ac3-b8f9-c2853199c07f`, extract records 77–88; see the [bounded extract](sources/claude-2026-10-26-to-11-03.jsonl) and [retrieval audit](sources/claude-2026-10-26-to-11-03-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the uniform anchor-parameter rule as recommended?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 5559, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-01T13:29:52.555Z; source line 5576; extract record 77*

Accepted, build it

---

**[Claude]** *2026-11-01T13:35:33.907Z; source line 5768; extract record 78*

**Built as accepted, and the direct suite is green on its first run.**

The change is one conjunct in NameResolution.scala: an own-package anchor parameter is written bare only when `short` would shorten it, so a non-unique simple name stays in full as a body leaf does. ResolutionRecordTest gains one case over the example caller scope. It pins Percept's and Waypoint's tuple as `(Gaze, domains.sentient.Distance)`, resolves each to itself, compacts each to the committed JSON, reads the compact form back, and checks that unscoped and scoped generation agree with the committed companion. It also pins Ego's and TypeDictionary's unique own-package parameters still bare. No definition and no committed Scala changed.

Direct run of every discovered suite on compiled classes with current main, mods and test resources on the loader roots:

| measure | result |
|---|---|
| tests | 1006 passed, 0 failed, canceled, ignored or pending |
| suites | 78 including discovery, 0 aborted |
| parse scope | 110 draco + 48 mods, 0 held back |
| surface losses | 0 across 158 types |
| example gate | 48 match / 0 differ / 0 error / 0 missing |
| pending mods actors | 0 |

Every other headline is unchanged. The record `git-record-2026-11-01-0831`, the CHANGELOG entry, DRACO.md, the handoff and the resume prompt describe the increment with the gate pending; the three documents also carry the 5f0e023 fold from the last commit. Nothing is committed.

The gate block. Paste the grep output and the final count line:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:" ; grep -c "pending .drake authoring (0)" target/test-output/DrakeGenTest.log
```

Expected: 1006 tests / 77 suites, zero failed, the headlines in the table above, and a trailing `1` from the file-only count. The commit block follows your green paste. It will `git add` the new record.

After the commit nothing from the sentient thread remains preserved. The one observation left in the example tree is EgoActor.scala, a hand-written actor over Ego with no definition, unmeasured and not recommended. Activation and the .dcon migration stay deferred.

---

*(Delegated action — Dev ran the full sbt gate: 1006 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 158 types; Drake.parse scope: 110 draco + 48 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. The trailing count of the zero-pending line printed 1. Source: 2026-11-01T14:24:36.717Z; source line 5772; extract record 79. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-01T14:25:26.173Z; source line 5809; extract record 80*

**Your gate matches the expected tree line for line.** 1006 succeeded, zero failed, canceled, ignored or pending. Surface losses 0 across 158 types, parse scope 110 + 48, the example gate 48 match / 0 differ, every other headline unchanged, and the file-only pending-actor count 1. The record, DRACO.md, DEVELOPMENT_HANDOFF.md, the resume prompt and memory now read "verified by Dev on 2026-11-01, 1006 tests / 77 suites, commit pending". The link checker is clean. Nothing is committed.

The commit block. The record is new, so it is added first; the commit is path-scoped to the resolver, the test, the record and the five documents, which also carry the 5f0e023 fold:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-11-01-0831 && git commit -F draco-git-record/git-record-2026-11-01-0831 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- src/mods/scala/draco/NameResolution.scala src/test/scala/draco/ResolutionRecordTest.scala draco-git-record/git-record-2026-11-01-0831 CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md && git push && git log -1 --oneline && git status --short
```

Paste the whole output, as last time. Expect seven files changed, one created, and an empty status.

After your paste I fold the hash into the documents for the next commit. With that, nothing from the sentient thread remains preserved, and the example tree holds one observation only: EgoActor.scala, a hand-written actor over Ego with no definition, unmeasured and not recommended. Activation and the .dcon migration stay deferred. I will bring the next point as one recommendation with accept-or-correct once the commit is in.

---

*(Delegated action — Dev committed ed57aa9, “Make an anchor parameter's bare form take the body leaf's uniqueness rule” (7 files changed, 179 insertions(+), 29 deletions(-)). Pushed to origin/main: 5f0e023..ed57aa9. Source: 2026-11-01T16:27:38.574Z; source line 5813; extract record 81. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-01T16:31:17.793Z; source line 6000; extract record 82*

**ed57aa9 is committed and pushed, and the hash is folded.** DRACO.md's baseline lead and tail paragraph, DEVELOPMENT_HANDOFF.md (start-here chain, latest-gate bullet, state bullet, section heading, evidence paragraph, ledger row, next design point), DEVELOPMENT_RESUME_PROMPT.md and auto-memory now say "committed and pushed by Dev as ed57aa9". The link checker is clean. The tree holds only that fold, three documents, which rides with the next commit.

**The next related point, measured first.** The ed57aa9 record said the increment's gate did not meet the defect because no test compacts the example definitions and the loader never re-reads its result. That is still the shape of the suite. ResolutionRecordTest pins unscoped equals scoped generation over the main corpus, and now the compact round trip of Percept, Waypoint, Ego and TypeDictionary by name. Nothing asserts the claim the two sweeps made, that every definition in the corpus IS the compact form. I ran that claim on the compiled classes over every definition under src/main/resources and src/mods/resources, the canonical scope for draco and the example caller scope for domains:

| measure | result |
|---|---|
| definitions | 183 |
| compact(td) encodes to td (fixpoint) | 183 |
| resolve(compact(td)) equals resolve(td) (reads back) | 183 |

True today, asserted nowhere as a whole. Had it been asserted, the day Percept and Waypoint took `domains.sentient.Distance` in full it would have failed on those two, which is exactly the inconsistency ed57aa9 closed.

**Recommendation.** Move the measurement into the assertion: one new ResolutionRecordTest case walking every definition under the two resource roots (excluding the nameless anchor `.json`), choosing the scope by package, asserting both rows above for each, and the count at 183 so a definition cannot drop out silently. No engine change, no definition or Scala change, no new suite. Expected gate 1007 tests / 77 suites, every headline unchanged. DRACO.md's stated goal applies: a measurement becomes an assertion, and the sweeps' "every definition is compact" stops being a sentence in a record.

**Still an observation, not recommended.** EgoActor.scala is referenced nowhere but a test comment and a review ledger row. It dates from chapter 23 in June, a stub modeled on NaturalActor, with no test, no assembly, no definition, and a name carrying the retired Actor suffix. Whether it takes a role-named definition or leaves the tree is a decision for you, and I will not bring it until this point is settled.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the corpus-wide compact fixpoint assertion in ResolutionRecordTest?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 5992, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-01T17:23:41.641Z; source line 6004; extract record 83*

Accepted, build it

---

**[Claude]** *2026-11-01T17:29:12.244Z; source line 6207; extract record 84*

**Built as accepted, and the direct run is green.** ResolutionRecordTest has a sixth case: it walks every definition under both resource roots, skipping only the nameless domain's anchor, picks the canonical scope for draco and the example caller's scope for domains, and asserts for each that the compact form is the definition itself and that the compact form reads back to the same resolution. The count is bounded below at 183, as the suite's whole-corpus generation case bounds the main corpus at 110, so a definition cannot drop out silently while an added type does not fail the gate. No engine line, no definition, no committed Scala, no new suite.

Verification, both on compiled classes with current main, mods and test resources on the loader roots:

| run | result |
|---|---|
| focused ResolutionRecordTest | 6 of 6, green first attempt |
| every discovered suite | 1007 tests / 78 suites including discovery, zero failed or aborted, green first attempt |

Every headline is unchanged: parse scope 110 + 48, surface losses 0 across 158 types, example gate 48 / 0, GenDrake 108 of 108, aspects 110, PON 80 / 550 / 42 and 7, scenario 23 clean, forest 1 of 2, CO-DECLARATION categorical, pending mods actors 0. The record is draco-git-record/git-record-2026-11-01-1228; the CHANGELOG entry, DRACO.md (baseline lead and tail paragraph), DEVELOPMENT_HANDOFF.md, the resume prompt and memory say "Dev's gate pending, expected 1007 / 77". The documents also carry the ed57aa9 fold. Link checker clean. Nothing committed.

The gate block. Expect 1007 succeeded, 0 failed, every headline as above, and a trailing 1 from the file-only count:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:" ; grep -c "pending .drake authoring (0)" target/test-output/DrakeGenTest.log
```

Paste the whole output. On a green gate I reword the record and documents to "verified by Dev" and hand over the commit block, which adds the new record and is path-scoped to the test, the record and the five documents.

---

*(Delegated action — Dev ran the full sbt gate: 1007 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 158 types; Drake.parse scope: 110 draco + 48 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. The trailing count of the zero-pending line printed 1. Source: 2026-11-01T19:32:40.776Z; source line 6211; extract record 85. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-01T19:34:02.587Z; source line 6310; extract record 86*

**Gate green, as predicted.** 1007 succeeded, 0 failed, canceled, ignored or pending, every headline matching the DRACO.md table, the file-only count 1. First full Dev gate green on this tree.

The record and documents now say so. The record's verification paragraph ends with your gate; DRACO.md's baseline lead reads 1007 tests / 77 suites verified by Dev on 2026-11-01, commit pending, with 1006 / 77 as "before it"; the tail paragraph's heading is "Dev verified 1007 / 77". The handoff's latest-gate bullet, section heading, state bullet, next design point, evidence paragraph and ledger row are reworded the same way, and the resume prompt says "GATED BY ME, COMMIT PENDING". Memory updated. No pending-gate phrase remains in any of them, and the link checker is clean. Nothing is committed.

The commit block. The record shows as staged and modified in your IDE, so the `git add` re-stages its final text. Path-scoped to the test, the record and the five documents, which also carry the ed57aa9 fold. Expect six files changed, one created, then an empty status:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-11-01-1228 && git commit -F draco-git-record/git-record-2026-11-01-1228 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- src/test/scala/draco/ResolutionRecordTest.scala draco-git-record/git-record-2026-11-01-1228 CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md && git push && git log -1 --oneline && git status --short
```

Paste the whole output. On your paste I fold the hash into the documents and memory, then measure the next related point before bringing one recommendation.

---

*(Delegated action — Dev committed a31e49b, “Assert the whole corpus is a fixpoint of the compact form” (6 files changed, 164 insertions(+), 20 deletions(-)). Pushed to origin/main: ed57aa9..a31e49b. Source: 2026-11-01T19:35:35.573Z; source line 6314; extract record 87. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-01T19:40:06.574Z; source line 6482; extract record 88*

**a31e49b folded.** DRACO.md's baseline lead and tail paragraph, the handoff's start-here chain, latest-gate bullet, state bullet, section heading, next design point, evidence paragraph and ledger row, the resume prompt and memory all name the commit. The tree holds only that fold, which rides with the next commit. Nothing from the sentient, anchor-parameter or compact-fixpoint threads remains preserved.

**The next related point, measured before recommending.** The fixpoint test's 183 definitions are 110 main, 25 staged draco and 48 example. Exactly one of them has no drake surface: the staged `draco.format.yaml.Yaml`, a two-aspect anchor kept when YAML authoring was retired, referenced nowhere outside its own trio. It is invisible to every gate for one reason, written in DrakeGenTest itself: the mods gate pins emit against the authored surface only for ACTOR definitions, and the pending headline counts only actors, the comment calling the plain and rule mods types "a later pass". That pass never came. So today the emit direction of 59 mods surfaces, the sentient eleven and the five domain objects among them, is pinned by nothing but the hand-run `DrakeCLI check`, the same shape of gap the fixpoint assertion closed yesterday.

Probe over all 73 mods definitions with the compiled classes:

| measure | result |
|---|---|
| definitions with a .drake | 72 of 73 (13 actors) |
| emit equals the surface, byte for byte | 72 of 72 |
| surface parses back to the committed JSON | 72 of 72 |
| without a .drake | Yaml alone |
| Yaml: the surface the emitter renders | `type Yaml from draco format Format(String)` and the domain line, XML's form; parses back to the committed JSON; Yaml.scala regenerates byte-identical |

**Recommendation.** Close the later pass DrakeGenTest named: widen its mods gate from the actors under domains to every definition under `src/mods/resources` with an authored surface, exact match whitespace-normalized as the main walk already does, and make the headline count every mods definition lacking a surface, its table row becoming "mods definitions pending .drake"; and give Yaml the two-line surface above so that count stays 0. Expected: 59 added tests, no new suite, 1066 tests / 77 suites; the parse scope stays 110 + 48, since DrakeParseTest's mods scope is the example tree and widening it is a point of its own; no engine, JSON or Scala change. Flagged, not recommended: removing Yaml instead, YAML authoring being retired and the domain unreferenced. That is your decision, and the surface preserves the definition either way.

---

## Status

ed57aa9 (one conjunct in NameResolution, 1006 tests) and a31e49b (ResolutionRecordTest's corpus-wide fixpoint case, 1007 tests) are committed and pushed by Dev. No definition or committed Scala moved in either. Open at the close and NOT accepted: widening DrakeGenTest's mods gate to every mods surface and giving Yaml its surface.
