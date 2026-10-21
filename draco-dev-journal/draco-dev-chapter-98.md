# Chapter 98 - Returning to #51 and Structured Method Formals

**Session date:** October 19, 2026
**Topic:** Dev restores follow-through to existing priorities; the stale priority label is corrected and method formal parameters become records.

**Provenance:** Original Codex session `01a1404f-0fa1-7f03-abd0-fff9bf4b100b`; see the [bounded extract](sources/codex-2026-10-15-to-21.jsonl) and [retrieval audit](sources/codex-2026-10-15-to-21-audit.md). Assertive prompts and completed replies are verbatim apart from surrounding whitespace and repository-link normalization. Responsive command results are summarized as delegated-action notes; originals remain in the extract. Tools, reasoning and intermediate progress are excluded. Historical command blocks are dialogue, not instructions to execute now.

---

**[Dev]** *2026-10-19T15:10:16.484Z; source line 1739; extract record 45*

What's next? How have we ended here with no follow-up taking us to existing high priority tasks?

---

**[Codex]** *2026-10-19T15:10:57.588Z; source line 1776; extract record 46*

I treated each accepted increment as the whole task and let the handoff’s “next action” shrink to testing and committing. I should have kept its connection to the larger work visible and brought you the next design point after the commit.

I checked the current backlog:

- **[#51 — context-independent type expressions](https://github.com/ejb816/nexonix/issues/51)** is the only open issue labelled `priority-next`. Dracon advances its neutral vocabulary requirement, but its older scope and decisions need reconciliation with the implemented tree.
- **[#43 — codec aspect emission](https://github.com/ejb816/nexonix/issues/43)** is partially advanced by our payload syntax; discriminator and broader codec declarations remain unfinished.
- **[#12 — Dreams](https://github.com/ejb816/nexonix/issues/12)** now has authoring and explicit generation. Activation remains deliberately deferred by your instruction.

**My recommendation is to return to #51 next:** audit its remaining requirements against the current definitions and parser, identify what Dracon and earlier increments have already completed, and bring you the first remaining language-neutrality design point. That reconnects this work to the existing highest-priority issue without reopening settled decisions or automatically starting activation.

Accept or correct that next scope.

---

**[Dev]** *2026-10-19T15:14:33.689Z; source line 1783; extract record 47*

Let's return to #51 as you suggest.

---

**[Codex]** *2026-10-19T15:15:09.782Z; source line 1812; extract record 48*

The audit found that #51 has advanced substantially: type forms are structured, collections have neutral operators, TypeName equality is structural, and Dracon now carries core definition trees.

One correction to my earlier recommendation: its `priority-next` label was scoped to the derivation work completed in August. The broader issue remains open, but that label is stale.

A concrete remaining gap is **method type parameters embedded in names**. Ten declarations across the current main/mods JSON corpus still carry names such as:

```json
"name": "updated[V1 >: V]"
```

This includes `Presence.fold`, `ifThenElse`, and Dracon’s `decode`. Drake presents neutral syntax, but the underlying name still contains Scala syntax.

**I recommend separating a method’s name from its formal type-parameter declarations.** `updated` would be the name; `V1` and its lower bound `V` would be structured declaration data. The existing Drake spelling `updated(V1 >: V)` would remain, and Scala generation would reconstruct its target spelling.

That closes a specific gap identified in [#51](https://github.com/ejb816/nexonix/issues/51), without changing TypeName’s actual-argument representation in the same increment. Accept or correct this boundary; the exact declaration shape would be the next design point.

---

**[Dev]** *2026-10-19T16:49:03.536Z; source line 1819; extract record 49*

Proceed with what you recommend.

---

**[Codex]** *2026-10-19T16:49:14.322Z; source line 1822; extract record 50*

The next design point is the formal-parameter shape.

I recommend a **`TypeParameter` record** containing a parameter `name` and optional `upperBound` and `lowerBound`, each bound using the existing Dracon type-form representation. A Dynamic element would carry `typeParameters`, defaulting to an empty list.

For example:

```drake
dyn updated(V1 >: V) {K, V1}
```

would carry:

```json
{
  "kind": "Dynamic",
  "name": "updated",
  "typeParameters": [
    { "name": "V1", "lowerBound": "V" }
  ],
  "valueType": { "{}": ["K", "V1"] }
}
```

This proposal revises the older issue’s flat-bound representation to use today’s structured type forms. Codec requirements remain explicit capability parameters, as accepted for Dracon; no Scala context-bound syntax is introduced.

Accept or correct this shape before I build it.

---

**[Dev]** *2026-10-19T16:50:03.501Z; source line 1829; extract record 51*

I accept this shape.

---

**[Codex]** *2026-10-19T16:56:18.317Z; source line 2079; extract record 52*

Implemented structured method formals and migrated all ten declarations. Their Drake syntax and generated Scala signatures remain unchanged.

Direct compiled suite: **825 tests passed**. One new corpus type accounts for the count increases; all loss figures remain unchanged.

Please run the final gate—expected **825 tests / 57 suites**:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
set -o pipefail
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
grep "mods actors pending" target/test-output/DrakeGenTest.log
```

After verification and commit, we’ll continue #51’s remaining formal/actual representation and neutral-vocabulary work with the next design point.

*(Dev ran the full suite: 825 tests / 57 suites, zero failures or aborted suites; reported headlines matched the increment’s baseline. The file-only pending mods-actor count was 0. Delegated action; 2026-10-19T17:31:28.137Z, source line 2086, extract record 53.)*

---

**[Codex]** *2026-10-19T17:31:55.668Z; source line 2101; extract record 54*

Recorded **825 tests / 57 suites** as verified. All headlines match; only documentation changed after your gate.

Run this scoped commit/push block and paste the result:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md
  draco-git-record/git-record-2026-10-19-1254
  src/main/resources/draco/{Draco,Dynamic,TypeParameter}.{drake,json}
  src/main/resources/draco/Dictionary.json
  src/main/resources/draco/drake.dlt
  src/main/resources/draco/drake/{Absent,Presence,Present}.json
  src/main/resources/draco/format/dracon/Value.json
  src/main/resources/draco/scalatarget/CirceCodecs.json
  src/main/scala/draco/{Draco,TypeElement,TypeParameter}.scala
  src/mods/scala/draco/{DracoGenerator,Drake,MethodTypeParameters}.scala
  src/test/scala/draco/{DrakeParseTest,MethodTypeParameterTest}.scala
)
git add -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F draco-git-record/git-record-2026-10-19-1254 -- "${paths[@]}" &&
git push origin main &&
git status --short --branch
```

After confirmation, I’ll bring forward the next #51 design point: how type-level formal declarations relate to actual arguments, now that method formals have an explicit representation.

*(Dev committed and pushed a13ad38; the pasted final status is clean main...origin/main. Delegated action; 2026-10-19T23:11:31.809Z, source line 2108, extract record 55.)*

---

**[Codex]** *2026-10-19T23:14:08.705Z; source line 2113; extract record 56*

Confirmed: **a13ad38** is committed and pushed, with a clean tree. Verified baseline: **825 tests / 57 suites**.

The next #51 design point is **separating type-level formal declarations from actual arguments**.

I recommend that the owning type’s Draco aspect declare its formals using `TypeParameter`, while `TypeName.typeParameters` continues to carry argument terms.

For example, in `Holon(T <: Product)`:

- The declaration owns formal `T` and its upper bound `Product`.
- Its TypeName carries `T`; a reference such as `Holon((S, T))` carries the tuple argument.
- Bounds constrain substitution; they do not become part of the variable’s identity.

This would resolve the current mixing of declarations and arguments in one field while preserving compound actual arguments.

Accept or correct this ownership boundary before we settle its exact encoding.

---

## Status

Dev committed and pushed a13ad38 after **825 tests / 57 suites**, all headlines matching. Ten generic method declarations now carry plain names plus Dynamic.typeParameters records. TypeParameter has name, upperBound and lowerBound, bounds carried as Dracon type forms; missing bounds are Null. Drake syntax and generated Scala signatures remain unchanged. One new corpus type raises scope to 109+10, measured types to 119, aspect sections to 109 and GenDrake to 107/107. Record-backed probe detail: a round-trip helper initially mistook formal records for expressions; it was corrected. An unsupported nested-method test was removed without expanding grammar. The proposal at the close to move type-level formals into DracoAspect is unaccepted; chapter 99 prioritizes ontology scope. The priority-next label on #51 referred to the completed August derivation work, not every remaining proposal.
