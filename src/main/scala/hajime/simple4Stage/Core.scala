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
  val ID_EX_Reg = Valid(new ID_EX_IO())
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

class CPUIO(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val frontend = Flipped(new FrontEndCpuIO())
  val dcache_axi4lite = new AXI4liteIO(addr_width = xprlen, data_width = xprlen)
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
  val pc4 = UInt(xprlen.W)
  val arith_logic_result = UInt(xprlen.W)
  val csr = UInt(xprlen.W)
}

class Debug_Info(implicit params: HajimeCoreParams) extends Bundle {
  val instruction = UInt(32.W)
  val pc = new ProgramCounter()
}

class ID_EX_IO(implicit params: HajimeCoreParams) extends Bundle {
  val dataSignals = new ID_EX_dataSignals()
  val ctrlSignals = new BasicCtrlSignals()
  val debug = if(params.debug) Some(new Debug_Info()) else None
}

class EX_WB_IO(implicit params: HajimeCoreParams) extends Bundle {
  val dataSignals = new EX_WB_dataSignals()
  val ctrlSignals = new BasicCtrlSignals()
  val debug = if(params.debug) Some(new Debug_Info()) else None
}

class CPU(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new CPUIO())
  io := DontCare

  val fence_in_pipeline = RegInit(false.B)

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
  val rs1_required_but_not_valid = WireInit(false.B)
  val rs2_required_but_not_valid = WireInit(false.B)

  // START OF ID STAGE

  val decoded_inst = Wire(new InstBundle())
  decoded_inst := io.frontend.resp.bits.inst
  val ID_EX_REG = Reg(Valid(new ID_EX_IO()))
  val EX_WB_REG = Reg(Valid(new EX_WB_IO()))
  io.debug_io.get.ID_EX_Reg := ID_EX_REG

  // EXステージがvalidであり，かつEXステージが破棄できない場合，またはIDステージで必要なレジスタ値を取得できない場合，またはfence命令がある場合にreadyを下げる
  // flushならばストールさせる必要はない
  ID_stall := !ID_flush && ((ID_EX_REG.valid && EX_stall) || rs1_required_but_not_valid || rs2_required_but_not_valid || fence_in_pipeline)
  io.frontend.resp.ready := cpu_operating && !ID_stall

  io.frontend.req := Mux(branch_evaluator.io.out.valid && ID_EX_REG.valid, branch_evaluator.io.out, branch_predictor.io.out)
  branch_predictor.io.pc := io.frontend.resp.bits.pc
  branch_predictor.io.imm := Mux(decoder.io.out.bits.isCondBranch, decoded_inst.b_imm, decoded_inst.j_imm)
  branch_predictor.io.BranchType := decoder.io.out.bits.branch

  decoder.io.inst := decoded_inst
  rf.io.rs1 := Mux(bypassingUnit.io.ID.out.rs1_bypassMatchAtEX, bypassingUnit.io.ID.out.rs1_value.bits, decoded_inst.rs1)
  rf.io.rs2 := Mux(bypassingUnit.io.ID.out.rs2_bypassMatchAtEX, bypassingUnit.io.ID.out.rs2_value.bits, decoded_inst.rs2)

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
  ID_EX_REG.bits.dataSignals.rs1 := rf.io.rs1_out
  ID_EX_REG.bits.dataSignals.rs2 := rf.io.rs2_out
  ID_EX_REG.bits.dataSignals.csr := decoded_inst.csr
  ID_EX_REG.bits.ctrlSignals.decode := decoder.io.out.bits
  ID_EX_REG.bits.ctrlSignals.rd_index := decoded_inst.rd

  bypassingUnit.io.ID.in.rs1_index.bits := decoded_inst.rs1
  bypassingUnit.io.ID.in.rs1_index.valid := decoder.io.out.bits.use_RS1
  bypassingUnit.io.ID.in.rs2_index.bits := decoded_inst.rs2
  bypassingUnit.io.ID.in.rs2_index.valid := decoder.io.out.bits.use_RS2

  rs1_required_but_not_valid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs1_bypassMatchAtEX -> !bypassingUnit.io.EX.in.rd.valid,
    bypassingUnit.io.ID.out.rs1_bypassMatchAtWB -> !bypassingUnit.io.WB.in.rd.valid,
  ))
  rs2_required_but_not_valid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs2_bypassMatchAtEX -> !bypassingUnit.io.EX.in.rd.valid,
    bypassingUnit.io.ID.out.rs2_bypassMatchAtWB -> !bypassingUnit.io.WB.in.rd.valid,
  ))

  if(params.debug) {
    ID_EX_REG.bits.debug.get.instruction := decoded_inst.bits
    ID_EX_REG.bits.debug.get.pc := io.frontend.resp.bits.pc
  }

  // retain the ID_EX register if stall
  when(EX_stall) {
    ID_EX_REG := ID_EX_REG
  }
  // flush the ID_EX register if branch miss, ecall or mret
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

  // TODO: ecall and mret frontend req

  branch_evaluator.io.req.bits.ALU_Result := alu.io.out
  branch_evaluator.io.req.bits.BranchType := ID_EX_REG.bits.ctrlSignals.decode.branch
  branch_evaluator.io.req.bits.destPC := ID_EX_REG.bits.dataSignals.bp_destPC
  branch_evaluator.io.req.bits.pc := ID_EX_REG.bits.dataSignals.pc
  branch_evaluator.io.req.bits.bp_taken := ID_EX_REG.bits.dataSignals.bp_taken
  branch_evaluator.io.req.valid := ID_EX_REG.valid

  if(params.useMulDiv) {
    multiplier.get.io.req.bits.rs1 := ID_EX_REG.bits.dataSignals.rs1
    multiplier.get.io.req.bits.rs2 := ID_EX_REG.bits.dataSignals.rs2
    multiplier.get.io.req.bits.funct := ID_EX_REG.bits.ctrlSignals.decode
    // TODO: review this
    multiplier.get.io.req.valid := ID_EX_REG.bits.ctrlSignals.decode.use_MUL
    multiplier.get.io.resp.ready := !(EX_WB_REG.valid && WB_stall)
  }

  csrUnit.io.req.csr := ID_EX_REG.bits.dataSignals.csr
  csrUnit.io.req.data := Mux(ID_EX_REG.bits.ctrlSignals.decode.value1 === Value1.CSR.asUInt,
    ID_EX_REG.bits.dataSignals.imm,
    ID_EX_REG.bits.dataSignals.rs1
  )
  csrUnit.io.req.funct := ID_EX_REG.bits.ctrlSignals.decode

  val EX_arithmetic_result = if(params.useMulDiv) {
    Mux(ID_EX_REG.bits.ctrlSignals.decode.use_MUL, multiplier.get.io.resp.bits.result, alu.io.out)
  } else {
    alu.io.out
  }

  ldstUnit.io.cpu.req.valid := ID_EX_REG.valid && ID_EX_REG.bits.ctrlSignals.decode.memValid
  ldstUnit.io.cpu.req.bits.addr := alu.io.out
  ldstUnit.io.cpu.req.bits.data := ID_EX_REG.bits.dataSignals.rs2
  ldstUnit.io.cpu.req.bits.funct := ID_EX_REG.bits.ctrlSignals.decode

  bypassingUnit.io.EX.in.rd.bits.index := ID_EX_REG.bits.ctrlSignals.rd_index
  bypassingUnit.io.EX.in.rd.bits.value := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.writeback_selector, 0.U)(Seq(
    WB_SEL.PC4.asUInt -> ID_EX_REG.bits.dataSignals.pc.nextPC,
    WB_SEL.ARITH.asUInt -> EX_arithmetic_result,
    WB_SEL.CSR.asUInt -> csrUnit.io.resp.data,
  ))
  bypassingUnit.io.EX.in.rd.valid := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.writeback_selector, false.B)(Seq(
    WB_SEL.PC4.asUInt -> true.B,
    WB_SEL.ARITH.asUInt -> (if(params.useMulDiv) !ID_EX_REG.bits.ctrlSignals.decode.use_MUL || multiplier.get.io.resp.valid else true.B),
    WB_SEL.CSR.asUInt -> true.B,
    WB_SEL.MEM.asUInt -> false.B,
    WB_SEL.NONE.asUInt -> false.B
  ))

  EX_WB_REG.valid := ID_EX_REG.valid
  EX_WB_REG.bits.dataSignals.pc4 := ID_EX_REG.bits.dataSignals.pc.nextPC
  EX_WB_REG.bits.dataSignals.arith_logic_result := EX_arithmetic_result
  EX_WB_REG.bits.dataSignals.csr := csrUnit.io.resp.data

  if(params.debug) EX_WB_REG.bits.debug.get := ID_EX_REG.bits.debug.get

  // WBステージがvalidかつ破棄できないかつEXステージに有効な値がある場合，またはメモリアクセス命令かつldstUnit.respがreadyでない，または乗算命令で乗算器がvalidでない
  EX_stall := (EX_WB_REG.valid && WB_stall && ID_EX_REG.valid) || (ID_EX_REG.bits.ctrlSignals.decode.memValid && !ldstUnit.io.cpu.req.ready) || if(params.useMulDiv) {
    (ID_EX_REG.bits.ctrlSignals.decode.use_MUL && !multiplier.get.io.resp.valid)
  } else {
    true.B
  }

  when(EX_stall) {
    EX_WB_REG := EX_WB_REG
  }

  // START OF WB STAGE
  val WB_RF_value = MuxLookup(EX_WB_REG.bits.ctrlSignals.RF_WB_ctrl, 0.U)(Seq(
    WB_ALU -> EX_WB_REG.bits.dataSignals.EX_ALU_val,
    WB_NOALU -> EX_WB_REG.bits.dataSignals.EX_noALU_val,
    WB_MEM -> ldstUnit.io.cpu.resp.bits.data,
  ))
  val WB_inst_can_retire = EX_WB_REG.valid && (EX_WB_REG.bits.ctrlSignals.RF_WB_ctrl =/= WB_MEM || (EX_WB_REG.bits.ctrlSignals.RF_WB_ctrl === WB_MEM && ldstUnit.io.cpu.resp.valid))
  rf.io.req.valid := WB_inst_can_retire && EX_WB_REG.bits.bypassSignals.rd_index.valid
  rf.io.req.bits.data := WB_RF_value
  rf.io.req.bits.rd := EX_WB_REG.bits.bypassSignals.rd_index.bits

  bypassingUnit.io.WB.in.valid := EX_WB_REG.valid
  bypassingUnit.io.WB.in.bits.rd.valid := EX_WB_REG.bits.bypassSignals.rd_index.valid
  bypassingUnit.io.WB.in.bits.rd.bits.index := EX_WB_REG.bits.bypassSignals.rd_index.bits
  bypassingUnit.io.WB.in.bits.rd.bits.value := WB_RF_value
  bypassingUnit.io.WB.in.bits.dcache_requested_but_not_valid := EX_WB_REG.bits.ctrlSignals.MEM_ctrl.mem_valid && !ldstUnit.io.cpu.resp.valid

  when(ID_EX_REG.valid && ID_EX_REG.bits.ctrlSignals.toWB.fence) {
    fence_in_pipeline := true.B
    io.frontend.resp.ready := false.B
    when(!bypassingUnit.io.ID.out.stall) {
      ID_EX_REG.valid := false.B
    }
  } .elsewhen(EX_WB_REG.valid && EX_WB_REG.bits.ctrlSignals.fence) {
    fence_in_pipeline := false.B
  }

  retired_inst_count := retired_inst_count + WB_inst_can_retire
  if(debug) {
    io.debug_io.get.debug_retired.bits.instruction := EX_WB_REG.bits.debug_inst.get.instruction & Fill(RISCV_Consts.INST_LEN, EX_WB_REG.valid)
    io.debug_io.get.debug_retired.bits.pc := EX_WB_REG.bits.debug_inst.get.pc & Fill(xprlen, EX_WB_REG.valid)
    io.debug_io.get.debug_retired.valid := EX_WB_REG.valid
    io.debug_io.get.debug_abi_map := rf.io.debug_abi_map.get
  }
}
