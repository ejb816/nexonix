# Draco Dev Journal — Chapter 48

**Session date:** June 19–22, 2026
**Topic:** Two arcs. First, **Slice B** — the World transformation service as a running actor graph (the first cross-medium transform end-to-end), and a sharpening of the *define-before-hand-write* discipline along the way. Then a pivot to dev-ergonomics: the **test-output logging migration** that moves bulky `println` off the IntelliJ console into per-suite files, rolled out incrementally suite-by-suite.

---

## Slice B — the transform as actors

With the semantic core proven in chapter 46 (a typed `Position` crossing to a `Location` through the `Observable`, meaning preserved as a passing assertion), Slice B wired it into Pekko actors: an Aerial **input adapter** (loose `PositionReport` Json → typed `Position`), **`World.Consumer`** (the transform interior: `Position → Observable → Location`, the geodesy concentrated here because "all transform rules live in World"), **`World.Provider`** (routing), and a Terrestrial **output adapter** (typed `Location` → loose `LocationReport` Json → the medium's `Consumer`). `WorldTransformServiceTest` runs a `PositionReport(51.5°, −0.12°, 35000 ft)` through the whole graph and recovers the world point at the Terrestrial sink (lat/lon < 1e-6, 35000 ft → 10668 m).

## "Make the definitions as soon as possible"

The keystone exchange of the session. The adapters first shipped with `TypeDefinition.Null`, rationalized as "glue like the Sinks."

> **Dev:** I think what flagged a problem for me is creating actors and domains without definitions. Whenever that happens, I would expect that there's a reliable effort to make the definitions as soon as possible. What do you think?

Right — and sharper than the code reflected. The honest bar is **defined** (JSON exists → in the type system, portable, in the Generator-fold queue), which is a separate, cheaper milestone than **generated**. A Sink is passive observation with no domain identity; an adapter is the **codec boundary** — a named architectural role. Calling it glue disguised debt as design. Fix: `Input.json`/`Output.json` written, the `Null`s retired, and `World.elementTypeNames` reverted (actors are *defined* via their own JSON but, per the media `Creator`/`Consumer` convention, are **not** listed as a domain's message-type members). Recorded as `feedback_define_before_handwrite`.

## println → logger

> **Dev:** What about eliminating the use of println and just use logger? Loss/gain?

The better answer than the `System.out`-hijacking trait first proposed: a logger routes by **appender**, not by hijacking stdout — file-only without forcing serial tests. The three-category rule fell out of the grep: **CLI/tool stdout** (`GeneratorCLI`, `scripts/*`, `CLI`, `RETEExample`) *is the product* — keep `println`; **shipped-code diagnostics** (`EgoActor`, `NaturalActor`) → class logger at `debug`; **test demonstration output** → a dedicated logger.

Mechanism: `draco.PersistentTestLog` (mix-in) exposes `log` (SLF4J `test.output`, file-only) and `console` (`test.report`, clean one-liner); `logback-test.xml` routes `test.output` through a `SiftingAppender` keyed on a `suite` MDC value to `target/test-output/<suite>.log`, truncated each run, never on the console. ScalaTest's per-test PASS/FAIL still shows (it arrives via sbt's logger, not stdout), plus one `→ <suite> output: …` pointer per suite.

## Rolled out incrementally

> **Dev:** Ok, let's continue this incremental approach.

Suite by suite at the Dev's pace: Primes → the `*GenTest`s → the rest of `draco/*` → the six `org.nexonix.*` → `YAMLRoundTripTest` + the non-suite actors. Two gotchas surfaced through the test gate (not before it): `log.info` wants a `String` where `println` took `Any` (7 sites wrapped in `s"…"`), and a bulk sweep targeting `println` misses bare `print(` — one in `TestValue` slipped three passes and was caught only by reading the console after a green run. Both are recorded in `project_test_output_logging`. The lesson from earlier in the session held throughout: **scoped-green ≠ suite-green** — every console surprise was a full-suite find.

## The YAML wink

> **Dev:** Proceed and surprise me with the YAML thing, see if I notice what you chose.

`YAMLRoundTripTest` keeps only its one-line headline (`YAML ROUND-TRIP REPORT: 63 / 63 passed, 0 YAML failures`) on the console via `console`; the banners, per-status breakdown, and DETAILS go to `log` (the file) — and the headline is echoed into the file too, so the persisted report is self-contained rather than decapitated.

## Where this leaves things

Full suite **184/184 green, console clean** end to end — only framework-level lines remain (SLF4J init replay; Evrete's `scala.Tuple3` JUL warning). This is a clean push point for the whole session: Egocentric→Sentient, the `*centric` deletion, the `Format` parameterized-self-domain Generator fix, Slice B, and the test-output logging migration. The TransformBuilder precursors that remain unchanged: the World transform expressed as **JSON-backed rules** (#2) and the **Generator actor-emission fold** (#3).
