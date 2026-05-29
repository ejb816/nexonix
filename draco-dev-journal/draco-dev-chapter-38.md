# Draco Dev Journal — Chapter 38

**Session date:** May 20, 2026
**Topic:** Closed two umbrellas (#28 codec asymmetry, #29 `comparisonOnlyExcluded → Map.empty`), shipped six tools to `bin/draco-gen` and `src/mods`, surfaced a latent `TypeName` reference-equality bug that ate the first `who-extends` run, then graduated to a corpus-wide data-quality finding: ~9 types whose JSON-declared derivation chain doesn't reach `DracoType` despite the 2026-05-09 architectural shift. Filed #38 (canonicalization umbrella) and landed 4 PoC fixes (CLI, REPL, Value, Unit) before stopping.

---

## Opening — closing #28

Picked up from Chapter 37's open issue:

> **Dev:** Let's start in this session with this: TypeElement.json omits value from dracoAspect.elements, causing codec asymmetry that strips defaults on YAML→JSON round-trip #28

The fix is a single Fixed element added to `TypeElement.json` (`{ "kind": "Fixed", "name": "value", "valueType": "String", "value": "\"\"" }`), but the cascade is wider:

1. **Trait gains a concrete default.** `TypeElement` now carries `lazy val value: String = ""` — concrete on parent, overridden by leaves whose `factory.parameters` declare `value` (Fixed / Mutable / Parameter / Monadic / Condition).
2. **Encoder gains the field.** `if (x.value.nonEmpty) Some("value" -> x.value.asJson) else None`.
3. **Redundant overrides removed** from Dynamic / Pattern / Action / Variable / Factory `factory.body` — they now inherit the trait default. This kept the per-leaf factory bodies consistent with how `parameters` and `body` already work.
4. **Five aspect JSONs restored** from their YAML twins: `DracoAspect.json`, `DomainAspect.json`, `RuleAspect.json`, `ActorAspect.json`, `TypeDefinition.json` regained their factory-parameter defaults (`TypeName.Null`, `Pattern.Null`, `Action.Null`, etc.) and their Monadic global-element bodies (`isEmpty` predicates + `TypeDefinition`'s encoder / decoder source).
5. **`DracoGenTest.comparisonOnlyExcluded`** lost the 5 aspect entries.

Hand-authored the restored JSONs rather than re-running `bin/draco-gen from-yaml` because the assembly hadn't been rebuilt with the new encoder yet — running `from-yaml` would have stripped `value` again. Once the dev rebuilt:

> **Dev:** I ran assembly followed by test in sbt. All passed.

Closed #28; updated MEMORY with the resolution note.

---

## Then — #29 Group B, with a principle

#28 cleared Group A of #29 (the 5 aspect files). Group B was 4 entries (`YAML.json`, `primes/Primes.json`, `primes/Numbers.json`, `primes/Accumulator.json`) flagged as carrying "Scala-only logic." The dev's directive was direct:

> **Dev:** Clear DracoGenTest.comparisonOnlyExcluded — eliminate the last hand-customized type declarations #29: Let's continue.

Each entry needed its own assessment. Worked through them simplest-to-hardest:

- **Accumulator.** Mutable → Fixed elements; `factory.body` gained four Fixed initializers (`mutable.Set[Int] ()` etc.). Required a small Generator addition: when any `valueType` contains `mutable.`, emit `import scala.collection.mutable`. Targeted check next to the existing `externalTypeImports` lookup — five lines.
- **Numbers.** `factory.body` got three Fixed entries computing via `Primes.nPrimes(_n)` / `Primes.naturals(2).take(primeSequence.last - 1)` / `Primes.composites(primeSequence)`. Renamed the factory parameter `n` → `_n` to match the Generator's `_<name>` convention. Verified no callers used named-arg `n =`.
- **Primes.** Gained `dracoAspect.elements` with one Fixed for `knowledge` (the Evrete bootstrap line, now `lazy val` to match Generator emission). Five LazyList helpers (`filter` / `naturals` / `composites` / `primesFromComposites` / `nPrimes`) packed into one verbatim Monadic `globalElement`, preserving the original comments and blank lines. Generator's `globalElementsDeclaration` indents each line by two spaces, so the JSON value is unindented and the emission gets the indent for free.
- **YAML.** Different shape — `YAML.scala`'s four conversion helpers (`loadTypeDefinition` / `emit` / `fromJson` / `toJson`) plus two import aliases (`jsonParser`, `yamlParser` / `yamlPrinter`) went into a single Monadic `globalElement`. The empty `trait YAML` was dropped: `YAML.json` now has no `elements`, no `factory`, no `derivation` — only `globalElements` — which routes through `Generator.objectGlobal` ("isObjectOnly" path). Resulting object: `extends DracoType` with `dracoType: DracoType = this`. The type-parameter loss (was `Type[YAML]`) was harmless; no external code referenced it.

All four cleared. `comparisonOnlyExcluded` reached `Map.empty`. Full suite green.

Closed #29 with the Group B resolution narrative. Also closed #18 — same root cause as #28, filed a week earlier, resolved by the same fix.

---

## A digression on the UI

Mid-session the dev posted a screenshot of Claude Code's "Create PR" widget showing `+26,922 −420`:

> **Dev:** I just screen shotted a part of the Claude App GUI in Code that I just noticed. Can you explain what it is and what I should do with it?

Diagnosed: the widget computes the diff against `master` (which the project has but doesn't use; everything happens on `main`). Hence the 327-file, 26k-line diff — every commit since the historical `master` branch was last touched. Verified via `git diff --shortstat origin/master..HEAD`: matched.

Then a vocabulary check:

> **Dev:** I don't use the term "PR" myself. what does it mean in words?

Explained: pull request, GitHub's review-step mechanism, derived from old distributed-git etiquette ("please pull my work in"). Followed by:

> **Dev:** This project has just one human making changes, for the immediate future at least.

That's already captured in `feedback_direct_main_edits.md` — solo workflow, edits on main, IDE-mediated per-tool-call review. The Create PR widget is built for multi-contributor flows and is pure overhead for the current setup. Recommended ignoring it; if the visual noise bothers, deleting the stale local `master` branch would silence the widget.

---

## `bin/draco-gen` gains discover + verify (#9)

With #29 closed, picked up #9 from the priority queue. Two new subcommands:

- **`bin/draco-gen verify <domain-json>`** — read-only. Compares the domain JSON's `elementTypeNames` against its package siblings on disk. Exits 5 on drift with a missing / extra / order report.
- **`bin/draco-gen discover <domain-json> [--force]`** — write mode. Rewrites `elementTypeNames` from the directory contents, sorted as types → rules → actors (each group alphabetical). Same git-safety as `from-yaml`: refuses dirty / untracked JSON without `--force`.

Both require domain JSONs (`domainAspect.typeName == typeName`). Caught a stale `PrimeOrdinal` entry in `Primes.json` — referenced a type that doesn't exist on disk, exactly the kind of drift `verify` is built to surface. Removed it from both JSON and hand-written.

Closed #9.

---

## The mods script batch

Then back to populating `src/mods/scala/scripts/`. Dev's framing:

> **Dev:** The new priority is to populate src/mods with user-oriented executable tools to use before the Generator domain dictionary is completed. Also, I think it's a good idea to permanently support draco.Generator, since the generator domain will be draco.generator.Generator.

Two distinct moves there. Second one: recorded as `project_generator_permanence.md`. The decision is that `draco.Generator` (imperative engine, hand-written) and `draco.generator.Generator` (typed domain from #11, future) coexist permanently — different namespaces, different roles, no deprecation. `bin/draco-gen` / `bin/draco-sc` / mods scripts all keep importing `draco.Generator` directly.

First move: built four scripts in succession:

1. **`list-domain`** — given a domain, summarize each member named in `elementTypeNames` (`extends X` for types, `N var(s), M-step action` for rules, message/signal counts for actors). One-screen mental model.
2. **`list-domains`** — discovery counterpart. Probes the canonical first-party set (Draco, Base, Primes, Language) by default; accepts dotted FQNs to override. Reports element count + composition.
3. **`who-extends`** — inverse derivation: scans every element in the canonical set and reports those whose dracoAspect chain transitively reaches the target. Cycle-protected.
4. **`diff-type`** — single-type drift check: `Generator.generate(td)` vs the hand-written `.scala`, normalized like `DracoGenTest`, side-by-side report on mismatch.

Side-fix while in the area: `inspect-type.scala` and `derivation-chain.scala` both had a broken `td == TypeDefinition.Null` check. `Generator.loadType` returns a typeName-only placeholder (not `Null`) when a resource is missing, so the existing check never fired. Replaced with aspect-emptiness detection.

Filed #30 / #31 / #32 for the three new scripts as the dev directed:

> **Dev:** test passed - You can start with your 3 candidate scripts, and when those work, we can add more. Let's get these task sin the issue tracker.

Then:

> **Dev:** "tasks in"

— terse follow-up confirming the rest of the batch should also be tracked. Filed #33 (`element-of`), #34 (`type-graph`), #35 (`probe-import`), #36 (`stats`) for the next round.

---

## The data bug

First sanity run after the assembly rebuild went sideways:

```
$ bin/draco-sc list-domains
Known domains:

  draco.Draco              [not a domain — lives in draco.Draco]
  draco.base.Base          [not a domain — lives in draco.base.Base]
  draco.primes.Primes      [not a domain — lives in draco.primes.Primes]
  draco.language.Language  [not a domain — lives in draco.language.Language]
```

All four reported "not a domain — lives in draco.X" where `draco.X` is the path that the script *just printed* as the actual `domainAspect.typeName.namePath`. So the `namePath` strings matched, but the `==` comparison didn't.

Investigation: `TypeName` is a hand-written trait with no `equals` / `hashCode` override. `apply` returns an anonymous trait instance each call. Default Java equals → reference equality. Two `TypeName`s with identical content compare unequal. The check `td.domainAspect.typeName == td.typeName` was always false.

`Generator.isDomain` escapes this only because its first clause is OR'd with `elementTypeNames.nonEmpty` and `(source && target).nonEmpty` fallbacks — Primes has nonempty elementTypeNames, so the OR carries the predicate to `true` despite the broken `==`. The mods scripts and `GeneratorCLI.isDomainJson` used only the first clause and broke cleanly.

Quick fix: replace `==` with `.namePath == .namePath` in five places. Filed #37 for the root-cause fix (add structural `equals` / `hashCode` to `TypeName` via the canonical JSON-authoring path — Dynamic globalElements emitting `override def equals(other: Any): Boolean = ...` and `override def hashCode: Int = ...`).

Worth remembering: the same anonymous-class equality trap is latent in every other `apply`-via-anonymous-class type in draco. It hasn't bitten anywhere else because nobody does structural `==` on those types — they're compared via `isEmpty` predicates or field accessors. But the trap is sized.

---

## The side-finding

After the fix, the rerun looked right:

- list-domains: 4 rows, correct counts (Draco 42 / Base 9 / Primes 5 / Language 1).
- list-domain Primes: 5-row table, 2 TYPE + 3 RULE.
- diff-type Primes: OK, no drift.
- who-extends DracoType draco: **44 matches across 57 elements**.

That 44 / 57 number was the side-finding. 57 elements − 3 rules (correctly excluded; rules don't have `dracoAspect.derivation`) − 1 self-skip (`DracoType` itself) = 53 type candidates. 44 matches → **9 types whose JSON-declared derivation chain doesn't reach DracoType**, despite the 2026-05-09 architectural shift declaring it should.

Surfaced types (visible by deduction from the matches list): CLI, REPL, Generator, TypeName, Value, plus the `Primes` and `YAML` domain types themselves. Some hand-written `.scala` says `extends DracoType` (CLI) but the JSON doesn't declare it. Some neither do.

Reported the gap to the dev. The response was a methodology statement:

> **Dev:** You are correct. Even if the draco types don't work, the declaration should still be consistent with the definition. Let's track these and fix them according to principle, which means make sure they generate and compile cleanly. If they still don't execute properly, the fix is via changing the type definition not writing scala code.

That's the JSON-normative principle promoted from policy to working rule: when JSON and Scala disagree, fix the JSON, regenerate the Scala, and if runtime breaks, fix the JSON further — don't reach for the Scala. The Generator is the bridge, not an escape hatch.

Filed [#38](https://github.com/ejb816/nexonix/issues/38) — umbrella for "Canonicalize: declare DracoType derivation on every draco type." Enumerated 12 outliers across four categories:

- Empty `dracoAspect.derivation`: TypeName, CLI, REPL, Value, Primes, YAML
- No `dracoAspect` at all: Draco, Unit, Language
- Missing JSON entirely: Generator
- Transitive gap (auto-resolves once parent declares DracoType): Accumulator, Numbers

---

## PoC batch

Dev picked "PoC + 2-3 more easy ones" as the scope for in-session validation. Did four:

- **`Unit`** (simplest — no elements, no factory, just the derivation declaration).
- **`REPL`** (parameterized trait `[L]`; companion's `dracoType: Type[REPL[_]]` stays wildcard).
- **`Value`** (has trait body with `val`s + a verbatim `def value[T]` Monadic; has factory; apply gains `typeDefinition` override).
- **`CLI`** (interesting case — previously routed through `objectGlobal`'s isObjectOnly path with `dracoType: DracoType = this`. Adding derivation re-routes through `typeGlobal` which emits a trait declaration plus `lazy val dracoType: Type[CLI]`. Both added to hand-written. `hasExplicitMain` keeps `extends App` off, so the companion header stays `extends DracoType` alone — matches `build.sbt`'s `mainClass := draco.CLI` expectation).

Each fix was: add `dracoAspect.derivation: [DracoType]` to JSON → trait gains `extends DracoType` → object gains `with DracoType` → `typeDefinition` gains `override` → factory body's anonymous class gains `typeDefinition` override (where there is one). The pattern is mechanical enough that the next 8 outliers should batch cleanly.

`bin/draco-sc who-extends DracoType draco` rerun: **48 matches** (up from 44). CLI, REPL, Value, Unit visible in the list. Ticked the 4 checkboxes on #38, posted progress comment.

---

## Issues filed / closed this session

Closed:
- **#28** — TypeElement codec asymmetry (Group A of #29).
- **#29** — `comparisonOnlyExcluded → Map.empty`.
- **#18** — Same root cause as #28; resolved by the same fix.
- **#9** — `bin/draco-gen discover` + `verify` modes.
- **#30** — `who-extends` script.
- **#31** — `list-domains` script.
- **#32** — `diff-type` script.

Filed:
- **#33** — `element-of` script.
- **#34** — `type-graph` script.
- **#35** — `probe-import` script.
- **#36** — `stats` script.
- **#37** — TypeName lacks structural equality; reference equality silently fails downstream checks.
- **#38** — Canonicalize: declare DracoType derivation on every draco type. 4 of 12 ticked.

---

## Memory and follow-up

Three memory files touched:
- `project_generator_permanence.md` — **new**. Records the decision that `draco.Generator` is permanent and coexists with `draco.generator.Generator`.
- `reference_draco_gen_cli.md` — refreshed (was 36 days stale; missed `from-yaml` / `to-yaml`; now covers `discover` / `verify` too).
- `MEMORY.md` — Key Facts updated: codec asymmetry resolved, `comparisonOnlyExcluded` is `Map.empty`, Primes migrated to generator-canonical form, generator permanence note added.

Followups (in priority order for next session):
1. Finish #38 — 8 outliers remaining. TypeName interleaves with #37 (equals/hashCode fix touches same file).
2. #37 root-cause fix — would let the 5 `.namePath ==` workarounds revert to `==`.
3. #33-#36 — next mods-script batch.

---

## What stuck

- **JSON-normative is now a working rule, not just policy.** Last session it was a load-path decision; this session the dev promoted it to a fix-direction principle: "the fix is via changing the type definition not writing Scala code." The architectural-shift gaps surfaced by `who-extends` aren't Scala bugs to patch in `.scala` files — they're JSON declarations missing from the canonical source.
- **The Generator emits enough Scala to make the bridge feasible.** Each PoC fix this session was JSON-edit → mechanical Scala update to match generator emission. No new Generator features needed; the pattern is already there.
- **`who-extends` is doing real corpus work, not just answering exploratory questions.** First run with a working version surfaced a corpus-wide data-quality gap and became the source of #38. Reverse-derivation as a tool earns its keep.
- **TypeName equality is an example of a class of latent bugs.** Anonymous-class `apply` with no `equals`/`hashCode` override is the default in draco today. It hasn't bitten elsewhere because nobody compares those types structurally. Worth keeping the trap in mind as the corpus grows.
- **The dev's investigative pauses keep catching the agent.** Last chapter's "let's work this out in discussion" pattern repeated. This time it produced #38 directly — the data-quality finding could have stayed a parenthetical in the agent's summary if the dev hadn't said "you are correct" and turned it into tracked work.
- **Mid-session UI questions are real.** The Create PR widget had been on screen for who knows how long, the dev hadn't asked about it. When they did, the answer ("ignore — it's for multi-contributor flows you don't use") came in two sentences and landed cleanly. Worth noticing that the agent's job includes "what is this thing on my screen" sometimes, not just code.
