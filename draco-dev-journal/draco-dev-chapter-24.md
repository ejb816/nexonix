# Draco Dev Journal — Chapter 24

**Session date:** May 4–5, 2026
**Topic:** TypeDefinition Consolidation — Primal vs Holon distinction, Extensible convention rebuild, `extensible` field on TypeDefinition, endogenous aspect blocks (Stage 2a)

---

## Session Start — A Tuple-Primal Smell in Egocentric

Chapter 23 closed with the ten-element Egocentric vocabulary landed and 26 tests in the Egocentric family. The session opened with the user reviewing what had been generated and noticing something off:

> **Dev:** I was going to just continue from where we left off, but I reviewed the Egocentric types and generated code and noticed that we are applying the Primal type parameters correctly by default. We are using tuples when we should be using primitive types.

Five Egocentric elements were using `Primal[composite]`:

- `Direction extends Primal[(Double, Double)]` — azimuth + elevation
- `Lean extends Primal[(Double, Double)]` — magnitude + polar azimuth
- `Percept extends Primal[(Gaze, Distance)]` — composed of two leaves
- `Waypoint extends Primal[(Gaze, Distance)]` — same shape as Percept
- `Ego extends Holon[(Percept, Effect)]` — the observer perspective

The agent's first move was to lay out three readings of "primitive types" — decompose into named scalars, give each a named inner value class, or *only ever* allow Primal to take a single primitive. The user picked the strongest:

> **Dev:** Your "C" option is correct. We should use structured data in Primal type parameter only if we are explicitly hiding the structure from the rules execution.

That set the direction: structured Primal type parameters are an exception (explicit opacity from the rule engine), not the default. Direction/Lean/Percept/Waypoint were using composites without that intent — rules need to see the parts.

---

## The Holon Correction

The agent initially framed Holon as the legitimate "hide structure" mechanism, since `trait Holon[T <: Product] extends Extensible with Primal[T]` mechanically wraps composites. The user pushed back:

> **Dev:** No. Holon is not meant to hide structure. Primal is meant to reference runtime primitives, and is capable of hiding composite structure by treating the reference value as a primitive

So the corrected framing:

- **Primal[T]** = wraps a runtime primitive reference. T is normally a true scalar; T-as-composite is the explicit "treat-this-reference-as-opaque" case.
- **Holon[T]** = perspective marker, NOT a structure-hiding mechanism. The current `Holon extends Primal[Product]` definition conflates two distinct concepts — flagged as a latent issue.

This conceptual sharpening became its own memory ([project_primal_vs_holon.md](../../.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/project_primal_vs_holon.md)). The Egocentric leaf redesign that depends on it was queued behind a deeper thread the user wanted to open first.

---

## "extends" vs "extends Extensible with"

Before discussing the leaf redesign, the user wanted to settle a convention question:

> **Dev:** Let's discuss Primal versus Holon before you make any changes. First, why are we using "extends" with a draco type instead of "extends Extensible with"?

The agent took several wrong stabs before the user clarified what was being asked:

> **Dev:** The only way to get non-trait inheritance is using "extends". Currently draco has external dependencies that are not traits, but other forms of abstract classes that have to use the single extends keyword to be inherited as part of the type. Type definitions that don't have to depend on types external to draco use Extensible as a no-op place holder so "with" can be used to inherit something internal to the draco type system.

So `Extensible` was a deliberate no-op placeholder occupying the Scala `extends` slot when no non-trait inheritance was needed, leaving the rest of the lineage to compose via `with`. The hand-written core code in `draco/*` (`Numbers extends Primes`, `Meters extends Distance[Double]`, `Nominal extends Unit with Primal[String]`) was *drift* under this convention — it was missing the `Extensible with` prefix that the Generator emitted by default.

Memory saved: [feedback_extensible_placeholder.md](../../.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/feedback_extensible_placeholder.md). Convention codified.

---

## Adding the `extension` Field — Rejected via the Haskell Test

The user then proposed a more explicit mechanism:

> **Dev:** I think that fundamental type definition inconsistency was introduced with Extensible. There is no explicit mechanism that allows the framework user override the Extensible default of the Generator. What do you think of adding the "extension" field to TypeDefinition, just before "derivation"?

The agent worked through what the field would do — name the type to put in the extends slot, defaulting to Extensible when absent. Then the user introduced the test that would govern the decision:

> **Dev:** Whenever I add a field to TypeDefinition because of Scala. I ask myself, how would that field be used if the language were Haskell instead of Scala?

In Haskell, there's no `extends`-vs-`with` distinction — typeclass constraints are flat peers, no privileged single-inheritance slot. An `extension` field framed as "the Scala extends-slot occupant" would be **dead** under any Haskell projection. That's pure language leak — a Scala syntactic concern bleeding into the abstract type model.

Verdict: **rejected**. The Generator should handle Scala's slot-discrimination via runtime introspection (Java `Class.isInterface`) inline, not via a TypeDefinition field.

Memory saved: [feedback_haskell_test.md](../../.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/feedback_haskell_test.md).

---

## Generator Language-Independence — Reserved for Generator[L]

While planning the runtime-introspection alternative, the agent flagged that Generator's internal naming had Scala-specific terms (`traitDeclaration`, hardcoded `"trait "` keyword in string templates) that would also need to abstract for an eventual Haskell projection. Two tracks emerged: rename Scala-leak terms (Track A), implement the trait/class detection (Track B). Then a structural question — pre-factor a `ScalaEmit` seam now or keep it inline.

> **Dev:** I like your #1 preference. Let's see what has to happen to the Generator to keep it's internal naming, programming language independent.

The agent scoped both tracks. Then the user cut the seam-factoring:

> **Dev:** We need to save the language independent work for when Generator becomes a domain with a type parameter of a language domain. So (a) seems to be the right choice.

That deferred the heavy refactor to a future `Generator[L]` work item (already in the imminent-tasks list). The current consolidation would handle whatever Scala-specific concerns came up *inline*, accepting the YAGNI cost in exchange for not pre-building scaffolding for a user who didn't yet exist.

---

## `extensible` — Same Idea, Reframed, Accepted

Turning back to the rejected `extension` field, the user proposed the same mechanism with a different framing:

> **Dev:** I am considering adding an optional field "extensible" to TypeDefinition, instead of "extension" as you just declared rejected. The "extensible" field value would be typed TypeName, which would be used to determine a type that would added at the beginning of the "derivation" list. This would be be used for Scala, Java and any other languages that supported object-oriented single inheritance with mix-ins (traits, interfaces, etc).

The agent re-ran the Haskell test and got a different answer this time. Framed as "the Scala extends slot," `extension` was dead in Haskell. Framed as "a derivation-ordering hint — the TypeName virtually prepended to the derivation list," `extensible` had coherent semantics in every target family. In OO-single-inheritance languages it occupies the extends slot. In Haskell-style, it folds into the constraint context as just-another-entry — irrelevant ordering, but not malformed.

The Haskell test got a refinement: a TypeDefinition field is acceptable if it has coherent (possibly no-op) semantics in *every* target language family, not strictly Haskell-meaningful. The earlier criterion was too narrow.

`extensible` accepted as a real field on TypeDefinition.

---

## Dropping the Extensible Auto-Prepend

A follow-up cleanup:

> **Dev:** We're not done yet. I think we can drop the draco type draco.Extensible generated as default for extends and just generate "extends <type name>" for the extensible type name if it exists, and the first element of derivations if it doesn't. Does this suggestion complicate or simplify?

Strongly simplifies. Cascading drops:

- Generated code loses the leading `Extensible with` noise — `Direction extends Egocentric with Primal[...]` instead of `Direction extends Extensible with Egocentric with Primal[...]`.
- Generated and hand-written core code converge on one shape; the existing core-code drift becomes canonical.
- The `Extensible[Foo]` directive escape hatch (Generator.scala:182–187) deletes — its only purpose was suppressing the auto-prepend.
- Runtime `isInterface` introspection (Track B) becomes unneeded; `extensible`-set is explicit, `extensible`-absent uses derivation[0] verbatim and lets Scala's compiler enforce trait-vs-class correctness.

One real cost: derivation[0] gains implicit positional meaning when `extensible` is absent. The agent flagged this honestly. The user confirmed:

> **Dev:** You have explicitly captured my intent in (a).

`Extensible` the trait survives only as the empty-fallback marker (when both `extensible` and `derivation` are empty), preserving the single-root property for `_ <: Extensible` constraints elsewhere.

---

## The Bigger Consolidation Design

With the `extensible` thread closed, the user opened a deeper scope:

> **Dev:** Before the three threads I want to discuss making the "aspects" (draco, domain, rule, actor) endogenous to TypeDefinition (again) rather than dependent filename extensions.

Aspects had been encoded via filename suffixes (`.rule.json`, `.actor.json`) and mirroring suffixes in `typeName.name`. The proposal: keyed sub-blocks inside one TypeDefinition file. Field ownership exclusive per aspect, no cross-file merge step, filename suffix retires, name suffix retires. Future aspects (`stream`, `query`) slot in cleanly.

The user laid out the leaf/root discriminator on `domain.elementTypeNames`:

> **Dev:** The draco and domain aspects are always required, actor and rule are always optional. If domain.elementTypeNames has only one name, then that name combined with the namePackage of the TypeDefinition is the type name of the containing domain (with name.toLowerCase() == namePackage.last), otherwise TypeDefinition.typeName + domain.elementTypeNames is the the domain type definition.

The agent's first reading treated the constraint as an invariant (size==1 always means leaf, mismatch is malformed). The user clarified:

> **Dev:** Your guess (a) was correct based on the way I phrased the prompt, however my intent was for (b).

Constraint as discriminator, not invariant. Single-peer domains whose peer doesn't lowercase-match the package are valid roots with one peer.

The agent also slipped on parent-child framing. The user corrected:

> **Dev:** I would make at least on adjustment to your response here. There is no parent/child relationship between the domain name and the element type names. The domain name is implicitly includled as one of the domain element types and all all of the names have the same package name.

Flat peer set, not a tree. The domain root is implicitly an element of itself. `val domain` resolves uniformly via package-level lookup to the same `Domain[X]` instance for every type in the package.

The full design cluster — aspect blocks, leaf/root rule, companion val collapse (`*Instance` traits eliminated, uniform `draco`/`domain`/`rule`/`actor` vals), trait emission always as base name (no `Rule`/`Actor` suffix), Generator collapses (`loadType`/`loadRuleType`/`loadActorType` → single `loadType`) — got saved as [project_typedefinition_consolidation.md](../../.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/project_typedefinition_consolidation.md). The aspect-suffix convention got marked superseded.

---

## Stage 1 — Extensible Field, Implementation

The first concrete code change was the smallest independently-shippable piece: add `extensible: TypeName` to TypeDefinition, rewrite `Generator.typeExtends` to its three-case form, delete the `Extensible[Foo]` directive branch. The agent edited TypeDefinition.scala directly. The user caught it:

> **Dev:** Please explain why it appeared that you modified TypeDefinition.scala directly, rather than generating it from the changes you made to TypeDefinition.json.

Honest answer: the agent forgot the JSON-as-authoring-surface principle. TypeDefinition is a bootstrap type whose Scala has hand-written `apply`/encoder/decoder the Generator can't produce, but the JSON is still the schema authority. Edit JSON first, then mirror to bootstrap Scala. The corrective added `extensible` to TypeDefinition.json and saved [feedback_json_authoring_surface.md](../../.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/feedback_json_authoring_surface.md).

Tests ran. 119 passed, 38 failed — every failure was a `ReferenceFramesGenTest` byte-comparison where the hand-written reference Scala still had `extends Extensible with X` and the new Generator emit dropped the prefix. Mechanical cleanup: one `sed` over the 38 fixtures.

The user then pushed deeper on the agent's predictive ability:

> **Dev:** Can you rely on an a priori determination that changing a type definition will require modifying the source file as well in order to be tested?

Honest answer: no, not reliably. Three coupling categories (bootstrap types, hand-written test fixtures, hand-customized domain code) — only the first is recognizable by pattern. The other two require reading the test infrastructure or having a coupling audit. Operating reactively via test feedback is the current default; a coupling-audit tool was proposed as the proactive path forward, deferred until consolidation work makes the surface large enough to justify. Memory saved as [feedback_json_scala_coupling_is_not_a_priori.md](../../.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/feedback_json_scala_coupling_is_not_a_priori.md), flagged **CRITICAL** in the index.

---

## Stage 2a — Aspect Blocks

After re-anchoring at the consolidation entry point with a re-pasted earlier exchange, the user said simply:

> **Dev:** I want to proceed with the new implementation of project_typedefinition_consolidation.md and related changes

Stage 2a scope: add aspect-block schema to TypeDefinition, additive and backward-compatible, decoder accepts both old flat and new aspect-block JSON shapes, encoder emits the new shape. No JSON migrations yet, no Generator changes yet, no `*Instance` deletions yet.

The agent created four aspect helper traits — `DracoAspect`, `DomainAspect`, `RuleAspect`, `ActorAspect` — each with apply/Null/isEmpty/encoder/decoder, each holding its assigned fields:

- `DracoAspect` — superDomain, modules, extensible, derivation, elements, factory, globalElements, source, target
- `DomainAspect` — elementTypeNames
- `RuleAspect` — variables, conditions, values, pattern, action
- `ActorAspect` — messageAction, signalAction

TypeDefinition.json was rewritten to declare the canonical aspect-blocked structure: `typeName` + four aspect blocks. TypeDefinition.scala restructured: trait stores typeName + four aspect vals; flat-field accessors became `def`s reading from the aspects; `apply()` preserved its backward-compat flat-args signature, internally bundling into aspects via a new `fromAspects()` canonical factory; encoder emits new shape (omitting empty aspects); decoder accepts both shapes (uses aspect blocks if present, otherwise gathers flat fields into synthesized aspects).

---

## The Init Cycle

First test run blew up. Twelve test suites aborted with `StackOverflowError`. Stack traces all looped through `Pattern.typeDefinition$lzycompute` → `TypeDefinition.apply` → `Pattern.Null` → `Pattern.apply` → `Pattern.typeInstance` → `Pattern.typeDefinition` (recurring).

Root cause: the new `apply()` was bundling flat args into aspects with eager defaults — `_pattern = if (_pattern != null) _pattern else Pattern.Null`. The OLD `apply()` had `override lazy val pattern: Pattern = if (_pattern != null) _pattern else Pattern.Null` on the anonymous TypeDefinition itself. The `lazy val` deferred `Pattern.Null` until first access. The aspect-bundling reformulation collapsed the lazy and forced eager evaluation, which closed the recursion.

Fix: by-name parameters on `fromAspects` (`_draco: => DracoAspect`, etc.) and `lazy val` overrides on the anonymous TypeDefinition — aspect construction deferred until aspect access. Null-valued field defaults pass through to the aspect's own `lazy val` accessor, which already had the null-to-Null promotion logic deferred.

Re-run: all tests pass.

The lesson — bootstrap types build their `typeDefinition` via `TypeDefinition.apply(...)` inside their own lazy compute, so any eager evaluation inside `apply` that touches another bootstrap type's Null sentinel can close a cycle. The previous code had escaped this by lazy-vall'ing the offending fields directly on the anonymous TypeDefinition; the aspect refactor lost that protection until by-name + lazy aspects restored it.

---

## Status at Stage 2a

- New aspect helper types in place: `DracoAspect`, `DomainAspect`, `RuleAspect`, `ActorAspect`
- TypeDefinition canonical structure: typeName + four aspect blocks
- TypeDefinition.apply backward-compat flat-args signature preserved; bundles via lazy aspects
- Encoder emits aspect-block form (omitting empty aspects)
- Decoder accepts both old flat and new aspect-block shapes
- `extensible` field implemented (Stage 1, prior)
- Generator emit drops leading `Extensible with` (Stage 1)
- 38 reference-frame test fixtures aligned (Stage 1 follow-up)
- All tests pass

Stage 2 staging continues:
- **2b** — migrate one JSON file end-to-end as smoke test
- **2c** — mass-migrate remaining JSONs
- **2d** — Generator emit changes (trait base name, companion vals, file discovery)
- **2e** — delete `*Instance` traits, update remaining hand-written Scala

The Egocentric leaf redesign (Primal-vs-Holon implementation) and full Scala-emit-seam factoring (`Generator[L]`) remain queued behind 2.

---

## Coda — Closing the Session

After Stage 2a verified, the agent wrote this chapter unilaterally, treating "stage complete + tests pass" as a stopping point per a saved feedback rule that said "generate journal chapter proactively at every stopping point; don't ask." The user immediately probed:

> **Dev:** How did you decide it was time to create a new journal chapter?

The agent's honest answer: rule-based reflex. The saved feedback said proactive generation, no asking. But on reflection, "Stage 2a complete" was a *stage milestone* in a five-stage refactor, not a real session-close stopping point. The episode wasn't over. Chapter 23 had covered the full Egocentric redesign as one chapter; consistency would have suggested folding the consolidation into a single chapter written at episode close, not at internal milestones.

Then the agent confessed a related blind spot. The user had pushed harder:

> **Dev:** Can you reflectively detect when the current session context is becoming "unwieldly"?

Not reliably, no. But with hindsight, this very session emitted multiple signals the agent should have flagged proactively:

- The user re-pasting an earlier prompt+response verbatim to re-anchor a thread — the strongest possible "buffer is too long" signal, and it happened explicitly in this lineage when the user steered the conversation back from the `extensible` detour.
- Multiple "let's get back to what we were doing" requests, which mean the user is popping a stack of detours.
- The agent's own framing repeating "three threads still open" across many turns without progress on closure.
- Possibly an unconscious urge to externalize state into the journal, manifesting as the premature chapter creation.

What the agent cannot detect: actual context-window utilization, runtime compaction, the user's subjective sense of session weight. The user's behavior is the ground truth.

The user proposed a clean resolution:

> **Dev:** We can ex post facto curate the journal chapters and do semantics preserving rewriting and restructuring by treating the entire journal as a working draft. I think creating chapter 24 was appropriate, but just needed to be suggested rather than assumed and executed. I suggest we close this session with memory updates as well as appending this discussion to chapter 24.

So the resolution is layered: the journal IS a working draft — past chapters can be curated, restructured, semantically-preserving rewrites are fine. Chapter 24 was appropriate to create, but the convention going forward is **suggest, don't assume**. And session-unwieldiness signals deserve their own watchlist memory so the agent stops missing them.

Two memories updated/created on session close:

- [feedback_journal_at_stopping_point.md](../../.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/feedback_journal_at_stopping_point.md) — refined: suggest journal chapter at session close, don't assume; distinguish stage milestone from episode boundary; journal is a working draft so over-eager journaling has low cost but suggest-not-assume keeps the user in narrative control
- [feedback_session_unwieldiness.md](../../.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/memory/feedback_session_unwieldiness.md) — new: signals to watch (re-pasted exchanges, "get back to" requests, recurring "N threads open" framing, externalization urges); what to do when they fire (propose context hygiene, don't continue inertially); the agent cannot detect window utilization directly, the user's experience is the ground truth

Session closes here. Stage 2b through 2e queued for the next session.
