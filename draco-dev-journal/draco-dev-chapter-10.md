# Draco Dev Journal — Chapter 10

**Session date:** March 23, 2026 (continued)
**Topic:** NaturalActor Fix, TypeDefinition.load (Classpath Loading), Generator Enhancements, TypeName.typeParameters, JSON Definition Files

---

## Session Start — Context Window Upgrade

> **Dev:** This a new session. Can you suggest our next step?

**Draco:** Based on the memory's imminent task list, the next step is #1: Fix NaturalActor.scala — it has malformed JSON in embedded strings and is meant to be the first working actor test example.

> **Dev:** yes

**Draco:** *(Reads NaturalActor.scala, NaturalActorTest.scala, Natural.scala, Natural.json, and related files)*

Five issues found:
1. Empty JSON strings in NaturalActor.scala (lines 13, 16) — always yield Null
2. Natural.scala uses `Main.roots.sourceRoot` instead of `Test.roots.sourceRoot`
3. All three Natural JSON files are empty
4. Vestigial `actors/` and `rules/` subdirectories (old convention)
5. Natural.scala's `_logicalPath` is a directory, not a file

---

## Context Window Discovery

> **Dev:** My son set me a message about the size of the context window provided by Claude Code. He noticed that his was now 1000000 asked me to look at mine, to see if it had been increased to 1000000, as well.

**Draco:** Your context window is 200k tokens. Your son may be using a model variant with 1M context.

> **Dev:** Can we switch to Sonnet?

**Draco:** You can switch with `/model sonnet`.

> **Dev:** I just looked at my son's text to me at it shows opus-4-6 with 1000K

**Draco:** Try updating Claude Code — `claude update`.

*(Dev exits, restarts)*

> **Dev:** Ok, now we have 1000K in Opus.

**Draco:** You're now at 1M context on Opus 4.6 — 43k/1000k used (4%).

---

## Fixing NaturalActor

**Draco:** *(Creates `Natural.json` with TypeDefinition for Natural as Primal[Int], creates `Natural.actor.json` with aspect-named ActorDefinition, fixes Natural.scala to use `Test.roots.sourceRoot`, rewrites NaturalActor.scala to load from JSON via SourceContent, removes vestigial `actors/` and `rules/` subdirectories)*

### DelayedInit: validAspects

> **Dev:** *(Runs test — NPE)*
> ```
> Cannot invoke "scala.collection.immutable.Seq.filter(scala.Function1)" because the return value of
> "draco.TypeName$.validAspects()" is null
> ```

**Draco:** Classic DelayedInit. `TypeName.validAspects` is a `private val` (not `lazy val`) in a companion that `extends App`.

*(Fixes `private val validAspects` → `private lazy val validAspects`)*

---

## TypeName.json — The First JSON-to-Source Roundtrip

> **Dev:** Can you create a TypeName.json in src/main/resources/draco that will generate the current TypeName.scala?

**Draco:** *(Analyzes TypeName.scala structure, Generator capabilities, and JSON format. Creates TypeName.json with all 6 elements, factory with computed body, and 7 globalElements capturing the helper methods as Dynamic elements with parameters and body)*

Identified gaps between generated and hand-written:
- `sealed` modifier (no modules to restrict — stylistic, not needed)
- `private`/`lazy` modifiers on globalElements
- Custom Null body (derived fields compute non-empty values from empty inputs)

---

## The Generate TypeName Test

> **Dev:** In src/test/scala/draco/TypeNameTest.scala rewrite the body of test("TypeName"), so that it tests how Generator generates the source of TypeName as defined in src/main/resources/draco/TypeName.json - but before you rewrite let's first discuss and then plan how to do that test.

**Draco:** Three options discussed: print-only, print + assertions, print + write to separate file.

> **Dev:** Consider generating the source of TypeName into src/test/scala/draco

**Draco:** Concern: `object TypeName` in test would conflict with the real one in main.

> **Dev:** Good call. Let's go with your plan plus suggestions.

*(Creates test with `generateAndVerify` — loads JSON, generates source, writes to `.generated.scala.txt`, optionally compiles via programmatic Scala compiler)*

### DelayedInit: circeImports

> **Dev:** *(Runs test — null where imports should be)*

**Draco:** `Generator.circeImports` is a `private val` — another DelayedInit casualty.

*(Fixes `private val circeImports` → `private lazy val circeImports`)*

### Programmatic Compilation

> **Dev:** Is there a way for a scala-test to invoke the scala compiler on an individual source file as part of a test?

**Draco:** Yes — `scala.tools.nsc` API. Create Settings, StoreReporter, Global, call `run.compile()`, check `reporter.hasErrors`. Stop after `typer` phase to avoid class file conflicts.

