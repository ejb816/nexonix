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
  +-- DomainType                       -- named domain with type dictionary
  |     +-- Domain[T]                  -- generic domain container
  +-- RuleType                         -- rule with pattern + action
        +-- Rule[T]                    -- generic rule container
```

### Key Files and Their Roles

| File | Role |
|------|------|
| `DracoType.scala` | Universal root trait. The one axiom: `DracoType.typeInstance` is a plain `val` |
| `Primal.scala` | `Primal[T]` — value-carrying base trait |
| `Type.scala` | `Type[T]` — generic type wrapper created via `Type[X](typeDefinition)` |
| `TypeInstance.scala` | Trait for self-registering companion objects |
| `TypeName.scala` | Qualified name with package path and resource path derivation |
| `TypeDefinition.scala` | Schema descriptor: typeName, modules, derivation, elements, factory, globalElements |
| `TypeElement.scala` | Sealed element hierarchy (Fixed, Mutable, Dynamic, Parameter, Monadic, Pattern, Action, Condition, Variable, Factory) |
| `Codec.scala` | `Codec[T]` encoder/decoder pair; `Codec.sub` for subtype codec derivation |
| `Domain.scala` | `Domain[T]` — generic domain container created from `DomainName` |
| `DomainType.scala` | Trait: domainName + typeDictionary |
| `DomainName.scala` | Domain identity with elementTypeNames list |
| `DomainInstance.scala` | Trait for domain-owning companions |
| `TypeDictionary.scala` | Map of TypeName to TypeDefinition within a domain |
| `DomainDictionary.scala` | Cross-domain registry (Map of DomainType to TypeDictionary) |
| `Dictionary.scala` | Generic Map[K,V] base abstraction |
| `Rule.scala` | `Rule[T]` container; owns the singleton `KnowledgeService` |
| `RuleType.scala` | Trait: ruleDefinition + pattern (Consumer[Knowledge]) + action (Consumer[RhsContext]) |
| `RuleDefinition.scala` | JSON-serializable rule specification (variables, conditions, values, pattern, action) |
| `RuleInstance.scala` | Trait for rule-owning companions |
| `Generator.scala` | Code generation from TypeDefinition and RuleDefinition JSON |
| `SourceContent.scala` | Reads source files from a URI root |
| `ContentSink.scala` | Writes generated content to output paths |
| `Main.scala` | Default source root (resources) and sink root (scala) URIs |
| `Value.scala` | JSON path extractor: navigates pathElements to extract typed values |
| `ActorBehavior.scala` | Default no-op Pekko ExtensibleBehavior |
| `RuleActorBehavior.scala` | Adds Evrete Knowledge base to ActorBehavior |
| `Service.scala` | `Service[T]` extends `RuleActorBehavior[T]` |
| `ServiceDomain.scala` | Ties a DomainType to a Service |
| `Draco.scala` | Root domain registering all framework types |

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
object MyType extends App with TypeInstance {           // or DomainInstance, RuleInstance
  lazy val typeDefinition: TypeDefinition = ...         // structural description
  lazy val typeInstance: Type[MyType] = Type[MyType](typeDefinition)
  // optional: def apply(...), lazy val Null, lazy val Default, domainInstance, ruleInstance
}
```

### DelayedInit / lazy val Rules

`App` uses `DelayedInit` in Scala 2 — ALL `val` initializers are delayed until `main()`. This causes null when accessed cross-object. The rule: **every val in an `extends App` companion must be `lazy val`** unless it is purely local to a non-lazy context.

- `typeDefinition`, `typeInstance` → `lazy val`
- `domainInstance`, `ruleInstance`, `knowledgeService` → `lazy val`
- `Null`, `Default` → `lazy val`
- Encoders/decoders → `implicit lazy val`
- Private vals referenced by lazy vals (action, pattern, ruleDefinition) → `private lazy val`
- Exception: `DracoType.typeInstance` is `val` (axiom, doesn't extend App)

### Generator

`Generator.scala` has four `generate` overloads:

1. `generate(td: TypeDefinition)` — trait + TypeInstance companion
2. `generate(td: TypeDefinition, dn: DomainName)` — trait + DomainInstance companion
3. `generate(tds: Seq[TypeDefinition])` — multi-type in one file, topologically sorted
4. `generate(rd: RuleDefinition)` — rule trait + RuleInstance companion with Evrete integration

Key internal methods:
- `factoryBody(factory)` — uses `Factory.body` when non-empty, parameter-derived overrides when empty
- `nullInstance(typeName, elements, factory)` — apply-based Null when factory present, null-cast fallback
- `nullValueFor(valueType, defaultValue)` — type-appropriate defaults ("" for String, Seq.empty, 0, false, etc.)
- `ruleImports(namePackage)` — auto-generates package hierarchy imports + draco._ + framework imports
- `domainGlobal(td, dn)` / `domainInstanceLiteral(objName, dn)` — DomainInstance companion generation
- `conditionFunctions` / `whereConditions` — Evrete condition compilation (fully qualified class names required)

Generated code embeds TypeDefinition/RuleDefinition as inline JSON parsed via `parser.parse("""...""").flatMap(_.as[T])`.

**Important:** PrimesRulesTest "Generate" tests overwrite rule source files with Generator output. Generated code must include imports and use `private lazy val` for action/pattern/ruleDefinition.

### Domains

Domains are peers (Draco, Base, Primes) in the `DomainDictionary`, not hierarchical.

**Base domain** (`draco.base`): Cardinal[T], Distance[T], Meters, Rotation[T], Radians, Ordinal, Nominal, Coordinate[T <: Product]. Cardinal is unconstrained (no Numeric bound on T). Coordinate is compositionally self-describing (no named Cartesian/Polar/Spherical types).

**Primes domain** (`draco.primes`): Working example of the rule engine. Accumulator (mutable state), Numbers (input sequences), three rules defined in JSON and generated into `draco.primes.rules`.

**Transform domains** (`draco.transform` in test): TransformDomain[SO, SI] with sourceDomain and sinkDomain. Examples: Alpha, Bravo, Charlie, Delta extend DataModel.

### JSON Serialization

All domain types use Circe with field elision (empty fields omitted on encode, defaulted on decode). The `"kind"` discriminator field in TypeElement enables polymorphic dispatch. Sub-type codecs use `Codec.sub` to narrow the parent encoder/decoder.

## Key Dependencies

- **Evrete** (`org.evrete`) — RETE-based rule engine; condition functions require fully qualified class names for runtime Java compilation
- **Apache Pekko** — Typed actor system
- **Circe** — JSON parsing and encoding
- **ScalaTest** — Testing framework
