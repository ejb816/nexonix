package draco.dreams

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps
import scala.collection.mutable

trait Initialization extends DracoType {
  val domains: Seq[draco.dreams.user.Create]
  val initial: draco.DomainOntology
  val responses: mutable.Buffer[draco.dreams.user.Response]
  var ontology: draco.DomainOntology
  val note: draco.dreams.user.Response => Unit
  val keep: draco.DomainOntology => Unit
  def owns(_latent: => draco.dreams.user.Latent, _typeName: => draco.TypeName): Boolean = {
    lazy val latent: draco.dreams.user.Latent = _latent
    lazy val typeName: draco.TypeName = _typeName
    latent.definition.typeName.namePath == typeName.namePath || latent.members.exists(m => m.typeName.namePath == typeName.namePath)
  }
  def parents(_latent: => draco.dreams.user.Latent): Seq[draco.TypeName] = {
    lazy val latent: draco.dreams.user.Latent = _latent
    latent.members.prepended(latent.definition).flatMap(td => draco.DracoAspect.parents(td.dracoAspect)).filter(p => !owns(latent, p))
  }
  def ready(_latent: => draco.dreams.user.Latent, _placed: => Seq[draco.dreams.user.Create]): Boolean = {
    lazy val latent: draco.dreams.user.Latent = _latent
    lazy val placed: Seq[draco.dreams.user.Create] = _placed
    parents(latent).filter(p => domains.exists(l => owns(l, p))).forall(p => placed.exists(l => owns(l, p)))
  }
  def order(_placed: => Seq[draco.dreams.user.Create], _remaining: => Seq[draco.dreams.user.Create]): Seq[draco.dreams.user.Create] = {
    lazy val placed: Seq[draco.dreams.user.Create] = _placed
    lazy val remaining: Seq[draco.dreams.user.Create] = _remaining
    lazy val wave: Seq[draco.dreams.user.Create] = remaining.filter(l => ready(l, placed))
    lazy val rest: Seq[draco.dreams.user.Create] = remaining.filterNot(l => ready(l, placed))
    (if (wave.isEmpty) draco.drake.Present[Unit](()) else draco.drake.Absent[Unit]()).ifThenElse(placed ++ remaining, order(placed ++ wave, rest))
  }
  def ordered: Seq[draco.dreams.user.Create] = order(Seq.empty, domains)
  def editorType: draco.ActorType = draco.dreams.editor.Editor.actorType(initial, note, keep)
  def run(_editor: => draco.dreams.user.Create => Unit): Unit = {
    lazy val editor: draco.dreams.user.Create => Unit = _editor
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
      _domains <- cursor.downField("domains").as[Option[Seq[draco.dreams.user.Create]]].map(_.getOrElse(Seq.empty))
    } yield Initialization (_domains)
  }

  def apply (
    _domains: => Seq[draco.dreams.user.Create] = Seq.empty
  ) : Initialization = new Initialization {
    override lazy val domains: Seq[draco.dreams.user.Create] = _domains
    override lazy val initial: draco.DomainOntology = draco.DomainOntology()
    override lazy val responses: mutable.Buffer[draco.dreams.user.Response] = scala.collection.mutable.ArrayBuffer()
    override var ontology: draco.DomainOntology = initial
    override lazy val note: draco.dreams.user.Response => Unit = r => responses += r
    override lazy val keep: draco.DomainOntology => Unit = o => ontology = o
    override lazy val typeDefinition: TypeDefinition = Initialization.typeDefinition
  }

  lazy val Null: Initialization = apply()


}
