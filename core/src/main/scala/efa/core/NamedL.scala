package efa.core

import scalaz.{@>, InvariantFunctor}

/** Type class that associates an editable (and typically localized)
  * name with objects of a type.
  *
  * The name field of an object can be updated via lens nameL.
  *
  * @see [[scalaz.Lens]]
  */
trait NamedL[A] extends Named[A] {
  def nameL: A @> String

  final override def name(a: A): String = nameL get a
}

object NamedL {
  def apply[A:NamedL]: NamedL[A] = implicitly

  def xmap[A,B](n: NamedL[A], f: A ⇒ B, g: B ⇒ A): NamedL[B] =
    new NamedL[B] {
      def nameL: B @> String = n.nameL.xmapA(f)(g)
    }

  def lensed[A,B](n: NamedL[A])(l: B @> A): NamedL[B] = new NamedL[B] {
    val nameL = l >=> n.nameL
  }

  implicit val NamedLInvariant: InvariantFunctor[NamedL] =
    new InvariantFunctor[NamedL] {
      def xmap[A,B](ma: NamedL[A], f: A ⇒ B, g: B ⇒ A) =
        NamedL.xmap(ma, f, g)
    }
}

// vim: set ts=2 sw=2 et:
