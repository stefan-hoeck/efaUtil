package efa.core

import scalaz._, Scalaz._

/** A type class similar to the Read type class in Haskell but
  * which returns validated results after parsing a String.
  */
trait Read[A] { self ⇒

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

  def reval[B](v: Validator[A,B]): Read[B] = revalD(v.run)

  def revalV[B](v: A ⇒ ValRes[B]): Read[B] = revalD(v(_).disjunction)

  def revalD[B](v: A ⇒ DisRes[B]): Read[B] = new Read[B] {
    override def readD(s: String): DisRes[B] = self readD s flatMap v
  }
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

object Read extends ReadFunctions with ReadSpecs {
  @inline def apply[A:Read]: Read[A] = implicitly

  def map[A,B](f: A ⇒ B)(implicit A:Read[A]): Read[B] = new Read[B] {
    override def read(s: String) = A read s map f
  }
}

trait ReadSpecs {
  import org.scalacheck.{Prop, Arbitrary, Properties}, Prop._
  import efa.core.syntax.string
  import efa.core.std.prop._

  def laws[A:Read:Equal:Arbitrary] = new Properties("read") {
    property("read is total") = readAll[A]
  }

  def showLaws[A:Show:Read:Equal:Arbitrary] = new Properties("readShow") {
    include(laws[A])
    property("show / read identity") = showRead[A]
  }

  def localizedLaws[A:Localized:Show:Read:Equal:Arbitrary] =
    new Properties("readLocalize") {
      include(showLaws[A])
      property("localized / read identity") = localizedRead[A]
    }
  
  def showRead[A:Show:Read:Equal:Arbitrary]: Prop = Prop forAll { a: A ⇒ 
    compareP(a, a.shows.read[A])
  }

  def toStringRead[A:Read:Equal:Arbitrary]: Prop = Prop forAll { a: A ⇒ 
    compareP(a, a.toString.read[A])
  }

  def localizedRead[A:Localized:Read:Equal:Arbitrary]: Prop = forAll { a: A ⇒ 
    Localized[A].names(a) foldMap (s ⇒ compareP(a, s.read[A]))
  }

  def readAll[A:Read]: Prop = forAll { s: String ⇒ 
    try{
      val r = s.read[A]; true
    } catch { case util.control.NonFatal(e) ⇒ false }
  }
}

// vim: set ts=2 sw=2 et:
