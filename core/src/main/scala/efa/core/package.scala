package efa

import scalaz.{Kleisli, \/, ValidationNEL, DList, NonEmptyList, State}
import efa.core.spi.UtilLoc

package object core {

  lazy val loc = Service.unique[UtilLoc](UtilLoc)

  type Nel[+A] = NonEmptyList[A]

  type Logs = DList[Log]

  type DisRes[+A] = NonEmptyList[String] \/ A

  type ValRes[+A] = ValidationNEL[String,A]

  type Validator[-R,+A] = Kleisli[DisRes,R,A]

  type ValSt[A] = ValRes[State[A,Unit]]
  
  type EndoVal[A] = Validator[A,A]
}

// vim: set ts=2 sw=2 et:
