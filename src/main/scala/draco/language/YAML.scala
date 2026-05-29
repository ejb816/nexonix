package draco.language

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

object YAML extends DracoType {
  override lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("YAML", _namePackage = Seq ("draco", "language")))
  lazy val dracoType: DracoType = this
  lazy val domainType: Domain[Language] = Domain[Language] (typeDefinition)

  import io.circe.{parser => jsonParser}
  import io.circe.yaml.{parser => yamlParser, printer => yamlPrinter}

  /** Parse a YAML string into a TypeDefinition. */
  def loadTypeDefinition(yaml: String): TypeDefinition =
    yamlParser.parse(yaml).flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)

  /** Render a TypeDefinition as a YAML string. */
  def emit(td: TypeDefinition): String =
    yamlPrinter.print(td.asJson)

  /** Convert a JSON string into YAML. */
  def fromJson(json: String): String =
    jsonParser.parse(json).map(yamlPrinter.print).getOrElse("")

  /** Convert a YAML string into JSON (compact pretty-printed). */
  def toJson(yaml: String): String =
    yamlParser.parse(yaml).map(_.spaces2).getOrElse("")
}
