# anonymous-patterns
Allows inline definition of patterns with named arguments in Scala.

## Matching Generic Types

Unlike normal pattern-matching the identifier for an anonymous pattern is a *type* identifier rather than a *term* identifier. Therefore the following pattern match will fail:

```scala
Option(5) match {
   case Some(get->i) => i
}
```

This fails because the type `Some` takes one type parameter. To match against generic types you need to instatiate them to some type:

```scala
Option(5) match {
   case Some[Int](get->i) => i
}
```

## Ignoring All Arguments

A special syntax is provided to create `Boolean` extractors, i.e. patterns that don't extract any members.

```scala
User(5L, "Julie", false) match {
   case User(_->_) => "User Found"
}
```
