# Draco

Draco is a self-describing, domain-driven framework for building data transformation
services that preserve semantic meaning within and across domains. Every concept — from a
primitive measurement to a business rule — is a first-class **type** that carries a
description of its own structure.

## Core Idea

Most systems separate a type's definition from its runtime representation. A class has
fields, but reasoning about them programmatically needs reflection or an external schema.
Draco closes that gap: every type carries a `TypeDefinition` describing its own structure
— its members, its parents, how to construct it, how it serializes. This is not bolted on
afterwards; it is the foundation.

So a `Meters` value does not merely hold a number. It knows it is a `Distance`, which is a
`Cardinal`, which is a `Unit` in the `Base` domain. A rule engine can match against these
types. A dictionary can enumerate a domain's members by name. A **target** can project the
whole thing into a programming language. All of it works because the type system is closed
over itself: `TypeDefinition` is described by a `TypeDefinition`.

---

## A note on vocabulary

**The architecture sections below are written in draco's own terms, not in any target
language's.** Draco is intended to project into several languages — Scala today, Haskell
and Lean intended — so a description that leans on one target's vocabulary describes an
accident of the current projection rather than the framework.

Where a host term is unavoidable today it is marked **[scala]** and listed in
*Language-specific residues* near the end, which records what would have to change for the
term to become neutral. That list is meant to shrink. When it can be emptied, this note
goes away.

The reference points for neutral vocabulary:

| draco says | rather than |
|---|---|
| type, definition | class, trait, record |
| **derivation** | extends / inherits / implements |
| **element** (fixed, mutable, dynamic) | field, method, property |
| **globals** | static members, companion members |
| **factory** | constructor, `apply`, `new` |
| **modules** | a sealed or closed family |
| **aspect** | mixin, annotation, marker interface |
| **projection** / **target** | code generation / backend |
| `[T]` `{T}` `{K,V}` `(A, B)` `A -> B` | sequence, set, map, tuple, function type |

The last row is not cosmetic. A map is stated `{K, V}` in both the normative JSON and the
DRAKE surface, and the Scala spelling `Map[K, V]` is produced by the Scala target alone —
nothing upstream of projection names it. That is the finished shape the rest of the
vocabulary is moving toward.

---

## Architecture

### The type system

Everything is a `DracoType` — a type whose single obligation is to carry its own
`typeDefinition`:

```text
DracoType                        -- universal root; carries typeDefinition
  |
  +-- Primal(T)                  -- carries a single value of type T
  +-- Holon(T)                   -- a *perspective* onto composite structure
  |
  +-- Type(T)                    -- the canonical runtime representative of a type
  |
  +-- Aspects                    -- carrier of the five aspect blocks
  |     +-- TypeDefinition       -- Aspects + typeName; the full schema of a type
  |
  +-- DomainType                 -- a named domain with a type dictionary
  |     +-- Domain(T)
  +-- RuleType                   -- a rule: pattern + action
  |     +-- Rule(T)
  +-- ActorType                  -- an actor: message and lifecycle handlers
  |     +-- Actor(T)
  |
  +-- TypeTransform(S, T)        -- a type-to-type transform      (derives Holon((S, T)))
  +-- DomainTransform(S, T)      -- a domain-to-domain transform  (derives Holon((S, T)))
```

`Primal` and `Holon` sit on different axes. `Primal(T)` is about *value* — it carries one
value, primitive or composite. `Holon(T)` is about *perspective* — it marks a point of view
onto composite structure. A type may be a value carrier, a perspective, both, or neither.

The whole `draco` package is itself a domain — the meta-domain whose members are the type
system.

### TypeDefinition and aspects

A `TypeDefinition` is a `typeName` plus **five aspect blocks**, each grouping one concern:

```text
TypeDefinition  (derives Aspects)
  +-- typeName     : TypeName
  +-- dracoAspect  : DracoAspect    -- structure shared by every type
  +-- domainAspect : DomainAspect   -- domain membership
  +-- ruleAspect   : RuleAspect     -- rule pattern and action
  +-- actorAspect  : ActorAspect    -- actor handlers
  +-- codecAspect  : CodecAspect    -- serialization discriminator
```

**`DracoAspect`** — the structure every type shares:

| Field | Purpose |
|---|---|
| `derivation` | the type's parents |
| `extensible` | optional primary parent taking the head position |
| `modules` | submodules; declares a closed family |
| `elements` | the type's own members |
| `factory` | how an instance is constructed |
| `globalElements` | members belonging to the type itself rather than an instance |
| `superDomain` | parent domain, for domain inheritance |
| `source` / `target` | transform endpoints |

**`DomainAspect`** — `typeName` (a self-loop if this type *is* a domain, a pointer to the
containing domain otherwise) and `elementTypeNames` (member names, non-empty only for a
domain).

**`RuleAspect`** — `pattern` (which carries the rule's variables and conditions) and
`action`.

**`ActorAspect`** — `messageType`, and the `start` / `message` / `signal` actions.

**`CodecAspect`** — `discriminator`, the field name a closed family encodes its variant
under.

**Role is presence, not name.** A type is a domain, a rule, or an actor because the
corresponding aspect is populated — never because of a name suffix, a filename, or a
marker parent. All aspects default to empty, so a simple type needs only a `typeName`, a
`derivation`, and a `domainAspect.typeName`.

### TypeName

`TypeName` is `name` + `namePackage` + `typeParameters`, with derived `namePath` and
`resourcePath`. It compares **structurally** — two names with the same content are the same
name.

Type parameters are part of that identity, and they carry a real distinction. A parameter
position holds either a **variable** or a **concrete type**. A type is **abstract** if any
position contains a variable at any depth — `Holon((S, T))` is abstract because `S` and `T`
are variables, not because the parameter is compound. A constraint on a variable (`T` bound
by some parent type) restricts what may be substituted and never makes the position
concrete. Only a fully concrete name can be the derivation for an atomic term: `Meters`
derives `Distance(Double)`, and that is what makes `Meters` a term rather than a schema.

So `Dictionary(K, V)` and `Dictionary(TypeName, TypeDefinition)` are different names — one
abstract, one concrete.

### TypeElement

A type's members are described by `TypeElement`, a closed family of eleven kinds:

```text
TypeElement                      -- name, valueType, parameters, body, value
  +-- BodyElement
        +-- Fixed                -- an immutable member
        +-- Mutable              -- a mutable member
        +-- Dynamic              -- a member computed from parameters
        +-- Local                -- a binding local to a body
        +-- Parameter            -- a factory or dynamic-member parameter
        +-- Monadic              -- a statement evaluated for effect
        +-- Condition            -- a predicate over parameters
        +-- Action               -- an ordered body, with variables in scope
        +-- Pattern              -- variables plus the conditions over them
        +-- Variable             -- a name bound to a type in a rule
        +-- Factory              -- construction: result type, parameters, body
```

Every element carries a `value`, and **the value's own structure classifies it**:

- a **string** is host-opaque source text, carried through to the target verbatim
- an **object** `{op: [operands]}` is a normative **expression tree** — `.` for paths, `()`
  for application, `->` for the arrow, `\` for abstraction, `if`, `(,)` for tuples, `=` for
  a named argument

The tree form is the direction of travel: a tree is projectable into any target, a string
is only meaningful to the one it was written for. Converting the remaining strings is
tracked work.

### Definitions, surfaces, and targets

Three representations, with one normative:

| | role |
|---|---|
| **JSON** (`X.json`) | **normative**. The definition. The only form loaded at runtime. |
| **DRAKE** (`X.drake`) | the human authoring **surface** — a whitespace-insignificant, keyword-bounded notation for the same content |
| **target source** (`X.scala`) | a **projection** into a programming language |

DRAKE is draco's own definition language. Its full specification is
`src/main/resources/draco/drake.dlt`, which is current and authoritative. In outline: a
type declares itself and its derivation, then keyword sections (`elements`, `factory`,
`globals`, `modules`), then `domain`, then optionally `rule` and `actor`. References are
written bare when they resolve in the referring type's own package and qualified
otherwise; a reference to a type outside every draco domain carries no package at all and
is written as a type expression (`Dictionary` derives `{K, V}`). Value types are `[T]` sequence, `{T}` set, `{K, V}` map, `mut {T}` mutable set,
`F(A, B)` application, `(A, B)` tuple, `A -> B` arrow.

Emission (`Drake.emit`) and parsing (`Drake.parse`) are mutual inverses, gated in both
directions across the corpus. One caveat for authors: **the parser builds expression trees
only for the application surface.** Other value forms come back as opaque text, so parsing
is currently a measurement tool rather than a complete authoring path.

### Loading

`TypeLoader` resolves a `TypeName` to a definition through a `DefinitionPath`:

```text
TypeLoader.loadType -> DefinitionPath.default.source(resourcePath) -> readDefinition
```

A `DefinitionPath` holds its **roots** explicitly and resolves **unique-or-error**: a name
found at more than one root is an ambiguity to report, not something to settle by search
order. Order carries no meaning, because order is exactly what cannot survive projection —
different hosts shadow by different rules, so depending on it would guarantee that the same
path meant different things in different targets.

The default path derives its roots from the host environment, but that is one realization,
not the definition. A path can be constructed from given roots with no host mechanism
involved, which is what makes the concept meaningful in a target that has none. A name that
resolves nowhere yields a name-only stub, which is a legitimate state — a member may be
declared before it is authored.

### Domains

A domain is a named collection of types. Every domain has a `TypeDefinition` whose
`domainAspect` self-loops and whose `elementTypeNames` lists its members, a `TypeDictionary`
mapping each member name to its definition, and a `Domain` instance tying them together.

Domains are **peers**, not a hierarchy — all at one level in the `DomainDictionary`.
Membership is recorded on both sides: the domain lists the member, the member names the
domain.

### The endogenous domains

Draco's own packages are its primary reference. Rather than teaching through invented
examples, the canonical material is what ships:

| Domain | Demonstrates |
|---|---|
| `draco` | self-description — the meta-domain whose members *are* the type system |
| `draco.base` | value types and measurement families |
| `draco.primes` | the rule engine and stateful working memory |
| `draco.format` (+ `json`, `xml`) | payload formats and path extraction over them |
| `draco.drake` | the definition surface as a domain |
| `draco.generator` | projection as a domain, parameterized by target |
| `draco.scalatarget` | the Scala target |
| `draco.rete` | rule-evaluation capability, held as its own vocabulary |

**Base** — measurement types:

```text
Unit
  +-- Cardinal(T)   (Unit, Primal(T))            -- quantities with magnitude
  |     +-- Distance(T)  +-- Meters
  |     +-- Rotation(T)  +-- Radians
  +-- Ordinal       (Unit, Primal(Enumeration))  -- ordered categories
  +-- Nominal       (Unit, Primal(String))       -- unordered labels

Coordinate(T)  (Holon(T))                        -- spatial position
```

`Cardinal(T)` is deliberately unconstrained: numeric requirements apply at the point of
use, not on the type. `Coordinate` derives `Holon` rather than `Primal` — a coordinate is a
*perspective* onto composite spatial structure, and it is structurally self-describing.
There are no named cartesian, polar, or spherical types; dimensionality and system live in
the type parameter.

### Rules

A rule is a type whose `ruleAspect` is populated. It is named for its concept — no suffix
on the file, the definition, or the projection. Its `pattern` carries the **variables** it
binds and the **conditions** over them; its `action` is the body that runs when the rule
fires.

A condition's parameters are **derived, not declared** — they are the pattern variables the
condition's expression mentions, computed at projection time from the expression tree. The
definition does not restate what it already implies.

```json
{
  "typeName": { "name": "PrimesFromNaturalSequence", "namePackage": ["draco", "primes"] },
  "ruleAspect": {
    "pattern": {
      "kind": "Pattern",
      "variables": [
        { "kind": "Variable", "name": "accumulator", "valueType": "Accumulator" },
        { "kind": "Variable", "name": "i1", "valueType": "Integer" },
        { "kind": "Variable", "name": "i2", "valueType": "Integer" },
        { "kind": "Variable", "name": "i3", "valueType": "Integer" }
      ],
      "conditions": [
        { "kind": "Condition", "valueType": "Boolean",
          "value": { "==": [ { "*": ["i1", "i2"] }, "i3" ] } }
      ]
    },
    "action": {
      "kind": "Action",
      "body": [
        { "kind": "Monadic", "value": { "()": [ { ".": ["ctx", "delete"] }, "i3" ] } }
      ]
    }
  },
  "domainAspect": { "typeName": { "name": "Primes", "namePackage": ["draco", "primes"] } }
}
```

The condition and the action body are expression **trees**, not text — the same normative
form any target can read.

### Actors

An actor is a type whose `actorAspect` is populated, named for its concept like any other.
The aspect carries a `messageType` and three actions: `start` (once, at construction),
`message` (per message received), and `signal` (lifecycle events).

An actor lives as an aspect on its type rather than as a separate artifact, so a type and
its behaviour travel together. Draco has an endogenous actor: the `Draco` domain carries an
actor aspect and validates its own dictionary by firing its own rules over its own members.

An `Assembly` wires a group of actors as pure data — members, bindings, and an entry point,
all by `TypeName` — validated and spawned generically rather than by hand-written wiring.

### Projection

A **target** turns definitions into source. `Generator` reads a `TypeDefinition` and
dispatches on what the definition *contains*:

1. two or more role aspects → **composed** — every present aspect contributes its block
2. rule aspect → rule projection
3. actor aspect → actor projection
4. domain → domain projection
5. globals but no type body → an object-only projection
6. otherwise → a plain type

Before dispatch, two normalizations run at the entry: a definition with no draco-domain
parent has the root appended — so an absent derivation means *derives the root*, and a
foreign parent alone still does — and neutral type expressions are rewritten into the
target's spelling. A
multi-definition overload emits a family into one file, ordered by dependency.

**The corpus is projection-canonical.** Every checked-in source file under the framework
tree is identical to what the target emits from that type's definition, verified by test
with an empty exclusion list. No hand-customization remains. Three gates hold the three
representations together:

| gate | pins |
|---|---|
| `DracoGenTest` | projection ≡ checked-in source, for every definition |
| `DrakeGenTest` | emitted surface ≡ checked-in `.drake` |
| `DrakeParseTest` | surface and definition round-trip in both directions |

---

## Language-specific residues

Everything below is a place where a target's vocabulary or mechanism has leaked into
something that ought to be neutral. Each should be replaced by a neutral description when
that becomes feasible; this list exists so the leaks are visible rather than assumed.

| Residue | Where | What neutral would look like |
|---|---|---|
| **[scala]** Eager-vs-deferred initialization discipline | every projected global must be lazily initialized, an artifact of the host's application-object semantics | a projection concern only; should never appear in a definition or in architecture prose |
| **[scala]** Sequence and set spellings | `valueType` strings still carry `Seq[T]` / `Set[T]` while maps already carry `{K, V}` | finish what the map started: `[T]` and `{T}` normative, host spelling produced by the target |
| **[scala]** Host-opaque value strings | most element `value`s are still target source text rather than expression trees | trees for every operator both renderers already know |
| **[scala]** Codec realization | `CodecAspect` is neutral (a discriminator), but codec derivation is expressed in one host library's encoder/decoder pair | a serialization capability domain, projected per target |
| **[scala]** Rule-evaluation binding | conditions are compiled by the host rule engine at runtime, requiring fully qualified names, and working memory boxes primitives | `draco.rete` as a capability domain expressing evaluation *discipline*, not one engine's configuration |
| **[scala]** Actor behaviour binding | `Actor(T)`'s derivation reaches a host actor library's behaviour type | an actor capability domain, with the host behaviour supplied by the target |
| **[scala]** Structural-identity members | `TypeName`'s identity comparison is authored as host-named members | a declared property — "this type's identity is structural" — that each target projects its own way |
| **[scala]** Type-parameter encoding | `typeParameters` is a list of strings, so a variable and its constraint are fused and abstractness is not computable | parameters as type expressions with variables and constraints separated |

The map constructor is the worked example of the whole column: it moved from a host name to
`{K, V}` in both the definition and the surface, with the host spelling produced in exactly
one function in the target. Each row above is the same move, not yet made.

---

## Semantic preservation in data transformation

Self-describing types, domain-scoped dictionaries, and rule-based transformation together
support one architectural goal: transformations that preserve meaning.

1. **Types carry their own meaning.** A `Meters` value carries its lineage through
   `Distance`, `Cardinal`, and `Unit`. A transformation consuming or producing it can
   check semantic compatibility, not just shape.
2. **Domains define vocabularies.** Each `TypeDictionary` is a closed vocabulary, so a
   transform between domains maps named types explicitly and unmapped types stay visible.
3. **Rules preserve invariants.** A rule enforces constraints that type checking cannot —
   the prime sieve's `i1 * i2 == i3` is a constraint on values, not types.
4. **Transforms are first-class.** `TypeTransform(S, T)` relates types; `DomainTransform(S, T)`
   relates domains. Both derive `Holon((S, T))`, viewing the pair as composite structure. A
   domain transform is itself a domain, so transformations compose and are governed by the
   same type system as the data they move.

## Working features

- **Self-description** — `TypeDefinition` describes `TypeDefinition`; the framework's own
  source is reproduced from its own definitions.
- **Three-way round trip** — definition, surface, and projection all pinned by tests.
- **Self-validation** — the `Draco` domain validates its own dictionary through its own
  rule engine, driven by a definition-backed actor.
- **Rules and actors from definitions** — including the media example chain, where a
  message crosses domains and its meaning is preserved at the sink.
- **Assemblies** — actor groups wired as data, validated and spawned generically.
- **Structural identity** — `TypeName` compares by content; definition resolution is
  unique-or-error across explicit roots.

## Work in progress

The backlog is [GitHub Issues](https://github.com/ejb816/nexonix/issues). Larger
directions:

- **Type expressions** — parameters and derivations in the same normative expression form
  as values, which is what makes abstract-versus-concrete computable rather than
  conventional.
- **Expression grammar** — parsing every value form into trees, closing the last gap
  between the surface and the definition.
- **`Generator(L)`** — projection as a definition-backed domain parameterized by target,
  with additional targets beyond Scala.
- **Dreams** — an editor for creating and modifying types, domains, rules, and actors
  through their definitions.
- **Orion** — cross-domain system-of-systems interaction patterns.

---

## Building and running

The sections from here on describe *this repository's current realization* and are
necessarily Scala-specific.

This is a Scala 2.13 project using sbt.

```bash
sbt compile
sbt test
sbt assembly      # builds the JAR behind bin/draco-gen and bin/draco-sc

sbt "testOnly draco.DracoGenTest"
sbt "testOnly draco.DrakeParseTest"
sbt "testOnly draco.primes.PrimesRulesTest -- -z \"PrimesFromNaturalSequence\""
```

Command-line tools run against the assembled JAR — rebuild after changing source:

```bash
bin/draco-gen verify draco/base/Meters.json     # regenerate and diff against checked-in source
bin/draco-gen discover draco/base/Base.json     # rebuild a domain's member list
bin/draco-sc who-extends DracoType              # runtime query against the JAR
```

DRAKE has a CLI (`emit | parse | check`) reachable through the assembled JAR; it has no
`bin/` wrapper yet.

Current build version is `2.0.0-alpha.5`. Releases are published on tag push.

## Getting started

Setup and the projection loop are target-specific, so there is one guide per target:

| Guide | Status |
|---|---|
| [`GETTING_STARTED_TARGET_SCALA.md`](GETTING_STARTED_TARGET_SCALA.md) | realized — the only working projection |
| [`GETTING_STARTED_TARGET_HASKELL.md`](GETTING_STARTED_TARGET_HASKELL.md) | stub — structure and open questions |
| [`GETTING_STARTED_TARGET_TYPESCRIPT.md`](GETTING_STARTED_TARGET_TYPESCRIPT.md) | stub — structure and open questions |

All three share one structure, because the loop itself is the same in each:

```text
  author X.json  ->  X.drake surface  ->  project into a target  ->  register  ->  verify
```

Only four things differ by target: the toolchain, the projection command, how you run the
result, and the command set. Everything else — the definition format, the surface
language, what registration means, what the gates check — is target-independent.

A definition is the same text whichever target projects it. This one, a measurement type
for the `Base` domain, lives at `src/main/resources/draco/base/Celsius.json`:

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

It names `Base` as its containing domain, derives from a concrete `Cardinal(Double)` — so
`Celsius` is an atomic term — and gives a factory. Nothing in it is Scala's; what differs
per target is only what comes out the other side. The per-target guide carries the
commands.

## Dependencies

| Library | Purpose |
|---|---|
| [Evrete](https://www.evrete.org/) | rule evaluation |
| [Apache Pekko](https://pekko.apache.org/) | actor runtime |
| [Circe](https://circe.github.io/circe/) | JSON serialization |
| [Logback](https://logback.qos.ch/) | logging |
| [ScalaTest](https://www.scalatest.org/) | testing |

## Project structure

```
src/
  main/
    resources/
      draco/                    -- definitions (normative) and .drake surfaces
        drake.dlt               -- the DRAKE specification
        base/ primes/           -- Base and Primes domains
        format/ rete/           -- format and rule-evaluation domains
        drake/ generator/ scalatarget/
    scala/
      draco/                    -- projection-canonical framework source
        base/ primes/ format/ rete/ drake/ generator/ scalatarget/
        dreams/                 -- Dreams scaffold (+ orion/)
  test/
    resources/ scala/           -- gates, rule tests, example-domain tests
  mods/                         -- engine tier and example domains
    scala/draco/                -- Generator, Drake, CLIs, DomainBuilder, Assembly
    scala/domains/              -- World media example chain
    scala/scripts/              -- bin/draco-sc scripts
    resources/domains/          -- example-domain definitions
bin/
  draco-gen                     -- projection CLI
  draco-sc                      -- runtime-query script runner
```

`src/mods` compiles into the same package tree as `src/main`. It holds the hand-written
engine — the projection and surface implementations that the framework does not yet
describe as definitions. Whether that tier is permanent or transitional is an open
question.
