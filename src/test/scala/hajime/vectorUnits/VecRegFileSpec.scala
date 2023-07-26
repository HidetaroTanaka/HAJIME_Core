package hajime.vectorUnits

import chisel3._
import chiseltest._
import hajime.common.HajimeCoreParams
import org.scalatest.flatspec._

import scala.util.Random

class VecRegFileSpec extends AnyFlatSpec with ChiselScalatestTester {
  Random.setSeed(0)
  it should "Write and Read VecRegFile" in {
    val params = HajimeCoreParams()
    test(VecRegFile(params)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      println("a")
      dut.io.sew.poke("b000".U(3.W))
      dut.io.rs1.poke(0.U)
      dut.io.rs1_index.poke(0.U)
      dut.io.rs2.poke(0.U)
      dut.io.rs2_index.poke(0.U)
      dut.io.req.valid.poke(true.B)
      dut.io.req.bits.rd.poke(3.U)
      dut.io.req.bits.sew.poke("b000".U)
      println("a")
      for(i <- 0 until params.vlen/8) {
        println(s"Write Index: $i")
        dut.io.req.bits.data.poke(Random.nextInt(256))
        dut.io.req.bits.index.poke(i.U)
        dut.clock.step()
      }
      dut.io.rs1.poke(3.U)
      for(i <- 0 until params.vlen/8) {
        println(s"Read Index: $i")
        dut.io.rs1_index.poke(i.U)
        println(s"Read Data: ${dut.io.rs1_out.peekInt()}")
        dut.clock.step()
      }
    }
  }
}
