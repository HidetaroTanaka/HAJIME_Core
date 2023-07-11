package hajime.simple4Stage

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

class BarrelShifterIO(xprlen: Int) extends Bundle {
  val in = Input(UInt((xprlen+1).W))
  val shamt = Input(UInt(log2Up(xprlen).W))
  val out = Output(UInt((xprlen+1).W))
}

class BarrelShifter(xprlen: Int) extends Module {
  val io = IO(new BarrelShifterIO(xprlen))

  require(xprlen == 64, "other than xprlen=64 is not supported because im lazy")

  val inputAsSInt = io.in.asSInt
  val ans_seq = (0 until 6).map(_ => Wire(SInt((xprlen+1).W)))
  ans_seq.foreach(_ := 0.S)
  ans_seq(5) := Mux(io.shamt(5), inputAsSInt >> 32, inputAsSInt)
  for(i <- (0 until 5).reverse) {
    ans_seq(i) := Mux(io.shamt(i), ans_seq(i+1) >> (1 << i), ans_seq(i+1))
  }
  io.out := ans_seq(0).asUInt

  /*
  // 5bit目が1ならば右へ32bitシフト
  val ans_stage1 = Mux(io.shamt(5), inputAsSInt >> 32, inputAsSInt)
  // 4bit目が1ならば右へ16bitシフト
  val ans_stage2 = Mux(io.shamt(4), ans_stage1 >> 16, ans_stage1)
  // 3bit目が1ならば右へ8bitシフト
  val ans_stage3 = Mux(io.shamt(3), ans_stage2 >> 8, ans_stage2)
  // 2bit目が1ならば右へ4bitシフト
  val ans_stage4 = Mux(io.shamt(2), ans_stage3 >> 4, ans_stage3)
  // 1bit目が1ならば右へ2bitシフト
  val ans_stage5 = Mux(io.shamt(1), ans_stage4 >> 2, ans_stage4)
  // 0bit目が0ならば右へ1bitシフト
  val ans_stage6 = Mux(io.shamt(0), ans_stage5 >> 1, ans_stage5)

  io.out := ans_stage6.asUInt
   */
}
