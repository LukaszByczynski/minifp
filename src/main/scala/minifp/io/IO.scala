package minifp.io

sealed trait IO[+A] { self =>
  private[io] val tag: IO.Tag.Type

  def map[B](f: A => B): IO[B] = {
    flatMap(a => IO.PureOp(f(a)))
  }

  def flatMap[B](f: A => IO[B]): IO[B] = {
    IO.FlatMapOp(self, f)
  }
}

object IO {

  private[io] object Tag {
    type Type = Int
    val Pure: Type    = 1
    val Effect: Type  = 2
    val FlatMap: Type = 3
  }

  private[io] abstract class Opcode[A](
      private[io] val tag: Tag.Type
  ) extends IO[A]

  private[io] final case class PureOp[A](a: A)                           extends Opcode[A](Tag.Pure)
  private[io] final case class EffectOp[A](effect: () => IO[A])          extends Opcode[A](Tag.Effect)
  private[io] final case class FlatMapOp[A, B](io: IO[A], f: A => IO[B]) extends Opcode[B](Tag.FlatMap)

  def pure[A](a: A): IO[A] = {
    PureOp(a)
  }

}
