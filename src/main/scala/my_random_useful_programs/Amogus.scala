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
  val array0 = Seq(0x8E, 0x57, 0x5, 0xAA, 0x2C, 0xA9, 0x35, 0xA6, 0x7F, 0x77, 0x46, 0x28, 0x70, 0x6F, 0xF0, 0xA6, 0x2B, 0x66, 0x22, 0x27, 0xDF, 0xA0, 0xDD, 0x81, 0xBC, 0x89, 0x39, 0xB9, 0xED, 0x4A, 0xC1, 0xD0, 0x56, 0x6, 0x3D, 0x42, 0x27, 0x2F, 0x9D, 0x70, 0xAD, 0x99, 0xDF, 0xA, 0x5C, 0x8E, 0xF4, 0x78)
  val array1 = Seq(0xCE, 0x1F, 0x94, 0x38, 0x74, 0x13, 0x62, 0x16, 0x4E, 0x70, 0xC9, 0xC6, 0xBF, 0xB3, 0xCE, 0xED, 0x6A, 0xE7, 0xD3, 0x3F, 0xC3, 0xAA, 0xC0, 0x3D, 0xF5, 0x89, 0xC5, 0x48, 0x18, 0x13, 0x15, 0x7E, 0x52, 0x97, 0xFE, 0x47, 0x56, 0x9A, 0xAF, 0xAC, 0x71, 0x8F, 0x4E, 0xED, 0xFE, 0x98, 0xA1, 0x2D)
  val sltArray = (array0 zip array1).map {
    case (n0, n1) => n0 < n1
  }
  sltArray.zipWithIndex.foreach {
    case (b, i) => println(s"index $i: $b")
  }
}