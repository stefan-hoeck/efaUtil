package efa.nb

import efa.react._
import scalaz._, Scalaz._, effect._
import efa.core._

package object node {
  type DataCookie = PureLookup ⇒ Option[IO[Unit]]

  type Destroyer = Option[IO[Unit]]

  type Editor = Option[IO[Unit]]

  type NtInfo = Pair[String,IO[Unit]]

  type Paster = (PasteType, org.openide.nodes.Node) ⇒ IO[Unit]

  type Renamer = Option[String ⇒ IO[Unit]]
}

// vim: set ts=2 sw=2 et:
