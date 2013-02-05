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

  //Aliases for some of the most common UniqueId types

  type IntId[A] = UniqueId[A,Int]

  type IntIdL[A] = UniqueIdL[A,Int]

  type LongId[A] = UniqueId[A,Long]

  type LongIdL[A] = UniqueIdL[A,Long]

  type StringId[A] = UniqueId[A,String]

  type StringIdL[A] = UniqueIdL[A,String]

  def IntId[A:IntId]: IntId[A] = implicitly

  def LongId[A:LongId]: LongId[A] = implicitly

  def StringId[A:StringId]: StringId[A] = implicitly

  def IntIdL[A:IntIdL]: IntIdL[A] = implicitly

  def LongIdL[A:LongIdL]: LongIdL[A] = implicitly

  def StringIdL[A:StringIdL]: StringIdL[A] = implicitly
}

// vim: set ts=2 sw=2 et:
