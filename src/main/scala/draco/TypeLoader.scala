package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps
import java.net.URL

trait TypeLoader extends DracoType

object TypeLoader extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeLoader", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeLoader] = Type[TypeLoader] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def readDefinition(url: URL): draco.drake.Presence[TypeDefinition] = (Option(url).flatMap { u => val source = scala.io.Source.fromURL(u); try io.circe.parser.parse(source.mkString).flatMap(_.as[TypeDefinition]).toOption finally source.close() } match { case Some(v) => draco.drake.Present(v); case None => draco.drake.Absent() })
  def loadFromResource(_resourcePath: => String): draco.drake.Presence[TypeDefinition] = {
    lazy val resourcePath: String = _resourcePath
    draco.generator.carrier.DefinitionPath.default.source(resourcePath).fold(draco.drake.Absent(), readDefinition)
  }
  def tryLoad(_typeName: => TypeName): draco.drake.Presence[TypeDefinition] = {
    lazy val typeName: TypeName = _typeName
    loadFromResource(typeName.resourcePath)
  }
  def rooted(td: TypeDefinition): TypeDefinition = if (td.dracoAspect.derivation.exists(_.namePackage.nonEmpty) || (td.typeName.name == "DracoType" && td.typeName.namePackage == Seq("draco"))) td else TypeDefinition(_typeName = td.typeName, _dracoAspect = DracoAspect(_superDomain = td.dracoAspect.superDomain, _modules = td.dracoAspect.modules, _extensible = td.dracoAspect.extensible, _derivation = td.dracoAspect.derivation :+ TypeName("DracoType", _namePackage = Seq("draco")), _elements = td.dracoAspect.elements, _factory = td.dracoAspect.factory, _globalElements = td.dracoAspect.globalElements), _domainAspect = td.domainAspect, _ruleAspect = td.ruleAspect, _actorAspect = td.actorAspect, _codecAspect = td.codecAspect)
  def loadType(_typeName: => TypeName): TypeDefinition = {
    lazy val typeName: TypeName = _typeName
    tryLoad(typeName).fold(TypeDefinition(typeName), rooted)
  }
}
