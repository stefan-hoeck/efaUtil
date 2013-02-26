package efa.io

import scalaz.effect.{IO, Resource}
import java.io._

trait ResourceFunctions {
  def resource[A](cl: A ⇒ Unit): Resource[A] = 
    new Resource[A] { def close(a: A) = IO(cl(a)) }
}

trait ResourceInstances {
  import resource.{resource ⇒ r}

  implicit val InputStreamResource = r[InputStream](_.close())

  implicit val OutputStreamResource = r[OutputStream](_.close())

  implicit val OutputStreamWriterResource = r[OutputStreamWriter](_.close())

  implicit val ReaderResource = r[Reader](_.close())

  implicit val WriterResource = r[Writer](_.close())
}

object resource extends ResourceInstances with ResourceFunctions

// vim: set ts=2 sw=2 et:
