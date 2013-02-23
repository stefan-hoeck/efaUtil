package efa.nb.tc.install

import efa.nb.tc.EfaTc
import scalaz._, Scalaz._

class Installer extends efa.nb.ActivatorIO {
  override protected def stopIO = EfaTc.registry foldMap (_.persist)
}

// vim: set ts=2 sw=2 et:
