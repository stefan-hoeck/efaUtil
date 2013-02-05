package efa.nb

import shapeless.{HNil, ::}

package object node {
  type FullChild = Child :: Parent :: HNil
}

// vim: set ts=2 sw=2 et:
