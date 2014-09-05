package efa.core

import scalaz.{Enum, Order}
import scalaz.std.anyVal._
import scalaz.syntax.std.option._
import scalaz.syntax.nel._

/** Describes the severity of a logging message.
  */
sealed abstract class Level (val level: Int) extends Ordered[Level] {
  def name = toString.toLowerCase
  override def compare (that: Level) = this.level compare that.level

  def log(msg: ⇒ String): Log = Log log (msg, this)
}

object Level {
  def trace: Level = Trace
  def debug: Level = Debug
  def info: Level = Info
  def warning: Level = Warning
  def error: Level = Error

  case object Trace extends Level (0)
  case object Debug extends Level (10)
  case object Info extends Level (100)
  case object Warning extends Level (1000)
  case object Error extends Level (10000)

  lazy val values = List[Level] (Trace, Debug, Info, Warning, Error)
  lazy val map = values map (_.name) zip values toMap

  implicit val enumInst: Enum[Level] = new Enum[Level] {
    private val pmap = values.zip(error::values.init).toMap
    private val nmap = values.zip(values.tail ::: List(trace)).toMap
    def pred(l: Level) = pmap(l)
    def succ(l: Level) = nmap(l)
    def order(a: Level, b: Level) = Order[Int] order (a.level, b.level)
  }

  implicit val readInst: Read[Level] = new Read[Level] {
    override def read (s: String): ValRes[Level] =
      Level.map get s toSuccess (loc logLevelMsg s wrapNel)
  }
}

// vim: set ts=2 sw=2 et:
