# Draco Dev Journal — Chapter 23

**Session date:** April 17–29, 2026
**Topic:** Egocentric Domain Vocabulary Redesign — endogenous semantics, Holon/Primal distinction, ten-element Egocentric increment

---

## Session Start — Picking Up From Chapter 22

Chapter 22 closed with Increment C landed (12 transform skeletons), an 80-test ReferenceFramesGenTest, and "Inner primal value types for transforms" as a deferred honesty item. The reference-frame domains existed as skeletons — leaves derived from `Primal[Double]`, transforms as empty `Transform[S, T]` traits, no semantic content.

The session opened with:

> **Dev:** Time to start adding type definitions to the *centric domains and domain transforms.

The agent's first move was to orient: 5 frames, 10 leaves, 4 assemblies, 12 transforms — all skeletal. The fork in the road was leaves first vs transforms first, with a quick recommendation for leaves so transforms had something real to map. The user clarified the order:

> **Dev:** We need to create the type definitions for the reference frame domains (domain scalars) before we create elementTypeNames entries for the transform domain part of the domain transform 2-tuple.

Sequence locked: scalars first across the reference frames, then later populate elementTypeNames on the 12 transform domains.

---

## Endogeny — Setting the Semantic Constraint

Focus narrowed to the Ego frame. The agent proposed each scalar getting its own inner value type rather than the generic `Primal[Double]`. The user accepted the direction and immediately raised the deeper constraint:

> **Dev:** The Ego frame semantics are unclear to me. I like your suggestion regarding "inner primal value types". We need to determine how to characterize Bearing and Reach units that are endogenous to the Percept rather than just being a double value.

"Endogenous to the Percept" became the load-bearing principle for the entire session. The unit semantics could not be imported from external frames — no degrees, no radians, no meters, no AU. Whatever the value type, it had to mean something *only relative to the observer's body*, with no external calibration.

Initial proposal:

- `Bearing.Value` — turn-fraction, where 1.0 = full turn back to facing. The unit "a turn" is proprioceptive — a bodily rotation, not a borrowed 2π or 360°.
- `Reach.Value` — multiples of the observer's own arm-reach. The unit *is itself a reach*, a bodily invariant of this observer.

A consequence emerged in writing it: a bare `Bearing.Value(0.25)` is meaningless without a Percept supplying an observer. The Percept is what makes Bearing and Reach readable at all — the observer is implicit in the Percept itself.

---

## Chirality — Two Layers, Two Types

> **Dev:** We need to add chirality to both Bearing and the Percept, which means clockwise and widdershins and right and left or something like those.

The agent proposed two distinct chirality types at two layers:

- `Bearing.Sense` = `Clockwise | Widdershins` — handedness of the *rotation itself*.
- `Percept.Side` = `Right | Left` — handedness of the *observer's body* engaging with the percept.

These were intentionally distinct types, not one shared `Chirality`, because rotational direction and bilateral embodiment are genuinely different things. Bundling them would have collapsed a real semantic distinction.

Open questions raised at this point: naming (Sense vs Chirality vs Rotation; Side vs Laterality vs Handedness), zero-turn semantics (is sense meaningful when magnitude is zero?), what Percept.Side actually corresponds to (which hand reaches, which hemifield, chirality of the perceived thing?), and whether Percept's structure changed under the addition.

These questions were never resolved — the next move overwrote them.

---

## Abandoned Path: Bamen Wubu (Eight Gates and Five Steps)

> **Dev:** I want ego-centric values to be based on the eight gates and five steps (also called thirteen postures/dynamics), which will cover all dynamic interactions of the ego entity with the environment

The user introduced the **Thirteen Dynamics (十三勢)** from Taijiquan — Bamen (8 gates: Peng, Lü, Ji, An, Cai, Lie, Zhou, Kao) and Wubu (5 steps: JinBu, TuiBu, ZuoGu, YouPan, ZhongDing). The agent saw the fit immediately: a vocabulary of *embodied dynamic interaction* with no external units anywhere — the perfect endogeny match.

Proposed structure:

```scala
object Bearing { sealed trait Value; case object Peng/Lu/Ji/An/Cai/Lie/Zhou/Kao extends Value }
object Reach   { sealed trait Value; case object JinBu/TuiBu/ZuoGu/YouPan/ZhongDing extends Value }
object Percept { case class Value(bearing: Bearing.Value, reach: Reach.Value) }
```

Consequences worth noting (briefly): `Percept.Side` evaporated because Zuo/You already encoded laterality. The eight gates came in four opposing pairs (Peng↔Lü, Ji↔An, Cai↔Lie, Zhou↔Kao) — energetic chirality built in. Rotational sense (Clockwise/Widdershins) remained orthogonal.

Four open questions surfaced (Pinyin vs English; product vs sum; rotational chirality; discrete vs continuous), but before any answers the user pulled back:

> **Dev:** Replace Percept with Shengren

The Shengren (聖人 — sage) rename was executed across five files:
- `Percept.json/.scala` → `Shengren.json/.scala` (`git mv` + content rewrite)
- `Egocentric.json/.scala` elementTypeNames entry
- `ReferenceFramesGenTest.scala` Ty val and Family entry

Tests didn't run because sbt was broken (the JVM/Scala-skew "bad constant pool index" failure flagged in chapter 22's calibration). Then:

> **Dev:** The Bamen Wubu is to complex to implement at this stage. Roll back Shengren to Percept and let's define the dynamics within the Ego frame more comprehensively but as simply as possible beyond that.

All five edits reversed — `git status` returned to clean. The Shengren detour cost five edits forward and five back, but the scout-and-retreat clarified what *wouldn't* work: the full martial-arts vocabulary was too specific to commit to without a clear use case, and the 13-posture cardinality wasn't right for a foundation.

The Bamen Wubu detour is preserved here verbatim because it shaped the next move — the agent proposed *six embodied dynamics* (Approach/Withdraw, Leftward/Rightward, Upward/Downward) as the simplification: "more comprehensive than turn-fractions, simpler than 13."

---

## Reach → Range → Distance — Naming Pivots

> **Dev:** Reach is only useful for touch-sensors and kinetic effectors. it should be binocular determinable Range instead of Reach, don't you think?

The agent agreed strongly: Reach assumed touch capability; Range via binocular parallax covered everything visible. Proposed unit: IPD-multiples (multiples of interpupillary distance), derived from vergence angle. Endogeny preserved — IPD is bodily.

Capability commitment named: Range-as-binocular implied two eyes on a baseline. Monocular egos would compute depth differently. The Egocentric frame committed to binocular embodiment.

This rename plan got staged but then superseded by a deeper renaming pass below.

---

## Slope as Proprioceptive Sensor

> **Dev:** Should have a proprioception sensor for ground slope wrt gravity orthogonality

Slope was added as a proprioceptive scalar — vestibular-sensed, "zero = level, magnitude grows with departure from level," dimensionless tilt. Endogeny preserved because gravity is universally available to any ground-bound body.

A useful structural distinction surfaced: exteroceptive scalars (Bearing, Range) are *about perceived things*; proprioceptive scalars (Slope) are *about the ego's own body*; composite (Percept) is the exteroceptive aggregate. Slope wasn't part of Percept — it was a fact about the ego itself.

Initial Slope draft was 1D (a single tilt magnitude). The user corrected:

> **Dev:** Slope has to be 2 dimensional tangent plane to point on surface

A tangent plane has 2 DOF, not 1. Slope became `Holon[(Double, Double)]` parameterized by two orthogonal tilts (pitch, roll). This was the first surfacing of "tuple Holon for a non-perspective composite" — a pattern that would be challenged later.

---

## Paths and Waypoints — Internal State

> **Dev:** How do we characterize paths made of waypoints as internal state to use for navigating the environment autonomously?

Introduced a third category alongside exteroceptive and proprioceptive: *mnemonic/cognitive state* — things the ego carries between moments. Waypoint and Path landed:

- `Waypoint` — a remembered target, structurally same as Percept (Bearing+Range pair) but semantically a target/intention
- `Path` — ordered sequence of waypoints

Three deeper questions: Waypoint = Percept (alias) vs distinct type? Path integration (single home-vector) instead of/in addition to waypoint lists? Path cursor location?

The user immediately surfaced the missing piece:

> **Dev:** The Percept is not the navigating actor. How is the actor associated with the Percept?

The agent had been conflating perceiver and perception. The Percept is the perceived; the Ego is the perceiver. The Egocentric frame had been missing the thing the frame was *of* — the ego actor itself. Every Bearing/Range/Slope was meaningful only relative to a specific actor's body and pose; the actor is the origin of the frame.

`Ego` proposed as a new actor type, the locus that holds all the ego-frame state (current Percept, Slope, Path, cursor).

---

## Abandoned Path: Body/Head Anatomical Decomposition

> **Dev:** There need to be at least two loci for state, one for motion and one for perception. Motion is the the primary effect of the Ego body whereas head is the focus of perception. The body can orient its centerline and the head can rotate independent of body orientation. The body also has ground effectors, specialized for locomotion, and general effectors for other effects.

This expanded the Ego frame's structure significantly: two loci, decoupled gaze, dual effector types (ground vs general). The agent proposed Body and Head as types within Egocentric, possibly as sub-domains, with Body holding Orientation/Course/effectors and Head holding Gaze/Range/Percept.

The agent's recommendation was sub-domains: `domains.egocentric.body` and `domains.egocentric.head` as their own Domains. This was wrong — twice over.

> **Dev:** Head is a Feature of a Body, not a sub-domain of ego-centric. Body and Head are peer types in the Egocentric domain. Also, adding a sub-package name to a domain package indicates a domain transform, not a sub-domain. Sub-domains are usually package name peers with the super-domain.

Corrections registered:
1. Body and Head are *peer types* within Egocentric, both at `domains.egocentric`, both in `Egocentric.elementTypeNames`. Flat under one package.
2. `domains.egocentric.body` would mean a *transform* (Egocentric → Body), not a sub-domain. Sub-package nesting is the transform convention.
3. Sub-domains are *package peers*, the way Egocentric/Geocentric/Heliocentric/Galactocentric are peers beneath Cosmocentric.

The "Head is a Feature of Body" relationship had no `Feature` primitive in draco — the agent confirmed via grep. Three options surfaced (Holon composition, informal, add new primitive); the agent proposed Holon composition (`Body extends Holon[(Head, Orientation, Course, …)]`).

Then the user pulled back from the entire anatomical decomposition:

> **Dev:** I made a mistake in referring to head versus body. Speaking in terms of human/mammalian structure, there is head, torso, left fore-limb, right fore-limb, left hind-limb, right hind-limb (any tail is an extension of the spine which is part of the torso). The 4 limbs and head are all both effectors and sensors, and the torso connects and sustains everything. … This is way too detailed to incorporate into the Egocentric domain.

Body, Head, GroundEffector, GeneralEffector — all dropped. The functional separation between motion-side and perception-side stayed in the *naming* of peer types, but no anatomy was encoded structurally. The Ego actor mediates both pathways without anatomical sub-types.

---

## Holon vs Primal — Design Rule Clarified

The agent had been using `Holon[tuple]` for non-perspective composites (Percept, Slope) and was about to do the same for Body/Head. The user issued the load-bearing correction:

> **Dev:** Only perspectives can be holons, and have interiority and exteriority of sense and effect. Primal is with respect to whether a value is unitary or composed.

Holon and Primal are different axes:
- **Holon** = perspective (subjective POV with interiority/exteriority of sense and effect)
- **Primal** = value (unitary or composed)

The agent attempted to read `Holon.scala` to ground the response; the user interrupted:

> **Dev:** I don't think the definition or implementation of Holon is going to be particularly helpful at this level of discussion. Also, when I said "perspectives" that is more in terms of reference frame where what I meant was the point of view (subjective) within the frame (objective).

So: reference frame = objective container; perspective = subjective POV within it. The Egocentric *domain* is the objective frame; the `Ego` *type* is the subjective POV within it. The Ego is the only Holon in the frame — every other element is a Primal (unitary or composed).

This invalidated several existing patterns:
- `Percept = Holon[(Bearing, Range)]` should be `Primal[(Gaze, Distance)]` (composed primal, not perspective)
- `Slope = Holon[(Double, Double)]` should be `Primal[(Double, Double)]`
- Existing types in other frames (`Position`, `Fix`, `Trajectory`, `Ephemeris`, `Elements`) all use `Holon[tuple]` for what are actually composed primals — flagged for eventual reclassification, out of scope for this session.

---

## Bearing → Direction, Range → Distance — Frame-Appropriate Vocabulary

> **Dev:** Let's reconsider the need for both Orientation and Bearing. … How about just having direction and distance as more intuitive references? I think bearing and range are more appropriate to a Geocentric view.

The terms `Bearing` and `Range` carried instrumented overtones — compass-card, radar baseline, external reference frames. They belonged in Geocentric where there *is* a cardinal frame to be a bearing *of*. The Egocentric vocabulary should be pre-instrumental, phenomenological:

- `Bearing` → `Direction` (pure body-relative angle)
- `Range` → `Distance` (pre-instrumental depth, body-scale unit)

Future Geocentric.Bearing and Geocentric.Range can show up as transformed analogs of egocentric Direction and Distance — that's the kind of asymmetry that makes the transforms semantically meaningful.

This also collapsed the earlier Reach→Range rename plan: it became Reach→Distance with the existing Bearing.json/.scala renaming to Direction.json/.scala.

---

## Lean — Effect, Not Sense

The earlier discussion had Slope as a proprioceptive *input* (sensed ground tilt). The user reclassified:

> **Dev:** "Lean" is the stabilizing force for maintaining a vertical (ground => sky) posture with respect to the ground plane as determined by gravity. If the ego instance is translating in 3D, it is not required. If the point location of the ego "obeys" gravity, then "lean" is the corrective "force" necessary to keep the ego perspective vertical with respect to gravity, while traversing a 3D ground surface topographically.

Slope (the abstract ground-tangent-plane geometry) collapsed into Lean (the body's corrective postural state). The egocentric frame doesn't have privileged access to "the ground's geometry" as an external fact — the embodied ego knows its own postural-stabilization state, and that state implies the ground.

Lean moved to the **effect** side of the perspective:
- Lean is *conditional* — only ground-bound egos need it; free-floating egos produce zero/identity Lean
- Conditionality is runtime, not type-system
- Gravity is the only required external reference Lean admits — the one external fact this otherwise-radical-egocentric frame allows, because gravity is universally available

Then on coordinate parameterization:

> **Dev:** How about polar coordinates for lean with respect to direction=0?

Polar (magnitude, azimuth) replaced Cartesian (forwardBack, leftRight). Why better:
- **Embodied directness** — proprioception delivers "tilted in this direction by this much" naturally
- **Shared angular reference** with `Direction` (forward = 0 turn-fractions)
- **Level-ground graceful** — `magnitude = 0` is the singular "no lean" state
- **Free-floating graceful** — magnitude stays 0, azimuth unused, no special-casing

---

## The Implementation Pass

User accepted the design with "Yes, let's make a pass with this and refine if necessary."

The full pass touched the egocentric package and the test:

**Renames (mechanical):**
- `Bearing.json/.scala` → `Direction.json/.scala`, shape upgrade `Primal[Double]` → `Primal[(Double, Double)]` (azimuth + elevation)
- `Reach.json/.scala` → `Distance.json/.scala`, shape unchanged

**Updated:**
- `Percept.json/.scala` — `Holon[(Bearing, Reach)]` → `Primal[(Gaze, Distance)]`
- `Egocentric.json/.scala` — elementTypeNames now lists all 10 elements
- `ReferenceFramesGenTest.scala` — scaladoc, ten new `Ty` vals, family expansion, total-types count 19 → 26

**New (8 leaf type pairs + 1 actor pair):**
- `Course` — extends `Direction` (translation direction)
- `Gaze` — extends `Direction` (sensor aim)
- `Lean` — `Primal[(Double, Double)]` polar (magnitude, azimuth)
- `Effect` — `Primal[Unit]` placeholder
- `Waypoint` — `Primal[(Gaze, Distance)]` (structurally same as Percept, distinct type)
- `Path` — `Primal[Seq[Waypoint]]`
- `Ego` + `Ego.actor.json` + `EgoActor.scala` — the only `Holon`, parameterized as `Holon[(Percept, Effect)]` for sense-in / effect-out boundary

The agent had to peek at `Holon.scala` despite the earlier "don't read it" steer because the implementation needed a concrete type parameter and `Holon[T <: Product]` constrained the choice. `Holon[(Percept, Effect)]` was committed as the perspective's interiority/exteriority structure.

---

## Generator-vs-Handwritten Drift

Tests surfaced a single drift:

```
trait Course extends Extensible with Direction              ← hand-written
trait Course extends Extensible with Egocentric with Direction  ← generated
```

Same for Gaze. The JSON derivation listed both Egocentric and Direction; the Generator faithfully emitted both; the hand-written version omitted Egocentric (redundant for Scala inheritance since Direction extends Egocentric, but the Generator emits exactly what the JSON declares). 

Per the principle established in chapter 22 — "only Generator + JSON are authoring surfaces; converge by the Generator catching up, not by hand-aligning" — except in this case the Generator was already correct and the hand-written file was the one out of step. Fix: align hand-written to generator output (option b from the test message). Two `Edit` calls. Tests passed.

---

## Final Element Vocabulary — Egocentric Ten Elements

| element | kind | shape | role |
|---|---|---|---|
| `Direction` | composed primal | `Primal[(Double, Double)]` *(az, el)* | primitive |
| `Distance` | unitary primal | `Primal[Double]` *(IPD-multiples)* | primitive |
| `Course` | composed primal | extends `Direction` | motion |
| `Gaze` | composed primal | extends `Direction` | sense |
| `Percept` | composed primal | `Primal[(Gaze, Distance)]` | sense |
| `Lean` | composed primal | `Primal[(Double, Double)]` *(magnitude, azimuth)* | effect |
| `Effect` | placeholder | `Primal[Unit]` | effect |
| `Waypoint` | composed primal | `Primal[(Gaze, Distance)]` | mnemonic |
| `Path` | composed primal | `Primal[Seq[Waypoint]]` | mnemonic |
| `Ego` | holon (perspective) | `Holon[(Percept, Effect)]` | — |

Plus `Ego.actor.json` + `EgoActor.scala`.

---

## Meta-Discussion — Abandoned Paths and Conversation Cost

After the journal-chapter draft was written, the user asked an honest meta-question:

> **Dev:** Actually, I want to discuss: "It's going to be long — there were many abandoned design paths" ... to see if I need to create better prompts, reduce the number options given for my decision, reading the responses more more carefully, or something else to mitigate the issue.

The agent responded with a categorization of every abandoned path in the session (chirality types, Bamen Wubu, Shengren rename, six discrete dynamics, Body/Head sub-domains, Holon-for-tuples, Slope→Lean refinement, Bearing/Range→Direction/Distance refinement) sorted by who introduced each, who pulled it back, and what the root cause was.

The honest diagnosis: most of the cost was on the agent's side, not the user's. Three categories:

1. **Agent's option-flooding.** The agent had been listing 3–4 options at almost every fork in the conversation, even small ones. The user's prompts were terse and directional; the agent's responses fanned them out into option trees, dragging the conversation length up. The user had explicitly asked once: "Give me your current recommendations for 1, 2 and 3, and I'll correct or accept accordingly." That should have been the default mode, not the exception.
2. **Agent's misreads of existing conventions.** Two avoidable: the sub-package-equals-transform rule (was in memory; the agent missed it), and the Holon-only-for-perspectives rule (the user had to teach it explicitly). The agent followed surface patterns in existing code rather than asking about design intent for foundational types.
3. **Genuine exploratory iteration.** The Bamen Wubu / Shengren branch and the Body/Head decomposition were real design exploration — the user tried richer vocabularies, saw the implementation cost, pulled back. That's how design conversations work; it's not waste even if it costs edits.

User's prompts, in honest assessment, were not the problem. They were *dense with signal*, every line introducing something that mattered: "endogenous to the Percept," "axially with left/right and forward/backward," "Lean is a stabilizing force." Density is not the same as cramming. The user was teaching a design vocabulary one constraint at a time and the constraints landed cleanly.

The user offered a self-deprecating closing:

> **Dev:** To quote Father Guido Sarducci, "I blam-ah myself." for any complexity of your responses, based on my trying to cram to much into the prompts.

The agent's response declined that framing — the cost was on the response side, not the prompt side. Forward commitment: **stop fanning prompts out into option trees**; default to one recommendation with accept/correct, and list alternatives only when the tradeoff is genuinely close.

---

## Session Summary

### Code Changes

1. **Egocentric vocabulary redesign landed.** Ten element types under `domains.egocentric`, plus the `Ego` actor. Old `Bearing`/`Reach`/`Percept` (the latter as Holon-of-tuple) gone; new `Direction`/`Distance` plus seven new elements in.
2. **First `Primal[tuple]` use.** `Direction = Primal[(Double, Double)]`, `Lean = Primal[(Double, Double)]`, `Percept = Primal[(Gaze, Distance)]`, `Waypoint = Primal[(Gaze, Distance)]`. The Generator handled tuple type parameters on Primal correctly on first pass — the same pattern Holon already supported.
3. **First `Primal[Seq[T]]` use.** `Path = Primal[Seq[Waypoint]]`. Generator and compilation both succeeded.
4. **First `Primal[Unit]` use.** `Effect` as an uncharacterized output placeholder.
5. **First trait-derivation chain in a leaf.** `Course extends Direction`, `Gaze extends Direction`. JSON derivation lists `[Egocentric, Direction]` and the Generator faithfully emits both. Hand-written had to converge after an initial omission of `Egocentric` from the trait extends list.
6. **First Holon for a designed perspective.** `Ego = Holon[(Percept, Effect)]` — the only Holon in the new Egocentric, capturing the sense-in / effect-out boundary as the perspective's structural commitment.
7. **`EgoActor.scala`** modeled on `NaturalActor` — receive/receiveSignal stubbed with `Behaviors.same`, `actorInstance` reads `Ego.actor.json` via `SourceContent`.
8. **`ReferenceFramesGenTest`** scaled from 19 to 26 types under one family expansion. All tests pass after the Course/Gaze drift fix.

### Design Decisions

- **Endogenous semantics is the load-bearing principle for the Egocentric frame.** No external units, no SI calibration, no compass references. The body is the measuring rod; the body's facing is the angular zero; the IPD is the depth unit. Every value is meaningful only relative to *this* observer's body. External-reference vocabulary (Bearing, Range, North, meters) belongs to Geocentric and outward.
- **Holon = perspective; Primal = value.** Different axes. Holon carries interiority/exteriority of sense and effect (subjective POV within an objective reference frame). Primal is unitary-or-composed. Existing `Holon[tuple]` uses for non-perspectives in other frames (Position, Fix, Trajectory, Ephemeris, Elements) are misclassified under this rule and need eventual reclassification — out of scope for this session.
- **Lean is an effect, not a sense.** What the ego knows about the ground is its own postural-stabilization state. There is no separate "sensed ground geometry" type; the embodied agent has only the body's response.
- **No anatomical decomposition.** Body/Head/limbs/effector-categories were rejected as too detailed. Functional separation between motion-side and perception-side lives in element naming, not in structural anatomy.
- **Sub-package nesting under a domain means *transform*, not sub-domain.** `domains.egocentric.body` would be the transform Egocentric→Body. Sub-domains are package peers (the way Egocentric/Geocentric/etc. are peers beneath Cosmocentric).
- **Trait-derivation chain (`Course extends Direction`) is a viable JSON pattern.** Course's JSON lists `[Egocentric, Direction]` in its derivation — the Generator emits both; the hand-written file must include both (no shortcut from Direction's transitive inheritance).

### Calibration

- **Hand-written must converge to Generator output, not the reverse — except when the hand-written is correct and the Generator emission is already canonical.** The Course/Gaze drift was the hand-written file omitting an explicit derivation that the JSON declared. Match the JSON.
- **Don't over-research the Holon/Primal trait sources during design discussion.** The user steer was clear: the conceptual distinction is the load-bearing decision; the implementation details of the trait don't help at the design level. (Source-reading is fine during *implementation* — that's when the agent actually needed `Holon[T <: Product]` to know the type-parameter constraint.)
- **Long branching design conversations need explicit consolidation passes.** This session had multiple abandoned paths (Bamen Wubu, Shengren rename and rollback, Body/Head decomposition, six discrete dynamics) that each consumed turns. The agent's structural summaries (the table-of-elements that got rewritten ~5 times) were the load-bearing checkpoint mechanism. Without those, the conversation would have lost coherence.
- **Naming pivots are cheap if caught early.** Bearing → Direction, Range → Distance, Slope → Lean, Reach → Range → Distance — all clean renames before any transform code referenced them. Late naming pivots (when transforms exist) would be much more expensive.
- **Default response shape: one recommendation with accept/correct, not an option tree.** The biggest source of conversation length in this session was the agent fanning each user directive out into 3–4 evaluation options. The user's prompts were dense with signal — each line carried a real constraint. The right response shape is to apply the constraint and propose, not to enumerate alternatives. List options only when the tradeoff is genuinely close. Codified into `feedback_one_recommendation.md`.

### Next Session Priorities

1. **Inner primal value types for the new Egocentric scalars.** `Direction`, `Distance`, `Lean`, etc. all use bare tuple/Double type parameters today. Adding named value classes (`Direction.Value(azimuth: Double, elevation: Double)`, `Lean.Value(magnitude: Double, azimuth: Double)`) brings field-level documentation into the Scala companions. This is the original "honesty later" item from chapter 22, now applicable to the new types.
2. **Apply the same redesign principles to Geocentric, Heliocentric, Galactocentric.** The other three reference frames still have skeletal leaves (Position, Altitude, Heading, Fix; Elements, Epoch, Ephemeris; Parallax, ProperMotion, RadialVelocity, Trajectory) that pre-date the Holon/Primal distinction. Each needs reclassification (Holon → Primal for non-perspective composites) and frame-appropriate vocabulary. The transforms can't get meaningful elementTypeNames until the source/target frames have proper scalar vocabularies.
3. **Populate elementTypeNames on the 12 transform domains.** Per the user's stated sequence — scalars first across all reference frames, then transform-domain elementTypeNames. The transforms list scalar-to-scalar transforms as their elements (e.g., `EgocentricGeocentric.elementTypeNames` would name transforms like `DirectionHeading`, `DistanceAltitude`, etc.).
4. **Cosmocentric as domain** — still deferred from chapter 22. With transforms about to grow elementTypeNames, the question of how Cosmocentric hosts them (cross-package elementTypeNames vs new `hostedDomains` field) becomes pressing.
5. **`Ego.actor` first behavior.** Currently `EgoActor.receive` is a stub (`Behaviors.same`). A real first behavior — even just logging the `(Percept, Effect)` boundary — would exercise the perspective's sense/effect pathway end-to-end.
6. **The `sbt` JVM/Scala-skew fix** — flagged in this session as blocked, deferred to a maintenance project session per user direction. Tracker for chapter 22's calibration item.
