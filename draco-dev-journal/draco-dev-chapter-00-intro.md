# Draco Dev Journal — Introduction

This journal documents the collaborative development of Draco, a self-describing domain-driven rule engine, across sixty-two chapters of development sessions between March 22 and July 13, 2026. An earlier session predating the journal (where initial companion object consistency work began) is referenced in Chapter 2 but was not captured. The sessions are transcribed as dialogues between Dev (the framework's creator) and the model (Claude, serving as pair-programming partner), capturing not just the code changes but the reasoning, missteps, and discoveries along the way.

## Journal Conventions

The canonical chapter format is a near-verbatim transcript:

- **Assertive Dev prompts** — prompts in which Dev directs, questions, decides, or corrects — are transcribed verbatim, labeled **[Dev]**.
- **Model responses** to those prompts are transcribed near-verbatim, labeled **[Claude]** (earlier chapters use **Dev:** / **Draco:**; the content contract is the same).
- **Responsive prompts** — Dev pasting back the results of commands or actions the model asked Dev to execute (`sbt test` output, tool runs) — are *not* transcribed as prompts. They are summarized and folded into the model's preceding response as an italic parenthetical, e.g. *(Delegated action — Dev ran `sbt test`: all 195 pass.)*
- Each chapter opens with a header block (**Session date**, **Topic**) and closes with a **Status** section (corpus counts, test counts, queued follow-ups).
- Exchanges are separated by `---` rules; code, tool output, and file content appear in fenced code blocks.

Chapter 15 documents the original transcription methodology (session data as source of truth, self-propagating format). The journal is now maintained from outside the development sessions — chapters are rebuilt and appended from the recorded session data rather than by the session-resident model — precisely so that journal upkeep never disrupts the development flow, and so the format cannot devolve.

A note on provenance: chapters 1–21 are original in-session transcripts; chapters 37–58 were rebuilt to the canonical format from recorded session data in July 2026; chapters 59–60 were rewritten in-session the same way. Chapters 22–36 remain in their original summary form — their session data had already been purged by Claude Code's retention window before the rebuild, and the journal does not fabricate dialogue it cannot source.

## Development Trajectory

### Where It Started

When the first session began, Draco already had a working type hierarchy, a RETE-based rule engine (via Evrete), Pekko actors, and Circe JSON serialization. But the framework was fragile in ways that weren't yet visible. Companion objects used plain `val` for fields that crossed object boundaries, a timebomb hidden by Scala 2's `DelayedInit` trait. The Generator could produce basic Scala source from type definitions but had never been tested against its own output. Three separate definition types — `DomainDefinition`, `RuleDefinition`, `ActorDefinition` — each carried their own structure, creating asymmetry in what was meant to be a symmetric system.

### The Arc in Six Movements

**Foundations (Chapters 1–18, March 22–31).** The structural root (`Extensible`/`Specifically`), actors as thin membranes over rules, the `lazy val` discipline against `DelayedInit`, codec generation, and the culminating unification of all three definition types into a single `TypeDefinition`. JSON files became the single source of truth, with generated Scala loading definitions from the classpath. The first alpha shipped (Chapter 17).

**Self-closure and the reference frames (Chapters 19–23, April 9–29).** `Holon[T]` and `Transform[S,T]` were born alongside the RuntimeCompiler — generate→compile→load→verify in a single test run. The `draco-gen` CLI gave the agent sub-second iteration. Four nested reference-frame domains (Egocentric through Galactocentric under Cosmocentric) exercised transform domains, and the Holon-vs-Primal axis distinction (perspective vs value) reclassified the whole vocabulary.

**The TypeDefinition consolidation (Chapters 24–31, May 4–13).** Aspect blocks (`DracoAspect`/`DomainAspect`/`RuleAspect`/`ActorAspect`) replaced filename-suffix aspects; all 114+ JSONs migrated. The four `*Instance` marker traits were deleted, then `Extensible` itself — DracoType became the sole root, with every instance carrying its own `typeDefinition`. An alphabetical canonicalization sweep made the entire `draco` package byte-equivalent to Generator output, and `Transform` split into `TypeTransform`/`DomainTransform`.

**Process and the endogenous pivot (Chapters 32–39, May 13–29).** The backlog moved from memory-file state to GitHub Issues; roadmap issues formalized the Haskell port, Generator[L], Maven Central, and Orion. The strategic redirect: deprioritize the pedagogically opaque reference frames in favor of draco-endogenous usability. `src/mods/` arrived as the third source tier — the permanent home for "draco-on-top-of-draco" (and eventually Dreams/Orion). JSON was formalized as the normative definition surface, and `comparisonOnlyExcluded` reached empty: no hand-customized type declarations remained.

**The World example domains (Chapters 40–50, June 1–24).** `DomainBuilder` and the two-track build model opened the movement; the missing TransformBuilder fixture became a whole example world of message domains (Aerial/Terrestrial/Marine/Ethereal under `World`). The Evrete Environment emerged as the rule↔ActorRef seam keeping generated rules Pekko-agnostic. Draco's founding thesis crystallized — *a transform is correct iff it preserves meaning* — and became a passing assertion when an Aerial `Position` crossed to a Terrestrial `Location` through the `Observable` world-fact with WGS84 geodesy. The reference frames were deleted wholesale; actor emission folded into the Generator via the `setupAction`/`messageAction`/`signalAction` model; `Assembly` made actor topology pure data.

**DRAKE (Chapters 51–62, June 25–July 13).** Draco's native definition language was named — DRAKE, "domain rules actor knowledge engine" — with JSON remaining canonical and `.drake` the human-authoring surface. The metamodel was authored in DRAKE, aspect-head grammar settled, YAML retired entirely, `CodecAspect` became the fifth aspect, and every codec under `src/main` now derives from JSON — no hand-authored codec strings remain. The corpus buildout (60 `.drake` files against 63 JSONs) surfaced a genuinely missing metamodel piece (`Local`/`loc`) and the deepest methodological lesson of the journal: round-trip tests preserve mis-modeling; validating a model requires a second, more opinionated projection that rejects what the lax one tolerates.

### The Breakthroughs

Selected moments that shifted the framework's trajectory:

**Extensible and Specifically** (Chapter 1). A non-parameterized structural root with Generator-substituted type parameters, and its dual for deferred specialization — generalization and specialization expressed structurally, without the language fighting back.

**Actors as thin membranes** (Chapter 1). Rules handle all logic within a domain; actors are merely the membrane between domains — `session.insert(msg); session.fire(); Behaviors.same`. The rule engine and actor system became one integrated design.

**The DelayedInit reckoning** (Chapters 2, 6). A single NPE uncovered a systemic minefield: every `val` in an `extends App` companion must be `lazy val`. The four-layer bug hunt in Chapter 6 showed how self-referential systems hide bugs behind other bugs.

**TypeDefinition unification** (Chapter 14). Dissolving the three definition types into one exposed circular initialization in factory defaults and produced a general rule: factory defaults for types that participate in self-description must be deferred.

**JSON as single source of truth** (Chapters 10–11, formalized 37–38). Classpath JSON became canonical; by Chapter 38 this was a fix-direction principle — when JSON and Scala disagree, fix the JSON and regenerate.

**Holon vs Primal** (Chapter 23). Perspective and value are orthogonal axes — a reclassification that became a permanent design rule.

**Aspect blocks and the Haskell test** (Chapter 24). Four keyed aspects in one TypeDefinition, admitted by a durable criterion: a field belongs in the language-neutral model only if it would survive a Haskell projection. Chapter 55 sharpened it — a field earns canon-membership only if non-derivable *and* consumed by a cross-language backend.

**DracoType as sole root** (Chapters 28, 30). The `*Instance` triad deleted, then Extensible itself — every draco instance carries its own `typeDefinition`, and the whole package became generator-canonical.

**The Environment seam** (Chapter 42). Rules reach ActorRefs by role-name through the Evrete Environment — generated rules stay completely Pekko-agnostic while still emitting sends.

**"Preserves meaning" as a passing assertion** (Chapters 44, 46). The founding thesis — a transform is correct iff it preserves meaning — made checkable: lat/lon recovered to <1e-6 through the canonical frame.

**The latent/actual split** (Chapter 54). Everything DRAKE authors is latent (definition-plane) by construction; Evrete firing and Pekko delivery are actual (execution-plane). The ontology became a working decision rule.

**All codecs derive** (Chapter 57). No hand-authored codec strings anywhere in `src/main` — the Chapter 55 thesis ("a custom codec is a derivation gap") proved by construction.

**Specification vs recognizer** (Chapter 60). Stating what should be there is a specification; catching what isn't requires a recognizer run over the whole population. Round-trips preserve mis-modeling; only an opinionated second projection (DRAKE, Haskell) rejects it.

### Where It's Heading

- **Generator[L]** — the language-parameterized super-domain, the north star from Chapter 43 onward: generation reframed as `Format[Json] => Format[Draco] => Format[L]`, with actors and rules doing the generation work. The self-port to Haskell is the validating criterion. By Chapter 58, all open architecture (the presence/inference model, declared-codec migration, Encoder/Decoder subelements) is explicitly deferred to Generator[L]; the procedural `draco.Generator` is frozen.

- **DRAKE** — draco's native authoring tongue (`.drake`), transparent to canonical JSON. The corpus is complete at 64 files — the drake-less and host-code tails both closed in Chapter 61. Next: the `JSON → .drake` emitter (emitter-first, parser after), with the Monadic-bare-expression and host-reserved-domain-name lints queued.

- **Dreams** — the user development layer (Domain Rules Editor Actor Message Service), second layer of the three-layer charter stated in Chapter 40: draco → Dreams → Orion. `src/mods` hosts its early stand-ins (`DomainBuilder`, and eventually TransformBuilder — whose `validate` is exactly "does meaning survive the change of representation").

- **Orion** — the outer system-of-systems runtime (roadmap issue #23), owner of actor multiplicity, wiring automation, and the deferred `MessageDomain` concept.

- **Primes / PON** — combinatorial induction over Prime Ordinal Notation as "draco in draco," the proving ground for DRAKE and the disciplined Monadic register.

The recurring theme across all sixty sessions is that self-description creates complexity: a type system closed over itself means every change to a foundational type can create circular dependencies, initialization-order bugs, and shadowing issues. The `lazy val` discipline, deferred factory defaults, classpath loading, the Haskell test, and finally DRAKE-as-recognizer are all engineering responses to the same mathematical property. The framework is learning to describe itself without tripping over its own reflection.

**Status at Chapter 62:** JSON corpus 64, `.drake` corpus 64 (complete), full suite 197/197; value expressions now structured trees (`TypeElement.value: Json`), with the principle "form is drake's, atoms may be the target's."

---

## Chapters

### [Chapter 1 — Extensible, Specifically, Actors as Membranes, Pipeline Validation](draco-dev-chapter-01.md)

The most architecturally significant early session. Created `Extensible` (non-parameterized structural root) and `Specifically[T]` (deferred specialization). Defined actors as thin membranes with all logic in rules. Validated the full Alpha-to-DataModel-to-Bravo pipeline end-to-end. Introduced Orion and the five ION patterns.

### [Chapter 2 — lazy val Normalization, DelayedInit Discovery, Generator Pre-Requisites](draco-dev-chapter-02.md)

The session that uncovered the `DelayedInit` timebomb. A `NullPointerException` in a rule companion led to a sweep of all 28+ companion objects, changing `val` to `lazy val` for every cross-object field. Established the foundational rule and identified Generator pre-requisites. Introduced language-neutral naming (`typeGlobal`, `globalElements`).

### [Chapter 3 — Generator Renames, Factory Semantics, Transform Domain Cleanup](draco-dev-chapter-03.md)

Implemented the three Generator pre-requisites from Chapter 2. Established `Factory.Null` semantics, cleaned up the transform domains (Alpha, DataModel, Bravo, Charlie, Delta), and designed the multi-type generation approach with topological sorting via `moduleOrder`.

### [Chapter 4 — Multi-Type File Generation, JSON Embedding Plan](draco-dev-chapter-04.md)

Implemented multi-type generation for sealed hierarchies like `TypeElement`. Discovered a subtlety: `sealed` is driven by the `modules` field, not by annotation. Proposed replacing composed factory-call literals with embedded JSON strings, and discovered a pre-existing `RuleDefinition` decoder bug.

### [Chapter 5 — JSON Embedding, Confidence Assessment, Generator Roadmap](draco-dev-chapter-05.md)

Replaced five literal-composition methods with two JSON-embedding helpers. A candid confidence assessment recommended against replacing hand-written framework types with Generator output, identifying the gaps that needed closing first. Established the natural sequence: consistency, imports, codecs, rules auto-generation.

### [Chapter 6 — Generator Consistency, Four-Layer Bug Hunt, Rule Imports](draco-dev-chapter-06.md)

The deepest early debugging session. Four layers of bugs peeled back one by one — `implicit lazy val` for codecs, `private lazy val` for rule fields, the `RuleDefinition` decoder reading the wrong JSON field, and missing imports in generated rule source. Corrected the `lazy val` rule and wrote the first `README.md`.

### [Chapter 7 — Codec Generation Planning](draco-dev-chapter-07.md)

A design-only session. Specified the three codec patterns in full detail, formalized elision rules (driven by factory parameter defaults), and noted the tension between elision and factory defaults.

### [Chapter 8 — Codec Generation Implementation, DomainDefinition Design](draco-dev-chapter-08.md)

Implemented all three codec generation patterns: simple field-based, discriminated union with `kind` dispatch, and `Codec.sub` wiring. Discovered that pattern detection order matters for intermediate sealed traits. Designed `DomainDefinition` with morphism semantics — source equals sink means endomorphism (Update), source differs from sink means Transform.

### [Chapter 9 — DomainDefinition, Domain\[T,U\] Experiment, TypeName Redesigns](draco-dev-chapter-09.md)

Created `DomainDefinition` across 18+ files, replacing the lightweight `DomainName`. A `Domain[T,U]` experiment failed against Scala's mixin requirements. Fixed `Actor.apply`, added `ActorDefinition` codecs, and redesigned `TypeName` with role-specific paths.

### [Chapter 10 — Logging, NaturalActor Fix, Triadic Symmetry, Aspect Naming](draco-dev-chapter-10.md)

Suppressed SLF4J/Pekko logging noise. Diagnosed and fixed the `NaturalActor` — `Actor.apply` was creating a no-op behavior, ignoring the custom `receive`. Made the triadic symmetry decision and introduced aspect-based naming conventions. Established JSON as the single source of truth.

### [Chapter 11 — TypeDefinition.load, typeParameters, JSON Definition Files](draco-dev-chapter-11.md)

The session where JSON-first generation came alive. `TypeDefinition.load` replaced inline embedding with classpath loading. Solved bootstrap recursion with `override lazy val`. Added `typeParameters` to `TypeName`. Created 10+ JSON definition files and the first successful generate-from-JSON tests.

### [Chapter 12 — Session Persistence, Journal Self-Propagation](draco-dev-chapter-12.md)

A brief session exploring session data persistence. Notable for the temporal self-reference: instructions from a later session bootstrapped this session's journal transcription, establishing the self-propagating pattern used for subsequent chapters.

### [Chapter 13 — Complete JSON Definitions, Generated Test Infrastructure](draco-dev-chapter-13.md)

Completed all remaining JSON definition files across the full type hierarchy. Built the test infrastructure for generated output (`src/generated/scala/draco/*.scala.generated` with diff comments). Reached 28 passing Generate tests. Documented the Generator evolution plan toward `Generator[L]`.

### [Chapter 14 — TypeDefinition Unification](draco-dev-chapter-14.md)

The culminating session of the foundations arc. Dissolved `DomainDefinition`, `RuleDefinition`, and `ActorDefinition` into a single unified `TypeDefinition`. Discovered and solved circular initialization in factory defaults. Collapsed the Generator from five overloads to two with detection-based dispatch. Established the DRACO.md symlink convention and the dev journal structure.

### [Chapter 15 — Journal Creation Methodology](draco-dev-chapter-15.md)

A meta-chapter documenting how the dev journal itself was built: using `/resume` to re-enter past sessions, a fixed self-propagating transcription prompt, and cross-reference correction. Describes the three properties that make the process work — session data as source of truth, self-propagating format, and the model as its own witness.

### [Chapter 16 — Update All Domains to New Conventions](draco-dev-chapter-16.md)

Flat packages (removed `rules/`, `actor/`, `unit/` subdirectories), aspect naming (`.rule.json`, `.actor.json`), empty JSON files populated, 9 Base element JSONs created, all 8 domain Scala sources standardized. Established domain discovery convention — JSON for domains contains only type identity; Generator discovers contents by scanning.

### [Chapter 17 — Alpha Release: CLI, REPL\[L\], sbt-assembly, GitHub Release](draco-dev-chapter-17.md)

Fixed override lazy val bug in 8 domain anonymous classes. Created CLI (object-only entry point) and REPL[L] (language-parameterized evaluator). Introduced `isObjectOnly` and `hasExplicitMain` in Generator. Published v2.0.0-alpha.1 to GitHub with fat JAR.

### [Chapter 18 — Generator Owns Type Loading, Rule/Actor Auto-Suffix](draco-dev-chapter-18.md)

Identified Int vs Integer bug in RemoveCompositeNumbers. Eliminated inline JSON duplication — rules load from classpath via Generator.loadRuleType. Moved all type loading from TypeDefinition.load to Generator. Established auto-suffix convention: Generator appends "Rule"/"Actor" from aspect naming. Began prime gap rules design.

### [Chapter 19 — Reference Frames, Holon, RuntimeCompiler](draco-dev-chapter-19.md)

The session that laid the transform-domain foundation. Four example domains renamed to nested reference frames (Egocentric/Geocentric/Heliocentric/Galactocentric) under a new Cosmocentric super-domain. `Holon[T <: Product]` was born, `Transform[S,T]` became the first two-parameter type, and the RuntimeCompiler enabled generate→compile→load→verify in a single test run — the first concrete step toward self-closure.

### [Chapter 20 — Increment A, the draco-gen CLI](draco-dev-chapter-20.md)

Reference Frames Increment A landed five domain skeletons. The pivotal move was `draco-gen`, a Bash-invokable CLI around the Generator, giving sub-second iteration instead of 60-second sbt cycles. Fixed cross-package derivation imports and established that generator output is canonical — hand-written Scala must match it byte-for-byte.

### [Chapter 21 — Increment B, Aspect Suffixes, loadType](draco-dev-chapter-21.md)

Per-frame leaf and assembly vocabularies (14 JSON+Scala pairs); inline `TypeDefinition` literals eliminated in favor of `Generator.loadType`. Introduced the aspect-suffix convention (`.rule`/`.actor` in `TypeName.name`) and decided `elementTypeNames` lives permanently in JSON. A nested-aspect-scope detour was deliberately rolled back.

### [Chapter 22 — Generator Simplification, Domain Tuples, Increment C](draco-dev-chapter-22.md)

Collapsed verbose domain-instance literals to `Domain[X](typeDefinition)` and fixed a `.rule`-suffix leak. The key insight: a domain transform is a "domain tuple" already encoded by `source`/`target` on TypeDefinition — `isDomain` became structural, unblocking Increment C's 12 transform-domain skeletons with no schema change.

### [Chapter 23 — Egocentric Endogenous Semantics, Holon vs Primal](draco-dev-chapter-23.md)

A deep redesign of Egocentric around endogenous units (turn-fractions, IPD-multiples — no external calibration). The load-bearing result: Holon = perspective, Primal = value — orthogonal axes that reclassified the entire vocabulary. Ten Egocentric elements landed, and a candid meta-discussion diagnosed option-flooding as the main conversation cost.

### [Chapter 24 — Aspect Blocks, the Haskell Test](draco-dev-chapter-24.md)

Opened the TypeDefinition consolidation arc. Sharpened Primal-vs-Holon, codified the `Extensible with` convention, and added the `extensible` field after applying the "Haskell test" (would this field survive a Haskell projection?). Stage 2a introduced aspect blocks — DracoAspect/DomainAspect/RuleAspect/ActorAspect as keyed sub-blocks of one TypeDefinition.

### [Chapter 25 — Bulk Aspect Migration, the Lean Direction, YAML](draco-dev-chapter-25.md)

Stage 2c migrated all 114 JSONs to aspect-block form; Aspects became an unsealed parent (future user-defined aspects). The strategic leap: the Lean-equivalent semantic-convergence direction, with RETE demoted to an execution optimization. Created `draco.language` with YAML as language-v1, validated by a 117/117 round-trip test.

### [Chapter 26 — Aspects-as-Parent, chainHits, SessionStart Hook](draco-dev-chapter-26.md)

Stage 2d partial landing: five aspect TypeDefinitions authored, `TypeDefinition extends Aspects`, and conditional emission via the `chainHits` derivation-walker. Installed a SessionStart hook to mechanically redirect the agent out of stray worktrees — a structural fix for a recurring cross-session failure.

### [Chapter 27 — Instance Traits Deleted, Naming Convention](draco-dev-chapter-27.md)

Stage 2d completed. Converged on `<root>Type` / `<root>Aspect` naming, then deleted the four `*Instance` marker traits entirely — the kind-instance contract became structural. The change cascaded through ~200 files via six migration scripts. Validated the convention-pivot-before-migration discipline.

### [Chapter 28 — Bootstrap Rescue, DracoType as Root](draco-dev-chapter-28.md)

A deep bootstrap-soundness rescue after TypeDefinition.scala was declared "hard to overstate how broken this is." Built `YamlToJsonBootstrap`, purged 17 back-compat defs, and made the entire `draco.*` package generatable from JSON — driving the shift to DracoType-as-root: every instance carries its own `typeDefinition`. Closed 156/156 green.

### [Chapter 29 — DomainAspect.typeName, Leaf Discrimination](draco-dev-chapter-29.md)

Stage 2e reframed: a `typeName: TypeName` field on `DomainAspect` makes every type self-declare its containing domain — a self-loop for domains, a container-pointer for leaves. The domain/leaf discriminator became locally computable. Migrated across schema, Scala, Generator, and 123 resource files.

### [Chapter 30 — Canonicalization Sweep I, Extensible Eliminated](draco-dev-chapter-30.md)

Began the alphabetical canonicalization sweep via `DracoGenTest`, making each hand-written `.scala` byte-equivalent to Generator output. Fixed an `ActorActor` suffix-doubling bug and polished emission style. Mid-sweep, Extensible was eliminated entirely — DracoType became the sole root.

### [Chapter 31 — Canonicalization Sweep II, Transform Split](draco-dev-chapter-31.md)

Second half of the sweep. `Transform` split into `TypeTransform`/`DomainTransform`; RuntimeCompiler and Specifically deleted (folded into Generator); Primal and Holon promoted to `extends DracoType`. Established encoder/decoder elision symmetry ("what the encoder elides, the decoder must tolerate") and brought the full corpus green.

### [Chapter 32 — GitHub Issues Migration](draco-dev-chapter-32.md)

A purely administrative session: nine custom labels, 19 backlog issues in deterministic priority order, and the `Imminent Tasks` sections excised from MEMORY.md in favor of a live GitHub backlog. Codified the lesson that separating planning from mechanical execution avoids mid-execution re-decisions.

### [Chapter 33 — Roadmap Issues, .actor Consolidation](draco-dev-chapter-33.md)

First real exercise of the Issues workflow: roadmap issues seeded for the Haskell port (#20), Generator[L] (#21), Maven Central (#22), and Orion (#23). Two stale pickups taught "verify the premise against current code before implementing." Consolidated `.actor` sibling types into the parent's `actorAspect`, completing residual Stage 2d debt.

### [Chapter 34 — The Endogenous Redirect](draco-dev-chapter-34.md)

A strategic redirect: reference-frame example domains deprioritized in favor of draco-endogenous improvements for external users. Purged vestigial metadata from 7 base types and surfaced the derivation-less-trait emission rule. All 246 tests green.

### [Chapter 35 — isLeaf, Dispatcher Refactor](draco-dev-chapter-35.md)

Closed #24 with the explicit `isLeaf` predicate and a flat 5-way `generate()` dispatcher with an exhaustivity guard — after learning that removing `else` from an if-chain quietly degrades the return type. Eight IntelliJ warning fixes followed, one cascading through three layers.

### [Chapter 36 — scala-cli Toolkit, the src/mods Tier](draco-dev-chapter-36.md)

Built the scala-cli script toolkit for runtime queries against the draco jar. Six reframings converged on the biggest structural move of the month: `src/mods/` as a third source tier — the home for "draco-on-top-of-draco" content and future Generator[L] per-language emission.

### [Chapter 37 — mods Layer Policy, JSON-Normative](draco-dev-chapter-37.md)

Two architectural decisions: speculative outer layers (`draco.dreams`, `draco.dreams.orion`) live permanently in mods with a one-way reference rule; and the YAML-vs-JSON policy inverted — JSON is the normative source form, YAML a human-authoring stand-in. Built `from-yaml`/`to-yaml` with git-aware safety and surfaced a latent TypeElement codec asymmetry.

### [Chapter 38 — Codec Symmetry Closed, comparisonOnlyExcluded Empty](draco-dev-chapter-38.md)

Closed the codec-asymmetry umbrellas — `comparisonOnlyExcluded` reached `Map.empty`: the last hand-customized type declarations eliminated. Shipped six tools (discover/verify, who-extends, diff-type, and more); who-extends immediately surfaced ~9 types whose derivation didn't reach DracoType. JSON-normative was promoted to a working fix-rule.

### [Chapter 39 — README Alignment, the Ontology Principle](draco-dev-chapter-39.md)

A full README alignment pass after six weeks of drift. The worked example shifted from the reference frames to draco's own four endogenous domains, on the principle that a teaching example must itself be a coherent ontology. Every retired term audited out; a verified Getting Started walkthrough added.

### [Chapter 40 — DomainBuilder, Two-Track Build, sbt Upgrade](draco-dev-chapter-40.md)

The first mods usability stand-in, `draco.DomainBuilder` (define/dictionary/validate/generate), whose rigorous `validate` immediately caught a real defect. Established the two-track build model and the invariant "a declared Draco domain member is JSON-backed." Stated the three-layer charter: draco → Dreams → Orion.

### [Chapter 41 — The World Fixture, First Generated Actor](draco-dev-chapter-41.md)

"Continue TransformBuilder" pivoted into a whole example world of message domains (Aerial/Terrain/Marine/Ethereal). Forced `draco.format.Format[T]` and the Generator's first actor-from-`actorAspect` emission — a green end-to-end Aerial Consumer actor and rule generated entirely from JSON. Named the recapitulation principle: the examples replay draco's own ontogeny.

### [Chapter 42 — The Environment Seam](draco-dev-chapter-42.md)

Closed the rule-RHS-to-ActorRef blocker: the Evrete Environment is the seam (`session.set("role", ref)` ↔ `ctx.getRuntime().get(role)`), keeping generated rules completely Pekko-agnostic. Built the full `Creator → Provider → Consumer` chain driven by a single `FlightIntent`.

### [Chapter 43 — Generator\[L\] as North Star, Four Media Complete](draco-dev-chapter-43.md)

The keystone statement: "When Generator becomes a programming-language-parameterized super-domain, actors and rules will do the work." Generation reframed as `Format[Json] => Format[Draco] => Format[L]`, with the Haskell self-port as validating criterion. Completed the four-media fixture with deliberately non-trivial cross-medium vocabulary; all Phase-1 pipelines green.

### [Chapter 44 — SourceSink/Transformer, the Founding Thesis](draco-dev-chapter-44.md)

A conceptual dialogue (via the trimurti and Castaneda's Eagle) resolved the linear triad into a `SourceSink` ↔ `Transformer` duality — and crystallized draco's founding thesis: a transform is correct iff it preserves meaning. `World` is the shared semantic invariant.

### [Chapter 45 — World as the Transformer](draco-dev-chapter-45.md)

Phase 2 opened with the realization that the Transformer is not a per-medium actor but the `World` domain itself. Settled the two-layer stack with the uniform format as the seam: SourceSink absorbs format diversity, World absorbs semantic work. The four media became World subdomains.

### [Chapter 46 — Observable, Meaning Preserved](draco-dev-chapter-46.md)

World's first transform. Settled the canonical coordinate frame (dual Geocentric/Heliocentric Cartesian) and the world-fact's name: `Observable`. An Aerial `Position` crossed to a Terrestrial `Location` through WGS84 geodesy — "preserves meaning" became a passing assertion (lat/lon recovered to <1e-6).

### [Chapter 47 — Sentient, Reference Frames Retired](draco-dev-chapter-47.md)

Egocentric became `Sentient`, a World subdomain in mods — then the Dev directed deleting all the `*centric` reference-frame domains wholesale. The first full-suite run caught a latent parameterized-self-domain bug, establishing the standing rule: scoped-green ≠ suite-green; full `sbt test` is the push gate. Suite 183/183.

### [Chapter 48 — Slice B End-to-End, Logging Migration](draco-dev-chapter-48.md)

The first cross-medium transform ran end-to-end through a live actor graph (Aerial adapter → World.Consumer → World.Provider → Terrestrial adapter). Then a dev-ergonomics pivot: test output migrated from `println` to a SiftingAppender file-only logger. Established "make the definitions as soon as possible" — defined is a separate, cheaper milestone than generated.

### [Chapter 49 — Discovery Scaffold, Two-Action Actor Model](draco-dev-chapter-49.md)

`ExampleDomainsGenTest` charted exactly which example types generate (26 match / 22 differ / 0 error), sorting the gaps into capability, cosmetic, and alignment buckets. A design dialogue settled the two-action actor model — `signalAction` as run-once setup, `messageAction` per-message — letting the Evrete session persist across messages. First half landed as a byte-identical no-op.

### [Chapter 50 — Actor Lifecycle Defined, Appendix A](draco-dev-chapter-50.md)

The actor lifecycle settled on two orthogonal dimensions: statefulness (an author choice, encoded structurally) and multiplicity (Orion-only, kept out via always-`def` emission). The Creator actor-fold closed end-to-end. Discovered the example-domain comments were unsourced prose — preserved verbatim in Appendix A before stripping (preserve-then-strip became a standing principle).

### [Chapter 51 — Assembly, Primes/PON, DRAKE Named](draco-dev-chapter-51.md)

`draco.Assembly` made actor topology pure data — validatable without Pekko, spawned by one generic spawner; the hand-written guardians retired. The Primes domain's true goal surfaced (combinatorial induction over Prime Ordinal Notation). And DRAKE — draco's native definition language, "domain rules actor knowledge engine" — was named, its bracket taxonomy settled. Suite reached 200/200.

### [Chapter 52 — Metamodel in DRAKE, Host Plumbing Expelled](draco-dev-chapter-52.md)

TypeDefinition and the five aspects authored in DRAKE. Three notation questions settled, producing the principle: DRAKE's value surface carries only unquoted, language-neutral expressions — anything that can't be written that way is host plumbing and belongs to the Generator.

### [Chapter 53 — Continuation Rules, Pattern Consolidation](draco-dev-chapter-53.md)

DRAKE gained newline-continuation rules. The first rule-drake exposed that RuleAspect carried its LHS in two places; consolidating into `pattern` walked into a pre-existing discriminated-encoder bug (subtype-only fields silently dropped), fixed with a round-trip guard. Suite 200/200.

### [Chapter 54 — Aspect-Head Grammar, Latent/Actual](draco-dev-chapter-54.md)

DRAKE's aspect-heads got their grammar (canonical order `type→domain→rule→actor`). A philosophical excursion landed the latent/actual ontology — draco's own definition-plane/execution-plane split — which drove two cleanups: `Actor[T]`'s vestigial factory removed, the `*Definition` alias triad retired.

### [Chapter 55 — CodecAspect, the Canon Criterion](draco-dev-chapter-55.md)

A fifth aspect entered the canon: `CodecAspect`, carrying one field — `discriminator: String` — lifting the last hardcoded codec magic string out of the Generator. Sharpened the membership criterion: non-derivable AND consumed by a cross-language backend. Suite 203/203.

### [Chapter 56 — YAML Retired](draco-dev-chapter-56.md)

YAML removed as a definition language — never really used, only tested. `draco.language.YAML`, its host sub-domain, the CLI conversions, the dependency, and 6 orphaned files all deleted. JSON is now the only definition surface, with DRAKE the authoring format in waiting. Suite 195/195.

### [Chapter 57 — All Codecs Derive](draco-dev-chapter-57.md)

Milestone: one new `elisionCheck` case made TypeDefinition's codec derive; the two embedded circe blobs were deleted; the inherited-elements "wart" erased via transitive derivation-walking. No hand-authored codec strings remain anywhere under `src/main`.

### [Chapter 58 — Presence and Inference](draco-dev-chapter-58.md)

A pure design session producing the presence + inference model: an aspect must be explicit iff its JSON can't be reconstructed from information already present. The entire model — declared-codec migration, Encoder/Decoder subelements — was explicitly deferred to the typed Generator[L]; the procedural Generator is frozen.

### [Chapter 59 — DRAKE Corpus Buildout](draco-dev-chapter-59.md)

Authored 33 of 39 missing `.drake` files — everything expressible in settled syntax (corpus 56/62). First corpus uses of multi-derivation `from` and type-application. Six files remained as a syntax-gap tail.

### [Chapter 60 — Local/loc, Specification vs Recognizer](draco-dev-chapter-60.md)

The `dyn`-in-body gap led through the bracket-optional grammar rule to a genuinely missing metamodel piece: the `Local`/`loc` body-element kind, discovered when a local `val` was found smuggled as host code in a `Monadic`. Implemented end-to-end, verified 195/195. Closed with the journal's deepest methodological post-mortem: round-trip tests preserve mis-modeling — validating a model requires an opinionated second projection, a recognizer rather than a specification.

### [Chapter 61 — Corpus Complete: Loose Ends, CLI(L), the scala Package Saga](draco-dev-chapter-61.md)

Journal duty formally handed to Cowork. Parser readiness settled emitter-first, then the loose ends closed: result-as-`Dynamic.value`, Primes and Value decomposed out of host code, CLI reconceived as CLI(L) with commands as data. The `draco.scala` package-shadowing saga killed the escape rule in favor of capability naming (`draco.scalasource.ScalaSource`). The `.drake` corpus reached completeness — 64 of 64 — at 197/197, committed and pushed with its audit record.

### [Chapter 62 — Value Expressions as Structured Trees](draco-dev-chapter-62.md)

Value-expression syntax settled: operator-ness is a property of the symbol, not the grammar. Dev's Action.json prototype (S-expression trees in JSON) exposed a misspelled `RHSContext` the opaque string had never caught — vindicating the recognizer lesson of Chapter 60. `TypeElement.value` went `String → Json`, expression trees spread incrementally through the corpus (Seq.empty, Primes' infix conditions, Value's five-level Haskell-form trees), and the governing principle emerged: form is drake's, atoms may be the target's. Five green runs, 197/197.
