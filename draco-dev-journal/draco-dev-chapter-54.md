# Draco Dev Journal — Chapter 54

**Session date:** July 4, 2026
**Topic:** DRAKE's four aspect-heads get their grammar settled (mandatory `type`+`domain` prefix, optional `rule`/`actor`, canonical order `type→domain→rule→actor`), followed by a philosophical excursion that lands a real ontology — the `actor`/`rule` capabilities as **membrane/production**, then pried apart into two orthogonal axes (**liminal/axial** spatial, **latent/actual** = definition/execution). That ontology then drives two concrete cleanups: **Issue A** removes `Actor[T]`'s vestigial factory (exposing three Generator import gaps), and **Issue B** retires the redundant `domainDefinition`/`ruleDefinition`/`actorDefinition` alias triad. Every step test-gated; the suite ends **200/200**.

---

## 1. "Nothing is stray junk" — drake is syntax discovery

The session opened with me reviewing the uncommitted `.drake` edits and mislabelling the in-flight aspect-heads as "stray junk." The Dev's correction was foundational:

> **Dev:** Nothing is "stray junk" in any files, particularly the .drake files that don't even have a parser yet. … I am working with the drake files directly to discover the syntax I need … your criticism was both useless and non-productive.

Recorded as [[feedback_drake_is_syntax_discovery]]: hand-authored `.drake` **is** the design act (there's no parser to satisfy); never call in-flight drake junk, and never judge which aspects a type "should" have. The productive reframe the Dev had found: a single type definition can carry **all four aspects at once** (`type` + `domain` + `rule` + `actor`) — exercising co-presence the `Aspects`/`TypeDefinition` model always allowed but no prior drake did.

## 2. The aspect-head grammar, settled in four moves

Across several tight exchanges the surface grammar firmed up:

1. **Co-presence is permitted, not required.** Only `type`+`domain` are mandatory; `rule`/`actor` appear only when populated (empty heads elide, like `from` at root).
2. **`domain` is not a head** — it's bound to `type`, the mandatory line *immediately after* it. The real heads are `type` (carrying `domain`) and the optional `rule`/`actor`.
3. **`domain` has two forms**, discriminated by name self-match (the `DomainAspect.typeName` self-loop): *membership* (`type Action` / `domain draco Draco`) vs *is-a-domain* (`type Draco` / `domain draco Draco  types [ … ]`).
4. **Canonical order `type → domain → rule → actor`** (settled for parsing). The Dev proposed it; I agreed on two independent grounds — mandatory-prefix-before-optional is parser-friendly, and it mirrors the normative `Aspects` field order (one sequence across surface/JSON/Scala). Weighed `actor`-before-`rule` (runtime outer-membrane→inner-session dataflow) and rejected it: definitional order wins.

## 3. The ontology excursion — membrane/production, then two orthogonal axes

The Dev asked me to describe the use-cases of the four combinations "at the draco core level." I framed the two optional heads as orthogonal capabilities plugging into two runtime fabrics: `rule` → RETE reactivity (pattern→action), `actor` → Pekko agency (start/message/signal). The 2×2: **Value** / **Production** / **Membrane** / **Cell**, with the bottom-right hiding a coupling choice (compose separate rule+actor, or fuse into an autonomous cell).

Then the Dev pushed on the metaphysics:

> **Dev:** Given the conceptual poles of liminal/axial and latent/actual, are you adding membrane/production as a member of that equivalence class?

I answered honestly: membrane maps cleanly to liminal/latent, but production→axial was the contestable leg (axial arguably names the type-*core* spine, not the firing rule). The Dev then dissolved my mapping:

> **Dev:** The rule as data is pattern + action which is a typed (structured) representation of a non-actualized process

That is the key correction. If the rule-as-data is a *non-actualized process*, it is **latent**, not actual — its actualization is the RETE firing at runtime, which the definition doesn't contain. So the rule sits at **(axial, latent)** simultaneously — which is only possible if **liminal/axial and latent/actual are two orthogonal axes, not one collapsed equivalence class**. The rule is the witness that pries them apart. The payoff: **latent/actual turns out to be draco's own definition-plane / execution-plane split** — drake/JSON/aspects are the entire latent column; Evrete-firing + Pekko-delivery is the actual one. Every aspect reads uniformly as "a typed representation of a non-actualized X" (rule = non-actualized process, actor = non-actualized boundary, value = non-actualized structure).

This wasn't decoration — it became the decision rule for Issue A/B: **everything drake authors is latent by construction; the redundant `*Definition` fields and the actor factory were latent-plane clutter with no actualization role.**

## 4. Issue A — remove `Actor[T]`'s vestigial factory

Investigating `Actor.json`/`.scala`/`.drake`, the finding: `Actor.apply`/`Null` are **dead** (zero references), and concrete actors bypass them entirely (`new Actor[Msg]{…}` emitted per-actor by the Generator's `actorType()` path). The type parameter is a red herring for factory-necessity — `Type[T]`/`Domain[T]`/`Rule[T]` are all parameterized *with* live factories; `Primal[T]` parameterized with none. What decides it: a *uniform* construction someone calls. An actor's essence is its overridden `receive`/`receiveSignal`, which no fixed factory can supply — so `Actor[T]` is an abstract container, extended not instantiated, and carries no factory.

Removed the factory from `Actor.json`/`.scala`/`.drake`, and — checking the Dev's separate point 1b — moved `ExtensibleBehavior[T]` into the `extensible` field (`derivation=[ActorType]`) so the field does its designed job (head-of-`extends` for an external type) and `Actor.json` faithfully compiles from `Actor.drake`.

`DracoGenTest` then exposed **three Generator import gaps** that removing the factory surfaced (all real, general fixes):
1. `externalImports` scanned `derivation` but not the `extensible` field → the moved `ExtensibleBehavior` lost its import. Fixed by also scanning `extensible.name`.
2. The `isLeaf||isActor` branch forced `pekkoImports` for any actor type; a factory-less container needs none (those imports serve factory-emitted `receive`/`receiveSignal`). Gated on `factory.valueType.nonEmpty`.
3. The multi-type merge (`GenerateAndCompileTest` "Draco core group") concatenates `derivation` but not the scalar `extensible` → the group lost the import and failed to compile. Fixed by folding constituents' `extensible` into the synthetic `mergedTd.derivation`.

Suite **200/200**. Two drake conventions recorded alongside: **`extensible` = external head-of-extends vs `derivation` = internal mixins**, and **quoted literal ⇒ elidable `String` type**.

## 5. Issue B — retire the `*Definition` alias triad

`domainDefinition`, `ruleDefinition`, `actorDefinition` were each just an alias of `DracoType.typeDefinition` (holdovers from the dissolved `*Definition` types). Mapping showed the two issues have different shapes — the alias is a **symmetric triad** (do it together as one decision), while the vestigial-factory issue was Actor-only. Executed cleanest→messiest, **domain → rule → actor**, one aspect per test gate.

The recurring lesson: **the "redundant" alias was never fully dead — each had exactly one live reader** that had to be repointed to `typeDefinition`, not just deleted:
- **Domain:** `DomainBuilder.validate`/`generate` read `domain.domainDefinition`. (5 files; green first try.)
- **Rule:** the trap. `ruleGlobal` emitted `Rule[X](typeDefinition, _pattern, _action)` — a **positional** first arg feeding the old `_ruleDefinition` param. Removing the param broke all 11 generated rule companions *plus* a hand-written test rule (`org.nexonix…TupleFactRule`) my first sweep missed because I scoped it to `src/main`+`src/mods` and skipped `src/test` (→ [[feedback_search_main_project]]).
- **Actor:** `ActorType` had *only* `actorDefinition`, so it collapsed to a bare marker `trait ActorType extends DracoType`; plus 14 concrete override sites + the Generator:995 emission.

Minimal pattern throughout: keep the factory *param* names (internal plumbing now feeding only `typeDefinition`), remove only the field element + its overrides. Suite **200/200** after each aspect and at the end.

## 6. Dead trait-companion factory cleanup

The secondary thread, done in the same session. `RuleType` turned out to be the *only* abstract `*Type` trait carrying a factory (`DomainType`/`ActorType`/`DracoType` have none). Its `apply`/`Null` were confirmed uncalled — rules construct via the `Rule[T]` container. Removing the factory from `RuleType.json` (regenerating `RuleType.scala` to a pure trait + minimal companion) had **zero ripple**: suite stayed **200/200** on the first run. The clean removal is itself the proof the factory was truly dead — the exact contrast with the `*Definition` aliases, each of which hid a single live reader that broke compilation until repointed. `RuleType` is now symmetric with `DomainType`/`ActorType`: an abstract trait, no factory.
