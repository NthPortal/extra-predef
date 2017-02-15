package com

package object nthportal extends ExtraPredefCore {
  implicit final class ExtraRichExtendsAny[A](private val a: A) extends AnyVal {
    /**
      * Asserts that `this` is not `null`.
      *
      * @throws NullPointerException if `this` is `null`
      * @return `this`, if it is not `null`
      */
    @throws[NullPointerException]
    def nonNull: A = {
      if (a == null) throw new NullPointerException("ExtraRichExtendsAny.nonNull")
      a
    }
  }

  implicit final class ExtraRichOrderedInt(private val prev: Int) extends AnyVal {
    def thenCompare[A](a1: A, a2: A)(implicit ord: Ordering[A]): Int = {
      if (prev != 0) prev else ord.compare(a1, a2)
    }
  }
}
