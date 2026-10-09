package draco.service.zeromq

import draco._
import io.circe.{Json, parser}
import io.circe.syntax._
import org.evrete.api.Knowledge
import scala.util.control.NonFatal

/** Handwritten staging rules for the provisional bridge protocol, not codec syntax.
  * Each rule retracts its message, so repeated fires cannot resend old facts.
  */
object BridgeRules {
  final case class Incoming(text: String)
  final case class Result(source: TypeName, payload: Json)
  final case class Endpoint(domain: TypeName, deliver: Json => Unit)

  final class Routing(ontology: DomainOntology, endpoints: Seq[Endpoint]) {
    private val domains = ontology.keys.toVector
    private val inputs = endpoints.toVector
    def resolve(name: TypeName): Either[String, Endpoint] = {
      val matches = domains.filter(_.typeDefinition.typeName == name)
      if (matches.isEmpty) Left("unknown domain")
      else if (matches.size != 1) Left("ambiguous domain")
      else inputs.filter(_.domain == matches.head.typeDefinition.typeName) match {
        case Seq(endpoint) => Right(endpoint)
        case Seq() => Left("missing input endpoint")
        case _ => Left("ambiguous input endpoint")
      }
    }
  }

  def install(k: Knowledge, routing: Routing, output: draco.service.TextOutput,
              failed: String => Unit): Unit = {
    val incoming = Rule[Incoming](
      _pattern = _ => (),
      _action = ctx => {
        val wire = ctx.get[Incoming]("$wire")
        ctx.deleteFact("$wire")
        val decoded = for {
          envelope <- parser.parse(wire.text).left.map(_ => "malformed JSON")
          _ <- Either.cond(envelope.isObject, (), "envelope must be an object")
          identity <- envelope.hcursor.downField("destinationDomain").as[TypeName]
            .left.map(_ => "invalid destinationDomain")
          payload <- envelope.hcursor.downField("payload").focus.toRight("missing payload")
          endpoint <- routing.resolve(identity)
        } yield (endpoint, payload)
        decoded match {
          case Left(error) => failed(error)
          case Right((endpoint, payload)) =>
            try endpoint.deliver(payload)
            catch { case NonFatal(e) => failed(s"input endpoint failed: ${e.getClass.getSimpleName}") }
        }
      })
    k.builder().newRule("draco.service.zeromq.Incoming")
      .forEach("$wire", classOf[Incoming]).execute(incoming.action(_)).build()

    val result = Rule[Result](
      _pattern = _ => (),
      _action = ctx => {
        val value = ctx.get[Result]("$result")
        ctx.deleteFact("$result")
        routing.resolve(value.source) match {
          case Left(error) => failed(error)
          case Right(_) =>
            val envelope = Json.obj("sourceDomain" -> value.source.asJson, "payload" -> value.payload)
            if (!output.send(envelope.noSpaces)) failed("transport rejected output")
        }
      })
    k.builder().newRule("draco.service.zeromq.Result")
      .forEach("$result", classOf[Result]).execute(result.action(_)).build()
  }
}
