package hajime.axiIO

import chisel3._
import chisel3.util._

trait AXIlite_hasAddrChannel {
  val addr: UInt
  def alignedToWord: Bool = addr.tail(addr.getWidth-2) === 0.U
}
/**
 * AXIlite Read Request Channel
 * from master
 * @param addr_width address width
 */
class AXIlite_ARchannel(addr_width: Int) extends Bundle with AXIlite_hasAddrChannel {
  val addr = Output(UInt(addr_width.W))
  val prot = Output(UInt(3.W))
}

/**
 * AXIlite Write Address Request Channel
 * from master
 * @param addr_width address width
 */
class AXIlite_AWchannel(addr_width: Int) extends Bundle with AXIlite_hasAddrChannel {
  val addr = Output(UInt(addr_width.W))
  val prot = Output(UInt(3.W))
}

trait AXIlite_isResponse {
  val resp: UInt
  def exception: Bool = {
    (resp === "b010".U(3.W)) || (resp === "b011".U(3.W)) || (resp === "b101".U(3.W))
  }
}

/**
 * AXIlite Write Success Channel? idk
 * from slave
 */
class AXIlite_Bchannel extends Bundle with AXIlite_isResponse {
  val resp = Output(UInt(3.W))
}

trait AXIlite_hasDataChannel {
  val data: UInt
}

/**
 * AXIlite Read Response Channel from slave
 * @param data_width
 */
class AXIlite_Rchannel(data_width: Int) extends Bundle with AXIlite_isResponse with AXIlite_hasDataChannel {
  val data = Output(UInt(data_width.W))
  val resp = Output(UInt(3.W))
}

/**
 * AXIlite Write Data Request Channel from master
 * @param data_width
 */
class AXIlite_Wchannel(data_width: Int) extends Bundle with AXIlite_hasDataChannel {
  require(data_width % 8 == 0, "data_width is not multiply of 8")
  val data = Output(UInt(data_width.W))
  // val ready = Input(Bool())
  val strb = Output(UInt((data_width/8).W))
  // val valid = Output(Bool())
}

class AXI4liteIO(addr_width: Int, data_width: Int) extends Bundle {
  val ar = Irrevocable(new AXIlite_ARchannel(addr_width))
  val aw = Irrevocable(new AXIlite_AWchannel(addr_width))
  val b = Flipped(Irrevocable(new AXIlite_Bchannel))
  val r = Flipped(Irrevocable(new AXIlite_Rchannel(data_width)))
  val w = Irrevocable(new AXIlite_Wchannel(data_width))
}
