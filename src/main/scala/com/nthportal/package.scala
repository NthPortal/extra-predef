package com

package object nthportal extends ExtraPredefCore {
  implicit final class ExtraRichNullable[A](private val a: A) extends AnyVal {
    /**
      * Asserts that `this` is not `null`.
      *
      * @throws NullPointerException if `this` is `null`
      * @return `this`, if it is not `null`
      */
    @throws[NullPointerException]
    def nonNull: A = {
      if (a == null) throw new NullPointerException("ExtraRichNullable.nonNull")
      a
    }

    def orIfNull(value: A): A = if (a != null) a else value

    @inline
    def ??(value: A): A = orIfNull(value)
  }

  implicit final class ExtraRichOrderedInt(private val prev: Int) extends AnyVal {
    def thenCompare[A](a1: A, a2: A)(implicit ord: Ordering[A]): Int = {
      if (prev != 0) prev else ord.compare(a1, a2)
    }
  }

  implicit final class ExtraRichOrdering[T](private val ord: Ordering[T]) extends AnyVal {
    def thenOrderingBy[S: Ordering](f: T => S): Ordering[T] = (x, y) => ord.compare(x, y).thenCompare(f(x), f(y))

    @inline
    def thenBy[S: Ordering](f: T => S): Ordering[T] = thenOrderingBy(f)
  }
}
