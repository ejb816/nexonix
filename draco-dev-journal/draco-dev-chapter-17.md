# Draco Dev Journal — Chapter 17

**Session date:** March 25–26, 2026
**Topic:** Alpha Release — CLI, REPL[L], Object-Only Types, sbt-assembly, GitHub Release

---

## Session Start — Test Failures and Bug Fix

Six tests were failing across four test suites. Two distinct issues:

### Issue 1: NPE in All Domain Companions

All 8 domain companions (Draco, Base, Primes, DataModel, Alpha, Bravo, Charlie, Delta) had the same initialization bug in the anonymous `Domain[T]` class:

```scala
lazy val domainInstance: DomainType = new Domain[Primes] {
    override val domainDefinition: TypeDefinition = TypeDefinition(
      typeDefinition.typeName,  // resolves to this.typeDefinition (null), not Primes.typeDefinition
      ...
    )
    override val typeDictionary: TypeDictionary = TypeDictionary(domainDefinition)
    override val typeDefinition: TypeDefinition = typeInstance.typeDefinition
}
```

The `override val typeDefinition` inside the anonymous class shadows the outer companion's `typeDefinition`. During `domainDefinition` init, `typeDefinition.typeName` resolves to `this.typeDefinition` (the not-yet-initialized override), causing null.

**Fix:** `override val` → `override lazy val` for all three fields (`domainDefinition`, `typeDictionary`, `typeDefinition`) in all 8 domain files.

### Issue 2: Stale Resource Paths in TestValue

`TestValue.scala` referenced old sub-package paths (`draco/primes/rules/AddNaturalSequence.json`) instead of the flat aspect naming (`draco/primes/AddNaturalSequence.rule.json`).

**Fix:** Updated all three paths to the new convention.

---

## Alpha Release Planning

The session pivoted to preparing the first release for others to work with — both clone (source) and binary (JAR) distribution.

Key build.sbt issues identified:
- Version was `1.1.1-SNAPSHOT` — doesn't reflect the scope of the redesign
- `mainClass` referenced stale `org.mitre.anvil.rules.DataDictionaryMap`
- No assembly plugin for fat JAR creation
- No CI/CD for automated releases

---

## Design Decision: REPL[L] and CLI

> **Dev:** I'm thinking of creating a CLI for the framework so it can still have a mainClass. What about embedding a language REPL for whatever the generated language is?

This led to designing two new types:

### REPL[L] — Language-Parameterized Evaluator

`REPL[L]` is parameterized by target language, a peer of `Generator[L]`:

```
Generator[L]  — TypeDefinition → L    (emitter)
REPL[L]       — L → result            (evaluator)
```

**Key design decision:** REPL[L] is a peer of Generator[L], not a specialization. They share the language parameter but have fundamentally different roles. Neither implies the other. What binds them is the capability domain (`draco.scala` when `L = Scala`), not inheritance.

> **Dev:** You've validated my clairsentience/claircognizance. You gave me the answer I felt before I serialized gnosis into language.

The relationship was tested against Haskell: independent type classes sharing a type variable, not derived from each other.

### CLI — Framework Entry Point

CLI takes an optional REPL instance. Initial design:

```scala
trait CLI extends Extensible { val repl: REPL[_] }
object CLI extends App with TypeInstance { ... }
```

Type algebra: `REPL[_]` (existential — any language), defaults to `REPL.Null: REPL[Nothing]` (bottom type = no language = no REPL).

---

## The Static Forwarder Problem

Building the assembly JAR revealed a Scala 2 limitation: when an object has a companion trait of the same name, the JVM can't generate a static `main` forwarder. The trait's class file is an interface and can't hold static methods.

This meant `java -jar draco-2.0.0-alpha.1.jar` couldn't find an entry point for `draco.CLI`.

### Failed Approach: Rename the Trait

Renaming `trait CLI` to `trait CLIType` fixed the JVM issue but broke CLI out of the type system — the naming convention requires trait and object to share the same name.

### Failed Approach: Remove App

Removing `extends App` and defining `def main` explicitly kept the trait name but still couldn't generate the static forwarder — the companion trait is the fundamental issue, not App.

---

## Design Decision: Object-Only Types

> **Dev:** Create a flag or conditional in the definition that tells the generator that the trait won't be useful or used and to just create the object.

Four options were discussed:

| Option | Approach | Verdict |
|--------|----------|---------|
| A | Boolean flag `_objectOnly` on TypeDefinition | Rejected — declaration, not discovery |
| B | Empty elements convention | Too broad — catches marker traits |
| C | Derivation marker | Overloads derivation semantics |
| D | Structural inference from factory + elements + derivation | Preferred |

> **Dev:** I have reasons for preferring option D. Infer what my reasons are.

**Draco's reasons for Option D:**
1. Structure tells you what something is — consistent with isDomain/isRule/isActor inference
2. No new fields on TypeDefinition — keeps the type lean
3. The inference isn't fragile — it's correct: if you add elements later, you NEED a trait, and the change should propagate

### Factory Without a Trait

> **Dev:** A custom factory does not make sense with no trait at all.

This clarified the design: factory creates instances of the trait. No trait → no factory. They're coupled. CLI gave up the initialization parameter.

### Final Predicate: isObjectOnly

A type is object-only when:
- No elements (nothing abstract)
- No factory (no instances)
- Empty derivation (not part of a hierarchy)
- Has globalElements (the object has behavior)

### Object Extends DracoType

> **Dev:** What about having the object just extend DracoType, just to keep it minimally in the type system?

DracoType is the universal root — "I carry a typeDefinition, I'm self-describing." TypeInstance adds companion registration, which object-only types don't need. The object IS the instance:

```scala
object CLI extends DracoType {
  lazy val typeDefinition: TypeDefinition = ...
  lazy val typeInstance: DracoType = this
  def main(args: Array[String]): Unit = println("Draco 2.0.0-alpha.1")
}
```

---

## Design Decision: hasExplicitMain Convention

A Dynamic globalElement named `"main"` signals the Generator to omit `App` from the companion header. Applied to all three companion generation methods (typeGlobal, domainGlobal, ruleGlobal). Combined with isObjectOnly for entry points like CLI.

CLI.json:

```json
{
  "typeName" : { "name" : "CLI", "namePackage" : ["draco"] },
  "globalElements" : [
    { "kind" : "Dynamic", "name" : "main", "valueType" : "Unit",
      "parameters" : [{"kind" : "Parameter", "name" : "args", "valueType" : "Array[String]"}],
      "body" : [{"kind" : "Monadic", "value" : "println(\"Draco 2.0.0-alpha.1\")"}] }
  ]
}
```

---

## Build and Release

### build.sbt Changes
- Version: `1.1.1-SNAPSHOT` → `2.0.0-alpha.1`
- mainClass: `org.mitre.anvil.rules.DataDictionaryMap` → `draco.CLI`
- Added sbt-assembly config (merge strategy for META-INF, reference.conf, services)

### plugins.sbt
- Added `sbt-assembly` 2.1.5

### .gitignore
- Added `project/project/`, `project/target/`, `src/generated/`, `.draco/`

### GitHub Actions Workflow

`.github/workflows/release.yml` — triggers on `v*` tag push:
1. Checkout + JDK 17 setup
2. `sbt test`
3. `sbt assembly`
4. Create GitHub Release with JAR attached
5. Auto-marks as prerelease for alpha/beta tags

### Release Process

Terminal auth for tag push was not configured (GitHub requires PAT for HTTPS). The tag was created locally, and the release was published manually through the GitHub web UI:

- Tag: `v2.0.0-alpha.1`
- Release: pre-release, JAR uploaded manually
- URL: `https://github.com/ejb816/nexonix/releases/tag/v2.0.0-alpha.1`

---

## Generator Changes Summary

| Addition | Purpose |
|----------|---------|
| `hasExplicitMain(globalElements)` | Detects Dynamic globalElement named "main" |
| `isObjectOnly(td)` | No elements, no factory, empty derivation, has globalElements |
| `objectGlobal(td)` | Emits `object X extends DracoType` with `typeInstance = this` |
| `appMixin` in typeGlobal/domainGlobal/ruleGlobal | Omits `App` when hasExplicitMain is true |

---

## Draco Domain Updated

`Draco.scala` elementTypeNames updated to include `CLI` and `REPL`.

---

## Session Summary

1. **Bug fix** — `override val` → `override lazy val` in all 8 domain anonymous classes; stale paths in TestValue
2. **REPL[L]** — language-parameterized evaluator, peer of Generator[L], stub for alpha
3. **CLI** — object-only entry point, extends DracoType, globalElement main
4. **Object-only types** — structural inference (isObjectOnly), Generator emits object extending DracoType
5. **hasExplicitMain** — Dynamic globalElement "main" suppresses App in companions
6. **Alpha release** — v2.0.0-alpha.1 published to GitHub with fat JAR
7. **Getting Started guide** — identified as needed for others to understand the framework; deferred
