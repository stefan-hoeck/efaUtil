package efa.core

import org.scalacheck.Properties
import scalaz.scalacheck.ScalazProperties.{order ⇒ orderz, monoid ⇒ monoidz}

object DescTest extends Properties("Desc") {
  include(Read.showLaws[Desc])
  include(ToXml.laws[Desc])
  include(orderz.laws[Desc])
  include(monoidz.laws[Desc])
}

// vim: set ts=2 sw=2 et:

