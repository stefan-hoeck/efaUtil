package efa.io

import efa.core.{ToXml, Name}
import efa.core.std.anyVal._
import efa.core.typeclass._
import org.scalacheck.Arbitrary
import scalaz.Equal
import scalaz.std.anyVal._

case class TestClass(name: Name, int: Int, bool: Boolean)

object TestClass {
  implicit val equalInst: Equal[TestClass] = equal
  implicit val arbInst: Arbitrary[TestClass] = arbitrary
  implicit val toXmlInst: ToXml[TestClass] = ToXml.derive
}

// vim: set ts=2 sw=2 et:
