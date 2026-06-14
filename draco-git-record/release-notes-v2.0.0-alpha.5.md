## v2.0.0-alpha.5 — DomainBuilder, the first `src/mods` stand-in

This release opens the usability/scalability build-out toward the **dreams** (user development) and **orion** (deployment) tiers. `src/mods` now hosts user-callable stand-ins for under-development core features — given to early-access users now, with a clean promotion path into the framework — and the first one establishes how a domain dictionary becomes fully usable from JSON data alone.

### Highlights

**`draco.DomainBuilder` — build a domain dictionary from JSON alone**

The first stand-in, public-API-only (no new dependencies):

- **`define`** loads a domain *and every member's full definition*, returning a concrete dictionary that is actually **populated** — the non-hollow counterpart to core's `TypeDictionary.apply`, which holds member names without content.
- **`dictionary`** assembles the cross-domain registry from defined domains.
- **`validate`** rigorously checks structural invariants — self-declaration, completeness (no unauthored members), and derivation resolvability (no dangling inheritance) — returning human-readable problems.
- **`generate`** emits Scala for the whole dictionary, tolerant of incomplete (skeleton) members so an in-progress *user* domain still builds.

Tested by `DomainBuilderTest` over the four endogenous domains — Draco, Base, Primes, Language.

**A root-compiled `src/mods/scala/draco` staging tier**

A second mods compilation track: this directory is compiled *into* the draco artifact (not the scripts subproject), so hand-written stand-ins ship in the jar, are conflict-checked against `src/main/scala/draco` as same-package/same-project, and are testable from `src/test` with no cross-project cycle. Documented in `src/mods/README.md`.

**`Generator` and `GeneratorCLI` relocated out of the JSON type system**

`DomainBuilder.validate` immediately earned its keep: it caught that `Draco` declared `Generator` as a domain member with no JSON backing. The hand-written engine and its CLI moved into the staging tier (still compiled into root, so all ~50 `Generator.loadType` callers are unchanged), and `Generator` was dropped from `Draco`'s member list. This establishes the invariant the validator enforces: **a declared Draco domain member is JSON-backed.**

**Toolchain: sbt 1.7.3 → 1.12.9**

Moved to the current sbt 1.x. This required dropping the build's `managedScalaInstance := false` plus its manual `scala-tool` configuration and letting sbt auto-manage the Scala instance, so it resolves the matching `2.13.16` compiler bridge correctly under sbt 1.12's Zinc.

### Notes

- Full test suite green on sbt 1.12.9.
- Dev-journal chapter 40 documents the session in full, including the design alternatives that were considered and not taken.

**Full details:** see [`CHANGELOG.md`](https://github.com/ejb816/nexonix/blob/main/CHANGELOG.md).
