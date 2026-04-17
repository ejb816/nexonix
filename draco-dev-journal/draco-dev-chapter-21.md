# Draco Dev Journal — Chapter 21

**Session date:** April 15–17, 2026
**Topic:** Reference Frames Increment B, Inline TypeDefinition Elimination, Aspect Suffix Convention, Domain Discovery Design

---

## Session Start — Chapter 20 Review and Journal Push

> **Dev:** Review chapter 20, push if not already pushed, then proceed with Increment B.

**Draco:** Chapter 20 covered Increment A (5 domain skeletons, draco-gen CLI, Generator cross-package import fix). The journal repo was 4 commits ahead of origin/main — pushed. Then hit the worktree problem again: the `charming-tu` worktree was at ancient commit `8c8172b` (nexonix-only, no draco code). Same resolution as Chapter 20 — work directly in main's tree.

---

## Increment B — Per-Frame Leaves and Assembly Types

Each of the four sub-frames gained its native vocabulary. Design decisions made during implementation:

**Leaves with opaque values:** `<Leaf> extends <Frame> with Primal[<primitive>]` — all primitives are `Double` in this increment. Bearing (angle), Reach (distance), Altitude, Heading, Epoch (Julian date), Parallax (arcseconds), RadialVelocity (km/s).

**Leaves with internal structure:** `<Leaf> extends <Frame> with Holon[(T1, T2, ...)]` — Position (lat/lon as `(Double, Double)`), Elements (six Keplerian elements as 6-tuple), ProperMotion (RA/Dec as `(Double, Double)`).

**Assemblies:** `<Assembly> extends <Frame> with Holon[(<Leaf1>, <Leaf2>, ...)]` — tuple of *leaf traits*, not underlying primitives. Percept = `(Bearing, Reach)`, Fix = `(Position, Altitude, Heading)`, Ephemeris = `(Elements, Epoch)`, Trajectory = `(Parallax, ProperMotion, RadialVelocity)`.

**Frames gained `elementTypeNames`.** Adding leaves to a frame's JSON flips `isDomain(td)` to true, switching the Generator from the lean type pattern to the domain pattern (`domainInstance: Domain[T]` with `_elementTypeNames` list). The `Primes.json` example from Increment A's `elementTypeNames`-free design was revealed as aspirational — domain discovery was never implemented in the Generator. This becomes a design discussion point later in the session.

| Frame | Leaves | Assembly |
|---|---|---|
| Egocentric | Bearing, Reach | Percept |
| Geocentric | Position, Altitude, Heading | Fix |
| Heliocentric | Elements, Epoch | Ephemeris |
| Galactocentric | Parallax, ProperMotion, RadialVelocity | Trajectory |

14 new JSON+Scala pairs. `bin/draco-gen compile-multi` green on every family and on the full 19-type universe. `ReferenceFramesGenTest` expanded from 12 to 44 tests: 19 parse, 19 generator/hand-written match, 5 per-family compileMulti, 1 universe compileMulti. No new Generator bugs surfaced.

---

## Long-Term Goals vs Current State

Before continuing to the inline TypeDefinition work, a full stock-take of design-document ambitions vs repo reality.

**Landed:** Type-system redesign (Extensible, Specifically[T], TypeDefinition unification, flat packages), Holon + Transform[S,T], RuntimeCompiler + GenerateAndCompileTest, draco-gen CLI, reference frames Increments A+B, Primes domain with rules, Alpha→DataModel→Bravo pipeline, v2.0.0-alpha.2.

**Stubbed:** Dreams (`trait Dreams {}`), Orion (`trait Orion {}`), Service (`trait Service`). Three stubs, zero functionality.

**Not started:** Increment C (12 transform domains), Transformation meta-type in TypeElement, Formats domain, capability domains (draco.rete/json/actor/scala), Generator[L] as domain, MessageDomain, ION patterns, Getting Started guide.

**Key divergence found:** Memory entry `project_type_system_redesign.md` §5 claimed "Domain Discovery — No elementTypeNames in JSON (DONE — 2026-03-24)" but the Generator has no scanning/discovery code. The discovery mechanism was never implemented. `elementTypeNames` has been hand-maintained in Scala all along.

---

## Inline TypeDefinition Elimination

**The goal:** Replace `lazy val typeDefinition: TypeDefinition = TypeDefinition(_typeName = ..., _derivation = ...)` literals in 8 domain-root companions with `Generator.loadType(TypeName(...))`.

**Design discussion: elementTypeNames in JSON permanently.** The original design intent was for the Generator to scan resource siblings and auto-populate `elementTypeNames` at runtime. This was superseded:

> **Dev:** Do you see any problems with keeping elementTypeNames in the domain definition permanently? Part of domain discovery could, in principle, involve automatic update of the JSON domain definition.

The revised design: `elementTypeNames` lives in the domain JSON permanently. Discovery becomes a *tool* that updates the JSON (write-mode: `bin/draco-gen discover`), not a Generator-runtime behavior. Rationale: completeness (JSON is self-contained), portability across Generator[L] (non-Scala targets can't scan JVM classpaths), deterministic ordering, round-trippability.

**Migration pattern:** Each domain companion's inline `TypeDefinition(...)` block replaced with `Generator.loadType(TypeName(...))`. The `domainInstance` block simplified from `TypeDefinition(typeDefinition.typeName, _elementTypeNames = Seq(...))` to `domainDefinition = typeDefinition` — reading `elementTypeNames` from the loaded JSON directly. No Scala-side duplication of the list. Custom logic preserved: Primes helper methods, Alpha.one/two factories, Bravo.result factory, DataModel nested traits.

8 domains migrated: Draco (21 elements), Base (9), Primes (3), DataModel (3), Alpha (2), Bravo (1), Charlie (empty), Delta (empty).

Memory entry `project_type_system_redesign.md` §5 rewritten to reflect the permanent-elementTypeNames design.

---

## Domain Discovery Design Discussion

The session shifted from implementation to design for domain discovery (items 1–5 from a proposed sequence). Key conclusions:

**Item 3 (scope decisions)** consumed most of the discussion. The initial proposal was three separate JSON fields (`elementTypeNames`, `ruleTypeNames`, `actorTypeNames`). The developer's counter-proposal was better:

> **Dev:** I'm thinking that we reinforce whatever conditional convention the Generator uses to determine if it is generating a rule type or an actor type by requiring the String value of TypeName.name to be `<type name>.rule` or `<type name>.actor`, as appropriate.

This puts the aspect into the TypeName itself — self-describing through data. The filename aspect (`AddNaturalSequence.rule.json`) and the TypeName.name suffix (`"AddNaturalSequence.rule"`) are two expressions of the same fact, reinforcing each other. A single `elementTypeNames` list carries all categories; consumers filter by suffix when they need separation.

### Detour — Nested Aspect Scopes (Explored, Then Abandoned)

> **Dev:** I just had a horrible idea (horrible in the sense of a significant change in how a type definition is generated). What if we used `<type name>.rule` and `<type name>.actor` to mean the type definition would generate the implementation body as an internal declaration that would reference an instance by `<TypeName>.rule.instance` or `<TypeName>.actor.instance`, (instead of `<TypeName>.ruleInstance` of `<TypeName>.actorInstance`). All declarations would exist in the same Scala file, if there were three different type definitions using the same name with the different aspects.

**Draco:** Proposed a concrete shape: `object Primes { ... object Rule { lazy val definition = ...; lazy val instance: RuleInstance = ... } object Actor { ... } }` with condition functions as peers on the outer object for stable Evrete FQNs. Identified the key risk: Evrete's class-lookup via string FQNs for condition functions. If condition functions live inside `object Rule`, their JVM class becomes `Primes$Rule$.w0` — the `$` separators from Scala nested-object compilation. Proposed keeping condition functions at companion scope, not inside the nested object, making the JVM naming irrelevant to Evrete.

> **Dev:** For the evrete class lookup specifically: Instead of declaring an inner object, what about `lazy val rule: RuleInstance = new RuleInstance {...}`?

**Draco:** `lazy val rule` avoids the nested-object JVM class naming, but creates an anonymous class (`$$anon$1`) for the `new RuleInstance { ... }` body. If condition functions were inside that anonymous class, their FQNs would be compiler-generated and unstable. Solution stays the same: condition functions as peers on the outer object with stable FQNs; the form of `rule` (object vs lazy val) becomes a style choice since Evrete never looks up the rule container's class — only condition function classes and fact type classes.

> **Dev:** (b)

Confirmed: `X.rule` is a namespace wrapping `.instance` + `.typeDefinition`, not the instance itself. Access path: `X.rule.instance`, `X.rule.typeDefinition`. Same micro-shape for Domain, Rule, Actor.

> **Dev:** Your shape example has revealed something important. There is a question as to why there still has to be "domainInstance" and "typeInstance" in the same lexical scope. Every "instance" should be in its own explicitly named scope. Also, the words "Domain", "Rule" and "Actor" should be used for these fixed names of internal object declarations.

The developer proposed a deeper restructuring:

```scala
object Primes extends App with DomainInstance {
  lazy val definition: TypeDefinition = Generator.loadType(TypeName("Primes", ...))
  lazy val instance: Type[Primes] = Type[Primes](definition)
  object Domain {
    lazy val instance: DomainType = Domain[Primes]
  }
  object Rule {
    lazy val definition: TypeDefinition = Generator.loadType(TypeName("Primes.Rule", ...))
    lazy val instance: RuleType = Rule[Primes](definition)
  }
  object Actor {
    lazy val definition: TypeDefinition = Generator.loadType(TypeName("Primes.Actor", ...))
    lazy val instance: ActorType = Actor[Primes](definition)
  }
}
```

**Draco:** Drafted full target shapes for Primes (domain root) and Accumulator (domain element). Identified consequences: naming collisions (`object Domain` shadows `draco.Domain[T]` — Generator must emit fully-qualified references), DomainInstance/TypeInstance traits need rework (abstract members renamed, or satisfied by nested objects via Scala's `val X: Y` satisfied by `object X extends Y`), codebase-wide rename of `typeInstance`/`domainInstance`/`typeDefinition` at every caller and override site.

> **Dev:** Before I asked for the Primes example, I should have told you that the draco types TypeInstance, DomainInstance, RuleInstance and ActorInstance should no longer be used anywhere, as the new type declaration structure doesn't need them.

**Draco:** Redrafted without *Instance traits. Significant simplification cascade: `trait Primes extends Extensible` (no DomainInstance), `object Primes extends App` (no `with DomainInstance[Primes]`), all instance-factory override boilerplate disappears (`override val typeDefinition = Alpha.typeDefinition; override val typeInstance = Alpha.typeInstance; override val domainInstance = Alpha.domainInstance` collapses to just the data override). Four traits + their JSONs deleted from draco core.

The developer then provided three further assumptions that expanded the scope:

> **Dev:** All three objects (Domain, Rule, Actor) are declared in every generated DracoType. There is a default type definition in the Generator for any scope type name given to the Generator (includes `<type name>`, `<type name>.Rule` and `<type name>.Actor`, excludes `<type name>.Domain`). The values in the Domain scope are determined by whether or not the Domain scope is contained in a DomainType. If it is then the Domain scope references the DracoType it is declared in, which is also a DomainType. If it is not, then the Domain scope references the Domain it is an element of.

**Draco:** Drafted three examples under these assumptions: Primes (domain root — `Domain.instance` references itself), Accumulator (domain element — `Domain.instance` references `Primes.Domain.instance`), and AddNaturalSequence (rule-only base name — default primary TypeDefinition generated, `Domain.instance` references Primes). Identified that `draco.Rule` and `draco.Actor` would need to become parameterized (`Rule[T]`/`Actor[T]`) to parallel `Domain[T]`, and that the Generator would need structural inference (walk `namePackage`, compare base name to last package element) to determine domain-containment at generation time.

> **Dev:** We need to roll back the discussion to the point just before I prompted "a horrible idea." I've spent too much of our time on a path I can't easily commit to.

Design returned to the simpler aspect-suffix-in-TypeName.name proposal. The nested-scope exploration produced genuine insights — uniform aspect access patterns, the Domain-as-structural-view concept, the instance-factory boilerplate elimination — but the implementation scope was too large to commit to in this session. The exploration is preserved here for future reference.

---

## Aspect Suffix Convention — Implementation

Phase 1 landed:

**8 rule/actor JSONs updated** — `typeName.name` gained `.rule` or `.actor` suffix. Current Scala identifiers (`AddNaturalSequenceRule`, `BravoActor`) and member names (`ruleInstance`, `actorInstance`) unchanged.

**4 domain JSONs updated** — `elementTypeNames` gained aspect-suffixed entries alongside plain type names:
- Primes: added `AddNaturalSequence.rule`, `PrimesFromNaturalSequence.rule`, `RemoveCompositeNumbers.rule`
- DataModel: added `AssembleResult.rule`, `DataModel.actor`
- Bravo: added `Bravo.actor`
- Natural: added `Natural.actor`

**Generator isRule/isActor updated** — suffix check added as primary detection alongside existing structural/derivational checks. This fixed Natural.actor which had no derivation and was invisible to the old `isActor` logic.

**Survey findings preserved:** Generator's `isRule` was structural (has variables), `isActor` was derivation-based (extends ActorType/ActorInstance/ExtensibleBehavior). Two different mechanisms — both now reinforced by the suffix check.

`TupleFact.rule.json` under `org/nexonix/rules/rete/` is orphaned — no domain root in that package. Its name-suffix update is correct but it won't appear in any `elementTypeNames` list.

---

## Verification

Full test suite: **107/107 pass.** Up from the 48-test `GenerateAndCompileTest` baseline — the new `ReferenceFramesGenTest` (44 tests) plus existing tests across all packages.

---

## Session Summary

### Code Changes

1. **Increment B** — 14 new JSON+Scala pairs (10 leaves + 4 assemblies). 4 sub-frame JSONs gained `elementTypeNames`. 4 sub-frame `.scala` files regenerated (now domain pattern). `ReferenceFramesGenTest` expanded to 44 tests.

2. **Inline TypeDefinition elimination** — 8 domain-root JSONs gained `elementTypeNames`. 8 domain-root `.scala` companions migrated to `Generator.loadType` + `domainDefinition = typeDefinition`. Custom logic preserved.

3. **Aspect suffix convention** — 8 rule/actor JSONs gained `.rule`/`.actor` suffix in `typeName.name`. 4 domain JSONs list aspect-suffixed entries in `elementTypeNames`. Generator `isRule`/`isActor` reinforced with suffix check.

### Design Decisions

- **elementTypeNames lives permanently in JSON.** Discovery is a tool (write-mode), not a Generator-runtime scan. Rationale: completeness, portability, determinism.
- **Aspect suffix in TypeName.name.** `.rule`/`.actor` reserved suffixes. Reinforces filename aspect. Single `elementTypeNames` list carries all categories.
- **Domain-ness is structural** (type name matches last package element). The `DomainInstance` derivation is no longer the signal — `elementTypeNames.nonEmpty` drives `isDomain(td)`.

### Next Session Priorities

1. **Item 1 — verify the 8 inline-TD migrations at runtime** (test suite passed but specific runtime behavior like TypeDictionary resolution via loaded elementTypeNames should be spot-checked).
2. **Item 5 — `loadType` silent-fallback risk.** Change to `Option` or throw on missing JSON now that all domains rely on it.
3. **Item 2 — Generator simplification.** Emit `Domain[T](typeDefinition)` instead of verbose `Domain[T](_domainDefinition = TypeDefinition(...))`. Regenerate reference frames. Closes the pattern gap between hand-written and generator-output.
4. **Domain discovery CLI** — `bin/draco-gen discover`/`verify` subcommands, informed by the aspect-suffix convention and the settled elementTypeNames-in-JSON design.
5. **Reference-frame Increment C** — 12 transform domains.
