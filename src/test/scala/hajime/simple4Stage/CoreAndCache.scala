package hajime.simple4Stage

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common._
import hajime.publicmodules._
import hajime.vectormodules.VectorCpu

class CoreAndCache[T <: CpuModule](iCacheMemsize: Int = 8192, dCacheMemsize: Int = 8192, tohost: Int = 0x10000000, useVector: Boolean = false, cpu: Class[T]) extends Module {
  implicit val params = HajimeCoreParams(useException = true, useVector = if(cpu == classOf[VectorCpu] && !useVector) throw new Exception("useVector is false") else useVector)
  val io = IO(new Bundle{
    val resetVector = Input(UInt(64.W))
    val hartid = Input(UInt(64.W))
    val toHost = ValidIO(UInt(64.W))
    val debugIO = Output(new debugIO())
    val iCacheInitialising = Input(Bool())
    val dCacheInitialising = Input(Bool())
    val iMemInitialiseAxi = Flipped(new AXI4liteIO(addrWidth = 64, dataWidth = 32))
    val dMemInitialiseAxi = Flipped(new AXI4liteIO(addrWidth = 64, dataWidth = 64))
  })

  val core = withReset(io.iCacheInitialising || io.dCacheInitialising || reset.asBool) {
    Module(new Core(cpu))
  }
  val icache = Module(IcacheForVerilator(memsize = iCacheMemsize))
  val dcache = Module(DcacheForVerilator(dcacheBaseAddr = 0x00004000, tohost = tohost, memsize = dCacheMemsize))

  icache.io := DontCare
  io.iMemInitialiseAxi := DontCare
  core.io.iCacheAxi4Lite := DontCare
  dcache.io := DontCare
  io.dMemInitialiseAxi := DontCare
  core.io.dCacheAxi4Lite := DontCare

  when(io.iCacheInitialising) {
    icache.io <> io.iMemInitialiseAxi
    core.io.iCacheAxi4Lite.ar.ready := false.B
    core.io.iCacheAxi4Lite.aw.ready := false.B
    core.io.iCacheAxi4Lite.w.ready := false.B
  } .otherwise {
    icache.io <> core.io.iCacheAxi4Lite
    io.iMemInitialiseAxi.ar.ready := false.B
    io.iMemInitialiseAxi.aw.ready := false.B
    io.iMemInitialiseAxi.w.ready := false.B
  }

  when(io.dCacheInitialising) {
    dcache.io <> io.dMemInitialiseAxi
    core.io.dCacheAxi4Lite.ar.ready := false.B
    core.io.dCacheAxi4Lite.aw.ready := false.B
    core.io.dCacheAxi4Lite.w.ready := false.B
  } .otherwise {
    dcache.io <> core.io.dCacheAxi4Lite
    io.dMemInitialiseAxi.ar.ready := false.B
    io.dMemInitialiseAxi.aw.ready := false.B
    io.dMemInitialiseAxi.w.ready := false.B
  }

  core.io.resetVector := io.resetVector
  core.io.hartid := io.hartid
  io.toHost := dcache.debug
  io.debugIO := core.io.debugIo.get
}

object CoreAndCache extends App {
  def apply[T <: CpuModule](icache_memsize: Int, dcache_memsize: Int, tohost: Int, useVector: Boolean = false, cpu: Class[T]): CoreAndCache[T] = new CoreAndCache(icache_memsize, dcache_memsize, tohost, useVector, cpu)
  ChiselStage.emitSystemVerilogFile(apply(icache_memsize = 8192, dcache_memsize = 8192, tohost = 0x10000000, useVector = false, classOf[Cpu]), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
