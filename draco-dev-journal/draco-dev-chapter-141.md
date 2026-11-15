# Chapter 141 - The Block-Value Refusal, and What Dracon Must Read

**Session date:** November 14, 2026
**Topic:** Two increments under GitHub #11: a block carrying a value outside its body is a Problem of `gendrake.ElementRefusalOf` (6e475be), and Dracon gains `keys` and `compact`, the two inspections the value-tree refusal needs (101ea48). The value-tree refusal as a definition is recommended.

**Provenance:** Original Claude Code session `92b66875-d6a1-4bde-bc3d-a3186065293e`, extract records 11–22; see the [bounded extract](sources/claude-2026-11-14-to-15-session-2-continued.jsonl) and [retrieval audit](sources/claude-2026-11-14-to-15-session-2-continued-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Accept the block-value refusal as a Problem in ElementRefusalOf?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 5202, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-15T02:33:14.934Z; source line 5209; extract record 11*

Accepted, build it

---

**[Claude]** *2026-11-15T02:37:58.741Z; source line 5287; extract record 12*

The block-value refusal is built and ready for your gate. My direct run of every discovered suite passed 1292 tests / 94 suites including discovery on its first run; nothing is committed.

### What was built

- **`gendrake.ElementRefusalOf`** now reports the keyword problem, then the block-value problem, then recurses. A dyn with parameters or a body, or a case-branch, whose own value spells to a non-empty text is one Problem with the emitter's message, unchanged.
- **`Drake.scala`**: `refuseElement` keeps only the formals check and the value-tree check. The two block checks and the now-unused `opensBlock` are removed.
- **`ElementRefusalOfTest`**: one test added, and the emitter test restated to pin the block refusal by exact message.

### As recommended, one behavior difference

The block-value refusal is judged over the whole definition before the two that remain, and is raised as an `IllegalArgumentException`. No test outside the suite changed.

The em dash and backticks in the quoted message, which I had not probed, are carried as written.

### Headlines to expect

`Tests: succeeded 1292` (1291 plus the one added test). No corpus type is added and there is no new grep line. These move, by the definition's six added lines:

| headline | expected |
|---|---|
| Element | 1147 elements (1394 lines) |
| Expression | 688 values (506 trees, 182 leaves) |
| Slot | 1131 slots (81 arrows) |
| TypeForm | 2196 forms (490 trees, 1706 leaves) |
| type leaves | 1706 |
| host text | 21 of 2937, 0 outside the seams |

Unchanged: parse scope 130 + 73, losses 0 across 203 types, both aspect probes 130, GenDrake 128 of 128, Uses 96, Definition 203 from 545, Refusal 203 with 0 refused, rule 24, actor 15, codec 4, Formal 36, GEN MAP 48 / 0, scenario 23 clean, PON 42 and 7, the rete finding. `DrakeCLI check` reads drift 0 over main and mods, and the trio is stable on regeneration.

### The gate

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|host text|type leaves|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|RuleAspect over|ActorAspect over|Uses over|CodecAspect over|Definition over|TypeForm over|Expression over|Element over|Formal over|Slot over|Refusal over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

After a green gate I will record it and hand over the commit and push blocks. The record is `draco-git-record/git-record-2026-11-14-2137`; it also folds `05aa585` into the shared documents. Nothing on the tracker was touched.

---

*(Delegated action — Dev ran the full sbt gate: 1292 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 130 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 128 of 128 definitions emitted, 0 missing, 0 not in the corpus; Element over the corpus: 1147 elements reproduced in 203 definitions (1394 lines), 0 differing; Expression over the corpus: 688 values reproduced in 203 definitions (506 trees, 182 leaves), 0 differing; Definition over the corpus: 203 surfaces reproduced whole from 545 sections, 0 skipped; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; RuleAspect over the corpus: 203 definitions, 24 rule sections reproduced, 0 skipped; ActorAspect over the corpus: 203 definitions, 15 actor sections reproduced, 0 skipped; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 203 types; Drake.parse scope: 130 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; host text in values: 21 leaves of 2937 in 11 definitions, 0 outside the 11 named seams; DracoAspect over the corpus: 130 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; type leaves: 1706 in 203 definitions, 0 carrying host type syntax; Slot over the corpus: 1131 value-type slots reproduced in 203 definitions (81 arrows parenthesized), 0 differing; Refusal over the corpus: 203 definitions (4 carrying a payload), 0 refused; Uses over the corpus: 203 definitions, 96 records reproduced, 0 skipped; CodecAspect over the corpus: 203 definitions, 4 codec sections reproduced, 0 skipped; the forest runs: 1 of 2 ash alarms crossed to birch; Formal over the corpus: 36 formals reproduced in 203 definitions (33 lists, 7 bounded), 0 differing; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; TypeForm over the corpus: 2196 type forms reproduced in 203 definitions (490 trees, 1706 leaves), 0 differing. Source: 2026-11-15T02:41:06.755Z; source line 5291; extract record 13. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-15T02:41:25.483Z; source line 5310; extract record 14*

Your gate matches my direct run on every line: 1292 tests, zero failed. I recorded it in the git record, DRACO.md (§1 baseline now 1292 tests / 93 suites, the §5 line and the closing paragraph), the handoff and the resume prompt.

The commit covers 11 files, which is everything `git status` shows:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md draco-git-record/git-record-2026-11-14-2137 src/main/resources/draco/gendrake/ElementRefusalOf.drake src/main/resources/draco/gendrake/ElementRefusalOf.json src/main/scala/draco/gendrake/ElementRefusalOf.scala src/mods/scala/draco/Drake.scala src/test/scala/draco/gendrake/ElementRefusalOfTest.scala) && git add -- "${paths[@]}" && git commit -F draco-git-record/git-record-2026-11-14-2137 -- "${paths[@]}" && git status --short && git log --oneline -1
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git push origin main
```

Please paste the output of both; I need the hash to fold into the next record.

The journal is current through chapter 139; the last two increments (`05aa585` and this one) are not yet covered. After the commit I will bring the next point under #11, one of the two element refusals still hand-written.

---

*(Delegated action — Dev committed 6e475be, “The block-value refusal is data: gendrake.ElementRefusalOf returns a block carrying a value outside its body as a Problem (GitHub #11)” (11 files changed, 331 insertions(+), 40 deletions(-)). `git log --oneline -1`: 6e475be (HEAD -> main) The block-value refusal is data: gendrake.ElementRefusalOf returns a block carrying a value outside its body as a Problem (GitHub #11). Pushed to origin/main: 05aa585..6e475be. Source: 2026-11-15T02:43:45.124Z; source line 5314; extract record 15. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-15T02:44:12.224Z; source line 5345; extract record 16*

`6e475be` is committed and pushed (`05aa585..6e475be`, 11 files, tree clean). Its hash folds into the shared documents with the next commit.

### Next point under GitHub #11: what the value-tree refusal needs from Dracon

**What I measured.** Two element refusals are still hand-written, and neither can cross as it stands.

- **A value outside the tree language** (`refuseUnrenderable`). Its two messages name the unknown operator and print the whole value: `unknown operator '<:' in {"<:":["a","b"]}` and `unrenderable value 1`. It also refuses a node with more than one key. `Dracon` cannot say any of that: it offers `has(name)`, `field(name)`, `isObject` and the text and list readers, but no way to read a record's own keys and no compact text of a value. `ExpressionOf` works around this by trying the known keys, which finds a known operator but cannot name an unknown one.
- **Invalid method formals** (`MethodTypeParameters.validate`). It is shared by three callers: the emitter, the parser, and the Scala generator. Moving it is a question of where a check shared across targets lives, which is larger than this thread.

**Recommendation.** Add two inspections to `draco.format.dracon.Dracon`, as an increment of their own, so the value-tree refusal can follow with its messages unchanged.

- `dyn keys [Text]`: the field names of a record in their order, empty for anything else.
- `dyn compact Text`: the value's compact text.
- Tests in `DraconTest`. No consumer changes in this increment.

**What would move.** `Dracon` is one of the eleven named seams in `HostTextTest`, so its recorded host-text count would change with the two new bodies, and I would update that count in the same commit. The Dracon trio regenerates; no other definition should change.

**Not probed.** Whether the two bodies generate as written; both read the wrapped Json directly, as `isObject` does.

**Rejected.** Changing the messages so they need neither (an unknown operator could no longer be named). Also building the two inspections and the refusal together: the vocabulary is a seam change and deserves its own gate.

**Deferred, not recommended now.** The formals check, for the reason above.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Accept adding keys and compact text to Dracon as the next increment?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 5342, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-15T02:46:52.759Z; source line 5349; extract record 17*

Accepted, build it

---

**[Claude]** *2026-11-15T02:50:41.996Z; source line 5412; extract record 18*

The two Dracon inspections are built and ready for your gate. My direct run of every discovered suite passed 1293 tests / 94 suites including discovery on its first run; nothing is committed.

### What was built

- **`draco.format.dracon.Dracon`** gains two elements:
  - `dyn keys [Text]`: a record's field names in their written order, empty for anything else.
  - `dyn compact Text`: the value's compact text.
- The trio was re-authored in drake, parsed and regenerated; the Scala gains two `def`s on the trait and nothing else moves.
- **`DraconTest`**: one test added, including an unknown operator named through `keys` and printed through `compact`.
- No consumer changed; nothing reads the two yet.

### One thing I got wrong in the recommendation

I said Dracon's recorded host-text count in `HostTextTest` would move. It does not: neither body is host syntax, so Dracon stays at 5 leaves and the list at 21 in 11 definitions.

Both bodies generated as written.

### Headlines to expect

`Tests: succeeded 1293` (1292 plus the one added test). No corpus type is added and there is no new grep line. These move, by Dracon's two added lines:

| headline | expected |
|---|---|
| Element | 1149 elements (1396 lines) |
| Expression | 690 values (507 trees, 183 leaves) |
| Slot | 1133 slots (81 arrows) |
| TypeForm | 2199 forms (491 trees, 1708 leaves) |
| type leaves | 1708 |
| host text | 21 of 2941, 0 outside the seams |

Every other headline is unchanged. `DrakeCLI check` reads drift 0 over main and mods, and the trio is stable on regeneration.

### The gate

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|host text|type leaves|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|RuleAspect over|ActorAspect over|Uses over|CodecAspect over|Definition over|TypeForm over|Expression over|Element over|Formal over|Slot over|Refusal over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

After a green gate I will record it and hand over the commit and push blocks. The record is `draco-git-record/git-record-2026-11-14-2149`; it also folds `6e475be` into the shared documents. Nothing on the tracker was touched.

---

*(Delegated action — Dev ran the full sbt gate: 1293 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 130 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 128 of 128 definitions emitted, 0 missing, 0 not in the corpus; Element over the corpus: 1149 elements reproduced in 203 definitions (1396 lines), 0 differing; Expression over the corpus: 690 values reproduced in 203 definitions (507 trees, 183 leaves), 0 differing; Definition over the corpus: 203 surfaces reproduced whole from 545 sections, 0 skipped; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; RuleAspect over the corpus: 203 definitions, 24 rule sections reproduced, 0 skipped; ActorAspect over the corpus: 203 definitions, 15 actor sections reproduced, 0 skipped; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 203 types; Drake.parse scope: 130 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; host text in values: 21 leaves of 2941 in 11 definitions, 0 outside the 11 named seams; DracoAspect over the corpus: 130 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; type leaves: 1708 in 203 definitions, 0 carrying host type syntax; Slot over the corpus: 1133 value-type slots reproduced in 203 definitions (81 arrows parenthesized), 0 differing; Refusal over the corpus: 203 definitions (4 carrying a payload), 0 refused; Uses over the corpus: 203 definitions, 96 records reproduced, 0 skipped; CodecAspect over the corpus: 203 definitions, 4 codec sections reproduced, 0 skipped; the forest runs: 1 of 2 ash alarms crossed to birch; Formal over the corpus: 36 formals reproduced in 203 definitions (33 lists, 7 bounded), 0 differing; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; TypeForm over the corpus: 2199 type forms reproduced in 203 definitions (491 trees, 1708 leaves), 0 differing. Source: 2026-11-15T02:54:10.507Z; source line 5416; extract record 19. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-15T02:54:34.751Z; source line 5435; extract record 20*

Your gate matches my direct run on every line: 1293 tests, zero failed. I recorded it in the git record, DRACO.md (§1 baseline now 1293 tests / 93 suites, the §5 line and the closing paragraph), the handoff and the resume prompt.

The commit covers 9 files, which is everything `git status` shows:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md draco-git-record/git-record-2026-11-14-2149 src/main/resources/draco/format/dracon/Dracon.drake src/main/resources/draco/format/dracon/Dracon.json src/main/scala/draco/format/dracon/Dracon.scala src/test/scala/draco/format/dracon/DraconTest.scala) && git add -- "${paths[@]}" && git commit -F draco-git-record/git-record-2026-11-14-2149 -- "${paths[@]}" && git status --short && git log --oneline -1
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git push origin main
```

Please paste the output of both; I need the hash to fold into the next record.

The journal is current through chapter 139; the last three increments (`05aa585`, `6e475be` and this one) are not yet covered. After the commit I will bring the next point under #11: the value-tree refusal as Problems, reading the two new inspections.

---

*(Delegated action — Dev committed 101ea48, “Dracon reads a record's keys and a value's compact text: the two inspections the value-tree refusal needs (GitHub #11)” (9 files changed, 143 insertions(+), 6 deletions(-)). `git log --oneline -1`: 101ea48 (HEAD -> main) Dracon reads a record's keys and a value's compact text: the two inspections the value-tree refusal needs (GitHub #11). Pushed to origin/main: 6e475be..101ea48. Source: 2026-11-15T02:56:30.498Z; source line 5439; extract record 21. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-15T02:56:51.289Z; source line 5452; extract record 22*

`101ea48` is committed and pushed (`6e475be..101ea48`, 9 files, tree clean). Its hash folds into the shared documents with the next commit.

### Next point under GitHub #11: the value-tree refusal as a definition

**What I measured.** `refuseUnrenderable` in `Drake.scala` judges one value tree and has two callers:

- `refuseElement`, for each rendered element's value, where the definition's name is known;
- `Drake.expression`, the public entry point for one value, where there is no definition and so no subject for a Problem.

It refuses two things, at any depth: a single-key node whose key is neither structural nor in the fixity table (`unknown operator '<:' in …`), and a value that is neither a text leaf nor a single-key node (`unrenderable value …`). With `keys` and `compact` both are now sayable from a definition.

**Recommendation.** One new trio, `gendrake.ValueRefusalOf`, that returns the reasons as texts, wrapped into Problems where a subject exists.

- `ValueRefusalOf`: factory `par node Dracon`, one element `reasons [Text]`. It carries the two messages unchanged and recurses into the operands.
- `ElementRefusalOf` gains a third problem after the keyword and block-value ones: each reason of its element's value as a `Problem` on the subject.
- `Drake.expression` raises the first of `ValueRefusalOf(value).reasons`; `refuseUnrenderable` leaves the emitter, and `refuseElement` keeps only the formals check.

**Why texts and not Problems in the new trio.** `Drake.expression` has no definition to name, and inventing a subject there would put a false one in the record.

**One behavior difference.** As with the previous two, the refusal is judged over the whole definition before the formals check, and is raised from the emitter as an `IllegalArgumentException` (it was a `RuntimeException`, and still is one by subclass). A block whose misplaced value is also unrenderable is still refused for the block first.

**Not probed.** The recursion over operands when the single operand is not a list (the hand code treats it as one operand), and the structural key list containing a quoted backslash, which forced a separate binding in `ExpressionOf`.

**After this.** The emit side keeps one hand-written refusal: the formals check, shared with the parser and the Scala generator. I would bring that as its own design question, not as the next increment by default.

One corpus type would be added, so the parse scope, loss, aspect and GenDrake counts each move by one, and the test count by the five per-type gates plus a small focused suite.

---

## Status

6e475be (the block-value refusal in `gendrake.ElementRefusalOf`, `opensBlock` leaving Drake.scala; 11 files) is committed and pushed by Dev after a gate of 1292 tests. 101ea48 (`Dracon.keys`, a record's field names in written order, and `Dracon.compact`, a value's compact text; no consumer changed; 9 files) is committed and pushed by Dev after a gate of 1293 tests. Each gate matched its direct run on every headline and was green on its first run, as was each direct run. One point of the second recommendation did not hold and the reply says so: the seam count in HostTextTest does not move, neither new body being host syntax. No compaction boundary falls in this chapter. Recommended in the chapter's last reply and not yet accepted at its close: the value-tree refusal as `gendrake.ValueRefusalOf`; the formals check is named there as a larger question, deferred. Nothing on the tracker was touched; #11 stays open. Activation and .dcon are deferred; PON is suspended.
