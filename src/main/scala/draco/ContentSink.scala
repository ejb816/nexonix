package draco

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}


trait ContentSink extends DracoType {
  def write(content: String): Unit
}

object ContentSink extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("ContentSink"))
  lazy val dracoType: Type[ContentSink] = Type[ContentSink] (typeDefinition)

  def apply(
             _sinkRoot: URI,
             _logicalPath: String
           ): ContentSink = new ContentSink {
    override val typeDefinition: TypeDefinition = ContentSink.typeDefinition
    override def write(content: String): Unit = {
      Files.createDirectories(sinkPath.getParent)
      Files.write(sinkPath, content.getBytes(StandardCharsets.UTF_8))
    }
    val sinkPath: Path = Paths.get(_sinkRoot.resolve(_logicalPath))
  }
}