package draco

import java.net.{URI, URL}
import scala.io.{BufferedSource, Source}

trait SourceContent {
  val resourceClass: Class[_]
  val resourceURL: URL
  val resourcePath: String
  val resourceURI: URI
  val source: BufferedSource
  val sourceLines: Seq[String]
  val sourceString: String
}

object SourceContent extends App {
  println("Companion object SourceContent exists.")
  def apply(
             _resourcePath: String,
             _resourceClass: Class[_] = classOf[SourceContent]
           ) : SourceContent = new SourceContent {
    override val resourcePath: String = _resourcePath
    override val resourceClass: Class[_] = _resourceClass
    override val resourceURL: URL = resourceClass.getResource(resourcePath)
    override val resourceURI: URI = resourceURL.toURI
    override val source: BufferedSource = Source.fromFile(resourceURL.toURI)
    override val sourceLines: Seq[String] = source.getLines.toSeq
    override val sourceString: String = sourceLines.mkString
    source.close()
  }
}