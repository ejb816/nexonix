# Draco Dev Journal — Chapter 57

**Session date:** July 7, 2026
**Topic:** Closing the codec loop. `TypeDefinition` was the **only** type in the corpus hand-authoring its codec (aspect-`isEmpty`-guarded encoder/decoder embedded as Monadic `globalElements` strings). Chapter 55 argued this was a *derivation gap, not an irreducible codec*. This chapter proves it: one new `elisionCheck` case makes the codec derive, the Monadic escape hatch is deleted, and the sole counterexample to "every codec derives from JSON" is gone. Suite **195/195**.

---

## 1. The one-line mechanism

The whole gap was that `elisionCheck` had no case for aspect-typed fields, so they fell through to "always encode" — which is *not* the intended elision (`TypeDefinition` elides an empty aspect via that aspect's own `isEmpty`). One case closes it:

```
case s if s.endsWith("Aspect")   => Some(s"!$s.isEmpty(x.${p.name})")
```

placed after the `Seq[`/`Map[` cases (so `Seq[DracoAspect]` still routes to the Seq case). It emits exactly `if (!DracoAspect.isEmpty(x.dracoAspect)) Some("dracoAspect" -> x.dracoAspect.asJson) else None` — the hand-authored form, now derived. It fires only for aspect-typed *factory parameters*, and `TypeDefinition` is the only type with those, so no other type's emission moved (the 50 other `DracoGenTest` byte-compares confirm it).

## 2. Satisfying the codec gate — and the honesty wart

`codecDeclaration`'s simple-codec path gates on `paramNames.subsetOf(elementNames)`. `TypeDefinition`'s aspect params are accessible only via inheritance from `Aspects`, so the gate failed — which is *why* the escape hatch existed. Two ways to satisfy it were on the table (ch. "how does TypeDefinition.json change"):

- **(A) list the aspects in `TypeDefinition.json`'s `elements`** — self-contained, verifiable, but re-declares inherited fields.
- **(B) relax the gate to see inherited elements** — honest, but needs the Generator to load the parent (`Aspects`) during single-type generation.

Took **(A)** for this increment: self-contained and byte-verifiable without new cross-type loading. The cost is a wart — the generated trait now re-declares the five aspect vals it already inherits, and `TypeDefinition.json`'s `elements` re-list them purely to pass the gate. The **`.drake` was left unchanged** (`type TypeDefinition from Aspects` + `typeName`) because it's the *honest* shape statement — `from Aspects` already implies the aspects. So there's a deliberate drake-vs-JSON asymmetry, flagged for the Dev: option (B) remains available as a follow-up to erase it.

## 3. What was deleted, and the milestone

`TypeDefinition.json` lost its two Monadic `globalElements` (the raw embedded `encoder`/`decoder` Scala strings). `TypeDefinition.scala` was regenerated: the codec block moves above `apply` (companion emission order), the lambda binds `x` not `td`, columns collapse to single spaces, the decoder yields `TypeDefinition (...)` not `apply(...)`. Behaviourally identical — same fields, same elision, same round-trip (the derived encoder even preserved the empty-aspect elision, so no corpus JSON changed shape). The one gen-test miss was a missing blank line before the closing `}` — the Generator's convention for a factory+codec type with no trailing `globalElements` (`TypeName.scala` has it too); added, green.

**Milestone:** with the escape hatch gone, **every codec under `src/main/scala/draco/` now derives from JSON** — no hand-authored codec strings remain anywhere in the corpus. `comparisonOnlyExcluded` was already `Map.empty`; now the last type that *needed* a hand-authored escape (even though it lived in the JSON, not an exclusion) is gone too. The Scala-leak that ch.55 §4 identified as the one open codec residue is closed. See [[project_codec_aspect]].

## 4. Erasing the wart — refer, don't re-declare

The Dev pushed on §2's wart: *can `TypeDefinition.json` refer to the aspects the way `BodyElement.json` refers to its subtypes?* The answer sharpened the design. `BodyElement`'s `modules` is a **sum-type** list ("these are my subtypes") that drives the discriminated codec — categorically wrong for aspects, which `TypeDefinition` **has all of simultaneously** (a product). But the by-reference instinct was right: that mechanism already exists as **`derivation` / `from Aspects`**. The aspects are referenced there, once. The re-listing in `elements` was only a codec-gate crutch.

So option (B) got built: a new `inheritedElementNames(td, familyMap)` walks `derivation` transitively (ancestors from `familyMap`, else `loadType` from classpath; cycle-guarded), and the simple-codec gate unions those with own elements. Then `TypeDefinition.json`'s `elements` dropped back to `[typeName]`, its trait back to `val typeName` alone, and the `.drake`'s `from Aspects` is now the whole truth — no asymmetry.

The relaxation is **monotonic** (only grows the accessible-name set), so it can't disable an existing codec — but it *over-triggered* on the first run: `Type[T]`, `Meters`, `Radians` newly got codecs. Each is a **pure inheritance wrapper** — zero own elements, its single factory param delegated to a parent's field (`typeDefinition` from `DracoType`; `value` from `Distance`/`Rotation`) — and each was intentionally codec-less. The distinguishing signal: `TypeDefinition` declares an own element (`typeName`) and *composes* the inherited aspects onto it, whereas the wrappers declare nothing of their own. So the gate gained an `ownElementNames.nonEmpty` guard: **a type earns a simple codec only if it declares ≥1 field of its own AND every param is reachable (own or inherited).** Every pre-existing simple-codec type already has nonempty own elements, so no regression; `Type`/`Meters`/`Radians` stay codec-less; `TypeDefinition` derives. Suite **195/195**. The wart is gone and the rule is now a defensible semantic, not a crutch.
