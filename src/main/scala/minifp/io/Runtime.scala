package minifp.io

import scala.annotation.tailrec
import scala.collection.View.FlatMap

object Runtime {
  import IO._

  @inline
  def unsafeRun[A](program: IO[A]): A = {
    var opcode: IO[Any] = program
    var cont: IO[Any]   = null
    var result: Any     = null

    while (opcode != null) {
      opcode.tag match {
        case Tag.Pure =>
          result = opcode.asInstanceOf[PureOp[Any]].a
          opcode = cont

        case Tag.Effect =>
          opcode = opcode.asInstanceOf[EffectOp[Any]].effect()

        case Tag.FlatMap =>
          val io: FlatMapOp[Any, Any] = opcode.asInstanceOf[FlatMapOp[Any, Any]]
          opcode = io.io
          if (cont == null) {
            cont = EffectOp(() => {
              cont = null
              io.f(result)
            })
          } else {
            val Anycont = cont
            cont = EffectOp(() => {
              cont = Anycont
              io.f(result)
            })
          }
      }
    }

    result.asInstanceOf[A]
  }
}
