# Development Handoff

Updated October 21, 2026 by Codex for a new task in **either Codex or Claude Code**.
This is the current checkpoint; the journal and git records preserve historical checkpoints.
Follow DRACO.md section 7 and verify the actual tree on arrival.

## Checkpoint and permissions

- Updated October 21, 2026 by Claude Code. Observed branch `main`, HEAD **7d5de4f** (the unit type,
  committed and pushed by Dev at the verified 840 / 59 gate). The tree now carries the UNCOMMITTED
  host-vocabulary increment: `src/main/resources/draco/scalatarget/HostBindings.{drake,json}` and
  `src/main/scala/draco/scalatarget/HostBindings.scala` (new trio), Draco and ScalaTarget trios
  (member lists), `src/test/scala/draco/DomainLoadingTest.scala`, `src/test/scala/draco/format/dracon/DraconTest.scala`
  (its pinned ScalaTarget member list now names HostBindings; Dev's first gate failed on it, 844 / 845),
  DRACO.md (table rows moved),
  README.md, CHANGELOG.md, this file and record `git-record-2026-10-21-1915` (gate line a
  placeholder until Dev's run). Do not discard it. The previous increment, as committed, was: `src/mods/scala/draco/{Drake,DracoGenerator,AssemblyValidator}.scala`,
  `src/main/scala/draco/CLI.scala` (regenerated), twelve re-authored drake sources with their JSON
  and seven JSON-only example definitions (36 files in status), `src/main/resources/draco/drake.dlt`,
  `src/test/scala/draco/{DrakeParseTest,DomainLoadingTest}.scala`, DRACO.md, README.md,
  CHANGELOG.md, this file and record `git-record-2026-10-21-1830` (gate line a placeholder until
  Dev's run). Do not discard it.
- Latest verified code baseline: **845 tests / 59 suites**, Dev's October 21 second full gate on the
  host-vocabulary increment, zero failed/canceled/ignored/pending tests, zero aborted suites, parse
  scope 110 + 10, one loss across 120, aspect sections 110, GenDrake 108 of 108, every other headline
  unchanged, pending mods actors 0 (the first run failed DraconTest's pinned member list, corrected).
- Before it, **840 tests / 59 suites**, Dev's October 21 second full gate on the unit-type increment, zero failed/canceled/ignored/pending tests, zero aborted suites, every
  report-only headline unchanged, pending mods actors 0. The first run had failed the two assembly
  suites (AssemblyValidator read the consumer arrow's result as the string `Unit`); fixed before the
  second. Before it, 839 / 59 at b95419c and 835 / 58 at b17ddb8.
- Dev ACCEPTED on October 21, after the loader landed, the unit type as the empty Objective `()`,
  rendering `Unit` in Scala, the corpus re-authored and Base's `Unit` kept. Built as accepted.
- Do not run sbt, git commit or git push. Give Dev commands and wait for results. Direct
  compiler/projection probes have been used, but do not replace Dev's full gate.
- One design point per reply; one recommendation with accept-or-correct; build only what
  Dev accepts. After each completed increment, reconnect to the next existing priority.
- New GitHub issues require explicit approval every time. None created or modified here.
- Shared project documentation is the cross-agent memory. No private Claude/Codex memory
  is required or overwritten. The copyable prompt is in DEVELOPMENT_RESUME_PROMPT.md.

## Next action

After Dev's commit of the host-vocabulary increment: with the loader, the unit type and the vocabulary landed, the next #51 design point is the
one Dev asked for on October 20: shortening the bootstrap files themselves — which files, through
which operation (the loader's scope now exists; the trio gates read JSON unscoped and the companions
load unscoped), and what the gates compare. Journal chapter 100 is written from this session's record
at its closing handoff. One recommendation.

## Completed in this session

| Commit | Increment | Dev's full gate |
|---|---|---|
| 9185f1a | Generate/Generated and executable transform bindings | 782 / 55 |
| db7e7a8 | Dracon wrapper/format, Encoder and Decoder, Circe adapters | 804 / 56 |
| d169b0e | Core definition trees use Dracon; explicit payload codecs | 809 / 56 |
| f97c913 | Payload declarations belong to the owning CodecAspect | 815 / 56 |
| a13ad38 | Structured method TypeParameter records, ten migrated declarations | 825 / 57 |
| b17ddb8 | Explicit ontology-scoped resolution and binding vocabulary | 835 / 58 |

All six commits and pushes were performed by Dev, with results preserved in the transcript
extract. Chapter 95 starts at a3d2d35, the documentation commit after chapter 94's code baseline.

Durable contracts:

- Generate names a held domain and transform, answers through the Response consumer with
  Generated carrying one Emission per definition. Factory-supplied executable bindings must
  match held definitions. Fresh session; Problems on refusal; no writes or activation.
- Dracon replaces draco.format.json.JSON as combined wrapper/format. Encoder(T): T -> Dracon;
  Decoder(T): Dracon -> Presence(T). Circe implementation belongs at the wrapper/target boundary.
  The proposed separate six-form value family was not accepted.
- Core value/valueType/derivation/argument trees use Dracon. CodecAspect.payload names fields
  whose Dracon values (or sequence elements) serialize directly, preserving raw JSON payloads.
  Ordinary Dracon wrapper encoding remains distinct. The generator's field allowlist is gone.
- Dynamic.typeParameters contains TypeParameter records with name and Dracon upper/lower
  bounds; absent bounds are Null. Method names contain no generic target syntax. TypeName's
  existing header/actual argument representation has not been split.
- NameResolution receives DomainOntology and primitive/host bindings explicitly. Lookup:
  local self/formals first, unique ontology identity second, explicit bindings last. Unknown
  or ambiguous names fail; dotted names also require an exact known identity. Existing type
  operators recurse. Definition/domain anchors stay qualified. Quoted and host-opaque spans
  are preserved; structured symbolic leaves respect local binders.
- Scoped Drake parse/emit, JSON read/compact and Scala generate are implemented. Scala adds
  selective imports with collision checks. Dreams Generate uses its held ontology and the
  TransformBinding.nameBindings pairs; duplicates fail before execution. GenDrake receives
  the scope through its fresh session. Standalone legacy sessions retain unscoped emission.
- Repository bootstrap files remain qualified; there has been **no bulk shortening**. No
  Haskell generator was implemented. Imports discussed in the transcript are illustrative.

## Verification and lessons

| Headline | Verified value |
|---|---|
| DomainAspect | 109 complete, 1 parameterized included, 0 skipped |
| GenDrake | 107/107, 0 missing, 0 extra |
| Example-domain generation | 28 match, 20 differ, 0 error, 0 missing / 48 |
| Surface losses | 1 across 119 types, ActorAspect type form |
| Parse scope | 109 draco + 10 mods, 0 held back |
| DracoAspect | 109 complete, 2 frozen authored-ahead baselines, 0 skipped |
| PON / canonical | 80 numbers, 550 expressions, 42 discrepancies / 7 differ |
| Scenario | 23 files, all clean |
| Forest | 1 of 2 ash alarms crossed to birch |
| Subtype visibility | CO-DECLARATION DROPS THE FACT |
| Pending mods actors | 0, file-only DrakeGenTest.log |

Final direct compiled probe also passed 835, with 59 suites including Runner discovery
versus sbt's 58. Earlier probe harness failures are not erased: missing test resources caused
three scenario failures and two PON cancellations; restored resources yielded the final full
pass. An integration fixture omitted Format, Dracon's parent; supplying both fixed it without
weakening validation. The final passing probe includes the local-value/import collision guard.
Earlier wrapper/core probes hit sandbox socket failures; separate permitted loopback runs and
Dev's complete gates subsequently passed. See chapter Status notes and per-commit records.

DrakeCLI retains known ActorAspect/BodyElement authored-ahead differences (surface 2, JSON 1);
Editor check was 0/0. Probe directories were removed before Dev's gate. This documentation pass
needs no sbt rerun. Check whitespace, source hashes, chapter links/index and shared symlinks.
Dev's shell does not have rg; use grep in handed-over commands. Never edit generated main Scala
alone: regenerate the trios. Every val in an App object must be lazy.

## Deferred work and priority context

- #51 is the active continuation: context-independent type expressions/vocabulary. Its
  priority-next label referred to the August derivation edge, already completed; the label
  did not approve all old proposals. Reconcile issue prose against current code and Dev's words.
- Before the ontology detour, Codex proposed placing type-level formal declarations in the
  owning DracoAspect while TypeName carries actual arguments. **Not accepted.** Return to it
  only after the short-name work, one point at a time.
- Activation: Dev explicitly deferred the proposal to start an explicit Assembly of held actor
  definitions using factory-supplied executable constructors matched to them. **Not accepted.**
- .dcon extension migration deferred until wrapper work; no acceptance of migration yet.
- #43 broader codec-aspect emission and #12 Dreams remain context, not new authorizations.
  Inferred derivation is later work. Membership reciprocity was surfaced, not filed.
- Persistence belongs to the consuming project. ZeroMQ protocol choices remain provisional;
  transport work is independent of this bootstrap design.

## Documentary coverage and gaps

Chapters **95–99** cover this session through Dev's October 21 handoff request, after chapter
94's Claude session. Audit: `draco-dev-journal/sources/codex-2026-10-15-to-21-audit.md`.
The extract contains 71 originals: 36 user messages, 35 final replies, 35 complete exchanges
plus the open request. All 12 responsive results are summarized in chapters and retained in
full in the extract. Three observed compactions retain originals on both sides; this is bounded
recoverability, not a claim of complete historical capture. This documentation pass's final
reply is outside the boundary and can be recovered by the next documentation pass.

Chapter 94's 39 originals and quotations were rechecked; its historical Status is preserved.
Claude's previously out-of-bound closing reply is now preserved in a separate one-record
bridge extract. The missing f4336a4 gate paste in chapter 94 remains a disclosed older gap.
Both documentation sync markers advance to 99 after reconciliation. Historical git-record
statements about chapter-94 coverage remain historical; do not rewrite prior commit records.
