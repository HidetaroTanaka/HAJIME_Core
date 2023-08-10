package hajime.publicmodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO._
import hajime.common.COMPILE_CONSTANTS
import chiseltest._
import org.scalatest.flatspec._

import scala.io._

class Icache_for_Verilator(memsize: Int = 0x2000) extends Module with ChecksAxiReadResp with ChecksAxiWriteResp {
  val io = IO(Flipped(new AXI4liteIO(addr_width = 64, data_width = 32)))
  // AR channel
  io.ar.ready := true.B
  // AW channel
  io.aw.ready := true.B
  // W channel
  io.w.ready := true.B

  // 0x000 ~ 0x1FFC
  val mem = SyncReadMem(memsize/4, Vec(4, UInt(8.W)))
  val addr_reg = RegNext(io.ar.bits.addr)

  // read
  val readDataFromMem = Wire(chiselTypeOf(io.r.bits))
  // Vecでは下位要素が先頭だが，信号では逆
  readDataFromMem.data := Cat(mem.read(io.ar.bits.addr.head(62)).reverse)
  readDataFromMem.resp := MuxCase(R_OKEY.U, Seq(
    RegNext(io.ar.bits.addr > 0x1FFC.U(64.W)) -> R_DECERR.U,
    RegNext(!io.ar.bits.alignedToWord) -> R_SLVERR.U,
  ))

  val r_channel_bits_reg = Reg(chiselTypeOf(io.r.bits))
  val r_channel_valid_reg = Reg(Bool())
  val r_stall = io.r.valid && !io.r.ready
  val retain_r_channel = RegNext(r_stall)
  when(r_stall) {
    r_channel_bits_reg := io.r.bits
    r_channel_valid_reg := io.r.valid
    io.ar.ready := false.B
  }.otherwise {
    r_channel_bits_reg := readDataFromMem
    r_channel_valid_reg := RegNext(io.ar.valid && io.ar.ready)
  }

  io.r.bits := Mux(retain_r_channel, r_channel_bits_reg, readDataFromMem)
  io.r.valid := Mux(retain_r_channel, r_channel_valid_reg, RegNext(io.ar.valid && io.ar.ready))

  // write
  io.b.bits.resp := 0.U
  val b_valid = RegInit(false.B)
  val b_resp = RegInit(0.U(3.W))
  val writeData_asVec = Wire(Vec(4, UInt(8.W)))
  for((w,i) <- writeData_asVec.zipWithIndex) {
    w := io.w.bits.data(8*i+7, 8*i)
  }
  when(io.aw.valid && io.w.valid) {
    mem.write(io.aw.bits.addr.head(62), writeData_asVec, io.w.bits.strb.asBools)
    b_valid := true.B
    b_resp := MuxCase(W_OKEY.U, Seq(
      (io.aw.bits.addr > 0x1FFC.U(64.W)) -> W_DECERR.U,
      io.aw.bits.alignedToWord -> W_SLVERR.U,
    ))
  } .otherwise {
    b_valid := false.B
    b_resp := W_OKEY.U
  }
  io.b.valid := b_valid
  io.b.bits.resp := b_resp
}

object Icache_for_Verilator extends App {
  def apply(memsize: Int = 8192): Icache_for_Verilator = new Icache_for_Verilator(memsize)
  ChiselStage.emitSystemVerilogFile(Icache_for_Verilator(memsize = 8192), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}

class Icache_for_VerilatorSpec extends AnyFlatSpec with ChiselScalatestTester {
  it should "write and read correctly" in {
    test(Icache_for_Verilator(memsize = 1024)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
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
