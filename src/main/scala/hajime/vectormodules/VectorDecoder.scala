package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.VectorInstructions._
import hajime.common._
import hajime.publicmodules._

object VectorDecoder extends App with DecodeConstants with VectorOpConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    //               Is configuration-setting?
    //               |  AVL selector
    //               |  |             vtype selector
    //               |  |             |
    VSETVLI  -> List(Y, AVL_SEL.RS1,  VTYPE_SEL.ZIMM10),
    VSETIVLI -> List(Y, AVL_SEL.UIMM, VTYPE_SEL.ZIMM9),
    VSETVL   -> List(Y, AVL_SEL.RS1,  VTYPE_SEL.RS2),
  )
  def apply(implicit params: HajimeCoreParams): VectorDecoder = new VectorDecoder()
  ChiselStage.emitSystemVerilogFile(VectorDecoder(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
class VectorDecoderResp extends Bundle with ScalarOpConstants with VectorOpConstants {
  val isConfsetInst = Bool()
  val avl_sel = UInt(AVL_SEL.getWidth.W)
  val vtype_sel = UInt(VTYPE_SEL.getWidth.W)

  def toList: List[UInt] = List(isConfsetInst, avl_sel, vtype_sel)
}

class VectorDecoderIO(implicit params: HajimeCoreParams) extends Bundle {
  val inst = Input(new InstBundle())
  val out = Output(new VectorDecoderResp)
}

class VectorDecoder(implicit params: HajimeCoreParams) extends Module with DecodeConstants with VectorOpConstants {
  val io = IO(new VectorDecoderIO)
  val table: Array[(BitPat, List[EnumType])] = VectorDecoder.table
  val tableForListLookup = table.map {
    case (inst, ls) => (inst, ls.map(_.asUInt))
  }

  import ContentValid._
  val csignals = ListLookup(io.inst.bits,
    default = List(N, AVL_SEL.NONE, VTYPE_SEL.NONE).map(_.asUInt),
    mapping = tableForListLookup
  )

  for((out, sig) <- io.out.toList zip csignals) {
    out := sig
  }
}
