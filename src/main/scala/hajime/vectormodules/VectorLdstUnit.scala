package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO._
import hajime.common.HajimeCoreParams
import hajime.publicmodules._

class ScalarLdstSignalIn(implicit params: HajimeCoreParams) extends Bundle {
  val rs1Value = UInt(params.xprlen.W)
  val rs2Value = UInt(params.xprlen.W)
  val immediate = UInt(params.xprlen.W)
  val scalarDecode = new ID_output()
}

class ScalarLdstUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(Irrevocable(new ScalarLdstSignalIn()))
  val resp = new ValidIO(new LDSTResp())
}

class VectorLdstUnit(implicit params: HajimeCoreParams) extends Module {
  val scalarIO = IO(new ScalarLdstUnitIO())
  val vectorIO = if(params.useVector) Some(IO(new VectorExecUnitIO())) else None
  val dcache = IO(new AXI4liteIO(addr_width = params.xprlen, data_width = params.xprlen))

  // scalar
  val scalarAddr = scalarIO.req.bits.rs1Value + scalarIO.req.bits.immediate
}
