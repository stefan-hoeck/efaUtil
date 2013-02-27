package efa.io

//import scalaz.effect.{IO, Resource}
//
//class Bytes(bytes: Array[Byte]) 
//  extends java.io.ByteArrayInputStream(bytes) {
//  private[this] var closed = false
//
//  override def close() { closed = true }
//
//  def isClosed = closed
//}
//
//object Bytes {
//  def apply(bytes: Array[Byte]): Bytes = new Bytes(bytes)
//
//  def apply(str: String): Bytes = apply(str getBytes scalaz.CharSet.UTF8)
//
//  implicit val BytesResource: Resource[Bytes] = new Resource[Bytes] {
//    def close(b: Bytes) = IO(b.close())
//  }
//
//  implicit val BytesAsInput: AsInput[Bytes] = new AsInput[Bytes] {
//    def inputStream(b: Bytes) = valLogIO success b
//    def readError(b: Bytes) = "Error when reading bytes"
//    def writeError(b: Bytes) = "Error when reading bytes"
//  }
//}

// vim: set ts=2 sw=2 et:
