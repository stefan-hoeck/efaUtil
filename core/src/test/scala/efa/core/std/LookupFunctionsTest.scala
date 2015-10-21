package efa.core.std

import scalaz._, Scalaz._, effect._
import org.scalacheck._
import org.openide.util.Lookup
import org.openide.util.lookup.{InstanceContent, AbstractLookup}

case class TestClass(s: String)

object LookupFunctionsTest
  extends Properties("LookupFunctions")
  with LookupFunctions {

  def lkp(l: List[AnyRef]): Lookup = {
    val ic = new InstanceContent
    l foreach ic.add
    new AbstractLookup(ic)
  }

  implicit val TestClassEquals = Equal.equalA[TestClass] 

  val tcGen = for {s <- Arbitrary.arbitrary[String]} yield TestClass(s)

  val listGen = Gen.containerOf[List, TestClass](tcGen) suchThat { l ⇒
    l.size ≟ l.toSet.size }

  property("lkpAll") = Prop.forAll(listGen) { l ⇒
    val res = for {
      ls ← all[TestClass](lkp(l))
    } yield (ls.toList ≟ l)

    res.unsafePerformIO
  }
  
  property("lkp") = Prop.forAll(listGen) { l ⇒
    head[TestClass](lkp(l)) map (_ ≟ l.headOption) unsafePerformIO
  }
}

// vim: set ts=2 sw=2 et:
