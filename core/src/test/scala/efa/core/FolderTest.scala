package efa.core

import org.scalacheck._, Prop._
import scalaz._, Scalaz._, scalacheck.ScalaCheckBinding._

object FolderTest extends Properties("Folder") {
  type TestF = Folder[Int,String]

  def streamGen[A] (g: Gen[A]) = for {
    c ← Gen choose (1, 3)
    as ← Gen listOfN (c, g)
  } yield as.toStream

  val dataGen = Gen choose (0, Int.MaxValue)
  val datasGen = streamGen(dataGen)
  val lblGen = Gen.identifier
  val leafGen = ^(datasGen, lblGen) (Folder(_, Stream.empty, _))
  val l1Gen = ^(datasGen, streamGen(leafGen), lblGen) (Folder.apply)
  val rootGen = ^(datasGen, streamGen(l1Gen), lblGen) (Folder.apply)

  val rootDataGen = for {
    r ← rootGen
    d ← Gen oneOf r.allData
  } yield (r, d)

  val rootFolderGen = for {
    r ← rootGen
    f ← Gen oneOf r.allFolders
  } yield (r, f)
  
  implicit val TestFArbitrary: Arbitrary[TestF] = Arbitrary(rootGen)

  property("equal") = forAll {tf: TestF ⇒ tf ≟ tf}

  property("map") = forAll {tf: TestF ⇒ tf.map(identity) ≟ tf}

  property("mapLabel") = forAll {tf: TestF ⇒ tf.mapLabel(identity) ≟ tf}

  property("find") = Prop.forAll(rootDataGen) {p ⇒ 
    val (r, d) = p
    (r find (d≟)) ≟ d.some
  }

  property("findFolder") = Prop.forAll(rootFolderGen) {p ⇒ 
    val (r, d) = p
    (r findFolder (d≟)) ≟ d.some
  }

  property("filter") = Prop.forAll(rootDataGen) {p ⇒ 
    val (r, d) = p
    (r filter (d≠) find (d ≟ )) ≟ none
  }

  property("filterFolder") = Prop.forAll(rootFolderGen) {p ⇒ 
    val (r, d) = p
    (d ≟ r) ||
    ((r filterFolder (d≠) findFolder (d ≟ )) ≟ none)
  }

  property("remove") = Prop.forAll(rootDataGen) {p ⇒ 
    val (r, d) = p
    (r remove d find (d ≟ )) ≟ none
  }

  property("removeFolder") = Prop.forAll(rootFolderGen) {p ⇒ 
    val (r, d) = p
    (d ≟ r) ||
    ((r removeFolder d findFolder (d ≟ )) ≟ none)
  }

  property("update") = Prop.forAll {p: (TestF, Int) ⇒ 
    val (f, i) = p
    val first = f.allData.head
    val updated = f update (first, i)
    
    (updated.find(first≟) ≟ none) :| "removed" &&
    (updated.allData.head ≟ i) :| "replaced" 
  }

  property("updateFolder") = Prop.forAll {p: (TestF, String) ⇒ 
    val (f, s) = p
    val newF = Folder[Int,String](Stream.empty, Stream.empty, s)
    val last = f.allFolders.last
    val updated = f updateFolder (last, newF)
    
    (updated.findFolder(last≟) ≟ none) :| "removed" &&
    (updated.allFolders.last ≟ newF) :| "replaced" 
  }
}

// vim: set ts=2 sw=2 et:
