package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.VectorInstructions._
import hajime.common._
import hajime.publicmodules._

object VDecode extends DecodeConstants with VectorOpConstants {
  import ContentValid._

  /**
   * wtf is this sussy method name
   * @param vInst
   * @param vSource
   * @return
   */
  private def amogus(vInst: String, vSource: VSOURCE.Type): List[EnumType] = {
    import VEU_FUN._
    val veuFunSel = vInst match {
      case "vadd" => ADD
      case "vsub" => SUB
      case "vrsub" => RSUB
      case "vadc" => ADC
      case "vmadc" => MADC
      case "vsbc" => SBC
      case "vmsbc" => MSBC
      case "vand" => AND
      case "vor" => OR
      case "vxor" => XOR
      case "vmseq" => SEQ
      case "vmsne" => SNE
      case "vmsltu" => SLTU
      case "vmslt" => SLT
      case "vmsleu" => SLEU
      case "vmsle" => SLE
      case "vmsgtu" => SGTU
      case "vmsgt" => SGT
      case "vminu" => MINU
      case "vmin" => MIN
      case "vmaxu" => MAXU
      case "vmax" => MAX
      case "vmerge" => MERGE
      case "vmv" => MV
      case _ => throw new Exception("fuck")
    }
    List(Y, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.NONE, UMOP.NORMAL, Y, veuFunSel, vSource)
  }

  val table: Array[(BitPat, List[EnumType])] = Array(
    //               Is configuration-setting?          vector addressing modes
    //               |  AVL selector                    |                unit-stride vector addressing modes
    //               |  |             vtype selector    |                |             write back to VRF?
    //               |  |             |                 |                |             |
    VSETVLI  -> List(Y, AVL_SEL.RS1,  VTYPE_SEL.ZIMM10, MOP.NONE,        UMOP.NONE,    N, VEU_FUN.ADD, VSOURCE.VV),
    VSETIVLI -> List(Y, AVL_SEL.UIMM, VTYPE_SEL.ZIMM9,  MOP.NONE,        UMOP.NONE,    N, VEU_FUN.ADD, VSOURCE.VV),
    VSETVL   -> List(Y, AVL_SEL.RS1,  VTYPE_SEL.RS2,    MOP.NONE,        UMOP.NONE,    N, VEU_FUN.ADD, VSOURCE.VV),
    // use i_imm for vm
    VLE8     -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VLE16    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VLE32    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VLE64    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VSE8     -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    VSE16    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    VSE32    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    VSE64    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    // i have to change vl for these insts
    // VLM      -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.MASK_E8, Y, VEU_FUN.ADD, VSOURCE.VV),
    // VSM      -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.UNIT_STRIDE, UMOP.MASK_E8, Y, VEU_FUN.ADD, VSOURCE.VV),
    VLSE8    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.STRIDED,     UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VLSE16   -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.STRIDED,     UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VLSE32   -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.STRIDED,     UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VLSE64   -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.STRIDED,     UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VSSE8    -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.STRIDED,     UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    VSSE16   -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.STRIDED,     UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    VSSE32   -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.STRIDED,     UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    VSSE64   -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.STRIDED,     UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    // change to VS1USE, VS2USE
    VLOXEI8  -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.IDX_ORDERED, UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VLOXEI16 -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.IDX_ORDERED, UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VLOXEI32 -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.IDX_ORDERED, UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VLOXEI64 -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.IDX_ORDERED, UMOP.NORMAL,  Y, VEU_FUN.ADD, VSOURCE.VV),
    VSOXEI8  -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.IDX_ORDERED, UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    VSOXEI16 -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.IDX_ORDERED, UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    VSOXEI32 -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.IDX_ORDERED, UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),
    VSOXEI64 -> List(N, AVL_SEL.NONE, VTYPE_SEL.NONE,   MOP.IDX_ORDERED, UMOP.NORMAL,  N, VEU_FUN.ADD, VSOURCE.VV),

    VADD_VV  -> amogus("vadd", VSOURCE.VV),
    VADD_VX  -> amogus("vadd", VSOURCE.VX),
    VADD_VI  -> amogus("vadd", VSOURCE.VI),
    VSUB_VV  -> amogus("vsub", VSOURCE.VV),
    VSUB_VX  -> amogus("vsub", VSOURCE.VX),
    VRSUB_VX -> amogus("vrsub", VSOURCE.VX),
    VRSUB_VI -> amogus("vrsub", VSOURCE.VI),
    VADC_VVM -> amogus("vadc", VSOURCE.VV),
    VADC_VXM -> amogus("vadc", VSOURCE.VX),
    VADC_VIM -> amogus("vadc", VSOURCE.VI),
    VMADC_VVM -> amogus("vmadc", VSOURCE.VV),
    VMADC_VXM -> amogus("vmadc", VSOURCE.VX),
    VMADC_VIM -> amogus("vmadc", VSOURCE.VI),
    VMADC_VV -> amogus("vmadc", VSOURCE.VV),
    VMADC_VX -> amogus("vmadc", VSOURCE.VX),
    VMADC_VI -> amogus("vmadc", VSOURCE.VI),
    VSBC_VVM -> amogus("vsbc", VSOURCE.VV),
    VSBC_VXM -> amogus("vsbc", VSOURCE.VX),
    VMSBC_VVM -> amogus("vmsbc", VSOURCE.VV),
    VMSBC_VXM -> amogus("vmsbc", VSOURCE.VX),
    VMSBC_VV -> amogus("vmsbc", VSOURCE.VV),
    VMSBC_VX -> amogus("vmsbc", VSOURCE.VX),
    VAND_VV -> amogus("vand", VSOURCE.VV),
    VAND_VX -> amogus("vand", VSOURCE.VX),
    VAND_VI -> amogus("vand", VSOURCE.VI),
    VOR_VV -> amogus("vor", VSOURCE.VV),
    VOR_VX -> amogus("vor", VSOURCE.VX),
    VOR_VI -> amogus("vor", VSOURCE.VI),
    VXOR_VV -> amogus("vxor", VSOURCE.VV),
    VXOR_VX -> amogus("vxor", VSOURCE.VX),
    VXOR_VI -> amogus("vxor", VSOURCE.VI),
    VMSEQ_VV -> amogus("vmseq", VSOURCE.VV),
    VMSEQ_VX -> amogus("vmseq", VSOURCE.VX),
    VMSEQ_VI -> amogus("vmseq", VSOURCE.VI),
    VMSNE_VV -> amogus("vmsne", VSOURCE.VV),
    VMSNE_VX -> amogus("vmsne", VSOURCE.VX),
    VMSNE_VI -> amogus("vmsne", VSOURCE.VI),
    VMSLTU_VV -> amogus("vmsltu", VSOURCE.VV),
    VMSLTU_VX -> amogus("vmsltu", VSOURCE.VX),
    VMSLT_VV -> amogus("vmslt", VSOURCE.VV),
    VMSLT_VX -> amogus("vmslt", VSOURCE.VX),
    VMSLEU_VV -> amogus("vmsleu", VSOURCE.VV),
    VMSLEU_VX -> amogus("vmsleu", VSOURCE.VX),
    VMSLEU_VI -> amogus("vmsleu", VSOURCE.VI),
    VMSLE_VV -> amogus("vmsle", VSOURCE.VV),
    VMSLE_VX -> amogus("vmsle", VSOURCE.VX),
    VMSLE_VI -> amogus("vmsle", VSOURCE.VI),
    VMSGTU_VX -> amogus("vmsgtu", VSOURCE.VX),
    VMSGTU_VI -> amogus("vmsgtu", VSOURCE.VI),
    VMSGT_VX -> amogus("vmsgt", VSOURCE.VX),
    VMSGT_VI -> amogus("vmsgt", VSOURCE.VI),
    VMINU_VV -> amogus("vminu", VSOURCE.VV),
    VMINU_VX -> amogus("vminu", VSOURCE.VX),
    VMIN_VV -> amogus("vmin", VSOURCE.VV),
    VMIN_VX -> amogus("vmin", VSOURCE.VX),
    VMAXU_VV -> amogus("vmaxu", VSOURCE.VV),
    VMAXU_VX -> amogus("vmaxu", VSOURCE.VX),
    VMAX_VV -> amogus("vmax", VSOURCE.VV),
    VMAX_VX -> amogus("vmax", VSOURCE.VX),
    VMERGE_VVM -> amogus("vmerge", VSOURCE.VV),
    VMERGE_VXM -> amogus("vmerge", VSOURCE.VX),
    VMERGE_VIM -> amogus("vmerge", VSOURCE.VI),
    VMV_VV -> amogus("vmv", VSOURCE.VV),
    VMV_VX -> amogus("vmv", VSOURCE.VX),
    VMV_VI -> amogus("vmv", VSOURCE.VI),
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
  def useVecAluExec: Bool = !isConfsetInst && (mop === MOP.NONE.asUInt)
  def useVecLdstExec: Bool = !isConfsetInst && (mop =/= MOP.NONE.asUInt)
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
    default = List(N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.NONE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV).map(_.asUInt),
    mapping = tableForListLookup
  )

  for((out, sig) <- io.out.toList zip csignals) {
    out := sig
  }
  io.out.vm := io.inst.bits(25)
}
