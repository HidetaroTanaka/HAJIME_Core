package hajime.publicmodules

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.axiIO.AXI4SIG_CHECK.resp_exception
import hajime.axiIO.AXI4liteIO
import hajime.common.{CACHE_FUNCTIONS, RISCV_Consts}

class MEM_ctrl_IO extends Bundle {
  val memWrite = Bool()
  val memRead  = Bool()
  val mem_func = UInt(CACHE_FUNCTIONS.BYTE.getWidth.W)
  val mem_sext = Bool()

  def mem_valid: Bool = memWrite || memRead
}

class LDSTReq(xprlen: Int) extends Bundle {
  val addr = UInt(xprlen.W)
  val data = UInt(xprlen.W)
  val MEM_ctrl = new MEM_ctrl_IO
}

class LDSTResp(xprlen: Int) extends Bundle {
  val data = UInt(xprlen.W)
  val exception = Bool()
}

class LDSTCpuIO(xprlen: Int) extends Bundle {
  val req = Flipped(new DecoupledIO(new LDSTReq(xprlen)))
  val resp = new ValidIO(new LDSTResp(xprlen))
}

class LDSTIO(xprlen: Int) extends Bundle {
  val cpu = new LDSTCpuIO(xprlen)
  val dcache_axi4lite = new AXI4liteIO(addr_width = xprlen, data_width = xprlen)
}

// TODO: Add Atomic Extension Support
class LDSTUnit(xprlen: Int) extends Module {
  val io = IO(new LDSTIO(xprlen))
  val strb_width = xprlen/8

  val req_reg = Reg(chiselTypeOf(io.cpu.req.bits))
  when(io.cpu.req.ready && io.cpu.req.valid) {
    req_reg := io.cpu.req.bits
  }

  io.cpu.req.ready := io.cpu.req.valid && MuxCase(true.B, Seq(
    io.cpu.req.bits.MEM_ctrl.memRead -> io.dcache_axi4lite.ar.ready,
    io.cpu.req.bits.MEM_ctrl.memWrite -> (io.dcache_axi4lite.aw.ready && io.dcache_axi4lite.w.ready),
  ))
  io.cpu.resp.valid := MuxCase(true.B, Seq(
    req_reg.MEM_ctrl.memRead -> io.dcache_axi4lite.r.valid,
    req_reg.MEM_ctrl.memWrite -> io.dcache_axi4lite.b.valid,
  ))
  io.cpu.resp.bits.exception := MuxCase(false.B, Seq(
    req_reg.MEM_ctrl.memRead -> resp_exception(io.dcache_axi4lite.r.bits.resp),
    req_reg.MEM_ctrl.memWrite -> resp_exception(io.dcache_axi4lite.b.bits.resp),
  ))
  io.cpu.resp.bits.data := MuxLookup(req_reg.MEM_ctrl.mem_func, io.dcache_axi4lite.r.bits.data)(Seq(
    CACHE_FUNCTIONS.BYTE -> Mux(req_reg.MEM_ctrl.mem_sext, hajime.common.Functions.sign_ext(io.dcache_axi4lite.r.bits.data(7,0), xprlen), io.dcache_axi4lite.r.bits.data(7,0).zext.asUInt),
    CACHE_FUNCTIONS.HALFWORD -> Mux(req_reg.MEM_ctrl.mem_sext, hajime.common.Functions.sign_ext(io.dcache_axi4lite.r.bits.data(15,0), xprlen), io.dcache_axi4lite.r.bits.data(15,0).zext.asUInt),
    CACHE_FUNCTIONS.WORD -> Mux(req_reg.MEM_ctrl.mem_sext, hajime.common.Functions.sign_ext(io.dcache_axi4lite.r.bits.data(31,0), xprlen), io.dcache_axi4lite.r.bits.data(31,0).zext.asUInt),
  ))

  // AXI4Lite Read Address Request Channel
  io.dcache_axi4lite.ar.valid := io.cpu.req.bits.MEM_ctrl.memRead && io.cpu.req.valid
  io.dcache_axi4lite.ar.bits.addr := io.cpu.req.bits.addr
  io.dcache_axi4lite.ar.bits.prot := 0.U(3.W)

  // AXI4Lite Write Address Request Channel
  io.dcache_axi4lite.aw.valid := io.cpu.req.bits.MEM_ctrl.memWrite && io.cpu.req.valid
  io.dcache_axi4lite.aw.bits.addr := io.cpu.req.bits.addr
  io.dcache_axi4lite.aw.bits.prot := 0.U(3.W)

  // AXI4Lite Write Response Channel
  io.dcache_axi4lite.b.ready := true.B

  // AXI4Lite Read Response Channel
  io.dcache_axi4lite.r.ready := true.B

  // AXI4Lite Write Data Request Channel
  io.dcache_axi4lite.w.valid := io.cpu.req.bits.MEM_ctrl.memWrite && io.cpu.req.valid
  io.dcache_axi4lite.w.bits.data := io.cpu.req.bits.data
  io.dcache_axi4lite.w.bits.strb := MuxLookup(io.cpu.req.bits.MEM_ctrl.mem_func, 0.U(strb_width.W))(Seq(
    CACHE_FUNCTIONS.BYTE -> "h01".U(strb_width.W),
    CACHE_FUNCTIONS.HALFWORD -> "h03".U(strb_width.W),
    CACHE_FUNCTIONS.WORD -> "h0F".U(strb_width.W),
    CACHE_FUNCTIONS.DOUBLEWORD -> "hFF".U(strb_width.W)
  ))
}

object LDSTUnit extends App {
  def apply(xprlen: Int): LDSTUnit = new LDSTUnit(xprlen)

  (new ChiselStage).emitVerilog(this.apply(xprlen = RISCV_Consts.XLEN), args = Array(
    "--emission-options=disableMemRandomization,disableRegisterRandomization"))
}
