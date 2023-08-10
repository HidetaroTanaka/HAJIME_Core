package hajime.publicmodules
import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common.COMPILE_CONSTANTS
import chiseltest._
import org.scalatest.flatspec._
import scala.io._

class Dcache_for_Verilator(memsize: Int = 8192) extends Module {
  val io = IO(Flipped(new AXI4liteIO(addr_width = 64, data_width = 64)))
}
