package efa.nb.node

import efa.core.{ValRes, EndoVal}
import scalaz._, Scalaz._

trait StNodeFunctions {

  def sg[A,B,C,D](o: ValOut[A,B])(set: (A,B) ⇒ C)(get: D ⇒ A)
    : ValOut[D,C] =
  o.withIn ((a: A,vb: ValRes[B]) ⇒ vb map (set(a,_))) contramap get

  def lens[A,B] (o: ValStOut[A,A])(l: B @> A): ValStOut[B,B] =
    mapSt[B,A,B] (o ∙ l.get)(l)

  def mapSt[A,B,C] (o: ValStOut[A,B])(l: C @> B): ValStOut[A,C] = {
    def st (s: State[B,Unit]): State[C,Unit] =
      init[C] >>= (c ⇒ l := s.exec (l get c) void)

    o map (_ map st)
  }

  def valFromInput[A,B] (o: ValOut[A,B])(v: A ⇒ EndoVal[B])
    : ValOut[A,B] = o withIn ((a,vb) ⇒ vb flatMap (v(a) run _ validation))

}

// vim: set ts=2 sw=2 et:
