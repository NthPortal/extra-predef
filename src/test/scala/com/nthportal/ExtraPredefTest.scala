package com.nthportal

import org.scalatest.{FlatSpec, Matchers}

class ExtraPredefTest extends FlatSpec with Matchers {

  behavior of "ExtraPredef"

  it should "check for null correctly" in {
    a [NullPointerException] should be thrownBy {(null: Any).nonNull}

    noException should be thrownBy {"a string".nonNull}
  }

  it should "chain ordering calls" in {
    case class OrderingChainTest(a: Int, b: Int, c: Int) extends Ordered[OrderingChainTest] {
      override def compare(that: OrderingChainTest): Int = {
        (this.a compare that.a)
          .thenCompare(this.b, that.b)
          .thenCompare(this.c, that.c)
      }
    }

    val test = OrderingChainTest(1, 2, 3)

    test should be > OrderingChainTest(1, 2, 2)
    test should be > OrderingChainTest(1, 1, 4)
    test should be > OrderingChainTest(0, 3, 4)

    test should be < OrderingChainTest(1, 2, 4)
    test should be < OrderingChainTest(1, 3, 2)
    test should be < OrderingChainTest(2, 1, 2)

    test shouldNot be > OrderingChainTest(1, 2, 3)
    test shouldNot be < OrderingChainTest(1, 2, 3)
  }
}
