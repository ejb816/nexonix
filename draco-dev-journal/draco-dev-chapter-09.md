# Draco Dev Journal — Chapter 9

**Session date:** March 23, 2026 (continued)
**Topic:** SLF4J/Pekko Logging Suppression, NaturalActor ActorRef Fix, Triadic Symmetry Decision, Aspect-Based Naming Convention, TypeName Refactoring

---

## Session Start — Logging Noise Plan

> **Dev:** Implement the following plan:
> # Suppress SLF4J and Pekko Logging Noise
> *(A structured plan identifying dual SLF4J providers and Pekko startup INFO as root causes, with two changes: remove `slf4j-jdk14` from build.sbt and add `logback-test.xml`)*

**Draco:** *(Reads build.sbt, removes `slf4j-jdk14` dependency declaration and its `libraryDependencies` entry, creates `src/test/resources/logback-test.xml` with Pekko at WARN and root at INFO)*

Two changes made:

1. **`build.sbt`** — Removed the `slf4j-jdk14` dependency. `logback-classic` already provides the SLF4J binding.
2. **`src/test/resources/logback-test.xml`** — Created with Pekko logger set to WARN and root logger at INFO.

---

## The SLF4J Replay Warning

> **Dev:** This showed up after your fix:
> ```
> SLF4J(W): A number (1) of logging calls during the initialization phase have been intercepted and are
> SLF4J(W): now being replayed. These are subject to the filtering rules of the underlying logging system.
> SLF4J(W): See also https://www.slf4j.org/codes.html#replay
> ```

**Draco:** That's an SLF4J 2.x replay warning — Pekko creates a logger before SLF4J finishes binding to logback. The fix is to tell SLF4J the exact provider class so it binds immediately, skipping ServiceLoader discovery. This requires forking the test JVM.

*(Adds to build.sbt:)*

```scala
Test / fork := true,
Test / javaOptions += "-Dslf4j.provider=ch.qos.logback.classic.spi.LogbackServiceProvider",
```

The replay warning happens because Pekko creates a logger during static initialization, before SLF4J finishes discovering logback via `ServiceLoader`. The explicit provider property makes SLF4J bind immediately without ServiceLoader scanning, eliminating the window where log calls get queued.

---

## Logback for Dreams

> **Dev:** Back to the log issue — should there be a logback-main.xml as well as the logback-test.xml that you created/modified?

**Draco:** No. `logback-test.xml` in `src/test/resources/` is sufficient — it's picked up automatically by Logback during test runs. A main-scope config would only be needed if you're running the application.

> **Dev:** This framework is going to have a built-in service/application Dreams (Domain Rules Editor Actor Message Service). Does this change your opinion?

**Draco:** Yes — if Dreams will run as an application, add a `src/main/resources/logback.xml` with the same Pekko suppression.

> **Dev:** Yes but also try to also suppress the replay message in both the main and test version.

**Draco:** The SLF4J replay warning is hardcoded to `System.err` — no logback config can suppress it. The `-Dslf4j.provider` JVM option is the only fix, and it needs to apply to `run` as well.

*(Changes `Test / fork` and `Test / javaOptions` to unscoped `fork` and `javaOptions`, creates `src/main/resources/logback.xml` with same config as logback-test.xml)*

> **Dev:** Given the the afore-mentioned message is hard-wired, will it at least only show up once per execution?

**Draco:** Yes, it only shows up once — during SLF4J's one-time initialization. But with `fork := true` and `-Dslf4j.provider=...`, you shouldn't see it at all.

> **Dev:** Won't I still see it when running tests in the src/test/scala directory?

**Draco:** No — `fork := true` and `javaOptions` without a task scope apply to the entire project, including test runs. If you're running tests from your IDE though, that's a different story — the IDE launches its own JVM and won't pick up sbt's `javaOptions`. You'd need to add the property to your IntelliJ run configuration.

> **Dev:** Ok, I should reload the project with sbt and see if it still appears.

**Draco:** Sounds good. If it still appears, it's likely coming from the IDE's own JVM rather than sbt's forked one.

---

## NaturalActor ActorRef Investigation

> **Dev:** Investigate how I am misunderstanding how to create an ActorRef and sending an Int value to it so that it prints the number received. Look at NaturalActor.scala and NaturalActorTest.scala in the context of using Pekko actors, and determine why the actor referenced by the ActorRef isn't receiving and printing the number.

**Draco:** *(Reads NaturalActor.scala, NaturalActorTest.scala, Actor.scala, ActorInstance.scala, ActorType.scala, Natural.scala)*

Two issues found in `NaturalActorTest.scala`:

### 1. `Actor.apply` creates a no-op actor, ignoring `NaturalActor`'s `receive`

`Actor.apply[T]` creates an anonymous `Actor[T]` whose `receive` just returns `Behaviors.same` — it does nothing. Your custom `receive` that prints `msg.value` lives inside `NaturalActor.actorInstance`, but that object is never used as the behavior. You're passing `actorInstance.actorDefinition` (just the definition metadata) into `Actor.apply`, which builds a brand new no-op actor from it.

### 2. `systemActorOf` creates a second, independent actor

`system.systemActorOf(actorBehavior, ...)` spawns a new actor under the system guardian, separate from the `ActorSystem` root actor. The `ActorSystem` itself is the root actor, but you're sending the message to this second child actor.

### Corrected test

```scala
test("Actor[Natural]") {
  val system = ActorSystem[Natural](NaturalActor.actorInstance, "naturalActor")
  println("Sending number 10 to naturalActor:")
  system ! Natural(10)
  Thread.sleep(100)
  system.terminate()
}
```

Key changes:
- Pass `NaturalActor.actorInstance` (the actual behavior with your custom `receive`) directly to `ActorSystem`
- Send the message to `system` — the `ActorSystem[T]` itself is an `ActorRef[T]`
- No need for `systemActorOf`

### The Type Mismatch

> **Dev:**
> ```
> type mismatch;
>  found   : draco.ActorType
>  required: org.apache.pekko.actor.typed.Behavior[domains.natural.Natural]
>     val system = ActorSystem[Natural](NaturalActor.actorInstance, "naturalActor")
> ```

**Draco:** `ActorInstance` declares `actorInstance` as `ActorType`, but `ActorSystem` needs `Behavior[Natural]`. The runtime object is an `Actor[Natural]` (which is a `Behavior[Natural]`), but the declared type is too narrow. Cast it:

```scala
val system = ActorSystem[Natural](
  NaturalActor.actorInstance.asInstanceOf[Actor[Natural]],
  "naturalActor"
)
```

---

## Triadic Symmetry — The Parameterization Decision

> **Dev:** If I give ActorInstance a type parameter, should I not also add one to DomainInstance and RuleInstance?

**Draco:** *(Reads ActorInstance.scala, DomainInstance.scala, RuleInstance.scala)*

No — the reason `ActorInstance` needs a type parameter is specific to actors. `Actor[T]` extends `ExtensibleBehavior[T]`, so the message type `T` flows through Pekko's typed API. `DomainInstance` and `RuleInstance` don't have the same problem — there's no external typed API forcing the parameter to be visible at the trait level.

> **Dev:** So I could have the Generator add `.asInstanceOf[...]` as necessary in generated code, right?

**Draco:** Yes, that works. The Generator knows the concrete type from the `TypeDefinition`, so it can emit the cast in the right place.

> **Dev:** It bothers me that adding a type parameter to ActorInstance alone, breaks the triadic symmetry between Domain, Rule and Actor related declarations.

**Draco:** That's a valid concern. A few options that preserve the triad:

1. **Parameterize all three** — uniform and future-proofs them
2. **Parameterize none** — use `asInstanceOf` in generated code where Pekko demands it
3. **Cast at point of use** — the cast is a Pekko integration detail, not a Draco structural concern

Option 2/3 feels most consistent with Draco's philosophy — the type parameter on `Actor[T]` is inherited from Pekko's `ExtensibleBehavior[T]`, not from Draco's own type model.

> **Dev:** I'm at #3 now so I'll stay with that until an obvious reason to change appears.

**Draco:** Good call. The cast is a one-line Pekko adapter detail — not worth restructuring the core type model over.

---

## Aspect-Based Naming Convention

> **Dev:** I'm currently using the fixed names of "rule" and "actor" as subpackages of the domain package name, to segregate the definition files...
>
> *(Corrected prompt:)* I want to back off of using subpackage names to segregate the definition data files. Instead, I'm considering just adding the suffix to the simple type name based on its relevance to the triad. So the possible name suffixes would be `<type name>` (with no suffix), `<type name>Domain`, `<type name>Rule`, `<type name>Actor`, `<type name>DomainRule`, `<type name>ActorRule`, `<type name>DomainRuleActor`. Then use concatenated dot file extension names like `<type name>.json`, `<type name>.domain.json`, and `<type name>.domain.rule.actor.json` where the order of the composed suffix was part of the semantics.

**Draco:** The suffix approach is cleaner than subpackages for avoiding name collisions. A few considerations:

- **Ordering as semantics is risky** — if `DomainRule` means something different from `RuleDomain`, that's a convention that lives only in developers' heads
- **Combinatorial growth** — three aspects give 7 non-empty combinations; do all 7 actually occur?
- **Dot extensions are nice** — `Natural.domain.rule.json` reads well and sorts naturally

What concrete case prompted this?

> **Dev:** I think the order meaning can be enforced by the Generator, since any type that is in a domain, and since an actor references its own domain and a target domain, then the generator should be able to appropriately generate the proper suffix and order.

**Draco:** That makes sense — if the Generator is the single source of truth for naming, it can deterministically derive the suffix and ordering from the definition metadata. The developer never has to remember the convention.

### Dyadic Ordering Semantics

> **Dev:** To the extent that the ontic nature of the three definition types supports their mutual semantic "orthogonality", I think we can assign unambiguous semantics to any permutation of the three by deriving the meaning from the dyad formed from the first two of the three. For instance DomainRuleActor means "DomainRule Actor" not "Domain RuleActor". Since RuleActor can exist on its own, you could also have "RuleActor Domain" but not "Rule ActorDomain". All 2-combinations have no duplicates, and any 3-combination can have non-adjacent duplicates.

**Draco:** That's a clean formalism. The left-to-right dyadic parsing gives a consistent rule: the first two elements form the primary compound concept, and the third is the outer role. And constraints like "not `ActorDomain`" fall out naturally from the ontology.

The invalid permutations never appear because the Generator won't produce them — they'd require relationships that can't exist in the definition metadata. So the naming convention is self-enforcing.

> **Dev:** In the previous prompt I wrote "to duplicates" instead of "no duplicates". Was your response based on auto-correcting that typo?

**Draco:** Yes — I read "to" as "no" based on context.

> **Dev:** I'm just checking your checking of my prompts. You got my meaning.

---

## JSON as Single Source of Truth

> **Dev:** Let's look at how we can use both embedded JSON strings and files containing the same JSON in a way that can be helpful in improving both the manually created source code and how the Generator can generate it. The goal is to make it easier for Dreams to create new or modify existing Domain, Rule and Actor types.

**Draco:** *(Launches Explore agent to survey all JSON usage patterns across the codebase — finds embedded JSON (Pattern B) and file-based loading (Pattern A) coexisting)*

The unified approach for Dreams:

1. **JSON file is always the single source of truth** — every definition lives in a `.json` file using the aspect naming convention
2. **Generated Scala always loads from file, not embedded JSON** — via `SourceContent` referencing the resource path
3. **The resource path is derivable from TypeName** — combined with the aspect suffix
4. **Dreams edits only JSON files** — invokes the Generator to regenerate Scala
5. **Embedded JSON becomes optional** — for self-contained distribution

> **Dev:** Do you have a recommendation for which to start with, based on your estimation of the path of least resistance?

**Draco:** File naming convention first. The Generator needs to know the naming rules before it can emit correct resource paths. Once that's locked in, the Generator changes are mechanical.

> **Dev:** I'll take your recommendation.

---

## TypeName Refactoring — Adding Aspects

> **Dev:** If I am correctly reading "TypeName makes the JSON self-describing" as your recommendation, then I'll go with that.

**Draco:** Yes, that's the recommendation. An `aspects` field in `TypeName` keeps the naming convention visible in the JSON itself.

> **Dev:** Yes, please. *(To: "Want me to implement the TypeName changes now?")*

**Draco:** *(Greps for all references to old fields — domainNamePath, domainResourcePath, actorNamePath, actorResourcePath, ruleNamePath, ruleResourcePath, domainName, actorName, ruleName — across the codebase. Reads Generator.scala, TypeDefinition.scala, TestTypeModule.scala, DomainDefinition.scala)*

Changes made:

### TypeName.scala — Complete Rewrite

Removed:
- `domainName`, `actorName`, `ruleName` fields
- Six derived path fields (`domainNamePath`, `domainResourcePath`, `actorNamePath`, `actorResourcePath`, `ruleNamePath`, `ruleResourcePath`)

Added:
- `aspects: Seq[String]` — canonical subset of `("domain", "rule", "actor")`, always stored in Domain > Rule > Actor order
- `qualifiedName` — `"Natural"` + aspects suffix -> `"NaturalDomainRule"`
- `namePath` — fully qualified: `"domains.natural.NaturalDomainRule"`
- `resourcePath` — file path: `"/domains/natural/Natural.domain.rule.json"`
- `canonicalOrder` enforces ordering regardless of input order

```scala
private val validAspects: Seq[String] = Seq("domain", "rule", "actor")

private def canonicalOrder: Seq[String] => Seq[String] = aspects =>
  validAspects.filter(aspects.contains)

private def aspectSuffix: Seq[String] => String = aspects =>
  canonicalOrder(aspects).map(_.capitalize).mkString

private def aspectExtension: Seq[String] => String = aspects => {
  val ordered = canonicalOrder(aspects)
  if (ordered.isEmpty) "" else "." + ordered.mkString(".")
}
```

### Generator.scala — Two References

- `typeName.domainNamePath` -> `typeName.namePath` (Evrete condition function references and rule naming)

### TypeDefinition.scala — One Reference

- `typeName.domainResourcePath` -> `typeName.resourcePath` (JSON file loading)

### TestTypeModule.scala — Reworked

All `TypeName` calls now use named `_namePackage` and `_aspects` parameters instead of the old positional `_domainName` second argument. `domainNamePath` -> `namePath` in Factory valueType and print statements.

### Backward Compatibility

Embedded rule files still have `domainName`/`actorName`/`ruleName` in their JSON strings — Circe ignores extra fields. No JSON resource files used the old fields.

---

## TestTypeModule — User Modification

Dev modified `TestTypeModule.scala` further: changed `DomainDefinition` -> `TypeDefinition` throughout, reflecting the TypeDefinition unification (completed later in Chapter 14). Domain definitions like `dracoDomainDefinition` became `TypeDefinition` instances using `_elementTypeNames` directly, with the `DomainDefinition` import removed.

---

## Documentation Updates

> **Dev:** Time to update the project README.md based on all the work since we started this session. Then update CLAUDE.md so we can start a new session and continue forward with that.

**Draco:** *(Updates all three files)*

**README.md:**
- Added actor system (Actor/ActorType/ActorInstance/ActorDefinition triad)
- Added Instance Triad table with `asInstanceOf` adapter pattern
- Added TypeName aspects section with full suffix/extension table
- Added JSON-as-single-source-of-truth design
- Added Dreams section
- Added Logback to dependencies
- Updated project structure with logback files and test domain directories
- Updated Work in Progress (Dreams, JSON migration, TypeInstance imports)
- Removed references to deleted files (ActorBehavior, RuleActorBehavior, Service, ServiceDomain)

**CLAUDE.md:**
- Added ActorInstance to type hierarchy
- Added Instance Triad table
- Added TypeName aspects documentation
- Added Actor usage patterns with `asInstanceOf`
- Added JSON source-of-truth section
- Added Logging Configuration section
- Updated key files table

**MEMORY.md:**
- Added Actor System Design section
- Added TypeName Aspect Naming Convention section
- Added JSON single-source-of-truth design direction
- Added Logging Configuration details
- Updated imminent tasks list
- Cleared stale session-specific entries

---

## Memory System Discussion

> **Dev:** I don't recall where MEMORY.md came from or how it is being used and updated.

**Draco:** MEMORY.md is part of Claude Code's auto-memory system. It's a persistent memory directory at `~/.claude/projects/.../memory/` whose contents survive across conversations. When I start a new session, MEMORY.md is automatically loaded into my context, so I can pick up where we left off. The difference from CLAUDE.md: CLAUDE.md is checked into the repo. MEMORY.md is local to your machine — my working notes, not project documentation.

> **Dev:** Is MEMORY.md something that could be usefully shared and/or distributed across multiple installation instances of Claude Code and Claude App under my current Anthropic user accounts?

**Draco:** Currently, no. There's no built-in sync mechanism. Since it's just a plain file on disk, you could share it manually (symlink, Dropbox, iCloud), but the path includes the project directory path, so the same project on a different machine would need the same absolute path. The more practical cross-installation bridge is CLAUDE.md — since it's checked into the repo, any Claude Code instance that clones the repo gets it automatically.

---

## Session Wrap

> **Dev:** I think it's time for me to start a new session. What happens to this session data when I don't continue with it?

**Draco:** This session's conversation history is stored locally on your machine. You can resume it later via `claude --continue` or `claude --resume`. The key things that carry forward to a new session regardless: CLAUDE.md (from repo), MEMORY.md (from local memory), and the code itself.

---

## Session Summary

Major accomplishments this session:

1. **SLF4J/Pekko logging noise suppressed** — Removed redundant `slf4j-jdk14` dependency, added `logback-test.xml` and `logback.xml`, configured `fork := true` with explicit SLF4J provider property to eliminate replay warnings
2. **NaturalActor ActorRef diagnosis** — Identified two bugs: `Actor.apply` creates no-op behavior (ignoring custom `receive`), and `systemActorOf` creates a redundant child actor. Fix: pass `actorInstance` directly to `ActorSystem`, send to `system` itself
3. **Triadic symmetry preserved** — Decision: keep `DomainInstance`, `RuleInstance`, `ActorInstance` all unparameterized, use `asInstanceOf` cast at Pekko integration boundary rather than breaking structural symmetry
4. **Aspect-based naming convention designed** — Replaces subpackage segregation with suffix/extension naming: `Natural.domain.rule.json` / `NaturalDomainRule`. Dyadic ordering semantics. Generator enforces canonical ordering and valid permutations.
5. **TypeName refactored** — Removed 9 old fields (domainName/actorName/ruleName and 6 derived paths), added `aspects`, `qualifiedName`, `namePath`, `resourcePath`. Updated Generator, TypeDefinition, TestTypeModule.
6. **JSON as single source of truth** — Design direction established: JSON files canonical, generated Scala loads from file, resource paths derived from TypeName, Dreams edits JSON only
7. **Dreams acknowledged** — Built-in service/application needs `logback.xml` for runtime; will drive JSON-first workflow
8. **Documentation updated** — README.md, CLAUDE.md, MEMORY.md all reflect current state
