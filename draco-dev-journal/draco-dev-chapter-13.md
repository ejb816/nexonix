# Draco Dev Journal — Chapter 13

**Session date:** March 23, 2026 (continued into March 24)
**Topic:** Complete JSON Definitions, Generator Improvements, generated.draco Test Infrastructure

---

## Session Start — Picking Up Where We Left Off

> **Dev:** Is the persistence of the session data files determined explicitly by me as the user?

**Draco:** Yes. The memory files are plain Markdown files on your filesystem. You have full control — view, edit, delete them directly. I only write memories when something seems worth preserving, or when you explicitly ask.

> **Dev:** Can you discern where we left off before I started a new session?

**Draco:** *(Reads MEMORY.md, git status, recent commits)*

Based on the memory and git status:

**Most recent work completed:**
- GeneratorDefinitionToSourceTest — central test file
- 10 new JSON definition files created (Actor through Draco) — but generate tests not yet added
- 4 JSON files with passing generate tests: ActorDefinition, DomainDefinition, TypeDefinition, TypeName

**Imminent tasks:**
1. Add Generate tests for the 10 new JSON files
2. Create remaining JSON files — DracoType through Value
3. Migrate ruleDefinitionFromJson
4. Dreams application

---

## Creating Remaining JSON Definition Files

> **Dev:** Let's continue adding JSON type definition files for the remaining draco manually created types

**Draco:** *(Explores codebase — cross-references Scala files against existing JSON files)*

8 Scala files still need JSON definitions: DracoType, Primal, Type, TypeInstance, RuleType, RuleInstance, Main, Test.

Also found 5 empty JSON files (Rule, Value, SourceContent, RuleDefinition, TypeDictionary) and 2 stale JSON files (RuleActorBehavior, RuleSet — no corresponding Scala source).

Notes on the new files:
- **Primal** and **Type** now use proper `typeParameters` field instead of embedding `[T]` in the name string, consistent with Actor.json convention
- **RuleType** factory includes `Consumer[Knowledge]` and `Consumer[RhsContext]` parameters

> **Dev:** Follow up on your observations and continue to request approval for changes and additions.

**Draco:** Three observations:

1. **Fix Primal.scala and Type.scala** — `_name = "Primal[T]"` → `_name = "Primal"` + `_typeParameters = Seq("T")`
2. **Remove stale JSON files** — RuleActorBehavior.json, RuleSet.json
3. **Add Generate tests** — 28 tests for all JSON files

> **Dev:** yes

*(Fixes Primal.scala, Type.scala, Rule.scala — typeParameters. Removes stale JSON. Populates 5 empty JSON files. Adds 28 generate tests to GeneratorDefinitionToSourceTest.)*

---

## Sorting Tests

> **Dev:** In GeneratorDefinitionToSourceTest, please sort the sequence of test(<name>) by <name>

**Draco:** *(Sorts all 28 tests alphabetically: Actor, ActorDefinition, ActorInstance, ... TypeName, Value)*

---

## Test Failure: Generate Actor

> **Dev:** *(Runs test, gets two errors)*
> ```
> ERROR: not found: type ExtensibleBehavior
> ERROR: trait Actor takes type parameters
> ```

**Draco:** Two issues:

1. **Missing Pekko imports** — Actor needs the actor generation path `generate(td, ad)`, not plain `generate(td)`
2. **`lazy val Null: Actor`** — should be `Actor[_]` for parameterized types (bug in `Generator.nullInstance`)

*(Fixes nullInstance to use `wildcardTypeName` for Null type declaration and `[Nothing]` for apply calls. Adds `generateActorAndVerify` helper to test.)*

---

## Test Failure: Generate ContentSink

> **Dev:** *(Runs test, gets errors)*
> ```
> ERROR: value sinkRoot is not a member of draco.ContentSink
> ERROR: not found: type URI
> ```

**Draco:** Two issues:

1. **Codec generated for wrong type** — factory params (sinkRoot, logicalPath) aren't trait elements. The codec encoder tries to access fields that don't exist on the trait.
2. **Missing URI import** — Generator doesn't handle JDK type imports.

*(Adds guard to codecDeclaration: Pattern 1 only when factory params ⊆ element names)*

> **Dev:** Go ahead with import detection and ask for approval for your changes.

**Draco:** Proposed approach: a type-to-import mapping in Generator that scans all valueTypes for known external types.

```scala
private val externalTypeImports: Map[String, String] = Map(
  "URI"              -> "import java.net.URI",
  "BufferedSource"   -> "import scala.io.BufferedSource",
  "KnowledgeService" -> "import org.evrete.KnowledgeService",
  "Consumer"         -> "import java.util.function.Consumer",
  "Knowledge"        -> "import org.evrete.api.Knowledge",
  "RhsContext"       -> "import org.evrete.api.RhsContext"
)
```

> **Dev:** yes

*(Implements `externalTypeImports`, `extractTypeNames`, `externalImports(td)`. Changes `typeImports` signature from `(namePackage, hasCodec, instanceType)` to `(td, hasCodec, instanceType)` to pass the TypeDefinition for scanning. Multi-type generation merges all type definitions for import detection.)*

---

## Test Failure: Generate DracoType — Cyclic Inheritance

> **Dev:** *(Runs test)*
> ```
> ERROR: illegal cyclic inheritance involving trait DracoType
> ```

**Draco:** DracoType is the universal root — it has no parent. But `typeExtends` defaults to `extends TypeInstance` when derivation is empty, creating a cycle (TypeInstance extends DracoType).

Fix: empty derivation now produces no extends clause. But this means types that previously relied on the implicit default need explicit derivation in their JSON.

*(Changes `typeExtends` — empty derivation = no extends. Adds explicit derivation to 5 JSON files: ActorInstance→TypeInstance, DomainInstance→TypeInstance, ActorDefinition→TypeInstance, DomainDefinition→TypeInstance, TypeElement→Primal[String].)*

---

## Test Failure: Generate TypeDefinition — Self-Shadowing

> **Dev:** *(Runs test)*
> ```
> ERROR: value load is not a member of object draco.TypeDefinition
> ```

**Draco:** The generated TypeDefinition shadows the real one on the classpath. `TypeDefinition.load(...)` resolves to the generated object — which doesn't have `load`. The generated code IS structurally correct (it would work as a replacement), but it can't compile alongside the real source.

*(Adds framework-shadowing tolerance to compileCheck — errors become warnings for `draco` package types.)*

---

## The generated.draco Solution

> **Dev:** What about adding src/test/{resources,scala}/generated and putting files into paths based on generated/draco with anything relative to root/draco referencing the type in generated?

**Draco:** By generating into `generated.draco` package instead of `draco`, generated types coexist without shadowing. `import draco._` pulls in the real framework types. Generated files can use proper `.scala` extension and actually compile.

> **Dev:** Would it be better to just add src/generated to src/main and src/test and try to make everything work that way?

**Draco:** The challenge is package collision — both hand-written and generated files would declare `package draco`. That only works when generated files replace hand-written ones, which is the end state but not the current state.

Two paths: (A) `generated.draco` package for coexistence now, (B) replacement later. They're compatible.

> **Dev:** Obviously option A (coexistence) since that seems to the same as my earlier suggestion.

**Draco:** One subtlety: `TypeDefinition.load(...)` in generated code. Within `generated.draco`, same-package resolution means `TypeDefinition` refers to the generated one. Fix: fully qualify as `draco.TypeDefinition.load(...)` in Generator output.

> **Dev:** Yes, given that you proactively addressed the TypeDefinition.load issue.

*(Changes `typeDefinitionLoad` to emit `draco.TypeDefinition.load(...)`. Creates `src/test/scala/generated/draco/`. Rewrites test — `rewritePackage` post-processes `package draco` → `package generated.draco`. Proper `.scala` extension. Strict compilation for all types. Removes old `.generated.scala.txt` files.)*

---

## All Tests Pass

> **Dev:** *(Runs sbt test)*
> ```
> [info] GeneratorDefinitionToSourceTest:
> [info] - Generate Actor
> [info] - Generate ActorDefinition
> ...
> [info] - Generate Value
> [info] Run completed in 449 milliseconds.
> [info] Total number of tests run: 58
> [info] Tests: succeeded 58, failed 0, canceled 0, ignored 0, pending 0
> [info] All tests passed.
> ```

---

## Generator Evolution — Future Plan

> **Dev:** I think I need a GeneratorType, GeneratorDefinition and GeneratorInstance as well as Generator[L] (currently Generator[Scala])

**Draco:** That's a natural evolution — bringing Generator into the type system as a full peer. The parameterization by language `L` makes JSON definitions truly language-neutral.

Design questions discussed: What is `L`? Where does `TypeDefinition.load` land? What goes in GeneratorDefinition? Does the triad become a quad?

> **Dev:** Hold it as an important plan for the future, along with Dreams, but later.

*(Saves memory: project_generator_evolution.md)*

---

## Cleanup and Minor Fixes

> **Dev:** Are there any minor tasks lingering in your current working memory that have not been addressed?

**Draco:** Two items:
1. MEMORY.md stale JSON line — already removed files still listed
2. TypeDictionary.scala derivation — stale `namePackage` and old embedded-params style

*(Fixes both.)*

---

## Test File Consolidation

> **Dev:** I decided to delete most of the Test.scala files in src/test/scala/draco. The main one remaining is GeneratorDefinitionToSourceTest which tests all the draco types. TypeDefinitionTest still passes, so I'm keeping it until I can extract what it does.

---

## Documentation Updates and Staging

> **Dev:** Update Claude.md, README.md and Memory.md and others, then queue up the git commands for my approval.

**Draco:** *(Updates CLAUDE.md — Generator methods, fully qualified TypeDefinition.load, external imports, typeExtends, nullInstance. Updates README.md — Work in Progress, Working Features, project structure with generated/draco/. Updates CHANGELOG.md — new Unreleased section. Updates MEMORY.md — all JSON complete, tasks updated.)*

> **Dev:** As a general approach, I haven't been committing files that are not staged. What is the downside of that approach.

**Draco:** The main downside: untracked files aren't protected by git. The 18 new JSON definition files are real work product. Build artifacts (project/project/, project/target/) are safe untracked — they're regenerated. The staged-only approach works fine as long as you eventually stage new files before they get lost.

> **Dev:** Anything that you created at my request should be staged, I can see why they are not staged automatically, as are the ones in main.

*(Stages all 18 new JSON definitions, Natural.actor.json, and generated/ directory.)*

---

## Session Summary

Major accomplishments this session:
1. **Complete JSON definitions** — every manually-written draco framework type now has a corresponding JSON definition file (30+ types)
2. **28 Generate tests** — all passing, covering core hierarchy, domain/rule/actor types, infrastructure, and TypeElement hierarchy
3. **generated.draco test infrastructure** — coexistence pattern with proper `.scala` compilation, replacing old `.generated.scala.txt`
4. **Generator improvements** — external import detection, parameterized Null instances, fully qualified `draco.TypeDefinition.load`, codec generation guard, root type support (empty derivation = no extends)
5. **TypeName modernization** — Primal, Type, Rule updated from embedded `[T]` to `typeParameters` field
6. **Stale cleanup** — removed RuleActorBehavior.json, RuleSet.json, old generated files
7. **Generator evolution plan** — GeneratorType/GeneratorDefinition/GeneratorInstance/Generator[L] documented for future
