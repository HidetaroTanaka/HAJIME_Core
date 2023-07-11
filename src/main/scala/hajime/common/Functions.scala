package hajime.common

import chisel3._
import chisel3.util._

object Functions {
  def sign_ext(in: UInt, extend_to: Int): UInt = {
    require(in.getWidth < extend_to, "fuck")
    Cat(Fill(extend_to - in.getWidth, in(in.getWidth-1)), in)
  }
}
