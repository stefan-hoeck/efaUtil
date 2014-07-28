package efa.core

import scalaz._, Scalaz._

/** A type class similar to the Read type class in Haskell but
  * which returns validated results after parsing a String.
  */
trait Read[A] {

  /** Tries to read a value from a String. The result is stored
    * in a `Validation`.
    */
  def read(s: String): ValRes[A] = readD(s).validation

  /** Tries to read a value from a String. The result is stored
    * in a Disjunction.
    */
  def readD(s: String): DisRes[A] = read(s).disjunction

  /** Parsing Strings in the Reader Monad
    */
  final lazy val validator: Validator[String,A] = Kleisli(readD)
}

trait ReadFunctions {
  def read[A](v: Validator[String,A]): Read[A] = new Read[A] {
    override def readD(s: String) = v run s
  }

  def readD[A](v: String ⇒ DisRes[A]): Read[A] = new Read[A] {
    override def readD (s: String) = v(s)
  }

  def readV[A](v: String ⇒ ValRes[A]): Read[A] = new Read[A] {
    override def read (s: String) = v(s)
  }

  def localized[A:Localized](values: Seq[A]): Read[A] = new Read[A] {
    lazy val map: Map[String, A] =
      values flatMap (a ⇒ Localized[A] names a map (_ → a)) toMap

    override def read (s: String): ValRes[A] =
      map get s toSuccess (loc notFoundMsg s wrapNel)
  }
}

object Read extends ReadFunctions {
  @inline def apply[A:Read]: Read[A] = implicitly
}

trait ReadSpecs {
  import org.scalacheck.Prop, Prop._
  import efa.core.syntax.StringOps
  import efa.core.std.prop._
  
  def showRead[A:Show:Read:Equal]: A ⇒ Prop =
    a ⇒ compareP(a, a.shows.read[A])

  def toStringRead[A:Read:Equal]: A ⇒ Prop =
    a ⇒ compareP(a, a.toString.read[A])

  def localizedRead[A:Localized:Read:Equal]: A ⇒ Prop =
    a ⇒ Localized[A].names(a) foldMap (s ⇒ compareP(a, s.read[A]))

  def readAll[A:Read]: String ⇒ Prop = s ⇒ try{
    val r = s.read[A]; true
  } catch { case util.control.NonFatal(e) ⇒ false }
}

object ReadSpecs extends ReadSpecs

// vim: set ts=2 sw=2 et:
