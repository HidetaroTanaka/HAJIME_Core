package hajime.publicmodules

import chisel3._
import chiseltest._
import hajime.common.HajimeCoreParams
import chisel3.experimental.BundleLiterals._
import org.scalatest.flatspec._

import scala.util.Random

object Functions {
  def generate_Int64RandomHexString(): String = {
    IndexedSeq.fill(16)(Random.nextInt(16).toHexString).reduce(_ + _)
  }

  def generate_Int64RandomHexString(n: Int): String = {
    val number_of_zeros = 16 - n
    (0 until 16).map(i => if(i < number_of_zeros) "0" else Random.nextInt(16).toHexString).reduce(_ + _)
  }
  def bigIntToString32format(bigInt: BigInt): String = {
    String.format("%32s", bigInt.toString(16).toUpperCase).replace(' ', '0')
  }
}

import Functions._

class MultiplierTest extends AnyFlatSpec with ChiselScalatestTester {
  Random.setSeed(0)
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
          result_array :+= dut.io.resp.bits.result.peekInt()
          result_tag_array :+= dut.io.resp.bits.tag.peekInt()
        }
      }
      dut.io.req.valid.poke(false.B)
      dut.io.req.bits.multiplicand.bits.poke(0.U(64.W))
      dut.io.req.bits.multiplier.bits.poke(0.U(64.W))
      dut.io.req.bits.tag.poke(0.U)
      while(dut.io.resp.valid.peekBoolean()) {
        dut.clock.step()
        result_array :+= dut.io.resp.bits.result.peekInt()
        result_tag_array :+= dut.io.resp.bits.tag.peekInt()
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

class NonPipelinedMultiplierSpec extends AnyFlatSpec with ChiselScalatestTester {
  Random.setSeed(0)
  val multiplicand_array = (0 until HajimeCoreParams().robEntries).map(_ => BigInt("0000000000000000" + generate_Int64RandomHexString(Random.nextInt(16)), 16))
  val multiplier_array = (0 until HajimeCoreParams().robEntries).map(_ => BigInt("0000000000000000" + generate_Int64RandomHexString(Random.nextInt(16)), 16))
  it should s"perform multiplication" in {
    test(NonPipelinedMultiplier(HajimeCoreParams())).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for((num1, num2, i) <- (multiplicand_array zip multiplier_array).zipWithIndex.map {
        case ((num1, num2), i) => (num1, num2, i)
      }) {
        dut.io.req.valid.poke(true.B)
        dut.io.req.bits.multiplicand.bits.poke(num1)
        dut.io.req.bits.multiplicand.signed.poke(false.B)
        dut.io.req.bits.multiplier.bits.poke(num2)
        dut.io.req.bits.multiplier.signed.poke(false.B)
        dut.io.req.bits.tag.poke(i.U)
        dut.io.resp.ready.poke(true.B)
        dut.clock.step()
        dut.io.resp.ready.poke(Random.nextBoolean().B)
        dut.io.req.valid.poke(false.B)
        dut.io.req.bits.multiplicand.bits.poke(0.U)
        dut.io.req.bits.multiplier.bits.poke(0.U)
        while(!(dut.io.resp.valid.peekBoolean() && dut.io.resp.ready.peekBoolean())) {
          dut.clock.step()
          dut.io.resp.ready.poke(Random.nextBoolean().B)
        }
        val result = bigIntToString32format(dut.io.resp.bits.result.peekInt())
        val answer = bigIntToString32format(num1 * num2)
        dut.io.resp.bits.result.expect(num1 * num2)
        println(s"result: 0x$result, answer: 0x$answer")
      }
    }
  }
}
