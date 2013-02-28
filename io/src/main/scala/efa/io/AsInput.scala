package efa.io

import efa.core.{DisRes, ToXml, Efa}, Efa._
import java.io._
import valLogIO._, resource._
import scala.util.control.NonFatal
import scalaz.{Reader ⇒ _, _}, Scalaz._, iteratee._, Iteratee._, effect.IO
import scalaz.std.indexedSeq._
import scala.xml.XML

trait AsInput[-A] {
  /** Returns an `InputStream` from an A
    *
    * It is OK for this IO-action to throw an exception when being run.
    * The exception will be caught and
    * transeferred to a DisRes by the public functions of this
    * type class.
    */
  protected def is(a: A): IO[InputStream]

  def inputStream(a: A): ValLogIO[InputStream] = for {
    i ← except(liftIO(is(a)), openError(a))
    _ ← debug(opened(a))
  } yield i

  def reader(a: A): ValLogIO[Reader] = for {
    is ← inputStream(a)
    r  ← except(success(new InputStreamReader(is)), openError(a))
  } yield r

  def bufferedReader(a: A): ValLogIO[BufferedReader] = for {
    r  ← reader(a)
    br ← except(success(new BufferedReader(r)), openError(a))
  } yield br

  def readXml[B:ToXml](a: A): LogToDisIO[B] =
    (IterateeT.head[B,LogToDisIO] &= xml(a) run) map { _.get }

  def allLines(a: A): LogToDisIO[IxSq[String]] =
    consume[String,LogToDisIO,IxSq] &= lines(a) run

  def xml[B:ToXml](a: A): EnumIO[B] =
    iter.resourceEnum(inputStream(a), name(a))(xmlR[B](a))

  def lines(a: A): EnumIO[String] =
    iter.resourceEnum(bufferedReader(a), name(a))(lineR(a))

  def allBytes(a: A): LogToDisIO[Array[Byte]] =  {
    type Bytes = Array[Byte]

    val consIter = 
      Iteratee.fold[Bytes,LogToDisIO,Bytes](Array.empty){ _ ++ _ }

    consIter &= bytes(a) run
  }

  def copy[B:AsOutput](a: A, b: B): LogToDisIO[Unit] =
    AsOutput[B].bytesOut(b) &= bytes(a) run

  def bytes(a: A, buffer: Int = 8192): EnumIO[Array[Byte]] =
    iter.resourceEnum(inputStream(a), name(a))(bytesR(a, buffer))

  private def xmlR[B:ToXml](a: A)(i: InputStream): EnumIO[B] =
    new SingleEnumIO[B](readError(a)) {
      protected def load() = XML.load(i).readD[B]
    }

  private def lineR(a: A)(r: BufferedReader): EnumIO[String] =
    new RecursiveEnumIO[String](readError(a)) {
      protected def next() = Option(r.readLine)
    }

  private def bytesR(a: A, buffer: Int)(i: InputStream)
    : EnumIO[Array[Byte]] = new RecursiveEnumIO[Array[Byte]](readError(a)) {
    protected def next() = {
      val bytes = new Array[Byte](buffer)

      i read bytes match {
        case -1 ⇒ None
        case x  ⇒ Some(bytes take x)
      }
    }
  }

  def name(a: A): String

  private def opened(a: A): String = loc opened name(a)

  private def readError(a: A)(t: Throwable): String = loc readError (name(a), t)

  private def openError(a: A)(t: Throwable): String = loc openError (name(a), t)
}

trait AsInputInstances {
  implicit val IsAsInput: AsInput[InputStream] = new AsInput[InputStream] {
    protected def is(i: InputStream) = IO(i)
    def name(i: InputStream) = i.toString
  }

  implicit val ClassAsInput: AsInput[(String,Class[_])] =
    new AsInput[(String,Class[_])] {
      protected def is(p: (String,Class[_])) = 
        IO(p._2 getResourceAsStream p._1)

      def name(p: (String,Class[_])) = p._1
    }
}

object AsInput {
  def apply[A:AsInput]: AsInput[A] = implicitly
}

private abstract class RecursiveEnumIO[E](msg: Throwable ⇒ String)
  extends EnumeratorT[E,LogToDisIO] {
    protected def next(): Option[E]

    def apply[A] = (s: StepIO[E,A]) ⇒ s mapCont { k ⇒
      try {
        next() cata (e ⇒ k(elInput(e)) >>== apply[A], s.pointI)
      } catch { case NonFatal(e) ⇒ failIter(msg(e)) }
    }
  }

private abstract class SingleEnumIO[E](msg: Throwable ⇒ String)
  extends EnumeratorT[E,LogToDisIO] {
    protected def load(): DisRes[E]

    def apply[A] = (s: StepIO[E,A]) ⇒ s mapCont { k ⇒
      try {
        load() fold (failNelIter[E,A](_), e ⇒ k(elInput(e)) >>== apply[A])
      } catch { case NonFatal(e) ⇒ failIter(msg(e)) }
    }
  }

// vim: set ts=2 sw=2 et:
