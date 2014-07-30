package efa.io

import scalaz.effect.{IO, Resource}

class Bytes(bytes: Array[Byte]) extends java.io.ByteArrayInputStream(bytes) {
  private[this] var closed = false
  private[this] var opened = false

  def open() { if(opened) sys.error("already opened"); opened = true }

  override def close() { if(closed) sys.error("already closed"); closed = true }

  def isClosed = closed

  def wasOpened = opened
}

object Bytes {
  def apply(bytes: Array[Byte]): Bytes = new Bytes(bytes)

  def apply(str: String): Bytes = apply(str getBytes efa.io.CharSet.UTF8)

  def apply(lines: List[String]): Bytes = apply(lines mkString "\n")

  implicit val BytesResource: Resource[Bytes] = new Resource[Bytes] {
    def close(b: Bytes) = IO(b.close())
  }

  implicit val BytesAsInput: AsInput[Bytes] = new AsInput[Bytes] {
    protected def is(b: Bytes) = IO{ b.open(); b }
    def name(b: Bytes) = s"Bytes: ${b.hashCode}"
  }
}

class BytesOut extends java.io.ByteArrayOutputStream {
  private[this] var closed = false
  private[this] var opened = false

  def open() { if(opened) sys.error("already opened"); opened = true }

  override def close() { if(closed) sys.error("already closed"); closed = true }

  def isClosed = closed

  def wasOpened = opened

  def getString = new String(toByteArray)

  def getLines: List[String] = getString match {
    case "" ⇒ Nil
    case x  ⇒ x split "\n" toList
  }
}

object BytesOut {
  implicit val BytesResource: Resource[BytesOut] = new Resource[BytesOut] {
    def close(b: BytesOut) = IO(b.close())
  }

  implicit val BytesAsInput: AsOutput[BytesOut] = new AsOutput[BytesOut] {
    protected def os(b: BytesOut) = IO{ b.open(); b }
    def name(b: BytesOut) = s"BytesOut: ${b.hashCode}"
  }
}

// vim: set ts=2 sw=2 et:
