# Draco Dev Journal — Chapter 59

**Session date:** July 8, 2026
**Topic:** Continuing the drake corpus buildout (ch.58 next-session task). Transcript.

---

**[Dev]** Continue the drake corpus buildout

**[Claude]** *(after reproducing the ch.58 worklist loop — 39 JSON defs lacking a `.drake` sibling — reading the template and every missing JSON, then authoring the settled-syntax ones)*

Done. Here's where the corpus buildout landed.

## Authored 33 of the 39 `.drake` files

The corpus is now **56 `.drake` / 62 JSON**. Everything expressible in settled drake syntax is written:

- **draco core (18):** DracoType, Primal, Type, ActorType, RuleType, DomainType, Domain, Rule, TypeDictionary, DomainDictionary, Holon, DomainTransform, TypeTransform, Assembly, Binding, REPL, Main, Test
- **base (10):** Base, Cardinal, Coordinate, Distance, Meters, Nominal, Ordinal, Radians, Rotation, Unit
- **format (1):** Format
- **primes (4):** Accumulator, Numbers, PrimesFromNaturalSequence, RemoveCompositeNumbers (the two rules — first corpus rules to carry `conditions`/`con`)

**Style choices** (recorded in memory): matched the *existing concrete corpus* — `[ ]` brackets, `globalElements`, `par name` without `_` — plus the settled canon (`type→domain→rule` order, bare `type` for DracoType-rooted). All `drake.dlt`-target deltas (indentation-only, `globals`, `par _name`) I deliberately left for the Phase-2a/2b sweep rather than fold a style migration into transcription. First corpus uses of multi-derivation `from` (`from Unit Primal(T)`) and `from` with type-application (`from Holon((S, T))`).

## 6 remain — a syntax-gap tail

| File(s) | Gap |
|---|---|
| **Dictionary, CLI, ContentSink** | `dyn` with parameters + multi-statement body |
| **SourceContent** (+ContentSink) | `mon` inside a factory body |
| **Value, Primes** | multi-line host-code `Monadic` (the genuine out-of-drake tail; Primes is also the primes is-a-domain head) |

**My recommendation:** settle a `dyn`-with-body form (nested `parameters`/`body`, reusing `par`/`mon`/`fix`, symmetric with `factory`), author Dictionary/CLI/ContentSink/SourceContent with it, and scope Value + Primes out as the multi-line-host-code tail. Want me to draft that `dyn`-with-body syntax against Dictionary (the richest case) so you can see it concretely before committing?

*(Wrote 33 `.drake` files; nothing touched Scala/JSON.)*

---

*(Continued in ch.60 — the syntax-gap resolution, the bracket model, and the `Local` discovery.)*
