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

  type VLIOEnum[A] = EnumeratorT[A,ValLogIO]

  type VLIOStep[E,A] = StepT[E,ValLogIO,A]

  type VLIOIter[E,A] = IterateeT[E,ValLogIO,A]

  type DisIO[A] = EitherT[IO,Nel[String],A]

  type EnumIO[A] = EnumeratorT[A,DisIO]

  type IterIO[E,A] = IterateeT[E,DisIO,A]

  type StepIO[E,A] = StepT[E,DisIO,A]
}

// vim: set ts=2 sw=2 et:
