package hajime.publicmodules

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chisel3.experimental.BundleLiterals._
import hajime.common._

class Multiplier_nxn(n: Int) extends Module {
  val io = IO(new Bundle{
    val multiplicand = Input(UInt(n.W))
    val multiplier = Input(UInt(n.W))
    val out = Output(UInt((n*2).W))
  })

  val pp: Seq[UInt] = (0 until n).map(i => Fill(n, io.multiplier(i)) & io.multiplicand)
  io.out := pp.zipWithIndex.map {
    case (d, i) => (Cat(0.U(n.W), d) << i).asUInt
  }.reduce(_ + _)
}

object Multiplier_nxn extends App {
  def apply(n: Int): Multiplier_nxn = new Multiplier_nxn(n)
  (new ChiselStage).emitVerilog(apply(8), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}

class Multiplier_64x8 extends Module {
  val io = IO(new Bundle{
    val multiplicand = Input(UInt(64.W))
    val multiplier = Input(UInt(8.W))
    val out = Output(UInt(72.W))
  })

  val subMultipliers = (0 until 8).map(_ => Module(new Multiplier_nxn(8)))
  for((d,i) <- subMultipliers.zipWithIndex) {
    d.io.multiplicand := io.multiplicand((i+1)*8-1, i*8)
    d.io.multiplier := io.multiplier
  }
  io.out := subMultipliers.map(x => x.io.out).zipWithIndex.map {
    case (d,i) => (Cat(0.U(56.W), d) << (i*8)).asUInt
  }.reduce(_ + _)
}

class MultiplierReq(implicit params: HajimeCoreParams) extends Bundle {
  val multiplicand = new Bundle{
    val bits = UInt(params.xprlen.W)
    val signed = Bool()
  }
  val multiplier = new Bundle {
    val bits = UInt(params.xprlen.W)
    val signed = Bool()
  }
  val tag = UInt(params.robTagWidth.W)
}

class MultiplierResp(implicit params: HajimeCoreParams) extends Bundle {
  val result = UInt((params.xprlen*2).W)
  val tag = UInt(params.robTagWidth.W)
}

class Multiplier(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new Bundle{
    val req = Flipped(DecoupledIO(new MultiplierReq()))
    val resp = DecoupledIO(new MultiplierResp())
    val debug = if(params.debug) Some(Output(MixedVec((0 until 7).map(
      i => Valid(Multiplier_InternalReg(params, stage=i))
    )))) else None
  })
  require(params.xprlen == 64)
  io := DontCare

  class Multiplier_InternalReg(implicit params: HajimeCoreParams, stage: Int) extends Bundle {
    require(0 <= stage && stage < 7)
    val tag = UInt(params.robTagWidth.W)
    val result = UInt((params.xprlen + (stage+1)*8).W)
    val multiplicand = UInt(params.xprlen.W)
    val multiplier = UInt((params.xprlen - (stage+1)*8).W)
  }
  object Multiplier_InternalReg {
    def apply(implicit params: HajimeCoreParams, stage: Int): Multiplier_InternalReg = new Multiplier_InternalReg()
    def default_withValid(implicit params: HajimeCoreParams, stage: Int): Valid[Multiplier_InternalReg] = {
      val fuck = Wire(Valid(apply(params, stage)))
      fuck.bits := 0.U.asTypeOf(fuck.bits)
      fuck.valid := false.B
      fuck
    }
  }

  val stageRegisters = RegInit(MixedVecInit((0 until 7).map(i => Multiplier_InternalReg.default_withValid(params, i))))
  val multipliers_64x8 = (0 until 8).map(_ => Module(new Multiplier_64x8))
  multipliers_64x8.foreach(x => x.io := DontCare)

  // STAGE 0
  multipliers_64x8(0).io.multiplicand := io.req.bits.multiplicand.bits
  multipliers_64x8(0).io.multiplier := io.req.bits.multiplier.bits(7,0)
  val stage0_result = multipliers_64x8(0).io.out
  when(!io.resp.ready && stageRegisters(1).valid) {
    stageRegisters(0) := stageRegisters(0)
  } .otherwise {
    stageRegisters(0).valid := io.req.valid
    stageRegisters(0).bits.tag := io.req.bits.tag
    stageRegisters(0).bits.result := stage0_result
    stageRegisters(0).bits.multiplicand := io.req.bits.multiplicand.bits
    stageRegisters(0).bits.multiplier := io.req.bits.multiplier.bits(63, 8)
    if (params.debug) io.debug.get(0) := stageRegisters(0)
  }
  io.req.ready := io.resp.ready || !stageRegisters(0).valid

  /*
  // STAGE 1
  multipliers_64x8(1).io.multiplicand := stageRegisters(0).bits.multiplicand
  multipliers_64x8(1).io.multiplier := stageRegisters(0).bits.multiplier(7,0)
  val stage1_result = (multipliers_64x8(1).io.out << 8.U) + stageRegisters(0).bits.result
  stageRegisters(1).valid := stageRegisters(0).valid
  stageRegisters(1).bits.tag := stageRegisters(0).bits.tag
  stageRegisters(1).bits.result := stage1_result
  stageRegisters(1).bits.multiplicand := stageRegisters(0).bits.multiplicand
  stageRegisters(1).bits.multiplier := stageRegisters(0).bits.multiplier(55, 8)

  if(params.debug) io.debug.get(1) := stageRegisters(1)
   */

  // STAGE 1 TO 6
  for(i <- 1 until 7) {
    multipliers_64x8(i).io.multiplicand := stageRegisters(i-1).bits.multiplicand
    multipliers_64x8(i).io.multiplier := stageRegisters(i-1).bits.multiplier(7,0)
    // retain stageRegister if current information cant be sent to the next
    when(!io.resp.ready && (if(i == 6) true.B else stageRegisters(i+1).valid)) {
      stageRegisters(i) := stageRegisters(i)
    } .otherwise {
      stageRegisters(i) := stageRegisters(i - 1)
      stageRegisters(i).bits.result := (multipliers_64x8(i).io.out << (i * 8).U) + stageRegisters(i - 1).bits.result
      stageRegisters(i).bits.multiplier := stageRegisters(i - 1).bits.multiplier >> 8.U
    }
    if(params.debug) io.debug.get(i) := stageRegisters(i)
  }

  // STAGE 7
  multipliers_64x8(7).io.multiplicand := stageRegisters(6).bits.multiplicand
  multipliers_64x8(7).io.multiplier := stageRegisters(6).bits.multiplier(7,0)
  io.resp.bits.result := (multipliers_64x8(7).io.out << 56.U) + stageRegisters(6).bits.result
  io.resp.bits.tag := stageRegisters(6).bits.tag
  io.resp.valid := stageRegisters(6).valid
}

object Multiplier extends App {
  def apply(implicit params: HajimeCoreParams): Multiplier = new Multiplier()
  (new ChiselStage).emitVerilog(apply(HajimeCoreParams()), args=COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}