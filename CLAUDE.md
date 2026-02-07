# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

## Architecture Overview

### Draco: Domain-Driven Rule Engine

Draco is a domain-driven rule engine built on top of the Evrete rules engine, with Apache Pekko for actor-based concurrency. The core concept is **Domains** containing **TypeDefinitions** that can have associated **Rules**.

### Key Abstractions

**Domain Hierarchy:**
- `DomainElement` - Base trait providing `knowledgeService`, `knowledge`, `domain`, and `typeDefinition`
- `DomainType` - Trait for types within a domain, containing `domainName`, `typeDictionary`, and `domainDictionary`
- `Domain[T]` - Generic domain container created with a `DomainName` and optional sub-domains

**Type System:**
- `TypeName` - Qualified name with package path (e.g., `TypeName("Primes", Seq("draco", "primes"))`)
- `TypeDefinition` - Complete type definition with elements, parameters, derivation, and rules
- `TypeElement` - Sealed trait with subtypes: `Fixed` (val), `Mutable` (var), `Dynamic` (def), `Parameter`. Each has a `typeValue: TypeValue`
- `TypeDictionary` - Maps `TypeName` → `TypeDefinition` for a domain
- `Primal[T]` - Base trait for value-carrying types with a single `value: T` field

**Definition Support Types (extend `TypeValue`):**
- `TypeValue` - Extends `Primal[String]`. Contains `parameters: Seq[Parameter]`, `preamble: Seq[TypeElement]`, `valueType: String`, and `value: String`. Note: TypeValue references TypeElement via preamble, and TypeElement references TypeValue, creating a mutual dependency
- `Condition` - Extends `TypeValue` with `valueType` fixed to "Boolean" and empty `preamble`. Used for rule conditions
- `Action` - Extends `TypeValue` (work in progress). Will be used for rule actions

**Rules:**
- `RuleDefinition` - JSON-serializable rule specification with `variables`, `conditions`, `values`, and `action`. The `conditions` field is being migrated from `Seq[String]` to `Seq[Condition]`
- `Rule` - Trait with `pattern` (Knowledge => Unit) and `action` (RhsContext => Unit)
- `Generator` - Generates Scala source code from both `TypeDefinition` and `RuleDefinition` JSON files

**Content Pipeline:**
- `SourceContent` - Reads source files from a URI root
- `ContentSink` - Writes generated content to output paths
- `Main.roots` - Default source root (resources) and sink root (scala)

### Transform Domains

Transform domains (`draco.transform`) model transformations between domains with `sourceDomain` and `sinkDomain`. Example domains: Alpha, Bravo, Charlie, Delta extend `DataModel`.

### Primes Example Domain

The `draco.primes` package demonstrates the rule engine with prime number sieve rules:
- `Accumulator` - Tracks prime/composite sets and timing
- `Numbers` - Natural sequence input
- Rules defined in JSON at `src/main/resources/draco/primes/rules/`
- Generated Scala rules at `src/main/scala/draco/primes/rules/`

### JSON Serialization

All domain types use Circe for JSON encoding/decoding. Type definitions and rules can be loaded from JSON resources and generated into Scala code.

### Actor Integration

`ActorBehavior[T]` extends Pekko's `ExtensibleBehavior[T]` for actor-based rule execution.

## Key Dependencies

- **Evrete** (`org.evrete`) - Core rules engine with RETE algorithm
- **Apache Pekko** - Typed actor system
- **Circe** - JSON parsing and encoding
- **ScalaTest** - Testing framework
