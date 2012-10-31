package efa.io

import scalaz._, Scalaz._, effect._

/**
 * Provides a convenient link between the OO world and the IO Monad
 *
 * Note, that the factory method is NOT referentially transparent.
 * It should therefore only be used in cases, where referential
 * transparency is not strictly required. One such use case might be
 * the field of an Object (= module) that must be initialized in
 * the IO-monad but should be immutable afterwards.
 *
 * IOCached form a Monad. Although their equality cannot be tested directly
 * (they are not equal when they have the same source, since the cached
 * value might be different), they can be considered equal when they
 * return the same cached value once initialized. This is important
 * when considering functor and monad laws. It can therefore easily
 * be shown, that map (identity) returns a IOCached value that is
 * equal to the original value since it just delegates it's 'get'
 * method to the original.
 */
sealed trait IOCached[+A] {

  import IOCached.Derived

  def get: IO[A]

  def map[B] (f: A ⇒ B): IOCached[B] = Derived (get map f)

  def flatMap[B] (f: A ⇒ IOCached[B]): IOCached[B] =
    Derived (get flatMap (f (_).get))

  def cached: IOCached[A] = IOCached apply get
}

object IOCached {

  /**
   * Creates an immutable (cached) reference to a value calculated
   * from src. This method is not referentially transparent. It
   * was designed to create cached fields in service provider
   * Objects that are initialized via the IO-Monad.
   *
   * For instance, if src is a complex IO-action whose return value
   * needs to be calculated only once. Within a pure application that
   * is started from within IO, such tricks are not needed, since
   * these values can be calculated directly within the IO-Monad and
   * then used anywhere else in the code.
   *
   * In a modular (Netbeans Platform) application that runs not within
   * the IO-Monad but uses Objects to provide behavior, this hack
   * can be useful to provide the same functionality.
   *
   * Note that the cached value must still be accessed via the IO-Monad
   * and is therefore not leaked out. 
   */
  def apply[A] (src: IO[A]): IOCached[A] = new Impl (src)

  final private class Impl[A] (src: IO[A]) extends IOCached[A] {
    private[this] lazy val a = src.unsafePerformIO

    val get: IO[A] = IO(a)
  }

  final private case class Derived[A] (get: IO[A]) extends IOCached[A]

  implicit val IOCachedMonad = new Monad[IOCached] {
    def point[A] (a: ⇒ A): IOCached[A] = Derived (IO (a))
    def bind[A,B](i: IOCached[A])(f: A ⇒ IOCached[B]): IOCached[B] =
      i flatMap f
  }
}

// vim: set ts=2 sw=2 et:
