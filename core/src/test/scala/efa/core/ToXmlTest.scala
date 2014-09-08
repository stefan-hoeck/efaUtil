package efa.core

import efa.core.Efa._
import efa.core.syntax.{nodeSeq, string}
import scalaz._, Scalaz._, scalacheck.ScalaCheckBinding._
import org.scalacheck._, Arbitrary.arbitrary
import scala.xml.Node
import shapeless.contrib.scalaz.instances.{EqualI, deriveEqual}
import shapeless.contrib.scalacheck.ArbitraryI
import shapeless.contrib.scalacheck

case class ToXmlCc(id: Id, name: Name, desc: Desc)

object ToXmlCc {
  implicit val equalInst: Equal[ToXmlCc] = deriveEqual
  implicit val defaultInst: Default[ToXmlCc] = Default.derive
  implicit val arbInst: Arbitrary[ToXmlCc] = scalacheck.deriveArbitrary
  implicit val toXmlInst: ToXml[ToXmlCc] = ToXml.derive
}

object ToXmlTest extends Properties("ToXml") with ToXmlSpecs {
  property("stringXml") = writeReadXml[String]

  property("intXml") = writeReadXml[Int]

  property("longXml") = writeReadXml[Long]

  property("doubleXml") = writeReadXml[Double]

  property("booleanXml") = writeReadXml[Boolean]

  property("derive") = writeReadXml[ToXmlCc]

  implicit val toXmlListInt = ToXml.listToXml[Int]("anInt")

  property("listToXml") = writeReadXml[List[Int]]

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
