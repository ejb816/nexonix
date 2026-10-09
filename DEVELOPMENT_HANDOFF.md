# Development Handoff

This is the shared resumption checkpoint for Claude Code and Codex, not an additional
architecture specification or a transcript. Follow DRACO.md section 7 and verify this
snapshot against the current tree before acting.

## Checkpoint

- Updated: 2026-10-09, by Claude Code, at the end of the first authoring increment.
- Observed branch/HEAD: main at f54da3f, matching local origin/main; no fresh remote query.
- The tree carries TWO uncommitted bodies of work: Codex's 2026-10-07 documentation
  checkpoint (chapters 90–92, their transcript audit, index, shared docs, record
  `git-record-2026-10-07-1105`), which Dev had not committed when this session began,
  and this session's authoring increment (code, definitions, tests, docs, record
  `git-record-2026-10-09-1218`). The shared documents (CHANGELOG, DRACO.md, README,
  this file) contain both sets of edits, so one commit is proposed; see Resumption.
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
- Scope boundary accepted: this increment takes a Latent to an Actual one domain at a
  time, reusing core's rules; composing Actuals into a `DomainOntology` with explicit
  intra- and inter-relationships is the increment after. The Editor builds its own
  knowledge from core's rule patterns (no reply protocol on the Draco actor).

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

Claude did not run sbt. A compiled-class probe recompiled the WHOLE tree — main, mods
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

The last Dev-verified full run remains 701 tests / 50 suites on 2026-10-07 (f54da3f).
That result does not verify this increment.

## Documentation Coverage

- Journal coverage reaches chapter 92 (Codex, 2026-10-07, uncommitted). Both
  `draco-docs-synced-through` markers stay at 92: DRACO.md and README are code-current
  for this increment, but **chapter 93 — this Claude session — is not written.** Its
  source is this session's transcript (Dev's three messages: the handoff opener, the
  Latent/Actual/ontology naming, the "Latent and Actual operate on domains" correction
  and acceptance). Write it per the journal conventions before advancing the markers.
- Codex's chapters 90–92 and their audit are untouched by this session.

## Resumption

1. Dev runs the full gate (expected 713 tests / 51 suites, baselines unchanged):

   ```bash
   cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
   ```

2. On green, one path-scoped commit carrying both bodies of work, message from the
   2026-10-09 record (the 2026-10-07 record is committed alongside for the audit trail):
   `git add -A -- <paths>` then `git commit -F draco-git-record/git-record-2026-10-09-1218 -- <paths>`.
   The paths are every file `git status --short` lists; nothing else is pending.

3. Next authorized work, in Dev's accepted order: compose accepted Actuals and their
   intra-/inter-relationships into the `DomainOntology`; the endogenous initialization
   client over existing actors/messages; then generation and activation as explicit
   operations. Candidate rules surfaced, not filed: membership reciprocity (a supplied
   member the domain does not name is silently dropped) and duplicate identity.

### First action for the next agent

Read DRACO.md, this file and the 2026-10-09 record; inspect `git status` and `git log`.
If f54da3f is still HEAD, both bodies of work are uncommitted — preserve them. Confirm
Dev's sbt result before claiming any baseline. Write chapter 93 when Dev asks for the
journal; then design the ontology-composition increment with Dev before building.
