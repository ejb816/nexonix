# Draco Dev Journal — Chapter 73

**Session date:** August 10–17, 2026 (session possibly ongoing)
**Topic:** The #44 endgame (session possibly still open): the kickoff prepared at chapter 72's close, re-issued fresh after a connection-killed false start, finds a complete #60 bracket implementation already sitting in the working tree — verified read-level, then 179/180 with the one failure being the `=` named-argument node the renderer couldn't spell, patched in one line → the Map recommendation ("don't give it a package") answered by Dev dissolving the question instead: `{K,V}` in drake and JSON, `Map[K,V]` minted entirely inside the Scala target (the `ScalaTarget` → `TargetScala` rename trailed for later) → the pilot in two gated steps — `scalaTypeExpression`/`targetTypes` provably inert at 514/514, then six valueTypes neutral, both Drake converters, and the arity guard that stops `{K, V}` becoming `Set[K, V]` → the five-question design dialogue: `from Map(K, V)` proven constitutive, not incidental; the expected post-#51 `Dictionary.drake` (`from {K, V}`, `DracoType` gone, `rooted`'s `nonEmpty` gate exposed as too narrow); the round-trip's blindness to redundancy worked through twice — Claude's "lossy-on-purpose" framing overturned by Dev's own question into normalization under the INFERENCE principle, with Dictionary the one true positive → "Is it time to get back to #51?" answered yes, narrowly: the derivation edge only, three independent findings converging on `derivation` holding a type expression → 514/514 with the full pilot in, the record written closing #44 and #60, and the one call Claude asked for — the Scala-first TypeDefinition shape change — answered by events instead: the commit broke on paste (the heredoc terminated early and zsh ran the message's own lines as commands, though #44 and #60 closed cleanly), was re-landed as `0acf2da` via `-F` from a file, and the heredoc retired for good → Dev's redirect: classpath-as-compile-time-truth to become a language-neutral `definitionPath` with the same semantic impact in Scala, Haskell, and TypeScript — ordered first-wins exposed as a JVM accident, roots become a set under unique-or-error, #37's structural `TypeName` equality promoted to prerequisite and landed (520/520), Generator's drifted duplicate loader deleted, then `DefinitionPath` itself (`roots: Seq[URI]`, the path a property of the runtime by Dev's confirmation, 117 generated companions untouched) landed 524/525 → 525/525, the one failure exposing a real defect — circe 0.14.1 has no `java.net.URI` codec and the codec gate's rule was too narrow → the type-parameters-in-type-names dialogue: abstractness is a free variable at any position at any depth, a constraint never makes a position concrete, the corpus obeys the biconditional 9 abstract / 31 concrete with zero exceptions, and parameters stay in the identity → #62 filed at Dev's direction (report-only tests should assert their numbers) and enriched on request, Claude's "sibling gap" claim corrected in the process → the dracoreview20260815.md comprehensive review re-ordering everything: DRACO.md proven months-stale (26 mentions of types existing in zero Scala files) and rewritten verified, Dev's standing rule fixed — a new issue only with his explicit approval — README rewritten language-neutral with a vocabulary table and an eight-row residues table, GETTING_STARTED split per target (Scala realized, Haskell and TypeScript stubs carrying their hardest questions), and CHANGELOG caught up 37 records with its trigger moved onto the git-record step — where the session, possibly still open, leaves off. Transcript.

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

---

**[Dev]** The commit did not work properly.

*(Pasted the terminal transcript: `git add -A` ran clean, then the pasted heredoc block broke — the shell's `>....` continuation prompt shows it terminating early, after which the commit message's remaining lines were executed as zsh commands, ending in `zsh: command not found: it`. Both issue closes nonetheless succeeded — `gh issue close 60` and `gh issue close 44`, each with its prepared comment — and `git push` answered "Everything up-to-date": no commit existed to push.)*

**[Claude]** The heredoc broke during paste — the terminal's `>....` continuation shows it, and once it terminated early the remaining message lines were handed to zsh as commands (`command not found: it`). The backticks and em-dashes are inert inside `<<'EOF'`, so this is paste handling on a long block, not quoting. The fix is to drop the heredoc entirely.

**Already done, don't re-run:** issues #60 and #44 are closed, and `git add -A` staged everything (including the record file). Only the commit and push are outstanding.

*(Extracted the commit message from the record file itself with `sed`; edited `git-record-2026-08-10-1845` to note the paste failure.)* Message extracted straight from the record, so there's no drift. And the record now carries a note recording what actually happened, since it's an audit trail.

Run this — three commands, no heredoc:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && git add -A && git commit -F /private/tmp/claude-501/-Users-edwardburke-IdeaProjects-nexonix/d5795217-d874-4024-9351-926b6293df3e/scratchpad/commit-msg.txt && git push
```

The `git add -A` is there again because I just edited the record file to note the paste failure — it needs re-staging.

The message file was extracted from the record itself rather than retyped, so the committed message and the audit record are the same bytes. The record keeps the full text and now carries a note explaining why the form below it wasn't the form used — worth having, since that block is the template you'd otherwise paste from next time.

For future records I'd write the message to a file first and have the block read it with `-F`. A message this long has no business going through a heredoc; there's nothing gained by inlining it and a whole class of paste and quoting failures avoided.

*(Delegated action — Dev ran the block the next day and pasted the result check: `[main 0acf2da] drake: the argument as an opener, and Map as a type expression (#44, #60)` — 17 files changed, 837 insertions, 58 deletions, the record file and `draco-dev-journal/draco-dev-chapter-73.md` among them; pushed `746de5b..0acf2da`.)* Landed and pushed clean: `746de5b..0acf2da`, working tree empty, #44 and #60 closed.

One thing rode along that wasn't mine: `draco-dev-chapter-73.md` was swept in by `git add -A`. That's Cowork's file and expected — just noting it so the 17-file count isn't a surprise.

Where this leaves the round-trip:

| | |
|---|---|
| gated | 80 draco + 10 mods types, both directions |
| loss | 16 fields, all owned |
| — expression form | 12 → #61 |
| — legacy `Seq()` | 3 → #55 |
| — reference package | 1 → #51 |

That last row is the whole of what's left, and it's one reference: `Dictionary`'s derivation on `Map`.

The standing question is still yours to answer — I asked it before the commit and don't want it lost in the mechanics. **#51's derivation edge: go?** My recommendation is that edge alone, with `Dictionary` as the single driving case, done when `reference package` reads zero and `Dictionary.drake` opens `from {K, V}` with `DracoType` gone.

The reason I'm not just starting: it changes what `derivation` holds, which makes it a `TypeDefinition` shape change — Scala-first by your rule, where the classpath is the compile-time truth. The last two steps were corpus data plus a renderer, and neither could break anything that wasn't already gated. This one can.

---

**[Dev]** I want the classPath as the compile time truth to become language neutral definitionPath, so I can use it for type definitions that generate other target languages. This should include the runtime capability of read access to definition sources embedded in the runtime libraries, for purposes of creating domain dictionaries, without having to generate any target language source code.

**[Claude]** That's a direct challenge to the premise I was cautioning on — "classpath is compile-time truth" is itself a host-bound claim, and you're saying replace it rather than defer to it. Let me ground this in what already exists before I respond to it. *(Read `TypeLoader`, the `Source` definition (#57), and Generator's `loadType`.)*

Both halves of what you're describing already exist — that's the useful finding here. What's missing isn't the capability, it's the *concept*, and the fact that it's currently spelled twice in JVM terms.

**Runtime read of embedded definitions** already works: `TypeLoader.loadFromResource` reads the `.json` out of the jar via `getResourceAsStream`, and every generated companion calls it at runtime. That's the capability you're asking for, already load-bearing.

**Domain dictionaries with no generated target source** also already works: `DomainBuilder.define(name, namePackage)` loads a domain's definition plus every member's full definition and returns a `DomainType` with a populated `typeDictionary`. No codegen for the domain being defined — it's exercised by `DomainBuilderTest` and `DracoActorTest` today.

So the work is naming and neutralizing an existing capability, not building one. Three things are actually wrong with it:

1. **It's spelled twice.** `getResourceAsStream` appears independently in `TypeLoader.scala:13` and `Generator.scala:36` — two loaders that must agree and nothing makes them.
2. **Resolution is JVM, though the path already isn't.** `TypeName.resourcePath` yields `/draco/Dictionary.json`, which is neutral in form; only the *lookup* is a classloader. That's a smaller gap than it sounds.
3. **There's a second, unrelated mechanism.** `SourceContent` reads definitions by `java.net.URI` + `fromFile` for tooling. Two ways to find a definition, no shared concept between them — which is precisely the concept you're naming.

**My recommendation: build the seam first and leave it inert.** Introduce `DefinitionPath` in main as a definition-backed type with a single realization — embedded-in-library, exactly today's behaviour — and route both `getResourceAsStream` sites through it. Zero behaviour change, suite stays green. Then the filesystem realization lands as a second realization and subsumes `SourceContent`'s loading role, and `DomainBuilder` promotes from mods to main reading through it. That's the shape that worked twice today: renderer first inert, corpus second.

One thing I'd want confirmed before writing it, because it determines the type rather than the code: **I'm assuming `definitionPath` is an ordered sequence of roots with first-wins shadowing** — the property worth keeping from classpath, and implied by your "libraries" being plural. A single root would be a materially different type. Correct me if it's the latter.

On your actual challenge: you're right that `classpath = compile-time truth` is a host-bound rule and shouldn't be the grounding one — this retires it. It doesn't dissolve the specific #51 caution though, since the Scala in `src/main/scala` still has to compile whatever `derivation` becomes. What it does change is that a *new* target would no longer need a JVM classpath to know the type graph, which is the thing that actually matters for #20 and #21.

#51's edge stays open and small; neither track blocks the other.

---

**[Dev]** From my current position, I would like DefinitionPath to have the same semantic impact in Scala, Haskell, TypeScript etc, as generated target languages.

**[Claude]** That reframes it, and it says my last question was the wrong one. Ordered first-wins is a *JVM accident* — Haskell's package db and TypeScript's module resolution shadow by different rules, so preserving classpath's order semantics would guarantee the opposite of what you're asking for. Let me check one thing that decides whether the alternative is even reachable. *(Checked `TypeName` for `equals`/`hashCode`.)*

Confirmed, and it matters: `TypeName` is a trait with plain `val`s and no `equals`/`hashCode` — reference equality only. That's #37, and it turns out to be a prerequisite rather than a nicety, for a reason I'll come to.

**First, the correction.** "The property worth keeping from classpath" was the wrong instinct — ordered first-wins isn't a property, it's an artifact of how the JVM happens to search. Preserving it would mean the same `DefinitionPath` resolves differently in Scala than in Haskell or TypeScript, which is precisely the semantic divergence you're ruling out. So order must carry *no* meaning, and roots are a **set**: a `TypeName` resolves to exactly one definition, or the path is ill-formed and says so. That's the only rule that survives all three hosts unchanged, because it refuses to depend on any of them.

**And that invalidates the seam I proposed.** I suggested one realization reproducing today's behaviour, inert, zero change. But today's behaviour *is* the JVM resolving — `getResourceAsStream(path)` asks the classloader to search and hands back whichever root it found first. Wrapping that in a neutral name gives you a language-neutral *name* over host *semantics*, which is the trap you just named. The seam has to change behaviour to be worth anything.

**The increment that actually carries the meaning is small.** Stop taking the first match; take *all* matches and require exactly one — `getResources` (plural) rather than `getResourceAsStream`. The JVM is then only reading bytes at roots it enumerates, while draco decides what resolution means. Haskell and TypeScript enumerate their own roots and apply the identical rule, and all three agree by construction rather than by coincidence. Small change, real semantic content, and testable.

It's also not risk-free in a way that's useful: if any `TypeName` currently resolves to two resources — `src/main/resources` alongside `target/classes`, or a test resource shadowing a main one — it starts failing loudly where it used to silently pick one. That failure would be information, and it's the kind of silent clobbering you already watch for on a case-insensitive filesystem.

**Why #37 becomes a prerequisite**, concretely rather than in principle. `TypeDictionary.kvMap` is `Map[TypeName, TypeDefinition]` — keyed by a type with reference equality. It works today only because one instance flows through the whole path. A `DefinitionPath` assembling definitions from two roots produces *distinct* `TypeName` instances for the same type, so collision detection would never fire and the dictionary would happily hold duplicate keys. Unique-or-error is unimplementable until `TypeName` compares structurally.

So my recommendation: **#37 first**, then the `getResources` change, then roots as an explicit set. #37 is small, it's been open since May, and it stops being cleanup the moment definitions can come from more than one place.

I'd like your go-ahead before starting — #37 changes equality on a type used as a map key throughout, which is the kind of change that surfaces in places neither of us predicts.

---

**[Dev]** You have my go-ahead

**[Claude]** Scoped narrowly with a fix sketch. Let me look at the current artifacts — this touches three (JSON, Scala, drake) and I have to predict generator output exactly. *(Viewed #37; surveyed the corpus for multi-line values — zero exist; read the Generator's `factoryBody`/`methodParameters` paths, `DracoGenTest`'s comparison mode, and the drake emitter's block rules; edited `TypeName.json`, `TypeName.scala`, `TypeName.drake`; created `TypeNameTest.scala`.)*

DracoGenTest compares rather than writes, so all three artifacts must be hand-matched. All confirmed. Three artifacts written plus a test. This changes equality on a type used as a map key throughout, so full suite:

```bash
sbt test
```

**What I did**, following #37's sketch: two `Dynamic` entries in `TypeName.json`'s `factory.body`, with the logic in `value` rather than a `Monadic` body — `methodBody` returns the value directly when body is empty, and it keeps the expression on one line. That last part isn't cosmetic: **there are currently zero multi-line values in the corpus**, and drake's surface is line-based, so a multi-line body would have broken the round-trip. The issue's sketch had embedded newlines.

Predicted emissions I had to match by hand, since `DracoGenTest` compares rather than writes:

```text
    override def equals(other: Any): Boolean = other match { ... }
    override def hashCode: Int = (name, namePackage, typeParameters).hashCode
```

and in `TypeName.drake`, `equals` opens a block (it has a parameter) while `hashCode` stays a leaf.

**Two things worth your attention.**

First, a semantic edge I made explicit rather than silent. Type parameters are part of the identity, so a reference `Dictionary(K, V)` is **not** equal to the declaration `Dictionary` — formals on the declaring type, actuals on references. Resolution is unaffected (`namePath` and `resourcePath` both exclude them), but `TypeDictionary.kvMap` is a `Map[TypeName, _]` keyed by declarations, so a lookup by parameterized reference misses. That follows the issue's sketch, and there's a test asserting it so it's a decision on record. If you'd rather identity meant "the same type" regardless of parameters, that's a one-line change and I'd want to make it before `DefinitionPath` depends on it.

Second, the neutrality debt, since you just set that bar. This writes host-opaque Scala into the normative corpus — `match`, and the JVM names `equals`/`hashCode`. Haskell and TypeScript get nothing from it. The neutral statement is *"TypeName's identity is structural"*, a property that each target projects (Scala equals+hashCode, Haskell `deriving Eq`). That's a TypeDefinition-level property and a bigger move than you approved, so I took the scoped path — but it's the same shape of mistake we spent this afternoon undoing with `Map`, and it'll want revisiting before there's a second target rather than after.

I've left the five `.namePath == .namePath` workarounds in place. They're harmless and correct, one of them is in generated code (`GeneratorCLI`) so reverting means editing its JSON, and I'd rather confirm equality works before touching five more files.

*(Delegated action — Dev ran `sbt test`: 520 tests run, 38 suites, all passed — with one compiler warning at `TypeNameTest.scala:42`, "draco.TypeName and String are unrelated: they will most likely always compare unequal", and the note "I'm not sure what to think about the warning above but everything else passed.")* The warning is correct and it's mine — the test was sloppy, not the code. Scala's lint fires because both operand types are statically known and unrelated, so it's telling me the comparison can't be meaningful *as written*.

The test is still worth keeping, though: it pins the `case _ => false` branch, and without that branch a non-`TypeName` argument would throw a `MatchError` rather than answer false. The fix is to hide the static type, which is also the honest shape of the scenario — equality is called with `Any`, not with a known `String`. *(Edited `TypeNameTest.scala`.)*

One test file, no production change:

```bash
sbt "testOnly draco.TypeNameTest"
```

That should return the build to zero warnings — the compiler reported exactly one, and it was mine.

On the substance: **520/520 with structural equality in place**, and nothing broke. That's the outcome I was least sure of, since changing equality on a map-key type is the kind of thing that surfaces somewhere unrelated. It didn't — which also tells us something useful, namely that the five `.namePath == .namePath` workarounds had already absorbed every place it mattered.

That clears #37 and unblocks the sequence you approved. Next is the `getResources` change: stop taking the first match, take all matches and require exactly one. That's the increment where draco starts deciding what resolution means instead of inheriting the JVM's answer, and it's the one that can fail loudly — if any `TypeName` currently resolves to two resources on the classpath, it'll say so. That failure would be worth having.

Two things I'd still like to settle, both from the last message and neither urgent enough to block that step:

- Whether type parameters belong in `TypeName`'s identity. Currently they do, so a reference `Dictionary(K, V)` doesn't equal the declaration `Dictionary`. It's tested and on record, but `DefinitionPath` will lean on this equality, and it's cheaper to change now than after.
- Whether to revert the five workarounds to plain `==`. Pure cleanup, and one of them lives in generated code, so it means editing `GeneratorCLI.json` rather than the Scala.

I'd take the `getResources` step next and leave both of those; say if you'd rather clear them first.

---

**[Dev]** The type parameters as part of a type name in draco implicitly indicate whether or not the type is  abstract or concrete. If any type parameter is unbound in the type name, then there can be no value instance for that type and it remains abstract. Only type names with all parameters bound can be the derivation for an atomic term equivalent to that type. Let me know if this is consistent with how we expect to used parameterized types in type definitions.

**[Claude]** That principle has a direct consequence for what we just landed, and it's checkable against the corpus. Let me survey how parameters are actually used. *(Ran three corpus surveys over every declaration and parameterized reference in both trees.)*

Let me correct that classification before I answer — my script matched parameter strings literally, and three cases are mis-binned by it.

Yes — consistent, and more strictly than I expected. The corpus already obeys this rule everywhere, without it ever having been written down.

Across 40 parameterized references in both corpora:

- **9 abstract** (a parameter contains a free variable), **31 concrete** (all parameters closed)
- The biconditional **holds with zero exceptions**: a reference carries a free variable *iff* its owner declares formals. No concrete type derives from an unbound parent; no abstract type has its variables silently closed.
- **Zero under-applied references** — nothing names a parameterized type without supplying parameters, so there's no third, implicit state.

Your "atomic term" clause holds too: every one of the 31 fully-bound references sits on a type with no formals of its own — `Meters from Distance(Double)`, `Radians from Rotation(Double)`, `Nominal from Primal(String)`, `JSON from Format(JSON)`. Fully bound and atomic go together in every case.

**But the test isn't expressible against the data as it's currently shaped**, and that's the finding worth having. My first pass mis-binned 3 of the 40, in three different ways:

- `Coordinate` declares `"T <: Product"` — name and bound fused into one string, so it never matches the reference's `"T"`. The bound constrains the variable; it isn't part of its identity.
- `DomainTransform` and `TypeTransform` reference `Holon((S, T))` — the parameter is a *compound*, so "is this parameter a formal?" is the wrong question. You have to look inside it.
- And generally `Primal(Seq[Waypoint])`, `Primal((Gaze, Distance))` — nested expressions only distinguishable from the above by parsing them.

"Is any parameter unbound?" is a free-variable question over a type expression. `typeParameters: Seq[String]` can't answer it — I had to regex identifiers out of the strings to get the numbers above, which is a heuristic, not a decision procedure. So this promotes #51's `[String] → [TypeParameter]` from tidiness to a **semantic requirement**: abstract-vs-concrete is a property draco should be able to compute, and today it can't.

It also settles the equality question I left open — **keep type parameters in the identity**. `Dictionary(K, V)` and `Dictionary(TypeName, TypeDefinition)` aren't two spellings of one thing; one is abstract and one is concrete, and by your rule only the second can be a derivation for an atomic term. They should compare unequal.

With one caveat that argues the same way: equality compares those parameters as raw *strings*. So `Holon((S, T))` and `Holon((S,T))` differ on whitespace alone, and a declaration's `T <: Product` never equals a reference's `T`. Structural equality over an unstructured field is only as good as the spelling — and `DefinitionPath` comparing names across independently-authored roots is exactly where that would bite.

---

**[Dev]** I may have used "bound" incorrectly or ambiguously. What I meant was, the presence of a type "variable"  T, indicates that there is no concrete type in that position in the type parameter list, regardless of whether "<:" or  ">:" are part of the type expression in that position.

**[Claude]** That's the reading I computed — I stripped `<:` / `>:` to get the variable's *name*, then asked whether a position contains one of the owner's variables. So nothing shifts: 9 abstract, 31 concrete, biconditional holds with no exceptions. *(Re-ran the survey counting constraints by site.)*

Worth naming the collision so it doesn't recur, since "bound" carries two unrelated meanings right here: *bound* as in a variable bound by a binder, and *bound* as in `<: Product`. Your rule is about neither — it's whether the position holds a **variable** or a **concrete type**, and a constraint doesn't move it either way.

One datum that confirms your model independently: constraints appear in **6 declarations and 0 references**. The corpus already treats a constraint as belonging to the declaration site only — which is exactly right under your rule, since a constraint restricts what may be *substituted for* a variable and says nothing about whether the position is concrete.

And the property is recursive, not positional: `Holon((S, T))` is abstract because a variable appears anywhere inside the parameter, not because the parameter *is* one. That's the part no amount of string matching reaches.

Which hands `TypeParameter` its shape for #51 fairly directly:

- a **position** holds either a variable or a type expression
- a **variable** is a name plus an optional constraint (`<:` / `>:` with a type expression)
- abstractness is "does any position contain a variable, at any depth"

Today `"T <: Product"` fuses the variable's identity and its constraint into one string, which is why the declaration `T <: Product` and the reference `T` don't compare equal even though they're the same variable. Splitting those two is what makes both the equality and the abstract/concrete test work by construction rather than by regex.

---

**[Dev]** Does it seem we are in sync, conceptually, on type parameters in type names? Can we proceed  to the next step?

**[Claude]** Yes — in sync, and I can state the model back to check rather than assert it:

- a `typeParameters` entry is a **position**, holding either a variable or a concrete type expression
- a variable may carry a constraint; the constraint restricts substitution and never makes the position concrete
- a type is **abstract iff any position contains a variable at any depth** — recursive, not top-level
- only a fully concrete name can be the derivation for an atomic term
- therefore parameters are identity-bearing, and abstract/concrete variants of one base name are different terms

One sub-question I'd rather flag than silently decide, and it doesn't block: whether a variable's *constraint* is part of its identity, i.e. whether the declaration's `T <: Product` and a reference's `T` denote the same variable. Your framing says the constraint isn't the identity, which implies they should compare equal — but that's a `TypeParameter` question, and it lands when #51 builds that type, not now.

Proceeding to the `getResources` step. *(Read Generator's private loader; edited `TypeLoader.json`/`.scala`/`.drake` and `Generator.scala`.)*

Generator has a **complete duplicate loader** — its own `loadFromResource`, `tryLoad`, and even its own `resourcePath` that differs from `TypeName.resourcePath` on the empty-package case (`//Name.json` vs `/Name.json`). So the change has two halves, or half the system keeps first-wins. `"URL" -> "import java.net.URL"` is in the import table, so the bare name is the established pattern (same as `URI` in `SourceContent`). Three artifacts written, then the deduplication — Generator's private loader must go, or half the system keeps first-wins.

Full suite — this changes how every definition in the system is found:

```bash
sbt test
```

**What this step actually does.** Resolution stops being the JVM's answer and becomes draco's:

- `definitionSources(resourcePath)` enumerates **every** root carrying that path — `getResources`, plural, rather than `getResourceAsStream`, which returns whichever the classloader reached first.
- `loadFromResource` then requires **exactly one**: none is still a stub (`DomainBuilder.isStub` depends on that), one reads, and two or more is an error naming the sources. Haskell and TypeScript enumerate their own roots and apply that same rule, so the three agree by construction rather than by coincidence.
- `Generator`'s duplicate loader is gone, delegating to `TypeLoader`.

That last part wasn't tidiness. The two loaders **had already drifted**: `Generator.resourcePath` built `/$np/$name.json` unconditionally, so an empty package gave `//Name.json` where `TypeName.resourcePath` gives `/Name.json`. Nothing caught it because nothing in the corpus has an empty package — a divergence waiting for the first type that did. Two loaders can't both be the definition path.

**What might fail, and it would be informative.** If any `TypeName` currently resolves to two resources on the test classpath, this now says so instead of silently picking. I don't expect it — sbt copies resources to one output directory — but that failure is the whole point of the change, so if it fires, read it before assuming it's a bug.

**What I predicted and could have wrong**, since `DracoGenTest` compares generated Scala byte-for-byte: I added `import java.net.URL` after the circe imports, on the basis that external imports come last in `typeImports` and that `URL` is in the lookup table. If the ordering differs the test will name it and it's a one-line fix.

I also removed `parser` from Generator's imports — it was used only inside the loader I deleted, so it would otherwise be an unused-import warning, and the last run was clean apart from mine.

*(Delegated action — Dev ran `sbt test`: "All tests passed".)* Green. So no `TypeName` resolves to two roots on the current classpath, and the import prediction held.

Let me get this landed before opening the next front — last time three uncommitted changes meant three candidate causes for any failure. *(Checked `git status`; wrote `git-record-2026-08-12-2110` and `git-record-2026-08-12-2115`.)* Clean split by path this time — no shared files, so two commits are genuinely feasible.

Two records written. Paste as one block — `-F` from a file throughout, per the heredoc lesson:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-12-2110 | sed '1d' > /tmp/msg-a && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-12-2115 | sed '1d' > /tmp/msg-b && git add draco-git-record/git-record-2026-08-12-2110 src/main/resources/draco/TypeName.json src/main/resources/draco/TypeName.drake src/main/scala/draco/TypeName.scala src/test/scala/draco/TypeNameTest.scala && git commit -F /tmp/msg-a && git add draco-git-record/git-record-2026-08-12-2115 src/main/resources/draco/TypeLoader.json src/main/resources/draco/TypeLoader.drake src/main/scala/draco/TypeLoader.scala src/mods/scala/draco/Generator.scala && git commit -F /tmp/msg-b && git push
```

Then, separately, so a failed commit doesn't swallow it:

```bash
gh issue close 37 --comment "TypeName compares structurally, authored via TypeName.json's factory body and covered by TypeNameTest. Type parameters are part of the identity — abstract and concrete variants of one base name are different terms. The five .namePath == .namePath workarounds are left in place: they are correct, and one is in generated code. Equality still compares parameters as strings, which is #51's [String] -> [TypeParameter]."
```

**Where the DefinitionPath track stands.** Two of the three steps you approved are done: structural equality, then single-resolution. What's left is the one you named originally — **roots as an explicit set**.

That's the step that actually delivers what you asked for, and it's worth being clear that the last change didn't. Resolution is draco's now, but the *roots* are still whatever the host classloader happens to expose. "Read access to definition sources embedded in the runtime libraries" is still a JVM property rather than a draco one — a Haskell target has no classloader to inherit roots from, so there's nothing yet for it to project.

Making roots explicit is where `DefinitionPath` becomes a type rather than a pair of functions, and it's the first of these steps that adds a definition to the corpus rather than changing one. I'd want to agree its shape with you before writing it — in particular whether a root is a URI, and whether `DomainBuilder` promoting out of `src/mods` belongs in the same step or the one after.

Two smaller things still on the table, neither blocking: reverting the five `.namePath` workarounds, and #51's derivation edge, which is still the last entry in the drake loss report.

*(Delegated action — Dev ran both blocks and pasted the result check: `[main c76bf09] TypeName identity is structural (#37)` — 6 files, 206 insertions, both record files and `TypeNameTest.scala` created; `[main f62089b] One resolution, and it is draco's rather than the JVM's` — 4 files, 56 insertions, 26 deletions; pushed `0acf2da..f62089b`; #37 closed with the comment.)* Both commits are correct in content and the tree is clean. One blemish worth noting for next time: **`git-record-...-2115` landed in commit A rather than B.** Your IDE auto-adds new files to VCS, so it was already staged, and `git commit` commits the whole index regardless of what the preceding `git add` listed. Scoping the commit needs a pathspec on the *commit* — `git commit -F msg -- <paths>`. Not worth rewriting pushed history over a record file's placement; both messages are accurate about their own change.

Before I propose a shape for the roots, one number that decides the main design question. *(Counted generated companions calling `TypeLoader.loadType`.)* That settles the main design question by itself.

**The crux.** `TypeLoader.loadType(typeName)` takes no path, and **117 generated companions** call it that way (166 sites in total). So either the path is threaded — changing every generated file and requiring `Generator` to emit a path argument, which means each companion needs one in scope — or it's ambient, and generated code doesn't move at all.

**My recommendation, in one shape:**

- **A root is a URI.** It's how `Main.roots` and `SourceContent` already express location, and `getResources` yields URLs that convert. I'd not model library identity separately: a jar URL is already `jar:file:/…/foo.jar!/draco/X.json`, so the library is recoverable from the URI — including in the collision error, which is the place it matters.
- **`DefinitionPath` is a draco type**: a set of roots, plus resolution with the unique-or-error rule that just landed. A set, not a sequence — order can't survive projection.
- **One ambient default path**, populated by enumerating the host's roots, with explicit paths constructible alongside it. The roots are then *held* rather than *searched*: inspectable, extendable, and constructible with no classloader in sight. That last part is what makes it projectable — a Haskell target has no classloader to inherit from, so a path it can be *given* is the only version that means anything there.
- **`DomainBuilder` promotion is the step after.** Dictionary assembly is a separate concept from definition resolution, and bundling them would blur which one the commit is about.

The only thing I'd like a yes-or-correct on is the ambient default, since it's binary and everything else follows: **the path is a property of the runtime, not a parameter of each lookup.** It keeps 117 generated files still and makes the roots explicit at the same time — but it does mean resolution reads ambient state, which is a real cost and the reason I'd rather you chose it than have me assume it.

---

**[Dev]** Your assertion "the path is a property of the runtime" seems correct to me, and indicates the direction I expected for this.

**[Claude]** Confirmed — building it. One thing to verify first, because the default path's roots come from the JVM's classpath and sbt only exposes that faithfully to a forked JVM. *(Verified `fork := true` in `build.sbt`; created `DefinitionPath.json`, `DefinitionPath.scala` (hand-predicted generator output), and `DefinitionPath.drake`; registered the type in `Draco.json`/`.scala`/`.drake`; rewired `TypeLoader` onto the path.)*

`DracoGenTest` auto-discovers resource paths, so a new JSON automatically demands a matching hand-written Scala. Four artifacts written — the Scala is my best prediction of generator output, and the test's diff will correct it if I'm off. Now `TypeLoader` delegates to the path — the uniqueness rule moves onto `DefinitionPath` where it belongs.

Full suite:

```bash
sbt test
```

**What this delivers.** `DefinitionPath` is now a type in the corpus, holding `roots: Seq[URI]` explicitly and owning resolution — `sources` finds every root carrying a path, `source` applies the unique-or-error rule. `TypeLoader.loadFromResource` is one line delegating to `DefinitionPath.default`, so all 117 generated companions are untouched. The path is a property of the runtime, as you confirmed.

The point of the shape: a path can now be **constructed** rather than only discovered. `DefinitionPath(someRoots)` needs no classloader, which is what makes it mean anything on a host that hasn't got one. `hostRoots` is just how the *default* is populated — one realization, not the definition.

**Three things I'd watch on this run**, in the order I think they're likely:

1. **The generated Scala.** `DefinitionPath.scala` is a new file I hand-predicted — companion boilerplate, import order (`URI` then `URL`, per the table order that `SourceContent` follows), `Null` shape. If any of that is off, `DracoGenTest` prints both normalized versions and it's a mechanical fix.
2. **Root derivation under sbt.** `hostRoots` reads `java.class.path`, which is only faithful in a forked JVM — `fork := true` is set, so this should hold, but it's the assumption most likely to be wrong in a way that fails everything at once rather than subtly.
3. **Cost.** Resolution now probes every classpath root per lookup instead of asking the classloader once: roughly 50 roots × 117 types. Jar lookups are cached by the JVM after first open, so I expect this to be unmeasurable, but a visibly slower suite is the signal.

One deliberate scope call: I did **not** promote `DomainBuilder` out of `src/mods`. Dictionary assembly is a separate concept from definition resolution, and it's cleaner as its own step once this one is green.

*(Delegated action — Dev ran `sbt test` and pasted the run under the header "One error:" — 524 of 525 passed, the one failure `DracoGenTest` on the new `DefinitionPath.scala`.)* 524/525 — one failure, and the diff is precise. Two things I got wrong: **a codec is generated** (factory params ⊆ element names, so `simpleCodecDeclaration` fires — `SourceContent` escapes this only because its params aren't a subset), and the globals come *last*, after `Null`.

That codec needs `Encoder[URI]`/`Decoder[URI]` to exist, which the test can't tell me since it never compiles generated output. Checking. *(Located the circe-core 0.14.1 jar in the Coursier cache and searched it; read the codec gate and `codecDeclaration`; surveyed the corpus for other host-typed factory parameters; edited `Generator.scala`'s gate and `DefinitionPath.json`'s default value; re-predicted `DefinitionPath.scala`.)*

Decisive: **circe 0.14.1 has no `java.net.URI` codec at all** — no `encodeUri`/`decodeUri`, no reference to `java/net/URI` anywhere in the jar. So the generated codec wouldn't compile. The gate already excludes function-like params because "those have no circe codec" — `URI` is the same problem, just not enumerated. That's a real generator defect my type happened to expose: **any** definition with a `URI`/`URL`/`File` factory param would emit uncompilable code. The only other codec-bearing type with such a parameter is `Rule` (`Consumer[Knowledge]`, `Consumer[RhsContext]`), and both are *already* excluded as function-like. So the fix changes exactly one type's output — mine.

```bash
sbt test
```

**The one failure was worth having** — it exposed a real generator defect, not just my bad prediction.

The codec gate refused function-like parameters "because those have no circe codec," which is the right *reason* attached to too narrow a *rule*. What disqualifies a parameter isn't being a function, it's having no instance. I verified against the jar: **circe 0.14.1 contains no `java.net.URI` codec and never mentions the type.** So any definition with a `URI`, `URL`, or `File` factory parameter would have emitted a codec that cannot compile. Nothing in the corpus had one until now, which is why it sat unnoticed — and `DracoGenTest` compares text rather than compiling generated output, so it wouldn't have caught it either.

The fix reads the host types off `externalTypeImports`, which is already the generator's list of types it doesn't own, with `Json` excepted as circe's own. I checked the blast radius before touching it.

Also corrected from the diff, and one thing the report caught that I'd have missed:

- No codec block, so no circe imports — just `URI` and `URL`
- Globals emit **after** `Null`, not before
- `Null` is `apply()`, since the parameter has a default
- The factory default is now the canonical `Seq.empty` tree rather than the legacy string `"Seq()"` — the loss report went `[empty-collection spelling: 3 → 4]`, which was **me adding a fourth instance of #55** while writing new corpus data. Worth watching that number on new definitions; it's the only thing that flagged it.

Everything else passed, including the two risks I flagged: `hostRoots` resolved correctly under forked sbt, and the suite ran in 10s — same as before, so probing every classpath root per lookup costs nothing measurable.

*(Delegated action — Dev re-ran `sbt test`: all tests passed.)* 525/525. `DefinitionPath` is in. *(Checked `git status`; wrote `git-record-2026-08-13-1030`.)*

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-13-1030 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

**Where the DefinitionPath track stands.** All three steps you approved are done:

| | |
|---|---|
| structural equality | `TypeName` compares by content — the prerequisite for detecting a collision at all |
| single resolution | unique-or-error, and Generator's duplicate loader gone |
| explicit roots | `DefinitionPath` holds `roots: Seq[URI]` and owns resolution |

What you originally asked for is now true: definitions embedded in runtime libraries are readable at runtime, domain dictionaries assemble from them with no generated target source, and the concept that finds them is draco's rather than the JVM's. `hostRoots` is one realization of a default, not the definition — which is the part that makes it mean anything on a host with no classloader.

**Three things open, none blocking, in the order I'd take them:**

1. **`DomainBuilder` promotion** out of `src/mods`. It already does dictionary-from-definitions with no codegen; what's missing is that it lives in the speculative layer and calls `Generator.loadType` rather than reading a path. It's the natural completion of this track.
2. **#51's derivation edge** — still the last entry in the drake loss report, and the same fix as `Dictionary`'s double spelling and `rooted`'s narrow gate.
3. The five `.namePath` workarounds — pure cleanup.

One observation worth carrying forward: the loss report caught me adding a legacy `Seq()` to new corpus data, and the codec gate produced uncompilable code that no test would have caught by compiling. Both were found by report-only output rather than by an assertion. Worth remembering when the next new definition goes in — read the numbers, not just the pass/fail.

---

**[Dev]** Create an issue that suggests updating report-only tests to have assertions that reflect expectations of correct output in reports.

**[Claude]** Let me ground it in the actual set rather than describe it abstractly. *(Surveyed the report-only set — six tests across four files — captured their current numbers from the test output, wrote the issue body, ran `gh issue create`.)*

Six of them, across four files. Confirmed the fix landed: **16 fields** now, down from 17. Writing the issue.

Created: #62 — *Report-only tests should assert their expected numbers, not just print them*.

The body is grounded in the concrete miss rather than the general principle: new corpus data pushed `[empty-collection spelling: 3 → 4]`, the suite passed, and the only thing that caught it was reading the headline on the way past.

Two design points I argued for rather than left open, since they're where this kind of issue usually goes wrong:

- **Assert per category, not on the total.** This afternoon the total moved for a legitimate reason (the mods corpus entering the walk) at the same moment a category moved for an illegitimate one. A total-only assertion would have been updated wholesale and would have buried the regression inside a change that genuinely needed the number raised.
- **Exact equality, not `<=`.** A ratchet that only catches regressions lets improvements accumulate silently until the baseline drifts away from reality and nobody trusts it. Exact equality makes every movement force a deliberate edit — and the edit is where the reason gets recorded.

It also notes that corpus growth *will* make these fail, and that this is the intended behaviour rather than a cost — a new definition adding to a known tail is exactly the event worth interrupting on.

Each nonzero baseline is tied to its owning issue (#55, #61, #51, #42) so the numbers read as debts with owners rather than magic constants. Labelled `cleanup`; I didn't add `priority-next` — say if you want it flagged as the next pickup.

The `DefinitionPath` commit block from before is still unrun, if you want that landed first.

*(Delegated action — Dev ran the commit block and pasted the record result check: `[main db87dbd] DefinitionPath: the roots are held, not searched` — 11 files, 215 insertions, 25 deletions; pushed `f62089b..db87dbd`.)* Landed clean — `f62089b..db87dbd`, 11 files, working tree empty.

**Four commits pushed today**, and they form one arc:

| | |
|---|---|
| `0acf2da` | the argument as an opener (#60), and `Map` as a type expression — closed #44 |
| `c76bf09` | `TypeName` identity is structural — closed #37 |
| `f62089b` | one resolution, and it is draco's rather than the JVM's |
| `db87dbd` | `DefinitionPath` — the roots are held, not searched |

The first was the drake round-trip closing out. The last three were your `definitionPath` direction, and they landed in the order you set: equality first (without it a collision can't be detected), then resolution, then the roots themselves.

**Open, in the order I'd take them:**

1. **`DomainBuilder` promotion** out of `src/mods` — the natural completion of this track. It already assembles dictionaries from definitions with no codegen; what remains is that it lives in the speculative layer and calls `Generator.loadType` instead of reading a path.
2. **#51's derivation edge** — `Dictionary`'s `from Map(K, V)` becoming `from {K, V}`. Still the single `reference package` entry in the loss report, and the same fix as the double spelling and `rooted`'s narrow gate.
3. **#62** — baselines on the report-only tests.
4. The five `.namePath` workarounds — cleanup.

One thing worth saying about today rather than leaving implicit: the two defects that mattered were both found by *reading numbers*, not by assertions — the legacy `Seq()` in new corpus data, and a codec gate emitting code that could not compile. #62 addresses the first kind. The second kind is different and still uncovered: `DracoGenTest` compares generated Scala as text and never compiles it, so a generator change can produce uncompilable output for any type whose hand-written counterpart hasn't been updated to match. `GenerateAndCompileTest` compiles three groups, not the corpus. I'm not proposing anything — just flagging it, since it's the sibling gap and you'd want to decide whether it's worth closing rather than have me open another issue for it.

---

**[Dev]** Your "One thing worth saying..." narrative seems like it belongs in the #62 issue description, beyond just informing me in your response.

**[Claude]** Fair — it's the same family as #62 (a gate that observes without verifying), and folding it in beats opening another issue. Let me verify the claims before they go into a durable record. *(Checked whether `DracoGenTest` and `ExampleDomainsGenTest` ever compile generated output; counted `ExampleDomainsGenTest`'s match/differ split; updated the issue body via `gh`.)*

Before I write that into a durable record, I checked it — and my claim to you was wrong.

Correcting what I told you: **generated Scala for the draco corpus *is* transitively compile-checked.** `DracoGenTest` pins every generated file to a hand-written one — the only exclusions are the `TypeElement` family, which the multi-type test covers against `TypeElement.scala` — and sbt compiles all of those. So a generator change producing uncompilable output fails loudly rather than slipping through. My "sibling gap" was overstated.

The real uncovered surface is narrower and lands inside #62 rather than beside it: **`ExampleDomainsGenTest` reports 28 match / 20 differ over 48 example-domain types, and never compiles any of it.** Those 20 aren't pinned to anything.

#62 updated with two sections.

**"Why these reports carry more weight than they look like they do"** — the shared root of both defects. Neither was wrong code a gate failed to check; both were code paths nothing in the corpus had ever exercised. The `Seq()` spelling, and a codec gate that would emit an uncompilable codec for any parameter circe can't encode, latent until the first `URI` parameter appeared. A gate only verifies what the corpus happens to exercise, and these six numbers are the instrument that notices when the corpus changes shape underneath the gates.

**"What this is NOT — so nobody fixes the wrong thing"** — this is the part I'd have got wrong if I'd written it straight from my message to you. Recorded explicitly: generated Scala *is* transitively compile-checked (pinned to hand-written files, which sbt compiles), so a future reader doesn't waste effort adding compilation where the guarantee already holds.

And that isolates the one place it genuinely bites. `ExampleDomainsGenTest`'s 20 differing outputs are pinned to nothing — no assertion, no compiled counterpart. It's the only one of the six whose number stands for genuinely unverified output rather than a known, owned tail, so the issue flags it as needing different treatment: a baseline records the drift, but only compiling would say whether those 20 are wrong or merely different.

Worth noting the sequence, since it's the second time today: I stated something confidently, checking it before it became durable showed it was wrong, and the corrected version turned out to be *more* useful than the original — it names a specific target instead of a vague gap.

---

**[Dev]** Since #62 is not a priority yet, let's continue with what the current priority. What does that seem to be, since that last commit/push?

**[Claude]** Let me check the backlog's own answer rather than mine. *(Listed issues by `priority-next` — empty.)*

Nothing is flagged — `priority-next` went with #44 and #60, both closed today. So the backlog doesn't answer it; the work does.

**The priority is the unfinished half of your own stated requirement.** You asked for definitionPath so you could read definitions embedded in runtime libraries *"for purposes of creating domain dictionaries, without having to generate any target language source code."* Three commits delivered the reading. The dictionary half is still sitting in the speculative layer.

Concretely, `DomainBuilder` (`src/mods/scala/draco/DomainBuilder.scala`) says so itself in its own header — it exists because:

```text
// TypeDictionary.apply, today:
override lazy val elementTypes: Seq[TypeDefinition] =
  _domainDefinition.domainAspect.elementTypeNames.map(name =>
    TypeDefinition(TypeName(name, _namePackage = ...)))    // names, no content
```

A concrete `TypeDictionary` holds member *names* but not member *definitions*. `DomainBuilder` is a `src/mods` stand-in supplying the populated counterpart, and its doc names the promotion path explicitly: when core's dictionary instantiation becomes comprehensive, the file moves from the mods `draco` tree to the main one.

That's now possible in a way it wasn't this morning, because there's a path to read through. And the change may be very small — `TypeDefinition(TypeName(...))` becoming a load through `TypeLoader`, in `TypeDictionary.json`'s factory body. `elementTypes` is a `lazy val`, so members load on access rather than at construction, which is what keeps `Domain[Draco]` from pulling the whole dictionary in during companion initialization.

Two things that fold into it rather than competing with it:

- **#47** ("promote `isStub` to a shared main-tree predicate") is literally a piece of this promotion — `isStub` lives in `DomainBuilder` and distinguishes a named-but-unauthored member, which only matters once dictionaries populate.
- **#41** ("complete mods domain dictionaries via discover") is downstream of the same capability.

So: **promote dictionary population into `TypeDictionary`, then retire or thin `DomainBuilder`.** I'd want to size it before committing to a shape — the one-line theory is a hypothesis, and the recursion question deserves checking rather than assuming.

Shall I scope it? And separately, worth setting `priority-next` on something once you've decided, since nothing currently carries it.

---

**[Dev]** @"/Users/edwardburke/Downloads/dracoreview20260815.md"
This is a comprehensive review of the DRACO framework project. Use this as fundamental resource for continuing our development.

**[Claude]** This reorders things, and it directly challenges the priority I named. Let me verify a sample of its falsifiable claims before I treat any of it as settled — starting with the ones about files I touched today. *(Sampled the review's falsifiable claims against the tree.)*

I sampled its falsifiable claims. **It holds up**, including on files I edited today:

| claim | verified |
|---|---|
| §4.1 `Draco.json` omits `Local` | Confirmed — `Local.json` exists, undeclared. (`DefinitionPath` I added today *is* correctly declared.) |
| §4.4 `CLI.version` stale | Confirmed — prints `alpha.1`, build is `alpha.5` |
| §5 `AGENTS.md` not a symlink | Confirmed — regular file, Jun 14, vs `DRACO.md` Jul 22. `CLAUDE.md` *is* a symlink. |
| §5 `DRACO.md` describes non-existent architecture | Confirmed hard — `TypeInstance`/`DomainInstance`/`RuleInstance`/`typeInstance` appear **26 times** in `DRACO.md` and in **zero** Scala files |
| §4.15 `org/nexonix` dead | Confirmed — 18 files, referenced by nothing in draco/mods/test |

Two calibrations. **Rows 6 and 11 of §4 are garbled** — a `||` operator broke the markdown table, so those two defects are unreadable and need re-reading from the source. And minor drift: the loss report is 16 fields across **91** types now, not 90, and `DRACO.md` doesn't actually mention `loadRuleType` (that's `AGENTS.md`).

**It changes my answer, and shows why it was premature.** I said the priority was promoting `DomainBuilder` into `TypeDictionary`. The review's §8 Q1 — *"Is mods now the engine tier, or still speculative outer layers?"* — is precisely the decision that gates it, and I'd presumed an answer. §3.6 makes the case that the split has already inverted in practice: the entire engine is hand-written in mods, and `DomainBuilder` exists solely because core builds hollow dictionaries. So that promotion is either right or exactly backwards depending on a call only you can make.

**My recommendation is the review's #1: rewrite `DRACO.md` first**, and I have first-hand evidence rather than just agreement. I have been operating from it all day. It told me `TypeElement extends Primal[String]` (actual: `Primal[Json]`), that Generator owns loading (actual: `TypeLoader`), and that companions carry `typeInstance` (actual: `dracoType`). I caught each only by reading source. A session that trusted it would propagate the stale model into the corpus — and that file is the input to every future session, including every one where I'm the one being steered.

One live conflict worth resolving in that rewrite, because it's a contradiction I'm currently subject to: `DRACO.md` instructs "file an issue for deferred work," while memory carries your restraint rule that says don't open issues unprompted. Today I followed the memory and asked first. Those should agree.

The four questions in §8 are yours, and I won't guess at them — mods-vs-core, the deferred presence/codec pair, #61 lint-vs-trees, and whether alpha.6 plus `bin/draco-drake` comes before #51. The first one unblocks the most.

---

**[Dev]** Once DRACO.md (symlinked with CLAUDE.md) is updated, shouldn't it be updated every session after that?

**[Claude]** Yes — but "update it every session" is exactly the discipline that just failed for four months, so I'd rather not answer with more of it.

The root cause is structural: **prose is the one part of this system with no gate.** Generated Scala is pinned to hand-written by `DracoGenTest`. The drake surface is pinned by two round-trips. Definitions are pinned by the corpus. Docs are pinned by attention — and we established this morning, with #62, that attention is exactly what doesn't scale.

Let me test whether the draco-shaped fix is viable rather than just propose it. *(Ran a resolution check over every backticked capitalised token in `DRACO.md`.)*

I tested the obvious gate and **it doesn't work**: 115 of 165 backticked capitalised tokens in `DRACO.md` don't resolve, but almost all are English prose (`The`, `Only`, `Key`) or legitimate host types (`Boolean`, `Seq`, `Map`, `URI`, `ExtensibleBehavior`). A blanket lint is pure noise.

But the signal is sitting inside that list. The genuinely stale ones — `TypeInstance`, `DomainInstance`, `RuleInstance`, `ActorInstance`, `Extensible`, `DataModel`, `Alpha`/`Bravo`/`Charlie`/`Delta`, `PrimeOrdinal`, `Polar`, `Spherical` — share one property: they are **retired names**. Types that existed and were deleted. Every major drift the review found is that same failure: a removal landed in code and the doc kept the name.

That's a precise, zero-noise gate — assert no doc mentions a retired name, and add to the list when you retire something, at the moment you have the context.

**And there's decisive evidence for your instinct, from this repo.** Two artifacts describe the same fact:

- `MEMORY.md` — *"`dracoType`/`domainType`/`ruleType`/`actorType` kind-vals; `*Instance` traits deleted"* — **correct**
- `DRACO.md` — the `*Instance` triad, `typeInstance` vals, 26 mentions — **wrong, for months**

Memory is updated *as part of doing the work*, with a specific trigger. `DRACO.md` is updated when someone remembers. Same author, same sessions, opposite outcomes. So yes — but what made memory work isn't diligence, it's that the update is attached to an event rather than to a session boundary.

**So my answer is yes, with the mechanism changed rather than the intention strengthened:**

1. **Shrink what's volatile.** Following the review: `DRACO.md` becomes agent operating rules — no sbt/git, lazy-val discipline, byte-equivalence gates, issue-vs-memory rules — which change rarely, and `README.md` carries architecture, which changes when architecture does. Then "update every session" mostly stops applying to `DRACO.md` at all.
2. **Gate the retired names**, so removals can't silently persist in prose.
3. **Trigger on the event, not the session.** The rule that works is "a type was renamed or removed → update the docs and the retired list in the same commit," which is the same shape as the record-file convention that has held all week.

One thing I'd want from you either way: the issue-filing contradiction. `DRACO.md` says file an issue for deferred work; memory says don't open issues unprompted. Today I followed memory and asked first. Whichever you want should be the one that survives the rewrite — I shouldn't be picking between two of your own instructions.

---

**[Dev]** If a new issue is created, it should be with my explicit approval. Does that help, or are you asking for more specific direction?

**[Claude]** That's sufficient — it resolves the contradiction cleanly, and it's the version I'd have argued for anyway since it's the one with evidence behind it.

How I'll apply it, so you can veto the wording now rather than after the rewrite:

- **New issue → explicit approval, every time.** In the rewritten `DRACO.md` that replaces "file an issue for deferred work," which is the instruction currently pulling the other way.
- **Deferred work with no issue → surfaced in conversation**, which is what memory already prescribes, and what I did today with the `Cartesian.json` stray `DracoType` derivation and the `ExampleDomainsGenTest` compile gap.
- **Comments on an existing issue → I'll treat as not needing approval**, since your sentence covers creation and memory says to prefer commenting on the owning issue. Say if you'd rather that were gated too.

The reason it matters more than it looks: **the review is a large issue-generator.** §4 alone has 18 verified defect rows, and §7 has ten ranked recommendations. Under the old `DRACO.md` instruction the obvious move would be to file a dozen issues from it — which is precisely the backlog inflation your rule exists to prevent, and it would bury the four or five that actually matter. So those come to you in conversation, batched and prioritised, and only become issues if you say so.

That leaves the rewrite itself as the next concrete piece of work, and the four questions in §8 as the ones still open. I'd start on `DRACO.md` when you're ready — it's the highest-leverage item and it doesn't depend on any of the §8 answers, since none of them touch agent operating rules.

---

**[Dev]** I'm ready for you to start on DRACO.md, given you have what you need from me.

**[Claude]** Verifying every structural claim before I write it — the point of this file is that it's true. *(Read the old `DRACO.md` in full; verified the Aspects fields, the package list, and the companion shape against the source; wrote the replacement; grepped it for every retired symbol; wrote `git-record-2026-08-15-1500`.)*

All verified. Writing the replacement.

Rewritten — 229 lines, and every retired symbol now appears **only** in the "do not reintroduce" list. Symlink intact.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-15-1500 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

Docs only — no code touched, so no gate to run.

**What the file is now.** Operating rules first, since those don't change with the architecture: no sbt/git, definitions move as a trio, lazy-val discipline, what each of the three gates pins and therefore what a failure *means*, the four-artifact split, and the commit-message and pathspec mechanics we learned the hard way this week.

Then a compact orientation section, every claim checked. I kept it short deliberately — architecture detail belongs in README. But **I didn't just point at README**, because README is itself stale, and pointing at a wrong document would have made the next session worse rather than better. So §5 states plainly which neighbouring docs are unreliable and which aren't (`drake.dlt` is current and authoritative).

Three things worth flagging:

- **Your issue rule is now written down** — approval required for creation, conversation for deferred work, comments ungated. That contradiction is resolved in the file rather than living in my memory alone.
- **The retired-names list is the part meant to outlive this commit.** Every drift the review found was the same failure: a type was removed, the prose kept the name. That's the seed of the gate I proposed — and it's checkable precisely because the general version isn't, which I tested rather than assumed.
- **I did not touch `AGENTS.md`.** It should be a symlink to this file, but it's a diverged older copy and replacing it deletes tracked content. That's your call, not mine.

Remaining from the review's §5: README, `GETTING_STARTED`, and a CHANGELOG block covering the ~25 git-records since alpha.5. README is the substantial one and the natural next piece if you want the docs pass finished before returning to code — the §8 questions still gate the code work, and the first of them (mods as engine tier) is one README would have to state either way.

---

**[Dev]** Can we update the README.md next?If so, make sure that the architecture descriptions eliminate, or at least minimize target language specific (in this case, Scala) terms, references and concepts. In the minimize case, explicitly note that such language specific description should be replaced by language neutral descriptions when it's feasible to do so.

**[Claude]** Now I'll rewrite it. The neutrality constraint shapes the whole document, so let me be explicit about the vocabulary swap and quarantine what can't be neutralized yet. *(Read the old `README.md` in full; wrote the 542-line replacement; verified zero retired symbols remain and swept the architecture half for host vocabulary; wrote `git-record-2026-08-15-1620`.)*

All four remaining Scala terms sit in the vocabulary table's *"rather than"* column — they appear only as the terms being replaced. The architecture half is clean.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-15-1620 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

Docs only, 542 lines. **Zero retired symbols remain**, and the architecture half is clean of host vocabulary.

**How I handled your constraint.** Two structural devices rather than scattered caveats:

1. **A vocabulary table up front** — each neutral term against the host term it replaces (derivation/extends, element/field, globals/statics, factory/constructor, modules/sealed, projection/codegen, `[T]` `{K,V}` `(A,B)` `A -> B`). The substitution is legible instead of merely performed. Parameter notation follows it too: `Primal(T)`, `Holon((S, T))`.

2. **A "Language-specific residues" section** — this is your *"explicitly note when it should be replaced"* requirement made into a table rather than footnotes. Eight leaks, each with what neutral would look like: initialization discipline, `Seq[T]`/`Set[T]` still in valueTypes while maps are already neutral, host-opaque value strings, codec realization, the rule-evaluation and actor-behaviour bindings, structural identity authored as host-named members, and type parameters as strings. Scattering these through the prose would have hidden how many there are; eight in one column is a measurement.

The map constructor is named there as the worked example — `{K, V}` normative in both definition and surface, `Map[K, V]` produced in exactly one function. Every other row is that same move, not yet made.

**One thing the neutral framing bought.** Writing without host vocabulary forced the abstract/concrete rule to be stated properly for the first time — a position holds a variable or a concrete type, abstractness is recursive, a constraint never makes a position concrete, and only a fully concrete name can derive an atomic term. That's why `Meters` deriving `Distance(Double)` is a term while `Dictionary(K, V)` is a schema. It had been in conversation and issue comments; it's now in the doc.

Operational sections stay Scala-specific and say so at the boundary — they describe this repository, not the architecture.

Remaining from the docs pass: `GETTING_STARTED.md`, a CHANGELOG block for the ~25 records since alpha.5, and `AGENTS.md` — still your call, since making it a symlink deletes tracked content.

---

**[Dev]** Let's rename GETTING_STARTED to GETTING_STARTED_TARGET_SCALA, and then create separate stub documentation markdown files for Haskell and TypeScript, to be filled in as we gain experience with those language development tools. Make a first pass at generalizing the structure of GETTING_STARTED*, to accommodate multiple target languages.

**[Claude]** *(Read `GETTING_STARTED.md`; verified the CLI subcommand surface against the source; `mv`-renamed the file and wrote the three target guides; corrected `README.md`'s getting-started section and `DRACO.md`'s doc-status table; wrote `git-record-2026-08-17-1100`.)* Two corrections that fall out: `draco-gen` has **no** `from-yaml`/`to-yaml` (removed with YAML) and **no** `drake` subcommand — the surface CLI is a separate `DrakeCLI` with `emit | parse | check`. My README reference to `bin/draco-gen drake` was wrong; I'll fix it in the same pass.

Structural parity confirmed — all three have 9 sections and the only heading difference is the target name in §4. One stale reference to fix, in the doc-status table I wrote two days ago:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-17-1100 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

Docs only. The rename was a plain `mv`, so `git add -A` records it.

**The generalization, and what it asserts.** Nine sections, identical across all three files — verified, the only differing heading is the target name in §4. Section 0 states the loop and says plainly that nothing in it belongs to a target; **only four things do** — toolchain, projection command, running the result, command set. The parallel files are what make that claim checkable rather than merely asserted.

README's own getting-started became a duplicate under that split, so it now carries the neutral loop, pointers to the three guides, and the example definition — genuinely target-independent text — while commands live per target.

**The stubs aren't placeholders.** Each carries the open questions its target must answer, written down while the reasons are fresh:

- **Haskell's hardest is derivation.** Scala's `extends`/`with` is subtyping; Haskell has none. A derivation becomes a constraint, an embedded record, or a sum — and that choice determines what `globals` and `typeDefinition` even mean there.
- **TypeScript's is identity.** Draco's `TypeName` identity is nominal; TypeScript's type system is structural, so `Meters` and `Radians` would be interchangeable by default. Whatever that target does about branding *is* its semantic-preservation story.

Each file also names what its target tests about the framework: Haskell stresses whether the *structure* is neutral, TypeScript whether *identity* is. Both should find residues, and those belong in README's residues table when they do.

**Two corrections surfaced while writing**, and one is mine from two days ago. `draco-gen` has no `from-yaml`/`to-yaml` (the old guide listed them) and **no `drake` subcommand** — the surface CLI is a separate `draco.DrakeCLI` with `emit | parse | check`. My README invoked `bin/draco-gen drake`, which doesn't exist. Both fixed, and the Scala guide now carries a verified command table for all three CLIs — which is the kind of thing that rots first, so it's worth having checked rather than recalled.

Remaining from the docs pass: `CHANGELOG.md` (an alpha.6 block from the ~27 records since June), and `AGENTS.md`, still your call since making it a symlink deletes tracked content.

---

**[Dev]** Let's now update CHANGELOG and determine what triggers its update going forward. It should be more frequent that it has been so far.

**[Claude]** *(Read `CHANGELOG.md`; enumerated the git-records since alpha.5 against `git log`; wrote the block; anchored the trigger in `DRACO.md`'s operating rules; wrote `git-record-2026-08-17-1500`.)* I have the full span — 37 records, chapters 41–73. Writing the block, folding in the two existing Unreleased entries. Now anchoring the trigger in DRACO.md's operating rules, so it survives outside the CHANGELOG itself:

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-08-17-1500 | sed '1d' > /tmp/msg && git add -A && git commit -F /tmp/msg && git push
```

**The block.** `[Unreleased]` now covers 37 records and journal chapters 41–73, organised for a reader outside the tree rather than as 37 bullets. The through-line: DRAKE arrived complete; values became expression trees; loading left the Generator for `TypeLoader` and an explicit `DefinitionPath`; the framework began validating its own foundation through the rule engine it ships; YAML was retired. Plus the four Changed items that alter *how definitions are written* — uniform root meaning, role-is-presence, the `RuleAspect` restructure, and actor-ness on the aspect alone.

I folded the two existing Unreleased entries in rather than displacing them, and added a **Documentation** section covering this week's three passes.

**The trigger — my recommendation, now written into both files.** A CHANGELOG entry is written **at the same moment as the commit's git-record, in the same commit.** The record carries the reasoning; the CHANGELOG carries one or two sentences of observable fact.

The choice isn't arbitrary, and it's the same argument that produced the `DRACO.md` rewrite:

- A record file **already** has to be written before every commit, and that step has held all week without anyone thinking about it. Extending it costs nothing.
- Release time is precisely what was being relied on — and this file went **75 days and 37 commits** without one arriving.

This repo already demonstrates both outcomes. Auto-memory is updated as part of doing the work, and it's accurate. The CHANGELOG waited for a release boundary and fell a quarter of a year behind. Same author, same sessions.

That's the docs pass complete except `AGENTS.md`, which still needs your call — making it a symlink deletes tracked content, and it's a diverged copy rather than a stale one, so there may be something in it you'd want kept.

Worth noting for whenever you tag: `[Unreleased]` as it stands is a credible **alpha.6** block. It's a much larger release than alpha.5 was, and the last visible one to anyone outside the tree is June 3.

*(Session possibly ongoing at time of transcription — chapter to be extended if more follows.)*

## Status

This chapter transcribes the August 10–17, 2026 session (opening 15:26 UTC August 10; last recorded exchange 17:09 UTC August 17, the CHANGELOG-trigger directive) — **possibly still ongoing at transcription time**: no closing exchange exists in the recorded data, so this chapter may be extended again. **The opening arc (August 10). A false start precedes it:** the same kickoff prompt was first issued August 9 at 21:56 in a session that began making changes — language spec, emitter, parser, extending the parse gate to the mods actors — and died on "API Error: Connection closed mid-response"; that session was abandoned and the kickoff re-issued fresh on August 10. Its partial changes are the likely explanation for the working-tree state the fresh session found — a **complete, unverified #60 implementation already in the tree** (the `drake.dlt` BRACKETS spec, `parLines`/`arguments` as exact inverses, `aerial/Input.drake` re-authored with brackets, `DrakeParseTest` extended over the mods corpus), which Claude verified read-level rather than re-deriving. **Opening state (carried from chapter 72's close):** last commit `746de5b`, full suite 495/495, `DrakeParseTest` covering 80 types both directions with only the codec aspect held back, measured loss 14 fields (10 expression form #61, 3 legacy `Seq()` #55, 1 package-less `Map`), two decisions waiting on Dev (the Map package, the group-1 extraction), #60 the other thing between #44 and closing, and the standing rule: no unprompted issues. **Test counts across the arc:** **179/180** (`DrakeParseTest` with the mods corpus in for the first time — the one failure `Drake.expression`'s missing `=` named-argument node on `aerial/Input.json` gate 2, patched as a one-line `case "=" => args.mkString(" = ")`; #60 itself proved clean, zero loss entries from `Input`) → **180/180** → **514/514** (commit 1 — `scalaTypeExpression` + `targetTypes` — confirmed provably inert, zero braces in the corpus, generated files byte-identical) → **514/514** (commit 2 plus the whole Map pilot; `DracoGenTest` byte-identical). Note the suite grew 495 → 514 between sessions/runs as recorded. **What landed (all uncommitted in the tree at the open end, record written):** (a) **#60 verified through both gates** — 90 types (80 draco + 10 mods) round-tripping, `terrestrial/Output` held out of gate 2 only for the inline-tuple reason (invisibility, not ambiguity); the whole mods corpus adds just 2 losses, both `terrestrial/Output.json`, #61's territory. (b) **The `=` renderer completion.** (c) **The Map pilot** — Dev's directive dissolved the Map-package question rather than answering it: `{K,V}` in drake and JSON, `Map[K,V]` generated "completely in the scope Generator[TargetScala]" (the `ScalaTarget` → `TargetScala` rename explicitly deferred until Dev explains it). Commit 1: `scalaTypeExpression` (brace family `{T}`/`{K,V}`/`mut {T}` → Scala, rewriting brace groups in place) and `targetTypes` (an explicit, exhaustive element walk — the subtypes are `lazy val` traits with no `copy`, and `Pattern`/`Action` carry `variables` a shared-fields walk would drop), applied at both `generate` overloads beside `TypeLoader.rooted`, deliberately not a 48-site sweep. Commit 2: six valueTypes neutral (`Dictionary` ×3, `TypeDictionary`, `DomainDictionary`, `CLI`'s `{String, Seq[String] => Unit}`), `Drake.typeExpression`'s recursing brace case, `parseTypeExpression`'s arity guard (the latent `{K, V}` → `Set[K, V]` wrong answer), four `.drake` files re-spelled, `drake.dlt` VALUE-TYPES gaining the map row and losing its now-false "JSON always holds a Scala type string" claim. **Loss report: 14 → 16 fields across 90 types — not a regression** but the mods corpus entering the measurement (both new entries `terrestrial/Output.json`); owners unchanged: 12 expression form (#61), 3 `Seq()` (#55), 1 reference package. **The design dialogue (pairs of questions Dev drove, each landing on #51):** `from Map(K, V)` proven **constitutive, not incidental** — `removed`/`updated`/`get`/`iterator` are exactly Scala's four abstract `Map` members, dropping the derivation would compile but Dictionary would stop *being* a map (Claude's earlier sentence implying otherwise corrected on challenge); the expected post-#51 `Dictionary.drake` — `from {K, V}` with `DracoType` gone (Dictionary is the *only* type in either corpus spelling `DracoType` beside another parent — compensation for `Map` being foreign), requiring `TypeLoader.rooted:15`'s `nonEmpty` gate widened to "contains a named parent", with the further host-interface-obligation step (the four `dyn`s derived, `Option`/`Iterator` gone — Dictionary as the densest host-leakage concentration) held separate; the round-trip-reveals-redundancy question — a perfect round-trip is where redundancy is maximally invisible, elision-plus-inference is the mechanism (bare references already do it), and Claude's "lossy-on-purpose" framing was overturned by Dev's follow-up into **normalization under the INFERENCE principle**: root both sides and compare, redundant `DracoType` becomes correctly invisible, a gate-2 failure becomes a true positive, and Dictionary still fails — its `DracoType` really is information until `rooted` can tell a foreign parent from a rooting one. **#44/#60 state at the arc's close:** record `git-record-2026-08-10-1845` written, closing both; the two changes were combined into one commit (shared hunks in `Drake.scala`/`drake.dlt`), boundary named in the record for per-hunk staging if Dev wants two. The commit's fate — a paste-broken heredoc, then a clean landing as `0acf2da` — opens the continuation below. **#51 state:** freeze declared expired on its own terms (frozen in favour of the parser, which now round-trips 90 types); recommendation accepted-in-principle but not started — **the derivation edge only** (`derivation` holds a type expression rather than a `TypeName`), Dictionary the single driving case, done when the `reference package` loss line reaches zero and `Dictionary.drake` reads `from {K, V}`; the rest of #51 (atomic `typeParameters`, `[String]` → `[TypeParameter]`, the reserved primitive set) explicitly not bundled. **Queued at the arc's close (dispositions in the continuation below):** Dev's call on the #51 edge as a **Scala-first TypeDefinition shape change** (classpath is compile-time truth) — the premise Dev's `definitionPath` redirect then retired; the `ScalaTarget` → `TargetScala` rename and Dev's promised explanation (still unexplained at the open end); the group-1 extraction re-scoped (placement **TypeDefinition, not Source** — #57 makes Source the bound of the varying side; timing after #61, four structural predicates first; `hasCodec` flagged as neutral-in-intent, Scala-in-definition); the `surfaceCarried` narrowing offer (hold out the one Map residual, verify packages on 111 of 112) superseded in spirit by the root-both-sides insight but never executed; wiring `DracoType` elision into the emitter/`rooted` (small, flags exactly one type today); the Dictionary host-interface-obligation step as its own future argument; and the incidental find — `src/mods/resources/domains/world/Cartesian.json` still carrying a sole `['DracoType']` derivation, outside #50 item 1's sweep, deliberately not filed.

**The continuation (August 11–17).** **The commit incident (August 11):** the paste of the prepared heredoc block broke — the block terminated early and zsh executed the commit message's remaining lines as shell commands (`zsh: command not found: it`), so `git push` answered "Everything up-to-date" with no commit made — yet both `gh issue close` commands had already run clean, so **#60 and #44 closed before their commit existed**. Diagnosis: paste handling on a long block, not quoting — backticks and em-dashes are inert inside `<<'EOF'`. Fix: the message extracted from the record file itself with `sed` (same bytes as the audit trail), the record annotated with the failure, and the commit re-issued as `git commit -F <file>` — landing August 12 as **`0acf2da` "drake: the argument as an opener, and Map as a type expression (#44, #60)"**, 17 files, pushed `746de5b..0acf2da`. The lesson — commit messages through a file, never a heredoc — became standing practice for every subsequent commit and was later written into the rewritten DRACO.md. **The DefinitionPath track (August 12–13), Dev's redirect:** classpath-as-compile-time-truth to become a language-neutral `definitionPath` — runtime read of definitions embedded in runtime libraries, domain dictionaries with no generated target source — and, in the second directive, **the same semantic impact in Scala, Haskell, TypeScript**. Claude found both capabilities already exist (`TypeLoader.loadFromResource`, `DomainBuilder.define`), spelled twice in JVM terms with Generator carrying a complete duplicate loader that had **already drifted** (`//Name.json` vs `/Name.json` on the empty-package case); the second directive overturned Claude's ordered-first-wins assumption — a JVM accident, not a property — for **roots as a set under unique-or-error**, and promoted **#37 (structural `TypeName` equality) from cleanup to prerequisite** (collision detection is unimplementable over reference equality on a map-key type). After "You have my go-ahead," three commits landed in the approved order: **`c76bf09` "TypeName identity is structural (#37)"** — equality/hashCode authored in `TypeName.json`'s factory body as single-line values (zero multi-line values exist in the corpus; the issue's sketch had newlines), 520/520 with one test-side lint warning fixed, the five `.namePath == .namePath` workarounds deliberately left, #37 closed; **`f62089b` "One resolution, and it is draco's rather than the JVM's"** — `getResources` plural, exactly-one-or-error, Generator's duplicate loader deleted ("all tests passed", count not recorded); **`db87dbd` "DefinitionPath: the roots are held, not searched"** — `roots: Seq[URI]` explicit, `hostRoots` one realization of the default, `TypeLoader.loadFromResource` a one-line delegation, all 117 generated companions untouched under Dev's confirmed **"the path is a property of the runtime"**. One commit-scoping blemish recorded: `git-record-...-2115` landed in commit A because the IDE auto-adds new files and `git commit` commits the whole index — pathspec belongs on the *commit* (`git commit -F msg -- <paths>`), the second mechanics lesson later written into DRACO.md. **The 524/525 failure was worth having:** the `DracoGenTest` diff on the hand-predicted `DefinitionPath.scala` exposed a real generator defect — the codec gate excluded only function-like parameters while **circe 0.14.1 contains no `java.net.URI` codec at all** (verified against the jar), so any definition with a `URI`/`URL`/`File` factory parameter would have emitted uncompilable code; the fix sources exclusions from `externalTypeImports`, blast radius exactly one type. The loss report's headline also caught Claude adding a fourth legacy `Seq()` (#55) to new corpus data — both defects found by reading report-only numbers, not assertions. Then **525/525**. **The type-parameter decisions (design dialogue transcribed verbatim above):** Dev's principle — type parameters in a type name implicitly indicate abstract vs concrete — verified against the corpus: 40 parameterized references, **9 abstract / 31 concrete, the biconditional holding with zero exceptions and zero under-applied references**, every fully-bound reference sitting on a formal-less type; the "bound" ambiguity resolved (a position holds a variable or a concrete type; a `<:`/`>:` constraint — 6 declarations, 0 references — never makes a position concrete; abstractness is recursive, `Holon((S, T))`); the sync check answered with the five-point model stated back; consequences — **type parameters stay in `TypeName` identity** (`Dictionary(K, V)` ≠ `Dictionary(TypeName, TypeDefinition)`), #51's `[String]` → `[TypeParameter]` promoted from tidiness to **semantic requirement** (free-variable questions aren't answerable over strings), and the variable-constraint-identity sub-question deferred to #51. **#62 (August 13):** created at Dev's explicit directive — report-only tests should assert their expected numbers — six tests across four files, per-category exact-equality baselines each tied to its owning issue (#55, #61, #51, #42), labelled `cleanup`, no `priority-next`; then enriched at Dev's direction with the response narrative, in the course of which Claude **corrected its own "sibling gap" claim before it became durable**: generated draco-corpus Scala *is* transitively compile-checked (pinned to hand-written files sbt compiles); the genuinely unpinned surface is `ExampleDomainsGenTest`'s 20-of-48 differing example-domain outputs, compiled by nothing. **The review and the documentation overhaul (August 14–17):** Dev's "what does the current priority seem to be" answered with the `DomainBuilder`→`TypeDictionary` promotion — then re-ordered by the **dracoreview20260815.md** upload, whose falsifiable claims Claude sampled and confirmed (including 26 mentions of `TypeInstance`/`DomainInstance`/`RuleInstance`/`typeInstance` in `DRACO.md` against zero in Scala; two garbled §4 table rows noted), and whose §8 Q1 (mods as engine tier?) was exactly the decision the promotion had presumed. The overhaul, each commit block issued with a git-record (**no landing confirmation for these four appears in the session data**): (1) **DRACO.md rewritten and verified** (record `2026-08-15-1500`) — 229 lines, operating rules first, every structural claim checked against source, retired symbols only in the do-not-reintroduce list, symlink intact; preceded by Dev's "shouldn't it be updated every session after that?" answered structurally — prose is the one ungated artifact, a blanket backtick lint measured as pure noise (115/165 non-resolving, almost all legitimate), the zero-noise gate is **retired names**, and the trigger that works is the event, not the session (the MEMORY.md-right/DRACO.md-wrong contrast as evidence); and by Dev's standing rule, transcribed verbatim above and written into the file: **"If a new issue is created, it should be with my explicit approval"** — deferred work surfaced in conversation, comments ungated. (2) **README.md rewritten** (record `2026-08-15-1620`) — 542 lines under Dev's language-neutrality directive: a vocabulary table (derivation/extends, element/field, globals/statics, factory/constructor, modules/sealed, projection/codegen), an eight-row **Language-specific residues** table making the minimize-case notes a measurement rather than footnotes, and the abstract/concrete rule stated properly in a doc for the first time. (3) **GETTING_STARTED → GETTING_STARTED_TARGET_SCALA plus Haskell and TypeScript stubs** (record `2026-08-17-1100`) — nine identical sections, only four target-specific things (toolchain, projection command, running, command set); the stubs carry each target's hardest question (Haskell: derivation without subtyping; TypeScript: nominal identity over a structural type system); two CLI corrections surfaced — no `from-yaml`/`to-yaml`, and no `draco-gen drake` subcommand (`DrakeCLI` is separate, `emit | parse | check`), the latter Claude's own two-day-old README error. (4) **CHANGELOG caught up** (record `2026-08-17-1500`) — `[Unreleased]` covering 37 records and journal chapters 41–73 (75 days behind), noted as a credible alpha.6 block, and **the trigger decided and anchored in DRACO.md: the CHANGELOG entry is written with the commit's git-record, in the same commit** — the record carries the reasoning, the CHANGELOG the observable fact. **Test counts across the continuation:** 520/520 (38 suites, #37 in; one lint warning, test-side, fixed) → "All tests passed" (`getResources`) → 524/525 → 525/525 (`DefinitionPath`); the suite grew 514 → 520 → 525 as `TypeNameTest` and the new definition entered. **Commits confirmed landed in the data:** `0acf2da`, `c76bf09`, `f62089b`, `db87dbd`; the four docs commits were issued as blocks but have no pasted confirmation. **Issues:** #60, #44 closed (during the broken-commit paste), #37 closed, #62 opened at Dev's direction and enriched; the standing approval rule now in DRACO.md. **Queued at the open end:** `AGENTS.md` — still Dev's call, a *diverged* copy rather than a stale one, symlinking deletes tracked content; the review's §8 questions, all Dev's — mods-as-engine-tier (gating the `DomainBuilder`/`TypeDictionary` promotion), the deferred presence/codec pair, #61 lint-vs-trees, alpha.6 + `bin/draco-drake` vs #51; **#51's derivation edge still open and still the last loss-report entry** (16 fields across 91 types); the `DomainBuilder` promotion with #47 and #41 folding into it; the five `.namePath` workarounds; the `TargetScala` rename explanation; nothing carries `priority-next`. **The session may still be open** — the last recorded exchange is the August 17 17:09 UTC CHANGELOG directive and its response, with the AGENTS.md call and an alpha.6 tag both explicitly left with Dev.
