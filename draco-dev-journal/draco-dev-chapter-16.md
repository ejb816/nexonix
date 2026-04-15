# Draco Dev Journal — Chapter 16

**Session date:** March 24, 2026
**Session ID:** deae5697-59f4-4cdc-bf9b-e510e10a2801
**Topic:** Task 2 — Update All Domains to New Conventions (Flat Packages, Aspect Naming, JSON as Source of Truth)

---

## Session Start — Situation Assessment

> **Dev:** What is the situation as you see it, with regard to proceeding to the next priority task for the draco framework?

**Draco:** Based on the memory and git status, here's how I see it:

**Completed:** TypeDefinition unification (task 1) — DomainDefinition, RuleDefinition, and ActorDefinition have been dissolved into TypeDefinition. The git status shows the deleted files alongside the modified TypeDefinition.scala, confirming this is done.

**Next up: Task 2 — Update existing domains (Draco, Base, Primes) to new conventions.**

This means bringing the three core domains into alignment with the post-unification model where all definitions use TypeDefinition, the Extensible/Specifically[T] pattern is in place, companion objects follow current conventions (lazy vals, TypeInstance traits), and JSON definition files are canonical.

The key question: Are the working-tree modifications already partway through task 2, or are they residual from task 1 that hasn't been committed yet?

> **Dev:** Yes

*(Draco launches two exploration agents in parallel — one to read all framework domain files (Draco, Base, Primes and their types), one to read all test domain files (DataModel, Alpha, Bravo, Charlie, Delta). Both agents audit convention compliance.)*

---

## Assessment Results

The framework layer (core draco types) was found fully conformant — unified TypeDefinition, lazy vals correct, JSON definitions present and canonical, Extensible pattern in place.

The domain layer had several gaps:

| Issue | Severity |
|-------|----------|
| Base.json and Primes.json empty (0 bytes) | High |
| Accumulator.json and Numbers.json empty | High |
| Primes domain missing TypeDictionary | Medium |
| Draco.scala creates Type inline (non-standard pattern) | Medium |
| Test domain typeDefinitions missing derivation | Medium |
| Actor/rule types in sub-packages (actor/, rules/) instead of flat domain package | High |

---

## Design Decision: Should Example Domains Be Included?

> **Dev:** Should we add the example domains { DataModel, Alpha, Bravo, Charlie, Delta } to Task 2?

**Draco:** Yes. They're already partially updated, and they serve as the validation that the conventions actually work end-to-end. The natural ordering follows the dependency graph:

1. Draco, Base, Primes — framework's own domains first (foundation)
2. DataModel — the shared pipeline domain (bridge)
3. Alpha, Bravo, Charlie, Delta — the test domains that extend DataModel (consumers)

> **Dev:** Let's proceed with Task 2, keep me in the approval loop.

---

## Design Decision: Domain Discovery (No elementTypeNames in JSON)

> **Dev:** What about if in the JSON for a domain, we just had the domain as a type itself, and let the Generator populate the names where it needs to in the source generation based on the existence of valid type definitions for that domain's contained types?

**Draco:** That's a much better design. The domain doesn't *declare* its contents — it *discovers* them. A domain's JSON is just its type identity (typeName + derivation), the same as any other type. The Generator then scans the resource path for valid TypeDefinitions whose package places them inside that domain, and populates elementTypeNames in the generated source.

This means:
- JSON stays minimal — a domain is just a type, no special fields needed
- No redundant manifest — adding a new type means creating its JSON file in the right package
- Generator is the authority — it resolves domain contents structurally
- Self-description is emergent — the domain describes itself by the types that exist within it

Two clarification questions:
1. Recursive subdirectories — should `draco/primes/rules/*.json` types also count as elements?
2. Test domains already have minimal JSON — just need Generator discovery. Correct?

> **Dev:** Question 2 is correct. Question 1 points to (possibly) unfinished update work. The "actor" and "rule" were to be removed as part of a domain type package name and only be used as `<type name>.{rule,actor}.json` file name in resources, and generated as `<type name>{Rule,Actor}.scala`, to avoid unintentional name conflict in the domain type package if the same type name were to be used both as a type (in the domain) and actor type or rule type.

---

## Design Decision: Flat Packages with Aspect Naming

This clarified the convention:

**Resources (aspect naming):**
- `domains/bravo/Bravo.json` — domain type
- `domains/bravo/BravoResult.json` — element type
- `domains/bravo/BravoActor.actor.json` — actor aspect
- `domains/dataModel/AssembleResult.rule.json` — rule aspect

**Generated Scala (flat domain package):**
- All types in the domain's package — no `actor/` or `rules/` subdirectories
- Generator discovers all types by scanning a single directory

`Natural.actor.json` already followed this convention. The unfinished work was moving the rest.

---

## Phase 1: Flatten JSON Files

> **Dev:** Yes, very good. We can check for more after this is done.

*(Draco reads all rule/actor JSON and Scala files, tests, and references across the codebase, then presents the flattening plan in two phases — JSON moves + namePackage updates, then Scala moves + import/reference updates)*

> **Dev:** Yes

Six JSON files moved from subdirectories to flat structure with aspect naming, namePackage fields updated to remove "rules" and "actor" segments:

| From | To |
|------|-----|
| `draco/primes/rules/AddNaturalSequence.json` | `draco/primes/AddNaturalSequence.rule.json` |
| `draco/primes/rules/PrimesFromNaturalSequence.json` | `draco/primes/PrimesFromNaturalSequence.rule.json` |
| `draco/primes/rules/RemoveCompositeNumbers.json` | `draco/primes/RemoveCompositeNumbers.rule.json` |
| `test/.../dataModel/rules/AssembleResult.json` | `test/.../dataModel/AssembleResult.rule.json` |
| `test/.../bravo/BravoActor.json` | `test/.../bravo/BravoActor.actor.json` |
| `test/.../dataModel/DataModelActor.json` | `test/.../dataModel/DataModelActor.actor.json` |

Old subdirectories removed.

---

## Phase 2: Flatten Scala Sources

*(Draco presents the plan — 7 Scala files to move, all imports and FQCNs to update)*

> **Dev:** Yes

Seven Scala source files moved to flat domain packages:

| Old Package | New Package |
|-------------|-------------|
| `draco.primes.rules.AddNaturalSequence` | `draco.primes` |
| `draco.primes.rules.PrimesFromNaturalSequence` | `draco.primes` |
| `draco.primes.rules.RemoveCompositeNumbers` | `draco.primes` |
| `domains.dataModel.rules.AssembleResult` | `domains.dataModel` |
| `domains.dataModel.actor.DataModelActor` | `domains.dataModel` |
| `domains.bravo.actor.BravoActor` | `domains.bravo` |
| `domains.natural.actor.NaturalActor` | `domains.natural` |

All references updated across the codebase:
- Package declarations and imports in moved files
- `import domains.bravo.actor._` → removed (same package now) in `AlphaBravoTest.scala`
- `import domains.dataModel.actor._` → removed in `AlphaBravoTest.scala`
- `import domains.natural.actor._` → `import domains.natural._` in `NaturalActorTest.scala`
- `rules.PrimesFromNaturalSequence` → `PrimesFromNaturalSequence` in `PrimesRulesTest.scala`
- `.newRule("draco.primes.rules.X")` → `.newRule("draco.primes.X")` in all three rule files
- `.where("draco.primes.rules.X.w0(...")` → `.where("draco.primes.X.w0(...")` in condition-bearing rules
- Inline JSON `namePackage` in rule sources — removed "rules" segment
- Generate test resource paths — `draco/primes/rules/X.json` → `draco/primes/X.rule.json`
- Generate test sink paths — `draco/primes/rules/X.scala` → `draco/primes/X.scala`

Zero stale references remaining (verified via grep). Empty leftover directories (`actors/`, `rule/`) cleaned up.

---

## Phase 3: Populate Empty JSON Files

> **Dev:** yes

Four previously-empty JSON files populated:

**Base.json** — typeName + derivation (DomainInstance). Minimal domain identity.

**Primes.json** — typeName + derivation (DomainInstance). Same pattern.

**Accumulator.json** — typeName, derivation (Primes), 4 Mutable elements (primeSet, compositeSet, naturalSet, intervalTextSet), factory.

**Numbers.json** — typeName, derivation (Primes), 3 Fixed elements (primeSequence, naturalSequence, compositeSequence), factory with parameter `n: Int = 22`.

---

## Phase 4: Base Element JSON Definitions

> **Dev:** Let's do the Base elements and fix Unit

*(Draco reads all 9 Base element Scala sources, plus the empty unit/Unit.json)*

Nine JSON files created, all in the flat `draco/base/` directory:

| Type | Key Features |
|------|-------------|
| Unit | Root — elements (name, description), no derivation |
| Cardinal[T] | extends Unit + Primal[T], typeParameters: [T] |
| Coordinate[T] | extends Primal[T], typeParameters: [T <: Product] |
| Distance[T] | extends Cardinal[T], typeParameters: [T] |
| Meters | extends Distance[Double], factory(value: Double) |
| Nominal | extends Unit + Primal[String] |
| Ordinal | extends Unit + Primal[Enumeration] |
| Rotation[T] | extends Cardinal[T], typeParameters: [T] |
| Radians | extends Rotation[Double], factory(value: Double) |

The old `unit/` subdirectory was removed — `Unit.json` now lives flat alongside the other Base element JSONs.

For parameterized types, `typeParameters` on TypeName (e.g., `"typeParameters": ["T"]`) was used rather than embedding the parameter in the name string (e.g., `"Cardinal[T]"`).

---

## Phase 5: PrimeOrdinal — New JSON + Scala Update

