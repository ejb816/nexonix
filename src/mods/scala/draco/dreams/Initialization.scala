package draco.dreams

import draco.dreams.editor.Editor
import draco.dreams.user.Create
import draco.dreams.user.Latent
import draco.dreams.user.Response

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps
import scala.collection.mutable

trait Initialization extends DracoType {
  val domains: Seq[Create]
  val initial: draco.DomainOntology
  val responses: mutable.Buffer[Response]
  var ontology: draco.DomainOntology
  val note: Response => Unit
  val keep: draco.DomainOntology => Unit
  def owns(_latent: => Latent, _typeName: => draco.TypeName): Boolean = {
    lazy val latent: Latent = _latent
    lazy val typeName: draco.TypeName = _typeName
    latent.definition.typeName.namePath == typeName.namePath || latent.members.exists(m => m.typeName.namePath == typeName.namePath)
  }
  def parents(_latent: => Latent): Seq[draco.TypeName] = {
    lazy val latent: Latent = _latent
    latent.members.prepended(latent.definition).flatMap(td => draco.DracoAspect.parents(td.dracoAspect)).filter(p => !owns(latent, p))
  }
  def ready(_latent: => Latent, _placed: => Seq[Create]): Boolean = {
    lazy val latent: Latent = _latent
    lazy val placed: Seq[Create] = _placed
    parents(latent).filter(p => domains.exists(l => owns(l, p))).forall(p => placed.exists(l => owns(l, p)))
  }
  def order(_placed: => Seq[Create], _remaining: => Seq[Create]): Seq[Create] = {
    lazy val placed: Seq[Create] = _placed
    lazy val remaining: Seq[Create] = _remaining
    lazy val wave: Seq[Create] = remaining.filter(l => ready(l, placed))
    lazy val rest: Seq[Create] = remaining.filterNot(l => ready(l, placed))
    (if (wave.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(placed ++ remaining, order(placed ++ wave, rest))
  }
  def ordered: Seq[Create] = order(Seq.empty, domains)
  def editorType: draco.ActorType = Editor.actorType(initial, note, keep)
  def run(_editor: => Create => Unit): Unit = {
    lazy val editor: Create => Unit = _editor
    ordered.foreach(editor)
  }
  def complete: Boolean = responses.size == domains.size
  def accepted: Boolean = complete && responses.forall(r => r.accepted)
}

object Initialization extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("Initialization", _namePackage = Seq ("draco", "dreams")))
  lazy val dracoType: Type[Initialization] = Type[Initialization] (typeDefinition)
  lazy val domainType: Domain[Dreams] = Domain[Dreams] (typeDefinition)

  implicit lazy val encoder: Encoder[Initialization] = Encoder.instance { x =>
    val fields = Seq(
      if (x.domains.nonEmpty) Some("domains" -> x.domains.asJson) else None
    ).flatten
    Json.obj(fields: _*)
  }
  implicit lazy val decoder: Decoder[Initialization] = Decoder.instance { cursor =>
    for {
      _domains <- cursor.downField("domains").as[Option[Seq[Create]]].map(_.getOrElse(Seq.empty))
    } yield Initialization (_domains)
  }

  def apply (
    _domains: => Seq[Create] = Seq.empty
  ) : Initialization = new Initialization {
    override lazy val domains: Seq[Create] = _domains
    override lazy val initial: draco.DomainOntology = draco.DomainOntology()
    override lazy val responses: mutable.Buffer[Response] = scala.collection.mutable.ArrayBuffer()
    override var ontology: draco.DomainOntology = initial
    override lazy val note: Response => Unit = r => responses += r
    override lazy val keep: draco.DomainOntology => Unit = o => ontology = o
    override lazy val typeDefinition: TypeDefinition = Initialization.typeDefinition
  }

  lazy val Null: Initialization = apply()


}
