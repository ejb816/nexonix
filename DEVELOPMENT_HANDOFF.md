# Development Handoff

Updated October 22, 2026 by Claude Code for a new task in **either Codex or Claude Code**.
This is the current checkpoint; the journal and git records preserve historical checkpoints.
Follow DRACO.md section 7 and verify the actual tree on arrival.

## Checkpoint and permissions

- Observed branch: `main`, HEAD **7b1bfea**, clean and aligned with `origin/main`; Dev's pasted
  October 22 output confirms commit and push. This handoff adds documentation only, still
  uncommitted when written: journal chapters 100–104, their source extract and audit, the
  introduction's index, DRACO.md, README.md, CHANGELOG.md, this file, DEVELOPMENT_RESUME_PROMPT.md
  and record `git-record-2026-10-22-0730`. If Dev commits it, expect HEAD after 7b1bfea. Inspect
  status; do not assume a clean tree or discard pending documentation.
- Latest verified code baseline: **849 tests / 60 suites**, Dev's October 22 full sbt gate on
  7b1bfea, zero failed/canceled/ignored/pending tests, zero aborted suites, every report-only
  headline as the DRACO.md table states, pending mods actors 0. No runtime changes since.
- Do not run sbt, git commit or git push. Give Dev commands and wait for results. Direct
  compiler/projection probes have been used (overlay the current resources on a copy of the
  compiled classes under `target/scala-2.13/`, recompile the changed engine into it, put
  recompiled test suites on the scalatest Runner's runpath rather than the classpath); they do
  not replace Dev's full gate, and they missed suites more than once this session — run every
  discovered suite, with the test resources on the loader's roots, before handing over a gate.
- One design point per reply; one recommendation with accept-or-correct; build only what Dev
  accepts. After each verified commit, bring the next design point tied to the existing work.
- New GitHub issues require explicit approval every time. None created or modified here.
- Shared project documentation is the cross-agent memory. No private Claude/Codex memory is
  required or overwritten. The copyable prompt is in DEVELOPMENT_RESUME_PROMPT.md.

## First action in the new session

Read DRACO.md, the shared documentation skill, this checkpoint, chapter 104 and its audit.
Inspect git status/log. Restate the **unaccepted** presence proposal below with your own reading
and ask Dev to accept or correct it before building. Dev closed the previous session with it open;
closing is not acceptance.

The open proposal (Claude, October 22): drake.dlt marks `rule` and `actor` presence-significant
and facet-creating — writing the head, even empty, says the type is a rule or an actor, and the
JSON must carry the key with an empty value — and `Drake.parse` honors it (a `rule` head sets the
role), but the generated TypeDefinition codec elides an empty RuleAspect or ActorAspect, so a
present-empty role is lost through the carrier. Recommended: the two role slots in `Aspects`
become Presence-typed (`ruleAspect`, `actorAspect` as `Presence` of their aspect), Present
emitted even when empty, Absent omitted; about 78 readers across the engine, the dreams rules and
the tests move from an emptiness test to a presence fold. With it, the two authored-ahead surfaces
(`draco/BodyElement.drake`, `draco/ActorAspect.drake`, frozen since 2026-09-28 with present-empty
`rule`/`actor` heads, a three-line host lambda, an explicit factory name and a different element
order) re-canonicalize as the plain types they are, their JSON catches up (closing the last
surface loss, `ActorAspect => Boolean`), the frozen baselines under
`src/test/resources/draco/draketarget/draco-aspect-baseline/` and the exclusions in DrakeGenTest,
DrakeParseTest and DracoAspectTest go, and the presence behavior moves into a round-trip test.
The alternative is to re-canonicalize the two files without touching the carrier, closing every
gate residue but leaving the spec's presence principle unimplemented for roles.

Code to inspect: `src/main/resources/draco/Aspects.drake` and `TypeDefinition.drake` (the slots
and the generated codec's elision), `src/mods/scala/draco/Drake.scala` (`isRule`/`isActor` flags
in `parseDefinition`), `src/mods/scala/draco/DracoGenerator.scala` (`isRule`, `isActor`,
`hasActorBehavior`, `roleAspectCount`), `src/mods/resources/draco/dreams/editor/*.drake` (readers
of `ruleAspect`), the two authored-ahead files and the baseline README.

## Completed in the previous session (Claude Code, October 21–22)

| Commit | Increment | Dev's full gate |
|---|---|---|
| b95419c | Two-stage loading: `DomainBuilder.identities` / `load` | 839 / 59 |
| 7d5de4f | The unit type is the empty Objective `()` | 840 / 59 (second run) |
| 44a5005 | `draco.scalatarget.HostBindings`; Local listed in Draco | 845 / 59 (second run) |
| 0b8fc03 | Seventeen Dracon referrers compact; canonical scoped generation | 845 / 59 (third run) |
| 773b6d9 | The resolution record, `uses` | 849 / 60 |
| d279709 | The canonical corpus swept | 849 / 60 (second run) |
| 7b1bfea | Formals in `DracoAspect.typeParameters`; TypeName actuals only | 849 / 60 |

All seven commits and pushes were performed by Dev, with results preserved in the transcript
extract. Chapters 100–104 cover them; chapter 99 ends with the Codex handoff this session received.

Durable contracts:

- `DomainBuilder.identities(declarations, path)` composes the identity-only ontology of declared
  domains from anchors alone; `DomainBuilder.load(declarations, bindings, path)` resolves every body
  through NameResolution over it. Nothing is discovered; the companions load unscoped.
- The unit type is the empty Objective `()`, spelled `Unit` by the Scala target; no host leaf names it.
- `HostBindings` declares the Scala target's host vocabulary as `[(Text, TypeName)]` pairs, supplied
  explicitly to `load` and to Generate; JDK-implicit names are bound bare; the hand-written engine
  objects (DracoGenerator, Drake, NameResolution, Generated) are engine bindings.
- Scoped generation CANONICALIZES: compact, then qualified only where the engine cannot read a bare
  name (`DracoGenerator.readable`), then shortened back by selective import; the output does not
  depend on the author's spelling. The compact form keeps host leaves as authored, shortens only
  names unique across ontology and vocabulary (draco.rete wraps stay bare), keeps nominal anchors in
  full and spells anchor parameters own-package bare, else full.
- A definition carries `uses`, its resolution record, written by compact, validated on every scoped
  read, dropped by resolution; `NameResolution.recorded(td)` is the lenient scope a file carries in
  itself; the engine reads it only during a generation. Unscoped, scoped and pinned generation
  agree on every definition of the canonical corpus (ResolutionRecordTest pins it).
- The canonical corpus is swept: every main definition but BodyElement and ActorAspect, and the
  staged dreams and service definitions, carry compact bodies; the example domains are not swept.
- A definition's header formals are `TypeParameter` records in `DracoAspect.typeParameters`; its own
  TypeName is bare; a TypeName's parameters are a reference's actuals. Rebuild sites that carry the
  slot: `TypeLoader.rooted`, the generator's `targetTypes` and family merge.
- The test suites that generate corpus Scala supply `CanonicalScope.scope` (a test helper) explicitly;
  PrimesRulesTest writes its generation into src/main and is scoped for that reason.

## Verification and lessons

| Headline | Verified value (7b1bfea) |
|---|---|
| DomainAspect | 110 complete, 1 parameterized included, 0 skipped |
| DracoAspect | 110 complete, 2 frozen authored-ahead baselines, 0 skipped |
| GenDrake | 108/108, 0 missing, 0 extra |
| Example-domain generation | 28 match, 20 differ, 0 error, 0 missing / 48 |
| Surface losses | 1 across 120 types, ActorAspect type form |
| Parse scope | 110 draco + 10 mods, 0 held back |
| PON / canonical | 80 numbers, 550 expressions, 42 discrepancies / 7 differ |
| Scenario | 23 files, all clean |
| Forest | 1 of 2 ash alarms crossed to birch |
| Subtype visibility | CO-DECLARATION DROPS THE FACT |
| Pending mods actors | 0, file-only DrakeGenTest.log |

Lessons the session paid for, each in its record: the §6 rebuild rule bit three times (`TypeLoader.rooted`
and the generator's rebuilds silently dropped `uses`, then `typeParameters`; compile the regenerated
loader BEFORE regenerating the corpus); the engine keys decisions on bare spellings, so scoped generation
must canonicalize rather than resolve; the engine's loader must apply a record only during generation or
DomainBuilder and the companions disagree; a `git checkout` of a directory holding uncommitted edits
reverts them — restore specific files; a probe that omits a suite or the test resources from the loader's
roots misses what Dev's gate finds.

## Deferred work and priority context

- The presence proposal above is the next design point, unaccepted.
- The 48 example domains under `src/mods/resources/domains` are outside the canonical ontology,
  report-only (28 match / 20 differ) and unswept; whether they join under their own declared ontology
  is an open question, not put to Dev.
- Activation (an explicitly supplied Assembly of held actor definitions with factory-supplied
  executable constructors) and the `.dcon` extension remain deferred; no Haskell target exists.
- #43 broader codec-aspect emission, #12 Dreams, inferred derivation and membership reciprocity remain
  context, not authorizations. Persistence belongs to the consuming project; ZeroMQ choices are provisional.

## Documentary coverage and gaps

Chapters **100–104** cover the Claude Code session from the Codex handoff through Dev's close request,
with audit `draco-dev-journal/sources/claude-2026-10-21-to-22-audit.md`: 55 original records, 28 user
messages (11 assertive, 17 responsive), 27 final replies, 27 complete exchanges plus the open close
request. No compaction was observed in the frozen prefix; the reply completing this documentation pass is
outside it and can be recovered by the next pass. Chapters 95–99 (Codex) and 94 (Claude) keep their
historical Status sections. Both documentation sync markers advance to 104 after reconciliation.
