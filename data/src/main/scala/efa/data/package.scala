package efa

import scalaz._, Scalaz._

package object data {
  type IntId[A] = UniqueId[A,Int]

  type IntIdL[A] = UniqueIdL[A,Int]

  type LongId[A] = UniqueId[A,Long]

  type LongIdL[A] = UniqueIdL[A,Long]

  def IntId[A:IntId]: IntId[A] = implicitly

  def LongId[A:LongId]: LongId[A] = implicitly

  def IntIdL[A:IntIdL]: IntIdL[A] = implicitly

  def LongIdL[A:LongIdL]: LongIdL[A] = implicitly
}

// vim: set ts=2 sw=2 et:
