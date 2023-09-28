package hajime.vectormodules

import chisel3._

trait VectorOpConstants {
  object AVL_SEL extends ChiselEnum {
    val NONE, RS1, UIMM = Value
  }
  object VTYPE_SEL extends ChiselEnum {
    val NONE, ZIMM10, ZIMM9, RS2 = Value
  }
  object MOP extends ChiselEnum {
    val NONE, UNIT_STRIDE, IDX_UNORDERED, STRIDED, IDX_ORDERED = Value
  }
  object UMOP extends ChiselEnum {
    val NONE, NORMAL, WHOLE_REGISTER, MASK_E8, FAULT_ONLY_FIRST = Value
  }

  object VEU extends ChiselEnum {
    val NONE, ARITHMETIC, LOGICAL, SHIFT = Value
  }

  object VEU_FUN extends ChiselEnum {
    val ADD = Value(0.U)
    val SUB = Value(1.U)
    val RSUB = Value(2.U)
    val SEQ = Value(3.U)
    val SNE = Value(4.U)
    val SLTU = Value(5.U)
    val SLT = Value(6.U)
    val SLEU = Value(7.U)
    val SLE = Value(8.U)
    val SGTU = Value(9.U)
    val SGT = Value(10.U)
    val MINU = Value(11.U)
    val MIN = Value(12.U)
    val MAXU = Value(13.U)
    val MAX = Value(14.U)
    val MERGE = Value(15.U)
    val MV = Value(16.U)

    // val AND = Value(0.U)
    // val OR = Value(1.U)
    // val XOR = Value(2.U)
  }

  object VSOURCE extends ChiselEnum {
    val VV, VX, VI = Value
  }
}
