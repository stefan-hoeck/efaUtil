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
  def childrenL(path: Path): P @?> F[C]

  def childL(c: C): F[C] @?> C

  final def fullChildL(cp: C :: Path): P @?> C =
    childrenL(cp.tail) >=> childL(cp.head)

  def add(c: C, path: Path): ValSt[P]

  def delete(c: C :: Path): ValSt[P]

  def update(cp: C :: Path, c: C): ValSt[P] =
    fullChildL(cp) %== { _ ⇒ c } success

  def mapLensed[K,V](l: C @> Map[K,V])(implicit u: UniqueId[V,K])
    : ParentL[({type λ[α]=Map[K,α]})#λ,P,V,C :: Path] =
    ParentL map { fullChildL(_) >=> ~l }

  def mplusLensed[F[_]:MonadPlus:Foldable,D:Equal](l: C @> F[D])
    :ParentL[F,P,D,C :: Path] =
    ParentL mplus { fullChildL(_) >=> ~l }
}

trait ParentLFunctions {
  def mapRoot[P,C,Id]
    (l: P @> Map[Id,C])
    (implicit u: UniqueId[C,Id])
    : ParentL[({type λ[α]=Map[Id,α]})#λ,P,C,P :: HNil] = map{ _ ⇒ ~l }

  def map[P,C,Path<:HList,Id]
    (l: Path ⇒ P @?> Map[Id,C])
    (implicit u: UniqueId[C,Id])
    : ParentL[({type λ[α]=Map[Id,α]})#λ,P,C,Path] =
    new MapParentL[P,C,Path,Id] {
      def childrenL(path: Path) = l(path)
    }

  def mplus[F[_]:MonadPlus:Foldable,P,C:Equal,Path<:HList]
    (l: Path ⇒ P @?> F[C]): ParentL[F,P,C,Path] =
    new MPlusParentL[F,P,C,Path] {
      def childrenL(path: Path) = l(path)
    }

  def mplusRoot[F[_]:MonadPlus:Foldable,P,C:Equal]
    (l: P @> F[C]): ParentL[F,P,C,P :: HNil] = mplus { _ ⇒ ~l }
}

object ParentL extends ParentLFunctions

private[core] abstract class MapParentL[P,C,Path<:HList,Id](
    implicit u: UniqueId[C,Id])
  extends ParentL[({type λ[α]=Map[Id,α]})#λ,P,C,Path] {

  final def childL(c: C): Map[Id,C] @?> C = PLens mapVPLens u.id(c)

  final def add(c: C, path: Path): ValSt[P] =
    childrenL(path) %== { _ + u.idPair(c) } success

  final def delete(c: C :: Path): ValSt[P] =
    childrenL(c.tail) %== { _ - u.id(c.head) } success
}

abstract class MPlusParentL[F[_]:MonadPlus:Foldable,P,C:Equal,Path<:HList]
  extends ParentL[F,P,C,Path] {

  def childL(c: C): F[C] @?> C = Lenses foldableLookup c

  def add(c: C, path: Path): ValSt[P] =
    childrenL(path) %== { c.η[F] <+> _ } success

  def delete(c: C :: Path): ValSt[P] = 
    childrenL(c.tail) %== { _ filter { c.head ≠ _ } } success
}

// vim: set ts=2 sw=2 et:
