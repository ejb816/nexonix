# Draco Dev Journal — Chapter 20

**Session date:** April 14, 2026
**Topic:** Reference Frames Increment A, the `draco-gen` CLI Tool, Generator Cross-Package Import Fix

---

## Session Start — Reorientation

> **Dev:** Review chapter 19 in draco-dev-journal to recall where we were heading next.

**Draco:** *(Reads chapter 19)* Where we left off: multi-source compilation fix proposed at the very end (the biggest single lever for the 17 GenerateAndCompileTest failures), with the next-session priorities being (1) drive 17→0 Generator emission failures, (2) complete TypeInstance hierarchy, (3) draco.formats.Formats domain, (4) begin reference-frame example domains.

> **Dev:** I want to resume with creating the type definitions for the reference frame examples, and generate source code from them.

The fourth priority. Increment A is the right starting move — small enough to surface the JSON-to-Scala pipeline issues without committing to the full design.

---

## Worktree Mishap

The session opened in worktree `stoic-saha` whose branch `claude/stoic-saha` descends from an ancient nexonix-only commit (`8c8172b`) — no draco code at all, just `org.nexonix.json.Value` and friends. The real draco work lives on `main` at `ecf3bf7`. Two branches with no common draco ancestry.

Compounding the surprise: chapter 19's substantial work (Holon, Transform[S,T], RuntimeCompiler, Primal→TypeInstance changes, GenerateAndCompileTest) is **uncommitted and staged** in main's working tree, not on any branch.

**Dev's choice:** option 1b — work directly in main's working tree. Reference-frame files would be additive, no conflict with the staged Chapter 19 changes. The empty `reference-frames` worktree I created off the wrong base was removed.

---

## Pattern Reconnaissance — DataModel as Template

The existing example domains (`src/test/{resources,scala}/domains/`) gave the canonical pattern:

```scala
trait DataModel extends DomainInstance         // super-domain
trait Alpha extends DataModel                  // sub-domain
```

