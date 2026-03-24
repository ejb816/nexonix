# Draco Dev Journal — Chapter 3

**Session date:** March 23, 2026 (continued)
**Topic:** Multi-Type File Generation, JSON Embedding Plan Design

---

## Session Start — Multi-Type File Generation

> **Dev:** Implement the following plan:
> # Plan: Multi-Type File Generation from Module References
> *(A plan to extract `traitDeclaration` from `generate(td)`, add `moduleOrder` for topological sorting, and add a new `generate(typeDefinitions: Seq[TypeDefinition])` overload that produces multiple types in one file — driven by the `modules` field. Plus two tests in TypeDefinitionTest.)*

**Draco:** *(Reads Generator.scala (509 lines), TypeDefinitionTest.scala, TypeElement.scala, and TypeDefinition.scala in parallel to understand the current code before making changes)*

Implementation in Generator.scala:

1. **Extracted `traitDeclaration` helper** — refactored from the body of `generate(td)`:

```scala
private def traitDeclaration (td: TypeDefinition) : String =
  s"${typeModifier(td.modules)}trait ${td.typeName.name} ${typeExtends(td.derivation)} ${typeBody(td.elements)}"
```

2. **Added `moduleOrder` for topological sorting** — walks the module tree depth-first from roots. Roots = types not referenced as a module by any other type in the set:

```scala
private def moduleOrder (tds: Seq[TypeDefinition]) : Seq[TypeDefinition] = {
  val byName = tds.map(td => baseName(td.typeName.name) -> td).toMap
  val ordered = scala.collection.mutable.LinkedHashSet[String]()
  def walk (td: TypeDefinition) : Unit = {
    val name = baseName(td.typeName.name)
    if (!ordered.contains(name)) {
      ordered += name
      td.modules.flatMap(tn => byName.get(baseName(tn.name))).foreach(walk)
    }
  }
  val referencedAsModule = tds.flatMap(_.modules.map(tn => baseName(tn.name))).toSet
  tds.filterNot(td => referencedAsModule.contains(baseName(td.typeName.name))).foreach(walk)
  tds.foreach(walk) // safety net for unreachable types
  ordered.toSeq.map(byName)
}
```

3. **Added `generate(typeDefinitions: Seq[TypeDefinition])` overload** — delegates to single-type `generate` for size ≤ 1, otherwise sorts via `moduleOrder`, emits one `package` line followed by all trait declarations + companion objects:

```scala
def generate (typeDefinitions: Seq[TypeDefinition]) : String = {
  if (typeDefinitions.isEmpty) return ""
  if (typeDefinitions.size == 1) return generate(typeDefinitions.head)
  val ordered = moduleOrder(typeDefinitions)
  val pkg = ordered.head.typeName.namePackage.mkString(".")
  val typeBlocks = ordered.map { td =>
    s"${traitDeclaration(td)}\n\n${typeGlobal(td)}"
  }
  s"\npackage $pkg\n\n${typeBlocks.mkString("\n\n")}\n"
}
```

4. **Refactored `generate(td)`** to use `traitDeclaration` — output unchanged.

Two tests added to TypeDefinitionTest.scala:

- **"Multi-type generation: TypeElement hierarchy"** — collects all 12 TypeDefinitions from the existing companion objects (`TypeElement`, `BodyElement`, `Fixed`, `Mutable`, `Dynamic`, `Parameter`, `Monadic`, `Pattern`, `Action`, `Condition`, `Variable`, `Factory`), calls `Generator.generate(types)`, asserts ordering and `sealed` keywords.

- **"Multi-type generation: simple parent-child (Animal/Dog)"** — two synthetic TypeDefinitions passed in reversed order; asserts Animal appears before Dog, Dog has `apply`, single package declaration.

---

## Test Failure — `sealed trait Fixed`

> **Dev:** `sbt "testOnly draco.TypeDefinitionTest"`

Results: 5 passed, 1 failed:

```
[info] - Multi-type generation: TypeElement hierarchy *** FAILED ***
[info]   742 was not less than -1 BodyElement should appear before Fixed (TypeDefinitionTest.scala:116)
```

**Draco:** The assertion searched for `"sealed trait Fixed"` but `Fixed` has no modules — so the Generator correctly produces `trait Fixed` (not sealed). The `indexOf("sealed trait Fixed")` returned -1, failing the comparison. Fixed the assertion to search for `"trait Fixed"` instead.

> **Dev:** *(Reruns tests)*

All 6 tests pass.

---

## Where Did the TypeDefinitions Come From?

> **Dev:** Where did you get the JSON type definitions for the modules referenced by TypeElement?

**Draco:** From the Scala companion objects, not from JSON. The test does:

```scala
val types: Seq[TypeDefinition] = Seq(
  TypeElement.typeDefinition,
  BodyElement.typeDefinition,
  Fixed.typeDefinition,
  ...
)
```

Each of those is the `lazy val typeDefinition` defined in the companion object in `TypeElement.scala`. The plan said "Collects all 12 TypeDefinitions from the existing companion objects" — so the caller provides the full set directly. Loading them from JSON resource files was explicitly listed as out of scope.

> **Dev:** Can you show me the JSON format data that the Generator would use to generate this TypeElement.scala?

**Draco:** *(Added a test "TypeElement hierarchy as JSON" that encodes all 12 TypeDefinitions via `.asJson.spaces2` and prints them. Attempted to run it, but Dev rejected the sbt invocation.)*

---

## Proposal: Replace Literal Composition with Embedded JSON

> **Dev:** Let's restructure the way definition data is embedded in the generated source code, which will also affect the Generator source code. Right now we're embedding TypeDefinition by composing the factory methods of the TypeDefinition, TypeElement and its subtypes. Instead of that I propose the JSON string definition be a string literal in an expression that parses that string into circe Json, which is the argument of the TypeDefinition decoder that converts Json object into the TypeDefinition instance.

**Draco:** That's a significant simplification — replacing the composed factory-call literals with an embedded JSON string that gets parsed and decoded at runtime. This would eliminate `typeDefinitionLiteral`, `elementLiteral`, `factoryLiteral`, and `ruleDefinitionLiteral` entirely.

*(Enters plan mode. Launches an Explore agent to thoroughly map the current literal generation pipeline — all five private methods, their call sites in `typeGlobal` and `generate(rd)`, the TypeDefinition/RuleDefinition/TypeName encoder/decoder round-trip status, and existing test coverage.)*

### Key Findings from Exploration

1. **Five literal methods form a composition chain** — `typeDefinitionLiteral` calls `typeNameLiteral`, `elementLiteral`, and `factoryLiteral`. `ruleDefinitionLiteral` calls `typeNameLiteral` and `elementLiteral`. All five are only used within these two top-level literals.

2. **TypeDefinition encoder/decoder round-trips correctly** — all fields preserved (empty fields elided on encode, defaulted on decode).

3. **RuleDefinition encoder/decoder has a pre-existing bug** — the decoder reads the `"action"` field twice (once as Pattern, once as Action), meaning pattern data is lost during round-trip. Out of scope for this change.

4. **TypeName encoder/decoder round-trips correctly** — derived fields (`fullName`, `resourcePath`) recomputed at construction.

5. **No existing round-trip test** — the "TypeDefinition as JSON" test only encodes, doesn't decode back.

---

## Scope Question

> **Draco:** Should the RuleDefinition literal also be replaced with the same JSON-embed approach, or just TypeDefinition for now?

> **Dev:** Both TypeDefinition and RuleDefinition.

*(Reads RuleDefinition.scala and a generated rule file (AddNaturalSequence.scala) to understand both sides.)*

---

## The Plan

**Draco:** *(Designs and writes the plan)*

### Changes to Generator.scala

1. **Add import** — `import io.circe.syntax.EncoderOps` (needed for `.asJson`)

2. **Add two JSON-embedding helpers:**

```scala
private def typeDefinitionFromJson (td: TypeDefinition) : String = {
  val json = td.asJson.spaces2
  s"""io.circe.parser.parse(\"\"\"$json\"\"\").flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)"""
}

private def ruleDefinitionFromJson (rd: RuleDefinition) : String = {
  val json = rd.asJson.spaces2
  s"""io.circe.parser.parse(\"\"\"$json\"\"\").flatMap(_.as[RuleDefinition]).getOrElse(RuleDefinition.Null)"""
}
```

Two-step: `io.circe.parser.parse` (string → Json), then `.as[T]` (Json → T via decoder).

3. **Replace usages:**
   - `typeGlobal`: `typeDefinitionLiteral(td)` → `typeDefinitionFromJson(td)`
   - `generate(rd)`: `ruleDefinitionLiteral(rd)` → `ruleDefinitionFromJson(rd)`

4. **Delete five obsolete private methods:** `typeNameLiteral`, `elementLiteral`, `factoryLiteral`, `typeDefinitionLiteral`, `ruleDefinitionLiteral`

*(Plan was written but Dev had not yet approved it for implementation when the session concluded.)*

---

## Dev Journal Instruction

> **Dev:** Read draco-dev-journal/draco-dev-chapter-1.md for the format. Transcribe this session as the next chapter. Use the other chapter for format and structure information only. The content itself should come exclusively from this session.

**Draco:** *(Reads chapter 14 for format conventions, checks existing chapters — finds chapters 4–14 exist. Writes this session as chapter 3.)*
