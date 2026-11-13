# Chapter 131 - The Type-Form Renderer and the Fixity Table as Definitions

**Session date:** November 11–12, 2026
**Topic:** Two increments under GitHub #11: the type-form renderer of the drake surface becomes the definition `gendrake.TypeFormOf` (270ca7d), and drake's operator fixity table becomes the definition `draketarget.Fixity`, read by the parser and the emitter (f80584d), so that the expression renderer can follow.

**Provenance:** Original Claude Code session `92b66875-d6a1-4bde-bc3d-a3186065293e`, extract records 13–24; see the [bounded extract](sources/claude-2026-11-10-to-13-session-2-continued.jsonl) and [retrieval audit](sources/claude-2026-11-10-to-13-session-2-continued-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the type-form renderer as the definition gendrake.TypeFormOf?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 1481, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-11T22:48:44.958Z; source line 1497; extract record 13*

Accepted, build it

---

**[Claude]** *2026-11-11T22:54:57.930Z; source line 1684; extract record 14*

The type-form renderer is now the definition `gendrake.TypeFormOf`, the emitter delegates to it, and my direct run is green at 1210 tests. Nothing is committed; it waits on your gate.

**Built as accepted**
- **The definition:** [TypeFormOf.drake](../src/main/resources/draco/gendrake/TypeFormOf.drake) with its JSON and generated Scala, deriving `draketarget.Surface` and listed by GenDrake. It dispatches over the nine node keys with `guard(form.has(key)).ifThenElse(…)` and recurses through `TypeFormOf(member).value`.
- **The probe:** the nine-way dispatch, which I had not probed before recommending, parsed, generated and compiled on the first attempt.
- **The emitter:** `drakeType` in [Drake.scala](../src/mods/scala/draco/Drake.scala) is one call to the definition. `TypeForm.neutral` stays, as the reference and as the tree text for every other reader.
- **The test:** `TypeFormOfTest`, five tests, the 85th suite. It compares the definition with the hand-written renderer over every type form in main and mods.
- **No surface moved:** `DrakeCLI check` reads drift 0 over main and mods.

**One difference from the hand-written renderer**, on malformed input only: a node that is none of the nine yields the empty text, where `TypeForm.neutral` raises. Drake has no failure form yet. No definition in the corpus carries such a node, and the test pins the behaviour.

**Direct run** (everything compiled from source, current resources on the loader roots): 1210 tests, 86 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on the first run. The ten new tests are the suite's five and the new definition's five per-type gates.

Headlines that move with the one added corpus type:

| headline | was | now |
|---|---|---|
| parse scope | 121 + 73 | 122 + 73 |
| surface losses | 0 across 194 types | 0 across 195 types |
| aspect sections (both probes) | 121 | 122 |
| GenDrake | 119 of 119 | 120 of 120 |
| `Uses over` | 87 records | 88 records |
| `Definition over` | 194 surfaces, 518 sections | 195 surfaces, 521 sections |
| host text | 21 of 2237 | 21 of 2369, still 0 outside the seams |
| type leaves | 1585 in 194 | 1602 in 195 |

New line: `TypeForm over the corpus: 2062 type forms reproduced in 195 definitions (460 trees, 1602 leaves), 0 differing`. Example gate, scenario, PON 42 and 7, and the rete finding are unchanged.

Gate, with `TypeForm over` added to the grep:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|host text|type leaves|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|RuleAspect over|ActorAspect over|Uses over|CodecAspect over|Definition over|TypeForm over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Please paste the whole output. Expected: `Tests: succeeded 1210, failed 0`. The commit and push blocks follow once it passes; the record is `git-record-2026-11-11-1753`, which also carries the `decc752` fold. Nothing on the tracker was touched.

---

*(Delegated action — Dev ran the full sbt gate: 1210 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 122 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 120 of 120 definitions emitted, 0 missing, 0 not in the corpus; Definition over the corpus: 195 surfaces reproduced whole from 521 sections, 0 skipped; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; RuleAspect over the corpus: 195 definitions, 24 rule sections reproduced, 0 skipped; ActorAspect over the corpus: 195 definitions, 15 actor sections reproduced, 0 skipped; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 195 types; Drake.parse scope: 122 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; host text in values: 21 leaves of 2369 in 11 definitions, 0 outside the 11 named seams; DracoAspect over the corpus: 122 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; type leaves: 1602 in 195 definitions, 0 carrying host type syntax; Uses over the corpus: 195 definitions, 88 records reproduced, 0 skipped; CodecAspect over the corpus: 195 definitions, 4 codec sections reproduced, 0 skipped; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; TypeForm over the corpus: 2062 type forms reproduced in 195 definitions (460 trees, 1602 leaves), 0 differing. Source: 2026-11-12T15:44:43.143Z; source line 1688; extract record 15. The full pasted text is retained in the extract.)*

---

*(Context, not dialogue — the session's fifth compaction boundary is at source line 1696, between Dev's gate paste and the reply below, which is an original record from after it.)*

**[Claude]** *2026-11-12T15:45:18.056Z; source line 1768; extract record 16*

Your gate matches my direct run line for line: 1210 tests, zero failed, and every headline as expected, including the new `TypeForm over` line. I recorded it in the git-record, DRACO.md (new top baseline **1210 tests / 85 suites**), the handoff and the resume prompt; `git diff --check` is clean.

Commit, path-scoped over the fourteen paths:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md draco-git-record/git-record-2026-11-11-1753 src/main/resources/draco/gendrake/GenDrake.drake src/main/resources/draco/gendrake/GenDrake.json src/main/resources/draco/gendrake/TypeFormOf.drake src/main/resources/draco/gendrake/TypeFormOf.json src/main/scala/draco/gendrake/GenDrake.scala src/main/scala/draco/gendrake/TypeFormOf.scala src/mods/scala/draco/Drake.scala src/test/scala/draco/gendrake/TypeFormOfTest.scala) && git add -- "${paths[@]}" && git commit -F draco-git-record/git-record-2026-11-11-1753 -- "${paths[@]}" && git status --short && git log --oneline -1
```

Push:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git push origin main && git status -sb | head -1
```

Expected: 14 files changed, an empty `git status --short`, then `decc752..<new hash>  main -> main`.

Once you paste the outputs I will fold the hash into the shared documents with the next increment and bring the next related point under #11. Nothing on the tracker was touched.

---

*(Delegated action — Dev committed 270ca7d, “The type-form renderer of the drake surface is definition-backed: gendrake.TypeFormOf (GitHub #11)” (14 files changed, 925 insertions(+), 13 deletions(-)). `git log --oneline -1`: 270ca7d (HEAD -> main) The type-form renderer of the drake surface is definition-backed: gendrake.TypeFormOf (GitHub #11). Pushed to origin/main: decc752..270ca7d. Status line `## main...origin/main`. Source: 2026-11-12T16:44:04.521Z; source line 1772; extract record 17. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-12T16:44:36.408Z; source line 1819; extract record 18*

`270ca7d` is committed and pushed (`decc752..270ca7d`, 14 files); `main` and `origin/main` agree. The hash is not yet folded into the shared documents; it rides with the next increment's record, as before.

**Next related point under GitHub #11: drake's operator fixity table as a definition.**

What I measured in [Drake.scala](../src/mods/scala/draco/Drake.scala):

- **What is left hand-written on the emit side:** the expression renderer (`render` and `parenthesized`, about 50 lines) and the element renderer (`elementLines`, `leafLines`, `caseLines`, `sectionLines` and the keyword table, about 80 lines).
- **Order:** the element renderer calls the expression renderer, so the expression renderer is the next leaf, as the type form was.
- **What blocks it:** the expression renderer decides parentheses from the fixity table — 15 operators, each with a precedence and a grouping, plus the three flat-variadic ones. That table is a private value in the hand-written engine in mods, shared by the parser and the emitter.
- **Why that matters:** a definition in main may not reach into mods, so `gendrake.ExpressionOf` cannot be written until the table lives in main.

**Recommendation:** move the table alone, as one definition in `draco.draketarget` (the surface's own vocabulary, beside the layout types).

- It carries the operators as data, in existing vocabulary only: name, precedence, grouping, and the flat-variadic set.
- `Drake.parse` and `Drake.emit` both read it from there; the private table and its case class leave the engine.
- No surface, JSON or generated Scala of any existing definition moves.
- One focused test pins the 15 rows against Haskell's Prelude fixities as drake.dlt states them, and checks that parser and emitter read the same table.
- Expected movement: one corpus type added, so the per-type headlines each move by one.

**Deliberately left out:** the expression renderer itself. It also raises on an unknown operator, and drake has no failure form, which is a design question of its own. The Scala target's precedence table in `DracoGenerator` also stays; it is that target's own.

**One thing I have not probed:** whether a table of records with a character-valued grouping generates cleanly. I would probe that first and come back if the shape has to change.

Nothing on the tracker was touched.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the fixity table as a definition, read by the parser and the emitter?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 1813, outside the reply text, and is not an extract record. The session's sixth compaction boundary is at source line 1837, directly after this message; the reply below is an original record from after it.)*

**[Dev]** *2026-11-12T19:03:29.105Z; source line 1829; extract record 19*

Accepted, build it

---

**[Claude]** *2026-11-12T19:11:50.790Z; source line 2066; extract record 20*

The fixity table is built as accepted and ready for your gate. The direct run of every discovered suite passed 1219 tests in 87 suites including discovery, zero failed, canceled, ignored, pending or aborted.

**What was built**
- **`draketarget.Fixity`** (new trio, listed by DrakeTarget): a record of `precedence` and `associativity`, with three globals — `operators` (the fifteen rows), `table` (their map) and `flattened` (`++`, `&&`, `||`).
- **`Drake.scala`**: the private case class and both private tables are gone; the parser and the emitter read `Fixity.table` and `Fixity.flattened`. Their logic is untouched.
- **`FixityTest`** (4 tests, the 86th suite): the rows as drake.dlt states them, the parser reading every row, the emitter's minimal parentheses, and the record's codec.
- **Documents**: the record `git-record-2026-11-12-1410` and the CHANGELOG entry are written. The 270ca7d fold is in DRACO.md, the handoff and the resume prompt. drake.dlt and README each gain one sentence naming the definition.

**Two things that differ from what I said**
- The grouping is carried as Text (`left`, `right`, `none`), not a character. That was the shape question I had not probed; it generated cleanly on the first attempt.
- The first direct run failed three ScenarioGenTest tests. The cause was my harness: the test resources were not on a loader root. With that corrected, the same classes are green.

**Headlines to expect** (one corpus type added, no new grep line)
- `Tests: succeeded 1219`
- parse scope 123 + 73, 0 held back; surface losses 0 across 196 types
- DomainAspect and DracoAspect 123 sections; GenDrake 121 of 121
- Rule 24, Actor 15, Uses 89, Codec 4, each over 196 definitions
- Definition: 196 surfaces from 524 sections
- host text 21 leaves of 2433, 0 outside the seams; type leaves 1612 in 196
- TypeForm: 2076 type forms (464 trees, 1612 leaves), 0 differing
- GEN MAP 48 / 0; scenario 23 clean; forest 1 of 2; PON 42 and 7; CO-DECLARATION DROPS THE FACT

`DrakeCLI check` reads drift 0 over main and mods, so no other surface, JSON or Scala moved. Nothing on the tracker was touched.

Please run the gate and paste the output; I'll hand over the commit and push blocks once it is green.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|host text|type leaves|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|RuleAspect over|ActorAspect over|Uses over|CodecAspect over|Definition over|TypeForm over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

---

*(Delegated action — Dev ran the full sbt gate: 1219 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 123 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 121 of 121 definitions emitted, 0 missing, 0 not in the corpus; Definition over the corpus: 196 surfaces reproduced whole from 524 sections, 0 skipped; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; RuleAspect over the corpus: 196 definitions, 24 rule sections reproduced, 0 skipped; ActorAspect over the corpus: 196 definitions, 15 actor sections reproduced, 0 skipped; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 196 types; Drake.parse scope: 123 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; host text in values: 21 leaves of 2433 in 11 definitions, 0 outside the 11 named seams; DracoAspect over the corpus: 123 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; type leaves: 1612 in 196 definitions, 0 carrying host type syntax; Uses over the corpus: 196 definitions, 89 records reproduced, 0 skipped; CodecAspect over the corpus: 196 definitions, 4 codec sections reproduced, 0 skipped; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; TypeForm over the corpus: 2076 type forms reproduced in 196 definitions (464 trees, 1612 leaves), 0 differing. Source: 2026-11-12T23:16:28.572Z; source line 2078; extract record 21. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-12T23:16:48.769Z; source line 2111; extract record 22*

Your gate is green: 1219 tests, zero failed, canceled, ignored or pending, and every headline matches the direct run. The baseline is now 1219 tests / 86 suites, recorded in the record, DRACO.md §1 and §5, the handoff and the resume prompt.

The tree holds only the fifteen intended files. Commit:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-11-12-1410 src/main/resources/draco/draketarget/Fixity.drake src/main/resources/draco/draketarget/Fixity.json src/main/scala/draco/draketarget/Fixity.scala src/test/scala/draco/draketarget/FixityTest.scala && git commit -F draco-git-record/git-record-2026-11-12-1410 -- draco-git-record/git-record-2026-11-12-1410 src/main/resources/draco/draketarget/Fixity.drake src/main/resources/draco/draketarget/Fixity.json src/main/scala/draco/draketarget/Fixity.scala src/test/scala/draco/draketarget/FixityTest.scala src/main/resources/draco/draketarget/DrakeTarget.drake src/main/resources/draco/draketarget/DrakeTarget.json src/main/scala/draco/draketarget/DrakeTarget.scala src/mods/scala/draco/Drake.scala src/main/resources/draco/drake.dlt CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md && git status --short && git log --oneline -1
```

Then push:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git push origin main && git status -sb | head -1
```

Please paste both outputs. The new hash will be folded into the documents with the next increment, as before.

The journal is current through chapter 129; three commits will be uncovered after this one (decc752, 270ca7d and this). Nothing on the tracker was touched.

Once the commit is in, I'll bring the next related point under GitHub #11: the expression renderer as a definition, which the fixity table now makes possible.

---

*(Delegated action — Dev committed f80584d, “Drake's operator fixity table is a definition: draketarget.Fixity, read by the parser and the emitter (GitHub #11)” (15 files changed, 569 insertions(+), 28 deletions(-)). `git log --oneline -1`: f80584d (HEAD -> main) Drake's operator fixity table is a definition: draketarget.Fixity, read by the parser and the emitter (GitHub #11). Pushed to origin/main: 270ca7d..f80584d. Status line `## main...origin/main`. Source: 2026-11-12T23:50:05.224Z; source line 2121; extract record 23. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-12T23:50:33.479Z; source line 2142; extract record 24*

`f80584d` is committed and pushed (`270ca7d..f80584d`, 15 files); `main` and `origin/main` agree. The hash is not yet folded into the shared documents; it rides with the next increment's record.

**Next related point under GitHub #11: the expression renderer as a definition.**

What I measured in [Drake.scala](../src/mods/scala/draco/Drake.scala):

- **What it is:** `render` and `parenthesized`, about 50 lines. It spells eight structural nodes (path, arrow, application, lambda, tuple, the two collection literals, named argument) and the fifteen infix operators. It also writes a pair of parentheses only where the fixity table says the surface would otherwise regroup.
- **What it needs from a definition:** recursion carrying the parent's operator and the operand's position, and a way to tell which operator a node is. Dracon has `has(name)` but nothing that reads a node's own key, so the dispatch would try the known keys in turn, as `TypeFormOf` does.
- **Who reads its errors:** it raises on an unknown operator and on a value that is neither a leaf nor a single-key node. No test asserts either.

**Recommendation:** one definition, `gendrake.ExpressionOf`, deriving `draketarget.Surface`, in existing vocabulary only.

- It takes the value, the parent's operator and the operand's position and count, and reads `Fixity.table` for the parentheses.
- `Drake.emit`'s `expression` delegates to it.
- The two refusals stay in the emitter as a check before the call, as the three codec guards did, so malformed input still raises exactly as today.
- One focused suite compares it with the current renderer over every value in main and mods, with a headline in the gate grep.
- Expected movement: one corpus type added, so the per-type headlines each move by one; no surface, JSON or other Scala moves.

**Deliberately left out:** the element renderer, a `keys` inspection on Dracon (not needed for this), and a failure form for drake.

**Not yet probed:** whether a 23-way dispatch with four-argument recursion generates and compiles. `TypeFormOf`'s nine-way one did on the first attempt; I would probe this first and come back if the shape has to change.

The journal is current through chapter 129; decc752, 270ca7d and f80584d are uncovered. Nothing on the tracker was touched.

Accept, or correct it first?

---

## Status

270ca7d (`gendrake.TypeFormOf`, the emitter's `drakeType` delegating; 14 files; Dev's gate 1210 tests, the new `TypeForm over` line as expected) and f80584d (`draketarget.Fixity`, read by the parser and the emitter; 15 files; Dev's gate 1219 tests) are committed and pushed by Dev, each gate green on its first run with every headline matching the direct run. The fixity increment's first direct run failed three ScenarioGenTest tests through the harness (test resources off the loader roots) and was green on the second. The session's fifth and sixth compaction boundaries fall in this chapter, with original records on both sides of each. Recommended in the chapter's last reply and not yet accepted at its close: the expression renderer as the definition `gendrake.ExpressionOf`. Not accepted: the element renderer, a failure form for the guards. Nothing on the tracker was touched; #11 stays open. Activation and .dcon are deferred; PON is suspended.
