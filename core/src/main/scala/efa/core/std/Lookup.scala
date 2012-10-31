package efa.core.std

import efa.core.Provider
import org.openide.util.Lookup
import scalaz._, Scalaz._
import scalaz.effect.IO
import scala.collection.JavaConversions._

trait LookupFunctions {
  private[this] def lkp[A](l: Lookup)(implicit m: Manifest[A]): Option[A] =
    Option(l lookup m.erasure.asInstanceOf[Class[A]])

  private[this] def lkpAll[A](l: Lookup)(implicit m: Manifest[A]): List[A] =
    l lookupAll m.erasure.asInstanceOf[Class[A]] toList

  def head[A:Manifest](l: Lookup): IO[Option[A]] = IO(lkp[A](l))

  def all[A:Manifest](l: Lookup): IO[List[A]] = IO(lkpAll[A](l))

  def lkp: Lookup = Lookup.getDefault

  def lookupHead[A:Manifest]: IO[Option[A]] = head(lkp)

  def lookupAll[A:Manifest]: IO[List[A]] = all(lkp)

  def provide[A,P<:Provider[A]:Manifest]: IO[List[A]] =
    lookupAll[P] map (_ foldMap (_.get) toList)
}

object lookup extends LookupFunctions

// vim: set ts=2 sw=2 et:
