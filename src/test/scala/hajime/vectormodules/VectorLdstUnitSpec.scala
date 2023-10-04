package hajime.vectormodules

import chisel3._
import chiseltest._
import hajime.axiIO.AXI4liteIO
import hajime.common.HajimeCoreParams
import org.scalatest.flatspec._
import scala.io._

object MemInitializer {
  def initialiseMemWithAxi(filename: String, axi: AXI4liteIO, initialising: Bool, clock: Clock, baseAddr: Int): Unit = {
    axi.ar.bits.addr.poke(0.U)
    axi.ar.bits.prot.poke(0.U)
    axi.ar.valid.poke(false.B)
    axi.aw.bits.addr.poke(0.U)
    axi.aw.bits.prot.poke(0.U)
    axi.aw.valid.poke(false.B)
    axi.b.ready.poke(true.B)
    axi.r.ready.poke(true.B)
    axi.w.bits.data.poke(0.U)
    axi.w.bits.strb.poke(0.U)
    axi.w.valid.poke(false.B)
    initialising.poke(true.B)
    // initialise icache from file
    val fileSource = Source.fromFile(filename)
    for ((data, idx) <- fileSource.getLines().zipWithIndex) {
      axi.aw.bits.addr.poke((idx * 4 + baseAddr).U)
      axi.aw.valid.poke(true.B)
      axi.w.bits.data.poke(s"h$data".U(32.W))
      axi.w.bits.strb.poke(0xF.U(8.W))
      axi.w.valid.poke(true.B)
      clock.step()
    }
    fileSource.close()
    axi.aw.valid.poke(false.B)
    axi.w.valid.poke(false.B)
    initialising.poke(false.B)
  }
}

class VectorLdstUnitSpec extends AnyFlatSpec with ChiselScalatestTester {
  it should "vector memory access correctly" in {
    implicit val params = HajimeCoreParams()
    test(new VectorLdstUnitWithDcache()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      MemInitializer.initialiseMemWithAxi("src/main/resources/applications_rv64i/median_data.hex", dut.dCacheInitialiseIO.bits, dut.dCacheInitialiseIO.valid, dut.clock, 0x4000)
      // TODO: a
    }
  }
}
