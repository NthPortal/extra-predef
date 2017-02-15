package com.nthportal

/**
  * An extra `Predef` which has more methods and implicit classes
  * than [[scala.Predef]].
  *
  * For an extra predef without any implicit classes, use
  * [[ExtraPredefCore]] instead.
  *
  * @see [[ExtraPredefCore]]
  */
trait ExtraPredef extends ExtraPredefCore {
  implicit final class ExtraRichExtendsAny[A](private val a: A) {
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

  implicit final class ExtraRichOrderedInt(private val prev: Int) {
    def thenCompare[A](a1: A, a2: A)(implicit ord: Ordering[A]): Int = {
      if (prev != 0) prev else ord.compare(a1, a2)
    }
  }
}
