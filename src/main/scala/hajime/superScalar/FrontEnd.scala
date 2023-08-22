package hajime.superScalar

import chisel3._
import chisel3.util._
import hajime.axiIO._
import hajime.common._

class FrontEndReq(implicit params: HajimeCoreParams) extends Bundle {
  val pc = UInt(params.xprlen.W)
}
class FrontEndResp(implicit params: HajimeCoreParams) extends Bundle {
  val pc = UInt(params.xprlen.W)
  val inst = UInt(RISCV_Consts.INST_LEN.W)
}

class FrontEndCpuIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(new ValidIO(new FrontEndReq()))
  val resp = new DecoupledIO(Vec(params.issue_width, new FrontEndResp()))
}

class FrontEndIO(implicit params: HajimeCoreParams) extends Bundle {
  val cpu = new FrontEndCpuIO()
  val icache_axi4lite = new AXI4liteIO(addr_width = params.xprlen, data_width = RISCV_Consts.INST_LEN * params.issue_width)
  val reset_vector = Input(UInt(params.xprlen.W))
  val exception = Output(Bool())
}

class FrontEnd(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new FrontEndIO())
  io := DontCare

  val pc_reg = RegInit(io.reset_vector)
  io.icache_axi4lite.ar.bits.addr := MuxCase(pc_reg+(params.issue_width.U << 2), Seq(
    io.cpu.req.valid -> io.cpu.req.bits.pc,
    (!io.icache_axi4lite.ar.ready || !io.icache_axi4lite.r.valid || !io.cpu.resp.ready) -> pc_reg,
  ))

  when(io.cpu.resp.valid || io.cpu.resp.ready) {
    pc_reg := io.icache_axi4lite.ar.bits.addr
  }

  io.icache_axi4lite.ar.bits.prot := 0.U
  io.icache_axi4lite.ar.valid := io.cpu.resp.ready || io.cpu.req.valid

  for((d,i) <- io.cpu.resp.bits.zipWithIndex) {
    d.pc := pc_reg + (i.U << 2)
    d.inst := io.icache_axi4lite.r.bits.data((i+1)*RISCV_Consts.INST_LEN-1,i*RISCV_Consts.INST_LEN)
  }
  io.cpu.resp.valid := io.icache_axi4lite.r.valid
  io.icache_axi4lite.r.ready := io.cpu.resp.ready || io.cpu.req.valid

  io.exception := io.icache_axi4lite.r.bits.exception
}

object FrontEnd extends App {
  def apply(implicit params: HajimeCoreParams): FrontEnd = new FrontEnd()
  (new chisel3.stage.ChiselStage).emitVerilog(apply(HajimeCoreParams()), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}