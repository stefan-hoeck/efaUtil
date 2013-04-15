package efa.io

import efa.core.{TaggedToXml, ToXml, Efa, Named}, Efa._
import java.io._
import scala.xml.{PrettyPrinter, XML}
import scalaz.{Writer ⇒ _, _}, Scalaz._, effect.IO
import scalaz.iteratee.EnumeratorT.enumOne
import CharSet.UTF8
import logDisIO._, resource._

trait AsOutput[A] extends Named[A] {
  protected def os(a: A): IO[OutputStream]

  def outputStream(a: A): LogDisIO[OutputStream] = for {
    o ← except(liftIO(os(a)), openError(a))
    _ ← debug(opened(a))
  } yield o

  def writer(a: A, c: CharSet = UTF8): LogDisIO[Writer] = for {
    o ← outputStream(a)
    w ← except(point(new OutputStreamWriter(o, c)), openError(a))
  } yield w

  def printWriter(a: A, c: CharSet = UTF8): LogDisIO[PrintWriter] =
    for {
      w  ← writer(a, c)
      pw ← except(point(new PrintWriter(w)), openError(a))
    } yield pw

  def bytesI(a: A): IterIO[Array[Byte],Unit] = {
    def write(bs: Array[Byte], o: OutputStream) =
      except(point(o.write(bs, 0, bs.length)), writeError(a))

    iter.resourceIter(outputStream(a), name(a))(write)
  }

  def linesI(a: A, c: CharSet = UTF8): IterIO[String,Unit] = {
    def write(s: String, w: PrintWriter) =
      except(point(w.println(s)), writeError(a))

    iter.resourceIter(printWriter(a, c), name(a))(write)
  }

  def stringI(a: A, c: CharSet = UTF8): IterIO[String,Unit] = {
    def write(s: String, w: Writer) =
      except(point(w.write(s)), writeError(a))

    iter.resourceIter(writer(a, c), name(a))(write)
  }

  def writeLine(a: A, s: String, c: CharSet = UTF8): LogDisIO[Unit] =
    linesI(a, c) &= enumOne(s) run

  def writeString(a: A, s: String, c: CharSet = UTF8): LogDisIO[Unit] =
    stringI(a, c) &= enumOne(s) run

  def writeXml[B:TaggedToXml](a: A, 
                              b: B,
                              pretty: Option[PrettyPrinter] = None,
                              c: CharSet = UTF8): LogDisIO[Unit] =
    writeXmlTag(a, b, TaggedToXml[B].tag, pretty, c)

  def writeXmlTag[B:ToXml](a: A, 
                           b: B,
                           tag: String,
                           pretty: Option[PrettyPrinter] = None,
                           c: CharSet = UTF8): LogDisIO[Unit] =
    xmlTagI[B](a, tag, pretty, c) &= enumOne(b) run
    

  def xmlI[B:TaggedToXml](a: A, 
                          pretty: Option[PrettyPrinter] = None,
                          c: CharSet = UTF8): IterIO[B,Unit] =
    xmlTagI(a, TaggedToXml[B].tag, pretty, c)

  def xmlTagI[B](a: A, 
                 tag: String,
                 pretty: Option[PrettyPrinter] = None,
                 c: CharSet = UTF8)
                 (implicit T:ToXml[B]): IterIO[B,Unit] = {

    def write(b: B, w: Writer): LogDisIO[Unit] = {
      def x: scala.xml.Node = T writeTag (tag, b)
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

trait AsOutputSyntax {
  implicit class AsOutputOps[A](a: A)(implicit O: AsOutput[A]) {
    def outputStream: LogDisIO[OutputStream] = O outputStream a

    def writer(c: CharSet = UTF8): LogDisIO[Writer] = O.writer(a, c)

    def printWriter(c: CharSet = UTF8): LogDisIO[PrintWriter] =
      O.printWriter(a, c)

    def bytesI: IterIO[Array[Byte],Unit] = O bytesI a

    def linesI(c: CharSet = UTF8): IterIO[String,Unit] = O.linesI(a, c)

    def stringI(c: CharSet = UTF8): IterIO[String,Unit] = O.stringI(a, c)

    def writeLine(s: String, c: CharSet = UTF8): LogDisIO[Unit] =
      O.writeLine(a, s, c)

    def writeString(s: String, c: CharSet = UTF8): LogDisIO[Unit] =
      O.writeString(a, s, c)

    def writeXml[B:TaggedToXml](b: B,
                                pretty: Option[PrettyPrinter] = None,
                                c: CharSet = UTF8): LogDisIO[Unit] =
      O.writeXml(a, b, pretty, c)

    def writeXmlTag[B:ToXml](b: B,
                             tag: String,
                             pretty: Option[PrettyPrinter] = None,
                             c: CharSet = UTF8): LogDisIO[Unit] =
      O.writeXmlTag(a, b, tag, pretty, c)
      

    def xmlI[B:TaggedToXml](pretty: Option[PrettyPrinter] = None,
                            c: CharSet = UTF8): IterIO[B,Unit] =
      O.xmlI(a, pretty, c)

    def xmlTagI[B:ToXml](tag: String,
                         pretty: Option[PrettyPrinter] = None,
                         c: CharSet = UTF8): IterIO[B,Unit] =
      O.xmlTagI(a, tag, pretty, c)
  }
}

object AsOutput extends AsOutputInstances {
  def apply[A:AsOutput]: AsOutput[A] = implicitly

  object syntax extends AsOutputSyntax
}

// vim: set ts=2 sw=2 et:
