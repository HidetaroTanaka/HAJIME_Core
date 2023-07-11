package hajime.simple4Stage

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.common.{CACHE_FUNCTIONS, COMPILE_CONSTANTS, InstBundle, RISCV_Consts}
import hajime.common.ScalarOpConstants._

class MEM_ctrl_IO extends Bundle {
  val memWrite = Bool()
  val memRead  = Bool()
  val mem_func = UInt(CACHE_FUNCTIONS.BYTE.getWidth.W)
  val mem_sext = Bool()

  def mem_valid: Bool = memWrite || memRead
}

/**
 * decoded results of inst
 */
class ID_output extends Bundle {
  val ALUin_ctrl = UInt(ALUin_X.getWidth.W)
  val ALU_funct  = new ALU_functIO
  val NOALU_ctrl = UInt(NOALU_X.getWidth.W)
  val RF_WB_ctrl = UInt(WB_X.getWidth.W)
  val PC_WB_ctrl = UInt(PCWB_X.getWidth.W)
  val MEM_ctrl   = new MEM_ctrl_IO
  val BranchType = UInt(BR_N.getWidth.W)
  val fence = Bool()
}

/**
 * this does only decode, top module determines whether inst itself is valid or not
 * @param xprlen
 * @param inst_width
 */
class DecoderIO(xprlen: Int) extends Bundle {
  // from frontend
  val inst = Input(UInt(RISCV_Consts.INST_LEN.W))
  // output
  val out = ValidIO(new ID_output)
}

import hajime.common.Instructions._
import hajime.common.CACHE_FUNCTIONS._

class Decoder(xprlen: Int) extends Module {
  val io = IO(new DecoderIO(xprlen))

  // R: x[rd] = ALU(x[rs1], x[rs2])
  // I: x[rd] = ALU(x[rs1], imm_i)
  // I_LOAD: x[rd] = mem[ALU_ADD(x[rs1], imm_i)]
  // I_JALR: PC = ALU_ADD(x[rs1], imm_i), x[rd] = PC+4
  // S: mem[ALU_ADD(x[rs1], imm_s)] = x[rs2]
  // B: PC = if (ALU(x[rs1], x[rs2]) == taken) PC+imm_b else PC+4
  // U_LUI: x[rd] = imm_u
  // U_AUIPC: x[rd] = ALU_ADD(PC, imm_u)
  // J: PC = PC+imm_j (from decode stage), x[rd] = PC+4

  // ALU_in1: x[rs1], PC(only with imm_u, imm_j)
  // ALU_in2: x[rs2], imm_i, imm_s, imm_u, imm_j
  // ALU_func: (funct3, addsubflag, op32)
  // VALUE_NOALU: PC+4, x[rs2], imm_u
  // RF_WB: ALU, mem, value_noALU(PC+4, imm_u)
  // PC_WRITE_FROM: PC+x[rs1](JALR), PC+imm_b(BRANCH), PC+imm_j(JAL)
  // MEM: (MEMWRITE, MEMREAD, MEM_func, MEM_sext)

  val csignals = {
  ListLookup(io.inst,
    //         List(valid, ALUin_ctrl,     ALU_func,   flag, op32, NOALU_ctrl, RF_WB_ctrl, PC_WB_ctrl, memWrite, memRead, mem_function, mem_sext, BranchType, fence)
               List(N,     ALUin_X,        ALU_X,      N,    N,    NOALU_X,    WB_X,       PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
    Array( //
      ADD   -> List(Y,     ALUin_RS1_RS2,  ALU_ADDSUB, N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SUB   -> List(Y,     ALUin_RS1_RS2,  ALU_ADDSUB, Y,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SLL   -> List(Y,     ALUin_RS1_RS2,  ALU_SLL,    N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SLT   -> List(Y,     ALUin_RS1_RS2,  ALU_SLT,    N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SLTU  -> List(Y,     ALUin_RS1_RS2,  ALU_SLTU,   N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      XOR   -> List(Y,     ALUin_RS1_RS2,  ALU_XOR,    N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SRL   -> List(Y,     ALUin_RS1_RS2,  ALU_SR,     N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SRA   -> List(Y,     ALUin_RS1_RS2,  ALU_SR,     Y,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      OR    -> List(Y,     ALUin_RS1_RS2,  ALU_OR,     N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      AND   -> List(Y,     ALUin_RS1_RS2,  ALU_AND,    N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      ADDW  -> List(Y,     ALUin_RS1_RS2,  ALU_ADDSUB, N,    Y,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SUBW  -> List(Y,     ALUin_RS1_RS2,  ALU_ADDSUB, Y,    Y,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SLLW  -> List(Y,     ALUin_RS1_RS2,  ALU_SLL,    N,    Y,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SRLW  -> List(Y,     ALUin_RS1_RS2,  ALU_SR,     N,    Y,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SRAW  -> List(Y,     ALUin_RS1_RS2,  ALU_SR,     Y,    Y,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),

      //       List(valid, ALUin_ctrl,     ALU_func,   flag, op32, NOALU_ctrl, RF_WB_ctrl, PC_WB_ctrl, memWrite, memRead, mem_function, mem_sext, BranchType)
      ADDI  -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SLTI  -> List(Y,     ALUin_RS1_IMI,  ALU_SLT,    N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SLTIU -> List(Y,     ALUin_RS1_IMI,  ALU_SLTU,   N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      XORI  -> List(Y,     ALUin_RS1_IMI,  ALU_XOR,    N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      ORI   -> List(Y,     ALUin_RS1_IMI,  ALU_OR,     N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      ANDI  -> List(Y,     ALUin_RS1_IMI,  ALU_AND,    N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SLLI  -> List(Y,     ALUin_RS1_IMI,  ALU_SLL,    N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SRLI  -> List(Y,     ALUin_RS1_IMI,  ALU_SR,     N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SRAI  -> List(Y,     ALUin_RS1_IMI,  ALU_SR,     Y,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      ADDIW -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    Y,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SLLIW -> List(Y,     ALUin_RS1_IMI,  ALU_SLL,    N,    Y,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SRLIW -> List(Y,     ALUin_RS1_IMI,  ALU_SR,     N,    Y,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      SRAIW -> List(Y,     ALUin_RS1_IMI,  ALU_SR,     Y,    Y,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),

      //       List(valid, ALUin_ctrl,     ALU_func,   flag, op32, NOALU_ctrl, RF_WB_ctrl, PC_WB_ctrl, memWrite, memRead, mem_function, mem_sext, BranchType)
      LB    -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    N,    NOALU_RS2,  WB_MEM,     PCWB_X,     N,        Y,       BYTE,         Y,        BR_N,       N),
      LH    -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    N,    NOALU_RS2,  WB_MEM,     PCWB_X,     N,        Y,       HALFWORD,     Y,        BR_N,       N),
      LW    -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    N,    NOALU_RS2,  WB_MEM,     PCWB_X,     N,        Y,       WORD,         Y,        BR_N,       N),
      LD    -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    N,    NOALU_RS2,  WB_MEM,     PCWB_X,     N,        Y,       DOUBLEWORD,   Y,        BR_N,       N),
      LBU   -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    N,    NOALU_RS2,  WB_MEM,     PCWB_X,     N,        Y,       BYTE,         N,        BR_N,       N),
      LHU   -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    N,    NOALU_RS2,  WB_MEM,     PCWB_X,     N,        Y,       HALFWORD,     N,        BR_N,       N),
      LWU   -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    N,    NOALU_RS2,  WB_MEM,     PCWB_X,     N,        Y,       WORD,         N,        BR_N,       N),

      JALR  -> List(Y,     ALUin_RS1_IMI,  ALU_ADDSUB, N,    N,    NOALU_PC4,  WB_NOALU,   PCWB_JALR,  N,        N,       MEM_NONE,     N,        BR_N,       N),

      //       List(valid, ALUin_ctrl,     ALU_func,   flag, op32, NOALU_ctrl, RF_WB_ctrl, PC_WB_ctrl, memWrite, memRead, mem_function, mem_sext, BranchType)
      SB    -> List(Y,     ALUin_RS1_IMS,  ALU_ADDSUB, N,    N,    NOALU_RS2,               WB_X,       PCWB_X,     Y,        N,       BYTE,         N,        BR_N,       N),
      SH    -> List(Y,     ALUin_RS1_IMS,  ALU_ADDSUB, N,    N,    NOALU_RS2,               WB_X,       PCWB_X,     Y,        N,       HALFWORD,     N,        BR_N,       N),
      SW    -> List(Y,     ALUin_RS1_IMS,  ALU_ADDSUB, N,    N,    NOALU_RS2,               WB_X,       PCWB_X,     Y,        N,       WORD,         N,        BR_N,       N),
      SD    -> List(Y,     ALUin_RS1_IMS,  ALU_ADDSUB, N,    N,    NOALU_RS2,               WB_X,       PCWB_X,     Y,        N,       DOUBLEWORD,   N,        BR_N,       N),

      BEQ   -> List(Y,     ALUin_RS1_RS2,  ALU_XOR,    N,    N,    NOALU_PC_IF_MISPREDICT,  WB_X,       PCWB_BRANCH,N,        N,       MEM_NONE,     N,        BR_EQ,      N),
      BNE   -> List(Y,     ALUin_RS1_RS2,  ALU_XOR,    N,    N,    NOALU_PC_IF_MISPREDICT,  WB_X,       PCWB_BRANCH,N,        N,       MEM_NONE,     N,        BR_NE,      N),
      BLT   -> List(Y,     ALUin_RS1_RS2,  ALU_SLT,    N,    N,    NOALU_PC_IF_MISPREDICT,  WB_X,       PCWB_BRANCH,N,        N,       MEM_NONE,     N,        BR_LT,      N),
      BGE   -> List(Y,     ALUin_RS1_RS2,  ALU_SLT,    N,    N,    NOALU_PC_IF_MISPREDICT,  WB_X,       PCWB_BRANCH,N,        N,       MEM_NONE,     N,        BR_GE,      N),
      BLTU  -> List(Y,     ALUin_RS1_RS2,  ALU_SLTU,   N,    N,    NOALU_PC_IF_MISPREDICT,  WB_X,       PCWB_BRANCH,N,        N,       MEM_NONE,     N,        BR_LTU,     N),
      BGEU  -> List(Y,     ALUin_RS1_RS2,  ALU_SLTU,   N,    N,    NOALU_PC_IF_MISPREDICT,  WB_X,       PCWB_BRANCH,N,        N,       MEM_NONE,     N,        BR_GEU,     N),

      LUI   -> List(Y,     ALUin_X,        ALU_X,      N,    N,    NOALU_IMMU,              WB_NOALU,   PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),
      AUIPC -> List(Y,     ALUin_PC_IMMU,  ALU_ADDSUB, N,    N,    NOALU_X,                 WB_ALU,     PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       N),

      //                                                                                                correct pc is written in ID stage
      JAL   -> List(Y,     ALUin_X,        ALU_X,      N,    N,    NOALU_PC4,               WB_NOALU,   PCWB_JAL,   N,        N,       MEM_NONE,     N,        BR_N,       N),
      // add others later
      FENCE -> List(Y,     ALUin_X,        ALU_X,      N,    N,    NOALU_X,                 WB_X,       PCWB_X,     N,        N,       MEM_NONE,     N,        BR_N,       Y),

    ))
  }

  io.out.valid := (if(xprlen == 64) csignals(0) else csignals(0).asBool && !csignals(4).asBool)
  io.out.bits.ALUin_ctrl := csignals(1)
  io.out.bits.ALU_funct.fn := csignals(2)
  io.out.bits.ALU_funct.addsubFlag := csignals(3)
  io.out.bits.ALU_funct.op32 := csignals(4)
  io.out.bits.NOALU_ctrl := csignals(5)
  io.out.bits.RF_WB_ctrl := csignals(6)
  io.out.bits.PC_WB_ctrl := csignals(7)
  io.out.bits.MEM_ctrl.memWrite := csignals(8)
  io.out.bits.MEM_ctrl.memRead := csignals(9)
  io.out.bits.MEM_ctrl.mem_func := csignals(10)
  io.out.bits.MEM_ctrl.mem_sext := csignals(11)
  io.out.bits.BranchType := csignals(12)
  io.out.bits.fence := csignals(13)
}

object Decoder extends App {
  def apply(xprlen: Int): Decoder = new Decoder(xprlen)
  (new chisel3.stage.ChiselStage).emitVerilog(apply(xprlen = RISCV_Consts.XLEN), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
