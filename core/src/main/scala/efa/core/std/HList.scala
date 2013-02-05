package efa.core.std

import efa.core._
import shapeless._
  
trait HListInstances {
  implicit def HListNamed[H:Named,T<:HList]: Named[H :: T] = 
    new Named[H :: T]{ def name (a: H :: T) = Named[H] name a.head }

  implicit def HListUniqueId[H,T<:HList,Id](implicit u: UniqueId[H,Id])
    : UniqueId[H :: T,Id] = UniqueId get { u id _.head }

  implicit def HListParent[H,T<:HList,F[_],B](implicit p: Parent[F,H,B])
    : Parent[F,H :: T,B] = p contramap { _.head } 
}

object hList extends HListInstances

// vim: set ts=2 sw=2 et:
