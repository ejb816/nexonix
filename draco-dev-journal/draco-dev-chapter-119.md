# Chapter 119 - The Scenario Assertion and the PON Measurement

**Session date:** November 3, 2026
**Topic:** ScenarioDrakeTest's round-trip becomes an assertion; the PON pair is measured and a per-category assertion recommended; Dev's instruction on PON arrives and is interrupted.

**Provenance:** Original Claude Code session `f12792d4-b3c1-4ac3-b8f9-c2853199c07f`, extract records 113–120; see the [bounded extract](sources/claude-2026-10-26-to-11-03.jsonl) and [retrieval audit](sources/claude-2026-10-26-to-11-03-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Assert ScenarioDrakeTest's round-trip and retire its table row?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 8363, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-03T14:38:36.984Z; source line 8373; extract record 113*

Accepted, build it

---

**[Claude]** *2026-11-03T14:45:28.492Z; source line 8517; extract record 114*

**Built as recommended.** The round-trip test in `src/test/scala/scenario/ScenarioDrakeTest.scala` is renamed "the scenario in today's drake: every file parses and round-trips" and fails after its console line when any file is rejected or drifted, naming each with its error or a pointer to the logged pair. The class doc's report-only paragraph now reads as a gate dated today, with the worklist's history and the RootInterface sentence corrected to the two case branches it has carried since `case` was reserved. The per-file "as parsed" log and the console headline are unchanged, and the headline stays in the gate grep. DRACO.md loses the row, its assertions paragraph names the fifth, and the report-only rule keeps PON as its one live example.

**One correction to yesterday's wording.** I said the table would then hold the PON pair, the file-only DrakeGenTest row and the rete row. It also keeps the parse-scope row, which it always had. The record, DRACO.md and the handoff say so.

**Verified three ways.**

| run | result |
|---|---|
| focused ScenarioDrakeTest | 1 test, green, headline 23 files, 0 rejected, 0 drifted, 23 clean |
| negative probe on the scratch tree, one drifted surface and one rejected | 1 failed, both named: `REJECTED birch/Compound.drake: unexpected section 'whatever'` and `DRIFTED ash/RootInterface.drake`; headline 1 rejected, 1 drifted, 21 clean |
| every discovered suite, overlays rebuilt from source | 1119 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or aborted, every headline unchanged, pending mods definitions 0 |

The probe settled one more thing the record and handoff carry: the two scenario suites read their resources relative to the working directory, not from the classpath root the corpus suites derive, so a probe of them runs from the scratch tree. The real tree was untouched.

**Written, uncommitted:** the record `draco-git-record/git-record-2026-11-03-0942`, the CHANGELOG entry, DRACO.md's lead, table, assertions paragraph, rule and tail, the handoff with a new section and the procedure note, the resume prompt and memory, all at "Dev's gate pending, expected 1119 / 77". The b321df6 hash fold rides in the same set. Links check clean.

The gate, expecting `succeeded 1119` and a trailing `1`, every line as in your last paste but the test's own line, which now reads "every file parses and round-trips" instead of "(report only)":

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:" ; grep -c "pending .drake authoring (0)" target/test-output/DrakeGenTest.log
```

On green I reword the record and documents to verified and hand over the commit block, which includes `git add` of the new record.

---

*(Delegated action — Dev ran the full sbt gate: 1119 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 183 types; Drake.parse scope: 110 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. The trailing count of the zero-pending line printed 1. Source: 2026-11-03T18:11:04.773Z; source line 8521; extract record 115. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-03T18:12:03.816Z; source line 8558; extract record 116*

**Verified.** Your gate matches the direct run line for line: 1119 tests / 77 suites, zero failed, canceled, ignored or pending, every headline as before, the scenario line now printed by the asserting test, and the file-only grep at 1. The record's verification paragraph, DRACO.md's lead and tail, the handoff's chain, state, gate sentence, evidence and ledger row, and the resume prompt's header all read "gated by Dev at 1119 / 77, commit pending". No "gate pending" phrase remains, and the link checker is clean.

The tree holds five modified files and the new record, nothing committed. The commit block, path-scoped, with the record staged first since it is untracked:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-11-03-0942 && git commit -F draco-git-record/git-record-2026-11-03-0942 --trailer "Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" -- src/test/scala/scenario/ScenarioDrakeTest.scala draco-git-record/git-record-2026-11-03-0942 CHANGELOG.md DRACO.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md && git push && git log -1 --oneline && git status --short
```

Expect six files changed, the push from b321df6, and an empty status. On your paste I fold the hash into DRACO.md, the handoff, the resume prompt and memory, then measure the next related point. The table now holds four rows, none of them a round-trip count: the parse scope, the two PON readings, the file-only pending-surface count and the categorical rete finding. The PON pair is the only remaining measurement with a tail, and whether its 42 discrepancies and 7 canonical differences are a worklist or a settled description of the corpus is what I will read before saying anything about it.

---

*(Delegated action — Dev committed f4b57ae, “Assert the scenario round-trip and retire its table row” (6 files changed, 187 insertions(+), 33 deletions(-)). Pushed to origin/main: b321df6..f4b57ae. Source: 2026-11-03T18:33:55.352Z; source line 8562; extract record 117. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-03T18:36:07.903Z; source line 8646; extract record 118*

**Committed and folded.** f4b57ae is on origin, six files, the tree clean at the commit. The hash is in DRACO.md's lead and tail, the handoff's chain, state, gate sentence, next-design-point opener, evidence and ledger row, and the resume prompt's header. Those three files are modified in the tree and ride with the next record.

**The PON pair, measured.** The two rows left in the table with a tail are PonCorpusTest's, 42 discrepancies and 7 canonical differences. They are a different kind of number from the four the table has shed.

- **What they measure.** The suite at `src/test/scala/domains/pon/PonCorpusTest.scala` runs the Pon evaluator over the "Prime ordinal 78" document, 559 lines mapping 0 to 79 to expression sequences, and reports every expression that does not evaluate to its index, then compares each number's first expression with the generator's canonical form. The evaluator itself is pinned by three hard gates in the same suite: the agreed values, encode then eval as identity over 1 to 256, and every generated decomposition over 2 to 200 evaluating to its number.
- **What the 42 are.** 19 parse failures and 23 wrong values, no sequence errors. The parse failures are unbalanced parentheses in the document's own text, for example the line for 56 reads `((:(())).((:())(()))` with one closing paren missing. The wrong values are expressions whose sum is another number. The 7 are two numbers whose document line carries no canonical at all, 1 and 29, and five whose first expression is a different ordering from the generator's, four of them still evaluating correctly and the one for 40 evaluating to 20.
- **What they have done.** Nothing. The document was committed once, on 2026-06-27, and never changed. Both counts have stood at 42 and 7 since the table's origin at 87a2bb9 on 2026-08-31 and in every paste this session.

So this is not a tail to drive to zero. The suite's own doc says the discrepancies are the document's "few errors or inconsistencies", and the document is the artifact under measurement, not a definition to re-canonicalize. What the counts do is detect an evaluator regression over 508 real expressions that currently agree, and that detection is read by a human or not at all, the exact gap GitHub issue 62 describes, with these two rows in its table.

**Recommendation.** Assert both counts exactly, per category, as that issue proposes: the corpus test fails unless it finds 80 numbers, 550 expressions, 19 parse failures, 23 wrong values and 0 sequence errors; the canonical test unless it finds 80 numbers, 2 missing and 5 differing. Each headline prints its breakdown, the comments say the tail is the frozen document's and date it, "(report only)" leaves both test names, and the two rows leave the table as the sixth and seventh measurements to become assertions, both console lines staying in the gate grep. No test count moves, no engine, definition or Scala change, expected gate 1119 / 77. A failure then means one of two things, both worth stopping for: the document changed, which re-baselines deliberately, or the evaluator moved on an expression shape the three hard gates do not cover. The table would then hold the parse scope, the file-only pending-surface count and the categorical rete finding, none a measurement with a tail.

One caveat on provenance: issue 62 is model-authored, so the per-category form is prior reasoning, not your specification. I recommend it because the two categories have different causes, a document typo against a document arithmetic error, and a total would let one rise as the other fell.

Considered and not recommended: listing the 42 by line in the test, which the log already does; and correcting the document, which would measure a different artifact.

One observation, not recommended: the suite's "emit regenerated PON document" test writes `src/test/resources/domains/pon/prime-ordinal-generated.md` into the source tree on every run. The file is tracked and comes back byte-identical, so the tree stays clean, but it is the same shape as the PrimesRulesTest write the handoff already flags.

---

**[Dev]** *2026-11-03T20:54:00.941Z; source line 8650; extract record 119*

The PON related work will eventually be contained in the draco.primes.Primes domain. Right now it's in domains.pon.Pon and development needs to be suspended on all PON activity until it is explicitly resumed by me, when I am ready tomintegerte it into the draco native Primes domain.

---

*(Source event, not dialogue — the request was interrupted by Dev: “[Request interrupted by user]”, 2026-11-03T20:54:18.801Z; source line 8695; extract record 120. A compaction boundary sits between the prompt above and this marker. No reply exists in this record.)*

---

## Status

f4b57ae (the scenario round-trip asserted, 1119 tests, the fifth measurement to become an assertion) is committed and pushed by Dev. The closing reply measured the PON pair (42 discrepancies as 19 parse failures and 23 wrong values; 7 canonical differences) and recommended asserting both counts by category. Dev's answer is this record's last prompt: it was delivered at source line 8650 immediately before a compaction boundary and interrupted eighteen seconds later; Dev re-sent it with one word corrected as the first prompt of the next session record, where chapter 120 takes it up. No reply to it exists in this record.
