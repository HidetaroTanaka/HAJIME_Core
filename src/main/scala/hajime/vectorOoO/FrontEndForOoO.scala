package hajime.vectorOoO

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.BundleInitializer._
import hajime.common._
import hajime.simple4Stage._

class FrontEndForOoO(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new FrontEndIO())
  io := DontCare

  val pc_reg = RegInit(Valid(new ProgramCounter()).Init(
    _.valid -> false.B,
    _.bits.addr -> io.reset_vector,
  ))
  // PCの更新はCPUが行う
  when(io.cpu.req.valid) {
    pc_reg := io.cpu.req
  }
  .otherwise {
    pc_reg.valid := false.B
  }

  io.icache_axi4lite.ar.bits.addr := Mux(io.cpu.req.valid, io.cpu.req.bits.addr, pc_reg.bits.addr)
  io.icache_axi4lite.ar.bits.prot := 0.U
  io.icache_axi4lite.ar.valid := io.cpu.req.valid || pc_reg.valid

  io.cpu.resp.bits.pc := pc_reg.bits
  io.cpu.resp.bits.inst.bits := io.icache_axi4lite.r.bits.data
  io.cpu.resp.valid := io.icache_axi4lite.r.valid
  io.icache_axi4lite.r.ready := io.cpu.resp.ready

  val instAccessFault = pc_reg.bits.addr > 0x1FFC.U
  val instAddressMisaligned = pc_reg.bits.addr(1, 0) =/= 0.U
  io.cpu.resp.bits.exceptionSignals.bits := MuxCase(0.U, Seq(
    instAccessFault -> Causes.fetch_access.U,
    instAddressMisaligned -> Causes.misaligned_fetch.U,
  ))
  io.cpu.resp.bits.exceptionSignals.valid := instAccessFault || instAddressMisaligned
}

object FrontEndForOoO extends App {
  implicit val params: HajimeCoreParams = HajimeCoreParams()
  def apply(implicit params: HajimeCoreParams): FrontEndForOoO = new FrontEndForOoO()
  ChiselStage.emitSystemVerilogFile(new FrontEndForOoO(), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
