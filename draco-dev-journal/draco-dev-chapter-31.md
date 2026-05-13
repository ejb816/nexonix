# Draco Dev Journal — Chapter 31

**Session date:** May 10–13, 2026
**Topic:** Alphabetical canonicalization sweep — second half. From `Main` through `Value`. Three eliminations (`RuntimeCompiler`, `Specifically`, `Extensible` propagation residue), one binary split (`Transform` → `TypeTransform` + `DomainTransform`), one base-trait promotion (`Primal`/`Holon` → `DracoType`), and several Generator polishes — most notably the recovery of round-trip-safe codec emission via `referencesCirce`, `decoderForLine`, the strict-codec-leaf-only encoder match, and Monadic-in-trait-body. Sweep ends with the whole `draco/*` package generator-canonical. **Finish-pass on May 13** then brings the full test corpus back to green: 38 reference-frame test Scalas swept, 11 sub-package types dropped from byte-equivalence comparison via a new `comparisonOnlyExcluded` distinction in DracoGenTest, three structural sub-package files (`Base`/`Coordinate`/`Distance`) regenerated, three dangling-reference test edits clearing chapter-31 deletions, and one Generator bug fixed (multi-type generate's `instanceType` was never propagated, dropping pekko imports for any merged source containing actor types).

---

## Resume after Chapter 30

Chapter 30 paused after **Holon** with ~16 per-type entries pending plus the TypeElement group test plus three sub-packages. The dev returned and resumed at **Main** in standard sweep cadence. The next ~10 files (Main through Type) moved fast — by this point the Generator had absorbed enough polish that most diffs reduced to single-edit canonicalizations.

Significant per-type events accumulated mid-sweep are captured below by theme rather than file-by-file.

---

## Eliminate RuntimeCompiler

`RuntimeCompiler` was the runtime Scala compiler used by `GenerateAndCompileTest` and `RuntimeCompilerTest` to materialize Generator output into running classes. As a draco type it didn't really earn its keep — its three methods (`compile` / `compileMulti` / `loadClass`) were Scala-specific by design and weren't going to be regenerated cross-language.

First attempt: just make it a clean draco type with `extends DracoType`. The agent resisted briefly — the dev pushed back, the agent dropped the resistance, and the implementation moved forward. Test passed.

Then the dev came back with a sharper take:

> **Dev:** Since you gave me more resistance on this change than any other I have asked for, I decided to rollback RuntimeCompiler back to when it still had 'extends Extensible' on the trait, and then ask you to make the functions part of Generator.

Functions migrated to `Generator` (where `Generator[L]` would eventually live anyway). And:

> **Dev:** If all the functions called are now in Generator, why can't we safely delete RuntimeCompiler files?

The agent agreed. `RuntimeCompiler.scala`, `RuntimeCompiler.json`, and `RuntimeCompilerTest.scala` all got the file-level treatment. Tests that referenced `RuntimeCompiler.compile` now call `Generator.compile`. Net: −1 type, +0 functionality, classes load with the same JVM tools they always did.

**Lesson the agent took away:** when the dev pushes back on a design recommendation, the right response is not to re-argue from the same premises. The dev had already factored in the costs the agent was protecting against.

---

## Delete Specifically

The trait `Specifically[T]` was a marker added speculatively (chapter 24-ish) to help create more specific instances of aspect type instances — `Domain[T]`, `Rule[T]`, `Actor[T]`. The dev confirmed it had never been used:

> **Dev:** I'll just delete it for now and try direct inheritance in the future.

`Specifically.scala`, `Specifically.json` gone. No references to update — that's the whole point of speculatively-introduced marker traits: they leave no callers when removed.

---

## Split Transform into TypeTransform + DomainTransform

Both flavors needed: type-level transforms (`Transform[S, T] <: TypeTransform[S, T]`, structural mappers between two types) and domain-level transforms (`DomainTransform[S, T] <: DomainTransform[S, T]`, the reference-frame transforms in `domains/*/<peer>/*Transform.scala`).

> **Dev:** I've changed Transform (after it passed) to TypeTransform and created DomainTransform based on the post-passed version of Transform...

The dev did the rename and the new file creation manually. Agent verified by running both single-type tests. `Transform.scala` deleted (was the prior unified version).

The architecturally interesting follow-on: **DomainTransform requires its `S` and `T` to extend `DomainType`**. The reference-frame leaves all chain through `Cosmocentric`, so the right way to satisfy the bound is to make `Cosmocentric` itself extend `DomainType`. The dev codified this:

> **Dev:** All the *centric types were intended to start as domains with no element types. Cosmocentric was the only one that extends DomainType directly.

Cosmocentric.json got `dracoAspect.derivation: [DomainType]`. Cosmocentric.scala's trait got `extends DomainType`. Every other `*centric` (Egocentric/Geocentric/Heliocentric/Galactocentric) extends Cosmocentric, so the DomainType-ness propagates without touching them.

---

## Primal and Holon to DracoType

Initial state: `Primal[T]` and `Holon[T]` were vestiges of the `Extensible` era — they still said `extends Extensible` despite Extensible being eliminated in chapter 30. Pure historical drift.

The dev asked:

> **Dev:** Primal does not extend DracoType. Is there a reason for that?

There wasn't. `Primal` became `extends DracoType` with an abstract `val typeDefinition: TypeDefinition`. Holon followed the same pattern. Cascade was bigger than expected: every type whose factory body chained through Primal needed `override lazy val typeDefinition` added (Natural, all 12 TypeElement family members, etc.). Python script + targeted hand-fixes did the work. ~15 files touched.

---

## TypeDefinition — The Five-vs-Eighteen Reconciliation

The TypeDefinition migration was the single largest reconciliation of the sweep.

**Problem:** the hand-written `TypeDefinition.apply` took 18 flat parameters — one for each of the 4 aspects' fields, exploded. The YAML factory expected 5 — one per aspect block (`_typeName`, `_dracoAspect`, `_domainAspect`, `_ruleAspect`, `_actorAspect`). Generator-canonical wanted to emit the aspect-shaped 5-param form. So `TypeDefinition(...)` callers everywhere — `TestTypeModule.scala` (15 call sites), `TypeDefinitionTest.scala` (4 call sites), every hand-written file that built its own `lazy val typeDefinition` inline (`Generated.scala`, `TypeDictionary.scala`, `TypeElement.scala` × 12 inline TDs, `Value.scala`) — were passing flat keyword args that no longer matched.

The migration:
1. Rewrote `TypeDefinition.scala` to aspect-shaped `apply` with 5 params.
2. Added the encoder/decoder as Monadic globalElements in `TypeDefinition.yaml` + `.json`.
3. Walked every flat-param call site and rewrapped: `_derivation = Seq(...)`, `_elements = Seq(...)`, `_factory = Factory(...)` → all inside `_dracoAspect = DracoAspect(...)`. `_elementTypeNames` → `_domainAspect = DomainAspect(...)`.
4. Discovered along the way: `Generator.scala` had its own self-describing TypeDefinition at line ~20 (Generator describes itself) AND a `mergedTd` synthesis in `generate(Seq[TypeDefinition])` at line ~1089. Both needed the same wrapping.

After getting the file to compile, the test failed on a missing-imports diff. Root cause: the generator emits `circeImports` only when `hasCodec(td)` is true (auto-generated codec). But TypeDefinition's codec is hand-rolled via Monadic globalElements; `hasCodec` returns false; circeImports skipped. Fix added the helper:

```scala
private def referencesCirce (td: TypeDefinition) : Boolean = {
  val texts = td.dracoAspect.globalElements.iterator.map(_.value) ++
              td.dracoAspect.globalElements.iterator.flatMap(_.body.map(_.value)) ++
              td.dracoAspect.factory.body.iterator.map(_.value)
  val markers = Seq("Encoder[", "Decoder[", "Encoder.instance", "Decoder.instance",
                    "Json.obj", "Json.fromString", ".asJson", "io.circe")
  texts.exists(t => t != null && markers.exists(t.contains))
}
```

And the typeImports gate became `if (hasCodec || referencesCirce(td)) circeImports else Seq.empty`. This load-bears for every bootstrap type that hand-rolls its codec.

---

## TypeElement — The Largest Multi-Type Reconciliation

12 sub-types (TypeElement + BodyElement + 10 leaves: Fixed, Mutable, Dynamic, Parameter, Monadic, Pattern, Action, Condition, Variable, Factory) all generated together. Five distinct Generator fixes surfaced from a single multi-type group test:

**(1) Trait body `Fixed` with default — `val` → `lazy val`.** Hand-written had `val parameters: Seq[Parameter] = Seq()` in trait body; subtypes' factory bodies emit `override lazy val parameters = _parameters`. Scala forbids `lazy val` overriding a concrete non-lazy `val`. The fix:

```scala
case f: Fixed =>
  if (f.value.nonEmpty) s"  lazy val ${f.name}: ${f.valueType} = ${f.value}"
  else s"  val ${f.name}: ${f.valueType}"
```

`lazy val` (with default) for defaulted-Fixed; plain abstract `val` when no default. The override-with-lazy-val pattern is now uniformly safe.

**(2) Unreachable parent-case in encoder match.** `discriminatedCodecDeclaration` always emitted `case _: $name => "$name"` after the leaves. But the trait is sealed (we only generate discriminated codecs when modules.nonEmpty, which forces `sealed`), so the parent case is unreachable. Compile warning. Just removed:

```scala
// no `fallbackMatch` — the leaves are exhaustive on a sealed trait
val matchArms = leaves.map(leaf => s"""      case _: $leaf => "$leaf"""").mkString("\n")
```

**(3) Decoder strictness asymmetry — the big one.** Generator was emitting:

```scala
_value <- cursor.downField("value").as[String]    // strict
```

…for any Parameter whose own factory had `value: ""` (empty default). But the encoder uses `if (te.value.nonEmpty) Some(...)` for elidable types (`String` / `Seq[_]` / `Map[_]`), which means the encoder *would* omit empty values. Round-trip broken: encode produces `{"kind":"Parameter","name":"x","valueType":"String"}` (no value key), decoder requires value, decode fails, all-of-family-decodes-to-Null, NPE in `moduleOrder` because `TypeDefinition.Null.typeName == null`.

The fix factored decoder-line emission into a shared helper that mirrors the encoder's elision rules — String/Seq/Map fields are always Option-tolerant with the type's zero default:

```scala
private def decoderForLine (p: Parameter, indent: String) : String = {
  val typeZero: Option[String] = p.valueType match {
    case "String"                    => Some("\"\"")
    case s if s.startsWith("Seq[")   => Some("Seq.empty")
    case s if s.startsWith("Map[")   => Some("Map.empty")
    case _                           => None
  }
  val defaultOpt = typeZero.orElse(if (p.value.nonEmpty) Some(p.value) else None)
  defaultOpt match {
    case Some(d) =>
      s"""${indent}_${p.name} <- cursor.downField("${p.name}").as[Option[${p.valueType}]].map(_.getOrElse($d))"""
    case None =>
      s"""${indent}_${p.name} <- cursor.downField("${p.name}").as[${p.valueType}]"""
  }
}
```

Used in three places: `fieldElisionDecoder`, `discriminatedCodecDeclaration` leaf cases, `discriminatedCodecDeclaration` fallback case. The principle now: **encoder may elide → decoder must tolerate**.

**(4) Fallback case for unknown kinds.** When `td.factory.parameters` is empty (the parent has no own fields), the fallback decoder case now emits `Left(DecodingFailure)` instead of trying to construct a generic parent instance. Cleaner fail-fast semantics for unknown discriminator values.

**(5) `lazy val Null = apply()` when all params default.** When every factory parameter carries a default value, `nullInstance` now emits a bare `apply()` instead of repeating each `_param = default` line. Smaller, more idiomatic.

The flagged-but-deferred issue from this round: **the canonical encoder doesn't emit the `value` field for TypeElement subtypes** because `value: String` lives on `Primal[String]`, not in TypeElement's elements list. So encoding any non-empty value of a Fixed/Parameter etc. loses it on round-trip. Recorded for later. Doesn't break compilation; doesn't break the tests in this sweep; surfaces if we exercise YAMLRoundTripTest broadly.

---

## Value — Monadic in Trait Body

The `Value` trait has a `def value[T] (_source: Json)(implicit decoder: Decoder[T]): T = { ... JSON pointer walking ... }` method. Type parameter on the method, two parameter lists with the second implicit, multi-line body. Far beyond what Generator's `Fixed`/`Mutable`/`Dynamic` cases can express.

The clean fix: extend Generator's `typeBody` to handle `Monadic` — emit the value string verbatim, with per-line indent:

```scala
case mo: Monadic =>
  mo.value.linesIterator.map(l => s"  $l").mkString("\n")
```

Then added the `value[T]` method as a Monadic element in `Value.json`. This is the trait-body parallel to how `factoryBody` already handles Monadic. Useful for any future type with method-level type parameters, implicit parameter lists, or multi-line bodies that don't fit the Fixed/Mutable/Dynamic vocabulary.

---

## Generator Polish Catalog (Cumulative this Session)

Building on chapter 30's catalog:

15. `referencesCirce(td)` — auto-detects `Encoder[` / `Decoder[` / `.asJson` / `Json.obj` / etc. in globalElements and factory body strings; gates circeImports emission. Replaces strict reliance on `hasCodec`.
16. `decoderForLine(p, indent)` helper — single source of truth for the encoder/decoder asymmetry. String/Seq/Map fields always tolerate missing JSON.
17. `case other => Left(DecodingFailure(...))` for fallback when parent has no fields. Replaces previous "construct generic parent" fallback.
18. Trait body `Fixed` with default → `lazy val` (was `val`). Permits `override lazy val` downstream uniformly.
19. Trait body `Monadic` support — emits verbatim Scala source with per-line indent.
20. Encoder match drops unreachable parent case for sealed discriminated types.
21. `Null = apply()` when every factory parameter has a default (instead of per-param overrides).

The Generator's surface is now substantially more expressive: any reasonable hand-written type's Scala can be regenerated cleanly from a JSON/YAML representation. The discipline that emerged: **what the encoder elides, the decoder tolerates; what the trait body needs that the typed vocabulary can't express, Monadic catches**.

---

## Final Sweep State

Every type alphabetically through `Value` now passes `DracoGenTest`:

| Type | Sweep result |
|---|---|
| Actor → Holon | chapter 30 |
| Main, Primal, REPL, Rule, RuleAspect, RuleType, RuntimeCompiler\*, SourceContent, Specifically\*, Test, Transform→TypeTransform, Type | clean per-type runs |
| TypeDefinition | aspect-shaped apply, 19 call sites rewrapped, circeImports for hand-rolled codecs |
| TypeDictionary | canonical adopted |
| TypeElement (12-type group) | 5 Generator fixes, hand-written rewritten |
| TypeName | canonical adopted |
| TypeTransform | clean — no edits |
| Value | Monadic in typeBody, value[T] method preserved via JSON |

*\* deleted, not canonicalized*

---

## Pause

**Branch:** `main`, parent repo. Uncommitted. (Dev commits via IDE per `feedback_direct_main_edits.md`.)

**Test state:** every type alphabetically through Value passes its DracoGenTest. The broader test suite (`DomainsGenTest`, `YAMLRoundTripTest`, `GenerateAndCompileTest`) has not been re-run since the encoder/decoder strictness changes; expected fallout:

- **YAMLRoundTripTest** may surface the `value` field loss for TypeElement subtypes (Primal-inherited field not in encoder).
- **GenerateAndCompileTest** group tests may pick up new emission patterns and either close or reopen baseline failures.
- **DomainsGenTest** likely clean — its types don't depend on the changed decoder paths.

**Resume**: run the full test corpus to surface the broader fallout, then handle YAML round-trip's `value` field issue. After that, the natural next chunk is the sub-packages (`draco/base/*`, `draco/language/*`, `draco/primes/*`) — they should benefit from all the Generator polish but haven't been through per-type DracoGenTest scrutiny.

---

## Resume after Pause (May 13)

The dev returned and immediately spotted what hadn't happened:

> **Dev:** I just noticed there was no commit and push in the last session.

The working tree was sitting on `main` with the entire chapter-31 sweep uncommitted — 156 tests green when last seen, but never serialized. Standard agent workflow per `feedback_direct_main_edits.md` is to draft a `draco-git-record/git-record-YYYY-MM-DD-HHMM` file with the commit commands and let the dev execute via IDE. That step was simply missed at the prior pause.

> **Dev:** Just one commit record to cover the work since that last commit and push

Agent drafted `draco-git-record/git-record-2026-05-13-1300` covering chapters 29 + 30 + 31 as a single bundled commit. Standard format: `git add` for each new file, `git add -u` for modifications/deletions, then a HEREDOC commit message describing the chapter-spanning work, then `git push`. Before the dev ran it, agent flagged two sanity checks: (a) `git add -u` would also pick up two unexpectedly-modified files (`org/nexonix/format/Value.scala`, `org/nexonix/rules/rete/Rete.scala`) that weren't in the chapter narrative, and (b) the resume note's "full-corpus test run" had never actually been done — the commit message claimed "per-type DracoGenTest passes" but the broader corpus might surface issues the message would need to acknowledge.

The dev opted to do that test run before committing.

---

## The Full-Corpus Run: 192 PASS / 59 FAIL

The full `sbt test` surfaced 59 failures across 5 suites. Categorization:

- **DomainsGenTest: 38 failures** — every reference-frame test scaffold (`src/test/scala/domains/**/*.scala`) had three uniform diffs: `lazy val typeDefinition` lacked `override`, `Generator.loadType` was FQN-prefixed `draco.Generator.loadType`, and `Seq("domains"` lacked the canonical space after `Seq`. Plus the 12 transform companions had `extends App {` instead of `extends App with DracoType {`. None of these had been touched by the chapter-31 sweep — the sweep ran on `src/main/scala/draco/`, the test scaffolds were untouched.
- **DracoGenTest: 18 failures** — 17 sub-package types (`draco/base/*` × 10, `draco/language/*` × 2, `draco/primes/*` × 6 minus one rule that was clean), plus one in the rules. These were the "next chunk" the chapter-31 pause forecast.
- **GeneratorDefinitionToSourceTest: 1 failure** — `Generate Extensible` test still in the file though `Extensible.json` was deleted.
- **GenerateAndCompileTest: 1 failure** — `dracoCoreGroup` Seq still listed `Extensible.json`/`RuntimeCompiler.json`/`Specifically.json`/`Transform.json` (all deleted), and was missing the new `DomainTransform.json`/`TypeTransform.json` from the binary split. Fails at the JSON-load step before getting to compilation.
- **RuntimeCompilerTest: 1 failure** — `Compile generated source that references draco types`. The embedded test source had `trait GeneratedTrait with DracoType` — invalid Scala syntax (`with` requires preceding `extends`). Pre-existing typo, not chapter-31 fallout.

Three buckets of intent:
1. **Pre-existing-flagged-as-resume-work (55):** The 38 reference-frame failures + the 17 sub-package failures. Both buckets were known followups; the chapter-31 pause note named them.
2. **Genuine chapter-31 dangling references (3):** The Extensible test, the `dracoCoreGroup` list, the `with`→`extends` typo (technically pre-existing but only surfaces because nothing else in that test source is broken anymore).
3. **Surprise that didn't surface yet:** The Generator multi-type imports bug that would surface later, after Phase 3 brought GenerateAndCompileTest's `dracoCoreGroup` back from JSON_FAIL to actually-running-and-then-failing.

---

## Three Dangling-Reference Fixes

Quick edits, all in test sources:

- `src/test/scala/draco/GeneratorDefinitionToSourceTest.scala` — dropped the `test("Generate Extensible") { ... }` block.
- `src/test/scala/draco/GenerateAndCompileTest.scala` — `dracoCoreGroup` Seq: removed the 4 deleted-JSON entries, added `DomainTransform.json`/`TypeTransform.json`, kept alphabetical (24 entries total). Updated the leading comment claim ("All other types defined under `src/main/resources/draco/` root") to remain truthful.
- `src/test/scala/draco/RuntimeCompilerTest.scala` — `with` → `extends` in the embedded source string.

> **Dev:** yes

Three edits applied in parallel. None touch production code.

---

## Scope Expansion: "This Is the Next Session"

> **Dev:** This is the "next session", so let's fix everything here and rewrite the commit and push record if necessary.

Major scope expansion from "fix the 3 dangling references" to "address all 55 sub-package + reference-frame failures here too." Agent proposed a three-phase plan and asked one moment for redirect:

**Phase 1 — Reference-frame mechanical sweep (38 files):** uniform `sed` substitutions over `src/test/scala/domains/**`. Three universal subs (`override` injection, drop `draco.` FQN on `Generator`, space after `Seq`) plus `with DracoType` injection on the 12 transform companions identified by directory depth.

**Phase 2 — Trivial sub-package fixes (4 files):** same sed pattern adapted for `loadRuleType` on the 3 rule files (`AddNaturalSequenceRule`, `PrimesFromNaturalSequenceRule`, `RemoveCompositeNumbersRule`); 3-line addition to `Language.scala` (`dracoType`, `elementTypeNames`, `domainType` were missing from the hand-written companion).

**Phase 3 — Sub-package decisions (13 hairier files):** the architecturally interesting bucket. Categorization based on per-file diff:

- **Hand-written has Scala-only logic (4):** `YAML` (yaml/json conversion helpers using `io.circe.yaml.{parser,printer}`), `Primes` (`filter`/`naturals`/`composites`/`nPrimes` LazyList computations), `Numbers` (factory body computes via `Primes.*` helpers), `Accumulator` (`scala.collection.mutable.Set` defaults). Per `feedback_haskell_test.md` — Scala-only content shouldn't be embedded in JSON via Monadic blocks; it would translate to dead vocabulary in any other language target. Per `feedback_json_authoring_surface.md` — JSON is for type *shape*, not for Scala-specific implementation logic.
- **Hand-written carries vestigial metadata (5+2):** `Cardinal`, `Nominal`, `Ordinal`, `Rotation`, `Unit` all have `override val name = "Cardinal"`-style constants. `Meters`/`Radians` have the same plus a hand-written single-line factory style. The constants are vestigial — they exist only as Scala self-description, no caller reads them, and propagating them through the inheritance chain (Unit→Cardinal→Distance/Rotation→Meters/Radians) would require Generator-mechanics burden disproportionate to value (each subtype would need `override` annotations the Generator doesn't currently emit).
- **Genuinely structural (3):** `Base` lacks `DracoType` in JSON derivation though hand-written has `extends DracoType`; `Coordinate` and `Distance` are stripped stubs that the canonical Generator emission cleanly supersedes.

The two-track decision: 11 types go to comparison-only-exclusion; 3 get actually fixed.

> **Dev:** yes

---

## Phase 1 Execution

Single sed sweep over the 38 files using `find ... -exec sed -i '' ...`. Three universal substitutions applied to all 38, then a fourth pass over `find ... -mindepth 3` selecting exactly the 12 transform files for the `with DracoType` injection. Spot-checked `Cosmocentric.scala` and `EgocentricGeocentric.scala` — both clean.

The 4 auxiliary test scaffolds in `src/test/scala/domains/` (`EgoActor.scala`, `Natural.scala`, `NaturalActor.scala`, plus `DomainsGenTest.scala` itself) were excluded from the sweep — none appeared in the failure list because they're handled separately by their respective tests.

---

## Phase 2 Execution

Two pieces:

**Rule files (3):** `sed` sweep with substitutions for `draco.Generator.loadRuleType` → `Generator.loadRuleType` and `Seq(` → `Seq (`.

**Language.scala (1):** `Edit` adding three lines — `lazy val dracoType: Type[Language] = Type[Language] (typeDefinition)`, blank line, `lazy val elementTypeNames: Seq[String] = Seq ("YAML")`, blank line — between the existing `typeDefinition` and `domainType` lines.

---

## Phase 3 — The `comparisonOnlyExcluded` Distinction

The existing `excluded` map in `DracoGenTest` filters out both per-type tests (parses + comparison) — its semantic is "skip per-type tests entirely". For the 11 sub-package types whose hand-written `.scala` carries Scala-only logic or vestigial metadata, the JSONs DO parse correctly — only the byte-equivalence comparison should be skipped. Adding to the existing `excluded` map would silently drop the parses test coverage too.

The cleanest extension: a parallel `comparisonOnlyExcluded: Map[String, String]` consulted by the comparison-loop only. The parses-loop continues to iterate over `perTypeTypes` unfiltered.

```scala
private val comparisonOnlyExcluded: Map[String, String] = Map(
  "draco/language/YAML.json"      -> "Hand-written has Scala-specific YAML/JSON conversion helpers...",
  "draco/primes/Primes.json"      -> "Hand-written has Scala-only LazyList helpers...",
  // ... 9 more
)

perTypeTypes.filterNot(ty => comparisonOnlyExcluded.contains(ty.resourcePath)).foreach { ty =>
  test(s"${ty.resourcePath}: Generator output matches ${ty.scalaPath} (whitespace-normalized)") { ... }
}
```

Each entry carries its rationale as the value — searchable, self-documenting, and visible in any future test edit.

The 11 entries split: 4 "logic-laden" + 7 "metadata-vestigial." The latter 7 also include the two factory types (`Meters`/`Radians`).

---

## Phase 3 — Base.json Augmentation + 3 Regenerations

**Base.json** previously had no `dracoAspect` block at all — just `typeName` + `domainAspect.elementTypeNames`. The hand-written `Base.scala` had `trait Base extends DracoType` but the Generator was emitting bare `trait Base` (because no derivation). The principled fix: Base IS the root of a domain, every domain root extends DracoType, the JSON should reflect that. Added:

```json
"dracoAspect": {
  "derivation": [
    { "name": "DracoType", "namePackage": ["draco"] }
  ]
},
```

After this, the Generator now emits `trait Base extends DracoType` and `object Base extends App with DracoType { override lazy val typeDefinition ... }` matching hand-written intent.

The three Scala files (`Base.scala`, `Coordinate.scala`, `Distance.scala`) were rewritten via `Write` to match what the Generator would emit — predicted from the JSON shapes plus the diff snapshots already captured in the failed test output. All three end up canonical-form: `import draco._` instead of FQN, `with DracoType` on the companion, `override lazy val typeDefinition`, `dracoType` + `domainType` lines, and (for Base) `elementTypeNames`.

The risk of hand-predicting Generator output: if the actual emission differs in something not anticipated (extra blank line, different import order), the comparison test still fails. Mitigating: the diff snapshots in the test failure messages are the literal Generator output for each, so prediction is essentially copy-paste with the JSON augmentation accounted for in Base's case.

---

## The Generator Bug: Multi-Type Imports Drop

Re-running `GenerateAndCompileTest`: the `TypeElement hierarchy` and `TypeDefinition family` group tests pass. The `Draco core types` group, now actually able to load all 24 JSONs, fails at compilation:

```
not found: type Behavior
not found: type TypedActorContext
not found: type Signal
not found: value Behaviors
```

Inspection of the dumped source `/tmp/Draco_core_types-generated.scala`: the merged file's import block contained `import org.apache.pekko.actor.typed.ExtensibleBehavior` (from `Actor`'s derivation) but was missing the triple `import org.apache.pekko.actor.typed.{Behavior, Signal, TypedActorContext}` + `import org.apache.pekko.actor.typed.scaladsl.Behaviors` that single-type Actor.scala generation correctly emits.

Root cause traced to `Generator.scala:1106`. The single-type code path branches on `instanceType`:

```scala
val isActorType = isActor(td)
val instanceType = if (isActorType) "actor" else ""
val imports = typeImports(td, hasCodec(td), instanceType)
```

…and `typeImports`'s match on `instanceType` adds `pekkoImports` when `"actor"`. But the multi-type code path at line 1106 was:

```scala
val imports = typeImports(mergedTd, anyCodec)  // instanceType defaulted to ""
```

— never propagated the actor-detection. Pre-chapter-31 this never mattered because Actor's Dynamic factory body was being emitted as stubs (chapter 31's polish #5: "factory.body Dynamic emits methodParameters — overrides didn't actually override"); the emitted source didn't actually reference Behavior/TypedActorContext/Signal/Behaviors. Once chapter 31 made Dynamic factory bodies emit their real method signatures, the bug had a way to bite — but only when Actor was bundled into a multi-type generate. The chapter-28 dracoCoreGroup test included Actor and was reported passing; the implication is either that test didn't really exercise the codepath that materializes Actor's body methods, or the body-stub-only emission masked the issue.

Surfaced by the very act of restoring `dracoCoreGroup` in this finish-pass.

One-line fix:

```scala
val instanceType = if (ordered.exists(isActor)) "actor" else ""
val imports = typeImports(mergedTd, anyCodec, instanceType)
```

This becomes the 22nd item in the cumulative Generator polish catalog.

---

## Re-Run: Full Corpus Green

> **Dev:** GeneratorAndCompileTest passed and then all tests passed

Final test state at end of session: full corpus green. DracoGenTest 30 root types byte-equivalent + 11 comparison-only-excluded (parses still verified for all 11), 38 reference-frame test scaffolds byte-equivalent, GenerateAndCompileTest's three group tests all pass (TypeElement hierarchy 12-type, TypeDefinition family 6-type, Draco core types 24-type), YAMLRoundTripTest 116/116 unchanged.

The git-record was updated through the session — first cut covered chapter-31's work alone, then expanded to acknowledge the three dangling-reference fixes, then expanded again to reflect the Phase 1/2/3 finish-pass and the Generator multi-type imports fix as polish item #22. The final form is one bundled commit covering chapters 29 + 30 + 31 + finish-pass.

---

## Final Pause

**Branch:** `main`, parent repo. Uncommitted, awaiting `draco-git-record/git-record-2026-05-13-1300` execution via IDE.

**Test state:** full corpus green.

**Resume:** Stage 2e leaf detection proper — the original goal that kicked off chapter 29; the foundation it needed (DracoType-as-root, comprehensive Generator polish, full-package generatability, sub-package alignment) is now firmly in place. The 11 comparison-only-excluded types remain hand-written; future could either augment Generator to emit Scala-only sections from JSON Monadic blocks, or accept the exclusion as permanent.
