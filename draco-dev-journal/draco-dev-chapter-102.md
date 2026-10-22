# Chapter 102 - Shortening the Dracon Referrers and the Resolution Record

**Session date:** October 21, 2026
**Topic:** The seventeen main definitions that spell the Dracon path get compact bodies with anchors kept qualified, which forces scoped generation to canonicalize rather than resolve; then a definition carries its resolution record, `uses`, so an unscoped reader resolves the file from itself. Committed as 0b8fc03 after three gate runs and 773b6d9 after one.

**Provenance:** Original Claude Code session `72019384-f3dd-404d-8ef3-7b676c27b714`; see the [bounded extract](sources/claude-2026-10-21-to-22.jsonl) and [retrieval audit](sources/claude-2026-10-21-to-22-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace and the paste wrapper tags. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning and intermediate progress are excluded. Historical command blocks are dialogue, not instructions to execute now.

---

**[Dev]** *2026-10-21T20:43:58.602Z; source line 1405; extract record 25*

I accept shortening the bootstrap files' bodies against the canonical identity ontology and HostBindings, with nominal anchors kept qualified, the Scala regenerated through the scoped generator with selective imports, DracoGenTest made scoped, starting with the definitions that spell the Dracon path

---

**[Claude]** *2026-10-21T21:20:28.884Z; source line 1922; extract record 26*

Built as you accepted it, with one finding that reshaped the engine side. Nothing is committed.

**The sweep.** The seventeen main definitions that spelled the Dracon path now carry compact bodies, `[Dracon]`, `Presence(TypeName)`, `Absent()`, with every nominal anchor still qualified: the own name, the domain pointer and the derivation records. The companions, the engine's parent chains and the ontology's edges read them unscoped as before. The compact form itself now keeps anchors in full under every policy, and drake.dlt records the rule.

**The finding.** Resolving a definition in full before generating it, which is what the scoped generator did until today, breaks the engine. The engine keys several decisions on conventional bare spellings: the codec's optional-field elision, the payload check, the actor-minting factory, and the own-package lookup it uses to judge a referenced type's codec. The first scoped gate failed twelve per-type comparisons that way. The scoped generator therefore canonicalizes instead. It takes the compact form, re-qualifies only what the engine cannot read as a bare name, generates, and shortens exactly those identities back by selective import where Scala's own scoping allows. The rule for "readable" took four iterations to get right, each caught by compiling the whole tree: a host-table name is read bare only when the host realizes it, so Decoder and Encoder in the Dracon domain stay qualified; a table key is never bare in its own package, because the standard circe import outranks a same-package member; and an own-package name shadowed by a hand-written class through a wildcard import stays qualified, which is how `draco.dreams.editor.Generated` collides with the bootstrap `draco.Generated`. The actor-minting factory now accepts `draco.ActorType` by identity.

**The consequence.** Because generation no longer depends on how an author spelled a resolvable name, every definition-backed Scala file was regenerated through the scoped generator. 35 changed, gaining imports such as `import draco.format.dracon.Dracon` and `import org.evrete.api.ActivationMode`; the other 85 were byte-identical. Every suite that generates corpus Scala now supplies the scope explicitly through a new CanonicalScope test helper, since the unscoped engine misreads a swept body's referrers.

**Verification so far.** 29 suites on compiled classes, 770 passed, zero failed or aborted, every headline unchanged, pending mods actors 0. The entire main and mods tree compiles against the regenerated classes. No test or corpus type was added. The record, DRACO.md, README, CHANGELOG and the handoff are written.

Expected: **845 tests / 59 suites**, every report-only headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite on the bootstrap-shortening tree: 842 of 845 passed, the three primes rules failed in DracoGenTest, every headline unchanged, pending mods actors 0. Delegated action; 2026-10-21T21:23:48.881Z, source line 1926, extract record 27.)*

---

**[Claude]** *2026-10-21T21:26:22.133Z; source line 1996; extract record 28*

Your run failed exactly three tests, the primes rules in DracoGenTest, and every headline was unchanged. The cause was a pre-existing practice, not the sweep: PrimesRulesTest generates those three rules unscoped and writes the output into src/main/scala, so in your run it overwrote the regenerated files before DracoGenTest compared them. In my probe the two suites happened to run in the other order. Two things changed:

- **PrimesRulesTest now generates through the canonical scope**, like every other suite that produces corpus Scala, so what it writes is what the gate expects.
- **Integer, System and Enumeration are bound bare in HostBindings.** They had java.lang and scala identities, which made the scoped generator add `import java.lang.Integer` to the rules it regenerated. The Scala target reads those names bare, as it reads Seq and Map, so the bindings now say so. With that, the three rules regenerate byte-identically to the committed files, and no regenerated file carries such an import.

DracoGenTest, PrimesRulesTest, DomainLoadingTest, DrakeGenTest, DrakeParseTest and GenerateAndCompileTest pass 571 / 571 on compiled classes, the changed Scala compiles, and the record, DRACO.md and the handoff carry the failed run and the fix.

Expected: **845 tests / 59 suites**, every report-only headline unchanged, pending mods actors 0.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev's second run did not compile: three `not found: value CanonicalScope` errors in PrimesRulesTest. Delegated action; 2026-10-21T21:27:07.266Z, source line 2000, extract record 29.)*

---

**[Claude]** *2026-10-21T21:27:25.817Z; source line 2030; extract record 30*

A compile error of my making: PrimesRulesTest lives in package draco.primes and imports draco's names one by one, so the unqualified helper did not resolve. The three calls now say `draco.CanonicalScope.scope`, and the file compiles against your current classes with no errors. Nothing else changed since your last run.

Expected: **845 tests / 59 suites**, every report-only headline unchanged, pending mods actors 0.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite a third time: 845 tests / 59 suites, zero failed or aborted suites, every headline unchanged, pending mods actors 0. Delegated action; 2026-10-21T21:28:59.440Z, source line 2034, extract record 31.)*

---

**[Claude]** *2026-10-21T21:29:34.181Z; source line 2069; extract record 32*

Recorded **845 tests / 59 suites**, zero failed, canceled, ignored, pending or aborted, every headline unchanged, pending mods actors 0. The record carries all three of your runs. DRACO.md §1 names this the verified baseline, and CHANGELOG and the handoff say verified. The change set is 97 files with no renames; every changed path lies inside the pathspecs below, and the IDE has staged the two new files.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md draco-git-record/git-record-2026-10-21-2000 src/main/resources src/main/scala src/mods/scala/draco src/test/scala) && git add -- "${paths[@]}" && git diff --cached --check && git commit -F draco-git-record/git-record-2026-10-21-2000 -- "${paths[@]}" && git push origin main && git status --short --branch
```

Please paste the final status line. After it I bring the next #51 design point: the rest of the sweep, or first the question it raised, whether a shortened file should carry its own resolution record so an unscoped reader can resolve it, as Haskell and Scala files carry imports.

---

*(Dev committed and pushed 0b8fc03 (97 files), status clean. Delegated action; 2026-10-21T21:30:10.930Z, source line 2073, extract record 33.)*

---

**[Claude]** *2026-10-21T21:31:07.418Z; source line 2104; extract record 34*

Confirmed: 0b8fc03 is committed and pushed, main is clean at origin, and 845 / 59 is the verified baseline.

The next #51 design point is **what a shortened file knows about itself**. The sweep exposed the shape of the problem. A swept definition's bodies say `Dracon`, and only a reader holding the ontology can say which Dracon. Today's unscoped readers survived because the first seventeen files are read by scoped paths, and the engine now canonicalizes. But three paths still generate unscoped: the GeneratorCLI's generate command, DomainBuilder's whole-domain generate, and the CLI definition's own generate. Each will misread a swept referrer, and the rest of the sweep makes every file a swept one. Giving each of those paths a declaration set and vocabulary is heavy, and it would push the ontology into every command line.

This is the problem every module language solves the same way: the file carries its imports. Haskell writes `import Draco.Format.Dracon (Dracon)`, Scala writes `import draco.format.dracon.Dracon`, and the reader resolves the module from the file alone. You asked on October 20 how short names would work in Haskell. This is the answer: the record projects straight onto the module's import list.

**Recommendation.** A definition carries a **resolution record**, a `uses` list of the identities its bodies name by their short form. The compact step writes it, since it already knows exactly which identities it shortened, so the record is derived, never hand-maintained. Authoring validates it against the ontology, so a stale entry is a Problem. Any reader, scoped or not, qualifies the file's bodies from the record alone: the short leaf `Dracon` is the one `uses` entry with that simple name, and compact only ever shortens a name that is unique, so the record cannot be ambiguous. In drake it is a bracketed name list after the header, written in the anchor spelling, `draco format dracon Dracon`; in the carrier it is a list of TypeName records; it is empty and elided for a file that spells everything in full, so the unswept corpus does not change. The rest of the sweep follows once the record exists, and the three unscoped paths need no scope of their own.

The alternative is to finish the sweep first and give every remaining generation path an explicit scope, which works but makes the ontology a prerequisite for reading any file at all.

---

**[Dev]** *2026-10-21T21:32:37.696Z; source line 2108; extract record 35*

I accept the resolution record: a definition carries a `uses` list of the identities its bodies name short, written by compact and validated against the ontology, so an unscoped reader resolves the file from itself; the rest of the sweep follows it.

---

**[Claude]** *2026-10-21T21:46:38.880Z; source line 2438; extract record 36*

Built as you accepted it. Nothing is committed.

**What landed**

- **The carrier.** TypeDefinition gains `uses`, a list of TypeNames, default empty and elided, beside the type's own name. Its trio is regenerated. The loader's rooting step and the generator's target-type rebuild now carry the slot. The first did not, and the record silently never reached the engine until it did, which is the §6 rule about rebuilds once more.
- **The surface.** `uses [ ... ]` is a section of its own between the type section and the domain line, one entry per line in the anchor spelling, `draco format dracon Dracon`. It is elided when empty, and `uses` is a reserved word. I first emitted it indented inside the type section, which broke the Draco-aspect probe's section cut, so it stands on its own.
- **The resolver.** The compact form writes the record: every ontology identity a body names short, outside the definition's own package, sorted. Every scoped read validates it, so an entry the ontology does not define is an error. A resolved definition carries no record, since every name is in full and compact recomputes it. A recorded scope, built from the file's own list with no bindings, is lenient: a leaf the record does not name passes as written.
- **The engine.** Unscoped generation of a recorded definition goes through that scope and shortens by import exactly as scoped generation does, and the loader does the same for a recorded definition it loads with no scope in effect. The unscoped GeneratorCLI, which failed on TypeDefinition at the start of this build because it loaded the swept TypeName, now produces the pinned Scala for TypeName, TypeDefinition, Problem and CirceCodecs.
- **Tests and docs.** ResolutionRecordTest is the 60th suite with four tests. The seventeen swept files are re-swept and carry their records. drake.dlt, DRACO.md, README, CHANGELOG, the handoff and record `git-record-2026-10-21-2130` are written.

**Verification so far.** 29 suites on compiled classes, 766 passed, zero failed or aborted, every headline unchanged, pending mods actors 0. The whole main and mods tree compiles against the regenerated classes. The one remaining asymmetry is documented: an unswept file spelled in full gains its imports only under a scope, which the rest of the sweep removes.

Expected: **849 tests / 60 suites**, every report-only headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite on the resolution-record tree, green on its first run: 849 tests / 60 suites, zero failed or aborted suites, every headline unchanged, pending mods actors 0. Delegated action; 2026-10-22T01:34:08.746Z, source line 2442, extract record 37.)*

---

**[Claude]** *2026-10-22T01:35:06.575Z; source line 2473; extract record 38*

Recorded **849 tests / 60 suites**, zero failed, canceled, ignored, pending or aborted, every headline unchanged, pending mods actors 0, green on its first run. DRACO.md §1 names this the verified baseline, and the record, CHANGELOG and handoff say verified. The change set is 50 files with no renames; every changed path lies inside the pathspecs below.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md draco-git-record/git-record-2026-10-21-2130 src/main/resources src/main/scala src/mods/scala/draco src/test/scala) && git add -- "${paths[@]}" && git diff --cached --check && git commit -F draco-git-record/git-record-2026-10-21-2130 -- "${paths[@]}" && git push origin main && git status --short --branch
```

