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
  val signalIn = Flipped(ValidIO(new VectorExecUnitSignalIn()))
  val signalToVRF = Output(new VectorExecUnitSignalToVRF())
  val dataIn = Input(new VectorExecUnitDataIn())
  val dataOut = Output(new VectorExecUnitDataOut())
}

abstract class VectorExecUnit(val op: (UInt, UInt, UInt, Bool) => UInt)(implicit params: HajimeCoreParams) extends Module {
  val io = new VectorExecUnitIO()
}
