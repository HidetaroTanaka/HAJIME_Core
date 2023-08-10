package hajime.publicmodules
import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import chisel3.experimental.BundleLiterals._
import hajime.axiIO.AXI4liteIO
import hajime.common.COMPILE_CONSTANTS
import chiseltest._
import org.scalatest.flatspec._
import scala.io._

class Dcache_for_Verilator(dcacheBaseAddr: Int, tohost: Int, memsize: Int = 0x2000) extends Module {
  require(memsize % 8 == 0, s"memsize $memsize is not multiple of 8")

  val io = IO(Flipped(new AXI4liteIO(addr_width = 64, data_width = 64)))
  val debug = IO(ValidIO(UInt(32.W)))

  val debugToHost = RegInit(WireInit(Valid(UInt(32.W)).Lit(
    _.bits -> 0.U,
    _.valid -> false.B,
  )))
  debug := debugToHost

  io.aw.ready := true.B
  io.ar.ready := true.B
  io.w.ready := true.B

  val internalReadAddr = io.ar.bits.addr - dcacheBaseAddr.U
  val internalWriteAddr = io.aw.bits.addr - dcacheBaseAddr.U

  // 0x000 ~ 0x1FFC
  // D$ Address space from Core: 0x00004000 ~ 0x00005FFFC for word
  // doubleWord at 0x0000: 0x0000 ~ 0x0007
  val mem = SyncReadMem(memsize/8, Vec(8, UInt(8.W)))

  val readDataFromMem = Wire(chiselTypeOf(io.r.bits))
  readDataFromMem.data := Cat(mem.read(io.ar.bits.addr.head(61)).reverse)
  // Byte: 任意の
  // readDataFromMem.resp := Mux()
}
