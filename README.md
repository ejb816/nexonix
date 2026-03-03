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
  |
  +-- DomainType                 -- a named domain with a type dictionary
  |     +-- Domain[T]            -- generic domain container
  |
  +-- RuleType                   -- a rule with pattern + action
        +-- Rule[T]              -- generic rule container
```

Every companion object in the framework extends `TypeInstance`, providing two things:

- `typeDefinition: TypeDefinition` — the structural description of the type (its fields, parent types, factory method, etc.)
- `typeInstance: Type[T]` — the canonical runtime representative of the type

This pair makes every type in the system self-describing and introspectable at runtime.

### TypeDefinition

A `TypeDefinition` is the schema for a type. It contains:

| Field | Type | Purpose |
|-------|------|---------|
| `typeName` | `TypeName` | Qualified name with package path |
| `modules` | `Seq[TypeName]` | Subtypes (makes the trait `sealed`) |
| `derivation` | `Seq[TypeName]` | Parent traits (`extends ... with ...`) |
| `elements` | `Seq[TypeElement]` | Trait body members |
| `factory` | `Factory` | Companion `apply()` specification |
| `globalElements` | `Seq[BodyElement]` | Companion object members |

`TypeDefinition` is serializable to and from JSON via Circe, with empty fields elided. This means a type can be defined entirely in a JSON file, loaded at runtime, and used to generate Scala source code.

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

- A `DomainName` listing the short names of its member types
- A `TypeDictionary` mapping `TypeName` to `TypeDefinition` for each member
- A `Domain[T]` instance that ties these together

Domains are peers in the system — the framework domain (`Draco`), the base measurement domain (`Base`), and application domains like `Primes` all sit at the same level in the `DomainDictionary`.

The companion object of a domain extends `DomainInstance`:

```scala
object Primes extends App with DomainInstance {
  lazy val typeDefinition: TypeDefinition = ...
  lazy val typeInstance: Type[Primes] = Type[Primes](typeDefinition)

  lazy val domainInstance: Domain[Primes] = Domain[Primes](
    _domainName = DomainName(
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq("Numbers", "PrimesRuleData")
    )
  )
}
```

### Rules

Rules are defined as JSON and generated into Scala. A `RuleDefinition` specifies:

- `variables` — fact variables bound in the RETE network (`$accumulator`, `$i1`, etc.)
- `conditions` — Boolean predicates evaluated by Evrete's runtime compiler
- `action` — the body executed when the rule fires

The `Generator` transforms a `RuleDefinition` into a Scala source file containing:

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

Draco integrates Apache Pekko for actor-based concurrency:

```
ExtensibleBehavior[T]      (Pekko)
  +-- ActorBehavior[T]     -- default no-op receive/receiveSignal
        +-- RuleActorBehavior[T]  -- adds an Evrete Knowledge base
              +-- Service[T]
                    +-- ServiceDomain[T]  -- ties a Domain to a Service
```

A `ServiceDomain` binds a domain's type system to an Evrete knowledge base inside a Pekko actor. This is the intended deployment model: domains as actors, rules as the message-processing logic, types as the vocabulary.

### Code Generation

The `Generator` reads JSON definitions and produces Scala source code. It handles three generation modes:

1. **TypeInstance generation** — From a `TypeDefinition`, generates a trait and companion object with `typeDefinition`, `typeInstance`, optional `apply()`, `Null`, `Default`, and global elements.
2. **DomainInstance generation** — Same as above, but the companion extends `DomainInstance` and includes a `domainInstance: Domain[T]` declaration with its member type list.
3. **RuleInstance generation** — From a `RuleDefinition`, generates a rule trait and companion with embedded JSON, condition functions, pattern/action lambdas, auto-generated imports, and Evrete integration.

Generated code embeds its own `TypeDefinition` or `RuleDefinition` as an inline JSON string parsed at startup via Circe. This keeps the generated code self-contained and round-trippable.

The content pipeline (`SourceContent` / `ContentSink` / `Main`) reads JSON from `src/main/resources/` and writes generated Scala to `src/main/scala/`.

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
- Code generation from `TypeDefinition` JSON (single type, multi-type with topological sort, domain instances)
- Code generation from `RuleDefinition` JSON with condition functions, pattern/action lambdas, and auto-generated imports
- Factory method generation with support for both parameter-derived and custom override bodies
- Null/Default instance generation using factory `apply()` with type-appropriate defaults
- Evrete RETE rule engine integration with runtime-compiled conditions
- Rule execution with `StatefulSession` working memory (prime sieve)
- Pekko actor integration (`ActorBehavior`)
- Content pipeline for reading JSON resources and writing generated Scala source
- Base domain type hierarchy (Cardinal, Distance, Meters, Rotation, Radians, Coordinate, etc.)
- Transform domain modeling with source/sink domain pairs (Alpha, Bravo, Charlie, Delta examples)

## Work in Progress

- **Codec generation** — Circe encoders/decoders are currently hand-written. The Generator should produce them: field-based codecs for simple types, kind-discriminated dispatch for sealed hierarchies, `Codec.sub` wiring for subtypes.
- **Rules subdomain auto-generation** — `rules` is a reserved subpackage name. A `rules.Rules` companion that extends `DomainInstance` should be auto-generated as a container for all rule types in that subpackage.
- **Type parameters in TypeName** — Type parameters are currently encoded in the name string (e.g., `"Cardinal[T]"`). A proper `typeParameters` field is planned.
- **ServiceDomain deployment** — The actor + rule + domain integration is structurally complete but not yet exercised end-to-end as a running service.
- **Transform domain rules** — Transform domains model source-to-sink mappings but do not yet generate transformation rules automatically.

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
| [ScalaTest](https://www.scalatest.org/) | Testing framework |

## Project Structure

```
src/
  main/
    resources/
      draco/                          -- JSON type and rule definitions
        primes/rules/                 -- Prime sieve rule definitions
        base/                         -- Base domain type definitions
    scala/
      draco/                          -- Core framework types
        base/                         -- Base domain (Unit, Cardinal, Coordinate, ...)
        primes/                       -- Primes domain (Accumulator, Numbers)
          rules/                      -- Generated rule source
        dreams/                       -- Transform utilities
  test/
    scala/
      draco/                          -- Framework tests
        transform/                    -- Transform domain examples (Alpha, Bravo, ...)
      org/nexonix/                    -- Additional rule tests
```
