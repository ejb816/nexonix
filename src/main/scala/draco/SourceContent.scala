package draco

import java.net.{URI, URL}
import java.nio.file.Paths
import scala.io.{BufferedSource, Source}

trait
SourceContent {
  val source: BufferedSource
  val sourceLines: Seq[String]
  val sourceString: String
}

object SourceContent extends App {
  private lazy val NullSourceContent: URL = classOf[SourceContent].getResource("/NullSourceContent")
  def apply(
             _sourceRoot: URI,
             _logicalPath: String
           ) : SourceContent = new SourceContent {
    val sourceURI: URI = _sourceRoot.resolve (URI.create(_logicalPath))
    override val source: BufferedSource = Source.fromFile(sourceURI)
    override val sourceLines: Seq[String] = source.getLines.toSeq
    override val sourceString: String = sourceLines.mkString
    source.close()
  }
}