package hajime.simple4Stage

import circt.stage.ChiselStage
import chisel3.util._
import chisel3._
import hajime.common._

class RegFileReq(implicit params: HajimeCoreParams) extends Bundle {
  val rd = UInt(5.W)
  val data = UInt(params.xprlen.W)
}

class debug_map_physical_to_abi(implicit params: HajimeCoreParams) extends Bundle {
  import params._
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

  def toList: List[UInt] = (ra :: sp :: gp :: tp :: t0 :: t1 :: t2 :: s0 :: s1 :: a0 :: a1 :: a2 :: a3 :: a4 :: a5 :: a6 :: a7 ::
    s2 :: s3 :: s4 :: s5 :: s6 :: s7 :: s8 :: s9 :: s10 :: s11 :: t3 :: t4 :: t5 :: t6 :: Nil)

  def map_physical_to_abi(implicit params: HajimeCoreParams, regfile: Vec[UInt]): debug_map_physical_to_abi = {
    val res = Wire(new debug_map_physical_to_abi())
    res.zero := 0.U(xprlen.W)
    for((out, physical) <- res.toList zip regfile) {
      out := physical
    }
    res
  }
}

class RegFileIO(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val rs1 = Input(UInt(5.W))
  val rs2 = Input(UInt(5.W))
  val rs1_out = Output(UInt(xprlen.W))
  val rs2_out = Output(UInt(xprlen.W))
  val req = Flipped(ValidIO(new RegFileReq()))
  val debug_abi_map = if(debug) Some(Output(new debug_map_physical_to_abi())) else None
}

class RegFile(implicit params: HajimeCoreParams) extends Module {
  import params._
  val io = IO(new RegFileIO())

  val regfile = Reg(Vec(31, UInt(xprlen.W)))
  if(debug) {
    io.debug_abi_map.get := io.debug_abi_map.get.map_physical_to_abi(params, regfile)
  }

  for(i <- 1 until 32) {
    when(io.req.valid && (io.req.bits.rd === i.U(5.W))) {
      regfile((i-1).U) := io.req.bits.data
    }
  }

  io.rs1_out := 0.U(xprlen.W)
  io.rs2_out := 0.U(xprlen.W)
  for(i <- 1 until 32) {
    when(io.rs1 === i.U(5.W)) {
      io.rs1_out := regfile((i-1).U)
    }
    when(io.rs2 === i.U(5.W)) {
      io.rs2_out := regfile((i-1).U)
    }
  }
}

object RegFile extends App {
  def apply(implicit params: HajimeCoreParams): RegFile = new RegFile()
  // (new ChiselStage).emitVerilog(apply(XLEN, CORE_Consts.debug), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
  ChiselStage.emitSystemVerilogFile(RegFile(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
