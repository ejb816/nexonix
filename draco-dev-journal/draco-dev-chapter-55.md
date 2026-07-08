# Draco Dev Journal — Chapter 55

**Session date:** July 7, 2026
**Topic:** A fifth aspect enters the `TypeDefinition` canon. `CodecAspect` joins `Draco`/`Domain`/`Rule`/`Actor` as a peer, carrying a single field — `discriminator: String` — that lifts the one hardcoded codec magic string (`"kind"`) out of the Generator and into the definition surface. Landed in two test-gated increments: (A) `CodecAspect` as a standalone canonical type, (B) wiring it through `Aspects` + `TypeDefinition`. Suite ends **202/202**.

---

## 1. The opening scope question — what does a codec aspect *carry*?

The Dev asked to "continue with adding CodecAspect to the type definition canon." No prior CodecAspect design existed in the codebase or chapter 54, so the first act was reconstructing the four-aspect pattern and finding where a fifth would sit.

The four existing aspects each own a concern: **DracoAspect** = structure (derivation/elements/factory/globalElements/source/target), **DomainAspect** = membership (typeName self-loop + elementTypeNames), **RuleAspect** = production (pattern/values/action), **ActorAspect** = membrane (messageAction/signalAction/setupAction). Reading the Generator's codec path (`simpleCodecDeclaration` / `discriminatedCodecDeclaration` / `subtypeCodecDeclaration`, dispatched by `codecDeclaration`) showed the codec **strategy** is already fully *derived* from `DracoAspect` structure — modules force a discriminated union, derivation-from-a-discriminated-parent forces `Codec.sub`, else a simple field-elision codec. The only genuinely codec-specific, **non-derivable** fact was the discriminator field name `"kind"`, hardcoded as a string literal in `discriminatedCodecDeclaration`.

That framed a real fork the Dev had to settle — put to them as one question, recommendation first:

> **Dev's choice:** `discriminator only` — a single `discriminator: String` field (empty = no discrimination; `"kind"` for sealed families). Codec strategy stays Generator-derived from `DracoAspect`; we do **not** duplicate it into the aspect.

The smallest honest step: name the one thing that was a magic constant, leave everything derivable derived.

## 2. Increment A — CodecAspect as a standalone canonical type

Before touching the load-bearing `TypeDefinition`, `CodecAspect` landed on its own so the Generator-emission match could be verified on the simplest possible file.

The test that governs this is `DracoGenTest`: it **filesystem-walks** `src/main/resources/draco/` for `.json`, auto-including any new definition, and for each asserts (a) it parses and (b) `Generator.generate(json)` matches the hand-written `.scala` **byte-for-byte** (whitespace-normalized). So the hand-written `CodecAspect.scala` had to reproduce Generator output exactly. Rather than guess, I traced `fieldElisionEncoder` / `fieldElisionDecoder` / `elisionCheck` / `decoderForLine`: a `String` param with a non-empty `value` (default) elides on `.nonEmpty` in the encoder and defaults to `""` via `.getOrElse("")` in the decoder. The key subtlety: `elisionCheck` returns `None` (always-encode) when `p.value.isEmpty` — so to make `discriminator` *elidable*, its factory parameter needs `"value": "\"\""` (the JSON idiom for an empty-string default, already used by `Binding`/`Fixed`/`Monadic`/etc.).

Files:
- **New:** `CodecAspect.json` (shape), `CodecAspect.scala` (mirrors the other four aspect companions), `CodecAspect.drake` (`par discriminator String ""` — the discovered empty-string-default convention).
- **Register:** `"CodecAspect"` into `Draco.json` + `Draco.scala` `elementTypeNames` (alphabetical, after `CLI`), and into `GenerateAndCompileTest`'s `typeDefinitionGroup`.

Suite gate: **108/108** on the two generator tests, including `CodecAspect.json: Generator output matches CodecAspect.scala`. The emission trace was exact on the first run.

## 3. Increment B — wiring codecAspect into Aspects + TypeDefinition

With the type verified, the canon integration. First a safety check: `grep` confirmed `TypeDefinition` is the **only** implementer of `Aspects` (its `apply` anonymous class is the sole site overriding the aspect vals), so a new abstract `codecAspect` val on the trait could break nothing else.

The revealing detail is *how* `TypeDefinition`'s aspect-aware encoder/decoder survive the byte-for-byte gen test at all: they aren't derived by the Generator's generic codec path (which would emit `Some("dracoAspect" -> …)` with no elision). They're authored as **Monadic `globalElements` strings** inside `TypeDefinition.json` — raw Scala embedded verbatim, using each aspect's own `X.isEmpty(...)` as the elision guard. So wiring codecAspect meant editing those embedded strings *and* the mirror `.scala` in lockstep:

- **Aspects** (`.json`/`.scala`/`.drake`): `+ val codecAspect: CodecAspect`.
- **TypeDefinition** (`.json`/`.scala`/`.drake`): new `codecAspect` factory param (`CodecAspect.Null` default) + `if (!CodecAspect.isEmpty(td.codecAspect)) Some("codecAspect" -> …)` in the encoder Monadic + the decoder `for`-line and 6-arg `apply(...)` yield.

Two deliberate choices:
1. **Appended last** (draco→domain→rule→actor→**codec**), not inserted. Every positional `TypeDefinition(...)` call site keeps working because all params after `typeName` default; additive-at-the-end mirrors how the canon has grown.
2. The encoder/decoder alignment columns line up **for free** — `CodecAspect`/`codecAspect` are the same character width as `ActorAspect`/`actorAspect`, so the existing aligned `Some(... -> ...)` spacing needed no rework.

Full suite: **202/202**. The load-bearing evidence is `YAMLRoundTripTest 66/66` and `TypeDefinitionTest` encode/decode — both round-trip a `TypeDefinition` that now carries `codecAspect`, and both stayed green, confirming the field elides when empty (so no existing corpus JSON changed shape) and reconstructs when present.

## 4. The criterion for aspect fields — and why encoder/decoder don't qualify yet

Before consuming the field, the Dev asked the sharper question: *when* should `CodecAspect` gain `encoder`/`decoder` fields (and discriminators)? The answer turned out to be a criterion, not a date. A field earns canon-membership only if it is **both** (i) non-derivable from the rest of the `TypeDefinition`, and (ii) something a cross-language backend would consume ([[feedback_haskell_test]]). `discriminator` passed both — the tag string is a free choice, and any Aeson/whatever backend emitting a tagged union needs it.

Grounding it in the corpus: a `grep` showed **exactly one** type hand-authors a codec — `TypeDefinition` itself (its aspect-`isEmpty`-guarded encoder/decoder, embedded as Monadic `globalElements` strings). Every other type's codec is Generator-*derived*. And that one custom case isn't irreducible: it's hand-authored only because `elisionCheck` has no `*Aspect` case, so aspect fields fall through to "always encode." Add one rule — `case s if s.endsWith("Aspect") => Some(s"!$s.isEmpty(x.${p.name})")` — and `simpleCodecDeclaration` reproduces it, retiring the escape hatch. So it's a **derivation gap, not an irreducible codec.**

Conclusion: there is currently **zero** forcing case for `encoder`/`decoder` fields. They become warranted only for a codec that resists all three reductions (structural derivation, a discriminator, a small declarative elision/rename delta) — and even then must enter as a *structured* description, never embedded Scala strings, or they just relocate the Scala-leak. One example is a special case; ≥2 irreducible codecs before generalizing a shape. Likely never.

## 5. Consuming the discriminator

With the criterion settled, the loop closed. `discriminatedCodecDeclaration` now derives its wire key:

```
val discriminator = if (td.codecAspect.discriminator.nonEmpty) td.codecAspect.discriminator else "kind"
```

threaded into the three emitted sites — encoder wire key, decoder `downField`, and the decode-failure message. The generated local `val kind` (which holds the tag *value*) keeps its name, so nothing else shifts. The `"kind"` fallback is exact: every discriminated type today has an empty `discriminator`, so every byte of generated output is unchanged — which the 51 byte-for-byte `DracoGenTest` comparisons confirm.

A proof test (`TypeDefinitionTest`) rebuilds the Animal/Dog union with `CodecAspect("species")` on the parent and asserts `"species"` flows into both encoder and decoder with no `"kind"` left behind. Suite **203/203**.

`TypeDefinition` is now five aspects wide, and the fifth is fully live — authored in the canon, consumed in emission. The named next step is the `*Aspect` `elisionCheck` rule from §4: making `TypeDefinition`'s own codec derive, which would delete the sole hand-authored-codec counterexample and prove the "custom codec was a derivation gap" thesis by construction. Held for a separate increment.
