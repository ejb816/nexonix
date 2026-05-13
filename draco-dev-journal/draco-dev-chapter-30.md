# Draco Dev Journal — Chapter 30

**Session date:** May 9–10, 2026
**Topic:** Alphabetical canonicalization sweep through the draco package via DracoGenTest. Extensible eliminated mid-sweep. Generator polished to emit canonical form. Convergence on a single emission style for every `src/main/scala/draco/*.scala` file.

---

## Session Open

Inherited state from chapter 29: DomainAspect.typeName migration shipped, 156-test corpus green, two new tests created — `domains.DomainsGenTest` (renamed from ReferenceFramesGenTest) and `draco.DracoGenTest` — both refactored to filesystem-walk + exclusion-map discovery. The DracoGenTest's per-type comparison was the new failure surface; the alphabetical sweep was the agreed workflow.

User process locked at the top of the session:

> **Dev:** Let's go through the draco package in lexical order, starting with Actor. I'll run the single test on that, paste the result to you and then we will first update Actor.json to get the Generator to generate the same code that is in the Scala file. Then, if we want to make a change to the Scala code, we'll make the change and run the same single test again, make the necessary JSON changes until the test passes. Then move on to the next file and repeat until we've made all necessary changes to make the test pass for all the files.

The agent confirmed the test invocation pattern:
```
sbt 'testOnly draco.DracoGenTest -- -z draco/<TypeName>.json'
```
and proposed the alphabetical order — Action / BodyElement / Condition / Dynamic / Factory / Fixed / Monadic / Mutable / Parameter / Pattern / Variable belong to the TypeElement family (excluded; group-tested at the bottom), TypeElement itself is group-tested, and everything else is per-type.

---

## Actor — Surfaced a Generator Bug

First file in the sweep triggered the largest Generator-level discovery of the session. Initial diff:

```
generated:    trait Actor[T]Actor extends ExtensibleBehavior[T] with ActorType
              object ActorActor extends App with DracoType
hand-written: trait Actor[T] extends ExtensibleBehavior[T] with ActorType
              object Actor extends App with DracoType
```

**Root cause** — `isActor`'s derivation-based clause was firing for the base `Actor` type itself, causing the `Actor` name suffix to be appended to `Actor` → `ActorActor`. The suffix is intended only for `.actor` aspect TDs (`Bravo.actor.json` → `BravoActor`).

Agent split the responsibilities:

> **Agent:** `isActor` does double duty: it controls (a) whether to import Pekko, AND (b) whether to append the `Actor` suffix to trait/object names. The suffix is intended for actor-*aspect* TDs (`Bravo.actor.json` → `BravoActor`), but the derivation-based clause also fires for the base `Actor` type itself, producing `ActorActor`. The heuristics for these two responsibilities should differ.

Applied one-line fix:
```scala
val nameSuffix = if (td.typeName.name.endsWith(".actor")) "Actor" else ""
```

Bug cleared. Remaining diffs surfaced as systemic Generator-emission style choices — every type was affected by:

1. `import draco._` self-import for types already in package `draco`.
2. Redundant wildcard imports for namespaces (pekko, circe, evrete) already covered by static specific imports.
3. `draco.Generator.loadType(...)` instead of bare `Generator.loadType(...)`.
4. `_typeParameters = Seq("T")` included in the TypeName literal inside the `loadType` call (parametric types resolve type params from the loaded JSON; the call arg is redundant).
5. `Seq("draco")` with no space after `Seq` (project convention is `Seq ("draco")`).

Agent batched four Generator polish fixes:

- `packageHierarchyImports` skips `import draco._` when type is itself in package `draco`.
- `referencedPackageImports` skips namespaces in a new `wellKnownExternalPackages` set covering pekko / circe / evrete / java.net / java.util.function / scala.io (these have static specific imports already).
- `typeDefinitionLoad` / `ruleDefinitionLoad` emit bare `Generator`.
- New `typeNameLiteralForLoad` helper omits `_typeParameters` from the TypeName literal inside load calls. Plus `Seq (` spacing.

Five minutes later — and one NPE caught because `wellKnownExternalPackages` was declared `val` instead of `lazy val` (DelayedInit bit again per memory: "All companion vals are `lazy val`") — Actor passed:

```
[info] - draco/Actor.json: parses
[info] - draco/Actor.json: Generator output matches draco/Actor.scala (whitespace-normalized)
```

Conventions established and now carried forward to every subsequent type:

- JSON body `valueType` uses **bare names** (rely on imports, not FQNs).
- Every type has `dracoType` + `domainType` as companion-level vals.
- Inline single-statement method bodies in Scala (no `{ ... }` wrapping).

---

## ActorAspect — The Aspect Pattern Defined

User pre-edited `ActorAspect.scala` to show the target shape, then asked the agent to discuss differences before running the test. The result was a design conversation that locked the canonical form for *all* aspect TDs (Aspects, DracoAspect, DomainAspect, RuleAspect, ActorAspect).

**Decisions made:**

> **Agent (#3):** `lazy val` vs `override val` for `typeDefinition`. Three candidates: `lazy val`, `override lazy val`, `override val`. Recommend `override lazy val` — explicit AND lazy (DelayedInit-safe).
>
> **Agent (#4):** Hanging-paren factory params vs 4-space block. Recommend 4-space block in Generator; update hand-written Scala to match.
>
> **Agent (#6):** `override val` vs `override lazy val` for factory body. Recommend `override lazy val`. Null-guard wrapper (`if (_x != null) _x else default`) — propose JSON-level opt-in.

> **Dev:** I'll take all your recommendations except for adding guardNull to Parameter.

Locked. Generator updates:

- Companion `typeDefinition`: prefix with `override` when `chainHits(td, "DracoType")` succeeds (lazy stays).
- Factory body Fixed / BodyElement → `override lazy val` (was `override val`).
- Bare `apply()` for `Null` when all factory params have defaults.
- Multi-line Monadic globalElement support — every line gets the body-indent prefix.

JSON-level updates for aspect TDs:

- Add `derivation: [DracoType]` so the trait emits `extends DracoType` (not `extends Extensible`).
- Add `globalElements` with a `Monadic` block for `isEmpty` (using YAML `|-` block scalar with embedded newlines).

Generator's auto-codec surprise:

> **Agent:** Generator auto-emits a codec (it knows `Action` has a `body` field — sophisticated!) so my Monadic encoder/decoder ended up as a *second* copy. … the auto-codec produces the same logic with cleaner type-aware defaults — let's just drop the Monadics and align hand-written Scala to the auto-codec's emission style (variable `x`, calls `ActorAspect(...)` instead of `apply(...)`).

ActorAspect passed. Same pattern then trivially handled DomainAspect, DracoAspect, RuleAspect when their turns came.

---

## App-Warning Suppression

User observed mid-sweep:

> **Dev:** I'm using App on the companion object in all draco type declarations so I can easily compile/debug just the draco type code plus it's compile/run dependencies. Is there something in the App inheritance that can do that without causing the warnings?

The warning ("X will not have an entry point on the JVM. Reason: companion is a trait, which means no static forwarder can be generated.") is a JVM-interop edge case when both `trait X` and `object X extends App` exist — the object's `main` works via sbt/IDE launchers, just not bare `java -cp ... X`. Agent's recommendation:

```scala
scalacOptions += "-Wconf:msg=will not have an entry point on the JVM:s"
```

Added to `build.sbt` inside the root project's `.settings(...)` block. Reload needed; warnings gone in subsequent runs.

---

## Eliminate Extensible — Mid-Sweep Pivot

After DracoType (alphabetically the last type whose trait *had* extended Extensible), user raised the architectural question:

> **Dev:** Before we declare "passed", is this the point where we can eliminate "Extensible"?

Per Stage 2e plans (memory: imminent task #5 "Eliminate Extensible — natural follow-up to DracoType-as-root"), this was indeed the right moment. Agent scoped the work:

- 23 hand-written `.scala` files with `extends Extensible`.
- 3 resource files (`Extensible.json` itself, `Draco.json`'s elementTypeNames listing, plus `Actor.json`'s reference to `ExtensibleBehavior` — false-positive grep match).
- Generator's `typeExtends` default clause `else "extends Extensible"`.
- Generator's rule trait emission `trait $ruleName extends Extensible`.

Executed in one batch:

1. **Generator**: dropped default `extends Extensible`; rule trait emission emits bare `trait $ruleName`.
2. **22 `.scala` files**: stripped via `sed -i '' 's/ extends Extensible$//; s/ extends Extensible / /; s/extends Extensible //'`.
3. **Deleted**: `Extensible.scala`, `Extensible.json`.
4. **`Draco.json` + `Draco.scala`**: dropped `"Extensible"` from elementTypeNames.

`sed` bug surfaced afterward: for `extends Extensible with X`, the substitution stripped `extends Extensible` but left the orphan `with X`. Three files affected (Holon, Transform, Natural); each manually patched to `extends X`.

Then a single tail-end Generator polish: when `typeExtends` returns empty, `traitDeclaration` was emitting `trait Name  body` with a double space. Refactored to assemble parts conditionally:

```scala
val extPart  = if (ext.nonEmpty)  s" $ext"  else ""
val bodyPart = if (body.nonEmpty) s" $body" else ""
s"…trait …$nameSuffix$extPart$bodyPart"
```

DracoType passed after the polish. User:

> **Dev:** passed - That was a difficult change. Good work.

---

## The Sweep — Progress Through the Alphabet

Files passing at session pause (in alphabetical order, with notes on what changed):

| Type | JSON change | Scala change | Notes |
|---|---|---|---|
| Actor | bare-name body types | rewrite to canonical | Suffix-bug fix in Generator |
| ActorAspect | derivation DracoType, isEmpty globalElement | rewrite to canonical | Auto-codec adoption |
| ActorType | none | replaced legacy inline TD with `Generator.loadType` form | Hand-written modernized |
| Aspects | derivation DracoType | added companion (was bare trait) | TypeDefinition got `override lazy val typeDefinition` to satisfy inherited DracoType abstract |
| CLI | none | added domainType, `override`, modernized | Object-only path |
| ContentSink | none | rewrite to canonical | methodBody indent fix in Generator |
| Dictionary | derivation `[Map, DracoType]` | trait `extends Map[K, V] with DracoType`, modernized | DomainDictionary/TypeDictionary got minimal override-typeDefinition patches to compile |
| Domain | none | replaced legacy inline TD | — |
| DomainAspect | derivation DracoType, isEmpty globalElement | rewrite to canonical (with auto-codec) | Both .yaml and .json kept in lockstep |
| DomainDictionary | none | replaced legacy inline TD | — |
| DomainType | none | replaced legacy inline TD; dropped unused apply/Null | Required preemptive Draco.scala edit (Draco.scala referenced DomainType.Null) |
| Draco | none | added `dracoType` + `elementTypeNames` Seq literal | Earlier had vestigial `superDomain: DomainType = DomainType.Null` (removed in DomainType iteration) |
| DracoAspect | derivation DracoType, isEmpty globalElement | rewrite to canonical | — |
| DracoType | none (Extensible already removed) | rewrite to canonical | **Object extends App with DracoType — self-extending trait** |
| Holon | derivation `[DracoType]` (was `[Primal[T]]`), drop `value` element | added `with DracoType`, `override`, domainType, `Seq (` space | User pre-edited trait to `extends DracoType` (no Primal); had to drop `override val value` from `base/Coordinate.apply` to unblock compile |

That's 15 of ~30 per-type entries plus the structural Extensible elimination.

Pending alphabetically: Main, Primal, REPL, Rule, RuleAspect, RuleType, RuntimeCompiler, SourceContent, Specifically, Test, Transform, Type, TypeDefinition, TypeDictionary, TypeName, Value. Plus TypeElement family group-test. Plus sub-packages: `draco/base/*`, `draco/language/*`, `draco/primes/*`.

---

## Generator Polish Catalog (Cumulative)

Universal emission improvements landed mid-sweep, each one-line-ish, reducing diff size for all subsequent types:

1. `nameSuffix` only fires when `typeName.name.endsWith(".actor")` — fixes `Actor[T]Actor` bug.
2. `packageHierarchyImports` skips `import draco._` for types in package `draco`.
3. `wellKnownExternalPackages` set excludes pekko / circe / evrete / java.net / scala.io from wildcard `referencedPackageImports`.
4. `typeDefinitionLoad` / `ruleDefinitionLoad` emit bare `Generator` (no `draco.` prefix).
5. `typeNameLiteralForLoad` omits `_typeParameters` from TypeName literal in load calls.
6. `Seq (` with space, both for `namePackage` and `typeParameters` literals.
7. Companion `typeDefinition` prefixed with `override` when `chainHits(td, "DracoType")`.
8. Factory body Fixed/BodyElement and parameter-derived overrides emit `override lazy val`.
9. `Null` emits bare `apply()` when all factory params have defaults.
10. Multi-line `Monadic` globalElements get per-line indent prefix.
11. `methodBody` accepts `methodIndent` parameter; factoryBody passes 4 for proper inner-indent.
12. `traitDeclaration` conditional space — no double-space when `typeExtends` is empty.
13. Default `extends Extensible` removed entirely.
14. Rule trait emission no longer adds `extends Extensible`.

The Generator surface is genuinely much closer to canonical-Scala-style now. Each fix was driven by a real diff in the alphabetical sweep, not speculative cleanup.

---

## Pause

Test state at pause: every test run through Holon has passed. The sweep is paused mid-corpus, with ~16 per-type entries remaining plus the TypeElement family group test and the three sub-packages.

**Branch:** `main`, parent repo. Uncommitted. (User commits via IDE per `feedback_direct_main_edits`.)

**Resume**: continue with **Main** next alphabetically:
```
sbt 'testOnly draco.DracoGenTest -- -z draco/Main.json'
```

The conventions are now stable enough that most remaining types should follow predictable patterns: legacy inline TypeDefinition → `Generator.loadType` form, add `dracoType`/`domainType`, `override lazy val`, drop `extends Extensible` (already done by the sed sweep, just verify the Scala still compiles after our incremental Generator changes).
