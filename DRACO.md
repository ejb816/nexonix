# DRACO.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
A symlink `CLAUDE.md → DRACO.md` ensures Claude Code auto-loads this file.

## Related Documentation

- **README.md** — Comprehensive overview of the framework, its architecture, semantic preservation goals, and current feature status
- **CHANGELOG.md** — History of changes made with Claude Code assistance
- **Auto-memory** — Persistent project memory at `~/.claude/projects/.../memory/MEMORY.md` with architectural decisions, lazy val rules, Generator naming conventions, and imminent task list
- **GitHub Issues** — `github.com/ejb816/nexonix/issues` is the durable, shared backlog and forward work-tracker (see below)

## Issue Tracking & Session Workflow

GitHub Issues is the canonical, cross-session **backlog and decision log** — the shared, provider-agnostic complement to auto-memory (which holds *durable knowledge*: how the system works, conventions, user feedback). Roughly: **issues track work to do; memory records what is known.** Neither replaces `draco-git-record/` (audit trail) or the dev journal (historical narrative).

- **At session start**, check the backlog for the pickup point: `gh issue list --label priority-next` (the `priority-next` label = "pick this up next session"). Fall back to `gh issue list` for the wider backlog.
- **File an issue for deferred work.** When work is scoped out, a decision is deferred, or a bug is noticed in passing, open an issue (`gh issue create`) with a self-contained body — file paths and enough context to act without the originating conversation — rather than leaving it only in prose. Label it (`generator`, `cleanup`, `tooling`, `bug`, `next-feature`, `docs`, `roadmap`) and add `priority-next` if it's the natural next pickup.
- **Close on completion**, and cross-link related issues instead of duplicating. Memory notes may cite issue numbers and vice versa.

## Build and Test Commands

This is a Scala 2.13 project using sbt.

```bash
# Compile the project
sbt compile

# Run all tests
sbt test

# Run a specific test class
sbt "testOnly draco.primes.PrimesRulesTest"

# Run a specific test within a class
sbt "testOnly draco.primes.PrimesRulesTest -- -z \"PrimesFromNaturalSequence\""

# Continuous compilation (recompile on file changes)
sbt ~compile

# Interactive sbt shell
sbt
```

**Important:** The user handles all compiles, commits, and pushes via their IDE. Do not run sbt, git commit, or git push.

## Architecture Overview

### Draco: Self-Describing Domain-Driven Rule Engine

Draco is a domain-driven rule engine built on Evrete (RETE algorithm), with Apache Pekko for actor-based concurrency. The core principle is **self-description**: every type carries a `TypeDefinition` that describes its own structure, making the type system closed over itself.

### Type Hierarchy

```
DracoType                              -- universal root; carries typeDefinition
  +-- Primal[T]                        -- wraps a single value: T
  +-- TypeInstance                     -- companion objects that register themselves
  |     +-- DomainInstance             -- companions owning a domain
  |     +-- RuleInstance               -- companions owning a rule
  |     +-- ActorInstance              -- companions owning an actor
  +-- DomainType                       -- named domain with type dictionary
  |     +-- Domain[T]                  -- generic domain container
  +-- RuleType                         -- rule with pattern + action
  |     +-- Rule[T]                    -- generic rule container
  +-- ActorType                        -- actor with actor definition
        +-- Actor[T]                   -- generic actor container (extends Pekko ExtensibleBehavior[T])
```

### The Instance Triad

Three parallel traits extend `TypeInstance`:

| Trait | Owns |
|-------|------|
| `DomainInstance` | `domainInstance: DomainType` |
| `RuleInstance` | `ruleInstance: RuleType` |
| `ActorInstance` | `actorInstance: ActorType` |

All three use `TypeDefinition` as their definition type — `DomainType.domainDefinition`, `RuleType.ruleDefinition`, and `ActorType.actorDefinition` are all `TypeDefinition`. The separate `DomainDefinition`, `RuleDefinition`, and `ActorDefinition` types have been dissolved into `TypeDefinition`.

These are deliberately kept structurally symmetric — none is parameterized. When Pekko requires `Behavior[T]`, use `actorInstance.asInstanceOf[Actor[T]]` at the integration boundary.

### Key Files and Their Roles

| File | Role |
|------|------|
| `DracoType.scala` | Universal root trait. The one axiom: `DracoType.typeInstance` is a plain `val` |
| `Primal.scala` | `Primal[T]` — value-carrying base trait |
| `Type.scala` | `Type[T]` — generic type wrapper created via `Type[X](typeDefinition)` |
| `TypeInstance.scala` | Trait for self-registering companion objects |
| `TypeName.scala` | Qualified name with package path and type parameters |
| `TypeDefinition.scala` | Unified schema descriptor: typeName, modules, derivation, elements, factory, globalElements, plus domain fields (elementTypeNames, source, target), rule fields (pattern — which carries variables, conditions — plus values, action), and actor fields (start, message, signal) |
| `TypeElement.scala` | Sealed element hierarchy (Fixed, Mutable, Dynamic, Parameter, Monadic, Pattern, Action, Condition, Variable, Factory) |
| `Codec.scala` | `Codec[T]` encoder/decoder pair; `Codec.sub` for subtype codec derivation |
| `Domain.scala` | `Domain[T]` — generic domain container created from `TypeDefinition` |
| `DomainType.scala` | Trait: domainDefinition (TypeDefinition) + typeDictionary |
| `DomainInstance.scala` | Trait for domain-owning companions |
| `TypeDictionary.scala` | Map of TypeName to TypeDefinition within a domain |
| `DomainDictionary.scala` | Cross-domain registry (Map of DomainType to TypeDictionary) |
| `Dictionary.scala` | Generic Map[K,V] base abstraction |
| `Rule.scala` | `Rule[T]` container; owns the singleton `KnowledgeService` |
| `RuleType.scala` | Trait: ruleDefinition (TypeDefinition) + pattern (Consumer[Knowledge]) + action (Consumer[RhsContext]) |
| `RuleInstance.scala` | Trait for rule-owning companions |
| `Actor.scala` | `Actor[T]` — generic actor container extending `ExtensibleBehavior[T] with ActorType` |
| `ActorType.scala` | Trait: actorDefinition (TypeDefinition) |
| `ActorInstance.scala` | Trait for actor-owning companions |
| `Generator.scala` | Code generation from TypeDefinition JSON; detects domain/rule/actor from aspect content; owns all type loading (`loadType`) |
| `SourceContent.scala` | Reads source files from a URI root |
| `ContentSink.scala` | Writes generated content to output paths |
| `Main.scala` | Default source root (resources) and sink root (scala) URIs |
| `format/Value.scala` | `Value[F]` — path extractor over a format payload: name + pathElements + abstract `value[T: Decoder](_source: F)`; the json sub-domain's `Value` (extends `Value[JSON]`) carries the circe implementation |
| `Draco.scala` | Root domain registering all framework types |

### TypeName

`TypeName` identifies a type with three fields:

| Field | Type | Purpose |
|-------|------|---------|
| `name` | `String` | Simple type name (e.g., `"Natural"`) |
| `namePackage` | `Seq[String]` | Package path (e.g., `Seq("domains", "natural")`) |
| `typeParameters` | `Seq[String]` | Type parameters (e.g., `Seq("T")` for `Primal[T]`); formals on declaring type, actuals on references |

Derived fields:
- `namePath` — `"domains.natural.Natural"` (fully qualified)
- `resourcePath` — `"/domains/natural/Natural.json"` (JSON file path)

Domain-ness is structural (name matches last package element). Rule/actor roles are expressed through derivation in TypeDefinition, not through TypeName.

### TypeElement Sealed Hierarchy

All defined in `TypeElement.scala`:

```
TypeElement (sealed)  extends Primal[String]
  -- name, valueType, parameters: Seq[Parameter], body: Seq[BodyElement]
  +-- BodyElement (sealed)
        +-- Fixed          -- val name: Type = value
        +-- Mutable        -- var name: Type = value
        +-- Dynamic        -- def name(params): Type = body
        +-- Parameter      -- constructor/method parameter
        +-- Monadic        -- side-effecting statement (no name, Unit)
        +-- Condition      -- Boolean predicate: parameters + value expression
        +-- Action         -- rule RHS: variables + values + body
        +-- Pattern        -- rule LHS: variables + conditions
        +-- Variable       -- Evrete fact variable: name + valueType
        +-- Factory        -- apply() spec: valueType + parameters + body
```

JSON serialization uses `"kind"` discriminator. `Codec.sub` creates subtype codecs from the parent `TypeElement` encoder/decoder.

### Companion Object Pattern

Every companion object follows the same pattern:

```text
object MyType extends App with TypeInstance {           // or DomainInstance, RuleInstance, ActorInstance
  lazy val typeDefinition: TypeDefinition = ...         // structural description
  lazy val typeInstance: Type[MyType] = Type[MyType](typeDefinition)
  // optional: def apply(...), lazy val Null, lazy val Default, domainInstance, ruleInstance, actorInstance
}
```

### DelayedInit / lazy val Rules

`App` uses `DelayedInit` in Scala 2 — ALL `val` initializers are delayed until `main()`. This causes null when accessed cross-object. The rule: **every val in an `extends App` companion must be `lazy val`** unless it is purely local to a non-lazy context.

- `typeDefinition`, `typeInstance` → `lazy val`
- `domainInstance`, `ruleInstance`, `actorInstance`, `knowledgeService` → `lazy val`
- `Null` → `lazy val`
- Encoders/decoders → `implicit lazy val`
- Private vals referenced by lazy vals (action, pattern, ruleDefinition) → `private lazy val`
- globalElements Fixed vals → `lazy val` (Generator emits this)
- Factory body `typeInstance`/`typeDefinition` overrides → `override lazy val` (prevents bootstrap recursion)
- Null instance `typeInstance`/`typeDefinition` overrides → `override lazy val` (prevents mutual Null recursion)
- Exception: `DracoType.typeInstance` is `val` (axiom, doesn't extend App)

### Generator

`Generator.scala` has two `generate` overloads:

1. `generate(td: TypeDefinition)` — detects type role and dispatches:
   - **Rule** (`td.variables.nonEmpty`) → rule imports + `ruleGlobal` (RuleInstance companion)
   - **Domain** (`td.elementTypeNames.nonEmpty`) → domain imports + `domainGlobal` (DomainInstance companion)
   - **Actor** (derivation contains ActorType/ActorInstance/ExtensibleBehavior) → Pekko imports + `typeGlobal`
   - **Otherwise** → plain `typeGlobal` (TypeInstance companion)
2. `generate(tds: Seq[TypeDefinition])` — multi-type in one file, topologically sorted

Key internal methods:
- `isDomain(td)` / `isRule(td)` / `isActor(td)` — detection helpers inspecting TypeDefinition content and derivation
- `loadType(typeName)` — classpath-based runtime type loading from `<name>.json` (every type, rule, and actor loads via this single path; rule-/actor-ness is carried by the aspect, not a name suffix)
- `typeDefinitionLoad(td)` — emits `draco.Generator.loadType(TypeName(...))` in generated code (for every type, rules included)
- `parameterizedName(tn)` / `wildcardTypeName(tn)` — TypeName-aware helpers using `typeParameters` field
- `factoryBody(factory)` — uses `Factory.body` when non-empty, parameter-derived overrides when empty; appends `override lazy val typeInstance/typeDefinition`
- `nullInstance(typeName, elements, factory)` — uses `wildcardTypeName` for parameterized types (e.g., `Null: Actor[_]`); `apply[Nothing]()` for type params; `apply()` for simple factories; direct element overrides for computed factories
- `nullValueFor(valueType, defaultValue)` — type-appropriate defaults ("" for String, Seq.empty, 0, false, etc.)
- `typeExtends(derivation)` — empty derivation → `extends Extensible`; derivation[0] == "Extensible" with typeParams → substitute typeParam; otherwise → `extends Extensible with derivation[0] with ...`
- `externalTypeImports` — lookup table mapping external type names (URI, BufferedSource, KnowledgeService, Consumer, etc.) to import statements
- `externalImports(td)` — scans valueTypes across elements/factory/globalElements and returns matching external imports
- `packageHierarchyImports(namePackage)` — shared: `draco._` + parent package chain
- `typeImports(td, hasCodec, instanceType)` — combines package hierarchy + circe + Pekko + external imports
- `ruleImports(namePackage)` — package hierarchy + Evrete framework imports
- `domainGlobal(td)` / `domainInstanceLiteral(objName, td)` — DomainInstance companion generation from TypeDefinition
- `ruleGlobal(td)` — RuleInstance companion generation from TypeDefinition's rule fields
- `conditionFunctions` / `whereConditions` — Evrete condition compilation (fully qualified class names required)
- Codec generation: `simpleCodecDeclaration` (only when factory params ⊆ element names), `discriminatedCodecDeclaration`, `subtypeCodecDeclaration`

**Rule names are bare (no suffix):** A rule generates a Scala object named for its bare concept — `"AddNaturalSequence"` in JSON → `object AddNaturalSequence` — exactly like actors and plain types. A type is a rule by carrying a `ruleAspect` (detected via `isRule`), never by a name suffix; `ruleGlobal` uses `td.typeName.name` directly. Actors likewise keep their bare name (no "Actor" suffix) — actor-ness is the `actorAspect`, object named as authored (e.g., `Consumer`). (The Generator historically appended "Rule" to rule objects — the last remnant of the `.rule`/`.actor` name-suffix holdover #40 retired; removed 2026-07-22 so generated class names obey the same "presence, not name" principle as the JSON filenames and rule/actor detection.)

**Important:** PrimesRulesTest "Generate" tests overwrite rule source files with Generator output. Generated code must include imports and use `private lazy val` for action/pattern/ruleDefinition.

### JSON as Single Source of Truth

JSON definition files are the canonical representation for types, domains, rules, and actors:

1. **JSON file is the source of truth** — every definition lives in a bare `<TypeName>.json` file (e.g. `AddNaturalSequence.json`, `Consumer.json`). Rule-/actor-ness is carried by the `ruleAspect`/`actorAspect`, never by a name suffix. All files live in the domain's resource directory (no `rules/` or `actor/` subdirectories).
2. **Domain discovery** — a domain's JSON contains only its type identity (typeName + derivation). The Generator discovers the domain's contents by scanning the resource directory for valid TypeDefinition JSON files. No `elementTypeNames` in JSON.
3. **Generator owns all type loading** — `Generator.loadType(typeName)` loads a definition from the classpath using `getResourceAsStream` (`<name>.json`), for every type regardless of aspect. `TypeDefinition.load` has been removed.
4. **Generator emits load calls** — `draco.Generator.loadType(TypeName(...))` for every generated type, rules included (fully qualified for portability across packages)
5. **Dreams edits JSON** — to create or modify a type, Dreams writes the `.json` file and invokes the Generator to regenerate Scala source. Dreams will use `SourceContent`/`ContentSink` for file I/O (not classpath loading).

### Domains

Domains are peers (Draco, Base, Primes) in the `DomainDictionary`, not hierarchical.

**Base domain** (`draco.base`): Cardinal[T], Distance[T], Meters, Rotation[T], Radians, Ordinal, Nominal, Coordinate[T <: Product]. Cardinal is unconstrained (no Numeric bound on T). Coordinate is compositionally self-describing (no named Cartesian/Polar/Spherical types).

**Primes domain** (`draco.primes`): Working example of the rule engine. Accumulator (mutable state), Numbers (input sequences), PrimeOrdinal (recursive ordinal type), three rules defined in JSON (`AddNaturalSequence.json`, etc.) and generated as bare Scala objects (`AddNaturalSequence`, `PrimesFromNaturalSequence`, `RemoveCompositeNumbers`). Evrete working memory uses boxed `java.lang.Integer` — rule variables must use `classOf[Integer]`, not `classOf[Int]`.

**Transform domains** (in test): Examples: Alpha, Bravo, Charlie, Delta extend DataModel.

### Actors

`Actor[T]` extends `ExtensibleBehavior[T] with ActorType`. The companion `Actor.apply[T]` creates a default no-op actor from a `TypeDefinition`. Custom actors override `receive` and `receiveSignal` in the `actorInstance` definition.

To pass an `actorInstance` to Pekko's `ActorSystem[T]`, cast: `actorInstance.asInstanceOf[Actor[T]]`. This adapter pattern preserves the triadic symmetry of `DomainInstance`/`RuleInstance`/`ActorInstance` — none is parameterized.

### JSON Serialization

All domain types use Circe with field elision (empty fields omitted on encode, defaulted on decode). The `"kind"` discriminator field in TypeElement enables polymorphic dispatch. Sub-type codecs use `Codec.sub` to narrow the parent encoder/decoder.

### Logging Configuration

- `src/main/resources/logback.xml` — Runtime logging config for Dreams; Pekko set to WARN
- `src/test/resources/logback-test.xml` — Test logging config; same Pekko suppression
- `build.sbt` — `fork := true` with `-Dslf4j.provider=ch.qos.logback.classic.spi.LogbackServiceProvider` to eliminate SLF4J initialization replay warnings
- Only `logback-classic` as SLF4J provider (no `slf4j-jdk14`)

## Key Dependencies

- **Evrete** (`org.evrete`) — RETE-based rule engine; condition functions require fully qualified class names for runtime Java compilation
- **Apache Pekko** — Typed actor system
- **Circe** — JSON parsing and encoding
- **Logback** — SLF4J logging implementation
- **ScalaTest** — Testing framework
