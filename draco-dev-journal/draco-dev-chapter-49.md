# Draco Dev Journal — Chapter 49

**Session date:** June 23, 2026
**Topic:** Toward generating the example domains. A discovery scaffold (`ExampleDomainsGenTest`) charts exactly which example types already generate and which don't; then a design dialogue settles the actor-emission model — two `Action` elements (`signalAction` = run-once setup, `messageAction` = per-message), authored in user space — and its first half lands in the Generator.

---

## "What else before we can generate the example domains?"

The example domains (Aerial/Terrestrial/Marine/Ethereal/World/Sentient) are hand-written ahead of the Generator. Rather than guess the gap, a discovery test was scaffolded — `ExampleDomainsGenTest`, modeled on `DracoGenTest` but **non-failing**: it walks every `.json` under `src/mods/.../domains`, generates, diffs against the committed `.scala`, and writes a per-type MATCH/DIFF/ERROR map to `target/test-output/ExampleDomainsGenTest.log` with a one-line console summary. (Non-failing on purpose: most example types are expected to differ today, and we just got the suite green — a permanently-red gate would undo that. Harden to `assert(differ == 0)` once the gaps close.)

The map: **26 match / 22 differ / 0 error (of 48)** — and the 22 sort into three very different buckets:

- **12 genuine capability gaps:** the 8 factory actors (`Creator`×4, `Input`, `Output`, `World.Consumer`, `World.Provider`) + 4 `OriginateReport` rules. The `Creator` diff confirmed it: the Generator emits the *terminal* `val actorType` (no downstream ref, no `session.set` seed) where the hand-written is the *factory* `def actorType(consumer: ActorRef[…])`.
- **6 cosmetic:** the domain shells — byte-identical except import style (`import domains._` + `import domains.world._` vs the specific `import domains.world.World`).
- **4 alignment-choice:** `Position`/`Location` (the Generator actually emits *more* — a derived `Encoder`/`Decoder` codec — plus ScalaDoc/`with DracoType` deltas) and `Observable`/`Cartesian` (genuine custom geodesy logic, overlapping the transform-as-rules question).

0 errors mattered too: the Generator never chokes on any shape. So "what else" reduced to essentially one capability — the actor-emission fold.

## The two-action actor model

> **Dev:** There should be two Action type elements as part of an ActorAspect: signalAction and messageAction. The signalAction is used to manage the actor's behavior, such as creating a rules stateful or stateless session and loading the rules and initial data into working memory. The messageAction just places the message in working memory and fires the rules.

`ActorAspect` *already* carried both `Action` elements — so this was about semantics, not schema. Today's actors cram the whole lifecycle into `messageAction` (Aerial's `Consumer` does `newStatefulSession()` → insert → fire → **close** every message, rebuilding the session per message); `signalAction` is unused. The Dev's split is the fix, and its payoff is that **the session persists across messages**.

The reconciliation it forced: `signalAction` was wired to Pekko's `receiveSignal`, but Pekko Typed has no startup signal and session *creation* must happen once and yield a reference `messageAction` reuses. So `signalAction` maps to **construction-time setup**, not `receiveSignal`.

> **Dev:** Since Action is a type element, we can start letting it exist in user space, as part of the user supplied type definition for the actor aspect.

This resolved the sub-questions (stateful vs stateless, the initial-data source): they're not Generator features but **lines the author writes in the `signalAction` body**. The Generator stays semantics-blind — it owns only *placement*: emit `signalAction` once at construction (its `Fixed` `val session` persists, in scope), `messageAction` in `receive`. The downstream ref (`session.set("consumer", ref)`) is just a `signalAction` line; the only piece still needing schema is *where the `ActorRef[T]` factory parameter is declared*.

## First half landed

> **Dev:** Continue with what we have now, and after we will figure out which definitions to create to extend the test.

`Generator.actorBehavior` now emits `signalAction` as a run-once construction block (4-space instance scope) and `messageAction` in `receive`; `receiveSignal` is a no-op (PostStop cleanup deferred). **Byte-identical for every existing actor** — none populate `signalAction`, so empty setup → the same emission as before. Verified: full suite **185/185** green, the example map **unchanged at 26/22/0** — a deliberate no-op regression check confirming the mechanism slotted in without disturbing the working actors.

## Where this leaves things

The actor-emission fold is half-built: the signal/message split is a Generator capability; the remaining half is the **downstream role→type declaration** (so `def actorType(role: ActorRef[T])` + the seed can be emitted). Next session: the authoring pass — choose which example actors to re-define (session setup into `signalAction`, stateful/stateless per actor, the downstream ref once its schema lands), which starts flipping the 22 DIFFs toward MATCH. The 6 cosmetic shell-import diffs and the `Position`/`Location` codec are cheap reconciliations available any time.
