package minifp.io

import internals._

trait IO[+E, +A] { self =>
  private[io] val ioTag: IOTag.Type

  def flatMap[E1 >: E, B](f: A => IO[E1, B]): IO[E, B] = {
    FlatMapOp(self, f)
  }

  def flatMapError[E1, B >: A](f: E => IO[E1, B]): IO[E1, B] = {
    FlatMapErrorOp(self, f)
  }

  def map[B](f: A => B): IO[E, B] = {
    flatMap(a => PureOp(f(a)))
  }

  def mapError[E1](f: E => E1): IO[E1, A] = {
    flatMapError(e => RaiseOp(f(e)))
  }

  def attempt: IO[Nothing, Either[E, A]] = {
    AttemptOp(self)
  }

  def >>=[E1 >: E, B](f: => IO[E1, B]): IO[E, B] = {
    flatMap(_ => f)
  }

}

object IO {

  def unit: IO[Nothing, Unit] = {
    PureOp(())
  }

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
