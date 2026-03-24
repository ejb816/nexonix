# Draco Dev Journal — Chapter 5

**Session date:** March 23, 2026 (continued)
**Topic:** Generator Consistency Updates, DelayedInit Bug Hunt, RuleDefinition Decoder Fix, Rule Import Generation, README.md

---

## Session Start — Generator Consistency Plan

> **Dev:** Implement the following plan:
> # Plan: Generator Consistency Updates
> *(A structured plan addressing three inconsistencies: factoryBody ignoring Factory.body, nullInstance using null-cast instead of factory apply, and missing DomainInstance companion generation)*

**Draco:** *(Reads Generator.scala in full. Launches an Explore agent to find the definitions of Factory, DomainName, DomainInstance, and BodyElement across the codebase. Reads Domain.scala and DomainInstance.scala for the apply pattern. Reads Primes.scala for the target DomainInstance pattern.)*

Implementation proceeds as four tracked tasks:

### Task 1: Refactor factoryBody to Honor Factory.body

Changed signature from `factoryBody(parameters: Seq[Parameter])` to `factoryBody(factory: Factory)`.

When `factory.body` is non-empty, each BodyElement becomes an override line directly:

```scala
if (factory.body.nonEmpty) {
  val overrides = factory.body.map {
    case f: Fixed   => s"    override val ${f.name}: ${f.valueType} = ${f.value}"
    case m: Mutable => s"    override var ${m.name}: ${m.valueType} = ${m.value}"
    case d: Dynamic => s"    override def ${d.name}: ${d.valueType} = ${d.value}"
    case be: BodyElement => s"    override val ${be.name}: ${be.valueType} = ${be.value}"
  }
```

When empty, falls back to parameter-derived behavior with the automatic `typeDefinition` override.

### Task 2: Add nullValueFor Helper

New private method placed before `nullInstance`:

```scala
private def nullValueFor(valueType: String, defaultValue: String): String = {
  if (defaultValue.nonEmpty) defaultValue
  else valueType match {
    case "String"                   => "\"\""
    case s if s.startsWith("Seq[")  => "Seq.empty"
    case s if s.startsWith("Map[")  => "Map.empty"
    case "Int" | "Long"             => "0"
    case "Double" | "Float"         => "0.0"
    case "Boolean"                  => "false"
    case _                          => s"null.asInstanceOf[$valueType]"
  }
}
```

### Task 3: Refactor nullInstance to Use Factory Apply

Changed signature to `nullInstance(typeName, elements, factory)`. When a factory exists (`valueType.nonEmpty`), generates Null via `apply()` with null-equivalent args from `nullValueFor`. Falls back to null-cast only when no factory is present.

### Task 4: Add DomainInstance Companion Generation

Three new methods:

- **`domainInstanceLiteral(objName, dn)`** — produces the `Domain[X](_domainName = DomainName(...))` call
- **`domainGlobal(td, dn)`** — produces the full DomainInstance companion object
- **`generate(td, dn)`** — new overload producing trait + DomainInstance companion

Target output matches the hand-written Primes pattern.

---

## The Test Failure Cascade

> **Dev:** You have done a great job on the more recent, comprehensive changes to the type system. However, I just ran all the tests in sbt and 7 out of 32 failed. I have the complete output of the tests in the sbt shell window. What is the best way to proceed with that information to work with you to fix the errors that are causing the 7 test failures?

**Draco:** The most effective approach: copy the failing test output from your sbt shell and paste it here. Specifically, I need the failure summaries and stack traces.

> **Dev:** *(Pastes complete test output showing 7 failures across 3 test classes)*

**Draco:** Two distinct error patterns:

**Pattern 1 (5 failures):** `NullPointerException: Cannot invoke "io.circe.Decoder.apply(io.circe.HCursor)" because "d" is null` — in TestValue, TupleFactReteTest, and three PrimesRulesTest "Generate" tests. The decoder itself is null when `jsonContent.as[RuleDefinition]` is called.

**Pattern 2 (2 failures):** `NoSuchElementException: last of empty list` — in PrimesRulesTest "PrimesFromNaturalSequence.rule" and "AddAndRemoveRulesTest". Rules never fired, `compositeSet` was empty.

*(Reads all three test files and all codec declarations across the codebase)*

### The DelayedInit Root Cause

`RuleDefinition` and `Value` companion objects extend `App` but have `implicit val encoder/decoder` — not `lazy val`. Due to `DelayedInit`, these are null when accessed from test contexts before `main()` runs.

Other types already use `lazy`:
- TypeDefinition.scala — `lazy implicit val` ✓
- TypeName.scala — `lazy implicit val` ✓
- DomainName.scala — `lazy implicit val` ✓
- TypeElement.scala — `implicit lazy val` ✓

Fix: Changed both files to `implicit lazy val`:

```scala
// RuleDefinition.scala
implicit lazy val encoder: io.circe.Encoder[RuleDefinition] = ...
implicit lazy val decoder: io.circe.Decoder[RuleDefinition] = ...

// Value.scala
implicit lazy val encoder: Encoder[Value] = ...
implicit lazy val decoder: Decoder[Value] = ...
```

### The Missing .accept(knowledge) Fix

For Pattern 2, the tests accessed `ruleInstance.pattern` (a `Consumer[Knowledge]`) without calling `.accept(knowledge)`, so rules were never registered:

```scala
// Before:
rules.PrimesFromNaturalSequence.ruleInstance.pattern
// After:
rules.PrimesFromNaturalSequence.ruleInstance.pattern.accept(knowledge)
```

Same fix for `AddNaturalSequence` and `RemoveCompositeNumbers`.

---

## Layer Two — Private Val Null

> **Dev:** *(Pastes new error)*
> ```
> Cannot invoke "java.util.function.Consumer.accept(Object)" because the return value of
> "draco.RuleType.pattern()" is null
> ```

**Draco:** Same `DelayedInit` problem, one level deeper. `PrimesFromNaturalSequence extends App` has `private val pattern` and `private val action` — these are null when `lazy val ruleInstance` tries to reference them. They need to be `private lazy val`.

Fixed all three rule files:

```scala
// Before:
private val ruleDefinition : RuleDefinition = ...
private val action: Consumer[RhsContext] = ...
private val pattern: Consumer[Knowledge] = ...

// After:
private lazy val ruleDefinition : RuleDefinition = ...
private lazy val action: Consumer[RhsContext] = ...
private lazy val pattern: Consumer[Knowledge] = ...
```

Also updated the Generator template so future generated rules emit `private lazy val`.

Updated the memory note — the old guidance said "Private vals used only within same body → fine as `val`" was wrong. Corrected to: "Private vals referenced by lazy vals → `private lazy val` (DelayedInit makes them null when lazy val triggers)."

---

## Layer Three — The RuleDefinition Decoder Bug

> **Dev:** *(Pastes new error)*
> ```
> Cannot invoke "draco.RuleDefinition.typeName()" because "rd" is null
> ```

**Draco:** The decoder was previously null, so we never saw the actual decode error. Now it runs but fails. `.getOrElse(null)` produces null because the decode returns `Left`.

Traced through the RuleDefinition decoder:

```scala
_pattern  <- cursor.downField("action").as[Option[Pattern]].map(_.getOrElse(Pattern.Null))
_action   <- cursor.downField("action").as[Option[Action]].map(_.getOrElse(Action.Null))
```

Line 77: `_pattern` reads from `cursor.downField("action")` — the same field as `_action` on line 78. The JSON "action" field has `"kind": "Action"`, so decoding it as `Pattern` fails (`Codec.sub` emap rejects it: "Expected Pattern, got Action"). This `Left` propagates through the for-comprehension.

This was a pre-existing bug masked by the null decoder — the decode never ran far enough to hit this field-name error.

Fix: `cursor.downField("action")` → `cursor.downField("pattern")` for the `_pattern` field. Since the JSON has no `"pattern"` field, `as[Option[Pattern]]` returns `None`, giving `Pattern.Null`.

---

## Layer Four — The Test Overwrites the Source

> **Dev:** I hope you have a sense of humor... PrimeRulesTest actually overwrites the existing source code implementation with the rule source code generated by the test. The good news? It can be fixed by the Generator generating "import draco._" in the generated rule source code. You might as well add imports for circe and evrete while you're at it.

**Draco:** *(Adds `ruleImports` method to Generator)*

```scala
private def ruleImports(namePackage: Seq[String]): String = {
  val packageImports = namePackage.inits.toSeq.tail.init.map(p => s"import ${p.mkString(".")}._")
  val allPackageImports = if (packageImports.contains("import draco._")) packageImports
    else "import draco._" +: packageImports
  val frameworkImports = Seq(
    "import io.circe.{Json, parser}",
    "import org.evrete.api.{Knowledge, RhsContext}",
    "import java.util.function.Consumer"
  )
  (allPackageImports ++ frameworkImports).mkString("\n")
}
```

For `draco.primes.rules`, this generates `import draco.primes._`, `import draco._`, plus framework imports. For non-draco packages (like TupleFactRule's `org.nexonix.rules.rete.rules`), `import draco._` is prepended since it's not naturally in the package hierarchy.

Added the `ruleImports` call to `generate(rd: RuleDefinition)` between the package declaration and trait declaration.

---

## Writing the README

> **Dev:** Can you write a comprehensive README.md for a person who knows nothing about this software framework? Include the type system and its extensions into domains, rules and actors, as well as what can reasonably be inferred from the fundamental concepts and how they can be applied to create data transformation services that preserve semantics within and between domains. Summarize the features that the tests have shown to work, and the features that still need to be worked on. This is just a first draft, based on any information you can retrieve from the persistent storage you have maintained for this project.

**Draco:** *(Reads all memory files, CLAUDE.md, CHANGELOG.md. Launches an Explore agent to do a thorough read of: the core type hierarchy, domain system, rule system, actor system, content pipeline, base domain, transform domains, primes domain, and test files — 54 tool uses across 9 major areas)*

The README covers:

- **Core idea** — why self-describing types matter and how the type system is closed over itself
- **Type system** — the full hierarchy from DracoType through TypeInstance, TypeDefinition, and the TypeElement sealed family
- **Domains** — DomainName, Domain[T], DomainInstance, TypeDictionary, peer domain architecture
- **Rules** — JSON-defined RuleDefinition, RETE integration, condition functions with fully-qualified names for Evrete's Java compiler, example JSON rule
- **Actors** — the ActorBehavior → Service → ServiceDomain stack
- **Code generation** — all three modes and the content pipeline
- **Base domain** — measurement type hierarchy and compositional coordinate design
- **Semantic preservation** — how types carry meaning, domains define vocabularies, rules preserve invariants, and transforms are first-class
- **Working vs. in-progress features** — grounded in what the tests exercise
- **Build instructions, dependencies, project structure**

---

## Documentation Updates for Next Session

> **Dev:** I'm expecting to start a new session for the next phase, so please add inline updates to Claude.md as well as adding references to all the other documentation in the project that you have been updating.

**Draco:** Updated all three files:

**CLAUDE.md** — Rewrote the architecture section from scratch. Removed stale references to `DomainElement`, `TypeValue`, `preamble`, the conditions migration note. Added complete key files table, TypeElement hierarchy, companion object pattern, DelayedInit/lazy val rules, Generator details for all four `generate` overloads, and a "Related Documentation" section linking to README.md, CHANGELOG.md, and auto-memory.

**CHANGELOG.md** — Added new `[Unreleased]` section with all session changes: DomainInstance generation, nullValueFor, rule imports, factoryBody/nullInstance refactors, lazy val fixes, RuleDefinition decoder fix, test fixes.

**MEMORY.md** — Marked completed Generator tasks (consistency updates, rule imports, framework imports). Added "Bug Fixes Applied This Session" section. Updated imminent task list with completed items struck through and new task #6 added.

---

## Session Summary

Major accomplishments this session:

1. **Generator consistency updates** — factoryBody honors Factory.body, nullInstance uses factory apply() with type-appropriate defaults, DomainInstance companion generation added matching the Primes pattern.
2. **Four-layer bug hunt** — Each fix revealed the next layer:
   - Layer 1: `implicit val` → `implicit lazy val` for RuleDefinition/Value codecs (DelayedInit null)
   - Layer 2: `private val` → `private lazy val` for rule action/pattern/ruleDefinition (DelayedInit null through lazy val reference)
   - Layer 3: RuleDefinition decoder reading `_pattern` from `"action"` field instead of `"pattern"` (pre-existing bug masked by null decoder)
   - Layer 4: Generated rule source missing imports (tests overwrite source files with Generator output)
3. **Rule import generation** — `ruleImports(namePackage)` auto-generates package hierarchy imports + `import draco._` + framework imports for Circe, Evrete, Consumer.
4. **Comprehensive README.md** — Full project documentation for newcomers, covering architecture, semantic preservation, working features, and roadmap.
5. **Documentation refresh** — CLAUDE.md rewritten with current architecture, CHANGELOG.md updated, MEMORY.md updated with completed tasks and bug fix record.
6. **Memory note corrected** — The lazy val rule for private vals was wrong ("fine as val"); corrected to require `private lazy val` when referenced by lazy vals.
