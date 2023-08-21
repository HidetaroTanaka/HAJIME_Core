package hajime.simple4Stage

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common._

/**
 * PC Update request from CPU
 */
class FrontEndReq(implicit params: HajimeCoreParams) extends Bundle {
  val pc = UInt(params.xprlen.W)
}

/**
 * send instruction from FrontEnd to CPU
 */
class FrontEndResp(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val pc = new ProgramCounter()
  val inst = new InstBundle()
  val exceptionSignals = new ValidIO(UInt(params.xprlen.W))
}

class FrontEndCpuIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(new ValidIO(new FrontEndReq()))
  val resp = Irrevocable(new FrontEndResp())
}

class FrontEndIO(implicit params: HajimeCoreParams) extends Bundle {
  val cpu = new FrontEndCpuIO()
  val icache_axi4lite = new AXI4liteIO(addr_width = params.xprlen, data_width = 32)
  val reset_vector = Input(UInt(params.xprlen.W))
}

// TODO: add inst address misaligned, access fault
class Frontend(implicit params: HajimeCoreParams) extends Module {
  import params._
  val io = IO(new FrontEndIO())
  io := DontCare

  val pc_reg = RegInit(new ProgramCounter().initialise(io.reset_vector))
  val addr_req_to_axi_ar = MuxCase(pc_reg.nextPC, Seq(
    io.cpu.req.valid -> io.cpu.req.bits.pc,
    // if AXI AR is not ready, or AXI R is not valid, or CPU is not ready, retain PC
    (!io.icache_axi4lite.ar.ready || !io.icache_axi4lite.r.valid || !io.cpu.resp.ready) -> pc_reg.addr,
  ))

  // If CPU gets instruction from Frontend, move PC to next one
  when(io.cpu.resp.valid || io.cpu.resp.ready) {
    pc_reg.addr := addr_req_to_axi_ar
  }

  io.icache_axi4lite.ar.bits.addr := addr_req_to_axi_ar
  io.icache_axi4lite.ar.bits.prot := 0.U
  io.icache_axi4lite.ar.valid := io.cpu.resp.ready // || io.cpu.out.valid

  io.cpu.resp.bits.pc := pc_reg
  io.cpu.resp.bits.inst.bits := io.icache_axi4lite.r.bits.data
  io.cpu.resp.valid := io.icache_axi4lite.r.valid
  io.icache_axi4lite.r.ready := io.cpu.resp.ready // || io.cpu.out.valid

  val instAccessFault = pc_reg.addr > 0x1FFC.U
  val instAddressMisaligned = pc_reg.addr(1,0) =/= 0.U
  io.cpu.resp.bits.exceptionSignals.bits := MuxCase(0.U, Seq(
    instAccessFault -> Causes.fetch_access.U,
    instAddressMisaligned -> Causes.misaligned_fetch.U,
  ))
  io.cpu.resp.bits.exceptionSignals.valid := instAccessFault || instAddressMisaligned
}

object Frontend extends App {
  def apply(implicit params: HajimeCoreParams): Frontend = new Frontend()
  ChiselStage.emitSystemVerilogFile(Frontend(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}

