package hajime.simple4Stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec._

import scala.io._

class Core_ApplicationTest extends AnyFlatSpec with ChiselScalatestTester {
  def initialiseImem(filename: String, dut: Core_and_cache): Unit = {
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

  def initialiseDmem(filename: String, dut: Core_and_cache): Unit = {
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

  def get_toHostChar(dut: Core_and_cache): Char = {
    dut.io.toHost.bits.peekInt().toChar
  }

  def get_toHostValid(dut: Core_and_cache): Boolean = {
    dut.io.toHost.valid.peekBoolean()
  }

  val rv64iTestList = Seq(
    "helloworld", "median", "printInt64", "selection_sort", "memcpy", "quicksort"
  )

  for(e <- rv64iTestList) {
    it should s"execute $e" in {
      test(new Core_and_cache()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        println(s"test $e:")
        fork {
          initialiseImem(s"src/main/resources/applications_rv64i/${e}_inst.hex", dut)
        }.fork {
          initialiseDmem(s"src/main/resources/applications_rv64i/${e}_data.hex", dut)
        }.join()
        dut.clock.setTimeout(65536)
        dut.io.reset_vector.poke(0.U)
        dut.io.hartid.poke(0.U)

        var toHostWrittenChar: List[Char] = List()
        while (!(get_toHostValid(dut) && (get_toHostChar(dut) == '\u0000'))) {
          dut.clock.step()
          if (get_toHostValid(dut)) {
            // print(get_toHostChar())
            toHostWrittenChar = toHostWrittenChar :+ get_toHostChar(dut)
          }
        }
        dut.io.debug_io.debug_abi_map.a0.expect(0.U(64.W))
        toHostWrittenChar.foreach(print)
        println()
      }
    }
  }

  val rv64mTestList = Seq(
    "factorial", "power", "vector_innerproduct"
  )

  for(e <- rv64mTestList) {
    it should s"execute $e" in {
      test(new Core_and_cache()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        println(s"test $e:")
        fork {
          initialiseImem(s"src/main/resources/applications_rv64m/${e}_inst.hex", dut)
        }.fork {
          initialiseDmem(s"src/main/resources/applications_rv64m/${e}_data.hex", dut)
        }.join()
        dut.clock.setTimeout(65536)
        dut.io.reset_vector.poke(0.U)
        dut.io.hartid.poke(0.U)

        var toHostWrittenChar: List[Char] = List()
        while (!(get_toHostValid(dut) && (get_toHostChar(dut) == '\u0000'))) {
          dut.clock.step()
          if (get_toHostValid(dut)) {
            // print(get_toHostChar())
            toHostWrittenChar = toHostWrittenChar :+ get_toHostChar(dut)
          }
        }
        dut.io.debug_io.debug_abi_map.a0.expect(0.U(64.W))
        toHostWrittenChar.foreach(print)
        println()
      }
    }
  }
}
