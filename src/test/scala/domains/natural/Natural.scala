package domains.natural

import draco._
import io.circe._
trait Natural extends TypeInstance with Primal[Int]

object Natural extends App with TypeInstance {
  lazy val sourceContent: String = SourceContent (
    _sourceRoot = Test.roots.sourceRoot,
    _logicalPath = "domains/natural/Natural.json"
  ).sourceString
  lazy val typeDefinition: TypeDefinition = parser.parse(sourceContent).flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)
  lazy val typeInstance: DracoType = Type[Natural] (typeDefinition)
  def apply (_int: Int) : Natural = new Natural {
    override val typeInstance: DracoType = Natural.typeInstance
    override val value: Int = _int
    override val typeDefinition: TypeDefinition = Natural.typeDefinition
  }
}