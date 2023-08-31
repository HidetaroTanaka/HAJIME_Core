package hajime.publicmodules

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common.RISCV_Consts._
import hajime.common._

class ALUIO(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val funct = Input(new ID_output)
  val in1 = Input(UInt(xprlen.W))
  val in2 = Input(UInt(xprlen.W))
  val out = Output(UInt(xprlen.W))
}

class ALU(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  import params._
  val io = IO(new ALUIO())
  require(xprlen == 64 || xprlen == 32)

  val in1_record = if(xprlen == 64) Mux(io.funct.op32, Mux(io.funct.arithmetic_funct === ARITHMETIC_FCN.SR.asUInt && !io.funct.alu_flag, Cat(0.U(32.W), io.in1(31,0)), Functions.sign_ext(io.in1(31,0), xprlen)), io.in1) else io.in1
  val in2_record = if(xprlen == 64) Mux(io.funct.op32, Functions.sign_ext(io.in2(31,0), xprlen), io.in2) else io.in2

  val shin = Mux(io.funct.arithmetic_funct === ARITHMETIC_FCN.SR.asUInt, in1_record, Reverse(in1_record))
  val right_barrel_shifter = Module(new BarrelShifter())
  right_barrel_shifter.io.in := Cat(io.funct.alu_flag & shin(xprlen-1), shin)
  right_barrel_shifter.io.shamt := Mux(io.funct.op32, Cat(false.B, in2_record(4,0)), in2_record(5,0))
  val shout_r = right_barrel_shifter.io.out(xprlen-1, 0)
  val shout_l = Reverse(shout_r)

  val addsub_record = Mux(io.funct.alu_flag, in1_record - in2_record, in1_record + in2_record)
  io.out := MuxLookup(io.funct.arithmetic_funct, 0.U(xprlen.W))(
    Seq(
      ARITHMETIC_FCN.ADDSUB -> Mux(io.funct.op32, Functions.sign_ext(addsub_record(31,0), xprlen), addsub_record),
      ARITHMETIC_FCN.SLL -> Mux(io.funct.op32, Functions.sign_ext(shout_l(31,0), xprlen), shout_l),
      ARITHMETIC_FCN.SLT -> (io.in1.asSInt < io.in2.asSInt).asUInt,
      ARITHMETIC_FCN.SLTU -> (io.in1 < io.in2),
      ARITHMETIC_FCN.XOR -> (io.in1 ^ io.in2),
      ARITHMETIC_FCN.SR -> Mux(io.funct.op32, Functions.sign_ext(shout_r(31,0), xprlen), shout_r),
      ARITHMETIC_FCN.OR -> (io.in1 | io.in2),
      ARITHMETIC_FCN.AND -> (io.in1 & io.in2),
    ).map{
      case (arith_fun, uint) => (arith_fun.asUInt, uint)
    }
  )
}

object ALU extends App {
  def apply(implicit params: HajimeCoreParams): ALU = new ALU()
  ChiselStage.emitSystemVerilogFile(ALU(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
