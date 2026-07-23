package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait TypeLoader extends DracoType

object TypeLoader extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeLoader", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeLoader] = Type[TypeLoader] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def loadFromResource(resourcePath: String): Option[TypeDefinition] = Option(getClass.getResourceAsStream(resourcePath)).flatMap { stream => val source = scala.io.Source.fromInputStream(stream); try io.circe.parser.parse(source.mkString).flatMap(_.as[TypeDefinition]).toOption finally source.close() }
  def tryLoad(typeName: TypeName): Option[TypeDefinition] = loadFromResource(typeName.resourcePath)
  def loadType(typeName: TypeName): TypeDefinition = tryLoad(typeName).getOrElse(TypeDefinition(typeName))
}
