//> using scala 2.13

// list-domain — list the elements of a domain by loading each element's TypeDefinition.
//
// Differs from `bin/draco-gen verify` (which checks JSON↔disk sync) by *loading* and
// *summarizing* each member — derivation for types, variable/action counts for rules,
// message/signal-action counts for actors. Use to get a one-screen mental model of a
// domain's surface without opening individual files.
//
// Usage:   bin/draco-sc list-domain <name> [namePackage...]
// Example: bin/draco-sc list-domain Primes draco primes
//          bin/draco-sc list-domain Draco draco

package scripts

import draco._

object ListDomain {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      System.err.println("usage: list-domain <name> [namePackage...]")
      System.err.println("example: list-domain Primes draco primes")
      sys.exit(2)
    }

    val name = args(0)
    val pkg  = args.drop(1).toSeq

    // Generator.loadType returns a placeholder (input typeName, all aspects empty)
    // when the resource is missing — not TypeDefinition.Null. Detect via aspect emptiness.
    def loaded(td: TypeDefinition): Boolean =
      !DracoAspect.isEmpty(td.dracoAspect) ||
      !DomainAspect.isEmpty(td.domainAspect) ||
      !RuleAspect.isEmpty(td.ruleAspect) ||
      !ActorAspect.isEmpty(td.actorAspect)

    val td = Generator.loadType(TypeName(name, _namePackage = pkg))
    if (!loaded(td)) {
      System.err.println(s"error: no TypeDefinition found for ${pkg.mkString(".")}.$name")
      sys.exit(1)
    }

    val dom = td.domainAspect
    val isSelfDomain = dom.typeName.name.nonEmpty && dom.typeName.namePath == td.typeName.namePath
    if (!isSelfDomain) {
      val living = if (dom.typeName.name.isEmpty) "(none declared)" else dom.typeName.namePath
      System.err.println(s"error: ${td.typeName.namePath} is not a domain (domainAspect.typeName != typeName)")
      System.err.println(s"       its domain is: $living")
      sys.exit(2)
    }

    println(s"Domain: ${td.typeName.namePath}  (${dom.elementTypeNames.size} element(s))")
    println()

    if (dom.elementTypeNames.isEmpty) {
      println("  (no elementTypeNames declared)")
      return
    }

    case class Row(kind: String, name: String, detail: String, missing: Boolean)

    val rows: Seq[Row] = dom.elementTypeNames.map { elementName =>
      val kind =
        if (elementName.endsWith(".rule"))  "RULE"
        else if (elementName.endsWith(".actor")) "ACTOR"
        else                                 "TYPE"
      val elementTd = Generator.loadType(TypeName(elementName, _namePackage = pkg))
      if (!loaded(elementTd)) {
        Row(kind, elementName, "[MISSING — no resource found]", missing = true)
      } else {
        val detail = kind match {
          case "RULE" =>
            val vars   = elementTd.ruleAspect.pattern.variables.size
            val steps  = elementTd.ruleAspect.action.body.size
            s"$vars var(s), $steps-step action"
          case "ACTOR" =>
            val msg = elementTd.actorAspect.message.body.size
            val sig = elementTd.actorAspect.signal.body.size
            s"$msg message-step(s), $sig signal-step(s)"
          case _ =>
            if (elementTd.dracoAspect.derivation.isEmpty) "(no derivation)"
            else "extends " + elementTd.dracoAspect.derivation.map(_.name).mkString(" with ")
        }
        Row(kind, elementName, detail, missing = false)
      }
    }

    val nameColWidth = rows.map(_.name.length).max
    rows.foreach { r =>
      val paddedName = r.name.padTo(nameColWidth, ' ')
      println(f"  ${r.kind}%-5s  $paddedName  ${r.detail}")
    }

    val byKind = rows.groupBy(_.kind).view.mapValues(_.size).toMap
    val summary = Seq("TYPE", "RULE", "ACTOR")
      .flatMap(k => byKind.get(k).map(n => s"$n ${k.toLowerCase}${if (n == 1) "" else "s"}"))
      .mkString(", ")
    println()
    println(s"Summary: $summary")

    val missing = rows.count(_.missing)
    if (missing > 0) {
      println(s"Drift: $missing element(s) listed but unreachable. Run: bin/draco-gen verify <path-to-${name}.json>")
      sys.exit(1)
    }
  }
}
