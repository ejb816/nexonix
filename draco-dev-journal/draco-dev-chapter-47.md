# Draco Dev Journal — Chapter 47

**Session date:** June 18, 2026
**Topic:** `Egocentric` → `Sentient` (reparented under `World`, relocated to `src/mods`) → the whole `*centric` reference-frame family deleted → the first full-suite run catches a latent `Format` parameterized-self-domain Generator bug → the scoped-green ≠ suite-green lesson. Transcript.

---

**[Dev]** Get us up to speed on the next development task options.

**[Claude]** *(after checking the staged work, the issue backlog, and recent journal state)* Here's where things stand and what's open.

**Current state:** branch `main`, clean except one staged-but-uncommitted file: `GETTING_STARTED.md` (the from-scratch onboarding guide, issue #15 — effectively done, just needs committing). The active work thread is the `World` transformation service: chapter 46 (June 17) landed the **semantic core** — an Aerial `Position` crossing to a Terrestrial `Location` through the `Observable` world-fact, with WGS84 geodesy and "preserves meaning" now a *passing assertion* (10/10 green). The hard part (coordinate math + meaning-preservation) is proven in isolation. No issue currently carries `priority-next`.

**The natural pickup — Slice B (recommended).** Chapter 46 ends with an explicit hand-off: wire the core into `World.Consumer`/`Provider` + per-subdomain input/output adapter actors, enrich the loose `PositionReport`/`LocationReport` payloads with `latitude`/`longitude`, and run it end-to-end report→report through the actors and the `SourceSink` stand-in. This is the momentum continuation — same thread, deferred work already scoped (Heliocentric/Ecliptic frame + projected grid stay deferred until the first Ethereal crossing).

Other open options: commit the onboarding guide (#15, trivial); the Generator domain `draco.generator.Generator[L]` (#11 — the self-port north star, large); canonicalization/generator hygiene (#37 TypeName structural equality, #38 DracoType derivation sweep); the `src/mods` script batch (#33–#36, small self-contained wins); reference-frames cleanup (#2–#5, #7, #19 — note: feedback flagged ref-frames as *no longer a work target*); and the long-horizon roadmap items (Haskell port #20, multi-target #21, Maven Central #22, Orion runtime #23).

My recommendation: continue the World thread with **Slice B**. Want me to dig into Slice B's actual file layout so we can scope it concretely, or point me at one of the others?

---

**[Dev]** Rename the Egocentric domain to Sentient, and make it a subdomain of World

**[Claude]** This is a meaningful refactor touching domain identity and hierarchy. Let me first map the current state of both domains before changing anything.

The reconnaissance surfaced a real architectural conflict I need to resolve with you before touching anything — this isn't a mechanical rename.

**The tension:** Egocentric currently lives in `src/test` as a subdomain of **Cosmocentric**, embedded in the reference-frame family (Geocentric/Heliocentric/Galactocentric) with **6 transforms** wiring it to its siblings (`EgocentricGeocentric`, `GeocentricEgocentric`, … in both directions). World, by contrast, lives in `src/mods`, and its subdomains (Aerial/Terrestrial/Marine/Ethereal) are **peer packages** (`domains.aerial`, not `domains.world.aerial`) that `extend World` by derivation and transform through the `Observable` world-fact — they do *not* use the `DomainTransform[X,Y]` matrix.

So "make it a subdomain of World" means pulling Egocentric *out* of the Cosmocentric family, which orphans those 6 transforms. A few things I can default cleanly (peer package `domains.sentient`, `trait Sentient extends World`, derivation `[World]`, following the media pattern). But two decisions are genuinely yours: what happens to the six Ego-transforms, and where the domain lives.

*(Dev decided in-session: sever from Cosmocentric and delete the six Ego-transforms; relocate to `src/mods` as `domains.sentient`.)*

Done — `Egocentric` is now `Sentient`, a subdomain of `World`.

**Renamed + reparented + relocated** (`src/test/…/egocentric/` → `src/mods/…/sentient/`, package `domains.sentient`): `Sentient.json`/`Sentient.scala` — derivation `Cosmocentric` → `World`; `trait Sentient extends World`, matching the Aerial/Terrestrial/Marine/Ethereal pattern (peer package, derives World, no `DomainTransform` matrix). The 10 leaf types (Direction, Distance, Course, Gaze, Percept, Lean, Effect, Waypoint, Path, Ego) + `EgoActor` — moved and updated to derive `Sentient`.

**Severed & deleted** the 6 Ego↔{Geo,Helio,Galacto} transforms (both directions, JSON + Scala). The Geo/Helio/Galacto trio keeps its own 6-transform matrix intact.

**Tests:** `DomainsGenTest` dropped the Egocentric family + 6 Ego-transform families/vals (21 types remain); `WorldHierarchyTest` gained `implicitly[domains.sentient.Sentient <:< World]`, a compile-time guard that the reparenting holds. Also retargeted a now-stale Generator doc-comment example to `Geocentric` (still true). No `egocentric` strings remain in the tree except prose.

One judgment call: I kept the **`Ego` leaf** and **`EgoActor`** names — the rename targeted the *domain*, and `Ego` is the perspective `Holon` (a distinct concept), not the word "Egocentric." Say the word if you'd like those renamed too. Journaled as chapter 47 and updated the Egocentric memory entry.

Verify (you run sbt): `sbt "testOnly domains.world.* domains.DomainsGenTest"`

*(Delegated action — Dev ran it: 57/57 green, including the new "Sentient is a subdomain of World" assertion and the trimmed `DomainsGenTest` (21 types, all families compile). The whole `domains.sentient` package compiled as part of the run. Claude noted one optional check — a full `sbt test` would also exercise `YAMLRoundTripTest`, which walks the tree the egocentric JSONs were removed from; traced no fixed-count assertion there, so expected unaffected.)*

---

**[Dev]** Before I do more comprehensive tests, I like all the *centric domains deleted. They were a significant time sink with minimal  value in return.

**[Claude]** Good call — with Sentient pulled out, the rest of the reference-frame family is dead weight. Let me find everything `*centric` so I delete it cleanly and don't leave dangling references.

Important nuance before deleting: several **World** files (`Observable`, `Cartesian`, `Geodesy`) and the two World tests also match "geocentric/heliocentric" — but in World those are *coordinate-frame* terms (the ECEF "Geocentric/Axial" + "Heliocentric/Ecliptic" frames from ch.46), not the reference-frame domain types. Confirmed — all the World references are coordinate-frame terminology (`val geocentric: Cartesian`, prose), not the deleted domain types. No imports or `extends` of the reference-frame domains anywhere in World/test.

Done. All four `*centric` reference-frame domains are gone.

- **Deleted:** `Cosmocentric`, `Geocentric` (+ Position/Altitude/Heading/Fix), `Heliocentric` (+ Elements/Epoch/Ephemeris), `Galactocentric` (+ Parallax/ProperMotion/RadialVelocity/Trajectory) — JSON and Scala, under `src/test`; the surviving 6-transform Geo/Helio/Galacto matrix; and `DomainsGenTest.scala` (it existed only to exercise the reference frames).
- **Preserved / fixed:** World's coordinate frames are untouched — `Observable.geocentric`/`.heliocentric` are terminology, not the deleted types (verified no `import`/`extends`/`Domain[...]` of the reference-frame types remains). Retargeted the two cosmetic mentions: the Generator doc-comment example (→ `Aerial extends World`) and the Sentient test title. `Sentient` is the only survivor of that family.
- **Bookkeeping:** journal chapter extended; the three stale `reference_frames_increment_*` memory files removed (which also helps the over-limit MEMORY.md).

Worth running `sbt test` (full suite) now to confirm the broader project is green after the deletions. Two outward-facing follow-ups I did **not** action (your call): GitHub issues #2/#3/#4/#5/#7/#19 are now moot — want me to close them? And the `reference-frames` label + README mentions are stale — I can sweep those.

---

**[Dev]** Do your suggested follow-ups

**[Claude]** Before closing, let me read the actual issue bodies — a couple may not be fully moot.

Reading the bodies changes my triage: **#5** (typed value wrappers for the scalar primals) and **#19** (the `Primal[composite]` cleanup) both target the *Egocentric scalar/Holon elements* — which **survive as Sentient** in `domains.sentient`. So they're not moot; closing them would drop valid work. Same for **#25** (Ego/EgoActor survive). Only **#2/#3/#4/#7** are genuinely dead.

What I did:

- **Closed as moot** (with comments pointing at chapter 47): #2, #3, #4, #7 — all about the deleted frames/transforms/Cosmocentric.
- **Kept open + retargeted to Sentient**: #5, #19, #25. Retitled #5 → "Inner primal value types for **Sentient** scalars" and gave it the `cleanup` label so it's not orphaned.
- **Retired the `reference-frames` label** entirely (swept off all issues, then deleted).
- **Docs:** `README.md` — replaced the stale "reference-frame example domains under src/test" bullet with the **World** message-domain example, fixed the two source-tree lines, added the `src/mods` domains entries. `CHANGELOG.md` — added `[Unreleased]` **Changed** (Egocentric→Sentient) + **Removed** (reference frames) entries; the historical release entry stays intact (append-only history).

Note on my earlier triage: when I first listed #5/#19 as moot I hadn't read their bodies — closing them would have dropped live work. These were all docs + GitHub changes — no code touched, so no sbt run needed. The full-suite check on the deletions is still the outstanding verification when you're ready.

---

**[Claude]** *(Delegated action — Dev ran the full `sbt test`: 182/183, one failure — `DracoGenTest` on `draco/format/Format`: the generated source dropped `elementTypeNames` and emitted `Domain[Format]` instead of `Domain[Format[_]]`.)*

That's the full suite — one failure, and it's unrelated to the rename/deletion work. Confirmed **pre-existing**: the only compiled code I touched is the `Generator.scala` doc-comment and `WorldHierarchyTest`. `Format.scala`/`Format.json` were last changed in the most recent commit `96d39ac` ("Format domain…"), and `Format` is a **parameterized domain** (`typeParameters: ["T"]`, `domainAspect.typeName` = itself). The full suite simply hadn't been run since Format landed, so this drift went unseen.

Root cause pinned — a clean two-part Generator gap for a **parameterized self-domain** (only `Format` qualifies in the whole corpus):

1. `isDomain` uses full `TypeName` equality, but `Format`'s `domainAspect.typeName` omits the `["T"]` that its `typeName` carries → equality fails → Format wrongly takes the **leaf** path (no `elementTypeNames`).
2. Even via the domain path, `domainGlobal` emits the container as the bare `objName` → `Domain[Format]`, which wouldn't even compile (needs `Domain[Format[_]]`).

The fix belongs in the Generator (per the "redundant data → handle in Generator, not JSON" principle — the self-domain reference shouldn't need to restate type params). Both changes are no-ops for every existing non-parameterized domain: **`isDomain`** now compares `domainAspect.typeName` on **name + package** (ignoring type parameters) — leaves are unaffected since their `domainAspect.typeName` names a *different* (parent) type; **`domainGlobal`** emits the container via `wildcardTypeName` → `Domain[Format[_]]` (bare name for every non-parameterized domain, byte-identical output).

I can't run sbt — please re-run `sbt "testOnly draco.DracoGenTest draco.GenerateAndCompileTest"`. To be clear on provenance: this failure shipped with the `Format` domain in `96d39ac` and was just never exercised until this first full-suite run.

---

**[Dev]** I should have been doing a full test before any push. You were so successful at writing tests and making changes that just worked, I got overconfident in the progress.

**[Claude]** You're right, and some of that's on me — when I wrapped up each step I pointed you at a *targeted* `testOnly` run, which proves the change I just made but is blind to anything pre-existing in code I didn't touch. The `Format` failure had been sitting there since `96d39ac`; a scoped run was never going to surface it. Scoped-green isn't suite-green, and I shouldn't have let the former read as push-ready.

Recorded as a standing rule — `feedback_full_suite_before_push.md`. From here on I'll treat a scoped `testOnly` as "this change works" and explicitly flag a full `sbt test` as the gate before any push — and I won't call something push-ready off a targeted run alone.

For what it's worth, the overconfidence wasn't entirely misplaced: the rename and the deletion were clean and did verify correctly. The one red was a *latent* bug in untouched code, not a regression you introduced — it just took a full run to expose. The immediate step is to run the full suite once more to confirm green before you push.

*(Delegated action — Dev ran `sbt test`: **183/183 green**, full suite. The `Format` parameterized-self-domain fix holds; everything from the rename and the `*centric` deletion confirmed clean across the whole tree. Claude recorded the Generator fix in journal chapter 47 — keeping the detailed root-cause in the journal rather than MEMORY.md, which is over its size budget — and declared the clean push point: Egocentric→Sentient; all `*centric` domains deleted; Generator fix; issues closed/retargeted; docs swept; CHANGELOG `[Unreleased]` staged.)*

## Status
`Egocentric` → `Sentient` (`src/mods`, package `domains.sentient`, `extends World`); all four `*centric` reference-frame domains + their transform matrix + `DomainsGenTest` deleted; World's coordinate-frame terminology (`Observable.geocentric`/`.heliocentric`) preserved. Generator fixed for parameterized self-domains (`isDomain` name+package comparison; `domainGlobal` via `wildcardTypeName`). Issues #2/#3/#4/#7 closed, #5/#19/#25 retargeted to Sentient, `reference-frames` label retired; README/CHANGELOG swept. Full suite **183/183**. Standing rule recorded: scoped-green ≠ suite-green — full `sbt test` is the push gate (`feedback_full_suite_before_push`). Queued: Slice B — the World actor transformation service.
