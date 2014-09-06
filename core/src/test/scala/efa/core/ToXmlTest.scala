package efa.core

import Efa._
import efa.core.syntax.{nodeSeq, string}
import scalaz._, Scalaz._, scalacheck.ScalaCheckBinding._
import org.scalacheck._, Arbitrary.arbitrary
import scala.xml.Node

case class ToXmlCc(id: Id, name: Name, desc: Desc)

object ToXmlCc {
  implicit val equalInst: Equal[ToXmlCc] = deriveEqual
  implicit val defaultInst: Default[ToXmlCc] = Default.derive
  implicit val arbInst: Arbitrary[ToXmlCc] = deriveArbitrary
  implicit val toXmlInst: ToXml[ToXmlCc] = ToXml.derive
}

object ToXmlTest extends Properties("ToXml") with ToXmlSpecs {
  property("stringXml") = Prop forAll writeReadXml[String]

  property("intXml") = Prop forAll writeReadXml[Int]

  property("longXml") = Prop forAll writeReadXml[Long]

  property("doubleXml") = Prop forAll writeReadXml[Double]

  property("booleanXml") = Prop forAll writeReadXml[Boolean]

  property("derive") = Prop forAll writeReadXml[ToXmlCc]

  implicit val toXmlListInt = ToXml.listToXml[Int]("anInt")

  property("listToXml") =
    Prop.forAll(Gen listOf arbitrary[Int])(writeReadXml)

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
