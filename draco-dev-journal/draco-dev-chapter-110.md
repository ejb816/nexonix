# Chapter 110 — Cartesian and Observable Projection Repairs

**Session date:** October 25, 2026
**Topic:** Cartesian and Observable Projection Repairs.

Source: [bounded transcript audit](sources/codex-2026-10-22-to-26-audit.md), Codex extract records 57–68. Original assertive prompts and final replies are reproduced with outer whitespace and repository-local Markdown links normalized; responsive command results become italic delegated-action notes. Their full originals remain in the extract. Closing Status is a sourced maintenance summary, not dialogue.

---

**[Dev]** *2026-10-25T19:05:08.722Z; source line 1654; extract record 57*

I accept repairing **`domains.world.Cartesian` alone** next.

---

**[Codex]** *2026-10-25T19:08:43.592Z; source line 1765; extract record 58*

Cartesian’s repair is implemented. Both factories retain their evaluation behavior; defaults, `Null` and generated codecs pass their checks. All **908 tests** passed across every discovered suite; documentation is updated.

Please run:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
set -o pipefail
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Expected: **908 tests / 68 suites**, example map **33 match / 15 differ**, zero losses across **130 types**, parse scope **110 + 20**. All other headlines unchanged.

*(Delegated action — Dev ran the full sbt gate: 908 tests / 68 suites, zero failures or aborts and zero canceled/ignored/pending tests. Source: 2026-10-25T19:11:03.031Z; source line 1772; extract record 59. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 33 match, 15 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 130 types; Drake.parse scope: 110 draco + 20 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. Full pasted output is retained in the extract.)*

---

**[Codex]** *2026-10-25T19:11:22.685Z; source line 1787; extract record 60*

Recorded **908 tests / 68 suites** as Dev-verified, with every expected headline matching. Documentation is updated; whitespace checks pass.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md
  draco-git-record/git-record-2026-10-25-1505
  src/mods/resources/domains/world/Cartesian.drake
  src/mods/resources/domains/world/Cartesian.json
  src/mods/scala/domains/world/Cartesian.scala
  src/test/scala/domains/world/CartesianProjectionTest.scala
)
git add -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F draco-git-record/git-record-2026-10-25-1505 -- "${paths[@]}" &&
git push origin main &&
git status --short --branch
```

*(Delegated action — Dev committed and pushed 804c8a4, “Carry Cartesian tuple factory in its definition”; the pasted final status is main...origin/main with no changed paths. Source: 2026-10-25T19:15:17.783Z; source line 1794; extract record 61. Full commands and results are retained in the extract.)*

---

**[Codex]** *2026-10-25T19:15:46.161Z; source line 1816; extract record 62*

Confirmed **804c8a4** is committed and pushed; the tree was clean. Baseline: **908 tests / 68 suites**.

I recommend repairing **`domains.world.Observable` alone** next. Its definition omits `fromGeodetic` and `toGeodetic`, so regeneration loses both helpers.

Carry them into Drake/JSON, preserving their strict parameters, lazy factory arguments and defaults. Include the generated nested Cartesian codecs, with projection, round-trip and default checks. Retain the existing Aerial-to-Terrestrial transform test and leave Geodesy’s mathematics unchanged.

Accept or correct this scope.

---

**[Dev]** *2026-10-25T19:17:12.945Z; source line 1823; extract record 63*

I accept repairing **`domains.world.Observable` alone** next. I

---

**[Codex]** *2026-10-25T19:26:38.552Z; source line 2081; extract record 64*

Observable’s repair is implemented. Helpers, evaluation behavior and nested codecs pass their checks. The full rerun passed **918 tests** after adding the required caller binding for Geodesy. Documentation records the initial loading failures and correction.

Please run:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
set -o pipefail
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Expected: **918 tests / 69 suites**, example map **34 match / 14 differ**, zero losses across **131 types**, parse scope **110 + 21**. All other headlines unchanged.

*(Delegated action — Dev ran the full sbt gate: 918 tests / 69 suites, zero failures or aborts and zero canceled/ignored/pending tests. Source: 2026-10-25T19:28:21.813Z; source line 2088; extract record 65. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 34 match, 14 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 131 types; Drake.parse scope: 110 draco + 21 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. Full pasted output is retained in the extract.)*

---

**[Codex]** *2026-10-25T19:28:48.345Z; source line 2103; extract record 66*

Recorded **918 tests / 69 suites** as Dev-verified, with every expected headline matching. Documentation is updated; whitespace checks pass.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md
  draco-git-record/git-record-2026-10-25-1517
  src/mods/resources/domains/world/Observable.drake
  src/mods/resources/domains/world/Observable.json
  src/mods/scala/domains/world/Observable.scala
  src/test/scala/domains/world/ObservableProjectionTest.scala
  src/test/scala/domains/ExampleScope.scala
  src/test/scala/domains/ExampleLoadingTest.scala
)
git add -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F draco-git-record/git-record-2026-10-25-1517 -- "${paths[@]}" &&
git push origin main &&
git status --short --branch
```

*(Delegated action — Dev committed and pushed 81154e3, “Carry Observable geodetic helpers in its definition”; the pasted final status is main...origin/main with no changed paths. Source: 2026-10-25T19:40:15.789Z; source line 2110; extract record 67. Full commands and results are retained in the extract.)*

---

**[Codex]** *2026-10-25T19:40:45.386Z; source line 2127; extract record 68*

Confirmed **81154e3** is committed and pushed; the tree was clean. Baseline: **918 tests / 69 suites**.

I recommend repairing **`domains.world.Provider` alone** next. Its JSON declares the factory and Actor derivation but lacks an actor aspect, so generation omits its forwarding behavior.

Author the explicit World actor role and existing `target(msg)` action, regenerate Scala, and pin:

- Lazy callback capture.
- Forwarding the same message exactly once.
- Continued behavior after messages and inert signal handling.
- Exact projections and the existing World assembly checks.

Activation and `.dcon` remain deferred. Accept or correct this scope.

---

## Status

Dev separately accepted Cartesian and Observable repairs, including their inferred codecs, verified 908 / 68 and 918 / 69, and committed/pushed 804c8a4 and 81154e3. Strict helper/tuple arguments and lazy factories remain distinct. Example map reaches 34 / 14, losses zero across 131 types, parse scope 110 + 21. Provider is proposed at this boundary.

Record-backed correction, not reconstructed dialogue: the Observable record [git-record-2026-10-25-1517](../draco-git-record/git-record-2026-10-25-1517) records an initial direct run of 916/918: two ExampleLoadingTest cases failed when newly visible Geodesy calls lacked a caller binding. ExampleScope gained the explicit binding and omission/non-ontological checks. The full direct rerun passed 918 tests / 70 suites including discovery; Dev’s first sbt gate passed 918 / 69. Geodesy mathematics and shared target vocabulary were unchanged.
