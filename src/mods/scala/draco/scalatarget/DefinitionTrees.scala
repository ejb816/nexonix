package draco.scalatarget

import draco.format.dracon.Dracon
import io.circe.Json
import scala.language.implicitConversions

/** Boundary adapters for the handwritten Scala parser/generator and their fixtures.
  * Definition-backed types use Dracon; the existing host tree algorithms use Circe.
  * These imports do not install a codec or change Dracon's wrapper wire format.
  */
object DefinitionTrees {
  implicit def wrap(value: Json): Dracon = Dracon(value)
  implicit def unwrap(value: Dracon): Json = value.json
  implicit def wrapMany(values: Seq[Json]): Seq[Dracon] = values.map(Dracon(_))
  implicit def unwrapMany(values: Seq[Dracon]): Seq[Json] = values.map(_.json)
}
