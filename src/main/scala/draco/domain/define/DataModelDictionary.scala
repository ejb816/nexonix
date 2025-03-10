package draco.domain.define

import draco.domain.{Dictionary, Domain, TypeData, TypeName}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import scala.language.implicitConversions

trait DataModelDictionary extends Dictionary[DataModel]

object DataModelDictionary {
  def apply(typeNames: (TypeName, Seq[TypeName])*): DataModelDictionary = {
    val elements: (String, DataModel) = ???
    new DataModelDictionary {
      override protected val internalMap: Map[String, DataModel] = ???
    }
  }
}