package minifp.io

sealed trait IO[+E, +A] { self =>
  private[io] val tag: IO.Tag.Type

  def flatMap[E1 >: E, B](f: A => IO[E1, B]): IO[E, B] = {
    IO.FlatMapOp(self, f)
  }

  def flatMapError[E1, B >: A](f: E => IO[E1, B]): IO[E1, B] = {
    IO.FlatMapErrorOp(self, f)
  }

  def map[B](f: A => B): IO[E, B] = {
    flatMap(a => IO.PureOp(f(a)))
  }

  def mapError[E1](f: E => E1): IO[E1, A] = {
    flatMapError(e => IO.RaiseOp(f(e)))
  }

  def attempt: IO[Nothing, Either[E, A]] = {
    IO.AttemptOp(self)
  }

}

object IO {

  private[io] object Tag {
    type Type = Int
    val Resume: Type       = 0
    val Pure: Type         = 1
    val Raise: Type        = 2
    val Effect: Type       = 3
    val FlatMap: Type      = 4
    val FlatMapError: Type = 5
    val Attempt: Type      = 6
  }

  private[io] abstract class Opcode[E, A](
      private[io] val tag: Tag.Type
  ) extends IO[E, A]

  private[io] final case class ResumeOp[E, A](effect: () => IO[E, A])                  extends Opcode[E, A](Tag.Resume)
  private[io] final case class PureOp[A](value: A)                                     extends Opcode[Nothing, A](Tag.Pure)
  private[io] final case class RaiseOp[E](error: E)                                    extends Opcode[E, Nothing](Tag.Raise)
  private[io] final case class EffectOp[E, A](effect: () => A)                         extends Opcode[E, A](Tag.Effect)
  private[io] final case class FlatMapOp[E, A, E1, B](io: IO[E, A], f: A => IO[E1, B]) extends Opcode[E, B](Tag.FlatMap)
  private[io] final case class FlatMapErrorOp[E, A, E1](io: IO[E, A], f: E => IO[E1, A])
      extends Opcode[E1, A](Tag.FlatMapError)
  private[io] final case class AttemptOp[E, A](io: IO[E, A]) extends Opcode[Nothing, Either[E, A]](Tag.Attempt)

  def pure[A](a: A): IO[Nothing, A] = {
    PureOp(a)
  }

  def effect[A](a: => A): IO[RuntimeException, A] = {
    EffectOp(() => a)
  }

  def ifM[E, A](cond: IO[E, Boolean])(onTrue: IO[E, A], onFalse: IO[E, A]): IO[E, A] = {
    cond.flatMap(c => if (c) onTrue else onFalse)
  }
}
