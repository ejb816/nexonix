package org.nexonix.json

import io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

sealed trait Domain {
  val typeName: String
  val viewName: String
  val sharedSchema: String
}

case class DataDomain(typeName: String,
                      viewName: String,
                      dataSchema: String,
                      sharedSchema: String) extends Domain

case class MessageDomain(typeName: String,
                         viewName: String,
                         messageSchema: String,
                         sharedSchema: String) extends Domain

case class TransformationRepository(dataDomain: DataDomain,
                                    messageDomains: Seq[MessageDomain])

object Main {

  def main(args: Array[String]): Unit = {
    val tdmTransRepo = TransformationRepository(
      DataDomain("ADM", "Any Data Model", "/DataDomain/ADM/schema.json", "/DataDomain/schema.json"),
      Seq(
        MessageDomain("MsgTypeA", "Message Type A", "/MessageDomains/MsgTypeA/schema.json", "/DataDomain/schema.json"),
        MessageDomain("MsgTypeB", "Message Type B", "/MessageDomains/MsgTypeB/schema.json", "/DataDomain/schema.json")
      )
    )
    val json = tdmTransRepo.asJson.spaces2
    val decodeTransRepo = decode[TransformationRepository](json)
    println(json)
    println(decodeTransRepo)
  }
}
