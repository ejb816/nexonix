package org.nexonix.domain

import io.circe.{Decoder, HCursor, Json}

trait Domain {
  val ontosName: String
  val typeName: String
  val viewName: String
  def asJson: Json = {
    Json.obj(
      "dictionaryName" -> Json.fromString(ontosName),
      "typeName" -> Json.fromString(typeName),
      "viewName" -> Json.fromString(viewName)
    )
  }
}

object Domain {
  implicit val decoder: Decoder[Domain] = (c: HCursor) => for {
    _ontosName <- c.downField("dictionaryName").as[String]
    _typeName <- c.downField("typeName").as[String]
    _viewName <- c.downField("viewName").as[String]
  } yield new Domain {
    override val ontosName: String = _ontosName
    override val typeName: String = _typeName
    override val viewName: String = _viewName
  }
}
