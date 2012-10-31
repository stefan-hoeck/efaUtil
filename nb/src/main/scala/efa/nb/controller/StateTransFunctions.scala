package efa.nb.controller

import efa.core.ValRes
import efa.io.LoggerIO
import efa.react._
import scalaz._, Scalaz._, effect.IO

trait StateTransFunctions {
  type St[A] = State[A,Unit]
  type ValSt[A] = ValRes[St[A]]
  type StTrans[A] = SET[A,ValSt[A]]

  def accum[A](st: ValSt[A], a: A): A = st fold (_ ⇒ a, _ exec a)

  def basicIn[A] (s: StTrans[A])(a: IO[A]): SIn[A] =
    sTrans.loop(toSST(s))(a)

  def basicLogIn[A] (s: StTrans[A], l: LoggerIO)(a: IO[A]): SIn[A] =
    basicIn(s --> l.logValRes)(a)

  def toSST[A] (s: StTrans[A]): SST[A,A] = eTrans.loopFold(s)(accum)

  def worldSST[A,B,W] (s: SET[B,ValSt[A]], wIn: SIn[W])
    (aaSST: SST[A,A], f: (A,W) ⇒ B): SST[A,A] = {
    def awSST: SST[A,W] = sTrans(_ ⇒ wIn run ())
    def abSST: SST[A,B] = aaSST ⊛ awSST apply f

    toSST(abSST >=> s)
  }

  def worldIn[A,B,W] (s: SET[B,ValSt[A]], wIn: SIn[W])
    (aaSST: SST[A,A], f: (A,W) ⇒ B, a: IO[A]): SIn[A] =
    sTrans.loop (worldSST(s, wIn)(aaSST, f))(a)
}

object StateTrans extends StateTransFunctions

// vim: set ts=2 sw=2 et:
