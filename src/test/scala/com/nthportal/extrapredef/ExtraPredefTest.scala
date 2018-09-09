package com.nthportal.extrapredef

import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scala.collection.immutable.SortedMap
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.language.implicitConversions
import scala.util.Try

class ExtraPredefTest extends FlatSpec with Matchers with OptionValues {
  import ExtraPredef._
  import ExtraPredefTest._

  private val _null: Any = null

  behavior of "ExtraPredef"

  it should "require state correctly" in {
    an [IllegalStateException] should be thrownBy requireState(false)
    noException should be thrownBy requireState(true)

    an [IllegalStateException] should be thrownBy requireState(requirement = false, "message")
    noException should be thrownBy requireState(requirement = true, "message")
  }

  it should "handle impossible conditions correctly" in {
    an [AssertionError] should be thrownBy !!!
  }

  it should "check for null correctly" in {
    a [NullPointerException] should be thrownBy _null.nonNull

    noException should be thrownBy "a string".nonNull
  }

  it should "coalesce null references correctly" in {
    _null ?? "bar" shouldEqual "bar"
    "foo" ?? 4 shouldEqual "foo"
    "foo" ?? null shouldEqual "foo"
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
    (BasicOrdered(1) <> 2) shouldBe true
    (BasicOrdered(2) <> 1) shouldBe true
    (BasicOrdered(1) <> 1) shouldBe false

    (BasicOrdered(1) !<> 2) shouldBe false
    (BasicOrdered(2) !<> 1) shouldBe false
    (BasicOrdered(1) !<> 1) shouldBe true
  }

  it should "create equivalent `Try`s from `Option`s" in {
    val t1 = Some("string").toTry
    t1.isSuccess shouldBe true
    t1.get shouldEqual "string"

    val t2 = None.toTry
    t2.isFailure shouldBe true
    a [NoSuchElementException] should be thrownBy t2.get
  }

  it should "create equivalent `Future`s from `Option`s" in {
    Some("string").toFuture.getNow shouldEqual "string"

    a [NoSuchElementException] should be thrownBy None.toFuture.getNow
  }

  it should "transform `Option`s" in {
    Some("string").transform(s => Some(s.toUpperCase), Some("none")).value shouldBe "STRING"
    Some("string").transform(s => Some(s.toUpperCase), None).value shouldBe "STRING"
    Some("string").transform(_ => None, Some("none")) shouldBe empty
    Some("string").transform(_ => None, None) shouldBe empty

    None.transform((_: Nothing) => Some("some"), Some("none")).value shouldBe "none"
    None.transform((_: Nothing) => Some("some"), None) shouldBe empty
    None.transform((_: Nothing) => None, Some("none")).value shouldBe "none"
    None.transform((_: Nothing) => None, None) shouldBe empty
  }

  it should "invert `Option`s" in {
    Some("string").invert("none") shouldBe empty
    None.invert("some").value shouldBe "some"

    Some("string").invertWith(Some("none")) shouldBe empty
    Some("string").invertWith(None) shouldBe empty
    None.invertWith(Some("some")).value shouldBe "some"
    None.invertWith(None) shouldBe empty
  }

  it should "create equivalent `Future`s from `Try`s" in {
    Try("string").toFuture.getNow shouldEqual "string"

    val ex = new Exception("foo")
    Try(throw ex).toFuture.failed.getNow should be theSameInstanceAs ex
  }

  it should "create equivalent `Future`s from `Either`s" in {
    Right[Throwable, String]("string").toFuture.getNow shouldEqual "string"

    val ex = new Exception("foo")
    Left(ex).toFuture.failed.getNow should be theSameInstanceAs ex
  }

  it should "test `SortedMap`s for ordered equality" in {
    val sm = SortedMap(1 -> 1, 2 -> 2, 3 -> 3)

    sm orderedEquals SortedMap(3 -> 3, 2 -> 2, 1 -> 1) shouldBe true
    sm orderedEquals SortedMap(1 -> 1, 2 -> 2, 3 -> 3)(Ordering[Int].reverse) shouldBe false
  }
}

object ExtraPredefTest {
  implicit final class FinishedFuture[A](private val self: Future[A]) extends AnyVal {
    def getNow: A = Await.result(self, Duration.Zero)
  }

  case class BasicOrdered(int: Int) extends Ordered[BasicOrdered] {
    override def compare(that: BasicOrdered): Int = this.int compare that.int
  }

  implicit def int2BasicOrdered(int: Int): BasicOrdered = BasicOrdered(int)
}
