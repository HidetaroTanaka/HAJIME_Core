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

trait AxiLiteHasAddrChannel {
  val addr: UInt
  def alignedTo(align: Int): Bool = addr.tail(addr.getWidth-align) === 0.U

  def alignedToWord: Bool = alignedTo(align = 2)

  def alignedToDouble: Bool = alignedTo(align = 3)
}
/**
 * AXIlite Read Request Channel
 * from master
 * @param addrWidth address width
 */
class AxiLiteARchannel(addrWidth: Int) extends Bundle with AxiLiteHasAddrChannel {
  val addr = Output(UInt(addrWidth.W))
  val prot = Output(UInt(3.W))
}

/**
 * AXIlite Write Address Request Channel
 * from master
 * @param addrWidth address width
 */
class AxiLiteAWchannel(addrWidth: Int) extends Bundle with AxiLiteHasAddrChannel {
  val addr = Output(UInt(addrWidth.W))
  val prot = Output(UInt(3.W))
}

trait AxiLiteIsResponse {
  val resp: UInt
  def exception: Bool
}

/**
 * AXIlite Write Success Channel? idk
 * from slave
 */
class AxiLiteBchannel extends Bundle with AxiLiteIsResponse with ChecksAxiWriteResp {
  val resp = Output(UInt(3.W))
  override def exception: Bool = writeErrorList.map(x => x.U === resp).reduce(_ || _)
}

trait AxiLiteHasDataChannel {
  val data: UInt
}

/**
 * AXIlite Read Response Channel from slave
 * @param dataWidth
 */
class AxiLiteRchannel(dataWidth: Int) extends Bundle with AxiLiteIsResponse with AxiLiteHasDataChannel with ChecksAxiReadResp {
  val data = Output(UInt(dataWidth.W))
  val resp = Output(UInt(3.W))

  override def exception: Bool = readErrorList.map(x => x.U === resp).reduce(_ || _)
}

/**
 * AXIlite Write Data Request Channel from master
 * @param dataWidth
 */
class AxiLiteWchannel(dataWidth: Int) extends Bundle with AxiLiteHasDataChannel {
  require(dataWidth % 8 == 0, "dataWidth is not multiply of 8")
  val data = Output(UInt(dataWidth.W))
  // val ready = Input(Bool())
  val strb = Output(UInt((dataWidth/8).W))
  // val valid = Output(Bool())
}

class AXI4liteIO(addrWidth: Int, dataWidth: Int) extends Bundle {
  val ar = Irrevocable(new AxiLiteARchannel(addrWidth))
  val aw = Irrevocable(new AxiLiteAWchannel(addrWidth))
  val b = Flipped(Irrevocable(new AxiLiteBchannel))
  val r = Flipped(Irrevocable(new AxiLiteRchannel(dataWidth)))
  val w = Irrevocable(new AxiLiteWchannel(dataWidth))
}
