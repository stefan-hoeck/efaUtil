package efa.core

sealed trait Log {
  def msg: String 
  def level: Level
}

object Log  {
  def log (m: ⇒ String, lvl: Level): Log = new Log {
    def msg = m
    def level = lvl
  }

  import Level._
  def trace (msg: ⇒ String) = log (msg, Trace)
  def debug (msg: ⇒ String) = log (msg, Debug)
  def info (msg: ⇒ String) = log (msg, Info)
  def warning (msg: ⇒ String) = log (msg, Warning)
  def error (msg: ⇒ String) = log (msg, Error)
}

// vim: set ts=2 sw=2 et:
