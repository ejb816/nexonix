# Trends

One column per review. A number's source is the check script section (`checks/review_checks.py`,
"§n") or the review/record it was quoted from. Where a value is a *claim* in prose rather than a
measurement, the cell says so. Both seed columns were produced by running the script at the
reviewed commit (`git worktree add <dir> <commit>`), so the next review can extend the table the
same way.

Granularity warning: the doc-drift ledger got finer between the two reviews (file-level items on
08-15, line-level on 08-28), so "doc items open" rising is not a regression. The retired-name
count is the comparable series.

| Series | Source | 2026-08-15 @ `db87dbd` | 2026-08-28 @ `0af56fd` |
|---|---|---|---|
| Commit reviewed (date) | §1 | `db87dbd` (2026-08-13) | `0af56fd` (2026-08-27) |
| Commits since previous review | `git log` | — | 10 |
| Days since last release, at review date | §1 / tags | 73 (alpha.5, 06-03) | 3 (alpha.6 cut 08-25) |
| **Corpus** | | | |
| Definition JSON (all `src/*/resources`) | §2 | 132 | 132 |
| `.drake` files (`src/` total) | §2 | 91 | 113 |
| Scala files (`src/` total) | §2 | 204 | 206 |
| Drake-first corpus (`scenario/` drake / JSON) | §2 | 0 / 0 | 22 / 0 |
| `extends App` companions, `src/main/scala/draco` whole tree (occurrences / files) | §5 | 81 / 69 | 81 / 69 |
| **Gates and tests** | | | |
| Full-suite count, last *confirmed* run | journal / record | 525 (`git-record-2026-08-13-1030`) | 526 at `05b14bd` (ch. 74:532); **527 expected** at HEAD, unconfirmed |
| Test files / suites | §9 | 45 / 38 | 47 / 40 |
| Suites ending a test in bare `succeed` (report-only) | §9 | 2 | 4 |
| Suites with `test(...)` and no assertion idiom | §9 | 9 | 11 |
| Tests writing tracked paths | §10 | 4 | 4 |
| `DracoGenTest.excluded` / `DrakeGenTest.authoredAhead` / `inlineTupleArgument` | §11 | 13 / 2 / 1 | 13 / 2 / 1 |
| Loss-report fields (record claim) | records | 16 across 90 types | 15 across 91 (12 #61, 3 #55) |
| Loss bucket `reference package` | records | 1 | 0 (structurally — gate 2 now forbids it) |
| CI | §14 | tag-only | tag-only (repaired `5b48f0c`, never executed) |
| **Metamodel state** | | | |
| `actorAspect.messageType` carriers | §7 | 1 | 11 |
| Definitions deriving `Actor` / `ActorType` / `ExtensibleBehavior` | §7 | 13 | 3 |
| `messageType` carriers with all bodies empty (D-19 latent) | §7 | 0 | 0 |
| Package-less (foreign) derivation references | §8 | 1 | 1 |
| Definitions spelling `DracoType` explicitly | §8 | 2 | 0 |
| Top-level `codecAspect` carriers | §3 | 0 | 0 |
| `Draco.json` `elementTypeNames` missing / order divergence | §4 | `Local` / index 10 | `Local` / index 10 |
| `override val` in App-companion files / in Generator templates | §6 | 9 / 3 | 9 / 3 |
| Renderer lacking a corpus operator | §13 | `SourceContract` lacks `\|\|` | `SourceContract` lacks `\|\|` |
| **Ledger** | | | |
| Defects tracked (open / half / fixed) | ledger D | 18 (18 / 0 / 0) | 24 (22 / 1 / 1) |
| Design findings (open / half / closed) | ledger F | 11 (11 / 0 / 0) | 12 (7 / 2 / 3) |
| Doc-drift items open (see granularity warning) | ledger X | 10 | 16 |
| Retired-name hits: `DRACO.md` / `README.md` / `AGENTS.md` / `HOLARCHY.md` | §16 | 36 / 20 / 53 / 12 | 13 (all in the Retired list) / 0 / 53 / 12 |
| Version strings in play (`build.sbt`, README, `CLI.scala`) | §12 | alpha.5 / alpha.1-3 / alpha.1 | alpha.6 / alpha.5 / alpha.1 |
| Previous review's recommendations: taken / mostly / partial / not started | ledger R | — | 1 / 1 / 3 / 5 (of 10) |
| **Process** | | | |
| Journal lag behind tree | §17 | ~1 week (ch. 73 at 08-10) | 0 commits, 1 exchange (ch. 74) |
| Git-records stating gate scope (of records since previous review) | records | not consistently | 10 / 10 |
| CHANGELOG entries per git-record (since rule landed 08-17) | CHANGELOG | n/a (rule did not exist) | 3 / 3 after `9f9bebd` self-repair; 2 misses before |
| Open GitHub issues | GitHub | not fetched | not fetched (unreachable from session) |
