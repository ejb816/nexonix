package draco.service

import draco._

trait TextOutput extends DracoType {
  val send: String => Boolean
}

object TextOutput extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TextOutput", _namePackage = Seq ("draco", "service")))
  lazy val dracoType: Type[TextOutput] = Type[TextOutput] (typeDefinition)
  lazy val domainType: Domain[Service] = Domain[Service] (typeDefinition)

  def apply (
    _send: => String => Boolean
  ) : TextOutput = new TextOutput {
    override lazy val send: String => Boolean = _send
    override lazy val typeDefinition: TypeDefinition = TextOutput.typeDefinition
  }

  lazy val Null: TextOutput = apply(
    _send = null.asInstanceOf[String => Boolean]
  )


}
