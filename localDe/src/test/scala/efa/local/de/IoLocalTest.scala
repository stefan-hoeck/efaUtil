package efa.local.de

import org.scalacheck._, Prop._

object IoLocalTest extends Properties("IoLocal") {

  property("registered") = efa.io.loc.isInstanceOf[IoLocal]

}

// vim: set ts=2 sw=2 et:
