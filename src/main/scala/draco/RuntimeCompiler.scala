package draco

import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.StoreReporter
import java.io.{File, PrintWriter}
import java.nio.file.Files

trait RuntimeCompiler extends TypeInstance

object RuntimeCompiler extends App with TypeInstance {
  lazy val typeDefinition: TypeDefinition = Generator.loadType(TypeName ("RuntimeCompiler", _namePackage = Seq("draco")))
  lazy val typeInstance: Type[RuntimeCompiler] = Type[RuntimeCompiler] (typeDefinition)

  def compile(source: String, fileName: String): Either[Seq[String], File] = {
    val tempDir = Files.createTempDirectory("draco-gen").toFile
    val sourceFile = new File(tempDir, fileName)
    val writer = new PrintWriter(sourceFile)
    writer.write(source)
    writer.close()

    val settings = new Settings()
    settings.usejavacp.value = true
    settings.outputDirs.setSingleOutput(tempDir.getAbsolutePath)

    val reporter = new StoreReporter(settings)
    val compiler = new Global(settings, reporter)
    val run = new compiler.Run()
    run.compile(List(sourceFile.getAbsolutePath))

    if (reporter.hasErrors) {
      Left(reporter.infos.collect {
        case info if info.severity == reporter.ERROR => info.msg
      }.toSeq)
    } else {
      Right(tempDir)
    }
  }

  def compileMulti(sources: Seq[(String, String)]): Either[Seq[String], File] = {
    val tempDir = Files.createTempDirectory("draco-gen").toFile
    val sourceFiles = sources.map { case (source, fileName) =>
      val sourceFile = new File(tempDir, fileName)
      val writer = new PrintWriter(sourceFile)
      writer.write(source)
      writer.close()
      sourceFile.getAbsolutePath
    }

    val settings = new Settings()
    settings.usejavacp.value = true
    settings.outputDirs.setSingleOutput(tempDir.getAbsolutePath)

    val reporter = new StoreReporter(settings)
    val compiler = new Global(settings, reporter)
    val run = new compiler.Run()
    run.compile(sourceFiles.toList)

    if (reporter.hasErrors) {
      Left(reporter.infos.collect {
        case info if info.severity == reporter.ERROR => info.msg
      }.toSeq)
    } else {
      Right(tempDir)
    }
  }

  def loadClass(classDir: File, className: String): Class[_] = {
    val loader = new java.net.URLClassLoader(
      Array(classDir.toURI.toURL),
      Thread.currentThread.getContextClassLoader
    )
    loader.loadClass(className)
  }
}
