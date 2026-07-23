package draco

import java.net.URI

trait ContentSink extends DracoType {
  def write(content: String): Unit
}

object ContentSink extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("ContentSink", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[ContentSink] = Type[ContentSink] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _sinkRoot: URI,
    _logicalPath: String
  ) : ContentSink = new ContentSink {
    val sinkPath: java.nio.file.Path = java.nio.file.Paths.get(_sinkRoot.resolve(_logicalPath))
    override def write(content: String): Unit = {
      java.nio.file.Files.createDirectories(sinkPath.getParent)
      java.nio.file.Files.write(sinkPath, content.getBytes(java.nio.charset.StandardCharsets.UTF_8))
    }
    override lazy val typeDefinition: TypeDefinition = ContentSink.typeDefinition
  }

  lazy val Null: ContentSink = apply(
    _sinkRoot = null.asInstanceOf[URI],
    _logicalPath = ""
  )

}
