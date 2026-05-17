//> using scala 2.13

// inspect-type — load a TypeDefinition by TypeName and pretty-print its aspect-by-aspect shape.
//
// Differs from `bin/draco-gen inspect` (which takes a filesystem path and dumps raw JSON)
// by exercising the production loading API (Generator.loadType) and grouping output by aspect.
//
// Usage:   bin/draco-sc inspect-type <name> [namePackage...]
// Example: bin/draco-sc inspect-type Primal draco
//          bin/draco-sc inspect-type Meters draco base

package scripts

import draco._

object InspectType {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      System.err.println("usage: inspect-type <name> [namePackage...]")
      System.err.println("example: inspect-type Primal draco")
      sys.exit(2)
    }

    val name = args(0)
    val pkg  = args.drop(1).toSeq

    val td = Generator.loadType(TypeName(name, _namePackage = pkg))

    if (td == TypeDefinition.Null) {
      System.err.println(s"error: no TypeDefinition found for ${pkg.mkString(".")}.$name")
      System.err.println(s"       expected resource: ${if (pkg.isEmpty) "" else pkg.mkString("/", "/", "/")}$name.{yaml,json}")
      sys.exit(1)
    }

    println(s"=== TypeName ===")
    println(s"  name             = ${td.typeName.name}")
    println(s"  namePackage      = ${td.typeName.namePackage.mkString(".")}")
    println(s"  typeParameters   = ${if (td.typeName.typeParameters.isEmpty) "(none)" else td.typeName.typeParameters.mkString(", ")}")

    println()
    println(s"=== DracoAspect ===")
    println(s"  derivation       = ${if (td.dracoAspect.derivation.isEmpty) "(none)" else td.dracoAspect.derivation.map(t => s"${t.namePath}${if (t.typeParameters.isEmpty) "" else t.typeParameters.mkString("[", ", ", "]")}").mkString(", ")}")
    println(s"  elements         = ${td.dracoAspect.elements.size} element(s)")
    td.dracoAspect.elements.foreach { e =>
      val kind = e match {
        case _: Fixed     => "Fixed"
        case _: Mutable   => "Mutable"
        case _: Dynamic   => "Dynamic"
        case _: Parameter => "Parameter"
        case _: Monadic   => "Monadic"
        case _: Pattern   => "Pattern"
        case _: Action    => "Action"
        case _: Condition => "Condition"
        case _: Variable  => "Variable"
        case _: Factory   => "Factory"
        case _            => "?"
      }
      println(s"    - $kind ${e.name}: ${e.valueType}${if (e.value.isEmpty) "" else s" = ${e.value}"}")
    }
    val f = td.dracoAspect.factory
    if (f.valueType.nonEmpty) {
      println(s"  factory          = ${f.valueType} (${f.parameters.size} param(s), ${f.body.size} body element(s))")
    } else {
      println(s"  factory          = (none)")
    }
    println(s"  globalElements   = ${td.dracoAspect.globalElements.size} element(s)")

    println()
    println(s"=== DomainAspect ===")
    val dom = td.domainAspect
    val isSelfDomain = dom.typeName.name.nonEmpty && dom.typeName == td.typeName
    println(s"  typeName         = ${if (dom.typeName.name.isEmpty) "(none)" else dom.typeName.namePath}${if (isSelfDomain) "  [SELF — this type IS a domain]" else ""}")
    println(s"  elementTypeNames = ${if (dom.elementTypeNames.isEmpty) "(none)" else dom.elementTypeNames.mkString(", ")}")

    println()
    println(s"=== RuleAspect ===")
    println(s"  variables        = ${if (td.ruleAspect.variables.isEmpty) "(none)" else s"${td.ruleAspect.variables.size} var(s)"}")

    println()
    println(s"=== ActorAspect ===")
    val a = td.actorAspect
    val hasActor = a.messageAction.body.nonEmpty || a.signalAction.body.nonEmpty
    println(s"  messageAction    = ${if (a.messageAction.body.isEmpty) "(none)" else s"${a.messageAction.body.size} body element(s)"}")
    println(s"  signalAction     = ${if (a.signalAction.body.isEmpty) "(none)" else s"${a.signalAction.body.size} body element(s)"}")
    if (!hasActor) println(s"  (no actor behavior)")
  }
}
