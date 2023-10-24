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
  val array0 = Seq(0xC02D, 0x5923, 0x2871, 0xAFF6, 0x880D, 0xE71D, 0xBE2F, 0xF57A, 0x5B71, 0x34C7, 0xE16F, 0xD3B7, 0x1C60, 0x3EE7, 0x8CF4, 0x5234, 0x520F, 0x5B9A, 0xCE6B, 0x2A58, 0xB2A7, 0xEFE8, 0x546E, 0xC4B3, 0xF4E1, 0xBEB7, 0x1FF4, 0x358, 0x5FBC, 0x8726, 0x8A4B, 0x2F49, 0x9362, 0x4C5E, 0xDFD9, 0x1615, 0xC5AD, 0xE4D8, 0x6233, 0xB139, 0x44C8, 0x466E, 0x1906, 0x407D, 0x33E1, 0x6844, 0xA3A7, 0xE5FC)
  val array1 = Seq(0xA225, 0xA2B1, 0x56F0, 0x3862, 0x6079, 0x2300, 0x3BF0, 0xAE87, 0xF41A, 0x5A87, 0xD476, 0x2F2B, 0x1049, 0xDA35, 0xFE1C, 0xCBE1, 0x4FA7, 0x2AA6, 0xFB7E, 0x923, 0x738D, 0x3C7C, 0x7797, 0x7B0B, 0x7FAE, 0xC46E, 0xBA4C, 0x8001, 0xE40B, 0x590E, 0xD048, 0x294A, 0xD186, 0xEBAD, 0x77BB, 0xB47F, 0x677A, 0xB406, 0x1E40, 0xDBD5, 0xAE23, 0x299A, 0x3F04, 0xA982, 0xC976, 0xDC6D, 0xACE0, 0x5D39)
  val array2 = Seq(0x40B5, 0x4514, 0xC3E9, 0xD60A, 0x3BFE, 0x563, 0x11B1, 0xB809, 0x3422, 0xCA78, 0xA74F, 0x115F, 0x3F2C, 0x29E4, 0x31F6, 0xCA9B, 0xBCC0, 0xF7, 0xA2B0, 0x7360, 0x5093, 0xB7FA, 0xD5CD, 0xDBE, 0x7A44, 0xBD3, 0xA64E, 0x39A8, 0x520B, 0xFD93, 0x3710, 0x7FE9, 0x96CF, 0x1CD9, 0xE935, 0x7CD7, 0x7929, 0xEEC1, 0x84F6, 0xBEC1, 0xB11B, 0x6754, 0x12D0, 0x1CD5, 0xE89, 0xBB5B, 0xC66F, 0x479B)
  val carry = (array0 zip array1).map {
    case (a, b) => (a + b) > 0xFFFF
  }
  carry.zipWithIndex.foreach {
    case (b, i) => println(s"index $i: $b")
  }
  println()
  (array2 zip carry).map {
    case (n, b) => n + 0x1919 + (if(b) 1 else 0)
  }.zipWithIndex.foreach {
    case (n, i) => println(s"index $i: ${n.toHexString}")
  }
}