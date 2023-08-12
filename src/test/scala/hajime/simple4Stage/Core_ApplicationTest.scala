package hajime.simple4Stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec._

import scala.io._

class Core_ApplicationTest extends AnyFlatSpec with ChiselScalatestTester {
  it should "print Hello World in C language" in {
    test(new Core_and_cache()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      def initialiseImem(filename: String): Unit = {
        dut.io.imem_initialiseAXI.ar.bits.addr.poke(0.U)
        dut.io.imem_initialiseAXI.ar.bits.prot.poke(0.U)
        dut.io.imem_initialiseAXI.ar.valid.poke(false.B)
        dut.io.imem_initialiseAXI.aw.bits.addr.poke(0.U)
        dut.io.imem_initialiseAXI.aw.bits.prot.poke(0.U)
        dut.io.imem_initialiseAXI.aw.valid.poke(false.B)
        dut.io.imem_initialiseAXI.b.ready.poke(true.B)
        dut.io.imem_initialiseAXI.r.ready.poke(true.B)
        dut.io.imem_initialiseAXI.w.bits.data.poke(0.U)
        dut.io.imem_initialiseAXI.w.bits.strb.poke(0.U)
        dut.io.imem_initialiseAXI.w.valid.poke(false.B)
        dut.io.icache_initialising.poke(true.B)
        // initialise icache from file
        val fileSource = Source.fromFile(filename)
        for ((data, idx) <- fileSource.getLines().zipWithIndex) {
          dut.io.imem_initialiseAXI.aw.bits.addr.poke((idx * 4).U)
          dut.io.imem_initialiseAXI.aw.valid.poke(true.B)
          dut.io.imem_initialiseAXI.w.bits.data.poke(s"h$data".U(32.W))
          dut.io.imem_initialiseAXI.w.bits.strb.poke(0xF.U(8.W))
          dut.io.imem_initialiseAXI.w.valid.poke(true.B)
          dut.clock.step()
        }
        fileSource.close()
        dut.io.imem_initialiseAXI.aw.valid.poke(false.B)
        dut.io.imem_initialiseAXI.w.valid.poke(false.B)
        dut.io.icache_initialising.poke(false.B)
      }

      def initialiseDmem(filename: String): Unit = {
        dut.io.dmem_initialiseAXI.ar.bits.addr.poke(0.U)
        dut.io.dmem_initialiseAXI.ar.bits.prot.poke(0.U)
        dut.io.dmem_initialiseAXI.ar.valid.poke(false.B)
        dut.io.dmem_initialiseAXI.aw.bits.addr.poke(0.U)
        dut.io.dmem_initialiseAXI.aw.bits.prot.poke(0.U)
        dut.io.dmem_initialiseAXI.aw.valid.poke(false.B)
        dut.io.dmem_initialiseAXI.b.ready.poke(true.B)
        dut.io.dmem_initialiseAXI.r.ready.poke(true.B)
        dut.io.dmem_initialiseAXI.w.bits.data.poke(0.U)
        dut.io.dmem_initialiseAXI.w.bits.strb.poke(0.U)
        dut.io.dmem_initialiseAXI.w.valid.poke(false.B)
        dut.io.dcache_initialising.poke(true.B)
        // initialise icache from file
        val fileSource = Source.fromFile(filename)
        for ((data, idx) <- fileSource.getLines().zipWithIndex) {
          dut.io.dmem_initialiseAXI.aw.bits.addr.poke((idx * 4 + 0x4000).U)
          dut.io.dmem_initialiseAXI.aw.valid.poke(true.B)
          dut.io.dmem_initialiseAXI.w.bits.data.poke(s"h$data".U(32.W))
          dut.io.dmem_initialiseAXI.w.bits.strb.poke(0xF.U(8.W))
          dut.io.dmem_initialiseAXI.w.valid.poke(true.B)
          dut.clock.step()
        }
        fileSource.close()
        dut.io.dmem_initialiseAXI.aw.valid.poke(false.B)
        dut.io.dmem_initialiseAXI.w.valid.poke(false.B)
        dut.io.dcache_initialising.poke(false.B)
      }
      def get_toHostChar(): Char = {
        dut.io.toHost.bits.peekInt().toChar
      }
      def get_toHostValid(): Boolean = {
        dut.io.toHost.valid.peekBoolean()
      }
      fork {
        initialiseImem("src/main/resources/applications/helloworld_inst.hex")
      } .fork {
        initialiseDmem("src/main/resources/applications/helloworld_data.hex")
      } .join()
      dut.clock.setTimeout(1024)
      dut.io.reset_vector.poke(0.U)
      dut.io.hartid.poke(0.U)

      var toHostWrittenChar: List[Char] = List()
      while (!(get_toHostValid() && (get_toHostChar() == '\u0000'))) {
        dut.clock.step()
        if (get_toHostValid()) {
          // print(get_toHostChar())
          toHostWrittenChar = toHostWrittenChar :+ get_toHostChar()
        }
      }
      dut.io.debug_io.debug_abi_map.a0.expect(0.U(64.W))
      toHostWrittenChar.foreach(print)
      println()
    }
  }
  /*
  it should "execute median" in {
    test(new hajime.simple4Stage.Core_and_cache(icache_hexfilename = "src/main/resources/applications/median_inst.hex", dcache_hexfilename = "src/main/resources/applications/median_data.hex")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(65536)
      def get_toHostChar(): Char = {
        dut.io.toHost.bits.peekInt().toChar
      }
      def get_toHostValid(): Boolean = {
        dut.io.toHost.valid.peekBoolean()
      }
      var toHostWrittenChar: List[Char] = List()
      while (!(get_toHostValid() && (get_toHostChar() == '\u0000'))) {
        dut.clock.step()
        if (get_toHostValid()) {
          // print(get_toHostChar())
          toHostWrittenChar = toHostWrittenChar :+ get_toHostChar()
        }
      }
      dut.io.debug_io.debug_abi_map.a0.expect(0.U(64.W))
      toHostWrittenChar.foreach(print)
      println()
      println(s"IPC for median test: ${dut.io.performance_counters.retired_inst_count.peek().litValue.toDouble / dut.io.performance_counters.cycle_count.peek().litValue.toDouble}")
    }
  }
   */
}
