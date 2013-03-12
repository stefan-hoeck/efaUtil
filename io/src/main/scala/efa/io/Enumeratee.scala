package efa.io

import efa.core.DisRes
import scala.collection.immutable.{IndexedSeq ⇒ IxSq}
import scalaz._, Scalaz._, std.indexedSeq._
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
}

object enumeratee extends EnumerateeFunctions

// vim: set ts=2 sw=2 et:
