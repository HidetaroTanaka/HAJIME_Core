package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO._
import hajime.common._
import hajime.publicmodules._
import hajime.simple4Stage._

class VectorLdstUnitWithDcache(dcache_memsize: Int = 8192, tohost: Int = 0x10000000)(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new Bundle {
    val signalIn = Flipped(DecoupledIO(new Bundle {
      val scalar = new ScalarLdstSignalIn()
      val vector = new VectorExecUnitSignalIn()
    }))
    val readVrf = Flipped(new VecRegFileReadIO())
    val scalarResp = ValidIO(new LDSTResp())
    val vectorResp = Output(new VectorExecUnitDataOut())
    val toExWbReg = Output(Valid(new exWbIo()))
  })
  val dCacheInitialiseIO = IO(new Bundle {
    val valid = Input(Bool())
    val bits = Flipped(new AXI4liteIO(addrWidth = 64, dataWidth = 64))
  })

  val vecLdstUnit = withReset(dCacheInitialiseIO.valid || reset.asBool) {
    Module(new VectorLdstUnit())
  }
  val dCache = Module(new DcacheForVerilator(dcacheBaseAddr = 0x00004000, tohost = tohost, memsize = dcache_memsize))

  dCacheInitialiseIO := DontCare
  vecLdstUnit.io := DontCare
  dCache.io := DontCare

  vecLdstUnit.io.signalIn <> io.signalIn
  vecLdstUnit.io.readVrf <> io.readVrf
  io.scalarResp := vecLdstUnit.io.scalarResp
  io.vectorResp := vecLdstUnit.io.vectorResp
  io.toExWbReg := vecLdstUnit.io.toExWbReg

  when(dCacheInitialiseIO.valid) {
    dCache.io <> dCacheInitialiseIO.bits
  } otherwise {
    dCache.io <> vecLdstUnit.io.dcache
  }
}

object VectorLdstUnitWithDcache extends App {
  implicit val params = HajimeCoreParams()
  ChiselStage.emitSystemVerilogFile(new VectorLdstUnitWithDcache(), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
