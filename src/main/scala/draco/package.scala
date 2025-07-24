package object draco {
  trait Declared extends App {
    def declared (declaredType: Class[_]) : Unit = {
      println(s"Declared and compiled type ${declaredType.getName}")
    }
  }
  trait Draco
  object Draco extends Declared {

    Base.main(Array[String]())
    declared(classOf[Base])
    Dictionary.main(Array[String]())
    declared(classOf[Dictionary[String,String]])
    Generator.main(Array[String]())
    declared(classOf[Generator])
    Member.main(Array[String]())
    declared(classOf[Member])
    RootType.main(Array[String]())
    declared(classOf[RootType])
    SourceContent.main(Array[String]())
    declared(classOf[SourceContent])
    TypeDefinition.main(Array[String]())
    declared(classOf[TypeDefinition])
    TypeDictionary.main(Array[String]())
    declared(classOf[TypeDictionary])
    TypePackage.main(Array[String]())
    declared(classOf[TypePackage])
    TypePackage.main(Array[String]())
    declared(classOf[TypePackage])
    Value.main(Array[String]())
    declared(classOf[Value])
  }
}