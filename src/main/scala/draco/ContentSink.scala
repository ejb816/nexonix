package draco

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}


trait ContentSink {
  def write(content: String): Unit
}

object ContentSink {
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