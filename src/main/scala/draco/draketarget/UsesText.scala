package draco.draketarget

import draco._

trait UsesText extends Surface

object UsesText extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("UsesText", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[UsesText] = Type[UsesText] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  def apply (
    _identities: => Seq[String] = Seq.empty
  ) : UsesText = new UsesText {
    override lazy val value: String = (if (_identities.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", (Seq("uses [") ++ _identities.map(entry => "  " ++ entry) ++ Seq("]")).mkString("\n"))
    override lazy val typeDefinition: TypeDefinition = UsesText.typeDefinition
  }

  lazy val Null: UsesText = apply()


}
