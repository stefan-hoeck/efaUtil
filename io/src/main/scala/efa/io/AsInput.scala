package efa.io

import java.io._
import valLogIO._, resource._
import scala.util.control.NonFatal
import scalaz.{Reader ⇒ _, _}, Scalaz._, iteratee._, Iteratee._

trait AsInput[-A] {
  def inputStream(a: A): ValLogIO[InputStream]

  def reader(a: A): ValLogIO[Reader] = for {
    is ← inputStream(a)
    r  ← except(success(new InputStreamReader(is)), _ ⇒ readError(a))
  } yield r

  def bufferedReader(a: A): ValLogIO[BufferedReader] = for {
    r  ← reader(a)
    br ← except(success(new BufferedReader(r)), _ ⇒ readError(a))
  } yield br

//  def lines(a: A): VLIOEnum[String] =
//    iter.resourceEnum(bufferedReader(a))(lineR(_, readError(a)))
//
//  def bytes(a: A, buffer: Int = 8192): VLIOEnum[Array[Byte]] =
//    iter.resourceEnum(inputStream(a))(bytesR(_, readError(a), buffer))
//
//  private def lineR(r: BufferedReader, msg: ⇒ String): VLIOEnum[String] =
//    new RecursiveVLIOEnum[String,BufferedReader](msg) {
//      protected def next() = Option(r.readLine)
//    }
//
//  private def bytesR(is: InputStream, msg: ⇒ String, buffer: Int)
//    : VLIOEnum[Array[Byte]] =
//    new RecursiveVLIOEnum[Array[Byte], InputStream](msg) {
//      protected def next() = {
//        val bytes = new Array[Byte](buffer)
//
//        is read bytes match {
//          case -1 ⇒ None
//          case x  ⇒ Some(bytes take x)
//        }
//      }
//    }

  def readError(a: A): String

  def writeError(a: A): String
}

//private abstract class RecursiveVLIOEnum[E,R](msg: ⇒ String)
//  extends EnumeratorT[E,ValLogIO] {
//    protected def next(): Option[E]
//
//    def apply[A] = (s: VLIOStep[E,A]) ⇒ s mapCont { k ⇒
//      try {
//        val n = next()
//        n cata (e ⇒ k(elInput(e)) >>== apply[A], s.pointI)
//      } catch {
//        case NonFatal(_) ⇒ iterateeT[E,ValLogIO,A](fail(msg))
//      }
//    }
//  }
//
//private abstract class SingleVLIOEnum[E,R](msg: ⇒ String)
//  extends EnumeratorT[E,ValLogIO] {
//    def next(): Option[E]
//
//    def apply[A] = (s: VLIOStep[E,A]) ⇒ s mapCont { k ⇒
//      try {
//        val n = next()
//        n cata (e ⇒ k(elInput(e)) >>== apply[A], s.pointI)
//      } catch {
//        case NonFatal(_) ⇒ iterateeT[E,ValLogIO,A](fail(msg))
//      }
//    }
//  }

// vim: set ts=2 sw=2 et:
