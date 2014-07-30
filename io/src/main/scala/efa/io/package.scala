package efa

import efa.core.{Service, DisRes, Logs, Nel}
import efa.io.spi.IOLoc
import scalaz._, Scalaz._, effect._, iteratee._

package object io {

  lazy val loc = Service.unique[IOLoc](IOLoc)

  type DisIO[A] = EitherT[IO,Nel[String],A]

  type LogDisIO[A] = Kleisli[DisIO,LoggerIO,A]

  type EnumIO[A] = EnumeratorT[A,LogDisIO]

  type IterIO[E,A] = IterateeT[E,LogDisIO,A]

  type StepIO[E,A] = StepT[E,LogDisIO,A]

  type ClassResource = (String,Class[_])

  def opened(s: String): String = loc opened s

  def readError(s: String)(t: Throwable): String = loc readError (s, t)

  def openError(s: String)(t: Throwable): String = loc openError (s, t)
}

// vim: set ts=2 sw=2 et:
