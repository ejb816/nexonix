# Draco Dev Journal — Chapter 36

**Session date:** May 16–17, 2026
**Topic:** Built the scala-cli toolkit so the agent can run runtime queries against the draco jar without writing one-off tests. Six design reframings reshaped the approach mid-discussion: "discuss the REPL" → "no actually I imagined .sc scripts" → "but how does an external jar-only user run these?" → "you can't call sbt — does scala-cli sidestep that?" → "still seeing IntelliJ resolution errors" → "how much of this is IntelliJ-specific?" → and finally the bigger architectural pivot: introduce `src/mods/` as a third source tier alongside `main/` and `test/`, with the scripts migrating in as its first content. Toolkit lives at `src/mods/scala/scripts/` as part of an sbt `mods` subproject that depends on draco's root jar.

---

## The opening framing

After three issues closed in one session (#17/#24/#27, chapters 34 + 35), the dev opened a new thread:

> **Dev:** Let's discuss making the REPL work so you can work with the draco jar file as well as the source files in the project.

The agent's first read: literal REPL — interactive Scala session against the jar that the agent could drive over a pipe. Surveyed existing artifacts and found `draco/REPL.scala` is just an empty placeholder trait (`trait REPL[L]`), the assembled jar exists at `target/scala-2.13/draco-2.0.0-alpha.2.jar`, and the agent's Bash tool gives a fresh JVM per call — making a persistent REPL session impractical without fragile fifo plumbing.

The agent's first recommendation was a one-shot `bin/draco-eval "<scala expr>"` wrapper around `scala -e`. Cheap, fresh-JVM per call, matched the agent's Bash model.

Then the first reframing:

> **Dev:** I was imagining you creating multiple Scala script (.sc) files as part of set of tools for working as draco user.

Different shape entirely. The user wasn't asking for an interactive REPL — they were asking for a *toolkit*: an accreting library of `.sc` scripts that the agent (and external users) could invoke, with each script answering a focused question. That fits much better with the agent's tool model: each script is a durable artifact, called by name, parameterized by args. The collection grows as needs surface.

Sketch produced: `scripts/` directory at repo root, `bin/draco-sc <name> [args]` wrapper, two starter scripts for proof of concept.

---

## The second reframing — external users

> **Dev:** How does this affect a user who only has the jar file as a dependency in their other project? How would they run the scripts as a draco user?

This pushed on the "external user" framing implicit in the project redirect (chapter 34). If the goal is *usability of draco as a dependency*, the scripts can't be wholly internal to the repo. They need a viable invocation path for someone who's just `libraryDependencies += "ejb816" %% "draco" % "X.Y.Z"`-ed the jar.

The answer the agent settled on: scala-cli is the medium. A script with a `//> using dep "ejb816::draco:X.Y.Z"` directive at the top is a self-contained, runnable, customizable artifact. External users `brew install scala-cli`, copy a script, run it. They can also *modify* a script — which is the killer feature versus CLI subcommands. An external author building their own domain can take `inspect-type.sc`, adjust to inspect their custom types, re-run.

Pre-Maven-Central caveat: until [#22](https://github.com/ejb816/nexonix/issues/22) lands, external users can't do this. So the design needs to be forward-compatible — the in-repo scripts use `using jar` (supplied by `bin/draco-sc --jar` from the local build), and an external user post-publication swaps to `using dep`. Same script source, both audiences.

---

## The third reframing — the sbt constraint

> **Dev:** You're currently not able to call sbt directly. Do you expect scala-cli to be different? I would prefer it if we can get it to work.

A precise reframing of *why* the convention exists. The "don't call sbt" rule isn't a technical block — it's protecting the user's interactive sbt session and zinc/IntelliJ build state from agent interference. scala-cli is a fundamentally different beast: one-shot, no shared state, just consumes the assembled jar. The agent calling `scala-cli` doesn't touch sbt at all.

Once that was clear, the install bar was the only friction. `scala-cli` wasn't on PATH (neither was `scala` or `cs` — the dev runs everything through sbt's internally-managed Scala). brew was available though.

> **Dev:** Yes [install scala-cli]

`brew install scala-cli` landed `scala-cli 1.14.0` at `/opt/homebrew/bin/scala-cli`. Default Scala version is 3.8.3 but draco is 2.13 — scripts need a `//> using scala 2.13` directive.

---

## First smoke test — the stale jar

Quick verification that scala-cli + draco jar + `import draco._` works:

```text
//> using scala 2.13
//> using jar "/Users/edwardburke/IdeaProjects/nexonix/target/scala-2.13/draco-2.0.0-alpha.2.jar"
import draco._
val td = Generator.loadType(TypeName("Primal", _namePackage = Seq("draco")))
println(s"  derivation count = ${td.dracoAspect.derivation.size}")
```

Compile error: `value dracoAspect is not a member of draco.TypeDefinition`. The jar was assembled before Stage 2d (chapter 27, `td.draco` → `td.dracoAspect` rename, 2026-05-07). The pipeline works; the API mismatch is the diagnostic. The agent simplified the smoke to use only `td.typeName.name` (which exists in both shapes), confirmed end-to-end execution, then noted the stale jar in the user-facing summary and proceeded to author scripts targeting the *current* source — knowing the dev would refresh the jar before final smoke.

This pitfall is worth remembering: **scripts compile against the jar, not the source tree.** A source change followed by a `bin/draco-sc` call without `sbt assembly` will produce a misleading compile error.

---

## Authoring the toolkit

Four artifacts:

1. **`bin/draco-sc`** — thin bash wrapper, ~30 lines. Finds the latest `target/scala-2.13/draco-*.jar`, runs `scala-cli run scripts/<name>.sc --jar <jar> -- <args>`. Validates arg count, lists available scripts on lookup failure. Mirrors `bin/draco-gen`'s pattern.

2. **`scripts/inspect-type.sc`** — load by `TypeName` (production API), pretty-print aspect-by-aspect. Differs from `bin/draco-gen inspect` (which takes a path and dumps raw JSON) by exercising the resource-loaded production path and grouping output. Initial version used `e.getClass.getSimpleName` for element kind, which returned empty for anonymous-subclass `Fixed`/`Mutable`/etc. instances — fixed in the polish pass with a pattern match on the TypeElement subtypes.

3. **`scripts/derivation-chain.sc`** — walk `dracoAspect.derivation` transitively with cycle protection, indented tree output. Something `bin/draco-gen` doesn't do at all.

4. **`scripts/README.md`** — convention, catalog, authoring rules. Documents the `using scala 2.13` directive, the `args` array, exit codes (0/1/2), the external-user `using dep` evolution path.

The agent considered a second script `regenerate.sc` (load → Generator.generate → print) but ditched it as near-duplicative of `bin/draco-gen generate`. `derivation-chain.sc` won the slot because it composes APIs in a way the CLI doesn't.

---

## Smoke green after the jar refresh

> **Dev:** [pasted sbt assembly output, jar hash b2a861e8...]

```
$ bin/draco-sc inspect-type Primal draco
=== TypeName ===
  name             = Primal
  namePackage      = draco
  typeParameters   = T

=== DracoAspect ===
  derivation       = draco.DracoType
  elements         = 1 element(s)
    -  value: T          ← empty kind label; pattern-match fix landed next
  factory          = (none)
  ...
```

```
$ bin/draco-sc derivation-chain Meters draco base
Derivation chain for draco.base.Meters:

■ draco.base.Meters
  └─ draco.base.Distance[Double]
    └─ draco.base.Cardinal[T]
      └─ draco.base.Unit  [root: no further derivation]
      └─ draco.Primal[T]
        └─ draco.DracoType  [root: no further derivation]
```

The derivation-chain output is exactly what #17's purge work + Cardinal's `extends Unit with Primal[T]` should produce: a branching tree where `Cardinal[T]` bifurcates into the `Unit` and `Primal[T]` ancestors, and `Primal[T]` then chains to `DracoType`. The tool surfaces the post-Stage-2d cascade visually in a way a code read doesn't.

(One minor display note: `Cardinal[T]` shows `[T]` not `[Double]` — the walk preserves the declared derivation TypeName, not the substituted instantiation. That's correct behavior, surfacing the literal JSON authoring; a substituted-type walker would be a separate script.)

After the pattern-match polish, `inspect-type` element lines read cleanly: `- Fixed value: T`.

---

## Lessons

### Listen for the reframings

Four explicit reframings in this discussion, each one a useful pivot:

1. *"Multiple .sc scripts"* — not a REPL.
2. *"How does the external user run them?"* — not just an internal tool.
3. *"You can't call sbt — is scala-cli different?"* — the constraint is about convention, not capability; scala-cli sidesteps it.
4. (Implicit) *"`regenerate.sc` is just a renamed `bin/draco-gen generate`"* — scripts should add value beyond the existing CLI.

The pattern: when the dev's question reframes the work, the agent's instinct should be to *redesign*, not to *defend the previous sketch*. Chapter 33 had the same lesson ("scope creep can be the right answer"). This chapter is the same lesson at a different scale.

### Scripts as a different shape than CLI subcommands

`bin/draco-gen` already had `inspect`, `generate`, `compile`, `compile-multi`. Adding more CLI subcommands for the agent's runtime queries would have been the path of least resistance. But it would have missed two things the script form does naturally:

1. **External-author customization** — they copy + modify, vs being locked into our parameterization.
2. **Self-declaring dependencies via `using dep`** — script is the artifact, scala-cli handles resolution. CLI subcommands require the user to have already wired the jar into their classpath.

The first is the redirect's whole goal (chapter 34). The second is the on-ramp to it.

### The stale-jar pitfall is worth a memory note

The agent's first smoke test produced a misleading-looking error (`dracoAspect not a member`) because the jar predated the rename. The right diagnosis ("jar is stale, source has moved past it") wasn't obvious from the error alone — could easily have been misread as "scripts are wrong." Saved as `reference_draco_sc_toolkit.md` so future-me has the diagnostic for next time.

---

## IDE resolution — the fifth reframing

The next day, IDE-side friction surfaced:

> **Dev:** ~/IdeaProjects/nexonix/scripts/inspect-type.sc
> Error:(14, 5) Cannot resolve symbol args
> [… plus 6 more cannot-resolve errors across the two scripts …]

Pure IntelliJ-inspection errors, not runtime failures — the scripts still ran fine through `bin/draco-sc`. But the editor noise was real and would only grow as the toolkit grew. The agent's diagnosis: IntelliJ's Scala plugin doesn't natively understand the `.sc` *script form* — it doesn't know `args` is implicitly bound, doesn't honor `using` directives, and won't resolve `import draco._` without classpath context.

Two repair options surfaced:
- **BSP integration** — wire IntelliJ to scala-cli's auto-generated `.bsp/scala-cli.json` for full code intelligence
- **Convert `.sc` → `.scala` with explicit `object … extends App`** — bypasses `.sc` script semantics; IntelliJ understands `extends App` natively

> **Dev:** Option A [BSP integration]

The agent also amended `bin/draco-sc` to keep the BSP config in sync with the chosen jar (idempotent `setup-ide` check), then walked the dev through IntelliJ's BSP import procedure based on scala-cli's official docs (`File → New → Project from Existing Sources → BSP`).

> **Dev:** I'm still getting "Cannot resolve symbol: args" in the .sc file editor.

BSP fixed the *classpath* part (import resolution) but didn't teach IntelliJ about scala-cli's `.sc` wrapper-class convention where `args` is a synthetic member of the generated wrapper. The agent had been hoping BSP would carry that through; it doesn't.

The pragmatic pivot: do the conversion anyway.

> **Dev:** Yes.

Conversion executed:
- `scripts/inspect-type.sc` → `scripts/inspect-type.scala` wrapping the body in `object InspectType { def main(args: Array[String]): Unit = { … } }`
- `scripts/derivation-chain.sc` → `scripts/derivation-chain.scala` same shape with `object DerivationChain`
- `bin/draco-sc` updated to find `.scala` extension
- README catalog + authoring conventions updated to reflect the new form (kebab-case filename, PascalCase object name, explicit `def main` not top-level code, `extends App` skipped because of the DelayedInit deprecation warning surfaced by scala-cli)
- BSP config and `.scala-build/` wiped + regenerated since filenames changed

Smoke test against the new form, both scripts:

```
$ bin/draco-sc derivation-chain Meters draco base
Derivation chain for draco.base.Meters:

■ draco.base.Meters
  └─ draco.base.Distance[Double]
    └─ draco.base.Cardinal[T]
      └─ draco.base.Unit  [root: no further derivation]
      └─ draco.Primal[T]
        └─ draco.DracoType  [root: no further derivation]
```

Identical to the `.sc` form's output. The conversion is functionally a wash; the win is purely IDE friendliness.

### Side-effect cleanup

IntelliJ's BSP import created `scripts/.idea/` files which got auto-staged when the dev imported the project. After the conversion + BSP-config regeneration, those `.idea` entries appeared as "AD" (added in index, deleted in working tree) in git status. The `.gitignore` already had `/.idea/` but root-only — same pattern as the earlier `.bsp/` / `.scala-build/` fix, so `.idea/` got promoted to match anywhere. `git reset HEAD scripts/.idea/` removed the stale staged entries.

## Lessons

### Listen for the reframings (continued from earlier)

Adding the fifth reframing to the list: *"IDE integration is still broken even after BSP."* The agent had a strong prior that BSP would solve the issue (scala-cli's own docs frame it as the canonical path). When it didn't, the right move was to abandon the `.sc` form rather than keep tunneling on BSP variants. The `.scala` form was always available; the agent had reached for `.sc` because it was the more idiomatic scala-cli pattern, but "idiomatic" lost to "IntelliJ-friendly" once the test result came in.

### IDE-friendly forms cost almost nothing

Converting `.sc` (top-level code, implicit `args`) to `.scala` with `object Name { def main(args: Array[String]): Unit = { … } }` added one wrapper line per script. The cost is negligible. The benefit is that the editor can fully resolve the code without any BSP gymnastics. For a toolkit that's likely to grow, "the IDE understands it natively" is a meaningful long-term property.

### Gitignore patterns: leading-slash vs anchored

This session hit the leading-slash trap twice — first with `.bsp/` / `.scala-build/` from scala-cli's project structure, then with `.idea/` from IntelliJ's BSP import inside `scripts/`. The fix in both cases was the same: drop the leading slash so the pattern matches anywhere, not just the repo root. Worth remembering as a default: ide/build cache directories should generally be matched globally (no leading slash), since they can appear under any subproject or sub-tooling integration.

## The "IntelliJ-specific" audit

Before verifying the IntelliJ side, the dev asked a sharp question:

> **Dev:** Before I verify these changes, I have to ask: How much of this would be necessary if I wasn't using an IDE? Or IntelliJ specifically?

The agent answered honestly: roughly 20–25% of the diff was IntelliJ accommodation. The `.sc` → `.scala` conversion in particular was IntelliJ-specific — without IntelliJ, `.sc` would have been the more idiomatic scala-cli form (less boilerplate, top-level code, implicit `args`). `setup-ide` refresh logic in the wrapper, `.idea/` gitignore promotion, and the BSP attempt were all IntelliJ artifacts.

The breakdown was a reset point — but the dev liked the IntelliJ-friendly form anyway:

> **Dev:** No, I like what you've done. Instead, let's speculate about creating a third directory under nexonix/src/ called mods.

This was the sixth reframing — and the biggest. Not "fix the toolkit further" but "rethink where the toolkit lives."

## The `src/mods/` proposal

The dev's design:

- Third tier alongside `src/main/` (draco core) and `src/test/` (test fixtures)
- Direct dependency on `src/main`'s artifact (jar)
- No new third-party dependencies of its own
- `mods/resources/` for TypeDefinitions (analog of `main/resources/`, `test/resources/`)
- Per-language directories under `mods/` for Generator emission targets (right now just `scala/`; eventually `haskell/`, `lean/` as Generator[L] grows)
- Sources treated as either scala-cli scripts or compiled-and-executed code

The agent's first read was substantial — this maps the *Generator-as-domain-with-language-type-parameter* vision (issue [#11](https://github.com/ejb816/nexonix/issues/11)) onto a physical directory structure. Generator[L] without mods/ has no natural place to write its multi-language output. Generator[L] with mods/ writes to `mods/<L>/`.

The agent proposed a "begin without over-investing" 30-minute minimal scope, then asked four questions before committing to an approach. The dev's answers were terse but each redirected the design:

1. **Start mods/resources empty** — matched the lean recommendation
2. **Scripts collapse into mods (no separation)** — "any user can be human or AI agent" — the agent had drafted a separate scripts/ vs mods/ distinction; the dev unified them as one tier
3. **"mods" name comes from RPG modding tooling (Vortex)** — naming confirmed, not negotiable
4. **mods/ is early prep for Generator[L]** — confirmed the agent's read

Answer #2 was the meaningful delta. Combined with #4, it transformed the minimal scope from "30-min placeholder" to "45–90 min real migration plus sbt subproject."

## The compiled-source-root question

The agent surfaced one design choice before executing: should `src/mods/scala/` be a compiled sbt source root, or a content-only directory?

- **Compiled source root**: scripts need `package` declarations, become both scala-cli runnable AND JVM-runnable. Mods becomes a real artifact (`draco-mods-X.Y.Z.jar`). Symmetric with how Generator output will eventually live there.
- **Content-only**: simpler today, less symmetric with future Generator emission.

The agent recommended compiled source root; the dev agreed:

> **Dev:** Let's go with compiled-source-root + a bit more ceremony per script

The "bit more ceremony" is one `package scripts` line per script. Cheap.

## Migration execution

Eleven changes:

1. Created `src/mods/{resources,scala/scripts}/` empty directories
2. Moved `scripts/inspect-type.scala` → `src/mods/scala/scripts/inspect-type.scala` with `package scripts` added
3. Same for `scripts/derivation-chain.scala`
4. Rewrote `bin/draco-sc` to find scripts at the new location AND **drop the `setup-ide` BSP-refresh block** — no longer needed because sbt's `mods` subproject gives IntelliJ a real Scala source root, no BSP gymnastics required
5. Wrote new `src/mods/README.md` covering the whole mods/ tier (replaces the script-specific `scripts/README.md`)
6. Added `mods` subproject to `build.sbt`:

```text
lazy val mods = (project in file("src/mods"))
  .dependsOn(root)
  .settings(
    name := "draco-mods",
    Compile / scalaSource      := baseDirectory.value / "scala",
    Compile / resourceDirectory := baseDirectory.value / "resources",
    scalacOptions += "-Wconf:msg=will not have an entry point on the JVM:s",
    publish / skip := true
  )
```

7. `.gitignore`: `/target/` → `target/` to match `src/mods/target/` from the subproject build; dropped the now-redundant `/project/target/` entry
8. Deleted the old `scripts/` directory entirely (`git rm` the three files, `rm -rf` the dir)
9. Updated memory `reference_draco_sc_toolkit.md` to reflect new paths and the no-BSP-needed insight
10. Smoke-tested both scripts from the new location

`bin/draco-sc inspect-type Primal draco` → identical output to pre-migration. `bin/draco-sc derivation-chain Meters draco base` → identical tree (Meters → Distance[Double] → Cardinal[T] branching into Unit and Primal[T] → DracoType).

11. Extended this journal entry with the `src/mods/` arc (you are here).

## What this commits to architecturally

`src/mods/` formalizes things that were previously implicit:

- **Generator output target is now structural, not ad-hoc.** Today's `src/generated/` (gitignored, scattered) becomes `src/mods/<lang>/<package>/` (versioned, organized) when Generator[L] starts emitting there.
- **"Draco-on-top-of-draco" content has a home.** The reference frames could eventually migrate from `src/test/` to `src/mods/` (per the chapter-34 redirect) — they're more "example extensions" than "test fixtures."
- **The script/library distinction collapses.** Same `.scala` file can be a scala-cli script (one-shot dev iteration) or a JVM-runnable class (compiled into `draco-mods.jar`). Generator emission will produce both shapes from TypeDefinitions.
- **External-user simulation gets sharper.** The "no new third-party dependencies" constraint on mods/ means it's a strict dogfood: anything mods/ wants beyond what draco provides is a signal that draco core is missing something.

## Lessons

### The "what's IDE-specific?" audit was the unlock

The agent had been accumulating IntelliJ accommodations (setup-ide refresh, BSP attempts, .sc → .scala) without stepping back. The dev's audit question was the structural reset that surfaced the bigger move: instead of optimizing the toolkit's IDE story, restructure where the toolkit lives so IDE concerns disappear. Mods-as-sbt-subproject means IntelliJ already understands it; no special handling needed.

When you're accreting workarounds for a specific tool, the right move is often to change the architecture so the tool's preferences become irrelevant, not to keep adding workarounds.

### Subproject means no BSP

The earlier chapter's BSP setup was a workaround for `scripts/` not being a recognized Scala source root. Once `src/mods/scala/` is a sbt subproject's source root, IntelliJ gets full code intel through sbt's normal project import — no per-directory `scala-cli setup-ide`, no `.bsp/scala-cli.json` to maintain. The `bin/draco-sc` wrapper got simpler as a result.

This is the second time in this session that a structural change made earlier tactical effort obsolete (the first was the `.sc` → `.scala` move replacing the BSP attempt). Worth remembering: tactical fixes often persist past their expiration date; structural moves can retire them wholesale.

### Six reframings is a lot

This chapter's discussion phase had six explicit reframings before the work converged on its final shape. That's not a sign of bad design — it's a sign that the right design was discovered iteratively, with each reframing tightening the conception. The agent's job was to listen and re-plan each time, not to defend a previous sketch.

## Status at close

- **Migrated to `src/mods/`** (uncommitted, pending dev's IDE commit and sbt verification):
  - `bin/draco-sc` (simplified — no more setup-ide block)
  - `src/mods/scala/scripts/inspect-type.scala`, `src/mods/scala/scripts/derivation-chain.scala`, `src/mods/README.md`
  - `build.sbt` (added `mods` subproject)
  - `.gitignore` (`target/` global, `/project/target/` dropped)
  - `scripts/` directory deleted
- **System install**: `scala-cli 1.14.0` via brew (persistent, one-time, from earlier in this chapter)
- **Memory updated**: `reference_draco_sc_toolkit.md` reflects new location + no-BSP-needed insight
- **Smoke**: both scripts work end-to-end from `src/mods/scala/scripts/`
- **Pending sbt verification**: dev needs to `sbt reload` then `sbt mods/compile` to confirm the subproject compiles. If green, the dual-role pattern (scala-cli script AND compilable class) is proven.
- **IDE resolution**: should "just work" via sbt's project re-import — no BSP setup needed
- **`priority-next`**: unclaimed; original redirect candidates (`#11`, `#18`, `#9`) still queued

Natural commit message: `Introduce src/mods/ tier + migrate scripts under sbt mods subproject`. Still infrastructure, no issue to close.

## Next questions (deferred)

1. Should mods/ eventually publish its own jar (`draco-mods-X.Y.Z`)? Today `publish / skip := true` — when external users want it, flip that.
2. The reference-frames migration from `src/test/` to `src/mods/` — when is the right moment? Probably when someone wants to make a substantive change to them.
3. Generator[L] retargeting to emit into `mods/<lang>/` — first concrete consumer of the mods/ structure. Whoever picks up [#11](https://github.com/ejb816/nexonix/issues/11) inherits this.

---

## Next-script candidates (deferred)

For when a real question wants them:

- `aspect-summary.sc <name>` — like inspect-type but tighter, single-line summary per aspect
- `domain-tree.sc <DomainName>` — walk a domain's `elementTypeNames` recursively
- `roundtrip.sc <resource-path>` — load → encode → decode → compare (catches codec asymmetries)
- `find-callers.sc <method-name>` — bytecode-level call graph against the jar (probably overkill)
- `validate-aspect.sc <resource-path>` — check JSON parses to a non-Null TypeDefinition + warn on common shape errors

The discipline (from `scripts/README.md`): only add when the same question has come up twice. No premature library.
