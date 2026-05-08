package domains.natural

import draco._
import io.circe._
trait Natural extends Extensible with Primal[Int]

object Natural extends App {
  lazy val sourceContent: String = SourceContent (
    _sourceRoot = Test.roots.sourceRoot,
    _logicalPath = "domains/natural/Natural.json"
  ).sourceString
  lazy val typeDefinition: TypeDefinition = parser.parse(sourceContent).flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)
  lazy val dracoType: DracoType = Type[Natural] (typeDefinition)
  def apply (_int: Int) : Natural = new Natural {
    override val value: Int = _int
  }
}