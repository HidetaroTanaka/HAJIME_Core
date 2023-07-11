package hajime.common

import chisel3._
import chisel3.util._

object CACHE_FUNCTIONS {
  val MEM_NONE = 0.U(2.W)
  val BYTE = MEM_NONE
  val HALFWORD = 1.U(2.W)
  val WORD = 2.U(2.W)
  val DOUBLEWORD = 3.U(2.W)

  def CACHE_FUNC_WIDTH: Int = MEM_NONE.getWidth
}
