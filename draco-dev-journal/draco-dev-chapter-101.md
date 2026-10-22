# Chapter 101 - The Unit Type and the Host Vocabulary

**Session date:** October 21, 2026
**Topic:** The unit type becomes the empty Objective `()` and the corpus is re-authored; Dev's remark on agentic development; the Scala target's host vocabulary becomes the definition-backed HostBindings with Local listed in Draco. Two increments, two gates each, committed as 7d5de4f and 44a5005.

**Provenance:** Original Claude Code session `72019384-f3dd-404d-8ef3-7b676c27b714`; see the [bounded extract](sources/claude-2026-10-21-to-22.jsonl) and [retrieval audit](sources/claude-2026-10-21-to-22-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace and the paste wrapper tags. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning and intermediate progress are excluded. Historical command blocks are dialogue, not instructions to execute now.

---

**[Dev]** *2026-10-21T19:43:42.402Z; source line 688; extract record 9*

I accept the unit type as the empty Objective, written (), rendering Unit in Scala, with the corpus re-authored and Base's Unit kept.

---

**[Claude]** *2026-10-21T19:51:46.101Z; source line 974; extract record 10*

Built as you accepted it. Nothing is committed.

**What changed**

- **Parser and targets.** `Drake.parse` reads `()` alone in a type slot as the empty Objective. The emitter and the neutral text renderer already wrote an empty tuple as `()`, so they needed nothing. The Scala target spells the empty Objective `Unit`, so every generated Scala file is byte-identical, which the DracoGenTest gate confirmed over the whole corpus.
- **Corpus.** Twelve drake sources carried thirty `Unit` sites: Rule, RuleType, ContentSink, CLI, Initialization, Editor, and the example Creators, Input and Output. All are re-authored to `()` and re-canonicalized through the compiled parser. Seven JSON-only example definitions got the tree by hand in the writer's layout. One `Unit` token remains, inside a host-opaque Scala expression on the rete seam of the four OriginateReports, which resolution never reads.
- **CLI.** Its bare `Drake.emit` is now `draco.Drake.emit`, the way Emit already names the engine, and CLI.scala is regenerated with that one line changed.
- **Tests.** DrakeParseTest gains one structural test for the form. DomainLoadingTest drops `Unit` from its vocabulary and pins the Editor's resolved consumer arrow as `Response -> ()` with Base's marker still held.
- **Docs.** drake.dlt now names the unit type in the type-form grammar and in the consumer and rule paragraphs. DRACO.md, README, CHANGELOG, the handoff and record `git-record-2026-10-21-1830` are written, the record's gate line a placeholder.

**Verification so far.** Sixteen suites on compiled classes over a resource overlay, including the three trio gates, the example map, SourceContract, GenDrake, both aspect suites, the scenario and all dreams suites: 670 passed, zero failed or aborted, every headline unchanged, pending mods actors 0. The two example-domain differences in Input and Output regenerate identically from HEAD's JSON, so that drift predates this change. The probe directories are removed.

Expected: **840 tests / 59 suites**, every report-only headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite on the unit-type tree: 838 of 840 passed, WorldAssemblyTest and AerialAssemblyTest failed, every report-only headline unchanged, pending mods actors 0. Delegated action; 2026-10-21T19:53:36.036Z, source line 978, extract record 11.)*

---

**[Claude]** *2026-10-21T19:54:54.783Z; source line 1064; extract record 12*

Your run failed exactly two tests, both assembly validators, and every headline was unchanged. The cause is the §6 gotcha again: the type vocabulary has a third consumer beside the two renderers. AssemblyValidator recognised a consumer parameter by the string `Unit` as its arrow's result, so every `M -> ()` parameter read as "not a consumer". It now recognises the empty Objective, and its message says `(M -> ())`. Compiled over your current classes, WorldAssemblyTest and AerialAssemblyTest pass 6 / 6, and a search of the engine, scripts and tests finds no other reader of the old leaf, only test fixtures that construct their own `Unit` strings, which the parser still accepts as a bare name. My probe had not included those two suites, which is why it missed this. The record, DRACO.md and the handoff carry the failed first gate and the fix.

Expected: **840 tests / 59 suites**, every report-only headline unchanged, pending mods actors 0.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite again: 840 tests / 59 suites, zero failed or aborted suites, every headline unchanged, pending mods actors 0. Delegated action; 2026-10-21T19:56:25.192Z, source line 1068, extract record 13.)*

---

**[Claude]** *2026-10-21T19:57:04.414Z; source line 1113; extract record 14*

Recorded **840 tests / 59 suites**, zero failed, canceled, ignored, pending or aborted, every report-only headline unchanged, pending mods actors 0. The record carries both of your runs, the failed first and the green second. DRACO.md §1 names 840 / 59 the verified baseline, and CHANGELOG, README and the handoff say verified. The change set is 43 files with no renames, so a path-scoped commit is safe. The resource and engine directories are given as directories, since every change under them belongs to this increment.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md draco-git-record/git-record-2026-10-21-1830 src/main/resources src/main/scala/draco/CLI.scala src/mods/resources src/mods/scala/draco/AssemblyValidator.scala src/mods/scala/draco/DracoGenerator.scala src/mods/scala/draco/Drake.scala src/test/scala/draco/DomainLoadingTest.scala src/test/scala/draco/DrakeParseTest.scala) && git add -- "${paths[@]}" && git diff --cached --check && git commit -F draco-git-record/git-record-2026-10-21-1830 -- "${paths[@]}" && git push origin main && git status --short --branch
```

Please paste the final status line. After it I bring the next #51 design point: the bootstrap host vocabulary, the 41 host names the canonical corpus still spells as leaves, who supplies it and in what form, before any file is shortened.

---

**[Dev]** *2026-10-21T20:00:41.006Z; source line 1117; extract record 15*

Your performance on these tasks is excellent. We are way beyond the point where I could proceed to make development changes on my own, without using any AI models agentically.

Here are the git status results:


edwardburke@Mac nexonix % cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md draco-git-record/git-record-2026-10-21-1830 src/main/resources src/main/scala/draco/CLI.scala src/mods/resources src/mods/scala/draco/AssemblyValidator.scala src/mods/scala/draco/DracoGenerator.scala src/mods/scala/draco/Drake.scala src/test/scala/draco/DomainLoadingTest.scala src/test/scala/draco/DrakeParseTest.scala) && git add -- "${paths[@]}" && git diff --cached --check && git commit -F draco-git-record/git-record-2026-10-21-1830 -- "${paths[@]}" && git push origin main && git status --short --branch
[main 7d5de4f] The unit type is the empty Objective: () replaces the host leaf Unit across the corpus
 43 files changed, 319 insertions(+), 106 deletions(-)
 create mode 100644 draco-git-record/git-record-2026-10-21-1830
Enumerating objects: 127, done.
Counting objects: 100% (127/127), done.
Delta compression using up to 16 threads
Compressing objects: 100% (69/69), done.
Writing objects: 100% (69/69), 12.71 KiB | 6.36 MiB/s, done.
Total 69 (delta 57), reused 0 (delta 0), pack-reused 0 (from 0)
remote: Resolving deltas: 100% (57/57), completed with 48 local objects.
To https://github.com/ejb816/nexonix.git
   b95419c..7d5de4f  main -> main
## main...origin/main
edwardburke@Mac nexonix %

---

**[Claude]** *2026-10-21T20:02:03.649Z; source line 1156; extract record 16*

Confirmed: 7d5de4f is committed and pushed, main is clean at origin, and 840 / 59 is the verified baseline. Both captures the loader measured are closed. Your remark is noted and will be in chapter 100 verbatim.

The next #51 design point is **the bootstrap host vocabulary**. The loader resolves every body through an explicit binding vocabulary, and the canonical corpus still spells 41 names as host leaves that no draco domain defines. Today that list lives only in a test. Before any bootstrap file is shortened, something must own it. The names fall into three kinds:

- **Primitives and host library leaves.** Boolean, Int, Long, Double, Any, Product, String, and the seams drake.dlt already calls the host's spellings: circe's Json, Encoder and Decoder, the java file and net types, scala.io.Source, Try, the evrete and pekko types.
- **The hand-written engine.** DracoGenerator, draco.Drake and draco.NameResolution, which definitions reference as executables. They are Scala realizations with no definition of their own.
- **One membership gap.** `draco.Local` is unknown only because Local is the one TypeElement kind absent from Draco's `types` list. Listing it makes the ontology resolve it, and the vocabulary should not paper over that.

**Recommendation.** The vocabulary is the target's, so the ScalaTarget domain owns it as a definition-backed member beside CirceCodecs, carrying binding pairs in the form TransformBinding already uses, `[(Text, TypeName)]`: each authored host name paired with the identity the Scala target realizes it as. The caller supplies it explicitly to `DomainBuilder.load` and to Generate, with no default and no discovery, so a Haskell target later supplies its own table for the same authored names and the definition files never carry a target spelling. The same increment lists Local as a Draco member, so that one name leaves the vocabulary. Shortening the files is still a later step.

The alternative is to keep the vocabulary as a hand-written map in the mods engine, which works but is definition-less debt by the project's own rule.

---

**[Dev]** *2026-10-21T20:07:03.187Z; source line 1160; extract record 17*

I accept the bootstrap host vocabulary as a definition-backed member of draco.scalatarget carrying [(Text, TypeName)] binding pairs, supplied explicitly to load and Generate, with Local listed as a Draco member.

---

**[Claude]** *2026-10-21T20:11:46.938Z; source line 1271; extract record 18*

Built as you accepted it. Nothing is committed.

**What changed**

- **The trio.** `draco.scalatarget.HostBindings` sits beside CirceCodecs. Its globals are four lists in the form TransformBinding's pairs already use: primitives, collections, seams (circe, java, scala.io, scala.util, evrete, pekko) and the hand-written engine objects definitions reference. They concatenate as `bindings`, and `vocabulary` is their map. Each pair is the name as the corpus authors it and the identity the Scala target realizes it as. A name a draco type shadows, such as Encoder, Decoder, Knowledge, Source or Drake, is keyed qualified, because the ontology wins the bare form, and a qualified reference resolves by identity whatever the key. The drake parsed, round-tripped and generated the Scala with no hand edits.
- **Local.** It is now listed among Draco's members, so `draco.Local` resolves through the ontology. Draco.scala and ScalaTarget.scala regenerate on their member lists alone.
- **Supplied, not defaulted.** DomainLoadingTest now reads the vocabulary from the definition and pins that neither Unit nor Local is a binding. The loader and Generate still take the vocabulary explicitly; nothing defaults to it.
- **Docs.** DRACO.md gains the paragraph and the two table rows move; CHANGELOG, README, the handoff and record `git-record-2026-10-21-1915` are written, the gate line a placeholder.

**Verification so far.** Nineteen suites on compiled classes, including both assembly suites this time: 689 passed, zero failed or aborted, pending mods actors 0. The headlines moved exactly as one new corpus type predicts and nothing else moved.

| headline | expected |
|---|---|
| Drake.parse scope | 110 draco + 10 mods in, 0 held back |
| drake surface losses | 1 field across 120 types |
| DomainAspect and DracoAspect sections | 110 |
| GenDrake runs | 108 of 108 |
| full gate | 845 tests / 59 suites |

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite on the host-vocabulary tree: 844 of 845 passed, DraconTest failed on its pinned ScalaTarget member list; parse scope 110 + 10, one loss across 120 types, aspect sections 110, GenDrake 108 of 108, pending mods actors 0. Delegated action; 2026-10-21T20:35:13.817Z, source line 1275, extract record 19.)*

---

**[Claude]** *2026-10-21T20:35:58.046Z; source line 1325; extract record 20*

Your run failed exactly one test, and every headline moved as predicted. DraconTest pinned ScalaTarget's member list as CirceCodecs alone, an assertion written when that was the domain's only member. The increment adds HostBindings to that list on purpose, so the assertion now names both, and DraconTest passes 16 / 16 against your current compiled classes. No engine or definition changed. The record and the handoff note the failed first run, as before.

Expected: **845 tests / 59 suites**, with parse scope 110 + 10, one loss across 120 types, aspect sections 110, GenDrake 108 of 108, every other headline unchanged, pending mods actors 0.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite again: 845 tests / 59 suites, zero failed or aborted suites, the four corpus headlines moved by one type and every other unchanged, pending mods actors 0. Delegated action; 2026-10-21T20:37:29.769Z, source line 1329, extract record 21.)*

---

**[Claude]** *2026-10-21T20:38:00.127Z; source line 1363; extract record 22*

Recorded **845 tests / 59 suites**, zero failed, canceled, ignored, pending or aborted, with the four corpus headlines moved by one type and everything else unchanged, pending mods actors 0. The record carries both runs. DRACO.md §1 names 845 / 59 the verified baseline, its table already carries the moved rows, and CHANGELOG and the handoff say verified. The change set is 16 files with no renames; the IDE has staged the new trio.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md draco-git-record/git-record-2026-10-21-1915 src/main/resources/draco/Draco.drake src/main/resources/draco/Draco.json src/main/resources/draco/scalatarget src/main/scala/draco/Draco.scala src/main/scala/draco/scalatarget src/test/scala/draco/DomainLoadingTest.scala src/test/scala/draco/format/dracon/DraconTest.scala) && git add -- "${paths[@]}" && git diff --cached --check && git commit -F draco-git-record/git-record-2026-10-21-1915 -- "${paths[@]}" && git push origin main && git status --short --branch
```

