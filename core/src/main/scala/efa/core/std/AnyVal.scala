package efa.core.std

import efa.core.{Read, loc, UniqueId, ToXml, Default}
import scalaz._, Scalaz._

trait AnyValInstances {
  private def inst[A](
    v: String ⇒ Validation[Exception, A],
    msg: String ⇒ String
  ): Read[A] with UniqueId[A,A] = new Read[A] with UniqueId[A,A]{
    override def read(s: String) =
      ((_: Exception) ⇒ msg(s).wrapNel) <-: v(s)

    def id (a: A) = a
  }

  implicit val booleanInst =
    inst(_.parseBoolean, loc parseBooleanMsg _)

  implicit val intInst =
    inst(_.parseInt, loc parseIntMsg _)
  
  implicit val longInst =
    inst(_.parseLong, loc parseIntMsg _)

  implicit val doubleInst =
    inst(_.parseDouble, loc parseFloatMsg _)

  implicit val byteInst =
    inst(_.parseByte, loc parseIntMsg _)

  implicit val shortInst =
    inst(_.parseShort, loc parseIntMsg _)

  implicit val floatInst =
    inst(_.parseFloat, loc parseFloatMsg _)

  import ToXml.readShow

  implicit val BooleanToXml: ToXml[Boolean] = readShow

  implicit val IntToXml: ToXml[Int] = readShow
  
  implicit val LongToXml: ToXml[Long] = readShow

  implicit val DoubleToXml: ToXml[Double] = readShow

  implicit val ByteToXml: ToXml[Byte] = readShow

  implicit val ShortToXml: ToXml[Short] = readShow

  implicit val FloatToXml: ToXml[Float] = readShow

  implicit val UnitDefault: Default[Unit] = Default.monoid[Unit]

  implicit val BooleanDefault: Default[Boolean] = Default default true

  implicit val IntDefault: Default[Int] = Default.monoid[Int]
  
  implicit val LongDefault: Default[Long] = Default.monoid[Long]

  implicit val DoubleDefault: Default[Double] = Default.monoid[Double]

  implicit val ByteDefault: Default[Byte] = Default.monoid[Byte]

  implicit val ShortDefault: Default[Short] = Default.monoid[Short]

  implicit val FloatDefault: Default[Float] = Default.monoid[Float]
}

object anyVal extends AnyValInstances

// vim: set ts=2 sw=2 et:
