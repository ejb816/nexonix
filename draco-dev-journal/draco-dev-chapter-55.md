# Draco Dev Journal — Chapter 55

**Session date:** July 7, 2026
**Topic:** A fifth aspect enters the `TypeDefinition` canon — `CodecAspect`, carrying a single field `discriminator: String` that lifts the one hardcoded codec magic string (`"kind"`) out of the Generator. Landed in two test-gated increments, followed by the criterion for aspect fields and consumption of the discriminator in emission. Transcript.

---

**[Dev]** Let's continue with adding CodecAspect to the type definition canon.

**[Claude]** *(after reading the four aspect files, `Aspects`, `TypeDefinition`, the Generator's codec path, and the chapter-54 journal tail)* No prior CodecAspect design exists — this is a fresh addition to the canon, so let me confirm the shape before writing files. *(Editorial note: the July 5 tail of the Chapter 54 session had in fact discussed codec-as-fifth-aspect and three-state semantics — see ch.54's closing exchanges; the in-session claim understated that groundwork.)*

I now understand the full shape. The four aspects each own a concern (Draco=structure, Domain=membership, Rule=production, Actor=membrane), and the codec **strategy** (simple / discriminated / subtype) is already fully *derived* by the Generator from `DracoAspect` structure. The one genuinely codec-specific, non-derivable fact currently living as a hardcoded magic string in the Generator is the discriminator field name `"kind"`.

Adding `CodecAspect` as the fifth aspect is mechanically regular (mirror the other four across: `CodecAspect.scala`, `CodecAspect.json`, `CodecAspect.drake`, plus wiring into `Aspects`, `TypeDefinition`, and their JSON/drake/encoder/decoder). But what the aspect should *carry* is a canon-design decision that's yours, and I don't want to guess it wrong before writing seven files. *(Put the fork to Dev as a question, recommendation first. Dev chose **discriminator only** — a single `discriminator: String` field, empty = no discrimination, `"kind"` for sealed families; codec strategy stays Generator-derived from `DracoAspect`, not duplicated into the aspect.)*

The key test is `DracoGenTest`: it auto-discovers new JSON and requires `Generator.generate(CodecAspect.json)` to exactly match a hand-written `CodecAspect.scala` byte-for-byte (whitespace-normalized). So I traced the exact String-field elision idiom the Generator produces (`fieldElisionEncoder`/`fieldElisionDecoder`/`elisionCheck`/`decoderForLine`) rather than guess — the subtlety being that `elisionCheck` returns `None` (always-encode) when a parameter's `value` is empty, so an elidable `discriminator` needs `"value": "\"\""` (the empty-string-default JSON idiom already used by `Binding`/`Fixed`/`Monadic`). And the critical structural question — how `TypeDefinition.scala`'s aspect-aware encoder/decoder are emitted — resolved to: they're authored as **Monadic `globalElements` strings** in `TypeDefinition.json`, emitted verbatim by the Generator.

I'll proceed in a focused first increment: **introduce `CodecAspect` as a standalone canonical draco type** (its JSON/Scala/drake + `Draco` registration), get it test-green, and *then* wire it into `Aspects`/`TypeDefinition` as a second step. This de-risks my Generator-emission prediction on the simplest file before touching load-bearing `TypeDefinition`.

Increment A landed:
- **New:** `CodecAspect.json` (one elided `String` field `discriminator`, default `""`, `isEmpty` global, `Draco` membership), `CodecAspect.scala` (hand-written to match Generator emission), `CodecAspect.drake` (`par discriminator String ""` — the discovered convention).
- **Registration:** `"CodecAspect"` in `Draco.json` + `Draco.scala` `elementTypeNames` (alphabetical, after `CLI`), and `GenerateAndCompileTest`'s `typeDefinitionGroup`.

Recommend running the two generator tests to green-light this before Increment B.

*(Delegated action — Dev ran `sbt "testOnly draco.DracoGenTest draco.GenerateAndCompileTest"`: **108/108**, including `CodecAspect.json: Generator output matches CodecAspect.scala`. The emission trace was exact on the first run.)*

Increment A is green. Now **Increment B**: wire `codecAspect` into `Aspects` + `TypeDefinition`. First confirmed via grep that `TypeDefinition.scala` is the **only** implementer of `Aspects` — the new abstract val breaks nothing else. Then:

- **`Aspects`** (`.json`/`.scala`/`.drake`) — added `val codecAspect: CodecAspect`.
- **`TypeDefinition`** (`.json`/`.scala`/`.drake`) — new `codecAspect` factory param (`CodecAspect.Null` default) + the `codecAspect` line in the Monadic encoder/decoder strings (emitted verbatim, so JSON and `.scala` edited in lockstep), `apply`/`Null`/decoder round-trip, `par codecAspect CodecAspect CodecAspect.Null`.

Design notes: appended **last** (draco→domain→rule→actor→**codec**) rather than inserted mid-list — keeps every positional `TypeDefinition(...)` call site working (all params after `typeName` default), and additive-at-the-end matches how the canon has evolved. The alignment in the encoder/decoder strings lines up exactly because `CodecAspect`/`codecAspect` are the same width as `ActorAspect`/`actorAspect`.

The load-bearing change is `TypeDefinition`'s JSON round-trip, so: fast emission check first, then the full suite as the push gate.

*(Delegated action — Dev ran the gen tests then `sbt test`: **202/202** — including `YAMLRoundTripTest 66/66` and `TypeDefinitionTest` encode/decode, which round-trip a `TypeDefinition` now carrying `codecAspect`; the empty aspect elides, so no existing corpus JSON changed shape. Wrote journal chapter 55 and a new `project_codec_aspect` memory.)*

CodecAspect is fully in the canon. The natural next increment: the field is *authored* but not yet *consumed* — the Generator still hardcodes `"kind"` in `discriminatedCodecDeclaration`. Closing the loop means having that emitter read `td.codecAspect.discriminator` (falling back to `"kind"` when empty), which is the whole justification for the field. I've held it as a separate step.

---

**[Dev]** When should we add encoder and decoder as fields (and discriminators) in CodecAspect?

**[Claude]** Good question — it's really a question about the *criterion* for a field earning its place in the canon, not just timing. Grounded answer: today **exactly one** type hand-authors a codec — `TypeDefinition` itself (the `.yaml` is just its companion). Every other type's codec is Generator-*derived*. That single fact drives the whole answer.

**The criterion (not the calendar).** A field earns a place in `CodecAspect` only if it's **both**: (1) **non-derivable** from the rest of the `TypeDefinition` (elements/factory/derivation/modules), and (2) something a **cross-language backend would consume** — the Haskell/Aeson test from [[feedback_haskell_test]]. `discriminator` passed both: the tag string `"kind"` is a free choice, not a function of structure, and any backend emitting a tagged union needs it. That's *why* it was the right — and so far only — field.

**Why encoder/decoder don't qualify yet.** Derived codecs fail test #1 — the strategy is already a function of `DracoAspect`. The one hand-authored codec (`TypeDefinition`'s aspect-`isEmpty`-guarded encoder, embedded as Monadic `globalElements` strings) looks like the forcing case, but it isn't irreducible: it's hand-authored only because `elisionCheck` has no `*Aspect` case, so aspect fields fall through to "always encode." That's a **derivation gap, not an irreducible codec.** Add one rule —

```scala
case s if s.endsWith("Aspect") => Some(s"!$s.isEmpty(x.${p.name})")
```

— and `simpleCodecDeclaration` reproduces `TypeDefinition`'s codec, retiring the Monadic escape hatch entirely. No `CodecAspect` field needed. So: **there is currently zero forcing case for encoder/decoder fields.**

**When they *do* become right:** only when we hit a codec that resists **all three** reductions — structural derivation, a discriminator, and a small declarative elision/rename delta. And even then the field must be a **structured description** (a field → wire-key → elision-rule plan), never an embedded Scala string — otherwise it just relocates the Scala-leak and still fails the Haskell test. One example is a special case; I'd want **≥2** genuinely-irreducible codecs before generalizing a shape.

Recommended sequence: (1) consume `discriminator`; (2) add the `*Aspect` `elisionCheck` case — makes `TypeDefinition`'s codec derive and *proves* the "custom codec was really a derivation gap" thesis by deleting the sole counterexample; (3) revisit encoder/decoder fields only if a truly irreducible codec appears after that — likely never, and if so, structured-only.

---

**[Dev]** Consume the discriminator

**[Claude]** Implementing: `discriminatedCodecDeclaration` will read `td.codecAspect.discriminator`, falling back to `"kind"`. Since no discriminated type sets the field yet, the fallback keeps all generated output byte-identical — tests stay green, proving no regression.

**Generator.scala** — `discriminatedCodecDeclaration` now derives the wire key from the aspect:

```scala
val discriminator = if (td.codecAspect.discriminator.nonEmpty) td.codecAspect.discriminator else "kind"
```

threaded into the three emitted spots — encoder wire key, decoder `downField`, and the decode-failure message. The generated local `val kind` (the tag *value* holder) keeps its name, so nothing else shifts.

**TypeDefinitionTest.scala** — new proof case: the change is a no-op on all existing output, so the test rebuilds the in-memory `Animal`/`Dog` discriminated union with `CodecAspect("species")` on the parent and asserts the authored tag flows into both encoder and decoder, with no default `"kind"` left behind.

Why this is safe: the fallback is exact — every discriminated type today has an empty `codecAspect.discriminator`, so every byte of generated output is unchanged, which is precisely what the byte-for-byte `DracoGenTest` comparison will confirm.

*(Delegated action — Dev ran `sbt test`: **203/203** — the new discriminator test green, all 51 `DracoGenTest` byte-for-byte comparisons still match, `YAMLRoundTripTest 66/66` holds. Updated the journal chapter and `project_codec_aspect` memory.)*

The loop is closed: `CodecAspect.discriminator` is both *authored* (canon) and *consumed* (emission). The one open thread, if you want it later: the `*Aspect` case in `elisionCheck`, which would make `TypeDefinition`'s own aspect-guarded codec *derive* from `simpleCodecDeclaration`, letting you delete the hand-authored Monadic encoder/decoder strings from `TypeDefinition.json` — retiring the sole Scala-leak escape hatch. Clean place to stop for now, though.

## Status
`TypeDefinition` is five aspects wide, and the fifth is fully live — authored in the canon, consumed in emission. Suite **203/203**. Aspect-field criterion settled: non-derivable AND cross-language-consumable; zero forcing case for `encoder`/`decoder` fields today. Queued: the `*Aspect` `elisionCheck` rule to make `TypeDefinition`'s own codec derive — held for a separate increment.
