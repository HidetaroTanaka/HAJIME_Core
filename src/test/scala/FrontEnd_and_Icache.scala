import chisel3._
import hajime.simple4Stage._

class FrontEnd_and_Icache extends Module{
  val io = IO(new Bundle{
    val cpu = new FrontEndCpuIO(xprlen = 64)
    val reset_vector = Input(UInt(64.W))
  })
  val frontend = Module(new Frontend(xprlen = 64))
  val iCache = Module(new Icache_model)
  frontend.io.icache_axi4lite <> iCache.io
  frontend.io.reset_vector := io.reset_vector
  frontend.io.cpu <> io.cpu
}
