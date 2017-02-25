package anonymouspatterns

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.Transform
import nsc.transform.TypingTransformers

class AnonymousPatterns(val global : Global) extends Plugin {
   import global._

   val name = "anonymous-patterns"
   val description = "Allows anonymous pattern matches without a stable identifier."
   val components = List[PluginComponent](Component)

   private object Component extends PluginComponent
         with Transform
         with TypingTransformers {

      val global : AnonymousPatterns.this.global.type = AnonymousPatterns.this.global
      val runsAfter = List("parser")
      val phaseName = "anonymous-patterns"
      def newTransformer(unit : CompilationUnit) : BaseTransformer =
         new BaseTransformer(unit)

      val Arrow = newTermName("$minus$greater")
      def freshTerm() : TermName =
         freshTermName("anonymous_pattern")(currentFreshNameCreator)
      def freshType() : TypeName =
         freshTypeName("anonymous_pattern")(currentFreshNameCreator)

      class BaseTransformer(unit : CompilationUnit) extends TypingTransformer(unit) {

         val patternTransformer = new PatternTransformer()

         override def transform(tree : Tree) : Tree = tree match {
            case Match(sel, cases) ⇒
               val casesExtractors = cases map {
                  /*case CaseDef(Apply(clazz, List(Apply(Ident(Arrow), List(Bind(accessor, Ident(termNames.WILDCARD)), bind)))), guard, body) ⇒
                     (CaseDef(Apply(clazz, List(bind)), guard, body), None)*/
                  case CaseDef(pat, guard, body) ⇒
                     val newPat = patternTransformer.transform(pat)
                     val extractors = patternTransformer.stack.toList
                     patternTransformer.stack.clear()
                     (CaseDef(newPat, guard, body), extractors)
               }
               val newCases = casesExtractors.map(_._1)
               println("------------newCases-------------")
               println(newCases)
               println(showRaw(newCases))
               val res = Block(
                  casesExtractors.map(_._2).flatten,
                  Match(sel, newCases))
               println(res)
               res
            case _ ⇒ super.transform(tree)
         }
      }

      class PatternTransformer extends Transformer {
         val stack : collection.mutable.Stack[Tree] =
            collection.mutable.Stack()

         override def transform(tree : Tree) : Tree = tree match {
            case Apply(
               Ident(clazz),
               List(
                  Apply(
                     Ident(Arrow),
                     List(Bind(accessor, Ident(termNames.WILDCARD)), bind)))) ⇒

               val extractorName = freshTerm()
               val extractor =
                  q"""
                   private object $extractorName {
                      def unapply(clazz : ${clazz.toTypeName}) =
                         Some(clazz.${accessor.toTermName})
                   }
                   """
               stack.push(extractor)
               Apply(
                  Ident(extractorName),
                  List(bind))
            case _ ⇒ super.transform(tree)
         }
      }
   }
}
