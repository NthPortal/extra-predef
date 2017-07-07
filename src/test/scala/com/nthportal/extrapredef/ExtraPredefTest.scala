package com.nthportal.extrapredef

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.SortedMap
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.implicitConversions
import scala.util.Try

class ExtraPredefTest extends FlatSpec with Matchers {
  private val predef = new ExtraPredef {}

  import ExtraPredefTest._
  import predef._

  private val _null: Any = null

  behavior of "ExtraPredef"

  it should "check for null correctly" in {
    a [NullPointerException] should be thrownBy {_null.nonNull}

    noException should be thrownBy {"a string".nonNull}
  }

  it should "coalesce null references correctly" in {
    _null ?? "bar" should equal ("bar")
    "foo" ?? 4 should equal ("foo")
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
    val ord1 = Ordering.by[OrderingChainTest1, Int](_.a).thenBy(_.b).thenBy(_.c)

    case class OrderingChainTest1(a: Int, b: Int, c: BasicOrdered) extends Ordered[OrderingChainTest1] {
      override def compare(that: OrderingChainTest1): Int = ord1.compare(this, that)
    }

    val test1 = OrderingChainTest1(1, 2, 3)

    test1 should be > OrderingChainTest1(1, 2, 2)
    test1 should be > OrderingChainTest1(1, 1, 4)
    test1 should be > OrderingChainTest1(0, 3, 4)

    test1 should be < OrderingChainTest1(1, 2, 4)
    test1 should be < OrderingChainTest1(1, 3, 2)
    test1 should be < OrderingChainTest1(2, 1, 2)

    test1 shouldNot be > OrderingChainTest1(1, 2, 3)
    test1 shouldNot be < OrderingChainTest1(1, 2, 3)

    val ord2 = Ordering.by[OrderingChainTest2, Int](_.a)
      .thenBy(Ordering.by[OrderingChainTest2, Int](_.b))
      .thenBy(Ordering.by[OrderingChainTest2, BasicOrdered](_.c))

    case class OrderingChainTest2(a: Int, b: Int, c: BasicOrdered) extends Ordered[OrderingChainTest2] {
      override def compare(that: OrderingChainTest2): Int = ord2.compare(this, that)
    }

    val test2 = OrderingChainTest2(1, 2, 3)

    test2 should be > OrderingChainTest2(1, 2, 2)
    test2 should be > OrderingChainTest2(1, 1, 4)
    test2 should be > OrderingChainTest2(0, 3, 4)

    test2 should be < OrderingChainTest2(1, 2, 4)
    test2 should be < OrderingChainTest2(1, 3, 2)
    test2 should be < OrderingChainTest2(2, 1, 2)

    test2 shouldNot be > OrderingChainTest2(1, 2, 3)
    test2 shouldNot be < OrderingChainTest2(1, 2, 3)
  }

  it should "create equivalent `Try`s from `Option`s" in {
    val t1 = Some("string").toTry
    t1.isSuccess should be (true)
    t1.get should equal ("string")

    val t2 = None.toTry
    t2.isFailure should be (true)
    a [NoSuchElementException] should be thrownBy {t2.get}
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

  it should "create equivalent `Future`s from `Try`s" in {
    Await.result(Try("string").toFuture, Duration.Zero) should equal ("string")

    val ex = new Exception("foo")
    Await.result(Try(throw ex).toFuture.failed, Duration.Zero) should be theSameInstanceAs ex
  }

  it should "create equivalent `Future`s from `Either`s" in {
    Await.result(Right("string").toFuture, Duration.Zero) should equal ("string")

    val ex = new Exception("foo")
    Await.result(Left(ex).toFuture.failed, Duration.Zero) should be theSameInstanceAs ex
  }

  it should "test `SortedMap`s for ordered equality" in {
    val sm = SortedMap(1 -> 1, 2 -> 2, 3 -> 3)

    sm orderedEquals SortedMap(3 -> 3, 2 -> 2, 1 -> 1) shouldBe true
    sm orderedEquals SortedMap(1 -> 1, 2 -> 2, 3 -> 3)(implicitly[Ordering[Int]].reverse) shouldBe false
  }

  it should "convert collections to `SortedMap`s" in {
    val sm1 = SortedMap(1 -> 1, 2 -> 2, 3 -> 3)

    sm1.toSortedMap should be theSameInstanceAs sm1
    sm1.toSeq shouldEqual Seq(1 -> 1, 2 -> 2, 3 -> 3)
    Seq(3 -> 3, 2 -> 2, 1 -> 1).toSortedMap orderedEquals sm1 shouldBe true

    val reverseOrdering = implicitly[Ordering[Int]].reverse

    val sm2 = sm1.toSortedMap(reverseOrdering)
    sm2 orderedEquals sm1 shouldBe false

    sm2.toSortedMap(reverseOrdering) should be theSameInstanceAs sm2
    sm2.toSeq shouldEqual Seq(3 -> 3, 2 -> 2, 1 -> 1)
    Seq(1 -> 1, 2 -> 2, 3 -> 3).toSortedMap(reverseOrdering) orderedEquals sm2 shouldBe true
  }
}

object ExtraPredefTest {
  case class BasicOrdered(int: Int) extends Ordered[BasicOrdered] {
    override def compare(that: BasicOrdered): Int = this.int compare that.int
  }

  implicit def int2BasicOrdered(int: Int): BasicOrdered = BasicOrdered(int)
}
