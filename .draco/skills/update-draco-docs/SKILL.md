---
name: update-draco-docs
description: Reconcile Draco journal chapters with DRACO.md, README.md and DEVELOPMENT_HANDOFF.md when Dev requests a documentation or continuity update. Inspect shared agent symlinks without replacing them. Manually triggered only.
---

# Update Draco Docs

Work from the current nexonix checkout root (`git rev-parse --show-toplevel`), not a
machine-specific path. Read DRACO.md section 7 and DEVELOPMENT_HANDOFF.md first.
This shared skill and its link checker are version-controlled project procedures.
DRACO.md remains the authority if instructions disagree; correct the stale skill.
Either agent can read this file directly when requested to update documentation.
Automatic skill discovery is not required, and no per-agent copy should be created.

## Evidence and Scope

- Inspect branch, HEAD and staged/unstaged changes. Preserve existing work.
- Read journal Status, introduction and both docs' `draco-docs-synced-through` markers.
  A chapter marker describes reviewed journal coverage, not all code currency.
- Verify architecture against source, changes against commits and git records, dialogue
  against actual transcripts and delegated actions against Dev's output. Summaries
  cannot establish verbatim dialogue or a passing run.
- For journal updates after compaction, preserve a bounded extract of original messages
  with source identity, source-line sequence, UTC timestamps and text hashes. Report
  user messages, final replies, complete exchanges, omissions and open turns separately.
  Deduplicate event mirrors; never use replacement-history summaries as original dialogue.
  Test recoverability on both sides of observed compactions without claiming that the
  absence of a detected gap proves complete historical capture. Audit existing quotations
  in the covered range; retain their historical Status. Do not copy tools, reasoning,
  credentials or unrelated conversation material into the project transcript.
- Both Claude Code and Codex may maintain the journal under Dev's September 23 permission;
  Cowork is not mandatory. This does not authorize sbt, commits, pushes or new issues.

## Journal and Documentation

When requested, update the journal using its introduction's contract: Session date and
Topic, verbatim assertive Dev prompts, near-verbatim agent-labeled responses, responsive
output folded into italic delegated-action notes, closing Status. Identify excerpts and
omissions. Label record-backed summaries explicitly; never invent dialogue. Preserve
failed runs and corrections. Keep old statuses historical, current state in the handoff.

Read unsynced chapters in sequence and reconcile DRACO.md and README.md against code.
Carry durable decisions, architecture, baselines and pitfalls, not the entire narrative.
Later corrections override proposals. Verify changed paths and symbols. Update the index
and advance markers only through reviewed chapters; also check older affected sections.

Refresh DEVELOPMENT_HANDOFF.md: branch/HEAD, tree changes, implemented versus tested
versus committed/pushed, evidence and test scope, coverage/gaps, next authorized action.
Do not infer authorization from recommendations or overwrite private agent memory to
simulate synchronization. Write CHANGELOG and the corresponding git record together.

## Agent Links

CLAUDE.md and AGENTS.md are tracked relative symlinks to DRACO.md (Git mode 120000).
.claude is a local symlink to .draco. Only this SKILL.md and scripts/check_links.sh
are allowlisted for version control; other .draco contents remain ignored.
Ignore entries do not untrack files.
Edit DRACO.md directly, never either agent link.

Run `bash .draco/skills/update-draco-docs/scripts/check_links.sh <repo-root>` to inspect.
Report mode must not mutate files or .gitignore. Missing links require explicit repair
authorization before `--fix`, which only creates missing links. Conflicting files,
directories or wrong-target links need a separate decision: never replace them
automatically, even if a file matches DRACO.md. Do not add AGENTS directories or copies.

## Verification and Handoff

- Check staged and unstaged diffs, whitespace, chapter links/index, coverage markers,
  source attribution and symlinks. Confirm no runtime changes for docs-only work.
- Syntax-check helper changes and exercise report mode; these checks are not sbt.
- Include this skill and helper in the scoped commit when they change. Do not force-add
  ignored settings, worktrees, private memory or other skills.
- Do not run sbt, commit or push. When requested, give Dev a path-scoped command with
  a message file and `git commit -F <file> -- <paths>`, never a heredoc message.
- Report documents changed, coverage, evidence gaps and checks performed.
