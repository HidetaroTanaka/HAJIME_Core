package hajime.publicmodules

import circt.stage.ChiselStage
import chisel3._
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
  val exceptionSignals = new ValidIO(UInt(params.xprlen.W))
}

class LDSTCpuIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(Irrevocable(new LDSTReq()))
  val resp = new ValidIO(new LDSTResp())
}

class LDSTIO(implicit params: HajimeCoreParams) extends Bundle {
  val cpu = new LDSTCpuIO()
  val dcache_axi4lite = new AXI4liteIO(addr_width = params.xprlen, data_width = params.xprlen)
}

// TODO: Add load/store address misaligned, access fault
// TODO: Add Atomic Extension Support
class LDSTUnit(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new LDSTIO())
  val strb_width = params.xprlen/8

  val req_reg = Reg(chiselTypeOf(io.cpu.req.bits))
  when(io.cpu.req.ready && io.cpu.req.valid) {
    req_reg := io.cpu.req.bits
  }

  io.cpu.req.ready := io.cpu.req.valid && MuxCase(true.B, Seq(
    io.cpu.req.bits.funct.memRead -> io.dcache_axi4lite.ar.ready,
    io.cpu.req.bits.funct.memWrite -> (io.dcache_axi4lite.aw.ready && io.dcache_axi4lite.w.ready),
  ))
  io.cpu.resp.valid := MuxCase(true.B, Seq(
    req_reg.funct.memRead -> io.dcache_axi4lite.r.valid,
    req_reg.funct.memWrite -> io.dcache_axi4lite.b.valid,
  ))
  // TODO: Check Access Fault and Address Misaligned
  io.cpu.resp.bits.exceptionSignals := DontCare
  io.cpu.resp.bits.data := MuxLookup(req_reg.funct.memory_length, io.dcache_axi4lite.r.bits.data)(Seq(
    MEM_LEN.B.asUInt -> Mux(req_reg.funct.mem_sext, hajime.common.Functions.sign_ext(io.dcache_axi4lite.r.bits.data(7,0), params.xprlen), io.dcache_axi4lite.r.bits.data(7,0).zext.asUInt),
    MEM_LEN.H.asUInt -> Mux(req_reg.funct.mem_sext, hajime.common.Functions.sign_ext(io.dcache_axi4lite.r.bits.data(15,0), params.xprlen), io.dcache_axi4lite.r.bits.data(15,0).zext.asUInt),
    MEM_LEN.W.asUInt -> Mux(req_reg.funct.mem_sext, hajime.common.Functions.sign_ext(io.dcache_axi4lite.r.bits.data(31,0), params.xprlen), io.dcache_axi4lite.r.bits.data(31,0).zext.asUInt),
    MEM_LEN.D.asUInt -> io.dcache_axi4lite.r.bits.data
  ))

  // AXI4Lite Read Address Request Channel
  io.dcache_axi4lite.ar.valid := io.cpu.req.bits.funct.memRead && io.cpu.req.valid
  io.dcache_axi4lite.ar.bits.addr := io.cpu.req.bits.addr
  io.dcache_axi4lite.ar.bits.prot := 0.U(3.W)

  // AXI4Lite Write Address Request Channel
  io.dcache_axi4lite.aw.valid := io.cpu.req.bits.funct.memWrite && io.cpu.req.valid
  io.dcache_axi4lite.aw.bits.addr := io.cpu.req.bits.addr
  io.dcache_axi4lite.aw.bits.prot := 0.U(3.W)

  // AXI4Lite Write Response Channel
  io.dcache_axi4lite.b.ready := true.B

  // AXI4Lite Read Response Channel
  io.dcache_axi4lite.r.ready := true.B

  // AXI4Lite Write Data Request Channel
  io.dcache_axi4lite.w.valid := (io.cpu.req.bits.funct.memory_function === MEM_FCN.M_WR.asUInt) && io.cpu.req.valid
  io.dcache_axi4lite.w.bits.data := io.cpu.req.bits.data
  io.dcache_axi4lite.w.bits.strb := MuxLookup(io.cpu.req.bits.funct.memory_length, 0.U(strb_width.W))(Seq(
    MEM_LEN.B.asUInt -> "h01".U(strb_width.W),
    MEM_LEN.H.asUInt -> "h03".U(strb_width.W),
    MEM_LEN.W.asUInt-> "h0F".U(strb_width.W),
    MEM_LEN.D.asUInt -> "hFF".U(strb_width.W)
  ))
}

object LDSTUnit extends App {
  def apply(implicit params: HajimeCoreParams): LDSTUnit = new LDSTUnit()
  ChiselStage.emitSystemVerilogFile(LDSTUnit(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
