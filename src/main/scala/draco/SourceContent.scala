package draco

import java.net.{URI, URL}
import scala.io.{BufferedSource, Source}

trait SourceContent {
  val resourceClass: Class[_]
  val resourceURL: URL
  val resourcePath: String
  val resultPath: String
  val source: BufferedSource
  val sourceLines: Seq[String]
  val sourceString: String
}

object SourceContent extends App {
  private lazy val NullSourceContent: URL = classOf[SourceContent].getResource("/NullSourceContent")
  def apply(
             _resourcePath: String,
             _resourceClass: Class[_] = classOf[SourceContent]
           ) : SourceContent = new SourceContent {
    override val resourcePath: String = _resourcePath
    override val resourceClass: Class[_] = _resourceClass
    override val resourceURL: URL = Option(resourceClass.getResource(resourcePath)).getOrElse(NullSourceContent)
    override val resultPath: String = resourceURL.getPath
    override val source: BufferedSource = Source.fromFile(resourceURL.toURI)
    override val sourceLines: Seq[String] = source.getLines.toSeq
    override val sourceString: String = sourceLines.mkString
    source.close()
  }
}