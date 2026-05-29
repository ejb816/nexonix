# Draco Dev Journal — Chapter 39

**Session date:** May 29, 2026
**Topic:** A full README alignment pass. The README had drifted ~6 weeks behind the architecture (last touched 2026-04-15, before Stages 2c/2d/2e, the DracoType-as-root shift, the aspect-block redesign, the JSON-normative migration, and the `src/mods` tier). Planned the rewrite as eight approval-gated steps, pivoted the example-domain strategy from the *centric reference frames to draco's own four endogenous domains, and landed every step. Closes with a clean terminology audit and a release-prep git record bundling the uncommitted chapter-38 work with this session's docs.

---

## Opening — the drift report

The session opened with a documentation request:

> **Dev:** The README.md is significantly out of data on this project. Please determine what it would take to bring it to date based on what's in memory in general, in source code and data and what is in dev-draco-journal chapters written since the last README update. Plan the update in steps that allow for approval and plan correction.

`git log README.md` put the last README-touching commit at `dada78e` (2026-04-15) — "Holon base trait, Transform[S,T], RuntimeCompiler, Primal→TypeInstance." Everything since (14 commits, journal chapters 24–38) was unrepresented. The gap audit surfaced the big-ticket staleness:

- The whole `TypeInstance` / `*Instance` trait triad — **deleted** (Stage 2d, ch. 27). README still had the `DracoType → TypeInstance → DomainInstance/...` diagram and `typeInstance: Type[T]` vals.
- **DracoType-as-root** (ch. 28) and the **aspect-block** redesign (ch. 25–27) — README's flat 14-field `TypeDefinition` table predated both.
- **JSON-normative policy** + YAML-as-authoring-stand-in (ch. 37) — README said "JSON as single source of truth" but described `TypeDefinition.load` (removed) and no YAML twin.
- **Transform split** → TypeTransform / DomainTransform (ch. 31); **`.actor.json` consolidation** into `actorAspect` (ch. 33); **base/ canonicalization** (ch. 34); **Extensible elimination** (ch. 30–31); **`comparisonOnlyExcluded → Map.empty`** byte-equivalence (ch. 38).
- Tooling: `bin/draco-gen` grown to 8 subcommands; `bin/draco-sc` + `src/mods` tier; alpha.2 / alpha.3 releases; backlog migrated to GitHub Issues.

Proposed an 8-step plan, each gated on approval: (2) Type System, (3) TypeDefinition+Aspects+TypeName+TypeElement, (4) the domain example backbone, (5) Code Generation + JSON-normative + Semantic Preservation, (6) Working Features + WIP, (7) Building/Structure/Getting-Started, (8) terminology audit. Asked three scoping questions — whether to add an Egocentric worked example, whether to trim Semantic Preservation, and whether to add a Getting Started section.

---

## The pivot: domain dictionary as ontological backbone

The first scoping question — use the Egocentric ten-element vocabulary as the README's worked example — drew a sharp redirect:

> **Dev:** Egocentric et al. is nowhere close to being sufficient as an example domain dictionary. The domain dictionary is the unifying artifact that all type definitions cohere around, ontologically speaking. It would be better to use the core endogenous domains in draco core as as foundational domain dictionary. Feel free to elide all references any of the *centric domains and their element types.

This was the strategic continuation of chapter 34's reference-frames demotion, but stated as an *ontological* principle rather than a time-budget call: the domain dictionary is the artifact type definitions cohere around, so the example domain dictionary has to actually be coherent. The *centric frames never were — they were skeletons built to exercise the Generator, not to teach the framework. Draco's own packages, by contrast, *are* a real domain dictionary: the meta-domain (Draco) plus Base, Primes, Language.

Reframed Step 4 around **The Four Endogenous Domains** — a table mapping each to what it demonstrates (Draco → self-description, Base → `Primal[T]` + sealed hierarchies, Primes → the rule engine, Language → the authoring surface + `Generator[L]` trajectory). Flagged one wrinkle: there is no draco-endogenous actor type, so the actor section can't have an endogenous worked example. Offered three options and recommended (c) — describe `ActorAspect` structurally, leave `NaturalActor` as the test-only demonstration.

The second round of scoping answers:

> **Dev:** 1. Use TypeTransform / DomainTransform type terms. The term transform domain has meaning in the context of a domain transform. We can work out the details interactively.
> 2. Minimal getting started walkthrough, to be extended and expanded subsequently.

Locked: TypeTransform / DomainTransform in the prose, with "transform domain" reserved for the situational sense (a `DomainTransform` *is* a domain); a minimal Getting Started as a seed for later expansion. Defaulted the actor wrinkle to (c). Plan finalized; began executing.

---

## Steps 2–3 — type system and aspects

Before writing, grounded each claim in source rather than memory — read `DracoType.scala`, `Primal.scala`, `Holon.scala`, `Aspects.scala`, the four `*Aspect.scala`, `TypeDefinition.scala`, `Type.scala`, `TypeTransform`/`DomainTransform`, plus `Meters`/`Distance`/`Cardinal`/`Coordinate`/`Unit` and an actual rule (`AddNaturalSequenceRule` + its JSON).

Two corrections came directly from that grounding:

- Memory's Key Facts still flagged "Holon[T] currently extends Primal[T]" as a latent issue, but `Holon.scala` now reads `extends DracoType`. Used the source — the "different axes" framing (Primal = value, Holon = perspective) is now literally true in the type hierarchy, not just conceptually.
- The old README said "`Primal[T]` now extends `TypeInstance`." Backwards twice over — `TypeInstance` is deleted and `Primal extends DracoType`. Fixed.

Step 2 replaced the diagram and rewrote "The Instance Triad" → "The Companion Val Convention" (the three vals every companion now carries: `typeDefinition`, `dracoType`, `domainType`, with a verbatim-faithful `Meters` example). Step 3 replaced the flat field table with the four aspect-block tables (`DracoAspect` / `DomainAspect` / `RuleAspect` / `ActorAspect`), documented the structural role predicates including `isLeaf` and the `domainAspect.typeName` self-loop, and added the `TypeElement.value` field with its YAML-round-trip rationale (#28).

---

## Step 4 — the sections were already there

A surprise on reaching Step 4: the Domains / Four Endogenous Domains / Rules / Actors sections were **already present and current** in the file, aspect-block-aware and matching the plan — even though the only edits this session had been Steps 2–3 at the top.

The explanation is context compaction: earlier turns of this same session (since summarized away) had already executed Step 4, and those edits were on disk. Rather than blindly rewrite, verified the existing content against source — `Cardinal extends Unit with Primal[T]`, `Coordinate extends Holon[T]`, `Unit extends DracoType` all checked out — and moved on. An honest "this is already done correctly" beat a redundant rewrite.

---

## Step 5 — code generation, JSON-normative, transforms

Grounded the dispatch description in `Generator.scala`: the real `generate(td)` is a flat predicate chain — `isRule → isDomain → isObjectOnly → isLeaf || isActor → throw`. The old README's 4-way list named the deleted `RuleInstance` / `TypeInstance`. Rewrote to the 5-way table (with the exhaustivity-guard throw), removed `TypeDefinition.load` (generated code calls `Generator.loadType` / `loadRuleType`), and added the three policy paragraphs: JSON-normative loading, the generator-canonical package (empty `comparisonOnlyExcluded`), and YAML-as-authoring-stand-in via `from-yaml` / `to-yaml`.

Semantic Preservation item 4 had the stale `TransformDomain` / `sourceDomain` / `sinkDomain` framing. Rewrote to `TypeTransform[S, T]` / `DomainTransform[S, T]` (both `extends Holon[(S, T)]`, source/target on `dracoAspect`), preserving the dev's "transform domain" sense — a `DomainTransform` is itself a domain.

---

## Step 6 — features, curated against source

The Working Features list had grown to ~40 flat bullets carrying multiple retired entities (`ActorInstance`, `RuntimeCompiler`, `TypeDefinition.load`, un-split `Transform`, `Primal extends TypeInstance`, the `31/48` baseline, reference-frame increments). Regrouped under Type system / Code generation / JSON-YAML / Rule engine / Actors / Domains / Tooling, every item re-checked. Verified the live tool surface before asserting it: `GeneratorCLI.scala` confirmed all 8 `draco-gen` subcommands (`generate` / `compile` / `compile-multi` / `inspect` / `discover` / `verify` / `from-yaml` / `to-yaml`), and `bin/draco-sc` confirmed the six-script set.

Work in Progress now opens with a pointer to GitHub Issues for the live backlog and keeps Generator-domain / Dreams / Orion as the named in-flight directions.

---

## Step 7 — building, structure, getting started (and a correction)

A correctness check caught an overstatement I'd drafted: the WIP said Dreams/Orion are "developed under `src/mods/`." `find` showed `draco.dreams` (Dreams.scala, Service.scala, orion/) still lives in `src/main/scala/draco/dreams`, and `src/mods` holds only the scripts. So mods is the *intended* home, not the current one. Reworded to say exactly that.

For Getting Started, verified `runGenerate` actually **prints to stdout** rather than writing the file — so the walkthrough redirects (`bin/draco-gen generate … > …Celsius.scala`) instead of falsely claiming the command writes in place. The example adds a `Celsius` leaf to Base (derive `Cardinal[Double]`, name Base as container, give a factory), generates it, registers it in `Base.json`'s `elementTypeNames`, and uses it — then `verify` to confirm the round-trip.

Project Structure was rewritten from the real tree: dropped the gone `primes/rules/` (rules are flat `*.rule.json`) and the duplicate test-`draco/` line; added `language/`, `src/mods/scala/scripts/`, `bin/`, the `dreams/` scaffold, and the YAML-twin note. Building gained `sbt assembly`, `DracoGenTest`, the `bin/` tool examples, and the alpha.1–alpha.3 release line.

---

## Step 8 — terminology audit

A grep sweep for every retired term — `TypeInstance` / `*Instance`, `typeInstance` vals, `TypeDefinition.load`, `.actor.json`, `TransformDomain` / `sourceDomain` / `sinkDomain`, `RuntimeCompiler`, `primes/rules`, un-split `Transform[`, `GenerateAndCompileTest` baselines. Every survivor was either an intentional "removed / dissolved / not a separate X" mention or a legitimate current use (`Natural.actorType`, the `draco.actor` capability domain, the split transforms). No stale assertions remained. README net change: **+319 / −200** across one file.

---

## The resume

The session was interrupted mid-Step-7 (at the `runGenerate` permission prompt) and resumed with:

> **Dev:** Review the current version of README.md and align it with action steps you proposed in this session for updating it, and let me know if there any remain steps to be acted on and what they are.

Re-read the file tail, confirmed Steps 2–6 landed and Step 7 was partial (Getting Started not yet added, Project Structure still stale), then finished both and ran the Step 8 audit. Reported the per-step status table and flagged two optional follow-ups: actually running `draco-gen generate` on a real `Celsius.json` to make the walkthrough bulletproof, and a parallel CHANGELOG.md refresh.

---

## What stuck

- **The example domain dictionary must itself be a coherent ontology.** The dev's pivot wasn't "the *centric frames are a time sink" (that was chapter 34) — it was "a domain dictionary is the artifact type definitions cohere around, so a teaching example has to be one." Draco's own four endogenous domains *are* a domain dictionary; the reference frames never were. The README now teaches the framework through the framework.
- **Context compaction can hide completed work — verify, don't assume.** Reaching Step 4 and finding it already done was disorienting, but the right response was to check the on-disk content against source and confirm, not to re-execute. The summary had dropped the turns where Step 4 landed; the file hadn't.
- **Ground every doc claim in source.** Two README assertions (Holon's parent, Primal's parent) were stale-in-memory but the source was current; one drafted claim (Dreams in mods) was the reverse — aspirational stated as done. Reading `Generator.scala`, `Holon.scala`, and `find src/mods` caught all three. Docs drift in both directions.
- **A walkthrough that lies about tool behavior is worse than none.** Checking that `draco-gen generate` prints to stdout (rather than writing) changed the Getting Started example from wrong-but-plausible to correct. The one place a reader will literally copy-paste is the place to be most careful.
- **Approval-gated steps kept the rewrite honest.** Eight checkpoints meant the dev could redirect the example-domain strategy after Step 1's plan without unwinding written prose — the pivot cost nothing because it landed before Step 4 was (re)touched.
