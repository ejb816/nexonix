# Draco Dev Journal — Chapter 6

**Session date:** March 23, 2026 (continued)
**Topic:** Codec Generation Implementation, TupleFactRule Fix, DomainDefinition Consistency Discussion, DomainDefinition Upgrade Plan

---

## Session Start — Implementing the Codec Generation Plan

> **Dev:** Implement the following plan:
> # Codec Generation for Draco Generator
> *(The full codec generation plan — all three codec patterns, nine new helper methods, five modified methods, phased implementation order — later documented in Chapter 7)*

**Draco:** *(Reads Generator.scala in full. Launches two parallel agents: one to catalog existing hand-written codec patterns across TypeName.scala, TypeElement.scala, TypeDefinition.scala, Codec.scala, Value.scala, DomainName.scala; another to read the TypeDefinitionTest.scala test assertions for verification targets)*

Implementation proceeds in five tracked tasks, following the plan's implementation order:

### Task 1: Foundation Helpers

Three private methods added to Generator.scala after `nullValueFor`:

- **`elisionCheck(p, instanceVar)`** — returns `Some(check)` for parameters with defaults, `None` for required fields. String → `.nonEmpty`, Seq/Map → `.nonEmpty`, Factory → `.valueType.nonEmpty`, Action → `.body.nonEmpty`, Pattern → `.variables.nonEmpty`.
- **`findDiscriminatedParent(td, familyMap)`** — walks derivation chain upward to find the **root** discriminated union ancestor (e.g., TypeElement, not BodyElement). Returns `Some(name)` of the topmost ancestor with modules.
- **`collectLeafModules(td, familyMap)`** — recursively collects concrete subtypes, skipping intermediate sealed traits (BodyElement is intermediate; Fixed, Mutable, etc. are leaves).

### Task 2: Pattern 1 — Simple Field-Based Codecs

Three methods for types like TypeName, TypeDefinition, Value:

- **`fieldElisionEncoder(params, instanceVar)`** — generates `Encoder.instance { x => val fields = Seq(...).flatten; Json.obj(fields: _*) }`
- **`fieldElisionDecoder(params, typeName)`** — generates `Decoder.instance { cursor => for { ... } yield TypeName(...) }`
- **`simpleCodecDeclaration(td)`** — combines encoder + decoder as `implicit lazy val`

### Task 3: Pattern 3 — Codec.sub Wiring

- **`subtypeCodecDeclaration(td, parentName)`** — generates `private lazy val codec = Codec.sub[Parent, Child](Parent.encoder, Parent.decoder)` with `implicit def encoder` / `implicit def decoder`

### Task 4: Pattern 2 — Discriminated Union

The most complex pattern. Two methods:

- **`discriminatedCodecDeclaration(td, familyMap)`** — Full encoder with `kind` match arms over all leaf subtypes, plus fallback. Full decoder dispatching on `cursor.downField("kind").as[String]` to per-subtype for-comprehensions.
- **`encoderFieldLines(instanceVar, parentTd, leaves, familyMap)`** — Builds the shared encoder field set restricted to **parent-accessible fields only** (elements + factory params). This is critical: subtype-specific fields like `Pattern.variables` exist on leaf types but not on the parent trait, so the encoder can only access fields declared on the parent.

### Task 5: Dispatcher and Wiring

- **`codecDeclaration(td, familyContext)`** — Top-level dispatcher. Key ordering decision: checks `findDiscriminatedParent` **first** (so intermediate sealed traits like BodyElement get Pattern 3, not Pattern 2), then checks `td.modules.nonEmpty` for Pattern 2, then factory check for Pattern 1.
- **`hasCodec(td, familyContext)`** — Boolean convenience for import generation.
- **`circeImports`** — `import io.circe.{Decoder, Encoder, Json}` + `import io.circe.syntax.EncoderOps`

Modified methods:
- **`typeGlobal(td)`** → `typeGlobal(td, familyContext: Seq[TypeDefinition] = Seq.empty)` — codec block inserted after typeInstance, before factory/Null/Default
- **`domainGlobal(td, dn)`** → `domainGlobal(td, dn, familyContext: Seq[TypeDefinition] = Seq.empty)` — same insertion
- **`generate(td)`** — adds circe imports when codec present
- **`generate(td, dn)`** — adds circe imports when codec present
- **`generate(typeDefinitions)`** — passes `typeDefinitions` as familyContext to each `typeGlobal` call, adds circe imports when any type has codec

### Key Design Discovery During Implementation

The initial `codecDeclaration` checked `td.modules.nonEmpty` first (Pattern 2), then looked for a discriminated parent (Pattern 3). This was wrong for **BodyElement** — it has modules (it's a sealed trait) AND derives from TypeElement (which also has modules). The hand-written code gives BodyElement `Codec.sub[TypeElement, BodyElement]`, not its own discriminated union. Fixed by checking `findDiscriminatedParent` first: intermediate sealed traits that derive from a higher discriminated parent get Pattern 3.

Similarly, `findDiscriminatedParent` initially returned the **immediate** parent with modules. For Fixed (derives from BodyElement, which derives from TypeElement), this returned "BodyElement" — but the hand-written code uses `Codec.sub[TypeElement, Fixed]`. Fixed by walking all the way to the **root** discriminated ancestor.

---

## TupleFactRule Compilation Fix

> **Dev:** *(Pastes full test compilation output showing an error in TupleFactReteTest.scala)*
> ```
> lazy value pattern in object TupleFactRule cannot be accessed as a member of
> object org.nexonix.rules.rete.rules.TupleFactRule from class TupleFactReteTest
> ```

**Draco:** Pre-existing bug, not caused by the codec changes. `TupleFactRule.pattern` is `private lazy val` but the test calls it directly at line 29. Same pattern that was fixed previously in PrimesRulesTest.

```scala
// Before:
TupleFactRule.pattern (knowledge)
// After:
TupleFactRule.ruleInstance.pattern.accept(knowledge)
```

> **Dev:** I did not expect that the problem was caused by any of your changes. Pasting the entire error text for running all the tests was the easiest way for me to get your assistance. Thank you!

---

## Task Recall

> **Dev:** What is your recall of what tasks are left to do?

**Draco:** Based on the memory file, the remaining Generator tasks are:

5. **Rules subdomain auto-generation** — `rules` is a reserved subpackage name; `rules.Rules` extends `DomainInstance with <ParentDomain>` and is auto-generated as a container for all rule types in that subpackage.
6. **TypeInstance/DomainInstance imports** — Extend the `ruleImports` pattern to `generate(td)` and `generate(td, dn)` methods.

Just completed: ~~4. Codec generation~~ — the three patterns we just implemented.

---

## The Definition Type Consistency Discussion

> **Dev:** There is a glitch in the consistency of the "definition types." TypeDefinition is the root definition type, and DracoType is the root type for derivation. The three immediate "subtypes" of DracoType are DomainType, RuleType, and ActorType. Even though it is not yet well defined in Draco, it should be straightforward to define the other type definition associated types. The inconsistency is that there should be a DomainDefinition, that has and is referenced by TypeName in the same way that RuleDefinition has and is referenced by TypeName, which also implies ActorDefinition. Let's discuss.

**Draco:** *(Launches three parallel Explore agents: one to read all definition-related types, one to read all domain-related types and companions, one to read the RuleDefinition/RuleType/RuleInstance/Rule chain as a pattern)*

The current structure has a clear asymmetry:

**Runtime type hierarchy** (DracoType):
```
DracoType          → typeDefinition: TypeDefinition
├── DomainType     → domainName: DomainName
├── RuleType       → ruleDefinition: RuleDefinition
└── ActorType      → (not yet defined)
```

**Definition types** (what describes each):
```
TypeDefinition     → typeName: TypeName     ✓ root definition
RuleDefinition     → typeName: TypeName     ✓ parallel to RuleType
DomainName         → typeName: TypeName     ✗ not a "Definition"
???                                         ✗ no ActorDefinition
```

The inconsistency: `RuleType` carries a `RuleDefinition` (rich — variables, conditions, pattern, action), but `DomainType` carries only `DomainName` (just typeName + elementTypeNames). A consistent design would be:

```
TypeDefinition      — structure of any type
├── DomainDefinition — structure of a domain
├── RuleDefinition   — structure of a rule
└── ActorDefinition  — structure of an actor
```

Questions posed for discussion:
1. Should DomainDefinition replace DomainName, or wrap it?
2. What additional fields beyond elementTypeNames?
3. For ActorDefinition — minimal viable shape?
4. Subtypes of TypeDefinition, or peers sharing TypeName?

---

## DomainDefinition Design Emerges

> **Dev:** DomainDefinition obviates the need for DomainName, as you discerned from my prompt. DomainName was a move toward consistency without getting there. For additional fields in DomainDefinition, start with elementTypeNames, which are simple names whose package is the same as the domain type. Add subdomains, which are referenced by TypeName since subdomains are any domain that directly derives from a domain type. Beyond those consider source and sink, given that they represent the domain and codomain of morphism pointing from left to right, respectively. If source equals sink, then it's an endomorphism. Endomorphisms are Updates and Morphisms are Transforms. We will not use the term morphism in the definition keywords.

**Draco:** The DomainDefinition shape:

```
DomainDefinition
  typeName: TypeName              — identity
  elementTypeNames: Seq[String]   — simple names (package = domain's package)
  subdomains: Seq[TypeName]       — domains that directly derive from this domain
  source: TypeName                — left side (domain of the mapping)
  sink: TypeName                  — right side (codomain of the mapping)
```

Semantic interpretation of source/sink:

| source | sink | meaning |
|--------|------|---------|
| empty | empty | Plain domain (Draco, Base, Primes) |
| X | X | Update (endomorphism) |
| X | Y | Transform (X ≠ Y) |

The distinction between Update and Transform is entirely structural, not a separate type hierarchy.

---

## Subdomains: Top-Down or Bottom-Up?

> **Dev:** Propose to me whether the domain determines its subdomains or the subdomains determine the superdomain, within domain definition. I don't think it can be both.

**Draco:** **Bottom-up: the subdomain declares its superdomain.**

The reasoning follows from consistency with TypeDefinition's own patterns:

- `modules` (top-down) is for **sealed** hierarchies — the parent must exhaustively enumerate its children because the set is closed.
- `derivation` (bottom-up) is for **open** extension — the child declares what it extends. Adding a child doesn't touch the parent.

Domains are open, not sealed. You can add a `rules` subdomain to Primes without Primes needing to know about it. The `rules.Rules` auto-generation task then becomes: generate a DomainDefinition whose `superdomain` points to the parent domain's TypeName.

So the revised field is `superdomain: TypeName` instead of `subdomains: Seq[TypeName]`:

```
DomainDefinition
  typeName: TypeName
  elementTypeNames: Seq[String]
  superdomain: TypeName            — empty for root domains
  source: TypeName
  sink: TypeName
```

> **Dev:** Ready for plan to upgrade to DomainDefinition

---

## Planning the DomainDefinition Upgrade

**Draco:** *(Enters plan mode. Launches three parallel agents:)*

1. *Explore agent — cataloging every DomainName reference across the codebase (18 files found)*
2. *Explore agent — reading the RuleDefinition/RuleType/RuleInstance/Rule chain as the pattern to follow*
3. *Explore agent — reading all domain companions (Draco, Base, Primes, all transform domains) and TransformDomain trait*

Also reads TypeDictionary.scala (takes DomainName as factory param), ServiceDomain.scala (references `_domain.domainName.typeName`), TestTypeModule.scala (7 DomainName instances), and empty DomainNameTest.scala.

*(Launches Plan agent with full context from all three Explore agents)*

### The Plan (7 Phases)

**Phase 1: Create DomainDefinition.scala** — `sealed trait` with 5 fields (typeName required, rest defaulted). Companion follows RuleDefinition pattern: `extends App with TypeInstance`, `apply()` with defaults (`Seq.empty`, `TypeName.Null`), `Null`, encoder with elision, decoder with `Option[T].getOrElse`. JSON backward-compatible with existing DomainName JSON.

**Phase 2: Update core framework types (4 files)** — DomainType (`domainName` → `domainDefinition`), Domain (`_domainName` → `_domainDefinition`), TypeDictionary (factory param + apply + Null), ServiceDomain (`.domainName.typeName` → `.domainDefinition.typeName`).

**Phase 3: Update Generator.scala** — Add `TypeName` case to `elisionCheck`. Rename `domainInstanceLiteral`, `domainGlobal`, `generate(td, dn)` param types. Generate `DomainDefinition(...)` with optional superdomain/source/sink fields.

**Phase 4: Update domain companions (3 files)** — Draco, Base, Primes: `DomainName(...)` → `DomainDefinition(...)`, `_domainName` → `_domainDefinition`. Draco's elementTypeNames: `"DomainName"` → `"DomainDefinition"`.

**Phase 5: Update test files (7 files)** — All five transform domains, TestTypeModule (7 variable renames + import), DomainNameTest → DomainDefinitionTest.

**Phase 6: Delete DomainName.scala**

**Phase 7: Update documentation** — CLAUDE.md, README.md, CHANGELOG.md.

Files NOT modified: DomainInstance.scala (references DomainType, not DomainName), DomainDictionary.scala (works with DomainType/TypeDictionary).

All code changes must be atomic — project won't compile in intermediate states.

---

## Session Summary

Major accomplishments this session:

1. **Codec generation implemented** — All three patterns (simple field-based, discriminated union, Codec.sub wiring) added to Generator.scala. Nine new helper methods, five modified methods. Key discovery: pattern detection must check for discriminated parent before modules (intermediate sealed traits like BodyElement get Codec.sub, not their own union). Root-walking for `findDiscriminatedParent` ensures Fixed → TypeElement (not BodyElement).
2. **TupleFactRule fix** — Pre-existing `private lazy val pattern` access bug, same as PrimesRulesTest fix.
3. **Definition type consistency discussion** — Identified the DomainName/RuleDefinition asymmetry. Designed DomainDefinition with 5 fields: typeName, elementTypeNames, superdomain (bottom-up), source, sink. Update vs Transform is structural (source == sink or not).
4. **DomainDefinition upgrade plan complete** — 7-phase plan covering 18 files, ready to execute.
