package draco.format

import draco._

trait Format[T] extends Primal[T]

object Format extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Format", _namePackage = Seq ("draco", "format")))
  lazy val dracoType: Type[Format[_]] = Type[Format[_]] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[Format[_]] = Domain[Format[_]] (typeDefinition)
}
