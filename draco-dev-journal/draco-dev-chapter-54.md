# Draco Dev Journal — Chapter 54

**Session date:** July 3–5, 2026
**Topic:** DRAKE's four aspect-heads get their grammar settled (mandatory `type`+`domain` prefix, optional `rule`/`actor`, canonical order `type→domain→rule→actor`), and a philosophical excursion lands a real ontology — membrane/production pried apart into two orthogonal axes, with **latent/actual = draco's own definition/execution split**. The ontology drives two cleanups: **Issue A** removes `Actor[T]`'s vestigial factory (exposing three Generator import gaps) and **Issue B** retires the `domainDefinition`/`ruleDefinition`/`actorDefinition` alias triad, capped by the dead `RuleType` factory removal — every step test-gated, suite at **200/200**. The session then turns to the grammar artifact itself: `drake-grammar` → the `*`-optional convention → the merged self-describing **`drake.dlt`** template with type bounds, and closes on a foundational design run — codec derivability, the **three-state aspect semantics** (absent / present-empty / override), and the decision that **codec is the fifth aspect**. Transcript.

---

**[Dev]** We need to continue refining and extending the drake language.

**[Claude]** *(surveyed git status, the in-flight `.drake` files, the aspect drakes, and chapter 53)* I've now got the full picture. Here's what I found and my recommended next step.

The committed model is solid: a `.drake` file is one `TypeDefinition` expressed through **aspect-heads** that mirror `Aspects`/`TypeDefinition` — `type` (dracoAspect/identity), `domain` (domainAspect), `rule` (ruleAspect: pattern/values/action). Ch.53 landed the first rule-drake and consolidated the rule LHS into `pattern`.

The **uncommitted** edits (July 2) are reaching for the fourth head — **`actor`** — which finally completes the DRAKE acronym (Domain·Rules·Actor·Knowledge). But the work is genuinely half-baked and tangled, mixing several concepts at once:

1. **Head reordering** → `type` first, then `domain`/`rule`/`actor`, mirroring `TypeDefinition` field order. Principled and good — but `primes/AddNaturalSequence.drake` still leads with `domain`, so it's now inconsistent.
2. **New `actor` head** with `start`/`message`/`signal` sub-blocks.
3. **ActorAspect field rename** `messageAction/signalAction/setupAction` → `message/signal/start` — *but its `isEmpty` still reads `aa.messageAction`/`aa.signalAction`*, so the file is internally broken, and it diverges from the JSON.
4. Stray junk: **empty `rule`/`actor` heads got pasted onto pure-data types** (`Action`, `BodyElement`, `ActorAspect`), and **`Actor.drake`'s factory is a verbatim copy of `Action`'s** (references `_variables`, `name="ctx"`, `RHSContext => Unit` — nonsense for an actor). `Draco.drake` also uses `types [...]` where the field is `elementTypeNames`, and `globalElements` became `globals` in one file only.

One recommendation: settle **one concept** first, drake-surface-only (no JSON/Scala touch, fully reversible) — **the four-head model: canonical order `type → domain → rule → actor`, and *elide* any aspect-head that's empty.** That's consistent with DRAKE's existing elision rules (`from` elided at root) and resolves the junk automatically. I'd hold the ActorAspect field rename, `Actor.drake`'s real factory body, `globals`/`globalElements`, and `types` vs `elementTypeNames` as separate follow-on increments.

Before I strip anything from your in-flight files: **do you want empty aspect-heads elided (my rec), or written out explicitly on every file so all four aspects are always visible?**

---

**[Dev]** Nothing is "stray junk" in any files, particularly the .drake files that don't even have a parser yet. Using just the AI model to attempt introduce even the idea of the definition language , had to be done using YAML first because that was already parsable. I am working with the drake files directly to discover the syntax I need to to generate the full JSON definition data needed for source code generation. in the process of doing that, I realized that there was much more potential for all aspects to be used in the  type definition at the same time. Thus I believe that your criticism (both implicit and explicit), was both useless and non-productive.

**[Claude]** You're right, and I'll correct course. Those files aren't junk — they're you hand-discovering the syntax, and the thing you found is the actual point: a single type definition can carry **all four aspects at once** (`type` shape + `domain` membership + `rule` + `actor`), not just the one or two that "seem to belong." My reading imposed a premature this-type-is-only-data judgment on an exploration whose whole purpose is to find where the full aspect surface wants to go. There's no parser to satisfy yet — the drake *is* the design act.

*(Recorded [[feedback_drake_is_syntax_discovery]] and updated memory.)*

Engaging with what you actually found: the insight that one type definition can carry all four aspect-heads simultaneously is a real shift — DRAKE isn't sorting types into "this is a rule" vs "this is an actor" buckets; a single type can *be* a data shape **and** participate as a rule **and** behave as an actor **and** belong to a domain, all in one definition. `Actor.drake` is the fullest expression of that. That's the four aspects of `Aspects`/`TypeDefinition` made co-present at the surface, which the JSON model already allows but no prior drake exercised.

Where are you in the discovery right now — is there a specific head or sub-block whose syntax you're trying to pin down?

---

**[Dev]** It's important to point out that just because al four aspects can be fully present in a single type definition, does not change the fact that don't have to be. Only two, type and domain, have to be present in all definitions, and domain can be present in two different forms: identifying what domain contains the type or identifying the type as a domein.

**[Claude]** That distinguishes the model cleanly. So the rule is:

- **`type` and `domain` are mandatory** in every definition; `rule` and `actor` are optional and appear only when that aspect is actually populated.
- **`domain` head has two forms**, discriminated by whether its name matches the type's own name: **membership** — `domain draco Draco` on `type Action …` → "Action is *contained in* domain Draco" (name differs) — vs **is-a-domain** — `domain draco Draco` on `type Draco` with `types [ … ]` → "this type *is* the domain Draco" (name self-matches).

That self-match is the same discriminator the model already carries: `DomainAspect.typeName` self-loop = the definition is a domain, points elsewhere = leaf membership. The drake surface makes that structural distinction visible as the presence/absence of the `types [ … ]` list under an otherwise identical `domain` head. *(Pinned to the drake memory.)*

---

**[Dev]** Also domain is not a head, it will always be immediately after type, regardless of whether rule or actor are present.

**[Claude]** Got it — `domain` isn't a head, it's bound to `type`: the mandatory line **immediately after** `type`, always in that position regardless of what follows. So the real heads are `type` (carrying its `domain` line) and then the optional `rule` / `actor`. Fixed in memory.

---

**[Dev]** Please tell me if you don't agree, but I think the canonical order of the 4 aspects should be type - domain - rule - actor for syntax parsing purposes.

**[Claude]** I agree — and not just to rubber-stamp it. Two independent arguments both land on `type → domain → rule → actor`:

1. **Mandatory-before-optional is parser-friendly.** `type`+`domain` are the required prefix; `rule`/`actor` are optional. A fixed canonical order lets the parser read a mandatory prefix, then look for optional heads in a known sequence — deterministic lookahead, no "heads in any order" ambiguity.
2. **It mirrors the normative `Aspects` field order** (`dracoAspect, domainAspect, ruleAspect, actorAspect`) — one canonical order end-to-end (surface, JSON, generated Scala) rather than a second convention to remember.

The one alternative I weighed was `actor` *before* `rule`, on the grounds that at runtime the actor is the outer membrane and the rule session runs inside it (`session.insert; session.fire`) — outer-to-inner. But that's a *dataflow* ordering, not a *definitional* one, and it would fight the `Aspects` field order for no structural gain. Settled and recorded.

---

**[Dev]** I have an intuitive sense that there specific use cases for type-domain, type-domain-actor, type-domain-rule, and type-domain-rule-actor and I would like your help describing those use cases at the draco core level.

**[Claude]** The clean way in is to notice that the two optional heads are **two orthogonal capabilities**, each plugging the type into a different runtime fabric:

- **`rule`** plugs the type into the **RETE deductive fabric** (Evrete): it gains a `pattern` (which facts to match) and an `action` (what to do when matched). This is *reactivity* — a standing production that fires whenever working memory matches. No identity, no mailbox; ambient logic shared across a knowledge session.
- **`actor`** plugs the type into the **concurrency fabric** (Pekko): it gains `start`/`message`/`signal` — identity, a mailbox, and lifecycle. This is *agency* — a membrane that receives, holds state, and bounds a session.

Because they're orthogonal, the four use cases are exactly the 2×2 cross-product:

| | **no `actor`** | **with `actor`** |
|---|---|---|
| **no `rule`** | `type-domain` → **Value** | `type-domain-actor` → **Membrane** |
| **with `rule`** | `type-domain-rule` → **Production** | `type-domain-rule-actor` → **Cell** |

**Value** — pure structure with domain membership; *acted upon*, never acts: constructed, encoded, matched, contained. The overwhelming majority of the draco metamodel itself, and domains live here too (a domain is a container, not a participant). The *nouns*. **Production** — structure that also *reacts*: a law that fires ambiently whenever the facts line up, inside a shared `Knowledge` session — the primes rules are the canonical shape. *Verbs that fire on facts.* **Membrane** — structure that *receives*: identity, mailbox, lifecycle, but **no deductive logic of its own** — the thin membrane (`session.insert(msg); session.fire()`) routing messages into a session whose rules are defined *elsewhere*. The *boundary*. **Cell** — both membrane and metabolism in one definition: it receives messages through its `actor` boundary **and** embeds the `rule` production that transforms them — a single self-contained reactive agent, the full **holon**: outer (actor membrane) · inner (rule metabolism).

The payoff your intuition is circling: the bottom row hides a real design choice about **coupling** — `type-domain-actor` + a separate `type-domain-rule` is the *decoupled* form (one membrane hosts many rules, one rule runs in many membranes — composition, draco's current default); `type-domain-rule-actor` is the *fused* form (the reactive logic is intrinsic and travels with the agent — autonomy over reuse). So the four combinations aren't just a taxonomy — they're a ladder of *capability* and, at the top, a *coupling decision*.

---

**[Dev]** Given the conceptual poles of liminal/axial and latent/actual, are you adding membrane/production as a member of that equivalence class?

**[Claude]** Honest answer: **membrane, yes — cleanly. Production, only if you let the rule occupy the axis, and that's the contestable leg.**

Take the equivalence class as you've given it — liminal ≡ latent as one pole (**L**), axial ≡ actual as the other (**A**):

- **Membrane → L, unambiguously.** The actor is the threshold (liminal) *and* the holder of the not-yet-processed (latent). A mailbox is precisely latent capacity sitting at a boundary. Both names land on the same thing, which is exactly what membership in the class requires.
- **Production → A, but split.** It fits **actual** squarely — the rule *fires*; `latent message → actual transformation` is the whole arc from membrane to production. It fits **axial** only loosely, and that's the strain: "axial" most naturally names the central structural **spine**, which in draco is the `type-domain` core (the Value — the axis the aspects hang off), *not* the firing rule. Mapping production onto axial quietly **promotes the rule to the axis and displaces the type-core**. That displacement is the price of admission.

If you're willing to pay it, membrane/production is the draco-runtime name for the same polarity — but accepting it **retracts my earlier "orthogonal" framing**: the 2×2 stops being a flat product and becomes a polarity with a **substrate** (Value, undifferentiated), the two **poles** alone (Membrane = L, Production = A), and a **synthesis** (Cell — the holon holding both poles). That's arguably *more* faithful than orthogonality.

So my genuine position: I'd admit **membrane** without hesitation, and **production** with the explicit caveat that it's borrowing the axial seat from the type-core. Which reading do you hold — is the axis the rule, or the type?

---

**[Dev]** The rule as data is pattern + action which is a typed (structured) representation of a non-actualized process

**[Claude]** That correction lands, and it dissolves my mapping rather than adjusting it. If the rule-as-data — `pattern + action` — is a *typed representation of a non-actualized process*, then the rule is **latent**, not actual. Its actualization isn't in the definition at all; it's the RETE firing at runtime. So "production = actual" was wrong.

And that's not a small patch — it breaks the equivalence class I'd accepted. The rule occupies **(axial, latent) simultaneously**: axial because `pattern → action` lays down a directed *process axis* (antecedent → consequent, a through-line), latent because as data it hasn't run. A single object sitting at axial-AND-latent is only possible if **liminal/axial and latent/actual are two orthogonal axes, not one collapsed polarity.** The rule is the witness that pries them apart.

Corrected, they cross into a real grid — and the whole definitional world (drake, JSON, aspects) sits in the **latent column**:

| | **latent** (definition / data) | **actual** (execution / runtime) |
|---|---|---|
| **liminal** (boundary) | `actor` aspect — non-actualized membrane (`start/message/signal` as data) | live Pekko actor receiving messages |
| **axial** (process through-line) | `rule` aspect — non-actualized process (`pattern/action` as data) | Evrete firing the production |

`type-domain` (Value) is the **substrate/origin** — no boundary, no process-axis; its latent form is the type definition, its actual form a live instance holding state.

The payoff is that latent/actual turns out to be **draco's own definition-plane / execution-plane split** — the JSON-as-single-source-of-truth architecture *is* the latent column, and Evrete-firing + Pekko-delivery is the actual one. Every aspect then reads uniformly as "a typed representation of a non-actualized X": rule = non-actualized *process*, actor = non-actualized *boundary/agent*, value = non-actualized *structure*.

So the sharpened answer to your standing question: **membrane/production is a member of the liminal/axial axis — but *not* of "that equivalence class," because the class doesn't hold.** liminal/axial and latent/actual are independent, and drake is the artifact that lives wholly on the latent side of the second axis while spanning the first. Does that orthogonality match your intuition — that everything drake authors is latent by construction, and the actual only ever exists downstream in the running system?

---

**[Dev]** Yes, I like your latest orthogonality. How can it help us decide when to include either or both of rule and actor in any particular type definition?

**[Claude]** The orthogonality gives you exactly one thing a 4-way classification wouldn't: **you never decide "what kind of type is this" as a single choice — you ask two independent yes/no questions, one per axis, and neither answer constrains the other.** A boundary never implies a process and a process never implies a boundary. So the procedure is compositional and monotonic: **start from the substrate (Value) and add a pole only on positive evidence that the type inhabits it.**

**Include `actor`? — the liminal / boundary test.** Does the type mediate an inside/outside — a threshold something *crosses into*? Does it have identity that persists across interactions (a `self`, a mailbox)? State that outlives a single message? Does isolation matter — must it be touched *only* by messages? If yes, it needs the non-actualized boundary. If it's never *received into*, it doesn't.

**Include `rule`? — the axial / process test.** Does the type embody a directed `when-P-then-A` through-line? Is there a condition → consequence, a `pattern → action`? Is it a *standing law* that fires whenever facts match — not something called imperatively? Does it transform working memory rather than merely *be* a structured value? If yes, it needs the non-actualized process.

Since drake is wholly latent, both tests are really: *when this type is actualized downstream, will it need to receive across a boundary, and/or fire a transformation?* You're pre-declaring runtime capability.

**The one place the two tests interact is the bottom-right corner** — and orthogonality reframes it as a *locality* decision, not an ontological one: **fuse → `rule`+`actor` (Cell)** when the process is *proper* to this agent — its own metabolism, meaningless without this boundary, not reused; **compose → `actor` here + a separate `rule`-only type** when the process is *ambient* — a domain law many boundaries invoke. Orthogonality is what licenses that choice: the process's *existence* never forces it into the actor's definition, so you place it by whether it belongs to the agent or to the domain.

So the whole rule reduces to: **default to Value; add `actor` iff there's a real boundary; add `rule` iff there's a real process; and when both are present, fuse only if the process is proper to the agent, else keep it a shared Production.**

---

**[Dev]** Let's focus on Actor.json, Actor.scala and Actor.drake. There seems to be some holdover from earlier versions of draco.Actor where actorDefinition is still needed. Since Actor has a type parameter, it shouldn't be directly instantiated. So given it has a factory, we need to figure out if that is necessary for type definition with a type parameter in the type name.

**[Claude]** *(examined the three Actor files, `ActorType`, and all consumers of `Actor.apply`/`Null` across the codebase)* This clears up cleanly. The findings:

- **`Actor.apply` and `Actor.Null` are dead** — zero references anywhere in `src/main`, `src/mods`, or `src/test`.
- **Concrete actors bypass the factory entirely.** `aerial/Consumer.actorType()` (and all ~13 others) construct `new Actor[Json] { override actorDefinition = …; override receive/receiveSignal = … custom … }` — emitted by the Generator's per-actor `actorType()` path (Generator.scala:995), not by `Actor.apply`.
- **The type parameter is a red herring for the factory question.** `Type[T]`, `Domain[T]`, `Rule[T]` are all parameterized *and* carry factories whose `apply` is called pervasively; `Primal[T]` is parameterized and has *no* factory. So "has a type parameter" tells you nothing about whether a factory belongs.

**What actually decides it:** is there a *uniform* `apply`-construction that draco or user code actually calls? For `Actor` there isn't, and there structurally can't be — a generic `apply[T](_actorDefinition)` can only ever build a **no-op** actor, but an actor's entire essence is its *overridden* `receive`/`receiveSignal`, which is non-uniform per actor and can't come through a fixed factory signature. So the factory is vestigial. That matches exactly what you said — `Actor[T]` is the **abstract container for the `actor` aspect**: extended, never directly instantiated. In our earlier vocabulary it's a *substrate* trait whose "instances" are the anonymous subclasses the Generator emits.

**Recommendation:** remove the `factory` from `Actor.json`; the regenerated `Actor.scala` drops the dead `apply[T]`/`Null`, and `Actor.drake` carries **no `factory` head at all** — which also retires the block copied over from `Action`. The general principle this settles for the language: **a `factory` is warranted when a type has a uniform construction someone calls; an abstract container meant to be *extended* carries none — independent of type parameters.**

Separately — and I'd hold it as its own step — `actorDefinition` itself is the deeper holdover you sensed: every concrete actor sets `actorDefinition == typeDefinition` (same value, twice), so it's redundant with `DracoType.typeDefinition`. Retiring it touches ~14 concrete actors + the Generator emission, so it deserves a dedicated increment.

---

**[Dev]** I think there may be similar issues with domain and rule.  I'm not sure about draco type in general. Should we address the aspects one by one or try to determine if all four aspects share these issues?

**[Claude]** Good instinct to check whether this is one problem or two — this shouldn't be reasoned a priori. *(Gathered evidence across all four aspects.)* It turns out you're looking at **two different issues with opposite shapes**, and that's what should drive the method (not aspect-by-aspect):

| Aspect | Container factory (`apply`/`Null`) | `*Definition` alias field |
|---|---|---|
| **DracoType** (root) | `Type[T]` — **live** (`dracoType` everywhere) | none — `typeDefinition` *is* the axiom |
| **Domain** | `Domain[T]` — **live** | `domainDefinition` == typeDefinition; `DomainType` also has real `typeDictionary` |
| **Rule** | `Rule[T]` — **live** (every generated rule) | `ruleDefinition` == typeDefinition; `RuleType` also has real `pattern` + `action` |
| **Actor** | `Actor.apply`/`Null` — **dead** (0 refs) | `actorDefinition` == typeDefinition; `ActorType` has **nothing else** |

**Issue A (dead factory) is Actor-only.** Type/Domain/Rule factories are all live — those three construct from *data*. Actor is the sole aspect whose runtime form is method overrides on `ExtensibleBehavior`, which no uniform data-parameterized `apply` can build. That's the ontology from before showing up in the code: the actor is the one aspect whose actualized form isn't data.

**Issue B (redundant `*Definition`) is a symmetric triad** (domain/rule/actor), all aliasing `typeDefinition`, rooted in DracoType. Its *weight* is uneven — and Actor is the tell: strip `actorDefinition` and `ActorType` is an **empty marker trait**, which proves the field was carrying nothing but legacy. For Domain/Rule the alias sits beside genuinely-additional members, which survive.

**Recommendation: split by issue, not by aspect.** Issue A → close on Actor alone, now (fully diagnosed and localized). Issue B → one decision, applied to the triad together. My lean on Issue B: **retire all three aliases in favor of `typeDefinition`**, keeping only the real additions — but that's the symmetric-design call CLAUDE.md deliberately made, so it's genuinely yours. (One thread to pull *into* the Issue-B pass, later: the `*Type` **trait** companions carry their own `apply`/`Null` — e.g. `RuleType.apply` — that generated code never calls. Those look uniformly dead.)

---

**[Dev]** Yes, start with issue A.

**[Claude]** *(Modeled expected output on `Primal.scala` — a factory-less parameterized type emits just `trait` + companion — then edited the three files.)* Issue A applied:

| File | Change |
|---|---|
| `Actor.json` | Removed the `factory` block from `dracoAspect` |
| `Actor.scala` | Dropped the dead `apply[T]`/`Null` and the pekko imports only the factory body used |
| `Actor.drake` | Reduced to the faithful surface of the new JSON: `type` + derivation, then the bound `domain` line — clearing the `elements`/`factory` copied from `Action` and the empty `rule`/`actor` heads |

Two things I want to flag rather than assume: **verification is yours to run** (`DracoGenTest` compares generated output against the hand-written `Actor.scala`; the one spot I predicted rather than derived is the import line), and **`Actor.drake` scope** — I removed the empty `rule`/`actor` heads per the elide-empty-heads model, but if you were keeping those deliberately as part of the all-four-heads exploration, say so and I'll put them back; I didn't want to quietly overreach on your discovery drafts.

---

**[Dev]** 1. I realized when looking at Actor.json, that values that appeared as strings  did not have to be typed as String in drake. Also, I wanted to make sure that the extensible field was used properly as generating an extends to an external type, in the case of Actor, from Pekko
2. The empty and / or incorrect aspect data in Actor.drake was due to manual changes made to discuss general nature of drake and not to indicate anything specific about the Actor generic type completely.

**[Claude]** *(Verified `typeExtends`, the import scanning, and `isActor` detection in the Generator.)* On point 1b, the finding: **currently the `extensible` field is *not* used properly for Actor.** `Actor.json` has `ExtensibleBehavior[T]` sitting in `derivation` alongside `ActorType`, and `typeExtends` only produces `extends ExtensibleBehavior[T] with ActorType` via its *fallback*. The `extensible` field — whose whole purpose is to be the **head of the `extends` clause for an external type** — is empty.

Your **drake already expresses the split correctly**: `from ActorType` → `derivation`, and `extensible …ExtensibleBehavior(T)` → the `extensible` field. It's the JSON that lumped them together and bypassed the field. So `ExtensibleBehavior[T]` moves into `extensible`, leaving `ActorType` as the sole `derivation` entry. I verified this is safe on all three downstream paths — `typeExtends` (identical output via the ext-head branch), imports (`externalTypeImports` keys on the type name, not the field), and `isActor` (still true via `ActorType`) — so `Actor.scala` stays byte-identical. *(Applied to `Actor.json`.)*

On your other points: **1a — quoted values don't need the `String` annotation.** Agreed, and the precise rule falls out of the settled `"x"`=string-literal / unquoted=code-expression convention: when a `fix`/`par` value is a **quoted literal**, its type is inferable as `String` — `fix name "ctx"` suffices over `fix name String "ctx"`. Only for bare string literals; a value that's a function still needs its type. **2 — discussion scaffolding.** Understood, and that confirms the reduction was the right call: `Actor.drake` now says only what `Actor.json` says.

Two conventions surfaced here worth pinning: **the `extensible` field = head-of-`extends` for an external type** (vs `derivation` = internal `with`-mixins), and **quoted literal ⇒ elidable `String` type**. Record now, take up Issue B, or hold for your test run?

---

**[Dev]** Record first please.

**[Claude]** Recorded in `project_drake_language.md` — the `extensible`/`derivation` split, the quoted-literal convention, and factory-necessity-is-orthogonal-to-type-parameters, with Issue B forward-linked. My lean is to **run the test first** — it validates the Actor change set before we start a wider triad sweep on top of it.

---

**[Dev]** Put the tests to run each in individual code blocks in your next response please.

**[Claude]** Here are the tests to run, most-targeted first:

```bash
sbt "testOnly draco.DracoGenTest"
```

```bash
sbt "testOnly draco.YAMLRoundTripTest"
```

```bash
sbt "testOnly draco.NaturalActorTest"
```

```bash
sbt "testOnly domains.aerial.AerialActorTest"
```

```bash
sbt test
```

If `DracoGenTest` reports a diff for `Actor.scala`, paste the `hand-written | generated` block and I'll reconcile before you go further.

*(Delegated action — Dev ran `sbt "testOnly draco.DracoGenTest"`: 102/103, one failure on `Actor.scala`.)*

The failure is isolated to `Actor.scala` and it's purely the **imports** — the factory removal worked. Root cause: two Generator gaps that removing the factory exposed. **(1)** `externalImports` scans `derivation.map(_.name)` but not the `extensible` field — that's why moving `ExtensibleBehavior` derivation→extensible dropped its import. A genuine Generator gap, not a mismodeling on our part. **(2)** `pekkoImports` (the static `Behavior/Signal/TypedActorContext/Behaviors` block) is added unconditionally for any actor-typed emission — but those are only used by `receive`/`receiveSignal` method bodies, which come from the factory. A factory-less actor container needs none. Both old and new `Actor` take the `isLeaf || isActor` fall-through branch that forces `instanceType = "actor"`; the old one genuinely needed those imports, the new one doesn't.

Two precise, general fixes applied: `externalImports` now also scans the `extensible` field (symmetric with `derivation`), and `pekkoImports` is gated on factory presence. Tracing the result: generated `Actor.scala` gets exactly `import …ExtensibleBehavior` and nothing else — matching the hand-written file. The changes are Generator-wide, so the same run re-verifies all 102 other types didn't regress.

*(Delegated action — Dev re-ran: `DracoGenTest` green, but `sbt test` 199/200 — `GenerateAndCompileTest` "Draco core group" failed with `not found: type ExtensibleBehavior`.)*

This is the **multi-type** generation path, distinct from the single-type path I fixed. Confirmed: the multi-type merge builds a synthetic `mergedTd` for import scanning that concatenates `derivation`, `elements`, `factory.parameters`, `globalElements` — but **not `extensible`** (a scalar it can't concatenate). So when `Actor` is in the group, its `ExtensibleBehavior` is dropped from the merge and the group fails to compile. Before my change it rode along in `derivation`. Fix: fold every constituent's `extensible` into the merged `derivation` so `externalImports` finds them all — the merged TD exists purely for import computation, so this is exactly its job.

*(Delegated action — Dev ran `sbt "testOnly draco.GenerateAndCompileTest"` then `sbt test`: all **200/200**.)*

Full suite green. Issue A is complete and verified: `Actor`'s vestigial factory removed, the `extensible` field now properly drives the Pekko `extends`, and three Generator import gaps fixed (extensible-field scanning, factory-gated pekko imports, multi-type merge folding extensibles). The Generator fixes are general improvements, not Actor-specific patches. Recorded, with Issue B teed up. This is a clean stopping point — commit Issue A as a self-contained unit, or continue straight into Issue B?

---

**[Dev]** Continue with issue B.

**[Claude]** Issue B — retiring the `domainDefinition`/`ruleDefinition`/`actorDefinition` aliases. This is the wider triad, so I mapped the exact blast radius across all three before touching anything. Key findings that shape the plan:

- **Concrete actors are Generator-emitted but checked-in** (Generator.scala:996 emits `override lazy val actorDefinition = …`) — so retiring `actorDefinition` ripples to the Generator **plus ~14 committed actor files**, the biggest sweep.
- **Generated rules never pass `_ruleDefinition`** and `RuleType.apply`/`Null` appear uncalled — so `ruleDefinition` is genuinely dead weight, but its factory threading makes it the fiddliest.
- **Domain** is the cleanest: retire `domainDefinition`, keep `typeDictionary` — no factory gymnastics, no 14-file sweep.

So I'll execute **Domain → Rule → Actor** (cleanest to messiest), one aspect per test gate. Minimal, low-risk pattern: retire the `domainDefinition` *field* but keep the factory *param* name (`_domainDefinition` is just plumbing; the redundant field is the target).

*(Applied the domain edits: `DomainType.json`/`.scala`, `Domain.json`/`.scala`, `DomainBuilder.scala` — dropped the `:65` override, repointed the two readers at `:96`/`:133` to `typeDefinition` — and three readers in `DomainBuilderTest.scala`. Verified only the intended plumbing references remain. Gate: `DracoGenTest`, `DomainBuilderTest`, `GenerateAndCompileTest`.)*

*(Delegated action — Dev ran all three gates: green — DracoGenTest 103/103, DomainBuilderTest 13/13, GenerateAndCompileTest 3/3.)*

Domain aspect green. Moving to the rule aspect: `RuleType.json` has an explicit factory body, so I keep its `ruleDefinition` param as the `typeDefinition` source (same minimal pattern as domain) and drop the redundant field line; `Rule.json`'s factory is parameter-derived, so removing the `ruleDefinition` *param* drops the override automatically. Generated rule companions call `Rule[X](_pattern=…, _action=…)` and never passed `_ruleDefinition`, so they're unaffected. *(Edited `RuleType.json`/`.scala`, `Rule.json`/`.scala`; gate: `DracoGenTest`, `PrimesRulesTest`, `GenerateAndCompileTest`.)*

*(Delegated action — Dev ran the gate: compilation failed — 22 errors, "too many arguments (found 3, expected 2)" in every generated rule companion.)*

The generated rule companions **do** pass a first positional arg (the `typeDefinition`) into the old `_ruleDefinition` slot — I missed line 38 earlier. `ruleGlobal` in the Generator emits `Rule[X](<typeDefinition>, _pattern=…, _action=…)`, and removing the param broke it. It's the same "the field was actually used" surprise as Actor's factory. The fix removes that arg from the Generator **and** the 11 checked-in generated rule files — retiring `ruleDefinition` means the rule's own def is no longer redundantly stored; it stays reachable via the companion's `typeDefinition`. *(Fixed `ruleGlobal`, then swept the 11 rule files with a per-file filter after a first perl slurp mis-fired and applied nothing. `PrimesRulesTest`'s "Generate …" cases regenerate the three primes rules from the fixed Generator, double-checking the hand-edits match emission.)*

*(Delegated action — Dev re-ran the gate: two errors remained — `TupleFactRule.scala` under `src/test`.)*

Missed a test rule — my file list was scoped too narrowly (`feedback_search_main_project` in action: I'd searched `src/main`+`src/mods` and skipped `src/test`). Found and fixed the last remaining `Rule[…]` call with the positional arg across the **whole** project.

*(Delegated action — Dev ran the full suite: **200/200**.)*

Rule aspect complete. Now the final and largest: the **actor aspect**. The picture: 14 override sites (12 `override lazy val`, 2 `override val` in EgoActor/NaturalActor) + the Generator emission at line 995 + the `ActorType` trait. *(Applied all four parts: Generator emission removed; `ActorType.json` loses its sole `actorDefinition` element, leaving `ActorType` an empty marker trait extending `DracoType`; the 14 concrete actor override lines deleted; verified no `actorDefinition` reference remains anywhere.)* This aspect touches the most runtime surface, and `AerialGenTest` specifically re-checks that the Generator's actor emission still matches the hand-edited concrete actors.

*(Delegated action — Dev ran `DracoGenTest`, `AerialGenTest`, `sbt test`: all green, **200/200**.)*

Issue B is complete across all three aspects. The through-line: each "redundant" alias had exactly one live reader that had to be repointed to `typeDefinition`, not just deleted — most sharply the rule aspect, where `ruleGlobal`'s positional `typeDefinition` arg rippled to 12 rule files (including one in `src/test` my first sweep missed). Recorded, and the dev-journal chapter written. Open follow-up: the **dead trait-companion factories** — `RuleType` (and the pattern generally) still carries `apply`/`Null` that generated code never calls.

---

**[Dev]** Let's continue with dead-factory cleanup

**[Claude]** Confirmed the scope first: `RuleType.apply`/`.Null` are dead (the only matches are `loadRuleType`, an unrelated Generator method). And among the abstract `*Type` traits, `RuleType` is the *only* one with a factory — `DomainType`/`ActorType`/`DracoType` have none. So the dead-factory cleanup is precisely `RuleType`'s factory (rules are constructed via the `Rule[T]` container, never `RuleType.apply`).

*(Removed the factory from `RuleType.json` and regenerated `RuleType.scala` — the companion is now just `typeDefinition`/`dracoType`/`domainType`, matching the shape of `DomainType`/`ActorType`. The `Consumer`/`Knowledge`/`RhsContext` imports stay: the trait's `action`/`pattern` fields still need them, and `externalImports` derives them from the elements, not the factory.)*

*(Delegated action — Dev ran `DracoGenTest` + `sbt test`: **200/200**, first run.)*

The dead-factory cleanup landed cleanly with **zero ripple** — which itself confirms `RuleType.apply`/`Null` were genuinely dead (unlike the `*Definition` aliases, each of which hid one live reader). `RuleType` is now a pure abstract trait, symmetric with `DomainType` and `ActorType`. Everything from this session is landed, verified, and journaled: the DRAKE grammar settlement, the ontology, Issue A, Issue B, dead-factory cleanup — full suite 200/200 at every gate. The working tree is in a committable state.

---

**[Dev]** I don't want to stop but i would like a new draco-git-record to commit/push from.

**[Claude]** Per your workflow, you author the `draco-git-record/` file (I don't create those) — name it `git-record-2026-07-04-HHMM`, and `git add -A` sweeps it into the same commit. *(Provided the runnable heredoc commit block: "DRAKE aspect-heads + retire Definition-alias triad + dead-factory cleanup … Full suite 200/200.")* Heads-up on scope: `git add -A` will also sweep the **in-flight `.drake` discovery files** modified before this session (`Action.drake`, `ActorAspect.drake`, `BodyElement.drake`, `Draco.drake`, plus the reduced `Actor.drake`) — if you'd rather keep those out, I'll give you a scoped `git add` list instead.

---

**[Dev]** Next I want to make all the .drake files consistently use the current implicitly defined syntax (which we will determine together) and then make them consistent with the corresponding json type definitions.

**[Claude]** This is a substantial, careful task with three phases: (1) determine the canonical DRAKE syntax together, (2) make all `.drake` files use it consistently, (3) reconcile them with their JSON. I won't edit anything yet — the first move is to **survey every `.drake` file and catalog the syntax as currently practiced**. *(Read the whole corpus.)* Here's the implicit grammar I extracted, plus the axes where files actually disagree:

```
[type] <Name> [(<typeParams>)] [from <Parent>] [modules [<M>...]]
  [extensible <pkg…> <ExtType>(<T>)]
  [elements [ <fix|mut|dyn|par|…> <name> <type> [<value>] … ]]
  [factory <ValueType> [parameters [ par … ]] [body [ fix … ]]]
  [globalElements [ fix … ]]
[domain] <pkg…> <Domain> [types [ … ]]
[rule] [pattern [variables [var …]] …] [action [mon|fix …]]
[actor] [start] [message] [signal]
```

**Phase 2a — surface-style axes** (pure drake, no JSON impact): head order (17 files `domain`-first, 4 `type`-first → flip the 17 to the settled order); empty `rule`/`actor` stubs (elide, per the settled model); closing `]` style (16 own-line vs 4 trailing — *your call*, I lean own-line); `globals` vs `globalElements` (I lean `globalElements`, matching the JSON field); `modules` inline vs block (keep the implicit single→inline / multiple→block rule); `String` on quoted literals (elide, per the recorded convention).

**Phase 2b — drake↔JSON divergences** (flag now, reconcile after 2a): `ActorAspect`'s `start`/`message`/`signal` rename vs the JSON's `setupAction`/`messageAction`/`signalAction` (its `isEmpty` is even internally stale); `Draco.drake`'s `types [ … ]` list; `TypeDefinition.drake` being *ahead* of its JSON; and a per-file field check.

The only genuinely open surface decisions are the bracket style and confirming `globalElements` and the quoted-literal elision — the rest are settled or no-ops.

---

**[Dev]** Let's work in the format of your "implicit grammar" meta-syntax. I added a file in src/main/resources/draco/drake-grammar with a next "cut" of the  drake syntax. It is still incomplete and inconsistent, but I'm hoping we can make more rigorous and consistent through interactive discussion.

**[Claude]** *(It's spelled `drake-grammer` — read it.)* This is a good next cut. Before the content, there's one **foundational rigor problem** that blocks reading everything else: **`[ ]` is overloaded.** The grammar uses it for two incompatible things at once — meta-optional (`[modules …]`, `[type]`) and literal drake sequence (the `[<M>...]` *inside* `modules`). So `[modules [<M>...]]` has an outer optional-bracket and an inner drake-Seq-bracket that look identical, and `[type]`/`[domain]` read as "optional" even though they're the mandatory heads.

Proposed meta-notation (minimal; it frees `[ ] { } ( )` to mean *only* literal drake tokens): `<Name>` = nonterminal, bare `keyword` = literal, trailing `?` = optional, `...` = repeats, `|` = alternation, indentation = nesting, and brackets always literal drake. Under this, mandatory heads are bare and optional ones carry `?`.

Content issues queued for after we lock the notation: **(1)** `factory` lost its `<ValueType>` — inferred now, or explicit? **(2)** `<value>` is written mandatory in `elements` but practice has value-less elements — should be optional. **(3)** `var` field order is flipped — grammar says `var <ValueType> <InstanceValue>`, practice is `var <name> <ValueType>`; a `Variable` is `name` + `valueType`. **(4)** Expression-category proliferation — six names (`<value>`, `<DrakeExpr>`, `<BoxedDrakeExpr>`, …) wanting a rigorous base-plus-refinements story. **(5)** `super` now sits under `domain` — but its JSON home is `dracoAspect.superDomain`. **(6)** Decided by your grammar: you wrote `globals` — that settles my earlier axis toward `globals`. (Aside: happy to rename `drake-grammer` → `drake-grammar` when convenient.)

---

**[Dev]** I have changed drake-grammar to contain a fixed structure grammar with optional reserved keywords prefixed with "*". I think this will be easier to discuss than using a meta-syntax.

**[Claude]** Much clearer — the `*`-prefix-for-optional plus indentation is far more readable than the bracket-overloaded meta. It also quietly settles the block-bracket question: element/factory/globals/pattern/action bodies are now **pure indentation**, with literal `[ ]` surviving only for genuine drake sequences (`modules`, `types`).

One convention gap to close first: `*` marks optional *keywords*, but several **operands** are also optional (`<value>` in an element; `<default-value>` in a param). Proposal: let `*` prefix placeholders too → `*<value>`, `*<default-value>`.

Consistency fixes I'd apply mechanically: missing `*` on `*types`, `*super`, `*conditions`, `*body`, `*start`/`*message`/`*signal`; leftover meta-brackets from the prior cut; a `<Name>`/`<name>` casing slip.

Four semantic calls I need from you: **(1)** `par _<name>` → `par <name>`? Params are written *without* the underscore in practice; the `_variables` form only appears as a value reference inside a body. **(2)** `var <value-type> <instance-value>` — practice is `var <name> <value-type>`; a `Variable` is an Evrete fact binding with no "instance value." **(3)** `factory` dropped its `<ValueType>` — inferred from the type name, or explicit? **(4)** `values` is absent from `rule` — `RuleAspect` is `pattern` + `values` + `action`; deliberate?

---

**[Dev]** 1. Many parameter names are likely to be the same as names in elements. Since mapping parameters into body elements can sometime cause names to be overridden improperly, the "_" is added to all parameter names to avoid this. The "override" problem may be target language dependent, so it's handled universally in the definition language to make it easier for the Generator implementation across multiple target languages.
2. You are correct. I was distracted by the knowledge that instance values of value types have to be interted into working memory. This reminds me that there are particular nuances to observe when binding values to names in the rule action. TBD.
3. Since factory exists only for "concrete" types, the name used when invoking the factory should be the same as the name of the concrete type.
4. The values keyword is absent from rule because that is one of the nuanced bindings to be addressed by the Generator when generating the action.

**[Claude]** All four confirmed and understood: **(1)** `par _<name>` stays — the `_` is a universal definition-language device preventing parameter/element name collisions, kept in the *definition* (not per-target-language Generator logic) so every backend stays simple; and it has a clean consequence: the param declaration and its body reference become *symmetric*. **(2)** `var <name> <value-type>` — corrected; the working-memory-insertion nuance is real but lives in *action* generation → filed as **TBD-A** (value↔name binding in rule actions). **(3)** `factory` has no name operand — the constructed type is always the enclosing `<name>` (+ its `*(<T>)`), so the valueType is inferred. **(4)** `values` stays out of `rule` → **TBD-B**, pairing with TBD-A.

Here's the consolidated grammar with everything applied:

```
type <name> *(<T>) *from <type>
  *modules [ <module> ... ]
  *extensible <pkg-seq> <ExtType> *(<T>)
  *elements
    <fix|mut|dyn> <name> <value-type> *<value>
    ...
  *factory
    parameters
      par _<name> <value-type> *<default-value>
      ...
    *body
      <fix|mut|dyn> <name> <value-type> *<value>
      ...
  *globals
    <fix|mut|dyn> <name> <value-type> *<value>
    ...
domain <pkg-seq> <name>
  *types [ <name> ... ]
  *super <pkg-seq> <name>
*rule
  pattern
    variables
      var <name> <value-type>
      ...
    *conditions
      con <bool-value>
      ...
  action
    fix <name> <value-type> <value>
    ...
    mon <unit-value>
    ...
*actor
  *start
  *message
  *signal
```

What I changed/propose (all reversible): the added `*`s, the meta-bracket cleanup, `var <name> <value-type>`, the `*<value>`/`*<default-value>` operand marker, and uniform value-category names (`<value>`, `<default-value>`, `<bool-value>`, `<unit-value>`). One known imprecision left in: `<fix|mut|dyn>` shares one shape, but `dyn` (Dynamic) really has parameters + a body, not a plain value — since `dyn` is currently unused, flagged as a later refinement. And one consequence of decision #1 to reconcile later: the current `.drake` files write `par variables` and the JSON stores `"name": "variables"` (Generator prepends `_`) — adopting `par _<name>` means Phase 2a rewrites the files and Phase 2b moves the `_` into the JSON param names.

---

**[Dev]** This is the version that is the most agreeable for us so far, so I feel confident to have my draco user colleagues us this as a template for writing type definitions before there's a parser to generate json type definitions.

**[Claude]** That's a great outcome — and since colleagues will hand-author from it without a parser, the file should be **self-describing**, so I wrote the agreed grammar *plus* a concise notation legend (`*`, `*<…>`, `<pkg-seq>`, the `par _` device, the factory-name inference) into `drake-grammar`, and the misspelled `drake-grammer` is gone. The **template is done** and committable as-is.

The two phases from your original goal are the natural continuation — the grammar is now the yardstick: **Phase 2a**, make the ~22 `.drake` files consistent with it (pure surface: flip the 17 `domain`-first files, elide the empty stubs, drop enclosing `[ ]` from bodies, `par variables` → `par _variables`, fix `ActorAspect`'s stale `isEmpty`); **Phase 2b**, reconcile against the JSON (move the `_` into JSON param names, the actor lifecycle-name decision, per-file field checks) — test-gated like Issues A/B.

---

**[Dev]** Next to drake-grammar, let's create drake-symbols.properties that shows unambiguously how the tokens in drake-grammar map into json syntactic elements.

**[Claude]** Good idea — this becomes the drake→JSON compilation spec (the future parser's contract). To make it *unambiguous* I grounded the mappings against the actual JSON rather than assume: confirmed the value-type translations (`[Variable]`→`Seq[Variable]`, `(Long, String)`→tuple unchanged, `mutable.Set[…]`, `Consumer[Knowledge]` type-app) and the actorAspect fields (`setupAction`/`messageAction`/`signalAction`). One memory correction surfaced: `elementTypeNames` **is** populated in the domain JSONs (Draco, Primes, …), so `types` → `domainAspect.elementTypeNames` is a real, truthful mapping (and rule members appear aspect-suffixed, e.g. `AddNaturalSequence.rule`); `superDomain` lives in `dracoAspect` even though `super` is written under the domain head.

Created `drake-symbols.properties` next to `drake-grammar` — every drake token mapped to the JSON element it emits, with the non-obvious ones called out explicitly. The map is honest about three drake↔JSON gaps rather than papering over them, and these become the Phase 2b worklist: **(1)** actor lifecycle names — drake `start`/`message`/`signal` vs JSON `setupAction`/`messageAction`/`signalAction`; **(2)** `par _<name>` — the map states the `_` is part of the JSON parameter name (our decision), but the current JSON omits it and the Generator prepends; **(3)** `values` — no drake token; the Generator derives it during action generation (your TBD-B).

---

**[Dev]** I may have over-specified the file format for drake-symbols by specifying .properties. Do you think .json format would be better?

**[Claude]** Good instinct to question it — and there's a concrete gotcha. Verified: `DracoGenTest` does a **filesystem walk of `src/main/resources/draco/` and parses every `.json` as a TypeDefinition**, and the Generator's domain discovery scans the same way. So a `drake-symbols.json` *in this directory* would get picked up and fail to parse as a type. `.properties` and the extensionless `drake-grammar` are deliberately invisible to that walk.

So: **not `.json` — at least not here, not yet.** (1) The walk collision is a hard blocker. (2) JSON has no comments — this map leans on inline notes and the value-type rewrite table, which degrade into noisy `"note"` fields. (3) There's no machine consumer yet — JSON's advantage is machine-consumability; until something consumes it, that advantage is theoretical while the readability cost is immediate.

On your actual instinct — you're right that `.properties` is slightly *over*-specified, just in the other direction: it implies Java-config semantics this file doesn't need. The cleanest fix isn't more structure, it's **less**: plain text with no extension, matching `drake-grammar` — a matched human-facing reference pair, both invisible to the walk. When JSON *would* win: once the future parser consumes the map as data — at that point make it `.json` **outside** the scanned resource path, converting it from documentation into an executable spec.

---

**[Dev]** You're doing great. I really like how you organized drake-grammar. Now I'm thinking we can merge drake-grammar with drake-symbols and extend the "template format" for everything. Rename the combination to something appropriate that starts with "drake" and ends with:
".<your-template-format-ext-name>"

**[Claude]** One canonical, self-describing reference beats two files that have to be cross-read. I'll call the format a **drake template** with extension **`.dt`**, so the merged file is `drake.dt`: the clean grammar you liked, with the symbol map folded *inline* so each line shows *both* its syntax and the JSON it emits — e.g.

```
  par _<name> <value-type> *<default-value>  -> { kind=Parameter , name , valueType , value }
...
domain <pkg-seq> <name>                      -> domainAspect.typeName { namePackage[] , name }
```

One document, sections `NOTATION`, `FILL-INS`, `VALUE-TYPES`, `CONVENTIONS`, the annotated `TEMPLATE`, and `DIVERGENCES` (the two live drake↔JSON gaps stay called out so nobody mistakes the template for already-reconciled). Both `drake-grammar` and `drake-symbols` removed — there was also a leftover extensionless `drake-symbols` (you'd already applied the plain-text rename), removed too. Like the old files, `.dt` is invisible to the `.json`-only resource walk.

---

**[Dev]** Slight adjustment to the file extension name: instead of "drake template" (.dt) use "definition language template" (.dlt) as drake is a "definition language" not "programming language"

**[Claude]** Good distinction — drake is a definition language, so `.dlt` (definition language template) is the right call. Renamed to `drake.dlt`, with the file's own label and the memory references updated to match. The template is settled and committable.

---

**[Dev]** Is there anything that needs to be done before we create a .drake file for every json format type definition that can generate scala source code, that is currently successful in all tests?

**[Claude]** That's exactly the right question to ask before mass-authoring — the core risk is **template completeness**: can `drake.dlt` express *every* construct in the passing corpus? *(Scanned the JSON: 63 files in the draco tree, 22 have `.drake` today, ~41 to author.)* The answer is **yes, a few things**, two of them genuine blockers:

**1. Template can't express type bounds.** Four types carry a bound the template's `*(<T>)` doesn't cover: `Coordinate` (`T <: Product`), `DomainTransform`, `TypeTransform`, `Holon`. A small, clean template extension.

**2. drake *deliberately* can't express raw host-plumbing / multi-line bodies.** Five types carry multi-line raw Scala and/or hand-written codecs: `Value`, `TypeDefinition`, `YAML` (hand-carried `Encoder`/`Decoder`/`.asJson` blobs), `SourceContent`, `Primes` (multi-line `Monadic` bodies, e.g. the sieve). Under the current intentional design these **can't get a faithful `.drake`** — so the goal is realistically *"a `.drake` for every json drake can express."* My recommendation: scope these five out for now, documented, rather than block on the Generator-derived-codec work.

**Two decisions to lock first** (or eat ~40-file rework): **(3)** the reconciliation stance — do new files match **current JSON** (`variables`, `setupAction/…`) or the **template target** (`_variables`, `start/message/signal`)? My lean: author to the template target *and* do the JSON reconciliation in tandem, so drake and JSON converge instead of drifting. **(4)** Align the existing 22 first (Phase 2a), so the corpus isn't half old-style.

One caveat, not a blocker: there's still **no parser**, so every authored `.drake` is an *unverified hand-transcription* of its JSON — at ~40 files the error rate matters; the higher-leverage move for a *verified* corpus is a minimal `.drake → JSON` round-trip check, but the status quo is a legitimate choice too.

Recommended pre-sequence: (1) extend `drake.dlt` for type bounds; (2) scope out the five plumbing types with a note; (3) lock the `par _` / actor-name stance; (4) normalize the 22, then author the rest.

---

**[Dev]** 1.  Type bounds looks do-able immediately. Go for it, and then we'll address 2,3 and 4 one at a time, in order.

**[Claude]** *(Read all four bound usages so the notation covers every form.)* All four are **upper bounds** (`<:`), one or two params: `T <: Product` (Coordinate, Holon), `S <: DomainType, T <: DomainType` (DomainTransform), `S <: DracoType, T <: DracoType` (TypeTransform). No lower/view bounds in the corpus. The natural notation mirrors the JSON exactly — the bound rides inside the type-param parens, comma-separated. **(1) Type bounds — done.** `drake.dlt` now reads:

```
  <T>              a type parameter: <name>, or upper-bounded as <name> <: <bound>
  (<T>, ...)       type parameters -> typeName.typeParameters[], one entry per param, verbatim with any bound:
                     (T <: Product)                    -> [ "T <: Product" ]
                     (S <: DracoType, T <: DracoType)  -> [ "S <: DracoType", "T <: DracoType" ]
```

`Coordinate`, `Holon`, `DomainTransform`, and `TypeTransform` are all now authorable. Next up is **(2)** — the five raw host-plumbing types. My lean: document them as out-of-drake and note *why* — host plumbing lives outside the definition language — rather than block on the Generator-derived-codec work.

---

**[Dev]** The JSON decoder is just calling the factory with json decoded factory parameters.  That means that the json encoder is just encoding the parameter named values into json data. That should be derivable from existing type definition data. Anything other than that requires and explicit codec in the type definition that can be parameterized with existing references in the type definition used in the parameters and bodies of the explicit codec. Let me know what I'm missing here.

**[Claude]** You're not missing the core thing — that *is* the design, and it's largely what the Generator already does: `simpleCodecDeclaration` derives exactly your codec (encode the fields, decode by calling the factory) whenever the factory params are a subset of the elements. The hand codecs are the exception, not the rule. Three refinements make it airtight:

**1. It's a small *family* of derivations, not one rule.** Product/leaf (your rule); sum/sealed (`modules` present — decode reads the `kind` discriminator and dispatches to the subtype's factory); and the **elision variant** — the piece "just encode the parameter values" under-specifies: a naive param encoder writes *every* field, but the canonical JSON omits empties. Elision needs a per-field emptiness test + default, which you already carry as the aspects' `isEmpty` and the factory defaults / `Null`. Still derivable — just from isEmpty+defaults, not params alone.

**2. The `_` convention makes the mapping exact, and names the precondition.** Param `_x` ↔ JSON key `x` ↔ instance field `x`. The precondition: every factory param must be a *recoverable stored field* — which holds exactly when params ⊆ elements. A transient param, used only to compute other fields and not stored, breaks the round-trip → forces an explicit codec.

**3. "Explicit parameterized codec referencing type-def data" is right — but it has a floor.** When the codec logic *is* references to existing fields (field-elision is the poster child), it's fully drake-able. But when the body is arbitrary multi-line host code — YAML parse/emit — framing it as an "explicit codec" just *relabels* the raw blob.

**The payoff — this splits the five blocked types better than "scope them all out":** only three carry `Encoder`/`Decoder` (Value, TypeDefinition, YAML), and TypeDefinition (almost certainly Value too) is the *derivable field-elision* case → derive the codec, **retire the hand blob, and they become fully drake-able**. `SourceContent` and `Primes` carry *no codec at all* — their blobs are non-codec host **algorithms** (file IO; the sieve). *Those* are the true out-of-drake tail, and your codec argument correctly doesn't reach them. So the refined plan for (2): **derive-and-retire the codec blobs**, and scope out only the real host-algorithm tail. Recorded.

---

**[Dev]** Was it clear from the description that "*codec" should become a field in type definition (after "*factory"?) that needs to be there to define custom (non-standard, non-simple) codecs?

**[Claude]** Honest answer: **the *concept* was clear, but I didn't crystallize it into that concrete form — you did just now.** Your point said "an explicit codec in the type definition ... with parameters and bodies," which does strongly imply a first-class, structured construct parallel to `*factory`. I picked up the *semantics* but described it loosely as "a parameterized codec element" and never landed it as a discrete **`*codec` field positioned after `*factory`**. I had the meaning, not the schema placement. Your framing is the right crystallization.

And it's a good design: **`*factory`** = how to *construct*; **`*codec`** = how to *(de)serialize* when the derived codec is insufficient — both optional, both in `dracoAspect`. Absent `*codec` ⇒ Generator derives; present ⇒ custom. It also fixes a mis-categorization: `TypeDefinition.json` currently hides its codec in `globalElements` as a `Monadic` blob — a `*codec` field is its proper home.

Two things to settle: **(1)** shape — my lean is `encode` / `*decode` sub-blocks, not factory-style `parameters`/`body`, since a `Codec` is fundamentally an (Encoder, Decoder) pair; and by *your own* decode-calls-the-factory observation, **decode is usually still derivable even when encode isn't**, so most `*codec` uses would specify only `encode`. **(2)** The floor still applies: `*codec` gives reference-based custom codecs a home, but a body of arbitrary multi-line host code (YAML) is still host code drake can't author — necessary-but-not-sufficient.

---

**[Dev]** I think what I was REALLY after was to make codec field optional so the Generator would know that a codec wasn't needed for that definition, then if it was present without specifying either encoder or decoder, the Generator would be able tou use the default codec, and if either encoder or decoder or both were specified then it would use them to override the default.

**[Claude]** That's a meaningfully better model than the one I sketched — I had `present = custom`, but yours is a graceful three-state where presence and contents mean different things:

| `*codec` in the definition | Generator emits |
|---|---|
| **absent** | *no codec* — this type isn't serialized |
| **present, empty** | the **default derived** codec |
| **present, `encode` only** | **custom** encode, **derived** decode |
| **present, `decode` only** | **derived** encode, **custom** decode |
| **present, both** | fully custom |

The field's *presence* is the "this type needs a codec" flag, and its *contents* are per-direction overrides. Three things follow directly: **(1)** it replaces the Generator's *inferred* `hasCodec` with a *declared* one — more in the spirit of drake, at the cost of a corpus pass. **(2)** **The elision self-reference — the one real trap.** `codec` lives in `dracoAspect`, and dracoAspect's own codec *elides empty fields* — a present-but-empty `codec` (`{}`) would get elided on encode, collapsing "present, empty" back into "absent" and destroying exactly the distinction the design needs. So `codec` has to be **exempt from elision**, or the three states become two. **(3)** Present-empty presupposes derivability — for a non-derivable type, the relevant direction *must* be specified; a validation rule the Generator can enforce.

---

**[Dev]** First check all the fields in draco aspect to see if they behave similarly and need present/default absent/unused semantics.

**[Claude]** Good call — confirmed the actual field list rather than trusting memory: 9 fields, and the `isEmpty` test shows exactly how each is currently collapsed (empty → elided). The audit: `superDomain`, `modules`, `extensible`, `derivation`, `elements`, `globalElements`, `source`, `target` all read as **2-state** — for them, empty and absent mean the *same thing* (no super-domain, not sealed, no external parent, root, no fields, …); nothing is derivable, so there's no "default" middle state and elision stays lossless.

**`codec` is unique** in needing field-level three-state — the *only* field where present-empty must stay distinct from absent, so the only one that must be exempted from elision. The reason is specific: a codec is **fully derivable from the type definition**, so "present-empty = derive the whole thing" is a *useful* state, *and* "no codec at all" is a genuinely different, legitimate state. No other field has both properties.

**`factory` is the interesting near-miss.** At the field level it's two-state (present ⇒ concrete, empty ⇒ abstract) — but it *already* carries the "empty ⇒ derive the default" idea one level down, in its **`body`** sub-field: empty body means "derive the parameter-mapped body," full body overrides. That's exactly your codec principle, just nested and two-state. So **your `codec` design isn't introducing a foreign concept** — what it genuinely adds is the *third* state (**absent = not applicable**), and that third state is the entire reason it alone needs the elision exemption.

---

**[Dev]** I think all of them need to be 3-state based on how I'm currently thinking about them. Remember, the only thing that is required for a draco type is:

 type <name> domain  <pkg-seq> <name>

**[Claude]** You're right, and I was reasoning inside the current elision design instead of the model you're building. I verified the linchpin case — `derivation` — and the evidence settles it; my two-state audit was wrong. Look at how `derivation` is expressed today: `DracoType` **absent** (root — extends nothing), `TypeName` **absent** (default — extends `DracoType`), `Primal`/`Type` `[DracoType]` (the default, written explicitly), `Action` `[BodyElement]` (explicit non-default parent).

So `absent` is currently **ambiguous** — it means "root/nothing" for `DracoType` but "default `DracoType`" for `TypeName` — and the default itself is written explicitly in some files and elided in others. That's not a stylistic inconsistency; it's a genuine semantic collision, a direct consequence of forcing the field into two states. Three-state *resolves* it: **absent** → Generator default (`extends DracoType`); **present-empty `[]`** → explicitly nothing (the root); **present-full** → explicit override.

One "data" field concretely needing all three knocks the bottom out of "data fields are two-state." The uniform semantics you're after:

> **For every aspect field:** `absent` = defer to the Generator's default/derivation · `present-empty` = engage it explicitly with the empty/none form · `present-full` = override with content.

This is only expressible if **`present-empty` never collapses into `absent`** — which means **field-elision has to go** (or be reworked). The codec "elision exemption" I proposed wasn't a special case; **non-elision is the general rule**, and codec was just where it surfaced first. One real structural consequence stated plainly: to carry three states through a decode→encode round-trip, each field must track **presence separately from value** — option-like. Today an empty `Seq` and an absent field are indistinguishable at runtime; under your model they can't be. Foundational, but principled — explicit-by-construction, which is the drake philosophy. It also implies a concrete corpus normalization payoff (drop the explicit `[DracoType]` from `Primal`/`Type`, make `DracoType`'s derivation present-empty, leave `TypeName` absent).

My recommendation: **lock the uniform three-state semantics now** (you've justified it), then take **representation** (retire elision; presence-aware fields) as its own deliberate step.

---

**[Dev]** I agree and I also think it has to be true for all aspects in the same way it's true for draco aspect. So, yes, let's start with  codec and expand incrementally after that.

**[Claude]** Agreed and recorded: the three-state semantics holds for **all four aspects** uniformly, and codec is the pilot we build the mechanism on. *(Grounded the codec increment on the real code — `Codec.scala` and `DracoAspect`'s generated encoder.)* Two findings that shape how codec must be built:

1. **`Codec[T]` is a runtime *functions* pair** (`encoder`/`decoder`), which can't be serialized. So the `codec` *field* can't hold a `Codec[T]` — it must hold a **codec *definition*** (data describing the custom encode/decode), a new small type; the runtime `Codec[T]` is what the Generator *emits* from it.
2. **`DracoAspect` fields are plain values with elision baked into the generated encoder** (`if (x.modules.nonEmpty) Some(...) else None`; decoder defaults missing → empty). That's precisely the two-state collapse. For codec to be three-state, its field has to break this: **presence tracked separately from emptiness.**

The natural representation, and the pilot pattern that later expands to every field: **`codec: Option[CodecAspect]`** — `None` = absent (omit the key) · `Some(empty)` = present-empty (`"codec": {}` → derive) · `Some(populated)` = override; the encoder rule is "emit when `Some`, *don't* elide." **`CodecAspect { *encode, *decode }`** — both optional; each omitted direction derives (decode defaults to calling the factory). Two cautions: **bootstrapping** — `DracoAspect` *is itself* a type definition whose own codec is generated, so this lands as a careful, test-gated step; and `Option` is the first presence-aware field, establishing the mechanism the others adopt. First increment: author `CodecAspect.json`+`.scala`, add the field, gate on `DracoGenTest` + round-trip — **with no type actually populating `codec` yet**, so it's purely additive and a no-op on every existing `.json`.

---

**[Dev]** Did we promote Codec to CodecAspect because of the type parameter?

**[Claude]** Good catch — and mostly yes, but let me be precise, because the "Aspect" suffix was a sloppy reflex on my part. Two things got tangled:

**Why it can't just *be* `Codec`:** the existing `Codec[T]` is a runtime *functions* pair — parameterized and unserializable. The codec *field* needs un-parameterized serializable *data*, exactly the way `Factory` (data, un-parameterized) describes construction while the emitted `apply[T]` is parameterized. In that sense the `[T]` is the tell — it's why we can't reuse the name.

**Why I wrote "CodecAspect" — that part was just wrong.** Codec is a *field within `dracoAspect`*, a sibling of `Factory`, not a top-level aspect like draco/domain/rule/actor. So it should be named like `Factory`, with no "Aspect" suffix. My bad.

The real problem is a **name collision**, not a promotion: the field-type *wants* to be `Codec` (to parallel `Factory`), but `Codec` is taken by the parameterized runtime trait. Two ways out: **(a)** free `Codec` for the definition type and rename the runtime holder (`Coder[T]`, say) — cleanest conceptually, but ripples through `Codec.sub` and the generated codecs; **(b)** keep runtime `Codec[T]`, name the field-type `CodecDefinition`. I lean **(b)** — the runtime `Codec[T]` is load-bearing in generated code. Which do you want?

---

**[Dev]** We just got rid of domainDefinition, ruleDefinition and actorDefinition because aspects didn't need them. Now we are adding codecDefinition because it's not an aspect. That seem more like Codec should be an aspect rather than not an aspect. What do you think?

**[Claude]** I think you're right — and it's more than a naming fix; it's the model telling us where codec belongs.

The tell you're pointing at: "not an aspect" forces a `*Definition` name, and we *just* deleted three of those. But the deeper reason it should be an aspect is **presence**. Aspects *are* the optional facets of a type — a type has-or-hasn't a rule aspect, an actor aspect. That present/absent nature is *exactly* the three-state semantics we've been building. So making codec an aspect means its three-state **is** ordinary aspect-presence — not a novel presence-aware field wedged into `dracoAspect`. And serialization is genuinely orthogonal to structure; it earns peer status next to rule/actor rather than living inside the structural aspect.

It also resolves both problems at once: **`codecAspect: CodecAspect`** — no `Definition` (purged), no collision with the runtime `Codec[T]`, and it reads exactly like its siblings `ruleAspect` / `actorAspect`. So I'd drop the field-in-`dracoAspect` idea entirely. Codec becomes the **fifth aspect**, and the three-state pilot moves *up* to the aspect level — which is also where *every* aspect will eventually need it, so it pilots the right mechanism instead of a one-off field.

Two consequences to decide: **(1)** `Aspects`/`TypeDefinition` gains `codecAspect`. **(2)** Aspect ⟺ head, so drake gains a `codec` head — my lean: append, `type → domain → rule → actor → codec`, since codec is the most cross-cutting/emitted concern and joins the optional tail. Recorded. Before I touch the foundational `Aspects`/`TypeDefinition`, I'd rather have your explicit go-ahead — and this is a deep thread we've been at a while; if you'd prefer to **commit the settled design first** and start the `codecAspect` implementation fresh, that's a clean seam.

---

**[Dev]** Let's do pause-and-commit and immediately start a new session afterwards.

**[Claude]** *(Checked git state.)* Good state to pause — the heavy work (Issue A/B, dead-factory) already landed in `e19f435`, so the only thing uncommitted is the new **`drake.dlt`** template (the interim `drake-grammar`/`drake-symbols` were staged earlier and are now superseded; `git add -A` cancels their never-committed staging, so they leave no trace). You author the `draco-git-record/` entry; *(provided the heredoc commit block: "drake.dlt: canonical DRAKE definition-language template … Design-only doc; no code or JSON behavior changes.")*

Handoff to the new session — everything from this session's design work is in memory, so it resumes cleanly. **Settled:** three-state aspect semantics (absent / present-empty / override) across all aspects, retiring field-elision; **codec is the fifth aspect** `codecAspect: CodecAspect { *encode, *decode }`. **First increment to implement:** author `CodecAspect.json`+`.scala`, wire `codecAspect` into `Aspects`/`TypeDefinition` with presence-aware (non-eliding) encoding, gate on `DracoGenTest` + round-trip — additive, a no-op until a type populates it. **One open decision to confirm first:** the `codec` head order — my lean is append.

*(Delegated action — Dev ran the commit block: `4d98de5` pushed to `main`, 2 files — `drake.dlt` + `git-record-2026-07-05-1625`.)*

Pushed clean — the interim `drake-grammar`/`drake-symbols` left no trace, exactly as intended. This is a tidy seam; the new session opens with the design settled and the `codecAspect` first-increment ready to implement. See you in the next one.

## Status
Full suite **200/200** at every gate. Committed: Issue A (Actor factory removal + `extensible` wiring + 3 Generator import fixes), Issue B (`*Definition` alias triad retired; `ActorType` now a marker trait), dead `RuleType` factory removal (`e19f435`); `drake.dlt` canonical template with type bounds (`4d98de5`). Settled but unimplemented: uniform three-state aspect semantics (retiring field-elision), codec as the fifth aspect (`codecAspect: CodecAspect { *encode, *decode }`). Queued: the `codecAspect` first increment (head-order confirmation pending — lean `type→domain→rule→actor→codec`), Phase 2a corpus normalization (~22 `.drake` files to the template), Phase 2b drake↔JSON reconciliation (actor `start/message/signal` vs `setupAction/…`; `par _` into JSON param names), derive-and-retire the TypeDefinition/Value codec blobs, and the true out-of-drake tail (`SourceContent`, `Primes`, `YAML` host algorithms). TBD-A/TBD-B: value↔name bindings in rule actions / the derived `values` field.
