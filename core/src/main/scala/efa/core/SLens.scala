package efa.core

import shapeless._, HList._
import scalaz._, Scalaz._, scalaz.{Lens ⇒ Lensz}

trait SLens[A] {

  def at[L <: HList, N <: Nat](n : N)(
    implicit iso : Iso[A, L], lens : NthLens[L, N]
  ): A @> lens.Elem =
    Lensz.lensu[A, lens.Elem] (
      (a, e) ⇒ iso from (lens set(iso to a, e)),
      a ⇒ lens get (iso to a)
    )
}

object SLens {
  def apply[A]: SLens[A] = new SLens[A]{}
}

trait NthLens[L,N] {
  type Elem
  def get (l: L): Elem
  def set (l: L, e: Elem): L
}

object NthLens {
  implicit def nthLens[L <: HList, N <: Nat, E](
    implicit la: NthLensAux[L, N, E]
  ) = new NthLens[L, N] {
    type Elem = E
    def get(l : L) : Elem = la get l
    def set(l : L, e : Elem) : L = la set (l, e)
  }
}

trait NthLensAux[L <: HList, N <: Nat, E] {
  def get (l: L): E
  def set (l: L, e: E): L
}

object NthLensAux {
  implicit def nthLens[L <: HList, N <: Nat, E](
    implicit atx : AtAux[L, N, E], replace : ReplaceAtAux[L, N, E, E, L]
  ) = new NthLensAux[L, N, E] {
    def get(l: L) : E = l[N] 
    def set(l: L, e: E) : L = l.updatedAt[N](e)
  }
}

// vim: set ts=2 sw=2 et:
