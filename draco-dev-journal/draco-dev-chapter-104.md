# Chapter 104 - Formals in the Aspect, and the Close

**Session date:** October 21–22, 2026
**Topic:** A definition's header formals become TypeParameter records in DracoAspect, its own name bare, a TypeName's parameters a reference's actuals; the 23 parameterized trios migrate by re-parsing and the Scala is byte-identical. Committed as 7b1bfea on its first gate. The next design point — Presence-typed role slots for the present-empty roles the spec defines — is put to Dev, who closes the session with it open.

**Provenance:** Original Claude Code session `72019384-f3dd-404d-8ef3-7b676c27b714`; see the [bounded extract](sources/claude-2026-10-21-to-22.jsonl) and [retrieval audit](sources/claude-2026-10-21-to-22-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace and the paste wrapper tags. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning and intermediate progress are excluded. Historical command blocks are dialogue, not instructions to execute now.

---

**[Dev]** *2026-10-22T02:34:00.792Z; source line 3067; extract record 49*

I accept header formals as TypeParameter records in DracoAspect.typeParameters, with TypeName.typeParameters carrying actual arguments only and a definition's own name keyed bare.

---

**[Claude]** *2026-10-22T02:52:03.985Z; source line 3363; extract record 50*

Built as you accepted it. Nothing is committed.

**What landed**

- **The aspect.** DracoAspect gains `typeParameters`, a list of TypeParameter records, as its first slot, the same record a method's formals already use: a name with optional upper and lower bounds as type forms. Its emptiness test counts it.
- **The parser and the surface.** A header's parameters are read as formals through the method-formal reader, duplicates refused, into the aspect, and the definition's own TypeName carries none. The implicit factory result applies the name to the formals' names. The header transform spells the formals from the aspect, so the drake surface is unchanged: `type Coordinate(T <: Product)` reads and writes exactly as before.
- **The engine.** Wherever it rendered the definition's own name, the trait header, the wildcard self-type, the Null instance, the factory bracket, the family test, it now reads formals from the aspect. A reference's parameters are still spelled from its TypeName. The loader's rooting rebuild and the generator's two rebuilds carry the slot; forgetting that the loader also had to be recompiled before regenerating cost one blind pass that lost every formal, the §6 rule again. The resolver binds the formals' names over the whole definition from the aspect.
- **The corpus.** The 23 parameterized trios are migrated by re-parsing their drake: names bare in JSON, records in the aspect. Every generated Scala file is byte-identical except the three trios that define the change. Identity does not move, since definitions were already keyed bare.
- **Tests.** Five suites moved pins from the name to the aspect, including a round-trip helper that treated every parameter entry as a type form. No test or corpus type was added.

**Verification so far.** Every definition-backed Scala file regenerated through the scoped generator, unscoped equal to scoped equal to pinned over all 134 definitions, the whole main and mods tree compiled, and every discovered suite run: 849 passed, zero failed or aborted, every headline unchanged, pending mods actors 0. The record, DRACO.md, drake.dlt, README, CHANGELOG and the handoff are written.

Expected: **849 tests / 60 suites**, every report-only headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && set -o pipefail && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"; grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Please paste the grep output and the pending-actors line.

---

*(Dev ran the full suite on the formal/actual tree on the morning of October 22, green on its first run: 849 tests / 60 suites, zero failed or aborted suites, every headline unchanged, pending mods actors 0. Delegated action; 2026-10-22T10:48:14.929Z, source line 3367, extract record 51.)*

---

**[Claude]** *2026-10-22T10:48:52.651Z; source line 3405; extract record 52*

Recorded **849 tests / 60 suites**, zero failed, canceled, ignored, pending or aborted, every headline unchanged, pending mods actors 0, green on its first run. DRACO.md §1 names this the verified baseline, and the record, CHANGELOG and handoff say verified. The change set is 45 files with no renames; every changed path lies inside the pathspecs below.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && paths=(CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md draco-git-record/git-record-2026-10-21-2300 src/main/resources src/main/scala src/mods/scala/draco src/test/scala) && git add -- "${paths[@]}" && git diff --cached --check && git commit -F draco-git-record/git-record-2026-10-21-2300 -- "${paths[@]}" && git push origin main && git status --short --branch
```

Please paste the final status line. With it, every question #51 held is closed: short names resolve through an explicit ontology, files carry their records, host names are the target's declared vocabulary, and formals and actuals live in their own slots. After the commit I bring the next design point from what remains: the example domains, the two authored-ahead surfaces, or the deferred proposals, activation and .dcon.

---

*(Dev committed and pushed 7b1bfea (45 files), status clean. Delegated action; 2026-10-22T10:52:55.684Z, source line 3409, extract record 53.)*

---

**[Claude]** *2026-10-22T10:55:27.357Z; source line 3498; extract record 54*

Confirmed: 7b1bfea is committed and pushed, main is clean at origin, and 849 / 60 is the verified baseline. Every question #51 held is closed.

The next design point comes from the last residue the gates still carry: **the two authored-ahead surfaces**, BodyElement and ActorAspect. They cost three exclusions in the trio gates, two frozen baselines in the aspect probe, the one remaining surface-loss count, and the one JSON drift the CLI reports. I measured what they actually hold, and it is three separate things.

- **A presence exhibit.** Both end with empty `rule` and `actor` heads. drake.dlt is explicit about what that means: `*rule` and `*actor` are presence-significant and facet-creating, so writing the head, even empty, says the type is a rule or an actor, and the key must be emitted in JSON with an empty value. The parser honors that today: a `rule` head sets the role even with nothing under it. The carrier does not: TypeDefinition's generated codec elides an empty RuleAspect or ActorAspect, so a present-empty role is lost on the way through JSON. That is the "presence model not yet in JSON" the baseline notes name. The two files were kept as exhibits of it, though neither type is in fact a rule or an actor.
- **Two surface liberties** unrelated to presence: a host lambda written across three lines, which breaks the single-line value rule, and an explicit factory name with a different element order.
- **The last surface loss.** ActorAspect's JSON still carries `ActorAspect => Boolean` as host text, because the JSON was never re-canonicalized from the authored drake.

**Recommendation.** Make the carrier honor the spec: the two role slots in Aspects become Presence-typed, `ruleAspect` and `actorAspect` as Presence of their aspect, with Present emitted in JSON even when empty and Absent omitted, so a present-empty rule or actor survives loading, as the parser already asserts it. Then the two exhibits are no longer needed as exhibits: both files re-canonicalize as the plain types they are, their JSON catches up, the last surface loss closes, the frozen baselines and the three exclusions go, and the presence behavior moves into a round-trip test where it belongs. The cost is real: about 78 readers of the two slots across the engine, the dreams rules and the tests move from an emptiness test to a presence fold, which is the Presence family's purpose.

The alternative is to retire the exhibits alone, re-canonicalizing the two files without touching the carrier, which closes every gate residue at once but leaves the spec's presence principle unimplemented for roles.

This is also a natural boundary. Eleven increments have landed in this session since the handoff, and journal chapter 100 is written from its record at the closing handoff, so closing here is as sound a choice as building on.

---

**[Dev]** *2026-10-22T10:56:10.848Z; source line 3502; extract record 55*

Close the session here: prepare the handoff and write journal chapter 100 from this session's record; leave the presence decision open for the next session.

---

## Status

Dev's gate on the formal/actual tree passed its first run on the morning of October 22 at **849 / 60**, every headline unchanged, pending mods actors 0; Dev committed and pushed 7b1bfea (45 files). #51's open questions are closed: short names resolve through an explicit ontology, files carry their records, host names are the target's declared vocabulary, formals and actuals live in their own slots. The presence decision — `ruleAspect` and `actorAspect` as Presence so a present-empty role survives the carrier, with the two authored-ahead surfaces re-canonicalized — is OPEN; Dev asked to close the session here. This Status is a maintenance note written during the open close request; the reply completing this documentation pass is outside the frozen boundary.
