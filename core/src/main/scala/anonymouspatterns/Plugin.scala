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
      def newExtractor(clazz : Name) : TermName =
         freshTermName("anonymous_pattern$" + clazz.toString)(currentFreshNameCreator)

      class BaseTransformer(unit : CompilationUnit) extends TypingTransformer(unit) {

         val patternTransformer = new PatternTransformer()

         override def transform(tree : Tree) : Tree = tree match {
            case Match(sel, cases) ⇒
               val casesExtractors = cases map {
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
               Apply(
                  Ident(Arrow),
                  List(Bind(accessor1, Ident(termNames.WILDCARD)), bind1)) :: rest) ⇒

               val accessorsBinders = rest flatMap {
                  case Apply(
                     Ident(Arrow),
                     List(Bind(accessor, Ident(termNames.WILDCARD)), bind)) ⇒

                     List((accessor, transform(bind)))
                  case _ ⇒
                     error("Invalid anonymous pattern")
                     Nil
               }
               val accessors = accessor1 :: accessorsBinders.map(_._1)
               val binders = transform(bind1) :: accessorsBinders.map(_._2)

               val accessed = accessors.map(accessor ⇒
                  q"clazz.${accessor.toTermName}")

               val extractorName = newExtractor(clazz)
               val extractor =
                  q"""
                   private object $extractorName {
                      def unapply(clazz : ${clazz.toTypeName}) =
                         Some((..$accessed))
                   }
                   """
               stack.push(extractor)
               Apply(
                  Ident(extractorName),
                  binders)
            case Apply(
               Ident(clazz),
               List(Apply(
                  Ident(Arrow),
                  List(Ident(termNames.WILDCARD), Ident(termNames.WILDCARD))))) ⇒

               val extractorName = newExtractor(clazz)
               val extractor =
                  q"""
                   private object $extractorName {
                      def unapply(clazz : ${clazz.toTypeName}) : Boolean = true
                   }
                   """
               stack.push(extractor)
               Apply(
                  Ident(extractorName),
                  Nil)
            case _ ⇒ super.transform(tree)
         }
      }
   }
}
