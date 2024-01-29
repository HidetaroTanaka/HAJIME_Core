package hajime.simple4Stage

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common.{ScalarOpConstants, _}
import hajime.publicmodules._
import hajime.vectormodules._

import scala.annotation.unused
import scala.reflect.ClassTag

class debugIO(implicit params: HajimeCoreParams) extends Bundle {
  val debugRetired = Valid(new Bundle{
    val instruction = new InstBundle()
    val pc = new ProgramCounter()
  })
  val debugAbiMap = new debug_map_physical_to_abi()
  val vrfMap = if(params.useVector) Some(Vec(32, UInt(params.vlen.W))) else None
  // val ID_EX_Reg = Valid(new idExIo())
}

object debugIO {
  def apply(implicit params: HajimeCoreParams): debugIO = new debugIO()
}

class CoreIO(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val iCacheAxi4Lite = new AXI4liteIO(addrWidth = xprlen, dataWidth = 32)
  val dCacheAxi4Lite = new AXI4liteIO(addrWidth = xprlen, dataWidth = xprlen)
  val resetVector = Input(UInt(xprlen.W))
  val hartid = Input(UInt(xprlen.W))
  val debugIo = if(debug) Some(Output(new debugIO())) else None
}
class Core[T <: CpuModule](cpu: Class[T])(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new CoreIO())
  val frontend = Module(new Frontend())
  val internalCpu = Module(cpu.getDeclaredConstructor(classOf[HajimeCoreParams]).newInstance(params))
  io.iCacheAxi4Lite <> frontend.io.icache_axi4lite
  io.dCacheAxi4Lite <> internalCpu.io.dCacheAxi4Lite
  frontend.io.reset_vector := io.resetVector
  internalCpu.io.frontend <> frontend.io.cpu
  internalCpu.io.hartid := io.hartid
  if (params.debug) io.debugIo.get := internalCpu.io.debugIo.get
}

object Core extends App {
  implicit val params = HajimeCoreParams(useException = false, useVector = true, debug = false, fpga = true)
  def apply[T <: CpuModule](cpu: Class[T])(implicit params: HajimeCoreParams): Core[T] = {
    if(cpu == classOf[VectorCpu] && !params.useVector) {
      throw new Exception("useVector is false")
    } else {
      new Core(cpu)
    }
  }
  ChiselStage.emitSystemVerilogFile(new Core(classOf[VectorCpu]), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}

@unused
class AcceleratorInterface extends Bundle {
  ???
}

@unused
class Interrupts extends Bundle {
  val software_int = Bool()
  val timer_int = Bool()
}

class CpuIo(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val frontend = Flipped(new FrontEndCpuIO())
  val dCacheAxi4Lite = new AXI4liteIO(addrWidth = xprlen, dataWidth = xprlen)
  val hartid = Input(UInt(xprlen.W))
  val debugIo = if(debug) Some(Output(new debugIO())) else None
  // val accelerators = Vec(2, new AcceleratorInterface)
}

abstract class CpuModule(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new CpuIo())
}

class BasicCtrlSignals(implicit params: HajimeCoreParams) extends Bundle {
  val decode = new ID_output()
  val rdIndex = UInt(5.W)
}

class ID_EX_dataSignals(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val pc = new ProgramCounter()
  val bpDestPc = UInt(xprlen.W)
  val bpTaken = Bool()
  // Should I change these signals to value1, value2? (Only two of these are actually used)
  val imm = UInt(xprlen.W)
  val rs1 = UInt(xprlen.W)
  val rs2 = UInt(xprlen.W)
  val zimm = UInt(12.W)
}

class EX_WB_dataSignals(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val pc = new ProgramCounter()
  val exResult = UInt(xprlen.W)
  val datatoCSR = UInt(xprlen.W)
  val csrAddr = UInt(12.W)
}

class Debug_Info(implicit params: HajimeCoreParams) extends Bundle {
  val instruction = UInt(32.W)
  val pc = new ProgramCounter()
}

class VectorDataSignals(implicit params: HajimeCoreParams) extends Bundle {
  /**
   * vector masking is encoded in inst[25]
   */
  val mask = Bool()
  val vs1 = UInt(5.W)
  val vs2 = UInt(5.W)
  val vd = UInt(5.W)
}

class idExIo(implicit params: HajimeCoreParams) extends Bundle {
  val dataSignals = new ID_EX_dataSignals()
  val ctrlSignals = new BasicCtrlSignals()
  val exceptionSignals = new Valid(UInt(params.xprlen.W))
  val vectorDataSignals = if(params.useVector) Some(new VectorDataSignals()) else None
  val vectorCtrlSignals = if(params.useVector) Some(new VectorDecoderResp()) else None
  val debug = if(params.debug) Some(new Debug_Info()) else None
}

class exWbIo(implicit params: HajimeCoreParams) extends Bundle {
  val dataSignals = new EX_WB_dataSignals()
  val ctrlSignals = new BasicCtrlSignals()
  val exceptionSignals = new Valid(UInt(params.xprlen.W))
  val vectorCsrPorts = if(params.useVector) Some(new VecCtrlUnitResp()) else None
  val vectorExecNum = if(params.useVector) Some(Valid(UInt(log2Up(params.vlen/8).W))) else None
  val debug = if(params.debug) Some(new Debug_Info()) else None
}

class Cpu(implicit params: HajimeCoreParams) extends CpuModule with ScalarOpConstants with VectorOpConstants {
  // val io = IO(new CpuIo())
  io := DontCare

  // fence, ecall, mretがEX、WBに存在する
  // ecallやmretがWBステージにあり，ジャンプ先PCがあるならばreadyを上げるが，IDにある命令は受け取らない
  val sysInstInPipeline = WireInit(false.B)

  // Modules
  val decoder = Module(new Decoder())
  val branchPredictor = Module(new BranchPredictor())
  val rf = Module(new RegFile())
  rf.io := DontCare
  val alu = Module(new ALU())
  alu.io := DontCare
  val branchEvaluator = Module(new BranchEvaluator())
  branchEvaluator.io := DontCare
  val bypassingUnit = Module(new BypassingUnit())
  bypassingUnit.io := DontCare
  val ldstUnit = Module(new LDSTUnit())
  ldstUnit.io := DontCare
  val csrUnit = Module(new CSRUnit())
  csrUnit.io := DontCare
  val multiplier = if(params.useMulDiv) Some(Module(new NonPipelinedMultiplierWrap())) else None
  if(params.useMulDiv) multiplier.get.io := DontCare

  ldstUnit.io.dCacheAxi4Lite <> io.dCacheAxi4Lite

  val cpuOperating = RegInit(false.B)
  when(!reset.asBool) {
    cpuOperating := true.B
  }

  val exStall = WireInit(false.B)
  val wbStall = WireInit(false.B)
  val idStall = WireInit(false.B)
  val idFlush = WireInit(false.B)
  val exFlush = WireInit(false.B)
  // update PC in WB stage for ecall, mret or exception
  val wbPcRedirect = WireInit(false.B)
  val rs1RequiredButNotValid = WireInit(false.B)
  val rs2RequiredButNotValid = WireInit(false.B)

  // START OF ID STAGE
  // TODO: 可読性向上のため，validのみのブロックとvalid && readyのブロックに分ける．EX，WBも同様

  val decodedInst = Wire(new InstBundle())
  decodedInst := io.frontend.resp.bits.inst
  val idExReg = Reg(Valid(new idExIo()))
  val exWbReg = Reg(Valid(new exWbIo()))
  // io.debugIo.get.ID_EX_Reg := idExReg

  // EXステージがvalidであり，かつEXステージが破棄できない場合，またはIDステージで必要なレジスタ値を取得できない場合，またはfence命令がある場合にreadyを下げる
  // flushならばストールさせる必要はない
  idStall := !idFlush && ((idExReg.valid && exStall) || rs1RequiredButNotValid || rs2RequiredButNotValid || sysInstInPipeline)
  io.frontend.resp.ready := cpuOperating && !idStall

  io.frontend.req := Mux(branchEvaluator.io.out.valid && idExReg.valid, branchEvaluator.io.out, branchPredictor.io.out)
  io.frontend.req.valid := wbPcRedirect || (branchEvaluator.io.out.valid && idExReg.valid) || (branchPredictor.io.out.valid && io.frontend.resp.valid && io.frontend.resp.ready)
  branchPredictor.io.pc := io.frontend.resp.bits.pc
  branchPredictor.io.imm := Mux(decoder.io.out.bits.isCondBranch, decodedInst.getImm(ImmediateEnum.B), decodedinst.getImm(ImmediateEnum.J))
  branchPredictor.io.BranchType := decoder.io.out.bits.branch

  decoder.io.inst := decodedInst
  rf.io.rs1 := decodedInst.rs1
  rf.io.rs2 := decodedInst.rs2

  val idInstValid = io.frontend.resp.valid && io.frontend.resp.ready
  val idFetchException = io.frontend.resp.bits.exceptionSignals.valid && idInstValid
  val idIllegalInstruction = !decoder.io.out.valid && idInstValid
  val idEcall = decoder.io.out.valid && (decoder.io.out.bits.branch === Branch.ECALL.asUInt) && idInstValid
  val idException = idFetchException || idIllegalInstruction || idEcall

  idExReg.valid := idInstValid
  idExReg.bits.exceptionSignals.valid := idException
  idExReg.bits.exceptionSignals.bits := MuxCase(0.U, Seq(
    idFetchException -> io.frontend.resp.bits.exceptionSignals.bits,
    idIllegalInstruction -> Causes.illegal_instruction.U,
    idEcall -> Causes.machine_ecall.U,
  ))
  idExReg.bits.dataSignals.pc := io.frontend.resp.bits.pc
  idExReg.bits.dataSignals.bpDestPc := branchPredictor.io.out.bits.pc
  idExReg.bits.dataSignals.bpTaken := branchPredictor.io.out.valid
  idExReg.bits.dataSignals.imm := MuxCase(0.U, Seq(
    (decoder.io.out.bits.value1 === Value1.U_IMM.asUInt) -> decodedInst.getImm(ImmediateEnum.U),
    (decoder.io.out.bits.value1 === Value1.UIMM19_15.asUInt) -> decodedInst.uimm19To15,
    (decoder.io.out.bits.value2 === Value2.I_IMM.asUInt) -> decodedInst.getImm(ImmediateEnum.I),
    (decoder.io.out.bits.value2 === Value2.S_IMM.asUInt) -> decodedInst.getImm(ImmediateEnum.S),
  ))

  val rs1ValueToEX = Mux(bypassingUnit.io.ID.out.rs1_value.valid, bypassingUnit.io.ID.out.rs1_value.bits, rf.io.rs1_out)
  val rs2ValueToEX = Mux(bypassingUnit.io.ID.out.rs2_value.valid, bypassingUnit.io.ID.out.rs2_value.bits, rf.io.rs2_out)

  idExReg.bits.dataSignals.rs1 := rs1ValueToEX
  idExReg.bits.dataSignals.rs2 := rs2ValueToEX
  idExReg.bits.dataSignals.zimm := decodedInst.zimm
  idExReg.bits.ctrlSignals.decode := decoder.io.out.bits
  idExReg.bits.ctrlSignals.rdIndex := decodedInst.rd

  bypassingUnit.io.ID.in.rs1_index.bits := decodedInst.rs1
  // exceptionの際にこれを下げる必要があるかもしれない
  bypassingUnit.io.ID.in.rs1_index.valid := decoder.io.out.bits.useRs1 && decoder.io.out.valid && io.frontend.resp.valid
  bypassingUnit.io.ID.in.rs2_index.bits := decodedInst.rs2
  bypassingUnit.io.ID.in.rs2_index.valid := decoder.io.out.bits.useRs2 && decoder.io.out.valid && io.frontend.resp.valid

  rs1RequiredButNotValid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs1_bypassMatchAtEX -> (!bypassingUnit.io.EX.in.bits.rd.valid),
    bypassingUnit.io.ID.out.rs1_bypassMatchAtWB -> (!bypassingUnit.io.WB.in.bits.rd.valid),
  ))
  rs2RequiredButNotValid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs2_bypassMatchAtEX -> (!bypassingUnit.io.EX.in.bits.rd.valid),
    bypassingUnit.io.ID.out.rs2_bypassMatchAtWB -> (!bypassingUnit.io.WB.in.bits.rd.valid),
  ))

  if(params.debug) {
    idExReg.bits.debug.get.instruction := decodedInst.bits
    idExReg.bits.debug.get.pc := io.frontend.resp.bits.pc
  }

  // retain the ID_EX register if stall
  when(exStall) {
    idExReg := idExReg
  }
  // flush the ID_EX register if branch miss, ecall, mret or exception
  idFlush := exFlush || branchEvaluator.io.out.valid
  when(idFlush) {
    idExReg.valid := false.B
  }

  // START OF EX STAGE

  alu.io.in1 := MuxLookup(idExReg.bits.ctrlSignals.decode.value1, 0.U)(Seq(
    Value1.RS1.asUInt -> idExReg.bits.dataSignals.rs1,
    Value1.U_IMM.asUInt -> idExReg.bits.dataSignals.imm,
  ))
  alu.io.in2 := MuxLookup(idExReg.bits.ctrlSignals.decode.value2, 0.U)(Seq(
    Value2.RS2.asUInt -> idExReg.bits.dataSignals.rs2,
    Value2.I_IMM.asUInt -> idExReg.bits.dataSignals.imm,
    Value2.S_IMM.asUInt -> idExReg.bits.dataSignals.imm,
    Value2.PC.asUInt -> idExReg.bits.dataSignals.pc.addr,
  ))

  alu.io.funct := idExReg.bits.ctrlSignals.decode

  branchEvaluator.io.req.bits.ALU_Result := alu.io.out
  branchEvaluator.io.req.bits.BranchType := idExReg.bits.ctrlSignals.decode.branch
  branchEvaluator.io.req.bits.destPC := idExReg.bits.dataSignals.bpDestPc
  branchEvaluator.io.req.bits.pc := idExReg.bits.dataSignals.pc
  branchEvaluator.io.req.bits.bp_taken := idExReg.bits.dataSignals.bpTaken
  branchEvaluator.io.req.valid := idExReg.valid

  if(params.useMulDiv) {
    val multiplierHasValue = RegInit(false.B)
    // EXステージに有効な乗算命令があり，かつ乗算器の出力のvalidとreadyが共にtrueで無ければ乗算器に保持するべき情報（現在のEXステージの乗算命令）がある
    multiplierHasValue := idExReg.bits.ctrlSignals.decode.useMul && idExReg.valid && !(multiplier.get.io.resp.ready && multiplier.get.io.resp.valid)
    multiplier.get.io.req.bits.rs1 := idExReg.bits.dataSignals.rs1
    multiplier.get.io.req.bits.rs2 := idExReg.bits.dataSignals.rs2
    multiplier.get.io.req.bits.funct := idExReg.bits.ctrlSignals.decode
    // 乗算器に保持するべき情報（現在のEXステージの乗算命令）があればvalidを下げる（乗算器が既に情報を受け取っているため）
    multiplier.get.io.req.valid := idExReg.bits.ctrlSignals.decode.useMul && !multiplierHasValue && idExReg.valid && !exFlush
    multiplier.get.io.resp.ready := !(exWbReg.valid && wbStall)
  }

  val exArithmeticResult = if(params.useMulDiv) {
    Mux(idExReg.bits.ctrlSignals.decode.useMul, multiplier.get.io.resp.bits, alu.io.out)
  } else {
    alu.io.out
  }

  ldstUnit.io.cpu.req.valid := idExReg.valid && !exFlush && (idExReg.bits.ctrlSignals.decode.memValid)
  ldstUnit.io.cpu.req.bits.addr := alu.io.out
  ldstUnit.io.cpu.req.bits.data := idExReg.bits.dataSignals.rs2
  ldstUnit.io.cpu.req.bits.funct := idExReg.bits.ctrlSignals.decode

  bypassingUnit.io.EX.in.bits.rd.bits.index := idExReg.bits.ctrlSignals.rd_index
  bypassingUnit.io.EX.in.bits.rd.bits.value := MuxLookup(idExReg.bits.ctrlSignals.decode.writeback_selector, 0.U)(Seq(
    WB_SEL.PC4.asUInt -> idExReg.bits.dataSignals.pc.nextPC,
    WB_SEL.ARITH.asUInt -> exArithmeticResult,
  ))
  bypassingUnit.io.EX.in.bits.rd.valid := MuxLookup(idExReg.bits.ctrlSignals.decode.writeBackSelector, false.B)(Seq(
    WB_SEL.PC4.asUInt -> true.B,
    WB_SEL.ARITH.asUInt -> (if(params.useMulDiv) !idExReg.bits.ctrlSignals.decode.useMul || multiplier.get.io.resp.valid else true.B),
    WB_SEL.CSR.asUInt -> false.B,
    WB_SEL.MEM.asUInt -> false.B,
    WB_SEL.NONE.asUInt -> false.B,
  )) && idExReg.valid
  bypassingUnit.io.EX.in.valid := idExReg.bits.ctrlSignals.decode.writeToRd && idExReg.valid

  // メモリアクセス命令であればldstUnitがreadyである必要があり，
  // 乗算命令であればmultiplier.respがvalidである必要がある
  // vsetvl系でないベクタ命令ならば最終要素の実行である必要がある(idxReg == vl)
  exWbReg.valid := idExReg.valid && (!idExReg.bits.ctrlSignals.decode.memValid || ldstUnit.io.cpu.req.ready) &&
    (if(params.useMulDiv) !idExReg.bits.ctrlSignals.decode.useMul || multiplier.get.io.resp.valid else true.B)
  exWbReg.bits.dataSignals.pc := idExReg.bits.dataSignals.pc
  exWbReg.bits.dataSignals.exResult := MuxLookup(idExReg.bits.ctrlSignals.decode.writeBackSelector, 0.U)(Seq(
    WB_SEL.ARITH.asUInt -> exArithmeticResult,
  ))
  exWbReg.bits.dataSignals.datatoCSR := Mux(idExReg.bits.ctrlSignals.decode.value1 === Value1.RS1.asUInt, idExReg.bits.dataSignals.rs1, idExReg.bits.dataSignals.imm)
  exWbReg.bits.dataSignals.csrAddr := idExReg.bits.dataSignals.zimm

  exWbReg.bits.ctrlSignals := idExReg.bits.ctrlSignals

  exWbReg.bits.exceptionSignals.valid := (idExReg.valid && idExReg.bits.exceptionSignals.valid) || (idExReg.bits.ctrlSignals.decode.branch === Branch.ECALL.asUInt) && idExReg.valid
  // only machine-mode ecall and illegal inst is supported now
  exWbReg.bits.exceptionSignals.bits := MuxCase(0.U, Seq(
    // if there is already exception before ID, then retain
    idExReg.bits.exceptionSignals.valid -> idExReg.bits.exceptionSignals.bits,
    // else if exception in EX (load/store misaligned or access fault),
  ))
    Mux(idExReg.bits.ctrlSignals.decode.branch === Branch.ECALL.asUInt, 0xb.U(params.xprlen.W), 0.U)

  if(params.debug) exWbReg.bits.debug.get := idExReg.bits.debug.get

  // WBステージがvalidかつ破棄できないかつEXステージに有効な値がある場合，またはメモリアクセス命令かつldstUnit.reqがreadyでない，または乗算命令で乗算器がvalidでない
  // またはベクタ命令実行完了前にスカラ命令がID_EXレジスタにある，またはチェイニング不可能なベクタ命令（構造ハザード・0要素目の値が用意できていないなど）
  exStall := idExReg.valid && ((exWbReg.valid && wbStall) || (idExReg.bits.ctrlSignals.decode.memValid && !ldstUnit.io.cpu.req.ready) || (if(params.useMulDiv) {
    idExReg.bits.ctrlSignals.decode.useMul && !multiplier.get.io.resp.valid
  } else false.B))

  when(wbStall) {
    exWbReg := exWbReg
  }

  // flush the EX_WB register if ecall, mret or exception
  exFlush := wbPcRedirect
  when(exFlush) {
    exWbReg.valid := false.B
  }

  // START OF WB STAGE
  // メモリアクセス命令かつ，ldstUnitのrespがvalidでなければストール
  wbStall := exWbReg.valid && (exWbReg.bits.ctrlSignals.decode.memValid && !ldstUnit.io.cpu.resp.valid)
  val dmemoryAccessException = (exWbReg.bits.ctrlSignals.decode.memValid && ldstUnit.io.cpu.resp.valid && ldstUnit.io.cpu.resp.bits.exceptionSignals.valid)
  wbPcRedirect := exWbReg.valid && (exWbReg.bits.ctrlSignals.decode.branch === Branch.MRET.asUInt || exWbReg.bits.exceptionSignals.valid || dmemoryAccessException)
  when(wbPcRedirect) {
    io.frontend.req.bits.pc := csrUnit.io.resp.data
  }
  // 割り込みまたは例外の場合は、PCのみ更新しリタイアしない（命令を破棄）
  val wbInstCanRetire = exWbReg.valid && !(exWbReg.bits.exceptionSignals.valid || dmemoryAccessException) && !wbStall
  rf.io.req.valid := wbInstCanRetire && exWbReg.bits.ctrlSignals.decode.writeToRd
  rf.io.req.bits.data := MuxLookup(exWbReg.bits.ctrlSignals.decode.writeBackSelector, 0.U)(Seq(
    WB_SEL.PC4 -> exWbReg.bits.dataSignals.pc.nextPC,
    WB_SEL.ARITH -> exWbReg.bits.dataSignals.exResult,
    WB_SEL.CSR -> csrUnit.io.resp.data,
    WB_SEL.MEM -> ldstUnit.io.cpu.resp.bits.data,
  ).map{
    case (wb_sel, data) => (wb_sel.asUInt, data)
  })
  rf.io.req.bits.rd := exWbReg.bits.ctrlSignals.rdIndex

  bypassingUnit.io.WB.in.bits.rd.valid := bypassingUnit.io.WB.in.valid && (!exWbReg.bits.ctrlSignals.decode.memRead || ldstUnit.io.cpu.resp.valid)
  bypassingUnit.io.WB.in.bits.rd.bits.index := exWbReg.bits.ctrlSignals.rdIndex
  bypassingUnit.io.WB.in.bits.rd.bits.value := rf.io.req.bits.data
  bypassingUnit.io.WB.in.valid := exWbReg.bits.ctrlSignals.decode.writeToRd && wbInstCanRetire

  csrUnit.io.req.valid := exWbReg.valid
  // ecallやmretの処理はcsrUnit内で行われる
  csrUnit.io.req.bits.funct := exWbReg.bits.ctrlSignals.decode
  csrUnit.io.req.bits.data := exWbReg.bits.dataSignals.datatoCSR
  csrUnit.io.req.bits.csr_addr := exWbReg.bits.dataSignals.csrAddr
  csrUnit.io.fromCPU.hartid := io.hartid
  csrUnit.io.fromCPU.cpu_operating := cpuOperating
  csrUnit.io.fromCPU.inst_retire := wbInstCanRetire
  csrUnit.io.exception.valid := (exWbReg.bits.exceptionSignals.valid || dmemoryAccessException) && exWbReg.valid
  csrUnit.io.exception.bits.mepc_write := exWbReg.bits.dataSignals.pc.addr
  csrUnit.io.exception.bits.mcause_write := Mux(dmemoryAccessException, ldstUnit.io.cpu.resp.bits.exceptionSignals.bits, exWbReg.bits.exceptionSignals.bits)

  // EXまたはWBステージにfence, ecall, mretがある
  sysInstInPipeline := (idExReg.valid && idExReg.bits.ctrlSignals.decode.isSysInst) || (exWbReg.valid && exWbReg.bits.ctrlSignals.decode.isSysInst)

  if(params.debug) {
    io.debugIo.get.debugRetired.bits.instruction.bits := exWbReg.bits.debug.get.instruction & Fill(32, exWbReg.valid)
    io.debugIo.get.debugRetired.bits.pc.addr := exWbReg.bits.debug.get.pc.addr & Fill(params.xprlen, exWbReg.valid)
    io.debugIo.get.debugRetired.valid := wbInstCanRetire
    io.debugIo.get.debugAbiMap := rf.io.debug_abi_map.get
  }
}

object Cpu extends App {
  def apply(implicit params: HajimeCoreParams): Cpu = new Cpu()
}