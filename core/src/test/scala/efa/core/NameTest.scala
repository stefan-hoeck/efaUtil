package efa.core

import org.scalacheck.Properties
import scalaz.scalacheck.ScalazProperties.{order ⇒ orderz, monoid ⇒ monoidz}

object NameTest extends Properties("Name") {
  property("readShow") = ReadSpecs.showRead[Name]
  property("toStringRead") = ReadSpecs.toStringRead[Name]
  property("readAll") = ReadSpecs.readAll[Name]
  property("toXml") = ToXmlSpecs.writeReadXml[Name]
  property("enum laws") =  orderz.laws[Name]
  property("monoid laws") = monoidz.laws[Name]
}

// vim: set ts=2 sw=2 et:
