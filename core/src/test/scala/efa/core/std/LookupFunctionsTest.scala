package efa.core

//import scalaz._, Scalaz._, effects._
//import org.scalacheck._
//import org.openide.util.Lookup
//import org.openide.util.lookup.{InstanceContent, AbstractLookup}
//
//object LookupPimpTest extends Properties("LookupPimp") with LookupPimps {
//  def lkp(l: List[AnyRef]): Lookup = {
//    val ic = new InstanceContent
//    l foreach ic.add
//    new AbstractLookup(ic)
//  }
//
//  case class TestClass(s: String)
//  implicit val TestClassEquals = equalA[TestClass] 
//
//  val tcGen = for {s <- Arbitrary.arbitrary[String]} yield TestClass(s)
//
//  val listGen = Gen.containerOf[List, TestClass](tcGen) suchThat { l ⇒
//    l.size ≟ l.toSet.size }
//
//  property("lkpAll") = Prop.forAll(listGen) { l ⇒
//    val res = for {
//      ls ← lkp(l).all[TestClass]
//    } yield (ls ≟ l.toSeq)
//
//    res.unsafePerformIO
//  }
//  
//  property("lkp") = Prop.forAll(listGen) { l ⇒
//    lkp(l).head[TestClass] map (_ ≟ l.headOption) unsafePerformIO
//  }
//}

// vim: set ts=2 sw=2 et:
