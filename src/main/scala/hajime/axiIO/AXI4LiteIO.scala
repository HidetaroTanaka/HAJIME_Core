package hajime.axiIO

import chisel3._
import chisel3.util._

/**
 * AXIlite Read Request Channel
 * from master
 * @param addr_width address width
 */
class AXIlite_ARchannel(addr_width: Int) extends Bundle {
  val addr = Output(UInt(addr_width.W))
  val prot = Output(UInt(3.W))
  // val ready = Input(Bool())
  // val valid = Output(Bool())
}

/**
 * AXIlite Write Address Request Channel
 * from master
 * @param addr_width address width
 */
class AXIlite_AWchannel(addr_width: Int) extends Bundle {
  val addr = Output(UInt(addr_width.W))
  val prot = Output(UInt(3.W))
  // val ready = Input(Bool())
  // val valid = Output(Bool())
}

/**
 * AXIlite Write Success Channel? idk
 * from slave
 */
class AXIlite_Bchannel extends Bundle {
  // val ready = Input(Bool())
  val resp = Output(UInt(3.W))
  // val valid = Output(Bool())
}

/**
 * AXIlite Read Response Channel from slave
 * @param data_width
 */
class AXIlite_Rchannel(data_width: Int) extends Bundle {
  val data = Output(UInt(data_width.W))
  // val ready = Input(Bool())
  val resp = Output(UInt(3.W))
  // val valid = Output(Bool())
}

/**
 * AXIlite Write Data Request Channel from master
 * @param data_width
 */
class AXIlite_Wchannel(data_width: Int) extends Bundle {
  require(data_width % 8 == 0, "data_width is not multiply of 8")
  val data = Output(UInt(data_width.W))
  // val ready = Input(Bool())
  val strb = Output(UInt((data_width/8).W))
  // val valid = Output(Bool())
}

class AXI4liteIO(addr_width: Int, data_width: Int) extends Bundle {
  val ar = new DecoupledIO(new AXIlite_ARchannel(addr_width))
  val aw = new DecoupledIO(new AXIlite_AWchannel(addr_width))
  val b = Flipped(new DecoupledIO(new AXIlite_Bchannel))
  val r = Flipped(new DecoupledIO(new AXIlite_Rchannel(data_width)))
  val w = new DecoupledIO(new AXIlite_Wchannel(data_width))
}

object AXI4SIG_CHECK {
  def resp_exception(resp: UInt): Bool = {
    (resp === "b010".U(3.W)) || (resp === "b011".U(3.W)) || (resp === "b101".U(3.W))
  }
}