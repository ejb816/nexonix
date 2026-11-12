package draco.draketarget

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait Fixity extends DracoType {
  val precedence: Int
  val associativity: String
}

object Fixity extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Fixity", _namePackage = Seq ("draco", "draketarget")))
  lazy val dracoType: Type[Fixity] = Type[Fixity] (typeDefinition)
  lazy val domainType: Domain[DrakeTarget] = Domain[DrakeTarget] (typeDefinition)

  implicit lazy val encoder: Encoder[Fixity] = Encoder.instance { x =>
    val fields = Seq(
      Some("precedence" -> x.precedence.asJson),
      Some("associativity" -> x.associativity.asJson)
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Fixity] = Decoder.instance { cursor =>
    for {
      _precedence <- cursor.downField("precedence").as[Int]
      _associativity <- cursor.downField("associativity").as[Option[String]].map(_.getOrElse(""))
    } yield Fixity (_precedence, _associativity)
  }

  def apply (
    _precedence: => Int,
    _associativity: => String
  ) : Fixity = new Fixity {
    override lazy val precedence: Int = _precedence
    override lazy val associativity: String = _associativity
    override lazy val typeDefinition: TypeDefinition = Fixity.typeDefinition
  }

  lazy val Null: Fixity = apply(
    _precedence = 0,
    _associativity = ""
  )

  lazy val operators: Seq[(String, Fixity)] = Seq(("*", Fixity(7, "left")), ("/", Fixity(7, "left")), ("%", Fixity(7, "left")), ("+", Fixity(6, "left")), ("-", Fixity(6, "left")), ("++", Fixity(5, "right")), ("==", Fixity(4, "none")), ("!=", Fixity(4, "none")), ("<", Fixity(4, "none")), ("<=", Fixity(4, "none")), (">", Fixity(4, "none")), (">=", Fixity(4, "none")), ("&&", Fixity(3, "right")), ("||", Fixity(2, "right")), ("->", Fixity(0, "right")))
  lazy val table: Map[String, Fixity] = operators.toMap
  lazy val flattened: Set[String] = Set("++", "&&", "||")
}
