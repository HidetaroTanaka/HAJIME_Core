package hajime.publicmodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO._
import hajime.common.COMPILE_CONSTANTS
import chiseltest._
import org.scalatest.flatspec._

import scala.io._

// Should I check unaligned exception in Core or Cache?
class IcacheForVerilator(memsize: Int = 0x2000) extends Module {
  val io = IO(Flipped(new AXI4liteIO(addrWidth = 64, dataWidth = 32)))
  // AR channel
  io.ar.ready := true.B
  // AW channel
  io.aw.ready := true.B
  // W channel
  io.w.ready := true.B

  // 0x000 ~ 0x1FFF -> 1byte * 8192
  // -> 4byte * 2048
  // 2048 -> 0x000~0x3FF -> 0~1023
  // 4096 -> 0x000~0x7FF -> 0~2023
  // 8192 -> 0x000~0xFFF -> 0~4096
  val mem = SyncReadMem(memsize, Vec(4, UInt(8.W)))

  // read
  val readDataFromMem = Wire(chiselTypeOf(io.r.bits))
  // Vecでは下位要素が先頭だが，信号では逆
  readDataFromMem.data := Cat(mem.read(io.ar.bits.addr.head(62)).reverse)
  readDataFromMem.resp := 0.U

  val rChannelBitsReg = Reg(chiselTypeOf(io.r.bits))
  val rChannelValidReg = Reg(Bool())
  val rStall = io.r.valid && !io.r.ready
  val retainRchannel = RegNext(rStall)
  when(rStall) {
    rChannelBitsReg := io.r.bits
    rChannelValidReg := io.r.valid
    io.ar.ready := false.B
  }.otherwise {
    rChannelBitsReg := readDataFromMem
    rChannelValidReg := RegNext(io.ar.valid && io.ar.ready)
  }

  io.r.bits := Mux(retainRchannel, rChannelBitsReg, readDataFromMem)
  io.r.valid := Mux(retainRchannel, rChannelValidReg, RegNext(io.ar.valid && io.ar.ready))

  // write
  io.b.bits.resp := 0.U
  val bValid = RegInit(false.B)
  val bResp = RegInit(0.U(3.W))
  val writeDataAsVec = Wire(Vec(4, UInt(8.W)))
  for((w,i) <- writeDataAsVec.zipWithIndex) {
    w := io.w.bits.data(8*i+7, 8*i)
  }
  when(io.aw.valid && io.w.valid) {
    mem.write(io.aw.bits.addr.head(62), writeDataAsVec, io.w.bits.strb.asBools)
    bValid := true.B
    bResp := 0.U
  } .otherwise {
    bValid := false.B
    bResp := 0.U
  }
  io.b.valid := bValid
  io.b.bits.resp := bResp
}

object IcacheForVerilator extends App {
  def apply(memsize: Int = 8192): IcacheForVerilator = new IcacheForVerilator(memsize)
  ChiselStage.emitSystemVerilogFile(IcacheForVerilator(memsize = 8192), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}

class IcacheForVerilatorSpec extends AnyFlatSpec with ChiselScalatestTester {
  it should "write and read correctly" in {
    test(IcacheForVerilator(memsize = 1024)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.io.ar.bits.addr.poke(0.U)
      dut.io.ar.bits.prot.poke(0.U)
      dut.io.ar.valid.poke(false.B)
      dut.io.aw.bits.addr.poke(0.U)
      dut.io.aw.bits.prot.poke(0.U)
      dut.io.aw.valid.poke(false.B)
      dut.io.b.ready.poke(true.B)
      dut.io.r.ready.poke(true.B)
      dut.io.w.bits.data.poke(0.U)
      dut.io.w.bits.strb.poke(0.U)
      dut.io.w.valid.poke(false.B)

      // write from file manually
      val fileSource = Source.fromFile("src/main/resources/rv64ui/add_inst.hex")
      var writtenArray: IndexedSeq[String] = Nil.toIndexedSeq
      // inputArray.foreach(println)
      for((data, idx) <- fileSource.getLines().zipWithIndex) {
        writtenArray :+= data
        dut.io.aw.bits.addr.poke((idx*4).U)
        dut.io.aw.valid.poke(true.B)
        dut.io.w.bits.data.poke(s"h$data".U(32.W))
        dut.io.w.bits.strb.poke(0xF.U(8.W))
        dut.io.w.valid.poke(true.B)
        dut.clock.step()
      }
      dut.io.aw.valid.poke(false.B)
      dut.io.w.valid.poke(false.B)
      dut.clock.step()
      for(i <- writtenArray.indices) {
        dut.io.ar.bits.addr.poke((i * 4).U)
        dut.io.ar.valid.poke(true.B)
        dut.clock.step()
        dut.io.r.bits.data.expect(s"h${writtenArray(i)}".U(32.W))
      }

      fileSource.close
    }
  }
}
