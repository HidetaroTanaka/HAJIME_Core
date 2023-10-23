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
  val array0 = Seq(6, -12, 12, -5, 20, 15, -14, 8, 9, -19, 18, 11, 7, -10, -16, -3, 16, -23, -9, 19, 4, 1, -17, -20, 3, 0, -21, -8, -6, -22, 2, -13, -2, 22, 5, -24, -11, 14, -1, 10, -4, 13, 17, 21, -15, -7, -18)
  val array1 = Seq(9, -13, -16, -21, -3, -23, 12, -17, -15, 22, 15, -19, 16, 10, -2, 6, -12, 4, 20, 18, -9, -7, 17, -14, 14, 8, -20, -24, -11, -10, 7, 2, 21, -4, 3, -22, 0, -8, 5, -18, -1, 13, -5, 19, 1, -6, 11)
  (array0 zip array1).map {
    case (a, b) => a + b
  }.zipWithIndex.foreach {
    case (d, i) => println(s"idx $i: $d")
  }
}