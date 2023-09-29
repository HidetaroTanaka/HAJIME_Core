package hajime.common

import chisel3._
import chisel3.util._
import chisel3.internal.firrtl._

object Functions {
  implicit class signExtend(uint: UInt) {
    def ext(targetWidth: Int): UInt = {
      if(uint.getWidth < targetWidth) {
        Cat(Fill(targetWidth - uint.getWidth, uint.head(1)), uint)
      } else {
        uint
      }
    }
  }
}
