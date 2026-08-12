# Draco Dev Journal — Chapter 73

**Session date:** August 10, 2026 (session possibly ongoing)
**Topic:** The #44 endgame (session possibly still open): the kickoff prepared at chapter 72's close, re-issued fresh after a connection-killed false start, finds a complete #60 bracket implementation already sitting in the working tree — verified read-level, then 179/180 with the one failure being the `=` named-argument node the renderer couldn't spell, patched in one line → the Map recommendation ("don't give it a package") answered by Dev dissolving the question instead: `{K,V}` in drake and JSON, `Map[K,V]` minted entirely inside the Scala target (the `ScalaTarget` → `TargetScala` rename trailed for later) → the pilot in two gated steps — `scalaTypeExpression`/`targetTypes` provably inert at 514/514, then six valueTypes neutral, both Drake converters, and the arity guard that stops `{K, V}` becoming `Set[K, V]` → the five-question design dialogue: `from Map(K, V)` proven constitutive, not incidental; the expected post-#51 `Dictionary.drake` (`from {K, V}`, `DracoType` gone, `rooted`'s `nonEmpty` gate exposed as too narrow); the round-trip's blindness to redundancy worked through twice — Claude's "lossy-on-purpose" framing overturned by Dev's own question into normalization under the INFERENCE principle, with Dictionary the one true positive → "Is it time to get back to #51?" answered yes, narrowly: the derivation edge only, three independent findings converging on `derivation` holding a type expression → 514/514 with the full pilot in, the record written closing #44 and #60, and the session left open on the one call Claude asked for — the Scala-first TypeDefinition shape change. Transcript.

---

**[Dev]** *(The prepared kickoff from chapter 72's close, transcribed as issued. It was first issued August 9 at 21:56 in a session that began making changes — language spec, emitter, parser, extending the parse gate to the mods actors — and died on "API Error: Connection closed mid-response"; that session was abandoned and the kickoff re-issued fresh on August 10. Its partial changes may explain the working-tree state the fresh session found.)*

Continue the drake round-trip work (#44). Last commit 746de5b, full suite 495/495.

State: DrakeParseTest covers 80 types in both directions; only the codec aspect is
held back. The measured loss is down to 14 fields, and each has an owner:
  - 10  expression form (string vs tree)  -> #61, the drake expression grammar
  -  3  legacy "Seq()" spellings          -> #55
  -  1  host reference with no package    -> the Map decision below

Two things are waiting on a decision from me, not on work:

1. MAP. A drake reference is bare when it lives in the referring type's own package.
   A host type with NO package (Map, in Dictionary.json) is therefore spelled bare
   and comes back owning "draco". Giving Map a real package would fix it and would
   let DrakeParseTest.surfaceCarried stop normalizing derivation/modules packages —
   gate 2 would then verify packages outright. It touches the host-reserved-name
   question (Scala's is scala.collection.immutable.Map), so it is a corpus-data call.

2. GROUP-1 EXTRACTION. A set of Generator helpers is already language-neutral but
   lives inside the Scala engine: isDomain/isRule/isActor/isLeaf and the role-aspect
   count, conditionParameters, isActorMintingFactory, the derivation-graph walks and
   topological sort, the codec gate. Where they should land is staged toward Source
   (#57) and is a structural placement call.

Also open and NOT blocked on me: #60, a nested argument list has no terminator (the
par ending the inner list also opens the enclosing list's next argument). It is a
bracketing decision about the surface, it keeps the mods actors outside the parse
gate, and it is the other thing standing between #44 and closing.

Do not open new issues without asking. See memory: feedback_do_not_inflate_the_backlog.

**[Claude]** I'll start by getting oriented in the current state before addressing the decisions. *(Checked git log/status and the diff, the issue list, `Drake.scala`'s lexer and reserved words, `Dictionary.json`, `surfaceCarried` in `DrakeParseTest`, both resource trees' derivation/modules references, the group-1 helper bodies, `drake.dlt`'s BRACKETS/INFERENCE/RESIDUAL sections, #57, `derivationRef`, and the mods `sourceRoot`.)* There are uncommitted changes. The working tree is ahead of your description — it already contains a complete #60 implementation. Working tree checked. Here's where things actually stand, plus what I found on each decision.

## #60 is already implemented in the tree — unverified

The four uncommitted files are a complete #60: the `drake.dlt` spec (argument-as-second-opener under the one BRACKETS rule), `parLines`/`arguments` as exact inverses in `Drake.scala:169`, `aerial/Input.drake` re-authored with brackets, and `DrakeParseTest` extended to walk the **mods corpus** under both gates.

Read-level check passes: `[`/`]` are in `reserved` (`Drake.scala:532`), the lexer emits a standalone `[` only when whitespace-follows so `.get[Double]` stays glued (`:557`), the single-line `… ]` case in `parLines` is covered, and the mods root URI resolves correctly (`sourceRoot` carries its trailing slash). `terrestrial/Output` is held out of gate 2 only, for the inline-tuple reason — which is right: that's invisibility, not ambiguity, so brackets wouldn't address it.

I don't run sbt, so that's static reasoning, not a green suite:

```bash
sbt "testOnly draco.DrakeParseTest"
```

## 1. MAP — my recommendation is *don't*

Two facts I checked that change the framing:

- **Map is the only package-less reference in either corpus.** I walked all `derivation`/`modules` entries in both trees: 112 references, 111 are draco-domain types carrying a package, one is `Map`. So it is exactly your 1 field, and it's the only thing standing between gate 2 and verifying packages outright.
- **Giving it a package changes no generated Scala.** `derivationRef` (`Generator.scala:313`) qualifies only when the parent's simple name collides with the declaring type's. `Map` ≠ `Dictionary`, so it emits bare either way. The host-reserved-name question isn't touched at emission at all — it's purely normative corpus data.

Which is why I'd still say no. `drake.dlt:384` already carries your resolution on exactly this: *name the capability, not the host language*. Writing `scala collection immutable Map` into the normative JSON contradicts that and pins the JVM inside the corpus that #21 wants to project to Haskell and Lean — for a field the target never reads.

**Instead, take the benefit without the decision:** narrow `surfaceCarried` from stripping `namePackage` on *all* derivation/modules references to holding out the one known residual, the same shape as `inlineTupleArgument`. Gate 2 then verifies packages outright on 111 of 112 references today. The loss report is unaffected (it doesn't normalize), so the tail stays honestly at 14, and `drake.dlt:158`'s RESIDUAL paragraph stays true as written. The real Map question then lands where it belongs, in #51's constitutive-vs-incidental host types.

## 2. GROUP-1 — placement is TypeDefinition, but the timing is after #61

**Placement:** not Source. #57 makes Source the *bound of the varying side* — `Generator(L <: Source)` with Drake and ScalaTarget as the arguments. These helpers read a `TypeDefinition` and are consumed identically by every projection, so they're shared vocabulary belonging to the thing they interrogate, not to the thing that varies. Putting them on Source would push common ground onto the parameter.

**Timing:** the group isn't as uniform as it looks. Reading the bodies:

| helper | body |
|---|---|
| `isDomain`, `isRule`, `isObjectOnly`, `isLeaf` | structural field comparisons — neutral, expressible once #61 lands |
| `isActor`, `roleAspectCount` | need a lambda (`exists(tn => …)`, `count(identity)`) — drake has no lambda surface |
| `moduleOrder` | mutable `LinkedHashSet` + nested recursive closure — not expressible |
| `hasCodec` | `codecDeclaration(td, ctx).nonEmpty` — *defined as "the Scala emitter produced something"* |

That last row is the one worth flagging: the codec gate isn't language-neutral as written, it's neutral-in-intent and Scala-in-definition. Extracting it is a rewrite into a definition predicate, not a move.

So: moving the group now relocates Scala into a neutral-looking home without making any of it neutral — which is the failure mode `feedback_model_validation_via_projection` warns about, a projection that preserves the mis-modeling. **#44 closes without it.** I'd sequence it after #61, and start with the four structural predicates only.

Both of these leave #44 needing only the suite run on the in-tree #60. Want me to make the `surfaceCarried` narrowing now, so it goes into the same run?

*(Delegated action — Dev ran `testOnly draco.DrakeParseTest`: 180 tests run, 179 succeeded, **1 FAILED** — `mods domains/aerial/Input.json` in gate 2, `Drake.expression: unknown operator '='` on the named-argument tree; the loss report printed 16 fields across 90 types, and the scope line read "80 draco + 10 mods types in, 0 codec-aspect + 0 drake-less held back".)* 179/180. The one failure isn't the parser or #60 — gate 1 passed on `Input.drake`. It's the test's own normalizer. The failure is a gap in `Drake.expression`, surfaced exactly because the mods corpus is the first to carry named arguments. *(Confirmed against the loss log; two edits to `Drake.scala`.)* Both mods losses are in `terrestrial/Output.json` — the already-held-out inline-tuple case. **Nothing from `aerial/Input`**, which means its trees round-trip identically and the only thing failing is that `Drake.expression` can't render the `=` node. One-line completion: `case "=" => args.mkString(" = ")` alongside `(,)` — the other argument-position node.

```bash
sbt "testOnly draco.DrakeParseTest"
```

**What the run established**, beyond the one gap:

- #60 works. `aerial/Input` — three named arguments, each a two-call chain, the exact case that blocked the mods corpus — passes gate 1 as authored, and contributes **zero** entries to the loss report. Its trees round-trip identically; only the renderer couldn't spell them.
- All 10 mods types pass gate 1. Nine of ten pass gate 2 (Input was the tenth, now fixed).
- The whole mods corpus adds just **2** losses, both in `terrestrial/Output.json` — the inline-tuple case you're already holding out, plus its `{".":[…]}`-path-vs-string tail. That's #61's territory, not a new category.

The tail is now 16 across 90 types, and the owners are unchanged: 12 expression form (#61), 3 `Seq()` spellings (#55), 1 reference package (the Map decision). Nothing unaccounted.

That leaves #44 needing only your two calls. My recommendations stand, and the run sharpened the first one: since `aerial/Input` proved clean, the single `reference package` loss really is Map alone, so narrowing `surfaceCarried` to hold out just that one reference would let gate 2 verify packages outright on 111 of 112 — without writing `scala collection immutable` into the normative corpus. Say the word and I'll make that edit; on group-1 I'd hold until #61, for the reasons above.

---

**[Dev]** *(Pasted the `DrakeParseTest` re-run: 180 tests run, all 180 passed.)* Regarding "1.MAP - my recommendation is don't". Let's consider the generation of any Scala code that contains Map (immutable from Scala, as in Dictionary type definition definition) to be generated from "{K,V}" in drake and json, completely in the scope Generator[TargetScala]. (If it's not obvious yet, I'll explaing later why I'm switching ScalaTarget to TargetScala.)

**[Claude]** Suite green at 180/180.

That dissolves the problem rather than answering it — `Map` stops being a reference at all, so there's no package to decide. And it completes the notation family: `[T]` Seq, `{T}` Set, `{K,V}` Map. *(Censused brace and `Map` occurrences across both corpora, read `typeExpression`/`parseTypeExpression`, and counted the Generator's valueType emission sites.)* `[T]` = Seq is already live in the corpus (`[Variable]`, `[TypeName]`, `[BodyElement]` …), so `{K,V}` is a real extension of a notation that exists, not a new invention. The machinery is already symmetric — `typeExpression` has `Seq[T]→[T]`, `Set[T]→{T}`, `mut {T}`. And one latent wrong answer sitting exactly where you're pointing: `parseTypeExpression:758` turns **any** `{…}` into `Set[…]` regardless of arity — so drake `{K, V}` currently parses to `Set[K, V]`.

## What your sentence resolves — and one site it doesn't

Map appears in the corpus at **6 places**, but they're two different kinds of thing:

| kind | sites |
|---|---|
| `valueType` strings | `Dictionary` ×3, `TypeDictionary`, `DomainDictionary`, `CLI` (`Map[String, Seq[String] => Unit]`) |
| `derivation` reference | `Dictionary` only — `{"name":"Map","typeParameters":["K","V"]}` |

`{K,V}` is a **type expression**; `derivation` is `Seq[TypeName]`. So the five valueTypes convert cleanly, but `from Map(K, V)` can't hold an expression until `derivation` admits one — which is #51 proper. Worth saying plainly: **the 1-field round-trip loss lives in the derivation, not in the valueTypes**, so this change doesn't close it. It closes the larger thing instead.

## The work, and it's the right way round

Your framing inverts the current direction, which is the whole point. Today JSON carries *Scala* syntax (`Map[K, V]`, `Seq[T]`) and Drake converts **away** from it; you're saying JSON carries the neutral form and TargetScala converts **to** Scala.

That ordering matters, because doing the drake surface first would build converters that the JSON change then deletes. Going straight at JSON avoids the churn:

1. **JSON**: 5 valueTypes → `{K, V}`, `{TypeName, TypeDefinition}`, `{DomainType, TypeDictionary}`, `{String, [String] -> Unit}`.
2. **Generator gains a target-side type renderer** — this is the new capability, and it's the part that's genuinely "completely in the scope of `Generator[TargetScala]`". Today all **48** valueType emission sites write the string verbatim (`Generator.scala:346` and friends), because JSON was already Scala-shaped. They'd route through one `{A,B}` → `Map[A,B]` function. Mechanical, and `DracoGenTest` gates every generated file with `comparisonOnlyExcluded` empty, so it fails loudly rather than silently.
3. **`typeExpression` needs nothing** — a `{K, V}` with no `[` falls through verbatim. Only `parseTypeExpression`'s `{…}` case needs an arity guard so two members stop becoming `Set[K, V]`.
4. **`Dictionary`'s derivation stays a TypeName**, and the loss stays measured at 1, until #51 unfreezes.

Step 2 is the one with reach — 48 call sites — so per your small-increments preference I'd land steps 1+3+4 and step 2 as separate commits, each with a green suite.

Shall I start? And I'll leave `ScalaTarget` → `TargetScala` alone until you explain it — I'd only be guessing at the reason, and the rename touches the domain-name/package coupling.

---

**[Dev]** Start.

**[Claude]** Correcting one thing in my own sequencing before I touch anything: JSON-neutral without the renderer produces broken Scala, so those can't be separate commits in that order. The renderer must land **first** — it's inert until the corpus uses it. *(Read the two splitters, confirmed zero braces in today's corpus valueTypes, found the `TypeLoader.rooted` normalization boundary at both `generate` entry points, captured every TypeElement constructor signature and the aspect fields, then made two edits to `Generator.scala`.)* Both splitters already track `{}` depth, so a map's inner arrow stays nested. Zero braces in the corpus today, so the renderer is provably inert on landing. There's already a normalization boundary at both entry points (`TypeLoader.rooted`) — the target pass composes there. The subtypes are `lazy val` traits, not case classes — no `copy`, and `Pattern`/`Action` carry extra fields a naive rewrite would silently drop. So the walk must be explicit and exhaustive.

Commit 1 is written and should be **provably inert**: `scalaTypeExpression` returns its argument untouched when there's no `{`, and zero valueTypes in the corpus contain one. So every generated file should come out byte-identical.

Since this sits on the `generate` entry boundary for *everything*, the full suite is the right gate rather than a scoped run:

```bash
sbt test
```

What landed:

- **`scalaTypeExpression`** — the brace family (`{T}` set, `{K,V}` map, `mut {T}` mutable set) → Scala. Rewrites brace groups **in place** rather than splitting and reassembling the whole expression, so spacing outside a brace survives character-for-character and a brace can sit anywhere a type can — under an arrow, in a tuple, as a type argument.
- **`targetTypes`** — the walk, applied at both `generate` overloads alongside the existing `TypeLoader.rooted`. Each element kind is rebuilt with *all* its fields; `Pattern` and `Action` carry `variables` outside the shared `body`/`parameters`, which a walk over just the shared fields would have dropped silently. Empty rule/actor aspects pass through unrebuilt.

Two judgement calls worth flagging, since neither is forced:

1. I handled **both** brace arities, not just the map arity you're migrating. `{T}` → `Set[T]` costs one line and is what the notation already means in drake; leaving it out would mean the renderer disagrees with `parseTypeExpression` about the same syntax.
2. I did **not** rewrite the 48 emission sites individually. One normalization at the entry means the next constructor to go neutral (Seq, Set) is a change in one function, not a re-sweep — and there's no site that can be missed.

Once this is green, commit 2 is the corpus: the 5 valueTypes → `{…}`, plus the arity guard on `parseTypeExpression:758`, which today turns any `{A, B}` into `Set[A, B]`.

*(Delegated action — Dev ran `sbt test`: **514 tests run, 37 suites, all passed**.)* 514/514 — commit 1 confirmed inert. Now the corpus. *(Edited the six JSON valueTypes across `Dictionary.json`, `TypeDictionary.json`, `DomainDictionary.json`, `CLI.json` — one site initially missed because `kvMap`'s valueType is the last field in its object, no trailing comma; both Drake converters; the four `.drake` files; `drake.dlt`'s VALUE-TYPES section; verified no `Map[` remains in any valueType.)* All six sites converted. Commit 2 is complete:

```bash
sbt test
```

**What changed:**

- **JSON** — 6 valueTypes now neutral: `Dictionary` (`{K, V}` ×2, `{K, V1}`), `TypeDictionary`, `DomainDictionary`, `CLI` (`{String, Seq[String] => Unit}`).
- **`Drake.typeExpression`** gained a brace case that recurses into members. Without it, CLI's inner `Seq[String] => Unit` would have leaked Scala's syntax onto the drake surface — the emitter's bracket-scan bails out on a brace-wrapped expression, so it would have passed the whole thing through untouched.
- **`Drake.parseTypeExpression`** — the arity guard. `{T}` → `Set[T]` as before; `{K, V}` now carried through instead of becoming `Set[K, V]`.
- **`.drake` corpus** — the four files re-spelled.
- **`drake.dlt`** — VALUE-TYPES gained the map row, and its note no longer claims the JSON always holds a Scala type string, because for this row it doesn't.

**The one thing worth looking at yourself.** `Dictionary.drake` now reads:

```text
type Dictionary(K, V) from Map(K, V) DracoType
  elements
    fix kvMap {K, V}
```

The same concept spelled two ways in one file. That's the increment being honest rather than a slip: `derivation` holds `TypeName`s, so `from` still names a *reference* while the elements carry *expressions*. It's also the same `Map` reference that's the corpus's only package-less one — so the 1-field round-trip loss and this double spelling are one problem with one fix, and both land in #51. I recorded that in the DIVERGENCES entry rather than leaving it to be rediscovered.

If the mixed spelling bothers you more than waiting for #51 does, the alternative is dropping the `Map` derivation from `Dictionary` entirely — it already carries `kvMap` plus exactly the four members Scala's `Map` requires — but that changes what the type *is*, so I didn't touch it.

---

**[Dev]** Are you indicating that the correct Scala would be generated without "from Map(K,V)"?

**[Claude]** Fair challenge — let me check rather than assert. *(Read the generated `Dictionary.scala` and `TypeDictionary.scala`; searched every external use of `typeDictionary`/`domainDictionary`/`kvMap`.)*

No — and my earlier sentence implied otherwise, which was wrong.

Dropping the derivation gives `trait Dictionary[K, V] extends DracoType`, and the four members stop implementing anything. They aren't a substitute for `extends Map[K, V]` — they exist *because* of it. `removed`, `updated`, `get`, `iterator` are exactly Scala's four abstract `Map` members, which is why they're the four that are there.

It would most likely still **compile**: nothing abstract goes unimplemented, `new TypeDictionary { … }` stays instantiable, and the only external uses I can find are `.get(…)` (`DomainBuilderTest.scala:54`) and `.elementTypes`, both defined directly on the trait rather than inherited from `Map`. But compiling isn't the test. `Dictionary` would stop *being* a map — nothing could pass a `TypeDictionary` where a `Map` is wanted, and the four delegating members would be left with no interface to satisfy.

Which cuts the other way from what I suggested: the derivation is **constitutive**, not incidental. Dictionary genuinely is-a map, and the four members are the evidence rather than the workaround. So the `from Map(K, V)` line can't be deleted — it has to become `from {K, V}`, which needs `derivation` to hold a type expression instead of a `TypeName`. That's #51, and it's the same fix as the package-less-reference residual.

The DIVERGENCES entry I wrote says this correctly; the sentence in my message didn't. Nothing in the code changed on the strength of it — I flagged it as an alternative and didn't take it.

---

**[Dev]** What do you expect Dictionary.drake to be after #51?

**[Claude]** Let me check one thing that decides whether `DracoType` still gets spelled. *(Read `TypeLoader.rooted`; searched both corpora for every JSON spelling `DracoType` in a derivation.)* Checked the corpus, and it answers this cleanly.

**`Dictionary` is the only type in either corpus that spells `DracoType` alongside another parent.** Every other multi-parent type — `Ordinal`/`Cardinal`/`Nominal` (`Unit`, `Primal`), `Path`/`Direction` (`Sentient`, `Primal`), `FixReport` (`Marine`, `JSON`) — reaches the root *through* its named parent, because those parents are draco types that root themselves. Dictionary has to spell it because its other parent, `Map`, is a host type standing outside draco's graph entirely.

So the `DracoType` on that line isn't a convention. It's compensation for `Map` being foreign — and #51 removes the thing it's compensating for. My expectation:

```text
type Dictionary(K, V) from {K, V}
  elements
    fix kvMap {K, V}
    dyn removed {K, V} [
      ...
```

Only the first line changes, and it loses two things at once: `Map` stops being a reference, and `DracoType` stops being needed. The two spellings of the same concept collapse into one — `from {K, V}` and `fix kvMap {K, V}`, which reads correctly as *a map that delegates to a map*.

That does need one rule widened. `TypeLoader.rooted:15` gates on `derivation.nonEmpty`, so a derivation of just `[{K, V}]` would count as rooted and Dictionary would silently stop deriving `DracoType`. The test has to become "contains a named parent" — a structural expression doesn't root you, which is exactly the distinction that makes today's `DracoType` there necessary.

**The further step, which I'd separate from #51.** Once `from {K, V}` says *neutrally* that Dictionary is a map, the four `dyn`s are host-interface obligations rather than authored content: `removed`/`updated`/`get`/`iterator` are precisely Scala's four abstract `Map` members, and every body is uniform delegation to `kvMap`. A Haskell target would need none of them and something else instead. Derived from (derivation is a map) + (the delegate is `kvMap`), the file becomes:

```text
type Dictionary(K, V) from {K, V}
  elements
    fix kvMap {K, V}
domain draco Draco
```

which also takes `Option` and `Iterator` with it — the last host type names in the file — plus four host-opaque bodies. Dictionary is currently the densest concentration of host leakage in the corpus, and that's not a coincidence: it's the one type whose *content* is a host data structure.

I'd hold that second step as its own thing, though. It's the same shape as the codec gate — a target obligation inferred from the definition — but #51 as written is about type expressions, and inferring interface members from a structural derivation is a different claim that deserves its own argument.

One incidental find, not worth an issue: `src/mods/resources/domains/world/Cartesian.json` still carries a sole `['DracoType']` derivation, the kind #50 item 1 stripped from the 27 in main. It's in mods, so it was outside that sweep.

---

**[Dev]** When drake parser <-> emitter is working properly shouldn't that reveal unnecessary explicit "DracoType"  in json and drake?

**[Claude]** That question cuts at something real, and the answer has a twist. Let me check how the test loads definitions first. *(Read `DrakeParseTest`'s load path; confirmed neither `Drake` nor either drake test calls `TypeLoader.rooted`.)*

Not as it's built — and this is precisely the failure mode you've flagged before: a round-trip preserves mis-modeling. Redundant data round-trips exactly as faithfully as load-bearing data, so a *perfect* round-trip is the condition under which redundancy is maximally invisible. Confirmed mechanically: neither `Drake` nor either drake test ever calls `TypeLoader.rooted` — the emitter reads raw JSON and spells what's there, so an explicit `DracoType` goes out and comes back untouched.

But there's a version of your question that's exactly right, and drake already does it for one field.

What reveals inferable content isn't fidelity — it's the emitter **deliberately eliding** what the loader can reconstruct, with the round-trip then testing the inference rather than the transport. That's what bare references already are: `from Dictionary` omits the package because the referring type's own package is reconstructable, and gate 2 proves the reconstruction. And the single remaining loss in that report is that machinery *working* — `Map` is the case where the inference is wrong, and the gate is what says so. So the surface has the mechanism; `DracoType` just isn't wired into it.

Wiring it in is small: the emitter drops a derivation entry that `rooted` would supply, the parser doesn't re-add it, and gate 2 compares against the source JSON. The Scala side is already indifferent — `generate` roots at entry, which is why #50 item 1 could strip 27 of these and get byte-identical output.

And here's what makes it worth your attention rather than mine: **today that would flag exactly one type.** Eliding `DracoType` from Dictionary's `[Map, DracoType]` reconstructs as `[Map]`, because `rooted:15` gates on `derivation.nonEmpty`. Same finding I reached last turn by reading a hundred JSON files — except the suite finds it, names the type, and keeps finding it if it regresses.

The sharp edge is that it changes the emitter's contract from faithful to lossy-on-purpose. Gate 1 is unaffected. Gate 2 stops being "does the surface carry everything" and becomes "is the inference sound" — which is a stronger and more useful claim, but it means a failure there is no longer necessarily a parser bug. It's the same reading the reference-package residual already has.

---

**[Dev]** How is the emitter lossy if the json DracoType is made redundant and unnecessary by the language neutral definition of the Generator?

**[Claude]** You're right — "lossy" was wrong, and the caveat I hung on it doesn't survive.

If `rooted` supplies `DracoType` whenever it's absent, then its presence in the JSON carries no information: it's determined by the rule, not by the author. Eliding a field with zero information content isn't loss, it's normalization. That's drake.dlt's own INFERENCE principle — *inferable* means *not information* — and it's the same basis on which bare references already work. Nobody calls those lossy.

My error was comparing against the wrong thing. I was measuring the round-trip against the JSON **as authored**, where `[Map, DracoType]` → `[Map]` looks like a loss. The correct comparison is against the JSON **under the inference rule** — root both sides, then compare. Against that, elision is exactly the identity, and the gate stays as strong as it is now rather than weakening.

Gate 2 already contains the shape of this: `surfaceCarried` strips `namePackage` from derivation/modules before comparing, which is the same move — compare up to an inference the surface deliberately doesn't spell. Rooting both sides is that trick applied to one more field.

And the consequence is better than what I described. With both sides rooted:

- Redundant `DracoType` becomes **invisible** to the gate — correctly, since it isn't information. It's revealed instead by simply not appearing on the drake surface, so the `.drake` corpus becomes the readout.
- A failure now means something exact: *this content is not reconstructable by the stated rule.* Not ambiguous, not "might be a parser bug" — a true positive.

Which is what makes Dictionary the interesting case. Root both sides and it still fails: source `[Map, DracoType]` stays put, while the round-trip yields `[Map]` and `rooted` won't add the root back because the derivation is non-empty. So `DracoType` there really is information — not because the type is special, but because `rooted:15`'s `nonEmpty` gate can't see that `Map` is a foreign parent that doesn't root. The gate would be pointing straight at the rule that's too narrow.

So there's no contract change to weigh. The emitter stays faithful on information; the one type that fails is the one where the redundancy isn't redundant yet.

---

**[Dev]** Is it time to get back to #51?

**[Claude]** Yes — but two things first, and the second one is the actual argument.

**Commit 2 has never been run.** The 514/514 was commit 1. Everything since — the six JSON valueTypes, both Drake converters, the four `.drake` files, drake.dlt — is unverified:

```bash
sbt test
```

And the tree now holds three separable pieces of work uncommitted at once (#60, the `=` renderer fix, the Map pilot). Worth landing those as commits before opening a new front, or the next failure has three candidate causes.

**On #51 itself: the freeze has expired on its own terms.** It was frozen *in favour of the parser*, and the parser now round-trips 90 types across two corpora in both directions. That reason is spent.

More to the point, the last three exchanges each arrived at #51 from a different direction without being aimed there:

- the package-less `Map` reference — the one field the loss report still counts
- `Dictionary.drake` spelling one concept two ways, `from Map(K, V)` beside `fix kvMap {K, V}`
- `rooted:15`'s `nonEmpty` gate, which can't tell a foreign parent from a rooting one

Those are one fix wearing three hats: **`derivation` holding a type expression rather than a `TypeName`.** When three independent findings converge on a single edge, that edge is ready.

So my recommendation is yes, narrowly — that edge only, with Dictionary as the single driving case. Not #51 entire. The rest of it (atomic `typeParameters`, `[String]` → `[TypeParameter]`, the reserved primitive set) is real but nothing currently forces it, and bundling them is the kind of leap that loses the thread.

Two things in its favour beyond timing. The pilot is already built and green: `scalaTypeExpression` + `targetTypes` is exactly the target-side machinery this needs, and Map proved the shape end to end. And the increment has a definition of done that isn't a judgement call — the loss report goes to zero on `reference package`, and `Dictionary.drake` reads `from {K, V}` with `DracoType` gone.

The one thing I'd want your call on before starting: this changes what `derivation` *holds*, so it's a TypeDefinition shape change and Scala-first by your own rule — classpath is compile-time truth. That's a different kind of step from the last two, which were corpus data plus a renderer.

*(Delegated action — Dev ran `sbt test`: **514 tests run, 37 suites, all passed** — commit 2 and the full Map pilot gated.)* 514/514 with the full Map pilot in. That's the whole path proven end to end: JSON neutral, drake surface neutral, `Map[K, V]` produced by the target alone — and `DracoGenTest` byte-identical, so no generated Scala moved.

Let me check the record convention so the commits match it. *(Checked the most recent record; created `git-record-2026-08-10-1845`.)* Matches the ritual. Record written to `draco-git-record/git-record-2026-08-10-1845` — it's the paste-able block, and it sweeps itself into the commit via `git add -A` as the convention requires. It closes #44 and #60.

One thing worth naming since the record cites the number: the loss report reads **16** where your opening message said 14. That's not a regression — it's the mods corpus entering the measurement. Both new entries are in `terrestrial/Output.json`, the inline-tuple case, and the categories are unchanged. The draco side is exactly where it was.

I combined the two changes into one commit rather than splitting them. They share hunks in `Drake.scala` and `drake.dlt`, and `git add -i` isn't available to me — but IntelliJ does per-hunk staging, so the record names the boundary if you'd rather have two.

That leaves the question you asked, still open and now unblocked: **#51's derivation edge.** My recommendation stands — that edge alone, Dictionary as the single driving case, done when the loss report's `reference package` line reaches zero and `Dictionary.drake` reads `from {K, V}` with `DracoType` gone.

The one call I need from you before starting is the one I flagged: this changes what `derivation` *holds*, so it's a TypeDefinition shape change and Scala-first by your rule, not corpus-data-plus-a-renderer like the last two. Different kind of step, and I didn't want to start it on my own read.

*(Session possibly ongoing at time of transcription — chapter to be extended if more follows.)*

## Status

This chapter transcribes the August 10, 2026 session (15:26–22:48 UTC in the recorded data) — **likely still ongoing at transcription time**: the last recorded exchange is the 514/514 paste at 22:46 with Claude's record-written response and the open #51 question, and no closing exchange exists, so this chapter ends on an open session and may be extended. **A false start precedes it:** the same kickoff prompt was first issued August 9 at 21:56 in a session that began making changes — language spec, emitter, parser, extending the parse gate to the mods actors — and died on "API Error: Connection closed mid-response"; that session was abandoned and the kickoff re-issued fresh on August 10. Its partial changes are the likely explanation for the working-tree state the fresh session found — a **complete, unverified #60 implementation already in the tree** (the `drake.dlt` BRACKETS spec, `parLines`/`arguments` as exact inverses, `aerial/Input.drake` re-authored with brackets, `DrakeParseTest` extended over the mods corpus), which Claude verified read-level rather than re-deriving. **Opening state (carried from chapter 72's close):** last commit `746de5b`, full suite 495/495, `DrakeParseTest` covering 80 types both directions with only the codec aspect held back, measured loss 14 fields (10 expression form #61, 3 legacy `Seq()` #55, 1 package-less `Map`), two decisions waiting on Dev (the Map package, the group-1 extraction), #60 the other thing between #44 and closing, and the standing rule: no unprompted issues. **Test counts across the arc:** **179/180** (`DrakeParseTest` with the mods corpus in for the first time — the one failure `Drake.expression`'s missing `=` named-argument node on `aerial/Input.json` gate 2, patched as a one-line `case "=" => args.mkString(" = ")`; #60 itself proved clean, zero loss entries from `Input`) → **180/180** → **514/514** (commit 1 — `scalaTypeExpression` + `targetTypes` — confirmed provably inert, zero braces in the corpus, generated files byte-identical) → **514/514** (commit 2 plus the whole Map pilot; `DracoGenTest` byte-identical). Note the suite grew 495 → 514 between sessions/runs as recorded. **What landed (all uncommitted in the tree at the open end, record written):** (a) **#60 verified through both gates** — 90 types (80 draco + 10 mods) round-tripping, `terrestrial/Output` held out of gate 2 only for the inline-tuple reason (invisibility, not ambiguity); the whole mods corpus adds just 2 losses, both `terrestrial/Output.json`, #61's territory. (b) **The `=` renderer completion.** (c) **The Map pilot** — Dev's directive dissolved the Map-package question rather than answering it: `{K,V}` in drake and JSON, `Map[K,V]` generated "completely in the scope Generator[TargetScala]" (the `ScalaTarget` → `TargetScala` rename explicitly deferred until Dev explains it). Commit 1: `scalaTypeExpression` (brace family `{T}`/`{K,V}`/`mut {T}` → Scala, rewriting brace groups in place) and `targetTypes` (an explicit, exhaustive element walk — the subtypes are `lazy val` traits with no `copy`, and `Pattern`/`Action` carry `variables` a shared-fields walk would drop), applied at both `generate` overloads beside `TypeLoader.rooted`, deliberately not a 48-site sweep. Commit 2: six valueTypes neutral (`Dictionary` ×3, `TypeDictionary`, `DomainDictionary`, `CLI`'s `{String, Seq[String] => Unit}`), `Drake.typeExpression`'s recursing brace case, `parseTypeExpression`'s arity guard (the latent `{K, V}` → `Set[K, V]` wrong answer), four `.drake` files re-spelled, `drake.dlt` VALUE-TYPES gaining the map row and losing its now-false "JSON always holds a Scala type string" claim. **Loss report: 14 → 16 fields across 90 types — not a regression** but the mods corpus entering the measurement (both new entries `terrestrial/Output.json`); owners unchanged: 12 expression form (#61), 3 `Seq()` (#55), 1 reference package. **The design dialogue (pairs of questions Dev drove, each landing on #51):** `from Map(K, V)` proven **constitutive, not incidental** — `removed`/`updated`/`get`/`iterator` are exactly Scala's four abstract `Map` members, dropping the derivation would compile but Dictionary would stop *being* a map (Claude's earlier sentence implying otherwise corrected on challenge); the expected post-#51 `Dictionary.drake` — `from {K, V}` with `DracoType` gone (Dictionary is the *only* type in either corpus spelling `DracoType` beside another parent — compensation for `Map` being foreign), requiring `TypeLoader.rooted:15`'s `nonEmpty` gate widened to "contains a named parent", with the further host-interface-obligation step (the four `dyn`s derived, `Option`/`Iterator` gone — Dictionary as the densest host-leakage concentration) held separate; the round-trip-reveals-redundancy question — a perfect round-trip is where redundancy is maximally invisible, elision-plus-inference is the mechanism (bare references already do it), and Claude's "lossy-on-purpose" framing was overturned by Dev's follow-up into **normalization under the INFERENCE principle**: root both sides and compare, redundant `DracoType` becomes correctly invisible, a gate-2 failure becomes a true positive, and Dictionary still fails — its `DracoType` really is information until `rooted` can tell a foreign parent from a rooting one. **#44/#60 state:** record `git-record-2026-08-10-1845` written, closing both — but **no commit/push confirmation and no issue-close confirmation appears in the session data**; the two changes were combined into one commit (shared hunks in `Drake.scala`/`drake.dlt`), boundary named in the record for per-hunk staging if Dev wants two. **#51 state:** freeze declared expired on its own terms (frozen in favour of the parser, which now round-trips 90 types); recommendation accepted-in-principle but not started — **the derivation edge only** (`derivation` holds a type expression rather than a `TypeName`), Dictionary the single driving case, done when the `reference package` loss line reaches zero and `Dictionary.drake` reads `from {K, V}`; the rest of #51 (atomic `typeParameters`, `[String]` → `[TypeParameter]`, the reserved primitive set) explicitly not bundled. **Queued at the open end:** Dev's call on the #51 edge as a **Scala-first TypeDefinition shape change** (classpath is compile-time truth) — the one thing Claude declined to start on his own read; the `ScalaTarget` → `TargetScala` rename and Dev's promised explanation; the group-1 extraction re-scoped (placement **TypeDefinition, not Source** — #57 makes Source the bound of the varying side; timing after #61, four structural predicates first; `hasCodec` flagged as neutral-in-intent, Scala-in-definition); the `surfaceCarried` narrowing offer (hold out the one Map residual, verify packages on 111 of 112) superseded in spirit by the root-both-sides insight but never executed; wiring `DracoType` elision into the emitter/`rooted` (small, flags exactly one type today); the Dictionary host-interface-obligation step as its own future argument; and the incidental find — `src/mods/resources/domains/world/Cartesian.json` still carrying a sole `['DracoType']` derivation, outside #50 item 1's sweep, deliberately not filed. Backlog untouched this session: no issues opened, none confirmed closed in the data.
