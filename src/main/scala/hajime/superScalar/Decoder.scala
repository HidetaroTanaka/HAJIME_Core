package hajime.superScalar

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.common._
import hajime.common.ScalarOpConstants._
import hajime.common.CACHE_FUNCTIONS._
import hajime.common.Instructions._
import hajime.publicmodules.{ALU_functIO, MEM_ctrl_IO}

class ID_output extends Bundle {
  val ALUin_ctrl = UInt(ALUin_X.getWidth.W)
  val ALU_funct = new ALU_functIO
  val NOALU_ctrl = UInt(NOALU_X.getWidth.W)
  val RF_WB_ctrl = UInt(WB_X.getWidth.W)
  val PC_WB_ctrl = UInt(PCWB_X.getWidth.W)
  val MEM_ctrl = new MEM_ctrl_IO
  val BranchType = UInt(BR_N.getWidth.W)
  val fence = Bool()
}

class DecoderIO extends Bundle {
  val inst = Input(Valid(UInt(RISCV_Consts.INST_LEN.W)))
  val out = ValidIO(new ID_output)
}

class Decoder(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new DecoderIO)

  val csignals = {
    ListLookup(io.inst.bits,
      //       List(valid,  ALUin_ctrl,     ALU_func,   flag, op32, NOALU_ctrl, RF_WB_ctrl, PC_WB_ctrl, memWrite, memRead,  mem_function, mem_sext, BranchType, fence,  csr)
               List(N,      ALUin_X,        ALU_X,      N,    N,    NOALU_X,    WB_X,       PCWB_X,     N,        N,        MEM_NONE,     N,        BR_N,       N,      CSR_NONE),
      Array( //
        ADD -> List(Y,      ALUin_RS1_RS2,  ALU_ADDSUB, N,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,        MEM_NONE,     N,        BR_N,       N,      CSR_NONE),
        SUB -> List(Y,      ALUin_RS1_RS2,  ALU_ADDSUB, Y,    N,    NOALU_X,    WB_ALU,     PCWB_X,     N,        N,        MEM_NONE,     N,        BR_N,       N,      CSR_NONE),
        SLL -> List(Y, ALUin_RS1_RS2, ALU_SLL, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SLT -> List(Y, ALUin_RS1_RS2, ALU_SLT, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SLTU -> List(Y, ALUin_RS1_RS2, ALU_SLTU, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        XOR -> List(Y, ALUin_RS1_RS2, ALU_XOR, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SRL -> List(Y, ALUin_RS1_RS2, ALU_SR, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SRA -> List(Y, ALUin_RS1_RS2, ALU_SR, Y, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        OR -> List(Y, ALUin_RS1_RS2, ALU_OR, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        AND -> List(Y, ALUin_RS1_RS2, ALU_AND, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        ADDW -> List(Y, ALUin_RS1_RS2, ALU_ADDSUB, N, Y, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SUBW -> List(Y, ALUin_RS1_RS2, ALU_ADDSUB, Y, Y, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SLLW -> List(Y, ALUin_RS1_RS2, ALU_SLL, N, Y, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SRLW -> List(Y, ALUin_RS1_RS2, ALU_SR, N, Y, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SRAW -> List(Y, ALUin_RS1_RS2, ALU_SR, Y, Y, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),

        //       List(valid, ALUin_ctrl,     ALU_func,   flag, op32, NOALU_ctrl, RF_WB_ctrl, PC_WB_ctrl, memWrite, memRead, mem_function, mem_sext, BranchType)
        ADDI -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SLTI -> List(Y, ALUin_RS1_IMI, ALU_SLT, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SLTIU -> List(Y, ALUin_RS1_IMI, ALU_SLTU, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        XORI -> List(Y, ALUin_RS1_IMI, ALU_XOR, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        ORI -> List(Y, ALUin_RS1_IMI, ALU_OR, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        ANDI -> List(Y, ALUin_RS1_IMI, ALU_AND, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SLLI -> List(Y, ALUin_RS1_IMI, ALU_SLL, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SRLI -> List(Y, ALUin_RS1_IMI, ALU_SR, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SRAI -> List(Y, ALUin_RS1_IMI, ALU_SR, Y, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        ADDIW -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, Y, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SLLIW -> List(Y, ALUin_RS1_IMI, ALU_SLL, N, Y, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SRLIW -> List(Y, ALUin_RS1_IMI, ALU_SR, N, Y, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        SRAIW -> List(Y, ALUin_RS1_IMI, ALU_SR, Y, Y, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),

        //       List(valid, ALUin_ctrl,     ALU_func,   flag, op32, NOALU_ctrl, RF_WB_ctrl, PC_WB_ctrl, memWrite, memRead, mem_function, mem_sext, BranchType)
        LB -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, N, NOALU_RS2, WB_MEM, PCWB_X, N, Y, BYTE, Y, BR_N, N, CSR_NONE),
        LH -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, N, NOALU_RS2, WB_MEM, PCWB_X, N, Y, HALFWORD, Y, BR_N, N, CSR_NONE),
        LW -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, N, NOALU_RS2, WB_MEM, PCWB_X, N, Y, WORD, Y, BR_N, N, CSR_NONE),
        LD -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, N, NOALU_RS2, WB_MEM, PCWB_X, N, Y, DOUBLEWORD, Y, BR_N, N, CSR_NONE),
        LBU -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, N, NOALU_RS2, WB_MEM, PCWB_X, N, Y, BYTE, N, BR_N, N, CSR_NONE),
        LHU -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, N, NOALU_RS2, WB_MEM, PCWB_X, N, Y, HALFWORD, N, BR_N, N, CSR_NONE),
        LWU -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, N, NOALU_RS2, WB_MEM, PCWB_X, N, Y, WORD, N, BR_N, N, CSR_NONE),

        JALR -> List(Y, ALUin_RS1_IMI, ALU_ADDSUB, N, N, NOALU_PC4, WB_NOALU, PCWB_JALR, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),

        //       List(valid, ALUin_ctrl,     ALU_func,   flag, op32, NOALU_ctrl, RF_WB_ctrl, PC_WB_ctrl, memWrite, memRead, mem_function, mem_sext, BranchType)
        SB -> List(Y, ALUin_RS1_IMS, ALU_ADDSUB, N, N, NOALU_RS2, WB_X, PCWB_X, Y, N, BYTE, N, BR_N, N, CSR_NONE),
        SH -> List(Y, ALUin_RS1_IMS, ALU_ADDSUB, N, N, NOALU_RS2, WB_X, PCWB_X, Y, N, HALFWORD, N, BR_N, N, CSR_NONE),
        SW -> List(Y, ALUin_RS1_IMS, ALU_ADDSUB, N, N, NOALU_RS2, WB_X, PCWB_X, Y, N, WORD, N, BR_N, N, CSR_NONE),
        SD -> List(Y, ALUin_RS1_IMS, ALU_ADDSUB, N, N, NOALU_RS2, WB_X, PCWB_X, Y, N, DOUBLEWORD, N, BR_N, N, CSR_NONE),

        BEQ -> List(Y, ALUin_RS1_RS2, ALU_XOR, N, N, NOALU_PC_IF_MISPREDICT, WB_X, PCWB_BRANCH, N, N, MEM_NONE, N, BR_EQ, N, CSR_NONE),
        BNE -> List(Y, ALUin_RS1_RS2, ALU_XOR, N, N, NOALU_PC_IF_MISPREDICT, WB_X, PCWB_BRANCH, N, N, MEM_NONE, N, BR_NE, N, CSR_NONE),
        BLT -> List(Y, ALUin_RS1_RS2, ALU_SLT, N, N, NOALU_PC_IF_MISPREDICT, WB_X, PCWB_BRANCH, N, N, MEM_NONE, N, BR_LT, N, CSR_NONE),
        BGE -> List(Y, ALUin_RS1_RS2, ALU_SLT, N, N, NOALU_PC_IF_MISPREDICT, WB_X, PCWB_BRANCH, N, N, MEM_NONE, N, BR_GE, N, CSR_NONE),
        BLTU -> List(Y, ALUin_RS1_RS2, ALU_SLTU, N, N, NOALU_PC_IF_MISPREDICT, WB_X, PCWB_BRANCH, N, N, MEM_NONE, N, BR_LTU, N, CSR_NONE),
        BGEU -> List(Y, ALUin_RS1_RS2, ALU_SLTU, N, N, NOALU_PC_IF_MISPREDICT, WB_X, PCWB_BRANCH, N, N, MEM_NONE, N, BR_GEU, N, CSR_NONE),

        LUI -> List(Y, ALUin_X, ALU_X, N, N, NOALU_IMMU, WB_NOALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        AUIPC -> List(Y, ALUin_PC_IMMU, ALU_ADDSUB, N, N, NOALU_X, WB_ALU, PCWB_X, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),

        // correct pc is written in ID stage
        JAL -> List(Y, ALUin_X, ALU_X, N, N, NOALU_PC4, WB_NOALU, PCWB_JAL, N, N, MEM_NONE, N, BR_N, N, CSR_NONE),
        //        List(valid,  ALUin_ctrl,     ALU_func,   flag, op32, NOALU_ctrl, RF_WB_ctrl, PC_WB_ctrl, memWrite, memRead,  mem_function, mem_sext, BranchType, fence,  csr)
        FENCE  -> List(Y,      ALUin_X,        ALU_X,      N,    N,    NOALU_X,    WB_X,       PCWB_X,     N,        N,        MEM_NONE,     N,        BR_N,       Y,      CSR_NONE),
        CSRRWI -> List(Y,      ALUin_RS1_CSR,  ALU_ADDSUB, N,    N,    NOALU_X,    WB_NOALU,   PCWB_X,     N,        N,        MEM_NONE,     N,        BR_N,       N,      CSR_WRITE),

      ))
  }
}
