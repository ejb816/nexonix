package draco.format.xml

import draco.format._
import draco._

trait XML extends Format[XML]

object XML extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("XML", _namePackage = Seq ("draco", "format", "xml")))
  lazy val dracoType: Type[XML] = Type[XML] (typeDefinition)

  lazy val elementTypeNames: Seq[String] = Seq ()

  lazy val domainType: Domain[XML] = Domain[XML] (typeDefinition)
}
