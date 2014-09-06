package efa.core

import org.scalacheck.Properties
import scalaz.scalacheck.ScalazProperties.{order ⇒ orderz, monoid ⇒ monoidz}

object DescTest extends Properties("Desc") {
  property("enum laws") =  orderz.laws[Desc]
  property("monoid laws") = monoidz.laws[Desc]
}

// vim: set ts=2 sw=2 et:

