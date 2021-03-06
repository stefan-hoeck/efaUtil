package efa.core

import org.openide.util.Lookup
import efa.core.syntax.lookup
import scalaz._, Scalaz._, effect._

/** Provides one instance of a given service that is accessed via the default
  * Lookup. Once such an instant is initialized, it cannot be changed. This
  * class therefore should not be used for services whose implementations are
  * to change at runtime.
  */
sealed abstract class Service[A:Unerased:Default] {

  /** Use this only for referentially transparent services that never change
    * during the application live-cycle. One use case might be for
    * loading localization data.
    */
  def unsafeGet = service.unsafePerformIO

  lazy val service: IO[A] = {
    lazy val default = Default[A].default

    Lookup.getDefault.head[A] map (_ getOrElse default)
  }
} 

object Service {
  def apply[A:Unerased:Default]: Service[A] = new Service[A]{}

  def unique[A:Unerased:Default]: A = apply[A].unsafeGet
}

// vim: set ts=2 sw=2 et:
