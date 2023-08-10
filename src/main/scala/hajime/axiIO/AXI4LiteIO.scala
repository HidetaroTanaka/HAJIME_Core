package hajime.axiIO

import chisel3._
import chisel3.util._

trait ChecksAxiReadResp {
  val R_OKEY = 0
  val R_EXOKEY = 1
  // unaligned access -> misaligned exception
  val R_SLVERR = 2
  // out-of-bound access -> access fault exception
  val R_DECERR = 3
  val R_PREFETCHED = 4
  val R_TRANSFAULT = 5
  val R_OKEYDIRTY = 6
  val R_RESERVED = 7
  def readErrorList: List[Int] = List(R_SLVERR, R_DECERR, R_TRANSFAULT)
}

trait ChecksAxiWriteResp {
  val W_OKEY = 0
  val W_EXOKAY = 1
  val W_SLVERR = 2
  val W_DECERR = 3
  val W_DEFER = 4
  val W_TRANSFAULT = 5
  val W_RESERVED = 6
  val W_UNSUPPORTED = 7
  def writeErrorList: List[Int] = List(W_SLVERR, W_DECERR, W_DEFER, W_TRANSFAULT, W_UNSUPPORTED)
}

trait AXIlite_hasAddrChannel {
  val addr: UInt
  def alignedTo(align: Int): Bool = addr.tail(addr.getWidth-align) === 0.U

  def alignedToWord: Bool = alignedTo(align = 2)

  def alignedToDouble: Bool = alignedTo(align = 3)
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
  def exception: Bool
}

/**
 * AXIlite Write Success Channel? idk
 * from slave
 */
class AXIlite_Bchannel extends Bundle with AXIlite_isResponse with ChecksAxiWriteResp {
  val resp = Output(UInt(3.W))
  override def exception: Bool = writeErrorList.map(x => x.U === resp).reduce(_ || _)
}

trait AXIlite_hasDataChannel {
  val data: UInt
}

/**
 * AXIlite Read Response Channel from slave
 * @param data_width
 */
class AXIlite_Rchannel(data_width: Int) extends Bundle with AXIlite_isResponse with AXIlite_hasDataChannel with ChecksAxiReadResp {
  val data = Output(UInt(data_width.W))
  val resp = Output(UInt(3.W))

  override def exception: Bool = readErrorList.map(x => x.U === resp).reduce(_ || _)
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
