package com.nthportal.extrapredef

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
  implicit final class ExtraRichNullable[A](private val a: A) {
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

  implicit final class ExtraRichOrderedInt(private val prev: Int) {
    def thenCompare[A](a1: A, a2: A)(implicit ord: Ordering[A]): Int = {
      if (prev != 0) prev else ord.compare(a1, a2)
    }
  }

  implicit final class ExtraRichOrdering[T](private val ord1: Ordering[T]) {
    def thenOrderingBy[S](f: T => S)(implicit ord2: Ordering[S]): Ordering[T] = (x, y) => {
      val res1 = ord1.compare(x, y)
      if (res1 != 0) res1 else ord2.compare(f(x), f(y))
    }

    @inline
    def thenBy[S: Ordering](f: T => S): Ordering[T] = thenOrderingBy(f)
  }
}
