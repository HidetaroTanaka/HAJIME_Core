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

class BasicCtrlSignals(implicit params: HajimeCoreParams) extends Bundle {
  val decode = new DecoderIO
  val rd_index = UInt(5.W)
}

class ID_EX_dataSignals(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val pc = new ProgramCounter()
  val bp_destPC = UInt(xprlen.W)
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

  val fence_in_pipeline = RegInit(false.B)

  // Modules
  val decoder = Module(new Decoder())
  val branch_predictor = Module(new BranchPredictor())
  val rf = Module(new RegFile())
  val alu = Module(new ALU())
  val branch_evaluator = Module(new BranchEvaluator())
  val bypassingUnit = Module(new BypassingUnit())
  val ldstUnit = Module(new LDSTUnit())
  val csrUnit = if(params.debug) Some(Module(new CSRUnit())) else None
  val multiplier = if(params.debug) Some(Module(new Multiplier())) else None

  ldstUnit.io.dcache_axi4lite <> io.dcache_axi4lite

  // START OF ID STAGE
  io.frontend.resp.ready := ((cycle_count =/= 0.U(xprlen.W)) && !bypassingUnit.io.ID.out.stall && !fence_in_pipeline) || io.frontend.req.valid

  val decoded_inst = Wire(new InstBundle(xprlen))
  decoded_inst.bits := io.frontend.resp.bits.inst
  val ID_EX_REG = Reg(Valid(new ID_EX_IO(xprlen)))
  val pcPlus4 = io.frontend.resp.bits.pc + 4.U(xprlen.W)

  io.frontend.req := Mux(branch_evaluator.io.out.valid && ID_EX_REG.valid, branch_evaluator.io.out, branch_predictor.io.req)
  branch_predictor.io.pc := io.frontend.resp.bits.pc
  branch_predictor.io.pc4 := pcPlus4
  branch_predictor.io.branch_pc_if_taken := Mux(branch_predictor.io.isJAL, decoded_inst.j_imm, decoded_inst.b_imm) + io.frontend.resp.bits.pc
  branch_predictor.io.isJALR := (decoder.io.out.bits.PC_WB_ctrl === PCWB_JALR)
  branch_predictor.io.isJAL := (decoder.io.out.bits.PC_WB_ctrl === PCWB_JAL)
  branch_predictor.io.isBranch := (decoder.io.out.bits.PC_WB_ctrl === PCWB_BRANCH)

  decoder.io.inst := decoded_inst.bits
  rf.io.rs1 := decoded_inst.rs1
  rf.io.rs2 := decoded_inst.rs2

  val ID_inst_valid = decoder.io.out.valid && io.frontend.resp.valid
  ID_EX_REG.valid := ID_inst_valid && io.frontend.resp.ready
  ID_EX_REG.bits.dataSignals.ID_noALU_val := MuxLookup(decoder.io.out.bits.NOALU_ctrl, 0.U)(
    Seq(
      NOALU_IMMU -> decoded_inst.u_imm,
      NOALU_PC4 -> pcPlus4,
      NOALU_RS2 -> Mux(bypassingUnit.io.ID.out.rs2_value.valid, bypassingUnit.io.ID.out.rs2_value.bits, rf.io.rs2_out),
      NOALU_PC_IF_MISPREDICT -> Mux(branch_predictor.io.req.valid, pcPlus4, branch_predictor.io.branch_pc_if_taken),
    )
  )
  ID_EX_REG.bits.dataSignals.ID_aluin1_val := MuxCase(0.U, Seq(
    ALUin_USE_RS1(decoder.io.out.bits.ALUin_ctrl) -> Mux(bypassingUnit.io.ID.out.rs1_value.valid, bypassingUnit.io.ID.out.rs1_value.bits, rf.io.rs1_out),
    (decoder.io.out.bits.ALUin_ctrl === ALUin_PC_IMMU) -> io.frontend.resp.bits.pc,
  ))
  ID_EX_REG.bits.dataSignals.ID_aluin2_val := MuxLookup(decoder.io.out.bits.ALUin_ctrl, 0.U)(
    Seq(
      ALUin_RS1_RS2 -> Mux(bypassingUnit.io.ID.out.rs2_value.valid, bypassingUnit.io.ID.out.rs2_value.bits, rf.io.rs2_out),
      ALUin_RS1_IMI -> decoded_inst.i_imm,
      ALUin_RS1_IMS -> decoded_inst.s_imm,
      ALUin_PC_IMMU -> decoded_inst.u_imm,
    )
  )
  ID_EX_REG.bits.bypassSignals.rd_index.bits := decoded_inst.rd
  ID_EX_REG.bits.bypassSignals.rd_index.valid := decoder.io.out.bits.RF_WB_ctrl =/= WB_X
  ID_EX_REG.bits.dataSignals.predicated_PC := branch_predictor.io.req
  ID_EX_REG.bits.ctrlSignals.toEX := decoder.io.out.bits
  ID_EX_REG.bits.ctrlSignals.toEX.ALUin_ctrl := decoder.io.out.bits.ALUin_ctrl
  ID_EX_REG.bits.ctrlSignals.toWB := decoder.io.out.bits

  bypassingUnit.io.ID.in.valid := ID_inst_valid
  bypassingUnit.io.ID.in.bits.rs1_index.bits := decoded_inst.rs1
  bypassingUnit.io.ID.in.bits.rs1_index.valid := ALUin_USE_RS1(decoder.io.out.bits.ALUin_ctrl)
  bypassingUnit.io.ID.in.bits.rs2_index.bits := decoded_inst.rs2
  bypassingUnit.io.ID.in.bits.rs2_index.valid := ALUin_USE_RS2(decoder.io.out.bits.ALUin_ctrl) || NOALU_USE_RS2(decoder.io.out.bits.NOALU_ctrl)

  if(debug) {
    ID_EX_REG.bits.debug_inst.get.instruction := decoded_inst.bits
    ID_EX_REG.bits.debug_inst.get.pc := io.frontend.resp.bits.pc
  }

  // retain the ID_EX register if stall
  when(bypassingUnit.io.EX.out.stall) {
    ID_EX_REG := ID_EX_REG
  }
  // flush the ID_EX register if branch miss
  when(branch_evaluator.io.out.valid) {
    ID_EX_REG.valid := false.B
  }

  // START OF EX STAGE
  alu.io.in1 := ID_EX_REG.bits.dataSignals.ID_aluin1_val
  alu.io.in2 := ID_EX_REG.bits.dataSignals.ID_aluin2_val
  alu.io.funct := ID_EX_REG.bits.ctrlSignals.toEX.ALU_funct

  val ID_noALU_val = ID_EX_REG.bits.dataSignals.ID_noALU_val

  branch_evaluator.io.req.bits.ALU_Result := alu.io.out
  branch_evaluator.io.req.bits.BranchType := ID_EX_REG.bits.ctrlSignals.toEX.BranchType
  branch_evaluator.io.req.bits.PC_WB_ctrl := ID_EX_REG.bits.ctrlSignals.toEX.PC_WB_ctrl
  branch_evaluator.io.req.bits.bp_taken := ID_EX_REG.bits.dataSignals.predicated_PC.valid
  branch_evaluator.io.req.bits.jalr_predPC := ID_EX_REG.bits.dataSignals.predicated_PC.bits.pc
  branch_evaluator.io.req.bits.PC_if_bp_incorrect := ID_noALU_val
  branch_evaluator.io.req.valid := ID_EX_REG.valid

  bypassingUnit.io.EX.in.valid := ID_EX_REG.valid
  bypassingUnit.io.EX.in.bits.rd.bits.value := Mux(ID_EX_REG.bits.ctrlSignals.toWB.RF_WB_ctrl === WB_ALU, alu.io.out, ID_EX_REG.bits.dataSignals.ID_noALU_val)
  bypassingUnit.io.EX.in.bits.rd.bits.index := ID_EX_REG.bits.bypassSignals.rd_index.bits
  bypassingUnit.io.EX.in.bits.rd.valid := (ID_EX_REG.bits.ctrlSignals.toWB.RF_WB_ctrl =/= WB_X && ID_EX_REG.bits.ctrlSignals.toWB.RF_WB_ctrl =/= WB_MEM)
  bypassingUnit.io.EX.in.bits.dcache_req_not_ready_and_has_mem_inst_in_EX_stage := ID_EX_REG.bits.ctrlSignals.toWB.MEM_ctrl.mem_valid && !ldstUnit.io.cpu.req.ready

  ldstUnit.io.cpu.req.valid := ID_EX_REG.valid && ID_EX_REG.bits.ctrlSignals.toWB.MEM_ctrl.mem_valid
  ldstUnit.io.cpu.req.bits.addr := alu.io.out
  ldstUnit.io.cpu.req.bits.data := ID_EX_REG.bits.dataSignals.ID_noALU_val
  ldstUnit.io.cpu.req.bits.MEM_ctrl := ID_EX_REG.bits.ctrlSignals.toWB.MEM_ctrl

  val EX_WB_REG = Reg(Valid(new EX_WB_IO(xprlen)))

  EX_WB_REG.valid := ID_EX_REG.valid // && !branch_evaluator.io.out.valid
  EX_WB_REG.bits.bypassSignals.rd_index := ID_EX_REG.bits.bypassSignals.rd_index
  EX_WB_REG.bits.dataSignals.EX_ALU_val := alu.io.out
  EX_WB_REG.bits.dataSignals.EX_noALU_val := ID_noALU_val
  EX_WB_REG.bits.ctrlSignals := ID_EX_REG.bits.ctrlSignals.toWB

  if(debug) EX_WB_REG.bits.debug_inst.get := ID_EX_REG.bits.debug_inst.get

  when(bypassingUnit.io.WB.out.stall) {
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
