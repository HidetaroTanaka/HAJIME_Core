package hajime.publicmodules

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common._
import Functions._

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
  val dCacheAxi4Lite = new AXI4liteIO(addrWidth = params.xprlen, dataWidth = params.xprlen)
}

// TODO: Retain req signals when AXI resp is not valid
// TODO: Add Atomic Extension Support
class LDSTUnit(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new LDSTIO())
  val strb_width = params.xprlen/8

  val req_reg = Reg(chiselTypeOf(io.cpu.req.bits))
  when(io.cpu.req.ready && io.cpu.req.valid) {
    req_reg := io.cpu.req.bits
  } .elsewhen(io.cpu.resp.valid) {
    req_reg.funct.memoryFunction := MEM_FCN.M_NONE.asUInt
  }

  io.cpu.req.ready := io.cpu.req.valid && MuxCase(true.B, Seq(
    io.cpu.req.bits.funct.memRead -> io.dCacheAxi4Lite.ar.ready,
    io.cpu.req.bits.funct.memWrite -> (io.dCacheAxi4Lite.aw.ready && io.dCacheAxi4Lite.w.ready),
  ))
  io.cpu.resp.valid := MuxCase(true.B, Seq(
    req_reg.funct.memRead -> io.dCacheAxi4Lite.r.valid,
    req_reg.funct.memWrite -> io.dCacheAxi4Lite.b.valid,
  ))
  val topAddress = req_reg.addr + MuxLookup(req_reg.funct.memoryLength, 0.U)(Seq(
    MEM_LEN.B.asUInt -> 0.U,
    MEM_LEN.H.asUInt -> 1.U,
    MEM_LEN.W.asUInt -> 3.U,
    MEM_LEN.D.asUInt -> 7.U,
  ))
  val access_fault = (topAddress >= 0x00006000.U && topAddress =/= 0x10000000.U)
  val address_misaligned = (topAddress(3) =/= req_reg.addr(3))

  io.cpu.resp.bits.exceptionSignals.bits := MuxCase(0.U, Seq(
    (req_reg.funct.memRead && access_fault) -> Causes.load_access.U,
    (req_reg.funct.memWrite && access_fault) -> Causes.store_access.U,
    (req_reg.funct.memRead && address_misaligned) -> Causes.misaligned_load.U,
    (req_reg.funct.memWrite && address_misaligned) -> Causes.misaligned_store.U,
  ))
  io.cpu.resp.bits.exceptionSignals.valid := (access_fault || address_misaligned) && req_reg.funct.memValid
  io.cpu.resp.bits.data := MuxLookup(req_reg.funct.memoryLength, io.dCacheAxi4Lite.r.bits.data)(Seq(
    MEM_LEN.B.asUInt -> Mux(req_reg.funct.memSExt, io.dCacheAxi4Lite.r.bits.data(7,0).ext(params.xprlen), io.dCacheAxi4Lite.r.bits.data(7,0).zext.asUInt),
    MEM_LEN.H.asUInt -> Mux(req_reg.funct.memSExt, io.dCacheAxi4Lite.r.bits.data(15,0).ext(params.xprlen), io.dCacheAxi4Lite.r.bits.data(15,0).zext.asUInt),
    MEM_LEN.W.asUInt -> Mux(req_reg.funct.memSExt, io.dCacheAxi4Lite.r.bits.data(31,0).ext(params.xprlen), io.dCacheAxi4Lite.r.bits.data(31,0).zext.asUInt),
    MEM_LEN.D.asUInt -> io.dCacheAxi4Lite.r.bits.data
  ))

  // AXI4Lite Read Address Request Channel
  io.dCacheAxi4Lite.ar.valid := io.cpu.req.bits.funct.memRead && io.cpu.req.valid
  io.dCacheAxi4Lite.ar.bits.addr := io.cpu.req.bits.addr
  io.dCacheAxi4Lite.ar.bits.prot := 0.U(3.W)

  // AXI4Lite Write Address Request Channel
  io.dCacheAxi4Lite.aw.valid := io.cpu.req.bits.funct.memWrite && io.cpu.req.valid
  io.dCacheAxi4Lite.aw.bits.addr := io.cpu.req.bits.addr
  io.dCacheAxi4Lite.aw.bits.prot := 0.U(3.W)

  // AXI4Lite Write Response Channel
  io.dCacheAxi4Lite.b.ready := true.B

  // AXI4Lite Read Response Channel
  io.dCacheAxi4Lite.r.ready := true.B

  // AXI4Lite Write Data Request Channel
  io.dCacheAxi4Lite.w.valid := (io.cpu.req.bits.funct.memoryFunction === MEM_FCN.M_WR.asUInt) && io.cpu.req.valid
  io.dCacheAxi4Lite.w.bits.data := io.cpu.req.bits.data
  io.dCacheAxi4Lite.w.bits.strb := MuxLookup(io.cpu.req.bits.funct.memoryLength, 0.U(strb_width.W))(Seq(
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
