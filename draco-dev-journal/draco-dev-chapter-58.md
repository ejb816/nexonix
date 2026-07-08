# Draco Dev Journal — Chapter 58

**Session date:** July 7, 2026
**Topic:** A design discussion, not a code change (beyond `drake.dlt`). Starting from the codec "which default do I want" question, the Dev drew out two general principles — **presence** (absent vs present-empty vs present-full) and **inference** (explicit vs derivable) — that now govern aspects and fields uniformly. Several of my framings were corrected along the way. The whole model is scoped as the target for the future typed **Generator[L]**; the procedural `draco.Generator` is left unchanged.

---

## 1. The seed: "present-empty must not equal absent"

Draco elides empties everywhere — an empty field is omitted on encode, defaulted on decode — so JSON can't distinguish *deliberately empty* from *never provided*. That's lossless only when the two mean the same thing. **Codec was the canary**: `encoder: {}` ("give me the default encoder") is a positive instruction, distinct from no `encoder` key. The Dev: "In general I don't want present and empty to be the same as absent."

Sharpened to three states — **absent** / **present-empty** / **present-full** — the principle is: preserve the distinction wherever it carries meaning; elide only where present-empty and absent genuinely coincide. Encoded in `drake.dlt` as two markers: `*` presence-significant (present-empty != absent), `~` elidable (present-empty == absent), combinable as `*~`.

## 2. Two corrections that reshaped it

I proposed an **intrinsic/extrinsic** split (codec "computed by default" / a modifier; rule/actor "opt-in") and generalized "selector" to the actor. Both were wrong, and the Dev's corrections were the real content:

- **Actor `start`/`message`/`signal` are not selectors.** Default behaviour is generated regardless of presence; presence only *overrides*. So they're default-when-omitted → elidable (`*~`).
- **Codec is not computed-by-default.** "Absent codec means no codec needed for that type. Present codec means encoder or decoder may be absent but not both." So codec is opt-in like everything else; its `encoder`/`decoder` are true **selectors** — a present codec generates exactly the sides present, an omitted side is *suppressed* (not defaulted), and >=1 side is required.

The payoff: the aspect-level marker **derives** from field behaviour instead of being stipulated —
- fields *default-when-omitted* (rule/actor) → present-empty aspect = a fully-defaulted facet != absent → `*`
- fields are *selectors* (codec sides; suppress-when-omitted, >=1 required) → present-empty aspect = nothing == absent → `*~`

Nothing is asserted; each layer forces the next. My "intrinsic/extrinsic" story was discarded — every facet is simply opt-in.

## 3. The inference principle

The Dev then resolved the declared-vs-inferred codec question by a second principle: **an aspect must be explicit iff its json can't be reconstructed from information already present.**

- `rule`, `actor`, `codec` — never inferred; explicit in drake **and** json (irreducible authored intent: a rule's logic, an actor's behaviour, the decision to serialize and how).
- `draco` — authored as the drake structure; its json is a projection of that.
- `domain` — **inferable from `TypeName`** (namePackage/name already fix membership: `draco.base.Meters` → `Base`).

This settles codec: **declared, never inferred** → the structural codec gate built in ch.57 (`ownElementNames.nonEmpty` + `inheritedElementNames`) is, for the target, retired in favour of an explicit `codec` aspect on each serializing type. Presence and inference interlock: the inferable aspects (`draco`/`domain`) are always determinable, so **presence-significance lives entirely in the explicit opt-in aspects** (`rule`/`actor`/`codec`) — exactly where the `*` vs `*~` distinction sits.

## 4. Scope: all of it is Generator[L]'s

The Dev's closing directive: *whatever is still open stays open until `Generator` becomes a domain type parameterized by target language, with per-target operation rules.* So the presence model, the declared-codec migration, and the `encoder`/`decoder` subelement types (`Encoder`/`Decoder`/`EncoderField`/`DecoderField`, sketched as `as`/`when`/`value` and `from`/`default`) are **deferred to the typed Generator[L]** — see [[project_generator_evolution]] / [[project_generator_permanence]]. Custom override codecs aren't needed "for a while."

Decisions for the **current procedural Generator: none required.** It and the JSON corpus stay coherent (195/195). `discriminator` stays a **field** (default "kind") — per the inference rule it's a free tag choice, not reconstructable, so it must be explicit; it already is, so zero change. `drake.dlt` now carries the full target model with `NOTES`/`DIVERGENCES` marking every deep item "deferred to Generator[L], not pending on the procedural Generator."

No tests run — the only artifact touched was `drake.dlt` (hand-authored design surface, no parser; [[feedback_drake_is_syntax_discovery]]).
