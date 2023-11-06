package hajime.common

import chisel3._
import chisel3.util._
import Functions._

object RISCV_Consts {
  val INST_LEN: Int = 32
}

trait ScalarOpConstants {
  object ContentValid extends ChiselEnum {
    val N, Y = Value
  }
  object Branch extends ChiselEnum {
    val NONE, EQ, NE, LT, GE, LTU, GEU, JAL, JALR, ECALL, MRET = Value
    val condBranchList = EQ :: NE :: LT :: GE :: LTU :: GEU :: Nil
    val jumpList = JAL :: JALR :: Nil
    def isCondBranch(signal: UInt): Bool = {
      condBranchList.map(_.asUInt).has(signal)
    }
    def isJump(signal: UInt): Bool = {
      jumpList.map(_.asUInt).has(signal)
    }
  }
  object Value1 extends ChiselEnum {
    val ZERO, RS1, U_IMM, UIMM19_15 = Value
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
    val aluList = ADDSUB :: SLL :: SLT :: SLTU :: XOR :: SR :: OR :: AND :: Nil
    val mulList = MUL_LOW :: MUL_HIGH :: MUL_HISU :: MUL_HIU :: Nil
    def use_ALU(signal: UInt): Bool = {
      aluList.map(_.asUInt).has(signal)
    }
    def use_MUL(signal: UInt): Bool = {
      mulList.map(_.asUInt).has(signal)
    }
  }
  object WB_SEL extends ChiselEnum {
    val NONE, PC4, ARITH, CSR, MEM, VECTOR = Value
    def write_to_rd(signal: UInt): Bool = {
      signal =/= WB_SEL.NONE.asUInt
    }
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
    val N, C, S, W, R, I = Value
  }
}

object ImmediateEnum extends ChiselEnum {
  val I, S, B, U, J = Value
}

object COMPILE_CONSTANTS {
  val CHISELSTAGE_ARGS = Array("--emission-options=disableMemRandomization,disableRegisterRandomization")
  val FIRTOOLOPS = Array("-disable-all-randomization", "-strip-debug-info")
}
