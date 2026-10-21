package draco.scalatarget

import draco._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.EncoderOps

trait HostBindings extends DracoType

object HostBindings extends App with DracoType {
  override lazy val typeDefinition: TypeDefinition = TypeLoader.loadType(TypeName ("HostBindings", _namePackage = Seq ("draco", "scalatarget")))
  lazy val dracoType: Type[HostBindings] = Type[HostBindings] (typeDefinition)
  lazy val domainType: Domain[ScalaTarget] = Domain[ScalaTarget] (typeDefinition)

  lazy val primitives: Seq[(String, draco.TypeName)] = Seq(("Any", draco.TypeName("Any")), ("Boolean", draco.TypeName("Boolean")), ("Double", draco.TypeName("Double")), ("Int", draco.TypeName("Int")), ("Long", draco.TypeName("Long")), ("Product", draco.TypeName("Product")), ("String", draco.TypeName("String")), ("Integer", draco.TypeName("Integer")), ("System", draco.TypeName("System")), ("Enumeration", draco.TypeName("Enumeration")))
  lazy val collections: Seq[(String, draco.TypeName)] = Seq(("Array", draco.TypeName("Array")), ("Iterator", draco.TypeName("Iterator")), ("Map", draco.TypeName("Map")), ("Option", draco.TypeName("Option")), ("Seq", draco.TypeName("Seq")), ("Some", draco.TypeName("Some")), ("ArrayBuffer", draco.TypeName("ArrayBuffer", Seq("scala", "collection", "mutable"))))
  lazy val seams: Seq[(String, draco.TypeName)] = Seq(("Json", draco.TypeName("Json", Seq("io", "circe"))), ("io.circe.Encoder", draco.TypeName("Encoder", Seq("io", "circe"))), ("io.circe.Decoder", draco.TypeName("Decoder", Seq("io", "circe"))), ("URI", draco.TypeName("URI", Seq("java", "net"))), ("URL", draco.TypeName("URL", Seq("java", "net"))), ("File", draco.TypeName("File", Seq("java", "io"))), ("Files", draco.TypeName("Files", Seq("java", "nio", "file"))), ("Path", draco.TypeName("Path", Seq("java", "nio", "file"))), ("Paths", draco.TypeName("Paths", Seq("java", "nio", "file"))), ("StandardCharsets", draco.TypeName("StandardCharsets", Seq("java", "nio", "charset"))), ("BufferedSource", draco.TypeName("BufferedSource", Seq("scala", "io"))), ("scala.io.Source", draco.TypeName("Source", Seq("scala", "io"))), ("Try", draco.TypeName("Try", Seq("scala", "util"))), ("ActivationMode", draco.TypeName("ActivationMode", Seq("org", "evrete", "api"))), ("org.evrete.api.Knowledge", draco.TypeName("Knowledge", Seq("org", "evrete", "api"))), ("KnowledgeService", draco.TypeName("KnowledgeService", Seq("org", "evrete", "api"))), ("StatefulSession", draco.TypeName("StatefulSession", Seq("org", "evrete", "api"))), ("ExtensibleBehavior", draco.TypeName("ExtensibleBehavior", Seq("org", "apache", "pekko", "actor", "typed"))))
  lazy val engine: Seq[(String, draco.TypeName)] = Seq(("DracoGenerator", draco.TypeName("DracoGenerator", Seq("draco"))), ("draco.Drake", draco.TypeName("Drake", Seq("draco"))), ("NameResolution", draco.TypeName("NameResolution", Seq("draco"))))
  lazy val bindings: Seq[(String, draco.TypeName)] = primitives ++ collections ++ seams ++ engine
  def vocabulary: Map[String, draco.TypeName] = bindings.toMap
}
