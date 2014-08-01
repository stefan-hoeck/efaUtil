package efa.core

import std.state.toState
import Shapeless._
import scalaz._, Scalaz._

/**
 * A data structure to can be used to represent an
 * entry in a filesystem with a label and
 * an arbitrary number of data items and subfolders
 */
case class Folder[+A,+B](
  data: Stream[A],
  folders: Stream[Folder[A,B]],
  label: B
) {

  lazy val allData: Stream[A] = data #::: (folders flatMap (_.allData))

  lazy val allFolders: Stream[Folder[A,B]] =
    this #:: (folders flatMap (_.allFolders))

  def map[C] (f: A ⇒ C): Folder[C,B] =
    Folder(data map f, folders map (_ map f), label)

  def mapLabel[C] (f: B ⇒ C): Folder[A,C] =
    Folder(data, folders map (_ mapLabel f), f(label))

  def |+|[C>:A,D>:B:Semigroup](that: Folder[C,D]): Folder[C,D] = Folder (
    data #::: that.data, folders #::: that.folders, (label: D) ⊹ that.label
  )

  def filter (p: A ⇒ Boolean): Folder[A,B] =
    Folder(data filter p, folders map (_ filter p), label)

  def filterFolder (p: Folder[A,B] ⇒ Boolean): Folder[A,B] =
    Folder(data, folders filter p map (_ filterFolder p), label)

  def find (p: A ⇒ Boolean): Option[A] = allData find p

  def findFolder (p: Folder[A,B] ⇒ Boolean): Option[Folder[A,B]] =
    allFolders find p

  def add[C>:A] (c: C): Folder[C,B] =
    Folder(c #:: (data: Stream[C]), folders, label)

  def addFolder[C>:A,D>:B] (f: Folder[C,D]): Folder[C,D] =
    Folder(data, f #:: (folders: Stream[Folder[C,D]]), label)

  def remove[C>:A:Equal] (c: C): Folder[C,B] = filter (c≠)

  def removeFolder[C>:A:Equal,D>:B:Equal] (f: Folder[C,D]): Folder[C,D] =
    filterFolder (f≠)

  def update[C>:A:Equal] (o: C, n: C): Folder[C,B] = updateWhere(n)(o≟ )

  def updateWhere[C>:A] (n: C)(p: C ⇒ Boolean): Folder[C,B] =
    map ((a: C) ⇒ p(a) ? n | a)
  
  def updateFolder[C>:A:Equal,D>:B:Equal] (o: Folder[C,D], n: Folder[C,D])
  : Folder[C,D] =
    ((this: Folder[C,D]) ≟ o) ?
    n |
    Folder(data, folders map (_.updateFolder(o, n)), label)
}

object Folder {
  self ⇒ 

  def leaf[D,L](lbl: L) = Folder[D,L](Stream.empty, Stream.empty, lbl)

  implicit class FolderLenses[A,D,L](val lens: A @> Folder[D,L]) extends AnyVal {

    def label = lens >=> self.label
    
    def folders = lens >=> self.folders

    def data = lens >=> self.data

    def removeFolder (f: Folder[D,L])(implicit D: Equal[D], L: Equal[L])
      : State[A,Unit] = lens mods_ (_ removeFolder f)
    
    def remove(d: D)(implicit D: Equal[D]): State[A,Unit] =
      lens mods_ (_ remove d)

    def updateFolder (o: Folder[D,L], n: Folder[D,L])(
      implicit D: Equal[D], L: Equal[L]): State[A,Unit] =
      lens mods_ (_ updateFolder (o, n))

    def update(o: D, n: D)(implicit D: Equal[D]): State[A,Unit] =
      lens mods_ (_ update (o, n))

    def updateWhere(n: D)(p: D ⇒ Boolean): State[A,Unit] =
      lens mods_ (_.updateWhere(n)(p))

    def add (d: D, f: Folder[D,L])(
      implicit D: Equal[D], L: Equal[L]): State[A,Unit] = 
      updateFolder (f, f add d)

    def addFolder (newFolder: Folder[D,L], o: Folder[D,L])(
      implicit D: Equal[D], L: Equal[L]): State[A,Unit] =
      updateFolder (o, o addFolder newFolder)
  }

  def label[A,B]: Folder[A,B] @> B=
    Lens.lensu((a,b) ⇒ a.copy(label = b), _.label)

  def data[A,B]: Folder[A,B] @> Stream[A] =
    Lens.lensu((a,b) ⇒ a.copy(data = b), _.data)

  def folders[A,B]: Folder[A,B] @> Stream[Folder[A,B]] =
    Lens.lensu((a,b) ⇒ a.copy(folders = b), _.folders)

  def indexFolders[A,B] (f: Folder[A,B]) : (Int,Folder[A,(B,Int)]) = {
    type IState[X] = State[Int,X]
    val st: B ⇒ IState[(B, Int)] = b ⇒ toState(i ⇒ (i + 1, (b, i)))

    lazy val fSt: Folder[A,B] ⇒ IState[Folder[A,(B,Int)]] = f ⇒  for {
      newLabel   ← st(f.label)
      newFolders ← f.folders traverse fSt
    } yield Folder(f.data, newFolders, newLabel)

    fSt(f) apply 0
  }

  implicit def FolderEqual[A:Equal,B:Equal]: Equal[Folder[A,B]] = deriveEqual

  implicit def FolderTraverse[R]: Traverse[({type λ[α]=Folder[α,R]})#λ] =
    new Traverse[({type λ[α]=Folder[α,R]})#λ] {
      def traverseImpl[G[_]:Applicative,A,B](fa: Folder[A,R])(f: A => G[B])
        : G[Folder[B,R]] = {
        val ds: G[Stream[B]] = fa.data traverse f
        val fs: G[Stream[Folder[B,R]]] =
          fa.folders traverse (traverseImpl(_)(f))
        
        ds ⊛ fs ⊛ fa.label.η[G] apply Folder.apply
      }
    }

  implicit def FolderMonoid[A,B:Monoid]: Monoid[Folder[A,B]] = deriveMonoid
}

// vim: set ts=2 sw=2 et:
