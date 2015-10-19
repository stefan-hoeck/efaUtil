package efa.core
//
//import org.scalacheck._, Prop._
//import scalaz._, Scalaz._
//
//object LensesTest extends Properties("Lenses") {
//
//  property("traverseLookupBy") = forAll{ i: Int ⇒
//    val l: List[Int] @?> Int = Lenses traverseLookupBy { i == _ }
//    val is = List(i - 1, i, i + 1)
//    val notFound = List(i - 1, i + 1)
//
//    (l.get(is) ≟ i.some) :| "returns i if it exists" &&
//    (l.set(is, i + 2) ≟ List(i - 1, i + 2, i + 1).some) :| "replaces i if it exists" &&
//    (l.get(notFound) ≟ none) :| "get returns none if i doesnt's exist" &&
//    (l.set(notFound, i + 2) ≟ none) :| "set returns none if i doesnt't exist"
//  }
//}
//
//// vim: set ts=2 sw=2 et:
