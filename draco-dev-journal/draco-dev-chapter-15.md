# Draco Dev Journal — Chapter 15

**Session date:** March 24, 2026
**Topic:** Journal Creation Methodology — How to Create a Session Journal

---

## Overview

This chapter documents how to create a development journal from Claude Code sessions. The process is simple: resume each session in chronological order, issue a fixed transcription prompt, and the model transcribes its own session into the next chapter. The format is self-propagating — one chapter establishes the conventions, and every subsequent session can replicate them.

---

## The Problem

Development sessions in Claude Code contain everything — prompts, responses, tool calls, code changes, architectural discussions, false starts, breakthroughs. But session data is opaque. A journal extracts that into readable narrative that humans and future AI sessions can reference, preserving not just what changed but the reasoning behind the changes.

---

## The Mechanism: `/resume`

Claude Code's `/resume` command switches into a previous session, restoring its full conversational context. The model re-enters that session as if it had never left — it can see every message exchanged, every tool call made, every file read or written.

This is the key capability: the model doesn't reconstruct what happened from git diffs or file timestamps. It has the actual session data. And since it participated in the original session, it is both subject and author — recounting its own work, not interpreting someone else's.

---

## The Process

### Step 1: Bootstrap — Write the First Chapter by Hand

Start a new session (or use the current one). Create the journal directory and write the first chapter from the earliest session you want to capture. This establishes the format conventions:

- **Dev:** and **Draco:** (or whatever names suit) as speaker labels in bold
- Blockquotes for user prompts
- Code blocks for code, tool calls, and terminal output
- Section headers for major topics
- Verbatim fidelity to what was actually said

Alternatively, resume the earliest session and write the first chapter there, establishing the format as you go.

### Step 2: Resume Each Session in Chronological Order

For each subsequent session, from earliest to most recent:

1. `/resume` — select the target session
2. Issue the fixed transcription prompt (see below)
3. The model reads the format exemplar, then transcribes its own session as the next chapter
4. `/resume` — select the next session, or return to the orchestrating session

Because sessions are visited in chronological order, chapter numbers match session order naturally. No renumbering is needed, and cross-references to earlier chapters are always references to sessions that actually preceded the current one.

### Step 3: Write the Introduction

After all chapters are transcribed, create an introduction (Chapter 0 or a separate intro file) by reading all chapters and synthesizing the development trajectory, key breakthroughs, and a chapter index with hyperlinks.

---

## The Fixed Prompt

The transcription prompt that each resumed session receives:

> Read draco-dev-journal/draco-dev-chapter-01.md for the format. Transcribe this session as the next chapter. Use the other chapter for format and structure information only. The content itself should come exclusively from this session.

This does three things:

1. **Points to a format exemplar** — the model reads an existing chapter to learn the conventions
2. **Instructs transcription** — "Transcribe this session as the next chapter"
3. **Prevents contamination** — "The content itself should come exclusively from this session"

The prompt is deliberately minimal. The conventions are self-evident from the exemplar. Any resumed session given this prompt and one chapter as reference can produce the next chapter without further instruction. Each new chapter becomes another potential exemplar — the format is self-propagating.

---

## The Process in Summary

```
1.  Create journal directory and write the first chapter (bootstrap)

For each subsequent session (in chronological order):
    2.  /resume → select the target session
    3.  Issue the fixed prompt:
        "Read draco-dev-journal/draco-dev-chapter-01.md for the format.
         Transcribe this session as the next chapter.
         Use the other chapter for format and structure information only.
         The content itself should come exclusively from this session."
    4.  The model transcribes its own session into the next chapter file
    5.  /resume → continue to next session

After all chapters are written:
    6.  Write the introduction by reading all chapters and synthesizing
```

---

## Why This Works

1. **Session data is the source of truth.** `/resume` restores the full conversational context — the model reads what actually happened, not a reconstruction.

2. **The format is self-propagating.** One chapter establishes all conventions. The fixed prompt points to it, and every subsequent session replicates the format without further instruction.

3. **The model is its own witness.** Each session is transcribed by the same model that participated in it. No lossy translation between participant and narrator.

4. **Chronological order prevents confusion.** When sessions are visited earliest-first, chapter numbers match session order. Cross-references to earlier chapters always point backward in time, which is the natural direction for "the plan from Chapter N" or "the fix from Chapter N."

The limitation: the transcription is the model's perspective. Dev's internal reasoning, motivations beyond what was stated in prompts, and reactions not expressed in the session are absent. The journal captures what was said and done, not everything that was thought.

---

## Historical Note

The original Draco journal was created in reverse chronological order — the most recent session was transcribed first (as Chapter 1) and each `/resume` went further back in time. This was pragmatic (the most recent session was already active) but required renumbering all files afterward, updating every title heading and cross-reference, and correcting cases where the transcribing model had confused chronological predecessors with successors. The simplified chronological approach described above eliminates all of that post-processing.

---

## Session Summary

This chapter documents the journal creation methodology for reuse in future development stretches. The core insight: `/resume` plus a fixed prompt makes session transcription a mechanical process. Chronological order makes it a clean one.
