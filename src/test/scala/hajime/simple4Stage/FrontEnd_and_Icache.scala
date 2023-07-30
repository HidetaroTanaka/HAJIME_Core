package hajime.simple4Stage

import chisel3._
import hajime.publicmodules._
import hajime.common._

class FrontEnd_and_Icache(implicit params: HajimeCoreParams) extends Module{
  val io = IO(new Bundle{
    val cpu = new FrontEndCpuIO()
    val reset_vector = Input(UInt(64.W))
  })
  val frontend = Module(new Frontend())
  val iCache = Module(new Icache_model(hexfileName = "src/main/resources/rv64ui/add_inst.hex"))
  frontend.io.icache_axi4lite <> iCache.io
  frontend.io.reset_vector := io.reset_vector
  frontend.io.cpu <> io.cpu
}
