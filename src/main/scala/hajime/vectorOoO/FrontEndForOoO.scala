package hajime.vectorOoO

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common._
import hajime.simple4Stage._
import hajime.common.BundleInitializer._

class FrontEndForOoO(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new FrontEndIO())

  val pc_reg = RegInit(Valid(new ProgramCounter()).Init(
    _.valid -> true.B,
    _.bits.addr -> io.reset_vector,
  ))
  val toAxiAR = MuxCase(pc_reg.bits.nextPC, Seq(
    io.cpu.req.valid -> io.cpu.req.bits.addr,
    // axiがreadyでなければPCを維持
    (!io.icache_axi4lite.ar.ready || !io.icache_axi4lite.r.valid || !io.cpu.resp.ready) -> pc_reg.bits.addr
  ))
  // cpuがFrontEndから命令を読み取ればaddr
  when(io.cpu.resp.valid && io.cpu.resp.ready) {
    pc_reg := io.cpu.req.bits
  }
}
