import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.common.RISCV_Consts
import hajime.publicmodules.{Dcache_model, Icache_model}
import hajime.simple4Stage._

class Core_and_cache(icache_hexfilename: String, dcache_hexfilename: String, icache_memsize: Int = 8192, dcache_memsize: Int = 8192, tohost: Int = 0x10000000) extends Module {
  /*
  val io = IO(new Bundle{
    val reset_vector = Input(UInt(64.W))
    val toHost = ValidIO(UInt(64.W))
    val performance_counters = new Performance_CountersIO(64)
    val debug_io = Output(new debugIO(64))
  })

  val core = Module(Core(xprlen = 64, debug = true))
  val icache = Module(new Icache_model(hexfileName = icache_hexfilename, memsize = icache_memsize))
  val dcache = Module(new Dcache_model(dcacheBaseAddr = 0x00004000, tohost = tohost, hexfileName = dcache_hexfilename, memsize = dcache_memsize))

  core.io.icache_axi4lite <> icache.io
  core.io.dcache_axi4lite <> dcache.io

  core.io.reset_vector := io.reset_vector
  io.performance_counters := core.io.performance_counters
  io.toHost := dcache.debug
  io.debug_io := core.io.debug_io.get
   */
}

object Core_and_cache extends App {
  def apply(icache_hexfilename: String, dcache_hexfilename: String, icache_memsize: Int, dcache_memsize: Int, tohost: Int): Core_and_cache = new Core_and_cache(icache_hexfilename, dcache_hexfilename, icache_memsize, dcache_memsize, tohost)
  (new ChiselStage).emitVerilog(apply(icache_hexfilename = null, dcache_hexfilename = null, icache_memsize = 8192, dcache_memsize = 8192, tohost = 0x10000000), args = hajime.common.COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
