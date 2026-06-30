# Draco Dev Journal — Chapter 52

**Session date:** June 28, 2026
**Topic:** The metamodel-in-DRAKE corpus is completed — `TypeDefinition` and the five aspects (`Aspects`, `DracoAspect`, `DomainAspect`, `RuleAspect`, `ActorAspect`) are authored. Along the way three notation questions get settled by the Dev, each tightening DRAKE toward "transparent surface, nothing host-specific": `from DracoType` is elided (the root is the default), `isEmpty` is reclassified `Monadic`→`Fixed` (a named predicate, not an effect hatch) and the normative JSON is corrected to match, and — after a genuine detour through Haskell expression syntax — the triple-quote multi-line delimiter is rejected, which logically expels `TypeDefinition`'s circe encoder/decoder from drake entirely. Closed with the JSON/Scala correction verified at DracoGenTest 103/103.

---

## 1. Picking up — TypeDefinition + the aspects

The queued next step from ch.51 was the last of the metamodel corpus: `TypeDefinition` and the aspect family, the types that introduce the `globalElements` block (absent since Path B stripped it from `TypeName`) and the `isEmpty` predicates. I opened with `DomainAspect.drake` as the simplest concrete proposal — and, mirroring the JSON faithfully, wrote it with `type DomainAspect from DracoType` and the `isEmpty` predicate as a triple-quoted `mon`.

Both choices were immediately corrected by the Dev. Faithful-to-the-JSON turned out to be the wrong default twice over.

## 2. The root is the default

> **Dev:** Every type is rooted in DracoType, so why does it have to be explicit in drake?

Right. `DracoType` is the universal root, so `from DracoType` carries no information — it should be elided exactly like an empty derivation, and `from` should earn its place only when the parent is something *other* than the root (`from Primal(String)`, `from Aspects`). This also dissolves a corpus inconsistency: `TypeName.json` has no derivation key, the aspect JSONs have `derivation: [DracoType]` — both now render as a bare `type X` line, and the compiler supplies the root. `DomainAspect.drake` lost its `from`.

## 3. isEmpty is a Fixed, not a Monadic

> **Dev:** Why did you choose Monadic for the BodyElement named isEmpty? Except for the "lazy" it should have been Fixed.

The sharper correction, and entirely mine to own — inherited faithfully from a JSON model that was itself wrong. `isEmpty` is a *named val binding* (name `isEmpty`, type `DomainAspect => Boolean`, value the lambda) — that is precisely `Fixed` (`val name: Type = value`). `Monadic` is the *nameless, `Unit`, effectful* escape hatch — the Dev's own framing from ch.51 ("an escape hatch for operational expressions"). Cramming `lazy val isEmpty: … = …` into a Monadic `value` string was abusing the hatch to smuggle in the binding keyword.

And the "lazy" is no obstacle: the Generator already emits globalElements `Fixed` vals as `lazy val`. So `Fixed(isEmpty, DomainAspect => Boolean, "da => …")` regenerates `lazy val isEmpty: DomainAspect => Boolean = da => …` — identical output, correctly modeled. The drake became `fix isEmpty (DomainAspect => Boolean) da => …`, the function type wrapped in parens so its `=>` stays distinct from the value's `=>`.

## 4. The Haskell detour

The Dev then probed the modeling from an unexpected angle:

> **Dev:** Can you show me what it would look like of the scala syntax in fix isEmpty was converted to Haskell? Don't do it in the file just show me.

```haskell
isEmpty :: DomainAspect -> Bool
isEmpty da = null (name (typeName da)) && null (elementTypeNames da)
```

The instructive fallout: **`lazy` vanishes** (Haskell is lazy by default — confirming laziness is a host-emission detail, not a model property), field access inverts (`da.typeName.name` ↦ `name (typeName da)`), and `.isEmpty` ↦ `null` (a stdlib vocabulary mapping). Nothing in the element is Scala-specific at the model level. I read this as evidence for the `Fixed` reclassification — but the Dev was aiming somewhere larger:

> **Dev:** I'm not suggesting generating Haskell, I'm considering Haskell EXPRESSION syntax as part of drake

A real design fork. The argument *for*: drake already chose Haskell for its **type** notation (`[T]` list, `{T}` set, `(A,B)` tuple — ch.51), so the expression bodies being Scala is the one inconsistency left; going Haskell-for-expressions finishes the job and makes the authoring surface genuinely host-neutral (the Scala-isms `=>`/`Boolean`/method-chains become Generator rendering). It also gives the `Monadic` register a principled home — do-notation over the monads the Dev named in ch.51 (working memory, Accumulator). The cost is honest: the Generator stops passing value strings through verbatim and becomes a real **expression translator**, and the corpus needs re-authoring.

