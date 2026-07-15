package draco.format

import draco._
import io.circe.Decoder

trait Value[F] extends DracoType {
  val name: String
  val pathElements: Seq[String]
  def value[T: Decoder](_source: F): T
}

object Value extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Value", _namePackage = Seq ("draco", "format")))
  lazy val dracoType: Type[Value[_]] = Type[Value[_]] (typeDefinition)
  lazy val domainType: Domain[Format[_]] = Domain[Format[_]] (typeDefinition)
}
