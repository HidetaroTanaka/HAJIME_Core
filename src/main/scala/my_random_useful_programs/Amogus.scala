package my_random_useful_programs

object Amogus extends App {
  val instList = List(
    "VADD_VV",
    "VADD_VX",
    "VADD_VI",
    "VSUB_VV",
    "VSUB_VX",
    "VRSUB_VX",
    "VRSUB_VI",
    "VADC_VVM",
    "VADC_VXM",
    "VADC_VIM",
    "VMADC_VVM",
    "VMADC_VXM",
    "VMADC_VIM",
    "VMADC_VV",
    "VMADC_VX",
    "VMADC_VI",
    "VSBC_VVM",
    "VSBC_VXM",
    "VMSBC_VVM",
    "VMSBC_VXM",
    "VMSBC_VV",
    "VMSBC_VX",
    "VAND_VV",
    "VAND_VX",
    "VAND_VI",
    "VOR_VV",
    "VOR_VX",
    "VOR_VI",
    "VXOR_VV",
    "VXOR_VX",
    "VXOR_VI",
    "VMSEQ_VV",
    "VMSEQ_VX",
    "VMSEQ_VI",
    "VMSNE_VV",
    "VMSNE_VX",
    "VMSNE_VI",
    "VMSLTU_VV",
    "VMSLTU_VX",
    "VMSLT_VV",
    "VMSLT_VX",
    "VMSLEU_VV",
    "VMSLEU_VX",
    "VMSLEU_VI",
    "VMSLE_VV",
    "VMSLE_VX",
    "VMSLE_VI",
    "VMSGTU_VX",
    "VMSGTU_VI",
    "VMSGT_VX",
    "VMSGT_VI",
    "VMINU_VV",
    "VMINU_VX",
    "VMIN_VV",
    "VMIN_VX",
    "VMAXU_VV",
    "VMAXU_VX",
    "VMAX_VV",
    "VMAX_VX",
    "VMERGE_VVM",
    "VMERGE_VXM",
    "VMERGE_VIM",
    "VMV_VV",
    "VMV_VX",
    "VMV_VI",
  )
  instList.foreach(
    x => {
      val tmpSeq = x.split("_")
      println(s"$x -> List(Y, Branch.NONE, Value1.${
        tmpSeq(1) match {
          case "VV" => "ZERO"
          case "VVM" => "ZERO"
          case "VX" => "RS1"
          case "VXM" => "RS1"
          case "VI" => "UIMM19_15"
          case "VIM" => "UIMM19_15"
        }
      }, Value2.ZERO, ARITHMETIC_FCN.NONE, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, Y),")
    }
  )
}

object Amogus1 extends App {
  val dataArray = Seq(0xA4, 0x3, 0x16, 0x5A, 0x84, 0xBD, 0x7A, 0xC4, 0x41, 0x55, 0x44, 0x6D, 0xE7, 0x3C, 0x0, 0x1B, 0xA7, 0x2A, 0x2C, 0x2B, 0xE4, 0xC7, 0x3, 0x82, 0xB8, 0xAB, 0xA1, 0x90, 0xF3, 0x1B, 0x81, 0x5, 0xE7, 0x6C, 0xA7, 0xD, 0x19, 0x2A, 0xC4, 0x31, 0x98, 0x5, 0xFA, 0xDE, 0x88, 0x1D, 0xA0, 0x95)
  val indexArray: Seq[Byte] = Seq(-11, -12, 6, 8, 4, -4, -8, 16, -13, 7, 11, 2, 10, 20, -22, 1, -6, -2, -19, -5, -15, -16, 19, -17, 21, 15, -7, 22, -21, 18, 9, 17, -20, -23, -18, 3, -1, -3, -14, -10, 13, 0, -24, 5, 14, -9, 12)
  println(indexArray.slice(0, 28).map(x => x.toHexString).mkString("{", ",", "}"))
  println((0 until 28).map(i => dataArray(indexArray(i)+24).toHexString).mkString("{", ",", "}"))
}