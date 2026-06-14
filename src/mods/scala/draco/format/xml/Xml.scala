package draco.format.xml

import draco._
import draco.format._

trait Xml extends Format[String]

object Xml extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("Xml", _namePackage = Seq ("draco", "format", "xml")))
  lazy val dracoType: Type[Xml] = Type[Xml] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[Xml] = Domain[Xml] (typeDefinition)
}
