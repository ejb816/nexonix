#!/usr/bin/env python3
"""
draco-reviews/checks/review_checks.py — reproducible, read-only measurements for a DRACO review.

Usage:  python3 draco-reviews/checks/review_checks.py [repo-root]     (default: cwd)

Runs no sbt and no git write. Uses `git` read-only (log, tag, ls-files). Prints a Markdown block
that a review pastes verbatim, so every number a review quotes has one source and one scope.

Every section states the scope it counted. A number without its scope is the class of error the
2026-08-15 review made (53 vs 81 companions) and the 2026-08-28 review had to correct.

Sections:
  1 head            commit, date, tags, dirty count
  2 corpus          JSON / .drake / .scala by tree
  3 codec           top-level codecAspect carriers (expected: none while codec is inferred)
  4 draco.json      elementTypeNames vs sibling files, and ordering vs GeneratorCLI category order
  5 companions      `extends App` occurrences and files, by tree
  6 lazy-val        `override val` / eager `val` in App companions (discipline: lazy val only)
  7 actors          messageType carriers, Actor/ActorType derivations, latent isEmpty trap
  8 derivations     package-less (foreign) references, explicit DracoType spellings
  9 tests           test files, literal test(...) sites, report-only `succeed`, assert-free suites
 10 test-writes     tests that write files, with the paths they name and whether those are tracked
 11 exclusions      gate hold-outs: DracoGenTest.excluded, DrakeGenTest/DrakeParseTest.authoredAhead,
                    DrakeParseTest.inlineTupleArgument, comparisonOnlyExcluded
 12 versions        build.sbt / README / CLI.scala / latest tag
 13 renderers       operator sets of the three expression renderers (drift check)
 14 workflows       .github/workflows triggers
 15 deps            build.sbt dependencies with zero import hits (heuristic map)
 16 docs            symlinks, retired names in root docs, doc-status claims, dead-weight presence
 17 history         journal reach, latest git-record, CHANGELOG blocks, last claimed test counts
"""
import json
import os
import re
import subprocess
import sys
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else os.getcwd()).resolve()


# ----------------------------------------------------------------------------- helpers
def sh(*args, default=""):
    try:
        return subprocess.run(args, cwd=ROOT, capture_output=True, text=True, check=False).stdout.strip()
    except Exception:
        return default


def files(glob):
    return sorted(p for p in ROOT.glob(glob) if p.is_file())


def rel(p):
    return str(Path(p).resolve().relative_to(ROOT)) if isinstance(p, (str, Path)) else p


def read(p):
    try:
        return Path(p).read_text(encoding="utf-8", errors="replace")
    except Exception:
        return ""


def load_json(p):
    try:
        return json.loads(read(p))
    except Exception:
        return None


def grep_count(pattern, paths, flags=re.M):
    rx = re.compile(pattern, flags)
    total, hit_files = 0, 0
    for p in paths:
        n = len(rx.findall(read(p)))
        if n:
            total += n
            hit_files += 1
    return total, hit_files


def h(title):
    print(f"\n### {title}\n")


TRACKED = set(sh("git", "ls-files").splitlines())


def tracked(path):
    return path in TRACKED


# ----------------------------------------------------------------------------- 1 head
def sec_head():
    h("1. HEAD")
    print(f"- root: `{ROOT}`")
    print(f"- HEAD: `{sh('git', 'log', '-1', '--format=%h %ad %s', '--date=short')}`")
    tags = sh("git", "tag", "--sort=-creatordate").splitlines()
    print(f"- tags ({len(tags)}): {', '.join(f'`{t}`' for t in tags[:5]) or 'none'}")
    if tags:
        print(f"- latest tag points at: `{sh('git', 'log', '-1', '--format=%h %ad', '--date=short', tags[0])}`")
    dirty = [l for l in sh("git", "status", "--short").splitlines() if l.strip()]
    print(f"- working tree: {len(dirty)} changed/untracked path(s){' — counts below include them' if dirty else ''}")
    print(f"- tracked files: {len(TRACKED)}")


# ----------------------------------------------------------------------------- 2 corpus
TREES = ["src/main/resources", "src/mods/resources", "src/test/resources",
         "src/main/scala", "src/mods/scala", "src/test/scala"]


def sec_corpus():
    h("2. Corpus (scope: each tree separately, then src/ total)")
    print("| tree | .json | .drake | .scala | .dlt |")
    print("|---|---|---|---|---|")
    tot = Counter()
    for t in TREES:
        c = Counter()
        for ext in ("json", "drake", "scala", "dlt"):
            c[ext] = len(files(f"{t}/**/*.{ext}"))
            tot[ext] += c[ext]
        print(f"| `{t}` | {c['json']} | {c['drake']} | {c['scala']} | {c['dlt']} |")
    print(f"| **src/ total** | **{tot['json']}** | **{tot['drake']}** | **{tot['scala']}** | {tot['dlt']} |")
    # JSON/drake pairing under the two definition corpora
    for t in ("src/main/resources/draco", "src/mods/resources"):
        js = {p.with_suffix("") for p in files(f"{t}/**/*.json")}
        dk = {p.with_suffix("") for p in files(f"{t}/**/*.drake")}
        print(f"- `{t}`: {len(js)} JSON, {len(dk)} drake; JSON without drake: {len(js - dk)}, drake without JSON: {len(dk - js)}")
    sc = files("src/test/resources/scenario/**/*.drake")
    scj = files("src/test/resources/scenario/**/*.json")
    print(f"- `src/test/resources/scenario`: {len(sc)} drake, {len(scj)} JSON (drake-first corpus; outside DracoGenTest/DrakeGenTest)")


# ----------------------------------------------------------------------------- 3 codec
def all_defs():
    out = []
    for t in ("src/main/resources", "src/mods/resources", "src/test/resources"):
        for p in files(f"{t}/**/*.json"):
            d = load_json(p)
            if isinstance(d, dict) and "typeName" in d:
                out.append((p, d))
    return out


def sec_codec(defs):
    h("3. codecAspect (scope: every definition JSON under src/*/resources)")
    carriers = [rel(p) for p, d in defs if "codecAspect" in d]
    print(f"- definitions scanned: {len(defs)}")
    print(f"- top-level `codecAspect` carriers: {len(carriers)}" + (f" — {', '.join(carriers)}" if carriers else " (codec is still inferred structurally)"))


# ----------------------------------------------------------------------------- 4 Draco.json
def sec_draco_json(defs):
    h("4. Draco.json elementTypeNames (scope: src/main/resources/draco/*.json, flat)")
    p = ROOT / "src/main/resources/draco/Draco.json"
    d = load_json(p)
    if not d:
        print("- Draco.json not found/parsed")
        return
    names = d.get("domainAspect", {}).get("elementTypeNames", [])
    sib = {q.stem: load_json(q) for q in files("src/main/resources/draco/*.json")}
    sib.pop("Draco", None)
    missing = sorted(set(sib) - set(names))
    extra = sorted(set(names) - set(sib))
    print(f"- listed: {len(names)}; sibling JSON (excluding Draco itself): {len(sib)}")
    print(f"- missing from list: {missing or 'none'}")
    print(f"- listed but no JSON: {extra or 'none'}")

    def cat(n):
        j = sib.get(n) or {}
        if j.get("actorAspect"):
            return 2
        if j.get("ruleAspect"):
            return 1
        return 0

    expected = sorted(names, key=lambda n: (cat(n), n))
    if expected == list(names):
        print("- order matches GeneratorCLI category order (types, rules, actors; alphabetical within)")
    else:
        i = next(i for i, (a, b) in enumerate(zip(names, expected)) if a != b)
        print(f"- order DIVERGES from category order at index {i}: listed `{names[i]}`, expected `{expected[i]}` → `draco-gen verify Draco.json` would exit non-zero")


# ----------------------------------------------------------------------------- 5 companions
def sec_companions():
    h("5. `extends App` companions (scope stated per row)")
    print("| scope | occurrences | files |")
    print("|---|---|---|")
    for label, glob in [("src/main/scala/draco (flat dir only)", "src/main/scala/draco/*.scala"),
                        ("src/main/scala/draco (whole tree)", "src/main/scala/draco/**/*.scala"),
                        ("src/main/scala/org (whole tree)", "src/main/scala/org/**/*.scala"),
                        ("src/mods/scala/draco", "src/mods/scala/draco/**/*.scala"),
                        ("src/mods/scala/domains", "src/mods/scala/domains/**/*.scala")]:
        n, f = grep_count(r"\bextends App\b", files(glob))
        print(f"| `{label}` | {n} | {f} |")


# ----------------------------------------------------------------------------- 6 lazy-val
def sec_lazy():
    h("6. lazy-val discipline (scope: files containing `extends App` under src/main/scala + src/mods/scala; plus Generator.scala emitted templates)")
    rx_over = re.compile(r"^\s*override val\b", re.M)
    hits = []
    for p in files("src/main/scala/**/*.scala") + files("src/mods/scala/**/*.scala"):
        txt = read(p)
        if "extends App" not in txt:
            continue
        for m in rx_over.finditer(txt):
            hits.append((rel(p), txt.count("\n", 0, m.start()) + 1))
    print(f"- `override val` (not lazy) in App-companion files: {len(hits)}")
    for f, l in hits[:20]:
        print(f"  - `{f}:{l}`")
    gen = read(ROOT / "src/mods/scala/draco/Generator.scala")
    tmpl = [gen.count("\n", 0, m.start()) + 1 for m in re.finditer(r"override val", gen)]
    print(f"- `override val` in Generator.scala emitted templates: {len(tmpl)}" + (f" — lines {tmpl}" if tmpl else ""))


# ----------------------------------------------------------------------------- 7 actors
def sec_actors(defs):
    h("7. Actor-ness (scope: every definition JSON under src/*/resources)")
    carriers, latent, deriv_actor = [], [], []
    for p, d in defs:
        aa = d.get("actorAspect") or {}
        if "messageType" in aa:
            carriers.append(rel(p))
            bodies = [aa.get(k, {}).get("body") for k in ("start", "message", "signal")]
            if all(not b for b in bodies):
                latent.append(rel(p))
        for ref in (d.get("dracoAspect") or {}).get("derivation") or []:
            if ref.get("name") in ("Actor", "ActorType", "ExtensibleBehavior"):
                deriv_actor.append((rel(p), ref.get("name")))
    print(f"- `actorAspect.messageType` carriers: {len(carriers)}")
    for c in carriers:
        print(f"  - `{c}`")
    print(f"- carriers with ALL of start/message/signal empty (would be elided by ActorAspect.isEmpty → D-03 trap): {len(latent)}"
          + (" — " + ", ".join(f"`{x}`" for x in latent) if latent else ""))
    print(f"- definitions whose derivation names Actor / ActorType / ExtensibleBehavior: {len(deriv_actor)}")
    for f, n in deriv_actor:
        print(f"  - `{f}` → {n}")


# ----------------------------------------------------------------------------- 8 derivations
def sec_derivations(defs):
    h("8. Derivation census (scope: every definition JSON under src/*/resources)")
    refs, foreign, root_spelled, empty = 0, [], [], 0
    for p, d in defs:
        der = (d.get("dracoAspect") or {}).get("derivation") or []
        if not der:
            empty += 1
        for ref in der:
            refs += 1
            if not ref.get("namePackage"):
                foreign.append((rel(p), ref.get("name")))
            if ref.get("name") == "DracoType":
                root_spelled.append(rel(p))
    print(f"- definitions: {len(defs)}; derivation references: {refs}; definitions with empty derivation: {empty}")
    print(f"- package-less (foreign) references: {len(foreign)}" + ("".join(f"\n  - `{f}` → {n}" for f, n in foreign)))
    print(f"- definitions spelling `DracoType` explicitly (gate 2 does not root; a sole `[DracoType]` fails round-trip): {len(root_spelled)}"
          + ("".join(f"\n  - `{f}`" for f in root_spelled)))


# ----------------------------------------------------------------------------- 9 tests
def sec_tests():
    h("9. Tests (scope: src/test/scala; static — a suite count needs `sbt test`)")
    tfiles = files("src/test/scala/**/*.scala")
    suites = [p for p in tfiles if re.search(r"\bextends\s+AnyFunSuite\b|\bextends\s+AnyFlatSpec\b|\bextends\s+AnyWordSpec\b", read(p))]
    lit = 0
    report_only, assert_free = [], []
    for p in suites:
        txt = read(p)
        n = len(re.findall(r'^\s*test\s*\(', txt, re.M))
        lit += n
        if re.search(r"^\s*succeed\s*$", txt, re.M):
            report_only.append(rel(p))
        if n and not re.search(r"\b(assert|assertResult|assertThrows|fail|intercept|shouldBe|should\b|===)\b", txt):
            assert_free.append((rel(p), n))
    print(f"- test files: {len(tfiles)}; suites (FunSuite/FlatSpec/WordSpec): {len(suites)}; literal `test(...)` sites: {lit} (loop-generated tests not counted)")
    print(f"- suites ending a test in bare `succeed` (report-only): {len(report_only)}")
    for r in report_only:
        print(f"  - `{r}`")
    print(f"- suites with `test(...)` sites and no assertion idiom at all: {len(assert_free)}")
    for r, n in assert_free:
        print(f"  - `{r}` ({n} tests)")


# ----------------------------------------------------------------------------- 10 test writes
def sec_test_writes():
    h("10. Tests that write files (scope: src/test/scala; heuristic on write idioms)")
    rx_write = re.compile(r"(contentSink\.write|Files\.write|\.write\(|PrintWriter)")
    rx_sink = re.compile(r"ContentSink\(\s*Generator\.(\w+)\.sinkRoot\s*,\s*\"([^\"]+)\"")
    rx_path = re.compile(r"\"(src/[^\"]+)\"")
    sink_root = {"main": "src/main/scala/", "test": "src/test/scala/", "generated": "src/generated/"}
    for p in files("src/test/scala/**/*.scala"):
        txt = read(p)
        if not rx_write.search(txt):
            continue
        targets = []
        for m in rx_sink.finditer(txt):
            path = sink_root.get(m.group(1), f"<{m.group(1)}>/") + m.group(2)
            targets.append(path)
        for m in rx_path.finditer(txt):
            targets.append(m.group(1))
        lines = [str(txt.count("\n", 0, m.start()) + 1) for m in rx_write.finditer(txt)]
        print(f"- `{rel(p)}` (write at line {', '.join(lines)})")
        for t in sorted(set(targets)):
            flag = "TRACKED" if tracked(t) else ("untracked" if not (ROOT / t).exists() else "exists, untracked")
            print(f"  - → `{t}` — {flag}")
        if not targets:
            print("  - target not resolvable statically (see source)")


# ----------------------------------------------------------------------------- 11 exclusions
def sec_exclusions():
    h("11. Gate hold-outs (scope: the three round-trip suites)")

    def block(path, name):
        txt = read(ROOT / path)
        m = re.search(rf"val {name}\b[^=]*=\s*(Map|Set)\s*(\(|\.empty)", txt)
        if not m:
            return None
        if m.group(2) == ".empty":
            return []
        # collect string literals until the matching close paren (depth-aware)
        i, depth, buf = m.end() - 1, 0, []
        for j in range(i, len(txt)):
            c = txt[j]
            if c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
                if depth == 0:
                    buf = txt[i:j + 1]
                    break
        return re.findall(r'"([^"]+\.json)"\s*(?:->|,|\))', buf)

    for path, name in [("src/test/scala/draco/DracoGenTest.scala", "excluded"),
                       ("src/test/scala/draco/DracoGenTest.scala", "comparisonOnlyExcluded"),
                       ("src/test/scala/draco/DrakeGenTest.scala", "authoredAhead"),
                       ("src/test/scala/draco/DrakeParseTest.scala", "authoredAhead"),
                       ("src/test/scala/draco/DrakeParseTest.scala", "inlineTupleArgument")]:
        v = block(path, name)
        if v is None:
            print(f"- `{Path(path).name}.{name}`: not found")
        else:
            print(f"- `{Path(path).name}.{name}`: {len(v)}" + (" — " + ", ".join(f"`{x}`" for x in v) if v else ""))


