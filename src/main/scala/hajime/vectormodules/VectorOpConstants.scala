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
    val UNIT_STRIDE   = Value("b00".U(2.W))
    val IDX_UNORDERED = Value("b01".U(2.W))
    val STRIDED       = Value("b10".U(2.W))
    val IDX_ORDERED   = Value("b11".U(2.W))
  }
  object UMOP extends ChiselEnum {
    val NORMAL            = Value("b00000".U(5.W))
    val WHOLE_REGISTER    = Value("b01000".U(5.W))
    val MASK_E8           = Value("b01011".U(5.W))
    val FAULT_ONLY_FIRST  = Value("b10000".U(5.W))
  }
  object WIDTH extends ChiselEnum {
    val E8  = Value("b000".U(3.W))
    val E16 = Value("b101".U(3.W))
    val E32 = Value("b110".U(3.W))
    val E64 = Value("b111".U(3.W))
  }
}
