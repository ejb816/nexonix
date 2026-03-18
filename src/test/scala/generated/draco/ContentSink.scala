
package generated.draco

import draco._
import java.net.URI

trait ContentSink  {
  def write(content: String): Unit
}

object ContentSink extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = draco.TypeDefinition.load(TypeName ("ContentSink", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[ContentSink] = Type[ContentSink] (typeDefinition)

  def apply (
    _sinkRoot: URI,
    _logicalPath: String
  ) : ContentSink = new ContentSink {
    override val sinkRoot: URI = _sinkRoot
    override val logicalPath: String = _logicalPath
    override lazy val typeInstance: DracoType = ContentSink.typeInstance
    override lazy val typeDefinition: TypeDefinition = ContentSink.typeDefinition
  }

  lazy val Null: ContentSink = apply(
    _sinkRoot = null.asInstanceOf[URI],
    _logicalPath = ""
  )


}
