package minifp.io

import org.scalatest.funsuite.AnyFunSuite

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
}
