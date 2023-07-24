package hajime.publicmodules

import chisel3._
import chiseltest._
import hajime.common.HajimeCoreParams
import chisel3.experimental.BundleLiterals._
import org.scalatest.flatspec._

import scala.util.Random

class MultiplierTest extends AnyFlatSpec with ChiselScalatestTester {
  Random.setSeed(0)
  def generate_Int64RandomHexString(): String = {
    IndexedSeq.fill(16)(Random.nextInt(16).toHexString).reduce(_ + _)
  }
  val multiplicand_array = (0 until HajimeCoreParams().robEntries).map(_ => BigInt("0000000000000000" + generate_Int64RandomHexString(), 16))
  // multiplicand_array.foreach(x => println(x))
  val multiplier_array = (0 until HajimeCoreParams().robEntries).map(_ => BigInt("0000000000000000" + generate_Int64RandomHexString(), 16))
  // multiplier_array.foreach(x => println(x))
  val answer_array = (multiplicand_array zip multiplier_array).map{
    case (num1, num2) => num1 * num2
  }
  var result_array: IndexedSeq[BigInt] = IndexedSeq()
  var result_tag_array: IndexedSeq[BigInt] = IndexedSeq()
  it should s"perform pipelined multiplication" in {
    test(Multiplier(HajimeCoreParams())).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for (((num1, num2), i) <- (multiplicand_array zip multiplier_array).zipWithIndex) {
        dut.io.req.valid.poke(true.B)
        dut.io.req.bits.multiplicand.bits.poke(num1.U(64.W))
        dut.io.req.bits.multiplicand.signed.poke(false.B)
        dut.io.req.bits.multiplier.bits.poke(num2.U(64.W))
        dut.io.req.bits.multiplier.signed.poke(false.B)
        dut.io.req.bits.tag.poke(i.U)
        dut.io.resp.ready.poke(true.B)
        dut.clock.step()
        if(dut.io.resp.valid.peekBoolean()) {
          result_array = result_array :+ dut.io.resp.bits.result.peekInt()
          result_tag_array = result_tag_array :+ dut.io.resp.bits.tag.peekInt()
        }
      }
      dut.io.req.valid.poke(false.B)
      dut.io.req.bits.multiplicand.bits.poke(0.U(64.W))
      dut.io.req.bits.multiplier.bits.poke(0.U(64.W))
      dut.io.req.bits.tag.poke(0.U)
      while(dut.io.resp.valid.peekBoolean()) {
        dut.clock.step()
        result_array = result_array :+ dut.io.resp.bits.result.peekInt()
        result_tag_array = result_tag_array :+ dut.io.resp.bits.tag.peekInt()
      }
      def bigIntToString32format(bigInt: BigInt): String = {
        String.format("%32s", bigInt.toString(16).toUpperCase).replace(' ', '0')
      }
      result_tag_array.lazyZip(result_array.map(bigIntToString32format)).lazyZip(answer_array.map(bigIntToString32format)).toIndexedSeq.foreach {
        case (tag, result, answer) => {
          println(s"tag: $tag, result: 0x$result, answer: 0x$answer")
          assert(result == answer)
        }
      }
    }
  }
}
