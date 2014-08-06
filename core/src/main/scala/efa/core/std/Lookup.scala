package efa.core.std

import efa.core.{Provider, Lookupable}
import org.openide.util.Lookup
import scalaz._, Scalaz._
import scalaz.effect.IO
import scala.collection.JavaConversions._

trait LookupFunctions {
  //@TODO: Implement via a Macro that checks that runtime type does not
  //suffer from erasure. This will render the implementation completely
  //type safe.
  private[this] def lkp[A:Lookupable](l: Lookup): Option[A] =
    Option(l lookup Lookupable[A].clazz)

  private[this] def lkpAll[A:Lookupable](l: Lookup): List[A] =
    l lookupAll Lookupable[A].clazz toList

  def head[A:Lookupable](l: Lookup): IO[Option[A]] = IO(lkp[A](l))

  def all[A:Lookupable](l: Lookup): IO[List[A]] = IO(lkpAll[A](l))

  def lkp: Lookup = Lookup.getDefault

  def lookupHead[A:Lookupable]: IO[Option[A]] = head(lkp)

  def lookupAll[A:Lookupable]: IO[List[A]] = all(lkp)

  def provide[A,P<:Provider[A]:Lookupable]: IO[List[A]] =
    lookupAll[P] map (_ foldMap (_.get) toList)
}

object lookup extends LookupFunctions

// vim: set ts=2 sw=2 et:

