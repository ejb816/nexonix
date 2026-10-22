# Chapter 103 - Sweeping the Canonical Corpus

**Session date:** October 21, 2026
**Topic:** Every remaining main definition and the staged dreams and service definitions are compacted with their records in one pass; the compact form learns that host leaves keep the author's spelling, that only names unique across ontology and vocabulary are shortened, and that anchor parameters follow the anchor convention. Committed as d279709 after two gate runs.

**Provenance:** Original Claude Code session `72019384-f3dd-404d-8ef3-7b676c27b714`; see the [bounded extract](sources/claude-2026-10-21-to-22.jsonl) and [retrieval audit](sources/claude-2026-10-21-to-22-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace and the paste wrapper tags. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning and intermediate progress are excluded. Historical command blocks are dialogue, not instructions to execute now.

---

**[Dev]** *2026-10-22T01:45:25.335Z; source line 2530; extract record 41*

I accept sweeping the rest of the canonical corpus in one pass: every remaining main definition and the staged dreams and service definitions compacted with their records, Scala byte-identical and gate-pinned, the example domains left out.

---

**[Claude]** *2026-10-22T02:14:37.137Z; source line 2871; extract record 42*

Built as you accepted it. Nothing is committed.

**The sweep.** 132 definitions are compact with their records: every main definition except the two authored-ahead ones, BodyElement and ActorAspect, whose frozen surfaces the gates compare, and the 24 staged dreams and service definitions. 55 JSON files changed, 50 drake surfaces were re-emitted, and 58 definitions carry a record. The example domains stay out.

**What the sweep taught the compact form.** Its first runs over the whole corpus exposed three rules, each caught by the unscoped-equals-scoped check:

- A host leaf keeps the author's spelling. The compact form never writes a binding's identity into a definition and never shortens one. The previous compact did shorten host names, so eleven definition-backed Scala files regenerate: the host imports gained at 0b8fc03 revert, and Decoder, Encoder and Generated are spelled in full where a host name shadows them.
- An identity is shortened only when its simple name is unique across the ontology and the vocabulary, so a reader holding just the file's record finds it unambiguous and a target table cannot capture it. The draco.rete wrap types are the exception and stay bare, their bare name being their realization.
- An anchor's type parameters follow the anchor's convention, bare in the own package and in full elsewhere, so Nominal still derives `Primal(draco.drake.Text)`.

**Two corrections the sweep forced.** The engine's loader applied a definition's record for every caller, so DomainBuilder and the companions disagreed about one definition and the GenDrake binding stopped matching its held definitions. The record is now read only during a generation. The hand-written draco.Generated bootstrap shadows the Editor's Generated through the wildcard import, so it joins HostBindings' engine bindings, and KnowledgeService's binding now names org.evrete, where the class lives. My own vocabulary had placed it wrongly.

**One mistake of mine, corrected.** Partway through, a restore from HEAD also reverted this increment's uncommitted engine edits, which made one sweep run with the old compact rules. I re-applied the edits, restored the resources once more, and ran the whole sequence from a clean state.

**Verification so far.** From that clean state: the sweep, then every definition verified with unscoped, scoped and pinned generation agreeing on all 132 and both sealed families, the whole main and mods tree compiled, and 31 suites with 775 passed, zero failed or aborted, every headline unchanged, pending mods actors 0. ResolutionRecordTest now pins the agreement over the whole main corpus. No test or corpus type was added. The record, DRACO.md, README, CHANGELOG and the handoff are written.

Expected: **849 tests / 60 suites**, every report-only headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite on the swept corpus: 848 of 849 passed, DomainLineTest failed, every headline unchanged, pending mods actors 0. Delegated action; 2026-10-22T02:18:10.895Z, source line 2875, extract record 43.)*

---

**[Claude]** *2026-10-22T02:22:01.929Z; source line 2989; extract record 44*

Your run failed exactly one test, and every headline was unchanged. DomainLineTest pinned the bootstrap spelling of three swept bodies, Surface, DomainLine and DomainLineOf saying `draco.drake.Text`, exactly as GenDrakeTest had. Its pins now say `Text`, with each definition's record naming `draco.drake.Text` and the anchor parameter unchanged, and the suite passes 2 / 2 against your current compiled classes. No engine or definition changed.

I also ran every suite at once as a check against further missed pins. Three scenario tests failed in that probe, but only because the test resources were not on the loader's roots in my JVM; your run, where they are, passed them. The record and the handoff note both.

Expected: **849 tests / 60 suites**, every report-only headline unchanged, pending mods actors 0.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite again: 849 tests / 60 suites, zero failed or aborted suites, every headline unchanged, pending mods actors 0. Delegated action; 2026-10-22T02:25:45.048Z, source line 2993, extract record 45.)*

---

**[Claude]** *2026-10-22T02:26:15.256Z; source line 3023; extract record 46*

Recorded **849 tests / 60 suites**, zero failed, canceled, ignored, pending or aborted, every headline unchanged, pending mods actors 0. The record carries both runs. DRACO.md §1 names this the verified baseline, and CHANGELOG and the handoff say verified. The change set is 126 files with no renames; every changed path lies inside the pathspecs below.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md draco-git-record/git-record-2026-10-21-2230 src/main/resources src/main/scala src/mods/resources/draco src/mods/scala/draco src/test/scala) && git add -- "${paths[@]}" && git diff --cached --check && git commit -F draco-git-record/git-record-2026-10-21-2230 -- "${paths[@]}" && git push origin main && git status --short --branch
```

Please paste the final status line. With it, the short-name work you asked for on October 20 is complete for the canonical corpus. After the commit I bring the next design point: the example domains, or the return to the formal/actual question that #51 still holds.

---

*(Dev committed and pushed d279709 (126 files), status clean. Delegated action; 2026-10-22T02:27:14.717Z, source line 3031, extract record 47.)*

---

**[Claude]** *2026-10-22T02:28:06.694Z; source line 3063; extract record 48*

Confirmed: d279709 is committed and pushed, main is clean at origin, and 849 / 60 is the verified baseline. With it, the short-name work you asked for on October 20 is complete for the canonical corpus: bodies say `Dracon`, files carry their records, and any reader resolves a file from itself.

That returns the thread to the question #51 still holds, the one Codex raised before the ontology detour and you did not accept: **the formal/actual split**. Today one slot does two jobs. A definition's header, `type Domain(T)` or `type Coordinate(T <: Product)`, puts its formals into TypeName.typeParameters as type forms, a variable or a bound leaf. A reference, `Dictionary(TypeName, TypeDefinition)`, puts its actuals into the same slot. The engine tells them apart by context, and the identity rule, that a name is abstract when any position holds a variable, has to read a declaration as if it were a reference to itself. Method formals already left that arrangement on October 19: a Dynamic carries TypeParameter records, name plus bounds, and its name holds no generic syntax. The type level has not followed, and 23 corpus definitions declare formals.

**Recommendation.** Header formals become TypeParameter records in a new DracoAspect slot, `typeParameters`, the same record a method already uses, with the bounds as type-form trees. TypeName.typeParameters then carries actual arguments only. A definition's own name is keyed bare, which is already how Uniqueness, self-declaration and the ontology key it, so identity does not move. The drake header is unchanged, `type Coordinate(T <: Product)`, since the parser reads the formals into the aspect and the emitter writes them back from it, and references are unchanged. The Scala target assembles `trait Coordinate[T <: Product]` from the aspect exactly as it does a method's bracket today, so the generated Scala is byte-identical and gate-pinned. The engine's five header reads move from the name to the aspect, and the trio migration is one mechanical sweep over the 23 definitions.

The alternative is to leave the two jobs in one slot and keep telling them apart by context, which works today but means a declaration and a reference to it are the same value, and the Haskell target would have to re-derive the formals from an identity.

One smaller item stands beside it for later: the 48 example domains, outside the canonical ontology and still in the unswept form.

---

## Status

Dev's gate on the swept corpus failed its first run on DomainLineTest's bootstrap-spelling pins (848 of 849) and passed its second at **849 / 60**, every headline unchanged, pending mods actors 0; Dev committed and pushed d279709 (126 files). Record-backed: a restore from HEAD mid-increment reverted the uncommitted engine edits, so one sweep ran with the old compact rules before the edits were re-applied and the sweep re-run from a clean state; the record names it. The short-name work Dev asked for on October 20 is complete for the canonical corpus.
