package hajime.vectormodules

import chisel3._
import chisel3.experimental.BundleLiterals._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.Functions.{lsHasElementEquivalentToUInt, signExtend}
import hajime.common._
import hajime.publicmodules._
import hajime.simple4Stage._

class VectorExecUnitSignalIn(implicit params: HajimeCoreParams) extends Bundle {
  val vs1 = UInt(5.W)
  val vs2 = UInt(5.W)
  val vd = UInt(5.W)
  // vectorDecode内のvmを使う
  // val vm = Bool()
  val scalarVal = UInt(params.xprlen.W)
  val vectorDecode = new VectorDecoderResp()
  val scalarDecode = new ID_output()
  val vecConf = new VecCtrlUnitResp()
  val pc = new ProgramCounter()
  val debug = if(params.debug) Some(new Debug_Info()) else None
}

class VectorExecUnitDataOut(implicit params: HajimeCoreParams) extends Bundle {
  val toVRF = ValidIO(new VecRegFileWriteReq())
}

class VectorExecUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val signalIn = Flipped(DecoupledIO(new VectorExecUnitSignalIn()))
  val readVrf = Flipped(new VecRegFileReadIO())
  val dataOut = Output(new VectorExecUnitDataOut())
  val toExWbReg = Output(Valid(new EX_WB_IO()))
}

/**
 * combinational execution unit (arithmetic/logical)
 * @param params
 */
abstract class VectorExecUnit(implicit params: HajimeCoreParams) extends Module with VectorOpConstants {
  require(params.useVector, "Insert Funni Amongus Reference here")
  def exec(vectorDec: VectorDecoderResp, values: VecRegFileReadResp, vsew: UInt): Seq[UInt]

  val io = IO(new VectorExecUnitIO())

  val instInfoReg = RegInit(Valid(new VectorExecUnitSignalIn()).Lit(
    _.valid -> false.B,
  ))

  val idx = RegInit(0.U(log2Up(params.vlen/8).W))
  val reductionAccumulator = RegInit(0.U(params.xprlen.W))
  val executedNum = RegInit(0.U(log2Up(params.vlen/8).W))
  when((io.signalIn.valid && io.signalIn.ready) || io.dataOut.toVRF.bits.last) {
    idx := 0.U
    executedNum := 0.U
  } .elsewhen(instInfoReg.valid) {
    idx := idx + 1.U
    executedNum := executedNum + (io.dataOut.toVRF.valid && io.dataOut.toVRF.bits.writeReq).asUInt
  } .otherwise {
    idx := 0.U
    executedNum := 0.U
  }

  when(io.signalIn.valid && io.signalIn.ready) {
    instInfoReg.valid := true.B
    instInfoReg.bits := io.signalIn.bits
  } .elsewhen(io.dataOut.toVRF.bits.last) {
    instInfoReg.valid := false.B
  } .otherwise {
    instInfoReg := instInfoReg
  }

  import VEU_FUN._
  // TODO: vs1Outの判定はMVVではなくベクトルマスク命令か否かで
  io.readVrf.req.idx := Mux(instInfoReg.bits.vectorDecode.vSource === VSOURCE.MVV.asUInt, idx.head(idx.getWidth-3), idx)
  io.readVrf.req.sew := Mux(instInfoReg.bits.vectorDecode.vSource === VSOURCE.MVV.asUInt, 0.U, instInfoReg.bits.vecConf.vtype.vsew)
  io.readVrf.req.vs1 := instInfoReg.bits.vs1
  io.readVrf.req.vs2 := instInfoReg.bits.vs2
  io.readVrf.req.vd := instInfoReg.bits.vd
  // ベクトルマスク命令ならばidx自体が対応するため下げる
  io.readVrf.req.readVdAsMaskSource := instInfoReg.bits.vectorDecode.veuFun.writeAsMask && (instInfoReg.bits.vectorDecode.vSource =/= VSOURCE.MVV.asUInt)

  val execValue1 = Mux(instInfoReg.bits.vectorDecode.vSource === VSOURCE.VV.asUInt || instInfoReg.bits.vectorDecode.vSource === VSOURCE.MVV.asUInt, io.readVrf.resp.vs1Out, instInfoReg.bits.scalarVal)
  val execValue2 = io.readVrf.resp.vs2Out
  val execValue3 = io.readVrf.resp.vdOut
  val execValueVM = io.readVrf.resp.vm

  val valueToExec = Wire(new VecRegFileReadResp())
  valueToExec.vs1Out := execValue1
  valueToExec.vs2Out := execValue2
  valueToExec.vdOut := execValue3
  valueToExec.vm := execValueVM

  io.dataOut.toVRF.bits.last := (idx === instInfoReg.bits.vecConf.vl-1.U) && instInfoReg.valid
  io.dataOut.toVRF.bits.index := Mux(instInfoReg.bits.vectorDecode.veuFun.writeAsMask, idx.head(idx.getWidth-3), idx)
  io.dataOut.toVRF.valid := instInfoReg.valid
  io.dataOut.toVRF.bits.vtype := instInfoReg.bits.vecConf.vtype
  io.dataOut.toVRF.bits.vtype.vsew := Mux(instInfoReg.bits.vectorDecode.veuFun.writeAsMask, 0.U, instInfoReg.bits.vecConf.vtype.vsew)
  io.dataOut.toVRF.bits.vd := instInfoReg.bits.vd
  io.dataOut.toVRF.bits.writeReq := instInfoReg.valid

  io.signalIn.ready := !instInfoReg.valid || io.dataOut.toVRF.bits.last

  val execResult = exec(instInfoReg.bits.vectorDecode, valueToExec, instInfoReg.bits.vecConf.vtype.vsew)

  io.toExWbReg.valid := io.dataOut.toVRF.bits.last
  io.toExWbReg.bits.dataSignals := DontCare
  io.toExWbReg.bits.dataSignals.pc := instInfoReg.bits.pc
  io.toExWbReg.bits.ctrlSignals.decode := instInfoReg.bits.scalarDecode
  io.toExWbReg.bits.ctrlSignals.rd_index := 0.U
  io.toExWbReg.bits.exceptionSignals.valid := false.B
  io.toExWbReg.bits.exceptionSignals.bits := DontCare
  io.toExWbReg.bits.vectorCsrPorts.get := instInfoReg.bits.vecConf
  io.toExWbReg.bits.vectorExecNum.get.valid := io.dataOut.toVRF.bits.last
  io.toExWbReg.bits.vectorExecNum.get.bits := executedNum + (io.dataOut.toVRF.valid && io.dataOut.toVRF.bits.writeReq).asUInt
  if(params.debug) io.toExWbReg.bits.debug.get := instInfoReg.bits.debug.get
}

class IntegerAluExecUnit(implicit params: HajimeCoreParams) extends VectorExecUnit {
  override def exec(vectorDec: VectorDecoderResp, values: VecRegFileReadResp, vsew: UInt): Seq[UInt] = {
    import values._
    val vadcResult = (vs2Out +& vs1Out) + vm
    val vsbcResult = (vs2Out -& vs1Out) - vm
    val vs2Mask = vs2Out(0)
    val vs1Mask = vs1Out(0)
    val vs2ForMult = MuxLookup(vsew, vs2Out)(
      (0 until 4).map(
        // vs2がunsignedならば零拡張，そうでなければ符号拡張
        i => i.U -> {
          val vs2OutMainBits = vs2Out((8 << i) - 1, 0)
          Mux(vectorDec.veuFun === VEU_FUN.MULHU.asUInt, Cat(false.B, vs2OutMainBits), vs2OutMainBits.ext(params.xprlen+1))
        }
      )
    ).asSInt
    val vs1ForMult = MuxLookup(vsew, vs1Out)(
      (0 until 4).map(
        // vs1がunsignedならば零拡張，そうでなければ符号拡張
        i => i.U -> {
          val vs1OutMainBits = vs1Out((8 << i) - 1, 0)
          Mux(Seq(VEU_FUN.MULHU, VEU_FUN.MULHSU).map(_.asUInt).has(vectorDec.veuFun), Cat(false.B, vs1OutMainBits), vs1OutMainBits.ext(params.xprlen+1))
        }
      )
    ).asSInt

    val vdForMult = MuxLookup(vsew, vdOut)(
      (0 until 4).map(
        i => i.U -> vdOut((8 << i) - 1, 0).ext(params.xprlen+1)
      )
    ).asSInt
    // vs2*vs1, vd*vs1
    val multiplyRes = (Mux(Seq(VEU_FUN.MADD, VEU_FUN.NMSUB).map(_.asUInt).has(vectorDec.veuFun), vdForMult, vs2ForMult) * vs1ForMult).asUInt
    val multiplyResLowBits = MuxLookup(vsew, multiplyRes(7,0))(
      (0 until 4).map(
        i => i.U -> multiplyRes((8 << i) - 1, 0)
      )
    )
    // (15, 8)
    // (31, 16)
    // (63, 32)
    // (127, 64)
    val multiplyResHighBits = MuxLookup(vsew, multiplyRes(15, 8))(
      (0 until 4).map(
        i => i.U -> multiplyRes((16 << i) - 1, 8 << i)
      )
    )

    // multiply-add
    val mulAddRes = MuxLookup(vectorDec.veuFun, multiplyResLowBits + vdOut)(Seq(
      VEU_FUN.MACC -> (multiplyRes + vdOut),
      VEU_FUN.NMSAC -> (-multiplyRes + vdOut),
      VEU_FUN.MADD -> (multiplyRes + vs2Out),
      VEU_FUN.NMSUB -> (-multiplyRes + vs2Out),
    ).map {
      case (fcn, res) => (fcn.asUInt -> res)
    })

    // vadd, vsub, vrsub, vadc, vmadc, (vsbc, vmsbc),
    // seq, sne,
    // sltu, slt, sleu, sle,
    // sgtu, sgt,
    // minu, min,
    // maxu, max,
    // merge, mv
    // TODO: optimise (64bit加減算器1つで再現できる）
    // is firtool doing enough optimisations?
    vs2Out + vs1Out :: vs2Out - vs1Out :: vs1Out - vs2Out :: vadcResult.tail(1) :: vadcResult.head(1) :: vsbcResult.tail(1) :: vsbcResult.head(1) ::
      (vs2Out === vs1Out) :: !(vs2Out === vs1Out) ::
      (vs2Out < vs1Out) :: (vs2Out.asSInt < vs1Out.asSInt) :: !(vs2Out > vs1Out) :: !(vs2Out.asSInt > vs1Out.asSInt) ::
      (vs2Out > vs1Out) :: (vs2Out.asSInt > vs1Out.asSInt) ::
      Mux(vs2Out < vs1Out, vs2Out, vs1Out) :: Mux(vs2Out.asSInt < vs1Out.asSInt, vs2Out, vs1Out) ::
      Mux(vs2Out > vs1Out, vs2Out, vs1Out) :: Mux(vs2Out.asSInt > vs1Out.asSInt, vs2Out, vs1Out) ::
      Mux(vm, vs1Out, vs2Out) :: vs1Out ::
      (vs2Out & vs1Out) :: (vs2Out | vs1Out) :: (vs2Out ^ vs1Out) ::
      (vs2Mask && vs1Mask) :: !(vs2Mask && vs1Mask) :: (vs2Mask && !vs1Mask) :: (vs2Mask ^ vs1Mask) ::
      (vs2Mask || vs1Mask) :: !(vs2Mask || vs1Mask) :: (vs2Mask || !vs1Mask) :: !(vs2Mask ^ vs1Mask) ::
      multiplyResLowBits :: multiplyResHighBits :: multiplyResHighBits :: multiplyResHighBits ::
      mulAddRes :: mulAddRes :: mulAddRes :: mulAddRes ::
      // reduction
      vs2Out + vs1Out :: Mux(vs2Out > vs1Out, vs2Out, vs1Out) :: Mux(vs2Out.asSInt > vs1Out.asSInt, vs2Out, vs1Out) ::
      Mux(vs2Out < vs1Out, vs2Out, vs1Out) :: Mux(vs2Out.asSInt < vs1Out.asSInt, vs2Out, vs1Out) ::
      (vs2Out & vs1Out) :: (vs2Out | vs1Out) :: (vs2Out ^ vs1Out) :: Nil
  }

