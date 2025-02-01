package org.nexonix.domain

import io.circe.generic.auto._
import io.circe.{Decoder, HCursor, Json}

trait DataDomain extends Domain {
  val dataSchema: String
  val superDomain: DataDomain
  val subDomains: DataDomainMap

  def createMap: DataDomain => DataDomainMap = _ => DataDomainMap()

  override def asJson: Json = Json.obj(
    "dataSchema" -> Json.fromString(dataSchema),
    "superDomain" -> Json.fromString(superDomain.typeName),
    "subDomains" -> subDomains.asJson
  ).deepMerge(super.asJson)
}

object DataDomain {
  implicit def decoder: Decoder[DataDomain] = (c: HCursor) => for {
    _domain <- decoder.apply(c)
    _dataSchema <- c.downField("dataSchema").as[String]
    _superDomain <- c.downField("superDomain").as[DataDomain]
    _subDomain <- c.downField("subDomains").as[DataDomainMap]
  } yield new DataDomain {
    override val dataSchema: String = _dataSchema
    override val superDomain: DataDomain = _superDomain
    override val subDomains: DataDomainMap = _subDomain
    override val ontosName: String = _domain.ontosName
    override val typeName: String = _domain.typeName
    override val viewName: String = _domain.viewName
  }
}
