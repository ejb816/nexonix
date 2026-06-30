# Draco Dev Journal — Chapter 53

**Session date:** June 29, 2026
**Topic:** DRAKE gains newline-break continuation rules, then the first **rule-drake** (`AddNaturalSequence`) lands and — under review — exposes that `RuleAspect` carries its rule LHS in two places at once. The fix to consolidate variables/conditions into `pattern` walks straight into a *pre-existing* codec bug: the discriminated `TypeElement` encoder silently drops subtype-only fields the decoder reads. So the work splits: **(b1)** repair the encoder (proved with a round-trip guard), then **(b2)** do the consolidation across all twelve rule definitions. Both verified green; a stale doc line swept up at the end.

---

## 1. Newline-break continuation rules

The session opened on DRAKE layout:

> **Dev:** I have some tweaks for newline breaks in drake language: newline after commas in sequences`[ a, b, ...]` and after infix numeric and boolean operators in a long concatenated list of operations.

Read as a **continuation convention**: a trailing comma in a comma value-list, or a trailing infix operator (`&&`/`||`/`+`/…) in a long chain, signals "the expression continues on the next line." Clean and unambiguous for a future parser. Applied to the corpus: `BodyElement`'s ten-element `modules [Fixed, Mutable, …]` broke one-per-line; the long `isEmpty` predicates (`DracoAspect` 9 clauses, `RuleAspect` 6) broke after each `&&`; short chains (`DomainAspect` 2) stayed inline. (The Dev later broke `ActorAspect`'s 3-clause one too — the "long" threshold is theirs to set.) Block-level `[ … ]` (elements/parameters) stays newline-separated with no commas, per the existing rule; the comma-break applies only to value-lists. Pure drake-surface — no parser yet, so no JSON/Scala impact.

## 2. The first rule-drake — and a two-homes smell

> **Dev:** Let me know what you think of AddNaturalSequence.drake

`primes/AddNaturalSequence.drake` is the first rule expressed in DRAKE. It introduces `rule` as a third aspect-head (alongside `domain` and `type`, mapping to the JSON's three keys: typeName, ruleAspect, domainAspect), with the LHS/RHS split visible: `pattern { variables }` then `action`. Nice touches: `var` as the Variable-element keyword (distinct from `mut` for Mutable), and `action` dropping the `body [ … ]` wrapper so the RHS reads as a bare statement list.

Two findings under review. The small one, owned by the Dev's own fix mid-review: a body statement was missing its `mon`. The substantive one: the drake nests `variables` under `pattern`, but the JSON keeps `variables` at `ruleAspect` *top-level* (no `pattern` object), and the Generator reads it there. So they disagree — and the disagreement exposed that **`RuleAspect` carries variables/conditions in two places at once** (top-level *and* `pattern.variables`/`pattern.conditions`), which is why its own `isEmpty` checks both. A fork, not a typo:

- **(a)** make the drake match today's JSON (variables directly under `rule`);
- **(b)** take the drake as the better model and consolidate the LHS into `pattern`, retiring the redundant top-level fields.

> **Dev:** Do (b)

## 3. (b) hits a latent codec bug — split into (b1)/(b2)

Tracing the blast radius before touching anything turned up a wall. The discriminated `TypeElement` encoder emits **only parent-trait fields** — `kind/name/valueType/parameters/body/value`. Two subtypes carry fields that live only on the subtype: `Pattern` (variables, conditions) and `Action` (variables, values). The **decoder** reads them per-case; the **encoder never writes them**. A Generator comment even documents it: subtype-specific fields are "omitted (decoded per-case in the decoder)." It had never fired because **no rule populates `pattern` today** — every rule keeps its variables in `ruleAspect.variables`, which `RuleAspect`'s own encoder serializes directly.

(b) routes the rule LHS *through* `pattern` → the lossy encoder → every rule's variables would vanish on encode. So the drake instinct surfaced two things at once: the model redundancy and the codec asymmetry beneath it. Split:

> **Dev:** (b1) first

## 4. (b1) — make the discriminated encoder round-trip subtype fields

A new Generator helper, `subtypeExtraFields`, computes each leaf's factory params not present on the parent trait and appends them to the discriminated encoder per-case:

```text
).flatten ++ (x match {
  case x: Pattern => Seq( …variables…, …conditions… ).flatten
  case x: Action  => Seq( …variables…, …values… ).flatten
  case _ => Seq.empty
})
```

Only `Pattern` and `Action` have extras, so only they get arms. `TypeElement.scala` was regenerated to match, and `TypeDefinitionTest` gained a round-trip guard that builds a `Pattern`/`Action` with populated subtype fields, encodes, and asserts they survive — a test that *fails on the old encoder and passes on the new*. Full suite **200/200**. The fix is the same family as the #28 codec-symmetry work, now extended to subtype-only fields.

## 5. (b2) — the consolidation

With a codec that round-trips `pattern`, the move itself:

- **Generator** — `isRule` and `ruleGlobal` repointed to `td.ruleAspect.pattern.variables/.conditions`.
- **`RuleAspect`** — dropped top-level `variables`/`conditions`; now `pattern, values, action` (pattern-first, LHS→RHS). Rewrote `.json`, `.scala`, `.drake`; `isEmpty` checks `pattern.*` only.
- **Every rule JSON re-nested** — and here a project-wide grep paid off: the obvious three `primes` rules were only part of it. **Eight `src/mods` domain rules** (aerial/marine/ethereal/terrestrial × Consume/Originate) also carried top-level `variables` and are generated by the chain tests — they'd have produced variable-less rules under the repointed Generator. All eight wrapped into `pattern`. `TupleFact.rule.json` was correctly *left alone*: it uses an old flat shape with no `ruleAspect` wrapper, so the change doesn't touch how it decodes. Two `bin/draco-sc` scripts repointed.

Because the variables are the same — merely sourced from `pattern` now — every rule `.scala` regenerates byte-identical. Full suite **200/200**: `DracoGenTest` confirms `RuleAspect.scala` matches the Generator and all twelve rules still regenerate; `PrimesRulesTest` confirms the sieve still executes; `YAMLRoundTripTest` (65/65) now genuinely round-trips `pattern`-bearing rules through the repaired codec.

A stale line in DRACO.md ("rule fields (variables, conditions, values, pattern, action)") was corrected to reflect the new shape. `RuleAspect` is now a clean `pattern { variables, conditions } + values + action`, and the rule LHS has exactly one home.
