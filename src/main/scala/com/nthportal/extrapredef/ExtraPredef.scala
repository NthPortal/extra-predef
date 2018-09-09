package com.nthportal.extrapredef

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

/**
  * An extra `Predef` which has more methods and implicit classes
  * than [[scala.Predef]].
  */
trait ExtraPredef {
  import ExtraPredef._

  /**
    * Tests an expression, throwing an [[IllegalStateException]] if false.
    * This method is similar to
    * [[Predef.require(requirement:Boolean):Unit* `Predef.require`]],
    * but tests a state instead of a method argument.
    *
    * @param requirement the expression to test
    * @throws IllegalStateException if the expression is false
    */
  @inline
  @throws[IllegalStateException]
  def requireState(requirement: Boolean): Unit = {
    if (!requirement) throw new IllegalStateException("state requirement failed")
  }

  /**
    * Tests an expression, throwing an [[IllegalStateException]] if false.
    * This method is similar to
    * [[Predef.require(requirement:Boolean,message:=>Any):Unit* `Predef.require`]],
    * but tests a state instead of a method argument.
    *
    * @param requirement the expression to test
    * @param message     a String to include in the failure message
    * @throws IllegalStateException if the expression is false
    */
  @inline
  @throws[IllegalStateException]
  def requireState(requirement: Boolean, message: => Any): Unit = {
    if (!requirement) throw new IllegalStateException("state requirement failed: " + message)
  }

  /** Indicates that a condition or code path is impossible. */
  def impossible: Nothing = throw new AssertionError("purportedly impossible")

  /** Indicates that a condition or code path is impossible.
    *
    * Alias for [[impossible]].
    */
  @inline
  def !!! : Nothing = impossible

  /* implicit conversions to value classes. */

  implicit def asExtraRichNullable[A](self: A): ExtraRichNullable[A] =
    new ExtraRichNullable(self)

  implicit def asExtraRichOrderedInt(prev: Int): ExtraRichOrderedInt =
    new ExtraRichOrderedInt(prev)

  implicit def asExtraRichOrdered[A <: Ordered[A]](self: A): ExtraRichOrdered[A] =
    new ExtraRichOrdered(self)

  implicit def asExtraRichOption[A](self: Option[A]): ExtraRichOption[A] =
    new ExtraRichOption(self)

  implicit def asExtraRichTry[A](self: Try[A]): ExtraRichTry[A] =
    new ExtraRichTry(self)

  implicit def asExtraRichEither[L, R](self: Either[L, R]): ExtraRichEither[L, R] =
    new ExtraRichEither(self)

  implicit def asExtraRichSortedMap[K, V](self: collection.SortedMap[K, V]): ExtraRichSortedMap[K, V] =
    new ExtraRichSortedMap(self)
}

object ExtraPredef extends ExtraPredef {
  final class ExtraRichNullable[A](private val self: A) extends AnyVal {
    /**
      * Asserts that `this` is not `null`.
      *
      * @throws NullPointerException if `this` is `null`
      * @return `this`, if it is not `null`
      */
    @throws[NullPointerException]
    def nonNull: A = {
      if (self == null) throw new NullPointerException("ExtraRichNullable.nonNull")
      self
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
    def orIfNull[B >: A](value: => B): B = if (self != null) self else value

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

  final class ExtraRichOrderedInt(private val prev: Int) extends AnyVal {
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

  final class ExtraRichOrdered[A <: Ordered[A]](private val self: A) extends AnyVal {
    /**
      * Returns `true` if `this` and `that` are unequal by their
      * [[java.lang.Comparable natural ordering]]; `false` otherwise.
      * (That is, returns `true` if `this` is greater than or less than `that`.)
      *
      * @param that the thing to which to compare `this`
      * @return `true` if `this` and `that` are unequal by their natural ordering
      */
    def <>(that: A): Boolean = (self compare that) != 0

    /**
      * Returns `true` if `this` and `that` are equal by their
      * [[java.lang.Comparable natural ordering]]; `false` otherwise.
      * (That is, returns `true` if `this` is neither greater than nor less
      * than `that`.)
      *
      * @param that the thing to which to compare `this`
      * @return `true` if `this` and `that` are equal by their natural ordering
      */
    def !<>(that: A): Boolean = (self compare that) == 0
  }

  final class ExtraRichOption[+A](private val self: Option[A]) extends AnyVal {
    /**
      * Returns a [[Try]] from this [[Option]].
      *
      * The Try returned is a [[scala.util.Success Success]] with the value of this
      * Option if this Option is defined, or a [[scala.util.Failure Failure]] with
      * a `NoSuchElementException` if this Option is empty.
      *
      * @return a Try from this Option
      */
    def toTry: Try[A] = Try { self.get }

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
      if (self.isDefined) ifDefined(self.get) else ifEmpty
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
    def invertWith[B](ifEmpty: => Option[B]): Option[B] = if (self.isEmpty) ifEmpty else None
  }

  final class ExtraRichTry[+A](private val self: Try[A]) extends AnyVal {
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
    def toFuture: Future[A] = Future.fromTry(self)
  }

  final class ExtraRichEither[+L, +R](private val either: Either[L, R]) extends AnyVal {
    /**
      * Returns a completed [[Future]] from this [[Either]].
      *
      * The Future returned succeeds with the `Right` value if this Either
      * is a [[scala.util.Right Right]], or fails with the `Left` value if
      * this Either is a [[scala.util.Left Left]].
      *
      * @return a Future from this Either
      */
    def toFuture(implicit ev: L <:< Throwable): Future[R] = Future.fromTry(either.toTry)
  }

  final class ExtraRichSortedMap[K, +V](private val map: collection.SortedMap[K, V]) extends AnyVal {
    /**
      * Returns `true` if this [[collection.SortedMap `SortedMap`]] contains
      * the same mappings in the same order as another `SortedMap`; `false` otherwise.
      *
      * @param other the other `SortedMap`
      * @return `true` if this `SortedMap` contains the same mappings in the same
      *         order as `other`; `false` otherwise
      */
    def orderedEquals[V1 >: V](other: collection.SortedMap[K, V1]): Boolean = map sameElements other
  }
}
