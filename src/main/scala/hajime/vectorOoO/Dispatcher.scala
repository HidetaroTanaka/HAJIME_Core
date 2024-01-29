package hajime.vectorOoO

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.BundleInitializer._
import hajime.common._
import hajime.publicmodules._
import hajime.simple4Stage._
import hajime.vectormodules._

class RenamedReg(implicit params: HajimeCoreParams) extends Bundle {
  val num = UInt(params.physicalRegWidth.W)
  val useAs = UseRegisterAs()
}

class DispatcherDataSignals(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val pc = new ProgramCounter()
  val renamedRs1 = new RenamedReg()
  val renamedRs2 = new RenamedReg()
  val renamedRs3 = new RenamedReg()
  assert(renamedRs3.useAs =/= UseRegisterAs.SCALAR, "renamedRs3 must be used as Vector Register")
  val renamedRd = new RenamedReg()
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
  val scalarCtrlSignals = new BasicCtrlSignals()
  val exceptionSignals = new Valid(UInt(params.xprlen.W))
  val vectorCtrlSignals = if(params.useVector) Some(new VectorDecoderResp()) else None
  val debug = if(params.debug) Some(new Debug_Info()) else None
}

class DispatcherIO(implicit params: HajimeCoreParams) extends Bundle {
  val frontend = Flipped(new FrontEndCpuIO())
  val hartid = Input(UInt(params.xprlen.W))
  val toExecutor = new DecoupledIO(new DispatcherOutput())
  // リタイア状態テーブルの同じ物理レジスタをフリーリストに入れる
  val retiredRd = Input(Valid(new RetiredReg()))
  val bpMiss = Input(Bool())
}

class Dispatcher(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new DispatcherIO())

  val decoder = Module(new Decoder())
  val branchPredictor = Module(new BranchPredictor())
  val vectorDecoder = if(params.useVector) Some(Module(new VectorDecoder())) else None
  val renameUnit = Module(new RenameUnit())

  val instBundle = Wire(new InstBundle())
  instBundle := io.frontend.resp.bits.inst

  decoder.io.inst := instBundle
  vectorDecoder.get.io.inst := instBundle
  branchPredictor.io.pc := io.frontend.resp.bits.pc
  branchPredictor.io.imm := Mux(decoder.io.out.bits.isCondBranch, instBundle.getImm(ImmediateEnum.B), instBundle.getImm(ImmediateEnum.J))
  branchPredictor.io.BranchType := decoder.io.out.bits.branch
}
