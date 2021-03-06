package efa.core

import scalaz._, Scalaz._

/** Type class that allows the editing of a unique identifier
  * in an object of a given type.
  *
  * See UniqueId for a more thorough explanation about identifiers.
  */
trait UniqueIdL[A,I] extends UniqueId[A,I] { self ⇒

  /** `scalaz.Lens` for accessing and updating the identifier
    */
  def idL: A @> I

  def id(a: A) = idL get a

  def lensed[B](lens: B @> A): UniqueIdL[B,I] = new UniqueIdL[B,I] {
    def idL = lens >=> self.idL
  }

  /** Sets a unique identifier for each value in a container starting
    * at Monoid zero.
    */
  def generateIds[F[_]:Traverse]
    (fa: F[A])(implicit e: Enum[I], m: Monoid[I]): F[A] =
    fa traverseS { a ⇒ e succState { idL set (a, _) } } eval m.zero

  /** Sets a unique identifier for each value in a container starting
    * at Monoid zero.
    */
  def generateIdsMap[F[_]:Traverse]
    (fa: F[A])(implicit e: Enum[I], m: Monoid[I]): Map[I,A] =
    idMap(generateIds(fa))

  /** Creates a unique identifier from a collection of values and
    * sets it as the new identifier of the given value.
    *
    * See [[efa.core.UniqueId.newId]] for implementation details.
    */
  def setUniqueId[F[_]:Foldable]
    (fa: F[A], a: A)(implicit e: Enum[I], m: Monoid[I]): A =
    idL set (a, newId(fa))

}

object UniqueIdL extends UniqueIdLFunctions {
  @inline def apply[A,I](implicit U: UniqueIdL[A,I]): UniqueIdL[A,I] = U

  def xmap[A,B,I](f: A ⇒ B)(g: B ⇒ A)(implicit A: UniqueIdL[A,I]): UniqueIdL[B,I] =
    new UniqueIdL[B,I] {
      def idL: B @> I = A.idL.xmapA(f)(g)
    }

  def lensed[A,B,I](l: B @> A)(implicit A: UniqueIdL[A,I]): UniqueIdL[B,I] =
    A lensed l
}

trait UniqueIdLFunctions {
  def idL[A]: UniqueIdL[A, A] = lens(Lenses.L)

  def lens[A,I](l: A @> I): UniqueIdL[A,I] = new UniqueIdL[A,I]{ val idL = l }
}

// vim: set ts=2 sw=2 et:
