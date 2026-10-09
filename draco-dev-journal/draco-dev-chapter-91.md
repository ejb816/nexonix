# Chapter 91 - Authoring Plan and Dreams Name Skeleton

**Session date:** 2026-10-06–2026-10-07
**Topic:** The local authoring proposal and the three empty Dreams domains.

**Provenance:** Original Codex message records; see the [bounded extract](sources/codex-2026-10-05-to-07.jsonl) and [audit](sources/codex-2026-10-05-to-07-audit.md). Prompts and final replies are verbatim apart from surrounding and line-end whitespace. Responsive command results are summarized in italic notes; their originals remain in the extract. Historical commands are evidence only.

---

**[Dev]** *2026-10-06T14:14:38.833Z; source line 463*

What do you recommend on how to proceed with the implementation of the next increment?

---

**[Codex]** *2026-10-06T14:15:21.178Z; source line 475*

I recommend an **end-to-end local authoring increment**, using the endogenous client to prove the shared service operations before connecting them to ZeroMQ.

The first milestone should be: **initialize a dictionary from supplied canonical JSON definitions, create a type, modify it, and reject an invalid change without damaging the accepted dictionary.**

I would implement it in this order:

1. **Build dictionaries from supplied definitions.**
   Add an in-memory composition path that takes `TypeDefinition` values and selected domain identities. Today, `DomainBuilder` loads definitions from resources, while `TypeDictionary.apply` creates name-only placeholders. Neither directly supports client-authored definitions.

2. **Define the service messages and results in Drake.**
   Start with operations for submitting a batch of definition changes, validating it, composing a dictionary, and retrieving the accepted state. Results should identify the request and carry either the resulting state or structured problems. Keep the Drake, JSON and generated Scala artifacts synchronized under `src/mods`.

3. **Process changes through service actor rules.**
   I propose validating a candidate batch before accepting it atomically. This lets a new domain and its members arrive together. Reject duplicate identities, inconsistent domain membership and unresolved required references; preserve the previous accepted state on failure.

4. **Add the endogenous initialization client.**
   It supplies canonical JSON through existing actor messages and receives the same results as future external authoring requests. The consuming project decides where those definitions come from and how accepted changes are persisted.

There is a validation issue to address explicitly: existing rules resolve some parents through `TypeLoader`, and self-declaration checks omit type parameters. Authoring validation must examine the **supplied candidate definitions and complete identities**, rather than accidentally validating against older classpath resources.

The tests should demonstrate initialization, creation, replacement, related changes in one batch, rejection without partial mutation, and retrieval of the resulting dictionary. They should also establish that authoring neither generates nor activates code.

`ServiceConfiguration` should contain `Assembly`, as agreed, with only the additional fields this working path actually requires. Once this local path passes, the following increment can connect ZeroMQ domain-input rules to those same operations. The bridge’s current startup dictionary snapshot should remain unchanged until live dictionary replacement has an explicit design.

No edits made during this review.

---

**[Dev]** *2026-10-07T13:42:26.222Z; source line 485*

Before we start this increment, I would like to extend and enhance the name skeleton beyond draco core, into the dreams layer, with draco.dreams.Dreams, draco.dreams.editor.Editor, draco.dreams.user.User domains, as the next layer surrounding the draco core. Will these domains be useful and can they be added without negatively impacting the next increment?

---

**[Codex]** *2026-10-07T13:43:17.363Z; source line 502*

**Yes. These domains would give the next increment a useful organizational home, and adding them as empty domain skeletons should have limited impact.** I recommend a small, separately verified increment before authoring implementation.

Their provisional responsibilities could be:

| Domain | Intended scope |
|---|---|
| `draco.dreams.Dreams` | The surrounding layer for constructing and evolving Draco-defined systems. |
| `draco.dreams.editor.Editor` | Definition-authoring operations and editing workflows, using Draco validation and dictionary primitives. |
| `draco.dreams.user.User` | User-facing participation and interaction concepts, with specific members left open. |

This lets `draco.service.Service` retain reusable service capabilities while `Editor` provides the authoring application. The endogenous initialization client can use the same authoring operations without depending on user interaction.

There is one existing artifact to reconcile: [Dreams.scala](/Users/edwardburke/IdeaProjects/nexonix/src/main/scala/draco/dreams/Dreams.scala) already declares an empty, handwritten `Dreams` trait. It has no definition trio or companion. We must replace or relocate that scaffold when establishing the real domain, rather than introduce a duplicate fully qualified name. An empty `draco.dreams.Service` trait also exists; its relationship to the newer `draco.service.Service` should be documented explicitly.

For the skeleton increment, I recommend:

- Establish all three domains as synchronized Drake/JSON/generated Scala trios, staged consistently under `src/mods`.
- Keep membership empty initially.
- Compose them as dictionary peers. Package nesting alone should not imply super-domain inheritance or registration as members of `Draco`.
- Add projection, loading and dictionary-composition tests, then run the full suite.

That establishes the vocabulary without changing transport behavior, validation semantics or the authoring protocol. We can then place the next increment’s editing definitions in `Editor`, deciding their responsibilities as they become concrete.

No edits made yet.

---

**[Dev]** *2026-10-07T14:20:48.266Z; source line 509*

Proceed with adding the dreams domains to the draco name skeleton.

---

**[Codex]** *2026-10-07T14:24:41.462Z; source line 555*

Added **Dreams, Editor and User** as empty domain trios under `src/mods`. They compose as dictionary peers without implied inheritance or membership.

The generated Dreams replaces the old handwritten scaffold. Documentation and handoff are updated.

**Verified:** 555 tests / 7 suites passed in the isolated probe, including 16 new tests. Measured corpus baselines are unchanged.

Please run the full gate, expected **701 tests / 50 suites**:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix &&
set -o pipefail &&
sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|DomainAspect over|DracoAspect over|CO-DECLARATION|error|Failed|FAILED|^\[info\] (Tests:|Suites:)"
```

I haven’t run sbt, committed or pushed.

*(Delegated action — Dev ran the full sbt suite and supplied stdout: 701 tests / 50 suites passed, zero failures or aborted suites; reported baselines were unchanged. Source line 562, 2026-10-07T14:30:17.957Z.)*

---

**[Codex]** *2026-10-07T14:31:14.225Z; source line 577*

Confirmed: **701 tests / 50 suites passed**, with no failures or aborted suites. All report-only baselines match, including **zero mods actors pending Drake authoring**.

I recorded the result and prepared the commit message. Only documentation changed after your run.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix &&
paths=(
  CHANGELOG.md DEVELOPMENT_HANDOFF.md DRACO.md README.md
  draco-git-record/git-record-2026-10-07-1022
  src/main/scala/draco/dreams/Dreams.scala
  src/mods/README.md
  src/mods/resources/draco/dreams/Dreams.drake
  src/mods/resources/draco/dreams/Dreams.json
  src/mods/resources/draco/dreams/README.md
  src/mods/resources/draco/dreams/editor/Editor.drake
  src/mods/resources/draco/dreams/editor/Editor.json
  src/mods/resources/draco/dreams/user/User.drake
  src/mods/resources/draco/dreams/user/User.json
  src/mods/resources/draco/service/README.md
  src/mods/scala/draco/dreams/Dreams.scala
  src/mods/scala/draco/dreams/editor/Editor.scala
  src/mods/scala/draco/dreams/user/User.scala
  src/mods/scala/scripts/list-domains.scala
  src/test/scala/draco/dreams/DreamsTest.scala
) &&
test -s /tmp/draco-dreams-commit-message.txt &&
git diff --check &&
git add -A -- "${paths[@]}" &&
git diff --cached --check &&
git commit -F /tmp/draco-dreams-commit-message.txt -- "${paths[@]}" &&
git push origin main &&
git status --short --branch
```

I’ll wait for your results before continuing.

*(Delegated action — Dev committed and pushed f54da3f to origin/main; the supplied output ended with a clean main...origin/main status. Source line 584, 2026-10-07T14:38:35.372Z.)*

---

**[Codex]** *2026-10-07T14:38:42.343Z; source line 587*

Confirmed: **`f54da3f` committed and pushed successfully**, with a clean working tree.

Dreams, Editor and User are now established as empty domain peers. The verified baseline is **701 tests / 50 suites**, with unchanged report-only figures.

We can now return to the local authoring increment, placing editing operations in `draco.dreams.editor.Editor` while retaining reusable service capabilities in `draco.service.Service`.

## Status

At this historical closing point, f54da3f was committed and pushed, with a clean tree reported by Dev. The Dreams skeleton was implemented and verified by Dev: 701 tests / 50 suites, unchanged report-only baselines. Candidate-batch acceptance and detailed authoring types remained proposals; no authoring behavior was added.