Please paste the final status line. After it I bring the next #51 design point: the rest of the sweep, now that a recorded file resolves itself.

---

*(Dev committed and pushed 773b6d9 (50 files), status clean. Delegated action; 2026-10-22T01:37:44.547Z, source line 2481, extract record 39.)*

---

**[Claude]** *2026-10-22T01:38:42.974Z; source line 2522; extract record 40*

Confirmed: 773b6d9 is committed and pushed, main is clean at origin, and 849 / 60 is the verified baseline.

The next #51 design point is **the rest of the sweep**. Seventeen of the 110 main definitions carry compact bodies and records; the other 93, and the 25 staged dreams and service definitions with pinned Scala, still spell their cross-package names in full. That leaves the one asymmetry the record did not remove: an unswept file gains its imports only under a scope, because scoped generation canonicalizes while unscoped generation keeps the author's spelling. Every gate therefore still names the canonical scope, and the GeneratorCLI still produces different Scala from the file than the gate pins.

What the full sweep does and does not change is now known rather than guessed. Scoped generation already compacts before it generates, so the pinned Scala of every file was produced from its compact form. Sweeping a file writes that same compact form into its JSON, with its record, and re-emits its drake; the Scala is byte-identical by construction, and DracoGenTest proves it. After the sweep, unscoped and scoped generation agree on every file, so the ontology is needed only to author and validate, and the test scope becomes a convenience rather than a necessity.

**Recommendation.** Sweep the whole canonical corpus in one mechanical pass: the 93 remaining main definitions and the 25 staged dreams and service definitions, each compacted against the canonical identity ontology and HostBindings with its record written, drake re-emitted, Scala untouched and gate-pinned. The 48 example domains stay out, since they live outside the canonical ontology, are report-only, and already drift in 20 of 48 files. No engine or test changes are expected; the only risk is a file whose round trip the sweep exposes, which the three trio gates would show.

The alternative is domain by domain, which costs a gate and a commit per domain for no added safety, since each file is already pinned.

---

## Status

Dev's gates: the bootstrap-shortening tree failed its first run on three primes rules overwritten by the unscoped PrimesRulesTest (842 of 845), did not compile on its second (an unqualified helper reference), and passed its third at **845 / 59**; the resolution-record tree passed its first at **849 / 60**, ResolutionRecordTest being the 60th suite. Dev committed and pushed 0b8fc03 (97 files) and 773b6d9 (50 files). The engine lesson of the chapter: scoped generation must spell a definition canonically — compact, then qualified only where the engine cannot read a bare name — because the engine keys codec elision, the payload check and the actor-minting factory on bare spellings; and a resolution record lets the unscoped paths read a swept file.
