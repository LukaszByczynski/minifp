package minifp.io

import org.scalatest.funsuite.AnyFunSuite
import scala.util.Try

class IOTests extends AnyFunSuite {

  test("pure") {
    val program = IO.pure(1)

    assertResult(1)(Runtime.unsafeRun(program))
  }

  test("pure + flatMap") {
    val program = IO.pure(1).flatMap(x => IO.pure(x + 2))

    assertResult(3)(Runtime.unsafeRun(program))
  }

  test("pure + map") {
    val program = IO.pure(1).map(_ + 3)

    assertResult(4)(Runtime.unsafeRun(program))
  }

  test("pure + 2x flatMap") {
    val program = IO
      .pure(1)
      .flatMap(e => IO.pure(e * 2))
      .flatMap(e => IO.pure(e * 2))

    assertResult(4)(Runtime.unsafeRun(program))
  }

  test("pure + 2 x flatMap with map") {
    val program = for {
      x <- IO.pure(1)
      y <- IO.pure(2).map(_ * 2)
    } yield x + y

    assertResult(5)(Runtime.unsafeRun(program))
  }

  test("stack safe test") {
    val program = (0 until 1000000)
      .foldRight(IO.pure(0))((_, o) => o.flatMap(e => IO.pure(e + 1)))

    assertResult(1000000)(Runtime.unsafeRun(program))
  }

  test("effect") {
    val program = IO.effect("test")

    assertResult("test")(Runtime.unsafeRun(program))
  }

  test("effect - catch error") {
    val program = IO.effect(throw new IllegalArgumentException("test"))

    assertResult(Left("test"))(Runtime.unsafeRunSync(program).left.map(_.getMessage()))
  }

  test("attempt") {
    val program = IO.effect(throw new IllegalArgumentException("test")).attempt

    assertResult(Left("test"))(Runtime.unsafeRun(program).left.map(_.getMessage()))
  }

  test("attempt + error") {
    val program = for {
      x <- IO.effect(throw new IllegalArgumentException("test")).attempt
      _ <- IO.effect(throw new IllegalArgumentException("test2"))
    } yield x

    assertResult(Left(classOf[RuntimeException]))(Try(Runtime.unsafeRun(program)).toEither.left.map(_.getClass()))
  }

  test("attempt + flatMap") {
    val program = for {
      x      <- IO.effect(throw new IllegalArgumentException("test")).attempt
      y      <- IO.pure(2)
      result <- IO.pure(if (x.isLeft) 1 else 2)
    } yield result * y

    assertResult(2)(Runtime.unsafeRun(program))
  }

  test("mapError + attempt") {
    val program = IO.effect(throw new IllegalArgumentException("test")).mapError(_.getMessage()).attempt

    assertResult(Left("test"))(Runtime.unsafeRun(program))
  }

  test("ifM") {
    val program = IO.ifM(IO.pure(true))(IO.pure(1), IO.pure(2))

    assertResult(1)(Runtime.unsafeRun(program))
  }

  test("unit") {
    val program = IO.pure(1) >>= IO.unit

    assertResult(())(Runtime.unsafeRun(program))
  }
}
