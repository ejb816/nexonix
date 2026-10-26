# Development Handoff

Prepared October 26, 2026 by Codex for a new session in either Codex or Claude Code.
This checkpoint supersedes the accumulated historical checkpoints in earlier versions of
this file. Git records, commits and journal chapters retain their historical evidence.
No destination session has been launched or messaged.

## Start here

Read DRACO.md (AGENTS.md and CLAUDE.md are symlinks), this file, and
`.draco/skills/update-draco-docs/SKILL.md`. Inspect status and log before editing.
Use DEVELOPMENT_RESUME_PROMPT.md as the opening prompt. Private agent memory is not required.

- Branch: main. Last code commit: **a8b5447**, Consumer repair. Dev's pasted output
  confirms commit and push; local main and origin/main agreed on October 26 inspection.
- Latest Dev-verified gate: **933 tests / 71 suites**, October 25, on the Consumer tree.
  Zero failed/canceled/ignored/pending tests and zero aborted suites. First Dev gate green.
- All accepted implementation work is complete, verified, committed and pushed by Dev.
- Only documentation is pending for this handoff: CHANGELOG.md, DRACO.md, README.md,
  DEVELOPMENT_HANDOFF.md, DEVELOPMENT_RESUME_PROMPT.md, journal introduction, chapters 105–112,
  their Codex extract/audit and Claude closing extract, and draco-git-record/git-record-2026-10-26-1253. No runtime or test-source edits.
  A documentation-only commit may follow a8b5447; inspect it and preserve uncommitted docs.
- **The next recommendation below is preserved, NOT accepted.** The handoff request
  authorizes documentation, not implementation of that recommendation.

## Next design point — acceptance required

Codex recommends a narrow repair of named-construction qualification in
`src/mods/scala/draco/DracoGenerator.scala`, method `constructionInitializer` (near line 260).
It currently writes `valueType.split('.').last`, so a qualified construction of
`domains.terrestrial.Location` can emit bare `Location` without a usable import.
This was observed while authoring Consumer; its accepted implementation uses the valid
positional call and is already tested and committed.

Proposed scope:

- Preserve the resolved constructor qualification while retaining named-parameter mapping.
- Add a generated compilation/runtime regression from a different package, without an
  import masking the defect; check scoped and unscoped generation.
- Measure main/staged/example corpus impact before any necessary regeneration; obey trio gates.
- Keep Consumer's accepted positional call unchanged. No general constructor redesign,
  unrelated example repair or new issue is authorized.

Receiving agent: verify this reading, restate this one recommendation and ask Dev to accept
or correct it before building. Do not treat its presence here as acceptance.

## Operating boundaries

- Do not run sbt, git commit or git push. Dev runs those commands; supply path-scoped blocks
  and wait for his results. Scope the commit itself as well as git add; use a message file.
- Use grep in Dev's shell; rg is unavailable. Read-only git and direct compiler/test probes
  are allowed. Never reset or overwrite unrelated changes.
- One design point per reply, one recommendation with accept-or-correct; build only accepted
  scope. After a verified commit, bring the next related point rather than only acknowledging it.
- Before handing over a gate, run **every discovered suite**, with current test resources
  on the loader roots as well as main/mods resources. Read all report-only headlines.
- New GitHub issues require explicit approval. None created during this session.
- Definitions move as Drake/JSON/generated-Scala trios. Every val in an App object must be lazy.
  The canonical corpus remains free of comparison exclusions.
- Activation and .dcon migration remain deferred. No new transforms, geodesy redesign,
  Haskell target, or wholesale example sweep is authorized. Existing issue prose is prior
  model reasoning, not Dev's specification. Persistence belongs to the consuming project;
  transport choices remain provisional.

## Current verification evidence

Dev's October 25 full sbt output and commit/push output were pasted into this conversation.
The preceding direct run passed **933 tests / 72 suites including Runner discovery**.
The one extra direct suite is discovery; sbt reports 71. All sources used by these tests
were compiled directly and current main/mods/test resources were overlaid on loader roots.
The handoff changes are documentation-only; no new implementation gate was run for them.

| Headline | Verified at a8b5447 |
|---|---|
| DomainAspect | 110 complete, 1 parameterized included, 0 skipped |
| DracoAspect | 110 complete, 0 frozen baselines, 0 skipped |
| GenDrake | 108/108, 0 missing, 0 extra |
| Example generation | 36 match, 12 differ, 0 error, 0 missing / 48 |
| Surface losses | 0 fields across 133 types |
| Parse scope | 110 draco + 23 mods, 0 held back |
| PON | 80 numbers, 550 expressions, 42 discrepancies |
| Canonical PON | 80 numbers, 7 differ |
| Scenario | 23 files, 0 rejected, 0 drifted, 23 clean |
| Forest | 1 of 2 ash alarms crossed to birch |
| Subtype visibility | CO-DECLARATION DROPS THE FACT |
| Pending mods actors | 0; file-only target/test-output/DrakeGenTest.log |

Do not infer the last row from console output. The example map remains report-only;
individual repaired types now have exact projection and behavior gates.

## Completed session ledger

These are factual commit/record summaries, not reconstructed dialogue. All implementation
commits and pushes were performed by Dev after his full gate. Records live in draco-git-record.

| Commit | Accepted increment | Dev gate | Record suffix |
|---|---|---|---|
| b04d81d | Presence-typed rule/actor slots and field encoding; retire authored-ahead exhibits | 857 / 61 | 2026-10-22-0800 |
| 375c513 | Compile and execute all four empty-role projections | 861 / 61 | 2026-10-22-0739 |
| 1d69df1 | Reciprocal membership for 16 existing definitions across five example domains | 873 / 62 | 2026-10-23-1048 |
| ae0264e | HCursor target binding, caller AerialSink binding, all-48 loading gate | 875 / 63 | 2026-10-23-1106 |
| 3608fab | Aerial OriginateReport conversion carried in definition | 880 / 64 | 2026-10-23-1216 |
| bd6b7b3 | Marine OriginateReport conversion and truncation | 886 / 65 | 2026-10-23-1613 |
| 97bf690 | Terrestrial OriginateReport conversion and signed truncation | 893 / 66 | 2026-10-24-1008 |
| e819663 | Ethereal OriginateReport; fourth conversion-helper gap closed | 900 / 67 | 2026-10-25-1455 |
| 804c8a4 | Cartesian tuple overload and inferred coordinate codecs | 908 / 68 | 2026-10-25-1505 |
| 81154e3 | Observable helpers, nested codecs and caller Geodesy binding | 918 / 69 | 2026-10-25-1517 |
| 3648806 | Provider actor role and forwarding | 925 / 70 | 2026-10-25-1543 |
| a8b5447 | Consumer actor role and Position-to-Location branch | 933 / 71 | 2026-10-25-1557 |

Starting HEAD was 4dcef69, the documentation handoff after 7b1bfea (849 / 60).
EXAMPLE_DOMAIN_AUDIT.md is a historical read-only audit at 375c513, not a current snapshot.

## Durable implementation decisions and findings

- Aspects.ruleAspect and actorAspect carry Presence. Their containing record omits Absent
  and emits Present directly as the existing aspect JSON object, including `{}`. No standalone
  Presence codec was introduced. BodyElement and ActorAspect are canonical plain types;
  former role exhibits moved into RolePresenceTest. No frozen fixtures/exclusions remain.
- RolePresenceTest compiles/loads all four combinations after carrier round-trip, fires
  declared rules and exercises actors through BehaviorTestKit. Empty action invocation count
  is not measured.
- All 48 example identities belong to their explicitly declared six domains. ExampleScope
  composes those domains with canonical dependencies and explicit bindings. HCursor belongs
  to HostBindings; AerialSink and Geodesy are caller-owned helpers, not domain members or
  shared target vocabulary. Loading is tested independently of declaration order.
- Four OriginateReport conversions now originate from their definitions, preserving payload
  keys, defaults, integer arithmetic and callbacks. They are repaired individually, not abstracted.
- Cartesian keeps lazy scalar arguments, strict tuple argument and Null. Observable keeps
  strict helper parameters and lazy frames; Geodesy mathematics is untouched. Their inferred
  codecs were explicitly accepted and tested for round-trip and missing-value defaults.
- Observable's first direct run passed 916/918: two loading failures exposed Geodesy as a
  newly visible dependency. Adding its explicit caller binding and omission/status checks
  led to a fully green rerun and first green Dev gate. This failure is preserved in its record.
- Provider forwards unchanged World messages once; Consumer converts only Position, ignores
  other messages, uses feet * 0.3048 and math.round(h).toInt, and captures its callback lazily.
  Both preserve inert signals and existing World assembly behavior. Consumer uses strict local
  bindings in its case branch and a positional Location call. No generator change was made.
- Scoped canonical generation and TypeName formal/actual separation remain as documented in
  DRACO.md and chapters 100–104. Rebuilds must preserve uses and typeParameters. Companions
  load unscoped; recorded resolution is applied during generation. Do not sweep examples by
  assuming scoped and unscoped spellings are identical.

The 12 remaining example differences in Dev's latest report are:
aerial/Aerial, aerial/Input, aerial/Position; ethereal/ConsumeReport, ethereal/Ethereal;
marine/ConsumeReport, marine/Marine; terrestrial/ConsumeReport, terrestrial/Location,
terrestrial/Output, terrestrial/Terrestrial; world/World. These are not uniformly harmless
spelling differences. In particular Output's authored projection still lacks its handwritten
message binder/dispatch. Inventory is context, not repair authorization.

## Reproducing the direct verification procedure

Temporary scripts/logs are optional evidence, not prerequisites. The session used
/tmp/membership-build.py, /tmp/draco-role-build/tests.log and target/scala-2.13/role-{classes,test-classes}.
Do not run that old script's `bootstrap` mode: it contains obsolete, source-mutating migration
steps. Reconstruct only the compile/test procedure below if those files are missing or stale.

1. Read the jar entries from target/streams/test/fullClasspath/_global/streams/export
   (fallback: target/streams/runtime/dependencyClasspath/_global/streams/export). Verify they
   exist. If dependencies are absent, request Dev's build rather than running sbt yourself.
2. Use dedicated clean class directories under target/scala-2.13/, not /tmp. Existing tests
   derive repository resource locations from class locations; /tmp output caused wrong roots.
   Exclude old test/classes from the compiler classpath to avoid silently reusing stale sources.
3. Overlay src/main/resources, src/mods/resources **and src/test/resources** into the dedicated
   main class root. Exclude its root .json/.drake anchors as sbt does; MinimalTypeTest relies on
   that behavior. Keep resources unique across loader roots to avoid DefinitionPath ambiguity.
4. Compile all Scala under src/main/scala, src/mods/scala/draco and src/mods/scala/domains via
   `java -Xmx3g -cp <jars> scala.tools.nsc.Main -classpath <classes>:<jars> -d <classes> @<sources>`.
   Then compile all src/test/scala into the dedicated test directory using the same classpath.
5. Run `java -Xmx3g -cp <classes>:<jars> org.scalatest.tools.Runner -o -R <test-classes>`.
   Put test classes on Runner's runpath, not the JVM classpath; this matters for isolated runtime
   compilation/class loading. Capture stdout. Existing ZeroMQ tests need local sockets and may
   require sandbox permission. Run every discovered suite, not just a remembered subset.
6. Check all headlines above, the file-only mods actor line, and git status/diff afterwards.
   Some tests generate files into source trees; PrimesRulesTest must remain scoped. Never
   revert such output blindly, especially when preserving another agent's edits.

Direct verification supplements, never replaces, Dev's full sbt gate. Hand over the established
console grep plus file-only grep only after the direct run passes and headlines are understood.

## Journal coverage and handoff integrity

DRACO.md and README.md markers now reach **chapter 112**. Read chapters 105–112 and
`sources/codex-2026-10-22-to-26-audit.md` for this receiving session’s decisions and verification.
The extract preserves 85 original messages: 43 user (19 assertive, 24 result receipts),
42 final replies, 42 completed exchanges and the open journal-recovery acceptance. Original
records survive both observed compactions; no missing original was detected in this bounded
sequence. The current maintenance pass’s final reply is outside the frozen prefix.

The previous Claude handoff close is recovered in `sources/claude-2026-10-22-handoff-close.jsonl`
and rendered in chapter 105: two replies and Dev’s documentation commit/push receipt. The
prior 55-record extract was verified against source; chapters 100–104’s historical statuses
remain unchanged. Chapter 104 correctly leaves Presence open at its own historical boundary.

The initial October 26 handoff omitted journal maintenance. Dev challenged that omission,
and accepted recovering the original exchanges here. Chapter 112 preserves that correction.
No compaction summary was used as quotation evidence. Git records supplement explicitly
labeled Status notes, including Observable’s failed direct run and subsequent binding fix.

Checks: source-line/timestamp/hash verification, exchange and compaction-boundary checks,
chapter text/index/link checks, documentation reconciliation, shared symlinks and whitespace.
Only documentation and bounded dialogue extracts change; no runtime/tests, sbt, commits,
pushes, new issues or private-memory modifications by Codex. No new implementation gate claimed.
