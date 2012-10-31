package efa.local.de

import org.scalacheck._, Prop._

object CoreLocalTest extends Properties("CoreLocal") {

  property("registered") = efa.core.loc.isInstanceOf[CoreLocal]

}

// vim: set ts=2 sw=2 et:
