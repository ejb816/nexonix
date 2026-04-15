#!/usr/bin/env python3
"""
Map extracted session pairs to chapters, detect missing pairs.

Two-pass strategy:
  Pass 1: Match prompts + validate timestamps against chapter dates (high confidence)
  Pass 2: For chapters with <50% date-validated matches, use prompt matching alone
           (because many chapters say "March 23 continued" but sessions ran earlier)
Then build time windows from all validated matches and detect gaps.
"""

import json
import os
import re
import sys
from pathlib import Path
from difflib import SequenceMatcher
from datetime import datetime, timedelta, date

JOURNAL_DIR = os.path.join(os.path.dirname(__file__), "..")
EXTRACTED_DIR = os.path.join(os.path.dirname(__file__), "extracted")
GAPS_DIR = os.path.join(os.path.dirname(__file__), "gaps")
MAPPING_PATH = os.path.join(os.path.dirname(__file__), "mapping.json")

os.makedirs(GAPS_DIR, exist_ok=True)

MONTH_MAP = {
    "January": 1, "February": 2, "March": 3, "April": 4,
    "May": 5, "June": 6, "July": 7, "August": 8,
    "September": 9, "October": 10, "November": 11, "December": 12
}


def normalize_text(text):
    text = re.sub(r'\*\*', '', text)
    text = re.sub(r'\*\(.*?\)\*', '', text)
    text = re.sub(r'```[\s\S]*?```', '[code block]', text)
    text = re.sub(r'>\s*', '', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text.lower()


def extract_chapter_prompts(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        text = f.read()
    prompts = []
    parts = re.split(r'(?=>\s*\*\*Dev:\*\*)', text)
    for part in parts:
        match = re.match(r'>\s*\*\*Dev:\*\*\s*(.*?)(?=\n\n\*\*Draco:\*\*|\n\*\*Draco:\*\*|\Z)', part, re.DOTALL)
        if match:
            prompt_text = match.group(1)
            prompt_text = re.sub(r'\n>\s*', '\n', prompt_text).strip()
            prompts.append(prompt_text)
    return prompts


def extract_chapter_date(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        for line in f:
            if line.startswith("**Session date:**"):
                return line.strip().replace("**Session date:**", "").strip()
    return ""


def parse_chapter_dates(date_str):
    dates = set()
    clean = date_str.split("(")[0].strip()
    clean = re.sub(r',\s*\d+:\d+\s*(AM|PM)\s*\w*', '', clean)
    for month_name, month_num in MONTH_MAP.items():
        if month_name in clean:
            day_match = re.search(r'(\d+)(?:[–-](\d+))?,\s*(\d{4})', clean)
            if day_match:
                day_start = int(day_match.group(1))
                day_end = int(day_match.group(2)) if day_match.group(2) else day_start
                year = int(day_match.group(3))
                for day in range(day_start, day_end + 1):
                    try:
                        dates.add(date(year, month_num, day))
                    except ValueError:
                        pass
            break
    return dates


def extract_chapter_session_id(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        for line in f:
            if line.startswith("**Session ID:**"):
                return line.strip().replace("**Session ID:**", "").strip()
    return None


def load_all_extracted():
    sessions = {}
    for fname in os.listdir(EXTRACTED_DIR):
        if fname.startswith("_") or not fname.endswith(".json"):
            continue
        with open(os.path.join(EXTRACTED_DIR, fname), 'r', encoding='utf-8') as f:
            data = json.load(f)
        sessions[data["sessionId"]] = data
    return sessions


def fuzzy_match_score(text1, text2):
    t1 = normalize_text(text1)
    t2 = normalize_text(text2)
    n = min(200, len(t1), len(t2))
    if n < 10:
        return 1.0 if t1 == t2 else 0.0
    return SequenceMatcher(None, t1[:n], t2[:n]).ratio()


def parse_timestamp(ts):
    if not ts:
        return None
    try:
        return datetime.fromisoformat(ts.replace("Z", "+00:00"))
    except:
        return None


def timestamp_on_dates(ts_str, valid_dates):
    ts = parse_timestamp(ts_str)
    if not ts:
        return False
    for d in valid_dates:
        dt_start = datetime(d.year, d.month, d.day, 0, 0, tzinfo=ts.tzinfo) - timedelta(hours=6)
        dt_end = datetime(d.year, d.month, d.day, 23, 59, 59, tzinfo=ts.tzinfo) + timedelta(hours=6)
        if dt_start <= ts <= dt_end:
            return True
    return False


def main():
    sessions = load_all_extracted()
    print(f"Loaded {len(sessions)} extracted sessions.")

    # Discover chapters dynamically — one per draco-dev-chapter-NN.md in the journal dir
    import glob as _glob
    chapter_files = sorted(_glob.glob(os.path.join(JOURNAL_DIR, "draco-dev-chapter-*.md")))
    chapter_nums = []
    for cf in chapter_files:
        m = re.search(r"draco-dev-chapter-(\d+)\.md$", cf)
        if m:
            chapter_nums.append(int(m.group(1)))

    chapters = {}
    for ch_num in chapter_nums:
        fname = f"draco-dev-chapter-{ch_num:02d}.md"
        fpath = os.path.join(JOURNAL_DIR, fname)
        if not os.path.exists(fpath):
            continue
        chapters[ch_num] = {
            "filename": fname,
            "date": extract_chapter_date(fpath),
            "dates": parse_chapter_dates(extract_chapter_date(fpath)),
            "explicitSession": extract_chapter_session_id(fpath),
            "existingPrompts": extract_chapter_prompts(fpath),
            "matchedSessions": set(),
            "matchedPairIndices": {},
        }

    print(f"Loaded {len(chapters)} chapters.\n")

    all_pairs_flat = []
    for sid, sdata in sessions.items():
        for idx, pair in enumerate(sdata["pairs"]):
            all_pairs_flat.append((sid, idx, pair))

    # === Pass 1: Prompt matching with date validation ===
    print("=== Pass 1: Prompt matching with date validation ===\n")

    # Store all prompt->pair matches (with and without date validation)
    prompt_matches = {}  # (ch_num, prompt_idx) -> (sid, pair_idx, score, date_valid)

    for ch_num, ch_info in sorted(chapters.items()):
        for pi, prompt in enumerate(ch_info["existingPrompts"]):
            best_score = 0
            best_match = None
            for sid, idx, pair in all_pairs_flat:
                score = fuzzy_match_score(prompt, pair["userPrompt"])
                if score > best_score:
                    best_score = score
                    best_match = (sid, idx, pair)

            if best_score >= 0.6 and best_match:
                sid, idx, pair = best_match
                date_valid = timestamp_on_dates(pair["timestamp"], ch_info["dates"])
                prompt_matches[(ch_num, pi)] = (sid, idx, best_score, date_valid)

    # Count date-validated matches per chapter
    for ch_num, ch_info in sorted(chapters.items()):
        total = len(ch_info["existingPrompts"])
        date_valid_count = sum(1 for (cn, pi), (_, _, _, dv) in prompt_matches.items()
                               if cn == ch_num and dv)
        any_match_count = sum(1 for (cn, pi) in prompt_matches if cn == ch_num)
        date_pct = (date_valid_count / total * 100) if total > 0 else 0

        # Decision: if >30% of prompts have date-validated matches, require date validation
        # Otherwise, accept prompt-only matches (chapter dates are transcription dates, not session dates)
        use_date_filter = date_pct > 30

        ch_info["useDateFilter"] = use_date_filter
        ch_info["matchedPairIndices"] = {}

        for pi, prompt in enumerate(ch_info["existingPrompts"]):
            key = (ch_num, pi)
            if key not in prompt_matches:
                continue
            sid, idx, score, date_valid = prompt_matches[key]

            if use_date_filter and not date_valid:
                continue

            ch_info["matchedSessions"].add(sid)
            if sid not in ch_info["matchedPairIndices"]:
                ch_info["matchedPairIndices"][sid] = set()
            ch_info["matchedPairIndices"][sid].add(idx)

        # Add explicit session
        if ch_info["explicitSession"] and ch_info["explicitSession"] in sessions:
            ch_info["matchedSessions"].add(ch_info["explicitSession"])

        matched = sum(len(v) for v in ch_info["matchedPairIndices"].values())
        mode = "DATE" if use_date_filter else "PROMPT"
        sessions_str = ', '.join(sorted(s[:8] for s in ch_info['matchedSessions']))
        print(f"Ch {ch_num:02d} [{mode:6s}]: {total:2d} prompts, "
              f"{matched:2d} matched ({date_valid_count} date-valid, {any_match_count} any-match) "
              f"-> [{sessions_str}]")

    # === Step 2: Build time windows ===
    print("\n=== Step 2: Time windows ===\n")

    chapter_windows = {}
    for ch_num, ch_info in sorted(chapters.items()):
        for sid, indices in ch_info["matchedPairIndices"].items():
            sdata = sessions[sid]
            timestamps = []
            for idx in indices:
                ts = parse_timestamp(sdata["pairs"][idx]["timestamp"])
                if ts:
                    timestamps.append(ts)
            if timestamps:
                min_ts = min(timestamps) - timedelta(minutes=10)
                max_ts = max(timestamps) + timedelta(minutes=10)
                chapter_windows[(ch_num, sid)] = (min_ts, max_ts)
                print(f"  Ch {ch_num:02d} / {sid[:8]}: "
                      f"{min_ts.strftime('%m-%d %H:%M')} to {max_ts.strftime('%m-%d %H:%M')} "
                      f"({len(indices)} matched)")

    # === Step 3: Detect gaps ===
    print("\n=== Step 3: Detecting gaps ===\n")

    globally_matched = set()
    for ch_info in chapters.values():
        for sid, indices in ch_info["matchedPairIndices"].items():
            for idx in indices:
                globally_matched.add((sid, idx))

    pair_to_chapter = {}

    for sid, sdata in sessions.items():
        for idx, pair in enumerate(sdata["pairs"]):
            if (sid, idx) in globally_matched:
                continue

            ts = parse_timestamp(pair["timestamp"])
            if not ts:
                continue

            best_ch = None
            best_distance = float('inf')

            for (ch_num, win_sid), (win_min, win_max) in chapter_windows.items():
                if win_sid != sid:
                    continue

                if win_min <= ts <= win_max:
                    best_ch = ch_num
                    best_distance = 0
                    break
                else:
                    dist = min(abs((ts - win_max).total_seconds()),
                               abs((ts - win_min).total_seconds()))
                    if dist < 1800 and dist < best_distance:
                        best_distance = dist
                        best_ch = ch_num

            if best_ch is not None:
                pair_to_chapter[(sid, idx)] = best_ch

    # Build reports
    total_gaps = 0
    mapping = {}

    for ch_num, ch_info in sorted(chapters.items()):
        chapter_gaps = []

        for (sid, idx), assigned_ch in pair_to_chapter.items():
            if assigned_ch != ch_num:
                continue

            pair = sessions[sid]["pairs"][idx]

            is_dup = False
            for ep in ch_info["existingPrompts"]:
                if fuzzy_match_score(pair["userPrompt"], ep) >= 0.5:
                    is_dup = True
                    break
            if is_dup:
                continue

            chapter_gaps.append({
                "sessionId": sid,
                "pairIndex": idx,
                "timestamp": pair["timestamp"],
                "userPrompt": pair["userPrompt"],
                "toolSummary": pair["toolSummary"],
                "assistantResponse": pair["assistantResponse"],
                "promptPreview": pair["promptPreview"],
            })

        chapter_gaps.sort(key=lambda g: g["timestamp"])

        # Deduplicate
        seen = set()
        deduped = []
        for gap in chapter_gaps:
            norm = normalize_text(gap["userPrompt"])[:100]
            if norm not in seen:
                seen.add(norm)
                deduped.append(gap)
        chapter_gaps = deduped

        total_gaps += len(chapter_gaps)
        print(f"Ch {ch_num:02d}: {len(chapter_gaps):3d} gaps "
              f"({len(ch_info['existingPrompts'])} existing)")

        if chapter_gaps:
            gap_path = os.path.join(GAPS_DIR, f"chapter-{ch_num:02d}.json")
            with open(gap_path, 'w', encoding='utf-8') as f:
                json.dump({
                    "chapter": ch_num,
                    "filename": ch_info["filename"],
                    "date": ch_info["date"],
                    "existingPromptCount": len(ch_info["existingPrompts"]),
                    "matchedSessions": sorted(ch_info["matchedSessions"]),
                    "gaps": chapter_gaps
                }, f, indent=2, ensure_ascii=False)
        else:
            gap_path = os.path.join(GAPS_DIR, f"chapter-{ch_num:02d}.json")
            if os.path.exists(gap_path):
                os.remove(gap_path)

        mapping[str(ch_num)] = {
            "filename": ch_info["filename"],
            "date": ch_info["date"],
            "sessions": sorted(ch_info["matchedSessions"]),
            "existingPrompts": len(ch_info["existingPrompts"]),
            "matchedPairs": sum(len(v) for v in ch_info["matchedPairIndices"].values()),
            "gaps": len(chapter_gaps)
        }

    with open(MAPPING_PATH, 'w', encoding='utf-8') as f:
        json.dump(mapping, f, indent=2)

    print(f"\nTotal gaps: {total_gaps}")
    total_pairs = sum(len(s["pairs"]) for s in sessions.values())
    print(f"Extracted: {total_pairs}, Matched: {len(globally_matched)}, "
          f"Gaps: {len(pair_to_chapter)}, Unassigned: "
          f"{total_pairs - len(globally_matched) - len(pair_to_chapter)}")


if __name__ == "__main__":
    main()
