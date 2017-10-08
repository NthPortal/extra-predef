package com.nthportal.extrapredef

/**
  * [[ExtraPredef]] sans implicit classes, because implicit classes
  * in traits cannot extend [[AnyVal]].
  */
trait ExtraPredefCore {
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
}
