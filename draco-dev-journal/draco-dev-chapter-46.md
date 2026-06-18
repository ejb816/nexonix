# Draco Dev Journal — Chapter 46

**Session date:** June 17, 2026
**Topic:** `World`'s first transform. A long, precise spec dialogue settled the canonical coordinate frame (dual Geocentric/Axial + Heliocentric/Ecliptic Cartesian, ECEF), the named-vs-positional coordinate representation (both, bridged by the constructor), and the world-fact's name (`Observable`). Then the semantic core landed: an Aerial `Position` crosses to a Terrestrial `Location` through the `Observable`, with WGS84 geodesy, and **"preserves meaning" became a passing assertion.**

---

## Determining the coordinate names

The thin payloads (one discriminator, one id, one vertical) had no horizontal coordinates. Adding them opened the real problem:

> **Dev:** So how will the horizontal plane coordinate names be determined?

The vertical had been a gift — one axis, divergence by *unit* (feet/metres/fathoms/km), names picked freely. The horizontal plane is two axes where the media diverge by *coordinate system*, so the names are *determined by the system* (geodetic → latitude/longitude, grid → easting/northing, orbital → right ascension/declination). And meaning-preservation now needs a shared canonical the systems project to and from — the reference-frame substrate returning.

> **Dev:** The World needs to support two frames simultaneously: Geocentric/Axial and Heliocentric/Ecliptic.

`World`'s invariant: not one canonical frame but two, materialized at once — Earth-centered/equatorial and Sun-centered/ecliptic, related by a rotation (obliquity ε≈23.44°, constant) and a translation (Earth↔Sun, time-dependent). Terrestrial media live in Geocentric/Axial, Ethereal in Heliocentric/Ecliptic; a terrestrial⇄ethereal transform *is* that frame conversion.

> **Dev:** World canonical = 3D Cartesian, Polar axis = z-axis, Geocenter = origin?

Confirmed, with one open degree of freedom flagged — origin + z fix only two axes; the x-axis direction is the Earth-fixed vs inertial fork. The recommendation (x → prime meridian, ECEF) makes the terrestrial media's `lat/lon/alt → x/y/z` time-free, so the opener needs no ephemeris.

> **Dev:** Confirmed.

## Named or positional? Both.

draco's existing coordinates are *positional* — `Coordinate[T <: Product]` holds a tuple, the reference frames use `Holon[tuple]`. But the Dev's own data-model axiom — messages are composition trees of *named* fields — pointed the other way. Raising the fork drew the resolution:

> **Dev:** The type value constructors can automatically map position coordinate instance values into the name-field values.

So not a choice: a **named-field** type whose **Factory maps a positional coordinate's values into the named fields** (`(_1, _2, _3) → x, y, z`). Named surface, positional substrate, the constructor bridging them. `domains.world.Cartesian` was built to that shape (named `x`/`y`/`z`, plus `apply((x, y, z))`), mirroring the `RuleAspect` `elements`+`factory` pattern — and it compiled green, confirming the keystone.

## Naming the world-fact

> **Dev:** What about Observable instead of Datum? I've used it in similar contexts, but would it work here?

It works *better*. The media's messages are perspectival **observations**; the world-fact is the objective thing they're reports *of*. Naming it `Observable` makes "preserves meaning" literal — the reports differ, the `Observable` is one — and it sits exactly opposite the `Holon` axis: a `Holon` is the perspective that observes, an `Observable` is what is observed. (Minor caveat: Rx's famous `Observable`, but the codebase is Pekko and it's namespaced.)

## The first transform — risk-reduced

> **Dev:** I'll go with deferral in both since it seems like risk reduction, and recovering the deferred work later does not seem problematic.

Both deferrals taken: geodetic on both sides (Terrestrial's projected grid deferred), and real geodetic ⇄ ECEF only (the Heliocentric/Ecliptic translation + time deferred until the first Ethereal crossing). That scoped a self-contained **semantic-core** slice — the transform as typed forms + geodesy + a direct test, leaving the actor wiring for next.

Built: `Observable` (dual-frame, `fromGeodetic`/`toGeodetic` Geocentric/Axial primitives — using `Cartesian`'s positional constructor to map the ECEF triple); `Geodesy` (WGS84 geodetic⇄ECEF, Bowring inverse, a plain utility); typed `Position` (Aerial) and `Location` (Terrestrial); and one additive change, `trait World extends DracoType`, so the typed forms (which extend their medium, not a `Json` shell) carry `typeDefinition`.

The test crosses an Aerial `Position(51.5°, −0.12°, 35000 ft)` to a Terrestrial `Location` through the `Observable`:

> **Dev:** [`testOnly domains.world.* …`]
> *an Aerial Position transforms to a Terrestrial Location with the Observable (world-fact) preserved*
> **Tests: succeeded 10, failed 0**

Latitude/longitude recovered to <1e-6, altitude reframed 35000 ft → 10668 m through the canonical, the `Observable`'s geocentric frame equal to the point's ECEF. The world-fact survived the change of representation: meaning preserved, and now checkable.

## Where this leaves things

The hardest part — the coordinate math and the meaning-preservation it underwrites — is proven in isolation. What remains is plumbing: **Slice B** wires the semantic core into the actor transformation service — `World.Consumer`/`Provider` + per-subdomain input/output adapter actors — enriches the loose `PositionReport`/`LocationReport` payloads with `latitude`/`longitude`, and runs it end-to-end report→report through the actors and the `SourceSink` stand-in. The Heliocentric/Ecliptic frame and the projected grid wait their turn; the spine that carries meaning across media now exists and holds.
