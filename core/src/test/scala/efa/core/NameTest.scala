package efa.core

import org.scalacheck.Properties
import scalaz.scalacheck.ScalazProperties.{order ⇒ orderz, monoid ⇒ monoidz}

object NameTest extends Properties("Name") {
  property("enum laws") =  orderz.laws[Name]
  property("monoid laws") = monoidz.laws[Name]
}

// vim: set ts=2 sw=2 et:
