package efa.core

import efa.core.std.anyVal._
import scalaz._, Scalaz._
import org.scalacheck._

object ReadTest
  extends Properties("Read")
  with ReadSpecs {

  property("readInt") = showRead[Int]

  property("readLong") = showRead[Long]

  property("readShort") = showRead[Short]

  property("readByte") = showRead[Byte]

  property("readFloat") = showRead[Float]

  property("readDouble") = showRead[Double]

  property("readBoolean") = showRead[Boolean]

  property("intReadFail") = readAll[Int]

  property("longReadFail") = readAll[Long]

  property("shortReadFail") = readAll[Short]

  property("byteReadFail") = readAll[Byte]

  property("floatReadFail") = readAll[Float]

  property("doubleReadFail") = readAll[Double]

  property("booleanReadFail") = readAll[Boolean]

  import TestLoc._

  property("localizedRead") = localizedRead[TestLoc]

  property("localizedReadFail") = readAll[TestLoc]
} 

// Stubs to test the properties of LocalizedParser

private[core] sealed trait TestLoc {
  def loc: Localization
}

private[core] object TestLoc {
  
  case object NameT extends TestLoc {
    val loc = Localization("name", "Name", "N", "a name")
  }

  case object ValueT extends TestLoc {
    val loc = Localization("value", "Value", "V", "a value")
  }
  
  lazy val values = List[TestLoc](NameT, ValueT)

  implicit lazy val TestLocLocalized: Localized[TestLoc] =
    new Localized[TestLoc]{def loc (l: TestLoc) = l.loc}

  implicit lazy val TestLocArbitrary: Arbitrary[TestLoc] =
    Arbitrary(Gen oneOf values)

  implicit lazy val TestLocEqual: Equal[TestLoc] =
    Equal.equalA[TestLoc]

  implicit lazy val TestLocRead: Read[TestLoc] = Read localized values
}

// vim: set ts=2 sw=2 et:
