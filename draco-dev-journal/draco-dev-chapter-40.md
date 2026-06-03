# Draco Dev Journal — Chapter 40

**Session date:** June 1–2, 2026
**Topic:** The first `src/mods` usability/scalability stand-in — `draco.DomainBuilder` — and everything its existence forced into the open: the hollow `TypeDictionary` gap, a staging-dir build model where `src/mods/scala/draco` compiles *into* root, a rigorous `validate` that immediately caught a real defect, the relocation of `Generator`/`GeneratorCLI` out of the JSON-backed type system, and a multi-round sbt 1.7.3 → 1.12.9 upgrade whose first fix was wrong.

---

## Opening — the layering charter

The session opened with a scope-setting statement, not a task:

> **Dev:** We now need to focus on early versions of usability and scalability features in the draco framework... Usability... means allowing domain experts/workers to build fully functional applications, services and tools, without knowing anything about specific programming languages or DevOps. ... draco... is just the core, with dreams the next outer layer, for user development, and orion as the third layer... Scalability... has two dimensions, functional and performant. The functional axis refers to the framework's inherent support of indefinite and incremental domain and type extensibility... The early versions... will first be developed in the src/mods subproject... essential early access to features that will end up as part of both the dreams and orion layers...

Grounding in `src/mods` showed it was read-only introspection only (six scala-cli scripts), no `dreams`/`orion` code yet. The first proposal — a "describe → scaffold → generate → verify" dreams slice — drew the first redirect:

> **Dev:** Before addressing dreams and orion specifically we need to use src/mods to create user callable functions that will be called in place of draco features that are still under development, like rules with patterns and actions that will be associated with the Generator domain that does not exist yet.

So: mods hosts **stand-ins** for under-development core features. The Generator-domain example mapped cleanly onto the existing imperative `if (isRule) … else if (isDomain) …` dispatch in `Generator.generate` — but then:

> **Dev:** I was just using Generator as an example, but that comes after some other functions I've yet to mention.

---

## The first function: a domain dictionary from JSON alone

> **Dev:** The ability to comprehensively define a domain dictionary with just JSON data and be able to create concrete instance of it and generate code from it, even if it is the stubbed skeleton.

Reading the dictionary types surfaced the gap that justifies the stand-in: `TypeDictionary.apply(domainDefinition)` is **hollow** — it builds members via the empty `TypeDefinition(TypeName(n))` constructor and never calls `Generator.loadType`, so a concrete dictionary holds member *names* without *content*. You cannot generate code from it. `DomainBuilder` is the populated counterpart: `define` loads each member's full `TypeDefinition`; `dictionary` assembles the cross-domain registry; `generate` emits Scala for the domain plus every member, skeleton-tolerant per "even if it is the stubbed skeleton."

The namespace was the Dev's call:

> **Dev:** Since it going to become part of draco it should be src/mods/{resources,scala}/draco for conflict detection and resolution

`package draco` shared with `src/main/scala/draco` so a duplicate FQN is a hard error.

---

## The cross-project cycle, and the Dev's key insight

A request — "write a test for DomainBuilder that builds the draco domains" — hit a wall: `DomainBuilder` was compiling in the `mods` sbt subproject, and `root`'s `src/test` can't depend on `mods` (`mods dependsOn root` already, so the reverse is a project cycle). My framing over-emphasized the test framework. The Dev cut through it:

> **Dev:** I should only need the mods subproject for writing and running scripts. Does that help the issue your describing?

It did — decisively. If the `mods` subproject is just for scripts, then `DomainBuilder` shouldn't belong to it at all. The resolution became a **two-track build**: `src/mods/scala/scripts` stays the `mods` subproject (consumes the jar); `src/mods/scala/draco` is compiled **into root** via `root`'s `Compile / unmanagedSourceDirectories`. Now `DomainBuilder` ships in the jar (what early-access users need), is testable from `src/test` with no cycle, and a duplicate `draco.*` FQN vs `src/main` is a same-project compile error. `mods` was scoped to `scala/scripts`. `DomainBuilderTest` landed in `src/test/scala/draco/`.

---

## validate, and the defect it caught

> **Dev:** I don't see a development related problem with allowing the DomainBuilder to rigorously test the draco endogenous domains. Any problems with that should be fixed sooner rather than later.

The non-contradiction was made explicit: the *capability* `generate` stays skeleton-tolerant (user domains have holes); the *test* of shipped domains is rigorous (they should have none). `validate(domain): Seq[String]` got battery 1–2 — self-declaration, completeness (no stub members), draco-internal derivation resolvability. It ran 12/13 green and the one failure was exactly the point:

```
Draco — validate reports no problems *** FAILED ***
  member Generator is declared but unauthored (no JSON on disk)
```

`Draco.json` listed `Generator` as a member, but `Generator` was the lone one of 42 with no `.json` — the last holdout of the 2026-04-15 inline-TD elimination.

---

## Generator: two forks, then a better third

I framed two fixes — **A** (extract `Generator`'s inline TD to JSON, make it a real member; but this re-opens `comparisonOnlyExcluded`, regressing #29) and **B** (drop it from `elementTypeNames`, leave the engine in `src/main`). The Dev proposed something cleaner than either:

> **Dev:** We kind of crossed wires here. I just interrupted you by suggesting that instead of making an exclusion as being in draco, remove it from src/main/scala/draco and put it permanently in src/mods/scala/draco without a JSON type definition anywhere (main or mods) and removing it from elementTypeNames in Draco.

The grep made it safe: **50 src/main files** call `Generator.load*` (the bootstrap spine — can't move tiers), but the **emitter** is consumed only by `GeneratorCLI`/tests/`DomainBuilder`. Crucially, moving `Generator.scala` to `src/mods/scala/draco` keeps it root-compiled (the staging dir compiles into root), so all 50 callers resolve unchanged — a source-location move, no tier change, no `main → mods` violation. It also makes `src/mods/scala/draco` mean "hand-written Scala in the jar, outside the JSON type system" (stand-ins + permanent engine), enforcing the real invariant: **a declared Draco domain member is JSON-backed** — exactly what `validate` checks.

`git mv` + dropping `"Generator"` from `Draco.json`. `DracoGenTest` then caught the expected coupling — the hand-written `Draco.scala` keeps its own `elementTypeNames` list — and that was synced too. **110/110.** `GeneratorCLI` followed `Generator` into the staging dir (no JSON, depends on `Generator.generate`).

---

## The sbt detour — and the fix that wasn't

An out-of-context question about sbt versions turned into a real yak-shave. The launcher was already 1.12.9 but `project/build.properties` pinned 1.7.3; bumping it surfaced two things. First, an `idea-shell` error after a manual `reboot` inside IntelliJ's managed shell (cosmetic — IntelliJ injects that command; a manual reboot drops it; use IntelliJ's Reload instead). Then the real one:

```
ServiceConfigurationError: xsbti.compile.CompilerInterface2:
  scala.tools.xsbt.CompilerBridge Unable to get public no-arg constructor
```

My first fix — add `scala2-sbt-bridge % scala-tool` to both scala-tool blocks — **did not work**. The dep resolved (confirmed in the Coursier cache), no `lib/` dir shadowed it, but the error was identical. Diagnosis: with `managedScalaInstance := false`, sbt doesn't *instantiate* the bridge the way Zinc 1.12 needs; it lets `ServiceLoader` try a no-arg ctor on `scala.tools.xsbt.CompilerBridge`, which fails. The loading *mechanism* was wrong, not a missing dep.

> **Dev:** [chose] Fix forward to 1.12.9

So the manual apparatus came out entirely: removed `ThisBuild / managedScalaInstance := false` and both hand-supplied `scala-tool` blocks, letting sbt auto-manage the Scala instance and resolve the matching 2.13.16 bridge itself. `jline` (a REPL runtime dep that lived only in scala-tool) moved into root's normal `libraryDependencies`. `reload; clean; compile` →

> **Dev:** We're gold

Docs were corrected to record that the fix was *dropping* the setting, not adding the bridge — the wrong first attempt is preserved here and in `feedback_sbt_managed_scala_bridge` so the next instance doesn't repeat it.

---

## Where it stands

- `draco.DomainBuilder` (`define`/`dictionary`/`validate`/`generate`) — first `src/mods` stand-in, public-API-only, tested over Draco/Base/Primes/Language.
- `src/mods/scala/draco` established as a root-compiled staging tier; `Generator` + `GeneratorCLI` relocated there.
- sbt on 1.12.9 with a standard (managed) Scala instance.
- Memory + CHANGELOG + README + build.sbt comments all current.

**Open / next:** `TransformBuilder` penciled as the sibling stand-in (Dev plug). `Codec`/`Generated` remain non-member, non-JSON core infra in `src/main` (left as-is). The broader "several functions" the Dev alluded to — beyond the domain-dictionary one — are still unnamed.
