package hajime.fg_mtCore

import chisel3._
import chisel3.util._
import hajime.common.{CORE_Consts, InstBundle, RISCV_Consts}

class MyInterrupts extends Bundle {
  val mtip = Input(Bool())
}


class CoreIO extends Bundle {
  val imem = new DecoupledIO(UInt(RISCV_Consts.INST_LEN.W))
  val dmem = new DecoupledIO(UInt(RISCV_Consts.XLEN.W))
  val interrupt = new MyInterrupts
  val hartid = Input(Vec(CORE_Consts.THREADS, UInt()))
  val reset_vector = Input(UInt(RISCV_Consts.XLEN.W))
}

class Datapath extends Module {
  val io = IO(new Bundle{
    val inst = Input(new InstBundle(xprlen = RISCV_Consts.XLEN))
  })
  val funct7 = io.inst.funct7

}

class Core extends Module{
  val io = IO(new CoreIO)

}
