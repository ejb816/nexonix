#!/usr/bin/env python3
"""
Extract (dev prompt, model response) pairs from Claude Code JSONL session files.
Produces one JSON file per session with chronologically ordered conversation pairs.
"""

import json
import os
import sys
import glob
import re
from pathlib import Path
from collections import defaultdict

PROJECTS_DIR = os.path.expanduser("~/.claude/projects")


def _project_root():
    """Repo root — two levels up from this script (draco-dev-journal/tools/<script>)."""
    return os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))


def _claude_project_encoding(abs_path):
    """Claude Code encodes absolute filesystem paths for ~/.claude/projects/ by replacing
    both '/' and '.' with '-'. So /Users/a/b/.hidden/c → -Users-a-b--hidden-c."""
    return abs_path.replace("/", "-").replace(".", "-")


_PROJECT_ENCODED = _claude_project_encoding(_project_root())
MAIN_DIR = os.path.join(PROJECTS_DIR, _PROJECT_ENCODED)
# Worktrees live under <project>/.draco/worktrees/<name>, encoded as <main>--draco-worktrees-<name>
WORKTREE_PATTERN = os.path.join(PROJECTS_DIR, _PROJECT_ENCODED + "--draco-worktrees-*")
OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "extracted")

os.makedirs(OUTPUT_DIR, exist_ok=True)


def find_all_jsonl_files():
    """Find all JSONL session files across main project and worktrees."""
    files = []
    # Main project sessions
    for f in glob.glob(os.path.join(MAIN_DIR, "*.jsonl")):
        files.append(f)
    # Worktree sessions
    for wt_dir in glob.glob(WORKTREE_PATTERN):
        for f in glob.glob(os.path.join(wt_dir, "*.jsonl")):
            files.append(f)
    return sorted(files)


def parse_jsonl(filepath):
    """Parse a JSONL file into a list of message objects."""
    messages = []
    with open(filepath, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, 1):
            line = line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
                messages.append(obj)
            except json.JSONDecodeError:
                print(f"  Warning: skipped malformed JSON at line {line_num}", file=sys.stderr)
    return messages


def extract_text_from_content(content):
    """Extract plain text from message content (string or array of blocks)."""
    if isinstance(content, str):
        return content.strip()
    if isinstance(content, list):
        texts = []
        for block in content:
            if isinstance(block, dict):
                if block.get("type") == "text":
                    texts.append(block.get("text", "").strip())
            elif isinstance(block, str):
                texts.append(block.strip())
        return "\n\n".join(t for t in texts if t)
    return ""


def is_real_user_prompt(content):
    """Check if a user message is a real dev prompt (not a tool_result continuation)."""
    if isinstance(content, str):
        return True
    if isinstance(content, list):
        # If the first block is tool_result, it's a continuation, not a new prompt
        for block in content:
            if isinstance(block, dict):
                if block.get("type") == "tool_result":
                    return False
                if block.get("type") == "text":
                    return True
        # If all blocks are strings, it's a prompt
        if all(isinstance(b, str) for b in content):
            return True
    return False


def summarize_tool_uses(tool_blocks):
    """Convert tool_use blocks into narrative summaries."""
    summaries = []
    for tool in tool_blocks:
        name = tool.get("name", "unknown")
        inp = tool.get("input", {})

        if name in ("Read", "read"):
            fp = inp.get("file_path", "?")
            fname = os.path.basename(fp)
            summaries.append(f"Reads {fname}")
        elif name in ("Write", "write"):
            fp = inp.get("file_path", "?")
            fname = os.path.basename(fp)
            summaries.append(f"Creates {fname}")
        elif name in ("Edit", "edit"):
            fp = inp.get("file_path", "?")
            fname = os.path.basename(fp)
            summaries.append(f"Edits {fname}")
        elif name in ("Bash", "bash"):
            cmd = inp.get("command", "")
            if len(cmd) > 60:
                cmd = cmd[:57] + "..."
            summaries.append(f"Runs: {cmd}")
        elif name in ("Glob", "glob"):
            pattern = inp.get("pattern", "?")
            summaries.append(f"Searches for {pattern}")
        elif name in ("Grep", "grep"):
            pattern = inp.get("pattern", "?")
            summaries.append(f"Greps for '{pattern}'")
        elif name == "Agent":
            desc = inp.get("description", inp.get("prompt", "")[:50])
            summaries.append(f"Launches agent: {desc}")
        elif name == "TodoWrite":
            summaries.append("Updates task list")
        elif name == "AskUserQuestion":
            summaries.append("Asks user a question")
        else:
            summaries.append(f"Uses {name}")

    if not summaries:
        return ""
    return " ".join(f"*({s})*" for s in summaries)


def extract_conversation_pairs(messages):
    """
    Extract (user prompt, assistant response) pairs from parsed messages.

    A conversation pair consists of:
    1. A real user prompt (not a tool_result)
    2. All assistant responses until the next real user prompt
    3. Tool uses between are summarized into narrative
    """
    # Index messages by uuid for parent chain traversal
    by_uuid = {}
    for msg in messages:
        uuid = msg.get("uuid")
        if uuid:
            by_uuid[uuid] = msg

    # Build conversation turns
    pairs = []
    current_prompt = None
    current_prompt_time = None
    assistant_texts = []
    tool_uses = []

    for msg in messages:
        msg_type = msg.get("type")
        content = msg.get("message", {}).get("content", "")
        timestamp = msg.get("timestamp", "")

        if msg_type == "user":
            if is_real_user_prompt(content):
                # Save previous pair if exists
                if current_prompt is not None and (assistant_texts or tool_uses):
                    tool_summary = summarize_tool_uses(tool_uses)
                    response_text = "\n\n".join(assistant_texts)
                    pairs.append({
                        "timestamp": current_prompt_time,
                        "userPrompt": current_prompt,
                        "toolSummary": tool_summary,
                        "assistantResponse": response_text,
                        "promptPreview": current_prompt[:150].replace("\n", " ")
                    })

                # Start new pair
                current_prompt = extract_text_from_content(content)
                current_prompt_time = timestamp
                assistant_texts = []
                tool_uses = []

            # tool_result continuations — just skip (not a new prompt)

        elif msg_type == "assistant":
            if isinstance(content, list):
                for block in content:
                    if isinstance(block, dict):
                        if block.get("type") == "text":
                            text = block.get("text", "").strip()
                            if text:
                                assistant_texts.append(text)
                        elif block.get("type") == "tool_use":
                            tool_uses.append(block)
                        # Skip thinking blocks

    # Don't forget the last pair
    if current_prompt is not None and (assistant_texts or tool_uses):
        tool_summary = summarize_tool_uses(tool_uses)
        response_text = "\n\n".join(assistant_texts)
        pairs.append({
            "timestamp": current_prompt_time,
            "userPrompt": current_prompt,
            "toolSummary": tool_summary,
            "assistantResponse": response_text,
            "promptPreview": current_prompt[:150].replace("\n", " ")
        })

    return pairs


def process_session(filepath):
    """Process a single JSONL session file and output extracted pairs."""
    session_id = os.path.splitext(os.path.basename(filepath))[0]
    print(f"Processing {session_id} ({os.path.getsize(filepath) / 1024:.0f} KB)...")

    messages = parse_jsonl(filepath)
    if not messages:
        print(f"  No messages found, skipping.")
        return

    pairs = extract_conversation_pairs(messages)

    # Get session time range
    timestamps = [m.get("timestamp", "") for m in messages if m.get("timestamp")]
    timestamps = [t for t in timestamps if t]

    result = {
        "sessionId": session_id,
        "sourceFile": filepath,
        "startTime": min(timestamps) if timestamps else "",
        "endTime": max(timestamps) if timestamps else "",
        "totalMessages": len(messages),
        "extractedPairs": len(pairs),
        "pairs": pairs
    }

    output_path = os.path.join(OUTPUT_DIR, f"{session_id}.json")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(result, f, indent=2, ensure_ascii=False)

    print(f"  Extracted {len(pairs)} conversation pairs -> {output_path}")
    return result


def main():
    files = find_all_jsonl_files()
    print(f"Found {len(files)} JSONL session files.\n")

    summaries = []
    for filepath in files:
        result = process_session(filepath)
        if result:
            summaries.append({
                "sessionId": result["sessionId"],
                "startTime": result["startTime"],
                "endTime": result["endTime"],
                "pairCount": result["extractedPairs"]
            })

    # Write summary
    summary_path = os.path.join(OUTPUT_DIR, "_summary.json")
    with open(summary_path, 'w', encoding='utf-8') as f:
        json.dump(summaries, f, indent=2)
    print(f"\nSummary written to {summary_path}")
    print(f"Total pairs extracted: {sum(s['pairCount'] for s in summaries)}")


if __name__ == "__main__":
    main()
