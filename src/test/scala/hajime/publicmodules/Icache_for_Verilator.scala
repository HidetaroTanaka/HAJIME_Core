package hajime.publicmodules

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.axiIO.AXI4liteIO

class Icache_for_Verilator(memsize: Int = 8192) extends Module {
  val io = IO(new Bundle {
    val axi = Flipped(new AXI4liteIO(addr_width = 64, data_width = 32))
    val initialising = Input(Bool())
  })
  io := DontCare

  val mem = SyncReadMem(memsize, UInt(32.W))
}
