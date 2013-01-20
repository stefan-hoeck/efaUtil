package efa.nb.node

import efa.data.{UniqueId, Named, IntId, LongId}
import efa.react._
import org.openide.nodes.{Children, Node}
import scalaz._, Scalaz._, effect.IO

final class NbChildren private() extends Children.Keys[NodeSetter] {
  import NbChildren._

  override def createNodes (ns: NodeSetter) = Array(NbNode createIO ns)

  import scala.collection.JavaConversions._

  override protected def addNotify() {
    if (!addNotified) {
      addNotified = true
      setKeys(seq)
    }
  }

  private[node] def set (np: FullInfo): IO[Unit] = IO {
    map = np._1
    seq = np._2
    if (addNotified) setKeys (seq)
  }

  private[this] var addNotified = false
  private[this] var map: FullMap = Map.empty
  private[this] var seq: Setters = IndexedSeq.empty

  private[node] def mapAt (i: Int): IO[SetterMap] =
    IO (map get i getOrElse Map.empty)

  private[node] def children: IO[Setters] = IO (seq)
}

object NbChildren extends NbChildrenFunctions {
  val create: IO[NbChildren] = IO (new NbChildren)
}

trait NbChildrenFunctions {
  type Info[+A,+B,K] = (Map[K,A],IndexedSeq[B])

  type Setter = NodeSetter

  //Map from unique index to corresponding node
  type SetterMap = Map[Any, Setter] 

  //Sequence of index-node pair
  type Setters = IndexedSeq[Setter]

  type SetterInfo = (SetterMap, Setters)
  
  //Handles (at least part of) the Children of type A
  //Creates a (modified) sequence of nodes from a value of type A
  //and a SourceMap
  //Side effects are a possibility, since some of the nodes
  //might be adjusted during creation of the SourceSeq
  type Factory[-A,+B] = (Out[B], A, SetterMap) ⇒ IO[SetterInfo]

  //Maps and Int-Index to a OutSourceMap
  type FullMap = Map[Int, SetterMap]
 
  type FullInfo = (FullMap, Setters)  

  implicit def InfoMonoid[A,B,K] = new Monoid[Info[A,B,K]] {
    val zero: Info[A,B,K] = (Map.empty, IndexedSeq.empty)
    def append (a: Info[A,B,K], b: ⇒ Info[A,B,K]) =
      (a._1 ++ b._1, a._2 ++ b._2)
  }

  implicit def FactoryFunctor[R] =
    new Functor[({type λ[α]=Factory[R,α]})#λ] {
    def map[A,B] (nf: Factory[R,A])(f: A ⇒ B): Factory[R,B] =
      (ob,a,m) ⇒ nf (ob compose f,a,m)
  }

  implicit def FactoryContravariant[R] =
    new Contravariant[({type λ[α]=Factory[α,R]})#λ] {
    def contramap[A,B] (nf: Factory[A,R])(f: B ⇒ A): Factory[B,R] =
      (ob,a,m) ⇒ nf (ob,f(a),m)
  }

  /**
   * Displays a single value of type A in a Node.
   * If the value changes, no new Node is created, but
   * the old Node is overwritten. This will keep it
   * expanded if it was so before.
   */
  def singleF[A,B] (out: NodeOut[A,B]): Factory[A,B] =
    uniqueIdF[A,B,Unit](out)(UniqueId.trivial) ∙ (List(_))

  /**
   * Displays a list of objects each in a Node.
   * New nodes are created every time the sequence
   * changes, therefore this factory is usually not
   * well suited for Nodes that have themselves children,
   * since those will be in a collapsed state whenever
   * a new sequence is displayed, no matter what the
   * previos state of the nodes where.
   */
  def listF[A,B] (out: NodeOut[A,B]): Factory[List[A],B] = (ob,as, _) ⇒ {
    def setter (a: A) = create (out)(ob, a)
    def toPair(ss: List[Setter]): SetterInfo = (Map.empty, ss.toIndexedSeq)

    as traverse setter map toPair
  }

  /**
   * Displays a List of values with a unique Long as id number.
   */
  def longIdF[A:LongId,B](out: NodeOut[A,B]): Factory[List[A],B] =
    uniqueIdF(out)

  /**
   * Displays a List of values with a unique Int as id number.
   */
  def intIdF[A:IntId,B](out: NodeOut[A,B]): Factory[List[A],B] =
    uniqueIdF(out)

  /**
   * Displays a List of values with a unique Long as id number. Values
   * are sorted by name before being displayed.
   */
  def longIdNamedF[A:LongId:Named,B](out: NodeOut[A,B]): Factory[List[A],B] =
    longIdF(out) ∙ Named[A].nameSort

  /**
   * Displays a List of values with a unique Int as id number. Values
   * are sorted by name before being displayed.
   */
  def intIdNamedF[A:IntId:Named,B](out: NodeOut[A,B]): Factory[List[A],B] =
    intIdF(out) ∙ Named[A].nameSort

  /**
   * Displays a sequence of objects each in a Node.
   * There must be a unique id available for each object.
   * Nodes are reused for other objects with the same
   * unique id. This prevents that an expanded node
   * is collapsed when the sequence changes an the
   * node's object is updated.
   *
   * The method does not check whether each id in the
   * sequence is indeed unique. This might lead to
   * unexpected behavior if ids are not indeed unique.
   */
  def uniqueIdF[A,B,I] (out: NodeOut[A,B])(implicit u: UniqueId[A,I])
    : Factory[List[A],B] = pairListF[I,A,B](out) ∙ u.pairList

  /**
   * Displays a map in nodes. Since maps are unordered, values are sorted
   * alphabetically.
   */
  def mapF[A,B:Named,C](out: NodeOut[B,C]): Factory[Map[A,B],C] =
    pairListF[A,B,C](out) ∙ Named[B].sortedPairs[A]

  /**
   * Displays a list of key - value pairs in nodes. Existing nodes for
   * a given key A are reused.
   *
   * Note that key values must be unique, otherwise the behavior is
   * undefined.
   */
  def pairListF[A,B,C] (out: NodeOut[B,C]): Factory[List[(A,B)],C] = 
    (oc, abs, m) ⇒ {
      def setterPair (p: (A,B)): IO[(Any,NodeSetter)] =
        m get p._1 cata (
          display (out)(oc, p._2),
          create(out)(oc, p._2)
        ) strengthL p._1
      
      for {
        pairList ← abs traverse setterPair
      } yield (pairList.toMap, pairList.toIndexedSeq map (_._2))
    }

  def children[A,B] (fs: Factory[A,B]*): NodeOut[A,B] = NodeOut(
    (ob, n) ⇒ a ⇒ {

      def single (p: (Factory[A,B],Int)): IO[FullInfo] = for {
        sm ← n.hc mapAt p._2
        si ← p._1 (ob, a, sm)
      } yield (Map (p._2 -> si._1), si._2)

      fs.toList.zipWithIndex foldMap single flatMap n.hc.set
    }
  )

  private def display[A,B] (no: NodeOut[A,B])(ob: Out[B], a: A)(s: NodeSetter) =
    s setOut (n ⇒ no.run(ob, n)(a)) as s

  private def create[A,B] (no: NodeOut[A,B])(ob: Out[B], a: A) =
    NodeSetter.apply >>= display(no)(ob, a)
}

// vim: set ts=2 sw=2 et:
