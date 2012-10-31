package efa.core

import scalaz.Contravariant

trait Localized[-A] {
  def loc(a: A): Localization
  def locName(a: A): String = loc(a).locName
  def shortName(a: A): String = loc(a).shortName
  def desc(a: A): String = loc(a).desc
  def names(a: A): List[String] = loc(a).names
}

trait IsLocalized {
  def loc: Localization
  def locName = loc.locName
  def desc = loc.desc
  def shortName = loc.shortName

  override def toString = locName
}

object Localized {
  @inline def apply[A:Localized]: Localized[A] = implicitly

  def get[A](f: A ⇒ Localization): Localized[A] = new Localized[A] {
    def loc (a: A) = f(a)
  }

  implicit val LocalizedContravariant = new Contravariant[Localized] {
    def contramap[A,B](l: Localized[A])(f: B ⇒ A) = get(f andThen l.loc)
  }
}

// vim: set ts=2 sw=2 et:
