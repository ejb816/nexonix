package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait CLI[L] extends DracoType

object CLI extends DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("CLI", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[CLI[_]] = Type[CLI[_]] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def load(path: String): TypeDefinition = {
    val source: scala.io.BufferedSource = scala.io.Source.fromFile(path)
    val text: String = try source.mkString finally source.close()
    io.circe.parser.parse(text).flatMap(_.as[TypeDefinition]).getOrElse(TypeDefinition.Null)
  }
  def version: Unit = println("Draco 2.0.0-alpha.1")
  def inspect(path: String): Unit = println(TypeDefinition.encoder(load(path)).spaces2)
  def generate(path: String): Unit = println(scalatarget.ScalaTarget.generator(load(path)))
  def drake(path: String): Unit = println(draco.drake.Drake.generator(load(path)))
  lazy val commands: Map[String, Seq[String] => Unit] = Map("version" -> (_ => version), "inspect" -> (as => inspect(as.head)), "generate" -> (as => generate(as.head)), "drake" -> (as => drake(as.head)))
  def main(args: Array[String]): Unit = commands.getOrElse(args.headOption.getOrElse("version"), (_: Seq[String]) => version)(args.drop(1).toSeq)
}
