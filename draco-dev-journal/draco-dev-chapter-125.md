# Chapter 125 - FactDisjointness and the Rete Wrap Types

**Session date:** November 9–10, 2026
**Topic:** The co-declaration hazard becomes a definition-time rule (#63 constraint 4); definitions name the draco.rete wrap types for Evrete's session and knowledge (#49, the vocabulary half).

**Provenance:** Original Claude Code session `1cf64c57-6ed5-4b94-9b2e-faf865e8fdc0`, extract records 31–42; see the [bounded extract](sources/claude-2026-11-07-to-10.jsonl) and [retrieval audit](sources/claude-2026-11-07-to-10-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the #63 fact-type lint as recommended?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 1732, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-09T17:26:38.245Z; source line 1750; extract record 31*

Accepted, build it

---

**[Claude]** *2026-11-09T17:39:53.440Z; source line 2017; extract record 32*

Built as accepted. The first direct run caught three mistakes of mine, all corrected before the second.

**What changed.** [FactDisjointness.drake](../src/main/resources/draco/FactDisjointness.drake) is the new rule in Draco: it joins a DomainType with the DomainOntology and inserts one Problem naming the domain and every pair of declared fact types where one is an ancestor of the other. Its helpers sit where their data is, so the engine is untouched:

- TypeDefinition gains `identity` and `factTypes` beside `isStub`: a rule member's declared fact types read as identities through its resolution record, a dotted name as its path, a bare name through `uses` and then the own package.
- DomainOntology gains `ancestors`, a recursive walk over its derivation edges, and `relatedFactTypes`.
- Draco lists the rule, so its actor's knowledge registers it. The Editor records it in `uses` and fires it beside the five core rules.

Five JSON files are re-canonicalized, five Scala files regenerated, and `DrakeCLI check` is clean over main and mods. The rule passes its own lint. FactDisjointnessRulesTest is the new suite: the canonical ontology yields zero Problems, siblings pass while a type and its parent are refused naming both, and the record wins over the own package for a bare name.

**The first run's three failures.** I had written the split with `java.util.regex.Pattern`, a host class the HostBindings vocabulary does not carry, so every resolution path over TypeDefinition's body refused it, eight tests across five suites. The character class `"[.]"` replaces it, and a definition body names no host class. EditorTest's registered-rule count moved from ten to eleven. And Editor.scala, generated while the overlay was mid-update, spelled the new line bare where the canonical scope spells it in full like the other core rules. All three are recorded.

| run | result |
|---|---|
| first direct run | 1116 passed, 11 failed, all in the increment |
| second direct run, corrected | 1127 tests / 79 suites, zero failed or aborted |

Every headline is as expected, four corpus counts up by one: parse scope 111 + 73, surface losses 0 across 184, aspect sections 111, GenDrake 109 of 109. #63 stays open for its grammar points, so no tracker comment.

Gate first, and paste the grep output. Expect 1127 tests, 78 suites:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Then confirm the tree shows 17 modified files and five new ones, and commit. The new files need adding first:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git status --short
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-11-09-1231 src/main/resources/draco/FactDisjointness.drake src/main/resources/draco/FactDisjointness.json src/main/scala/draco/FactDisjointness.scala src/test/scala/draco/FactDisjointnessRulesTest.scala && git commit -F draco-git-record/git-record-2026-11-09-1231 -- src CHANGELOG.md DRACO.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md draco-git-record/git-record-2026-11-09-1231 && git log --oneline -1 && git status --short
```

---

*(Delegated action — Dev ran the full sbt gate: 1127 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 111 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 109 of 109 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 184 types; Drake.parse scope: 111 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 111 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. Dev committed 995fade, “FactDisjointness: a core rule refuses a domain whose rules declare fact types related by derivation (GitHub #63, constraint 4)” (22 files changed, 920 insertions(+), 16 deletions(-)). The paste shows no push. `git status --short` listed 22 paths. Source: 2026-11-09T18:24:12.197Z; source line 2021; extract record 33. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-09T18:25:26.827Z; source line 2068; extract record 34*

995fade is in, 22 files, tree clean after it. Your gate is the new baseline, 1127 tests / 78 suites, and DRACO.md §1 leads with it, the two earlier gates of the day chained behind. The hash and gate are in the handoff's current checkpoint and dated section and in the resume prompt. Those three files are the only uncommitted change.

The fold commit:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && printf 'Fold 995fade into the handoff, resume prompt and DRACO.md\n\nDocumentation only: the hash and gate of the #63 constraint-4 increment (1127 tests green, the new baseline in DRACO.md section 1), recorded in DEVELOPMENT_HANDOFF.md, DEVELOPMENT_RESUME_PROMPT.md and DRACO.md sections 1 and 5.\n\nCo-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>\n' > /tmp/fold-msg.txt && git commit -F /tmp/fold-msg.txt -- DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md && git log --oneline -1 && git status --short
```

#63 stays open for its grammar points, so no comment goes on it under the rule you set. Three increments landed today, two issues closed, and six commits are ahead of origin as far as the local log shows: d026cd3, d8de334, 929c850, a6a0f96, 995fade and this fold.

The next recommendation would come from the 22 that remain. The endogenous candidates after today are #43 (the discriminator clause, marked future in drake.dlt), #49 (the Evrete activation mode named in Editor.drake and Draco.drake), #58 (start and signal dispatch) and #59 (literal and destructuring patterns), plus #63's own grammar points. Each needs a design point first.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Run the fold commit, then take the next recommendation from the 22?” with the buttons “Yes, both”, “Fold only”. The question is a tool call at source line 2060, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-09T18:32:23.574Z; source line 2072; extract record 35*

Yes, both

---

**[Claude]** *2026-11-09T18:35:18.237Z; source line 2159; extract record 36*

**Recommendation: [#49](https://github.com/ejb816/nexonix/issues/49)'s vocabulary half, the `draco.rete` wrap types named wherever a definition body names Evrete's session or knowledge type today.**

The owning issue's history matters here. Its July attempt was rolled back because it put Evrete configuration into a new main-tree domain, and the review that followed set the principled direction: wrapped *type names* in main with the binding in mods is the right axis, configuration is not. The November 4 status says "distinguish type vocabulary from session configuration". This is the type-vocabulary side alone, and it revives nothing retracted.

What I measured against the tree:

| spelled in definition bodies today | sites |
|---|---|
| `org.evrete.api.StatefulSession`, in full | 12 definitions: Draco, gendrake Emitter, the Editor, TransformBinding, the four example Creators, the four example Consumers |
| `org.evrete.api.Knowledge`, in full | 3: Primes, the Editor, TransformBinding |
| references to the wrap type `draco.rete.StatefulSession` | 0 |

Meanwhile Rule and RuleType already name the wrap types `Knowledge` and `RhsContext` bare, recorded in `uses`, and the generator realizes a draco.rete wrap type as the Evrete class through its host-import table. So the vocabulary and its realization exist; twelve definitions simply do not use them. The probe through the compiled CLIs on a scratch copy confirms the one gap: a bare `StatefulSession` reaches the Scala verbatim with no import, because the host-import table has rows for Knowledge, KnowledgeService and RhsContext and none for StatefulSession, and HostBindings keys StatefulSession bare where it keys Knowledge qualified, the convention for a name a draco type shadows.

**The increment.** One engine row, the import for StatefulSession in the host-import table. HostBindings re-keys its StatefulSession entry qualified, as Knowledge is. The twelve definitions re-author the type as `StatefulSession` and the three as `Knowledge`, each recording `draco rete StatefulSession` or `draco rete Knowledge` in `uses`, exactly as Rule does. JSON re-parsed, Scala regenerated: each file changes one declared type to the bare name and gains the import. After it, every Evrete type named in a definition body is a draco.rete wrap type. The two-stage loader then resolves the bare names to the wrap types, which the direct run checks through DomainLoadingTest and ExampleLoadingTest.

**What stays, deliberately.** `ActivationMode.CONTINUOUS` in Draco and the Editor, `new KnowledgeService()` in Rule, and the session methods. Those are configuration and glue, the HOW the review said not to quarantine one line at a time. The wrap types gain no capability declarations here. That is #49's large track, and it wants its own deliberate start after the names are in place.

Why this over the other candidates: #43's discriminator clause is marked future in drake.dlt with an unsettled question, #58 needs a neutral signal vocabulary first, #59 and #63's grammar points are grammar designs. This one completes a migration already two-thirds done, with the mechanism proven on Knowledge and RhsContext, and it moves the README's "leaves" residue row.

Rejected: a new domain or any session-construction wrapper, the shape that was rolled back; and leaving the enum sites unchanged while introducing a `Session` capability, which would be the speculative surface the issue warns against.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the rete wrap-type spelling as recommended?” with the buttons “Accepted, build it”, “Correct it”. The question is a tool call at source line 2146, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-09T19:12:43.454Z; source line 2163; extract record 37*

Accepted, build it

---

**[Claude]** *2026-11-09T19:17:39.998Z; source line 2306; extract record 38*

Built as accepted, green on the first direct run.

**What changed.** Fourteen definitions re-authored, one engine row, one host-binding key:

- The twelve actor `start` blocks that spelled `org.evrete.api.StatefulSession` now say `StatefulSession`, and the three that spelled `org.evrete.api.Knowledge` say `Knowledge`, each recording the wrap type in `uses` exactly as Rule does. Draco and the four example Consumers gain their first `uses` section.
- The generator's host-import table gains the StatefulSession row in [DracoGenerator.scala](../src/mods/scala/draco/DracoGenerator.scala), the one engine line.
- HostBindings keys StatefulSession qualified, as it keys Knowledge, the convention for a name a draco type shadows.

Fourteen JSON files are the parser's reading of their surfaces, fourteen Scala files the generator's output on the recompiled engine: each declared type goes bare and the file gains its import, with no duplicate anywhere. `DrakeCLI check` is clean over main and mods. Every Evrete type a definition body names is now a `draco.rete` wrap type. The ActivationMode enum, KnowledgeService and the session methods stay as host glue, the configuration track #49 keeps open.

| run | result |
|---|---|
| direct run, every discovered suite | 1127 tests / 79 suites, zero failed or aborted, green first run |

Every headline is unchanged. No test added or removed. The README's residue row and bullet for "the leaves" now say the rule-engine types are wrap types and the actor-library types remain.

Gate first, and paste the grep output. Expect 1127 tests, 78 suites:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Then confirm the tree shows 48 modified files and the new record, and commit:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git status --short
```

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add draco-git-record/git-record-2026-11-09-1415 && git commit -F draco-git-record/git-record-2026-11-09-1415 -- src CHANGELOG.md DRACO.md README.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md draco-git-record/git-record-2026-11-09-1415 && git log --oneline -1 && git status --short
```

#49 stays open, so no tracker comment under the rule you set.

---

*(Delegated action — Dev ran the full sbt gate: 1127 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 111 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 109 of 109 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 184 types; Drake.parse scope: 111 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 111 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. Dev committed 2804904, “Definitions name the draco.rete wrap types for Evrete's session and knowledge; StatefulSession gains its import row (GitHub #49, the vocabulary half)” (49 files changed, 338 insertions(+), 63 deletions(-)). The paste shows no push. `git status --short` listed 49 paths. Source: 2026-11-09T20:02:46.411Z; source line 2310; extract record 39. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-09T20:03:34.661Z; source line 2347; extract record 40*

2804904 is in, 49 files, tree clean after it. Your gate stays the baseline at 1127 tests / 78 suites, and DRACO.md §1 now leads with this increment, the FactDisjointness gate chained behind at the same count. The hash and gate are in the handoff's current checkpoint and dated section and in the resume prompt. Those three files are the only uncommitted change.

The fold commit:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && printf 'Fold 2804904 into the handoff, resume prompt and DRACO.md\n\nDocumentation only: the hash and gate of the rete wrap-type increment (1127 tests green, GitHub #49 vocabulary half), recorded in DEVELOPMENT_HANDOFF.md, DEVELOPMENT_RESUME_PROMPT.md and DRACO.md sections 1 and 5.\n\nCo-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>\n' > /tmp/fold-msg.txt && git commit -F /tmp/fold-msg.txt -- DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md && git log --oneline -1 && git status --short
```

Four increments landed today, two issues closed, two advanced and left open. Eight commits are ahead of origin as far as the local log shows: d026cd3, d8de334, 929c850, a6a0f96, 995fade, a3b52a7, 2804904 and this fold.

The next recommendation would come from the 22 that remain. After today the endogenous candidates are #43 (the discriminator clause, marked future in drake.dlt), #58 (start and signal dispatch, needing a neutral signal vocabulary), #59 (literal and destructuring patterns), #63's grammar points and #49's configuration track. Each begins with a design point for you.

---

*(Delegated action — Dev committed 3e87527, “Fold 2804904 into the handoff, resume prompt and DRACO.md” (3 files changed, 20 insertions(+), 14 deletions(-)). The paste shows no push. Source: 2026-11-10T15:23:49.529Z; source line 2357; extract record 41. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-10T15:25:49.366Z; source line 2443; extract record 42*

**Recommendation: bound the host text left in definition values. Re-author the three leaves that are not at a target seam, and assert the rest as a named seam list.**

**Owning issue.** No open issue owns this exactly, and I am not asking for a new one. The nearest is [#59](https://github.com/ejb816/nexonix/issues/59), which names this tail as the metric that must not move the wrong way, and whose one remaining client in the definition corpus is among the leaves below. #61, which retired the host-opaque expression forms, is closed. The README's residue row "host-opaque value strings" is what this would bound. The increment would not close #59.

**What I measured.** I looked first at the five candidates. #59's literal and destructuring patterns have exactly one client in the definitions, a tuple destructure in Dracon; the rest of that issue is about self-hosting the hand-written engine. #58 and #43 have no client at all: no actor needs a second signal, and no definition carries a discriminator. So I measured the residue directly, every string leaf under a `value` in the main and mods corpus, PON excluded:

| measured | count |
|---|---|
| value string leaves | 1984 |
| leaves carrying host syntax: a Scala lambda, `try`, `new`, a block | 23, in 13 definitions |
| of those, at a target seam | 20, in 11 definitions |
| of those, in ordinary definitions | 3 |

The seams are where host realization is the content: the Dracon wrapper, CirceCodecs, the DefinitionPath carrier, Rule's knowledge service, the circe decode in CLI and TypeLoader, the runtime lookup in the four OriginateReports, and TransformBinding's guarded run. The three outside them are a Scala lambda in ActorAspect's `isEmpty`, and a `try … finally` in CLI and in SourceContent.

**The increment.**

- ActorAspect's `aa => …` becomes the drake lambda `\aa -> …`, as DracoAspect already writes its own. The probe through the compiled CLIs regenerates its Scala byte-identical.
- CLI's and SourceContent's `try … finally` become `bracket`, the form TypeLoader has used since September. The probe changes one line of Scala in each, the same try and finally around a bound source, and all three compile.
- One new test turns the measurement into a gate: every host-syntax value leaf must sit in a definition on a named seam list. A new one anywhere else fails naming the path and the leaf. The list starts at the eleven definitions above and is meant to shrink.

That makes DRACO.md's statement "`=>` is never a drake token" true outside the seams, gives the README residue row a number that cannot grow silently, and follows the pattern of the seven measurements you have already turned into assertions. No engine change, no headline moves, one test added.

Rejected: re-authoring the seam leaves themselves in this increment. Each is a seam decision of its own, and the destructuring one is #59's grammar.

---

## Status

995fade (FactDisjointness, 1127 tests) and 2804904 (the rete wrap types, 1127 tests) are committed by Dev, with the fold 3e87527; no paste in this chapter shows a push. The first direct run of the FactDisjointness increment failed eleven tests of the increment's own making, corrected before the second. Dev's "Yes, both" answers the next-step question to run the fold commit and then take the next recommendation. #63 and #49 stay open. Open at the close and NOT accepted: bounding the host text left in definition values.