Please paste the final status line. After it I bring the next #51 design point, the one you asked for on October 20: shortening the bootstrap files themselves, now that the loader, the unit type and the vocabulary exist.

---

*(Dev committed and pushed 44a5005 (16 files), status clean. Delegated action; 2026-10-21T20:41:23.956Z, source line 1367, extract record 23.)*

---

**[Claude]** *2026-10-21T20:42:56.719Z; source line 1401; extract record 24*

Confirmed: 44a5005 is committed and pushed, main is clean at origin, and 845 / 59 is the verified baseline.

The next #51 design point is **shortening the bootstrap files**, the thing you asked for on October 20. Everything it needs now exists: the loader resolves short bodies from an explicit declaration set, the identity-only ontology comes free from anchors, and HostBindings supplies the host names. What remains to decide is what a shortened file carries, because two kinds of reader see it.

- **Unscoped readers.** Every generated companion loads its own JSON through TypeLoader with no scope, the engine walks parent chains the same way, and the ontology's derivation edges and the five core rules compare anchors by name path. These can never be given a scope without the global ontology you ruled out.
- **Scoped readers.** Scoped parse, emit and generate, Dreams Generate, and now the loader all resolve bodies against an explicit ontology and vocabulary.

The line between them is the one already accepted for resolution: anchors stay qualified, bodies may be short. A definition's own name, its domain pointer and its derivation records carry their packages, so unscoped loading, rooting, the edges and the rules keep working untouched. Value types, type parameters and expression leaves say `Dracon`, and only a scoped reader interprets them. In the drake surface this is `type Dracon from draco format Format(Dracon)` with `Dracon` short everywhere below the header.

