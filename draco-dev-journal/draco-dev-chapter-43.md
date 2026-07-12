# Draco Dev Journal — Chapter 43

**Session date:** June 14–15, 2026
**Topic:** Two arcs in one session. First a design dialogue that reframed what the actor/rule machinery is *for* — generation itself, in a language-parameterized `Generator[L]` — and pinned the litmus that keeps draco portable (`src/main` definitive, `src/mods` non-definitive). Then, back to the concrete: completing the message-domain fixture from one medium to four — Aerial, Terrestrial, Marine, Ethereal — with the vocabulary carefully chosen so every cross-medium transform is non-trivial. All four Phase-1 pipelines green (13 tests). Transcript.

---

**[Dev]** The Format[T] transforms are type-primitive based whether it's within the format domain (e.g. Json => Json) or between different format domains (e.g. Json => Xml). Since we only have Json format (for now) the only two general possibilities for actor receive message types for consumer ActorRef are Format[Json] or Domain[T:< DomainType]. You may need more context to answer you questions.

**[Claude]** That context is exactly what I needed — it pins down the `???` in `actorType(provider: ActorRef[???])`. Let me play it back to make sure I've got it, then show how it collapses schema question (1).

**The two-axis model, as I now hold it:**

- A **Format transform** is *type-primitive based*: it moves the `T` of `Format[T]` — `Json ⇒ Json` (reshape, same primitive) or `Json ⇒ Xml` (`io.circe.Json ⇒ String`, cross-format). Representational, not semantic.
- A **Domain transform** moves `Domain[T <: DomainType]` — `Aerial ⇒ Marine`. Semantic, medium-to-medium.
- So a channel's message type — the type parameter of the downstream `ActorRef` — is drawn from exactly `{ Format[primitive], Domain[T <: DomainType] }`, nothing else.

**The key consequence for Phase 1.** Today every Aerial channel is `Format[Json]`, and that's *already* the actor's own `Actor[draco.format.json.Json]` parameter. With only one format and a single medium, **the downstream ref type equals the actor's own message type** — a relay/originate within a medium never changes the channel type. So in Phase 1 the Generator doesn't need a per-role *type* at all; it only needs the *role names*. `Domain[T]` is the second possibility but it can't bite until Phase 2 brings a second medium or a second format.

**That shrinks schema (1) to role-names-only** — `actorAspect.downstream: ["consumer"]`, from which the Generator emits, per role, a factory param typed as the actor's own message type plus the `session.set("consumer", consumer)` seed line. No `Format[Json]` vs `Domain[T]` discriminator needed yet — it's implied by the actor's `Actor[T]`. That field only grows an explicit type when a downstream channel diverges from the actor's own, which is a Phase-2 event.

**The one thing I'd still want your intent on (deferrable):** when Phase 2 *does* diverge, will cross-medium actors exchange **Format primitives** (`Json ⇒ Xml`, channel stays `Format[_]`) or **Domain containers** (`Domain[Aerial] ⇒ Domain[Marine]`, channel becomes `Domain[T]`)? That decides whether a `downstream` entry's future type field names a format primitive or a `DomainType` — but I don't need to bake it now.

So: shall I implement schema (1) as `actorAspect.downstream: [roleNames]` and fold it into the Generator, leaving the Domain[T] axis for Phase 2? Or have I mis-mapped the model anywhere?

---

**[Dev]** Format[Json] => Format[Xml] assumes format conversion for a specific message-type-value. Format[Json] => Format[Draco] assumes conversion of type definition generating a draco type, Format[Json] => Domain[T] assumes conversiion from Json into a subtype-value of type T.

**[Claude]** Got it — and the `Format[Draco]` case is the one that reorganizes the picture. Let me reflect it back, because it reconciles "three conversions" with the "two channel types" you gave before.

**The three conversions sort into two kinds:**

| Conversion | What it produces | Kind |
|---|---|---|
| `Format[Json] => Format[Xml]` | the *same* message-type-value, re-serialized | runtime, value-level (representation change) |
| `Format[Json] => Domain[T]` | a strong subtype-value of `T` decoded from loose JSON | runtime, value-level (representation → semantics) |
| `Format[Json] => Format[Draco]` | a **draco type** generated from a TypeDefinition JSON | build-time, **meta**-level (this is the Generator) |

So the reconciliation with your earlier message: a *consumer ActorRef's* message type ranges over only `{ Format[_], Domain[T] }` precisely because those are the two **runtime, value-level** outputs. `Format[Draco]` is never a message channel — it's the Generator itself, the self-describing closure (JSON definition ⇒ draco type). That's why it didn't appear in the channel list even though it's a real conversion.

**Placing the Aerial work on this map:** `Creator.originate` (FlightIntent ⇒ PositionReport) is a degenerate row-1 — a value reshape that *stays* inside `Format[Json]`; the Consumer's eventual "lift loose JSON into a typed Aerial member" would be row-2 (`Format[Json] => Domain[Aerial]`). Everything Phase-1 is therefore still `Format[Json]` end to end.

**Schema consequence — unchanged for now, but the type field is bounded:** when a `downstream` role's type *is* declared explicitly (Phase 2), it ranges over `{ Format[primitive], Domain[T] }` — never `Format[Draco]`, since that's the build-time Generator path, not a send.

Have I split runtime-channel vs build-time-meta the way you intend — i.e., `Format[Draco]` is the Generator and stays off the actor channels?

---

**[Dev]** When Generator becomes a programming language parameterized super-domain, then actors and rules will do the work.

**[Claude]** That reframes what the Aerial example actually was. Let me state the convergence as I now see it, because it changes where the next effort goes.

**Generation is itself a conversion — and the substrate I just built is the one it runs on.**

`Format[Json] => Format[Draco]` (parse a TypeDefinition into a draco type) followed by `Format[Draco] => Format[L]` (emit it in language L) *is* the Generator. Today `draco.Generator` is the hand-written, imperative special-case with **L hard-wired to Scala**. `Generator[L]` parameterizes L — and once it does, each emission decision stops being an imperative branch and becomes a **rule** (pattern over a TypeDefinition shape → emit a fragment), with **actors** pipelining those rules and handing partial output across the **Environment seam** — structurally identical to Creator → Provider → Consumer.

So the Aerial pipeline was never really TransformBuilder's fixture. It was the dress rehearsal for how `Generator[L]` does its own work: message domains and code generation are the same actor+rule machinery, one carrying flight positions, the other carrying TypeDefinitions becoming source. The conversion taxonomy is the rule set; L is the parameter that selects which target-language emission rules fire.

**Consequence for the move I was about to make:** folding schema (1) into the *imperative* `draco.Generator` now runs against the grain of what you just said — that's growing the stand-in, not the durable form. So my recommendation flips: **don't hand-extend the imperative Generator for the example.** Leave Provider/Creator hand-written — they're a validated mechanism proof, not unfinished business — and let the next real increment be the first *generation* rule+actor in `draco.generator`: an actor whose rule matches a TypeDefinition carrying `actorAspect` and emits the actor-companion Scala, with L=Scala as the first instantiation of `Generator[L]`.

Is that the direction — pivot from extending the imperative engine to standing up `Generator[L]` as the actor+rule domain — or do you want the Aerial loop fully closed (the minimal imperative fold) before that pivot?

---

**[Dev]** If the language parameterization is going to be useful, then everything in draco has to be generated from definitions, including Evrete, Pekko and CIrce, Otherwise DRACO cannot self-port to another language like Haskell. Creating and keeping definitions that generate code are a long term goal so any draco user can use any appropriate programming language.

**[Claude]** That's the acid test, and it sharpens every design choice before it.

**Self-port to Haskell is the validating criterion for "everything is generated."** It fails the instant any hand-written code carries a Scala-only coupling that isn't itself the output of a definition. And right now the heaviest such couplings are precisely the three external services: Evrete (`org.evrete.api.*`), Pekko (`org.apache.pekko.*`), and Circe (`io.circe.*`). They appear as literal imports and literal types inside generated code — so today the Generator emits Scala-bound source even from language-neutral definitions. Haskell has rule engines, actor libraries, and JSON libraries too, but none of those import strings survive the port.

**The principle that keeps the door open:** a definition must never name an *implementation* type (`org.evrete...StatefulSession`, `org.apache.pekko...ActorRef`, `io.circe.Json`). It names a **capability** type — `draco.rete.Session`, `draco.actor.Ref`, `draco.format.json.Json` — and `Generator[L]` holds the binding table that resolves each capability type to a concrete library *for language L*. The import table hand-coded inside today's `draco.Generator` (`externalTypeImports`) is exactly that binding layer in embryo — for L=Scala only.

**Where we already are on this:** `draco.format.Format[T]` is the first capability domain — Circe lives *behind* it (`Json extends Format[io.circe.Json]`). The two missing peers are obvious: **`draco.rete`** (absorbs Evrete) and **`draco.actor`** (absorbs Pekko). The Aerial actors I just wrote deliberately violate this (they touch `org.evrete`/`org.apache.pekko`/`io.circe` directly) — fine for a mechanism proof, but they map *exactly* the surfaces those two capability domains must cover.

So the long-term spine is: **drive hand-written Scala to zero, then drive raw external couplings to zero by hiding Evrete/Pekko behind `draco.rete`/`draco.actor` the way Circe is already hidden behind `draco.format`.** Only then is L genuinely free.

*(Recorded as the north-star memory `project_self_port_capability_domains`.)* My standing recommendation for the next concrete increment is to stand up `draco.rete` and `draco.actor` as capability domains mirroring `draco.format` — but that's a real architectural commitment, so I'll leave the entry point to you rather than start it unprompted.

---

**[Dev]** Having non-definitive implementations (that's what the dependencies are) are absolutely necessary, which is one reason why src/mods exists. The criteria having a definition is if it's going into src/main, which is what will happen to Generator once it becomes a domain.

**[Claude]** That corrects my mental model in an important way — I had the litmus wrong. Let me restate it the way you've framed it:

**`src/main` ⟺ definition-backed (definitive). Non-definitive code — the dependency bindings, the actual library glue — lives in `src/mods`, and is necessary.** You can't define your way to the bottom; something must bind to a real Evrete, a real Pekko, a real Circe. That binding is non-definitive by nature, and `src/mods` exists precisely to hold it. "Has a definition" isn't a universal aspiration — it's the *entry criterion for `src/main`*.

So I was wrong a moment ago to call the Aerial actors (and the imperative `draco.Generator`) "violations" for touching `org.evrete`/`org.apache.pekko`/`io.circe`. They're in `src/mods` — they're **correctly-placed non-definitive implementations**, not defects. The "no raw external types" rule applies to `src/main` only.

And the pattern is already realized: `draco.format.Format[T]` is in `src/main` (definitive, abstract), while `draco.format.json.Json` — the binding to `io.circe.Json` — is in `src/mods` (non-definitive). The definition/binding split *is* the main/mods split. `draco.rete` and `draco.actor` would follow the same shape: abstract capability definitions in main, Evrete/Pekko bindings in mods.

That also fixes how self-porting actually works: you **don't drive the couplings to zero** — you localize them to the swappable mods layer. The `src/main` definitions are the portable invariant; each target language supplies *its own* non-definitive mods bindings. Porting to Haskell = regenerate `src/main` for L=Haskell from the shared definitions, then write a Haskell mods layer binding a Haskell rule engine / actor lib / JSON lib. Evrete never leaves; it just stays in the Scala mods layer, replaceable.

And `Generator` "becoming a domain" is exactly the moment it crosses from mods into main — at which point it must be definition-backed, while the hand-written imperative engine stays in mods as the non-definitive bootstrap.

*(Corrected the just-written memory and index, which carried the wrong "couplings to zero" framing.)* This refines, rather than replaces, the earlier conclusion: extending the imperative Generator in mods is legitimate ongoing work, not "growing a stand-in to avoid." No code changed this turn — it was model alignment, now captured.

---

**[Dev]** Back to the example domains for supporting TransformBuilder. Where are we?

**[Claude]** *(after grounding in the actual file state)* Here's the map.

**One medium exists — Aerial — and its Phase 1 is complete and green:** the `Aerial` domain + two loose-JSON message shells (`FlightIntent`, `PositionReport`) behind strong `Format[Json]` handles; the full **Creator → Provider → Consumer** execution context, with algorithmic origination, passing both `AerialChainTest` cases; and the framework capability that was actually blocking it — a rule RHS reaching a downstream `ActorRef` via the Evrete **Environment seam**.

**Nothing else of the example is built yet:** no second medium, no `World` super-domain, no cross-domain transform types/rules, and no `TransformBuilder`.

TransformBuilder exists to populate **message→message cross-domain transforms**. A transform needs two endpoints with *divergent* native representations. **Right now there's exactly one medium, so there is no transform for TransformBuilder to build.** The gap from here to "TransformBuilder has a real subject" is, in order:

1. **A second medium** (Terrain or Marine) — its representation *deliberately divergent* from Aerial's (different fields, units, structure), because that divergence is the entire point.
2. **The `World` super-domain** — the supervening domain the transform computes *through* (also where the retired Centric frames return, as World's geometric substrate).
3. **Strong cross-domain element types + transform rules** — Phase 2, which *is* TransformBuilder's home.

My recommendation: build the **second medium next**. The one design decision worth making upfront is *what* the second medium is and how its representation diverges from Aerial. Want me to stand up Terrain as that second medium, or would you rather pick the medium/representation first?

---

**[Dev]** Terrain in Holarchy is more general than in the example domain. In The example domain is just "solid ground" or "land surface". In Holarchy, it is limited region that supports a distinct ecology an climate.

**[Claude]** Understood — same word, two different axes. Let me pin it so I don't conflate them:

- **Example `Terrain` = "solid ground / land surface"** — a *physical transport medium*, peer of Aerial / Marine / Ethereal (air / land / sea / space).
- **Holarchy `Terrain` = "a limited region that supports a distinct ecology and climate"** — a *perspective level* (`Tribe·Terrain`, `Terrain·World`), at region/biome scale.

The media names were *borrowed* from the holarchy, but the senses diverge: a physical medium is orthogonal to a holarchic scale. The example uses the medium sense only; the holarchy's ecology/climate semantics stay out of it.

The useful consequence for the build: land-surface Terrain pushes its native representation toward **ground-referenced** data — grid reference, elevation, heading-over-ground — which is genuinely divergent from Aerial's airspace fields. That divergence is exactly what a future `Aerial ⇒ Terrain` transform has to bridge.

*(Recorded the distinction in the holarchy reference memory.)* The one thing worth your call, since the collision with a holarchy level is real: **keep the name `Terrain`** (understood as land surface) **or give the example medium an unambiguous name** (Ground / Surface / Land)? My lean is to keep `Terrain` — the clash is nominal — but I'd rather you decide before I commit a package name.

---

**[Dev]** Is there anything linguistically comparable to Aerial and Marine to replace Terrain?

**[Claude]** Yes — the natural peer is **Terrestrial**.

"Aerial, marine, terrestrial" is the canonical habitat triad in biology, so it reads instantly as "of the land," and it's the same register as the others: a Latinate medium-adjective (*terra* + *-estrial*, like *aer* + *-ial*, *mare* + *-ine*, *aether* + *-eal*). Crucially it does **not** collide with the holarchy *level* `Terrain` — different word, so the disambiguation you just drew survives.

The set would be: **Aerial · Marine · Terrestrial · Ethereal**.

One caveat worth naming: *terrestrial* also means "of planet Earth," which brushes against your `World` super-domain. Inside a four-medium set it reads as "land," not "planet," so the overlap is mild. If that planet overtone bothers you, the precise collision-free alternative is **Telluric** (from *tellus* = ground/soil) — same adjectival register, no planet sense, no `Terrain` clash — just less common, with a faint geophysics flavor.

My recommendation: **Terrestrial** — it's the true linguistic sibling of Aerial and Marine and the foursome sounds coherent. Which do you want me to use for the package and types?

---

**[Dev]** That occurred to me as well, but you justified it better. Let's go with that.

**[Claude]** Terrestrial it is — land-surface medium, package `domains.terrestrial`, peer of Aerial. I'll build it as a full Phase-1 peer (mirroring Aerial's now-proven shape) with a deliberately **divergent ground-referenced representation** so a future `Aerial ⇒ Terrestrial` transform is non-trivial. Message types parallel Aerial's pair: `MarchIntent` (seed) → `GroundReport` (propagated).

*(Built the full Phase-1 peer: 9 JSON + 10 Scala + 1 test — `domains.terrestrial` domain + `MarchIntent`/`GroundReport` shells, Creator → Provider → Consumer over the same Environment seam, `TerrestrialSink`, 3 rules, `TerrestrialChainTest`. All JSON validated.)*

The **divergence** that makes it useful to TransformBuilder:

| | Aerial | Terrestrial |
|---|---|---|
| msg-type key/value | `msg: "POS"` | `type: "GROUND_TRACK"` |
| identity key | `callsign` | `unit` |
| altitude | `altFt` (feet) | `elevM` (metres) |

Origination is the parallel of Aerial's `FL→ft`: `elevFt → elevM` via `(ft*3048)/10000`, so `900 ft → 274 m` (the test asserts `274`).

**Your move:** compile and run both chains — `sbt "testOnly domains.aerial.AerialChainTest domains.terrestrial.TerrestrialChainTest"`. It's mechanical mirroring of proven shapes, so I expect green. Once it passes, we have the **two divergent media** that were the missing precondition.

---

**[Dev]** The term "track" indicates a hunter perspective. What would term would you suggest for a neutral perspective?

**[Claude]** Good catch — "track" carries the pursuer's frame (following a quarry's spoor). Same leak as "contact" (a detector's frame) or "sighting" (an observer's). The neutral framing is the bare fact of *where the entity is*, with no one watching or chasing it.

My suggestion: **position** — `"GROUND_TRACK"` → `"GROUND_POSITION"` (or `"GROUND_POS"`).

It's perspective-free: an entity *has* a position regardless of who observes it. It's also the conceptually honest choice, because all four media fundamentally report the same thing — where something is — and they diverge only in *representation*, not in concept. Aerial already uses the neutral `"POS"`; "track" was the anomaly that smuggled in a viewpoint.

If you'd rather Terrestrial's tag read distinctly from Aerial's, two other genuinely neutral options: **fix** (a determined point — `"GROUND_FIX"`) or **location** (`"GROUND_LOCATION"`). All three avoid the hunter frame; I'd take `position` for honesty, `fix` if you want visual distinctness. Want me to make it `GROUND_POSITION`, or pick one of the alternatives?

---

**[Dev]** For these examples abbreviations seem unnecessary. Also "ground" seems to be the same as "terrestrial" so I'm not sure why both are needed. What do you think?

**[Claude]** I agree on both counts, and the second one actually improves the design.

**Abbreviations:** agreed — in a pedagogical example they cost clarity for no benefit. Spell them out everywhere: `POS → POSITION`, `altFt → altitudeFeet`, `elevM → elevationMetres`, `elevFt → elevationFeet` (and `msg → message` while we're at it).

**"ground" == "terrestrial":** also agreed — the domain already establishes the medium, so `GROUND_…` and `GroundReport` are saying "terrestrial" twice. Dropping "ground" has a bonus I like: it makes the two media properly **symmetric** — both get a `PositionReport`, and the divergence collapses entirely onto the *representation* (keys + units), which is the honest picture of what a transform bridges. A cosmetic tag difference was never the real divergence; the fields are.

So the shape becomes:

```
domains.aerial.PositionReport       {"message":"POSITION", "callsign":"NX1042", "altitudeFeet":35000}
domains.terrestrial.PositionReport  {"type":"POSITION",    "unit":"NX5500",     "elevationMetres":274}
```

I'd keep `FlightIntent` / `MarchIntent` as-is — "flight" and "march" are the medium's *activity*, not a synonym for the medium-noun the way "ground" is for "terrestrial." This touches both media for consistency (Aerial gets the un-abbreviation too). Want me to apply it across both?

---

**[Dev]** I do like "location" for terrestrial because it weakly implies fixed.

**[Claude]** "Location" it is — and the position/location split is a nice semantic divergence on top of the representational one: *position* (Aerial) reads as a point in motion, *location* (Terrestrial) weakly implies fixed/emplaced. That makes `Aerial ⇒ Terrestrial` a genuine reframing, not just a unit conversion.

Final naming, applying all three agreed changes (un-abbreviate, drop redundant "ground", neutral concept):

```
domains.aerial.PositionReport       {"message":"POSITION", "callsign":"NX1042", "altitudeFeet":35000}
domains.terrestrial.LocationReport  {"type":"LOCATION",    "unit":"NX5500",     "elevationMetres":274}
```

*(Applied across both media: Terrestrial `GroundReport → LocationReport` — type + files + all refs, tag `"GROUND_TRACK" → "LOCATION"`, keys `elevM/elevFt → elevationMetres/elevationFeet`; Aerial `"msg"/"POS"/"altFt" → "message"/"POSITION"/"altitudeFeet"` across the rule + all three tests. Swept for stragglers — clean.)*

**Compile checkpoint** (this touched Aerial's existing tests, so worth running both fully): `sbt "testOnly domains.aerial.* domains.terrestrial.*"`. Once green, we have the two divergent media in place and the next step is Phase 2 — the `World` super-domain and the first `Aerial ⇒ Terrestrial` transform, which is TransformBuilder's actual subject.

---

**[Dev]** You are doing so well at helping to define these domains, I think I'd like to go for Marine and Ethereal, before TransformBuilder.

**[Claude]** Thank you — and yes, let's complete the set. Before I generate ~40 more files, let me lock the **vocabulary** for both, since that's the part worth getting right up front (and it saves us another rename pass like Terrestrial's).

Applying the principles we've settled — no abbreviations, no medium-redundant qualifiers, neutral perspective-free tags, and a genuine *semantic* divergence in the "where" concept, not just units:

| Aspect | Aerial ✓ | Terrestrial ✓ | **Marine** | **Ethereal** |
|---|---|---|---|---|
| medium | air | land surface | sea | space |
| report type | `PositionReport` | `LocationReport` | `FixReport` | `EphemerisReport` |
| tag value | `POSITION` | `LOCATION` | `FIX` | `EPHEMERIS` |
| "where" stance | point in motion | weakly fixed | a determined sample | computed / predicted |
| discriminator key | `message` | `type` | `kind` | `category` |
| identity key | `callsign` | `unit` | `vessel` | `object` |
| vertical field (unit) | `altitudeFeet` (ft) | `elevationMetres` (m) | `depthFathoms` (fathoms) | `altitudeKilometres` (km) |
| intent type | `FlightIntent` | `MarchIntent` | `VoyageIntent` | `LaunchIntent` |
| origination | flightLevel→ft (×100) | elevationFeet→m (×0.3048) | depthMetres→fathoms (÷1.8288) | altitudeNauticalMiles→km (×1.852) |

The thing I like most: the four report concepts are four genuinely distinct *epistemic stances on "where"* — **position** (a point along a path), **location** (a fixed place), **fix** (a determination you take), **ephemeris** (a position you compute from orbital mechanics). Each medium also gets a signature unit, so every cross-medium transform is non-trivial on both axes.

Two genuine choices I'd flag before committing: **Ethereal report = `EphemerisReport`** is my pick (most evocative for an orbiting body), but `VectorReport` is the cleaner one-word alternative; and **Ethereal identity key = `object`** (a tracked space object), with `designator` the alternative if you'd rather it read as a catalogue ID. Confirm or adjust?

---

**[Dev]** Confirm

**[Claude]** Confirmed — building both.

*(Built Marine and Ethereal as full Phase-1 peers — 20 files each: domain + intent/report shells + Creator→Provider→Consumer + sink + 3 rules + chain test. Marine origination `depthMetres → depthFathoms` (183 m → 100 fathoms); Ethereal `altitudeNauticalMiles → altitudeKilometres` (100 nm → 185 km). All JSON validated, inventory symmetric across the four media.)*

All four media are built and symmetric. The four media now span four genuinely distinct epistemic stances on *"where"*, each with its own representation:

| medium | report (tag) | stance | discriminator / id | vertical | origination |
|---|---|---|---|---|---|
| aerial | `PositionReport` (`POSITION`) | point in motion | `message` / `callsign` | `altitudeFeet` | FL390 → 39000 ft |
| terrestrial | `LocationReport` (`LOCATION`) | weakly fixed | `type` / `unit` | `elevationMetres` | 900 ft → 274 m |
| marine | `FixReport` (`FIX`) | a determination taken | `kind` / `vessel` | `depthFathoms` | 183 m → 100 fathoms |
| ethereal | `EphemerisReport` (`EPHEMERIS`) | computed / predicted | `category` / `object` | `altitudeKilometres` | 100 nm → 185 km |

Every cross-medium pair diverges on concept, keys, *and* units — so any transform is non-trivial on multiple axes.

**Compile checkpoint** (all four): `sbt "testOnly domains.aerial.* domains.terrestrial.* domains.marine.* domains.ethereal.*"`. Marine and Ethereal mirror the proven shape, so I expect green. Once it passes, **all of TransformBuilder's preconditions exist**.

*(Delegated action — Dev ran the four-media `testOnly`: **13/13 pass**, all four chain tests green, and `AerialGenTest` re-generated the Aerial `Consumer.scala`/`ConsumeReportRule.scala` idempotently, confirming the Generator still reproduces the actor after the un-abbreviation. Wrote this journal chapter.)*

---

## Status

Four message domains — Aerial, Terrestrial, Marine, Ethereal — each a complete Phase-1 isolated pipeline (Creator→Provider→Consumer over the Environment seam, with algorithmic origination), **13/13 green**. All of TransformBuilder's preconditions now exist: four genuinely divergent native representations. Next is Phase 2 — the `World` super-domain and the first cross-medium transform (e.g. `Aerial ⇒ Terrestrial`), TransformBuilder's actual subject. And per Part I of this session, that transform work is itself a rehearsal for `Generator[L]`: the same rule-and-actor machinery, one pipeline carrying positions, the other carrying TypeDefinitions becoming source. North star recorded (`project_self_port_capability_domains`): `src/main` ⟺ definition-backed; the necessary non-definitive dependency bindings (Evrete/Pekko/Circe) live in `src/mods`.
