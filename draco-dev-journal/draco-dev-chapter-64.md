# Draco Dev Journal — Chapter 64

**Session date:** July 15–16, 2026
**Topic:** Rule-aspect emission for `Generator.drake` (the JSON→drake emitter grows past plain types, 267/267) → the actor-aspect question — no clean corpus, Phase-2b blocks it → the `setupAction/messageAction/signalAction → start/message/signal` rename across every surface → the actor emitter increment and its "bare `types`" hack → Dev's model corrections: the domain aspect is mandatory, any type may carry all five aspects, and an actor's rule set is *all the rules of its domain* → the aerial landing (271/271) with the `.rule` suffix marked a holdover → the issue-tracking lapse named and reversed: #39–#45 filed, the convention codified in DRACO.md. Transcript.

---

**[Dev]** Let's do the next task that let's us round-trip between drake and json

**[Claude]** *(read the emitter/language/corpus memory files, recent git log, `Generator.scala`, `DrakeGenTest.scala`, sample `.drake`/JSON pairs, and drake.dlt)* I'll start by understanding the current state of the drake↔JSON round-trip work. This is the key state: `Generator.drake` (JSON→drake) exists for plain types, but there's **no drake→JSON parser** — so we can't actually round-trip yet. The hand-authored `.drake` corpus is explicitly "the future parser's test corpus."

**What exists:** `Generator.drake(td)` — the **JSON → drake** emitter (plain types), verified by `DrakeGenTest` doing exact-match against the hand-authored `.drake` corpus. The corpus is emitter-canonical after the Phase-2a sweep.

**What's missing:** the **drake → JSON** parser. It doesn't exist. `DrakeGenTest` is a one-directional projection check — it never parses `.drake` back. So today there is *no actual round-trip*; the target is `JSON → .drake → JSON = identity`. So "the next task that lets us round-trip" is the reverse arrow: a **drake → JSON parser**, closing the loop the emitter opened. The emitter gives us a perfect spec — for every plain-type JSON, `Generator.drake` produces canonical drake, and the parser must invert it exactly.

There's one genuine fork before I start building, since it determines everything downstream. *(Posed the fork as a question; Dev chose the emitter-rules increment as the next step.)* Let me build it: extend `Generator.drake` to emit the rule aspect. This is exactly the Phase-2a situation: the emitter defines canonical form, and the 3 hand-authored rule `.drake` files use the *old* bracketed style (`variables [ ... ]`) plus inconsistent head order (AddNaturalSequence is domain-first). Per the BRACKETS rule in drake.dlt, `variables`/`conditions` are **leaf blocks → no brackets** in canonical form.

*(Implemented across Generator, test, and corpus.)* Here's what I did.

## Rule-aspect emission for `Generator.drake` (JSON → drake)

The emitter previously threw on any rule aspect (`sys.error("rule/actor/codec … not yet emitted")`). It now emits the `rule` head, so all three primes rules project to drake — the emitter's coverage extends from plain types to plain types + rules.

**`Generator.scala`** (`src/mods/scala/draco/`):
- **Header:** `type ${stripAspect(td.typeName.name)}` — strips the `.rule` resource-naming suffix so the surface names the bare concept (`type AddNaturalSequence`, not `AddNaturalSequence.rule`). No-op for plain types.
- **Guard:** dropped `RuleAspect` from the not-yet-emitted `sys.error`; actor/codec still reject loudly.
- **`drakeKeyword`:** added `Variable → var`, `Condition → con`.
- **`drakeLeaf`:** `Condition` renders value-only like `mon` (its boolean expression tree, no name/type) → `con i1 * i2 == i3` via the existing `drakeExpression` `*`/`==` operators.
- **`drake()` body:** new `rule` block appended after `domain` (canonical order `type → domain → rule`). `variables`/`conditions` are **leaf blocks**, so `drakeSection` emits them **bracket-free** per the drake.dlt BRACKETS rule; the `action` body sits directly under `action`.

**`DrakeGenTest.scala`:** discovery now includes `.rule.json` (still excludes `.actor.json`); `deriveDrakePath` strips `.rule`/`.actor` so `AddNaturalSequence.rule.json → AddNaturalSequence.drake`. The 3 rules now get per-type exact-match tests.

**3 rule `.drake` files re-authored to emitter-canonical form** (adopt-emitter-output, same as the Phase-2a sweep — layout only, content git-preserved): leaf-block brackets removed (`variables [ … ]` → bare, `conditions [ … ]` → bare), and `AddNaturalSequence` flipped domain-first → **type-first** (it was the lone domain-first authoring quirk).

I can't run sbt — please run `sbt test` (or `sbt "testOnly draco.DrakeGenTest"`). **Note on "round-trip":** this extends the **JSON → drake** direction only. A genuine round-trip still needs the reverse **drake → JSON parser**, which doesn't exist yet — that remains the biggest single step toward closing the loop whenever you want to take it on. Next emitter increments after this: actor aspect, then codec.

*(Delegated action — Dev ran `sbt testOnly draco.DrakeGenTest`, unquoted in zsh, so sbt ran the full suite: **267/267, all pass** — including the 3 new rule projections in `DrakeGenTest` alongside `DracoGenTest`/`PrimesRulesTest` — then choked harmlessly on the leftover `draco.DrakeGenTest` token: `testOnly` needs quotes in zsh.)*

All **267 tests pass** — the rule-aspect emitter increment is verified. *(Updated the emitter memory to record that the rule increment landed.)* The JSON→drake emitter now covers plain types **and** rules. Remaining emitter increments: **actor**, then **codec**. And the reverse **drake→JSON parser** remains the outstanding piece for a true round-trip whenever you want to tackle it. Nothing committed — that's yours via the IDE.

---

**[Dev]** Is there an emitter task for actor aspect?

**[Claude]** Good question — let me check whether the actor aspect has any corpus to emit from, since that determines whether it's a real next task. *(Searched the whole repo for `.actor.json` and populated `actorAspect`s.)*

Short answer: **not a clean, test-gated one like rules was.** The actor emitter path exists in code (`drake()` still `sys.error`s on a populated `actorAspect`), but there's no corpus in the tested tree to drive or verify it. Here's what I found:

**In `src/main/resources/draco/` (what `DrakeGenTest` walks):**
- **Zero `.actor.json` files**, and no plain-type JSON carries a populated `actorAspect`. TypeDefinition.json / Aspects.json only *mention* `actorAspect` as a field name in their element lists — both already pass `DrakeGenTest`, which confirms their `actorAspect` is empty. So the emitter's actor guard is currently **unreachable** here.
- The only `actor` heads in draco `.drake` are **present-empty** — `ActorAspect.drake`, `Action.drake`, `BodyElement.drake` (just `actor / start / message / signal` with no content). These are the three `authoredAhead` exclusions, blocked by the **unbuilt presence model** (present-empty elides to absent in JSON, so the emitter can't reconstruct them) — a presence-model task, *not* an actor-content-emission task.

**Where populated actor JSON actually lives — `src/mods` media domains:** `aerial/terrestrial/marine/ethereal` → `Creator`, `Consumer`, `Input`, `Output` all have real `setupAction`/`messageAction`/`signalAction`. But they're **outside** the `DrakeGenTest` walk, they have **no `.drake` siblings** — so no hand-authored target to exact-match against — and they carry the **`setupAction/messageAction/signalAction` → `start/message/signal` divergence** — the "Phase 2b" rename noted in memory and in `drake.dlt` DIVERGENCES.

**So the actor increment isn't drop-in.** My recommendation: **defer actor** until you want to pull the `src/mods` media domains into the drake round-trip. When you do, the cleanest first step is the **Phase-2b `setupAction→start` field rename** (align JSON to the drake surface), then author one actor `.drake` as the first target and extend the test walk to `src/mods`. Want me to scope that Phase-2b rename as the next task, or is there a different direction you'd rather take (e.g. the codec aspect, or the drake→JSON parser)?

---

**[Dev]** Phase-2b field rename is next

**[Claude]** Right. Phase-2b is the `setupAction/messageAction/signalAction → start/message/signal` rename — aligning the JSON, `ActorAspect` Scala type, its codec, the Generator actor path, and every mods actor JSON to the drake surface. Per the coupling-isn't-a-priori and search-whole-src feedback, let me map the full surface before touching anything. *(Grepped all of src, then read and edited every surface.)* Phase-2b field rename complete across all surfaces. Please run `sbt test` to verify.

## Phase-2b: `setupAction/messageAction/signalAction → start/message/signal`

**Core type (JSON + Scala in lockstep):**
- `ActorAspect.json` — element names, factory param names, and the `isEmpty` value expression
- `ActorAspect.scala` — trait vals, codec keys (encode `"message"/"signal"/"start"` + decode `downField`), `apply` params, overrides, `isEmpty`. Element order kept **message, signal, start**, so `DracoGenTest`'s exact-match against Generator output still holds.

**Generator actor path:** `Generator.scala` — `td.actorAspect.start/.message/.signal` + comments.

**10 mods actor JSONs** (aerial/terrestrial/marine/ethereal → Creator/Consumer/Input/Output) — the `actorAspect` keys, via a key-specific sed.

**Supporting:** 2 mods scripts (`inspect-type`, `list-domain`), `ActorAspect.drake` (fixed its own pre-existing `start`/`messageAction`/`signalAction` inconsistency), `AerialActorTest` doc comments, `DRACO.md` + `README.md` field docs.

**`drake.dlt`:** annotations updated so drake token and JSON field now match, and the **actor-name DIVERGENCES row removed** — the divergence this task existed to close.

**Left as-is:** `src/generated/*.scala.generated` (scratch docs, not compiled) and `CHANGELOG.md` history (accurate for when written; a new entry belongs with your commit).

**Verification story:** the real end-to-end gate is `AerialGenTest` ("generate Consumer actor") — it loads the renamed `Consumer.json`, the `ActorAspect` decoder reads the new keys, the Generator reads the renamed fields, and the generated actor should be **byte-identical** (only aspect field *names* changed, never bodies). `DracoGenTest` gates the `ActorAspect.json → ActorAspect.scala` sync. `ActorAspect.json` stays in `DrakeGenTest`'s `authoredAhead` (present-empty heads), so it's unaffected there.

**Scope note:** this is the *rename only*. Two things I deliberately did **not** fold in (separate concepts): reordering the JSON elements to `start/message/signal`, and the `par _<name>` underscore move (the other Phase-2b divergence). Actor *emission* is now naming-aligned but still needs `.drake` targets for the mods domains + the presence model before it's a test-gated increment.

---

**[Dev]** What's next then?

**[Claude]** You're at a clean decision point. Here's the map of what's left on the drake↔JSON work, then my pick.

**Emitter (`Generator.drake`, JSON→drake) coverage:** plain types ✓ → rules ✓ → **actor** (naming now aligned, but not emitted — `drake()` still `sys.error`s on a populated `actorAspect`) → codec (not emitted). **The other half — a drake→JSON parser — still doesn't exist**, so there's no *actual* round-trip yet.

**My recommendation: the actor emitter increment — it's the direct continuation, and Phase-2b just cleared its only real blocker (naming).** Crucially, the presence-model blocker does **not** apply here: the mods media-domain actors have *populated* `start`/`message`/`signal` bodies, which emit fine — only the *present-empty* metamodel heads (ActorAspect/Action/BodyElement.drake) need the presence model. So this is genuinely doable now. Concretely it's three steps:

1. Implement the `actor` head in `Generator.drake` (start/message/signal blocks) — mirrors the `rule` block I just wrote.
2. Author `.drake` for the mods actors (the emit targets).
3. Extend `DrakeGenTest`'s walk to `src/mods/resources` so those get exact-match gated.

One wrinkle I already spotted while reading `Consumer.json`: its `domainAspect` has `elementTypeNames: ["ConsumeReport"]` but **no** `domainAspect.typeName` — and the current `drake()` skips the whole domain block when the typeName is empty, which would drop `elementTypeNames`. So this increment will surface (and fix) a small domain-emission gap.

Cheaper alternatives if you'd rather stay small: the **`par _<name>` underscore move** (wide but mechanical), or the **two queued lints** (Monadic-bare-expression, host-reserved-domain-name). And the big one whenever you want the real loop: the **drake→JSON parser**. Want me to take on the actor emitter increment?

---

**[Dev]** Do actor emitter increment

**[Claude]** *(surveyed the full mods domains tree — 47 mods JSONs, 10 actors, no `.drake` in mods yet — then read all 10 actor JSONs)* Extending the test walk to all of `src/mods` would demand authoring 47 `.drake` — too big. I'll scope this increment to **actors only**. Key findings that shape scope: **all actors** derive `Actor[JSON]` (Output: `Actor[World]`), single-`message` except **aerial/Consumer** (has all three start/message/signal); Creators/Input/Output carry a `factory` returning `ActorType` with `ActorRef` params — the current factory emission handles this fine; **wrinkle #2 (real):** Consumers/Creators have `elementTypeNames` (the rule types the session loads) but **no** `domainAspect.typeName` — the current domain block drops them; **wrinkle #3 (out-of-drake tail):** `terrestrial/Output` has a multi-line host-code `report` body → defer it, like Value/Primes were.

I scoped to the **3 aerial actors** (Consumer exercises start/message/signal; Creator+Input exercise factory params) — mirroring the rule increment's 3-file scope. Everything's in place. Please run `sbt test`. Here's the increment:

## Actor emitter increment — `Generator.drake` now emits the `actor` aspect

**Emitter (`Generator.scala`):**
- Guard narrowed to **codec only** (actor no longer rejected).
- New `actor` block after `rule`: `actor` head + `start`/`message`/`signal` action blocks (each elides when its body is empty), mirroring the `rule` block — bodies render one indent deeper via the shared `drakeElement`.
- **Domain-block fix (flagged decision):** `elementTypeNames` now emit as a bare `types [...]` block even when `domainAspect.typeName` is empty — the wrinkle I predicted. Verified no draco-tree file has this shape, so existing emission is unchanged.

**Authored 3 aerial actor `.drake`** (`src/mods/resources/domains/aerial/`): `Consumer.drake` (start + message + signal + bare `types`), `Creator.drake` (factory with `ActorRef` param + bare `types`), `Input.drake` (factory param + empty-types, the clean domain case).

**Test (`DrakeGenTest`):** new mods-actor walk over `src/mods/resources/domains`, filtered to `actorAspect`-nonempty. Actors with a `.drake` get exact-match tests (the 3 aerial); the rest are logged as **pending** (report-only). Expected: 3 exact-match + 1 pending report listing the 7 others.

## Scope calls I made (flag for your review)

1. **Wrinkle #2 — `types` without a `domain` head.** `drake.dlt` puts `types` under `domain`; I decoupled it for the no-typeName actor case rather than drop the data. This is a syntax-discovery decision — if you'd rather represent an actor's rule-types differently (or treat the missing `domainAspect.typeName` as a modeling gap to fix in the JSON instead), say so and I'll adjust. I did **not** edit `drake.dlt` grammar yet, pending your call.
2. **Wrinkle #3 — `terrestrial/Output` deferred.** Its `report` body is multi-line host code — the genuine out-of-drake tail, like Value/Primes. It stays pending.
3. **3 aerial actors, not all 10** — the other 3 domains' actors are structurally identical and a trivial follow-up sweep once you've blessed the surface above.

Once `sbt test` is green, the natural follow-ups are: sweep the remaining 6 clean actors, then decide on Output's tail — and separately, codec emission remains the last emitter aspect.

---

**[Dev]** All valid types have a domain aspect. What do you mean by domain head? Domain aspect immediately follows draco aspect in drake.

**[Claude]** Let me check the corpus before responding — I want to be precise about what these actor JSONs actually carry versus a normal member type, and whether every `.drake` really has a domain line. *(Checked; then traced whether the actors' `elementTypeNames` is consumed anywhere.)*

That's a decisive correction — and the corpus backs you completely. You're right, and my "domain head" framing was the error. **Every draco `.drake` has a `domain` line** (I checked — zero without one), and **every normal type carries `domainAspect.typeName`**: member types use the membership form (`Position`, `PositionReport`, `ConsumeReport.rule` all have `typeName: Aerial`), domains use the is-a-domain form (`Aerial` has `typeName: Aerial` + its dictionary). So you're correct: the domain aspect is mandatory and always emits immediately after the draco aspect. My gating the domain line on `typeName.isEmpty → Seq.empty`, and then inventing a "bare `types`" branch, was papering over a symptom.

**The real finding:** the mods **actors are the only types missing `domainAspect.typeName`** — they lack the domain membership every valid type has. And there's a deeper tangle I hadn't seen until I traced the usage: **their `elementTypeNames` is live and is not domain-membership at all.** `Generator.scala:1371` reads it to *"Build the actor's private Knowledge by walking its own `elementTypeNames`"* — the rules the actor's session loads. It's actor→rule wiring **overloaded onto the domain field**, and it's emitted into the generated `Consumer.scala`. So these actors' domain aspect has two problems at once: the membership `typeName` is **missing**, and `elementTypeNames` is **present but means something else** (rules-loaded, not domain members).

**My recommendation:** fix this in the model, not the emitter — (1) add the missing membership `domainAspect.typeName` (`Aerial`/`Terrestrial`/…) to the 10 actor JSONs, so the domain line emits normally (`domain domains aerial Aerial`); (2) revert my domain-block hack + the bare-`types` branch, and re-author the aerial `.drake` with the proper domain line.

That leaves one genuine question for you, since it's your domain model: the actor's **rules-loaded list**. If it stays as `domainAspect.elementTypeNames`, a membership actor emits a `domain … Aerial` line *plus* a `types [...]` list — which reads as "Aerial's members are [ConsumeReport]," semantically wrong. Options: (a) leave it and accept the odd `types` read, (b) move rules-loaded out of `domainAspect` into the actor aspect where it belongs, or (c) something else you have in mind. How do you want to handle the rules-loaded list — and shall I go ahead with adding the membership `typeName` to the actor JSONs?

---

**[Dev]** Any type definition can have all five aspects defined. the domain aspect determines a domain as itself or what domain the draco aspect is a member of. An actor is either a domain itself, or a member of domain. either way, it has the same rule set: All of the rules in the domain.

**[Claude]** That reframes it cleanly — the rule set is a property of the *domain*, not the actor, so a per-actor rule list is the wrong shape entirely. *(Read exactly how the Generator builds an actor's Knowledge today.)* That closes the loop on the model — and it exposes that the current code has it backwards. Let me play it back to confirm, then the concrete consequence.

**The model as I now understand it:**
- Any TypeDefinition may carry all five aspects.
- The **domain aspect** is what places the type: `typeName` self-matches → the type *is* a domain (is-a-domain); `typeName` differs → the draco aspect is a *member* of that domain.
- An **actor** is one such type. Whether it is a domain or a member, its rule set is **all the rules of its domain** — derived from the domain aspect, never a list the actor carries.

**Where the code contradicts this:** `actorKnowledge` (Generator.scala:1375) builds the actor's Knowledge by walking **`td.domainAspect.elementTypeNames` — the actor's own curated list** — and the `hasActorBehavior` comment even says "an actor owns a rule-domain: its elementTypeNames are the rules its session loads." That's the wrong shape you're describing. Consequently aerial `Consumer` carries `[ConsumeReport]` and `Creator` carries `[OriginateReport]` — each loading a *subset*, when both should load Aerial's *whole* rule set.

**The correction, concretely:** (1) actor JSONs get a proper domain aspect — membership `typeName` = their domain, and **drop the per-actor `elementTypeNames`**; (2) `actorKnowledge` resolves the actor's **domain** and accepts **all** that domain's rules; (3) my bare-`types` emitter branch is deleted; the domain line always emits, right after the draco aspect, as you said.

Two things this forces, and I want your call before I touch runtime code:

- **Behavior change:** every actor then loads its domain's *full* rule set (aerial `Consumer` would begin accepting `OriginateReport` too, not just `ConsumeReport`). Intended, yes?
- **How does an actor enumerate "all rules in its domain"?** The clean way is: the domain's own `elementTypeNames` lists its members *including its rules* — which `Primes` already does (`…AddNaturalSequence.rule, …`) but the mods domains **don't** (`Aerial` = `[FlightIntent, PositionReport]`, no rules). So the actor resolves its domain, reads that dictionary, filters the `.rule` entries — which requires completing the mods domain dictionaries first (the `discover` command already does exactly this). The alternative is scanning the domain package for `.rule.json` at generate time.

My recommendation: domain-dictionary-as-source-of-truth (option 1) — it matches Primes, keeps the domain aspect authoritative, and reuses `discover`. Sequence: complete the mods domain dictionaries → change `actorKnowledge` to derive from the domain → fix the actor JSONs → revert the emitter hack → regenerate + let the actor/chain tests gate it. Shall I proceed that way, and is the fuller rule-loading behavior what you want?

---

**[Dev]** Proceed  as you suggest but make careful note that the ".rule" you mentioned (in AddNaturalSequence.rule) should not exist, and is a holdover from before aspects were well-defined in their current form. We can fix that after we fix the current issue, unless it's a blocker.

**[Claude]** Understood on both counts. First, let me record the `.rule` note as you asked *(wrote `project_rule_aspect_suffix_holdover.md` to memory)*, then investigate the key unknowns before changing runtime code.

*(Investigated: `AerialGenTest` regenerates `Consumer.scala` from JSON; `loadMember` already handles `.rule`; both aerial rule objects `ConsumeReportRule`/`OriginateReportRule` exist and compile — so aerial actors can safely load the whole domain rule set. Scoped the landing to aerial, then implemented across Generator, Aerial JSON/Scala, the 3 actor JSONs, `.drake` files, and the committed actor Scala. Along the way caught that `Input.json` says `msg.value.hcursor` but the working `Input.scala` uses `msg.json.hcursor` — a pre-existing drift, so regenerating Input from its JSON would break the build; reverted the planned `AerialGenTest` extension and hand-updated only `Consumer.scala`/`Creator.scala`, which are the two that build Knowledge.)*

Aerial-scoped landing complete. Please run `sbt test`.

## What landed — the model correction, realized on aerial

**The core fix (`Generator.actorKnowledge`):** an actor's Knowledge is now built from its **domain's** rule set. It resolves the domain from the domain aspect — `isDomain` → itself; otherwise `loadType(domainAspect.typeName)` — and accepts **all** the domain's `.rule` entries. No more per-actor rule list.

**Model corrections embodied:**
- **Domain aspect is mandatory** — reverted my "bare `types` without a domain head" hack. The domain line always emits after the draco aspect.
- **Aerial actor JSONs** (`Consumer`/`Creator`/`Input`) → membership `domainAspect.typeName = Aerial`, per-actor `elementTypeNames` dropped.
- **`Aerial.json`/`Aerial.scala`** dictionary gains its two rules (`ConsumeReport.rule`, `OriginateReport.rule`) so the actors can find them.
- **`Consumer.scala`/`Creator.scala`** knowledge hand-updated to accept both rules (matches the new Generator output; `Input.scala` is a pure forwarder with no Knowledge, untouched).
- **Aerial `.drake`** re-authored: `domain domains aerial Aerial`, no bogus `types`.

**Behavior change (verified safe):** every aerial actor now loads **both** `ConsumeReport` + `OriginateReport`. Safe because the rules are fact-type-scoped — `ConsumeReport` matches `PositionReport`, `OriginateReport` matches `FlightIntent` — and each actor inserts only its own message type, so the extra rule lies dormant (the unset `"consumer"` var is never touched in the Consumer). `AerialActorTest` should stay green.

I made it a **single-run** change (hand-updated the committed `.scala`), so one `sbt test` verifies it — no gen-then-recompile dance needed. `AerialGenTest` regenerates `Consumer.scala` idempotently to the same output.

## Notes / follow-ups
- **`.rule` suffix** — recorded as a holdover to remove. It's the current rule-discriminator (I filter `elementTypeNames` on `.rule`); **not a blocker** — when it goes, that filter becomes a `ruleAspect`-presence check.
- **Pre-existing drift found** — `Input.json` says `msg.value.hcursor` but working `Input.scala` uses `msg.json.hcursor`. Regenerating Input would break it, so I left it alone and spawned a task chip to fix the JSON (+ its `.drake`) separately.
- **Remaining sweep** — terrestrial/marine/ethereal actors still have the old shape (untouched, still compile/run). Once aerial is green, the same treatment sweeps them, plus a full `discover` to complete the mods domain dictionaries.

If aerial goes green, I'll do the three-domain sweep next.

*(Delegated action — Dev ran `sbt test`: **271/271, all pass.**)*

**All 271 tests pass.** The aerial-scoped actor-rules-from-domain model is verified:

- **`DrakeGenTest`** — all 3 aerial actors exact-match (`Consumer`/`Creator`/`Input` with the proper `domain domains aerial Aerial` line), plus the pending-report for the other 7.
- **`DracoGenTest`** — `ActorAspect.json → ActorAspect.scala` still matches (the `start/message/signal` rename holds).
- **`AerialActorTest`, `AerialAssemblyTest`, all chain tests, `WorldAssemblyTest`** — green, confirming the both-rules loading is safe exactly as predicted (fact-type-scoped rules; the dormant `OriginateReport` never touches the unset `"consumer"` var).

One expected shift worth calling out: **`ExampleDomainsGenTest` moved from 30-match/18-differ → 24-match/24-differ** (still report-only, non-failing). That's the inconsistency I flagged: `actorKnowledge` changed globally, but only aerial's JSONs/dictionary were migrated — so the Generator now emits *different* (empty-knowledge) output for the terrestrial/marine/ethereal actors, because their old-shape `elementTypeNames` entries (`"OriginateReport"`, no `.rule` suffix) don't pass the `.rule` filter. Their **committed** Scala is untouched and still runs correctly (the chain tests prove it) — it's only the generate-vs-committed comparison that diverged. The sweep closes that gap.

Aerial is green and committed-ready (your IDE handles the commit). **Next: sweep terrestrial/marine/ethereal** — now a mechanical repeat of the aerial work, per domain: domain `.json`/`.scala` dictionary += its `.rule` members; actor JSONs → membership `typeName`, drop `elementTypeNames`; hand-update each actor's `knowledge` block; author the actor `.drake`. That restores `ExampleDomainsGenTest` to full match and completes the model across all four media domains. One variation to watch: terrestrial's `Output` actor with the multi-line host-code body — I'll emit its JSON/Scala but defer its `.drake` (stays pending). Want me to proceed with the sweep, or would you rather commit the aerial landing first and pick up the sweep after?

---

**[Dev]** I'm not sure why, but for too many sessions, the project work has not used github issue tracking for this github project. Wouldn't it help with AI model memory across session if we resumed using the project specific issue tracking?

**[Claude]** You're right, and it would genuinely help. Let me look at the current state of the tracker before I give you a concrete recommendation. *(Checked `gh` auth and the issue list.)*

The data backs your instinct exactly. The tracker is healthy and authed (`gh` works as `ejb816`), and it *was* used well — the closed issues include the lineage of this very work (#26 "Consolidate `*.actor` sibling types into parent's `actorAspect`", #28/#29 codec/comparison cleanup). But:

- **Newest open issue is #38 from ~May 20; the newest of any kind is June 18.** Today is July 16 — so ~1–2 months, i.e. the entire drake-language / emitter / aspects arc, produced **zero** issues.
- **No `priority-next` issue is currently open** (all got closed). So a session that tries to "pick up where we left off" from the tracker finds nothing — which is exactly why recent sessions drifted to memory-driven "what's next" (we've hit that question three times today).

So yes — resume it. Concretely why it helps cross-session memory, beyond "good hygiene":

1. **It answers "what's next" from ground truth, not recall.** `gh issue list --label priority-next` is deterministic; my file-memory prose is a summary I reconstruct. The repeated "what's next?" this session is the symptom.
2. **It's shared and provider-agnostic.** My `~/.claude/.../memory/` is local to this machine and to Claude Code. Issues are visible to you, to other tools, and to *any* future session or model.
3. **It makes open decisions trackable.** The `.rule` removal, the mods-domain `discover`, codec emission, the drake→JSON parser, the Input.json drift — these are floating in memory prose right now; as issues you can reprioritize and close them with provenance.

One nuance so we don't over-claim: **issues and file-memory are complementary, not substitutes.** Issues = *actionable work* (backlog, decisions, bugs — forward-looking). File-memory = *durable knowledge* (how the system works, your preferences, the conventions and the model corrections you taught me today — reference, not tasks). They reinforce each other: a memory note can cite an issue #, an issue can cite a memory fact. (And both are distinct from `draco-git-record/` = audit trail and the dev journal = historical narrative — no redundancy if we keep the roles clear.)

**Offer:** with your go-ahead I'll bootstrap the backlog right now by filing issues for this session's live threads — the terrestrial/marine/ethereal sweep (`priority-next`), the `.rule`/`.actor` suffix holdover, the mods domain dictionaries via `discover`, the actor `.drake` emission tail, codec emission, the drake→JSON parser, and the `Input.json` drift — so the tracker reflects reality again. Two questions before I create anything on your repo: **(1)** go ahead and file all of these (I'll link the ones that relate to existing #11/#21/#14 rather than duplicate), or a subset? **(2)** Want me to add a short note to `CLAUDE.md`/`DRACO.md` making "check `priority-next` at session start, file issues for deferred work" an explicit convention, so it doesn't lapse again?

---

**[Dev]** Yes to both questions.

**[Claude]** Both approved. *(Wrote seven issue bodies to files to avoid shell-escaping problems, created them via `gh issue create`, dismissed the superseded task chip, then edited DRACO.md and the issues-workflow memory.)* Done — issue tracking is resumed and codified.

**Filed 7 issues** (#39–#45), each with a self-contained body:

| # | Issue | Labels |
|---|---|---|
| #39 | Sweep terrestrial/marine/ethereal actors to the domain-derived rule model | `priority-next`, `generator` |
| #40 | Remove the `.rule`/`.actor` name-suffix holdover | `cleanup`, `generator` |
| #41 | Complete mods domain dictionaries via `discover` | `cleanup`, `tooling` |
| #42 | Actor `.drake` emission: Output multi-line tail + remaining actors | `generator` |
| #43 | Codec aspect emission in `Generator.drake` | `generator` |
| #44 | drake→JSON parser — close the round-trip | `next-feature` |
| #45 | aerial `Input.json` `msg.value`/`msg.json` drift | `bug` |

- #44 cross-links the Generator[L] roadmap (#11/#21) and Lean track (#14) rather than duplicating.
- **#39 is the sole `priority-next`** — the deterministic pickup pointer for the next session (it's the terrestrial/marine/ethereal sweep we were about to do).
- The `Input.json` task chip is **converted to #45** and dismissed.

**Codified the convention in `DRACO.md`** (new "Issue Tracking & Session Workflow" section): check `gh issue list --label priority-next` at session start; file a self-contained, labeled issue for deferred work; close on completion. It states the split explicitly — **issues = work to do; auto-memory = durable knowledge; git-record = audit; journal = narrative** — so the roles stay distinct and this doesn't lapse again. Since `DRACO.md` is checked in, that change rides along with your next commit. Also updated the issues-workflow memory to record the resumption and the `#39–#45` map.

Net effect for cross-session memory: a future session (any model) can now open `priority-next` → #39 and continue, without reconstructing state from prose. Want me to keep going on #39 (the sweep) now, or pause here?

## Status
The JSON→drake emitter (`Generator.drake`) now covers **plain types + rules + actors** — only codec remains — while the reverse **drake→JSON parser still doesn't exist** (#44), so there is still no true round-trip. New this chapter: the `rule` block (canonical order `type → domain → rule → actor`; `variables`/`conditions` as bracket-free leaf blocks, `con` value-only), the Phase-2b rename **`setupAction/messageAction/signalAction → start/message/signal`** across ActorAspect JSON/Scala/codec, the Generator actor path, 10 mods actor JSONs, scripts, docs, and drake.dlt (its DIVERGENCES row closed), and the `actor` block with the first 3 mods `.drake` files ever authored (aerial `Consumer`/`Creator`/`Input`). The session's centerpiece is Dev's model correction: **any type may carry all five aspects; the domain aspect is mandatory and places the type (self-match = is-a-domain, otherwise member); an actor's rule set is all the rules of its domain** — which exposed `actorKnowledge` walking a per-actor curated list overloaded onto `domainAspect.elementTypeNames`. The aerial-scoped fix landed: actors derive Knowledge from their domain's `.rule` dictionary entries, Aerial's dictionary gained its two rules, the bare-`types` emitter hack was reverted, and the fact-type-scoped rules made whole-domain loading behaviorally safe. Dev also flagged the **`.rule` suffix as a holdover** to remove (not a blocker; #40). Suite grew **267 → 271, all green** (3 rule projections + 3 aerial actor exact-matches + pending report); `ExampleDomainsGenTest` deliberately sits at 24-match/24-differ (report-only) until the three-domain sweep. Pre-existing drift found: `Input.json` `msg.value` vs working `msg.json` (#45). And after a ~2-month lapse (nothing filed since May/June), **GitHub issue tracking was resumed and codified in DRACO.md**: issues #39–#45 filed, #39 (terrestrial/marine/ethereal sweep) the sole `priority-next` pickup pointer. Queued: #39 sweep, #40 suffix removal, #41 `discover` dictionaries, #42 actor-emission tail (terrestrial Output's multi-line host-code body + 7 pending `.drake`), #43 codec emission, #44 the parser, #45 the Input drift — plus the presence model for the present-empty metamodel actor heads, the `par _<name>` underscore move, and the two lints carried from ch.61.
