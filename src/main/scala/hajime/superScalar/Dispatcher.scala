package hajime.superScalar

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.common._
import hajime.publicmodules.{ALU_functIO, ID_output, MEM_ctrl_IO}

class DispatcherResp(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val pc = UInt(xprlen.W)
  val pc_if_mispredict = UInt(xprlen.W)
  val renamed_rs1 = Valid(UInt(log2Up(physicalRegFileEntries).W))
  val renamed_rs2 = Valid(UInt(log2Up(physicalRegFileEntries).W))
  val renamed_rd  = Valid(UInt(log2Up(physicalRegFileEntries).W))
  val immediate = Valid(UInt(xprlen.W))
  val decoder_out = Valid(new ID_output)
  val debug_inst = if(debug) Some(UInt(RISCV_Consts.INST_LEN.W)) else None
}

class DispatcherIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(new DecoupledIO(Vec(params.issue_width, new FrontEndResp())))
  val resp_toExecutor = new DecoupledIO(Vec(params.issue_width, new DispatcherResp()))
  val resp_toFrontEnd = new ValidIO(new FrontEndReq())
}

class Dispatcher {

}
