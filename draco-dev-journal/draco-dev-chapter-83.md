# Chapter 83 - Shared Journal and Development Continuity

**Session date:** September 23, 2026
**Topic:** Explicit Claude Code and Codex handoffs, shared documentation and preservation of agent links.

**Provenance:** Selected written prompts from this Codex task, followed by an explicitly
labeled account of the documentation work. Earlier development is recorded in chapters
81-82; moving tasks between app projects does not change repository history. This is
not a verbatim transcript of every intermediate tool call or response.

**[Dev]**

I've moved all sessions (other than this one) out of this (nexonix) project and into the edwardburke project. The reason for this is I want this session to be available to start supporting the continuity of draco-dev-journal, DRACO.md and README.md, through the handoffs between using Claude Code and Codex. The moved sessions are all earlier in the nexonx timeline which is already covered by draco-dev-journal, which allows us to focus more easily on the most recent changes to nexonix/draco made by Codex, to update all of the important, project embedded documentation and memory.

---

**[Dev]**

The last time I updated draco-dev-journal, Claude Code interpreted my prompt as permission to do it, rather than handoff to Cowork. This was after I switched from Opus to Fable as the working model for Claude Code. The update was successful, using the the information embedded in the nexonix project, which included rules about updating draco-dev-journal , as well as DRACO.md and README.md. While using Codex, we can add to the rules to support explicit handoff between Claude Code and Codex, in both directions, to manage continuous development of the project. Codes can continue with the permission that Claude Code assumed implicitly.

---

**[Dev]**

I had previously symbolically linked  AGENTS.md to DRACO.md with the idea that it would correspond to having CLAUDE.md (in the project) symbolically linked to DRACO.md. At one point it looked like you were creating a directory called AGENTS or AGENTS.md which I was concerned would conflict with the AGENTS.md symbolic link, somehow. When I checked the the project files and directories, I saw AGENTS.md was still a symbolic link to DRACO.md. Did you create a file or directory outside of the project that had AGENTS in the name?

*Editorial verification note:* Both repository agent entrypoints were inspected as
symlinks to DRACO.md and tracked with Git mode 120000. The documentation update writes
DRACO.md itself, not either link. This inspection does not establish a complete audit
of historical filesystem actions outside the repository. No outside AGENTS file or
directory is created by this documentation increment.

---

**[Dev]**

Are you ready to do the markdown file updates, including draco-dev-journal, DRACO.md, README.md and any other project local memories or skills that may need updating?

**[Dev]**

Please proceed with the updates.

**[Codex]**

I'll reconcile the journal's chapter-80 endpoint with the later commits and available
session records, then update the documentation and project-local continuity notes.
I'll distinguish sourced dialogue from factual summaries wherever transcript evidence
is missing.

---

## Documentation Work

*Editorial account, not reconstructed dialogue:* The Cowork-only rule was superseded
by Dev's explicit authorization. DRACO.md now governs both agents and retains the
restrictions on running sbt, committing, pushing and creating issues. DEVELOPMENT_HANDOFF.md
holds the shared resumption checkpoint: code state, verification scope, documentary
coverage, evidence and remaining decisions. Private auto-memory is a retrieval aid, not
the authority for the other agent; neither agent's private store was rewritten.

Chapter 81 was recovered from the actual Claude Code session after chapter 80 rather
than inferred from commit messages. Its first conditional full-run failure is preserved.
Chapter 82 records the Codex development and successful Dev-run commit/push, identifying
the initial Text portion as a summary rather than invented dialogue. The introduction,
DRACO.md and README.md were reconciled through this chapter; historical chapter Status
sections keep their historical numbers rather than being rewritten to today's count.

The project-local documentation skill still called the agent links ignored, instructed
automatic repairs and modified .gitignore even during a check. It was corrected to use
read-only inspection by default, acknowledge tracked links and require explicit repair
authorization. This skill lives under ignored .draco and is a local convenience, not a
portable dependency. The common protocol is in tracked documents.

## Status

The development checkpoint remains `7de07ba`, with Dev's prior 652-test / 46-suite full
run. This increment changes documentation and a local documentation helper, not runtime
source; no new sbt run is claimed. Journal coverage now reaches chapter 83, with disclosed
transcript limits in chapters 81-82. Documentation changes are not yet committed or pushed
at this chapter's close. Dev performs those actions. No additional Draco feature or issue
has been authorized. Either Claude Code or Codex can resume from DEVELOPMENT_HANDOFF.md
after checking it against the actual working tree.