# ----------------------------------------------------------------------------- 12 versions
def sec_versions():
    h("12. Version triple (+ tag)")
    b = re.search(r'version\s*:=\s*"([^"]+)"', read(ROOT / "build.sbt"))
    r = re.search(r"Current build version is `([^`]+)`", read(ROOT / "README.md"))
    c = re.search(r'println\("Draco ([^"]+)"\)', read(ROOT / "src/main/scala/draco/CLI.scala"))
    t = sh("git", "tag", "--sort=-creatordate").splitlines()
    vals = {"build.sbt": b and b.group(1), "README.md": r and r.group(1), "CLI.scala": c and c.group(1), "latest tag": t[0] if t else None}
    for k, v in vals.items():
        print(f"- {k}: `{v}`")
    distinct = {v.lstrip("v") for v in vals.values() if v}
    print(f"- distinct versions in play: {len(distinct)}" + (" — AGREE" if len(distinct) == 1 else " — DISAGREE"))


# ----------------------------------------------------------------------------- 13 renderers
def sec_renderers():
    h("13. Expression renderers — operator sets (scope: the three `case \"op\"` tables)")
    rx = re.compile(r'case\s+((?:"[^"]+"\s*\|\s*)*"[^"]+")\s*=>')
    sets = {}
    for label, path in [("Drake.expression", "src/mods/scala/draco/Drake.scala"),
                        ("Generator.expression", "src/mods/scala/draco/Generator.scala"),
                        ("SourceContract.ExpressionRenderer", "src/mods/scala/draco/SourceContract.scala")]:
        txt = read(ROOT / path)
        ops = set()
        for m in rx.finditer(txt):
            ops.update(re.findall(r'"([^"]+)"', m.group(1)))
        # keep only operator-looking keys (short, non-alphanumeric or a known keyword op)
        ops = {o for o in ops if (not re.fullmatch(r"[A-Za-z]\w*", o) or o == "if") and not o.startswith("$") and not re.fullmatch(r"(Seq|Set|Map)(\(\)|\.empty)", o)}
        sets[label] = ops
        print(f"- {label}: {sorted(ops)}")
    union = set().union(*sets.values())
    for label, ops in sets.items():
        miss = sorted(union - ops)
        if miss:
            print(f"- {label} LACKS: {miss}")
    # operators actually used in the corpus
    used = Counter()
    for p in files("src/main/resources/**/*.json") + files("src/mods/resources/**/*.json"):
        used.update(re.findall(r'\{\s*"([^"\w][^"]{0,3}|if|\\\\)"\s*:\s*\[', read(p)))
    print(f"- operator keys used in JSON values: {dict(sorted(used.items()))}")
    for k in used:
        for label, ops in sets.items():
            if k not in ops:
                print(f"  - `{k}` is used in the corpus but not rendered by {label}")


# ----------------------------------------------------------------------------- 14 workflows
def sec_workflows():
    h("14. CI workflows")
    wfs = files(".github/workflows/*.yml") + files(".github/workflows/*.yaml")
    if not wfs:
        print("- none")
    for p in wfs:
        txt = read(p)
        m = re.search(r"^on:\s*\n((?:[ \t]+.*\n)+)", txt, re.M)
        trig = re.sub(r"\s+", " ", m.group(1)).strip() if m else "?"
        print(f"- `{rel(p)}`: on: {trig}")


# ----------------------------------------------------------------------------- 15 deps
DEP_IMPORTS = {
    "scala-swing": r"scala\.swing", "jline": r"\bjline\b", "circe-optics": r"io\.circe\.optics",
    "circe-generic": r"io\.circe\.generic", "circe-core": r"io\.circe", "circe-parser": r"io\.circe\.parser",
    "pekko-actor-testkit-typed": r"pekko\.actor\.testkit", "pekko-actor-typed": r"pekko\.actor\.typed",
    "evrete": r"org\.evrete", "scalatest": r"org\.scalatest", "monocle": r"monocle\.",
}


def sec_deps():
    h("15. build.sbt dependencies vs import hits (scope: all src/**/*.scala + bin/; heuristic package map)")
    txt = read(ROOT / "build.sbt")
    arts = sorted(set(re.findall(r'%%?\s*"([^"]+)"\s*%\s*"?[\w$.{}-]*"?', txt)))
    sources = files("src/**/*.scala") + files("bin/*")
    for a in arts:
        pat = DEP_IMPORTS.get(a)
        if not pat:
            print(f"- `{a}`: (no package pattern in map — not checked)")
            continue
        n, f = grep_count(pat, sources)
        hint = "" if n else "  ← ZERO hits"
        if n and f <= 2:
            where = [rel(p) for p in sources if re.search(pat, read(p))]
            hint = f"  (only in {', '.join(f'`{w}`' for w in where)})"
        print(f"- `{a}`: {n} hit(s) in {f} file(s){hint}")
    for lib, ver in re.findall(r'"(pekko-[\w-]+|circe-core)"\s*%\s*"([^"]+)"', txt):
        pass
    m = re.search(r'pekkoVersion\s*=\s*"([^"]+)"', txt) or re.search(r'pekko[^\n]*"(\d[^"]*)"', txt)
    if m:
        print(f"- Pekko version: `{m.group(1)}`" + ("  ← milestone" if "-M" in m.group(1) else ""))


# ----------------------------------------------------------------------------- 16 docs
RETIRED = ["TypeInstance", "DomainInstance", "RuleInstance", "ActorInstance", "typeInstance",
           "loadRuleType", "loadActorType", "loadAll", "from-yaml", "to-yaml", ".rule.json", ".actor.json",
           "Extensible\\b", "PrimeOrdinal", "Egocentric", "\\bTerrain\\b", "Alpha/Bravo", "GETTING_STARTED.md\\b"]
ROOT_DOCS = ["DRACO.md", "README.md", "AGENTS.md", "CHANGELOG.md", "HOLARCHY.md", "ORION.md",
             "GETTING_STARTED_TARGET_SCALA.md", "GETTING_STARTED_TARGET_HASKELL.md", "GETTING_STARTED_TARGET_TYPESCRIPT.md",
             "src/mods/README.md", "src/main/resources/draco/drake.dlt"]


def sec_docs():
    h("16. Documentation")
    for name in ("CLAUDE.md", "AGENTS.md"):
        p = ROOT / name
        if p.is_symlink():
            print(f"- `{name}` → symlink to `{os.readlink(p)}`")
        elif p.exists():
            print(f"- `{name}` is a REGULAR FILE ({p.stat().st_size} bytes)")
        else:
            print(f"- `{name}` absent")
    print("- retired names per doc (count of lines; DRACO.md's own Retired list and CHANGELOG history are expected to mention them):")
    for d in ROOT_DOCS:
        txt = read(ROOT / d)
        if not txt:
            continue
        c = {}
        for r in RETIRED:
            n = len(re.findall(r, txt))
            if n:
                c[r.replace("\\b", "").replace("\\", "")] = n
        if c:
            print(f"  - `{d}`: {c}")
    # retired names in src (should be zero)
    n, f = grep_count(r"TypeInstance|DomainInstance|RuleInstance|ActorInstance|loadRuleType|loadActorType", files("src/**/*.scala"))
    print(f"- retired type/method names in src/**/*.scala: {n} hit(s) in {f} file(s)")
    # doc-status claims in DRACO.md
    txt = read(ROOT / "DRACO.md")
    m = re.search(r"## 5\. Documentation status(.*?)(?:\n---|\n## )", txt, re.S)
    if m:
        print("- DRACO.md §5 document-status claims (verify each against the file it describes):")
        for line in m.group(1).splitlines():
            if line.strip().startswith("- **"):
                print(f"  - {line.strip()[:140]}")
    for d in ("PrimeOrdinal.txt", "viz", "src/main/scala/org/nexonix", "src/test/scala/org/nexonix", "src/mods/scala/draco/format/yaml"):
        p = ROOT / d
        if p.exists():
            n = len(files(f"{d}/**/*")) if p.is_dir() else 1
            print(f"- dead-weight candidate present: `{d}` ({n} file(s))")
    for p in ROOT.iterdir():
        if p.name.startswith(" "):
            print(f"- filename with leading space: `{p.name}`")


# ----------------------------------------------------------------------------- 17 history
def sec_history():
    h("17. History")
    chapters = sorted(files("draco-dev-journal/draco-dev-chapter-*.md"))
    if chapters:
        last = chapters[-1]
        m = re.search(r"\*\*Session date:\*\*\s*(.+)", read(last))
        print(f"- journal: {len(chapters)} chapter files; last `{last.name}` — session date: {m.group(1).strip() if m else '?'}")
    recs = sorted(p.name for p in files("draco-git-record/git-record-*"))
    print(f"- git-records: {len(recs)}; latest `{recs[-1] if recs else 'none'}`")
    ch = read(ROOT / "CHANGELOG.md")
    blocks = re.findall(r"^## \[([^\]]+)\](?: - (\S+))?", ch, re.M)
    print(f"- CHANGELOG blocks: " + ", ".join(f"`{v}`{' ' + d if d else ''}" for v, d in blocks[:4]))
    unrel = re.search(r"^## \[Unreleased\]\n(.*?)(?=^## \[)", ch, re.S | re.M)
    if unrel:
        n = len([l for l in unrel.group(1).splitlines() if l.strip().startswith("- ")])
        print(f"- `[Unreleased]` bullet lines: {n}")
    # claimed suite counts — these are CLAIMS in prose, not measurements
    claims = []
    for p in (chapters[-2:] if chapters else []) + files("draco-git-record/git-record-*")[-6:]:
        for m in re.finditer(r"\*\*(\d{3}) tests run[^*]*\*\*|(\d{3})/(\d{3})\b|Expected:\s*(\d{3}) tests", read(p)):
            if m.group(2) and m.group(2) != m.group(3):
                continue
            v = m.group(1) or m.group(2) or m.group(4)
            kind = "expected" if m.group(4) else ("run" if m.group(1) else "n/n")
            claims.append((p.name, v, kind))
    if claims:
        print("- suite counts CLAIMED in journal/records (most recent files; verify scope before quoting):")
        seen = set()
        for f, v, k in claims:
            if (f, v) in seen:
                continue
            seen.add((f, v))
            print(f"  - `{f}`: {v} ({k})")


# ----------------------------------------------------------------------------- main
def main():
    if not (ROOT / "build.sbt").exists():
        print(f"error: {ROOT} does not look like the nexonix repo root (no build.sbt)", file=sys.stderr)
        sys.exit(2)
    print(f"# review_checks — {sh('git', 'log', '-1', '--format=%h')} — generated read-only; paste into the review's §0/§4")
    defs = all_defs()
    sec_head()
    sec_corpus()
    sec_codec(defs)
    sec_draco_json(defs)
    sec_companions()
    sec_lazy()
    sec_actors(defs)
    sec_derivations(defs)
    sec_tests()
    sec_test_writes()
    sec_exclusions()
    sec_versions()
    sec_renderers()
    sec_workflows()
    sec_deps()
    sec_docs()
    sec_history()


if __name__ == "__main__":
    main()
