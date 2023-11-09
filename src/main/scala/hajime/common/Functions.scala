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
  implicit class lsHasElementEquivalentToUInt[T <: scala.collection.Iterable[UInt]](ls: T) {
    def has(elem: UInt): Bool = {
      ls.map(_ === elem).reduce(_ || _)
    }
  }
  @deprecated("this only supports List, so use lsHasElementEquivalentToUInt instead as it supports everything Iterable")
  implicit class seqHasElementEquivalentToUInt(ls: List[UInt]) {
    def has(elem: UInt): Bool = {
      Functions.lsHasElementEquivalentToUInt(ls).has(elem)
    }
  }
  implicit class booleanToInt(b: Boolean) {
    def toInt: Int = {
      if(b) 1 else 0
    }
  }
}