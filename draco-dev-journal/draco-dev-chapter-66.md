# Draco Dev Journal — Chapter 66

**Session date:** July 21–22, 2026
**Topic:** The parser discussion inverts — Dev freezes drake and pivots to converting the Generator from a tool to a domain → `draco.drake.Drake` and `draco.generator.Generator[L]` land as capability domains, gated by Dev's conceptual correction that `loadType` never belonged to the Generator (born as `draco.TypeLoader`, ending a main→mods layering inversion) → `generate-multi`, a latent leading-newline defect fixed, byte-exact emitter output, a surgical two-tree `loadType` sweep → `draco.Source` as the language-neutral supertype, the `<language>Source → <language>Target` rename (and Dev's correction that `draco.Source` itself stays) → the "CO in DRACO" dialogue: conditions/actions, semantics as substitution rules checked by the target's type system, definition accrual, meaning-as-relations, entanglement/decoherence — then Dev's refocus directive → Draco self-validation rules (`Problem` + `Completeness`) as the prerequisite to Generator rules → Dev's challenge to the "Rule" file-name suffix finishes #40 at the class-name level → DRACO.md synced, 299/299. Transcript.

---

**[Dev]** Let's discuss the options available for continuing work towards a drake parser, starting with issue #46

**[Claude]** I'll pull up issue #46 and the surrounding context. *(Viewed #46, the issue list, #44/#42/#43, the drake corpus census, `DrakeGenTest`'s exact-match walk and `authoredAhead` exclusions; launched a background survey of the drake emitter + test state.)* Core corpus is 1:1 — `draco/` 45/45, primes 6/6, format 2/2. The mods domains are partial (3/9, actors only). That's a strong base for a parser. I have enough to frame this. One finding reshapes the question.

**The drake grammar is already settled.** `DrakeGenTest` exact-matches `Generator.drake` output against the hand-authored `.drake` for every discovered type under `draco/`, with only three exclusions — and all three are *presence-model* deferrals (Phase 2b), not grammar holes. The mods actors including terrestrial `Output` now exact-match too.

That means **#46 is not on the parser's critical path**, in either part:

- **Part 2 (anonymous-`new` Scala projection)** runs the *opposite* direction — JSON→Scala. It touches no drake syntax. Real work, but orthogonal to the parser.
- **Part 3 (Value full expression tree)** actively *expands* the grammar: lambdas, `->`, `if`, nested `()` unfolding. Doing it first means writing a parser against a moving target.

So the sequencing question inverts. My recommendation: **start the parser now (#44 increment-1) against today's frozen surface, and defer #46 part 3 until it exists.** The reasoning is the validation argument already in your own practice — a round-trip test is the second opinionated projection that catches mis-modeling a one-directional emitter preserves. Right now `DrakeGenTest` proves the emitter is *self-consistent with what you hand-authored*; it cannot prove drake is *invertible*. `parse(Generator.drake(td)) == td` over the corpus is a much sharper instrument, and you can only build it cheaply while the grammar holds still. Then part 3 lands *afterwards*, and the day you author lambda/`if` syntax it gets emitter + parser + round-trip gating simultaneously — instead of being ratified by an emitter that can only agree with itself.

Two side notes while you decide: **#42 is closeable** — its sole remaining blocker was terrestrial `Output`'s multi-line tail, and `Output.drake` now expresses it fully and passes the exact-match walk (the "pending" comment at `DrakeGenTest.scala:110` is stale). **#43 (codec aspect) is a genuine grammar hole** — `drake()` still `sys.error`s on a populated `codecAspect`. One open question I'd want your call on before drafting anything: should the parser be hand-written Scala in `src/mods` (mirroring `Generator.drake`'s own status as permanent imperative engine), or does the grammar want to be a draco definition from the start — the self-hosting move toward #11/#14?

*(The background survey returned.)* Survey's back, and it sharpens the recommendation considerably. First a correction to my own numbers: the draco corpus is **67/67** covered, yielding **64 exact-match tests** (not 50 of 53 — I'd counted only the top-level directory). The important finding is that **drake has two layers with very different maturity**, and `drake.dlt` says so itself:

**The declaration layer is settled.** `type`/`from`, `fix`/`var`/`dyn`/`par`/`loc`/`mon`, `domain`, the aspect heads — all 64 exact-matched, and the BRACKETS rule (leaf blocks bare, nesting blocks require `[ ]`) is already implemented in `drakeSection`. `drake.dlt` notes bracket omission is a *token-local detectable error* — exactly the property that makes a recursive-descent parser cheap and its errors good.

**The expression layer is explicitly unfinished.** `drake.dlt`'s EXPRESSIONS section marks the surface grammar and fixity-resolution pass as "(future)", and records that current rendering is **flat and unparenthesized**, default `infixl 9`. That's a genuine round-trip hazard: a nested mixed-operator tree rendered flat can re-parse into a different shape than it started. The emitter is lossy there *today*.

So the ordering I'd propose: **parser inc-1 (declarations only, expression values as opaque line-tails) → parser inc-2 (expressions + fixity) with #46 part 3 folded in → #46 part 2 whenever, independently.** Also worth knowing: `drake.dlt:253–282` keeps an explicit DIVERGENCES ledger — effectively the parser's known-gaps list already written down. The hand-written-vs-definition question still stands and is the one I'd actually want your call on.

---

**[Dev]** Can we maintain drake at its current non-parser state and move to converting Generator from a a tool to a domain?

**[Claude]** Yes — and I think it's the better move, for a reason beyond just sequencing. *(Checked the ground truth: ScalaSource's definition, the capability-domain pattern, build.sbt's compile paths.)* The ground truth makes it a cleaner pivot than I expected.

**Freezing drake is safe** — and safe for a specific reason, not just optimism. `DrakeGenTest`'s 64 exact-match tests are a drift guard: as long as they stay green, a frozen drake can't silently rot while you work elsewhere. You'd be freezing a plateau, not a half-finished increment.

**The stronger argument is that Generator-as-domain actually needs drake to stay put.** Look at what `ScalaSource` currently is — its entire content is one global:

```
type ScalaSource
  globals
    fix generator (TypeDefinition => String) Generator.generate
domain draco scalasource ScalaSource
```

That's the whole capability domain. And it is the *only* one. So `Generator[L]` today would be an abstraction over a singleton — nothing constrains it, nothing can falsify it. drake is the second projection you already have, but it's sitting as a method on the tool rather than as a domain. That asymmetry is precisely the thing to fix, and fixing it requires drake's grammar to hold still.

So my recommendation for increment-1: **promote drake to a capability-domain peer of ScalaSource** — one `.json`, one `.drake`, one generated `.scala`, whose sole global is `fix generator (TypeDefinition => String) Generator.drake`. That is the Generator-as-domain conversion in its smallest true increment. It changes drake's *status* without touching its *surface*, which is exactly what you asked for. And it self-gates: `DrakeGenTest` walks `draco/` recursively, so the new type immediately acquires its own exact-match test. Then increment-2 is `draco.generator.Generator[L]` with two genuine instances to constrain the abstraction.

I checked the layering concern — `build.sbt:64` puts `src/mods/scala/draco` on the *root* compile path, so there's one `draco.Generator` and it's fully visible to `src/main/scala/draco/**`. No main→mods violation. Three things worth knowing before you commit: **#43 stays armed** (the codec-aspect tripwire in `Generator.drake` is live during the freeze); **#46 part 2 gets absorbed rather than done** (anonymous-`new` becomes ScalaSource's business once projections are domains); **naming is the one real fork** — I'd go `draco.drake.Drake`, since `drake` is free where `scala` was host-reserved. Want me to draft `Drake.json` and its `.drake`?

---

**[Dev]** We can put the new domain in src/main as draco.generator.Genrator, which will not conflict with  src/mods draco.Generator. We can also put draco.drake.Drake in src/main, since draco.acalasource.ScalaSource started there.

**[Claude]** Both placements work structurally — domain-ness requires the type name to match the last package element, and `draco.generator.Generator` / `draco.drake.Drake` both satisfy that, same as `ScalaSource`. *(Checked the collision risk.)* Both are structurally fine, but they differ in one respect worth catching before you start.

**`draco.drake.Drake` works today, zero prerequisites.** In package `draco.drake`, `import draco._` brings in `draco.Generator` unshadowed, so the emitted `Generator.loadType(...)` and the global `Generator.drake` both resolve correctly.

**`draco.generator.Generator` has a prerequisite.** The emitter emits a *bare* reference — `Generator.loadType(...)`. Package-level there's no conflict, as you say. But inside `object Generator` in package `draco.generator`, the bare name `Generator` binds to the enclosing object itself, not to `draco.Generator`. The generated companion would emit `Generator.loadType(TypeName("Generator", …))` and fail to compile — `loadType` isn't a member of the new domain object.

Worth noting: DRACO.md already documents the emission as `draco.Generator.loadType(TypeName(...))`, "fully qualified for portability across packages." That's **stale** — no generated file in `src/main/scala/draco/` uses the qualified form. The documented intent and its rationale are exactly right; the implementation just never got there, and `draco.generator.Generator` is the first case where it bites. The fix is a one-liner, but the blast radius isn't one line — it changes every generated file, so `DracoGenTest`'s gate goes red until the whole corpus is regenerated.

So my recommendation is to take them in this order: **1. `draco.drake.Drake`** — no prerequisite, and the increment that actually matters; **2. qualify `typeDefinitionLoad`** + regeneration sweep, as its own commit; **3. `draco.generator.Generator`** — now unblocked, with two real capability domains to abstract over. Shall I draft `Drake.json` and `Drake.drake` for step 1?

---

**[Dev]** yes

**[Claude]** *(Checked that `ScalaSource.generator` is already consumed by `CLI.generate` — the capability-domain seam is load-bearing, not decorative. Created `Drake.json` and `Drake.drake`.)* Both drafted, mirroring `ScalaSource` exactly — same derivation, same single `generator` global, same domain-aspect self-loop. I kept `"value": "Generator.drake"` as a host-opaque string rather than an expression tree, because that's what `ScalaSource` does and consistency with the sibling is the whole point of this increment. To generate the Scala:

```bash
mkdir -p src/main/scala/draco/drake
bin/draco-gen generate src/main/resources/draco/drake/Drake.json > src/main/scala/draco/drake/Drake.scala
```

What I expect to fall out: a `trait Drake extends DracoType`, an `object Drake extends App with DracoType` with `typeDefinition`/`dracoType`/`elementTypeNames`/`domainType`, and `lazy val generator: TypeDefinition => String = Generator.drake`. Two existing walks should pick the type up without any test edits — `DrakeGenTest` (discovery walk) and `DracoGenTest` (byte-for-byte). I haven't run anything, so treat those as expectations to confirm rather than results. Once it's green, the natural next consumer is `CLI`: a `drake` command through `drake.Drake.generator` would be the first place the two capability domains are actually interchangeable.

*(Delegated action — Dev ran the generate and pasted the result: exactly the predicted companion shape.)* That's exactly the predicted shape — `Generator.drake` bound as the capability, and the bare `Generator.loadType` resolving through `import draco._` to `draco.Generator` with no shadowing. Structurally identical to `ScalaSource.scala` apart from the one value. Worth running the two gates that should now see the new type: `sbt "testOnly draco.DrakeGenTest"` and `sbt "testOnly draco.DracoGenTest"`. Assuming green, you now have two capability domains with an identical signature, which is the precondition `Generator[L]` was missing. On what follows — I'd previously put the `typeDefinitionLoad` qualification next, but there's a cheaper thing worth doing first: adding a `drake` command to `CLI` alongside `generate`. It's a few lines, it's independent of the regeneration sweep, and it's the first point where the two domains are actually substitutable at a call site rather than merely parallel in shape.

*(Delegated action — Dev ran both gates: 76 and 111 tests, all green — `Drake.json` appears in all three walks with zero test edits.)* Both green — the self-gating held. *(Brought the 120-day-old `project_generator_evolution` memory up to date.)* Step 1 is done and verified. `ScalaSource` and `Drake` now sit as peer capability domains with an identical signature — `TypeDefinition => String` — each naming one projection of the normative model. Two loose ends from earlier worth closing out when convenient: **#42 looks closeable** (that's my inference though, not something I've had you confirm), and **#46 has effectively been re-scoped by this pivot** — worth a comment on the issue recording that. For the next increment I'd still take the `CLI drake` command before the regeneration sweep.

---

**[Dev]** Show me what you mean in your description "take the CLI drake command before the regeneration sweep".  Use drake, json or scala text as examples, as approrpriate.

**[Claude]** Let me read the actual CLI definition so the example is exact rather than illustrative. *(Read `CLI.drake` and `CLI.scala`.)* Here's the whole increment. `CLI.json` is the source of truth; the other two fall out of it.

**1. `CLI.json` — add one global beside `generate`:** a `Dynamic` named `drake` taking `path: String`, body `println(draco.drake.Drake.generator(load(path)))`, and extend the `commands` value with `"drake" -> (as => drake(as.head))`.

**2. `CLI.drake` — what the emitter should then produce:**

```
    dyn drake Unit
      parameters
        par path String
      mon println(draco.drake.Drake.generator(load(path)))
    fix commands Map(String, [String] => Unit) Map("version" -> (_ => version), "inspect" -> (as => inspect(as.head)), "generate" -> (as => generate(as.head)), "drake" -> (as => drake(as.head)))
```

**3. `CLI.scala` — what `draco-gen` should then emit:**

```scala
  def drake(path: String): Unit = println(draco.drake.Drake.generator(load(path)))
  lazy val commands: Map[String, Seq[String] => Unit] = Map(..., "generate" -> (as => generate(as.head)), "drake" -> (as => drake(as.head)))
```

The payoff: `commands` becomes a dispatch table where two capability domains sit behind the *same* `TypeDefinition => String` slot, selected by a string. That's substitutability demonstrated at a call site, which is the thing `Generator[L]` would abstract.

**One thing to note, and it's why this is worth doing first.** Compare the two `Monadic` values: `generate` gets away with the bare `scalasource.` prefix because no member of `CLI` is named `scalasource`. But `def drake` *is* a member, so a bare `drake.Drake.generator` would try to select `Drake` on the method `drake` and fail to compile. Leading with `draco.` sidesteps it. That's the same shadowing class that blocks step 3 — a member name occluding a package name — showing up here in a two-line change instead of in the middle of a corpus-wide regeneration. Cheap place to hit it, and it lets you settle the policy (qualify everywhere vs. qualify on collision) on a small surface before step 2 applies that policy to every generated file. Want me to make the `CLI.json` edit and draft the matching `.drake`?

---

**[Dev]** Yes, I think I get it. Make the change.

**[Claude]** *(Edited `CLI.json` — the `drake` dyn and the extended `commands` — and `CLI.drake` to match.)* Both edited. To regenerate the Scala and check all three agree:

```bash
bin/draco-gen generate src/main/resources/draco/CLI.json > src/main/scala/draco/CLI.scala
sbt "testOnly draco.DracoGenTest draco.DrakeGenTest"
```

Two notes on what I wrote. **The `draco.` prefix is load-bearing** — bare `drake.Drake` would bind to the new `def drake` member and fail. This leaves `CLI` with one unqualified capability reference (`scalasource.ScalaSource`) and one qualified (`draco.drake.Drake`), which is an honest asymmetry rather than a tidy one; step 2's qualification policy is what would normalize it. **I edited `CLI.drake` by hand**, which inverts the normal direction — here I'm predicting emitter output. If the test disagrees, take the emitter's version; my draft has no authority over the surface.

---

**[Dev]** I had already done "sbt assembly" earlier, so I did not have to this time:

*(Pasted the run — regenerated `CLI.scala`, then both gates: 187 tests, all green.)*

**[Claude]** 187 green, and the `[info] compiling 1 Scala source` line is the part that matters most — `CLI.scala` compiled, which confirms the shadowing analysis was right and the `draco.` prefix resolves it. Had I left it as `drake.Drake.generator`, that compile would have failed rather than any test. The hand-authored `CLI.drake` also matched the emitter exactly, so no correction needed there. `CLI.scala:21-22` is now the thing worth looking at: two capability domains, one `TypeDefinition => String` slot, selected by a string at runtime. That's the substitutability `Generator[L]` would give a type to.

One caveat on actually using it: your earlier `sbt assembly` predates this change, so the jar doesn't have the `drake` command yet. After a fresh assembly, the demonstration I'd reach for is `bin/draco-gen drake src/main/resources/draco/drake/Drake.json` — the drake capability domain emitting its own definition through the CLI. Recommend the full `sbt test` before pushing. That closes the cheap step. Step 2 — qualifying `typeDefinitionLoad` and sweeping the regeneration — is next, and it now has a concrete precedent: this increment settled that the resolution for member/package shadowing is to qualify the reference, not rename the member.

---

**[Dev]** Do step 2

**[Claude]** Step 2's blast radius needs scoping before I touch the emitter. *(Counted the bare `Generator.loadType` references, checked `bin/draco-gen`'s entry point and `GeneratorCLI`'s subcommands.)* Before editing the emitter — two discoveries, one of which corrects something I told you earlier.

**`bin/draco-gen` does not run the CLI we just edited.** It invokes `draco.GeneratorCLI` — a hand-written tool in `src/mods/scala/draco/GeneratorCLI.scala`, entirely separate from the definition-backed `draco.CLI`. There is no `drake` subcommand. So my suggested `bin/draco-gen drake …` would just print usage and exit 1 — that was wrong. The `drake` command we added is real and gated, but it lives on `draco.CLI`, reachable as `java -cp target/scala-2.13/draco-*.jar draco.CLI drake <path>`. Worth knowing there are two CLIs in play, only one of which is definition-backed.

**Step 2 is bigger than I estimated, in two ways I should have checked before recommending it:** **117 files** carry the bare `Generator.loadType`, not the 56 under `src/main/scala` — plus 59 in `src/mods/scala` and 2 in `src/test/scala`. And **`TypeElement.scala` has no regeneration path** — it's the sealed family emitted by `Generator.generate(Seq[TypeDefinition])`, but `runGenerate` is single-type only; there's no `generate-multi` subcommand.

Given that, I want to revise my recommendation rather than execute the plan as scoped: **qualify conditionally — emit `draco.Generator.loadType(...)` only when the emitted reference would actually be shadowed, i.e. when `td.typeName.name == "Generator"`.** One line, zero regeneration, no tooling gap, and it unblocks step 3 immediately. My original argument for uniform qualification was that DRACO.md documents it — but the bare form is *correct everywhere it currently appears*. Uniform qualification buys consistency of output text, not correctness, and the price turned out to be a 117-file sweep plus a tooling gap. The counter-argument, which is real: conditional emission means the generator produces different reference forms for different types with no local explanation. Which do you want? If uniform, I'd do the `generate-multi` subcommand as a prerequisite increment first.

---

**[Dev]** This appears to be a choice with serious implications that I don't understand well enough to make. Since it ot involves CLI.scala, which you say isn't used, my preference would be to ignore CLI and focus on how to continue to use GeneratorCLI.scala instead to create the the Generator domain, and get it to work.

**[Claude]** Let me clear up the confusion first, because I explained it poorly.

**The choice has nothing to do with `CLI.scala`.** It's about one line in the emitter that affects *every* generated companion object. Every generated type contains a line like `override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Foo", ...))`. That bare `Generator` means "the `draco.Generator` tool". It works for all 117 files today. But the new domain type will itself be *named* `Generator` — so inside `object Generator`, the word `Generator` refers to itself, and it has no `loadType`. That one file needs `draco.Generator.loadType(...)` written out in full. So the choice was only: write it out **everywhere** (uniform text, but rewrites 117 files), or **only where it's actually needed** (one line, no rewrites). `CLI.scala` was just one of the 117 — not the subject.

**And I didn't mean `CLI.scala` is unused.** `draco.CLI` is definition-backed, compiled, and gated by both test walks. What I got wrong was claiming `bin/draco-gen` runs it. It doesn't; that's `GeneratorCLI`. Both exist and both are real; only `GeneratorCLI` is the working tool.

Given you want the Generator domain working, I'm taking the one-line option — it unblocks step 3 now, and it's a one-line reversal later if uniformity ever matters. *(Made the conditional emitter edit; modelled the domain on `Primal` — a `fix` element with no value becomes an abstract `val`; created `Generator.json` and `Generator.drake`.)* Drafted:

```
type Generator(L)
  elements
    fix generator (TypeDefinition => String)
domain draco generator Generator
```

`sbt assembly` has to come first this time — the jar needs my `typeDefinitionLoad` change, or the emitted load call won't be qualified and `Generator.scala` won't compile. **One genuine unknown, worth watching rather than assuming:** `Generator` is the first type in the corpus that is *both* type-parameterized *and* self-domained. `Primal` is parameterized but belongs to `Draco`; `ScalaSource` and `Drake` are self-domained but unparameterized. So the `domainType` line has no precedent — whether the emitter produces `Domain[Generator]` or `Domain[Generator[_]]` is undetermined, and the bare form wouldn't compile against a parameterized trait. I'd rather find out from the compiler than guess.

---

**[Dev]** I don't think we're in sync, conceptually. You're insisting on loadType being in Generator itself. The method loadType is simply a utility function for bringing persistent data into memory as TypeDefinition. It doesn't have to be defined in Generator, it could be defined by anything in the draco package. If we refine it somewhere else now, won't the mitigate the problem were considering?

**[Claude]** You're right, and it's a better framing than mine. *(Checked what `loadType` actually depends on.)* The ground truth makes the case stronger than you put it.

`loadType` is four lines — `tryLoad(typeName).getOrElse(TypeDefinition(typeName))` plus a classpath read. Nothing about it is generation. But look at `Draco.scala:6`:

```scala
object Draco extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Draco", ...))
```

The framework's *root domain* depends on the code generator to load its own definition. And `draco.Generator` isn't even definition-backed — it's a hand-written tool in `src/mods`, absent from `Draco.drake`'s type list. So every definition-backed type in `src/main` carries a runtime dependency on a speculative-tier tool, which `project_mods_layer_policy` forbids in spirit and only survives because `build.sbt:64` puts that directory on the root compile path. So this isn't just a shadowing workaround — it's a real layering inversion, and moving `loadType` fixes it at the root.

**My recommendation for the home: `TypeDefinition` itself** — `TypeDefinition.loadType(typeName)`. It's the type being loaded, it's definition-backed and core, and no new shadowing surface is introduced. It reverses the earlier `TypeDefinition.load` removal in *location* only — there'd still be exactly one load path, which was the point of that consolidation.

**But it does not avoid the sweep, and I want to be straight about that.** Any change to the emitted text turns `DracoGenTest` red until every generated file is regenerated. Moving `loadType` eliminates the *special case* and the coupling; it doesn't eliminate the regeneration. So the honest comparison: my conditional edit — 0 files regenerated, but a special-case rule in the emitter and the main→mods coupling stays; your approach — removes the special case *and* the coupling, at the cost of the sweep. **Yours is the better architecture.** If you take it, my conditional edit should be reverted — it'd be dead weight.

Sequenced: 1. `generate-multi` in `GeneratorCLI` (still needed for `TypeElement.scala`); 2. move `loadType`/`tryLoad` onto `TypeDefinition`, leaving a forwarder on `Generator`; 3. point the emitter at the new home; revert the conditional edit; 4. regenerate the sweep; 5. `draco.generator.Generator` generates with no special case at all. One thing I'd want to check before committing to "117 files": the hard gate is `DracoGenTest` over `draco/` (56 files) — the `src/mods` Scala appears to be report-only, and if so, those 59 can be regenerated opportunistically. Want me to start with step 1, or confirm the mods gating question first?

---

**[Dev]** Confirm first

**[Claude]** *(Read `ExampleDomainsGenTest` and `AerialGenTest`.)* Confirmed — and it shrinks step 4 substantially.

**`ExampleDomainsGenTest` is report-only.** Its sole assertion is `assert(total > 0, ...)` — a discovery scaffold. The docstring says so outright: it "does not `fail()` on a mismatch," with a note to "harden to `assert(differ == 0)` once the gaps close." **`AerialGenTest` isn't a byte-gate either.** So the 59 `src/mods/scala` files are unguarded, and the 2 in `src/test/scala` are hand-written. Combined with keeping a forwarder on `Generator`, those 61 files never need touching. **The blocking sweep is `draco/` only: 55 single-type files plus `TypeElement.scala`.**

One wrinkle the count exposes: `DracoGenTest`'s per-type walk *excludes* the `TypeElement` family members — they're covered by the single "TypeElement family" test. So a naive per-JSON loop would emit a wrong standalone file for each family member. The sweep script has to skip them and route `TypeElement.scala` through `generate-multi`. Revised sizing: 55 files + TypeElement, not 117 — roughly half, and the half that remains is uniform and scriptable. Shall I start on `generate-multi`?

---

**[Dev]** Yes, but call it either multiGenerate or generateMultiple (Can you name function with a "-" in the name?)

**[Claude]** Good question, and the answer is a useful distinction: **a Scala method can't contain a hyphen** (you'd need backticks — `` `generate-multi` `` — which nobody wants). But `compile-multi` is a *string literal* matched in `main`, not a method name. So the hyphen lives only in the CLI subcommand string. I'll keep the subcommand `generate-multi` for symmetry with `compile-multi`, and name the method `runGenerateMultiple` per your preference — flagging that the sibling is `runCompileMulti`, so `runGenerateMulti` would be the more consistent choice if you'd rather match.

*(Read the sibling — important difference: `runCompileMulti` generates each source separately; what's needed is the other overload, `Generator.generate(tds: Seq[TypeDefinition])`, emitting one file for the sealed family. Made the three edits to `GeneratorCLI.scala`: the method, the `main` case, a usage line.)* `generate-multi` is in:

```scala
private def runGenerateMultiple(paths: Seq[String]): Unit = {
  val tds    = paths.map(loadTypeDefinition)
  val source = Generator.generate(tds)
  print(source)
  if (!source.endsWith("\n")) println()
}
```

To verify, once `sbt assembly` picks it up: `bin/draco-gen generate-multi $(ls src/main/resources/draco/{Action,BodyElement,Condition,Dynamic,Factory,Fixed,Local,Monadic,Mutable,Parameter,Pattern,TypeElement,Variable}.json) | diff - src/main/scala/draco/TypeElement.scala`. An empty diff confirms the new subcommand reproduces the existing hand-checked file before we change anything about `loadType` — which is what makes it a trustworthy tool for step 4. I'm guessing at the family membership there; `DracoGenTest`'s family test knows the real set. Two open items I'm carrying: my conditional-qualification edit still needs reverting in step 3, and `draco/generator/Generator.json` + `.drake` are sitting in the tree ungenerated — they'll fail `DracoGenTest` if you run the full suite before we get there.

---

**[Dev]** Does this look correct? :

*(Pasted the run — `sbt assembly` clean, then the `generate-multi` diff: 13 differences, every one a `<` blank line.)*

**[Claude]** Yes — that's a clean result, and better than it looks at first glance. Every difference is a **blank line**, not content. There are 13 of them, and you passed 13 JSON files — so it's exactly one extra blank line per type, emitted by `Generator.generate(Seq[TypeDefinition])` itself. Not a single content line differs, which means the family membership and ordering I guessed are both right.

`DracoGenTest` doesn't see these because its `normalize()` collapses blank-line runs and trims leading blanks — which is why the on-disk `TypeElement.scala` has drifted from verbatim generator output without the gate ever noticing. So `generate-multi` works. That raises a question worth settling before step 4, though — **is the same whitespace drift present in the single-type files?** Cheap to check: `generate` Primal and Draco piped through `diff`. If those come back empty, the sweep produces a clean, reviewable diff where every changed line is a `loadType` line. If they show blank-line drift too, the sweep is really two changes wearing one coat and we should separate them.

*(Delegated action — Dev ran the two single-type diffs: each showed exactly one leading blank line.)* Uniform: exactly one leading blank line on every file, single-type and multi-type alike. So `Generator.generate` has been emitting a leading newline all along, invisible because `normalize()` trims it. That's worth fixing at the source rather than working around. *(Traced it to `Generator.scala:1796` — the `s"""` interpolators open with a newline `stripMargin` doesn't remove; wrapped the `generate(td)` chain in `.stripPrefix("\n")` and dropped the leading `\n` from the multi-type template, deliberately leaving the `mkString("\n\n")` block join alone — spacing is a formatting judgment, not a bug.)* After `sbt assembly`, expect the two single-type diffs **empty**, and the multi-type down to **12** — the remaining inter-block blanks I left deliberately. Worth noting this was a latent defect, not something we introduced — the generator has always emitted a leading blank line, and `DracoGenTest`'s normalization has been quietly absorbing it. The byte-exactness we're establishing is new, and it's what makes the sweep trustworthy.

*(Delegated action — Dev re-ran all three diffs: both single-type diffs empty; `TypeElement` down to 11 inter-block blank lines.)* Byte-exact on both single-type files, and `TypeElement` dropped to 11 — the leading newline is gone and the line numbers all shifted by one, exactly as intended. (My "13" last turn was a miscount; it was 12.) Now step 3. *(Investigated the loading cluster — four functions, and Generator's private `resourcePath` duplicates what `TypeName.resourcePath` already derives. Then hit the crux: `TypeDefinition.scala` is generated and gated — putting `loadType` there means authoring host-code functions into a core type's JSON definition, regenerating, and keeping the `.drake` in sync, with codec derivation, `Aspects` inheritance, and bootstrap order all in play. Asked Dev whether to author into `TypeDefinition` or make a new type; Dev chose a new type. Settled the sub-decisions: name `TypeLoader`, three `dyn` globals, a Draco member like `SourceContent`/`ContentSink`. Verified the import mechanism — `referencesCirce` scans global bodies for `"io.circe"`, exactly how `CLI`'s `load` dyn earns its circe imports. Created `TypeLoader.json` + `TypeLoader.drake`, registered `TypeLoader` in `Draco.json`/`Draco.drake`.)* Everything for step 3a is authored. This turn deliberately stops **before** switching the emitter — `TypeLoader` should land green as a standalone type before anything depends on it. What to watch, in priority order: **(1)** `TypeLoader.scala` must compile — the whole design hangs on `referencesCirce` seeing `io.circe` in the value and emitting the `Decoder` import; **(2)** `DrakeGenTest` on `TypeLoader` — my hand-authored `.drake` is a *prediction* of emitter output, and if the emitter formats differently, the emitter wins; **(3)** the `Draco.scala` regen should now list `TypeLoader`. Note this turn's regen still emits `Generator.loadType` (unchanged emitter) — that's expected; it flips in the step-4 sweep, not now.

*(Delegated action — Dev ran assembly + generate TypeLoader/Draco + `sbt test`: 286 of 287, the sole failure the flagged orphan — `draco/generator/Generator.json` with no `Generator.scala` yet.)* 286 of 287 green — and the one failure is exactly the orphan I flagged. The real result is buried in that wall: **`TypeLoader` landed green on both gates, first try.** My hand-authored `.drake` matched the emitter exactly; the `referencesCirce` → `import io.circe.Decoder` mechanism worked and `_.as[TypeDefinition]` resolved. The design held with no correction. *(Made the step-3b emitter edit — every generated load call now points at `TypeLoader.loadType`; the conditional is gone.)* Type loading is officially a `TypeLoader` capability, not a `Generator` concern. Now the sweep — the low-risk way: the only content change in the emitter since these files were generated is `Generator.loadType(` → `TypeLoader.loadType(`, so a **surgical text replacement** produces byte-identical results to regeneration, with a clean one-line-per-file diff. Critically, the replacement targets `.loadType(` only — `Generator.generate` in `ScalaSource.scala` and `Generator.drake` in `Drake.scala`, which are *deliberate* bindings to the tool, stay untouched. The one file that doesn't exist yet — `generator/Generator.scala` — gets generated fresh with the new jar. Two things I genuinely can't predict: whether `generator/Generator.scala` compiles (the parameterized-self-domain `Domain[Generator]` vs `Domain[Generator[_]]` question — this is the real test of the whole exercise), and `git diff --stat` showing a single changed line per swept file.

---

**[Dev]** The shell comments will interfere with copy/paste execution of your commands

**[Claude]** Right — the inline `#` comments break a clean copy-paste. Here's the same sweep as one uninterrupted block:

```bash
sbt assembly
find src/main/scala/draco -name '*.scala' -print0 | xargs -0 sed -i '' 's/Generator\.loadType(/TypeLoader.loadType(/g'
mkdir -p src/main/scala/draco/generator
bin/draco-gen generate src/main/resources/draco/generator/Generator.json > src/main/scala/draco/generator/Generator.scala
sbt test
```

The two things I'm watching, unchanged: whether `generator/Generator.scala` compiles, and `git diff --stat` showing a single changed line per swept file.

*(Delegated action — Dev ran the block: all 287 pass.)* All 287 pass. The two unknowns both resolved cleanly. *(Read the generated file.)* There it is — line 15: `Domain[Generator[_]]`. The emitter already knew to wildcard a parameterized type in domain position (`wildcardTypeName`), so the concern was unfounded; no emitter change needed. And line 10 is the payoff: `TypeLoader.loadType` inside `object Generator` in package `draco.generator`, resolving cleanly via `import draco._` with nothing shadowing it. That's the entire problem, solved. **The Generator domain exists, generates, compiles, and passes both gates. The tool-to-domain conversion is done.** *(Recorded the milestone in memory.)* Type loading is now a `draco.TypeLoader` capability rather than a Generator concern — which was your reframe, and it turned out to fix more than the shadowing: it also removed the layering inversion where `src/main`'s root domain depended on a `src/mods` tool to load its own definition.

**One loose end, cosmetic but worth deciding on.** `ExampleDomainsGenTest` dropped from 30 match to 0/48 differ — collateral: the 59 mods example `.scala` files still say `Generator.loadType` while the emitter now emits `TypeLoader.loadType`. They still compile and the test is report-only, so the suite is green — but the report is now uninformative. The same surgical sed restores it, targeting `src/mods/scala/domains` only (the hand-written tool files in `src/mods/scala/draco` are deliberately excluded — they call `loadType` internally and must keep it). Want me to fold the mods sweep in, or leave the report as-is?

*(Delegated action — Dev ran the mods sed + `sbt test`: 287/287.)* `ExampleDomainsGenTest` is back to **30 match, 18 differ** — the report is informative again, showing the real #46-era gaps rather than 48 uniform `loadType` mismatches. 287/287 green. The whole track is complete and consistent: **`draco.generator.Generator[L]`** and **`draco.drake.Drake`** are domains alongside `draco.scalasource.ScalaSource`; **`draco.TypeLoader`** owns type loading — which ended both the self-shadowing *and* the `src/main`→`src/mods` runtime coupling; emitter cleanups (`generate-multi`, the latent leading-`\n` fix). Two forward notes: **the abstraction now has three instances to constrain it** — whenever you want `Generator[L]` to *do* something rather than describe the shape, there's finally a real trichotomy to generalize over; and **`draco.rete` / `draco.actor`** remain the unbuilt capability-domain peers if the self-port direction is where you head next. Good place to stop. Want me to draft the commit message, or leave it here?

---

**[Dev]** Context is  is at only 21%. Why are you suggesting a stop?

**[Claude]** Fair — that was habit, not a judgment call from the actual state. Context is fine at 21%, the suite is green, and there's a clear next increment. No reason to stop; I misread "natural pause in the work" as "time to wrap."

The strongest next step is the one this whole track was built to enable: **make `Generator[L]` actually abstract over its three instances instead of just describing their shape.** Right now `ScalaSource`, `Drake`, and `generator/Generator` each independently carry `generator: TypeDefinition => String`, but nothing ties them together — `Generator[L]` is a trait that *looks* like the supertype without being one. The payoff of having three instances is that the abstraction is finally falsifiable. Before I propose a concrete shape, one genuine design question that determines everything downstream — and it's yours, not mine to default:

*(Dev interrupted the response before the question could be posed; no answer was requested or given.)*

---

**[Dev]** We need a draco type  draco.Source for ScalaSource to derive from, so that Generator[Source] can be the programming language neutral superdomain of the Generator[ScalaSource] subdomain.

**[Claude]** Good — that's a cleaner shape than either option I posed. `L` is instantiated with a *Source type*, and `Source ← ScalaSource` gives you the `Generator[Source] ← Generator[ScalaSource]` super/sub relation directly through the type hierarchy. *(Grounded the design in the corpus — studied how `Value from Value(JSON)` concretizes a parent's abstract elements.)* That surfaced the crucial detail: the derived type's *trait* extends the parent, but the *companion object* extends `App with DracoType` regardless — the object never extends its own trait. Abstract elements get concretized in the **factory**, not the object.

That settles the shape of this first step. `ScalaSource`'s `generator` is an object-level global, and the object isn't a `Source` — so I should **not** try to hoist `generator` into `Source` as an abstract element yet (it would leave `trait ScalaSource` with an unimplemented member the object can't satisfy). The clean, honest minimal step is: `Source` as a neutral supertype, `ScalaSource` derives it. Whether the projection function belongs *on* `Source` is a real follow-up question, not this step. *(Created `Source.json`/`Source.drake`, rewired `ScalaSource`'s derivation from `DracoType` to `Source`, registered `Source` as a Draco member.)* All authored. What this establishes: `trait Source extends DracoType` and `trait ScalaSource extends Source` — so `ScalaSource <: Source` at the type level, which is what makes `Generator[Source]` the supertype of `Generator[ScalaSource]`. If green, the next question becomes the interesting one: whether `Drake` should also derive `Source`, and whether the `generator` projection belongs *on* `Source` as a contract — which, per the object-doesn't-extend-trait finding, would mean rethinking where that function lives rather than a simple hoist.

*(Delegated action — Dev ran assembly + the three generates + `sbt test`: 290/290.)* 290/290 — three new green tests for `Source`, and everything else held. The hierarchy is real: `trait Source extends DracoType`, `trait ScalaSource extends Source`. The symmetry now cuts a clear next step: **`Drake` should also derive `Source`** — right now the two projection domains aren't yet peers under the neutral supertype. The deeper question it sets up — and the one I'd flag rather than silently decide — is whether the `generator` projection function should become a **contract on `Source`** rather than an independent global on each domain. Because a companion object doesn't extend its own trait, you *can't* express "every Source provides a generator" with an abstract `val` on the trait satisfied by an object global. Making that contract real is genuine design, not a mechanical edit. Want me to make `Drake` derive `Source` now, or pause on the contract question first?

---

**[Dev]** Contract on Source. This is what I've been working towards since I first mentioned Haskell as another generated source language. I'm hoping that the only data a specific language needs to provide is the syntax templates in the form of substitution strings  that the  generator contract functions use to generate the source.

**[Claude]** Yes — and you have more of it already built than the framing suggests. The proof-of-concept exists in embryo: `Generator.expression` and `drakeExpression` are *two functions rendering the same `{op:[operands]}` tree* to different surfaces. That's already "one contract, per-language rendering" — the operators `.`/`->`/`\`/`if` are exactly substitution points that render as Scala tokens or Haskell-symbol tokens depending on target. What you're describing is naming that pattern and making it the organizing principle, with the language types as template-bags.

Let me state the contract shape back precisely, so we're aligned: **`Source` carries the generation *functions* (the invariant control flow — walk the TypeDefinition, decide what to emit), and a set of abstract *template slots* (substitution strings). A specific language provides only the slots.** `ScalaSource` fills them with Scala tokens, `HaskellSource` with Haskell tokens, same functions driving both. That also resolves last step's tension cleanly: `Source` stops being a marker and becomes the engine.

Now the one thing I owe you as honest engineering, stated once and then I'll drop it: **the strong form — "*only* templates, nothing else" — holds cleanly for the rendering layer and strains at structural divergence.** Type rendering (`Seq[T]` vs `[T]`) and expression rendering are pure substitution — provably, because drake already does them. But `trait X` + companion `object X` has no Haskell analog; Haskell emits a `data`/`class`/`instance` shape — a *different number and structure* of declarations, not the same skeleton with different tokens. That's not a template slot, it's a structural branch. This session already bumped into the sharp edge of it (the companion object isn't its own trait — a Scala-specific structural fact). So the realistic destination is: templates carry the surface, plus a small number of per-language *structural* decisions. Most of the win is real; the "100% templates" version likely isn't.

Given that, the disciplined first increment is to **start in the proven zone**: lift the expression-tree renderer onto `Source` as the first contract method. It has *two existing instances* to generalize from — the same discipline that's carried every step this session: never abstract over one instance. Haskell then becomes the third instance, providing just its operator template strings. One fork I won't decide silently: I'd **prototype the contract shape in hand-written Scala first** and only back-port to a `Source` *definition* once the shape is proven — this is exploratory design where a premature definition could lock in the wrong abstraction. Tell me if you'd rather go definition-first.

---

**[Dev]** I'm happy to do hand-written scala first, as long as it is understood that we want to use definitions as soon as practical.

**[Claude]** Understood — Scala prototype as a scaffold to find the shape, definition-backed as soon as the boundary is proven, not a permanent hand-written home. *(Recorded that constraint in memory, then read the two renderers side by side.)* This is the whole thesis in one screen. The traversal is **identical** — recurse operands, dispatch on `op`. Everything language-specific is in how each operator's tokens render, and most operators are the *same* in both:

| op | Scala | Drake | varies? |
|----|-------|-------|---------|
| `.` `()` `(,)` `*` `==` `!=` | identical | identical | **no** — shared |
| `->` | ` => ` | ` -> ` | token only |
| `if` | `if (c) t else e` | `if c then t else e` | token only |
| `\` | `(p1, p2) => body` | `\p1 p2 -> body` | **structural** (Scala's single-param-no-parens rule) |

So the slot boundary is sharp: one shared engine, three tiny per-language slots, of which two are pure strings and one (`lambda`) carries a structural wrinkle — the honest boundary, in miniature. *(Built `SourceContract.scala` — `ExpressionRenderer` engine + `SourceTemplates` slots + `ScalaTemplates`/`DrakeTemplates` — and `SourceContractTest`, which proves the engine+slots reproduce both production renderers over a corpus covering every operator.)* Three findings worth carrying into the design regardless of the test result: **(1)** the clean/structural split is visible in the slots themselves — `arrow` and `conditional` are pure substitution, `lambda` is a method because Scala's single-param rule is *logic*, not a template; **(2)** Haskell's expression layer is nearly free — its slots would be essentially identical to `DrakeTemplates`, because both are Haskell-derived; **(3)** `=` is the seam again — `Generator.expression` renders it; `drakeExpression` errors on it (drake consumes named-args at the application surface before they reach the renderer). Once green, the back-port makes `render` a `Source` contract method — at which point we'll hit the real question of how a definition expresses "abstract template slot."

---

**[Dev]** I think "SourceContract" puts to much structure in one file. I would expect that they would be types in Generator[Source] domain, but I'm not sure how to create that manually. What if "type Drake from Generator[Source]"?

**[Claude]** You're right on both counts — one file is doing four jobs, and the fix isn't four smaller files. Three of those four pieces should *fold into the type hierarchy you're describing*, not sit beside it as loose helpers.

**"type Drake from Generator[Source]" — mechanically, yes.** It's the exact shape we already have working: `type Value from Value(JSON)` generates `trait Value extends draco.format.Value[JSON]`. So the *how* is solved. **But it conflates two roles, and that's the thing to settle before wiring it.** Two distinct jobs: the **Source role** — the *token set* (`arrow`, `conditional`, `lambda`), what varies per language; and the **Generator role** — the *engine* (`render`) bound to a token set. Mapped onto your domain, the prototype's pieces collapse: `Source` declares the abstract slots **and** the engine; `ScalaSource extends Source` / `DrakeSource extends Source` provide *only* the slot values. There is no standalone `SourceTemplates`/`ScalaTemplates`/`DrakeTemplates` — they *are* `Source` and its subtypes. That's your "types in the domain," and it's fewer types than the prototype, not more.

Given that, `Drake` extending `Generator[Source]` reads as the wrong seam: `Drake`/`DrakeSource` is a **Source** (a token set), so it should be `... extends Source`. The *generator* over it is `Generator[DrakeSource]`, and `Generator[Source]` stays the neutral supertype — `L` ranges over `Source` subtypes, so `Generator[DrakeSource] <: Generator[Source]` driven by `DrakeSource <: Source`.

**One honest blocker gates the back-port to definitions.** For `DrakeSource` to *provide* the slots `Source` declares abstract, either its companion **object must extend the `Source` trait** (a generation pattern we don't emit today) *or* `Source` becomes an instance type with a factory that supplies the slots, the way `Value` concretizes inherited members. That's the same structural seam from two steps ago, and it's the real decision — it determines whether a "language" is a singleton object or an instance. *(Posed the question; Dev's next message went elsewhere.)* Where things stand for when you pick it back up: the prototype proves the engine-plus-slots boundary; the open decision is how a language provides its slots, and I won't move on it until you say. Nothing is broken or half-applied — the suite was last green at 290, and the prototype is additive.

---

**[Dev]** I think we need a fundamental rename of the language specific domains. change <language>Source to <language>Target. "Source" in  draco is ambiguous because it is also used  in a fundamental way to define transforms (source, target)

**[Claude]** Agreed — "Source" is overloaded: the transform machinery already owns `source`/`target` as fundamental vocabulary, so a language type called `ScalaSource` reads as "the source side of a transform" rather than "the Scala generation target." And it's semantically backwards: the language is what you generate *toward*, i.e. the target. *(Mapped the full blast radius, then executed: created `Target.json`/`.drake` and `ScalaTarget.json`/`.drake` in a new `scalatarget` package, deleted the old `Source`/`scalasource` resources, updated the `Draco` member list, the `CLI` call site, the prototype — renamed `TargetContract`/`TargetContractTest` — and the prose in `Generator.scala` comments and `drake.dlt`.)*

Two judgment calls I'm making, flag if you disagree: I'm renaming the **package** `scalasource → scalatarget` too (a self-domained type in a `scalasource` package named `ScalaTarget` would be incoherent), and I'm leaving **`Drake`** as-is — it's the drake target but was never named `DrakeSource`, so it's outside the `<language>Source` pattern. I deliberately left the old generated `.scala` in place so the tree still compiles for the `sbt assembly` that bakes the new resources — they get regenerated and deleted in the same block. To recap the change set: renamed `draco.Source → draco.Target` and `draco.scalasource.ScalaSource → draco.scalatarget.ScalaTarget`; untouched, deliberately — `Drake` and `SourceContent` (genuinely the input side — correct sense of "source").

---

**[Dev]** I think I made a mistake on the rename of Source to Target. I intended for draco.Source to remain source because drake and json were still source but the language subdomains were to become target as in the transform concept

**[Claude]** That's my error — I over-applied it. You said rename the *language* subdomains to Target, and I also renamed the neutral `draco.Source` supertype to `Target`, which you didn't intend. Your taxonomy makes sense: `Source` stays as the neutral concept (drake and json are source-side, native representations), and the foreign language projections (Scala → `ScalaTarget`, later Haskell/Lean) are the *targets* of the transform.

Good news: you hadn't run the regeneration block yet, so all the `.scala` is still original — only resources and the prototype changed. *(Corrected back: restored `Source.json`/`.drake`, removed `Target`, reverted the `Draco` member list, pointed `ScalaTarget`'s derivation at `Source`, restored the prototype's `Source` naming, fixed the memory written under the wrong assumption.)* All corrected. `ScalaSource → ScalaTarget` stays (Scala is a foreign target); `draco.Source` is restored as the neutral supertype; `ScalaTarget from Source`. Since the `.scala` was never regenerated, `Source.scala` and `Draco.scala` are still right as-is — only `ScalaTarget` and `CLI` need generating, plus deleting the old `scalasource` dir. Expect the suite back at **290** with `ScalaTarget` in place of `ScalaSource` in the gate lists, and `Source` still present.

One thing worth surfacing directly, since it may bear on where this goes next: `trait ScalaTarget extends Source` literally reads "a target is-a source." Under your model that's fine — `Source` is the *source-code-projection* supertype and the foreign ones happen to also be transform targets — but if it ever grates, the alternative is a distinct target-side supertype. Not something to decide now; just noting the relationship the derivation creates so it's not a surprise later.

*(Delegated action — Dev ran the block: 293/293 green.)* 293/293. The rename landed correctly: **`ScalaTarget`** (package `scalatarget`) replaces `ScalaSource` across all three gates and generates `trait ScalaTarget extends Source`; **`Source`** is intact; **`SourceContractTest`** passes all three — `ScalaTemplates`/`DrakeTemplates` reproduce both production renderers, and the `=`-is-Scala-only case holds. Count reconciliation: 290 → 293 is the three `SourceContractTest` tests now compiling and running. Clean checkpoint. Two things still open: **the contract fold** (gated by the how-does-a-subtype-provide-slots decision) and **the `ScalaTarget extends Source` naming tension** (noted in memory). I'll wait for your direction.

---

**[Dev]** Let's clarify the the "CO" in "DRACO". With regard to generate, we need to  convert procedural/imperative into inferential/inductive, which means distinguishing between conditions and actions (predicates and functions) in the source domain contract.

**[Claude]** Draco the lawgiver — the name is about rules, and a rule has exactly two halves: a **condition** (the LHS predicate that decides when it fires) and an **action** (the RHS function that runs). That duality is already the spine of the model — `RuleType` = `pattern` (conditions) + `action`, and `TypeElement` carries `Condition`, `Action`, and `Pattern` as first-class kinds. The anomaly is the *Generator*: it's the one part of draco that's procedural — a hand-written `op match` — when everything else is expressed as rules. So "make generate inferential" = make the Generator obey the same condition/action law the rest of draco already does.

Here's the sharpening. My prototype's `render` is a disguised condition/action table:

```scala
op match {
  case "."  => args.mkString(".")        // condition: op=="."   → action: join with "."
  case "->" => args.mkString(t.arrow)    // condition: op=="->"  → action: join with arrow
  case "\\" => t.lambda(...)             // condition: op=="\\"  → action: lambda render
}
```

Every arm is a `(condition, action)` pair — but the two are welded together in code. Splitting them cleanly gives the inferential form: `RenderRule(condition: Node => Boolean, action: (Node, Seq[String]) => String)` — render(node) = the action of the rule whose condition matches; inference, not dispatch. And here's the payoff that lands your earlier "structural vs surface" distinction precisely:

- **Conditions are language-invariant.** "Is this node a `->`?" is a fact about the *model's shape* — the same whether you target Scala, drake, or Haskell. Conditions belong to `Source`.
- **Actions are per-target.** How a matched `->` *renders* (`=>` vs `->`) is the token — that's what a target contributes.

So the clean cut isn't "arrow/conditional/lambda slots"; it's: **`Source` owns the conditions (the predicates over model structure), each target owns the actions (the renderers).** And — critically — it's the cut that makes generation *data*: a set of `(Condition, Action)` pairs is exactly a draco `Rule`, which is why this is also the self-hosting bridge. Once the Generator is condition/action rules, it's expressible as a rule domain and can be definition-backed. Concretely, the first increment I'd take: refactor the prototype's `ExpressionRenderer` from `op match` into an explicit rule table, and let the existing `SourceContractTest` prove the inferential form reproduces both procedural renderers unchanged. Before I refactor: does that condition-invariant / action-per-target split match what you mean by distinguishing predicates from functions in the source contract — or are you drawing the line somewhere I'm not seeing?

---

**[Dev]** How do we indicate that the split preserves semantics? In other words, how do we ensure  we're creating the same actions in different programming languages?

**[Claude]** The honest answer is that the split *doesn't* preserve semantics on its own — keying two actions to the same condition guarantees they render the same *node*, not that they *mean* the same. `ScalaTemplates.arrow = " => "` and a hypothetical `HaskellTarget.arrow = " -> "` are independent strings; nothing structurally stops someone writing `" + "`. So the question is exactly right, and it has two parts: how you *indicate* the shared meaning, and how you *enforce* it.

**Indicate it: the condition carries the meaning, the action carries only the spelling.** The semantic anchor is the normative concept named by the condition — the `->` node in the model *is* "function/type arrow," defined once, language-neutrally. You don't author `ScalaArrow` and `HaskellArrow` as independent facts and hope they align; there is *one* meaning and N projections of it. Semantic identity is indicated by both actions answering to the same denotation-bearing condition — one anchor, many spellings.

**Enforce it, two ways, matching the two kinds of action we already found:**

- **By construction (where it's possible):** where an action reduces to a *shared neutral function + a per-language token* — `args.mkString(t.arrow)` — there is literally one action; N languages cannot diverge because they share the code and differ only in a leaf string. Where this holds, preservation is *free*. This is why "reduce it to tokens" matters beyond tidiness: a token can't carry divergent behavior, only a spelling. Maximize this surface.
- **By evaluation-equivalence (where it isn't):** the `lambda` case is per-language *logic*, not a token, so shared-code can't vouch for it. There you need the condition to carry a **language-neutral denotation** (a reference evaluator: `->` *means* a function, `if` *means* a conditional, `\` *means* a lambda), and a gate: render the tree to each target, evaluate it *in that target*, and assert every target's value equals the reference denotation. That's the cross-language analog of the round-trip you already trust. Semantic preservation *is* denotation preservation.

So the split preserves semantics iff the condition has a neutral denotation, and each target's action either **(a)** collapses to shared-function-plus-token, or **(b)** passes the eval-equivalence gate. What `SourceContractTest` checks today is *weaker* — each target reproduces its own hand-written reference renderer (a within-language drift guard). The missing piece is the *across-language* check. The boundary bites here too: this is clean for **expressions** — they denote values and functions, so they're evaluable — and murky for **declarations** — what does it mean for `trait X` + companion to "denote the same" as a Haskell `class`/`instance`? So semantic preservation is provable *first* at the expression layer, which is exactly where we're prototyping. One thing I want to pin before building it: by "indicate," do you mean this should be *encoded in the model* — the condition literally carrying its denotation as data — or is the eval gate the indication you're after?

---

**[Dev]** I'm looking to drake to carry some of the semantic load, in that I'm expecting the definition language can build domain ontologies via definition accrual.

**[Claude]** That reframes it, and it's a stronger answer than the external evaluator I was reaching for. You're saying meaning is *definitional*, not *operational* — a concept means what its accrued definitions say it means, the way a Lean library builds up: each definition grounded in prior ones, the corpus growing into an ontology where a term's meaning is its position in the definitional web. drake isn't just a second projection of the model; it's the surface on which that ontology accrues. That's the self-description principle taken to its conclusion — the system carries its own semantics rather than borrowing them from an evaluator outside it.

And it answers the either/or I posed cleanly: the "indication" is neither a denotation field on `Condition` nor an eval test — it's the **shared definitional ancestry** in the accrued ontology. Two targets' renderings of `->` mean the same thing because they descend from the *same drake definition* of `->`, not because their tokens happen to match or their evaluations happen to agree. Sameness is declared by common origin in the ontology.

Here's how I'd reconcile it with the semantic-preservation thread, honestly, because I don't think accrual *eliminates* grounding — it **concentrates** it, which is the real win. Definition accrual propagates meaning *downward* from composites to their parts: a composite concept is preserved across languages **for free**, because its composition is defined once in the ontology — a target can't diverge on how `\params -> body` is assembled if the assembly is an accrued definition, only on the leaf tokens. So the residue — the thing accrual *can't* ground definitionally — is the **primitive floor**: the irreducible concepts that bottom out the definitional regress. Those are the only ones a target must ground itself. Everything above the floor is preserved by ancestry.

That reframes the whole semantic-preservation burden: it shrinks from "verify every rendering across every language" to "ground the primitive floor per language; the ontology preserves the rest." The `lambda` wrinkle that resisted tokenization earlier is exactly a clue about where the floor is — if Scala's single-param-parens rule is genuinely primitive-structural, it's a floor concept the target must declare; if it can be *defined* in terms of more primitive concepts, it moves *above* the floor and becomes invariant. Finding that line is the actual design work. So the crux question: **what is the primitive floor for generation, and how does a target declare its grounding of it?** Where would you put `->` — is it a primitive the target spells, or is it definable in drake in terms of something more primitive, so that its cross-language sameness accrues rather than being asserted?

---

**[Dev]** The "->" is a term, which the reference to  a semantic value, not a semantic value itself, other than as an explicit reference in a specific context. All meaning is relative (from relations) between terms, and contextual (in a domain).

**[Claude]** That dissolves the primitive floor — there's nothing to ground because there were never any atoms with intrinsic content. A term is a reference; its meaning is its differential position among other terms (relative) within a domain (contextual). `->` doesn't *carry* meaning, it *points* — and it points to a relational role that only exists in a context. That's the categorical/structuralist stance: a thing is known only by its relations, objects have no interior. And it's what actually makes draco sovereign — a relational web is *self-closing*, whereas any intrinsic primitive would be a dependency on meaning imported from outside the system.

So semantic preservation stops being "ground the primitives" and becomes "**preserve the relations**." A target rendering means the same iff it holds the same relational position in the target domain that the source term holds in the source domain. Preservation = the projection is a **structure-preserving map**: relations in the source have images in the target. There's nothing to denote; there's a shape to carry across.

And that converges — I think non-accidentally — with the machinery already in the codebase. A structure-preserving map between domains is exactly `DomainTransform[S,T]`. The `ScalaSource → ScalaTarget` rename wasn't cosmetic; it positioned the language as the **target** of a transform whose source is the neutral model. Generation *is* a domain transform. The condition/action split from three turns ago is that transform's rules: the **condition references a term's relational role**, the **action is the transform's image of that term in the target domain**. "Make generate inferential" and "meaning is relational" are the same statement seen from two sides.

Which relocates the hard part honestly: preservation becomes *checkable* — "do the target terms hold the corresponding relations?" — but only to the extent the **target domain actually carries relational structure to check against**. Right now `ScalaTarget` is nearly empty: one global, no accrued relations. That's not a flaw in the view — it's the work the view identifies. Definition accrual in drake is precisely how the target domains *grow* the relational structure that makes the transform's preservation a real, verifiable homomorphism rather than a token table. Is that the right reading — that the generator should be modeled as a `DomainTransform` from the model domain to a language-target domain, with semantic preservation being relation-preservation across it?

---

**[Dev]** The semantics is determined by the value substitution rules as the source patterns are applied to the target syntax string templates. The strong type system of the target is what does most of the checking.

**[Claude]** Yes — and that collapses the abstraction into something already half-built. The substitution rules *are* the semantics: a source pattern matches and binds the values out of the model's relational structure, and the action drops those values into the target's syntax template. That's exactly the shape the prototype already has — `t.arrow`, `t.conditional`, `t.lambda` are target syntax templates with substitution holes, and the "condition" is the source pattern that binds what fills them. So the contract we prototyped *is* the substitution-rule form; what your point adds is the checker.

And the checker already exists in the pipeline. `GenerateAndCompileTest` and `RuntimeCompiler` generate code and compile it. That *is* "the target's type system does the checking," it just hasn't been named as the semantic-preservation gate. A substitution rule that violates the target's relational structure yields ill-typed code, and the target compiler rejects it. You get the verification for free by producing real target code and letting the target check itself — no external semantic verifier to build.

This is also why the multi-target / Lean direction compounds rather than just multiplies work: the strength of the check scales with the target's type system. Scala catches a lot; Haskell more; Lean most — a dependent type system can encode the relational constraints themselves. Each stronger target you add tightens the preservation check on the *same* substitution rules. The targets cross-check each other through their type systems.

The one line I'd draw, and you already drew it with the word "most": a strong type system catches **incoherence** — the substitution produced structurally-wrong code — but not **infidelity** — a substitution that type-checks yet isn't what the source meant, right shape and wrong value. The compiler verifies the *result* is coherent, not that the *rule* was faithful. That residual is exactly what the second projection covers: drake round-tripping to the same model checks fidelity where no target type system can. So the two sides close it — target type system for coherence of the output, drake for fidelity to the source. Does that match where you're headed, or are you drawing the coherence/fidelity split differently?

---

**[Dev]** It's interesting that you mention coherence because I view the existence of a rule to represent entanglement, and the execution  of rule to be decoherence.

**[Claude]** That lands the whole thread on a single axis. A rule, unfired, is a relation held in potential — a correlation over every context it *could* match, none of them actualized. That's coherence in your sense: the relational structure is intact and superposed, meaning-as-potential. Firing collapses it — the "whenever this, then that" decoheres onto specific facts, here and now, one branch of the potential made definite. And it makes my loose word literal: "coherence" isn't just *well-typed*, it's *entanglement intact* — the relations still hold, un-collapsed.

What makes it more than a pretty mapping is the design constraint it forces: **the transform must preserve entanglement — it must emit rules, never their executed outcomes.** If a substitution rule maps a source relation to a target *value* (a decohered fact), it collapses the potential in transit and the meaning is lost — you've shipped one branch instead of the correlation. To preserve semantics, the action must map relation → relation, rule → rule: the generated target must itself be entangled (a standing rule/definition), and decoherence must be deferred to the target's *own* runtime. The generator carries the coherent web across; the target collapses it later, in its context.

And that's exactly why the type-system check is the right static gate — it verifies the entanglement is intact *without firing*. Type-checking inspects whether the relations hold; it doesn't execute, so it doesn't decohere. That recolors the coherence/fidelity split precisely: the target type system checks **coherence** (the entangled structure survived the projection), and execution — eval, the round-trip — checks the **decohered outcomes** (when you *do* collapse it, the right facts fall out). Two views of the same object, before and after collapse.

It's also internally consistent with where the project already points. "RETE is performance, not necessity" — decoherence-mechanism is swappable; the entangled relational content is the engine-independent invariant. And definition accrual is the growth of the entangled web: each new definition adds relations, i.e. adds entanglement to the ontology. I'd resist over-literalizing — it's a structural isomorphism, not a claim that rules obey Bell inequalities — but it earns its keep because it *constrains*: generate rules not outcomes, check coherence without collapsing. So the sharp question it puts to the substitution rules: must every action be required to produce a target *relation* (a rule or definition that stays entangled), with any action that would emit a bare decohered value treated as a semantic error?

---

**[Dev]** Your "sharp question", though interesting, is diverting us from focussing on the practical matter of creating type definitions in the Drake domain for the rules needed to replicate the procedural use of functions and predicates in the procedural Generator in the rules-based Generator domain.

**[Claude]** Fair — I was circling. Back to the concrete work: authoring drake definitions for the rules that replace the procedural Generator's functions and predicates, targeting the `draco.generator.Generator` domain. *(Grounded in how a rule is actually authored in drake.)* The rule format is clear: `pattern` (variables + `con` conditions) → `action` (body). That maps directly onto what we need: procedural **predicate** (`op == "->"`) → rule **condition**; procedural **function** (render via template) → rule **action**; the value being dispatched on → the pattern's matched **fact**.

The expression renderer is the right first target — we already validated its condition/action split in `SourceContractTest`, so we're transcribing a proven shape, not inventing one. But authoring its rules forces the first real definition: **what fact do the rules match on?** The tree is raw `Json` today; rules need a typed node. That node type *is* the first ontology accrual. Here's the strawman I'd react to, in the `generator` domain:

```
type Expression
  elements
    fix op String
    fix operands [String]
domain draco generator Generator
```

```
type RenderArrow
domain draco generator Generator
rule
  pattern
    variables
      var e Expression
    conditions
      con e.op == "->"
  action
    = e.operands.mkString(arrow)
```

Two choices are baked into that strawman, and they're the real substance: **(1) The action is value-producing** (`= <expr>` yields the rendered string). The existing `Action` is Evrete-shaped — `RhsContext => Unit`, side-effecting via `ctx`. A generator rule instead *returns* its rendered fragment. So either `Action` grows a value-returning form, or generator rules insert a `Rendered` fact Evrete-style. I lean value-producing — it's what a transformation rule *is*. **(2) `arrow` is a reference into the target domain** — `ScalaTarget` supplies `" => "`, `Drake` supplies `" -> "`. That's your value-substitution: the rule is the source pattern, `arrow` is the hole, the target fills it. The invariant operators (`.`, `()`, tuple, infix) need no target reference at all — their rules are pure. If that shape is right, the natural first slice is `Expression` + the invariant-operator rules, then `RenderArrow`/`RenderConditional`/`RenderLambda` once we settle how a rule references a target token. Want me to write those first, or adjust the fact model / action shape before I commit files?

---

**[Dev]** Before we define any rules for the Generator, have to be able to bring the DracoDomainDictionary into memory so it can be validated.  The Draco domain itself has to be responsible for that, and we have to to create those rules before anything else. Assuming that draco has URI data access to the definitions,  It should be able to detect it's target language, load the definitions to create the DracoDomainDictionary,  and either regenerate or just validate the target source. Once this is done, the framework should be ready for a user to create a new domain dictionary or modify an existing one.

**[Claude]** That reorders the whole plan, and correctly — the generation rules operate *over* the dictionary, so the dictionary must first be loadable and self-validating, and that capability belongs to the Draco domain, not to a mods tool or the procedural Generator. *(Read `DomainBuilder` and the dictionary types.)* Now the picture's complete, and your ordering is right for a reason beyond sequence — it's also the easier engineering.

**This isn't greenfield.** `DomainBuilder.define`/`validate`/`generate` already implement exactly this, procedurally, and the file explicitly calls itself "a src/mods stand-in for an under-development draco core capability" whose promotion path is "moves from the mods draco tree to the main draco tree." So the task is: promote that capability into the Draco domain *as rules*, and it's the prerequisite the generation rules stand on.

**The honest split that avoids a bootstrap paradox:** you can't load-via-rules if the rules aren't loaded yet, so — **Load = kernel.** `TypeLoader` + `SourceContent` (URI access) already exist; `DomainBuilder.define` already walks a domain's members and loads each into a populated dictionary. That's the base case — procedural, and it's fine that it is. **Validate = rules.** The three checks in `DomainBuilder.validate` — self-declaration, completeness, derivation-resolvability — are *predicates*: conditions that fire on violation and report. That's the negative-lint shape you already use, and it's genuinely inferential.

Two things make validation the right *first* artifact: **(1) the facts already exist** — these rules match on `TypeDefinition`/`DomainType`/`TypeName`, all defined; unlike the expression rules, there's nothing to model first. **(2) They fit the existing rule machinery with no metamodel change** — a validation rule matches a fact and *inserts a `Problem` fact*, the same Evrete side-effecting shape the Primes rules use. A clean dictionary produces zero `Problem`s — literally `DomainBuilderTest`'s "validate reports no problems," now rule-driven. So the concrete first increment: **`Problem` + the three validation rules** (`SelfDeclaration`, `Completeness`, `DerivationResolvable`) in the draco domain. Target-language detection and regenerate-or-validate come *after* — they orchestrate around a dictionary that can already validate itself. Want me to draft them as drake definitions, transcribing `DomainBuilder.validate` check-for-check? And one thing to confirm on the fact model: is the member `TypeDefinition` the fact granularity you want, or should the domain be the fact the rules pattern on?

---

**[Dev]** Is it too soon to make Draco domain also an actor that runes these rules?

**[Claude]** Not too soon as the *destination* — Draco-as-actor running its own rules is the self-contained unit, and it's exactly the Dreams/Orion direction: a domain that receives a message, fires its rules, reports. In your own framing it's the decoherence agent — the rules are the standing entanglement, and the actor's `session.fire()` is what collapses them. So yes, that's where this lands.

One increment early to *author* it, though, for a concrete reason: an actor is the **deployment vehicle** for rules (Session per actor), not what proves them. The validation rules don't exist yet, so an actor would have nothing to run — and wrapping unproven rules in Pekko lifecycle means debugging two things at once. So the tight sequence, with the actor as the *very next* step, not a distant one:

1. **Author** `Problem` + the three validation rules (definitions).
2. **Prove them in a bare Session** — build the Knowledge, insert the `DracoDomainDictionary`'s `TypeDefinition`s as facts, fire, assert zero `Problem`s. That's a *permanent* test (the rules' correctness gate), not throwaway. Evrete's Knowledge/Session run independently of Pekko, so this needs no actor.
3. **Then give Draco an `actorAspect`** — the Draco actor receives a `Validate` (later `Load`, `Regenerate`) message and runs step 2 as its behavior. Now the actor wraps a *proven* core, and its own concerns are isolated from rule correctness.

The payoff of that ordering: step 2's test survives forever as the rule gate, and when a rule misfires later you'll know it's the rule, not the actor. Want me to draft `Problem` + the three validation rules now, with the Draco actor teed up right behind them?

---

**[Dev]** Yes

**[Claude]** Here are the drafts. I'll show them inline first because three real modeling choices are baked in and I'd rather you see them before I commit files. **`Problem`** — the validation-finding fact (elements `subject TypeName` + `message String`, with a matching factory). **`SelfDeclaration`** — transcribes `validate`'s check 1 (a domain must claim to be itself): fires when `domainAspect` is non-empty and `domainAspect.typeName.namePath != typeName.namePath`, inserting a `Problem`. **`Completeness`** — check 2 (a declared member must have real content, not a stub): four ANDed `con` lines, one per empty aspect, exactly `isStub`'s `&&`:

```
type Completeness
domain draco Draco
rule
  pattern
    variables
      var td TypeDefinition
    conditions
      con DracoAspect.isEmpty(td.dracoAspect)
      con DomainAspect.isEmpty(td.domainAspect)
      con RuleAspect.isEmpty(td.ruleAspect)
      con ActorAspect.isEmpty(td.actorAspect)
  action
    mon ctx.insert(Problem(td.typeName, s"member ${td.typeName.name} is declared but unauthored (no JSON on disk)"))
```

**`DerivationResolvable`** — check 3, and this is the one with a genuine fork: the direct translation puts a `TypeLoader.loadType` call inside the condition, and needs an `isStub` predicate that's currently private in `DomainBuilder`. The alternative is a **fact join** — insert ancestor TDs as facts too and match "member derives from ancestor ∧ ancestor is stub" — more Evrete-idiomatic, keeps conditions pure, but requires derivation edges as facts.

The three decisions, so you can steer: **(1)** fact = `TypeDefinition`, uniformly — one fact-set feeds all three rules; **(2)** actions insert `Problem` facts — the Evrete-native shape, no metamodel change, zero `Problem`s after firing = valid; **(3)** `DerivationResolvable`'s load-in-condition vs join shape. My recommendation: commit `Problem` + `SelfDeclaration` + `Completeness` now (the two clean single-fact rules), prove them firing in a bare Session against the real dictionary, and settle `DerivationResolvable`'s shape as its own small step — rather than let the hardest rule set the pattern for the easy two. Should I write those three to files and wire them into the Draco domain, holding `DerivationResolvable` for the follow-up?

---

**[Dev]** Proceed as you suggest.

**[Claude]** Approved. Let me get the JSON structure exactly right by reading an existing rule and a simple type before authoring. *(Read the Primes rule JSON.)* Grounding in the real rule JSON caught a bug in my own `SelfDeclaration` draft before it shipped — worth flagging honestly. A rule's `domainAspect.typeName` points at its **domain**, not itself (Primes' rules carry `domainAspect: Primes`). And every *member* likewise carries `domainAspect → its domain` — exactly the self-loop that discriminates domain from leaf. So `SelfDeclaration` as I wrote it — "fire when `domainAspect` non-empty and doesn't self-loop" — would false-positive on **every member**, since members legitimately don't self-loop. It needs a **domain-role fact**, not a bare `TypeDefinition`, which is the fact-granularity question I'd flagged, now with teeth.

So I'm narrowing this commit to the genuinely clean one: `Problem` + `Completeness`. A stub is a stub regardless of role — all-aspects-empty is unambiguous on any `TypeDefinition`, no domain/member distinction needed. That proves the whole mechanism (fact → condition → `Problem` action) with zero fact-model risk. `SelfDeclaration` and `DerivationResolvable` both follow, once the mechanism is proven. *(Wrote `Problem.json`/`.drake` and `Completeness.json`/`.drake`, wired both into the Draco member list.)* All four resource files written and wired. Generate the Scala and run the gates:

```bash
sbt assembly
bin/draco-gen generate src/main/resources/draco/Problem.json > src/main/scala/draco/Problem.scala
bin/draco-gen generate src/main/resources/draco/Completeness.json > src/main/scala/draco/CompletenessRule.scala
bin/draco-gen generate src/main/resources/draco/Draco.json > src/main/scala/draco/Draco.scala
sbt test
```

Note `Completeness.json` → **`CompletenessRule.scala`** (the Generator appends "Rule" to a rule's name, as with `AddNaturalSequenceRule`). What I'm watching, in order of risk: **(1)** `CompletenessRule.scala` compiles — the first hand-authored Evrete rule with host-string conditions calling `draco.*Aspect.isEmpty`; **(2)** `DrakeGenTest` on the two hand-authored `.drake` files; **(3)** `DomainBuilderTest`'s "Draco — validate reports no problems" with the two new members. Once green, the next step is the **Session proof** (no actor yet): zero `Problem`s on the real dictionary, one `Problem` when a deliberately-stubbed member is inserted.

---

**[Dev]** What reason do you  have for putting "Rule" in the file name when rule is only one of five aspects?

**[Claude]** Honestly — no principled one. I appended it because the Generator still enforces it and the tests expect it, not because I judged it correct. On your challenge, it doesn't hold up, and it's worth naming precisely what it is.

It's the **last vestige of the `.rule`/`.actor` name-suffix holdover that #40 set out to remove.** That commit ("rule-ness is aspect presence") de-suffixed the JSON filenames and de-suffixed *actors'* Scala — actors now generate bare — but it deliberately left the *rule* Scala suffixed. So the principle "aspect-ness is presence, not name" was adopted and then applied to four of the five aspects and to actors, with rules as the one unfinished corner. You're pointing straight at it. And the inconsistencies are real:

- **Asymmetric across aspects** — only `rule` gets a suffix; `domain`, `actor`, `codec`, `draco` don't. There's no structural reason rule is special.
- **Member-name ≠ class-name** — I just added `"Completeness"` to the Draco member list, but the class generates as `CompletenessRule`. Actors keep member == class; rules don't round-trip by name.
- **Worse for a noun** — the suffix half-reads on imperative names (`AddNaturalSequence` → `AddNaturalSequenceRule`), but `Completeness` is already a noun naming a validation concept, so `CompletenessRule` is just redundant.

So the fix is to **finish #40**: drop the suffix in the Generator so rules generate bare, like actors. Blast radius, so you can weigh it: `Generator.ruleGlobal` (drop the `+ "Rule"`), `DracoGenTest`'s rule → `scalaPath` mapping, `PrimesRulesTest`'s "Generate" expectations, and regenerate + rename every existing rule's Scala — the 3 primes rules plus the mods `*ReportRule` rules, and any references to them. Mechanical, but cross-cutting. My recommendation: do it as its own small increment *before* we lean further into rules — it completes a principle you already adopted, and it cleans up `Completeness` in the same stroke rather than baking the vestige into the new validation work.

---

**[Dev]** I had thought that we already fixed this in an earlier session. You should look in the draco-dev-journal latest 2 or 3 chapters to figire out how we missed it.

**[Claude]** *(Launched an agent to search journal chapters 63–65 for the suffix history; meanwhile checked the current code state directly.)* Confirmed in the current code, so this isn't a stale-file illusion — the suffix is genuinely still applied: `Generator.scala:1426` (`val name = td.typeName.name + "Rule"`) and again at 1795 for the rule-branch, and the files on disk are `AddNaturalSequenceRule.scala`, `PrimesFromNaturalSequenceRule.scala`, `RemoveCompositeNumbersRule.scala`. Whatever happened earlier, it isn't in effect now.

*(The journal-search agent returned: the class-name suffix removal was NOT done in chapters 63–65. #40 — filed in ch.64, implemented in ch.65 — removed the `.rule`/`.actor` resource/file-name suffix and switched detection to aspect-presence, but explicitly left the generated Scala object names unchanged.)* Traced it. You didn't miss a bug — you're remembering a *real* fix, but it was a different suffix that shares the name. What landed in chapter 65 was **#40**, and it removed the `.rule`/`.actor` **filename/typeName** suffix. The generated Scala **class/object** suffix was explicitly left alone. The load-bearing line from ch.65:

> "Generated rule `.scala`: `loadRuleType`→`loadType`, `.newRule` string de-suffixed (object names/whereConditions unchanged)."

"object names ... unchanged" is the exact carve-out. So my earlier "last vestige of #40" was slightly off, and the journal corrects it: it's not a half-finished #40, it's the **unstarted extension of #40's principle to the class-name level.** The principle ("presence, not name") was adopted and applied to filenames and detection; applying it to the generated class names is the piece that was never done — and the ch.65 note is essentially a breadcrumb that the earlier session knew it was stopping at the resource level. Which makes the case to finish it now stronger, not weaker: you already ratified the principle; this just carries it the last step. Want me to take that on as its own increment now — completing #40 at the class-name level — and then return to `Completeness` (which then generates bare as `Completeness.scala`)?

---

**[Dev]** Please do.

**[Claude]** On it. This is a cross-cutting sweep, so let me first map every spot the suffix is assembled or expected before editing. *(Mapped all four Generator assembly points — the object name at 1426, the `whereConditions` FQN base at 1451 that must move in lockstep or the Evrete `.where(...)` string points at a nonexistent object, `actorKnowledge`'s `${r}Rule` reference at 1519, and the generate-branch trait name at 1795 — and edited them plus the now-false comment. Then the tests: `DracoGenTest.deriveScalaPath` collapsed to no rule special-case; `PrimesRulesTest`'s three sink paths and object references. Mapped every remaining reference — generated actors in the 4 mods domains, `AerialGenTest`/`AerialActorTest` — and applied a surgical sed + file renames: 3 primes rules, 8 mods `*Report` rules renamed to bare. Caught the `TupleFact` test fixture too — `TupleFactReteTest` regenerates it via `Generator.generate`, so it needed the same de-suffixing. Verified no dangling suffixed references anywhere; corrected the stale `project_generator_loading` memory.)*

Working tree is fully consistent — no dangling suffixed references, the Generator no longer assembles `+ "Rule"`, and every rule file is renamed to its bare concept. The sed'd content equals exactly what the fixed Generator now emits, so the gates should accept it. To build and verify, plus generate the still-pending `Problem`/`Completeness`/`Draco`:

```bash
sbt assembly
bin/draco-gen generate src/main/resources/draco/Problem.json > src/main/scala/draco/Problem.scala
bin/draco-gen generate src/main/resources/draco/Completeness.json > src/main/scala/draco/Completeness.scala
bin/draco-gen generate src/main/resources/draco/Draco.json > src/main/scala/draco/Draco.scala
sbt test
```

`Completeness.json` now generates bare as **`Completeness.scala`** — the whole point of the change. This finishes #40's principle at the class-name level: rule-ness is aspect presence, not a name suffix, now true of the generated Scala too. Two follow-ups worth noting: **DRACO.md is now stale** — it still documents "Generator appends 'Rule'" — want me to update it, or will you? And once the suite is green, we're back where we paused before the suffix detour: the **Session proof** for `Completeness`, then `SelfDeclaration` with the domain-role fact, then the Draco actor.

---

**[Dev]** Update DRACO.md

**[Claude]** *(Found the three stale spots, read the context, made two edits, confirmed no other stale suffix references remain.)* DRACO.md is updated. Two edits: the **"Rule name suffix"** subsection is now **"Rule names are bare (no suffix)"** — describes rules generating as bare objects like actors, notes `ruleGlobal` uses `td.typeName.name` directly, and carries a short parenthetical on the history (last remnant of #40's holdover, removed 2026-07-22). And the **Primes domain** description now lists the bare object names (`AddNaturalSequence`, etc.). The other `Rule` mentions that remain are all still accurate. The doc, memory, and code are now consistent on this. Once you run the build block and it's green, we pick back up at the `Completeness` Session proof.

---

**[Dev]** After you check the results of your run block, tell me where we should continue in the next session:

*(Pasted the run — `sbt assembly` + the three generates + `sbt test`: 299/299, all pass; the `ExampleDomainsGenTest` report showing "8 missing".)*

**[Claude]** 299/299 green. Let me confirm the key wins and check one anomaly in the report before I lay out the continuation. *(Checked `ExampleDomainsGenTest` line 39 — its path-deriver still appended `Rule.scala`, so after the rename it can't find the 8 mods rules; report-only, so nothing failed, but misleading. Fixed it — no rebuild needed. Wrote the continuation state to memory as `project_draco_self_validation`, flagged ACTIVE.)*

The verification checklist:

- **`Completeness.scala` compiled and fired through the gates.** This was the real unknown — the first hand-authored Evrete rule with host-string `draco.*Aspect.isEmpty(...)` conditions. It parses, matches the emitter's `.drake`, matches generated Scala, and *compiles*. The rule mechanism works end to end.
- **`Problem` + `Completeness`** are green on all three gates and now Draco members.
- **Suffix removal is complete and consistent** — every renamed rule matches the bare Generator output, `TupleFact` shows bare in the test list, and `DomainBuilderTest`'s "Draco — validate reports no problems" still passes with the two new members.
- **The one anomaly** — `ExampleDomainsGenTest`'s "8 missing" — was my loose end (its path-deriver still appended `Rule`); I just fixed it, no rebuild required, it'll read `0 missing` next run.

**Where to pick up next session, in order:**

1. **The `Completeness` Session proof** — the immediate next increment. Compiling isn't firing: build a Knowledge from `Completeness` (bare — `Completeness.ruleType.pattern.accept(k)`), insert the `DracoDomainDictionary`'s member `TypeDefinition`s, `fire()`, and assert **zero `Problem`s on the real dictionary + one on a deliberately-stubbed member**. Bare Evrete Session, no actor yet. This becomes a permanent test.
2. **`SelfDeclaration`** — with the corrected **domain-role fact** (the fix for the false-positive: members legitimately carry `domainAspect → their domain`, so it can't match a bare `TypeDefinition`).
3. **`DerivationResolvable`** — settle its load-in-condition vs fact-join shape.
4. **Promote to a Draco actor** — `actorAspect` on `Draco`, receiving a `Validate` message that runs the rules on its Session. The actor wraps the proven core; it's the destination, sequenced after the rules fire clean.

Held in parallel (deferred behind this track): the `SourceContract` expression-rendering prototype, ready to fold onto `draco.Source` when the validation work reaches a pause. Good stopping point — everything's green, and the next move is a focused, well-scoped test.

## Status
The session ran July 21–22 and closed at **299/299 green** (278 → 287 → 290 → 293 → 299 across the arc), with everything landed in the working tree — no commits or pushes are recorded in the session data (Dev commits via the IDE). It opened as a drake-parser options discussion (#46/#44): the survey established that drake's *declaration* layer is settled (64 exact-match tests, brackets token-locally checkable) while the *expression* layer is explicitly future in `drake.dlt` — then Dev inverted the plan entirely: freeze drake, convert the Generator from a tool to a domain. That conversion became the session's structural spine: **`draco.drake.Drake`** landed as a capability-domain peer of `ScalaSource` (self-gated by the discovery walks, zero test edits), the `CLI drake` command demonstrated two capability domains substitutable behind one `TypeDefinition => String` slot, and **`draco.generator.Generator[L]`** — the first type both parameterized and self-domained (`Domain[Generator[_]]` emitted correctly, no emitter change) — completed the triad. The gating problem (the emitted bare `Generator.loadType` self-shadowing inside `object Generator`) was dissolved by Dev's conceptual correction — `loadType` is a utility, not a Generator concern — yielding **`draco.TypeLoader`** (authored as JSON/drake, landed green on both gates first try) and ending the layering inversion where `src/main`'s root domain depended on a `src/mods` tool to load its own definition. Enablers along the way: the `generate-multi` GeneratorCLI subcommand (method `runGenerateMultiple`), a latent leading-newline emitter defect fixed (generator output now byte-exact against the corpus, making the `loadType` sed sweep a pure one-line-per-file diff across ~56 main + ~48 mods files), and `ExampleDomainsGenTest`'s report restored to informative (30 match / 18 differ). The second movement built the language-abstraction ladder: **`draco.Source`** as the neutral supertype (`ScalaSource <: Source`), then Dev's rename directive — `<language>Source → <language>Target`, since "Source" collides with the transform vocabulary — over-applied by Claude to the neutral supertype and corrected by Dev: **`draco.Source` stays; `ScalaSource → draco.scalatarget.ScalaTarget`** (`trait ScalaTarget extends Source`, tension noted). The `SourceContract` prototype (shared `ExpressionRenderer` engine + three per-language template slots, validated by `SourceContractTest` against both production renderers) proved Dev's substitution-template thesis in the expression layer. The session's intellectual centerpiece is the **"CO in DRACO" dialogue** (July 22): conditions/actions as the inferential form of generate; semantics-as-substitution-rules with the target's strong type system checking coherence and drake round-trip checking fidelity; definition accrual building domain ontologies; Dev's meaning-is-relational thesis (terms as references, meaning relative and contextual — generation as a relation-preserving `DomainTransform`); and the entanglement/decoherence framing (rule existence = entanglement, execution = decoherence) — closed by Dev's refocus directive back to practical rule definitions. Dev then reordered again: before Generator rules, the **Draco domain must load and validate the DracoDomainDictionary itself** — load stays kernel (`TypeLoader`/`SourceContent`), validation becomes rules transcribed from `DomainBuilder.validate`. **`Problem`** (the validation-finding fact) and **`Completeness`** (the first hand-authored Evrete rule — all-aspects-empty → insert `Problem`) landed green; `SelfDeclaration` was caught pre-ship as a would-be false-positive (needs a domain-role fact) and deferred with `DerivationResolvable` (load-in-condition vs fact-join). Dev's challenge to `CompletenessRule.scala` exposed that #40 was finished only at the resource level — the journal search (ch.63–65) confirmed the generated-class suffix was #40's explicit carve-out — and the sweep completed it: Generator's four assembly points de-suffixed, 3 primes + 8 mods rules + the `TupleFact` fixture renamed bare, tests and path-derivers collapsed, DRACO.md and memory synced. Issues touched but not closed in-session: #42 flagged closeable, #46 re-scoped by the pivot, #44 deferred against a frozen grammar; #43's codec tripwire stays armed. **Next-session pickup** (recorded in memory as `project_draco_self_validation`, ACTIVE): (1) the `Completeness` Session proof — bare Evrete Session over the real dictionary, zero `Problem`s + one on an injected stub, as a permanent test; (2) `SelfDeclaration` with the domain-role fact; (3) `DerivationResolvable`'s shape; (4) the Draco actor (`actorAspect` + `Validate` message) wrapping the proven core — with the `SourceContract`-onto-`Source` fold held in parallel. Still queued from ch.65: the presence model for present-empty metamodel actor heads, the two lints carried from ch.61, and #46 part 2 (absorbed into ScalaTarget's business once projections are domains).
