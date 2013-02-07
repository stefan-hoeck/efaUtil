package efa.core

import scalaz._, Scalaz._

/** Represents a parent-child relationship between two types.
  *
  * This type class not only abstracts over the parent and child types
  * but also over the type of container in which children are stored.
  * There must be a implementation of [[scalaz.Foldable]]
  * defined for the container type.
  *
  * This type class has its uses in UI programming, especially when
  * displaying nested data structures in tree-like views.
  *
  * @tparam P The parent type
  * @tparam C The child type
  * @tparam F The container type in which objects of type P store
  *           their children
  */
trait Parent[F[_],-P,C] {
  implicit def T: Foldable[F]

  /** Returns all children associated with a parent
    */
  def children(p: P): F[C]

  /** Returns all children associated with a parent as a `List`
    */
  def childrenList(p: P): List[C] = children(p).toList

  /** Returns a list of children sorted by name
    */
  def sortedChildren(p: P)(implicit n: Named[C]): List[C] =
    n nameSortF children(p)

  /** Returns a list of children sorted by name together with their
    * unique identifier.
    */
  def sortedUniqueChildren[Id]
    (p: P)
    (implicit n: Named[C], u: UniqueId[C,Id]): List[(Id,C)] =
    u pairs sortedChildren(p)

  def contramap[A] (get: A ⇒ P): Parent[F,A,C] = new Parent[F,A,C] {
    val T = Parent.this.T
    def children(a: A) = Parent.this children get(a)
  }
}

trait ParentFunctions {
  def parent[F[+_]:Foldable,P,C](get: P ⇒ F[C]): Parent[F,P,C] =
    new Parent[F,P,C] {
      val T = Foldable[F]
      def children (p: P) = get(p)
    }
}

object Parent extends ParentFunctions

// vim: set ts=2 sw=2 et:
