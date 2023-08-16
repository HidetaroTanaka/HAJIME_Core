package hajime.simple4Stage

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common._
import hajime.publicmodules._

import scala.annotation.unused

class debugIO(implicit params: HajimeCoreParams) extends Bundle {
  val debug_retired = Valid(new Bundle{
    val instruction = new InstBundle()
    val pc = new ProgramCounter()
  })
  val debug_abi_map = new debug_map_physical_to_abi()
  // val ID_EX_Reg = Valid(new ID_EX_IO())
}

object debugIO {
  def apply(implicit params: HajimeCoreParams): debugIO = new debugIO()
}

class CoreIO(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val icache_axi4lite = new AXI4liteIO(addr_width = xprlen, data_width = 32)
  val dcache_axi4lite = new AXI4liteIO(addr_width = xprlen, data_width = xprlen)
  // val hartid = Input(UInt(xprlen.W))
  val reset_vector = Input(UInt(xprlen.W))
  val hartid = Input(UInt(xprlen.W))
  val debug_io = if(debug) Some(Output(new debugIO())) else None
}

class Core(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new CoreIO())
  val frontend = Module(new Frontend())
  val cpu = Module(new CPU())
  io.icache_axi4lite <> frontend.io.icache_axi4lite
  io.dcache_axi4lite <> cpu.io.dcache_axi4lite
  frontend.io.reset_vector := io.reset_vector
  cpu.io.frontend <> frontend.io.cpu
  cpu.io.hartid := io.hartid
  if(params.debug) io.debug_io.get := cpu.io.debug_io.get
}

object Core extends App {
  def apply(implicit params: HajimeCoreParams): Core = new Core()
  ChiselStage.emitSystemVerilogFile(Core(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
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

class CPUIO(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val frontend = Flipped(new FrontEndCpuIO())
  val dcache_axi4lite = new AXI4liteIO(addr_width = xprlen, data_width = xprlen)
  val hartid = Input(UInt(xprlen.W))
  val debug_io = if(debug) Some(Output(new debugIO())) else None
  // val accelerators = Vec(2, new AcceleratorInterface)
}

class BasicCtrlSignals() extends Bundle {
  val decode = new ID_output
  val rd_index = UInt(5.W)
}

class ID_EX_dataSignals(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val pc = new ProgramCounter()
  val bp_destPC = UInt(xprlen.W)
  val bp_taken = Bool()
  val imm = UInt(xprlen.W)
  val rs1 = UInt(xprlen.W)
  val rs2 = UInt(xprlen.W)
  val csr = UInt(12.W)
}

class EX_WB_dataSignals(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val pc = new ProgramCounter()
  val arith_logic_result = UInt(xprlen.W)
  val datatoCSR = UInt(xprlen.W)
  val csr_addr = UInt(12.W)
}

class Debug_Info(implicit params: HajimeCoreParams) extends Bundle {
  val instruction = UInt(32.W)
  val pc = new ProgramCounter()
}

// TODO: add inst address misaligned exception (0x0), inst address fault (0x1), and illegal inst exception (0x2)
class ID_EX_IO(implicit params: HajimeCoreParams) extends Bundle {
  val dataSignals = new ID_EX_dataSignals()
  val ctrlSignals = new BasicCtrlSignals()
  val debug = if(params.debug) Some(new Debug_Info()) else None
}

class EX_WB_IO(implicit params: HajimeCoreParams) extends Bundle {
  val dataSignals = new EX_WB_dataSignals()
  val ctrlSignals = new BasicCtrlSignals()
  val interrupt = Bool()
  val interrupt_cause = UInt(params.xprlen.W)
  val debug = if(params.debug) Some(new Debug_Info()) else None
}

class CPU(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new CPUIO())
  io := DontCare

  // fence, ecall, mretがEX、WBに存在する
  // ecallやmretがWBステージにあり，ジャンプ先PCがあるならばreadyを上げるが，IDにある命令は受け取らない
  val sysInst_in_pipeline = WireInit(false.B)

  // Modules
  val decoder = Module(new Decoder())
  val branch_predictor = Module(new BranchPredictor())
  val rf = Module(new RegFile())
  rf.io := DontCare
  val alu = Module(new ALU())
  alu.io := DontCare
  val branch_evaluator = Module(new BranchEvaluator())
  branch_evaluator.io := DontCare
  val bypassingUnit = Module(new BypassingUnit())
  bypassingUnit.io := DontCare
  val ldstUnit = Module(new LDSTUnit())
  ldstUnit.io := DontCare
  val csrUnit = Module(new CSRUnit())
  csrUnit.io := DontCare
  val multiplier = if(params.useMulDiv) Some(Module(new NonPipelinedMultiplierWrap())) else None
  if(params.useMulDiv) multiplier.get.io := DontCare

  ldstUnit.io.dcache_axi4lite <> io.dcache_axi4lite

  val cpu_operating = RegInit(false.B)
  when(!reset.asBool) {
    cpu_operating := true.B
  }

  val EX_stall = WireInit(false.B)
  val WB_stall = WireInit(false.B)
  val ID_stall = WireInit(false.B)
  val ID_flush = WireInit(false.B)
  val EX_flush = WireInit(false.B)
  // update PC in WB stage for ecall, mret or interrupt
  val WB_pc_redirect = WireInit(false.B)
  val rs1_required_but_not_valid = WireInit(false.B)
  val rs2_required_but_not_valid = WireInit(false.B)

  // START OF ID STAGE

  val decoded_inst = Wire(new InstBundle())
  decoded_inst := io.frontend.resp.bits.inst
  val ID_EX_REG = Reg(Valid(new ID_EX_IO()))
  val EX_WB_REG = Reg(Valid(new EX_WB_IO()))
  // io.debug_io.get.ID_EX_Reg := ID_EX_REG

  // EXステージがvalidであり，かつEXステージが破棄できない場合，またはIDステージで必要なレジスタ値を取得できない場合，またはfence命令がある場合にreadyを下げる
  // flushならばストールさせる必要はない
  ID_stall := !ID_flush && ((ID_EX_REG.valid && EX_stall) || rs1_required_but_not_valid || rs2_required_but_not_valid || sysInst_in_pipeline)
  io.frontend.resp.ready := cpu_operating && !ID_stall

  io.frontend.req := Mux(branch_evaluator.io.out.valid && ID_EX_REG.valid, branch_evaluator.io.out, branch_predictor.io.out)
  io.frontend.req.valid := WB_pc_redirect || (branch_evaluator.io.out.valid && ID_EX_REG.valid) || (branch_predictor.io.out.valid && io.frontend.resp.valid && io.frontend.resp.ready)
  branch_predictor.io.pc := io.frontend.resp.bits.pc
  branch_predictor.io.imm := Mux(decoder.io.out.bits.isCondBranch, decoded_inst.b_imm, decoded_inst.j_imm)
  branch_predictor.io.BranchType := decoder.io.out.bits.branch

  decoder.io.inst := decoded_inst
  rf.io.rs1 := decoded_inst.rs1
  rf.io.rs2 := decoded_inst.rs2

  /** IDステージの命令を処理するか否か
   */
  val ID_inst_valid = decoder.io.out.valid && io.frontend.resp.valid && io.frontend.resp.ready
  ID_EX_REG.valid := ID_inst_valid
  ID_EX_REG.bits.dataSignals.pc := io.frontend.resp.bits.pc
  ID_EX_REG.bits.dataSignals.bp_destPC := branch_predictor.io.out.bits.pc
  ID_EX_REG.bits.dataSignals.bp_taken := branch_predictor.io.out.valid
  ID_EX_REG.bits.dataSignals.imm := MuxCase(0.U, Seq(
    (decoder.io.out.bits.value1 === Value1.U_IMM.asUInt) -> decoded_inst.u_imm,
    (decoder.io.out.bits.value1 === Value1.CSR.asUInt) -> decoded_inst.csr_uimm,
    (decoder.io.out.bits.value2 === Value2.I_IMM.asUInt) -> decoded_inst.i_imm,
    (decoder.io.out.bits.value2 === Value2.S_IMM.asUInt) -> decoded_inst.s_imm,
  ))
  ID_EX_REG.bits.dataSignals.rs1 := Mux(bypassingUnit.io.ID.out.rs1_value.valid, bypassingUnit.io.ID.out.rs1_value.bits, rf.io.rs1_out)
  ID_EX_REG.bits.dataSignals.rs2 := Mux(bypassingUnit.io.ID.out.rs2_value.valid, bypassingUnit.io.ID.out.rs2_value.bits, rf.io.rs2_out)
  ID_EX_REG.bits.dataSignals.csr := decoded_inst.csr
  ID_EX_REG.bits.ctrlSignals.decode := decoder.io.out.bits
  ID_EX_REG.bits.ctrlSignals.rd_index := decoded_inst.rd

  bypassingUnit.io.ID.in.rs1_index.bits := decoded_inst.rs1
  bypassingUnit.io.ID.in.rs1_index.valid := decoder.io.out.bits.use_RS1 && decoder.io.out.valid && io.frontend.resp.valid
  bypassingUnit.io.ID.in.rs2_index.bits := decoded_inst.rs2
  bypassingUnit.io.ID.in.rs2_index.valid := decoder.io.out.bits.use_RS2 && decoder.io.out.valid && io.frontend.resp.valid

  rs1_required_but_not_valid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs1_bypassMatchAtEX -> (!bypassingUnit.io.EX.in.bits.rd.valid),
    bypassingUnit.io.ID.out.rs1_bypassMatchAtWB -> (!bypassingUnit.io.WB.in.bits.rd.valid),
  ))
  rs2_required_but_not_valid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs2_bypassMatchAtEX -> (!bypassingUnit.io.EX.in.bits.rd.valid),
    bypassingUnit.io.ID.out.rs2_bypassMatchAtWB -> (!bypassingUnit.io.WB.in.bits.rd.valid),
  ))

  if(params.debug) {
    ID_EX_REG.bits.debug.get.instruction := decoded_inst.bits
    ID_EX_REG.bits.debug.get.pc := io.frontend.resp.bits.pc
  }

  // retain the ID_EX register if stall
  when(EX_stall) {
    ID_EX_REG := ID_EX_REG
  }
  // flush the ID_EX register if branch miss, ecall, mret or interrupt
  ID_flush := EX_flush || branch_evaluator.io.out.valid
  when(ID_flush) {
    ID_EX_REG.valid := false.B
  }

  // START OF EX STAGE
  alu.io.in1 := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.value1, 0.U)(Seq(
    Value1.RS1.asUInt -> ID_EX_REG.bits.dataSignals.rs1,
    Value1.U_IMM.asUInt -> ID_EX_REG.bits.dataSignals.imm,
  ))
  alu.io.in2 := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.value2, 0.U)(Seq(
    Value2.RS2.asUInt -> ID_EX_REG.bits.dataSignals.rs2,
    Value2.I_IMM.asUInt -> ID_EX_REG.bits.dataSignals.imm,
    Value2.S_IMM.asUInt -> ID_EX_REG.bits.dataSignals.imm,
    Value2.PC.asUInt -> ID_EX_REG.bits.dataSignals.pc.addr,
  ))
  alu.io.funct := ID_EX_REG.bits.ctrlSignals.decode

  branch_evaluator.io.req.bits.ALU_Result := alu.io.out
  branch_evaluator.io.req.bits.BranchType := ID_EX_REG.bits.ctrlSignals.decode.branch
  branch_evaluator.io.req.bits.destPC := ID_EX_REG.bits.dataSignals.bp_destPC
  branch_evaluator.io.req.bits.pc := ID_EX_REG.bits.dataSignals.pc
  branch_evaluator.io.req.bits.bp_taken := ID_EX_REG.bits.dataSignals.bp_taken
  branch_evaluator.io.req.valid := ID_EX_REG.valid

  if(params.useMulDiv) {
    val multiplier_hasValue = RegInit(false.B)
    // EXステージに有効な乗算命令があり，かつ乗算器の出力のvalidとreadyが共にtrueで無ければ乗算器に保持するべき情報（現在のEXステージの乗算命令）がある
    multiplier_hasValue := ID_EX_REG.bits.ctrlSignals.decode.use_MUL && ID_EX_REG.valid && !(multiplier.get.io.resp.ready && multiplier.get.io.resp.valid)
    multiplier.get.io.req.bits.rs1 := ID_EX_REG.bits.dataSignals.rs1
    multiplier.get.io.req.bits.rs2 := ID_EX_REG.bits.dataSignals.rs2
    multiplier.get.io.req.bits.funct := ID_EX_REG.bits.ctrlSignals.decode
    // 乗算器に保持するべき情報（現在のEXステージの乗算命令）があればvalidを下げる（乗算器が既に情報を受け取っているため）
    multiplier.get.io.req.valid := ID_EX_REG.bits.ctrlSignals.decode.use_MUL && !multiplier_hasValue && ID_EX_REG.valid && !EX_flush
    multiplier.get.io.resp.ready := !(EX_WB_REG.valid && WB_stall)
  }

  val EX_arithmetic_result = if(params.useMulDiv) {
    Mux(ID_EX_REG.bits.ctrlSignals.decode.use_MUL, multiplier.get.io.resp.bits, alu.io.out)
  } else {
    alu.io.out
  }

  ldstUnit.io.cpu.req.valid := ID_EX_REG.valid && ID_EX_REG.bits.ctrlSignals.decode.memValid && !EX_flush
  ldstUnit.io.cpu.req.bits.addr := alu.io.out
  ldstUnit.io.cpu.req.bits.data := ID_EX_REG.bits.dataSignals.rs2
  ldstUnit.io.cpu.req.bits.funct := ID_EX_REG.bits.ctrlSignals.decode

  bypassingUnit.io.EX.in.bits.rd.bits.index := ID_EX_REG.bits.ctrlSignals.rd_index
  bypassingUnit.io.EX.in.bits.rd.bits.value := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.writeback_selector, 0.U)(Seq(
    WB_SEL.PC4.asUInt -> ID_EX_REG.bits.dataSignals.pc.nextPC,
    WB_SEL.ARITH.asUInt -> EX_arithmetic_result,
    // WB_SEL.CSR.asUInt -> csrUnit.io.resp.data,
  ))
  bypassingUnit.io.EX.in.bits.rd.valid := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.writeback_selector, false.B)(Seq(
    WB_SEL.PC4.asUInt -> true.B,
    WB_SEL.ARITH.asUInt -> (if(params.useMulDiv) !ID_EX_REG.bits.ctrlSignals.decode.use_MUL || multiplier.get.io.resp.valid else true.B),
    WB_SEL.CSR.asUInt -> false.B,
    WB_SEL.MEM.asUInt -> false.B,
    WB_SEL.NONE.asUInt -> false.B
  )) && ID_EX_REG.valid
  bypassingUnit.io.EX.in.valid := ID_EX_REG.bits.ctrlSignals.decode.write_to_rd && ID_EX_REG.valid

  // メモリアクセス命令であればldstUnitがreadyである必要があり，
  // 乗算命令であればmultiplier.respがvalidである必要がある
  EX_WB_REG.valid := ID_EX_REG.valid && (!ID_EX_REG.bits.ctrlSignals.decode.memValid || ldstUnit.io.cpu.req.ready) && (if(params.useMulDiv) !ID_EX_REG.bits.ctrlSignals.decode.use_MUL || multiplier.get.io.resp.valid else true.B)
  EX_WB_REG.bits.dataSignals.pc := ID_EX_REG.bits.dataSignals.pc
  EX_WB_REG.bits.dataSignals.arith_logic_result := EX_arithmetic_result
  EX_WB_REG.bits.dataSignals.datatoCSR := Mux(ID_EX_REG.bits.ctrlSignals.decode.value1 === Value1.RS1.asUInt, ID_EX_REG.bits.dataSignals.rs1, ID_EX_REG.bits.dataSignals.imm)
  EX_WB_REG.bits.dataSignals.csr_addr := ID_EX_REG.bits.dataSignals.csr

  EX_WB_REG.bits.ctrlSignals := ID_EX_REG.bits.ctrlSignals

  EX_WB_REG.bits.interrupt := (ID_EX_REG.bits.ctrlSignals.decode.branch === Branch.ECALL.asUInt) && ID_EX_REG.valid
  // only machine-mode ecall is supported now
  EX_WB_REG.bits.interrupt_cause := Mux(ID_EX_REG.bits.ctrlSignals.decode.branch === Branch.ECALL.asUInt, 0xb.U(params.xprlen.W), 0.U)

  if(params.debug) EX_WB_REG.bits.debug.get := ID_EX_REG.bits.debug.get

  // WBステージがvalidかつ破棄できないかつEXステージに有効な値がある場合，またはメモリアクセス命令かつldstUnit.reqがreadyでない，または乗算命令で乗算器がvalidでない
  EX_stall := ID_EX_REG.valid && ((EX_WB_REG.valid && WB_stall) || (ID_EX_REG.bits.ctrlSignals.decode.memValid && !ldstUnit.io.cpu.req.ready) || (if(params.useMulDiv) {
    (ID_EX_REG.bits.ctrlSignals.decode.use_MUL && !multiplier.get.io.resp.valid)
  } else {
    false.B
  }))

  when(WB_stall) {
    EX_WB_REG := EX_WB_REG
  }

  // flush the EX_WB register if ecall, mret or interrupt
  EX_flush := WB_pc_redirect
  when(EX_flush) {
    EX_WB_REG.valid := false.B
  }

  // START OF WB STAGE
  // メモリアクセス命令かつ，ldstUnitのrespがvalidでなければストール
  WB_stall := EX_WB_REG.valid && (EX_WB_REG.bits.ctrlSignals.decode.memValid && !ldstUnit.io.cpu.resp.valid)
  // TODO: add memory misaligned exception (load misaligned: 0x4, store misaligned: 0x6), and memory access fault exception (load fault: 0x5, store fault: 0x7)
  WB_pc_redirect := EX_WB_REG.valid && (EX_WB_REG.bits.ctrlSignals.decode.branch === Branch.MRET.asUInt || EX_WB_REG.bits.interrupt)
  when(WB_pc_redirect) {
    io.frontend.req.bits.pc := csrUnit.io.resp.data
  }
  // 割り込みまたは例外の場合は、PCのみ更新しリタイアしない（命令を破棄）
  val WB_inst_can_retire = EX_WB_REG.valid && !EX_WB_REG.bits.interrupt && !WB_stall
  rf.io.req.valid := WB_inst_can_retire && EX_WB_REG.bits.ctrlSignals.decode.write_to_rd
  rf.io.req.bits.data := MuxLookup(EX_WB_REG.bits.ctrlSignals.decode.writeback_selector, 0.U)(Seq(
    WB_SEL.PC4 -> EX_WB_REG.bits.dataSignals.pc.nextPC,
    WB_SEL.ARITH -> EX_WB_REG.bits.dataSignals.arith_logic_result,
    WB_SEL.CSR -> csrUnit.io.resp.data,
    WB_SEL.MEM -> ldstUnit.io.cpu.resp.bits.data,
  ).map{
    case (wb_sel, data) => (wb_sel.asUInt, data)
  })
  rf.io.req.bits.rd := EX_WB_REG.bits.ctrlSignals.rd_index

  bypassingUnit.io.WB.in.bits.rd.valid := bypassingUnit.io.WB.in.valid && (!EX_WB_REG.bits.ctrlSignals.decode.memRead || ldstUnit.io.cpu.resp.valid)
  bypassingUnit.io.WB.in.bits.rd.bits.index := EX_WB_REG.bits.ctrlSignals.rd_index
  bypassingUnit.io.WB.in.bits.rd.bits.value := rf.io.req.bits.data
  bypassingUnit.io.WB.in.valid := EX_WB_REG.bits.ctrlSignals.decode.write_to_rd && WB_inst_can_retire

  csrUnit.io.req.valid := EX_WB_REG.valid
  // ecallやmretの処理はcsrUnit内で行われる
  csrUnit.io.req.bits.funct := EX_WB_REG.bits.ctrlSignals.decode
  csrUnit.io.req.bits.data := EX_WB_REG.bits.dataSignals.datatoCSR
  csrUnit.io.req.bits.csr_addr := EX_WB_REG.bits.dataSignals.csr_addr
  csrUnit.io.fromCPU.hartid := io.hartid
  csrUnit.io.fromCPU.cpu_operating := cpu_operating
  csrUnit.io.fromCPU.inst_retire := WB_inst_can_retire
  csrUnit.io.interrupt.valid := EX_WB_REG.bits.interrupt
  csrUnit.io.interrupt.bits.mepc_write := EX_WB_REG.bits.dataSignals.pc.addr
  csrUnit.io.interrupt.bits.mcause_write := EX_WB_REG.bits.interrupt_cause

  // EXまたはWBステージにfence, ecall, mretがある
  sysInst_in_pipeline := (ID_EX_REG.valid && ID_EX_REG.bits.ctrlSignals.decode.isSysInst) || (EX_WB_REG.valid && EX_WB_REG.bits.ctrlSignals.decode.isSysInst)

  if(params.debug) {
    io.debug_io.get.debug_retired.bits.instruction.bits := EX_WB_REG.bits.debug.get.instruction & Fill(32, EX_WB_REG.valid)
    io.debug_io.get.debug_retired.bits.pc.addr := EX_WB_REG.bits.debug.get.pc.addr & Fill(params.xprlen, EX_WB_REG.valid)
    io.debug_io.get.debug_retired.valid := WB_inst_can_retire
    io.debug_io.get.debug_abi_map := rf.io.debug_abi_map.get
  }
}
