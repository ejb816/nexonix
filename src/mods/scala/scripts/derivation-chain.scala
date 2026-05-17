//> using scala 2.13

// derivation-chain — walk a type's `dracoAspect.derivation` transitively and print
// each ancestor in order. Useful for answering "does X extend DracoType?" or "what's
// the full inheritance picture for this type?" without re-deriving it from source.
//
// Cycle-protected (mirrors Generator.chainHits' walk).
//
// Usage:   bin/draco-sc derivation-chain <name> [namePackage...]
// Example: bin/draco-sc derivation-chain Meters draco base
//          bin/draco-sc derivation-chain Cardinal draco base

package scripts

import draco._
import scala.collection.mutable

object DerivationChain {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      System.err.println("usage: derivation-chain <name> [namePackage...]")
      System.err.println("example: derivation-chain Meters draco base")
      sys.exit(2)
    }

    val name = args(0)
    val pkg  = args.drop(1).toSeq

    def fmt(tn: TypeName): String = {
      val params = if (tn.typeParameters.isEmpty) "" else tn.typeParameters.mkString("[", ", ", "]")
      s"${tn.namePath}$params"
    }

    def walk(tn: TypeName, depth: Int, seen: mutable.Set[String]): Unit = {
      val key = tn.namePath
      if (seen.contains(key)) {
        println(s"${"  " * depth}↪ ${fmt(tn)}  [cycle]")
        return
      }
      seen += key

      val td = Generator.loadType(tn)
      val marker = if (depth == 0) "■" else "└─"
      val suffix =
        if (td == TypeDefinition.Null) "  [no TypeDefinition resource found]"
        else if (td.dracoAspect.derivation.isEmpty) "  [root: no further derivation]"
        else ""

      println(s"${"  " * depth}$marker ${fmt(tn)}$suffix")

      if (td != TypeDefinition.Null) {
        td.dracoAspect.derivation.foreach(child => walk(child, depth + 1, seen))
      }
    }

    val rootName = TypeName(name, _namePackage = pkg)
    println(s"Derivation chain for ${fmt(rootName)}:")
    println()
    walk(rootName, 0, mutable.Set.empty[String])
  }
}
