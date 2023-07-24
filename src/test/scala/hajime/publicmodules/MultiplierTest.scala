package hajime.publicmodules

import chisel3._
import chiseltest._
import hajime.common.HajimeCoreParams
import org.scalatest.exceptions.TestFailedException
import org.scalatest.flatspec._

import scala.util.Random

class MultiplierTest extends AnyFlatSpec with ChiselScalatestTester {
  if(false) {
    it should "multiply 8 bit correctly" in {
      test(Multiplier_nxn(8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        for (x <- 0 to 0xFF; y <- 0 to 0xFF) {
          dut.io.multiplicand.poke(x.U(8.W))
          dut.io.multiplier.poke(y.U(8.W))
          println(s"${x} * ${y} = ${dut.io.out.peekInt()}")
          try {
            dut.io.out.expect((x * y).U(16.W))
            dut.clock.step()
          } catch {
            case _: TestFailedException => println(s"test failed at ${x} * ${y}")
          }
        }
      }
    }
  }
  Random.setSeed(0)
  def generate_Int64RandomHexString(): String = {
    IndexedSeq.fill(16)(Random.nextInt(16).toHexString).reduce(_ + _)
  }
  for(i <- 0 until 8) {
    val multiplicand_string = generate_Int64RandomHexString()
    val multiplier_string = generate_Int64RandomHexString()
    val multiplicand = BigInt("0000000000000000" + multiplicand_string, 16)
    val multiplier = BigInt("0000000000000000" + multiplier_string, 16)
    it should s"multiply 0x${multiplicand_string.toUpperCase} and 0x${multiplier_string.toUpperCase} correctly for test $i" in {
      test(Multiplier(HajimeCoreParams())).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        var accumulate = BigInt("00000000000000000000000000000000", 16)
        // println((multiplicand * multiplier).toString(16).toUpperCase)
        dut.io.req.valid.poke(true.B)
        dut.io.req.bits.multiplicand.bits.poke(multiplicand.U(64.W))
        dut.io.req.bits.multiplicand.signed.poke(false.B)
        dut.io.req.bits.multiplier.bits.poke(multiplier.U(64.W))
        dut.io.req.bits.multiplier.signed.poke(false.B)
        dut.io.req.bits.tag.poke(0.U)
        dut.io.resp.ready.poke(true.B)
        dut.clock.step()
        dut.io.req.valid.poke(false.B)
        dut.io.req.bits.multiplicand.bits.poke(0.U)
        dut.io.req.bits.multiplier.bits.poke(0.U)
        for (i <- 0 until 6) {
          accumulate = accumulate + ((multiplicand * ((multiplier >> (8 * i)) & BigInt(0xFF))) << (8 * i))
          println(s"Stage $i: 0x${accumulate.toString(16).toUpperCase}")
          println(s"stageRegisters($i).result: 0x${dut.io.debug.get(i).bits.result.peekInt().toString(16).toUpperCase}")
          dut.io.debug.get(i).bits.result.expect(accumulate)
          dut.clock.step()
        }
        accumulate = accumulate + (multiplicand * ((multiplier >> 48) & BigInt(0xFF)) << 48)
        println(s"Stage 6: 0x${accumulate.toString(16).toUpperCase}")
        println(s"stageRegisters(6).result: 0x${dut.io.debug.get(6).bits.result.peekInt().toString(16).toUpperCase}")
        dut.io.debug.get(6).bits.result.expect(accumulate)
        accumulate = accumulate + (multiplicand * ((multiplier >> 56) & BigInt(0xFF)) << 56)
        println(s"Result: 0x${accumulate.toString(16).toUpperCase}")
        println(s"result = 0x${dut.io.resp.bits.result.peekInt().toString(16).toUpperCase}")
        dut.io.resp.bits.result.expect(multiplicand * multiplier)
      }
    }
  }
}
