#!/usr/bin/env python3
"""
Build an insertions file for a chapter by reading its gap JSON and formatting
each missing pair as a Dev/Draco markdown block, suitable for manual integration.

Usage:
  build_chapter.py <chapter_number>

Reads:  <tools>/gaps/chapter-NN.json
Writes: <tools>/gaps/chapter-NN-insertions.md
"""
import json
import os
import re
import sys
import textwrap


def chapter_paths(chapter_num):
    tools_dir  = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    journal    = os.path.abspath(os.path.join(tools_dir, ".."))
    gaps_dir   = os.path.join(tools_dir, "gaps")
    nn         = f"{chapter_num:02d}"
    return {
        "gaps":         os.path.join(gaps_dir, f"chapter-{nn}.json"),
        "chapter":      os.path.join(journal,  f"draco-dev-chapter-{nn}.md"),
        "insertions":   os.path.join(gaps_dir, f"chapter-{nn}-insertions.md"),
    }


def format_dev_prompt(prompt):
    """Format a user prompt as a Dev blockquote."""
    lines = prompt.split('\n')
    result = []
    for i, line in enumerate(lines):
        if i == 0:
            result.append(f'> **Dev:** {line}')
        else:
            result.append(f'> {line}')
    return '\n'.join(result)


def trim_response(response, max_chars=2000):
    """Trim long responses to essential content."""
    if len(response) <= max_chars:
        return response
    lines = response.split('\n')
    kept = []
    char_count = 0
    for line in lines:
        if char_count + len(line) > max_chars:
            break
        kept.append(line)
        char_count += len(line) + 1
    result = '\n'.join(kept)
    if len(result) < len(response):
        result += '\n\n[Response trimmed for brevity.]'
    return result


def format_draco_response(response, tool_summary):
    """Format an assistant response as a Draco response."""
    prefix = '**Draco:**'
    if tool_summary:
        prefix = f'**Draco:** {tool_summary}'
    trimmed = trim_response(response)
    return f'{prefix} {trimmed}'


def format_pair(gap):
    """Format a single gap entry as a Dev/Draco pair."""
    dev   = format_dev_prompt(gap['userPrompt'])
    draco = format_draco_response(gap['assistantResponse'], gap.get('toolSummary', ''))
    return f'{dev}\n\n{draco}'


def main(chapter_num):
    paths = chapter_paths(chapter_num)

    if not os.path.exists(paths["gaps"]):
        print(f"error: no gap file at {paths['gaps']}", file=sys.stderr)
        print("(Run map_and_detect_gaps.py first, or check the chapter number.)", file=sys.stderr)
        sys.exit(1)

    with open(paths["gaps"]) as f:
        data = json.load(f)
    gaps = data['gaps']
    total = len(gaps)

    with open(paths["insertions"], 'w') as f:
        f.write(f"# Chapter {chapter_num} — Missing Pairs for Insertion\n\n")
        f.write("Each section below is one missing pair, tagged with its pairIndex and timestamp.\n")
        f.write("Insert these into the chapter at the correct chronological position.\n\n")

        for i, gap in enumerate(gaps):
            f.write("---\n\n")
            f.write(f"<!-- GAP {i+1}/{total} pairIndex={gap['pairIndex']} ts={gap['timestamp']} -->\n\n")
            f.write(format_pair(gap))
            f.write('\n\n')

    print(f"Wrote {total} insertions to {paths['insertions']}")
    print("\nPair index mapping (gap pairIndex -> where it goes):")
    print("Existing chapter has pairs at indices NOT in this list.")
    print("Gap indices:", [g['pairIndex'] for g in gaps])


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"usage: {os.path.basename(sys.argv[0])} <chapter_number>", file=sys.stderr)
        sys.exit(1)
    try:
        main(int(sys.argv[1]))
    except ValueError:
        print(f"error: chapter number must be an integer, got '{sys.argv[1]}'", file=sys.stderr)
        sys.exit(1)
