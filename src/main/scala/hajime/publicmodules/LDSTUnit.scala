package hajime.publicmodules

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common._

class LDSTReq(implicit params: HajimeCoreParams) extends Bundle {
  val addr = UInt(params.xprlen.W)
  val data = UInt(params.xprlen.W)
  val funct = Input(new ID_output)
}

class LDSTResp(implicit params: HajimeCoreParams) extends Bundle {
  val data = UInt(params.xprlen.W)
  val exception = Bool()
}

class LDSTCpuIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(Irrevocable(new LDSTReq()))
  val resp = new ValidIO(new LDSTResp())
}

class LDSTIO(implicit params: HajimeCoreParams) extends Bundle {
  val cpu = new LDSTCpuIO()
  val dcache_axi4lite = new AXI4liteIO(addr_width = params.xprlen, data_width = params.xprlen)
}

// TODO: Add Atomic Extension Support
class LDSTUnit(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new LDSTIO())
  val strb_width = params.xprlen/8

  val req_reg = Reg(chiselTypeOf(io.cpu.req.bits))
  when(io.cpu.req.ready && io.cpu.req.valid) {
    req_reg := io.cpu.req.bits
  }

  io.cpu.req.ready := io.cpu.req.valid && MuxCase(true.B, Seq(
    (io.cpu.req.bits.funct.memory_function === MEM_FCN.M_RD.asUInt) -> io.dcache_axi4lite.ar.ready,
    (io.cpu.req.bits.funct.memory_function === MEM_FCN.M_WR.asUInt) -> (io.dcache_axi4lite.aw.ready && io.dcache_axi4lite.w.ready),
  ))
  io.cpu.resp.valid := MuxCase(true.B, Seq(
    req_reg.MEM_ctrl.memRead -> io.dcache_axi4lite.r.valid,
    req_reg.MEM_ctrl.memWrite -> io.dcache_axi4lite.b.valid,
  ))
  io.cpu.resp.bits.exception := MuxCase(false.B, Seq(
    req_reg.MEM_ctrl.memRead -> io.dcache_axi4lite.r.bits.exception,
    req_reg.MEM_ctrl.memWrite -> io.dcache_axi4lite.b.bits.exception,
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
