# Draco Dev Journal — Chapter 47

**Session date:** June 18, 2026
**Topic:** `Egocentric` → `Sentient`: the perspective frame leaves the Cosmocentric reference-frame family and becomes a subdomain of `World`, relocating from `src/test` to `src/mods` alongside the other media (Aerial/Terrestrial/Marine/Ethereal).

---

## The prompt

> **Dev:** Rename the Egocentric domain to Sentient, and make it a subdomain of World

A two-word rename on the surface, but reconnaissance surfaced that `Egocentric` was not a free-standing domain: it was a **subdomain of `Cosmocentric`**, embedded in the reference-frame family (Geocentric/Heliocentric/Galactocentric) and wired to its three siblings by **six transforms** (`EgocentricGeocentric`, `GeocentricEgocentric`, … both directions). `World`, by contrast, lives in `src/mods`, and its media subdomains are **peer packages** (`domains.aerial`, not `domains.world.aerial`) that `extend World` and cross meaning through the `Observable` world-fact — they do *not* use the `DomainTransform[X,Y]` matrix.

So "make it a subdomain of World" *pulls Egocentric out of the Cosmocentric family*, orphaning those six transforms. Two decisions were genuinely the Dev's:

> **Decision 1 — transforms:** *Sever from Cosmocentric and delete the six Ego-transforms.* Sentient becomes a clean World medium that crosses via the `Observable`, like Aerial/Terrestrial. Geo/Helio/Galacto keep their own 3×2 = 6-transform matrix among themselves.
>
> **Decision 2 — location:** *Move to `src/mods`.* Sentient (+ its ten leaf types + `EgoActor`) joins the other media as a real World peer, package `domains.sentient`.

## What changed

- **Moved + renamed** the eleven definition files (`Sentient` + Direction/Distance/Course/Gaze/Percept/Lean/Effect/Waypoint/Path/Ego) and their Scala from `src/test/{resources,scala}/domains/egocentric/` to `src/mods/{resources,scala}/domains/sentient/`. `EgoActor.scala` came along. The leaf names are unchanged — only `Egocentric→Sentient` and the package moved; the `Ego` leaf (the perspective `Holon`) keeps its name.
- **Reparented:** `Sentient.json` derivation `Cosmocentric` → `World`; `trait Sentient extends World`. The ten leaves derive `Sentient` exactly as they derived `Egocentric`.
- **Deleted** the six Ego-transforms (the three `domains.egocentric.{geo,helio,galacto}centric` outbound and the three `domains.{geo,helio,galacto}centric.egocentric` inbound), JSON and Scala.
- **`DomainsGenTest`** lost the Egocentric family and the six Ego-transform families/vals (21 types remain: Cosmocentric + the three outward frames + their leaves + the surviving 6-transform matrix). Doc/counts updated.
- **`WorldHierarchyTest`** gained a compile-time assertion — `implicitly[domains.sentient.Sentient <:< World]` — that fails to compile if the reparenting regresses.
- A stale Generator doc-comment example (`Egocentric extends Cosmocentric`) was retargeted to `Geocentric`, which still holds.

## Why it fits

`Sentient` names the **observing** subdomain — the medium whose messages are first-person percepts and effects — sitting opposite the `Observable` (the observed world-fact) on the same axis Chapter 46 established. As a World medium it will eventually cross to Aerial/Terrestrial/etc. through the `Observable`, not through a bespoke frame-to-frame transform. The reference-frame matrix (a different, geometric model) stays self-consistent without it.

## Verify

```
sbt "testOnly domains.world.* domains.DomainsGenTest"
```
(The Dev ran it: 57/57 green, including the new Sentient assertion.)

## Then: the rest of the frames go

> **Dev:** Before I do more comprehensive tests, I like all the *centric domains deleted. They were a significant time sink with minimal value in return.

With Sentient extracted, the remaining reference-frame family — `Cosmocentric`, `Geocentric`, `Heliocentric`, `Galactocentric`, their leaves, and the surviving 3×2 transform matrix — had no dependents and little payoff. **Deleted wholesale**, along with `DomainsGenTest` (which existed only to exercise them).

The one subtlety: World legitimately uses `geocentric` / `heliocentric` as **coordinate-frame** field names on `Observable`/`Cartesian` (the ECEF dual-frame from Chapter 46). Those are not the deleted domains and stayed put; a grep confirmed no `import`/`extends`/`Domain[...]` usage of the reference-frame *types* survived. Two cosmetic mentions were retargeted — a Generator doc-comment example (→ `Aerial extends World`) and the Sentient test title.

The reference-frame increment memory files were deleted (history lives here), and the ref-frame GitHub issues (#2/#3/#4/#7) were closed as moot (#5/#19/#25 stayed open, retargeted at the surviving `Sentient` types); the `reference-frames` label was retired.

## A latent failure the full suite caught

The scoped runs were green, so the first full `sbt test` was the real gate — and it red-flagged one pre-existing failure: `DracoGenTest` on `draco/format/Format`. Unrelated to this work (shipped with the `Format` domain in `96d39ac`, never exercised by a scoped run). Root cause: `Format` is the corpus's only **parameterized self-domain** (`Format[T]`, `domainAspect.typeName` = itself). Two coupled Generator gaps:

- `isDomain` compared `domainAspect.typeName` by full `TypeName` equality; `Format`'s self-reference omits the `["T"]` its `typeName` carries, so equality failed and `Format` fell through to the leaf path (no `elementTypeNames`).
- `domainGlobal` emitted the container as the bare object name → `Domain[Format]`, which wouldn't even compile (needs `Domain[Format[_]]`).

Fix (in the Generator, since the self-reference shouldn't have to restate type params): `isDomain` now compares name + package only; `domainGlobal` emits the container through `wildcardTypeName`. Both are no-ops for every non-parameterized domain. Full suite: **183/183**.

The lesson stuck as a standing rule — scoped-green ≠ suite-green; full `sbt test` is the push gate (memory `feedback_full_suite_before_push`).
