# Draco Dev Journal — Chapter 43

**Session date:** June 15, 2026
**Topic:** Two arcs in one session. First a design dialogue that reframed what the actor/rule machinery is *for* — generation itself, in a language-parameterized `Generator[L]` — and pinned the litmus that keeps draco portable (`src/main` definitive, `src/mods` non-definitive). Then, back to the concrete: completing the message-domain fixture from one medium to four — Aerial, Terrestrial, Marine, Ethereal — with the vocabulary carefully chosen so every cross-medium transform is non-trivial. All four Phase-1 pipelines green (13 tests).

---

## Part I — what is the actor/rule machinery *for*?

Chapter 42 closed the 6c blocker: a rule's RHS can now reach a Pekko `ActorRef` via the Evrete Environment seam. With that proven, the Dev turned to the telos, in a sequence of terse statements.

> **Dev:** The Format[T] transforms are type-primitive based whether it's within the format domain (e.g. Json => Json) or between different format domains (e.g. Json => Xml).

This pinned the type vocabulary for actor channels: a message channel's type is drawn from `{ Format[primitive], Domain[T <: DomainType] }`, and Format transforms move the `T` of `Format[T]`. Then the taxonomy sharpened:

> **Dev:** Format[Json] => Format[Xml] assumes format conversion for a specific message-type-value. Format[Json] => Format[Draco] assumes conversion of type definition generating a draco type, Format[Json] => Domain[T] assumes conversion from Json into a subtype-value of type T.

Three conversions, sorting into two kinds: two are *runtime, value-level* (`Format[Xml]` re-serialises a message; `Domain[T]` decodes loose JSON into a strong domain value) and one is *build-time, meta-level* — `Format[Draco]`, **which is the Generator itself** (a TypeDefinition JSON becoming a draco type). That reconciled the earlier "two channel types": a consumer `ActorRef`'s message type ranges only over the runtime outputs, never `Format[Draco]`.

Then the keystone:

> **Dev:** When Generator becomes a programming language parameterized super-domain, then actors and rules will do the work.

So the Aerial pipeline was never just TransformBuilder's fixture — it was the dress rehearsal for how `Generator[L]` does its *own* work. Generation is the conversion `Format[Json] => Format[Draco] => Format[L]`; today `draco.Generator` is the imperative special-case with L hard-wired to Scala. In `Generator[L]`, each emission decision becomes a rule and actors pipeline them across the Environment seam — the very machinery 6c validated.

### The acid test, and a correction

> **Dev:** If the language parameterization is going to be useful, then everything in draco has to be generated from definitions, including Evrete, Pekko and Circe, Otherwise DRACO cannot self-port to another language like Haskell.

Self-port to Haskell is the validating criterion. The heaviest couplings are the three external services, which appear as literal imports inside generated code — none of which survive a port. The fix: definitions name *capability* types (`draco.rete.Session`, `draco.actor.Ref`, `draco.format.json.Json`), and `Generator[L]` resolves each to a concrete library for L. `draco.format.Format[T]` already does this for Circe; `draco.rete` and `draco.actor` are the missing peers.

I initially over-stated this as "drive the external couplings to zero," and the Dev corrected the model:

> **Dev:** Having non-definitive implementations (that's what the dependencies are) are absolutely necessary, which is one reason why src/mods exists. The criteria having a definition is if it's going into src/main, which is what will happen to Generator once it becomes a domain.

The litmus, stated cleanly: **`src/main` ⟺ definition-backed. Non-definitive code — the dependency bindings — lives in `src/mods`, and is necessary, not a defect.** You don't eliminate the Evrete coupling; you *localise* it to a swappable mods layer. The pattern is already realised for serialization: `Format[T]` is in `src/main` (definitive), `draco.format.json.Json` (binding `io.circe.Json`) in `src/mods`. The definition/binding split *is* the main/mods split. Self-porting = regenerate `src/main` for L + write L's mods bindings; `Generator` "becoming a domain" is the moment it crosses mods→main and must be definition-backed.

The practical consequence: the Aerial actors touching `org.evrete`/`org.apache.pekko`/`io.circe` *in src/mods* are correctly placed, not violations — and hand-extending the imperative Generator is legitimate work on a non-definitive layer, not a stand-in to avoid. Recorded as the north-star memory `project_self_port_capability_domains`.

## Part II — completing the four-media fixture

> **Dev:** Back to the example domains for supporting TransformBuilder. Where are we?

Status: one medium (Aerial), Phase-1 complete. TransformBuilder needs *two* divergent media before a cross-medium transform is even expressible. So: build the second.

### Naming, by dialogue

The second medium's name took a careful path, and the care paid off in the design.

> **Dev:** Terrain in Holarchy is more general than in the example domain. In The example domain is just "solid ground" or "land surface". In Holarchy, it is limited region that supports a distinct ecology and climate.

A name collision: the example media were *named after* holarchy levels, but the senses diverge (a physical transport medium ⟂ a holarchic scale). Rather than overload `Terrain`:

> **Dev:** Is there anything linguistically comparable to Aerial and Marine to replace Terrain?

`Terrestrial` — the canonical sibling in the *aerial / marine / terrestrial* habitat triad, and free of the holarchy-`Terrain` clash.

> **Dev:** That occurred to me as well, but you justified it better. Let's go with that.

Then two refinements that improved the model, not just the spelling:

> **Dev:** The term "track" indicates a hunter perspective. What would term would you suggest for a neutral perspective?

"Track" smuggled in a pursuer's frame (as would "contact" or "sighting"). The neutral framing is the bare fact of *where* something is. This opened a vein: each medium could take a *different* neutral word for "where," each a distinct epistemic stance.

> **Dev:** For these examples abbreviations seem unnecessary. Also "ground" seems to be the same as "terrestrial" so I'm not sure why both are needed.

Both right. Abbreviations spelled out everywhere (`POS`→`POSITION`, `altFt`→`altitudeFeet`, …). And "ground" duplicates "terrestrial," so the report type became `LocationReport`, not `GroundReport` — which made the two media properly symmetric, with the divergence living entirely in the *representation*.

> **Dev:** I do like "location" for terrestrial because it weakly implies fixed.

That sealed the theme: *position* (a point in motion) for Aerial, *location* (weakly fixed) for Terrestrial — a genuine semantic split, not just unit relabelling.

### Marine and Ethereal

> **Dev:** You are doing so well at helping to define these domains, I think I'd like to go for Marine and Ethereal, before TransformBuilder.

Having learned from the Terrestrial rename, the vocabulary was settled in a table *before* generating any files — and the Dev confirmed it. The payoff is four distinct epistemic stances on "where," each with a signature unit:

| medium | report (tag) | stance on "where" | discriminator / id | vertical | origination |
|---|---|---|---|---|---|
| aerial | `PositionReport` (`POSITION`) | a point in motion | `message` / `callsign` | `altitudeFeet` | FL390 → 39000 ft |
| terrestrial | `LocationReport` (`LOCATION`) | weakly fixed | `type` / `unit` | `elevationMetres` | 900 ft → 274 m |
| marine | `FixReport` (`FIX`) | a determination one takes | `kind` / `vessel` | `depthFathoms` | 183 m → 100 fathoms |
| ethereal | `EphemerisReport` (`EPHEMERIS`) | computed from orbital mechanics | `category` / `object` | `altitudeKilometres` | 100 nm → 185 km |

Every cross-medium pair diverges on concept, keys, *and* units, so no transform is a trivial relabel. Marine and Ethereal were generated as full Phase-1 peers (Creator→Provider→Consumer + shells + sink + chain test each), mirroring the proven shape.

> **Dev:** [`testOnly domains.aerial.* domains.terrestrial.* domains.marine.* domains.ethereal.*`]
> **Tests: succeeded 13, failed 0** — all four media green; `AerialGenTest` re-generated the Aerial Consumer idempotently after the un-abbreviation.

## Where this leaves things

All of TransformBuilder's preconditions now exist: four message domains with genuinely divergent native representations, each with a working isolated (creation-phase) pipeline. The next step is finally Phase 2 — the `World` super-domain and the first cross-medium transform (e.g. `Aerial ⇒ Terrestrial`), which is TransformBuilder's actual subject. And per Part I, that transform work is itself a rehearsal for `Generator[L]`: the same rule-and-actor machinery, one pipeline carrying positions, the other carrying TypeDefinitions becoming source.
