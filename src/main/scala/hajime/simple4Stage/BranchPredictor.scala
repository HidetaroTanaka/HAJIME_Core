package hajime.simple4Stage

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common._
import hajime.common.RISCV_Consts._

class BranchPredictorIO(implicit params: HajimeCoreParams) extends Bundle with ScalarOpConstants {
  // 分岐成立予測であれば，io.req.validはtrue
  // （分岐不成立予測なら単にPC+4を入れるだけ）
  // 分岐先予測はio.req.bits.pc
  val req = new ValidIO(new FrontEndReq())
  val pc = Input(new ProgramCounter())
  val b_imm = Input(UInt(params.xprlen.W))
  val BranchType = UInt(Branch.NONE.getWidth.W)
}

class BranchPredictor(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new BranchPredictorIO())
  // use static branch predictor for placeholder
  val branch_taken_predict = Wire(Bool())
  branch_taken_predict := MuxLookup(io.BranchType, false.B)(
    Branch.condBranchList.map(
      x => x.asUInt -> (io.pc.addr > io.b_imm)
    ) ++ Branch.jumpList.map(
      x => x.asUInt -> true.B
    )
  )

  // if jal ra, LABEL, push PC+4
  // if jalr x0, ra, 0, pop
  // i'll ignore other shit
  val RAS = RegInit(VecInit(Seq.fill(params.ras_depth)(0.U(params.xprlen.W))))
  val RAS_ptr = RegInit(0.U(log2Up(params.ras_depth).W))

  def RAS_push(value: UInt): Unit = {
    RAS(RAS_ptr) := value
    RAS_ptr := Mux(RAS_ptr === (params.ras_depth-1).U, (params.ras_depth-1).U, RAS_ptr + 1.U)
  }
  def RAS_pop(): UInt = {
    val pop_value = RAS(RAS_ptr)
    RAS_ptr := Mux(RAS_ptr === 0.U, 0.U, RAS_ptr - 1.U)
    pop_value
  }
  when(io.BranchType === Branch.JAL.asUInt) {
    RAS_push(io.pc.nextPC)
  }

  io.req.valid := branch_taken_predict
  io.req.bits.pc := Mux(io.BranchType === Branch.JALR.asUInt, RAS_pop(), io.pc.addr + io.b_imm)
}

object BranchPredictor extends App {
  def apply(implicit params: HajimeCoreParams): BranchPredictor = new BranchPredictor()
  ChiselStage.emitSystemVerilogFile(BranchPredictor(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}