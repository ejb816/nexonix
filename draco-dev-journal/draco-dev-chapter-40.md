# Draco Dev Journal — Chapter 40

**Session date:** June 1–3, 2026
**Topic:** The first `src/mods` usability/scalability stand-in — `draco.DomainBuilder` — and everything its existence forced into the open: the hollow `TypeDictionary` gap, a two-track build model where `src/mods/scala/draco` compiles *into* root, a rigorous `validate` that immediately caught a real defect, the relocation of `Generator`/`GeneratorCLI` out of the JSON-backed type system, a multi-round sbt 1.7.3 → 1.12.9 upgrade whose first fix was wrong, and the v2.0.0-alpha.5 release closeout. Transcript.

---

**[Dev]** We now need to focus on early versions of usability and scalability features in the draco framework, to support the developers of applications and services using draco as the core binary dependency. Usability in this case means allowing domain experts/workers to build fully functional applications, services and tools, without  knowing anything about specific programming languages or DevOps. The draco framework, as it exists now, is just the core, with dreams the next outer layer, for user development, and orion as the third layer proving support for configuration, initialization and deployment for whatever the user builds using draco and dreams. Scalability in this context has two dimensions, functional and performant. The functional axis refers to the framework's inherent support of indefinite and incremental domain and type extensibility, applicable to both domain containment and type inheritance.

The early versions of usability and scalability features, will first be developed in the src/mods subproject of the draco framework project. This provide essential early access to features that that will end up as part of both the dreams and orion layers of the framework. That means that collaboration with framework users can begin immediately, and that early access users can help refine and stabilize the dreams an orion features before the are moved to the framework outer layers canonized in src/main of the released framework.

**[Claude]** *(after grounding in `src/mods`, `bin/`, and the README)* Here's where things actually stand, and what I'd build first. Today the mods tier is **read-only introspection only** — six scala-cli scripts (`inspect-type`, `derivation-chain`, `list-domain`, `list-domains`, `who-extends`, `diff-type`) that *observe* the running framework. There is **no `draco.dreams` or `draco.dreams.orion` code yet** — the policy memo reserves those namespaces, but the tree is just `scala/scripts/`. So mods is effectively greenfield with a clean constraint already in place: *no new third-party deps* (anything mods needs that draco can't provide is a signal core is incomplete). The introspection scripts are the *observer* half of usability; missing is the *author* half.

What I'd build first (one recommendation): **seed `draco.dreams` with a single vertical authoring slice — "describe → scaffold → generate → verify" — driven entirely from YAML and a guided CLI.** A `dreams new-type` / `new-domain` flow: plain-language-ish YAML stub → canonical `.json` via `from-yaml` → Generator emits Scala → round-trip through `Generator.loadType` + `diff-type` to *prove the artifact is real and loadable* — reported in domain-expert terms, not compiler terms. It exercises both axes in one stroke: a non-programmer authors a functioning type end-to-end, and the only operation it performs is "add one more type" — indefinite/incremental extensibility made into a tool. Orion layers on naturally *after* there's an artifact worth deploying. Before I design or scaffold anything, one alignment check — the answer changes what I produce next. *(session data gap — the alignment question itself is not in the extract)*

---

**[Dev]** Before addressing dreams and orion specifically we need to use src/mods to create user callable functions that will be called in place of draco features that are sill under development, like rules with patterns and actions that will be associated with the Generator domain that does not exist yet.

**[Claude]** This reframes it well — before dreams/orion, mods should host **user-callable stand-in functions** that occupy the slot of draco features not yet built. *(after reading the generator-evolution/permanence memos and the rule dispatch)* The thing you're describing as "the Generator domain that doesn't exist yet" is, right now, the imperative dispatch inside `draco.Generator.generate(td)`:

```text
if (isRule(td))        → ruleImports + ruleGlobal
else if (isDomain(td)) → typeImports + domainGlobal
else if (isObjectOnly) → objectGlobal
else if (isLeaf||isActor) → unified trait + global
```

Each branch **is** a rule: the classifier is the **pattern**, the emission template the **action**. In the future `draco.generator.Generator` domain (#11) these become first-class draco rules over `TypeDefinition` facts. So the first mods increment is a faithful stand-in: lift that dispatch into public, user-callable, rule-shaped form — explicit `(pattern, action)` pairs as plain `TypeDefinition`-typed functions (deliberately **not** `RuleType`/`Consumer[Knowledge]` — those are Evrete-specific and wrong for generation rules), with patterns reimplemented from public aspect fields so the classification is *addressable*. When the typed Generator domain lands, the stand-in retires — clean promotion path. One thing genuinely yours: the namespace — I'd propose `draco.pending.GeneratorRules`, or name what you want.

---

**[Dev]** I was just using Generator as an example, but that comes after some other functions I've yet to mention.

**[Claude]** Got it — Generator is downstream. I'll hold that thought and not scaffold anything yet. What I'll carry forward as the stable frame, independent of which function we start with: **`src/mods` hosts user-callable stand-ins** for draco features still under development — real, callable functions now, with a clean promotion path into `main` when the underlying feature lands; the **no new third-party deps** constraint stays; and these are **callable library functions**, distinct from the read-only `scripts/` toolkit. What are the functions that come first?

---

**[Dev]** The ability to comprehensively define a domain dictionary with just JSON data and be able to create concrete instance of it and generate code from it, even if it is the stubbed skeleton.

**[Claude]** *(after reading the dictionary types and `Primes.json`)* That's concrete, and exploring the dictionary types surfaced exactly why it needs a stand-in. A domain *is* already comprehensively defined in JSON: `Primes.json` carries `domainAspect.elementTypeNames` plus the per-member JSONs. The **data** side is there. But the **concrete-instance** side is hollow: `TypeDictionary.apply(domainDefinition)` builds its members as `elementTypeNames.map(name => TypeDefinition(TypeName(name, namePackage)))` — the *empty* constructor, all aspects `Null`. It never calls `Generator.loadType`. So a concrete `TypeDictionary` holds member **names but not member content** — you can't generate code from it. That's the "feature still under development" the stand-in fills.

Proposal — a mods library object with three composed, user-callable functions: **`define(domainName, namePackage): DomainType`** (loads the domain def, then *every* member's full `TypeDefinition`, returning a `Domain` with a **populated** `typeDictionary` — the non-hollow counterpart to `TypeDictionary.apply`); **`dictionary(domains*): DomainDictionary`** (assembles the cross-domain registry); **`generate(domainName, namePackage): Map[TypeName, String]`** (runs `Generator.generate` over each populated member, robust to incomplete members — a compilable **skeleton** for anything thin, per your "even if it is the stubbed skeleton"). All composed from draco's **public** API, no new deps. I'll surface the hollow-`TypeDictionary` finding as a core issue too, rather than silently working around it. What package should I scaffold this under?

---

**[Dev]** Since it going to become part of draco it should be src/mods/{resources,scala}/draco for conflict detection and resolution

**[Claude]** Understood — `package draco` in `src/mods/scala/draco/`, mirroring `src/main/scala/draco/`, with `src/mods/resources/draco/` as the resource peer. Same package name across the two compilation units means sbt and the IDE flag any duplicate FQN, so a stand-in can't silently diverge from — or collide with — what it's destined to become. At promotion the file just moves from the mods `draco` tree to the main `draco` tree and the conflict (if any) resolves in place. Last decision: the object name. My recommendation: **`DomainBuilder`** — `draco.DomainBuilder`, with `define` / `dictionary` / `generate`. Reads as a core-worthy peer to `Domain`/`DomainDictionary`/`Generator`, and doesn't collide with any existing `draco.*` name.

---

**[Dev]** I like DomainBuilder. I'll put in a plug for adding TransformBuilder as well, when you're ready to do that.

**[Claude]** `DomainBuilder` it is, and I'll pencil in `TransformBuilder` as the sibling stand-in for the transform path. *(confirmed loader/TypeName/DracoType signatures, then wrote `src/mods/scala/draco/DomainBuilder.scala`)* Three composed, public-API-only functions — `define` (populated dictionary via `Generator.loadType` per member), `dictionary` (cross-domain registry over defined domains), `generate` (Scala for the domain + every member, skeleton-tolerant with a marked placeholder if any one member fails so the batch survives) — plus a public `isStub(td)` so callers can tell named-but-unauthored members apart. Verification path is yours: `sbt mods/compile`, then exercise from `sbt mods/console` — note `bin/draco-sc` runs against the *jar* and won't see mods classes. Two follow-ups on your go: file the hollow-`TypeDictionary` finding as a core issue, and add a "library stand-ins" section to `src/mods/README.md`.

---

**[Dev]** Can you write a test for DomainBuilder that "builds" the draco domains?

**[Claude]** *(after reading build.sbt)* The `mods` subproject has **no test framework by design** — build.sbt enforces "no new third-party deps of its own," so there's no ScalaTest there, and the root test config can't see mods classes (root has no dependency on mods; wiring one would be a project cycle). So the faithful "test" for this tier is a runnable verification harness matching the scripts' convention (assertions + exit codes), run via `sbt mods/runMain`.

---

**[Dev]** Why can't you write the test in src/test?

**[Claude]** You're right to push — let me correct myself, because I conflated two things. The test framework isn't the blocker (root's `Test` already has ScalaTest). The real obstacle is the sbt **project graph**: `src/test` compiles in the `root` project; `DomainBuilder` compiles in the `mods` project; for a test in `src/test` to reference it, `root` must depend on `mods` — but `mods` already `dependsOn(root)`. `root → mods` plus `mods → root` is a mutual project dependency, and sbt rejects cyclic project dependencies outright. That's the actual "can't" — not the framework, and not the package.

Two cycle-free ways to still get the test into `src/test`: recommended — inject mods's compiled output into root's Test classpath via a setting (`root / Test / internalDependencyClasspath += Attributed.blank((mods / Compile / classDirectory).value)` plus a compile ordering dependency) — a settings-level reference, not a project edge, so no graph cycle. The alternative — giving `mods` its own `Test` config — bumps against its documented "no new third-party deps." Since this touches build.sbt — your domain — do you want me to make that wiring edit, or would you rather wire it yourself and I just write the test file?

---

**[Dev]** I should only need the mods subproject for writing and running scripts. Does that help the issue your describing?

**[Claude]** Yes — that resolves it cleanly, and it reframes the whole thing correctly. The cycle only existed because I let `DomainBuilder` be compiled by the `mods` *subproject*. If that subproject is just the home for **scripts**, then `DomainBuilder` shouldn't belong to it at all. The clean two-track model:

- `src/mods/scala/scripts/` → the `mods` subproject (scala-cli scripts, consume the jar). Scoped to scripts only.
- `src/mods/scala/draco/` → **compiled by `root`** as a staging source directory. So `DomainBuilder` lands in the draco jar (exactly what early-access users need), conflict-detection against `src/main/scala/draco/` becomes a hard same-project compile error, and `src/test` can test it natively — **no cycle, no cross-project wiring.**

*(made the two build.sbt edits — root's `unmanagedSourceDirectories` gains the staging dir, `mods` scoped to `scala/scripts` — and wrote `src/test/scala/draco/DomainBuilderTest.scala`)* The test is an `AnyFunSuite` in `package draco` that builds Draco / Base / Primes / Language. Per domain it asserts `define` returns a self-declaring domain whose `typeDictionary` is **populated** — member count matches the JSON's `elementTypeNames`, members keyed correctly, and **none are stubs** (the crux: precisely what distinguishes `DomainBuilder` from the hollow `TypeDictionary.apply`) — and that `generate` emits non-empty, package-declaring Scala for the domain object plus every member. Plus a cross-domain `dictionary` test. One judgment call to flag: the **no-stub** assertion doubles as a data-hygiene check — if any canonical domain's JSON declares a member with no file on disk, that test fails with the offending name listed. That's a real, informative failure, not a `DomainBuilder` bug — but tell me if you'd rather the test tolerate stubs.

---

**[Dev]** I don't see a development related problem with allowing the DomainBuilder to rigorously test the draco endogenous domains. Any problems with that should be fixed sooner rather than later. What are your suggestions about that?

**[Claude]** I agree — and there's no contradiction to resolve, just a clean separation of concerns: **the capability stays skeleton-tolerant** (`generate` still emits a skeleton for an unauthored member, because a *user's* in-progress domain legitimately has holes — the usability promise), while **the shipped endogenous domains should have no holes**, so the *test* asserts zero tolerance is needed and any failure is a real defect to fix now.

My main suggestion: **make the rigor a reusable capability, not just test assertions** — add `DomainBuilder.validate(domain): Seq[String]` returning structured problems, and have the test assert it's empty per domain. Three payoffs: the test gets concise, a `validate` script becomes trivial, and — most importantly — *validation is itself part of the usability mission*: a domain expert authoring in JSON needs "is my domain well-formed?" feedback without touching a compiler. Proposed check battery, prioritized: (1) **completeness** — no member is a stub; (2) **derivation resolvability** — every member's derivation TypeName loads; (3) **declaration↔discovery parity** — declared `elementTypeNames` == the `.json` files actually present (the strongest check, but needs a classpath resource scan); (4) **codec round-trip**. I'd build **1 + 2 now**, run against the four domains, and fix whatever surfaces. I'd expect findings concentrated in **Language** (recent churn) and possibly a declaration/file mismatch in one of the larger domains. Want me to add `validate` with checks 1–2 and wire the test to it?

---

**[Dev]** Yes

**[Claude]** On it — `validate` (self-declaration + completeness + derivation resolvability, external supertypes skipped) added to `DomainBuilder`, and `DomainBuilderTest` rewritten to assert `validate(...).isEmpty` per domain alongside the populated-dictionary and generate checks. Per the project rule I don't run sbt myself — run `sbt "testOnly draco.DomainBuilderTest"`, with a `reload` first since the build changed. Paste the output — especially any `validate` failures, which list the offending member/derivation by name — and I'll triage each one. My bet is Language surfaces something.

*(Delegated action — Dev ran it, noting a missed `reload` on the previous run: 12/13 green, and the single failure was the validator working as intended — not in Language but in Draco itself:)*

```
- Draco — validate reports no problems *** FAILED ***
  List("member Generator is declared but unauthored (no JSON on disk)") was not empty
```

*(after reading `Draco.json`, `Generator.scala`, and `DracoGenTest`)* The validator caught a real one, and it's specific: `Draco.json` declares **42 members**; `Generator` is the **only one without a JSON**. Its `TypeDefinition` is still **inlined in `Generator.scala`** — the lone holdout of the 2026-04-15 inline-TD elimination. Its peers (`CLI`, `REPL`, `Main`, `Test`, `Value`, `SourceContent`, `ContentSink`) are the same kind of thing yet each *has* a JSON and is a proper member. And the investigation changed my recommendation: `DracoGenTest.comparisonOnlyExcluded` is **`Map.empty`** — that emptiness is the deliberate outcome of #29 ("no hand-written customizations remain on type declarations"). Two honest fixes, pulling opposite ways:

- **A — Make `Generator` a real JSON-backed member.** Extract the inline TD to `Generator.json`. *But* the JSON shape would never match the huge hand-written engine, forcing a `comparisonOnlyExcluded` entry and **re-opening the category #29 just closed.**
- **B — Stop claiming the engine as a dictionary member.** Remove `"Generator"` from `Draco.json`'s `elementTypeNames`. One line, no bootstrap touch, no #29 regression; the *typed* generator arrives later as `draco.generator.Generator` (#11).

I lean **B**. Holding — I won't touch `Draco.json` or `Generator` until you say which way. Where things stand: `DomainBuilder` + `validate` are in place and the test runs 12/13, with the single failure being exactly the real finding we wanted it to catch.

---

**[Dev]** What do you think about putting Generator in its current form, into src/mods exclusively?

**[Claude]** Good question to probe — but let me ground the answer in who actually calls Generator. *(grep across the tree)* The grep makes the answer concrete, and it's a "no" for the whole object — but a meaningful "yes" for part of it. Generator is really two things fused, with opposite tiers:

- **The loader** (`loadType`/`tryLoad`/`loadFromResource`/`loadAll`) — called by **50 src/main files**: `DracoType`, `Primal`, every aspect, every dictionary, all of `base/`, `language/`, `primes/`. It's the bootstrap spine. This **cannot** live in mods: that's `main → mods`, which the layering forbids outright.
- **The emitter** (`generate`/`compile`/`compileMulti`) — called by **nobody in core runtime**; only `GeneratorCLI`, tests, the mods scripts, and `DomainBuilder`. This half genuinely *is* mods-tier tooling.

So "Generator in its current form → src/mods exclusively" is not feasible; the feasible version of your instinct is a loader/emitter split. My recommendation: keep these as two moves — clear the **finding** now with the cheap Fix B, and treat the split as a separate, deliberate initiative. What's your read?

---

**[Dev]** We kind of crossed wires here. I just interrupted you by suggesting that instead of making an exclusion as being in draco, remove it from src/main/scala/draco and put it permanently in src/mods/scala/draco without a JSON type definition anywhere (main or mods) and removing it from elementTypeNames in Draco.

**[Claude]** That's actually a cleaner idea than my Fix B — and the key realization is that it *works without breaking those 50 callers*, because of the wiring we just put in. `src/mods/scala/draco` compiles **into root** (the staging `unmanagedSourceDirectories`). So moving `Generator.scala` there keeps `draco.Generator` in the same root output — all 50 `Generator.loadType` callers and every test still resolve it. It's a source-location move, not a tier/artifact change, so there's no cycle and no `main → mods` violation.

One correction to my own framing after checking references: `src/main/scala/draco` won't be *uniformly* JSON-backed (`Codec`, `Generated`, and `GeneratorCLI` also lack JSON twins). The accurate, defensible principle your move enforces is narrower and exactly the one `validate` checks: **a declared Draco domain member must be JSON-backed.** `Generator` was the sole violator. Evidence confirms it's safe: nothing external references Generator's vestigial self-model, and `GeneratorCLI` is the same kind of thing (no JSON, not a member, depends on `Generator.generate`) — the engine's CLI front-end, logically the same tooling tier.

*(Executed: `git mv src/main/scala/draco/Generator.scala src/mods/scala/draco/` — history preserved, engine body untouched — and removed `"Generator"` from `Draco.json`'s `elementTypeNames`, 42 → 41.)* No build.sbt change this time, so no `reload` needed — `sbt "testOnly draco.DomainBuilderTest"` should be 13/13, and a full `sbt test` confirms the 50 callers.

*(Delegated action — Dev ran the tests: `DracoGenTest` failed on Draco — the generated `Draco.scala` and the hand-written one differ by exactly the `"Generator"` entry in `elementTypeNames`.)*

Expected coupling — the hand-written `Draco.scala` keeps its own `elementTypeNames` list mirroring the JSON, and I only edited the JSON. `DracoGenTest` correctly caught the drift. *(synced `Draco.scala`: the `Generator.loadType` call on line 6 stays — that's the engine call, still resolved; only the `"Generator"` member string goes)* That was the only place the member list is duplicated in hand-written Scala. Re-run both suites.

*(Delegated action — Dev ran `sbt "testOnly draco.DracoGenTest draco.DomainBuilderTest"`: **110/110** — DracoGenTest 97, DomainBuilderTest 13, all green in 596 ms.)*

All 110 green. The relocation is fully validated: `Generator` compiles and resolves from its new home for all 50 callers, `Draco.json`/`Draco.scala` agree, and `validate` is clean across all four domains. *(landed the doc/memory updates: `src/mods/README.md` now documents the two compilation tracks; `project_generator_permanence` records the move; new `project_domainbuilder` memory + MEMORY.md pointer)* One open decision: **`GeneratorCLI`** is still the lone holdout in `src/main/scala/draco` that's tooling-without-a-JSON and depends on `Generator.generate`. By the exact logic we just applied, it belongs next to `Generator`. I recommend moving it — want me to?

---

**[Dev]** Let's move it

**[Claude]** Moved. `bin/draco-gen` calls `java -cp "$jar" draco.GeneratorCLI` — and since `src/mods/scala/draco` compiles into root, `draco.GeneratorCLI` stays the same FQN in the assembled jar, so the CLI is unaffected (just needs the usual `sbt assembly` to rebuild the jar after source changes). No other references. *(memory accuracy fixes in both notes)* Where this leaves us: **`src/mods/scala/draco/`** = `DomainBuilder` (stand-in) + `Generator`/`GeneratorCLI` (permanent engine/tooling) — compiled into root but outside the JSON type system; the invariant under test — `DomainBuilder.validate` rigorously holds the endogenous domains to it (110/110 green); penciled next — `TransformBuilder` (your plug), plus validate checks 3–4 whenever you want deeper rigor. Good stopping point. What's next?

---

**[Dev]** First, out of context question. What version of sbt should I be running for Scala 2 and how would I update to it both in IntelliJ and on my MacOS?

**[Claude]** Useful distinction up front: **sbt's version is mostly independent of your project's Scala version** — any sbt 1.x builds 2.12/2.13 fine (sbt 1.x internally runs on Scala 2.12; your `scalaVersion := "2.13.16"` is separate). *(checked the pins)* That explains the banner: your **launcher** is already current (Homebrew sbt **1.12.9**), but `project/build.properties` pins **1.7.3**, so every build for this project bootstraps 1.7.3 regardless. That file is the authoritative switch. Recommendation: bump the pin to **1.12.9** (sbt 2.0 exists but it's a larger migration aimed at Scala 3; stay on 1.x).

One plugin caveat from `project/plugins.sbt`: `sbt-dependency-graph` 0.10.0-RC1 is **obsolete** — merged into sbt itself in 1.4 (`dependencyTree` is built in); remove it when you bump. `sbt-native-packager` and `sbt-assembly` are fine on 1.12. Three places: (1) `project/build.properties` → `sbt.version=1.12.9` — authoritative, both CLI and IntelliJ obey it; (2) the macOS launcher — already current, `brew upgrade sbt` later; (3) IntelliJ — reads `build.properties` automatically; confirm JDK 17 for the sbt JVM and hit the sbt reload/refresh. Want me to make the two edits now so you can `reload` and confirm? I won't run sbt; you'd drive the reload.

---

**[Dev]** I switched to 1.12.9 and got this: *(pasted the IntelliJ sbt-shell output — `reload` warned of a version mismatch, a manual `reboot` fetched and booted sbt 1.12.9 cleanly, then: `Not a valid command: idea-shell`, exit code 1)*

**[Claude]** The upgrade actually **succeeded** — look at the reboot output: it fetched and booted `sbt 1.12.9 (Java 17.0.11)`, pulled Scala 2.12.21 for sbt, and loaded your build with no errors. The `idea-shell` error is **not a build problem** — it's an IntelliJ wrapper artifact. `idea-shell` is a command IntelliJ *injects* into its managed sbt shell via the `idea.sbt` file — notice the first run loaded `plugins.sbt,idea.sbt`, but the manual reboot loaded only `plugins.sbt`. When you typed `reboot` inside IntelliJ's shell, sbt restarted itself without IntelliJ's injected file, so the wrapper's follow-up `idea-shell` command was undefined. Nothing's broken. Fix — let IntelliJ do the restart: stop the sbt shell, click **Reload All sbt Projects**, and the new shell banner should read 1.12.9 with no error. Going forward, when `build.properties` changes, accept IntelliJ's reload prompt rather than typing `reboot` in the shell.

---

**[Dev]** Now this after 2. and 3. (Might have skipped 1.) *(pasted the IntelliJ-relaunched sbt 1.12.9 session — `test` fails compiling the 71 sources with:)*

```
java.util.ServiceConfigurationError: xsbti.compile.CompilerInterface2:
  scala.tools.xsbt.CompilerBridge Unable to get public no-arg constructor
```

**[Claude]** That's a real one, and it's a direct consequence of your build's **`managedScalaInstance := false`** setting colliding with sbt 1.12's newer compiler. sbt 1.12's Zinc uses the newer **`CompilerInterface2`** SPI, which loads the Scala **compiler bridge** as a service needing a public no-arg constructor. Because your build sets `managedScalaInstance := false`, sbt does not auto-provide the bridge — your `scala-tool` config supplies `scala-library` and `scala-compiler` but *not* the bridge. sbt 1.7.3's older Zinc didn't require this, which is why it worked before. Nothing about your code is wrong.

Fix (keeps your build shape): add the matching bridge to the `scala-tool` configuration — `"org.scala-lang" % "scala2-sbt-bridge" % scalaVersion.value % "scala-tool"` — in **both** places you declare `scala-tool` deps (the `mods` settings and the bare block feeding `root`). Then `reload`, `clean`, `compile` — the `clean` matters. Two alternatives: root-cause cleanup (drop `managedScalaInstance := false` and the manual `scala-tool` deps entirely, letting sbt auto-manage the instance *and* the right bridge — bigger change, only if the lib-vendoring requirement is gone), or defer back to 1.7.3. Want me to make the `scala2-sbt-bridge` edit? I can't run sbt to confirm, but it's the standard fix for this setup.

---

**[Dev]** Yes, make the edit please, and make sure it is documented in the appropriate places.

**[Claude]** *(made both `scala-tool` edits with inline rationale, then documented: new `feedback_sbt_managed_scala_bridge` memory with a MEMORY.md pointer, and a CHANGELOG `[Unreleased]` Build entry — plus the previously-undocumented `DomainBuilder` and `Generator`/`GeneratorCLI` relocation from this session)* Verify with `reload; clean; compile` — the `clean` matters, a stale 1.7.3-era bridge can otherwise linger. One thing to watch: the fix assumes `scala2-sbt-bridge` is published for exactly 2.13.16 (it should be). If `reload` reports it unresolved, that's the signal to fall back to the root-cause route — tell me and I'll make that change instead.

*(Delegated action — Dev ran `reload; clean; compile`: the **identical** `CompilerInterface2` / `CompilerBridge` error.)*

Two clean attempts with the bridge dep and the *identical* error — that tells me the bridge being loaded isn't the one I added; something stale is shadowing it. *(checked before changing anything else)* That rules out the simple culprits and points to the real one: **`scala2-sbt-bridge` 2.13.16 did resolve** (it's in your Coursier cache), and there's **no `lib/` dir** shadowing it. So the problem isn't a *missing* bridge; it's **how `managedScalaInstance := false` hands the bridge to sbt 1.12's Zinc.** With managed instance off, sbt just drops the bridge on the classpath and Zinc's `ServiceLoader` tries to construct `scala.tools.xsbt.CompilerBridge` via a no-arg constructor it doesn't have; the **managed** path instantiates the bridge the correct way instead. Adding the dependency can't fix that, because the *mechanism* is what's wrong — my first fix did not work.

*(Dev chose the fix-forward option — stay on 1.12.9, drop the manual apparatus.)*

Fix forward it is. Applied in three edits: removed the `mods` project's `scala-tool` block; removed the bottom `managedScalaInstance := false` + `scala-tool` apparatus; preserved `jline` (a REPL runtime dep that lived only in scala-tool) as a normal `libraryDependencies` entry on `root`. Verified no stragglers — the only remaining `ScalaTool`/`managedScalaInstance` mentions are explanatory comments. The build is now standard: sbt manages the Scala instance and resolves the matching 2.13.16 compiler bridge itself, so the failing ServiceLoader path no longer applies. Verify with a full `reload; clean; compile`; if it now compiles, follow with `sbt test` to confirm the 110 still pass under 1.12.9. Once it's green I'll update the memory/CHANGELOG notes to reflect that the fix was *dropping* `managedScalaInstance := false` rather than adding the bridge dep.

---

**[Dev]** We're gold

**[Claude]** We're gold — and I owe the docs a correction, since the fix turned out to be *dropping* `managedScalaInstance := false`, not adding the bridge dep. *(fixed the memory file, MEMORY.md, and CHANGELOG so all four docs agree on what actually worked; wrote dev-journal chapter 40 with the wrong-first-attempt preserved)* Recap of where we landed this session: **`draco.DomainBuilder`** — first `src/mods` usability/scalability stand-in (`define`/`dictionary`/`validate`/`generate`), public-API-only, tested over the four endogenous domains; the **staging-dir build model** — `src/mods/scala/draco` compiles into root, `mods` scoped to scripts, no cycle, conflict detection for free; **`Generator` + `GeneratorCLI`** relocated into that tier, out of the JSON type system, `Draco` membership corrected, **110/110**; **sbt 1.12.9** fixed by dropping the manual apparatus (not by adding the bridge dep, which failed), `jline` preserved as a normal dep; **docs current**. Natural next picks: `TransformBuilder`, or naming the next of the "several functions" beyond the domain-dictionary one.

---

**[Dev]** Let's end this session with a commit/push/release, It looks like you've updated the memory and documentation, so we can do the next dev-git-record with commit message.

**[Claude]** *(gathered the diff, the git-record format, and the tag/version conventions; then made the release-prep edits)* Release **v2.0.0-alpha.5**, dated 2026-06-03. `build.sbt` → version `2.0.0-alpha.5`; `CHANGELOG.md` → `[Unreleased]` finalized into a dated `[2.0.0-alpha.5] - 2026-06-03` section (Added: DomainBuilder + the staging tier; Changed: Generator/GeneratorCLI relocation; Build: sbt 1.12.9); dev-git-record written as a runnable script, `draco-git-record/git-record-2026-06-03-0957` — it `git add`s the new/moved files, commits, pushes, then tags `v2.0.0-alpha.5` and pushes the tag (CI auto-creates the GitHub release + uploads the jar). Commit subject:

> Release prep v2.0.0-alpha.5 — DomainBuilder (first src/mods stand-in), Generator/GeneratorCLI relocation, sbt 1.12.9

Run it from your terminal: `bash draco-git-record/git-record-2026-06-03-0957`. Once you push the tag, `gh release edit v2.0.0-alpha.5 --notes-file <notes>` swaps in curated notes per your usual flow.

---

**[Dev]** DO you you have the notes so I can delegate the "gh release" to you?

**[Claude]** Yes — `gh` 2.92.0, authenticated as `ejb816` on `ejb816/nexonix`, so I can run the release edit for you once the tag's pushed and CI has created the release. Curated notes written to `draco-git-record/release-notes-v2.0.0-alpha.5.md`. The handoff, since the release can't be edited until it exists: (1) **you run** the git-record script — commit, push, tag, push tag; (2) CI sees the tag and auto-creates the release + jar, usually a minute or two; (3) **tell me it's pushed** and I'll run `gh release edit v2.0.0-alpha.5 --notes-file …` — I'll confirm the release exists first, and if CI's still mid-flight I'll wait and retry rather than erroring.

---

**[Dev]** The commit/push is don. Should I run  sbt assembly?

**[Claude]** Short answer: **no — you don't need `sbt assembly` for the release.** Your CI builds and uploads the jar when the **tag** lands; a local assembly doesn't feed the release at all. The only reason to run it is to refresh your *local* `target/scala-2.13/draco-*.jar` that `bin/draco-sc`/`bin/draco-gen` use (they'd otherwise keep using the alpha.4 jar with `Generator` at its old path) — purely a local-tooling convenience, optional. *(checked the remote)* But the release isn't triggered yet — the **tag `v2.0.0-alpha.5` isn't on origin**, there's no release, and the latest CI run is still alpha.4. "Commit/push done" landed the branch commit, but the **tag push** — the last two lines of the record script, the thing that actually triggers CI — hasn't run. To finish:

```
git tag v2.0.0-alpha.5          # skip if it already exists locally
git push origin v2.0.0-alpha.5
```

Once the tag's pushed, ping me — I'll confirm CI created the release, then apply the curated notes.

---

**[Dev]** Ok, I did it this time.

**[Claude]** Tag's pushed (`ca45694`) and the **Release workflow is running** (34s in). The release doesn't exist yet — CI creates it at the end (alpha.4 took ~2m45s). Watching the run in the background; the moment it completes I'll verify the release exists and run the `gh release edit`.

*(Background task completed — the CI watch exited 0; verified the release and applied the notes.)*

Done — the release is live with the curated notes and jar attached:

- **Release:** https://github.com/ejb816/nexonix/releases/tag/v2.0.0-alpha.5
- **Jar:** `draco-2.0.0-alpha.5.jar` (CI-built, attached), not a draft
- **Notes:** curated notes applied successfully

That closes the session cleanly. Shipped in **v2.0.0-alpha.5**: `draco.DomainBuilder` (first `src/mods` stand-in), the root-compiled `src/mods/scala/draco` staging tier, `Generator`/`GeneratorCLI` relocated out of the JSON type system with `Draco` membership corrected, and the sbt 1.12.9 toolchain move. Full suite green at 110. Loose threads for next session: `TransformBuilder` (the sibling stand-in you flagged), and naming the rest of the "several functions" beyond the domain-dictionary one. Nice work — we're shipped.

## Status
`draco.DomainBuilder` (`define`/`dictionary`/`validate`/`generate`) landed as the first `src/mods` stand-in, public-API-only, tested over Draco/Base/Primes/Language. `src/mods/scala/draco` established as a root-compiled staging tier; `Generator` + `GeneratorCLI` relocated there; the invariant "a declared Draco domain member is JSON-backed" now enforced by `validate` and green. sbt on **1.12.9** with a standard (managed) Scala instance — the wrong first fix (adding `scala2-sbt-bridge` under `managedScalaInstance := false`) is preserved here and in `feedback_sbt_managed_scala_bridge`. Full suite **110/110**; released as **v2.0.0-alpha.5** (2026-06-03) with curated notes and CI-built jar. Open / next: `TransformBuilder` penciled as the sibling stand-in (Dev plug); `validate` checks 3 (declaration↔discovery parity) and 4 (codec round-trip) queued; `Codec`/`Generated` remain non-member, non-JSON core infra in `src/main` (left as-is); the broader "several functions" the Dev alluded to are still unnamed.
