//> using scala 2.13

// list-domains — probe known top-level draco domains and summarize each.
//
// Discovery counterpart to `list-domain`. For each probed name, loads the TD,
// confirms it self-declares as a domain (domainAspect.typeName == typeName),
// and reports element count + composition (types / rules / actors).
//
// Default probe set: Draco, Base, Primes. Override by passing
// dotted fully-qualified names:
//
//   bin/draco-sc list-domains
//   bin/draco-sc list-domains draco.primes.Primes
//   bin/draco-sc list-domains draco.Draco draco.base.Base

package scripts

import draco._

object ListDomains {
  // Canonical first-party domains in src/main. Dreams / Orion live in src/mods.
  private val defaultDomains: Seq[(String, Seq[String])] = Seq(
    ("Draco",    Seq("draco")),
    ("Base",     Seq("draco", "base")),
    ("Primes",   Seq("draco", "primes"))
  )

  // Parse dotted FQN to (simpleName, packageComponents).
  private def parseFqn(fqn: String): (String, Seq[String]) = {
    val parts = fqn.split('.').filter(_.nonEmpty).toSeq
    if (parts.isEmpty) (fqn, Seq.empty)
    else (parts.last, parts.init)
  }

  def main(args: Array[String]): Unit = {
    val domains: Seq[(String, Seq[String])] =
      if (args.isEmpty) defaultDomains
      else args.toSeq.map(parseFqn)

    def loaded(td: TypeDefinition): Boolean =
      !DracoAspect.isEmpty(td.dracoAspect) ||
      !DomainAspect.isEmpty(td.domainAspect) ||
      !RuleAspect.isEmpty(td.ruleAspect) ||
      !ActorAspect.isEmpty(td.actorAspect)

    println("Known domains:")
    println()

    sealed trait Result
    case object NotLoadable extends Result
    case class NotDomain(actualDomain: String) extends Result
    case class Domain(total: Int, types: Int, rules: Int, actors: Int) extends Result

    val rows: Seq[(String, Result)] = domains.map { case (name, pkg) =>
      val path = (pkg :+ name).mkString(".")
      val td = Generator.loadType(TypeName(name, _namePackage = pkg))
      if (!loaded(td)) (path, NotLoadable)
      else if (!(td.domainAspect.typeName.name.nonEmpty && td.domainAspect.typeName.namePath == td.typeName.namePath)) {
        val actual = if (td.domainAspect.typeName.name.isEmpty) "(none)" else td.domainAspect.typeName.namePath
        (path, NotDomain(actual))
      } else {
        val items       = td.domainAspect.elementTypeNames
        val loadedItems = items.map(n => Generator.loadType(TypeName(n, _namePackage = pkg)))
        val rules   = loadedItems.count(t => !RuleAspect.isEmpty(t.ruleAspect))
        val actors  = loadedItems.count(t => !ActorAspect.isEmpty(t.actorAspect))
        val types   = items.size - rules - actors
        (path, Domain(items.size, types, rules, actors))
      }
    }

    val pathWidth = rows.map(_._1.length).max
    rows.foreach { case (path, result) =>
      val padded = path.padTo(pathWidth, ' ')
      result match {
        case NotLoadable =>
          println(f"  $padded  [NOT LOADABLE]")
        case NotDomain(actual) =>
          println(f"  $padded  [not a domain — lives in $actual]")
        case Domain(total, types, rules, actors) =>
          val parts = Seq(
            Option.when(types  > 0)(s"$types type${if (types  == 1) "" else "s"}"),
            Option.when(rules  > 0)(s"$rules rule${if (rules  == 1) "" else "s"}"),
            Option.when(actors > 0)(s"$actors actor${if (actors == 1) "" else "s"}")
          ).flatten.mkString(", ")
          val composition = if (parts.isEmpty) "(empty)" else parts
          println(f"  $padded  $total%2d element${if (total == 1) " " else "s"}   ($composition)")
      }
    }

    val anyError = rows.exists {
      case (_, NotLoadable)   => true
      case (_, NotDomain(_))  => true
      case _                  => false
    }
    if (anyError) sys.exit(1)
  }
}
