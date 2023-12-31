package hajime.vectormodules

import chisel3._
import chiseltest._
import hajime.common.HajimeCoreParams
import org.scalatest.flatspec._

import scala.util.Random

class VecRegFileSpec extends AnyFlatSpec with ChiselScalatestTester {
  def readWriteTest(dut: VecRegFile, sew: Int, vlen: Int): Unit = {
    var input_array: IndexedSeq[String] = Nil.toIndexedSeq
    println(s"sew=$sew (e${8 << sew}) test:")
    dut.io.readReq(1).req.sew.poke(sew.U(3.W))
    dut.io.readReq(1).req.idx.poke(0.U)
    dut.io.readReq(1).req.vs1.poke(0.U)
    dut.io.readReq(1).req.vs2.poke(0.U)
    dut.io.writeReq(1).valid.poke(true.B)
    dut.io.writeReq(1).bits.writeReq.poke(true.B)
    dut.io.writeReq(1).bits.vd.poke(3.U)
    dut.io.writeReq(1).bits.vtype.vsew.poke(sew.U)
    for (i <- 0 until vlen / (8 << sew)) {
      println(s"Write Index: $i")
      // e8 -> 256, e16 -> 65536, e32 -> 2^32, e64 -> 2^64
      // 0 -> 2, 1 -> 4, 2 -> 8, 3 -> 16
      val input = (0 until (2 << sew)).map(_ => Random.nextInt(16).toHexString).reduce(_ + _)
      println(s"Write Data: $input")
      dut.io.writeReq(1).bits.data.poke(s"h$input".U)
      input_array :+= input
      dut.io.writeReq(1).bits.index.poke(i.U)
      dut.clock.step()
    }
    dut.io.readReq(1).req.vs1.poke(3.U)
    dut.io.writeReq(1).valid.poke(false.B)
    dut.io.writeReq(1).bits.writeReq.poke(false.B)
    for (i <- 0 until vlen / (8 << sew)) {
      println(s"Read Index: $i")
      dut.io.readReq(1).req.idx.poke(i.U)
      println(s"Read Data: ${dut.io.readReq(1).resp.vs1Out.peekInt().toString(16)}")
      dut.io.readReq(1).resp.vs1Out.expect(s"h${
        val signExt = !((input_array(i)(0) >= '0') && (input_array(i)(0) <= '7'))
        val head = if(signExt) "F" else "0"
        val extString = sew match {
          case 0 => head * 14
          case 1 => head * 12
          case 2 => head * 8
          case 3 => ""
        }
        extString + input_array(i)
      }".U)
      dut.clock.step()
    }
    println()
  }
  Random.setSeed(0)
  it should "Write and Read VecRegFile" in {
    implicit val params: HajimeCoreParams = HajimeCoreParams()
    test(new VecRegFile(2)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      for(sew <- 0 until 4) readWriteTest(dut, sew, params.vlen)
      // vm test
      /*
      println("vm test:")
      var vmArray: IndexedSeq[Boolean] = Nil.toIndexedSeq
      dut.io.reqEx.valid.poke(true.B)
      dut.io.reqEx.bits.vd.poke(0.U)
      dut.io.reqEx.bits.sew.poke(0.U)
      dut.io.reqEx.bits.vm.poke(true.B)
      for (i <- 0 until params.vlen / 8) {
        println(s"Mask Write Index: $i")
        val input = Random.nextBoolean()
        println(s"Mask Write Data: $input")
        dut.io.reqEx.bits.data.poke(input.B)
        vmArray :+= input
        dut.io.reqEx.bits.index.poke(i.U)
        dut.clock.step()
      }
      dut.io.reqEx.valid.poke(false.B)
      for(i <- 0 until params.vlen / 8) {
        println(s"Mask Read Index: $i")
        dut.io.readIndex.poke(i.U)
        println(s"Mask Read Data: ${dut.io.vm.peekBoolean()}")
        dut.io.vm.expect(vmArray(i).B)
        dut.clock.step()
      }
      // check whether vm follows 4.5. Mask Register Layout
      dut.io.vs1.poke(0.U)
      dut.io.sew.poke(0.U)
      for(i <- 0 until params.vlen / 64) {
        println(s"Read Index: $i")
        dut.io.readIndex.poke(i.U)
        println(s"Read Data: ${dut.io.vs1Out.peekInt().toString(16)}")
        val expectedValueFromVs1 = vmArray.slice(i*8, (i+1)*8).map(if(_) "1" else "0").reverse.reduce(_ + _)
        dut.io.vs1Out.expect(s"b$expectedValueFromVs1".U)
        dut.clock.step()
      }
       */
    }
  }
}
