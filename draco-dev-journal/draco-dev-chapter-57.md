# Draco Dev Journal — Chapter 57

**Session date:** July 7, 2026 (continues ch.56)
**Topic:** Closing the codec loop. `TypeDefinition` was the only type in the corpus hand-authoring its codec (aspect-`isEmpty`-guarded encoder/decoder embedded as Monadic `globalElements` strings); ch.55 argued this was a derivation gap, not an irreducible codec. This chapter proves it — one new `elisionCheck` case makes the codec derive, the Monadic escape hatch is deleted, and the re-declaration wart is then erased by teaching the codec gate to follow `derivation`. Suite **195/195**. Transcript.

---

*(The first exchange chronologically preceded the YAML retirement of ch.56 — it scoped the increment that was then executed after YAML was cleared.)*

**[Dev]** How does TypeDefinition.json have to change to make sure that the proper codec is generated from it? Are there any other json definition files that have to me modified for the Generator to generate the expected encoder and decoder?

**[Claude]** Good question — this is exactly the `elisionCheck` follow-up made concrete. Let me verify the precise gate that decides whether a codec is auto-derived, rather than answer from memory.

**The gate that currently blocks derivation.** `codecDeclaration` reaches the simple-codec path for `TypeDefinition`, but this test fails:

```scala
val elementNames = td.dracoAspect.elements.map(_.name).toSet   // {typeName}
val paramNames   = td.dracoAspect.factory.parameters.map(_.name).toSet  // {typeName, dracoAspect, …, codecAspect}
if (paramNames.subsetOf(elementNames) && !anyFunctionLike) simpleCodecDeclaration(td) else ""
```

The five aspect params aren't in `elements` (they're inherited from `Aspects`), so `subsetOf` is false → no auto-codec → that's *why* the Monadic escape hatch exists.

**What `TypeDefinition.json` must change:** (1) add the five aspects to `elements` (as `Fixed`), so `paramNames ⊆ elementNames` and the gate passes; (2) delete the two Monadic `globalElements`. But `elements` alone isn't enough: with the current `elisionCheck`, aspect params fall through to `None` → the derived encoder would emit `Some("dracoAspect" -> …)` *unconditionally*, dropping the empty-aspect elision and changing the wire form. So this **requires the Generator rule** —

```scala
case s if s.endsWith("Aspect") => Some(s"!$s.isEmpty(x.${p.name})")
```

— added to `elisionCheck`, placed after the `Seq[`/`Map[` cases (so `Seq[DracoAspect]` still routes to the Seq case). (3) Regenerate the mirror `TypeDefinition.scala` — the byte-for-byte `DracoGenTest` target shifts: the codec block moves *above* `apply`/`Null` (companion emission order), the lambda binds `x` not `td`, the aligned columns collapse to single spaces, and the decoder yields `TypeDefinition (…)` not `apply(…)`. Semantically identical.

**Other JSON files: none.** The `endsWith("Aspect")` rule only fires on factory **parameters** of an aspect type — and a grep confirms `TypeDefinition` is the only definition with those. The matches in the five `*Aspect.json` files are their own factory *return* `valueType` (never run through `elisionCheck`), and `Aspects.json` has no factory.

Two caveats worth deciding on first: **redundant trait redeclaration** — adding aspects to `elements` makes the generated trait redeclare the abstract vals it already inherits from `Aspects`; the *cleaner* alternative is to teach the gate about inherited members (walk `derivation`), which is arguably the more honest fix. And the rule assumes every `*Aspect` has a companion `isEmpty` — true for all five today; an invariant to keep. My recommendation: the gate-relaxation variant. Want me to implement it that way?

---

**[Dev]** Yes

*(Answering the closing offer of ch.56 — take on the codec-derivation increment next. For this increment the self-contained path was taken first; the gate-relaxation follows two exchanges later.)*

**[Claude]** Executing the codec-derivation increment.

1. **Generator.scala** — `elisionCheck` gains the one case above. This is the whole mechanism: aspect-typed fields now elide via their companion `isEmpty`. It only fires for aspect-typed *factory params* — and `TypeDefinition` is the only type with those, so no other type's emission changes.
2. **TypeDefinition.json** — added the five aspects to `elements` (satisfies the `paramNames ⊆ elementNames` codec gate) and **deleted the two Monadic `globalElements`** — the hand-authored escape hatch is gone.
3. **TypeDefinition.scala** — rewritten to match the now-*derived* emission: codec block above `apply`, `x` lambda, `TypeDefinition (...)` yield, trait lists all six fields. Semantically identical to the old hand-authored codec (same fields, same elision).

**One honesty note I want to flag:** re-listing the aspects in `TypeDefinition.json`'s `elements` is a Generator-gate mechanic, not a pure shape statement — the drake's `type TypeDefinition from Aspects` already implies them. So I **left `TypeDefinition.drake` unchanged** (its `from Aspects` + `typeName` is the cleaner truth). If you'd rather not carry that JSON-vs-drake asymmetry, the alternative is the gate-relaxation — a deeper Generator change I can do as a follow-up. For now this is the self-contained, verifiable path.

*(Delegated action — Dev ran the gen tests: **103/104, 1 failure** — the `TypeDefinition.scala` byte-compare.)*

The diff is a perfect match except one thing: the generated output has a **blank line before the closing `}`**, which my hand-written file lacks. That's the Generator's convention for a factory+codec type with no trailing `globalElements` — `TypeName.scala` has exactly that blank line too. Fixed; everything else matched byte-for-byte, which confirms the derived codec is exactly right.

*(Delegated action — Dev re-ran the gen tests, then the full suite: **195/195**. Wrote journal chapter 57; closed out the open thread in `project_codec_aspect` memory.)*

**The codec loop is closed at 195/195.** Milestone: **every codec under `src/main/scala/draco/` now derives from JSON** — no hand-authored codec strings remain anywhere in the corpus. The Scala-leak that chapter 55 flagged as the one open codec residue is gone. One thing left on the table (your call): I took the self-contained path, whose cost is the deliberate wart — the generated trait re-declares the five vals it inherits from `Aspects`, and there's a drake↔JSON `elements` asymmetry. The gate-relaxation erases it but needs the Generator to load the parent during single-type generation. Not urgent, and everything's green either way.

---

**[Dev]** So can  we have TypeDefinition.json refer to the aspects similar to how BodyElement.json refers to its subtypes?

**[Claude]** Good instinct to probe — let me look at exactly how `BodyElement.json` references its subtypes before answering, because the mechanism matters.

`BodyElement.json`'s `modules` is a **sum-type** mechanism — "these are my subtypes" — and it drives the *discriminated-union* codec (pattern-match on subtype, emit a `"kind"` tag). Aspects are the opposite: `TypeDefinition` **has all five simultaneously** (a product), not "is one of." So `modules` would be categorically wrong here — it'd generate a `sealed trait TypeDefinition` with a tag-dispatching codec.

But your instinct — *reference the aspects by name instead of re-declaring them* — is exactly right, and that mechanism **already exists**: it's `derivation` / the drake's `type TypeDefinition from Aspects`. The aspects are referenced there, once. The only reason I *also* re-listed them in `elements` was that the codec gate doesn't look *through* `derivation` to inherited fields. So the clean answer is: make the gate honor the reference that's already there. Implemented:

- **Generator.scala** — new helper `inheritedElementNames(td, familyMap)` walks `derivation` transitively (ancestors from `familyMap`, else `loadType` from classpath; cycle-guarded); the simple-codec gate unions those with own elements.
- **TypeDefinition.json** — `elements` back to `[typeName]`; aspects referenced once via `derivation: [Aspects]`.
- **TypeDefinition.scala** — trait back to `val typeName` alone; codec still derives.
- **TypeDefinition.drake** — unchanged and now *consistent*; the drake↔JSON asymmetry is gone.

The gate change is **monotonic** — it can only make *more* types eligible for a simple codec, never fewer — so it can't break an existing simple-codec type. The one thing to watch is whether any *other* type newly qualifies; `DracoGenTest`'s byte-compare will flag any surprise.

*(Delegated action — Dev ran the gen tests: **101/104, 3 failures** — `Type`, `Meters`, `Radians`.)*

As I flagged, the monotonic relaxation over-triggered: `Type[T]`, `Meters`, and `Radians` newly got codecs. Their factory param is a *single inherited field* (`typeDefinition` from `DracoType`; `value` from `Distance`/`Rotation`) — they're **pure inheritance wrappers** with **zero own elements**, and were intentionally codec-less. `TypeDefinition` differs: it has an own element (`typeName`) *plus* the inherited aspects. So the right narrowing is an `ownElementNames.nonEmpty` guard — **a type earns a simple codec only if it declares ≥1 field of its own AND every param is reachable (own or inherited).** That keeps pure wrappers codec-less while letting a record compose inherited fields, and every existing simple-codec type already has nonempty own elements, so no regression.

*(Delegated action — Dev re-ran the gen tests, then the full suite: **195/195** — `TypeDefinition` keeps its derived codec, `Type`/`Meters`/`Radians` revert to codec-less, every existing type untouched. Updated chapter 57 and the `project_codec_aspect` memory to mark the wart resolved.)*

So the answer to your question: **not via `modules`** (sum-type mechanism → discriminated codec), **but yes via `derivation`** — which was already there. The codec gate now follows it, so `TypeDefinition.json` references the aspects *once* with no re-listing, and `from Aspects` is the whole truth in both surfaces. This closes the whole arc cleanly — a natural stopping point.

## Status
Suite **195/195**. Every codec under `src/main/scala/draco/` derives from JSON — the Monadic escape hatch is deleted and the "derivation gap, not irreducible codec" thesis is proven by construction. The gate is now a defensible semantic (`ownElementNames.nonEmpty` + `inheritedElementNames` via `derivation`), not a crutch; drake and JSON agree. See [[project_codec_aspect]].
