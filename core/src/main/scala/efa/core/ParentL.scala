package efa.core

import scalaz._, Scalaz._
import shapeless.{HNil, HList, ::}

/**
  * Use cases
  *   Delete node:
  *   deleteP[C,P,F[_]](implicit ParentL[F,P,C]): NodeOut[C,ValSt[P]]
  *   
  *   The problem is with deeply nested objects. C must represent the full
  *   path, while P is only the head of the path
  */
trait ParentL[F[_],P,C,Path <: HList] {
  implicit def M: MonadPlus[F]

  def childrenL(path: Path): P @> F[C]

  def add(c: C, path: Path): ValSt[P] =
    childrenL(path) %== { c.η[F] <+> _ } success

  def delete(c: C :: Path)(implicit e: Equal[C]): ValSt[P] = 
    childrenL(c.tail) %== { _ filter { c.head ≠ _ } } success

  def update(cp: C :: Path, c: C)(implicit e: Equal[C]): ValSt[P] = {
    def adj(co: C) = (co ≟ cp.head) ? c | co

    childrenL(cp.tail) %== { _ map adj } success
  }
}

// vim: set ts=2 sw=2 et:
