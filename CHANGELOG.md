# Changelog

All notable changes to the Nexonix/Draco project will be documented in this file.

## [Unreleased]

### Added

- **`draco.Holon[T <: Product]`** — Base trait for primal type values (T has accessible substructure: tuples, case classes). `extends Extensible with Primal[T]`. Companion uses `Generator.loadType`.

- **`draco.Transform[S <: DracoType, T <: DracoType]`** — First two-parameter draco type. `extends Extensible with Holon[(S, T)]`. Primal type value whose `value` is a 2-tuple of source and target references, captured via the companion-as-instance pattern to avoid circular initialization.

- **`draco.RuntimeCompiler`** — `compile`, `compileMulti`, and `loadClass` helpers wrapping `scala.tools.nsc.Global` for runtime Scala compilation in tests (`scala-compiler` was already a dependency).

- **`draco.GenerateAndCompileTest`** — Comprehensive regression test: every JSON definition under `src/main/resources/draco/` is loaded, run through `Generator.generate`, and compiled via `RuntimeCompiler`. Baseline: 31 passed, 17 failed, 48 total. The `TypeElement` sealed hierarchy compiles as one unit via `compileMulti`.

- **`draco.RuntimeCompilerTest`** — Four tests covering the full cycle: simple standalone class, source referencing draco types (classpath bridging), full cycle from JSON (Holon), and error reporting.

- **`draco.GeneratorCLI` + `bin/draco-gen`** — Bash-invocable Generator CLI, packaged in the sbt-assembly fat JAR. Subcommands: `generate`, `compile`, `compile-multi`, `inspect`. Rebuild with `sbt assembly`. Enables fast iteration on Generator output and source-definition debugging.

- **Reference-frame example domains (Increment A)** — Cosmocentric super-domain plus Egocentric/Geocentric/Heliocentric/Galactocentric peer sub-domain skeletons under `src/test/{resources,scala}/domains/<frame>/`. No leaves or transforms yet — Increments B and C remain.

- **`domains.ReferenceFramesGenTest`** — 12-assertion verification harness: per-frame JSON parse + whitespace-normalized match of generator output to committed Scala, plus Cosmocentric standalone compile and family-wide `compileMulti`. Failure messages include the exact `bin/draco-gen generate ... > ...` recipe to reconcile drift.

- **Chapter 20 of draco-dev-journal** — Covers the reference-frame Increment A session, including the CLI pivot and the Generator cross-package import bug discovery.

- **draco-dev-journal extraction tooling** — Scripts under `draco-dev-journal/tools/` for extracting user↔assistant pairs from Claude Code JSONL session files (`extract_sessions.py`), matching them against existing chapters to identify gaps (`map_and_detect_gaps.py`), and producing per-chapter insertion/merged-draft files (`gaps/build_chapter.py`, `gaps/integrate_chapter.py`, `gaps/process_gaps.py`). Scripts use dynamic project-root resolution (no hardcoded paths); per-chapter scripts take the chapter number via argv. Outputs are regenerable and excluded via `.gitignore`.

### Changed

- **`draco.Primal[T]`** now extends `TypeInstance` (was `DracoType`). All factory anonymous classes use `override lazy val typeInstance` to break circular initialization. Applies to `TypeElement` (11 factories), `Coordinate`, `Meters`, `Radians`, and the `Primal` companion itself.

- **`draco.base.Coordinate`** retargets from `Primal[T]` to `Holon[T]`. Downstream types still see `Primal[T]` by transitivity.

- **Inline `TypeDefinition` eliminated for leaf types** — `Coordinate`, `Unit`, `Cardinal`, `Nominal`, `Ordinal`, `Distance`, `Rotation`, `Meters`, `Radians`, `Accumulator`, `Numbers`. All 11 leaf companions' `typeDefinition` now delegates to `Generator.loadType(TypeName(...))`; TypeDefinition data lives exclusively in JSON resource files. Domain-root types (`Draco`, `Base`, `Primes`) retain inline `TypeDefinition` for now.

- **`Generator.typeImports`** — New `referencedPackageImports` helper emits `import <pkg>._` for any cross-package `TypeName` in `derivation` / `modules` / `superDomain` / `source` / `target`. Unblocks any sub-type whose supertype lives in a non-parent package (e.g. `Egocentric extends Cosmocentric` where `Cosmocentric` lives in `domains.cosmocentric`). Purely additive — `GenerateAndCompileTest` baseline unchanged.

- **Chapters 01-19 of draco-dev-journal** — Content updates from running the gap-filling pipeline against source session data; adds Chapter 19 (Holon / Transform[S,T] / RuntimeCompiler design session).

### Fixed

- **`PrimesRulesTest.indexDifference`** — Returns `List.empty` for lists with fewer than 2 elements instead of crashing with `UnsupportedOperationException: tail of empty list`.

- **CI `release.yml`** — Adds an explicit sbt install step before the test/release phase, resolving a CI failure where sbt was unavailable on the runner.

### Removed

- **`draco.dreams.Transform`** — Obsolete runtime-only transform type, superseded by `draco.Transform[S, T]`. Scala source and JSON resource both deleted.

---

## [2.0.0-alpha.2] - 2026-03-31

### Changed

- **Generator owns all type loading** — Moved all type loading from `TypeDefinition.load` to Generator (`loadType`, `loadRuleType`, `loadActorType`, `loadAll`). `TypeDefinition.load` removed.

- **Auto-suffix naming convention** — Generator auto-appends "Rule" or "Actor" to generated type names based on file aspect (`.rule.json`, `.actor.json`). JSON `typeName` uses the base concept name only.

- **Rule type renames** — `AddNaturalSequence` → `AddNaturalSequenceRule`, `PrimesFromNaturalSequence` → `PrimesFromNaturalSequenceRule`, `RemoveCompositeNumbers` → `RemoveCompositeNumbersRule`, `AssembleResult` → `AssembleResultRule`.

- **Actor JSON renames** — `BravoActor.actor.json` → `Bravo.actor.json`, `DataModelActor.actor.json` → `DataModel.actor.json` (remove baked-in suffix from filenames).

- **Dev journal resequenced** — Chapters reordered into chronological order; added chapters 17-18.

### Removed

- **Inline JSON in rule Scala files** — Rules now load from `.rule.json` via `Generator.loadRuleType` instead of duplicating JSON inline.

- **TypeDefinition.load** — Replaced by Generator loading methods.

### Fixed

- **Int vs Integer in RemoveCompositeNumbers** — Evrete working memory uses boxed types; changed to `classOf[Integer]` instead of `classOf[Int]`.

---

## [2.0.0-alpha.1] - 2026-03-26

### Added

- **Extensible** — Non-parameterized structural root trait. Generator `typeExtends` convention: empty derivation → `extends Extensible`.

- **Specifically[T]** — Specialization trait extending `Extensible` with a type parameter, for deferred structural commitments.

- **TypeDefinition unification** — Dissolved `DomainDefinition`, `RuleDefinition`, and `ActorDefinition` into `TypeDefinition`. Ten new optional fields added: `elementTypeNames`, `source`, `target` (domain); `variables`, `conditions`, `values`, `pattern`, `action` (rule); `messageAction`, `signalAction` (actor). All default to empty/Null. `DomainType.domainDefinition`, `RuleType.ruleDefinition`, and `ActorType.actorDefinition` are now `TypeDefinition`-typed. The three definition source files and their JSON resource files have been deleted.

- **Generator detection-based dispatch** — `generate(td: TypeDefinition)` inspects content to determine generation mode: `isDomain` (elementTypeNames), `isRule` (variables), `isActor` (derivation). Replaces five overloads with two (`generate(td)` and `generate(tds: Seq[TypeDefinition])`).

- **Generate test pattern** — Output to `src/generated/scala/draco/<TypeName>.scala.generated` with diff comparison. `Generated.scala` alongside Main/Test for source/sink transitions.

- **superDomain field on TypeDefinition** — Supports domain inheritance hierarchy.

### Changed

- **TypeName simplified** — `aspects` field removed. `qualifiedName`, `aspectExtension`, `canonicalOrder` removed. Resource paths simplified to `/${namePackage}/${name}.json`.

- **Deferred factory defaults** — `pattern`, `action`, `messageAction`, `signalAction` defaults in `TypeDefinition.apply` use `null` with `override lazy val` resolution to avoid circular initialization in the self-describing type system.

### Removed

- **DomainDefinition** — Dissolved into `TypeDefinition`. Source file, JSON resource, and all references removed.
- **RuleDefinition** — Dissolved into `TypeDefinition`. Source file, JSON resource, and all references removed.
- **ActorDefinition** — Dissolved into `TypeDefinition`. Source file, JSON resource, and all references removed.
- **Generator overloads** — `generate(td, dd: DomainDefinition)`, `generate(td, ad: ActorDefinition)`, `generate(rd: RuleDefinition)` removed.

---

## [Previous - JSON Definitions, Generated Tests, External Imports]

### Added

- **Complete JSON definition files** — Every manually-written draco framework type now has a corresponding JSON definition file in `src/main/resources/draco/`. New files: DracoType, Primal, Type, TypeInstance, RuleType, RuleInstance, Main, Test. Populated previously empty files: Rule, Value, SourceContent, RuleDefinition, TypeDictionary. Added missing derivation to: ActorInstance, DomainInstance, ActorDefinition, DomainDefinition (→ TypeInstance), TypeElement (→ Primal[String]).

- **GeneratorDefinitionToSourceTest** — 28 generate tests covering all draco framework types: core hierarchy (DracoType, Primal, Type, TypeInstance), domain types, rule types, actor types, infrastructure types, and the TypeElement sealed hierarchy (multi-type generation). Tests generate source, write to `generated.draco` package, and compile-check.

- **Generated output in `generated.draco` package** — Generated Scala files written to `src/test/scala/generated/draco/*.scala` with `package generated.draco`. Coexists with real framework types without shadowing. Replaces old `.generated.scala.txt` approach.

- **External type import detection** — Generator automatically detects external types (URI, URL, BufferedSource, KnowledgeService, Consumer, Knowledge, RhsContext) referenced in elements/factory/globalElements and emits appropriate import statements.

- **Parameterized Null instances** — `nullInstance` now uses `wildcardTypeName` for parameterized types (e.g., `lazy val Null: Actor[_] = apply[Nothing]()`) instead of unparameterized type name.

### Changed

- **TypeName `typeParameters` field** — Primal, Type, and Rule Scala sources updated to use proper `typeParameters = Seq("T")` instead of embedding `[T]` in the name string.

- **`typeDefinitionLoad` fully qualified** — Generator now emits `draco.TypeDefinition.load(...)` instead of `TypeDefinition.load(...)`, enabling generated code to work in any package (including `generated.draco`).

- **`typeExtends` no longer defaults to `extends TypeInstance`** — Empty derivation produces no extends clause (DracoType is the root). Types that previously relied on the implicit default now have explicit derivation in their JSON definitions.

- **`typeImports` signature** — Changed from `(namePackage, hasCodec, instanceType)` to `(td, hasCodec, instanceType)` to enable external import detection from the TypeDefinition.

- **Codec generation guard** — Simple codecs (Pattern 1) only generated when factory parameters are a subset of declared trait elements, preventing invalid field access in encoder/decoder.

- **TypeDictionary derivation** — Fixed stale `namePackage` (`org.nexonix.domains` → `draco`) and updated to use `typeParameters` field instead of embedded `[TypeName,TypeDefinition]`.

### Removed

- **Stale JSON files** — Deleted `RuleActorBehavior.json` and `RuleSet.json` (no corresponding Scala source).
- **Old generated files** — Removed all `*.generated.scala.txt` files, replaced by `generated/draco/*.scala`.

---

## [Previous - DomainDefinition, Rule Generation, Actor System]

### Added

- **DomainDefinition** — Replaced `DomainName` with `DomainDefinition`, a richer domain descriptor with `typeName`, `elementTypeNames`, `superdomain`, `source`, and `sink` fields. Makes domain definitions consistent with `RuleDefinition`. The `source`/`sink` fields formalize Update (endomorphism: source == sink) and Transform (source != sink) domain patterns. JSON backward compatible — existing `DomainName` JSON deserializes via defaulted `Option` fields.

- **DomainInstance generation** — New `generate(td: TypeDefinition, dd: DomainDefinition)` overload in Generator produces DomainInstance companion objects matching the hand-written Primes pattern. Includes `domainInstanceLiteral` and `domainGlobal` helpers.

- **nullValueFor helper** — Generator now produces type-appropriate null-equivalent values ("" for String, Seq.empty for Seq, 0 for Int, false for Boolean, etc.) instead of `null.asInstanceOf[T]` when generating Null instances.

- **Rule import generation** — `ruleImports(namePackage)` auto-generates package hierarchy imports (e.g., `import draco._`, `import draco.primes._`), always includes `import draco._`, plus framework imports for Circe, Evrete, and `java.util.function.Consumer`.

- **README.md** — Comprehensive project documentation covering architecture, type system, domains, rules, actors, code generation, semantic preservation, and current feature status.

### Changed

- **factoryBody** — Now takes `Factory` instead of `Seq[Parameter]`. When `factory.body` is non-empty, uses body elements directly as overrides. When empty, falls back to parameter-derived behavior.

- **nullInstance** — Now takes `Factory` as additional parameter. When a factory exists, generates Null via `apply()` with null-equivalent args. Falls back to null-cast only when no factory is present.

- **Generated rule vals** — `ruleDefinition`, `action`, and `pattern` in generated rule companions are now `private lazy val` instead of `private val` (required for DelayedInit compatibility).

- **Generated rule types** — `Consumer[RhsContext]` and `Consumer[Knowledge]` now use short names (imported) instead of fully qualified types in generated rule source.

- **Generated rule ruleDefinition** — Now uses embedded JSON parsed via `parser.parse(...)` instead of `RuleDefinition.Null` placeholder.

### Fixed

- **RuleDefinition encoder/decoder** — Changed from `implicit val` to `implicit lazy val` to fix DelayedInit null when accessed from tests.

- **Value encoder/decoder** — Same fix: `implicit val` to `implicit lazy val`.

- **RuleDefinition decoder** — Fixed `_pattern` field reading from wrong JSON field (`"action"` instead of `"pattern"`), which caused decode failure returning Left.

- **PrimesRulesTest rule registration** — Tests now call `ruleInstance.pattern.accept(knowledge)` instead of just accessing `ruleInstance.pattern`, which never registered the rules with the knowledge base.

- **Generated rule files** — All three primes rules (PrimesFromNaturalSequence, AddNaturalSequence, RemoveCompositeNumbers) updated with `private lazy val` for ruleDefinition/action/pattern.

## [Previous]

### Added

- **Factory type** - New `Factory` subtype of `BodyElement` combining `parameters` and `body` fields for type construction. Used in `TypeDefinition` instead of separate parameters/body fields.

- **Monadic type** - New `Monadic` subtype of `BodyElement` for side-effecting statements that don't require names. Always has `valueType = "Unit"`. Simplifies rule action bodies by eliminating artificial variable names for effectful operations.

- **globalElements field** - Added `globalElements: Seq[BodyElement]` to `TypeDefinition` for declaring members in companion objects.

- **JSON field elision** - Encoders/decoders for all draco types now elide empty fields from JSON output and handle missing fields gracefully on decode:
  - `TypeElement` - elides empty `name`, `valueType`, `value`, `parameters`, `body`
  - `TypeDefinition` - elides empty `modules`, `derivation`, `elements`, `factory`, `globalElements`, `rules`
  - `RuleDefinition` - elides empty `variables`, `conditions`, `values`, `action`
  - `TypeName` - elides empty `namePackage`, `parent`
  - `Value` - elides empty `name`, `pathElements`
  - `DomainDefinition` - elides empty `elementTypeNames`, `superdomain`, `source`, `sink`

### Changed

- **TypeDefinition field renames**:
  - `moduleElements` → `modules`
  - `derivesFrom` → `derivation`

- **Dynamic type** - Now uses `parameters` and `body` fields instead of `value` for method definitions with parameters.

- **Rule trait** - Split `rule: Knowledge => Unit` into two separate concerns:
  - `pattern: Knowledge => Unit` - for matching/binding
  - `action: RhsContext => Unit` - for execution

- **Generator improvements**:
  - Generates separate `action` and `pattern` vals instead of single `rule`
  - Condition functions declared before they are referenced in `.where()` clauses
  - Uses fully qualified class names in generated code (no import statements)
  - Uses `java.util.function.Consumer[org.evrete.api.RhsContext]` for action type to match Evrete's Java API
  - Condition functions in `.where()` use fully qualified class reference for Evrete's runtime compilation

- **RuleDefinition encoder** - Fixed field name inconsistency: encoder now uses `"typeName"` instead of `"name"` to match decoder.

### Fixed

- **Rule.apply** - Now correctly returns a `Rule` instance instead of `Unit`.

- **Evrete integration** - Condition functions now work with Evrete's runtime Java compilation by using fully qualified class names in `.where()` expressions.

### Removed

- **@static annotation** - Removed from generated condition functions. Scala 2.13 companion object methods provide static forwarders that are accessible from Java.

- **Import statements in generated rules** - All type references in generated rule code now use fully qualified names.

## Notes

This changelog summarizes changes made with Claude Code assistance. The project uses:
- Scala 2.13
- Evrete rules engine
- Apache Pekko actors
- Circe for JSON serialization
