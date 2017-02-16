package com.nthportal.extrapredef

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

class ExtraPredefTest extends FlatSpec with Matchers {
  private val predef = new ExtraPredef {}

  import predef._
  import ExtraPredefTest._

  private val _null: Any = null

  behavior of "ExtraPredef"

  it should "check for null correctly" in {
    a [NullPointerException] should be thrownBy {_null.nonNull}

    noException should be thrownBy {"a string".nonNull}
  }

  it should "coalesce null references correctly" in {
    _null ?? "bar" should equal ("bar")
    "foo" ?? "bar" should equal ("foo")
    "foo" ?? null should equal ("foo")
  }

  it should "chain comparisons" in {
    case class ComparisonChainTest(a: Int, b: Int, c: BasicOrdered) extends Ordered[ComparisonChainTest] {
      override def compare(that: ComparisonChainTest): Int = {
        (this.a compare that.a)
          .thenCompare(this.b, that.b)
          .thenCompare(this.c, that.c)
      }
    }

    val test = ComparisonChainTest(1, 2, 3)

    test should be > ComparisonChainTest(1, 2, 2)
    test should be > ComparisonChainTest(1, 1, 4)
    test should be > ComparisonChainTest(0, 3, 4)

    test should be < ComparisonChainTest(1, 2, 4)
    test should be < ComparisonChainTest(1, 3, 2)
    test should be < ComparisonChainTest(2, 1, 2)

    test shouldNot be > ComparisonChainTest(1, 2, 3)
    test shouldNot be < ComparisonChainTest(1, 2, 3)
  }

  it should "chain orderings" in {
    val ordering = Ordering.by[OrderingChainTest, Int](_.a).thenBy(_.b).thenBy(_.c)

    case class OrderingChainTest(a: Int, b: Int, c: BasicOrdered) extends Ordered[OrderingChainTest] {
      override def compare(that: OrderingChainTest): Int = ordering.compare(this, that)
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

  it should "create equivalent `Future`s from `Option`s" in {
    Await.result(Some("string").toFuture, Duration.Zero) should equal ("string")

    a [NoSuchElementException] should be thrownBy {Await.result(None.toFuture, Duration.Zero)}
  }

  it should "invert `Option`s" in {
    Some("string").invertWith("none") shouldBe empty
    None.invertWith("some").get should be ("some")
  }
}

object ExtraPredefTest {
  case class BasicOrdered(int: Int) extends Ordered[BasicOrdered] {
    override def compare(that: BasicOrdered): Int = this.int compare that.int
  }

  implicit def int2BasicOrdered(int: Int): BasicOrdered = BasicOrdered(int)
}
