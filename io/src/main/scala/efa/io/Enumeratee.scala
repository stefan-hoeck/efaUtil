package efa.io

import efa.core.DisRes
import scala.collection.immutable.{Vector ⇒ IxSq}
import scalaz._, Scalaz._, std.vector._
import scalaz.iteratee._, Iteratee._
import StepT.{Cont, Done}

trait EnumerateeFunctions {

  /** A faster implementation of EnumerateeT.map */
  def mapper[O,I,F[_]:Monad](f: O ⇒ I): EnumerateeT[O,I,F] =
    new EnumerateeT[O,I,F] {
      type Stp[A] = StepT[I,F,A]
      type It[A,B] = IterateeT[A,F,B]

      def apply[A]: Stp[A] ⇒ It[O,Stp[A]] = _ match {
        case s@Done(a, i) ⇒ sdone[O,F,Stp[A]](s, emptyInput).pointI
        case Cont(g)      ⇒ scont[O,F,Stp[A]](io ⇒ 
          g(io map (f(_))) >>== apply[A]
        ).pointI
      }
    }

  def accumMap[E,F[_]:Monad,S](p: E ⇒ State[S,E])(s: S)
    : EnumerateeT[E, E, F] = new EnumerateeT[E, E, F] {
    def apply[A] = {
      def loop(s: S) = step(s) andThen cont[E, F, StepT[E, F, A]]
      def step(s: S): (Input[E] ⇒ IterateeT[E, F, A]) ⇒ 
                      (Input[E] ⇒ IterateeT[E, F, StepT[E, F, A]]) = {
        k ⇒ in ⇒
          in(
            el = e ⇒ {
              val (newS, newE) = p(e) apply s
              k(elInput(newE)) >>== doneOr(loop(newS))
            }
            , empty = cont(step(s)(k))
            , eof = done(scont(k), in)
          )
        }

      EnumerateeT.doneOr(loop(s))
    }
  }

  def parMap[O,I,F[_]:Monad](f: O ⇒ I): EnumerateeT[IxSq[O],IxSq[I],F] =
    mapper(_.par map f toVector)

  /** Map through a validator */
  def mapDis[O,I](f: O ⇒ DisRes[I]): EnumerateeT[O,I,LogDisIO] =
    new EnumerateeT[O,I,LogDisIO] {
      type Stp[A] = StepT[I,LogDisIO,A]
      type It[A,B] = IterateeT[A,LogDisIO,B]

      def apply[A]: Stp[A] ⇒ It[O,Stp[A]] = _ match {
        case s@Done(a, i) ⇒ sdone[O,LogDisIO,Stp[A]](s, emptyInput).pointI
        case Cont(g)      ⇒ scont[O,LogDisIO,Stp[A]](io ⇒ 
          io traverse f fold (logDisIO failNelIter _, g(_) >>== apply[A])
        ).pointI
      }
    }

  def reduceDis[O]: EnumerateeT[DisRes[O],O,LogDisIO] =
    new EnumerateeT[DisRes[O],O,LogDisIO] {
      type DR[A] = DisRes[A]
      type Stp[A] = StepT[O,LogDisIO,A]
      type It[A,B] = IterateeT[A,LogDisIO,B]

      def apply[A]: Stp[A] ⇒ It[DR[O],Stp[A]] = _ match {
        case s@Done(a, i) ⇒ sdone[DR[O],LogDisIO,Stp[A]](s, emptyInput).pointI
        case Cont(g)      ⇒ scont[DR[O],LogDisIO,Stp[A]](io ⇒ 
          io.sequence fold (logDisIO failNelIter _, g(_) >>== apply[A])
        ).pointI
      }
    }
}

object enumeratee extends EnumerateeFunctions

// vim: set ts=2 sw=2 et:
