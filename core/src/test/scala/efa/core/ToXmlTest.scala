package efa.core

import efa.core.Efa._
import efa.core.syntax.{nodeSeq, string}
import scalaz._, Scalaz._, scalacheck.ScalaCheckBinding._
import org.scalacheck._, Arbitrary.arbitrary
import scala.xml.Node

case class ToXmlCc(id: Int, name: String, desc: String)

object ToXmlCc {
  implicit val equalInst: Equal[ToXmlCc] = Equal.equalA
  implicit val defaultInst: Default[ToXmlCc] =
    Default default ToXmlCc(0,"","")
  implicit val arbInst: Arbitrary[ToXmlCc] = Arbitrary(
    ^^(Gen choose (0,100),
       Gen.identifier,
       Gen.identifier
      )(ToXmlCc.apply)
  )
  implicit val toXmlInst: ToXml[ToXmlCc] = new ToXml[ToXmlCc]{
    def toXml(tc: ToXmlCc) =
      ("id" xml tc.id) ++
      ("name" xml tc.name) ++
      ("desc" xml tc.desc)

    def fromXml(ns: Seq[Node]) = ^^(
      ns.readTag[Int]("id"),
      ns.readTag[String]("name"),
      ns.readTag[String]("desc")
    )(ToXmlCc.apply)
  }
}

object ToXmlTest extends Properties("ToXml") with ToXmlSpecs {
  property("stringXml") = laws[String]

  property("intXml") = laws[Int]

  property("longXml") = laws[Long]

  property("doubleXml") = laws[Double]

  property("booleanXml") = laws[Boolean]

  property("derive") = laws[ToXmlCc]

  implicit val toXmlListInt = ToXml.listToXml[Int]("anInt")

  property("listToXml") = laws[List[Int]]

  implicit val TestClassEqual = Equal.equalA[TestClass]

  implicit val TestClassToXml = new ToXml[TestClass] {
    def toXml (t: TestClass) = ("id" xml t.id) ++ ("name" xml t.name)
    def fromXml (ns: Seq[Node]) =
      ^(ns.readTag[Int]("id"), ns.readTag[String]("name")) (TestClass.apply)
  }

  property("tagNotFound") = Prop.forAll {s: String ⇒ 
    def res = (<tc><name>{s}</name></tc>).read[TestClass]
    res ≟ (loc tagNotFoundMsg "id" failureNel)
  }

  case class TestClass (id: Int, name: String)
}

// vim: set ts=2 sw=2 et:
