# Chapter 124 - Rooted Rule Traits and the One Stub Predicate

**Session date:** November 9, 2026
**Topic:** GitHub #50 item 2 and #47 are built and closed: every generated rule trait derives DracoType, and `TypeDefinition.isStub` is the one definition of a stub.

**Provenance:** Original Claude Code session `1cf64c57-6ed5-4b94-9b2e-faf865e8fdc0`, extract records 7–30; see the [bounded extract](sources/claude-2026-11-07-to-10.jsonl) and [retrieval audit](sources/claude-2026-11-07-to-10-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the #50 item 2 increment as recommended?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 714, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-09T15:02:34.385Z; source line 726; extract record 7*

Accepted, build it

---

**[Claude]** *2026-11-09T15:11:51.480Z; source line 1124; extract record 8*

Built as accepted, with one finding the measurement had missed and one harness slip on my side, both recorded.

**What changed.** The rule branch of `spelledSource` in [DracoGenerator.scala](../src/mods/scala/draco/DracoGenerator.scala) now calls `traitDeclaration(td)` in place of the literal bare trait, and the unused binding beside it is gone. Every generated rule trait reads `trait X extends DracoType`, one line each:

- The 23 corpus files measured before the recommendation (five draco validation rules, three primes rules, Emit, EmissionReceived, five editor rules, eight example rules).
- Two more under the test corpus I had not counted, the forest scenario rules AshBirchAlarm and BirchAlarmReceived, which ScenarioGenTest pins to the Scala committed beside them. The measurement walked src/main and src/mods only.
- TupleFact.scala under src/test, which TupleFactReteTest writes through the engine on every run. The direct run rewrote it, so it is committed with the increment.

NaturalActor, Service and EgoActor keep their bare traits. They have no definition.

**Verification.** The engine was recompiled with scalac into the direct-run overlay and generates all 25 definition-backed files whitespace-identical to the tree. The first full run failed three ScenarioGenTest loading tests because I put the test class directory on the JVM classpath as well as the Runner's runpath, which made the scenario definitions appear at two loader roots. That was a harness error, now noted in the handoff's procedure. The second run, done as the procedure says:

| run | result |
|---|---|
| every discovered suite, compiled classes, current resources on loader roots | 1118 tests / 78 suites, zero failed or aborted |

Every headline matches the DRACO.md table. The tree holds only the intended changes.

**Documents.** The record `draco-git-record/git-record-2026-11-09-1005` cites #50 and its remaining scope; CHANGELOG has one entry; DRACO.md's role paragraph, §5 line and a dated paragraph; the handoff has a new current checkpoint and dated section; the resume prompt a new head. The November 8 fold edits ride here.

Gate first, and paste the grep output:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Expected: 1118 tests / 77 suites, zero failed, every headline as the table. Then confirm the tree shows only the 31 modified files and the new record, and commit:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git status --short
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-11-09-1005 && git commit -F draco-git-record/git-record-2026-11-09-1005 -- src CHANGELOG.md DRACO.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md draco-git-record/git-record-2026-11-09-1005 && git log --oneline -1 && git status --short
```

On your verified commit the #50 thread closes under the re-decided rule: one comment citing the commit, and the closure, when you say so. The next candidate is #47, the stub predicate consolidated, as a design point first.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Run the full gate and, if green, the commit block?” with the buttons “Running it”, “Correct it”. The question is a tool call at source line 1116, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-09T15:12:38.780Z; source line 1128; extract record 9*

Running it

---

**[Claude]** *2026-11-09T15:12:42.204Z; source line 1133; extract record 10*

Standing by for the gate output. When it comes back I'll read the test count and every headline against the table before saying anything about the commit.

---

**[Dev]** *2026-11-09T15:21:07.776Z; source line 1137; extract record 11*

I can't tell if you're waiting for me to run the gate or if you're waiting for an agent to finish.

---

**[Claude]** *2026-11-09T15:21:12.873Z; source line 1142; extract record 12*

I'm waiting for you. No agent or background task is running on my side. The direct run finished earlier and everything I can do without sbt is done.

The next step is yours: run the gate block in your terminal and paste the grep output here, then the status and commit blocks if the gate is green. I'll check the count and the headlines against the table when the output arrives.

---

*(Delegated action — Dev ran the full sbt gate: 1118 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 183 types; Drake.parse scope: 110 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. Source: 2026-11-09T15:24:16.059Z; source line 1146; extract record 13. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-09T15:24:46.390Z; source line 1176; extract record 14*

The gate matches the direct run and the table on every line. Your result is written into the record, the handoff, DRACO.md and the resume prompt, so the commit carries it.

Confirm the tree first. It should show 31 modified files (the engine, 23 corpus rules, two scenario rules, TupleFact, four documents) and the one new record:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git status --short
```

Then commit:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-11-09-1005 && git commit -F draco-git-record/git-record-2026-11-09-1005 -- src CHANGELOG.md DRACO.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md draco-git-record/git-record-2026-11-09-1005 && git log --oneline -1 && git status --short
```

Paste the output. With the hash I fold it into the three continuity documents, and the #50 thread is ready to close on your word: one comment citing the commit, then the closure.

---

*(Delegated action — Dev committed d026cd3, “Render the rule-only trait through traitDeclaration, so every generated rule trait derives DracoType (closes #50 item 2)” (32 files changed, 218 insertions(+), 50 deletions(-)). The paste shows no push. `git status --short` listed 32 paths. Source: 2026-11-09T16:01:48.320Z; source line 1180; extract record 15. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-09T16:02:42.867Z; source line 1234; extract record 16*

d026cd3 is in, 32 files, tree clean after it. The hash is folded into the handoff's current checkpoint and dated section, the resume prompt's state and first task, and DRACO.md's §5 line and dated paragraph. Those three files are the only uncommitted change.

The fold commit, as the November 6 one was:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && printf 'Fold d026cd3 into the handoff, resume prompt and DRACO.md\n\nDocumentation only: the hash of the #50 item 2 increment, committed by Dev on 2026-11-09 after his green gate, recorded in DEVELOPMENT_HANDOFF.md, DEVELOPMENT_RESUME_PROMPT.md and DRACO.md section 5.\n\nCo-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>\n' > /tmp/fold-msg.txt && git commit -F /tmp/fold-msg.txt -- DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md && git log --oneline -1 && git status --short
```

On the tracker, the thread close under the re-decided rule would be one comment on [#50](https://github.com/ejb816/nexonix/issues/50) saying item 2 landed in d026cd3 with the one-line change and the 26 files, then closing the issue as completed. I post nothing until you say so. After that, the backlog is 23 and the next recommendation is #47 as a design point.

---

*(Delegated action — Dev committed d8de334, “Fold d026cd3 into the handoff, resume prompt and DRACO.md” (3 files changed, 16 insertions(+), 18 deletions(-)). The paste shows no push. Source: 2026-11-09T16:11:45.339Z; source line 1238; extract record 17. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-09T16:11:52.477Z; source line 1249; extract record 18*

d8de334 is in and the tree is clean. Every accepted increment is committed; whether d026cd3 and d8de334 are pushed is yours to check.

One decision waits: the #50 thread close. On your word I post one comment on the issue citing d026cd3 and close it as completed. Until then I touch nothing on the tracker.

---

**[Dev]** *2026-11-09T16:19:59.211Z; source line 1253; extract record 19*

Please close #50

---

**[Claude]** *2026-11-09T16:21:29.777Z; source line 1328; extract record 20*

[#50](https://github.com/ejb816/nexonix/issues/50) is closed as completed. The one comment ([link](https://github.com/ejb816/nexonix/issues/50#issuecomment-6745150195)) says item 2 landed in d026cd3 with the one-line engine change and the 26 generated files, and that item 1 landed in July. The tracker shows 23 open issues. This is the first thread close under the §1 rule you re-decided on November 8.

The closure is written into the handoff's current checkpoint, the resume prompt and DRACO.md's §5 line and dated paragraph. Those three files are uncommitted and ride with the next commit, as the November 8 fold rode with d026cd3. Nothing is pushed; d026cd3, d8de334 and whatever else is ahead of origin are yours to push.

The next candidate is [#47](https://github.com/ejb816/nexonix/issues/47), the stub predicate. Today the same four-aspect test lives in four places: TypeLoader's `isStub` by name, DomainBuilder's by definition, Completeness's four inline conditions, and six scripts. The recommendation would be a design point first, where the one predicate lives, before any build.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Bring the #47 design-point recommendation now?” with the buttons “Yes, #47”, “Not now”. The question is a tool call at source line 1320, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-09T16:22:56.012Z; source line 1332; extract record 21*

Yes, #47

---

**[Claude]** *2026-11-09T16:24:58.741Z; source line 1398; extract record 22*

**Recommendation: the stub predicate becomes a global on `TypeDefinition`, `isStub`, and every other site references it.**

The owning issue is [#47](https://github.com/ejb816/nexonix/issues/47). Its November 4 status says the remaining scope is consolidation against current callers, not restoring the old three-site design, and that reads right against the tree today. The invariant "all four role and structure aspects empty" is spelled in five places, none of them the definition that owns the shape:

- TypeLoader's `isStub`, by name: loads the type, then tests the four aspects inline. The engine calls it at two sites in codec eligibility.
- DomainBuilder's `isStub`, by definition, in the mods tier: the same four lines. Four internal sites and eleven test sites call it.
- Completeness's rule: the same four tests as four `con` lines, rendered as four `where` clauses.
- Six scripts: the same test negated by hand as "loaded".
- DerivationResolvable, which the issue named, no longer carries it. It tests ontology membership since October.

**The design point: where the one definition lives.** On `TypeDefinition` itself, as a global `fix isStub (TypeDefinition -> Boolean)` with a lambda value. That is exactly the form DracoAspect and DomainAspect already use for `isEmpty`, the type that owns the shape owning the test of it. In Haskell it is `isStub :: TypeDefinition -> Bool` in the TypeDefinition module, a module-level function. Then:

- Completeness's four conditions become one, `con TypeDefinition.isStub(td)`.
- TypeLoader's `isStub` stays as the by-name convenience the engine uses, its body `TypeDefinition.isStub(loadType(typeName))`. It no longer spells the invariant.
- DomainBuilder's `isStub` goes. Its four internal sites and the eleven test sites say `TypeDefinition.isStub`.
- The six scripts say `!TypeDefinition.isStub(td)`.

Semantics unchanged: the four-aspect conjunction, codec aspect and the `uses` record ignored, as every site has it today. Both stub shapes the system makes, the loader's typeName-only stub and the dictionary's placeholder, are `TypeDefinition(name)` and satisfy it.

What moves: the TypeDefinition, TypeLoader and Completeness trios, the generated Scala of each through the compiled CLIs, DomainBuilder, eleven test lines, six scripts, and one new TypeDefinitionTest case: a bare name is a stub, Null is a stub, any one aspect present is not. No engine change, so no two-run protocol. DracoGenTest pins the three regenerated files and CompletenessRulesTest the rule.

Rejected: naming it `isEmpty` to match the aspects, since a stub has a name and may carry a record, and "stub" is the word every caller and DRACO.md already use. Rejected: defining it as equality with `TypeDefinition(td.typeName)`, which would reclassify a definition carrying only a codec aspect or a record, a semantic change the issue does not ask for.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the #47 design point as recommended?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 1384, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-09T16:36:40.301Z; source line 1402; extract record 23*

Accepted, build it

---

**[Claude]** *2026-11-09T16:41:21.114Z; source line 1553; extract record 24*

Built as accepted, with one thing observed in passing.

**What changed.** [TypeDefinition.drake](../src/main/resources/draco/TypeDefinition.drake) gains a `globals` section with the one line, `fix isStub (TypeDefinition -> Boolean)`, in the form the aspects' `isEmpty` already takes. TypeLoader's by-name `isStub` delegates to it, its local binding gone. Completeness has one condition where it had four. The three JSON files are the parser's reading of their surfaces and the three Scala files the generator's output, on the engine you gated this morning, so no two-run protocol applies. DomainBuilder's own copy is removed and its three sites, the eleven test sites and the six scripts say `TypeDefinition.isStub`. Nothing else spells the four-aspect test. Semantics are unchanged.

TypeDefinitionTest gains one case: a bare name and Null are stubs, a record alone does not authorize a definition, and each aspect present makes one.

**Verification.** Every discovered suite on compiled classes with current resources on the loader roots:

| run | result |
|---|---|
| direct run, test classes on the runpath only | 1119 tests / 78 suites, zero failed or aborted, green first run |

Every headline matches the table. TypeDefinitionTest is the one suite whose count moves. `DrakeCLI check` over src/main/resources is clean.

**Observed, not fixed.** When I compiled the six scripts against the new classes, five compiled and [inspect-type.scala](../src/mods/scala/scripts/inspect-type.scala) did not. It has been broken since the core-tree migration of October 17, passing a Dracon value where the engine takes host Json. The scripts compile under the mods subproject alone, so no gate sees them. Whether to repair or retire it is your call, and I recorded it rather than widening the scope.

Gate first, and paste the grep output. Expect 1119 tests, 77 suites:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Then confirm the tree shows 28 modified files and the new record, and commit:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git status --short
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-11-09-1139 && git commit -F draco-git-record/git-record-2026-11-09-1139 -- src CHANGELOG.md DRACO.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md draco-git-record/git-record-2026-11-09-1139 && git log --oneline -1 && git status --short
```

On your verified commit the #47 thread closes as #50 did, on your word. The backlog would then be 22.

---

*(Delegated action — Dev ran the full sbt gate: 1119 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 183 types; Drake.parse scope: 110 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. Dev committed 929c850, “The stub predicate has one definition: TypeDefinition.isStub, referenced by TypeLoader, Completeness, DomainBuilder and the scripts (closes #47)” (29 files changed, 255 insertions(+), 175 deletions(-)). The paste shows no push. `git status --short` listed 29 paths. Source: 2026-11-09T16:51:09.169Z; source line 1557; extract record 25. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-09T16:51:59.163Z; source line 1611; extract record 26*

929c850 is in, 29 files, tree clean after it. Your gate is the new baseline, 1119 tests / 77 suites, and DRACO.md §1 now leads with it, the d026cd3 gate chained behind. The hash and gate are in the handoff's current checkpoint and dated section, the resume prompt's state and first task. Those three files are the only uncommitted change.

The fold commit, as before:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && printf 'Fold 929c850 into the handoff, resume prompt and DRACO.md\n\nDocumentation only: the hash and gate of the #47 increment, committed by Dev on 2026-11-09 at 1119 tests green, recorded in DEVELOPMENT_HANDOFF.md, DEVELOPMENT_RESUME_PROMPT.md and DRACO.md sections 1 and 5.\n\nCo-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>\n' > /tmp/fold-msg.txt && git commit -F /tmp/fold-msg.txt -- DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md && git log --oneline -1 && git status --short
```

On the tracker, the thread close would be one comment on [#47](https://github.com/ejb816/nexonix/issues/47) saying the predicate landed in 929c850 as `TypeDefinition.isStub` with every caller re-pointed, then closing it as completed. I post nothing until you say so. After that the backlog is 22, and the next recommendation comes from those.

Four commits since this morning are unpushed as far as I can see locally: d026cd3, d8de334, 929c850 and the fold to come.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Post the #47 closing comment and close the issue?” with the buttons “Close #47”, “Not yet”. The question is a tool call at source line 1596, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-09T16:52:13.971Z; source line 1615; extract record 27*

Close #47

---

**[Claude]** *2026-11-09T16:53:17.428Z; source line 1664; extract record 28*

[#47](https://github.com/ejb816/nexonix/issues/47) is closed as completed. The comment ([link](https://github.com/ejb816/nexonix/issues/47#issuecomment-6745941000)) says the predicate landed in 929c850 as `TypeDefinition.isStub` with every caller re-pointed and semantics unchanged. The tracker shows 22 open issues. Two threads closed today under the re-decided rule, #50 and #47.

HEAD is still 929c850, so the fold commit I handed over earlier has not run. The three documents now carry the hash, your gate and both closures, so the fold message should say so. This block replaces the earlier one:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && printf 'Fold 929c850 and the #47 closure into the handoff, resume prompt and DRACO.md\n\nDocumentation only: the hash and gate of the #47 increment (1119 tests green, the new baseline in DRACO.md section 1), the closures of #50 and #47 on the tracker at Dev'"'"'s instruction, and the next task, one recommendation from the 22 open issues.\n\nCo-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>\n' > /tmp/fold-msg.txt && git commit -F /tmp/fold-msg.txt -- DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md && git log --oneline -1 && git status --short
```

Unpushed as far as the local log shows: d026cd3, d8de334, 929c850 and this fold.

The next recommendation would come from the 22 that remain, its owning issue inspected first. The endogenous candidates are #43 (discriminator syntax), #49 (the Evrete activation mode named in Editor.drake), #58 (start and signal dispatch), #59 (literal and destructuring patterns) and #63 (typed dispatch). The roadmaps, the sentient items and the unimplemented scripts are not, and #62's PON half stays suspended. The one defect found today, inspect-type.scala not compiling since October 17, sits under #33 through #36's script territory and is your call.

---

*(Delegated action — Dev committed a6a0f96, “Fold 929c850 and the #47 closure into the handoff, resume prompt and DRACO.md” (3 files changed, 29 insertions(+), 17 deletions(-)). The paste shows no push. Source: 2026-11-09T16:54:15.815Z; source line 1668; extract record 29. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-09T16:56:43.897Z; source line 1741; extract record 30*

**Recommendation: [#63](https://github.com/ejb816/nexonix/issues/63) constraint 4, a core rule in Draco that refuses a domain whose rules declare two fact types related by derivation.**

The owning issue's November 4 status lists what remains of the typed-dispatch design: payload and tag relationships, closedness, and "an inheritance-related fact-set lint". The lint is the one piece that is a bounded increment rather than a grammar, and it has a measured defect behind it. SubtypeFactVisibilityTest's finding is the one categorical row left in the DRACO.md table: when one Evrete knowledge declares two fact types from one hierarchy, the engine resolves neither, skips the insert, and nothing throws. The §6 gotcha says the corpus is safe only by shape. I measured that shape today over the whole corpus:

| measured | result |
|---|---|
| domains with rules | 9 of 25 |
| rules in them | 23 |
| domains declaring derivation-related fact types | 0 |

Each medium declares two siblings, Draco a disjoint taxonomy, the Editor six request types whose shared parents Latent and Request are declared by no rule. Nothing guards that. The next rule anyone writes over `Request` beside the five intent rules would quietly disable the Editor.

**The design point:** the guarantee is a definition-time rule, not a runtime check, owned by draco the way Uniqueness is. `draco.FactDisjointness` joins `var d DomainType` with `var o DomainOntology` in the validation session the Editor already fires. For the domain it collects the fact types every rule member's pattern variables declare, and inserts a Problem naming the domain and the pair when one declared type is an ancestor of another through the ontology's derivation edges. A rule's variable type is text, so the rule reads it as an identity the way the resolution record was made for: a dotted name is its path, a bare name is looked up in the member's `uses` record by simple name, else it is own-package. That is the same reading AssemblyValidator applies, now in core where main may not call the mods resolver. The ancestor walk is a recursive dyn over `o.derivations`, which the ontology already projects.

What moves: one new trio in Draco, listed as a Draco member and fired by the Editor beside the five core rules, with a FactDisjointnessRulesTest in the form of UniquenessRulesTest: a domain with two siblings passes, a domain declaring a type and its parent is refused naming both, the whole canonical ontology passes. Five per-type tests for the new corpus type, so the parse-scope, surface-loss, aspect-section and GenDrake headlines each move by one, the table updated in the commit. No engine change. The lint fires nowhere in the corpus today, which is the point: it turns the shape into a rule before the shape breaks.

Why this over the other four: #43's discriminator clause is marked FUTURE in drake.dlt with field-versus-derived unsettled, so it needs a spec decision first. #58 needs a neutral signal vocabulary before Pekko's PostStop can leave the engine. #49 and #59 are large designs. This one closes a measured hazard with the machinery already in place.

Rejected: a lint in DomainBuilder's hand-written validate, which the Editor does not run; and a runtime check at knowledge construction, which fires after the definition has already been accepted.

---

## Status

d026cd3 (the rule-only trait through `traitDeclaration`, 1118 tests) and 929c850 (`TypeDefinition.isStub`, 1119 tests) are committed by Dev, with the folds d8de334 and a6a0f96; no paste in this chapter shows a push. The first direct run of the #50 increment was a harness error (the test classes on the JVM classpath), corrected before the second. Dev's "Running it" and the follow-up show a moment in which neither side knew who was waiting. On Dev's instructions "Please close #50" and "Close #47" each issue was closed with one comment; 22 issues remained open. Open at the close and NOT accepted: #63 constraint 4 as a core rule.
