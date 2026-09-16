# DRACO — authoritative source

Edward established (2026-08-15): **https://github.com/ejb816/nexonix** is the
authoritative source for all DRACO design and development information.

Any session in this project should clone/read that repo before answering
design questions, rather than relying on conversation memory or prior notes here.

## Repo landmarks (as of commit 0af56fd, 2026-08-27; verified by review-2026-08-28)

| Path | What it holds |
|---|---|
| `DRACO.md` (`CLAUDE.md` → symlink) | Operating rules, the three gates and what a failure means, orientation (verified against the tree 2026-08-15), document status, gotchas. The file every Claude Code session auto-loads |
| `README.md` | Architecture in draco's own vocabulary (type, derivation, element, aspect, projection), a Language-specific residues table, working features, project structure. Rewritten 2026-08-15 |
| `AGENTS.md` | **Not a symlink** — a regular file frozen at 2026-06-23, verbatim the pre-rewrite DRACO.md. Treat as stale until Dev resolves it (decisions.md Q-05) |
| `CHANGELOG.md` | Current through HEAD; one entry per commit's git-record since 2026-08-17 |
| `GETTING_STARTED_TARGET_SCALA.md` / `_HASKELL.md` / `_TYPESCRIPT.md` | One guide per target, nine identical sections; Scala realized, the other two are structure plus each target's open questions |
| `HOLARCHY.md` | Eight-level holarchy of perspectives (conceptual, not implemented; names drifted — `Egocentric`→`Sentient`, `Terrain`→`Terrestrial`) |
| `ORION.md` | DJINN / ORION acronym vocabulary; six names exist as empty traits under `src/main/scala/draco/dreams/orion/` |
| `src/main/resources/draco/drake.dlt` | The DRAKE surface specification. Docs call it current; the reviews list five stale items (ledger D-11) |
| `src/main/resources/draco/*.json` + `.drake` | The draco corpus: 81 definitions, each a JSON/drake pair, projected to `src/main/scala/draco/**` |
| `src/mods/resources/`, `src/mods/scala/` | The engine tier in practice (`Generator`, `GeneratorCLI`, `Drake`, `DrakeCLI`, `Expression`, `SourceContract`, `DomainBuilder`, `Assembly*`) and the example media domains (49 definitions, 10 with drake) |
| `src/test/resources/scenario/` | 22 drake-only definitions (Forest/Ash/Birch) — #63's acceptance corpus; no JSON, no Scala |
| `draco-dev-journal/` | Chapters 00–74+, near-verbatim session transcripts; written by Cowork, not by the coding session |
| `draco-git-record/` | One file per commit, containing that commit's message and reasoning; release notes per version |
| `draco-reviews/` | This folder: the reviews and their continuity files (see `README.md` here) |
| `bin/` | `draco-gen` (generate / generate-multi / compile / compile-multi / inspect / discover / verify) and `draco-sc` scripts. No `draco-drake` wrapper; `DrakeCLI` is reachable through the assembled jar only |
| `src/`, `build.sbt`, `project/` | Scala 2.13 / sbt implementation; version in `build.sbt` |
| `.github/workflows/release.yml` | The only CI; runs on `v*` tags |
| `viz/` | Hand-authored charts from one run (dead weight per the reviews) |
| GitHub Issues | Canonical backlog; `priority-next` label marks the next pickup. Not reachable from a Cowork session — reconstruct from records/CHANGELOG/journal |

## Working rules from DRACO.md

- Edward handles all compiles, commits, and pushes via his IDE — do not run
  `sbt`, `git commit`, or `git push`.
- A new GitHub issue requires Dev's explicit approval, every time; commenting on an existing issue does not.
- Model-authored prose (issues, memory, review documents) is prior reasoning to re-examine, never Dev's authority.
- JSON definition files are the single source of truth; the Generator emits Scala. Definitions move as a trio (JSON + `.drake` + `.scala`).
- Rule-/actor-ness is carried by the aspect, never by a name suffix.
- Every `val` in an `extends App` companion must be `lazy val`.
- A CHANGELOG entry is written with each commit's git-record, in the same commit.
