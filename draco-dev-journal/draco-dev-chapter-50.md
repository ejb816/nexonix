# Draco Dev Journal — Chapter 50

**Session date:** June 24, 2026
**Topic:** How actors that thread `ActorRef`s through working memory get *defined* (not hand-written). A design dialogue strips away three over-abstractions, settles the actor lifecycle along two orthogonal dimensions (statefulness = Draco author choice; multiplicity = Orion-only), lands the always-`def actorType` emission, and — discovering the example-domain comments are Claude-authored prose with no upstream source — preserves them verbatim here before stripping, against a future comment-bearing definition language.

---

## The opening, and three corrections

The thread resumed at the rule↔ref seam: actors whose providers seed a consumer `ActorRef` into Evrete working memory (`session.set("consumer", ref)` ↔ `ctx.getRuntime().get(...)`). The first instinct was to model the seam — a role/port/channel concept in the type definition. The Dev cut that down three times, and each cut sharpened the boundary.

> **Dev:** I'm a bit concerned about any explicit reference to type definition aspects. It is a user authoring choice at the draco level how to wire actors together. That is more automated at the orion outer layers.

So wiring/topology is not type-definition content. The actor type describes the membrane; *who it forwards to* is composition, authored at Draco and automated at Orion.

> **Dev:** It is completely domain dependent logic how actors use actor refs whether in the actor itself or in the rules executed by the actor. The consumer/provider model was invoked as supporting the set of domain examples only.

There is no universal pattern to name. How an actor uses a ref — in `receive` or in a rule RHS — is just that domain's logic; the Generator emits the body verbatim and attaches no meaning. The consumer/provider idiom is the *examples'* choice, not a framework concept.

> **Dev:** Manually "defined" not written.

The actors get authored as JSON TypeDefinitions and generated — not left as hand-`.scala`. "Defined" (author writes the definition) is the milestone, distinct from "written" (Scala by hand). The enabler landed: an actor factory now honours `dracoAspect.factory.parameters`, so a downstream ref is an ordinary construction parameter — `def actorType(consumer: ActorRef[Json])` — with the `session.set` line being the domain's own body, not a framework-derived seam.

## signalAction, and the two dimensions

> **Dev:** Before these example domains and domain element type, I had not considered using signalAction for anything. Now I see it functions very well for actor initialization and shutdown in all cases. What do you think?

The instinct (lifecycle deserves a home distinct from `messageAction`) is right; "one field for init *and* shutdown" can't hold literally, because init fires at construction and shutdown arrives as the `PostStop` signal — two seams. The reconciliation: `messageAction`→`receive`, `signalAction`→`receiveSignal`, with a run-once `setup` for the acquire half. The Dev then reframed my "persistent session?" question into its true axes:

> **Dev:** There are two dimensions for your question about actor persistence 1) Is there both stateless and stateful actor types 2) are there multiple value instances of an actor type running concurrently?

- **Statefulness** — a Draco author choice, encoded *structurally* by where the session is created: `messageAction` (per-message) ⇒ stateless; `setup` (once) ⇒ stateful. No "kind" flag.
- **Multiplicity** — at first floated as a Draco/Orion shared knob, then narrowed:

> **Dev:** Multi-instance stateful is in scope, and my preference is that any or all of the four are configuration parameters in orion, and definition author choices in draco.

> **Dev:** Multiplicity as its own dimension will occur in the orion layer, not in draco. While signalAction is invoked in PostStop cleanup, what comes before that is any post-rule-execution that would interact with working memory, and execute any pre-cleanup code the definition author would add to signalAction.

So `signalAction` is the whole shutdown routine, author-owned: at `PostStop` it does the post-rule-execution read of the accumulated working memory (harvest), then pre-cleanup, then close. The fold: `messageAction` accumulates, `signalAction` reaps.

## The always-`def` emission (option a)

A `lazy val actorType` is *itself* a single-instance commitment — a multiplicity fact baked into Draco. To keep multiplicity wholly in Orion:

> **Dev:** (a)

The Generator now **always emits `def actorType(…): ActorType`** (nullary when no construction params), never `lazy val`. A `def` mints a fresh `new Actor[T]{}` per call, so "call once per spawn" gives per-instance session state — multi-instance-stateful works *without* `Behaviors.setup`, preserving `ActorType = ExtensibleBehavior`. `knowledge` (the compiled RETE network) stays type-scope/shared; only the session is per-instance. Migrated 6 actor declarations + 8 `.actorType()` call sites; suite green at 185/185. `ExampleDomainsGenTest` held at 26 match / 22 differ — and the `Creator` diff narrowed to **comments only**: structurally the generated `def actorType(consumer:…)` now matches the hand-written.

## The comment provenance, and preserve-then-strip

That comment-only diff raised the real question:

> **Dev:** I'm thinking of introducing comments in the type definition language that succeeds YAML. Since I didn't put comments in source code examples I created, I assume you introduced them in your "hand-written" source code. Does the "authoritative" comment source exist only in the source code itself, after you wrote it?

The verification was unambiguous: the comment text lives in the `.scala` files and **nowhere in any definition** (no JSON/YAML twin). `git blame` attributes it to `ejb816` only because the Dev commits through the IDE; the prose ("SourceSink (Brahma)", "rule↔ref seam") is Claude's, from the media-domains sessions. So the authoritative source is the source code, with no origin prior to my writing it — these are my characterization, not authored design intent. The right direction is the Dev's: comments become a first-class construct *in the successor definition language*, authored there, generated down.

> **Dev:** Yes. We can recover comments after we introduce the definition language.

I then jumped to `perl`-stripping the comments — and was rightly stopped:

> **Dev:** I didn't mean to suggest destructively strip the comments. We should be able to preserve the specific comments for when we have the definition language that permits them. Just preserve them in a journal chapter explicit appendix.

Preserve-then-strip, not delete. (Recorded as a standing principle in auto-memory.) Hence Appendix A: every comment on the 17 definition-backed example types, verbatim, keyed by its JSON twin and attachment point, so each has an unambiguous home to return to once the language permits it. The five hand-only files (`AerialSink`/`MarineSink`/`EtherealSink`/`TerrestrialSink`, `world/Geodesy`) have no JSON twin, aren't regenerated, and keep their comments in place — not preserved here, not stripped.

## The lifecycle reclaim landed

With the `Creator` fold closed, the keystone followed: `ActorAspect` gained a third `Action` field, `setupAction`, making the triad name-honest — `setupAction`→construction (acquire, run once), `messageAction`→`receive`, `signalAction`→`receiveSignal` (`case PostStop`: harvest the accumulated working memory, then cleanup). HEAD `91e4a7c` had squatted `signalAction` on the construction slot and left `receiveSignal` a no-op; this evicts it. The Generator's `actorBehavior` now reads `setupAction` for the run-once block and scaffolds `receiveSignal` as `signal match { case PostStop => <body>; Behaviors.same; case _ => Behaviors.same }` (PostStop fully-qualified, so no import changes). Safe by construction: every definition has empty `setupAction`/`signalAction` today, so emission is byte-identical — 185/185 green, gen-map still 30/18, `DracoGenTest`'s `ActorAspect` (the schema's own self-generation) matched the hand-edited `.scala` on the first run. The demanding cell — multi-instance stateful — is now expressible: `def actorType(...)` mints a fresh per-spawn behavior, `setupAction` stands up its private session, `messageAction` accumulates, `signalAction` reaps at PostStop.

## The first stateful actor

To prove the reclaimed lifecycle rather than leave the new fields dormant, the Aerial `Consumer` was promoted to the demanding cell — multi-instance stateful. A clarifying exchange first dissolved a non-question I'd posed:

> **Dev:** Anything that can be done with working memory in signalAction can also be done in messageAction, depending on how those actions are defined in the type definition. I'm not sure I understand the "one semantic question".

Right — there's no framework decision about *where* the harvest lives; once `setupAction` stands the session up, working memory is reachable from both `messageAction` and `signalAction`. The framework fixes only *when* each runs; the placement is pure authoring. So the only real consequence of reaping in `signalAction` is observable timing (the sink fills at stop), which the test must await.

The promotion, all in the definitions: `Consumer.json`'s `setupAction` creates a persistent session plus a `consumed` buffer seeded into the Evrete Environment (`session.set("consumed", consumed)`); `messageAction` is just insert+fire (no close); `signalAction` drains the buffer to `AerialSink` and closes at `PostStop`. `ConsumeReport.rule.json`'s RHS changed from recording immediately to appending into the buffer via the same `ctx.getRuntime().get(...)` seam the consumer-ref uses. Regenerated through `AerialGenTest` — the first non-empty `signalAction`/`setupAction` emission, and it compiled clean. The behavioral tests (`AerialChainTest` sends two intents and asserts both are reaped after `whenTerminated`; `AerialActorTest` seeds the buffer for the rule-direct angle and awaits the reap for the actor angle) went green — after one miss where I updated `AerialChainTest` but forgot `AerialActorTest` also exercised the old per-fire recording. 185/185, gen-map still 30/18.

## Where this leaves things

Done: always-`def` actor emission; the `consumer` factory parameter defined on all four `Creator` JSONs; the `ActorRef` import folded into the typed brace (matching convention, so no offset cascade); comment provenance established; Appendix A preserved; comments stripped from the 17 def-backed files. Net: **the `Creator` actor-fold is closed end-to-end** — `ExampleDomainsGenTest` moved 26→**30 match / 18 differ**, the four Creators now byte-clean against generation, suite green at 185/185. The remaining 18 are all genuine: `Input`/`Output`/`World.Consumer`/`World.Provider` (their own factory params, downstream types differ), the 4 `OriginateReport` rules (the rule-side `getRuntime` read — a real capability, not cosmetic), `Observable`/`Cartesian` (real custom geodesy), and the domain-shell import-style cases. Also done this session: the `signalAction`→`receiveSignal` + `setupAction` reclaim, and the first stateful actor (Aerial `Consumer`) exercising it end-to-end (both above). Still queued: finish the other four factory actors (`Input`/`Output`/`World.Consumer`/`World.Provider`) the same way as `Creator`; and, the larger arc, the comment-bearing definition language that supersedes YAML — at which point Appendix A is the reauthoring source.

---

## Appendix A — Example-domain source comments (preserved for the definition language)

Provenance: Claude-authored, hand-written into the example `.scala` during the media-domains sessions (commits through `b04da0d`/ch.42–47); no definition twin ever carried them. Stripped from the 17 definition-backed example types in this chapter so regeneration is clean; recorded here verbatim, keyed by the JSON definition they document and the point they attached to, for reauthoring once the successor type-definition language permits comments. Fenced as `text` (comment fragments, not whole compilation units).

### domains/aerial/Creator — Creator.json
Attachment: type-level, before `object Creator`
```text
// The source face of the medium's SourceSink (Brahma): receives a seed intent,
// whose OriginateReport rule originates a report and sends it — via the rule↔ref
// seam — to the medium's Consumer, seeded as "consumer" in the session Environment.
// With the per-medium relay stripped, the Creator hands directly to the Consumer;
// cross-medium transforms live in World, not inside a Format domain.
```

### domains/aerial/OriginateReportRule — OriginateReport.rule.json
Attachment: rule action body
```text
// Creation phase: the report is *originated algorithmically* from the intent —
// no JSON fixture. Here the origination is a unit transform (flight level, in
// hundreds of feet, → altitude in feet) carrying the callsign through. This is
// the seam where a real medium would synthesize its native representation.
```

### domains/aerial/Position — Position.json
Attachment: type-level, before the `Position` type
```text
/** The TYPED Aerial position form — a direct subtype of `Aerial` (hence `World`),
  * the strong composition tree the input adapter decodes a loose `PositionReport`
  * into. Geodetic horizontal (degrees) + altitude in feet; the discriminator is
  * subsumed by the type itself. */
```

### domains/aerial/Input — Input.json
Attachment: type-level, before `object Input`
```text
/** Aerial input adapter (codec, World-bound): decodes a loose `PositionReport` Json
  * into the typed `Position` — a direct subtype of `Aerial`, hence an indirect
  * subtype of `World` — and hands it to `World.Consumer`. The only place that knows
  * Aerial's wire schema; World itself deals only in typed values.
  *
  * Definition-backed (`Input.json`, `actorAspect.messageAction`) like the medium's
  * `Creator`/`Consumer`; the Scala body stays hand-written until the actor-emission
  * Generator fold. Not a `domainAspect` member of Aerial — actors are defined types
  * but not message-type members, matching the `Creator`/`Consumer` convention. */
```

### domains/ethereal/Creator — Creator.json
Attachment: type-level, before `object Creator`
```text
// The source face of the medium's SourceSink (Brahma): receives a seed intent,
// whose OriginateReport rule originates a report and sends it — via the rule↔ref
// seam — to the medium's Consumer, seeded as "consumer" in the session Environment.
// With the per-medium relay stripped, the Creator hands directly to the Consumer;
// cross-medium transforms live in World, not inside a Format domain.
```

### domains/ethereal/OriginateReportRule — OriginateReport.rule.json
Attachment: rule action body
```text
// Creation phase: originate an EphemerisReport algorithmically from the launch
// intent. Ethereal's native representation diverges from the other media —
// discriminator "category", id "object", altitude in kilometres, and the report
// concept is an "EPHEMERIS" (a position computed from orbital mechanics), distinct
// from position/location/fix. The origination is a clean unit transform (nautical
// miles -> kilometres).
```

### domains/marine/Creator — Creator.json
Attachment: type-level, before `object Creator`
```text
// The source face of the medium's SourceSink (Brahma): receives a seed intent,
// whose OriginateReport rule originates a report and sends it — via the rule↔ref
// seam — to the medium's Consumer, seeded as "consumer" in the session Environment.
// With the per-medium relay stripped, the Creator hands directly to the Consumer;
// cross-medium transforms live in World, not inside a Format domain.
```

### domains/marine/OriginateReportRule — OriginateReport.rule.json
Attachment: rule action body
```text
// Creation phase: originate a FixReport algorithmically from the voyage intent.
// Marine's native representation diverges from the other media — discriminator
// "kind", id "vessel", depth in fathoms not feet/metres, and the report concept
// is a "FIX" (a determination one takes), distinct from position/location. The
// origination is a clean unit transform (metres -> fathoms).
```

### domains/terrestrial/Creator — Creator.json
Attachment: type-level, before `object Creator`
```text
// The source face of the medium's SourceSink (Brahma): receives a seed intent,
// whose OriginateReport rule originates a report and sends it — via the rule↔ref
// seam — to the medium's Consumer, seeded as "consumer" in the session Environment.
// With the per-medium relay stripped, the Creator hands directly to the Consumer;
// cross-medium transforms live in World, not inside a Format domain.
```

### domains/terrestrial/OriginateReportRule — OriginateReport.rule.json
Attachment: rule action body
```text
// Creation phase: originate a LocationReport algorithmically from the march
// intent. Terrestrial's native representation diverges from Aerial's — different
// keys (type/unit/elevationMetres vs message/callsign/altitudeFeet), metres not
// feet, "LOCATION" (weakly fixed) not "POSITION" (a point in motion). The
// origination is a clean unit transform (feet -> metres) carrying the unit id
// through; this is the divergence a future Aerial=>Terrestrial transform must
// bridge.
```

### domains/terrestrial/Location — Location.json
Attachment: type-level, before the `Location` type
```text
/** The TYPED Terrestrial location form — a direct subtype of `Terrestrial` (hence
  * `World`), the strong composition tree the output adapter encodes an `Observable`
  * into before re-serialising to a loose `LocationReport`. Geodetic horizontal
  * (degrees) + elevation in metres. */
```

### domains/terrestrial/Output — Output.json
Attachment: type-level, before `object Output`
```text
/** Terrestrial output adapter (codec): encodes a typed `Location` (a World subtype)
  * back into a loose `LocationReport` Json and hands it to the medium's `Consumer`
  * — the sink face of Terrestrial's SourceSink. The only place that knows
  * Terrestrial's wire schema.
  *
  * Definition-backed (`Output.json`, `actorAspect.messageAction`) like the medium's
  * `Creator`/`Consumer`; the Scala body stays hand-written until the actor-emission
  * Generator fold. Not a `domainAspect` member of Terrestrial — actors are defined
  * types but not message-type members, matching the `Creator`/`Consumer` convention. */
```

### domains/world/Consumer — Consumer.json
Attachment: type-level, before `object Consumer`
```text
/** World.Consumer — the transform INPUT face (Vishnu-at-work). Receives a typed
  * source value (a World subtype, decoded by a subdomain input adapter) and
  * produces the target representation through the change of form that PRESERVES
  * MEANING: it projects through the `Observable` world-fact (geodetic -> ECEF ->
  * geodetic), then hands the typed result to the `Provider`.
  *
  * This is where "all transform rules live in World" is realised — so World knows
  * the media types by design. For this first slice the transform is Scala (the
  * `Geodesy`/`Observable` core proven by `AerialTerrestrialTransformTest`);
  * expressing it as JSON-backed World rules is the next precursor to TransformBuilder. */
```
Attachment: inline in `receive`, `case position: Position` branch
```text
// input adapter projected nothing yet — World owns the geodesy: feet ->
// metres, geodetic -> ECEF (the Observable world-fact) -> geodetic.
```

### domains/world/Observable — Observable.json
Attachment: type-level, before the `Observable` type
```text
/** The world-fact: the objective thing the media's reports are reports *of*, the
  * meaning-invariant a transform must preserve. A `Holon` is the perspective that
  * observes; an `Observable` is what is observed. It carries the canonical position
  * in BOTH frames simultaneously — `geocentric` (Geocentric/Axial, ECEF) and
  * `heliocentric` (Heliocentric/Ecliptic) — and will later grow identity / velocity
  * / time as the tracked thing acquires more state. Heliocentric is `Cartesian.Null`
  * until the first crossing to Ethereal. */
```
Attachment: member `def fromGeodetic`
```text
/** Geocentric/Axial input-adapter primitive: geodetic (degrees, metres) -> Observable.
  * Uses Cartesian's positional-value constructor to map the ECEF triple into x/y/z. */
```
Attachment: member `def toGeodetic`
```text
/** Geocentric/Axial output-adapter primitive: Observable -> geodetic (degrees, metres). */
```

### domains/world/Provider — Provider.json
Attachment: type-level, before `object Provider`
```text
/** World.Provider — the transform OUTPUT face: re-dispatches a transformed typed
  * value (a World subtype) to the target subdomain's output path. Thin in this
  * first vertical (one configured downstream); type-directed routing across many
  * targets arrives with the JSON-backed transform rules. */
```

### domains/world/Cartesian — Cartesian.json
Attachment: type-level, before the `Cartesian` type
```text
/** A named-field 3D Cartesian coordinate (metres). The composition tree exposes
  * `x`, `y`, `z` as fields (messages-as-named-trees), and the factory *also* accepts
  * a positional coordinate value, mapping `(_1, _2, _3) -> (x, y, z)` — so the named
  * surface and draco's positional `Coordinate` substrate compose through the
  * constructor rather than being a choice between them.
  *
  * Frame-agnostic: which frame a `Cartesian` is in (Geocentric/Axial vs
  * Heliocentric/Ecliptic) is fixed by where it sits in the world-fact, not by the
  * coordinate itself. */
```
Attachment: member `def apply (value: (Double, Double, Double))`
```text
/** Map a positional coordinate value into the named fields. */
```

### domains/world/World — World.json
Attachment: type-level, before the `World` domain
```text
/** The super-domain the four media derive — the shared semantic ground of the
  * transformation service (the Eagle's emanations / the invariant against which
  * meaning is preserved). A *message domain*: every contained type is a subtype of
  * `World`. For now that discipline is carried implicitly by `Domain[T]` + the
  * media's derivation; a future explicit `Message[Domain]` will name and enforce it.
  *
  * Not a Format domain: `World`'s messages are typed values (subdomain subtypes),
  * not `Format[Json]` shells. Its transform machinery (input/output adapter actors
  * and the meaning-preserving transform rules) is added in the next slice. */
```
