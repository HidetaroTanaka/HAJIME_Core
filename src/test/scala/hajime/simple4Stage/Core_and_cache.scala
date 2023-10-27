package hajime.simple4Stage

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common._
import hajime.publicmodules._
import hajime.vectormodules.VectorCpu

class Core_and_cache[T <: CpuModule](icache_memsize: Int = 8192, dcache_memsize: Int = 8192, tohost: Int = 0x10000000, useVector: Boolean = false, cpu: Class[T]) extends Module {
  implicit val params = HajimeCoreParams(useException = true, useVector = if(cpu == classOf[VectorCpu] && !useVector) throw new Exception("useVector is false") else useVector)
  val io = IO(new Bundle{
    val reset_vector = Input(UInt(64.W))
    val hartid = Input(UInt(64.W))
    val toHost = ValidIO(UInt(64.W))
    val debug_io = Output(new debugIO())
    val icache_initialising = Input(Bool())
    val dcache_initialising = Input(Bool())
    val imem_initialiseAXI = Flipped(new AXI4liteIO(addr_width = 64, data_width = 32))
    val dmem_initialiseAXI = Flipped(new AXI4liteIO(addr_width = 64, data_width = 64))
  })

  val core = withReset(io.icache_initialising || io.dcache_initialising || reset.asBool) {
    Module(new Core(cpu))
  }
  val icache = Module(Icache_for_Verilator(memsize = icache_memsize))
  val dcache = Module(Dcache_for_Verilator(dcacheBaseAddr = 0x00004000, tohost = tohost, memsize = dcache_memsize))

  icache.io := DontCare
  io.imem_initialiseAXI := DontCare
  core.io.icache_axi4lite := DontCare
  dcache.io := DontCare
  io.dmem_initialiseAXI := DontCare
  core.io.dcache_axi4lite := DontCare

  when(io.icache_initialising) {
    icache.io <> io.imem_initialiseAXI
    core.io.icache_axi4lite.ar.ready := false.B
    core.io.icache_axi4lite.aw.ready := false.B
    core.io.icache_axi4lite.w.ready := false.B
  } .otherwise {
    icache.io <> core.io.icache_axi4lite
    io.imem_initialiseAXI.ar.ready := false.B
    io.imem_initialiseAXI.aw.ready := false.B
    io.imem_initialiseAXI.w.ready := false.B
  }

  when(io.dcache_initialising) {
    dcache.io <> io.dmem_initialiseAXI
    core.io.dcache_axi4lite.ar.ready := false.B
    core.io.dcache_axi4lite.aw.ready := false.B
    core.io.dcache_axi4lite.w.ready := false.B
  } .otherwise {
    dcache.io <> core.io.dcache_axi4lite
    io.dmem_initialiseAXI.ar.ready := false.B
    io.dmem_initialiseAXI.aw.ready := false.B
    io.dmem_initialiseAXI.w.ready := false.B
  }

  core.io.reset_vector := io.reset_vector
  core.io.hartid := io.hartid
  io.toHost := dcache.debug
  io.debug_io := core.io.debug_io.get
}

object Core_and_cache extends App {
  def apply[T <: CpuModule](icache_memsize: Int, dcache_memsize: Int, tohost: Int, useVector: Boolean = false, cpu: Class[T]): Core_and_cache[T] = new Core_and_cache(icache_memsize, dcache_memsize, tohost, useVector, cpu)
  ChiselStage.emitSystemVerilogFile(apply(icache_memsize = 8192, dcache_memsize = 8192, tohost = 0x10000000, useVector = false, classOf[CPU]), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
