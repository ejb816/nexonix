# Draco Dev Journal — Chapter 56

**Session date:** July 7, 2026
**Topic:** Retiring YAML as a definition language. With DRAKE almost ready to parse, the Dev cleared YAML — "never really used, only tested" — to drop dead surface before the `TypeDefinition` codec-derivation work. The removal took out the `draco.language.YAML` type, its host `Language` sub-domain, the `from-yaml`/`to-yaml` CLI, the `circe-yaml` dependency, the round-trip test, and 6 orphaned `.yaml` companions. Suite ends **195/195**.

---

## 1. The framing — and an honest "does it help?"

The Dev offered YAML for removal "if it makes any of the aspect issues easier," delegating the call. Investigating first turned up the decisive fact: `YAMLRoundTripTest` **does not read the checked-in `.yaml` files** — it derives YAML on the fly from each JSON (`YAML.emit(originalTd)` → parse back → compare canonical JSON). So the 6 `.yaml` companions were already **orphaned artifacts** (only the aspect family had them — 6 of ~50 types — itself a tell that YAML was never really authored in).

That made the honest answer: dropping YAML does **not** meaningfully ease the codec-derivation work. The round-trip test would survive the codec change on its own (it re-derives YAML from the new JSON), and removing the Monadic multi-line codec strings actually makes YAML *simpler* to emit. So the two were not fused. YAML was removed on its own merits — confirmed-dead surface, a dependency, and a `TypeDefinition.yaml` that would silently go stale after the codec change — as a **separate increment before** the codec work, leaving it a cleaner world to land in.

## 2. The Language-domain consequence

The one non-mechanical decision: `draco.language.YAML` was the **sole member** of the `Language` sub-domain, and `DomainBuilderTest` asserts every built domain has ≥1 member (`members.nonEmpty`). So `Language` could not survive as an empty shell without either a test carve-out (a dead, untested, member-less domain) or a fabricated member. Cleanest was to **retire `Language` entirely** — it existed only to host YAML — and reinstate it when DRAKE becomes a defined type, at which point it'll be shaped around DRAKE's actual form rather than YAML's ghost. Git preserves the old `Language` for reference ([[feedback_preserve_before_destructive_strip]]).

## 3. Scope — bounded, all soft dependencies

Every "reference" to the removed pieces turned out to be data or comments, never a hard type-dependency:
- **Deleted:** `language/YAML.{scala,json}`, `language/Language.{scala,json}`, `YAMLRoundTripTest.scala`, 6 `.yaml` companions.
- **`GeneratorCLI`:** removed `from-yaml`/`to-yaml` (import, docs, help, `runFromYaml`/`runToYaml`/`readYamlAst`, dispatch), plus the now-dead `readJsonAst` (its only caller was `runToYaml`) and the newly-unused `Json` import.
- **`build.sbt`:** dropped `circe-yaml` (used nowhere else — the surviving `draco.format.yaml.Yaml` marker doesn't touch it).
- **Data/comments:** `DomainBuilderTest` domain set, the two `scripts/*` probe sets, and comment mentions in `AerialShellTest`/`Generator`/`DracoGenTest`/`src/mods/README.md`.

**Kept deliberately:** `draco.format.yaml.Yaml` — that's YAML-as-*message-format* in the media domains, a different axis from definition-authoring, and speculative `src/mods`.

## 4. Result

Suite **195/195** (down 8 from 203: `YAMLRoundTripTest` ×1, Language's `DomainBuilderTest` ×3, Language/YAML `DracoGenTest` parse+compare ×4). JSON is now the *only* definition surface, with DRAKE the human-authoring format-in-waiting. The `TypeDefinition` codec-derivation increment (retire the Monadic escape hatch via the `*Aspect` `elisionCheck` rule — see [[project_codec_aspect]]) now lands in a YAML-free world with no stale-companion or round-trip coupling to reason about.
