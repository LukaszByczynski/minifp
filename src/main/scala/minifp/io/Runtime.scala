package minifp.io

import scala.annotation.tailrec
import scala.collection.View.FlatMap

object Runtime {
  import IO._

  @inline
  def unsafeRun[E, A](program: IO[E, A]): A = unsafeRunSync(program) match {
    case Right(result) => result
    case Left(err)     => throw new RuntimeException(s"Unexpected error: $err")
  }

  @inline
  def unsafeRunSync[E, A](program: IO[E, A]): Either[E, A] = {
    var opcode: IO[Any, Any] = program
    var cont: IO[Any, Any]   = null
    var result: Any          = null
    var error: Any           = null

    while (opcode != null) {
      opcode.tag match {
        case Tag.Resume =>
          opcode = opcode.asInstanceOf[ResumeOp[Any, Any]].effect()

        case Tag.Pure =>
          if (error == null)
            result = opcode.asInstanceOf[PureOp[Any]].value
          opcode = cont

        case Tag.Raise =>
          error = opcode.asInstanceOf[RaiseOp[Any]].error
          opcode = cont

        case Tag.Effect =>
          if (error == null) {
            try {
              result = opcode.asInstanceOf[EffectOp[Any, Any]].effect()
            } catch {
              case err: RuntimeException => error = err
            }
          }
          opcode = cont

        case Tag.FlatMap =>
          val io: FlatMapOp[Any, Any, Any, Any] = opcode.asInstanceOf[FlatMapOp[Any, Any, Any, Any]]
          opcode = io.io
          if (cont == null) {
            cont = ResumeOp(() => {
              cont = null
              if (error == null)
                io.f(result)
              else null
            })
          } else {
            val _cont = cont
            cont = ResumeOp(() => {
              cont = _cont
              if (error == null)
                io.f(result)
              else null
            })
          }

        case Tag.FlatMapError =>
          val io: FlatMapErrorOp[Any, Any, Any] = opcode.asInstanceOf[FlatMapErrorOp[Any, Any, Any]]
          opcode = io.io
          val _cont = cont
          cont = ResumeOp(() => {
            if (error != null) {
              cont = _cont
              io.f(error)
            } else
              _cont
          })

        case Tag.Attempt =>
          val _cont = cont
          opcode = opcode.asInstanceOf[AttemptOp[Any, Any]].io
          cont = ResumeOp(() => {
            result = if (error != null) Left(error) else Right(result)
            error = null
            _cont
          })
      }
    }

    if (error == null)
      Right(result.asInstanceOf[A])
    else
      Left(error.asInstanceOf[E])
  }
}
