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
    def orIfNull[B >: A](value: => B): B = if (a != null) a else value

    /**
      * Returns `this` if `this` is not `null`, or a default value otherwise.
      *
      * This method is a `null`-coalescing operator, and is an alias of [[orIfNull]].
      *
      * @param value a default value if `this` is `null`
      * @return `this` if `this` is not `null`, or the default value otherwise
      * @see [[orIfNull]]
      */
    @inline
    def ??[B >: A](value: => B): B = orIfNull(value)
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

  implicit final class ExtraRichOrdered[A <: Ordered[A]](private val a: A) {
    /**
      * Returns `true` if `this` and `that` are unequal by their
      * [[java.lang.Comparable natural ordering]]; `false` otherwise.
      * (That is, returns `true` if `this` is greater than or less than `that`.)
      *
      * @param that the thing to which to compare `this`
      * @return `true` if `this` and `that` are unequal by their natural ordering
      */
    def <>(that: A): Boolean = (a compare that) != 0

    /**
      * Returns `true` if `this` and `that` are equal by their
      * [[java.lang.Comparable natural ordering]]; `false` otherwise.
      * (That is, returns `true` if `this` is neither greater than nor less
      * than `that`.)
      *
      * @param that the thing to which to compare `this`
      * @return `true` if `this` and `that` are equal by their natural ordering
      */
    def !<>(that: A): Boolean = (a compare that) == 0
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

  implicit final class ExtraRichOption[+A](private val opt: Option[A]) {
    /**
      * Returns a [[Try]] from this [[Option]].
      *
      * The Try returned is a [[scala.util.Success Success]] with the value of this
      * Option if this Option is defined, or a [[scala.util.Failure Failure]] with
      * a `NoSuchElementException` if this Option is empty.
      *
      * @return a Try from this Option
      */
    def toTry: Try[A] = Try {opt.get}

    /**
      * Returns a completed [[Future]] from this [[Option]].
      *
      * The Future returned succeeds with the value of this Option if this Option
      * is defined, or fails with a `NoSuchElementException` if this Option is empty.
      *
      * @return a completed Future from this Option
      */
    def toFuture: Future[A] = Future.fromTry(toTry)

    /**
      * Returns an [[Option]] by applying `ifDefined` to the value of this
      * Option if this Option is defined, or `ifEmpty` if this Option is empty.
      *
      * @param ifDefined a function to apply to the value of this Option
      *                  if it is defined
      * @param ifEmpty   an Option to return if this Option is empty
      * @tparam B the type of the Option returned
      * @return an Option by applying `ifDefined` to the value of this Option
      *         if this Option is defined, or `ifEmpty` if this Option is empty
      */
    def transform[B](ifDefined: A => Option[B], ifEmpty: => Option[B]): Option[B] = {
      if (opt.isDefined) ifDefined(opt.get) else ifEmpty
    }

    /**
      * Returns an [[Option]] containing the specified value if this Option
      * is empty, or an empty Option if this Option is defined.
      *
      * @param ifEmpty the value to use if this Option is empty
      * @tparam B the type of the returned Option
      * @return an Option containing the specified the specified value if this
      *         Option is empty, or an empty Option if this Option is defined
      */
    def invert[B](ifEmpty: => B): Option[B] = invertWith(Some(ifEmpty))

    /**
      * Returns the specified [[Option]] if this Option is empty, or an empty
      * Option if this Option is defined.
      *
      * @param ifEmpty the option to return if this Option is empty
      * @tparam B the type of the returned Option
      * @return the specified Option if this Option is empty, or an empty
      *         Option if this Option is defined
      */
    def invertWith[B](ifEmpty: => Option[B]): Option[B] = if (opt.isEmpty) ifEmpty else None
  }

  implicit final class ExtraRichTry[+A](private val t: Try[A]) {
    /**
      * Returns a completed [[Future]] from this [[Try]].
      *
      * The Future returned succeeds with the value of this Try if it is a
      * [[scala.util.Success Success]], or fails with a `NoSuchElementException` if
      * this Try is a [[scala.util.Failure Failure]].
      *
      * @return a Future from this Try
      * @see [[Future.fromTry]]
      */
    def toFuture: Future[A] = Future.fromTry(t)
  }

  implicit final class ExtraRichEither[+A, +B](private val either: Either[A, B]) {
    /**
      * Returns a completed [[Future]] from this [[Either]].
      *
      * The Future returned succeeds with the `Right` value if this Either
      * is a [[scala.util.Right Right]], or fails with the `Left` value if
      * this Either is a [[scala.util.Left Left]].
      *
      * @return a Future from this Either
      */
    def toFuture(implicit ev: A <:< Throwable): Future[B] = Future.fromTry(either.toTry)
  }
}
