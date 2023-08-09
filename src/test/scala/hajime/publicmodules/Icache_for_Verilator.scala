package hajime.publicmodules

import chisel3._
import circt.stage.ChiselStage
import hajime.axiIO.AXI4liteIO
import hajime.common.COMPILE_CONSTANTS
import chiseltest._
import org.scalatest.flatspec._
import scala.io._

class Icache_for_Verilator(memsize: Int = 8192) extends Module {
  val io = IO(new Bundle {
    val axi = Flipped(new AXI4liteIO(addr_width = 64, data_width = 32))
    val initialising = Input(Bool())
  })
  // AR channel
  io.axi.ar.ready := true.B
  // AW channel
  io.axi.aw.ready := true.B
  // W channel
  io.axi.w.ready := true.B

  val mem = SyncReadMem(memsize, UInt(32.W))
  val addr_reg = RegNext(io.axi.ar.bits.addr)

  // read
  val r_channel_bits = Wire(chiselTypeOf(io.axi.r.bits))
  r_channel_bits.data := mem.read(io.axi.ar.bits.addr.head(62))
  r_channel_bits.resp := Mux(RegNext(io.axi.ar.bits.alignedToWord), 0.U, "b011".U)

  val r_channel_bits_reg = Reg(chiselTypeOf(io.axi.r.bits))
  val r_channel_valid_reg = Reg(Bool())
  val r_stall = io.axi.r.valid && !io.axi.r.ready
  val retain_r_channel = RegNext(r_stall)
  when(r_stall) {
    r_channel_bits_reg := r_channel_bits_reg
    r_channel_valid_reg := r_channel_valid_reg
  } .otherwise {
    r_channel_bits_reg := r_channel_bits
    r_channel_valid_reg := RegNext(io.axi.ar.valid && io.axi.ar.ready)
  }

  io.axi.r.bits := Mux(retain_r_channel, r_channel_bits_reg, r_channel_bits)
  io.axi.r.valid := !io.initialising && RegNext(io.axi.ar.valid && io.axi.ar.ready)

  // write
  io.axi.b.bits.resp := 0.U
  when(io.initialising) {
    val b_valid = RegInit(false.B)
    when(io.axi.aw.valid && io.axi.w.valid && io.axi.w.bits.strb === 0xF.U(4.W)) {
      mem.write(io.axi.aw.bits.addr.head(62), io.axi.w.bits.data)
      b_valid := true.B
    }
    io.axi.b.valid := b_valid
  } .otherwise {
    io.axi.b.valid := false.B
  }
}

object Icache_for_Verilator extends App {
  def apply(memsize: Int = 8192): Icache_for_Verilator = new Icache_for_Verilator(memsize)
  ChiselStage.emitSystemVerilogFile(Icache_for_Verilator(memsize = 8192), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}

class Icache_for_VerilatorSpec extends AnyFlatSpec with ChiselScalatestTester {
  it should "write and read correctly" in {
    test(Icache_for_Verilator(memsize = 1024)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.io.axi.ar.bits.addr.poke(0.U)
      dut.io.axi.ar.bits.prot.poke(0.U)
      dut.io.axi.ar.valid.poke(false.B)
      dut.io.axi.aw.bits.addr.poke(0.U)
      dut.io.axi.aw.bits.prot.poke(0.U)
      dut.io.axi.aw.valid.poke(false.B)
      dut.io.axi.b.ready.poke(true.B)
      dut.io.axi.r.ready.poke(true.B)
      dut.io.axi.w.bits.data.poke(0.U)
      dut.io.axi.w.bits.strb.poke(0.U)
      dut.io.axi.w.valid.poke(false.B)

      // write from file manually
      val fileSource = Source.fromFile("src/main/resources/rv64ui/add_inst.hex")
      var writtenArray: IndexedSeq[String] = Nil.toIndexedSeq
      // inputArray.foreach(println)
      dut.io.initialising.poke(true.B)
      for((data, idx) <- fileSource.getLines().zipWithIndex) {
        writtenArray :+= data
        dut.io.axi.aw.bits.addr.poke((idx*4).U)
        dut.io.axi.aw.valid.poke(true.B)
        dut.io.axi.w.bits.data.poke(s"h$data".U(32.W))
        dut.io.axi.w.bits.strb.poke(0xF.U(8.W))
        dut.io.axi.w.valid.poke(true.B)
        dut.clock.step()
      }
      dut.io.initialising.poke(false.B)
      dut.io.axi.aw.valid.poke(false.B)
      dut.io.axi.w.valid.poke(false.B)
      dut.clock.step()
      for(i <- writtenArray.indices) {
        dut.io.axi.ar.bits.addr.poke((i * 4).U)
        dut.io.axi.ar.valid.poke(true.B)
        dut.clock.step()
        dut.io.axi.r.bits.data.expect(s"h${writtenArray(i)}".U(32.W))
      }

      fileSource.close
    }
  }
}
