package efa.io

import scalaz._, Scalaz._, scalacheck.ScalaCheckBinding._
import efa.core._, Efa._
import efa.core.syntax._
import org.scalacheck._
import Arbitrary._

case class TestClass(str: String, int: Int, bool: Boolean)

object TestClass {
  implicit val TestClassEqual: Equal[TestClass] = Equal.equalA

  implicit val TestClassArbitrary = Arbitrary(
    ^^(Gen.identifier, arbitrary[Int], arbitrary[Boolean]) (TestClass.apply)
  )

  implicit val TestClassToXml: ToXml[TestClass] = new ToXml[TestClass] {
  
    def toXml (t: TestClass): Seq[scala.xml.Node] =
      ("str" xml t.str) ++
      ("int" xml t.int) ++
      ("bool" xml t.bool)

    def fromXml (ns: Seq[scala.xml.Node]): ValRes[TestClass] = ^^(
      ns.readTag[String]("str"),
      ns.readTag[Int]("int"),
      ns.readTag[Boolean]("bool")
    )(TestClass.apply)
  }
}

// vim: set ts=2 sw=2 et:
