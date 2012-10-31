package efa.io

//import scalaz._, Scalaz._, effects._
//
//trait StateIOs {
//
//  def initIO[S]: StateIO[S, S] = stateT[IO,S, S](s ⇒ io(s, s))
//
//  def modifyIO[S](f: S ⇒ S) = initIO[S] flatMap (s ⇒ stateT(_ ⇒ io(f(s), ())))
//
//  implicit def StateIOMonad[S]: Monad[({type λ[α]=StateIO[S,α]})#λ] =
//    Monad.StateTMonad
//
//  def putIO[S](s: S): StateIO[S,Unit] = stateT(_ ⇒ io(s, ()))
//
//}

// vim: set ts=2 sw=2 et:
