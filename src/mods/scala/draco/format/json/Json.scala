package draco.format.json

import draco._
import draco.format._

trait Json extends Format[io.circe.Json]

object Json extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Json", _namePackage = Seq ("draco", "format", "json")))
  lazy val dracoType: Type[Json] = Type[Json] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[Json] = Domain[Json] (typeDefinition)
}
