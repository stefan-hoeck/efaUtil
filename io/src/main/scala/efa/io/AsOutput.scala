package efa.io

import java.io._

trait AsOutput[-A] {
  def outputStream(a: A): ValLogIO[OutputStream]

  def writeError(a: A): String
}

object AsOutput {
  def apply[A:AsOutput]: AsOutput[A] = implicitly
}

// vim: set ts=2 sw=2 et:
