//> using scala 2.13

// who-extends — find types whose dracoAspect.derivation chain transitively reaches a target.
//
// Inverse of `derivation-chain` (which walks the ancestors of a single type): given a
// target, find every type — across the canonical scan set — whose chain reaches it.
// Cycle-protected like derivation-chain.
//
// Scan set: Draco, Base, Primes. Sub-domains in src/mods are not scanned by
// default. Rules and actors typically have no dracoAspect.derivation, so they don't appear.
//
// Usage:   bin/draco-sc who-extends <name> [namePackage...]
// Example: bin/draco-sc who-extends DracoType draco
//          bin/draco-sc who-extends Primal draco

package scripts

import draco._
import scala.collection.mutable

object WhoExtends {
  private val scanDomains: Seq[(String, Seq[String])] = Seq(
    ("Draco",    Seq("draco")),
    ("Base",     Seq("draco", "base")),
    ("Primes",   Seq("draco", "primes"))
  )

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      System.err.println("usage: who-extends <name> [namePackage...]")
      System.err.println("example: who-extends DracoType draco")
      sys.exit(2)
    }

    val targetName = args(0)
    val targetPkg  = args.drop(1).toSeq
    val targetTn   = TypeName(targetName, _namePackage = targetPkg)
    val targetKey  = targetTn.namePath

    def loaded(td: TypeDefinition): Boolean =
      !DracoAspect.isEmpty(td.dracoAspect) ||
      !DomainAspect.isEmpty(td.domainAspect) ||
      !RuleAspect.isEmpty(td.ruleAspect) ||
      !ActorAspect.isEmpty(td.actorAspect)

    // True iff `tn`, or any transitive ancestor via dracoAspect.derivation, has
    // namePath == target. Cycle-protected via the per-call seen set.
    def reaches(tn: TypeName, target: String, seen: mutable.Set[String]): Boolean = {
      val key = tn.namePath
      if (seen.contains(key)) return false
      seen += key
      if (key == target) return true
      val td = Generator.loadType(tn)
      if (!loaded(td)) return false
      td.dracoAspect.derivation.exists(parent => reaches(parent, target, seen))
    }

    case class Match(elementPath: String, derivationHead: String)
    val matches = mutable.ArrayBuffer.empty[Match]
    var scanned = 0
    var domainsScanned = 0

    scanDomains.foreach { case (domName, domPkg) =>
      val domTd = Generator.loadType(TypeName(domName, _namePackage = domPkg))
      val isDomain =
        loaded(domTd) &&
        domTd.domainAspect.typeName.name.nonEmpty &&
        domTd.domainAspect.typeName.namePath == domTd.typeName.namePath
      if (isDomain) {
        domainsScanned += 1
        domTd.domainAspect.elementTypeNames.foreach { elementName =>
          scanned += 1
          val elementTn = TypeName(elementName, _namePackage = domPkg)
          if (elementTn.namePath != targetKey && reaches(elementTn, targetKey, mutable.Set.empty[String])) {
            val elementTd = Generator.loadType(elementTn)
            val head =
              if (!loaded(elementTd) || elementTd.dracoAspect.derivation.isEmpty) "(no direct derivation)"
              else "extends " + elementTd.dracoAspect.derivation.map(_.name).mkString(" with ")
            matches += Match(elementTn.namePath, head)
          }
        }
      }
    }

    println(s"Types extending ${targetTn.namePath} (transitive):")
    println()

    if (matches.isEmpty) {
      println(s"  (no matches across $scanned element(s) in $domainsScanned domain(s))")
      sys.exit(1)
    } else {
      val pathWidth = matches.map(_.elementPath.length).max
      matches.foreach { m =>
        val padded = m.elementPath.padTo(pathWidth, ' ')
        println(f"  $padded  ${m.derivationHead}")
      }
      println()
      println(s"${matches.size} match(es) across $scanned element(s) in $domainsScanned domain(s).")
    }
  }
}
