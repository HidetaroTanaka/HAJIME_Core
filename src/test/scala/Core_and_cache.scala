import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.common.HajimeCoreParams
import hajime.publicmodules._
import hajime.simple4Stage._

class Core_and_cache(dcache_hexfilename: String, icache_memsize: Int = 8192, dcache_memsize: Int = 8192, tohost: Int = 0x10000000) extends Module {
  val params = HajimeCoreParams()
  val io = IO(new Bundle{
    val reset_vector = Input(UInt(64.W))
    val toHost = ValidIO(UInt(64.W))
    val debug_io = Output(debugIO(params))
    val icache_initialising = Input(Bool())
  })

  val core = Module(Core(params))
  val icache = Module(Icache_for_Verilator(memsize = icache_memsize))
  val dcache = Module(new Dcache_model(dcacheBaseAddr = 0x00004000, tohost = tohost, hexfileName = dcache_hexfilename, memsize = dcache_memsize))

  core.io.icache_axi4lite <> icache.io.axi
  icache.io.axi := io.icache_initialising
  core.io.dcache_axi4lite <> dcache.io

  core.io.reset_vector := io.reset_vector
  core.io.hartid := 0.U
  io.toHost := dcache.debug
  io.debug_io := core.io.debug_io.get
}

object Core_and_cache extends App {
  def apply(icache_hexfilename: String, dcache_hexfilename: String, icache_memsize: Int, dcache_memsize: Int, tohost: Int): Core_and_cache = new Core_and_cache(dcache_hexfilename, icache_memsize, dcache_memsize, tohost)
  (new ChiselStage).emitVerilog(apply(icache_hexfilename = null, dcache_hexfilename = null, icache_memsize = 8192, dcache_memsize = 8192, tohost = 0x10000000), args = hajime.common.COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
