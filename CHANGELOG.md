# Changelog

All notable changes to the Nexonix/Draco project will be documented in this file.

## [Unreleased]

### Changed

- **`Egocentric` → `Sentient`, reparented to `World`** — the perspective frame was renamed and pulled out of the (now-deleted) reference-frame family to become a `World` subdomain (`trait Sentient extends World`), a media peer of Aerial/Terrestrial/Marine/Ethereal. Relocated from `src/test/.../domains/egocentric/` to `src/mods/.../domains/sentient/`; its six Egocentric↔{Geo,Helio,Galacto} transforms were deleted (it now crosses through the `Observable` world-fact rather than the `DomainTransform` matrix). `WorldHierarchyTest` asserts `Sentient <:< World`. Covers dev-journal chapter 47.

### Removed

- **Reference-frame example domains** — `Cosmocentric` and the `Geocentric` / `Heliocentric` / `Galactocentric` peer frames, their leaves, the surviving transform matrix, and `domains.DomainsGenTest` were deleted: a significant maintenance surface with minimal return. Closed issues #2/#3/#4/#7 (now moot); #5/#19/#25 stay open, retargeted at the surviving `Sentient` types. World's `geocentric`/`heliocentric` are coordinate-frame field names on `Observable`/`Cartesian` and are unaffected.

---

## [2.0.0-alpha.5] - 2026-06-03

The first `src/mods` usability/scalability stand-in. `DomainBuilder` makes a domain dictionary buildable, validatable, and generatable from JSON alone; its rigorous `validate` over the four endogenous domains drove `Generator`/`GeneratorCLI` out of the JSON-backed type system into a root-compiled `src/mods/scala/draco` staging tier; and the toolchain moved to sbt 1.12.9. Covers dev-journal chapter 40.

### Added

- **`draco.DomainBuilder`** (`src/mods/scala/draco/`) — First `src/mods` usability/scalability stand-in for an under-development core capability: comprehensively build a domain dictionary from JSON, stand up a *populated* concrete instance, validate it, and generate code. Public-API-only (no new deps). Functions: `define` (loads a domain plus every member's full `TypeDefinition` — the non-hollow counterpart to `TypeDictionary.apply`), `dictionary`, `validate` (self-declaration + completeness + derivation resolvability), `generate` (skeleton-tolerant). Tested by `DomainBuilderTest` over Draco/Base/Primes/Language.

- **`src/mods/scala/draco` staging tier** — A second mods compilation track: this directory is compiled *into* `root` (via `Compile / unmanagedSourceDirectories`), so hand-written Scala there ships in the draco jar, is conflict-checked against `src/main/scala/draco` as same-package/same-project, and is testable from `src/test` with no cross-project cycle. The `mods` subproject is scoped to `scala/scripts`. Documented in `src/mods/README.md`.

### Changed

- **`Generator` and `GeneratorCLI` relocated to `src/mods/scala/draco/`** — The hand-written engine and its CLI move out of `src/main/scala/draco/` (still compiled into root via `unmanagedSourceDirectories`, so all ~50 `Generator.loadType` callers are unchanged and `draco.Generator` stays in the jar). `Generator` is removed from `Draco.json` / `Draco.scala` `elementTypeNames` — it is infrastructure, not a JSON-backed domain member. Establishes `src/mods/scala/draco/` as "hand-written Scala in the jar, outside the JSON type system" (stand-ins + permanent engine code), enforcing the invariant that a declared Draco domain member is JSON-backed (what `DomainBuilder.validate` checks).

### Build

- **sbt 1.7.3 → 1.12.9** — `project/build.properties` bumped to the current sbt 1.x. This required **removing** the build's `managedScalaInstance := false` setting and both hand-supplied `scala-tool` configurations (root + `mods`): sbt 1.12's Zinc loads the compiler bridge via the `CompilerInterface2` `ServiceLoader`, which the manual bridge setup couldn't satisfy (`ServiceConfigurationError: ... CompilerBridge Unable to get public no-arg constructor`). Letting sbt auto-manage the Scala instance resolves the matching `2.13.16` bridge correctly. `jline` (a REPL runtime dependency, previously only in the scala-tool config) is now a normal `libraryDependencies` entry.

---

## [2.0.0-alpha.4] - 2026-05-29

The JSON-normative release: JSON is the sole runtime load path, the entire `src/main/scala/draco/` package is byte-equivalent to Generator output, and the user-facing tooling (`bin/draco-gen`, `bin/draco-sc`, `src/mods`) and README are brought current. Covers dev-journal chapters 37–39.

### Added

- **`TypeElement.value` field** — A `Fixed` element (default `""`) so default-bearing elements survive a YAML→JSON round-trip without their defaults being stripped. The trait carries `lazy val value: String = ""`; the encoder emits `value` when non-empty (closes #28, #18).

- **`bin/draco-gen from-yaml` / `to-yaml`** — Convert between a JSON definition and its human-authoring YAML twin, with git-aware safety (a conversion refuses to clobber dirty/untracked work without `--force`).

- **`bin/draco-gen discover` / `verify`** — `verify <domain-json>` checks a domain's `elementTypeNames` against its package siblings on disk (exits non-zero on drift); `discover <domain-json> [--force]` rewrites the list from directory contents, sorted types → rules → actors (closes #9).

- **`bin/draco-sc` + `src/mods/scala/scripts/`** — A scala-cli script runner over the assembled draco jar, plus the first script batch: `list-domain`, `list-domains`, `who-extends`, `diff-type` (joining `inspect-type`, `derivation-chain`). For runtime queries a jar-only user would otherwise need a test for.

- **Getting Started walkthrough (README)** — A minimal define-JSON → generate → register → use → verify loop, seeded for later expansion.

### Changed

- **JSON-normative load path** — `Generator` loading is JSON-only: `tryLoad` does a single JSON lookup, `loadFromResource` lost its YAML branch, and `resourcePath` no longer takes an extension parameter. YAML is a human-authoring stand-in visible only to `from-yaml` / `to-yaml`, never to the loader.

- **`DracoGenTest.comparisonOnlyExcluded` → `Map.empty`** — The last four hand-customized type declarations (`Accumulator`, `Numbers`, `Primes`, `YAML`) were re-expressed in generator-canonical form, so every file under `src/main/scala/draco/` is now byte-equivalent to Generator emission. `Generator` gained targeted `import scala.collection.mutable` emission when any `valueType` contains `mutable.` (closes #29).

- **`DracoType` derivation declared on `CLI` / `REPL` / `Value` / `Unit`** — First PoC batch of the canonicalization umbrella: each gains `dracoAspect.derivation: [DracoType]` in JSON, regenerated to match. `who-extends DracoType` coverage rose 44 → 48 (#38, 4 of 12).

- **`src/mods/` layer policy** — Codified as a permanent third source tier for speculative outer layers (`draco.dreams`, `draco.dreams.orion`); `mods → main` references allowed, `main → mods` forbidden.

- **README.md** — Full rewrite (+319 / −200) aligning it with the post-alpha.3 architecture: DracoType-as-root, the companion-val convention, aspect blocks, the four endogenous domains as the example backbone, the 5-way Generator dispatch, JSON-normative policy, and the current tooling.

- **`build.sbt`** — Version `2.0.0-alpha.3` → `2.0.0-alpha.4`.

### Fixed

- **Codec asymmetry** — Defaults were silently stripped on YAML→JSON round-trip because `TypeElement` omitted `value` from its encoded form (closes #28, #18; root cause shared with #29).

- **`TypeName` reference-equality** — `TypeName` has no structural `equals`/`hashCode`, so `apply`-produced instances compared unequal by reference and `domainAspect.typeName == typeName` was always false. Worked around with `.namePath == .namePath` in five sites (`GeneratorCLI` + mods scripts); structural-equality root fix tracked in #37.

- **`inspect-type` / `derivation-chain` null check** — The `td == TypeDefinition.Null` guard never fired (`loadType` returns a typeName-only placeholder, not `Null` on miss); replaced with aspect-emptiness detection. Also removed a stale `PrimeOrdinal` entry from `Primes.json` surfaced by `verify`.

### Removed

- **`YamlToJsonBootstrap.scala`** — One-shot bootstrap superseded by the `bin/draco-gen from-yaml` CLI.

---

## [2.0.0-alpha.3] - 2026-05-17

The architectural-consolidation release. `DracoType` becomes the universal root, the type system is reorganized into aspect blocks, and the whole `draco.*` package becomes generatable from — and byte-equivalent to — its JSON. Several scaffolding types from the alpha.2 era are eliminated. Covers dev-journal chapters 20–36. Closes #1, #6, #8, #17, #24, #26, #27.

### Added

- **`draco.Holon[T <: Product]`** — Perspective marker onto composite structure; an axis distinct from `Primal[T]` (value). Now `extends DracoType`.

- **Aspect blocks** — `TypeDefinition` reorganized to extend a new `Aspects` parent carrying four sub-aspects: `DracoAspect` (shared structure), `DomainAspect` (membership), `RuleAspect` (rule LHS/RHS), `ActorAspect` (handlers). Each is itself a `DracoType` with its own JSON definition.

- **`DomainAspect.typeName`** — Every type declares its containing domain: a self-loop means the type *is* a domain, a container-pointer means it is a leaf. The universal domain-vs-leaf discriminator, replacing package-scan heuristics.

- **`isLeaf` predicate + flat dispatcher** — `Generator.generate` restructured into an explicit `isRule` / `isDomain` / `isObjectOnly` / `isLeaf` / `isActor` table with an exhaustivity guard (closes #24, #27).

- **`TypeTransform[S, T]` and `DomainTransform[S, T]`** — The overloaded `Transform[S, T]` split into a type-to-type and a domain-to-domain transform; both `extend Holon[(S, T)]`.

- **`draco.language` sub-domain + YAML authoring** — `Language` and `YAML` types; YAML promoted to a human-authoring surface alongside the canonical JSON.

- **`draco.GeneratorCLI` + `bin/draco-gen`** — Bash-invocable Generator CLI in the sbt-assembly fat JAR (`generate` / `compile` / `compile-multi` / `inspect`).

- **`src/mods/` source tier** — A third tier alongside `main` and `test`, with the script toolkit migrated in under an sbt `mods` subproject.

- **Reference-frame example domains (Increments A–C)** — Cosmocentric super-domain plus the Egocentric / Geocentric / Heliocentric / Galactocentric peer frames and the full 4×3 transform matrix, with `domains.DomainsGenTest` (renamed from `ReferenceFramesGenTest`). Retained under `src/test/` but de-prioritized as teaching material.

- **draco-dev-journal extraction tooling** — Scripts under `draco-dev-journal/tools/` that pull user↔assistant pairs from Claude Code session logs, match them against committed chapters, and surface gaps. Outputs are regenerable and gitignored.

- **GitHub Issues backlog** — Project backlog migrated from `MEMORY.md` to GitHub Issues with a label lifecycle (`roadmap` → `next-feature` → `priority-next`) and a tracking-issue pattern.

### Changed

- **`DracoType` as universal root** — Every type in `draco.*` extends `DracoType` and carries its own `typeDefinition`; the whole package is generatable from JSON.

- **Companion val collapse** — The per-companion `typeInstance` val became `dracoType` / `domainType` (plus `ruleType` / `actorType` where relevant). Companions drop `extends … with TypeInstance`.

- **Actors consolidated into `actorAspect`** — An actor is no longer a separate `.actor` sibling type; its behavior lives as the `actorAspect` block on its parent type's `TypeDefinition` (closes #8, #26).

- **`draco.base` canonicalization** — The seven base measurement types stripped of vestigial `name`/`description` metadata, leaving minimal self-describing types (closes #17).

- **Generator-canonical package** — `src/main/scala/draco/*` swept to be byte-equivalent to Generator emission; `DracoGenTest` walks the resource tree and compares per type.

### Fixed

- **`SourceContent` whitespace** — `sourceLines.mkString` → `mkString("\n")`; a latent bug masked while only JSON (whitespace-immaterial) was consumed, surfaced by the first YAML file read.

- **`PrimesRulesTest.indexDifference`** — Returns `List.empty` for lists with fewer than two elements instead of throwing on `tail of empty list`.

- **CI `release.yml`** — Adds an explicit sbt install step before the test/release phase, resolving a runner failure where sbt was unavailable.

### Removed

- **The `*Instance` trait family** — `TypeInstance`, `DomainInstance`, `RuleInstance`, `ActorInstance` dissolved into the companion-val convention.

- **`Extensible`** — Eliminated mid-sweep; all core traits chain to `DracoType` directly.

- **`Specifically[T]`** — Tag-style trait with no live use.

- **`draco.RuntimeCompiler`** — `compile` / `compileMulti` / `loadClass` migrated onto `Generator`; the standalone type and its test deleted.

- **Inline `TypeDefinition`s** — Removed from all remaining hand-written companions; definition data lives exclusively in JSON resources.

- **`draco.dreams.Transform`** — Obsolete runtime-only transform, superseded by the `Transform` split.

- **Hand-written example-domain test scaffolds** — `Alpha` / `Bravo` / `Charlie` / `Delta` / `DataModel` (and their actors/rules) removed; per-package generation walks now cover their round-trip semantics.

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
