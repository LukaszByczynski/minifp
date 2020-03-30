package minifp.io

package internals {

  private[io] object IOTag {
    type Type = Int

    final val Resume       = 0
    final val Pure         = 1
    final val Raise        = 2
    final val Effect       = 3
    final val Map          = 4
    final val FlatMap      = 5
    final val FlatMapError = 6
    final val Attempt      = 7
  }

  private[io] abstract class Opcode[E, A](
      private[io] val ioTag: IOTag.Type
  ) extends IO[E, A]

  private[io] final case class ResumeOp[E, A](effect: () => IO[E, A]) extends Opcode[E, A](IOTag.Resume)

  private[io] final case class PureOp[A](value: A) extends Opcode[Nothing, A](IOTag.Pure)

  private[io] final case class RaiseOp[E](error: E) extends Opcode[E, Nothing](IOTag.Raise)

  private[io] final case class EffectOp[E, A](effect: () => A) extends Opcode[E, A](IOTag.Effect)

  private[io] final case class MapOp[E, A, B](io: IO[E, A], f: A => B) extends Opcode[E, B](IOTag.Map)
  private[io] final case class FlatMapOp[E, A, E1, B](io: IO[E, A], f: A => IO[E1, B])
      extends Opcode[E, B](IOTag.FlatMap)

  private[io] final case class FlatMapErrorOp[E, A, E1](io: IO[E, A], f: E => IO[E1, A])
      extends Opcode[E1, A](IOTag.FlatMapError)

  private[io] final case class AttemptOp[E, A](io: IO[E, A]) extends Opcode[Nothing, Either[E, A]](IOTag.Attempt)

}
