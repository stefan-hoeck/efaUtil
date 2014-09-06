package efa.core

import org.scalacheck.Properties
import scalaz.scalacheck.ScalazProperties.{enum ⇒ enumz, monoid ⇒ monoidz}

object IdTest extends Properties("Id") {
  property("enum laws") =  enumz.laws[Id]
  property("monoid laws") = monoidz.laws[Id]
}

// vim: set ts=2 sw=2 et:
