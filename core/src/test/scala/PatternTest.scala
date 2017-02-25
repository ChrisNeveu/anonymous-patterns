package anonymouspatterns

object PatternTest {
   case class Foo(a : Int, b : Boolean)
   Foo(5, true) match {
      case Foo(a -> i) ⇒ i
   }
}
