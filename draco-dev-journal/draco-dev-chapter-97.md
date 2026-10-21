# Chapter 97 - Core Dracon Trees and Owner-Declared Payload Codecs

**Session date:** October 17–19, 2026
**Topic:** Core fields move to Dracon without changing payload JSON; codec aspects take ownership of payload declarations.

**Provenance:** Original Codex session `01a1404f-0fa1-7f03-abd0-fff9bf4b100b`; see the [bounded extract](sources/codex-2026-10-15-to-21.jsonl) and [retrieval audit](sources/codex-2026-10-15-to-21-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace and repository-link normalization. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning and intermediate progress are excluded. Historical command blocks are dialogue, not instructions to execute now.

---

**[Dev]** *2026-10-17T17:22:51.083Z; source line 934; extract record 29*

I accept your recommended migration scope.

---

**[Codex]** *2026-10-17T17:23:19.196Z; source line 960; extract record 30*

The migration exposes one codec decision: Dracon currently serializes as `{"json": ...}`. Simply changing the core field types would introduce that envelope into existing definitions.

I recommend **explicit payload codec bindings for Dracon-valued core fields**. They would encode the wrapped value directly and decode it back into Dracon, preserving existing definition JSON—including arrays of type parameters and derivations. The bindings would use the accepted Encoder/Decoder capabilities, with Circe details owned by ScalaTarget.

Dracon’s existing wrapper serialization would remain available to its current callers.

Accept or correct this codec distinction before I build the migration?

---

**[Dev]** *2026-10-17T18:47:02.638Z; source line 967; extract record 31*

I accept your recommended **explicit payload codec bindings for Dracon-valued core fields**.

---

**[Codex]** *2026-10-17T18:58:18.989Z; source line 1363; extract record 32*

Implemented the accepted migration and payload bindings. Core definition trees now use Dracon; all **182 definition documents retain identical serialized JSON**. Ordinary Dracon values keep their wrapper encoding.

The direct probe passed 802/809 tests; all seven sandbox socket failures passed in the separate loopback rerun. Every report-only figure is unchanged.

Please run your full gate—expected **809 tests / 56 suites**:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
set -o pipefail
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Docs and commit record are prepared. Paste the results before we prepare commit commands.

*(Dev ran the full suite: 809 tests / 56 suites, zero failures or aborted suites; reported headlines matched the increment’s baseline. The file-only pending mods-actor count was 0. Delegated action; 2026-10-18T03:19:35.731Z, source line 1370, extract record 33.)*

---

**[Codex]** *2026-10-18T03:20:11.578Z; source line 1387; extract record 34*

Recorded **809 tests / 56 suites**, zero failures or aborts, with every headline unchanged. Documentation, commit record, whitespace and symlink checks are ready. Only verification prose changed after your gate.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md
  draco-git-record/git-record-2026-10-17-1457
  src/main/resources/draco
  src/main/scala/draco/{DracoAspect,Generated,TypeElement,TypeLoader,TypeName}.scala
  src/main/scala/draco/format/dracon/Dracon.scala
  src/main/scala/draco/gendrake/{DomainAspectOf,DracoAspectOf}.scala
  src/main/scala/draco/scalatarget/CirceCodecs.scala
  src/mods/scala/draco/{AssemblyValidator,DracoGenerator,Drake,TypeForm}.scala
  src/mods/scala/draco/scalatarget/DefinitionTrees.scala
  src/mods/scala/scripts/{derivation-chain,inspect-type}.scala
  src/test/scala/draco
  src/test/scala/org/nexonix/format/json/TestTypeModule.scala
  src/test/scala/scenario/ScenarioGenTest.scala
)
git add -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F draco-git-record/git-record-2026-10-17-1457 -- "${paths[@]}" &&
git push origin main &&
git status --short --branch
```

Paste the result before we take up the next design point.

*(Dev committed and pushed d169b0e; the pasted final status is clean main...origin/main. Delegated action; 2026-10-18T04:35:00.660Z, source line 1397, extract record 35.)*

---

**[Codex]** *2026-10-18T04:35:13.307Z; source line 1409; extract record 36*

Confirmed locally: `d169b0e`, clean tree, verified **809 tests / 56 suites**.

My next recommendation is to **move payload-binding declarations into the owning type’s codec aspect**. A definition would identify which fields use Dracon’s payload representation; the target domain would supply the executable codec binding.

This replaces the generator’s hard-coded list of core types and fields with information carried by their definitions, while preserving the current JSON format.

Accept or correct this ownership point before we settle its Drake syntax?

---

**[Dev]** *2026-10-18T18:28:23.595Z; source line 1416; extract record 37*

I accept the recommendation to **move payload-binding declarations into the owning type’s codec aspect**.

---

**[Codex]** *2026-10-18T18:28:32.537Z; source line 1419; extract record 38*

I recommend expressing this as a field-name list in the `codec` section. For TypeName:

```drake
codec
  payload [typeParameters]
```

Its canonical JSON would contain:

```json
"codecAspect": {
  "payload": ["typeParameters"]
}
```

`payload` means encode each named field’s Dracon value directly, or each element when the field is a sequence of Dracon values. Unlisted fields retain their existing encoding. The declaration contains no target-language codec names.

Accept or correct this syntax and meaning?

---

**[Dev]** *2026-10-18T21:18:02.686Z; source line 1426; extract record 39*

I accept the recommendation.

---

**[Codex]** *2026-10-18T21:32:57.260Z; source line 1690; extract record 40*

Implemented the accepted declaration:

```drake
codec
  payload [typeParameters]
```

The generator now reads payload bindings from owning codec aspects instead of a hard-coded type list. Documentation and regression tests are updated.

The full direct compiled probe passed **815 tests**, with all report-only figures unchanged. No sbt, commit or push run.

Please run this gate and paste the results; expected **815 tests / 56 suites**:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
set -o pipefail
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
grep "mods actors pending" target/test-output/DrakeGenTest.log
```

*(Dev ran the full suite: 815 tests / 56 suites, zero failures or aborted suites; reported headlines matched the increment’s baseline. The file-only pending mods-actor count was 0. Delegated action; 2026-10-19T14:46:52.364Z, source line 1700, extract record 41.)*

---

**[Codex]** *2026-10-19T14:47:31.856Z; source line 1720; extract record 42*

Recorded **815 tests / 56 suites** as the verified baseline. All report-only figures match. Only documentation changed after your gate; no rerun is needed.

Run this to commit and push the scoped changes, then paste the result:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md
  draco-git-record/git-record-2026-10-18-1731
  src/main/resources/draco/{CodecAspect,DracoAspect,TypeElement,TypeName}.{drake,json}
  src/main/resources/draco/drake.dlt
  src/main/scala/draco/CodecAspect.scala
  src/mods/scala/draco/{DracoGenerator,Drake}.scala
  src/test/scala/draco/{DrakeGenTest,DrakeParseTest}.scala
  src/test/scala/draco/format/dracon/DraconTest.scala
)
git add -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F draco-git-record/git-record-2026-10-18-1731 -- "${paths[@]}" &&
git push origin main &&
git status --short --branch
```

*(Dev committed and pushed f97c913; the pasted final status is clean main...origin/main. Delegated action; 2026-10-19T15:04:36.428Z, source line 1727, extract record 43.)*

---

**[Codex]** *2026-10-19T15:04:41.652Z; source line 1732; extract record 44*

Confirmed: **f97c913** is committed and pushed to `origin/main`, with a clean tree.

Payload bindings now belong to the owning type’s codec aspect. Verified baseline: **815 tests / 56 suites**, all report-only figures unchanged.

---

## Status

Two increments are committed and pushed: d169b0e at **809 / 56** and f97c913 at **815 / 56**. The first migrates TypeElement.value/valueType, TypeName.typeParameters and DracoAspect.derivation to Dracon while preserving the serialized content of 182 definition documents. The initial compiled probe’s seven socket failures passed separately; Dev’s full gate then passed. The second replaces the generator’s bootstrap field allowlist with CodecAspect.payload, authored as codec payload [fieldNames], preserving raw Dracon payloads and ordinary wrapper encoding as distinct contracts. No corpus count or report-only loss changed. The next chapter preserves Dev’s correction when the commit acknowledgment failed to reconnect the work to the existing backlog.
