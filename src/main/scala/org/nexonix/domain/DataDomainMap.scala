package org.nexonix.domain


import io.circe.{Decoder, Encoder, HCursor}

case class DataDomainMap(elems: (String, DataDomain)*) extends DomainMap[DataDomain] {
  protected val internalMap: Map[String, DataDomain] = Map(elems: _*)

  def messageDomain(key: String): MessageDomain = this (key) match {
    case md: MessageDomain =>
      md
    case _ =>
      null
  }

  override implicit val encoder: Encoder[DataDomain] = (a: DataDomain) => a.asJson
}

object DataDomainMap {
  implicit val DataDomainMapDecoder: Decoder[DataDomainMap] = (c: HCursor) => {
    for {
      map <- c.as[(String, DataDomain)]
    } yield DataDomainMap(map)
  }
}
