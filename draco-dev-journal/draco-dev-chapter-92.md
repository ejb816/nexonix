# Chapter 92 - Domain Ownership and Claude Handoff

**Session date:** 2026-10-07
**Topic:** Proposed type-placement criteria and preservation before switching agents.

**Provenance:** Original Codex message records; see the [bounded extract](sources/codex-2026-10-05-to-07.jsonl) and [audit](sources/codex-2026-10-05-to-07-audit.md). Prompts and final replies are verbatim apart from surrounding and line-end whitespace. Responsive command results are summarized in italic notes; their originals remain in the extract. Historical commands are evidence only.

---

**[Dev]** *2026-10-07T14:53:52.419Z; source line 594*

Can you describe how to decide which new type definitions in the authoring increment should be added to the dreams domains, and which need to be added to existing draco domains? Should we depend on all such decisions be collaborative?

---

**[Codex]** *2026-10-07T14:54:21.732Z; source line 597*

The deciding factor should be **what a type means and owns**, rather than which component first needs it.

| Placement | Use when the definition represents… |
|---|---|
| Existing Draco domains | A general framework concept needed independently of editing: type/domain structure, dictionary composition, validation or generation. Prefer extending an existing concept when appropriate. |
| `draco.service.Service` | Reusable service capabilities and configuration: `ServiceConfiguration`, communication capabilities, and service lifecycle contracts. |
| `draco.dreams.editor.Editor` | Definition-authoring operations and their workflow: proposed changes, edit requests, validation requests, acceptance/rejection results and editing state. |
| `draco.dreams.user.User` | The person’s participation in that workflow, once concrete requirements establish what needs representation. |
| `draco.dreams.Dreams` | Concepts shared across Dreams applications that have no more specific home. It should not become a miscellaneous container. |

For example, an **edit request** belongs in Editor, while its payload can use the existing `TypeDefinition`. An **editing validation result** may belong in Editor while carrying existing Draco `Problem` values. The meaning of domain consistency belongs to core; deciding whether to accept a proposed edit belongs to the authoring workflow.

A useful dependency check is: **could core still describe, validate and generate definitions without knowing that Editor exists?** It should. Editor can depend on core and service capabilities; those reusable layers should not acquire editing-specific dependencies.

For uncertain cases, I recommend starting in the narrowest justified domain. A type used only by the editor does not yet demonstrate that it needs a framework-wide home. We can promote it when another concrete use establishes shared meaning.

**Placement should be collaborative at architectural boundaries, but not require your approval for every definition.** For each increment, I should first present a short inventory of proposed types, their homes and their responsibilities. Once we agree on those boundaries, I can implement routine details and document the reasoning.

I should bring a decision back to you when it introduces a new domain, changes an agreed responsibility, creates a cross-layer dependency, or reveals that a previously local concept needs broader ownership. That preserves your architectural direction without turning every implementation choice into a permission checkpoint.

---

**[Dev]** *2026-10-07T15:02:25.859Z; source line 604*

Is the the current task-session context large enough to consider a hand-off to Claude? I don't want to lose verbatim exchanges to automatic context compression. If there is sufficient new task content, we should update and/or add to draco-dev-journal, DRACO.md, README.md, etc., as needed before a hand-off.

## Status

Record-backed maintenance note, not a recovered final reply: this documentation pass adds chapters 90–92 and their bounded transcript/audit, reconciles shared documentation and prepares the Claude handoff. HEAD is f54da3f, matching local origin/main; pending changes are documentation only. The latest full-suite evidence remains Dev’s 701 tests / 50 suites. Domain-placement and collaboration guidance above are Codex recommendations, not newly accepted policy. The source boundary includes the unanswered handoff request; this pass’s eventual final response is outside the extract.
