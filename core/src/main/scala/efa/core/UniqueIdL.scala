package efa.core

import scalaz._, Scalaz._

/** Type class that allows the editing of a unique identifier
  * in an object of a given type.
  *
  * See UniqueId for a more thorough explanation about identifiers.
  */
trait UniqueIdL[A,I] extends UniqueId[A,I] {

  /** `scalaz.Lens` for accessing and updating the identifier
    */
  def idL: A @> I

  def id (a: A) = idL get a

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
}

trait UniqueIdLFunctions {

  def intIdL[A] (l: A @> Int): IntIdL[A] = lens (l)

  def longIdL[A] (l: A @> Long): LongIdL[A] = lens (l)

  def stringIdL[A] (l: A @> String): StringIdL[A] = lens (l)

  def idL[A]: UniqueIdL[A, A] = lens (Lens.self)

  def lens[A,I] (l: A @> I): UniqueIdL[A,I] = new UniqueIdL[A,I] {
    val idL = l
  }
}

// vim: set ts=2 sw=2 et:
