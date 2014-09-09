package efa.core

import scalaz.{Enum, Order, Show}
import scalaz.std.anyVal._
import scalaz.syntax.std.option._
import scalaz.syntax.nel._
import org.scalacheck.{Arbitrary ⇒ Arb, Gen}

/** Describes the severity of a logging message.
  */
sealed abstract class Level(val level: Int) extends Ordered[Level] {
  def name = toString.toLowerCase
  override def compare (that: Level) = this.level compare that.level

  def log(msg: ⇒ String): Log = Log log (msg, this)

  def succ: Level
  def pred: Level
}

object Level {
  def trace: Level = Trace
  def debug: Level = Debug
  def info: Level = Info
  def warning: Level = Warning
  def error: Level = Error

  case object Trace extends Level (0){ def succ = Debug; def pred = Error}
  case object Debug extends Level (10){ def succ = Info; def pred = Trace}
  case object Info extends Level (100){ def succ = Warning; def pred = Debug}
  case object Warning extends Level (1000){ def succ = Error; def pred = Info}
  case object Error extends Level (10000){ def succ = Trace; def pred = Warning}

  lazy val values = List[Level] (Trace, Debug, Info, Warning, Error)
  lazy val map = values flatMap (l ⇒ List(l.name → l, l.toString → l)) toMap

  implicit val arbInst: Arb[Level] = Arb(Gen oneOf values)

  implicit val enumInst: Enum[Level] = new Enum[Level] {
    def pred(l: Level) = l.pred
    def succ(l: Level) = l.succ
    def order(a: Level, b: Level) = Order[Int] order (a.level, b.level)
    override def min = Some(trace)
    override def max = Some(error)
  }

  implicit val readInst: Read[Level] = new Read[Level] {
    override def read (s: String): ValRes[Level] =
      Level.map get s toSuccess (loc logLevelMsg s wrapNel)
  }

  implicit val showInst: Show[Level] = Show shows (_.name)
}

// vim: set ts=2 sw=2 et:
