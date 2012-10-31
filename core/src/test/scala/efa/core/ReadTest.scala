package efa.core

import efa.core.std.anyVal._
import scalaz._, Scalaz._
import org.scalacheck._

object ReadTest
  extends Properties("Read")
  with ReadSpecs {

  property("readInt") = Prop forAll showRead[Int]

  property("readLong") = Prop forAll showRead[Long]

  property("readShort") = Prop forAll showRead[Short]

  property("readByte") = Prop forAll showRead[Byte]

  property("readFloat") = Prop forAll showRead[Float]

  property("readDouble") = Prop forAll showRead[Double]

  property("readBoolean") = Prop forAll showRead[Boolean]

  property("intReadFail") = Prop forAll readAll[Int]

  property("longReadFail") = Prop forAll readAll[Long]

  property("shortReadFail") = Prop forAll readAll[Short]

  property("byteReadFail") = Prop forAll readAll[Byte]

  property("floatReadFail") = Prop forAll readAll[Float]

  property("doubleReadFail") = Prop forAll readAll[Double]

  property("booleanReadFail") = Prop forAll readAll[Boolean]

  import TestLoc._

  property("localizedRead") = Prop forAll localizedRead[TestLoc]

  property("localizedReadFail") = Prop forAll readAll[TestLoc]
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
