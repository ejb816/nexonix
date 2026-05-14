# Draco Dev Journal — Chapter 32

**Session date:** May 13, 2026
**Topic:** Execute the GitHub Issues migration prepared at the close of chapter 31. Nine custom labels created at `ejb816/nexonix`, five GitHub default labels dropped, nineteen backlog issues created in deterministic order (so `#1` ends up `priority-next`), the `Imminent Tasks` and `Pending pattern convergence` sections excised from `MEMORY.md`, and the planning memory rewritten as an execution record. No Scala/JSON/YAML touched.

---

## The prompt

The session opened with one line:

> **Dev:** Let's migrate to GitHub Issues in this session, from setup done in previous session.

Everything else in this chapter is the agent executing the plan that chapter 31 had already finalized as `project_github_issues_migration.md`. The plan was deliberately structured so the next session could execute mechanically without re-deliberating — and that's what happened.

---

## Why the migration

The `## Imminent Tasks` list in `MEMORY.md` had reached 18 items, with the natural follow-up tasks from chapters 28–31 ready to land on it. Memory was being used as a backlog. Memory is supposed to hold conventions, feedback, and slowly-evolving facts about the project — not state that changes every session.

Two specific costs were accumulating:

1. **No queryability.** A flat numbered list can't be filtered by category. The chapter-31 finish-pass added several items that overlapped existing entries; the dev had no way to surface those overlaps short of re-reading the whole list each time.
2. **No commit cross-reference.** Closing a memory-list item meant editing the memory file. Closing a GitHub issue takes `closes #N` in a commit message and the link is permanent.

GitHub Issues was the obvious target. The repo (`ejb816/nexonix`) was already public, `gh` was already authed with `repo` scope on the `ejb816` account, Issues was already enabled. The setup overhead was zero — the only work was structuring the migration so the resulting issues were useful, not just a translated list.

---

## Setup state inherited from chapter 31

Chapter 31's finish-pass left a fully prepared plan as a single memory file: `project_github_issues_migration.md`. The plan included:

- **Label set design.** Nine custom labels with colors chosen to be visually distinct: `stage-2e` and `next-feature` purple, `generator` green, `reference-frames` blue, `bootstrap` yellow, `tooling` light-blue, `docs` darker-blue, `cleanup` lavender, `priority-next` bright red. The reasoning: every backlog item slots cleanly into one or two of these axes (track / area / urgency).
- **Default labels to drop.** GitHub seeds new repos with `duplicate`, `good first issue`, `help wanted`, `invalid`, `wontfix`. For a solo project they're noise.
- **19 issue drafts** with titles, bodies, and label assignments. Bodies cite specific memory files so future readers can recover full context.
- **Ordering rule.** Issue `#1` should be `priority-next` (Stage 2e leaf detection proper). GitHub assigns issue numbers in creation order; on an empty repo the first `gh issue create` becomes `#1`. So `priority-next` had to fire first.

This kind of advance preparation is what made the May 13 session mechanical. The dev didn't have to re-decide anything mid-execution.

---

## Execution

### Labels (one batch)

Nine `gh label create` calls chained with `&&` for fail-fast behavior. All succeeded. The `gh label list` verification afterward showed all nine custom labels alongside the eight remaining GitHub defaults — twelve labels after the `enhancement`/`question`/`bug` defaults survive. Then five `gh label delete --yes` calls to drop the unwanted defaults, leaving the final palette: `bug`, `enhancement`, `question` (GitHub defaults preserved as catch-all), plus the nine custom labels.

### Issues (five sub-batches)

Done in batches of four for human-readable output. Each batch chained with `&&` so a failure would surface immediately and not get masked by later successes.

- Batch 1: `#1` alone (priority-next, sequencing-critical)
- Batch 2: `#2`–`#5` (reference-frames cluster)
- Batch 3: `#6`–`#9` (bootstrap + tooling cluster)
- Batch 4: `#10`–`#13` (tooling + next-feature cluster)
- Batch 5: `#14`–`#17` (next-feature + docs + cleanup cluster)
- Batch 6: `#18`–`#19` (final two)

Verification with `gh issue list --json number,title,labels` confirmed all 19 created with intended labels and order. `#1` is `[stage-2e, priority-next] Stage 2e leaf detection proper`.

### MEMORY.md edits

Three surgical Edit operations:

1. Replaced the `project_github_issues_migration.md` pointer line under `## Tooling` with two lines: a live `[GitHub Issues](https://github.com/ejb816/nexonix/issues)` reference plus an EXECUTED-marked pointer to the migration plan file.
2. Removed the `## Pending pattern convergence` section (its single item became issue `#9`).
3. Replaced the entire `## Imminent Tasks` section with a much shorter `## Backlog` section pointing at the live Issues page with a label-key one-liner.

The before/after delta: ~28 lines removed, 2 lines added under Tooling, 2 lines added as the new `## Backlog` section. Net `MEMORY.md` shrinks by ~24 lines.

### Migration memory file rewrite

The plan file (`project_github_issues_migration.md`) had served its purpose. Per the plan's own step 4, the body was replaced with a brief execution record: what got created, what got edited, and a one-liner for future sessions on how to find current work (`gh issue list --label priority-next`).

---

## What didn't happen

No Scala, JSON, YAML, or build files touched. No new commits to draco code. The migration was purely:

- 24 `gh` API calls (9 label creates + 5 label deletes + 19 issue creates + a couple of list verifications)
- 3 edits to `MEMORY.md`
- 1 rewrite of `project_github_issues_migration.md`

Test suite state unchanged from chapter 31's close. The branch had inherited deletions in `src/test/resources/domains/{alpha,bravo,charlie,dataModel,delta}/*.json` staged from chapter 31's example-domain cleanup — those remain staged for the dev's own per-tool IDE-approval workflow and aren't part of this session.

---

## What this enables going forward

The natural pickup is now one command:

```
gh issue list -R ejb816/nexonix --label priority-next
```

Currently that returns issue `#1` — Stage 2e leaf detection proper, with the rule `elementTypeNames.size == 1 && single-name.toLowerCase == namePackage.last`. The foundation for it landed in chapter 28; chapters 29–31 pivoted away to handle `DomainAspect.typeName`, the `DracoType`-as-root shift, and the canonicalization sweep. The path is now clear.

Cross-issue dependencies are documented in issue bodies. For example, `#3` (populate `elementTypeNames` on the 12 transform domains) is blocked on `#4` (Holon/Primal redesign in non-Egocentric frames). `#2` (replace hand-written reference-frame Scalas with generated) wants `#9` (`bin/draco-gen regenerate` mode) to exist first. These could later become GitHub issue dependencies via the projects API, but for 19 issues with a solo dev, narrative in issue bodies is enough.

---

## Lesson — separating planning from execution

Chapter 31 prepared the migration plan as a self-contained memory file with explicit ordering rules, exact `gh` invocations, and pre-drafted issue bodies. Chapter 32 then executed it mechanically without any new design decisions. This worked because the plan really was complete — nothing was deferred to "figure out when we get there."

The pattern generalizes: when a task is largely administrative (file moves, structured creates, batch edits) and well-understood at design time, doing the design in one session and the execution in the next is faster than mixing them. The mixed-mode failure mode is mid-execution re-decisions that contradict prior decisions made minutes earlier. Separating the two means the executing session can be terse and verifiable.

---

## Status at close

- 9 labels live at `ejb816/nexonix`
- 19 issues created, `#1` priority-next
- MEMORY.md `## Backlog` replaces `## Imminent Tasks` + `## Pending pattern convergence`
- `project_github_issues_migration.md` rewritten as execution record
- No code changes
- Test suite untouched since chapter 31 close

Next session picks up from [`#1` Stage 2e leaf detection proper](https://github.com/ejb816/nexonix/issues/1).
