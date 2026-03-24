# Draco Dev Journal — Chapter 7

**Session date:** March 23, 2026 (continued)
**Topic:** Codec Generation Planning — Designing Auto-Generated Circe Codecs for the Generator

---

## Session Start — What's Next?

> **Dev:** Can you suggest the next task we can work on, based on recent activity in this project?

**Draco:** Based on the memory file and recent activity, here are the next tasks in priority order from the Imminent Generator Tasks list:

1. **Codec generation** (next up) — Generate Circe encoders/decoders automatically: simple field-based codecs, discriminated union dispatch, sub-type codec wiring (`Codec.sub`), richer Factory metadata for custom body overrides.
2. **Rules subdomain auto-generation** — `rules` as a reserved subpackage name; auto-generate `rules.Rules` extending `DomainInstance`.
3. **TypeInstance/DomainInstance imports** — Extend the `ruleImports` pattern to the other `generate` methods.

Codec generation is the natural next step — it builds directly on the Generator work already completed. It would close a significant gap: right now codecs are hand-written, but since TypeDefinition already describes the full structure of each type, the Generator has enough information to emit them automatically.

---

## Scoping the Work

> **Dev:** What do you need from me, if anything, to start Codec generation?

**Draco:** *(Reads Codec.scala, Generator.scala, TypeElement.scala, TypeDefinition.scala, TypeName.scala, RuleDefinition.scala, Value.scala, DomainName.scala, Accumulator.scala — thorough review of all existing hand-written codec patterns and the Generator's current companion object generation structure)*

*(Launches two Explore agents in parallel:)*
1. *Codec patterns agent — cataloging all three codec patterns across the codebase, mapping TypeDefinition fields to codec fields, documenting elision conventions*
2. *Generator structure agent — understanding typeGlobal/domainGlobal/ruleGlobal methods, what they currently emit, where codec generation would fit, checking base/primes domain types for existing codecs*

After thorough analysis, two design decisions needed:

**1. Where to emit the codec code?** The current hand-written codecs live inline in each companion object. Should generated codecs follow the same pattern (emit encoder/decoder as part of the `typeGlobal` / companion generation), or should they go to a separate file?

**2. Scope for the first pass** — which types should get generated codecs?
- **Option A**: Start with simple field-based codecs only (types whose TypeDefinition has a factory with parameters — like TypeName, TypeDefinition pattern). The straightforward case.
- **Option B**: Also include the discriminated union pattern (sealed traits with `modules` — like TypeElement). More complex because the Generator needs to know the `"kind"` mapping and per-subtype field differences.
- **Option C**: All three patterns including `Codec.sub` wiring.

Everything else — the field elision convention, the `Option[X].map(_.getOrElse(default))` decoder pattern, the `lazy implicit val` requirement — derivable from the existing examples without further guidance.

---

## Pause for Commit

> **Dev:** I'm going pause this session here, and exit, so I can commit and push the current changes, before we make any more.

**Draco:** Sounds good. When you're ready to pick this back up, we can start from those two design decisions — where to emit the codec code, and which patterns to tackle first.

---

## Resuming — Design Decisions

> **Dev:** generated codecs go into the generated type global and I prefer Option C for the scope of the first pass.

**Draco:** Both decisions clear:
1. Codecs emitted inline in the generated companion object (typeGlobal/domainGlobal)
2. All three patterns in the first pass — simple field-based, discriminated union, and Codec.sub wiring

*(Enters plan mode. Launches three agents:)*
1. *Explore agent — deep dive on all codec patterns: field elision, discriminated union dispatch, Codec.sub usage, how codec fields map to TypeDefinition, field elision conventions*
2. *Explore agent — Generator structure: typeGlobal method branches, domainGlobal, rule generation, base/primes domain codecs, generated code patterns*
3. *Plan agent — full implementation design for all three codec patterns*

---

## The Plan

### Three Codec Patterns

**Pattern 1 — Simple field-based** (like TypeName, TypeDefinition, Value, DomainName):
- Encoder: `Seq[Option[(String, Json)]]` with field elision → `.flatten` → `Json.obj(fields: _*)`
- Decoder: For-comprehension reading fields via `cursor.downField("x").as[Option[T]].map(_.getOrElse(default))`
- Fields come from **factory parameters** (not elements — elements may include derived/computed fields like `TypeName.fullName`)

**Pattern 2 — Discriminated union** (like TypeElement — sealed traits with `modules`):
- Encoder: Per-subtype match arms, each encoding that subtype's factory parameters with a `"kind"` discriminator
- Decoder: Dispatch on `cursor.downField("kind").as[String]`, each case reading subtype-specific factory parameters
- Intermediate sealed traits (like BodyElement) recursed through — only leaf subtypes in match arms

**Pattern 3 — Sub-type wiring** (like Parameter, Factory, etc.):
- `private lazy val codec = Codec.sub[Parent, Child](Parent.encoder, Parent.decoder)`
- `implicit def encoder` / `implicit def decoder` (not `lazy val` — delegation)

### Pattern Detection

| Condition | Pattern |
|-----------|---------|
| `td.modules.nonEmpty` | Pattern 2 (discriminated union) |
| `td.derivation` references a type in familyContext with modules | Pattern 3 (Codec.sub) |
| `td.factory.valueType.nonEmpty && td.factory.parameters.nonEmpty` | Pattern 1 (simple field-based) |
| None of the above | No codec (abstract type) |

### Elision Rules

Driven by factory parameters:
- **Parameter has a non-empty default** (`p.value.nonEmpty`) → elide on encode, `Option[T].map(_.getOrElse(default))` on decode
- **Parameter has no default** (`p.value.isEmpty`) → always encode, `.as[T]` on decode

Elision checks by value type (for parameters with defaults):

| Type | Check | Default |
|------|-------|---------|
| `String` | `.nonEmpty` | `""` |
| `Seq[...]` | `.nonEmpty` | `Seq.empty` |
| `Map[...]` | `.nonEmpty` | `Map.empty` |
| `Factory` | `.valueType.nonEmpty` | `Factory.Null` |
| `Action` | `.body.nonEmpty` | `Action.Null` |
| `Pattern` | `.variables.nonEmpty` | `Pattern.Null` |
| Other | always encode | — |

### Placement in Generated Output

After typeInstance, before factory/Null/Default:

```
object Name extends App with TypeInstance {
  lazy val typeDefinition = ...
  lazy val typeInstance = ...

  // CODEC HERE

  def apply(...) = ...
  lazy val Null = ...
  lazy val Default = ...
  // global elements
}
```

### New Helper Methods in Generator.scala

Nine new private methods:
1. `elisionCheck(p, instanceVar)` — emptiness check expression or None
2. `fieldElisionEncoder(params, instanceVar)` — Pattern 1 encoder body
3. `fieldElisionDecoder(params, typeName)` — Pattern 1 decoder body
4. `simpleCodecDeclaration(td)` — Pattern 1 complete
5. `subtypeCodecDeclaration(td, parentName)` — Pattern 3 complete
6. `collectLeafModules(td, familyMap)` — recursive concrete subtype collection
7. `discriminatedCodecDeclaration(td, familyMap)` — Pattern 2 complete
8. `findDiscriminatedParent(td, familyMap)` — walks derivation for a parent with modules
9. `codecDeclaration(td, familyContext)` — top-level dispatcher

### Modifications to Existing Methods

1. `typeGlobal(td)` → `typeGlobal(td, familyContext = Seq.empty)` — insert codec block
2. `domainGlobal(td, dn)` → add `familyContext` — insert codec block
3. `generate(td)` — add Circe imports when codec present
4. `generate(td, dn)` — add Circe imports when codec present
5. `generate(Seq[TypeDefinition])` — pass typeDefinitions as familyContext, add imports

### Implementation Order

1. Foundation helpers: `elisionCheck`, `findDiscriminatedParent`, `collectLeafModules`
2. Pattern 1: `fieldElisionEncoder`, `fieldElisionDecoder`, `simpleCodecDeclaration`
3. Pattern 3: `subtypeCodecDeclaration`
4. Pattern 2: `discriminatedCodecDeclaration`
5. Dispatcher: `codecDeclaration`
6. Modify `typeGlobal` and `domainGlobal`
7. Modify all four `generate` overloads

### Verification

1. Existing tests pass unchanged
2. TypeDefinitionTest "TypeDefinition generates Scala code" — now includes encoder/decoder
3. TypeDefinitionTest "Multi-type generation: TypeElement hierarchy" — Pattern 2 on TypeElement, Pattern 3 on subtypes
4. TypeDefinitionTest "Multi-type generation: Animal/Dog" — Pattern 2 on Animal, Pattern 3 on Dog
5. Visual inspection against hand-written codecs

### Key Design Discovery — Pattern 2 Encoder Approach

The existing hand-written TypeElement encoder uses a **shared field set** (the parent's elements: `name`, `valueType`, `value`, `parameters`, `body`) for all subtypes. This means subtype-specific fields like `Pattern.variables` and `Action.values` are **not encoded** — the decoder defaults them to `Seq.empty`.

The generated Pattern 2 encoder uses a **per-subtype approach** instead — each match arm encodes that subtype's own factory parameters. This is more verbose but more correct for round-tripping. Generated code can afford the verbosity.

### Key Design Discovery — Elision vs. Factory Defaults

Examining the hand-written codecs revealed an inconsistency: `Value.name` and `DomainName.elementTypeNames` have empty Parameter defaults (meaning "required"), yet their hand-written codecs treat them as optional with elision. The generator uses the deterministic rule: empty default = required, non-empty default = elide. This is slightly stricter than some hand-written codecs but consistent and predictable.

---

## Session Summary

This session was entirely planning — no code changes. The output is a comprehensive implementation plan for auto-generating Circe codecs in the Generator, covering all three patterns (simple field-based, discriminated union, Codec.sub wiring), with clear detection logic, elision rules, placement conventions, and a phased implementation order. The plan is ready to execute in the next session.