*(Integrates programmatic compiler into test. Compilation check is best-effort — works in IDE, caught and skipped under sbt fork due to `managedScalaInstance := false` classpath issues)*

---

## TypeDefinition.load — Classpath Loading

> **Dev:** Is now a good time to remove the inline JSON string from the generated code and replace it with reading the <type name>.json file directly from src/main/resources?

**Draco:** Two options: SourceContent (filesystem) or classpath loading. Classpath is simpler for generated code — no source root needed, works anywhere resources are on classpath.

> **Dev:** Option B will work for this pass of specifically testing the draco type definitions with the draco source code generator. However, I may need SourceContent and ContentSink when Draco is deployed as binary jar files containing Dreams.

**Draco:** Option B for now. Dreams will use SourceContent/ContentSink for user-defined domains.

*(Rewrites `TypeDefinition.load` to use `getResourceAsStream(typeName.resourcePath)`. Renames `typeDefinitionFromJson` → `typeDefinitionLoad`, emitting `TypeDefinition.load(TypeName(...))`. Generated code no longer needs `import io.circe.parser`.)*

---

## Generator Enhancements — Iterative Fixes

### The First Successful Generate

> **Dev:** *(Runs test — one compiler error: Default missing `_name`)*

**Draco:** `defaultInstance` only includes parameters with defaults, but `name` is required. Fixed to use `nullValueFor` for required params.

> **Dev:** That, Is. AWESOME!

### Comparing Generated vs Hand-Written

```diff
-sealed trait TypeName {
+trait TypeName extends TypeInstance {

-  lazy val typeDefinition: TypeDefinition = TypeDefinition (
-    _typeName = TypeName (...),
-    _elements = Seq (...),
-    _factory = Factory (...)
-  )
+  lazy val typeDefinition: TypeDefinition = TypeDefinition.load(TypeName ("TypeName", _namePackage = Seq("draco")))

-  private lazy val validAspects: Seq[String] = ...
-  private def canonicalOrder: Seq[String] => Seq[String] = ...
+  val validAspects: Seq[String] = ...
+  def canonicalOrder(aspects: Seq[String]): Seq[String] = ...
```

Gaps identified: `sealed` (not needed), `private`/`lazy` on globalElements, custom Null body.

### Removing Default

> **Dev:** Is generating <type name>.Default in addition to <type name>.Null actually useful?

**Draco:** Not really. For TypeName they're identical. Removed from Generator.

### Factory Body and Null Overrides

> **Dev:** Check the additions I made to the TypeName.scala file and update the Generator accordingly.

**Draco:** Two additions found:
1. Factory body: `override val typeInstance/typeDefinition` referencing companion
2. Null body: `TypeDefinition.Null` and `TypeName.Null` self-reference

*(Updates Generator: `factoryBody` appends instance overrides to both paths; `nullInstance` with factory body uses direct element overrides with `TypeDefinition.Null` and self-referencing Null)*

### Bootstrap Recursion — The StackOverflow

> **Dev:** *(Runs test — StackOverflowError)*
> ```
> at draco.TypeName$anon$1.<init>(TypeName.scala:46)
> at draco.TypeName$.apply(TypeName.scala:39)
> at draco.TypeName$.typeDefinition$lzycompute(TypeName.scala:16)
> ```

**Draco:** Infinite recursion: `TypeName.typeDefinition` → `TypeDefinition.load(TypeName("TypeName",...))` → `TypeName.apply` → `override val typeInstance = TypeName.typeInstance` → `TypeName.typeInstance` → `Type[TypeName](typeDefinition)` → back to start.

Fix: `override lazy val` for typeInstance/typeDefinition in factory body — defers evaluation until first access.

### Mutual Null Recursion

> **Dev:** *(Runs sbt test — multiple StackOverflows)*
> ```
> at draco.TypeName$.Null(TypeName.scala:50)
> at draco.TypeDefinition$.Null$lzycompute(TypeDefinition.scala:101)
> at draco.TypeName$anon$2.<init>(TypeName.scala:57)
> ```

**Draco:** Same pattern in Null: `TypeName.Null` needs `TypeDefinition.Null`, which calls `TypeDefinition(TypeName.Null)`, which needs `TypeName.Null` — cycle.

Fix: `override lazy val` for typeDefinition/typeInstance in Null body too.

> **Dev:** *(Runs sbt test — 32/33 pass, only Generate TypeName fails on classpath issue)*

---

## Imports for generate(td) and generate(td, dd)

> **Dev:** Is it too early to add ActorDefinition to this?

**Draco:** Not too early. The Instance triad is symmetric — handle all three import patterns together.

