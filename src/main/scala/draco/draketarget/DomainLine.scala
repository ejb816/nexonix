package draco.draketarget

import draco._

trait DomainLine extends Surface

object DomainLine extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("DomainLine", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[DomainLine] = Type[DomainLine] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  def apply (
    _package: Seq[String],
    _name: String
  ) : DomainLine = new DomainLine {
    override lazy val value: String = "domain " ++ _package.mkString(" ") ++ " " ++ _name
    override lazy val typeDefinition: TypeDefinition = DomainLine.typeDefinition
  }

  lazy val Null: DomainLine = apply(
    _package = Seq.empty,
    _name = ""
  )


}
