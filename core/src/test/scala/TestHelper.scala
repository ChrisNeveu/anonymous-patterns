package anonymouspatterns

import tools.nsc.{ Global, Settings }
import tools.nsc.io.VirtualDirectory
import tools.nsc.reporters.{ ConsoleReporter, Reporter }
import reflect.internal.util.Position

object TestHelper {
   case class Result(errors : List[String], warnings : List[String])

   def compile(reporter : Option[Reporter] = None) = {
      val settings = new Settings()
      val virtualDirectory = new VirtualDirectory("(memory)", None)
      settings.outputDirs.setSingleOutput(virtualDirectory)
      settings.usejavacp.value = true
      settings.embeddedDefaults(getClass.getClassLoader)

      val global = new Global(settings, reporter.getOrElse(new ConsoleReporter(settings))) {
         override protected def loadRoughPluginsList() = List(new AnonymousPatterns(this))
      }
      val run = new global.Run()
      run.compile(Nil)

      !global.reporter.hasErrors
   }

   def compileFiles(paths : String*) : Result = {
      var errors = List[String]()
      var warnings = List[String]()
      compile(Some(new Reporter {
         override protected def info0(pos : Position, msg : String, severity : Severity, force : Boolean) = severity match {
            case ERROR   ⇒ errors ::= msg
            case WARNING ⇒ warnings ::= msg
            case _       ⇒
         }
      }))
      Result(errors, warnings)
   }
}
