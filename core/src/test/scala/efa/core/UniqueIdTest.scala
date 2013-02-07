package efa.core

import org.scalacheck._, Prop._
import scalaz._, Scalaz._

object UniqueIdTest extends Properties("UniqueId") {
  implicit val PairUid: UniqueId[(Int,String),Int] =
    UniqueId get { _._1 }

  property("idPair") = forAll { p: (Int,String) ⇒ 
    val (i, s) = p

    PairUid.idPair(p) ≟ (i, (i, s))
  }

  property("idMap") = forAll { ss: List[String] ⇒ 
    val pairs = ss.zipWithIndex map { case (s,i) ⇒ (i,s) }
    val res = pairs.zipWithIndex map { case (p,i) ⇒ (i,p) } toMap

    PairUid.idMap(pairs) ≟ res
  }

  property("pairs") = forAll { ss: List[String] ⇒ 
    val pairs = ss.zipWithIndex map { case (s,i) ⇒ (i,s) }
    val res = pairs.zipWithIndex map { case (p,i) ⇒ (i,p) }

    PairUid.pairs(pairs) ≟ res
  }

  property("newId_nonEmpty") = forAll { ss: List[String] ⇒ 
    val pairs = ss.zipWithIndex map { case (s,i) ⇒ (i,s) }
    val exp = ss.isEmpty ? 1 | ss.size

    PairUid.newId(pairs) ≟ exp
  }
}

// vim: set ts=2 sw=2 et:
