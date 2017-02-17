package com.nthportal.extrapredef.math

import org.scalatest.{FlatSpec, Matchers}

class OrderingExtrasTest extends FlatSpec with Matchers {
  import OrderingExtras.Implicits._

  behavior of "OrderingExtras"

  it should "add natural ordering comparison operators" in {
    (1 <> 2) should be (true)
    (2 <> 1) should be (true)
    (1 <> 1) should be (false)

    (1 !<> 2) should be (false)
    (2 !<> 1) should be (false)
    (1 !<> 1) should be (true)
  }
}
