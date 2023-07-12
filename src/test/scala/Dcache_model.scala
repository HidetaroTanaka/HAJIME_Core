import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import hajime.axiIO.AXI4liteIO
import hajime.common._

// This actually isn't compatible with AXI4-Lite, but anyway
class Dcache_model(dcacheBaseAddr: Int, tohost: Int, hexfileName: String) extends Module {
  val io = IO(Flipped(new AXI4liteIO(addr_width = 64, data_width = 64)))
  val debug = IO(Output(UInt(64.W)))
  val debug_reg = RegInit(0.U(64.W))
  debug := debug_reg

  io := DontCare

  io.aw.ready := true.B
  io.ar.ready := true.B
  io.w.ready := true.B

  val internalReadAddr = io.ar.bits.addr - dcacheBaseAddr.U
  val internalWriteAddr = io.aw.bits.addr - dcacheBaseAddr.U
  // 0x000 ~ 0xFFF
  // D$ Address space from Core: 0x00004000 ~ 0x00004FFF
  val mem = SyncReadMem(4096, UInt(8.W))
  if(hexfileName != null) loadMemoryFromFile(mem, hexfileName)

  val readData_vec = Wire(Vec(8, UInt(8.W)))
  readData_vec.foreach(_ := 0.U(8.W))

  // Read
  when(io.ar.valid) {
    for((d,i) <- readData_vec.zipWithIndex) {
      d := mem.read(internalReadAddr + i.U)
    }
  } .elsewhen(io.aw.valid && io.w.valid) {
    val strb = io.w.bits.strb.asBools
    for((mask,i) <- strb.zipWithIndex) {
      when(mask) {
        mem.write(internalWriteAddr + i.U, io.w.bits.data(7 + i * 8, i * 8))
      }
    }
    when(io.aw.bits.addr === tohost.U) {
      debug_reg := io.w.bits.data
    }
  }

  io.r.bits.data := Cat(readData_vec.reverse)
  io.r.valid := RegNext(io.ar.valid)

  io.b.valid := RegNext(io.aw.valid && io.w.valid)
}

object Dcache_model extends App {
  def apply(dcacheBaseAddr: Int, tohost: Int, hexfileName: String) = new Dcache_model(dcacheBaseAddr, tohost, hexfileName)
  (new ChiselStage).emitVerilog(new Dcache_model(dcacheBaseAddr = 0x00004000, tohost = 0x00001000, hexfileName = null), args = hajime.common.COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
