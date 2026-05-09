# Draco Dev Journal — Chapter 28

**Session date:** May 8–9, 2026
**Topic:** Stage 2e foundation. Original goal was leaf detection on a clean base; never reached. The session became a deep audit of TypeDefinition.scala bootstrap soundness, then a one-type-at-a-time push to make the entire `draco.*` package generatable from JSON, then the architectural shift to DracoType-as-root with every instance carrying typeDefinition. Closes with all 156 tests green and the platform finally ready for leaf detection — which is now Stage 2e proper, in a fresh session.

---

## Session Open

The user's opening message was the inherited task list from chapter 27's tail:

> **Dev:** Stage 2e (leaf detection) on a clean base

The agent began the standard pre-flight: confirmed location (parent main, fully synced through Stage 2d at `0fc7566`), pulled the chapter 27 design memo for leaf detection (`elementTypeNames.size == 1 && single-name.toLowerCase == namePackage.last`), and surveyed the conflicts the original Stage 2c memo had warned about — that adding leaf markers naively would trip Generator's `isDomain` check (which flips on `elementTypeNames.nonEmpty`).

Recommendation: do `Direction.json` as canary first, get the smallest possible feedback loop.

---

## "We Are in Serious Trouble"

Before the canary edit could happen, the user reframed:

> **Dev:** I think we are in serious trouble. I do not see how TypeDefinition.scala could possibly be generated from TypeDefinition.yaml or from the TypeDefinition.json that would have been the intermediate form for generating TypeDefinition.scala

The agent dropped the Stage 2e plan and started auditing. Read the hand-written `TypeDefinition.scala` carefully against `TypeDefinition.yaml`. Found a substantial gap — not just back-compat shims, structural divergences:

1. The trait body declares 17 convenience `def`s (`def superDomain = dracoAspect.superDomain`, ...) — pure hand-written code with no data-model counterpart. They were back-compat shims for callers that read `td.superDomain` directly, added during Stage 2c when aspects were introduced.
2. Legacy `apply` takes 17 flat parameters; YAML's factory has 5 aspect-shaped params.
3. `fromAspects` factory exists in Scala only — no YAML representation.
4. Custom `decoder` accepts BOTH aspect-block and legacy-flat JSON shapes (dual-format) — Generator can't emit dual shape.
5. Custom `encoder` with elision logic.
6. Bootstrap recursion: the `typeDefinition` companion val constructed itself by calling `apply(...)` — and that `apply` is the legacy 17-param form.
7. `extends App` companion convention.
8. **`_elements` over-counted aspects** — listed all four aspects as own elements, double-counting what's inherited from `Aspects`.
9. `_derivation` was empty in the bootstrap value, even though the trait `extends Aspects` has Aspects in its derivation.

The agent's read: TypeDefinition.yaml is *aspirational*. It describes the post-back-compat shape. The hand-written Scala is the runtime authority. They diverge substantially. The "round-trip 117/117 verified" claim from chapter 26 was structural data round-trip, NOT a claim that emitted Scala equals hand-written Scala.

The user accepted this framing immediately:

> **Dev:** We have to focus completely on correcting the mistakes in TypeDefinition.scala without generating it and without making modifications to any other source code until we absolutely have to. It is hard for me to overstate how broken this is.

---

## Bootstrap Cleanup Round 1

Agent proposed a localized fix to lines 30-52 of TypeDefinition.scala — rewrite the bootstrap value via `fromAspects` with the right `DracoAspect` block (one element `typeName`, one derivation entry `Aspects`).

User went further. They edited TypeDefinition.scala themselves and reported:

> **Dev:** I've changed TypeDefinition.scala to what I think it should be. Let's discuss how we should proceed from that.

Agent read the user's edit. Significant changes:
- Trait body: 17 convenience defs gone. Just `val typeName: TypeName` plus 4 redeclared aspect vals (which the user later acknowledged were unintentional).
- `fromAspects` deleted entirely.
- Bootstrap value collapsed to `TypeDefinition.Null`.
- Decoder still references `fromAspects` — compile error.

Several compatibility questions surfaced:

> **Dev:** When doing "new TypeDefinition { ... }" that code body implements all 5 fields for the type name and 4 aspects values, even though only typeName is supplied in the trait. I unintentionally re-declared the 4 aspect fields.

Agreement: drop the four aspect val redeclarations from the trait body. Trait becomes:

```scala
sealed trait TypeDefinition extends Aspects {
  val typeName: TypeName
}
```

Plus a small fix: route the decoder through the surviving public constructor `apply` (extracting flat fields from the just-built aspect blocks). Awkward — aspects in, flat out, apply rebuilds aspects — but uses only the public surface, no new methods.

The user's principle here was deliberate:

> **Dev:** I intentionally removed the helper fromAspects, so it wouldn't have to be specified in the JSON definition.

API minimization driving the data model: every Scala construct must have a JSON-data-model counterpart, so Scala-only helpers are noise.

The user also flagged a missed opportunity from earlier sessions:

> **Dev:** By the way, we should not have deleted the JSON files when we added YAML, but used them to see if the new YAML converted into the existing JSON for generating the source code.

Agreed and saved as feedback. The right move when adding a new authoring format is to keep the old as ground truth until equivalence is proven, not just empirically claimed.

---

## YamlToJsonBootstrap

The user's preference for the recovery path:

> **Dev:** Let's use Scala to re-create all, including TypeDefinition, from YAML

Agent created `src/test/scala/draco/language/YamlToJsonBootstrap.scala` — a one-shot test that walks `src/main/resources/draco/`, loads each `.yaml` via the TypeDefinition decoder, encodes back via the encoder, writes the JSON sibling. Always overwrites, safe to re-run.

Result on first invocation: 6 JSON files materialized — `ActorAspect.json`, `Aspects.json`, `DomainAspect.json`, `DracoAspect.json`, `RuleAspect.json`, `TypeDefinition.json`. None of these had ever been in git history (verified by `git log --all --diff-filter=A -- "*Aspect.json"` returning nothing). They were born YAML during Stage 2d's aspect redesign; the JSON path was never the authoring surface for them.

After this, every YAML file in the corpus had a JSON sibling. YAMLRoundTripTest (118 files) stayed green throughout.

---

## Generator Cascade — The 17 Convenience Defs

With the convenience defs removed from TypeDefinition.scala, Generator.scala stopped compiling. Agent surveyed:

> **Dev:** Now you have to fix Generator because the 17 defs that were spuriously introduced in a previous session, no longer exist in TypeDefinition.scala

Found ~50 distinct call sites across `Generator.scala` and one in `TypeDictionary.scala`. Did the rewrite via `replace_all` per field name: `td.X` → `td.dracoAspect.X` (for the 9 DracoAspect fields), `td.elementTypeNames` → `td.domainAspect.elementTypeNames`, `td.variables`/`td.action`/etc. → `td.ruleAspect.X`. Same for `parentTd.X`, `moduleTd.X`, and underscore-syntax `_.X` patterns. Verified zero remaining unfixed references.

User ran the test. ContentSink-specific failure. Plus a scope-clarification:

> **Dev:** Can you detect any dependencies in Generator that require YAML?

Answer: hard compile dependency on the `circe-yaml` library; soft runtime preference (tries `.yaml` first then falls back to `.json`); JSON-only deployment works fine. Nothing in Generator's *logic* requires YAML resources at runtime.

---

## GenerateAndCompileTest Restructured

Then a request that opened up the rest of the session:

> **Dev:** Can we now extend GenerateAndCompileTest so that the rest of the definitions in the draco package can be included? I am hoping all the Scala sources in the draco package (except for Generator.scala) can be generated from JSON definitions, but we need to find out if there need to be more fixes in TypeDefinition group or TypeElement group first by testing them with more definitions.

Agent restructured the existing single walker-based test into three focused group tests, each compiled as one unit:
1. **TypeElement hierarchy** (12 types: TypeElement + BodyElement + Fixed/Mutable/Dynamic/Parameter/Monadic + Pattern/Action/Condition/Variable/Factory)
2. **TypeDefinition family** (6 types: TypeDefinition + Aspects + 4 sub-aspects)
3. **Draco core types** (26 remaining types in `draco/` root)

First run: TypeElement and TypeDefinition green. Draco core failed with a parse error and an Extensible-cyclic.

Two targeted Generator fixes landed:
- **Merged TD now includes `_derivation`** in `generate(typeDefinitions)` so external imports (e.g., pekko `ExtensibleBehavior` referenced by Actor's derivation) resolve across the multi-type compile unit.
- **Extensible-self-cyclic guard** in `typeExtends`: `else if (td.typeName.name == "Extensible") ""` so `trait Extensible extends Extensible` doesn't get emitted.

---

## DracoType-as-Root

The user asked about Extensible:

> **Dev:** Since you mentioned Extensible first, I have a concern that the new field "extensible" in DracoAspect means that the explicit definition and declaration of Extensible is no longer useful, and can be elided from the definitions now. That means that DracoType can be the root type in the draco type inheritance chain. What do you think about that?

Agent unpacked: the *field* `extensible: TypeName` in DracoAspect is the metadata slot for "what to extend"; the *trait* `Extensible` is the no-instance-contract marker base. Two different things. Removing the trait would force every instance (TypeName, Holon, Primal[T], TypeElement) to gain an instance-level `typeDefinition: TypeDefinition` reference — heavier than the current design.

Recommended two-tier root be preserved unless the user specifically wanted every instance to carry typeDefinition.

User's answer was unambiguous:

> **Dev:** With the aspects consolidated under a single TypeDefinition, the answer is yes, every draco type in the draco.* packages should have a reference to it's own TypeDefinition.

Architectural shift committed.

Agent proposed two Generator changes: typeExtends default `extends DracoType` instead of `extends Extensible`; chainHits-conditional auto-emit becomes always-true except for DracoType itself. User accepted ("Let's do it.").

Result: TypeElement broke. TypeElement leaves emit `override lazy val typeDefinition` based on the Generator's claim, but the *classpath* has hand-written `Primal extends Extensible` — no typeDefinition abstract on Primal — so the override fails with "overrides nothing."

This is when the principle clarified.

---

## Scala-First vs JSON-First

> **Dev:** Does it make sense to change source code first, since I usually do that first when I'm working without an AI model?

Agent's reply (saved as feedback): yes — for inheritance-graph changes, Scala-first is the right move because the classpath defines compile-time truth. No amount of Generator output can pretend Primal extends DracoType when classpath says otherwise. The refined principle:

> JSON is authoring surface for type *shape* (fields, factory, codec); hand-written Scala is authoring surface for the *inheritance backbone*. The two have different ontological status.

Plan agreed: revert the chainHits-always-true and typeExtends-default Generator changes. Do the architectural shift hand-written-side. Then re-apply Generator changes once classpath agrees with intent.

---

## One-Type-at-a-Time Push

For the rest of the session, the workflow stabilized. User's framing:

> **Dev:** Can we do one Scala file at a time? I'm fine with running GenerateAndCompileTest.scala in between as many times as necessary.

Each iteration: pick a failing type, look at its hand-written `.scala`, decide what JSON authoring (and possibly Generator extension) is needed, apply, re-run test, identify next failing type.

Order it played out:

**ContentSink**: First. Hand-written `extends DracoType` (user pre-edited). Required a new `factory.body` block. Required Generator additions: `case mo: Monadic` (raw code emission for the local `sinkPath` val), Dynamic case using `methodParameters(d.parameters)` (was missing), parents-list pattern emitting `with DracoType` on companions when `chainHits(td, "DracoType")`. Also added derivation `[DracoType]` to ContentSink.json.

**SourceContent**: Same pattern. Five-entry factory.body (sourceURI Monadic, three Fixed overrides for source/sourceLines/sourceString, source.close() Monadic).

**RuleType + Type**: Different problem — factory parameters had leading underscores (`_typeDefinition`, `_ruleDefinition`) which produced `override val _typeDefinition` overriding nothing. Renamed params (drop the leading underscore) and added `hasTypeDefinitionOverride` check in Generator (skip auto-emit when factory provides its own `typeDefinition` override) to avoid duplicate definitions.

**Codec suppression**: After RuleType added action/pattern as elements, the Generator emitted a `simpleCodecDeclaration` which tried to find `Encoder[Consumer[Knowledge]]` (doesn't exist). Added `isFunctionLikeType` helper — skip codec generation when any param has a function-like value type.

**Main + Test**: Trait elements (sourceRoot/sinkRoot) didn't match factory params (sourceName/sinkName); apply computed the URI overrides via `classOf[X].getResource("/").toURI.resolve(...)`. Encoded via factory.body. Added `globalElements` for `roots` lazy val on the companion. Updated hand-written Main.scala and Test.scala to extend DracoType pattern; cascaded into hand-written `Generated.scala` (which extends Main, transitively gained typeDefinition contract) — minimal `override val typeDefinition` patches.

**Dictionary family**: The interesting one. `Dictionary[K,V] extends Map[K,V]` — Map has 4 abstract methods (iterator, get, removed, updated) that the hand-written Dictionary trait satisfies via concrete delegations to `kvMap`. JSON only had `kvMap` as element. Generator's typeBody Dynamic case only emitted abstract `def`. Extended typeBody to emit `def name(...): T = methodBody(d.body)` when body is non-empty (mirrors the Fixed concrete-default pattern). Added the four delegations as Dynamic elements with body. For `updated[V1 >: V]`, embedded the type parameter in the `name` field — small workaround, no data-model change.

**Actor**: Last. Factory was empty; trait extends pekko `ExtensibleBehavior[T] with ActorType`. Required factory.parameters with `actorDefinition`, factory.body with two Fixed overrides (typeDefinition + actorDefinition both = `_actorDefinition`) and two Dynamic methods (receive + receiveSignal) with body returning `Behaviors.same[T]`. Used FQN for pekko types (`org.apache.pekko.actor.typed.Behavior`, `TypedActorContext`, `Signal`, `scaladsl.Behaviors`) since externalTypeImports doesn't scan factory.body for type references.

After Actor: all three groups PASS. Whole `draco.*` package generates and compiles from JSON.

---

## Reference Frames Patch

The architectural shift's `with DracoType on companions` change broke ReferenceFramesGenTest's whitespace-normalized diff comparison (26 failures). Each diff was the same one-line: hand-written `object Foo extends App` vs generated `object Foo extends App with DracoType`. Bulk-patched all 26 hand-written domain files via `sed`. All green.

---

## Final State

**Tests:** 156 total, all PASS. GenerateAndCompileTest's three groups verify the whole `draco.*` package generates and compiles from JSON. ReferenceFramesGenTest (38 reference-frame types) generates byte-matchable Scala. YAMLRoundTripTest 118/118.

**TypeDefinition.scala:** clean. Trait body has just `val typeName`. No back-compat 17 defs. No `fromAspects`. Bootstrap value is `TypeDefinition.Null`. Decoder routes through `apply`. Custom encoder + dual-shape decoder retained as hand-written exceptions (the Generator can't emit either).

**Generator gains:** 12 new emission features documented in `project_generator_emission_features_2026-05-09.md`.

**Architectural shift:** every type that gets generated from JSON now extends DracoType (transitively or directly). Every instance carries `typeDefinition`. Companion objects of DracoType-derived types extend `with DracoType` themselves.

**Cascade still partial:** Primal/TypeName/TypeElement/Holon hand-written code still says `extends Extensible`. Eliminating Extensible and migrating those four traits is the natural follow-up — but each migration would propagate a chain of typeDefinition overrides through anonymous instances and would benefit from its own focused session.

---

## What's Open

- **Stage 2e leaf detection** — the original opener of this session, never reached. Foundation now in place. Fresh session.
- **Eliminate Extensible** — finishes the architectural shift. Hand-written cascade similar to Stage 2d's.
- **Replace hand-written domain files with generated** — 26 ref-frame files now byte-matchable.
- **Reconcile TypeDefinition.yaml with hand-written apply** — 5-aspect-param vs 18-flat-param mismatch. Bootstrap regenerability blocked until reconciled.

---

## Stopping-Point Ritual

Memory updates landed:
- New: `project_dracotype_root_shift.md` (architectural shift reference)
- New: `feedback_scala_first_for_inheritance.md` (refined authoring-surface principle)
- New: `project_generator_emission_features_2026-05-09.md` (catalog of new Generator capabilities)
- Refined: `feedback_json_authoring_surface.md` (now scoped to type *shape*; defers to scala-first-for-inheritance for backbone changes)
- Updated: `MEMORY.md` index, key facts, imminent-tasks queue.

Session closes here. Stage 2e proper queued for next session.

> **Dev:** Beam me up, Scotty.

(Implied. The user actually asked about wrapping up before relaunching their Claude app for an update.)
