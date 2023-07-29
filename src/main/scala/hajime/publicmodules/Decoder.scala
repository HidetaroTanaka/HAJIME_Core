package hajime.publicmodules

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common.CACHE_FUNCTIONS._
import hajime.common.Instructions._
import hajime.common._

abstract trait DecodeConstants extends ScalarOpConstants {
  val table: Array[(BitPat, List[EnumType])]
}

object RV32IDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    //      　     valid           ALU_in1       ALU_in2       Arithmetic_Function     ALU_flag            Memory_Function                          fence)
    //             |  Branch       |             |             |                       |  op32             |               Memory_Length            |
    //             |  |            |             |             |                       |  |  WB_selector   |               |          Mem_SEXT      |
    // R-type      |  |            |             |             |                       |  |  |             |               |          |  CSR_Func   |
    ADD    -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SUB    -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.ADDSUB,  Y, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLL    -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLL,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLT    -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLT,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLTU   -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLTU,    N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    XOR    -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.XOR,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRL    -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SR,      N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRA    -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SR,      Y, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    OR     -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.OR,      N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    AND    -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.AND,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // I-type
    ADDI   -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLTI   -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SLT,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLTIU  -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SLTU,    N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    XORI   -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.XOR,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    ORI    -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.OR,      N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    ANDI   -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.AND,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLLI   -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SLL,     N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRLI   -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SR,      N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRAI   -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SR,      Y, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    LB     -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.B, Y, CSR_FCN.N, N),
    LH     -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.H, Y, CSR_FCN.N, N),
    LW     -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.W, Y, CSR_FCN.N, N),
    LBU    -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.B, N, CSR_FCN.N, N),
    LHU    -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.H, N, CSR_FCN.N, N),
    JALR   -> List(Y, Branch.JALR, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.PC4,   MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // S-type
    SB     -> List(Y, Branch.NONE, Value1.RS1,   Value2.S_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.B, N, CSR_FCN.N, N),
    SH     -> List(Y, Branch.NONE, Value1.RS1,   Value2.S_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.H, N, CSR_FCN.N, N),
    SW     -> List(Y, Branch.NONE, Value1.RS1,   Value2.S_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.W, N, CSR_FCN.N, N),

    // B-type
    BEQ    -> List(Y, Branch.EQ,   Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.XOR,     N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BNE    -> List(Y, Branch.NE,   Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.XOR,     N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BLT    -> List(Y, Branch.LT,   Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLT,     N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BGE    -> List(Y, Branch.GE,   Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLT,     N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BLTU   -> List(Y, Branch.LTU,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLTU,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    BGEU   -> List(Y, Branch.GEU,  Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLTU,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // U-type
    LUI    -> List(Y, Branch.NONE, Value1.U_IMM, Value2.ZERO,  ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    AUIPC  -> List(Y, Branch.NONE, Value1.U_IMM, Value2.PC,    ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // J-type
    JAL    -> List(Y, Branch.JAL,  Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.PC4,   MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // SYSTEM
    FENCE  -> List(Y, Branch.NONE, Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, Y),
    ECALL  -> List(Y, Branch.NONE, Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.I, N),
    // ebreak is unimplemented
    EBREAK -> List(Y, Branch.NONE, Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
  )
}

object RV64IDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    // R-type
    ADDW   -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.ADDSUB,  N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SUBW   -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.ADDSUB,  Y, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLLW   -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SLL,     N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRLW   -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SR,      N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRAW   -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,   ARITHMETIC_FCN.SR,      Y, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),

    // I-type
    ADDIW  -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SLLIW  -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SLL,     N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRLIW  -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SR,      N, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    SRAIW  -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.SR,      Y, Y, WB_SEL.ARITH, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    LD     -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.D, Y, CSR_FCN.N, N),
    LWU    -> List(Y, Branch.NONE, Value1.RS1,   Value2.I_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.MEM,   MEM_FCN.M_RD,   MEM_LEN.W, N, CSR_FCN.N, N),

    // S-type
    SD     -> List(Y, Branch.NONE, Value1.RS1,   Value2.S_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.D, N, CSR_FCN.N, N),
  )
}

object RV32MDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    MUL    -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_LOW,  N, N, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    MULH   -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_HIGH, N, N, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    MULHSU -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_HISU, N, N, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
    MULHU  -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_HIU,  N, N, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
  )
}

object RV64MDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    MULW   -> List(Y, Branch.NONE, Value1.RS1,   Value2.RS2,  ARITHMETIC_FCN.MUL_LOW,  N, Y, WB_SEL.ARITH,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N),
  )
}

object ZicsrDecode extends DecodeConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    CSRRW  -> List(Y, Branch.NONE, Value1.RS1,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.W, N),
    CSRRS  -> List(Y, Branch.NONE, Value1.RS1,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.S, N),
    CSRRC  -> List(Y, Branch.NONE, Value1.RS1,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.C, N),
    CSRRWI -> List(Y, Branch.NONE, Value1.CSR,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.W, N),
    CSRRSI -> List(Y, Branch.NONE, Value1.CSR,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.S, N),
    CSRRCI -> List(Y, Branch.NONE, Value1.CSR,  Value2.ZERO, ARITHMETIC_FCN.NONE,     N, N, WB_SEL.CSR,    MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.C, N),
  )
}

class ID_output extends Bundle with ScalarOpConstants {
  val valid = Bool()
  val branch = UInt()
  val value1 = UInt()
  val value2 = UInt()
  val arithmetic_funct = UInt()
  val alu_flag = Bool()
  val op32 = Bool()
  val writeback_selector = UInt()
  val memory_function = UInt()
  val memory_length = UInt()
  val mem_sext = UInt()
  val csr_funct = UInt()
  val fence = Bool()
  def toList: List[UInt] = (valid :: branch :: value1 :: value2 :: arithmetic_funct :: alu_flag :: op32 ::
    writeback_selector :: memory_function :: memory_length :: mem_sext :: csr_funct :: fence :: Nil)
}

class DecoderIO extends Bundle {
  val inst = Input(Valid(UInt(RISCV_Consts.INST_LEN.W)))
  val out = new ID_output
}

class Decoder(implicit params: HajimeCoreParams) extends Module with DecodeConstants {
  import ContentValid._
  val io = IO(new DecoderIO)
  val table: Array[(BitPat, List[EnumType])] = RV32IDecode.table ++
    (if(params.xprlen == 64) RV64IDecode.table else Nil.toArray) ++
    (if(params.useZicsr) ZicsrDecode.table else Nil.toArray) ++
    (if(params.useMulDiv) RV32MDecode.table else Nil.toArray) ++
    (if(params.useMulDiv && (params.xprlen == 64)) RV64MDecode.table else Nil.toArray)
  val tableForListLookup = table.map{
    case (inst, ls) => (inst, ls.map(_.asUInt))
  }

  val csignals = {
    ListLookup(io.inst.bits,
      default = List(N, Branch.NONE, Value1.ZERO,  Value2.ZERO,  ARITHMETIC_FCN.NONE,    N, N, WB_SEL.NONE,  MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N).map(_.asUInt),
      mapping = tableForListLookup
    )
  }
  for((out, sig) <- (io.out.toList zip csignals)) {
    out := sig
  }
}

object Decoder extends App {
  def apply(implicit params: HajimeCoreParams): Decoder = new Decoder()
  ChiselStage.emitSystemVerilogFile(Decoder(HajimeCoreParams()), firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info"))
}
