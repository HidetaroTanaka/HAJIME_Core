package hajime.common

import chisel3._
import chisel3.util._
import hajime.publicmodules.DecodeConstants
import hajime.vectormodules.VectorOpConstants

import scala.annotation.unused

// copied from sodor
object Instructions {
  def BEQ                = BitPat("b?????????????????000?????1100011")
  def BNE                = BitPat("b?????????????????001?????1100011")
  def BLT                = BitPat("b?????????????????100?????1100011")
  def BGE                = BitPat("b?????????????????101?????1100011")
  def BLTU               = BitPat("b?????????????????110?????1100011")
  def BGEU               = BitPat("b?????????????????111?????1100011")
  def JALR               = BitPat("b?????????????????000?????1100111")
  def JAL                = BitPat("b?????????????????????????1101111")
  def LUI                = BitPat("b?????????????????????????0110111")
  def AUIPC              = BitPat("b?????????????????????????0010111")
  def ADDI               = BitPat("b?????????????????000?????0010011")
  def SLLI               = BitPat("b000000???????????001?????0010011")
  def SLTI               = BitPat("b?????????????????010?????0010011")
  def SLTIU              = BitPat("b?????????????????011?????0010011")
  def XORI               = BitPat("b?????????????????100?????0010011")
  def SRLI               = BitPat("b000000???????????101?????0010011")
  def SRAI               = BitPat("b010000???????????101?????0010011")
  def ORI                = BitPat("b?????????????????110?????0010011")
  def ANDI               = BitPat("b?????????????????111?????0010011")
  def ADD                = BitPat("b0000000??????????000?????0110011")
  def SUB                = BitPat("b0100000??????????000?????0110011")
  def SLL                = BitPat("b0000000??????????001?????0110011")
  def SLT                = BitPat("b0000000??????????010?????0110011")
  def SLTU               = BitPat("b0000000??????????011?????0110011")
  def XOR                = BitPat("b0000000??????????100?????0110011")
  def SRL                = BitPat("b0000000??????????101?????0110011")
  def SRA                = BitPat("b0100000??????????101?????0110011")
  def OR                 = BitPat("b0000000??????????110?????0110011")
  def AND                = BitPat("b0000000??????????111?????0110011")
  def ADDIW              = BitPat("b?????????????????000?????0011011")
  def SLLIW              = BitPat("b0000000??????????001?????0011011")
  def SRLIW              = BitPat("b0000000??????????101?????0011011")
  def SRAIW              = BitPat("b0100000??????????101?????0011011")
  def ADDW               = BitPat("b0000000??????????000?????0111011")
  def SUBW               = BitPat("b0100000??????????000?????0111011")
  def SLLW               = BitPat("b0000000??????????001?????0111011")
  def SRLW               = BitPat("b0000000??????????101?????0111011")
  def SRAW               = BitPat("b0100000??????????101?????0111011")
  def LB                 = BitPat("b?????????????????000?????0000011")
  def LH                 = BitPat("b?????????????????001?????0000011")
  def LW                 = BitPat("b?????????????????010?????0000011")
  def LD                 = BitPat("b?????????????????011?????0000011")
  def LBU                = BitPat("b?????????????????100?????0000011")
  def LHU                = BitPat("b?????????????????101?????0000011")
  def LWU                = BitPat("b?????????????????110?????0000011")
  def SB                 = BitPat("b?????????????????000?????0100011")
  def SH                 = BitPat("b?????????????????001?????0100011")
  def SW                 = BitPat("b?????????????????010?????0100011")
  def SD                 = BitPat("b?????????????????011?????0100011")
  def FENCE              = BitPat("b?????????????????000?????0001111")
  def FENCE_I            = BitPat("b?????????????????001?????0001111")
  def MUL                = BitPat("b0000001??????????000?????0110011")
  def MULH               = BitPat("b0000001??????????001?????0110011")
  def MULHSU             = BitPat("b0000001??????????010?????0110011")
  def MULHU              = BitPat("b0000001??????????011?????0110011")
  def DIV                = BitPat("b0000001??????????100?????0110011")
  def DIVU               = BitPat("b0000001??????????101?????0110011")
  def REM                = BitPat("b0000001??????????110?????0110011")
  def REMU               = BitPat("b0000001??????????111?????0110011")
  def MULW               = BitPat("b0000001??????????000?????0111011")
  def DIVW               = BitPat("b0000001??????????100?????0111011")
  def DIVUW              = BitPat("b0000001??????????101?????0111011")
  def REMW               = BitPat("b0000001??????????110?????0111011")
  def REMUW              = BitPat("b0000001??????????111?????0111011")
  def LR_W               = BitPat("b00010??00000?????010?????0101111")
  def SC_W               = BitPat("b00011????????????010?????0101111")
  def LR_D               = BitPat("b00010??00000?????011?????0101111")
  def SC_D               = BitPat("b00011????????????011?????0101111")
  def ECALL              = BitPat("b00000000000000000000000001110011")
  def EBREAK             = BitPat("b00000000000100000000000001110011")
  def URET               = BitPat("b00000000001000000000000001110011")
  def MRET               = BitPat("b00110000001000000000000001110011")
  def DRET               = BitPat("b01111011001000000000000001110011")
  def SFENCE_VMA         = BitPat("b0001001??????????000000001110011")
  def WFI                = BitPat("b00010000010100000000000001110011")
  def CSRRW              = BitPat("b?????????????????001?????1110011")
  def CSRRS              = BitPat("b?????????????????010?????1110011")
  def CSRRC              = BitPat("b?????????????????011?????1110011")
  def CSRRWI             = BitPat("b?????????????????101?????1110011")
  def CSRRSI             = BitPat("b?????????????????110?????1110011")
  def CSRRCI             = BitPat("b?????????????????111?????1110011")
}

object VectorInstructions extends ScalarOpConstants with VectorOpConstants {
  def VSETVLI            = BitPat("b0????????????????111?????1010111")
  def VSETIVLI           = BitPat("b11???????????????111?????1010111")
  def VSETVL             = BitPat("b1000000??????????111?????1010111")

  def generateBitPatForVecLDST(mop: MOP.Type, umop: UMOP.Type, width: MEM_LEN.Type, load: Boolean): BitPat = {
    val nf = "000" // don't have to care nf as LMUL is always 1
    val mew = "0" // mew with 1 is reserved
    val _mop = mop match {
      case MOP.UNIT_STRIDE => "00"
      case MOP.IDX_UNORDERED => "01"
      case MOP.STRIDED => "10"
      case MOP.IDX_ORDERED => "11"
      case _ => throw new Exception()
    }
    val vm = "?"
    val _umop = if(mop == MOP.UNIT_STRIDE) umop match {
      case UMOP.NORMAL => "00000"
      case UMOP.WHOLE_REGISTER => "01000"
      case UMOP.MASK_E8 => "01011"
      case UMOP.FAULT_ONLY_FIRST => "10000"
      case _ => throw new Exception()
    } else "?????"
    val _width = width match {
      case MEM_LEN.B => "000"
      case MEM_LEN.H => "101"
      case MEM_LEN.W => "110"
      case MEM_LEN.D => "111"
    }
    val opcode = if(load) "0000111" else "0100111"
    BitPat("b" + nf + mew + _mop + vm + _umop + "?????" + _width + "?????" + opcode)
  }
  def VLE8               = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.NORMAL, MEM_LEN.B, load = true)
  def VLE16              = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.NORMAL, MEM_LEN.H, load = true)
  def VLE32              = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.NORMAL, MEM_LEN.W, load = true)
  def VLE64              = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.NORMAL, MEM_LEN.D, load = true)
  def VSE8               = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.NORMAL, MEM_LEN.B, load = false)
  def VSE16              = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.NORMAL, MEM_LEN.H, load = false)
  def VSE32              = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.NORMAL, MEM_LEN.W, load = false)
  def VSE64              = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.NORMAL, MEM_LEN.D, load = false)
  def VLM                = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.MASK_E8, MEM_LEN.B, load = true)
  def VSM                = generateBitPatForVecLDST(MOP.UNIT_STRIDE, UMOP.MASK_E8, MEM_LEN.B, load = false)
  def VLSE8              = generateBitPatForVecLDST(MOP.STRIDED,     UMOP.NORMAL,  MEM_LEN.B, load = true)
  def VLSE16             = generateBitPatForVecLDST(MOP.STRIDED,     UMOP.NORMAL,  MEM_LEN.H, load = true)
  def VLSE32             = generateBitPatForVecLDST(MOP.STRIDED,     UMOP.NORMAL,  MEM_LEN.W, load = true)
  def VLSE64             = generateBitPatForVecLDST(MOP.STRIDED,     UMOP.NORMAL,  MEM_LEN.D, load = true)
  def VSSE8              = generateBitPatForVecLDST(MOP.STRIDED,     UMOP.NORMAL,  MEM_LEN.B, load = false)
  def VSSE16             = generateBitPatForVecLDST(MOP.STRIDED,     UMOP.NORMAL,  MEM_LEN.H, load = false)
  def VSSE32             = generateBitPatForVecLDST(MOP.STRIDED,     UMOP.NORMAL,  MEM_LEN.W, load = false)
  def VSSE64             = generateBitPatForVecLDST(MOP.STRIDED,     UMOP.NORMAL,  MEM_LEN.D, load = false)
  def VLOXEI8            = generateBitPatForVecLDST(MOP.IDX_ORDERED, UMOP.NORMAL,  MEM_LEN.B, load = true)
  def VLOXEI16           = generateBitPatForVecLDST(MOP.IDX_ORDERED, UMOP.NORMAL,  MEM_LEN.H, load = true)
  def VLOXEI32           = generateBitPatForVecLDST(MOP.IDX_ORDERED, UMOP.NORMAL,  MEM_LEN.W, load = true)
  def VLOXEI64           = generateBitPatForVecLDST(MOP.IDX_ORDERED, UMOP.NORMAL,  MEM_LEN.D, load = true)
  def VSOXEI8            = generateBitPatForVecLDST(MOP.IDX_ORDERED, UMOP.NORMAL,  MEM_LEN.B, load = false)
  def VSOXEI16           = generateBitPatForVecLDST(MOP.IDX_ORDERED, UMOP.NORMAL,  MEM_LEN.H, load = false)
  def VSOXEI32           = generateBitPatForVecLDST(MOP.IDX_ORDERED, UMOP.NORMAL,  MEM_LEN.W, load = false)
  def VSOXEI64           = generateBitPatForVecLDST(MOP.IDX_ORDERED, UMOP.NORMAL,  MEM_LEN.D, load = false)

  def vsourceGen(vsource: VSOURCE.Type): String = {
    vsource match {
      case VSOURCE.VV => "000"
      case VSOURCE.VX => "100"
      case VSOURCE.VI => "011"
      case VSOURCE.MVV => "010"
      case VSOURCE.MVX => "110"
      case _ => throw new Exception("amogus")
    }
  }
  def vFunct6Gen(vInst: String): String = {
    vInst match {
      case "vadd" => "000000"
      case "vsub" => "000010"
      case "vrsub" => "000011"
      case "vadc" => "010000"
      case "vmadc" => "010001"
      case "vsbc" => "010010"
      case "vmsbc" => "010011"
      case "vand" => "001001"
      case "vor" => "001010"
      case "vxor" => "001011"
      case "vmseq" => "011000"
      case "vmsne" => "011001"
      case "vmsltu" => "011010"
      case "vmslt" => "011011"
      case "vmsleu" => "011100"
      case "vmsle" => "011101"
      case "vmsgtu" => "011110"
      case "vmsgt" => "011111"
      case "vminu" => "000100"
      case "vmin" => "000101"
      case "vmaxu" => "000110"
      case "vmax" => "000111"
      case "vmul" => "100101"
      case "vmulh" => "100111"
      case "vmulhu" => "100100"
      case "vmulhsu" => "100110"
      case "vmacc" => ???
      case "vnmsac" => ???
      case "vmadd" => ???
      case "vnmsub" => ???
      case "vmerge" => "010111"
      case "vmv" => "010111"
      case "vredsum" => ???
      case "vredmaxu" => ???
      case "vredmax" => ???
      case "vredminu" => ???
      case "vredmin" => ???
      case "vredand" => ???
      case "vredor" => ???
      case "vredxor" => ???
      case "vmand" => "011001"
      case "vmnand" => "011101"
      case "vmandn" => "011000"
      case "vmxor" => "011011"
      case "vmor" => "011010"
      case "vmnor" => "011110"
      case "vmorn" => "011100"
      case "vmxnor" => "011111"
      case "vmv_x_s" => ???
      case "vmv_s_x" => ???
      case _ => throw new Exception(s"inst $vInst is invalid")
    }
  }
  def vArithGen(vInst: String, vsource: VSOURCE.Type, vm: String, vs2Zero: Boolean) = {
    if (vm == "?" || vm == "0" || vm == "1") {
      BitPat("b" + vFunct6Gen(vInst) + vm + (if(vs2Zero) "00000" else "?????") + "?????" + vsourceGen(vsource) + "?????" + "1010111")
    } else {
      throw new Exception(s"vm $vm is invalid")
    }
  }
  def vArithGen(vInst: String, vsource: VSOURCE.Type, vm: String): BitPat = vArithGen(vInst, vsource, vm, vs2Zero = false)
  def vArithGen(vInst: String, vsource: VSOURCE.Type): BitPat = vArithGen(vInst, vsource, vm = "?")
  def VADD_VV  = vArithGen(vInst = "vadd", vsource = VSOURCE.VV)
  def VADD_VX  = vArithGen(vInst = "vadd", vsource = VSOURCE.VX)
  def VADD_VI  = vArithGen(vInst = "vadd", vsource = VSOURCE.VI)
  def VSUB_VV  = vArithGen(vInst = "vsub", vsource = VSOURCE.VV)
  def VSUB_VX  = vArithGen(vInst = "vsub", vsource = VSOURCE.VX)
  def VRSUB_VX = vArithGen(vInst = "vrsub", vsource = VSOURCE.VX)
  def VRSUB_VI = vArithGen(vInst = "vrsub", vsource = VSOURCE.VI)
  def VADC_VVM = vArithGen(vInst = "vadc",  vsource = VSOURCE.VV, vm = "0")
  def VADC_VXM = vArithGen(vInst = "vadc",  vsource = VSOURCE.VX, vm = "0")
  def VADC_VIM = vArithGen(vInst = "vadc",  vsource = VSOURCE.VI, vm = "0")
  def VMADC_VVM = vArithGen(vInst = "vmadc", vsource = VSOURCE.VV, vm = "0")
  def VMADC_VXM = vArithGen(vInst = "vmadc", vsource = VSOURCE.VX, vm = "0")
  def VMADC_VIM = vArithGen(vInst = "vmadc", vsource = VSOURCE.VI, vm = "0")
  def VMADC_VV  = vArithGen(vInst = "vmadc", vsource = VSOURCE.VV, vm = "1")
  def VMADC_VX  = vArithGen(vInst = "vmadc", vsource = VSOURCE.VX, vm = "1")
  def VMADC_VI  = vArithGen(vInst = "vmadc", vsource = VSOURCE.VI, vm = "1")
  def VSBC_VVM  = vArithGen(vInst = "vsbc", vsource = VSOURCE.VV, vm = "0")
  def VSBC_VXM  = vArithGen(vInst = "vsbc", vsource = VSOURCE.VX, vm = "0")
  def VMSBC_VVM = vArithGen(vInst = "vmsbc", vsource = VSOURCE.VV, vm = "0")
  def VMSBC_VXM = vArithGen(vInst = "vmsbc", vsource = VSOURCE.VX, vm = "0")
  def VMSBC_VV  = vArithGen(vInst = "vmsbc", vsource = VSOURCE.VV, vm = "1")
  def VMSBC_VX  = vArithGen(vInst = "vmsbc", vsource = VSOURCE.VX, vm = "1")
  def VAND_VV   = vArithGen(vInst = "vand", vsource = VSOURCE.VV)
  def VAND_VX   = vArithGen(vInst = "vand", vsource = VSOURCE.VX)
  def VAND_VI   = vArithGen(vInst = "vand", vsource = VSOURCE.VI)
  def VOR_VV    = vArithGen(vInst = "vor", vsource = VSOURCE.VV)
  def VOR_VX    = vArithGen(vInst = "vor", vsource = VSOURCE.VX)
  def VOR_VI    = vArithGen(vInst = "vor", vsource = VSOURCE.VI)
  def VXOR_VV   = vArithGen(vInst = "vxor", vsource = VSOURCE.VV)
  def VXOR_VX   = vArithGen(vInst = "vxor", vsource = VSOURCE.VX)
  def VXOR_VI   = vArithGen(vInst = "vxor", vsource = VSOURCE.VI)
  def VMSEQ_VV  = vArithGen(vInst = "vmseq", vsource = VSOURCE.VV)
  def VMSEQ_VX  = vArithGen(vInst = "vmseq", vsource = VSOURCE.VX)
  def VMSEQ_VI  = vArithGen(vInst = "vmseq", vsource = VSOURCE.VI)
  def VMSNE_VV  = vArithGen(vInst = "vmsne", vsource = VSOURCE.VV)
  def VMSNE_VX  = vArithGen(vInst = "vmsne", vsource = VSOURCE.VX)
  def VMSNE_VI  = vArithGen(vInst = "vmsne", vsource = VSOURCE.VI)
  def VMSLTU_VV = vArithGen(vInst = "vmsltu", vsource = VSOURCE.VV)
  def VMSLTU_VX = vArithGen(vInst = "vmsltu", vsource = VSOURCE.VX)
  def VMSLT_VV = vArithGen(vInst = "vmslt", vsource = VSOURCE.VV)
  def VMSLT_VX = vArithGen(vInst = "vmslt", vsource = VSOURCE.VX)
  def VMSLEU_VV = vArithGen(vInst = "vmsleu", vsource = VSOURCE.VV)
  def VMSLEU_VX = vArithGen(vInst = "vmsleu", vsource = VSOURCE.VX)
  def VMSLEU_VI = vArithGen(vInst = "vmsleu", vsource = VSOURCE.VI)
  def VMSLE_VV = vArithGen(vInst = "vmsle", vsource = VSOURCE.VV)
  def VMSLE_VX = vArithGen(vInst = "vmsle", vsource = VSOURCE.VX)
  def VMSLE_VI = vArithGen(vInst = "vmsle", vsource = VSOURCE.VI)
  def VMSGTU_VX = vArithGen(vInst = "vmsgtu", vsource = VSOURCE.VX)
  def VMSGTU_VI = vArithGen(vInst = "vmsgtu", vsource = VSOURCE.VI)
  def VMSGT_VX = vArithGen(vInst = "vmsgt", vsource = VSOURCE.VX)
  def VMSGT_VI = vArithGen(vInst = "vmsgt", vsource = VSOURCE.VI)
  def VMINU_VV = vArithGen(vInst = "vminu", vsource = VSOURCE.VV)
  def VMINU_VX = vArithGen(vInst = "vminu", vsource = VSOURCE.VX)
  def VMIN_VV = vArithGen(vInst = "vmin", vsource = VSOURCE.VV)
  def VMIN_VX = vArithGen(vInst = "vmin", vsource = VSOURCE.VX)
  def VMAXU_VV = vArithGen(vInst = "vmaxu", vsource = VSOURCE.VV)
  def VMAXU_VX = vArithGen(vInst = "vmaxu", vsource = VSOURCE.VX)
  def VMAX_VV = vArithGen(vInst = "vmax", vsource = VSOURCE.VV)
  def VMAX_VX = vArithGen(vInst = "vmax", vsource = VSOURCE.VX)

  def VMUL_VV = vArithGen(vInst = "vmul", vsource = VSOURCE.MVV)
  def VMUL_VX = vArithGen(vInst = "vmul", vsource = VSOURCE.MVX)
  def VMERGE_VVM = vArithGen(vInst = "vmerge", vsource = VSOURCE.VV, vm = "0")
  def VMERGE_VXM = vArithGen(vInst = "vmerge", vsource = VSOURCE.VX, vm = "0")
  def VMERGE_VIM = vArithGen(vInst = "vmerge", vsource = VSOURCE.VI, vm = "0")
  def VMV_VV = vArithGen(vInst = "vmv", vsource = VSOURCE.VV, vm = "1", vs2Zero = true)
  def VMV_VX = vArithGen(vInst = "vmv", vsource = VSOURCE.VX, vm = "1", vs2Zero = true)
  def VMV_VI = vArithGen(vInst = "vmv", vsource = VSOURCE.VI, vm = "1", vs2Zero = true)

  def VMAND_MM = vArithGen(vInst = "vmand", vsource = VSOURCE.MVV, vm = "1")
  def VMNAND_MM = vArithGen(vInst = "vmnand", vsource = VSOURCE.MVV, vm = "1")
  def VMANDN_MM = vArithGen(vInst = "vmandn", vsource = VSOURCE.MVV, vm = "1")
  def VMXOR_MM = vArithGen(vInst = "vmxor", vsource = VSOURCE.MVV, vm = "1")
  def VMOR_MM = vArithGen(vInst = "vmor", vsource = VSOURCE.MVV, vm = "1")
  def VMNOR_MM = vArithGen(vInst = "vmnor", vsource = VSOURCE.MVV, vm = "1")
  def VMORN_MM = vArithGen(vInst = "vmorn", vsource = VSOURCE.MVV, vm = "1")
  def VMXNOR_MM = vArithGen(vInst = "vmxnor", vsource = VSOURCE.MVV, vm = "1")
}

object Causes {
  val misaligned_fetch = 0x0
  val fetch_access = 0x1
  val illegal_instruction = 0x2
  val breakpoint = 0x3
  val misaligned_load = 0x4
  val load_access = 0x5
  val misaligned_store = 0x6
  val store_access = 0x7
  val user_ecall = 0x8
  val machine_ecall = 0xb
  val all = {
    val res = collection.mutable.ArrayBuffer[Int]()
    res += misaligned_fetch
    res += fetch_access
    res += illegal_instruction
    res += breakpoint
    res += misaligned_load
    res += load_access
    res += misaligned_store
    res += store_access
    res += user_ecall
    res += machine_ecall
    res.toArray
  }
}
object CSRs {
  val vstart = 0x008
  val vxsat = 0x009
  val vxrm = 0x00A
  val vcsr = 0x00F
  val vl = 0xC20
  val vtype = 0xC21
  val vlenb = 0xC22
  val cycle = 0xc00
  val time = 0xc01
  val instret = 0xc02
  val hpmcounter3 = 0xc03
  val hpmcounter4 = 0xc04
  val hpmcounter5 = 0xc05
  val hpmcounter6 = 0xc06
  val hpmcounter7 = 0xc07
  val hpmcounter8 = 0xc08
  val hpmcounter9 = 0xc09
  val hpmcounter10 = 0xc0a
  val hpmcounter11 = 0xc0b
  val hpmcounter12 = 0xc0c
  val hpmcounter13 = 0xc0d
  val hpmcounter14 = 0xc0e
  val hpmcounter15 = 0xc0f
  val hpmcounter16 = 0xc10
  val hpmcounter17 = 0xc11
  val hpmcounter18 = 0xc12
  val hpmcounter19 = 0xc13
  val hpmcounter20 = 0xc14
  val hpmcounter21 = 0xc15
  val hpmcounter22 = 0xc16
  val hpmcounter23 = 0xc17
  val hpmcounter24 = 0xc18
  val hpmcounter25 = 0xc19
  val hpmcounter26 = 0xc1a
  val hpmcounter27 = 0xc1b
  val hpmcounter28 = 0xc1c
  val hpmcounter29 = 0xc1d
  val hpmcounter30 = 0xc1e
  val hpmcounter31 = 0xc1f
  val mstatus = 0x300
  val misa = 0x301
  val medeleg = 0x302
  val mideleg = 0x303
  val mie = 0x304
  val mtvec = 0x305
  val mscratch = 0x340
  val mcounteren = 0x306
  val mepc = 0x341
  val mcause = 0x342
  val mtval = 0x343
  val mip = 0x344
  val mtinst = 0x34a
  val mtval2 = 0x34b
  val tselect = 0x7a0
  val tdata1 = 0x7a1
  val tdata2 = 0x7a2
  val tdata3 = 0x7a3
  val dcsr = 0x7b0
  val dpc = 0x7b1
  val dscratch = 0x7b2
  val mcycle = 0xb00
  val minstret = 0xb02
  val mhpmcounter3 = 0xb03
  val mhpmcounter4 = 0xb04
  val mhpmcounter5 = 0xb05
  val mhpmcounter6 = 0xb06
  val mhpmcounter7 = 0xb07
  val mhpmcounter8 = 0xb08
  val mhpmcounter9 = 0xb09
  val mhpmcounter10 = 0xb0a
  val mhpmcounter11 = 0xb0b
  val mhpmcounter12 = 0xb0c
  val mhpmcounter13 = 0xb0d
  val mhpmcounter14 = 0xb0e
  val mhpmcounter15 = 0xb0f
  val mhpmcounter16 = 0xb10
  val mhpmcounter17 = 0xb11
  val mhpmcounter18 = 0xb12
  val mhpmcounter19 = 0xb13
  val mhpmcounter20 = 0xb14
  val mhpmcounter21 = 0xb15
  val mhpmcounter22 = 0xb16
  val mhpmcounter23 = 0xb17
  val mhpmcounter24 = 0xb18
  val mhpmcounter25 = 0xb19
  val mhpmcounter26 = 0xb1a
  val mhpmcounter27 = 0xb1b
  val mhpmcounter28 = 0xb1c
  val mhpmcounter29 = 0xb1d
  val mhpmcounter30 = 0xb1e
  val mhpmcounter31 = 0xb1f
  val mucounteren = 0x320
  val mhpmevent3 = 0x323
  val mhpmevent4 = 0x324
  val mhpmevent5 = 0x325
  val mhpmevent6 = 0x326
  val mhpmevent7 = 0x327
  val mhpmevent8 = 0x328
  val mhpmevent9 = 0x329
  val mhpmevent10 = 0x32a
  val mhpmevent11 = 0x32b
  val mhpmevent12 = 0x32c
  val mhpmevent13 = 0x32d
  val mhpmevent14 = 0x32e
  val mhpmevent15 = 0x32f
  val mhpmevent16 = 0x330
  val mhpmevent17 = 0x331
  val mhpmevent18 = 0x332
  val mhpmevent19 = 0x333
  val mhpmevent20 = 0x334
  val mhpmevent21 = 0x335
  val mhpmevent22 = 0x336
  val mhpmevent23 = 0x337
  val mhpmevent24 = 0x338
  val mhpmevent25 = 0x339
  val mhpmevent26 = 0x33a
  val mhpmevent27 = 0x33b
  val mhpmevent28 = 0x33c
  val mhpmevent29 = 0x33d
  val mhpmevent30 = 0x33e
  val mhpmevent31 = 0x33f
  val mvendorid = 0xf11
  val marchid = 0xf12
  val mimpid = 0xf13
  val mhartid = 0xf14
  val mconfigptr = 0xf15
  val cycleh = 0xc80
  val instreth = 0xc82
  val hpmcounter3h = 0xc83
  val hpmcounter4h = 0xc84
  val hpmcounter5h = 0xc85
  val hpmcounter6h = 0xc86
  val hpmcounter7h = 0xc87
  val hpmcounter8h = 0xc88
  val hpmcounter9h = 0xc89
  val hpmcounter10h = 0xc8a
  val hpmcounter11h = 0xc8b
  val hpmcounter12h = 0xc8c
  val hpmcounter13h = 0xc8d
  val hpmcounter14h = 0xc8e
  val hpmcounter15h = 0xc8f
  val hpmcounter16h = 0xc90
  val hpmcounter17h = 0xc91
  val hpmcounter18h = 0xc92
  val hpmcounter19h = 0xc93
  val hpmcounter20h = 0xc94
  val hpmcounter21h = 0xc95
  val hpmcounter22h = 0xc96
  val hpmcounter23h = 0xc97
  val hpmcounter24h = 0xc98
  val hpmcounter25h = 0xc99
  val hpmcounter26h = 0xc9a
  val hpmcounter27h = 0xc9b
  val hpmcounter28h = 0xc9c
  val hpmcounter29h = 0xc9d
  val hpmcounter30h = 0xc9e
  val hpmcounter31h = 0xc9f
  val mcycleh = 0xb80
  val minstreth = 0xb82
  val mhpmcounter3h = 0xb83
  val mhpmcounter4h = 0xb84
  val mhpmcounter5h = 0xb85
  val mhpmcounter6h = 0xb86
  val mhpmcounter7h = 0xb87
  val mhpmcounter8h = 0xb88
  val mhpmcounter9h = 0xb89
  val mhpmcounter10h = 0xb8a
  val mhpmcounter11h = 0xb8b
  val mhpmcounter12h = 0xb8c
  val mhpmcounter13h = 0xb8d
  val mhpmcounter14h = 0xb8e
  val mhpmcounter15h = 0xb8f
  val mhpmcounter16h = 0xb90
  val mhpmcounter17h = 0xb91
  val mhpmcounter18h = 0xb92
  val mhpmcounter19h = 0xb93
  val mhpmcounter20h = 0xb94
  val mhpmcounter21h = 0xb95
  val mhpmcounter22h = 0xb96
  val mhpmcounter23h = 0xb97
  val mhpmcounter24h = 0xb98
  val mhpmcounter25h = 0xb99
  val mhpmcounter26h = 0xb9a
  val mhpmcounter27h = 0xb9b
  val mhpmcounter28h = 0xb9c
  val mhpmcounter29h = 0xb9d
  val mhpmcounter30h = 0xb9e
  val mhpmcounter31h = 0xb9f
  val all = {
    val res = collection.mutable.ArrayBuffer[Int]()
    res += cycle
    res += instret
    res += hpmcounter3
    res += hpmcounter4
    res += hpmcounter5
    res += hpmcounter6
    res += hpmcounter7
    res += hpmcounter8
    res += hpmcounter9
    res += hpmcounter10
    res += hpmcounter11
    res += hpmcounter12
    res += hpmcounter13
    res += hpmcounter14
    res += hpmcounter15
    res += hpmcounter16
    res += hpmcounter17
    res += hpmcounter18
    res += hpmcounter19
    res += hpmcounter20
    res += hpmcounter21
    res += hpmcounter22
    res += hpmcounter23
    res += hpmcounter24
    res += hpmcounter25
    res += hpmcounter26
    res += hpmcounter27
    res += hpmcounter28
    res += hpmcounter29
    res += hpmcounter30
    res += hpmcounter31
    res += mstatus
    res += misa
    res += medeleg
    res += mideleg
    res += mie
    res += mtvec
    res += mscratch
    res += mepc
    res += mcause
    res += mtval
    res += mip
    res += tselect
    res += tdata1
    res += tdata2
    res += tdata3
    res += dcsr
    res += dpc
    res += dscratch
    res += mcycle
    res += minstret
    res += mhpmcounter3
    res += mhpmcounter4
    res += mhpmcounter5
    res += mhpmcounter6
    res += mhpmcounter7
    res += mhpmcounter8
    res += mhpmcounter9
    res += mhpmcounter10
    res += mhpmcounter11
    res += mhpmcounter12
    res += mhpmcounter13
    res += mhpmcounter14
    res += mhpmcounter15
    res += mhpmcounter16
    res += mhpmcounter17
    res += mhpmcounter18
    res += mhpmcounter19
    res += mhpmcounter20
    res += mhpmcounter21
    res += mhpmcounter22
    res += mhpmcounter23
    res += mhpmcounter24
    res += mhpmcounter25
    res += mhpmcounter26
    res += mhpmcounter27
    res += mhpmcounter28
    res += mhpmcounter29
    res += mhpmcounter30
    res += mhpmcounter31
    res += mucounteren
    res += mhpmevent3
    res += mhpmevent4
    res += mhpmevent5
    res += mhpmevent6
    res += mhpmevent7
    res += mhpmevent8
    res += mhpmevent9
    res += mhpmevent10
    res += mhpmevent11
    res += mhpmevent12
    res += mhpmevent13
    res += mhpmevent14
    res += mhpmevent15
    res += mhpmevent16
    res += mhpmevent17
    res += mhpmevent18
    res += mhpmevent19
    res += mhpmevent20
    res += mhpmevent21
    res += mhpmevent22
    res += mhpmevent23
    res += mhpmevent24
    res += mhpmevent25
    res += mhpmevent26
    res += mhpmevent27
    res += mhpmevent28
    res += mhpmevent29
    res += mhpmevent30
    res += mhpmevent31
    res += mvendorid
    res += marchid
    res += mimpid
    res += mhartid
    res.toArray
  }
  val all32 = {
    val res = collection.mutable.ArrayBuffer(all:_*)
    res += cycleh
    res += instreth
    res += hpmcounter3h
    res += hpmcounter4h
    res += hpmcounter5h
    res += hpmcounter6h
    res += hpmcounter7h
    res += hpmcounter8h
    res += hpmcounter9h
    res += hpmcounter10h
    res += hpmcounter11h
    res += hpmcounter12h
    res += hpmcounter13h
    res += hpmcounter14h
    res += hpmcounter15h
    res += hpmcounter16h
    res += hpmcounter17h
    res += hpmcounter18h
    res += hpmcounter19h
    res += hpmcounter20h
    res += hpmcounter21h
    res += hpmcounter22h
    res += hpmcounter23h
    res += hpmcounter24h
    res += hpmcounter25h
    res += hpmcounter26h
    res += hpmcounter27h
    res += hpmcounter28h
    res += hpmcounter29h
    res += hpmcounter30h
    res += hpmcounter31h
    res += mcycleh
    res += minstreth
    res += mhpmcounter3h
    res += mhpmcounter4h
    res += mhpmcounter5h
    res += mhpmcounter6h
    res += mhpmcounter7h
    res += mhpmcounter8h
    res += mhpmcounter9h
    res += mhpmcounter10h
    res += mhpmcounter11h
    res += mhpmcounter12h
    res += mhpmcounter13h
    res += mhpmcounter14h
    res += mhpmcounter15h
    res += mhpmcounter16h
    res += mhpmcounter17h
    res += mhpmcounter18h
    res += mhpmcounter19h
    res += mhpmcounter20h
    res += mhpmcounter21h
    res += mhpmcounter22h
    res += mhpmcounter23h
    res += mhpmcounter24h
    res += mhpmcounter25h
    res += mhpmcounter26h
    res += mhpmcounter27h
    res += mhpmcounter28h
    res += mhpmcounter29h
    res += mhpmcounter30h
    res += mhpmcounter31h
    res.toArray
  }
}
