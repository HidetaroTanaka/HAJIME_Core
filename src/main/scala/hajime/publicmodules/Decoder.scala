package hajime.publicmodules

import chisel3._
import chisel3.util._
import hajime.common.CACHE_FUNCTIONS._
import hajime.common.Instructions._
import hajime.common._

abstract trait DecodeConstants extends ScalarOpConstants {
  val table: Array[(BitPat, List[BitPat])]
}

object RV32IDecode extends DecodeConstants {
  val table: Array[(BitPat, List[BitPat])] = Array(
    //      List(valid, Branch, ALU_in1,   ALU_in2, Arithmetic_Function, ALU_flag, ALU_op32, Multiply_Function, WriteBack_selector, Memory_Function, Memory_Length, Memory_SEXT, CSR_Function, fence)
    // R-type
    ADD    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SUB    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.ADDSUB, Y, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SLL    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.SLL,    N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SLT    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.SLT,    N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SLTU   -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.SLTU,   N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    XOR    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.XOR,    N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SRL    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.SR,     N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SRA    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.SR,     Y, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    OR     -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.OR,     N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    AND    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.AND,    N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),

    // I-type
    ADDI   -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SLTI   -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.SLT,    N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SLTIU  -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.SLTU,   N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    XORI   -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.XOR,    N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    ORI    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.OR,     N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    ANDI   -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.AND,    N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SLLI   -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.SLL,    N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SRLI   -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.SR,     N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    SRAI   -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.SR,     Y, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    LB     -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.B, Y, CSR_FCN.N, N),
    LH     -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.H, Y, CSR_FCN.N, N),
    LW     -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.W, Y, CSR_FCN.N, N),
    LBU    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.B, N, CSR_FCN.N, N),
    LHU    -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.H, N, CSR_FCN.N, N),
    JALR   -> List(Y, Branch.JALR, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.PC4,   MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),

    // S-type
    SB     -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.B, N, CSR_FCN.N, N),
    SH     -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.H, N, CSR_FCN.N, N),
    SW     -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.I_IMM, ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.W, N, CSR_FCN.N, N),

    // B-type
    BEQ    -> List(Y, Branch.EQ,   ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.XOR,    N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    BNE    -> List(Y, Branch.NE,   ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.XOR,    N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    BLT    -> List(Y, Branch.LT,   ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.SLT,    N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    BGE    -> List(Y, Branch.GE,   ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.SLT,    N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    BLTU   -> List(Y, Branch.LTU,  ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.SLTU,   N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    BGEU   -> List(Y, Branch.GEU,  ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.SLTU,   N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),

    // U-type
    LUI    -> List(Y, Branch.NONE, ALU_in1.U_IMM, ALU_in2.ZERO,  ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
    AUIPC  -> List(Y, Branch.NONE, ALU_in1.U_IMM, ALU_in2.PC,    ARITH_FCN.ADDSUB, N, N, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),

    // J-type
    JAL    -> List(Y, Branch.JAL,  DontCare,      DontCare,      DontCare,         N, N, MUL_FCN.NONE, WB_SEL.PC4,   MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),

    // SYSTEM
    FENCE  -> List(Y, Branch.NONE, DontCare,      DontCare,      DontCare,         N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, Y),
    ECALL  -> List(Y, Branch.NONE, DontCare,      DontCare,      DontCare,         N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.I, N),
    // ebreak is unimplemented
    EBREAK -> List(Y, Branch.NONE, DontCare,      DontCare,      DontCare,         N, N, MUL_FCN.NONE, WB_SEL.NONE,  MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
  )
}

object RV64IDecode extends DecodeConstants {
  val table: Array[(BitPat, List[BitPat])] = Array(
    ADDW   -> List(Y, Branch.NONE, ALU_in1.RS1,   ALU_in2.RS2,   ARITH_FCN.ADDSUB, N, Y, MUL_FCN.NONE, WB_SEL.ARITH, MEM_FCN.M_NONE, DontCare,  N, CSR_FCN.N, N),
  )
}

class _Decoder(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  when(ALU_in1.RS1 === ALU_in1.RS1) {
    println("aaaaa")
  }
}


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
        // CSRRW  -> List(Y,),
        CSRRWI -> List(Y,      ALUin_X,        ALU_X, N,    N,    NOALU_X,    WB_CSR,     PCWB_X,     N,        N,        MEM_NONE,     N,        BR_N,       N,      CSR_WRITE),

      ))
    def amogus(x: Int, xs: Int*): Seq[Int] = {
      x +: xs
    }
  }
}