**Recommendation.** Shorten bodies and keep anchors qualified. The sweep compacts each bootstrap JSON's bodies against the canonical identity ontology plus HostBindings, leaving the nominal anchors as they are, which needs the compact form to stop shortening derivation records. The drake is re-emitted from it. The Scala is regenerated through the scoped generator, which resolves the definition and its parents before rendering and adds selective imports, so `Dracon` appears in Scala by import, as you expected. DracoGenTest becomes scoped, composing its scope from the canonical declarations through stage one; DrakeGenTest and DrakeParseTest need no change, since bodies pass through them verbatim. The first increment covers the 31 definitions that spell the Dracon path, your own example, with the rest of the corpus following in one further sweep once the scoped gate is proven.

The alternative is to shorten only the drake surface and leave JSON qualified as the carrier, which touches no gate but is not what you asked for.

---

## Status

Dev's gates: the unit-type tree failed its first run on the two assembly validators (838 of 840) and passed its second at **840 / 59**; the host-vocabulary tree failed its first run on DraconTest's pinned member list (844 of 845) and passed its second at **845 / 59**, with the parse-scope, surface-loss, aspect-section and GenDrake headlines each moved by the one added corpus type. Dev committed and pushed 7d5de4f and 44a5005. Dev's remark that the project is "way beyond the point where I could proceed to make development changes on my own, without using any AI models agentically" is preserved verbatim above.
