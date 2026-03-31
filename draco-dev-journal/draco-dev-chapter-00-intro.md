# Draco Dev Journal — Introduction

This journal documents the collaborative development of Draco, a self-describing domain-driven rule engine, across eighteen development sessions between March 22 and March 31, 2026. An earlier session predating the journal (where initial companion object consistency work began) is referenced in Chapter 2 but was not captured. The sessions are transcribed as dialogues between Dev (the framework's creator) and Draco (Claude, serving as pair-programming partner), capturing not just the code changes but the reasoning, missteps, and discoveries along the way.

## Development Trajectory

### Where It Started

When the first session began, Draco already had a working type hierarchy, a RETE-based rule engine (via Evrete), Pekko actors, and Circe JSON serialization. But the framework was fragile in ways that weren't yet visible. Companion objects used plain `val` for fields that crossed object boundaries, a timebomb hidden by Scala 2's `DelayedInit` trait. The Generator could produce basic Scala source from type definitions but had never been tested against its own output. Three separate definition types — `DomainDefinition`, `RuleDefinition`, `ActorDefinition` — each carried their own structure, creating asymmetry in what was meant to be a symmetric system.

### The Breakthroughs

Several moments across these sessions shifted the framework's trajectory:

**Extensible and Specifically** (Chapter 1). The search for a structural root type hit Scala's linearization wall — `Extensible[T]` forced all descendants to inherit the root's type parameter. The solution was elegant: make `Extensible` non-parameterized as a trait, but let the Generator substitute type parameters when it appears in a derivation. Its dual, `Specifically[T]`, provides deferred specialization. Together they give Draco a way to express both generalization and specialization structurally, without the language fighting back.

**Actors as thin membranes** (Chapter 1). The architecture of Draco's actor system crystallized when the distinction between within-domain and cross-domain communication clicked: rules handle all logic within a domain; actors are merely the membrane between domains, doing nothing more than `session.insert(msg); session.fire(); Behaviors.same`. ActorRefs live in the Evrete Environment, not in actor state. This was the moment the rule engine and the actor system became one integrated design rather than two separate subsystems.

**The DelayedInit reckoning** (Chapters 2, 6). What began as a simple NPE in a rule companion uncovered a systemic problem: Scala 2's `App` trait delays all `val` initializers, and in a self-describing system where types reference each other's definitions, this creates a minefield of nulls. The fix — `lazy val` everywhere — sounds mechanical, but the four-layer bug hunt in Chapter 6, where each fix unmasked the next deeper problem, revealed how self-referential systems can hide bugs behind other bugs. The discipline that emerged (every `val` in an `extends App` companion must be `lazy val`) became a foundational rule of the framework.

**Language-neutral naming** (Chapter 2). Renaming `companionObject` to `typeGlobal` and `typeGlobals` to `globalElements` was more than cosmetic. It was the realization that Draco's own vocabulary — derivation, elements, factory, modules — maps naturally across programming languages. This opened the door to `Generator[L]`, a generator parameterized by target language rather than hardcoded to Scala.

**Triadic symmetry** (Chapter 10). The decision to keep `DomainInstance`, `RuleInstance`, and `ActorInstance` all unparameterized — using `asInstanceOf` at the Pekko boundary rather than breaking structural symmetry — was a deliberate choice of architectural consistency over type-level precision. It preserves the principle that all three instance traits are structurally identical.

**TypeDefinition unification** (Chapter 14). Dissolving `DomainDefinition`, `RuleDefinition`, and `ActorDefinition` into a single `TypeDefinition` with optional fields was the culmination of the entire arc. It exposed a new class of problem — circular initialization when factory defaults like `Pattern.Null` call back into `TypeDefinition.apply` — and produced a general rule: factory defaults for types that participate in self-description must be deferred. The more TypeDefinition absorbs, the more defaults it needs, and the more opportunities for these cycles. This tension is inherent to self-describing systems.

**JSON as single source of truth** (Chapters 10-11). Replacing inline Scala literals with `TypeDefinition.load(typeName)` reading from classpath JSON files was the turning point toward Dreams. Once JSON files are canonical and generated Scala loads from them at runtime, the path to a visual editor becomes clear: Dreams edits JSON, invokes the Generator, and the framework regenerates itself.

### Where It's Heading

The journal points toward several convergent goals:

- **Dreams** — Domain Rules Editor Actor Message Service. A visual environment for creating and modifying Draco types, domains, rules, and actors by editing their JSON definitions. Dreams is the reason JSON became the source of truth.

- **Generator[L]** — The Generator becoming a self-describing domain itself, parameterized by target language, with its own types, rules, and actors. Capability domains (`draco.rete`, `draco.json`, `draco.actor`, `draco.scala`) would provide language-specific generation rules.

- **Orion** — A system-of-systems architecture built on five ION interaction patterns (PSION, ANION, CATION, IONIC, UNION) governing how domains communicate. Data domains vs. message domains, with actors as the membrane between them.

The recurring theme across all eighteen sessions is that self-description creates complexity: a type system closed over itself means every change to a foundational type can create circular dependencies, initialization order bugs, and shadowing issues. The `lazy val` discipline, deferred factory defaults, and classpath-based loading are all engineering responses to this mathematical property. The framework is learning to describe itself without tripping over its own reflection.

---

## Chapters

### [Chapter 1 — Extensible, Specifically, Actors as Membranes, Pipeline Validation](draco-dev-chapter-01.md)

The most architecturally significant session. Created `Extensible` (non-parameterized structural root) and `Specifically[T]` (deferred specialization). Defined actors as thin membranes with all logic in rules. Validated the full Alpha-to-DataModel-to-Bravo pipeline end-to-end. Introduced Orion and the five ION patterns.

### [Chapter 2 — lazy val Normalization, DelayedInit Discovery, Generator Pre-Requisites](draco-dev-chapter-02.md)

The session that uncovered the `DelayedInit` timebomb. A `NullPointerException` in a rule companion led to a sweep of all 28+ companion objects, changing `val` to `lazy val` for every cross-object field. Established the foundational rule and identified Generator pre-requisites. Introduced language-neutral naming (`typeGlobal`, `globalElements`).

### [Chapter 3 — Generator Renames, Factory Semantics, Transform Domain Cleanup](draco-dev-chapter-03.md)

Implemented the three Generator pre-requisites from Chapter 2. Established `Factory.Null` semantics, cleaned up the transform domains (Alpha, DataModel, Bravo, Charlie, Delta), and designed the multi-type generation approach with topological sorting via `moduleOrder`.

### [Chapter 4 — Multi-Type File Generation, JSON Embedding Plan](draco-dev-chapter-04.md)

Implemented multi-type generation for sealed hierarchies like `TypeElement`. Discovered a subtlety: `sealed` is driven by the `modules` field, not by annotation. Proposed replacing composed factory-call literals with embedded JSON strings, and discovered a pre-existing `RuleDefinition` decoder bug.

### [Chapter 5 — JSON Embedding, Confidence Assessment, Generator Roadmap](draco-dev-chapter-05.md)

Replaced five literal-composition methods with two JSON-embedding helpers. A candid confidence assessment recommended against replacing hand-written framework types with Generator output, identifying the gaps that needed closing first. Established the natural sequence: consistency, imports, codecs, rules auto-generation.

### [Chapter 6 — Generator Consistency, Four-Layer Bug Hunt, Rule Imports](draco-dev-chapter-06.md)

The deepest debugging session. Four layers of bugs peeled back one by one — `implicit lazy val` for codecs, `private lazy val` for rule fields, the `RuleDefinition` decoder reading the wrong JSON field, and missing imports in generated rule source. Corrected the `lazy val` rule and wrote the first `README.md`.

### [Chapter 7 — Codec Generation Planning](draco-dev-chapter-07.md)

A design-only session. Specified the three codec patterns in full detail, formalized elision rules (driven by factory parameter defaults), and noted the tension between elision and factory defaults.

### [Chapter 8 — Codec Generation Implementation, DomainDefinition Design](draco-dev-chapter-08.md)

Implemented all three codec generation patterns: simple field-based, discriminated union with `kind` dispatch, and `Codec.sub` wiring. Discovered that pattern detection order matters for intermediate sealed traits. Designed `DomainDefinition` with morphism semantics — source equals sink means endomorphism (Update), source differs from sink means Transform.

### [Chapter 9 — DomainDefinition, Domain\[T,U\] Experiment, TypeName Redesigns](draco-dev-chapter-09.md)

Created `DomainDefinition` across 18+ files, replacing the lightweight `DomainName`. A `Domain[T,U]` experiment failed against Scala's mixin requirements. Fixed `Actor.apply`, added `ActorDefinition` codecs, and redesigned `TypeName` with role-specific paths.

### [Chapter 10 — Logging, NaturalActor Fix, Triadic Symmetry, Aspect Naming](draco-dev-chapter-10.md)

Suppressed SLF4J/Pekko logging noise. Diagnosed and fixed the `NaturalActor` — `Actor.apply` was creating a no-op behavior, ignoring the custom `receive`. Made the triadic symmetry decision and introduced aspect-based naming conventions. Established JSON as the single source of truth.

### [Chapter 11 — TypeDefinition.load, typeParameters, JSON Definition Files](draco-dev-chapter-11.md)

The session where JSON-first generation came alive. `TypeDefinition.load` replaced inline embedding with classpath loading. Solved bootstrap recursion with `override lazy val`. Added `typeParameters` to `TypeName`. Created 10+ JSON definition files and the first successful generate-from-JSON tests.

### [Chapter 12 — Session Persistence, Journal Self-Propagation](draco-dev-chapter-12.md)

A brief session exploring session data persistence. Notable for the temporal self-reference: instructions from a later session bootstrapped this session's journal transcription, establishing the self-propagating pattern used for subsequent chapters.

### [Chapter 13 — Complete JSON Definitions, Generated Test Infrastructure](draco-dev-chapter-13.md)

Completed all remaining JSON definition files across the full type hierarchy. Built the test infrastructure for generated output (`src/generated/scala/draco/*.scala.generated` with diff comments). Reached 28 passing Generate tests. Documented the Generator evolution plan toward `Generator[L]`.

### [Chapter 14 — TypeDefinition Unification](draco-dev-chapter-14.md)

The culminating session. Dissolved `DomainDefinition`, `RuleDefinition`, and `ActorDefinition` into a single unified `TypeDefinition`. Discovered and solved circular initialization in factory defaults. Collapsed the Generator from five overloads to two with detection-based dispatch. Established the DRACO.md symlink convention and the dev journal structure.

### [Chapter 15 — Journal Creation Methodology](draco-dev-chapter-15.md)

A meta-chapter documenting how the dev journal itself was built: using `/resume` to re-enter past sessions, a fixed self-propagating transcription prompt, reverse-chronological creation order followed by renumbering, and cross-reference correction. Describes the three properties that make the process work — session data as source of truth, self-propagating format, and the model as its own witness.

### [Chapter 16 — Update All Domains to New Conventions](draco-dev-chapter-16.md)

Flat packages (removed `rules/`, `actor/`, `unit/` subdirectories), aspect naming (`.rule.json`, `.actor.json`), empty JSON files populated, 9 Base element JSONs created, all 8 domain Scala sources standardized. Established domain discovery convention — JSON for domains contains only type identity; Generator discovers contents by scanning.

### [Chapter 17 — Alpha Release: CLI, REPL\[L\], sbt-assembly, GitHub Release](draco-dev-chapter-17.md)

Fixed override lazy val bug in 8 domain anonymous classes. Created CLI (object-only entry point) and REPL[L] (language-parameterized evaluator). Introduced `isObjectOnly` and `hasExplicitMain` in Generator. Published v2.0.0-alpha.1 to GitHub with fat JAR.

### [Chapter 18 — Generator Owns Type Loading, Rule/Actor Auto-Suffix](draco-dev-chapter-18.md)

Identified Int vs Integer bug in RemoveCompositeNumbers. Eliminated inline JSON duplication — rules load from classpath via Generator.loadRuleType. Moved all type loading from TypeDefinition.load to Generator (loadType/loadRuleType/loadActorType/loadAll). Established auto-suffix convention: Generator appends "Rule"/"Actor" from aspect naming. Began prime gap rules design.
