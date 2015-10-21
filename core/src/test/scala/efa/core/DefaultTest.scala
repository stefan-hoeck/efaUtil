package efa.core

import Default.!!!
import org.scalacheck.Properties
import scalaz.Equal
import scalaz.syntax.equal._

case class DefaultCc(id: Id, name: Name)

object DefaultCc {
  implicit val equalInst: Equal[DefaultCc] = Equal.equalA
  implicit val defaultInst: Default[DefaultCc] = 
    Default default DefaultCc(!!![Id],!!![Name])
}

object DefaultTest extends Properties("Default") {
  property("derive") = !!![DefaultCc] ≟ DefaultCc(!!![Id], !!![Name])
}

// vim: set ts=2 sw=2 et:
