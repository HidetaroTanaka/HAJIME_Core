package hajime.vectormodules

import chisel3._
import chiseltest._
import hajime.common.HajimeCoreParams
import org.scalatest.flatspec._

import scala.util.Random

class VecRegFileSpec extends AnyFlatSpec with ChiselScalatestTester {
  def readWriteTest(dut: VecRegFile, sew: Int, vlen: Int): Unit = {
    var input_array: IndexedSeq[String] = Nil.toIndexedSeq
    println(s"sew=$sew (e${1 << sew}) test:")
    dut.io.sew.poke(sew.U(3.W))
    dut.io.vs1.poke(0.U)
    dut.io.readIndex.poke(0.U)
    dut.io.vs2.poke(0.U)
    dut.io.req.valid.poke(true.B)
    dut.io.req.bits.vd.poke(3.U)
    dut.io.req.bits.sew.poke(sew.U)
    for (i <- 0 until vlen / (8 << sew)) {
      println(s"Write Index: $i")
      // e8 -> 256, e16 -> 65536, e32 -> 2^32, e64 -> 2^64
      // 0 -> 2, 1 -> 4, 2 -> 8, 3 -> 16
      val input = (0 until (2 << sew)).map(_ => Random.nextInt(16).toHexString).reduce(_ + _)
      println(s"Write Data: $input")
      dut.io.req.bits.data.poke(s"h$input".U)
      input_array :+= input
      dut.io.req.bits.index.poke(i.U)
      dut.clock.step()
    }
    dut.io.vs1.poke(3.U)
    dut.io.req.valid.poke(false.B)
    for (i <- 0 until vlen / (8 << sew)) {
      println(s"Read Index: $i")
      dut.io.readIndex.poke(i.U)
      println(s"Read Data: ${dut.io.vs1Out.peekInt().toString(16)}")
      dut.io.vs1Out.expect(s"h${input_array(i)}".U)
      dut.clock.step()
    }
    println()
  }
  Random.setSeed(0)
  it should "Write and Read VecRegFile" in {
    val params = HajimeCoreParams()
    test(VecRegFile(params)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      for(sew <- 0 until 4) readWriteTest(dut, sew, params.vlen)
    }
  }
}
