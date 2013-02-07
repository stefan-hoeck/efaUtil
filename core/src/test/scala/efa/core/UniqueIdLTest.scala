package efa.core

import org.scalacheck._, Prop._
import scalaz._, Scalaz._

object UniqueIdLTest extends Properties("UniqueIdL") {
  implicit val PairUid: UniqueIdL[(Int,String),Int] =
    UniqueIdL lens Lens.firstLens

  property("generateIds_List") = {
    val ss = List.fill(1000000)((0, ""))
    val exp = ss.zipWithIndex map { case ((_,s),i) ⇒ (i,s) }

    PairUid.generateIds(ss) ≟ exp
  }

  property("generateIds_Vector") = forAll{ ss: List[String] ⇒
    val exp = ss.toVector.zipWithIndex map { _.swap }

    PairUid.generateIds(ss.toVector map { (0, _) }) ≟ exp
  }
}

// vim: set ts=2 sw=2 et:
