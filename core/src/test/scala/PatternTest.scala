package anonymouspatterns

object PatternTest {
   case class Foo(a : Int, b : Boolean)
   case class Bar(c : String, d : Foo)
   Foo(5, true) match {
      case Foo(a -> i) ⇒ i
   }
   Foo(5, true) match {
      case Foo(a -> i, b -> t) ⇒ (i, t)
   }
   Bar("", Foo(5, true)) match {
      case Bar(d -> Foo(a -> i, b -> t)) ⇒ (i, t)
   }
   Foo(5, true) match {
      case Foo(_ -> _) ⇒ "Foo"
   }
}
