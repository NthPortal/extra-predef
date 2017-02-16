package com.nthportal.extrapredef

import scala.concurrent.Future
import scala.util.Try

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

    /**
      * Returns `this` if `this` is not `null`, or a default value otherwise.
      *
      * This method is `null`-coalescing.
      *
      * @param value a default value if `this` is `null`
      * @return `this` if `this` is not `null`, or the default value otherwise
      * @see [[??]]
      */
    def orIfNull(value: A): A = if (a != null) a else value

    /**
      * Returns `this` if `this` is not `null`, or a default value otherwise.
      *
      * This method is a `null`-coalescing operator, and is equivalent to [[orIfNull]].
      *
      * @param value a default value if `this` is `null`
      * @return `this` if `this` is not `null`, or the default value otherwise
      * @see [[orIfNull]]
      */
    @inline
    def ??(value: A): A = orIfNull(value)
  }

  implicit final class ExtraRichOrderedInt(private val prev: Int) {
    /**
      * Returns the result of the immediately previous comparison if it was not `0`;
      * otherwise the result of comparing two other things (`a1` and `a2`).
      *
      * This method is intended to be used to chain comparisons, as in the
      * following example:
      *
      * {{{
      * case class PlayingCard(rank: Rank, suit: Suit) extends Ordered[PlayingCard] {
      *   override def compare(other: PlayingCard): Int = {
      *     (this.rank compare that.rank)
      *       .thenCompare(this.suit, that.suit)
      *   }
      * }
      * }}}
      *
      * While this method can called on arbitrary [[Int]]s, to be meaningful,
      * it should only be called on the result of a previous comparison.
      *
      * Because the implicit class containing this method does not extend [[AnyVal]],
      * invoking this method results in object creation. Consequently, it is almost
      * certainly more efficient to compare tupled elements instead, as in the following
      * modification of the above example:
      *
      * {{{
      * case class PlayingCard(rank: Rank, suit: Suit) extends Ordered[PlayingCard] {
      *   override def compare(other: PlayingCard): Int = {
      *     (this.rank, this.suit) compare (that.rank, that.suit)
      *   }
      * }
      * }}}
      *
      * If one wants to use this method efficiently, it is recommended to copy the code
      * from [[com.nthportal.ExtraRichOrderedInt the `com.nthportal` package object]]
      * (which extends [[AnyVal]]) and paste it where needed.
      *
      * @param a1  the first thing to compare
      * @param a2  the second thing to compare
      * @param ord an [[Ordering]] for `a1` and `a2`
      * @tparam A the type of `a1` and `a2`
      * @return the result of the previous comparison if it was not `0`, or the result of
      *         comparing the two other things otherwise
      */
    def thenCompare[A](a1: A, a2: A)(implicit ord: Ordering[A]): Int = {
      if (prev != 0) prev else ord.compare(a1, a2)
    }
  }

  implicit final class ExtraRichOrdering[T](private val ord1: Ordering[T]) {
    /**
      * Returns a new [[Ordering]] which compares elements by applying a function (`f`)
      * to them if this Ordering returned `0` when comparing them.
      *
      * This method is intended to be used to build more complex Orderings from
      * other Orderings, as in the following example:
      *
      * {{{
      * case class PlayingCard(rank: Rank, suit: Suit)
      *
      * object PlayingCard {
      *   val rankOnlyOrdering: Ordering[PlayingCard] = Ordering.by(_.rank)
      *   val fullOrdering: Ordering[PlayingCard] = rankOnlyOrdering.thenOrderingBy(_.suit)
      * }
      * }}}
      *
      * @param f a function mapping elements to some value to be compared
      * @tparam S the return type of `f`
      * @return a new [[Ordering]] which compares elements by applying a function to them
      *         if this Ordering returned `0` when comparing them
      */
    def thenOrderingBy[S](f: T => S)(implicit ord2: Ordering[S]): Ordering[T] = (x, y) => {
      val res1 = ord1.compare(x, y)
      if (res1 != 0) res1 else ord2.compare(f(x), f(y))
    }

    /**
      * Returns a new [[Ordering]] which compares elements by applying a function (`f`)
      * to them if this Ordering returned `0` when comparing them.
      *
      * This method is an alias of [[thenOrderingBy]], intended to be used
      * when chaining several calls in order to reduce verbosity, as in the
      * following example:
      *
      * {{{
      * case class IntTuple(a: Int, b: Int, c: Int, d: Int)
      *
      * object IntTuple {
      *   val ordering: Ordering[IntTuple] =
      *     Ordering.by[IntTuple, Int](_.a)
      *       .thenBy(_.b)
      *       .thenBy(_.c)
      *       .thenBy(_.d)
      * }
      * }}}
      *
      * @param f a function mapping elements to some value to be compared
      * @tparam S the return type of `f`
      * @return a new [[Ordering]] which compares elements by applying a function to them
      *         if this Ordering returned `0` when comparing them
      * @see [[thenOrderingBy]]
      */
    @inline
    def thenBy[S: Ordering](f: T => S): Ordering[T] = thenOrderingBy(f)
  }

  implicit final class ExtraRichOption[A](private val opt: Option[A]) {
    /**
      * Returns a completed [[Future]] from this [[Option]].
      *
      * The Future returned:
      *  - succeeds with the value of this Option if this Option is defined
      *  - fails with a `NoSuchElementException` if this Option is empty
      *
      * @return a completed Future from this Option
      */
    def toFuture: Future[A] = Future.fromTry(Try(opt.get))

    /**
      * Returns an [[Option]] containing the specified value if this Option
      * is empty, or an empty Option if this Option is defined.
      *
      * @param value the value to use if this Option is empty
      * @tparam B the type of the returned Option
      * @return an Option containing the specified the specified value if this
      *         Option is empty, or an empty Option if this Option is defined
      */
    def invert[B](value: => B): Option[B] = invertWith(Some(value))

    /**
      * Returns the specified [[Option]] if this Option is empty, or an empty
      * Option if this Option is defined.
      *
      * @param option the option to return if this Option is empty
      * @tparam B the type of the returned Option
      * @return the specified Option if this Option is empty, or an empty
      *         Option if this Option is defined
      */
    def invertWith[B](option: => Option[B]): Option[B] = if (opt.isEmpty) option else None
  }
}
