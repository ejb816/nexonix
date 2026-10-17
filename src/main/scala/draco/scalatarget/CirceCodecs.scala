package draco.scalatarget

import draco._

trait CirceCodecs extends DracoType

object CirceCodecs extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CirceCodecs", _namePackage = Seq ("draco", "scalatarget")))
  lazy val dracoType: Type[CirceCodecs] = Type[CirceCodecs] (typeDefinition)
  lazy val domainType: Domain[ScalaTarget] = Domain[ScalaTarget] (typeDefinition)

  def encoder[T](_host: => io.circe.Encoder[T]): draco.format.dracon.Encoder[T] = {
    lazy val host: io.circe.Encoder[T] = _host
    draco.format.dracon.Encoder(value => draco.format.dracon.Dracon(host(value)))
  }
  def decoder[T](_host: => io.circe.Decoder[T]): draco.format.dracon.Decoder[T] = {
    lazy val host: io.circe.Decoder[T] = _host
    draco.format.dracon.Decoder(source => (host.decodeJson(source.json).toOption match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() }))
  }
}