*(Creates shared infrastructure: `packageHierarchyImports`, `typeImports(namePackage, hasCodec, instanceType)`, `pekkoImports`, `ruleFrameworkImports`. Refactors `ruleImports` to reuse `packageHierarchyImports`. Adds `generate(td, ad: ActorDefinition)` overload with Pekko imports.)*

---

## TypeName.typeParameters

> **Dev:** One thing that was unintentionally dropped was dealing with type formal and type actual parameters in TypeName, and how declarations with generics get generated.

> **Dev:** do we just need to add the property "parameters", or do we add "actuals" for the case where there is a mix of formal and actual type args?

**Draco:** A single TypeName never needs both formals AND actuals. The declaring TypeName has formals; reference TypeNames have actuals. Whether a parameter is formal or actual is determined by context, not by the TypeName itself.

> **Dev:** If you say we don't need two parameter lists in TypeName, I'll go with it.

*(Adds `typeParameters: Seq[String]` to TypeName trait, apply, Null, encoder, decoder, and JSON. Adds Generator helpers: `parameterizedName(tn)` → `"Primal[T]"`, `wildcardTypeName(tn)` → `"Primal[_]"`. Updates `typeNameLiteral` to include typeParameters. Updates `traitDeclaration`, `typeGlobal`, `domainGlobal`, `typeExtends`, codec declarations, `nullInstance` to use TypeName-aware helpers.)*

> **Dev:** All tests succeeded

---

## Creating JSON Definition Files — Batch

> **Dev:** Start current step 3.

**Draco:** *(Creates JSON definitions for TypeDefinition, DomainDefinition, ActorDefinition. Adds Generate tests. Extracts `generateAndVerify` helper. Creates GeneratorDefinitionToSourceTest.scala.)*

> **Dev:** I ran all tests in sbt and everything passed. Lets move the contents of TypeNameTest.scala to GeneratorDefinitionToSourceTest.scala, and put the tests in order alphabetically by simple type name. When that works, let's go through everything in the draco package one at a time in that order, adding it to the new test file.

*(Moves generate tests to GeneratorDefinitionToSourceTest, keeps TypeNameTest with just the TypeName test. Creates JSON files for: Actor, ActorInstance, ActorType, ContentSink, Dictionary, Domain, DomainDictionary, DomainInstance, DomainType, Draco.)*

> **Dev:** pause here and consolidate before continuing. Then return to asking for approval when making edits.

---

## PrimesRulesTest — User Modification

Dev modified `PrimesRulesTest.scala` to use `TypeDefinition` instead of `RuleDefinition` for the rule generation tests, reflecting the TypeDefinition unification (completed later in Chapter 14). The three Generate tests now parse rule JSON as `TypeDefinition` and call `Generator.generate(rule)` with the unified overload.

---

## Documentation and Session Wrap

> **Dev:** Update the README.md and prepare to start a new session. (CLAUDE.md, and other files?)

*(Updates README.md — Working Features (classpath loading, typeParameters, imports, NaturalActor, Generate test pattern), Work in Progress (JSON files in progress, rule migration planned), TypeName table (typeParameters), Code Generation section (5 overloads). Updates CLAUDE.md — TypeName typeParameters, DelayedInit lazy val rules, Generator section, JSON section. Updates MEMORY.md — completed tasks, JSON file status, imminent tasks.)*

---

## Session Summary

Major accomplishments this session:

1. **NaturalActor fixed and working** — JSON files populated, SourceContent loading from test resources, vestigial files removed, first working actor test end-to-end
2. **TypeDefinition.load (classpath loading)** — `getResourceAsStream` replaces `SourceContent` for generated code; Generator emits `TypeDefinition.load(TypeName(...))` instead of inline JSON
3. **Generator enhancements** — `lazy val` on globalElements, `override lazy val` on factory body and Null instance overrides (bootstrap and mutual recursion fixes), `defaultInstance` removed, import infrastructure (`packageHierarchyImports`, `typeImports`, Pekko imports), `generate(td, ad)` overload
4. **TypeName.typeParameters** — proper type parameter support replacing embedded `[T]` in name string; `parameterizedName`, `wildcardTypeName` helpers; trait/companion/codec generation updated
5. **Generate test pattern** — `GeneratorDefinitionToSourceTest.scala` with `generateAndVerify`, programmatic compilation (best-effort under sbt fork)
6. **10+ new JSON definition files** — Actor through Draco, with Generate tests for core types
7. **DelayedInit fixes** — `validAspects`, `circeImports` changed from `val` to `lazy val`
8. **Context window** — upgraded from 200k to 1M tokens on Opus 4.6
