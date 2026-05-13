# Draco Dev Journal — Chapter 29

**Session date:** May 9, 2026 (continuation)
**Topic:** Stage 2e leaf detection reframed — `typeName: TypeName` added to `DomainAspect` so every type's `domainAspect.typeName` declares its containing domain (self-loop for domains, container-pointer for leaves). Migration executed: schema, Scala, Generator, and 123 of 124 resource files updated. Session paused before tests run — to be resumed.

---

## Session Open

The session opened with the worktree-redirect SessionStart hook firing as expected; agent moved to parent `main` (still at `20c6bc2`, Stage 2e foundation), confirmed clean tree, refreshed memory index. User asked "what's next?"

Agent recommended **Stage 2e leaf detection proper** (item 4 in the imminent-tasks list), noting that the foundation work in chapter 28 had specifically been to unblock this, and listed the alternatives (eliminate-Extensible, reconcile TypeDefinition.yaml) so the user could redirect.

---

## Reframe: From Leaf Detection to Aspect-Val Architecture

The user immediately broadened the scope:

> **Dev:** I want to include the so-called Stage 2e leaf detection into a broader scoped discussion about the 4 aspect values in the type definition. Specifically noting that both val dracoType and val domainType should always be present in any source generated from a draco type definition. The val dracoType will always reference the draco type singleton runtime instance. If the draco type is also a domain type, then val domainType will reference the Domain[<draco type>] of the draco type]. If the draco type is not itself a domain type, then the val domainType will reference the Domain[<other draco type>] that contains draco type as an element type of the other draco domain. The problem is to look at the type definition val typeName and val domainAspect, and decide, based on the value of domainAspect.elementTypeNames, whether whether val domainType should reference Domain[<draco type>] or Domain[other draco type].

The agent peeked at sample JSONs to ground the discussion: `Primes.json` had a `domainAspect.elementTypeNames` of six entries; `Numbers.json` had no `domainAspect` block at all. The discriminator the user proposed — `domainAspect.elementTypeNames` non-empty vs absent — was **locally computable**.

But the agent flagged an asymmetry the framing hadn't quite resolved: discrimination is local, but **container lookup for a leaf is not** — the leaf's TypeDefinition does not name its containing domain. Today the relationship is encoded inversely (the domain lists its leaves), and historically leaves like `Numbers` also derive-from `Primes` — but `dracoAspect.derivation` is *not* a reliable proxy for containment in general. `Direction` in `Egocentric` extends `Primal`, not `Egocentric`. So derivation cannot stand in for "container of."

Agent surfaced two clean options:

1. **Generator package-scan**: for a leaf, find the type in the same package whose `domainAspect.elementTypeNames` contains this leaf's name. Single source of truth (the domain's listing), no back-pointer duplication.
2. **Explicit `domainAspect.containingDomain`** on every leaf. Self-describing per-TypeDefinition, but redundant with the domain's listing and prone to drift.

Recommended (1), package-scan, on the grounds that elementTypeNames stays the sole authority and the Generator already needs cross-type knowledge for derivation chains. Also offered a symmetric corollary for the four aspects: aspect *presence* drives kind-val emission across the board.

---

## User Pivot: `typeName: TypeName` on DomainAspect

The user cut across the recommendation:

> **Dev:** Sorry to cut across your recommendation, but lets consider adding val typeName: TypeName field to DomainAspect. If the value of domainAspect.typeName equals typeDefinition.typeName, then the draco type is a domain type and domainAspect.elementTypeNames is the list of contained draco types, otherwise domainAspect.typeName is the containing domain, and domainAspect.elementTypeNames is either empty or not there.

This was a stronger design than option (2) because the new field's *name* — `typeName`, not `containingDomain` — let `DomainAspect` itself declare which domain it pertains to. Treats DomainAspect as named-by-self for domains and named-by-container for leaves. Symmetric on both sides of the relationship.

Agent accepted the pivot:

> **Agent:** Yes — that's better than my recommendation. Three things make it cleaner:
> 1. **Discriminator becomes a positive declaration**, not an absence-of-evidence (`elementTypeNames` empty could also describe a degenerate domain). `domainAspect.typeName == typeDefinition.typeName` is unambiguous.
> 2. **Container lookup goes local.** No package-scan, no cross-type Generator query. Each TypeDefinition is self-describing for both its domain identity and its membership.
> 3. **Cross-package containment falls out for free.** A leaf at `draco.X` can declare containment in a domain at `draco.Y` purely by setting `domainAspect.typeName` to that TypeName. No new mechanism, no convention layered on the package tree.

Agent then raised two refinements worth deciding before implementation:

- **Should `RuleAspect` and `ActorAspect` follow the same shape?** I.e., gain a `typeName` for parallelism. For Draco/Domain the distinction matters (self vs container); for Rule/Actor it might be vacuous.
- **Sub-domain story.** Should a sub-domain (e.g., Primes inside Draco) be expressible? Currently Draco.json doesn't list Primes — sub-domains are package peers, not nested elements.

---

## User Closes the Design

The user resolved both questions tightly:

> **Dev:** The typeName was added to DomainAspect because of the dual-meaning issue of the generated val domainType. Neither actorType nor ruleType have that issue. Having x.y.Y be a domain contained in x.X the way x.Y is contained in x.X seems both counter-intuitive and not particularly useful at this stage.

The asymmetry was principled: only `domainType` has two referents, so only `DomainAspect` needs the disambiguator. `RuleAspect`/`ActorAspect` stay as-is — `ruleType`/`actorType` always reference self. And dropping nested sub-domains kept the model flat: one domain per package, leaves point up to it, no nesting.

Final shape locked:

- `DomainAspect`: gains `typeName: TypeName`. Discriminator is `domainAspect.typeName == typeDefinition.typeName`.
- `RuleAspect` / `ActorAspect`: unchanged.
- Every TypeDefinition carries a `domainAspect`. Generator emits `dracoType` (self) + `domainType` (Domain[self] or Domain[container]); emits `ruleType` / `actorType` only when those aspects are present.
- Generator invariant on load: for every leaf L pointing at D, `D.elementTypeNames` contains `L.name`, and conversely.

User: **"Let's migrate."**

---

## Migration Plan

Agent proposed a six-step sequence:

1. **Schema**: add `typeName: TypeName` to `DomainAspect.yaml` + `.json`.
2. **DomainAspect.scala**: add `val typeName`; thread through apply/encoder/decoder/isEmpty/Null. Decoder stays lenient (`typeName` optional → `TypeName.Null`) so partial migration works.
3. **TypeDefinition.scala**: add `_domainTypeName: TypeName = TypeName.Null` flat param; thread through apply, encoder, decoder fallback.
4. **Generator.scala**: switch `isDomain` to `domainAspect.typeName == typeDefinition.typeName`; emit `domainType = Domain[Self]` for domains, `Domain[<Container>]` for leaves; ensure `domainAspect.typeName` joins cross-package imports; emit `domainType` for *all* generated types.
5. **Resource sweep** (Python script): add `domainAspect.typeName` to every JSON/YAML. Domains self-loop; leaves point at their package's domain. Orphan resolution: aspect TDs (`Aspects`/`DracoAspect`/`DomainAspect`/`RuleAspect`/`ActorAspect`) get added to `Draco.elementTypeNames`.
6. **Tests + iterate**.

User did not push back; agent began executing.

---

## Steps 1–4: Code Changes

**Schema** (`DomainAspect.{yaml,json}`): added a `typeName: TypeName` element ahead of `elementTypeNames`, with factory parameter defaulting to `TypeName.Null`.

**`DomainAspect.scala`**: trait gained `val typeName: TypeName`; `apply` gained `_typeName: TypeName = TypeName.Null` parameter; `isEmpty` updated to require both `typeName.name.isEmpty && elementTypeNames.isEmpty`; encoder emits `typeName` when nonempty; decoder reads `typeName` as optional (`getOrElse TypeName.Null`).

**`TypeDefinition.scala`**: `apply` gained `_domainTypeName: TypeName = TypeName.Null` between `_globalElements` and `_elementTypeNames`. Decoder fallback path passed `TypeName.Null` for the new field. Final `apply` reconstruction passes both `domainAspect.typeName` and `domainAspect.elementTypeNames`.

**`Generator.scala`** — five distinct edits:

1. **`referencedPackageImports`**: included `td.domainAspect.typeName` so a cross-package container reference triggers an import.
2. **`isDomain`**: discriminator becomes `domainAspect.typeName.name.nonEmpty && domainAspect.typeName == typeDefinition.typeName`. Kept legacy `elementTypeNames.nonEmpty` and `(source && target)` clauses as **transitional fallbacks** for hand-constructed test fixtures (e.g., `TestTypeModule`) and pre-migration transform domains.
3. **New helpers**: `domainTypeLiteral(containerName: String)` parameterized by container; `containerName(td)` resolves leaf's container from `domainAspect.typeName.name`.
4. **`domainGlobal`**: now emits both `dracoType: Type[Self]` (which it had been missing) and `domainType: Domain[Self]`.
5. **`typeGlobal` / `objectGlobal` / `ruleGlobal`**: emit `domainType` when `containerName(td)` is non-empty (skip silently during partial migration); rule path also gains `dracoType: Type[<Name>Rule]`.

The actor path flows through `typeGlobal` with `nameSuffix = "Actor"`, so it gets `domainType` for free.

---

## Step 5: Resource Sweep

Wrote `/tmp/migrate_domain_typename.py` (PyYAML-dependent). Logic:

