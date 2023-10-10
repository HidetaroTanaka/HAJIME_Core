package hajime.vectormodules

import chisel3._
import hajime.common.Functions._

trait VectorOpConstants {
  object AVL_SEL extends ChiselEnum {
    val NONE, RS1, UIMM = Value
  }
  object VTYPE_SEL extends ChiselEnum {
    val NONE, ZIMM10, ZIMM9, RS2 = Value
  }
  /**
   * Vector Memory Op
   */
  object MOP extends ChiselEnum {
    val NONE, UNIT_STRIDE, IDX_UNORDERED, STRIDED, IDX_ORDERED = Value
  }

  /**
   * Vector Unit-stride Memory Op
   */
  object UMOP extends ChiselEnum {
    val NONE, NORMAL, WHOLE_REGISTER, MASK_E8, FAULT_ONLY_FIRST = Value
  }

  object VEU_FUN extends ChiselEnum {
    val NONE, ADD, SUB, RSUB, ADC, MADC, SBC, MSBC, SEQ, SNE, SLTU, SLT, SLEU, SLE, SGTU, SGT, MINU, MIN, MAXU, MAX, MERGE, MV, AND, OR, XOR, MAND, MNAND, MANDN, MXOR, MOR, MNOR, MORN, MXNOR = Value
    val compMaskList = SEQ :: SNE :: SLTU :: SLT :: SLEU :: SLE :: SGTU :: SGT :: Nil
    val carryMaskList = MADC :: MSBC :: Nil
    val ignoreMaskList = ADC :: MADC :: SBC :: MSBC :: MERGE :: Nil
    val maskInstList = MAND :: MNAND :: MANDN :: MXOR :: MOR :: MNOR :: MORN :: MXNOR :: Nil

    implicit class masks(signal: UInt) {
      def isCompMask: Bool = {
        compMaskList.map(_.asUInt).has(signal)
      }
      def isCarryMask: Bool = {
        carryMaskList.map(_.asUInt).has(signal)
      }
      def isArithmeticMask: Bool = {
        signal.isCompMask || signal.isCarryMask
      }
      def ignoreMask: Bool = {
        ignoreMaskList.map(_.asUInt).has(signal)
      }
      def isMaskInst: Bool = {
        maskInstList.map(_.asUInt).has(signal)
      }
      def writeAsMask: Bool = {
        signal.isArithmeticMask || signal.isMaskInst
      }
    }
  }

  object VSOURCE extends ChiselEnum {
    val VV, VX, VI = Value
  }
}
