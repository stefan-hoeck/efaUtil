package efa.core

import Default.!!!
import org.scalacheck.Properties
import scalaz.Equal
import scalaz.syntax.equal._

case class DefaultCc(id: Int, name: String)

object DefaultCc {
  implicit val equalInst: Equal[DefaultCc] = Equal.equalA
  implicit val defaultInst: Default[DefaultCc] = 
    Default default DefaultCc(0, "")
}

object DefaultTest extends Properties("Default") {
  property("derive") = !!![DefaultCc] ≟ DefaultCc(0,"")
}

// vim: set ts=2 sw=2 et:
