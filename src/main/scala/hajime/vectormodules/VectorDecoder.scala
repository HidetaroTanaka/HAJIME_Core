package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.VectorInstructions._
import hajime.common._
import hajime.publicmodules._

object VDecode extends DecodeConstants with VectorOpConstants {
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = Array(
    //               Is configuration-setting?          vector addressing modes
    //               |  AVL selector                    |                unit-stride vector addressing modes
    //               |  |             vtype selector    |                |            write back to VRF?
    //               |  |             |                 |                |            |
    VSETVLI  -> List(Y, AVL_SEL.RS1,  VTYPE_SEL.ZIMM10, MOP.NONE,        UMOP.NONE,   N, VEU_FUN.ADD, VSOURCE.VV),
    VSETIVLI -> List(Y, AVL_SEL.UIMM, VTYPE_SEL.ZIMM9,  MOP.NONE,        UMOP.NONE,   N, VEU_FUN.ADD, VSOURCE.VV),
    VSETVL   -> List(Y, AVL_SEL.RS1,  VTYPE_SEL.RS2,    MOP.NONE,        UMOP.NONE,   N, VEU_FUN.ADD, VSOURCE.VV),
    // use i_imm for vm
    VLE8     -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV),
    VLE16    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV),
    VLE32    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV),
    VLE64    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV),
    VSE8     -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV),
    VSE16    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV),
    VSE32    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV),
    VSE64    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV),

    VADD_VV  -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.NONE,        UMOP.NONE,   Y, VEU_FUN.ADD, VSOURCE.VV)
  )
}

object VectorDecoder extends App {
  def apply(implicit params: HajimeCoreParams): VectorDecoder = new VectorDecoder()
  ChiselStage.emitSystemVerilogFile(VectorDecoder(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
class VectorDecoderResp extends Bundle with ScalarOpConstants with VectorOpConstants {
  val isConfsetInst = Bool()
  val avl_sel = UInt(AVL_SEL.getWidth.W)
  val vtype_sel = UInt(VTYPE_SEL.getWidth.W)
  val mop = UInt(MOP.getWidth.W)
  val umop = UInt(UMOP.getWidth.W)
  val vrfWrite = Bool()
  val veuFun = UInt(VEU_FUN.getWidth.W)
  val vSource = UInt(VSOURCE.getWidth.W)
  val vm = Bool()

  def toList: List[UInt] = List(isConfsetInst, avl_sel, vtype_sel, mop, umop, vrfWrite, veuFun, vSource)
}

class VectorDecoderIO(implicit params: HajimeCoreParams) extends Bundle {
  val inst = Input(new InstBundle())
  val out = Output(new VectorDecoderResp)
}

class VectorDecoder(implicit params: HajimeCoreParams) extends Module with DecodeConstants with VectorOpConstants {
  val io = IO(new VectorDecoderIO)
  import ContentValid._
  val table: Array[(BitPat, List[EnumType])] = VDecode.table
  val tableForListLookup = table.map {
    case (inst, ls) => (inst, ls.map(_.asUInt))
  }

  import ContentValid._
  val csignals = ListLookup(io.inst.bits,
    default = List(N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV).map(_.asUInt),
    mapping = tableForListLookup
  )

  for((out, sig) <- io.out.toList zip csignals) {
    out := sig
  }
  io.out.vm := io.inst.bits(25)
}