  import VEU_FUN._
  // TODO: vs1Outの判定はMVVではなくベクトルマスク命令か否かで
  valueToExec.vs1Out := MuxCase(execValue1, Seq(
    (instInfoReg.bits.vectorDecode.vSource === VSOURCE.MVV.asUInt) -> execValue1(7,0)(idx(2,0)),
    (instInfoReg.bits.vectorDecode.veuFun.isReductionInst) -> Mux(idx === 0.U, execValue1, reductionAccumulator)
  )) // Mux(instInfoReg.bits.vectorDecode.vSource === VSOURCE.MVV.asUInt, execValue1(7,0)(idx(2,0)), execValue1)
  valueToExec.vs2Out := Mux(instInfoReg.bits.vectorDecode.vSource === VSOURCE.MVV.asUInt, execValue2(7,0)(idx(2,0)), execValue2)
  // VMADC, VMSBCでマスクが無効(vm=1)の場合は0
  valueToExec.vm := !(instInfoReg.bits.vectorDecode.veuFun.isCarryMask && instInfoReg.bits.vectorDecode.vm) && io.readVrf.resp.vm
  // reductionでvm=0ならばaccumulatorをそのまま入れる
  val rawResult = Mux(instInfoReg.bits.vectorDecode.veuFun.isReductionInst && !instInfoReg.bits.vectorDecode.vm && !io.readVrf.resp.vm,
    reductionAccumulator,
    MuxLookup(instInfoReg.bits.vectorDecode.veuFun, 0.U)(
    Seq(ADD, SUB, RSUB, ADC, MADC, SBC, MSBC, SEQ, SNE, SLTU, SLT, SLEU, SLE, SGTU, SGT,
      MINU, MIN, MAXU, MAX, MERGE, MV, AND, OR, XOR, MAND, MNAND, MANDN, MXOR, MOR, MNOR, MORN, MXNOR,
      MUL, MULH, MULHU, MULHSU, MACC, NMSAC, MADD, NMSUB,
      REDSUM, REDMAXU, REDMAX, REDMINU, REDMIN, REDAND, REDOR, REDXOR).zipWithIndex.map(
      x => x._1.asUInt -> execResult(x._2)
    )
  ))
  reductionAccumulator := rawResult

  io.dataOut.toVRF.bits.data := Mux(instInfoReg.bits.vectorDecode.veuFun.writeAsMask, MuxLookup(idx(2, 0), rawResult)(
    (0 until 8).map(
      i => i.U -> Cat((0 until 8).reverse.map(
        j => if (j == i) rawResult(0) else io.readVrf.resp.vdOut(j)
      ))
    )
  ), rawResult)
  io.dataOut.toVRF.bits.vm := instInfoReg.bits.vectorDecode.veuFun.writeAsMask
  // vadc, vmadc, bsbc, vmsbc, vmerge, vmand...vmxnor writes to VRF regardless of vm
  // reductionならば最後のみ書く
  io.dataOut.toVRF.bits.writeReq := Mux(instInfoReg.bits.vectorDecode.veuFun.isReductionInst, io.dataOut.toVRF.bits.last, instInfoReg.bits.vectorDecode.vm || io.readVrf.resp.vm || instInfoReg.bits.vectorDecode.veuFun.ignoreMask || instInfoReg.bits.vectorDecode.veuFun.isMaskInst)
  // reductionならばidxは0
  when(instInfoReg.bits.vectorDecode.veuFun.isReductionInst) {
    io.dataOut.toVRF.bits.index := 0.U
  }

  when(instInfoReg.valid) {
    when(instInfoReg.bits.vectorDecode.veuFun === VEU_FUN.MV_X_S.asUInt) {
      // vmv.x.s
      io.dataOut.toVRF.valid := true.B
      io.dataOut.toVRF.bits.last := true.B
      io.dataOut.toVRF.bits.writeReq := false.B
      io.toExWbReg.valid := true.B
      io.toExWbReg.bits.vectorExecNum.get.bits := 1.U
      io.toExWbReg.bits.vectorExecNum.get.valid := true.B
      io.toExWbReg.bits.ctrlSignals.rd_index := instInfoReg.bits.vd
      io.toExWbReg.bits.dataSignals.exResult := io.readVrf.resp.vs2Out
    }.elsewhen(instInfoReg.bits.vectorDecode.veuFun === VEU_FUN.MV_S_X.asUInt) {
      // vmv.s.x
      io.dataOut.toVRF.valid := true.B
      io.dataOut.toVRF.bits.data := instInfoReg.bits.scalarVal
      io.dataOut.toVRF.bits.last := true.B
      io.dataOut.toVRF.bits.writeReq := true.B
      io.toExWbReg.valid := true.B
      io.toExWbReg.bits.vectorExecNum.get.bits := 1.U
      io.toExWbReg.bits.vectorExecNum.get.valid := true.B
    }
  }
}

object IntegerAluExecUnit extends App {
  implicit val params: HajimeCoreParams = HajimeCoreParams(useVector = true)
  def apply(implicit params: HajimeCoreParams): IntegerAluExecUnit = {
    if(params.useVector) new IntegerAluExecUnit() else throw new Exception("fuck")
  }
  ChiselStage.emitSystemVerilogFile(new IntegerAluExecUnit(), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
