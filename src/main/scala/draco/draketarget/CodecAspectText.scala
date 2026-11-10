package draco.draketarget

import draco._

trait CodecAspectText extends Surface

object CodecAspectText extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CodecAspectText", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[CodecAspectText] = Type[CodecAspectText] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  def apply (
    _fields: => Seq[String] = Seq.empty
  ) : CodecAspectText = new CodecAspectText {
    override lazy val value: String = (if (_fields.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse("", (Seq("codec", "  payload [" ++ _fields.mkString(", ") ++ "]")).mkString("\n"))
    override lazy val typeDefinition: TypeDefinition = CodecAspectText.typeDefinition
  }

  lazy val Null: CodecAspectText = apply()


}
