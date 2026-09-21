# Draco Dev Journal — Chapter 79

**Session date:** September 18, 2026
**Topic:** The session that closed drake.dlt EVALUATION and opened the target-neutral carrier — lazy step 3 landing a factory parameter by-name with the factory body as its prelude (`b404d7e`) after a first run failed 133 parser tests because `Drake.parse` handed cursor-consuming arguments to by-name factories, the three bodied factories that read a parameter twice authored to read it once with `loc x T _x` (`578c92e`), the actor-minting factory folded into the same convention as a fourth step (`374f168`), the `ifThenElse` bridge surfaced as "is the presence the package, or is it nothing?" and answered by Dev with "package" as a JVM term, the name path as the least common denominator between Scala packages and Haskell modules and the empty path as a local scratch module, Dev's reading of Dictionary.json that the `Map` head "is not necessary" leading to the collection sugar carried as the node keys `[]` and `{}` with no host head in the corpus (`0b0e63a`), `Option` becoming `Presence` on the loader path with one conversion at the host boundary through the declared symbol `presence` and `fold` doing the chaining (`20778f3`), and `ActorRef(M)` read as a consumer of M, `(M -> Unit)`, with the assembly keeping identity and the term provisional until Pekko is integrated through drake — built at the close and awaiting its run. Transcript.

---

**[Dev]** Continue the draco work from c60a9ed (2026-09-18, main, 622 tests / 44 suites green).

Read first: memory project_drake_semantics_2026-09-13 — the agreed design and what has landed,
item 10 for the laziness decision. Then project_drake_parser for the parser state and
project_generator_permanence for the compiled-CLI regeneration workflow and its two rules
(overlay main AND test resources; rebuild the overlay after every engine recompile). Two
feedback notes govern how we work: feedback_lazy_discussion (one point per reply when we are
discussing) and user_directional_relations (restate any non-commuting relation with an example
and confirm before building on it).

Where we are: a call is f(a, b) with positional then name:value arguments; value types and type
parameters are type-form trees; the JSON corpus is what the drake parses to; Presence, Present
and Absent are draco.drake's first members with fold as dispatch; evaluation is lazy by default
with `now` to override, and steps 1 (body bindings) and 2 (method parameters) are landed.

Check git status before starting — three commits may still be pending from the last session:
the journal chapter 78 (draco-git-record/git-record-2026-09-18-1110), the docs sync of DRACO.md
and README.md through chapter 78 (path-scoped to DRACO.md README.md .gitignore), and the
AGENTS.md relink decision.

Next increment, agreed: lazy step 3 — factory parameters become by-name (127 parameters), in the
same two-run shape as steps 1 and 2. After that: ifThenElse as a second dyn on Presence, with how
a Boolean condition becomes a presence the one open design point — surface it before building.
Also open for discussion when I raise it: how much abstract types must support the codecs of
concrete types.

Known finding, not filed: the re-canonicalization sweeps turned three infix rule conditions
(PrimesFromNaturalSequence, RemoveCompositeNumbers, SelfDeclaration) from trees back into
host-opaque strings, because the parser does not tree infix operators yet (#61); no gate saw it.
Surface it when the parser work reaches operators.

Conventions unchanged: I run sbt, commits and pushes; you author the trio (drake, json, scala)
drake-first, write the record and CHANGELOG entry with each commit, and hand me the command
block with the expected numbers. Surface structural moves before building them.

**[Claude]** I'll start by reading the three memory notes and checking git status for the pending commits.

Git status is clean and all three pending commits are on main. Now I'll read the step 2 record, the engine's factory rendering, and the EVALUATION section of drake.dlt.

Now the engine: how factory parameters and the `apply` signature render today, and how step 2 wired `now` for method parameters.

Last check: eyeball the three doc edits and the new test as written.

Step 3 is built and ready for your run. The three pending commits from last session were already on main, so the tree was clean at the start.

**What changed.** The engine's one factory site now spells a parameter `_x: => T`, with `now par` keeping it strict, in [DracoGenerator.scala:623](src/mods/scala/draco/DracoGenerator.scala:623). No prelude is needed. A bodiless factory already binds each parameter once as the instance's `override lazy val x = _x`, and a bodied one reads `_x` inside the lazy members it authors. So an argument is evaluated on first read and never if unused. `PresenceTest` gains one test: `Present(sys.error(...))` constructs, and two reads of `value` evaluate the argument once.

**Scope, measured today, not the 127 from the sizing:**

| where | factory parameters | definitions | moved |
|---|---|---|---|
| src/main | 101 | 44 | yes, where plain |
| scenario | 13 | 10 | yes, where plain |
| example domains | 19 | 12 | yes, where plain |
| plain `apply` blocks swept | 122 | 44 projections | all |

**One decision I made, for you to accept or correct.** The actor-minting factory is a separate render path: `factory ActorType` becomes `def actorType(consumer: ActorRef[...])` with bare, strict names, and its parameters are wiring the host actor system reads at start. I left it strict. Making it by-name needs the rename-and-prelude shape, so it is a fourth step if you want one, not a residue of this one.

**One residue, stated rather than hidden.** Three bodied factories read a parameter at two sites: `Domain` and `TypeDictionary` read `_domainDefinition` twice, `DomainLineOf` reads `_definition` twice. Each site sits inside a lazy member, so the argument is evaluated at most twice, not once. The parameter cannot be renamed for a prelude because every call site uses the `_name = ...` named-argument convention. Every call site today passes a value, so nothing is observable. Your call whether that stands.

**One run, with two as the fallback.** The change is mechanical, so I hand-applied the form to all 44 generated projections and verified the diff touches only parameter lines. The JSON corpus does not move. Expected: 623 tests / 44 suites green, GEN MAP 28 match / 20 differ, every other headline unchanged. If `DracoGenTest` names a file, paste the Failed block and I regenerate those with the compiled engine over the full overlay.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Commit, after the run is green. The record is [git-record-2026-09-18-1227](draco-git-record/git-record-2026-09-18-1227), and the CHANGELOG entry, drake.dlt EVALUATION section and DRACO.md paragraph and baseline count are updated with it.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-18-1227 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1227 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1227 && git push
```

Next after this lands is `ifThenElse` as the second dyn on Presence. The open design point there is how a Boolean condition becomes a presence, and I will surface that before building anything.

*(Delegated action — Dev ran run 1: `sbt test` 490 passed, 133 failed, all of them in `draco.DrakeParseTest` and `scenario.ScenarioGenTest` — 65 `emit(parse(source))` and 65 `parse(emit(td))` per-type tests across main and mods, the `++` structural test, the loss report itself and the scenario's json-equals-parse assertion; the scenario line read 23 files, 12 rejected, 11 clean; `DracoGenTest` green, GenDrake 96 of 96, GEN MAP 28 match / 20 differ, PON and canonical unchanged.)*

All 133 failures are in the parser gates, and `DracoGenTest` passed, so the by-name form is exact. The cause is a genuine laziness effect: `Drake.parse` builds elements with arguments that consume the token cursor, and a by-name argument is now consumed on first read, out of order. I'll find the construction sites.

The grep found no other site with that hazard. One last look at the sequenced parser lines, then the run 2 handover.

Run 1 was a real finding, not a regeneration miss. `DracoGenTest` was green, so the hand-applied by-name form is the engine's byte for byte, and GEN MAP and every headline held. The 133 failures are all parse-based tests, and they are the first observed consequence of laziness.

**What broke.** `Drake.parse` is hand-written Scala that calls the drake-defined factories with arguments that consume the token cursor, such as `Monadic(parseValue(c))` and `Factory(valueType, parseSection(parameters), parseSection(body))`. With by-name parameters those arguments now run at the first read of the instance, not in token order, so the cursor desynchronised and the parser's `expect` threw at every definition.

**The fix is at the caller,** which is where an effect is sequenced under call-by-need, as in Haskell. Each of the six sites in [Drake.scala:1006](src/mods/scala/draco/Drake.scala:1006) binds its argument to a `val` before the call. The `TypeDefinition` builder at the end of `parse` reads only locals the loop has settled, so it needed nothing. I grepped the rest of the tree for an effectful expression in argument position of a factory call and found none. The gotcha is recorded in DRACO.md section 6, the record, the CHANGELOG entry and the dlt STATUS block, and a memory note.

**Run 2.** Expected: 623 tests / 44 suites, all green, GEN MAP 28 match / 20 differ, every other headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

Commit, once green. The commit block is unchanged and sits at the foot of [git-record-2026-09-18-1227](draco-git-record/git-record-2026-09-18-1227).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-18-1227 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1227 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1227 && git push
```

The two points from the handover still stand for your decision: the actor-minting factory left strict, and the three bodied factories that read a parameter at two sites.

*(Delegated action — Dev ran run 2 and the commit block: `sbt test` 623 passed, 0 failed, every headline at baseline — GenDrake runs 96 of 96; GEN MAP 28 match, 20 differ, 0 error, 0 missing (of 48); PON 80 numbers, 550 expressions, 42 discrepancies; canonical 7 differ; surface losses 5 fields across 108 types [expression form 2] [type form 3]; parse scope 98 draco + 10 mods, 0 held back; scenario 23 files, 0 rejected, 0 drifted, 23 clean; the forest 1 of 2; committed and pushed as `b404d7e`, 51 files changed, `1e3ad40..b404d7e`.)*

---

**[Dev]** After you review the successful result of the terminal command output, I need you to expand the narrative for me on "The two points from the handover still stand for your decision: the actor-minting factory left strict, and the three bodied factories that read a parameter at two sites.", so we can discuss, if necessary.

**[Claude]** Run 2 is green at 623 / 44 with every headline at baseline, and b404d7e is pushed. Before writing the narrative I'll check how an actor-minting factory actually uses its parameter, so the description is grounded, and note the landing in memory.

Step 3 is landed at b404d7e, 623 tests / 44 suites, every headline at its baseline. Here are the two points in full.

**Point 1. The actor-minting factory stays strict.**

Drake has two factory conventions, and only one of them moved. A plain factory becomes the companion's `apply`, whose parameters arrive as `_x` and are bound once by the instance's own lazy members. That is the shape step 3 made by-name.

An actor-minting factory is written `factory ActorType` with parameters, and the engine renders it through a different path as `def actorType(consumer: ActorRef[JSON])`. The names are bare, not underscored. The parameter is not a field of the instance at all. It is wiring: the actor's start action seeds it into the Evrete session so that rules can reach the peer actor, and the caller is a Pekko spawn in a chain test or an Assembly binding. In drake terms it is the same keyword, `par`, under the same `factory` head, so the EVALUATION section's sentence "every parameter is lazy" reads as if it should apply. Today it does not, and nothing in drake.dlt says why.

Why I left it. Making it lazy needs a different mechanism from the one step 3 used. There is no `override lazy val` waiting to memoize the value, because the parameter is not a member. Call-by-need would need the method-prelude shape from step 2: the parameter arrives as `_consumer` and a `lazy val consumer` binds it once before the `new Actor` block. That is a rename of every actor factory's parameter and a change to the actor template, which is the first hand-written-looking corner of the generated actor. It is a separate step in its own right, not a residue of this one.

The question underneath, for you: is an actor's wiring a value the actor's definition reads lazily, like any other parameter, or is it a fact about the system that exists before the actor does? If the first, a fourth step follows. If the second, the drake keyword is doing two jobs, and the dlt should say the actor-minting factory is strict by nature, the way `mut` is.

**Point 2. Three bodied factories read a parameter at two sites.**

Step 3's guarantee rests on the factory body being the prelude. Where the body binds `override lazy val x = _x`, the parameter is evaluated exactly once. Where a bodied factory reads `_x` at one site inside one lazy member, the same holds. Three definitions read a parameter at two sites:

- `Domain` binds `typeDefinition` to `_domainDefinition` and also builds `TypeDictionary(_domainDefinition)`.
- `TypeDictionary` reads `_domainDefinition` twice inside one expression that builds its element types.
- `DomainLineOf` reads `_definition` twice inside one call that builds its `DomainLine`.

Each read is inside a lazy member, so the argument is evaluated at most twice, never on construction, and never if the member is unused. Every call site today passes a value already in hand, so nothing is observable. But "at most twice" is not "at most once", and drake.dlt promises once.

Why I did not close it in the engine. The method prelude of step 2 works because a method's body reads the bare name and the parameter carries the underscore. A factory body reads the underscore name, because inside the instance the bare name is the member. So the memoizing binding would need a third name, and the parameter would have to be renamed to make room for it. Every generated companion calls `TypeName("X", _namePackage = ...)` by that underscore name. Renaming the parameter breaks the named-argument convention across the whole corpus.

There is an authoring route instead. In `Domain`, the second site could read the member the first site bound, `TypeDictionary(typeDefinition)`, and the parameter is then read once. `DomainLineOf` and `TypeDictionary` could bind a `loc` for the parameter and read that. That is a drake edit in three definitions, not an engine change, and it makes the promise true by the same mechanism the promise rests on: the body is the prelude. It also tells an author something about drake, that a parameter read twice in a body is a smell the language does not hide for you.

Which of the two do you want to take up first?

---

**[Dev]** Accept, the authoring route for the three bodied factories first

**[Claude]** Building it. The compiled classes from your run 2 are the committed engine and parser, so the whole trio can be regenerated without sbt. First the three definitions and the member names I must not shadow.

Two loose ends: the Scala diff shows a stray added blank line per file, and the emit check wants a different argument form. I'll look at both.

The round-trip check reports two drifting pairs and my filter hid their names. I need to see they are the standing exclusions and not my three.

The drift is exactly the three standing exclusions, so the three edited pairs round-trip. Nothing else is needed before your run.

Built, drake-first, with the trio regenerated by the compiled CLIs and no engine change:

- **Domain** reads the member its first line bound: `TypeDictionary(typeDefinition)`.
- **TypeDictionary** binds `loc domainDefinition TypeDefinition _domainDefinition` and both sites read the local.
- **DomainLineOf** binds `loc definition TypeDefinition _definition` and both sites read the local.

That `loc x T _x` line is step 2's method prelude authored in drake, and it is now the idiom for a bodied factory that reads a parameter more than once. The generated Scala differs by the renames plus one `lazy val` per file. The dlt STATUS block records the idiom, the CHANGELOG has its entry, and the record is [git-record-2026-09-18-1250](draco-git-record/git-record-2026-09-18-1250).

**Run.** Expected: 623 tests / 44 suites, all green, every headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-18-1250 | sed '1d' > /tmp/msg && git add -A -- src CHANGELOG.md draco-git-record/git-record-2026-09-18-1250 && git commit -F /tmp/msg -- src CHANGELOG.md draco-git-record/git-record-2026-09-18-1250 && git push
```

The actor-minting factory's strictness remains the open point, whenever you want to take it up.

*(Delegated action — Dev ran the suite and committed without pasting the output; the next reply finds `578c92e` on main and the tree clean.)*

---

**[Dev]** Accept, take up the actor-minting factory next

**[Claude]** Checking first whether the bodied-factory commit landed, then reading the actor template so the recommendation is concrete.

578c92e is in, so the tree is clean. Here is the one recommendation for the actor-minting factory, with the shape it would render, for accept or correct before I build.

**What the parameter actually is.** Ten definitions carry one: Draco's `problems`, Emitter's `received`, and eight example-domain actors whose parameter is a peer's `ActorRef`. Every one is read inside the actor's `receive` action, per message, as `session.set("consumer", consumer)`. So it is not read once at start, as I said in the handover. It is read on every message, by name, exactly as a method body reads its parameters.

**Recommendation: fold it into step 2's convention, because `actorType` is a method.** The parameter arrives as `_consumer: => ActorRef[JSON]`, and the block opens with the prelude `lazy val consumer = _consumer` before `new Actor`. The action bodies already read the bare name, so no drake changes. In drake's terms nothing new is said: a `par` is lazy, `now` overrides, and an actor's wiring is a value its behaviour reads on first use. The rendered head becomes:

```text
def actorType(_consumer: => ActorRef[JSON]): ActorType = {
  lazy val consumer: ActorRef[JSON] = _consumer
  new Actor[JSON] { ... }
}
```

The other branch, declaring the actor-minting factory strict by nature in drake.dlt, would make `par` mean two things under one keyword, and I have no semantic reason for it. The ref is a value; deferring its first read changes nothing observable, and a peer that does not exist yet is not a case the corpus has.

**Cost.** One engine site, the `actorDecl` line and its block, plus ten projections: two in main, eight in the example domains. GEN MAP stays 28 / 20 if I sweep all eight. Callers pass values, so nothing else moves. No test needed beyond the gates.

Accept, and I build it as a fourth step in the same one-run shape.

---

**[Dev]** Accept, build the fourth step

**[Claude]** Building step 4. One refinement from the shape I showed, stated up front: the prelude renders as a lazy member of the actor instance, not a local before `new Actor`, because drake calls this construct a `factory`, and step 3's convention is that the factory's instance binds each parameter once. The engine's actor body keeps its indentation that way, and the bare names the action bodies read resolve to the member.

The sweep is exact and no caller uses `actorType` as a function value. Now the docs, record and memory in one pass.

Step 4 is built and ready for your run. Eight `now` marks exist in the corpus, not eleven as I first wrote from memory, and the dlt and commit message now say 8.

**What changed.** The actor template in [DracoGenerator.scala:1520](src/mods/scala/draco/DracoGenerator.scala:1520) now spells the actor-minting factory's parameters through the same method-parameter renderer as step 2, and the minted actor binds each one once as its first member. Nullary actors render exactly as before, so the scenario's actors and every other `actorType()` are byte-identical. Ten projections were swept to the engine's form: Draco and Emitter in main, and the eight example-domain actors, matching and differing alike, so GEN MAP holds. No caller moves, and no caller uses `actorType` as a function value.

**One refinement, stated rather than hidden.** The shape I showed you had the prelude as a local before `new Actor`. It renders instead as a lazy member of the actor, which is step 3's own prelude and keeps the actor body's indentation. The two are observationally identical, and the record says so.

Rendered head, for the record:

```text
def actorType(_consumer: => ActorRef[JSON]): ActorType = new Actor[JSON] {
  override lazy val typeDefinition: TypeDefinition = Creator.typeDefinition
  lazy val consumer: ActorRef[JSON] = _consumer
  ...
```

**Run.** Expected: 623 tests / 44 suites, all green, every headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-18-1517](draco-git-record/git-record-2026-09-18-1517).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-18-1517 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1517 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1517 && git push
```

This closes the four steps of EVALUATION. Next is `ifThenElse` as the second dyn on Presence, and the Boolean-to-Presence bridge is the design point I will surface before building anything.

*(Delegated action — Dev ran the handed-over commands: `sbt test` 623 passed, 0 failed, every headline at baseline — GenDrake runs 96 of 96; GEN MAP 28 match, 20 differ, 0 error, 0 missing (of 48); PON 80 numbers, 550 expressions, 42 discrepancies; canonical 7 differ; surface losses 5 fields across 108 types [expression form 2] [type form 3]; parse scope 98 draco + 10 mods, 0 held back; scenario 23 files, 0 rejected, 0 drifted, 23 clean; the forest 1 of 2; committed and pushed as `374f168`, 15 files changed, `578c92e..374f168`.)*

---

**[Dev]** Accept, surface the Boolean-to-Presence bridge for ifThenElse

**[Claude]** 374f168 is in, so the four steps of EVALUATION are closed. Before framing the bridge I want the corpus's actual conditionals in front of me, since they are what `ifThenElse` has to express.

Landed, and the memory notes carry it. Now the bridge, one point.

The corpus has five conditionals, in three definitions: TypeName's `namePath` and `resourcePath`, TypeLoader's `rooted`, and two in DefinitionPath. Every condition is a Boolean from a host predicate: `namePackage.isEmpty`, `endsWith(...)`, an `exists` joined by `||`. None of them is a Presence yet.

Your design has `ifThenElse` dispatch on a Presence: Present takes the then-branch, Absent the else-branch. The point that decides everything else is what the Present carries when it came from a Boolean. Take `namePath`:

```text
if (namePackage.isEmpty) name else s"${namePackage.mkString(".")}.${name}"
```

Read one way, the presence here is the package itself. `namePackage` is a `Presence[[String]]`, the then-branch uses the value, and the construct is `fold`, which already exists: `namePackage.fold(name, pkg => join(".", pkg) ++ "." ++ name)`. On that reading `ifThenElse` is not a second dyn at all, and the Boolean never arises because the author writes the presence directly.

Read the other way, the condition is a fact, not a value. `namePackage.isEmpty` holds or it does not, and nothing is carried into the then-branch. Then the presence is of nothing, a `Presence[Unit]` in Haskell's terms, `Present(())` or `Absent`, and `ifThenElse(p, t, e)` is `fold` with a then-branch that ignores its argument. The bridge from a Boolean is then one declared symbol, Haskell's `guard`, which turns a truth into a presence of nothing. That is the only piece that would need to exist, and a host predicate like `endsWith` stays host-opaque behind it.

The two readings give different corpus edits and a different dlt sentence, so before anything else: in `namePath`, is the presence the package, or is it nothing?

---

**[Dev]** This will take more discussion, because us of the term "package" is involved, which a JVM related term. The problem arises with the question of how to map between packages and modules (as between Scala and Haskell) using a reliable least common denominator approach to figuring it out. Remember I want to use the empty space with the empty name path as a local scratch module that won't be used as a permanent reference to  a real instantsiable module.

**[Claude]** Let me restate the reading before building on it.

The least common denominator is a name path: a sequence of segments, with the type's name last. A Scala target spells the segments before the name as a package, lower-case and dot-joined. A Haskell target spells them as a module, capitalised and dot-joined. Neither term belongs in drake; the definition carries only the sequence. The empty sequence is a module too, the local scratch one: a definition there can be worked on and projected, but nothing permanent may refer to it, so a `from` or `modules` reference into the empty path is an error rather than a bare host type.

If that is the reading, it changes the `namePath` question I asked, and in a way that removes it. The conditional in `namePath` exists only because the empty path was treated as an absence needing a special case. Once the empty path is a module like any other, the spelling is uniform: join the segments and the name with the separator, and an empty sequence contributes no segments. The two conditionals in TypeName dissolve into one `join`, and the presence question does not arise there at all.

One thing I want confirmed before going further, because it decides the drake surface: does the reference residue from the parser work now flip? Today a reference with no package, `Map` in Dictionary, is spelled bare and read back as owning the referring package, because the surface cannot say "no package". Under your reading, bare means the scratch module, so `Map` cannot be spelled bare at all and needs a foreign spelling of its own. Is that the intent?

---

**[Dev]** Considering the use of "Map" in Dictionary.json:


```
{
  "kind": "Fixed",
  "name": "value",
  "valueType": {
    "()": [
      "Map",
      "K",
      "V1"
    ]
  },
  "value": {
    "()": [
      "kvMap.updated",
      "key",
      "value"
    ]
  }
}
```


versus


```
{
  "kind": "Fixed",
  "name": "value",
  "valueType": {
    "{}": [
      "K",
      "V1"
    ]
  },
  "value": {
    "()": [
      "kvMap.updated",
      "key",
      "value"
    ]
  }
}
```


The use of "Map" in  Dictionary.json is not necessary. Are there other examples?

**[Claude]** Measuring rather than recalling: every type-form head and leaf in the corpus, so the host names show up as a list.

Yes, and the drake surface never says `Map` at all. `Dictionary.drake` spells the slot `{K, V1}`; the `Map` head in the JSON is the parser's encoding of the sugar, chosen on 2026-09-16 when `[T]`, `{T}` and `{K, V}` were treed as Seq, Set and Map applications. Your second snippet, with `{}` as the node key and no head, is the encoding the surface actually implies. The same host name also sits in Dictionary's derivation, where `from {K, V}` comes back as a foreign `Map` reference.

Measured over the corpus's value types and type parameters:

| host name in the JSON | count | what drake already writes |
|---|---|---|
| `Seq` as a Parametric head | 70 | `[T]` |
| `Map` as a Parametric head | 8 | `{K, V}` |
| `Seq.empty` as a value tree | every `[]` default | `[]` |
| `mutable.Set[...]` as host text | 8 | `mut {T}` |

Those are the same case as yours: the surface is neutral and the carrier is not. A plain `{T}` Set never occurs, so `Set` appears nowhere.

A second group is different in kind. These heads have no neutral spelling in drake today, and the host name is doing real work:

| head | count |
|---|---|
| `Option` | 10 |
| `ActorRef` | 6 |
| `Consumer` (Java's functional interface) | 6 |
| `LazyList` | 5 |
| `java.util.List` | 4 |
| `Iterator`, `Array`, `java.util.ArrayList` | 1 each |

`Option` is the one with a draco form already waiting, `Presence(T)`. The rest are host residues of the kind the README's table records, and the leaves (`String`, `Int`, `Json`, `URI`, the evrete and pekko types) are a third, larger set.

So my reading of your question: the first group is an encoding defect to fix in the carrier, with the drake unchanged, and the second is a separate question about which host names drake should own a form for. Is that the split you meant?

---

**[Dev]** Yes. The latter all need some form of drake target language neutral representation.

**[Claude]** Recording the decisions before replying, since three of them are new: the neutral node keys for the collection sugar, the second group needing drake forms, and the empty name path as the scratch module.

Agreed, and recorded. One recommendation on where to start, for accept or correct.

**Start with the first group, the carrier catching up with the surface.** The parser writes `[T]` as a Seq application and `{K, V}` as a Map application, and the emitter and engine read those heads back. Changing the encoding to the node keys your snippet shows, `[]` and `{}`, touches the parser, the emitter, the engine's type spelling, and the `[]` value default, and then the corpus re-canonicalizes with the compiled CLIs. No drake file moves, no design is open, and the gates decide it. It also clears the ground: every neutral form for the second group then lands in a carrier that is already neutral, instead of beside `Seq` heads that contradict it.

The second group comes after, one head at a time, each surfaced before it is built. `Option` goes first because `Presence(T)` already exists and the ten uses are in the engine's own definitions, so the swap is authored in drake and exercises the runtime. `ActorRef`, `Consumer`, `LazyList` and the Java lists each need a form drake does not have yet, and those are the discussions.

The `ifThenElse` bridge is parked behind both. Under your scratch-module reading, TypeName's two conditionals dissolve into a `join`, so the corpus's real conditionals are the three in TypeLoader and DefinitionPath, and they will look different once their `Option` uses are Presences.

Accept, and I build the encoding fix next.

---

**[Dev]** Accept, build the encoding fix

**[Claude]** Building. First the exact code at every site that writes or reads the collection heads, in parser, emitter, engine, type-form helpers and tests, plus a check that a Python load-and-dump reproduces the corpus JSON byte for byte, which decides whether I can re-encode the corpus myself in one run.

All remaining edits are independent of each other, so one pass: the renderer contract and its test samples, the two DrakeParseTest sites, the corpus re-encode, then dlt, CHANGELOG, record and memory.

The encoding fix is built and ready for your run, as one run.

**What changed.** The bracket is now the node: `[T]` parses to `{"[]": [T]}` and `{T}` or `{K, V}` to `{"{}": [...]}`, and the `[]` and `{}` value defaults parse to `{"[]": []}` and `{"{}": []}` instead of a `Seq.empty` path. The Scala target alone names `Seq`, `Set` and `Map`, in [DracoGenerator.scala:185](src/mods/scala/draco/DracoGenerator.scala:185) for a type and in its expression renderer for a value. The parser, the drake emitter and `TypeForm.neutral` each gained the two node cases and lost their Seq, Set and Map sugar cases, so the encoding is unambiguous. The renderer contract gained a `collection` slot, both template sets implement it, and the mirror test has three new sample trees. No drake file moves.

**Corpus.** Twenty definitions re-encoded, by a Python walk rather than the compiled parser, which still writes the old encoding. The walk is byte-exact because the JSON printer reproduces every corpus file but one, and that one carries none of this. The head rewrite ran only inside type-form subtrees, so CLI's `Map(...)` value call stays an application. Dictionary's derivation `from {K, V}` still comes back as a reference named `Map`, deliberately: that is a reference, not a type form, and belongs to the scratch-module discussion.

| measured | count |
|---|---|
| collection heads re-encoded | 78 |
| empty-collection values re-encoded | 22 |
| files changed | 20 |

**Run.** Expected: 623 tests / 44 suites, all green, every headline unchanged. `ScenarioGenTest` asserts JSON equals `parse(drake)` exactly, so it is also the check that my encoding equals the new parser's, key for key.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-18-1633](draco-git-record/git-record-2026-09-18-1633).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-18-1633 | sed '1d' > /tmp/msg && git add -A -- src CHANGELOG.md draco-git-record/git-record-2026-09-18-1633 && git commit -F /tmp/msg -- src CHANGELOG.md draco-git-record/git-record-2026-09-18-1633 && git push
```

Next in the agreed order is the second group, one head at a time, starting with `Option` becoming `Presence`. I will surface its shape before building.

*(Delegated action — Dev ran the handed-over commands: `sbt test` 623 passed, 0 failed, every headline at baseline — GenDrake runs 96 of 96; GEN MAP 28 match, 20 differ, 0 error, 0 missing (of 48); PON 80 numbers, 550 expressions, 42 discrepancies; canonical 7 differ; surface losses 5 fields across 108 types [expression form 2] [type form 3]; parse scope 98 draco + 10 mods, 0 held back; scenario 23 files, 0 rejected, 0 drifted, 23 clean; the forest 1 of 2; committed and pushed as `0b0e63a`, 29 files changed, `374f168..0b0e63a`.)*

---

**[Dev]** Accept, surface the shape for Option to Presence

**[Claude]** 0b0e63a is in. Before shaping anything I need the ten `Option` uses as they are written, and who consumes each, since a swap to `Presence` changes what the callers see.

Landed. The shape, then the one question it turns on.

**The ten uses are five members.** `DefinitionPath.source`, `Dictionary.get`, and TypeLoader's `readDefinition`, `loadFromResource` and `tryLoad`, each `Option(T)` in its signature and once more in its `value` line. One of the five cannot move: `Dictionary.get` overrides the host's `Map.get`, whose result type is fixed, the same situation that made its parameter `now`. So the swap is four members, all in the loader path, and the engine's one hand-written consumer, `tryLoad(...).isDefined`, becomes a `fold`.

**The bodies are where the shape is decided.** Each body ends at a host boundary that yields an Option: `found.headOption`, circe's `.toOption`, and `flatMap` chaining one loader step into the next. `Presence` has `fold` and nothing else. So either the host's Option becomes a Presence at the boundary, once per body, through a declared symbol like `join` in drake.dlt, rendered by the Scala target as a fold into `Present` or `Absent`, and the chaining is written as `fold` from then on. Or `Presence` grows the combinators the bodies use, `map` and `flatMap` as dyns on the family defined by dispatch like `fold`, and the bodies are written in Presence throughout, with the boundary symbol still needed once at the outermost host call.

My reading is the first: the boundary symbol is unavoidable either way, `fold` already expresses what `flatMap` does here, and adding combinators is a runtime decision that deserves its own discussion rather than riding on this swap. Is the boundary the place you want the conversion, with `fold` doing the chaining?

---

**[Dev]** Yes, the boundary, with fold doing the chaining

**[Claude]** Agreed and building. First the two definitions in full and their projections, since the bodies are where the edits land, and the engine's symbol table where the boundary symbol goes.

Built and ready for your run.

**What changed.** Four members on the loader path now declare `draco.drake.Presence(T)`: `DefinitionPath.source`, and TypeLoader's `readDefinition`, `loadFromResource` and `tryLoad`. Each converts once at its host boundary through the new declared symbol `presence`, and from there the definition chains with `fold`. `loadType` reads `tryLoad(typeName).fold(TypeDefinition(typeName), rooted)`. `Dictionary.get` keeps `Option`, because it overrides the host's `Map.get`, the same reason its parameter is `now`.

**The symbol.** `presence x` sits beside `join` in the dlt's SYMBOLS. The Scala target renders it as a `match` into `Present` or `Absent`, in [DracoGenerator.scala:205](src/mods/scala/draco/DracoGenerator.scala:205). A match rather than `map` and `getOrElse` because each branch is typed against the declared result, so `Absent()` infers its parameter; `Presence` is invariant, and a least upper bound would be an existential. I probed the inference with the compiler from the exported classpath before authoring, and the fold chaining, the `loadType` fold and the engine's `fold(false, _ => true)` all compile. That probe is a check I would reuse.

**Trio.** Drake authored first. JSON by the compiled parser, since `presence(...)` is an ordinary call and the circe read inside it stays an opaque leaf. Scala by the compiled engine with the one symbol it lacks hand-rendered to the new spelling. Both definitions round-trip under the checker. The engine's one hand-written consumer of the old API moved with it.

**Run.** Expected: 623 tests / 44 suites, all green, every headline unchanged.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-18-1700](draco-git-record/git-record-2026-09-18-1700).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-18-1700 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1700 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-1700 && git push
```

Next heads in the second group are `ActorRef`, `Consumer`, `LazyList` and the Java lists, each surfaced before it is built.

*(Delegated action — Dev ran the handed-over commands: `sbt test` 623 passed, 0 failed, every headline at baseline — GenDrake runs 96 of 96; GEN MAP 28 match, 20 differ, 0 error, 0 missing (of 48); PON 80 numbers, 550 expressions, 42 discrepancies; canonical 7 differ; surface losses 5 fields across 108 types [expression form 2] [type form 3]; parse scope 98 draco + 10 mods, 0 held back; scenario 23 files, 0 rejected, 0 drifted, 23 clean; the forest 1 of 2; committed and pushed as `20778f3`, 11 files changed, `0b0e63a..20778f3`.)*

---

**[Dev]** Accept, surface the shape for ActorRef next

**[Claude]** 20778f3 is in. Before shaping `ActorRef` I need its uses as written, how a rule reaches the ref, and what draco already has that names the same thing, since Assembly and Binding exist.

Landed. One reading, for you to confirm or correct before anything is shaped further.

`ActorRef(M)` appears in six actor-minting parameters, and the actor does two things with it: seeds it into the rule session under a name, and sends to it with the host's `!`. Draco already names what the parameter is from the other side. `AssemblyValidator` reads `ActorRef[M]` as "the message type a peer receives", and a `Binding` names the slot by member and parameter, so the assembly is the wiring and the parameter is the endpoint the wiring fills.

Read that way, the least common denominator across targets is not a reference at all. It is a consumer of M: something that accepts an M and has an effect. In drake's own notation that already exists, the arrow to `Unit`: `par consumer draco.format.json.JSON -> Unit`. Sending becomes application, `consumer(report)`, with no `!` symbol to declare. Scala realizes the endpoint as a Pekko ref wrapped in a function, and the tell lives in the one place the assembly spawns and binds; Haskell realizes it as a function into `IO`, which is what it is.

The same reading covers the next head unasked. `Consumer` in `Rule` and `RuleType`, six uses, is Java's functional interface for exactly "accepts an M", so the two heads collapse into one form drake already has.

Is a consumer of M what an `ActorRef` means in draco, or does the reference itself carry something the function does not, such as identity, that the assembly needs to keep?

---

**[Dev]** Yes, a consumer of M, the assembly keeps identity. This will have to be changed later when the Pekko dependency is integrated into draco through type definitions in drake that have Pekko implementations in ScalaTarget. The term "ActorRef" is provisional because that's the term used in Pekko as the address of the message queue of a specific typed message actor..

**[Claude]** Recorded, with your wording that the term is provisional until Pekko is integrated through drake definitions with implementations in the Scala target. Before I put the concrete shape in front of you I need the two places the reference travels beyond the factory parameter: the rule actions that send through the session environment, and the assembly wiring.

The two places are clear. Here is the shape, for accept or correct.

**The form.** A parameter typed `ActorRef(M)` becomes `M -> Unit`, drake's own arrow, and sending becomes application. `Creator`'s parameter reads `par consumer draco.format.json.JSON -> Unit`, `Output`'s send reads `mon consumer(report)`, `Input`'s reads `worldConsumer(position)`. The Scala target already spells that arrow as a function type, so the minted actor becomes `actorType(_consumer: => JSON => Unit)` with no engine change.

**Where the reference is kept.** The assembly holds the Pekko refs, in `AssemblySpawner`, and converts each one at its boundary, once, as `ref ! _`, when it hands a member its constructor arguments. The four chain tests do the same at their spawn line. `AssemblyValidator` reads the message type from the arrow's source instead of from `ActorRef[M]`. Nothing else knows a ref exists.

**The rule side.** Four `OriginateReport` actions read the consumer back out of the session environment through host text, typed as Pekko's `ActorRef`. They read it as `JSON => Unit` and apply it instead, which removes the last four `org.apache.pekko` leaves from the corpus.

| moves | count |
|---|---|
| drake parameters, six actors | 6 |
| drake-less world Consumer and Provider, JSON only | 2 |
| send sites in drake | 2 |
| rule actions with host text | 4 |
| hand-written: validator, spawner adapters, chain tests | 1 + adapters + 4 |

**Out of scope, named.** `Rule` and `RuleType` type `pattern` and `action` as Java's `Consumer(Knowledge)` and `Consumer(RhsContext)`. It is the same form, but those are Evrete's registration hooks rather than draco endpoints, and I would take them as their own step.

No engine change, no new symbol, and the example projections regenerate with the compiled engine, so this is one run. Accept, and I build it.

---

**[Dev]** Accept, build the ActorRef form

**[Claude]** Building. First the exact lines that move: the six drakes, the four drake-less rule definitions, the twelve projections, and every hand-written site that holds a ref.

The checker's drift dropped from 11 at HEAD to 5, because the six drake-less definitions I authored as trees now round-trip. That is worth stating in the record.

Built and ready for your run, as one run: there is no engine change.

**What changed.** The six actor-minting parameters typed by the host's `ActorRef(M)` are now `(M -> Unit)`, and the two sends in drake are applications, `consumer(report)` and `worldConsumer(position)`. The arrow must be parenthesized in a name-type-value slot. My first pass wrote it bare, and the parser read `JSON -> Unit` as type `JSON` with a default value of `-> Unit`, which the engine faithfully rendered as nonsense. That is fixed and the JSON shows the arrow node.

**Where the ref lives now.** The assembly keeps the host's refs, and each boundary converts once with `ref ! _`: the three adapters in `WorldAssemblyTest`, one in `AerialAssemblyTest`, and the spawn line in each of the three chain tests. `AssemblyValidator` reads the message type from the arrow's source. `DracoActorTest` passes a list, not a ref, and my regex touched it; I reverted that line.

**Corpus.** Six drakes parsed by the compiled parser. Six drake-less definitions authored as the parser writes trees: world Consumer and Provider, and the four `OriginateReport` actions, which now read the consumer from the session as a function and apply it, so the last four `org.apache.pekko` leaves are gone. Twelve projections moved: the four matching Creators by the compiled engine, the eight differing ones by hand on exactly the moved lines. A side effect worth having: the mods checker's drift fell from 11 to 5, because those six drake-less definitions round-trip for the first time.

**Named and not moved.** `Rule` and `RuleType` still type `pattern` and `action` as Java's `Consumer`. Same form, its own step. `AssemblySpawner` keeps its sequence of refs, which is the identity you said the assembly keeps.

**Run.** Expected: 623 tests / 44 suites, all green, GEN MAP 28 match / 20 differ, every other headline unchanged. The chain and assembly tests exercise the boundary for real.

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sbt test 2>&1 | tee /tmp/sbt-test.log | grep -E "GEN MAP|surface losses|parse scope|PON CORPUS|CANONICAL|scenario in|forest runs|GenDrake runs|CO-DECLARATION|error|Failed|FAILED|^\[info\] Tests:"
```

**Commit,** once green. The record is [git-record-2026-09-18-2333](draco-git-record/git-record-2026-09-18-2333).

```bash
cd /Users/edwardburke/IdeaProjects/nexonix && sed -n "/^--- MESSAGE ---$/,\$p" draco-git-record/git-record-2026-09-18-2333 | sed '1d' > /tmp/msg && git add -A -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-2333 && git commit -F /tmp/msg -- src DRACO.md CHANGELOG.md draco-git-record/git-record-2026-09-18-2333 && git push
```

Next heads are `Consumer` in the rule types, `LazyList` in Primes, and the Java lists in Draco, Emitter and the two received-message types. Each gets surfaced before it is built.

---

## Status

This chapter transcribes the September 18, 2026 session, opened from `c60a9ed` with the new-session prompt written at chapter 78's close; its last two exchanges fall after midnight UTC and are the same evening local. The three commits that prompt said might be pending — the chapter 78 journal, the docs sync through chapter 78 and the AGENTS.md decision — were already on main, so the tree was clean at the start. **Opening state (carried from chapter 78's close):** `c60a9ed`, 622 tests / 44 suites green, 98 draco types in scope / 108 measured, `GenDrake` 96 of 96, surface losses 5 (expression form 2, type form 3 — ActorAspect and Value, the standing exclusions), lazy steps 1 and 2 landed, the agreed order: lazy step 3 → `ifThenElse` as the second dyn on Presence with the Boolean-to-Presence bridge surfaced first. **Commits landed, five, every one green before its push, and the count 623 / 44 throughout:** **`b404d7e` "Lazy by default, step 3: a factory parameter is by-name, and the factory body is its prelude"** (623/44) — the engine's one factory site spells `_x: => T`; a bodiless factory already binds `override lazy val x = _x`, a bodied one reads `_x` inside its lazy members, so no prelude is needed; 122 parameters in plain `apply` blocks across 44 projections in main, the scenario and the example domains (the sizing's 127 remeasured as 101 main, 13 scenario and 19 example-domain factory parameters, of which the plain ones moved); `PresenceTest` gains the test that `Present(sys.error(...))` constructs and two reads evaluate once; run 1 was 490 / 133 failed, every failure a parser gate, because `Drake.parse` passed cursor-consuming expressions (`Monadic(parseValue(c))`, `Factory(valueType, parseSection(...), ...)`) as factory arguments and by-name ran them at first read rather than in token order — the first observed consequence of laziness, fixed at the six call sites by binding the argument to a `val` before the call, "where an effect is sequenced under call-by-need", recorded as a DRACO.md gotcha. **`578c92e` "A bodied factory reads a parameter once: the prelude is authored where a body read it twice"** (623/44) — the authoring route Dev chose over an engine change: `Domain` reads the member its first line bound (`TypeDictionary(typeDefinition)`), `TypeDictionary` and `DomainLineOf` bind `loc x T _x` — step 2's method prelude authored in drake, now the idiom — three drake lines, JSON and Scala regenerated by the compiled CLIs, no engine change. **`374f168` "Lazy by default, step 4: the actor-minting factory's parameter is by-name, bound once by the actor"** (623/44) — the parameter is read per message inside `receive` (`session.set("consumer", consumer)`), not once at start as the step-3 handover had said, so `factory ActorType` follows step 3 rather than a convention of its own: `def actorType(_consumer: => ActorRef[JSON])` with `lazy val consumer = _consumer` as the minted actor's first member (a lazy member, not a local before `new Actor`, the one refinement stated); ten projections, two in main and eight example-domain; the four steps of EVALUATION closed, every parameter, binding and method call-by-need and `now` marking the 8 the host forces (not eleven, as first written from memory). **`0b0e63a` "The collection sugar is carried neutrally: the bracket is the node, and no host head enters the corpus"** (623/44) — from Dev's Dictionary.json reading: `[T]` parses to `{"[]": [T]}`, `{T}` and `{K, V}` to `{"{}": [...]}`, the `[]`/`{}` value defaults to `{"[]": []}`/`{"{}": []}` instead of a `Seq.empty` path; the Scala target alone names `Seq`, `Set`, `Map`, in one function each, and the renderer contract gains a `collection` slot; the corpus measured 70 `Seq` heads, 8 `Map` heads, every `[]` default as `Seq.empty` and 8 `mutable.Set` host texts before the fix; 78 collection heads and 22 empty-collection values re-encoded in 20 files by a byte-exact Python walk, no drake file moved; Dictionary's derivation `from {K, V}` still returns as a reference named `Map`, deliberately, because a reference is not a type form. **`20778f3` "Option becomes Presence on the loader path: one conversion at the host boundary, then fold"** (623/44) — four of the five `Option` members move (`DefinitionPath.source`, TypeLoader's `readDefinition`, `loadFromResource`, `tryLoad`; `Dictionary.get` keeps `Option` because it overrides the host's `Map.get`); the new declared symbol `presence` beside `join` in the dlt's SYMBOLS, rendered by the Scala target as a `match` into `Present` or `Absent` typed against the declared result because `Presence` is invariant; `loadType` reads `tryLoad(typeName).fold(TypeDefinition(typeName), rooted)`; the boundary rather than Presence combinators, by Dev's answer "Yes, the boundary, with fold doing the chaining"; the inference probed with the compiler from the exported classpath before authoring. **Built at the close, not yet run:** the ActorRef form — the six actor-minting parameters typed `ActorRef(M)` become `(M -> Unit)`, drake's own arrow, parenthesized in a name-type-value slot (a bare `JSON -> Unit` parsed as type `JSON` with default `-> Unit`, caught and fixed); the two sends in drake become applications `consumer(report)` and `worldConsumer(position)`; the assembly keeps the host's refs and converts each once with `ref ! _` (three adapters in `WorldAssemblyTest`, one in `AerialAssemblyTest`, the spawn line in the three chain tests); `AssemblyValidator` reads the message type from the arrow's source; the four `OriginateReport` actions read the consumer from the session as a function and apply it, removing the last four `org.apache.pekko` leaves; six drake-less definitions authored as trees drop the mods checker's drift from 11 to 5; twelve projections moved; no engine change; record `git-record-2026-09-18-2333`, expected 623 / 44 green with GEN MAP 28 / 20. Dev's wording is recorded: "The term "ActorRef" is provisional because that's the term used in Pekko as the address of the message queue of a specific typed message actor", to be changed when Pekko is integrated through drake definitions with implementations in the Scala target. It landed the next session as `2fcf58e`. **The discussion, transcribed verbatim and conducted one point per reply:** the bridge question — in `namePath`, is the presence the package, or is it nothing? — answered by Dev with the term "package" as JVM-bound, the least common denominator a name path whose segments a Scala target spells as a package and a Haskell target as a module, and "the empty space with the empty name path as a local scratch module that won't be used as a permanent reference"; under that reading TypeName's two conditionals dissolve into a `join` and the corpus's real conditionals are the three in TypeLoader and DefinitionPath; Dev's "The use of "Map" in Dictionary.json is not necessary. Are there other examples?" and the census that split the host names into the encoding defect (Seq, Map, Seq.empty, mutable.Set — the surface already neutral) and the heads with no drake form (Option 10, ActorRef 6, Consumer 6, LazyList 5, java.util.List 4, Iterator/Array/ArrayList 1 each), with Dev's ruling "The latter all need some form of drake target language neutral representation"; the ActorRef reading — a consumer of M, not a reference, `Consumer` collapsing into the same form — confirmed "Yes, a consumer of M, the assembly keeps identity". **Counts across the arc:** 622/44 → 623/44 and held there through five commits; types 98/108 throughout; `GenDrake` 96 of 96; losses 5 (expression form 2, type form 3) at every run; GEN MAP 28/20 throughout; PON 80 numbers / 550 expressions / 42 discrepancies, canonical 7; scenario 23 files, 0 rejected, 0 drifted, 23 clean, except run 1 of step 3 at 12 rejected / 11 clean; `now` marks 8; the mods checker's drift 11 → 5 in the unrun ActorRef change. **Queued at the close:** the ActorRef run and commit (`2fcf58e`, next session); the remaining heads of the second group, each surfaced before it is built — `Consumer(Knowledge)` and `Consumer(RhsContext)` in `Rule` and `RuleType` (Evrete's registration hooks, same form, its own step), `LazyList` in Primes, the Java lists in Draco, Emitter and the two received-message types, and the leaves (`String`, `Int`, `Json`, `URI`, the evrete and pekko types) after them; the `ifThenElse` bridge, parked behind the carrier work — `guard` turning a truth into a presence of nothing is the one piece that would need to exist, and the three real conditionals will look different once their `Option` uses are Presences; the reference residue under the scratch-module reading — whether a bare reference can still mean "the referring package" once bare means the scratch module, and Dictionary's `from {K, V}` coming back as a foreign `Map`; `mut {T}` as host text (8 sites) until `mut` moves to the element; Presence combinators (`map`, `flatMap` as dyns by dispatch) deferred as a runtime decision of its own; Dev's question about abstract types and codecs, still open for discussion when he raises it; the three infix rule conditions de-treed by the sweeps (PrimesFromNaturalSequence, RemoveCompositeNumbers, SelfDeclaration) and #61's lambdas; `draketarget.DomainAspect` and the aspect-named target types; the `AGENTS.md` copy and chapter 76's standing items. The next session opens from `20778f3` with the ActorRef form in the working tree awaiting its run.
