package draco

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}


trait ContentSink {
  def write(content: String): Unit
}

object ContentSink extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = TypeDefinition (
    _typeName = TypeName (
      _name = "ContentSink",
      _namePackage = Seq ("draco")
    ),
    _elements = Seq (
      Dynamic ("write", "Unit", Seq (Parameter ("content", "String", "")), Seq.empty)
    ),
    _factory = Factory (
      "ContentSink",
      _parameters = Seq (
        Parameter ("sinkRoot", "URI", ""),
        Parameter ("logicalPath", "String", "")
      )
    )
  )
  lazy val typeInstance: Type[ContentSink] = Type[ContentSink] (typeDefinition)

  def apply(
             _sinkRoot: URI,
             _logicalPath: String
           ): ContentSink = new ContentSink {

    val sinkPath: Path = Paths.get(_sinkRoot.resolve(_logicalPath))

    override def write(content: String): Unit = {
      Files.createDirectories(sinkPath.getParent)
      Files.write(sinkPath, content.getBytes(StandardCharsets.UTF_8))
    }
  }
}