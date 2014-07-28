package efa.core

import Efa._, syntax._
import org.scalacheck._, Prop._
import scalaz._, Scalaz._

object LevelTest extends Properties("Level") {
  property("read") = Level.values ∀ (l ⇒ l.name.read[Level] ≟ l.success)

  property("equal") = Level.values ∀ (l ⇒ l ≟ l)

  property("notEqual") = Level.values ∀ (l1 ⇒
    Level.values.filterNot (l1 ==) ∀ (l1 ≠ ))
}

// vim: set ts=2 sw=2 et:
