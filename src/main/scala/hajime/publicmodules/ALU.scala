package hajime.publicmodules

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.common.RISCV_Consts._
import hajime.common.ScalarOpConstants._
import hajime.common.{COMPILE_CONSTANTS, Functions}


class ALU_functIO extends Bundle {
  val fn = UInt(ALU_X.getWidth.W)
  val addsubFlag = Bool()
  val op32 = Bool()
}

class ALUIO(xprlen: Int) extends Bundle {
  val funct = Input(new ALU_functIO)
  val in1 = Input(UInt(xprlen.W))
  val in2 = Input(UInt(xprlen.W))
  val out = Output(UInt(xprlen.W))
}

class ALU(xprlen: Int) extends Module {
  val io = IO(new ALUIO(xprlen))
  require(xprlen == XLEN || xprlen == 32)

  val in1_record = if(xprlen == 64) Mux(io.funct.op32, Mux(io.funct.fn === ALU_SR && !io.funct.addsubFlag, Cat(0.U(32.W), io.in1(31,0)), Functions.sign_ext(io.in1(31,0), xprlen)), io.in1) else io.in1
  val in2_record = if(xprlen == 64) Mux(io.funct.op32, Functions.sign_ext(io.in2(31,0), xprlen), io.in2) else io.in2

  val shin = Mux(io.funct.fn === ALU_SR, in1_record, Reverse(in1_record))
  val right_barrel_shifter = Module(new BarrelShifter(xprlen))
  right_barrel_shifter.io.in := Cat(io.funct.addsubFlag & shin(xprlen-1), shin)
  right_barrel_shifter.io.shamt := Mux(io.funct.op32, Cat(false.B, in2_record(4,0)), in2_record(5,0))
  val shout_r = right_barrel_shifter.io.out(xprlen-1, 0)
  val shout_l = Reverse(shout_r)

  val addsub_record = Mux(io.funct.addsubFlag, in1_record - in2_record, in1_record + in2_record)
  io.out := MuxLookup(io.funct.fn, 0.U(xprlen.W))(
    Seq(
      ALU_ADDSUB -> Mux(io.funct.op32, Functions.sign_ext(addsub_record(31,0), xprlen), addsub_record),
      ALU_SLL -> Mux(io.funct.op32, Functions.sign_ext(shout_l(31,0), xprlen), shout_l),
      ALU_SLT -> (io.in1.asSInt < io.in2.asSInt).asUInt,
      ALU_SLTU -> (io.in1 < io.in2),
      ALU_XOR -> (io.in1 ^ io.in2),
      ALU_SR -> Mux(io.funct.op32, Functions.sign_ext(shout_r(31,0), xprlen), shout_r),
      ALU_OR -> (io.in1 | io.in2),
      ALU_AND -> (io.in1 & io.in2),
    )
  )
}

object ALU extends App {
  def apply(xprlen: Int): ALU = new ALU(xprlen)
  (new ChiselStage).emitVerilog(apply(xprlen = XLEN), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
