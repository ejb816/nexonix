package draco

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import java.net.URI

trait ContentSink extends DracoType {
  def write(_content: => String): Unit
}

object ContentSink extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ContentSink", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[ContentSink] = Type[ContentSink] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _sinkRoot: => URI,
    _logicalPath: => String
  ) : ContentSink = new ContentSink {
    lazy val sinkPath: Path = Paths.get(_sinkRoot.resolve(_logicalPath))
    override def write(_content: => String): Unit = {
      lazy val content: String = _content
      Files.createDirectories(sinkPath.getParent)
      Files.write(sinkPath, content.getBytes(StandardCharsets.UTF_8))
    }
    override lazy val typeDefinition: TypeDefinition = ContentSink.typeDefinition
  }

  lazy val Null: ContentSink = apply(
    _sinkRoot = null.asInstanceOf[URI],
    _logicalPath = ""
  )


}
