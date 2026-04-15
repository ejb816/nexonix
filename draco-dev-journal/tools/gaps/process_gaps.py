#!/usr/bin/env python3
"""
Print a compact summary of each gap in a chapter's gap JSON — useful for eyeballing
what's missing without opening the raw JSON.

Usage:
  process_gaps.py <chapter_number>

Reads: <tools>/gaps/chapter-NN.json
"""
import json
import os
import sys


def chapter_gap_path(chapter_num):
    tools_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    return os.path.join(tools_dir, "gaps", f"chapter-{chapter_num:02d}.json")


def main(chapter_num):
    path = chapter_gap_path(chapter_num)
    if not os.path.exists(path):
        print(f"error: no gap file at {path}", file=sys.stderr)
        sys.exit(1)

    with open(path) as f:
        data = json.load(f)
    gaps = data['gaps']
    total = len(gaps)

    for i, gap in enumerate(gaps):
        prompt = gap['userPrompt']
        if len(prompt) > 300:
            prompt = prompt[:300] + '...'

        response = gap['assistantResponse']
        if len(response) > 1500:
            response = response[:1500] + f'... [TRUNCATED - total len: {len(response)}]'

        tool_summary = gap.get('toolSummary', '')

        print(f"=== GAP {i+1}/{total} === pairIndex={gap['pairIndex']} ts={gap['timestamp']}")
        print(f"TOOLS: {tool_summary}")
        print(f"PROMPT: {prompt}")
        print(f"RESPONSE: {response}")
        print()


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"usage: {os.path.basename(sys.argv[0])} <chapter_number>", file=sys.stderr)
        sys.exit(1)
    try:
        main(int(sys.argv[1]))
    except ValueError:
        print(f"error: chapter number must be an integer, got '{sys.argv[1]}'", file=sys.stderr)
        sys.exit(1)
