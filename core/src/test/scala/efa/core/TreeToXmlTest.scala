package efa.core

//import scalaz._, Scalaz._, scalacheck.ScalaCheckBinding._
//import org.scalacheck._, Prop._
//import scala.xml.Node
//
//object TreeToXmlTest
//   extends Properties ("TreeToXml")
//   with EfaPimps 
//   with ToXmlSpecs {
//  case class Cc (l: String)
//
//  val ccGen = Gen.identifier ∘ (Cc.apply)
//
//  implicit val CcEqual = equalA[Cc]
//
//  implicit val CcArbitrary = Arbitrary(ccGen)
//
//  implicit val CcToXml = new ToXml[Cc] {
//    def toXml (cc: Cc): Seq[Node] = <lbl>{cc.l}</lbl>
//    def fromXml (ns: Seq[Node]): ValRes[Cc] =
//      ns.readTag[String] ("lbl") map (Cc.apply)
//  }
//
//  property("ccToXml") = Prop forAll writeReadXml[Cc]
//
//  implicit val CcListToXml = listToXml[Cc] ("ccs")
//
//  property("ccListToXml") = Prop forAll writeReadXml[List[Cc]]
//
//  implicit val CcListTreeToXml = treeToXml[List[Cc]] ("folder")
//
//  lazy val leafGen: Gen[Tree[List[Cc]]] =
//    Gen.listOf[Cc](ccGen) ∘ (ccs ⇒ node(ccs, Stream.empty))
//
//  lazy val branchGen: Gen[Tree[List[Cc]]] =
//    Gen.listOf[Cc](ccGen) ⊛ treesGen apply ((l,ts) ⇒ node (l, ts))
//
//  lazy val treesGen: Gen[Stream[Tree[List[Cc]]]] =
//    Gen choose (0, 2) >>= (n ⇒ Gen.listOfN (n, treeGen) ∘ (_.toStream))
//
//  lazy val treeGen: Gen[Tree[List[Cc]]] =
//    Gen oneOf (leafGen, branchGen)
//
//  implicit val TreeArbitrary = Arbitrary (treeGen)
//
//  property("ccListTreeToXml") = Prop forAll writeReadXml[Tree[List[Cc]]]
//}

// vim: set ts=2 sw=2 et:
