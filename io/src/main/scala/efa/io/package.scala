package efa

import efa.core.{Service, DisRes, Logs}
import efa.io.spi.IOLoc
import scalaz._, Scalaz._, effect._

package object io {

  lazy val loc = Service.unique[IOLoc](IOLoc)

  type LogIO[+A] = WriterT[IO,Logs,A]
  type ValLogIO[+A] = EitherT[LogIO,NonEmptyList[String],A]
  type LogIOR[+A] = IO[(Logs,A)]
  type ValLogIOR[+A] = LogIOR[DisRes[A]]

  val ValLogIOMonad = Monad[ValLogIO]
//  type StateIO[S,A] = StateT[IO,S,A]
//
//  implicit def StateIOMonad[S] =
//     implicitly[Monad[({type λ[α]=StateIO[S,α]})#λ]]

  def resource[A](cl: A ⇒ Unit): Resource[A] = new Resource[A] {
    def close(a: A) = IO(cl(a))
  }
}

// vim: set ts=2 sw=2 et:
