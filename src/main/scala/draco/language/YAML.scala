package draco.language

import draco._
import io.circe.{Json, parser => jsonParser}
import io.circe.syntax.EncoderOps
import io.circe.yaml.{parser => yamlParser, printer => yamlPrinter}

trait YAML

object YAML extends App {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("YAML", _namePackage = Seq ("draco", "language")))
  lazy val dracoType: Type[YAML] = Type[YAML] (typeDefinition)

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