Then the Dev supplied the counterweight:

> **Dev:** Not yet. I might want to consider how Draco users might be more familiar with the OO value reference notation than the more pure function composition notation.

This is the decisive point for an *authoring surface*. Subject-first dot-navigation (`da.typeName.name.isEmpty`) is the dominant idiom across Java/Scala/Python/JS/C#; prefix composition reads inside-out and is native only to FP users. But the tension dissolves once you see that **the dot isn't inherently OO** — `a.b.c` and `c(b(a))` are duals, differing only in reading direction. So drake can keep the familiar left-to-right navigation as its surface while defining `.` as its own host-neutral projection operator; the Generator still renders it either way (Scala `da.typeName.name` or Haskell `name (typeName da)`). Familiarity and host-neutrality were never actually in conflict. The Haskell exploration earned its keep by *proving the dot is translatable* (hence neutral), not by proposing users write prefix.

> **Dev:** Let's keep going with the scala expression syntax. You have reassured me of its viability as drake expression syntax.

## 5. Authoring the rest — and the triple-quote's quiet death

With direction reaffirmed, the four remaining aspects went in using only settled notation — `fix isEmpty` as a single-line `&&` chain, no multi-line delimiter needed. That left `TypeDefinition`, whose `encoder`/`decoder` are genuinely multi-line (`implicit lazy val` circe blocks — `Fixed` carries `lazy` but not `implicit`). I proposed the triple-quote `mon """…"""` for exactly those.

> **Dev:** No triple quote please. I prefer the unquoted language neutral drake expression syntax.

This has a sharp logical consequence. The `fix isEmpty` predicates are *already* unquoted language-neutral expressions, so they stand. But the encoder/decoder **cannot** be both unquoted and language-neutral — they're raw circe plumbing (`Encoder.instance { … Json.obj(fields: _*) }`), full of `[ ] { } "` and `_*` that would collide with drake's own structural tokens the instant you unquote them. The triple-quote was the *only* thing letting them sit in drake; the quote was hiding the fact that **they aren't drake expressions, they're host residue.** So rejecting the quote rejects the blobs: I pulled the encoder/decoder out of `TypeDefinition.drake`, leaving it elements + factory only.

The principle this settles, recorded in memory: **drake's value surface carries only unquoted, language-neutral expressions. Anything that can't be written that way isn't a drake expression — it's host plumbing, and it belongs to the Generator.** The field-elision codec (emit each aspect only when `!isEmpty`, default to `Null` on decode) is fully mechanical and structure-derivable; retiring the two hand-carried `Monadic` blobs from `TypeDefinition.json` into a Generator-derived codec is deferred follow-up **(3)**. Until then `TypeDefinition.drake` runs ahead of its JSON.

## 6. Correcting the normative JSON — verified

> **Dev:** Do (2) now

"(2)" being: make the `isEmpty` reclassification real in the canonical JSON, not just the drake corpus. Per aspect, the `globalElements` entry flipped `Monadic`(`Unit`, value `lazy val isEmpty: … = …`) → `Fixed`(name `isEmpty`, valueType `<X>Aspect => Boolean`, single-line value `… => …`) across `DomainAspect`/`DracoAspect`/`RuleAspect`/`ActorAspect` (`Aspects` has no predicate).

The subtlety that made this safe: the two Generator emission paths differ. `Monadic` indents every value line by two spaces; `Fixed` inlines the value verbatim after `lazy val name: type =`. Since the drakes authored each predicate single-line, the JSON `Fixed` value is single-line too — which both matches the drake and sidesteps any multi-line indentation mismatch. `DracoGenTest`'s `normalize` preserves line breaks (it only trims trailing/blank-line whitespace), so a 2-line `Monadic` block and a 1-line `Fixed` block are genuinely different to the test. With no sbt in the assistant's hands, I hand-applied the deterministic 1-line `Fixed` output to the four `*Aspect.scala` files so the test would *verify* the edit rather than merely demand a regen.

It did: **DracoGenTest 103/103.** Every JSON parses, every Generator output matches its committed Scala whitespace-normalized, `TypeDefinition` included (its unchanged Monadic codecs still round-trip). The metamodel-in-DRAKE corpus is complete; the five aspects round-trip drake↔JSON in spirit, `TypeDefinition` pending (3).
