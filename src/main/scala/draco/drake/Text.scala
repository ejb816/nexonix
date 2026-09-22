package draco.drake

import draco._

trait Text extends DracoType

object Text extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Text", _namePackage = Seq ("draco", "drake")))
  lazy val dracoType: Type[Text] = Type[Text] (typeDefinition)
  lazy val domainType: Domain[Drake] = Domain[Drake] (typeDefinition)
}
