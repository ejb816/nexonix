# Decisions

Two lists. The first is the questions the reviews asked Dev (§8 of each review), with what has
happened to each. The second is decisions Dev has taken that the reviews depend on, with where the
tree records them — so a review does not re-ask a question that is already answered, and does not
cite a model's proposal as Dev's decision.

The repo's rule applies here too (`DRACO.md:70-73`): model-authored prose is not Dev's intent. A
row moves to *answered* only on Dev's word — in this session, in the journal, or in a commit he
wrote — never because the work happened to go one way.

---

## Questions asked of Dev

| ID | Asked | Question | Status | Answer / where recorded |
|---|---|---|---|---|
| Q-01 | 08-15 | Is mods now *the* engine tier, or still "speculative outer layers"? Decides whether `DomainBuilder.define` is promoted into core `TypeDictionary`, and how the docs describe the split | **open** | Re-asked 08-28. Ch. 73 folds #47 and #41 into the promotion, so two backlog items wait on it |
| Q-02 | 08-15 | Should the presence model and declared-codec migration stay deferred to Generator[L], or land in the procedural Generator? | **open** | Re-asked 08-28 at 4½ months. Costs: two `authoredAhead` exclusions, an empty fifth aspect, a `drake.dlt` codec template `CodecAspect` cannot satisfy |
| Q-03 | 08-15 | #61: build operator trees in the parser now, or keep drake emitter-first and add only the lint? | **narrowed** | alpha.6 release notes state parse is a measurement tool (Known Limitations, tracked as #61). Neither trees nor lint has landed. 08-28 re-asked narrower: is the lint worth a morning *before* #51's remaining edges, given `scenario/` is a live drake-first corpus? |
| Q-04 | 08-15 | Is an alpha.6 release and a `bin/draco-drake` wrapper worth doing before #51? | **half answered** | alpha.6 shipped (`70ab3f2`, 2026-08-25) — answered by Dev's action. `bin/draco-drake` not built; promoted to a published limitation instead. 08-28 re-asked: worth the hour before #63? |
| Q-05 | 08-28 | `AGENTS.md`: symlink it to `DRACO.md`, or delete it? It is a 16 KB verbatim copy of the pre-rewrite architecture, addressed to Codex, and every record since 08-15 defers it as Dev's call | **open** | |
| Q-06 | 08-28 | The co-declaration finding (F-01) is recorded as a gotcha. Validation rule now, or wait for #63's dispatch grammar to make it unreachable? | **open** | Review's recommendation: rule now (R-11), because a user domain can hit it before #63 lands |

## Decisions Dev has taken that the reviews depend on

| ID | Date | Decision | Recorded at | Bearing on reviews |
|---|---|---|---|---|
| DD-01 | 2026-08-15 | A new GitHub issue requires Dev's explicit approval every time; commenting on an existing issue does not | `DRACO.md:22-26`; `git-record-2026-08-15-1500` "TWO RULES RECONCILED"; ch. 73 | Reviews surface deferred work in prose, never file it. Resolved the DRACO.md/memory conflict the 08-15 review flagged |
| DD-02 | 2026-08-15 | Model-authored prose — issues, memory, review documents — is prior reasoning to re-examine, never Dev's authority or specification | `DRACO.md:70-73` | Applies to these reviews. Never cite a review as the reason a design is settled |
| DD-03 | 2026-08-17 | A CHANGELOG entry is written with each commit's git-record, in the same commit | `CHANGELOG.md:5-17`; `DRACO.md:57-62`; `git-record-2026-08-17-1500` | Reviews check the rule holds (it failed twice, self-repaired in `9f9bebd`, held 3/3 after) |
| DD-04 | 2026-08-25 | **Nominal over structural**: reference slots stay `TypeName`; structure lives in `valueType`; the boundary is drawn by the loader (loadable ⇒ nominal). No shape change to `TypeDefinition`/`derivation` | ch. 74 ("My mistake. I should have explicitly stated my choice of nominal over structural"); comment on #51; `git-record-2026-08-25-1240` | Retires the 08-15 review's risk #2 as a *decided boundary*. The shape-change proposal was the model's, not Dev's — his provenance correction is in ch. 74 |
| DD-05 | 2026-08-25 | Flat `TypeParameter`, nesting unrepresentable — chosen; the `(,)`-product-head and depth-1-as-rule follow-ups are *proposed, not decided* | ch. 74; comment on #51 | Do not describe the follow-ups as decided |
| DD-06 | 2026-08-26 | Actor instances are never message data: an actor's message type is a role parameter, not an inheritance edge; `derivation` holds data-inheritance edges only | `git-record-2026-08-26-1009`; ch. 74 | Basis for the 10-actor migration. `world/Consumer`/`Provider` left as-is *deliberately* (D-24) |
| DD-07 | 2026-08-26 | The actor match-case dispatch grammar (#63) is the next major issue after #51 | ch. 74 (Dev: next highest priority) | Sets the expected next front for the following review |
| DD-08 | 2026-08-27 | Journal authorship: Cowork writes `draco-dev-journal/`; Claude Code does the session work; the two ride in one commit under Cowork-first ordering | `DRACO.md:47-55`; `git-record-2026-08-27-2251` | Reviews note that "journal and work commits are strictly disjoint" no longer holds — deliberately |
| DD-09 | 2026-08-17 | Push/PR CI is Dev's call, not a side effect of a deprecation fix — deferred | `git-record-2026-08-17-1830` | R-04/R-14 "add push/PR CI" is a recommendation to Dev, not a task the pair can take |
| DD-10 | 2026-09-16 | Reviews live in `draco-reviews/` in the repo; ancillary continuity files (this folder) are the primary location for review-to-review state | this session | |