JSON `derivation` mirrors the inheritance chain. Inline `TypeDefinition` in the Scala companion (Chapter 19's `Generator.loadType` sweep covered leaves only — domain roots like `DataModel`, `Primes`, `Base` still use inline). The hand-written `.scala` carries the full domain pattern: `domainInstance: DomainType = new Domain[T] { ... }` with `_elementTypeNames` populated.

For Increment A — five domain skeletons with no leaves yet — `_elementTypeNames` would simply be `Seq.empty`.

---

## Increment A — Files Land

Five JSON definitions under `src/test/resources/domains/<frame>/`:

| Frame | Derivation |
|---|---|
| `Cosmocentric` | `DomainInstance` (super) |
| `Egocentric` | `Cosmocentric` |
| `Geocentric` | `Cosmocentric` |
| `Heliocentric` | `Cosmocentric` |
| `Galactocentric` | `Cosmocentric` |

And five matching Scala companions under `src/test/scala/domains/<frame>/`, each emulating the full domain pattern (with empty element list).

Plus a `ReferenceFramesGenTest` with three assertions per frame (15 tests total): JSON parses, Generator output compiles, and… a stub for whether the generator output matches the hand-written file. The third was incomplete.

---

## The Pointed Question

> **Dev:** Does the new test verify that your "manually written code" is the same as the Generator would produce from the new definitions?

**Draco:** No. The test verifies the Generator produces **compilable** source, but it doesn't compare that source against the hand-written `.scala` files on disk. They could silently diverge.

Three ways to close the gap, ordered loosest-to-tightest: (1) normalized text comparison, (2) make the hand-written files ephemeral (delete them, generator output is the only source), (3) regenerate on every build via an sbt source generator. Option 2 or 3 is the right philosophical direction (matches Chapter 19's self-closure vision). Option 1 is the cheap intermediate — preserves hand-written files for IDE navigation but catches drift.

> **Dev:** add the normalized-comparison assertion to ReferenceFramesGenTest

The normalization: strip trailing whitespace per line, collapse blank-line runs, trim leading/trailing blanks. Indentation, token order, substantive content all preserved. On mismatch, the test prints both normalized bodies side-by-side with `!!` markers on differing lines.

I predicted the test would fail on first run because `isDomain(td)` is defined as `elementTypeNames.nonEmpty`, and our element-less skeletons would take the lean type-pattern branch in the Generator while my hand-written code carried the full domain scaffolding.

---

## What the Generator Actually Emits

Test ran. As predicted, all 5 comparisons failed. But the failure shape was more informative than I'd thought:

```scala
package domains.cosmocentric

import draco._
import domains._

trait Cosmocentric extends Extensible with DomainInstance

object Cosmocentric extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.Generator.loadType(TypeName ("Cosmocentric", _namePackage = Seq("domains", "cosmocentric")))
  lazy val typeInstance: Type[Cosmocentric] = Type[Cosmocentric] (typeDefinition)
}
```

Three Chapter 19 conventions baked in automatically: `with Extensible`, `extends App with TypeInstance` (companion-as-instance), `Generator.loadType` (no inline). My hand-written code was missing all three.

And worse — for the four sub-frames, the generated source failed to compile with `not found: type Cosmocentric`. The Generator emitted `import draco._` and `import domains._` but never `import domains.cosmocentric._`. The derivation reference was orphaned.

A real Generator bug, surfaced cleanly by the test.

---

## The Pivot — Generator as a Tool

> **Dev:** I have an idea. I'm going eventually replace Generator as a type with Generator as a domain with rules and actors. Before we do that, why don't we take the current Generator and make it a tool for you to use. I'm assuming you can modify your own tools. so you can fix any issues with your tool version and migrate those changes back to the current Generator type in src/main. What do you think?

**Draco:** I had to calibrate what "modify your own tools" means — I can't literally modify the MCP tool registry mid-session. What I *can* do is create a CLI binary I invoke via `Bash`. Functionally equivalent for the iteration workflow.

I proposed three pieces: a `GeneratorCLI.scala` main class, a fast-start fat JAR via sbt-assembly, an optional shell wrapper. I also pressure-tested the "scratch worktree" idea — for a single contributor working on a single file, direct editing on a feature branch beats the worktree ceremony.

> **Dev:** You created some Python code to do data extraction for the draco-dev-journal for updating the chapters, so I figured we could take the Generator source code and make an executable app for it and use it as you suggested through bash invocation of a binary executable that we created for you to use.

The Python tools as the mental model. Same shape: build the binary, invoke from Bash.

---

## Building `draco-gen`

`build.sbt` was already set up perfectly: sbt-assembly plugin installed, fat JAR target at `target/scala-2.13/draco-${version}.jar`, current `mainClass` is `draco.CLI`. No build configuration changes needed — sbt-assembly packages every class, so a new `main`-bearing object is enough. Different `main`s can be invoked via `java -cp <jar> <class>`.

**`src/main/scala/draco/GeneratorCLI.scala`** — four subcommands:

| Subcommand | Purpose |
|---|---|
| `generate <json>` | Print Scala emitted by `Generator.generate(td)` to stdout |
| `compile <json>` | Generate + `RuntimeCompiler.compile`; pass/fail with stderr error dump |
| `compile-multi <json>...` | Generate each + `RuntimeCompiler.compileMulti` as one unit |
| `inspect <json>` | Decode JSON to `TypeDefinition`, re-encode as pretty JSON |

Exit codes: 0 ok, 1 usage error, 2 JSON parse/decode error, 3 compile failure.

**`bin/draco-gen`** — thin bash wrapper that auto-resolves the latest `draco-*.jar` under `target/scala-2.13/` (no version string hardcoded, survives bumps).

User ran `sbt assembly` once. Smoke tests:

```
=== Cosmocentric ===
OK  Cosmocentric  (classes in /var/folders/.../draco-gen13787398917185297179)
=== Egocentric ===
FAIL  Egocentric
  not found: type Cosmocentric
[generated source dumped to stderr]
```

Exactly what the test had shown. Now actionable in <1 second instead of 60+.

---

## The Generator Import Bug — Diagnosis and Fix

`Generator.typeImports` builds two import categories:

- `packageHierarchyImports(namePackage)` — walks the type's own package parents, force-includes `import draco._`
- `externalImports(td)` — whitelist keyed by simple name → fixed package (JVM stdlib like `URI`, framework types like `Knowledge`)

Neither inspects the packages of types in `td.derivation`, `td.modules`, `td.superDomain`, `td.source`, `td.target`. For Cosmocentric it accidentally compiled because `DomainInstance` lives in `draco` (force-imported); for the sub-frames, `Cosmocentric` lives in `domains.cosmocentric` — neither parent-walked nor in the external whitelist.

The fix added a new `referencedPackageImports(td)` helper:

```scala
private def referencedPackageImports (td: TypeDefinition) : Seq[String] = {
  val ownInits: Set[Seq[String]] = td.typeName.namePackage.inits.toSet
  val covered: Set[Seq[String]] = ownInits + Seq("draco")
  val referenced: Seq[TypeName] =
    (td.derivation
      ++ td.modules
      ++ Seq(td.superDomain, td.source, td.target))
      .filter(tn => tn != null && tn.name.nonEmpty)
  referenced
    .map(_.namePackage)
    .filter(_.nonEmpty)
    .filterNot(covered.contains)
    .distinct
    .map(p => s"import ${p.mkString(".")}._")
}
```

Wired into `typeImports`, deduped against the package-hierarchy imports.

**Regression risk check (before applying):**
- `Primes.json` (derives `DomainInstance` in `draco`) — `draco` is in covered set → no new import → unchanged ✓
- `Numbers.json` (derives `Primes` in `draco.primes`) — own package matches → no new import → unchanged ✓
- `Holon.json` (derives `Primal` in `draco`) — covered ✓

User rebuilt. Egocentric compile now showed:

```
FAIL  Egocentric
  object cosmocentric is not a member of package domains
  not found: type Cosmocentric
```

The import was now emitted (`import domains.cosmocentric._` visible in the dumped source). But compilation still failed — one error deeper.

---

## The Compile-Multi Epiphany

The failure wasn't a Generator bug. The emitted source was correct. The problem was that `RuntimeCompiler.compile` runs the Scala compiler with the application's runtime classpath — the assembly fat JAR — which contains `src/main` classes only. The hand-written `Cosmocentric.class` lives in `src/test/scala/`, compiled to `target/scala-2.13/test-classes/`, which isn't in the JAR.

When the CLI runs from the assembly JAR and the generated Egocentric source says `import domains.cosmocentric._`, the import is syntactically right but the class doesn't exist on the runtime compiler's classpath.

This isn't a Generator issue. It's an isolation-of-compilation issue. `RuntimeCompiler.compileMulti` already exists for exactly this — chapter 19 introduced it for the TypeElement sealed-trait family. We needed to expose it via the CLI.

Added `compile-multi` subcommand. Smoke test on all 5 frames as one unit:

```
OK  [Cosmocentric, Egocentric, Geocentric, Heliocentric, Galactocentric]  (classes in /var/folders/.../draco-gen5786633770402776539)
```

Generator emission is correct *and* internally consistent across the family.

---

## Closing the Loop

Two things flowed naturally from the above:

**1. Refresh hand-written from generator output.** Option A from the earlier discussion (Generator as canonical, drop the speculative domain scaffolding). One-liner per frame:

```sh
bin/draco-gen generate src/test/resources/domains/$f/$Name.json > src/test/scala/domains/$f/$Name.scala
```

The `domainInstance: DomainType = new Domain[T] { ... }` blocks vanished. Skeletons became lean `trait + TypeInstance companion` — exactly what the JSON describes. The domain pattern will reappear in Increment B when leaves arrive and `isDomain(td)` flips to true.

**2. Reshape `ReferenceFramesGenTest`.** The original 15-test design had four sub-frame "Generator output compiles" assertions that were architecturally guaranteed to fail (single-source compilation can't see the sibling type) — noise pretending to be tests. Restructured to 12 tests:

| # | Test | Mechanism |
|---|---|---|
| 1–5 | `<Frame>: JSON parses` | One per frame |
| 6–10 | `<Frame>: Generator output matches hand-written (whitespace-normalized)` | One per frame; failure includes the exact `bin/draco-gen generate ... > ...` recipe to reconcile |
| 11 | `Cosmocentric: Generator output compiles standalone` | Single-source `RuntimeCompiler.compile` (Cosmocentric has no cross-frame deps) |
| 12 | `Reference frames: all 5 compile together as one unit (compileMulti)` | Single `RuntimeCompiler.compileMulti` over the family |

`Tests passed: 12 of 12 - 1 sec 464 ms`.

---

## Regression Check

> **Dev:** I'm going to run GenerateAndCompileTest in the IDE.

Before the Generator change: 31 passed, 17 failed, 48 total.

After the Generator change: **31 passed, 17 failed, 48 total.**

Same baseline. The 17 failing types are unchanged. `referencedPackageImports` is purely additive — it only affects types whose JSON references cross-package TypeNames, which none of the main draco types do (everything's in `draco.*` and derives within `draco.*`). The fix shipped without disturbing any committed `.scala`.

---

## Memory Consolidation

Two new memory files plus index updates:

- `reference_draco_gen_cli.md` — full CLI spec for future sessions (subcommands, when to reach for it, rebuild via `sbt assembly`)
- `project_reference_frames_increment_a.md` — Increment A status, layout, patterns established, increments remaining

`MEMORY.md` updates:
- Architecture date refreshed
- New `Tooling` section pointing at the CLI reference
- Corrected the "no inline TypeDefinitions remain" claim — only leaves were swept; domain roots and `src/test` examples still use inline
- Added the 2026-04-14 `referencedPackageImports` Generator change to Key Facts
- Marked task #5 (reference-frame rebuild) as Increment A done with B/C remaining

---

## Session Summary

### Code Changes

1. **`Generator.referencedPackageImports`** — new private helper; emits `import <pkg>._` for cross-package TypeNames in derivation/modules/superDomain/source/target. Wired into `typeImports`. Unblocks any sub-domain whose super-domain lives in a non-parent package.
2. **`draco.GeneratorCLI`** — main-bearing object with `generate`/`compile`/`compile-multi`/`inspect` subcommands. ~90 lines; lives in the assembly fat JAR.
3. **`bin/draco-gen`** — bash wrapper, version-agnostic JAR resolution.
4. **5 reference-frame JSON type definitions** — `Cosmocentric`/`Egocentric`/`Geocentric`/`Heliocentric`/`Galactocentric` under `src/test/resources/domains/<frame>/`. Cosmocentric derives from `DomainInstance`; the four frames derive from `Cosmocentric`.
5. **5 reference-frame Scala companions** — under `src/test/scala/domains/<frame>/`. Mirror generator output exactly (lean type pattern, not domain pattern, since skeletons have no elements).
6. **`domains.ReferenceFramesGenTest`** — 12 tests verifying JSON parse + generator/hand-written equivalence + standalone and family compilation. Failure messages include the exact CLI command to reconcile drift.

### Design Decisions

- **Generator as canonical source for example-domain Scala.** Hand-written `.scala` files mirror generator output; drift caught by the comparison test in `ReferenceFramesGenTest`. Recipe to reconcile printed in failure messages: `bin/draco-gen generate <json> > <scala>`.
- **CLI as iteration tool, not a separate Generator copy.** Original framing was "scratch worktree + migrate fixes back" — replaced with "single canonical Generator + Bash-invokable CLI access." No divergent copy to maintain. The CLI naturally evolves into the Generator-as-domain entry point in the future.
- **`compile-multi` is the right verification path for type families.** Single-source `RuntimeCompiler.compile` is inadequate for any type whose `extends` clause references a sibling generated/hand-written type that's not on the assembly JAR's classpath. Tests for related families should use `compileMulti`.
- **Element-less domain skeletons emit the type pattern, not the domain pattern.** `isDomain(td)` requires `elementTypeNames.nonEmpty`. Frames without leaves get `trait + TypeInstance companion`. Domain pattern reappears naturally when leaves arrive in Increment B.

### Next Session Priorities

1. **Increment B — per-frame leaves and Assembly types**:
   - Egocentric: Bearing, Reach → Percept
   - Geocentric: Position (renamed from Coordinate to avoid `draco.base.Coordinate` collision), Altitude, Heading → Fix
   - Heliocentric: Elements, Epoch → Ephemeris
   - Galactocentric: Parallax, ProperMotion, RadialVelocity → Trajectory
   - 12 leaves + 4 Assembly = 16 new JSON+Scala pairs. Each leaf is a `Primal`-derived type; Assembly aggregates the leaves. Comparison test will surface any further Generator wrinkles as JSON shapes get richer.
2. **Increment C — 12 transform domains** at `domains.<src>.<tgt>.<SrcTgt>` extending `Transform[S, T]`. Validates two-parameter generation and the peer matrix.
3. **Continue chipping at the 17 GenerateAndCompileTest failures** as `bin/draco-gen` makes single-type Generator iteration fast.
4. **Optional**: `brew install sbt` so Bash-side regression checks are self-serve, removing the sbt-shell handoff.
