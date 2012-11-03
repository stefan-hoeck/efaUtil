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

  def validate[A,B](v: Validator[A,B]): ValEET[A,B] =
    eTrans.id[A] map (v run _ validation)

  def success[A]: ValEET[A,A] = validate(Validators.dummy)

  def lensed[A,B] (in: ValSET[B,B])(l: A @> B): VSET[A,A] =
    in map (_ map (l := _ void)) contramap l.get

  def lensedV[A,B] (in: VSET[B,B])(l: A @> B): VSET[A,A] = {
    def nextSt (s: State[B,Unit]): State[A,Unit] =
      init[A] >>= (a ⇒ l := s.exec (l get a)) void

    in map (_ map nextSt) contramap l.get
  }

  def checkBox[A] (b: CheckBox)(l: A @> Boolean): VSET[A,A] =
    lensed(values(b) >=> success)(l)

  def comboBox[A,B] (b: ComboBox[B])(l: A @> B): VSET[A,A] =
    lensed(values(b) >=> success)(l)

  def intIn[A](
    t: TextField,
    v: EndoVal[Int] = Validators.dummy[Int]
  )(l: A @> Int): VSET[A,A] = textIn[A,Int](t, v = v)(l)

  def longIn[A](
    t: TextField,
    v: EndoVal[Long] = Validators.dummy[Long]
  )(l: A @> Long): VSET[A,A] = textIn[A,Long](t, v = v)(l)

  def stringIn[A](
    t: TextField,
    v: EndoVal[String] = Validators.dummy[String]
  )(l: A @> String): VSET[A,A] = textIn[A,String](t, v = v)(l)

  def textIn[A,B:Read](
    t: TextField,
    v: EndoVal[B] = Validators.dummy[B],
    str: B ⇒ String = (b: B) ⇒ b.toString
  )(l: A @> B): VSET[A,A] = {
    def vtot: Validator[String,B] = Read[B].validator >=> v
    def valSET = values(t) andThen validate(vtot) contramap str

    lensed(valSET)(l)
  }
}

object InputWidgets extends InputWidgets

// vim: set ts=2 sw=2 et:
