package draco

object CLI extends DracoType {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(
    TypeName ("CLI", _namePackage = Seq ("draco"))
  )
  lazy val typeInstance: DracoType = this
  def main (args: Array[String]): Unit = {
    println ("Draco 2.0.0-alpha.1")
  }
}
