package hajime.vectorOoO

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.BundleInitializer._
import hajime.common._
import hajime.simple4Stage._
import hajime.vectormodules.VectorDecoderResp

class DispatcherDataSignals(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val pc = new ProgramCounter()
  val renamedRs1 = Valid(UInt(physicalRegWidth.W))
  val renamedRs2 = Valid(UInt(physicalRegWidth.W))
  val renamedRd = Valid(UInt(physicalRegWidth.W))
  // jalr: immVal1 -> inst[31,20], immVal2 -> pc from RAS
  // csr: immVal1 -> inst[31,20] (csr addr), immVal2 -> inst[4:0]
  // vsetvli: immVal1 -> inst[30,20]
  // vsetivli: immVal1 -> inst[29,20], immVal2 -> inst[4:0]
  // vop.vi: immVal2 -> inst[4:0]
  val immVal1 = UInt(xprlen.W)
  val immVal2 = UInt(xprlen.W)
}
class DispatcherOutput(implicit params: HajimeCoreParams) extends Bundle {
  val dataSignals = new DispatcherDataSignals()
  val ctrlSignals = new BasicCtrlSignals()
  val exceptionSignals = new Valid(UInt(params.xprlen.W))
  val vectorCtrlSignals = if(params.useVector) Some(new VectorDecoderResp()) else None
  val debug = if(params.debug) Some(new Debug_Info()) else None
}

class DispatcherIO(implicit params: HajimeCoreParams) extends Module {
  val frontend = Flipped(new FrontEndCpuIO())
  val hartid = Input(UInt(params.xprlen.W))
  val toExecutor = new DecoupledIO(new DispatcherOutput())
}

class Dispatcher(implicit params: HajimeCoreParams) extends Module {

}
