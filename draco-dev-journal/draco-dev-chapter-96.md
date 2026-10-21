# Chapter 96 - Dracon as Wrapper and Format Domain

**Session date:** October 15–17, 2026
**Topic:** Dev redirects from activation to Dracon, corrects the proposed six-form family, and accepts explicit Encoder and Decoder capabilities.

**Provenance:** Original Codex session `01a1404f-0fa1-7f03-abd0-fff9bf4b100b`; see the [bounded extract](sources/codex-2026-10-15-to-21.jsonl) and [retrieval audit](sources/codex-2026-10-15-to-21-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace and repository-link normalization. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning and intermediate progress are excluded. Historical command blocks are dialogue, not instructions to execute now.

---

**[Dev]** *2026-10-15T21:02:47.506Z; source line 519; extract record 13*

Save this recommendation for the future. I want to now consider wrapping Json (from circe) in a drake version of json format so I can eliminate direct references to target language names and symbols in type definitions that are not wrappers. We can refer to the resulting format as dracon (with "dcon" as the file extension to possibly replace "json") and it will be identically parsable as json, but have a different set of operators, names and symbols for defining codecs, which will be bound  the corresponding lexemes in the target language domain.

---

**[Codex]** *2026-10-15T21:03:43.415Z; source line 550; extract record 14*

Saved the activation recommendation as **deferred and unaccepted** in the [handoff](../DEVELOPMENT_HANDOFF.md). Only documentation changed.

My reading of **dracon** is that JSON supplies the syntax—objects, arrays, strings, numbers, booleans and null—while Draco supplies the meaning of its codec expressions. A JSON parser could read a `.dcon` file unchanged; interpreting its operators would require Draco’s vocabulary and target bindings.

The existing JSON wrapper provides a starting point, but non-wrapper definitions still expose `Json`, `Decoder`, cursor operations and Circe calls. Renaming that wrapper alone would leave those dependencies.

**My recommendation for the first design point:** dracon owns the neutral value and codec-operation vocabulary; target domains bind those operations to their implementations. A Scala/Circe wrapper would contain the Circe types and calls, while ordinary definitions use dracon names. Keep `.dcon` migration for a later point, once that boundary works.

Accept that ownership boundary, or correct it.

---

**[Dev]** *2026-10-15T21:07:43.286Z; source line 557; extract record 15*

I accept that ownership boundary,

---

**[Codex]** *2026-10-15T21:07:54.966Z; source line 562; extract record 16*

Accepted: dracon owns the neutral value and codec vocabulary; target domains own its implementation bindings. `.dcon` migration remains a later point.

The next design point is **what a dracon value is**. A target alias alone could hide the name `Json`, but definitions would still need neutral operations to construct and inspect values.

My recommendation is a **Draco-owned value family covering JSON’s six forms**: null, Boolean, number, text, array and object. Codec definitions operate on that family; the Scala/Circe wrapper supplies the conversion and implementation bindings. This gives the operations a meaning independent of Circe before we choose their names and symbols.

Accept the six-form neutral value family, or correct it.

---

**[Dev]** *2026-10-15T21:12:06.003Z; source line 569; extract record 17*

I was considering the replacing the draco.format.json.JSON domain with draco.format.dracon.Dracon, and agree with deferring ".dcon" as the extension until after the wrapper type definitions working.

---

**[Codex]** *2026-10-15T21:12:14.058Z; source line 572; extract record 18*

That places dracon as the successor to the existing format domain: **`draco.format.dracon.Dracon` replaces `draco.format.json.JSON`**, with `.json` retained until the wrapper definitions work.

The six-form value family was my proposal; your replacement direction doesn’t require accepting it.

My recommendation is to make **Dracon the neutral JSON-value wrapper and its format domain**, following the existing JSON type’s combined role. Its public operations use Draco vocabulary, with Circe-specific types and calls confined to the target implementation boundary. We can introduce separate value-form types when the codec operations establish a need.

Accept Dracon as that combined wrapper and format domain, or correct it.

---

**[Dev]** *2026-10-15T21:13:32.422Z; source line 579; extract record 19*

I accept Dracon as that combined wrapper and format domain,

---

**[Codex]** *2026-10-15T21:14:14.765Z; source line 600; extract record 20*

Accepted and recorded: **`draco.format.dracon.Dracon` replaces `draco.format.json.JSON` as the combined wrapper and format domain**, retaining `.json` for now.

The existing code exposes the next boundary: `Format.Value` requires `T: Decoder`, so even a renamed wrapper would still make ordinary definitions depend on Circe’s decoder contract.

My recommendation is to make **encoding and decoding first-class dracon capabilities**, passed explicitly to operations that need them. Target wrappers implement those capabilities using Circe’s Encoder and Decoder; ordinary definitions refer only to the dracon capabilities.

Accept explicit dracon codec capabilities, or correct it.

---

**[Dev]** *2026-10-15T23:18:22.691Z; source line 607; extract record 21*

I accept explicit dracon codec capabilities,

---

**[Codex]** *2026-10-15T23:20:46.062Z; source line 663; extract record 22*

Accepted and recorded. The next point is the capabilities’ **operation and result contract**.

My recommendation is:

- `Encoder(T)` supplies `encode: T -> Dracon`.
- `Decoder(T)` supplies `decode: Dracon -> Presence(T)`.

A successful decode returns Present; incompatible input returns Absent. This uses drake’s existing result vocabulary without exposing Circe’s Decoder, Either, exceptions or null to callers.

Accept those two capability signatures, or correct them.

---

**[Dev]** *2026-10-16T01:02:01.950Z; source line 670; extract record 23*

I accept those two capability signatures

---

**[Codex]** *2026-10-16T01:11:19.397Z; source line 855; extract record 24*

Implemented the accepted wrapper increment:

- **Dracon replaces the JSON format domain**, retaining `.json`.
- Encoder and Decoder provide the accepted signatures, with explicit Circe adapters in ScalaTarget.
- Path selection and typed decoding return Presence; missing paths skip decoding.
- Example chains and initialization now use Dracon.

The full compiled-class probe passed **797/804 tests**; seven sandbox socket-bind failures then passed in the **9/9 loopback rerun**. Wrapper projection checks show zero drift. Nothing committed or pushed.

Please run the full gate. Expected: **804 tests / 56 suites**. Counts now reflect three additional corpus types: **108 + 10** parse scope, one loss across **118**, **108** aspect sections, GenDrake **106/106**. Other headlines should remain unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
set -o pipefail
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
grep "mods actors pending" target/test-output/DrakeGenTest.log
```

Paste the results; I’ll record them before preparing the commit commands.

*(Dev ran the full suite: 804 tests / 56 suites, zero failures or aborted suites; reported headlines matched the increment’s baseline. The file-only pending mods-actor count was 0. Delegated action; 2026-10-16T20:34:39.368Z, source line 876, extract record 25.)*

---

**[Codex]** *2026-10-16T20:35:25.072Z; source line 905; extract record 26*

Recorded **804 tests / 56 suites, zero failures or aborts** in DRACO.md, README, CHANGELOG, the handoff and commit record. Every headline matches; whitespace and symlink checks pass. Only documentation changed after your gate.

The inspected tree contains only this increment. These commands stage its paths; the commit omits a pathspec under DRACO.md’s rename exception so both old and new paths are included.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md build.sbt
  draco-git-record/git-record-2026-10-15-1703
  src/main/resources/draco/drake.dlt
  src/main/resources/draco/format
  src/main/resources/draco/scalatarget
  src/main/scala/draco/format
  src/main/scala/draco/scalatarget
  src/mods/resources/domains/{aerial,ethereal,marine,terrestrial}
  src/mods/scala/domains/{aerial,ethereal,marine,terrestrial}
  src/mods/scala/draco/DracoGenerator.scala
  src/test/scala/domains
  src/test/scala/draco/dreams/InitializationTest.scala
  src/test/scala/draco/format/dracon/DraconTest.scala
  src/test/scala/org/nexonix/format/json/TestValue.scala
)
git add -A -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F draco-git-record/git-record-2026-10-15-1703 &&
git push origin main &&
git status --short --branch
```

Paste the result before we take up the next Dracon design point.

*(Dev committed and pushed db7e7a8; the pasted final status is clean main...origin/main. Delegated action; 2026-10-17T17:21:12.388Z, source line 915, extract record 27.)*

---

**[Codex]** *2026-10-17T17:21:25.262Z; source line 927; extract record 28*

Confirmed locally: `db7e7a8`, clean tree, with the verified **804 tests / 56 suites** baseline.

My next recommendation is to make **Dracon the value type of core definition trees**: `TypeElement.value` and `valueType`, `TypeName.typeParameters`, and `DracoAspect.derivation`. Their serialized JSON would stay identical; their definitions would express null, selection and inspection through neutral Dracon operations, with Circe implementation details inside the wrapper or target bindings.

This would carry the accepted boundary into the definitions that still directly name `Json`, while leaving `.dcon` deferred.

Accept or correct this migration scope?

---

## Status

Dev confirmed **804 tests / 56 suites** on October 16 and committed/pushed db7e7a8 on October 17. Dracon replaces the JSON format domain; Encoder(T) supplies T -> Dracon and Decoder(T) supplies Dracon -> Presence(T), with explicit Circe adapters at the target boundary. The six-form family was not accepted. The first compiled probe passed 797/804; seven sandbox socket failures passed in a separate 9/9 loopback rerun, followed by Dev’s complete green gate. Three net-new corpus types move parse scope to 108+10, measured types to 118, aspect sections to 108 and GenDrake to 106/106; losses and other headlines remain unchanged. The .json extension remains; .dcon and activation are deferred. Core-tree migration is proposed at the close, accepted in chapter 97.
