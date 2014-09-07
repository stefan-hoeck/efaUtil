package efa.core

/** Similar to the mighty Scalaz._ import, importing Efa._ gives access to
  * (almost) all the goodies of the efa.core module.
  */
object Efa
  extends std.AllFunctions
  with std.AllInstances
  with DefaultFunctions
  with Lenses
  with TypeClassInstances
  with TypeClassFunctions

// vim: set ts=2 sw=2 et:
