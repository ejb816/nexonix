# Draco

Draco is a self-describing, domain-driven framework for building data transformation services that preserve semantic meaning within and across domains. It combines a reflective type system, a RETE-based rule engine, and an actor model into a single coherent architecture where every concept — from primitive measurements to complex business rules — is represented as a first-class type that knows its own structure.

## Core Idea

Most software systems separate their type definitions from their runtime representations. A Java class has fields, but you need reflection or an external schema to reason about them programmatically. Draco eliminates this gap: every type in the system carries a `TypeDefinition` that describes its own structure — its fields, its parent types, how to construct it, and how to serialize it. This self-description is not bolted on after the fact; it is the foundation everything else is built on.

This means that a `Meters` value doesn't just hold a `Double` — it knows it is a `Distance`, which is a `Cardinal`, which is a `Unit` in the `Base` domain. A rule engine can pattern-match against these types. A code generator can produce Scala source from their JSON definitions. A domain dictionary can enumerate every type in a domain by name. All of this works because the type system is closed over itself: `TypeDefinition` is itself described by a `TypeDefinition`.

## Architecture

### The Type System

Everything in Draco is a `DracoType`:

```
DracoType                        -- the universal root; carries typeDefinition
  |
  +-- Primal[T]                  -- a DracoType that wraps a single value: T
  |
  +-- TypeInstance               -- companion objects that register themselves
  |     +-- DomainInstance       -- companions that own a domain
  |     +-- RuleInstance         -- companions that own a rule
  |     +-- ActorInstance        -- companions that own an actor
  |
  +-- DomainType                 -- a named domain with a type dictionary
  |     +-- Domain[T]            -- generic domain container
  |
  +-- RuleType                   -- a rule with pattern + action
  |     +-- Rule[T]              -- generic rule container
  |
  +-- ActorType                  -- an actor with an actor definition
        +-- Actor[T]             -- generic actor container (extends Pekko ExtensibleBehavior[T])
```

Every companion object in the framework extends `TypeInstance`, providing two things:

- `typeDefinition: TypeDefinition` — the structural description of the type (its fields, parent types, factory method, etc.)
- `typeInstance: Type[T]` — the canonical runtime representative of the type

This pair makes every type in the system self-describing and introspectable at runtime.

### The Instance Triad: DomainInstance, RuleInstance, ActorInstance

Three parallel traits extend `TypeInstance` for companions that own domain, rule, or actor definitions:

| Trait | Owns |
|-------|------|
| `DomainInstance` | `domainInstance: DomainType` |
| `RuleInstance` | `ruleInstance: RuleType` |
| `ActorInstance` | `actorInstance: ActorType` |

All three use `TypeDefinition` as their unified definition type — `DomainType.domainDefinition`, `RuleType.ruleDefinition`, and `ActorType.actorDefinition` are all `TypeDefinition`. Domain, rule, and actor roles are distinguished by which fields are populated, not by separate definition types.

These three are deliberately kept structurally symmetric — none is parameterized. When Pekko's typed API requires `Behavior[T]` (which `Actor[T]` implements), the adapter uses `asInstanceOf` at the integration boundary rather than breaking the triadic symmetry by parameterizing `ActorInstance` alone.

### TypeDefinition

A `TypeDefinition` is the schema for a type. It contains:

| Field | Type | Purpose |
|-------|------|---------|
| `typeName` | `TypeName` | Qualified name with package path |
| `superDomain` | `TypeName` | Parent domain (for domain inheritance) |
| `modules` | `Seq[TypeName]` | Subtypes (makes the trait `sealed`) |
| `derivation` | `Seq[TypeName]` | Parent traits (`extends ... with ...`) |
| `elements` | `Seq[TypeElement]` | Trait body members |
| `factory` | `Factory` | Companion `apply()` specification |
| `globalElements` | `Seq[BodyElement]` | Companion object members |
| `elementTypeNames` | `Seq[String]` | Domain member type names |
| `source` / `target` | `TypeName` | Domain transform source/target |
| `variables` | `Seq[Variable]` | Rule fact variables |
| `conditions` | `Seq[Condition]` | Rule LHS conditions |
| `values` | `Seq[Value]` | Rule value extractors |
| `pattern` / `action` | `Pattern` / `Action` | Rule LHS/RHS |
| `messageAction` / `signalAction` | `Action` | Actor receive/signal handlers |

All fields beyond `typeName` default to empty/Null, so a simple type needs only `typeName`. Domain, rule, and actor roles are expressed by populating the relevant fields — there are no separate definition types.

`TypeDefinition` is serializable to and from JSON via Circe, with empty fields elided. This means a type can be defined entirely in a JSON file, loaded at runtime, and used to generate Scala source code.

### TypeName

`TypeName` identifies a type with three fields:

| Field | Type | Purpose |
|-------|------|---------|
| `name` | `String` | Simple type name (e.g., `"Natural"`) |
| `namePackage` | `Seq[String]` | Package path (e.g., `Seq("domains", "natural")`) |
| `typeParameters` | `Seq[String]` | Type parameters (e.g., `Seq("T")` for `Primal[T]`) |

Derived fields:
- `namePath` — fully qualified: `"domains.natural.Natural"`
- `resourcePath` — JSON file path: `"/domains/natural/Natural.json"`

Domain-ness is structural (name matches last package element). Rule and actor roles are expressed through derivation in `TypeDefinition`, not through `TypeName`.

### TypeElement Hierarchy

The members of a type are described by `TypeElement`, a sealed hierarchy that extends `Primal[String]` (where the `value` is the expression body):

```
TypeElement                      -- name, valueType, parameters, body
  +-- BodyElement                -- used in factory bodies and companion objects
        +-- Fixed                -- immutable field:  val name: Type = value
        +-- Mutable              -- mutable field:    var name: Type = value
        +-- Dynamic              -- method:           def name(params): Type = body
        +-- Parameter            -- constructor/method parameter with optional default
        +-- Monadic              -- side-effecting statement (no name, Unit type)
        +-- Condition            -- Boolean predicate with parameters
        +-- Action               -- rule RHS: variables + values + body statements
        +-- Pattern              -- rule LHS: variables + conditions
        +-- Variable             -- Evrete fact variable binding
        +-- Factory              -- apply() specification: return type + parameters + body
```

JSON serialization uses a `"kind"` discriminator field. A `Fixed` element looks like:

```json
{
  "kind": "Fixed",
  "name": "primeSet",
  "valueType": "mutable.Set[Int]"
}
```

### Domains

A domain is a named collection of types. Every domain has:

- A `TypeDefinition` with `elementTypeNames` populated, describing the domain's member types, optional superdomain, and source/target for transforms
- A `TypeDictionary` mapping `TypeName` to `TypeDefinition` for each member
- A `Domain[T]` instance that ties these together

Domains are peers in the system — the framework domain (`Draco`), the base measurement domain (`Base`), and application domains like `Primes` all sit at the same level in the `DomainDictionary`.

The companion object of a domain extends `DomainInstance`:

```scala
object Primes extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = ...
  lazy val typeInstance: Type[Primes] = Type[Primes](typeDefinition)

  lazy val domainInstance: Domain[Primes] = Domain[Primes](
    _domainDefinition = TypeDefinition(
      typeDefinition.typeName,
      _elementTypeNames = Seq("Numbers", "PrimesRuleData")
    )
  )
}
```

### Rules

Rules are defined as JSON and generated into Scala. A rule `TypeDefinition` populates:

- `variables` — fact variables bound in the RETE network (`$accumulator`, `$i1`, etc.)
- `conditions` — Boolean predicates evaluated by Evrete's runtime compiler
- `action` — the body executed when the rule fires

The `Generator` detects a rule (via `variables.nonEmpty`) and produces a Scala source file containing:

- A trait extending `RuleInstance`
- A companion object with the rule's `pattern` (registers the rule with an Evrete `Knowledge` base) and `action` (the RHS lambda)
- Condition functions as companion object methods, referenced by fully qualified name so Evrete's Java-based runtime compiler can find them

Example JSON rule (prime sieve — removes composite numbers from working memory):

```json
{
  "typeName": {
    "name": "PrimesFromNaturalSequence",
    "namePackage": ["draco", "primes", "rules"]
  },
  "variables": [
    { "kind": "Variable", "name": "accumulator", "valueType": "draco.primes.Accumulator" },
    { "kind": "Variable", "name": "i1", "valueType": "Integer" },
    { "kind": "Variable", "name": "i2", "valueType": "Integer" },
    { "kind": "Variable", "name": "i3", "valueType": "Integer" }
  ],
  "conditions": [
    {
      "kind": "Condition",
      "value": "i1 * i2 == i3",
      "parameters": [
        { "kind": "Parameter", "name": "i1", "valueType": "Integer" },
        { "kind": "Parameter", "name": "i2", "valueType": "Integer" },
        { "kind": "Parameter", "name": "i3", "valueType": "Integer" }
      ]
    }
  ],
  "action": {
    "kind": "Action",
    "body": [
      { "kind": "Monadic", "value": "ctx.delete(i3)" },
      { "kind": "Monadic", "value": "accumulator.compositeSet.addOne(i3)" }
    ]
  }
}
```

Rules are registered with an Evrete `Knowledge` base and fired against a `StatefulSession` containing working memory facts.

### Actors

Draco integrates Apache Pekko for actor-based concurrency. The actor model follows the same self-describing pattern as domains and rules:

```
ExtensibleBehavior[T]      (Pekko)
  +-- Actor[T]             -- generic actor container; extends ActorType
```

An `ActorInstance` companion owns an `Actor[T]` that defines `receive` and `receiveSignal` behaviors. The actor's metadata is carried by a `TypeDefinition` (the same unified type used for domains and rules).

```scala
object NaturalActor extends App with ActorInstance {
  lazy val actorInstance: ActorType = new Actor[Natural] {
    override val actorDefinition: TypeDefinition = ...
    override def receive(ctx: TypedActorContext[Natural], msg: Natural): Behavior[Natural] = {
      println(s"msg.value = ${msg.value}")
      Behaviors.same[Natural]
    }
    override def receiveSignal(ctx: TypedActorContext[Natural], msg: Signal): Behavior[Natural] = {
      Behaviors.same[Natural]
    }
  }
}
```

To use with Pekko's typed API, cast at the integration boundary:

```scala
val system = ActorSystem[Natural](
  NaturalActor.actorInstance.asInstanceOf[Actor[Natural]],
  "naturalActor"
)
system ! Natural(10)
```

### Code Generation

The `Generator` reads JSON definitions and produces Scala source code. A single `generate(td: TypeDefinition)` method detects the type's role and dispatches accordingly:

1. **Rule** (`variables.nonEmpty`) — Rule imports + RuleInstance companion with condition functions, pattern/action lambdas, Evrete integration.
2. **Domain** (`elementTypeNames.nonEmpty`) — Domain imports + DomainInstance companion with member type list.
3. **Actor** (derivation contains ActorType/ExtensibleBehavior) — Pekko imports + TypeInstance companion.
4. **Otherwise** — Plain TypeInstance companion with `typeDefinition`, `typeInstance`, optional `apply()`, `Null`, and global elements.
5. **Multi-type** — `generate(tds: Seq[TypeDefinition])` generates multiple types in one file with topological sort by module dependency.

**JSON as single source of truth:** JSON definition files are the canonical representation of types. Every manually-written draco framework type now has a corresponding JSON definition file. Generated Scala source loads definitions from these JSON files at runtime via classpath loading (`TypeDefinition.load`). The Generator produces code that calls `draco.TypeDefinition.load(TypeName(...))` (fully qualified), which reads the JSON from the classpath using `getResourceAsStream`. The fully qualified reference ensures generated code works in any package, including `generated.draco` for testing.

The content pipeline (`SourceContent` / `ContentSink` / `Main` / `Test`) reads JSON from `src/main/resources/` and writes generated Scala to `src/main/scala/`. For Dreams (deployed as a jar), `SourceContent`/`ContentSink` will handle file-based I/O to user-defined domain directories.

### The Base Domain

The `Base` domain provides primitive measurement types:

```
Unit (name, description)
  +-- Cardinal[T] (Primal[T])      -- quantities with magnitude
  |     +-- Distance[T]
  |     |     +-- Meters            -- T = Double
  |     +-- Rotation[T]
  |           +-- Radians           -- T = Double
  +-- Ordinal (Primal[Enumeration]) -- ordered categories
  +-- Nominal (Primal[String])      -- unordered labels

Coordinate[T <: Product] (Primal[T]) -- spatial positions, independent of Unit
```

`Cardinal[T]` is deliberately unconstrained at the type level — `Numeric` bounds are applied at the point of use, not on the type itself. `Coordinate` is structurally self-describing: there are no named `Cartesian`, `Polar`, or `Spherical` types. A coordinate's dimensionality and system are expressed through its type parameter (e.g., a 3-tuple of `Meters` and two `Radians` is implicitly spherical).

### Dreams (Domain Rules Editor Actor Message Service)

Dreams is the planned built-in service application for Draco. It provides an editor interface for creating and modifying Domain, Rule, and Actor types. Dreams operates on JSON definition files as the single source of truth, invoking the Generator to produce Scala source as a derived artifact.

## Semantic Preservation in Data Transformation

The combination of self-describing types, domain-scoped dictionaries, and rule-based transformation enables a specific architectural goal: data transformations that preserve semantic meaning.

When data moves between domains (e.g., from a sensor domain to an analysis domain), the type system ensures that:

1. **Types carry their own meaning.** A `Meters` value is not just a `Double`; it carries its lineage through `Distance`, `Cardinal`, and `Unit`. Any transformation that consumes or produces `Meters` can verify semantic compatibility at the type level.

2. **Domains define vocabularies.** Each domain's `TypeDictionary` is a closed vocabulary. A transform between domains can map source types to sink types explicitly, and the dictionaries make unmapped types visible.

3. **Rules preserve invariants.** Domain-specific rules can enforce constraints that pure type checking cannot. The prime sieve demonstrates this: the rule `i1 * i2 == i3` is a semantic constraint that operates on the values, not just the types.

4. **Transforms are first-class.** The `TransformDomain` trait models a transformation as a domain in its own right, with explicit `sourceDomain` and `sinkDomain`. Transformations can be composed, inspected, and governed by the same type system that governs the data.

## Working Features (Verified by Tests)

- Self-describing type system with `TypeDefinition`, `TypeName`, and the full `TypeElement` hierarchy
- JSON serialization/deserialization with field elision for all core types
- `TypeDictionary` and `DomainDictionary` for domain-scoped type registries
- Unified code generation from `TypeDefinition` JSON with automatic role detection (domain, rule, actor, plain type)
- Multi-type generation with topological sort by module dependency
- Codec generation: field-based codecs, kind-discriminated dispatch for sealed hierarchies, `Codec.sub` wiring for subtypes
- Factory method generation with support for both parameter-derived and custom override bodies; type parameter support on `apply[T]`
- Null instance generation: `apply()` for simple factories, direct element overrides for computed factories
- Classpath-based JSON loading via `TypeDefinition.load(typeName)` using `getResourceAsStream`
- Generator emits `TypeDefinition.load(TypeName(...))` instead of inline JSON embedding
- Import generation: `packageHierarchyImports` + `typeImports` for type/domain/actor generation; `ruleImports` for rules
- `TypeName.typeParameters` field for proper generic type support (replaces embedding `[T]` in name string)
- Evrete RETE rule engine integration with runtime-compiled conditions
- Rule execution with `StatefulSession` working memory (prime sieve)
- Pekko actor integration with `Actor[T]`, `ActorInstance`, `ActorType`
- NaturalActor working end-to-end test: JSON loading, actor creation, message sending
- TypeDefinition unification — domain, rule, and actor definitions dissolved into TypeDefinition; Generator detects role from content
- Content pipeline for reading JSON resources and writing generated Scala source
- Generate test pattern: load JSON, generate source, write to `generated.draco` package, programmatic compilation check
- Complete JSON definition files for all draco framework types (30+ types including TypeElement hierarchy)
- External type import detection in Generator (URI, BufferedSource, KnowledgeService, Consumer, etc.)
- Parameterized Null instances: `Null: Actor[_] = apply[Nothing]()` for generic types
- Base domain type hierarchy (Cardinal, Distance, Meters, Rotation, Radians, Coordinate, etc.)
- Transform domain modeling with source/sink domain pairs (Alpha, Bravo, Charlie, Delta examples)

## Work in Progress

- **Update existing domains** — Migrate Draco, Base, Primes to new Extensible/simplified conventions.
- **Generator domain** — `draco.generator.Generator[L]` as a self-describing domain parameterized by target language, with capability domains (`draco.rete`, `draco.json`, `draco.actor`, `draco.scala`).
- **Dreams application** — Domain Rules Editor Actor Message Service; built-in editor for creating and modifying types, domains, rules, and actors via their JSON definitions.
- **Orion** — Open Resilient Inter-Operability Nexus; five ION interaction patterns for cross-domain system-of-systems integration.

## Building and Running

This is a Scala 2.13 project using sbt.

```bash
sbt compile       # Compile
sbt test          # Run all tests
sbt ~compile      # Continuous compilation

# Run specific tests
sbt "testOnly draco.TypeDefinitionTest"
sbt "testOnly draco.primes.PrimesRulesTest"
sbt "testOnly draco.primes.PrimesRulesTest -- -z \"PrimesFromNaturalSequence\""
```

## Dependencies

| Library | Purpose |
|---------|---------|
| [Evrete](https://www.evrete.org/) | RETE-based rule engine |
| [Apache Pekko](https://pekko.apache.org/) | Typed actor system |
| [Circe](https://circe.github.io/circe/) | JSON serialization |
| [Logback](https://logback.qos.ch/) | SLF4J logging implementation |
| [ScalaTest](https://www.scalatest.org/) | Testing framework |

## Project Structure

```
src/
  main/
    resources/
      draco/                          -- JSON type and rule definitions
        primes/rules/                 -- Prime sieve rule definitions
        base/                         -- Base domain type definitions
      logback.xml                     -- Runtime logging config (Pekko suppressed)
    scala/
      draco/                          -- Core framework types
        base/                         -- Base domain (Unit, Cardinal, Coordinate, ...)
        primes/                       -- Primes domain (Accumulator, Numbers)
          rules/                      -- Generated rule source
        dreams/                       -- Dreams application
  test/
    resources/
      domains/                        -- Test domain JSON definitions
      logback-test.xml                -- Test logging config
    scala/
      draco/                          -- Framework tests
      draco/                          -- Framework tests and generation tests
      domains/                        -- Test domain examples (Natural, Alpha, Bravo, ...)
      org/nexonix/                    -- Additional rule and format tests
```
