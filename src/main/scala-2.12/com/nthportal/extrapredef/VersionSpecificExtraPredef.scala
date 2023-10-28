package com.nthportal.extrapredef

/**
 * Internal implementation detail of [[ExtraPredef]] to handle differences between Scala versions.
 */
trait VersionSpecificExtraPredef {
  import VersionSpecificExtraPredef._

  implicit def toOrderingOps[A](self: Ordering[A]): OrderingOps[A] = new OrderingOps(self)
}

object VersionSpecificExtraPredef {
  /*
   * Scala (https://www.scala-lang.org)
   *
   * Copyright EPFL and Lightbend, Inc.
   *
   * Licensed under Apache License 2.0
   * (http://www.apache.org/licenses/LICENSE-2.0).
   *
   * See the NOTICE file distributed with this work for
   * additional information regarding copyright ownership.
   */
  final class OrderingOps[T](private val self: Ordering[T]) extends AnyVal {

    /**
     * Creates an Ordering[T] whose compare function returns the result of this Ordering's compare function, if it is
     * non-zero, or else the result of `other`s compare function.
     *
     * @example
     *   {{{
     * case class Pair(a: Int, b: Int)
     *
     * val pairOrdering = Ordering.by[Pair, Int](_.a)
     *                             .orElse(Ordering.by[Pair, Int](_.b))
     *   }}}
     *
     * @param other
     *   an Ordering to use if this Ordering returns zero
     */
    def orElse(other: Ordering[T]): Ordering[T] =
      (x, y) => {
        val res1 = self.compare(x, y)
        if (res1 != 0) res1 else other.compare(x, y)
      }

    /**
     * Given f, a function from T into S, creates an Ordering[T] whose compare function returns the result of this
     * Ordering's compare function, if it is non-zero, or else a result equivalent to:
     *
     * {{{
     * Ordering[S].compare(f(x), f(y))
     * }}}
     *
     * This function is equivalent to passing the result of `Ordering.by(f)` to `orElse`.
     *
     * @example
     *   {{{
     * case class Pair(a: Int, b: Int)
     *
     * val pairOrdering = Ordering.by[Pair, Int](_.a)
     *                             .orElseBy[Int](_.b)
     *   }}}
     */
    def orElseBy[S](f: T => S)(implicit ord: Ordering[S]): Ordering[T] =
      (x, y) => {
        val res1 = self.compare(x, y)
        if (res1 != 0) res1 else ord.compare(f(x), f(y))
      }
  }
}
