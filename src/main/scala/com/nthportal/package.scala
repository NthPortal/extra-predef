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
}
