# draco-reviews

Periodic reviews of the DRACO framework as it moves from alpha to beta, and the files that carry
state from one review to the next. The reviews are snapshots; everything else here is the
continuity.

Per `DRACO.md:70-73`, everything in this folder is model-authored prose: prior reasoning to
re-examine, never Dev's intent or a specification. `decisions.md` is the one place where Dev's
own decisions are recorded, each with its source in the tree.

## Reviews

| Review | Commit reviewed | Depth | Headline |
|---|---|---|---|
| `review-2026-08-15.md` | `db87dbd` (2026-08-13) | full, first | Tree is further along than any doc admits; docs are the biggest liability; 18 concrete defects; parse is not generation-safe |
| `review-2026-08-28.md` | `0af56fd` (2026-08-27) | full re-verification | Docs largely fixed; a new first-ranked finding (co-declaration drops the fact); defect table barely moved (1 fixed, 15 open, 6 new) |

## Continuity files

| File | What it holds | Who updates it |
|---|---|---|
| `ledger.md` | Every finding with a stable ID and status: D (defects), F (design), X (doc drift), R (recommendations + uptake) | each review |
| `errata.md` | Claims a review got wrong, found by a later review or the check script; the reviewer rules derived from them | each review |
| `trends.md` | One column per review: corpus, gate, metamodel, ledger and process series | each review |
| `beta-criteria.md` | Proposed beta exit criteria (unratified), scored pass/partial/fail per review | each review; Dev ratifies or strikes rows |
| `decisions.md` | Questions asked of Dev and their status; decisions Dev has taken, with where the tree records them | each review, and Dev |
| `checks/review_checks.py` | Read-only measurements (no sbt, no git writes); prints a Markdown block the review pastes verbatim | any review; extend when a review needs a number the script does not print |
| `authoritative-source.md` | Repo landmarks and the working rules a review session must respect | when landmarks move |

## Protocol for a review

1. **Measure first.** Run `python3 draco-reviews/checks/review_checks.py` at the commit under
   review, and again at the previously reviewed commit (`git worktree add /tmp/prev <commit>` —
   read-only, delete after). The diff between the two outputs is the first draft of §0. Nothing is
   called "new" that appears in both.
2. **Diff the ledger.** For every D/F/X row: re-find the code (line numbers move), set the status
   transition, update `Where`. Score every R row from the previous review: taken / mostly /
   partial / not started, with the commit that did it.
3. **Choose the depth, and say so at the top of the review.** Full re-verification at a release
   boundary, after a metamodel shape change, or when the previous review was not full. Otherwise a
   delta review: ledger diff, check-script diff, and the commits since — with the sections that
   were not re-verified named as such.
4. **Write the review** with the fixed section numbering (0 delta · 1 summary · 2 as-is
   architecture · 3 design · 4 defects · 5 docs · 6 history/process/risk · 7 recommendations · 8
   questions), so consecutive reviews diff. §4 rows carry ledger IDs. Every count carries its
   scope. Every claim about parser behaviour comes from running it.
5. **Update the continuity files** in the same pass: new ledger rows, errata for anything the
   previous review got wrong, a trends column, a beta-criteria column, decisions status. The
   review is not done until these are.
6. **Store** the review as `review-YYYY-MM-DD.md` here, and a copy in the Claude project's docs
   (`claude/review-YYYY-MM-DD.md`) so a session that has not cloned the repo can still read it.

The rules in `errata.md` §"Rules derived" are part of the protocol. Each exists because a review
got something wrong; do not add one without a row that justifies it.

## Reading order for a new session

`authoritative-source.md` → `decisions.md` → `ledger.md` → the latest review → `trends.md`.
