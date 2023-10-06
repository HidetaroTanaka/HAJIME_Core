package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common._
import hajime.publicmodules._
import chisel3.experimental.BundleLiterals._

class VectorExecUnitSignalIn(implicit params: HajimeCoreParams) extends Bundle {
  val vs1 = UInt(5.W)
  val vs2 = UInt(5.W)
  val vd = UInt(5.W)
  // vectorDecode内のvmを使う
  // val vm = Bool()
  val scalarVal = UInt(params.xprlen.W)
  val vectorDecode = new VectorDecoderResp()
  val vecConf = new VecCtrlUnitResp()
}

class VectorExecUnitDataOut(implicit params: HajimeCoreParams) extends Bundle {
  val toVRF = ValidIO(new VecRegFileWriteReq())
}

class VectorExecUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val signalIn = Flipped(DecoupledIO(new VectorExecUnitSignalIn()))
  val readVrf = Flipped(new VecRegFileReadIO())
  val dataOut = Output(new VectorExecUnitDataOut())
}

/**
 * combinational execution unit (arithmetic/logical)
 * @param params
 */
abstract class VectorExecUnit(implicit params: HajimeCoreParams) extends Module with VectorOpConstants {
  // 演算内容をここに書けばop関数はいらない？
  def exec(vectorDec: VectorDecoderResp, values: VecRegFileReadResp): Seq[UInt]

  val io = IO(new VectorExecUnitIO())

  val idx = RegInit(0.U(log2Up(params.vlen/8).W))
  when((io.signalIn.valid && io.signalIn.ready) || io.dataOut.toVRF.bits.last) {
    idx := 0.U
  } .otherwise {
    idx := idx + 1.U
  }

  val instInfoReg = RegInit(Valid(new VectorExecUnitSignalIn()).Lit(
    _.valid -> false.B,
  ))

  when(io.signalIn.valid && io.signalIn.ready) {
    instInfoReg.valid := true.B
    instInfoReg.bits := io.signalIn.bits
  } .elsewhen(io.dataOut.toVRF.bits.last) {
    instInfoReg.valid := false.B
  } .otherwise {
    instInfoReg := instInfoReg
  }

  io.readVrf.req.idx := idx
  io.readVrf.req.sew := instInfoReg.bits.vecConf.vtype.vsew
  io.readVrf.req.vs1 := instInfoReg.bits.vs1
  io.readVrf.req.vs2 := instInfoReg.bits.vs2
  io.readVrf.req.vd := instInfoReg.bits.vd

  val execValue1 = Mux(instInfoReg.bits.vectorDecode.vSource === VSOURCE.VV.asUInt, io.readVrf.resp.vs1Out, instInfoReg.bits.scalarVal)
  val execValue2 = io.readVrf.resp.vs2Out
  val execValue3 = io.readVrf.resp.vdOut
  val execValueVM = io.readVrf.resp.vm

  val valueToExec = Wire(new VecRegFileReadResp())
  valueToExec.vs1Out := execValue1
  valueToExec.vs2Out := execValue2
  valueToExec.vdOut := execValue3
  valueToExec.vm := execValueVM

  io.dataOut.toVRF.bits.last := (idx-1.U === instInfoReg.bits.vecConf.vl)
  io.dataOut.toVRF.bits.index := idx
  io.dataOut.toVRF.valid := instInfoReg.valid
  io.dataOut.toVRF.bits.vtype := instInfoReg.bits.vecConf.vtype
  io.dataOut.toVRF.bits.vd := instInfoReg.bits.vd
  io.dataOut.toVRF.bits.writeReq := instInfoReg.valid

  io.signalIn.ready := !instInfoReg.valid || io.dataOut.toVRF.bits.last

  val execResult = exec(instInfoReg.bits.vectorDecode, valueToExec)

  /*
  io.dataOut.toVRF.bits.data := MuxLookup(io.signalIn.bits.vectorDecode.veuFun, 0.U) (
    (0 until instNum).map(i => i.U -> exec(io.signalIn.bits.scalarDecode, io.signalIn.bits.vectorDecode, io.dataIn)(i))
  )
   */
}

