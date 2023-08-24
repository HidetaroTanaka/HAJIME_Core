package hajime.vectormodules

import chisel3._

trait VectorOpConstants {
  object AVL_SEL extends ChiselEnum {
    val NONE, RS1, UIMM = Value
  }
  object VTYPE_SEL extends ChiselEnum {
    val NONE, ZIMM10, ZIMM9, RS2 = Value
  }
}
