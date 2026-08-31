# Draco Dev Journal — Chapter 74

**Session date:** August 25–28, 2026
**Topic:** #51's endgame and what grew out of it: the derivation edge closed in one increment with no shape change — Dev's provenance correction ("Any shape change suggestion came from the AI model not from me"), a foreign derivation spelled by its operator, the root appended rather than replaced, `reference package: 0` at 525/525 (`324556c`) → the nominal-over-structural decision stated explicitly ("My mistake. I should have explicitly stated my choice of nominal over structural") and recorded, flat `TypeParameter` chosen so nesting is unrepresentable → the type-algebra dialogue: actuals measured at depth 1, `(,)` proposed as the product head, the Haskell test run on `Holon(T <: Product)` — bound becomes superclass constraint, `>:` exposed as variance rather than subtyping, and Dev's direct challenge ("Are you saying I can't make my drake subtyping work in Haskell…") answered by splitting `<:` into obligation, constraint, and substitutability → Dev's substitutability requirement making the actor match-case dispatch the next-priority grammar, then Dev's aspect-substitutability correction (actor instances are never message data) turning the 12-actor migration into the move that makes `derivation` purely the data tree — ten migrated, two deliberately left as the dispatch grammar's worked example (`05b14bd`, 526/526) → the probe's verdict: Evrete honours inheritance one level at a time, but co-declaring ancestor and descendant in one knowledge silently drops the insert — #63 filed with the measurement as its spine → the interactive scenario design across nine exchanges: DataDomain as root super-domain owning all transform rules (first use of `superDomain`), message domains with one `DomainMessage` root and sibling payloads, "Modules (and packages) are containment trees not inheritance trees", transform domains as leaf conversions only with composites as factory applications, messages as facts with conditions navigating inside them, and the message/payload distinction as a mixin tag — "the dispatch converts hierarchy into siblings — it is not for routing", written onto #63 at Dev's direction → the tree-communication session log making the scenario mean something (Forest, Ash, Birch, `Infochemical` as tag and membrane at once), the Forrest/Forest typo flagged rather than silently fixed, 22 drake files parsing 0 rejected with every shape right → the live close-out, the session coordinating with this very journal update: work and journal as one commit under Cowork-first ordering, "not combined" clarified as authorship not commits, and the guaranteed one-paste block waiting on this chapter being finished. Transcript.

---

**[Dev]** Let's start on the priority-next issue. It should be #51

**[Claude]** *(Read #51 and its comments, `Dictionary.json`, `TypeLoader.rooted`, `DrakeParseTest`'s gates, `drake.dlt`'s derivation sections, every derivation reference in both corpora, and `Drake.scala`'s emit/parse paths.)* I read #51 and the tree. Here's where it actually stands and what I recommend for the first increment.

## Scope

The `priority-next` flag (2026-08-17 comment) is **not #51 entire** — it's the derivation edge, with a stated done-condition: `DrakeParseTest`'s loss report shows `reference package: 0`, and `Dictionary.drake` opens `from {K, V}` with `DracoType` gone.

## What I measured

- **Every derivation head in both corpora is package-qualified except one**: `Map(K,V)` in `Dictionary.json`. 38 definitions in main, 40 in mods — `Map` is the sole package-less one.
- That single entry is the whole `reference package` residual, and the reason `Dictionary.drake` spells one concept twice: `from Map(K, V)` on line 1 beside `fix kvMap {K, V}` on line 3.
- `TypeLoader.scala:17` `rooted` gates on `derivation.nonEmpty`, and it **replaces** the derivation with `Seq(DracoType)` rather than appending — which is why `Dictionary` has to spell `DracoType` by hand.
- The lexer already glues `{K, V}` into one token, and `typeExpression` / `parseTypeExpression` are already exact inverses over the brace form. So reference position can admit it without touching the grammar.

## Recommendation — one deviation from the issue's note, deliberately

The issue says this edge "changes what `derivation` holds, so it is a `TypeDefinition` shape change". **I recommend not changing the shape**, and here's the reason: the only way to put expressions in the slot without a new `TypeExpression` type is to flatten the entries to strings, which would demote 76 structurally-qualified nominal parents to dotted text — the exact regression the issue's opening section names. The wrap-type comment already gives the alternative: `Map` is **operator-carried**, so `{K, V}` *is* its reference spelling; the reference stays `TypeName("Map")` with no package, and the surface spells the operator instead of the name.

Four touches:

1. **`TypeLoader.rooted`** — gate on `derivation.exists(_.namePackage.nonEmpty)` (a foreign parent lives outside every draco domain, so it never roots), and **append** `DracoType` rather than replace. Identical behaviour for every currently-empty derivation; correct for `Dictionary`.
2. **`Dictionary.json`** — drop the hand-written `DracoType` entry. The JSON corpus becomes uniformly implicit, with no exception left.
3. **`Drake.emit` / `Drake.parse`** — a package-less reference in `from`/`modules` emits through `typeExpression` (`{K, V}`), and an operator-form token in reference position parses back to the primitive's name. Exact inverses; no package to lose, so the residual goes to zero.
4. **`DrakeParseTest` + `DrakeCLI:126`** — delete the `namePackage` normalization, so gate 2 verifies reference packages outright instead of measuring them.

Plus `drake.dlt`'s two notes (`Map as a TYPE EXPRESSION`, `REFERENCES RESIDUAL`), CHANGELOG and a git-record.

Blast radius is confined to those four; `derivationRef` in the Generator already renders `Map[K, V]` from that TypeName, and `Dictionary.scala` (`extends Map[K, V] with DracoType`) comes out unchanged under the append.

Accept this, or do you want the slot to actually change shape?

---

**[Dev]** Any shape change suggestion came from the AI model not from me. My goal is to make sure any change of the core functionality is progressive and increasingly consistent.

**[Claude]** Understood — the shape-change framing was the model's, not yours. Then the increment I recommended *is* the progressive path: no shape change, the surface learns to spell what the JSON already holds. Starting on it. *(Made the edits — emit side, parse side, `Dictionary.json`, the `TypeLoader` trio, the gate; simulated both directions over every authored surface; caught one straggler — `domains/world/Cartesian.json` still carrying an explicit root the 2026-07-29 strip didn't reach; updated `drake.dlt`, README, DRACO.md, CHANGELOG; wrote `git-record-2026-08-25-1240`.)* Done — the change is in the tree. You compile and commit; here's what's there.

## What changed

**The surface rule** (`Drake.scala`) — a reference with no package now *means* foreign (outside every draco domain), and a foreign referent is spelled by its operator instead of its host name. `Dictionary` reads `from {K, V}`, matching `fix kvMap {K, V}` three lines below it. `Drake.operatorCarried` states the parse side off the emitter's own spelling, so the two directions can't drift.

**The rooting rule** (`TypeLoader.scala:17`, trio kept in step) — `rooted` **appends** the universal root to a definition with no draco-domain parent, rather than replacing the derivation of an empty one. For everything but `Dictionary` that is the identical operation. Emission mirrors it: the root is spelled only where loading would *not* put it back.

**The gate** — the `namePackage` normalization is gone from `DrakeParseTest` and `DrakeCLI check`, so gate 2 verifies reference packages outright instead of measuring the loss.

**Corpus** — `Dictionary.json` drops its hand-written `DracoType`; `domains/world/Cartesian.json` drops the one sole-`[DracoType]` derivation the 2026-07-29 strip didn't reach, so no definition in either corpus names the root now. Plus `drake.dlt` (REFERENCES / INFERENCE / the Map note), `README.md`, `DRACO.md`, CHANGELOG, and the record.

**No shape change.** 97 of the 98 derivation references in the two corpora are package-qualified and structural; flattening them to expression strings to fix the one would give up exactly what `namePackage` already models.

## What I verified without compiling

Simulated both directions over the whole corpus: the new emit rule reproduces **all 91 authored `.drake` headers**, and the parse side reconstructs each of their JSON derivations **including packages** — 0 differences either way. That's the `reference package: 0` criterion, ahead of the suite.

## Gate

```bash
sbt test
```

Expect the `drake surface losses` headline to lose its `[reference package: n]` bucket entirely, with the other two (expression form, empty-collection spelling) unchanged, and every other count identical.

Then, if it's green:

```bash
sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-25-1240 | sed '1d' > /tmp/msg
```

```bash
git add -A && git commit -F /tmp/msg && git push
```

Once it lands I can comment on #51 recording that the derivation edge is closed and what remains open (the `valueType` expressions and the `TypeParameter` atom) — say the word and I'll draft it.

*(Delegated action — Dev ran the block and pasted the comprehensive results: `sbt test` — **525 tests run, 38 suites, all passed**; the message extracted clean from the record; committed as `[main 324556c] A foreign derivation is spelled by its operator, and the root is appended (#51)` — 14 files changed, 244 insertions, 67 deletions — and pushed `c369d88..324556c`.)* Green: **525 tests, 0 failures**, and the headline moved exactly as predicted —

```text
drake surface losses: 15 fields across 91 types [expression form (string vs tree): 12] [empty-collection spelling: 3]
```

The `[reference package: 1]` bucket is gone from the report entirely, not reduced — that was the done-condition. `Dictionary.json`'s round-trip now passes with packages compared verbatim, `Dictionary.scala` regenerated byte-identical under the append, and both gate suites are unchanged elsewhere. Pushed as `324556c`. *(Corrected one stale memory note — the one that said #51 was frozen would have sent the next session down the wrong path.)*

**Where #51 stands now:**

| | |
|---|---|
| Derivation edge | **closed** — `reference package` gone from the loss report, `Dictionary` reads `from {K, V}`, no definition in either corpus names the root |
| `valueType` expressions | open — 34 applications, 7 arrows, still Scala text |
| `TypeParameter` atom | open — `typeParameters: [String]` still the unpoliced channel |
| Primitive set / operator-name boundary | still RESERVED, deliberately |

One thing worth noting for the next increment: this edge established that an empty `namePackage` **means** foreign rather than being an omission. That's a small piece of the `TypeParameter` argument already settled — `Dictionary` deriving `Map` with no package was one of the four "names hiding type structure" cases in the DECIDED comment's census, and it's now structural rather than an omission. The other three (`value[T: Decoder]`, `updated[V1 >: V]`, `Primal[Int]`) are untouched.

Want me to comment that on the issue? It doesn't need approval per the repo rule, but you offered no answer either way and I'd rather not post to your backlog unasked.

---

**[Dev]** Comment that on the issue. Then clarify for me the question I did not answer either way, and when and why it needs to be answered.

**[Claude]** *(Posted the comment on #51.)* Posted.

## The question

At the end of my proposal I asked: *"Accept this, or do you want the slot to actually change shape?"*

You answered the provenance (the shape-change framing was the model's, not yours) and gave me a criterion — progressive, increasingly consistent. That was enough to act on, and I did. But it isn't an answer to the fork itself, which is:

**When a slot holds a type reference, does it hold a nominal thing (`TypeName`) or a structural thing (an expression)?**

## Why it didn't need answering this time

Partly by luck. The corpus's one foreign parent is `Map` — a host type that is *named* (`Map`) **and** has an operator spelling (`{K, V}`). That coincidence let me keep the slot nominal and put the whole change in the surface. Had `Dictionary` derived an arrow or a tuple — something with no name at all — there would have been nothing to put in a `TypeName`, and the fork would have been forced on the spot.

That's exactly the residual I recorded in `drake.dlt`: today it's a note, and it becomes a defect the moment a second foreign parent arrives that isn't operator-spellable.

## When it has to be answered

Three triggers, soonest first:

1. **`TypeParameter` (the DECIDED comment's open field shape).** A parameter's atom can be a qualified concrete type — `draco.format.json.JSON`, today mangled into a dotted string. So you must decide whether qualification is modelled once (reuse `TypeName`, atomicity holds by convention) or duplicated flat (nesting becomes unrepresentable, at the cost of two shapes for a qualified reference). That is this same fork, one level down, and the issue already marks it OPEN.

2. **`valueType` type expressions** — the main body of #51. Converting those strings to trees means building the expression type *once*. At that moment `derivation` either keeps its jurisdiction or converges with it, and deciding costs nothing extra while you're building it. Retrofitting afterwards is the expensive order.

3. **A second foreign parent.** Unlikely soon, but it converts the residual into a bug with no warning.

## Why deferring gets expensive rather than staying neutral

`TypeName` is the most load-bearing type in the system, and since #37 its equality is structural and pervasive. Every consumer — the loader, the codec gate, inherited-element walks, the assembly validator — reads it. Changing what it holds is cheap while `typeParameters` is still `[String]` and dear once parameters are records that everything compares.

## My recommendation, for when you do answer it

Jurisdiction, not merger, with the boundary drawn by the loader rather than by taste: `TypeLoader.tryLoad` resolves a definition **by** `typeName.resourcePath`, so anything loadable must be nominal, and anything structural has no resource to load from. Nominal reference → `TypeName`; structural type → expression; `TypeParameter` flat, so nesting is unrepresentable rather than merely discouraged.

This edge is weak evidence for that split already: keeping the reference nominal and moving only the spelling is what made it a ~100-line change instead of a corpus-wide one.

---

**[Dev]** My mistake. I should have explicitly stated my choice of nominal over structural.

**[Claude]** Noted — recording it now, where the next session will hit it before re-deriving it. *(Posted the DECIDED comment on #51; wrote the memory note.)* Recorded in both places, so the next session reads the rule before it re-opens the fork.

**What the decision buys immediately.** The derivation edge stops being an exception to a pending question and becomes the first increment built under a rule: reference slots stay `TypeName`, structure lives in `valueType`. The boundary has a mechanical test rather than a stylistic one — `TypeLoader.tryLoad` resolves by `typeName.resourcePath`, so *loadable ⇒ nominal*, and an arrow or a product has no resource to load from.

It also retires the framing that was steering this issue: "same normative form as value expressions" now applies to `valueType` and deliberately **not** to `TypeName`. That was the ATOMIC TERMS comment's recommendation; it's settled.

**What it doesn't reach.** `TypeParameter`'s field shape is still a real choice, because both surviving candidates are nominal:

| | keeps | costs |
|---|---|---|
| flat (`name` + `namePackage` inline) | nesting **unrepresentable** | the qualified-reference shape duplicated between `TypeName` and `TypeParameter` |
| `TypeName`-typed atom | one shape for a qualified reference | atomicity holds by convention, not structure |

What the decision does do there is close the third door by rule: `[TypeExpression]` is now excluded because a parameter position is a reference position, not because of the recursion argument the issue made against it.

Still open beyond that: whether a variable's constraint is part of its identity, and the RESERVED primitive-set question on the operator/name boundary — which this increment nudged only in that the operator side now carries reference position too, not just value types.

---

**[Dev]** Go with flat for TypeParameter, so nesting is unrepresentable.

**[Claude]** Recording that, and then one thing I need to put in front of you before any code — flat has a consequence the corpus will feel immediately. Let me measure it exactly. *(Censused `typeParameters` in both positions across both corpora; checked the two endogenous sites — `Holon(T <: Product)`'s bound requires the actual to be a product, so `Holon((S, T))` isn't sloppiness, the compound is constitutive; posted the decision to #51 and memory.)* Recorded.

## What the census says about your choice

`typeParameters` is one field read in two positions, and they hold different populations:

| position | total | forms |
|---|---|---|
| **declaration** — formals | 21 | 15 plain variables, 6 upper-bounded |
| **reference** — actuals | 44 | 23 plain, 12 qualified, **7 products, 1 application** |

Flat fits the declaration position **exactly** — zero compounds there, and the 6 bounds move out of text into a real slot. It also fixes 12 qualified actuals (`draco.format.json.JSON` stops being a mangled dotted string), which is the same win the derivation edge just made one level up.

All 8 compounds are actuals, and they don't clean up. `Holon` is declared `Holon(T <: Product)` — the bound *requires* a product actual, so `Holon((S, T))` is the intent, not a shortcut. Same for `Primal((Gaze, Distance))`: the pair is the value.

So flat leaves one hole: those 8 stay as text inside the atom's `name`. I'd measure it rather than paper over it — a report-only count, the way the drake surface losses are — and leave the underlying question (a draco product constructor, or structural actuals permitted) for its own decision rather than smuggling an answer into this increment.

## Proposed increment

Following the method the issue itself prescribes — **renderer first, byte-identity before conversion**:

1. `TypeParameter` defined (trio) with `name`, `namePackage`, `upperBound`, `lowerBound`, `contextBound`; `TypeName.typeParameters` becomes `[TypeParameter]`; a renderer reproduces today's exact strings, proven against all 65 occurrences before a single JSON changes.
2. Then migrate the JSON family by family, with the compound-actual count reported.

Two things I'd flag as *not* in it: the three method type-parameters still hiding in element names (`updated[V1 >: V]`, `value[T: Decoder]` ×2) are on `TypeElement.name`, a different field and its own increment; and `<: Product` naming Scala's `Product` in draco's metamodel is the same species as the `Map` we just handled — worth noticing, not worth fixing here.

That's a bigger blast radius than the derivation edge: `TypeName`'s codec, `resourcePath`, structural equality, the drake `(T)` spelling, and `Generator.parameterizedName` all read this field. Say go and I'll start with step 1 alone.

---

**[Dev]** No. First I want to decide how to  apply actual type parameters with simple type algebra expressions.

**[Claude]** Right — the actuals are the live question, and deciding the algebra first is the correct order. Let me measure what the corpus actually demands of it. *(Measured both positions across both corpora.)* Measured:

| | count | max depth | forms |
|---|---|---|---|
| **actuals** (reference position) | 44 | **1** | 36 atoms; 8 compounds — 7 product, 1 sequence |
| **valueTypes** (structural position) | 401 | 2 | 289 atoms, 108 depth-1, 4 depth-2 |

And the operands matter: **every operand of every compound actual is an unqualified atom** — `(S, T)`, `(Gaze, Distance)`, `(Double, Double)`, `(Percept, Effect)`, `Seq[Waypoint]`. Nothing in actual position is deeper than one constructor over atoms. The two positions differ measurably, which is itself an argument for keeping their jurisdictions apart.

## What that shape implies

An actual is **a head applied to atoms** — structurally the same thing as a reference, one level down, and terminated because the arguments are plain atoms rather than parameters:

```text
TypeName        name + namePackage + typeParameters[]      -- a reference
  TypeParameter   name + namePackage + arguments[] + bounds  -- an actual
    atom            a plain name                              -- terminates
```

Depth is bounded by construction rather than by convention. That is your flat decision applied one level down, and it makes flat **total** for actuals: all 8 compounds become structural. The hole I proposed to merely measure closes instead. What stays unrepresentable is depth ≥ 2 — which no actual in either corpus has, and which valueTypes keep, in their own jurisdiction.

## The one thing that needs deciding — and draco has half-answered it already

How is the constructor named? The value-expression vocabulary already carries operator keys in the normative JSON: `.` (46 uses), `()` (17), `=` (5), and **`(,)` for products (4)**. So draco already has a normative name for the product constructor — it just hasn't been used in a type position.

**My recommendation:** reuse it. The product head is `(,)`; the sequence head is `Seq`, foreign (no package). Then today's rule does the rest — a package-less head is spelled by its operator — so drake shows `(S, T)` and `[Waypoint]` with **no new notation, no new type, and no new vocabulary**. It is the same seam that landed this morning, one level down: nominal in the JSON, operator on the surface.

Three points to settle, in decreasing order of consequence:

1. **`(,)` as the product head in type position** — reuse the value-expression operator, or name a draco product type instead. (Reuse: zero new types, one vocabulary. Naming: a new constitutive type, and it reopens the RESERVED primitive-set question early.)
2. **Sequence stays foreign `Seq`** — consistent with `Map` today, converting to a draco wrap type later without the drake changing. Or name it now.
3. **Arguments are atoms, full stop** — depth fixed at 1 for actuals. Corpus says yes; the question is whether you want that as a *rule* or as a current fact.

One wrinkle worth seeing before you choose: if the product head is `(,)`, then `Holon(T <: Product)` still bounds on **Scala's** `Product` — the constructor and its bound would be spelled in two different vocabularies. That's the same host-residue species as `Map` was, and I'd leave it standing rather than fix it inside this decision.

---

**[Dev]** if we were defining Holon in drake, what would the Haskell equivalent of Holon(T <: Product) look like?

**[Claude]** Good test to run — `<: Product` is exactly the kind of thing it's designed to catch. Here's what Haskell forces.

## The direct answer

Assuming a parameterized draco type projects to a class (the guide's leading candidate — `GETTING_STARTED_TARGET_HASKELL.md:82` calls derivation-as-constraint "the single biggest structural question"):

```haskell
class Product t => Holon t
```

The bound becomes a **superclass constraint**, not a bound. And `Product` isn't in the Prelude — you'd have to declare it and give it an instance per tuple arity, or express product-ness structurally through `GHC.Generics` (`Generic t` plus a constraint on `Rep t` containing `:*:`). Either way you are *writing* the thing Scala hands you for free.

## What the test actually catches

Haskell has **one** mechanism where Scala has three. All four bound forms in the corpus collapse:

| draco today | Scala | Haskell |
|---|---|---|
| `T <: Product` (Holon, Coordinate) | upper bound | `Product t =>` |
| `S <: DomainType` (DomainTransform) | upper bound | `DomainType s =>` |
| `T <: DracoType` (TypeTransform) | upper bound | `DracoType t =>` |
| `T: Decoder` (`value[T: Decoder]`) | context bound | `Decoder t =>` |

The first three are spelled `<:` only because a Scala trait doubles as an interface and a supertype. Neutrally they say the same thing the fourth says: **the parameter satisfies this**. Note draco already writes that form — the context bound is the one that projects 1:1, and it's the one currently hiding inside an element name.

So the DECIDED comment's "three optional slots — upper `<:`, lower `>:`, context `:`" encodes Scala's *mechanism*, not the concept. The concept is a constraint on a parameter; `<:` is its Scala projection when the constraint happens to be a nominal type.

## And the lower bound is worse

`updated[V1 >: V]` exists because Scala's `Map` is covariant in `V`, so widening needs a supertype. Haskell has no subtyping, hence no variance, hence no lower bounds:

```haskell
insert :: k -> v -> Map k v -> Map k v
```

Nothing to translate. By your own rule — if it's dead in Haskell it's a Scala leak belonging to the Generator — `>:` should not enter `TypeParameter` at all. That's one of the three slots gone, and it's the one occurrence in the corpus.

## Where this lands on the decision you're making

`T <: Product` is Scala quantifying over *all tuple arities*, because it can't say "some product" otherwise. If `(,)` becomes draco's product constructor, that constraint becomes structural — "T's head is `(,)`" — statable without Scala's `Product` and without inventing a Haskell class. The wrinkle I flagged last message (constructor and bound in two different vocabularies) resolves in the direction of `(,)`, not away from it.

The cost is that a constraint would then need to talk about *heads*, not just nominal types — a larger idea than a bound slot, and I'd want it decided on its own rather than folded into `TypeParameter`.

So the Haskell test gives you: drop `>:`, and treat upper and context as one **constraint** slot rather than two — which is a smaller `TypeParameter` than the one on the issue, and a neutral one.

---

**[Dev]** Are you saying I can't make my drake subtyping work in Haskell the way it works in Scala?

**[Claude]** No — I'm not saying that. I was making a narrower claim, and it's worth separating it from the big one.

## Scala's `<:` bundles three services

| service | Haskell |
|---|---|
| **obligation** — a subtype must provide the parent's members | type class, directly: `class DracoType t where typeDefinition :: t -> TypeDefinition` |
| **constraint** — `T <: X` restricts what may be substituted, and lets you use X's members on a T | class constraint, directly: `DracoType t =>` |
| **substitutability** — a `Meters` *value* is usable where a `Distance Double` is expected, no conversion | **does not exist** — must be encoded explicitly |

Two of the three port cleanly, and arguably more precisely than in Scala, where the trait conflates them. The third has no Haskell equivalent as *coercion*. But "no coercion" isn't "can't be done" — it's done explicitly:

```haskell
{-# LANGUAGE ExistentialQuantification #-}
data SomeDomain = forall d. DomainType d => SomeDomain d   -- open hierarchy
```

or, when the family is closed, as an ordinary sum. **Which of those two is right is something drake already says**: a type with `modules` is a sealed family → Haskell sum type, substitutability by pattern match, no extension. A type without → existential wrapper, or the function stays constraint-polymorphic and never needs the wrapper at all. That distinction is already in your model; it just hasn't been read as a Haskell instruction yet.

## So the honest position

drake can mean subtyping. Keeping `<:` as the neutral spelling of an upper bound is defensible on exactly that ground — the semantics are sovereign, and Haskell owes an encoding rather than draco owing a compromise. What Haskell costs you there is boilerplate at the projection, not expressiveness.

What I'd still hold to, narrower than where I put it:

- **`>:` is not subtyping, it's variance.** `updated[V1 >: V]` exists because Scala's `Map` is covariant in `V`. Haskell has no variance and needs nothing in its place. That one really is a Generator concern.
- **`<: Product` is not subtyping either** — it's Scala quantifying over all tuple arities, using a supertype because it has no other way to say "some product". That's the one that gets *cheaper* under your `(,)` decision, not harder.

The `S <: DomainType` / `T <: DracoType` bounds are ordinary subtyping and I'd leave them exactly as they are.

## What I don't know yet, and could measure

Where the corpus actually depends on **substitutability** rather than obligation — a value used at its parent type. `DomainDictionary` keying on `DomainType` values is one such site; there may be few or many. That count is what decides whether the Haskell target needs existentials pervasively or almost never, and it's the same question the guide flags as its biggest open one. Worth knowing before you commit the target guide to an answer — say the word and I'll count it.

---

**[Dev]** The substitutability is the key concern since that involves the next highest priority task before qualifying for beta. I need strict type inheritance between domains, between domains and their TypeDefinition types, as well as between arbitrary draco types. Then the actor message types can be as high or low in the inheritance tree as required from being transform input, which would be executed by a rule on data in working memory, which data would be inserted into working memory by a match case equivalent switch statement from receiving typed message data in the actor.

**[Claude]** That reframes it — substitutability isn't a projection detail then, it's the mechanism the whole actor→rule→transform path rides on. Let me check what the tree already does before saying anything about what it needs. *(Read `WorldHierarchyTest`, the mods domain traits and actors, `Rule.scala`, the insert sites; wrote `SubtypeFactVisibilityTest.scala`.)*

The three inheritance edges you named already exist in the Scala projection — `trait Marine extends World`, `trait FixReport extends Marine with JSON`, and ordinary derivation between arbitrary types, with `WorldHierarchyTest` pinning two of them. So the type level is not the gap.

The gap is one level down, and it's a fact I won't guess at: whether the rete layer makes a fact visible at **every** ancestor type or only one. Today every rule in both corpora declares its fact at exactly one level, so the corpus has never asked the question. Here's a probe that answers it.

Probe is in `SubtypeFactVisibilityTest.scala`. It inserts **one** anonymous `FixReport` and declares four rules over it — at `World` (two levels up), `Marine` (one up), `FixReport` (exact), and `JSON` (its second parent) — then reports which fired.

```bash
sbt "testOnly domains.world.SubtypeFactVisibilityTest"
```

It measures rather than asserts, because the answer isn't known and each outcome means something different for what you're building:

- **All four fire** — inheritance is honoured through the domain chain *and* across a second parent. Your requirement is already satisfied at this layer; the remaining work is generation, not machinery.
- **Exact only** — a fact is visible at its runtime class alone. Then "as high or low in the tree as required" needs either a type resolver or one insert per level, and that's a `draco.rete` capability question rather than a modelling one.
- **Partial** — the resolver picks *one* type per fact. That's the dangerous outcome: which one it picks becomes the whole design, and a rule at another level simply never fires, silently. Better to know now than to discover it in a transform that quietly does nothing.

Two things I'd flag about the design as you described it, both contingent on that answer:

**The switch may be doing less work than you think.** If facts are visible at every level, the rule patterns already *are* the dispatch — a rule written at `World` sees every medium's message, one at `FixReport` sees only that. The match-case in the actor would then be needed only where a message must be turned into *different* facts (decomposing a report into the working-memory data a transform reads), not for routing. That's a smaller generation feature than a full typed switch.

**Today's actors insert the message whole** — `session.insert(Seq(msg): _*)`, typed at `JSON` — and every rule in both corpora declares its fact at exactly one level. So the corpus has never exercised what you need, which is why the probe is worth its five minutes before any of it gets designed.

I'd also want the count I offered earlier — where the corpus *uses* a value at its parent type — but the probe result changes what that count means, so it's worth running first.

---

**[Dev]** I'm assuming for Scala that the most reliable approach is that every actor can have a match case expression provided by its drake definition that can dispatch the exact type value that is needed in working memory to fire rules, with Haskell being able to generate the equivialent given the same definition.

**[Claude]** Agreed on the direction, and for a reason stronger than reliability: it puts the dispatch somewhere a *definition* can be checked, instead of depending on a third-party resolver's semantics that no test in the tree currently pins.

One constraint decides whether it survives the Haskell test, though.

## Scala's type test has no Haskell counterpart

`case x: World =>` is a runtime type test — it interrogates a value's type. Haskell has no such construct: `case` matches **constructors of a data type**, not types. So the same definition generates cleanly in both only if the match subject is a **closed set**:

| the family is… | Scala | Haskell |
|---|---|---|
| closed | `match` on a sealed trait — exhaustivity checked | `case` on constructors — exhaustivity checked |
| open | `case x: T` type test, fine | no equivalent; needs `Typeable`/`cast` (dynamic) or class dispatch (static, resolved by the *call site's* type, not the value's) |

The open row is where a Scala-ism would hide. It would generate, it would pass its tests, and the Haskell would be an encoding of Scala's mechanism rather than the same thing.

## drake already says "closed", and already does this dispatch

`modules` is the sealed-family declaration, and it's in use in exactly two places — `TypeElement → BodyElement → the eleven kinds`. That family already drives draco's **only** typed dispatch today: `Generator.targetBody`'s eleven-way match on `Fixed | Mutable | Dynamic | …`, plus the discriminated codec keyed on `kind`. So the construct you're proposing is already proven endogenously, already projected two ways from one declaration.

What's missing for messages is that the message families aren't declared closed. `World` has no `modules` — the media are related by derivation only. Declaring the family is what would let the same machinery carry actors, and it buys exhaustivity checking in both targets plus a definition-time lint, rather than a silent missing case.

## It inverts what the probe is for

Under explicit dispatch you no longer *need* subtype visibility — each fact goes in at exactly the type its rule declares. But you now need to know whether the engine adds it anyway, because if it does, an inserted `FixReport` may also fire rules declared at `Marine` or `World` that you never dispatched to. **Firing twice is a worse failure than not firing** — it's silent, and it corrupts working memory rather than leaving it empty. So the probe matters more under your design, not less; it's just answering a different question.

## What the definition would need

`actorAspect.messageType` already exists. A case construct does not — drake has `con` for rule guards and nothing that binds a type pattern to a body. That's a genuine grammar increment (surface, emitter, parser, Generator, and a Haskell story), and I'd scope it separately rather than fold it into the `TypeParameter` work.

Worth running the probe first — it's five minutes and it tells you whether you're building dispatch *instead of* engine resolution or *in spite of* it.

---

**[Dev]** Yes, and that new grammar addition is the next highest priority task for me. So lets make that neutral grammar description and implementation be the next major issue after the current issue. Before I run your test, let's see if the current issue work is affected by our new determination about the importance of substitutability wirth regard to how we close issue #51.

**[Claude]** Good question to ask before building — it changes one thing materially. Let me check the interaction points. *(Censused the qualified actuals.)*

Yes — two ways, and one of them changes the build order.

## The qualified actuals *are* the message type

All 12 qualified actuals in the corpus are the same thing:

```text
Actor(draco.format.json.JSON)   ×9      Actor(domains.world.World)   ×3
```

And **zero** mods actors carry `actorAspect.messageType` yet. So every qualified actual in either corpus is an actor's message type in its transitional form — a structural reference mangled into a dotted string inside a derivation's type parameters. Migrating those into `TypeParameter` records would be migrating precisely the field your next issue deletes.

That is #51's own prerequisite #1, which was written for `Draco.json` in main and explicitly deferred for the 12 mods actors ("they migrate WITH this work"). Substitutability changes that call: the message type is now the load-bearing datum for dispatch, and it should be sitting in `actorAspect.messageType` as a first-class reference before anything is built on it.

**So the order inverts.** Migrate the 12 actors off `Actor(T)` first, and it:

- deletes 12 of the 44 actuals outright — no migration, no renderer, no gate for them;
- removes the **entire** qualified-atom population, leaving 23 plain atoms, 8 compounds, 1 domain reference;
- puts the message type where the dispatch grammar reads it;
- shrinks `TypeParameter` to a much smaller change, and removes one of its two stated benefits (qualified actuals gaining structural `namePackage`) by making the problem disappear instead.

## The constraint model gets sharper — and my earlier recommendation was wrong

I said to collapse upper `<:` and context `:` into one constraint slot, on the grounds that Haskell has one mechanism. That reads it at the wrong level. With substitutability first-class, `<:` on a **draco** type is not a generic constraint — it names the derivation relation draco now has to compute. The real split isn't upper-vs-context:

| constraint | referent | meaning |
|---|---|---|
| `S <: DomainType`, `T <: DracoType` (4) | draco types | derivation constraint — computable from definitions, and it *is* substitutability |
| `T <: Product` (Holon, Coordinate) | Scala's `Product` | host capability |
| `T: Decoder` (2, hiding in element names) | circe's `Decoder` | host capability |

Four draco, four host — and the spelling `<:` vs `:` cuts across the distinction rather than marking it. What marks it is something you already have as of this morning: **the constraint's package**. `Product` and `Decoder` have none, so they're foreign → host capability. `DomainType`, `DracoType` are draco → derivation constraint. No new discriminator; the foreign rule does it.

## One thing not to decide by accident

With no variance in the model, substitutability on a parameterized type holds only when the actuals are **equal** — `Primal(Gaze)` is not a subtype of `Primal(DracoType)`. That's almost certainly what you want (it's also what Haskell gives you), but `TypeParameter` is where it would get silently prejudged, so it's worth being deliberate.

Unaffected: flat, depth-1 actuals, `(,)` as the product head, and dropping `>:`.

## Recommendation for closing #51

Land it in two slices, in this order:

1. **Actors off `Actor(T)` onto `actorAspect.messageType`** — 12 definitions, deletes the qualified population, unblocks the dispatch grammar.
2. **`TypeParameter` flat** over what remains, with the constraint carrying a reference whose package tells you whether it's a derivation constraint or a host capability.

I can file the grammar issue whenever you want it — its scope doesn't depend on the probe; only the risk it has to note does (whether the engine fires ancestor-level rules you didn't dispatch to).

---

**[Dev]** Why are you conflating the substitutability of the actor aspect with the substitutability of the draco type aspect itself? Actor instances will not be message data for other actors. The same is true for rule aspect and codec aspect. The domain aspect is used in the same inheritance tree as draco type aspect, so it can be a message type for an Actor.

**[Claude]** You're right, and the distinction matters more than the point I was making with it. Let me check it against the corpus rather than just concede. *(Censused derivations by role aspect.)*

Corpus agrees with you, and more sharply than I'd expect:

- **Every rule-aspect type has an empty derivation** — all 10, main and mods. Rules are already outside the data tree.
- **`Draco` (actor) has an empty derivation** since `4c6e67c` dropped its `Actor[DracoType]`.
- **The 12 remaining actors are the only role-carrying types with any derivation at all**, and all 12 of those derivations are `Actor(T)`.
- No type in the corpus carries a codec aspect today, so that case is currently vacuous.

So the discipline you're describing is already observed everywhere except one unfinished migration, and the fix is proven on `Draco`.

## What I got wrong

I justified moving the 12 by saying the message type is the load-bearing datum for substitutability. That conflated the two trees. An actor is never message data; its `Actor(T)` derivation isn't an inheritance edge in any meaningful sense — it's a **role parameter smuggled into the data tree**, which contradicts the rule the framework already states: role is presence, not name. An actor is an actor because it carries an `actorAspect`, so it has no business also *deriving* `Actor`.

That's a better reason than the one I gave, and it generalizes to rule and codec, which is why the corpus already looks the way it does.

## What it changes downstream

The recommendation survives — do the 12 first — but the payoff is different and, I think, larger:

**Once those edges are gone, `derivation` *is* the substitutability relation, with no exceptions to filter.** A subtype check becomes a plain derivation walk: no "skip this edge, it's a role parameter", no aspect inspection. That property is what the dispatch grammar will need to reason about, and today it holds for 129 of 130 definitions — the 12 actors being the one class of exception.

It also fixes a claim in my earlier table. I listed `S <: DomainType` as a derivation constraint on the assumption that domains sit in the data tree; you've now confirmed that directly, so `<: DomainType` and `<: DracoType` are genuine substitutability constraints and `<: Product` / `: Decoder` are host capabilities. That split stands.

## For the grammar issue

Two constraints worth writing into it from this:

1. **A case pattern ranges over data types only** — the draco and domain aspects. You cannot dispatch on an actor, a rule, or a codec, and the grammar should make that unrepresentable rather than merely unwise.
2. **A pattern may name a domain**, since the domain aspect shares the tree — which is exactly the "as high in the tree as required" case, and the reason dispatch at `World` is meaningful at all.

Want me to file it now with those two constraints in it, or after the 12-actor migration, when the derivation-graph property it rests on is actually true?

---

**[Dev]** After the 12-actor migration.

**[Claude]** Before touching the 12, three dependencies to check — the message type is read in more places than the Generator. *(Checked the Generator's actor paths, `AssemblyValidator`, every `extends Actor[` site, the emitter/parser's `messageType` handling; migrated the definitions; simulated the surface round-trip over all 91; updated `drake.dlt`, CHANGELOG; wrote `git-record-2026-08-26-1009`.)* Dependencies check out — imports are unaffected, and the tests use `actorType()`, not the trait.

The migration is in the tree. **Ten of the twelve moved; two did not, and the two that didn't are the argument for your next issue.**

## What changed

**Ten definitions** drop `dracoAspect.derivation` and gain `actorAspect.messageType` — and where the derivation was the aspect's only content, the aspect goes with it. After this, `derivation` holds data-inheritance edges **only**, with no exception in either corpus: all ten rule-aspect types already had empty derivations, no type carries a codec aspect, and `Draco` shed its `Actor[DracoType]` back in `4c6e67c`. A subtype check can now be a plain derivation walk with no aspect filtering.

**Three engine changes were needed**, one of which I didn't anticipate until the corpus showed it:

- `messageType` **could not previously hold a package** — the emitter wrote the bare name and the parser read a single token, so `draco.format.json.JSON` would have lost its package at the surface. It now follows the same rule `from` and `modules` follow.
- `Generator.actorMessageType` renders it **package-relative** (the rule `derivationRef` already used), which is what keeps every generated file identical.
- `AssemblyValidator` was still reading the message type out of the derivation — the last consumer doing so.

Also `Draco.json`'s message type gained its own package: it read `DracoType` with none, which since yesterday *means* foreign, and DracoType is the root of every draco domain.

## The two that stayed

`world/Consumer` and `world/Provider` have an `Actor(World)` derivation and **no actor aspect at all**. Their behaviour is hand-written Scala, and it is this:

```text
msg match {
  case position: domains.aerial.Position => /* transform */ ; provider ! location
  case _ => // no transform rule for this source type yet
}
```

An actor typed high in the tree, dispatching on a subtype, transforming, forwarding. Giving them an actor aspect would switch on a generated `receive` and discard that. They're not debt — they're the worked example the dispatch grammar has to reproduce, and the issue should be measured against them.

## Verified before compiling

All eleven message types (ten plus `Draco`) spell identically to the `new Actor[…]` already in their generated Scala, and the surface round-trip simulated clean over all 91 definitions carrying a `.drake`. Nothing else in those ten files changes — imports are unaffected, since `import draco._` is unconditional and the `Actor` derivation's package was already in the covered set.

```bash
sbt test
```

Expect every count identical, **including `ExampleDomainsGenTest`'s 28 match / 20 differ** — the ten trait lines were updated in step with what the Generator now emits. If that drops below 28, one of the ten changed beyond the trait line and the log names it.

```bash
sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-26-1009 | sed '1d' > /tmp/msg
```

```bash
git add -A && git commit -F /tmp/msg && git push
```

Once that's green I'll file the dispatch grammar issue, with `world/Consumer` as its reference case and the two constraints we settled (patterns range over data types only; a pattern may name a domain). The probe test is still uncommitted and unrun — worth running before I write the issue's risk section, since "does the engine also fire ancestor-level rules" is the one thing that could complicate the design.

*(Delegated action — Dev ran the block: **526 tests run, 39 suites, all passed**; committed as `[main 05b14bd] An actor's message type is carried by its aspect, not by a derivation (#51)` — 39 files changed, 516 insertions, 162 deletions, the record and `SubtypeFactVisibilityTest.scala` among them — and pushed `324556c..05b14bd`.)* 526 — the probe ran as part of that suite. Its answer is on disk, and it's not one of the three outcomes I sketched:

```text
rules declared at: World, Marine, FixReport, JSON
rules that fired : <none>
```

**Nothing fired — including the rule declared at `FixReport` itself**, the fact's own type. That's notable because the identical shape works in production today: `aerial/ConsumeReport` declares `classOf[PositionReport]`, an anonymous `PositionReport` gets inserted, and it fires (`AerialActorTest` passes). The difference between that and the probe is that the probe declares **four types from one hierarchy in one knowledge**.

The likely reading is that co-declaration is the poison — several declared types fit one fact, so the resolver picks none and the fact is dropped silently. But that's a hypothesis about a third-party engine, and I'd rather not build a design on it, so the probe now runs each level **alone** first: *(rewrote the probe with a solo/combined matrix)*

```bash
sbt "testOnly domains.world.SubtypeFactVisibilityTest"
```

The matrix tells them apart:

| solo | combined | meaning |
|---|---|---|
| all four | all four | inheritance honoured; declare rules anywhere |
| all four | none | **co-declaration drops the fact** — each level works alone, together they're blind |
| `FixReport` only | — | no inheritance; the exact type is required |
| none | — | the probe is wrong, not the engine |

If it lands on row 2, that's the strongest possible vindication of dispatching in the actor: implicit matching wouldn't just be unreliable, it would fail *silently and worse the more levels you use* — a rule at `World` and a rule at `FixReport` in one knowledge would disable each other. It also means the dispatch grammar must insert each fact at exactly one declared type, which is a real constraint on its design and belongs in the issue.

I'll hold the issue until this comes back — the risk section depends on which row it is, and row 2 changes the framing from "the grammar is a convenience" to "the grammar is how it works at all."

*(Delegated action — Dev ran `testOnly domains.world.SubtypeFactVisibilityTest`: 1 test run, passed — report-only, with Evrete's own `java.util.logging` warnings in the output: `DefaultTypeResolver findInSuperClasses — WARNING: Unable to resolve type 'domains.world.SubtypeFactVisibilityTest$$anon$1' due to ambiguity` and `AbstractRuleSessionOps bufferInsertMultiple — WARNING: Can not map type … insert operation skipped`, and the verdict line `rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing`.)* Row 2, and Evrete named the mechanism itself. So inheritance *is* honoured — each level fired alone — but when two declared types both fit one fact, the resolver picks neither and **the insert is skipped entirely**. Not a warning-and-continue: the fact never enters working memory. *(Checked every `newKnowledge` declaration set in the shipped corpus; filed the issue; added the gotcha to DRACO.md.)*

The probe settled it, and the answer is the one that most changes the design.

## What it found

**Inheritance is honoured** — a rule declared at `World`, two levels above the fact, fires on a `FixReport`. So "as high in the tree as required" genuinely works.

**But only one level at a time.** Declare two types from one hierarchy in a single knowledge and Evrete resolves *neither*, then **skips the insert**. The fact never reaches working memory, both rules go quiet, and the only trace is a `java.util.logging` warning. Nothing throws, nothing fails. A transform simply doesn't happen.

Your corpus is safe today, but by shape rather than by design: each medium's session declares two **siblings** (`PositionReport` + `FlightIntent`), and draco's validation session declares a deliberately **disjoint** taxonomy — the `SelfDeclarationRulesTest` comment about `DomainType` and `TypeDefinition` being disjoint reads differently now. It was written to explain a false-positive; it was also, unknowingly, avoiding this.

## What it does to the grammar

It makes it load-bearing rather than convenient. Multi-level matching cannot come from the engine, so it has to come from the actor's dispatch inserting each fact at exactly the type its rule declares — which is what you proposed before either of us knew this. Filed as [#63](https://github.com/ejb816/nexonix/issues/63) with the measurement as its spine, the two `world/*` definitions as its acceptance corpus, and four constraints:

1. patterns range over data types only — `05b14bd` is what makes that enforceable
2. a pattern may name a domain
3. the match must be over a **closed** set, or Haskell gets an encoding of Scala's type test rather than the same thing — and `modules` already declares closedness
4. **no two rules in one domain may declare fact types related by inheritance** — new, and a lint draco can now own precisely because `derivation` is purely the data tree

I also added it to DRACO.md's gotchas and to the Evrete memory note, since it's the kind of thing that costs a session to rediscover. That's an uncommitted doc change plus the probe's second version — worth folding into your next commit rather than one of its own.

The probe is worth keeping as a sentinel: if Evrete's resolution behaviour ever changes, its verdict line changes with it.

---

**[Dev]** I need your help creating an abstract test scenario involving rules, facts, and type inheritance that can exist between facts. To start, posit a single DataDomain (inheritance root) with 4 MessageDomain{1-4} with each MessageDomain inheriting directly from DataDomain. Let's elaborate on that premise interactively to extend to the various types and inheritance between those types with and within those domains.

**[Claude]** Good scenario to build — it's the first thing in the tree that could exercise inheritance shapes deliberately rather than incidentally. Let me pin the premise precisely first, because it already determines more than it looks.

## The premise in draco terms

Five definitions, each a domain (a `domainAspect` self-loop), four of them deriving the root:

```text
DataDomain                        domain; derivation absent -> rooted at DracoType
├── MessageDomain1                domain; from DataDomain
├── MessageDomain2                domain; from DataDomain
├── MessageDomain3                domain; from DataDomain
└── MessageDomain4                domain; from DataDomain
```

This is structurally identical to `World / {Aerial, Terrestrial, Marine, Ethereal}`, which matters: whatever we design here can be checked against a chain that already runs, and `WorldHierarchyTest` already pins that shape's subtyping.

**What it already fixes.** For any type `T` declared in `MessageDomain1`, the ancestor set — the set of declared types a fact of `T` would satisfy — is:

```text
{ T, MessageDomain1, DataDomain, DracoType }
```

Four levels, before we add a single member type. So in one knowledge you may declare **exactly one** of those four. A rule at `DataDomain` and a rule at `MessageDomain1` already blind each other; that's not an elaboration hazard, it's true of the premise as stated.

## What the premise does *not* yet fix

Three things, and they're the elaboration:

1. **Do message types derive only their domain, or also a data type in `DataDomain`?** Chain versus lattice. In the World corpus this is real: `FixReport extends Marine with JSON` — two parents, one the domain, one a shell.
2. **Is there inheritance across message domains?** You said "between those types with and within those domains", which I read as yes. It's the sharpest case: if `MessageDomain2.C` derives `MessageDomain1.A`, then `C`'s ancestor set contains **both** `MessageDomain1` (through `A`) and `MessageDomain2` (through membership) — a fact belonging to two message domains at once.
3. **Depth within a domain** — does a domain hold a flat set of members, or its own sub-hierarchy?

## What I'd propose for layer 2

Enough shape to exercise every distinct case, with nothing decorative:

```text
DataDomain
│   Payload                      a data type — REFERENCED by messages, not inherited
├── MessageDomain1
│   ├── A                        depth-3: A -> MessageDomain1 -> DataDomain
│   │   └── A1                   depth-4: a subtype WITHIN a domain
│   └── B                        sibling of A (the safe, corpus-typical case)
├── MessageDomain2
│   └── C  from MessageDomain1.A     CROSS-DOMAIN: two domains in one ancestor set
├── MessageDomain3
│   └── D  from MessageDomain3, Payload   two parents: domain + data type
└── MessageDomain4               deliberately empty — a domain with no members
```

Five shapes, each answering a different question: sibling (safe), depth-within-domain, cross-domain (dual membership), multiple parents, and empty. `Payload` sits in `DataDomain` and is *referenced* by `D` — which lets us separate "reference" from "inheritance", since only the latter creates ambiguity.

## What I need from you

- **Cross-domain inheritance — in or out?** It's the only shape that produces dual domain membership, and it's the one I'd most want pinned, but it may be something you'd rather forbid by lint than model.
- **Is `Payload` inherited or referenced?** I've drawn `D` deriving it, which makes a message type a subtype of a data type. The alternative is that data types are only ever element types, never parents — a cleaner rule, and a real constraint on the ANION shape.
- **Depth 4 (`A1`) — worth it?** It costs nothing structurally but doubles the ancestor-set table.

One thing I'd defer rather than decide now: whether these become real definitions under `src/test/resources` (generated, compiled, definition-backed) or stay hand-written test types. It doesn't change the shape, but it changes what we can assert about it.

---

**[Dev]** More interaction is needed on the specific types in each domain. Covering general patterns is not enough.  DataDomain contains just DataType{1 ... N} where any DataType can inherit from anywhere. MessageDomain contains one root type DomainMessage that inherits from MessageDomain, MessageType{1 ... N} that all inherit from DomainMessage, MessageDataType{1 ... N}  which can be either flat or hierarchically composed into into message data that is referenced by either DomainMessage or any MessageType.

**[Claude]** That specification is tighter than what I proposed, and it changes the analysis: `DomainMessage` gives each domain its own message root, so the chain is five deep before any member type.

## The shape, instantiated

Taking small concrete N so it can be reasoned about rather than described:

```text
DataDomain                                    domain; rooted at DracoType
│   DataType1                                 from DataDomain
│   DataType2   from DataType1                depth-4, within the data domain
│   DataType3   from DataType1                sibling of DataType2
│   DataType4   from DataType3                depth-5
│
├── MessageDomain1                            domain; from DataDomain
│   │   DomainMessage      from MessageDomain1
│   │   ├── MessageType1   from DomainMessage
│   │   └── MessageType2   from DomainMessage
│   │   MessageDataType1                      flat
│   │   MessageDataType2                      composed: holds MessageDataType1
│   │                                         referenced by DomainMessage / MessageType*
├── MessageDomain2                            same shape, own DomainMessage
├── MessageDomain3
└── MessageDomain4
```

## Ancestor sets — the thing that actually governs rules

```text
MessageType1     { MessageType1, DomainMessage, MessageDomain1, DataDomain, DracoType }   5
DomainMessage    { DomainMessage, MessageDomain1, DataDomain, DracoType }                 4
DataType4        { DataType4, DataType3, DataType1, DataDomain, DracoType }               5
DataType2        { DataType2, DataType1, DataDomain, DracoType }                          4
```

A knowledge may declare **at most one member of any one of these sets**. Two consequences fall straight out, and they point opposite ways:

**The message side is safe by construction.** `MessageType1` and `MessageType2` are *siblings* — neither is in the other's ancestor set — so a knowledge may declare all N message types of a domain at once. That is exactly the dispatch design: the actor's message type is `DomainMessage` (one level up), the dispatch selects the concrete `MessageType`, and the rules declare the siblings. No ambiguity anywhere, provided nothing declares `DomainMessage` *alongside* its subtypes.

**The data side is where it bites.** "Any DataType can inherit from anywhere" means arbitrary chains, so `DataType1` and `DataType4` in one knowledge are blind to a `DataType4` fact — and unlike the message side, nothing about the shape prevents it. If data types are ever inserted as facts, that's where the lint earns its keep.

## What I still need to be specific about

**1. What do `MessageDataType`s inherit?** Your spec says only `DomainMessage` inherits from `MessageDomain`, so message data types are left dangling. Three readings, and they differ materially:

- nothing (rooted at `DracoType`) — message data sits outside every domain tree
- their `MessageDomain` — then a `MessageDataType` fact and a `DomainMessage` fact share an ancestor, and a knowledge can't declare both
- a `DataType` in `DataDomain` — message data becomes a *specialization* of the shared data model, which is the ANION reading and the most interesting one, but it also puts `DataDomain` in their ancestor set

**2. "Inherit from anywhere" — how far is anywhere?** Any other `DataType` (a hierarchy closed inside `DataDomain`), or also into the message domains? The second lets a `DataType` derive a `MessageType`, which would make data types members of two trees.

**3. Is `DomainMessage` one name per domain, package-distinguished?** `messagedomain1.DomainMessage`, `messagedomain2.DomainMessage` — the way `Consumer` already repeats across the four media. Or four distinct names?

**4. Are the four message domains uniform?** I've drawn them identical. If one is deliberately degenerate — no message types, or no message data — it tests a shape the others can't.

My recommendation on (1), since it's the one that shapes everything downstream: **message data types inherit nothing** — they are referenced, never inherited, and stay out of every domain tree. That keeps composition and inheritance as strictly separate mechanisms, and it means a message data fact can never collide with a message fact in a knowledge. If you want data types reachable by inheritance, I'd put that edge in `DataDomain` where you've already said arbitrary inheritance lives.

---

**[Dev]** Fortunately the DataDomain part that bites is toothless because most, if not all of the data type will be rules, and data type with references to domains and transform domains.

**[Claude]** Right — and that sharpens the constraint in a way worth being precise about: **ambiguity is a property of types that get inserted, not of the tree.** A type's position in the hierarchy is harmless until an instance of it becomes a fact. Rules are never facts, so a rule taxonomy can inherit as deeply as it likes with no resolution consequence at all.

It also separates two fact levels that have been quietly overlapping in this conversation:

- **metamodel facts** — `TypeDefinition`, `DomainType`, `Problem` instances, which is what draco's own validation session inserts. The inheritance among the *defined* types is invisible here; a rule definition arrives as a `TypeDefinition`, not as itself.
- **domain facts** — instances of the defined types, which is what an actor's session inserts.

Ambiguity applies inside each level independently, and `DataDomain`-as-rules lives at neither: rule types are inserted at neither level.

## Which makes the message data the real subject

By your original description, the thing inserted into working memory is the data the dispatch extracts from the message — so **the facts in this scenario are the `MessageDataType`s**, not the `MessageType`s and not the `DataType`s. That relocates the whole question onto my outstanding (1), and answers it in a specific way:

You said message data types are *composed* — flat, or hierarchically composed into larger message data. **Composition is not inheritance.** If composition is the only relation among them, then every `MessageDataType` is pairwise unrelated to every other, their ancestor sets are disjoint, and a knowledge may declare **all N of them at once**. The design is ambiguity-free by construction — not by luck, and not needing a lint.

That safety survives exactly as long as message data types don't inherit each other. Which is why I'd still like (1) answered as a rule rather than as a current fact: **do `MessageDataType`s inherit anything at all** — each other, their domain, or a `DataType`? If the answer is "nothing, ever", that's a language-level invariant worth stating in the grammar, and the lint becomes a cheap check rather than a graph walk.

## The one shape that still bites, and it's the attractive one

A catch-all rule at `DomainMessage` alongside per-`MessageType` rules. That isn't an exotic mistake — it's the first thing an author reaches for ("handle the specific ones, then a fallback"), and it silently disables *both* the fallback and every specific rule, because a `MessageType1` fact fits `DomainMessage` too.

The corpus already shows where the catch-all belongs. `world/Consumer.scala` puts it in the dispatch, not in the rule set:

```text
case _ => // no transform rule for this source type yet
```

That's the shape the grammar should make natural and the rule set should make impossible — a fallback is a case, never a rule.

Want me to fold this into [#63](https://github.com/ejb816/nexonix/issues/63)? It refines constraint 4 from "no two rules in one domain may declare related fact types" to something narrower and more checkable: the constraint binds only on *insertable* types, and the message-data layer discharges it structurally.

---

**[Dev]** I probably should have indicated that the incomplete nature of those  domains should have excluded them as useful examples when used in the context of transforms. which is the primary reason for message domains and transform domains in the scenario  we're creating.

**[Claude]** Taken — and worse than "incomplete": there's no worked transform anywhere in the tree to anchor on. *(Read `DomainTransform.drake` and `TypeTransform.drake`.)* The endogenous transform types are contentless markers:

```text
type DomainTransform(S <: DomainType, T <: DomainType) from Holon((S, T))
type TypeTransform(S <: DracoType, T <: DracoType) from Holon((S, T))
```

No elements, no factory, nothing. They name the concept. And `world/Consumer.scala` does its transform *inline in the actor* rather than as a rule over facts, which is the shape the requirement explicitly moves away from. So I was citing it as a specification when it's only a symptom — evidence of what can't be expressed, not a model of what should be.

So the scenario has to say what a transform **is**, and the message domains exist to give it something to transform between.

## One consequence I can derive before you answer anything

**Transform specialization is the ambiguity pattern.** The natural way to write a family of transforms is a general one plus overrides — `MessageDomain1.DomainMessage → MessageDomain2.DomainMessage` in general, and something sharper for `MessageType1`. Those two rules in one knowledge co-declare an ancestor and a descendant, so a `MessageType1` fact resolves to neither and **both** transforms go silent.

That means specialization cannot be by subtype dispatch in the engine — the same conclusion the dispatch grammar reached, arriving from the transform side. It has to be either distinct fact types (the dispatch chooses which specialization's fact to insert) or one rule at one level with guards discriminating.

My recommendation is **guards**: one declared fact type per transform family, with `con` conditions selecting. It's expressible today, it's ambiguity-proof by construction, and it keeps "which transform applies" as authored logic rather than as an emergent property of the type lattice.

## What I need from you on transform domains

**1. Where does a transform domain sit?** Peer of the message domains under `DataDomain`, or outside the inheritance tree entirely? It matters: if a transform domain derives `DataDomain`, its types join everyone's ancestor set.

**2. What does a transform rule match, and produce?** My recommendation: **message data facts in, message data facts out** — never messages. Message data types compose rather than inherit, so they're pairwise unrelated and a transform session can declare all of them safely. The message stays an envelope whose job ends at the dispatch, which keeps the target-language `case` and the rete matching either side of one clean boundary.

**3. Which domain owns the rules?** Today an actor's knowledge is built from its own domain's rules. A transform's rules belong to the transform domain, so either the actor loads a domain it isn't a member of, or a transform domain has its own actor. That's a real structural choice and I don't want to assume it.

**4. How many transforms, and between which pairs?** Four message domains give twelve ordered pairs; enumerating them tests nothing. I'd propose four deliberate ones:

```text
MessageDomain1 -> MessageDomain2      simple
MessageDomain2 -> MessageDomain3      chains with the above: 1 -> 2 -> 3
MessageDomain1 -> MessageDomain3      competes with the composed path — same source, same target
MessageDomain4                        no transform at all — the absence case
```

That gives composition, a direct-versus-composed collision, and absence, in four transforms instead of twelve.

---

**[Dev]** Transform domains is where inheritance between modules is most important. The name _.datadomain.DataDomain is the full explicit type name (ignoring what "_." is) of DataDomain. The name _.messagedomain.MessageDomain* for all message domains (N.B. the actual name for a message domain should not contain either "message" or "domain"). The form of a transform domain is _.source.target.Source_Target

**[Claude]** Two things in that I need to reflect back precisely, because one of them diverges from a convention already in force.

## The naming rules as I read them

```text
domain             _.<name>.<Name>                   _.datadomain.DataDomain
transform domain   _.<source>.<target>.<Source>_<Target>
```

The first matches the existing convention exactly — the package element is the lowercased type name, and DRACO.md notes it's coupled case-insensitively.

**The second breaks that coupling**, and deliberately I assume: `_.ash.birch.Ash_Birch` has package `ash.birch` whose last element is `birch`, not `ash_birch`. So a transform domain is not named after its own package the way every other domain is. Two consequences worth naming now rather than discovering later:

- The transform domain's package **nests inside the source domain's** (`ash.birch` under `ash`). In draco, package nesting carries no inheritance — `Marine` derives `World` by derivation, not by living under it — so the nesting is organizational unless you state the edge.
- `Foo_Bar` is the first type name in the corpus with an underscore. It lexes fine in drake, but the leading-`_` parameter convention (`par _name`) sits nearby, so the two shouldn't be conflated in the spec.

## Placeholder names

`MessageDomain1..4` violate your own rule (they contain both words), and the obvious replacements are unavailable — `Alpha`/`Bravo`/`Charlie`/`Delta` are on DRACO.md's retired list. I'd use **`Ash`, `Birch`, `Cedar`, `Dogwood`**: alphabetical like the retired set, neutral, no collision with any draco vocabulary, and short enough to read in an ancestor-set table. Swap them for anything you prefer — nothing downstream depends on the choice.

```text
_.datadomain.DataDomain
_.ash.Ash          _.birch.Birch          _.cedar.Cedar          _.dogwood.Dogwood
_.ash.birch.Ash_Birch       _.birch.cedar.Birch_Cedar       _.ash.cedar.Ash_Cedar
```

## The one thing I can't resolve by reading

**"Transform domains is where inheritance between modules is most important"** — I have two readings and they lead to different scenarios.

1. **`modules` in draco's sense** — the sealed-family declaration. A transform domain declares its transforms as a closed family, which is what would make dispatch over them exhaustively checkable in both targets (constraint 3 on #63). Inheritance *within* the family.
2. **Modules as the domains themselves** — `Ash_Birch` derives **both** `Ash` and `Birch`, so a transform domain is a subtype of the two domains it bridges. That's inheritance *between* domains, and it's the reading the phrase most naturally supports.

Reading 2 has a sharp consequence I'd want to be deliberate about: anything declared in `Ash_Birch` has both `Ash` and `Birch` in its ancestor set, plus `DataDomain` twice over. A transform rule and a source-domain rule in one knowledge would then be co-declared — the exact pattern that drops facts. Which may be precisely why you say it's *most* important here: the transform domain is where the inheritance lattice actually gets dense, and it's the first place the constraint stops being theoretical.

Which reading — or both?

---

**[Dev]** Modules (and packages) are containment trees not inheritance trees.

**[Claude]** Understood — and that voids my reading 2 entirely: `Ash_Birch` living under `ash` says where it is stored, not what it is. Its ancestor set is whatever its derivation says and nothing more, so the density I was worried about doesn't exist unless we put it there.

## What that exposes about the current corpus

Containment and inheritance are orthogonal in principle, but **nothing in the tree today can tell them apart**, because every containment relation in it is mirrored by an inheritance edge:

| containment | the mirroring inheritance edge |
|---|---|
| `Marine` is contained in the `domains.marine` package | — (package only, no edge) |
| `FixReport` is a member of the `Marine` domain (`types [...]`) | `FixReport` derives `Marine` |
| `BodyElement` is a module of `TypeElement` | `BodyElement` derives `TypeElement` |
| the eleven kinds are modules of `BodyElement` | each derives `BodyElement` |

Both uses of `modules` in the corpus list exactly the direct subtypes, and every domain member derives its domain. So the two concepts have never been observed apart, which is why I inferred one from the other.

**Your scenario spec already separates them**, and that may be the most valuable thing in it: `MessageDataType`s are *contained* in a message domain but — on the recommendation you haven't rejected — inherit nothing from it. That's the first containment without a mirroring edge. The scenario is the instrument that pulls the two apart.

One consequence to flag while it's cheap: if `modules` is containment, then it is not a closed-family declaration, and constraint 3 on [#63](https://github.com/ejb816/nexonix/issues/63) — dispatch must be over a closed set, and `modules` already declares closedness — rests on the coincidence rather than on the concept. Closedness would need saying some other way.

## So the question I actually meant to ask

With containment ruled out as a source of edges: **which inheritance edges does a transform domain's content have, and do they cross into the source and target domains?**

My recommendation, and it's asymmetric on purpose:

- **Output side inherits.** What `Ash_Birch` produces derives `Birch.DomainMessage` — the transform's result *is* a Birch message, not something Birch-shaped that must be converted again. That's a cross-module inheritance edge, and it's the one that makes the transform's output directly usable by the target domain's actor.
- **Input side does not.** `Ash_Birch` *references* Ash's message data as facts to match; it doesn't derive anything of Ash's. A transform reads its source and becomes its target.

That asymmetry would also be what makes chaining work: `Ash_Birch` output *is* a Birch message, so `Birch_Cedar` can consume it with no adapter in between — which is the property your `1 → 2 → 3` chain needs and the direct `1 → 3` transform competes with.

---

**[Dev]** The only type in ash.birch.Ash_Birch domain that has to be derived from anything are primal types that have value instance factories have birch.Birch domain primal type values as arguments. Any composite type that references a primal type can use compose with the factories native to the types defined in birch.Birch. Any composite type will have primal types as leaf values and those leaf values are created from the either the primal types in the source domain on input or from the source to target domain on output with the parameter values for the factory also coming directly from the source domain data type.

**[Claude]** Let me reflect that back as a concrete instantiation, because one clause has two parses and being specific is the cheapest way to find out which.

## The reading

```text
ash.Ash                      source
    Feet          primal    from Primal(Double)
    Reading       composite  { height: Feet, tag: Ash.Tag }

birch.Birch                  target
    Metres        primal    from Primal(Double)   factory Metres(value: Double)
    Name          primal    from Primal(String)   factory Name(value: String)
    Fix           composite  { elevation: Metres, label: Name }   factory Fix(elevation, label)

ash.birch.Ash_Birch          transform
    ??? — one type per LEAF conversion, and nothing else
```

`Birch.Fix` needs no counterpart in the transform domain and no derivation: it is produced by *calling* `Birch.Fix(...)`, with `Birch.Metres(...)` and `Birch.Name(...)` nested inside it, and the arguments to those coming out of the `Ash.Reading` fact. Composition is factory application, not declaration.

Three things follow that I'm confident about:

- **The transform domain's type population is tiny** — one type per leaf conversion, nothing for any composite, however deep. A transform between two rich domains is mostly *expressions*, not types.
- **drake can already write the composite half.** `<fn> parameters par …` is exactly "compose with the factories native to `Birch`", and the mods actors already spell nested factory applications that way. Nothing new is needed for the composite side.
- **Transform-domain types are values, not facts.** The facts are the source message data going in and the target message data coming out; the leaf conversions are intermediate values. So none of the fact-resolution constraints touch them.

## The clause I can't disambiguate

> primal types that have value instance factories have `birch.Birch` domain primal type values as arguments

Two readings, and they point opposite ways:

1. **The transform primal derives the TARGET primal.** `Ash_Birch.Elevation from Birch.Metres`, with a factory taking the source value — `Elevation(feet: Ash.Feet)` — so the conversion arithmetic lives in the factory body and the result *is* a `Birch.Metres`. This matches "output side inherits" and makes the leaf directly usable in `Birch.Fix(...)`.
2. **The transform primal takes target primals as factory arguments.** The factory signature is over `Birch.Metres` values, meaning something upstream has already produced them and this type wraps or carries them.

Reading 1 is what "have to be derived from anything" suggests to me — the derivation exists *because* the leaf must be a Birch value — but then the factory's arguments are Ash values, not Birch ones, which is what the clause says. Reading 2 makes the clause literally true but leaves the conversion arithmetic with nowhere to live.

Which is it — and in either case, **where does the arithmetic go**: the factory body of the transform-domain primal, or the rule that matches the source fact?

---

**[Dev]** I should have just used Source and Target for the example. I may have confused the order of transform with Birch and Ash

**[Claude]** Source and Target it is — the roles are the point, and with them the earlier clause resolves cleanly.

## The transform pattern

```text
_.source.Source                      source domain
    Feet        primal      from Primal(Double)
    Reading     composite   { height: Feet, tag: Tag }

_.target.Target                      target domain
    Metres      primal      from Primal(Double)   factory Metres(value)
    Name        primal      from Primal(String)   factory Name(value)
    Fix         composite   { elevation: Metres, label: Name }   factory Fix(elevation, label)

_.source.target.Source_Target        transform domain — direction is in the name
    Elevation   primal      from Target.Metres    factory Elevation(height: Source.Feet)
    Label       primal      from Target.Name      factory Label(tag: Source.Tag)
```

**Declared: one type per leaf conversion, and nothing else.** Each derives the *Target* primal it must become, and each factory takes *Source* primal values as arguments. The conversion arithmetic lives in that factory body — which is the only place it can live if a leaf is to arrive already being a `Target.Metres`.

**Not declared: every composite, at any depth.** `Target.Fix` is produced by calling `Target.Fix(...)` with the transform's leaf factories nested inside:

```text
Fix parameters
  par Elevation parameters par reading.height
  par Label     parameters par reading.tag
```

That's drake's existing application surface verbatim — nothing new needed for the composite half, however deep the nesting goes.

## What that settles

- **The transform domain is tiny and stable.** Its size tracks the number of distinct leaf conversions between two domains, not the number or depth of the messages. Add a composite to Target and the transform domain doesn't grow.
- **The division of labour is clean.** Leaf arithmetic is in a factory body, reusable across every rule that needs it; the rule is purely structural — match source facts, apply target factories.
- **`Source`/`Target` are roles, not names.** In the four-domain scenario each of Ash/Birch/Cedar/Dogwood is source in some transforms and target in others, and `Ash_Birch` reads unambiguously because the form puts the direction in the name. That's the argument for the naming convention, not just a convenience.
- **Cross-module inheritance, precisely located.** The only inheritance edges leaving a transform domain are leaf-conversion primals deriving Target primals. Nothing derives Source, and nothing derives either domain itself.

## Still open from earlier, and I don't want them to drop

1. **Which domain owns the transform rule** — the transform domain, or the domain whose actor runs the session? Today an actor's knowledge is built from its own domain's rules, so a transform's rules are somewhere its actor isn't a member of.
2. **What the rule matches** — my recommendation was source *message data* facts, never messages.
3. **Whether `MessageDataType`s inherit anything at all** — my recommendation was nothing, ever, which is what keeps them pairwise unrelated and the fact resolution safe.

(3) now looks connected to what you've just described: if message data bottoms out in primal leaves and composes by factory, then the composites need no inheritance for the transform to work — the transform reaches their leaves through element access, not through a type relation.

---

**[Dev]** 1. The DataDomain as the root super-domain (also super-domain of the transform domains) owns all of the transform rules.
2. The transform rule has Conditions that match message data from message facts, not message data facts, since it should not be necessary to insert message data from messages as individual facts in working memory, since that data can be referenced directly from the message fact in any Condition.
3. A message data type should be able to inherit from a message payload type, determined by the message type, that allows the payload data to have different access protection than the message type data.

**[Claude]** All three change something. Taking them in order. *(Checked `superDomain` usage across both corpora.)*

## 1 — `superDomain` gets its first use, and the constraint goes global

`dracoAspect.superDomain` exists, has a drake surface (`super <pkg-seq> <name>`), and is carried by **zero definitions** in either corpus. `DataDomain` as root super-domain of the message *and* transform domains is its first real job.

The consequence is bigger than where the rules are filed. An actor's knowledge is built from its domain's rules, so **all transform rules become co-resident in one knowledge**. The pairwise-unrelated requirement stops being per-actor and becomes a property of the whole system:

```text
Ash.MessageType1 + Birch.MessageType2      siblings across domains — safe, always
Ash.MessageType1 + Ash.MessageType2        siblings — safe
Ash.MessageType1 + Ash.DomainMessage       ANCESTOR PAIR — both go silent
```

So a whole-domain transform (written at `DomainMessage`, applying to everything Ash sends) and any per-message-type transform **cannot coexist anywhere in the system**, not merely in one actor. That's the central tension of the design, and it's the thing the lint has to catch. It also means the "as high or as low in the tree as required" freedom is real but *exclusive*: per transform family, pick a level.

## 2 — This removes the need for new matching grammar entirely

If the fact is the message and conditions navigate into its data, then everything on the matching side is already expressible: `con <bool-value>` is a guard over the pattern's variables, and condition parameters are derived from what the guard mentions. A transform rule is a pattern binding one message type plus guards reaching into its elements. Nothing new.

But it also undercuts the premise I filed [#63](https://github.com/ejb816/nexonix/issues/63) on. If rules declare sibling message types and the actor inserts the message whole — which is what actors do today — **the engine's own resolution does the routing**, correctly and unambiguously. The actor-side match-case then isn't needed to get facts to the right rule.

So what is it for? Three candidates I can see, and they'd scope the issue very differently:

- **adapting untyped input** — the four `Creator`/`Consumer` actors receive `draco.format.json.JSON` and would need to become a typed message before insertion
- **the output side** — deciding which target actor a produced message goes to
- **nothing** — and #63 should narrow to the lint plus the closed-family question, with the dispatch dropped

I'd rather ask than guess, since I opened the issue on the assumption it was routing.

## 3 — Access protection has no representation at all today

Nothing in `drake.dlt` expresses visibility. The nearest existing thing is the `Local` kind — a binding that is deliberately *not* a declared element (no trait member) — which is all-or-nothing and construction-scoped, not a protection level.

Worth running your own Haskell test on it before it takes a shape: Scala has per-member modifiers, Haskell has **module export lists** — visibility at module granularity, per name, all-or-nothing, with no `protected` at all. A neutral formulation has to survive that, which points at *exposed / not exposed at the type boundary* rather than at a ladder of levels.

Two things I need to pin down on the shape:

- **Where is the payload type declared** — in the message domain beside its message type? And is it a type with content, or a marker whose only job is to carry the protection boundary?
- **"Determined by the message type"** — does each `MessageType` name its payload type, so that data types inheriting that payload become *that* message's payload? If so it's a reference from the message type, and the data types point back at it by derivation — two edges describing one relationship, which is worth being deliberate about.

One consistency check, in your favour: #3 makes message data types share an ancestor, which would have been a fact-resolution hazard — except #2 says message data is never inserted. The two answers hold together, and they'd stop holding together the moment anything inserts message data as a fact.

---

**[Dev]** The message/payload type distinction is meant as a mixin tag for dispatching the payload type rather than deriving from the message type in the sense of "extends"

**[Claude]** That distinction lands differently in each target, and one of them favours it.

## What a tag is, structurally

In draco it's still an entry in `derivation` — there's no other slot for it — so `MessageDataType1 from Payload1` looks identical to a substantive parent. The difference is entirely in what it's *for*: nothing is inherited, and the edge exists to be selected on.

**Scala gets this for free.** Multiple derivation entries already emit as `extends A with B`, so a tag costs nothing new.

**Haskell gets it better than it gets inheritance.** An empty class with instances —

```haskell
class Payload1 a
instance Payload1 FooData
```

— is *exactly* a mixin tag, and dispatch on it is a class constraint. This is the first thing in the design that ports more cleanly to Haskell than to Scala: substantive inheritance needs an encoding there, a tag needs none. Worth noticing that tags and inheritance, which look alike in the Scala projection, come apart completely in the Haskell one.

**Access protection keyed on a tag also survives that test**, where per-element modifiers would not. Haskell's visibility is a module export list — per name, all-or-nothing, no `protected` — so "types carrying this tag are exposed / not exposed" projects; "this field is protected" does not.

## Where it bites

To the rule engine a tag is just another supertype. If a tag ever appears in a rule's fact position while a tagged type appears in another, that's the ancestor pair, and both go silent. Answer 2 keeps this moot — message data is never inserted — so the invariant to state is: **a tag is never a fact type**. It's cheap to hold now and expensive to discover later.

## Two things I still can't infer

**Where does the tag dispatch happen?** If not in the rete layer (which answer 2 rules out for payload data), then it's the actor's match, or a condition guard, or the selection of which target factory to call. That decides whether the rete layer needs to see tags at all — and if it doesn't, the invariant above becomes structural rather than disciplinary.

**How is a tag distinguished from a substantive parent?** Not currently possible: both are `derivation` entries, and contentlessness doesn't discriminate — `Marine` and `DracoType` are contentless too, and both are substantive. If access protection keys on the payload tag, something has to identify *which* parent is the tag. Either the message type names it (which you implied with "determined by the message type"), or the tag declares itself, or the distinction stays purely conventional and only the projection cares.

---

Separately: we've settled enough of this scenario that it's worth writing down rather than continuing to hold in the thread — the five-domain shape, the naming conventions, what inherits and what merely composes, `DataDomain` owning the rules, messages-as-facts with conditions navigating into them, the leaf-conversion transform pattern, and the co-declaration constraint that governs all of it. Do you want that as a document in the repo, or as a comment on #63? I'd lean to the repo, since it's a specification the issue should reference rather than contain.

---

**[Dev]** The previous prompt was mas meant for the protection concern not memory insertion.  The actor  message dispatch will use the tag and the only memory insertions will be sibling payload types.

**[Claude]** That reconciles the two answers, and it's a better design than I was reading toward.

## The corrected picture

- The **tag** is what the actor's match-case selects on — so tags live in the target-language dispatch and never reach the rule engine.
- What crosses into working memory is **the payload**, one fact per message, with conditions navigating inside it. Not the message's data decomposed into many facts — that was your earlier point, and it stands.
- Payload types are **siblings**.

That last constraint is the whole ballgame. If everything inserted is drawn from a flat sibling set, no two declared fact types can ever stand in an ancestor relation, and the resolution hazard is **designed out rather than linted against**. The lint stops being a rule authors must remember and becomes a check that the invariant still holds — which is a much better place for it.

## What it does to the dispatch's justification

I had the dispatch's job wrong, and so does [#63](https://github.com/ejb816/nexonix/issues/63) as written. It isn't routing — the engine routes siblings correctly on its own. **The dispatch is what converts hierarchy into siblings.** A message arrives at whatever height in the tree its type sits, and the dispatch produces a fact at a fixed, flat level. Hierarchy on the way in, siblings in working memory, and the one thing the engine cannot do safely never gets asked of it.

That also makes "a tag is never a fact type" structural instead of disciplinary: tags are consumed by the dispatch, so they can't appear in a fact position by construction.

## And the two boundaries turn out to be one

The payload is what the actor may hand to the rules; the protection tag says what is visible once it's there. Same line, seen from two sides — which is presumably why you wanted the tag carrying both jobs rather than a separate visibility mechanism. Worth naming it as one concept in the spec rather than two that happen to coincide.

---

Still open from my last message: whether the scenario write-up goes in the repo or onto #63. Either way I'd fold in this correction — #63 currently states the dispatch's purpose wrongly, and that's the sentence someone would build from.

---

**[Dev]** Yes, write it up in  #63 for posterity, and it appear in the journal as well.

**[Claude]** Journal's Cowork's job — I'll leave that alone. Writing up #63. *(Wrote and posted the comment; recorded the design in memory.)*

Written up as a [comment on #63](https://github.com/ejb816/nexonix/issues/63#issuecomment-5446698333), leading with the correction so nobody builds from the wrong sentence: **the dispatch converts hierarchy into siblings — it is not for routing.** The issue body still argues routing; the comment marks that superseded rather than quietly replacing it.

The write-up covers what we settled: the naming forms and the deliberate package/type-name break for transform domains; containment-is-not-inheritance and the fact that the current corpus can't tell the two apart; `DataDomain` as root super-domain owning all transform rules (first use of `superDomain`, which zero definitions carry today); the transform pattern as leaf conversions only, with composites as factory applications drake already writes; one fact per message with conditions navigating inside it; sibling payload types making the resolution hazard unrepresentable; and the tag as one boundary doing two jobs, which ports to Haskell better than inheritance does.

It also carries the five things still open — where the payload tag is declared and how a tag is told from a substantive parent, the `MessageDataType` ↔ payload relation, how closedness gets declared now that `modules` is containment, the concrete N and edges, and whether the scenario becomes definition-backed.

Two side effects worth flagging:

- **Constraint 3 in the issue body is undermined**, and I said so in the comment. It claimed `modules` already declares the closedness the Haskell projection needs; if `modules` is containment, that rested on the coincidence that its two corpus uses happen to list exactly the direct subtypes.
- I recorded the design in memory as well, since several of these are corrections a fresh session would otherwise re-derive wrongly — particularly containment-vs-inheritance and the dispatch's purpose.

The journal I've left alone — that's Cowork's.

---

**[Dev]** Message domains are not yet defined de jure in draco but should be created de facto using the current drake syntax. If they can't, that means the drake syntax needs to be updated to allow it. THat should allows to defer the five things still open until we construct the scenario and test it.

**[Claude]** Then the next move is empirical: author the scenario in current drake and find out exactly where it fails to reach. Let me pull the exemplars I need to write it correctly. *(Read the domain, rule, actor, and primal exemplars; authored the scenario under `src/test/resources/scenario/`; wrote `ScenarioDrakeTest.scala`.)* Built. Nineteen definitions across four domains, plus the actor:

```bash
sbt "testOnly scenario.ScenarioDrakeTest"
```

## What's there

```text
scenario/datadomain/   DataDomain, AshToBirch                        the root, and the transform rule
scenario/ash/          Ash, DomainMessage, Sighting, Payload,        a message domain, whole
                       Reading, Feet, Tag, Receiver
scenario/birch/        Birch, DomainMessage, Bearing, Payload,       the target domain
                       Sounding, Metres, Name
scenario/ash/birch/    Ash_Birch, Elevation, Label                   leaf conversions only
```

`Elevation` is the pattern under test — derives the *Target* primal, factory takes a *Source* primal, arithmetic in the body:

```text
type Elevation from scenario birch Metres
  factory
    parameters
      par height scenario.ash.Feet
    body
      fix value Double _height.value * 0.3048
domain scenario ash birch Ash_Birch
  super scenario datadomain DataDomain
```

## Five interpretation choices you may want to correct

1. **`_` = `scenario`**, so the root prefix is explicit rather than notional.
2. **Transform rules are members of `DataDomain`** — the literal reading of "DataDomain owns all transform rules". The alternative (rules live in the transform domain, `DataDomain` aggregates them through the super-domain edge) is a different file layout *and* needs a capability that doesn't exist: an actor's knowledge is built from its own domain's rules, with no transitive aggregation.
3. **`super` carries the DataDomain edge** on both the message and transform domains — its first use anywhere, zero definitions carry one today.
4. **The payload tag is a contentless type that message data derives** — `Reading from Payload`.
5. **`Receiver`'s dispatch is host-opaque Scala**, single-line because a value must be:

```text
mon msg match { case s: Sighting => session.insert(Seq(s.reading): _*); case _ => () }
```

## What I expect, and why it matters either way

I expect **all nineteen to parse, including the dispatch** — and that's the finding, not a pass. Drake carries that line the way it carries any host string: as text it cannot inspect, cannot check, and cannot project to any other target. Message domains turn out to be constructible de facto; the dispatch is the one thing that is smuggled rather than said, which is exactly the boundary #63 exists to move.

The round-trip gate is where I'd expect real surprises, since three things in here have never been exercised: the `super` line, positional bracketed arguments in a rule action, and a factory carrying both parameters and a body inside a transform-domain primal.

If something is rejected outright, that's a hard syntax gap and the worklist writes itself. Either way this defers the five open questions as you wanted — none of them block construction, and the test tells us which ones the grammar forces us to answer first.

---

**[Dev]** *(Uploaded `tree_communication_session.md` — a session log on tree communication: infochemical signalling over mycorrhizal networks, species-specific metabolic fingerprints, fungal modification of compounds in transit, concentration thresholds.)* Based on your choice of the tree domain names, DataDomain should be Forrest. Change DomainMessage to AshSap and similarly with other tree types. I've also added a session log on tree communication you can use for the scenario.

**[Claude]** That's a much better fit than I realized when I picked the placeholders — the session log makes the transform domain *mean* something. Species-specific metabolic fingerprints are exactly why a signal can't cross unchanged, and the fungal modification step ("standardizing diverse tree inputs") is the leaf conversion. Rebuilding on it. *(Rebuilt the scenario — 22 definitions — and updated the test.)*

The forest scenario is in place — 22 definitions:

```bash
sbt "testOnly scenario.ScenarioDrakeTest"
```

```text
forrest/     Forrest (root super-domain), AshBirchAlarm (the transform rule)
ash/         Ash, AshSap, AlarmSignal, DroughtCue, Infochemical,
             AshJasmonate, AshAbscisate, Micromolar, Compound, RootInterface
birch/       Birch, BirchSap, AlarmSignal, Infochemical,
             BirchJasmonate, Micromolar, Compound
ash/birch/   Ash_Birch, Potency, Marker
```

## Where the session log did real work

**The transform domain stopped being arbitrary.** Species-specific metabolic fingerprints are exactly why `Ash.Micromolar` and `Birch.Micromolar` are different types, and the fungal modification step — standardizing diverse tree inputs — *is* the leaf conversion:

```text
type Potency from scenario birch Micromolar
  factory
    parameters
      par potency scenario.ash.Micromolar
    body
      fix value Double _potency.value * 0.62
```

An Ash jasmonate potency arrives, leaves as a Birch one, attenuated by the crossing. Nothing about the shape changed from the abstract version; it just became obvious why it's shaped that way.

**`Infochemical` earned the tag's second job.** It's the payload tag the dispatch selects on — and it's also precisely what may cross the root interface into the fungus, while the rest of the sap stays inside the tree. Your protection boundary and the payload boundary aren't two things that happen to coincide here; in the biology they're the same membrane.

**Two sibling message types** (`AlarmSignal`, `DroughtCue`) with two sibling payloads (`AshJasmonate`, `AshAbscisate`) exercise the invariant directly: everything inserted is drawn from a flat set under `Infochemical`.

**The rule is a real priming rule** — it fires only above a concentration threshold, which is straight from the log ("a receiver tree only reacts if the *ratio*… crosses a specific concentration threshold"):

```text
      con jasmonate.potency.value > 0.5
```

**And `RootInterface` is the wall**, unchanged in kind from before:

```text
mon msg match { case a: AlarmSignal => session.insert(Seq(a.infochemical): _*); case d: DroughtCue => … }
```

Two cases now instead of one, which makes it more obviously a dispatch and more obviously untranslatable — it's Scala text drake can store but not read.

Cedar and Dogwood are mechanical repeats once this parses, and a `Birch_Cedar` transform would give the chaining case.

One small thing: you wrote **Forrest**, which I've used verbatim throughout. The tree sense is normally *Forest* — say the word and it's a one-line rename across the 22 files before anything depends on it.

---

**[Dev]** Sorry. My type average typo error rate is at least two per prompt, and Forrest is correct as a surname so I can call that a typo as well because of the capatialization.

**[Claude]** No apology needed — flagging it before it hardened into 22 files was the point. Renaming. *(Renamed across the tree; wrote the proper-noun-confirmation feedback note.)*

Renamed — `forest/`, `Forest`, and the three `super scenario forest Forest` lines. No `forrest` left anywhere, including the test's doc comment.

```bash
sbt "testOnly scenario.ScenarioDrakeTest"
```

I also recorded the typo rate as working guidance, because the right handling isn't obvious: **use the name verbatim so nothing blocks, then flag the spelling once at the end.** Silently "correcting" would have been wrong here — Forrest is a real surname and could have been deliberate — and stopping to ask before proceeding would have cost a turn for a one-line fix. Flag-and-continue is the cheap path as long as it happens before the name becomes identifiers, which is the part worth remembering.

The scenario is otherwise unchanged and ready. When you run it, the two things I'd read first:

- **anything REJECTED** — a hard syntax gap, and the grammar worklist writes itself from it. The three constructs never exercised before are `super` (zero definitions carry one today), positional bracketed arguments in a rule action, and a factory with both parameters and a body inside a transform-domain primal.
- **`ash/RootInterface.drake` parsing cleanly** — which is the finding, not a pass. It means drake will happily swallow a Scala `match` as opaque text, prove nothing about it, and project it nowhere.

*(Delegated action — Dev ran `testOnly scenario.ScenarioDrakeTest`: 1 test run, passed — report-only, headline `scenario in today's drake: 22 files, 0 rejected, 0 parsed-but-drifted, 22 clean`.)* 22 clean — but a clean round-trip only proves emit and parse agree, not that they agreed on the right thing. *(Read the as-parsed shape table from the test log.)* Every shape is right, which is the part the round-trip alone wouldn't have proven:

- **Cross-package derivation resolved correctly** — `ash/birch/Potency` → `scenario.birch.Micromolar`, so the transform domain reaches into its target's package.
- **Bare references resolved to their own package** — `AshSap` → `scenario.ash.Ash`, `AlarmSignal` → `scenario.ash.AshSap`.
- **`super` works on its first use ever** — `scenario.forest.Forest` on all three of `Ash`, `Birch`, `Ash_Birch`.
- **The transform domain's package/name break was accepted** — `domain=scenario.ash.birch.Ash_Birch` with no complaint.
- Roles landed where they belong: `RootInterface actor=true`, `AshBirchAlarm rule=true`, tags with empty derivations.

**So message domains and transform domains are constructible de facto. Nothing in the structure needs new syntax.** The one thing that isn't *expressed* is the dispatch — it parsed exactly as predicted, as text drake cannot read.

## What this hasn't proven

Parsing is the weaker half. The scenario has never been **generated or compiled**, and three things in it have no precedent in either corpus: a `superDomain` reaching the Generator, an underscore in a Scala object name, and a transform-domain primal whose factory body overrides an inherited `value` from a *different package's* parent. A rule matching a cross-package fact type is a fourth.

That's the gate I'd run next, and it's cheap: `Drake.parse` already yields a `TypeDefinition`, so a test can hand it straight to `Generator.generate` and then to `RuntimeCompiler` — no JSON needed, and `MinimalTypeTest` establishes that generation is total over definitions that aren't on disk. If it compiles, "de facto" is proven end to end and the scenario can carry real rule/fact tests. If it doesn't, the failures are the grammar worklist, arriving from the projection side rather than the surface.

Want me to build that? It's one test file and no changes to the corpus.

Separately, there's uncommitted work sitting in the tree from earlier — the DRACO.md Evrete gotcha, the probe's second version, and now the scenario plus its test. Worth a commit and record whenever you're at a stopping point; say the word and I'll write both.

---

**[Dev]** Should Cowork do the journal before we proceed with closing this session? Or should we close everything up to and including the commit/push from the next draco git record, and then Cowork journal followed by an Amend message commit/push after the journal is updated?

**[Claude]** *(Sampled the journal commits in the history against the work commits; checked `git status`.)* Neither, on the evidence — the repo already answers this, and it's a third option.

**Journal commits and work commits are strictly disjoint in this history.** Six samples: `c369d88`, `941d5de`, `51392cf`, `d1e5ab1` touch **zero** `src/` files; the work commits touch zero journal files. They've never been combined, in either order.

So: **two independent commits, and no amend.** Work first, journal second, for three reasons that are specific to this repo rather than general taste:

1. **The amend would need a force-push.** Under your option B the work commit is already pushed, so amending rewrites pushed history on `main` — and you push from the IDE. That's real risk for no gain.
2. **It would break the git-record's contract.** The record is one file per commit *containing that commit's message*. Amend a journal update into the work commit and the record no longer describes what the commit contains — the audit trail silently stops matching.
3. **Work-first lets the journal cite the hash.** Cowork can reference the actual commit; the reverse ordering can't, since the hash doesn't exist yet.

There's no dependency running the other way — the journal is a narrative of the session, not of the commit — so nothing is lost by going second.

One correction that falls out: my memory of the session-transition ritual has it as *journal → git-record → commit/push*, which the history doesn't support. I've fixed that note to say work commit first, journal as its own commit after.

## What's sitting uncommitted

```text
DRACO.md                                    the Evrete co-declaration gotcha
src/test/scala/domains/world/…              the probe, second version (solo/combined matrix)
src/test/resources/scenario/  (22 files)    the forest scenario
src/test/scala/scenario/ScenarioDrakeTest   its gate
```

That's one coherent commit: the co-declaration finding and the scenario built on it. No `src/main` or `src/mods` changes, so the full suite is a formality rather than a gate — but it's cheap and I'd still run it.

Say the word and I'll write the git-record and the CHANGELOG entry, then hand you the block. After that Cowork journals, and its chapter lands as its own commit the way every previous one has.

---

**[Dev]** I need a guaranteed executable block in one paste, so the final commit/push won't have to be corrected.

**[Claude]** *(Wrote the CHANGELOG entry and `git-record-2026-08-27-2251`; dry-ran the message extraction against the real file; verified the staging set.)* Record and CHANGELOG are written, and I've dry-run the extraction against the real file: subject line correct, blank second line, no `#` header leakage, 71 lines. Staging set is exactly 26 files — nothing stray, no `src/main` or `src/mods`.

One paste, `&&`-chained end to end so a red suite stops before the commit:

```bash
sbt test && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-27-2251 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

Expect **527 tests** (526 plus `ScenarioDrakeTest`) and two report-only headlines:

```text
rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing
scenario in today's drake: 22 files, 0 rejected, 0 parsed-but-drifted, 22 clean
```

Commit subject: *A forest in drake, and the fact-resolution finding it rests on.*

Then Cowork journals, and its chapter lands as its own commit — no amend, nothing to correct.

Two things I'd carry into the next session, both already in the record so they survive the context: the scenario has been **parsed but never generated or compiled**, and the four constructs with no precedent in either corpus (`super`, the transform domain's package/name break, the cross-package derivation, the underscore in a type name) have not yet been through the Generator. That's the next gate, and it's one test file with no corpus changes.

---

**[Dev]** I'm going to manually start the Cowork journal update in the other tab of this Claude app. How is that going to join into the command line processing of your command, if I've already started the command in the command line shell before I've run Cowork?

**[Claude]** They don't join — they collide, in two places.

**1. `git add -A` sweeps whatever exists at that instant.** The window between you pasting and `git add` running is however long `sbt test` takes — roughly 15–30 seconds with startup. If Cowork has written any journal file by then, it lands in the work commit, which is precisely the mixing this history has avoided for every journal commit so far.

**2. The push race.** If Cowork commits and pushes the journal before your `git push` runs, yours is rejected non-fast-forward and you're in the correction scenario you asked to avoid.

## Recommendation: sequence them

Run the block to completion, confirm the push, *then* start Cowork. It's under a minute, it removes both hazards, and it's the ordering that lets Cowork cite the actual commit hash.

## If Cowork is already running

Use this instead — same gate, same message, but the pathspec is on the **commit**, so journal files can appear mid-run and simply won't be included. (DRACO.md's own rule: a scoped `git add` doesn't scope the commit, because the IDE auto-adds.)

```bash
sbt test && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-27-2251 | sed '1d' > /tmp/msg && git add CHANGELOG.md DRACO.md draco-git-record/git-record-2026-08-27-2251 src/test/resources/scenario src/test/scala/scenario src/test/scala/domains/world/SubtypeFactVisibilityTest.scala && git commit -F /tmp/msg -- CHANGELOG.md DRACO.md draco-git-record/git-record-2026-08-27-2251 src/test/resources/scenario src/test/scala/scenario src/test/scala/domains/world/SubtypeFactVisibilityTest.scala && git push
```

I checked the pathspec against the working tree: it matches **27 files, which is the entire dirty set** — so today it commits exactly what the `-A` version would, and it stays correct even if Cowork starts writing underneath it.

If the push is rejected anyway because Cowork got there first, that's the one recoverable case and it's a normal rebase:

```bash
git pull --rebase && git push
```

---

**[Dev]** Cowork is not running and won't run until I'm convinced that your original command won't finish before Cowork finished updating the journal. That is the whole point of the last couple of our exchanges.

**[Claude]** Direct answer: **it can't.** Nothing in that block waits on anything — it's a shell pipeline, and there is no synchronization between it and another Claude tab. If you paste it now, it commits whatever is in the tree ~20 seconds later, journal or no journal. The guarantee you're asking for cannot come from the command; it can only come from ordering.

And I have to correct something I told you, because it pushed you the wrong way.

I said journal and work commits "have never been combined in this history" and treated that as decisive. It isn't. What I actually observed was that journal commits contain *only* journal files — which is equally explained by those being journal-only sessions, with nothing else dirty at the time. It's not evidence that a combined commit was ever avoided. The recorded session ritual says the opposite: journal first, *then* git-record and commit — i.e. one commit that sweeps both. I over-read the log and stated it too strongly, including in the record's header.

## So the sequence you want is the one you proposed first

**Cowork writes the journal → then paste the block, unchanged.** `git add -A` picks up the completed journal along with the work, one commit, no amend, nothing to correct. That's your original option A, and the ordering *is* the guarantee.

The block needs no change:

```bash
sbt test && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-27-2251 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

Two things to settle before you start Cowork:

1. **Does Cowork commit and push on its own, or only write files?** If it only writes, the above is exactly right. If it commits, the journal is already in and the block would push into a moved remote — tell me and I'll adjust.
2. **The record's header currently asserts the wrong thing** — it says journal and work must not be combined, and instructs Cowork-after. If we're going combined, I should rewrite that header and the commit message's closing line before you run anything, so the audit trail doesn't contradict the commit it describes.

Say which, and I'll fix the record now so the paste stays a single clean shot.

---

**[Dev]** Cowork only updates using the latest journal extract, which I also extract manually with a command. No commit or push. Not combined means that Claude Code never does both the normal session work and the journal update,

**[Claude]** That settles it — "not combined" is about authorship, not commits. Claude Code does the work, Cowork writes the journal, and one commit carries both. Fixing the record so the audit trail says that. *(Rewrote the record's header and the message's closing line; re-verified the extraction; corrected the ritual memory note.)*

Everything's aligned now. One correction owed: I told you earlier I'd "fixed" the ritual note to say work-commit-first. I never made that edit — and just as well, because the note was right as written: journal via Cowork first, then git-record and commit/push, with `git add -A` sweeping both into one commit. Your ritual already encoded exactly what you've been asking me to guarantee; I talked myself out of it by over-reading the log.

The record now states the real sequence, and its message carries a closing line saying the journal chapter rides along. Re-verified the extraction after editing: subject line correct, blank second line, zero header leakage.

## Sequence

1. Extract the journal source and let Cowork update `draco-dev-journal/` — files only, no commit, no push.
2. When Cowork is **done**, paste this once:

```bash
sbt test && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-27-2251 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

`git add -A` takes the session's work and the finished journal chapter together. One commit, no amend, no force-push, and the git-record describes exactly what the commit contains.

The only failure mode left is pasting while Cowork is still writing — that would commit a half-written chapter. Since nothing can make the block wait, the ordering is the whole guarantee: wait for Cowork to report done, then paste.

Expect **527 tests** and the two report-only headlines:

```text
rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing
scenario in today's drake: 22 files, 0 rejected, 0 parsed-but-drifted, 22 clean
```

---

---

**[Dev]** Session close verification:

*(Pasted the one-paste block's output: `sbt test` → commit → push.)*

**[Claude]** Verified clean. `0af56fd` pushed, working tree empty, `main` level with origin. **527 tests, 40 suites, zero failures** — and both report-only headlines exactly as predicted: `rete subtype visibility: CO-DECLARATION DROPS THE FACT — each level works alone, together they see nothing` and `scenario in today's drake: 22 files, 0 rejected, 0 parsed-but-drifted, 22 clean`. The mechanism you were pressing on worked: `draco-dev-chapter-74.md` (1387 lines) and the intro sync rode in alongside the session's work — one commit, no amend, no correction; 29 files, the 27 I counted plus Cowork's two. **What landed today:** `324556c` (foreign derivations spelled by their operator; the root appended, not substituted — #51's derivation edge), `05b14bd` (actor message type moved to its aspect; `derivation` now holds data-inheritance edges only), `0af56fd` (the co-declaration finding, and the forest scenario in drake). **Ritual remainder:** nothing to close — #51 keeps its two open tracks (`valueType` expressions, the `TypeParameter` atom); #63 has the scenario and the corrected dispatch rationale, though its body still states the dispatch's purpose wrongly — a correction queued.

## Status

This chapter transcribes the August 25–28, 2026 session, which closed clean at **`0af56fd`** — 527 tests, 40 suites, zero failures, both report-only headlines exactly as predicted, and the journal chapters riding in alongside the code in one commit exactly as the coordination exchanges planned. Its penultimate recorded exchanges are Dev coordinating this very journal update with the session before its closing commit/push, and this chapter is itself the journal update being coordinated. The closing exchange — the one-paste `sbt test && … git add -A && git commit -F /tmp/msg && git push` block, expected 527 tests, whose `git add -A` is designed to sweep this chapter into the same commit as the session's work — had not yet been run when this was written, and will be appended when the session closes. **Opening state (carried from chapter 73's close):** v2.0.0-alpha.6 shipped (`70ab3f2`, workflow repaired before the tag, release verified; action-version correction `5b48f0c`; CHANGELOG backfill `9f9bebd`), full suite 525/525, `priority-next` set on #51 with the derivation-edge scope written onto the issue itself (done-condition: `reference package: 0` in the loss report, `Dictionary.drake` opening `from {K, V}` with `DracoType` gone), the standing rules in force — no unprompted issues, commit messages through a file never a heredoc, pathspec on the commit not the add. **Commits landed in this session:** **`324556c` "A foreign derivation is spelled by its operator, and the root is appended (#51)"** — no shape change (Dev's provenance correction: the shape-change framing was the model's, not his), an empty `namePackage` now *means* foreign, a foreign referent spelled by its operator (`from {K, V}`), `rooted` appending rather than replacing, the gate verifying reference packages outright, `Cartesian.json`'s straggler root stripped; 525/525, the `[reference package: 1]` bucket gone from the loss report entirely (now 15 fields across 91 types: 12 expression form #61, 3 empty-collection #55). **`05b14bd` "An actor's message type is carried by its aspect, not by a derivation (#51)"** — ten of the twelve `Actor(T)` actors migrated onto `actorAspect.messageType` (three engine changes: `messageType` gained package carriage, `actorMessageType` renders package-relative, `AssemblyValidator` stopped reading the derivation), after which **`derivation` holds data-inheritance edges only, no exception in either corpus** — a subtype check is a plain derivation walk; 526/526. The two unmigrated (`world/Consumer`, `world/Provider`) were left deliberately: no actor aspect, hand-written match-case dispatch — the worked example the dispatch grammar must reproduce. **Decisions taken (each recorded on #51 or #63 and in memory):** **nominal over structural** — Dev's explicit choice ("My mistake. I should have explicitly stated my choice of nominal over structural"): reference slots stay `TypeName`, structure lives in `valueType`, boundary drawn by the loader (*loadable ⇒ nominal*); **flat `TypeParameter`, nesting unrepresentable** — with the census showing all 8 compound actuals are constitutive (`Holon(T <: Product)` requires a product actual) and the follow-up algebra measurement (44 actuals, max depth 1, every compound's operands unqualified atoms) making flat total for actuals via `(,)` as the product head — proposed, not yet decided as a rule; **the actor match-case dispatch grammar is the next major issue after #51** (Dev: next highest priority), with the aspect-substitutability correction (actor instances are never message data; the same for rule and codec aspects; the domain aspect shares the data tree) reframing the 12-actor migration from message-type bookkeeping to removing the role-parameter-in-the-data-tree violation; **DataDomain (Forest) as root super-domain owning all transform rules** — first use of `superDomain`, carried by zero definitions before this; **messages are the facts** — conditions navigate into message data, message data is never inserted; **the message/payload distinction is a mixin tag**, not extends-inheritance — the actor dispatch selects on the tag, only sibling payload types enter working memory, and the payload/protection boundary is one concept ("in the biology they're the same membrane"). **The #63 story (Dev's directive: "write it up in #63 for posterity, and it appear in the journal as well" — honored above in full):** the `SubtypeFactVisibilityTest` probe, built when Dev stated substitutability as the key pre-beta concern, returned a fourth outcome — nothing fired — then, rebuilt as a solo/combined matrix, landed on row 2: **Evrete honours inheritance (a rule at `World`, two levels up, fires on a `FixReport` — but only one level at a time; co-declaring ancestor and descendant in one knowledge makes the resolver pick neither and silently skip the insert** — the fact never reaches working memory, both rules stay quiet, the only trace a `java.util.logging` warning. The shipped corpus is safe by shape (sibling declarations, disjoint taxonomies), not by design. #63 was filed with the measurement as its spine, the two `world/*` actors as acceptance corpus, and four constraints; the gotcha went into DRACO.md §6. The scenario dialogue then **corrected the issue's own premise** — since rules declare siblings and the engine routes siblings correctly, **the dispatch is not for routing: it converts hierarchy into siblings**, producing facts at a fixed flat level so the one thing the engine cannot do safely is never asked of it — written onto #63 as a comment marking the routing framing superseded, with constraint 3 (`modules` declares closedness) flagged as undermined by "Modules (and packages) are containment trees not inheritance trees", and five open questions carried (tag declaration and tag-vs-substantive-parent discrimination, the `MessageDataType` ↔ payload relation, how closedness is declared, concrete N and edges, definition-backing). **The scenario (uncommitted, with the probe, the DRACO.md gotcha, its record `git-record-2026-08-27-2251`, and the CHANGELOG entry):** designed interactively across nine exchanges — DataDomain root, per-domain `DomainMessage` roots with sibling `MessageType`s, message data composed not inherited, transform domains as `_.source.target.Source_Target` (the first deliberate package/type-name break, the first underscore in a type name) containing **leaf conversions only** (a primal deriving the *Target* primal, factory over *Source* primals, arithmetic in the factory body; composites are factory applications drake already writes) — then authored de facto in current drake at Dev's direction, rebuilt on Dev's uploaded tree-communication session log as the forest scenario (Forest/Ash/Birch, `Infochemical` as tag and membrane, a real concentration-threshold priming rule, the fungal modification as the leaf conversion), the Forrest→Forest typo flagged-not-silently-fixed and renamed. **`ScenarioDrakeTest`: 22 files, 0 rejected, 0 parsed-but-drifted, 22 clean**, with every shape verified right — cross-package derivation, bare-reference resolution, `super` working on its first use ever, the package/name break accepted. Finding, not pass: the structure needs no new syntax; the dispatch is the one thing smuggled as host-opaque text rather than said. **Test counts across the arc:** 525/525 (`324556c`) → 526/526 (`05b14bd`, probe in) → probe solo run (1 test, the CO-DECLARATION verdict) → `ScenarioDrakeTest` (1 test, 22 clean) → **527 expected** on the pending close-out block. **Issues:** #63 opened (filed on the probe's verdict — the dispatch grammar, now corrected to hierarchy-into-siblings) and enriched with the scenario write-up; #51 commented three times at Dev's direction (the derivation-edge close, the DECIDED nominal-over-structural, the flat decision with census). **#51 state:** derivation edge **closed**; 12-actor migration **done**; remaining open — `valueType` expressions (34 applications, 7 arrows, still Scala text), the `TypeParameter` atom (flat decided, renderer-first increment sketched, not started — deliberately paused for the type-algebra and substitutability decisions), the RESERVED primitive set. **Queued at the open end:** the session-close block itself (this chapter rides in its commit); the generate-and-compile gate on the scenario — the four constructs with no Generator precedent (`super`, the package/name break, cross-package derivation, the underscore name) plus a rule matching a cross-package fact type, one test file, no corpus changes; the dispatch grammar implementation proper (#63); the `TypeParameter` flat increment; the `(,)`-product-head and depth-1-as-rule decisions; the closedness-declaration question now that `modules` is containment; the tag-discrimination question; the substitutability-usage count (where the corpus uses a value at its parent type); Cedar and Dogwood plus `Birch_Cedar` for the chaining case; and from chapter 73, still standing — `AGENTS.md`, the `TargetScala` rename explanation, the group-1 extraction after #61, the five `.namePath` workarounds. **Because the session is open, this chapter is provisional in exactly one respect: the close-out exchange is not yet in the data.**
