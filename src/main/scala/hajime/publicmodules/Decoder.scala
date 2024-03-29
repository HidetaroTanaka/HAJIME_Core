package hajime.publicmodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.Instructions._
import hajime.common._

trait DecodeConstants extends ScalarOpConstants {
  val table: Array[(BitPat, List[EnumType])]
}

object RV32IDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    //      　     valid           ALU_in1       ALU_in2       Arithmetic_Function     ALU_flag            Memory_Function                          fence)
    //             |  Branch       |             |             |                       |  op32             |               Memory_Length            |
    //             |  |            |             |             |                       |  |  WB_selector   |               |          Mem_SEXT      |
    // R-type      |  |            |             |             |                       |  |  |             |               |          |  CSR_Func   |
    ADD    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SUB    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.ADDSUB,  Y, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLL    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLL,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLT    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLT,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLTU   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLTU,    N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    XOR    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.XOR,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRL    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SR,      N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRA    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SR,      Y, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    OR     -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.OR,      N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    AND    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.AND,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // I-type
    ADDI   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLTI   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SLT,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLTIU  -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SLTU,    N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    XORI   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.XOR,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    ORI    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.OR,      N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    ANDI   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.AND,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLLI   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SLL,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRLI   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SR,      N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRAI   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SR,      Y, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    LB     -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.B, Y, CSR_FCN.N, N),
    LH     -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.H, Y, CSR_FCN.N, N),
    LW     -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.W, Y, CSR_FCN.N, N),
    LBU    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.B, N, CSR_FCN.N, N),
    LHU    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.H, N, CSR_FCN.N, N),
    JALR   -> List(Y, Branch.JALR,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.PC4,   MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // S-type
    SB     -> List(Y, Branch.NONE,  Value1.RS1,   Value2.S_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.B, N, CSR_FCN.N, N),
    SH     -> List(Y, Branch.NONE,  Value1.RS1,   Value2.S_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.H, N, CSR_FCN.N, N),
    SW     -> List(Y, Branch.NONE,  Value1.RS1,   Value2.S_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.W, N, CSR_FCN.N, N),

    // B-type
    BEQ    -> List(Y, Branch.EQ,    Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.XOR,     N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BNE    -> List(Y, Branch.NE,    Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.XOR,     N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BLT    -> List(Y, Branch.LT,    Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLT,     N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BGE    -> List(Y, Branch.GE,    Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLT,     N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BLTU   -> List(Y, Branch.LTU,   Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLTU,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BGEU   -> List(Y, Branch.GEU,   Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLTU,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // U-type
    LUI    -> List(Y, Branch.NONE,  Value1.U_IMM, Value2.ZERO,  ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    AUIPC  -> List(Y, Branch.NONE,  Value1.U_IMM, Value2.PC,    ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // J-type
    JAL    -> List(Y, Branch.JAL,   Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.PC4,   MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // SYSTEM
    FENCE  -> List(Y, Branch.NONE,  Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, Y),
    // csrAddr[mepc] = address of ECALL, csrAddr[mcause]=cause, pc=Cat(mtvec.head(xprlen-2), 0.U(2.W))
    ECALL  -> List(Y, Branch.ECALL, Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.I, N),
    // pc=csrAddr[mepc]
    MRET   -> List(Y, Branch.MRET,  Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.R, N),
    // ebreak is unimplemented
    EBREAK -> List(Y, Branch.NONE,  Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    // TODO: implement wfi and external exception
  ).map {
    // not vector instructions
    case (bitpat, enumTypeList) => (bitpat, enumTypeList :+ N)
  }
}

object RV64IDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    // R-type
    ADDW   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.ADDSUB,  N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SUBW   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.ADDSUB,  Y, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLLW   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLL,     N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRLW   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SR,      N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRAW   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SR,      Y, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // I-type
    ADDIW  -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLLIW  -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SLL,     N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRLIW  -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SR,      N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRAIW  -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SR,      Y, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    LD     -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.D, Y, CSR_FCN.N, N),
    LWU    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.W, N, CSR_FCN.N, N),

    // S-type
    SD     -> List(Y, Branch.NONE,  Value1.RS1,   Value2.S_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.D, N, CSR_FCN.N, N),
  ).map {
    // not vector instructions
    case (bitpat, enumTypeList) => (bitpat, enumTypeList :+ N)
  }
}

object RV32MDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    MUL    -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_LOW,  N, N, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    MULH   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_HIGH, N, N, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    MULHSU -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_HISU, N, N, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    MULHU  -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_HIU,  N, N, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
  ).map {
    // not vector instructions
    case (bitpat, enumTypeList) => (bitpat, enumTypeList :+ N)
  }
}

object RV64MDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    MULW   -> List(Y, Branch.NONE,  Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_LOW,  N, Y, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
  ).map {
    // not vector instructions
    case (bitpat, enumTypeList) => (bitpat, enumTypeList :+ N)
  }
}

object ZicsrDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    CSRRW  -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.W, N),
    CSRRS  -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.S, N),
    CSRRC  -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.C, N),
    CSRRWI -> List(Y, Branch.NONE,  Value1.UIMM19_15,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.W, N),
    CSRRSI -> List(Y, Branch.NONE,  Value1.UIMM19_15,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.S, N),
    CSRRCI -> List(Y, Branch.NONE,  Value1.UIMM19_15,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.C, N),
  ).map {
    // not vector instructions
    case (bitpat, enumTypeList) => (bitpat, enumTypeList :+ N)
  }
}

object RvvDecode extends DecodeConstants {
  import ContentValid._
  import VectorInstructions._
  val table: Array[(BitPat, List[EnumType])] = Array(
    // Probably use Vector Specified Decoder outside?
    VSETVLI  -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO,   ARITHMETIC_FCN.NONE,      N, N, WB_SEL.VECTOR,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VSETIVLI -> List(Y, Branch.NONE,  Value1.UIMM19_15, Value2.ZERO,  ARITHMETIC_FCN.NONE,  N, N, WB_SEL.VECTOR,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VSETVL   -> List(Y, Branch.NONE,  Value1.RS1,  Value2.RS2,    ARITHMETIC_FCN.NONE,      N, N, WB_SEL.VECTOR,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),

    VLE8     -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VLE16    -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.H, N, CSR_FCN.N, N, Y),
    VLE32    -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.W, N, CSR_FCN.N, N, Y),
    VLE64    -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.D, N, CSR_FCN.N, N, Y),
    VSE8     -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VSE16    -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.H, N, CSR_FCN.N, N, Y),
    VSE32    -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.W, N, CSR_FCN.N, N, Y),
    VSE64    -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.D, N, CSR_FCN.N, N, Y),
    // VLM      -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.B, N, CSR_FCN.N, N, Y),
    // VSM      -> List(Y, Branch.NONE,  Value1.RS1,  Value2.I_IMM,  ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VLSE8    -> List(Y, Branch.NONE,  Value1.RS1,  Value2.RS2,    ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VLSE16   -> List(Y, Branch.NONE,  Value1.RS1,  Value2.RS2,    ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.H, N, CSR_FCN.N, N, Y),
    VLSE32   -> List(Y, Branch.NONE,  Value1.RS1,  Value2.RS2,    ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.W, N, CSR_FCN.N, N, Y),
    VLSE64   -> List(Y, Branch.NONE,  Value1.RS1,  Value2.RS2,    ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.D, N, CSR_FCN.N, N, Y),
    VSSE8    -> List(Y, Branch.NONE,  Value1.RS1,  Value2.RS2,    ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VSSE16   -> List(Y, Branch.NONE,  Value1.RS1,  Value2.RS2,    ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.H, N, CSR_FCN.N, N, Y),
    VSSE32   -> List(Y, Branch.NONE,  Value1.RS1,  Value2.RS2,    ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.W, N, CSR_FCN.N, N, Y),
    VSSE64   -> List(Y, Branch.NONE,  Value1.RS1,  Value2.RS2,    ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.D, N, CSR_FCN.N, N, Y),
    VLOXEI8  -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO,   ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VLOXEI16 -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO,   ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.H, N, CSR_FCN.N, N, Y),
    VLOXEI32 -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO,   ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.W, N, CSR_FCN.N, N, Y),
    VLOXEI64 -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO,   ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_RD,   MEM_LEN.D, N, CSR_FCN.N, N, Y),
    VSOXEI8  -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO,   ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VSOXEI16 -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO,   ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.H, N, CSR_FCN.N, N, Y),
    VSOXEI32 -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO,   ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.W, N, CSR_FCN.N, N, Y),
    VSOXEI64 -> List(Y, Branch.NONE,  Value1.RS1,  Value2.ZERO,   ARITHMETIC_FCN.ADDSUB,    N, N, WB_SEL.NONE,    MEM_FCN.M_WR,   MEM_LEN.D, N, CSR_FCN.N, N, Y),

    VADD_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VADD_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VADD_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VSUB_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VSUB_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VRSUB_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VRSUB_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    /*
    VADC_VVM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VADC_VXM -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VADC_VIM -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMADC_VVM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMADC_VXM -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMADC_VIM -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMADC_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMADC_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMADC_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VSBC_VVM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VSBC_VXM -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSBC_VVM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSBC_VXM -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSBC_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSBC_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
     */
    VAND_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VAND_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VAND_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VOR_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VOR_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VOR_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VXOR_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VXOR_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VXOR_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSEQ_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSEQ_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSEQ_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSNE_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSNE_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSNE_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLTU_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLTU_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLT_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLT_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLEU_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLEU_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLEU_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLE_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLE_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSLE_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSGTU_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSGTU_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSGT_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMSGT_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMINU_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMINU_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMIN_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMIN_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMAXU_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMAXU_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMAX_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMAX_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMERGE_VVM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMERGE_VXM -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMERGE_VIM -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMV_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMV_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMV_VI -> List(Y, Branch.NONE, Value1.UIMM19_15, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMAND_MM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMNAND_MM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMANDN_MM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMXOR_MM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMOR_MM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMNOR_MM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMORN_MM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMXNOR_MM -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMUL_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMUL_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMULH_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMULH_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMULHU_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMULHU_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMULHSU_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMULHSU_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMACC_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMACC_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VNMSAC_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VNMSAC_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMADD_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMADD_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VNMSUB_VV -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VNMSUB_VX -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VREDSUM_VS -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VREDMAXU_VS -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VREDMAX_VS -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VREDMINU_VS -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VREDMIN_VS -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VREDAND_VS -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VREDOR_VS -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VREDXOR_VS -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMV_X_S -> List(Y, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.VECTOR, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
    VMV_S_X -> List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
  )
}

class ID_output(implicit params: HajimeCoreParams) extends Bundle with ScalarOpConstants {
  val branch = UInt(Branch.getWidth.W)
  val value1 = UInt(Value1.getWidth.W)
  val value2 = UInt(Value2.getWidth.W)
  val arithmeticFunct = UInt(ARITHMETIC_FCN.getWidth.W)
  val aluFlag = Bool()
  val op32 = Bool()
  val writeBackSelector = UInt(WB_SEL.getWidth.W)
  val memoryFunction = UInt(MEM_FCN.getWidth.W)
  val memoryLength = UInt(MEM_LEN.getWidth.W)
  val memSExt = Bool()
  val csrFunct = UInt(CSR_FCN.getWidth.W)
  val fence = Bool()
  val vector = if(params.useVector) Some(Bool()) else None
  def toList: List[UInt] = (branch :: value1 :: value2 :: arithmeticFunct :: aluFlag :: op32 ::
    writeBackSelector :: memoryFunction :: memoryLength :: memSExt :: csrFunct :: fence ::
    (if(params.useVector) vector.get :: Nil else Nil))
  def isCondBranch: Bool = Branch.isCondBranch(branch)
  def isJump: Bool = Branch.isJump(branch)
  // ALUへの値でrs1を使うまたはCSRでrs1レジスタを使う
  def useRs1: Bool = Value1.use_RS1(value1)
  // ALUへの値でrs2を使うまたはストア命令
  def useRs2: Bool = Value2.use_RS2(value2) || (memoryFunction === MEM_FCN.M_WR.asUInt)
  def useAlu: Bool = ARITHMETIC_FCN.use_ALU(arithmeticFunct)
  def useMul: Bool = ARITHMETIC_FCN.use_MUL(arithmeticFunct)
  def writeToRd: Bool = WB_SEL.write_to_rd(writeBackSelector)
  def memRead: Bool = (memoryFunction === MEM_FCN.M_RD.asUInt)
  def memWrite: Bool = (memoryFunction === MEM_FCN.M_WR.asUInt)
  def memValid: Bool = memRead || memWrite
  def isSysInst: Bool = fence || (branch === Branch.ECALL.asUInt || branch === Branch.MRET.asUInt)
}

class DecoderIO(implicit params: HajimeCoreParams) extends Bundle {
  val inst = Input(new InstBundle())
  val out = Output(Valid(new ID_output()))
}

class Decoder(implicit params: HajimeCoreParams) extends Module with DecodeConstants {
  import ContentValid._
  val io = IO(new DecoderIO)
  val table: Array[(BitPat, List[EnumType])] = RV32IDecode.table ++
    (if(params.xprlen == 64) RV64IDecode.table else Nil.toArray) ++
    (if(params.useZicsr) ZicsrDecode.table else Nil.toArray) ++
    (if(params.useMulDiv) RV32MDecode.table else Nil.toArray) ++
    (if(params.useMulDiv && (params.xprlen == 64)) RV64MDecode.table else Nil.toArray) ++
    (if(params.useVector) RvvDecode.table else Nil.toArray)
  val tableForListLookup = table.map {
    // V拡張が無ければ最後の要素を抜く
    case (inst, ls) => (inst, (if(params.useVector) ls else ls.init).map(_.asUInt))
  }

  val csignals = {
    ListLookup(io.inst.bits,
      default = (List(N, Branch.NONE,  Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N).map(_.asUInt) ++ (if(params.useVector) List(N.asUInt) else Nil)),
      mapping = tableForListLookup
    )
  }
  io.out.valid := csignals.head.asBool
  for((out, sig) <- (io.out.bits.toList zip csignals.tail)) {
    out := sig
  }
}

object Decoder extends App {
  def apply(implicit params: HajimeCoreParams): Decoder = new Decoder()
  ChiselStage.emitSystemVerilogFile(Decoder(HajimeCoreParams()), firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info"))
}
