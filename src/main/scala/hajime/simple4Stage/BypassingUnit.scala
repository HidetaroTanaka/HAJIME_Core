package hajime.simple4Stage

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common._

class RegIndexWithValue(implicit params: HajimeCoreParams) extends Bundle {
  val index = UInt(5.W)
  val value = UInt(params.xprlen.W)
}

class BypassingLogicInputs_ID extends Bundle {
  val rs1_index = Valid(UInt(5.W))
  val rs2_index = Valid(UInt(5.W))
}

class BypassingLogicOutputs_ID(implicit params: HajimeCoreParams) extends Bundle {
  val rs1_value = Valid(UInt(params.xprlen.W))
  val rs1_bypassMatchAtEX = Bool()
  val rs1_bypassMatchAtWB = Bool()
  val rs2_value = Valid(UInt(params.xprlen.W))
  val rs2_bypassMatchAtEX = Bool()
  val rs2_bypassMatchAtWB = Bool()
}

class BypassingLogicInputs_EX(implicit params: HajimeCoreParams) extends Bundle {
  val rd = Valid(new RegIndexWithValue())
}

class BypassingLogicInputs_WB(implicit params: HajimeCoreParams) extends Bundle {
  val rd = Valid(new RegIndexWithValue())
}

class BypassingLogicIO_ID(implicit params: HajimeCoreParams) extends Bundle {
  val in = Input(new BypassingLogicInputs_ID)
  val out = Output(new BypassingLogicOutputs_ID())
}

class BypassingLogicIO_EX(implicit params: HajimeCoreParams) extends Bundle {
  /**
   * in.valid: 該当ステージの命令がrdへ書き込むか否か
   * in.bits.rd.bits.valid: 該当ステージでrdへ書き込む値が用意されているか否か
   * in.bits.rd.bits.index: 該当ステージのrdのインデックス
   * in.bits.rd.bits.value: 該当ステージのrdの値
   */
  val in = Flipped(ValidIO(new BypassingLogicInputs_EX()))
}

class BypassingLogicIO_WB(implicit params: HajimeCoreParams) extends Bundle {
  val in = Flipped(ValidIO(new BypassingLogicInputs_WB()))
}

class BypassingUnit(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new Bundle {
    val ID = new BypassingLogicIO_ID()
    val EX = new BypassingLogicIO_EX()
    val WB = new BypassingLogicIO_WB()
  })
  // Bypass rs1 value from WB, EX to ID
  // EXステージからフォワーディングする必要があるが不可能（乗算、メモリアクセス等）な場合を考えて、IDのvalidのみ考える
  val WB_rd_and_ID_rs1_matches_not_zero = ((io.ID.in.rs1_index.bits =/= 0.U(5.W)) && (io.ID.in.rs1_index.bits === io.WB.in.bits.rd.bits.index) && io.ID.in.rs1_index.valid && io.WB.in.valid)
  val EX_rd_and_ID_rs1_matches_not_zero = ((io.ID.in.rs1_index.bits =/= 0.U(5.W)) && (io.ID.in.rs1_index.bits === io.EX.in.bits.rd.bits.index) && io.ID.in.rs1_index.valid && io.EX.in.valid)
  // EXのrdとIDのrs1が一致しており、かつIDのrs1がvalidならばEXを優先
  io.ID.out.rs1_value.bits := Mux(EX_rd_and_ID_rs1_matches_not_zero, io.EX.in.bits.rd.bits.value, io.WB.in.bits.rd.bits.value)
  // これがvalidでないときの挙動はCPU側でやる
  io.ID.out.rs1_value.valid := MuxCase(false.B,
    Seq(
      // EXのrdとIDのrs1が一致しており、かつIDのrs1がvalidならばEXのrdを持ってくる（EXがvalidでなければ結果もvalidではない、メモリロードや乗算など）
      EX_rd_and_ID_rs1_matches_not_zero -> io.EX.in.bits.rd.valid,
      // 上と同様（メモリロード命令でBチャネルがvalidでない時など）
      WB_rd_and_ID_rs1_matches_not_zero -> io.WB.in.bits.rd.valid
    )
  )
  // 値が用意できるかに関わらず、EX・WBからフォワーディングすべき値があるか否か
  io.ID.out.rs1_bypassMatchAtEX := EX_rd_and_ID_rs1_matches_not_zero
  io.ID.out.rs1_bypassMatchAtWB := WB_rd_and_ID_rs1_matches_not_zero

  val WB_rd_and_ID_rs2_matches_not_zero = ((io.ID.in.rs2_index.bits =/= 0.U(5.W)) && (io.ID.in.rs2_index.bits === io.WB.in.bits.rd.bits.index) && io.ID.in.rs2_index.valid && io.WB.in.valid)
  val EX_rd_and_ID_rs2_matches_not_zero = ((io.ID.in.rs2_index.bits =/= 0.U(5.W)) && (io.ID.in.rs2_index.bits === io.EX.in.bits.rd.bits.index) && io.ID.in.rs2_index.valid && io.EX.in.valid)
  io.ID.out.rs2_value.bits := Mux(EX_rd_and_ID_rs2_matches_not_zero, io.EX.in.bits.rd.bits.value, io.WB.in.bits.rd.bits.value)
  io.ID.out.rs2_value.valid := MuxCase(false.B,
    Seq(
      EX_rd_and_ID_rs2_matches_not_zero -> io.EX.in.bits.rd.valid,
      WB_rd_and_ID_rs2_matches_not_zero -> io.WB.in.bits.rd.valid,
    )
  )
  io.ID.out.rs2_bypassMatchAtEX := EX_rd_and_ID_rs2_matches_not_zero
  io.ID.out.rs2_bypassMatchAtWB := WB_rd_and_ID_rs2_matches_not_zero
}

object BypassingUnit extends App {
  def apply(implicit params: HajimeCoreParams): BypassingUnit = new BypassingUnit()
  ChiselStage.emitSystemVerilogFile(BypassingUnit(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}