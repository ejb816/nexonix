# Changelog

All notable changes to the Nexonix/Draco project will be documented in this file.

## [Unreleased]

### Added

- **Factory type** - New `Factory` subtype of `BodyElement` combining `parameters` and `body` fields for type construction. Used in `TypeDefinition` instead of separate parameters/body fields.

- **Monadic type** - New `Monadic` subtype of `BodyElement` for side-effecting statements that don't require names. Always has `valueType = "Unit"`. Simplifies rule action bodies by eliminating artificial variable names for effectful operations.

- **typeGlobals field** - Added `typeGlobals: Seq[BodyElement]` to `TypeDefinition` for declaring members in companion objects.

- **JSON field elision** - Encoders/decoders for all draco types now elide empty fields from JSON output and handle missing fields gracefully on decode:
  - `TypeElement` - elides empty `name`, `valueType`, `value`, `parameters`, `body`
  - `TypeDefinition` - elides empty `modules`, `derivation`, `elements`, `factory`, `typeGlobals`, `rules`
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
