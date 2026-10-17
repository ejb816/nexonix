package draco.format

import draco._

trait Value[F] extends DracoType {
  val name: String
  val pathElements: Seq[String]
  def value(_source: => F): draco.drake.Presence[F]
}

object Value extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Value", _namePackage = Seq ("draco", "format")))
  lazy val dracoType: Type[Value[_]] = Type[Value[_]] (typeDefinition)
  lazy val domainType: Domain[Format[_]] = Domain[Format[_]] (typeDefinition)
}
