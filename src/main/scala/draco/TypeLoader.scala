package draco

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps
import java.net.URL

trait TypeLoader extends DracoType

object TypeLoader extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("TypeLoader", _namePackage = Seq ("draco")))
  lazy val dracoType: Type[TypeLoader] = Type[TypeLoader] (typeDefinition)
  lazy val domainType: Domain[Draco] = Domain[Draco] (typeDefinition)

  def readDefinition(url: URL): Option[TypeDefinition] = Option(url).flatMap { u => val source = scala.io.Source.fromURL(u); try io.circe.parser.parse(source.mkString).flatMap(_.as[TypeDefinition]).toOption finally source.close() }
  def loadFromResource(resourcePath: String): Option[TypeDefinition] = DefinitionPath.default.source(resourcePath).flatMap(readDefinition)
  def tryLoad(typeName: TypeName): Option[TypeDefinition] = loadFromResource(typeName.resourcePath)
  def rooted(td: TypeDefinition): TypeDefinition = if (td.dracoAspect.derivation.exists(_.namePackage.nonEmpty) || (td.typeName.name == "DracoType" && td.typeName.namePackage == Seq("draco"))) td else TypeDefinition(_typeName = td.typeName, _dracoAspect = DracoAspect(_superDomain = td.dracoAspect.superDomain, _modules = td.dracoAspect.modules, _extensible = td.dracoAspect.extensible, _derivation = td.dracoAspect.derivation :+ TypeName("DracoType", _namePackage = Seq("draco")), _elements = td.dracoAspect.elements, _factory = td.dracoAspect.factory, _globalElements = td.dracoAspect.globalElements, _source = td.dracoAspect.source, _target = td.dracoAspect.target), _domainAspect = td.domainAspect, _ruleAspect = td.ruleAspect, _actorAspect = td.actorAspect, _codecAspect = td.codecAspect)
  def loadType(typeName: TypeName): TypeDefinition = tryLoad(typeName).map(rooted).getOrElse(TypeDefinition(typeName))
}
