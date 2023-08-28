package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common._

class VecRegFileReq(implicit params: HajimeCoreParams) extends Bundle {
  val vd = UInt(5.W)
  val sew = UInt(3.W)
  val index = UInt(log2Up(params.vlen/8).W)
  val data = UInt(params.xprlen.W)
  val vm = Bool()
}

class VecRegFileIO(implicit params: HajimeCoreParams) extends Bundle {
  val sew = Input(UInt(3.W))
  val readIndex = Input(UInt(log2Up(params.vlen/8).W))
  val vs1 = Input(UInt(5.W))
  val vs1Out = Output(UInt(params.xprlen.W))
  val vs2 = Input(UInt(5.W))
  val vs2Out = Output(UInt(params.xprlen.W))
  val vm = Output(Bool())
  val req = Flipped(ValidIO(new VecRegFileReq()))
}

class VecRegFile(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new VecRegFileIO())

  val vrf = Mem(32, Vec(params.vlen, Bool()))

  io.vs1Out := MuxLookup(io.sew, 0.U)(
    (0 until 4).map(
      i => i.U -> Cat((0 until (8 << i)).reverse.map(j => vrf.read(io.vs1)((io.readIndex << (i + 3)).asUInt + j.U)))
    )
  )
  io.vs2Out := MuxLookup(io.sew, 0.U)(
    (0 until 4).map(
      i => i.U -> Cat((0 until (8 << i)).reverse.map(j => vrf.read(io.vs2)((io.readIndex << (i + 3)).asUInt + j.U)))
    )
  )

  when(io.req.valid) {
    val internalWriteData = VecInit((0 until params.vlen).map(_ => false.B))
    val internalWriteMask = VecInit((0 until params.vlen).map(_ => false.B))
    when(io.req.bits.vm) {
      internalWriteData(io.req.bits.index) := io.req.bits.data(0)
      internalWriteMask(io.req.bits.index) := true.B
    } .otherwise {
      for(i <- 0 until 4) {
        switch(io.req.bits.sew) {
          is(i.U) {
            for(j <- 0 until (8 << i)) {
              internalWriteData((io.req.bits.index << (i+3)).asUInt + j.U) := io.req.bits.data(j)
              internalWriteMask((io.req.bits.index << (i+3)).asUInt + j.U) := true.B
            }
          }
        }
      }
    }
    vrf.write(io.req.bits.vd, internalWriteData, internalWriteMask)
  }

  io.vm := vrf.read(0.U)(io.readIndex)
}

object VecRegFile extends App {
  def apply(implicit params: HajimeCoreParams): VecRegFile = new VecRegFile()
  (new chisel3.stage.ChiselStage).emitVerilog(apply(HajimeCoreParams()), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}