# Development Handoff

This is the shared resumption checkpoint for Claude Code and Codex, not an additional
architecture specification or a transcript. Follow DRACO.md section 7 and verify this
snapshot against the current tree before acting.

## Checkpoint

- Updated: 2026-10-12, by Claude Code, closing the session for a handoff. Destination:
  either agent; Dev named the OpenAI platform (Codex) as the likely next. Everything the
  next agent needs is in tracked project files — DRACO.md, this file, the records, chapter
  93 and its audit; no Claude-private memory is required or relied upon. (the second
  increment of the day; the first authoring slice is 462cde6, its verification note b8d9a34).
- Observed branch/HEAD: main at efa9085, pushed; Dev's paste shows `main...origin/main`
  clean after the push. That commit is the composition increment.
- Pending after it, documentation only: the verified-baseline note, journal chapter 93 with
  its bounded extract and audit, the intro index, both sync markers to 93, this file and
  record `git-record-2026-10-12-1247`.
- Destination: either agent. Dev alternates sessions; the next step is Dev's full
  `sbt test`, then the commit/push.
- Permission: Dev authorized both agents to maintain the journal and shared documentation
  on September 23. sbt, commit, push and new-issue restrictions remain in force.

## Decisions this session (Dev, 2026-10-09)

- `DomainDictionary` is the **domain ontology**, `DomainOntology`: an operational ontology
  built from domains and their intra- and inter-relationships. "Dictionary" was an
  expedient on the way to a formal ontology and now names only a domain's `TypeDictionary`.
- The authoring types are **Latent** and **Actual**, and they operate on **domains**, not
  ontologies: a Latent is one domain in candidate form, an Actual the accepted (or
  rejected) domain. Ontologies are built from domains and their relationships.
- Scope boundary accepted: the first slice takes a Latent to an Actual one domain at a
  time, reusing core's rules; the Editor builds its own knowledge from core's rule
  patterns (no reply protocol on the Draco actor).
- **The ontology is draco's innate, endogenous ontology** (Dev, 2026-10-12): the framework's
  own domains plus the dreams definitions as they can be accepted — self-referential, to be
  maintained with care. The Editor is accepted as its holder on condition that acceptance
  never compromises the framework: acceptance alters nothing running (generation and
  activation stay explicit), and `Uniqueness` — over name and package, parameters free —
  refuses a second owner of any name. Update-by-identity: a Latent whose domain name and
  package match an accepted domain replaces it. Delivering the grown ontology to a caller
  waits for the initialization client.
- **Derivation is the ontology's first explicit relationship**, with provenance on the
  edge (`declared`), declared edges first; inferred derivation (classification by shared
  elements) is a later rule. Scope of the second increment: the relationship alone —
  `Derivation`, projected `derivations`, `dependencies`, `including`, the rule reading
  edges — Editor untouched. Composing accepted Actuals into the ontology is still ahead.

## Completed Development

- **Ontology rename.** `DomainOntology` replaces `DomainDictionary` across the trio, the
  Draco member list, `DomainBuilder` (`dictionary` → `ontology`), the ZeroMQ bridge and
  five tests; `defines(typeName)` added. Map shape unchanged.
- **Core composition.** `TypeDictionary(domainDefinition, members = [])` and
  `Domain(domainDefinition, members = [])` populate from supplied definitions;
  `DomainBuilder.define` loads from resources and delegates.
- **Core validation.** `DerivationResolvable` joins the `DomainOntology` fact and flags
  any named parent it does not define — no package filter, no loader call.
  `SelfDeclaration` compares the whole TypeName. The validation suites and the Draco
  actor test insert the ontology as a fact.
- **Service.** `ServiceConfiguration` contains an `Assembly` (the accepted shape).
- **Editor.** `Latent`, `Actual` and the `Editor` actor in `draco.dreams.editor`, as
  generated trios under `src/mods/{resources,scala}/draco/dreams/editor`. Actual's
  populated `domain` is a dyn, not a carried field (codec eligibility is not transitive).
- **Tests.** `EditorTest` (13: six trio checks, one structural, six behavioural: acceptance
  with a populated domain, Completeness/DerivationResolvable/SelfDeclaration problems,
  ordering and non-mutation, JSON round trip). ServiceTest +2, SelfDeclarationRulesTest +1,
  DerivationResolvableRulesTest rewritten (+1, including a parent no resource holds).
  DreamsTest keeps Dreams and User only. DomainBuilderTest checks `defines`.
- **Docs.** DRACO.md (orientation paragraph, §1 probe sentence, three §6 gotchas, retired
  list), README (ontology vocabulary, work in progress, status), drake.dlt history line,
  ZEROMQ contract, Service/Dreams/mods READMEs, CHANGELOG (Added + Changed), git record.

## Verification

Composition increment: Dev's full run on 2026-10-12 passed **733 tests / 53 suites, 0 failed,
0 aborted**, headlines at 105 draco + 10 mods, one loss across 115 types, 105 aspect
sections, GenDrake 103/103, everything else unchanged; committed/pushed efa9085. That is
the current verified baseline; DRACO.md §1 records it. The probe before it — main, mods
and tests recompiled into `target/scala-2.13/probe-*`, hidden resources excluded — had
passed 733 / 0 failed / 53 suites as sbt counts (54 with the Runner's wrapper): 723 + 5 corpus tests for `Uniqueness` + 2 `UniquenessRulesTest`
+ 2 net in EditorTest + 1 in DomainOntologyTest. The added corpus type moves four
headlines by one — parse scope 105 draco + 10 mods, one loss across 115 types, 105
aspect sections, GenDrake 103/103; the DRACO.md table carries the two tabled rows.
Everything else unchanged. Probe dirs removed.

Derivation increment: Dev's full run on 2026-10-10 passed **723 tests / 52 suites, 0 failed,
0 aborted**, the four moved headlines matching the probe, and committed/pushed d181e64.
That is the current verified baseline; DRACO.md §1 records it. The probe before it —
main, mods and tests recompiled into `target/scala-2.13/probe-*`, hidden resources
excluded — passed **723 tests / 0 failed / 52 suites** as sbt counts (53 with the
Runner's wrapper): 713 + 5 corpus tests for `Derivation` + 5 in `DomainOntologyTest`.
The one added corpus type moves four headlines by one — parse scope 104 draco + 10 mods,
one surface loss across 114 types, 104 sections in both aspect probes, GenDrake 102/102;
the DRACO.md table carries the two tabled rows. Every other figure unchanged. Two
earlier probe iterations caught defects in the new tests only. Probe dirs removed.

First authoring slice (verified): Claude did not run sbt. A compiled-class probe recompiled the WHOLE tree — main, mods
and tests — into `target/scala-2.13/probe-{classes,test-classes}` (the `Domain` factory's
new parameter makes every compiled companion binary-stale, so a partial overlay was not
an option), overlaid current resources (hidden dotfiles excluded, as sbt excludes them),
and ran every suite with the scalatest Runner:

- **713 tests, 0 failed, 0 aborted**; 52 suites as the Runner counts (51 as sbt will:
  50 + EditorTest). Expected sbt arithmetic from Dev's 701: −5 +13 +2 +1 +1 = 713.
- Report-only headlines all match DRACO.md: gen map 28/20/0/0 of 48; surface losses 1
  across 113; parse scope 103 draco + 10 mods; PON 80/550/42; canonical 7; scenario 23
  clean; DomainAspect 103/1/0; DracoAspect 103/2/0; GenDrake 101/101; CO-DECLARATION
  DROPS THE FACT; mods actors pending Drake authoring 0 (file-only).
- Earlier probe iterations caught: the ambiguous single-fact `insert` (fixed with
  `Seq(fact): _*`); unsupplied members never reaching working memory (the Editor now
  inserts the candidate's populated `elementTypes`); the obsolete `draco`-package filter.
- The probe directories were removed afterwards. Probe logs: scratchpad only, transient.

Dev then ran the full gate on 2026-10-09: **713 tests / 51 suites, 0 failed, 0 aborted**,
every report-only headline matching, and committed/pushed 462cde6. That is the current
verified baseline; DRACO.md §1 records it.

## Documentation Coverage

- Journal coverage reaches chapter 93 (this Claude session, 2026-10-07 to 10-12): 13
  assertive Dev prompts verbatim, 16 final replies near-verbatim, 5 delegated-action notes;
  the bounded extract `sources/claude-2026-10-07-to-12.jsonl` (33 records with source
  line, UTC, SHA-256) and its audit record the frozen prefix. The reply answering Dev's
  wrap-up request is outside the boundary; chapter 93's Status says so. Both
  `draco-docs-synced-through` markers advance to 93 after reviewing DRACO.md and README
  against the chapter — they were written during the session and carry its decisions.
- Codex's chapters 90–92 and their audits are untouched by this session.

## Resumption

1. The gate and the commit are done (efa9085). Commit this documentation pass.

2. Next authorized work, in Dev's accepted order: the endogenous initialization client
   over existing actors/messages (it needs a way to receive the Editor's grown ontology —
   the open design point); then generation and activation as explicit operations.
   Later: inferred derivation (classification by shared elements). Candidate rule
   surfaced, not filed: membership reciprocity (a supplied member the domain does not
   name is silently dropped).

### First action for the next agent

Read DRACO.md, this file and the 2026-10-09 record; inspect `git status` and `git log`.
Expect HEAD at or after efa9085 with a clean tree; the baseline is 733 / 53. Read chapter 93
and DRACO.md §3's three 2026-10-09/12 paragraphs for the decisions. Open the initialization
client with its first design question — how a caller receives the Editor's grown ontology —
and settle it with Dev before building; the candidate-batch idea from chapter 91 is still
not an accepted protocol. Write chapter 93 when Dev asks for the
journal; then design the ontology-composition increment with Dev before building.
