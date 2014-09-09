package efa.core

import Efa._, syntax._
import org.scalacheck._, Prop._
import scalaz._, Scalaz._
import scalaz.scalacheck.ScalazProperties.{enum ⇒ enumz}

object LevelTest extends Properties("Level") {
  include(Read.showLaws[Level])
  include(enumz.laws[Level])
}

// vim: set ts=2 sw=2 et:
