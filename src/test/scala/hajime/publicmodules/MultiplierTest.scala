package hajime.publicmodules

import chisel3._
import chiseltest._
import hajime.common.HajimeCoreParams
import org.scalatest.flatspec._

import scala.util.Random

object Functions {
  def generateInt64RandomHexString(): String = {
    IndexedSeq.fill(16)(Random.nextInt(16).toHexString).reduce(_ + _)
  }

  def generateInt64RandomHexString(n: Int): String = {
    val number_of_zeros = 16 - n
    (0 until 16).map(i => if(i < number_of_zeros) "0" else Random.nextInt(16).toHexString).reduce(_ + _)
  }
  def bigIntToString32format(bigInt: BigInt): String = {
    String.format("%32s", bigInt.toString(16).toUpperCase).replace(' ', '0')
  }
}

import hajime.publicmodules.Functions._

class MultiplierTest extends AnyFlatSpec with ChiselScalatestTester {
  Random.setSeed(0)
  val multiplicandArray = (0 until HajimeCoreParams().robEntries).map(_ => BigInt("0000000000000000" + generateInt64RandomHexString(), 16))
  // multiplicandArray.foreach(x => println(x))
  val multiplierArray = (0 until HajimeCoreParams().robEntries).map(_ => BigInt("0000000000000000" + generateInt64RandomHexString(), 16))
  // multiplierArray.foreach(x => println(x))
  val answerArray = (multiplicandArray zip multiplierArray).map{
    case (num1, num2) => num1 * num2
  }
  var resultArray: IndexedSeq[BigInt] = IndexedSeq()
  var resultTagArray: IndexedSeq[BigInt] = IndexedSeq()
  it should s"perform pipelined multiplication" in {
    test(Multiplier(HajimeCoreParams())).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for (((num1, num2), i) <- (multiplicandArray zip multiplierArray).zipWithIndex) {
        dut.io.req.valid.poke(true.B)
        dut.io.req.bits.multiplicand.bits.poke(num1.U(64.W))
        // dut.io.out.bits.multiplicand.signed.poke(false.B)
        dut.io.req.bits.multiplier.bits.poke(num2.U(64.W))
        // dut.io.out.bits.multiplier.signed.poke(false.B)
        dut.io.req.bits.tag.poke(i.U)
        dut.io.resp.ready.poke(true.B)
        dut.clock.step()
        if(dut.io.resp.valid.peekBoolean()) {
          resultArray :+= dut.io.resp.bits.result.peekInt()
          resultTagArray :+= dut.io.resp.bits.tag.peekInt()
        }
      }
      dut.io.req.valid.poke(false.B)
      dut.io.req.bits.multiplicand.bits.poke(0.U(64.W))
      dut.io.req.bits.multiplier.bits.poke(0.U(64.W))
      dut.io.req.bits.tag.poke(0.U)
      while(dut.io.resp.valid.peekBoolean()) {
        dut.clock.step()
        resultArray :+= dut.io.resp.bits.result.peekInt()
        resultTagArray :+= dut.io.resp.bits.tag.peekInt()
      }
      resultTagArray.lazyZip(resultArray.map(bigIntToString32format)).lazyZip(answerArray.map(bigIntToString32format)).toIndexedSeq.foreach {
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
  val multiplicandArray = (0 until HajimeCoreParams().robEntries).map(_ => BigInt("0000000000000000" + generateInt64RandomHexString(Random.nextInt(16)), 16))
  val multiplierArray = (0 until HajimeCoreParams().robEntries).map(_ => BigInt("0000000000000000" + generateInt64RandomHexString(Random.nextInt(16)), 16))
  it should s"perform multiplication" in {
    test(NonPipelinedMultiplier(HajimeCoreParams())).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for((num1, num2, i) <- (multiplicandArray zip multiplierArray).zipWithIndex.map {
        case ((num1, num2), i) => (num1, num2, i)
      }) {
        dut.io.req.valid.poke(true.B)
        dut.io.req.bits.multiplicand.bits.poke(num1)
        // dut.io.out.bits.multiplicand.signed.poke(false.B)
        dut.io.req.bits.multiplier.bits.poke(num2)
        // dut.io.out.bits.multiplier.signed.poke(false.B)
        dut.io.req.bits.tag.poke(i.U)
        dut.io.resp.ready.poke(true.B)
        while(!(dut.io.resp.valid.peekBoolean() && dut.io.resp.ready.peekBoolean())) {
          dut.clock.step()
          dut.io.req.valid.poke(false.B)
          dut.io.req.bits.multiplicand.bits.poke(0.U)
          dut.io.req.bits.multiplier.bits.poke(0.U)
          dut.io.req.bits.tag.poke(0.U)
          dut.io.resp.ready.poke(Random.nextBoolean().B)
        }
        val result = bigIntToString32format(dut.io.resp.bits.result.peekInt())
        val answer = bigIntToString32format(num1 * num2)
        dut.io.resp.bits.result.expect(num1 * num2)
        println(s"result: 0x$result, answer: 0x$answer")
        dut.clock.step()
      }
    }
  }
}
