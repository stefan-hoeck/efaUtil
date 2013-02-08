package efa.core

import scalaz._, Scalaz._
import shapeless.{HNil, HList, ::, LastAux, Last}

/** Type class that provides create, update, and delete operations
  * for deeply nested immutable data structures.
  *
  * When working with deeply nested data structures, some functions
  * working on a objects deep down in the tree might need some
  * information about the parent objects. For instance, when validating
  * an object's name for uniqueness one needs access to all the
  * other objects of the same type in the parent object. Typically
  * in immutable data structures, a child object does not have access to
  * a reference of the parent object.
  *
  * A convenient way to pass all parent objects down from the very
  * root alongside the child object is by using `HList`'s. So, instead
  * of passing just an object of type `Leaf`, we pass an `HList` of type
  * `Leaf :: Branch :: Root :: HNil` (probably using a
  * type alias). That way we not only have access to the whole object
  * path leading to the actual `Leaf`, we also have the means to update
  * the data tree when changing parts of the `Leaf` object.
  *
  * This is what type class `ParentL` is meant for. It provides
  * create, update, and delete functionality for deeply nested
  * immutable data trees using partial lenses for reading and
  * updating fields in the data structure.
  *
  * Two helper functions are provided to go further down the data tree
  * and create new implementation of `ParentL` out of existing ones:
  * Function `mapLensed` is used for child objects stored in a `Map`
  * while `mplusLensed` requires a `Foldable` `MonadPlus` instance
  * for the container type.
  *
  * @tparam F    The container type in which a parent object
  *              stores its children
  * @tparam P    The type of parent objects at the very root of the
  *              data tree
  * @tparam C    The type of child objects
  * @tparam Path Represents the type of the path leading from parent
  *              to child. This must be a subtype of `shapeless.HList`
  *              and start with the parent type `P`:
  *             `Other :: Parent :: Types :: P :: HNil`
  * @see `shapeless.HList`
  * @see `scalaz.PLens`
  */
trait ParentL[F[_],P,C,Path<:HList] extends Parent[F,Path,C] {

  /** The last type in `HList` `Path` must be type `P`
    */
  implicit protected def plast: LastAux[Path,P]

  /** The empty container
    */
  protected def empty: F[C]

  /** Partial lens from root to children
    */
  def childrenL(path: Path): P @?> F[C]

  /** Partial lens used to update a single child
    */
  def childL(c: C): F[C] @?> C

  /** Returns all children accessible via the given `Path`
    */
  final def children(path: Path): F[C] =
    childrenL(path).getOr[F[C]](path.last, empty)

  /** Partial lens to access and update a single
    * child given a parent `P`
    */
  final def fullChildL(cp: C :: Path): P @?> C =
    childrenL(cp.tail) >=> childL(cp.head)

  /** Adds a new child to a given parent
    */
  def add(c: C, path: Path): State[P,Unit]

  /** Adds a new child to a given parent by first creating and
    * setting a new unique identifier for the child
    */
  final def addUnique[Id:Enum:Monoid]
    (c: C, path: Path)
    (implicit u: UniqueIdL[C,Id]): State[P,Unit] =
    add(u.setUniqueId(children(path), c), path)

  /** Successfully adds a new child to a given parent
    */
  final def addV(c: C, path: Path): ValSt[P] = add(c, path).success

  /** Successfully adds a new child to a given parent by first creating and
    * setting a new unique identifier for the child
    */
  final def addUniqueV[Id:Enum:Monoid]
    (c: C, path: Path)
    (implicit u: UniqueIdL[C,Id]): ValSt[P] =
    addUnique[Id](c, path).success

  /** Removes a child from a parent
    */
  def delete(c: C :: Path): State[P,Unit]

  /** Successfully removes a child from a parent
    */
  final def deleteV(c: C :: Path): ValSt[P] = delete(c).success

  /** Updates (replaces) a child with a new one
    */
  def update(cp: C :: Path, c: C): State[P,Unit] =
    fullChildL(cp) %== { _ ⇒ c }

  /** Successfully updates (replaces) a child with a new one
    */
  final def updateV(cp: C :: Path, c: C): ValSt[P] = update(cp, c).success

  /** Creates a new `ParentL` from this instance, with the
    * same parent type and `C :: Path` as the path type.
    *
    * Children are stored in a `Map` from a unique identifier to
    * the actual data object.
    */
  def mapLensed[K,V](l: C @> Map[K,V])(implicit u: UniqueId[V,K])
    : ParentL[({type λ[α]=Map[K,α]})#λ,P,V,C :: Path] =
    ParentL map { fullChildL(_) >=> ~l }

  /** Creates a new `ParentL` from this instance, with the
    * same parent type and `C :: Path` as the path type.
    *
    * Children are stored in a container for which type classes
    * `Foldable` and `MonadPlus` must be provided.
    */
  def mplusLensed[G[_]:MonadPlus:Foldable,D:Equal](l: C @> G[D])
    :ParentL[G,P,D,C :: Path] =
    ParentL mplus { fullChildL(_) >=> ~l }
}

trait ParentLFunctions {
  def mapRoot[P,C,Id]
    (l: P @> Map[Id,C])
    (implicit u: UniqueId[C,Id])
    : ParentL[({type λ[α]=Map[Id,α]})#λ,P,C,P :: HNil] = map{ _ ⇒ ~l }

  def map[P,C,Path<:HList,Id]
    (l: Path ⇒ P @?> Map[Id,C])
    (implicit u: UniqueId[C,Id],
      last: LastAux[Path,P])
    : ParentL[({type λ[α]=Map[Id,α]})#λ,P,C,Path] =
    new MapParentL[P,C,Path,Id] {
      def childrenL(path: Path) = l(path)
    }

  def mplus[F[_]:MonadPlus:Foldable,P,C:Equal,Path<:HList]
    (l: Path ⇒ P @?> F[C])
    (implicit last: LastAux[Path,P]): ParentL[F,P,C,Path] =
    new MPlusParentL[F,P,C,Path] {
      def childrenL(path: Path) = l(path)
    }

  def mplusRoot[F[_]:MonadPlus:Foldable,P,C:Equal]
    (l: P @> F[C]): ParentL[F,P,C,P :: HNil] = mplus { _ ⇒ ~l }
}

object ParentL extends ParentLFunctions

private[core] abstract class MapParentL[P,C,Path<:HList,Id](
    implicit u: UniqueId[C,Id], last: LastAux[Path,P])
  extends ParentL[({type λ[α]=Map[Id,α]})#λ,P,C,Path] {
  protected val plast = last
  val T = Foldable[({type λ[α]=Map[Id,α]})#λ]
  protected def empty = Map.empty

  final def childL(c: C): Map[Id,C] @?> C = PLens mapVPLens u.id(c)

  final def add(c: C, path: Path): State[P,Unit] =
    childrenL(path) %== { _ + u.idPair(c) }

  final def delete(c: C :: Path): State[P,Unit] =
    childrenL(c.tail) %== { _ - u.id(c.head) }
}

private[core] abstract class MPlusParentL[F[_],P,C:Equal,Path<:HList](
    implicit last: LastAux[Path,P],
    m: MonadPlus[F],
    f: Foldable[F])
  extends ParentL[F,P,C,Path] {
  val T = f
  protected val plast = last
  protected def empty = m.empty

  def childL(c: C): F[C] @?> C = Lenses foldableLookup c

  def add(c: C, path: Path): State[P,Unit] =
    childrenL(path) %== { c.η[F] <+> _ }

  def delete(c: C :: Path): State[P,Unit] = 
    childrenL(c.tail) %== { _ filter { c.head ≠ _ } }
}

// vim: set ts=2 sw=2 et:
