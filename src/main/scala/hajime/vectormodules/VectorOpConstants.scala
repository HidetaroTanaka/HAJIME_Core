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

  object VEU_FUN extends ChiselEnum {
    val NONE, ADD, SUB, RSUB, ADC, MADC, SBC, MSBC, SEQ, SNE, SLTU, SLT, SLEU, SLE, SGTU, SGT, MINU, MIN, MAXU, MAX, MERGE, MV, AND, OR, XOR = Value
  }

  object VSOURCE extends ChiselEnum {
    val VV, VX, VI = Value
  }
}
