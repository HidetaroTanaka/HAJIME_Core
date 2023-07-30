package hajime.simple4Stage

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common._

class BranchEvaluatorReq(implicit params: HajimeCoreParams) extends Bundle with ScalarOpConstants {
  import params._
  val ALU_Result = UInt(xprlen.W)
  val BranchType = UInt(Branch.NONE.getWidth.W)
  // conditional branch: pc+b_imm, jalr: RAS
  val destPC = UInt(xprlen.W)
  val pc = new ProgramCounter()
  // if branch prediction is taken and miss, out is pc+4
  // else if branch prediction is not taken and miss, out is b_imm
  // else if jalr prediction is miss, out is ALU_Result
  val bp_taken = Bool()
}

class BranchEvaluatorIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(new ValidIO(new BranchEvaluatorReq()))
  val out = new ValidIO(new FrontEndReq())
}

class BranchEvaluator(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new BranchEvaluatorIO())

  // Branch Insts
  val branch_taken = MuxLookup(io.req.bits.BranchType, false.B)(
    Seq(
      Branch.EQ -> !(io.req.bits.ALU_Result.orR),
      Branch.NE -> io.req.bits.ALU_Result.orR,
      Branch.LT -> io.req.bits.ALU_Result(0),
      Branch.LTU -> io.req.bits.ALU_Result(0),
      Branch.GE -> !io.req.bits.ALU_Result(0),
      Branch.GEU -> !io.req.bits.ALU_Result(0),
    ).map{
      case (branch, res) => (branch.asUInt, res)
    }
  )

  io.out.valid := io.req.valid && MuxLookup(io.req.bits.BranchType, false.B)(
    Seq(
      Branch.JALR.asUInt -> (io.req.bits.destPC =/= io.req.bits.ALU_Result),
    ) ++ Branch.condBranchList.map(
      x => x.asUInt -> (io.req.bits.bp_taken =/= branch_taken)
    )
  )
  io.out.bits.pc := Mux(io.req.bits.BranchType === Branch.JALR.asUInt, io.req.bits.ALU_Result & Cat(Fill(params.xprlen-1, true.B), false.B), Mux(
    branch_taken, io.req.bits.destPC, io.req.bits.pc.nextPC
  ))
}

object BranchEvaluator extends App {
  def apply(implicit params: HajimeCoreParams): BranchEvaluator = new BranchEvaluator()
  ChiselStage.emitSystemVerilogFile(apply(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}