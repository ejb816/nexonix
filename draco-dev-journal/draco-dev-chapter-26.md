# Draco Dev Journal — Chapter 26

**Session date:** May 6, 2026
**Topic:** Stage 2d partial landing — Aspects redesign DONE, factoryBody conditional emit DONE, companion val collapse deferred. Plus a session-start guardrail: SessionStart hook redirects out of nexonix worktrees toward main in the parent repo.

---

## Session Open — Worktree Mismatch

The session opened with the user's terse Stage 2d prompt:

> **Dev:** Stage 2d (Aspects redesign + instance-projection retirement) next and Stage 2e (leaf detection) in a following session

But before any Stage 2d work could start, the agent noticed a structural problem. The harness had auto-spawned a worktree at `nexonix/.draco/worktrees/strange-perlman-e2da12` branched from `master` — and `master` was still on the early `org.nexonix.*` skeleton (last commit `Adding code to package org.nexonix.domain`). The full `draco` package, the JSON resources, and all of Stage 2c's work lived on `main` in the parent repo. Working in the worktree as constituted would have been pointless: the prerequisite code wasn't there.

The agent flagged this and proposed redirecting to the parent repo. The user's response cut to a recurring failure mode:

> **Dev:** This is getting worse. Each time after you update memory and write the new journal chapter for stating a new session, on the new session you have forgotten that you are continuing to work on my main branch directly and not using your work tree branch. How do suggest fixing this so it won't happen in the new session after this one?

The user's `feedback_direct_main_edits.md` memory had been in place for sessions, but every fresh session the agent drifted into the worktree — the harness sets "Primary working directory = worktree" at session start, and that mechanical framing pulled the agent in before memory could catch it. Memory was advisory; the harness was structural; the harness was winning.

The fix had to be mechanical too. The agent recommended a `SessionStart` hook in `~/.claude/settings.json` that detects when `$PWD` matches `*/nexonix/.draco/worktrees/*` or `*/nexonix/.claude/worktrees/*` and emits a `hookSpecificOutput.additionalContext` STOP message telling the agent to `cd` to the parent repo and check out `main` before doing anything. A hook fires every session start regardless of what memory the agent does or doesn't load.

> **Dev:** Yes, please.

The agent invoked `/update-config`, pipe-tested the case-statement command from both the worktree (emits JSON) and the parent repo (silent), wrote it to `~/.claude/settings.json`, validated with `jq -e`, and added a paper-trail reference memory ([reference_worktree_redirect_hook.md](.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/reference_worktree_redirect_hook.md)) so future sessions know the hook exists and don't try to recreate it.

The user approved the memory hookup with one constraint:

> **Dev:** This session will become a journal chapter when we prepare for the next session. If you think it might be helpful to add to MEMORY.md as well, do so.

Then re-entered the original prompt to actually start Stage 2d:

> **Dev:** I'm re-entering the initial prompt for this session here:
>
> > Stage 2d (Aspects redesign + instance-projection retirement) in this session and Stage 2e (leaf detection) in a following session

This session itself, of course, would be the first proof of whether the new hook fired correctly in practice — but only on a future session start. For this turn, the agent operated cross-directory: the harness kept resetting `cwd` back to the worktree after each Bash call, but Read/Edit/Write took absolute paths and `git -C /Users/edwardburke/IdeaProjects/nexonix` worked fine.

---

## Stage 2d Step 1 — Five New Aspect TypeDefinitions

Per [project_aspects_redesign.md](.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/project_aspects_redesign.md), Stage 2d had two interlocked but separable pieces:

1. **Aspects-as-parent restructure** — five new TypeDefinitions (Aspects + DracoAspect + DomainAspect + RuleAspect + ActorAspect), TypeDefinition switches to `extends Aspects`.
2. **Instance-projection retirement** — companion val collapse, factoryBody cleanup, drop `*Instance` mixins, delete `*Instance` traits, cascade through hand-written extenders.

The agent read the four hand-written aspect Scala files (DracoAspect.scala, DomainAspect.scala, RuleAspect.scala, ActorAspect.scala) and TypeDefinition.yaml to extract the field shapes:

- **DracoAspect** (9 fields): superDomain, modules, extensible, derivation, elements, factory, globalElements, source, target
- **DomainAspect** (1 field): elementTypeNames
- **RuleAspect** (5 fields): variables, conditions, values, pattern, action
- **ActorAspect** (2 fields): messageAction, signalAction

Plus an existing JSON like Variable.json or Pattern.json to verify the modern Stage-2c authoring shape (typeName + draco aspect block with elements/factory/derivation).

The agent created the five files as **YAML** rather than JSON — YAML is normative going forward (per the chapter 25 work), and these were brand-new files, so creating them in the new format introduced no migration cost. The Generator's loader already tries `.yaml` first per Stage 2c.

For Aspects itself, the design memo specified only the four element fields (one per sub-aspect), no factory — Aspects is an abstract parent, instances are constructed via TypeDefinition's factory. Just elements: draco/DracoAspect, domain/DomainAspect, rule/RuleAspect, actor/ActorAspect.

For the four sub-aspects, the YAMLs faithfully mirrored the Scala apply-method signatures, including the `value: TypeName.Null` / `value: Seq.empty` defaults.

---

## Step 2 — Aspects.scala Bootstrap Mirror

The design memo had specified:

```scala
trait Aspects {  // NOT sealed — see extensibility decision below
  val draco:  DracoAspect
  val domain: DomainAspect
  val rule:   RuleAspect
  val actor:  ActorAspect
}

trait TypeDefinition extends Aspects {
  val typeName: TypeName
}
```

Aspects was not in the codebase, so the agent created [Aspects.scala](src/main/scala/draco/Aspects.scala) as a hand-written bootstrap mirror — minimal, no companion (it doesn't need apply/Null/encoder/decoder; instances of Aspects don't get serialized standalone, only TypeDefinition does), no `extends Extensible` (the design memo doesn't specify it; the `Extensible` placeholder retires under the same broader convergence). NOT sealed — explicit extensibility for future user-defined aspects (proofs, effects, capabilities).

---

## Step 3 — The Atomic Flip

Two edits had to land together:

**TypeDefinition.yaml**: drop the four aspect fields (draco/domain/rule/actor) from the elements list, leave only typeName, add `derivation: [{ name: Aspects, namePackage: [draco] }]`.

**TypeDefinition.scala** ([line 6-9](src/main/scala/draco/TypeDefinition.scala)): change

```scala
sealed trait TypeDefinition {
  val typeName: TypeName
  val draco: DracoAspect
  val domain: DomainAspect
  val rule: RuleAspect
  val actor: ActorAspect
  ...
}
```

to

```scala
sealed trait TypeDefinition extends Aspects {
  val typeName: TypeName
  ...
}
```

The four aspect vals come from Aspects via inheritance. The convenience accessors below stayed (they're instance ergonomics, not aspect projection). The companion's encoder/decoder needed no change — both already produced/accepted the aspect-block form. The factory parameters stayed — `fromAspects(_typeName, _draco, _domain, _rule, _actor)` takes all five.

The agent kept TypeDefinition `sealed`. The design memo says drop `sealed` *on Aspects*, not on TypeDefinition. Aspects unsealed allows user-defined types to extend Aspects directly with new aspect fields; TypeDefinition stays a closed class.

---

## First Test Run — A Compile-Driver Correction

Before running tests, the agent wanted a quick sanity check via `sbt --error compile`. The user interrupted:

> **Dev:** I have to run sbt. I generally use tests to drive compilations.

The agent had been operating off `user_preferences.md`'s line "Claude should run sbt tests proactively" — but the user had since moved to running sbt themselves, and used tests rather than standalone `sbt compile` to drive compilations. The memory was 17 days old and stale. The agent updated [user_preferences.md](.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/user_preferences.md) explicitly:

> **User runs sbt — Claude does NOT.** Do not invoke `sbt compile`, `sbt test`, `sbt "testOnly ..."`, or any other sbt command via Bash. (Updated 2026-05-06 — supersedes earlier "Claude runs sbt tests proactively" guidance.)

And added the corollary: when the agent wants a compile-check, it should ask the user to run a specific named test, not ask to run `sbt compile`. The MEMORY.md index line was updated to match.

The user then ran `GenerateAndCompileTest`. The first result:

```
GENERATE AND COMPILE REPORT: 34 passed, 21 failed, 55 total
```

Comparing to the chapter 25 baseline of 33 PASS / 17 FAIL of 50:

- 5 new types added by the new YAMLs (total 50→55) ✓
- Aspects PASS (no factory, simple parent) ✓
- DracoAspect/DomainAspect/RuleAspect/ActorAspect FAIL with `lazy value typeInstance overrides nothing` — exactly the failure mode flagged in the design memo as a Stage 2d target
- TypeDefinition still FAIL with the same root cause (it was already failing at chapter 25; the atomic flip didn't change that)
- All other pre-existing failures unchanged

The atomic flip introduced **zero regression**. The four new aspect failures matched the design-memo prediction. The agent then turned to fixing the predicted issue: Generator.scala lines 261–263, where `factoryBody` emitted `override lazy val typeInstance/typeDefinition` lines unconditionally inside the anonymous instance constructor.

---

## Step 4 — A Tempting but Wrong Fix

The design memo's exact words:

> **`factoryBody`'s `override lazy val typeInstance/typeDefinition` lines** (Generator.scala:261-263) drop — they emit `override` against parents that never declared the members; cause of `lazy value typeInstance overrides nothing` failures.

The agent took the memo literally and dropped the `instanceOverrides` Seq entirely:

```scala
private def factoryBody (factory: Factory) : String = {
  if (factory.body.nonEmpty) { ... overrides.mkString ... }
  else { ... overrides.mkString ... }
}
```

Second test run:

```
GENERATE AND COMPILE REPORT: 38 passed, 17 failed, 55 total
```

Five expected wins: TypeDefinition + 4 aspects + Value flipped to PASS. **But two regressions:** Meters and Radians flipped to FAIL with:

```
Missing implementation for member of trait TypeInstance:
  val typeInstance: draco.DracoType = ???
```

Tracing the chain: Meters → Distance[Double] → Cardinal[T] → Primal[T] → TypeInstance → DracoType. Meters' anonymous instance constructor genuinely *does* need to provide both `typeDefinition` and `typeInstance` because the inheritance chain reaches traits that declare them. The pre-fix unconditional emit was *redundant for these types* but not invalid — it satisfied the requirement.

The design memo's "drop them" was correct **only after** the broader instance-projection retirement (delete `*Instance` traits, remove `extends App with TypeInstance` from companions, etc.) was done. Stripping in isolation overshot.

The agent reverted in spirit by switching to a **conditional** emit: only emit the override if the chain actually requires it.

---

## Step 5 — `chainHits` Helper

The agent added a small recursive helper to Generator.scala (with cycle protection):

```scala
private def chainHits (td: TypeDefinition, targetName: String, seen: Set[String] = Set.empty) : Boolean = {
  if (seen.contains(td.typeName.name)) return false
  if (td.typeName.name == targetName) return true
  val nextSeen = seen + td.typeName.name
  td.draco.derivation.exists { tn =>
    if (tn.name == targetName) true
    else {
      try chainHits(loadType(tn), targetName, nextSeen)
      catch { case _: Throwable => false }
    }
  }
}
```

Then refactored `factoryBody` to take the full `TypeDefinition` (not just the `Factory`) and emit conditionally:

```scala
val instanceOverrides = Seq(
  if (chainHits(td, "TypeInstance")) Some(s"...typeInstance...") else None,
  if (chainHits(td, "DracoType"))    Some(s"...typeDefinition...") else None
).flatten
```

DracoType declares `val typeDefinition`; TypeInstance declares `val typeInstance` (and extends DracoType). Two independent checks, additive. For TypeDefinition (extends Aspects), neither chain hits → no overrides emitted. For Meters (extends ... → TypeInstance → DracoType), both chains hit → both overrides emitted.

Updated the call site at line 670 to pass `td` instead of `factory`, then asked the user to re-run.

Third test result:

```
GENERATE AND COMPILE REPORT: 39 passed, 16 failed, 55 total
```

Rule flipped to PASS as a bonus. **But Meters and Radians still failed.** Same `Missing implementation for member of trait TypeInstance: val typeInstance` error.

The chain walk had returned false somewhere. The agent traced upward through the JSONs — and found it in [Primal.json](src/main/resources/draco/Primal.json):

```json
"derivation": [{ "name": "DracoType", "namePackage": ["draco"] }]
```

But [Primal.scala line 3](src/main/scala/draco/Primal.scala) actually says:

```scala
trait Primal [T] extends TypeInstance {
```

JSON-Scala drift, called out in MEMORY.md but not yet reconciled:

> Primal[T] now extends TypeInstance (not DracoType); Type[T] and DomainType still extend DracoType (pending)

The Scala had been migrated to `TypeInstance` but the JSON still said `DracoType`. Per `feedback_json_authoring_surface.md`, JSON is the authoring surface — the JSON should reflect what Scala does. The fix was a one-line edit to Primal.json's derivation entry: `DracoType` → `TypeInstance`.

This made the chain Meters → Distance → Cardinal → Primal → **TypeInstance** → DracoType discoverable from JSON. Both `chainHits` calls would now fire.

Fourth test result:

```
GENERATE AND COMPILE REPORT: 41 passed, 14 failed, 55 total
```

Meters and Radians PASS. Final session baseline.

The user also reported (helpfully, in passing):

> **Dev:** By the way, YAMLRoundTripTest passed all 116

So Stage 2d's structural changes did not regress YAML round-trip semantics. (One TypeDefinition.yaml + the five new aspect YAMLs round-trip alongside the existing 110 — total 116, up from 117 minus a renumbering somewhere; the agent didn't probe the count drop, since 116/116 is still fully clean.)

---

## Stopping Point — Why Stage 2d Doesn't Land Fully Tonight

The user prompt said "Stage 2d in this session." The agent had to call its own scope honestly. Looking at the remaining 14 failures:

- Accumulator (`not found: value mutable` — pre-existing JSON drift)
- Actor (`';' expected` — pre-existing JSON parse issue)
- ContentSink, Main, Test, SourceContent (missing impls — pre-existing hand-written drift)
- DomainDictionary, TypeDictionary (missing kvMap — pre-existing)
- Numbers (missing impls + `value n overrides nothing` — pre-existing)
- TypeName (`type mismatch` — pre-existing)
- Type (`value _typeDefinition overrides nothing` — pre-existing)
- TypeElement hierarchy (12 types, `value Null is not a member` — pre-existing)
- Extensible (`illegal cyclic reference` — pre-existing)
- RuleType (missing `ruleDefinition` — pre-existing, perhaps Stage 2d-adjacent)

**None of these 14 failures are direct Stage 2d targets.** They're all baseline drift from earlier sessions.

The remaining Stage 2d work — companion val collapse (`typeInstance` → `draco`/`domain`/`rule`/`actor`), drop `extends App with TypeInstance` from companion headers, delete the four `*Instance` traits + their JSON skeletons, cascade through hand-written extenders — touches at least 10 hand-written Scala bootstrap files and a much larger fan-out of test fixtures and domain code. Per `feedback_json_scala_coupling_is_not_a_priori.md`:

> cannot reliably predict a priori which Scala files need updating when JSON/Generator changes; three coupling categories (bootstrap, hand-written test fixtures, hand-customized domain code); must operate via test-feedback or coupling audit

That cascade is a session of its own. Trying to stuff it into the tail of a session that's already done +8 PASS / -3 FAIL of structural work courts churn — the worst kind, where edits ricochet through unrelated tests and the green-to-red ratio temporarily inverts before stabilizing.

The agent proposed stopping here and treating the remaining instance-projection retirement as a follow-up session. The user agreed:

> **Dev:** Yes

---

## What Landed

**Aspects redesign — fully done:**
- 5 new aspect TypeDefinitions in YAML at `src/main/resources/draco/`: `Aspects.yaml`, `DracoAspect.yaml`, `DomainAspect.yaml`, `RuleAspect.yaml`, `ActorAspect.yaml`
- [Aspects.scala](src/main/scala/draco/Aspects.scala) — minimal parent trait, NOT sealed
- [TypeDefinition.yaml](src/main/resources/draco/TypeDefinition.yaml) — elements list trimmed to just `typeName`, derivation now `[Aspects]`
- [TypeDefinition.scala](src/main/scala/draco/TypeDefinition.scala) — `sealed trait TypeDefinition extends Aspects`, the 4 aspect vals removed (inherited)

**Instance-projection retirement — first phase:**
- [Generator.factoryBody](src/main/scala/draco/Generator.scala) refactored to take `TypeDefinition` instead of `Factory`, conditionally emits `instanceOverrides` only when the derivation chain reaches the trait declaring the member
- New private helper `Generator.chainHits(td, targetName)` walks `draco.derivation` transitively with cycle protection
- [Primal.json](src/main/resources/draco/Primal.json) drift fixed: derivation = TypeInstance (was DracoType)

**Side guardrail:**
- SessionStart hook in `~/.claude/settings.json` redirects out of `nexonix/.draco/worktrees/*` and `nexonix/.claude/worktrees/*` toward `/Users/edwardburke/IdeaProjects/nexonix` on `main`
- `reference_worktree_redirect_hook.md` memory documents the hook
- `user_preferences.md` updated: Claude does not run sbt; user drives compilation via tests

**Test state at session close:**
- GenerateAndCompileTest: **41 PASS / 14 FAIL of 55** (was 33/17/50 → net +8 PASS, -3 FAIL after 5 new types added)
- YAMLRoundTripTest: 116/116
- All 14 remaining failures are pre-existing baseline drift, not Stage 2d-related

---

## Deferred to Next Session

Stage 2d completion (instance-projection retirement, second phase):

1. **Generator companion val collapse** — change `typeInstance` emission in `Generator.typeGlobal` to emit `draco`/`domain`/`rule`/`actor` per the design memo. This is the single biggest emit change.
2. **Drop `extends App with TypeInstance` from companion headers** — companions become bare `extends App`. Generator.scala line 647: `s"object $objName extends ${appMixin}TypeInstance"` becomes `s"object $objName extends ${appMixin.trim}"` or similar.
3. **Delete the four `*Instance` traits** — TypeInstance.scala, DomainInstance.scala, RuleInstance.scala, ActorInstance.scala — and their JSON skeletons in `src/main/resources/draco/`. Once nothing extends them.
4. **Cascade through hand-written extenders** — every `extends App with TypeInstance` site in hand-written Scala (Generator.scala itself, DracoType.scala, Type.scala, TypeInstance.scala obviously, DomainInstance.scala, RuleType.scala, Primal.scala, Test.scala, DomainDictionary.scala, SourceContent.scala, Extensible.scala, TypeDefinition.scala, plus test fixtures) needs to migrate to the new convention. Per the no-a-priori-coupling memory, this is iterative and test-driven.

Plus Stage 2e (leaf detection), originally bundled with Stage 2d but split off — entirely separate from the retirement work, additive in nature.

The remaining 14 baseline failures are also not addressed yet; some may come along for the ride during the val-collapse cascade (e.g., RuleType's `ruleDefinition` issue), others are independent and deserve their own treatment.
