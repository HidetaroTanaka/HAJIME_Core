package hajime.simple4Stage

import chisel3.stage.ChiselStage
import chisel3.util._
import chisel3._
import hajime.common.{COMPILE_CONSTANTS, CORE_Consts}
import hajime.common.RISCV_Consts._

class RegFileReq(xprlen: Int) extends Bundle {
  val rd = UInt(5.W)
  val data = UInt(xprlen.W)
}

class debug_map_physical_to_abi(xprlen: Int) extends Bundle {
  val zero = UInt(xprlen.W)
  val ra = UInt(xprlen.W)
  val sp = UInt(xprlen.W)
  val gp = UInt(xprlen.W)
  val tp = UInt(xprlen.W)
  val t0 = UInt(xprlen.W)
  val t1 = UInt(xprlen.W)
  val t2 = UInt(xprlen.W)
  val s0 = UInt(xprlen.W)
  val s1 = UInt(xprlen.W)
  val a0 = UInt(xprlen.W)
  val a1 = UInt(xprlen.W)
  val a2 = UInt(xprlen.W)
  val a3 = UInt(xprlen.W)
  val a4 = UInt(xprlen.W)
  val a5 = UInt(xprlen.W)
  val a6 = UInt(xprlen.W)
  val a7 = UInt(xprlen.W)
  val s2 = UInt(xprlen.W)
  val s3 = UInt(xprlen.W)
  val s4 = UInt(xprlen.W)
  val s5 = UInt(xprlen.W)
  val s6 = UInt(xprlen.W)
  val s7 = UInt(xprlen.W)
  val s8 = UInt(xprlen.W)
  val s9 = UInt(xprlen.W)
  val s10 = UInt(xprlen.W)
  val s11 = UInt(xprlen.W)
  val t3 = UInt(xprlen.W)
  val t4 = UInt(xprlen.W)
  val t5 = UInt(xprlen.W)
  val t6 = UInt(xprlen.W)

  def map_physical_to_abi(regfile: Vec[UInt]): debug_map_physical_to_abi = {
    val res = Wire(new debug_map_physical_to_abi(xprlen))
    res.zero := 0.U(xprlen.W)
    res.ra := regfile(0.U)
    res.sp := regfile(1.U)
    res.gp := regfile(2.U)
    res.tp := regfile(3.U)
    res.t0 := regfile(4.U)
    res.t1 := regfile(5.U)
    res.t2 := regfile(6.U)
    res.s0 := regfile(7.U)
    res.s1 := regfile(8.U)
    res.a0 := regfile(9.U)
    res.a1 := regfile(10.U)
    res.a2 := regfile(11.U)
    res.a3 := regfile(12.U)
    res.a4 := regfile(13.U)
    res.a5 := regfile(14.U)
    res.a6 := regfile(15.U)
    res.a7 := regfile(16.U)
    res.s2 := regfile(17.U)
    res.s3 := regfile(18.U)
    res.s4 := regfile(19.U)
    res.s5 := regfile(20.U)
    res.s6 := regfile(21.U)
    res.s7 := regfile(22.U)
    res.s8 := regfile(23.U)
    res.s9 := regfile(24.U)
    res.s10 := regfile(25.U)
    res.s11 := regfile(26.U)
    res.t3 := regfile(27.U)
    res.t4 := regfile(28.U)
    res.t5 := regfile(29.U)
    res.t6 := regfile(30.U)
    res
  }
}

class RegFileIO(xprlen: Int, debug: Boolean) extends Bundle {
  val rs1 = Input(UInt(RF_INDEX_WIDTH.W))
  val rs2 = Input(UInt(RF_INDEX_WIDTH.W))
  val rs1_out = Output(UInt(xprlen.W))
  val rs2_out = Output(UInt(xprlen.W))
  val req = Flipped(ValidIO(new RegFileReq(xprlen)))
  val debug_abi_map = if(debug) Some(Output(new debug_map_physical_to_abi(xprlen))) else None
}

class RegFile(xprlen: Int, debug: Boolean) extends Module {
  val io = IO(new RegFileIO(xprlen, debug))

  val regfile = Reg(Vec(31, UInt(xprlen.W)))
  if(debug) {
    io.debug_abi_map.get := io.debug_abi_map.get.map_physical_to_abi(regfile)
  }

  for(i <- 1 until 32) {
    when(io.req.valid && (io.req.bits.rd === i.U(RF_INDEX_WIDTH.W))) {
      regfile((i-1).U) := io.req.bits.data
    }
  }

  io.rs1_out := 0.U(xprlen.W)
  io.rs2_out := 0.U(xprlen.W)
  for(i <- 1 until 32) {
    when(io.rs1 === i.U(RF_INDEX_WIDTH.W)) {
      io.rs1_out := regfile((i-1).U)
    }
    when(io.rs2 === i.U(RF_INDEX_WIDTH.W)) {
      io.rs2_out := regfile((i-1).U)
    }
  }
}

object RegFile extends App {
  def apply(xprlen: Int, debug: Boolean): RegFile = new RegFile(xprlen, debug)
  (new ChiselStage).emitVerilog(apply(XLEN, CORE_Consts.debug), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
