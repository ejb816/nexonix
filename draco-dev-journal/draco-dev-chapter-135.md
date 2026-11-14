# Chapter 135 - The Formal Spelling as a Definition

**Session date:** November 13, 2026
**Topic:** One increment under GitHub #11: the spelling of a formal type parameter becomes the definition `gendrake.FormalOf`, read by the header in DracoAspectOf and by the element renderer, which now receives a method's formals as data (9d1e6cf).

**Provenance:** Original Claude Code session `92b66875-d6a1-4bde-bc3d-a3186065293e`, extract records 11–16; see the [bounded extract](sources/claude-2026-11-13-to-14-session-2-continued.jsonl) and [retrieval audit](sources/claude-2026-11-13-to-14-session-2-continued-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace, the paste wrapper tags, repository-local link destinations and, where a reply carries its own second-level headings, one level of heading depth. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning, intermediate progress and compaction summaries are excluded. Session dates use America/New_York; timestamps are UTC. Historical command blocks are dialogue, not instructions to execute now. The closing Status is a sourced maintenance summary, not dialogue.

---

*(Context, not dialogue — before this answer the app's next-step box asked “Build the formal spelling as the definition gendrake.FormalOf?” with the buttons “Accepted, build it”, “Correct it first”. The question is a tool call at source line 3421, outside the reply text, and is not an extract record.)*

**[Dev]** *2026-11-13T23:29:39.964Z; source line 3434; extract record 11*

Accepted, build it

---

**[Claude]** *2026-11-13T23:35:19.109Z; source line 3549; extract record 12*

The formal spelling is built as accepted and ready for your gate. My direct run of every discovered suite passed 1259 tests / 91 suites including discovery, green on its first run. Your sbt gate has not run yet.

- **`gendrake.FormalOf`:** spells one formal — the name, then the lower bound, then the upper bound, each bound through `TypeFormOf`.
- **Header:** `DracoAspectOf` reads it in place of its local function.
- **Element renderer:** `ElementOf` now takes `formals (TypeElement -> [TypeParameter])` and writes `name(formals)` itself.
- **Emitter:** `methodName` and `elementNameText` are gone from [Drake.scala](../src/mods/scala/draco/Drake.scala); `elementFormals` passes data only, and `refuseElement` still validates.
- **Surfaces unchanged:** `DrakeCLI check` reads drift 0 over main and mods.
- **Generation:** the `DracoAspectOf` call generated cleanly as a bound function. `FormalOf` was compiled before its three referrers, and all four generated files are stable.
- **New suite:** `FormalOfTest` has five tests. The corpus holds 36 formals in 33 lists, 7 of them bounded; my recommendation had counted only the bounded headers and the methods.

One difference, on no input in the corpus: a method's bound used to be spelled through `TypeForm.text` and a header's through the type-form renderer; both now go through `TypeFormOf`.

Please run the gate. The grep has one new term, `Formal over`:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|host text|type leaves|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|RuleAspect over|ActorAspect over|Uses over|CodecAspect over|Definition over|TypeForm over|Expression over|Element over|Formal over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Expected, and what to paste back: the `Tests:` line and every headline.

| headline | expected |
|---|---|
| Tests | 1259 succeeded, 0 failed, canceled, ignored or pending |
| Formal (new) | 36 formals reproduced in 200 definitions (33 lists, 7 bounded), 0 differing |
| Parse scope | 127 draco + 73 mods, 0 held back |
| Surface losses | 0 fields across 200 types |
| DomainAspect / DracoAspect | 127 complete sections each |
| GenDrake runs | 125 of 125 |
| RuleAspect / ActorAspect | 24 rule and 15 actor sections over 200 definitions |
| Uses / CodecAspect | 93 records and 4 codec sections over 200 definitions |
| Definition | 200 surfaces reproduced whole from 536 sections |
| Host text | 21 leaves of 2800 in 11 definitions, 0 outside the seams |
| Type leaves | 1697 in 200 definitions, 0 carrying host type syntax |
| TypeForm | 2195 type forms (498 trees, 1697 leaves), 0 differing |
| Expression | 662 values (477 trees, 185 leaves), 0 differing |
| Element | 1127 elements (1374 lines), 0 differing |
| Example gen map | 48 match, 0 differ |
| Scenario | 23 files, 23 clean |
| PON pair | 42 discrepancies and 7 canonical differences, as suspended |
| Rete | CO-DECLARATION DROPS THE FACT |

The record is `draco-git-record/git-record-2026-11-13-1834`, and the CHANGELOG, DRACO.md, README, the handoff and the resume prompt are updated with your gate marked pending. The baseline stays 1249 / 89 until you paste a green gate; then I will move it and hand over the commit and push blocks. The fold of `a216865` rides with that commit.

Nothing on the tracker was touched; #11 stays open. The journal is current through chapter 133; the `a80be95` and `a216865` exchanges and this increment are the uncovered part.

---

*(Delegated action — Dev ran the full sbt gate: 1259 tests succeeded, 0 failed, 0 canceled, 0 ignored, 0 pending. Reported headlines: DomainAspect over the corpus: 127 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 125 of 125 definitions emitted, 0 missing, 0 not in the corpus; Element over the corpus: 1127 elements reproduced in 200 definitions (1374 lines), 0 differing; Expression over the corpus: 662 values reproduced in 200 definitions (477 trees, 185 leaves), 0 differing; Definition over the corpus: 200 surfaces reproduced whole from 536 sections, 0 skipped; EXAMPLE-DOMAIN GEN MAP: 48 match, 0 differ, 0 error, 0 missing (of 48) — asserted zero differ; details in the log; RuleAspect over the corpus: 200 definitions, 24 rule sections reproduced, 0 skipped; ActorAspect over the corpus: 200 definitions, 15 actor sections reproduced, 0 skipped; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 200 types; Drake.parse scope: 127 draco + 73 mods types in, 0 codec-aspect + 0 drake-less held back; host text in values: 21 leaves of 2800 in 11 definitions, 0 outside the 11 named seams; DracoAspect over the corpus: 127 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; type leaves: 1697 in 200 definitions, 0 carrying host type syntax; Uses over the corpus: 200 definitions, 93 records reproduced, 0 skipped; CodecAspect over the corpus: 200 definitions, 4 codec sections reproduced, 0 skipped; the forest runs: 1 of 2 ash alarms crossed to birch; Formal over the corpus: 36 formals reproduced in 200 definitions (33 lists, 7 bounded), 0 differing; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing; TypeForm over the corpus: 2195 type forms reproduced in 200 definitions (498 trees, 1697 leaves), 0 differing. Source: 2026-11-14T03:04:05.466Z; source line 3556; extract record 13. The full pasted text is retained in the extract.)*

---

**[Claude]** *2026-11-14T03:04:35.284Z; source line 3585; extract record 14*

Your gate is green and matches the direct run on every headline: 1259 tests, zero failed, canceled, ignored or pending, with the new `Formal over` line reading 36 formals, 0 differing. I recorded it in the git record, DRACO.md, the handoff and the resume prompt. The baseline is now **1259 tests / 90 suites**; your paste has no `Suites:` line, so 90 is the direct run's 91 less its discovery wrapper. `git diff --check` is clean.

Commit, path-scoped (21 paths: 5 new, 16 modified, the fold of `a216865` included):

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md draco-git-record/git-record-2026-11-13-1834 src/main/resources/draco/gendrake/FormalOf.drake src/main/resources/draco/gendrake/FormalOf.json src/main/resources/draco/gendrake/DracoAspectOf.drake src/main/resources/draco/gendrake/DracoAspectOf.json src/main/resources/draco/gendrake/ElementOf.drake src/main/resources/draco/gendrake/ElementOf.json src/main/resources/draco/gendrake/GenDrake.drake src/main/resources/draco/gendrake/GenDrake.json src/main/scala/draco/gendrake/FormalOf.scala src/main/scala/draco/gendrake/DracoAspectOf.scala src/main/scala/draco/gendrake/ElementOf.scala src/main/scala/draco/gendrake/GenDrake.scala src/mods/scala/draco/Drake.scala src/test/scala/draco/gendrake/ElementOfTest.scala src/test/scala/draco/gendrake/FormalOfTest.scala) && git add -- "${paths[@]}" && git diff --cached --check && git commit -F draco-git-record/git-record-2026-11-13-1834 -- "${paths[@]}" && git status --short --branch
```

Push:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git push origin main && git status --short --branch
```

Expected: a commit of 21 files, a push from `a216865`, and `## main...origin/main` with nothing below it.

Once the hash is in, I'll fold it into the documents and bring the next related point under #11. Nothing on the tracker was touched. The journal is current through chapter 133; the `a80be95` and `a216865` exchanges and this increment are the uncovered part.

---

*(Delegated action — Dev committed 9d1e6cf, “The spelling of a formal type parameter is definition-backed: gendrake.FormalOf, read by the header and the element renderer (GitHub #11)” (21 files changed, 599 insertions(+), 113 deletions(-)). Pushed to origin/main: a216865..9d1e6cf. Status lines `## main...origin/main [ahead 1]` then `## main...origin/main`. Source: 2026-11-14T04:57:22.999Z; source line 3589; extract record 15. The full pasted text is retained in the extract.)*

---

*(Context, not dialogue — a compaction boundary is at source line 3603, between Dev's commit and push paste and the reply below, which is an original record from after it.)*

**[Claude]** *2026-11-14T04:58:27.669Z; source line 3686; extract record 16*

`9d1e6cf` is committed and pushed (21 files, `a216865..9d1e6cf`, `## main...origin/main`). I folded the hash into DRACO.md, the handoff and the resume prompt; those three edits are uncommitted and ride with the next commit.

### Next point under GitHub #11 (open, stays open)

**Recommendation: remove the three type-spelling dependencies from the section transforms, now that what they pass in is a definition.**

Measured in the tree:

- `DefinitionOf` receives four rendering dependencies and hands them down. Three spell types: `parameterText`, `formText`, `slotText`.
- `formText` is `TypeFormOf(form).value`, already a definition in main.
- `parameterText` is the same thing for a tree and the leaf's text for a string, which is what `TypeFormOf` gives for a leaf too.
- `slotText` is that spelling with a top-level arrow parenthesized. `ElementOf` already spells this rule itself, so it exists twice: once in `ElementOf`, once hand-written in `Drake.scala`.

What I would build:

- **One new definition, `gendrake.SlotOf`:** takes a type form, derives `draketarget.Surface`, and is the single spelling of a value-type slot. `ElementOf` and `DracoAspectOf` read it.
- **Four transforms lose parameters:** `DracoAspectOf`, `DomainAspectOf`, `ActorAspectOf` and `DefinitionOf` call `TypeFormOf` and `SlotOf` directly. `DefinitionOf` becomes `(definition, elementText)`.
- **`Drake.scala`:** `typeParameterSurface` and `typeExpressionSlot` go. `typeSurface` stays as a one-line delegate to `SlotOf`, because DrakeParseTest uses it as a normalizer.
- **Tests:** the three aspect tests that construct the transforms with callbacks are restated, and one of them pins that the form callback is never evaluated, which no longer applies. `SlotOf` adds its five per-type gates and a small focused suite.

Every surface should stay byte-identical, checked with `DrakeCLI check` and the corpus tests as before.

Not probed yet: how `TypeFormOf` spells an absent factory value type, which `DracoAspectOf` tests for emptiness. I will check that first when building and say so if it forces a shape change.

Left out: the fourth dependency, `elementText`. It stays because the element guard and a method's formals still live in mods; those are the two preserved candidates (formals on TypeElement, a failure form for the guards), neither accepted.

Nothing on the tracker was touched. The journal is current through chapter 133; the `a80be95`, `a216865` and `9d1e6cf` exchanges are uncovered. This session has compacted again, so I can write those chapters before the next commit if you want them in it.

---

## Status

9d1e6cf (`gendrake.FormalOf`; DracoAspectOf's header reading it; ElementOf's dependency `formals (TypeElement -> [TypeParameter])`; `methodName` and `elementNameText` leaving Drake.scala for `elementFormals`; 21 files) is committed and pushed by Dev after a gate of 1259 tests, every headline matching the direct run and the new `Formal over the corpus: 36 formals reproduced in 200 definitions (33 lists, 7 bounded), 0 differing` line as expected, green on its first run; the direct run was green on its first run. The baseline is 1259 tests. One compaction boundary falls in this chapter, between the commit paste and its reply, with original records on both sides. Recommended in the chapter's last reply and not yet accepted at its close: removing the three type-spelling dependencies from the section transforms. Not built and not accepted: `typeParameters` on TypeElement; a failure form for the guards. Nothing on the tracker was touched; #11 stays open. Activation and .dcon are deferred; PON is suspended.
