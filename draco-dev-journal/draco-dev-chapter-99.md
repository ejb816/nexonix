# Chapter 99 - Ontology-Scoped Names and the Next Handoff

**Session date:** October 20–21, 2026
**Topic:** Explicit operation ontology, local-first lookup and binding vocabulary; verified scoped APIs and the still-unaccepted bootstrap loading proposal.

**Provenance:** Original Codex session `01a1404f-0fa1-7f03-abd0-fff9bf4b100b`; see the [bounded extract](sources/codex-2026-10-15-to-21.jsonl) and [retrieval audit](sources/codex-2026-10-15-to-21-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace and repository-link normalization. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning and intermediate progress are excluded. Historical command blocks are dialogue, not instructions to execute now.

---

**[Dev]** *2026-10-20T14:18:36.366Z; source line 2123; extract record 57*

The full type name "draco.format.dracon.Dracon" has propagated pervasively throughout the text in the .drake, .json and .scala files. Is there no way to use the DracoOntology to allow that type name to be referenced by just "Dracon" over the the scope of drake sources and JSON? It should be resolvable in Scala by import statements, but I'm not sure how that would work in Haskell.

---

**[Codex]** *2026-10-20T14:18:59.253Z; source line 2136; extract record 58*

Yes. **The domain ontology can supply the scope in which `Dracon` resolves to its full identity.** Today’s `DomainOntology` records membership and relationships, but the parser and generator do not use it as a general short-name resolver. The repeated qualification compensates for that missing capability.

I recommend **resolving short type names against an explicitly supplied ontology**:

- Drake and JSON can say `Dracon`.
- Resolution identifies the uniquely matching type in that ontology.
- Missing or ambiguous names produce an error; qualification remains available to disambiguate.
- Generation uses the resolved identity to choose target imports and spelling.

That keeps the full identity without repeating its path at every reference. It also means a JSON definition containing short names needs its ontology context to be interpreted reliably.

Scala could emit:

```scala
import draco.format.dracon.Dracon
```

Haskell supports the same approach through selective imports. For an illustrative target module mapping:

```haskell
import Draco.Format.Dracon (Dracon)
```

The type can then appear simply as `Dracon`; qualified imports or module aliases handle collisions. The exact module mapping would belong to the Haskell target. [Haskell module specification](https://www.haskell.org/onlinereport/haskell2010/haskellch5.html)

**Accept or correct ontology-scoped resolution as the next design point.** It addresses the repetition you’ve identified before we proceed with separating type-level formals and actuals.

---

**[Dev]** *2026-10-20T14:23:08.133Z; source line 2143; extract record 59*

I accept ontology-scoped resolution, before we proceed with the previous recommendation.

---

**[Codex]** *2026-10-20T14:23:13.916Z; source line 2146; extract record 60*

The next design point is **which ontology supplies the resolution scope**.

I recommend using the **explicitly supplied DomainOntology for the operation**. In Dreams, that is the Editor’s held ontology; standalone parsing and generation receive one from their caller.

Within that scope, `Dracon` must identify exactly one type. Missing or ambiguous matches fail; a qualified name selects the exact identity. Resolution never depends on classpath discovery or iteration order.

Accept or correct this scope before we build it.

---

**[Dev]** *2026-10-20T14:26:24.706Z; source line 2153; extract record 61*

I accept using the **explicitly supplied DomainOntology for the operation**.

---

**[Codex]** *2026-10-20T14:26:39.272Z; source line 2158; extract record 62*

The next design point is **distinguishing locally declared names from ontology references**.

I recommend resolving names in this order:

1. The definition’s own name and its in-scope type parameters, including method formals.
2. Named types in the supplied ontology, requiring a unique match.

Existing type operators—such as `[T]`, `{K, V}` and `A -> B`—retain their meaning; resolution applies to the names inside them.

Thus `T` in `Encoder(T)` remains the declared parameter, while `Encoder` and `Dracon` resolve through the ontology. This also lets a new definition refer to itself before it has been added to the ontology.

Accept or correct this local-binding rule.

---

**[Dev]** *2026-10-20T17:22:47.997Z; source line 2165; extract record 63*

I accept your recommended name resolution

---

**[Codex]** *2026-10-20T17:23:26.826Z; source line 2184; extract record 64*

Tracing the call paths exposed one remaining boundary: **existing primitive and host-binding names**. The ontology does not define every name currently used—for example, `String`, `Int`, and a wrapper’s `io.circe.Json`.

I recommend keeping those meanings in an **explicit binding vocabulary supplied alongside the ontology**. Local declarations resolve first, ontology types second, and declared primitive/host bindings last. An unresolved name still fails; there is no classpath lookup or automatic acceptance of dotted names.

Thus `Dracon` resolves through the ontology, while `Json` can resolve through the Circe wrapper’s declared binding. This also gives each target—including Haskell—its own realization without putting target names back into ordinary definitions.

Accept or correct this binding boundary before I wire resolution into the operations.

---

**[Dev]** *2026-10-20T18:56:06.428Z; source line 2191; extract record 65*

I accept keeping those meanings in an **explicit binding vocabulary supplied alongside the ontology**.

---

**[Codex]** *2026-10-20T19:08:02.112Z; source line 2454; extract record 66*

Implemented ontology-scoped resolution for Drake, JSON, Scala generation, and Dreams Generate. Short names such as `Dracon` resolve through the supplied context; missing or ambiguous names fail.

Direct compiled suite: **835 passed**, all report-only figures unchanged. Bootstrap files remain qualified pending explicit loader scope.

Please run the final gate—expected **835 tests / 58 suites**:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
set -o pipefail
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
grep "mods actors pending" target/test-output/DrakeGenTest.log
```

After verification and commit, the next design point is supplying the bootstrap loader’s scope so repository definitions can adopt short names.

*(Dev ran the full suite: 835 tests / 58 suites, zero failures or aborted suites; reported headlines matched the increment’s baseline. The file-only pending mods-actor count was 0. Delegated action; 2026-10-20T21:27:29.548Z, source line 2461, extract record 67.)*

---

**[Codex]** *2026-10-20T21:29:19.248Z; source line 2494; extract record 68*

Recorded **835 tests / 58 suites**, all report-only headlines unchanged and zero pending mods actors. Only verification prose changed after your run; no retest needed. Whitespace and symlink checks pass.

```zsh
cd /Users/edwardburke/IdeaProjects/nexonix
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md
  draco-git-record/git-record-2026-10-20-1503
  src/main/resources/draco/drake.dlt
  src/main/resources/draco/gendrake/Emit.{drake,json}
  src/main/scala/draco/gendrake/Emit.scala
  src/mods/resources/draco/dreams/README.md
  src/mods/resources/draco/dreams/editor/{GenerateRequested,TransformBinding}.{drake,json}
  src/mods/scala/draco/{DracoGenerator,Drake,NameResolution}.scala
  src/mods/scala/draco/dreams/editor/{GenerateRequested,TransformBinding}.scala
  src/test/scala/draco/NameResolutionTest.scala
  src/test/scala/draco/dreams/editor/GenerationTest.scala
)
git add -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F draco-git-record/git-record-2026-10-20-1503 -- "${paths[@]}" &&
git push origin main &&
git status --short --branch
```

After your result, the next #51 design point is supplying explicit scope to bootstrap loading—the remaining step before shortening names in the repository’s definition files.

*(Dev committed and pushed b17ddb8; the pasted final status is clean main...origin/main. Delegated action; 2026-10-21T13:18:51.969Z, source line 2504, extract record 69.)*

---

**[Codex]** *2026-10-21T13:19:11.708Z; source line 2521; extract record 70*

Confirmed **`b17ddb8` committed and pushed**, with a clean tree. The verified baseline is **835 tests / 58 suites**.

The next #51 design point is **how loading obtains its resolution scope**. Today, `TypeLoader` reads definitions before `DomainBuilder` assembles their ontology. Making loading depend on that completed ontology would introduce a bootstrap cycle.

I recommend **two-stage loading from an explicitly supplied set of domain declarations**: first establish domain and member identities from their qualified anchors and membership lists; then resolve their definitions using that identity-only `DomainOntology` and the supplied binding vocabulary. This would let definition bodies say `Dracon` while keeping the scope explicit and independent of loading order.

Do you accept this bootstrap approach, or would you correct it?

---

**[Dev]** *2026-10-21T13:27:11.413Z; source line 2528; extract record 71*

I want you to prepare for a new task-session, for either continuing with Codex or Claude Code, which would include updating and adding chapters in draco-dev-journal, as well all the memory and data useful to a hand-off and a prompt for copy and paste into the new task session.

---

## Status

Dev’s October 20 full gate passed **835 tests / 58 suites**, zero failed/canceled/ignored/pending tests and zero aborted suites. All report-only headlines are unchanged; pending mods actors is zero. Dev committed/pushed b17ddb8 on October 21; Codex inspected clean main at that commit. Scope is explicit for Drake, JSON and Scala generation, and Dreams uses its held ontology plus factory-supplied bindings. Bootstrap files remain qualified. Record-backed probe detail: an initial full harness omitted test resources, producing three scenario failures and two PON cancellations; the corrected final full run passed 835, including the local-value collision guard. A fixture initially omitted Dracon’s parent Format and was corrected without weakening validation.

The two-stage loader proposal in the final reply is **not accepted**. Dev next asks for this handoff, not implementation. Type-level formal/actual separation, .dcon, activation and Haskell target work remain deferred or unaccepted. This Status is a maintenance note written during the open handoff request, not a recovered reply. Chapters 95–99 and the audit cover 35 complete Codex exchanges plus that request; the final response completing this documentation pass is outside the frozen boundary. No sbt, commit or push is run by Codex in this pass.
