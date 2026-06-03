# src/mods/ — the third tier

The `mods/` source tier sits alongside `src/main/` (draco core) and `src/test/`
(core test fixtures). It contains content that *uses* draco-as-a-dependency —
the kind of code an external author would write on top of the published jar.

**Constraints:**
- Depends on `src/main`'s artifact (via sbt's `dependsOn(root)`).
- Introduces **no new third-party dependencies of its own**. Anything mods/ needs
  beyond what draco itself provides is a signal that draco core is incomplete.
- Inspired by RPG modding conventions (Vortex etc.) — `mods/` is "what users
  build on top of the base game."

## Layout

```
src/mods/
├── resources/        TypeDefinitions in JSON/YAML (empty initially)
└── scala/
    ├── draco/        Hand-written Scala in package `draco`, compiled INTO root
    └── scripts/      Toolkit scripts (the `mods` subproject) — see catalog below
```

### Two compilation tracks

`src/mods/scala/` holds two kinds of content with **different build wiring**:

- **`scala/scripts/`** is the `mods` sbt subproject (`dependsOn(root)`). It compiles
  *against* the published draco jar and consumes it like an external author would.
  This is where the scala-cli toolkit scripts live (catalog below).

- **`scala/draco/`** is compiled **into `root`** via `root`'s
  `Compile / unmanagedSourceDirectories` (see build.sbt). Files here are in
  `package draco` and ship in the draco jar, but are deliberately **outside the
  JSON self-describing type system** — they have no `.json` twin and are not walked
  by `DracoGenTest`. Two reasons a file lives here:
    1. **Stand-ins** for under-development core/dreams/orion features, given to
       early-access users now (e.g. `DomainBuilder`). Promotes to `src/main` later.
    2. **Permanent hand-written engine code** that is core but not a self-describing
       type — currently `Generator` (the imperative load/generate/compile engine).
       It is *not* a Draco domain member; the typed self-describing generator will
       arrive separately as `draco.generator.Generator`
       ([#11](https://github.com/ejb816/nexonix/issues/11)).

  Because `scala/draco/` shares the `draco` package with `src/main/scala/draco/`
  *in the same root project*, a duplicate FQN is a hard compile error — conflict
  detection comes for free, and the invariant **"a declared Draco domain member is
  JSON-backed"** is enforceable (it's exactly what `DomainBuilder.validate` checks).

Future language directories (`mods/haskell/`, `mods/lean/`) will land alongside
`scala/` when `Generator[L]` (issue [#11](https://github.com/ejb816/nexonix/issues/11))
acquires those targets.

## Scripts catalog

Runnable [scala-cli](https://scala-cli.virtuslab.org/) `.scala` files under
`scala/scripts/`. Each composes draco's public API to answer a focused runtime
question — useful for both human and AI-agent draco users.

| Script | Purpose |
| --- | --- |
| [`inspect-type.scala`](scala/scripts/inspect-type.scala) | Load a `TypeDefinition` by `TypeName` and pretty-print its aspect-by-aspect shape. Exercises `Generator.loadType` (production API), groups output by aspect. Differs from `bin/draco-gen inspect` which takes a filesystem path and dumps raw JSON. |
| [`derivation-chain.scala`](scala/scripts/derivation-chain.scala) | Walk `dracoAspect.derivation` transitively (cycle-protected) and print each ancestor. Useful for "does X transitively extend DracoType?" and full-picture inheritance questions. |
| [`list-domain.scala`](scala/scripts/list-domain.scala) | Load a domain's `TypeDefinition` and summarize each member named in `elementTypeNames` — `extends X` for types, var/action counts for rules, message/signal counts for actors. One-screen mental model of a domain's surface. Reports stale entries (named in JSON but no resource on disk) as `[MISSING]` and exits 1, complementing `bin/draco-gen verify`. |
| [`list-domains.scala`](scala/scripts/list-domains.scala) | Discovery counterpart to `list-domain`. Probes the canonical first-party domains (Draco, Base, Primes, Language) — or a passed dotted-FQN set — and prints element count + composition (N types, M rules, K actors) for each. Answers the first-question-asked by a new draco explorer: *what domains live here?* |
| [`who-extends.scala`](scala/scripts/who-extends.scala) | Inverse of `derivation-chain`: given a target type, find every type whose `dracoAspect.derivation` chain transitively reaches it across the canonical scan set. Cycle-protected. Useful for "what concrete types implement this interface?" and similar reverse-lookup questions. |
| [`diff-type.scala`](scala/scripts/diff-type.scala) | Compare `Generator.generate(td)` against the hand-written `.scala` for a single type. Same whitespace-normalized side-by-side diff that `DracoGenTest` runs, but on demand. Use when iterating on a Generator change or JSON edit and you want fast feedback without running the full test suite. |

### Running a script

```
bin/draco-sc <script-name> [args...]
```

Looks up `src/mods/scala/scripts/<name>.scala`, runs it via `scala-cli` with the
local `target/scala-2.13/draco-*.jar` on the classpath. Rebuild the jar via
`sbt assembly` when draco source changes — scripts compile against whatever the
jar exposes.

### Prerequisite

```
brew install scala-cli
```

(or whatever your platform equivalent is).

### IDE code intelligence

Through sbt's `mods` subproject, IntelliJ (or any sbt-aware IDE) already treats
`src/mods/scala/` as a Scala source root depending on `main`. No separate BSP
setup needed — `import draco._` resolves natively, `args` is plain
`Array[String]` from the explicit `def main`, no inspection noise.

## Authoring conventions

- **First line is the scala-cli version directive**: `//> using scala 2.13`.
  scala-cli reads this for the runtime invocation; sbt ignores it (treats as a
  comment) when compiling the subproject.
- **Package declaration**: `package scripts` (or deeper subpackage if needed).
  The package is what makes the subproject compilable and gives IntelliJ a real
  source root to anchor to.
- **Explicit `object Name { def main(args: Array[String]): Unit = { … } }`**:
  not top-level code. Object name PascalCase; filename kebab-case to match the
  shell-invocation pattern (`bin/draco-sc inspect-type`).
- **Exit codes**: `0` ok, `1` data-not-found, `2` usage error.
- **No file writes**: scripts should be read-only / print-only. Anything that
  modifies repo state belongs in `bin/draco-gen` or a dedicated tool, not here.
- **Header comment**: one paragraph explaining what the script answers, plus a
  usage example. Doubles as the catalog entry above.

## When to add a script

When you've answered the same draco-runtime question more than once and would
rather have a reusable artifact than re-type the expression. Each script should
add value beyond `bin/draco-gen`'s existing subcommands — either by composing
multiple APIs, exercising the `TypeName`-based loading production code uses, or
surfacing relationships that aren't visible in a single `TypeDefinition` dump.

## External-user pattern (post-publication)

Once draco is on Maven Central ([#22](https://github.com/ejb816/nexonix/issues/22)),
external users will be able to copy a script, replace the `using jar` with a
`using dep` directive, and run it against their own project's draco:

```text
//> using scala 2.13
//> using dep "ejb816::draco:2.0.0-alpha.X"
package scripts
import draco._
// ... script body unchanged ...
```

The internal `bin/draco-sc` wrapper supplies `--jar` from the local build to
bypass dep resolution for in-repo development. Same script source, both audiences.
