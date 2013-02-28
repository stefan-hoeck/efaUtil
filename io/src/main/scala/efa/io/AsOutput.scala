package efa.io

import efa.core.{TaggedToXml, Efa}, Efa._
import java.io._
import scala.xml.{PrettyPrinter, XML}
import scalaz.{Writer ⇒ _, _}, Scalaz._, effect.IO
import CharSet.UTF8
import valLogIO._, resource._

trait AsOutput[-A] {
  protected def os(a: A): IO[OutputStream]

  def outputStream(a: A): ValLogIO[OutputStream] = for {
    o ← except(liftIO(os(a)), openError(a))
    _ ← debug(opened(a))
  } yield o

  def writer(a: A, c: CharSet = UTF8): ValLogIO[Writer] = for {
    o ← outputStream(a)
    w ← except(point(new OutputStreamWriter(o, c)), openError(a))
  } yield w

  def printWriter(a: A, c: CharSet = UTF8): ValLogIO[PrintWriter] =
    for {
      w  ← writer(a, c)
      pw ← except(point(new PrintWriter(w)), openError(a))
    } yield pw

  def name(a: A): String

  def bytesOut(a: A): IterIO[Array[Byte],Unit] = {
    def write(bs: Array[Byte], o: OutputStream) =
      except(point(o.write(bs, 0, bs.length)), writeError(a))

    iter.resourceIter(outputStream(a), name(a))(write)
  }

  def linesOut(a: A, c: CharSet = UTF8): IterIO[String,Unit] = {
    def write(s: String, w: PrintWriter) =
      except(point(w.println(s)), writeError(a))

    iter.resourceIter(printWriter(a, c), name(a))(write)
  }

  def stringOut(a: A, c: CharSet = UTF8): IterIO[String,Unit] = {
    def write(s: String, w: Writer) =
      except(point(w.write(s)), writeError(a))

    iter.resourceIter(writer(a, c), name(a))(write)
  }

  def xmlOut[B](a: A, 
                pretty: Option[PrettyPrinter] = None,
                c: CharSet = UTF8)
                (implicit T:TaggedToXml[B]): IterIO[B,Unit] = {
    def write(b: B, w: Writer): ValLogIO[Unit] = {
      def x: scala.xml.Node = T write b
      def run = pretty cata (p ⇒  w.write(p format x),
                             XML.write(w, x, c, true, null))

      except(point(run), writeError(a))
    }

    iter.resourceIter(writer(a, c), name(a))(write)
  }

  private def opened(a: A): String = loc opened name(a)

  private def writeError(a: A)(t: Throwable): String = loc writeError (name(a), t)

  private def openError(a: A)(t: Throwable): String = loc openError (name(a), t)
}

trait AsOutputInstances {
  implicit val OsAsOutput: AsOutput[OutputStream] = 
    new AsOutput[OutputStream] {
      override protected def os(o: OutputStream) = IO(o)
      def name(o: OutputStream) = o.toString
    }
}

object AsOutput extends AsOutputInstances {
  def apply[A:AsOutput]: AsOutput[A] = implicitly
}

// vim: set ts=2 sw=2 et:
