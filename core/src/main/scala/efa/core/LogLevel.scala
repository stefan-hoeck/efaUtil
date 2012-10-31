package efa.core

import scalaz._, Scalaz._

sealed abstract class Level (val level: Int) extends Ordered[Level] {
  def name = toString.toLowerCase
  override def compare (that: Level) = this.level compare that.level
}

object Level {
  case object Trace extends Level (0)
  case object Debug extends Level (10)
  case object Info extends Level (100)
  case object Warning extends Level (1000)
  case object Error extends Level (10000)

  lazy val values = List[Level] (Trace, Debug, Info, Warning, Error)
  lazy val map = values map (_.name) zip values toMap

  implicit val LevelEqual = Equal.equalA[Level]

  implicit val LevelRead = new Read[Level] {
    override def read (s: String): ValRes[Level] =
      map get s toSuccess (loc logLevelMsg s wrapNel)
  }
}

// vim: set ts=2 sw=2 et:
