#!/usr/bin/env python3
"""
Integrate missing pairs from a chapter's gap JSON into the chapter's markdown file,
producing an updated copy. The original chapter is not modified.

Strategy:
  1. Parse existing chapter into Dev/Draco blocks (preserving everything)
  2. Discover how many existing pairs there are, map them to their pairIndexes by
     taking the first N indexes NOT in the gap set (where N = existing Dev prompt count)
  3. Insert gap entries at their correct pairIndex positions
  4. Write the full updated chapter

Usage:
  integrate_chapter.py <chapter_number>

Reads:  <tools>/gaps/chapter-NN.json  and  <journal>/draco-dev-chapter-NN.md
Writes: <tools>/gaps/chapter-NN-updated.md
"""
import json
import os
import re
import sys


def chapter_paths(chapter_num):
    tools_dir  = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    journal    = os.path.abspath(os.path.join(tools_dir, ".."))
    gaps_dir   = os.path.join(tools_dir, "gaps")
    nn         = f"{chapter_num:02d}"
    return {
        "gaps":     os.path.join(gaps_dir, f"chapter-{nn}.json"),
        "chapter":  os.path.join(journal,  f"draco-dev-chapter-{nn}.md"),
        "updated":  os.path.join(gaps_dir, f"chapter-{nn}-updated.md"),
    }


def format_dev_prompt(prompt):
    """Format a user prompt as a Dev blockquote."""
    result_lines = []
    for i, line in enumerate(prompt.split('\n')):
        if i == 0:
            result_lines.append(f'> **Dev:** {line}')
        else:
            if line.strip():
                result_lines.append(f'> {line}')
            else:
                result_lines.append('>')
    return '\n'.join(result_lines)


def trim_response(response, max_chars=2000):
    """Trim very long responses to essential content at paragraph boundaries."""
    if len(response) <= max_chars:
        return response
    paragraphs = response.split('\n\n')
    kept = []
    char_count = 0
    for p in paragraphs:
        if char_count + len(p) > max_chars and kept:
            break
        kept.append(p)
        char_count += len(p) + 2
    result = '\n\n'.join(kept)
    if len(result) < len(response) - 100:
        result += '\n\n[Response continued with additional detail.]'
    return result


def format_gap_block(gap):
    """Format a gap entry as markdown lines."""
    result = [format_dev_prompt(gap['userPrompt']), '']
    tool_summary = gap.get('toolSummary', '')
    response = trim_response(gap['assistantResponse'])
    if tool_summary:
        result.append(f'**Draco:** {tool_summary} {response}')
    else:
        result.append(f'**Draco:** {response}')
    result.append('')
    return result


def main(chapter_num):
    paths = chapter_paths(chapter_num)

    for key, required_path in (("gaps", paths["gaps"]), ("chapter", paths["chapter"])):
        if not os.path.exists(required_path):
            print(f"error: missing {key} file at {required_path}", file=sys.stderr)
            sys.exit(1)

    with open(paths["gaps"]) as f:
        data = json.load(f)
    gaps = data['gaps']
    gap_indices = set(g['pairIndex'] for g in gaps)

    with open(paths["chapter"]) as f:
        chapter_text = f.read()

    lines = chapter_text.split('\n')
    dev_lines = [i for i, line in enumerate(lines) if line.startswith('> **Dev:**')]
    existing_count = len(dev_lines)

    if existing_count == 0:
        print(f"error: found 0 Dev prompts in {paths['chapter']}", file=sys.stderr)
        sys.exit(1)

    # Existing pairs occupy the first `existing_count` pairIndexes that are NOT gaps.
    if gaps:
        max_pair_index = max(g['pairIndex'] for g in gaps)
        universe_upper = max(max_pair_index + 1, existing_count + len(gaps))
    else:
        universe_upper = existing_count
    existing_indices = [i for i in range(universe_upper) if i not in gap_indices][:existing_count]

    print(f"Gap indices:     {sorted(gap_indices)}")
    print(f"Existing (first {existing_count}): {existing_indices}")
    print(f"Total pairs will be: {existing_count + len(gaps)}")
    print(f"\nFound {existing_count} Dev prompts at lines: {dev_lines}")

    header_end = dev_lines[0]

    blocks = []
    for i, dl in enumerate(dev_lines):
        start = dl
        end   = dev_lines[i + 1] if i + 1 < len(dev_lines) else len(lines)
        blocks.append({
            'pair_index': existing_indices[i],
            'lines':      lines[start:end],
            'type':       'existing',
        })

    gap_blocks = [
        {'pair_index': g['pairIndex'], 'lines': format_gap_block(g), 'type': 'gap'}
        for g in gaps
    ]

    all_blocks = blocks + gap_blocks
    all_blocks.sort(key=lambda b: b['pair_index'])

    output_lines = lines[:header_end]
    for block in all_blocks:
        output_lines.extend(block['lines'])

    with open(paths["updated"], 'w') as f:
        f.write('\n'.join(output_lines))

    print(f"\nWrote updated chapter to {paths['updated']}")
    print(f"Total blocks: {len(all_blocks)}")

    for i in range(1, len(all_blocks)):
        if all_blocks[i]['pair_index'] <= all_blocks[i-1]['pair_index']:
            print(
                f"WARNING: Order issue at blocks {i-1} and {i}: "
                f"indices {all_blocks[i-1]['pair_index']} and {all_blocks[i]['pair_index']}"
            )


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"usage: {os.path.basename(sys.argv[0])} <chapter_number>", file=sys.stderr)
        sys.exit(1)
    try:
        main(int(sys.argv[1]))
    except ValueError:
        print(f"error: chapter number must be an integer, got '{sys.argv[1]}'", file=sys.stderr)
        sys.exit(1)
