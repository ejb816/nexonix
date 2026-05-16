# Draco Dev Journal — Chapter 34

**Session date:** May 15–16, 2026
**Topic:** A strategic redirect away from reference-frames-as-examples toward draco-endogenous improvements, executed by picking #17 (Extensible drift) and purging vestigial `name`/`description` metadata from 7 `draco/base/*` types. 14 files edited (7 JSON + 7 Scala), 1 test file updated, 1 feedback memory rewritten. All 246 tests green after a single test-driven correction on `Unit.scala`.

---

## The redirect

The session opened with a routine pickup question:

> **Dev:** What are the issues to select from for our next activity?

The agent ran `gh issue list --label priority-next` and reported the lone result: **#25 Real Ego perception/effect loop (follow-up to #8)**, carrying `reference-frames` and `next-feature` labels.

The dev's response reframed the whole agenda:

> **Dev:** The reference frames domains are too much of time sink relative to the their usefulness as examples. We can keep them in src/test but we need to focus on improving usability of draco as a dependency for users who want to create their own domains. Pursuing draco-endogenous domains and types to support the usability of draco seems more important than trying creating new domains that nobody understands, as examples of how to create domains, rules and actors.

This was a meaningful priority shift. The reference-frames track had absorbed Increments A/B/C plus much of Stage 2c/2d/2e cascade work, but it wasn't paying off as pedagogical material — Egocentric/Geocentric/Cosmocentric was never going to be the conceptual frame an outside user learns draco through.

The agent saved a new feedback memory (`feedback_focus_endogenous_over_examples.md`) capturing the redirect: prefer `generator`/`tooling`/`docs`/`bootstrap`/`cleanup` issues over `reference-frames`; existing reference-frame code stays in place for now (move to `src/test` eventually, no rush) but is no longer a work target; #25's `priority-next` is stale.

Then the agent re-bucketed the 22 open issues against the new direction:

**Aligned (endogenous/usability):** #11 Generator[L], #17 Extensible drift, #18 YAML round-trip bug, #24 isLeaf, #14 Lean direction, #15 Getting Started, #9 draco-gen discover/verify, #10 sbt skew, plus #12/#13 (Dreams/Orion as larger endogenous architecture).
**Deprioritized:** #25, #19, #16, #7, #5, #4, #3, #2 (all reference-frames).
**Roadmap:** #20–#23.

Recommendation: **#17** as the immediate pickup — small and mechanical, directly tied to the "natural follow-up to the DracoType root shift" note already in memory, and removing `Extensible`-era drift from hand-written core improves how external authors read the codebase. After that, #11 (Generator[L]) as the larger endogenous move.

> **Dev:** yes [to relabeling].

`priority-next` moved from #25 → #17 via `gh issue edit`. (One small wrinkle: a `gh issue list --label priority-next` immediately after returned #25 due to cache lag, but a direct `gh issue view 25/17 --json labels` confirmed the move was correct.)

> **Dev:** Let's start now.

---

## Cold-reading #17

The issue body was short:

> Per `feedback_extensible_placeholder.md`. `Numbers`/`Meters`/`Nominal`/`Accumulator`/`Radians` hand-written code drifts from the canonical Generator pattern. Partly addressed by chapter-31 finish-pass's `comparisonOnlyExcluded` decision (those types are now intentionally hand-maintained), but the pattern of "hand-written holds the slot" should be revisited.

Memory carried two seemingly-conflicting threads: one entry said `Extensible eliminated mid-sweep` (canonicalization sweep, in-progress as of 2026-05-10), another said hand-written `Numbers`/`Meters`/etc. were "now canonical" per the older `feedback_extensible_placeholder.md`. The issue itself implied drift still existed.

The agent went to current code to resolve the conflict directly. `Extensible.scala` indeed doesn't exist anymore. But all 9 hand-written files mentioned in memory (Primal/Holon/TypeName/TypeElement plus the 5 issue-named ones plus Cardinal/Rotation/Ordinal/Unit transitively) still existed. Reading them showed:

- **Not drifting** (already canonical): `Primal.scala`, `Holon.scala`, `TypeElement.scala`
- **Drifting**: the other 7 in `draco/base/` — `Unit`, `Cardinal`, `Rotation`, `Ordinal`, `Nominal`, `Meters`, `Radians`

The drift had two distinct flavors when checked against the canonical reference (`Distance.scala`, which was already-canonical generator output):

1. **Companion shape**: missing `with DracoType` on `object`, missing `override` on `typeDefinition`, missing `domainType: Domain[Base]`. Pure shape drift.
2. **Vestigial trait fields**: `override val name: String = "X"` and `override val description: String = "..."` overrides inside the trait body.

The `comparisonOnlyExcluded` map in `DracoGenTest.scala` documented all 7 of these as intentionally excluded, with reasons in bucket (a) "Hand-written has Scala-only logic" or bucket (b) "Hand-written carries vestigial name/description constants; not load-bearing."

A grep across `src/` for `Unit.name`, `Cardinal.name`, `Meters.description`, etc. returned **zero hits**. Pure dead vestigial metadata. The chapter-31 exclusion comment had said the constants "the Scala source uses for self-description" — but nothing actually consumed them. Self-description for an audience of zero.

Then the JSON side: reading `Unit.json`, `Cardinal.json`, `Meters.json` showed `name`/`description` declared as `Fixed` elements with no `value`. So Generator was emitting abstract `val name: String` declarations and the hand-written code was supplying concrete defaults. The drift was the concrete-defaults layer.

Per `feedback_haskell_test.md` — would a Haskell port carry these constants? They're not consumed; the answer is no. Per `feedback_json_authoring_surface.md` — JSON authors type *shape*, but `name`/`description` Fixed elements with no value aren't really shape; they're declaration-only metadata that nothing reads.

The recommendation crystallized: **purge them entirely**.

---

## The recommendation and the scope agreement

> **Agent:** Recommendation: Option 1 — full purge.
> 1. JSON: remove the `name` / `description` Fixed elements from these 7 files [...]
> 2. Scala: remove the `val name`/`val description` declarations & overrides from the matching 7 .scala files; align their companion shapes (`extends App with DracoType`, `override lazy val typeDefinition`, add `domainType: Domain[Base]`)
> 3. DracoGenTest: drop the 7 `draco/base/*` entries from `comparisonOnlyExcluded` (they should now pass per-type comparison)
> 4. Leave alone `Numbers`, `Accumulator`, `Primes`, `YAML` — those have legitimate Scala-only logic [...]
> 5. Update `feedback_extensible_placeholder.md` since "Extensible holds the slot" is moot — Extensible is gone, and the surviving drift is just dead metadata
> This will touch 15 files (7 JSON + 7 Scala + 1 test). Sound right, or do you want to narrow the scope?

> **Dev:** Do the changes to the 15 files.

---

## Execution

Before charging in, one more lookup — what does the JSON encoder do when `dracoAspect` becomes empty? `Distance.json` has `dracoAspect: { derivation: [...] }` but no `elements`; `Unit.json` after the purge would have nothing left in `dracoAspect` at all. The decoder reads `dracoAspect` as `Option[DracoAspect]` with `DracoAspect.Null` default, and the encoder emits the field only when `!DracoAspect.isEmpty`. So an empty `dracoAspect` should be omitted entirely. That matched the JSON pattern.

JSON edits proceeded straight through:
- **Unit.json**: removed the entire `dracoAspect` block (only contained the two now-deleted elements)
- **Cardinal/Rotation/Ordinal/Nominal**: removed the `elements: [...]` block, kept the surrounding `derivation: [...]`
- **Meters/Radians**: removed the `elements: [...]` block, kept `derivation` + `factory`

Scala edits used the canonical `Distance.scala` as the companion-shape template:

```scala
object Distance extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Distance", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Distance[_]] = Type[Distance[_]] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
```

For Meters/Radians (the two with factories), the agent transcribed `Monadic`'s pattern from `TypeElement.scala` — multi-line `apply(...)`, `lazy val` overrides, and a trailing `Null` companion val. The numeric default `_value = 0.0` was a best guess (no canonical single-numeric-param leaf existed in the codebase to crib from); the test would tell us if Generator emitted something different.

The 7 `draco/base/*.json` entries came out of `comparisonOnlyExcluded` in `DracoGenTest.scala`, leaving only the 4 types with genuine Scala-only logic (Primes/Numbers/Accumulator/YAML).

Finally, `feedback_extensible_placeholder.md` got a full rewrite to reflect the new state: "Extensible no longer exists. [...] 'Hand-written holds the slot' now applies *only* to types with genuine Scala-only logic that doesn't translate cross-language." MEMORY.md's one-line index entry updated to match.

The agent flagged the one uncertainty up front:

> **Agent:** One uncertainty heads-up: for Meters and Radians I guessed `_value = 0.0` as the Null default for Double, and used multi-line apply + lazy val per the canonical TypeElement.scala pattern. If Generator emits something different (e.g. 0.toDouble, or a single-line factory for one-param), DracoGenTest will fail with a precise diff — paste it back and I'll align.

---

## The one test failure

`sbt test` returned 245/246. The one failure was `Unit`:

```
hand-written                          | generated
trait Unit extends DracoType          | trait Unit
object Unit extends App with DracoType| object Unit extends App {
  override lazy val typeDefinition... |   lazy val typeDefinition...
```

The diff was clean and informative. For a type with **no derivation in JSON** (Unit is a root with nothing to derive from), Generator emits:

- `trait Unit` — bare, no `extends DracoType`
- `object Unit extends App` — no `with DracoType` mixin
- `lazy val typeDefinition` — no `override` keyword (there's nothing to override)

This is consistent: `DracoType` only enters the picture via derivation. Unit, having none, doesn't get the mixin. Cardinal inherits DracoType-hood through Primal in its derivation list, so the cascade still works downstream — but Unit itself is a bare trait.

`Unit.scala` rewritten to match:

```scala
trait Unit

object Unit extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Unit", _namePackage = Seq ("draco", "base")))
  lazy val dracoType: Type[Unit] = Type[Unit] (typeDefinition)
  lazy val domainType: Domain[Base] = Domain[Base] (typeDefinition)
}
```

> **Dev:** passed

246/246. Including — and this surprised the agent on the upside — the `_value = 0.0` Null defaults for both Meters and Radians. The Monadic pattern transcription was correct end-to-end on the first try; the only failure was the derivation-less-trait edge case.

---

## What the workflow taught

### Recap of the actual decision

The "should we revisit 'hand-written holds the slot'" question from the issue body had a clear answer once the actual code state was checked. The five named types split cleanly into two groups:

- **Pure metadata drift** (Meters/Radians/Nominal — plus the four tangential parents Unit/Cardinal/Rotation/Ordinal): dead `name`/`description` constants serving no one. Solution: purge.
- **Legitimate Scala-only logic** (Numbers/Accumulator — plus Primes/YAML from elsewhere): genuine customization that doesn't cross-language port. Solution: leave alone; the chapter-31 `comparisonOnlyExcluded` decision stands.

The convention statement that emerges: **hand-written holds the slot only when the slot contains something Generator can't produce.** Self-description constants aren't that; LazyList helpers and mutable-set factories are.

### Generator's emission rule for derivation-less traits

This wasn't documented anywhere before today, but it's now clear:

- **Has derivation** → `trait X extends <head> with <tail...>`, `object X extends App with DracoType { override lazy val typeDefinition ... }`
- **No derivation** → `trait X`, `object X extends App { lazy val typeDefinition ... }`

The `override` shows up because `DracoType` declares an abstract `typeDefinition: TypeDefinition`, so anything `extends`-ing it must override. A bare trait inherits no such obligation. The companion object only mixes in `DracoType` when the trait itself does.

That's also why `Unit.scala` doesn't have the `with DracoType` chain on its companion — it would have nothing to be overriding.

### The redirect was the right move

The first concrete fruit of the redirect ("focus on draco-endogenous over examples") was a clean 14-file purge that brought 7 hand-written types into Generator-emission equivalence, dropped 7 exclusions from the test, and replaced a stale convention memory with an accurate one. Small, surgical, and immediately improves how an external draco user reads `draco/base/*`. Compare to spending another session polishing Egocentric (issue #25) — which would have improved nothing for anyone outside this project.

The redirect costs less than expected: existing reference-frame code stays where it is (no rip-out). The cost is just *not adding more*.

---

## Status at close

- **Closed**: (pending) #17 Extensible drift cleanup
- **`priority-next`**: unclaimed (was on #17 during execution)
- **Tests**: 246/246 green
- **Files touched** (15 + 2 memory):
  - JSON: `Unit.json`, `Cardinal.json`, `Rotation.json`, `Ordinal.json`, `Nominal.json`, `Meters.json`, `Radians.json`
  - Scala: `Unit.scala`, `Cardinal.scala`, `Rotation.scala`, `Ordinal.scala`, `Nominal.scala`, `Meters.scala`, `Radians.scala`
  - Test: `DracoGenTest.scala` (-7 `comparisonOnlyExcluded` entries)
  - Memory: `feedback_extensible_placeholder.md` (rewritten), `feedback_focus_endogenous_over_examples.md` (new)
- **Suggested next pickup**: per the redirect — #11 (`Generator[L]`) as the larger endogenous move, or smaller wins #24 (`isLeaf`) / #18 (YAML round-trip bug) / #9 (`draco-gen discover`/`verify`).

Natural commit message: `Purge vestigial name/description from 7 draco/base/* types (closes #17)`. The default branch is now `main` (switched in chapter 33), so the `closes #17` footer should auto-fire on push.
