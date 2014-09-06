package efa.io

import efa.core.{DisRes, ToXml, Named}
import efa.core.syntax.nodeSeq

import java.io._
import logDisIO._, resource._
import scala.util.control.NonFatal
import scalaz.{Reader ⇒ _, _}, Scalaz._, iteratee._, Iteratee._, effect.IO
import scalaz.std.indexedSeq._
import scala.xml.XML

trait AsInput[A] {
  def name(a: A): String

  /** Returns an `InputStream` from an A
    *
    * It is OK for this IO-action to throw an exception when being run.
    * The exception will be caught and
    * transeferred to a DisRes by the public functions of this
    * type class.
    */
  protected def is(a: A): IO[InputStream]

  final protected def isO(a: A) = is(a) map (_.some)

  def inputStream(a: A): LogDisIO[InputStream] = for {
    i ← except(liftIO(is(a)), openError(a))
    _ ← debug(opened(a))
  } yield i

  def reader(a: A): LogDisIO[Reader] = for {
    is ← inputStream(a)
    r  ← except(success(new InputStreamReader(is)), openError(a))
  } yield r

  def bufferedReader(a: A): LogDisIO[BufferedReader] = for {
    r  ← reader(a)
    br ← except(success(new BufferedReader(r)), openError(a))
  } yield br

  def readXml[B:ToXml](a: A): LogDisIO[B] =
    (IterateeT.head[B,LogDisIO] &= xml(a) run) map { _.get }

  def allLines(a: A): LogDisIO[IxSq[String]] =
    consume[String,LogDisIO,IxSq] &= lines(a) run

  def xml[B:ToXml](a: A): EnumIO[B] =
    iter.resourceEnum(inputStream(a), name(a))(xmlR[B](a))

  def lines(a: A): EnumIO[String] =
    iter.resourceEnum(bufferedReader(a), name(a))(lineR(a))

  def allBytes(a: A): LogDisIO[Array[Byte]] =  {
    type Bytes = Array[Byte]

    val consIter = 
      Iteratee.fold[Bytes,LogDisIO,Bytes](Array.empty){ _ ++ _ }

    consIter &= bytes(a) run
  }

  def copyTo[B:AsOutput](a: A, b: B): LogDisIO[Unit] =
    (AsOutput[B].bytesI(b) &= bytes(a) run) >>
    debug(loc.copied(name(a), AsOutput[B].name(b)))

  def bytes(a: A, buffer: Int = 8192): EnumIO[Array[Byte]] =
    iter.resourceEnum(inputStream(a), name(a))(bytesR(a, buffer))

  private def xmlR[B:ToXml](a: A)(i: InputStream): EnumIO[B] =
    iter.singleEnum[B](() ⇒ XML.load(i).readD[B], readError(a))

  private def lineR(a: A)(r: BufferedReader): EnumIO[String] =
    iter.readerEnum(() ⇒ Option(r.readLine) map { _.right }, readError(a))

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

  private def opened(a: A): String = loc opened name(a)

  private def readError(a: A)(t: Throwable): String = loc readError (name(a), t)

  private def openError(a: A)(t: Throwable): String = loc openError (name(a), t)
}

trait AsInputSyntax {
  implicit class AsInputOps[A](val a: A)(implicit I:AsInput[A]) {

    def inputStream: LogDisIO[InputStream] = I inputStream a

    def reader: LogDisIO[Reader] = I reader a

    def bufferedReader: LogDisIO[BufferedReader] = I bufferedReader a

    def readXml[B:ToXml]: LogDisIO[B] = I.readXml[B](a)

    def allLines: LogDisIO[IxSq[String]] = I allLines a

    def xmlIn[B:ToXml]: EnumIO[B] = I xml a

    def lines: EnumIO[String] = I lines a

    def allBytes: LogDisIO[Array[Byte]] = I allBytes a

    def copyTo[B:AsOutput](b: B): LogDisIO[Unit] = I.copyTo(a, b)

    def bytes(buf: Int = 8192): EnumIO[Array[Byte]] = I.bytes(a, buf)
  }
}

trait AsInputInstances {
  implicit val IsAsInput: AsInput[InputStream] = new AsInput[InputStream] {
    protected def is(i: InputStream) = IO(i)
    def name(i: InputStream) = i.toString
  }

  implicit val ClassAsInput: AsInput[ClassResource] =
    new AsInput[ClassResource] {
      protected def is(p: ClassResource) = IO(p._2 getResourceAsStream p._1)

      def name(p: ClassResource) = p._1
    }
}

object AsInput extends AsInputInstances {
  def apply[A:AsInput]: AsInput[A] = implicitly

  object syntax extends AsInputSyntax
}

abstract class RecursiveEnumIO[E](msg: Throwable ⇒ String)
  extends EnumeratorT[E,LogDisIO] {
    protected def next(): Option[E]

    def apply[A] = (s: StepIO[E,A]) ⇒ s mapCont { k ⇒
      try {
        next() cata (e ⇒ k(elInput(e)) >>== apply[A], s.pointI)
      } catch { case NonFatal(e) ⇒ 
        //call k with eof in order to close open resources.
        //k will be discarded afterwards and never be called again
        iter.vIter(k(eofInput).value >> fail(msg(e)))
      }
    }
  }

// vim: set ts=2 sw=2 et:
