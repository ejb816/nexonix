# Draco Dev Journal — Chapter 38

**Session date:** May 20, 2026
**Topic:** Closed #28 (codec asymmetry) and #29 (`comparisonOnlyExcluded → Map.empty`) → `discover`/`verify` in `bin/draco-gen` (#9) → four mods scripts → a latent `TypeName` reference-equality bug (#37) → `who-extends` surfaced a corpus-wide derivation gap, filed as canonicalization umbrella #38 with 4 PoC fixes → memory-quality feedback on absolute paths → CLI sbt's Java-25 crash diagnosed and resolved via a `JAVA_HOME` pin (#10 closed). Transcript.

---

**[Dev]** Let's start in this session with this: TypeElement.json omits value from dracoAspect.elements, causing codec asymmetry that strips defaults on YAML→JSON round-trip #28

**[Claude]** *(Read issue #28, TypeElement.json/scala, the 5 aspect JSON/YAML pairs, the element-kind JSONs, and the Generator's discriminated-codec emission path; then executed the fix.)*

**Changes to fix #28:**

1. **`value` added to TypeElement schema** — `TypeElement.json` now lists `value: String` (default `""`) in `dracoAspect.elements`. The discriminated encoder built from this list will now include `value`.
2. **Hand-written `TypeElement.scala` updated to match:** trait gains `lazy val value: String = ""` (concrete default on parent); encoder gains `if (x.value.nonEmpty) Some("value" -> x.value.asJson) else None`; redundant `override lazy val value: String = ""` removed from Dynamic / Pattern / Action / Variable / Factory leaves (they now inherit the trait default).
3. **Redundant `value:""` Fixed entries removed** from `factory.body` in `Dynamic.json`, `Pattern.json`, `Action.json`, `Variable.json`, `Factory.json`. Monadic and Condition retain `value: _value` (real overrides from a factory param).
4. **The 5 aspect JSONs restored from their YAML twins** — `DracoAspect.json`, `DomainAspect.json`, `RuleAspect.json`, `ActorAspect.json`, `TypeDefinition.json` now carry the previously-stripped `value:` data (factory-parameter defaults like `TypeName.Null` / `Pattern.Null` / `Action.Null`, and the Monadic `isEmpty` predicate / `encoder` / `decoder` source blocks).
5. **5 aspect entries removed from `comparisonOnlyExcluded`** in `DracoGenTest.scala` — per-type Generator comparison now runs for all 5 aspect files.

Hand-authored the aspect JSONs in canonical 2-space form rather than invoking `bin/draco-gen from-yaml`, because that path also goes through the encoder and would have stripped the data again until the assembly is rebuilt. Once you `sbt test`, the DracoGenTest family + per-type comparisons will verify the round-trip is whole.

*(Delegated action — Dev ran `sbt assembly` then `sbt test`: all passed.)*

Great. Memory note updated (the stale asymmetry entry). Tests green, #28 is ready to close, and Group A of the umbrella #29 is cleared (Group B — YAML.json + the 3 primes types — remains for separate Scala-only-logic assessment). Want me to draft the close comment on #28 and the Group-A-done update on #29?

---

**[Dev]** I just screen shotted a part of the Claude App GUI in Code that I just noticed. Can you explain what it is and what I should do with it?

**[Claude]** It's Claude Code's built-in **Create PR** widget. The "+26,922 −420" is the diff stat from your current branch (`main`) against `master` — Claude Code picked `master` as the PR base because the repo still has the historical `master` branch from way back. Your current uncommitted edits are just the 13 files from issue #28; the giant number is everything done on `main` since `master` was last touched (327 files of accumulated draco work).

Given your workflow (`feedback_direct_main_edits` — edit main directly, no PRs), **ignore the button**. It's misleading here, not actionable. If the visual noise bothers you, the clean fix is to delete the stale local `master` branch (`git branch -D master`) — but only after confirming GitHub's default branch is `main`, since the widget keys off whichever branch Claude Code treats as upstream. Want me to verify the GitHub default branch and any remaining `master` ref before you delete?

---

**[Dev]** I don't use the term "PR" myself. what does it mean in words?

**[Claude]** **PR = pull request.** It's a proposal on GitHub (or similar) to merge changes from one branch into another, with a review step in between — others can see the diff, comment, and approve before the merge happens. The name comes from old distributed-git etiquette: you'd ask a maintainer to *pull* your changes into their copy. So a "pull request" is literally "please pull my work in." In practice on GitHub it's the unit of review — one PR = one bundle of changes someone is asking to land on the main branch. Your workflow skips this entirely (you commit directly to `main` and review per-tool-call in the IDE), which is why the button isn't useful to you.

---

**[Dev]** This project has just one human making changes, for the immediate future at least.

**[Claude]** Right — that matches your current setup (`feedback_direct_main_edits`). Solo + IDE-mediated review per tool call makes the whole PR ceremony pure overhead. The Create PR widget is built for multi-contributor flows where the review step is the gate; for you the review *is* the IDE accept/reject loop on each edit. If you ever bring on a second human, that's when PRs start earning their keep — until then, ignore the button.

---

**[Dev]** > draft the close comment on #28 and the Group-A-done update on #29

**[Claude]** Cleaned up the completed tasks. Drafts below — not posting yet.

**Draft close comment for #28:**

```
Fixed. Landed on `main`, full test suite green.
1. TypeElement.json — added `value: String` as Fixed element with default "".
2. TypeElement.scala — trait gained `lazy val value: String = ""`; encoder
   gained the conditional emission; redundant leaf overrides removed.
3. Leaf JSONs cleaned — Dynamic/Pattern/Action/Variable/Factory dropped
   redundant value:"" from factory.body; Monadic and Condition keep value: _value.
4. Five aspect JSONs restored — full value: content their YAML twins always had.
5. DracoGenTest.comparisonOnlyExcluded — the 5 aspect entries removed.
Closes Group A of #29.
```

**Draft progress comment for #29:** Group A cleared by #28 (2026-05-20); remaining Group B, 4 entries — `draco/language/YAML.json`, `draco/primes/Primes.json`, `draco/primes/Numbers.json`, `draco/primes/Accumulator.json`.

Two follow-ups, only if you want them: edit the #29 body to strike through the Group A bullets and update the count to 4; post both comments and close #28 in one go. Say the word and I'll run either or both.

---

**[Dev]** Go ahead with both

**[Claude]** Done. [#28](https://github.com/ejb816/nexonix/issues/28) closed with the wrap-up comment. [#29](https://github.com/ejb816/nexonix/issues/29) body edited (Group A struck through, count updated to 4) and progress comment posted.

---

**[Dev]** Clear DracoGenTest.comparisonOnlyExcluded — eliminate the last hand-customized type declarations #29: Let's continue.

**[Claude]** *(Surveyed all 4 remaining files — Accumulator, Numbers, Primes, YAML — then worked simplest-to-hardest.)*

All four cleared. Summary of changes:

**Generator.scala** — added one targeted import detection: any `valueType` containing `mutable.` triggers `import scala.collection.mutable` (next to the existing externalTypeImports lookup).

**Accumulator** — converted Mutable→Fixed elements with `factory.body` initializers; hand-written gained canonical `dracoType` / `domainType` / `Null` lines and switched `val` → `override lazy val` for the four mutable sets.

**Numbers** — added `factory.body` with three Fixed entries computing via `Primes.nPrimes(_n)` etc.; factory parameter renamed `n` → `_n` to match Generator's `_<name>` convention (no callers use named args). Hand-written gained canonical lines.

**Primes** — added `dracoAspect.elements` with one `Fixed` for `knowledge` (becomes `lazy val knowledge: org.evrete.api.Knowledge = Rule.knowledgeService.newKnowledge("Primes")` — was plain `val`, semantically equivalent for this idempotent initializer); five helper methods (filter / naturals / composites / primesFromComposites / nPrimes) packed into one verbatim `Monadic` `globalElement`; hand-written gained `dracoType` and `elementTypeNames` lines to match canonical domain emission.

**YAML** — converted to "object-only" form. Old `trait YAML` removed; the four conversion helpers and their object-local imports (`jsonParser`, `yamlParser`, `yamlPrinter`) live in a single verbatim `Monadic` `globalElement`. Hand-written object now `extends DracoType` (via `Generator.objectGlobal`) with `dracoType: DracoType = this`. No external callers referenced `Type[YAML]`, so the type-parameter loss is harmless.

**DracoGenTest.comparisonOnlyExcluded** → `Map.empty`. The umbrella goal of #29 is reached if tests pass. Ready for `sbt test` on your end.

*(Delegated action — Dev ran `sbt test`: passed.)*

[#29](https://github.com/ejb816/nexonix/issues/29) closed, memory updated, completed tasks cleared. `comparisonOnlyExcluded` is now `Map.empty` — the "no hand-written type declarations" intent is reached. The session's natural next pickup is the priority-next queue, and per `feedback_journal_at_stopping_point` I should draft a journal chapter — want me to write **chapter 38** covering this session (closing #28 and #29)?

---

**[Dev]** Let's do more work in this session before we write chapter 38.

**[Claude]** Two things visible in the queue:

1. **#18** — same codec asymmetry as #28, predates it by a week. Already fixed in this session; should close as resolved.
2. **#9** — add `discover` + `verify` modes to `bin/draco-gen`. Tooling, generator-adjacent, self-contained. Natural next pickup given today's momentum.

*(Closed #18; implemented #9 in GeneratorCLI.scala; removed a stale `PrimeOrdinal` entry from Primes.)*

Implemented `discover` + `verify` in `GeneratorCLI.scala` and removed the stale `PrimeOrdinal` from Primes (which is exactly the kind of drift `verify` would catch).

**`bin/draco-gen verify <domain-json>`** — read-only check; exits 5 with a drift report (missing / extra / order) if `elementTypeNames` doesn't match the package siblings on disk.

**`bin/draco-gen discover <domain-json> [--force]`** — rewrites `elementTypeNames` from the directory contents, sorted as types → rules → actors (each group alphabetical). Same git-safety as `from-yaml`: refuses dirty/untracked JSON without `--force`.

Ready for `sbt assembly && sbt test` on your end. After that lands, I'll close #9.

*(Delegated action — Dev did a full reload → clean → assembly → test: passed.)*

[#9](https://github.com/ejb816/nexonix/issues/9) closed; memory reference for `draco-gen` refreshed (it was 36 days stale — missed `from-yaml` / `to-yaml` and now `discover` / `verify`). Want to keep going? Open candidates that fit today's generator/tooling momentum: **#11** (Generator domain `draco.generator.Generator[L]` — bigger architectural lift, probably its own multi-session arc), **#7** (Cosmocentric cross-package domain — out per `feedback_focus_endogenous_over_examples`), **#10** (sbt "bad constant pool index" doc/version-pin), **#15** (Getting Started guide). My recommendation: stop coding here and write chapter 38, since the session's natural arc (close #28, close #29, close #18, ship #9) is complete and well-scoped. Want me to draft chapter 38 now, or pick one more issue first?

---

**[Dev]** The new priority is to populate src/mods with user-oriented executable tools to use before the Generator domain dictionary is completed. Also,I think it's a good idea to permanently support draco.Generator, since the generator domain will be draco.generator.Generator.

**[Claude]** Two distinct actions. Let me handle them in parallel.

Three things shipped in this slot:

**`src/mods/scala/scripts/list-domain.scala`** — new script. Loads a domain's TD, walks `elementTypeNames`, summarizes each member (`extends X` for types, var/action counts for rules, message/signal counts for actors). Reports stale entries as `[MISSING]` and exits 1 (complementing `bin/draco-gen verify`). Try it: `bin/draco-sc list-domain Primes draco primes`.

**Side-fix to `inspect-type.scala` and `derivation-chain.scala`** — both had a broken `td == TypeDefinition.Null` check that never fired on missing resources (`Generator.loadType` returns a typeName-only placeholder, not `Null`, on miss). Replaced with an aspect-emptiness check; missing types now error cleanly.

**`project_generator_permanence.md`** — recorded the decision that `draco.Generator` is permanent and coexists with `draco.generator.Generator` (different namespaces, different roles, no deprecation when #11 lands).

Ready for `sbt clean assembly test`. After that's green, candidate next scripts: **`who-extends`** (reverse derivation — hard to compute manually; high user-value), **`list-domains`** (discovery: probe known domain roots, list each with element count), **`diff-type`** (structural diff between a JSON-loaded TD and the hand-written `.scala` via `Generator.generate(td)`). `who-extends` seems most valuable. Want me to build it next, or pick a different one?

---

**[Dev]** test passed - You can start with your 3 candidate scripts, and when those work, we can add more. Let's get these task sin the issue tracker.

**[Claude]** Issues created: [#30](https://github.com/ejb816/nexonix/issues/30) who-extends, [#31](https://github.com/ejb816/nexonix/issues/31) list-domains, [#32](https://github.com/ejb816/nexonix/issues/32) diff-type. Now building.

All three scripts shipped and catalog updated:

- **`list-domains.scala`** (#31) — probes Draco / Base / Primes / Language by default; accepts dotted FQNs to override. Reports element count + composition (types / rules / actors).
- **`who-extends.scala`** (#30) — inverse derivation: scans every element in the canonical set and reports those whose chain transitively reaches the target. Cycle-protected.
- **`diff-type.scala`** (#32) — single-type drift check: `Generator.generate(td)` vs the hand-written `.scala`, normalized like `DracoGenTest`, side-by-side report on mismatch.

After `sbt clean assembly test`, try:

```
bin/draco-sc list-domains
bin/draco-sc who-extends DracoType draco
bin/draco-sc diff-type Primes draco primes
```

Once you've tried them and confirmed the output reads right, I'll close #30 / #31 / #32 and propose the next batch. Possible follow-ups in that batch: **`element-of`** (which domain(s) include a type — catches double-listing and orphans), **`type-graph`** (derivation forest as indented text or DOT), **`probe-import`** (show what `Generator.typeImports` would emit for a given JSON), **`stats`** (corpus-wide tallies).

---

**[Dev]** "tasks in"

**[Claude]** Filing the next batch as issues now. Filed: [#33](https://github.com/ejb816/nexonix/issues/33) element-of, [#34](https://github.com/ejb816/nexonix/issues/34) type-graph, [#35](https://github.com/ejb816/nexonix/issues/35) probe-import, [#36](https://github.com/ejb816/nexonix/issues/36) stats. Backlog now has the next batch tracked. Ready for `sbt clean assembly test` on the three already-shipped scripts; once those read right, I'll close #30 / #31 / #32 and start on this new batch.

---

**[Dev]** CCheck this output against expectations:

*(Pasted the first run of the three scripts. `diff-type` reported OK, but `list-domains` printed all four domains as broken, and `who-extends` scanned nothing:)*

```
Known domains:

  draco.Draco              [not a domain — lives in draco.Draco]
  draco.base.Base          [not a domain — lives in draco.base.Base]
  draco.primes.Primes      [not a domain — lives in draco.primes.Primes]
  draco.language.Language  [not a domain — lives in draco.language.Language]
...
Types extending draco.DracoType (transitive):

  (no matches across 0 element(s) in 0 domain(s))
```

**[Claude]** Three of those four outputs are wrong. The bug is `TypeName ==` reference equality — same content, different instances. Let me confirm against the type and fix.

Diagnosis: `TypeName` is a hand-written trait with no `equals`/`hashCode` override, so `==` is reference equality. Two `TypeName` instances with identical content compare unequal, breaking `domainAspect.typeName == td.typeName` in every script that does that check.

- **diff-type** output was correct (doesn't use that check).
- **list-domains** + **who-extends** were both wrong (all four domains fail the false equality check; zero domains scanned).

**Quick fix applied** to all five affected sites — use `.namePath == .namePath` string comparison: `list-domains.scala`, `who-extends.scala`, `list-domain.scala`, `inspect-type.scala`, and `GeneratorCLI.scala`'s `isDomainJson` — same latent bug in the new `discover`/`verify` commands; would have surfaced first time you ran one of those on a domain JSON.

**Root cause filed as [#37](https://github.com/ejb816/nexonix/issues/37)** — add structural `equals`/`hashCode` to `TypeName` via the canonical JSON-authoring path, then revert the workarounds. `Generator.isDomain` only escaped this bug because its first clause is OR'd with the `elementTypeNames.nonEmpty` fallback.

Rebuild and rerun. Expected outputs after the fix: **list-domains** → 4 rows with element counts; **list-domain Primes** → 5-row table (2 TYPE + 3 RULE); **who-extends DracoType draco** → list of types whose chain reaches DracoType, with counts; **diff-type Primes** → still `OK`.

*(Delegated action — Dev reran all four scripts: list-domains showed Draco 42 / Base 9 / Primes 5 (2 types, 3 rules) / Language 1; list-domain Primes the 5-row table; who-extends DracoType a 44-row list — "44 match(es) across 57 element(s) in 4 domain(s)"; diff-type Primes OK.)*

All four scripts verified against expectations. #30, #31, #32 closed.

**Notable side-finding from `who-extends`:** 44 matches out of 57 scanned elements — meaning ~13 types have empty `dracoAspect.derivation` in JSON despite the 2026-05-09 "every draco type extends DracoType" architectural shift. The outliers visible by deduction: `CLI`, `REPL`, `Generator`, `TypeName`, `Value`, plus the `Primes` and `YAML` domain types themselves. Their hand-written `.scala` extends `DracoType` (where applicable) but their JSON doesn't declare it. This is a real data-quality gap — separate from the script work, but worth filing if you want it tracked.

**Backlog state:** 4 mods-script issues queued (#33–#36); #37 TypeName structural equality (would let me revert the 5 namePath workarounds). My recommendation: keep momentum on the mods batch; #37 is its own arc — touches JSON + hand-written + DracoGenTest equivalence — better as a standalone session. Want me to proceed with #33-#36, or pivot to #37, or file the data-quality finding first?

---

**[Dev]** You are correct. Even if the draco types don 't work, the declaration should still be consistent with the definition. Let's track these and fix them according to principle, which means make sure they generate and compile cleanly. If they still don't execute properly, the fix is via changing the type definition not writing scala code.

**[Claude]** *(Investigated the full set of outliers — both the elements who-extends missed, and the domain types themselves.)* Filed [#38](https://github.com/ejb816/nexonix/issues/38) with the 12-item enumeration and per-item checkboxes. Then did a proof-of-concept batch of four fixes to validate the workflow.

**Common pattern** — for each outlier: JSON gains `dracoAspect.derivation: [{ name: "DracoType", namePackage: ["draco"] }]` (creating the whole `dracoAspect` block where missing); the hand-written `.scala` trait gains `extends DracoType`; the companion object gains `with DracoType`; `typeDefinition` gains `override`. Where there's a factory (`Value`), the apply's anonymous class gains `override lazy val typeDefinition: TypeDefinition = X.typeDefinition`.

**Per-type notes:**
- **`Unit`** — simplest: empty trait, no factory.
- **`REPL`** — parameterized trait (`REPL[L]`); companion's `dracoType: Type[REPL[_]]` (wildcard) unchanged.
- **`Value`** — has trait body (vals + a verbatim `def value[T]`) and factory. Apply gains `typeDefinition` override.
- **`CLI`** — interesting case: previously routed through `Generator.objectGlobal` (no trait, `dracoType: DracoType = this`). Adding derivation re-routes through `typeGlobal`, which now emits a `trait CLI extends DracoType` *and* `lazy val dracoType: Type[CLI]`. Both added to hand-written. The `def main` stays in the object body via `globalElements` (`hasExplicitMain` keeps `extends App` off so `extends DracoType` alone is the companion header — matches `build.sbt`'s `mainClass := draco.CLI` expectation).

Ready for `sbt clean assembly test`. After that's green, `bin/draco-sc who-extends DracoType draco` should now report **48** matches (up from 44), with Unit / Value / REPL / CLI added to the list. Once verified, I'll tick those four off the #38 checklist. Remaining outliers: `TypeName` (interleaves with #37), `Primes`, `YAML`, `Draco`, `Language`, `Generator` (the design-thought one), plus `Accumulator` / `Numbers` auto-resolve once `Primes` lands.

*(Delegated action — Dev reran `bin/draco-sc who-extends DracoType draco`: 48 match(es) across 57 element(s), with `draco.CLI`, `draco.REPL`, `draco.Value`, `draco.base.Unit` now in the list.)*

The 4 new matches landed exactly as predicted. Checklist updated and progress comment posted on #38. 4 of 12 outliers ticked; 8 remain.

This is a strong stopping point. Session arc this turn alone (just since "let's continue" on #29): closed #9 (discover/verify), #30 (who-extends), #31 (list-domains), #32 (diff-type), #18 (duplicate); shipped `discover` + `verify` in `bin/draco-gen` and `list-domain` / `list-domains` / `who-extends` / `diff-type` in `src/mods`; found and worked around TypeName reference-equality (#37); filed canonicalization umbrella #38 + ticked 4 PoC fixes; filed next batch #33–#36; recorded `draco.Generator` permanence policy in memory.

My recommendation: **stop here and draft chapter 38** of the draco-dev-journal. The session has a clean shape (close one umbrella → ship tooling → find latent bug → file canonicalization umbrella → validate fix workflow) and chapter 38 will capture the methodology shift toward "JSON-normative fixes only." Want me to write it?

---

**[Dev]** I accept your recommendation.

**[Claude]** *(Went looking for the journal directory — tried `/nexonix/draco-dev-journal/` per the memory entry, then fell back to filesystem `find` — without a text response before the next prompt.)*

---

**[Dev]** You seem to be lookin for this:

/Users/edwardburke/IdeaProjects/nexonix/draco-dev-journal

**[Claude]** Chapter 38 written at `draco-dev-journal/draco-dev-chapter-38.md` and the MEMORY pointer updated. The chapter covers the full session arc — closing #28/#29, the six tools shipped, the TypeName equality bug + workaround, the data-quality finding that became #38, and the four PoC fixes. ~190 lines, structured like chapter 37 (prose with verbatim user quotes, technical detail, "what stuck" closing). Session is wrapped.

---

**[Dev]** I am concerned about how memory support text documents are being updated before each new session. The new model instance had two significant problems creating a new journal chapter.  I call the new model instance "you" because that's how I identify it when I'm in a session interacting with the current "instance" that I'm interacting with:

1. You did not know where to write the latest chapter
2. You did know how to find the directory to write it in

**[Claude]** You're right on both counts (and your point 2 phrasing — "did know" — I'm reading as a typo for "did not know," matching what happened: I tried `/nexonix/draco-dev-journal/` first, that's wrong, then groped through `find`).

The MEMORY entry was:

> draco-dev-journal lives at **/nexonix/draco-dev-journal/** (chapters 00 onward, latest is chapter 38). ...

That `/nexonix/` is a shorthand — not a real path on this machine. A fresh instance reads it literally and goes looking under `/`. The actual path is `/Users/edwardburke/IdeaProjects/nexonix/draco-dev-journal/`. Same pattern probably hurts elsewhere — scanned and found only one literal-shorthand site to fix (MEMORY.md line 86; the other `/nexonix/` hits are GitHub URLs or already-absolute paths). The bug propagated forward from chapter 37's entry into chapter 38's; same shorthand, copy-paste perpetuation.

Both layers patched:

**Immediate:** MEMORY.md line 86 now reads the absolute path. A fresh instance reading that entry can act on it cold.

**Systematic:** New feedback file `feedback_absolute_paths_in_memory.md` codifies the rule — any filesystem path in memory must be absolute (`/Users/...`) or explicitly project-relative with the root spelled out. Indexed into MEMORY.md's Feedback section so future instances see it during the memory scan. Includes the specific failure mode it surfaced from and a note about copy-forward propagation.

This is exactly the class of memory-quality issue worth recording — a fresh instance, today, would now find the journal on the first read instead of the third. The broader concern (memory updated before each new session) is real, and the right defense is feedback notes like this one that turn each near-miss into a constraint future-me applies during the same update cycle that produced the miss.

---

**[Dev]** Changing the topic:

**[Claude]** Go ahead — what's the new topic?

---

**[Dev]** Run sbt  with no arguments with your terminal access and discuss the result.

**[Claude]** *(Ran `sbt` and `java -version`/`sbt --version` from the terminal.)* Reproduced exactly what `feedback_sbt_bad_constant_pool.md` documents, and it's #10 in the open issue list.

**What sbt said (interpreted):** bootstrap reached `sbt.internal.parser.SbtParser` initialization — the component that parses `build.sbt`/`*.sbt` text. Parser tries to load Scala 2.12.17's `library.jar` symbols via the classfile reader. Reader trips on `bad constant pool index: 0 at pos: 49428` and aborts. From there everything is fallout: `ExceptionInInitializerError` → `NoClassDefFoundError` for `SbtParser$` → `discoverProjects` can't load any project, sbt never reaches a prompt.

**Why now (the version triple is the smoking gun):**

| Layer | Version |
|---|---|
| Launcher (Homebrew) | sbt 1.12.9 |
| Project-pinned sbt (`project/build.properties`) | sbt **1.7.3** |
| sbt 1.7.3's bundled Scala (for parsing `.sbt` files) | Scala **2.12.17** |
| sbt's reported JVM | **Java 25.0.2** (Homebrew dependency) |
| My shell's `java -version` | Java 17.0.11 (Temurin) |

sbt 1.12.9's launcher honors the project's `1.7.3` pin and pulls *that* sbt's Scala-2.12.17 toolchain. Scala 2.12.17 was released October 2022, well before Java 25 (2025). Its classfile parser can't handle attributes/constant-pool shapes that newer JDKs emit — hence the index-0 trip.

**Why your IDE-launched `sbt test` works fine:** IntelliJ has its own JVM bundled (typically a recent LTS) and runs sbt with that, not Homebrew's Java 25. The CLI path is the only one that breaks — matching what the feedback memo predicted.

**Two real options, neither destructive:** (1) bump `project/build.properties` from `sbt.version=1.7.3` to `1.10.x` — 1.10 ships with Scala 2.12.20, which has Java 25 classfile-parser fixes; cleanest fix, clears #10. (2) Pin Java per-shell via `JAVA_HOME=$(/usr/libexec/java_home -v 17) sbt` for CLI invocations — workaround, not a fix. My recommendation is option 1. Want me to check what the safest target version is and propose the exact bump?

---

**[Dev]** Tell me if you can replace JDK25 with JDK17 using homebrew, and if you thing that might fix any problems.

**[Claude]** You actually already have JDK 17 — Temurin 17.0.11 at `/Users/edwardburke/Library/Java/JavaVirtualMachines/temurin-17.0.11/`. That's not the Homebrew install; it's a separate Adoptium install, and it's what your shell's `java -version` returns. Homebrew's `openjdk` (currently 25.0.2) is `keg-only` and was pulled in as a **dependency of `sbt`** — so sbt's launcher points itself at the Homebrew openjdk regardless of what your shell PATH says.

**Can you replace via Homebrew?** Two paths, neither as clean as a single command: (1) `brew install openjdk@17` gives a Homebrew-managed JDK 17 alongside 25, but `sbt`'s formula declares its dependency on the *unversioned* `openjdk` (latest), so installing `@17` doesn't make sbt switch, and `brew uninstall openjdk` would complain ("required by sbt") or reinstall it. (2) Replace Homebrew's sbt with SDKMAN! / official tarball / Coursier, which honor `JAVA_HOME` — bigger lift.

**Will dropping Java 25 fix the problem?** Most likely yes — the crash is exactly the Java-25 ↔ Scala-2.12.17 incompatibility, and the memo notes Java 17 + Scala 2.12.x is "fragile but usually works." Your IDE already proves it.

**Recommendation — the smallest change that fixes CLI sbt:** set `JAVA_HOME` for sbt invocations. Don't touch Homebrew. Homebrew's sbt launcher uses `JAVA_HOME` if it's set, falling back to its bundled openjdk only when it isn't. Add to `~/.zshrc` (or `~/.zprofile`):

```text
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
```

That makes Temurin 17 — the JDK you already have, already trust, already use indirectly via IntelliJ — the CLI sbt's JVM too. No Homebrew install, no uninstall, no version juggling. Reversible by unsetting the variable. Verify with one new shell: `sbt sbtVersion` should print 1.7.3 with `(Temurin 17.0.11)` in the banner instead of `(Homebrew Java 25.0.2)`. If you also want to permanently outgrow the JDK-version sensitivity, the sbt 1.7.3 → 1.10.x bump remains the proper fix — but `JAVA_HOME=…/temurin-17` is the one-line change that resolves the immediate failure.

---

**[Dev]** See, I was to specific in my expectations. You have a better way of doing what might be needed.

**[Claude]** Noted — and worth filing as a pattern. The good thing about that interaction shape is your initial framing told me *which problem you wanted solved* (Java 25 is the offender), even if the proposed method (Homebrew swap) wasn't the cleanest path. I had room to come back with the smaller fix because you'd been specific about the goal, not just the verb.

Want me to apply the `~/.zshrc` change now? One caveat first: setting `JAVA_HOME` globally affects every Java tool on your system (anything that honors it — Maven, Gradle, scripts), not just sbt. For your setup that's almost certainly fine, since IntelliJ has its own JDK selection and there's no other Java tooling visible in `brew list`. But it is a system-wide change, not a per-project one. If you'd prefer a narrower scope — JAVA_HOME only when invoking sbt — I can write a tiny shell alias or wrapper instead. Your call.

---

**[Dev]** As long as it affects all my terminal uses as well as yours, for running sbt. That's the most important result, since those other environments should probably use the same JDK version.

**[Claude]** *(Edited `~/.zshrc`, then verified by spawning a fresh interactive zsh.)* Confirmed working. sbt now reports `(Eclipse Adoptium Java 17.0.11)` instead of `(Homebrew Java 25.0.2)`, builds without the constant-pool crash, and reports `1.7.3` from `sbtVersion` cleanly. The fix is in place for every new terminal session you open from here on (or after re-sourcing `.zshrc` in any open ones).

Two follow-up housekeeping moves I'd recommend:

1. **Close #10** with the resolution note (JAVA_HOME pin in `~/.zshrc`, verified by sbt banner change). The deeper "Homebrew's `sbt` formula tracks unversioned `openjdk`" upstream concern remains, but the practical resolution path is now documented and self-evident.
2. **Add a short memory note** so future model instances know JAVA_HOME is pinned in your shell config — that way, if a new instance sees `(Eclipse Adoptium Java 17.0.11)` in sbt output and wonders why, they don't dig into the wrong path.

Want me to do both?

---

**[Dev]** Yes

**[Claude]** Both done. [#10](https://github.com/ejb816/nexonix/issues/10) closed with the full diagnosis + resolution narrative + the "why not the other paths" comparison, so a future reader (or future me) can trace the decision. MEMORY's pointer to `feedback_sbt_bad_constant_pool.md` now carries the resolution flag inline, including the diagnostic signal ("if you see `(Eclipse Adoptium Java 17.0.11)` in the sbt banner, that's the pin working — don't try to fix it"), so a fresh instance doesn't reflexively chase the previous failure mode.

## Status

Full suite green (245-test baseline from ch.37; full reload → clean → assembly → test passed). `comparisonOnlyExcluded` is **`Map.empty`** — zero hand-customized type declarations. Closed this session: **#28, #29, #18, #9, #30, #31, #32, #10**. Filed: **#33–#36** (next mods-script batch: element-of, type-graph, probe-import, stats), **#37** (TypeName structural equals/hashCode — 5 `.namePath ==` workarounds pending revert), **#38** (canonicalize DracoType derivation on every draco type — 4 of 12 ticked: CLI, REPL, Value, Unit; 8 remain, TypeName interleaving with #37; Accumulator/Numbers auto-resolve once Primes lands). `who-extends DracoType draco` now reports 48/57. Tooling shipped: `bin/draco-gen discover`/`verify`; mods scripts `list-domain`, `list-domains`, `who-extends`, `diff-type`. Memory: `project_generator_permanence.md` new, `reference_draco_gen_cli.md` refreshed, `feedback_absolute_paths_in_memory.md` new. CLI sbt fixed via `JAVA_HOME` Temurin-17 pin in `~/.zshrc`. Queued: finish #38, #37 root-cause fix, #33–#36.
