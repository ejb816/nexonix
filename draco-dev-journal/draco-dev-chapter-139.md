# Chapter 139 - Drake's Failure Form Is Data

**Session date:** November 14, 2026
**Topic:** One increment under GitHub #11: `gendrake.RefusalOf` returns the three codec refusals as Problems and the emitter raises the first. Dev's gate passes 1280 tests, and Dev asks for the journal before the commit.

**Provenance:** Original Claude Code session `92b66875-d6a1-4bde-bc3d-a3186065293e`, extract records 17–21; see the [bounded extract](sources/claude-2026-11-14-session-2-continued.jsonl) and [retrieval audit](sources/claude-2026-11-14-session-2-continued-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Accept refusals as Problems from a definition, starting with the three codec guards?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 4506, outside the reply text, and is not an extract record. A compaction boundary is at source line 4521, directly after this message; the reply is an original record from after the boundary.)*

**[Dev]** *2026-11-14T17:53:41.432Z; source line 4513; extract record 17*

Accepted, build it

---

**[Claude]** *2026-11-14T17:59:33.524Z; source line 4709; extract record 18*

The failure form is built and ready for your gate. My direct run of every discovered suite passed 1280 tests / 93 suites including discovery on its first run; nothing is committed.

### What was built

- **New trio `gendrake.RefusalOf`**: takes a definition and returns `problems [Problem]`. It reports a codec discriminator, a duplicate payload field and a payload field that is not a name, in the emitter's order and with its messages. GenDrake lists it, so its trio moves too.
- **`Drake.emit`**: raises the first problem's message; the three hand-written guards are gone. The parser's own payload checks are untouched.
- **`RefusalOfTest`** (6 tests, the 92nd suite), including a corpus walk asserting no committed definition is refused.

### Two things that differ from the recommendation

- **Exception text and kind.** The two payload guards were `require`s, so their messages carried the host prefix "requirement failed: ". They now carry the Problem's message alone, and every refusal is raised as an `IllegalArgumentException`. No existing test needed changing.
- **A binder name.** A lambda binder called `message` does not parse (the parser reads it as the actor section's keyword), so it is `reason`. I added this to DRACO.md §6.

The pattern match I had not probed generated as written.

### Headlines to expect

`Tests: succeeded 1280` (1269, plus five per-type gates and six in the new suite). These move by the one corpus type:

| headline | expected |
|---|---|
| parse scope | 129 draco + 73 mods, 0 held back |
| surface losses | 0 across 202 types |
| DomainAspect / DracoAspect | 129 |
| GenDrake | 127 of 127 |
| Uses | 95 records |
| Definition | 202 surfaces from 542 sections |
| Element | 1120 elements (1367 lines) |
| Expression | 666 values (489 trees, 177 leaves) |
| Slot | 1103 slots (77 arrows) |
| TypeForm | 2148 forms (474 trees, 1674 leaves) |
| type leaves | 1674 |
| host text | 21 of 2844, 0 outside the seams |
| **new:** Refusal | 202 definitions (4 carrying a payload), 0 refused |

Unchanged: rule 24, actor 15, codec 4, Formal 36, GEN MAP 48 / 0, scenario 23 clean, PON 42 and 7, the rete finding. `DrakeCLI check` reads drift 0 over main and mods.

### The gate

Please paste the output; the grep now includes `Refusal over`.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|host text|type leaves|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|RuleAspect over|ActorAspect over|Uses over|CodecAspect over|Definition over|TypeForm over|Expression over|Element over|Formal over|Slot over|Refusal over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

After a green gate I will record it and hand over the commit and push blocks. The record is `draco-git-record/git-record-2026-11-14-1258`; it also folds `0b53e39` into the shared documents. Nothing on the tracker was touched.

The journal is current through chapter 136. This session has compacted again, so the exchanges since "Journal first" (the `73f77c8`, `ddaec87`, `0b53e39` increments and this one) are uncovered. Say so if you want chapters written before this commit.

---

*(Delegated action — Dev ran the full sbt gate: 1280 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 129 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 127 of 127 definitions emitted, 0 missing, 0 not in the corpus; Element over the corpus: 1120 elements reproduced in 202 definitions (1367 lines), 0 differing; Expression over the corpus: 666 values reproduced in 202 definitions (489 trees, 177 leaves), 0 differing; Definition over the corpus: 202 surfaces reproduced whole from 542 sections, 0 skipped; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; RuleAspect over the corpus: 202 definitions, 24 rule sections reproduced, 0 skipped; ActorAspect over the corpus: 202 definitions, 15 actor sections reproduced, 0 skipped; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 202 types; Drake.parse scope: 129 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; host text in values: 21 leaves of 2844 in 11 definitions, 0 outside the 11 named seams; DracoAspect over the corpus: 129 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; type leaves: 1674 in 202 definitions, 0 carrying host type syntax; Slot over the corpus: 1103 value-type slots reproduced in 202 definitions (77 arrows parenthesized), 0 differing; Refusal over the corpus: 202 definitions (4 carrying a payload), 0 refused; Uses over the corpus: 202 definitions, 95 records reproduced, 0 skipped; CodecAspect over the corpus: 202 definitions, 4 codec sections reproduced, 0 skipped; the forest runs: 1 of 2 ash alarms crossed to birch; Formal over the corpus: 36 formals reproduced in 202 definitions (33 lists, 7 bounded), 0 differing; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; TypeForm over the corpus: 2148 type forms reproduced in 202 definitions (474 trees, 1674 leaves), 0 differing. Source: 2026-11-15T02:09:06.539Z; source line 4713; extract record 19. The full pasted text is retained in the extract.)*

---

*(Context, not dialogue — a compaction boundary is at source line 4727, between Dev's gate paste and the reply below, which is an original record from after it.)*

**[Claude]** *2026-11-15T02:09:48.787Z; source line 4799; extract record 20*

Your gate matches my direct run on every line: 1280 tests, zero failed, and the new `Refusal over the corpus: 202 definitions (4 carrying a payload), 0 refused`. I recorded it in the git record, DRACO.md (§1 baseline now 1280 tests / 92 suites, the §5 line and the closing paragraph), the handoff and the resume prompt.

The commit covers 14 files, which is everything `git status` shows:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md draco-git-record/git-record-2026-11-14-1258 src/main/resources/draco/gendrake/GenDrake.drake src/main/resources/draco/gendrake/GenDrake.json src/main/resources/draco/gendrake/RefusalOf.drake src/main/resources/draco/gendrake/RefusalOf.json src/main/scala/draco/gendrake/GenDrake.scala src/main/scala/draco/gendrake/RefusalOf.scala src/mods/scala/draco/Drake.scala src/test/scala/draco/gendrake/RefusalOfTest.scala) && git add -- "${paths[@]}" && git commit -F draco-git-record/git-record-2026-11-14-1258 -- "${paths[@]}" && git status --short && git log --oneline -1
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git push origin main
```

Please paste the output of both; I need the hash to fold into the next record.

The journal still ends at chapter 136. The session has compacted since, and four increments are uncovered (`73f77c8`, `ddaec87`, `0b53e39` and this one). If you choose the journal first, hold the commit block: the chapters would go into this same commit and the path list would grow.

After the commit I will bring the next point under #11: the element refusals as Problems.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Write journal chapters 137 onward before you commit?” with the buttons “Commit first”, “Journal first” (a tool call at source line 4796, not an extract record). The message below is the open turn at the extract's boundary: it asked for this journal update, and its reply is outside the frozen prefix. The commit and push blocks of the reply above were held.)*

**[Dev]** *2026-11-15T02:13:45.402Z; source line 4809; extract record 21*

Journal first

---

## Status

Built, verified and uncommitted at the chapter's close: `gendrake.RefusalOf` (factory `par definition TypeDefinition`, one element `problems [Problem]`: a discriminator, a duplicate payload field, a payload field that is not a name, in the emitter's order and with its messages), listed by GenDrake; `Drake.emit` raising the first problem's message, its three hand-written guards removed; RefusalOfTest's six tests. Dev's gate passed 1280 tests, every headline matching the direct run and the new `Refusal over the corpus: 202 definitions (4 carrying a payload), 0 refused` line as expected, green on its first run; the direct run was green on its first run. Two things differ from the recommendation: the two payload refusals lose the host prefix “requirement failed: ” and every refusal is raised as an IllegalArgumentException, no existing test changing; and a lambda binder named `message` does not parse, so it is `reason`. The record is `git-record-2026-11-14-1258`, which also folds 0b53e39; the commit and push blocks handed over in the chapter's last reply were held when Dev answered “Journal first”, the open turn at the extract's boundary, so that these chapters ride in the same commit. Two compaction boundaries fall in this chapter, each directly after a Dev message, with original records on both sides. Next on the thread, not recommended in detail and not accepted: the element refusals as Problems. Nothing on the tracker was touched; #11 stays open. Activation and .dcon are deferred; PON is suspended.
