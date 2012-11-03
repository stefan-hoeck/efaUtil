package efa.nb

import efa.core._, Efa._
import efa.react.{SET, EET, Out, sTrans, eTrans}
import efa.react.swing.AllFunctions._
import scala.swing._
import scalaz._, Scalaz._

trait InputWidgets extends InputWidgetsFunctions

trait InputWidgetsFunctions {
  type ValEET[A,B] = EET[A,ValRes[B]]
  type ValSET[A,B] = SET[A,ValRes[B]]
  type StSET[A,B] = SET[A,State[B,Unit]]
  type VSET[A,B] = SET[A,ValRes[State[B,Unit]]]

  def checkBox[A] (b: CheckBox)(l: A @> Boolean): VSET[A,A] =
    lensed(values(b) >=> success)(l)

  def comboBox[A,B] (b: ComboBox[B])(l: A @> B): VSET[A,A] =
    lensed(values(b) >=> success)(l)

  def intIn[A](
    t: TextField,
    v: EndoVal[Int] = Validators.dummy[Int]
  )(l: A @> Int): VSET[A,A] = textIn[A,Int](t, v = v)(l)

  def lensed[A,B] (in: ValSET[B,B])(l: A @> B): VSET[A,A] =
    in map (_ map (l := _ void)) contramap l.get

  def lensedV[A,B] (in: VSET[B,B])(l: A @> B): VSET[A,A] = {
    def nextSt (s: State[B,Unit]): State[A,Unit] =
      init[A] >>= (a ⇒ l := s.exec (l get a)) void

    in map (_ map nextSt) contramap l.get
  }

  def longIn[A](
    t: TextField,
    v: EndoVal[Long] = Validators.dummy[Long]
  )(l: A @> Long): VSET[A,A] = textIn[A,Long](t, v = v)(l)

  def outOnly[A] (o: Out[A]): SET[A,Nothing] =
    (sTrans.id[A] to o) >|> eTrans.never

  def readVals[A:Read](
    t: TextField,
    f: A ⇒ String = (a: A) ⇒ a.toString
  ): ValSET[A,A] = 
    values(t) andThen validate(Read[A].validator) contramap f

  def revalidate[A,B](v: Validator[A,B]): ValEET[ValRes[A],B] =
    eTrans.id[ValRes[A]] map (_ flatMap (v run _ validation))

  def stringIn[A](
    t: TextField,
    v: EndoVal[String] = Validators.dummy[String]
  )(l: A @> String): VSET[A,A] = textIn[A,String](t, v = v)(l)

  def success[A]: ValEET[A,A] = validate(Validators.dummy)

  def textIn[A,B:Read](
    t: TextField,
    v: EndoVal[B] = Validators.dummy[B],
    str: B ⇒ String = (b: B) ⇒ b.toString
  )(l: A @> B): VSET[A,A] =
    lensed(readVals(t, str) andThen revalidate(v))(l)

  def validate[A,B](v: Validator[A,B]): ValEET[A,B] =
    eTrans.id[A] map (v run _ validation)
}

object InputWidgets extends InputWidgets

// vim: set ts=2 sw=2 et:
