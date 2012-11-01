package efa.nb

import efa.core._, Efa._
import efa.react.{SET, Out, sTrans}
import efa.react.swing.AllFunctions._
import scala.swing._
import scalaz._, Scalaz._

trait InputWidgets

trait InputWidgetsFunctions {
  type ValSET[A,B] = SET[A,ValRes[B]]
  type StSET[A,B] = SET[A,State[B,Unit]]
  type VSET[A,B] = SET[A,ValRes[State[B,Unit]]]

  def stIn[A,B](in: SET[B,B])(l: A @> B): StSET[A,A] =
    in map (l := _ void) contramap l.get

  def stValIn[A,B](in: SET[B,B])(l: A @> B): VSET[A,A] =
    valIn(stIn(in)(l))

  def validate[A,B,C] (in: SET[A,B])(v: Validator[B,C]): ValSET[A,C] =
    in map (v run _ validation)

  def lensed[A,B] (in: ValSET[B,B])(l: A @> B): VSET[A,A] =
    in map (_ map (l := _ void)) contramap l.get

  def valIn[A,B](in: StSET[A,B]): VSET[A,B] = in map (_.success)

  def checkBox[A] (b: CheckBox)(l: A @> Boolean): VSET[A,A] =
    stValIn(values(b))(l)

  def comboBox[A,B] (b: ComboBox[B])(l: A @> B): VSET[A,A] =
    stValIn(values(b))(l)

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
    def valSET = validate (values(t))(vtot) contramap str

    lensed(valSET)(l)
  }
}

// vim: set ts=2 sw=2 et:
