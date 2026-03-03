# Changelog

All notable changes to the Nexonix/Draco project will be documented in this file.

## [Unreleased]

### Added

- **DomainInstance generation** — New `generate(td: TypeDefinition, dn: DomainName)` overload in Generator produces DomainInstance companion objects matching the hand-written Primes pattern. Includes `domainInstanceLiteral` and `domainGlobal` helpers.

- **nullValueFor helper** — Generator now produces type-appropriate null-equivalent values ("" for String, Seq.empty for Seq, 0 for Int, false for Boolean, etc.) instead of `null.asInstanceOf[T]` when generating Null instances.

- **Rule import generation** — `ruleImports(namePackage)` auto-generates package hierarchy imports (e.g., `import draco._`, `import draco.primes._`), always includes `import draco._`, plus framework imports for Circe, Evrete, and `java.util.function.Consumer`.

- **README.md** — Comprehensive project documentation covering architecture, type system, domains, rules, actors, code generation, semantic preservation, and current feature status.

### Changed

- **factoryBody** — Now takes `Factory` instead of `Seq[Parameter]`. When `factory.body` is non-empty, uses body elements directly as overrides. When empty, falls back to parameter-derived behavior.

- **nullInstance** — Now takes `Factory` as additional parameter. When a factory exists, generates Null via `apply()` with null-equivalent args. Falls back to null-cast only when no factory is present.

- **Generated rule vals** — `ruleDefinition`, `action`, and `pattern` in generated rule companions are now `private lazy val` instead of `private val` (required for DelayedInit compatibility).

- **Generated rule types** — `Consumer[RhsContext]` and `Consumer[Knowledge]` now use short names (imported) instead of fully qualified types in generated rule source.

- **Generated rule ruleDefinition** — Now uses embedded JSON parsed via `parser.parse(...)` instead of `RuleDefinition.Null` placeholder.

### Fixed

- **RuleDefinition encoder/decoder** — Changed from `implicit val` to `implicit lazy val` to fix DelayedInit null when accessed from tests.

- **Value encoder/decoder** — Same fix: `implicit val` to `implicit lazy val`.

- **RuleDefinition decoder** — Fixed `_pattern` field reading from wrong JSON field (`"action"` instead of `"pattern"`), which caused decode failure returning Left.

- **PrimesRulesTest rule registration** — Tests now call `ruleInstance.pattern.accept(knowledge)` instead of just accessing `ruleInstance.pattern`, which never registered the rules with the knowledge base.

- **Generated rule files** — All three primes rules (PrimesFromNaturalSequence, AddNaturalSequence, RemoveCompositeNumbers) updated with `private lazy val` for ruleDefinition/action/pattern.

## [Previous]

### Added

- **Factory type** - New `Factory` subtype of `BodyElement` combining `parameters` and `body` fields for type construction. Used in `TypeDefinition` instead of separate parameters/body fields.

- **Monadic type** - New `Monadic` subtype of `BodyElement` for side-effecting statements that don't require names. Always has `valueType = "Unit"`. Simplifies rule action bodies by eliminating artificial variable names for effectful operations.

- **globalElements field** - Added `globalElements: Seq[BodyElement]` to `TypeDefinition` for declaring members in companion objects.

- **JSON field elision** - Encoders/decoders for all draco types now elide empty fields from JSON output and handle missing fields gracefully on decode:
  - `TypeElement` - elides empty `name`, `valueType`, `value`, `parameters`, `body`
  - `TypeDefinition` - elides empty `modules`, `derivation`, `elements`, `factory`, `globalElements`, `rules`
  - `RuleDefinition` - elides empty `variables`, `conditions`, `values`, `action`
  - `TypeName` - elides empty `namePackage`, `parent`
  - `Value` - elides empty `name`, `pathElements`
  - `DomainName` - elides empty `elementTypeNames`

### Changed

- **TypeDefinition field renames**:
  - `moduleElements` → `modules`
  - `derivesFrom` → `derivation`

- **Dynamic type** - Now uses `parameters` and `body` fields instead of `value` for method definitions with parameters.

- **Rule trait** - Split `rule: Knowledge => Unit` into two separate concerns:
  - `pattern: Knowledge => Unit` - for matching/binding
  - `action: RhsContext => Unit` - for execution

- **Generator improvements**:
  - Generates separate `action` and `pattern` vals instead of single `rule`
  - Condition functions declared before they are referenced in `.where()` clauses
  - Uses fully qualified class names in generated code (no import statements)
  - Uses `java.util.function.Consumer[org.evrete.api.RhsContext]` for action type to match Evrete's Java API
  - Condition functions in `.where()` use fully qualified class reference for Evrete's runtime compilation

- **RuleDefinition encoder** - Fixed field name inconsistency: encoder now uses `"typeName"` instead of `"name"` to match decoder.

### Fixed

- **Rule.apply** - Now correctly returns a `Rule` instance instead of `Unit`.

- **Evrete integration** - Condition functions now work with Evrete's runtime Java compilation by using fully qualified class names in `.where()` expressions.

### Removed

- **@static annotation** - Removed from generated condition functions. Scala 2.13 companion object methods provide static forwarders that are accessible from Java.

- **Import statements in generated rules** - All type references in generated rule code now use fully qualified names.

## Notes

This changelog summarizes changes made with Claude Code assistance. The project uses:
- Scala 2.13
- Evrete rules engine
- Apache Pekko actors
- Circe for JSON serialization
