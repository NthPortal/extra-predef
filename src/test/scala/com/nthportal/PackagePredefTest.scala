package com.nthportal

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

class PackagePredefTest extends FlatSpec with Matchers {
  import PackagePredefTest._

  private val _null: Any = null

  behavior of "package object Predef"

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

  it should "compare natural ordering correctly" in {
    (BasicOrdered(1) <> 2) should be (true)
    (BasicOrdered(2) <> 1) should be (true)
    (BasicOrdered(1) <> 1) should be (false)

    (BasicOrdered(1) !<> 2) should be (false)
    (BasicOrdered(2) !<> 1) should be (false)
    (BasicOrdered(1) !<> 1) should be (true)
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

  it should "transform `Option`s" in {
    Some("string").transform(s => Some(s.toUpperCase), Some("none")).get should be ("STRING")
    Some("string").transform(s => Some(s.toUpperCase), None).get should be ("STRING")
    Some("string").transform(_ => None, Some("none")) shouldBe empty
    Some("string").transform(_ => None, None) shouldBe empty

    None.transform((_: Nothing) => Some("some"), Some("none")).get should be ("none")
    None.transform((_: Nothing) => Some("some"), None) shouldBe empty
    None.transform((_: Nothing) => None, Some("none")).get should be ("none")
    None.transform((_: Nothing) => None, None) shouldBe empty
  }

  it should "invert `Option`s" in {
    Some("string").invert("none") shouldBe empty
    None.invert("some").get should be ("some")

    Some("string").invertWith(Some("none")) shouldBe empty
    Some("string").invertWith(None) shouldBe empty
    None.invertWith(Some("some")).get should be ("some")
    None.invertWith(None) shouldBe empty
  }
}

object PackagePredefTest {
  case class BasicOrdered(int: Int) extends Ordered[BasicOrdered] {
    override def compare(that: BasicOrdered): Int = this.int compare that.int
  }

  implicit def int2BasicOrdered(int: Int): BasicOrdered = BasicOrdered(int)
}
