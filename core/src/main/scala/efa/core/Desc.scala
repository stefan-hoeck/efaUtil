package efa.core

import efa.core.Name.{basicLatin, otherPrintable}
import efa.core.std.string._
import org.scalacheck.{Arbitrary ⇒ Arb, Gen}
import scalaz.{Show, Order, Monoid}
import scalaz.std.string._

/** Represents a human readable description of a thing.
  *
  * Unlike Names, descriptions are typically longer and can contain
  * additional whitespace like tabs, line feeds and carriage returns.
  * These characters are included in Desc's Arbitrary instance.
  *
  * Desc's Show instance displays the string without being enclosed
  * in '"'s, which makes it suitable for file output and user interfaces.
  */
final case class Desc(v: String) extends AnyVal {
  override def toString = v
}

object Desc extends Function1[String,Desc] {
  implicit val showInst: Show[Desc] = Show shows (_.v)
  implicit val orderInst: Order[Desc] = order.contramap(_.v)
  implicit val monoidInst: Monoid[Desc] = monoid.xmap(Desc)(_.v)
  implicit val defaultInst: Default[Desc] = Default default Desc("")
  implicit val readInst: Read[Desc] = Read map Desc

  private val whiteSpace = Gen oneOf (
    0x0009 toChar, //tab
    0x000A toChar, //line feed
    0x000D toChar //carriage return
  )

  private val descCharGen = Gen.frequency(
    (10, basicLatin),
    (2, otherPrintable),
    (1, whiteSpace)
  )

  implicit val arbInst: Arb[Desc] = 
    Arb(Gen.listOf(descCharGen) map (cs ⇒ Desc(cs.mkString)))
}

// vim: set ts=2 sw=2 et:
