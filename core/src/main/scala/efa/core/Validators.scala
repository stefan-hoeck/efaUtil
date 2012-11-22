package efa.core

import scalaz._, Scalaz._

trait Validators {

  private def kl[A,B](f: A ⇒ DisRes[B]): Validator[A,B] =
    Kleisli(f)

  def apply[A,B](f: A ⇒ DisRes[B]): Validator[A,B] = kl(f)

  def endo[A](f: A ⇒ DisRes[A]): Validator[A,A] = kl(f)

  def endoV[A](f: A ⇒ ValRes[A]): Validator[A,A] =
    endo (f andThen (_.disjunction))

  private def check[A](p: A ⇒ Boolean, msg: A ⇒ String): EndoVal[A] =
    kl(a ⇒ p(a) ? a.right[Nel[String]] | msg(a).wrapNel.left)

  /**
   * A dummy validator that returns Valid no matter what kind of value
   * is passed to it.
   */
   def dummy[A]: EndoVal[A] = kl(_.right)

  /**
   * This validator checks whether a String consists only of white-space
   * characters. It returns an invalid ValidationInfo object, if this is
   * the case.
   */
  val notEmptyString: EndoVal[String] =
    check(_.trim.nonEmpty,_ ⇒ loc.isEmptyMsg)

  /**
   * This is a Validator that is typically used in combination with other String
   * validators when a String either can be empty, or, if it is not empty, must
   * be validated according to other rules. Use the following syntax in that case:
   * MustBeEmptyString(value) || OtherValidator(value) &&...
   */
  val mustBeEmptyString: EndoVal[String] =
    check(_.isEmpty, _ ⇒ loc.mustBeEmptyMsg)

  /**
   * This Validator returns ValidationInfo.Valid iff the passed
   * value != notAllowed
   */
  def not[A:Equal] (notAllowed: A): EndoVal[A] =
    check (notAllowed ≠, loc notAllowedMsg _.toString) 

  def interval[A <% Ordered[A]] (min: A, max: A): EndoVal[A] = {
    require (min <= max)

    check(
      a ⇒ min <= a && a <= max,
      _ ⇒ loc.notInIntervalMsg(min.toString, max.toString)
    )
  }

  /**
   * Returns a Validator that checks that a passed String's length does
   * not exceed param length.
   */
  def maxStringLength (length: Int): Validator[String,String] = {
    require (length >= 0)

    check (_.length <= length, _ ⇒ loc maxStringLengthMsg length)
  }

  def uniqueName (ns: Set[String]): EndoVal[String] =
    check(s ⇒ !ns(s), loc.nameExists)
}

object Validators extends Validators

trait ValidatorSpecs {
  def validated[A:Equal,B] (f: (A,B) ⇒ A)(v: EndoVal[B]) = (p: (A,B)) ⇒ {
    val (a,b) = p
    def valRes = v(b).toOption ∘ f.curried(a)
    def setRes: Option[A] = 
      try{f(a,b).some} catch {case e: IllegalArgumentException ⇒ none}

    valRes ≟ setRes
  }
}

//object ValidatorSpecs extends ValidatorSpecs

// vim: set ts=2 sw=2 et:
