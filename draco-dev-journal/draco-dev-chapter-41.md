# Draco Dev Journal — Chapter 41

**Session date:** June 9–14, 2026 (one session, several sittings)
**Topic:** From "continue TransformBuilder" to a draco-supplied actor execution context. TransformBuilder's missing fixture became a whole example *world* of message domains (Aerial/Terrain/Marine/Ethereal under World), which forced a Format domain and the Generator's first actor-from-`actorAspect` emission — landing a green end-to-end Aerial Consumer actor+rule generated entirely from JSON. The session closed with a commit/push, a centric-vs-World verdict, and the Corpus Lucis Terrae holarchy entering the project as `HOLARCHY.md`. Transcript.

---

**[Dev]** Let's continue with TransformBuilder, where there is more design and implementation planning work

**[Claude]** *(after reading the DomainBuilder/Transform sources, chapter 40, and a concrete transform domain)* I now have the full picture. Let me lay out the design for `TransformBuilder` and surface the one real fork before writing code.

`DomainBuilder` filled a *concrete* core gap: `TypeDictionary.apply` is hollow (member names, no content), so it built the populated counterpart. For `TransformBuilder` the analogous concrete gap is this: a **transform domain** (e.g. `EgocentricGeocentric.json`) is *structurally* a domain — it has a `domainAspect`, so `DomainBuilder.define` already builds it as a plain domain. But that path **ignores everything that makes it a transform**: `dracoAspect.source`/`target`, the `DomainTransform[Source, Target]` derivation, the `domains.<source>.<target>.<Source><Target>` package convention, and the per-leaf **dual-inheritance** rule. No core machinery resolves the endpoints, validates those invariants, or composes multi-hop transforms.

Proposed API (`src/mods/scala/draco/TransformBuilder.scala`): `define(source, target, namePackage*)` → `BuiltTransform(source, transform, target)`; `dictionary(transforms*)`; `validate(t)`; `generate(source, target)`; `compose(frames*)`. The `validate` battery on top of DomainBuilder's three: (1) derivation shape — principal derives `DomainTransform[S,T]`; (2) package–endpoint consistency; (3) endpoint resolvability; (4) dual-inheritance of members — each member primal's derivation names both a source-side and a target-side TypeName.

The one real fork: `DomainBuilder`'s `validate`-to-zero contract had a natural subject (the first-party domains). For transforms there are **no first-party transform domains** — only the 12 reference-frame examples in `src/test`, which your memory marks "not a work target" and which are **skeletons** (principal only, zero member primals, so dual-inheritance has nothing to check). So I need to decide what `TransformBuilderTest` actually asserts against.

---

**[Dev]** The Centric domains got away from me in that they didn't easily allow for message type to message type cross domain transforms. I think I need you to help me define some notional message domains, that are complex enough to be distinctly different to require cross domain transforms, and realistic enough to be comparable to real message domains without compromising externally imposed information security constraints.

**[Claude]** *(after grounding in the Orion/ANION notes)* This reframes the work productively, and it also rescues the Centric domains rather than discarding them. The four reference frames are all the *same* data model (positions) under different origins — structurally identical primals, so a transform between them is just a coordinate rebase. Real message domains differ in **representation**, not origin: different field granularity, identity schemes, fragmentation, temporal models, completeness. That representational gap is what makes a cross-domain transform non-trivial, and it's exactly what the symmetric geometric frames lack. The fix isn't to throw the frames away — it's to **demote them one layer**: they become the *spatial substrate of a data domain*, and message domains transform *into* that data domain (the ANION pattern: `Message A → Data → Message B`).

Infosec stance: I'll build this as a deliberately **civilian air/maritime traffic picture** — multiple independent feeds reporting the same moving contacts in incompatible formats. Structurally identical to the hard problem but unclassified by construction, fully notional names, nothing mirroring any real spec. Proposed shape: three peer domains plus two mediated transforms — `domains.situ` (**Situ**, the data-domain truth, using the Centric frames as its spatial vocabulary), `domains.pulse` (**Pulse**, sensor-native: polar position, fragmented returns, radial-only velocity, epoch+offset time, stateful), `domains.ledger` (**Ledger**, service-native: complete geodetic snapshots, UUID + taxonomy, ISO time, stateless), with `PulseSitu` and `SituLedger` whose members genuinely dual-inherit. `compose(Pulse, Situ, Ledger)` *is* the ANION mediation, and the `Polar→Geodetic` step recovers `EgocentricGeocentric` as load-bearing.

One choice is yours before I start writing JSON — the carrier theme, since "comparable to real message domains" depends on which civilian analog reads closest to your actual work (I won't ask what that is).

---

**[Dev]** I had the idea as you were thinking that we ignore the civilian/military distinct and then consider the domains of earth, water, air and space.

**[Claude]** That's a cleaner axis than civ/mil — and it exposes something important: **medium and reference frame are orthogonal**. The Centric frames (ego/geo/helio/galacto) are about *where the origin is*; earth/water/air/space are about *what physical medium the entity lives in*. They were never competitors. The media give you the representational difference the Centric frames lacked; the Centric frames become the **geometric substrate** that medium-to-medium transforms compute *through*. Nothing built is wasted — it's relayered.

Each medium has a genuinely native state vocabulary — that's what forces real transforms:

| `domains.earth` | `domains.water` | `domains.air` | `domains.space` |
|---|---|---|---|
| `Grid` (easting/northing/zone) | `Surface` (lat/lon on sea) | `Geodetic` (lat/lon/alt MSL) | `Orbital` (Keplerian) |
| `Elevation` (AGL) | `Depth` (signed down) | `FlightLevel` (pressure alt) | `StateVector` (ECI pos+vel) |
| `Heading` + `Pace` | `Bearing` + `Knots` | `Vector` (3-D vel + climb) | `Epoch` |
| holon `Ground` | holon `Vessel` | holon `Craft` | holon `Orbiter` |

The representations have **different dimensionality and different reference data** — that's the gap. And the transforms are "distinctly different" as required: air→space is a genuine coordinate *and* dynamics transform; water→earth inverts `Depth`/`Elevation` sign with an honest partiality case; earth→air needs a terrain-height datum the earth domain doesn't carry; space→air is valid only inside an altitude band. No two are the same shape.

My recommendation: four **peer media domains + direct pairwise transforms** (not an ANION-mediated "truth" domain) — each medium *is* its own truth; there's no master representation. First cut: one transform pair end-to-end (`water↔earth` lighter, or `air→space` richer), then TransformBuilder against it. Which way do you want to go?

---

**[Dev]** We first need to create  isolated  functional intra-domain  communication message types (JSON data, not strongly typed), and then do cross domain by creating the World super-domain and creating type definitions (strong domain element types) and the World rules for creating the cross-domain transforms.

**[Claude]** Good — that's a clear sequencing: **(1)** each medium working in isolation on loose JSON messages, **(2)** then `World` super-domain with strong element types + transform rules for cross-domain. Grounding in how messages and actors actually flow: two existing pieces make Phase 1 fall out naturally — **`draco.Value`** is already exactly "read typed fields out of opaque `io.circe.Json` via a path," and actors are just `Behavior[T]`, so for loose messages `T = io.circe.Json`. Phase 1 per medium = **a JSON message** + **`Value` field-views over it** + **a `Behavior[Json]` actor**. The strong typing is deferred to exactly the point cross-domain transforms force it.

Built the **`air`** vertical slice as pattern-setter (richest medium): `AirReport.json` (loose message — geodetic position, kinematics, ident, flight level), `AirReport.scala` (`Value` field-views, **not** strong types), `AirActor.scala` (`Behavior[Json]`), `AirDomainTest.scala`. Fully isolated — nothing knows about other media or `World`. Two checks before I replicate: **(1)** is `io.circe.Json` + `draco.Value` + `Behavior[Json]` the right reading of "JSON data, not strongly typed"? **(2)** Replicate all three now, or one at a time?

---

**[Dev]** At the JSON format level the 4 domains don't exist as  defined domains. They are independently developed  message sets that are used to communicate data within a physical transport medium, initially independent of the other three media. Before transform services there is no way for semantic data to cross from one transport medium to another. Please don't try to project too far ahead about these domains being created to support TransformBuilder. We need to recapitulate the phylogeny of the draco framework domains into the ontogeny of these new example domains.

**[Claude]** The recapitulation framing changes the shape: at this stage the four media aren't domains at all — draco's own phylogeny started with format-level JSON experiments (`org.nexonix.format.json`, the pre-draco `Value`) long before TypeDefinitions and domains existed. So the examples' ontogeny should replay that: **independently developed message sets** living *outside* `domains.*`, with no domain identity, no shared schema, and no path for semantics to cross media. My `domains.air.AirReport` framing declared a domain prematurely.

Restructured. The `domains.air` files are gone; the air message set now lives in a pre-domain `media.*` namespace: `PositionReport.json` / `FlightIntent.json` (envelope discriminator `"msg": "POS"/"INT"`, air-native conventions: feet, knots, flight levels, epoch-millis), `AirMessageSet.scala` (`Value` field-views + discriminator dispatch), `AirMedium.scala` (a `Behavior[Json]` participant), `AirMediumTest.scala`. These will only *become* domains later, when World's type definitions confer domain identity on them.

One recommendation for the replication step: each of the other three message sets gets **deliberately divergent conventions** — water snake_case/degrees-true/signed-down meters, earth verbose keys/grid/km/h, space terse keys/Keplerian/Julian-date epochs. "Independently developed" should be visible in the JSON itself, because that representational divergence is precisely what will make cross-medium impossible until transform services exist.

---

**[Dev]** We need to rework the implementation of the air domain and its element types, focusing first on the data format input/output followed by the domain types and domain type elements. This needs to be an interactive dialog with terse prompt/response exchanges.

**[Claude]** Understood — terse dialog mode. Starting with data format I/O.

**Q1:** Air's I/O surface today is read-only: JSON files → Circe parse → `Value` views. For input/output, I propose a symmetric pair per message type — `read(Json): fields` and `write(fields): Json` — so the medium can both consume and emit its wire format. Is the write side in scope now, and is JSON-text-on-the-wire the I/O boundary — or do you have a different input/output notion in mind (file/stream endpoints, actor mailboxes as the boundary)?

---

**[Dev]** We need to supply from the draco framework (which means using type definitions and generating Scala, instead of manually writing it) the execution context for the example domains that show how to define and build transforms. Right now we're using JSON format exclusively for message domain data format. Part of the execution context will be actors that are message data creators, providers and consumers.

**[Claude]** Got it — the execution context itself must be draco-supplied: TypeDefinition JSON → Generator → Scala, no hand-written `AirMessageSet`/`AirMedium`. Messages stay loose JSON; the *context* around them is strongly defined.

**Q2:** Proposed structure — `domains/air/Air.json` (the domain); message shells `PositionReport.json`/`FlightIntent.json` as element types extending `Primal[Json]` — typed handles carrying the loose payload as `value`; actor roles `Creator`/`Provider`/`Consumer` as element types carrying `actorAspect`, generated as `Actor[Json]` behaviors. Two checks: **(a)** is `Primal[Json]` the right shell for message element types, and **(b)** are Creator/Provider/Consumer three domain element types (one actor each), or roles of a single actor type?

---

**[Dev]** (a)Yes -Primal[Json]
(b) Three actor types with different rule sets

**[Claude]** Three actor types, each with its own rule set — that's the pipeline: **Creator → Provider → Consumer**, each a thin membrane (`session.insert(msg); session.fire()`) whose behavior lives in its rules.

**Q3:** Proposed minimal first-cut rule sets (each a `.rule.json`, Evrete-generated): Creator/`CreatePositionReport` (stimulus fact → build `PositionReport(json)`, send to Provider); Provider/`ProvideReport` (a `PositionReport` fact → route to registered Consumer(s)); Consumer/`ConsumeReport` (a `PositionReport` fact → extract fields via `Value` paths, record digest). Cross-actor sends use ActorRefs stored in the Evrete `Environment`. Confirm: **(a)** this flow with Environment-held ActorRefs, and **(b)** start with `PositionReport` only, adding `FlightIntent` after the vertical slice works?

---

**[Dev]** Also, the examples and execution context should be in src/mods. Just tests in src/test.

**[Claude]** `src/mods` it is. Build wiring today only root-compiles `src/mods/scala/draco` — examples need two build.sbt additions (`unmanagedSourceDirectories += src/mods/scala/domains`, `unmanagedResourceDirectories += src/mods/resources`).

**Q4:** Proposed layout — `src/mods/resources/domains/air/` for the JSON definitions; `src/mods/scala/domains/air/` for **generated** Scala only (checked in); `src/test/scala/domains/air/` for tests only; `src/test/resources/media/air/` for sample wire messages — or dropped if Creator rules build the Json programmatically. The hand-written `src/test/scala/media/air/*` from last round gets deleted — superseded by the generated execution context. Confirm layout (and whether sample JSON fixtures stay or Creators originate all message data)?

---

**[Dev]** Creation and Streaming are different execution phases handled algorithmically. Streaming is loop-back through the transforms.

**[Claude]** Understood: two algorithmic execution phases — **Creation** (originate message data; no JSON fixtures, Creators compute it) and **Streaming** (continuous circulation, where a message loops back through the transforms — so streaming only becomes real once World's transform services exist).

**Q5:** For the isolated air slice: Creation phase — Creator algorithmically originates a finite message sequence (e.g. propagates NX1042 along its route, emitting successive `PositionReport`s and the `FlightIntent` that frames them), Provider routes, Consumer digests, no fixture files anywhere. Streaming phase — stubbed/absent; the loop-back path has nowhere to go until World exists. Confirm: creation-only for the air slice, streaming wired in Phase 2 when transforms close the loop?

---

**[Dev]** Confirmed

**[Claude]** *(after grounding in exemplars — rule JSON shape, actorAspect shape, the generate-test pattern)* Two framework gaps surfaced — both need your call before I author JSON (terse):

**Q6a — Generator doesn't consume `actorAspect` yet.** `ActorAspect` (messageAction/signalAction) exists as schema, but emission ignores it — every working actor (NaturalActor, EgoActor, PrimesActor) is hand-written. To draco-supply the execution context, I extend the Generator: a type with non-empty `actorAspect` gets a generated `actorType: ActorType = new Actor[Json] { receive/receiveSignal from the aspect's actions }`, with the thin-membrane body expressed as Monadics in the JSON. Confirm?

**Q6b — actor↔rule-set binding has no mechanism.** "Three actor types with different rule sets" needs the binding expressed in JSON. Today Knowledge is per-domain — all three actors would share all rules, and Provider's and Consumer's rules both pattern on `PositionReport`, so they'd cross-fire in each other's sessions. My recommendation: add `ruleTypeNames: Seq[TypeName]` to `ActorAspect`; each actor companion builds its session's Knowledge from exactly its listed rules. Accept, or different binding?

---

**[Dev]** What's missing is the draco.format.Format domain with draco.format.json.Json, draco.format.yaml.Yaml, draco.format.xml.Xml. Since we're only using Json, we can stub the other Format domains. We should keep the new domains and element types in src/mods, except for the format domain generic draco.format.Format[T] which can be in src/main

**[Claude]** Precedent checked: `draco.language` is self-contained (own `Language.json`; *not* listed in Draco's `elementTypeNames`). `draco.format` follows it.

**Q7 — Format domain shape:** `src/main/resources/draco/format/Format.json` + generated `Format.scala` — domain `draco.format.Format`, principal `Format[T] extends Primal[T]`, external content carried as `value: T`. In src/mods: `draco.format.json.Json extends Format[io.circe.Json]` (the one real format); `Yaml`/`Xml` stubs extending `Format[String]`. Air then re-targets: message shells `PositionReport extends Primal[Json]` and actor message type `T = Json` both mean **`draco.format.json.Json`**, not raw circe. (`draco.language.YAML` stays distinct — authoring language vs. transport format.)

Confirm Q7, and two still-pending calls from Q6: **(a)** Generator emits `actorType` from non-empty `actorAspect`; **(b)** `ActorAspect` gains `ruleTypeNames`. Accept a/b?

---

**[Dev]** Not sure - is a/b just a or b or can it be both a and b?

**[Claude]** Both — they're independent gaps that compose. **(a)** is *emission*: Generator turns a non-empty `actorAspect` into a generated `actorType` val. Without it, actors stay hand-written. **(b)** is *schema*: `ActorAspect` gains `ruleTypeNames` so the JSON can say *which* rules an actor's session loads. Without it, all actors share the whole domain's Knowledge and Provider/Consumer cross-fire on `PositionReport`. (a) without (b): generated actors, but no per-actor rule sets. (b) without (a): per-actor rule data with nothing consuming it. For "three actor types with different rule sets, draco-supplied," you need **both a and b**. Proceed with both?

---

**[Dev]** Yes

**[Claude]** *(A first "Yes" on the evening of June 12 started the grounding sweep but the run died on a model-selection error; the "Yes" was re-issued the next morning.)* Proceeding with both. Format layer landed first (tasks #1–3) — a self-contained, compilable increment: `Format.json`/`Format.scala` in src/main (`trait Format[T] extends Primal[T]`, domain principal, mirroring Egocentric for the domain shape and Primal for the parametric wildcard); json/yaml/xml sub-domains in src/mods (json real, yaml/xml stubs); build.sbt wired so root compiles `src/mods/scala/domains` and reads `src/mods/resources`.

A good point to compile before I touch the Generator: `sbt "reload" "compile"`. Two notes on what to watch: **DracoGenTest** sweeps `draco/*` and compares Generator output to checked-in Scala — `Format` is new, and this is the most likely place for a canonical-form nit; and root now also reads the mods resource dir — additive, but flag any double-resource warning. Next is the heavier, unverifiable-by-me surgery: **Generator (a)** actorType emission and **ActorAspect (b)** ruleTypeNames, one at a time with a compile between.

---

**[Dev]** I'm not clear on the need for ruleTypeNames, since to be in a domain a rule name is just another name in elementTypeNames which is part of DomainAspect.

**[Claude]** Good challenge — let me verify the actual rule→knowledge→session wiring before defending or dropping the field. *(reads PrimesRulesTest and the actor plumbing)* That wiring settles it — you're right, drop `ruleTypeNames`. A session's rule set is built by **explicit enumeration**:

```text
val knowledge = service.newKnowledge("…")
AddNaturalSequenceRule.ruleType.pattern.accept(knowledge)      // each rule named explicitly
RemoveCompositeNumbersRule.ruleType.pattern.accept(knowledge)
val session = knowledge.newStatefulSession()
```

There's no automatic "Knowledge-per-domain collects all rules." So the only thing an actor needs is *its list of rule names* — and a list of member names is exactly `domainAspect.elementTypeNames`. A parallel `ActorAspect.ruleTypeNames` would just duplicate that. **Revised mechanism (no schema change):** each actor type carries `domainAspect.elementTypeNames = [its rules]`, and the generated actor builds its own Knowledge by walking that list. The actor type is simultaneously an **actor** (its `actorAspect` → behavior) and a **mini-domain** (its `elementTypeNames` → rule set). No cross-fire: each actor's list is explicit and disjoint.

**The one real consequence** — it folds old-(b) into (a) rather than adding a field: `generate()` currently checks `isDomain` *before* `isActor`, so a type carrying *both* aspects routes to domain emission. The actor-emission work has to claim the combined case — "actor that owns a rule-domain" — and read `elementTypeNames` for the rule set. A dispatch refinement, not new schema. Confirm this shape and I'll proceed with the Generator emission.

---

**[Dev]** Thank you for the clarification. Reusing elementTypeNames was my intent and I did not realize the code emission condition you mentioned would not work for types that were both actor and rule. Proceed.

**[Claude]** Confirmed. Folding (b) into (a) and proceeding with the Generator surgery. *(reads the emission internals, then edits Generator.scala)* Critical safety property I'm preserving: the new branch is gated on `hasActorBehavior` (non-empty `actorAspect`), which is **false for every existing TD** — so existing emission and the DracoGenTest sweep are untouched. Two edits landed: a **dispatch branch** before `isDomain` (`hasActorBehavior(td)` → actor emission), and the **actor helpers** — `hasActorBehavior`, `actorMessageType`, `actorImports`, `actorKnowledge` (walks `elementTypeNames` → `<Rule>.ruleType.pattern.accept`), `actorActionBody`, `actorBehavior`, `actorGlobal`. A generated actor companion will look like:

```text
object Creator extends App with DracoType {
  override lazy val typeDefinition = Generator.loadType(...)
  lazy val elementTypeNames: Seq[String] = Seq("CreatePositionReport")
  private lazy val knowledge: Knowledge = {
    val k = Rule.knowledgeService.newKnowledge("Creator")
    CreatePositionReportRule.ruleType.pattern.accept(k)
    k
  }
  lazy val actorType: ActorType = new Actor[draco.format.json.Json] { ...receive/receiveSignal from messageAction... }
}
```

**This is a clean compile checkpoint** — the new code only *runs* when an actor TD exists, which doesn't yet. Next turn is task #6 — the air execution context, which actually exercises this emission path via a generate-test. Proceed to the air context now, or compile the foundation first?

---

**[Dev]** Before we continue I want to remap the domain names: air => Aerial,  earth  => Terrain,  sea => Marine, space => Ethereal, with the supervening domain being World.

**[Claude]** Clean remap, and it's well-timed — nothing strongly-typed carries the old names yet. Old `media/air` files deleted, plan + memory re-keyed: air → **Aerial** (`domains.aerial`), earth → **Terrain**, sea/water → **Marine**, space → **Ethereal**, supervening **World** (`domains.world`). All under `src/mods`; tests in `src/test`; transforms later as `domains.<src>.<tgt>`, with World hosting the cross-domain transform rules — peer-matrix layout mirroring the Centric precedent. Nothing else carried the old names — Format and the Generator are name-agnostic, so this was a clean swap. Ready to build the **Aerial** execution context (task #6). Proceed, or compile the foundation first?

---

**[Dev]** I just did sbt reload, clean and compile if that's what you meant by "compile the foundation first". If so, proceed.

**[Claude]** Good — foundation compiles. Proceeding to the Aerial context. Given I can't compile here, I'll build it in low-risk sub-steps with a compile between. Grounding surfaced a decision: none of the canonical leaves use factories — the factory/actor shapes I'd be *guessing*. Better to use the real draco workflow: I author the **JSON**, and the **Generator produces the Scala** when you run a gen-test — no hand-written "generated" code to drift. (`DracoGenTest` *compares* rather than writes, and only sweeps `draco/*`, so `domains.aerial` needs a dedicated runner.)

So task #6 splits. **Sub-step 6a now** = the domain + message-shell layer, mirroring `Language` (domain) and `Distance` (factory-less leaf) verbatim — zero new-emission risk: [Aerial.json + Aerial.scala], [PositionReport.json + PositionReport.scala] (`extends Aerial with Json` — a strong handle around loose `io.circe.Json`), and `AerialShellTest`, which proves the `draco.format.json.Json ← Format[io.circe.Json] ← Primal` chain composes and `loadType` resolves the mods-resources JSON. **6b is the real test of the Generator surgery** — Consumer + rules, where the loop is: I author the JSON, you run a generate-step to emit the Scala, and we fix any emission bug the compile surfaces. Compile 6a first, or go straight to authoring the 6b actor/rule JSON?

---

**[Dev]** Go to 6b

**[Claude]** 6b is the real exercise of the new emission. Before scoping it, I checked whether the codebase has a working pattern for the intricate part — a rule action sending to another actor via the Evrete Environment. **No existing pattern** — that's unproven framework territory. So 6b scopes down to a **single Consumer actor + one rule** (the minimal, safe exercise of the new emission), deferring the Creator→Provider cross-actor chain to 6c, which needs framework design that doesn't exist yet.

Four files authored — the actor/rule Scala is **generated by you**, not hand-written: `ConsumeReport.rule.json` (patterns on `PositionReport`, records its payload), `Consumer.json` (`Actor[draco.format.json.Json]`, thin membrane, `elementTypeNames=[ConsumeReport]`), `AerialSink.scala` (hand-written observability boundary), `AerialGenTest.scala` (gen-runner that writes `Consumer.scala` + `ConsumeReportRule.scala` and prints both). I gave the expected `Consumer.scala` emission so you can eyeball actual-vs-expected — this is the first live output of the new emission path.

*(Delegated action — Dev ran `AerialGenTest`: both files written; the printed `Consumer.scala` and `ConsumeReportRule.scala` matched the intended emission exactly.)*

That output is exactly the intended emission — the new actor path works end-to-end through the Generator. Behavioral test added (`AerialActorTest.scala`). **Two known risks to watch**, in the order they'd surface: **(1) Compile — name collision.** `ConsumeReportRule.scala` has both `import java.util.function.Consumer` and, in its own package, the `Consumer` *actor* object. The explicit import (precedence 2) should outrank a same-package definition from another compilation unit (precedence 4) — but if it errors as ambiguous, the fix is Generator-side (alias the functional import). **(2) Runtime — Evrete fact typing.** The test inserts an anonymous `new PositionReport {…}` while the rule declares `classOf[PositionReport]`. If Evrete's resolver doesn't walk the synthetic subclass up to `PositionReport`, the rule won't fire — fix would be `insertAs`, but Evrete usually handles supertype matching.

*(Delegated action — Dev ran `AerialShellTest` + `AerialActorTest`: **4/4 succeeded**.)*

All four green — and both flagged risks evaporated: the `Consumer`/`java.util.function.Consumer` collision resolved by import precedence, and Evrete matched the anonymous `PositionReport` against `classOf[PositionReport]`, so the rule fired both directly and through the actor. This validates the **entire new path end-to-end**: Format domain → Generator actor emission → a generated actor whose `knowledge` is built from its `elementTypeNames` → thin-membrane receive firing a generated rule → all from JSON, all resolved off the mods classpath. The draco-supplied execution context works. *(Wrote this chapter and updated memory.)*

**6c is the next design problem, not just authoring.** The Creator→Provider chain needs a rule to cause a send to another actor — and the grep confirmed **no such pattern exists** in the codebase (only `actorSystem ! msg` from test code, never from a rule RHS). My recommendation on record: keep sends in the actor membrane, not the rule — the rule computes output facts, the actor drains the session after `fire()` and forwards. The open question that decides 6c's shape: **how the generated actor learns its downstream `ActorRef`** — spawn the downstream in `setup`, or pass refs in the first message. Want me to work up the 6c design, or pause at this clean green checkpoint?

---

**[Dev]** Let's create the next git record for a commit and push to get ready for a new session.

**[Claude]** *(after checking the record format and tree state)* Created `git-record-2026-06-14-1246`. What it does: **`git add -A`** stages everything in one sweep — the `air`/`media` experiment files (staged-add + worktree-delete) net to **nothing** in history, and the unstaged `M build.sbt` gets included. **One plain commit** (no version bump, no tag — the Aerial work is mid-stream at 6c), titled *"Format domain + Generator actor emission + Aerial Phase-1 execution context"*, with sectioned detail and the abandoned-paths note (ruleTypeNames, the air/media namespace). **`git push`** to `main`. One rider to know about: `release-notes-v2.0.0-alpha.5.md` was staged before this session began — `git add -A` lands it now; restore it first if you'd rather it not ride along.

*(Delegated action — Dev ran the record: commit `96d39ac`, 25 files changed, 945 insertions; pushed `ca45694..96d39ac` to main.)*

Pushed clean — the `air`/`media` experiment left no trace (absent from the create-mode list, exactly as intended). Tree is ready for a fresh session. The pickup point is captured in three places a new instance will find: this journal chapter, memory (`project_media_domains_example.md` with the **6c blocker** — rule-RHS→`ActorRef` via Evrete Environment has no existing pattern), and the commit message itself.

---

**[Dev]** Before we move to the new session:  Conceptually compare the *centric domains with the World domains to see if the difference has enough distinctive value to keep the *centric domains versus just deleting them now.

**[Claude]** *(after grounding in what's actually in each corpus)* The factual picture: the 12 transform sub-domains are 1 JSON each — pure principal-only skeletons embodying *structure* (the 4×3 matrix, `DomainTransform[S,T]`, the `domains.X.Y` convention) and nothing else. Egocentric has 11 JSON (the real endogenous vocabulary + Ego actor); Geo/Helio/Galacto 4–5 each; Cosmocentric 1. **Nothing in src/main or src/mods depends on centric.**

The conceptual comparison: *centric* is about where the *origin/observer* is — one model re-expressed per origin, so its transforms are **coordinate rebases** (symmetric, invertible, lossless). *World* is about what *physical medium* the entity is in — genuinely different native models, so its transforms are **representational translations** (asymmetric, lossy, structure-changing). Two genuinely different *kinds* of transform — centric's only strong distinctive-value claim. But the catch: **World will need the geometric kind anyway** (Aerial↔Ethereal *is* a coordinate transform at its core), and keeping centric as a separate empty matrix doesn't supply that.

Verdict — two very unequal pools: **(1)** the transform apparatus (Cosmocentric + 12 transforms + the 3 thin frames): **low distinctive value** — skeletons, maintenance drag, and the transform *capability* lives in core, so deleting them costs TransformBuilder nothing. **(2) Egocentric: high, orthogonal value** — but it's *not a transform example*; it's the endogenous-vocabulary exemplar (Holon=perspective, body-as-measuring-rod, the `Primal[tuple]`/`Primal[Seq]`/`Primal[Unit]` firsts, a working Ego actor). World exercises none of this. Recommendation: **delete the centric transform corpus**; the one real decision is Egocentric — extract it as a standalone "endogenous vocabulary / Holon" example (re-rooting it off Holon, trimming ReferenceFramesGenTest), or delete the whole corpus. My lean is extract-Egocentric — it's the only piece you can't cheaply reproduce.

---

**[Dev]** You have nailed it. Ecocentric is the "root" perspective in a holarchy of perspectives. I actually have an existing description of an eight levels of nested holons. You want to look at that with me? It's at my website wuwei.org. I can steer you through it.

**[Claude]** Yes — I'd like to see it. That holarchy directly informs where Egocentric sits and how a "holarchy of perspectives" would shape the framework. *(fetched wuwei.org)* The landing page is just the foundation's overview — no holon content there. It exposes: **Corpus Lucis Terrae**, **Eight Gates and Five Steps**, **Internal Arts**, **Dao**, **About**. "Eight Gates and Five Steps" jumps out given "eight levels," and "Corpus Lucis Terrae" reads like it could frame a nested structure — but I'd rather you steer than guess. Which one has the eight nested holons?

---

**[Dev]** Corpus Lucis Terrae

**[Claude]** This is striking — and it reframes the centric decision entirely. The structure:

| Level | Holon | Perspective |
|---|---|---|
| 1 (root) | **Source Soul** | Egocentric / Individual |
| 2 | Soul Kin | Family |
| 3 | Kin Hold | Extended Family |
| 4 | Hold Clan | Clan |
| 5 | Clan Tribe | Tribe |
| 6 | Tribe Terrain | Terrain / Region |
| 7 | Terrain World | World / Planet |
| 8 | World Star | Cosmic / All Worlds |

Three connections to draco jump out immediately: **(1) The naming convention is already draco's transform convention.** Each holon is named by the *boundary between two adjacent levels* — `Source·Soul`, `Soul·Kin`, `Kin·Hold`… A holon *is* the membrane between an inner and an outer perspective. That is exactly `DomainTransform[S,T]` and the `domains.<inner>.<outer>` / `<Source><Target>` naming. The holarchy and the transform structure are the same shape. **(2) Egocentric = Source Soul = the root, confirmed** — and crucially, the *centric frames were a *placeholder* holarchy (perspective-by-astronomical-origin); this is the *real* one (perspective-by-holonic-scale). So centric isn't just "redundant with World" — it's a rough first sketch of *this*. **(3) `Terrain` (6) and `World` (7) are levels here** — which is where your media-domain names came from. And "covenants… right relationships between levels" reads precisely as the **transform rules** between holons; "Sicut Supra – Sic Infra" is the recursive self-similarity.

Steer me: dig into how the **eight levels map onto draco's perspective/Holon types**, or into the **covenants as inter-level transform rules** — and how do the physical media sit relative to these scale-levels, given `Terrain`/`World` appear in both?

---

**[Dev]** All I need is that this is available and reference-able in the workflow. If you characterize it in it's own permanent document in this project I think that will be enough for now. We can expand and refine it incrementally as it becomes relevant to implementation.

**[Claude]** *(checked repo doc conventions — no `docs/` dir; root-level conceptual docs are the pattern)* Captured and reference-able: **`HOLARCHY.md`** at the root, alongside DRACO.md/ORION.md — the eight levels, the source attribution, the three structural correspondences to draco (holon-naming = transform-naming, Egocentric=root, covenants=inter-level rules), the media-domain name overlap, and an explicit *Open questions* section for incremental refinement — marked clearly as a provisional conceptual reference. Plus a memory pointer (`reference_holarchy.md` + a MEMORY.md index line) so a fresh session surfaces it. `HOLARCHY.md` is currently untracked — it'll ride the next git record. Nothing else outstanding; the tree and the pickup notes (6c next) are intact for the new session.

## Status
Aerial 6a+6b green (`AerialShellTest` + `AerialActorTest`, **4/4**): a Generator-produced actor, its `knowledge` assembled from `elementTypeNames`, firing a generated rule through a thin-membrane receive, all from JSON. Landed: `draco.format.Format[T]` (src/main) + json/yaml/xml sub-domains (src/mods); Generator `hasActorBehavior` dispatch branch (checked before `isDomain`) + actor emission helpers; build.sbt mods wiring; name remap Aerial/Terrain/Marine/Ethereal under World. Abandoned paths: `ActorAspect.ruleTypeNames` (superseded by reusing `elementTypeNames`), the pre-domain `media/air` namespace (deleted, no git trace). Committed and pushed: `96d39ac` (25 files, 945 insertions). Post-commit: centric-corpus verdict (delete the transform apparatus; extract-Egocentric recommended, decision open) and `HOLARCHY.md` (untracked) capturing the Corpus Lucis Terrae eight-level holarchy. Next: **6c** — Creator→Provider cross-actor sends, blocked on a rule-RHS→`ActorRef` pattern that does not yet exist (framework design, not JSON authoring).
