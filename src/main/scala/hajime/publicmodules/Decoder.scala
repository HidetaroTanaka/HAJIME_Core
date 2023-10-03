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
    // csr_addr[mepc] = address of ECALL, csr_addr[mcause]=cause, pc=Cat(mtvec.head(xprlen-2), 0.U(2.W))
    ECALL  -> List(Y, Branch.ECALL, Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.I, N),
    // pc=csr_addr[mepc]
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
    VADD_VV  -> List(Y, Branch.NONE,  Value1.ZERO, Value2.ZERO,   ARITHMETIC_FCN.NONE,      N, N, WB_SEL.NONE,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),
  )
}

class ID_output(implicit params: HajimeCoreParams) extends Bundle with ScalarOpConstants {
  val branch = UInt(Branch.getWidth.W)
  val value1 = UInt(Value1.getWidth.W)
  val value2 = UInt(Value2.getWidth.W)
  val arithmetic_funct = UInt(ARITHMETIC_FCN.getWidth.W)
  val alu_flag = Bool()
  val op32 = Bool()
  val writeback_selector = UInt(WB_SEL.getWidth.W)
  val memory_function = UInt(MEM_FCN.getWidth.W)
  val memory_length = UInt(MEM_LEN.getWidth.W)
  val mem_sext = Bool()
  val csr_funct = UInt(CSR_FCN.getWidth.W)
  val fence = Bool()
  val vector = if(params.useVector) Some(Bool()) else None
  def toList: List[UInt] = (branch :: value1 :: value2 :: arithmetic_funct :: alu_flag :: op32 ::
    writeback_selector :: memory_function :: memory_length :: mem_sext :: csr_funct :: fence ::
    (if(params.useVector) vector.get :: Nil else Nil))
  def isCondBranch: Bool = Branch.isCondBranch(branch)
  def isJump: Bool = Branch.isJump(branch)
  // ALUへの値でrs1を使うまたはCSRでrs1レジスタを使う
  def use_RS1: Bool = Value1.use_RS1(value1)
  // ALUへの値でrs2を使うまたはストア命令
  def use_RS2: Bool = Value2.use_RS2(value2) || (memory_function === MEM_FCN.M_WR.asUInt)
  def use_ALU: Bool = ARITHMETIC_FCN.use_ALU(arithmetic_funct)
  def use_MUL: Bool = ARITHMETIC_FCN.use_MUL(arithmetic_funct)
  def write_to_rd: Bool = WB_SEL.write_to_rd(writeback_selector)
  def memRead: Bool = (memory_function === MEM_FCN.M_RD.asUInt)
  def memWrite: Bool = (memory_function === MEM_FCN.M_WR.asUInt)
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
