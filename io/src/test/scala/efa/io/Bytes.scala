package efa.io

import scalaz.effect.{IO, Resource}

class Bytes(bytes: Array[Byte]) 
  extends java.io.ByteArrayInputStream(bytes) {
  private[this] var closed = false

  override def close() { closed = true }

  def isClosed = closed
}

class BytesOut extends java.io.ByteArrayOutputStream {
  private[this] var closed = false

  override def close() { closed = true }

  def isClosed = closed

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
    protected def os(b: BytesOut) = IO(b)
    def name(b: BytesOut) = s"BytesOut: ${b.hashCode}"
  }
}

object Bytes {
  def apply(bytes: Array[Byte]): Bytes = new Bytes(bytes)

  def apply(str: String): Bytes = apply(str getBytes scalaz.CharSet.UTF8)

  def apply(lines: List[String]): Bytes = apply(lines mkString "\n")

  implicit val BytesResource: Resource[Bytes] = new Resource[Bytes] {
    def close(b: Bytes) = IO(b.close())
  }

  implicit val BytesAsInput: AsInput[Bytes] = new AsInput[Bytes] {
    protected def is(b: Bytes) = IO(b)
    def name(b: Bytes) = s"Bytes: ${b.hashCode}"
  }
}

// vim: set ts=2 sw=2 et:
