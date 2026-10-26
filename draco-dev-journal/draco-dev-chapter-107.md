# Chapter 107 — Auditing Example Membership and Loading

**Session date:** October 22–23, 2026
**Topic:** Auditing Example Membership and Loading.

Source: [bounded transcript audit](sources/codex-2026-10-22-to-26-audit.md), Codex extract records 19–32. Original assertive prompts and final replies are reproduced with outer whitespace and repository-local Markdown links normalized; responsive command results become italic delegated-action notes. Their full originals remain in the extract. Closing Status is a sourced maintenance summary, not dialogue.

---

**[Dev]** *2026-10-22T12:05:50.625Z; source line 577; extract record 19*

I accept your audit scope.

---

**[Codex]** *2026-10-22T12:09:22.038Z; source line 635; extract record 20*

The audit is complete. No code or definitions changed.

| Check | Finding |
|---|---|
| Declared membership | **32 of 48** identities included; **16 omitted members** already name those domains as owners |
| Strict resolution | **45 pass**; three stop at unknown names |
| Scoped versus unscoped output | **34 identical**, **11 spelling-only changes**, **3 errors** |
| Existing projection gaps | Remain substantive: missing helper methods, actor behavior, and a message-pattern binder |

The unresolved references are AerialSink, HCursor, Cartesian and—after removing the first blocker—Position. Temporary diagnostic bindings let all 48 resolve, but that does not establish that their generated Scala is correct.

Full findings and omitted-member list: [Example-domain audit](../EXAMPLE_DOMAIN_AUDIT.md).

**Recommendation:** first make domain membership reciprocal for the **16 existing definitions that already name those owners**, with a consistency check. Host bindings and projection repairs would follow as separate decisions.

**Accept that membership scope, or correct it?**

---

**[Dev]** *2026-10-23T14:47:17.528Z; source line 646; extract record 21*

I accept that membership scope.

---

**[Codex]** *2026-10-23T14:51:21.150Z; source line 743; extract record 22*

Implemented, uncommitted. All **48 example definitions** now have reciprocal domain membership. The five affected trios and consistency checks are updated.

All **873 tests passed** in the compiled-class run. Expected sbt gate: **873 tests / 62 suites**.

Two coverage headlines change: **zero losses across 125 types**, and parse scope **110 draco + 15 mods**. All others remain unchanged, including the example map at **28 match / 20 differ**.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
set -o pipefail
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the results.

*(Delegated action — Dev ran the full sbt gate: 873 tests / 62 suites, zero failures or aborts and zero canceled/ignored/pending tests. Source: 2026-10-23T15:02:15.474Z; source line 750; extract record 23. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 125 types; Drake.parse scope: 110 draco + 15 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. Full pasted output is retained in the extract.)*

---

**[Codex]** *2026-10-23T15:02:55.679Z; source line 765; extract record 24*

Recorded **873 tests / 62 suites**, green on your first gate. All headlines match. The 23-file change set passes whitespace checks.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md
  DRACO.md README.md EXAMPLE_DOMAIN_AUDIT.md
  draco-git-record/git-record-2026-10-23-1048
  src/test/scala/domains/ExampleMembershipTest.scala
)
for stem in aerial/Aerial ethereal/Ethereal marine/Marine terrestrial/Terrestrial world/World; do
  paths+=(
    "src/mods/resources/domains/$stem.drake"
    "src/mods/resources/domains/$stem.json"
    "src/mods/scala/domains/$stem.scala"
  )
done
git add -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F draco-git-record/git-record-2026-10-23-1048 -- "${paths[@]}" &&
git push origin main &&
git status --short --branch
```

Please paste the commit and final status; I’ll then bring the next design point.

*(Delegated action — Dev committed and pushed 1d69df1, “Make example-domain membership reciprocal for sixteen existing definitions”; the pasted final status is main...origin/main with no changed paths. Source: 2026-10-23T15:03:32.531Z; source line 772; extract record 25. Full commands and results are retained in the extract.)*

---

**[Codex]** *2026-10-23T15:04:36.618Z; source line 800; extract record 26*

Confirmed: **1d69df1** is committed and pushed, with **873 tests / 62 suites** verified.

The fresh probe now finds **48/48 identities** and resolves **46/48 definitions**. Only two external references remain:

| Reference | Proposed owner |
|---|---|
| `io.circe.HCursor` | Scala target’s `HostBindings` |
| `domains.aerial.AerialSink` | Explicit binding supplied by the example caller |

AerialSink is a hand-written observation boundary, so its binding belongs with the example caller.

**Recommendation:** add these bindings in their respective places and gate two-stage loading of all 48 example definitions. Keep body sweeping and projection repairs for subsequent decisions.

**Accept this scope, or correct it?**

---

**[Dev]** *2026-10-23T15:06:03.472Z; source line 807; extract record 27*

I accept this scope

---

**[Codex]** *2026-10-23T15:11:23.263Z; source line 939; extract record 28*

Implemented, uncommitted. All **48 example definitions** now pass the explicit two-stage loading gate. Missing HCursor or AerialSink bindings are rejected.

The full compiled-class run passed **875 tests**, with every report-only headline unchanged. Expected sbt gate: **875 tests / 63 suites**.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
set -o pipefail
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the results.

*(Delegated action — Dev ran the full sbt gate: 875 tests / 63 suites, zero failures or aborts and zero canceled/ignored/pending tests. Source: 2026-10-23T15:37:37.628Z; source line 946; extract record 29. Reported headlines: DomainAspect over the corpus: 110 complete sections reproduced, 1 parameterized included, 0 skipped; GenDrake runs: 108 of 108 definitions emitted, 0 missing, 0 not in the corpus; EXAMPLE-DOMAIN GEN MAP: 28 match, 20 differ, 0 error, 0 missing (of 48) — details in the log; PON CORPUS: 80 numbers, 550 expressions, 42 discrepancies — details in the log; CANONICAL CHECK: 80 numbers, 7 differ from generated canonical — details in the log; drake surface losses: 0 fields across 125 types; Drake.parse scope: 110 draco + 15 mods types in, 0 codec-aspect + 0 drake-less held back; DracoAspect over the corpus: 110 complete sections reproduced, 0 frozen authored-ahead baselines, 0 skipped; scenario in today's drake: 23 files, 0 rejected, 0 parsed-but-drifted, 23 clean; the forest runs: 1 of 2 ash alarms crossed to birch; rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing. Full pasted output is retained in the extract.)*

---

**[Codex]** *2026-10-23T15:38:17.305Z; source line 961; extract record 30*

Recorded **875 tests / 63 suites**, green on your first gate, with every headline unchanged. The 11-file change set passes whitespace checks.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DEVELOPMENT_RESUME_PROMPT.md DRACO.md README.md
  draco-git-record/git-record-2026-10-23-1106
  src/main/resources/draco/scalatarget/HostBindings.drake
  src/main/resources/draco/scalatarget/HostBindings.json
  src/main/scala/draco/scalatarget/HostBindings.scala
  src/test/scala/domains/ExampleLoadingTest.scala
  src/test/scala/domains/ExampleScope.scala
)
git add -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F draco-git-record/git-record-2026-10-23-1106 -- "${paths[@]}" &&
git push origin main &&
git status --short --branch
```

Please paste the commit and final status; I’ll then bring the next design point.

*(Delegated action — Dev committed and pushed ae0264e, “Supply explicit target and caller bindings for example-domain loading”; the pasted final status is main...origin/main with no changed paths. Source: 2026-10-23T15:39:46.406Z; source line 968; extract record 31. Full commands and results are retained in the extract.)*

---

**[Codex]** *2026-10-23T15:40:49.140Z; source line 999; extract record 32*

Confirmed: **ae0264e** is committed and pushed, with **875 tests / 63 suites** verified.

The next concrete gap is `aerial.OriginateReport`: its definition calls `originate(intent)`, but that conversion exists only in hand-written Scala. Resolving its names cannot supply the missing implementation.

**Recommendation:** repair this one rule’s trio. Author its existing conversion in Drake/JSON, regenerate Scala, and check both projection agreement and behavior:

- Preserve the callsign and convert `flightLevel × 100` to altitude feet.
- Preserve missing-field defaults: `"UNKNOWN"` and `0`.
- Keep the existing consumer contract.

The other example repairs remain separate.

**Accept this scope, or correct it?**

---

## Status

Dev accepted the audit, reciprocal membership for sixteen existing definitions, and the explicit loading bindings as separate decisions. He verified 873 / 62 at 1d69df1 and 875 / 63 at ae0264e and committed/pushed both. All 48 example definitions now belong to explicit domains and load through the explicit scope; HCursor is target vocabulary, AerialSink is caller-owned. The example map remains 28 / 20, losses zero across 125 types and parse scope 110 + 15. Aerial OriginateReport is the next unaccepted proposal at this boundary.
