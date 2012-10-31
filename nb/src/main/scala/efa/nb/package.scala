package efa

import efa.core.ValRes
import efa.nb.spi.NbLoc
import efa.react.SIn

package object nb {

  lazy val loc = efa.core.Service.unique[NbLoc] (NbLoc)

  type VSIn[+A] = SIn[ValRes[A]]
}

// vim: set ts=2 sw=2 et:
