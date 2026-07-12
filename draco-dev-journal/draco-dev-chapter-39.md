# Draco Dev Journal — Chapter 39

**Session date:** May 21–29, 2026
**Topic:** The full README alignment pass. The README had drifted ~6 weeks behind the architecture (last touched 2026-04-15, before Stages 2c/2d/2e, the DracoType-as-root shift, the aspect-block redesign, the JSON-normative migration, and the `src/mods` tier). An eight-step approval-gated plan, the pivot from the *centric reference frames to draco's four endogenous domains as the example backbone, every step landed, a clean terminology audit, then the release tail: journal + release-prep git record bundling the uncommitted chapter-38 work, CHANGELOG reconciliation, and the live `v2.0.0-alpha.4` release. Transcript.

---

**[Dev]** The README.md is significantly out of data on this project. Please determine what it would take to bring it to date based on what's in memory  in general, in source code and data and what is in dev-draco-journal chapters written since the last README update. Plan the update in steps that allow for approval and plan correction.

**[Claude]** *(after reading README.md, running `git log` on it, surveying the commits and journal chapters since, and walking the current source/resource/mods layout)* Read the current README, identified the last README-touching commit (`dada78e`, 2026-04-15), surveyed the 14 commits and 15 journal chapters since, and walked the current source/resource/mods layout. Here's what's stale and a step-by-step update plan.

**What's stale (gap audit).** Type system / instances (README is most wrong here):
- The whole `TypeInstance` / `DomainInstance` / `RuleInstance` / `ActorInstance` triad is **deleted** (Stage 2d, ch. 27). Companion val collapsed to `dracoType`/`domainType`/`ruleType`/`actorType`. `typeInstance: Type[T]` no longer exists.
- **DracoType-as-root architectural shift** (ch. 28, 2026-05-09): every `draco.*` type extends DracoType; every instance carries `typeDefinition`. README's diagram still has the old `DracoType → TypeInstance → DomainInstance/...` branching.
- **Aspects parent type + aspect blocks** (ch. 25–27): TypeDefinition fields are grouped as `dracoAspect`/`domainAspect`/`ruleAspect`/`actorAspect`. TypeDefinition extends Aspects.
- **Holon vs Primal** clarified (ch. 24): Holon = perspective marker; Primal = value carrier. README only name-drops Holon under Working Features.
- **`DomainAspect.typeName` + `isLeaf` predicate** (ch. 29, 35; #24): self-loop = domain, container-pointer = leaf.
- **Transform split into TypeTransform + DomainTransform** (ch. 31). **TypeElement gained `value`** (#28, ch. 38) — codec asymmetry resolved.

JSON / YAML / language: the **JSON-normative policy** (ch. 37) — JSON is the only load path, YAML is a human-authoring stand-in mediated by `bin/draco-gen from-yaml`/`to-yaml` with git-safety — is unrepresented; `draco.language` (Language, YAML) added; **`comparisonOnlyExcluded = Map.empty`** (#29, ch. 38) means the whole `src/main/scala/draco/*` package is byte-equivalent to Generator emission — a genuinely shippable round-trip property the README never mentions; **`.actor.json` sibling files removed** (#26, ch. 33) — actors are aspects of their parent type via `actorAspect`, `Generator.loadActorType`/`loadAll` are dead code, yet README still describes `.actor.json` aspect naming as current; **base/ types canonicalized** (#17, ch. 34); **Extensible eliminated** mid-sweep (ch. 30–31).

Tooling / structure: `bin/draco-gen` grew to include `from-yaml`, `to-yaml`, `discover`, `verify`; `bin/draco-sc` + `src/mods/scala/scripts/` (ch. 36) — the scala-cli toolkit; `src/mods/` as the third source tier (ch. 37) with the one-way reference rule (mods→main only); the Project Structure block is wrong on several lines (`primes/rules/` no longer exists, `language/`/`dreams/`/`src/mods/`/`bin/` unlisted); releases v2.0.0-alpha.2 and alpha.3 shipped; the backlog lives at GitHub Issues now (ch. 32), not in-prose. Examples: reference-frames de-emphasized (ch. 34) — draco-endogenous usability is the focus.

**Proposed update plan (approval gate per step).** Step 1 — outline approval (keep the current top-level shape but rename "The Instance Triad" → "Companion Val Convention", add an "Aspects" subsection). Step 2 — Core Idea + Type System rewrite (everything extends DracoType, companion val collapse, no `*Instance` references). Step 3 — TypeDefinition + Aspects + TypeName + TypeElement (the four aspect blocks, Holon-vs-Primal, `isLeaf`, Transform split, TypeElement `value`). Step 4 — Domain / Rule / Actor sections (new convention, no `.actor.json`, flat rule paths). Step 5 — Code Generation (current 5-way dispatch + exhaustivity guard), JSON-normative policy, `draco.language`, byte-equivalence. Step 6 — Working Features pruned + Work in Progress pointed at GitHub Issues. Step 7 — Tooling + Project Structure + Building/Releases. Step 8 — final terminology audit (`grep` for `TypeInstance`, `.actor.json`, `TypeDefinition.load`, … and explain any survivors).

**Question before I start.** Any sections you'd rather collapse, split, or drop? In particular: (1) add the full **Egocentric ten-element worked example** between Domains and Rules, or keep the README leaner? (2) "Semantic Preservation in Data Transformation" still reads true but uses the old `TransformDomain` wording — rewrite to TypeTransform/DomainTransform, or trim? (3) There's no **Getting Started** section — add a minimal "write one type, generate, load" walkthrough in this pass, or defer?

---

**[Dev]** Egocentric et al. is nowhere close to being sufficient as an example domain dictionary. The domain dictionary is the unifying artifact that all type definitions cohere around, ontologically speaking. It would be better to use the core endogenous domains in draco core as  as foundational  domain dictionary. Feel free to elide all references any of the *centric domains and their element types.

**[Claude]** Got it — the *centric domains drop out, and the four endogenous domains (Draco, Base, Primes, Language) become the worked-example backbone. That actually strengthens the plan rather than weakening it: draco's own type system *is* a domain dictionary, so the framework demonstrates itself.

**Step 4 reframed — "The Four Endogenous Domains":**
- **Draco** — the meta-domain. Its members are the type system itself (DracoType, Primal, Holon, Aspects, the TypeElement family, TypeDefinition, TypeName, Domain, Rule, Actor, …). Demonstrates self-description: TypeDefinition is described by a TypeDefinition.
- **Base** — measurement primitives (Cardinal, Distance/Meters, Rotation/Radians, Ordinal, Nominal, Coordinate). Demonstrates `Primal[T]`, sealed type hierarchies, and the canonicalized-after-#17 minimal shape.
- **Primes** — rule-driven domain (Accumulator, Numbers, Primes + three RETE rules). Demonstrates RuleAspect, condition compilation, and Evrete integration.
- **Language** — language tooling (Language, YAML). Demonstrates the trajectory toward `Generator[L]` and shows YAML-as-authoring-stand-in concretely.

That covers every framework feature without inventing example domains, and lines up with the GitHub Issues focus shift ("draco-endogenous usability over example domains," ch. 34).

**One wrinkle to flag:** there is **no draco-endogenous actor type** — actors are demonstrated only in tests (`NaturalActor`). Options: (a) use `NaturalActor` labeled as test-resident demonstration; (b) describe ActorAspect structurally with a small invented snippet; (c) skip an actor worked example entirely, cover Actor only at the type-system level — **recommended**, keeps the README endogenous.

Two questions still open from my previous message: (1) Semantic Preservation — keep with terminology updated, or trim to a one-paragraph statement of intent? Recommend trim — the `TransformDomain` framing predates the split and the section feels aspirational. (2) Getting Started — add minimal now, or defer? Recommend defer — a real one should walk `bin/draco-gen` and `bin/draco-sc` end-to-end. Confirm (or correct) (a/b/c), 1, and 2, and I'll start Step 2.

---

**[Dev]** 1. Use TypeTransform / DomainTransform type terms. The term transform domain has meaning in the context of a domain transform. We can work out the details interactively.
2. Minimal getting started walkthrough, to be extended and expanded subsequently.

**[Claude]** Locked in: (1) TypeTransform / DomainTransform terminology; "transform domain" reserved for the situational sense (a DomainTransform-as-domain), details to be worked interactively. (2) Minimal Getting Started walkthrough (define one type in JSON → generate → load), positioned as a seed for later expansion. Defaulting on the open wrinkle: **actor example = (c)** — skip the worked example, describe ActorAspect at the type-system level only; `NaturalActor` stays a test-only demonstration. Push back if you'd rather see (a) or (b).

**Final locked plan:** Step 2 — Core Idea + Type System; Step 3 — TypeDefinition + Aspects + TypeName + TypeElement (incl. `value`, `isLeaf`); Step 4 — The Four Endogenous Domains as the worked-example backbone, replacing the old "Base Domain" + "Dreams" subsections; Step 5 — Code Generation (5-way dispatch) + JSON-normative policy + byte-equivalence; Step 6 — Working Features + Work in Progress; Step 7 — Tooling + Project Structure + Building/Releases + minimal Getting Started; Step 8 — final terminology + cross-ref audit. Semantic Preservation I'll rewrite in-place during Step 5 (it pairs naturally with TypeTransform/DomainTransform). Ready to start Step 2 on your go-ahead — I'll write the section, surface the diff, and pause for approval before Step 3.

---

**[Dev]** Go ahead

**[Claude]** *(grounded the rewrite in current source before writing — read `DracoType.scala`, `Primal.scala`, `Holon.scala`, `Aspects.scala`, the four `*Aspect.scala`, `TypeDefinition.scala`, `Type.scala`, `TypeTransform`/`DomainTransform`, plus `Meters`/`Distance`/`Base`/`Primes` and the `AddNaturalSequenceRule` + its JSON — then edited README.md)* Step 2 complete. Here's what changed in the **Type System** section.

**Diagram** — replaced the old `DracoType → TypeInstance → DomainInstance/RuleInstance/ActorInstance` branch (all deleted) with the current shape: `Primal[T]` and `Holon[T <: Product]` as peers under DracoType (Holon extends DracoType directly in the current source, not Primal — the "different axes" framing); `Type[T]`, `Aspects → TypeDefinition`, `DomainType → Domain[T]`, `RuleType → Rule[T]`, `ActorType → Actor[T]`; `TypeTransform[S,T]` and `DomainTransform[S,T]`, both `extends Holon[(S,T)]`.

**"The Instance Triad" → "The Companion Val Convention"** — rewrote entirely: states the `*Instance` traits are dissolved; documents the three companion vals every type now carries (`typeDefinition`, `dracoType: Type[T]`, `domainType: Domain[D]`); a concrete `Meters` companion example, verbatim-faithful to the actual source (`extends App with DracoType`, `lazy val`s, factory `apply`, `Null`); notes the `DelayedInit`/`lazy val` rule; closes with: roles are now expressed by *which aspect block the TypeDefinition populates*, not by trait — teeing up Step 3.

Two small accuracy notes I corrected against source while writing: memory flagged "Holon extends Primal" as a latent issue, but the actual `Holon.scala` now reads `extends DracoType` — I used the source. And `Type[T]` is a real `DracoType` subtype and now appears in the diagram (it was implied but never shown before). Approve this section and I'll proceed to **Step 3**, or tell me what to adjust.

---

**[Dev]** Approved to proceed

**[Claude]** *(read the README sections to be edited, `TypeElement.scala`, and `Generator.scala`; confirmed `extensible` is the optional head-of-`extends` slot; edited README.md)* Step 3 complete. Changes to the **TypeDefinition / TypeName / TypeElement** sections:

**"TypeDefinition" → "TypeDefinition and Aspects"** — replaced the flat 14-field table (which mixed domain/rule/actor fields into one list) with the actual aspect-block structure: a tree showing `TypeDefinition extends Aspects` + the four blocks, and separate field tables per aspect matching the actual trait definitions I read — `DracoAspect` (derivation, extensible, modules, elements, factory, globalElements, superDomain, source/target — with `extensible` honestly described as the usually-empty legacy head-of-`extends` slot), `DomainAspect` (typeName self-loop vs container-pointer, elementTypeNames), `RuleAspect` (variables, conditions, values, pattern/action), `ActorAspect` (messageAction/signalAction). Replaced "roles by populated fields" prose with the explicit structural-dispatch predicates (Domain / Leaf / Rule / Actor), including `isLeaf`.

**TypeName** — swapped the stale `domains.natural.Natural` examples for real ones (`draco.base.Meters`); corrected the domain-ness rule to reference the `domainAspect.typeName` self-loop rather than "name matches last package element."

**TypeElement** — added the `value` field to the trait description and the diagram; added a real `Fixed`-with-value JSON example (from the Primes rule); added the one-line note on *why* `value` exists (YAML→JSON round-trip default preservation, #28).

Approve and I'll proceed to **Step 4** — replacing the Domains / Rules / Actors / Base-Domain / Dreams sections with the Four Endogenous Domains backbone.

---

**[Dev]** Approve and proceed

**[Claude]** *(read the README from the Domains section onward)* This is unexpected — the Domains, Four Endogenous Domains, Rules, and Actors sections already contain current, aspect-block-aware content that matches the Step 4 plan, even though I only edited the top sections in Steps 2–3. Reading the whole file fresh to establish its true current state: the Step 4 sections are **already present and current** — that work landed in earlier turns of this session (the conversation was compressed, but the edits are on disk and match the Step 4 plan). I verified them against source as I reviewed — `Cardinal extends Unit with Primal[T]`, `Coordinate extends Holon[T]`, `Unit extends DracoType` all check out.

So on to the genuinely-stale **Code Generation** and **Semantic Preservation** sections (Step 5). *(Read `Generator.scala`; confirmed the dispatch is `isRule → isDomain → isObjectOnly → isLeaf||isActor → throw` and that generated code calls `Generator.loadType`/`loadRuleType`, no `TypeDefinition.load`; edited README.md twice.)* Step 5 complete. Two edits:

**Code Generation** (was the most stale prose-section remaining) — rewrote to the actual 5-way dispatch (replacing the old 4-way list that named the deleted `RuleInstance`/`TypeInstance`); removed `TypeDefinition.load` (doesn't exist); added the **JSON-normative** policy, the **generator-canonical / empty exclusion-map** achievement (the whole `draco/*` package is byte-equivalent to Generator output), and **YAML-as-authoring-stand-in** with `bin/draco-gen from-yaml`/`to-yaml`.

**Semantic Preservation, item 4** — replaced the stale `TransformDomain`/`sourceDomain`/`sinkDomain` framing with `TypeTransform[S,T]` and `DomainTransform[S,T]` (both `extends Holon[(S,T)]`, source/target on `dracoAspect`), and preserved "transform domain" in the situational sense you specified (a `DomainTransform` *is* a domain).

Remaining stale blocks: **Working Features** (references `ActorInstance`, `TypeDefinition.load`, un-split `Transform`, reference-frames, `31/48`, `primes/rules/`, `.actor.json`) and **Work in Progress**; then **Building / Dependencies / Project Structure**. Approve and I'll do **Step 6**.

---

**[Dev]** Approve

**[Claude]** *(verified the actual `bin/draco-gen` subcommands via grep on `GeneratorCLI.scala` — all 8 confirmed — and the `bin/draco-sc` script set, so as not to assert tools that don't exist; edited README.md)* Step 6 complete. The **Working Features** list went from a flat 40-bullet list (riddled with `ActorInstance`, `TypeDefinition.load`, `Primal extends TypeInstance`, `RuntimeCompiler`, `Transform`, `31/48`, reference-frame increments) to a curated set grouped under Type system / Code generation / JSON-YAML / Rule engine / Actors / Domains / Tooling — every item checked against current source.

Key corrections baked in: removed all deleted entities (`*Instance`, `RuntimeCompiler`, `TypeDefinition.load`, un-split `Transform`); `Primal extends DracoType` (the old bullet had it backwards as `extends TypeInstance`); added generator-canonical/empty-exclusion-map, JSON-normative + YAML twin, `isLeaf`, TypeTransform/DomainTransform, the full `draco-gen` subcommand set, `draco-sc` + `src/mods/`; reference frames demoted to a single "test-only, no longer a focus" line.

**Work in Progress** now opens with a pointer to GitHub Issues for the live backlog, keeps Generator-domain / Dreams / Orion (with their `src/mods/` homes noted), and flags the minimal Getting Started as appearing below. Approve and I'll do **Step 7** (Building / Releases, Dependencies, Project Structure refresh, and the minimal Getting Started walkthrough I just promised).

---

**[Dev]** Approve

**[Claude]** *(verified test class names, the `dreams` directory contents, and the `src/mods` layout)* Useful — `draco.dreams` (`Dreams.scala`, `Service.scala`, `orion/`) still lives in `src/main/scala`, and `src/mods` currently holds only the scripts. So the migration of dreams/orion into `mods` is a *direction*, not done. First, correcting that WIP overstatement. Then the Building section — adding releases, the IDE-driven workflow note, and the CLI tooling pointer. Now adding the minimal Getting Started walkthrough — first confirming what `draco-gen generate` actually does (writes a file vs. prints) so the walkthrough is truthful.

*(Session interrupted here, mid-Step-7, at the `runGenerate` behavior check.)*

---

**[Dev]** Continue from where you left off.

**[Claude]** *(session data gap — no response recorded for this prompt; work resumed with the following exchange.)*

---

**[Dev]** Review the current version of README.md and align it with action steps you proposed in this session for updating it, and let me know if there any remain steps to be acted on and what they are.

**[Claude]** Reviewed. Steps 2–6 are fully landed. Step 7 is **partially** done — the WIP dreams/orion fix and the Building/Releases additions are in, but two Step-7 items remain: the **Getting Started** walkthrough was interrupted before I added it, and **Project Structure** is still stale (`primes/rules/`, missing `language/`, `src/mods/`, `bin/`, duplicate test line). *(Verified the actual tree: `draco-gen generate` **prints to stdout** — doesn't write the file itself — primes rule Scala is flat, no `rules/` subdir; checked `test/resources`; then added Getting Started, rewrote Project Structure, and ran the Step 8 grep audit.)*

Audit is clean — every remaining occurrence of a "retired" term is an intentional *"removed / dissolved / not a separate X"* mention or a legitimate current use (`Natural.actorType`, the `draco.actor` capability domain, the split `TypeTransform`/`DomainTransform`). No survivors of `TypeDefinition.load`, `TransformDomain`, `RuntimeCompiler`, `primes/rules/`, `*Instance` vals, or stale test baselines.

**Status of the plan:** all eight steps landed — Steps 2, 3, 5, 6 done earlier; Step 4 present + verified; Steps 7 and 8 completed this turn. What I did this turn to finish alignment:
- **Getting Started** — a minimal JSON→generate→load walkthrough using a new `Celsius` leaf in Base. Grounded in real behavior: I confirmed `draco-gen generate` **prints to stdout**, so the example redirects to the file rather than falsely claiming it writes one.
- **Project Structure** — rewrote the tree: removed the gone `primes/rules/` (rules are flat `*.rule.json`) and the duplicate test `draco/` line; added `language/`, `src/mods/scala/scripts/`, `bin/`, the `dreams/` scaffold, and the YAML-twin note.
- **Step 8 audit** — the grep sweep above.

Remaining (optional, your call — nothing required for alignment): (1) the `Celsius` walkthrough is illustrative — if you want it bulletproof, I can actually run `bin/draco-gen generate` on a real `Celsius.json` to confirm the emitted trait/companion matches the prose (I did *not* create that file — it's an example, not committed). (2) **CHANGELOG.md** — not in scope of this plan, but it's the sibling doc and is presumably also behind; say the word and I'll bring it current the same way. The README is fully aligned with the proposed plan. Want me to do either optional item, or are we done?

---

**[Dev]** Time to add the next journal chapter in /Users/edwardburke/IdeaProjects/nexonix/draco-dev-journal and then create a commit message for the next git record in /Users/edwardburke/IdeaProjects/nexonix/draco-git-record, to prepare for a new release.

**[Claude]** *(studied chapter 38's format and the git-record directory, checked the working-tree state, wrote draco-dev-chapter-39.md, checked `build.sbt`, bumped the version, and created `git-record-2026-05-29-1449`)* Both deliverables are in place.

**Journal — draco-dev-chapter-39.md.** Documents this session: the README drift report, the eight-step approval-gated plan, the dev's pivot to draco's four endogenous domains as the example backbone ("a domain dictionary is the artifact type definitions cohere around"), the TypeTransform/DomainTransform terminology decision, each step's execution, and the Step 8 audit. Preserves the verbatim Dev exchanges per the journal convention, including the mid-session resume and the context-compaction surprise at Step 4 (sections already present — verified rather than rewritten).

**Git record — git-record-2026-05-29-1449.** A single release commit bundling **two uncommitted sessions** (chapters 38 and 39), then the `v2.0.0-alpha.4` tag push. The key thing I found while preparing it: **chapter 38's work was never committed.** The last commit is `62f50ac` (May 19, chapter 37). Everything in the working tree from #28/#29/#9/#37/#38-PoC/mods-scripts is staged but uncommitted — so the next commit must span chapters 38 *and* 39, exactly as `git-record-2026-05-13-1300` spanned 29–31.

**One change I made that you should confirm:** I bumped **`build.sbt` → `2.0.0-alpha.4`** (one line) so the release tag and the build agree — tagging alpha.4 with the build still saying alpha.3 would be inconsistent. If you'd rather hold the version, reject that edit in IDE review and drop the tag section from the record; the commit still stands on its own.

Notes / things to verify before running: the commit message claims **"full corpus green"** — that rests on your chapter-38-close report plus this session's source edits to CLI/REPL/Value/Unit; if any of those PoC edits post-dated your last green run, do an `sbt test` before committing. The record ends with the tag push following the established CI pattern (push tag → CI auto-creates release + uploads jar → `gh release edit --notes-file` for curated notes); I left the curated-notes step commented since you'll want to author those. Per your workflow, I haven't run any git/sbt — the record is yours to execute (or adjust) via the IDE. Want me to also refresh **CHANGELOG.md** for the alpha.4 entry, or draft the curated release notes?

---

**[Dev]** Refresh CHANGELOG.md

**[Claude]** The `[Unreleased]` section is itself stale — it describes mid-April work (Holon/Transform/RuntimeCompiler/reference-frames Increment A), most of which was *reversed or superseded* before alpha.3 shipped (TypeInstance dissolved, Transform split, RuntimeCompiler deleted, Extensible eliminated). And there's no `alpha.3` entry at all. So refreshing properly means reconciling that block into an accurate `alpha.3` net-change section plus a new `alpha.4`, grounded in the actual commit history rather than memory.

*(Grounded in the tag-to-tag commit log.)* Key correction to my mental model: **alpha.3 (tag `48b2a56`, 2026-05-17) already includes the entire stale `[Unreleased]` block** (the 2026-04-15 commits) plus chapters 21–36. The one post-alpha.3 commit (`62f50ac`, JSON-normative, chapter 37) **plus** the uncommitted chapters 38–39 are what alpha.4 will contain. What I changed:

- **`[Unreleased]`** — now an empty placeholder (Keep-a-Changelog style; this commit *is* the release).
- **`[2.0.0-alpha.4] - 2026-05-29`** — chapters 37–39: the JSON-normative load path, `TypeElement.value`/#28/#18, `comparisonOnlyExcluded → Map.empty`/#29, `from-yaml`/`to-yaml` + `discover`/`verify`/#9, the mods script batch, the #37 workaround, the #38 PoC (CLI/REPL/Value/Unit), the README rewrite, `YamlToJsonBootstrap` removal, version bump. Correctly scoped to include chapter 37 (`62f50ac`, already committed) because it sits after the alpha.3 tag.
- **`[2.0.0-alpha.3] - 2026-05-17`** — a reconciled net-change section for chapters 20–36 (DracoType-as-root, aspect blocks, `DomainAspect.typeName`, `isLeaf`, Transform split, `draco.language`/YAML, GeneratorCLI/`bin/draco-gen`, `src/mods`, reference frames A–C, GitHub Issues migration; companion-val collapse, `.actor` consolidation, base/ purge, generator-canonical sweep; removal of the `*Instance` family / Extensible / Specifically / RuntimeCompiler / inline TypeDefinitions / example scaffolds). Closes #1/#6/#8/#17/#24/#26/#27. Written as *net change from alpha.2*, so the reversed-in-flight items simply don't appear as adds — which is how a release changelog should read.

alpha.2, alpha.1, and the older unversioned sections are untouched. I also updated the git record's comment block to name CHANGELOG.md (it's already captured by `git add -u`). One judgment call to flag: the alpha.3 section is reconstructed from commit messages + journal + memory, not from a diff against the alpha.2 tree — accurate at the level of *what shipped*, but a summary, not an exhaustive file-by-file. I can diff `v2.0.0-alpha.2..v2.0.0-alpha.3` directly and cross-check if you want; otherwise both the journal and the release-prep record + CHANGELOG are ready for your IDE review and commit.

---

**[Dev]** Follow up on this directly:


```
# Then, once CI has created the release, swap in curated notes:
#   gh release edit v2.0.0-alpha.4 --notes-file <curated-notes.md>
```


I have done everything in the record previous to that.

**[Claude]** You authorized this directly, so I'll verify CI created the release, author the curated notes, and apply them. State checks: commit landed and the local tag is here, but `gh` shows no `v2.0.0-alpha.4` release yet — CI is **in progress** building the release from the tag (run `26656771316`, ~2.5 min in; alpha.3 took ~2m52s, so it's close). The `gh release edit` can't run until that finishes.

One thing to flag: the tag `v2.0.0-alpha.4` is on the remote (→ `e7e01dc`), but `refs/heads/main` on origin is still at `62f50ac` — your branch push didn't advance the remote `main`, only the tag went up. CI builds fine from the tag, so the release isn't blocked, but your release commit is currently only reachable via the tag on origin. Worth a `git push` to `main` after this.

While CI finished, I read alpha.3's release notes for the house style (themed sections, closed-issues list, test status, caveats), and confirmed via `gh issue list` exactly which issues are actually closed since alpha.3 — **#9, #18, #28, #29, #30, #31, #32** (and #10, the toolchain pin), with **#38 still open** (PoC only) — so the list is accurate, not guessed. *(Drafted `alpha4-notes.md`, blocked on `gh run watch` until CI succeeded, applied the notes with `gh release edit`, and confirmed they landed.)*

Done — the curated notes are live. CI run `26656771316` completed successfully, creating the `v2.0.0-alpha.4` prerelease with the correctly-named `draco-2.0.0-alpha.4.jar` asset (the version bump took effect). The notes lead with the headline reversal (JSON-normative loading **inverts** alpha.3's YAML-first behavior), then cover codec symmetry (#28/#18), the empty `comparisonOnlyExcluded` (#29), the #38 PoC, tooling, docs, closed issues, and caveats (#37 open, #38 partial): https://github.com/ejb816/nexonix/releases/tag/v2.0.0-alpha.4

**One thing still outstanding — your remote `main`.** `origin/main` → still `62f50ac` (the *parent* commit). The release commit is reachable on the remote **only via the tag** — fragile (delete the tag and the commit can be GC'd) and `origin/main` is behind the released code. A plain `git push origin main` will fast-forward it to `e7e01dc`. Want me to run that, or will you handle it in your IDE? (I haven't touched git state per your workflow — flagging only.)

---

**[Dev]** The block format git command above had a run button that I clicked. Did that run the command? I couldn't tell.

**[Claude]** Yes — it ran, and it worked. `origin/main` on the remote is now at `e7e01dc`, matching your local `HEAD`. That's exactly what `git push origin main` does, so clicking the run button executed it successfully. State is now fully consistent: `origin/main` → `e7e01dc` (was `62f50ac`), tag `v2.0.0-alpha.4` → `e7e01dc`, local `HEAD` → `e7e01dc`. The release commit is now reachable via the branch, not just the tag — the fragility I flagged is resolved. Nothing left outstanding on the release.

To answer the underlying "I couldn't tell" question: the markdown code block's run button executes the command but doesn't always surface the output prominently. `git push` prints to stderr and says nothing on a clean fast-forward beyond the ref update, so a successful run can look silent. The reliable way to confirm after the fact is what I just did — compare `git ls-remote origin refs/heads/main` against local `HEAD`; if they match, the push landed.

---

**[Dev]** Looks like our work here is done. See you in a new session.

**[Claude]** Confirmed — everything's landed and consistent. Quick recap of what shipped this session: **Chapter 39** journal written (README alignment session); **README.md** brought fully current (8-step rewrite, +319/−200); **CHANGELOG.md** reconciled (accurate alpha.3 + new alpha.4 sections); **build.sbt** → `2.0.0-alpha.4`; **git record** authored, and you committed + tagged + pushed (branch and tag both at `e7e01dc`); **release `v2.0.0-alpha.4`** live with curated notes and the `draco-2.0.0-alpha.4.jar` asset.

*(Saved memory: new `project_alpha4_release.md` recording the release, what shipped, the workflow used, the `git push` tag-vs-branch gotcha and its fix, and the open follow-ups; MEMORY.md's Release section now points to it.)*

Open threads waiting for next time, in priority order: **#38** (8 of 12 DracoType-derivation outliers remain), **#37** (TypeName structural `equals`/`hashCode` root fix), then the #33–#36 mods-script batch. `gh issue list --label priority-next` will surface the natural pickup. Nice work this session — README, CHANGELOG, journal, and a clean release all landed. See you in the next one.

## Status
README fully aligned (+319/−200, all eight steps landed, terminology audit clean). CHANGELOG reconciled — empty `[Unreleased]`, new `alpha.4` (chapters 37–39) and reconstructed `alpha.3` (chapters 20–36) sections. `build.sbt` at `2.0.0-alpha.4`; release **v2.0.0-alpha.4** live with curated notes and the `draco-2.0.0-alpha.4.jar` asset; `origin/main`, tag, and local `HEAD` all at `e7e01dc`. Follow-ups queued: **#38** (8 of 12 DracoType-derivation outliers), **#37** (TypeName structural `equals`/`hashCode`), then the #33–#36 mods-script batch. Optional: run `bin/draco-gen generate` on a real `Celsius.json` to make the Getting Started walkthrough bulletproof.
