package hajime.simple4Stage

import chisel3._
import chisel3.util._
import hajime.common.{COMPILE_CONSTANTS, RISCV_Consts}
import hajime.common.Deprecated_ScalarOpConstants._

class BranchEvaluatorReq(xprlen: Int) extends Bundle {
  val ALU_Result = UInt(xprlen.W)
  val BranchType = UInt(BR_N.getWidth.W)
  val jalr_predPC = UInt(xprlen.W)
  val PC_if_bp_incorrect = UInt(xprlen.W)
  val bp_taken = Bool()
  val PC_WB_ctrl = UInt(PCWB_X.getWidth.W)
}

class BranchEvaluatorIO(xprlen: Int) extends Bundle {
  val req = Flipped(new ValidIO(new BranchEvaluatorReq(xprlen)))
  val out = new ValidIO(new FrontEndReq(xprlen))
}

class BranchEvaluator(xprlen: Int) extends Module {
  val io = IO(new BranchEvaluatorIO(xprlen))

  // Branch Insts
  val branch_taken = MuxLookup(io.req.bits.BranchType, false.B)(
    Seq(
      BR_EQ -> !(io.req.bits.ALU_Result.orR),
      BR_NE -> io.req.bits.ALU_Result.orR,
      BR_LT -> io.req.bits.ALU_Result(0),
      BR_LTU -> io.req.bits.ALU_Result(0),
      BR_GE -> !io.req.bits.ALU_Result(0),
      BR_GEU -> !io.req.bits.ALU_Result(0),
    )
  )

  io.out.valid := io.req.valid && MuxLookup(io.req.bits.PC_WB_ctrl, false.B)(
    Seq(
      PCWB_JALR   -> (io.req.bits.jalr_predPC =/= io.req.bits.ALU_Result),
      PCWB_BRANCH -> (io.req.bits.bp_taken =/= branch_taken),
    )
  )
  io.out.bits.pc := Mux(io.req.bits.PC_WB_ctrl === PCWB_JALR, io.req.bits.ALU_Result & Cat(Fill(xprlen-1, true.B), false.B), io.req.bits.PC_if_bp_incorrect)
}

object BranchEvaluator extends App {
  def apply(xprlen: Int): BranchEvaluator = new BranchEvaluator(xprlen)
  (new chisel3.stage.ChiselStage).emitVerilog(apply(xprlen = RISCV_Consts.XLEN), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}