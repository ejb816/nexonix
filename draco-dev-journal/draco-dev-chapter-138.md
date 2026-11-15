# Chapter 138 - The Emitter Refuses Once, Ahead of Rendering

**Session date:** November 14, 2026
**Topic:** One increment under GitHub #11: the emitter walks a definition's rendered elements through its refusals before any section renders, and the section transforms render their own elements through ElementOf (0b53e39). A failure form that is data is recommended.

**Provenance:** Original Claude Code session `92b66875-d6a1-4bde-bc3d-a3186065293e`, extract records 11–16; see the [bounded extract](sources/claude-2026-11-14-session-2-continued.jsonl) and [retrieval audit](sources/claude-2026-11-14-session-2-continued-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Accept moving the element refusals ahead of rendering and dropping elementText?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 4355, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-14T14:00:03.768Z; source line 4362; extract record 11*

Accepted, build it

---

**[Claude]** *2026-11-14T14:04:56.356Z; source line 4460; extract record 12*

The increment is built and passes my direct run: **1269 tests / 92 suites including discovery**, zero failed, canceled, ignored, pending or aborted, green on its first run. Your gate has not been run, and nothing is committed.

### What was built

- **`Drake.scala`** gains `refuseDefinition(td)`, which walks every element the surface renders through the existing `refuseElement`. The four section entry points call it before delegating, and `emit` reaches it through `definitionText` after its three codec guards. `elementLines` is removed.
- **`DracoAspectOf`, `RuleAspectOf`, `ActorAspectOf`** lose the `elementText` parameter and bind it themselves as `ElementOf(element, level).lines`.
- **`DefinitionOf`** is `DefinitionOf(definition)`; its `uses` record loses `draco TypeElement`.
- **Tests:** two restated (DracoAspectTest, SlotOfTest), none added, so the test count does not move.

The walk judges only what the surface renders: a factory's parameters and body only when the factory is present, a rule's or actor's elements only when that role is present. So the same definitions are refused as before. `DrakeCLI check` reads drift 0 over main and mods, and the four trios are stable on regeneration.

`Drake.emit` is now its three guards, the refusal walk and one call. Nothing is passed to the definitions but the definition.

### Headlines to expect

Tests should read 1269 succeeded, 0 failed, canceled, ignored or pending. Six headlines move, because five elements net left the corpus (four parameters and DefinitionOf's binding):

| headline | last gate | expected |
|---|---|---|
| Element | 1117 elements (1364 lines) | 1112 elements (1359 lines) |
| Expression | 661 values (483 trees, 178 leaves) | 660 values (486 trees, 174 leaves) |
| Slot | 1099 slots (81 arrows) | 1094 slots (76 arrows) |
| TypeForm | 2161 forms (483 trees, 1678 leaves) | 2131 forms (468 trees, 1663 leaves) |
| type leaves | 1678 | 1663 |
| host text | 21 leaves of 2807 | 21 leaves of 2818 |

Everything else should match your last gate: parse scope 128 + 73 with 0 held back, losses 0 across 201 types, both aspect probes 128, GenDrake 126 of 126, Formal 36, Definition 201 surfaces from 539 sections, GEN MAP 48 / 0, scenario 23 clean, PON 42 and 7, the rete line.

### Gate

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|host text|type leaves|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|RuleAspect over|ActorAspect over|Uses over|CodecAspect over|Definition over|TypeForm over|Expression over|Element over|Formal over|Slot over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Please paste the whole grep output. Once it is green I will record it and hand over the commit block (21 paths, record `git-record-2026-11-14-0904`, the fold of `ddaec87` included) and the push block.

Nothing on the tracker was touched. The journal is current through chapter 136.

---

*(Delegated action — Dev ran the full sbt gate: 1269 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 128 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 126 of 126 definitions emitted, 0 missing, 0 not in the corpus; Element over the corpus: 1112 elements reproduced in 201 definitions (1359 lines), 0 differing; Expression over the corpus: 660 values reproduced in 201 definitions (486 trees, 174 leaves), 0 differing; Definition over the corpus: 201 surfaces reproduced whole from 539 sections, 0 skipped; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; RuleAspect over the corpus: 201 definitions, 24 rule sections reproduced, 0 skipped; ActorAspect over the corpus: 201 definitions, 15 actor sections reproduced, 0 skipped; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 201 types; Drake.parse scope: 128 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; host text in values: 21 leaves of 2818 in 11 definitions, 0 outside the 11 named seams; DracoAspect over the corpus: 128 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; type leaves: 1663 in 201 definitions, 0 carrying host type syntax; Slot over the corpus: 1094 value-type slots reproduced in 201 definitions (76 arrows parenthesized), 0 differing; Uses over the corpus: 201 definitions, 94 records reproduced, 0 skipped; CodecAspect over the corpus: 201 definitions, 4 codec sections reproduced, 0 skipped; the forest runs: 1 of 2 ash alarms crossed to birch; Formal over the corpus: 36 formals reproduced in 201 definitions (33 lists, 7 bounded), 0 differing; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; TypeForm over the corpus: 2131 type forms reproduced in 201 definitions (468 trees, 1663 leaves), 0 differing. Source: 2026-11-14T14:15:20.439Z; source line 4470; extract record 13. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-14T14:15:41.484Z; source line 4480; extract record 14*

Your gate is green and matches my direct run on every headline, including the six that moved: 1269 tests, zero failed, canceled, ignored or pending. I recorded it in the git record, DRACO.md, the handoff and the resume prompt; the baseline stays 1269 tests / 91 suites. Nothing is committed or pushed.

### Commit

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md draco-git-record/git-record-2026-11-14-0904 src/main/resources/draco/gendrake/ActorAspectOf.drake src/main/resources/draco/gendrake/ActorAspectOf.json src/main/resources/draco/gendrake/DefinitionOf.drake src/main/resources/draco/gendrake/DefinitionOf.json src/main/resources/draco/gendrake/DracoAspectOf.drake src/main/resources/draco/gendrake/DracoAspectOf.json src/main/resources/draco/gendrake/RuleAspectOf.drake src/main/resources/draco/gendrake/RuleAspectOf.json src/main/scala/draco/gendrake/ActorAspectOf.scala src/main/scala/draco/gendrake/DefinitionOf.scala src/main/scala/draco/gendrake/DracoAspectOf.scala src/main/scala/draco/gendrake/RuleAspectOf.scala src/mods/scala/draco/Drake.scala src/test/scala/draco/draketarget/DracoAspectTest.scala src/test/scala/draco/gendrake/SlotOfTest.scala) && git add -- "${paths[@]}" && git commit -F draco-git-record/git-record-2026-11-14-0904 -- "${paths[@]}" && git status --short
```

That is 21 paths; `git status --short` should print nothing afterwards.

### Push

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git push origin main && git status -sb | head -1
```

Please paste both outputs. After that I will bring the next point under GitHub #11. Nothing on the tracker was touched, and the journal is current through chapter 136.

---

*(Delegated action — Dev committed 0b53e39, “The drake emitter refuses once, ahead of rendering; the section transforms render their own elements (GitHub #11)” (21 files changed, 250 insertions(+), 181 deletions(-)). Pushed to origin/main: ddaec87..0b53e39. Status line `## main...origin/main`. Source: 2026-11-14T14:16:48.666Z; source line 4490; extract record 15. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-14T14:17:14.672Z; source line 4509; extract record 16*

`0b53e39` is committed and pushed (`ddaec87..0b53e39`, 21 files, tree level with origin). Its hash folds into the shared documents with the next commit.

### Next point under GitHub #11: a failure form

**What I measured.** What is still hand-written on the emit side of `Drake.scala` is only refusal:

- the three codec guards in `emit` (a discriminator, a duplicate payload field, an invalid payload field);
- `refuseDefinition` / `refuseElement` (a kind with no keyword, invalid method formals, a block carrying a value outside its body);
- `refuseUnrenderable` (a value outside the tree language).

They stayed because drake has no way for a definition to raise. But draco already has a way to *report*: `draco.Problem` (a subject `TypeName` and a message), which the five validation rules produce.

**Recommendation.** Make the failure form data. A definition returns the problems, and the emitter raises when there are any.

- New trio `gendrake.RefusalOf`: factory `par definition TypeDefinition`, one element `problems [Problem]`.
- This increment carries only the three codec guards, with the messages they have today, so the form is settled on the smallest case.
- `Drake.emit` reads `RefusalOf(td).problems` and raises the first message; the three guards leave the emitter.
- The element refusals follow in later increments, each a point of its own.

**Not probed.** The invalid-field check is a pattern match on a text (`matches("[A-Za-z_][A-Za-z0-9_]*")`). I expect it to pass through as a host method call, as `split("[.]")` does in `TypeDefinition`, but I have not run it through the generator.

**Rejected.** A `raise` construct in drake: it adds a language feature that Haskell would model as a result type anyway. Also `Presence(Text)` per refusal: it carries no subject and cannot report more than one.

One corpus type would be added, so the parse scope, loss, aspect and GenDrake counts each move by one, and the test count by the five per-type gates plus a small focused suite.

---

## Status

0b53e39 (`refuseDefinition` in Drake.scala called by the four section entry points; DracoAspectOf, RuleAspectOf and ActorAspectOf binding `elementText` themselves and DefinitionOf taking the definition alone; `elementLines` removed; 21 files) is committed and pushed by Dev after a gate of 1269 tests, every headline matching the direct run, green on its first run; the direct run was green on its first run. No test or corpus type was added. The one difference is when a malformed element is refused: before any section renders. No rendering dependency is passed to a definition any more. The baseline stays 1269 tests. No compaction boundary falls in this chapter. Recommended in the chapter's last reply and not yet accepted at its close: a failure form that is data, `gendrake.RefusalOf` returning Problems, starting with the three codec guards. Nothing on the tracker was touched; #11 stays open. Activation and .dcon are deferred; PON is suspended.
