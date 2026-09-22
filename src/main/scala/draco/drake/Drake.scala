package draco.drake

import draco._

trait Drake extends Source

object Drake extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Drake", _namePackage = Seq ("draco", "drake")))
  lazy val dracoType: Type[Drake] = Type[Drake] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ("Presence", "Present", "Absent", "Text")

  lazy val domainType: Domain[Drake] = Domain[Drake] (typeDefinition)
}
