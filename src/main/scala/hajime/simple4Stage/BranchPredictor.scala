package hajime.simple4Stage

import chisel3._
import chisel3.util._
import hajime.common.COMPILE_CONSTANTS
import hajime.common.RISCV_Consts._

class BranchPredictorIO(xprlen: Int) extends Bundle {
  // 分岐成立予測であれば，io.req.validはtrue
  // （分岐不成立予測なら単にPC+4を入れるだけ）
  // 分岐先予測はio.req.bits.pc
  val req = new ValidIO(new FrontEndReq(xprlen))
  val pc = Input(UInt(xprlen.W))
  val pc4 = Input(UInt(xprlen.W))
  val branch_pc_if_taken = Input(UInt(xprlen.W))
  val isJALR = Input(Bool())
  val isJAL = Input(Bool())
  val isBranch = Input(Bool())
}

class BranchPredictor(xprlen: Int) extends Module {
  val io = IO(new BranchPredictorIO(xprlen))
  // use static branch predictor for placeholder
  val branch_taken = Wire(Bool())
  branch_taken := MuxCase(false.B,
    Array(
      (io.isBranch) -> (io.pc > io.branch_pc_if_taken),
      io.isJALR -> true.B,
      io.isJAL  -> true.B
    )
  )

  // if jal ra, LABEL, push PC+4
  // if jalr x0, ra, 0, pop
  // i'll ignore other shit
  val RAS = Reg(Vec(8, UInt(xprlen.W)))
  val RAS_ptr = Reg(UInt(4.W))

  def RAS_push(value: UInt): Unit = {
    RAS(RAS_ptr) := value
    RAS_ptr := Mux(RAS_ptr === RAS.length.U, RAS.length.U, RAS_ptr + 1.U)
  }
  def RAS_pop(): UInt = {
    val pop_value = RAS(RAS_ptr)
    RAS_ptr := Mux(RAS_ptr === 0.U, 0.U, RAS_ptr - 1.U)
    pop_value
  }
  when(io.isJAL) {
    RAS_push(io.pc4)
  }

  io.req.valid := branch_taken
  io.req.bits.pc := Mux(io.isJALR, RAS_pop(), io.branch_pc_if_taken)
}

object BranchPredictor extends App {
  def apply(xprlen: Int): BranchPredictor = new BranchPredictor(xprlen)
  (new chisel3.stage.ChiselStage).emitVerilog(apply(xprlen = XLEN), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}