package hajime.vectorUnits

import chisel3._
import chisel3.util._
import hajime.common._

class VectorRegister(implicit params: HajimeCoreParams) extends Bundle {
  val e8 = Vec(params.vlen / 8, UInt(8.W))
  def read(sew: UInt, index: UInt): UInt = {
    MuxLookup(sew, 0.U)((0 until 4).map(
      i => i.U(3.W) -> Cat((0 until (1 << i)).reverse.map(j => e8((index << i.U) + j.U)))
    ))
  }
  def write(sew: UInt, index: UInt, data: UInt): Unit = {
    require(data.getWidth == params.xprlen)
    for(i <- 0 until 4) {
      when(sew === i.U(3.W)) {
        for(j <- 0 until (1 << i)) {
          e8((index << i.U) + j.U) := data(j*8+7, j*8)
        }
      }
    }
  }
}

class VecRegFileReq(implicit params: HajimeCoreParams) extends Bundle {
  val rd = UInt(5.W)
  val sew = UInt(3.W)
  val index = UInt(log2Up(params.vlen/8).W)
  val data = UInt(params.xprlen.W)
}

class VecRegFileIO(implicit params: HajimeCoreParams) extends Bundle {
  val sew = Input(UInt(3.W))
  val rs1 = Input(UInt(5.W))
  val rs1_index = Input(UInt(log2Up(params.vlen/8).W))
  val rs1_out = Output(UInt(params.xprlen.W))
  val rs2 = Input(UInt(5.W))
  val rs2_index = Input(UInt(log2Up(params.vlen/8).W))
  val rs2_out = Output(UInt(params.xprlen.W))
  val vm = Output(UInt((params.vlen/8).W))
  val req = Flipped(ValidIO(new VecRegFileReq()))
}

class VecRegFile(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new VecRegFileIO())
  val vector_regfile = Reg(Vec(32, new VectorRegister()))

  io.rs1_out := vector_regfile(io.rs1).read(sew = io.sew, index = io.rs1_index)
  io.rs2_out := vector_regfile(io.rs2).read(sew = io.sew, index = io.rs2_index)

  when(io.req.valid) {
    vector_regfile(io.req.bits.rd).write(sew = io.req.bits.sew, index = io.req.bits.index, data = io.req.bits.data)
  }

  io.vm := Cat(vector_regfile(0).e8)
}

object VecRegFile extends App {
  def apply(implicit params: HajimeCoreParams): VecRegFile = new VecRegFile()
  (new chisel3.stage.ChiselStage).emitVerilog(apply(HajimeCoreParams()), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}