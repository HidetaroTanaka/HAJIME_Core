package hajime.simple4Stage

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common.{CACHE_FUNCTIONS, CacheIO, RISCV_Consts}

/**
 * PC Update request from CPU
 * @param xprlen
 */
class FrontEndReq(xprlen: Int) extends Bundle {
  val pc = UInt(xprlen.W)
}

/**
 * send instruction from FrontEnd to CPU
 * @param xprlen
 */
class FrontEndResp(xprlen: Int) extends Bundle {
  val pc = UInt(xprlen.W)
  // val pcPlus4 = UInt(xprlen.W)
  val inst = UInt(RISCV_Consts.INST_LEN.W)
}

class FrontEndCpuIO(xprlen: Int) extends Bundle {
  val req = Flipped(new ValidIO(new FrontEndReq(xprlen)))
  val resp = new DecoupledIO(new FrontEndResp(xprlen))
}

class FrontEndIO(xprlen: Int) extends Bundle {
  val cpu = new FrontEndCpuIO(xprlen)
  val icache_axi4lite = new AXI4liteIO(addr_width = xprlen, data_width = RISCV_Consts.INST_LEN)
  val reset_vector = Input(UInt(xprlen.W))
  val exception = Output(Bool())
}

class Frontend(xprlen: Int) extends Module {
  val io = IO(new FrontEndIO(xprlen))
  io := DontCare

  val pc_reg = RegInit(io.reset_vector)
  val addr_req_to_axi_ar = MuxCase(pc_reg+4.U(xprlen.W), Seq(
    io.cpu.req.valid -> io.cpu.req.bits.pc,
    // if AXI AR is not ready, or AXI R is not valid, or CPU is not ready, retain PC
    (!io.icache_axi4lite.ar.ready || !io.icache_axi4lite.r.valid || !io.cpu.resp.ready) -> pc_reg,
  ))

  // If CPU gets instruction from Frontend, move PC to next one
  when(io.cpu.resp.valid || io.cpu.resp.ready) {
    pc_reg := addr_req_to_axi_ar
  }

  io.icache_axi4lite.ar.bits.addr := addr_req_to_axi_ar
  io.icache_axi4lite.ar.bits.prot := 0.U
  io.icache_axi4lite.ar.valid := io.cpu.resp.ready || io.cpu.req.valid

  io.cpu.resp.bits.pc := pc_reg
  io.cpu.resp.bits.inst := io.icache_axi4lite.r.bits.data
  io.cpu.resp.valid := io.icache_axi4lite.r.valid
  io.icache_axi4lite.r.ready := io.cpu.resp.ready || io.cpu.req.valid

  io.exception := {
      (io.icache_axi4lite.r.bits.resp === "b010".U(3.W)) ||
      (io.icache_axi4lite.r.bits.resp === "b011".U(3.W)) ||
      (io.icache_axi4lite.r.bits.resp === "b101".U(3.W))
  }
}

object Frontend extends App {
  def apply(xprlen: Int): Frontend = new Frontend(xprlen)

  (new ChiselStage).emitVerilog(this.apply(xprlen = RISCV_Consts.XLEN), args = Array(
    "--emission-options=disableMemRandomization,disableRegisterRandomization"))
}

