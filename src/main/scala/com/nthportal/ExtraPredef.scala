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
}
