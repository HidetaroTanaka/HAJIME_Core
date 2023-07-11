package hajime.common

import chisel3._
import chisel3.util._

class CacheResp(data_width: Int) extends Bundle {
  val data = UInt(data_width.W)
}

class CacheReq(addr_width: Int, data_width: Int) extends Bundle {
  val addr = UInt(addr_width.W)
  val data = UInt(data_width.W)
  val function = UInt(CACHE_FUNCTIONS.CACHE_FUNC_WIDTH.W)
  val read = Bool()
  val write = Bool()
}

// TODO: Get rid of this shit and implement axi4-lite
class CacheIO(addr_width: Int, data_width: Int) extends Bundle {
  // ValidIO is Output
  val req = Flipped(new DecoupledIO(new CacheReq(addr_width, data_width)))
  val resp = new DecoupledIO(new CacheResp(data_width))

}
