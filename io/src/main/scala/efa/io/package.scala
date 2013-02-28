package efa

import efa.core.{Service, DisRes, Logs, Nel}
import efa.io.spi.IOLoc
import scalaz._, Scalaz._, effect._, iteratee._

package object io {

  lazy val loc = Service.unique[IOLoc](IOLoc)

  type LogIO[+A] = WriterT[IO,Logs,A]

  type ValLogIO[+A] = EitherT[LogIO,Nel[String],A]

  type LogIOR[+A] = IO[(Logs,A)]

  type ValLogIOR[+A] = LogIOR[DisRes[A]]

  type DisIO[+A] = EitherT[IO,Nel[String],A]

  type LogToDisIO[A] = Kleisli[DisIO,LoggerIO,A]

  type EnumIO[A] = EnumeratorT[A,LogToDisIO]

  type IterIO[E,A] = IterateeT[E,LogToDisIO,A]

  type StepIO[E,A] = StepT[E,LogToDisIO,A]
}

// vim: set ts=2 sw=2 et:
