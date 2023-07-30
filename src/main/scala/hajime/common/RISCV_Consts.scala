package hajime.common

import chisel3._
import chisel3.util._

object RISCV_Consts {
  val XLEN: Int = 64
  val INST_LEN: Int = 32
  val RF_INDEX_WIDTH: Int = 5
}

trait ScalarOpConstants {
  object ContentValid extends ChiselEnum {
    val Y, N = Value
  }
  object Branch extends ChiselEnum {
    val NONE, EQ, NE, LT, GE, LTU, GEU, JAL, JALR = Value
    val condBranchList = EQ :: NE :: LT :: GE :: LTU :: GEU :: Nil
    val jumpList = JAL :: JALR :: Nil
    def isCondBranch(signal: UInt): Bool = {
      condBranchList.map(x => signal === x.asUInt).reduce(_ || _)
    }
    def isJump(signal: UInt): Bool = {
      jumpList.map(x => signal === x.asUInt).reduce(_ || _)
    }
  }
  object Value1 extends ChiselEnum {
    val ZERO, RS1, U_IMM, CSR = Value
    def use_RS1(signal: UInt): Bool = {
      signal === RS1.asUInt
    }
  }
  // value to ALU in2
  object Value2 extends ChiselEnum {
    val ZERO, RS2, I_IMM, S_IMM, PC = Value
    def use_RS2(signal: UInt): Bool = {
      signal === RS2.asUInt
    }
  }
  object ARITHMETIC_FCN extends ChiselEnum {
    val NONE, ADDSUB, SLL, SLT, SLTU, XOR, SR, OR, AND, MUL_LOW, MUL_HIGH, MUL_HISU, MUL_HIU = Value
  }
  object WB_SEL extends ChiselEnum {
    val NONE, PC4, ARITH, CSR, MEM = Value
  }
  object MEM_FCN extends ChiselEnum {
    val M_NONE, M_RD, M_WR = Value
    def valid(signal: MEM_FCN.Type): Bool = {
      signal =/= M_NONE
    }
  }
  object MEM_LEN extends ChiselEnum {
    val B, H, W, D = Value
  }
  object CSR_FCN extends ChiselEnum {
    val N, C, S, W, I = Value
  }
}

object Deprecated_ScalarOpConstants {
  //************************************
  // Control Signals

  val Y = true.B
  val N = false.B

  // ALU_in1, ALU_in2 selector
  def ALUin_X = 0.U(2.W)
  def ALUin_RS1_RS2 = ALUin_X
  def ALUin_RS1_IMI = ALUin_RS1_RS2+1.U
  def ALUin_RS1_IMS = ALUin_RS1_IMI+1.U
  def ALUin_PC_IMMU = ALUin_RS1_IMS+1.U
  // def ALUin_RS1_CSR = ALUin_PC_IMMU+1.U

  def ALUin_USE_RS1(aluin_signal: UInt): Bool = {
    val matchList = Seq(ALUin_RS1_RS2, ALUin_RS1_IMI, ALUin_RS1_IMS)
    matchList.map(x => (aluin_signal === x)).reduce(_ || _)
  }
  def ALUin_USE_RS2(aluin_signal: UInt): Bool = {
    aluin_signal === ALUin_RS1_RS2
  }

  // ALU_func, same as funct3
  def ALU_X      = "b000".U(3.W)
  def ALU_ADDSUB = ALU_X
  def ALU_SLL    = ALU_ADDSUB+1.U
  def ALU_SLT    = ALU_SLL+1.U
  def ALU_SLTU   = ALU_SLT+1.U
  def ALU_XOR    = ALU_SLTU+1.U
  // SR!?!?!?!?!? Socialist-Revolutionaries reference????
  def ALU_SR     = ALU_XOR+1.U
  def ALU_OR     = ALU_SR+1.U
  def ALU_AND    = ALU_OR+1.U

  // NOALU selector
  def NOALU_X    = 0.U(3.W)
  def NOALU_PC4  = NOALU_X
  def NOALU_RS2  = NOALU_PC4+1.U
  def NOALU_IMMU = NOALU_RS2+1.U
  def NOALU_PC_IF_MISPREDICT = NOALU_IMMU+1.U
  def NOALU_CSR = NOALU_PC_IF_MISPREDICT+1.U

  def NOALU_USE_RS2(noalu_signal: UInt): Bool = {
    noalu_signal === NOALU_RS2
  }

  // RF writebacc selector
  def WB_X      = 0.U(3.W)
  def WB_ALU    = WB_X+1.U
  def WB_MEM    = WB_ALU+1.U
  def WB_NOALU  = WB_MEM+1.U
  def WB_CSR    = WB_NOALU+1.U

  // PCWrite selector (PC Update type)
  def PCWB_X      = 0.U(2.W)  // don't write pc
  def PCWB_JALR   = PCWB_X+1.U // write correct pc if jalr prediction is incorrect
  def PCWB_BRANCH = PCWB_JALR+1.U // write correct pc if branch prediction is incorrect
  def PCWB_JAL    = PCWB_BRANCH+1.U // already written in Decode

  // Branch
  def BR_N   = 0.U(3.W)
  def BR_EQ  = BR_N
  def BR_NE  = BR_EQ+1.U
  def BR_LT  = 4.U(3.W)
  def BR_GE  = BR_LT+1.U
  def BR_LTU = BR_GE+1.U
  def BR_GEU = BR_LTU+1.U

  def CSR_NONE = 0.U(2.W)
  /**
   * csrrc, csrrci: Read and Clear CSR according to rs1 or zimm
   *
   * CSR = CSR & ~{rs1, zimm}
   */
  def CSR_CLEAR = CSR_NONE+1.U

  /**
   * csrrs, scrrsi: Read and Set CSR according to rs1 or zimm
   *
   * CSR = CSR | {rs1, zimm}
   */
  def CSR_SET = CSR_CLEAR+1.U

  /**
   * csrrw, csrrwi: Read and Write CSR according to rs1 or zimm
   *
   * CSR = {rs1, zimm}
   */
  def CSR_WRITE = CSR_SET+1.U
}

object CORE_Consts {
  val THREADS: Int = 2
  val debug: Boolean = true
}

object COMPILE_CONSTANTS {
  val CHISELSTAGE_ARGS = Array("--emission-options=disableMemRandomization,disableRegisterRandomization")
  val FIRTOOLOPS = Array("-disable-all-randomization", "-strip-debug-info")
}
