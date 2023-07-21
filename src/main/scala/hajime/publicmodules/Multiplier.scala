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
    val req = DecoupledIO(new MultiplierReq())
    val resp = DecoupledIO(new MultiplierResp())
    val debug = Output(Valid(Multiplier_InternalReg(params, stage=0)))
  })
  require(params.xprlen == 64)
  io := DontCare

  class Multiplier_InternalReg(implicit params: HajimeCoreParams, stage: Int) extends Bundle {
    require(0 <= stage && stage < 7)
    val tag = UInt(params.robTagWidth.W)
    val result = UInt((params.xprlen + stage*8).W)
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

  // STAGE 0
  val stage0_SubMultipliers = (0 until 8).map(_ => Module(new Multiplier_nxn(8)))
  for((d,i) <- stage0_SubMultipliers.zipWithIndex) {
    d.io.multiplicand := io.req.bits.multiplicand.bits((i+1)*8-1, i*8)
    d.io.multiplier := io.req.bits.multiplier.bits(7,0)
  }
  val stage0_result = stage0_SubMultipliers.map(x => x.io.out).zipWithIndex.map {
    case (d,i) => (Cat(0.U(56.W), d) << (i*8)).asUInt
  }.reduce(_ + _)
  stageRegisters(0).valid := io.req.valid
  stageRegisters(0).bits.tag := io.req.bits.tag
  stageRegisters(0).bits.result := stage0_result
  stageRegisters(0).bits.multiplicand := io.req.bits.multiplicand.bits
  stageRegisters(0).bits.multiplier := io.req.bits.multiplier.bits(63, 8)

  io.debug := stageRegisters(0)
}

object Multiplier extends App {
  def apply(implicit params: HajimeCoreParams): Multiplier = new Multiplier()
  (new ChiselStage).emitVerilog(apply(HajimeCoreParams()), args=COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}