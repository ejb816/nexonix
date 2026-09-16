# Beta exit criteria — PROPOSED

**Status: proposed by the reviewer, not ratified by Dev.** Per `DRACO.md:70-73` this file is
model-authored prose: prior reasoning to re-examine, not a specification. It becomes the
project's beta gate only when Dev says so, and any criterion he strikes or rewrites is his to
strike. Until then the reviews score against it so that "alpha → beta" has a measurable distance.

Each criterion is derived from a finding that both reviews repeated. The ledger IDs say which.
"Pass" means verifiable at the reviewed commit by the check script or by reading the tree — not
by a claim in prose.

| ID | Criterion | Derived from | @ `db87dbd` (08-15) | @ `0af56fd` (08-28) |
|---|---|---|---|---|
| B-01 | `sbt test` leaves the checkout unchanged: no test writes a tracked path | D-13, F-11 | fail (4 writers) | fail (4) |
| B-02 | No suite headline can pass without asserting: no gate ends in bare `succeed`; report-only suites assert their baseline (#62) | D-14, D-23 | fail (2 suites) | fail (4) |
| B-03 | The round-trip gates hold with no hold-outs: `authoredAhead = ∅`, `inlineTupleArgument = ∅` (`DracoGenTest.excluded` is the TypeElement family tested as a group — a design choice, not a hold-out) | F-05, F-02 | fail (2 + 1) | fail (2 + 1) |
| B-04 | `Drake.parse` is an authoring path: every value it returns is generation-safe, or a lint rejects a host-opaque string carrying a drake-form token (#61) | F-02 | fail | fail (now demonstrated by `scenario/`) |
| B-05 | The co-declaration constraint is enforced — a validation rule in `DomainBuilder.validate`, or the dispatch grammar (#63) makes an ancestor-beside-descendant declaration unreachable | F-01 | n/a (not yet known) | fail (prose gotcha only) |
| B-06 | One version: `build.sbt`, `CLI.version`, README and the latest tag agree | D-04, X-10 | fail (3 in play) | fail (3) |
| B-07 | Generation is a pure function of the loaded family: no classpath probe during emission, no `Throwable` catch | F-03 | fail | fail |
| B-08 | Metamodel frozen: `TypeName`/`derivation` shape decided (DD-04), `TypeParameter` atom landed, `valueType` type expressions landed (#51 closed) | R-05, DD-04, DD-05 | fail | partial — shape decided, derivation and actor edges landed; atom and `valueType` open |
| B-09 | Every definition regenerates *and compiles*: `DracoGenTest` text-equal for all, and `ExampleDomainsGenTest` at match = all (or retired as a gate with the differing files brought under generation) | F-11 | fail (28/20) | fail (28/20, unverified after `05b14bd`) |
| B-10 | Docs verified against the tree at the release commit: `DRACO.md`, `README.md`, the three `GETTING_STARTED_TARGET_*`, `CHANGELOG.md`, `drake.dlt`; `AGENTS.md` resolved; no retired name outside DRACO.md's Retired list and CHANGELOG history | X-*, D-11 | fail | partial — DRACO.md/README/guides/CHANGELOG rewritten; `drake.dlt` stale (5 items); `AGENTS.md` unresolved; 16 line-level items open |
| B-11 | CI runs the suite on push/PR, not only on tag | D-17 | fail | fail (DD-09: Dev's call) |
| B-12 | Release hygiene: CHANGELOG entry per commit, release notes with a Known Limitations section, tag ↔ `build.sbt` ↔ notes checked before publish | X-16, D-17 | fail | **pass** (trigger holding 3/3; alpha.6 notes; workflow checks — though the checks have not yet run on a tag) |
| B-13 | The deferred pair decided: presence model and declared codec each have a date, or are re-filed post-Generator[L] with the two `authoredAhead` JSONs fixed | F-05, Q-02 | fail | fail |
| B-14 | Framework users can reach the surface CLI: `bin/draco-drake` or equivalent | R-09, Q-04 | fail | fail (published as a known limitation) |
| B-15 | Dead weight retired: `org/nexonix/**`, YAML leftovers, unused dependencies, Pekko off the milestone | D-15, D-16 | fail | fail |
| B-16 | Zero open rows in ledger section D at the beta commit | D-* | fail (18) | fail (22 open, 1 half) |

**Score:** 08-15 — 0 pass / 0 partial / 15 fail (B-05 n/a). 08-28 — 1 pass / 2 partial / 13 fail.

## Notes on the criteria

- B-05 is the only criterion that guards a *wrong answer* in a user's running system rather than a
  project-hygiene property. If Dev ratifies only one row, it should be this one.
- B-08 is the one Dev has already stated in his own words: the alpha.6 release notes say the
  metamodel is "NOT frozen — `TypeName` and `derivation` are expected to change shape under #51
  before beta." DD-04 has since decided that the shape does *not* change; the criterion is
  therefore narrower than the notes implied.
- B-09's second clause exists because `ExampleDomainsGenTest` may be the wrong gate rather than a
  failing one: if the 20 differing files are hand-written by design (as `world/Consumer` and
  `world/Provider` now are, D-24), the criterion is met by bringing the rest under generation and
  declaring the remainder hand-written in the test itself.
- B-11 is Dev's decision (DD-09), so a review scores it but does not press it.
- B-12 passed on process evidence; the tag/notes integrity checks in `release.yml` have never
  executed because the only tag predates them (D-17). The next tag is the real test.
