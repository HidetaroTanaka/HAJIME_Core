package hajime.publicmodules

import chisel3._
import chisel3.util._
import hajime.common._

class BarrelShifterIO(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val in = Input(UInt((xprlen+1).W))
  val shamt = Input(UInt(log2Up(xprlen).W))
  val out = Output(UInt((xprlen+1).W))
}

class BarrelShifter(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new BarrelShifterIO())

  require(params.xprlen == 64, "other than xprlen=64 is not supported because im lazy")

  val inputAsSInt = io.in.asSInt
  val ans_seq = (0 until 6).map(_ => Wire(SInt((params.xprlen+1).W)))
  ans_seq.foreach(_ := 0.S)
  ans_seq(5) := Mux(io.shamt(5), inputAsSInt >> 32, inputAsSInt)
  for(i <- (0 until 5).reverse) {
    ans_seq(i) := Mux(io.shamt(i), ans_seq(i+1) >> (1 << i), ans_seq(i+1))
  }
  io.out := ans_seq(0).asUInt
}
