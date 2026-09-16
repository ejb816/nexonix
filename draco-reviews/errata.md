# Review errata

Claims a review made that a later review, or the check script, showed to be wrong. Kept so the
reviewer learns what kind of claim it gets wrong, and so the rules at the bottom have evidence
behind them. A correction here does not edit the original review; the original stays as written.

## Corrections to `review-2026-08-15.md` (found 2026-08-28)

| Where | Claimed | Actual | Class |
|---|---|---|---|
| §2 Companion convention | "53 `App` companions" in `src/main/scala/draco` | 53 is the **flat directory only**; the tree has 81 occurrences in 69 files (`TypeElement.scala` alone holds 13) | count without scope |
| §2 Type system | eleven kinds "extending `Primal[Json]`" directly | They extend the intermediate `sealed trait BodyElement` (`TypeElement.scala:132`), which extends `TypeElement extends Primal[Json]` | structure read from memory, not file |
| §2 Generator dispatch | "else `typeGlobal`" | The last arm is guarded `isLeaf \|\| isActor` with an explicit `IllegalStateException` beneath (`Generator.scala:1716, 1738`) | line range read once, not re-read |
| §2 Generator dispatch | the `{K,V}` → `Map[K,V]` rewrite "lives only in `targetTypes`" | It is `scalaTypeExpression` (`:1562-1594`), reached from `targetTypes` and applied to **valueTypes only**; `_derivation` is copied verbatim (`:1638`) | function attribution |
| §3.7 | `mut{T}` "parses to something else with no error" | It errors — `case other => sys.error("unexpected section")` at `Drake.scala:1063` — unhelpfully and without position | parser behaviour predicted, not run |
| §3.7 | `Iterator( (K,V) )` parses to something else | The glued form is one token, which is correct. The hazard is the *spaced* form, and only where a single token is expected; in a leaf value slot `span` recovers the raw substring | parser behaviour predicted, not run |
| §3.7 | `takeText` throws `ArrayIndexOutOfBounds` at EOF | `take` (`:654`) indexes a `Vector` → `IndexOutOfBoundsException` | exception type from memory |
| §3.7 | "unknown or duplicate sections overwrite silently" | Duplicates overwrite silently (every section assigns a `var`); **unknown** sections error at `:1063` | half-true compound claim |
| §4 row 16 | `circe-optics` unused | Imported by `org/nexonix/format/json/TestCirceJson.scala:4` (an orphaned tutorial test, but an import) | grep scope too narrow |
| §4 row 6 | `SourceContract.scala:52` | `:51` | line number |
| §3.6 | `Generated.scala`/`Codec.scala` "violate the invariant `DomainBuilder.validate` enforces" | `validate` checks **declared** members only; neither is declared. They violate the ch. 43 principle, not the check | invariant vs principle conflated |

## Corrections to `review-2026-08-28.md` (found 2026-09-16, by the check script)

| Where | Claimed | Actual | Class |
|---|---|---|---|
| §3.2 | `inlineTupleArgument` "has since appeared" alongside `authoredAhead` | It was already present at `db87dbd` — `checks/review_checks.py` §11 reports it at both commits | "new" asserted without diffing against the previous commit |
| §4 counts | Two static test counts, 520 and 526, "neither can be settled" | True as stated, but the review should have said *why* static counting is unreliable here: the four loop-driven suites derive their counts from the corpus on disk, so any static method must replicate their filters exactly. The check script now prints only literal `test(...)` sites and says so | measurement without a method statement |
| §4 row 8 | `override val` sites listed as Generator templates + `Generated.scala` only | Six hand-written mods companions also carry `override val` (`checks/` §6). Not a wrong claim — an incomplete one | grep scope too narrow |

## Rules derived

Each rule below exists because a row above happened. Add a rule only with a row to justify it.

1. **Every count states its scope.** "81 companions in `src/main/scala/draco` (whole tree, occurrences)" — never "81 companions". The check script prints scope with every number; quote it with the number.
2. **Parser and lexer behaviour is run, not predicted.** For any claim about what `Drake.parse` does with an input, port the relevant function or run `DrakeCLI parse` and paste the result. The 08-28 review ported the lexer to Python and ran eight cases; three of the 08-15 claims changed.
3. **"New since last review" is asserted only after checking the previous commit.** Run the check script at both commits (a `git worktree` of the previous reviewed commit costs nothing) before calling anything new.
4. **A compound claim is verified per clause.** "Unknown or duplicate sections overwrite silently" was half wrong. Split it, verify each half, cite each half.
5. **Line numbers are re-found, not carried.** Every `file:line` in a new review is re-located in the current tree even where the finding is unchanged. Ten of eighteen defect rows moved between `db87dbd` and `0af56fd`.
6. **An invariant a check enforces and a principle a doc states are different things.** Say which one is violated.
7. **A number that only a run can produce is reported as a claim with its source.** Suite counts come from `sbt test` output pasted into the journal or a record; the review quotes the last *confirmed* run and names any later count as *expected*.
8. **Grep scope for "unused" is the whole tree plus `bin/`, and the result says where the hits are.** "Unused" with one orphaned importer is "used only by dead weight", which is a different recommendation.
