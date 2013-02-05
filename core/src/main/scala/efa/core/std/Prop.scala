package efa.core.std

import efa.core.ValRes
import scalaz.{Equal, Pointed, Monoid}
import scalaz.syntax.equal._
import org.scalacheck.Prop

trait PropFunctions {

  //@TODO: Some macros might be of interest here
  def compare[A:Equal](e: A, f: A): Prop =
    Prop.propBoolean(f ≟ e) :| "Expected: %s, but found %s".format(e, f)

  def compareP[F[_],A](e: A, f: F[A])(implicit P:Pointed[F], E:Equal[F[A]])
    : Prop = compare(P point e, f)
}

trait PropInstances {
  implicit val PropMonoid: Monoid[Prop] = new Monoid[Prop] {
    val zero = Prop propBoolean true
    def append (a: Prop, b: ⇒ Prop): Prop = a && b
  }
}

object prop extends PropFunctions with PropInstances

// vim: set ts=2 sw=2 et:
