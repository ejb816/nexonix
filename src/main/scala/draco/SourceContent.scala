package draco

import java.net.URI
import scala.io.BufferedSource

trait SourceContent extends DracoType {
  val source: BufferedSource
  val sourceLines: Seq[String]
  val sourceString: String
}

object SourceContent extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("SourceContent", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[SourceContent] = Type[SourceContent] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def apply (
    _sourceRoot: URI,
    _logicalPath: String
  ) : SourceContent = new SourceContent {
    val sourceURI: java.net.URI = _sourceRoot.resolve(java.net.URI.create(_logicalPath))
    override lazy val source: BufferedSource = scala.io.Source.fromFile(sourceURI)
    override lazy val sourceLines: Seq[String] = try source.getLines().toSeq finally source.close()
    override lazy val sourceString: String = sourceLines.mkString("\n")
    override lazy val typeDefinition: TypeDefinition = SourceContent.typeDefinition
  }

  lazy val Null: SourceContent = apply(
    _sourceRoot = null.asInstanceOf[URI],
    _logicalPath = ""
  )

}
