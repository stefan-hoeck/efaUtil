package efa.core

import scalaz.Contravariant

/** A type class that associates some [[efa.core.Localization]]s with objects
  * of a given type.
  */
trait Localized[-A] { self ⇒
  def loc(a: A): Localization

  def locName(a: A): String = loc(a).locName

  def shortName(a: A): String = loc(a).shortName

  def desc(a: A): String = loc(a).desc

  def names(a: A): List[String] = loc(a).names

  def contramap[B](f: B ⇒ A): Localized[B] = new Localized[B] {
    def loc(b: B) = self loc f(b)
  }

  def ∙ [B](f: B ⇒ A): Localized[B] = contramap(f)
}

/** A helper-trait for code reuse in classes that have a
  * field loc of type [[efa.core.Localization]].
  *
  * Just mix-in this trait to conveniently get direct access to
  * `locName`, `desc`, and `shortName`.
  */
trait IsLocalized {
  def loc: Localization
  def locName = loc.locName
  def desc = loc.desc
  def shortName = loc.shortName

  override def toString = locName
}

object Localized {
  @inline def apply[A:Localized]: Localized[A] = implicitly

  /** Factory method to conveniently create a new [[efa.core.Localized]]
    * instance.
    */
  def get[A](f: A ⇒ Localization): Localized[A] = new Localized[A] {
    def loc (a: A) = f(a)
  }

  @deprecated("Not sure, whether type class instances of type classes are" +
               "the way to go.", "0.2.0-SNAPSHOT")
  implicit val LocalizedContravariant = new Contravariant[Localized] {
    def contramap[A,B](l: Localized[A])(f: B ⇒ A) = get(f andThen l.loc)
  }
}

// vim: set ts=2 sw=2 et:
