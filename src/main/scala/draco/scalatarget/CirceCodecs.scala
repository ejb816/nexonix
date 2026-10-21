package draco.scalatarget

import draco.drake.Present
import draco.format.dracon.Dracon

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait CirceCodecs extends DracoType

object CirceCodecs extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CirceCodecs", _namePackage = Seq ("draco", "scalatarget")))
  lazy val dracoType: Type[CirceCodecs] = Type[CirceCodecs] (typeDefinition)
  lazy val domainType: Domain[ScalaTarget] = Domain[ScalaTarget] (typeDefinition)

  lazy val payloadEncoder: draco.format.dracon.Encoder[Dracon] = draco.format.dracon.Encoder(value => value)
  lazy val payloadDecoder: draco.format.dracon.Decoder[Dracon] = draco.format.dracon.Decoder(value => Present(value))
  lazy val encodePayload: io.circe.Encoder[Dracon] = io.circe.Encoder.instance(value => payloadEncoder.encode(value).json)
  lazy val decodePayload: io.circe.Decoder[Dracon] = io.circe.Decoder.instance(cursor => payloadDecoder.decode(Dracon(cursor.value)).fold(Left(io.circe.DecodingFailure("Absent payload", cursor.history)), value => Right(value)))
  def encoder[T](_host: => io.circe.Encoder[T]): draco.format.dracon.Encoder[T] = {
    lazy val host: io.circe.Encoder[T] = _host
    draco.format.dracon.Encoder(value => Dracon(host(value)))
  }
  def decoder[T](_host: => io.circe.Decoder[T]): draco.format.dracon.Decoder[T] = {
    lazy val host: io.circe.Decoder[T] = _host
    draco.format.dracon.Decoder(source => (host.decodeJson(source.json).toOption match { case Some(v) => Present(v); case None => draco.drake.Absent() }))
  }
}
