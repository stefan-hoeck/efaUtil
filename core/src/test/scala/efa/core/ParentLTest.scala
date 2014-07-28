package efa.core

import org.scalacheck._, Prop._
import scalaz._, Scalaz._
import shapeless.{HNil, ::}

object ParentLTest extends Properties("ParentL") {
  property("leaf_equal") = forAll { l: Leaf ⇒ l ≟ l }

  property("branch_equal") = forAll { l: Branch ⇒ l ≟ l }

  property("root_equal") = forAll { l: Root ⇒ l ≟ l }

  property("uniqueness") = forAll { r: Root ⇒ 
    def uniqueLeaves (b: Branch) = 
      b.leaves.toList.map { _._2.id }.sorted ≟ 
      b.leaves.keySet.toList.sorted

    (r.branches.map { _.id }.toSet.size ≟ r.branches.size) &&
    (r.branches ∀ uniqueLeaves)
  }

  property("add_branch") = forAll { p: (Root,Branch) ⇒ 
    val (r, b) = p
    val id = r.branches.size
    val exp = Root.branches mod (Branch.id.set(b, id) :: _, r)
    val res = Root.RootParent.addUnique[Int](r :: HNil, b) exec r
    
    exp ≟ res
  }

  property("update_branch") = forAll { p: (Root,Branch) ⇒ 
    val (r, b) = p
    val head = r.branches.head
    val id = head.id
    val replace = Branch.id.set(b, id)
    val exp = Root.branches.mod(replace :: _.tail, r)
    val res = Root.RootParent.update(head :: r :: HNil, replace).exec(r)
    
    exp ≟ res
  }

  property("delete_branch") = forAll { r: Root ⇒ 
    val head = r.branches.last
    val exp = Root.branches.mod(_.init, r)
    val res = Root.RootParent.delete(head :: r :: HNil) exec r

    exp ≟ res
  }

  property("add_leaf") = forAll { p: (Root,Leaf) ⇒ 
    val (r, l) = p
    val b = r.branches.head
    val id = b.leaves.size
    val newBranch = Branch.leaves.mod(_ + (id → Leaf.id.set(l, id)), b)
    val exp = Root.branches.mod(newBranch :: _.tail, r)
    val res = Branch.BranchParent.addUnique[Int](b :: r :: HNil, l) exec r

    exp ≟ res
  }

  property("update_leaf") = forAll { p: (Root,Leaf) ⇒ 
    val (r, l) = p
    val b = r.branches.head
    val oldLeaf = b.leaves.last._2
    val id = oldLeaf.id
    val newLeaf = Leaf.id.set(l, id)
    val newBranch = Branch.leaves.mod(_ + (id → newLeaf), b)
    val exp = Root.branches.mod(newBranch :: _.tail, r)
    val res = Branch.BranchParent.update(oldLeaf :: b :: r :: HNil, newLeaf) exec r

    exp ≟ res
  }

  property("delete_leaf") = forAll { p: (Root,Leaf) ⇒ 
    val (r, l) = p
    val b = r.branches.head
    val goner = b.leaves.head._2
    val id = goner.id
    val newBranch = Branch.leaves.mod(_ - id, b)
    val exp = Root.branches.mod(newBranch :: _.tail, r)
    val res = Branch.BranchParent.delete(goner :: b :: r :: HNil) exec r

    exp ≟ res
  }
}

// vim: set ts=2 sw=2 et:
