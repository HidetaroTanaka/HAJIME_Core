package hajime.simple4Stage

import chisel3._
import chisel3.util._
import hajime.common.{COMPILE_CONSTANTS, RISCV_Consts}

class RegIndexWithValue(xprlen: Int) extends Bundle {
  val index = UInt(5.W)
  val value = UInt(xprlen.W)
}

class BypassingLogicInputs_ID extends Bundle {
  val rs1_index = Valid(UInt(5.W))
  val rs2_index = Valid(UInt(5.W))
}

class BypassingLogicOutputs_ID(xprlen: Int) extends Bundle {
  val rs1_value = Valid(UInt(xprlen.W))
  val rs2_value = Valid(UInt(xprlen.W))
  val stall = Bool()
}

class BypassingLogicInputs_EX(xprlen: Int) extends Bundle {
  val rd = Valid(new RegIndexWithValue(xprlen))
  val dcache_req_not_ready_and_has_mem_inst_in_EX_stage = Bool()
}

class BypassingLogicOutputs_EX extends Bundle {
  val stall = Bool()
}

class BypassingLogicInputs_WB(xprlen: Int) extends Bundle {
  val rd = Valid(new RegIndexWithValue(xprlen))
  val dcache_requested_but_not_valid = Bool()
}

class BypassingLogicIO_ID(xprlen: Int) extends Bundle {
  val in = Input(Valid(new BypassingLogicInputs_ID))
  val out = Output(new BypassingLogicOutputs_ID(xprlen))
}

class BypassingLogicIO_EX(xprlen: Int) extends Bundle {
  val in = Input(Valid(new BypassingLogicInputs_EX(xprlen)))
  val out = Output(new BypassingLogicOutputs_EX)
}

class BypassingLogicIO_WB(xprlen: Int) extends Bundle {
  val in = Input(Valid(new BypassingLogicInputs_WB(xprlen)))
  val out = Output(new BypassingLogicOutputs_EX)
}

class BypassingUnit(xprlen: Int) extends Module {
  val io = IO(new Bundle{
    val ID = new BypassingLogicIO_ID(xprlen)
    val EX = new BypassingLogicIO_EX(xprlen)
    val WB = new BypassingLogicIO_WB(xprlen)
  })

  io.WB.out.stall := (io.WB.in.bits.dcache_requested_but_not_valid && io.WB.in.valid)
  // Send stall from WB to EX
  io.EX.out.stall := (io.WB.out.stall || io.EX.in.bits.dcache_req_not_ready_and_has_mem_inst_in_EX_stage) && io.EX.in.valid

  // Bypass rs1 value from WB, EX to ID
  val WB_rd_index_and_ID_rs1_index_matches_and_not_zero = ((io.ID.in.bits.rs1_index.bits =/= 0.U(5.W)) && (io.ID.in.bits.rs1_index.bits === io.WB.in.bits.rd.bits.index))
  val EX_rd_index_and_ID_rs1_index_matches_and_not_zero = ((io.ID.in.bits.rs1_index.bits =/= 0.U(5.W)) && (io.ID.in.bits.rs1_index.bits === io.EX.in.bits.rd.bits.index))
  io.ID.out.rs1_value.bits := Mux(EX_rd_index_and_ID_rs1_index_matches_and_not_zero && io.EX.in.bits.rd.valid, io.EX.in.bits.rd.bits.value, io.WB.in.bits.rd.bits.value)
  io.ID.out.rs1_value.valid := io.ID.in.bits.rs1_index.valid && io.ID.in.valid && MuxCase(false.B,
    Seq(
      // EXステージの命令がrdへ書き込む命令であり、かつIDステージのrs1とレジスタ番号が一致する場合、EXステージのrdの値をフォワーディングする
      // メモリロード命令であればWBステージで正しい値が出てくる
      (EX_rd_index_and_ID_rs1_index_matches_and_not_zero && io.EX.in.bits.rd.valid && io.EX.in.valid) -> io.EX.in.bits.rd.valid,
      (WB_rd_index_and_ID_rs1_index_matches_and_not_zero && io.WB.in.bits.rd.valid && io.WB.in.valid) -> io.WB.in.bits.rd.valid
    )
  )

  val WB_rd_index_and_ID_rs2_index_matches_and_not_zero = ((io.ID.in.bits.rs2_index.bits =/= 0.U(5.W)) && (io.ID.in.bits.rs2_index.bits === io.WB.in.bits.rd.bits.index))
  val EX_rd_index_and_ID_rs2_index_matches_and_not_zero = ((io.ID.in.bits.rs2_index.bits =/= 0.U(5.W)) && (io.ID.in.bits.rs2_index.bits === io.EX.in.bits.rd.bits.index))
  io.ID.out.rs2_value.bits := Mux(EX_rd_index_and_ID_rs2_index_matches_and_not_zero && io.EX.in.bits.rd.valid, io.EX.in.bits.rd.bits.value, io.WB.in.bits.rd.bits.value)
  io.ID.out.rs2_value.valid := io.ID.in.bits.rs2_index.valid && io.ID.in.valid && MuxCase(false.B,
      Seq(
        (EX_rd_index_and_ID_rs2_index_matches_and_not_zero && io.EX.in.bits.rd.valid && io.EX.in.valid) -> io.EX.in.bits.rd.valid,
        (WB_rd_index_and_ID_rs2_index_matches_and_not_zero && io.WB.in.bits.rd.valid && io.WB.in.valid) -> io.WB.in.bits.rd.valid
      )
    )
  // ID, EXステージが共にvalidであり、かつEXステージがストールまたはEXステージのrdからIDステージのrs1またはrs2へ値をフォワーディングする必要があるが、
  // EXステージのrdレジスタの値がvalidでない場合（例：メモリロード等）にストール
  // TODO: This signal is acting sus in sb test
  io.ID.out.stall := io.ID.in.valid && io.EX.in.valid && (io.EX.out.stall ||
    (((EX_rd_index_and_ID_rs1_index_matches_and_not_zero && io.ID.in.bits.rs1_index.valid) ||
      (EX_rd_index_and_ID_rs2_index_matches_and_not_zero && io.ID.in.bits.rs2_index.valid)) &&
      !io.EX.in.bits.rd.valid))
}

object BypassingUnit extends App {
  def apply(xprlen: Int): BypassingUnit = new BypassingUnit(xprlen)
  (new chisel3.stage.ChiselStage).emitVerilog(apply(RISCV_Consts.XLEN), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}