1. **Load** all 124 resource files into memory; index by `(packageTuple)`.
2. **Resolve domain per package**: pick the file whose `typeName.name` matches the package's last segment capitalized (e.g., `draco.primes` → `Primes.json`); else fall back to any file in that package qualifying as a domain via `is_domain_td`.
3. **Special handling for `draco` package**: Draco's `elementTypeNames` already had 21 entries but was missing 22 peer types (the aspect TDs, `Action`, `Pattern`, `Type`, `Holon`, etc.). The script union-ed all peer files into Draco's listing and rewrote it sorted (43 entries).
4. **Stamp** `domainAspect.typeName` on every TD: self-loop for domains, container-pointer for leaves.

Ran the script:

```
NOTE (no domain in package org.nexonix.rules.rete.rules): /Users/edwardburke/IdeaProjects/nexonix/src/test/resources/org/nexonix/rules/rete/TupleFact.rule.json
Files scanned: 124
Files written: 123
```

The single orphan was `TupleFact.rule.json` in `org.nexonix.rules.rete.rules` — a deeply nested test fixture with no domain in its package. Left its `domainAspect.typeName` unset; will surface only if the test exercises Generator output for it.

---

## Spot-Check Sample

Agent verified three resources after the sweep:

- `Numbers.json`: gained `domainAspect.typeName = Primes` (container pointer). `dracoAspect` block intact.
- `Primes.json`: `domainAspect.typeName = Primes` (self-loop), `elementTypeNames` preserved.
- `EgocentricGeocentric.json`: transform domain, gained self-loop `domainAspect.typeName`. Kept `source`/`target`.
- `Draco.json`: `elementTypeNames` expanded from 21 to 43 entries (all sorted), self-loop set.

The `DomainAspect.{yaml,json}` schema files themselves got `domainAspect.typeName = Draco` (since they're in package `draco` → leaves of Draco).

---

## Pause

Agent surfaced a self-review concern before handing off: `TestTypeModule.scala` constructs hand-written TypeDefinitions with `_elementTypeNames` but no `_domainTypeName`. Re-added the legacy `elementTypeNames.nonEmpty` clause to `isDomain` as a transitional fallback so those fixtures keep working without modification.

Asked the user to run **`YAMLRoundTripTest`** (codec round-trip for the new field) and **`GenerateAndCompileTest`** (generator emission across the whole corpus). User paused the session before running them.

---

## State at Pause

**Branch:** `main`, parent repo. Not committed. (Per `feedback_direct_main_edits`, edits land directly; user commits via IDE.)

**Files changed:**
- `src/main/resources/draco/DomainAspect.yaml` (schema)
- `src/main/resources/draco/DomainAspect.json` (schema)
- `src/main/scala/draco/DomainAspect.scala` (field + codec)
- `src/main/scala/draco/TypeDefinition.scala` (`_domainTypeName` threading)
- `src/main/scala/draco/Generator.scala` (discriminator, emission, imports)
- 122 resource JSONs (sweep)
- 1 resource YAML (`TypeDefinition.yaml`) (sweep)

**Files unchanged but worth knowing:**
- 8 hand-written domain Scala files (Primes, Base, DataModel, Alpha, Bravo, Charlie, Delta, plus Draco itself). They use `Generator.loadType(...)` so the new field flows through automatically.
- `TestTypeModule.scala` (hand-constructed fixtures) — still uses legacy isDomain path via the fallback clause.
- `TupleFact.rule.json` (the one orphan) — `domainAspect.typeName` unset.

**Tests not yet run** at pause. Expected risk areas:
1. **YAMLRoundTripTest**: should pass — codec is symmetric, lenient on missing `typeName`.
2. **GenerateAndCompileTest**: most likely to surface emission issues. Specifically watch for:
   - Cross-package imports for leaf-container pointers (should be covered by `referencedPackageImports` change).
   - The `Type[$wName]` literal in `domainGlobal` — could break if any domain has type parameters that confuse the wildcard substitution (none currently in the corpus).
   - Rule companions: `dracoType: Type[<Name>Rule]` is new emission and may trigger compile errors if the trait is parameterized.
3. **ReferenceFramesGenTest**: 94 tests — the transform domains all got self-loops, leaves got domain pointers. Should be stable.

When session resumes, the next action is to ask the user to run those tests (or let the user volunteer test results) and iterate.

---

## Memory Updates Pending

To capture before resuming:

- `project_dracotype_root_shift.md` should reference the new in-progress sub-stage.
- New memory: `project_domainaspect_typename.md` capturing the design (DomainAspect.typeName as discriminator + container pointer; rule/actor asymmetric rationale; sub-domain explicitly out of scope).
- `MEMORY.md` index entry for the above.
- "Imminent tasks" list reordering: Stage 2e leaf detection now subsumed under this domainaspect-typename pattern; the original `single-name == namePackage.last` heuristic is no longer needed.

These are written immediately following this chapter.
