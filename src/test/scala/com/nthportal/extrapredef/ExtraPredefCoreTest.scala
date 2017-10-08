package com.nthportal.extrapredef

import org.scalatest.{FlatSpec, Matchers}

class ExtraPredefCoreTest extends FlatSpec with Matchers {
  private val predef = new ExtraPredefCore {}

  import predef._

  behavior of "ExtraPredefCore"

  it should "require state correctly" in {
    an [IllegalStateException] should be thrownBy {requireState(false)}
    noException should be thrownBy {requireState(true)}

    an [IllegalStateException] should be thrownBy {requireState(requirement = false, "message")}
    noException should be thrownBy {requireState(requirement = true, "message")}
  }

  it should "handle impossible conditions correctly" in {
    an [AssertionError] should be thrownBy { !!! }
  }
}
