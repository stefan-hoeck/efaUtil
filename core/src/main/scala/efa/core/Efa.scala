package efa.core

/** Similar to the mighty Scalaz._ import, importing Efa._ gives access to
  * (almost) all the goodies of the efa.core module.
  */
object Efa
  extends std.AllFunctions
  with std.AllInstances
  with syntax.Syntaxes
  with DefaultFunctions
  with DefaultInstances
  with Lenses

// vim: set ts=2 sw=2 et:
