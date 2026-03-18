# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Related Documentation

- **README.md** — Comprehensive overview of the framework, its architecture, semantic preservation goals, and current feature status
- **CHANGELOG.md** — History of changes made with Claude Code assistance
- **Auto-memory** — Persistent project memory at `~/.claude/projects/.../memory/MEMORY.md` with architectural decisions, lazy val rules, Generator naming conventions, and imminent task list

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

| Trait | Owns | Definition Type |
|-------|------|-----------------|
| `DomainInstance` | `domainInstance: DomainType` | `DomainDefinition` |
| `RuleInstance` | `ruleInstance: RuleType` | `RuleDefinition` |
| `ActorInstance` | `actorInstance: ActorType` | `ActorDefinition` |

These are deliberately kept structurally symmetric — none is parameterized. When Pekko requires `Behavior[T]`, use `actorInstance.asInstanceOf[Actor[T]]` at the integration boundary.

### Key Files and Their Roles

| File | Role |
|------|------|
| `DracoType.scala` | Universal root trait. The one axiom: `DracoType.typeInstance` is a plain `val` |
| `Primal.scala` | `Primal[T]` — value-carrying base trait |
| `Type.scala` | `Type[T]` — generic type wrapper created via `Type[X](typeDefinition)` |
| `TypeInstance.scala` | Trait for self-registering companion objects |
| `TypeName.scala` | Qualified name with package path, aspects, and derived paths |
| `TypeDefinition.scala` | Schema descriptor: typeName, modules, derivation, elements, factory, globalElements |
| `TypeElement.scala` | Sealed element hierarchy (Fixed, Mutable, Dynamic, Parameter, Monadic, Pattern, Action, Condition, Variable, Factory) |
| `Codec.scala` | `Codec[T]` encoder/decoder pair; `Codec.sub` for subtype codec derivation |
| `Domain.scala` | `Domain[T]` — generic domain container created from `DomainDefinition` |
| `DomainType.scala` | Trait: domainDefinition + typeDictionary |
| `DomainDefinition.scala` | Domain definition: typeName, elementTypeNames, superDomain, source, target |
| `DomainInstance.scala` | Trait for domain-owning companions |
| `TypeDictionary.scala` | Map of TypeName to TypeDefinition within a domain |
| `DomainDictionary.scala` | Cross-domain registry (Map of DomainType to TypeDictionary) |
| `Dictionary.scala` | Generic Map[K,V] base abstraction |
| `Rule.scala` | `Rule[T]` container; owns the singleton `KnowledgeService` |
| `RuleType.scala` | Trait: ruleDefinition + pattern (Consumer[Knowledge]) + action (Consumer[RhsContext]) |
| `RuleDefinition.scala` | JSON-serializable rule specification (variables, conditions, values, pattern, action) |
| `RuleInstance.scala` | Trait for rule-owning companions |
| `Actor.scala` | `Actor[T]` — generic actor container extending `ExtensibleBehavior[T] with ActorType` |
| `ActorType.scala` | Trait: actorDefinition |
| `ActorDefinition.scala` | Actor definition metadata |
| `ActorInstance.scala` | Trait for actor-owning companions |
| `Generator.scala` | Code generation from TypeDefinition and RuleDefinition JSON |
| `SourceContent.scala` | Reads source files from a URI root |
| `ContentSink.scala` | Writes generated content to output paths |
| `Main.scala` | Default source root (resources) and sink root (scala) URIs |
| `Value.scala` | JSON path extractor: navigates pathElements to extract typed values |
| `Draco.scala` | Root domain registering all framework types |

### TypeName and Aspect Naming Convention

`TypeName` identifies a type with four fields:

| Field | Type | Purpose |
|-------|------|---------|
| `name` | `String` | Simple type name (e.g., `"Natural"`) |
| `namePackage` | `Seq[String]` | Package path (e.g., `Seq("domains", "natural")`) |
| `aspects` | `Seq[String]` | Role classification: subset of `("domain", "rule", "actor")` |
| `typeParameters` | `Seq[String]` | Type parameters (e.g., `Seq("T")` for `Primal[T]`); formals on declaring type, actuals on references |

Derived fields:
- `qualifiedName` — `"NaturalDomainRule"` (name + capitalized aspects)
- `namePath` — `"domains.natural.NaturalDomainRule"` (fully qualified)
- `resourcePath` — `"/domains/natural/Natural.domain.rule.json"` (JSON file path)

Canonical ordering is always Domain > Rule > Actor, enforced by `TypeName.canonicalOrder`. This replaces the earlier approach of subpackage segregation (`rules/`, `actors/` subdirectories).

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

```scala
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

`Generator.scala` has five `generate` overloads:

1. `generate(td: TypeDefinition)` — trait + TypeInstance companion
2. `generate(td: TypeDefinition, dd: DomainDefinition)` — trait + DomainInstance companion
3. `generate(td: TypeDefinition, ad: ActorDefinition)` — trait + ActorInstance companion with Pekko imports
4. `generate(tds: Seq[TypeDefinition])` — multi-type in one file, topologically sorted
5. `generate(rd: RuleDefinition)` — rule trait + RuleInstance companion with Evrete integration

Key internal methods:
- `typeDefinitionLoad(td)` — emits `draco.TypeDefinition.load(TypeName(...))` (fully qualified for portability across packages)
- `parameterizedName(tn)` / `wildcardTypeName(tn)` — TypeName-aware helpers using `typeParameters` field
- `factoryBody(factory)` — uses `Factory.body` when non-empty, parameter-derived overrides when empty; appends `override lazy val typeInstance/typeDefinition`
- `nullInstance(typeName, elements, factory)` — uses `wildcardTypeName` for parameterized types (e.g., `Null: Actor[_]`); `apply[Nothing]()` for type params; `apply()` for simple factories; direct element overrides for computed factories
- `nullValueFor(valueType, defaultValue)` — type-appropriate defaults ("" for String, Seq.empty, 0, false, etc.)
- `typeExtends(derivation)` — empty derivation = no extends clause (DracoType is the root); non-empty = `extends A with B`
- `externalTypeImports` — lookup table mapping external type names (URI, BufferedSource, KnowledgeService, Consumer, etc.) to import statements
- `externalImports(td)` — scans valueTypes across elements/factory/globalElements and returns matching external imports
- `packageHierarchyImports(namePackage)` — shared: `draco._` + parent package chain
- `typeImports(td, hasCodec, instanceType)` — combines package hierarchy + circe + Pekko + external imports
- `ruleImports(namePackage)` — package hierarchy + Evrete/Circe framework imports
- `domainGlobal(td, dd)` / `domainInstanceLiteral(objName, dd)` — DomainInstance companion generation
- `conditionFunctions` / `whereConditions` — Evrete condition compilation (fully qualified class names required)
- Codec generation: `simpleCodecDeclaration` (only when factory params ⊆ element names), `discriminatedCodecDeclaration`, `subtypeCodecDeclaration`

**Important:** PrimesRulesTest "Generate" tests overwrite rule source files with Generator output. Generated code must include imports and use `private lazy val` for action/pattern/ruleDefinition.

### JSON as Single Source of Truth

JSON definition files are the canonical representation for types, domains, rules, and actors:

1. **JSON file is the source of truth** — every definition lives in a `.json` file using the aspect naming convention (`Natural.json`, `Natural.domain.json`, `Natural.actor.json`, etc.)
2. **Generated Scala loads from classpath** — `TypeDefinition.load(typeName)` uses `getResourceAsStream(typeName.resourcePath)` to load JSON at runtime
3. **Generator emits load calls** — `draco.TypeDefinition.load(TypeName("TypeName", _namePackage = Seq("draco")))` (fully qualified) instead of inline JSON
4. **Dreams edits JSON** — to create or modify a type, Dreams writes the `.json` file and invokes the Generator to regenerate Scala source. Dreams will use `SourceContent`/`ContentSink` for file I/O (not classpath loading).

Rule generation (`ruleDefinitionFromJson`) still embeds inline JSON; migration to `RuleDefinition.load` is planned.

### Domains

Domains are peers (Draco, Base, Primes) in the `DomainDictionary`, not hierarchical.

**Base domain** (`draco.base`): Cardinal[T], Distance[T], Meters, Rotation[T], Radians, Ordinal, Nominal, Coordinate[T <: Product]. Cardinal is unconstrained (no Numeric bound on T). Coordinate is compositionally self-describing (no named Cartesian/Polar/Spherical types).

**Primes domain** (`draco.primes`): Working example of the rule engine. Accumulator (mutable state), Numbers (input sequences), three rules defined in JSON and generated into `draco.primes.rules`.

**Transform domains** (in test): Examples: Alpha, Bravo, Charlie, Delta extend DataModel.

### Actors

`Actor[T]` extends `ExtensibleBehavior[T] with ActorType`. The companion `Actor.apply[T]` creates a default no-op actor from an `ActorDefinition`. Custom actors override `receive` and `receiveSignal` in the `actorInstance` definition.

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
