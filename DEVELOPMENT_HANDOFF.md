# Development Handoff

Refreshed November 10, 2026 by Claude Code in the session that RECEIVED the handoff below and did its first task: the
journal is current through chapter 127, and through chapter 129 since that session journaled itself the same day (see the
current checkpoint). The checkpoint that follows this paragraph's section was prepared earlier the same day
by the previous Claude Code session, at Dev's direction, for a NEW SESSION in either agent: Dev called the handoff when
that session's context reached 75%, with the journal several sessions behind. It supersedes the record-and-codec, actor-section, rule-section and host-text checkpoints of the same day, the #49, #63, #47 and #50 checkpoints of November 9 and the November 8, November 6
and November 4 checkpoints below, which stay as written as the history of this handoff; the dated sections below them are the history of the increments,
kept as written. Git records, commits and journal chapters retain their historical evidence. No
destination session has been launched or messaged, and no private agent memory is required.

## Current checkpoint — the element renderer's two string-era respellings are removed (Claude Code, November 12, 2026; direct run 1239 / 89; Dev's gate 1239 green; commit pending)

Same session. HEAD 042bef1, pushed (Dev's paste: cafb359..042bef1). Dev accepted ("Accepted, build it") the next related
point under GitHub #11 (open; stays open). Record `git-record-2026-11-12-2123`, which also folds 042bef1 into the documents.

- **Built.** `Drake.scala`'s emitter writes a value as the expression renderer spells it and a name as authored:
  `defaultValue` (host empty collections onto `[]` / `{}`) and `elementName` (`f[T]` onto `f(T)`) are removed, and so is
  the parser's `parseElementName`, the inverse of the latter — an addition found while building. The normalizers in
  DrakeParseTest and DrakeCLI follow. HostTextTest +1 asserts no definition needs either respelling.
- **Evidence.** `DrakeCLI check` drift 0 over main and mods. Direct run: 1239 tests / 89 suites including discovery, green
  on its first run, every headline unchanged from 042bef1.
  Dev's first full gate on 2026-11-12: 1239 tests, zero failed, canceled, ignored or pending, every headline unchanged, green on its first run.
- **In the tree, uncommitted.** Drake.scala, DrakeCLI.scala, DrakeParseTest, HostTextTest, drake.dlt, CHANGELOG.md,
  DRACO.md, this file, the resume prompt, the record.
- **Next on this thread, none accepted.** The element renderer as a definition; removing the hand-written expression
  renderer; a failure form for the guards.

## Earlier the same session — drake's element keyword table is a definition (Claude Code, November 12, 2026; direct run 1238 / 89; Dev's gate 1238 green; committed as 042bef1, pushed)

Same session. HEAD cafb359, pushed (Dev's paste: f80584d..cafb359). Dev accepted ("Accepted, build it") the next related
point under GitHub #11 (open; stays open). Record `git-record-2026-11-12-2103`, which also folds cafb359 into the documents.

- **Built.** `draketarget.Keyword` (trio, listed by DrakeTarget): the globals `kinds` (the nine member kinds with their
  keywords), `table` and `strict` (`now`). `Drake.scala`'s emitter looks an element's keyword up by the name of the kind
  definition the element carries; the parser's member keywords are the table's values. KeywordTest (4) is new.
- **Evidence.** `DrakeCLI check` drift 0 over main and mods. Direct run: 1238 tests / 89 suites including discovery, green
  on its first run. Headlines: parse scope 125 + 73, losses 0 across 198 types, 125 aspect sections, GenDrake 123 of 123,
  91 records, Definition 198 surfaces from 530 sections, host text 21 of 2634, type leaves 1643, TypeForm 2115 forms
  (472 trees, 1643 leaves), Expression 632 values (453 trees, 179 leaves). No new grep line.
  Dev's first full gate on 2026-11-12: 1238 tests, zero failed, canceled, ignored or pending, every headline matching the direct run, green on its first run.
- **Committed.** As 042bef1 by Dev after the gate, 14 files, pushed (cafb359..042bef1).
- **Next on this thread, none accepted.** The element renderer; removing the hand-written expression renderer; a failure
  form for the guards.

## Earlier the same session — the expression renderer is a definition (Claude Code, November 12, 2026; direct run 1229 / 88; Dev's gate 1229 green; committed as cafb359, pushed)

Same session. HEAD f80584d, pushed (Dev's paste: 270ca7d..f80584d). Dev accepted ("Accepted, build it") the next related
point under GitHub #11 (open; stays open). Record `git-record-2026-11-12-1906`, which also folds f80584d into the documents.

- **Built.** `gendrake.ExpressionOf` (trio, listed by GenDrake) spells a value tree in drake's form with the minimal
  parentheses by `Fixity.table`; `Drake.expression` refuses a value outside the tree language and delegates to it; the
  hand-written renderer stays as `handExpression`, the reference. ExpressionOfTest (5) is new.
- **Evidence.** `DrakeCLI check` drift 0 over main and mods. Direct run: 1229 tests / 88 suites including discovery, green
  on its second run (the first failed the new corpus test on its own guessed lower bound). Headlines: parse scope
  124 + 73, losses 0 across 197 types, 124 aspect sections, GenDrake 122 of 122, 90 records, Definition 197 surfaces from
  527 sections, host text 21 of 2614, type leaves 1638, TypeForm 2107 forms (469 trees, 1638 leaves), and the new
  `Expression over the corpus: 629 values reproduced in 197 definitions (452 trees, 177 leaves), 0 differing`.
  Dev's first full gate on 2026-11-12: 1229 tests, zero failed, canceled, ignored or pending, every headline matching the direct run, the new `Expression over` line as expected, green on its first run. The gate grep in DRACO.md §1 carries `Expression over`.
- **Committed.** As cafb359 by Dev after the gate, 14 files, pushed (f80584d..cafb359).
- **Next on this thread, none accepted.** The element renderer; removing the hand-written expression renderer; a failure
  form for the guards.

## Earlier the same session — drake's fixity table is a definition (Claude Code, November 12, 2026; direct run 1219 / 87; Dev's gate 1219 green; committed as f80584d, pushed)

Same session. HEAD 270ca7d, pushed (Dev's paste: decc752..270ca7d). Dev accepted ("Accepted, build it") the next related
point under GitHub #11 (open; stays open). Record `git-record-2026-11-12-1410`, which also folds 270ca7d into the documents.

- **Built.** `draketarget.Fixity` (trio, listed by DrakeTarget): a record of precedence and associativity with the globals
  `operators`, `table` and `flattened`. `Drake.scala`'s parser and emitter read it in place of the engine's private table.
  FixityTest (4) is new. One sentence in drake.dlt names the definition.
- **Evidence.** `DrakeCLI check` drift 0 over main and mods. Direct run: 1219 tests / 87 suites including discovery, green
  (a first run failed three ScenarioGenTest tests on the harness: test resources off the loader roots). Headlines: parse
  scope 123 + 73, losses 0 across 196 types, 123 aspect sections, GenDrake 121 of 121, 89 records, Definition 196 surfaces
  from 524 sections, host text 21 of 2433, type leaves 1612, TypeForm 2076 forms (464 trees, 1612 leaves). Dev's first full gate on
  2026-11-12: 1219 tests, zero failed, canceled, ignored or pending, every headline matching the direct run, green on its first run. The baseline is **1219 tests / 86 suites**.
- **Committed.** By Dev as f80584d on 2026-11-12, pushed (270ca7d..f80584d).
- **Next on this thread, none accepted.** The expression renderer as a definition (it raises on an unknown operator, and
  drake has no failure form); the element renderer; a failure form for the guards.

## Earlier the same session — the type-form renderer is a definition (Claude Code, November 11, 2026; direct run 1210 / 86; Dev's gate 1210 green on November 12; committed as 270ca7d, pushed)

Same session. HEAD decc752, pushed (Dev's paste: e8dbd11..decc752). Dev accepted ("Accepted, build it") the next related
point under GitHub #11 (open; stays open). Record `git-record-2026-11-11-1753`, which also folds decc752 into the documents.

- **Built.** `gendrake.TypeFormOf` (trio, listed by GenDrake) maps a type form onto drake's spelling; `Drake.emit`'s
  `drakeType` delegates to it; `TypeForm.neutral` stays as the reference and as `TypeForm.text`'s tree spelling.
  TypeFormOfTest (5) is new. A node that is none of the nine yields the empty text (the hand-written renderer raises).
- **Evidence.** `DrakeCLI check` drift 0 over main and mods. Direct run: 1210 tests / 86 suites including discovery, green on
  its first run. Headlines: parse scope 122 + 73, losses 0 across 195 types, 122 aspect sections, GenDrake 120 of 120, 88
  records, Definition 195 surfaces from 521 sections, host text 21 of 2369, type leaves 1602, and the new
  `TypeForm over the corpus: 2062 type forms reproduced in 195 definitions (460 trees, 1602 leaves), 0 differing`.
  Dev's first full gate on 2026-11-12: 1210 tests, zero failed, canceled, ignored or pending, every headline matching, green on its first run. The baseline is **1210 tests / 85 suites**. The gate grep in DRACO.md §1 carries `TypeForm over`.
- **Committed.** By Dev as 270ca7d on 2026-11-12, pushed (decc752..270ca7d).
- **Next on this thread, none accepted.** The element and expression renderers; a failure form for the guards.

## Earlier the same session — the parser's dead type converter removed (Claude Code, November 11, 2026; direct run 1200 / 85; Dev's gate 1200 green; committed as decc752, pushed)

Same session, continued November 11. HEAD e8dbd11, pushed (Dev's paste: 427442e..e8dbd11). Dev accepted ("Accepted, build it")
the next related point under GitHub #11 (open; stays open). Record `git-record-2026-11-11-1205`, which also carries the fold of
e8dbd11 into the shared documents.

- **Built.** `parseTypeExpression` and `scalaArrow` removed from Drake.scala (unreachable since type-form trees, 2026-09-17);
  five stale comments corrected in Drake.scala and TypeForm.scala (the recommendation said four). No definition, JSON,
  generated Scala or test change.
- **Evidence.** Direct run on freshly compiled classes: 1200 tests / 85 suites including discovery, all green on the first
  run, every headline unchanged. Dev's first full gate on 2026-11-11: 1200 tests, zero failed, canceled, ignored or pending, every headline matching, green on its first run; the baseline stays 1200 / 84. Committed by Dev as decc752 and pushed (e8dbd11..decc752).
- **Next on this thread, none accepted.** The type-form renderer as a definition; the element and expression renderers; a
  failure form for the guards. Observed, left: Natural.json and TupleFact.json under src/test carry host-spelled types.
- **Journal.** Current through chapter 129; uncovered is what follows Dev's journal request of November 10 — the e8dbd11
  commit exchange and this increment.

## Earlier the same session — one type-form renderer in the drake emitter (Claude Code, November 10, 2026; direct run 1200 / 85; Dev's gate 1200 / 84; committed as e8dbd11, pushed)

Same session. After 427442e Dev accepted ("Accepted, build it") the next related point under GitHub #11 (open; stays open).
Record `git-record-2026-11-10-1714`.

- **Built.** Sentient Path authored `Primal([Waypoint])` (JSON re-parsed, Scala byte-identical); the emitter's string-era
  `typeExpression` removed, `drakeType` is `TypeForm.neutral`; `DrakeCLI check` drift 0 over main and mods. `TypeLeafTest` (2),
  the 84th suite, asserts every type leaf in main and mods is a name; the gate grep gains `type leaves`.
- **One existing assertion changed.** DracoAspectTest's factory-elision case pinned the removed respelling of the string
  "Example[T]"; it now uses the tree. The first direct run failed on it (1199 / 1).
- **Evidence.** Second direct run: 1200 tests / 85 suites including discovery, all green, every headline unchanged, `type
  leaves: 1585 in 194 definitions, 0 carrying host type syntax`. Dev's first full gate on November 10: 1200 tests, zero failed,
  canceled, ignored or pending, every headline matching. The baseline is **1200 tests / 84 suites**. Committed by Dev as e8dbd11 with the journal chapters, pushed the same day; `main` and `origin/main` both at e8dbd11, tree clean before this fold.
- **Journal.** Current through chapter 129, written before the commit at Dev's request after he noticed the session had been
  compacted: chapters 128–129 cover this session from its original record (19 records, 9 completed exchanges, the request the
  open turn; two compaction boundaries, originals on both sides), extract and audit `sources/claude-2026-11-10-session-2`.
  Markers at chapter 129. Uncovered: only what follows that request — this journal update's own reply and the commit.
- **Observed, left.** `parseTypeExpression` in the parser is dead code; Natural.json and TupleFact.json under src/test carry
  host-spelled types and have no surface.
- **Next, none accepted:** the type-form renderer as a definition; the element and expression renderers; a failure form for
  the guards; whether generation should consult compiled classes.

## Earlier the same session — the assembly of the drake surface is definition-backed (Claude Code, November 10, 2026; direct run 1198 / 84; Dev's gate 1198 / 83)

Same session as the journal checkpoint below. After d815338 Dev said "Yes, recommend" and then "Accepted, build it" to the one
recommendation: OWNING ISSUE GitHub #11 (open; stays open). Record `git-record-2026-11-10-1527`.

- **Built.** `draketarget.DefinitionText` (the six section texts in the order type, `uses`, domain, rule, actor, codec; the
  non-empty ones joined by a line break, one at the end) and `gendrake.DefinitionOf` (a definition and the four rendering
  dependencies DracoAspectOf receives, composing the six section transforms), listed by DrakeTarget and GenDrake. `Drake.emit`
  is its three guards and `definitionText(td)`. Four JSON parsed from their surfaces, four Scala files generated in the §6
  order and stable on regeneration; `DrakeCLI check` drift 0 over main and mods. `DefinitionTextTest` (5) is the 83rd suite.
- **Evidence.** Direct run of every discovered suite: 1198 tests / 84 suites including discovery, zero failed, canceled,
  ignored, pending or aborted, green on its first run. Headlines: example gate 48 / 0, surface losses 0 across 194 types, parse
  scope 121 + 73 with 0 held back, host text 21 leaves of 2237 in the 11 named seams, scenario 23 clean, GenDrake 119 of 119,
  domain and draco aspect probes 121, rule 24, actor 15, 87 records, 4 codec sections, `Definition over the corpus: 194
  surfaces reproduced whole from 518 sections, 0 skipped`, PON 42 and 7 as suspended, the rete finding categorical. The tree
  afterwards held the intended changes only. Dev's first full gate on November 10: 1198 tests, zero failed, canceled, ignored or pending, every headline matching, green on
  its first run. The baseline is **1198 tests / 83 suites**. Committed and pushed by Dev as 427442e on November 10 (20 files, d815338..427442e).
- **Rejected.** The guards inside the transform (drake has no failure form); the renderer track in this increment.
- **Next, none accepted, each a design question:** the element, expression and type-form renderers (about 380 lines of
  Drake.scala); a failure form for the guards; whether generation should consult compiled classes (DRACO.md §6).
- **Journal.** Chapters 128–129 now cover this session through the type-renderer increment's verified gate (see the checkpoint above).

## Earlier the same session — the journal is current through chapter 127 (Claude Code, November 10, 2026)

Observed on receipt: HEAD 043f578 on main, the handoff's documentation commit on c70d6b8, tree clean, local main level with the
local origin/main ref. Dev's gate on c70d6b8 stays the baseline: **1183 tests / 82 suites**. This session changed
documentation only: the journal update, committed and pushed by Dev as d815338 on November 10 (record
`git-record-2026-11-10-1343`, 30 files, 043f578..d815338).

- **Scope, confirmed by Dev before writing ("Accept").** Chapters 113–127, strictly chronological, no session skipped:
  113–119 from Claude record `f12792d4` (October 26 – November 3), 120 and 122 from `9e3f32b7` (November 3–6), 121 from the
  Codex rollouts of November 4–5 including the short troubleshooting conversation, 123–127 from `1cf64c57` (November 7–10).
- **Evidence.** Four bounded extracts and audits under `draco-dev-journal/sources/`: 231 records, 116 Dev messages, 113 final
  replies, 113 completed exchanges, one interrupted prompt. All 212 Claude records were re-read from their physical source
  lines and matched on timestamp, uuid and text hash; every assertive prompt and final reply was checked present in its
  chapter. 23 distinct compaction boundaries; no summary supplies a quotation. Dev's 60 pasted results are delegated-action
  notes generated from the pastes; the full pastes are in the extracts.
- **A correction the originals forced.** The Codex session's FIRST reply (2026-11-04T20:46:28Z) asked "What should I correct
  in the proposed issue-tracker reconciliation audit". Dev answered with the concern about tracker integration, replied
  "Accept" to the restated recommendation and "I approve your recommendation" to the batch. The November 6 checkpoint below,
  DRACO.md's November 6 paragraph and the resume prompt said Codex did not ask; that was Claude Code's inference without the
  rollouts. DRACO.md carries a dated correction; the November 6 checkpoint below stays as written, as history. Dev's distrust
  of the result is Dev's own statement and is unchanged, as is the November 8 verification.
- **Two facts for future records.** Dev's pasted gate output never carries a `Suites:` line (the grep filters for `Tests:`),
  so a suite count attributed to Dev's gate is the direct run's less discovery. Several of Dev's short answers ("Accepted,
  build it", "Correct it first", "Yes, both") are button replies to a next-step question that is a tool call, not reply text.
- **Markers.** `draco-docs-synced-through` is chapter 127 in DRACO.md and README.md: each chapter's Status and closing
  recommendation was read against both files. The chapters were generated from the records, not read line by line in full.
- **Not covered:** this session's own exchanges (the scope confirmation and this update), which belong to a later chapter.
- **NEXT, when Dev says development resumes:** ONE recommendation with its owning issue named — the assembly of the drake
  surface as a definition (preserved, NOT accepted, GitHub #11), or another of the 22 open issues. Nothing on the tracker
  before Dev decides.

## Previous checkpoint — handoff to a new session; the journal was the first task (Claude Code, November 10, 2026; done, see above)

Dev, November 10: "It is time for a handoff to a new session. The session context just reached 75% and I am not confident that
the draco-dev-journal can update and create chapters in the current session. It doesn't seem the draco-dev-journal has been
updated or added to for few sessions."

Observed: HEAD c70d6b8 on main, equal to origin/main (read back with `git ls-remote` on November 10); the tree held only
this handoff's documentation, which Dev commits and pushes with the blocks handed over (record `git-record-2026-11-10-1237`).
Dev's gate on c70d6b8 is the baseline: **1183 tests / 82 suites**, every headline matching. The gate grep is the one in
DRACO.md §1; it gained five lines this session (`host text`, `RuleAspect over`, `ActorAspect over`, `Uses over`,
`CodecAspect over`).

- **The session, in one line each.** 7d4ae0a the Codex reconciliation verified; d026cd3 every rule trait rooted (#50 closed);
  929c850 `TypeDefinition.isStub` (#47 closed); 995fade the FactDisjointness rule (#63 constraint 4, issue open); 2804904 the
  draco.rete wrap types named (#49 vocabulary half, issue open); e2ca0be the host-text bound and HostTextTest; 1c96d63,
  8b127aa, c70d6b8 the rule, actor, record and codec sections of the drake surface as definition-backed transforms (#11,
  issue open) — all six sections. Each was accepted by Dev, gated by Dev's full sbt run and committed by Dev; the dated
  sections below and the git records carry the measurements. 22 issues are open.
- **FIRST TASK for the receiving session: the journal.** Coverage ends at chapter 112 (closing commit 7495539, October 26);
  39 commits and four sessions are unjournaled. The sources and the procedure are in *Journal coverage and handoff integrity*
  at the end of this file. It was not attempted here: this session's own record is one of the sources, and its context was
  three-quarters full.
- **Preserved, NOT accepted.** After c70d6b8 the next recommendation was brought and met the handoff instruction instead of
  an answer: the ASSEMBLY of the drake surface as a definition — `draketarget.DefinitionText` owning the order of the six
  sections (type, `uses`, domain, rule, actor, codec; non-empty ones joined, one trailing newline) and `gendrake.DefinitionOf`
  composing the six transforms with DracoAspectOf's four rendering dependencies, `Drake.emit` reduced to its three guards and
  one call, two corpus types. It is a proposal. Behind it, each a design question and none accepted: the element, expression
  and type-form renderers (about 380 lines of `Drake.scala`, the real remaining mass of #11's drake side); a failure form
  for the codec guards; whether generation should consult compiled classes (`shadowedByHost`, DRACO.md §6).
- **Standing practice changed this session.** Every commit block handed to Dev ends with a push block; nothing was pushed
  between November 6 and November 10 because no block contained one. Generate a new definition, COMPILE it, then generate
  its referrers (§6).
- **Observed, not fixed:** `scripts/inspect-type.scala` has not compiled since 2026-10-17; GETTING_STARTED_TARGET_SCALA.md's
  authoring section still presents JSON as what is authored; EgoActor is a hand-written actor with no definition.
- **Deferred or suspended, unchanged:** activation; the .dcon migration; PON (nothing under domains/pon is touched).
- **Everything else stands as the sections below record.**

## Previous checkpoint — all six sections of the drake surface are definition-backed (Claude Code, November 10, 2026; done, see above)

Observed: HEAD c70d6b8 on main, this increment's commit (record `git-record-2026-11-10-1221`), committed by Dev on
November 10 on top of 8b127aa after his gate (1183 tests, zero failures, every headline matching, green on its first run)
and PUSHED the same day (`8b127aa..c70d6b8`; origin/main equal to local main, read back with `git ls-remote`). The tree was
clean after it but for this fold. Dev's gate on c70d6b8 is the current baseline: **1183 tests / 82 suites**.

- **Done this session.** The next related point under GitHub #11 was accepted ("Accepted, build it"): the `uses` record and
  the codec section, the last two the emitter rendered itself, are `draketarget.UsesText` / `gendrake.UsesOf` and
  `draketarget.CodecAspectText` / `gendrake.CodecAspectOf`, the emitter delegating through `usesText` and `codecAspectText`
  and keeping its three guards. Every surface is byte-identical. UsesAndCodecTest (5) reproduces all 85 records and 4 codec
  sections; four corpus types added; the gate grep gains `Uses over` and `CodecAspect over`. The generation order of the new
  §6 gotcha was followed and held. #11 stays open. Direct run of every discovered suite: 1183 tests / 83 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline as expected — example gate 48 / 0, surface losses 0 across 192 types, parse scope 119 + 73 with 0 held back, host text 21 leaves of 2186 in the 11 named seams and 0 outside, scenario 23 clean, GenDrake 117 of 117, the domain and draco aspect probes 119 complete, rule 24 and actor 15 sections over 192 definitions, `Uses over the corpus: 192 definitions, 85 records reproduced, 0 skipped`, `CodecAspect over the corpus: 192 definitions, 4 codec sections reproduced, 0 skipped`, the PON pair 42 and 7 as suspended, the rete finding categorical; the twenty-five new tests are UsesAndCodecTest's five and the five per-type gates of each of the four new definitions; the tree afterwards held the intended changes only.
- **Next action:** ONE recommendation: either the next design question on this thread (the section order as a definition, the
  element renderer, the guards' failure form) or another issue of the 22, its owning issue named.
- **Open for Dev, surfaced not filed:** whether generation should consult compiled classes at all (`shadowedByHost`, §6).
- **Observed, not fixed:** `scripts/inspect-type.scala` has not compiled since 2026-10-17 (recorded with the #47 increment).
- **Not done, by design:** no tracker action; no journal chapter.
- **Everything else stands as the sections below record.**

## Previous checkpoint — the actor section of the drake surface is definition-backed (Claude Code, November 10, 2026; done, see above)

Observed: HEAD 8b127aa on main, this increment's commit (record `git-record-2026-11-10-1120`), committed by Dev on
November 10 on top of 1c96d63 after his gate (1158 tests, zero failures, every headline matching, green on its first run)
and PUSHED the same day (`1c96d63..8b127aa`; origin/main equal to local main, read back with `git ls-remote`). The tree was
clean after it but for this fold. Dev's gate on 8b127aa is the current baseline: **1158 tests / 81 suites**.

- **Done this session.** The next related point under GitHub #11 was accepted ("Accepted, build it"): the actor section of
  `Drake.emit` is rendered by `draketarget.ActorAspectText` (layout) and `gendrake.ActorAspectOf` (mapping), the emitter
  delegating through `actorAspectText`; its local `reference` helper and the callerless `typeRef` are removed. Every
  surface is byte-identical (`DrakeCLI check` drift 0 over main and mods). ActorAspectTest (5) reproduces all 15 actor
  sections; two corpus types added; the gate grep gains `ActorAspect over`. Four of the surface's six sections are now
  definition-backed. #11 stays open. The first direct run failed two tests on one cause — a referrer generated before its
  new referent was compiled (DRACO.md §6, new gotcha) — corrected by regenerating. Second direct run of every discovered suite: 1158 tests / 82 suites including discovery, zero failed, canceled, ignored, pending or aborted, green; every headline as expected — example gate 48 / 0, surface losses 0 across 188 types, parse scope 115 + 73 with 0 held back, host text 21 leaves of 2149 in the 11 named seams and 0 outside, scenario 23 clean, GenDrake 113 of 113, the domain and draco aspect probes 115 complete, `RuleAspect over the corpus: 188 definitions, 24 rule sections`, `ActorAspect over the corpus: 188 definitions, 15 actor sections reproduced, 0 skipped`, the PON pair 42 and 7 as suspended, the rete finding categorical; the fifteen new tests are ActorAspectTest's five and the five per-type gates of each new definition; the tree afterwards held the intended changes only.
- **Next action:** the next related point, the `uses` record and the codec payload together, as ONE recommendation (brought
  to Dev on November 10 after the push; see the resume prompt for whether it was accepted).
- **Observed, not fixed:** `scripts/inspect-type.scala` has not compiled since 2026-10-17 (recorded with the #47 increment).
- **Not done, by design:** no tracker action; no journal chapter.
- **Everything else stands as the sections below record.**

## Previous checkpoint — the rule section of the drake surface is definition-backed (Claude Code, November 10, 2026; done, see above)

Observed: HEAD 1c96d63 on main, this increment's commit (record `git-record-2026-11-10-1107`), committed by Dev on
November 10 on top of 2f75d5a after his gate (1143 tests, zero failures, every headline matching, green on its first run)
and PUSHED the same day (`2f75d5a..1c96d63`; origin/main equal to local main, read back with `git ls-remote`). The tree was
clean after it but for this fold. Dev's gate on 1c96d63 is the current baseline: **1143 tests / 80 suites**.

- **Done this session.** The recommendation under GitHub #11 was accepted ("Accepted, build it"): the rule section of
  `Drake.emit`, hand-written until now, is rendered by `draketarget.RuleAspectText` (layout) and `gendrake.RuleAspectOf`
  (mapping), the shape of the type and domain sections' pairs; the emitter delegates through `ruleAspectText`. Every surface
  is byte-identical (`DrakeCLI check` drift 0 over main and mods with the new emitter). RuleAspectTest (5) reproduces all 24
  rule sections of the corpus; two corpus types added; the gate grep gains `RuleAspect over`. #11 stays open. Direct run of every discovered suite: 1143 tests / 81 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline as expected — example gate 48 / 0, surface losses 0 across 186 types, parse scope 113 + 73 with 0 held back, host text 21 leaves of 2057 in the 11 named seams and 0 outside, scenario 23 clean, GenDrake 111 of 111, the domain and draco aspect probes 113 complete, `RuleAspect over the corpus: 186 definitions, 24 rule sections reproduced, 0 skipped`, the PON pair 42 and 7 as suspended, the rete finding categorical; the fifteen new tests are RuleAspectTest's five and the five per-type gates of each new definition; the tree afterwards held the intended changes only.
- **Next action:** the next related point, the actor section by the same pattern, as ONE recommendation (brought to Dev on
  November 10 after the push; see the resume prompt for whether it was accepted).
- **Observed, not fixed:** `scripts/inspect-type.scala` has not compiled since 2026-10-17 (recorded with the #47 increment).
- **Not done, by design:** no tracker action; no journal chapter.
- **Everything else stands as the sections below record.**

## Previous checkpoint — the host text in definition values bounded (Claude Code, November 10, 2026; done, see above)

Observed: HEAD e2ca0be on main, this increment's commit (record `git-record-2026-11-10-1050`), committed by Dev on
November 10 on top of 3e87527 after his gate (1128 tests, zero failures, every headline matching, green on its first run);
the tree was clean after it but for this fold. e2ca0be is the last implementation commit and Dev's gate on it is the
current baseline: **1128 tests / 79 suites**. PUSH STATE, read on November 10 with `git ls-remote`: origin/main was at
40d02df (pushed November 6) and local main ten commits ahead — 7d4ae0a, d026cd3, d8de334, 929c850, a6a0f96, 995fade,
a3b52a7, 2804904, 3e87527, e2ca0be; no handed-over block has contained a push, which is Dev's (DRACO.md §1). The closing
comments on #50 and #47 cite d026cd3 and 929c850, which resolved on GitHub only once pushed. PUSHED by Dev on November 10
after the fold commit 2f75d5a: `40d02df..2f75d5a main -> main`, eleven commits, origin/main equal to local main (read back
with `git ls-remote`). Every commit block handed over from here ends with a push block.

- **Done this session.** The remaining endogenous candidates were read against the tree and found without clients in the
  definitions (#58, #43) or with one (#59), so the host-text residue was measured instead: 24 value leaves of 1984 carried
  the Scala target's syntax, three of them outside the target seams. Dev accepted ("Accepted, build it"): ActorAspect's
  lambda is drake's `\aa -> …`, CLI's and SourceContent's `try … finally` are `bracket`; `HostTextTest` asserts the
  remaining 21 leaves sit only in eleven named seam definitions at recorded counts, failing by file and leaf otherwise.
  No engine change; one test added; the gate grep gains `host text`. No issue owns it exactly (nearest #59, open); none
  was requested. Direct run of every discovered suite: 1128 tests / 80 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline unchanged — example gate 48 / 0, surface losses 0 across 184 types, parse scope 111 + 73 with 0 held back, scenario 23 clean, GenDrake 109 of 109, both aspect probes 111 complete, the PON pair 42 and 7 as suspended, the rete finding categorical — and the new line `host text in values: 21 leaves of 1997 in 11 definitions, 0 outside the 11 named seams`; the tree afterwards held the intended changes only.
- **Next action:** ONE recommendation from
  the 22 open issues or the seam list, its owning issue named, for accept-or-correct.
- **Observed, not fixed:** `scripts/inspect-type.scala` has not compiled since 2026-10-17 (recorded with the #47 increment).
- **Not done, by design:** no tracker action; no journal chapter; no push.
- **Everything else stands as the sections below record.**

## Previous checkpoint — the fourth increment from the reconciled backlog: the rete wrap types named (Claude Code, November 9, 2026; done, see above)

Observed: HEAD 2804904 on main, this increment's commit (record `git-record-2026-11-09-1415`), committed by Dev on
November 9 on top of a3b52a7 after his gate (1127 tests, zero failures, every headline matching, green on its first run);
the tree was clean after it but for this fold. Whether the day's commits (d026cd3, d8de334, 929c850, a6a0f96, 995fade,
a3b52a7, 2804904) are pushed is to be checked on resumption. 2804904 is the last implementation commit and Dev's gate on
it is the current baseline: **1127 tests / 78 suites**.

- **Done this session.** The recommendation on GitHub #49's type-vocabulary half was accepted ("Accepted, build it"): the
  twelve actor `start` blocks spelling `org.evrete.api.StatefulSession` and the three spelling `org.evrete.api.Knowledge`
  now name the `draco.rete` wrap types bare with `uses` records, as Rule has; the generator's host-import table gains the
  StatefulSession row (one engine line) and HostBindings keys it qualified like Knowledge. Fourteen JSON re-parsed, fourteen
  Scala regenerated on the recompiled engine (bare type plus import), `DrakeCLI check` clean. Configuration stays host glue
  (the ActivationMode enum, KnowledgeService, the session methods); #49 stays open for that track. Direct run of every discovered suite: 1127 tests / 79 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline unchanged — example gate 48 / 0, surface losses 0 across 184 types, parse scope 111 + 73 with 0 held back, scenario 23 clean, GenDrake 109 of 109, both aspect probes 111 complete, the PON pair 42 and 7 as suspended, the rete finding categorical; the tree afterwards held the intended changes only.
- **Next action:** ONE recommendation from
  the 22 open issues, its owning issue named, for accept-or-correct.
- **Observed, not fixed:** `scripts/inspect-type.scala` has not compiled since 2026-10-17 (recorded with the #47 increment).
- **Not done, by design:** no tracker action; no journal chapter; no push.
- **Everything else stands as the sections below record.**

## Previous checkpoint — the third increment from the reconciled backlog: the FactDisjointness rule (Claude Code, November 9, 2026; done, see above)

Observed: HEAD 995fade on main, this increment's commit (record `git-record-2026-11-09-1231`), committed by Dev on
November 9 on top of a6a0f96 after his gate (1127 tests, zero failures, every headline matching, green on its first run);
the tree was clean after it but for this fold. Whether the day's commits (d026cd3, d8de334, 929c850, a6a0f96, 995fade) are
pushed is to be checked on resumption. 995fade is the last implementation commit and Dev's gate on it is the current
baseline: **1127 tests / 78 suites**.

- **Done this session.** The recommendation on GitHub #63 constraint 4 was accepted ("Accepted, build it"): a core rule,
  `draco.FactDisjointness`, refusing a domain whose rules declare fact types related by derivation, the hazard
  SubtypeFactVisibilityTest measures (the engine drops the insert silently) and the corpus was safe from by shape only
  (measured: 23 rules in 9 domains, none related). Built with no engine change: `TypeDefinition.identity` / `factTypes`
  (a rule's declared fact types as identities through its record), `DomainOntology.ancestors` / `relatedFactTypes`, the
  rule's trio, Draco listing it, the Editor firing it beside the five core rules; five JSON re-canonicalized, five Scala
  regenerated, `DrakeCLI check` clean; FactDisjointnessRulesTest (3) the 78th suite; one corpus type added. The first direct run failed eleven tests, all in the increment (a host regex class in the drake body, the EditorTest rule count, a mid-update Editor spelling), corrected; the second run: 1127 tests / 79 suites including discovery, zero failed, canceled, ignored, pending or aborted, green; every headline as expected — example gate 48 / 0, surface losses 0 across 184 types, parse scope 111 + 73 with 0 held back, scenario 23 clean, GenDrake 109 of 109, both aspect probes 111 complete, the PON pair 42 and 7 as suspended, the rete finding categorical; the eight new tests are the five per-type gates for FactDisjointness and the three of FactDisjointnessRulesTest; the tree afterwards held the intended changes only.
- **Next action:** #63 stays open for its grammar
  points (payload and tag relationships, closedness); no comment until the thread closes or Dev asks. Then ONE
  recommendation from the 22 open issues, its owning issue named, for accept-or-correct.
- **Observed, not fixed:** `scripts/inspect-type.scala` has not compiled since 2026-10-17 (recorded with the #47 increment).
- **Not done, by design:** no tracker action; no journal chapter; no push.
- **Everything else stands as the sections below record.**

## Previous checkpoint — the second increment from the reconciled backlog: one stub predicate (Claude Code, November 9, 2026; done, see above)

Observed: HEAD 929c850 on main, this increment's commit (record `git-record-2026-11-09-1139`), committed by Dev on
November 9 on top of d8de334 after his gate (1119 tests, zero failures, every headline matching, green on its first run);
the tree was clean after it but for this fold. Whether d026cd3, d8de334 and 929c850 are pushed is to be checked on
resumption. 929c850 is the last implementation commit and Dev's gate on it is the current baseline: **1119 tests / 77 suites**.

- **Done this session.** The recommendation on GitHub #47 was brought as a design point (where the one stub predicate
  lives) and Dev accepted it ("Accepted, build it"): a global `fix isStub (TypeDefinition -> Boolean)` on TypeDefinition, the
  aspects' `isEmpty` form. Built: TypeDefinition, TypeLoader and Completeness re-authored in drake, JSON re-canonicalized,
  Scala regenerated on the d026cd3 engine (no engine change); DomainBuilder's copy removed and its three sites, eleven test
  sites and six scripts re-pointed; TypeDefinitionTest +1. Semantics unchanged. Direct run of every discovered suite: 1119 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline unchanged — example gate 48 / 0, surface losses 0 across 183 types, parse scope 110 + 73 with 0 held back, scenario 23 clean, GenDrake 108 of 108, both aspect probes 110 complete, the PON pair 42 and 7 as suspended, the rete finding categorical; TypeDefinitionTest the one suite whose count moves (+1); the tree afterwards held the intended changes only.
- **Observed, not fixed:** `scripts/inspect-type.scala` has not compiled since 2026-10-17 (a Dracon value passed to the
  engine's host-Json `expression`); the scripts compile under the `mods` subproject alone, so no gate sees it. Dev's decision.
- **Thread closed.** On Dev's instruction ("Close #47") one comment citing 929c850 was posted (https://github.com/ejb816/nexonix/issues/47#issuecomment-6745941000) and the issue closed as completed on November 9; 22 issues open.
- **Next action:** ONE recommendation from the 22 open issues, its owning issue named and its remaining scope checked against
  the tree, for accept-or-correct. Then ONE
  recommendation from the 22, its owning issue named, for accept-or-correct.
- **Not done, by design:** no tracker action; no journal chapter; no push. The docs point from November 8 stands
  (GETTING_STARTED_TARGET_SCALA.md's authoring section).
- **Everything else stands as the sections below record.**

## Previous checkpoint — the first increment from the reconciled backlog: rule traits derive DracoType (Claude Code, November 9, 2026; done, see above)

Observed: HEAD d026cd3 on main, this increment's commit (record `git-record-2026-11-09-1005`), committed by Dev on
November 9 on top of 7d4ae0a after his gate (1118, zero failures, every headline matching, green on its first run); the
tree was clean after it but for this fold. Whether d026cd3 and 7d4ae0a are pushed is to be checked on resumption. d026cd3
is the last implementation commit; Dev's gate on it is the current baseline.

- **Done this session.** The one next-priority recommendation from the 24 open issues was GitHub #50 item 2 (owning issue
  inspected first, per §1): the rule-only branch of `DracoGenerator.spelledSource` hard-coded a bare `trait X` while every
  other branch renders its trait through `traitDeclaration`, so the trait contradicted the rooted companion. Dev accepted
  ("Accepted, build it"). Built: the branch calls `traitDeclaration(td)`; the 23 rule-only Scala files (five draco
  validation rules, three primes rules, Emit, EmissionReceived, five editor rules, eight example rules) change one line
  each to `trait X extends DracoType`, applied as the recompiled engine renders them, and so do the two forest scenario
  rules under src/test pinned by ScenarioGenTest and `org/nexonix/rules/rete/rules/TupleFact.scala`, which
  TupleFactReteTest writes through the engine on every run — 26 generated files, one line each (25 of 25 with a definition
  on disk whitespace-identical from `GeneratorCLI generate` on the recompiled classes). No definition, JSON or hand-written
  test change. Direct run of every discovered suite: 1118 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or aborted, green; every headline unchanged — example gate 48 match / 0 differ, surface losses 0 across 183 types, parse scope 110 + 73 with 0 held back, scenario 23 clean, GenDrake 108 of 108, both aspect probes 110 complete, the PON pair 42 and 7 as suspended, the rete finding categorical; the tree afterwards held the intended changes only (the direct run's PrimesRulesTest and TupleFactReteTest rewrote their files identically).
- **Thread closed.** On Dev's instruction ("Please close #50") one comment citing d026cd3 was posted (https://github.com/ejb816/nexonix/issues/50#issuecomment-6745150195) and the issue closed as completed on November 9; 23 issues open. The fold commit is d8de334.
- **Next action:** ONE recommendation on GitHub #47 as a design point first, below. Then ONE
  recommendation on the next candidate, GitHub #47 — the stub predicate (TypeLoader.isStub by name, DomainBuilder.isStub by
  definition, Completeness's four inline conditions, six scripts) consolidated into one definition-backed place — as a
  design point first (where it lives), for accept-or-correct.
- **Not done, by design:** no tracker action; no journal chapter; no push. The docs point from November 8 stands:
  GETTING_STARTED_TARGET_SCALA.md's authoring section still presents JSON as what is authored.
- **Everything else stands as the sections below record.**

## Previous checkpoint — the reconciliation verified, the backlog reconciled (Claude Code, November 8, 2026; done, see above)

Observed: HEAD 7d4ae0a on main, this checkpoint's documentation commit (record `git-record-2026-11-08-0924`), committed
by Dev on November 8 on top of 40d02df, the pushed November 6 fold commit; the tree was clean after it, and whether 7d4ae0a
is pushed is to be checked on resumption. The last implementation commit is 48f466d; Dev's last gate
1118 / 77 on November 4 stands. Agent links and the skill are as DRACO.md §5 describes.

- **Done this session.** The read-only verification of the Codex reconciliation (2b9cfd5) was delivered as one
  recommendation and Dev accepted it ("Accept"). All eight closures are justified against the tree and the git
  history (#61, #55, #42, #41, #15, #51, #57, #46). All 24 status comments cite evidence that exists and state
  remaining scope accurately; every engine claim was checked in the source. Two defects: #50 credits 324556c with
  item 1, which landed in b93e3a1 / eafc649 on 2026-07-29; #46 calls part 3 superseded when `format/dracon/Value.drake`
  is a full native tree, so it is completed. After acceptance one correcting follow-up comment was posted on each
  (https://github.com/ejb816/nexonix/issues/50#issuecomment-6722652914, https://github.com/ejb816/nexonix/issues/46#issuecomment-6722653101). Nothing reopened, relabeled or edited; #51's label stays removed. The §1 rule was re-decided by Dev:
  the owning-issue check and the citation in the record stay, the per-commit tracker comment softens to thread close
  or Dev's request, re-dated 2026-11-08. DRACO.md §3's "parse is a measurement tool" sentence is corrected (parse is
  the authoring path; #61's side was right), the "#51 continuation" paragraph is marked historical, README's parser
  caveat and reconciliation paragraph are aligned, and DRACO.md carries a dated paragraph with the findings.
- **One docs point surfaced, not acted on:** GETTING_STARTED_TARGET_SCALA.md's authoring section still presents JSON as
  what is authored and drake as a human-facing surface, the reverse of practice since September. A refresh is Dev's call.
- **Not done, by design:** no tracker action beyond the two comments; no journal chapter; no implementation; no push.
- **Next action:** Dev commits this documentation. Then ONE next-priority recommendation from the reconciled backlog of
  24 open issues, its owning issue named and its remaining scope checked against the tree, for accept-or-correct. No
  candidate is assessed yet. #62's PON remainder is suspended; activation and .dcon deferred; EgoActor observed only.
- **Everything else stands as the sections below record:** journal coverage at chapter 112 with the uncovered range and
  its sources named at the end of this file.

## Previous checkpoint — the aborted Codex handoff and the verification task (Claude Code, November 6, 2026; done, see above)

Prepared at Dev's direction for a new Claude Code session, after the November 4 handoff to Codex went
wrong. Observed: HEAD 6682c41 (this checkpoint's documentation commit, record `git-record-2026-11-06-1255`),
pushed to origin/main on November 6 together with 2b9cfd5, the Codex session's documentation commit
that Dev executed on November 5. Tree at this handoff: clean but for the fold of this state into this
bullet and the resume prompt. The last implementation commit is 48f466d;
Dev's last gate 1118 / 77 on November 4 stands.

- **What the Codex session did.** The November 4 handoff proposed the issue-tracker reconciliation
  audit as the next task and told the receiving agent to ask Dev first what to correct in it. The Codex
  session did not ask: it ran the audit, proposed a batch, read Dev's "I approve your recommendation"
  as approval of the whole batch, and acted on the tracker — 32 status/evidence comments, eight
  closures (#61, #55, #42, #41, #15, #51 completed/reconciled; #57, #46 superseded, "not planned"),
  #51's priority-next label removed — then added the §1 rule "Issue tracking is part of each
  development increment" to DRACO.md, the checkpoint below to this file, a README paragraph and
  `git-record-2026-11-04-1658`, and handed Dev the commit, executed as 2b9cfd5. Its count correction
  (32 open issues, not 34) is right.
- **Dev does not trust that result** and directed this handoff so a Claude Code session verifies it.
  Nothing on the tracker has been touched since; the eight issues remain closed and the comments stand.
- **The task for the receiving session: a read-only verification of the reconciliation**, reported in
  conversation as one recommendation with accept-or-correct — each closure justified, partly justified
  or wrong against the tree and git history; each of the 24 status comments checked for the evidence it
  cites and the remaining scope it states; the #51 label removal and the §1 rule checked against what Dev
  actually approved. Nothing posted, reopened, relabeled or edited on the tracker until Dev says so; only
  after Dev's decision a next-priority recommendation, with its owning issue named. Spot checks on
  November 6 by Claude Code, nothing more: every commit hash the comments cite exists; ExampleMembershipTest,
  the three GETTING_STARTED files and the scripts directory are as the comments describe; DRACO.md §3 still
  says "Parse is a measurement tool, not yet an authoring path (GitHub #61)" while #61 is closed — one
  side is wrong.
- **Everything else stands as the sections below record:** PON suspended; EgoActor, activation and .dcon
  as observed and deferred; journal coverage at chapter 112 with the uncovered range and its sources
  named at the end of this file.

## Codex checkpoint — tracker reconciliation (November 4, 2026; committed by Dev as 2b9cfd5; UNVERIFIED, see above)

This checkpoint supersedes the proposed audit and opening instructions below; older sections remain
historical. Dev accepted the audit, then approved the complete proposed tracker/workflow batch with
"I approve your recommendation". Observed branch main, HEAD 3dfed24; starting tree clean.

- Audited all **32** open GitHub issues, including comments, against source and commits. The earlier
  handoff's 34 was a count error; its explicit issue list contained 32.
- Applied 32 reconciliation comments; closed #61, #55, #42, #41, #15 and #51 as completed/reconciled,
  and #57/#46 as superseded. Removed #51's stale priority-next label. **24 issues remain open**;
  no replacement priority assigned and no new issue created. Original bodies/comments preserved.
- Partial work remains explicit: #43 discriminator syntax, #58 lifecycle dispatch, #59 richer patterns,
  #63 broader payload/closedness/lint scope, #50 rule traits, #47 predicate duplication, #49 discipline,
  #11 generator realization and #12 remaining Dreams acceptance scope. #62's PON remainder is suspended.
- DRACO.md now requires an issue/scope check before recommending an increment, issue references in
  the git-record/change summary, and an evidence update after Dev's verified commit. Missing ownership
  is surfaced to Dev; it does not authorize a new issue. See git-record-2026-11-04-1658 for action detail.
- Local changes are documentation only: DRACO.md, this handoff, DEVELOPMENT_RESUME_PROMPT.md,
  CHANGELOG.md, README.md and the new git record. Not committed or pushed by Codex. No new runtime/test changes;
  no fresh compile or suite run. Latest Dev gate remains 1118 / 77 at 48f466d on November 4.
- Journal coverage remains chapter 112; no journal chapters or coverage markers advanced.
- **Next action:** Dev reviews/commits the documentation. Then bring one evidence-backed priority
  recommendation from the reconciled 24-issue backlog for accept-or-correct; no implementation task
  or replacement priority is accepted yet. Do not repeat the audit approval question below.

## Historical handoff — start here

Read DRACO.md (AGENTS.md and CLAUDE.md are symlinks), this file, and
`.draco/skills/update-draco-docs/SKILL.md`. Inspect `git status --short` and `git log -3` before
editing. Use DEVELOPMENT_RESUME_PROMPT.md as the opening prompt. Then bring Dev ONE recommendation
with accept-or-correct — the audit below is the proposed next task, and it is NOT yet accepted.

- **Branch** main. **Observed HEAD** b79cfc1 (November 4, the handoff commit, documentation only; record
  `git-record-2026-11-04-1446`), pushed to origin/main, on top of 48f466d, the last implementation commit. **Tree** at this
  handoff: clean but for the fold of b79cfc1 into this bullet, the resume prompt and memory. A later agent inspects, never assumes.
- **Latest Dev-verified gate:** **1118 tests / 77 suites**, November 4, on the 48f466d tree: zero
  failed/canceled/ignored/pending tests, zero aborted suites, every headline matching — parse scope
  110 draco + 73 mods with 0 held back (asserted), surface losses 0 across 183 types (asserted),
  scenario 23 clean (asserted), example gate 48 match / 0 differ (asserted), GenDrake 108 of 108,
  DomainAspect and DracoAspect 110 sections, forest 1 of 2, PON 80 / 550 / 42 and 7 differ
  (suspended measurements), rete CO-DECLARATION DROPS THE FACT. The direct run before it (every
  discovered suite on compiled classes with main, mods and test resources on the loader roots):
  1118 / 78 including the Runner's discovery wrapper. The procedure is at the end of this file.
- **State of the work.** Every accepted implementation increment is committed and pushed; the
  tree carried nothing but documentation at this handoff. Twenty-one commits since the last
  journaled commit, all Dev-gated and Dev-committed, all Claude Code sessions (October 26 – November 4):
  e241378, 9779e39, 112f277 (named construction); c5b8089 (terrestrial Output's Location branch);
  e138f04, 8540341, 3a3cbfc (json factories: LocationReport, the sibling reports, the four Intents);
  23333f3 (coordinate surfaces); 7be1497 (example map closed as a gate); ded1b5b (example body sweep,
  validator identity compare); 5f0e023 (sentient surfaces, one accepted correction); ed57aa9
  (anchor-parameter rule); a31e49b (compact fixpoint assertion); 8746ecd (every mods surface a gate,
  Yaml's surface); a4ff2c4 (parse scope widened to the mods tree); 6a15754 (Output hold-out removed);
  b321df6 (surface losses asserted zero); f4b57ae (scenario round-trip asserted); 1cfc38f (mods
  missing-surface gate, PON suspension recorded); 48f466d (parse-scope hold-back asserted). The
  ledger below has each one's record and gate.
- **Two threads closed, one decision taken.** The example-tree inventory of October (every example
  definition a trio, every companion the generator's output) is empty. The measurement-to-assertion
  thread (October 31 – November 4) retired seven report-only figures from the DRACO.md §1 table,
  which now holds the two PON rows and the categorical rete finding alone. PON development is
  SUSPENDED by Dev (November 3) until Dev resumes it for integration into `draco.primes.Primes`.
- **Journal coverage reaches chapter 112**, whose closing commit is 7495539 (October 26). The
  twenty-one commits above and the two Claude Code sessions that produced them are NOT journaled;
  see "Journal coverage and handoff integrity" at the end of this file for sources and procedure.
- **The issue tracker has had no human interaction since August 28** (see the next task). The
  recent records cite no issue but the last two, which cite #62.

## Named-construction head repair (accepted and built, October 26)

Dev accepted, in these words: "spell the named-construction head as authored/resolved, same as
the positional path, with one cross-package compile-and-run test checking scoped and unscoped
generation." `DracoGenerator.constructionInitializer` spells the applied head through
`expression(operands.head)`; the factory check and the `_name` parameter mapping are unchanged.
Unscoped, a cross-package construction keeps its qualification; scoped, the engine shortens it
and adds the selective import. `NamedConstructionTest` (src/test/scala/domains/world) generates,
compiles and runs a domains.world probe constructing domains.terrestrial.Location both ways and
pins an own-package bare head staying bare. Old and new engine generate byte-identical Scala for
the three named-argument sites (aerial/Input, terrestrial/Output, world/Observable) and Consumer;
no committed Scala regenerated.

## Dyn result as a named construction (accepted and built, October 26)

Dev accepted: "route a dyn's result (body `fix value` and leaf) through constructionInitializer
with the existing expression fallback, re-author Observable's `_geocentric:` as `geocentric:` with
its JSON re-canonicalized and Scala byte-identical, and add one NamedConstructionTest case."
`dynResult` renders every dyn result through the construction interception with `expression` as
fallback — never `defaultInitializer`, whose String-slot rule would quote the four Text-typed path
results in the corpus. One enabling change, flagged to Dev with the gate: `loadDracoType` looked up
only a dotted slot type, so the generator now keeps `definitionInEffect` (a lazy thread-local set
while a definition is spelled) and resolves a bare slot type against that definition's package, as
the codec judge does. Observable's JSON moved one leaf; Observable, Input, Output and Consumer
regenerate byte-identical. NamedConstructionTest has four cases.

## The head decides the construction (accepted and built, October 27)

Dev accepted: "key constructionInitializer's factory lookup on the applied head (dotted as
spelled, bare against the definition in effect) instead of the slot type, and add one
NamedConstructionTest case with a parent-typed slot, compiled and run." The lookup asks about
the head leaf; the slot type plays no part; `constructionInitializer` and `dynResult` take the
value alone. Nil corpus impact by construction. NamedConstructionTest has five cases. This closes
the three observations of October 26; nothing from the named-construction thread is preserved.

## Terrestrial Output carries its Location branch (accepted and built, October 29)

Dev accepted: "re-author Output's message as a case branch binding location to
domains.terrestrial.Location, with the three strict bindings and consumer(report), the report
constructed in the OriginateReport form, Output.scala regenerated from the definition, and one
OutputProjectionTest covering exact trio agreement, one report per Location, ignored other
messages and lazy consumer capture." The message body is `case location Location [ … ]` — the
branch type spelled bare, Location being in Output's own package — with two strict bindings
(`now fix payload`, `now fix report`; the accepted words' "three" counted the `mon consumer(report)`
line) and the report as `new LocationReport { … }` host text, the form OriginateReport already
carries. The JSON is re-canonicalized from the drake and Output.scala is the generator's output:
it gains the Terrestrial actor convention (`elementTypeNames`, a `knowledge` registering the
domain's two rules, as Creator and the terrestrial Consumer have) and renders the dispatch as
`case location: Location =>` with the generator's `case _ => ()`. OutputProjectionTest (4) is the
73rd suite. The example map moves to 37 match / 11 differ. LocationReport still has no factory, so
its construction stays host text; a `json` factory on it was named as adjacent, not recommended.

## LocationReport constructs through a json factory (accepted and built, October 30)

Dev accepted: "Build the LocationReport json factory with both terrestrial sites re-authored",
the recommendation being LocationReport's first drake surface carrying `factory parameters par
json Json` (the BARE spelling HostBindings carries and Dracon.drake authors; the qualified spelling
generates a `null.asInstanceOf` Null), its JSON re-canonicalized, its hand-written companion
replaced by the generator's output (which adds `apply(_json: => Json)`, the Dracon-shaped codec
and `Null`), and the two terrestrial sites — OriginateReport's action and Output's Location
branch — re-authored `fix report LocationReport LocationReport(payload)` and regenerated on that
one line each. No engine change. LocationReportTest (3) pins the trio, the by-name argument and
the codec round-trip; the existing OriginateReportTest and OutputProjectionTest pin the two
regenerated sites unchanged. Parse scope moves to 110 + 24, surface-loss coverage to 134 types;
the example map stays 37 / 11. Direct run of every discovered suite on October 30: 947 passed, 75
suites including discovery, zero failed/canceled/ignored/pending/aborted, every headline as stated,
pending mods actors 0 (the first run stopped at test compilation on an accessor in the new test,
corrected; no production file changed between runs). Dev's full sbt gate on October 30: 947 / 74, zero failed/canceled/ignored/pending, every headline matching, pending mods actors 0, green on its first run.

## The sibling reports take the json factory (accepted and built, October 30)

Dev accepted: "Build the three sibling report factories in one increment". Aerial PositionReport,
marine FixReport and ethereal EphemerisReport each gain the drake surface with `factory parameters
par json Json`, JSON re-canonicalized, companion replaced by the generator's output (27 lines
added, none removed), and each medium's OriginateReport re-authored `fix report X X(payload)` and
regenerated on that one line. No engine change. ReportFactoryTest (3), one suite iterating over the
three reports, pins the trios, the by-name argument and the codec round-trip; the four
OriginateReportTests pin the regenerated rules unchanged. Parse scope moves to 110 + 27,
surface-loss coverage to 137 types; the example map stays 37 / 11. Every report in the example
chain now constructs through its definition; the Output thread of October 29 is closed. Direct run of
every discovered suite on October 30: 956 passed, 76 suites including discovery, zero
failed/canceled/ignored/pending/aborted, green on its first run, every headline as stated, pending mods
actors 0. Dev's full sbt gate on October 30: 956 / 75, zero failed/canceled/ignored/pending, every
headline matching, pending mods actors 0, green on its first run. Committed and pushed by Dev as 8540341.

## The four Intent types take the json factory (accepted and built, October 30; Dev verified 967 / 76 on October 31)

Dev accepted: "Accepted, build it". Aerial FlightIntent, marine VoyageIntent, ethereal LaunchIntent and
terrestrial MarchIntent — each medium's entry message, the fact its OriginateReport joins — gain the drake
surface `type X from <Medium> draco format dracon Dracon` with `factory parameters par json Json`, JSON
re-canonicalized (the factory record only), companion replaced by the generator's output (25 lines and two
imports added, none removed). The eight test sites that built an intent as `new X { override lazy val
typeDefinition …; override (lazy) val json = … }` — the four OriginateReportTests, the marine, ethereal and
terrestrial chain tests, AerialAssemblyTest — now construct `X(payload)`. No engine change; no definition
constructs an intent (Creator's messageType is Dracon). IntentFactoryTest (3), one suite iterating over the
four, pins the trios, the by-name argument and the codec round-trip. Parse scope moves to 110 + 31,
surface-loss coverage to 141 types; the example map stays 37 / 11. Every message in the example chain now
constructs through its definition; the *Intent observation of the report records is closed. Direct run of
every discovered suite on October 30: 967 passed, 77 suites including discovery, zero
failed/canceled/ignored/pending/aborted, every headline as stated, pending mods actors 0. Dev's full sbt
gate on October 31: 967 / 76, zero failed/canceled/ignored/pending, every headline matching, pending mods
actors 0, green on its first run. Committed and pushed by Dev as 3a3cbfc.

## Position and Location take their surfaces and the generator's companions (accepted and built, October 31; Dev verified 974 / 77)

Dev accepted: "Accepted, build it". Aerial Position and terrestrial Location take the drake surfaces
`DrakeCLI emit` renders from their JSON (`type Position from Aerial`, three `fix` elements, a factory with
three defaulted parameters); parsing each back reproduces the committed JSON byte for byte, so no JSON
moves. Their hand-written companions are replaced by the generator's output, which differs only by the
codec inferred from the elements (21 lines: encoder by field name, decoder with the factory default for a
missing field, the imports), Cartesian's accepted inferred codecs of October 25 being the precedent; trait,
loader line, kind-vals, `apply` and `Null` are unchanged, so Input, world Consumer and five tests construct
as before. No engine change. CoordinateProjectionTest (3), one suite iterating over the two, pins the trios,
the lazy defaulted factory and Null, and the codec with missing-field defaults and an invalid value refused.
Parse scope moves to 110 + 33, surface-loss coverage to 143 types, the example map to 39 / 9. Direct run of
every discovered suite on October 31: 974 passed, 78 suites including discovery, zero
failed/canceled/ignored/pending/aborted, every headline as stated, pending mods actors 0. Dev's full sbt
gate on October 31: 974 / 77, zero failed/canceled/ignored/pending, every headline matching, pending mods
actors 0, green on its first run. Committed and pushed by Dev as 23333f3.

## The example map closes and becomes a gate (accepted and built, October 31; Dev verified 982 / 77, committed as 7be1497)

Dev accepted: "Accepted, build it". The nine remaining example differences were all companion spelling — the
three ConsumeReport companions (ethereal, marine, terrestrial: `with DracoType` and `override`), the five domain
objects (Aerial, Ethereal, Marine, Terrestrial, World: the same, `import domains._` and the wildcard world
import) and aerial Input (`elementTypeNames`, the unused private `knowledge` Creator carries, lazy bindings in
`receive`) — and the four ConsumeReports had no drake surface. Built as probed with the compiled CLIs: the four
ConsumeReport surfaces are the emitter's rendering (`type ConsumeReport`, the domain line, `rule` with one `var
report <Report>` and a `mon` action), their JSON re-canonicalized by parsing them back (the host-opaque action
string becomes a call tree, aerial's `ctx.getRuntime().get("consumed")` a nested one; the aspect keys take the
canonical order), the Scala generated from either JSON identical, aerial's companion regenerating byte-identical;
the nine companions are the generator's output; `DrakeCLI check` is clean over the five media. ExampleDomainsGenTest,
report-only since 2026-06-23, now asserts zero differ, zero error and zero missing over the 48 example definitions,
and the example-map row leaves the DRACO.md table, the third measurement to become an assertion. No engine change,
no new suite. Parse scope moves to 110 + 37, surface-loss coverage to 147 types, eight DrakeParseTest checks
activate. The first direct run failed ExampleLoadingTest's all-48 resolution: the treed ConsumeReport actions name the
three sibling sinks, and resolution knew only AerialSink (bound by ExampleScope on October 23); ExampleScope now
binds the four sinks by qualified name and the loading test checks each is a binding, not a member. Second direct
run of every discovered suite on October 31: 982 passed, 78 suites including discovery, zero
failed/canceled/ignored/pending/aborted, every headline as stated, pending mods actors 0. Dev's full sbt gate on
October 31: 982 succeeded, zero failed/canceled/ignored/pending, every headline matching, pending mods actors 0 —
982 / 77, green on its first run. Committed and pushed by Dev as 7be1497.

## The example bodies are swept and the validator compares by identity (accepted and built, October 31; Dev verified 983 / 77, committed as ded1b5b)

Dev accepted: "Accepted, build it" on the point raised after 7be1497 — sweep the example bodies as the canonical
corpus was swept on October 21, the one part of the corpus that sweep left out. MEASURED first with a probe over the
compiled classes, compacting all 48 example definitions through the example caller scope (ExampleScope's identities
and bindings): twelve change and gain a record — the four Creators and four OriginateReports, aerial Input,
terrestrial Output, world Consumer and Provider — `draco.format.dracon.Dracon` to `Dracon` (the host text inside
`ctx.getRuntime().get[…]` keeps its spelling), `domains.world.World` to `World` (recorded in Input, own-package bare
in Consumer and Provider including their `Actor(World)` header), Position and Location in Consumer's branch and
Input's binding, and the factory's `ActorType` recorded as the dreams records carry it; anchors (Output's
`messageType domains world World`, every derivation) stay in full; the other 36 are unchanged. Built as probed: the
twelve surfaces are `Drake.emit` of the compact form with a `uses` section, the JSON `DrakeCLI parse` of each new
surface (equal to the probe's compact JSON), the Scala `GeneratorCLI generate` over the current resources — two to
eight lines per file (Creators +8/−6, Input +5/−3, OriginateReports +3/−1, Output +4/−2, Consumer +8/−5, Provider
+3/−3), selective imports gained and the qualified spellings shortened, unscoped and scoped generation byte-identical
for all 48 — `DrakeCLI check` clean over the five media. ONE ENGINE CHANGE: `AssemblyValidator` compared a consumer
parameter's message type with the actor's by TEXT (`m.text` against `messageType.namePath`); run over the swept tree
for the record, the old validator fails both assembly suites with five errors of the shape "expects a consumer of
World but receives domains.world.World". Now `identity(td, text)` resolves a name through `NameResolution.recorded(td)`
(the definition's own record as the scope, the generator's reading of a recorded definition), completes a bare result
with the definition's own package (the compact form's convention for a bare name), and both sides compare by
`namePath` — name and package, parameters free, as definitions are keyed; nothing is loaded or discovered. The actor's
message type is a TypeName anchor the carrier holds in full (the surface elides Provider's own-package `messageType
World`), so the completion is exercised today only on the consumer side. WorldAssemblyTest +1 pins the three compact
spellings with their records, the actors' identities, the clean service and a crossed binding reported with both
identities in full; its first focused run failed on the test's own assertion that Provider's message type reads bare,
corrected before the direct run. No headline moves: twelve surfaces re-emitted, none added. Direct run of every
discovered suite on October 31: 983 passed, 78 suites including discovery, zero failed/canceled/ignored/pending/aborted,
green on its first run, every headline as stated, pending mods actors 0. Dev's full sbt gate on October 31: 983
succeeded, zero failed/canceled/ignored/pending, every headline matching, pending mods actors 0 — 983 / 77, green on
its first run. Committed and pushed by Dev as ded1b5b.

## The sentient domain takes its drake surfaces (accepted and built, October 31; one correction accepted and built; direct run 1005 / 78, Dev verified 1005 / 77, committed as 5f0e023)

Dev accepted: "Accepted, build it" on the point raised after ded1b5b — give the eleven sentient definitions (Sentient,
Course, Direction, Distance, Effect, Ego, Gaze, Lean, Path, Percept, Waypoint, the one example domain with no drake
surface, JSON-only since 2026-06) the surfaces `DrakeCLI emit` renders, re-canonicalize the five whose derivation
argument is a tuple (Direction, Ego, Lean, Percept, Waypoint) by parsing the surface back, and replace the eleven
companions with the generator's output. The five alone in the corpus failed `parse(emit(td)) == td`: the tuple argument
to the parent, `Primal((Gaze, Distance))`, sat in the derivation's type parameter as the pre-tree STRING `"(Gaze,
Distance)"`, the form before type-form trees of 2026-09-17, and the domain had no surface for any sweep to parse.
MEASURED on a scratch copy with the compiled CLIs before recommending: eleven surfaces of one or two lines (`type
Waypoint from Sentient draco Primal((Gaze, Distance))` and the domain line; Sentient's lists its ten members); parsing
back reproduces six JSON byte for byte and moves exactly one leaf in each of the five, to the Objective `{"(,)": ["Gaze",
"Distance"]}`; the Scala from either JSON is whitespace-identical to the committed companions (a leading blank line, one
more in Distance and Sentient, a trailing space on Sentient's trait line — the example gate normalizes whitespace);
`DrakeCLI check` clean; the compact form changes none of the eleven. Built as probed: the eleven `.drake` files are the
emitter's output copied in unchanged, the JSON `DrakeCLI parse` of them (six identical, five moved on the one leaf),
the Scala `GeneratorCLI generate` over the current resources; `DrakeCLI check src/mods/resources/domains/sentient`:
surface drift 0, json drift 0, skipped 0. No engine change, no new test: the round trip is what the 22 newly active
DrakeParseTest checks assert. Headline moves: parse scope 110 + 48, surface losses 0 across 158 types; the example gate
stays 48 / 0. Every definition under `src/mods/resources/domains` is now a trio. THE DIRECT RUN FOUND A DEFECT THE PROBE MISSED:
1004 of 1005 passed, ExampleLoadingTest's all-48 resolution failing with `Ambiguous type 'Distance':
domains.sentient.Distance, draco.base.Distance` — the old string leaf was host-opaque text the resolver never read,
the tree makes `Distance` a name, and a bare name resolves by unique ontology lookup with no own-package precedence
(Dev, 2026-10-20). The probe ran the emitter, parser, check and generator but not the two-stage loader, the one
consumer that resolves type-form leaves. Measured after: the whole ontology (182 definitions as authored) holds exactly
two ambiguous bare anchor parameters, Percept's and Waypoint's; spelled `domains.sentient.Distance` in full (the
sweep's convention for a non-unique simple name, as Decoder and Encoder are spelled in the Dracon domain) the resolver
reads both, the surface round-trips byte for byte, `DrakeCLI check` is clean, and the two companions regenerate with one
trait line changed, `Primal[(Gaze, domains.sentient.Distance)]`. Separately, the COMPACT form of either spelling fails:
`NameResolution.rewrite`'s `anchorParameter` renders an own-package anchor parameter bare unconditionally where `short`
checks uniqueness, so `resolve` writes a bare `Distance` that a second read cannot resolve — an engine inconsistency on
a collision the corpus never presented before, not met by any gate, the next related point. Dev ACCEPTED the correction
("Accepted, build it"): Percept's and Waypoint's headers read `Primal((Gaze, domains.sentient.Distance))`, their JSON is
the parse of the new surface (byte-equal to the probe's), their companions regenerate on the one trait line, no engine
change; `DrakeCLI check` over the domain: surface drift 0, json drift 0. SECOND DIRECT RUN: 1005 tests / 78 suites
including discovery, zero failed, canceled, ignored, pending or aborted, every headline as stated, pending mods actors 0.
The record (git-record-2026-10-31-1938) carries the measurements and the correction. OBSERVED, not recommended: `domains/sentient/EgoActor.scala` is a hand-written actor over
Ego with no definition. Deferred, neither accepted: activation (Codex, 2026-10-15) and the .dcon migration.

## An anchor parameter's bare form takes the body leaf's uniqueness rule (accepted and built, November 1; direct run 1006 / 78, Dev verified 1006 / 77, committed as ed57aa9)

Dev accepted: "Accepted, build it" on the point raised after 5f0e023 — make `anchorParameter` in `NameResolution.rewrite`
apply the same judgment as `short`: a local binding keeps its authored spelling, an own-package name is written bare
only when `short` would shorten it (simple name unique across the ontology and the host vocabulary), everything else
stays in full. The resolver had written an own-package anchor parameter bare unconditionally, so `resolve` turned
Percept's and Waypoint's `domains.sentient.Distance` (the 5f0e023 correction) back into the bare name the loader had
refused, and the compact form of either (resolve, then re-read) failed; no gate compacts the example definitions and
the example gate generates unscoped. MEASURED over all 183 definitions before recommending: 33 bare anchor parameters,
thirteen own-package (DomainType, TypeDictionary, TypeName, TypeDefinition, XML, Dracon ×2, Gaze ×2, Percept, Effect,
World ×2) every one unique and no host binding's name, six already in full, twenty formals or host leaves — so no
committed JSON or Scala changes; the only observable change is that Percept's and Waypoint's compact form equals the
committed JSON and reads back. BUILT: one conjunct on `anchorParameter` (`short(full, local) != full`);
ResolutionRecordTest +1 (five cases) over the example caller scope — the two tuples
`{"(,)": ["Gaze", "domains.sentient.Distance"]}`, resolve to self, compact to the committed JSON, read back, unscoped
equals scoped generation equals the committed companion; Ego's `Holon(Percept, Effect)` and TypeDictionary's
`Dictionary(TypeName, TypeDefinition)` still bare. REJECTED: dropping the own-package restriction so an anchor
parameter could take the record's short form. DIRECT RUN: 1006 tests / 78 suites including discovery, zero failed,
canceled, ignored, pending or aborted, green on its first run, every headline unchanged, pending mods actors 0. The
record (git-record-2026-11-01-0831) carries the measurement. OBSERVED, not recommended: `domains/sentient/EgoActor.scala`
is a hand-written actor over Ego with no definition. Deferred, neither accepted: activation and the .dcon migration.

## The whole corpus is asserted a fixpoint of the compact form (accepted and built, November 1; Dev verified 1007 / 77, committed as a31e49b)

Dev accepted: "Accepted, build it" on the point raised after ed57aa9 — one new ResolutionRecordTest case asserting over
every definition under `src/main/resources` and `src/mods/resources` that its compact form is the definition itself and
reads back to the same resolution, the canonical scope (`CanonicalScope.scope`) for draco and the example caller's scope
(a `NameResolution` over `ExampleScope.declarations` and `ExampleScope.bindings`) for domains. WHY: the record of ed57aa9
named the gap its defect had passed through — nothing compacts the example definitions, the two-stage loader never
re-reads its result — and the suite kept that shape after it; the sweeps' claim that every definition is in the compact
form was measured in their records and asserted nowhere as a whole. MEASURED before recommending, on the compiled classes
of the ed57aa9 direct run: 183 definitions (the nameless domain's `.json` anchor excluded), 183 whose compact form encodes
to the definition, 183 whose compact form reads back; on the tree of 5f0e023 the same walk fails on Percept and Waypoint.
BUILT: the sixth ResolutionRecordTest case — the walk over the two resource roots, the scope chosen by package, the two
assertions per definition naming the failing stem, the count bounded below at 183 (as the suite's whole-corpus generation
case bounds the main corpus at 110: a silent loss fails, an added type does not). No engine line, no definition, no
committed Scala, no new suite; no headline moves. DIRECT RUN: 1007 tests / 78 suites including discovery,
zero failed, canceled, ignored, pending or aborted, green on its first run, every headline unchanged (parse scope 110 + 48,
surface losses 0 across 158 types, example gate 48 / 0, GenDrake 108 of 108, aspects 110, PON 80 / 550 / 42 and 7, scenario
23 clean, forest 1 of 2, CO-DECLARATION categorical), pending mods actors 0. The record (git-record-2026-11-01-1228) carries the
measurement; the commit also carries the ed57aa9 fold in these documents. OBSERVED, not recommended:
`domains/sentient/EgoActor.scala`, a hand-written actor over Ego with no definition — referenced nowhere but a test
comment and a review-ledger row, a June stub modeled on NaturalActor, no test, no assembly, the retired Actor suffix in
its name; a role-named definition or removal is Dev's decision. Deferred, neither accepted: activation and the .dcon
migration.

## Every mods drake surface is a gate, and Yaml takes its surface (accepted and built, November 2; direct run 1068 / 78, Dev's gate 1068 / 77, committed as 8746ecd)

Dev accepted: "Accepted, build it" on the point raised after a31e49b. WHY: the fixpoint walk's 183 definitions hold exactly
one with no drake surface, the staged `draco.format.yaml.Yaml` (kept when YAML authoring was retired, chapter 56; referenced
nowhere outside its trio), and it was invisible to every gate because DrakeGenTest's mods section — since the actor-emission
increment of 2026-09-10 — scanned only `domains` and pinned `Drake.emit` against the authored surface for the 12 definitions
there whose `actorAspect` is Present (the dreams Editor, under `draco/dreams`, sat outside the scan), its headline counting only actors; its comment called the plain and rule mods types "a later
pass". So the emit direction of 60 mods surfaces was pinned by nothing but the hand-run `DrakeCLI check` (DrakeParseTest pins
`emit(parse(source))` for the example tree; DreamsTest, InitializationTest and ServiceTest pin their own surfaces by name).
MEASURED before recommending, on the a31e49b classes, over all 73 mods definitions: 72 with a surface, 72 of 72 emitting it
byte for byte, 72 of 72 parsing back to the committed JSON, Yaml alone without; Yaml's emitted surface is two lines, `type
Yaml from draco format Format(String)` and `domain draco format yaml Yaml` (XML's form), parses back to the committed JSON
byte for byte, and `Yaml.scala` regenerates byte-identical. BUILT: the mods section walks the whole of `src/mods/resources`,
drops the actor filter, keeps the exact-match test — 73 per-type gates today — and the report-only test is "mods definitions
still lacking a .drake", logging `mods definitions pending .drake authoring (N)`, the phrase kept so the gate grep still reads
it; `Yaml.drake` is `DrakeCLI emit --write` over its directory, unedited; `DrakeCLI check src/mods/resources` surface drift 0,
json drift 0. The DRACO.md table row is relabeled "mods definitions pending `.drake`", value 0. No engine, JSON or Scala
change; the parse scope stays 110 + 48, DrakeParseTest's mods scope being the example tree — widening it is a point of its
own, not recommended here. Flagged, not recommended: removing Yaml instead, Dev's decision; the surface preserves the
definition either way. FOCUSED: DrakeGenTest 184 tests (110 main, 73 mods, the report; 123 before — the recommendation had counted 13 actors and expected 1066), green on its first run
— the probe harness's first attempt aborted before any test with its class directory first on the classpath, where
`Main.roots` reads the source root; recompiled into the test class directory it ran, nothing in the tree changed. DIRECT RUN:
1068 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run, every
headline unchanged, pending mods definitions 0. Dev's full gate 1068 / 77 on November 2, green on its first run, every
headline matching, pending mods definitions 0 (grep count 1); committed and pushed by Dev as 8746ecd.

## DrakeParseTest's mods scope widens to the whole mods tree (accepted and built, November 2; Dev verified 1118 / 77, committed as a4ff2c4)

Dev accepted: "Accepted, build it" on the point raised after 8746ecd. WHY: after that commit the emit direction of every
mods surface is a gate, but the parser's two round-trip gates — `emit(parse(source))` reproduces the surface,
`parse(emit(td))` reproduces the definition — and the surface-loss measurement still walked `domains` alone, as since the
actor-emission increment of 2026-09-10; the 25 staged definitions under `src/mods/resources/draco` (the dreams Editor,
Initialization, the User requests, the service types, Yaml) sat outside all three. DreamsTest, EditorTest, InitializationTest
and ServiceTest pin 24 of the 25 by hand-listed name, in the emit direction only; ServiceTest compares the parsed JSON with the
committed JSON for two Service members; Yaml was pinned by nothing in the parse direction, and a hand list admits a staged
definition silently, as it admitted Yaml. MEASURED before recommending, on the 8746ecd classes, with a scratch copy of the
suite walking the mods root: 380 tests, zero failed — 73 surface gates (48 before), 72 definition gates (terrestrial Output
still held out for its inline tuple argument), the reports and structural cases; `drake surface losses: 0 fields across 183
types` (158 before), the whole corpus and the fixpoint assertion's count; `Drake.parse scope: 110 draco + 73 mods types in`
(110 + 48 before). BUILT: `modsPaths` walks `modsResourceRoot` itself instead of its `domains` child, the two filters and the
hold-out unchanged; the class doc, the mods section comment and the loss test's comment say "mods definitions" where they
said "mods actors", the historical note on why the actors were once outside kept; the by-name pins stay. No engine,
definition or Scala change, no new suite. The DRACO.md table rows move in this commit: losses 158 → 183 types, scope 110 + 48 →
110 + 73. Flagged, not recommended: keeping the by-name pins as the coverage — Yaml would need hand-listing and 23 staged
definitions would still lack the parse direction. FOCUSED: DrakeParseTest 380 (330 before), green on its first run, both
headlines as measured. DIRECT RUN: 1118 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or
aborted, green on its first run, every other headline unchanged, pending mods definitions 0. DEV'S GATE: 1118 / 77 on
November 2, zero failed, canceled, ignored or pending, every headline matching, pending mods definitions 0, green on its first
run. Committed and pushed by Dev as a4ff2c4.

## The stale Output hold-out leaves DrakeParseTest's definition gate (accepted and built, November 2; Dev verified 1119 / 77, committed as 6a15754)

Dev accepted: "Accepted, build it" on the point raised after a4ff2c4. WHY: `inlineTupleArgument` held terrestrial Output out
of `parse(emit(td))` since 0acf2da (2026-08-12), the one definition of 73 outside the definition gate and the only entry the
set ever held. Its comment was true then — an inline tuple argument carrying an application was one token to the lexer, so
the application came back as host-opaque text — and false since two dated changes: tuple and collection literals treed on
2026-09-20 (glued tokens split at depth-zero parentheses and commas since 2026-09-16), and Output.json re-canonicalized from
its own surface at c5b8089 on 2026-10-29, so the committed JSON carries the payload as a full tree and is the parser's
reading by construction. MEASURED before recommending, on the a4ff2c4 classes: `DrakeCLI parse Output.drake` equals the
committed JSON byte for byte; `DrakeCLI check` over the terrestrial directory is clean; a scratch copy of the suite with the
set emptied, compiled into the overlay, passes 381 with Output's definition case green and both headlines unchanged. BUILT:
the val and its comment, the `filterNot` on the definition walk and the file-only "mods held out of the JSON gate" log line
are removed; the report test keeps its other lines and the console headline. No engine, definition or Scala change, no new
suite, no headline moves. Flagged, not recommended: keeping the set as an empty mechanism against a future hold-out — dead
code guarding a case the parser no longer has. FOCUSED: DrakeParseTest 381 (380 before), green on its first run. DIRECT RUN:
1119 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run, every
headline unchanged, pending mods definitions 0. DEV'S GATE: 1119 / 77 on November 2, zero failed, canceled, ignored or
pending, every headline matching, pending mods definitions 0, green on its first run. Committed and pushed by Dev as 6a15754.

## The surface-loss count is asserted at zero and leaves the table (accepted and built, November 2; direct run 1119 / 78, Dev's gate 1119 / 77)

Dev accepted: "Accepted, build it" on the point raised after 6a15754. WHY: DrakeParseTest's "measured tail" — the
unnormalized comparison of every definition's raw encoder leaves with those of `parse(emit(td))`, report-only since the
parser's first increments — had read zero since b04d81d (2026-10-22, the role-presence re-canonicalization of BodyElement and
ActorAspect; one field from 2026-09-22, fifteen or sixteen before), covered the whole corpus (183 types) since a4ff2c4, and is
stricter than gate 2, whose `surfaceCarried` normalization lets a string on one side and a tree on the other pass. Zero means
every definition's JSON is exactly the parser's reading of its own surface; a measurement with nothing left to measure, whose
regression a green suite would hide. BUILT: the test is renamed "drake surface losses, unnormalized: none" and fails after its
console line when the loss list is nonempty, listing each field; section comment and class doc reworded (tail closed
2026-10-22, asserted since 2026-11-02; a loss means re-canonicalize from the drake). DRACO.md: the row leaves the §1 table, the
assertions paragraph names the fourth, the "read the report-only numbers" rule drops surface losses. The console headline is
unchanged and stays in the gate grep. No test count moves, no engine, definition or Scala change. NEGATIVE PROBE after building,
on a scratch project tree with copied resources and overlays (the suite reads sources from the project root the first classpath
entry sits under, not from the classpath — a probe class directory alone aborts the Runner): one empty-collection `value` of
draco/Action.json set to the legacy "Seq()"; gate 2 still passes Action, the asserted test fails naming
`.dracoAspect.factory.parameters[0].value: "Seq()" -> {"[]":[]}`, 380 / 1, headline `1 fields across 183 types
[empty-collection spelling: 1]`; the real tree untouched. Flagged, not recommended: the same move for ScenarioDrakeTest's row.
FOCUSED: DrakeParseTest 381, green on its first run. DIRECT RUN: 1119 tests / 78 suites including discovery, zero failed,
canceled, ignored, pending or aborted, green on its first run, every suite's count unchanged, every headline unchanged, pending
mods definitions 0. DEV'S GATE: 1119 tests / 77 suites on November 2, zero failed, canceled, ignored or pending, every headline
matching, pending mods definitions 0, green on its first run. Committed and pushed by Dev as b321df6.

## The scenario round-trip is asserted and leaves the table (accepted and built, November 3; direct run 1119 / 78, Dev's gate 1119 / 77)

Dev accepted: "Accepted, build it" on the point raised after b321df6. WHY: ScenarioDrakeTest (since 0af56fd, 2026-08-27)
walked the 23 drake surfaces under src/test/resources/scenario, checked that emitting what the parser read reproduces the
source, whitespace-normalized, logged each file's parsed shape and ended in `succeed`; its class doc called it a worklist for
the grammar. The worklist was empty at its first reading (22 clean on 2026-08-27; 23 since the scenario became a corpus at
87a2bb9 on 2026-08-31) and never held a rejected or drifted file through every re-canonicalization that touched the scenario.
ScenarioGenTest's first gate asserts the JSON direction (each committed .json is the parse of its .drake) and its other four
the Scala, the accepted rules, the transform shape and the run; neither DrakeGenTest nor DrakeParseTest walks the scenario, so
the surface direction was the one unasserted leg of that corpus. The class doc was also stale: RootInterface's dispatch has
been two `case` branches since 12bed84, not a host-opaque Scala `match`. BUILT: the test is renamed "the scenario in today's
drake: every file parses and round-trips" and fails after its console line naming each rejected file with its error and each
drifted file; the class doc's report-only paragraph becomes "A gate since 2026-11-03; a worklist before", with the
RootInterface sentence corrected. DRACO.md: the row leaves the §1 table (which now holds the parse-scope row, the PON pair, the
file-only DrakeGenTest row and the categorical rete row), the assertions paragraph names the fifth, the "read the report-only numbers"
rule keeps PON as its one live example. The console headline is unchanged and stays in the gate grep. No test count moves,
no engine, definition or Scala change. REJECTED: folding the scenario into DrakeParseTest's walk. NEGATIVE PROBE after
building, on the scratch project tree with copied scenario resources and overlays, run with that tree as the working
directory (this suite reads `src/test/resources/scenario` relative to the working directory, not from the classpath root the
corpus suites derive): RootInterface given a redundant parenthesized pair the parser dissolves, birch Compound a stray section
line; the asserted test fails naming both, `REJECTED birch/Compound.drake: Drake.parse: unexpected section 'whatever' in
Compound` and `DRIFTED ash/RootInterface.drake`, headline 23 files, 1 rejected, 1 drifted, 21 clean; the real tree untouched.
FOCUSED: ScenarioDrakeTest 1, green on its first run. DIRECT RUN: 1119 tests / 78 suites including discovery, zero failed,
canceled, ignored, pending or aborted, green on its first run, every suite's count unchanged, every headline unchanged, pending
mods definitions 0. DEV'S GATE: 1119 tests / 77 suites on November 3, zero failed, canceled, ignored or pending, every headline
matching, pending mods definitions 0, green on its first run. Committed and pushed by Dev as f4b57ae.

## The mods drake walk takes the main walk's gate and its file-only pending count retires (accepted and built, November 3; direct run 1118 / 78, Dev's gate 1118 / 77)

Dev accepted: "Accepted, build it" on the point raised after the PON suspension. WHY: DrakeGenTest's two walks differed in
shape. The main walk creates one test per definition under src/main/resources/draco and fails it with "missing X.drake" when the
surface is absent; the mods walk (every mods definition since 8746ecd, actors under domains alone from 2026-09-10) created a
test only for definitions that already had a surface and counted the rest in "mods definitions pending .drake authoring (N)" —
report-only since 1295a82 (2026-07-17), written to target/test-output/DrakeGenTest.log and to no console, read only by the
trailing `grep -c` of Dev's gate block. No other gate sees a missing mods surface (DrakeParseTest's mods walk collects .json
for the definition direction and .drake for the surface direction; the surface-loss assertion needs no file). The count has
been zero since Yaml took its surface. BUILT: the mods walk creates a test for every mods .json, `readModsDrake` mirroring
`readDrake`, the test failing `missing mods <path>`; the guard and the report-only test are gone; the section comment dates
the three states. DRACO.md: the §1 table loses its DrakeGenTest row (the table now holds the parse-scope row, the suspended
PON pair and the categorical rete row), the table's lead sentence no longer names a file-only exception, the assertions
paragraph names the sixth, the lead carries 1118. The gate block drops its trailing grep. Test count 1119 to 1118; no engine,
definition or Scala change. REJECTED: asserting inside the pending test and keeping the file-only headline. NEGATIVE PROBE
after building, on a scratch project tree (copied main, mods and test resources; copies of the sbt class directories with the
current resources overlaid and hidden anchors removed; the rewritten suite compiled into the copied test classes) with
draco/format/yaml/Yaml.drake removed: exactly one test fails, `mods draco/format/yaml/Yaml.json: Drake.emit matches
draco/format/yaml/Yaml.drake` with `missing mods draco/format/yaml/Yaml.drake`, 182 passed / 1 failed; the real tree untouched.
FOCUSED: DrakeGenTest 183 (110 main, 73 mods), green on its first run. DIRECT RUN: 1118 / 78 including discovery, green on its first run, every headline unchanged, DrakeGenTest 184 to 183 the only per-suite change. DEV'S GATE: 1118 tests / 77 suites on November 3, zero failed, canceled, ignored or pending, every headline
matching, green on its first run, no file-only line to read. Committed and pushed by Dev as 1cfc38f on November 4.

## The parse-scope hold-back is asserted at zero over both corpora and its row leaves the table (accepted and built, November 4; direct run 1118 / 78, Dev's gate 1118 / 77)

Dev accepted: "Accepted, build it". A CORRECTION opened the point: after 1cfc38f this document and the record called the parse-scope
row a count that moves legitimately with the corpus and said no measurement with a tail remained in scope; the row's "0 held back"
IS a tail with a settled value, printed by DrakeParseTest's report-only test "types held back from Drake.parse". WHY: the test
counted the two kinds of definition the suite filters out of both round-trip gates — one carrying a codec discriminator, one with no
drake surface — and both are already failures in DrakeGenTest (a missing surface by path, main always and mods since 1cfc38f; a
discriminator through `Drake.emit`'s error "codec discriminator not yet emitted"), so the parser suite silently dropped what the
emitter suite loudly refuses. The count has been zero since the table's origin at 87a2bb9: no definition in either corpus carries a
discriminator (the one hit for the word is draco/CodecAspect.json, as an element name), surfaces are complete at 110 / 110 and
73 / 73. And the mods half was never counted: the lists ran over the main paths alone while the mods walk applied the same filters.
BUILT: the test — renamed "types held back from Drake.parse: none, over both corpora" — walks the mods root as `modsPaths` does
before its filters, appends the mods held-back paths prefixed "mods " to both lists, keeps its log and console lines, and fails
listing each held-back definition with its reason; the class doc describes the two filters and the assertion instead of a
worklist. DRACO.md: the §1 table loses its DrakeParseTest row (the table now holds the suspended PON pair and the categorical rete
finding alone), the assertions paragraph counts seven and names this one, the rule paragraph lists the hold-back among the asserted
measurements, the lead carries the pending gate. The `parse scope` line stays in the gate grep. No test added or removed (1118);
no engine, definition or Scala change. REJECTED: removing the two filters as well (they define the scope, unlike the Output
hold-out — a point of its own); leaving the row as a corpus count (ResolutionRecordTest bounds the corpus at 110 and 183).
NEGATIVE PROBE after building, on a scratch project tree (copied resources, copies of the direct-run class directories with those
resources overlaid and hidden anchors removed, the rewritten suite compiled into the copied test classes, run from that tree with
its class directories first) with `domains/sentient/Gaze.drake` removed and `"codecAspect": {"discriminator": "tag"}` added to
draco/Action.json: the console line reads `109 draco + 72 mods types in, 1 codec-aspect + 1 drake-less held back`, and exactly
one test fails, naming both — `draco/Action.json: carries a codec discriminator` and `mods domains/sentient/Gaze.json: lacks
domains/sentient/Gaze.drake` — 377 run, 376 passed, 1 failed (the filters drop four per-type tests); the real tree untouched.
FOCUSED: DrakeParseTest 381, green on its first run, the console line `110 draco + 73 mods types in, 0 codec-aspect + 0 drake-less
held back`. DIRECT RUN: 1118 / 78 including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run, every headline unchanged, every suite's count identical to the 1118 baseline. DEV'S GATE: 1118 tests / 77 suites on November 4, zero failed, canceled, ignored or pending, every headline matching, green on its
first run. Committed and pushed by Dev as 48f466d on November 4.

## Every generated rule trait derives DracoType (accepted and built, November 9; direct run 1118 / 78; Dev's gate 1118 / 77, committed as d026cd3)

Dev accepted: "Accepted, build it". OWNING ISSUE: GitHub #50; item 1 landed in b93e3a1 and eafc649
(2026-07-29), item 2 is this increment, the issue's last scope. WHY: the rule-only branch of `DracoGenerator.spelledSource`
hard-coded `trait $ruleName` while the composed, actor, domain, object-only and leaf branches spell their trait through
`traitDeclaration` (modules modifier, header formals, `typeExtends` with the rooted parent, element body); the companion beside
it is rooted (`object X extends App with DracoType` when the loaded chain reaches DracoType), so two of a rule definition's three
projections showed the root and the trait contradicted it. MEASURED: exactly 23 rule-only definitions, none with elements,
globals, formals, factory, derivation or modules, so the rendering is exactly `trait X extends DracoType`; every file already
imports `draco._`; nothing instantiates a rule trait; no test pins a bare rule header; `Rule[T]` has no bound. The two other
bare traits under src (draco.dreams.Service, domains.sentient.EgoActor) are hand-written scaffolds without a definition, untouched.
BUILT: one engine line (and the unused `ruleName` binding removed); 23 corpus files, one line each, plus the test corpus the same
way — the two forest scenario rules (AshBirchAlarm, BirchAlarmReceived) pinned by ScenarioGenTest, and TupleFact.scala, which
TupleFactReteTest writes through the engine on every run (26 generated files in all; NaturalActor, bare and definition-less, untouched).
ENGINE-CHANGE PROTOCOL: the engine and the changed files recompiled with scalac into the direct-run overlay; `GeneratorCLI
generate` from the recompiled classes whitespace-identical to all 25 committed files with a definition on disk; the gates (DracoGenTest for the ten main rules, ExampleDomainsGenTest for the
eight example rules, EditorTest's trio tests for the five editor rules, GenerateAndCompileTest, PrimesRulesTest writing the
three primes rules through the engine into src/main) are the check. No definition, JSON or test change; no count or headline
moves. FIRST DIRECT RUN: the probe put the test class directory on the JVM classpath as well as the runpath, against step 5 of
the procedure below, so the scenario definitions sat at two loader roots and ScenarioGenTest's three loading tests failed on the
unique-root requirement — 1115 / 3, a harness error, every other suite green, every headline as expected; it also surfaced the two
scenario rules the measurement over src/main and src/mods had not counted. SECOND DIRECT RUN, test classes on the runpath alone: 1118 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or aborted, green; every headline unchanged — example gate 48 match / 0 differ, surface losses 0 across 183 types, parse scope 110 + 73 with 0 held back, scenario 23 clean, GenDrake 108 of 108, both aspect probes 110 complete, the PON pair 42 and 7 as suspended, the rete finding categorical; the tree afterwards held the intended changes only (the direct run's PrimesRulesTest and TupleFactReteTest rewrote their files identically). DEV'S GATE: 1118 tests on November 9, zero failed, canceled, ignored or pending, every headline matching, green on its first run. Committed by Dev as d026cd3 on November 9, folded as d8de334; #50 closed on the tracker the same day on Dev's instruction. Record `git-record-2026-11-09-1005`.

## The stub predicate has one definition, TypeDefinition.isStub (accepted and built, November 9; direct run 1119 / 78; Dev's gate 1119 / 77, committed as 929c850)

Dev accepted: "Accepted, build it", on a design point brought first. OWNING ISSUE: GitHub #47; its November 4 status names the
remaining scope as consolidation against current callers. WHY: the four-aspect stub test was spelled in five places and owned by
none — TypeLoader.isStub by name, DomainBuilder.isStub by definition (three internal sites, eleven test sites), Completeness's four
`con` lines, six scripts negating it by hand; DerivationResolvable no longer carries it. THE DESIGN POINT: a global `fix isStub
(TypeDefinition -> Boolean)` on TypeDefinition, exactly DracoAspect.isEmpty's and DomainAspect.isEmpty's form — the type that owns
the shape owns the test of it; TypeLoader's by-name isStub kept as the engine's convenience, delegating; Completeness one
condition; DomainBuilder's copy removed; every site says TypeDefinition.isStub. REJECTED: `isEmpty` as the name; equality with
`TypeDefinition(td.typeName)` (a semantic change). BUILT as accepted: the three trios through the compiled CLIs on the d026cd3
engine, `DrakeCLI check` clean; DomainBuilder, eleven test lines, six scripts re-pointed; TypeDefinitionTest +1 (1119). No engine
change, so no two-run protocol. OBSERVED, not fixed: scripts/inspect-type.scala has not compiled since 2026-10-17 (Dracon value to
the host-Json `expression`), seen when the six scripts were compiled against the new classes; the other five compile.
DIRECT RUN: 1119 tests / 78 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline unchanged — example gate 48 / 0, surface losses 0 across 183 types, parse scope 110 + 73 with 0 held back, scenario 23 clean, GenDrake 108 of 108, both aspect probes 110 complete, the PON pair 42 and 7 as suspended, the rete finding categorical; TypeDefinitionTest the one suite whose count moves (+1); the tree afterwards held the intended changes only. DEV'S GATE: 1119 tests on November 9, zero failed, canceled, ignored or pending, every headline matching, green on its first run. Committed by Dev as 929c850; #47 closed on the tracker the same day on Dev's instruction. Record `git-record-2026-11-09-1139`.

## FactDisjointness: the co-declaration hazard becomes a definition-time rule (accepted and built, November 9; direct run 1127 / 79 on its second run; Dev's gate 1127 / 78, committed as 995fade)

Dev accepted: "Accepted, build it". OWNING ISSUE: GitHub #63, constraint 4 ("No two rules in one domain may declare fact types
related by inheritance"); the grammar points of the issue stay open. WHY: the table's categorical row — co-declared types from one
hierarchy and Evrete resolves neither, skips the insert, logs a warning — was guarded by nothing; MEASURED over the whole corpus,
25 domains, 9 with rules, 23 rules, no related pair (siblings per medium, a disjoint taxonomy in Draco, six request types in the
Editor whose shared parents no rule declares). THE DESIGN: a definition-time rule owned by draco as Uniqueness is, joining
`var d DomainType` with `var o DomainOntology`, one Problem naming the domain and every pair in which a declared fact type is an
ancestor of another through the derivation edges; a variable's type text read as an identity through the member's record (dotted →
path, bare → `uses`, else own-package). REJECTED: DomainBuilder.validate (the Editor does not run it); a check at knowledge
construction. BUILT, no engine change: TypeDefinition.identity and factTypes as globals beside isStub (the split is the character class `"[.]"` — no
escaped backslash in a drake string and no host class name in the body); DomainOntology.ancestors (recursive over derivations, by namePath) and
relatedFactTypes; FactDisjointness.drake with `con o.relatedFactTypes(d).nonEmpty` and an action binding the pairs as "X derives Y";
Draco's types list and the Editor's `uses` and start block; five JSON re-canonicalized and five Scala regenerated through the
compiled CLIs on the a6a0f96 classes; `DrakeCLI check` clean over main and mods. The rule passes its own lint (DomainType and
DomainOntology, both already declared in Draco's session, unrelated). FactDisjointnessRulesTest (3): canonical ontology zero
Problems; siblings pass, a type and its parent refused naming "example.lint.C derives example.lint.A"; record over own package,
dotted as path, a plain type declares nothing — green on its first focused run. One corpus type added: parse scope 111 + 73,
surface losses 0 across 184 types, 111 aspect sections, GenDrake 109 of 109. FIRST DIRECT RUN 1116 / 11, three causes in the increment: the split had named
`java.util.regex.Pattern`, a host class outside the HostBindings vocabulary, which every NameResolution path over TypeDefinition's
body refused (eight tests across DomainLoadingTest, ExampleLoadingTest, ResolutionRecordTest, DracoGenTest, GenerateAndCompileTest);
EditorTest's registered-rule count moved from ten to eleven; and Editor.scala, generated while the overlay was mid-update, spelled
the new line bare where the canonical scope spells `draco.FactDisjointness` like the other core rules — all three corrected,
focused reruns green. SECOND DIRECT RUN: 1127 tests / 79 suites including discovery, zero failed, canceled, ignored, pending or aborted, green; every headline as expected — example gate 48 / 0, surface losses 0 across 184 types, parse scope 111 + 73 with 0 held back, scenario 23 clean, GenDrake 109 of 109, both aspect probes 111 complete, the PON pair 42 and 7 as suspended, the rete finding categorical; the eight new tests are the five per-type gates for FactDisjointness and the three of FactDisjointnessRulesTest; the tree afterwards held the intended changes only. DEV'S GATE: 1127 tests on November 9, zero failed, canceled, ignored or pending, every headline matching, green on its first run. Committed by Dev as 995fade. Record `git-record-2026-11-09-1231`.

## Definitions name the draco.rete wrap types (accepted and built, November 9; direct run 1127 / 79; Dev's gate 1127 / 78, committed as 2804904)

Dev accepted: "Accepted, build it". OWNING ISSUE: GitHub #49, the type-vocabulary half its 2026-07-28 review and 2026-07-31
cross-link set as the principled axis (wrapped type names in main, binding in mods — WHAT; configuration stays host glue — HOW);
the retracted configuration domain is not revived and the issue stays open. MEASURED: twelve definitions spelled
`org.evrete.api.StatefulSession` in full (Draco, Emitter, Editor, TransformBinding, four Creators, four Consumers), three spelled
`org.evrete.api.Knowledge` (Primes, Editor, TransformBinding); `draco.rete.StatefulSession` had no reference; Rule and RuleType name
`Knowledge` and `RhsContext` bare with `uses` records; a CLI probe showed a bare `StatefulSession` reaching Scala with no import —
no host-import row — and HostBindings keying it bare where Knowledge is keyed qualified. BUILT: one engine row in
`externalTypeImports`; HostBindings re-keyed; fourteen surfaces re-authored with the bare wrap type and `draco rete …` in `uses`
(Draco and the Consumers gaining their first record); JSON re-parsed, Scala regenerated on the recompiled engine (twelve files gain
`import org.evrete.api.StatefulSession`, three `import org.evrete.api.Knowledge`, the Editor's moving from the actor frame; no
duplicate), `DrakeCLI check` clean over main and mods. WHAT STAYS: `ActivationMode.CONTINUOUS`, `new KnowledgeService()`, the
session methods. REJECTED: a new domain or session wrapper; a capability for the enum sites. No test or headline moves.
DIRECT RUN: 1127 tests / 79 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline unchanged — example gate 48 / 0, surface losses 0 across 184 types, parse scope 111 + 73 with 0 held back, scenario 23 clean, GenDrake 109 of 109, both aspect probes 111 complete, the PON pair 42 and 7 as suspended, the rete finding categorical; the tree afterwards held the intended changes only. DEV'S GATE: 1127 tests on November 9, zero failed, canceled, ignored or pending, every headline matching, green on its first run. Committed by Dev as 2804904. Record `git-record-2026-11-09-1415`.

## The host text in definition values is bounded by an asserted seam list (accepted and built, November 10; direct run 1128 / 80; Dev's gate 1128 / 79, committed as e2ca0be)

Dev accepted: "Accepted, build it". OWNING ISSUE: none exactly, surfaced with the recommendation; nearest GitHub #59 (its tail
metric; its one corpus client is Dracon's destructure), which stays open. MEASURED: 1984 string leaves under a `value` in main and
mods, 24 carrying host syntax in 14 definitions (one a quoted-message false positive), three outside the seams — ActorAspect's
`aa => …`, CLI's and SourceContent's `try … finally`. BUILT: the lambda as `\aa -> …` (Scala byte-identical), the two as
`bracket(source, \s -> s.close(), \s -> …)` (one binding each), JSON re-parsed, `DrakeCLI check` clean, no engine change;
`HostTextTest` (1) walks both corpora, reads every string leaf under a `value` (type slots excepted, quoted text a literal) and
asserts host syntax only in the eleven named seams at their recorded counts — CLI 1, TypeLoader 1, Rule 1, Dracon 5, DefinitionPath
3, CirceCodecs 2, the four OriginateReports 1 each, TransformBinding 4: 21 leaves of 1997. Console line `host text in values: …`
added to the gate grep. NEGATIVE PROBE on a scratch tree (the suite reads the corpus relative to the working directory) with the old
ActorAspect and CLI carriers: fails naming ActorAspect's leaf, 23 leaves, 1 outside. The test's first compile stopped on the JSON
fold's inferred type, corrected before its first run. REJECTED: re-authoring seam leaves here; walking the test corpus.
DIRECT RUN: 1128 tests / 80 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline unchanged — example gate 48 / 0, surface losses 0 across 184 types, parse scope 111 + 73 with 0 held back, scenario 23 clean, GenDrake 109 of 109, both aspect probes 111 complete, the PON pair 42 and 7 as suspended, the rete finding categorical — and the new line `host text in values: 21 leaves of 1997 in 11 definitions, 0 outside the 11 named seams`; the tree afterwards held the intended changes only. DEV'S GATE: 1128 tests on November 10, zero failed, canceled, ignored or pending, every headline matching, green on its first run. Committed by Dev as e2ca0be. Record `git-record-2026-11-10-1050`.

## The rule section of the drake surface is definition-backed (accepted and built, November 10; direct run 1143 / 81; Dev's gate 1143 / 80, committed as 1c96d63, pushed)

Dev accepted: "Accepted, build it". OWNING ISSUE: GitHub #11 (native per-operation transformation remains; the issue stays open).
WHY: of the drake surface's sections the type and domain crossed to definitions on 2026-09-28 and 2026-09-22; rule, actor, `uses`
and codec were hand-written in `Drake.emit`. MEASURED: about eighteen emitter lines; 24 definitions with a rule section (11 main,
13 mods), 15 with an actor section, 77 with a record, 4 with a codec payload. BUILT: `draketarget.RuleAspectText` from Surface —
`variables`, `conditions`, `action` as rendered lines, `present` default true, blocks elided when empty, absent rendering nothing and
evaluating nothing; `gendrake.RuleAspectOf` from RuleAspectText — presence by `fold`, pattern elements at level 3 and action body at
level 2 through `elementText ((TypeElement, Int) -> [Text])`; DrakeTarget and GenDrake list them; `Drake.emit` delegates through
`private[draco] ruleAspectText(td)`. Four JSON re-parsed, four Scala regenerated on a settled overlay, the emitter recompiled against
them; `DrakeCLI check` drift 0 over main and mods. RuleAspectTest (5): layout and elisions; role as presence; a definition through
the live emitter and back; all 24 corpus rule sections over 186 definitions; the running Emit rule. Two corpus types: parse scope
113 + 73, surface losses 0 across 186 types, 113 aspect sections, GenDrake 111 of 111. REJECTED: rule and actor together.
DIRECT RUN: 1143 tests / 81 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline as expected — example gate 48 / 0, surface losses 0 across 186 types, parse scope 113 + 73 with 0 held back, host text 21 leaves of 2057 in the 11 named seams and 0 outside, scenario 23 clean, GenDrake 111 of 111, the domain and draco aspect probes 113 complete, `RuleAspect over the corpus: 186 definitions, 24 rule sections reproduced, 0 skipped`, the PON pair 42 and 7 as suspended, the rete finding categorical; the fifteen new tests are RuleAspectTest's five and the five per-type gates of each new definition; the tree afterwards held the intended changes only. DEV'S GATE: 1143 tests on November 10, zero failed, canceled, ignored or pending, every headline matching, green on its first run. Committed by Dev as 1c96d63 and pushed. Record `git-record-2026-11-10-1107`.

## The actor section of the drake surface is definition-backed (accepted and built, November 10; direct run 1158 / 82 on its second run; Dev's gate 1158 / 81, committed as 8b127aa, pushed)

Dev accepted: "Accepted, build it". OWNING ISSUE: GitHub #11, open. MEASURED: about seventeen emitter lines; 15 definitions with
an actor section (2 main, 13 mods); 77 with a record, 4 with a codec payload. BUILT: `draketarget.ActorAspectText` from Surface —
`messageReference`, `start`, `message`, `signal` as rendered lines, `present` default true, the `messageType` line and each block
elided when empty, absent rendering nothing and evaluating nothing; `gendrake.ActorAspectOf` from ActorAspectText — presence by
`fold`, `appliedName` and `reference` as DracoAspectOf writes them (own package bare, else package words; an unnamed message type
no line), bodies at level 2 through `elementText`; DrakeTarget and GenDrake list them; `Drake.emit` delegates through
`private[draco] actorAspectText(td)`, the block's local `reference` and the callerless `typeRef` removed. Four JSON re-parsed, four
Scala regenerated on a settled overlay, the emitter recompiled; `DrakeCLI check` drift 0 over main and mods. ActorAspectTest (5):
layout and elisions; role as presence; the message type bare and qualified through the live emitter and back; all 15 corpus actor
sections over 188 definitions; the running Emit rule. Two corpus types: parse scope 115 + 73, surface losses 0 across 188 types,
115 aspect sections, GenDrake 113 of 113. REJECTED: actor, `uses` and codec together. FIRST DIRECT RUN 1156 / 2, one cause: ActorAspectOf.scala was generated before ActorAspectText's class was compiled into the overlay and spelled the reference bare where the gate spells `draco.draketarget.ActorAspectText` in full; the engine's `shadowedByHost` consults the classpath (`Class.forName`), confirmed both ways with the CLI — regenerate a referrer of a new definition AFTER compiling that definition (now a DRACO.md §6 gotcha; also the real cause of the Editor line of November 9). Focused rerun 209 / 0. SECOND DIRECT RUN: 1158 tests / 82 suites including discovery, zero failed, canceled, ignored, pending or aborted, green; every headline as expected — example gate 48 / 0, surface losses 0 across 188 types, parse scope 115 + 73 with 0 held back, host text 21 leaves of 2149 in the 11 named seams and 0 outside, scenario 23 clean, GenDrake 113 of 113, the domain and draco aspect probes 115 complete, `RuleAspect over the corpus: 188 definitions, 24 rule sections`, `ActorAspect over the corpus: 188 definitions, 15 actor sections reproduced, 0 skipped`, the PON pair 42 and 7 as suspended, the rete finding categorical; the fifteen new tests are ActorAspectTest's five and the five per-type gates of each new definition; the tree afterwards held the intended changes only. DEV'S GATE: 1158 tests on November 10, zero failed, canceled, ignored or pending, every headline matching, green on its first run. Committed by Dev as 8b127aa and pushed. Record
`git-record-2026-11-10-1120`.

## The record and the codec section are definition-backed; no section layout is left in the emitter (accepted and built, November 10; direct run 1183 / 83; Dev's gate 1183 / 82, committed as c70d6b8, pushed)

Dev accepted: "Accepted, build it", the record's pair named UsesText / UsesOf as proposed. OWNING ISSUE: GitHub #11, open.
MEASURED: `uses` three emitter lines over 81 definitions; codec payload four lines over four definitions (TypeName, DracoAspect,
TypeElement, TypeParameter) with two validity checks and the discriminator refusal. BUILT: `draketarget.UsesText` (`identities`,
the bracketed list, nothing when empty) and `gendrake.UsesOf` (package words then the name); `draketarget.CodecAspectText`
(`fields`, `codec` / `payload [a, b]`) and `gendrake.CodecAspectOf` (the definition's payload); DrakeTarget and GenDrake list the
four; `Drake.emit` delegates through `private[draco] usesText` and `codecAspectText` and keeps its guards. GENERATION ORDER per the
§6 gotcha: layout types generated and compiled first, referrers after; all six files stable on regeneration. `DrakeCLI check` drift
0 over main and mods. UsesAndCodecTest (5): the two layouts; a definition through the live emitter and back with the three guards
still refusing; all 85 records and 4 codec sections over 192 definitions; the running Emit rule. Four corpus types: parse scope
119 + 73, surface losses 0 across 192 types, 119 aspect sections, GenDrake 117 of 117. REJECTED: guards inside the transforms;
`ResolutionRecordText`. DIRECT RUN: 1183 tests / 83 suites including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run; every headline as expected — example gate 48 / 0, surface losses 0 across 192 types, parse scope 119 + 73 with 0 held back, host text 21 leaves of 2186 in the 11 named seams and 0 outside, scenario 23 clean, GenDrake 117 of 117, the domain and draco aspect probes 119 complete, rule 24 and actor 15 sections over 192 definitions, `Uses over the corpus: 192 definitions, 85 records reproduced, 0 skipped`, `CodecAspect over the corpus: 192 definitions, 4 codec sections reproduced, 0 skipped`, the PON pair 42 and 7 as suspended, the rete finding categorical; the twenty-five new tests are UsesAndCodecTest's five and the five per-type gates of each of the four new definitions; the tree afterwards held the intended changes only. DEV'S GATE: 1183 tests on November 10, zero failed, canceled, ignored or pending, every headline matching, green on its first run. Committed by Dev as c70d6b8 and pushed. Record `git-record-2026-11-10-1221`.

## Next task as proposed on November 4 — the issue-tracker reconciliation audit (HISTORICAL: executed by Codex without the correction step, see the current checkpoint)

**How it arose (November 4).** Before pasting the 48f466d gate, Dev wrote: "there has been an absence
of visibility of interactions with the project issue tracking, and it concerns me that what we are
working on might not be associated with an important issue, or, if it is, there may be more important
issues that require finding more important tasks for us to work on." Claude Code read the tracker
(read-only `gh`) and recommended a reconciliation audit; Dev answered "Correct it first", then directed
this handoff instead of the audit, "including the description of the audit task as part of the hand-off".
The receiving session restates the task as ONE recommendation and builds only what Dev accepts.

**What the tracker says today (measured November 4, read-only).** 34 open issues at
github.com/ejb816/nexonix/issues. The last human interaction was a comment on #51 on August 26 and an
edit of #63 on August 28; every event since is an automatic commit reference (#51 on September 8,
October 19, 21 and 22; #61 on September 11 and 16), none after October 22. The one `priority-next`
label sits on #51 (type expressions), whose open questions DRACO.md records as CLOSED by the October
21–22 session (two-stage loading, the unit type, HostBindings, the swept corpus with `uses` records,
formals in the aspect); it is still open. #62 ("Report-only tests should assert their expected
numbers", 2026-08-13) is the issue the November assertion thread answers: of the six report-only tests
it lists, four are now assertions at zero (example map 7be1497, surface losses b321df6, mods pending
surfaces 1cfc38f, held-back types 48f466d), and the two PON reports stay measurements under Dev's
suspension; only the last two records cite it. #55 (three legacy `"Seq()"` values) is done in fact — no
definition under src/main or src/mods carries the string; the one hit is drake.dlt's prose. Everything
else is unexamined. Memory and this file hold model-authored readings of several others (#11 Generator
domain complete 2026-07-21; #58 `case` body element complete 2026-09-07; #42 actor emission; #43 codec
payload emission 2026-10-18 with the discriminator still unsupported; #12 Dreams, the October Editor
work) — readings to re-examine against the tree, not findings.

**The task as recommended.** A read-only reconciliation of all 34 open issues against the tree,
delivered IN CONVERSATION as one batched, prioritized list: for each issue, whether the tree has done
it, done part of it, or not touched it, with the commit hash, DRACO.md paragraph or source path as
evidence; which recent increments should have cited which issue; and which open issues are candidates
for Dev's next priority. Sources: `gh issue view N --repo ejb816/nexonix` for bodies and comments
(the open set is listed above by number: 63 62 61 59 58 57 55 51 50 49 47 46 43 42 41 36 35 34 33 25
23 22 21 20 19 16 15 14 13 12 11 5), DRACO.md §3 and its dated paragraphs, the git records, and the
code. Rules: nothing filed, closed, commented or relabeled until Dev says so — commenting on an
existing issue needs no approval under DRACO.md §1, but after a two-month silence Dev decides the
first touch; existing issue prose is prior model reasoning, not Dev's specification (§1); the audit
reports, Dev prioritizes. Rejected when recommended: picking the next task from the tracker as it
stands, since its one priority label points at closed work. Dev's "Correct it first" was not
elaborated before the handoff was requested: the receiving session should expect a correction to the
scope (all 34 or a subset), the delivery (conversation or a project document) or what may be done to
the tracker, and ask for it in one sentence before restating.

**Also in view, none recommended.** `domains/sentient/EgoActor.scala`, the one definition-less actor in
the example tree (unreferenced but by a test comment and a review-ledger row; a June stub with the
retired Actor suffix) — define under a role name or remove, Dev's decision. The journal gap below.
Deferred, neither accepted: activation (Codex's recommendation of 2026-10-15) and the .dcon migration.
PON is suspended. Inventory is context, not authorization.

**PON is suspended (Dev, November 3).** The PON pair (PonCorpusTest's 42 discrepancies and 7 canonical differences) was measured
after f4b57ae — 19 parse failures and 23 wrong values over a frozen 2026-06-27 document, 2 missing and 5 differing canonicals, both
counts constant since 87a2bb9 — and the recommendation to assert them per category and retire the rows was WITHDRAWN on Dev's
instruction: the PON work will eventually be contained in the native `draco.primes.Primes` domain; it sits in `domains.pon.Pon`
today, and development is suspended on all PON activity until Dev explicitly resumes it for that integration. Nothing under
`domains/pon` (suite, evaluator, corpus) is to be touched; its two rows stay in the DRACO.md table as measurements. The
measurement is recorded in DRACO.md's dated paragraph so it need not be repeated.

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
- PON development is SUSPENDED (Dev, November 3) until Dev explicitly resumes it for integration into
  `draco.primes.Primes`: do not assert, re-baseline or edit PonCorpusTest, `domains.pon.Pon` or the prime-ordinal corpus.
- Activation and .dcon migration remain deferred. No new transforms, geodesy redesign,
  Haskell target, or wholesale example sweep is authorized. Existing issue prose is prior
  model reasoning, not Dev's specification. Persistence belongs to the consuming project;
  transport choices remain provisional.

## Current verification evidence

Dev's full sbt outputs for the four increments were pasted into the Claude Code session
(October 26, 26, 29 and 29): 936, 937, 938 then 942 tests, zero failed/canceled/ignored/pending,
every headline below matching, file-only pending mods actors 0. Each was preceded by a direct
run of **every discovered suite** (936, 937, 938 / 73 and 942 / 74 including Runner discovery;
sbt reports one fewer): all
main/mods/domain and test sources compiled into fresh class roots under target/scala-2.13
(`nc-classes`, `nc-test-classes`) with current main/mods/test resources overlaid and the hidden
root anchors removed; `git status` afterwards showed only the intended changes. The Output
increment's first direct run failed its lazy-capture test through the test's own strict helper
parameter; the second run was green.

| Headline | Verified at the Output tree (and at 112f277, 9779e39, e241378, a8b5447) |
|---|---|
| DomainAspect | 110 complete, 1 parameterized included, 0 skipped |
| DracoAspect | 110 complete, 0 frozen baselines, 0 skipped |
| GenDrake | 108/108, 0 missing, 0 extra |
| Example generation | 48 match, 0 differ, 0 error, 0 missing / 48 — ASSERTED since October 31 (39 / 9 before the ConsumeReport trios and nine companions, 37 / 11 before the coordinate companions, 36 / 12 before the Output repair) |
| Surface losses (ASSERTED zero since 2026-11-02) | 0 fields across 183 types (158 before the widened mods scope, 147 before the sentient surfaces, 143 before the ConsumeReport surfaces, 141 before the coordinate surfaces, 137 before the Intent surfaces, 134 before the sibling surfaces, 133 before LocationReport's) |
| Parse scope (held-back ASSERTED zero over both corpora since November 4) | 110 draco + 73 mods, 0 held back (48 before the widened mods scope, 37 before the sentient surfaces, 33 before the ConsumeReport surfaces, 31 before the coordinate surfaces, 27 before the Intent surfaces, 24 before the sibling surfaces, 23 before LocationReport's) |
| PON (SUSPENDED by Dev, November 3; report-only, not to be asserted) | 80 numbers, 550 expressions, 42 discrepancies (19 parse, 23 value, 0 sequence; constant since 87a2bb9) |
| Canonical PON (same) | 80 numbers, 7 differ (2 missing, 5 differing form) |
| Scenario (ASSERTED since 2026-11-03) | 23 files, 0 rejected, 0 drifted, 23 clean |
| Forest | 1 of 2 ash alarms crossed to birch |
| Subtype visibility | CO-DECLARATION DROPS THE FACT |
| Pending mods definitions (ASSERTED since November 3: a missing mods surface fails its type's test by path; actors only before November 2) | 0 — no file-only line remains |

The example sweep's direct run (October 31, record 2026-10-31-1436): 983 / 78 including discovery, every
row above unchanged; Dev's full gate 983 / 77 on October 31, every row matching. The sentient surfaces' direct run
(October 31, record 2026-10-31-1938): first run 1005 tests / 78 including discovery, 1004 passed, ONE FAILED —
ExampleLoadingTest's all-48 resolution, `Ambiguous type 'Distance': domains.sentient.Distance, draco.base.Distance` from
Percept's and Waypoint's now-treed derivation argument; second run, after the accepted correction spelling that name in
full, 1005 / 78, zero failed or aborted, every row above as stated, pending mods actors 0. Dev's full gate 1005 / 77 on
October 31, green on its first run, every row matching, pending mods actors 0; committed and pushed as 5f0e023. The
anchor-parameter rule's direct run (November 1, record 2026-11-01-0831): 1006 / 78 including discovery, zero failed or
aborted, every row above unchanged, pending mods actors 0. Dev's full gate 1006 / 77 on November 1, green on its first
run, every row matching, pending mods actors 0; committed and pushed as ed57aa9. The compact fixpoint assertion's direct
run (November 1, record 2026-11-01-1228): 1007 / 78 including discovery, zero failed or aborted, green on its first run, every row
above unchanged, pending mods actors 0. Dev's full gate 1007 / 77 on November 1, green on its first run, every row
matching, pending mods actors 0; committed and pushed as a31e49b. The mods surface gate's direct run (November 2, record
2026-11-02-0907): 1068 / 78 including discovery, zero failed or aborted, green on its first run, every row above unchanged
but the relabeled file-only row, pending mods definitions 0. Dev's full gate 1068 / 77 on November 2, green on its first
run, every row matching, pending mods definitions 0; committed and pushed as 8746ecd. The widened parse scope's direct run
(November 2, record 2026-11-02-1202): 1118 / 78 including discovery, zero failed or aborted, green on its first run, the
surface-loss and parse-scope rows moved as stated above, every other row unchanged, pending mods definitions 0. Dev's full
gate 1118 / 77 on November 2, green on its first run, every row matching, pending mods definitions 0; committed and pushed as a4ff2c4. The removed Output hold-out's direct run
(November 2, record 2026-11-02-1433): 1119 / 78 including discovery, zero failed or aborted, green on its first run, every row
unchanged, pending mods definitions 0. Dev's full gate 1119 / 77 on November 2, green on its first run, every row matching,
pending mods definitions 0; committed and pushed as 6a15754. The asserted surface-loss count's direct run (November 2, record
2026-11-02-1451): 1119 / 78 including discovery, zero failed or aborted, green on its first run, every row unchanged, the
surface-loss row now an assertion, pending mods definitions 0; Dev's full gate 1119 / 77 on November 2, every headline matching,
pending mods definitions 0; committed and pushed as b321df6. The asserted scenario round-trip's direct run (November 3, record
2026-11-03-0942): 1119 / 78 including discovery, zero failed or aborted, green on its first run, every row unchanged, the scenario
row now an assertion, pending mods definitions 0; Dev's full gate 1119 / 77 on November 3, every headline matching, pending mods
definitions 0; committed and pushed as f4b57ae. The mods missing-surface gate's direct run (November 3, record 2026-11-03-1700):
1118 / 78 including discovery, zero failed or aborted, green on its first run, every row unchanged, DrakeGenTest 184 to 183, no
file-only line; Dev's full gate 1118 / 77 on November 3, every headline matching; committed and pushed as 1cfc38f on November 4. The asserted
parse-scope hold-back's direct run (November 4, record 2026-11-04-1414): 1118 / 78 including discovery, zero failed, canceled, ignored, pending or aborted, green on its first run, every headline unchanged, every suite's count identical to the 1118 baseline; Dev's full gate 1118 / 77 on November 4, every headline matching; committed and pushed as 48f466d on November 4.

Do not infer the last row from console output. The example map is a gate since October 31
(ExampleDomainsGenTest asserts zero differ, error and missing); individual repaired types also
have exact projection and behavior gates.

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
| 7495539 | Documentation handoff: chapters 105–112, audit, handoff files | — | 2026-10-26-1253 |
| e241378 | Named construction spells its head as authored; NamedConstructionTest (Claude Code) | 936 / 72 | 2026-10-26-1449 |
| 9779e39 | Dyn result intercepted as a named construction; bare slot type resolves; Observable `geocentric:` (Claude Code) | 937 / 72 | 2026-10-26-1534 |
| 112f277 | Construction factory lookup keyed on the applied head; parent-typed slot case (Claude Code) | 938 / 72 | 2026-10-29-0755 |
| c5b8089 | Terrestrial Output's Location branch in its definition; OutputProjectionTest (Claude Code) | 942 / 73 | 2026-10-29-1640 |
| e138f04 | LocationReport json factory; both terrestrial sites construct through it; LocationReportTest (Claude Code) | 947 / 74 | 2026-10-30-1038 |
| 8540341 | PositionReport, FixReport, EphemerisReport json factories; OriginateReports construct through them; ReportFactoryTest (Claude Code) | 956 / 75 | 2026-10-30-1113 |
| 3a3cbfc | FlightIntent, VoyageIntent, LaunchIntent, MarchIntent json factories; eight test sites construct through them; IntentFactoryTest (Claude Code) | 967 / 76 | 2026-10-30-2014 |
| 23333f3 | Position and Location drake surfaces and generated companions with inferred codecs; CoordinateProjectionTest (Claude Code) | 974 / 77 | 2026-10-31-0842 |
| 7be1497 | Four ConsumeReport drake surfaces with JSON re-canonicalized; nine companions the generator's output; ExampleDomainsGenTest a gate (Claude Code) | 982 / 77 | 2026-10-31-1105 |
| ded1b5b | Twelve example trios in the compact form with `uses` records (surfaces re-emitted, JSON re-canonicalized, Scala regenerated); AssemblyValidator compares message types by identity; WorldAssemblyTest +1 (Claude Code) | 983 / 77 | 2026-10-31-1436 |
| 5f0e023 | Eleven sentient drake surfaces, five tuple derivations re-canonicalized, Percept and Waypoint spelling Distance in full, eleven companions the generator's output (Claude Code) | direct run 1005 / 78 (first run 1004, corrected); Dev's gate 1005 / 77 on October 31 | 2026-10-31-1938 |
| ed57aa9 | The anchor-parameter bare form takes `short`'s uniqueness rule; no definition or committed Scala change; ResolutionRecordTest +1 (Claude Code) | direct run 1006 / 78; Dev's gate 1006 / 77 on November 1 | 2026-11-01-0831 |
| a31e49b | The whole corpus asserted a fixpoint of the compact form over both resource roots; ResolutionRecordTest +1, no engine or corpus change (Claude Code) | direct run 1007 / 78; Dev's gate 1007 / 77 on November 1 | 2026-11-01-1228 |
| 8746ecd | DrakeGenTest's mods gate widened to every mods definition and its headline relabeled; `Yaml.drake` the emitter's two lines; no engine, JSON or Scala change (Claude Code) | direct run 1068 / 78; Dev's gate 1068 / 77 on November 2 | 2026-11-02-0907 |
| a4ff2c4 | DrakeParseTest's mods walk widened to the whole mods tree, three comments reworded; two table rows moved; no engine, definition or Scala change (Claude Code) | direct run 1118 / 78; Dev gate 1118 / 77 on November 2 | 2026-11-02-1202 |
| 6a15754 | DrakeParseTest's `inlineTupleArgument` hold-out removed, Output under the definition gate; no engine, definition or Scala change (Claude Code) | direct run 1119 / 78; Dev gate 1119 / 77 on November 2 | 2026-11-02-1433 |
| b321df6 | DrakeParseTest's surface-loss measurement asserts zero and its DRACO.md table row leaves; negative probe 380 / 1 on a scratch tree; no engine, definition or Scala change (Claude Code) | direct run 1119 / 78; Dev gate 1119 / 77 on November 2 | 2026-11-02-1451 |
| f4b57ae | ScenarioDrakeTest's round-trip asserts, naming each rejected or drifted scenario surface, its class doc corrected and its DRACO.md table row gone; negative probe 1 rejected / 1 drifted on a scratch tree; no engine, definition or Scala change (Claude Code) | direct run 1119 / 78; Dev gate 1119 / 77 on November 3 | 2026-11-03-0942 |
| 1cfc38f | DrakeGenTest's mods walk takes the main walk's form, a missing mods surface failing its type's test by path, the report-only file-only pending count retired and its DRACO.md table row gone; negative probe 182 / 1 on a scratch tree; PON suspension recorded; no engine, definition or Scala change (Claude Code) | direct run 1118 / 78; Dev gate 1118 / 77 on November 3 | 2026-11-03-1700 |
| 48f466d | DrakeParseTest's held-back test asserts zero over both corpora, naming each held-back definition, its DRACO.md table row gone; negative probe 376 / 1 on a scratch tree; no test count, engine, definition or Scala change (Claude Code) | direct run 1118 / 78; Dev gate 1118 / 77 on November 4 | 2026-11-04-1414 |

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
  bindings in its case branch and a positional Location call. No generator change was made then;
  the three named-construction repairs of October 26–27 are the generator changes since.
- Terrestrial Output converts only Location, as a `case` branch in its definition, into a
  LocationReport whose payload keys are type, latitude, longitude and elevationMetres; other
  World messages are ignored, the consumer is captured lazily, signals are inert. The report is
  constructed as host text because LocationReport has no factory.
- A named-argument construction's head is spelled as authored or as the scope in effect spells
  it, exactly as a positional call is. Only NAMED-argument applications are intercepted, and only
  when the applied HEAD — a name leaf, dotted, or bare and resolved against the definition in
  effect's package — loads a definition with a factory; the slot type plays no part, and
  everything else renders as before. A dyn's result (body `fix value` and leaf) is intercepted
  the same way, with the plain expression as its fallback.
- Scoped canonical generation and TypeName formal/actual separation remain as documented in
  DRACO.md and chapters 100–104. Rebuilds must preserve uses and typeParameters. Companions
  load unscoped; recorded resolution is applied during generation. The examples were swept on
  October 31 only after a probe showed scoped and unscoped generation byte-identical for all 48.
- `AssemblyValidator` compares a consumer parameter's message type with the actor's by IDENTITY
  (name and package, parameters free): each name is resolved through its own definition's record
  (`NameResolution.recorded`), a bare result completed with that definition's package — the compact
  form's convention — and nothing is loaded or discovered. Any further consumer of the type
  vocabulary must read names the same way; text comparison broke at the unit type and again at
  the example sweep.

The 11 remaining example differences after the Output repair are:
aerial/Aerial, aerial/Input, aerial/Position; ethereal/ConsumeReport, ethereal/Ethereal;
marine/ConsumeReport, marine/Marine; terrestrial/ConsumeReport, terrestrial/Location,
terrestrial/Terrestrial; world/World. These have not been individually read for more than
spelling; the audit noted companion/import conventions and inferred codecs on Position and
Location. Inventory is context, not repair authorization.

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
   ScenarioDrakeTest and ScenarioGenTest instead read `src/test/resources/scenario` relative to the
   working directory, so a probe of them against altered resources runs from a scratch project tree.
   Exclude old test/classes from the compiler classpath to avoid silently reusing stale sources.
3. Overlay src/main/resources, src/mods/resources **and src/test/resources** into the dedicated
   main class root. Exclude its root .json/.drake anchors as sbt does; MinimalTypeTest relies on
   that behavior. Keep resources unique across loader roots to avoid DefinitionPath ambiguity.
4. Compile all Scala under src/main/scala, src/mods/scala/draco and src/mods/scala/domains via
   `java -Xmx3g -cp <jars> scala.tools.nsc.Main -classpath <classes>:<jars> -d <classes> @<sources>`.
   Then compile all src/test/scala into the dedicated test directory using the same classpath.
5. Run `java -Xmx3g -cp <classes>:<jars> org.scalatest.tools.Runner -o -R <test-classes>`.
   Put test classes on Runner's runpath, not the JVM classpath; this matters for isolated runtime
   compilation/class loading, and for the loader roots: the test class directory carries the test
   resources, so on the JVM classpath it is a second root for every scenario definition and
   ScenarioGenTest's three loading tests fail on `DefinitionPath`'s unique-root requirement (seen on
   November 9, 1115 / 3, a harness error). Capture stdout. Existing ZeroMQ tests need local sockets and may
   require sandbox permission. Run every discovered suite, not just a remembered subset.
6. Check all headlines above and git status/diff afterwards (no file-only line remains since November 3).
   Some tests generate files into source trees; PrimesRulesTest must remain scoped. Never
   revert such output blindly, especially when preserving another agent's edits.

Direct verification supplements, never replaces, Dev's full sbt gate. Hand over the established
console grep plus file-only grep only after the direct run passes and headlines are understood.

## Journal coverage and handoff integrity

DRACO.md and README.md markers reach **chapter 112**, whose closing commit is 7495539 (October 26,
the Codex documentation handoff). Chapters 105–112 and `sources/codex-2026-10-22-to-26-audit.md`
preserve the Codex session of October 22–26 from original records (85 messages, both compactions
survived); chapter 105 renders the previous Claude handoff close from
`sources/claude-2026-10-22-handoff-close.jsonl`. Chapter 112 preserves Dev's correction of the
premature "handoff ready" claim of October 26.

**Covered on November 10: chapters 113–127**, the thirty-nine commits e241378 … c70d6b8 and the handoff commit 043f578, from
the four sources below. Extracts and audits: `sources/claude-2026-10-26-to-11-03`, `sources/claude-2026-11-03-to-06`,
`sources/claude-2026-11-07-to-10`, `sources/codex-2026-11-04-to-05` (each a `.jsonl` and an `-audit.md`). That session is covered by chapters 128–129
(`sources/claude-2026-11-10-session-2`), written later the same day; uncovered is only what follows Dev's request for them. The paragraphs below are the previous
session's description of the sources, kept as written. Sources, on this machine only: the session records under
`/Users/edwardburke/.claude/projects/-Users-edwardburke-IdeaProjects-nexonix/`, identified by FIRST timestamp (UTC), not mtime:

| record | first .. last message (UTC) | size | what it holds |
|---|---|---|---|
| `f12792d4-b3c1-4ac3-b8f9-c2853199c07f.jsonl` | 2026-10-26 18:17 .. 2026-11-03 20:54 | 47 MB | Claude Code: e241378 … f4b57ae (named construction, json factories, example map and sweep, sentient surfaces, the first assertions) |
| `9e3f32b7-12cf-465c-9205-6fe4da31855e.jsonl` | 2026-11-03 20:54 .. 2026-11-07 02:10 | 8 MB | Claude Code: the PON suspension, 1cfc38f, 48f466d, the November 4 handoff, the November 6 handoff after the aborted Codex one (6682c41, 40d02df) |
| `1cf64c57-6ed5-4b94-9b2e-faf865e8fdc0.jsonl` | 2026-11-07 02:10 .. 2026-11-10 | 13 MB | Claude Code: the reconciliation verified (7d4ae0a) and the nine increments d026cd3 … c70d6b8, this handoff |
| `/Users/edwardburke/.codex/sessions/2026/11/04/` and `…/11/05/` (several `rollout-*.jsonl`) | 2026-11-04 15:44 local onward | — | Codex: the tracker reconciliation (2b9cfd5); not opened by Claude Code, only listed |

All three Claude records were compacted (compaction markers are present in each); a compaction summary is working context,
never quotation evidence.

Procedure, as chapters 93–94 and 105–112 followed it (DRACO.md §7, *Transcript integrity at handoff*, and the journal's own
Introduction): recover the ORIGINAL message records, not summaries; preserve a bounded project-local extract under
`draco-dev-journal/sources/` with source identity, sequence, timestamps, text hashes and explicit exclusions, and an audit
beside it; Dev's prompts verbatim, final replies near-verbatim, tool results excluded; report user and reply counts and
paired exchanges separately; label Claude and Codex distinctly; preserve corrections (this session's include the harness
error of d026cd3, the eleven failures of 995fade, the mislabelled "mid-update" cause corrected at 8b127aa, and Dev's
question about the missing pushes). Do not skip the older sessions to reach the newer ones. `draco-dev-journal/tools/`
holds the earlier extraction scripts. The git records `git-record-2026-10-26-1449` through `git-record-2026-11-10-1237`
and the dated DRACO.md paragraphs carry each increment's measurements and Dev's acceptance quotes, and support
record-backed Status summaries where dialogue is not recovered. Update the chapter index when chapters are added; advance
the `draco-docs-synced-through` markers only after the new chapters are reviewed against DRACO.md and README.md.
**Dev named the journal as the next session's task on November 10; the first step there is to confirm the scope with Dev
(which sessions, how many chapters) before writing.**

Checks at this handoff (November 10): `git status`/`git log` inspected; origin/main read with `git ls-remote` and equal to
local main at c70d6b8; DRACO.md, this file, the resume prompt and CHANGELOG aligned with c70d6b8; the journal directory and
the session-record directories listed, not opened beyond their timestamps; no runtime, test, sbt, commit, push, issue or
tracker change by the agent in preparing it. No implementation gate is claimed beyond Dev's 1183 / 82 of November 10.
