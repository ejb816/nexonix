# Development Handoff

Updated October 21, 2026 by Codex for a new task in **either Codex or Claude Code**.
This is the current checkpoint; the journal and git records preserve historical checkpoints.
Follow DRACO.md section 7 and verify the actual tree on arrival.

## Checkpoint and permissions

- Observed branch: `main`, HEAD **b17ddb8**, initially clean and aligned with local
  `origin/main`. Dev's pasted October 21 output confirms commit and push; this is stronger
  evidence than local remote-tracking state alone. No remote query was needed.
- Latest verified code baseline: **835 tests / 58 suites**, Dev's October 20 full sbt gate,
  zero failed/canceled/ignored/pending tests, zero aborted suites. No runtime changes since.
- This handoff adds documentation only, still uncommitted when written: journal chapters
  95–99, source extracts/audit, chapter-94 continuity note and introduction, DRACO.md,
  README.md, CHANGELOG.md, this file, DEVELOPMENT_RESUME_PROMPT.md and record
  `git-record-2026-10-21-0930`. If Dev commits it, expect HEAD after b17ddb8. Inspect status;
  do not assume a clean tree or discard pending documentation.
- Do not run sbt, git commit or git push. Give Dev commands and wait for results. Direct
  compiler/projection probes have been used, but do not replace Dev's full gate.
- One design point per reply; one recommendation with accept-or-correct; build only what
  Dev accepts. After each completed increment, reconnect to the next existing priority.
- New GitHub issues require explicit approval every time. None created or modified here.
- Shared project documentation is the cross-agent memory. No private Claude/Codex memory
  is required or overwritten. The copyable prompt is in DEVELOPMENT_RESUME_PROMPT.md.

## First action in the new session

Read DRACO.md, the shared documentation skill, this checkpoint, chapter 99 and its audit.
Inspect git status/log and the relevant loader/resolver code. Restate the **unaccepted**
bootstrap proposal below with your own reading and ask Dev to accept or correct it before
building. Do not treat the request for this handoff as acceptance.

Codex's October 21 proposal: two-stage loading from an explicitly supplied set of domain
declarations. First establish domain/member identities from qualified anchors and membership
lists; then resolve definitions using that identity-only DomainOntology plus the explicit
binding vocabulary. This is intended to avoid requiring a completed ontology to load the
very definitions from which it is built. It would enable repository bodies to say Dracon.
The proposal has not settled implementation placement or an API and is not implemented.

Code to inspect:

- `src/main/scala/draco/TypeLoader.scala` and its resource trio: current unscoped loading,
  root insertion, missing-resource stubs; generated companions call loadType.
- `src/mods/scala/draco/DomainBuilder.scala`: reads domain definitions and members before
  composing the ontology. Its resource loading still uses unscoped APIs.
- `src/mods/scala/draco/NameResolution.scala`: explicit ontology/bindings, scoped JSON
  read/compact, symbolic leaves, target import projection.
- `src/main/scala/draco/{DomainOntology,TypeDictionary,Domain}.scala`: membership and
  derivations, supplied member definitions with placeholders for absent members.
- `src/mods/scala/draco/{Drake,DracoGenerator}.scala`: scoped overloads and bootstrap paths.
- `src/mods/scala/draco/dreams/Initialization.scala` and editor/TransformBinding.scala:
  endogenous initialization and the held-ontology generation path.
- `src/test/scala/draco/NameResolutionTest.scala` and dreams/editor/GenerationTest.scala.

Respect main/mods layering: TypeLoader is generated main code; NameResolution is handwritten
mods code. Main must not depend on mods. Check bootstrap initialization cycles and trio gates
before proposing implementation. No global ontology or classpath discovery was accepted.

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
