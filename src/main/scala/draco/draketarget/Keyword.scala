package draco.draketarget

import draco._

trait Keyword extends DracoType

object Keyword extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Keyword", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[Keyword] = Type[Keyword] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  lazy val kinds: Seq[(String, String)] = Seq(("Fixed", "fix"), ("Mutable", "mut"), ("Dynamic", "dyn"), ("Parameter", "par"), ("Local", "loc"), ("Monadic", "mon"), ("Variable", "var"), ("Condition", "con"), ("Case", "case"))
  lazy val table: Map[String, String] = kinds.toMap
  lazy val strict: String = "now"
}
