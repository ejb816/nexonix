
package generated.draco

import draco._
import java.net.URI
import scala.io.BufferedSource

trait SourceContent  {
  val source: BufferedSource
  val sourceLines: Seq[String]
  val sourceString: String
}

object SourceContent extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("SourceContent", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[SourceContent] = Type[SourceContent] (typeDefinition)

  def apply (
    _sourceRoot: URI,
    _logicalPath: String
  ) : SourceContent = new SourceContent {
    override val sourceRoot: URI = _sourceRoot
    override val logicalPath: String = _logicalPath
    override lazy val typeInstance: DracoType = SourceContent.typeInstance
    override lazy val typeDefinition: TypeDefinition = SourceContent.typeDefinition
  }

  lazy val Null: SourceContent = apply(
    _sourceRoot = null.asInstanceOf[URI],
    _logicalPath = ""
  )


}