class ArithmeticVectorExecUnit(implicit params: HajimeCoreParams) extends VectorExecUnit {
  override def exec(vectorDec: VectorDecoderResp, values: VecRegFileReadResp): Seq[UInt] = {
    import values._
    val value1 = vs1Out
    val value2 = vs2Out
    // vadd, vsub, vrsub, (vadc, vmadc), (vsbc, vmsbc),
    // seq, sne,
    // sltu, slt, sleu, sle,
    // sgtu, sgt,
    // minu, min,
    // maxu, max,
    // merge, mv
    // TODO: optimise (64bit加減算器1つで再現できる）
    // is firtool doing enough optimisations?
    value2 + value1 :: value2 - value1 :: value1 - value2 :: (value2 +& value1) + vm :: (value2 -& value1) - vm ::
      (value2 === value1) :: !(value2 === value1) ::
      (value2 < value1) :: (value2.asSInt < value1.asSInt) :: !(value2 > value1) :: !(value2.asSInt > value2.asSInt) ::
      (value2 > value1) :: (value2.asSInt > value2.asSInt) ::
      Mux(value2 < value1, value2, value1) :: Mux(value2.asSInt < value1.asSInt, value2, value1) ::
      Mux(value2 > value1, value2, value2) :: Mux(value2.asSInt > value1.asSInt, value2, value1) ::
      Mux(vm, value1, value2) :: value1 :: Nil
  }

  val rawResult = MuxLookup(instInfoReg.bits.vectorDecode.veuFun, 0.U)(Seq(
    VEU_FUN.ADD -> 0,
    VEU_FUN.SUB -> 1,
    VEU_FUN.RSUB -> 2,
    VEU_FUN.ADC -> 3,
    VEU_FUN.MADC -> 3,
    VEU_FUN.SBC -> 4,
    VEU_FUN.MSBC -> 4,
    VEU_FUN.SEQ -> 5,
    VEU_FUN.SNE -> 6,
    VEU_FUN.SLTU -> 7,
    VEU_FUN.SLT -> 8,
    VEU_FUN.SLEU -> 9,
    VEU_FUN.SLE -> 10,
    VEU_FUN.SGTU -> 11,
    VEU_FUN.SGT -> 12,
    VEU_FUN.MINU -> 13,
    VEU_FUN.MIN -> 14,
    VEU_FUN.MAXU -> 15,
    VEU_FUN.MAX -> 16,
    VEU_FUN.MERGE -> 17,
    VEU_FUN.MV -> 18,
  ).map(x => x._1.asUInt -> execResult(x._2)))

  // 書き込み先がマスクならばidxとうまい具合に組み合わせてなんとかする
  // seq, ..., sgtならば最下位bitがvm
  // vmadc, vmsbcならば，SEW=8 -> 8bit目，..., SEW=64 -> 64bit目がvm
  import VEU_FUN._
  val writeVm = MuxCase(false.B, Seq(
    instInfoReg.bits.vectorDecode.veuFun.isCompMask -> rawResult(0),
    instInfoReg.bits.vectorDecode.veuFun.isCarryMask -> {
      MuxLookup(instInfoReg.bits.vecConf.vtype.vsew, false.B)(
        (0 until 4).map(i => i.U -> rawResult(8 << i))
      )
    }
  ))

  // idx(2,0)に応じてvdと合体
  io.dataOut.toVRF.bits.data := Mux(instInfoReg.bits.vectorDecode.veuFun.isMask, MuxLookup(idx(2,0), rawResult)(
    (0 until 8).map(
      i => i.U -> Cat((0 until 8).reverse.map(
        j => if(j == i) writeVm else io.readVrf.resp.vdOut(j)
      ))
    )
  ), rawResult)
  io.dataOut.toVRF.bits.vm := instInfoReg.bits.vectorDecode.veuFun.isMask
  // if inst[25] is 1, unmasked. if 0, write only v0.mask[i] = 1. vadc, vmadc, vsbc, vmsbc always writes to vd regardless of v0.mask
  io.dataOut.toVRF.bits.writeReq := instInfoReg.bits.vectorDecode.vm || io.readVrf.resp.vm || instInfoReg.bits.vectorDecode.veuFun.ignoreMask
}

class LogicalVectorExecUnit(implicit params: HajimeCoreParams) extends VectorExecUnit {
  override def exec(vectorDec: VectorDecoderResp, values: VecRegFileReadResp): Seq[UInt] = ???
}

object ArithmeticVectorExecUnit extends App {
  implicit val params: HajimeCoreParams = HajimeCoreParams()
  def apply(implicit params: HajimeCoreParams): ArithmeticVectorExecUnit = new ArithmeticVectorExecUnit()
  ChiselStage.emitSystemVerilogFile(new ArithmeticVectorExecUnit(), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}