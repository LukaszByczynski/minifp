package minifp.io

import scala.annotation.switch

object Runtime {
  import internals._

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
      (opcode.ioTag: @switch) match {
        case IOTag.Resume =>
          opcode = opcode.asInstanceOf[ResumeOp[Any, Any]].effect()

        case IOTag.Pure =>
          if (error == null) {
            result = opcode.asInstanceOf[PureOp[Any]].value
          }
          opcode = cont

        case IOTag.Raise =>
          error = opcode.asInstanceOf[RaiseOp[Any]].error
          opcode = cont

        case IOTag.Effect =>
          if (error == null) {
            try {
              result = opcode.asInstanceOf[EffectOp[Any, Any]].effect()
            } catch {
              case err: RuntimeException =>
                error = err
            }
          }
          opcode = cont

        case IOTag.Map =>
          val _cont                     = cont
          val map: MapOp[Any, Any, Any] = opcode.asInstanceOf[MapOp[Any, Any, Any]]

          opcode = map.io
          cont = ResumeOp(() => {
            if (error == null)
              result = map.f(result)
            _cont
          })

        case IOTag.FlatMap =>
          val _cont                               = cont
          val fmap: FlatMapOp[Any, Any, Any, Any] = opcode.asInstanceOf[FlatMapOp[Any, Any, Any, Any]]

          opcode = fmap.io
          cont = ResumeOp(() => {
            if (error == null) {
              cont = _cont
              fmap.f(result)
            } else {
              cont = null
              _cont
            }
          })

        case IOTag.FlatMapError =>
          val _cont                               = cont
          val fmap: FlatMapErrorOp[Any, Any, Any] = opcode.asInstanceOf[FlatMapErrorOp[Any, Any, Any]]

          opcode = fmap.io
          cont = ResumeOp(() => {
            if (error != null) {
              cont = _cont
              fmap.f(error)
            } else {
              cont = null
              _cont
            }
          })

        case IOTag.Attempt =>
          val _cont = cont
          opcode = opcode.asInstanceOf[AttemptOp[Any, Any]].io
          cont = ResumeOp(() => {
            result = if (error != null) Left(error) else Right(result)
            error = null
            _cont
          })

        case _ => ???
      }
    }

    if (error == null)
      Right(result.asInstanceOf[A])
    else
      Left(error.asInstanceOf[E])
  }
}
