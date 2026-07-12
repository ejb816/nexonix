# Draco Dev Journal — Chapter 56

**Session date:** July 7, 2026 (continues ch.55)
**Topic:** Retiring YAML as a definition language. With drake almost ready to parse, the Dev cleared YAML — never really used, only tested — and the removal took out the `draco.language.YAML` type, its host `Language` sub-domain, the `from-yaml`/`to-yaml` CLI, the `circe-yaml` dependency, the round-trip test, and 6 orphaned `.yaml` companions. Suite ends **195/195**. Transcript.

---

**[Dev]** Now that drake language is almost ready to parse, YAML as definition language can be dropped, since it was never really used, only tested. YAML doesn't have to be dropped immediately, but is available to be dropped if it makes any of the aspect issues easier. Regardless of what you can do with YAML, I accept your recommendation.

**[Claude]** Let me investigate the actual YAML surface before recommending — how coupled it is to the work I'm about to do on `TypeDefinition.json`.

Telling: only **6** `.yaml` files exist — exactly the aspect family — versus ~50 types with JSON. That's concrete evidence of "only tested, never really used." Key finding on coupling: `YAMLRoundTripTest` **does not read the 6 checked-in `.yaml` files at all** — it derives YAML on the fly from each JSON (`YAML.emit(originalTd)`) and round-trips that. So those 6 `.yaml` companions are already **orphaned dead artifacts**, and the test's "66/66" comes from exercising every JSON, not the companions.

That clarifies the relationship to the aspect work:
- YAML is **not blocking** the codec-derivation work. The test re-derives YAML from JSON, so it'd survive the `TypeDefinition.json` change on its own — and removing the multi-line Monadic codec strings actually makes YAML *simpler* to emit, not harder.
- What YAML *does* add is coupled surface that goes stale: after the codec change, `TypeDefinition.yaml` would silently misrepresent `TypeDefinition.json`.

**My recommendation — honest read first:** dropping YAML does *not* meaningfully ease the codec-derivation work, so I won't fuse the two. **But** YAML-as-definition-language is confirmed dead and worth removing on its own merits, and doing it *now, as a separate increment before the codec work*, cleanly retires the `.yaml` artifacts, drops the `circe-yaml` dependency (imported only by `YAML.scala`, `GeneratorCLI`, and the test — the separate `format.yaml.Yaml` message-format marker doesn't use it), and lets the codec change land in a simpler world. DRAKE supersedes YAML's role as the human-authoring format.

One planned scoping call reversed itself under checking: I'd intended to keep the `draco.language.Language` sub-domain as an empty shell (the natural future home for DRAKE) — but `DomainBuilderTest` asserts every built domain has ≥1 member, and **YAML is Language's only member**. Since `Language` currently exists *solely* to host YAML, the coherent endpoint is to **retire `Language` entirely** and reintroduce it when DRAKE becomes a defined type — cleaner than leaving dead, untested, member-less scaffolding. A final blast-radius check confirmed all "Language" references are comments or hardcoded string-data probe sets — no hard type-dependencies. Full removal is bounded (~15 files, mostly mechanical). Executed:

**Deleted (8 files + empty dirs)** — `draco/language/YAML.{scala,json}`, `draco/language/Language.{scala,json}`, `YAMLRoundTripTest.scala`, and the 6 orphaned `.yaml` companions (`RuleAspect`, `Aspects`, `DomainAspect`, `TypeDefinition`, `DracoAspect`, `ActorAspect`).

**Edited** —
- `GeneratorCLI.scala`: removed `from-yaml`/`to-yaml` (import, docs, help, `runFromYaml`/`runToYaml`/`readYamlAst`, dispatch cases), plus the now-dead `readJsonAst` (its only caller was `runToYaml`) and the newly-unused `Json` import.
- `build.sbt`: dropped the `circe-yaml` dependency.
- `DomainBuilderTest.scala`: dropped the `Language` domain from the built set; `list-domains.scala` / `who-extends.scala` probe sets likewise.
- Comment cleanups: `AerialShellTest`, `Generator`, `DracoGenTest`, `src/mods/README.md`.

**Kept deliberately:** `draco.format.yaml.Yaml` — YAML-as-*message-format* in the media domains, a different axis from definition-authoring.

Since `build.sbt` changed, this needs an sbt `reload` before testing. Expect the count to drop (no `YAMLRoundTripTest`; `DomainBuilderTest` now covers 3 domains, not 4).

*(Delegated action — Dev ran `reload` + `test` from the IDE sbt shell: **195/195**. The 8-test drop is exactly accounted for: `YAMLRoundTripTest` ×1, Language's `DomainBuilderTest` cases ×3, Language/YAML `DracoGenTest` parse+compare entries ×4. Wrote journal chapter 56; reconciled the now-stale memories — `project_yaml_authoring_workflow` and `project_draco_language_status` collapsed to retirement records, `reference_draco_gen_cli` stripped of `from-yaml`/`to-yaml`, MEMORY.md index updated.)*

**YAML-as-definition-language is retired at 195/195.** JSON is now the **sole** definition surface; DRAKE is the human-authoring format-in-waiting, and the `Language` sub-domain returns when DRAKE becomes a defined type. The still-open thread — scoped two turns ago — is the **codec-derivation** increment: add the `*Aspect` case to `elisionCheck`, list the aspects in `TypeDefinition.json`'s `elements`, and delete the Monadic encoder/decoder escape hatch so `TypeDefinition`'s own codec derives like every other type's. It now lands in a clean, YAML-free world. Want me to take that on next?

## Status
Suite **195/195** (down 8 from 203, all accounted for). JSON is the only definition surface; `Language` retired with YAML, to be reinstated for DRAKE. `circe-yaml` gone from `build.sbt`. Queued: the `TypeDefinition` codec-derivation increment (retire the Monadic escape hatch via the `*Aspect` `elisionCheck` rule — see [[project_codec_aspect]]).
