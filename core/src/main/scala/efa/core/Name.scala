package efa.core

import efa.core.std.string._
import org.scalacheck.{Arbitrary ⇒ Arb, Gen}
import scalaz.{Show, Order, Monoid}
import scalaz.std.string._

/** Represents a human readable name of a thing.
  *
  * Though this is not enforced at runtime or by the type system,
  * it is assumed that a Name consists of printable characters
  * with space and possibly tab being the only types of whitespace.
  * Name's Arbitrary instance returns only such instances of Names.
  *
  * Name's Show instance displays the string without being enclosed
  * in '"'s, which makes it suitable for file output and user interfaces.
  */
final case class Name(v: String) extends AnyVal {
  override def toString = v
}

object Name extends Function1[String,Name] {
  // Type Class instances
  implicit val showInst: Show[Name] = Show shows (_.v)
  implicit val orderInst: Order[Name] = order.contramap(_.v)
  implicit val monoidInst: Monoid[Name] = monoid.xmap(Name)(_.v)
  implicit val defaultInst: Default[Name] = Default default Name(loc.name)
  implicit val readInst: Read[Name] = Read map Name
  implicit val orderingInst: Ordering[Name] = orderInst.toScalaOrdering
  implicit val toXmlInst: ToXml[Name] = ToXml.readShow
  implicit val arbInst: Arb[Name] = 
    Arb(Gen.listOf(nameCharGen) map (cs ⇒ Name(cs.mkString)))

  private[core] def basicLatin     = Gen choose (0x0020 toChar, 0x007E toChar)
  private[core] def otherPrintable = Gen choose (0x00A0 toChar, 0xD7FF toChar)

  private def nameCharGen = Gen.frequency((10, basicLatin),(1, otherPrintable))
}

// vim: set ts=2 sw=2 et:
