# Chapter 142 - The Value-Tree Refusal, and the Formals Check in Core

**Session date:** November 14–15, 2026
**Topic:** Two increments under GitHub #11: `gendrake.ValueRefusalOf` returns the reasons a value is outside the tree language (980de86), and the check on a method's formals becomes the core definition `draco.FormalsRefusalOf`, read by the emitter, the parser and the Scala generator. Dev asks for the journal before running the gate.

**Provenance:** Original Claude Code session `92b66875-d6a1-4bde-bc3d-a3186065293e`, extract records 23–31; see the [bounded extract](sources/claude-2026-11-14-to-15-session-2-continued.jsonl) and [retrieval audit](sources/claude-2026-11-14-to-15-session-2-continued-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Accept the value-tree refusal as gendrake.ValueRefusalOf, read by ElementRefusalOf?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 5449, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-15T02:58:02.260Z; source line 5456; extract record 23*

Accepted, build it

---

**[Claude]** *2026-11-15T03:03:44.825Z; source line 5556; extract record 24*

The value-tree refusal is built and ready for your gate. My direct run of every discovered suite passed 1305 tests / 95 suites including discovery on its first run; nothing is committed.

### What was built

- **New trio `gendrake.ValueRefusalOf`**: takes a value and returns `reasons [Text]`. It reports an operator that is neither structural nor in the fixity table, and a value that is neither a text leaf nor a single-key node, at any depth, with the emitter's two messages unchanged. GenDrake lists it.
- **`gendrake.ElementRefusalOf`** now reports the keyword problem, then one Problem per reason of the element's value, then the block-value problem, then recurses.
- **`Drake.scala`**: `expression` raises the first reason. `refuseUnrenderable` is removed, and `refuseElement` keeps only the formals check.
- **`ValueRefusalOfTest`** (6 tests, the 94th suite) and one test added to `ElementRefusalOfTest`.

### Where this differs from the recommendation

- **Order against the block refusal.** I said a block whose misplaced value is also unrenderable would still be refused for the block first. It is refused for the value first, which is the order the hand-written emitter used. The block check spells the value, and spelling is only defined inside the tree language, so I guard it.
- **All reasons are reported.** The emitter stopped at the first; the definition lists every one, and the first is the same.

As recommended, the refusal is judged over the whole definition before the formals check and is raised as an `IllegalArgumentException`. No test outside the two suites changed. Both unprobed points held.

### Headlines to expect

`Tests: succeeded 1305` (1293, plus five per-type gates, six in the new suite and one in `ElementRefusalOfTest`). There is no new grep line. These move:

| headline | expected |
|---|---|
| parse scope | 131 draco + 73 mods, 0 held back |
| surface losses | 0 across 204 types |
| DomainAspect / DracoAspect | 131 |
| GenDrake | 129 of 129 |
| Uses | 97 records |
| Definition | 204 surfaces from 548 sections |
| Element | 1166 elements (1413 lines) |
| Expression | 705 values (519 trees, 186 leaves) |
| Slot | 1151 slots (83 arrows) |
| TypeForm | 2231 forms (503 trees, 1728 leaves) |
| type leaves | 1728 |
| host text | 21 of 3010, 0 outside the seams |
| Refusal | 204 definitions (4 carrying a payload), 0 refused |

Unchanged: rule 24, actor 15, codec 4, Formal 36, GEN MAP 48 / 0, scenario 23 clean, PON 42 and 7, the rete finding. `DrakeCLI check` reads drift 0 over main and mods, and the trios are stable on regeneration.

### The gate

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|host text|type leaves|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|RuleAspect over|ActorAspect over|Uses over|CodecAspect over|Definition over|TypeForm over|Expression over|Element over|Formal over|Slot over|Refusal over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

After a green gate I will record it and hand over the commit and push blocks. The record is `draco-git-record/git-record-2026-11-14-2202`; it also folds `101ea48` into the shared documents. Nothing on the tracker was touched.

---

*(Delegated action — Dev ran the full sbt gate: 1305 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 131 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 129 of 129 definitions emitted, 0 missing, 0 not in the corpus; Element over the corpus: 1166 elements reproduced in 204 definitions (1413 lines), 0 differing; Expression over the corpus: 705 values reproduced in 204 definitions (519 trees, 186 leaves), 0 differing; Definition over the corpus: 204 surfaces reproduced whole from 548 sections, 0 skipped; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; RuleAspect over the corpus: 204 definitions, 24 rule sections reproduced, 0 skipped; ActorAspect over the corpus: 204 definitions, 15 actor sections reproduced, 0 skipped; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 204 types; Drake.parse scope: 131 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; host text in values: 21 leaves of 3010 in 11 definitions, 0 outside the 11 named seams; DracoAspect over the corpus: 131 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; type leaves: 1728 in 204 definitions, 0 carrying host type syntax; Slot over the corpus: 1151 value-type slots reproduced in 204 definitions (83 arrows parenthesized), 0 differing; Refusal over the corpus: 204 definitions (4 carrying a payload), 0 refused; Uses over the corpus: 204 definitions, 97 records reproduced, 0 skipped; CodecAspect over the corpus: 204 definitions, 4 codec sections reproduced, 0 skipped; the forest runs: 1 of 2 ash alarms crossed to birch; Formal over the corpus: 36 formals reproduced in 204 definitions (33 lists, 7 bounded), 0 differing; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; TypeForm over the corpus: 2231 type forms reproduced in 204 definitions (503 trees, 1728 leaves), 0 differing. Source: 2026-11-15T03:06:31.774Z; source line 5566; extract record 25. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-15T03:06:59.674Z; source line 5585; extract record 26*

Your gate matches my direct run on every line: 1305 tests, zero failed. I recorded it in the git record, DRACO.md (§1 baseline now 1305 tests / 94 suites, the §5 line and the closing paragraph), the handoff and the resume prompt.

The commit covers 18 files, which is everything `git status` shows:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md draco-git-record/git-record-2026-11-14-2202 src/main/resources/draco/gendrake/ElementRefusalOf.drake src/main/resources/draco/gendrake/ElementRefusalOf.json src/main/resources/draco/gendrake/GenDrake.drake src/main/resources/draco/gendrake/GenDrake.json src/main/resources/draco/gendrake/ValueRefusalOf.drake src/main/resources/draco/gendrake/ValueRefusalOf.json src/main/scala/draco/gendrake/ElementRefusalOf.scala src/main/scala/draco/gendrake/GenDrake.scala src/main/scala/draco/gendrake/ValueRefusalOf.scala src/mods/scala/draco/Drake.scala src/test/scala/draco/gendrake/ElementRefusalOfTest.scala src/test/scala/draco/gendrake/ValueRefusalOfTest.scala) && git add -- "${paths[@]}" && git commit -F draco-git-record/git-record-2026-11-14-2202 -- "${paths[@]}" && git status --short && git log --oneline -1
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git push origin main
```

Please paste the output of both; I need the hash to fold into the next record.

The journal is current through chapter 139, and four increments are now uncovered (`05aa585`, `6e475be`, `101ea48` and this one). The session has not compacted since chapter 139 was written, so the originals are all in reach either way. If you choose the journal first, hold the commit block: the chapters would ride in this commit and the path list would grow.

After the commit, the next point under #11 is a design question, not an increment by default: where the formals check lives, since the emitter, the parser and the Scala generator all share it.

---

*(Delegated action — Dev committed 980de86, “The value-tree refusal is a definition: gendrake.ValueRefusalOf returns the reasons, read by ElementRefusalOf and Drake.expression (GitHub #11)” (18 files changed, 761 insertions(+), 45 deletions(-)). `git log --oneline -1`: 980de86 (HEAD -> main) The value-tree refusal is a definition: gendrake.ValueRefusalOf returns the reasons, read by ElementRefusalOf and Drake.expression (GitHub #11). Pushed to origin/main: 101ea48..980de86. Source: 2026-11-15T03:08:02.502Z; source line 5589; extract record 27. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-15T03:08:31.034Z; source line 5596; extract record 28*

`980de86` is committed and pushed (`101ea48..980de86`, 18 files, tree clean). Its hash folds into the shared documents with the next commit.

### Next point under GitHub #11: where the formals check lives

**What I measured.** `MethodTypeParameters.validate` is the one refusal still hand-written on the emit side. It is 30 lines in mods and has three callers: the drake emitter (`refuseElement`), the drake parser (`checkedDynamic`) and the Scala generator. For one method it checks:

- the name carries no bracket or parenthesis (formals are declared separately);
- no formal is declared twice;
- each formal's name is a name;
- each bound is a well-formed type form (the right number of operands per node) and its text carries no bound or context-bound marker.

None of this depends on a target. It is a statement about the definition: a method whose formals fail it is malformed for drake, for Scala, and for any later target alike.

**Recommendation.** Make it a definition in the core `draco` domain, beside `TypeParameter`, and have all three callers read it.

- New trio `draco.FormalsRefusalOf`: factory `par element TypeElement`, one element `reasons [Text]`, with today's messages. It returns texts for the same reason `ValueRefusalOf` does: the parser and the Scala generator have a method in hand, not necessarily a definition to name.
- `gendrake.ElementRefusalOf` reports each reason as a Problem, so the emitter's `refuseElement` and the hand-written half of `refuseDefinition` go away entirely.
- The parser and the Scala generator raise the first reason; `MethodTypeParameters.scala` is deleted.

**Why core and not `draco.generator`.** The parser is not a generator, and the rule is about the source type. Haskell would put it in the module that defines the formal.

**Differences to expect.** The messages lose the host prefix "requirement failed: ", as the payload refusals did. All reasons are reported, with the first unchanged.

**Not probed.** The bound check recognises a type-form node by trying the nine known keys, as `TypeFormOf` does, and I have not written that recursion in drake. If it grows awkward I would stop and bring it back as two increments: the name and duplicate checks first, the bound check second.

**Rejected.** Leaving it hand-written as a permanent seam: it carries no host realization, so it does not meet the bar the seam list sets. Also a core validation rule fired in the Editor's session: the three callers need an answer for one method without a rule session.

One corpus type would be added to `draco`, so the parse scope, loss, aspect and GenDrake counts each move by one, and Draco's own trio moves to list it.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Accept the formals check as a core draco definition, read by all three callers?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 5593, outside the reply text, and is not an extract record. A compaction boundary is at source line 5608, directly after this message; the reply is an original record from after the boundary. The turn it closes paused twice while a background run of the suites was pending and resumed on a task notification each time (source lines 5824 and 5851, not Dev messages); the texts that ended those pauses (lines 5820 and 5847) are intermediate progress, not extract records, and the reply below restates what they reported.)*

**[Dev]** *2026-11-15T14:06:50.985Z; source line 5600; extract record 29*

Accepted, build it

---

**[Claude]** *2026-11-15T14:17:46.295Z; source line 5895; extract record 30*

The formals check is built as `draco.FormalsRefusalOf` and my direct run of every discovered suite passed: 1317 tests, 96 suites including discovery, none failed, canceled, ignored, pending or aborted. That was its second run; your gate has not run yet and nothing is committed.

### What changed

- **New trio `draco.FormalsRefusalOf`**, listed by Draco: `reasons [Text]` for one method, in the hand-written check's order.
- **`gendrake.ElementRefusalOf`** reports each reason as a Problem, after the keyword problem and ahead of the value's.
- **`Drake.scala`**: `refuseElement` and the hand walk in `refuseDefinition` are gone; the parser raises the first reason.
- **`DracoGenerator.scala`**: `methodName` raises the first reason.
- **`MethodTypeParameters.scala`** is deleted.
- **Tests**: `FormalsRefusalOfTest` (7 tests, the 95th suite) is new; `MethodTypeParameterTest` and `ElementRefusalOfTest` are restated where they named the removed check.

### What did not go as recommended

- **Bound markers are reported per leaf.** "Invalid bound for 'T': A <: B" names the leaf, where the old check printed the whole bound's text. For a one-leaf bound the message is identical. One edge is lost: a leaf ending in a colon inside a tree is no longer caught.
- **The first run failed 8 tests, all mine.** Seven came from one redundant pair of parentheses I authored, which the parser dissolves. One was the new corpus test's guessed lower bounds (the corpus has 104 methods, 10 carrying formals).
- **An edit overran.** I removed `dracoAspectText` from `Drake.scala` by mistake and restored it before the first compile.

The differences I did predict held: messages lose "requirement failed: ", and every reason is reported with the first unchanged.

### Headlines to expect

- Parse scope 132 + 73, 0 held back
- Surface losses 0 across 205 types
- DomainAspect and DracoAspect 132
- GenDrake 130 of 130
- Uses 98 records
- Definition 205 surfaces from 551 sections
- Element 1203 elements (1456 lines)
- Expression 736 values (546 trees, 190 leaves)
- Slot 1189 slots (93 arrows)
- TypeForm 2316 forms (538 trees, 1778 leaves)
- Type leaves 1778
- Host text 21 leaves of 3190 in the 11 seams
- Refusal 205 definitions, 0 refused

Unchanged: rule 24, actor 15, codec 4, Formal 36 (33 lists, 7 bounded), example gate 48 / 0, scenario 23 clean, forest 1 of 2, PON 42 and 7, the rete finding. There is no new grep line, and `DrakeCLI check` reads drift 0 over main and mods.

### Gate

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|host text|type leaves|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|RuleAspect over|ActorAspect over|Uses over|CodecAspect over|Definition over|TypeForm over|Expression over|Element over|Formal over|Slot over|Refusal over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Please paste the `Tests:` line and the headlines. I expect 1317 tests; the baseline would then be 1317 / 95.

The record is [git-record-2026-11-15-0916](../draco-git-record/git-record-2026-11-15-0916); it also folds `980de86` into the shared documents. After a green gate I'll record it and hand over the commit and push blocks.

The journal is current through chapter 139. Five commits and this increment are uncovered, and this session has compacted again, so I can write those chapters before the commit if you want them in it.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Run the full gate and paste the Tests line and headlines?” with the buttons “Gate is green”, “Journal first” (a tool call at source line 5892, not an extract record). The message below is the open turn at the extract's boundary: it asked for this journal update, and its reply is outside the frozen prefix. Dev's gate on the formals increment had not been run.)*

**[Dev]** *2026-11-15T14:19:56.397Z; source line 5899; extract record 31*

Journal first

---

## Status

980de86 (`gendrake.ValueRefusalOf`, read by `gendrake.ElementRefusalOf` and `Drake.expression`; `refuseUnrenderable` leaving Drake.scala; 18 files) is committed and pushed by Dev after a gate of 1305 tests, every headline matching the direct run, green on its first run. One point of its recommendation did not hold and the reply says so: a block whose misplaced value is also unrenderable is refused for the value first. Built and uncommitted at the chapter's close, on November 15: `draco.FormalsRefusalOf` (factory `par element TypeElement`, `reasons [Text]`, the globals `wellFormed` and `leaves`), listed by Draco; `gendrake.ElementRefusalOf` reporting each reason as a Problem; `refuseElement` and the hand walk of `refuseDefinition` leaving Drake.scala; the parser and the Scala generator raising the first reason; `MethodTypeParameters.scala` deleted; FormalsRefusalOfTest's seven tests. Its direct run passed 1317 tests on its second run; the first failed 8, all of the increment's own making (an authored redundant pair of parentheses the parser dissolves, and the new corpus test's guessed lower bounds), and an edit that removed `dracoAspectText` by an overrun was restored before the first compile. One thing differs from the recommendation: a bound marker is reported per leaf, not with the whole bound's text. DEV'S GATE ON THIS INCREMENT HAD NOT BEEN RUN at the chapter's close: asked to run it, Dev answered “Journal first”, the open turn at the extract's boundary. The record is `git-record-2026-11-15-0916`, which also folds 980de86. One compaction boundary falls in this chapter, directly after Dev's acceptance of the formals check, with original records on both sides. With this increment no refusal or renderer of the drake surface is hand-written; nothing further on the thread is recommended or accepted. Nothing on the tracker was touched; #11 stays open. Activation and .dcon are deferred; PON is suspended.
