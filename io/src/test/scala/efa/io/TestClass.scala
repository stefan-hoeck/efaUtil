package efa.io

import efa.core._, efa.core.Efa._
import efa.core.syntax._
import org.scalacheck._, Arbitrary.arbitrary
import scala.xml.Node
import scalaz.Equal
import scalaz.{Scalaz, Equal}, Scalaz._, scalaz.scalacheck.ScalaCheckBinding._

case class TestClass(name: Name, int: Int, bool: Boolean)

object TestClass {
  implicit val equalInst: Equal[TestClass] = Equal.equalA
  implicit val arbInst: Arbitrary[TestClass] = Arbitrary(
    ^^(arbitrary[Name], 
       arbitrary[Int],
       arbitrary[Boolean]
      )(TestClass.apply)
  )
  implicit val toXmlInst: ToXml[TestClass] = new ToXml[TestClass] {
    def toXml(tc: TestClass) =
      ("name" xml tc.name) ++
      ("int" xml tc.int) ++
      ("bool" xml tc.bool)

    def fromXml(ns: Seq[Node]) =
      ^^(ns.readTag[Name]("name"),
         ns.readTag[Int]("int"),
         ns.readTag[Boolean]("bool")
        )(TestClass.apply)
  }
}

// vim: set ts=2 sw=2 et:
