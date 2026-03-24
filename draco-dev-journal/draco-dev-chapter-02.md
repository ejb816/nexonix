# Draco Dev Journal — Chapter 2

**Session date:** March 23, 2026 (continued)
**Topic:** Generator Renames, Rules Removal, Factory Semantics, Transform Domain Cleanup, Multi-Type Generation Planning

---

## Implementing the Pre-Requisite Plan

> **Dev:** Implement the following plan:
> # Plan: Rename typeGlobals/companionObject and Fix Factory Body Generation

**Draco:** *(Reads all 10 files targeted by the plan in parallel: TypeDefinition.scala, Generator.scala, TypeDefinitionTest.scala, DataModel.scala, CHANGELOG.md, and the five JSON resource files)*

### Change 1: Rename `typeGlobals` → `globalElements`

Applied `replace_all` across all files:

- **TypeDefinition.scala** — trait field, self-describing elements, factory parameter, apply param/override, encoder, decoder (8 occurrences)
- **Generator.scala** — 3 accessor references, generated literal string, method/param rename (`typeGlobalsDeclaration` → `globalElementsDeclaration`)
- **TypeDefinitionTest.scala** — 7 occurrences in test data
- **DataModel.scala** — factory parameter
- **5 JSON resources** — Test2.json, Spherical.json, TypeDefinition.json, TypeName.json, Draco.json
- **CHANGELOG.md** — 2 documentation references

### Change 2: Rename Generator Methods

- `companionObject` → `typeGlobal` (declaration + call site)
- `typeGlobalsDeclaration` → `globalElementsDeclaration` (covered by Change 1's `replace_all`)
- `hasGlobals` → `hasGlobalElements` (declaration + 2 conditionals)

### Change 3: Fix Factory Body Generation

Replaced `factoryBody` method — changed from `Seq[BodyElement]` parameter (which was always `Seq.empty` from `factory.body`) to `Seq[Parameter]`:

```scala
private def factoryBody (
  parameters: Seq[Parameter]
) : String = {
  val overrides = parameters.map { p =>
    s"    override val ${p.name}: ${p.valueType} = _${p.name}"
  }
  val tdOverride = "    override val typeDefinition: TypeDefinition = typeInstance.typeDefinition"
  val allOverrides = overrides :+ tdOverride
  s"{\n${allOverrides.mkString("\n")}\n  }"
}
```

Updated call site from `factoryBody(factory.body)` to `factoryBody(factory.parameters)`.

---

## Removing the `rules` Field from TypeDefinition

> **Dev:** Looks like I forgot to make sure the rules field in TypeDefinition was deleted

> **Dev:** *(Confirms removal)* Yes

**Draco:** Removed `rules: Seq[TypeName]` from TypeDefinition everywhere:

- **TypeDefinition.scala** — trait field, self-describing elements, factory parameter, apply (param + override), encoder, decoder yield
- **Generator.scala** — `typeDefinitionLiteral` rules block, `ruleSetDeclaration` method + call site in `typeGlobal`
- **TypeDefinitionTest.scala** — 4 occurrences (elements, factory params, factory body, `_rules` param)
- **DataModel.scala** — `_rules = ???`
- **5 JSON resources** — removed `"rules": []` lines

Left untouched: BoundingBoxFromSpherical.json, Unit.json, Primal.json (older schema format, not parsed by current TypeDefinition decoder).

---

## Factory Method Survey

> **Dev:** Are there sources that can be generated from TypeDefinition, that currently have manually created source code, that either have apply methods that are not useful, or not have apply methods that would be useful?

**Draco:** *(Launches Explore agent to survey all 31 companions with `lazy val typeDefinition` in `src/main/scala/draco/`)*

### Findings

**Has factory but apply is a no-op:**
- **Generator** — factory declares `Parameter("typeDictionary", ...)` but `apply` just returns `new Generator {}` — the parameter is ignored

**Has factory, apply is exact match (already correct):**
- **TypeDefinition**, **RuleDefinition**, **Value**, **DomainName**

**Has factory, apply has genuinely custom logic (can't be auto-generated):**
- **TypeName** — computes `nameElements`, `namePackage` from input
- **SourceContent**, **ContentSink** — file I/O in apply
- **Main**, **Test** — URI resolution logic
- **TypeDictionary** — reflection-based type loading
- **DomainDictionary** — builds a map
- **Service**, **RuleActorBehavior**, **ActorBehavior** — generic `[T]` types with custom wiring

**Has elements but no factory (no apply generated):**
- **DomainType**, **RuleType** — hand-written apply with defaults, but no factory in TypeDefinition

---

## Generator Instantiation Design Discussion

> **Dev:** The main reason for considering instance values for the Generator type, is the use case where the generate, build, deploy, configure and initialize process can be parallelized by spawning multiple actors. It is to early to try that, but do you think it worth preserving the option to instantiate Generators?

**Draco:** Yes. The factory already declares the parameter — keeping it costs nothing and preserves the path to actor-based parallelization. The current `new Generator {}` apply is a placeholder, which is fine at this stage. When the time comes, each actor would get its own `Generator` instance scoped to a `TypeDictionary` slice.

No changes needed now.

---

## Factory.Null Semantics

> **Dev:** I have no issue with having both custom and automated factory methods, as long as the definitions can easily override whatever can be automated. I do think that Factory.Null should indicate that no factory method should be generated.

**Draco:** That makes sense. The semantics:

- **`Factory.Null`** — no factory, no `apply` generated
- **Factory with parameters** — Generator produces the standard `apply` with `override val` body
- **Custom logic** — hand-written code in the companion overrides or supplements the generated `apply`

This is already how it works — `typeGlobal` only generates the apply branch when `factory.valueType.nonEmpty`.

> **Dev:** update the memory file with the rules removal and Factory.Null semantics

**Draco:** *(Updates MEMORY.md: rules field removal, Factory.Null semantics, custom/automated factory coexistence, Generator rename summary)*

---

## Context Window Discussion

> **Dev:** If I keep doing claude -c, which I interpret as restarting the same Claude Code "session", is there limit to the data accretion for that session that will exceed the mitigation of compression and clearing context?

**Draco:** Yes, eventually. Each compression pass is lossy — detail from earlier exchanges gets increasingly summarized. After many rounds of `claude -c`, you end up with deeply compressed history where early context is reduced to thin summaries, and the compression itself consumes space.

The memory file is the durable store designed for this. Starting a fresh session (`claude` without `-c`) that reads MEMORY.md gives a clean context window with essential knowledge at full fidelity. Use `claude -c` for continuing a train of thought across a few sessions; start fresh when the topic shifts.

---

## Clean Compile

> **Dev:** Compile has no errors based on just invoking AddNaturalSequence, as I have been doing when we were getting compile errors.

**Draco:** Good — clean compile confirms all the changes are consistent.

---

## Transform Domain Cleanup

> **Dev:** Before TypeDefinitionTest can run, we need to fix the example DataModel domain with four subdomains (Alpha, Bravo, Charlie, Delta) I fixed Alpha domain and its associated compile issues. Could you look at Alpha and update DataModel, Bravo, Charlie and Delta with corrections consistent with Alpha?

**Draco:** *(Reads all five files: Alpha, DataModel, Bravo, Charlie, Delta)*

Alpha's pattern (the fixed version):
- `import draco._` + `import draco.transform._`
- Clean trait: `trait Alpha extends dataModel.DataModel`
- Flat companion with `typeDefinition`, `typeInstance`, `domainInstance` directly
- Uses `Domain` from `draco._` (not `org.nexonix.domains`)

The other four had stale imports, wrapper patterns (`lazy val bravo: Bravo = new Bravo { ... }`), knowledge overrides, and DataModel had incorrect typeName ("Alpha" instead of "DataModel") plus `???` placeholders.

Rewrote all four to match Alpha's pattern:

```scala
package draco.transform.bravo

import draco._
import draco.transform._

trait Bravo extends dataModel.DataModel

object Bravo {
  val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "Bravo",
      _namePackage = Seq ("draco", "transform", "bravo")
    )
  )
  lazy val typeInstance: Type[Bravo] = Type[Bravo] (typeDefinition)
  lazy val domainInstance: Domain[Bravo] = Domain[Bravo] (
    _domainName = DomainName (
      _typeName = typeDefinition.typeName,
      _elementTypeNames = Seq ()
    )
  )
}
```

Charlie and Delta follow the identical structure. DataModel simplified to the same flat companion pattern without subdomain wiring or knowledge overrides.

> **Dev:** It works now.

---

## Planning Multi-Type File Generation

> **Dev:** SO are we ready to update Generator so we can generate the source code for TypeElement.scala from its type definition with the reference to the module type names?

**Draco:** *(Enters plan mode. Launches two Explore agents in parallel: one to read TypeElement.scala thoroughly (478 lines, 12 types, sealed hierarchy), another to trace how `modules` is used across the codebase.)*

### Key Findings

**TypeElement.scala structure:**
1. `sealed trait TypeElement extends Primal[String]` — root with 4 elements (name, valueType, parameters, body)
2. `sealed trait BodyElement extends TypeElement` — intermediate with 10 modules
3. `object BodyElement` — companion with typeDefinition, typeInstance, codec
4. `object TypeElement` — companion with typeDefinition, typeInstance, apply, Null, discriminated encoder/decoder
5-14. Ten leaf subtypes (Fixed through Factory) — each with sealed trait + companion (typeDefinition, typeInstance, apply, Null, codec)

**Current module handling:**
- `typeModifier(modules)` adds `sealed` keyword — the only current use
- `typeDefinitionLiteral` serializes modules into the literal but doesn't traverse them
- No mechanism to generate multiple types in one file from module references

**Custom parts that can't be auto-generated (for now):**
- TypeElement's discriminated union encoder/decoder
- BodyElement's and subtypes' codec delegations
- Subtypes with hardcoded inherited field values (Monadic: `name=""`, `valueType="Unit"`; Action: `name="ctx"`; etc.)

### The Plan

Three additions to Generator.scala:

1. **Extract `traitDeclaration` helper** from `generate(td)` — just the trait line
2. **Add `moduleOrder`** — depth-first walk from roots, parents before children, siblings in `modules` sequence order
3. **Add `generate(typeDefinitions: Seq[TypeDefinition])` overload** — topologically sorts, emits one package declaration + all trait/companion pairs

Two tests in TypeDefinitionTest.scala:
- TypeElement hierarchy (12 TypeDefinitions from companion objects)
- Simple parent-child synthetic example (Animal/Dog, reversed input order)

> **Draco:** Should I revert `typeGlobal` back to `companionObject` for clarity as an internal helper?

> **Dev:** Keep typeGlobal

*(Plan written, session continuing)*
