package hajime.publicmodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import chisel3.experimental.BundleLiterals._
import hajime.common._
import hajime.publicmodules

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
  ChiselStage.emitSystemVerilogFile(apply(8), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
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
  }
  val multiplier = new Bundle {
    val bits = UInt(params.xprlen.W)
  }
  val tag = UInt(params.robTagWidth.W)
  val sign = Bool()
  val decode = new ID_output()
}

class MultiplierResp(implicit params: HajimeCoreParams) extends Bundle {
  val result = UInt((params.xprlen*2).W)
  val tag = UInt(params.robTagWidth.W)
  val sign = Bool()
  val decode = new ID_output()
}

class Multiplier_InternalReg(implicit params: HajimeCoreParams, stage: Int) extends Bundle {
  require(0 <= stage && stage < 7)
  val tag = UInt(params.robTagWidth.W)
  val result = UInt((params.xprlen + (stage + 1) * 8).W)
  val multiplicand = UInt(params.xprlen.W)
  val multiplier = UInt((params.xprlen - (stage + 1) * 8).W)
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

  // STAGE 1 TO 6
  for(i <- 1 until 7) {
    multipliers_64x8(i).io.multiplicand := stageRegisters(i-1).bits.multiplicand
    multipliers_64x8(i).io.multiplier := stageRegisters(i-1).bits.multiplier(7,0)
    // retain stageRegister if current information cant be sent to the next
    when(!io.resp.ready && (if(i == 6) true.B else stageRegisters(i+1).valid)) {
      stageRegisters(i) := stageRegisters(i)
    } .otherwise {
      stageRegisters(i) := stageRegisters(i - 1)
      stageRegisters(i).bits.result := (multipliers_64x8(i).io.out << (i * 8)).asUInt + stageRegisters(i - 1).bits.result
      stageRegisters(i).bits.multiplier := stageRegisters(i - 1).bits.multiplier >> 8
    }
    if(params.debug) io.debug.get(i) := stageRegisters(i)
  }

  // STAGE 7
  multipliers_64x8(7).io.multiplicand := stageRegisters(6).bits.multiplicand
  multipliers_64x8(7).io.multiplier := stageRegisters(6).bits.multiplier(7,0)
  io.resp.bits.result := (multipliers_64x8(7).io.out << 56).asUInt + stageRegisters(6).bits.result
  io.resp.bits.tag := stageRegisters(6).bits.tag
  io.resp.valid := stageRegisters(6).valid
}

object Multiplier extends App {
  def apply(implicit params: HajimeCoreParams): Multiplier = new Multiplier()
  ChiselStage.emitSystemVerilogFile(apply(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}

class NonPipelinedMultipler_InternalReg(implicit params: HajimeCoreParams) extends Bundle {
  val tag = UInt(params.robTagWidth.W)
  val sign = Bool()
  val result = UInt((params.xprlen * 2).W)
  val multiplicand = UInt(params.xprlen.W)
  val multiplier = UInt(params.xprlen.W)
  val stage = UInt(3.W)
  val decode = new ID_output
}

object NonPipelinedMultipler_InternalReg {
  def apply(implicit params: HajimeCoreParams): NonPipelinedMultipler_InternalReg = new NonPipelinedMultipler_InternalReg()

  def default_withValid(implicit params: HajimeCoreParams): Valid[NonPipelinedMultipler_InternalReg] = {
    val internalWire = Wire(Valid(apply(params)))
    internalWire.bits := 0.U.asTypeOf(internalWire.bits)
    internalWire.valid := false.B
    internalWire
  }
}

/**
 * パイプライン化されていない乗算器
 * req.readyがfalseならば中に情報がある
 * @param params
 */
class NonPipelinedMultiplier(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new Bundle {
    val req = Flipped(Irrevocable(new MultiplierReq()))
    val resp = Irrevocable(new MultiplierResp())
  })
  require(params.xprlen == 64)
  io := DontCare

  val internalReg = RegInit(NonPipelinedMultipler_InternalReg.default_withValid(params))

  val multiplicand_greater_than_multiplier = io.req.bits.multiplicand.bits > io.req.bits.multiplier.bits
  val lessnum = Mux(multiplicand_greater_than_multiplier, io.req.bits.multiplier.bits, io.req.bits.multiplicand.bits)
  val greaternum = Mux(multiplicand_greater_than_multiplier, io.req.bits.multiplicand.bits, io.req.bits.multiplier.bits)

  val accept_current_request = io.req.valid && io.req.ready
  val executor_result = Mux(accept_current_request, 0.U((params.xprlen * 2).W), internalReg.bits.result)
  val executor_multiplicand = Mux(accept_current_request, greaternum, internalReg.bits.multiplicand)
  val executor_multiplier = Mux(accept_current_request, lessnum, internalReg.bits.multiplier)
  val executor_tag = Mux(accept_current_request, io.req.bits.tag, internalReg.bits.tag)
  val executor_sign = Mux(accept_current_request, io.req.bits.sign, internalReg.bits.sign)
  val executor_decode = Mux(accept_current_request, io.req.bits.decode, internalReg.bits.decode)
  val executor_stage = Mux(accept_current_request, 0.U(3.W), internalReg.bits.stage + 1.U(3.W))
  val executor_valid = accept_current_request || internalReg.valid

  val multiplier_64x8 = Module(new Multiplier_64x8)
  multiplier_64x8.io.multiplicand := executor_multiplicand
  multiplier_64x8.io.multiplier := executor_multiplier(7,0)

  // internalRegがvalidであり，respがvalidだがreadyでない場合
  when(internalReg.valid && io.resp.valid && !io.resp.ready) {
    internalReg := internalReg
  // respがvalidだがreadyでなく，かつinternalRegがvalidでないならば，現在の入力を保持する
  // また，現在のreqを受け付けているまたはinternalRegがvalidの場合も同様
  } .elsewhen((!internalReg.valid && io.resp.valid && !io.resp.ready) || accept_current_request || internalReg.valid) {
    internalReg.bits.result := io.resp.bits.result
    internalReg.bits.multiplicand := executor_multiplicand
    internalReg.bits.multiplier := executor_multiplier(63,8)
    internalReg.bits.tag := executor_tag
    internalReg.bits.sign := executor_sign
    internalReg.bits.decode := executor_decode
    internalReg.bits.stage := executor_stage
    internalReg.valid := Mux(io.resp.ready && io.resp.valid, false.B, executor_valid)
  } .otherwise {
    internalReg.valid := false.B
  }

  // internalRegがvalidでないならば，reqを受け取れる
  io.req.ready := !internalReg.valid
  // executor_validかつ，(multiplierが[0,0xFF]、またはstageが7)
  io.resp.valid := executor_valid && ((executor_multiplier(63,8) === 0.U) || executor_stage === 7.U(3.W))
  io.resp.bits.tag := executor_tag
  io.resp.bits.sign := executor_sign
  io.resp.bits.decode := executor_decode
  io.resp.bits.result := executor_result + MuxLookup(executor_stage, multiplier_64x8.io.out)(
    (0.U(3.W) -> multiplier_64x8.io.out) +: (1 until 8).map(i =>
      i.U(3.W) -> Cat(multiplier_64x8.io.out, 0.U((i * 8).W))
    )
  )
}

object NonPipelinedMultiplier extends App {
  def apply(implicit param: HajimeCoreParams): NonPipelinedMultiplier = new NonPipelinedMultiplier()
  ChiselStage.emitSystemVerilogFile(apply(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}

class NonPipelinedMultiplierWrapIOReq(implicit params: HajimeCoreParams) extends Bundle {
  val rs1 = UInt(params.xprlen.W)
  val rs2 = UInt(params.xprlen.W)
  val funct = new ID_output
}
class NonPipelinedMultiplierWrapIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(Irrevocable(new NonPipelinedMultiplierWrapIOReq()))
  val resp = Irrevocable(UInt(params.xprlen.W))
}

/**
 * 符号やデコードの処理等
 */
class NonPipelinedMultiplierWrap(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new NonPipelinedMultiplierWrapIO())

  val nonPipelinedMultiplier = Module(new NonPipelinedMultiplier())

  // rs1のMSBが1であり，かつrs1が符号付きであればrs1の符号を逆転させる
  val rs1_inverted = (io.req.bits.rs1.head(1).asBool &&
    ((io.req.bits.funct.arithmetic_funct === ARITHMETIC_FCN.MUL_HIGH.asUInt) || (io.req.bits.funct.arithmetic_funct === ARITHMETIC_FCN.MUL_HISU.asUInt)))
  // rs2に関しても同様
  val rs2_inverted = (io.req.bits.rs2.head(1).asBool && (io.req.bits.funct.arithmetic_funct === ARITHMETIC_FCN.MUL_HIGH.asUInt))

  nonPipelinedMultiplier.io.req.valid := io.req.valid
  io.req.ready := nonPipelinedMultiplier.io.req.ready
  nonPipelinedMultiplier.io.req.bits.tag := 0.U
  nonPipelinedMultiplier.io.req.bits.decode := io.req.bits.funct

  // invert if rs1.head=1 and MULH, MULHSU
  // half if op32
  nonPipelinedMultiplier.io.req.bits.multiplicand.bits := MuxCase(io.req.bits.rs1, Seq(
    rs1_inverted -> -io.req.bits.rs1,
    io.req.bits.funct.op32 -> io.req.bits.rs1.tail(32)
  ))
  // invert if rs2.head=1 and MULH, half if op32
  nonPipelinedMultiplier.io.req.bits.multiplier.bits := MuxCase(io.req.bits.rs2, Seq(
    rs2_inverted -> -io.req.bits.rs2,
    io.req.bits.funct.op32 -> io.req.bits.rs2.tail(32)
  ))
  nonPipelinedMultiplier.io.req.bits.sign := rs1_inverted ^ rs2_inverted

  io.resp.valid := nonPipelinedMultiplier.io.resp.valid
  nonPipelinedMultiplier.io.resp.ready := io.resp.ready
  val result128 = Mux(nonPipelinedMultiplier.io.resp.bits.sign, -nonPipelinedMultiplier.io.resp.bits.result, nonPipelinedMultiplier.io.resp.bits.result)
  io.resp.bits := MuxCase(result128(127,64), Seq(
    nonPipelinedMultiplier.io.resp.bits.decode.op32 -> Functions.sign_ext(result128(31,0), 64),
    (nonPipelinedMultiplier.io.resp.bits.decode.arithmetic_funct === ARITHMETIC_FCN.MUL_LOW.asUInt) -> result128(63,0),
  ))
}