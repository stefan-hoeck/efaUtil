package efa.io

import efa.core.{Named, DisRes, ToXml, Efa}, Efa._
import java.io._
import logDisIO._, resource._
import scala.util.control.NonFatal
import scalaz.{Reader ⇒ _, _}, Scalaz._, iteratee._, Iteratee._, effect.IO
import scalaz.std.indexedSeq._
import scala.xml.XML

trait AsInputO[A] {
  /** Returns an optional `InputStream` from an A together with a
    * String representing a meaningful name for the InputStream that
    * can be used during logging.
    *
    * It is OK for this IO-action to throw an exception when being run.
    * The exception will be caught and
    * transeferred to a DisRes by the public functions of this
    * type class.
    */
  protected def isO(a: A): IO[Option[(InputStream,String)]]

  protected def inputStreamO(a: A)
    : LogDisIO[Option[(InputStream,String)]] = for {
      o ← except(liftIO(isO(a)), openError(a.toString))
      _ ← o map { p ⇒ debug(opened(p._2)) } orZero
    } yield o

  protected def readerO(a: A): LogDisIO[Option[(Reader,String)]] = for {
    o ← inputStreamO(a)
    r ← o map { p ⇒ 
          except(success((new InputStreamReader(p._1), p._2)), openError(p._2))
        } sequence
  } yield r

  protected def bufferedReaderO(a: A)
    : LogDisIO[Option[(BufferedReader,String)]] =
    readerO(a) map { _ map { case(r,s) ⇒ (new BufferedReader(r), s) } }

  //def readXml[B:ToXml](a: A): LogDisIO[B] =
  //  (IterateeT.head[B,LogDisIO] &= xml(a) run) map { _.get }

  //def allLines(a: A): LogDisIO[IxSq[String]] =
  //  consume[String,LogDisIO,IxSq] &= lines(a) run

  //def xml[B:ToXml](a: A): EnumIO[B] =
  //  iter.resourceEnum(inputStream(a), name(a))(xmlR[B](a))

  //def lines(a: A): EnumIO[String] =
  //  iter.resourceEnum(bufferedReader(a), name(a))(lineR(a))

  //def allBytes(a: A): LogDisIO[Array[Byte]] =  {
  //  type Bytes = Array[Byte]

  //  val consIter = 
  //    Iteratee.fold[Bytes,LogDisIO,Bytes](Array.empty){ _ ++ _ }

  //  consIter &= bytes(a) run
  //}

  //def copyTo[B:AsOutput](a: A, b: B): LogDisIO[Unit] =
  //  (AsOutput[B].bytesI(b) &= bytes(a) run) >>
  //  debug(loc.copied(name(a), AsOutput[B].name(b)))

  //def bytes(a: A, buffer: Int = 8192): EnumIO[Array[Byte]] =
  //  iter.resourceEnumO(inputStreamO(a))(bytesR(buffer))

  //private def xmlR[B:ToXml](i: InputStream): EnumIO[B] =
  //  new SingleEnumIO[B](readError(i.toString)) {
  //    protected def load() = XML.load(i).readD[B]
  //  }

  //private def lineR(r: BufferedReader): EnumIO[String] =
  //  new RecursiveEnumIO[String](readError(r.toString)) {
  //    protected def next() = Option(r.readLine)
  //  }

  //private def bytesR(buffer: Int)(i: InputStream)
  //  : EnumIO[Array[Byte]] =
  //  new RecursiveEnumIO[Array[Byte]](readError(i.toString)) {
  //    protected def next() = {
  //      val bytes = new Array[Byte](buffer)

  //      i read bytes match {
  //        case -1 ⇒ None
  //        case x  ⇒ Some(bytes take x)
  //      }
  //    }
  //}
}

abstract class RecursiveEnumIO[E](msg: Throwable ⇒ String)
  extends EnumeratorT[E,LogDisIO] {
    protected def next(): Option[E]

    def apply[A] = (s: StepIO[E,A]) ⇒ s mapCont { k ⇒
      try {
        next() cata (e ⇒ k(elInput(e)) >>== apply[A], s.pointI)
      } catch { case NonFatal(e) ⇒ failIter(msg(e)) }
    }
  }

abstract class SingleEnumIO[E](msg: Throwable ⇒ String)
  extends EnumeratorT[E,LogDisIO] {
    protected def load(): DisRes[E]

    def apply[A] = (s: StepIO[E,A]) ⇒ s mapCont { k ⇒
      try {
        load() fold (failNelIter[E,A](_), e ⇒ k(elInput(e)))
      } catch { case NonFatal(e) ⇒ failIter(msg(e)) }
    }
  }

// vim: set ts=2 sw=2 et:
