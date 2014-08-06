package efa.core.std

import efa.core.{Provider, Unerased}
import org.openide.util.Lookup
import scalaz._, Scalaz._
import scalaz.effect.IO
import scala.collection.JavaConversions._

trait LookupFunctions {
  private[this] def lkp[A:Unerased](l: Lookup): Option[A] =
    Option(l lookup Unerased[A].clazz)

  private[this] def lkpAll[A:Unerased](l: Lookup): List[A] =
    l lookupAll Unerased[A].clazz toList

  def head[A:Unerased](l: Lookup): IO[Option[A]] = IO(lkp[A](l))

  def all[A:Unerased](l: Lookup): IO[List[A]] = IO(lkpAll[A](l))

  def lkp: Lookup = Lookup.getDefault

  def lookupHead[A:Unerased]: IO[Option[A]] = head(lkp)

  def lookupAll[A:Unerased]: IO[List[A]] = all(lkp)

  def provide[A,P<:Provider[A]:Unerased]: IO[List[A]] =
    lookupAll[P] map (_ foldMap (_.get) toList)
}

object lookup extends LookupFunctions

// vim: set ts=2 sw=2 et:

