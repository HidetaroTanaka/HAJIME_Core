package hajime.publicmodules

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import hajime.axiIO.AXI4liteIO

// This actually isn't compatible with AXI4-Lite, but anyway
class Dcache_model(dcacheBaseAddr: Int, tohost: Int, hexfileName: String, memsize: Int = 8192) extends Module {
  val io = IO(Flipped(new AXI4liteIO(addr_width = 64, data_width = 64)))
  val debug = IO(ValidIO(UInt(64.W)))

  val default_debug = Wire(Valid(UInt(64.W)))
  default_debug.bits := 0.U(64.W)
  default_debug.valid := false.B

  val debug_reg = RegInit(default_debug)
  debug := debug_reg
  debug_reg.valid := false.B

  io := DontCare

  io.aw.ready := true.B
  io.ar.ready := true.B
  io.w.ready := true.B

  val internalReadAddr = io.ar.bits.addr - dcacheBaseAddr.U
  val internalWriteAddr = io.aw.bits.addr - dcacheBaseAddr.U
  // 0x000 ~ 0x1FFF
  // D$ Address space from Core: 0x00004000 ~ 0x00005FFF
  val mem = SyncReadMem(memsize, UInt(8.W))
  if(hexfileName != null) loadMemoryFromFile(mem, hexfileName)

  val readData_vec = Wire(Vec(8, UInt(8.W)))
  readData_vec.foreach(_ := 0.U(8.W))
  // Read
  for ((d, i) <- readData_vec.zipWithIndex) {
    d := mem.read(internalReadAddr + i.U)
  }

  when(io.aw.valid && io.w.valid) {
    val strb = io.w.bits.strb.asBools
    for((mask,i) <- strb.zipWithIndex) {
      when(mask) {
        mem.write(internalWriteAddr + i.U, io.w.bits.data(7 + i * 8, i * 8))
      }
    }
    when(io.aw.bits.addr === tohost.U) {
      debug_reg.bits := io.w.bits.data
      debug_reg.valid := true.B
    }
  }

  io.r.bits.data := Cat(readData_vec.reverse)
  io.r.valid := RegNext(io.ar.valid)

  io.b.valid := RegNext(io.aw.valid && io.w.valid)
}

object Dcache_model extends App {
  def apply(dcacheBaseAddr: Int, tohost: Int, hexfileName: String, memsize: Int) = new Dcache_model(dcacheBaseAddr, tohost, hexfileName, memsize)
  (new ChiselStage).emitVerilog(new Dcache_model(dcacheBaseAddr = 0x00004000, tohost = 0x00001000, hexfileName = null, memsize = 8192), args = hajime.common.COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