> **Dev:** Go ahead with PrimeOrdinal making the scala source and json definition both consistent with the new TypeDefinition.

**PrimeOrdinal.json** created with:
- derivation from Extensible (the trait previously had no extends clause)
- 3 Fixed elements (prime, power, product — all self-referential PrimeOrdinal)
- Factory with 3 parameters (no empty-string defaults — field elision handles that)

**PrimeOrdinal.scala** updated:
- `trait PrimeOrdinal` → `trait PrimeOrdinal extends Extensible`
- Added `import draco._`, removed fully-qualified `draco.` prefixes throughout
- Added `_derivation` to inline TypeDefinition matching the JSON
- Factory parameters: dropped empty-string default `""`
- Factory instances now override `typeDefinition` and `typeInstance` (required by Extensible)

---

## Phase 6: Standardize Framework Domain Scala Sources

> **Dev:** yes

The standardized pattern applied to all three:

```
typeDefinition  →  typeInstance  →  domainInstance (with domainDefinition + typeDictionary)
```

**Draco.scala:**
- Replaced inline `new Type[Draco]` with standard `Type[Draco](typeDefinition)`
- `typeDefinition` is now a first-class `lazy val` (not derived from `typeInstance`)
- Added TypeDictionary
- Updated elementTypeNames: removed stale entries (ActorBehavior, DomainElement, RuleActorBehavior, Service), added new ones (Actor, Extensible, Specifically)

**Base.scala:**
- Cleaned up fully-qualified `draco.` prefixes → uses `import draco._`
- `domainDefinition` now reuses `typeDefinition.typeName` instead of a separate TypeName

**Primes.scala:**
- Added TypeDictionary (was missing)
- Fixed elementTypeNames: `"PrimesRuleData"` (nonexistent) → `"Accumulator"`, `"Numbers"`, `"PrimeOrdinal"`
- Cleaned up fully-qualified prefixes

---

## Phase 7: Standardize Test Domain Scala Sources

> **Dev:** yes

All five test domains updated to the same pattern:

| Change | DataModel | Alpha | Bravo | Charlie | Delta |
|--------|-----------|-------|-------|---------|-------|
| `val` → `lazy val` typeDefinition | yes | yes | yes | yes | yes |
| Added `_derivation` | DomainInstance | DataModel | DataModel | DataModel | DataModel |
| `Domain[T](...)` → `new Domain[T] { ... }` | yes | yes | yes | yes | yes |
| Added TypeDictionary | yes | yes | yes | yes | yes |
| Return type → `DomainType` | yes | yes | yes | yes | yes |
| Dropped empty `_elementTypeNames = Seq()` | — | — | — | yes | yes |

---

## Writing the Chapter

> **Dev:** Yes but also create the next chapter in draco-dev-journal, and transcribe this session into it.

**Draco:** *(Reads existing journal chapters for format reference)* *(Creates draco-dev-chapter-16.md)* *(Updates DRACO.md)* *(Updates MEMORY.md and project_type_system_redesign.md)*

All three artifacts written:

1. **`draco-dev-journal/draco-dev-chapter-16.md`** — Full session transcription covering the situation assessment, all design decisions (domain discovery, flat packages, aspect naming), and the seven phases of work with the specific changes made in each.

2. **`DRACO.md`** — Updated the "JSON as Single Source of Truth" section (flat aspect naming, domain discovery convention) and the Primes domain description (flat package, PrimeOrdinal added).

3. **Memory** — Updated `project_type_system_redesign.md` (added sections for flat packages, domain discovery, standardized domain pattern; updated status/next), updated `MEMORY.md` index (marked Task 2 DONE).

---

## Session Summary

Task 2 completed across all 8 domains (Draco, Base, Primes, DataModel, Alpha, Bravo, Charlie, Delta):

1. **Flat packages** — removed all `rules/`, `actor/`, `unit/` subdirectories from both resources and Scala sources
2. **Aspect naming** — `.rule.json` for rules, `.actor.json` for actors, `.json` for types
3. **JSON as source of truth** — all empty JSON files populated; all Base elements, PrimeOrdinal, Accumulator, Numbers now have canonical JSON definitions
4. **Domain discovery** — JSON for domains contains only type identity (no elementTypeNames); Generator will discover contents by scanning resource directories
5. **Consistent domain pattern** — all domains follow `typeDefinition → typeInstance → domainInstance` with TypeDictionary
6. **Import cleanup** — fully-qualified `draco.` prefixes replaced with `import draco._`
7. **Derivation alignment** — Scala typeDefinitions now include `_derivation` matching their JSON
8. **Zero stale references** — all old sub-package imports, FQCNs, resource paths, and sink paths updated

**Design decisions established this session:**
- Domains don't declare their contents — the Generator discovers them by scanning
- No `actor/` or `rules/` sub-packages — flat domain packages with aspect file naming
- `typeParameters` on TypeName instead of embedded in name string (for parameterized Base types)
