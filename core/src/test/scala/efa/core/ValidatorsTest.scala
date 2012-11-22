package efa.core

import scalaz._, Scalaz._
import org.scalacheck._

object ValidatorTest
  extends Properties("Validators") {
  import Validators._

  property("dummy") = Prop.forAll {s: String ⇒ dummy(s) ≟ s.right}

  property("notEmptyString") = Prop.forAll {s: String ⇒ 
    notEmptyString(s) fold (
      x ⇒ (x ≟ loc.isEmptyMsg.wrapNel) && s.trim.isEmpty,
      x ⇒ (x ≟ s) && x.trim.nonEmpty
    )
  }

  property("mustBeEmptyString") = Prop.forAll {s: String ⇒ 
    mustBeEmptyString(s) fold (
      x ⇒ (x ≟ loc.mustBeEmptyMsg.wrapNel) && s.nonEmpty,
      x ⇒ (x ≟ s) && (s.isEmpty)
    )
  }

  property("not") = Prop.forAll {i: Int ⇒ 
    not(0) apply i fold (
      x ⇒ (i ≟ 0) && (x ≟ loc.notAllowedMsg(i.toString).wrapNel),
      x ⇒ (i ≠ 0) && (x ≟ i)
    )
  }

  val intGen =
    Arbitrary.arbitrary[(Int, Int, Int)] suchThat { t ⇒ t._1 <= t._2 }

  property("interval") = Prop.forAll (intGen) { case (min, max, i) ⇒ 
    def fail = loc.notInIntervalMsg(min.toString, max.toString).wrapNel
    
    interval(min, max) apply i fold (
      x ⇒ (i < min || i > max) && (x ≟ fail),
      x ⇒ i >= min && i <= max && (x ≟ i)
    )
  }

  val strGen = Arbitrary.arbitrary[(Int, String)] suchThat { _._1 >= 0 }

  property("maxStringLength") = Prop.forAll (strGen) { case (i, s) ⇒ 
    maxStringLength (i)(s) fold (
      x ⇒ s.length > i && (x ≟ loc.maxStringLengthMsg(i).wrapNel),
      x ⇒ s.length <= i && (x ≟ s)
    )
  }

  property("uniqueName") = Prop.forAll { p: (Set[String],String) ⇒ 
    val (ns, n) = p

    uniqueName(ns).run(n).isLeft ≟ ns(n)
  }
    
} 

// vim: set ts=2 sw=2 et:
