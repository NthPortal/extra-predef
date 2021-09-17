package com.nthportal.extrapredef.math

import scala.language.implicitConversions

object OrderingExtras {

  trait ExtraOps[T] {
    self: Ordering[T]#Ops =>

    /**
     * Returns `true` if `lhs` and `rhs` are unequal by their [[java.lang.Comparable natural ordering]]; `false`
     * otherwise. (That is, returns `true` if `lhs` is greater than or less than `rhs`.)
     *
     * @param rhs
     *   the thing to which to compare `lhs`
     * @return
     *   `true` if `lhs` and `rhs` are unequal by their natural ordering
     */
    def <>(rhs: T): Boolean = !this.equiv(rhs)

    /**
     * Returns `true` if `lhs` and `rhs` are equal by their [[java.lang.Comparable natural ordering]]; `false`
     * otherwise. (That is, returns `true` if `lhs` is neither greater than nor less than `rhs`.)
     *
     * @param rhs
     *   the thing to which to compare `lhs`
     * @return
     *   `true` if `lhs` and `rhs` are equal by their natural ordering
     */
    def !<>(rhs: T): Boolean = this.equiv(rhs)
  }

  /**
   * This object contains a superset of the implicits found in [[Ordering.Implicits]].
   */
  object Implicits extends Ordering.ExtraImplicits {
    override implicit def infixOrderingOps[T](x: T)(implicit ord: Ordering[T]): Ordering[T]#Ops with ExtraOps[T] = {
      new ord.Ops(x) with ExtraOps[T]
    }
  }

}
