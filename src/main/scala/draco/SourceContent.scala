package draco

import java.net.{URI, URL}
import scala.io.{BufferedSource, Source}

trait SourceContent extends DracoType {
  val source: BufferedSource
  val sourceLines: Seq[String]
  val sourceString: String
}

object SourceContent extends App with DracoType {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("SourceContent"))
  lazy val dracoType: Type[SourceContent] = Type[SourceContent] (typeDefinition)

  private lazy val NullSourceContent: URL = classOf[SourceContent].getResource("/NullSourceContent")
  def apply (
              _sourceRoot: URI,
              _logicalPath: String
            ) : SourceContent = new SourceContent {
    override val typeDefinition: TypeDefinition = SourceContent.typeDefinition
    val sourceURI: URI = _sourceRoot.resolve(URI.create(_logicalPath))
    override val source: BufferedSource = Source.fromFile(sourceURI)
    override val sourceLines: Seq[String] = source.getLines.toSeq
    override val sourceString: String = sourceLines.mkString("\n")
    source.close()
  }
}
