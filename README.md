# Draco

Draco is a self-describing, domain-driven framework for building data transformation services that preserve semantic meaning within and across domains. It combines a reflective type system, a RETE-based rule engine, and an actor model into a single coherent architecture where every concept — from primitive measurements to complex business rules — is represented as a first-class type that knows its own structure.

## Core Idea

Most software systems separate their type definitions from their runtime representations. A Java class has fields, but you need reflection or an external schema to reason about them programmatically. Draco eliminates this gap: every type in the system carries a `TypeDefinition` that describes its own structure — its fields, its parent types, how to construct it, and how to serialize it. This self-description is not bolted on after the fact; it is the foundation everything else is built on.

This means that a `Meters` value doesn't just hold a `Double` — it knows it is a `Distance`, which is a `Cardinal`, which is a `Unit` in the `Base` domain. A rule engine can pattern-match against these types. A code generator can produce Scala source from their JSON definitions. A domain dictionary can enumerate every type in a domain by name. All of this works because the type system is closed over itself: `TypeDefinition` is itself described by a `TypeDefinition`.

## Architecture

### The Type System

Everything in Draco is a `DracoType` — a trait whose single obligation is to carry its own `typeDefinition`:

```text
DracoType                        -- universal root; carries `typeDefinition: TypeDefinition`
  |
  +-- Primal[T]                  -- wraps a single value: `value: T`
  +-- Holon[T <: Product]        -- a *perspective* onto composite structure
  |
  +-- Type[T]                    -- the canonical runtime representative of a type
  |
  +-- Aspects                    -- carrier of the four aspect blocks
  |     +-- TypeDefinition       -- Aspects + `typeName`; the full schema of a type
  |
  +-- DomainType                 -- a named domain with a type dictionary
  |     +-- Domain[T]            -- generic domain container
  |
  +-- RuleType                   -- a rule with pattern + action
  |     +-- Rule[T]              -- generic rule container
  |
  +-- ActorType                  -- an actor with an actor definition
  |     +-- Actor[T]             -- generic actor container (extends Pekko `ExtensibleBehavior[T]`)
  |
  +-- TypeTransform[S, T]        -- a type-to-type transform     (extends Holon[(S, T)])
  +-- DomainTransform[S, T]      -- a domain-to-domain transform (extends Holon[(S, T)])
```

`Primal` and `Holon` sit on different axes. `Primal[T]` is about *value* — it carries a single `value` of type `T`, which may be a primitive or a composite tuple. `Holon[T <: Product]` is about *perspective* — it marks a point of view onto composite structure. A type may be a value carrier, a perspective, both, or neither.

The whole `draco.*` package is itself a domain — the meta-domain whose members are the type system. `TypeDefinition` is described by a `TypeDefinition`; the system is closed over itself.

### The Companion Val Convention

Draco has no separate "instance" trait family — the former `TypeInstance` / `DomainInstance` / `RuleInstance` / `ActorInstance` traits have been dissolved. Every companion object in `draco.*` now follows a single structural convention, emitted verbatim by the Generator:

- `typeDefinition: TypeDefinition` — the type's structural description, loaded from its JSON resource via `Generator.loadType` (or `Generator.loadRuleType` for rules).
- `dracoType: Type[T]` — the canonical runtime representative of the type. Always present.
- `domainType: Domain[D]` — the domain the type belongs to. Always present. For a domain's own companion it points at `Domain[Self]`; for a member type it points at the `Domain[D]` that contains it.

A leaf type's companion is therefore just:

```scala
object Meters extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition =
    Generator.loadType(TypeName("Meters", _namePackage = Seq("draco", "base")))
  lazy val dracoType: Type[Meters] = Type[Meters](typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base](typeDefinition)

  def apply(_value: Double): Meters = new Meters {
    override lazy val value: Double = _value
    override lazy val typeDefinition: TypeDefinition = Meters.typeDefinition
  }
  lazy val Null: Meters = apply(_value = 0.0)
}
```

Because `App` uses `DelayedInit`, every `val` in a companion must be `lazy val` to avoid null-on-cross-object-access; the Generator enforces this in its output.

Domain, rule, and actor roles are no longer expressed by *which trait the companion extends*, but by *which aspect block its `TypeDefinition` populates* (see the *Aspects* subsection below). Pekko's typed API requires `Behavior[T]`, which `Actor[T]` implements; the integration uses `asInstanceOf` at that boundary rather than parameterizing the companion convention.

### TypeDefinition and Aspects

A `TypeDefinition` is the complete schema for a type. It carries a `typeName` plus four **aspect blocks**, each grouping the fields relevant to one concern:

```text
TypeDefinition  (extends Aspects)
  +-- typeName     : TypeName       -- qualified name
  +-- dracoAspect  : DracoAspect    -- structure shared by every type
  +-- domainAspect : DomainAspect   -- domain membership
  +-- ruleAspect   : RuleAspect     -- rule LHS/RHS  (populated only for rules)
  +-- actorAspect  : ActorAspect    -- actor handlers (populated only for actors)
```

`Aspects` is itself a `DracoType` and declares the four aspect-block fields; `TypeDefinition` adds `typeName`. Each aspect is in turn a `DracoType` with its own JSON definition, so the schema is built out of the same self-describing types as everything else.

**`DracoAspect`** — the structure every type shares:

| Field | Type | Purpose |
|-------|------|---------|
| `derivation` | `Seq[TypeName]` | Parent traits (`extends ... with ...`) |
| `extensible` | `TypeName` | Optional primary base taking the head of the `extends` clause; usually empty since `Extensible` was retired |
| `modules` | `Seq[TypeName]` | Submodules (makes the trait `sealed`) |
| `elements` | `Seq[TypeElement]` | Trait body members |
| `factory` | `Factory` | Companion `apply()` specification |
| `globalElements` | `Seq[BodyElement]` | Companion object members |
| `superDomain` | `TypeName` | Parent domain (domain inheritance) |
| `source` / `target` | `TypeName` | Transform source/target |

**`DomainAspect`** — domain membership:

| Field | Type | Purpose |
|-------|------|---------|
| `typeName` | `TypeName` | The domain this type belongs to: a self-loop if this type *is* a domain, a container-pointer if it is a leaf |
| `elementTypeNames` | `Seq[String]` | Member type names (non-empty only for a domain) |

**`RuleAspect`** — rule definition:

| Field | Type | Purpose |
|-------|------|---------|
| `variables` | `Seq[Variable]` | Rule fact variables |
| `conditions` | `Seq[Condition]` | Rule LHS conditions |
| `values` | `Seq[Value]` | Rule value extractors |
| `pattern` / `action` | `Pattern` / `Action` | Rule LHS / RHS |

**`ActorAspect`** — actor handlers:

| Field | Type | Purpose |
|-------|------|---------|
| `start` / `message` / `signal` | `Action` | Actor construction / receive / signal handlers |

A type's role is read **structurally** from these aspects rather than from a marker trait — the Generator's dispatch reduces to a few predicates:

- **Domain** — `domainAspect.elementTypeNames` is non-empty.
- **Leaf** — `domainAspect.typeName` points at a *different* domain (its container), and the type declares no members of its own (`isLeaf`).
- **Rule** — `ruleAspect.variables` is non-empty.
- **Actor** — `dracoAspect.derivation` reaches `ActorType` / Pekko `ExtensibleBehavior`.

All aspect fields default to empty/Null, so a simple leaf needs only `typeName`, a `dracoAspect.derivation`, and a `domainAspect.typeName`. `TypeDefinition` serializes to and from JSON via Circe with empty blocks elided, so a type can be defined entirely in a JSON file, loaded at runtime, and used to generate its Scala source.

### TypeName

`TypeName` identifies a type with three fields:

| Field | Type | Purpose |
|-------|------|---------|
| `name` | `String` | Simple type name (e.g., `"Meters"`) |
| `namePackage` | `Seq[String]` | Package path (e.g., `Seq("draco", "base")`) |
| `typeParameters` | `Seq[String]` | Type parameters (e.g., `Seq("T")` for `Primal[T]`) |

Derived fields:
- `namePath` — fully qualified: `"draco.base.Meters"`
- `resourcePath` — JSON resource path: `"/draco/base/Meters.json"`

Domain-ness is determined by the `domainAspect.typeName` self-loop, not by `TypeName` alone. Rule and actor roles are expressed through the rule/actor aspect blocks, not through `TypeName`.

### TypeElement Hierarchy

The members of a type are described by `TypeElement`, a sealed hierarchy that extends `Primal[String]`. Every element carries `name`, `valueType`, `parameters`, `body`, and a `value` (the expression body, defaulting to `""`):

```text
TypeElement                      -- name, valueType, parameters, body, value
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

JSON serialization uses a `"kind"` discriminator field, and every field is elided when empty. A `Fixed` element with a value looks like:

```json
{
  "kind": "Fixed",
  "name": "text",
  "valueType": "String",
  "value": "s\"Added $i to primeSet and naturalSet.\""
}
```

The `value` field was added to `TypeElement` so that default-bearing elements survive a YAML → JSON round-trip without their defaults being stripped.

### Domains

A domain is a named collection of types — the unifying artifact a set of type definitions coheres around. Every domain has:

- A `TypeDefinition` whose `domainAspect.elementTypeNames` lists its member types, with `domainAspect.typeName` self-looping to the domain itself.
- A `TypeDictionary` mapping each member `TypeName` to its `TypeDefinition`.
- A `Domain[T]` instance that ties these together.

Domains are **peers**, not a hierarchy: they all sit at the same level in the `DomainDictionary`. A member type points back at its containing domain through its own `domainAspect.typeName`, so membership is recorded on both sides — the domain lists the member, the member names the domain.

A domain's companion follows the same val convention as any other type; the only distinctions are that `elementTypeNames` is non-empty and `domainType` points at the domain itself:

```scala
object Base extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition =
    Generator.loadType(TypeName("Base", _namePackage = Seq("draco", "base")))
  lazy val dracoType: Type[Base] = Type[Base](typeDefinition)
  lazy val elementTypeNames: Seq[String] =
    Seq("Cardinal", "Coordinate", "Distance", "Meters",
        "Nominal", "Ordinal", "Radians", "Rotation", "Unit")
  lazy val domainType: Domain[Base] = Domain[Base](typeDefinition)
}
```

### The Four Endogenous Domains

Draco's own packages are its primary domain dictionary. Rather than teaching the framework through invented example domains, the canonical reference is the set of domains shipped in `draco.*`. Four of them, taken together, exercise every framework feature:

| Domain | Package | Demonstrates |
|--------|---------|--------------|
| **Draco** | `draco` | Self-description — the meta-domain whose members *are* the type system |
| **Base** | `draco.base` | `Primal[T]` value types and sealed measurement hierarchies |
| **Primes** | `draco.primes` | The RETE rule engine and stateful working memory |
| **Language** | `draco.language` | The human-authoring surface and the language-parametric Generator trajectory |

**Draco** — the meta-domain. Its members are the type system itself: `DracoType`, `Primal`, `Holon`, `Type`, `Aspects` and the four aspect blocks, the `TypeElement` family, `TypeDefinition`, `TypeName`, `Domain`, `Rule`, `Actor`, `TypeTransform`, `DomainTransform`, the `Generator`, and the I/O pipeline (`SourceContent`, `ContentSink`). Because each of these is itself defined by a JSON `TypeDefinition`, the package is self-generating: `TypeDefinition` is described by a `TypeDefinition`, and the entire `draco.*` package is reproducible from its JSON by the Generator (see *Code Generation*).

**Base** — primitive measurement types, demonstrating `Primal[T]` and `Holon[T]`:

```text
Unit                                    -- DracoType
  +-- Cardinal[T]   (Unit, Primal[T])         -- quantities with magnitude
  |     +-- Distance[T]
  |     |     +-- Meters                       -- T = Double
  |     +-- Rotation[T]
  |           +-- Radians                      -- T = Double
  +-- Ordinal       (Unit, Primal[Enumeration])-- ordered categories
  +-- Nominal       (Unit, Primal[String])    -- unordered labels

Coordinate[T <: Product]  (Holon[T])    -- spatial positions, independent of Unit
```

`Cardinal[T]` is deliberately unconstrained at the type level — `Numeric` bounds are applied at the point of use, not on the type. `Coordinate` extends `Holon` rather than `Primal`: a coordinate is a *perspective* onto composite spatial structure, and it is structurally self-describing — there are no named `Cartesian`, `Polar`, or `Spherical` types; a coordinate's dimensionality and system live in its type parameter (a 3-tuple of one `Meters` and two `Radians` is implicitly spherical). The measurement types were stripped of vestigial `name`/`description` metadata, leaving each a minimal self-describing type.

**Primes** — a rule-driven domain (`Accumulator`, `Numbers`, `Primes`, and three RETE rules). It is the working demonstration of the rule engine; see *Rules*, below.

**Language** — `Language` and `YAML`. This domain backs draco's human-authoring surface (YAML as a stand-in for JSON; see *Code Generation*) and seeds the longer-term `Generator[L]` direction, in which the Generator is itself a domain parameterized by target language.

### Rules

A rule is a type whose `ruleAspect` is populated. The Primes domain ships three, defined entirely in JSON resources named with a `.rule` aspect (`draco/primes/PrimesFromNaturalSequence.rule.json`) and generated into suffixed Scala types (`PrimesFromNaturalSequenceRule`). A rule's `ruleAspect` carries:

- `variables` — fact variables bound in the RETE network (`accumulator`, `i1`, …; bound as `$accumulator`, `$i1` in generated code)
- `conditions` — Boolean predicates compiled by Evrete's runtime Java compiler
- `action` — the body executed when the rule fires

The Generator detects a rule via `ruleAspect.variables.nonEmpty` and produces a Scala source file with: a trait, a companion carrying the usual `typeDefinition` / `dracoType` / `domainType` vals plus an Evrete `pattern` (registers the rule with a `Knowledge` base) and `action` (the RHS lambda), and condition functions as companion methods referenced by fully qualified name so Evrete's compiler can resolve them.

Example JSON rule (the prime sieve — deletes composite numbers from working memory):

```json
{
  "typeName": { "name": "PrimesFromNaturalSequence.rule", "namePackage": ["draco", "primes"] },
  "ruleAspect": {
    "variables": [
      { "kind": "Variable", "name": "accumulator", "valueType": "Accumulator" },
      { "kind": "Variable", "name": "i1", "valueType": "Integer" },
      { "kind": "Variable", "name": "i2", "valueType": "Integer" },
      { "kind": "Variable", "name": "i3", "valueType": "Integer" }
    ],
    "conditions": [
      {
        "kind": "Condition", "valueType": "Boolean", "value": "i1 * i2 == i3",
        "parameters": [
          { "kind": "Parameter", "name": "i1", "valueType": "Integer" },
          { "kind": "Parameter", "name": "i2", "valueType": "Integer" },
          { "kind": "Parameter", "name": "i3", "valueType": "Integer" }
        ]
      }
    ],
    "action": {
      "kind": "Action", "valueType": "Unit",
      "body": [
        { "kind": "Monadic", "valueType": "Unit", "value": "ctx.delete(i3)" },
        { "kind": "Monadic", "valueType": "Unit", "value": "accumulator.compositeSet.addOne(i3)" }
      ]
    }
  },
  "domainAspect": { "typeName": { "name": "Primes", "namePackage": ["draco", "primes"] } }
}
```

Rules are registered with an Evrete `Knowledge` base and fired against a `StatefulSession` of working-memory facts. Because Evrete's working memory uses boxed types, rule variables use `classOf[Integer]`, not `classOf[Int]`.

### Actors

Draco integrates Apache Pekko for actor-based concurrency. An actor is a type whose `actorAspect` is populated:

```text
ExtensibleBehavior[T]      (Pekko)
  +-- Actor[T]             -- generic actor container; extends ActorType
```

`ActorAspect` carries three actions — `start` (run once at construction), `message` (Pekko's `receive`) and `signal` (`receiveSignal`). An actor is no longer a separate sibling type: rather than a standalone `.actor.json` file, an actor lives as the `actorAspect` block on its *parent* type's `TypeDefinition`, so a type and its behavior travel together. The Generator detects an actor when `dracoAspect.derivation` reaches `ActorType` / Pekko's `ExtensibleBehavior`, and emits Pekko imports plus an `Actor[T]`.

To hand an `Actor[T]` to Pekko's typed `ActorSystem[T]`, cast at the integration boundary — this preserves the uniform companion convention rather than parameterizing it for actors alone:

```scala
val system = ActorSystem[Natural](Natural.actorType.asInstanceOf[Actor[Natural]], "natural")
system ! Natural(10)
```

There is no draco-endogenous actor type; the end-to-end actor path is exercised by the test-resident `NaturalActor`.

### Code Generation

The `Generator` reads JSON definitions and produces Scala source. `generate(td: TypeDefinition)` reads the type's role from its aspects and dispatches through a flat predicate table:

1. **Rule** (`isRule`) — name ends in `.rule`, or `ruleAspect.variables` is non-empty. Emits Evrete rule imports plus a companion carrying the rule's `pattern`, `action`, and condition functions; the generated type name gains a `Rule` suffix.
2. **Domain** (`isDomain`) — `domainAspect.elementTypeNames` is non-empty. Emits domain imports plus a companion exposing the member type list.
3. **Object-only** (`isObjectOnly`) — no trait, factory, or derivation, but has `globalElements`. Emits a bare `object … extends DracoType` with `dracoType = this`.
4. **Leaf or Actor** (`isLeaf || isActor`) — everything else. Leaves and actors share one trait+companion template, differentiated by an `Actor` name suffix and Pekko imports when the type's `derivation` reaches `ActorType` / `ExtensibleBehavior`.
5. A final `else` throws — the four predicates are meant to partition the type space, so the throw catches predicate drift rather than silently mis-generating.

`generate(tds: Seq[TypeDefinition])` emits several types into one file, topologically sorted by module dependency.

**JSON is normative; loading is JSON-only.** Every draco type has a JSON definition, and that JSON is the single form the runtime loads. Generated Scala calls `draco.Generator.loadType(TypeName(...))` — or `loadRuleType(...)` for rules — which reads the JSON from the classpath via `getResourceAsStream`. The fully qualified reference lets generated code compile in any package, including the `generated.draco` package used by the round-trip tests. (`TypeDefinition.load` no longer exists; the Generator owns all loading.)

**The package is generator-canonical.** Every hand-written file under `src/main/scala/draco/` is byte-for-byte identical to what the Generator emits from that type's JSON — verified by `DracoGenTest`, whose comparison-exclusion map is now empty. No hand-customizations remain on the type declarations; the JSON genuinely is the source.

**YAML is a human-authoring stand-in, not a load path.** A JSON file may keep a YAML twin for easier hand-editing. `bin/draco-gen from-yaml` / `to-yaml` convert between the two with git-aware safety (a conversion never silently clobbers uncommitted work), but the loader never reads YAML — JSON remains the only runtime form.

The content pipeline (`SourceContent` / `ContentSink` / `Main` / `Test`) reads JSON from `src/main/resources/` and writes generated Scala to `src/main/scala/`. For Dreams (deployed as a jar), `SourceContent` / `ContentSink` handle file-based I/O to user-defined domain directories.

## Semantic Preservation in Data Transformation

The combination of self-describing types, domain-scoped dictionaries, and rule-based transformation enables a specific architectural goal: data transformations that preserve semantic meaning.

When data moves between domains (e.g., from a sensor domain to an analysis domain), the type system ensures that:

1. **Types carry their own meaning.** A `Meters` value is not just a `Double`; it carries its lineage through `Distance`, `Cardinal`, and `Unit`. Any transformation that consumes or produces `Meters` can verify semantic compatibility at the type level.

2. **Domains define vocabularies.** Each domain's `TypeDictionary` is a closed vocabulary. A transform between domains can map source types to sink types explicitly, and the dictionaries make unmapped types visible.

3. **Rules preserve invariants.** Domain-specific rules can enforce constraints that pure type checking cannot. The prime sieve demonstrates this: the rule `i1 * i2 == i3` is a semantic constraint that operates on the values, not just the types.

4. **Transforms are first-class.** A `TypeTransform[S, T]` relates one type to another; a `DomainTransform[S, T]` relates one domain to another. Both extend `Holon[(S, T)]`, viewing the source/target pair as composite structure, and carry their `source` and `target` on the `dracoAspect`. A `DomainTransform` is itself a domain — a *transform domain* — so transformations can be composed, inspected, and governed by the same type system that governs the data they move.

## Working Features (Verified by Tests)

**Type system**
- `DracoType` as universal root — every `draco.*` type extends it and carries its own `typeDefinition`.
- Self-describing schema: `TypeDefinition` (four aspect blocks), `TypeName`, the sealed `TypeElement` hierarchy, and `Aspects` as their shared parent.
- Companion val convention — `dracoType` / `domainType` (plus `ruleType` / `actorType` where relevant); the former `TypeInstance` / `*Instance` trait family is gone.
- `Primal[T]` (value) and `Holon[T <: Product]` (perspective) as orthogonal axes under `DracoType`.
- `TypeTransform[S, T]` and `DomainTransform[S, T]`, both `extends Holon[(S, T)]`.
- `isLeaf` detection and the `DomainAspect.typeName` self-loop (domain) / container-pointer (leaf) distinction.

**Code generation**
- Structural role dispatch from aspects: `isRule` / `isDomain` / `isObjectOnly` / `isLeaf` / `isActor`, with an exhaustivity guard.
- Multi-type generation in one file, topologically sorted by module dependency.
- Codec generation — field-based codecs, `"kind"`-discriminated dispatch for sealed hierarchies, `Codec.sub` subtype wiring; the `TypeElement.value` field keeps codecs round-trip-symmetric.
- Factory generation (parameter-derived or custom bodies), type-parameterized `apply[T]`, and `Null` instances (including parameterized `Null: Actor[_]`).
- Import generation — package-hierarchy, circe, Pekko, cross-package, and external-type imports (`URI`, `BufferedSource`, `Consumer`, …); rule-specific imports for rules.
- **Generator-canonical package** — every file under `src/main/scala/draco/` is byte-equivalent to Generator output; `DracoGenTest`'s comparison-exclusion map is empty.

**JSON / YAML**
- JSON-normative loading: `Generator.loadType` / `loadRuleType` read JSON from the classpath via `getResourceAsStream`; `TypeDefinition.load` removed.
- Complete JSON definitions for every draco framework type.
- YAML twins as a human-authoring stand-in, mediated by `bin/draco-gen from-yaml` / `to-yaml` with git-aware safety; YAML is never a load path.

**Rule engine**
- Evrete RETE integration with runtime-compiled conditions (fully qualified class names required).
- Rule execution over a `StatefulSession` working memory — the prime sieve; facts boxed as `classOf[Integer]`.
- Rules defined in `.rule` JSON resources, generated to `…Rule`-suffixed Scala types.

**Actors**
- Pekko integration via `Actor[T]` / `ActorType`; an actor lives as the `actorAspect` of its parent type, not as a separate `.actor` sibling.
- `NaturalActor` end-to-end test: JSON load, actor creation, message send.

**Domains**
- `TypeDictionary` and `DomainDictionary` domain-scoped registries; domains are peers, discovered by scanning.
- **Base** measurement hierarchy (Unit, Cardinal, Distance/Meters, Rotation/Radians, Ordinal, Nominal, Coordinate), stripped of vestigial `name`/`description`.
- **Primes** rule domain (Accumulator, Numbers, three rules), fully canonicalized to Generator form.
- **Language** sub-domain (Language, YAML).
- **World** message-domain example under `src/mods/` — Aerial/Terrestrial/Marine/Ethereal media (plus `Sentient`) as subdomains of `World`, crossing through the `Observable` world-fact (meaning-preserving transform proven by `AerialTerrestrialTransformTest`).

**Tooling**
- `bin/draco-gen` — Bash-invocable Generator CLI (sbt-assembly fat JAR): `generate` / `compile` / `compile-multi` / `inspect` / `discover` / `verify` / `from-yaml` / `to-yaml`.
- `bin/draco-sc` — runs scala-cli scripts under `src/mods/scala/scripts/` against the assembled draco jar: `list-domain`, `list-domains`, `who-extends`, `derivation-chain`, `inspect-type`, `diff-type`.
- `src/mods/` — a third source tier alongside `main` and `test` for speculative outer layers; `mods → main` references allowed, `main → mods` forbidden.
- draco-dev-journal extraction tooling under `draco-dev-journal/tools/` — pulls user↔assistant pairs from Claude Code session logs, matches against committed chapters, surfaces gaps.

## Work in Progress

The active backlog lives in [GitHub Issues](https://github.com/ejb816/nexonix/issues); `priority-next` flags the natural pickup. The larger in-flight directions:

- **Generator domain** — `draco.generator.Generator[L]` as a self-describing domain parameterized by target language, with capability domains (`draco.rete`, `draco.json`, `draco.actor`, `draco.scala`). Coexists with the permanent hand-written `draco.Generator`.
- **Dreams application** (`draco.dreams`) — Domain Rules Editor Actor Message Service; a built-in editor for creating and modifying types, domains, rules, and actors via their JSON definitions. Currently scaffolded in `src/main/scala/draco/dreams`; `src/mods/` is its intended long-term home as a consumer layer atop draco.
- **Orion** (`draco.dreams.orion`) — Open Resilient Inter-Operability Nexus; five ION interaction patterns for cross-domain system-of-systems integration.
- **Getting Started guide** — a minimal walkthrough appears below; a fuller guide is planned.

## Building and Running

This is a Scala 2.13 project using sbt.

```bash
sbt compile       # Compile
sbt test          # Run all tests
sbt ~compile      # Continuous compilation
sbt assembly      # Build the fat JAR that backs bin/draco-gen and bin/draco-sc

# Run specific tests
sbt "testOnly draco.DracoGenTest"
sbt "testOnly draco.TypeDefinitionTest"
sbt "testOnly draco.primes.PrimesRulesTest"
sbt "testOnly draco.primes.PrimesRulesTest -- -z \"PrimesFromNaturalSequence\""
```

The two `bin/` tools run against the assembled JAR (rebuild it with `sbt assembly` after changing draco source):

```bash
bin/draco-gen verify draco/base/Meters.json   # regenerate + diff a type against its checked-in Scala
bin/draco-gen from-yaml draco/base/Meters.yaml # author in YAML, convert to normative JSON
bin/draco-sc who-extends DracoType             # runtime query against the jar
```

Released builds are tagged `v2.0.0-alpha.1` through `v2.0.0-alpha.3` (see [GitHub Releases](https://github.com/ejb816/nexonix/releases)); CI publishes a JAR on tag push.

## Getting Started

Draco types are authored as JSON and generated into Scala. Here is the smallest end-to-end loop — adding a new leaf measurement type to the Base domain.

**1. Write the JSON definition** at `src/main/resources/draco/base/Celsius.json`:

```json
{
  "typeName": { "name": "Celsius", "namePackage": ["draco", "base"] },
  "dracoAspect": {
    "derivation": [
      { "name": "Cardinal", "namePackage": ["draco", "base"], "typeParameters": ["Double"] }
    ],
    "factory": {
      "kind": "Factory",
      "valueType": "Celsius",
      "parameters": [ { "kind": "Parameter", "name": "value", "valueType": "Double" } ]
    }
  },
  "domainAspect": { "typeName": { "name": "Base", "namePackage": ["draco", "base"] } }
}
```

It names `Base` as its containing domain (`domainAspect.typeName`), derives from `Cardinal[Double]`, and gives a `factory` describing how to construct one.

**2. Generate the Scala** (after `sbt assembly`). `draco-gen generate` prints the source to stdout; redirect it into place:

```bash
bin/draco-gen generate draco/base/Celsius.json > src/main/scala/draco/base/Celsius.scala
```

The result is `trait Celsius extends Cardinal[Double]` plus a companion carrying `typeDefinition` / `dracoType` / `domainType`, an `apply(_value: Double)`, and a `Null`.

**3. Register it in the domain** by adding `"Celsius"` to `draco/base/Base.json`'s `domainAspect.elementTypeNames`, then regenerating `Base` the same way.

**4. Use it.** The generated companion loads its own definition from the classpath at runtime:

```scala
val c = Celsius(21.5)
c.value                 // 21.5 : Double
Celsius.typeDefinition  // the loaded TypeDefinition
Celsius.dracoType       // Type[Celsius]
```

`bin/draco-gen verify draco/base/Celsius.json` then confirms the checked-in Scala still matches Generator output — the round-trip the whole `draco.*` package maintains.

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
      draco/                          -- JSON definitions (normative source of truth)
        *.json / *.yaml               -- core framework types (+ optional YAML twins)
        base/                         -- Base domain definitions
        primes/                       -- Primes domain; *.rule.json rule definitions (flat)
        language/                     -- Language sub-domain (Language, YAML)
      logback.xml                     -- Runtime logging config (Pekko suppressed)
    scala/
      draco/                          -- Generator-canonical framework types
        base/                         -- Base domain (Unit, Cardinal, Coordinate, ...)
        primes/                       -- Primes domain (Accumulator, Numbers, *Rule)
        language/                     -- Language sub-domain
        dreams/                       -- Dreams application scaffold (Dreams, Service, orion/)
  test/
    resources/
      draco/                          -- Framework test JSON
      domains/                        -- Example-domain JSON (natural)
      logback-test.xml                -- Test logging config
    scala/
      draco/                          -- Framework + generation tests (DracoGenTest, ...)
      domains/                        -- Example-domain tests (natural, World media)
      org/nexonix/                    -- Additional rule and format tests
  mods/                               -- Third source tier: speculative outer layers
    resources/domains/                -- World example-domain JSON (world, aerial, sentient, ...)
    scala/domains/                    -- World example domains (world, aerial/.../ethereal, sentient)
    scala/scripts/                    -- bin/draco-sc scripts (list-domains, who-extends, ...)
bin/
  draco-gen                           -- Generator CLI (generate/verify/from-yaml/...)
  draco-sc                            -- scala-cli runtime-query script runner
```
