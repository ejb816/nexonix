# Draco Dev Journal — Chapter 46

**Session date:** June 17, 2026
**Topic:** `World`'s first transform. A long, precise spec dialogue settled the canonical coordinate frame (dual Geocentric/Axial + Heliocentric/Ecliptic Cartesian, ECEF), the named-vs-positional coordinate representation (both, bridged by the constructor), and the world-fact's name (`Observable`). Then the semantic core landed: an Aerial `Position` crosses to a Terrestrial `Location` through the `Observable`, with WGS84 geodesy, and **"preserves meaning" became a passing assertion.** Session closed with the commit record covering chapters 42–46 and a from-scratch `GETTING_STARTED.md`. Transcript.

---

**[Dev]** Are callsign and altitudeFeet all fields or just an example subset?

**[Claude]** An example subset — and worth being precise, because one of the current fields is special.

Each medium's loose payload right now has exactly **three** fields: a discriminator, an id, and a vertical (`message:"POSITION"` / `callsign` / `altitudeFeet`; `type:"LOCATION"` / `unit` / `elevationMetres`; `kind:"FIX"` / `vessel` / `depthFathoms`; `category:"EPHEMERIS"` / `object` / `altitudeKilometres`).

When I wrote `callsign` + `altitudeFeet` I gave the two *data* fields and silently dropped the third — the **discriminator** — on purpose: it's not data, it's the subdomain selector. Once the input adapter has decoded the loose JSON into a typed `Position` (a strong subtype of `Aerial`), the *type itself* carries what `message:"POSITION"` was asserting, so the discriminator doesn't reappear as a typed field. So a faithful typed `Position` would be just `callsign` + `altitudeFeet`.

So in the current toy, yes — `callsign` + `altitudeFeet` are all the (data) fields. But the toy is deliberately thin. That's the thing to settle before I build: **do you want the typed forms to mirror each medium's current loose payload (id + vertical, dropping the discriminator), or to carry a richer field set** — a real position with, say, latitude/longitude, time, heading/speed — so the `Aerial ⇒ Terrestrial` transform has more than one quantity to preserve meaning across?

---

**[Dev]** So how will the horizontal plane coordinate names be determined?

**[Claude]** The horizontal plane is where naming stops being free and becomes the actual transform problem.

The vertical was a gift: **one axis**, and the media diverge only by *unit* (feet / metres / fathoms / km) — a scalar conversion, so the names could be picked freely. The horizontal plane is **two axes**, and the media diverge by *coordinate system*, not just unit. So the names aren't picked — they're **determined by the system** each medium natively uses:

| medium | likely system | coordinate names |
|---|---|---|
| Aerial | geodetic | `latitude` / `longitude` (degrees) |
| Terrestrial | projected grid | `easting` / `northing` (m), or one `gridReference` |
| Marine | geodetic / relative | `latitude` / `longitude`, or `bearing` / `range` |
| Ethereal | orbital / celestial | `rightAscension` / `declination`, or ECI `x`/`y`/`z` |

In draco terms each is a `base.Coordinate[T <: Product]` composite — and since `Coordinate` is *compositionally self-describing* (no named Cartesian/Polar/Spherical), **the coordinate names *are* the composite's element names**. So "how determined" = choose the medium's coordinate system; its element names fall out of the `Coordinate` composition.

And here's the consequence the vertical hid: meaning-preservation now **requires a shared canonical position** — the World fact — that every system projects to and from. `latitude/longitude ⇒ easting/northing` isn't a field rename, it's a *projection*, and that math lives against a reference frame. This is exactly where the retired Centric frames return as `World`'s geometric substrate.

So two determinations are yours, and they fix everything downstream: **the coordinate system per medium** (which fixes the horizontal field names), and **`World`'s canonical horizontal form** (the invariant the projections preserve through). Which systems do you want per medium, and what's `World`'s canonical?

---

**[Dev]** The World needs to support two frames simultaneously: Geocentric/Axial and Heliocentric/Ecliptic

**[Claude]** That pins `World`'s invariant: not one canonical frame but **two, materialized simultaneously** —

- **Geocentric / Axial** — origin at Earth's center, reference plane the equatorial plane (aligned to Earth's spin axis).
- **Heliocentric / Ecliptic** — origin at the Sun, reference plane the ecliptic (Earth's orbital plane).

And the relationship between them is the heart of the transform. The two frames differ by a **rotation** — the obliquity of the ecliptic, ε ≈ 23.44°, about the equinox line, constant geometry — and a **translation** — the Earth↔Sun vector, *time-dependent* (ephemeris).

So "support both simultaneously" means `World` materializes the world-fact in both and keeps them consistent through that rotation + translation. The terrestrial media (Aerial / Terrestrial / Marine) live naturally in **Geocentric/Axial**; Ethereal lives in **Heliocentric/Ecliptic** — so a terrestrial ⇄ ethereal transform *is* this frame conversion. That's the Centric substrate returning, exactly as planned.

My recommendation on representation: **Cartesian `x`/`y`/`z` per frame** for `World`'s canonical. Rotation becomes a matrix multiply and translation a vector add; the media keep their *native* spherical/grid/orbital names, and the input adapter converts native → Cartesian-in-frame.

Two things decide whether the first vertical is real or stubbed: (1) media ⇄ frame mapping — terrestrial three → Geocentric/Axial, Ethereal → Heliocentric/Ecliptic, confirm? (2) **The translation needs time** — the obliquity rotation is constant, but Earth↔Sun requires an epoch. For the opening `Aerial ⇒ Terrestrial` vertical (both Geocentric/Axial), no translation is involved — pure same-frame, so it sidesteps this. Do you want to **start with a same-frame transform**, and bring the Heliocentric/Ecliptic translation + a time field in only when we first cross to Ethereal?

---

**[Dev]** World canonical = 3D Cartesian, Polar axis = z-axis, Geocenter = origin?

**[Claude]** Yes — that's the geocentric equatorial Cartesian frame, and it's the right canonical for Geocentric/Axial: origin = geocenter, z-axis = polar (Earth's rotation axis), so the x–y plane is the equatorial plane (and the Heliocentric/Ecliptic twin follows the same convention: origin = heliocenter, z = ecliptic pole).

One degree of freedom is still open, though: **origin + z fix only two axes.** A full 3D Cartesian frame needs the **x-axis direction** in the equatorial plane (z + x + right-handedness ⇒ y). That choice is exactly the Earth-fixed vs inertial fork:

- **x → prime meridian (0° longitude)** ⇒ **Earth-fixed (ECEF)**, rotates with Earth. `latitude/longitude/altitude → x/y/z` is a *static* conversion — no time needed.
- **x → vernal equinox** ⇒ **inertial (ECI)**, fixed in space. Natural for orbits, but `ECEF ↔ ECI` needs time (sidereal rotation).

My recommendation: make the Geocentric/Axial canonical **Earth-fixed, x → prime meridian (ECEF)**. Then the three terrestrial media's `lat/lon/alt → x/y/z` is pure geometry, and the opening `Aerial ⇒ Terrestrial` vertical needs **no time at all**. Time enters only when we cross to Heliocentric/Ecliptic (Ethereal). So: confirm **x → prime meridian / Earth-fixed** for the geocentric canonical, and that the heliocentric canonical is its Sun-origin, ecliptic-pole twin?

---

**[Dev]** Confirmed

**[Claude]** Confirmed and locked: `World`'s canonical is a **dual-frame 3D Cartesian** — Geocentric/Axial (geocenter origin, z = polar, x = prime meridian / ECEF) and Heliocentric/Ecliptic (Sun origin, z = ecliptic pole), held simultaneously, with names `x`/`y`/`z` per frame.

*(Checked how draco already models a coordinate before building the typed forms.)* It surfaces one representation fork worth settling: `base.Coordinate[T <: Product]` holds a **positional tuple** value — and the reference frames likewise use `Holon[tuple]`. So today a Cartesian point would be `Coordinate[(Double, Double, Double)]`, with x/y/z as *positions 1/2/3*, not named fields. But your own data-model statement — "messages are composition trees of **named** fields/elements/properties" — points the other way: toward `x`/`y`/`z` as real named elements of the tree. Those two conventions disagree, and the choice propagates through every typed form.

My recommendation: **named-field composite.** A `Cartesian` type with primal elements `x`, `y`, `z` (metres), so the names are first-class — consistent with messages-as-named-trees, and the ordinary shape for draco message types anyway. `World`'s canonical is then two named `Cartesian` frames held together (geocentric + heliocentric = the dual-frame world-fact). The positional `Coordinate[(x,y,z)]` is the alternative if you'd rather stay with the existing tuple convention. The named-vs-positional decision isn't quick to adjust later, which is why I'm asking it now.

---

**[Dev]** The type value constructors can automatically map position coordinate instance values into the name-field values.

**[Claude]** That resolves the fork cleanly — the two conventions aren't a choice, they compose: a **named-field** type whose **Factory/constructor maps a positional coordinate's values into the named fields** (`(_1, _2, _3) → x, y, z`). Named tree on the surface (per your message-as-named-tree model), positional `Coordinate[T <: Product]` underneath, the `apply` bridging them. That's draco's `Factory` element doing position→name binding.

*(Grounded the exact composite-with-factory pattern on an existing named-field composite — `elements` (named `Fixed` fields) + a `factory` (positional `Parameter`s with defaults) — and its generated Scala, then built the keystone: `domains.world.Cartesian` — named `x`/`y`/`z` (metres) with `Cartesian(x, y, z)` and `Cartesian((x, y, z))` (positional → named), mirroring the `RuleAspect` composite shape, plus `WorldCanonicalTest` checking both constructors. JSON valid.)*

Worth a compile checkpoint on just this keystone before I build the rest on its pattern: `sbt "testOnly domains.world.*"`. If `Cartesian` compiles, the world-fact and the typed forms all follow the same shape.

*(Delegated action — Dev ran it: **2/2 pass** — "Cartesian exposes named x/y/z, and its factory maps a positional coordinate value into the names," plus the hierarchy test. Keystone validated.)*

**The dual-frame world-fact** is the next type: a `World` member holding **`geocentric: Cartesian`** + **`heliocentric: Cartesian`** — the canonical truth in both frames at once. It needs a name, and naming the central World message type is yours to make — my suggestion is **`Datum`** (geodetically apt; literally "the given/fact"), but `Locus` / `Worldpoint` / something else works if you'd rather. And two calls scope the first `Aerial ⇒ Terrestrial` vertical:

1. **Native systems** — my recommendation: **geodetic `latitude`/`longitude` on both sides** for the opener, deferring Terrestrial's projected grid. Then the horizontal still round-trips `geodetic → ECEF → geodetic` — exercising the canonical *without* dragging in Transverse Mercator.
2. **Conversion scope** — real **geodetic ⇄ ECEF** (closed-form WGS84), since that's the meaningful, bounded part; the heliocentric translation (time/ephemeris) stays out until we first cross to Ethereal.

---

**[Dev]** What about Observable instead of Datum? I've use it in similar contexts, but would it work here?

**[Claude]** Yes — and I think it's *better* than `Datum`, for a reason that's load-bearing here.

The media's messages (`PositionReport`, `LocationReport`, `FixReport`, `EphemerisReport`) are perspectival **observations** — each a medium's representation of something. The world-fact is the objective thing those reports are reports *of*. Naming it **`Observable`** makes "preserves meaning" literal: the reports differ, the **Observable is one**. The transform is correct iff every report round-trips to the same `Observable`.

It also sits exactly opposite your `Holon` axis: a `Holon` is the *perspective that observes*; the `Observable` is *what is observed* — the objective invariant within the reference frame, where the `Holon` is the subjective POV. That duality is clean and on-theme.

And it scales without a rename: `Observable` starts carrying just the dual-frame position, and later grows identity / velocity / time as the tracked thing acquires more state — it's the *entity*, position is just its first attribute.

One caveat, small: `Observable` is famous from reactive streams (Rx), but this codebase is Pekko (which uses `Source`/`Flow`/`Sink`, never `Observable`), and it's namespaced `domains.world.Observable` — so no real clash, just a possible reader double-take.

So: **`Observable`** for the world-fact. With the name settled, I just need your two first-vertical calls — geodetic both sides, and real geodetic ⇄ ECEF with heliocentric/time deferred?

---

**[Dev]** I'll go with deferral in both since it seems like risk reduction, and recovering the the deferred work later does not seem problematic.

**[Claude]** Both deferrals taken — that scopes a clean, self-contained first slice. I'll build it as the **semantic core** (the meaning-preserving transform as typed forms + geodesy + a direct test), and leave the **actor wiring** (World.Consumer/Provider + per-subdomain adapter actors, and enriching the loose reports with lat/lon) to the next slice. That way the risky part — the coordinate math and meaning-preservation — gets proven in isolation before any plumbing.

*(Built the slice, after confirming the `DracoType` contract so the typed forms — which extend `Aerial`/`Terrestrial`, not a `Json` shell — carry `typeDefinition`.)* What landed:

- **`Observable`** — the world-fact, `geocentric` + `heliocentric: Cartesian`, with `fromGeodetic`/`toGeodetic` Geocentric/Axial adapter primitives (heliocentric stays `Cartesian.Null`, deferred).
- **`Geodesy`** — WGS84 geodetic ⇄ ECEF, closed-form (Bowring inverse), a plain utility.
- **Typed `Position`** (Aerial) and **`Location`** (Terrestrial) — named-field composition trees, geodetic both sides.
- **`AerialTerrestrialTransformTest`** — `Position(51.5, −0.12, 35000 ft) → Observable → Location`, asserting lat/lon preserved (<1e-6), `elevationMetres == 10668` (35000 ft reframed through the canonical), the `geocentric` frame equals the ECEF of the point, and all three are `World` subtypes.
- One additive change: `trait World extends DracoType` (so the typed forms carry `typeDefinition`).

**Compile checkpoint:** `sbt "testOnly domains.world.* domains.aerial.* domains.terrestrial.*"`. This is the moment "preserves meaning" becomes a passing assertion — if the geodesy round-trips cleanly, the Observable-as-invariant model holds. (Geodesy is the one place a numeric bug could hide; the test's tolerances should catch it.)

*(Delegated action — Dev ran it: **10/10 pass**, headline "an Aerial Position transforms to a Terrestrial Location with the Observable (world-fact) preserved." The WGS84 geodesy round-trips cleanly — latitude/longitude recovered to <1e-6, altitude reframed 35000 ft → 10668 m through the canonical, the `Observable`'s geocentric frame equal to the point's ECEF. The world-fact survives the change of representation, and it's now a passing assertion. Wrote this journal chapter.)*

**Slice B, the remaining plumbing for this vertical:** (1) `World.Consumer` / `World.Provider` + the per-subdomain **input adapter** (`Format[Json] ⇒ Position ⇒ Observable`) and **output adapter** (`Observable ⇒ Location ⇒ Format[Json]`) actors; (2) enrich the loose `PositionReport` / `LocationReport` payloads with `latitude` / `longitude`; (3) run it **end-to-end report→report** through the actors and the `SourceSink` stand-in. That's a clean next slice whenever you're ready — a natural place to pause, too.

---

**[Dev]** Context at 63%. Time for new session?

**[Claude]** Yes — this is a clean handoff point, so I'd start fresh.

Why it's a good boundary: **green milestone, nothing in flight** — the first transform works, all 10 tests pass, no half-built slice to carry in your head. **State is fully persisted** — chapter 46 captures the whole arc, and the `project_media_domains_example` memory has the current architecture, the `Observable`/`Cartesian`/`Geodesy`/typed-form details, every resolved decision, and **Slice B spelled out as the next step** — a fresh instance can pick it up cold. And 63% after a long, detailed session means a new window will think faster and cheaper than pushing this one further.

The next session's pickup, in one line: *Slice B — wire the green semantic core into the actor transformation service (`World.Consumer`/`Provider` + per-subdomain input/output adapter actors), enrich the loose `PositionReport`/`LocationReport` payloads with `latitude`/`longitude`, and run it end-to-end report→report through the actors + the `SourceSink` stand-in.*

One optional housekeeping note: the `project_media_domains_example` memory has grown long from many incremental appends — the first thing I do next session could be `/consolidate-memory` on it, but it's fully readable as-is. Good place to stop. The spine that carries meaning across media exists and holds.

---

**[Dev]** Ok, draco-git-record for Commit and Push

**[Claude]** *(after checking the git state)* Here are the commit and push commands for your record — I'm providing them for you to run, not executing them myself. `git add -A` captures all the adds, renames (Provider/Transformer/GroundReport → their replacements), and modifications from this session in one coherent commit:

```bash
git add -A && git commit -F - <<'EOF'
Media message-domains + World transformation service: first meaning-preserving transform

Example domains (src/mods) supporting TransformBuilder — four physical transport
media as message domains over a supervening World, built two-phase from JSON.

Phase 1 — four media as SourceSink pipelines (Aerial/Terrestrial/Marine/Ethereal):
- Each: domain + intent/report shells (loose io.circe.Json behind strong Format[Json]
  handles) + Creator + Consumer, over a rule-RHS -> ActorRef send via the Evrete
  Environment seam (session.set("role", ref) <-> ctx.getRuntime().get).
- Deliberately divergent native representations: POSITION/callsign/altitudeFeet,
  LOCATION/unit/elevationMetres, FIX/vessel/depthFathoms,
  EPHEMERIS/object/altitudeKilometres.
- Per-medium Transformer relay stripped — transforms live in World, not in a Format
  domain.

World transformation service (Phase 2 opening):
- World super-domain; the four media derive it (message-domain discipline via
  Domain[T]). trait World extends DracoType.
- Canonical = dual-frame 3D Cartesian (Geocentric/Axial ECEF + Heliocentric/Ecliptic),
  named-field x/y/z with a positional-value constructor.
- Observable = the world-fact (the objective invariant reports observe; opposite the
  Holon-as-observer axis), holding both frames.
- Geodesy = WGS84 geodetic <-> ECEF (closed-form, Bowring inverse).
- First meaning-preserving transform: Aerial Position -> Observable -> Terrestrial
  Location, geodetic both sides; "preserves meaning" is now a passing assertion.
  Heliocentric translation + time + projected grid deferred.

Dev journal chapters 42-46. All tests green.

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>
EOF
```

Then push `main`: `git push origin main`. That's 106 files in one commit — the full session's arc from the Aerial chain through the first World transform. Once it's pushed, a clean new session can pick up Slice B from the memory + chapter 46.

---

**[Dev]** Add a project markdown how-to for neozoan user who wants start on an unprepared Window, MacOS, or Linux/Unix platform and then download a project clone and install git, sbt and scala-cli and run any test app or tool in the project using sbt or the native shell or cli

**[Claude]** *(after gathering the exact project facts — tool scripts, sbt/Scala versions, clone info — so the guide is accurate rather than generic)* Created `GETTING_STARTED.md` at the repo root — a from-scratch onboarding guide grounded in this project's actual specifics:

- **Four tools, three platforms** — Git + JDK 17 + sbt + scala-cli, with copy-paste blocks for macOS (Homebrew + Coursier), Linux/Unix (Coursier), and Windows (winget *or* Coursier).
- **The Java-17 pin front and center** — leads with it and threads it through every install + the troubleshooting, since that's the project's real gotcha (`bad constant pool index` on a newer JDK).
- **Clone** — `git clone https://github.com/ejb816/nexonix.git`.
- **Run tests via sbt** — `sbt test`, real `testOnly` examples (`draco.primes.PrimesRulesTest`, `domains.world.*`), the `-- -z` name filter, and the interactive shell with `~compile`.
- **Run the app + tools natively** — `draco.CLI`, and the two real wrappers `bin/draco-gen` / `bin/draco-sc` (with `sbt assembly` first, and the Windows → Git Bash/WSL caveat).
- **Troubleshooting** + an optional IntelliJ note.

It's a new untracked file, so it'll show up in `git status` for your next commit. If you'd like, I can also add a one-line "Getting Started" link near the top of `README.md` pointing to it.

---

## Status

The hardest part — the coordinate math and the meaning-preservation it underwrites — is proven in isolation: `Position(51.5°, −0.12°, 35000 ft)` → `Observable` → `Location`, **10/10 green**, the world-fact intact through the change of representation. What remains is plumbing: **Slice B** wires the semantic core into the actor transformation service — `World.Consumer`/`Provider` + per-subdomain input/output adapter actors — enriches the loose `PositionReport`/`LocationReport` payloads with `latitude`/`longitude`, and runs it end-to-end report→report through the actors and the `SourceSink` stand-in. The Heliocentric/Ecliptic frame and the projected grid wait their turn. Session closed at a green milestone: commit record issued (106 files, chapters 42–46), `GETTING_STARTED.md` added for the next commit, Slice B queued as the fresh session's pickup. The spine that carries meaning across media now exists and holds.
