package org.nexonix.domain

import io.circe.Json

trait MessageDomain extends DataDomain {
  val coDomains: DataDomainMap
  override def asJson: Json = Option(coDomains) match {
    case Some(domains) =>
      Json.obj("coDomains" -> domains.asJson).deepMerge(super.asJson)
    case None =>
      Json.obj().deepMerge(super.asJson)
  }
}
