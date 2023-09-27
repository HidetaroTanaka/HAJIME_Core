package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common._
import hajime.publicmodules._

class VectorExecUnitSignalIn(implicit params: HajimeCoreParams) extends Bundle {
  val vs1 = UInt(5.W)
  val vs2 = UInt(5.W)
  val vd = UInt(5.W)
  val vm = Bool()
  val scalarDecode = new ID_output()
  val vectorDecode = new VectorDecoderResp()
  val vecConf = new VecCtrlUnitResp()
}

class VectorExecUnitSignalToVRF(implicit params: HajimeCoreParams) extends Bundle {
  val readIndex = UInt(log2Up(params.vlen/8).W)
  val sew = UInt(3.W)
}

class VectorExecUnitDataIn(implicit params: HajimeCoreParams) extends Bundle {
  val value1 = UInt(params.xprlen.W)
  val value2 = UInt(params.xprlen.W)
  val value3 = UInt(params.xprlen.W)
  val vm = Bool()
}

class VectorExecUnitDataOut(implicit params: HajimeCoreParams) extends Bundle {
  val toVRF = ValidIO(new VecRegFileWriteReq())
}

class VectorExecUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val signalIn = Flipped(DecoupledIO(new VectorExecUnitSignalIn()))
  val signalToVRF = Output(new VectorExecUnitSignalToVRF())
  val dataIn = Input(new VectorExecUnitDataIn())
  val dataOut = Output(new VectorExecUnitDataOut())
}

abstract class VectorExecUnit(val op: (UInt, UInt, UInt, Bool) => UInt)(implicit params: HajimeCoreParams) extends Module {
  // 演算内容をここに書けばop関数はいらない？
  def exec(scalarDec: ID_output, vectorDec: VectorDecoderResp): UInt

  val io = new VectorExecUnitIO()

  val idx = RegInit(0.U(log2Up(params.vlen/8).W))
  when(io.signalIn.valid && io.signalIn.ready || io.dataOut.toVRF.bits.last) {
    idx := 0.U
  } .otherwise {
    idx := idx + 1.U
  }
  io.dataOut.toVRF.bits.last := (idx-1.U === io.signalIn.bits.vecConf.vl)


  io.signalToVRF.readIndex := io.signalIn.bits
  io.dataOut.toVRF.bits.data := op(io.dataIn.value1, io.dataIn.value2, io.dataIn.value3, io.dataIn.vm)
}

class ArithmeticVectorExecUnit(implicit params: HajimeCoreParams) extends VectorExecUnit(
  (vs1, vs2, vs3, vm) => Mux(vm, vs1+vs2, vs3)
) {
  override def exec(scalarDec: ID_output, vectorDec: VectorDecoderResp): UInt = ???
}