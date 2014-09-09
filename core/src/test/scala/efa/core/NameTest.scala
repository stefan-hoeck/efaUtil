package efa.core

import org.scalacheck.Properties
import scalaz.scalacheck.ScalazProperties.{order ⇒ orderz, monoid ⇒ monoidz}

object NameTest extends Properties("Name") {
  include(Read.showLaws[Name])
  include(ToXml.laws[Name])
  include(orderz.laws[Name])
  include(monoidz.laws[Name])
}

// vim: set ts=2 sw=2 et:
