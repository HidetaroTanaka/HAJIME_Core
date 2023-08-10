package hajime.simple4Stage

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common._
import hajime.publicmodules._

class Core_and_cache(dcache_hexfilename: String, icache_memsize: Int = 8192, dcache_memsize: Int = 8192, tohost: Int = 0x10000000) extends Module {
  val params = HajimeCoreParams()
  val io = IO(new Bundle{
    val reset_vector = Input(UInt(64.W))
    val hartid = Input(UInt(64.W))
    val toHost = ValidIO(UInt(64.W))
    val debug_io = Output(debugIO(params))
    val icache_initialising = Input(Bool())
    val imem_initialiseAXI = Flipped(new AXI4liteIO(addr_width = 64, data_width = 32))
  })

  val core = withReset(io.icache_initialising || reset.asBool) {
    Module(Core(params))
  }
  val icache = Module(Icache_for_Verilator(memsize = icache_memsize))
  val dcache = Module(new Dcache_model(dcacheBaseAddr = 0x00004000, tohost = tohost, hexfileName = dcache_hexfilename, memsize = dcache_memsize))

  icache.io.axi := DontCare
  io.imem_initialiseAXI := DontCare
  core.io.icache_axi4lite := DontCare
  when(io.icache_initialising) {
    icache.io.axi <> io.imem_initialiseAXI
    core.io.icache_axi4lite.ar.ready := false.B
    core.io.icache_axi4lite.aw.ready := false.B
    core.io.icache_axi4lite.w.ready := false.B
  } .otherwise {
    icache.io.axi <> core.io.icache_axi4lite
    io.imem_initialiseAXI.ar.ready := false.B
    io.imem_initialiseAXI.aw.ready := false.B
    io.imem_initialiseAXI.w.ready := false.B
  }
  core.io.dcache_axi4lite <> dcache.io

  core.io.reset_vector := io.reset_vector
  core.io.hartid := io.hartid
  io.toHost := dcache.debug
  io.debug_io := core.io.debug_io.get
}

object Core_and_cache extends App {
  def apply(dcache_hexfilename: String, icache_memsize: Int, dcache_memsize: Int, tohost: Int): Core_and_cache = new Core_and_cache(dcache_hexfilename, icache_memsize, dcache_memsize, tohost)
  ChiselStage.emitSystemVerilogFile(apply(dcache_hexfilename = null, icache_memsize = 8192, dcache_memsize = 8192, tohost = 0x10000000), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
