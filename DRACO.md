# DRACO.md

Shared operating rules for Claude Code and Codex in this repository. `CLAUDE.md` and
`AGENTS.md` are symlinks to this file. Read `DEVELOPMENT_HANDOFF.md` when resuming work
or transferring development between agents; the bidirectional protocol is in section 7.

**The architecture baseline was audited on 2026-08-15; later verified updates are dated below.** The previous
version of this file described an architecture that had not existed for months — the
`*Instance` triad, `typeInstance` vals, `TypeElement extends Primal[String]`, Generator
owning type loading. It steered sessions wrong for as long as it stood. If you find a
statement here contradicted by the code, **the code is right and this file is a bug** —
fix it in the same commit as the work that exposed it.

---

## 1. Operating rules

These are the rules that do not change with the architecture. Read them first.

**Do not run sbt, git commit, or git push.** Dev compiles, commits, and pushes from the
IDE. Hand over a command block instead. `git status`, `git log`, `git show` and other
read-only git are fine.

**A new GitHub issue requires Dev's explicit approval, every time.** When work is scoped
out, a decision is deferred, or a bug is noticed in passing, *surface it in conversation*
— batched and prioritised — and file only if Dev says so. Commenting on an existing issue
does not need approval. The backlog grows faster than it drains, and an unprompted issue
buries the few that matter.

**Definitions move as a trio.** A type is three artifacts that must agree:
`src/main/resources/<pkg>/X.drake` (the Source — drake is the definition language),
`src/main/resources/<pkg>/X.json` (the normative form the drake parses to, a bootstrap carrier
and the only form loaded at runtime; re-canonicalized from the drake with `DrakeCLI parse` after
every parser step), `src/main/scala/<pkg>/X.scala` (generated). Change one, change all three, or a
gate fails. There is no partial edit.

**Every val in ANY object that extends `App` must be `lazy val` — the generated companions
AND the hand-written engine.** Scala 2 `App` uses `DelayedInit`, so eager `val` initializers are
deferred to `main()` and read as null across objects. This includes `typeDefinition`, the
kind-vals, `Null`, encoders/decoders, and any private val a lazy val references. `DracoGenerator`
itself extends `App`: a private lookup table added there as an eager `val` on 2026-09-11 failed
eleven tests on its first run. The one axiom exempt from it is `DracoType.typeDefinition`, which
does not extend `App`.

**Read the report-only numbers, not just pass/fail.** Several tests measure rather than
assert (drake surface losses, the example-domain generate map, PON discrepancies). Two
real defects in one August session were caught only by reading a headline that moved —
new corpus data quietly adding to a known tail. See GitHub #62. Until that lands, a green
suite does not mean nothing regressed.

**The baselines, originating at `87a2bb9` (2026-08-31).** The last verified full-suite baseline is
**983 tests / 77 suites**, verified by Dev on 2026-10-31 for the example body sweep (record `git-record-2026-10-31-1436`,
commit pending): one added test in WorldAssemblyTest, no new suite, zero failed/canceled/ignored/pending tests. Parse scope 110 + 37, surface losses 0 across 147 types, the
example gate 48 match / 0 differ; every headline unchanged, including pending mods actors 0. Twelve example trios
take the compact form with their `uses` records, surfaces re-emitted and Scala regenerated; `AssemblyValidator`
compares message types by identity, the one engine change. First full Dev gate green. Before it:
**982 tests / 77 suites**, verified by Dev on 2026-10-31 for the example map closed as a gate (record
`git-record-2026-10-31-1105`, committed as 7be1497): no new suite, eight newly active parser checks, zero
failed/canceled/ignored/pending tests. Parse scope 110 + 37, surface losses 0 across 147 types; example map 48 match /
0 differ, now ASSERTED and off the table below; every other headline unchanged, including pending mods actors 0. Four
example trios gain their drake surface with their JSON re-canonicalized, nine companions become the generator's
output, no engine change. First full Dev gate green. Before it:
**974 tests / 77 suites**, verified by Dev on 2026-10-31 for the coordinate surfaces and companions (record
`git-record-2026-10-31-0842`, committed as 23333f3): three new focused tests in one added suite and four newly active parser checks, zero
failed/canceled/ignored/pending tests. Parse scope 110 + 33, surface losses 0 across 143 types; example map 39 match /
9 differ and every other headline unchanged, including pending mods actors 0. Two example trios gain their drake
surface and the generator's companion, no JSON or engine change. First full Dev gate green. Before it:
**967 tests / 76 suites**, verified by Dev on 2026-10-31 for the Intent json factories (record
`git-record-2026-10-30-2014`, committed as 3a3cbfc): three new focused tests in one added suite and eight newly active parser checks, zero failed/canceled/ignored/pending
tests. Parse scope 110 + 31, surface losses 0 across 141 types; example map 37 match / 11 differ and every
other headline unchanged, including pending mods actors 0. Four example trios gain their drake surface and
factory, eight test sites re-author one construction, no engine change. First full Dev gate green. Before it:
**956 tests / 75 suites**, verified by Dev on 2026-10-30 for the sibling report factories (record
`git-record-2026-10-30-1113`, committed as 8540341): three new focused tests in one added suite and six newly active parser checks, zero failed/canceled/ignored/pending
tests. Parse scope 110 + 27, surface losses 0 across 137 types; example map 37 match / 11 differ and every
other headline unchanged, including pending mods actors 0. Three example trios gain their drake surface and
factory, three re-author one line, no engine change. First full Dev gate green. Before it:
**947 tests / 74 suites**, verified by Dev on 2026-10-30 for the LocationReport json factory (record
`git-record-2026-10-30-1038`, committed as e138f04): three new focused tests in one added suite and two newly active parser
checks, zero failed/canceled/ignored/pending tests. Parse scope 110 + 24, surface
losses 0 across 134 types; example map 37 match / 11 differ and every other headline unchanged, including
pending mods actors 0. One example trio gains its drake surface and factory, two re-author one line, no
engine change. First full Dev gate green. Before it:
**942 tests / 73 suites**, verified by Dev on 2026-10-29 for the terrestrial Output repair (record
`git-record-2026-10-29-1640`, committed as c5b8089): four new focused tests in one added suite, zero
failed/canceled/ignored/pending tests. Example map is 37 match / 11 differ; every other report-only
headline unchanged, including pending mods actors 0. One example trio changed, no engine change.
First full Dev gate green. Before it:
**938 tests / 72 suites**, verified by Dev on 2026-10-29 for the head-keyed construction lookup
(record `git-record-2026-10-29-0755`, committed as 112f277): one added test, zero failed/canceled/ignored/pending tests,
every report-only headline unchanged, including pending mods actors 0. No definition or committed
Scala changed. First full Dev gate green. Before it:
**937 tests / 72 suites**, verified by Dev on 2026-10-26 for the dyn result as a named construction
(record `git-record-2026-10-26-1534`, committed as 9779e39): one added test, zero failed/canceled/ignored/pending tests,
every report-only headline unchanged, including pending mods actors 0. Observable's JSON moved one
leaf; no committed Scala regenerated. First full Dev gate green. Before it:
**936 tests / 72 suites**, verified by Dev on 2026-10-26 for the named-construction head
(record `git-record-2026-10-26-1449`, committed as e241378): three new focused tests in one added suite, zero
failed/canceled/ignored/pending tests, every report-only headline unchanged, including pending mods
actors 0. No committed Scala regenerated. First full Dev gate green. Before it:
**933 tests / 71 suites**, verified by Dev on 2026-10-25 for the Consumer repair
(record `git-record-2026-10-25-1557`): five new focused tests, two newly active parser
checks and one actor emission check, zero failed/canceled/ignored/pending tests and zero
aborted suites. Example map is 36 match / 12 differ, surface losses zero across 133 types,
parse scope 110 + 23; every other headline unchanged, including pending mods actors 0.
First full Dev gate green. Before it:
**925 tests / 70 suites**, verified by Dev on 2026-10-25 for the Provider repair
(record `git-record-2026-10-25-1543`): four new focused tests, two newly active parser
checks and one actor emission check, zero failed/canceled/ignored/pending tests and zero
aborted suites. Example map is 35 match / 13 differ, surface losses zero across 132 types,
parse scope 110 + 22; every other headline unchanged, including pending mods actors 0.
First full Dev gate green. Before it:
**918 tests / 69 suites**, verified by Dev on 2026-10-25 for the Observable repair
(record `git-record-2026-10-25-1517`): eight new focused tests and two newly active parser
checks, zero failed/canceled/ignored/pending tests and zero aborted suites. Example map
is 34 match / 14 differ, surface losses zero across 131 types, parse scope 110 + 21;
every other headline unchanged, including pending mods actors 0. First Dev gate green;
the earlier direct-run loading failures and binding correction are recorded below. Before it:
**908 tests / 68 suites**, verified by Dev on 2026-10-25 for the Cartesian repair
(record `git-record-2026-10-25-1505`): six new focused tests and two newly active parser
checks, zero failed/canceled/ignored/pending tests and zero aborted suites. Example map
is 33 match / 15 differ, surface losses zero across 130 types, parse scope 110 + 20;
every other headline unchanged, including pending mods actors 0. First full gate green. Before it:
**900 tests / 67 suites**, verified by Dev on 2026-10-25 for the Ethereal OriginateReport
repair (record `git-record-2026-10-25-1455`): five new focused tests and two newly active
parser checks, zero failed/canceled/ignored/pending tests and zero aborted suites. Example
map is 32 match / 16 differ, surface losses zero across 129 types, parse scope 110 + 19;
every other headline unchanged, including pending mods actors 0. First full gate green. Before it:
**893 tests / 66 suites**, verified by Dev on 2026-10-24 for the Terrestrial OriginateReport
repair (record `git-record-2026-10-24-1008`): five new focused tests and two newly active
parser checks, zero failed/canceled/ignored/pending tests and zero aborted suites. Example
map is 31 match / 17 differ, surface losses zero across 128 types, parse scope 110 + 18;
every other headline unchanged, including pending mods actors 0. First full gate green. Before it:
**886 tests / 65 suites**, verified by Dev on 2026-10-23 for the Marine OriginateReport
repair (record `git-record-2026-10-23-1613`): four new focused tests and two newly active
parser checks, zero failed/canceled/ignored/pending tests and zero aborted suites. Example
map is 30 match / 18 differ, surface losses zero across 127 types, parse scope 110 + 17;
every other headline unchanged, including pending mods actors 0. First full gate green. Before it:
**880 tests / 64 suites**, verified by Dev on 2026-10-23 for the Aerial OriginateReport
repair (record `git-record-2026-10-23-1216`): three new focused tests and two newly active
parser checks, zero failed/canceled/ignored/pending tests and zero aborted suites. Example
map is 29 match / 19 differ, surface losses zero across 126 types, parse scope 110 + 16;
every other headline unchanged, including pending mods actors 0. First full gate green. Before it:
**875 tests / 63 suites**, verified by Dev on 2026-10-23 for explicit example resolution
bindings (record `git-record-2026-10-23-1106`): two tests in one added suite, zero failed,
canceled, ignored or pending tests, zero aborted suites. Every report-only headline matches,
including zero pending mods actors. Dev's first full gate was green. Before it:
**873 tests / 62 suites**, verified by Dev on 2026-10-23 for reciprocal example membership
(record `git-record-2026-10-23-1048`): two new consistency tests and ten newly active parser
tests, zero failed/canceled/ignored/pending tests, zero aborted suites. Surface losses remain
zero across 125 types; parse scope is 110 draco + 15 mods. Every other report-only headline
matches, including zero pending mods actors. Dev's first full gate was green. Before it:
**861 tests / 61 suites**, verified by Dev on 2026-10-22 for executable empty-role coverage
(record `git-record-2026-10-22-0739`): four added tests, no production or corpus changes,
zero failed/canceled/ignored/pending tests, zero aborted suites, every report-only headline
unchanged, including zero pending mods actors. Dev's first full gate was green. Before it:
**857 tests / 61 suites**, verified by Dev on 2026-10-22 for role presence (record
`git-record-2026-10-22-0800`): four new tests and four restored corpus tests, zero failed,
canceled, ignored or pending tests, zero aborted suites. Surface losses are zero across
120 types; DracoAspect has zero frozen baselines. Every other report-only headline matches,
including zero pending mods actors. Dev's first full gate was green. Before it:
**849 tests / 60 suites**, verified by Dev on 2026-10-22 for the formal/actual split (record
`git-record-2026-10-21-2300`): no test or corpus type added, zero failed, canceled, ignored or pending tests,
zero aborted suites, every report-only headline unchanged, including zero pending mods actors, green on its
first run. Before it, the same **849 / 60**, verified by Dev on 2026-10-21 for the corpus sweep (record
`git-record-2026-10-21-2230`): no test or corpus type added, zero failed, canceled, ignored or pending tests,
zero aborted suites, every report-only headline unchanged, including zero pending mods actors; Dev's first run
of that gate failed one bootstrap-spelling pin in DomainLineTest, moved before the second. Before it, the same
**849 tests / 60 suites**, verified by Dev on 2026-10-21 for the resolution record (record
`git-record-2026-10-21-2130`): four added tests in the added `ResolutionRecordTest`, zero failed, canceled,
ignored or pending tests, zero aborted suites, every report-only headline unchanged, including zero pending
mods actors, green on its first run. Before it:
**845 tests / 59 suites**, verified by Dev on 2026-10-21 for the bootstrap shortening of the Dracon referrers
through canonical scoped generation (record `git-record-2026-10-21-2000`): no test or corpus type added,
zero failed, canceled, ignored or pending tests, zero aborted suites, every report-only headline unchanged,
including zero pending mods actors; Dev's first run of that gate failed three primes rules overwritten by an
unscoped test, its second failed to compile on a helper reference, both fixed before the third. Before it,
the same **845 / 59**, verified by Dev on 2026-10-21 for the Scala target's host vocabulary as a
definition (record `git-record-2026-10-21-1915`): five per-type tests for the one added corpus type,
zero failed, canceled, ignored or pending tests, zero aborted suites, the parse-scope, surface-loss,
aspect-section and GenDrake headlines each moved by one (table updated in that commit), every other
headline unchanged, including zero pending mods actors; Dev's first run of that gate failed one pinned
member list in DraconTest, corrected before the second. Before it:
**840 tests / 59 suites**, verified by Dev on 2026-10-21 for the unit type as the empty Objective
(record `git-record-2026-10-21-1830`): one added test, zero failed, canceled, ignored or pending tests,
zero aborted suites, every report-only headline unchanged, including zero pending mods actors; Dev's
first run of that gate failed the two assembly suites, fixed before the second. Before it:
**839 tests / 59 suites**, verified by Dev on 2026-10-21 for two-stage loading from explicit domain
declarations (record `git-record-2026-10-21-1700`): four added tests and one added suite
(`DomainLoadingTest`), zero failed, canceled, ignored or pending tests, zero aborted suites, every
report-only headline unchanged, including zero pending mods actors in the file-only log. Before it:
**835 tests / 58 suites**, verified by Dev on 2026-10-20 for ontology-scoped names
(record `git-record-2026-10-20-1503`), committed and pushed by Dev as b17ddb8 on October 21:
ten added tests, zero failed, canceled,
ignored or pending tests, zero aborted suites, every report-only headline unchanged,
including zero pending mods actors. Before it:
**825 tests / 57 suites**, verified by Dev on 2026-10-19 for structured method formals
(record `git-record-2026-10-19-1254`), committed and pushed as a13ad38: ten added tests, zero failures or aborts,
all headlines matching the updated table, including zero pending mods actors. Before it:
**815 tests / 56 suites**, verified by Dev on 2026-10-19 for the payload-declaration
increment committed and pushed as f97c913 (record `git-record-2026-10-18-1731`): six new tests, zero
failed, canceled, ignored or pending tests, zero aborted suites, every report-only
headline unchanged, including zero pending mods actors in the file-only log. Before it:
**809 tests / 56 suites**, verified by Dev's full run reported on 2026-10-17 for the
core-tree migration committed and pushed by Dev as d169b0e (record `git-record-2026-10-17-1457`):
five new DraconTest cases, zero failed or aborted, every report-only headline unchanged,
including zero pending mods actors in Dev's pasted file-only log. Before it:
**804 tests / 56 suites**, verified by Dev's full run reported on 2026-10-16 for the
Dracon increment committed and pushed by Dev as db7e7a8 (record `git-record-2026-10-15-1703`):
zero failed or aborted, all report-only headlines matching the updated table, including
zero pending mods actors. Three net-new corpus types add fifteen tests; DraconTest adds seven. Before it:
**782 tests / 55 suites**, verified by Dev's full run on 2026-10-15 on the Generate increment
committed and pushed by Dev as 9185f1a (record `git-record-2026-10-15-1250`): eight new staged
trio tests and six GenerationTest tests, every report-only headline unchanged, including
zero pending mods actors in the file-only log checked by Codex. Before it:
**768 tests / 54 suites**, verified by Dev's run on 2026-10-15 at commit 78bff9d (the Create/Update and
Editor-rules increments, §3 *Create and Update* and *Editor-domain rules*, one commit, record
`git-record-2026-10-14-1217`; EditorTest +12, DreamsTest +4, DomainOntologyTest +1), with every report-only
figure unchanged — no main-corpus TYPE was added, and the transitive codec eligibility it carries is
byte-identical over the main corpus. Before it: **751 / 54** at f4336a4 (the Delete increment, §3 *Delete*,
record `git-record-2026-10-14-1136`; DomainOntologyTest +1, EditorTest +1, DreamsTest +2), committed by Dev on
2026-10-14 after the gate — its grep output was not pasted, but 768 = 751 + 17 as predicted, so the figure is
corroborated, not read. Before it:
**747 / 54** at 799e9ae (the Read increment, §3 *Read*, record `git-record-2026-10-13-2158`; EditorTest +1,
DreamsTest +2), Dev's run on 2026-10-14, every report-only figure unchanged. Before it: **744 / 54** at 5057a5c (the Request-family
increment, §3 *The Request family*, record `git-record-2026-10-13-1311`; DrakeParseTest +1, DreamsTest +2;
its two engine fixes byte-identical over the main corpus), Dev's run on 2026-10-13. Before it: **741 / 54** at 6869e47 (the delivery, CRUD and
initialization increments, §3 *Delivery*, *User defines the requests*, *Initialization*, one commit, record
`git-record-2026-10-12-1413`; EditorTest +1, DreamsTest +1, `InitializationTest` 6 new, the 54th suite),
Dev's run earlier on 2026-10-13. Before it: **735 / 53**, Dev's run
on 2026-10-12 on the same tree before the third increment; before that **733 / 53** at efa9085 (the composition increment, §3 *Composition: the Editor holds
draco's innate ontology*, Dev's run earlier the same day), with the parse-scope and
surface-loss rows below moved by the one corpus type it added and every other figure unchanged. Before it:
723 / 52 at d181e64 (derivation, 2026-10-10), 713 / 51 at 462cde6 (the first authoring slice, 2026-10-09),
701 / 50 at f54da3f (2026-10-07). The day
before: 713 / 51 at 462cde6 (the first authoring slice); before it 701 / 50 at f54da3f on 2026-10-07. Progression: 575 at `6f5a8bb`, then five per-type tests each for `gendrake.Emit`,
`generator.EmissionReceived` (`SurfaceReceived` until 2026-09-10) and `gendrake.Emitter`, plus the two gates of `GenDrakeTest`,
the first suite that fires a generator transform as rules — which also moves the two type
COUNTS below — 93 draco types in scope, 103 measured — and none of the loss figures; then one
structural test in `DrakeParseTest` for the `++` operator (2026-09-10); then `draketarget.DomainLine`
(five per-type tests) and its own two-test suite `DomainLineTest` (2026-09-11), which moves the
type counts to 94 in scope, 104 measured; then `gendrake.DomainLineOf`, the first member of the
transform domain (2026-09-11), to 95 and 105; then one more `DrakeParseTest` test for the
call syntax's at-most-once rule (2026-09-16); then one structural `DrakeParseTest` test for the
four type forms (2026-09-17); then one for the header's type parameters (2026-09-17); then the
`draco.drake` Presence family — three types, their per-type tests and a `DracoGenTest` group test
(2026-09-17), which moves the type counts to 98 in scope, 108 measured and GenDrake to 96 of 96; then
`fold` on the family and its own two-test suite `PresenceTest` (2026-09-17); then one `DrakeParseTest`
test for `now` (2026-09-17); then a `PresenceTest` test that a Present never evaluates `fold`'s default
(2026-09-18); then one that a factory argument is evaluated on first read and never if unused
(2026-09-18); then one that `ifThenElse` never evaluates the branch not taken (2026-09-20); then one `DrakeParseTest` test for the operator layer and the lambda
(2026-09-21); then one for the parenthesized sub-expression and the minimal pair (2026-09-21); then one for `bracket`
(2026-09-22) — **627 tests / 44 suites**. The Text increment adds five corpus tests and
four tests in `draco.drake.TextTest`: **636 tests / 45 suites passed** in Dev's full
sbt run; all report-only figures matched the then-current baselines. A direct compiled-class probe also passed 506 tests / 5 suites (TextTest, DracoGenTest,
DrakeGenTest, DrakeParseTest, GenDrakeTest); it measured 99 draco types in scope, 109 measured,
one surface loss, and GenDrake 97 of 97. These
are the headlines those tests print. They go to the console
logger, not to the per-suite files, so they have to be caught off stdout — every row below
except the last `DrakeGenTest` one, which prints only to its per-suite file:

```bash
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

| test | headline | baseline |
|---|---|---|
| `DrakeParseTest` | drake surface losses | 0 fields across 147 types — type form 0, expression form 0 |
| `DrakeParseTest` | Drake.parse scope | 110 draco + 37 mods in, 0 held back |
| `DrakeGenTest` | mods actors pending `.drake` | 0 — **file-only**, in `target/test-output/DrakeGenTest.log` |
| `PonCorpusTest` | PON corpus | 80 numbers, 550 expressions, 42 discrepancies |
| `PonCorpusTest` | canonical check | 80 numbers, 7 differ from generated canonical |
| `ScenarioDrakeTest` | scenario in today's drake | 23 files, 0 rejected, 0 drifted, 23 clean |
| `SubtypeFactVisibilityTest` | rete subtype visibility | CO-DECLARATION DROPS THE FACT (categorical, not a count) |

A number here disagreeing with a run means one of two things, and both are worth stopping
for: the run regressed, or this table went stale. **The commit that legitimately moves a
number updates this table in the same commit** — the same discipline the CHANGELOG entry
and the git-record already follow, and for the same reason: a figure nobody is obliged to
maintain is one nobody can trust. The alpha.6 notes still quote 16 surface-loss fields;
that was true when written and became 15 at `324556c`, which is exactly the drift this
table exists to make visible.

**Verified domain-aspect increment (2026-09-22):** two new definition-backed types,
`draketarget.DomainAspectText` and `gendrake.DomainAspectOf`, add ten corpus tests;
the new six-test `DomainAspectTest` brings the full count to **652 tests / 46 suites passed**.
GenDrake emitted 99/99 definitions. The new section test reproduced all 101 complete
domain sections, including the parameterized domain omitted by the retained DomainLine
compatibility test, with zero exclusions. All report-only figures matched the then-current table.

**Verified Draco-aspect increment (2026-09-28):** two new types add ten corpus tests
and DracoAspectTest adds seven focused tests. Dev's full run passed **669 tests / 47
suites**, with no failures or aborted suites and all report-only baselines matching.
An isolated compiled-class probe had passed **538
tests / 7 suites** (DracoAspectTest, DomainAspectTest, DracoGenTest, DrakeGenTest,
DrakeParseTest, GenDrakeTest, SourceContractTest). GenDrake emitted 101/101 definitions;
both aspect probes covered 103 sections with none skipped. The Draco probe uses frozen
pre-migration sections for ActorAspect and BodyElement, whose authored sources remain
ahead of JSON. Both Drake and Scala projections were byte-identical before/after over
all 103 current JSON definitions. The full run also confirmed the table above and zero
mods actors pending Drake authoring in the file-only report.

Three of these have since become ASSERTIONS and are deliberately not listed: the scenario
now projects, wires and runs under `ScenarioGenTest`, which fails rather than reports. So
does `GenDrakeTest`'s `GenDrake runs` line — in the capture grep for visibility, not here.
And since 2026-10-31 the example-domain gen map, which this table carried from 37 / 11 down to
39 / 9: `ExampleDomainsGenTest` now asserts 48 match / 0 differ / 0 error / 0 missing over the
example domains, and its `GEN MAP` line stays in the capture grep for the same reason.
Moving a measurement into an assertion is the goal; the table should shrink over time.

**Where things are recorded** — distinct artifacts and jobs, do not conflate them:

| artifact | holds | who writes it |
|---|---|---|
| auto-memory (`MEMORY.md` + notes) | tool-local retrieval aids for conventions and feedback; shared decisions must also be in project documents | the active agent, as work happens |
| GitHub Issues | work to do; decisions deferred | only with Dev's approval |
| `draco-git-record/` | audit trail — one file per commit, containing the commit message | this session, before the commit |
| `CHANGELOG.md` | the *fact* of each change, for a reader outside the tree | this session, **in the same commit as the record** |
| `draco-dev-journal/` | sourced historical narrative, preserving dialogue and corrections | Claude Code or Codex; Cowork may also contribute when Dev assigns it |
| `DEVELOPMENT_HANDOFF.md` | current checkpoint, evidence, open decisions and documentation coverage for the next agent | the outgoing agent; the incoming agent verifies it against the tree |

**Journal authorization (Dev, 2026-09-23).** Claude Code and Codex both have explicit
permission to maintain the journal, DRACO.md, README.md and project-embedded continuity
records during development and handoffs. This supersedes the Cowork-only restriction;
no separate Cowork handoff is required. Preserve the journal's source and format rules,
and distinguish verified facts, Dev's decisions and model proposals. This permission
does not authorize sbt, commits, pushes, new GitHub issues or unrelated changes.

**The CHANGELOG entry is written with the git-record, not at release time.** Both go in
the commit they describe. The record carries the reasoning; the CHANGELOG carries one or
two sentences of observable fact under `[Unreleased]`. Attaching it to a step that already
happens is the point — this file fell two and a half months behind when it depended on
remembering.

**Git-record filenames use `git-record-YYYY-MM-DD-HHMM` in local project time.**
Use the record heading to describe its purpose. Dev restored this convention on
2026-10-05; committed descriptive-suffix records remain historical exceptions.

**Commit messages go through a file, never a heredoc.** A long `git commit -F - <<'EOF'`
breaks on paste and the remainder runs as shell commands. Write the message to a file and
use `git commit -F <file>`. When a commit is meant to be path-scoped, put the pathspec on
the *commit* (`git commit -F msg -- <paths>`) — the IDE auto-adds new files, so a scoped
`git add` does not scope the commit. **A staged rename cannot be path-scoped:** its source path
matches nothing once the index drops it (`git add -A -- old` is fatal), and a pathspec naming only
the destination commits the add without the delete. The same holds for a never-committed file
whose deletion is staged. When the tree holds a `git mv`, verify `git status --short` shows only
the intended set and commit WITHOUT a pathspec (2026-10-13, after two failed blocks).

**Model-authored prose is not Dev's intent.** Issues, memory notes, and review documents
in this repo are largely model-authored and Dev-tolerated. Cite them as prior reasoning to
re-examine, never as Dev's authority or specification.

---

## 2. The gates, and what a failure means

Three suites pin the corpus. Knowing which one failed tells you what is actually wrong.

| gate | pins | a failure means |
|---|---|---|
| `DracoGenTest` | `DracoGenerator.generate(X.json)` ≡ hand-written `X.scala`, whitespace-normalized, for every definition | the JSON, the Generator, or the Scala moved without the others |
| `DrakeGenTest` | `Drake.emit(X.json)` ≡ hand-written `X.drake` | the emitter or the surface moved |
| `DrakeParseTest` | `emit(parse(source))` ≡ source, and `parse(emit(td))` ≡ td | the parser and emitter disagree, or a value form is not carried |

`DracoGenTest` compares **text and never compiles**. It is nonetheless a compile check in
effect, transitively: generated output is pinned to the hand-written files, and sbt
compiles those. Do not "fix" this by adding compilation — the guarantee holds. Since
2026-10-31 `ExampleDomainsGenTest` is the same gate over the 48 example definitions under
`src/mods/resources/domains` — 48 match / 0 differ, asserted — and sbt compiles those files
too; it was a report-only map, 9 differences at its last reading, until the four ConsumeReport
trios took their surfaces and the last nine example companions became the generator's output.

`comparisonOnlyExcluded` is `Map.empty`: no hand-written customisation remains under
`src/main/scala/draco/`. Keep it that way. If generated output is wrong, fix the JSON or
`DracoGenerator`, not the Scala.

---

## 3. Orientation — what is actually true

Compact by intent. Architecture detail belongs in `README.md` once that file is corrected
(§5); this section exists so a session is not misled in the meantime.

**Type system.** `DracoType` is a one-member trait (`val typeDefinition`).
`TypeDefinition extends Aspects { val typeName }`, and `Aspects` is **five** slots:

- `dracoAspect` — typeParameters (the header's formals, `[TypeParameter]`, 2026-10-21), superDomain, modules,
  extensible, derivation, elements, factory, globalElements
  (`derivation` is `[draco.format.dracon.Dracon]` since 2026-10-17, previously `[Json]`: a TypeName object for a draco parent, a type-form node
  for a FOREIGN one — Dictionary's `{"{}": ["K", "V"]}`; read names through `DracoAspect.parents`)
- `domainAspect` — typeName (self-loop for a domain, container pointer otherwise), elementTypeNames,
  source, target (both present = a **transform domain**; role is presence, as everywhere else)
- `ruleAspect` — Presence(RuleAspect), carrying pattern and action when Present
- `actorAspect` — Presence(ActorAspect), carrying messageType, start, message and signal when Present
- `codecAspect` — discriminator, payload (field names selecting direct Dracon payloads; 2026-10-18)

**Role is presence, not name.** A type is a rule because it carries a `ruleAspect`, an
actor because it carries an `actorAspect`. Generated objects take the bare authored name —
no `Rule` or `Actor` suffix, and no `.rule`/`.actor` filename suffix.

**`TypeElement`** is a sealed family of **twelve** kinds — Fixed, Mutable, Dynamic, Local,
Parameter, Monadic, Condition, Action, Pattern, Variable, Factory, Case — all extending
`Primal[draco.format.dracon.Dracon]` since 2026-10-17. Its payload is still a JSON node: either a host-opaque source string or a
single-key `{op: [operands]}` expression tree. **`valueType` holds Dracon on the same terms since 2026-10-17**
(the JSON-node representation dates to 2026-09-16) — a string is the type's authored text (all of the corpus today), an object
is a type-form tree (the parser trees the four forms since 2026-09-17); every consumer reads it through
`TypeForm.text` (`e.valueType.text`), and the engine spells it for the target once, in
`targetTypes`. Every element also carries `now: Boolean` (default false, elided), the strictness
override of drake.dlt EVALUATION. JSON uses a `"kind"` discriminator;
`Codec.sub` narrows the parent codec.

**`TypeName`** is `name` + `namePackage` + `typeParameters`, with derived `namePath` and
`resourcePath`. It compares **structurally** (GitHub #37). Its type parameters are the ACTUAL ARGUMENTS of a
REFERENCE (Dev, 2026-10-21): each is Dracon holding a JSON node on the value-type terms (wrapped since
2026-10-17; tree syntax since 2026-09-17) — a string is the authored text, a tree a type form, and the parser
builds the tree for every reference argument (`(S, T)` an Objective) — read as text through `TypeForm.text`
and spelled for Scala at each render site, because identity is not rewritten in place. A reference is
abstract iff any position contains a variable at any depth, and only a fully concrete name can be the
derivation for an atomic term; so `Dictionary(K, V)` ≠ `Dictionary(TypeName, TypeDefinition)`. A DEFINITION's
own name carries NO parameters: its header's FORMALS — `type Coordinate(T <: Product)` — are `TypeParameter`
records (name, optional upper and lower bounds as type forms, the record a method's formals use) held in
`DracoAspect.typeParameters`, and the definition is keyed bare, as Uniqueness, self-declaration and the
ontology always keyed it.

**Companion convention.** Every generated companion is:

```text
object X extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName("X", _namePackage = Seq(...)))
  lazy val dracoType: Type[X] = Type[X](typeDefinition)
  lazy val domainType: Domain[Container] = Domain[Container](typeDefinition)
  // plus derived codec, apply, Null; ruleType / actorType where the aspect is present
}
```

The kind-val is named for the kind — `dracoType`, `domainType`, `ruleType`, `actorType`.
There is no `typeInstance` and no `*Instance` trait.

**Loading.** `TypeLoader` owns it, not `DracoGenerator`:

```text
TypeLoader.loadType → tryLoad → draco.generator.carrier.DefinitionPath.default.source(resourcePath) → readDefinition
```

The loader path speaks `Presence`, not the host's option, since 2026-09-18: `source`, `readDefinition`,
`loadFromResource` and `tryLoad` declare `draco.drake.Presence(T)`, convert once at the host boundary with
the `presence` symbol, and chain with `fold`; `Dictionary.get` keeps `Option` because it overrides `Map.get`.
`rooted` appends the universal root to any definition carrying no NAMED parent — an absent
derivation is the common case, a solely foreign one (`Dictionary`, whose parent is a type form) the
other — so no definition in the corpus spells `DracoType`. An EMPTY package is not "foreign": it is
the nameless domain, draco's default package, whose whole identity is the empty path (Dev, 2026-09-20).
It has no companion, no dictionary entry and no name; `src/main/resources/.drake` (the one line `domain`)
and `.json` are its anchor for the parser and emitter, and nothing permanent may refer to it.

`DefinitionPath` — in `draco.generator.carrier`, the generator's source side, where host
realization is the content rather than a leak — holds `roots: Seq[URI]` explicitly and
resolves unique-or-error: more than one root carrying a name is a hard error, because order
cannot survive projection to another target language. `hostRoots` derives the default from `java.class.path`; it is one
realization, not the definition, and a path can be constructed without a classloader. A
missing definition yields a typeName-only stub, which is legitimate.

**`DracoGenerator` dispatch** (`src/mods/scala/draco/DracoGenerator.scala`, the hand-written
engine — renamed from `Generator` on 2026-09-08 so the definition-backed `draco.Generator(T)`
can own the name) normalizes at the entry
with `TypeLoader.rooted` (absent derivation means derives-`DracoType`) and `targetTypes`
(every value type takes the target's spelling here — a type-form tree through `targetType`, a
neutral `{K,V}` string through the `Map[K,V]` rewrite — the *only* place a Scala type spelling
is produced; a TypeName's parameters are spelled at each render site instead, because identity
is not rewritten in place). It then dispatches six ways: two-or-more role aspects → composed;
rule; actor; domain; object-only; plain type.

**Evaluation is lazy by default, `now` overrides** (drake.dlt EVALUATION, 2026-09-17): a `fix` or `loc`
inside a method, action or factory body renders `lazy val` (step 1); a method parameter is by-name,
`_x: => T`, bound once by a `lazy val x = _x` prelude (step 2, 2026-09-18); a factory parameter is by-name
too, and the factory body is its prelude — a bodiless factory binds `override lazy val x = _x`, a bodied
one reads `_x` inside its lazy members (step 3, 2026-09-18); the actor-minting `factory ActorType`
follows the same convention, `def actorType(_consumer: => M => Unit)` with `lazy val consumer =
_consumer` as the minted actor's first member (step 4, 2026-09-18; the parameter was the host's `ActorRef[M]`
until the consumer form landed the same day — see drake.dlt VALUE-TYPES). `now` before a member keeps it strict and is a reserved word; it is
required where the target binds the signature — a host method met (TypeName's `equals`, Dictionary's
`Map` operations, CLI's `main`) or a method used as a function value (TypeLoader's two).

**DRAKE** is draco's own definition surface — `X.drake` beside every `X.json`.
`Drake.emit` and `Drake.parse` are mutual inverses in `src/mods/scala/draco/Drake.scala`;
`DrakeCLI` offers `emit | parse | check`. Whitespace is insignificant. Three bracket rules:
name lists always bracketed, keyword blocks never, openers bracket themselves. References
are bare in the referring type's own package, qualified elsewhere, and written as a type
expression when the referent is outside every draco domain (`Dictionary` derives `{K, V}`).
Value types are
`[T]` Seq, `{T}` Set, `{K,V}` Map, their mutable versions `[T]+`, `{T}+`, `{K,V}+` (2026-09-20; `mut {T}` retired),
`F(A,B)`, `A -> B`, tuples, and `()` the unit type — the empty Objective (2026-10-21). The full spec is
`src/main/resources/draco/drake.dlt`, which is current and authoritative.

**Caveat, and it matters if you author drake:** `Drake.parse` trees every VALUE TYPE as one of
the four type forms (2026-09-17; no host-text type form remains since `[T]+` landed on 2026-09-20), and
builds expression trees for calls — `f(a, b)`, positional then `name:value`, one glued token split
at depth-0 parentheses, commas and dots (2026-09-16; the `parameters`/`par` call form, its `[ ]`
argument brackets and `.member` chain lines are RETIRED) — tuples, collection literals `[x, y]` /
`{a, b}` (2026-09-20), the INFIX OPERATOR LAYER — `* / %`, `+ -`, `++`, the six comparisons, `&&`,
`||` and `->`, read flat and reassociated by Haskell's Prelude fixities with the arrow loosest of all
(2026-09-21; `++` alone since 2026-09-10) — and a LAMBDA `\p1 p2 -> body`, the `\` node, whose body
takes everything right of its arrow (2026-09-21). A run trees only when every operand is ONE token
or the lambda; a run with a several-token operand — a host `if`, a `new`, a block — stays whole as a
host-opaque string in *drake* form, which `DracoGenerator.expression` would pass verbatim into
Scala, so the parser never hands the engine a tree with the wrong scope. A parenthesized group
holding an expression the parser reads DISSOLVES into its tree, and each renderer writes back the
minimal pair by its own target's fixity (2026-09-21) — so an authored redundant pair does not survive
re-canonicalization. `=>` is never a drake token: it is the Scala target's spelling of the lambda node. The JSON corpus is re-canonicalized from the drake after each parser step (`DrakeCLI parse
X.drake > X.json` with the compiled parser; 2026-09-16 for calls, 2026-09-17 for type forms, 2026-09-21 for operators and lambdas). BodyElement and ActorAspect were re-canonicalized
on 2026-10-22, removing the last two exceptions; format/json/Value came off the list on
2026-09-21 when its conditionals became `guard(c).ifThenElse(t, e)`. Parse is a measurement tool, not
yet an authoring path (GitHub #61).

**Tiers.** `src/main/scala` is definition-backed. `src/mods/scala` compiles into the same
package tree and holds the hand-written engine: `DracoGenerator`, `GeneratorCLI`, `Drake`,
`DrakeCLI`, `Expression`, `DomainBuilder`, `Assembly*`, `SourceContract`. mods → main is
allowed; main → mods is not. Whether mods is now *the* engine tier rather than a
speculative layer is an open question for Dev.

**Domain-aspect generation (2026-09-22).** `draketarget.DomainAspectText` owns complete
domain-section layout; `gendrake.DomainAspectOf` maps from TypeDefinition, reading the
super-domain from DracoAspect and the other fields from DomainAspect. `Drake.emit`
uses this generated transform, which reaches the running Emit/EmissionReceived flow.
The transform receives type-parameter spelling as an explicit callback, supplied by
the existing Drake type-form renderer; it never calls back into the full emitter.
`DomainLine` and `DomainLineOf` remain compatibility helpers, not full aspect renderers.

**Draco-aspect generation (2026-09-28).** `draketarget.DracoAspectText` owns the type
section's layout; `gendrake.DracoAspectOf` maps the header, derivation, modules,
extensibility, elements, factory and globals. It preserves own-package reference
elision, reconstructable-root elision and implicit versus explicit factory results.
`Drake.emit` delegates to it through `dracoAspectText`. Four explicit callbacks retain
parameter/type-form/value-type-slot spelling and element-body rendering in Drake;
the generated main types do not depend on mods classes. Super-domain output remains
in the domain section and owned by DracoAspect. This is Drake output only: the
Scala generator and GenScala have not been migrated. Rule/actor rendering and codec
rejection retain their existing behavior.

**Domains.** `draco` (root), `draco.base`, `draco.primes`, `draco.format` (+ `dracon`,
`xml`), `draco.rete`, `draco.drake` (the runtime: `Presence(T)` / `Present` / `Absent` with `fold` and `ifThenElse` as dispatch, 2026-09-17 and 2026-09-20; a Boolean reaches `ifThenElse` through the `guard` symbol), `draco.draketarget`, `draco.generator` (+ `carrier`),
`draco.genscala`, `draco.gendrake`, `draco.scalatarget`. Domains are
peers in the `DomainOntology`, not hierarchical. **The cross-domain structure is the DOMAIN
ONTOLOGY since 2026-10-09** (Dev: an operational ontology built from domains and their intra- and
inter-relationships; `DomainDictionary` was the expedient name); "dictionary" now means only a
domain's own `TypeDictionary`. Say ontology for the whole, dictionary for a domain's members. Example domains live in
`src/mods/scala/domains/` (the World / media chain).

**Service staging (2026-09-30).** `draco.service.Service` is a self-declaring peer
domain under `src/mods/{resources,scala}/draco/service`. Its initial trio was generated with
no members. It is the intended neutral home for service capabilities and
configuration, ahead of target-language ZeroMQ wrappers, not a running service.
Do not add it to Draco's member list: use explicit DomainOntology composition.
The main-corpus gates do not scan staged draco definitions; ServiceTest provides
five explicit projection, loading, validation and dictionary/generation checks.
The focused ServiceTest + DomainBuilderTest probe passed 15 tests / 2 suites;
Dev's full suite passed 674 tests / 48 suites on September 30. Existing corpus report
counts stay unchanged because this definition is outside those scans.

**Service plan (Dev decisions, recorded 2026-10-05; not implemented).**
ServiceConfiguration contains Assembly. The primordial service supports external
creation/modification of domain/type definitions and domain-ontology composition;
the first increment is authoring, validation and composition. Generation and activation
are later explicit operations. An endogenous initialization client uses existing
actors/messages and canonical JSON definitions, independently of the ZeroMQ track.
The consuming project owns persistence. Parsing remains rule-owned and dispatch
uses the active dictionary. See src/mods/resources/draco/service/README.md for the
shared plan and src/mods/ZEROMQ_CODEC_CONTRACT.md for the next integration proposal.
The empty Service anchor is committed in 604aff2; proposed envelope/socket/codec
choices are not settled. Journal coverage reaches chapter 104; the first authoring slice landed
2026-10-09 (below).

**ZeroMQ bridge staging (2026-10-05).** Service now contains TextOutput, the neutral
Text -> Boolean send capability (queue acceptance only). Root pins JeroMQ 0.6.0.
The handwritten mods bridge uses existing Draco actors/rules, bounded queues and
one socket-owning thread. JSON parsing, exact ontology resolution and output wrapping
remain rule actions. The executable loopback PAIR/single-frame fixture forwards the
payload to a JSON-input actor and returns typed-output JSON to the same peer; those
protocol choices are provisional. No authoring, initialization client, new codec syntax
or persistence is implemented. Direct compiled-class probe: 548 tests / 7 suites,
existing measured baselines unchanged. Dev's full run passed 685 tests / 49 suites
on October 5, with zero failures/aborts and unchanged report-only baselines, including
zero pending mods actors in the file-only Drake report. See the contract for limitations.

**Dreams name skeleton (2026-10-07).** draco.dreams.Dreams,
draco.dreams.editor.Editor and draco.dreams.user.User are empty, self-declaring
peer domains with trios under src/mods/{resources,scala}/draco/dreams. The old main
Dreams trait is replaced by generated staging source under the same fully qualified
name. Package nesting implies neither domain membership nor super-domain inheritance.
The legacy draco.dreams.Service and Orion scaffolds remain separate, unregistered
Scala traits; they are not new domain definitions. DreamsTest supplies 16 explicit
gates. The direct probe passed 555 tests / 7 suites; Dev's October 7 full run passed
701 tests / 50 suites with zero failures/aborts and unchanged report-only baselines.
See the Dreams README for scope.

**Domain ontology and the first authoring slice (2026-10-09; 462cde6, Dev's full run 713 / 51).**
`DomainDictionary` is renamed `DomainOntology` — the DOMAIN ONTOLOGY, Dev's operational ontology built
from domains and their intra- and inter-relationships; today it still holds only the map and gains
`defines(typeName)`, true when any domain key or any member matches by name and package (parameters
ignored: a definition is keyed bare). `TypeDictionary` and `Domain` factories take an optional
`members [TypeDefinition]` and populate from it, name-matched within the domain's package, placeholders
for the rest — the core capability `DomainBuilder.define` used to supply from resources (it now loads and
delegates; `DomainBuilder.dictionary` is `ontology`). `DerivationResolvable` joins `var o DomainOntology`
and flags any NAMED parent `o.defines` does not know — no package filter (a foreign parent is a type form,
never a name) and no classpath lookup, so the validation sessions insert the ontology as a fact.
`SelfDeclaration` compares the whole TypeName. `draco.service.ServiceConfiguration` contains an
`Assembly`. `draco.dreams.editor` gains `Latent` (one domain in candidate form: `definition` +
`members`), `Actual` (the same + `problems`; `accepted` iff none; `domain` is a DYN deriving the
populated DomainType, because a `DomainType` factory parameter would emit an uncompilable codec) and
the `Editor` actor (`actorType(ontology, actual: Actual -> Unit)` then; `actorType(initial, actual, ontology)` since the
delivery increment below; message `Latent`): per message it
composes the candidate through `Domain(definition, members)`, the ontology that WOULD result, and fires
the four core rules in a fresh stateful session over the candidate's populated members. It never
consults the classpath and never mutates the ontology given. Latent and Actual operate on DOMAINS;
composing accepted Actuals and their relationships into the ontology is the NEXT increment, as are the
endogenous initialization client, a membership-reciprocity rule (a supplied member the domain does not
name is silently dropped today), duplicate-identity rejection, generation and activation. EditorTest
(13) and two ServiceTest tests are new; DreamsTest keeps the two empty anchors.

**Derivation, the ontology's first explicit relationship (2026-10-09, second increment; d181e64, Dev's full run 723 / 52).**
Dev accepted derivation as the first relationship the ontology carries, with PROVENANCE on the edge and
declared edges first. `draco.Derivation` is `child` + `parent` (TypeNames) + `declared` (default true).
`DomainOntology` PROJECTS `derivations` at composition — one edge per named parent of every composed
domain's own definition and members, read through `DracoAspect.parents`, never re-authored — and answers
`owns(domain, typeName)`, `defines(typeName)` (now `keys.exists(owns)`), `dependencies(domain)` (the other
domains a domain's edges reach: Base → Draco through Cardinal → Primal is the first inter-domain
relationship the ontology can state) and `including(domain)` (a new ontology with one more domain; the
one it is called on is untouched — this is how an accepted Actual's domain will join). `DerivationResolvable`
now reads the EDGES: a Problem when an edge whose child is the member names a parent the ontology does not
define. Consequence: a member fact the ontology does not compose has no edges and is not judged — the
Editor's provisional ontology includes the candidate, and the validation tests compose their danglers in
through `including`. INFERRED derivation (OWL-style classification: B carries every element of A by name
and value type ⇒ B derives A; Dev asked, the idea is not naive) is a LATER rule and will set `declared`
false. The Editor is untouched. One corpus type added, so the parse-scope, surface-loss, aspect-section and
GenDrake headlines each move by one: 104 draco + 10 mods in scope, 1 loss across 114 types, 104 sections
in both aspect probes, GenDrake 102 of 102 (table updated in this commit). `DomainOntologyTest` (5) is new.

**Composition: the Editor holds draco's innate ontology (2026-10-12; efa9085, Dev's full run 733 / 53).** Dev: the ontology
being built is draco's INNATE, ENDOGENOUS ontology — the framework's own domains plus the dreams
definitions as they can be accepted — "highly self-referential, and we need to be careful how it is
maintained"; the Editor may hold it "as long as that acceptance doesn't compromise the integrity of the
framework itself". Two guarantees carry that. (1) Acceptance never alters the running framework: the
ontology is data, generation and activation stay explicit later operations. (2) Acceptance never redefines:
`draco.Uniqueness`, a core rule joining `var m TypeDefinition` with `var o DomainOntology`, is a Problem for
every definition whose name is owned by more than one composed domain — over NAME + PACKAGE, PARAMETERS
FREE (Dev, same day: parameters compose references, not definitions; `Dictionary(K, V)` and
`Dictionary(TypeName, TypeDefinition)` both resolve to the one `draco.Dictionary`; a dreams user gets a
distinct definition by package arrangement, and only a second definition of the same atomic name in the
same package is refused). UPDATE-BY-IDENTITY: `including(domain)` now REPLACES a composed domain of the same
name and package instead of adding beside it, so a resubmitted domain is an update and Uniqueness judges
the replacement like any creation. The Editor is STATEFUL: `mut accepted` starts as the ontology it was
minted with; per Latent it builds `accepted.including(candidate)`, inserts EVERY composed domain and EVERY
definition of that provisional ontology plus the ontology itself, fires the five core rules (Completeness,
SelfDeclaration, DerivationResolvable, Uniqueness, CollectProblems) in a fresh session, and swaps the state
through `guard(problems.isEmpty).ifThenElse(accept(provisional), accept(accepted))` — only when every rule is
quiet. Validating the WHOLE resulting ontology is what stops an update from orphaning a dependent: dropping
a member another domain derives from is refused, naming the dependent. The Actual stays the report on one
domain; delivering the grown ontology to a caller came the same afternoon (next paragraph). The Draco actor's
own knowledge gains Uniqueness as a member rule. `UniquenessRulesTest` (2) is new; EditorTest pins the three
agreed cases — a sibling accepted earlier now resolves, a collision is refused, an update replaces in place
and cannot orphan. One corpus type added: 105 draco + 10 mods in scope, 1 loss across 115 types, 105 aspect
sections, GenDrake 103 of 103 (table updated in this commit).

**Delivery: the grown ontology reaches the caller through a consumer (2026-10-12, third increment; Dev's full run
735 / 53 together with the fourth, record 1413).** The initialization client's first design point, settled before
building. The Editor's state was readable by nobody outside it, and the ontology has NO CODEC (a domain holds a
`TypeDictionary` of definitions), so it cannot ride a message or the JSON-codable Actual. Dev accepted "the ontology
consumer on the Editor's factory": `actorType(initial: DomainOntology, actual: Response -> Unit, ontology: DomainOntology -> Unit)`
(the consumer was `Actual -> Unit` for an hour, until the fourth increment typed it by User's Response) — the starting
state is now `initial`, and per Latent, AFTER the Actual, `guard(problems.isEmpty).ifThenElse(ontology(accepted), ())`
delivers the new state on acceptance only. A rejection delivers nothing, so a caller keeping its last delivery
holds the current state. This is the consumer-of-M form of 2026-09-18, the one way draco actors reach anyone,
and it holds in any target. Rejected: the ontology on the Actual (the report on one domain); a query message
with a reply (the ontology would have to be a message). `()` is the first unit literal in the corpus: the parser
carries it as a leaf and both renderers write it back as itself. No corpus type added, so no headline moves (the
dreams domains are mods-staged, outside the scans).

**User defines the requests, Editor the responses (2026-10-12, fourth increment; same run, 735 / 53).** Dev asked
what the "client" is to their "User" and restated the original view: Editor and User support CRUD, User defining
the request types and Editor the response types. Accepted reading: the initialization client IS a User — draco as
its own first, endogenous user, not a new domain and not a Service type; Create and Update are the `Latent` (the
ontology decides which, by identity); Read and Delete are LATER request types; the `Actual` is the response. So
`Latent` MOVED to `draco.dreams.user`. Then the mutual dependency: Editor consumes User's request, a user consumes
Editor's Actual — a cycle the ontology can hold but a Haskell module pair cannot. Accepted: User declares
`Response`, the abstract reply (`problems`, `accepted`; elements, no factory — the Aspects precedent), and
`Actual` DERIVES it: `type Actual from draco dreams user Response`, the corpus's first cross-package `from`. A
qualified reference in a `from` header or a `messageType` line is SPACE-separated package words, like the domain
line; the dotted form parses as one package word. The Editor's consumer is `(draco.dreams.user.Response -> Unit)`,
so a user never names `Editor.Actual`; the dependency runs Editor → User only, and the ontology states it through
derivation, the relationship it already projects (`dependencies(Editor)` = {User, Draco}; `dependencies(User)` =
{Draco}). The generator carried the inherited `problems` into Actual's codec and factory and imported the parent's
package unprompted. User holds Latent + Response; Editor holds Actual alone. EditorTest 16 (net +1 over the two
increments), DreamsTest 12 (+1): 735 / 53, verified by Dev's full run with every headline unchanged.

**Initialization: draco builds its innate ontology as its own first User (2026-10-12, fifth increment; same
commit, Dev's full run 741 / 54 on 2026-10-13).** Dev accepted "the computed dependency order from an empty
ontology". `draco.dreams.Initialization` is the Dreams domain's FIRST member — Dreams is the constructing layer,
and the client must name both User (requests) and Editor (the actor), which neither may do without re-creating
the cycle the fourth increment removed. It takes the canonical domains as Latents (SUPPLIED, never loaded: the
consuming project owns persistence; the test supplies them through DomainBuilder) and ORDERS them from the
definitions — `owns`, `parents` (named parents outside the Latent), `ready` (every parent some supplied domain
owns is placed), `order` (a recursive dyn: a wave at a time, supplied order kept within a wave, a parent nobody
supplied is no constraint, the remainder appended unchanged when no wave forms, so a cycle cannot hang it).
Every loaded definition is rooted at DracoType, so Draco alone is the first wave. `editorType` mints the Editor
over `DomainOntology()` with the client's own consumers `note` and `keep`; `run(editor)` sends the ordered
Latents; `mut ontology` holds the last delivery — the first `mut` element in a plain type (trait `var`, factory
body `override var`). Nothing is hand-listed. THE RUN FOUND A DEFECT: Format, then XML, JSON and Value, were
refused — `Format(F)` is the corpus's one parameterized domain and self-declares bare (`domain draco format
Format`, as its members point at it), and the 2026-10-09 SelfDeclaration compared the WHOLE TypeName. Dev's
2026-10-12 principle decides it (parameters compose references, not definitions; a self-declaration points at
the definition, keyed bare): `SelfDeclaration` and `DomainBuilder.validate` now compare `namePath`. This
alignment was the model's application of Dev's principle; Dev CONFIRMED it on 2026-10-13. The
endogenous run then accepts all 18 canonical domains, and the ontology it builds defines the client itself.
`InitializationTest` (6) is new, the 54th suite; EditorTest's SelfDeclaration case now refuses a
self-declaration naming ANOTHER definition and accepts the bare parameterized one.

**The Request family (2026-10-13, Read's first design point; 5057a5c, Dev's full run 744 / 54).** The Editor is
one actor with one message type, so a second request needs a shared shape. Dev accepted "the Request family in
User": `draco.dreams.user.Request`, abstract (no elements, no factory, as ActorType); `Latent` derives it; User
holds Latent, Request, Response. The Editor's `messageType` is `draco dreams user Request` and its message body
is one branch, `case latent draco.dreams.user.Latent [ … ]` — the corpus's first cross-package case dispatch —
rendering `case latent: Latent =>` plus the generator's own `case _ => ()` catch-all (a Request with no branch
is a no-op). Read and Delete join as further branches without touching the message type again; in Haskell,
Request is the sum type and the Editor a function over it. Two engine gaps closed on the way: the case-head
parser took every lower-case-initial token as the binder, so a DOTTED type lost its binder and became the
branch's name (the type slot is a value type: a token whose last segment is upper-case is the type now); and
`externalImports` scanned only an action's top-level statements, so a `[T]+` inside the branch lost its
`mutable` import (the scan looks one level down). DrakeParseTest +1, DreamsTest +2; the gates are byte-identical
over the main corpus.

**Read: one domain by name, answered with an Actual (2026-10-13, Read's second design point; 799e9ae, Dev's full
run 747 / 54).** Dev accepted "Read naming one domain answered with an Actual". Two facts bounded it: a
domain is named as definitions are keyed (name + package, parameters free), and the ontology has no codec, so a
user reads it one domain at a time. `draco.dreams.user.Read` derives Request and carries `fix domain TypeName`;
User holds Latent, Read, Request, Response. The Editor's second branch, `case read draco.dreams.user.Read [ … ]`,
binds `owner` as `presence(accepted.keys.find(namePath ==))` and answers through the SAME Response consumer:
`owner.fold(Actual(stub definition naming the domain, [], [one Problem]), \d -> Actual(d.typeDefinition,
d.typeDictionary.elementTypes))` — the domain as the ontology holds it when owned, a refused Actual naming it
when not. A Read never touches the state and never delivers an ontology; two consecutive cases are one
dispatch. Nothing new on the Editor's factory or in User's response vocabulary. Rejected: a new response type
for reads; reading a member by name; one answer carrying the whole ontology. EditorTest 17 (+1), DreamsTest 16
(+2).

**Delete through `excluding`, the judgment shared (2026-10-14; f4336a4, committed by Dev after the gate — the
run's output was not pasted, 751 / 54 inferred).** Dev
accepted "Delete through excluding". Core gains `DomainOntology.excluding(typeName)`, the inverse of `including`
(by name and package, parameters free; the original untouched; an unknown name removes nothing).
`draco.dreams.user.Delete` derives Request with `fix domain TypeName`; User holds Delete, Latent, Read, Request,
Response — the CRUD vocabulary complete. The Editor's third branch builds `accepted.excluding(delete.domain)`,
judges it by the same five rules, swaps the state only when quiet, answers with the domain as the ontology held
it (a stub naming it when not owned) and delivers the shrunk ontology on acceptance. Three outcomes without a
new rule: removed when nothing depends on it; refused naming the DEPENDENT when something derives from it
(DerivationResolvable over the whole result); refused with one Problem when not owned. Draco itself cannot be
deleted while anything derives from its root. THE JUDGMENT IS ONE GLOBAL: `dyn judge [Problem]` in the
Editor's `globals` (parameters `rules`, `provisional`; the session lines; `fix value problems.toSeq`) — the
first `globals` on an actor-minting type, rendered as a companion `def` visible inside the minted actor — called
by the Latent and Delete branches. The emitter orders `factory` before `globals`. DomainOntologyTest +1,
EditorTest +1, DreamsTest +2; DracoGenTest confirms the regenerated core type byte-identical.

**Create and Update, the verbs over the Latent shape (2026-10-14; 78bff9d, Dev's full run 768 / 54 together with the
next paragraph).** Dev
asked why User had Read and Delete but not Create and Update. Only history: Latent was named for its content
before the CRUD view, and Create/Update had been mapped onto it with the ontology deciding by identity — so a
Create of an existing name was a silent update and an Update of a misspelled name a silent creation. Dev
accepted "Create and Update deriving Latent". `Latent` is now the candidate SHAPE: elements `definition` and
`members`, NO factory, abstract as Response is, deriving Request; `Create` and `Update` derive it, each with a
factory. The Editor's Latent branch became two: Create is refused when `holder(accepted, identity)` is present
("already in the accepted ontology"), Update when it is absent ("not in the accepted ontology"), and both are
otherwise judged through `including` by the shared `judge`. `holder(ontology, typeName)` is the Editor's second
global, the Presence lookup all four branches share. Latent has no codec now, so the client's `domains`,
`order`, `ordered` and `run` are typed by `Create` (it builds from an empty ontology); `owns`, `parents` and
`ready` keep the shape. User holds Create, Delete, Latent, Read, Request, Response, Update — in Haskell four
constructors over one shared shape. EditorTest +1 (the intents test), DreamsTest +4.

**Editor-domain rules, the actor thinned (2026-10-15; same run, 768 / 54 at 78bff9d).** Dev observed that the dreams domains
carried no rule aspects, only an actor firing core's five rules — and the Editor had meanwhile grown judgment of
its own in actor code (the intent lookups folded inside four case branches, the whole-ontology judgment as a
global). Dev accepted "Editor-domain rules with the actor thinned". Core: `DomainOntology.holds(typeName)` and
`holding(typeName)` — a domain by identity, as a Boolean and as a Presence. User: `Request` declares three
bodiless dyns every verb implements — `proposal(accepted)` (the ontology the request asks for: `including` the
candidate, `excluding` the named domain, or the accepted ontology itself for a Read), `subject(accepted)` and
`contents(accepted)` (what the Actual reports on) — so a request knows what it asks for. Editor: `Accepted`
(the accepted ontology as a FACT, distinct from the raw provisional `DomainOntology` the core rules join — ONE
DomainOntology fact in working memory, never two, or every `var o DomainOntology` rule fires twice) and four
RULES, `CreateHeld`, `UpdateUnheld`, `ReadUnheld`, `DeleteUnheld`, each `var x <verb>` joined with `var a
Accepted` on `a.ontology.holds(identity)`, inserting the Problem. The actor is the MEMBRANE: nine patterns
build the knowledge; per message it binds `provisional`, `subject`, `contents` and `changing` STRICTLY (`now
fix`, before the swap), inserts the request, the Accepted fact, the provisional with its domains and
definitions, fires, swaps only when quiet, delivers the Actual and the ontology only on an accepted change.
No case, no globals: which rule matches is the dispatch. ENGINE: codec eligibility is TRANSITIVE —
`namesCodecless` loads a parameter's draco type and asks `hasCodec` with its loaded parent chain as family
context (§6 entry closed); DracoGenTest byte-identical. EditorTest +11 (trios ×5, the rules in a bare
session), DomainOntologyTest +1. Next, in Dev's accepted order: generation and activation as explicit
operations.

**Explicit generation (2026-10-15, Codex; Dev verified 782 / 55).** Dev accepted Generate → Generated,
then factory-supplied executable transform bindings. `user.Generate` derives Request and carries `domain`
and `transform` TypeNames; its proposal is the accepted ontology unchanged. `editor.Generated` derives
User's Response and carries those names, Emissions and inherited Problems. Editor's optional fourth
factory argument is `[TransformBinding]` (empty by default, so initialization still supplies none).
A binding supplies a transform domain, its super-domain snapshots and `(TypeDefinition, RuleType)` pairs:
RuleType's own definition identifies the generic Rule, so the concrete definition is explicitly supplied
by the caller as its association with the executable. This is a trusted executable binding, not proof
that arbitrary code implements a definition. `matches` checks source TypeDefinition, a named target,
held domain/member snapshots, super-domain coverage and the complete rule-definition set, including
inherited rules. GenerateRequested is the fifth Editor rule; it refuses missing names, absent/ambiguous
bindings or mismatched snapshots with Problems and queues a response function. The actor invokes that
function after the validation session has closed and Problems are collected; no case dispatch is added.
Only a quiet result runs the binding's rules in a fresh default-mode session with a received buffer,
exactly the GenDrakeTest chain; one Emission per domain/member definition is required. Exceptions and
count mismatches become a refused Generated with no partial emissions; the transform session closes in
finally. GenDrake is the first supplied binding; GenScala still awaits migration. Acceptance never runs
it; generation writes nothing, activates nothing, changes no ontology and delivers none. The transitive
codec check now normalizes referenced definitions and parent types before judging them, so a codable
Emission carrying neutral Text remains codable inside Generated. No main-corpus definition changed. Probes: 72 tests / four dreams suites and 537 tests / six
engine/corpus suites passed, measured headlines unchanged; Dev's full gate then passed 782 / 55 (768 + 14), zero failed or aborted suites, with every
report-only headline unchanged. The file-only pending-actor count is 0, read directly by Codex
after Dev's shell lacked rg. §1 records the verified baseline; Dev committed and pushed the increment as 9185f1a.

**Dracon wrapper and explicit codec capabilities (2026-10-15, Codex; Dev verified 804 / 56, reported 2026-10-16).** Dev accepted
Dracon's ownership of neutral value/codec vocabulary, target-domain implementation bindings, replacement
of `draco.format.json.JSON` by `draco.format.dracon.Dracon` as the combined wrapper/format domain,
explicit capabilities and their signatures: Encoder(T) supplies T -> Dracon; Decoder(T) supplies
Dracon -> Presence(T). The six-form family proposal was not accepted. Dracon and Value trios move to
format/dracon; Encoder and Decoder are two new function-valued capabilities there; ScalaTarget gains
CirceCodecs to bind explicit Circe instances to them. Dracon's raw Json field and cursor selection
stay inside the wrapper seam; its select returns Presence(Dracon), with invalid indices/missing paths
Absent and JSON null Present. **Implementation choice:** generic Format.Value(F) selects Presence(F),
removing its host Decoder bound without importing a child-domain capability into Format; Dracon.Value
adds decode(T)(source, decoder). A missing path never evaluates the decoder. Example message/actor
trios and initialization now name Dracon. JSON wire syntax and .json carrier paths remain; .dcon is
deferred. The core-tree migration below supersedes the remaining raw Json field types;
the payload declaration below is implemented; broader codec-field vocabulary and target lexeme tables remain later design points.
The complete direct probe ran 804 tests: 797 passed, seven ZeroMQ socket-bind failures in the sandbox;
the nine-test loopback suite then passed with local TCP access, including all seven failures. Dev subsequently reported a passing full sbt
gate on 2026-10-16: **804 / 56**, zero failed or aborted (three net-new corpus types ×5 + seven DraconTest tests); direct Runner counts 57
with its discovery wrapper. Parse scope moves to 108 + 10, one loss across 118, aspect sections 108,
GenDrake 106/106; all other report-only headlines unchanged. The table above carries the new measured
counts, all confirmed by Dev's full run, including zero pending mods actors in the file-only log.

**Core trees and payload bindings (2026-10-17, Codex; Dev verified 809 / 56).** Dev accepted
migrating TypeElement.value/valueType, TypeName.typeParameters and DracoAspect.derivation to
Dracon while preserving definition JSON, then explicitly accepted payload codec bindings.
The TypeElement factories and both aspect renderers move with their consumers. Dracon provides
neutral string/array/record construction, null/object/field inspection and nullable text/list
inspection; structural equality preserves TypeName identity. TypeName.read/write carry its
shape without Circe names; DracoAspect parent inspection and TypeLoader rooting use them.
CirceCodecs supplies explicit payload capabilities and host bindings. The handwritten generator
initially bound the scoped core fields through a bootstrap table; the next increment below replaces that table with declarations.
Ordinary Dracon codecs still encode the wrapper object. The host parser/generator and fixtures
opt into scalatarget.DefinitionTrees conversions at their boundary; no implicit payload codec
is installed. The legacy hand-written Generated.scala bootstrap (no definition trio) also uses
neutral constructors. Generated definition-backed Scala remains pinned by the corpus gates.
Direct full probe: 802 of 809 passed, seven sandbox socket-bind failures; the unchanged nine-test
loopback suite then passed with local TCP access. Five new DraconTest cases, no new corpus types:
Dev's full gate then passed **809 / 56**, zero failed or aborted. Old/new codecs reserialize all 182 current main/mods definition
inputs identically. Every report-only headline stays unchanged; DrakeCLI retains only the known
ActorAspect/BodyElement authored-ahead drift. Dev's pasted full-run headlines confirm every
figure, including zero pending mods actors; **809 / 56** is the new verified baseline. Record: `git-record-2026-10-17-1457`.

**Declared payload fields (2026-10-18, Codex; Dev verified 815 / 56 on 2026-10-19).** Dev accepted ownership
in the codec aspect and the syntax `codec payload [field, otherField]`, mapped to
`codecAspect.payload: [Text]`. The default is empty; a nonempty list makes CodecAspect
nonempty. TypeName declares typeParameters, DracoAspect declares derivation, and TypeElement
declares valueType/value. Their Scala projections remain byte-identical; only CodecAspect's
generated Scala changes. The generator reads the lists and inherited owner bindings instead
of a core-name allowlist. Target execution still binds through ScalaTarget/CirceCodecs.
A listed scalar Dracon encodes its payload; a listed sequence encodes each payload; unlisted
fields keep their encoding. Duplicate/malformed lists fail at parse; unknown/incompatible
fields fail at generation. Shared sealed-family fields retain their owner's representation.
Payload-only aspects remain in every corpus gate; discriminator/encoder/decoder Drake syntax
is still unsupported. Existing inferred codec eligibility is not replaced by selector semantics.
Final direct full probe with TCP access: **815 passed, zero failed or aborted** (57 suites
including the Runner's discovery wrapper); Dev's full sbt gate on 2026-10-19 confirmed
**815 / 56**, zero failed, canceled, ignored or pending tests and zero aborted suites; six new tests and
no new corpus types. All report-only figures unchanged. Of 182 reserialized definition inputs,
only the three owners gain payload metadata; existing wire fields are unchanged. Main DrakeCLI
check retains only the known authored-ahead drift. The verified full baseline is now 815 / 56. Record: `git-record-2026-10-18-1731`.

**Method formal parameters (2026-10-19, Codex; Dev verified 825 / 57).** Dev accepted separating
method names from formal declarations, then TypeParameter(name: Text, upperBound: Dracon,
lowerBound: Dracon), bounds absent as Dracon.Null. Dynamic adds typeParameters [TypeParameter],
default empty. Bounds carry existing type-form trees, not host parameter text. Drake retains
updated(V1 >: V), while JSON carries name "updated" and a formal record. Scala alone assembles
its bracketed signature. All ten name-embedded method declarations migrated; their Drake
surfaces and Scala signatures stay unchanged. TypeName's header/actual arguments remain
[Dracon]; this does not implement the older flat-atomic proposal for that field.
Parser/projection reject duplicate/invalid formals, malformed bounds and legacy generic
Dynamic names. Context bounds are not introduced; codecs remain explicit capabilities.
A new corpus type adds five corpus tests; MethodTypeParameterTest adds five focused tests.
Direct full probe: 825 passed, zero failed or aborted (58 suites including discovery);
Dev confirmed the full sbt gate **825 / 57** on 2026-10-19, zero failures or aborts. Parse scope 109+10; one loss across 119; aspect sections
109, GenDrake 107/107, zero pending mods actors, every other report headline unchanged.
The table reflects this verified corpus growth; the full baseline is now 825 / 57. DrakeCLI retains only the two known authored-ahead surfaces.
Record: `git-record-2026-10-19-1254`.

**Ontology-scoped names (2026-10-20, Codex; Dev gate verified).** Dev accepted an explicit
DomainOntology per operation, local self/type/method formal bindings before unique ontology
lookup, and an explicit primitive/host binding vocabulary alongside it. The hand-written
NameResolution engine resolves structural type forms and nominal references, reads scoped
JSON and produces compact JSON. Scoped Drake.parse/emit and DracoGenerator.generate overloads
use it; Scala adds selective imports when shortening does not collide with imports or locals.
Dreams Generate receives the current held ontology, with TransformBinding.nameBindings as
explicit alias/TypeName pairs (empty default, duplicates rejected). Definitions resolve before
rule registration/execution; resolution failures use Generated's Problems, with no emissions.
GenDrake emits through the scope supplied by the session; legacy standalone sessions preserve
their prior behavior. No implicit global ontology, resource discovery or host-name acceptance.
Host-opaque expression spans remain opaque; structured symbolic leaves resolve with local
binders respected. Qualified bootstrap files/loader and unscoped APIs are retained; selecting
and supplying bootstrap loader scope is a subsequent design point before bulk shortening those
files. No Haskell target implementation. Nine focused tests and one Dreams integration test
are added (Dev gate 835 / 58); no corpus type or count change. Final full direct
ScalaTest probe passed 835 tests, zero failures/cancellations/aborts (59 suites including
Runner discovery), with every report-only figure unchanged and file-only pending mods
actors 0. Dev's full sbt gate on October 20 confirmed 835 / 58, zero failed/canceled/ignored/
pending tests and zero aborted suites, with every headline unchanged.
Dev committed and pushed b17ddb8 on October 21.
Record: `git-record-2026-10-20-1503`.

**Two-stage loading from explicit domain declarations (2026-10-21, Claude Code; Dev verified 839 / 59).** Dev accepted
the loader as restated from Codex's October 21 proposal: "a DomainBuilder operation over explicit domain
declarations and bindings, identity stage from placeholders, body stage through NameResolution, bootstrap
files untouched in this increment." `DomainBuilder.identities(declarations, path)` is STAGE ONE: the
declared domains' own definitions are read as authored and composed as `Domain(definition)` with no
members, so core's `TypeDictionary` places a name-only placeholder for every listed member; a declaration
nothing defines, or one that does not self-declare as a domain, is refused outright. The anchors need no
scope because they are qualified by construction — a listed member's identity is the domain's package plus
its bare name. `DomainBuilder.load(declarations, bindings, path)` is STAGE TWO: a `NameResolution` over
that identity-only ontology and the explicit vocabulary resolves every body, the domain definition's and
each member's, and the populated ontology is composed from the resolved definitions, strictly (a factory
argument is by-name, so an unknown name must fail in `load`, not on the first read of a dictionary — §6).
`path` is an explicit `DefinitionPath`, default the host roots; nothing is discovered, and the generated
companions keep loading unscoped through `TypeLoader`, which main's tier never changes. MEASURED before
building, with a read-only probe over the 18 canonical domains (132 definitions, no stubs): the
identity-only ontology is, as a resolution scope, identical to the populated one over every definition
(NameResolution reads identities only); the corpus needs a 42-name host vocabulary to resolve, converging
in five rounds; one field no resolution can read — ActorAspect's authored-ahead `ActorAspect => Boolean`,
the surface-loss headline's one type form — so `NameResolution.form` now PRESERVES a string leaf that is
not a name as host-opaque type text, as it already preserved host-opaque expression spans (the one engine
adjustment the corpus forced); and resolution rewrites 93 of 132 definitions, 316 leaves by same-package
qualification (today's JSON carries value types as authored: bare in the own package chain, qualified
elsewhere) and 60 bare cross-package names given their unique identity — among them TWO CAPTURES that
change meaning, the host unit type `Unit` (drake.dlt's leaf in every consumer arrow) resolving to
`draco.base.Unit` in Editor and Initialization, and CLI's bare `Drake.emit` resolving to the Drake
domain's definition instead of the hand-written engine. Those two captures and the bootstrap vocabulary
are the NEXT design point, before any bootstrap file is shortened; `Local` being the one TypeElement kind
absent from Draco's `types` list was observed in passing, not filed. `DomainLoadingTest` (3) is new, the
59th suite — stage one against the populated ontology, stage two order-independent and equal to the
resolution of the unscoped load, and a fixture root whose body says `Dracon` loading qualified while an
undeclared domain stays unknown; `NameResolutionTest` +1. No corpus type changed, so no headline moves.

**The unit type is the empty Objective (2026-10-21, Claude Code; Dev verified 840 / 59).** The loader's measurement
became the next design point: under the accepted ontology-first rule, every consumer arrow `(M -> Unit)` in
a scope declaring Base resolved to the Base marker `draco.base.Unit` (Cardinal, Nominal and Ordinal derive
it). Dev accepted "the unit type as the empty Objective, written (), rendering Unit in Scala, with the
corpus re-authored and Base's Unit kept." `Drake.parse` reads `()` alone in a type slot as `{"(,)": []}` —
the empty product, Haskell's unit type, and already drake's unit VALUE — and the ScalaTarget spells the
empty Objective `Unit`, so every generated Scala file is byte-identical; `Unit` is no host leaf and no
vocabulary binding. Thirty sites in twelve drake sources were re-authored and re-canonicalized, seven
JSON-only example definitions carry the tree by hand; the one `Unit` left is inside a host-opaque
expression on the rete seam of the OriginateReports, which resolution never reads. CLI's bare `Drake.emit`
is qualified `draco.Drake.emit`, the engine as Emit spells it, and CLI.scala regenerated on that one line.
DrakeParseTest +1 (the form), DomainLoadingTest pins the Editor's resolved arrow as `Response -> ()`. The
first full gate failed the two ASSEMBLY suites: `AssemblyValidator` recognised a consumer parameter by the
string `Unit` as its arrow's result — the third consumer of the type vocabulary beside the two renderers
(§6, again) — and now recognises the empty Objective.
The remaining bootstrap-vocabulary point before shortening any file: the 41 host names the corpus still
spells as leaves.

**The Scala target's host vocabulary is a definition (2026-10-21, Claude Code; Dev verified 845 / 59).** Dev accepted
"the bootstrap host vocabulary as a definition-backed member of draco.scalatarget carrying [(Text, TypeName)]
binding pairs, supplied explicitly to load and Generate, with Local listed as a Draco member."
`draco.scalatarget.HostBindings` sits beside CirceCodecs: four global lists — `primitives`, `collections`,
`seams` (circe, java, scala.io, scala.util, evrete, pekko) and `engine` (DracoGenerator, draco.Drake,
NameResolution, the hand-written realizations definitions reference) — concatenated as `bindings`, with
`vocabulary` their map, in the form `TransformBinding.nameBindings` already carries. Each pair is the name as
the corpus authors it and the identity the ScalaTarget realizes it as; a name a draco type shadows (Encoder,
Decoder, Knowledge, Source, Drake) is keyed QUALIFIED, since the ontology wins the bare form, and a qualified
reference resolves by identity whatever the key. It is supplied explicitly — `DomainBuilder.load` takes it as
its bindings, Generate through `nameBindings` — never defaulted, never discovered, so a Haskell target supplies
its own table for the same authored names and no definition file carries a target spelling. `Local`, the one
TypeElement kind absent from Draco's `types` list, is listed, so `draco.Local` resolves through the ontology;
DomainLoadingTest reads the vocabulary from the definition and pins that neither Unit nor Local is a binding.
One corpus type added: parse scope 110 + 10, one loss across 120 types, 110 aspect sections, GenDrake 108 of
108 (table updated in this commit). Shortening the bootstrap files is still a later step.

**Bootstrap bodies shortened, anchors qualified, through canonical scoped generation (2026-10-21, Claude
Code; Dev verified 845 / 59).** Dev accepted "shortening the bootstrap files' bodies against the canonical identity
ontology and HostBindings, with nominal anchors kept qualified, the Scala regenerated through the scoped
generator with selective imports, DracoGenTest made scoped, starting with the definitions that spell the
Dracon path." The SEVENTEEN main definitions that spelled `draco.format.dracon.Dracon` (the TypeElement
family's eleven kinds, TypeName, TypeParameter, DracoAspect, DomainAspectOf, DracoAspectOf, CirceCodecs) carry
the COMPACT form in their bodies — `fix typeParameters [Dracon]`, `Presence(TypeName)`, `Absent()` — with
every nominal anchor (own name, domain pointer, derivation record) still qualified, so the companions, the
engine's parent chains and the ontology's edges read them unscoped as before; `NameResolution.compact` now
keeps anchors in full under every policy. SCOPED GENERATION CANONICALIZES (the point this increment settled):
the engine reads a definition under its own conventions and keys several decisions on bare spellings (codec
elision, the payload check, the actor-minting factory), so resolving a definition in full before generating
broke them; `generate(td, scope)` instead spells the COMPACT form re-qualified only where the engine cannot
read a bare name (`DracoGenerator.readable`: a host primitive; a host-table name the host realizes — not a
draco type sharing the simple name, Decoder/Encoder, and never in the file's own package, where the standard
circe import outranks a member; an own-package name no sibling package or host class shadows, `draco.Generated`
being the hand-written case), generates it, and shortens exactly those identities back by selective import
where Scala's scoping allows. Output no longer depends on how an author spelled a resolvable name. Every
definition loaded during a scoped generation is spelled the same way through a thread-local scope in effect.
CONSEQUENCE: every definition-backed Scala file regenerated through the scoped generator — 35 changed, 25
main and the ten staged dreams/service files, gaining imports such as `import draco.format.dracon.Dracon` —
and every suite that generates corpus Scala (DracoGenTest, GenerateAndCompileTest,
GeneratorDefinitionToSourceTest, TypeDefinitionTest, DreamsTest, InitializationTest, EditorTest, ServiceTest)
supplies the scope explicitly through the `CanonicalScope` test helper, because the UNSCOPED engine misreads a
swept body's referrers (their first run failed twelve per-type gates); PrimesRulesTest, which WRITES its
generation into src/main, joined them after Dev's first full gate (842 / 845) showed it overwriting three
regenerated rules. Integer, System and Enumeration are bound BARE in HostBindings, the Scala target reading them
so, which keeps `import java.lang.Integer` out of the regenerated rules. No test added, no corpus type changed.
The remaining bodies of the corpus follow in a later sweep; the companions still load unscoped.

**The resolution record: `uses` (2026-10-21, Claude Code; Dev verified 849 / 60).** Dev accepted "the resolution
record: a definition carries a `uses` list of the identities its bodies name short, written by compact and
validated against the ontology, so an unscoped reader resolves the file from itself; the rest of the sweep
follows it." The gap showed itself first: the unscoped GeneratorCLI, generating TypeDefinition, loaded the
swept TypeName and misjudged its payload. `TypeDefinition` gains `uses [TypeName]` (default empty, elided)
beside `typeName`; `TypeLoader.rooted` and the generator's target-type rebuild carry it (the first did not,
and the record never reached the engine until it did — §6's rule about rebuilds, again). On the surface
`uses [ … ]` is a section of its own between the type section and the domain line, each entry in the anchor
spelling (`draco format dracon Dracon`), written by the compact form, never by hand; `uses` is reserved.
`NameResolution.compactJson` writes it — every ontology identity a body names short, other than the own
package, sorted — every scoped read VALIDATES it (an entry the ontology does not define is an error), and a
resolved definition carries none (every name in full; compact recomputes it). `NameResolution.recorded(td)`
is the scope a definition carries in itself: a placeholder domain per package of the record, no bindings,
LENIENT (a leaf the record does not name passes as written). The engine's UNSCOPED generation spells a recorded
definition through that scope exactly as a scoped generation would, and its loader does the same for a
recorded definition loaded with no scope in effect: for the seventeen swept definitions unscoped and scoped
generation are identical and equal the pinned Scala, and their referrers generate unscoped again; the
ontology is needed only to author and validate the record. An unswept file spelled in full still gains its
imports only under a scope — the rest of the sweep removes that asymmetry. `ResolutionRecordTest` (4) is
the 60th suite. No corpus type changed, so no headline moves.

**The corpus swept (2026-10-21, Claude Code; Dev verified 849 / 60).** Dev accepted "sweeping the rest of the canonical
corpus in one pass: every remaining main definition and the staged dreams and service definitions compacted with
their records, Scala byte-identical and gate-pinned, the example domains left out." 132 definitions are compact
— every main definition but the authored-ahead BodyElement and ActorAspect, and the 24 staged dreams and service
definitions — 58 carrying a record; the example domains stay out. The sweep taught the compact form three rules:
a HOST LEAF KEEPS THE AUTHOR'S SPELLING (the compact form never writes or shortens a binding's identity — eleven
definition-backed Scala files regenerate: the host imports gained at 0b8fc03 revert, and Decoder, Encoder and
Generated are spelled in full where a host name shadows them); an identity is shortened only when its
simple name is UNIQUE ACROSS THE ONTOLOGY AND THE VOCABULARY (Decoder and Encoder in the Dracon domain stay in
full; the draco.rete wrap types stay bare, their bare name being their realization); an ANCHOR'S TYPE PARAMETERS
follow the anchor's convention, own-package bare, else in full (`Primal(draco.drake.Text)`). Two corrections the
sweep forced: the engine's loader reads a record only DURING A GENERATION, so DomainBuilder and the companions
agree about every definition (TransformBinding.matches had refused the held GenDrake); the hand-written
`draco.Generated` bootstrap joins HostBindings' engine bindings, since it shadows `draco.dreams.editor.Generated`
through `import draco._`, and KnowledgeService's binding names org.evrete. Unscoped and scoped generation now
agree over the WHOLE main corpus (ResolutionRecordTest pins it), so the ontology is needed only to author and
validate; the test suites' explicit scope is a convenience. No test or corpus type added.

**Formals in the aspect, actuals in the name (2026-10-21, Claude Code; Dev verified 849 / 60 on 2026-10-22).** Dev accepted "header
formals as TypeParameter records in DracoAspect.typeParameters, with TypeName.typeParameters carrying actual
arguments only and a definition's own name keyed bare", closing the formal/actual question #51 held since
Codex raised it. `DracoAspect.typeParameters [TypeParameter]` is the aspect's first slot; the parser reads a
header's parameters as formals (through the method-formal reader) into it and leaves the definition's TypeName
bare; the implicit factory result applies the name to the formals' names. `DracoAspectOf` spells the header
from the aspect, so the drake surface is unchanged. The engine reads formals from the aspect wherever it renders
the definition's own name (`formalScala`, `formalsBracket`, `wildcardName`); references spell from their
TypeName as before. TypeLoader's rooting and the generator's rebuilds carry the slot — one blind regeneration
lost every formal before the regenerated loader was compiled first, §6's rule again. The 23 parameterized trios
are migrated by re-parsing their drake; every generated Scala file is byte-identical but for the three defining
trios. Identity does not move. No test or corpus type added.

**Handoff checkpoint (2026-10-22).** Chapters 100–104 preserve the Claude Code session of October 21–22 with a
bounded original-message extract: 27 complete exchanges plus the open close request, seven commits from b95419c
to 7b1bfea, each at a Dev-verified gate. Chapters 95–99 preserve the Codex session before it (35 exchanges plus the
handoff request). See DEVELOPMENT_HANDOFF.md and DEVELOPMENT_RESUME_PROMPT.md for either agent. #51's open
questions are CLOSED by that session (two-stage loading, the unit type, the host vocabulary, the swept corpus with
records, formals in the aspect). The next design point is OPEN and unaccepted: Presence-typed role slots so a
present-empty rule or actor survives the carrier, with the two authored-ahead surfaces (BodyElement, ActorAspect)
re-canonicalized as plain types and their exhibit moved into a round-trip test. Activation and .dcon migration
remain deferred; the example domains remained unswept until 2026-10-31 (below).

**#51 continuation.** The issue's priority-next label was scoped to the derivation edge
completed in August; it is not evidence that every proposal in its older comments remains
current. Type-form trees, neutral collection operators, Presence/Text and Dracon supersede
parts of that discussion. Structured method formals close its name-overflow gap. After this
increment's gate and commit, return to #51's remaining formal/actual representation and
neutral vocabulary questions with one design point for Dev; do not end at commit confirmation
or silently adopt the old flat TypeParameter migration. Activation and .dcon remain deferred.

**Deferred activation (Dev, 2026-10-15).** Dev asked to preserve Codex's unaccepted recommendation:
start an explicitly supplied Assembly of held actor definitions, using factory-supplied executable
constructor bindings matched to those definitions. Activation remains deferred while Dracon is developed;
do not treat the recommendation as authorization. See the current handoff.

**Retired — do not reintroduce, and treat any doc mentioning these as stale:**
the `parameters`/`par` CALL form (`f parameters par a`), `par = name` named arguments, `.member
parameters` chain lines and `[ ]` argument brackets (a call is `f(a, b)` since 2026-09-16), the
dyn `=` result marker, the Haskell-form `if … then … else` surface as a plan (`ifThenElse` is a
dyn on `Presence`, not a keyword), `eager` (the strictness word is `now`),
the `Actor(T)` derivation on an actor (a ROLE's parameter has no business being an edge in the
data inheritance tree — actor-ness is aspect presence, and the message type lives in
`actorAspect.messageType`), `TypeInstance`, `DomainInstance`, `RuleInstance`, `ActorInstance`, `typeInstance`,
`Extensible`, `DomainDefinition`/`RuleDefinition`/`ActorDefinition`, `TypeDefinition.load`,
`loadRuleType`/`loadActorType`, the `.rule`/`.actor` filename suffixes, YAML and the
`from-yaml`/`to-yaml` CLI subcommands, `DomainDictionary` (renamed `DomainOntology` on 2026-10-09; the word dictionary now names only a
domain's `TypeDictionary`), the `draco.language` domain, the reference-frame
`*centric` domains, `Alpha`/`Bravo`/`Charlie`/`Delta`, `PrimeOrdinal`, named
`Cartesian`/`Polar`/`Spherical` coordinates.

---

## 4. Build and test

```bash
sbt test                                            # full suite — the gate before any push
sbt "testOnly draco.DrakeParseTest"                 # one class
sbt "testOnly draco.DracoGenTest -- -z \"TypeName\""  # one test
```

Recommend the **full suite** before a push, not a scoped run — scoped-green is not
suite-green. Report the gate scope with every count.

`bin/draco-gen` (generate/compile/inspect/discover/verify) and `bin/draco-sc` are built by
`sbt assembly` and can be stale relative to the tree.

**Regenerating without sbt.** The compiled classes of the last `sbt test` run the CLIs directly
and byte-exactly: `java -cp "$(cat target/streams/runtime/dependencyClasspath/_global/streams/export)"
draco.DrakeCLI parse X.drake` (or `draco.GeneratorCLI generate X.json`, `generate-multi …`). Two
rules: overlay the CURRENT resources on a copy of `target/scala-2.13/classes` first — main AND
`src/test/resources` — because an actor's projection walks its domain chain through the loader,
and a stale or missing definition yields a stub, an actor with rules missing; and rebuild that
overlay after every run that recompiles the engine, or a regeneration silently changes nothing.
An engine change cannot be regenerated by the compiled engine: those commits take two runs (the
first fails the gate, the regeneration follows, the second is green).

---

## 5. Documentation status

Current documentation was reconciled through journal chapter 104 on 2026-10-22.
Dates below distinguish that reconciliation from earlier rewrites; always check
claims against the code rather than treating a historical audit as permanent validation:

- **`README.md`** — **rewritten and verified 2026-08-15, synced through journal chapter 104 on
  2026-10-22.** The canonical architecture doc, written in draco's own vocabulary rather than any
  target's, with a *Language-specific residues* table recording every place a host term still leaks.
- **`GETTING_STARTED_TARGET_*.md`** — **rewritten 2026-08-17**, one guide per target:
  `SCALA` (realized), `HASKELL` and `TYPESCRIPT` (stubs holding structure and open
  questions). One shared skeleton; only the toolchain, projection command, running, and
  command set are target-specific.
- **`AGENTS.md` / `CLAUDE.md`** — tracked symlinks to DRACO.md, verified 2026-09-23.
  Both agents read the same operating rules; edit DRACO.md, never the symlinks.
- **`DEVELOPMENT_HANDOFF.md`** — shared current-state handoff and documentation coverage;
  refresh it at a handoff or completed development increment, and verify it on resumption.
- **`CHANGELOG.md`** — **current.** Release blocks through alpha.6 (2026-08-17); `[Unreleased]`
  carries one entry per commit since, written with each commit's record, and was consolidated
  into one section each (Added / Changed / Fixed / Build) with a lead paragraph on 2026-09-09.
- **`drake.dlt`** — current and authoritative for the surface.
- **`HOLARCHY.md` / `ORION.md`** — aspirational; vocabulary largely absent from code.

**Related documentation.** `README.md` (architecture, for a reader outside the tree),
`CHANGELOG.md` (the fact of each change), `draco-dev-journal/` (the session transcripts; the
`## Status` section of each chapter is its historical closing state), `DEVELOPMENT_HANDOFF.md`
(current resumption checkpoint), `src/main/resources/draco/drake.dlt`
(the surface specification).

**Local setup.** `CLAUDE.md` and `AGENTS.md` are tracked symlinks to this file;
`.claude` is an untracked local symlink to `.draco`. Inspect before any repair. With Dev's
approval, create only missing links using `ln -s DRACO.md CLAUDE.md`,
`ln -s DRACO.md AGENTS.md` or `ln -s .draco .claude` as appropriate; do not force-replace
an existing file, directory or different link. Never write through a symlink: many tools
replace it with a regular file. Tool-specific material (Claude Code
auto-memory, `.claude`/`.draco` settings) lives outside this file.

---

## 6. Gotchas worth carrying

- **Expression vocabulary has more than two consumers.** Alongside Drake and DracoGenerator,
  the SourceContract prototype and its tests carry operator cases. Retiring the `if` node
  passed scoped probes but failed two SourceContractTest cases in the first full run
  (chapter 81). Search every consumer and retain the full-suite gate. The TYPE vocabulary has a
  third consumer too: `AssemblyValidator` read a consumer's message type as text, caught by the
  unit type on 2026-10-21 and by the compact spellings of the example sweep on 2026-10-31, when it
  began comparing by identity through the definition's record.
- **Evrete** compiles conditions as Java at runtime: fully qualified class names required.
  Working memory is boxed — rule variables use `classOf[Integer]`, not `classOf[Int]`.
  A single-fact insert needs `Seq(fact): _*`. Tuple facts need a `forEach` declaration.
- **Two rules in one knowledge must not declare fact types related by inheritance.** Evrete
  resolves a fact's type by walking its supertypes; when more than one declared type fits, it
  resolves NEITHER and **skips the insert** — the fact never reaches working memory, both rules
  stay quiet, and the only trace is a `java.util.logging` warning ("due to ambiguity" / "insert
  operation skipped"). Nothing throws. A single rule declared anywhere up the chain works fine —
  `SubtypeFactVisibilityTest` measures both halves. The corpus is safe only by shape: each medium's
  session declares SIBLINGS (`PositionReport` + `FlightIntent`), and draco's validation session a
  disjoint taxonomy (`Problem` / `TypeDefinition` / `DomainType`). GitHub #63.
- **Actors** are thin membranes: `session.insert(msg); session.fire(); Behaviors.same`.
  `Rule.knowledgeService` is a singleton; Knowledge is per domain, Session per actor.
- **circe 0.14.1 has no `java.net.URI` codec** — the codec gate excludes parameters whose
  type has no derivable instance, sourced from `externalTypeImports`.
- **Values must be single-line.** No value in the corpus contains a newline, and drake's
  surface is line-based; a multi-line value breaks the round-trip.
- **macOS filesystem is case-insensitive** — same-package names differing only in case
  collide, and resources clobber silently.
- Use ` ```text ` not ` ```scala ` for pseudo-Scala; IntelliJ injects a parser into the latter.
- **`now` is required where the target binds a signature.** A `dyn` that meets a host method
  (`TypeName.equals`, `Dictionary`'s `Map` operations, `CLI.main`) or that is used as a function
  value (`TypeLoader.readDefinition`, `rooted`) cannot take by-name parameters in Scala; mark
  the parameter `now par …`. Find these by what a type DERIVES (a foreign parent, an explicit
  `main`) and by method-as-argument uses, not by method names.
- **Evrete's `execute` takes Java's `Consumer`; a Scala function value is refused there, a lambda literal
  converts.** A rule's `pattern` and `action` are functions (`(Knowledge -> Unit)`, `(RhsContext -> Unit)`,
  2026-09-20), applied as `pattern(knowledge)`; the engine's rule template writes `execute (action(_))` at
  the one call Evrete owns. Never hand a function value to a Java functional-interface parameter.
- **A factory argument is by-name, so a hand-written caller sequences its own effects.** Since lazy
  step 3 (2026-09-18) `X(expr)` evaluates `expr` on first read of the instance, not at the call. An
  argument that consumes state — a token cursor, an iterator, a `var` mutated afterwards — must be bound
  to a `val` before the call. `Drake.parse` had six such sites and failed 133 tests on the first run.
- **When a slot's type changes, grep its named-argument construction as well as its reads.** Changing
  `DracoAspect.derivation` from `[TypeName]` to `[Json]` (2026-09-21), every `.derivation` reader was
  found and moved; every `_derivation = Seq(TypeName(...))` construction was not — `Generated.scala`
  (hand-written, no definition) and twenty-one test sites stopped the first run at compile.
- **`loc` inside an actor action is not a binding.** `actorActionBody` renders `fix` as `lazy val` and
  `mut` as `var`; a `loc` falls through to the bare-expression case and its name is simply lost —
  the generated `receive` then references an undefined name. Use `fix` in `start`/`message`/`signal`
  (the first Editor draft, 2026-10-09, rendered four dangling expressions this way).
- **Codec eligibility is transitive since 2026-10-15** (it was not: `hasCodec` refused only function-like
  and host-external parameter types, so a factory parameter typed by a codec-less DRACO type —
  `DomainType`, `DomainOntology` — emitted a codec that could not compile, and `Actual` derived its
  populated `domain` as a `dyn` to dodge it, 2026-10-09). `namesCodecless` now loads the parameter's
  draco type and asks `hasCodec` of it with its loaded parent chain as family context; both are normalized
  through `targetTypes` before the check (otherwise Emission's neutral Text is wrongly judged codec-less). The Editor's
  `Accepted(ontology)` fact was the first type to need it. The `dyn` on Actual stays, for a different
  reason: the domain is derived from the definitions, not carried.
- **A lambda inside a splat argument is host text.** `insert(xs.flatMap(\d -> …): _*)` is one glued
  token to the parser (the dot rule), and `\d ->` reaches the target verbatim; bind the collection
  first (`fix definitions … flatMap(\d -> …)`, then `insert(definitions.toSeq: _*)`), 2026-10-15.
- **sbt does not copy hidden resources.** `src/main/resources/.json` and `.drake`, the nameless
  domain's anchor, never reach `target/classes` (HiddenFileFilter), so `loadType(TypeName.Null)` is a
  stub under sbt and `MinimalTypeTest` relies on that. A compiled-class probe that overlays resources
  with `cp -R src/main/resources/.` copies them and fails that one test; delete the dotfiles from the
  overlay.
- **`x: _*` with a BARE name is a named argument to the parser.** `f(definitions: _*)` reads as
  `definitions:_*` — the `name:value` call form — and renders `f(definitions = _*)`; an expression with a
  dot (`definitions.toSeq: _*`, `msg.members: _*`) is a glued token and passes through. The drift shows in
  `DrakeCLI check` as a surface loss on that line (2026-10-12).
- **A re-canonicalization sweep de-trees what the parser cannot tree.** `DrakeCLI parse` writes
  back what it reads; an infix condition authored as a tree (`{"==": [{"*": ["i1","i2"]}, "i3"]}`)
  came back as the string `"i1 * i2 == i3"` until the parser treed infix operators (2026-09-21).
  Three conditions (`PrimesFromNaturalSequence`, `RemoveCompositeNumbers`, `SelfDeclaration`)
  lost their trees this way on 2026-09-16; the generated Scala is identical, so no gate saw it.
  The operator layer restored all three on 2026-09-21. What the parser still cannot tree — a host
  `if`, a block — a sweep still de-trees: read the diff of every sweep.
- **A qualified reference in a `from` header or a `messageType` line is SPACE-separated package words**
  (`type Actual from draco dreams user Response`, `messageType draco dreams user Latent`), like the domain
  line — not dotted. The dotted form parses silently as one package word and a mangled name; `DrakeCLI
  parse` shows it at once (2026-10-12). Value types and expressions stay dotted (`draco.dreams.user.Latent`),
  and so does a `case` head's branch type, which is a value-type slot: `case latent draco.dreams.user.Latent [`
  (the parser read the dotted token as the binder's name until 2026-10-13).
- **`now` is a reserved word everywhere, including as a binding name.** A body binding called `now` is
  read as the strictness marker; name the thing `wave`, `current`, anything else (2026-10-12).
- **A self-declaration is keyed by name and package, parameters free.** `Format(F)` self-declares as
  `domain draco format Format`, as its members point at it; a rule comparing the whole structural TypeName
  refuses the corpus's own parameterized domain and everything deriving from it (the endogenous run found
  it, 2026-10-12). The same principle as Uniqueness: parameters compose references, not definitions.

## 7. Claude Code / Codex handoffs

**Shared documentation skill.** When Dev requests journal/documentation maintenance,
read [.draco/skills/update-draco-docs/SKILL.md](.draco/skills/update-draco-docs/SKILL.md).
Both agents use this single version-controlled procedure and its link checker, even
when their tool does not automatically discover that directory. Do not create a
second agent-specific copy or require a .claude link to read it. DRACO.md takes
precedence in a conflict. Only the skill file and checker are allowlisted under
.draco; local settings, worktrees and other material remain ignored.

This protocol works in both directions and is independent of the selected model.
Changing the agent or model does not change project permissions or accepted decisions.
The repository is the shared continuity record; private chat histories and tool-local
memory are optional evidence, not prerequisites or hidden sources of authority.
Since 2026-09-27 Dev alternates development sessions between Claude Code and Codex as a
standing practice; the cadence is set by experience, so every session begins by receiving
a handoff and every completed increment ends by preparing one.

### Receiving a handoff

1. Read these rules and `DEVELOPMENT_HANDOFF.md`, then inspect the actual branch, HEAD,
   working-tree changes and relevant files. Do not reset, overwrite or assume ownership
   of another agent's uncommitted work. A clean tree and matching commit are observations,
   not permanent properties of a handoff.
2. Read the latest journal Status and only the chapters, git records or transcripts
   needed for the current task. Earlier sessions already covered by the journal need
   not be re-imported. If evidence is unavailable, record the gap rather than invent it.
3. Distinguish implemented, generated, compiled, tested, committed and pushed states.
   Record who ran tests, their code checkpoint, scope and report-only numbers. A result
   from before a relevant edit does not verify that edit; local remote-tracking state
   is not fresh proof of a remote push.
4. Summarize the recovered checkpoint and any discrepancies. Continue only the work
   Dev has authorized; proposed next steps and deferred decisions are not approvals.

### Preparing a handoff

1. Update `DEVELOPMENT_HANDOFF.md` with the agent/date, branch and observed HEAD, changes
   still in the tree, completed work, verification evidence, blockers, decisions and the
   next authorized action. Identify a destination if Dev has chosen one; otherwise leave
   the checkpoint available to either agent. Writing it does not launch another session.
2. Keep DRACO.md, README.md, CHANGELOG.md and the git record aligned with changes actually
   made. Shared decisions and durable lessons belong in these project files, not only in
   auto-memory. Do not overwrite another tool's private memory as a substitute for handoff.
3. Track journal coverage separately from code/documentation currency. Reconcile the last
   chapter's closing commit with git history, including changes from either agent. Name
   uncovered ranges and available sources. Do not silently skip older uncovered work to
   reach a newer Codex or Claude Code increment.
4. When writing journal chapters, follow the Introduction's Journal Conventions: source
   Dev's words verbatim and model replies near-verbatim, label Claude and Codex distinctly,
   preserve corrections, and summarize delegated command results with their provenance.
   Git records and diffs support factual summaries, not reconstructed dialogue. Missing
   transcripts must be disclosed; voice noise and duplicate delivery are not new decisions.
5. Update the chapter index when adding chapters. Advance `draco-docs-synced-through`
   markers only after reviewing the corresponding chapters against DRACO.md and README.md.
   Keep coverage gaps explicit; never advance a marker merely because code is newer.
6. Give Dev a concise handoff summary, the remaining verification or decisions, and any
   requested commands to run. Do not commit, push, run sbt, create an issue or contact
   another agent merely because a handoff is ready.

### Transcript integrity at handoff

Compaction summaries are working context, never quotation evidence. Before journal
updates, recover original message records and preserve a bounded project-local extract
with source identity, sequence, timestamps, text hashes and explicit exclusions. Report
user/reply counts and paired exchanges separately; do not count event mirrors twice.
Distinguish missing originals, editorial omissions and the currently unanswered turn.
Report observed compactions without attributing gaps to them absent evidence. Verify
older quotations when extending an already-journaled task, but preserve historical Status.
Prefer a new development task at a verified increment boundary after the handoff is
current, not solely because compaction occurred. No task is created automatically.

<!-- draco-docs-synced-through: chapter 112 -->

**Role presence increment (2026-10-22, Codex; Dev verified 857 / 61).** Dev accepted Presence-typed
rule/actor slots and their field-level encoding. Absent omits the key; Present writes its
aspect object even when empty. The generator supports this encoding for Presence of the
two role-aspect types, checking codec eligibility on their contained aspect; it does not
supply a general Presence codec. Parser, emitter, role dispatch, completeness, loader stub
checks, dreams binding validation and inspection scripts use the distinction. BodyElement
and ActorAspect are canonical plain types; the frozen fixtures and exclusions are removed.
Four RolePresenceTest cases cover absent/rule/actor/both through JSON, Drake and generation;
four restored corpus tests bring the expected gate to 857 tests / 61 suites. The surface-loss
baseline above changes to zero; Dev’s full gate verified 857 / 61 on its first run.

The final direct compiled-class probe passed all 857 tests, 62 suites including Runner
discovery, no failures or aborts. Main/mods and test resources were on loader roots. All
other report-only headlines matched, including zero pending mods actors. Dev subsequently
verified 857 tests / 61 suites with the same headlines and no failures or aborts.

**Executable empty-role coverage (2026-10-22, Codex; Dev verified 861 / 61).** Four additional
RolePresenceTest cases compile and load the absent/rule/actor/both projections after carrier
round-trip. Declared rules register and permit session firing; declared actors process a
String and an Int through BehaviorTestKit, staying alive without effects. Both roles work
in the composed projection. No production behavior or corpus change. The tests do not
measure invocation count of an empty rule action. All 861 tests passed directly, 62 suites
including discovery, every report-only headline unchanged. Dev’s first full gate verified
861 tests / 61 suites, zero failures or aborts and matching headlines.
Record: `git-record-2026-10-22-0739`. Latest Dev-verified baseline is 861 / 61; commit pending.

**Example membership (2026-10-23, Codex; Dev verified 873 / 62).** Dev accepted reciprocal
membership for sixteen existing example definitions after the audit at 375c513. Five domain
trios now list their already-owned members; their missing Drake surfaces were emitted and
parsed back. Existing hand-written example companions retain their conventions, changing
only elementTypeNames. No body sweep or projection repair: the example map stays 28 match /
20 differ. Two new consistency tests plus ten newly active parser checks bring the direct
run to 873 tests, 63 suites including discovery, all passed. Surface-loss coverage grows to
125 (zero losses) and parse scope to 110 + 15; all other headlines match, pending mods actors 0.
Dev’s first full gate passed 873 / 62 with all expected headlines matching; commit pending.
Record: git-record-2026-10-23-1048. Audit: EXAMPLE_DOMAIN_AUDIT.md.

**Example resolution bindings (2026-10-23, Codex; Dev verified 875 / 63).** Dev accepted
io.circe.HCursor in ScalaTarget HostBindings and AerialSink supplied explicitly by the
example caller. HostBindings' trio adds the qualified cursor binding; ExampleScope is a
test caller helper supplying canonical dependencies, six example declarations and the
qualified sink binding. ExampleLoadingTest gates all 48 definitions against direct resolution,
reversed declaration order, no stubs, and failure when either external binding is withheld.
No body sweep or projection repairs. Two added tests and one suite yield 875 / 63; all
report-only headlines remain unchanged in the full direct run: 875 passed, 64 suites
including discovery, zero failures or aborts, pending mods actors 0. Dev’s first full sbt
gate verified 875 / 63, zero failures or aborts and matching headlines.
Record: git-record-2026-10-23-1106.


**Aerial OriginateReport (2026-10-23, accepted; Dev verified 880 / 64).** Its Drake/JSON now
owns the existing report conversion and its Scala is regenerated. Exact projection and
normal/default runtime checks pass. Full direct verification: 880 tests / 65 suites
including discovery, zero failures or aborts, with current main/mods/test resources.
Dev’s first full sbt gate passed 880 / 64. That increment measured a 29/19 example
map, zero losses across 126 types, 110 + 16 parse scope; other headlines unchanged.
No failed/canceled/ignored/pending tests or aborted suites; all expected headlines matched.
Dev committed and pushed the Aerial repair as 3608fab.


**Marine OriginateReport (2026-10-23, accepted; Dev verified 886 / 65).** Its Drake/JSON owns
its existing conversion and Scala is regenerated. Exact projections and runtime checks
cover normal input, integer truncation and missing fields. Full direct verification passed
886 tests / 66 suites including discovery, zero failures or aborts, with main/mods/test
resources on loader roots. Dev’s first full sbt gate passed 886 / 65. That increment measured
30/18 examples, zero losses across 127 types and parse scope 110 + 17; other headlines
unchanged. That gate passed 886 / 65, with zero failed/canceled/ignored/pending tests
and zero aborted suites. Dev committed and pushed the Marine repair as bd6b7b3.


**Terrestrial OriginateReport (2026-10-24, accepted; Dev verified 893 / 66).** Its existing
conversion now lives in Drake/JSON and its Scala is regenerated. Exact projections and
runtime checks cover normal input, positive and negative truncation, and missing fields.
Full direct verification passed 893 tests / 67 suites including discovery, with zero
failures or aborts and current main/mods/test resources on loader roots. Dev’s first full sbt gate passed
893 / 66, zero failed/canceled/ignored/pending tests and zero aborted suites. That increment measured 31/17 examples, zero losses across 128 types,
parse scope 110 + 18; other headlines unchanged. Latest Dev-verified baseline is
893 / 66 at that checkpoint. Dev committed and pushed the Terrestrial repair as 97bf690.


**Ethereal OriginateReport (2026-10-25, accepted; Dev verified 900 / 67).** Its existing
conversion now lives in Drake/JSON and its Scala is regenerated, closing the fourth audited
conversion-helper gap. Exact projections and runtime checks cover normal input, positive
and negative truncation, and missing fields. Full direct verification passed 900 tests /
68 suites including discovery, zero failures or aborts, with current main/mods/test resources
on loader roots. Dev’s first full sbt gate passed 900 / 67, zero failed/canceled/ignored/pending tests
and zero aborted suites. That increment measured 32/16 examples,
zero losses across 129 types, parse scope 110 + 19; other headlines unchanged. Latest
Dev-verified baseline is 900 / 67 at that checkpoint. Dev committed and pushed e819663.


**World Cartesian (2026-10-25, accepted; Dev verified 908 / 68).** The tuple overload now lives
in the definition as a global apply method with a strict tuple parameter. Generated Scala
preserves lazy scalar arguments, defaults and Null and includes the accepted inferred
coordinate codecs. Six focused tests cover exact projections, evaluation behavior and codecs.
All discovered suites passed directly: 908 tests / 69 suites including discovery, zero failures
or aborts, current main/mods/test resources on loader roots. Dev’s first full sbt gate passed 908 / 68, zero failed/canceled/ignored/pending tests
and zero aborted suites. Current
headlines: 33/15 example map, zero losses across 130 types, parse scope 110 + 20; all others
unchanged. That checkpoint passed 908 / 68. Dev committed and pushed the Cartesian repair as 804c8a4.


**World Observable (2026-10-25, accepted; Dev verified 918 / 69).** Both geodetic helpers
now live in Drake/JSON with strict parameters. Generated Scala preserves lazy frames and
defaults and includes nested Cartesian codecs. ExampleScope explicitly binds the existing
Geodesy helper; the first direct run exposed this dependency in two loading failures.
After the binding correction, all discovered suites passed: 918 tests / 70 suites including
discovery, zero failures or aborts, current main/mods/test resources on loader roots.
Dev’s first full sbt gate passed 918 / 69, zero failed/canceled/ignored/pending tests
and zero aborted suites. Current headlines: 34/14 example map, zero losses across 131 types,
parse scope 110 + 21; other headlines unchanged. That checkpoint passed 918 / 69. Dev committed and pushed the Observable repair as 81154e3.


**World Provider (2026-10-25, accepted; Dev verified 925 / 70).** Its explicit World actor role
and existing forwarding action now live in Drake/JSON. Regenerated Scala preserves lazy
target capture, unchanged delivery and inert signals. Four focused tests and the existing
World assembly checks pass. Full direct run: 925 tests / 71 suites including discovery,
zero failures or aborts, current main/mods/test resources on loader roots. Dev’s first full sbt gate passed
925 / 70, zero failed/canceled/ignored/pending tests and zero aborted suites. Current headlines: 35/13 example map, zero losses across 132 types, parse scope
110 + 22; all others unchanged. That checkpoint passed 925 / 70. Dev committed and pushed Provider as 3648806.


**World Consumer (2026-10-25, accepted; Dev verified 933 / 71).** Its World actor role and
Position-to-Location case now live in Drake/JSON. Regenerated Scala preserves strict local
bindings, Observable conversion, rounding, ignored other messages, lazy callback capture
and inert signals. Five focused tests and existing World assembly/transform checks pass.
The Location call is positional; the named-construction qualification loss observed then was
repaired on 2026-10-26 (below). Full direct run: 933 tests / 72 suites including
discovery, zero failures or aborts, current main/mods/test resources on loader roots.
Dev’s first full sbt gate passed 933 / 71, zero failed/canceled/ignored/pending tests
and zero aborted suites. Current headlines: 36/12 example map, zero losses across 133 types,
parse scope 110 + 23; other headlines unchanged. Dev committed and pushed this increment as a8b5447 after the 933 / 71 gate.


**October 26 handoff.** All accepted work through a8b5447 is verified and committed by Dev.
DEVELOPMENT_HANDOFF.md contains the current checkpoint, commit ledger and direct-run procedure.
The named-construction qualification repair it preserved as unaccepted was accepted and built
the same day (next paragraph). Documentation currency
is through this handoff; chapters 105–112 now cover the session from original records, including
the prior Claude closing exchange and the correction of the initially omitted journal work.
The source audit verifies recoverability across both observed compactions; its frozen boundary
is Dev’s acceptance of this repair, excluding the repair’s final reply. Private agent memory
is not a prerequisite.


**Named construction spells its head (2026-10-26, Claude Code; Dev verified 936 / 72).** Dev accepted
"spell the named-construction head as authored/resolved, same as the positional path, with one
cross-package compile-and-run test checking scoped and unscoped generation", after the reading was
verified by a direct probe: `DracoGenerator.constructionInitializer` spelled the SLOT TYPE's last
segment, so a Consumer variant constructing `domains.terrestrial.Location(latitude:…)` in
domains.world generated bare `Location(_latitude = …)` and scalac refused it; the positional path
renders the head verbatim, which is why the accepted Consumer compiles. The head is now
`expression(operands.head)` — as the author or the scope in effect spelled it; unscoped a
cross-package construction keeps its qualification, scoped the engine shortens it to the bare name
and adds the selective import. The factory check and the `_name` mapping are unchanged. MEASURED
before building: only three definitions carry a named argument, all examples — Input authors its
own package's Position bare, Output is not intercepted (LocationReport has no factory), Observable's
construction sits in a dyn body — and old and new engine generate byte-identical Scala for all
three and for Consumer, so no committed file regenerates. Two adjacent observations surfaced, NOT
recommended or accepted: the method keys its factory lookup on the slot type rather than the head,
and a dyn-body result never maps named parameters to the factory's `_` convention (Observable
authors `_geocentric` by hand, a Scala spelling in a drake source). `NamedConstructionTest` (3) is
the 72nd suite: the probe constructs Location inside a global dyn, the generated file imports only
`draco._` and `domains._`, both generations are compiled with the runtime compiler and run. Full
direct run 936 / 73 including discovery, every headline unchanged; Dev's first full gate 936, zero
failed/canceled/ignored/pending, pending mods actors 0. Committed and pushed by Dev as e241378.


**A dyn's result is intercepted as a named construction (2026-10-26, Claude Code; Dev verified 937 / 72).**
Dev accepted "route a dyn's result (body `fix value` and leaf) through constructionInitializer with
the existing expression fallback, re-author Observable's `_geocentric:` as `geocentric:` with its JSON
re-canonicalized and Scala byte-identical, and add one NamedConstructionTest case." A dyn's RESULT had
rendered through the plain expression, so a named argument reached Scala without the factory's `_`
prefix and Observable's `fromGeodetic` authored `_geocentric:` by hand. MEASURED first: 86 `fix value`
results, one a named construction, four Text-typed PATHS (`rendered.value` and the like) that the full
initializer's String-slot rule would have quoted — so the route is `dynResult`, the construction
interception with `expression` as fallback, at all five result sites, never `defaultInitializer`.
ONE ENABLING CHANGE, flagged with the gate: the interception looked up only a DOTTED slot type, and
Observable's result is `fix value Observable`, so the generator now keeps `definitionInEffect`, a lazy
thread-local set while a definition is spelled (`generateSpelled` → `spelledSource`), and a bare slot
type resolves against that definition's package, as `namesCodecless` resolves a bare parameter type;
unscoped and scoped generation agree, canonical spelling having qualified such names already. Only
Observable.json moved (one leaf); Observable, Input, Output and Consumer regenerate byte-identical.
NamedConstructionTest +1 (body and leaf result, compiled and run), its own-package case authoring the
slot type bare. Preserved then, built next: the interception keyed its factory lookup on the slot type, not
the applied head. Direct run 937 / 73 including discovery; Dev's first full gate 937, every headline
unchanged, pending mods actors 0. Committed and pushed by Dev as 9779e39.


**The head decides the construction (2026-10-27, Claude Code; Dev verified 938 / 72 on 2026-10-29).** Dev
accepted "key constructionInitializer's factory lookup on the applied head (dotted as spelled, bare against
the definition in effect) instead of the slot type, and add one NamedConstructionTest case with a parent-typed
slot, compiled and run." The interception had loaded the SLOT type to ask whether a named application was a
factory call; a probe `dyn make domains.terrestrial.Terrestrial domains.terrestrial.Location(latitude:…)`
rendered `Location(latitude = 1.5, …)` unmapped, Terrestrial being a domain with no factory. The lookup now
asks about the HEAD leaf — dotted as spelled, bare against the definition in effect, a non-name head no
construction — and `constructionInitializer`, `dynResult` take the value alone; the slot type plays no part,
as it never did on the positional path. Corpus impact nil by construction: at the three named sites and in
Consumer the head names what the slot named, all four byte-identical. NamedConstructionTest +1 (the
parent-typed slot, compiled and run, the result a Location). This closes the three observations of October 26;
nothing from the named-construction thread remains preserved. Direct run 938 / 73 including discovery on
2026-10-27; Dev's first full gate 938 on 2026-10-29, every headline unchanged, pending mods actors 0.
Committed and pushed by Dev as 112f277.


**Terrestrial Output carries its Location branch (2026-10-29, Claude Code; Dev verified 942 / 73).** Dev accepted
"re-author Output's message as a case branch binding location to domains.terrestrial.Location, with the
three strict bindings and consumer(report), the report constructed in the OriginateReport form,
Output.scala regenerated from the definition, and one OutputProjectionTest covering exact trio agreement,
one report per Location, ignored other messages and lazy consumer capture." Output was the one example
difference the audit found to be more than spelling: its message body read `location` with no binder (the
hand-written `case location: Location =>` never reached the definition) and constructed the report as a
named application of factory-less LocationReport, which the engine passes through unmapped and Scala has no
apply to meet. The body is now `case location Location [ … ]` — bare, Location being in Output's own
package — with `now fix payload`, `now fix report LocationReport new LocationReport { … }` (host text, the
form OriginateReport carries since 97bf690; two bindings, the accepted "three" counted the consumer line)
and `mon consumer(report)`. JSON re-canonicalized, Scala the generator's output: the dispatch with the
generator's `case _ => ()`, and the Terrestrial actor convention the hand-written file lacked
(`elementTypeNames`, a `knowledge` registering the domain's two rules, as Creator has). No engine change;
LocationReport kept no factory then, a `json` factory on it being adjacent and unrecommended (accepted and built the
next day, below). OutputProjectionTest
(4) is the 73rd suite; the first direct run failed its lazy-capture case through the TEST's own strict
helper parameter, corrected before the second. Direct run 942 / 74 including discovery; Dev's first full gate
942, every headline matching. Example map 37 match / 11 differ (table updated in this commit); every other
headline unchanged, pending mods actors 0. Committed and pushed by Dev as c5b8089.


**LocationReport constructs through a json factory (2026-10-30, Claude Code; Dev verified 947 / 74).**
Dev accepted "Build the LocationReport json factory with both terrestrial sites re-authored" — the
recommendation being LocationReport's first drake surface carrying `factory parameters par json Json`, its
JSON re-canonicalized, its hand-written companion replaced by the generator's output, the two terrestrial
sites (OriginateReport's action, Output's Location branch) re-authored `fix report LocationReport
LocationReport(payload)` and regenerated, one LocationReportTest, the three sibling reports to follow. Five
definition sites built a report as the host-opaque `new X { override lazy val typeDefinition …; override
lazy val json = payload }` because the four report types (LocationReport, PositionReport, FixReport,
EphemerisReport) were JSON-only, no surface, no elements, no factory. A Dracon carries `json` and Dracon's
own factory is `par json Json`; the derived report's factory is the same constructor. PROBED first with the
compiled CLIs: the BARE `Json` spelling (HostBindings' key, Dracon.drake's own) generates `apply(_json: =>
Json)`, the Dracon-shaped codec and `Null = apply(Json.Null)` with `import io.circe.{Decoder, Encoder,
Json}`; the qualified spelling generates a `null.asInstanceOf[io.circe.Json]` Null and was not used. Both
sites regenerate with exactly one line changed (`lazy val report = LocationReport(payload)` in the rule,
`val report = LocationReport(payload)` in the strict branch); the companion's trait, kind-vals and loader
line are unchanged; tests' hand constructions stay valid. No engine change. LocationReportTest (3) is the
74th suite — the trio, the by-name argument evaluated once on first read and never when unused, the codec
round-trip; the existing OriginateReportTest and OutputProjectionTest pin the regenerated sites unchanged.
The new surface enters the parse scope (110 + 24) and the surface-loss coverage (134), activating two
DrakeParseTest checks; the example map stays 37 / 11, the three files being the generator's output (table
updated in this commit). The first direct run stopped at test compilation on an accessor in the new test
(`TypeForm.text(valueType.json)`), corrected before the second; Dev's first full gate 947, every headline
matching, pending mods actors 0; committed and pushed by Dev as e138f04. Adjacent then, built next: the three
sibling reports by the same pattern; the four *Intent types share the shape but no definition constructs them.


**The sibling reports take the json factory (2026-10-30, Claude Code; Dev verified 956 / 75).**
Dev accepted "Build the three sibling report factories in one increment": aerial PositionReport, marine
FixReport and ethereal EphemerisReport each gain the surface `type X from <Medium> draco format dracon Dracon`
with `factory parameters par json Json`, JSON re-canonicalized, companion replaced by the generator's output
(27 lines added, none removed: `apply`, the Dracon-shaped codec, `Null`), and each medium's OriginateReport
re-authored `fix report X X(payload)` and regenerated on that one line — all probed with the compiled CLIs
before the recommendation and built as probed, `DrakeCLI check` clean over the three media. No engine change.
`ReportFactoryTest` (3) is the 75th suite, one suite because the three definitions are one pattern, each case
iterating over the three: the trio, the by-name argument, the codec round-trip; the four OriginateReportTests
pin the regenerated rules unchanged. Every report in the example chain now constructs through its definition,
closing the thread Output's unbound `location` opened on October 29. The three surfaces enter the parse scope
(110 + 27) and the surface-loss coverage (137), activating six DrakeParseTest checks; the example map stays
37 / 11 (table updated in this commit). Direct run 956 / 76 including discovery, green on its first run; Dev's first full gate 956 / 75, every
headline matching, pending mods actors 0. Committed and pushed by Dev as 8540341. The four *Intent types remained an
observation until the next increment.


**The four Intent types take the json factory (2026-10-30, Claude Code; Dev verified 967 / 76 on 2026-10-31).**
Dev accepted "Accepted, build it" on the recommendation that aerial FlightIntent, marine VoyageIntent, ethereal
LaunchIntent and terrestrial MarchIntent — each medium's entry message, the fact its OriginateReport joins, and the
one remaining shape of the kind the report thread repaired: medium + Dracon, no elements, no factory, no surface,
constructed only by tests as host text — take the json factory the reports took: the surface `type X from <Medium>
draco format dracon Dracon` with `factory parameters par json Json`, JSON re-canonicalized (the factory record and
nothing else), companion replaced by the generator's output (25 lines and two imports added, none removed: `apply`,
the Dracon-shaped codec, `Null`), the eight test sites — the four OriginateReportTests, the marine, ethereal and
terrestrial chain tests and AerialAssemblyTest — re-authored `X(payload)`, probed on FlightIntent with the compiled
CLIs before the recommendation and built as probed, `DrakeCLI check` clean over the four media. No engine change
and no definition site: no definition constructs an intent, Creator's messageType being Dracon. The four strict
`override val json` sites change nothing observable, each payload being an already-built value the by-name binding
captures. `IntentFactoryTest` (3) is the 76th suite, one suite because the four definitions are one pattern, each
case iterating over the four: the trio, the by-name argument, the codec round-trip. Every message in the example
chain — four intents, four reports, the World's Location sink — now constructs through its definition; nothing
from the report thread remains preserved. The four surfaces enter the parse scope (110 + 31) and the surface-loss
coverage (141), activating eight DrakeParseTest checks; the example map stays 37 / 11 (table updated in this
commit). Direct run 967 / 77 including discovery, green on its first run; Dev's first full gate 967 / 76 on 2026-10-31,
every headline matching, pending mods actors 0. Committed and pushed by Dev as 3a3cbfc.


**Position and Location take their surfaces and the generator's companions (2026-10-31, Claude Code; Dev verified
974 / 77).** Dev accepted "Accepted, build it" on the recommendation that aerial Position and
terrestrial Location — the two coordinate types of the example chain, and of the eleven example differences the
two whose companions lacked something the DEFINITION carries rather than companion spelling — take their drake
surfaces as the emitter renders them (`type Position from Aerial`, three `fix` elements, a factory with three
defaulted `par` lines) and have their hand-written companions replaced by the generator's output, which differs
only by the codec inferred from the elements: 21 lines, `encoder` writing the three fields by name, `decoder`
reading each through `Option` with the factory default for a missing field, and the imports — Cartesian's
accepted inferred codecs of 2026-10-25 being the precedent. Probed with the compiled CLIs before the
recommendation: parsing each emitted surface reproduces the committed JSON byte for byte, so no JSON moves;
trait, loader line, kind-vals, `apply` and `Null` are unchanged, so Input, world Consumer and five tests
construct as before. Built as probed, `DrakeCLI check` clean over both media; no engine change.
`CoordinateProjectionTest` (3) is the 77th suite, each case iterating over the two: the trio (with the element
names and the three defaults), the lazy once-evaluated arguments with Null and an omitted argument at zero, the
codec (three fields by name, an empty object to the defaults, a lone third field, an invalid latitude refused).
The two surfaces enter the parse scope (110 + 33) and the surface-loss coverage (143), activating four
DrakeParseTest checks; the example map moves to 39 / 9 (table updated in this commit). Remaining example
differences, all companion spelling: the three ConsumeReport companions, the five domain objects, aerial Input.
Direct run 974 / 78 including discovery, green on its first run; Dev's first full gate 974 / 77, every headline
matching, pending mods actors 0. Committed and pushed by Dev as 23333f3.


**The example map closes and becomes a gate (2026-10-31, Claude Code; Dev verified 982 / 77).** Dev accepted
"Accepted, build it" on the recommendation that the nine remaining example differences — all companion spelling:
the three ConsumeReport companions (ethereal, marine, terrestrial: `with DracoType` and `override`), the five domain
objects (Aerial, Ethereal, Marine, Terrestrial, World: the same two words, `import domains._` and the wildcard world
import) and aerial Input (`elementTypeNames`, the unused private `knowledge` Creator carries, lazy bindings in
`receive`) — be closed in one increment: the four ConsumeReports, which had no drake surface, take the surface
`DrakeCLI emit` renders (`type ConsumeReport`, the domain line, `rule` with one `var report <Report>` and a `mon`
action), their JSON RE-CANONICALIZED by parsing it back — the host-opaque action string becomes a call tree, aerial's
`ctx.getRuntime().get("consumed")` a nested one, and the aspect keys take the canonical order — with the Scala
generated from either JSON identical; the nine companions are replaced by the generator's output; and
`ExampleDomainsGenTest`, a report-only discovery map since 2026-06-23, is tightened into the gate its own comment
named — zero differ, zero error, zero missing, asserted — so the example-map row leaves the §1 table, the third
measurement to become an assertion. Probed with the compiled CLIs before the recommendation (all nine generated
companions compile against the current classes, the ConsumeReport round-trip as described) and built as probed:
`DrakeCLI check` clean over the five media. No engine change, no new suite. Aerial ConsumeReport's companion
already matched and regenerates byte-identical from the re-canonicalized JSON; Input's `knowledge` registers the
domain's two rules and is never evaluated, and its two bindings become `lazy val`, drake.dlt EVALUATION's default.
The four surfaces enter the parse scope (110 + 37) and the surface-loss coverage (147), activating eight
DrakeParseTest checks (table updated in this commit). After this every example definition under
`src/mods/resources/domains` is a trio or a JSON-only type whose companion is the generator's output, and the
example inventory the October sessions worked through is empty; deferred activation and the .dcon migration
remain, neither accepted. THE FIRST DIRECT RUN FAILED ONE TEST, ExampleLoadingTest's all-48 resolution: the treed actions name
`domains.<medium>.<Medium>Sink.record`, and resolution knew only AerialSink, bound by the example caller on 2026-10-23
when Consumer's action was a tree; `ExampleScope` now binds the four sinks by qualified name, the same decision, and
the loading test checks each is a binding, not a member. Second direct run 982 / 78 including discovery, green, every headline as stated; Dev's first full gate 982 / 77 on
2026-10-31, every headline matching, pending mods actors 0. Committed and pushed by Dev as 7be1497.


**The example bodies are swept and the validator compares by identity (2026-10-31, Claude Code; Dev verified 983 / 77).** Dev accepted "Accepted, build it" on the recommendation that the twelve example definitions the
compact form changes — the four Creators, the four OriginateReports, aerial Input, terrestrial Output, world Consumer and
Provider, the one part of the corpus the October 21 sweep left out — be swept as that corpus was: JSON compacted with
`uses` records, drake surfaces re-emitted, Scala regenerated and pinned by the example gate, the other 36 untouched;
together with `AssemblyValidator` comparing a consumer's message type with the actor's by IDENTITY through the
definition's record instead of by text, and one assembly test over a compact definition. MEASURED before recommending,
with a probe compacting all 48 through the example caller scope: `draco.format.dracon.Dracon` shortens to `Dracon` in
the Creators' and Output's consumer parameter and the OriginateReports' binding (the host text inside
`ctx.getRuntime().get[…]` keeps its spelling); `domains.world.World` to `World` in Input's `worldConsumer` (recorded)
and in Consumer's and Provider's parameter and `Actor(World)` header (own package, unrecorded); Position and Location
in Consumer's case branch and Input's binding; the factory's `ActorType`, recorded as the dreams records carry it;
anchors — Output's `messageType domains world World`, every derivation — stay in full. Built as probed: the surfaces
are `Drake.emit` of the compact form with a `uses` section, the JSON `DrakeCLI parse` of each new surface (equal to
the probe's compact JSON, so the round trip holds), the Scala `GeneratorCLI generate` over the current resources —
two to eight lines per file, the selective imports gained and the qualified spellings shortened, unscoped and scoped
generation byte-identical for all 48 — and `DrakeCLI check` clean over the five media. THE VALIDATOR compared
`m.text` with `messageType.namePath` as strings, the third consumer of the type vocabulary (§6); over the swept tree
the old one fails both assembly suites with five errors of the shape "expects a consumer of World but receives
domains.world.World". Now `identity(td, text)` resolves through `NameResolution.recorded(td)`, completes a bare result
with the definition's own package (the compact form's convention for a bare name) and both sides compare by
`namePath`, parameters free, as definitions are keyed; nothing is loaded or discovered. The actor's message type is an
anchor the carrier holds in full (the surface elides Provider's own-package `messageType World`), so the completion is
exercised today only on the consumer side — the new WorldAssemblyTest case's first focused run failed on the test's
own assertion that it reads bare, corrected before the direct run. WorldAssemblyTest +1 pins the three compact
spellings with their records, the actors' identities, the clean service and a crossed binding reported in full. No
headline moves: twelve surfaces re-emitted, none added. After this every definition in the corpus is in the compact
form but the five sentient JSON-only types (Direction, Ego, Lean, Percept, Waypoint) whose tuple value type is the
pre-tree string `"(Double, Double)"` and which alone fail `parse(emit(td)) == td` — an observation, not recommended;
deferred activation and .dcon remain, neither accepted. Direct run 983 / 78 including discovery, green on its first
run, every headline as stated; Dev's first full gate 983 / 77 on 2026-10-31, every headline matching, pending mods
actors 0. Commit pending.
