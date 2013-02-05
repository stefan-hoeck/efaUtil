package efa.core

import scalaz._, Scalaz._

trait Parent[F[_],-A,B] {
  implicit def T: Traverse[F]
  def children(a: A): F[B]
  def childrenList(a: A): List[B] = children(a).toList

  def sortedChildren(a: A)(implicit n: Named[B]): List[B] =
    n nameSortF children(a)

  def sortedUniqueChildren[Id]
    (a: A)
    (implicit n: Named[B], u: UniqueId[B,Id]): List[(Id,B)] =
    u pairs sortedChildren(a)

  def contramap[C] (get: C ⇒ A): Parent[F,C,B] = new Parent[F,C,B] {
    val T = Parent.this.T
    def children(c: C) = Parent.this children get(c)
  }
}

trait ParentFunctions {
  def parent[F[_]:Traverse,A,B](get: A ⇒ F[B]): Parent[F,A,B] =
    new Parent[F,A,B] {
      val T = implicitly[Traverse[F]]
      def children (a: A) = get(a)
    }
}

object Parent extends ParentFunctions {
}

// vim: set ts=2 sw=2 et:
