# Draco Dev Journal — Chapter 45

**Session date:** June 16–17, 2026
**Topic:** Phase 2 opens. A question about generated sources surfaced the real architecture of the transformation service — the `Transformer` is not a per-medium actor but the **`World`** domain — which forced a strip of the just-renamed media `Transformer`, then a sequence of clarifications (SourceSink as an integration-layer stand-in; messages as composition trees; the general-vs-message-domain distinction) that culminated in laying the `World` foundation: the four media now derive `World` as subdomains, with message-domain-ness carried implicitly by `Domain[T]`. Green (10 tests). Transcript.

---

**[Dev]** So when will generated scala sources appear in src/mods/scala?

**[Claude]** Honest answer: **some already do** — and the rest are waiting on a deliberate step we've twice deferred.

**Already generated, today:** `AerialGenTest` regenerates `Consumer.scala` and `ConsumeReportRule.scala` into `src/mods/scala/domains/aerial/` on every run — you saw it in the last two outputs. Those two files *are* Generator output; the test overwrites the hand-written versions idempotently. So generated Scala is already landing in `src/mods/scala`, for the shapes the Generator knows.

**Everything else is hand-written**, by the deliberate discovery rhythm (hand-template to nail the shape → teach the Generator → regenerate, exactly how 6a→6b went). The blocker for the rest is that three emission shapes don't exist in `draco.Generator` yet — the ones we invented by hand across 6c and the four-media build:

1. the factory `actorType(downstreamRef)` + the `session.set("<role>", ref)` seed line (`Transformer`, `Creator`)
2. the rule RHS `ctx.getRuntime().get[ActorRef[…]]("<role>") ! …` (`RelayReport`, `OriginateReport`)
3. the `originate(…)` helper body (origination)

(The domain companions and the shells *could* already be generated — the Generator handles those shapes — they're just not wired to a GenTest here.)

**So "when":** when we fold those three shapes into `draco.Generator` and add a GenTest per medium that writes the Scala. There's no automatic trigger — it's the "fold into the Generator" task, gated by two small schema questions: how a JSON declares an actor's downstream role→type, and how a rule expresses the `originate` helper.

My recommendation: **now is a good time to do it.** Four media is a solid corpus, and the shapes are stable post-rename. I'd start by settling the two schema bits and taking *one* medium (Aerial) fully generated end-to-end, then replicate to the other three. Want me to start the Generator fold, or keep it hand-written until Phase 2?

---

**[Dev]** It turns out that Format domains don't reference Transformer actors, all the rules that execute transforms as actions are in the World domain which is not a Format domain. The World domain has two actors, a consumer for transform input and a provider for transform output. The World consumer actor receives the World message type, and the World provider actor sends the transformed data to the proper subdomain consumer actor.

**[Claude]** That relocates the Transformer entirely — and it actually *completes* the Eagle mapping rather than breaking it. Let me play it back:

- Each **Format (media) domain is a `SourceSink`** — `Creator` + `Consumer`, Brahma + Shiva, the Eagle. No Transformer inside it.
- **`World` is the Transformer** (Vishnu / the Gift). Not a Format domain. It *holds* the transform rules, and its two actors are the membrane: `World.Consumer` (transform **input** — receives the World message type) and `World.Provider` (transform **output** — sends to the proper subdomain's `Consumer`).

So "Transformer" was never a per-medium actor — it's the `World` domain. The per-medium `Transformer` we just renamed is Phase-1 scaffolding that doesn't belong in the real model; the Format domains collapse to `Creator` + `Consumer`.

And it lines up exactly with your earlier conversion taxonomy. Because `World` is *not* a Format domain, its message type is the **semantic invariant** — a strong `Domain[World]` value, the world-fact itself, not a `Format[Json]` shell. So the flow reads:

```
media msg (Format[Json])  →  World.Consumer:  Format[Json] ⇒ Domain[World]   (decode to meaning)
                          →  World rules:      transform on the World form     (meaning preserved)
                          →  World.Provider:   Domain[World] ⇒ Format[Json]    (re-encode for target)
                          →  target media.Consumer
```

Two things I need from you before building `World`:

1. **The media `Transformer`** — strip it (Format domains → `Creator` + `Consumer`), or leave it as dead Phase-1 scaffold until `World` lands?
2. **The media→World direction** — who sends a source message into `World.Consumer`? The source medium's `Creator` directly, or a media-side forwarder?

Have I got the shape right?

---

**[Dev]** 1. Strip it.
2. The actual subdomain actor of World that receives format data and converts it to typed data, which is an indirect subtype of World and and a direct subtype of the subdomain.

**[Claude]** Both clear. Let me reflect #2 back to make sure I have it, then execute the strip.

**On #2:** the format→typed conversion lives in the *subdomain*, not in World. Each media domain is a **subdomain of World** (`Aerial <: World`), and it owns a typed message form that is a *direct* subtype of the subdomain and therefore an *indirect* subtype of World. The subdomain actor decodes its loose `Format[Json]` into that typed value (`Format[Json] ⇒ Domain[subdomain]`), and *that* typed value — already a World subtype by the hierarchy — is what `World.Consumer` receives. So there's no single monolithic "World message": the World message type is the supertype, and the real messages are subdomain-typed World-subtypes. The decode is the subdomain's job; World transforms typed→typed and dispatches via `World.Provider` to the target subdomain's consumer. That tells me the media domains will need to *derive World* and grow typed message forms — but that's the World build, next.

*(Executed #1 — stripped the per-medium `Transformer` and its `RelayReport` identity rule across all four media; rewrote the four `Creator.scala` (downstream is now the `Consumer`, env role key `transformer → consumer`) and the four chain tests, each now a single `Creator → Consumer` origination test. Final sweep: no leftovers, 8 Scala files per medium (down from 10), JSON valid. Corrected the architecture in memory — this supersedes the per-medium-Transformer framing.)*

**The corrected model, recorded:**
- Each Format (media) domain is a **`SourceSink`** (`Creator` + `Consumer`) and a **subdomain of `World`**. No `Transformer` inside it.
- **`World` is the Transformer** — not a Format domain, holds the transform rules, with two actors: `Consumer` (transform input) and `Provider` (transform output → target subdomain's `Consumer`).
- The "World message type" isn't monolithic: each subdomain decodes its `Format[Json]` into a typed value that's a direct subtype of the subdomain and an indirect subtype of `World`.

**Compile checkpoint:** `sbt "testOnly domains.aerial.* domains.terrestrial.* domains.marine.* domains.ethereal.*"` (now 4 origination tests + the Aerial sub-step tests; the relay tests are gone).

**The one open question for building `World`** (from #2): which subdomain actor does the `Format[Json] ⇒ Domain[subdomain]` decode and feeds `World.Consumer` — is it the `Consumer` (its "consume" = decode-and-hand-to-World, with the Phase-1 `Sink` having been the stand-in for that destination), or a distinct actor?

---

**[Dev]** The SourceSink is a test stand-in for an abstract meta-adapter (alternatively, integration layer) that converts between manifold external disjoint message formats and message domains and a single format that can distinguish between them in both input and output directions. The subdomain actors that ingest the uniform format and send typed data to be transformed are the input side of the transformation service and the subdomain actors that ingest the transformed typed data and send the uniform format data back to SourceSink are the output side of the transformation service.

**[Claude]** This lands the whole architecture as a clean two-layer stack, with the *uniform format* as the seam between them. Two stacked conversions, and **neither layer sees the other's concerns**:

- **SourceSink** (the integration layer, only ever a test stand-in) absorbs the *format* diversity: manifold external formats ⇄ one uniform format that can still tell the subdomains apart. The transformation service never sees the external zoo.
- **World** (the transformation service) absorbs the *semantic* work: uniform format ⇒ typed (input side, per-subdomain) → transform typed→typed preserving meaning → typed ⇒ uniform format (output side, per-subdomain) → handed back to SourceSink.

And the payoff insight: in the example, the four media's loose JSON **already is the uniform format** — a single `Format[Json]` whose discriminator (`message:"POSITION"` vs `type:"LOCATION"` vs `kind:"FIX"` vs `category:"EPHEMERIS"`) is exactly "a single format that can distinguish between them." So the current `Creator`/`Consumer` are the **SourceSink stand-in** (inject uniform JSON / receive it back), and the input/output subdomain actors + the typed forms + the transform rules are what `World` adds — none of which exist yet.

Mapping to your conversion taxonomy, the service is the two value-level rows back-to-back: `Format[Json] ⇒ Domain[sub<:World]` (input), `Domain ⇒ Domain` (transform), `Domain[sub<:World] ⇒ Format[Json]` (output).

Two checks before I build:
1. **Placement** — the input actor (`Format ⇒ typed`) and output actor (`typed ⇒ Format`) live on the **World / service** side, one pair per subdomain; the current `Creator`/`Consumer` stay as the SourceSink stand-in. Right?
2. **First slice** — rather than build all four at once, take **one transform pair end-to-end** (say `Aerial ⇒ Terrestrial`): give Aerial + Terrestrial typed forms (deriving `World`), build their input/output actors, one `World` transform rule, and a test asserting an injected Aerial `POSITION` comes back out as a Terrestrial `LOCATION` with meaning intact. Good as the Phase-2 opening?

---

**[Dev]** Messages, whether type or format, are represented by a composition tree of primal and composite named types and format data associated with the names referencing compositional values. Those names are themselves named as fields, elements, properties disjunctively, and bind specific names to the composite and primal values in the tree shaped message instance. The message type name represents the entire set of possible values for any specific instance. A general domain does not require the contained types be subtypes of the domain type. A message domain does require the contained types be subtypes of the domain type.

**[Claude]** This is the data-model floor under everything, and it maps cleanly onto draco's existing self-description — let me check the correspondence:

| your formalism | draco realization |
|---|---|
| composition tree of primal + composite named types | a `TypeDefinition` (typeName + elements), recursively — composite elements reference other `TypeDefinition`s |
| primal vs composite | `Primal[T]` (single value) vs a type carrying elements |
| names = **fields / elements / properties** (disjunctively) | `TypeElement` (`Fixed`/`Mutable`/`Dynamic`/…) — one concept, three vocabularies |
| format data associated with the names, referencing values | the loose `Format[Json]` instance + `Value` path-extraction over those names |
| type name = the whole set of possible instances | type-as-set: the `TypeDefinition` denotes its instance set |

So "message, whether type or format" = the *same composition tree*; the **format** form binds loose format-data at the names, the **typed** form binds typed primal/composite values at the same names. That sharpens the conversions: **format ⇄ typed within one medium** (the input/output adapters) is *tree-preserving* — same names, leaves go loose↔typed; **typed ⇒ typed across media** (the World rules) is *tree-changing but meaning-preserving* — Aerial's tree (`callsign`/`altitudeFeet`) becomes Terrestrial's (`unit`/`elevationMetres`); the tree (representation) changes, the denoted world-fact (meaning) is held.

And the **general vs message domain** distinction is the load-bearing one — it's the `MessageDomain` concept that Orion deferred, now made concrete:

- **General domain** (`Draco`, `Base`, `Primes`) — a *dictionary* of types; no requirement that the contained types subtype the domain.
- **Message domain** (`World`, and each medium) — *requires* contained types be subtypes of the domain type. This is exactly what makes a typed Aerial message a *direct* subtype of `Aerial` and, since `Aerial` (a message domain) is contained in `World` (a message domain), an *indirect* subtype of `World`. It formalizes last turn's subtype-chain claim: the containment chains *because* both are message domains.

One modeling question that decides how I build `World`: should "message domain" be an explicit construct — a `MessageDomain` (a `DomainType` variant that *declares/enforces* contained ⊂ domain) — or stay structural, i.e. "a message domain is just a domain all of whose contained types happen to derive the domain type," with no new type, only the derivation discipline? Which way do you want it?

---

**[Dev]** I think eventually (more sooner than later) we can make Message[Domain] explicit, but for now let's create it implicitly with Domain[T]

**[Claude]** Recorded. Concretely that means: **no new type yet** — `World` and the media stay plain `Domain[T]`, and "message domain" is realized purely as a *discipline*: every contained type derives the domain. That discipline alone gives you the subtype chain (`shell ⊂ medium ⊂ World`); the explicit `Message[Domain]` later just *names and enforces* what the derivation already does. (Noted your spelling — `Message[Domain]`, parameterized — for when we make it real.)

The structural picture, with precedent: `World` becomes the super-domain the four media derive — exactly the `Cosmocentric ← Egocentric/Geocentric/…` pattern from the reference frames (peers related by *derivation*, not package nesting; sub-package nesting is reserved for transforms). So `Aerial` (a `Domain`, and a message domain by discipline) derives `World` (a `Domain`, message domain by discipline), and a typed `PositionReport ⊂ Aerial ⊂ World` falls out for free.

I'd open the `World` build with the smallest safe slice — the hierarchy only:

1. `domains.world.World` — a bare `Domain[World]` (not a Format domain).
2. The four media derive `World` — Scala `trait Aerial extends World` (+ import) and JSON `dracoAspect.derivation` (Scala-first, per the inheritance rule), additive to the green Format-side tests.
3. A tiny hierarchy test: an `Aerial` `PositionReport` is, transitively, a `World`.

Lay that foundation now, or is there more of `World` (its `elementTypeNames`, the typed-form shape) you want to pin down before I touch files?

---

**[Dev]** Proceed with the foundation.

**[Claude]** *(Built it: `World.json` + `World.scala` — a bare `Domain[World]`, not a Format domain; `trait <Medium> extends World` in all four media (Scala-first) plus `dracoAspect.derivation = [World]` in each domain JSON; `WorldHierarchyTest`. JSON validated.)*

The `World` foundation is laid:

- **`domains.world.World`** — a bare `Domain[World]`, not a Format domain.
- **All four media derive `World`** — `trait Aerial extends World` (Scala-first), plus `dracoAspect.derivation = [World]` in each domain JSON. Follows the `Cosmocentric ← Egocentric` super-domain precedent (peers by derivation, not package nesting).
- **Subtype chain `shell ⊂ medium ⊂ World`** now holds, carried purely by the `Domain[T]` derivation discipline — message-domain-ness implicit, exactly as you decided.
- `WorldHierarchyTest` asserts each medium's shell is (transitively) a `World`, while still its own medium and a `Json` shell.

All additive to the green Format-side tests; no `build.sbt` change needed. **Compile checkpoint:** `sbt "testOnly domains.world.* domains.aerial.* domains.terrestrial.* domains.marine.* domains.ethereal.*"` — adds `WorldHierarchyTest` and re-runs the four origination chains (now `Creator → Consumer`).

*(Delegated action — Dev ran the `testOnly`: **10/10 pass**, including `WorldHierarchyTest` — "each medium derives World, and its message shells are indirect World subtypes" — and all four origination chains in their simplified `Creator → Consumer` form. The hierarchy is additive; nothing in the Format-side tests broke. Wrote this journal chapter.)*

**The next slice needs one decision from you — the typed-form shape.** Everything downstream (the `Format ⇒ typed` input adapter, the `typed ⇒ Format` output adapter, and the transform rules) hangs off what a *typed* message looks like as a composition tree. Concretely, for a typed Aerial position: a composite type (say `domains.aerial.Position`, a direct subtype of `Aerial`, hence `World`) whose elements are the named fields — `callsign` (a primal `String`), `altitudeFeet` (a primal `Int`) — the loose JSON's tree, now with typed leaves and a strong type name. If that's the shape you have in mind, I'll open the `Aerial ⇒ Terrestrial` vertical; or if the typed form should carry more (units as types, a shared `World` coordinate, etc.), tell me and I'll shape it that way first.

---

## Status

`World` exists as the super-domain; all four media derive it; the message-domain discipline (`shell ⊂ medium ⊂ World`) is in place implicitly via `Domain[T]`. **10/10 tests**, additive — the Format-side tests stayed green. What remains for the transformation service — and awaits the typed-form spec — is the substance: the **typed message forms** (composition trees, `Domain[sub<:World]`), the per-subdomain **input** (`Format ⇒ typed`) and **output** (`typed ⇒ Format`) adapters, and the meaning-preserving **transform rules**. The natural opening is a single `Aerial ⇒ Terrestrial` vertical: inject an Aerial `POSITION`, watch it come back out as a Terrestrial `LOCATION` with the world-fact intact — the moment "preserves meaning" becomes a passing assertion.
